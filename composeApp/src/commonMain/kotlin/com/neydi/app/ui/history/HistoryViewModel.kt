package com.neydi.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptDao
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.db.Trip
import com.neydi.app.data.db.TripDao
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
        ) { geziler, fisler -> combineTrips(geziler, fisler) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

internal fun combineTrips(trips: List<Trip>, receipts: List<Receipt>): List<HistoryTrip> {
    val fisHaritasi = receipts.groupBy { it.tripId }
    return trips.map { trip ->
        HistoryTrip(
            id = trip.id,
            // completedAt kapaliligin otoritesi; observeHistory zaten yalnizca
            // kapanmis geziler donduruyor, yine de null'a startedAt ile
            // dusuyoruz ki liste bir sira anahtari kaybetmesin.
            closedAt = trip.completedAt ?: trip.startedAt,
            totalMinor = trip.totalMinor,
            receipts = fisHaritasi[trip.id].orEmpty().let { tripReceipts ->
                tripReceipts.map {
                    HistoryReceipt(
                        id = it.id,
                        status = it.status,
                        totalMinor = it.totalMinor,
                        storeName = it.storeNameRaw,
                        capturedAt = it.capturedAt,
                        // Parca: toplami okunamamis (MISMATCHED + null) bir fis,
                        // ayni gezide baska fisler varken buyuk olasilikla uzun
                        // fisin parcasi - toplam yalnizca son parcada basili.
                        isPart = it.status == ReceiptStatus.MISMATCHED &&
                            it.totalMinor == null && tripReceipts.size > 1,
                    )
                }
            },
        )
    }
}
