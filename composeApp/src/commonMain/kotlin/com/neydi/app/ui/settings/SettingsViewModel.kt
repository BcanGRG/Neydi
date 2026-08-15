package com.neydi.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.HouseholdDao
import com.neydi.app.data.db.MemberDao
import com.neydi.app.data.db.ProductDao
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
 * Ayarlar'daki bir magaza satiri (tasarim karari 11).
 *
 * `receiptCount` YOK ve olmamali: bolumun isi "nerelerden alisveris ediyorsun"
 * sorusuna cevap vermek, fis saymak degil - sayilar Gecmis'te duruyor.
 */
data class StoreRow(val id: String, val name: String)

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
    /** Bos ise Magazalar bolumu HIC cizilmiyor (tasarim karari 11). */
    val stores: List<StoreRow> = emptyList(),
)

class SettingsViewModel(
    householdDao: HouseholdDao,
    memberDao: MemberDao,
    private val productDao: ProductDao,
    storeDao: StoreDao,
    private val clock: () -> Long,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    val state: StateFlow<SettingsState> = combine(
        householdDao.observeActive(),
        memberDao.observeAll(household),
        productDao.observeStaples(household),
        storeDao.observeAll(household),
    ) { home, members, staples, stores ->
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
            stores = stores.map { StoreRow(id = it.id, name = it.name) },
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
