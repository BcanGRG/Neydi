package com.neydi.app.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.ui.product.toPriceSection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Ekran 5'in fiyat bolumu (E17) - gercek sorgu, gercek veritabani.
 *
 * Kabul olcutu tasarimin kendi ornegi: `BİM · Dost · 100 TL` uste,
 * `Migros · Pınar · 130 TL` altta.
 */
class ProductPriceSectionTest {

    // FIYATLAR ARTIK "TL" TASIYOR ve bu testin degismesi DOGRU cozumdu.
    //
    // Test, kusurun kendisini kodluyordu: `formatChipMinor` para birimini
    // dusuruyor ve Money.kt'nin kendi kurali bu istisnayi YALNIZCA 24dp'lik
    // fiyat cipine veriyor. Bolum satirlari 15sp/14sp tablo satiri, cip degil;
    // yani kod cip muafiyetini cipin disinda uyguluyordu. Beklentiyi
    // guncellemek, dogrulanmis bir bulguyu geri almaktan iyi.

    private val home = DEFAULT_HOUSEHOLD_ID
    private val day = 24L * 60 * 60 * 1000

    private suspend fun ready(): NeydiDatabase {
        val db = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
            factory = { NeydiDatabaseConstructor.initialize() },
        ).setDriver(BundledSQLiteDriver()).build()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        listOf("bim" to "BİM", "migros" to "Migros").forEach { (chain, name) ->
            db.storeDao().insert(
                Store(id = "s-$chain", householdId = home, name = name, chain = chain, createdAt = 0),
            )
        }
        db.productDao().insert(
            Product(
                id = "p1", householdId = home, name = "Süt", matchKey = "sut",
                categoryId = "temel-gida", defaultUnit = "adet", createdAt = 0,
            ),
        )
        return db
    }

    private suspend fun observe(
        db: NeydiDatabase,
        minor: Long,
        at: Long,
        store: String? = "s-bim",
        brand: String? = null,
        packSize: Double? = null,
        packUnit: String? = null,
        id: String = "o-$at-$minor",
    ) = db.priceObservationDao().insert(
        PriceObservation(
            id = id, householdId = home, productId = "p1", storeId = store,
            unitPriceMinor = minor, brand = brand, packSize = packSize, packUnit = packUnit,
            observedAt = at, createdAt = at,
        ),
    )

    private suspend fun section(db: NeydiDatabase) =
        db.priceObservationDao().history(home, "p1", 9).first().toPriceSection()

    /**
     * SILINEN GOZLEM GECMISTEN DUSUYOR (karar 46).
     *
     * `deletedAt` kolonu bastan beri vardi ve hicbir yerden YAZILMIYORDU; yani
     * "yumusak silme" bu tabloda bir sozden ibaretti. Test iki seyi birden
     * kilitliyor: silme yaziliyor VE sorgu onu suzuyor. Ikincisi olmadan
     * silinen satir ekranda durmaya devam ederdi.
     */
    @Test
    fun aDeletedObservationLeavesTheHistory() = runTest {
        val db = ready()
        observe(db, 10_000, day, id = "keep")
        observe(db, 438_900, 2 * day, id = "wrong")
        assertEquals(2, section(db).history.size)

        db.priceObservationDao().softDelete("wrong", 3 * day)
        assertEquals(listOf("keep"), section(db).history.map { it.id })

        // BES SANIYELIK GERI DONUS HAKKI: satiri AYNEN geri getiriyor.
        db.priceObservationDao().undoDelete("wrong")
        assertEquals(setOf("keep", "wrong"), section(db).history.map { it.id }.toSet())
    }

    /** Gozlem yoksa bolum HIC cizilmiyor - "fiyat yok" da yazilmiyor. */
    @Test
    fun noObservationsMeansNoSection() = runTest {
        assertTrue(section(ready()).isEmpty)
    }

    /**
     * TEK MARKET, IKI MARKA - bolum CIZILIYOR (karar 58).
     *
     * Esik once iki farkli MARKET sayiyordu ve bu hal dusuyordu: BIM'de Dost
     * ile Pinar'i karsilastiran biri "nerede ucuz" bolumunu goremiyordu. Oysa
     * kullanicinin sorusu "hangisini alayim" ve iki marka o soruya tam olarak
     * cevap veriyor. Esik artik SATIR sayiyor, tipki bolumun kendi cizimi gibi.
     */
    @Test
    fun twoBrandsAtOneStoreAreAComparison() = runTest {
        val db = ready()
        observe(db, 10_000, day, store = "s-bim", brand = "Dost")
        observe(db, 13_000, 2 * day, store = "s-bim", brand = "Pınar")
        val rows = section(db).cheapest
        assertEquals(2, rows.size)
        assertEquals("Dost", rows.first().brand)
    }

    /** TEK SATIR karsilastirma DEGIL: kendisiyle kiyaslanan tek secenek. */
    @Test
    fun oneRowIsNotAComparison() = runTest {
        val db = ready()
        observe(db, 10_000, day, store = "s-bim", brand = "Dost")
        observe(db, 11_000, 2 * day, store = "s-bim", brand = "Dost")
        assertTrue(section(db).cheapest.isEmpty())
    }

    /**
     * TASARIMIN KENDI ORNEGI: iki market, iki marka, ucuz olan ustte.
     */
    @Test
    fun cheapestFirstAcrossStoresAndBrands() = runTest {
        val db = ready()
        observe(db, 13_000, at = day, store = "s-migros", brand = "Pınar")
        observe(db, 10_000, at = 2 * day, store = "s-bim", brand = "Dost")

        val rows = section(db).cheapest
        assertEquals(2, rows.size)
        assertEquals("BİM", rows[0].store)
        assertEquals("Dost", rows[0].brand)
        assertEquals("100,00 TL", rows[0].price)
        assertEquals("Migros", rows[1].store)
        assertEquals("Pınar", rows[1].brand)
        assertEquals("130,00 TL", rows[1].price)
    }

    /**
     * AYNI MARKETTEN IKI MARKA = IKI SATIR (karar 26).
     *
     * Yalnizca markete gore gruplamak "BIM'de 100 TL" derdi ve hangi marka
     * oldugunu soylemezdi - oysa fiyat farkinin buyuk kismi marka farki.
     */
    @Test
    fun twoBrandsInOneStoreAreTwoRows() = runTest {
        val db = ready()
        observe(db, 9_000, at = day, store = "s-bim", brand = "Dost", id = "a")
        observe(db, 12_000, at = 2 * day, store = "s-bim", brand = "Sek", id = "b")
        observe(db, 15_000, at = 3 * day, store = "s-migros", brand = "Pınar", id = "c")

        val rows = section(db).cheapest
        assertEquals(3, rows.size)
        assertEquals(listOf("Dost", "Sek", "Pınar"), rows.map { it.brand })
    }

    /**
     * HER CIFT ICIN EN SON FIYAT, ortalama DEGIL.
     *
     * Kullanicinin sorusu "simdi nerede ucuz". Ortalama alsaydik zam yapmis
     * bir marketi ucuz gostermeye devam ederdik.
     */
    @Test
    fun thePairKeepsItsLatestPriceNotAnAverage() = runTest {
        val db = ready()
        observe(db, 5_000, at = day, store = "s-bim", brand = "Dost", id = "eski")
        observe(db, 20_000, at = 5 * day, store = "s-bim", brand = "Dost", id = "yeni")
        observe(db, 15_000, at = 2 * day, store = "s-migros", brand = "Pınar", id = "c")

        val rows = section(db).cheapest
        assertEquals("Migros", rows[0].store, "BIM zamlandi, artik ucuz degil")
        assertEquals("200,00 TL", rows.single { it.store == "BİM" }.price)
    }

    /**
     * TEK MARKETTE BOLUM CIZILMIYOR.
     *
     * "Nerede ucuz" tek market varken cevabi olmayan bir soru; basligi bos
     * cizmek olmayan bir isi varmis gibi gosterirdi. Gecmis yine de dolu.
     */
    @Test
    fun oneStoreDrawsNoCheapestSection() = runTest {
        val db = ready()
        observe(db, 10_000, at = day, id = "a")
        observe(db, 11_000, at = 2 * day, id = "b")

        val s = section(db)
        assertTrue(s.cheapest.isEmpty())
        assertEquals(2, s.history.size, "gecmis yine de cizilmeli")
    }

    /**
     * SPARKLINE UC GOZLEMDEN ONCE CIZILMIYOR.
     *
     * Iki nokta bir dogru parcasi cizer ve olmayan bir trendi varmis gibi
     * gosterir.
     */
    @Test
    fun theSparklineNeedsThreeObservations() = runTest {
        val db = ready()
        observe(db, 1_000, at = day, id = "a")
        observe(db, 2_000, at = 2 * day, id = "b")
        assertTrue(section(db).sparkline.isEmpty())

        observe(db, 3_000, at = 3 * day, id = "c")
        assertEquals(listOf(1_000f, 2_000f, 3_000f), section(db).sparkline, "eskiden yeniye")
    }

    /** Gecmis YENIDEN ESKIYE ve dokuzla sinirli. */
    @Test
    fun theHistoryIsNewestFirstAndCappedAtNine() = runTest {
        val db = ready()
        repeat(12) { i -> observe(db, (i + 1) * 1_000L, at = (i + 1) * day, id = "o$i") }

        val history = section(db).history
        assertEquals(9, history.size)
        assertEquals("120,00 TL", history.first().price, "en yeni basta olmali")
    }

    /**
     * MARKETI SECILMEMIS GOZLEM GECMISTE GORUNUYOR.
     *
     * Sorgu `LEFT JOIN`; `INNER` olsaydi kullanicinin acele edip market
     * secmedigi cekimler sessizce kaybolurdu - hem de kendi kaydettikleri.
     */
    @Test
    fun aStorelessObservationIsStillInTheHistory() = runTest {
        val db = ready()
        observe(db, 4_000, at = day, store = null)

        val history = section(db).history
        assertEquals(1, history.size)
        assertEquals("market yok", history.single().store)
    }

    /** Silinmis gozlem ne gecmiste ne "nerede ucuz"da. */
    @Test
    fun aDeletedObservationIsInvisible() = runTest {
        val db = ready()
        observe(db, 10_000, at = day, id = "a")
        db.priceObservationDao().insert(
            PriceObservation(
                id = "b", householdId = home, productId = "p1", storeId = "s-migros",
                unitPriceMinor = 1, observedAt = 2 * day, createdAt = 2 * day, deletedAt = 3 * day,
            ),
        )

        val s = section(db)
        assertEquals(1, s.history.size)
        assertTrue(s.cheapest.isEmpty(), "silinen gozlem ikinci marketi yaratmamali")
    }

    /**
     * GECMIS SATIRININ TARIHI KISA AY ADIYLA - dar sutunun sarti.
     *
     * Bu testin varlik sebebi bir cihaz hatasi. Satir tam ay adi yaziyordu
     * ("22 Ağustos"), tarih sutunu ise sabit ve dar; metnin sarmaya karsi
     * kilidi de yoktu. Sonuc ekranda `22 / AğustoBİM / s` diye okunuyordu:
     * tarih uc satira boluniyor ve ikinci satiri market adiyla bitisiyordu.
     *
     * ONIZLEME NEDEN YAKALAMADI: fikstürleri `"6 Ağu"`, `"14 Ağu"` gibi KISA
     * degerler tasiyordu - yani maketin kopyasiydilar, uretim yolunun degil.
     * Bu yuzden nobetci `toPriceSection`in KENDI ciktisina bakiyor; fikstüre
     * bakan bir nobetci ayni hatayi bir kez daha kacirirdi.
     */
    @Test
    fun theHistoryRowDateUsesTheShortMonthName() = runTest {
        val db = ready()
        // On iki ay birden: hangi aya denk geldigi cihazin saat dilimine bagli
        // ve iddia ONA bagli OLMAMALI. Kilitlenen sey gun/ay degil, BICIM.
        (0 until 12).forEach { m -> observe(db, 6_250, at = (m * 31L + 1) * day, id = "m$m") }

        section(db).history.forEach { row ->
            val month = row.date.substringAfter(' ')
            assertEquals(3, month.length, "dar sutun UC HARFLIK ay istiyor: ${row.date}")
            assertTrue(
                MONTH_LONG.none { it == month },
                "tam ay adi dar sutunda satiri uce boluyor: ${row.date}",
            )
        }
    }



    /** Ambalaj biliniyorsa satirda gosteriliyor. */
    @Test
    fun aKnownPackIsShownOnTheRow() = runTest {
        val db = ready()
        observe(db, 10_000, at = day, store = "s-bim", packSize = 4.0, packUnit = "lt", id = "a")
        observe(db, 13_000, at = 2 * day, store = "s-migros", id = "b")

        val rows = section(db).cheapest
        assertEquals("4 lt", rows.single { it.store == "BİM" }.pack)
        assertEquals(null, rows.single { it.store == "Migros" }.pack)
    }
}

/** Tam ay adlari - kisa bicimin onlardan BIRI OLMADIGINI gostermek icin. */
private val MONTH_LONG = listOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık",
)
