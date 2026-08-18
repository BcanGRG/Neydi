package com.neydi.app.data.ocr

import com.neydi.app.data.store.chainKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [readTagFields] - zincir kapisi ve birim fiyat capraz kontrolu.
 *
 * 80 gercek etiketin hepsine karsi kosuyor. Iddialarin hepsi olculdu;
 * gerekcesi `docs/18-zincir-karsilastirmasi.md`.
 */
class TagFieldsTest {

    private val bim = chainKey("BİM")

    /**
     * KAPI TOHUMLA AYNI SOZLUGU KULLANIYOR.
     *
     * Bu test sikici gorunuyor ama tam olarak sessiz bir olumu engelliyor:
     * `SUPPORTED_CHAIN` elle `"bim"` yazilsaydi ve `chainKey` bir gun baska bir
     * anahtar uretse, kapi HIC acilmazdi ve butun ekran sessizce bos alanlarla
     * calisirdi - hicbir sey patlamadan.
     */
    @Test
    fun theGateSpeaksTheSeedVocabulary() {
        // `183746`yi SECMEDIM ve sebebi ogretici: ilk yazdigimda onu secmistim,
        // test kirmizi yandi - o etiketin fiyatini capraz kontrol zaten
        // eliyor. Kapinin acildigini gostermek icin ikisini de gecen bir
        // etiket gerekiyor.
        val ocr = TagFixtures.all.getValue("20260817_183728")
        assertNotNull(readTagFields(ocr, bim).price, "BIM kapisi acilmiyor")
        assertNull(readTagFields(ocr, "bilinmiyor").price)
    }

    /** Market secilmemisse hicbir sey okunmuyor - tahmin edilmiyor. */
    @Test
    fun noStoreMeansNoReading() {
        val f = readTagFields(TagFixtures.all.getValue("20260817_183746"), null)
        assertNull(f.price)
        assertNull(f.name)
        assertNull(f.pack)
        assertEquals(TagSkip.UNSUPPORTED_CHAIN, f.skipped)
    }

    /**
     * GRAMERI YAZILMAMIS ZINCIRDE HICBIR SEY OKUNMUYOR.
     *
     * Once Metro ve Migros birlikteydi. Migros'un grameri yazilinca liste
     * daraldi - bu bir gerileme degil, kapinin isini bitirdigi yer. Metro
     * hala icerde: olcumde her etikette bir sayi buluyor ama yanlis sayiyi.
     */
    @Test
    fun unsolvedChainsYieldNothing() {
        listOf("Metro").forEach { chain ->
            val tags = TagFixtures.of(chain)
            assertTrue(tags.isNotEmpty(), "$chain fiksturu bos - test dayanaksiz")
            tags.forEach { (tag, ocr) ->
                val f = readTagFields(ocr, chainKey(chain))
                assertNull(f.price, "$tag: kapi acik kalmis")
                assertNull(f.name, "$tag: kapi acik kalmis")
                assertEquals(TagSkip.UNSUPPORTED_CHAIN, f.skipped, "$tag")
            }
        }
    }

    /**
     * MIGROS PATATESI 4389,00 TL DEGIL - once kapi, simdi gramer sayesinde.
     *
     * Bu test once `null` bekliyordu: Migros'un grameri yoktu ve kapi her seyi
     * kesiyordu. Simdi gramer var ve DOGRU cevabi veriyor. Iddia degismedi,
     * karsilandigi yol degisti - o yuzden hem BIM okuyucusunun hala 4389
     * dedigini hem borunun 43,95 verdigini birlikte kilitliyorum.
     */
    @Test
    fun theMigrosPotatoIsNotWrittenAsFourThousandLira() {
        val ocr = TagFixtures.all.getValue("20260817_211114")
        assertEquals(438_900L, readTagPrice(ocr)?.minor, "BIM kurali artik baska sey donuyor")
        assertEquals(4_395L, readTagFields(ocr, chainKey("Migros")).price?.minor)
    }

