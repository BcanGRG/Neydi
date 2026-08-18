package com.neydi.app.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.CatalogSeedDao
import com.neydi.app.data.db.PriceObservationDao
import com.neydi.app.data.db.Store
import com.neydi.app.data.db.StoreDao
import com.neydi.app.data.matchKey as matchKeyOf
import com.neydi.app.data.store.chainKey as chainKeyOf
import com.neydi.app.data.db.writeTagObservation
import com.neydi.app.data.image.deleteFileAt
import com.neydi.app.data.image.downscaleForOcr
import com.neydi.app.data.ocr.TagFields
import com.neydi.app.data.ocr.readTag
import com.neydi.app.data.ocr.readTagFields
import com.neydi.app.data.formatMinor
import com.neydi.app.data.parseMinorInput
import com.neydi.app.data.repo.ListRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Etiket cekim ekraninin beyni (E15).
 *
 * ## Kamera bu sinifi HIC gormüyor
 *
 * Ekran fotografi cekiyor ve yalnizca YOLU veriyor. Sebep test edilebilirlik:
 * bu makinede cihaz test kaynak kumesi yok, ama `commonTest` gercek bir
 * bellek-ici Room veritabani kurabiliyor. Kamera disarida kalinca cekimden
 * gozleme kadar butun yol - OCR, alan doldurma, urun cozme, mukerrer koruma -
 * cihazsiz kosuyor.
 *
 * ## Fotograf KAYDEDINCE DE VAZGECINCE DE siliniyor
 *
 * Karar 29 fotografi saklamiyor. Ilk yazdigimda yalnizca kaydetme yolunda
 * siliyordum; vazgecen kullanicinin karesi diskte kaliyordu - yani "fotograf
 * saklanmiyor" sozu yalnizca mutlu yolda dogruydu.
 */
