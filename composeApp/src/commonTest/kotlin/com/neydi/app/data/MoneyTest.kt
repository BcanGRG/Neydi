package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Para bicimlendirmesi TURKCE: ondalik VIRGUL, binlik NOKTA.
 * commonMain'de locale bilen bir bicimlendirici yok, o yuzden elle yaziliyor
 * ve elle yazilan her sey gibi test edilmesi gerekiyor.
 */
class MoneyTest {

    @Test
    fun formatsBasicAmounts() {
        assertEquals("289,00 TL", formatMinor(28900))
        assertEquals("0,50 TL", formatMinor(50))
        assertEquals("0,05 TL", formatMinor(5))
        assertEquals("0,00 TL", formatMinor(0))
    }

    /** Binlik ayirici NOKTA - 1289,90 degil 1.289,90. */
    @Test
    fun usesDotForThousands() {
        assertEquals("1.289,90 TL", formatMinor(128990))
        assertEquals("12.345,67 TL", formatMinor(1234567))
        assertEquals("1.234.567,89 TL", formatMinor(123456789))
    }

    /** Kurus tek haneliyse basina sifir: "5,5 TL" degil "5,05 TL". */
    @Test
    fun padsSingleDigitMinor() {
        assertEquals("5,05 TL", formatMinor(505))
        assertEquals("5,50 TL", formatMinor(550))
    }

    /** Indirim satirlari negatif olabilir. */
    @Test
    fun formatsNegativeAmounts() {
        assertEquals("-12,50 TL", formatMinor(-1250))
        assertEquals("-1.000,00 TL", formatMinor(-100000))
    }

    @Test
    fun currencySuffixCanBeDropped() {
        assertEquals("289,00", formatMinor(28900, paraBirimi = ""))
    }
}
