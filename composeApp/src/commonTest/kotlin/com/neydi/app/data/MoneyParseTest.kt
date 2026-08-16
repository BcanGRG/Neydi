package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [parseMinor] - OCR metninden tutar okuma.
 *
 * Vakalar GERCEK FISLERIN OCR ciktisindan geliyor (fis doneminden E2 ile
 * tasindi); etiketin buyuk fiyat rakami ayni bicimlerle basiliyor, kurallar
 * degismeden gecerli.
 */
class MoneyParseTest {

    /** Ilk surum noktayi REDDEDIYORDU ve iki zincirin ikisi de nokta basiyor. */
    @Test
    fun parsesDotAsDecimalSeparator() {
        assertEquals(10600, parseMinor("106.00"))
        assertEquals(48458, parseMinor("484.58"))
        assertEquals(18, parseMinor("0.18"))
    }

    /** Virgul de kabul: baska zincirler oyle basabilir, ikisini de destekliyoruz. */
    @Test
    fun parsesCommaAsDecimalSeparator() {
        assertEquals(1250, parseMinor("12,50"))
        assertEquals(123456, parseMinor("1.234,56"))
        assertEquals(123456, parseMinor("1,234.56"))
    }

    /** OCR yildizi bazen `x` okuyor, bazen hic okumuyor - ucu de gecmeli. */
    @Test
    fun acceptsStarOrXOrNoPrefix() {
        assertEquals(10600, parseMinor("*106.00"))
        assertEquals(48458, parseMinor("x484.58"))
        assertEquals(480, parseMinor("4.80"))
    }

    /** Iki ondalik hanesi olmayan sayi PARA DEGIL. */
    @Test
    fun rejectsNonMoneyNumbers() {
        assertNull(parseMinor("1"))
        assertNull(parseMinor("0.182"))
        assertNull(parseMinor("%1."))
        assertNull(parseMinor("abc"))
    }

    /** Eksi tutar: indirim satirlari eksi basiyor. */
    @Test
    fun parsesNegativeAmounts() {
        assertEquals(-10495, parseMinor("-104,95"))
    }
}
