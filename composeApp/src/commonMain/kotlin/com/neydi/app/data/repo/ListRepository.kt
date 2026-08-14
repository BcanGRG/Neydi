package com.neydi.app.data.repo

import com.neydi.app.data.db.Product
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.db.Trip
import com.neydi.app.data.db.TripDao
import com.neydi.app.data.db.TripLine
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.matchKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Liste ekraninin tek veri kapisi. OFFLINE-FIRST: her yazma once yerel
 * veritabanina gider, okuma her zaman yerelden. Senkron (Faz 7) bunun
 * uzerine PendingOp kuyruguyla gelecek; ekran katmani degismeyecek.
 *
 * Zaman ve id URETIMI disaridan veriliyor (saat/uuid). Boylece repository
 * saf kalir ve testte deterministik olur - `Clock.System.now()` cagiran bir
 * repository "gecen sefer ne zaman aldik" mantigini test edilemez yapar.
 */
class ListRepository(
    private val tripDao: TripDao,
    private val tripLineDao: TripLineDao,
    private val productDao: ProductDao,
    private val saat: () -> Long,
    private val yeniId: () -> String,
) {

    fun activeTrip(householdId: String): Flow<Trip?> = tripDao.observeActive(householdId)

    /**
     * Aktif alisverisin satirlari. Aktif alisveris yoksa BOS liste - hata degil:
     * planlama modunda henuz gezi baslamamis olabilir.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun satirlar(householdId: String): Flow<List<TripLine>> =
        tripDao.observeActive(householdId).flatMapLatest { trip ->
            if (trip == null) flowOf(emptyList()) else tripLineDao.observeLines(trip.id)
        }

    /**
     * Aktif alisverisi dondurur, yoksa acar.
     *
     * "Ayni anda tek aktif alisveris" kuralinin uygulandigi TEK yer burasi:
     * kisit kismi index gerektirdigi icin semada ifade EDILEMIYOR (F2.3).
     * Yeni gezi acmadan once mutlaka buradan gecilmeli.
     */
    suspend fun openOrGetActiveTrip(householdId: String): Trip {
        tripDao.activeOrNull(householdId)?.let { return it }
        val trip = Trip(
            id = yeniId(),
            householdId = householdId,
            startedAt = saat(),
            createdAt = saat(),
        )
        tripDao.insert(trip)
        return trip
    }

    suspend fun finishShopping(tripId: String) = tripDao.complete(tripId, saat())

    /**
     * Urunu listeye ekler. ZATEN VARSA adet artirir, ikinci satir ACMAZ.
     *
     * UNIQUE(tripId, productId) bunu zaten engelliyor ama kisita carpip hata
     * almak kullaniciya "ekleyemedim" demek olurdu. Dogru davranis: es zaten
     * eklemisse adedi artir - iki kisi ayni ekmegi istedi, iki ekmek degil.
     */
    suspend fun add(
        householdId: String,
        tripId: String,
        product: Product,
        memberId: String,
        adet: Double = 1.0,
        oneridenMi: Boolean = false,
    ): TripLine {
        // SILINMISLERE DE bakiyoruz: tombstone satiri tabloda kaliyor ve
        // UNIQUE(tripId, productId) deletedAt'i bilmiyor. Yalnizca canli
        // satirlara baksaydik "cikardim, geri ekledim" akisi kisita carpip
        // uygulamayi cokertirdi - kullanicinin yapacagi en dogal ikinci hareket.
        val mevcut = tripLineDao.findIncludingDeleted(tripId, product.id)
        if (mevcut != null) {
            val guncel = if (mevcut.deletedAt != null) {
                // Mezardan cikar: yeni satir gibi davran ama AYNI id'yi koru.
                // Yeni id uretmek senkronda "silindi" ve "eklendi" olaylarini
                // birbirinden kopuk iki satira baglardi.
                mevcut.copy(
                    deletedAt = null,
                    quantity = adet,
                    checked = false,
                    checkedAt = null,
                    addedByMemberId = memberId,
                    fromSuggestion = oneridenMi,
                    createdAt = saat(),
                )
            } else {
                // Es zaten eklemis: adedi artir, "kim ekledi"yi EZME.
                mevcut.copy(quantity = mevcut.quantity + adet)
            }
            tripLineDao.update(guncel)
            return guncel
        }
        val satir = TripLine(
            id = yeniId(),
            householdId = householdId,
            tripId = tripId,
            productId = product.id,
            quantity = adet,
            unit = product.defaultUnit,
            addedByMemberId = memberId,
            fromSuggestion = oneridenMi,
            createdAt = saat(),
        )
        tripLineDao.insert(satir)
        return satir
    }

    /** checkedAt SAKLANIYOR: reyonda mi evde mi isaretlendi sorusu sonra lazim olacak. */
    suspend fun toggleChecked(satirId: String, isaretli: Boolean) =
        tripLineDao.setChecked(satirId, isaretli, if (isaretli) saat() else null)

    suspend fun remove(satirId: String) = tripLineDao.softDelete(satirId, saat())

    /**
     * Adiyla urun bulur, yoksa olusturur. matchKey uzerinden bakiyor ki
     * "Ekmek" ile "EKMEK" ayri urun olmasin (F2.4).
     */
    suspend fun findOrCreateProduct(
        householdId: String,
        ad: String,
        kategoriId: String,
        birim: String,
    ): Product {
        val anahtar = matchKey(ad)
        productDao.findByMatchKey(householdId, anahtar)?.let { return it }
        val urun = Product(
            id = yeniId(),
            householdId = householdId,
            name = ad,
            matchKey = anahtar,
            categoryId = kategoriId,
            defaultUnit = birim,
            createdAt = saat(),
        )
        productDao.insert(urun)
        return urun
    }
}
