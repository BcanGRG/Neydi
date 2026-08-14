package com.neydi.app.ui.liste

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.VARSAYILAN_HANE_ID
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.CatalogSeedDao
import com.neydi.app.data.db.Category
import com.neydi.app.data.db.CategoryDao
import com.neydi.app.data.db.MemberDao
import com.neydi.app.data.db.PriceObservationDao
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.matchKey
import com.neydi.app.data.miktarAyristir
import com.neydi.app.data.panoSatirlari
import com.neydi.app.data.repo.ListeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    private val productDao: ProductDao,
    private val catalogSeedDao: CatalogSeedDao,
    private val categoryDao: CategoryDao,
    private val priceObservationDao: PriceObservationDao,
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

    private val _alisverisModu = MutableStateFlow(false)
    val alisverisModu: StateFlow<Boolean> = _alisverisModu

    /**
     * Bos durumu ILK GUN mu DONGU ORTASI mi ayirt eder.
     * Hane hic urun gormediyse ilk gun; gormus ama liste bossa dongu ortasi.
     */
    private val bosTur: Flow<BosTur> = productDao.observeAll(hane)
        .map { if (it.isEmpty()) BosTur.ILK_GUN else BosTur.DONGU_ORTASI }

    @OptIn(ExperimentalCoroutinesApi::class)
    val durum: StateFlow<ListeDurumu> =
        combine(
            repo.aktifAlisveris(hane).flatMapLatest { trip ->
                if (trip == null) flowOf(emptyList()) else tripLineDao.observeListe(trip.id)
            },
            benimUyeId,
            _alisverisModu,
            bosTur,
        ) { satirlar, uyeId, mod, tur -> satirlar.bolumlere(uyeId, mod, tur) }
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

    fun alisverisModunuDegistir(acik: Boolean) {
        _alisverisModu.value = acik
    }

    /** Bos durumdaki reyon cipleri. */
    val kategoriler: StateFlow<List<Category>> = categoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Bir reyona dokununca o reyonun en yaygin urunleri oneri olur. */
    fun kategoriSecildi(kategori: Category) {
        viewModelScope.launch {
            _oneriler.value = catalogSeedDao.byCategory(kategori.id, limit = 8)
            _girdi.value = ""
        }
    }

    // --- Ekle sheet (F3.7) -----------------------------------------------

    private val _sheetAcik = MutableStateFlow(false)
    val sheetAcik: StateFlow<Boolean> = _sheetAcik

    private val _sheetKategori = MutableStateFlow<Category?>(null)
    val sheetKategori: StateFlow<Category?> = _sheetKategori

    private val _sheetUrunler = MutableStateFlow<List<CatalogSeed>>(emptyList())
    val sheetUrunler: StateFlow<List<CatalogSeed>> = _sheetUrunler

    fun sheetAc() { _sheetAcik.value = true }

    fun sheetKapat() {
        _sheetAcik.value = false
        _sheetKategori.value = null
        _sheetUrunler.value = emptyList()
    }

    fun sheetKategoriSec(kategori: Category) {
        _sheetKategori.value = kategori
        viewModelScope.launch {
            // Sheet'te daha fazla urun: burada yer var, oneri seridinde yok.
            _sheetUrunler.value = catalogSeedDao.byCategory(kategori.id, limit = 30)
        }
    }

    fun sheetGeri() {
        _sheetKategori.value = null
        _sheetUrunler.value = emptyList()
    }

    /** Sheet'ten urun eklemek sheet'i KAPATMAZ: pes pese ekleme normal. */
    fun sheettenEkle(tohum: CatalogSeed) {
        ekleIc(tohum.name, tohum.categoryId, tohum.defaultUnit, 1.0)
    }

    // --- Sepet tahmini + ozet (F3.8) ---------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    val tahmin: StateFlow<SepetTahmini> =
        repo.aktifAlisveris(hane).flatMapLatest { trip ->
            if (trip == null) {
                flowOf(SepetTahmini())
            } else {
                combine(
                    priceObservationDao.observeTahmin(trip.id),
                    priceObservationDao.observeFiyatliSayisi(trip.id),
                ) { tutar, fiyatli -> SepetTahmini(tutar, fiyatli) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SepetTahmini())

    private val _ozet = MutableStateFlow<AlisverisOzeti?>(null)
    val ozet: StateFlow<AlisverisOzeti?> = _ozet

    /**
     * Alisverisi bitirir ve ozet kartini acar.
     *
     * Sure alisverisin BASLAMA aninda degil, alisveris moduna GECILDIGINDE
     * baslamis sayilmali - liste evde bir gun once aciliyor olabilir ve
     * "26 saat surdu" yazmak sacma olurdu. Trip.startedAt su an liste
     * acilisini tutuyor; F4'te alisveris modu zamani ayri saklanacak.
     */
    fun alisverisiBitir() {
        viewModelScope.launch {
            val trip = repo.aktifAlisverisiAcVeyaAl(hane)
            val satirlar = tripLineDao.observeListe(trip.id).first()
            _ozet.value = AlisverisOzeti(
                alinanSayisi = satirlar.count { it.isaretli },
                toplamSayisi = satirlar.size,
                // Tutar fisten gelecek (Faz 4); simdilik bilinmiyor.
                tutarKurus = trip.totalMinor,
                sureDakika = null,
            )
            repo.alisverisiBitir(trip.id)
            _alisverisModu.value = false
        }
    }

    fun ozetiKapat() { _ozet.value = null }

    /**
     * PANODAN TOPLU EKLEME (F3.4).
     *
     * Mevcut aliskanligi dogrudan degistiren en ucuz kazanc: liste bugun
     * WhatsApp'ta yaziliyor. Kopyalayip yapistirmak, 12 urunu tek tek
     * yazmaya gore dakikalar kazandiriyor.
     *
     * Her satir miktar ayristiricidan geciyor, yani "2 kg elma" panodan da
     * dogru dusuyor. Bos satirlar ve madde isaretleri atiliyor.
     */
    fun panodanEkle(metin: String) {
        val satirlar = panoSatirlari(metin)
        if (satirlar.isEmpty()) return
        viewModelScope.launch {
            satirlar.forEach { satir ->
                val m = miktarAyristir(satir)
                if (m.ad.isNotBlank()) ekleBekle(m.ad, null, m.birim, m.adet)
            }
        }
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
        viewModelScope.launch { ekleBekle(ad, kategoriId, birim, adet) }
    }

    /**
     * Panodan toplu ekleme SIRAYLA calismali, o yuzden suspend. Her satiri
     * ayri coroutine'e atsaydik ayni urunu iki kez iceren bir pano iki satir
     * acmayi deneyip UNIQUE kisitina carpardi.
     */
    private suspend fun ekleBekle(ad: String, kategoriId: String?, birim: String?, adet: Double) {
            // Flow henuz yayin yapmadiysa DOGRUDAN oku. Sessizce vazgecmek
            // kullanicinin yazdigi seyin kaybolmasi demek olurdu.
            val uyeId = benimUyeId.value ?: memberDao.self(hane)?.id ?: return
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

    private companion object {
        /** Katalogda eslesmeyen serbest urunler buraya duser. */
        const val VARSAYILAN_KATEGORI = "temel-gida"
    }
}

/** Fiyati bilinen urunlerin toplami ve kacinin bilindigi. */
data class SepetTahmini(
    val tutarKurus: Long = 0,
    val fiyatliSayisi: Int = 0,
)

data class AlisverisOzeti(
    val alinanSayisi: Int,
    val toplamSayisi: Int,
    val tutarKurus: Long?,
    val sureDakika: Int?,
)
