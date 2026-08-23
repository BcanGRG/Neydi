package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Yas bicimi ve cip para bicimi (gezinme sozlesmesi).
 *
 * IKISI DE MERDIVENDEN/ANA BICIMDEN AYRILMAK ICIN VAR, o yuzden testlerin
 * cogu **ayriligi** sinliyor: ayni girdiyi hem [formatAge] hem
 * [formatRelativeDay] alsa farkli cevap vermeli. Aksi halde biri otekine
 * kayar ve fark sessizce kaybolur.
 */
class AgeAndChipFormatTest {

    // --- Yas (F11.25) --------------------------------------------------------

    /**
     * ASIL VAKA: merdivenin sildigi aralik.
     *
     * `formatRelativeDay` 7-13 gunu tek bir "geçen hafta"ya topluyor. Yas
     * bicimi toplamamali - kullanicinin okudugu sey tam olarak 8 ile 12
     * arasindaki fark.
     */
    @Test
    fun theWeekTheLadderErasesIsKeptInDays() {
        assertEquals("8 gün önce", formatAge(8))
        assertEquals("12 gün önce", formatAge(12))
        assertEquals("13 gün önce", formatAge(13))
    }

    /** On dorduncu gunde hafta basliyor - "2 hafta önce", "14 gün önce" degil. */
    @Test
    fun fourteenDaysBecomesTwoWeeks() {
        assertEquals("13 gün önce", formatAge(13))
        assertEquals("2 hafta önce", formatAge(14))
    }

    /** Hafta asagi yuvarliyor: 20 gun hala iki hafta. */
    @Test
    fun weeksRoundDown() {
        assertEquals("2 hafta önce", formatAge(20))
        assertEquals("3 hafta önce", formatAge(21))
        assertEquals("5 hafta önce", formatAge(37))
    }

    /** Alt iki basamak: gun sayisi orada bilgi tasimiyor. */
    @Test
    fun todayAndYesterdayUseWords() {
        assertEquals("bugün", formatAge(0))
        assertEquals("dün", formatAge(1))
        assertEquals("2 gün önce", formatAge(2))
    }

    /**
     * NEGATIF GUN cokmuyor.
     *
     * Gelecek tarihli gozlem yazilmamali (sozlesme) ama cihaz saati geriye
     * alinmis bir kullanicida negatif fark cikabilir; "-3 gün önce" yazmak
     * yerine en yakin dogru sozcuge dusuyor.
     */
    @Test
    fun negativeAgeDoesNotProduceNonsense() {
        assertEquals("bugün", formatAge(-3))
    }

    // --- Cip parasi (F11.26) -------------------------------------------------

    /** Cipte TL yok - yalnizca sayi. */
    @Test
    fun chipDropsTheCurrency() {
        assertEquals("89,00", formatChipMinor(8_900))
        assertEquals("159,90", formatChipMinor(15_990))
    }

    /** Bin liranin ALTINDA kurus duruyor; duşen tek sey TL. */
    @Test
    fun chipKeepsTurkishGroupingAndKurusBelowAThousand() {
        assertEquals("289,00 TL", formatMinor(28_900))
        assertEquals("289,00", formatChipMinor(28_900))
        assertEquals("999,99", formatChipMinor(99_999))
    }

    /**
     * 1.000 TL VE USTUNDE KURUS YAZILMIYOR (karar 81).
     *
     * BEKLENTI DEGISTI ve degismesi dogru cozumdu. Once "1.289,90" bekliyordu;
     * o metin 92dp'lik fiyat sutununa SIGMIYOR - olculdu, 99,38dp istiyor - ve
     * fazlasini dogrudan AD sutunundan caliyordu.
     *
     * Kurusu dusurmek bilgi kaybi degil: bin liralik bir sepette 56 kurus
     * okunacak bir sey degil ve tam degeri Urun Detayi'nda duruyor. Ayni ilke
     * karar 67'de manset icin zaten yaziliydi; karar 81 onu satira tasidi.
     *
     * EN YAKIN LIRAYA, asagi degil - asagi yuvarlamak butun fiyatlari
     * sistematik olarak ucuz gosterirdi.
     */
    @Test
    fun theChipDropsKurusFromAThousandUp() {
        assertEquals("1.290", formatChipMinor(128_990))
        assertEquals("1.000", formatChipMinor(100_000))
        // 1.234,56 -> asagi degil EN YAKINA: 1.235
        assertEquals("1.235", formatChipMinor(123_456))
    }

    /**
     * CIP ILE TAHMIN AYNI SEY DEGIL.
     *
     * `formatEstimate` tilde koyup kurusu atiyor (tutar turetilmis), cip ise
     * GOZLENMIS bir fiyati yaziyor - kurus duruyor, tilde yok. Ikisini
     * karistiran bir cagri, tahmini kesin gibi ya da gozlemi tahmin gibi
     * gosterirdi.
     */
    @Test
    fun chipIsNotAnEstimate() {
        assertEquals("89,00", formatChipMinor(8_900))
        assertEquals("~89 TL", formatEstimate(8_900))
    }
}
