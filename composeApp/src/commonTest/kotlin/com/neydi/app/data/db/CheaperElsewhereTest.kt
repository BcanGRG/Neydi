package com.neydi.app.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.ui.components.PriceHint
import com.neydi.app.ui.list.CheaperChip
import com.neydi.app.ui.list.provablySamePack
import com.neydi.app.ui.list.toSections
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * "Baska markette ucuz" cipi (F5.5, karar 41) - GERCEK veritabaniyla.
 *
 * ## Testin cikis noktasi bir CIHAZ HATASI
 *
 * Kullanici 22 Agustos'ta ayni sutu yedi dakika arayla iki markette cekti:
 * A101 36,00 ve BIM 62,50. Liste satiri bunu *"onceki 36,00 · %74 artis"*
 * diye gosterdi - oysa hicbir fiyat artmamisti, A101 sadece ucuzdu. Bir ekran
 * derindeki "Nerede ucuz" ayni iki sayiyi DOGRU okuyordu. Uygulama ayni veri
 * hakkinda birbiriyle celisen iki cumle kuruyordu ve yanlis olan, once
 * gorulendi.
 *
 * [aCheaperRivalChainWinsOverTheTrend] tam o vakayi kuruyor - sayilar
 * cihazdan, uydurma degil.
 *
 * ## SQL taklit EDILMIYOR
 *
 * Rakip gozlemi bulan sey dort correlated alt sorgu ve her birinde ic ice bir
 * "son gozlemin marketi" sorgusu var. Asil risk orada: Kotlin tarafi kusursuz
 * calisip yanlis kolonu okuyabilir. Bu yuzden testler `observeList`i gercekten
 * kosuyor.
 */
class CheaperElsewhereTest {

    private val home = DEFAULT_HOUSEHOLD_ID
    private val now = 10_000_000_000L
    private val day = 24L * 60 * 60 * 1000

    /** Cipin tazelik siniri - uretimdeki cagiranin verdigi degerin aynisi. */
    private val fresh get() = now - CheaperChip.FRESH_MS

