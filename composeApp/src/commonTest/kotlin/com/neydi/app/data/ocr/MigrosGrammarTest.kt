package com.neydi.app.data.ocr

import com.neydi.app.data.store.chainKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Migros grameri - 19 gercek etiketin ELLE KURULMUS dogruluk tablosuna karsi.
 *
 * Tablo koddan turetilmedi: her satir ham OCR dokumune bakilarak, etiketin
 * gercekte ne yazdigi okunarak yazildi. Yani bu test "kod ne yapiyor"u degil
 * "dogru cevap ne"yi kilitliyor - fikstur uretimiyle beklenti uretimi ayni
 * kaynaktan gelseydi test hicbir sey kanitlamazdi.
 *
 * ONEMLI OLAN SAYI YANLIS OLANLAR: 16 dogru / **0 yanlis** / 3 red. Kapsam
 * artabilir, ama yanlis sayisi artamaz.
 */
class MigrosGrammarTest {

    private val migros = chainKey("Migros")

    /** Etiket -> gercek fiyat (kurus). `null` = okuyucu susmali. */
    private val truth = mapOf(
        "20260817_211052" to 4_495L,
        "20260817_211105" to 6_995L,
        "20260817_211112" to 3_995L,
        "20260817_211114" to 4_395L,
        "20260817_211142" to 29_500L,
        "20260817_211204" to 14_995L,
        "20260817_211219" to 7_950L,
        "20260817_211234" to 22_390L,
        "20260817_211236" to 23_995L,
        "20260817_211311" to 14_295L,
        "20260817_211316" to 7_495L,
        "20260817_211335" to 33_995L,
        "20260817_211337" to 33_995L,
        "20260817_211407" to 21_995L,
        "20260817_211409" to 21_995L,
        "20260817_211430" to 37_995L,
        "20260817_211507" to 30_995L,
        "20260817_211525" to 2_750L,
        "20260817_211527" to 2_750L,
    )

    /**
     * HICBIR ETIKETTE YANLIS FIYAT YOK - bu testin asil iddiasi.
     *
     * SUSMAK HER ZAMAN KABUL, yanlis cevap ASLA. Ilk yazdigimda her etikette
     * dogru cevabi sart kosmustum ve `211527` kirmizi yandi - o etikette
     * okuyucu susuyor (`211525` ayni urunun ikinci karesi ve okunuyor).
     * Susmayi hata saymak testi kapsam olcusune cevirirdi; oysa kapsam
     * artabilir de azalabilir de, degismemesi gereken sey yanlis sayisi.
     *
     * Kapsam ayrica [measuredCoverage]'ta kilitli, yani sessizce dusemez.
     */
    @Test
    fun noTagYieldsAWrongPrice() {
        truth.forEach { (tag, want) ->
            val got = readTagFields(TagFixtures.all.getValue(tag), migros).price?.minor
                ?: return@forEach
            assertEquals(want, got, "$tag: YANLIS fiyat")
        }
    }

    /**
     * KURUS PARCASI VIRGUL TASIYOR - BIM kuralinin tam tersi.
     *
     * `211052`: lira `44` (h=692), kurus `,95` (h=368). BIM'de ayirici tasiyan
     * aday **ustu cizili eski fiyat** demekti ve elenirdi; burada dogru cevabin
     * kendisi. Iki gramerin neden ayri durdugunun en kisa kaniti.
     */
    @Test
    fun theKurusPieceCarriesItsComma() {
        val ocr = TagFixtures.all.getValue("20260817_211052")
        assertNull(BimGrammar.readPrice(ocr)?.minor?.takeIf { it == 4_495L }, "BIM kurali bunu okuyamamali")
        assertEquals(4_495L, MigrosGrammar.readPrice(ocr)?.minor)
    }

