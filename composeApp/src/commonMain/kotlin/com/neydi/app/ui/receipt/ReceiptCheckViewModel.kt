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
import com.neydi.app.data.receipt.chainKey
import com.neydi.app.data.receipt.samePhysicalReceipt
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
    /** Eslesen urunun adi; null ise henuz baglanmadi. */
    val productName: String?,
    val amountMinor: Long,
    val count: Double,
    /** Fiste YAZAN hali. Gri alt satir olarak gosteriliyor. */
    val rawText: String,
    val needsReview: Boolean,
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
     * Baslik alt satiri: *"Migros Ataşehir · 12 Ağustos 15:31 · 2 parça"*
     * (tasarim karari 9). Hicbiri okunamadiysa null ve satir cizilmiyor.
     */
    val subtitle: String? = null,
    val totalMinor: Long? = null,
    val sumMinor: Long = 0,
    val gateHolds: Boolean? = null,
    val rows: List<CheckRow> = emptyList(),
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
)

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
            if (receipt?.status == ReceiptStatus.PENDING) processor.process(receiptId)
            reload()
        }
    }

    private suspend fun reload(notice: String? = null) {
        val receipt = receiptDao.byId(receiptId)
        val lines = receiptLineDao.forReceipt(receiptId)
        // Fark kumesi: gezideki satirlardan, urunu fiste eslesmis olanlar
        // dusuluyor. Eslesmemis fis satirlari kimseyi aklayamaz - urunu belli
        // degil.
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
        val ownSum = lines.sumOf { if (it.isDiscount) -it.lineTotalMinor else it.lineTotalMinor }
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
        val group = receipt?.let { samePhysicalReceipt(receiptDao.forTrip(it.tripId), it.id) }
            .orEmpty()
        val isPart = group.size > 1
        val sum = if (isPart) {
            // null = grupta hic satir yok; kendi toplamimiz (0) dogru cevap.
            receiptLineDao.sumLinesForReceipts(group.map { it.id }) ?: ownSum
        } else {
            ownSum
        }
        val total = if (isPart) {
            // Hicbir parcanin toplami okunamadiysa null kaliyor ve kapi
            // "dogrulanamadi" diyor - parca cipi bunu notr gosteriyor.
            group.mapNotNull { it.totalMinor }.takeIf { it.isNotEmpty() }?.sum()
        } else {
            receipt?.totalMinor
        }
        _state.value = CheckState(
            loading = false,
            storeName = receipt?.storeNameRaw,
            subtitle = receiptSubtitle(
                store = receipt?.storeNameRaw,
                receiptDate = receipt?.receiptDate ?: receipt?.capturedAt,
                partCount = group.size,
            ),
            totalMinor = total,
            sumMinor = sum,
            gateHolds = total?.let { kotlin.math.abs(sum - it) <= TOLERANCE_MINOR },
            rows = lines.map { it.toRow() },
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
        )
    }

    private suspend fun ReceiptLine.toRow(): CheckRow = CheckRow(
        id = id,
        productName = matchedProductId?.let { productDao.byId(it)?.name },
        amountMinor = lineTotalMinor,
        count = quantity,
        rawText = rawText,
        needsReview = needsReview,
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
            val receipt = receiptDao.byId(receiptId) ?: return@launch
            val line = receiptLineDao.forReceipt(receiptId).firstOrNull { it.id == row.id }
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
                    // bunu bilmeli, yoksa buton bozuk sanilir.
                    ReceiptReadOutcome.KEPT_PREVIOUS ->
                        "$degrees° yönünde okunamadı, önceki okuma korundu."
                    ReceiptReadOutcome.PARSED -> "$degrees° yönünde okundu."
                    else -> null
                },
            )
        }
    }

    private var rotationIndex = 0

    private companion object {
        /** Denenecek aci sirasi. 0 EN SONDA: otomatik secim onu zaten denedi. */
        val ROTATION_ORDER = listOf(90, 270, 180, 0)
    }
}

/**
 * Fis Kontrol'un baslik alt satiri (tasarim karari 9).
 *
 * BOLUMLER TEK TEK OPSIYONEL: magaza okunamadiysa yalnizca tarih, tarih de
 * okunamadiysa yalnizca parca sayisi yaziliyor. Okunamayan bir seyin yerine
 * "-" ya da "Bilinmiyor" koymak, satiri bilgi tasimayan bir sey yapardi.
 *
 * PARCA SAYISI YALNIZCA BIRDEN COKSA: "1 parca" diye bir sey yok, tek
 * parcali fis zaten fisin kendisi.
 */
internal fun receiptSubtitle(store: String?, receiptDate: Long?, partCount: Int): String? {
    val parts = buildList {
        store?.takeIf { it.isNotBlank() }?.let { add(it) }
        receiptDate?.let { add(formatDayMonthTime(it)) }
        if (partCount > 1) add("$partCount parça")
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
