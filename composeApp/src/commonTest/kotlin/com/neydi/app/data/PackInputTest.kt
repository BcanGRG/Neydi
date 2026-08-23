package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Elle yazilan ambalajin okunmasi (karar 111).
 *
 * ## Neden bu kuralin testi var
 *
 * Bu alanin yazdigi deger DOGRUDAN paraya donusuyor: ambalaj yazilinca satir
 * tahmine giriyor ve bir tutar uretiyor. Yaniltici bir kabul, hic kabul
 * etmemekten kotu - cunku satir sessizce toplama girer ve yanlis bir sayi
 * cikarir. Bu yuzden reddetme tarafi da tek tek kilitli.
 */
class PackInputTest {

    /** Olagan hal: sayi + birim. */
    @Test
    fun aSizeAndAUnitAreRead() {
        assertEquals(3.0 to "kg", parsePack("3 kg"))
        assertEquals(500.0 to "gr", parsePack("500 gr"))
        assertEquals(1.5 to "lt", parsePack("1,5 lt"))
    }

    /** Bosluk sart degil, ondalikta nokta da kabul - klavye duzeni degisiyor. */
    @Test
    fun spacingAndSeparatorsAreForgiven() {
        assertEquals(1.0 to "kg", parsePack("1kg"))
        assertEquals(0.5 to "L", parsePack("0.5 L"))
        assertEquals(750.0 to "ml", parsePack("  750 ml  "))
    }

    /**
     * SAYILAN BIRIM AMBALAJ DEGIL.
     *
     * "3 adet"lik bir ambalaj, tartili satirin bolenine cevrilemez ve karar
     * 111'e gore satir yine ambalajsiz sayilirdi. Reddetmek, kullaniciya
     * yazdiginin ise yaramadigini SESSIZCE degil aninda soyluyor.
     */
    @Test
    fun aCountedUnitIsNotAPack() {
        assertNull(parsePack("3 adet"))
        assertNull(parsePack("2 paket"))
    }

    /** Taninmayan birim kabul edilmiyor - varsaymak yanlis sayi uretirdi. */
    @Test
    fun anUnknownUnitIsRejected() {
        assertNull(parsePack("3 kavanoz"))
        assertNull(parsePack("1 buyuk"))
    }

    /** Yarim yazilmis hal henuz bir cevap degil - alan yazarken dolduruluyor. */
    @Test
    fun aHalfTypedValueIsNotAnAnswer() {
        assertNull(parsePack("3"))
        assertNull(parsePack("3 k"))
        assertNull(parsePack(""))
    }

    /** Sifir ve negatif ambalaj yok - bolen olacak sayi. */
    @Test
    fun zeroIsNotAPack() {
        assertNull(parsePack("0 kg"))
        assertNull(parsePack("-1 kg"))
    }
}
