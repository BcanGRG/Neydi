package com.neydi.app.data.stats

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptLine
import com.neydi.app.data.db.ReceiptStatus
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
 * KARAR: istatistik **hem** `trip_line` **hem** eslesmis `receipt_line`
 * okuyor. Yalnizca listeyi okumak, tam olarak kullanicinin **yazmayi unuttugu**
 * urunleri saymamak demekti - yani Faz 4'un var olma sebebini es gecmek.
 *
 * O kararin bedeli tekillestirme: ayni urun hem listede isaretli hem fiste
 * eslesmis olabilir ve bu BIR alistir.
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
            receiptDao = db.receiptDao(),
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

    /** Kapali bir geziye, urune BAGLANMIS bir fis satiri ekler. */
    private suspend fun fisSatiriEkle(
        db: NeydiDatabase,
        tripId: String,
        productId: String,
        receiptDate: Long?,
        id: String,
    ) {
        db.receiptDao().insert(
            Receipt(
                id = "r-$id", householdId = home, tripId = tripId,
                imagePath = "/tmp/$id.jpg", capturedAt = 0, receiptDate = receiptDate,
                status = ReceiptStatus.VERIFIED, createdAt = 0,
            ),
        )
        db.receiptLineDao().insertAll(
            listOf(
                ReceiptLine(
                    id = "rl-$id", householdId = home, receiptId = "r-$id",
                    rawText = "X", rawTextNormalized = "x",
                    unitPriceMinor = null, lineTotalMinor = 1000,
                    matchedProductId = productId, needsReview = false, createdAt = 0,
                ),
            ),
        )
    }

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

    // --- Fisten gelen alimlar (kullanicinin kararı) -------------------------

    /**
     * LISTEYE YAZILMAMIS ama fiste olan urun SAYILIYOR.
     *
     * Bu, kararin butun sebebi: kullanicinin unuttugu ekmek de bir alim.
     */
    @Test
    fun receiptOnlyPurchaseIsCounted() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = sepetKapat(db, r, 10 * DAY, "Ekmek")
        val sut = r.findOrCreateProduct(home, "Süt", "temel-gida", "adet")
        fisSatiriEkle(db, trip, sut.id, receiptDate = 10 * DAY, id = "1")

        rebuilder(db).rebuild(home)

        assertEquals(1, db.productStatsDao().byProduct(sut.id)!!.purchaseCount)
    }

    /**
     * AYNI URUN HEM LISTEDE HEM FISTE = **BIR** ALIM.
     *
     * Tekillestirilmezse `purchaseCount` ikiye katlanir ve
     * `medianIntervalDays` yariya duser - yani uygulama her seyi iki kat sik
     * onermeye baslar. Sessiz, yavas ve geri alinmasi zor.
     */
    @Test
    fun listAndReceiptInSameTripCountOnce() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = sepetKapat(db, r, 10 * DAY, "Ekmek")
        val ekmek = db.productDao().findByMatchKey(home, "ekmek")!!
        fisSatiriEkle(db, trip, ekmek.id, receiptDate = 10 * DAY, id = "1")

        rebuilder(db).rebuild(home)

        assertEquals(1, db.productStatsDao().byProduct(ekmek.id)!!.purchaseCount)
    }

    /** Onaya dusmus fis satiri alim DEGIL: yanlis esleme gecmisi kirletir. */
    @Test
    fun unconfirmedReceiptLineIsNotAPurchase() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = sepetKapat(db, r, 10 * DAY, "Ekmek")
        val sut = r.findOrCreateProduct(home, "Süt", "temel-gida", "adet")
        db.receiptDao().insert(
            Receipt(
                id = "r-x", householdId = home, tripId = trip, imagePath = "/tmp/x.jpg",
                capturedAt = 0, status = ReceiptStatus.MISMATCHED, createdAt = 0,
            ),
        )
        db.receiptLineDao().insertAll(
            listOf(
                ReceiptLine(
                    id = "rl-x", householdId = home, receiptId = "r-x",
                    rawText = "X", rawTextNormalized = "x", unitPriceMinor = null,
                    lineTotalMinor = 1000, matchedProductId = sut.id,
                    needsReview = true, createdAt = 0,
                ),
            ),
        )

        rebuilder(db).rebuild(home)

        assertNull(db.productStatsDao().byProduct(sut.id))
    }

    /** Fisin BASILI tarihi kazaniyor: satin almanin gerceklestigi an o. */
    @Test
    fun printedReceiptDateWinsOverCloseTime() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = sepetKapat(db, r, 20 * DAY, "Ekmek")
        val sut = r.findOrCreateProduct(home, "Süt", "temel-gida", "adet")
        fisSatiriEkle(db, trip, sut.id, receiptDate = 18 * DAY, id = "1")

        rebuilder(db).rebuild(home)

        assertEquals(18 * DAY, db.productStatsDao().byProduct(sut.id)!!.lastPurchasedAt)
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
