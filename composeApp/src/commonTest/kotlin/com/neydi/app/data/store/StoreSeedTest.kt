package com.neydi.app.data.store

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.PriceObservation
import com.neydi.app.data.db.Store
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Market tohumu ve yapiskan secici (tasarim karari 11).
 *
 * Bu testin isirdigi yer IDEMPOTENSI: tohum her acilista kosuyor ve rastgele
 * id verilseydi her acilis yeni satir yazardi. Katalog tohumunda ayni hata bir
 * kez odendi; burada baslangictan itibaren kilitleniyor.
 */
class StoreSeedTest {

    private val home = DEFAULT_HOUSEHOLD_ID

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    @Test
    fun seedsSevenChains() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })

        val stores = db.storeDao().observeAll(home).first()
        assertEquals(7, stores.size)
        // KUME KARSILASTIRMASI, sirali liste DEGIL: `sorted()` kod noktasina
        // gore siraliyor ve "ŞOK" (U+015E) Turkce'de T'den once gelmesi
        // gerekirken sonra dusuyor. Projenin locale kurali burada da gecerli -
        // testin konusu yedi zincirin varligi, siralamasi degil.
        assertEquals(
            setOf("A101", "BİM", "CarrefourSA", "File", "Migros", "ŞOK", "Tarım Kredi"),
            stores.map { it.name }.toSet(),
        )
    }

    /** Zincir anahtari `chainKey`den geliyor - alias ogrenmesiyle ayni vokabuler. */
    @Test
    fun chainKeysMatchAliasVocabulary() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })

        val chains = db.storeDao().observeAll(home).first().associate { it.name to it.chain }
        assertEquals("bim", chains["BİM"])
        assertEquals("a101", chains["A101"])
        assertEquals("migros", chains["Migros"])
        // "Tarım Kredi" iki kelime: chainKey ilk anlamli kelimeyi aliyor.
        assertEquals("tarim", chains["Tarım Kredi"])
    }

    /**
     * IDEMPOTENSI. Bootstrap her acilista kosuyor; ucuncu kosumdan sonra da
     * yedi satir olmali.
     */
    @Test
    fun seedingIsIdempotent() = runTest {
        val db = db()
        repeat(3) { db.bootstrap(newId = { "m1" }, clock = { it.toLong() }) }

        assertEquals(7, db.storeDao().observeAll(home).first().size)
    }

    /**
     * ZINCIR ZATEN VARSA TOHUM YAZMIYOR.
     *
     * Cihazda gorulen hal: fis donemi `BIM` adiyla bir satir birakmisti ve
     * tohum yalnizca id'ye baksaydi yanina ikinci bir `bim` zinciri koyardi.
     * `findByChain` LIMIT 1 oldugu icin hangisinin donecegi belirsiz olurdu.
     */
    @Test
    fun seedSkipsChainsThatAlreadyExist() = runTest {
        val db = db()
        db.householdDao().upsert(
            com.neydi.app.data.db.Household(id = home, name = "Bizim ev", createdAt = 0),
        )
        // Eski yoldan gelmis bir satir - adi da farkli yazilmis.
        db.storeDao().insert(
            Store(id = "eski-1", householdId = home, name = "BIM", chain = "bim", createdAt = 0),
        )

        db.bootstrap(newId = { "m1" }, clock = { 1 })

        val bim = db.storeDao().observeAll(home).first().filter { it.chain == "bim" }
        assertEquals(1, bim.size)
        // Once gelen kazaniyor: tohum eski satiri EZMIYOR.
        assertEquals("BIM", bim.single().name)
        // Diger alti zincir yine geldi.
        assertEquals(7, db.storeDao().observeAll(home).first().size)
    }

    /** Ayni zincir `findByChain` ile bulunuyor - "+ Yeni market" tekillestirmesi buna dayaniyor. */
    @Test
    fun seededChainIsFoundByChainKey() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })

        assertNotNull(db.storeDao().findByChain(home, chainKey("BİM BADEMLİK ŞUBESİ")))
        assertNull(db.storeDao().findByChain(home, chainKey("Hiç Olmayan Market")))
    }

    // --- Yapiskan secici -----------------------------------------------------

    /** Hic gozlem yokken null: secici bos aciliyor, varsayilan uydurulmuyor. */
    @Test
    fun lastUsedStoreIsNullBeforeAnyObservation() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })

        assertNull(db.priceObservationDao().lastUsedStoreId(home))
    }

    @Test
    fun lastUsedStoreIsTheMostRecentObservation() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.productDao().insert(urun())

        db.priceObservationDao().insert(gozlem("o1", "store-seed-bim", observedAt = 100))
        db.priceObservationDao().insert(gozlem("o2", "store-seed-migros", observedAt = 300))
        db.priceObservationDao().insert(gozlem("o3", "store-seed-a101", observedAt = 200))

        assertEquals("store-seed-migros", db.priceObservationDao().lastUsedStoreId(home))
    }

    /** Silinen gozlem yapiskan degeri belirlemez. */
    @Test
    fun deletedObservationDoesNotStick() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.productDao().insert(urun())

        db.priceObservationDao().insert(gozlem("o1", "store-seed-bim", observedAt = 100))
        db.priceObservationDao().insert(
            gozlem("o2", "store-seed-migros", observedAt = 300).copy(deletedAt = 400),
        )

        assertEquals("store-seed-bim", db.priceObservationDao().lastUsedStoreId(home))
    }

    private fun urun() = com.neydi.app.data.db.Product(
        id = "p1",
        householdId = home,
        name = "Yoğurt",
        matchKey = "yogurt",
        categoryId = "temel-gida",
        defaultUnit = "adet",
        createdAt = 0,
    )

    private fun gozlem(id: String, storeId: String, observedAt: Long) = PriceObservation(
        id = id,
        householdId = home,
        productId = "p1",
        storeId = storeId,
        unitPriceMinor = 10_000,
        observedAt = observedAt,
        createdAt = observedAt,
    )

    @Suppress("unused")
    private fun unusedStoreCtorGuard() = Store(
        id = "x", householdId = home, name = "X", chain = "x", createdAt = 0,
    )
}
