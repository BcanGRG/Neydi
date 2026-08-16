package com.neydi.app.data.receipt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Parca dikisinin kimlik tarafi (F4.15).
 *
 * Girdiler GERCEK: hepsi kullanicinin telefonundan, `rawOcrText` kolonundan.
 * Dort parcalik AKYURT cekiminde altmis kalemin kirk ikisi iki parcada birden
 * okunmustu; bu testler o bindirmeyi goze gorunur kilan kimligi olcuyor.
 */
class LineIdentityTest {

    /** Ayni kalem, iki parcada. Tutar farkli okunsa bile kimlik AYNI olmali. */
    @Test
    fun sameItemInTwoPartsSharesIdentity() {
        val first = lineIdentity("15 8690530066359 2 Adet 29,50 %%20 59,00 t")
        val second = lineIdentity("15 8690530066359 2 Adet 29,50 %20 59,0O")
        assertEquals(first, second)
        assertNotNull(first)
    }

    /**
     * OCR RAKAMI HARFE CEVIRIYOR ve kimlik bunu geri almali.
     *
     * Gercek fiste goruldu: `S0` (50) ve `869O508101426` (sifir yerine harf O).
     * Duzeltilmezse ayni kalem iki parcada iki AYRI kimlik alir ve bindirme
     * goze gorunmez kalir - tam olarak kacirmak istemedigimiz sey.
     */
    @Test
    fun ocrLetterForDigitDoesNotSplitIdentity() {
        assertEquals(
            lineIdentity("50 2902898 0,648 Kg 59,50 %01 38,56"),
            lineIdentity("S0 2902898 0,648 Kg 59,50 %01 38,56"),
        )
        assertEquals(
            lineIdentity("56 8690508101426 1 Adet 53,90 %01 53,90"),
            lineIdentity("56 869O508101426 1 Adet 53,90 %01 53,90"),
        )
    }

    /**
     * FARKLI KALEMLER FARKLI KIMLIK. Ayni urunden iki kez alinsa bile fis iki
     * AYRI sira numarasi basiyor - bindirme sanip birini silmek kullanicinin
     * gercekten aldigi bir seyi yok etmek olurdu.
     */
    @Test
    fun differentSequenceNumbersStayDistinct() {
        assertNotEquals(
            lineIdentity("56 8690508101426 1 Adet 53,90 %01 53,90"),
            lineIdentity("57 8690508101426 1 Adet 53,90 %01 53,90"),
        )
    }

    /**
     * SIRA NUMARASI YOKSA KIMLIK DE YOK.
     *
     * BIM/File duzeninde satirin fisten gelen bir kimligi yok. Ad ve tutardan
     * kimlik uydurmak, ayni fiste iki adet ayri satir basilmis bir urunun
     * birini SILERDI. Silmemek en kotu ihtimalle mukerrer gosterir; silmek
     * veri kaybettirir.
     */
    /**
     * SERIT BINDIRMESI: ayni kalem iki seritte okunuyor, biri kaliyor - VE
     * ADI DA BIRLIKTE gidiyor.
     *
     * Adi birakmak butun fisi bir kaydirirdi: kalan ad bir sonraki kalemin adi
     * sanilirdi (AKYURT duzeninde ad, tutar satirinin bir ALTINDA).
     */
    @Test
    fun bandOverlapDropsItemWithItsName() {
        val rows = listOf(
            "43 8690576029431 1 Adet 28,50 %01 28,50",
            "ANKARA MAK.500 GR.KALEM",
            // ikinci seridin basi - ayni kalem yeniden
            "43 8690576029431 1 Adet 28,50 %01 28,50",
            "ANKARA MAK.500 GR.KALEM",
            "44 8690576029257 1 Adet 28,50 %01 28,50",
            "ANKARA MAK,500 GR. MANTI",
        )
        assertEquals(
            listOf(
                "43 8690576029431 1 Adet 28,50 %01 28,50",
                "ANKARA MAK.500 GR.KALEM",
                "44 8690576029257 1 Adet 28,50 %01 28,50",
                "ANKARA MAK,500 GR. MANTI",
            ),
            dedupeRepeatedItems(rows),
        )
    }

    /** KIMLIKSIZ SATIRLARA DOKUNULMUYOR - kunye, KDV dokumu, toplam. */
    @Test
    fun rowsWithoutIdentityAreUntouched() {
        val rows = listOf(
            "AKYURT SÜPERMARKET GIDA İNS.SAN.VE TİC. A.Ş.",
            "KDV % Matrah KDV Tutar",
            "Ödenecek KDV Dahíl Tutar *5.709,08",
        )
        assertEquals(rows, dedupeRepeatedItems(rows))
    }

    @Test
    fun classicLayoutHasNoIdentity() {
        assertNull(lineIdentity("KREMA 18YAĞLI 200ML %1. *106.00"))
        assertNull(lineIdentity("ALIŞVERIŞ POŞETi BiM 220 *1.00"))
        assertNull(lineIdentity("Odenecek KDV Dahil Tutar *225.50"))
    }
}
