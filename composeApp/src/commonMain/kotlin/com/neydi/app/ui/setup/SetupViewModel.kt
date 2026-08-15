package com.neydi.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.AppSettings
import com.neydi.app.data.db.AppSettingsDao
import com.neydi.app.data.db.CatalogSeedDao
import com.neydi.app.data.db.MemberDao
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.repo.STAPLE_LIMIT
import com.neydi.app.data.repo.resolveProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Kurulumun ikinci adimindaki tempo secenegi. */
data class TempoOption(val label: String, val days: Int?)

/**
 * Tasarimin dort tempo cipi.
 *
 * "BELIRSIZ" NULL TASIYOR ve bu bir cevap: kullanici bilmedigini soyledi,
 * biz de uydurmuyoruz. `medianIntervalDays` gercek veriyle dolana kadar oneri
 * motoru bu haneye tempo onculu vermiyor.
 */
internal val TEMPO_OPTIONS = listOf(
    TempoOption("Haftada 1", 7),
    TempoOption("10 günde bir", 10),
    TempoOption("2 haftada bir", 14),
    TempoOption("Belirsiz", null),
)

/** Kurulumun ilk adimindaki bir urun cipi. */
data class SetupItem(val seedId: String, val name: String, val selected: Boolean)

data class SetupState(
    val loading: Boolean = true,
    /** 0 = "Her zamankiler", 1 = "Tempo". Tasarimda "1 / 2" ve "2 / 2". */
    val step: Int = 0,
    val items: List<SetupItem> = emptyList(),
    val tempoDays: Int? = null,
    val tempoChosen: Boolean = false,
    val done: Boolean = false,
) {
    val selectedCount: Int get() = items.count { it.selected }

    /**
     * Ust sinira gelindi mi. `STAPLE_LIMIT` Ayarlar'da da yaziyor ("1/12"),
     * yani sinir kullanicinin daha sonra gorecegi sayinin AYNISI.
     */
    val atLimit: Boolean get() = selectedCount >= STAPLE_LIMIT
    val limit: Int get() = STAPLE_LIMIT

    /** Tasarimda iki adim var; ucuncusu (hane) auth ile Faz 7'de geliyor. */
    val stepCount: Int get() = 2
}

/**
 * Kurulum (Ekran 8) - IKI ADIM (tasarim karari 6).
 *
 * HANE ADIMI YOK ve bu bilincli: var olmayan bir auth icin adim cizmek,
 * tutulamayacak bir soz vermek olurdu - e-posta alani calismazsa ilk ekran ilk
 * hayal kirikligi olur. O adim auth ile birlikte Faz 7'de geliyor ve kurulum o
 * gun yeniden uc adim oluyor.
 */
class SetupViewModel(
    private val catalogSeedDao: CatalogSeedDao,
    private val settingsDao: AppSettingsDao,
    private val memberDao: MemberDao,
    private val repo: ListRepository,
    private val clock: () -> Long,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // SABIT LISTE DEGIL, KATALOGDAN: sira `commonalityRank`ten geliyor,
            // yani katalog buyudukce cipler kendiliginden tazeleniyor ve ayni
            // bilginin iki ayri kopyasi olmuyor (tasarim karari 5'in ayni
            // gerekcesi, burada 40 cip icin).
            val seeds = catalogSeedDao.mostCommon(limit = CHIP_COUNT)
            _state.value = SetupState(
                loading = false,
                items = seeds.map { SetupItem(it.id, it.name, selected = false) },
            )
        }
    }

    fun toggle(seedId: String) {
        val current = _state.value
        val item = current.items.firstOrNull { it.seedId == seedId } ?: return
        // SINIRA GELINDIYSE YENI SECIM YOK ama SECIM KALDIRMA HER ZAMAN VAR.
        // Ikisini birden kilitlemek kullaniciyi kendi sectigi on iki uruniyle
        // hapsederdi.
        if (!item.selected && current.atLimit) return
        _state.value = current.copy(
            items = current.items.map {
                if (it.seedId == seedId) it.copy(selected = !it.selected) else it
            },
        )
    }

    fun next() {
        _state.value = _state.value.copy(step = 1)
    }

    fun chooseTempo(days: Int?) {
        _state.value = _state.value.copy(tempoDays = days, tempoChosen = true)
    }

    /**
     * Kurulumu kapatir: secilen urunler SABIT olarak yaziliyor, tempo ve
     * tamamlanma damgasi ayarlara gidiyor.
     *
     * ATLAMA DA BURADAN GECIYOR ve gecmeli: "Atla" demek "kurulumu bir daha
     * gosterme" demek. Damga yazilmazsa kullanici her acilista ayni ekrani
     * gorurdu - atladigi ekrani.
     */
    fun finish(onDone: () -> Unit) {
        if (_state.value.done) return
        _state.value = _state.value.copy(done = true)
        viewModelScope.launch {
            val chosen = _state.value.items.filter { it.selected }
            val seeds = catalogSeedDao.mostCommon(limit = CHIP_COUNT).associateBy { it.id }
            chosen.forEach { item ->
                val seed = seeds[item.seedId] ?: return@forEach
                val product = resolveProduct(
                    repo = repo,
                    catalogSeedDao = catalogSeedDao,
                    householdId = household,
                    name = seed.name,
                    categoryId = seed.categoryId,
                    unit = seed.defaultUnit,
                )
                repo.setStaple(product.id, true)
            }
            settingsDao.upsert(
                AppSettings(
                    householdId = household,
                    setupCompletedAt = clock(),
                    // Tempo secilmediyse (atlandi) null kaliyor - "Belirsiz"
                    // ile ayni yer, cunku ikisi de ayni seyi soyluyor:
                    // bilmiyoruz.
                    tempoDays = _state.value.tempoDays,
                    createdAt = clock(),
                ),
            )
            // LISTE DOLU ACILMALI ve bunu cihaz ogretti: kurulum sabitleri
            // yaziyordu ama LISTEDE HICBIR SEY GORUNMUYORDU - sabitler geziye
            // gezi ACILIRKEN dusuyor (`seedStaples`) ve kurulumdan cikinca
            // acik gezi yoktu. Kullanici on iki urun sectikten sonra "Liste
            // bos" goruyordu; tasarimin kendi cumlesi ise "kurulum yapildi ->
            // liste zaten dolu".
            //
            // Hicbir sey secilmediyse gezi ACILMIYOR: bos bir gezi acmak
            // Gecmis'e hayalet satir yazardi ve ilk gun bos hali de kaybolurdu.
            if (chosen.isNotEmpty()) {
                memberDao.self(household)?.let { self ->
                    repo.openOrGetActiveTrip(householdId = household, memberId = self.id)
                }
            }
            onDone()
        }
    }

    private companion object {
        /** Tasarimin cerceve verisi 40 cip gosteriyor. */
        const val CHIP_COUNT = 40
    }
}
