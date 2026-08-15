package com.neydi.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptDao
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.db.Trip
import com.neydi.app.data.db.TripDao
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.receipt.physicalReceipts
import com.neydi.app.data.receipt.storeDisplayName
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Gecmisteki bir fisin listede gorunen hali. */
data class HistoryReceipt(
    val id: String,
    val status: ReceiptStatus,
    val totalMinor: Long?,
    val storeName: String?,
    val capturedAt: Long,
    /** Cok parcali cekimin parcasi mi - bkz. ReceiptCheckViewModel.isPart. */
    val isPart: Boolean = false,
)

/** Kapanmis bir gezi ve fisleri. */
data class HistoryTrip(
    val id: String,
    val closedAt: Long,
    val totalMinor: Long?,
    val receipts: List<HistoryReceipt>,
    /**
     * Gezinin magaza adi - tasarimin satirindaki BASLIK.
     *
     * Fisten geliyor; hic fis yoksa ya da okunamadiysa null ve satir
     * "Magaza okunamadi" yerine tarihi baslik yapiyor.
     */
    val storeName: String? = null,
    /** *"14 Agu · 18 urun"* satirindaki adet. */
    val itemCount: Int = 0,
)

/**
 * Gecmis ekraninin ViewModel'i (F4.9).
 *
 * SADECE OKUYOR. Gecmis, yanlis okunmus bir fise geri donmenin tek yolu -
 * kendisi hicbir sey degistirmiyor, duzeltme Fis Kontrol ekraninda yapiliyor.
 */
class HistoryViewModel(
    tripDao: TripDao,
    receiptDao: ReceiptDao,
    tripLineDao: TripLineDao,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    /**
     * Geziler ve fisler AYRI sorgulardan birlestiriliyor.
     *
     * Gezi basina ayri fis sorgusu acmak N+1 Flow demekti; hane olcegi kucuk
     * (iki kisi, elli gezi) oldugu icin iki sorguyu bellekte eslemek hem daha
     * hizli hem tek atomik yayin veriyor - liste yarim dolu gorunmuyor.
     */
    val trips: StateFlow<List<HistoryTrip>> =
        combine(
            tripDao.observeHistory(household),
            receiptDao.observeForHousehold(household),
            tripLineDao.observeLineCounts(household),
        ) { geziler, fisler, sayilar ->
            combineTrips(geziler, fisler, sayilar.associate { it.tripId to it.lineCount })
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

internal fun combineTrips(
    trips: List<Trip>,
    receipts: List<Receipt>,
    lineCounts: Map<String, Int> = emptyMap(),
): List<HistoryTrip> {
    val fisHaritasi = receipts.groupBy { it.tripId }
    return trips.map { trip ->
        HistoryTrip(
            id = trip.id,
            // completedAt kapaliligin otoritesi; observeHistory zaten yalnizca
            // kapanmis geziler donduruyor, yine de null'a startedAt ile
            // dusuyoruz ki liste bir sira anahtari kaybetmesin.
            closedAt = trip.completedAt ?: trip.startedAt,
            totalMinor = trip.totalMinor,
            // Ilk okunabilmis magaza adi: cok parcali fiste kunye yalnizca
            // ilk parcada basili (bkz. samePhysicalReceipt).
            // ZINCIR ADI, ticari unvan DEGIL (tasarim karari 13) - ayni
            // kural Gecmis satirinda da gecerli.
            storeName = fisHaritasi[trip.id].orEmpty()
                .sortedBy { it.capturedAt }
                .firstNotNullOfOrNull { storeDisplayName(it.storeNameRaw) },
            itemCount = lineCounts[trip.id] ?: 0,
            receipts = fisHaritasi[trip.id].orEmpty().let { tripReceipts ->
                // Fiziksel fis gruplari: parca olup olmadigini gezideki fis
                // SAYISI degil, ayni fisin parcasi olup olmadigi belirliyor
                // (bkz. samePhysicalReceipt - iki ayri magaza fisi tek gezide
                // olabiliyor ve cihazda oyleydi).
                val groupSize = physicalReceipts(tripReceipts)
                    .flatMap { grup -> grup.map { it.id to grup.size } }
                    .toMap()
                tripReceipts.map {
                    HistoryReceipt(
                        id = it.id,
                        status = it.status,
                        totalMinor = it.totalMinor,
                        storeName = it.storeNameRaw,
                        capturedAt = it.capturedAt,
                        // Parca: AYNI FIZIKSEL FISIN baska parcalari da var.
                        //
                        // `totalMinor == null` KOSULU KALDIRILDI ve sebebi
                        // somut: toplam yalnizca SON parcada basili, yani o
                        // kosul tam olarak son parcayi disarida birakiyordu.
                        // Son parca butun fisin toplamini tasiyip yalnizca kendi
                        // satirlarini tasidigi icin MISMATCHED olmasi
                        // KACINILMAZ - ve amber "sorunlu" zemini giyiyordu. Her
                        // uzun fiste, her seferinde, kullanici hata yapmadan.
                        //
                        // Kosul GEZIDEKI FIS SAYISI degil GRUP BOYU: iki ayri
                        // magaza fisi tek gezide olabiliyor ve o durumda ikisi
                        // de tek basina degerlendirilmeli - biri gercekten
                        // tutmuyorsa amber HAK EDILMIS.
                        //
                        // FAILED bundan MUAF: okunamayan bir parca gercek bir
                        // sorun ve sorunlu gorunmeye devam ediyor.
                        isPart = it.status == ReceiptStatus.MISMATCHED &&
                            (groupSize[it.id] ?: 1) > 1,
                    )
                }
            },
        )
    }
}
