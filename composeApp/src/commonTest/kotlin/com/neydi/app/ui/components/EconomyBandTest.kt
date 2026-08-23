package com.neydi.app.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Ekonomi bandinin cumlesi (karar 104-105).
 *
 * ## Neden bu cumlelerin testi var
 *
 * Bandin tasidigi soz tek satirlik: *"fiyat iki yerde asla yazilmaz."* Cipin
 * ne zaman cizildigi (satirin govdesinde) ile cumlenin ne yazdigi (burada)
 * ayri yerlerde kararlastiriliyor - yani sozun bozulmasi icin birinin
 * degismesi yetiyor. Nitekim `Single` dalinda tam olarak o olmustu: karar 83
 * *"ikinci satir yok"* diyordu, maket *"son 24,90 TL · Migros · 8 gun once"*
 * ciziyordu ve kod ucuncu bir sey yaziyordu.
 *
 * Karar 105 celiskiyi cozdu - ikisi ayni seyin IKI TAZELIK HALIYMIS - ve
 * asagidaki iki vaka o iki hali kilitliyor.
 */
class EconomyBandTest {

    private fun price(hint: PriceHint) = SecondLine.Price(hint).metaText()

    /**
     * TAZE GOZLEM: cumle fiyat YAZMAZ - fiyat cipte durur.
     *
     * Tasarimin maketi: *"Salatalik / BIM · bugun / 24,90 TL"*.
     */
    @Test
    fun aFreshObservationLeavesThePriceToTheChip() {
        assertEquals("BİM · bugün", price(PriceHint.Single("24,90 TL", "BİM", 0)))
    }

    /**
     * ESKIMIS GOZLEM: cip yok, o yuzden fiyat CUMLEYE girer.
     *
     * Guncel fiyat bilinmiyor; bilinen sey hatirlanan bir fiyat ve cumle onu
     * "son" diye isaretliyor. Cip cizilseydi eski fiyat bugunku fiyat gibi
     * okunurdu - satirin en cok bakilan yerinde.
     */
    @Test
    fun aStaleObservationMovesThePriceIntoTheSentence() {
        assertEquals(
            "son 24,90 TL · Migros · 8 gün önce",
            price(PriceHint.Single("24,90 TL", "Migros", 8)),
        )
    }

    /**
     * SINIR TAM OLARAK YEDI GUN VE ICE KAPALI.
     *
     * Yedinci gun hala taze, sekizinci gun degil. Sinirin hangi tarafta
     * oldugunu yazmayan bir kural, iki uygulamada iki farkli cevap verir -
     * ve fark tam da cipin cizilip cizilmemesi.
     */
    @Test
    fun theFreshnessBoundaryIsInclusive() {
        assertTrue(price(PriceHint.Single("10,00 TL", "A101", FRESH_DAYS)).startsWith("A101"))
        assertTrue(price(PriceHint.Single("10,00 TL", "A101", FRESH_DAYS + 1)).startsWith("son "))
    }

    /** Trend dalinda cumle GECMISI anlatir; guncel fiyat yine yalniz cipte. */
    @Test
    fun theTrendSentenceNeverRepeatsTheCurrentPrice() {
        val meta = price(
            PriceHint.Trend(
                from = "324,00 TL", to = "369,00 TL", deltaPercent = 14, rising = true,
            ),
        )
        assertEquals("önce 324,00 TL", meta)
        assertFalse(meta.contains("369"), "guncel fiyat cumleye sizmis")
    }

    /** Gozlem yoksa bant hic cizilmez - "fiyat yok" da yazilmaz. */
    @Test
    fun noObservationWritesNothing() {
        assertEquals("", price(PriceHint.None))
    }
}
