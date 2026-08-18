package com.neydi.app.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.ui.components.PriceHint
import com.neydi.app.ui.list.toPriceHint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Satir fiyat ipucu (E16) - GERCEK veritabaniyla, tek sorgudan.
 *
 * Test SQL'i taklit etmiyor, `observeList`i gercekten kosuyor: iki correlated
 * alt sorgu, magaza join'i ve `group_concat` gecmisi burada dogrulanyor. Eslemeyi
 * ayri test etmek mumkundu ama asil risk SQL tarafinda - projeksiyon alanlari
 * yanlis kolona baglanirsa Kotlin tarafi kusursuz calisip yanlis veri gosterir.
 */
class ListPriceHintTest {

    private val home = DEFAULT_HOUSEHOLD_ID
    private val now = 10_000_000_000L
    private val day = 24L * 60 * 60 * 1000

    private suspend fun setup(): Pair<NeydiDatabase, String> {
        val db = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
            factory = { NeydiDatabaseConstructor.initialize() },
        ).setDriver(BundledSQLiteDriver()).build()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.storeDao().insert(
            Store(id = "s-bim", householdId = home, name = "BİM", chain = "bim", createdAt = 0),
        )
        val trip = db.tripDao().let {
            val t = Trip(id = "t1", householdId = home, startedAt = 0, createdAt = 0)
            it.insert(t)
            t
        }
        return db to trip.id
    }

    private suspend fun lineFor(db: NeydiDatabase, tripId: String, name: String): String {
        val repo = ListRepository(
            tripDao = db.tripDao(), tripLineDao = db.tripLineDao(),
            productDao = db.productDao(), clock = { 0 }, newId = { "line-$name" },
        )
        val product = Product(
            id = "p-$name", householdId = home, name = name, matchKey = name.lowercase(),
            categoryId = "temel-gida", defaultUnit = "adet", createdAt = 0,
        )
        db.productDao().insert(product)
        repo.add(householdId = home, tripId = tripId, product = product, memberId = "m1")
        return product.id
    }

    private suspend fun observe(
        db: NeydiDatabase,
        productId: String,
        minor: Long,
        at: Long,
        store: String? = "s-bim",
        packSize: Double? = null,
        packUnit: String? = null,
        id: String = "o-$at",
    ) = db.priceObservationDao().insert(
        PriceObservation(
            id = id, householdId = home, productId = productId, storeId = store,
            unitPriceMinor = minor, packSize = packSize, packUnit = packUnit,
            observedAt = at, createdAt = at,
        ),
    )

    private suspend fun hintFor(db: NeydiDatabase, tripId: String, productId: String): PriceHint =
        db.tripLineDao().observeList(tripId).first()
            .single { it.productId == productId }
            .toPriceHint(now)

    /** Gozlemi olmayan urunde ikinci satir CIZILMEZ. */
    @Test
    fun aProductWithNoObservationsHasNoHint() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Tuz")
        assertEquals(PriceHint.None, hintFor(db, trip, p))
    }

    /**
     * TEK GOZLEM: yuzde YOK.
     *
     * Neyle karsilastirilacagi olmadan yuzde gostermek uydurma olurdu. Market
     * adi magaza join'inden geliyor - projeksiyonun en kolay yanlis baglanan
     * alani, cunku alt sorgunun ICINDEKI join'den cikiyor.
     */
    @Test
    fun oneObservationYieldsSingleWithStoreAndAge() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Yoğurt")
        observe(db, p, 3_850, at = now - 12 * day)

        val hint = assertIs<PriceHint.Single>(hintFor(db, trip, p))
        assertEquals("BİM", hint.store)
        assertEquals(12, hint.daysAgo)
        assertEquals("38,50", hint.price)
    }

    /** Market secilmemis gozlemde satir yine ciziliyor, market yerine isaret. */
    @Test
    fun anObservationWithoutAStoreStillDrawsALine() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Ekmek")
        observe(db, p, 1_500, at = now - day, store = null)

        val hint = assertIs<PriceHint.Single>(hintFor(db, trip, p))
        assertEquals("market yok", hint.store)
    }

    /**
     * IKI GOZLEM, AMBALAJ AYNI: trend + yuzde.
     *
     * `deltaPercent` ONCEKI fiyata gore: 38,50 -> 42,35 %10 artis.
     */
    @Test
    fun twoObservationsYieldATrend() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Süt")
        observe(db, p, 3_850, at = now - 20 * day, id = "a")
        observe(db, p, 4_235, at = now - 2 * day, id = "b")

        val hint = assertIs<PriceHint.Trend>(hintFor(db, trip, p))
        assertEquals("38,50", hint.from)
        assertEquals("42,35", hint.to)
        assertEquals(10, hint.deltaPercent)
        assertTrue(hint.rising)
    }

    /** Fiyat dususunde ok asagi - yuzde MUTLAK deger. */
    @Test
    fun aFallingPriceIsNotANegativePercent() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Peynir")
        observe(db, p, 10_000, at = now - 9 * day, id = "a")
        observe(db, p, 9_000, at = now - day, id = "b")

        val hint = assertIs<PriceHint.Trend>(hintFor(db, trip, p))
        assertEquals(10, hint.deltaPercent)
        assertTrue(!hint.rising)
    }

    /**
     * AMBALAJ DEGISTIYSE TREND BASTIRILIYOR.
     *
     * 900 gr -> 800 gr AYNI fiyata: bu bir fiyat dususu degil, gizli zam.
     * Trend dali secilseydi yesil bir asagi ok cizerdi ve gercegin TERSINI
     * soylerdi.
     */
    @Test
    fun aShrunkPackSuppressesTheTrend() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Cips")
        observe(db, p, 5_000, at = now - 30 * day, packSize = 900.0, packUnit = "gr", id = "a")
        observe(db, p, 5_000, at = now - day, packSize = 800.0, packUnit = "gr", id = "b")

        val hint = assertIs<PriceHint.PackChanged>(hintFor(db, trip, p))
        assertEquals("900 gr", hint.fromPack)
        assertEquals("800 gr", hint.toPack)
    }

    /**
     * AMBALAJI BILINMEYEN gozlem "degisti" SAYILMIYOR.
     *
     * `null` "ayni degil" demek degil, "bilmiyorum" demek - ve bilmediginden
     * zam cikarmak uydurma olurdu. Etiketlerin cogunda gramaj okunamiyor
     * (`docs/18`), yani bu dal nadir degil.
     */
    @Test
    fun anUnknownPackIsNotAPackChange() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Zeytin")
        observe(db, p, 5_000, at = now - 30 * day, packSize = 900.0, packUnit = "gr", id = "a")
        observe(db, p, 6_000, at = now - day, id = "b")

        assertIs<PriceHint.Trend>(hintFor(db, trip, p))
    }

    /**
     * SPARKLINE GECMISI ESKIDEN YENIYE.
     *
     * SQL yeniden eskiye siraliyor (`ORDER BY observedAt DESC`), sparkline ise
     * soldan saga ZAMAN okuyor. Ters cevrilmezse yukselen bir fiyat grafikte
     * duser gorunurdu - sessiz ve tam ters bir yalan.
     */
    @Test
    fun theSparklineRunsOldestToNewest() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Un")
        observe(db, p, 1_000, at = now - 30 * day, id = "a")
        observe(db, p, 2_000, at = now - 20 * day, id = "b")
        observe(db, p, 3_000, at = now - 10 * day, id = "c")

        val hint = assertIs<PriceHint.Trend>(hintFor(db, trip, p))
        assertEquals(listOf(1_000f, 2_000f, 3_000f), hint.history)
    }

    /** Gecmis SEKIZ gozlemle sinirli - sparkline'in cizebildigi kadar. */
    @Test
    fun theHistoryIsCappedAtEight() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Pirinç")
        repeat(12) { i -> observe(db, p, (i + 1) * 1_000L, at = now - (12 - i) * day, id = "o$i") }

        val hint = assertIs<PriceHint.Trend>(hintFor(db, trip, p))
        assertEquals(8, hint.history.size)
        assertEquals(12_000f, hint.history.last(), "en yeni gozlem sonda olmali")
    }

    /**
     * SILINMIS GOZLEM IPUCUNA GIRMIYOR.
     *
     * `deletedAt IS NULL` her alt sorguda ayri ayri yaziliyor ve biri
     * unutulursa silinen bir fiyat sessizce geri gelirdi.
     */
    @Test
    fun aDeletedObservationIsInvisible() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Bal")
        observe(db, p, 5_000, at = now - 5 * day, id = "a")
        db.priceObservationDao().insert(
            PriceObservation(
                id = "b", householdId = home, productId = p, storeId = "s-bim",
                unitPriceMinor = 9_999, observedAt = now - day, createdAt = now - day,
                deletedAt = now,
            ),
        )

        val hint = assertIs<PriceHint.Single>(hintFor(db, trip, p))
        assertEquals("50,00", hint.price)
    }

    /**
     * BASKA URUNUN GOZLEMI SIZMIYOR.
     *
     * Alt sorgular `po.productId = p.id` ile bagli; bag kopsa liste butun
     * satirlarda AYNI fiyati gosterirdi - gorunuste calisan, tamamen yanlis
     * bir ekran.
     */
    @Test
    fun observationsDoNotLeakBetweenProducts() = runTest {
        val (db, trip) = setup()
        val a = lineFor(db, trip, "Kahve")
        val b = lineFor(db, trip, "Çay")
        observe(db, a, 7_500, at = now - day, id = "a")

        assertIs<PriceHint.Single>(hintFor(db, trip, a))
        assertEquals(PriceHint.None, hintFor(db, trip, b))
    }
}
