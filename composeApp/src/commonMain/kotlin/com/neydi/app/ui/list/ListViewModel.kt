package com.neydi.app.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.CatalogSeedDao
import com.neydi.app.data.db.Category
import com.neydi.app.data.db.CategoryDao
import com.neydi.app.data.db.MemberDao
import com.neydi.app.data.db.PriceObservationDao
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.matchKey
import com.neydi.app.data.parseQuantity
import com.neydi.app.data.clipboardLines
import com.neydi.app.data.repo.ListRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Liste ekraninin durum sahibi.
 *
 * Satirlarin ESLESTIRILMESI (TripLine + Product + Category) SQL'de yapiliyor,
 * burada degil: uc ayri Flow'u combine etmek her degisimde uc yeniden yayin
 * uretir ve liste bir kare bosluklu gorunur.
 */
class ListViewModel(
    private val repo: ListRepository,
    private val tripLineDao: TripLineDao,
    private val memberDao: MemberDao,
    private val productDao: ProductDao,
    private val catalogSeedDao: CatalogSeedDao,
    private val categoryDao: CategoryDao,
    private val priceObservationDao: PriceObservationDao,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID
    /**
     * FLOW, tek seferlik okuma DEGIL: uyeyi bootstrap yaratiyor ve bu ekran
     * ondan once acilabiliyor. Tek okuma o yaristas null doner, bir daha
     * guncellenmez ve butun eklemeler sessizce hicbir sey yapmaz.
     */
    private val myMemberId: StateFlow<String?> =
        memberDao.observeSelf(household)
            .map { it?.id }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _shoppingMode = MutableStateFlow(false)
    val shoppingMode: StateFlow<Boolean> = _shoppingMode

    /**
     * Bos durumu ILK GUN mu DONGU ORTASI mi ayirt eder.
     * Hane hic urun gormediyse ilk gun; gormus ama liste bossa dongu ortasi.
     */
    private val emptyKind: Flow<EmptyKind> = productDao.observeAll(household)
        .map { if (it.isEmpty()) EmptyKind.ILK_GUN else EmptyKind.DONGU_ORTASI }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ListState> =
        combine(
            repo.activeTrip(household).flatMapLatest { trip ->
                if (trip == null) flowOf(emptyList()) else tripLineDao.observeList(trip.id)
            },
            myMemberId,
            _shoppingMode,
            emptyKind,
        ) { rows, memberId, mod, kind -> rows.toSections(memberId, mod, kind) }
            .stateIn(
                scope = viewModelScope,
                // 5 saniye: konfigurasyon degisiminde akis kopmasin, ama
                // uygulama arka plandayken veritabanini dinlemeye devam etmesin.
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ListState(),
            )

    fun toggleChecked(rowId: String, checked: Boolean) {
        viewModelScope.launch { repo.toggleChecked(rowId, checked) }
    }

    fun setShoppingMode(enabled: Boolean) {
        _shoppingMode.value = enabled
    }

    /** Bos durumdaki reyon cipleri. */
    val categories: StateFlow<List<Category>> = categoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Bir reyona dokununca o reyonun en yaygin urunleri oneri olur. */
    fun onCategorySelected(category: Category) {
        viewModelScope.launch {
            _suggestions.value = catalogSeedDao.byCategory(category.id, limit = 8)
            _input.value = ""
        }
    }

    // --- Ekle sheet (F3.7) -----------------------------------------------

    private val _sheetOpen = MutableStateFlow(false)
    val sheetOpen: StateFlow<Boolean> = _sheetOpen

    private val _sheetCategory = MutableStateFlow<Category?>(null)
    val sheetCategory: StateFlow<Category?> = _sheetCategory

    private val _sheetProducts = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val sheetProducts: StateFlow<List<CatalogSeed>> = _sheetProducts

    fun openSheet() { _sheetOpen.value = true }

    fun closeSheet() {
        _sheetOpen.value = false
        _sheetCategory.value = null
        _sheetProducts.value = emptyList()
    }

    fun selectSheetCategory(category: Category) {
        _sheetCategory.value = category
        viewModelScope.launch {
            // Sheet'te daha fazla urun: burada yer var, oneri seridinde yok.
            _sheetProducts.value = catalogSeedDao.byCategory(category.id, limit = 30)
        }
    }

    fun sheetBack() {
        _sheetCategory.value = null
        _sheetProducts.value = emptyList()
    }

    /** Sheet'ten urun eklemek sheet'i KAPATMAZ: pes pese ekleme normal. */
    fun addFromSheet(seed: CatalogSeed) {
        addInternal(seed.name, seed.categoryId, seed.defaultUnit, 1.0)
    }

    // --- Sepet tahmini + ozet (F3.8) ---------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    val estimate: StateFlow<BasketEstimate> =
        repo.activeTrip(household).flatMapLatest { trip ->
            if (trip == null) {
                flowOf(BasketEstimate())
            } else {
                combine(
                    priceObservationDao.observeEstimate(trip.id),
                    priceObservationDao.observePricedCount(trip.id),
                ) { amount, fiyatli -> BasketEstimate(amount, fiyatli) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BasketEstimate())

    private val _summary = MutableStateFlow<ShoppingSummary?>(null)
    val summary: StateFlow<ShoppingSummary?> = _summary

    /**
     * Alisverisi bitirir ve ozet kartini acar.
     *
     * Sure alisverisin BASLAMA aninda degil, alisveris moduna GECILDIGINDE
     * baslamis sayilmali - liste evde bir gun once aciliyor olabilir ve
     * "26 saat surdu" yazmak sacma olurdu. Trip.startedAt su an liste
     * acilisini tutuyor; F4'te alisveris modu zamani ayri saklanacak.
     */
    fun finishShopping() {
        viewModelScope.launch {
            val trip = repo.openOrGetActiveTrip(household)
            val rows = tripLineDao.observeList(trip.id).first()
            _summary.value = ShoppingSummary(
                takenCount = rows.count { it.checked },
                totalCount = rows.size,
                // Tutar fisten gelecek (Faz 4); simdilik bilinmiyor.
                amountMinor = trip.totalMinor,
                durationMinutes = null,
            )
            repo.finishShopping(trip.id)
            _shoppingMode.value = false
        }
    }

    fun dismissSummary() { _summary.value = null }

    /**
     * PANODAN TOPLU EKLEME (F3.4).
     *
     * Mevcut aliskanligi dogrudan degistiren en ucuz kazanc: liste bugun
     * WhatsApp'ta yaziliyor. Kopyalayip yapistirmak, 12 urunu tek tek
     * yazmaya gore dakikalar kazandiriyor.
     *
     * Her satir miktar ayristiricidan geciyor, yani "2 kg elma" panodan da
     * dogru dusuyor. Bos satirlar ve madde isaretleri atiliyor.
     */
    fun addFromClipboard(text: String) {
        val rows = clipboardLines(text)
        if (rows.isEmpty()) return
        viewModelScope.launch {
            rows.forEach { row ->
                val m = parseQuantity(row)
                if (m.name.isNotBlank()) addAndAwait(m.name, null, m.unit, m.count)
            }
        }
    }

    fun remove(rowId: String) {
        viewModelScope.launch { repo.remove(rowId) }
    }

    // --- Hizli ekleme (F3.3) --------------------------------------------

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input

    /**
     * Otomatik tamamlama. SKORA GORE siralaniyor, alfabetik DEGIL - kullanici
     * "ek" yazinca once Ekmek gormeli. Bu, algilanan zekanin buyuk kismini
     * sifir maliyetle veriyor; alfabetik siralamak ayni veriyle aptal gorunur.
     */
    private val _suggestions = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val suggestions: StateFlow<List<CatalogSeed>> = _suggestions

    fun onInputChanged(value: String) {
        _input.value = value
        viewModelScope.launch {
            // Miktar onekini ATIP arama yapiyoruz: "2 kg el" yazan biri
            // "el" ile eslesme bekler, "2 kg el" ile degil.
            val name = parseQuantity(value).name
            _suggestions.value = if (name.isBlank()) {
                emptyList()
            } else {
                catalogSeedDao.search(matchKey(name), limit = 6)
            }
        }
    }

    /** Serbest metinden ekle: "2 kg elma" gibi. */
    fun add(text: String) {
        val m = parseQuantity(text)
        if (m.name.isBlank()) return
        addInternal(name = m.name, categoryId = null, unit = m.unit, count = m.count)
    }

    /** Oneri cipinden ekle: kategori ve birim katalogdan gelir. */
    fun addFromSuggestion(seed: CatalogSeed) {
        val m = parseQuantity(_input.value)
        addInternal(
            name = seed.name,
            categoryId = seed.categoryId,
            unit = m.unit ?: seed.defaultUnit,
            count = m.count,
        )
    }

    private fun addInternal(name: String, categoryId: String?, unit: String?, count: Double) {
        viewModelScope.launch { addAndAwait(name, categoryId, unit, count) }
    }

    /**
     * Panodan toplu ekleme SIRAYLA calismali, o yuzden suspend. Her satiri
     * ayri coroutine'e atsaydik ayni urunu iki kez iceren bir pano iki satir
     * acmayi deneyip UNIQUE kisitina carpardi.
     */
    private suspend fun addAndAwait(name: String, categoryId: String?, unit: String?, count: Double) {
            // Flow henuz yayin yapmadiysa DOGRUDAN oku. Sessizce vazgecmek
            // kullanicinin yazdigi seyin kaybolmasi demek olurdu.
            val memberId = myMemberId.value ?: memberDao.self(household)?.id ?: return
            val trip = repo.openOrGetActiveTrip(household)

            // Kategori verilmediyse katalogda ayni matchKey'i ara: kullanici
            // elle yazsa bile urun dogru reyona dussun. Bulunmazsa son care
            // olarak "Temel Gida" - kategorisiz urun listede bolumsuz kalirdi.
            val key = matchKey(name)
            val seed = if (categoryId != null) {
                null
            } else {
                catalogSeedDao.search(key, limit = 1).firstOrNull { it.matchKey == key }
            }

            val product = repo.findOrCreateProduct(
                householdId = household,
                // Katalogda eslesme varsa KANONIK adi kullan: "sut" yazan biri
                // listede "Süt" gormeli. Yazildigi gibi birakmak ilk yazanin
                // yazimini kalici yapar - matchKey ikisini ayni urun saydigi
                // icin sonradan duzeltilemez de.
                name = seed?.name ?: name,
                categoryId = categoryId ?: seed?.categoryId ?: DEFAULT_CATEGORY,
                unit = unit ?: seed?.defaultUnit ?: "adet",
            )
            repo.add(
                householdId = household,
                tripId = trip.id,
                product = product,
                memberId = memberId,
                count = count,
            )
            _input.value = ""
            _suggestions.value = emptyList()
    }

    private companion object {
        /** Katalogda eslesmeyen serbest urunler buraya duser. */
        const val DEFAULT_CATEGORY = "temel-gida"
    }
}

/** Fiyati bilinen urunlerin toplami ve kacinin bilindigi. */
data class BasketEstimate(
    val amountMinor: Long = 0,
    val pricedCount: Int = 0,
)

data class ShoppingSummary(
    val takenCount: Int,
    val totalCount: Int,
    val amountMinor: Long?,
    val durationMinutes: Int?,
)
