package com.neydi.app.ui.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.TakeOutcome
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.stats.ProductStatsRebuilder
import com.neydi.app.data.repo.ListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Bitir ekranindaki tek satir. */
data class FinishRow(
    val id: String,
    val name: String,
    val count: Double,
    val unit: String,
    val taken: Boolean,
    /**
     * Kullanicinin beyani; beyan yoksa iyimser varsayim geregi [TakeOutcome.TAKEN]
     * gosteriliyor - kapanis zaten oyle yazdi.
     */
    val outcome: TakeOutcome,
)

/**
 * Fissiz mutabakati DUZELTME ekrani (F4.8).
 *
 * Mutabakat kapanista ZATEN CALISTI: planlananlar alindi yazildi. Bu ekranin
 * isi o iyimserligi geri almak - "sut bitmisti, alamadim" gibi. Iyimser
 * varsayimi duzeltilebilir yapmadan bırakmak onu bir tuzaga cevirirdi:
 * kullanici hic almadigi urunun gecmisine girdigini gormez, oneri motoru da
 * o urunu duzenli aliniyor sanip yanlis araliktan onerir.
 */
class FinishShoppingViewModel(
    private val tripId: String?,
    private val tripLineDao: TripLineDao,
    private val repo: ListRepository,
    private val statsRebuilder: ProductStatsRebuilder,
) : ViewModel() {

    private val householdId = DEFAULT_HOUSEHOLD_ID

    val rows: StateFlow<List<FinishRow>> =
        if (tripId == null) {
            flowOf(emptyList())
        } else {
            tripLineDao.observeList(tripId).map { list ->
                list.map {
                    FinishRow(
                        id = it.rowId,
                        name = it.name,
                        count = it.count,
                        unit = it.unit,
                        taken = it.checked,
                        outcome = it.takeOutcome ?: if (it.checked) TakeOutcome.TAKEN else TakeOutcome.FORGOTTEN,
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Kullanicinin beyani: aldim / gerekmedi / unuttum (F4.12).
     *
     * ANINDA YAZIYOR, "Kaydet" beklemiyor: kullanici ekrandan geri tusuyla
     * cikarsa duzeltmesi kaybolmamali. Gezi zaten kapali, yani bu yazma
     * hicbir seyi bekletmiyor.
     *
     * Ardindan ISTATISTIK YENIDEN KURULUYOR: "gerekmedi"/"unuttum" satiri
     * isaretsiz birakiyor, yani az once kapanista alim sayilmis bir satir
     * artik alim degil - `ProductStats` bunu ancak yeniden kurulumla gorur.
     */
    fun setOutcome(rowId: String, outcome: TakeOutcome) {
        viewModelScope.launch {
            repo.setOutcome(rowId, outcome)
            statsRebuilder.rebuild(householdId)
        }
    }
}
