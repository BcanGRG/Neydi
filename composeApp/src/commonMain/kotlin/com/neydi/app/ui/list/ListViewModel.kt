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
import com.neydi.app.data.db.TripStatus
import com.neydi.app.data.receipt.downscaleForOcr
import com.neydi.app.data.receipt.deleteFileAt
import com.neydi.app.data.receipt.writeBytesTo
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import com.neydi.app.data.matchKey
import com.neydi.app.data.parseQuantity
import com.neydi.app.data.clipboardLines
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.repo.resolveProduct
import com.neydi.app.data.stats.ProductStatsRebuilder
import com.neydi.app.data.suggest.Suggestion
import com.neydi.app.data.suggest.SuggestionEngine
import com.neydi.app.ui.product.ProductSheetState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
    private val statsRebuilder: ProductStatsRebuilder,
    private val suggestionEngine: SuggestionEngine,
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

    /**
     * Alisveris modu GEZININ DURUMUNDAN turuyor, yerel bir bayraktan degil.
     *
     * Boylece mod uygulama yeniden baslayinca kayboluyor DEGIL, ve Faz 7'de
     * senkron gelince esler ayni modu gorecek - "ben marketteyim" bilgisi
     * dogal olarak paylasilan bir sey.
     */
    val shoppingMode: StateFlow<Boolean> = repo.activeTrip(household)
        .map { it?.status == TripStatus.SHOPPING }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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
            shoppingMode,
            emptyKind,
        ) { rows, memberId, mod, kind -> rows.toSections(memberId, mod, kind) }
            .stateIn(
                scope = viewModelScope,
                // 5 saniye: konfigurasyon degisiminde akis kopmasin, ama
                // uygulama arka plandayken veritabanini dinlemeye devam etmesin.
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ListState(),
            )

    /**
     * Motorun onerileri - TEK SERIT yaklasiminin veri yarisi (F6.3).
     *
     * Serit girdi BOSKEN bunlari, kullanici yazarken otomatik tamamlamayi
     * gosteriyor. Iki ayri serit tasarimda ust uste binerdi; tek seridin modu
     * girdinin bos olup olmamasi.
     *
     * `rows` akisina bagli: liste her degistiginde (ekleme, isaretleme, yeni
     * gezi) yeniden hesaplaniyor - boylece cipten eklenen urun aninda seritten
     * dusuyor, cunku motor aktif listedekini onermiyor.
     */
    val engineSuggestions: StateFlow<List<Suggestion>> =
        repo.rows(household)
            .map { suggestionEngine.suggestions(household) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Cipten ekleme - [Suggestion] urun kimligi tasiyor, katalog aramasi yok.
     *
     * `isFromSuggestion = true` YAZILIYOR: `TripLine.fromSuggestion` kolonu ve
     * bu parametre en bastan vardi ama hicbir cagiran doldurmuyordu - yani
     * oneri isabeti olculemiyordu. Artik olculuyor.
     */
    fun addFromEngine(suggestion: Suggestion) {
        viewModelScope.launch {
            val memberId = selfMemberId() ?: return@launch
            val trip = repo.openOrGetActiveTrip(household, memberId)
            val product = productDao.byId(suggestion.productId) ?: return@launch
            repo.add(
                householdId = household,
                tripId = trip.id,
                product = product,
                memberId = memberId,
                isFromSuggestion = true,
            )
        }
    }

    /**
     * Bu cihazin uyesi.
     *
     * Flow henuz yayin yapmadiysa DOGRUDAN veritabanindan okunuyor - sessizce
     * vazgecmek kullanicinin yazdigi seyin kaybolmasi demek olurdu. Bu, F3.2'de
     * cihazda yakalanan bir hatanin dersi: ViewModel uyeyi yalnizca `init`'te
     * bir kez okuyordu ve o an bos donerse **her ekleme sessizce hicbir sey
     * yapmiyordu**.
     *
     * Gezi acmak da artik uye istiyor (sabit tohumlamasi icin), o yuzden uc
     * cagri yeri bunu paylasiyor.
     */
    private suspend fun selfMemberId(): String? =
        myMemberId.value ?: memberDao.self(household)?.id

    /**
     * Urun Detayi sheet'i (Ekran 5) - HEDEF DEGIL, sheet.
     *
     * `Destinations.kt` karari yaziyor: *"EKLE ve URUN DETAYI de hedef degil,
     * Liste uzerinde acilan bottom sheet'lerdir."* O yuzden Nav3 back stack'inde
     * degil, ekranin kendi state'inde.
     */
    private val _productSheet = MutableStateFlow<ProductSheetState?>(null)
    val productSheet: StateFlow<ProductSheetState?> = _productSheet

    fun openProductSheet(productId: String) {
        viewModelScope.launch {
            val product = productDao.byId(productId) ?: return@launch
            _productSheet.value = ProductSheetState(
                productId = product.id,
                name = product.name,
                isStaple = product.isStaple,
            )
        }
    }

    fun closeProductSheet() {
        _productSheet.value = null
    }

    /**
     * "Her zamankiler"e ekler / cikarir (F6.8).
     *
     * Sheet'in kendi state'i de ANINDA guncelleniyor: anahtar yazmanin
     * veritabanindan geri okunmasini beklemiyor, yoksa parmak kalkinca anahtar
     * eski halinde donmus gorunurdu. Listedeki satir zaten Flow'dan tazeleniyor.
     */
    fun setStaple(productId: String, isStaple: Boolean) {
        viewModelScope.launch {
            repo.setStaple(productId, isStaple)
            _productSheet.update { current ->
                current?.takeIf { it.productId == productId }?.copy(isStaple = isStaple) ?: current
            }
        }
    }

    fun toggleChecked(rowId: String, checked: Boolean) {
        viewModelScope.launch { repo.toggleChecked(rowId, checked) }
    }

    fun setShoppingMode(enabled: Boolean) {
        viewModelScope.launch {
            val memberId = selfMemberId() ?: return@launch
            val trip = repo.openOrGetActiveTrip(household, memberId)
            repo.setShoppingMode(trip.id, enabled)
        }
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
            val memberId = selfMemberId() ?: return@launch
            val trip = repo.openOrGetActiveTrip(household, memberId)
            val rows = tripLineDao.observeList(trip.id).first()
            _summary.value = ShoppingSummary(
                takenCount = rows.count { it.checked },
                totalCount = rows.size,
                // Tutar fisten gelecek (Faz 4); simdilik bilinmiyor.
                amountMinor = trip.totalMinor,
                durationMinutes = null,
            )
            // TEK CIHAZ KAPATIR. Donen deger onemli: false ise gezi baska bir
            // cihazda zaten kapanmis ve BU cagri hicbir sey yazmadi. Ozet
            // kartini yine gosteriyoruz - kullanici bitirdigini gormeli - ama
            // mutabakat ikinci kez CALISMIYOR; calissaydi satin almalar cift
            // sayilir ve oneri araliklari yariya duserdi.
            _summaryTripId = trip.id
            val closed = repo.closeTrip(trip.id, memberId = memberId)
            if (closed) {
                // VARSAYILAN-IYIMSER MUTABAKAT (F4.8): isaretlenmemis planli
                // satirlar alindi sayiliyor. Yalnizca KAPATAN cihaz yaziyor -
                // closed false ise gezi baska cihazda kapanmis ve mutabakat
                // orada calismis; burada tekrar calistirmak satin almalari
                // cift sayardi.
                val reconciled = repo.reconcileOptimistically(trip.id)
                // Ozet karti mutabakat SONRASI sayiyi gostermeli: "3/5 aldim"
                // yazip ardindan besini de alinmis kaydetmek kullaniciya yalan
                // soylemek olurdu.
                if (reconciled > 0) {
                    _summary.value = _summary.value?.copy(takenCount = rows.size)
                }
                // ISTATISTIK YENIDEN KURULUMU MUTABAKATTAN SONRA (F6.1).
                //
                // Sira onemli: once cagrilsaydi yeni kapanan gezinin iyimser
                // mutabakatla alindi yazilan satirlari, o kapanisin tetikledigi
                // yeniden kurulumun DISINDA kalirdi. `closed` dalinda olmasi da
                // cift kapanis korumasini miras aliyor.
                statsRebuilder.rebuild(household)
            }
        }
    }

    /**
     * Okunacak fis hazir - kimligi Fis Kontrol ekranini acmak icin.
     *
     * TEK SEFERLIK OLAY, kalici state DEGIL: tuketilmezse konfigurasyon
     * degisiminde ayni ekran tekrar acilir.
     */
    private val _openReceiptId = MutableStateFlow<String?>(null)
    val openReceiptId: StateFlow<String?> = _openReceiptId

    fun consumeOpenReceipt() {
        _openReceiptId.value = null
    }

    /**
     * Cekilen fisi kuyruga alir. OCR BURADA KOSMUYOR.
     *
     * Sira onemli: gezi zaten kapanmis (ozet karti gorunuyor demek ki
     * kapandi), yani bu is hicbir seyi bekletmiyor. Kucultme basarisiz olursa
     * ORIJINAL yol saklaniyor - fotograf kullanicinin tek kaniti.
     */
    fun attachReceipt(source: PlatformFile, destPath: String, rawPath: String) {
        // Gezi kimligi ozet kartindan geliyor, openOrGetActiveTrip'ten DEGIL.
        // O cagri burada YENI bir gezi acardi - gezi kapali cunku - ve fis bos,
        // yeni acilmis yanlis geziye baglanirdi. Ustelik her fis ekleme hayalet
        // bir gezi dogururdu.
        val tripId = _summaryTripId ?: return
        viewModelScope.launch {
            // Baytlari BURADA okuyoruz: kaynak `content://` URI de olabilir
            // (kamera FileProvider uzerinden yaziyor) ve PlatformFile ikisini
            // de cozuyor. Kucultmeye yol vermek cihazda sessizce basarisizdi.
            val bytes = source.readBytes()
            val ok = downscaleForOcr(bytes, destPath)
            if (!ok) {
                // Kucultemedik: HAM BAYTLARI ayni yola yaz. Boylece kayitli yol
                // HER ZAMAN bizim dosyamiz - `content://` URI saklamak yanlis
                // olurdu, izin baglari kalici degil ve yol yarin cozulmez.
                writeBytesTo(destPath, bytes)
            }
            // Ham dosyayi SIL. Fis fotograflari kisisel veri; iki kopya tutmak
            // hem yer hem gereksiz maruziyet, kucultulmus hali OCR icin yeterli.
            //
            // YOL uzerinden, PlatformFile.delete() uzerinden DEGIL: kameranin
            // dondurdugu dosya bir `content://` URI ve onun uzerinden silme
            // cihazda sessizce hicbir sey yapmadi - ham dosya diskte kaldi.
            if (destPath != rawPath) deleteFileAt(rawPath)
            val receipt = repo.enqueueReceipt(
                householdId = household,
                tripId = tripId,
                imagePath = destPath,
            )
            // Fis Kontrol ekranini ACMAK ICIN kimligi yayinla. Tek seferlik
            // olay: tuketilmezse konfigurasyon degisiminde ekran ikinci kez
            // acilirdi.
            _openReceiptId.value = receipt.id
        }
    }

    /**
     * Ozet kartinin ait oldugu gezi. Fis ONA baglanmali.
     *
     * `openOrGetActiveTrip` KULLANILAMAZ: gezi kapandigi icin o cagri YENI bir
     * gezi acar ve fis yanlis geziye - bos, yeni acilmis olana - baglanirdi.
     * Sessiz bir yanlis eslesme; fis dogru okunur ama yanlis alisverisin
     * ustune yazilir.
     */
    private var _summaryTripId: String? = null

    /**
     * Ozet kartinin ait oldugu gezi. Bitir ekranina gecerken gerekiyor.
     *
     * openOrGetActiveTrip'ten OKUNMUYOR: gezi kapali oldugu icin o cagri YENI
     * bir gezi acardi ve duzeltme ekrani bos gorunurdu.
     */
    val summaryTripId: String? get() = _summaryTripId

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
            val memberId = selfMemberId() ?: return
            val trip = repo.openOrGetActiveTrip(household, memberId)

            // Kategori/kanonik ad cozumlemesi ORTAK: fis duzeltmesi de ayni
            // fonksiyondan geciyor (ProductResolver), yoksa iki kapi ayni urunu
            // iki farkli yazimla olusturabilirdi.
            val product = resolveProduct(
                repo = repo,
                catalogSeedDao = catalogSeedDao,
                householdId = household,
                name = name,
                categoryId = categoryId,
                unit = unit,
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
