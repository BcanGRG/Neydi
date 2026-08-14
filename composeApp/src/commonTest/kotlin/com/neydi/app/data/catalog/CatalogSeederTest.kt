package com.neydi.app.data.catalog

import androidx.room3.Room
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.matchKey
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogSeederTest {

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private suspend fun NeydiDatabase.number(sql: String): Long =
        useWriterConnection { it.usePrepared(sql) { st -> st.step(); st.getLong(0) } }

    private suspend fun NeydiDatabase.text(sql: String): String =
        useWriterConnection { it.usePrepared(sql) { st -> st.step(); st.getText(0) } }

    @Test
    fun catalogIsSeeded() = runTest {
        val db = db()
        val result = db.seedCatalog()

        assertEquals(false, result.skipped)
        assertEquals(SEED_CATEGORIES.size.toLong(), db.number("SELECT COUNT(*) FROM category"))
        assertEquals(SEED_PRODUCTS.size.toLong(), db.number("SELECT COUNT(*) FROM catalog_seed"))
    }

    /**
     * Her acilista cagrilacak. Ikinci cagri hicbir sey yazmamali - yoksa
     * uygulama her acildiginda 245 satiri yeniden yazar.
     */
    @Test
    fun secondCallWritesNothing() = runTest {
        val db = db()
        db.seedCatalog()
        val second = db.seedCatalog()

        assertTrue(second.skipped, "ikinci tohumlama atlanmadi")
        assertEquals(SEED_PRODUCTS.size.toLong(), db.number("SELECT COUNT(*) FROM catalog_seed"))
    }

    /** matchKey veri dosyasinda degil, ekleme aninda F2.4 kuraliyla turetiliyor. */
    @Test
    fun matchKeyIsDerivedOnWrite() = runTest {
        val db = db()
        db.seedCatalog()

        assertEquals(
            matchKey("Ayçiçek Yağı"),
            db.text("SELECT matchKey FROM catalog_seed WHERE name = 'Ayçiçek Yağı'"),
        )
        assertEquals(
            "aycicek yagi",
            db.text("SELECT matchKey FROM catalog_seed WHERE name = 'Ayçiçek Yağı'"),
        )
    }

    /** Yayginlik 1'den baslar ve boslugu olmamali - siralama buna dayaniyor. */
    @Test
    fun commonalityRankHasNoGaps() {
        val orders = SEED_PRODUCTS.map { it.commonality }.sorted()
        assertEquals((1..SEED_PRODUCTS.size).toList(), orders)
        assertEquals("Ekmek", SEED_PRODUCTS.first { it.commonality == 1 }.name)
    }

    /** Her urunun kategorisi gercekten var olmali; yoksa liste bolumsuz kalir. */
    @Test
    fun everyProductHasAKnownCategory() {
        val ids = SEED_CATEGORIES.map { it.id }.toSet()
        val missing = SEED_PRODUCTS.filter { it.categoryId !in ids }
        assertTrue(missing.isEmpty(), "kategorisi tanimsiz urun: ${missing.map { it.name }}")
    }

    /**
     * Katalog matchKey uzerinden aranacak. Iki urun ayni anahtara duserse
     * arama hangisini gosterecegini bilemez.
     */
    @Test
    fun noMatchKeyCollisions() {
        val groups = SEED_PRODUCTS.groupBy { matchKey(it.name) }.filter { it.value.size > 1 }
        assertTrue(
            groups.isEmpty(),
            "ayni matchKey'e dusen urunler: ${groups.map { (k, v) -> "$k -> ${v.map { u -> u.name }}" }}",
        )
    }

    /** Kategori sirasi market gezme sirasi; bosluksuz 0..n olmali. */
    @Test
    fun categoryOrderHasNoGaps() {
        assertEquals(
            SEED_CATEGORIES.indices.toList(),
            SEED_CATEGORIES.map { it.order }.sorted(),
        )
        assertEquals("meyve-sebze", SEED_CATEGORIES.first { it.order == 0 }.id)
    }
}
