package com.neydi.app.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.formatEstimate
import com.neydi.app.ui.list.shownMinor
import com.neydi.app.data.EstimateLine
import com.neydi.app.data.EstimateSource
import com.neydi.app.data.foldBasket
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

    private suspend fun line(
        db: NeydiDatabase,
        tripId: String,
        productId: String,
        qty: Double = 1.0,
        unit: String = "adet",
    ) {
        db.productDao().insert(
            Product(
                id = productId, householdId = home, name = productId, matchKey = productId,
                categoryId = "temel-gida", defaultUnit = "adet", createdAt = 0,
            ),
        )
        db.tripLineDao().insert(
            TripLine(
                id = "l-$tripId-$productId", householdId = home, tripId = tripId,
                productId = productId, quantity = qty, unit = unit,
                addedByMemberId = "m1", createdAt = 0,
            ),
        )
    }

    private suspend fun observe(
        db: NeydiDatabase,
        productId: String,
        minor: Long,
        at: Long,
        id: String,
        storeId: String? = null,
    ) = db.priceObservationDao().insert(
        PriceObservation(
            id = id, householdId = home, productId = productId, storeId = storeId,
            unitPriceMinor = minor, observedAt = at, createdAt = at,
        ),
    )

    /** Zincir testleri icin market - `chain` alani "bim" gibi normalize hali. */
    private suspend fun store(db: NeydiDatabase, id: String, chain: String) =
        db.storeDao().insert(
            Store(id = id, householdId = home, name = chain, chain = chain, createdAt = 0),
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
    // --- AKTIF SEPET (E18'in ekranda gorunen yarisi) ------------------------
    //
    // Bu uc test bir BOSLUGU kapatiyor: yukaridaki sekiz test
    // `observeTripEstimates`i, yani KAPANMIS gezi sorgusunu deniyordu. Ekranin
    // ustundeki "Tahmini sepet" satirini besleyen sorgular AYRI
    // (`observeEstimate` / `observePricedCount` / `observeLineCount`) ve
    // hicbirinin testi yoktu - aktif sepette bir regresyon butun takimdan
    // sessizce gecerdi.
    //
    // UCU TEK SORGUYA INDI (karar 96): `observeEstimateLines` satirlari
    // getiriyor, toplami `foldBasket` kuruyor. Asagidaki iddialar AYNI
    // kaldi - degisen sey nerede hesaplandiklari.

    /** Miktar CARPILIYOR: "4x Makarna" dortle. */
    @Test
    fun theActiveBasketMultipliesByQuantity() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = null)
        line(db, "t1", "Makarna", qty = 4.0)
        observe(db, "Makarna", 1_700, at = day, id = "o1")

        val totals = db.priceObservationDao().observeEstimateLines("t1").first().fold()
        assertEquals(6_800, totals.amountMinor)
    }

    /**
     * KULLANICININ BILDIRDIGI KUSUR, SORGUNUN UCUNDAN: `3 kg Yogurt` x
     * 192,00 TL bir 3 kg'lik kovanin fiyati - carpilmiyor (karar 96, 111).
     *
     * Bu testin degeri saf kuralda degil BORUDA: sorgu `unit`i ve `packSize`i
     * gercekten tasiyor mu. Kural `BasketEstimateRuleTest`te ayrica kilitli.
     */
    @Test
    fun aWeighedLineWithNoPackDropsOutOfTheSum() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = null)
        line(db, "t1", "Yoğurt", qty = 3.0, unit = "kg")
        line(db, "t1", "Makarna"); line(db, "t1", "Süt"); line(db, "t1", "Çay")
        observe(db, "Yoğurt", 19_200, at = day, id = "o1")
        observe(db, "Makarna", 1_700, at = day, id = "o2")
        observe(db, "Süt", 6_250, at = day, id = "o3")
        observe(db, "Çay", 39_900, at = day, id = "o4")

        val totals = db.priceObservationDao().observeEstimateLines("t1").first().fold()
        // Yogurt toplamda YOK - eskiden 3 x 192,00 = 576,00 ekleniyordu.
        assertEquals(47_850, totals.amountMinor)
        // ...ama paydada var (karar 96: "toplamdan duser, PAYDA KALIR").
        assertEquals(3, totals.pricedCount)
        assertEquals(4, totals.totalCount)
    }

    /** Sorgu satirin KULLANICI SECTIGI birimini tasiyor (karar 108). */
    @Test
    fun theEstimateReadsTheUnitTheUserChose() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = null)
        line(db, "t1", "Yoğurt", qty = 2.0, unit = "kg")
        observe(db, "Yoğurt", 19_200, at = day, id = "o1")
        // Kullanici bu satirda "adet" secti: artik tartili degil, sayilan.
        db.tripLineDao().setQuantity("l-t1-Yoğurt", quantity = 2.0, unitOverride = "adet", at = day)

        val totals = db.priceObservationDao().observeEstimateLines("t1").first().fold()
        assertEquals(38_400, totals.amountMinor)
    }

    /**
     * FIYATI BILINMEYEN URUN TOPLAMA GIRMIYOR, ve payda onu SAYIYOR.
     *
     * Ikisi birden, cunku satirin cumlesi ("3 üründen 2 tanesini biliyorum")
     * tam olarak bu ikilinin farkindan cikiyor.
     */
    @Test
    fun anUnpricedProductLeavesTheSumButStaysInTheDenominator() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = null)
        line(db, "t1", "Süt"); line(db, "t1", "Çay"); line(db, "t1", "Ekmek")
        observe(db, "Süt", 6_250, at = day, id = "o1")
        observe(db, "Çay", 39_900, at = day, id = "o2")

        val totals = db.priceObservationDao().observeEstimateLines("t1").first().fold()
        assertEquals(46_150, totals.amountMinor)
        assertEquals(2, totals.pricedCount)
        assertEquals(3, totals.totalCount)
    }

    /**
     * PAY PAYDADAN BUYUK OLAMAZ - "hepsinin fiyatını biliyorum" yalani.
     *
     * Payda bir sure EKRANIN cizdigi satir sayisindan geliyordu, pay ise
     * veritabanindan. Urunu silinmis bir satir ekranda cizilmiyor ama
     * `pricedCount`ta duruyordu; `pricedCount > totalCount` olunca
     * `pricedCount < totalCount` yanlis donuyor ve satir eksik bilgiyi TAM
     * bilgi gibi sunuyordu. Ikisi artik ayni sorgu ailesinden geliyor.
     */
    @Test
    fun thePricedCountNeverExceedsTheLineCount() = runTest {
        val db = ready()
        trip(db, "t1", closedAt = null)
        line(db, "t1", "Süt"); line(db, "t1", "Çay")
        observe(db, "Süt", 6_250, at = day, id = "o1")
        observe(db, "Çay", 39_900, at = day, id = "o2")
        // Urun silindi: ekranda satir cizilmiyor, ama liste satiri duruyor.
        db.productDao().softDelete("Çay", 2 * day)

        val totals = db.priceObservationDao().observeEstimateLines("t1").first().fold()
        assertTrue(
            totals.pricedCount <= totals.totalCount,
            "pay paydayi asarsa sayac yalan yazar",
        )
    }

    /** Sorgu her satirin gozleminin ZINCIRINI tasiyor (karar 95). */
    @Test
    fun theEstimateReadsTheChainOfEachPrice() = runTest {
        val db = ready()
        store(db, "s-bim", "BİM")
        trip(db, "t1", closedAt = null)
        line(db, "t1", "Süt"); line(db, "t1", "Çay"); line(db, "t1", "Makarna")
        observe(db, "Süt", 6_250, at = day, id = "o1", storeId = "s-bim")
        observe(db, "Çay", 39_900, at = day, id = "o2", storeId = "s-bim")
        observe(db, "Makarna", 1_700, at = day, id = "o3", storeId = "s-bim")

        val totals = db.priceObservationDao().observeEstimateLines("t1").first().fold()
        assertEquals(EstimateSource.Chain("BİM"), totals.source)
    }

    /**
     * TEK MARKETSIZ GOZLEM ZINCIR ADINI DUSURUYOR - uctan uca (karar 113).
     *
     * `storeId` nullable ve marketsiz cekim mesru; bu test borunun `null`i
     * gercekten `null` olarak tasidigini kilitliyor. Kural
     * `BasketEstimateRuleTest`te ayrica var, ama `LEFT JOIN`in sessizce bos
     * dizge dondurmesi burada yakalanir.
     */
    @Test
    fun aStorelessObservationBreaksTheChainClaimEndToEnd() = runTest {
        val db = ready()
        store(db, "s-bim", "BİM")
        trip(db, "t1", closedAt = null)
        line(db, "t1", "Süt"); line(db, "t1", "Çay"); line(db, "t1", "Makarna")
        observe(db, "Süt", 6_250, at = day, id = "o1", storeId = "s-bim")
        observe(db, "Çay", 39_900, at = day, id = "o2", storeId = "s-bim")
        observe(db, "Makarna", 1_700, at = day, id = "o3") // marketsiz

        val totals = db.priceObservationDao().observeEstimateLines("t1").first().fold()
        assertEquals(EstimateSource.Mixed, totals.source)
    }

    /** Satirlari kurala baglayan tek yer - testler de ayni yoldan geciyor. */
    private fun List<EstimateRow>.fold() = foldBasket(
        map {
            EstimateLine(
                quantity = it.quantity,
                unit = it.unit,
                unitPriceMinor = it.unitPriceMinor,
                packSize = it.packSize,
                packUnit = it.packUnit,
                chain = it.chain,
                storeName = it.storeName,
            )
        },
    )
}
