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

    private suspend fun NeydiDatabase.sayi(sql: String): Long =
        useWriterConnection { it.usePrepared(sql) { st -> st.step(); st.getLong(0) } }

    private suspend fun NeydiDatabase.metin(sql: String): String =
        useWriterConnection { it.usePrepared(sql) { st -> st.step(); st.getText(0) } }

    @Test
    fun katalogYazilir() = runTest {
        val db = db()
        val sonuc = db.tohumlaKatalog()

        assertEquals(false, sonuc.atlandi)
        assertEquals(SEED_KATEGORILER.size.toLong(), db.sayi("SELECT COUNT(*) FROM category"))
        assertEquals(SEED_URUNLER.size.toLong(), db.sayi("SELECT COUNT(*) FROM catalog_seed"))
    }

    /**
     * Her acilista cagrilacak. Ikinci cagri hicbir sey yazmamali - yoksa
     * uygulama her acildiginda 245 satiri yeniden yazar.
     */
    @Test
    fun ikinciCagriHicbirSeyYazmaz() = runTest {
        val db = db()
        db.tohumlaKatalog()
        val ikinci = db.tohumlaKatalog()

        assertTrue(ikinci.atlandi, "ikinci tohumlama atlanmadi")
        assertEquals(SEED_URUNLER.size.toLong(), db.sayi("SELECT COUNT(*) FROM catalog_seed"))
    }

    /** matchKey veri dosyasinda degil, ekleme aninda F2.4 kuraliyla turetiliyor. */
    @Test
    fun matchKeyTuretilerekYazilir() = runTest {
        val db = db()
        db.tohumlaKatalog()

        assertEquals(
            matchKey("Ayçiçek Yağı"),
            db.metin("SELECT matchKey FROM catalog_seed WHERE name = 'Ayçiçek Yağı'"),
        )
        assertEquals(
            "aycicek yagi",
            db.metin("SELECT matchKey FROM catalog_seed WHERE name = 'Ayçiçek Yağı'"),
        )
    }

    /** Yayginlik 1'den baslar ve boslugu olmamali - siralama buna dayaniyor. */
    @Test
    fun yayginlikSiralamasiKesintisiz() {
        val siralar = SEED_URUNLER.map { it.yayginlik }.sorted()
        assertEquals((1..SEED_URUNLER.size).toList(), siralar)
        assertEquals("Ekmek", SEED_URUNLER.first { it.yayginlik == 1 }.ad)
    }

    /** Her urunun kategorisi gercekten var olmali; yoksa liste bolumsuz kalir. */
    @Test
    fun tumUrunlerinKategorisiTanimli() {
        val idler = SEED_KATEGORILER.map { it.id }.toSet()
        val eksik = SEED_URUNLER.filter { it.kategoriId !in idler }
        assertTrue(eksik.isEmpty(), "kategorisi tanimsiz urun: ${eksik.map { it.ad }}")
    }

    /**
     * Katalog matchKey uzerinden aranacak. Iki urun ayni anahtara duserse
     * arama hangisini gosterecegini bilemez.
     */
    @Test
    fun matchKeyCakismasiYok() {
        val gruplar = SEED_URUNLER.groupBy { matchKey(it.ad) }.filter { it.value.size > 1 }
        assertTrue(
            gruplar.isEmpty(),
            "ayni matchKey'e dusen urunler: ${gruplar.map { (k, v) -> "$k -> ${v.map { u -> u.ad }}" }}",
        )
    }

    /** Kategori sirasi market gezme sirasi; bosluksuz 0..n olmali. */
    @Test
    fun kategoriSirasiKesintisiz() {
        assertEquals(
            SEED_KATEGORILER.indices.toList(),
            SEED_KATEGORILER.map { it.sira }.sorted(),
        )
        assertEquals("meyve-sebze", SEED_KATEGORILER.first { it.sira == 0 }.id)
    }
}
