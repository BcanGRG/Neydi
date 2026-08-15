package com.neydi.app.data.receipt

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.Product
import com.neydi.app.data.db.ProductAlias
import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptLine
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.db.Trip
import com.neydi.app.data.matchKey
import com.neydi.app.data.stats.ProductStatsRebuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * ReceiptProcessor testleri. Okuyucu SAHTE - OCR'i degil, OCR sonrasi kararlari
 * dogruluyoruz: okunamayan fis esigi, alias onceligi, aritmetik durumu.
 *
 * Fis satirlari yine GERCEK cihaz ciktisi (bkz. ReceiptParserTest) - burada da
 * kendi yazdigim ornekle kendimi onaylamak istemiyorum.
 */
class ReceiptProcessorTest {

    private val home = "h1"
    private val trip = "t1"

    /** Cihazda BIM fisinden okunan gorsel satirlar. */
    private val bimLines = listOf(
        "BIM BIRLESIK MAGAZALAR A.S.",
        "13.08.2026 18:49 Sira No : 218",
        "2 ad X 53.00",
        "KREMA 18YAĞLI 200ML %1. *106.00",
        "TURŞU KORNI ŞON 670G 21. *84.50",
        "ALIŞVERIŞ POŞETi BiM 220 *1.00",
        "GOFRET FIND KREM142G %1. *34.00",
        "TOPLAM KDV *2.39",
        "Odenecek KDV Dahil Tutar *225.50",
        "Banka Kredi Kartı (1) *225.50",
    )

