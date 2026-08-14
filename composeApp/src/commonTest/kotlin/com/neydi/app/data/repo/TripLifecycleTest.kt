package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.TripStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F4.1 gezi yasam dongusu: PLANNING -> SHOPPING -> CLOSED.
 *
 * Bu dosyanin varlik sebebi tek bir cumle: MUTABAKATI TEK CIHAZ YAPAR. Iki
 * cihaz ayni geziyi kapatirsa satin almalar cift sayilir, `medianIntervalDays`
 * yariya duser ve uygulama her seyi iki kat sik onermeye baslar. Sessiz,
 * yavas ve geri alinmasi zor bir bozulma - o yuzden kurali zorlayan
 * karsilastir-ve-yaz burada test ediliyor.
 */
class TripLifecycleTest {

    private val home = "h1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    /** Saat SABIT: kapanis zamaninin ilerlemedigini boyle kanitlayabiliyoruz. */
    private fun repo(db: NeydiDatabase, clock: () -> Long = { 1_000L }): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            productDao = db.productDao(),
            clock = clock,
            newId = { "id-${++n}" },
        )
    }

    private suspend fun prepare(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = home, name = "Bizim ev", createdAt = 0))
    }

    @Test
    fun newTripStartsInPlanning() = runTest {
        val db = db(); prepare(db)
        val trip = repo(db).openOrGetActiveTrip(home)
        assertEquals(TripStatus.PLANNING, trip.status)
        assertNull(trip.ownerMemberId, "acilista kapatan uye olmamali")
        assertNull(trip.completedAt)
    }

    /** Alisveris modu KALICI: ekran durumu degil, gezinin durumu. */
    @Test
    fun shoppingModeIsPersistedOnTheTrip() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home)

        r.setShoppingMode(trip.id, enabled = true)
        assertEquals(TripStatus.SHOPPING, db.tripDao().byId(trip.id)?.status)

        r.setShoppingMode(trip.id, enabled = false)
        assertEquals(TripStatus.PLANNING, db.tripDao().byId(trip.id)?.status)
    }

    @Test
    fun closingSetsStatusOwnerAndTimestamp() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home)

        assertTrue(r.closeTrip(trip.id, memberId = "m1"), "ilk kapatma basarili olmali")

        val closed = assertNotNull(db.tripDao().byId(trip.id))
        assertEquals(TripStatus.CLOSED, closed.status)
        assertEquals("m1", closed.ownerMemberId)
        assertEquals(1_000L, closed.completedAt)
    }

    /**
     * ISIN OZU. Ikinci kapatma HICBIR SEY yazmamali.
     *
     * Saat ikinci cagriya kadar ilerletiliyor: eger kapatma korumasiz olsaydi
     * `completedAt` 2_000'e cikar ve `ownerMemberId` "m2" olurdu. Ikisinin de
     * degismemesi, karsilastir-ve-yazin gercekten calistigi anlamina gelir.
     */
    @Test
    fun secondCloseWritesNothing() = runTest {
        val db = db(); prepare(db)
        var now = 1_000L
        val r = repo(db, clock = { now })
        val trip = r.openOrGetActiveTrip(home)

        assertTrue(r.closeTrip(trip.id, memberId = "m1"))

        now = 2_000L
        assertFalse(
            r.closeTrip(trip.id, memberId = "m2"),
            "ikinci kapatma false donmeli - yoksa cagiran taraf mutabakati tekrar yapar",
        )

        val closed = assertNotNull(db.tripDao().byId(trip.id))
        assertEquals("m1", closed.ownerMemberId, "kapatan uye ILK kapatanda kalmali")
        assertEquals(1_000L, closed.completedAt, "kapanis zamani ilerlememeli")
    }

    /** Kapanmis gezi artik aktif degil; sonraki ekleme YENI gezi acmali. */
    @Test
    fun closedTripIsNoLongerActive() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val first = r.openOrGetActiveTrip(home)
        r.closeTrip(first.id, memberId = "m1")

        assertNull(db.tripDao().activeOrNull(home), "kapanmis gezi aktif gorunmemeli")

        val fresh = r.openOrGetActiveTrip(home)
        assertTrue(fresh.id != first.id, "kapanistan sonra yeni gezi acilmali")
        assertEquals(TripStatus.PLANNING, fresh.status)
    }

    /**
     * Kapanmis gezi SHOPPING'e geri cekilemez.
     *
     * Gercek akista bu kolayca olabilirdi: ozet karti kapanirken alisveris
     * modu anahtari da sifirlaniyor ve o yazma kapanmis geziye gidebilirdi.
     * Gezi yeniden "acik" gorunur, gecmisten kaybolur, ve bir sonraki ekleme
     * yeni gezi acmak yerine kapanmis geziye yazardi.
     */
    @Test
    fun closedTripCannotBeReopened() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home)
        r.closeTrip(trip.id, memberId = "m1")

        r.setShoppingMode(trip.id, enabled = true)

        val still = assertNotNull(db.tripDao().byId(trip.id))
        assertEquals(TripStatus.CLOSED, still.status, "kapanmis gezi SHOPPING'e donmemeli")
        assertNotNull(still.completedAt)
    }

    /** Gecmis yalnizca kapanmis gezileri gostermeli. */
    @Test
    fun historyShowsOnlyClosedTrips() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val first = r.openOrGetActiveTrip(home)
        r.closeTrip(first.id, memberId = "m1")
        val open = r.openOrGetActiveTrip(home)

        val history = db.tripDao().observeHistory(home).first()
        assertEquals(listOf(first.id), history.map { it.id })
        assertTrue(history.none { it.id == open.id }, "acik gezi gecmiste olmamali")
    }
}
