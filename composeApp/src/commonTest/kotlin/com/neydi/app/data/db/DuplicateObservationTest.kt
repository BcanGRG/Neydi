package com.neydi.app.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Mukerrer gozlem korumasi (tasarim: etiket akisi, 60 sn).
 *
 * BU TEST E15'TEN ONCE YAZILDI ve bu kasitli. Koruma yazma yolunun ICINDE
 * yasiyor; yol dogduktan sonra eklenirse "zaten calisiyordu" sanilan bir sey
 * icin geriye donuk test yazmak gerekir - ve o testin gercekten isirdigi hicbir
 * zaman gorulmez. Simdi yazilinca kural once, cagiran sonra geliyor.
 *
 * En kritik vaka [nullStoreStillDeduplicates]: SQL'de `NULL = NULL` yanlistir,
 * yani `=` ile yazilmis bir kosul marketi secilmemis cekimlerde SESSIZCE
 * calismazdi - hem de korumanin en cok gerektigi durumda.
 */
class DuplicateObservationTest {

    private val home = DEFAULT_HOUSEHOLD_ID

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private suspend fun hazir(): NeydiDatabase {
        val db = db()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.productDao().insert(urun("p1", "Yoğurt", "yogurt"))
        db.productDao().insert(urun("p2", "Süt", "sut"))
        return db
    }

    @Test
    fun firstObservationIsAlwaysWritten() = runTest {
        val db = hazir()
        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o1", at = 10_000))

        assertTrue(yazildi)
        assertEquals(1, sayi(db))
    }

    /** Ayni market + urun + fiyat, 60 sn icinde: ikincisi yazilmiyor. */
    @Test
    fun identicalObservationWithinTheWindowIsSkipped() = runTest {
        val db = hazir()
        db.priceObservationDao().insertUnlessRecentDuplicate(gozlem("o1", at = 100_000))

        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o2", at = 130_000))

        assertFalse(yazildi)
        assertEquals(1, sayi(db))
    }

    /**
     * Pencerenin DISI yaziliyor - ve bu korumanin asil sinirini cizen vaka.
     *
     * Ayni fiyati iki hafta sonra tekrar gormek GERCEK bir gozlemdir: fiyatin
     * degismedigini soyluyor. Kalici tekillestirme o bilgiyi silerdi.
     */
    @Test
    fun sameObservationOutsideTheWindowIsWritten() = runTest {
        val db = hazir()
        db.priceObservationDao().insertUnlessRecentDuplicate(gozlem("o1", at = 100_000))

        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o2", at = 100_000 + DUPLICATE_WINDOW_MS + 1))

        assertTrue(yazildi)
        assertEquals(2, sayi(db))
    }

    /** Sinir tam ustunde: 60 sn'nin kendisi HALA mukerrer. */
    @Test
    fun theBoundaryItselfCounts() = runTest {
        val db = hazir()
        db.priceObservationDao().insertUnlessRecentDuplicate(gozlem("o1", at = 100_000))

        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o2", at = 100_000 + DUPLICATE_WINDOW_MS))

        assertFalse(yazildi)
    }

    /**
     * MARKETI SECILMEMIS CEKIM - `NULL = NULL` tuzagi.
     *
     * `storeId = :storeId` yazilsaydi bu test kirilirdi: SQL'de iki NULL esit
     * degildir, yani mukerrer bulunamaz ve ikinci satir yazilirdi. `IS` ikisini
     * esit sayiyor.
     */
    @Test
    fun nullStoreStillDeduplicates() = runTest {
        val db = hazir()
        db.priceObservationDao().insertUnlessRecentDuplicate(gozlem("o1", store = null, at = 100_000))

        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o2", store = null, at = 110_000))

        assertFalse(yazildi)
        assertEquals(1, sayi(db))
    }