    /**
     * CAPRAZ KONTROL: manset ile birim fiyat celisirse fiyat YAZILMIYOR.
     *
     * BIM'de olculen iki vaka. Ikisinde de yanlis okunan sey manset degil,
     * BIRIM FIYAT satiri - ama hangisinin yanlis oldugunu koddan bilemiyoruz,
     * yalnizca uyusmadiklarini biliyoruz. Iki uyusmaz sayidan birini secmek
     * tahmin olurdu.
     *
     * Bedeli acik: bu iki etikette dogru bir manset eleniyor ve kullanici
     * fiyati elle yaziyor.
     */
    @Test
    fun aHeadlineThatFightsItsOwnUnitPriceIsDropped() {
        mapOf(
            "20260817_183746" to "T06,00 kg -> 6,00 okundu, gercegi 106,00",
            "20260817_183847" to "750/hg okundu, gercegi 57,50/kg",
        ).forEach { (tag, why) ->
            val ocr = TagFixtures.all.getValue(tag)
            assertNotNull(readTagPrice(ocr), "$tag: ham manset okunamiyor, test dayanaksiz")
            val f = readTagFields(ocr, bim)
            assertNull(f.price, "$tag ($why)")
            assertEquals(TagSkip.PRICE_CONTRADICTS_UNIT_PRICE, f.skipped, tag)
            assertNotNull(f.name, "$tag: ad da dusmus - celiski yalnizca FIYATI ilgilendirir")
        }
    }

    /**
     * UYUSAN ETIKETLERDE FIYAT KORUNUYOR - kontrolun asiri kiymadiginin kaniti.
     *
     * Dokuz vaka, orani 0,99872 ile 1,00001 arasinda. Bunlar olmadan
     * "her seyi eliyor" ile "dogru olani eliyor" ayirt edilemezdi.
     */
    @Test
    fun agreeingTagsKeepTheirPrice() {
        mapOf(
            "20260817_183728" to 3_400L,
            "20260817_183849" to 8_350L,
            "20260817_183920" to 2_900L,
            "20260817_183947" to 21_900L,
            "20260817_184007" to 38_900L,
            "20260817_184045" to 5_700L,
            "20260817_184101" to 46_900L,
            "20260817_184116" to 8_450L,
            "20260817_184202" to 3_900L,
        ).forEach { (tag, expected) ->
            val f = readTagFields(TagFixtures.all.getValue(tag), bim)
            assertEquals(expected, f.price?.minor, "$tag: uyusan etiketin fiyati dusmus")
            assertNull(f.skipped, tag)
        }
    }

    /**
     * GRAMAJI OLMAYAN COK-PAKET ELENMIYOR.
     *
     * `184300` 12'li tuvalet kagidi: manset 143 TL, birim fiyat 11,92/adet -
     * orani 12 ve etiket DOGRU. Kontrolun gramaj sartina baglanmasinin sebebi
     * bu; gramajsiz "oran 1 degil" demek dogru etiketleri elerdi.
     */
    @Test
    fun aMultiPackWithoutAPackSizeIsNotSuspicious() {
        val f = readTagFields(TagFixtures.all.getValue("20260817_184300"), bim)
        assertEquals(14_300L, f.price?.minor)
        assertNull(f.pack, "gramaj okunuyorsa bu testin dayanagi degisti")
        assertNull(f.skipped)
    }

    /**
     * OLCULEN KAPSAM: kapinin ardinda BIM'de kac etiket fiyat veriyor.
     *
     * 26 manset okunuyordu; capraz kontrol ikisini elediigi icin 24. Sayiyi
     * teste yaziyorum ki bir degisiklik onu sessizce dusuremesin.
     */
    @Test
    fun measuredCoverageBehindTheGate() {
        val priced = TagFixtures.of("BIM").count { (_, ocr) -> readTagFields(ocr, bim).price != null }
        assertEquals(24, priced, "BIM'de fiyat veren etiket sayisi degisti")
    }
}
