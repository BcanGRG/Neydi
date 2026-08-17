package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Satir silme ve "Geri al" (tasarim karari 37).
 *
 * BU TESTIN ISIRDIGI YER GERI ALMANIN SESSIZ KAYBI. Depoda zaten bir "mezar
 * kazma" yolu vardi - `add` icindeki `deletedAt = null` - ve ilk bakista geri
 * alma icin bicilmis kaftan gorunuyor. Ama o yol satiri YENIDEN KURUYOR:
 * `quantity` 1'e doner, `checked` sifirlanir, "kim ekledi" degisir.
 *
 * Onunla yazilmis bir "Geri al" hicbir hata vermez, hicbir mevcut testi
 * kirmaz ve iki kilo elmayi bir kiloya cevirir - kullanici bunu ancak
 * markette fark eder. O yuzden geri alma AYRI bir sorgu (`TripLineDao.restore`)
 * ve korunmasi gereken alanlar burada tek tek sinaniyor.
 */
class UndoDeleteTest {

    private val home = "h1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            productDao = db.productDao(),
            clock = { 1_000L },
            newId = { "id-${++n}" },
        )
    }

    private suspend fun prepare(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = home, name = "Bizim ev", createdAt = 0))
    }

    /** Silinen satir listeden dusuyor ama tabloda duruyor - tombstone. */
    @Test
    fun removeHidesTheRowButKeepsIt() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val elma = r.findOrCreateProduct(home, "Elma", "meyve-sebze", "kg")
        val satir = r.add(home, trip.id, elma, memberId = "m1", count = 2.0)

        r.remove(satir.id)

        assertTrue(r.rows(home).first().isEmpty())
        assertNotNull(db.tripLineDao().findIncludingDeleted(trip.id, elma.id))
    }

    /**
     * GERI ALMA MIKTARI KORUYOR - bu dosyanin varlik sebebi.
     *
     * `add` yoluyla yazilmis bir geri alma tam burada duser: o yol adedi 1'e
     * ceker, test 2.0 bekler.
     */
    @Test
    fun undoKeepsTheQuantity() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val elma = r.findOrCreateProduct(home, "Elma", "meyve-sebze", "kg")
        val satir = r.add(home, trip.id, elma, memberId = "m1", count = 2.0)

        r.remove(satir.id)
        r.undoRemove(satir.id)

        assertEquals(2.0, r.rows(home).first().single().quantity)
    }

    /** Isaretli satir silinip geri alinirsa ISARETLI donuyor. */
    @Test
    fun undoKeepsTheCheckedState() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val sut = r.findOrCreateProduct(home, "Süt", "sut-kahvaltilik", "adet")
        val satir = r.add(home, trip.id, sut, memberId = "m1")
        r.toggleChecked(satir.id, true)

        r.remove(satir.id)
        r.undoRemove(satir.id)

        assertTrue(r.rows(home).first().single().checked)
    }

    /** "Kim ekledi" korunuyor - es avatarinin kaynagi bu alan. */
    @Test
    fun undoKeepsWhoAddedIt() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val cay = r.findOrCreateProduct(home, "Çay", "icecek", "adet")
        val satir = r.add(home, trip.id, cay, memberId = "m2")

        r.remove(satir.id)
        r.undoRemove(satir.id)

        assertEquals("m2", r.rows(home).first().single().addedByMemberId)
    }

    /** Geri alinan satirin tombstone'u gercekten kalkiyor. */
    @Test
    fun undoClearsTheTombstone() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val elma = r.findOrCreateProduct(home, "Elma", "meyve-sebze", "kg")
        val satir = r.add(home, trip.id, elma, memberId = "m1")

        r.remove(satir.id)
        r.undoRemove(satir.id)

        assertNull(db.tripLineDao().findIncludingDeleted(trip.id, elma.id)?.deletedAt)
    }

    /**
     * SILME KOMSU SATIRA DOKUNMUYOR.
     *
     * Bariz gorunuyor ama silme bir `UPDATE` ve `WHERE id` yanlis yazilirsa
     * butun geziyi siler - o hata tek satirlik bir listede hic gorunmez.
     */
    @Test
    fun removeTouchesOnlyItsOwnRow() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val elma = r.add(home, trip.id, r.findOrCreateProduct(home, "Elma", "meyve-sebze", "kg"), memberId = "m1")
        r.add(home, trip.id, r.findOrCreateProduct(home, "Süt", "sut-kahvaltilik", "adet"), memberId = "m1")

        r.remove(elma.id)

        assertEquals(1, r.rows(home).first().size)
    }

    /** Art arda iki silme: geri alma yalnizca adini verdigi satiri diriltiyor. */
    @Test
    fun undoRestoresOnlyTheRowItNames() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val elma = r.add(home, trip.id, r.findOrCreateProduct(home, "Elma", "meyve-sebze", "kg"), memberId = "m1")
        val sut = r.add(home, trip.id, r.findOrCreateProduct(home, "Süt", "sut-kahvaltilik", "adet"), memberId = "m1")

        r.remove(elma.id)
        r.remove(sut.id)
        r.undoRemove(sut.id)

        val kalan = r.rows(home).first()
        assertEquals(1, kalan.size)
        assertEquals(sut.id, kalan.single().id)
    }

    /**
     * SILIP YENIDEN EKLEMEK hala calisiyor.
     *
     * Bu yol `restore`dan AYRI ve ayri kalmali: kullanici sildigi urunu elle
     * tekrar eklediginde niyeti "yeni bir satir", geri alma degil - adet 1'den
     * baslamali. Iki yolun ayni sonucu vermesi beklenmiyor.
     */
    @Test
    fun deletingThenAddingAgainStartsFresh() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val elma = r.findOrCreateProduct(home, "Elma", "meyve-sebze", "kg")
        val satir = r.add(home, trip.id, elma, memberId = "m1", count = 5.0)

        r.remove(satir.id)
        r.add(home, trip.id, elma, memberId = "m1")

        assertEquals(1.0, r.rows(home).first().single().quantity)
    }
}
