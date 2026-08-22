package com.neydi.app.data.catalog

import androidx.room3.Room
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.matchKey
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogSeederTest {

    private val HOME = DEFAULT_HOUSEHOLD_ID

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private suspend fun NeydiDatabase.number(sql: String): Long =
        useWriterConnection { it.usePrepared(sql) { st -> st.step(); st.getLong(0) } }

    private suspend fun NeydiDatabase.write(sql: String) =
        useWriterConnection { it.usePrepared(sql) { st -> st.step() } }

    private suspend fun NeydiDatabase.text(sql: String): String =
        useWriterConnection { it.usePrepared(sql) { st -> st.step(); st.getText(0) } }

    @Test
    fun catalogIsSeeded() = runTest {
        val db = db()
        val result = db.seedCatalog(HOME)

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
        db.seedCatalog(HOME)
        val second = db.seedCatalog(HOME)

        assertTrue(second.skipped, "ikinci tohumlama atlanmadi")
        assertEquals(SEED_PRODUCTS.size.toLong(), db.number("SELECT COUNT(*) FROM catalog_seed"))
    }


    /**
     * SURUM DEGISINCE YENIDEN YAZILIYOR (F2.7) - ozelligin varlik sebebi.
     *
     * Once kapida `SELECT COUNT(*) FROM category > 0` vardi ve bu **sessiz bir
     * kullanici hatasiydi**: ilk acilistan sonra katalogdaki hicbir duzeltme o
     * telefona ulasmiyordu. Yeni bir urun, duzeltilmis bir kategori, degismis
     * bir `matchKey` kurali - hicbiri. Yalnizca uygulamayi silip yeniden
     * kuranlar goruyordu.
     *
     * Test damgayi ELLE geri aliyor cunku `CATALOG_SEED_VERSION` bir sabit;
     * "surumu artir" demenin testten yapilabilir hali damgayi eskitmek.
     */
    @Test
    fun aNewSeedVersionRewritesTheCatalog() = runTest {
        val db = db()
        db.seedCatalog(HOME)
        // Katalogda elle bir bozulma: yeniden tohumlama bunu DUZELTMELI.
        db.write("UPDATE catalog_seed SET name = 'BOZUK' WHERE id = 'seed-1'")
        // Damgayi eskit = "gomulu katalog surumu artti".
        db.write("UPDATE app_settings SET catalogSeedVersion = 0")

        val again = db.seedCatalog(HOME)

        assertEquals(false, again.skipped, "surum degisti, yeniden yazilmaliydi")
        assertTrue(
            db.text("SELECT name FROM catalog_seed WHERE id = 'seed-1'") != "BOZUK",
            "yeniden tohumlama satiri duzeltmedi",
        )
    }

    /**
     * KATEGORILER SILINMIYOR, UZERINE YAZILIYOR.
     *
     * `DELETE` + `INSERT` olsaydi aradaki o anda `product.categoryId`
     * kategorilere bakan butun KULLANICI urunleri sahipsiz kalirdi. Test
     * kullanicinin kendi urununun yeniden tohumlamadan sag cikmasina bakiyor.
     */
    @Test
    fun reseedingKeepsUserProductsAttached() = runTest {
        val db = db()
        db.seedCatalog(HOME)
        db.write(
            """
            INSERT INTO product (id, householdId, name, matchKey, categoryId, defaultUnit,
                                 isStaple, createdAt)
            VALUES ('u1', '$DEFAULT_HOUSEHOLD_ID', 'Zencefil', 'zencefil', 'temel-gida',
                    'adet', 0, 0)
            """.trimIndent(),
        )
        db.write("UPDATE app_settings SET catalogSeedVersion = 0")

        db.seedCatalog(HOME)

        assertEquals(1L, db.number("SELECT COUNT(*) FROM product WHERE id = 'u1'"))
        assertEquals(
            1L,
            db.number(
                "SELECT COUNT(*) FROM category WHERE id = " +
                    "(SELECT categoryId FROM product WHERE id = 'u1')",
            ),
            "kullanicinin urunu sahipsiz kaldi",
        )
    }

    /** matchKey veri dosyasinda degil, ekleme aninda F2.4 kuraliyla turetiliyor. */
    @Test
    fun matchKeyIsDerivedOnWrite() = runTest {
        val db = db()
        db.seedCatalog(HOME)

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

    /**
     * BELGELENEN SAYILAR TESTE BAGLI (F1.2'nin kurdugu kural: dokumantasyon
     * curuyemez).
     *
     * Diger testlerin hepsi veritabanini VERI DOSYASIYLA karsilastiriyor, yani
     * kendine gonderme yapiyor: CatalogSeedData 50 urune dusurulse
     * `catalogIsSeeded`, `everyProductHasAKnownCategory` ve
     * `commonalityRankHasNoGaps` yine gecerdi. ROADMAP "245 urun + 12 kategori"
     * diyor; onu tutan tek sey bu iki satir.
     */
    @Test
    fun documentedCatalogSizeIsPinned() {
        assertEquals(245, SEED_PRODUCTS.size)
        assertEquals(12, SEED_CATEGORIES.size)
    }
}