internal class TagCaptureViewModel(
    private val repo: ListRepository,
    private val catalogSeedDao: CatalogSeedDao,
    private val storeDao: StoreDao,
    private val priceObservationDao: PriceObservationDao,
    private val clock: () -> Long,
    private val newId: () -> String,
    /** OCR ADIMI DISARIDAN: testler gercek bir fotograf olmadan fikstur verebilsin. */
    private val readFields: suspend (photoPath: String, chain: String?) -> TagFields = ::readFieldsFromPhoto,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    /**
     * Son secilen urunun adi - kart acilirken hazir gelir (karar 51).
     *
     * State'te DEGIL, cunku cizilen bir sey degil: kartin baslangic degeri.
     * State'e koymak "su an gosterilen" ile "bir dahakine gelecek" arasindaki
     * farki silerdi.
     */
    private var lastProductName: String? = null

    private val _state = MutableStateFlow(TagCaptureState())
    val state: StateFlow<TagCaptureState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // TEK ATISLIK okuma: cekim sirasinda zincir listesi degismiyor, ve
            // canli bir akis burada yalnizca kamera acikken veritabanini
            // dinlemek olurdu.
            _state.value = _state.value.copy(
                stores = storeDao.observeAll(household).first(),
                storeId = priceObservationDao.lastUsedStoreId(household),
            )
            lastProductName = priceObservationDao.lastUsedProductName(household)
        }
    }

    /**
     * Cekim BASARISIZ oldu - ve bunu soylemek zorundayiz.
     *
     * `CaptureController.capture` sozlesmesi *"false = kamera hazir degil ya da
     * yazma basarisiz; cagiran taraf bunu kullaniciya soylemek zorunda"* diyor.
     * Cagiran soylemiyordu: `if (capture(path)) onCaptured(path)` yaziliyordu ve
     * `else` dali bostu. Deklansore basip hicbir sey olmamasinin ikinci sebebi
     * buydu - birincisi geri bildirim yoklugu, bu ise gercekten kare
     * alinamamasi.
     *
     * TEK CUMLE, cunku sebebi BILMIYORUZ. Tasarim depolama dolusu icin
     * *"Yer kalmadi, fotograf alinamadi"*, kamera mesgulu icin *"Kamera su an
     * kullanilamiyor"* diyor; `capture` ciplak bir `Boolean` donduruyor ve
     * ikisini ayirt edemiyoruz. Yanlis sebebi soylemektense sebebsiz
     * soylemek dogru - ayrim icin denetleyicinin gerekce dondurmesi gerekiyor.
     */
    fun captureFailed() {
        _state.value = _state.value.copy(failure = "Fotoğraf alınamadı")
    }

    fun failureShown() {
        _state.value = _state.value.copy(failure = null)
    }

    fun selectStore(storeId: String) {
        _state.value = _state.value.copy(storeId = storeId, picker = null, pendingStoreName = null)
    }

    /**
     * Kare cekildi - kart HEMEN aciliyor, OCR arkada kosuyor.
     *
     * Kartin beklememesi tasarimin sarti: kullanici deklansore basti, bir sey
     * gorunmek zorunda. Alanlar `reading = true` iken iskelet ciziliyor.
     */
    fun onCaptured(photoPath: String) {
        _state.value = _state.value.copy(card = ConfirmCard(photoPath = photoPath), failure = null)
        viewModelScope.launch {
            val chain = currentChain()
            val fields = runCatching { readFields(photoPath, chain) }.getOrNull()
            // KART DEGISTIYSE YAZMA. Kullanici bu arada vazgecip yeni bir kare
            // cekmis olabilir; geciken OCR sonucu yeni karti ezmemeli.
            val card = _state.value.card ?: return@launch
            if (card.photoPath != photoPath) return@launch
            _state.value = _state.value.copy(
                card = card.copy(
                    reading = false,
                    priceText = fields?.price?.let { minorToInput(it.minor) } ?: "",
                    // OCR METNI ASLA URUN ADI OLMAZ (karar 51). Okunan ad
                    // KANIT olarak tasiniyor - urun seciciye "Etiket metni:
                    // DST YGRT 1000G" diye yaziliyor - ve urun kimligi
                    // katalogdan geliyor. Onceki hal OCR'i dogrudan ada
                    // yaziyordu ve Migros'ta ayni sut cihazda IKI urun oldu.
                    tagText = fields?.name?.name,
                    productName = lastProductName.orEmpty(),
                    brand = fields?.name?.brand,
                    kurusFromOcr = fields?.price?.kurusFromOcr == true,
                    skipped = fields?.skipped,
                ),
            )
        }
    }

    // ---------------------------------------------------------------- seciciler

    fun openPicker(picker: TagPicker) {
        _state.value = _state.value.copy(picker = picker, storeQuery = "", pendingStoreName = null)
        when (picker) {
            TagPicker.PRODUCT -> searchProducts(_state.value.card?.productName.orEmpty())
            TagPicker.BRAND -> loadBrandPool()
            TagPicker.STORE -> Unit
        }
    }

    fun closePicker() {
        _state.value = _state.value.copy(picker = null, pendingStoreName = null)
    }

    // --- Urun secici (karar 51)

    fun searchProducts(query: String) {
        _state.value = _state.value.copy(productQuery = query)
        viewModelScope.launch {
            // BOS ARAMADA EN YAYGINLAR: bos bir liste kullaniciya "katalog yok"
            // der; oysa katalog dolu, yalnizca sorgu bos.
            val picks =
                if (query.isBlank()) catalogSeedDao.mostCommon()
                else catalogSeedDao.search(matchKeyOf(query))
            if (_state.value.productQuery == query) {
                _state.value = _state.value.copy(productPicks = picks)
            }
        }
    }

    fun pickProduct(name: String) {
        lastProductName = name
        updateCard { it.copy(productName = name) }
        closePicker()
    }

    // --- Marka sheet'i (karar 52)

    private fun loadBrandPool() {
        val storeId = _state.value.storeId
        viewModelScope.launch {
            val pool = if (storeId == null) emptyList() else priceObservationDao.brandsSeenAt(household, storeId)
            // OCR'IN OKUDUGU MARKA DA HAVUZDA: sheet klavyesiz oldugu icin
            // (karar 52) yeni bir marka sisteme yalnizca OCR kapisindan
            // girebiliyor; onerilen markayi listeden dusurmek o tek kapiyi
            // kapatirdi.
            val suggested = _state.value.card?.brand
            val merged = if (suggested != null && suggested !in pool) listOf(suggested) + pool else pool
            _state.value = _state.value.copy(brandPool = merged)
        }
    }

    fun pickBrand(brand: String?) {
        updateCard { it.copy(brand = brand) }
        closePicker()
    }

    // --- Market secici (karar 40 + 59)

    fun searchStores(query: String) {
        // Yazi degisince bekleyen onay DUSUYOR: "AKYRUT" icin acilan onay,
        // kullanici adi duzeltince "AKYURT"u onaylamis gibi gorunmemeli.
        _state.value = _state.value.copy(storeQuery = query, pendingStoreName = null)
    }

    /**
     * "+ Yeni market" ILK dokunusu - henuz hicbir sey yaratmiyor (karar 59).
     *
     * Tek dokunusla kalici varlik yaratmak, geri alinamayan bir yazim hatasi
     * demekti. Ikinci dokunus onay cipinde.
     */
    fun proposeStore(name: String) {
        _state.value = _state.value.copy(pendingStoreName = name.trim())
    }

    fun confirmNewStore() {
        val name = _state.value.pendingStoreName?.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            // VAR OLAN ADIN BASKA YAZIMI SESSIZCE VAR OLANA BAGLANIR (karar 40):
            // "BIM" yazan biri "BIM"i bulmali, sekizinci bir satir degil.
            val existing = _state.value.stores.firstOrNull { chainKeyOf(it.chain) == chainKeyOf(name) }
            val id = existing?.id ?: newId().also {
                storeDao.insert(
                    Store(id = it, householdId = household, name = name, chain = name, createdAt = clock()),
                )
            }
            _state.value = _state.value.copy(
                stores = storeDao.observeAll(household).first(),
                storeId = id,
                picker = null,
                pendingStoreName = null,
                storeQuery = "",
            )
        }
    }

    /** Gozlemsiz markete uzun dokunus (karar 59); gozlemli market SILINMEZ. */
    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            if (priceObservationDao.hasObservationsAt(household, storeId)) {
                _state.value = _state.value.copy(failure = "Bu markette gözlem var, silinemez")
                return@launch
            }
            storeDao.softDelete(storeId, clock())
            val stores = storeDao.observeAll(household).first()
            _state.value = _state.value.copy(
                stores = stores,
                storeId = _state.value.storeId?.takeIf { id -> stores.any { it.id == id } },
            )
        }
    }

    fun toastShown() {
        _state.value = _state.value.copy(toast = null)
    }

    fun editPrice(text: String) = updateCard { it.copy(priceText = text) }

    fun editProductName(text: String) = updateCard { it.copy(productName = text) }

    /** Karti kapatir ve kareyi siler - saklanmayan fotograf saklanmamali. */
    fun dismissCard() {
        val path = _state.value.card?.photoPath
        _state.value = _state.value.copy(card = null)
        if (path != null) viewModelScope.launch { deleteCapture(path) }
    }

    /**
     * Gozlemi yazar ve KAMERAYA DONER - ekrandan cikmadan.
     *
     * ## SERI CEKIM
     *
     * Ilk surum kaydettikten sonra Liste'ye donuyordu. Kullanici bir BIM
     * turunda 12 etiket cekti ve kameraya 12 KEZ yeniden girdi; sartname
     * *"kamera 300 ms icinde hazir"* diyor, yani tam tersi. Reyonda deger
     * egrisi kac etiket cekilebildigine bagli (ROADMAP risk maddesi) ve her
     * kayittan sonra ekran degistirmek o egriyi dogrudan kiriyor.
     *
     * Kart kapaniyor, bildirim BU ekranda cikiyor, kamera zaten bagli kaliyor -
     * `ImageCapture` yeniden baglanmadigi icin hazir olma suresi pratikte sifir.
     */
    fun save() {
        val card = _state.value.card ?: return
        val minor = parseMinorInput(card.priceText) ?: return
        if (!card.canSave || _state.value.saving) return
        // ADSIZ GOZLEM YAZILMAZ. `canSave` sozlesme geregi yalnizca fiyata
        // bakiyor; ad yine de bos olabilecegi tek yerde - ilk kurulumun ilk
        // cekimi, "son secilen urun" diye bir sey henuz yokken - Kaydet
        // yazmiyor, SORUYOR. Butonu pasif birakmak kullaniciya neyin eksik
        // oldugunu soylemezdi.
        if (card.productName.isBlank()) {
            openPicker(TagPicker.PRODUCT)
            return
        }
        _state.value = _state.value.copy(saving = true)
        viewModelScope.launch {
            val written = writeTagObservation(
                repo = repo,
                catalogSeedDao = catalogSeedDao,
                priceObservationDao = priceObservationDao,
                householdId = household,
                productName = card.productName,
                priceMinor = minor,
                storeId = _state.value.storeId,
                brand = card.brand,
                at = clock(),
                newId = newId,
            )
            deleteCapture(card.photoPath)
            _state.value = _state.value.copy(
                card = null,
                saving = false,
                // MUKERRER ILE YENI AYRI CUMLELER: kullanici deklansore basti,
                // bir sey soylenmeli - ama "kaydedildi" demek yanlis olurdu.
                toast = if (written) savedToast(currentChain(), minor) else "Aynı fiyat az önce kaydedilmişti",
            )
        }
    }

    private fun currentChain(): String? {
        val id = _state.value.storeId ?: return null
        return _state.value.stores.firstOrNull { it.id == id }?.chain
    }

    private fun updateCard(block: (ConfirmCard) -> ConfirmCard) {
        val card = _state.value.card ?: return
        _state.value = _state.value.copy(card = block(card))
    }
}

