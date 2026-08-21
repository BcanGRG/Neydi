package com.neydi.app.data.ocr

import com.neydi.app.data.store.chainKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * A101 grameri - 19 gercek etiketin ELLE KURULMUS dogruluk tablosuna karsi.
 *
 * Tablo koddan turetilmedi. On sekiz satiri etiketin KENDI birim fiyat
 * satiriyla carpim kontrolunden gecti (`1.253,33 x 150 G = 188,00`), dordu
 * ayrica fotografa bakilarak okundu (`133214` = `265 TL`, `133249` = `105 TL`,
 * `133340` = `249 TL`, `133411` = `64,90 TL`). Fikstur uretimiyle beklenti
 * uretimi ayni kaynaktan gelseydi bu test hicbir sey kanitlamazdi.
 *
 * ONEMLI OLAN SAYI YANLIS OLANLAR: kapsam artabilir de azalabilir de,
 * degismemesi gereken sey yanlis sayisi.
 */
class A101GrammarTest {

    private val a101 = chainKey("A101")

    /** Etiket -> gercek fiyat (kurus). */
    private val truth = mapOf(
        "20260821_132811" to 6_450L,
        "20260821_132813" to 6_450L,
        "20260821_132819" to 1_975L,
        "20260821_133036" to 18_800L,
        "20260821_133143" to 9_500L,
        "20260821_133149" to 9_950L,
        "20260821_133158" to 13_500L,
        "20260821_133211" to 15_850L,
        "20260821_133214" to 26_500L,
        "20260821_133220" to 4_350L,
        "20260821_133226" to 4_350L,
        "20260821_133227" to 4_350L,
        "20260821_133232" to 16_900L,
        "20260821_133247" to 10_500L,
        "20260821_133248" to 10_500L,
        "20260821_133249" to 10_500L,
        "20260821_133322" to 5_950L,
        "20260821_133340" to 24_900L,
        "20260821_133411" to 6_490L,
    )

    /** Kurusu etikette BASILI olan etiketler - [aPrintedKurusIsNeverMissed] icin. */
    private val kurusPrinted = setOf(
        "20260821_132811", "20260821_132813", "20260821_132819", "20260821_133149",
        "20260821_133211", "20260821_133220", "20260821_133226", "20260821_133227",
        "20260821_133322", "20260821_133411",
    )

    private fun ocr(tag: String) = TagFixtures.all.getValue(tag)

    /**
     * HICBIR ETIKETTE YANLIS FIYAT YOK - bu testin asil iddiasi.
     *
     * Susmak kabul, yanlis cevap asla. Kapsam [measuredCoverage]'ta ayrica
     * kilitli, yani sessizce dusemez.
     */
    @Test
    fun noTagYieldsAWrongPrice() {
        truth.forEach { (tag, want) ->
            val got = readTagFields(ocr(tag), a101).price?.minor ?: return@forEach
            assertEquals(want, got, "$tag: YANLIS fiyat")
        }
    }

    /**
     * AYNI ETIKETIN UC CEKIMI AYNI FIYATI VERIYOR - grameri yazdiran olcum.
     *
     * `133220`/`133226`/`133227` ayni BIRSAH SUT 1 L etiketi. OCR uc ayri sey
     * okudu (`439`, `43-50`, `43`+`50`) cunku A101 virgulu liranin boyunda
     * basiyor; uc ayri kural uctan da 43,50 cikariyor.
     */
    @Test
    fun theSameTagShotThreeTimesReadsTheSamePrice() {
        val shots = listOf("20260821_133220", "20260821_133226", "20260821_133227")
        val read = shots.map { A101Grammar.readPrice(ocr(it))?.minor }
        assertEquals(listOf(4_350L, 4_350L, 4_350L), read, "uc cekim ayni fiyati vermeli")
    }

    /**
     * YAPISAN GLIFI GEOMETRI AYIKLIYOR - kural bu testte kilitli.
     *
     * `133220`de manset `439` diye geldi: OCR fiyat boyundaki virgulu bir 9
     * sanip liraya yapistirdi. Kanit kutularda - manset 1603..2432, kurus
     * 2209'da basliyor, yani 223 piksel BINDIRME var ve bu bir glif genisligi.
     * `133227`de bindirme yok (2016 vs 2079) ve hane korunuyor.
     *
     * BU KURAL KALKARSA: `439` + `50` = 439,50 yazilir, on kat yanlis.
     */
    @Test
    fun theAbsorbedGlyphIsDroppedByGeometry() {
        val absorbed = assertNotNull(ocr("20260821_133220").readableLira())
        assertEquals("439", absorbed.text.trim(), "olcum degisti: manset artik `439` degil")
        assertEquals(4_350L, A101Grammar.readPrice(ocr("20260821_133220"))?.minor)

        val clean = assertNotNull(ocr("20260821_133227").readableLira())
        assertEquals("43", clean.text.trim(), "olcum degisti: manset artik `43` degil")
        assertEquals(4_350L, A101Grammar.readPrice(ocr("20260821_133227"))?.minor)
    }

