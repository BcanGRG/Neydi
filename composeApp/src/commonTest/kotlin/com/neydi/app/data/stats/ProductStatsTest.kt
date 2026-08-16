package com.neydi.app.data.stats

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.repo.ListRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Bir gun, millis. Testlerde okunabilirlik icin. */
private const val DAY = 86_400_000L

/**
 * `ProductStats` hesabi (F6.1).
 *
 * KARAR (E10'da guncellendi): istatistigin tek kaynagi **isaretlenmis liste
 * satirlari**. Fis donemi ikinci bir kol daha okuyordu (eslesmis
 * `receipt_line`) ve gerekcesi kullanicinin *yazmayi unuttugu* urunleri de
 * saymakti; o kol kaynagiyla birlikte silindi.
 *
 * Tekillestirme (productId, tripId) YINE SART: ayni urun bir geziye iki satir
 * olarak girebilir ve bu BIR alistir.
 */
class ProductStatsTest {

    private val home = DEFAULT_HOUSEHOLD_ID
    private val me = "m1"
    private var now = 0L

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            productDao = db.productDao(),
            clock = { now },
            newId = { "id-${++n}" },
        )
    }

    private fun rebuilder(db: NeydiDatabase) =
        ProductStatsRebuilder(db.productStatsDao(), clock = { now })

    private suspend fun prepare(db: NeydiDatabase) {
        db.bootstrap(newId = { "seed" }, clock = { 0L })
    }

    /**
     * Bir alisveris turu: urunu ekle, isaretle, geziyi [at] aninda kapat.
     *
     * `at` gezinin KAPANIS ani - istatistigin kullandigi damga bu, satirin
     * `checkedAt`'i degil.
     */
    private suspend fun sepetKapat(
        db: NeydiDatabase,
        r: ListRepository,
        at: Long,
        vararg names: String,
    ): String {
        now = at
        val trip = r.openOrGetActiveTrip(home, me)
        names.forEach { name ->
            val product = r.findOrCreateProduct(home, name, "temel-gida", "adet")
            r.add(home, trip.id, product, memberId = me)
            val row = db.tripLineDao().find(trip.id, product.id)!!
            r.setTaken(row.id, true)
        }
        r.closeTrip(trip.id, memberId = me)
        return trip.id
    }

    // FIS YARDIMCISI VE DORT FIS TESTI E10'DA OLDU. `purchaseEvents` artik
    // tek kaynakli (isaretlenmis liste satirlari); fisten gelen alim kolu
    // kaynagiyla birlikte silindi. Kaybedilen davranis DAO'nun KDoc'unda
    // kayitli: listeye hic yazilmadan alinan urun artik sayilmiyor.

    // --- Sayim -------------------------------------------------------------

    @Test
    fun countsCheckedListRows() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        sepetKapat(db, r, 10 * DAY, "Ekmek")
        sepetKapat(db, r, 20 * DAY, "Ekmek")

        rebuilder(db).rebuild(home)

        val ekmek = db.productDao().findByMatchKey(home, "ekmek")!!
        val stats = db.productStatsDao().byProduct(ekmek.id)!!
        assertEquals(2, stats.purchaseCount)
        assertEquals(20 * DAY, stats.lastPurchasedAt)
    }

    /** ISARETLENMEMIS satir alim DEGIL: Bitir ekranindan geri alinmis olabilir. */
    @Test
    fun uncheckedRowsAreNotPurchases() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        now = 10 * DAY
        val trip = r.openOrGetActiveTrip(home, me)
        val product = r.findOrCreateProduct(home, "Havyar", "temel-gida", "adet")
        r.add(home, trip.id, product, memberId = me)
        r.closeTrip(trip.id, memberId = me)

        rebuilder(db).rebuild(home)

        assertNull(db.productStatsDao().byProduct(product.id))
    }

    /** ACIK gezi alim degil: henuz alinmadi. */
    @Test
    fun openTripIsNotCounted() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        now = 10 * DAY
        val trip = r.openOrGetActiveTrip(home, me)
        val product = r.findOrCreateProduct(home, "Ekmek", "temel-gida", "adet")
        r.add(home, trip.id, product, memberId = me)
        val row = db.tripLineDao().find(trip.id, product.id)!!
        r.setTaken(row.id, true)

        rebuilder(db).rebuild(home)

        assertNull(db.productStatsDao().byProduct(product.id))
    }

    // --- Medyan ------------------------------------------------------------

    /** Iki alim (tek aralik) YETMEZ: tek ornegin medyani o ornegin kendisidir. */
    @Test
    fun medianIsNullWithTwoPurchases() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        sepetKapat(db, r, 10 * DAY, "Ekmek")
        sepetKapat(db, r, 20 * DAY, "Ekmek")

        rebuilder(db).rebuild(home)

        val ekmek = db.productDao().findByMatchKey(home, "ekmek")!!
        assertNull(db.productStatsDao().byProduct(ekmek.id)!!.medianIntervalDays)
    }

    @Test
    fun medianIsComputedFromThreePurchases() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        sepetKapat(db, r, 0 * DAY, "Ekmek")
        sepetKapat(db, r, 10 * DAY, "Ekmek")
        sepetKapat(db, r, 20 * DAY, "Ekmek")

        rebuilder(db).rebuild(home)

        val ekmek = db.productDao().findByMatchKey(home, "ekmek")!!
        assertEquals(10, db.productStatsDao().byProduct(ekmek.id)!!.medianIntervalDays)
    }

    /**
     * MEDYAN, ORTALAMA DEGIL - ve fark tam olarak burada gorunuyor.
     *
     * Araliklar 10, 10, 40 gun: medyan **10**, ortalama **20**. Bir kez 40 gun
     * unutmak ortalamayi ikiye katlayip "10 gunde bir aliyoruz" gercegini
     * gizlerdi.
     */
    @Test
    fun medianResistsOneForgottenGap() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        sepetKapat(db, r, 0 * DAY, "Ekmek")
        sepetKapat(db, r, 10 * DAY, "Ekmek")
        sepetKapat(db, r, 20 * DAY, "Ekmek")
        sepetKapat(db, r, 60 * DAY, "Ekmek")

        rebuilder(db).rebuild(home)

        val ekmek = db.productDao().findByMatchKey(home, "ekmek")!!
        assertEquals(10, db.productStatsDao().byProduct(ekmek.id)!!.medianIntervalDays)
    }

    // --- Yeniden kurulum ---------------------------------------------------

    /** TAM YENIDEN KURULUM: iki kez cagirmak sayilari ikiye katlamiyor. */
    @Test
    fun rebuildIsIdempotent() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        sepetKapat(db, r, 10 * DAY, "Ekmek")
        sepetKapat(db, r, 20 * DAY, "Ekmek")

        rebuilder(db).rebuild(home)
        rebuilder(db).rebuild(home)

        val ekmek = db.productDao().findByMatchKey(home, "ekmek")!!
        assertEquals(2, db.productStatsDao().byProduct(ekmek.id)!!.purchaseCount)
        assertEquals(1, db.productStatsDao().observeAll(home).first().size)
    }

    /**
     * `muAdjust` YENIDEN KURULUMDA KORUNUYOR.
     *
     * O turetilmis veri degil, kullanicinin/motorun duzeltmesi. Sifirlanmasi
     * yanlis ogrenmeyi geri almanin tek yolunu butun istatistigi silmek yapardi.
     */
    @Test
    fun rebuildPreservesMuAdjust() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        sepetKapat(db, r, 10 * DAY, "Ekmek")
        rebuilder(db).rebuild(home)
        val ekmek = db.productDao().findByMatchKey(home, "ekmek")!!
        val stats = db.productStatsDao().byProduct(ekmek.id)!!
        db.productStatsDao().insertAll(listOf(stats.copy(muAdjust = 1.5)))

        rebuilder(db).rebuild(home)

        assertEquals(1.5, db.productStatsDao().byProduct(ekmek.id)!!.muAdjust)
    }

    /** Hic alim yoksa tablo bos - uydurma satir yazilmiyor. */
    @Test
    fun noPurchasesMeansNoRows() = runTest {
        val db = db(); prepare(db)
        assertEquals(0, rebuilder(db).rebuild(home))
    }

    // --- Saf medyan fonksiyonu --------------------------------------------

    @Test
    fun medianOfEvenIntervalCountAveragesTheMiddleTwo() {
        // Araliklar: 4, 6, 8, 10 -> ortadakiler 6 ve 8 -> 7
        val times = listOf(0L, 4 * DAY, 10 * DAY, 18 * DAY, 28 * DAY)
        assertEquals(7, medianIntervalDays(times))
    }

    /**
     * Ayni gun iki alim 0 gunluk bir aralik uretiyor ve o aralik ATILMIYOR -
     * gerceklesmis bir davranis (iki ayri fisle ayni gun alisveris).
     *
     * Araliklar [0, 10] -> cift sayida, ortadaki ikisinin ortalamasi = **5**.
     * Sifir atilsaydi tek aralik [10] kalir ve sonuc **10** olurdu; yani 5
     * gormek sifirin sayildiginin kanitidir.
     */
    @Test
    fun sameDayPurchaseKeepsItsZeroInterval() {
        assertEquals(5, medianIntervalDays(listOf(0L, 0L, 10 * DAY)))
        // Karsilastirma: sifir olmadan ayni son alim.
        assertEquals(10, medianIntervalDays(listOf(0L, 10 * DAY, 20 * DAY)))
    }
}
