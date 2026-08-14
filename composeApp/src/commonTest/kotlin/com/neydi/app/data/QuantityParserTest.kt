package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QuantityParserTest {

    private fun waitFor(girdi: String, adet: Double, birim: String?, ad: String) {
        val m = parseQuantity(girdi)
        assertEquals(adet, m.adet, "adet yanlis: '$girdi' -> $m")
        assertEquals(birim, m.birim, "birim yanlis: '$girdi' -> $m")
        assertEquals(ad, m.ad, "ad yanlis: '$girdi' -> $m")
    }

    @Test
    fun splitsNumberAndUnit() {
        waitFor("2 kg elma", 2.0, "kg", "elma")
        waitFor("5 L Ayçiçek Yağı", 5.0, "L", "Ayçiçek Yağı")
        waitFor("500 g beyaz peynir", 500.0, "g", "beyaz peynir")
        waitFor("2 paket makarna", 2.0, "paket", "makarna")
    }

    /** Turkce ondalik virgul. Nokta da kabul - klavyeye gore ikisi de cikiyor. */
    @Test
    fun decimalCommaAndDot() {
        waitFor("1,5 kg domates", 1.5, "kg", "domates")
        waitFor("1.5 kg domates", 1.5, "kg", "domates")
        waitFor("0,5 L süt", 0.5, "L", "süt")
    }

    /** Birim yazimlari kanonik hale eslenmeli; "gr" ile "g" ayri birim degil. */
    @Test
    fun unitSpellingsAreCanonicalised() {
        waitFor("250 gr ceviz", 250.0, "g", "ceviz")
        waitFor("2 lt su", 2.0, "L", "su")
        waitFor("3 tane limon", 3.0, "adet", "limon")
        waitFor("2 KG elma", 2.0, "kg", "elma")
    }

    /** Birimsiz sayi: adet var, birim yok - cagiran taraf urunun varsayilanini kullanir. */
    @Test
    fun numberWithoutUnit() {
        waitFor("3 ekmek", 3.0, null, "ekmek")
        waitFor("2 yumurta", 2.0, null, "yumurta")
    }

    /**
     * TUTUCU DAVRANIS. Tanimadigi kelimeyi birim SANMAMALI - "tam" bir birim
     * degil, adin parcasi. Yanlis ayristirmak hic ayristirmamaktan kotu.
     */
    @Test
    fun unknownWordIsPartOfName() {
        waitFor("2 tam buğday ekmek", 2.0, null, "tam buğday ekmek")
        waitFor("3 büyük boy yumurta", 3.0, null, "büyük boy yumurta")
    }

    /** Sayiyla BASLAMAYAN metin hic ayristirilmaz - "Yumurta 10'lu" 10 adet degil. */
    @Test
    fun textNotStartingWithNumberIsUntouched() {
        waitFor("Yumurta 10'lu", 1.0, null, "Yumurta 10'lu")
        waitFor("Ekmek", 1.0, null, "Ekmek")
        waitFor("Pınar Süt 1 L", 1.0, null, "Pınar Süt 1 L")
    }

    @Test
    fun meaninglessQuantityIsNotParsed() {
        waitFor("0 kg elma", 1.0, null, "0 kg elma")
        // Yalnizca sayi ve birim, ad yok: ad bos kalamaz.
        waitFor("2 kg", 1.0, null, "2 kg")
    }

    @Test
    fun whitespaceIsCollapsed() {
        waitFor("  2 kg   elma  ", 2.0, "kg", "elma")
        waitFor("   Ekmek   ", 1.0, null, "Ekmek")
    }

    /** Ayristirilan ad matchKey'e girecek; bos ad urun olusturamaz. */
    @Test
    fun emptyInput() {
        val m = parseQuantity("")
        assertEquals(1.0, m.adet)
        assertNull(m.birim)
        assertEquals("", m.ad)
    }
}