    private suspend fun setup(): Pair<NeydiDatabase, String> {
        val db = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
            factory = { NeydiDatabaseConstructor.initialize() },
        ).setDriver(BundledSQLiteDriver()).build()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        db.storeDao().insert(
            Store(id = "s-bim", householdId = home, name = "BİM", chain = "bim", createdAt = 0),
        )
        db.storeDao().insert(
            Store(id = "s-a101", householdId = home, name = "A101", chain = "a101", createdAt = 0),
        )
        val trip = Trip(id = "t1", householdId = home, startedAt = 0, createdAt = 0)
        db.tripDao().insert(trip)
        return db to trip.id
    }

    private suspend fun lineFor(db: NeydiDatabase, tripId: String, name: String): String {
        val repo = ListRepository(
            tripDao = db.tripDao(), tripLineDao = db.tripLineDao(),
            productDao = db.productDao(), clock = { 0 }, newId = { "line-$name" },
        )
        val product = Product(
            id = "p-$name", householdId = home, name = name, matchKey = name.lowercase(),
            categoryId = "temel-gida", defaultUnit = "adet", createdAt = 0,
        )
        db.productDao().insert(product)
        repo.add(householdId = home, tripId = tripId, product = product, memberId = "m1")
        return product.id
    }

    /**
     * AMBALAJ VARSAYILANI BILINEN VE AYNI - bilerek.
     *
     * Cipin ambalaj kapisi KANIT istiyor ([provablySamePack]), yani ambalajsiz
     * bir gozlem cifti cipi zaten dusurur. Varsayilan `null` olsaydi esik
     * testlerinin hepsi YESIL kalirdi ama yanlis sebepten: "5 TL esigi
     * calisiyor" diye okunan satir aslinda "ambalaj bilinmiyor"u olcuyor
     * olurdu. Esigi olcmek isteyen test, esikten baska her seyi gecerli
     * kilmak zorunda.
     */
    private suspend fun observe(
        db: NeydiDatabase,
        productId: String,
        minor: Long,
        at: Long,
        store: String? = "s-bim",
        packSize: Double? = 1.0,
        packUnit: String? = "kg",
    ) = db.priceObservationDao().insert(
        PriceObservation(
            id = "o-$productId-$at", householdId = home, productId = productId, storeId = store,
            unitPriceMinor = minor, packSize = packSize, packUnit = packUnit,
            observedAt = at, createdAt = at,
        ),
    )

    /** Butun bolumlerdeki satirlar, urun adiyla aranabilir halde. */
    private suspend fun rows(db: NeydiDatabase, tripId: String) =
        db.tripLineDao().observeList(tripId, fresh).first()
            .toSections(myMemberId = "m1", now = now)
            .sections.flatMap { it.rows }
            .associateBy { it.row.name }

    /**
     * CIHAZDAKI SUT VAKASI: cip ciziliyor VE trend bastiriliyor.
     *
     * Iki iddia birden, cunku karar 41 iki seyi birden soyluyor. Yalnizca
     * cipi dogrulayan bir test, trendin yaninda durmaya devam etmesini
     * gormezdi - hatanin kendisi de tam olarak oydu.
     */
    @Test
    fun aCheaperRivalChainWinsOverTheTrend() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Süt")
        observe(db, p, 3_600, at = now - 2 * day, store = "s-a101", packSize = 1.0, packUnit = "lt")
        observe(db, p, 6_250, at = now - day, store = "s-bim", packSize = 1.0, packUnit = "lt")

        val row = rows(db, trip).getValue("Süt").row
        assertEquals("A101'de 36,00", row.cheaperElsewhere)
        // Trend BASTIRILDI - ama satir kendi odedigi fiyati unutmadi.
        val hint = assertIs<PriceHint.Single>(row.priceHint)
        assertEquals("BİM", hint.store)
    }

    /**
     * YUZDE GECIYOR AMA TL GECMIYOR: cip yok.
     *
     * 3 TL'lik urunde %17 ucuzluk 50 kurustur. Bunu ihbar etmek, kullaniciyi
     * 50 kurus icin baska markete yollamaktir.
     */
    @Test
    fun aBigPercentOnATinyPriceIsNotWorthAChip() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Maydanoz")
        observe(db, p, 250, at = now - 2 * day, store = "s-a101")
        observe(db, p, 300, at = now - day, store = "s-bim")

        assertNull(rows(db, trip).getValue("Maydanoz").row.cheaperElsewhere)
    }

    /**
     * TL GECIYOR AMA YUZDE GECMIYOR: cip yok.
     *
     * 200 TL'lik urunde 6 TL fark gurultudur; ayni etikette bir sonraki hafta
     * kendiliginden kapanabilir.
     */
    @Test
    fun aSmallPercentOnALargePriceIsNotWorthAChip() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Deterjan")
        observe(db, p, 19_400, at = now - 2 * day, store = "s-a101")
        observe(db, p, 20_000, at = now - day, store = "s-bim")

        assertNull(rows(db, trip).getValue("Deterjan").row.cheaperElsewhere)
    }

    /**
     * BAYAT RAKIP: cip yok - ve bu ELEME SQL'DE.
     *
     * Gozlem 20 gunluk; 14 gunluk pencerenin disinda. Filtre Kotlin'de
     * olsaydi bu satir once "en ucuz" diye secilir, sonra atilirdi.
     */
    @Test
    fun aStaleRivalObservationIsFilteredOutInSql() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Zeytinyağı")
        observe(db, p, 20_000, at = now - 20 * day, store = "s-a101")
        observe(db, p, 40_000, at = now - day, store = "s-bim")

        assertNull(rows(db, trip).getValue("Zeytinyağı").row.cheaperElsewhere)
    }

    /**
     * TAZE RAKIP BAYATIN YERINI ALIYOR.
     *
     * Ayni urunde hem 20 gunluk 200 TL hem 3 gunluk 300 TL gozlem var. SQL
     * penceresi bayat olani hic getirmiyor, taze olan cipi kuruyor. Bu test
     * [aStaleRivalObservationIsFilteredOutInSql] ile birlikte okunmali: tek
     * basina "bayat eleniyor" demek, "bayat yuzunden gecerli alternatif de
     * kayboluyor" ihtimalini disarida birakmaz.
     */
    @Test
    fun aFreshRivalIsUsedEvenWhenAnOlderCheaperOneExists() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Bulgur")
        observe(db, p, 20_000, at = now - 20 * day, store = "s-a101")
        observe(db, p, 30_000, at = now - 3 * day, store = "s-a101")
        observe(db, p, 40_000, at = now - day, store = "s-bim")

        assertEquals("A101'de 300,00", rows(db, trip).getValue("Bulgur").row.cheaperElsewhere)
    }

    /**
     * AMBALAJ FARKLIYSA CIP YOK.
     *
     * 5 lt'lik bidon 1 lt'lik siseden ucuz degildir, sadece baska bir seydir.
     * Trendin ambalaj dalinin engelledigi yalanin market eksenindeki ikizi.
     */
    @Test
    fun aDifferentPackSizeIsNotACheaperAlternative() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Ayçiçek Yağı")
        observe(db, p, 10_000, at = now - 2 * day, store = "s-a101", packSize = 5.0, packUnit = "lt")
        observe(db, p, 20_000, at = now - day, store = "s-bim", packSize = 1.0, packUnit = "lt")

        assertNull(rows(db, trip).getValue("Ayçiçek Yağı").row.cheaperElsewhere)
    }

    /**
     * BIR YANIN AMBALAJI OKUNMAMISSA CIP YOK - CIHAZDAKI YOGURT VAKASI.
     *
     * 22 Agustos'ta yogurdun son gozleminin ambalaji okunamamisti (kullanici
     * 1,5 / 2 / 3 kg'lik uc boy cekmisti); A101'deki en ucuz gozlem ise 250
     * ml'lik kaseydi. Gevsek bir ambalaj kurali buraya *"A101'de 49,00"*
     * yazardi ve trendin yalanini cipin yalaniyla degistirirdi.
     *
     * Bu testin [aDifferentPackSizeIsNotACheaperAlternative]'den farki:
     * orada boylar BILINIYOR ve farkli, burada biri hic BILINMIYOR. Iki ayri
     * kural, cunku `null` "farkli" demek degil - ve yine de cip cizilmiyor.
     */
    @Test
    fun anUnreadPackOnEitherSideIsNotProofEnoughForAChip() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Yoğurt")
        observe(db, p, 4_900, at = now - 2 * day, store = "s-a101", packSize = 250.0, packUnit = "ml")
        observe(db, p, 19_200, at = now - day, store = "s-bim", packSize = null, packUnit = null)

        assertNull(rows(db, trip).getValue("Yoğurt").row.cheaperElsewhere)
    }

    /**
     * SON GOZLEM MARKETSIZSE CIP YOK.
     *
     * "Baska markette" cumlesinin ilk sarti bir "bu market"in olmasi.
     * Kullanici aceleyle market secmeden kaydettiginde karsilastirmanin
     * dayanagi yok.
     */
    @Test
    fun withoutAStoreOnTheLastObservationThereIsNoElsewhere() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Pirinç")
        observe(db, p, 3_000, at = now - 2 * day, store = "s-a101")
        observe(db, p, 9_000, at = now - day, store = null)

        assertNull(rows(db, trip).getValue("Pirinç").row.cheaperElsewhere)
    }

    /**
     * LISTE BASINA EN FAZLA UC, MUTLAK TL TASARRUFUNA GORE.
     *
     * Dorduncu aday esikleri GECIYOR - dusme sebebi kontenjan. Esikleri
     * gecmeyen bir dorduncuyle kurulsaydi test siralamayi degil elemeyi
     * dogrulardi ve "en fazla 3" satiri silinse bile yesil kalirdi.
     */
    @Test
    fun atMostThreeChipsSurviveAndTheyAreTheBiggestSavings() = runTest {
        val (db, trip) = setup()
        // Tasarruflar: 30 TL, 20 TL, 10 TL, 6 TL. Dorduncu hem %12 hem 6 TL,
        // yani iki esigi de geciyor.
        val plan = listOf(
            Triple("Kahve", 10_000L, 7_000L),
            Triple("Çay", 10_000L, 8_000L),
            Triple("Şeker", 10_000L, 9_000L),
            Triple("Tuz", 5_000L, 4_400L),
        )
        plan.forEach { (name, paid, rival) ->
            val p = lineFor(db, trip, name)
            observe(db, p, rival, at = now - 2 * day, store = "s-a101")
            observe(db, p, paid, at = now - day, store = "s-bim")
        }

        val all = rows(db, trip)
        assertEquals("A101'de 70,00", all.getValue("Kahve").row.cheaperElsewhere)
        assertEquals("A101'de 80,00", all.getValue("Çay").row.cheaperElsewhere)
        assertEquals("A101'de 90,00", all.getValue("Şeker").row.cheaperElsewhere)
        assertNull(all.getValue("Tuz").row.cheaperElsewhere)
    }

    /**
     * AYNI MARKETTEKI DAHA UCUZ GOZLEM CIP DEGIL.
     *
     * Cip "oraya ugra" diyor; ayni markette ugranacak baska bir yer yok. Bu
     * durum trendin isi ve trend bastirilmadan kaliyor.
     */
    @Test
    fun aCheaperObservationAtTheSameChainIsATrendNotAChip() = runTest {
        val (db, trip) = setup()
        val p = lineFor(db, trip, "Makarna")
        observe(db, p, 1_000, at = now - 2 * day, store = "s-bim")
        observe(db, p, 2_000, at = now - day, store = "s-bim")

        val row = rows(db, trip).getValue("Makarna").row
        assertNull(row.cheaperElsewhere)
        assertIs<PriceHint.Trend>(row.priceHint)
    }
}