    /**
     * SONDAKI SIFIR `o` OKUNDU - katlama olmazsa fiyat on kat kucuk cikar.
     *
     * `133211`de 158,50 TL manseti `1585o` diye geldi. Hane suzgeci tek basina
     * `1585` verir ve 4. kural bunu 15,85 yapar.
     */
    @Test
    fun aZeroPrintedAsTheLetterOIsStillAZero() {
        val head = assertNotNull(ocr("20260821_133211").readableLira())
        assertEquals("1585o", head.text.trim(), "olcum degisti: manset artik `1585o` degil")
        assertEquals(15_850L, A101Grammar.readPrice(ocr("20260821_133211"))?.minor)
    }

    /**
     * BASILI BIR KURUS HIC KACIRILMADI - `kurusFromOcr = true` bunun uzerine kurulu.
     *
     * Gramer kurus parcasi bulamayinca fiyati TAM LIRA sayiyor ve bunu
     * kullaniciya guvenli diye sunuyor. O guvenin dayanagi tam olarak bu:
     * kurusu sifir olmayan on etiketin onunda da kurus okundu.
     *
     * Capraz kontrol bu hatayi YAKALAMAZ - 43,00 ile 43,50 arasi %1,15 ve
     * tolerans %2. Bu yuzden iddia burada, ayri bir testte duruyor: karsi
     * ornek cikan gun kirilacak olan bu.
     */
    @Test
    fun aPrintedKurusIsNeverMissed() {
        kurusPrinted.forEach { tag ->
            val minor = assertNotNull(A101Grammar.readPrice(ocr(tag)), "$tag: fiyat okunmadi").minor
            assertTrue(minor % 100 != 0L, "$tag: basili kurus kacirildi - $minor")
        }
    }

    /**
     * KURUS BASILI DEGILSE fiyat tam lira ve TEREDDUTSUZ.
     *
     * Uc etiketin fotografina bakildi: `265 TL`, `249 TL`, `105 TL` - kurus
     * basili degil, okunacak bir sey yok. Kart bu etiketlerde *"Kurus
     * okunamadi"* uyarisi GOSTERMEMELI; gosterseydi A101 etiketlerinin
     * yarisinda cikar ve uyari anlamini yitirirdi.
     */
    @Test
    fun aWholeLiraPriceIsNotFlaggedAsUncertain() {
        (truth.keys - kurusPrinted).forEach { tag ->
            val price = assertNotNull(A101Grammar.readPrice(ocr(tag)), "$tag: fiyat okunmadi")
            assertEquals(0L, price.minor % 100, "$tag: tam lira bekleniyordu")
            assertTrue(price.kurusFromOcr, "$tag: gereksiz kurus uyarisi")
        }
    }

    /**
     * BINLIK KURALI OLCULEN HICBIR SEYI DEGISTIRMIYOR.
     *
     * Kural bin liranin ustundeki bir etiket icin yazildi ve oyle bir etiket
     * OLCULMEDI. Zararsiz oldugunun kaniti bu: on dokuz mansetin hicbiri
     * binlik ayirici tasimiyor, yani kural hicbirinde ateslenmiyor.
     */
    @Test
    fun theThousandsGuardFiresOnNoMeasuredTag() {
        val thousands = Regex("""\d{1,3}[.\s]\d{3}""")
        truth.keys.forEach { tag ->
            val head = assertNotNull(ocr(tag).readableLira()).text.trim()
            assertTrue(!thousands.containsMatchIn(head), "$tag: manset binlik ayirici tasiyor - $head")
        }
    }

    /**
     * Gramer KAYITLI ve desteklenen zincir listesinde.
     *
     * Ikisi birlikte degismek zorunda: liste karar 49'un cumlesini kuruyor
     * (*"A101, BİM ve Migros etiketlerini okuyabiliyoruz"*) ve biri eksik
     * kalirsa cumle sessizce yalan olur.
     */
    @Test
    fun theGrammarIsRegisteredAndAnnounced() {
        assertSame(A101Grammar, grammarFor(a101))
        assertTrue("A101" in SUPPORTED_CHAINS, "A101 desteklenen zincir listesinde degil")
    }

