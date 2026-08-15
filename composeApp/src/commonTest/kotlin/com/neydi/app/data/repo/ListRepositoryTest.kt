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
import kotlin.test.assertTrue

class ListRepositoryTest {

    private val home = "h1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    /** Saat ve id disaridan: test deterministik olsun diye (repository saf). */
    private fun repo(db: NeydiDatabase): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            receiptDao = db.receiptDao(),
            productDao = db.productDao(),
            clock = { 1_000L },
            newId = { "id-${++n}" },
        )
    }

    private suspend fun prepare(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = home, name = "Bizim ev", createdAt = 0))
    }

    /**
     * "Ayni anda tek aktif alisveris" kurali. Sema bunu ZORLAMIYOR - kismi
     * index gerekiyor ve Room yazamiyor (F2.3) - o yuzden kuralin tek
     * uygulanma yeri burasi ve testi de burada olmali.
     */
    @Test
    fun secondCallDoesNotOpenNewTrip() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)

        val first = r.openOrGetActiveTrip(home, "m1")
        val second = r.openOrGetActiveTrip(home, "m1")

        assertEquals(first.id, second.id, "ikinci cagri yeni bir alisveris acti")
    }

    @Test
    fun newTripOpensAfterFinishing() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)

        val first = r.openOrGetActiveTrip(home, "m1")
        r.closeTrip(first.id, memberId = "m1")
        val fresh = r.openOrGetActiveTrip(home, "m1")

        assertTrue(fresh.id != first.id, "bitmis alisveris hala aktif goruluyor")
    }

    /**
     * ASIL DAVRANIS: es zaten ekmek eklemisse ikinci ekleme HATA VERMEMELI,
     * adedi artirmali. UNIQUE(tripId, productId) ikinci satiri zaten
     * engelliyor; kisita carpip "ekleyemedim" demek yanlis cevap olurdu.
     */
    @Test
    fun readdingSameProductIncrementsQuantity() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val bread = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")

        r.add(home, trip.id, bread, memberId = "m1")
        r.add(home, trip.id, bread, memberId = "m2")

        val rows = r.rows(home).first()
        assertEquals(1, rows.size, "ikinci ekleme yeni satir acti")
        assertEquals(2.0, rows.single().quantity)
        // Ilk ekleyen korunuyor: "kim ekledi" bilgisi ezilmemeli.
        assertEquals("m1", rows.single().addedByMemberId)
    }

    /** matchKey uzerinden bakiyor: "Ekmek" ile "EKMEK" ayri urun olmamali. */
    @Test
    fun caseDoesNotSplitProducts() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)

        val a = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")
        val b = r.findOrCreateProduct(home, "EKMEK", "firin-ekmek", "adet")

        assertEquals(a.id, b.id)
    }

    @Test
    fun checkAndUncheckFlowThrough() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val milk = r.findOrCreateProduct(home, "Süt", "sut-kahvalti", "L")
        val row = r.add(home, trip.id, milk, memberId = "m1")

        r.toggleChecked(row.id, true)
        val checked = r.rows(home).first().single()
        assertTrue(checked.checked)
        // checkedAt saklanmali: reyonda mi evde mi isaretlendi sorusu sonra lazim.
        assertNotNull(checked.checkedAt)

        r.remove(row.id)
        assertTrue(r.rows(home).first().isEmpty(), "cikarilan satir hala listede")
    }

    /** Aktif alisveris yoksa satirlar BOS liste - hata degil. */
    @Test
    fun rowsEmptyWithoutActiveTrip() = runTest {
        val db = db(); prepare(db)
        assertTrue(repo(db).rows(home).first().isEmpty())
    }

    /** Silinen satir tombstone; sorgular onu getirmemeli. */
    @Test
    fun deletedRowDoesNotResurrect() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val product = r.findOrCreateProduct(home, "Yumurta", "sut-kahvalti", "adet")
        val row = r.add(home, trip.id, product, memberId = "m1")
        r.remove(row.id)

        // Ayni urun tekrar eklenebilmeli - tombstone yeni eklemeyi engellememeli.
        val again = r.add(home, trip.id, product, memberId = "m1")
        assertTrue(r.rows(home).first().size == 1)
        // Mezardan cikan satir adedi SIFIRDAN baslar, eski adedi tasimaz.
        assertEquals(1.0, again.quantity)
    }
}
