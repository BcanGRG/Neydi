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

    private val hane = DEFAULT_HOUSEHOLD_ID
    /**
     * FLOW, tek seferlik okuma DEGIL: uyeyi bootstrap yaratiyor ve bu ekran
     * ondan once acilabiliyor. Tek okuma o yaristas null doner, bir daha
     * guncellenmez ve butun eklemeler sessizce hicbir sey yapmaz.
     */
    private val benimUyeId: StateFlow<String?> =
        memberDao.observeSelf(hane)
            .map { it?.id }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _alisverisModu = MutableStateFlow(false)
    val alisverisModu: StateFlow<Boolean> = _alisverisModu

    /**
     * Bos durumu ILK GUN mu DONGU ORTASI mi ayirt eder.
     * Hane hic urun gormediyse ilk gun; gormus ama liste bossa dongu ortasi.
     */
    private val bosTur: Flow<EmptyKind> = productDao.observeAll(hane)
        .map { if (it.isEmpty()) EmptyKind.ILK_GUN else EmptyKind.DONGU_ORTASI }

    @OptIn(ExperimentalCoroutinesApi::class)
    val durum: StateFlow<ListState> =
        combine(
            repo.activeTrip(hane).flatMapLatest { trip ->
                if (trip == null) flowOf(emptyList()) else tripLineDao.observeList(trip.id)
            },
            benimUyeId,
            _alisverisModu,
            bosTur,
        ) { satirlar, uyeId, mod, tur -> satirlar.toSections(uyeId, mod, tur) }
            .stateIn(
                scope = viewModelScope,
                // 5 saniye: konfigurasyon degisiminde akis kopmasin, ama
                // uygulama arka plandayken veritabanini dinlemeye devam etmesin.
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ListState(),
            )

    fun toggleChecked(satirId: String, isaretli: Boolean) {
        viewModelScope.launch { repo.toggleChecked(satirId, isaretli) }
    }

    fun setShoppingMode(acik: Boolean) {
        _alisverisModu.value = acik
    }

    /** Bos durumdaki reyon cipleri. */
    val kategoriler: StateFlow<List<Category>> = categoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Bir reyona dokununca o reyonun en yaygin urunleri oneri olur. */
    fun onCategorySelected(kategori: Category) {
        viewModelScope.launch {
            _oneriler.value = catalogSeedDao.byCategory(kategori.id, limit = 8)
            _girdi.value = ""
        }
    }

    // --- Ekle sheet (F3.7) -----------------------------------------------

    private val _sheetAcik = MutableStateFlow(false)
    val sheetAcik: StateFlow<Boolean> = _sheetAcik

    private val _sheetKategori = MutableStateFlow<Category?>(null)
    val sheetKategori: StateFlow<Category?> = _sheetKategori

    private val _sheetUrunler = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val sheetUrunler: StateFlow<List<CatalogSeed>> = _sheetUrunler

    fun openSheet() { _sheetAcik.value = true }

    fun closeSheet() {
        _sheetAcik.value = false
        _sheetKategori.value = null
        _sheetUrunler.value = emptyList()
    }

    fun selectSheetCategory(kategori: Category) {
        _sheetKategori.value = kategori
        viewModelScope.launch {
            // Sheet'te daha fazla urun: burada yer var, oneri seridinde yok.
            _sheetUrunler.value = catalogSeedDao.byCategory(kategori.id, limit = 30)
        }
    }

    fun sheetBack() {
        _sheetKategori.value = null
        _sheetUrunler.value = emptyList()
    }

    /** Sheet'ten urun eklemek sheet'i KAPATMAZ: pes pese ekleme normal. */
    fun addFromSheet(seed: CatalogSeed) {
        addInternal(seed.name, seed.categoryId, seed.defaultUnit, 1.0)
    }

    // --- Sepet tahmini + ozet (F3.8) ---------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    val tahmin: StateFlow<BasketEstimate> =
        repo.activeTrip(hane).flatMapLatest { trip ->
            if (trip == null) {
                flowOf(BasketEstimate())
            } else {
                combine(
                    priceObservationDao.observeEstimate(trip.id),
                    priceObservationDao.observePricedCount(trip.id),
                ) { tutar, fiyatli -> BasketEstimate(tutar, fiyatli) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BasketEstimate())

    private val _ozet = MutableStateFlow<ShoppingSummary?>(null)
    val ozet: StateFlow<ShoppingSummary?> = _ozet

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
            val trip = repo.openOrGetActiveTrip(hane)
            val satirlar = tripLineDao.observeList(trip.id).first()
            _ozet.value = ShoppingSummary(
                alinanSayisi = satirlar.count { it.isaretli },
                toplamSayisi = satirlar.size,
                // Tutar fisten gelecek (Faz 4); simdilik bilinmiyor.
                tutarKurus = trip.totalMinor,
                sureDakika = null,
            )
            repo.finishShopping(trip.id)
            _alisverisModu.value = false
        }
    }

    fun dismissSummary() { _ozet.value = null }

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
    fun addFromClipboard(metin: String) {
        val satirlar = clipboardLines(metin)
        if (satirlar.isEmpty()) return
        viewModelScope.launch {
            satirlar.forEach { satir ->
                val m = parseQuantity(satir)
                if (m.ad.isNotBlank()) addAndAwait(m.ad, null, m.birim, m.adet)
            }
        }
    }

    fun remove(satirId: String) {
        viewModelScope.launch { repo.remove(satirId) }
    }

    // --- Hizli ekleme (F3.3) --------------------------------------------

    private val _girdi = MutableStateFlow("")
    val girdi: StateFlow<String> = _girdi

    /**
     * Otomatik tamamlama. SKORA GORE siralaniyor, alfabetik DEGIL - kullanici
     * "ek" yazinca once Ekmek gormeli. Bu, algilanan zekanin buyuk kismini
     * sifir maliyetle veriyor; alfabetik siralamak ayni veriyle aptal gorunur.
     */
    private val _oneriler = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val oneriler: StateFlow<List<CatalogSeed>> = _oneriler

    fun onInputChanged(yeni: String) {
        _girdi.value = yeni
        viewModelScope.launch {
            // Miktar onekini ATIP arama yapiyoruz: "2 kg el" yazan biri
            // "el" ile eslesme bekler, "2 kg el" ile degil.
            val ad = parseQuantity(yeni).ad
            _oneriler.value = if (ad.isBlank()) {
                emptyList()
            } else {
                catalogSeedDao.search(matchKey(ad), limit = 6)
            }
        }
    }

    /** Serbest metinden ekle: "2 kg elma" gibi. */
    fun add(metin: String) {
        val m = parseQuantity(metin)
        if (m.ad.isBlank()) return
        addInternal(ad = m.ad, kategoriId = null, birim = m.birim, adet = m.adet)
    }

    /** Oneri cipinden ekle: kategori ve birim katalogdan gelir. */
    fun addFromSuggestion(seed: CatalogSeed) {
        val m = parseQuantity(_girdi.value)
        addInternal(
            ad = seed.name,
            kategoriId = seed.categoryId,
            birim = m.birim ?: seed.defaultUnit,
            adet = m.adet,
        )
    }

    private fun addInternal(ad: String, kategoriId: String?, birim: String?, adet: Double) {
        viewModelScope.launch { addAndAwait(ad, kategoriId, birim, adet) }
    }

    /**
     * Panodan toplu ekleme SIRAYLA calismali, o yuzden suspend. Her satiri
     * ayri coroutine'e atsaydik ayni urunu iki kez iceren bir pano iki satir
     * acmayi deneyip UNIQUE kisitina carpardi.
     */
    private suspend fun addAndAwait(ad: String, kategoriId: String?, birim: String?, adet: Double) {
            // Flow henuz yayin yapmadiysa DOGRUDAN oku. Sessizce vazgecmek
            // kullanicinin yazdigi seyin kaybolmasi demek olurdu.
            val uyeId = benimUyeId.value ?: memberDao.self(hane)?.id ?: return
            val trip = repo.openOrGetActiveTrip(hane)

            // Kategori verilmediyse katalogda ayni matchKey'i ara: kullanici
            // elle yazsa bile urun dogru reyona dussun. Bulunmazsa son care
            // olarak "Temel Gida" - kategorisiz urun listede bolumsuz kalirdi.
            val anahtar = matchKey(ad)
            val seed = if (kategoriId != null) {
                null
            } else {
                catalogSeedDao.search(anahtar, limit = 1).firstOrNull { it.matchKey == anahtar }
            }

            val urun = repo.findOrCreateProduct(
                householdId = hane,
                // Katalogda eslesme varsa KANONIK adi kullan: "sut" yazan biri
                // listede "Süt" gormeli. Yazildigi gibi birakmak ilk yazanin
                // yazimini kalici yapar - matchKey ikisini ayni urun saydigi
                // icin sonradan duzeltilemez de.
                ad = seed?.name ?: ad,
                kategoriId = kategoriId ?: seed?.categoryId ?: VARSAYILAN_KATEGORI,
                birim = birim ?: seed?.defaultUnit ?: "adet",
            )
            repo.add(
                householdId = hane,
                tripId = trip.id,
                product = urun,
                memberId = uyeId,
                adet = adet,
            )
            _girdi.value = ""
            _oneriler.value = emptyList()
    }

    private companion object {
        /** Katalogda eslesmeyen serbest urunler buraya duser. */
        const val VARSAYILAN_KATEGORI = "temel-gida"
    }
}

/** Fiyati bilinen urunlerin toplami ve kacinin bilindigi. */
data class BasketEstimate(
    val tutarKurus: Long = 0,
    val fiyatliSayisi: Int = 0,
)

data class ShoppingSummary(
    val alinanSayisi: Int,
    val toplamSayisi: Int,
    val tutarKurus: Long?,
    val sureDakika: Int?,
)
