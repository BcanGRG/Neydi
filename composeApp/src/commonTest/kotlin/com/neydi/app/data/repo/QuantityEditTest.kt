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
import kotlin.test.assertNull

/**
 * Miktarin kendi yazma yolu (kararlar 107-109).
 *
 * ## Neden ayri bir dosya
 *
 * `ListRepositoryTest` eklemenin ve gezinin testi. Miktar duzenleme bu turda
 * acilan YENI bir kapi - `add` ile arasindaki fark (delta degil mutlak) tam da
 * karistirilmasi kolay olan sey, ve iki kapinin iddialarini ayri tutmak
 * karisikligi ilk basta engelliyor.
 */
class QuantityEditTest {

    private val home = DEFAULT_HOUSEHOLD_ID

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase, now: Long = 1_000L): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            productDao = db.productDao(),
            clock = { now },
            newId = { "id-${++n}" },
        )
    }

    private suspend fun row(db: NeydiDatabase, r: ListRepository, unit: String = "kg") =
        r.openOrGetActiveTrip(home, "m1").let { trip ->
            r.add(
                home,
                trip.id,
                r.findOrCreateProduct(home, "Domates", "meyve-sebze", unit),
                memberId = "m1",
            ).line
        }

    /**
     * `bootstrap` KULLANILIYOR, elle hane kurulumu degil.
     *
     * Liste sorgusu kategoriye JOIN yapiyor ve `CategoryDao`nun yazma yolu yok -
     * kategoriler ham SQL ile tohumlaniyor. Kategorisi olmayan satir sorgudan
     * hic donmez, yani elle kurulan bir hanede "gecerli birim" iddiasi
     * sinanamaz.
     */
    private suspend fun prepare(db: NeydiDatabase) {
        db.bootstrap(newId = { "m1" }, clock = { 0 })
    }

    /**
     * YAZMA MUTLAK, DELTA DEGIL.
     *
     * `add` bir delta yoluydu ve sayac ondan ayrilmak zorunda: "+" ile "-"
     * arasinda gidip gelen bir kontrolde delta biriktiren bir yol, kaybolan
     * tek bir dokunusta sessizce yanlis sayiya oturur.
     */
    @Test
    fun setQuantityWritesAnAbsoluteValue() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val line = row(db, r)

        r.setQuantity(line.id, 4.0, unitOverride = null)
        r.setQuantity(line.id, 2.5, unitOverride = null)

        assertEquals(2.5, r.rows(home).first().single().quantity)
    }

    /**
     * LWW DAMGASI DUSMUYOR.
     *
     * Miktar tam da esler arasinda cakisan sey: iki kisi ayni satiri ayni gun
     * duzenliyor. `updatedAt` yazilmazsa Faz 7'nin birlestirmesi hangi degerin
     * daha yeni oldugunu bilemez ve duzenleme sessizce kaybolur. `add` bu
     * alani hic yazmiyordu - orada onemli degildi, burada oyle.
     */
    @Test
    fun aQuantityEditStampsUpdatedAt() = runTest {
        val db = db(); prepare(db)
        val r = repo(db, now = 5_000L)
        val line = row(db, r)
        assertNull(line.updatedAt, "ekleme damga yazmiyordu, varsayim degismis")

        r.setQuantity(line.id, 3.0, unitOverride = null)

        assertEquals(5_000L, db.tripLineDao().byId(line.id)?.updatedAt)
    }

    /**
     * BIRIM SECIMI SATIRA OZEL - KATALOG YERINDE KALIYOR (karar 108).
     *
     * Urun Detayi ciplerin altina *"katalog: kg"* yaziyor ve o cumlenin dogru
     * kalmasi bu iddiaya bagli: secim urune yazilsaydi, bir satirda yapilan
     * degisiklik ayni urunun butun gelecek satirlarini da degistirirdi.
     */
    @Test
    fun pickingAUnitLeavesTheCatalogueDefaultAlone() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val line = row(db, r, unit = "kg")

        r.setQuantity(line.id, 3.0, unitOverride = "adet")

        assertEquals("adet", db.tripLineDao().byId(line.id)?.unitOverride)
        // Satirin kendi `unit`i de, urunun varsayilani da DOKUNULMAMIS.
        assertEquals("kg", db.tripLineDao().byId(line.id)?.unit)
        assertEquals("kg", db.productDao().byId(line.productId)?.defaultUnit)
    }

    /**
     * SECIM GERI ALINABILIYOR: `null` yazmak katalogu izlemeye dondurur.
     *
     * Ustune yazma degil SILME oldugu icin ayri bir yol gerekmiyor - ve
     * gerekmemesi onemli, cunku ikinci bir yol "geri al"in ne demek oldugunu
     * ikinci kez tanimlamak olurdu.
     */
    @Test
    fun clearingTheOverrideReturnsTheRowToTheCatalogue() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val line = row(db, r)

        r.setQuantity(line.id, 2.0, unitOverride = "adet")
        r.setQuantity(line.id, 2.0, unitOverride = null)

        assertNull(db.tripLineDao().byId(line.id)?.unitOverride)
    }

    /**
     * OKUMA TARAFI GECERLI BIRIMI VERIYOR.
     *
     * Sorgu `COALESCE(unitOverride, unit)` cozuyor; iki alani yukari tasiyip
     * her cizim yerinde `?:` yazmak, bir yerde unutmaya davet olurdu - ve
     * unutuldugu yer rozet olsaydi kullanici "3 kg" yerine "3 adet" okurdu.
     */
    @Test
    fun theListReadsTheEffectiveUnit() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val line = row(db, r, unit = "kg")

        r.setQuantity(line.id, 3.0, unitOverride = "adet")

        val projected = db.tripLineDao().observeList(line.tripId, freshAfter = 0L).first().single()
        assertEquals("adet", projected.unit)
        assertEquals("adet", projected.unitOverride)
    }
}
