package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Manset tutari (karar 67 · 2): kurus SIFIRSA yazilmiyor, varsa yaziliyor.
 *
 * Ekran okuyucu kuralinin gorsel ikizi - gezinme sozlesmesi fiyati
 * *"38 lira 50 kuruş"* okutuyor, *"kuruş sıfırsa okunmaz"*. Iki kanalin ayni
 * metni farkli okumasi, testin engelledigi sey.
 */
class HeadlineMoneyTest {

    /** Tam lira: iki sifir cumleden dusuyor. */
    @Test
    fun aWholeLiraDropsTheKurus() {
        assertEquals("32 TL", formatHeadlineMinor(3200))
        assertEquals("41 TL", formatHeadlineMinor(4100))
        assertEquals("0 TL", formatHeadlineMinor(0))
    }

    /**
     * KURUS VARSA DURUYOR: "32 TL" ile "32,50 TL" arasindaki fark elli kurus
     * ve yuvarlamak, gozlenmis bir sayiyi degistirmek olurdu.
     */
    @Test
    fun kurusSurvivesWhenItIsNotZero() {
        assertEquals("32,50 TL", formatHeadlineMinor(3250))
        assertEquals("32,05 TL", formatHeadlineMinor(3205))
        assertEquals("0,99 TL", formatHeadlineMinor(99))
    }

    /**
     * TILDE YOK - bu bicim ile [formatEstimate] arasindaki tek fark ve
     * kararin kendisi: tilde bir TAHMIN isareti, buradaki sayi ise etiketten
     * okunmus kesin bir fiyat.
     */
    @Test
    fun anObservedPriceNeverCarriesTheTilde() {
        assertFalse(formatHeadlineMinor(64250).contains("~"))
        // 64250 kurus = 642,50 TL ve tahmin bicimi kurusu YUVARLIYOR - 643.
        // Ilk yazimda burada 642 bekleniyordu; kod degil beklenti yanlisti ve
        // farkin kendisi de anlamli: manset kurusu DUSURUR (642,50 -> "642,50
        // TL" ya da tam lirada "642 TL"), tahmin ise YUVARLAR.
        assertEquals("~643 TL", formatEstimate(64250), "tahmin bicimi degismedi")
    }

    /** Binlik ayirici NOKTA, tam lirada da. */
    @Test
    fun usesDotForThousands() {
        assertEquals("1.289 TL", formatHeadlineMinor(128900))
        assertEquals("1.289,90 TL", formatHeadlineMinor(128990))
    }

    @Test
    fun formatsNegativeAmounts() {
        assertEquals("-32 TL", formatHeadlineMinor(-3200))
        assertEquals("-32,50 TL", formatHeadlineMinor(-3250))
    }

    @Test
    fun currencySuffixCanBeDropped() {
        assertEquals("32", formatHeadlineMinor(3200, currency = ""))
        assertEquals("32,50", formatHeadlineMinor(3250, currency = ""))
    }
}
