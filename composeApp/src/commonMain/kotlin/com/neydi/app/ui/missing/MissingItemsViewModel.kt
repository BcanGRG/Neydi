package com.neydi.app.ui.missing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.MemberDao
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.suggest.Suggestion
import com.neydi.app.data.suggest.SuggestionEngine
import com.neydi.app.data.suggest.longReasonText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** En fazla kac satir gosterilir (tasarim: "En fazla 8 satir"). */
private const val MAX_ROWS = 8

/**
 * Ekran 3'un uc bolumu ve VARSAYILANLARI.
 *
 * ASIMETRI BILINCLI ve tasarimin kendi karari: *"gecen sefer unuttun"* ve
 * *"her zamankiler"* varsayilan ACIK, tahmin bolumu varsayilan KAPALI.
 * Gerekcesi: tahmin uygulamanin **kendi basina akil yurutttugu tek yer** ve
 * varsayilan-acik muamelesi gormez - kullanici onaylamadan listeye girmemeli.
 */
enum class MissingSection(val title: String, val checkedByDefault: Boolean) {
    /** Olay tabanli: gecen gezide FORGOTTEN isaretlendi. */
    FORGOTTEN("Geçen sefer unuttun", checkedByDefault = true),

    /** Beyan tabanli: kullanici sabit ilan etti (F6.8). */
    STAPLE("Her zamankiler", checkedByDefault = true),

    /** Cikarim tabanli: yalnizca skor. Varsayilan KAPALI. */
    PREDICTED("Bitmiş olabilir", checkedByDefault = false),
}

data class MissingRow(
    val productId: String,
    val name: String,
    val reason: String,
    val section: MissingSection,
    val selected: Boolean,
)

data class MissingState(
    val loading: Boolean = true,
    val rows: List<MissingRow> = emptyList(),
) {
    val selectedCount: Int get() = rows.count { it.selected }

    /**
     * EKRAN HIC ACILMAZ HALI.
     *
     * Tasarimin *"uygulamanin en onemli bos-durum karari"*: onerilecek bir sey
     * yoksa bos bir kontrol listesi gostermek kullaniciya butonun degersiz
     * oldugunu OGRETIR. Yukleme bitmeden bu true donmemeli, yoksa ekran
     * acilir acilmaz kapanir.
     */
    val shouldSkip: Boolean get() = !loading && rows.isEmpty()
}

/**
 * Ekran 3 - "Eksik Olabilir" (F6.4).
 *
 * Onerileri motordan aliyor ve UC BOLUME ayiriyor. Bolum uyeligi skordan
 * degil, onerinin NEDENINDEN geliyor: olay (unutuldu) > beyan (sabit) >
 * cikarim (tahmin). Ayni urun iki bolume birden giremez.
 */
class MissingItemsViewModel(
    private val engine: SuggestionEngine,
    private val repo: ListRepository,
    private val productDao: ProductDao,
    private val memberDao: MemberDao,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    private val _state = MutableStateFlow(MissingState())
    val state: StateFlow<MissingState> = _state

    init {
        viewModelScope.launch {
            val rows = engine.suggestions(household)
                .map { it.toRow() }
                // Bolum sirasi tasarimdan: olay, beyan, cikarim.
                .sortedBy { it.section.ordinal }
                .take(MAX_ROWS)
            _state.value = MissingState(loading = false, rows = rows)
        }
    }

    private fun Suggestion.toRow(): MissingRow {
        val section = when {
            forgottenLastTrip -> MissingSection.FORGOTTEN
            isStaple -> MissingSection.STAPLE
            else -> MissingSection.PREDICTED
        }
        return MissingRow(
            productId = productId,
            name = name,
            reason = longReasonText(),
            section = section,
            selected = section.checkedByDefault,
        )
    }

    fun toggle(productId: String) {
        _state.update { state ->
            state.copy(
                rows = state.rows.map {
                    if (it.productId == productId) it.copy(selected = !it.selected) else it
                },
            )
        }
    }

    /**
     * Secilenleri listeye ekler.
     *
     * `fromSuggestion = true` YAZILIYOR: oneri isabetinin olculebilmesi buna
     * bagli - hangi onerinin kabul edildigini baska hicbir yerden bilemiyoruz.
     *
     * @param onDone ekleme bitince cagriliyor; cagiran taraf alisveris moduna
     *   geciriyor. Ekleme ile mod gecisini ayni yerde yapmak, ekleme
     *   basarisiz olsa bile moda gecmek demek olurdu.
     */
    fun addSelected(onDone: () -> Unit) {
        viewModelScope.launch {
            val memberId = memberDao.self(household)?.id
            if (memberId != null) {
                val trip = repo.openOrGetActiveTrip(household, memberId)
                _state.value.rows.filter { it.selected }.forEach { row ->
                    productDao.byId(row.productId)?.let { product ->
                        repo.add(
                            householdId = household,
                            tripId = trip.id,
                            product = product,
                            memberId = memberId,
                            isFromSuggestion = true,
                        )
                    }
                }
                // EKLEME BITTIKTEN SONRA moda geciliyor: ekleme basarisiz
                // olursa kullanici hala planlama modunda ve listesi eksik -
                // reyonda eksik listeyle dolasmaktan iyi.
                repo.setShoppingMode(trip.id, true)
            }
            onDone()
        }
    }

    /**
     * Onerilecek bir sey yokken dogrudan alisveris moduna gecer.
     *
     * Ekran hic gorunmuyor; kullanici "Alisverise cikiyorum"a bastigini
     * hatirliyor ve arada bos bir ekran gormuyor.
     */
    fun skipToShopping(onDone: () -> Unit) {
        viewModelScope.launch {
            val memberId = memberDao.self(household)?.id
            if (memberId != null) {
                repo.setShoppingMode(repo.openOrGetActiveTrip(household, memberId).id, true)
            }
            onDone()
        }
    }
}
