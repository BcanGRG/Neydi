package com.neydi.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.MemberDao
import com.neydi.app.data.repo.DataWipe
import com.neydi.app.data.repo.WipeCounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Silme ekranindaki tek satir: ne gidecek, kac tane. */
data class DeleteRow(val name: String, val count: String)

data class DeleteDataState(
    val rows: List<DeleteRow> = emptyList(),
    /**
     * Hane iki kisilikse tasarimin es uyarisi ciziliyor.
     *
     * KOSULLU CIZILIYOR cunku tasarimin cumlesi de kosullu: *"Hane iki
     * kisilikse esinin cihazindaki veri de silinir."* Tek kisilik hanede o
     * cumleyi yazmak, olmayan bir riski varmis gibi gostermek olurdu - ve
     * bugun hane HER ZAMAN tek kisilik (auth Faz 7).
     */
    val warnsSpouse: Boolean = false,
    /** Silme kosuyor - iki kez basilmasin diye dugme kilitleniyor. */
    val deleting: Boolean = false,
    val done: Boolean = false,
)

/**
 * "Verilerimi sil" onay destinasyonu (tasarim karari 2).
 *
 * SAYILAR EKRAN ACILIRKEN BIR KEZ OKUNUYOR, akisla degil. Akis olsaydi silme
 * sirasinda rakamlar gozun onunde erirdi; onay ekraninin isi kullanicinin
 * ONAYLADIGI SEYI sabit tutmak.
 */
class DeleteDataViewModel(
    private val wipe: DataWipe,
    private val memberDao: MemberDao,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    private val _state = MutableStateFlow(DeleteDataState())
    val state: StateFlow<DeleteDataState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val counts = wipe.counts(household)
            _state.value = DeleteDataState(
                rows = rowsOf(counts),
                warnsSpouse = memberDao.observeAll(household).first().size > 1,
            )
        }
    }

    fun delete(onDone: () -> Unit) {
        if (_state.value.deleting) return
        _state.value = _state.value.copy(deleting = true)
        viewModelScope.launch {
            wipe.wipe(household)
            _state.value = _state.value.copy(deleting = false, done = true)
            onDone()
        }
    }
}

/**
 * Sayilari tasarimin dort satirina cevirir.
 *
 * SIFIR SAYAN SATIR CIZILMIYOR - "Alışveriş 0" yazmak, olmayan bir seyi
 * silinecekler listesine koymak demek. Ayni ekranin ayni kurali: bos bolum
 * cizilmez.
 */
internal fun rowsOf(counts: WipeCounts): List<DeleteRow> = buildList {
    if (counts.trips > 0) add(DeleteRow("Alışveriş", "${counts.trips}"))
    if (counts.products + counts.prices > 0) {
        add(DeleteRow("Ürün ve fiyat geçmişi", "${counts.products} + ${counts.prices}"))
    }
    if (counts.staples + counts.blocks > 0) {
        add(DeleteRow("Her zamankiler, önerilmeyenler", "${counts.staples} + ${counts.blocks}"))
    }
}