    /** Farkli fiyat = farkli gozlem. Etiket degismis, kayit hakki var. */
    @Test
    fun differentPriceIsNotADuplicate() = runTest {
        val db = hazir()
        db.priceObservationDao().insertUnlessRecentDuplicate(gozlem("o1", price = 10_000, at = 100_000))

        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o2", price = 12_500, at = 110_000))

        assertTrue(yazildi)
        assertEquals(2, sayi(db))
    }

    /** Farkli market = farkli gozlem. Karsilastirmanin ekseni tam olarak bu. */
    @Test
    fun differentStoreIsNotADuplicate() = runTest {
        val db = hazir()
        db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o1", store = "store-seed-bim", at = 100_000))

        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o2", store = "store-seed-a101", at = 110_000))

        assertTrue(yazildi)
        assertEquals(2, sayi(db))
    }

    /** Farkli urun = farkli gozlem, fiyatlari ayni olsa bile. */
    @Test
    fun differentProductIsNotADuplicate() = runTest {
        val db = hazir()
        db.priceObservationDao().insertUnlessRecentDuplicate(gozlem("o1", product = "p1", at = 100_000))

        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o2", product = "p2", at = 110_000))

        assertTrue(yazildi)
        assertEquals(2, sayi(db))
    }

    /**
     * SILINMIS GOZLEM ENGEL DEGIL.
     *
     * Kullanici bir gozlemi silip ayni etiketi tekrar cekiyorsa niyeti bellidir.
     * Koruma kullanicinin kazasina karsi, kararina karsi degil.
     */
    @Test
    fun deletedObservationDoesNotBlockTheRewrite() = runTest {
        val db = hazir()
        db.priceObservationDao().insert(gozlem("o1", at = 100_000).copy(deletedAt = 105_000))

        val yazildi = db.priceObservationDao()
            .insertUnlessRecentDuplicate(gozlem("o2", at = 110_000))

        assertTrue(yazildi)
    }

    /** Seri cekim: ayni etikete uc kez basmak tek satir birakiyor. */
    @Test
    fun burstOfThreeLeavesOneRow() = runTest {
        val db = hazir()
        val sonuclar = listOf(100_000L, 101_500L, 103_000L).mapIndexed { i, t ->
            db.priceObservationDao().insertUnlessRecentDuplicate(gozlem("o$i", at = t))
        }

        assertEquals(listOf(true, false, false), sonuclar)
        assertEquals(1, sayi(db))
    }

    private suspend fun sayi(db: NeydiDatabase): Int =
        db.priceObservationDao().countRecentDuplicates(
            householdId = home, productId = "p1", storeId = "store-seed-bim",
            unitPriceMinor = 10_000, since = 0,
        ) + db.priceObservationDao().countRecentDuplicates(
            householdId = home, productId = "p1", storeId = null,
            unitPriceMinor = 10_000, since = 0,
        ) + db.priceObservationDao().countRecentDuplicates(
            householdId = home, productId = "p1", storeId = "store-seed-a101",
            unitPriceMinor = 10_000, since = 0,
        ) + db.priceObservationDao().countRecentDuplicates(
            householdId = home, productId = "p2", storeId = "store-seed-bim",
            unitPriceMinor = 10_000, since = 0,
        ) + db.priceObservationDao().countRecentDuplicates(
            householdId = home, productId = "p1", storeId = "store-seed-bim",
            unitPriceMinor = 12_500, since = 0,
        )

    private fun urun(id: String, name: String, key: String) = Product(
        id = id, householdId = home, name = name, matchKey = key,
        categoryId = "temel-gida", defaultUnit = "adet", createdAt = 0,
    )

    private fun gozlem(
        id: String,
        product: String = "p1",
        store: String? = "store-seed-bim",
        price: Long = 10_000,
        at: Long,
    ) = PriceObservation(
        id = id, householdId = home, productId = product, storeId = store,
        unitPriceMinor = price, observedAt = at, createdAt = at,
    )
}
