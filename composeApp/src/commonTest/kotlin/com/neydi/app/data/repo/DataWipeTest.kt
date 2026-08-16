package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.Product
import com.neydi.app.data.db.Store
import com.neydi.app.data.db.Trip
import com.neydi.app.ui.settings.rowsOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * "Verilerimi sil" (tasarim karari 2).
 *
 * BU EKRAN GERI ALINAMAZ BIR IS YAPIYOR, yani testin isi iki yonlu: silinmesi
 * gerekenin gittigini VE silinmemesi gerekenin kaldigini gostermek.
 */
class DataWipeTest {

    private val home = "h1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private suspend fun seed(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = home, name = "Bizim ev", createdAt = 0))
        db.tripDao().insert(Trip(id = "t1", householdId = home, startedAt = 0, createdAt = 0))
        db.productDao().insert(
            Product(
                id = "p1",
                householdId = home,
                name = "Ekmek",
                matchKey = "ekmek",
                categoryId = "temel-gida",
                defaultUnit = "adet",
                isStaple = true,
                createdAt = 0,
            ),
        )
        db.storeDao().insert(
            Store(id = "s1", householdId = home, name = "BIM", chain = "bim", createdAt = 0),
        )
    }

    @Test
    fun countsWhatWillGo() = runTest {
        val db = db(); seed(db)
        val counts = DataWipe(db.dataWipeDao()).counts(home)

        assertEquals(1, counts.trips)
        assertEquals(1, counts.products)
        assertEquals(1, counts.staples)
    }

    @Test
    fun wipeRemovesHouseholdData() = runTest {
        val db = db(); seed(db)
        val wipe = DataWipe(db.dataWipeDao())
        wipe.wipe(home)

        assertTrue(wipe.counts(home).isEmpty)
        assertTrue(db.storeDao().observeAll(home).first().isEmpty())
        assertEquals(null, db.tripDao().byId("t1"))
    }

    /**
     * HANE VE REFERANS VERISI KALIYOR. Silme "hesabi kapat" degil "verilerimi
     * sil"; hane satiri da giderse uygulama bir daha hicbir sey yazamaz.
     */
    @Test
    fun wipeKeepsHouseholdItself() = runTest {
        val db = db(); seed(db)
        DataWipe(db.dataWipeDao()).wipe(home)

        assertEquals("Bizim ev", db.householdDao().observeActive().first()?.name)
    }

    // --- Satirlarin metni ---------------------------------------------------

    /**
     * SIFIR SAYAN SATIR CIZILMIYOR. "Alisveris 0" yazmak, olmayan bir seyi
     * silineceklerin arasina koymak olurdu.
     */
    @Test
    fun zeroCountsDrawNoRows() {
        assertTrue(rowsOf(WipeCounts()).isEmpty())
        assertEquals(
            listOf("Alışveriş"),
            rowsOf(WipeCounts(trips = 3)).map { it.name },
        )
    }

    /** Iki seyi sayan satir IKI RAKAM tasiyor - tasarimin kendi bicimi ("9 + 2"). */
    @Test
    fun compoundRowsCarryBothNumbers() {
        val rows = rowsOf(WipeCounts(trips = 18, staples = 9, blocks = 2))
        assertEquals("9 + 2", rows.first { it.name.startsWith("Her zamankiler") }.count)
    }
}
