package com.neydi.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.Trip
import com.neydi.app.data.db.TripDao
import com.neydi.app.data.db.TripLineDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Kapanmis bir gezi.
 *
 * FIS ALANLARI E8'DE OLDU. Bu sinif once fis listesi, parca indeksi, okuma
 * durumu ve fisten okunan magaza adi tasiyordu; pivotla dordu de kaynaksiz
 * kaldi. Gezi kendi basina anlamli: NE ZAMAN ve KAC KALEM.
 *
 * TUTAR GECICI OLARAK YOK. `Trip.totalMinor`i yalnizca fis toplami yaziyordu
 * ve E11'de kolonuyla birlikte siliniyor; yerine E18'de gozlemlerden
 * hesaplanan `~` tahmini geliyor. Arada bir sey UYDURMAK - ornegin sifir
 * yazmak - "bedava alisveris" demek olurdu.
 */
data class HistoryTrip(
    val id: String,
    val closedAt: Long,
    /** *"14 Agu · 18 urun"* satirindaki adet. */
    val itemCount: Int = 0,
)

/**
 * Gecmis ekraninin ViewModel'i.
 *
 * SADECE OKUYOR ve artik gercekten oyle: fis donemi bu ekrani "yanlis okunmus
 * bir fise geri donmenin tek yolu" yapmisti, dolayisiyla satirlar Fis
 * Kontrol'e goturuyordu. O hedef yok; ekran artik yalnizca gecmisi gosteriyor.
 */
class HistoryViewModel(
    tripDao: TripDao,
    tripLineDao: TripLineDao,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    /**
     * Geziler ve kalem sayilari AYRI sorgulardan birlestiriliyor.
     *
     * Gezi basina ayri sorgu acmak N+1 Flow demekti; hane olcegi kucuk (iki
     * kisi, elli gezi) oldugu icin iki sorguyu bellekte eslemek hem daha hizli
     * hem tek atomik yayin veriyor - liste yarim dolu gorunmuyor.
     */
    val trips: StateFlow<List<HistoryTrip>> =
        combine(
            tripDao.observeHistory(household),
            tripLineDao.observeLineCounts(household),
        ) { geziler, sayilar ->
            combineTrips(
                trips = geziler,
                lineCounts = sayilar.associate { it.tripId to it.lineCount },
            )
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

internal fun combineTrips(
    trips: List<Trip>,
    lineCounts: Map<String, Int> = emptyMap(),
): List<HistoryTrip> = trips.map { trip ->
    HistoryTrip(
        id = trip.id,
        // completedAt kapaliligin otoritesi; observeHistory zaten yalnizca
        // kapanmis geziler donduruyor, yine de null'a startedAt ile dusuyoruz
        // ki liste bir sira anahtari kaybetmesin.
        closedAt = trip.completedAt ?: trip.startedAt,
        itemCount = lineCounts[trip.id] ?: 0,
    )
}
