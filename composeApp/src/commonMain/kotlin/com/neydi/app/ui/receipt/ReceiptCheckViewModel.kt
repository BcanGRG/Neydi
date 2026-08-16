package com.neydi.app.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.CatalogSeedDao
import com.neydi.app.data.db.ProductAlias
import com.neydi.app.data.db.ProductAliasDao
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptDao
import com.neydi.app.data.db.ReceiptLine
import com.neydi.app.data.db.ReceiptLineDao
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.db.TakeOutcome
import com.neydi.app.data.receipt.ReceiptProcessor
import com.neydi.app.data.receipt.attachReceiptToTrip
import com.neydi.app.data.receipt.ReceiptReadOutcome
import com.neydi.app.data.receipt.TOLERANCE_MINOR
import com.neydi.app.data.formatDayMonthTime
import com.neydi.app.data.store.chainKey
import com.neydi.app.data.store.storeDisplayName
import com.neydi.app.data.receipt.samePhysicalReceipt
import com.neydi.app.data.receipt.stitchParts
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.repo.resolveProduct
import com.neydi.app.data.stats.ProductStatsRebuilder
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Bir fis satirinin kullaniciya gorunen hali. */
data class CheckRow(
    val id: String,
    /**
     * Satirin geldigi fis (tasarim karari 4).
     *
     * TEK AKISTA SART: ekran artik butun parcalarin satirlarini birlikte
     * ciziyor, yani duzeltme yazilirken "hangi fis" sorusunun cevabi ekranin
     * kimliginden gelemez. Alias magaza zinciri bazinda yaziliyor ve zincir
     * satirin kendi fisinden okunuyor.
     */
    val receiptId: String,
    /** Eslesen urunun adi; null ise henuz baglanmadi. */
    val productName: String?,
    val amountMinor: Long,
    val count: Double,
    /** Fiste YAZAN hali. Gri alt satir olarak gosteriliyor. */
    val rawText: String,
    val needsReview: Boolean,
    /**
     * Fisin verdigi tek kimlik: barkod (tasarim karari 14).
     *
     * Ad okunamadiginda baslik slotunda BU duruyor - hata mesaji degil.
     * Yoksa null.
     */
    val barcode: String? = null,
)

/**
 * Bir parcanin bolumu (tasarim karari 4).
 *
 * PARCA HATA HALI DEGIL, NORMAL HAL. 60 kalemlik bir fis tek kareye
 * sigdirilinca satir basina 4,7 piksel dusuyor - cok kare fiziksel bir
 * zorunluluk, kolaylik degil. O yuzden parcalar ayri ekranlara degil, ayni
 * akisin bolum basliklarina donusuyor.
 */
data class CheckSection(
    val receiptId: String,
    /** *"Parça 1"*. Tek parcali fiste null ve baslik HIC cizilmiyor. */
    val title: String?,
    /** *"18 satır · 1 satır kontrol bekliyor"*. */
    val meta: String?,
    val rows: List<CheckRow>,
)

/** "Listede vardi, fiste yok" satiri (F4.12). */
data class UnaccountedRow(
    val rowId: String,
    val name: String,
    val outcome: TakeOutcome,
)

/**
 * Fis Kontrol ekraninin durumu.
 *
 * [gateHolds] uc durumlu: true tutuyor, false tutmuyor, null toplam okunamadi.
 * Ucunu ikiye katlamak "bizim okuma hatamizi kullanicinin hatasi gibi gostermek"
 * demek olurdu.
 */
