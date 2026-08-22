package com.neydi.app.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.stats.ProductStatsRebuilder
import com.neydi.app.data.suggest.SuggestionEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val DAY = 86_400_000L

/**
 * "Bunu onerme" (F6.5) - engelin yazilmasi, motoru susturmasi ve GERI ALINMASI.
 *
 * ## Neden bir bastirma testinin ucuncu yarisi "geri alma"
 *
 * Tasarim bu ozelligi *"kara delik olmamali"* diye sartlandirdi: kalici bir
 * reddin **her satirinin tek dokunusla geri alinabilir** olmasi, ozelligin
 * kendisi kadar ozelligin parcasi. Yalnizca "engellenen onerilmiyor"u
 * dogrulayan bir test, geri donusu olmayan bir bastirmayi da yesil sayardi.
 *
 * ## Silme YOK kurali burada olculuyor
 *
 * [SuggestionBlockDao.unblock] satiri SILMIYOR, `unblockedAt` yaziyor. Sebep
 * gelecege ait: uc-vurus otomatik bastirma yazildiginda motor, ayni urunu geri
 * engellemeden once kullanicinin bu kararini gorebilmeli.
 * [unblockingKeepsTheRowSoTheDecisionSurvives] tam bunu kilitliyor.
 */
class SuggestionBlockTest {

