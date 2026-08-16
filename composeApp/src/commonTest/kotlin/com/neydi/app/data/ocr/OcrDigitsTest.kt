package com.neydi.app.data.ocr

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [normalizeDigits] - OCR'in harfe cevirdigi rakamlari geri alma.
 *
 * Ornekler gercek fis OCR ciktisindan: `S0` (50 okunmali), `869O508101426`
 * (barkodda sifir yerine harf O). Fonksiyon fis doneminden degismeden
 * tasindi (E2); dogrudan testi o donemde yoktu, burada kazandi.
 */
class OcrDigitsTest {

    @Test
    fun fixesLetterOToZero() {
        assertEquals("8690508101426", normalizeDigits("869O508101426"))
    }

    @Test
    fun fixesLetterSToFive() {
        assertEquals("50", normalizeDigits("S0"))
        assertEquals("55", normalizeDigits("sS"))
    }

    @Test
    fun fixesLetterIVariantsToOne() {
        assertEquals("111", normalizeDigits("Ili"))
    }

    @Test
    fun leavesRealDigitsAndOtherCharsAlone() {
        assertEquals("1234567890", normalizeDigits("1234567890"))
        assertEquals("12,34", normalizeDigits("12,34"))
    }
}
