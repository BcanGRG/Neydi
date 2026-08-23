package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Yazilan miktarin temizligi (karar 108).
 *
 * ## Neden bu kuralin testi var
 *
 * Miktar alani yazilabilir oldugu an klavye bir sozlesme haline geliyor.
 * Kural olmadan iki hatadan biri kacinilmaz: ya cozumlenemeyen bir metin
 * kabul edilir ve sayi kaydedilmez, ya da temizlik fazla agresif olur ve
 * kullanicinin yazdigi rakam ekrandan kaybolur. Ikisi de sessiz.
 */
class DecimalInputTest {

    /** Rakam olmayan her sey duser - klavye ne gonderirse gondersin. */
    @Test
    fun onlyDigitsAndOneSeparatorSurvive() {
        assertEquals("15", sanitizeDecimal("1a5 kg"))
        assertEquals("", sanitizeDecimal("abc"))
    }

    /** Nokta da kabul ediliyor ama ciktida hep VIRGUL var - gosterilen bicim o. */
    @Test
    fun aDotBecomesAComma() {
        assertEquals("1,5", sanitizeDecimal("1.5"))
    }

    /**
     * IKINCI AYIRICI DUSUYOR, YAZI KESILMIYOR.
     *
     * "1,2,3" -> "1,23". Metni ikinci virgulden kesmek daha kolay olurdu ama
     * kullanicinin yazdigi 3'u yutardi - ve yutulan rakam, ekranda hicbir iz
     * birakmadan kaybolan bir dokunustur.
     */
    @Test
    fun aSecondSeparatorIsDroppedWithoutLosingDigits() {
        assertEquals("1,23", sanitizeDecimal("1,2,3"))
        assertEquals("1,25", sanitizeDecimal("1.2.5"))
    }

    /** Basta virgul yazilabiliyor - ara hal, henuz sayi degil ama silinmiyor. */
    @Test
    fun aLoneSeparatorIsAllowedAsAnIntermediateState() {
        assertEquals(",", sanitizeDecimal(","))
        assertEquals("1,", sanitizeDecimal("1,"))
    }
}
