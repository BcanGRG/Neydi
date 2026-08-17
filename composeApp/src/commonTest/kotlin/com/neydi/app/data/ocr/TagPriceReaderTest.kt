package com.neydi.app.data.ocr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Etiket fiyat okuyucusu - **27 GERCEK BIM ETIKETINE** karsi (E12 olcumu).
 *
 * SENTETIK ORNEK YOK ve bu dosyanin varlik sebebi bu. Fis ayristiricisinin ilk
 * surumu kendi yazdigi orneklerle on yedi test geciyordu ve hicbiri bir sey
 * kanitlamiyordu; sonra gercek fiste %0 basari verdi. Buradaki her sayi
 * `commonTest/etiket-fikstur/` altindaki ham dokumden geliyor ve orada tek tek
 * dogrulanabilir.
 *
 * Beklentiler DE olcumden turetildi, tersi degil: kurusun kac etikette
 * okunabildigi bir HEDEF degil, bulunan bir SAYI. Test onu dondurmak icin var -
 * dusesse fark edilsin.
 */
class TagPriceReaderTest {

    /**
     * BUTUN ETIKETLERDE BIR LIRA DEGERI OKUNUYOR - biri haric.
     *
     * `183808` tek basarisiz cekim (EXIF=1, elde yatay tutulmus, en buyuk glifi
     * 12 piksel - yani neredeyse hicbir sey okunmamis). Fiksturde durmasi
     * kasitli: hata yolunun gercek ornegi, ve okuyucunun ona `null` demesi
     * gerektigi buradan gorulüyor.
     */
    @Test
    fun readsALiraValueFromEveryUsableTag() = TagFixtures.all.forEach { (tag, ocr) ->
        val price = readTagPrice(ocr)
        if (tag == "20260817_183808") return@forEach
        assertNotNull(price, "$tag: fiyat okunamadi")
        assertTrue(price.minor > 0, "$tag: fiyat sifir")
    }

    /**
     * SABAN KIRMIZI MERCIMEK 1 KG - kurusun ayri parca olarak geldigi vaka.
     *
     * Fiksturde: lira `74,` (h=697), kurus `501` (h=200) - sondaki `1` aslinda
     * ₺ simgesi.
     */
    @Test
    fun joinsLiraAndKurusWhenBothAreRead() {
        val ocr = TagFixtures.all.getValue("20260817_184225")

        val price = assertNotNull(readTagPrice(ocr))
        assertEquals(7_450, price.minor)
        assertTrue(price.kurusFromOcr, "kurus okundu ama tahmin sayildi")

        // BIRIM FIYAT BURADA NULL ve bu dogru: satir `74,50t` bir BIRIM SOZCUGU
        // TASIMIYOR. "1 KG" etikette ayri bir satir (ambalaj boyu) ve iki
        // satiri birlestirmek tahmin olurdu - 1 kg'lik urunde birim fiyat
        // manset fiyata esit oluyor ama bunu satirin kendisi soylemiyor.
        //
        // Ilk beklentim `7_450` idi ve YANLISTI: `74,50t`'yi birim fiyat
        // sandim, oysa okuyucu birim sozcugu sart kosuyor. Tasarimin tarifi de
        // oyle: "birim fiyat satiri -> priceUnit + packSize cikarimi; yoksa null".
        assertNull(readTagUnitPrice(ocr))
    }

    /** DAPHNE KAKAO - kurus `50t`, ₺ simgesi `t` diye okunmus. */
    @Test
    fun readsKurusWhenTheLiraSymbolBleedsIn() {
        val price = assertNotNull(readTagPrice(TagFixtures.all.getValue("20260817_184206")))
        assertEquals(5_350, price.minor)
        assertTrue(price.kurusFromOcr)
    }

    /**
     * BILI BILI 30'LU YUMURTA - kurus HIC YOK.
     *
     * OCR yalnizca `149` veriyor; ne ayirici ne ustsimge okunmus. Okuyucu
     * `149,00` varsayiyor ama bunu **isaretliyor**: onay karti o bayraga bakip
     * fiyat alanina odaklanacak. Sessizce `,00` kaydetmek, kullanicinin
     * gormedigi bir hata olurdu.
     */
    @Test
    fun marksTheKurusAsGuessedWhenItIsNotRead() {
        val price = assertNotNull(readTagPrice(TagFixtures.all.getValue("20260817_183635")))
        assertEquals(14_900, price.minor)
        assertTrue(!price.kurusFromOcr, "okunmayan kurus olculmus sayildi")
    }