    /** Fikstur partisi zincire dogru baglanmis mi. */
    @Test
    fun everyA101FixtureIsLabelledA101() {
        assertEquals(truth.keys.sorted(), TagFixtures.of("A101").keys.sorted())
    }

    /**
     * ELENENLERIN HEPSI KADRAJDA IKI ETIKET OLAN KARELER.
     *
     * Dordunde de kullanici iki etiketi birden cekmis; mansetin sahibi bir
     * etiket, okunan birim fiyat satiri OTEKI etiket. Capraz kontrol ikisinin
     * uyusmadigini goruyor ve fiyati birakiyor - dogru davranis, cunku hangi
     * etiketin kastedildigi bilinmiyor.
     *
     * SAHADA BU ORAN DAHA IYI OLMALI: bu on dokuz kare telefonun kamerasiyla,
     * rehbersiz cekildi. Uygulamanin kendi cekiminde kirpim tek etikete
     * daraliyor.
     */
    @Test
    fun theOnlyDroppedTagsAreDoubleTagFrames() {
        val dropped = truth.keys.filter { readTagFields(ocr(it), a101).price == null }.sorted()
        assertEquals(
            listOf("20260821_133220", "20260821_133226", "20260821_133227", "20260821_133411"),
            dropped,
        )
    }

    /**
     * MARKA HIC TAHMIN EDILMIYOR - olculmus bir ret.
     *
     * Kunye kodu ayiklandiktan sonra marka kutusu doluyor ve sekizi dogru
     * geliyor, ama ALTISI yanlis (`VEGAN`, `AMET`, `ada`, etiket kunyesi...).
     * Karar 26 markayi satir kimligi yaptigi icin yanlis marka KALICI; karar
     * 39 `null`u mesru cevap sayiyor. Gerekce [A101Grammar.readName]'de.
     */
    @Test
    fun noBrandIsEverGuessed() {
        truth.keys.forEach { tag ->
            assertNull(A101Grammar.readName(ocr(tag))?.brand, "$tag: marka tahmin edildi")
        }
    }

    /**
     * KUNYE KODU IPUCU METNINE SIZMIYOR.
     *
     * A101 her etikette ad blogunun ustune `0430 2605091327` gibi bir kod
     * basiyor ve BIM'in `isStoreCode` suzgeci onu tanimiyor. Suzgec kalkarsa
     * ON DOKUZ ETIKETIN ON DOKUZU da kirmizi yanar.
     *
     * Sart bicime degil ORANA bakiyor - `0430` oneki A101'in magaza numarasi
     * ve baska subede baska.
     */
    @Test
    fun theArticleCodeNeverReachesTheProductHint() {
        truth.keys.forEach { tag ->
            val name = A101Grammar.readName(ocr(tag))?.name ?: return@forEach
            name.split(' ').forEach { word ->
                assertTrue(
                    word.length < 8 || word.count { it.isDigit() } * 2 <= word.length,
                    "$tag: kunye kodu ipucuna sizdi - `$word`",
                )
            }
        }
    }

    /**
     * GRAMAJ TEK ETIKET DISINDA HEP OKUNUYOR - ve o biri BILEREK bos.
     *
     * `133232` yumurta etiketi: gramaji `53-62 G`, bir aralik. Tek sayiya
     * indirmek gerekirdi ve hangisi dogru belli degil; `readTagPack` araliklari
     * reddediyor.
     */
    @Test
    fun packIsReadOnEveryTagExceptTheEggRange() {
        val missing = truth.keys.filter { A101Grammar.readPack(ocr(it)) == null }
        assertEquals(listOf("20260821_133232"), missing, "gramaj kapsami degisti")
    }

    /**
     * OLCULEN KAPSAM - sessizce dusmesin diye kilitli.
     *
     * Okunamayanlar capraz kontrolun eledikleri: kadrajda IKI etiket olan
     * kareler, birinin manseti digerinin birim fiyat satiriyla eslesmiyor.
     * Uygulamanin kendi cekiminde kirpim tek etikete daraldigi icin bu oran
     * sahada daha iyi olmali; buradaki on dokuz kare telefonun kamerasiyla,
     * rehbersiz cekildi.
     */
    @Test
    fun measuredCoverage() {
        val read = truth.keys.count { readTagFields(ocr(it), a101).price != null }
        assertEquals(EXPECTED_COVERAGE, read, "kapsam degisti")
    }
}

private const val EXPECTED_COVERAGE = 15
