package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Mod B - fissiz mutabakat (F4.8).
 *
 * Buradaki kural tek cumleyle: HICBIR SEYE DOKUNULMAZSA PLANLANANLAR ALINDI
 * SAYILIR. Tembel kullanim da kullanilabilir veri uretmeli, yoksa oneri motoru
 * hic beslenmez.
 */
class OptimisticReconcileTest {

    // DEFAULT_HOUSEHOLD_ID: bootstrap haneyi bu kimlikle tohumluyor.
    private val home = DEFAULT_HOUSEHOLD_ID

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
            clock = { 1_000L },
            newId = { "id-${++n}" },
        )
    }

    /**
     * BOOTSTRAP ILE hazirlaniyor, elle hane insert ederek DEGIL.
     *
     * Sebebi olculdu: `observeList` kategori tablosuyla JOIN yapiyor ve
     * kategoriler tohumlanmadiginda JOIN butun satirlari dusuruyor - yani
     * sorgu bos donuyor. Bunu fark etmemek testi SESSIZCE ise yaramaz yapardi:
     * ilk yazdigim hali bos liste uzerinde `all { it.checked }` cagirdigi icin
     * geciyordu ve hicbir sey kanitlamiyordu.
     */
    private suspend fun prepare(db: NeydiDatabase) {
        db.bootstrap(newId = { "seed" }, clock = { 0L })
    }

    /** Uc urunlu bir liste kurar; hicbiri isaretli degil. */
    private suspend fun threeItemList(db: NeydiDatabase, r: ListRepository): String {
        val trip = r.openOrGetActiveTrip(home)
        listOf("Ekmek", "Süt", "Domates").forEach { name ->
            val product = r.findOrCreateProduct(home, name, "temel-gida", "adet")
            r.add(home, trip.id, product, memberId = "m1")
        }
        return trip.id
    }

    @Test
    fun untouchedListCountsAsBought() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = threeItemList(db, r)

        val reconciled = r.reconcileOptimistically(trip)

        assertEquals(3, reconciled)
        val rows = db.tripLineDao().observeList(trip).first()
        // BOYUT KONTROLU SART: bos liste uzerinde `all` her zaman true doner ve
        // testin ilk hali tam olarak bu yuzden bir sey kanitlamiyordu.
        assertEquals(3, rows.size)
        assertTrue(rows.all { it.checked })
    }

    /** Reyonda isaretlenmis satirlar TEKRAR sayilmaz - donen sayi 3 degil 2. */
    @Test
    fun alreadyCheckedLinesAreNotCountedAgain() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = threeItemList(db, r)
        val first = db.tripLineDao().observeList(trip).first().first()
        r.setTaken(first.rowId, true)

        assertEquals(2, r.reconcileOptimistically(trip))
    }

    /**
     * IKINCI CAGRI SIFIR DONDURMELI.
     *
     * Kapanisi iki cihaz denerse mutabakat iki kez calisamaz; calissaydi satin
     * almalar cift sayilir ve medianIntervalDays yariya duserdi - uygulama her
     * seyi iki kat sik onermeye baslardi. Sessiz ve geri alinmasi zor bir
     * bozulma.
     */
    @Test
    fun secondReconcileWritesNothing() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = threeItemList(db, r)

        r.reconcileOptimistically(trip)
        assertEquals(0, r.reconcileOptimistically(trip))
    }

    /** Silinmis satir alindi SAYILMAZ: listeden cikarilmis urun satin alinmadi. */
    @Test
    fun deletedLinesAreNotReconciled() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = threeItemList(db, r)
        val rows = db.tripLineDao().observeList(trip).first()
        db.tripLineDao().softDelete(rows.first().rowId, 900L)

        assertEquals(2, r.reconcileOptimistically(trip))
    }

    /**
     * Bitir ekraninin geri alma yolu (F4.8).
     *
     * Iyimser varsayimi duzeltilebilir yapmadan birakmak onu bir tuzaga
     * cevirirdi: alinmamis urun satin alma gecmisine girer ve kullanici bunu
     * hic gormez.
     */
    @Test
    fun userCanUndoOptimisticTake() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = threeItemList(db, r)
        r.reconcileOptimistically(trip)

        val sut = db.tripLineDao().observeList(trip).first().first { it.name == "Süt" }
        r.setTaken(sut.rowId, false)

        val after = db.tripLineDao().observeList(trip).first()
        assertFalse(after.first { it.name == "Süt" }.checked)
        assertTrue(after.filter { it.name != "Süt" }.all { it.checked })
    }

    /** Geri alinan satirin checkedAt'i de temizlenmeli, yoksa "alindi" izi kalir. */
    @Test
    fun undoClearsCheckedTimestamp() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = threeItemList(db, r)
        r.reconcileOptimistically(trip)
        val row = db.tripLineDao().observeList(trip).first().first()

        r.setTaken(row.rowId, false)

        assertEquals(null, db.tripLineDao().find(trip, row.productId)?.checkedAt)
    }
}