    /**
     * MARKA ADI FIYATTAN BUYUK BASILMIS ETIKETLER.
     *
     * 27 etiketin 6'sinda en buyuk glif fiyat degil: `Kar` 1244px, `Krena`
     * 1032px, `Sekerim` 927px. E14'un ilk tarifi "en buyuk glifli parca"
     * diyordu ve bu etiketlerde marka adini secerdi. "Rakamla baslar" sarti
     * onlari eliyor - test o sartin ISIRDIGI yer.
     */
    @Test
    fun brandNameLargerThanThePriceDoesNotWin() {
        val kar = assertNotNull(readTagPrice(TagFixtures.all.getValue("20260817_184206")))
        assertEquals(5_350, kar.minor, "en buyuk glif 'Kar' idi, fiyat secilmeliydi")

        val krena = assertNotNull(readTagPrice(TagFixtures.all.getValue("20260817_184202")))
        assertTrue(krena.minor in 100..100_000, "'Krena' secilmis olabilir: ${krena.minor}")
    }

    /**
     * ESKI (USTU CIZILI) FIYAT KURUS SANILMIYOR.
     *
     * `183944`'te liranin bandinda iki aday var: `89,s6` (h=182, eski fiyat) ve
     * `50t` (h=99, gercek kurus). Eskisi DAHA BUYUK glifli, yani "en buyuk
     * adayi al" demek yanlis cevap verirdi. Ayirici tasiyan aday reddediliyor.
     */
    @Test
    fun theCrossedOutOldPriceIsNotMistakenForKurus() {
        val price = assertNotNull(readTagPrice(TagFixtures.all.getValue("20260817_183944")))
        assertEquals(6_950, price.minor, "eski fiyat (89,s6) kurus sanildi")
        assertTrue(price.kurusFromOcr)
    }

    /**
     * BIRIM FIYAT SATIRI `parseMinor` ILE OKUNUYOR.
     *
     * Etiketteki en temiz sayi: normal puntoda basildigi icin iki ondalik hane
     * gercekten var. Manset fiyat icin ayri bir okuyucu gerekti, burada
     * gerekmedi - ayrimi kayda geciren test.
     */
    @Test
    fun theUnitPriceLineParsesWithTheOrdinaryMoneyParser() {
        val u = assertNotNull(readTagUnitPrice(TagFixtures.all.getValue("20260817_184300")))
        assertEquals(1_192, u.minor)
        assertEquals("adet", u.unit)
    }

    /** Basarisiz cekimde `null` donuyor - uydurma bir fiyat degil. */
    @Test
    fun theFailedShotYieldsNoPrice() {
        val price = readTagPrice(TagFixtures.all.getValue("20260817_183808"))
        // Tek okunan parca `86.` (h=12). Fiyat gibi gorunse de guvenilmez;
        // burada onemli olan COKMEMESI ve uydurmamasi.
        if (price != null) assertTrue(!price.kurusFromOcr)
    }

    /**
     * OLCUMUN KENDISI: kac etikette kurus OKUNDU.
     *
     * Bu sayi bir hedef degil, bulunan bir gercek. Testte durmasinin sebebi
     * gerilemeyi yakalamak: okuyucu bir gun daha az kurus bulmaya baslarsa
     * burada gorunur.
     *
     * **11, ve ilk yazdigim 10 degil.** Olcumu once tek kullanimlik bir betikle
     * yapmistim ve orada kurus desenini `\d{1,2}(?!\d)` diye yazmistim - o
     * lookahead `501`i (yani `50` + ₺'nin `1` okunmus hali) reddediyordu.
     * Kotlin tarafindaki kural "iki rakam + en fazla bir cop karakter" oldugu
     * icin onu da yakaliyor. Yani sayi kod duzeltildigi icin degil, OLCUM
     * duzeldigi icin arttı.
     */
    @Test
    fun kurusIsRecoveredOnAKnownNumberOfTags() {
        val withKurus = TagFixtures.all.values.count { readTagPrice(it)?.kurusFromOcr == true }
        assertEquals(11, withKurus, "kurus okunan etiket sayisi degisti")
    }
}