data class CheckState(
    val loading: Boolean = true,
    val storeName: String? = null,
    /**
     * Baslik alt satirinin ESNEYEN yarisi: magaza adi (tasarim karari 13).
     * Sigmazsa uc nokta aliyor.
     */
    val subtitleStore: String? = null,
    /**
     * Baslik alt satirinin SABIT yarisi: *"· 12 Ağustos 15:31 · 2 parça"*.
     *
     * ASLA KIRPILMIYOR. Uzun ticari unvan cihazda tam olarak bunu ekran
     * disina itiyordu - halbuki satirin asil ayirt edici bilgisi tarih:
     * ayni marketten iki fis varsa onlari tarih ayiriyor.
     */
    val subtitleMeta: String? = null,
    val totalMinor: Long? = null,
    val sumMinor: Long = 0,
    val gateHolds: Boolean? = null,
    /**
     * Fisin TAMAMI, parca parca (tasarim karari 4).
     *
     * Tek parcali fiste tek bolum ve basligi null. Ekran bu listeyi sirayla
     * ciziyor; parca ayri bir hedef DEGIL - parcayi ayri ekran yapmak toplami
     * iki yere bolerdi.
     */
    val sections: List<CheckSection> = emptyList(),
    /**
     * Fiste GORUNMEYEN liste satirlari (F4.12) - tasarimdaki *"Listede vardi,
     * fiste yok (N)"* bolumu.
     *
     * Fis neyin alindigini zaten kanitliyor; uc-sonuc sorusu yalnizca fisin
     * DOGRULAMADIGI satirlar icin anlamli. O yuzden bolum fis satirlarindan
     * ayri ve yalnizca fark kumesini tasiyor.
     */
    val unaccounted: List<UnaccountedRow> = emptyList(),
    val failedMessage: String? = null,
    /** Tek seferlik bilgi: "bu yonde okunamadi, eski okuma korundu" gibi. */
    val notice: String? = null,
    /**
     * Bu fis cok parcali bir cekimin PARCASI mi (F4.13).
     *
     * Turetiliyor, saklanmiyor: gezide birden fazla fis varsa bu fis tek
     * basina bir alisverisin tamami degil, bir bolumu.
     *
     * ONCE `toplam == null` KOSULU DA VARDI VE SON PARCAYI DISARIDA
     * BIRAKIYORDU. Toplam yalnizca son parcada basili oldugu icin o parca
     * "parca degil" sayiliyor, ama fisin TAMAMININ toplamiyla kendi
     * satirlarinin toplami karsilastiriliyordu - yani her uzun fiste, her
     * seferinde amber "tutmuyor". Kullanici hicbir hata yapmadan. Ara parcalar
     * bu tuzaktan yalnizca toplamlari okunamadigi icin kurtuluyordu.
     *
     * Simdi kapi da gezi kapsaminda hesaplaniyor ([sumMinor] / [totalMinor]),
     * yani parca ekraninda gorunen sayilar FISIN TAMAMINA ait.
     */
    val isPart: Boolean = false,
    /** Fisin bagli oldugu gezi - "devamini cek" cekim oturumunu buna acıyor. */
    val tripId: String? = null,
    /**
     * Fisin kendi sira numaralarinda ATLANMIS kalemler (F4.15).
     *
     * Bos = eksik yok ya da fis sira numarasi basmiyor. Ekran bunu ancak
     * dolu oldugunda ciziyor - "0 kalem eksik" diye bir sey yok.
     */
    val missingSequences: List<Int> = emptyList(),
) {
    /** Butun parcalarin satirlari tek dizide - duzeltme akislari bunu kullaniyor. */
    val rows: List<CheckRow> get() = sections.flatMap { it.rows }
}