    private class FakeReader(
        private val lines: List<String>,
        private val error: Throwable? = null,
    ) : ReceiptReader {
        var lastRotation: Int? = null
            private set

        override suspend fun readLines(imagePath: String, forceRotation: Int?): Result<List<String>> {
            lastRotation = forceRotation
            return error?.let { Result.failure(it) } ?: Result.success(lines)
        }
    }

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private suspend fun prepare(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = home, name = "Bizim ev", createdAt = 0))
        db.tripDao().insert(Trip(id = trip, householdId = home, startedAt = 0, createdAt = 0))
        db.receiptDao().insert(
            Receipt(
                id = "r1",
                householdId = home,
                tripId = trip,
                imagePath = "/tmp/r1.jpg",
                capturedAt = 100,
                createdAt = 100,
            ),
        )
    }

    private fun processor(db: NeydiDatabase, reader: ReceiptReader): ReceiptProcessor {
        var n = 0
        return ReceiptProcessor(
            reader = reader,
            receiptDao = db.receiptDao(),
            receiptLineDao = db.receiptLineDao(),
            productDao = db.productDao(),
            aliasDao = db.productAliasDao(),
            tripDao = db.tripDao(),
            storeDao = db.storeDao(),
            statsRebuilder = ProductStatsRebuilder(db.productStatsDao(), clock = { 500L }),
            clock = { 500L },
            newId = { "line-${++n}" },
        )
    }

    // --- Okuma ve satir yazma -----------------------------------------------

    @Test
    fun writesOneLinePerProduct() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines)).process("r1")

        val lines = db.receiptLineDao().forReceipt("r1")
        assertEquals(listOf(10600L, 8450L, 100L, 3400L), lines.map { it.lineTotalMinor })
    }

    @Test
    fun verifiedWhenArithmeticHolds() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines)).process("r1")

        val receipt = db.receiptDao().byId("r1")
        assertEquals(ReceiptStatus.VERIFIED, receipt?.status)
        assertEquals(22550, receipt?.totalMinor)
    }

    /** Toplam satiri okunmadiysa MISMATCHED ama satirlar YINE yaziliyor. */
    @Test
    fun keepsLinesWhenTotalUnreadable() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines.filter { !it.contains("Odenecek") })).process("r1")

        assertEquals(ReceiptStatus.MISMATCHED, db.receiptDao().byId("r1")?.status)
        assertTrue(db.receiptLineDao().forReceipt("r1").isNotEmpty())
    }

    // --- Karar 11 · magaza satiri fisten doguyor ----------------------------

    /**
     * MAGAZA SATIRI ELLE DEGIL FISTEN DOGUYOR ve adi EKRANDA GORULEN hali -
     * ticari unvan degil (karar 13 ile ayni ad).
     *
     * Gezi de magazaya BAGLANIYOR: bag kurulmazsa `Trip.storeId` sonsuza kadar
     * null kalir ve fiyat karsilastirmasi hangi zincirden konustugunu bilemez.
     */
    @Test
    fun receiptGivesBirthToStoreRow() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines)).process("r1")

        val stores = db.storeDao().observeAll(home).first()
        assertEquals(1, stores.size)
        assertEquals("BIM", stores.single().name)
        assertEquals("bim", stores.single().chain)
        assertEquals(stores.single().id, db.tripDao().byId(trip)?.storeId)
    }

    /**
     * AYNI ZINCIR IKINCI SATIR DOGURMUYOR. Yoksa Ayarlar'daki bolum bir liste
     * degil bir gunluk olurdu: on fis = on "BIM" satiri.
     */
    @Test
    fun secondReceiptFromSameChainReusesStore() = runTest {
        val db = db(); prepare(db)
        db.receiptDao().insert(
            Receipt(
                id = "r2",
                householdId = home,
                tripId = trip,
                imagePath = "/tmp/r2.jpg",
                capturedAt = 200,
                createdAt = 200,
            ),
        )
        val p = processor(db, FakeReader(bimLines))
        p.process("r1")
        p.process("r2")

        assertEquals(1, db.storeDao().observeAll(home).first().size)
    }

    /**
     * MAGAZA ADI OKUNAMADIYSA HICBIR SEY YAZILMIYOR. "bilinmiyor" adinda bir
     * magaza satiri, bolumun bos kalmasindan daha kotu olurdu.
     */
    @Test
    fun unreadableStoreWritesNoStoreRow() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines.drop(1))).process("r1")

        assertTrue(db.storeDao().observeAll(home).first().isEmpty())
        assertEquals(null, db.tripDao().byId(trip)?.storeId)
    }

    /**
     * YENIDEN ISLEME USTUNE EKLEMEZ.
     *
     * Eklerse ayni fis iki kez sayilir ve aritmetik kapisi bunu ancak rastgele
     * yakalar - kullanici da "toplam tutmuyor" gorup neden oldugunu anlamaz.
     */
    @Test
    fun reprocessReplacesLinesInsteadOfAppending() = runTest {
        val db = db(); prepare(db)
        val p = processor(db, FakeReader(bimLines))
        p.process("r1")
        p.process("r1")

        assertEquals(4, db.receiptLineDao().forReceipt("r1").size)
    }

    // --- Okunamayan fis -----------------------------------------------------

    /**
     * Olculmus gercek durum: ~60 kalemlik fis tek karede satir basina 4,7
     * piksele dusuyor ve ML Kit 60 satirin 2'sini okuyabiliyor. Sessizce bos
     * sonuc gostermek en kotusu olurdu.
     */
    @Test
    fun tooFewLinesIsFailureWithExplanation() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(listOf("AKYURT", "*12.50"))).process("r1")

        val receipt = db.receiptDao().byId("r1")
        assertEquals(ReceiptStatus.FAILED, receipt?.status)
        assertEquals(UNREADABLE_MESSAGE, receipt?.errorMessage)
    }

    @Test
    fun readerFailureBecomesFailedStatus() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(emptyList(), error = IllegalStateException("gorsel acilamadi")))
            .process("r1")

        val receipt = db.receiptDao().byId("r1")
        assertEquals(ReceiptStatus.FAILED, receipt?.status)
        assertEquals("gorsel acilamadi", receipt?.errorMessage)
    }

    // --- Eslestirme ---------------------------------------------------------

    /** Eslesme yoksa satir ONAYA duser - tahmini eslestirme YOK. */
    @Test
    fun unmatchedLinesNeedReview() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines)).process("r1")

        val lines = db.receiptLineDao().forReceipt("r1")
        // BOYUT KONTROLU SART: `all {}` bos listede her zaman true doner, yani
        // process() satir yazmayi tamamen birakirsa - F4.4/F4.6'nin en agir
        // regresyonu - bu testin iki iddiasi da gecmeye devam ederdi.
        assertEquals(4, lines.size)
        assertTrue(lines.all { it.needsReview })
        assertTrue(lines.all { it.matchedProductId == null })
    }

    /**
     * ALIAS ONCELIKLI ve kullanicinin kararini tekrar SORMUYOR.
     *
     * Tekrar sormak F4.7'nin butun degerini yok ederdi: her fiste ayni
     * duzeltmeyi yapmak zorunda kalan kullanici duzeltmeyi birakir.
     */
    @Test
    fun aliasMatchesAndSkipsReview() = runTest {
        val db = db(); prepare(db)
        db.productDao().insert(
            Product(
                id = "p1", householdId = home, name = "Turşu",
                matchKey = "tursu", categoryId = "temel-gida",
                defaultUnit = "adet", createdAt = 0,
            ),
        )
        db.productAliasDao().insert(
            ProductAlias(
                id = "a1", householdId = home, storeChain = "bim",
                rawTextNormalized = matchKey("TURŞU KORNI ŞON 670G"),
                productId = "p1", createdAt = 0,
            ),
        )

        processor(db, FakeReader(bimLines)).process("r1")

        val tursu = db.receiptLineDao().forReceipt("r1").first { it.lineTotalMinor == 8450L }
        assertEquals("p1", tursu.matchedProductId)
        assertEquals(false, tursu.needsReview)
        assertEquals(1.0, tursu.confidence)
    }

    /** Alias SUBE degil ZINCIR bazli: baska subede de eslesmeli. */
    @Test
    fun chainKeyIgnoresBranch() {
        assertEquals("bim", chainKey("BIM BIRLESIK MAGAZALAR A.S."))
        assertEquals("bim", chainKey("BIM BADEMLIK SUBESI"))
        assertEquals("file", chainKey("FiLE MARKET MAĞAZACIL IK"))
        assertEquals("bilinmiyor", chainKey(null))
    }

    // --- Yon zorlamasi ------------------------------------------------------

    /** Elle cevirme okuyucuya GECIYOR; yoksa buton hicbir sey yapmazdi. */
    @Test
    fun forcedRotationReachesReader() = runTest {
        val db = db(); prepare(db)
        val reader = FakeReader(bimLines)
        processor(db, reader).process("r1", forceRotation = 90)

        assertEquals(90, reader.lastRotation)
    }

    @Test
    fun missingReceiptIsReported() = runTest {
        val db = db(); prepare(db)
        assertEquals(
            ReceiptReadOutcome.MISSING,
            processor(db, FakeReader(bimLines)).process("yok"),
        )
    }

    /**
     * ELLE YON CEVIRME IYI OKUMAYI BOZAMAZ.
     *
     * Cihazda yasandi: dik gorunen bir fis aslinda dondurulmus cekilmisti,
     * 0 dereceyi zorlayan buton okumayi iki satira dusurdu ve dogru
     * ayristirilmis alti satir erisilemez oldu. Kullanici bir butona basarak
     * calisan bir seyi kaybetmemeli.
     */
    @Test
    fun failedRereadKeepsPreviousReading() = runTest {
        val db = db(); prepare(db)
        // Ilk okuma iyi.
        processor(db, FakeReader(bimLines)).process("r1")
        val before = db.receiptLineDao().forReceipt("r1")

        // Ikinci okuma kullanilamaz.
        val outcome = processor(db, FakeReader(listOf("AKYURT", "*12.50"))).process("r1")

        assertEquals(ReceiptReadOutcome.KEPT_PREVIOUS, outcome)
        assertEquals(before.map { it.lineTotalMinor }, db.receiptLineDao().forReceipt("r1").map { it.lineTotalMinor })
        // Durum da geri alinmali: FAILED kalirsa ekran satirlari gizler.
        assertEquals(ReceiptStatus.VERIFIED, db.receiptDao().byId("r1")?.status)
    }

    @Test
    fun outcomeIsUnreadableWhenNothingToKeep() = runTest {
        val db = db(); prepare(db)
        assertEquals(
            ReceiptReadOutcome.UNREADABLE,
            processor(db, FakeReader(listOf("AKYURT", "*12.50"))).process("r1"),
        )
    }

    // --- Gezi toplamina devir (F4.11) --------------------------------------

    /**
     * FIS TOPLAMI GEZIYE DEVREDILIYOR.
     *
     * Bu devir hic yazilmamisti: ozet karti ve Gecmis `Trip.totalMinor` okuyor,
     * fis ise `Receipt.totalMinor` yaziyordu ve arada bag yoktu - ikisi de
     * kalici olarak "-" gosteriyordu. Derleme ve testler goremezdi, cunku iki
     * alan da gecerli; sadece biri hic dolmuyordu.
     */
    @Test
    fun receiptTotalRollsUpToTrip() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines)).process("r1")

        assertEquals(22550, db.tripDao().byId(trip)?.totalMinor)
    }

    /**
     * IKI FISLI GEZIDE TOPLANIYOR, ustune YAZILMIYOR.
     *
     * Uzun fis parca parca cekilebiliyor (F4.4: ~60 kalem tek kareye sigmiyor)
     * ve ayni geziye birden fazla fis baglanabiliyor. Tek fisi geziye yazmak iki
     * parcali alisverisin yarisini gostermek olurdu.
     */
    @Test
    fun twoReceiptsOnOneTripAreSummed() = runTest {
        val db = db(); prepare(db)
        db.receiptDao().insert(
            Receipt(
                id = "r2", householdId = home, tripId = trip,
                imagePath = "/tmp/r2.jpg", capturedAt = 200, createdAt = 200,
            ),
        )
        val p = processor(db, FakeReader(bimLines))
        p.process("r1")
        p.process("r2")

        assertEquals(45100, db.tripDao().byId(trip)?.totalMinor)
    }

    /**
     * TOPLAM OKUNAMADIYSA GEZI TUTARI NULL KALIR.
     *
     * Dogrulanmamis bir sayiyi 36sp'de manset yapmak, kullanicinin
     * sorgulayamayacagi bir yerde tahmini gercek gibi sunmaktir. Satirlarin
     * toplami Fis Kontrol ekraninda zaten gorunuyor.
     */
    @Test
    fun unreadableTotalLeavesTripAmountNull() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines.filter { !it.contains("Odenecek") })).process("r1")

        assertEquals(null, db.tripDao().byId(trip)?.totalMinor)
    }

    /** Satir tutari duzeltmesi gezi tutarini DEGISTIRMEZ: odenen para degismedi. */
    @Test
    fun lineCorrectionDoesNotChangeTripTotal() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines)).process("r1")
        val line = db.receiptLineDao().forReceipt("r1").first()

        db.receiptLineDao().setAmount(line.id, 999_99)

        assertEquals(22550, db.tripDao().byId(trip)?.totalMinor)
    }

    @Test
    fun storeNameIsRecorded() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines)).process("r1")

        val store = db.receiptDao().byId("r1")?.storeNameRaw
        assertNotNull(store)
        assertTrue(store.contains("BIM"), "magaza adi: $store")
    }

    // --- Gezi kapsamli aritmetik kapisi (F4.13 duzeltmesi) ------------------

    /**
     * SATIRLAR FIZIKSEL FIS BOYUNCA TOPLANIYOR, TEK FOTOGRAFTA DEGIL.
     *
     * Kapinin dayandigi sayi bu. Tek fotograf kapsaminda hesaplanan toplam,
     * uzun fisin SON parcasinda yapisal olarak tutmuyor cikiyordu: toplam
     * yalnizca o parcada basili ama satirlarin geri kalani onceki parcalarda.
     *
     * Test isiriyor: sorgu tek `receiptId`'ye cekilirse 45100 yerine 22550 doner.
     */
    @Test
    fun lineSumSpansAllPartsOfOneReceipt() = runTest {
        val db = db(); prepare(db)
        db.receiptDao().insert(
            Receipt(
                id = "r2", householdId = home, tripId = trip,
                imagePath = "/tmp/r2.jpg", capturedAt = 200, createdAt = 200,
            ),
        )
        val p = processor(db, FakeReader(bimLines))
        p.process("r1")
        p.process("r2")

        val singleReceipt = db.receiptLineDao().forReceipt("r1")
            .sumOf { if (it.isDiscount) -it.lineTotalMinor else it.lineTotalMinor }
        assertEquals(22550, singleReceipt, "tek parcanin satirlari")
        assertEquals(
            45100,
            db.receiptLineDao().sumLinesForReceipts(listOf("r1", "r2")),
            "iki parcanin satirlari",
        )
    }

    /**
     * INDIRIM CIKARILIYOR - isaret bayraktan geliyor, sayidan degil.
     *
     * Tutar her zaman pozitif saklaniyor (bkz. `ParsedLine` KDoc). Sorgu
     * bayragi gozardi etseydi indirimli fiste kapi ayristiricidan FARKLI karar
     * verirdi; ekran kapisinin ayni hatasi F5.6'da kayitli.
     */
    @Test
    fun lineSumSubtractsDiscounts() = runTest {
        val db = db(); prepare(db)
        processor(db, FakeReader(bimLines)).process("r1")
        db.receiptLineDao().insertAll(
            listOf(
                ReceiptLine(
                    id = "indirim", householdId = home, receiptId = "r1",
                    rawText = "INDIRIM *10.00", rawTextNormalized = "indirim",
                    unitPriceMinor = null, lineTotalMinor = 1000,
                    isDiscount = true, createdAt = 300,
                ),
            ),
        )

        assertEquals(21550, db.receiptLineDao().sumLinesForReceipts(listOf("r1")))
    }

    /** Hic satir yoksa NULL - "satir yok" ile "toplami sifir" ayri seyler. */
    @Test
    fun lineSumIsNullWhenNoLines() = runTest {
        val db = db(); prepare(db)

        assertEquals(null, db.receiptLineDao().sumLinesForReceipts(listOf("r1")))
    }
}
