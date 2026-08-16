package com.neydi.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.HouseholdDao
import com.neydi.app.data.db.MemberDao
import com.neydi.app.data.db.PriceObservationDao
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.db.Store
import com.neydi.app.data.db.StoreDao
import com.neydi.app.data.repo.STAPLE_LIMIT
import com.neydi.app.ui.components.turkishInitials
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Ayarlar'daki bir sabit urun satiri. */
data class StapleRow(val productId: String, val name: String)

/** Hane uyesi - avatar icin bas harfleriyle. */
data class MemberRow(val id: String, val initials: String, val isSelf: Boolean)

/**
 * Ayarlar'daki bir zincir (tasarim karari 11 + 36).
 *
 * SAYI YOK, BAYRAK VAR. Once hic bir sey tasimiyordu; karar 36 zincirleri iki
 * renge ayirdi - gozlemi olan metin renginde, yalnizca tohumdan gelen soluk.
 * Ayrimin GEREKTIRDIGI tek sey bir Boolean: "burada hic fiyat kaydettin mi".
 *
 * Sayi hala YOK ve olmamali: bolumun isi "nerelerden aliyorsun" sorusuna cevap
 * vermek, gozlem saymak degil. Sayilar urun ekseninde, Urun Detayi'nda duruyor.
 */
data class StoreRow(val id: String, val name: String, val hasObservation: Boolean = false)

/**
 * Ayarlar ekraninin durumu (Ekran 7).
 *
 * BOS BOLUM CIZILMEZ kurali burada VERIYLE karsilaniyor: alanlar null ya da
 * bos geldiginde ekran o bolumu hic acmiyor. Tasarimin kendi ifadesi: *"Bos
 * bir bolum basligi, olmayan bir isi varmis gibi gosterir."*
 */
data class SettingsState(
    val householdName: String? = null,
    /** Null ise "Katilma kodu" satiri CIZILMIYOR - Faz 7'ye kadar uretilmiyor. */
    val joinCode: String? = null,
    val members: List<MemberRow> = emptyList(),
    val staples: List<StapleRow> = emptyList(),
    val stapleLimit: Int = STAPLE_LIMIT,
    /**
     * Zincirler - gozlemi olanlar ONCE (tasarim karari 36).
     *
     * Bos ise bolum yine de cizilmiyor, ama sebebi degisti: karar 36 esigi
     * kaldirdi, yani "gozlem yoksa gizle" kurali OLDU. Geriye gezinme
     * sozlesmesinin genel degismezi kaldi - *bos bolum cizilmez*. Tohum yedi
     * zincir yazdigi icin bu dal pratikte yalnizca kullanici hepsini silerse
     * yasanir.
     */
    val stores: List<StoreRow> = emptyList(),
)

/**
 * Zincirleri Ayarlar satirlarina cevirir (tasarim karari 36).
 *
 * VIEWMODEL'IN ICINDEN CIKARILDI cunku burasi kararin kendisi: hangi zincir
 * koyu cizilecek ve hangi sirada duracak. `combine` lambdasinin icinde kalsaydi
 * test edebilmek icin Room + Main dispatcher + `viewModelScope` kurmak
 * gerekirdi - uc altyapi parcasi, sinanan sey bir Boolean ve bir siralamayken.
 *
 * GOZLEMLILER ONCE: renk ayrimi tek basina yetmiyor. Yedi adin arasinda
 * ikisinin koyu oldugunu gormek icin once onlari BULMAK gerekir; siralama
 * bilgiyi taranabilir yapiyor, renk de hangisi oldugunu soyluyor. Ayrica renk
 * gormeyen kullaniciya bilgi konum olarak ulasiyor - tek tasiyici renk olsaydi
 * ulasmazdi.
 *
 * `sortedByDescending` KARARLI: her grubun icinde `observeAll`in
 * `ORDER BY createdAt` sirasi, yani tohum sirasi korunuyor.
 */
internal fun storeRows(stores: List<Store>, observedStoreIds: Set<String>): List<StoreRow> =
    stores
        .map { StoreRow(id = it.id, name = it.name, hasObservation = it.id in observedStoreIds) }
        .sortedByDescending { it.hasObservation }

class SettingsViewModel(
    householdDao: HouseholdDao,
    memberDao: MemberDao,
    private val productDao: ProductDao,
    storeDao: StoreDao,
    priceObservationDao: PriceObservationDao,
    private val clock: () -> Long,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    val state: StateFlow<SettingsState> = combine(
        householdDao.observeActive(),
        memberDao.observeAll(household),
        productDao.observeStaples(household),
        storeDao.observeAll(household),
        priceObservationDao.observeStoreIdsWithObservations(household),
    ) { home, members, staples, stores, observedStoreIds ->
        val observed = observedStoreIds.toSet()
        SettingsState(
            householdName = home?.name,
            joinCode = home?.joinCode,
            members = members.map {
                MemberRow(
                    id = it.id,
                    initials = turkishInitials(it.displayName),
                    isSelf = it.isSelf,
                )
            },
            staples = staples.map { StapleRow(productId = it.id, name = it.name) },
            stores = storeRows(stores, observed),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    /**
     * Sabitten cikar - tasarimdaki satir sonu `close` dugmesi.
     *
     * Urunu SILMIYOR, yalnizca `isStaple`i dusuruyor: kullanici "her listeye
     * otomatik ekleme" demek istiyor, "bu urunu unut" demek degil. Fiyat
     * gecmisi ve istatistik yerinde kaliyor.
     */
    fun removeStaple(productId: String) {
        viewModelScope.launch { productDao.setStaple(productId, false, clock()) }
    }
}