/**
 * Fotograftan alanlara - gercek boru hatti.
 *
 * `downscaleForOcr` ONCE kosmak ZORUNDA: yonu piksele isliyor. Atlanirsa ML Kit
 * kareyi YAN goruyor ve sayfa dev satirlara cokuyor (F4.20'de fiste olculdu).
 *
 * `readTag`e EXIF gecilmiyor cunku hazirlanmis dosyada EXIF YOK - yon zaten
 * piksele islendi. O parametre yalnizca fikstur dokumu icin var.
 */
private suspend fun readFieldsFromPhoto(photoPath: String, chain: String?): TagFields {
    val staged = "$photoPath.ocr.jpg"
    val bytes = PlatformFile(photoPath).readBytes()
    val ok = downscaleForOcr(bytes, staged)
    val ocr = if (ok) readTag(staged) else readTag(photoPath)
    deleteFileAt(staged)
    return readTagFields(ocr, chain)
}

/** Kurusu duzenlenebilir metne cevirir: 3850 -> "38,50". */
internal fun minorToInput(minor: Long): String {
    val lira = minor / 100
    val kurus = (minor % 100).toInt()
    return "$lira,${kurus.toString().padStart(2, '0')}"
}

/**
 * `Gözlem kaydedildi · BİM · 24,90 TL` - tasarimin birebir bildirimi.
 *
 * ONCE YALNIZCA `Fiyat kaydedildi` yaziyordu ve seri cekimde bu yetersiz:
 * kart kapandiktan sonra ekranda NE kaydedildigini gosteren hicbir sey
 * kalmiyor, kamera yeniden aciliyor. Ard arda on iki etiket cekerken tek
 * dogrulama noktasi bu cumle - market ve tutar orada olmazsa kullanici yanlis
 * markete yazdigini ancak Liste'ye dondugunde gorur.
 *
 * MARKET YOKSA O PARCA DUSUYOR, "-" yazilmiyor: bos bir alan uydurmaktansa
 * kisa cumle dogru.
 *
 * VIEWMODEL'IN DISINDA, cunku cumlenin kendisi test edilebilir olmali; ViewModel
 * gercek bir Room veritabani ve Main dispatcher'i istiyor (bkz.
 * `writeTagObservation`in ayni gerekceyle ayrilmasi).
 */
internal fun savedToast(chain: String?, minor: Long): String {
    val money = formatMinor(minor)
    return if (chain == null) "Gözlem kaydedildi · $money" else "Gözlem kaydedildi · $chain · $money"
}
