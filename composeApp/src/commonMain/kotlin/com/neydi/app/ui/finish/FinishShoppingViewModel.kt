package com.neydi.app.ui.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.db.TripLineDao
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
) : ViewModel() {

    val rows: StateFlow<List<FinishRow>> =
        if (tripId == null) {
            flowOf(emptyList())
        } else {
            tripLineDao.observeList(tripId).map { list ->
                list.map { FinishRow(it.rowId, it.name, it.count, it.unit, it.checked) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Bir satirin "alindi" durumunu degistirir.
     *
     * ANINDA YAZIYOR, "Kaydet" beklemiyor: kullanici ekrandan geri tusuyla
     * cikarsa duzeltmesi kaybolmamali. Gezi zaten kapali, yani bu yazma
     * hicbir seyi bekletmiyor.
     */
    fun setTaken(rowId: String, taken: Boolean) {
        viewModelScope.launch { repo.setTaken(rowId, taken) }
    }
}