class ReceiptCheckViewModel(
    private val receiptId: String,
    private val processor: ReceiptProcessor,
    private val receiptDao: ReceiptDao,
    private val receiptLineDao: ReceiptLineDao,
    private val productDao: ProductDao,
    private val tripLineDao: TripLineDao,
    private val aliasDao: ProductAliasDao,
    private val catalogSeedDao: CatalogSeedDao,
    private val repo: ListRepository,
    private val statsRebuilder: ProductStatsRebuilder,
    // Saat ve id URETIMI disaridan: repository'de de boyle. Testte
    // deterministik olmasi icin sart.
    private val clock: () -> Long,
    private val newId: () -> String,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckState())
    val state: StateFlow<CheckState> = _state

    /** Duzeltme sheet'i icin: dokunulan satir. */
    private val _editing = MutableStateFlow<CheckRow?>(null)
    val editing: StateFlow<CheckRow?> = _editing

    /** Duzeltme sheet'indeki urun onerileri. */
    private val _suggestions = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val suggestions: StateFlow<List<CatalogSeed>> = _suggestions

    init {
        viewModelScope.launch {
            val receipt = receiptDao.byId(receiptId)
            // BEKLEYEN fis burada isleniyor: OCR fotograf cekilirken degil,
            // ekran acilinca kosuyor (F4.2'nin "fotograf asla bloklamaz" kurali).
            //
            // OKUNAMAMIS FIS DE YENIDEN DENENIYOR (F4.14b) ve bu bir tercih
            // degil zorunluluk: ayristirici ya da yon puanlayicisi duzeldiginde
            // eski basarisiz fisler kendiliginden duzelmeli. Aksi halde
            // kullanicinin tek yolu "baska yonde oku" dugmesiydi - yani
            // uygulamanin kendi hatasini elle telafi etmesi isteniyordu.
            //
            // Satiri OLAN fise dokunulmuyor: yeniden okuma ancak elde hicbir
            // sey yokken bedava.
            //
            // KOSUL DURUM DEGIL, ELDE SATIR OLUP OLMADIGI. Once yalnizca
            // FAILED fisler yeniden deneniyordu ama satirsiz bir fis
            // MISMATCHED de olabiliyor (toplam okundu, satir okunmadi) - ve o
            // hal de kaybedecek hicbir sey tasimiyor. Etiket degil, icerik
            // onemli.
            val nothingToLose = receiptLineDao.forReceipt(receiptId).isEmpty()
            if (receipt?.status == ReceiptStatus.PENDING || nothingToLose) {
                processor.process(receiptId)
            }
            reload()
        }
    }

    private suspend fun reload(notice: String? = null) {
        val receipt = receiptDao.byId(receiptId)
        // KAPSAM FIZIKSEL FIS, FOTOGRAF DEGIL (tasarim karari 4). Gruplama
        // `samePhysicalReceipt` icinde ve cekim sirasina gore sirali.
        val group = receipt?.let { samePhysicalReceipt(receiptDao.forTrip(it.tripId), it.id) }
            .orEmpty()
        val isPart = group.size > 1
        // PARCA DIKISI (F4.15): bindiren satirlar bir kez sayiliyor.
        val stitched = stitchParts(group.associateWith { receiptLineDao.forReceipt(it.id) })
        val linesByReceipt = stitched.kept
        val lines = linesByReceipt.values.flatten().ifEmpty { receiptLineDao.forReceipt(receiptId) }
        // Fark kumesi: gezideki satirlardan, urunu fiste eslesmis olanlar
        // dusuluyor. Eslesmemis fis satirlari kimseyi aklayamaz - urunu belli
        // degil.
        //
        // BUTUN PARCALAR SAYILIYOR: tek akista "listede vardi, fiste yok"
        // sorusunun cevabi fisin tamamindan gelmeli. Yalnizca acik parcanin
        // satirlarina bakmak, ilk parcada okunmus urunleri ikinci parcaya
        // gecince "eksik" gosterirdi.
        val matched = lines.mapNotNull { it.matchedProductId }.toSet()
        val unaccounted = receipt?.tripId?.let { tripId ->
            tripLineDao.observeList(tripId).first()
                .filter { it.productId !in matched }
                .map { row ->
                    UnaccountedRow(
                        rowId = row.rowId,
                        name = row.name,
                        outcome = row.takeOutcome
                            ?: if (row.checked) TakeOutcome.TAKEN else TakeOutcome.FORGOTTEN,
                    )
                }
        }.orEmpty()
        // KAPI TEK: ayristiricinin kurali (F4.5/F5.6) ekranda da gecerli -
        // indirimler CIKARILIYOR. Eski hali hepsini pozitif topluyordu ve
        // indirimli fiste islemcinin yazdigi durum (VERIFIED) ile ekrandaki
        // cip (tutmuyor) CELISIYORDU. `isDiscount` v3'te kalici oldugu icin
        // ayni hesap artik veritabanindan yeniden kurulabiliyor.
        val ownLines = linesByReceipt[group.firstOrNull { it.id == receiptId }]
            ?: receiptLineDao.forReceipt(receiptId)
        val ownSum = ownLines.sumOf {
            if (it.isDiscount) -it.lineTotalMinor else it.lineTotalMinor
        }
        // KAPI FIZIKSEL FIS KAPSAMINDA HESAPLANIYOR (F4.13 duzeltmesi).
        //
        // Aritmetik degismez fiziksel fise ait, FOTOGRAFA degil. Uzun fis parca
        // parca cekilince TOPLAM yalnizca SON parcada basili oluyor ama o parca
        // fisin yalnizca bir bolumunun satirlarini tasiyor - yani tek fotograf
        // kapsaminda hesaplanan kapi son parcada YAPISAL OLARAK tutmuyor
        // cikiyordu. Her uzun fiste, her seferinde, kullanici hicbir hata
        // yapmadan amber "tutmuyor". Ara parcalar bundan yalnizca toplamlari
        // null oldugu icin kurtuluyordu.
        //
        // KAPSAM GEZI DEGIL: bir gezide iki AYRI magaza fisi olabiliyor ve
        // cihazda tam olarak o vardi (BIM + File Market). Gezi kapsami, dogru
        // okunmus File Market fisini BIM'in ayristirma hatasiyla amber'a
        // ceviriyordu - duzeltilmek istenen hatanin yer degistirmis hali.
        // Gruplama `samePhysicalReceipt` icinde, gerekcesiyle birlikte.
        //
        // TOPLAM SQL'DEN DEGIL DIKILMIS SATIRLARDAN (F4.15). `sumLinesForReceipts`
        // veritabanindaki HER satiri topluyordu; parcalar birbirine bindiginde
        // ayni kalem iki kez sayiliyor ve fisin toplamini asiyordu. Cihazda
        // olculdu: dort parcalik bir cekimde altmis kalemin kirk ikisi iki
        // parcada birden okunmustu, yani kapi yapisal olarak "tutmuyor"
        // diyordu. Kotlin tarafinda toplamak dikisin sonucunu kullanabilmenin
        // tek yolu - SQL bindirmeyi bilmiyor.
        val sum = lines.sumOf { if (it.isDiscount) -it.lineTotalMinor else it.lineTotalMinor }
        val total = if (isPart) {
            // Hicbir parcanin toplami okunamadiysa null kaliyor ve kapi
            // "dogrulanamadi" diyor - parca cipi bunu notr gosteriyor.
            //
            // TEKILLESTIRILIYOR (F4.15): fisin basili toplami TEK bir sayi ve
            // yalnizca fisin sonunda duruyor. Bindirme artik ISTENDIGI icin
            // ayni kuyruk iki karede birden okunabiliyor - o zaman ayni toplam
            // iki kez toplanip kapiyi ters yonde bozardi. Satirlari
            // tekillestirip toplami tekillestirmemek, duzeltilen hatanin yer
            // degistirmis hali olurdu.
            group.mapNotNull { it.totalMinor }.distinct()
                .takeIf { it.isNotEmpty() }?.sum()
        } else {
            receipt?.totalMinor
        }
        _state.value = CheckState(
            loading = false,
            storeName = receipt?.storeNameRaw,
            subtitleStore = storeDisplayName(receipt?.storeNameRaw),
            subtitleMeta = receiptMeta(
                receiptDate = receipt?.receiptDate ?: receipt?.capturedAt,
                partCount = group.size,
            ),
            totalMinor = total,
            sumMinor = sum,
            gateHolds = total?.let { kotlin.math.abs(sum - it) <= TOLERANCE_MINOR },
            sections = buildSections(group, linesByReceipt, receipt, lines, stitched.overlapCount),
            unaccounted = unaccounted,
            // SATIR VARSA hata mesaji GOSTERILMEZ.
            //
            // Okunamayan fis mesaji ekrani devraliyor; satirlar duruyorken bunu
            // yapmak dogru ayristirilmis veriyi erisilemez kilardi. Cihazda
            // tam bu olmustu: yon zorlanip okuma bozulunca alti satir ekrandan
            // kayboldu, halbuki veritabaninda duruyorlardi.
            failedMessage = receipt?.errorMessage
                ?.takeIf { receipt.status == ReceiptStatus.FAILED && lines.isEmpty() },
            notice = notice,
            isPart = isPart,
            tripId = receipt?.tripId,
            missingSequences = stitched.missingSequences,
        )
    }

    /**
     * Satirlari bolumlere ayirir (tasarim karari 4).
     *
     * TEK PARCADA BASLIK YOK: "Parça 1" diye bir sey yok, tek parcali fis zaten
     * fisin kendisi - `subtitleMeta`daki parca sayisi da ayni kurali izliyor.
     */
    private suspend fun buildSections(
        group: List<Receipt>,
        linesByReceipt: Map<Receipt, List<ReceiptLine>>,
        current: Receipt?,
        fallbackLines: List<ReceiptLine>,
        overlap: Map<String, Int>,
    ): List<CheckSection> {
        if (group.size <= 1) {
            return listOf(
                CheckSection(
                    receiptId = current?.id ?: receiptId,
                    title = null,
                    meta = null,
                    rows = fallbackLines.map { it.toRow(current?.id ?: receiptId) },
                ),
            )
        }
        return group.mapIndexed { index, part ->
            val partLines = linesByReceipt[part].orEmpty()
            CheckSection(
                receiptId = part.id,
                title = "Parça ${index + 1}",
                meta = sectionMeta(partLines, overlap[part.id] ?: 0),
                rows = partLines.map { it.toRow(part.id) },
            )
        }
    }

    private suspend fun ReceiptLine.toRow(owner: String): CheckRow = CheckRow(
        id = id,
        receiptId = owner,
        productName = matchedProductId?.let { productDao.byId(it)?.name },
        amountMinor = lineTotalMinor,
        count = quantity,
        rawText = rawText,
        needsReview = needsReview,
        barcode = barcodeOf(rawText),
    )

    fun edit(row: CheckRow) {
        _editing.value = row
        viewModelScope.launch {
            // Oneriler ham metinden aranıyor: fis "TURŞU KORNI ŞON" yaziyor,
            // kullanicinin gormek istedigi "Turşu".
            _suggestions.value = catalogSeedDao.search(row.rawText.take(12), limit = 8)
        }
    }

    /**
     * Sonraki parcanin fisi hazir - Route bunu yakalayip ekrani YENI fise
     * degistiriyor. Tek seferlik olay.
     */
    private val _nextPartId = MutableStateFlow<String?>(null)
    val nextPartId: StateFlow<String?> = _nextPartId

    fun consumeNextPart() {
        _nextPartId.value = null
    }

    /**
     * Uzun fisin SONRAKI PARCASINI ayni geziye ekler (F4.13).
     *
     * "Parca parca cek" tavsiyesi tek dokunusluk bir yol olmadan yarim
     * kaliyordu: kullanici Liste'ye donup ozet kartini yeniden bulmak
     * zorundaydi. Ayni ekleme yardimcisi kullaniliyor ([attachReceiptToTrip]) -
     * `content://` ve kucultme dersleri orada tek yerde duruyor.
     */
    fun attachNextPart(source: PlatformFile, destPath: String, rawPath: String) {
        viewModelScope.launch {
            val receipt = receiptDao.byId(receiptId) ?: return@launch
            val next = attachReceiptToTrip(
                repo = repo,
                householdId = receipt.householdId,
                tripId = receipt.tripId,
                source = source,
                destPath = destPath,
                rawPath = rawPath,
            )
            _nextPartId.value = next.id
        }
    }

    fun dismissEdit() {
        _editing.value = null
        _suggestions.value = emptyList()
    }

    /**
     * Satiri bir urune baglar VE ALIAS YAZAR (F4.7).
     *
     * Alias yazmak bu ekranin en kalici katkisi: ayni fis metni bir daha
     * sorulmuyor. Ay 2'yi ay 1'den olculebilir bicimde iyi yapan tek mekanizma
     * bu - herhangi bir benzerlik esigi ayarindan daha degerli, cunku esik
     * tahmin eder, alias BILIR.
     */
    fun confirm(row: CheckRow, productName: String) {
        viewModelScope.launch {
            // SATIRIN KENDI FISI, ekranin fisi DEGIL (tasarim karari 4). Tek
            // akista ekranda baska parcalarin satirlari da duruyor; alias
            // zinciri o satirin geldigi fisten okunmali, yoksa ikinci parcada
            // yapilan duzeltme birinci parcanin zincirine yazilirdi.
            val receipt = receiptDao.byId(row.receiptId) ?: return@launch
            val line = receiptLineDao.forReceipt(row.receiptId).firstOrNull { it.id == row.id }
                ?: return@launch
            val product = resolveProduct(
                repo = repo,
                catalogSeedDao = catalogSeedDao,
                householdId = receipt.householdId,
                name = productName,
            )
            receiptLineDao.confirmMatch(row.id, product.id)
            aliasDao.insert(
                ProductAlias(
                    id = newId(),
                    householdId = receipt.householdId,
                    storeChain = chainKey(receipt.storeNameRaw),
                    rawTextNormalized = line.rawTextNormalized,
                    productId = product.id,
                    confirmedAt = clock(),
                    createdAt = clock(),
                ),
            )
            // Kullanici bir fis satirini urune BAGLADI: bu yeni bir alim kaydi,
            // yani istatistik yeniden kurulmali (F6.1). Kapanis coktan gecti.
            statsRebuilder.rebuild(receipt.householdId)
            dismissEdit()
            reload()
        }
    }

    /**
     * Fiste gorunmeyen liste satirinin akibeti (F4.12).
     *
     * Yazmanin ardindan istatistik yeniden kuruluyor: "gerekmedi"/"unuttum"
     * satiri isaretsiz birakiyor ve az once alim sayilmis bir satir artik alim
     * degil.
     */
    fun setOutcome(rowId: String, outcome: TakeOutcome) {
        viewModelScope.launch {
            val receipt = receiptDao.byId(receiptId) ?: return@launch
            repo.setOutcome(rowId, outcome)
            statsRebuilder.rebuild(receipt.householdId)
            reload()
        }
    }

    /** Tutari duzeltir. Aritmetik kapisi aninda yeniden hesaplaniyor. */
    fun fixAmount(row: CheckRow, amountMinor: Long) {
        viewModelScope.launch {
            receiptLineDao.setAmount(row.id, amountMinor)
            dismissEdit()
            reload()
        }
    }

    /**
     * Bir sonraki yonu deneyip yeniden okur.
     *
     * TEK BUTON, SABIT ACI DEGIL: ilk halinde "Duz oku" (0 derece) ve "Cevir"
     * (90 derece) diye iki buton vardi ve cihazda "Duz oku" calisan bir okumayi
     * bozdu - fis dondurulmus cekilmisti. Kullaniciya hangi acinin dogru
     * oldugunu sormak zaten anlamsiz; onun bildigi tek sey "yanlis gorunuyor,
     * baskasini dene".
     *
     * Otomatik secim iki gercek fiste dogru bildi; bu buton ucuncude yanlis
     * bilirse kullanici tikanmasin diye var.
     */
    fun rereadNextRotation() {
        viewModelScope.launch {
            val degrees = ROTATION_ORDER[rotationIndex % ROTATION_ORDER.size]
            rotationIndex++
            _state.value = _state.value.copy(loading = true, notice = null)
            val outcome = processor.process(receiptId, forceRotation = degrees)
            reload(
                notice = when (outcome) {
                    // ONCEKI OKUMA KORUNDU: kullanici bir sey kaybetmedi ve
                    // bunu bilmeli, yoksa buton bozuk sanilir. Iki ayri sebep
                    // ayni cumleye ciiyor - "okunamadi" ve "daha kotu okundu" -
                    // cunku kullanici acisindan sonuc ayni: ekran degismedi ve
                    // elindeki kayip degil.
                    ReceiptReadOutcome.KEPT_PREVIOUS ->
                        "${label(degrees)} daha iyisini vermedi, önceki okuma duruyor."
                    // SATIR SAYISI YAZILIYOR. "Okundu" tek basina hicbir sey
                    // soylemiyordu: kullanici ekranin degisip degismedigini
                    // ancak satirlari sayarak anlayabiliyordu.
                    ReceiptReadOutcome.PARSED ->
                        "${label(degrees)} okundu · ${_state.value.rows.size} satır"
                    else -> null
                },
            )
        }
    }

    private var rotationIndex = 0

    private fun label(degrees: Int?): String = degrees?.let { "$it° yönünde" } ?: "Yeniden"

    private companion object {
        /**
         * Denenecek sira. ILK DENEME OTOMATIK (null), elle acilar sonra.
         *
         * Once dogrudan 90 dereceye atliyordu ve bu, otomatik secim
         * duzeldikce yanlis hale geldi: yon puanlayicisi (F4.14b) ve gorsel
         * satir gruplamasi (F4.19) duzeltildikten sonra otomatik yol cogu
         * fiste zaten dogruyu buluyor. Kullanicinin "yanlis gorunuyor"
         * dediginde istedigi ilk sey bir aci degil, DUZGUN bir yeniden okuma.
         *
         * 0 en sonda: otomatik secim onu zaten deniyor.
         */
        val ROTATION_ORDER = listOf(null, 90, 270, 180, 0)
    }
}

