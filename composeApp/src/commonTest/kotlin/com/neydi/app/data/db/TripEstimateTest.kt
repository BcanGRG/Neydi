package com.neydi.app.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.formatEstimate
import com.neydi.app.ui.list.shownMinor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Gezi tahminleri (E18) - gercek sorgu, gercek veritabani.
 *
 * En kritik iddia [aPastTripKeepsThePriceOfItsOwnTime]: gecmis bir gezinin
 * tutari BUGUNKU fiyattan degil, O GUNKU fiyattan hesaplanmali.
 */
class TripEstimateTest {

    private val home = DEFAULT_HOUSEHOLD_ID
    private val day = 24L * 60 * 60 * 1000

    private suspend fun ready(): NeydiDatabase {
        val db = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
            factory = { NeydiDatabaseConstructor.initialize() },
        ).setDriver(BundledSQLiteDriver()).build()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        return db
    }

    private suspend fun trip(db: NeydiDatabase, id: String, closedAt: Long?) =
        db.tripDao().insert(
            Trip(
                id = id, householdId = home, startedAt = 0,
                completedAt = closedAt, createdAt = 0,
            ),
        )

    private suspend fun line(db: NeydiDatabase, tripId: String, productId: String, qty: Double = 1.0) {
        db.productDao().insert(
            Product(
                id = productId, householdId = home, name = productId, matchKey = productId,
                categoryId = "temel-gida", defaultUnit = "adet", createdAt = 0,
            ),
        )
        db.tripLineDao().insert(
            TripLine(
                id = "l-$tripId-$productId", householdId = home, tripId = tripId,
                productId = productId, quantity = qty, unit = "adet",
                addedByMemberId = "m1", createdAt = 0,
            ),
        )
    }

    private suspend fun observe(db: NeydiDatabase, productId: String, minor: Long, at: Long, id: String) =
        db.priceObservationDao().insert(
            PriceObservation(
                id = id, householdId = home, productId = productId, storeId = null,
                unitPriceMinor = minor, observedAt = at, createdAt = at,
            ),
        )

    private suspend fun estimate(db: NeydiDatabase, tripId: String): Long? =
        db.priceObservationDao().observeTripEstimates(home).first()
            .firstOrNull { it.tripId == tripId }
            .shownMinor()

    /**
     * GECMIS GEZI KENDI ZAMANININ FIYATINI TASIYOR.
     *
     * Urun gezi gununde 10 TL, bugun 30 TL. Gezinin tutari **10 TL** olmali:
     * kullanici o gun onu odedi. Bugunku fiyati kullansaydik gecen ayin
     * alisverisi her zamdan sonra biraz daha pahali gorunurdu - hic yasanmamis
     * bir tutar, ve "ne kadar harcadim" sorusuna yalan bir cevap.
     */
    @Test
    fun aPastTripKeepsThePriceOfItsOwnTime() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = 10 * day)
        listOf("a", "b", "c").forEach { line(db, "t1", it) }
        listOf("a", "b", "c").forEachIndexed { i, p ->
            observe(db, p, 1_000, at = 9 * day, id = "eski$i")
            observe(db, p, 3_000, at = 20 * day, id = "yeni$i")
        }

        assertEquals(3_000L, estimate(db, "t1"), "3 urun x 10 TL = 30 TL, bugunku fiyat DEGIL")
    }

    /** Adet carpiliyor: 2 x 10 TL = 20 TL. */
    @Test
    fun quantityMultipliesThePrice() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = 10 * day)
        line(db, "t1", "a", qty = 2.0)
        line(db, "t1", "b")
        line(db, "t1", "c")
        listOf("a", "b", "c").forEachIndexed { i, p -> observe(db, p, 1_000, at = day, id = "o$i") }

        assertEquals(4_000L, estimate(db, "t1"))
    }

    /**
     * ESIGIN ALTINDA TUTAR YOK.
     *
     * On sekiz urunluk bir gezide yalnizca ikisinin fiyati biliniyorsa
     * *"~20 TL"* yazmak yanlis bir guven verir. Esik `EstimatedBasket` ile
     * AYNI sabit - iki yerde iki sayi olsaydi ayni gezi listede tutarli,
     * baslikta tutarsiz gorunurdu.
     */
    @Test
    fun belowTheThresholdNoAmountIsShown() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = 10 * day)
        listOf("a", "b", "c", "d").forEach { line(db, "t1", it) }
        observe(db, "a", 1_000, at = day, id = "o1")
        observe(db, "b", 1_000, at = day, id = "o2")

        assertNull(estimate(db, "t1"), "iki fiyatli urun esigin altinda")
    }

    /** Hicbir urunun fiyati yoksa gezi sonucta HIC gorunmuyor - sifir degil. */
    @Test
    fun aTripWithNoPricesIsAbsentRatherThanZero() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = 10 * day)
        line(db, "t1", "a")

        val rows = db.priceObservationDao().observeTripEstimates(home).first()
        assertTrue(rows.none { it.tripId == "t1" })
        assertNull(estimate(db, "t1"))
    }

    /**
     * GEZIDEN SONRA CEKILEN ETIKET O GEZIYE GIRMIYOR.
     *
     * `observedAt <= completedAt` sartinin ISIRDIGI yer. Sart olmasaydi bugun
     * cekilen bir etiket gecen ayin gezisini yeniden fiyatlandirirdi.
     */
    @Test
    fun anObservationAfterTheTripDoesNotCount() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = 5 * day)
        listOf("a", "b", "c").forEach { line(db, "t1", it) }
        listOf("a", "b", "c").forEachIndexed { i, p -> observe(db, p, 9_999, at = 30 * day, id = "sonra$i") }

        assertNull(estimate(db, "t1"), "gezi kapandiktan sonraki gozlem sayilmamali")
    }

    /** Aktif gezi (kapanmamis) bu sorguda YOK - orada aktif sepet tahmini var. */
    @Test
    fun anOpenTripIsNotInTheHistoryEstimates() = runTest {
        val db = ready()
        trip(db, "acik", closedAt = null)
        listOf("a", "b", "c").forEach { line(db, "acik", it) }
        listOf("a", "b", "c").forEachIndexed { i, p -> observe(db, p, 1_000, at = day, id = "o$i") }

        assertTrue(db.priceObservationDao().observeTripEstimates(home).first().isEmpty())
    }

    /** Iki gezi ayri ayri hesaplaniyor - toplamlar birbirine karismiyor. */
    @Test
    fun tripsAreEstimatedIndependently() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = 10 * day)
        trip(db, "t2", closedAt = 20 * day)
        listOf("a", "b", "c").forEach { line(db, "t1", it) }
        listOf("d", "e", "f").forEach { line(db, "t2", it) }
        listOf("a", "b", "c").forEachIndexed { i, p -> observe(db, p, 1_000, at = day, id = "x$i") }
        listOf("d", "e", "f").forEachIndexed { i, p -> observe(db, p, 5_000, at = day, id = "y$i") }

        assertEquals(3_000L, estimate(db, "t1"))
        assertEquals(15_000L, estimate(db, "t2"))
    }

    /**
     * BICIM HER ZAMAN TILDE ILE ve kurussuz.
     *
     * Uygulamada kesin tutar diye bir veri YOK; iki ondalik hane bir kesinlik
     * iddiasidir ve tildenin soyledigini ayni satirda geri alirdi.
     */
    @Test
    fun theAmountIsAlwaysWrittenWithATilde() {
        assertEquals("~643 TL", formatEstimate(64_250))
        assertEquals("~1.085 TL", formatEstimate(108_540))
    }
}
