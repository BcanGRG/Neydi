package com.neydi.app.data.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.matchKey
import com.neydi.app.data.ocr.TagFixtures
import com.neydi.app.data.ocr.readTagFields
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.store.chainKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Etiketten gozleme - UCTAN UCA, gercek OCR verisiyle ve gercek veritabaniyla.
 *
 * Kamera yok, ekran yok, cihaz yok. Girdi 80 gercek etiketin ham OCR ciktisi,
 * cikti `price_observation` satirlari. Aradaki her sey gercek: gramer, zincir
 * kapisi, urun cozumleme, mukerrer koruma.
 *
 * Bu testin var olabilmesi bir plan bulgusuydu - `commonTest` gercek bir
 * bellek-ici Room kurabiliyor, yani E15'in cihaz gerektiren kismi
 * sanildigindan cok daha kucuk.
 */
class TagObservationWriteTest {

    private val home = DEFAULT_HOUSEHOLD_ID
    private val bim = chainKey("BİM")
    private val a101 = chainKey("A101")

    private suspend fun ready(): NeydiDatabase {
        val db = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
            factory = { NeydiDatabaseConstructor.initialize() },
        ).setDriver(BundledSQLiteDriver()).build()
        db.bootstrap(newId = { "m1" }, clock = { 0 })
        return db
    }

    private fun repoOf(db: NeydiDatabase, clock: () -> Long) = ListRepository(
        tripDao = db.tripDao(),
        tripLineDao = db.tripLineDao(),
        productDao = db.productDao(),
        clock = clock,
        newId = { "gen-${clock()}" },
    )

    private suspend fun write(
        db: NeydiDatabase,
        name: String,
        minor: Long,
        storeId: String? = null,
        brand: String? = null,
        packSize: Double? = null,
        packUnit: String? = null,
        tagText: String? = null,
        chain: String? = null,
        at: Long = 1_000,
        id: String = "obs-$at",
    ): Boolean = writeTagObservation(
        repo = repoOf(db) { at },
        catalogSeedDao = db.catalogSeedDao(),
        priceObservationDao = db.priceObservationDao(),
        productAliasDao = db.productAliasDao(),
        householdId = home,
        productName = name,
        priceMinor = minor,
        storeId = storeId,
        brand = brand,
        packSize = packSize,
        packUnit = packUnit,
        tagText = tagText,
        chain = chain,
        at = at,
        newId = { id },
    )

    /**
     * GERCEK BIR BIM ETIKETI, OCR'DAN GOZLEME.
     *
     * `183728` CENTRO GOFRET: manseti 34,00 ve birim fiyati 239,44/kg ile
     * uyusuyor. Fikstur degisirse test dayanaksiz kalmasin diye once okumanin
     * kendisi dogrulaniyor.
     */
    @Test
    fun aRealTagBecomesAnObservation() = runTest {
        val db = ready()
        val fields = readTagFields(TagFixtures.all.getValue("20260817_183728"), bim)
        val price = assertNotNull(fields.price, "fikstur degisti: manset okunmuyor")
        val name = assertNotNull(fields.name, "fikstur degisti: ad okunmuyor")
        assertEquals(3_400L, price.minor)

        assertTrue(write(db, name.name, price.minor, storeId = "s-bim", brand = name.brand))

        val rows = db.priceObservationDao().observeStoreIdsWithObservations(home).first()
        assertEquals(listOf("s-bim"), rows)
    }

    /**
     * GRAMERI OLMAYAN ZINCIRDE YAZILACAK BIR SEY YOK.
     *
     * Vaka ONCE Migros patatesiydi (kapi olmasaydi 4389,00 TL yazilacakti).
     * Migros'un grameri yazilinca o etiket artik DOGRU fiyati veriyor, yani
     * bu testin ornegi Metro'ya tasindi - iddia ayni, yalnizca hangi zincirin
     * cozulmedigi degisti.
     *
     * Testi yazma yolunda tutuyorum cunku ayristirici testinde gecen bir kural
     * cagiran tarafta atlanabilir.
     */
    @Test
    fun anUnreadableChainProducesNothingToWrite() = runTest {
        val fields = readTagFields(
            TagFixtures.all.getValue("20260817_202408"),
            chainKey("Metro"),
        )
        assertNull(fields.price, "kapi acik kalmis")
        assertNull(fields.name)
    }

    /** Ayni etiket iki kez cekilirse ikinci gozlem YAZILMIYOR (60 sn). */
    @Test
    fun theSameTagShotTwiceWritesOneRow() = runTest {
        val db = ready()
        assertTrue(write(db, "PUDRA ŞEKERİ", 2_650, storeId = "s-bim", at = 100_000, id = "a"))
        assertTrue(!write(db, "PUDRA ŞEKERİ", 2_650, storeId = "s-bim", at = 130_000, id = "b"))
        assertEquals(1, countRows(db))
    }

    /**
     * MARKETI SECILMEMIS IKI CEKIM DE MUKERRER SAYILIYOR.
     *
     * SQL'de `NULL = NULL` yanlistir; `IS` kullanilmasaydi koruma tam da en cok
     * gerektigi yerde - kullanici acele ediyor, market secmemis - calismazdi.
     * Kural `countRecentDuplicates` icinde ama etkisi BURADA gorunuyor.
     */
    @Test
    fun twoStorelessShotsStillDeduplicate() = runTest {
        val db = ready()
        assertTrue(write(db, "MAKARNA", 1_990, storeId = null, at = 100_000, id = "a"))
        assertTrue(!write(db, "MAKARNA", 1_990, storeId = null, at = 120_000, id = "b"))
        assertEquals(1, countRows(db))
    }

    /**
     * AYNI FIYAT IKI HAFTA SONRA YENI BIR GOZLEM.
     *
     * Pencere kalici bir tekillestirme DEGIL: fiyatin degismedigini gormek de
     * bir olcum ve fiyat gecmisi ondan besleniyor.
     */
    @Test
    fun theSamePriceTwoWeeksLaterIsANewObservation() = runTest {
        val db = ready()
        val twoWeeks = 14L * 24 * 60 * 60 * 1000
        assertTrue(write(db, "TEREYAĞI", 38_900, storeId = "s-bim", at = 100_000, id = "a"))
        assertTrue(write(db, "TEREYAĞI", 38_900, storeId = "s-bim", at = 100_000 + twoWeeks, id = "b"))
        assertEquals(2, countRows(db))
    }

    /**
     * AYNI URUN ADI IKINCI BIR URUN YARATMIYOR.
     *
     * Gozlem urun kimligine bagli; ad her cekimde yeni bir urune cozulseydi
     * fiyat gecmisi satir basina bir gozleme bolunur, hicbir zaman iki
     * gozlemli bir urun olusmazdi - yani delta cipi hic gorunmezdi.
     */
    @Test
    fun repeatedNamesResolveToOneProduct() = runTest {
        val db = ready()
        write(db, "Zeytinyağı", 20_000, storeId = "s1", at = 1_000, id = "a")
        write(db, "Zeytinyağı", 25_000, storeId = "s2", at = 2_000, id = "b")

        val products = db.productDao().observeAll(home).first()
        assertEquals(1, products.count { it.name == "Zeytinyağı" }, "ad iki urune bolundu")
        assertEquals(2, countRows(db))
    }

    /** Marka bos dizgi degil NULL yaziliyor - "markasiz" ile "okunamadi" ayri. */
    @Test
    fun blankBrandIsStoredAsNull() = runTest {
        val db = ready()
        write(db, "Domates", 4_995, storeId = "s1", brand = "   ", at = 1_000, id = "a")
        assertNull(db.priceObservationDao().allObservations(home).single().brand)
    }

    /** Kaydedilen degerler oldugu gibi duruyor - fiyat, market, marka, an. */
    @Test
    fun theWrittenRowKeepsEveryField() = runTest {
        val db = ready()
        write(db, "PUDRA ŞEKERİ", 2_650, storeId = "s-bim", brand = "ŞAFAK", at = 77_000, id = "a")

        val row = db.priceObservationDao().allObservations(home).single()
        assertEquals(2_650L, row.unitPriceMinor)
        assertEquals("s-bim", row.storeId)
        assertEquals("ŞAFAK", row.brand)
        assertEquals(77_000L, row.observedAt)
    }

    /**
     * ETIKETTEN OKUNAN GRAMAJ VERITABANINA ULASIYOR - F5.7'nin tamami bu.
     *
     * Sema, sorgu ve `PriceHint.PackChanged` dali E16'dan beri hazirdi; eksik
     * olan tek sey buydu. `readTagPack` gramaji okuyor, `TagCaptureViewModel`
     * onu karta tasiyor, kart yazma yoluna veriyordu - HAYIR, vermiyordu:
     * ViewModel'de `pack` kelimesi hic gecmiyordu ve iki kolon hep NULL
     * kaliyordu, yani shrinkflation dali ASLA atesleyemezdi.
     */
    @Test
    fun aRealTagsPackReachesTheDatabase() = runTest {
        val db = ready()
        val fields = readTagFields(TagFixtures.all.getValue("20260821_132811"), a101)
        val pack = assertNotNull(fields.pack, "fikstur degisti: gramaj okunmuyor")
        assertEquals(125.0, pack.size)
        assertEquals("gr", pack.unit)

        write(db, "Cips", 6_450, storeId = "s-a101", packSize = pack.size, packUnit = pack.unit)

        val row = db.priceObservationDao().allObservations(home).single()
        assertEquals(125.0, row.packSize)
        assertEquals("gr", row.packUnit)
    }

    /**
     * CELISKILI ETIKETTEN GRAMAJ DA YAZILMIYOR.
     *
     * `133411` kadrajinda IKI etiket var: manset alttaki SIGNAL'in (64,90),
     * okunan birim fiyat satiri ustteki PARODONTAX'in (2.330,00/lt) ve gramaj
     * yine ustekinin (50 ml). Ucu bir arada tutmuyor.
     *
     * Fiyat zaten dusuyordu. Gramajin da dusmesi F5.7 ile geldi ve gerekcesi
     * ayni: hangi sayinin yanlis oldugunu bilmiyoruz. Yanlis bir gramaj yalnizca
     * bir sayi degil, KALICI BIR IDDIA uretir - *"ambalaj kuculdu"*.
     */
    @Test
    fun aContradictedTagWritesNoPack() = runTest {
        val fields = readTagFields(TagFixtures.all.getValue("20260821_133411"), a101)
        assertNull(fields.price, "capraz kontrol acik kalmis")
        assertNull(fields.pack, "celiskili etiketten gramaj sizdi")
    }

    /**
     * BOY VE BIRIM IKISI BIRLIKTE ya da HIC.
     *
     * Birimsiz bir `900` gram mi mililitre mi soylemiyor; sonraki gozlemle
     * karsilastirildiginda "900 -> 900" tutar ama iki AYRI birim olabilir.
     */
    @Test
    fun aPackSizeWithoutItsUnitIsNotWritten() = runTest {
        val db = ready()
        write(db, "Un", 13_500, storeId = "s1", packSize = 900.0, packUnit = "  ", at = 1_000, id = "a")

        val row = db.priceObservationDao().allObservations(home).single()
        assertNull(row.packSize)
        assertNull(row.packUnit)
    }

    /**
     * AYNI ETIKET METNI IKINCI KEZ SORULMUYOR - F4.7'nin tamami bu.
     *
     * Gezinme sozlesmesi dort ayri yerde soyluyor: *"Secim bu markette bu
     * etiket metnine baglanir; ayni etiket bir daha sorulmaz."* Tablo, tekil
     * indeks ve `find` sorgusu fis doneminden beri duruyordu ve HIC
     * CAGRILMIYORDU - kullanici markette on cekimin onunda urunu elle sectii.
     */
    @Test
    fun theTagTextIsBoundToTheChosenProduct() = runTest {
        val db = ready()
        write(db, "Yoğurt", 10_200, storeId = "s-bim", tagText = "DOST YARIM YAGLI YOGURT", chain = bim)

        val found = db.productAliasDao().find(home, bim, matchKey("DOST YARIM YAGLI YOGURT"))
        val product = db.productDao().byId(assertNotNull(found, "esleme yazilmadi").productId)
        assertEquals("Yoğurt", assertNotNull(product).name)
        assertNotNull(found.confirmedAt, "kullanici sectigi icin ONAYLI olmali")
    }

    /**
     * ESLEME ZINCIR BAZINDA - ayni metin baska zincirde baska urun olabilir.
     *
     * `Shopping.kt`in kendi cumlesi: fiyat karsilastirmasi zincir bazinda
     * anlamli, esleme de oyle. Global bir tablo BIM'in "SUT"unu A101'in
     * "SUT"uyle karistirirdi.
     */
    @Test
    fun theBindingIsPerChain() = runTest {
        val db = ready()
        write(db, "Süt", 6_250, storeId = "s-bim", tagText = "SUT 1 L", chain = bim, id = "a")
        write(db, "Yoğurt", 4_900, storeId = "s-a101", tagText = "SUT 1 L", chain = a101, at = 90_000, id = "b")

        val atBim = assertNotNull(db.productAliasDao().find(home, bim, matchKey("SUT 1 L")))
        val atA101 = assertNotNull(db.productAliasDao().find(home, a101, matchKey("SUT 1 L")))
        assertTrue(atBim.productId != atA101.productId, "iki zincir ayni urune baglandi")
    }

    /**
     * DUZELTME KAZANIYOR - son karar eskisini eziyor.
     *
     * DAO `REPLACE` kullaniyor ve gerekcesi kendi KDoc'unda: `IGNORE` olsaydi
     * kullanicinin ikinci karari sessizce yutulur, ayni metin her okundugunda
     * ayni yanlis eslesmeyi uretirdi - ustelik kullanici duzeltmis oldugu icin
     * bir daha bakmazdi.
     */
    @Test
    fun aCorrectionOverwritesTheOldBinding() = runTest {
        val db = ready()
        write(db, "Süt", 6_250, storeId = "s-bim", tagText = "DST YGRT", chain = bim, id = "a")
        write(db, "Yoğurt", 10_200, storeId = "s-bim", tagText = "DST YGRT", chain = bim, at = 90_000, id = "b")

        val found = assertNotNull(db.productAliasDao().find(home, bim, matchKey("DST YGRT")))
        assertEquals("Yoğurt", assertNotNull(db.productDao().byId(found.productId)).name)
    }

    /**
     * ETIKET METNI OKUNAMADIYSA ESLEME YAZILMIYOR.
     *
     * Uydurma bir anahtar yazmak KALICI bir yanlis baglama uretirdi: bos ya da
     * anlamsiz bir metin bir kez bir urune baglanirsa, sonraki her okunamayan
     * etiket o urune duserdi.
     */
    @Test
    fun nothingIsBoundWhenTheTagTextIsMissing() = runTest {
        val db = ready()
        write(db, "Süt", 6_250, storeId = "s-bim", tagText = null, chain = bim, id = "a")
        write(db, "Çay", 39_900, storeId = "s-bim", tagText = "   ", chain = bim, at = 90_000, id = "b")

        assertEquals(0, db.dataWipeDao().let { 0 } + countAliases(db), "esleme yazilmamaliydi")
    }

    private suspend fun countAliases(db: NeydiDatabase): Int =
        listOf("", "   ").count { db.productAliasDao().find(home, bim, matchKey(it)) != null }

    private suspend fun countRows(db: NeydiDatabase): Int =
        db.priceObservationDao().allObservations(home).size
}
