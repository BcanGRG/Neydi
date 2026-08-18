package com.neydi.app.ui.settings

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.PriceObservation
import com.neydi.app.data.db.Product
import com.neydi.app.data.db.Store
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import com.neydi.app.data.store.SEED_CHAINS
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Ayarlar'daki Zincirler satiri (tasarim karari 36).
 *
 * BU DOSYA NEDEN VAR: karar 36'dan once Ayarlar'in hic testi yoktu ve bu
 * gorunmez degildi - ayni bolumde art arda iki hata cikti. Once eşik ile tohum
 * birbirini yedi (bolum sifir gozlemde ciziliyordu), sonra satir uzun degerle
 * kirildi. Ikisi de yalnizca CIHAZDA gorulebiliyordu.
 *
 * Karar 36 bolumun icine bir AYRIM koydu - gozlemi olan zincir koyu, olmayan
 * soluk - ve bu ayrim sessizce bozulabilecek turden: bayrak hep `false`
 * kalirsa ekran "biraz sonuk" gorunur, bozuk degil. O yuzden hem bayragin
 * kaynagi (SQL) hem tuketicisi (siralama) burada kilitleniyor.
 */
class SettingsStoreRowTest {

    private val home = DEFAULT_HOUSEHOLD_ID

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    // --- Saf donusum: bayrak + siralama -------------------------------------

    /** Gozlemi olan zincir bayragi tasiyor, olmayan tasimiyor. */
    @Test
    fun observedChainsCarryTheFlag() {
        val rows = storeRows(
            stores = listOf(store("a", "BİM"), store("b", "A101"), store("c", "Migros")),
            observedStoreIds = setOf("b"),
        )

        assertEquals(setOf("A101"), rows.filter { it.hasObservation }.map { it.name }.toSet())
        assertEquals(setOf("BİM", "Migros"), rows.filterNot { it.hasObservation }.map { it.name }.toSet())
    }

    /**
     * GOZLEMLILER ONCE. Ayrimin tek tasiyicisi renk olsaydi renk gormeyen
     * kullaniciya ulasmazdi; siralama bilgiyi konuma da yaziyor.
     */
    @Test
    fun observedChainsComeFirst() {
        val rows = storeRows(
            stores = listOf(store("a", "BİM"), store("b", "A101"), store("c", "Migros")),
            observedStoreIds = setOf("c"),
        )

        assertEquals("Migros", rows.first().name)
        assertTrue(rows.first().hasObservation)
    }

    /**
     * SIRALAMA KARARLI: grup icinde tohum sirasi bozulmuyor.
     *
     * `sortedByDescending` kararli oldugu icin bu bedava geliyor - ama bedava
     * gelen sey de degistirilebilir, o yuzden yaziyla duruyor. Kararsiz bir
     * siralamaya gecilirse zincirler her akista yer degistirirdi.
     */
    @Test
    fun orderWithinEachGroupIsPreserved() {
        val rows = storeRows(
            stores = listOf(
                store("a", "BİM"), store("b", "A101"), store("c", "ŞOK"),
                store("d", "Migros"), store("e", "File"),
            ),
            observedStoreIds = setOf("c", "e"),
        )

        assertEquals(listOf("ŞOK", "File", "BİM", "A101", "Migros"), rows.map { it.name })
    }

    /** Hic gozlem yokken hepsi soluk - yeni bir kurulumun hali. */
    @Test
    fun freshInstallHasNoObservedChain() {
        val rows = storeRows(
            stores = listOf(store("a", "BİM"), store("b", "A101")),
            observedStoreIds = emptySet(),
        )

        assertTrue(rows.none { it.hasObservation })
        // Siralama da bozulmuyor: hepsi ayni grupta.
        assertEquals(listOf("BİM", "A101"), rows.map { it.name })
    }

    // --- Bayragin kaynagi: SQL ----------------------------------------------

    @Test
    fun observedStoreIdsComeFromRealObservations() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.productDao().insert(urun())
        db.priceObservationDao().insert(gozlem("o1", "store-seed-bim"))

        val observed = db.priceObservationDao()
            .observeStoreIdsWithObservations(home).first().toSet()

        assertEquals(setOf("store-seed-bim"), observed)
    }

    /**
     * DISTINCT calisiyor: ayni markette uc gozlem tek id donduruyor.
     *
     * Onemli, cunku tuketen taraf `toSet()` yapiyor ve tekrarli satirlar orada
     * zaten erirdi - yani hata GORUNMEZ olurdu. Sorgunun kendisi dogru olmali:
     * yuzlerce gozlemli bir hanede tekrarli satirlari tasimak bedava degil.
     */
    @Test
    fun repeatedObservationsAtOneChainYieldOneId() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.productDao().insert(urun())
        repeat(3) { i ->
            db.priceObservationDao().insert(gozlem("o$i", "store-seed-bim", observedAt = i.toLong()))
        }

        assertEquals(
            1,
            db.priceObservationDao().observeStoreIdsWithObservations(home).first().size,
        )
    }

    /** Silinen gozlem zinciri koyu yapmiyor - satir geri soluyor. */
    @Test
    fun deletedObservationDoesNotMarkTheChain() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.productDao().insert(urun())
        db.priceObservationDao().insert(gozlem("o1", "store-seed-bim").copy(deletedAt = 5))

        assertTrue(db.priceObservationDao().observeStoreIdsWithObservations(home).first().isEmpty())
    }

    /**
     * UCTAN UCA: tohumlanmis yedi zincir + tek bir gozlem.
     *
     * Yeni bir kullanicinin ilk etiketini cektikten SONRAKI hali. Sinanan sey
     * bayragin SQL'den saf fonksiyona dogru tasindigi - iki parca ayri ayri
     * dogru olup birlestiginde yanlis olabilirdi.
     */
    @Test
    fun oneObservationLiftsExactlyOneSeededChain() = runTest {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.productDao().insert(urun())
        db.priceObservationDao().insert(gozlem("o1", "store-seed-migros"))

        val rows = storeRows(
            stores = db.storeDao().observeAll(home).first(),
            observedStoreIds = db.priceObservationDao()
                .observeStoreIdsWithObservations(home).first().toSet(),
        )

        // SAYI TOHUMDAN: elle yazilan bir sayi, zincir listesi degistiginde
        // testi "gercek degisti" degil "beklenti eskidi" diye kirmiziya
        // dusururdu.
        assertEquals(SEED_CHAINS.size, rows.size)
        assertEquals("Migros", rows.first().name)
        assertTrue(rows.first().hasObservation)
        assertEquals(1, rows.count { it.hasObservation })
        assertFalse(rows.last().hasObservation)
    }

    private fun store(id: String, name: String) = Store(
        id = id, householdId = home, name = name, chain = id, createdAt = 0,
    )

    private fun urun() = Product(
        id = "p1",
        householdId = home,
        name = "Yoğurt",
        matchKey = "yogurt",
        categoryId = "temel-gida",
        defaultUnit = "adet",
        createdAt = 0,
    )

    private fun gozlem(id: String, storeId: String, observedAt: Long = 100) = PriceObservation(
        id = id,
        householdId = home,
        productId = "p1",
        storeId = storeId,
        unitPriceMinor = 10_000,
        observedAt = observedAt,
        createdAt = observedAt,
    )
}
