package com.neydi.app.data.suggest

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.ProductStats
import com.neydi.app.data.db.TakeOutcome
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.stats.ProductStatsRebuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val DAY = 86_400_000L

/**
 * Skor formulu ve oneri ureticisi (F6.2).
 *
 * Formul testleri SAF - veritabani yok, cunku formulun kendisi saf ve butun
 * agirliklari tek dosyada. Uretici testleri gercek Room ile: "aktif listede
 * olan onerilmez" gibi kurallar sorgulara bagli.
 */
class SuggestionEngineTest {

    private val home = DEFAULT_HOUSEHOLD_ID
    private val me = "m1"
    private var now = 0L

    private fun stats(
        median: Int? = 10,
        count: Int = 5,
        mu: Double = 0.0,
    ) = ProductStats(
        productId = "p1",
        householdId = home,
        purchaseCount = count,
        lastPurchasedAt = 0L,
        medianIntervalDays = median,
        muAdjust = mu,
        updatedAt = 0L,
    )

    // --- Saf formul ----------------------------------------------------------

    /** Tempo yoksa skor YOK: uydurmak yerine "bilmiyorum". */
    @Test
    fun noMedianMeansNoScore() {
        assertNull(score(stats(median = null), daysSince = 30, lastOutcome = null))
        assertNull(score(stats(median = 0), daysSince = 30, lastOutcome = null))
    }

    /** Gecikmislik ORANA gore: 3 gunluk ekmegin 4. gunu, 30 gunluk deterjanin 20. gununden acil. */
    @Test
    fun overdueIsRelativeToCadence(): Unit {
        val bread = score(stats(median = 3, count = 5), daysSince = 4, lastOutcome = null)!!
        val detergent = score(stats(median = 30, count = 5), daysSince = 20, lastOutcome = null)!!
        assertTrue(bread > detergent, "ekmek=$bread deterjan=$detergent")
    }

    /**
     * SIKLIK ESITLIGI BOZAR, GOVDEYI DEVIREMEZ.
     *
     * 100 alimlik bir urunun frekans katkisi ln(101)/10 = 0,46 - yani vakti
     * gelmemis (oran 0,3) cok-alinan bir urun, vakti gelmis (oran 1,0) az
     * alinan bir urunu gecemiyor.
     */
    @Test
    fun frequencyCannotBeatOverdue() {
        val frequentButEarly = score(stats(median = 10, count = 100), daysSince = 3, lastOutcome = null)!!
        val rareButDue = score(stats(median = 10, count = 2), daysSince = 10, lastOutcome = null)!!
        assertTrue(rareButDue > frequentButEarly)
    }

    /** "Unuttum" yukseltir, "gerekmedi" bastirir - ve ikisi AYNI DEGIL (F4.12'nin sebebi). */
    @Test
    fun forgottenBoostsAndNotNeededDamps() {
        val base = score(stats(), daysSince = 10, lastOutcome = null)!!
        val forgotten = score(stats(), daysSince = 10, lastOutcome = TakeOutcome.FORGOTTEN)!!
        val notNeeded = score(stats(), daysSince = 10, lastOutcome = TakeOutcome.NOT_NEEDED)!!
        assertEquals(base + 0.5, forgotten, 1e-9)
        assertEquals(base - 1.0, notNeeded, 1e-9)
        assertTrue(forgotten > base && base > notNeeded)
    }

    /** "Gerekmedi" bir tempo boyunca susturur: -1,0 tam bir gecikme oranina bedel. */
    @Test
    fun notNeededSilencesForOneCadence() {
        // 10 gunluk tempoda 10. gun: normalde tam esikte olurdu.
        val silenced = score(stats(), daysSince = 10, lastOutcome = TakeOutcome.NOT_NEEDED)!!
        assertTrue(silenced < 0.85, "susturulmamis: $silenced")
        // Bir tempo daha gecince (20. gun) yeniden esige geliyor.
        val recovered = score(stats(), daysSince = 20, lastOutcome = TakeOutcome.NOT_NEEDED)!!
        assertTrue(recovered >= 0.85, "geri gelmedi: $recovered")
    }

    /** muAdjust oldugu gibi ekleniyor - denetlenebilir tek duzeltme kanali. */
    @Test
    fun muAdjustShiftsTheScore() {
        val base = score(stats(), daysSince = 10, lastOutcome = null)!!
        val nudged = score(stats(mu = 0.3), daysSince = 10, lastOutcome = null)!!
        assertEquals(base + 0.3, nudged, 1e-9)
    }

