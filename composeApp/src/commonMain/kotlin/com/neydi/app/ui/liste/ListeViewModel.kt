package com.neydi.app.ui.liste

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.VARSAYILAN_HANE_ID
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.CatalogSeedDao
import com.neydi.app.data.db.MemberDao
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.matchKey
import com.neydi.app.data.miktarAyristir
import com.neydi.app.data.repo.ListeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Liste ekraninin durum sahibi.
 *
 * Satirlarin ESLESTIRILMESI (TripLine + Product + Category) SQL'de yapiliyor,
 * burada degil: uc ayri Flow'u combine etmek her degisimde uc yeniden yayin
 * uretir ve liste bir kare bosluklu gorunur.
 */
class ListeViewModel(
    private val repo: ListeRepository,
    private val tripLineDao: TripLineDao,
    private val memberDao: MemberDao,
    private val catalogSeedDao: CatalogSeedDao,
) : ViewModel() {

    private val hane = VARSAYILAN_HANE_ID
    /**
     * FLOW, tek seferlik okuma DEGIL: uyeyi bootstrap yaratiyor ve bu ekran
     * ondan once acilabiliyor. Tek okuma o yaristas null doner, bir daha
     * guncellenmez ve butun eklemeler sessizce hicbir sey yapmaz.
     */
    private val benimUyeId: StateFlow<String?> =
        memberDao.observeSelf(hane)
            .map { it?.id }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val durum: StateFlow<ListeDurumu> =
        combine(
            repo.aktifAlisveris(hane).flatMapLatest { trip ->
                if (trip == null) flowOf(emptyList()) else tripLineDao.observeListe(trip.id)
            },
            benimUyeId,
        ) { satirlar, uyeId -> satirlar.bolumlere(uyeId) }
            .stateIn(
                scope = viewModelScope,
                // 5 saniye: konfigurasyon degisiminde akis kopmasin, ama
                // uygulama arka plandayken veritabanini dinlemeye devam etmesin.
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ListeDurumu(),
            )

    fun isaretle(satirId: String, isaretli: Boolean) {
        viewModelScope.launch { repo.isaretle(satirId, isaretli) }
    }

    fun cikar(satirId: String) {
        viewModelScope.launch { repo.cikar(satirId) }
    }

    // --- Hizli ekleme (F3.3) --------------------------------------------

    private val _girdi = MutableStateFlow("")
    val girdi: StateFlow<String> = _girdi

    /**
     * Otomatik tamamlama. SKORA GORE siralaniyor, alfabetik DEGIL - kullanici
     * "ek" yazinca once Ekmek gormeli. Bu, algilanan zekanin buyuk kismini
     * sifir maliyetle veriyor; alfabetik siralamak ayni veriyle aptal gorunur.
     */
    private val _oneriler = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val oneriler: StateFlow<List<CatalogSeed>> = _oneriler

    fun girdiDegisti(yeni: String) {
        _girdi.value = yeni
        viewModelScope.launch {
            // Miktar onekini ATIP arama yapiyoruz: "2 kg el" yazan biri
            // "el" ile eslesme bekler, "2 kg el" ile degil.
            val ad = miktarAyristir(yeni).ad
            _oneriler.value = if (ad.isBlank()) {
                emptyList()
            } else {
                catalogSeedDao.search(matchKey(ad), limit = 6)
            }
        }
    }

    /** Serbest metinden ekle: "2 kg elma" gibi. */
    fun ekle(metin: String) {
        val m = miktarAyristir(metin)
        if (m.ad.isBlank()) return
        ekleIc(ad = m.ad, kategoriId = null, birim = m.birim, adet = m.adet)
    }

    /** Oneri cipinden ekle: kategori ve birim katalogdan gelir. */
    fun oneridenEkle(tohum: CatalogSeed) {
        val m = miktarAyristir(_girdi.value)
        ekleIc(
            ad = tohum.name,
            kategoriId = tohum.categoryId,
            birim = m.birim ?: tohum.defaultUnit,
            adet = m.adet,
        )
    }

    private fun ekleIc(ad: String, kategoriId: String?, birim: String?, adet: Double) {
        viewModelScope.launch {
            // Flow henuz yayin yapmadiysa DOGRUDAN oku. Sessizce vazgecmek
            // kullanicinin yazdigi seyin kaybolmasi demek olurdu.
            val uyeId = benimUyeId.value ?: memberDao.self(hane)?.id ?: return@launch
            val trip = repo.aktifAlisverisiAcVeyaAl(hane)

            // Kategori verilmediyse katalogda ayni matchKey'i ara: kullanici
            // elle yazsa bile urun dogru reyona dussun. Bulunmazsa son care
            // olarak "Temel Gida" - kategorisiz urun listede bolumsuz kalirdi.
            val anahtar = matchKey(ad)
            val tohum = if (kategoriId != null) {
                null
            } else {
                catalogSeedDao.search(anahtar, limit = 1).firstOrNull { it.matchKey == anahtar }
            }

            val urun = repo.urunBulVeyaOlustur(
                householdId = hane,
                // Katalogda eslesme varsa KANONIK adi kullan: "sut" yazan biri
                // listede "Süt" gormeli. Yazildigi gibi birakmak ilk yazanin
                // yazimini kalici yapar - matchKey ikisini ayni urun saydigi
                // icin sonradan duzeltilemez de.
                ad = tohum?.name ?: ad,
                kategoriId = kategoriId ?: tohum?.categoryId ?: VARSAYILAN_KATEGORI,
                birim = birim ?: tohum?.defaultUnit ?: "adet",
            )
            repo.ekle(
                householdId = hane,
                tripId = trip.id,
                product = urun,
                memberId = uyeId,
                adet = adet,
            )
            _girdi.value = ""
            _oneriler.value = emptyList()
        }
    }

    private companion object {
        /** Katalogda eslesmeyen serbest urunler buraya duser. */
        const val VARSAYILAN_KATEGORI = "temel-gida"
    }
}