    private val home = DEFAULT_HOUSEHOLD_ID
    private val me = "m1"
    private var now = 0L
    private var n = 0

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase) = ListRepository(
        tripDao = db.tripDao(), tripLineDao = db.tripLineDao(),
        productDao = db.productDao(), clock = { now }, newId = { "id-${++n}" },
    )

    private fun engine(db: NeydiDatabase) = SuggestionEngine(
        statsDao = db.productStatsDao(), productDao = db.productDao(),
        tripDao = db.tripDao(), tripLineDao = db.tripLineDao(),
        blockDao = db.suggestionBlockDao(), clock = { now },
    )

    /** Uc alisverislik gecmis: urun 0/10/20. gunlerde alinmis olur. */
    private suspend fun threePurchases(db: NeydiDatabase, r: ListRepository, name: String): String {
        var productId = ""
        repeat(3) { i ->
            now = i * 10 * DAY
            val trip = r.openOrGetActiveTrip(home, me)
            val product = r.findOrCreateProduct(home, name, "temel-gida", "adet")
            productId = product.id
            r.add(home, trip.id, product, memberId = me)
            r.setTaken(db.tripLineDao().find(trip.id, product.id)!!.id, true)
            r.closeTrip(trip.id, memberId = me)
        }
        ProductStatsRebuilder(db.productStatsDao(), clock = { now }).rebuild(home)
        return productId
    }

    private suspend fun block(db: NeydiDatabase, productId: String, at: Long = now) =
        db.suggestionBlockDao().upsert(
            SuggestionBlock(
                id = "b-$productId-$at", householdId = home, productId = productId,
                source = BlockSource.MANUAL, blockedAt = at, createdAt = at,
            ),
        )

    /**
     * ENGELLENEN URUN ONERILMIYOR - ve baslangicta ONERILIYORDU.
     *
     * Iki iddia birlikte, cunku yalnizca ikinci yarisi (`emptyList`) dogrulanan
     * bir test, oneriyi bastiran seyin engel DEGIL baska bir kural oldugu
     * halde de yesil kalirdi. Once oneriyi gormek, engelin gercekten bir sey
     * degistirdigini kanitliyor.
     */
    @Test
    fun aBlockedProductIsNotSuggested() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val p = threePurchases(db, repo(db), "Yumurta")

        now = 34 * DAY
        assertEquals(listOf("Yumurta"), engine(db).suggestions(home).map { it.name })

        block(db, p)
        assertEquals(emptyList(), engine(db).suggestions(home))
    }

    /** Engel kaldirilinca urun GERI GELIYOR - "kara delik olmamali". */
    @Test
    fun unblockingBringsTheSuggestionBack() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val p = threePurchases(db, repo(db), "Yumurta")
        now = 34 * DAY
        block(db, p)
        assertEquals(emptyList(), engine(db).suggestions(home))

        db.suggestionBlockDao().unblock(home, p, now)
        assertEquals(listOf("Yumurta"), engine(db).suggestions(home).map { it.name })
    }

    /**
     * ENGEL KALKINCA SATIR DURUYOR - silinmiyor.
     *
     * Kullanicinin karari kayitta kaliyor ki uc-vurus otomatik bastirma
     * yazildiginda motor onu gorebilsin. Satir silinseydi ayni urun bir
     * sonraki turda sessizce geri engellenirdi.
     */
    @Test
    fun unblockingKeepsTheRowSoTheDecisionSurvives() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val p = threePurchases(db, repo(db), "Yumurta")
        now = 34 * DAY
        block(db, p, at = 30 * DAY)
        db.suggestionBlockDao().unblock(home, p, now)

        // Yururlukte degil...
        assertFalse(db.suggestionBlockDao().isBlocked(home, p))
        // ...ama satir yerinde ve tarihi duruyor.
        val rows = db.suggestionBlockDao().blockHistory(home)
        assertEquals(1, rows.size)
        assertEquals(30 * DAY, rows.single().blockedAt)
        assertEquals(now, rows.single().unblockedAt)
    }

    /**
     * IKINCI KEZ KALDIRMA TARIHI TAZELEMIYOR.
     *
     * `unblockedAt` kullanicinin kararinin ANI. Ikinci bir "geri al" cagrisi
     * onu bugune cekseydi, kayit kullanicinin yapmadigi bir dokunusu anlatirdi.
     */
    @Test
    fun unblockingTwiceDoesNotRewriteTheDate() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val p = threePurchases(db, repo(db), "Yumurta")
        block(db, p, at = 30 * DAY)
        db.suggestionBlockDao().unblock(home, p, 31 * DAY)
        db.suggestionBlockDao().unblock(home, p, 99 * DAY)

        assertEquals(31 * DAY, db.suggestionBlockDao().blockHistory(home).single().unblockedAt)
    }

    /**
     * YENIDEN ENGELLEMEK IKINCI SATIR DOGURMUYOR.
     *
     * `UNIQUE(householdId, productId)` yuzunden `upsert` eskisini eziyor. Bunun
     * onemi Ayarlar listesinde: iki satir, kullaniciya ayni urunu iki kez
     * gosterir ve "Geri al"in hangisini kaldirdigi belirsiz olurdu.
     */
    @Test
    fun blockingAgainReplacesTheRowInsteadOfAddingOne() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val p = threePurchases(db, repo(db), "Yumurta")
        block(db, p, at = 10 * DAY)
        db.suggestionBlockDao().unblock(home, p, 11 * DAY)
        block(db, p, at = 12 * DAY)

        val rows = db.suggestionBlockDao().blockHistory(home)
        assertEquals(1, rows.size)
        assertEquals(12 * DAY, rows.single().blockedAt)
        assertTrue(rows.single().unblockedAt == null, "yeniden engel yururlukte olmali")
        assertTrue(db.suggestionBlockDao().isBlocked(home, p))
    }

    /**
     * AYARLAR LISTESI: yalnizca YURURLUKTEKI engeller, YENIDEN ESKIYE.
     *
     * Kaldirilmis engel listede DURMUYOR - satir tabloda kalsa da kullanici
     * icin o is bitti. Siralama en son karar en ustte: "az once ne yaptim"
     * sorusu listenin basindan cevaplanir.
     */
    @Test
    fun theSettingsListShowsOnlyActiveBlocksNewestFirst() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val r = repo(db)
        val a = threePurchases(db, r, "Yumurta")
        val b = threePurchases(db, r, "Kola")
        val c = threePurchases(db, r, "Cips")

        block(db, a, at = 10 * DAY)
        block(db, b, at = 20 * DAY)
        block(db, c, at = 30 * DAY)
        db.suggestionBlockDao().unblock(home, b, 40 * DAY)

        assertEquals(
            listOf("Cips", "Yumurta"),
            db.suggestionBlockDao().observeBlocked(home).first().map { it.name },
        )
    }

    /** Silinmis urun listede gorunmuyor - JOIN'in tombstone kosulu. */
    @Test
    fun aDeletedProductDropsOutOfTheSettingsList() = runTest {
        val db = db(); db.bootstrap(newId = { "seed" }, clock = { 0L })
        val p = threePurchases(db, repo(db), "Yumurta")
        block(db, p, at = 10 * DAY)

        db.productDao().softDelete(p, 20 * DAY)
        assertEquals(emptyList(), db.suggestionBlockDao().observeBlocked(home).first())
    }
}