/**
 * Baslik alt satirinin SABIT yarisi (tasarim karari 13).
 *
 * PARCA SAYISI YALNIZCA BIRDEN COKSA: "1 parca" diye bir sey yok, tek
 * parcali fis zaten fisin kendisi.
 */
/**
 * Parca bolum basliginin sag yarisi (tasarim karari 4).
 *
 * SATIR SAYISI HER ZAMAN, KONTROL SAYISI VARSA. Tasarimin ornegi:
 * *"16 satır · 1 satır kontrol bekliyor"* - ikinci parca bunu tasiyor, birinci
 * parca yalnizca *"18 satır"*. Bekleyen satir yokken o cumleyi yazmak, olmayan
 * bir isi varmis gibi gostermek olurdu.
 */
internal fun sectionMeta(lines: List<ReceiptLine>, overlap: Int = 0): String {
    val review = lines.count { it.needsReview }
    return buildString {
        // HAM SAYI YAZILIYOR, elenmis degil (F4.15). Tamamen bindiren bir
        // parca aksi halde "Parça 3 · 0 satır" olurdu ve bu "cekim basarisiz"
        // diye okunup kullaniciyi tam da kacinmak istedigimiz seye - yeniden
        // cekmeye - iterdi. O kare bosa gitmedi; ustuste bindi.
        append("${lines.size + overlap} satır")
        // Bindirme GIZLENMIYOR ama bir uyari da degil: artik ISTENEN sey.
        if (overlap > 0) append(" · $overlap'i önceki parçayla ortak")
        if (review > 0) append(" · $review satır kontrol bekliyor")
    }
}

internal fun receiptMeta(receiptDate: Long?, partCount: Int): String? {
    val parts = buildList {
        receiptDate?.let { add(formatDayMonthTime(it)) }
        if (partCount > 1) add("$partCount parça")
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

/**
 * Ham fis satirindan barkodu cikarir (tasarim karari 14).
 *
 * AKYURT duzeninde tutar satiri `3 8683206511079 1 Adet 189,90 %20 189,90`
 * gibi basiliyor: sira no, barkod, adet, birim fiyat, KDV, tutar. Barkod
 * bunlarin arasindaki EN UZUN rakam dizisi - EAN-13 on uc hane, sira no ve
 * adet bir iki hane, tutarlar ise ondalik ayirici tasiyor.
 *
 * SEKIZ HANE ESIGI: daha kisa bir rakam dizisi barkod degil (EAN-8 en kisa
 * standart). Esik olmasaydi "189" gibi bir tutar parcasi barkod sanilirdi.
 *
 * @return barkod, ya da satirda oyle bir sey yoksa null.
 */
internal fun barcodeOf(rawText: String): String? =
    rawText.split(" ", "\t")
        .map { it.trim() }
        .filter { it.length >= 8 && it.all(Char::isDigit) }
        .maxByOrNull { it.length }