    // --- Uretici (gercek Room) ----------------------------------------------

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(), tripLineDao = db.tripLineDao(),
            receiptDao = db.receiptDao(), productDao = db.productDao(),
            clock = { now }, newId = { "id-${++n}" },
        )
    }

    private fun engine(db: NeydiDatabase) = SuggestionEngine(
        statsDao = db.productStatsDao(), productDao = db.productDao(),
        tripDao = db.tripDao(), tripLineDao = db.tripLineDao(), clock = { now },
    )

    /** Uc alisverislik gecmis kurar: urun 0/10/20. gunlerde alinmis olur. */
    private suspend fun threePurchases(db: NeydiDatabase, r: ListRepository, name: String) {
        repeat(3) { i ->
            now = i * 10 * DAY
            val trip = r.openOrGetActiveTrip(home, me)
            val product = r.findOrCreateProduct(home, name, "temel-gida", "adet")
            r.add(home, trip.id, product, memberId = me)
            r.setTaken(db.tripLineDao().find(trip.id, product.id)!!.id, true)
            r.closeTrip(trip.id, memberId = me)
        }
        ProductStatsRebuilder(db.productStatsDao(), clock = { now }).rebuild(home)
    }

    /** Vakti gelen urun oneriliyor, gerekce verisiyle. */
    @Test
    fun dueProductIsSuggested() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val r = repo(db)
        threePurchases(db, r, "Yumurta")

        now = 34 * DAY // son alim 20. gun, 14 gun gecti, tempo 10
        val suggestions = engine(db).suggestions(home)

        assertEquals(1, suggestions.size)
        with(suggestions.single()) {
            assertEquals("Yumurta", this.name)
            assertEquals(14, daysSince)
            assertEquals(10, intervalDays)
        }
    }

    /** Vakti GELMEMIS urun onerilmiyor - bos serit, alakasiz seritten iyi. */
    @Test
    fun earlyProductIsNotSuggested() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val r = repo(db)
        threePurchases(db, r, "Yumurta")

        now = 22 * DAY // son alimdan 2 gun sonra, tempo 10
        assertEquals(emptyList(), engine(db).suggestions(home))
    }

    /** AKTIF LISTEDE OLAN ONERILMEZ: kullanici zaten yazmis. */
    @Test
    fun productAlreadyOnTheListIsNotSuggested() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val r = repo(db)
        threePurchases(db, r, "Yumurta")

        now = 34 * DAY
        val trip = r.openOrGetActiveTrip(home, me)
        val product = db.productDao().findByMatchKey(home, "yumurta")!!
        r.add(home, trip.id, product, memberId = me)

        assertEquals(emptyList(), engine(db).suggestions(home))
    }

    /** En fazla 5 oneri - tasarimin siniri. */
    @Test
    fun suggestionsAreCappedAtFive() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val r = repo(db)
        repeat(8) { i -> threePurchases(db, r, "Urun $i") }

        now = 60 * DAY // hepsi fazlasiyla gecikmis
        assertEquals(5, engine(db).suggestions(home).size)
    }

    /** Skoru yuksek olan once: liste mutlak degil GORECE aciliyete gore. */
    @Test
    fun suggestionsAreSortedByScore() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val r = repo(db)
        threePurchases(db, r, "Yumurta")   // son alim 20*DAY
        threePurchases(db, r, "Ekmek")     // ayni tempo, ayni gunler

        // Ekmek'i bir kez daha al: son alimi 30. gun olsun - daha az gecikmis.
        now = 30 * DAY
        val trip = r.openOrGetActiveTrip(home, me)
        val bread = db.productDao().findByMatchKey(home, "ekmek")!!
        r.add(home, trip.id, bread, memberId = me)
        r.setTaken(db.tripLineDao().find(trip.id, bread.id)!!.id, true)
        r.closeTrip(trip.id, memberId = me)
        ProductStatsRebuilder(db.productStatsDao(), clock = { now }).rebuild(home)

        now = 45 * DAY
        val names = engine(db).suggestions(home).map { it.name }
        assertEquals(listOf("Yumurta", "Ekmek"), names)
    }

    // --- Gerekce metni (F6.3) -----------------------------------------------

    /** Maketlerden birebir: "14 gun oldu". */
    @Test
    fun reasonTextShowsDaysSince() {
        val s = Suggestion("p", "Yumurta", 1.5, daysSince = 14, intervalDays = 10, forgottenLastTrip = false)
        assertEquals("14 gün oldu", s.reasonText())
    }

    /** Kullanicinin kendi beyani gun sayisini EZIYOR. */
    @Test
    fun forgottenBeatsDayCount() {
        val s = Suggestion("p", "Ekmek", 1.5, daysSince = 4, intervalDays = 3, forgottenLastTrip = true)
        assertEquals("geçen sefer unutmuştun", s.reasonText())
    }

    @Test
    fun sameDayAndYesterdayReadNaturally() {
        assertEquals("bugün almıştın", Suggestion("p", "X", 2.0, 0, 10, false).reasonText())
        assertEquals("dün almıştın", Suggestion("p", "X", 2.0, 1, 10, false).reasonText())
    }
}
