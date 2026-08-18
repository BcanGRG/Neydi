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
import com.neydi.app.data.db.TripDao
import com.neydi.app.data.db.TripLine
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.db.TripStatus
import com.neydi.app.data.matchKey
import com.neydi.app.data.parseQuantity
import com.neydi.app.data.clipboardLines
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.repo.resolveProduct
import com.neydi.app.data.stats.ProductStatsRebuilder
import com.neydi.app.data.suggest.Suggestion
import com.neydi.app.data.suggest.SuggestionEngine
import com.neydi.app.ui.components.turkishInitials
import com.neydi.app.ui.product.ProductSheetState
import com.neydi.app.ui.product.toPriceSection
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
    private val tripDao: TripDao,
    private val tripLineDao: TripLineDao,
    private val memberDao: MemberDao,
    private val productDao: ProductDao,
    private val catalogSeedDao: CatalogSeedDao,
    private val categoryDao: CategoryDao,
    private val priceObservationDao: PriceObservationDao,
    private val statsRebuilder: ProductStatsRebuilder,
    private val suggestionEngine: SuggestionEngine,
    /** Fiyat ipucunun yas hesabi icin - test saat kurmadan kossun diye disaridan. */
    private val clock: () -> Long,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    // TAKILI FIS KUYRUGU E7'DE OLDU. Bu ekranin init'i arka planda OCR
    // kosturuyordu; etiket modelinde boyle bir kuyruk YOK - gozlem cekim
    // aninda, kullanici onaylayarak yaziliyor, yani sonradan islenecek bir sey
    // birakmiyor. Liste ekrani artik yalnizca listenin isini yapiyor.

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

    /**
     * Basligin alt satirini besleyen son KAPANMIS gezi (Ekran 1 tasarimi).
     *
     * `observeHistory` zaten yalnizca kapanmis gezileri, `completedAt DESC`
     * siralamasiyla donduruyor - ilki en sonuncusu.
     */
    /**
     * Basligin TAMAMINI besleyen tek akis: son gezi + avatar.
     *
     * TEK AKISTA BIRLESTIRILDI cunku `combine` bes akistan sonra tipli
     * asiri yukleme sunmuyor ve vararg surumu her seyi `Any?` yapiyor -
     * yani tip guvenligi sessizce kayboluyordu.
     */
    private val header: Flow<HeaderData> = combine(
        tripDao.observeHistory(household, limit = 1),
        memberDao.observeSelf(household),
        memberDao.observeAll(household),
        priceObservationDao.observeTripEstimates(household),
    ) { trips, self, all, estimates ->
        val byTrip = estimates.associateBy { it.tripId }
        HeaderData(
            lastTrip = trips.firstOrNull()?.let { trip ->
                trip.completedAt?.let {
                    // TUTAR ESIGI GECMIYORSA NULL ve baslik onu hic yazmiyor
                    // (`lastTripSummary`). "Son alisveris: dun · ~40 TL" on
                    // sekiz urunluk bir gezide yanlis bir guven verirdi.
                    LastTrip(closedAt = it, totalMinor = byTrip[trip.id].shownMinor())
                }
            },
            selfInitials = self?.displayName?.let { turkishInitials(it) },
            hasPartner = all.size > 1,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ListState> =
        combine(
            repo.activeTrip(household).flatMapLatest { trip ->
                if (trip == null) flowOf(emptyList()) else tripLineDao.observeList(trip.id)
            },
            myMemberId,
            shoppingMode,
            emptyKind,
            header,
        ) { rows, memberId, mod, kind, head ->
            rows.toSections(memberId, mod, kind, now = clock()).copy(
                lastTrip = head.lastTrip,
                selfInitials = head.selfInitials,
                hasPartner = head.hasPartner,
            )
        }
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
     * SERIDIN TEK MODU KALDI. Ikinci mod otomatik tamamlamaydi ve kokteki
     * metin alanina baglaydi; karar 63 o alani kaldirdi, tamamlama Ekle
     * sheet'inin arama alanina tasindi.
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
    /** Sheet acik kalirken eklenen urun sayisi (Ekran 2 tasarimi). */
    private val _sheetAddedCount = MutableStateFlow(0)
    val sheetAddedCount: StateFlow<Int> = _sheetAddedCount

    fun addFromEngine(suggestion: Suggestion) {
        viewModelScope.launch {
            val memberId = selfMemberId() ?: return@launch
            val trip = repo.openOrGetActiveTrip(household, memberId)
            val product = productDao.byId(suggestion.productId) ?: return@launch
            signalAdded(
                repo.add(
                    householdId = household,
                    tripId = trip.id,
                    product = product,
                    memberId = memberId,
                    isFromSuggestion = true,
                ),
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

    /**
     * @param rowId sheet'in acildigi SATIR - "Listeden cikar" bunu siliyor
     *   (tasarim karari 38). Urun kimligi yetmiyor: ayni urun baska bir gezide
     *   de olabilir ve silinecek olan bu gezideki satir.
     */
    fun openProductSheet(productId: String, rowId: String? = null) {
        viewModelScope.launch {
            val product = productDao.byId(productId) ?: return@launch
            _productSheet.value = ProductSheetState(
                productId = product.id,
                rowId = rowId,
                name = product.name,
                isStaple = product.isStaple,
            )
            // FIYAT BOLUMU AYRI VE SONRA: sheet gozlemleri BEKLEMEDEN aciliyor.
            // Tek atisla beklenseydi dokunusla acilis arasinda bir sorgu
            // gecikmesi olurdu; sheet zaten sifir-gozlem halini cizebiliyor.
            priceObservationDao.history(household, product.id, HISTORY_LIMIT)
                .collect { rows ->
                    _productSheet.update { current ->
                        if (current?.productId != product.id) return@update current
                        current.copy(price = rows.toPriceSection())
                    }
                }
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

    /**
     * Dongu ortasi bos durumunun hayalet butonu (Ekran 1 tasarimi).
     *
     * Zaten listede olan urun ATLANIYOR - bkz. `ListRepository.addFromLastTrip`.
     */
    fun addFromLastTrip() {
        viewModelScope.launch {
            val memberId = selfMemberId() ?: return@launch
            repo.addFromLastTrip(household, memberId)
        }
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

    /**
     * Bos durumdaki bir reyona dokunus - o reyonun urunleri EKLE SHEET'INDE.
     *
     * Once kokteki oneri seridini dolduruyordu; karar 63 o seridi tek moda
     * (motorun onerileri) indirdi ve katalog gezinmesini sheet'e verdi.
     * Reyondan urun secmek zaten sheet'in kendi akisi.
     */
    fun onCategorySelected(category: Category) {
        openSheet()
        selectSheetCategory(category)
    }

    // --- Ekle sheet (F3.7) -----------------------------------------------

    private val _sheetOpen = MutableStateFlow(false)
    val sheetOpen: StateFlow<Boolean> = _sheetOpen

    private val _sheetCategory = MutableStateFlow<Category?>(null)
    val sheetCategory: StateFlow<Category?> = _sheetCategory

    private val _sheetProducts = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val sheetProducts: StateFlow<List<CatalogSeed>> = _sheetProducts

    /**
     * Listedeki urunlerin `matchKey`leri - Ekle sheet'indeki isaret icin
     * (tasarim karari 12).
     *
     * `matchKey` uzerinden, urun kimligi uzerinden DEGIL: katalog tohumu ile
     * kullanicinin kendi ekledigi urun ayri satirlar olabilir ama ayni seyi
     * anlatiyorlar - "Sut" iki kez isaretsiz gorunmemeli.
     */
    val listMatchKeys: StateFlow<Set<String>> =
        repo.rows(household)
            .map { rows ->
                rows.mapNotNull { productDao.byId(it.productId)?.matchKey }.toSet()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /**
     * Ilk gun bos durumunun 12 cipi (tasarim karari 5).
     *
     * URUN cipi, REYON cipi degil: urun cipi tek dokunusta listeye
     * dusuruyor, reyon cipi bir adim daha ekliyor - ve bos durumun tek isi
     * ilk satiri en kisa yoldan dogurmak.
     */
    private val _starterProducts = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val starterProducts: StateFlow<List<CatalogSeed>> = _starterProducts

    // YUKLEME BURADA, YUKARIDAKI init BLOGUNDA DEGIL.
    //
    // Kotlin ozellik baslaticilarini ve init bloklarini BILDIRIM SIRASINA
    // gore kosturuyor: yukaridaki init calistiginda `_starterProducts` henuz
    // null ve uygulama acilista NullPointerException ile cokuyordu. Derleme
    // ve testler bunu goremedi - yalnizca cihazda goruldu.
    init {
        viewModelScope.launch {
            _starterProducts.value = catalogSeedDao.mostCommon(limit = 12)
        }
    }

    /** Sheet ici arama (Ekran 2 tasarimi). */
    private val _sheetQuery = MutableStateFlow("")
    val sheetQuery: StateFlow<String> = _sheetQuery

    private val _sheetResults = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val sheetResults: StateFlow<List<CatalogSeed>> = _sheetResults

    fun onSheetQueryChanged(text: String) {
        _sheetQuery.value = text
        viewModelScope.launch {
            _sheetResults.value = if (text.isBlank()) {
                emptyList()
            } else {
                // matchKey uzerinden: kullanici "sut" yazinca "Sut" bulunmali
                // (bkz. MatchKey.kt'nin Turkce buyuk/kucuk harf tuzagi).
                catalogSeedDao.search(matchKey(text), limit = 20)
            }
        }
    }

    fun openSheet() {
        _sheetOpen.value = true
        _sheetQuery.value = ""
        _sheetResults.value = emptyList()
        // Sayac her acilista SIFIRLANIYOR: "3 urun eklendi" bu oturumun
        // sayisi, hanenin toplam gecmisi degil.
        _sheetAddedCount.value = 0
    }

    /**
     * Sheet'teki `"kuru kayısı" ekle` butonu - GERCEKTEN EKLIYOR.
     *
     * Once yalnizca sheet'i KAPATIYORDU ve o zaman anlasilabilirdi: kokte bir
     * metin alani vardi, kullanici oraya donup yazabilirdi. Karar 63 o alani
     * kaldirdi; buton eskisi gibi kalsaydi katalogda olmayan bir urun
     * uygulamaya HIC girilemez olurdu.
     *
     * [add] cagriliyor, `addFromSheet` degil: sheet'in arama metni serbest bir
     * cumle ("2 kg kuru kayisi") ve miktar ayristirmasi orada.
     */
    fun addSheetQuery() {
        val text = _sheetQuery.value
        if (text.isBlank()) return
        add(text)
        closeSheet()
    }

    fun closeSheet() {
        _sheetOpen.value = false
        _sheetQuery.value = ""
        _sheetResults.value = emptyList()
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
        _sheetAddedCount.value += 1
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
            // TUTAR AKTIF SEPETIN TAHMINI (E18). Gezi HENUZ kapanmadigi icin
            // `observeTripEstimates` onu gormuyor - o sorgu `completedAt`
            // dolu geziler icin. Aktif sepetin kendi tahmini zaten var ve
            // dogru olan da o: kart kapanmadan once cizilecek.
            val estimate = priceObservationDao.observeEstimate(trip.id).first()
            val priced = priceObservationDao.observePricedCount(trip.id).first()
            // TUTAR YOKSA KART HIC ACILMIYOR (karar 45).
            //
            // Once yalnizca TUTAR null'laniyordu ve kart yine aciliyordu:
            // mansetsiz, "Alisveris bitti." ve "N urun alindi" satirlariyla.
            // Karar 45 birebir soyle diyor: *"36sp manset dusuyorsa kart hic
            // gorunmuyor, yerine hicbir sey konmuyor"*; gerekcesi de acik -
            // *"kartin var olus sebebi manset; o dusunce geriye kalan iki
            // satir kart acmayi hak etmiyor"*.
            //
            // ROADMAP bunu F11.23 diye tasiyordu ve ertelenme gerekcesi
            // "E18 ile ayni PR'da olmali, yoksa kart tamamen kaybolur"di.
            // E18 kapandi; erteleme gerekcesi de kapandi.
            val amount = estimate.takeIf { priced >= MIN_PRICED_ITEMS }
            _summary.value = amount?.let {
                ShoppingSummary(
                    takenCount = rows.count { row -> row.checked },
                    totalCount = rows.size,
                    amountMinor = it,
                    durationMinutes = null,
                )
            }
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

    /** Ozet kartinin ait oldugu gezi - "Hepsini almadim" duzeltmesi buna gidiyor. */
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

    private val _pendingDelete = MutableStateFlow<DeletedRow?>(null)

    /**
     * En son silinen satir - geri alma seridinin kaynagi (tasarim karari 37).
     *
     * SILME ANINDA KALICI, snackbar bir BEKLEME DEGIL. Satir hemen tombstone
     * aliyor, kategori sayaci hemen dusuyor, liste hemen kapaniyor. Serit
     * yalnizca bes saniyelik bir GERI DONUS HAKKI sunuyor; suresi dolunca
     * ekstra bir sey olmuyor, yalnizca hak bitiyor.
     *
     * Tersi - "bes saniye bekle sonra sil" - listeyi yalan soyleyen bir halde
     * birakirdi: satir duruyor ama gitmis sayiliyor, sayac hangi rakami
     * gostereceğini bilmiyor, ve kullanici o arada uygulamayi kapatirsa silme
     * hic olmuyor.
     */
    val pendingDelete: StateFlow<DeletedRow?> = _pendingDelete

    private var deleteSeq = 0L

    /**
     * Satiri siler ve geri alma seridini tetikler.
     *
     * ADI ONCEDEN OKUNUYOR: serit *"Maydanoz silindi"* diyor, yani silinen
     * satirin adi lazim - ve silindikten SONRA sorulsaydi sorgu `deletedAt IS
     * NULL` filtresine takilip bos donerdi. Cagiran taraf zaten adi ekranda
     * cizdigi icin parametre olarak geciyor; ikinci bir sorgu yazmak ayni
     * bilgiyi iki kaynaktan almak olurdu.
     */
    fun remove(rowId: String, name: String) {
        viewModelScope.launch {
            repo.remove(rowId)
            _pendingDelete.value = DeletedRow(rowId = rowId, name = name, seq = ++deleteSeq)
        }
    }

    /** "Geri al" - satir miktariyla ve isaretli haliyle geri geliyor. */
    fun undoDelete() {
        val silinen = _pendingDelete.value ?: return
        _pendingDelete.value = null
        viewModelScope.launch { repo.undoRemove(silinen.rowId) }
    }

    /**
     * Yazilmis gozlemi siler ve GERI ALMA hakki verir (karar 46).
     *
     * Silme ANINDA kalici, snackbar bir bekleme degil - satir silmenin kendi
     * kurali burada da gecerli ([pendingDelete] KDoc'u). Gozlem hemen mezar
     * aliyor, fiyat cipi hemen tazeleniyor; serit yalnizca bes saniyelik bir
     * geri donus hakki sunuyor.
     *
     * Ayni `_pendingDelete` akisini KULLANMIYOR: o satir silmeye ait ve
     * `undoDelete` farkli bir tabloya gidiyor. Ikisini tek akista birlestirmek,
     * "Geri al"in hangi seyi geri alacagini calisma zamaninda cozmek olurdu.
     */
    fun deleteObservation(id: String) {
        viewModelScope.launch {
            priceObservationDao.softDelete(id, clock())
            _pendingObservationDelete.value = id
        }
    }

    fun undoObservationDelete() {
        val id = _pendingObservationDelete.value ?: return
        _pendingObservationDelete.value = null
        viewModelScope.launch { priceObservationDao.undoDelete(id) }
    }

    fun dismissObservationUndo() {
        _pendingObservationDelete.value = null
    }

    private val _pendingObservationDelete = MutableStateFlow<String?>(null)

    /** Silinen gozlem - snackbar'in UCUNCU kullanimi (karar 46). */
    val pendingObservationDelete: StateFlow<String?> = _pendingObservationDelete

    /** Serit suresi doldu ya da kullanici baska bir sey sildi. */
    fun dismissDeleteUndo() {
        _pendingDelete.value = null
    }


    private val _lastAdded = MutableStateFlow<AddedRow?>(null)

    /**
     * En son eklenen satir - ekran onu gorunur kilsin diye.
     *
     * Kullanicinin yasadigi sorun: klavye acikken yazip ekliyorsun, satir kendi
     * reyonuna dusuyor ve o reyon ekranin altindaysa **hicbir sey olmamis gibi**
     * gorunuyor. Girdi alani temizleniyor, liste kipirdamiyor, eklendi mi
     * eklenmedi mi belli degil.
     *
     * SIRA DEGISTIRILMIYOR: satiri en uste tasimak akla gelen ilk cozum ama
     * listenin reyon duzenini bozardi - markette gezerken satirlarin reyon
     * sirasinda durmasi bu ekranin butun isi. Cozum satiri tasimak degil, ONA
     * BAKMAK.
     */
    val lastAdded: StateFlow<AddedRow?> = _lastAdded

    private var addSeq = 0L

    /**
     * "Bu satir az once eklendi" sinyali.
     *
     * TEK KAPI olmasinin sebebi bir hata: `addFromEngine` `repo.add`i DOGRUDAN
     * cagiriyordu ve sinyali kimse yazmiyordu - yani oneri seridinden eklenen
     * urun hicbir zaman gorunur kilinmiyordu. Ekleme yollari bes tane ve
     * yenisi eklenebilir; her birinin ayni iki satiri hatirlamasini beklemek
     * bu hatanin tekrarini istemek olurdu.
     *
     * SAYAC DA TASINIYOR, yalnizca id DEGIL: ayni urunu ikinci kez eklemek yeni
     * satir acmiyor, var olanin adedini artiriyor - yani id degismiyor.
     * Yalnizca id'ye bakan bir ekran "ayni deger" gorup kipirdamazdi ve
     * kullanici tam da ikinci eklemede eklendi mi diye bakiyor olurdu.
     *
     * TOPLU EKLEMEDE (pano, "gecen sefer aldiklarini ekle") her satir sinyali
     * ezip gecer ve SONUNCUSU kazanir - dogrusu bu: yirmi satir eklenirken
     * yirmi kez kaydirmanin anlami yok.
     */
    private fun signalAdded(line: TripLine) {
        _lastAdded.value = AddedRow(rowId = line.id, seq = ++addSeq)
    }

    /** Serbest metinden ekle: "2 kg elma" gibi. */
    fun add(text: String) {
        val m = parseQuantity(text)
        if (m.name.isBlank()) return
        addInternal(name = m.name, categoryId = null, unit = m.unit, count = m.count)
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

        // Kategori/kanonik ad cozumlemesi ORTAK: etiket onayi da ayni
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
        signalAdded(
            repo.add(
                householdId = household,
                tripId = trip.id,
                product = product,
                memberId = memberId,
                count = count,
            ),
        )
    }

}

/**
 * En son eklenen satir ve kacinci ekleme oldugu.
 *
 * [seq] tek basina bir sayac degil, TETIKLEYICI: ayni satira ikinci kez ekleme
 * yapildiginda [rowId] degismedigi icin ekranin bunu yeni bir olay olarak
 * gorebilmesinin tek yolu bu.
 */
data class AddedRow(val rowId: String, val seq: Long)

/**
 * Silinen satir ve kacinci silme oldugu.
 *
 * [seq] tetikleyici: art arda iki satir silinirse serit ikincisi icin yeniden
 * dogmali. Yalnizca [rowId]'ye bakan bir ekran, ayni satir iki kez silinip geri
 * alindiginda kipirdamazdi.
 */
data class DeletedRow(val rowId: String, val name: String, val seq: Long)

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

/** Basligin uc parcasi: son gezi, avatar bas harfleri, es var mi. */
private data class HeaderData(
    val lastTrip: LastTrip?,
    val selfInitials: String?,
    val hasPartner: Boolean,
)

/** Ekran 5'in alim gecmisi dokuz satir gosteriyor (tasarim). */
private const val HISTORY_LIMIT = 9
