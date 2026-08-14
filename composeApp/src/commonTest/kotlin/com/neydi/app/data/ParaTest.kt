package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Para bicimlendirmesi TURKCE: ondalik VIRGUL, binlik NOKTA.
 * commonMain'de locale bilen bir bicimlendirici yok, o yuzden elle yaziliyor
 * ve elle yazilan her sey gibi test edilmesi gerekiyor.
 */
class ParaTest {

    @Test
    fun temelBicimlendirme() {
        assertEquals("289,00 TL", kurusFormatla(28900))
        assertEquals("0,50 TL", kurusFormatla(50))
        assertEquals("0,05 TL", kurusFormatla(5))
        assertEquals("0,00 TL", kurusFormatla(0))
    }

    /** Binlik ayirici NOKTA - 1289,90 degil 1.289,90. */
    @Test
    fun binlikAyirici() {
        assertEquals("1.289,90 TL", kurusFormatla(128990))
        assertEquals("12.345,67 TL", kurusFormatla(1234567))
        assertEquals("1.234.567,89 TL", kurusFormatla(123456789))
    }

    /** Kurus tek haneliyse basina sifir: "5,5 TL" degil "5,05 TL". */
    @Test
    fun tekHaneliKurusSifirlanir() {
        assertEquals("5,05 TL", kurusFormatla(505))
        assertEquals("5,50 TL", kurusFormatla(550))
    }

    /** Indirim satirlari negatif olabilir. */
    @Test
    fun negatifTutar() {
        assertEquals("-12,50 TL", kurusFormatla(-1250))
        assertEquals("-1.000,00 TL", kurusFormatla(-100000))
    }

    @Test
    fun paraBirimiKaldirilabilir() {
        assertEquals("289,00", kurusFormatla(28900, paraBirimi = ""))
    }
}
