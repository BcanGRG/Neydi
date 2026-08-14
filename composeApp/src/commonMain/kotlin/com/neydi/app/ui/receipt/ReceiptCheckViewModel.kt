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
import com.neydi.app.data.receipt.ReceiptProcessor
import com.neydi.app.data.receipt.ReceiptReadOutcome
import com.neydi.app.data.receipt.TOLERANCE_MINOR
import com.neydi.app.data.receipt.chainKey
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.repo.resolveProduct
import kotlinx.coroutines.flow.MutableStateFlow
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
    val totalMinor: Long? = null,
    val sumMinor: Long = 0,
    val gateHolds: Boolean? = null,
    val rows: List<CheckRow> = emptyList(),
    val failedMessage: String? = null,
    /** Tek seferlik bilgi: "bu yonde okunamadi, eski okuma korundu" gibi. */
    val notice: String? = null,
)

class ReceiptCheckViewModel(
    private val receiptId: String,
    private val processor: ReceiptProcessor,
    private val receiptDao: ReceiptDao,
    private val receiptLineDao: ReceiptLineDao,
    private val productDao: ProductDao,
    private val aliasDao: ProductAliasDao,
    private val catalogSeedDao: CatalogSeedDao,
    private val repo: ListRepository,
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
        val sum = lines.sumOf { it.lineTotalMinor }
        val total = receipt?.totalMinor
        _state.value = CheckState(
            loading = false,
            storeName = receipt?.storeNameRaw,
            totalMinor = total,
            sumMinor = sum,
            gateHolds = total?.let { kotlin.math.abs(sum - it) <= TOLERANCE_MINOR },
            rows = lines.map { it.toRow() },
            // SATIR VARSA hata mesaji GOSTERILMEZ.
            //
            // Okunamayan fis mesaji ekrani devraliyor; satirlar duruyorken bunu
            // yapmak dogru ayristirilmis veriyi erisilemez kilardi. Cihazda
            // tam bu olmustu: yon zorlanip okuma bozulunca alti satir ekrandan
            // kayboldu, halbuki veritabaninda duruyorlardi.
            failedMessage = receipt?.errorMessage
                ?.takeIf { receipt.status == ReceiptStatus.FAILED && lines.isEmpty() },
            notice = notice,
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
            dismissEdit()
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
            val degrees = SIRALI_ACILAR[rotationIndex % SIRALI_ACILAR.size]
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
        val SIRALI_ACILAR = listOf(90, 270, 180, 0)
    }
}