    /**
     * MONEY FIYATI ALINMIYOR - ve daha BUYUK basildigi icin tehlikeli.
     *
     * `211430`: `MONEYLI FIYAT` altinda 169,95 (h=239), `NORMAL SATIS FIYATI`
     * altinda 379,95 (h=181). "En buyuk glif" kurali yanlis olani seciyordu.
     * Money fiyati kart gerektiriyor - etiketin kendisi yaziyor - yani raf
     * fiyati degil.
     */
    @Test
    fun theLoyaltyCardPriceIsNotTheShelfPrice() {
        assertEquals(
            37_995L,
            readTagFields(TagFixtures.all.getValue("20260817_211430"), migros).price?.minor,
        )
        assertEquals(
            22_390L,
            readTagFields(TagFixtures.all.getValue("20260817_211234"), migros).price?.minor,
        )
    }

    /**
     * MANAV ETIKETINDE BIRIM FIYAT SATIRI OKUNUYOR, manset degil.
     *
     * `211114`te manset `4389` diye yapismis - son hane 5 yerine 9. Ayni sayi
     * `BIRIM FIYAT: 43,95 TL/KG` satirinda tertemiz basili. Kiloyla satilan
     * urunde ikisi ayni sayi, dolayisiyla temiz olani okumak tahmin degil.
     */
    @Test
    fun onAProducePlaqueTheUnitPriceLineWins() {
        val ocr = TagFixtures.all.getValue("20260817_211114")
        // Mansetin BOZUK oldugunu fiksturun kendisinden dogruluyorum: `4389`
        // parcasi orada duruyor. Test icin uretim API'si acmiyorum.
        assertNotNull(ocr.lines.firstOrNull { it.text.trim() == "4389" }, "fikstur degisti")
        assertEquals(4_395L, readTagFields(ocr, migros).price?.minor)
    }

    /**
     * OCR BIR HANE UYDURDUYSA CAPRAZ KONTROL YAKALIYOR.
     *
     * `211219` (1 L sut): parcalar `799` + `,50`, yani gramer 799,50 okuyor.
     * Gercek 79,50 - birim fiyat satiri `79,50 TL/LT` ve ambalaj `1L` bunu
     * soyluyor. Gramer tek basina yanlis; boru hatti dogru davraniyor.
     */
    @Test
    fun aSpuriousDigitIsCaughtByTheCrossCheck() {
        val ocr = TagFixtures.all.getValue("20260817_211219")
        assertEquals(79_950L, MigrosGrammar.readPrice(ocr)?.minor, "gramerin ham cevabi degisti")
        val fields = readTagFields(ocr, migros)
        assertNull(fields.price)
        assertEquals(TagSkip.PRICE_CONTRADICTS_UNIT_PRICE, fields.skipped)
    }

    /**
     * `30 Lİ` OTUZ LITRE DEGIL.
     *
     * Adet deseni once ASCII `L[UI]` yazilmisti ve Turkce `İ`yi kaciriyordu;
     * kacirinca ambalaj deseni ayni satiri `30 L` diye okuyup otuz yumurtayi
     * otuz litreye ceviriyordu. Turkce metinde ASCII varsayimi sessiz bir hata
     * kaynagi.
     */
    @Test
    fun thirtyEggsAreNotThirtyLitres() {
        val pack = assertNotNull(MigrosGrammar.readPack(TagFixtures.all.getValue("20260817_211204")))
        assertEquals(30.0, pack.size)
        assertEquals("adet", pack.unit)
    }

    /** Ad OKUNMUYOR - gerekcesi [MigrosGrammar.readName] KDoc'unda. */
    @Test
    fun theProductNameIsDeliberatelyNotRead() {
        truth.keys.forEach { tag ->
            assertNull(MigrosGrammar.readName(TagFixtures.all.getValue(tag)), tag)
        }
    }

    /** OLCULEN KAPSAM: kac etiket fiyat veriyor. Dusmesi gorunur olsun. */
    @Test
    fun measuredCoverage() {
        val priced = truth.keys.count { readTagFields(TagFixtures.all.getValue(it), migros).price != null }
        assertEquals(16, priced, "Migros'ta fiyat veren etiket sayisi degisti")
    }
}
