package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MiktarAyristiriciTest {

    private fun bekle(girdi: String, adet: Double, birim: String?, ad: String) {
        val m = miktarAyristir(girdi)
        assertEquals(adet, m.adet, "adet yanlis: '$girdi' -> $m")
        assertEquals(birim, m.birim, "birim yanlis: '$girdi' -> $m")
        assertEquals(ad, m.ad, "ad yanlis: '$girdi' -> $m")
    }

    @Test
    fun sayiVeBirimAyrilir() {
        bekle("2 kg elma", 2.0, "kg", "elma")
        bekle("5 L Ayçiçek Yağı", 5.0, "L", "Ayçiçek Yağı")
        bekle("500 g beyaz peynir", 500.0, "g", "beyaz peynir")
        bekle("2 paket makarna", 2.0, "paket", "makarna")
    }

    /** Turkce ondalik virgul. Nokta da kabul - klavyeye gore ikisi de cikiyor. */
    @Test
    fun ondalikVirgulVeNokta() {
        bekle("1,5 kg domates", 1.5, "kg", "domates")
        bekle("1.5 kg domates", 1.5, "kg", "domates")
        bekle("0,5 L süt", 0.5, "L", "süt")
    }

    /** Birim yazimlari kanonik hale eslenmeli; "gr" ile "g" ayri birim degil. */
    @Test
    fun birimYazimlariKanoniklesir() {
        bekle("250 gr ceviz", 250.0, "g", "ceviz")
        bekle("2 lt su", 2.0, "L", "su")
        bekle("3 tane limon", 3.0, "adet", "limon")
        bekle("2 KG elma", 2.0, "kg", "elma")
    }

    /** Birimsiz sayi: adet var, birim yok - cagiran taraf urunun varsayilanini kullanir. */
    @Test
    fun birimsizSayi() {
        bekle("3 ekmek", 3.0, null, "ekmek")
        bekle("2 yumurta", 2.0, null, "yumurta")
    }

    /**
     * TUTUCU DAVRANIS. Tanimadigi kelimeyi birim SANMAMALI - "tam" bir birim
     * degil, adin parcasi. Yanlis ayristirmak hic ayristirmamaktan kotu.
     */
    @Test
    fun taninmayanKelimeAdinParcasi() {
        bekle("2 tam buğday ekmek", 2.0, null, "tam buğday ekmek")
        bekle("3 büyük boy yumurta", 3.0, null, "büyük boy yumurta")
    }

    /** Sayiyla BASLAMAYAN metin hic ayristirilmaz - "Yumurta 10'lu" 10 adet degil. */
    @Test
    fun sayiylaBaslamayanMetinDokunulmaz() {
        bekle("Yumurta 10'lu", 1.0, null, "Yumurta 10'lu")
        bekle("Ekmek", 1.0, null, "Ekmek")
        bekle("Pınar Süt 1 L", 1.0, null, "Pınar Süt 1 L")
    }

    @Test
    fun anlamsizMiktarAyristirilmaz() {
        bekle("0 kg elma", 1.0, null, "0 kg elma")
        // Yalnizca sayi ve birim, ad yok: ad bos kalamaz.
        bekle("2 kg", 1.0, null, "2 kg")
    }

    @Test
    fun bosluklarTemizlenir() {
        bekle("  2 kg   elma  ", 2.0, "kg", "elma")
        bekle("   Ekmek   ", 1.0, null, "Ekmek")
    }

    /** Ayristirilan ad matchKey'e girecek; bos ad urun olusturamaz. */
    @Test
    fun bosGirdi() {
        val m = miktarAyristir("")
        assertEquals(1.0, m.adet)
        assertNull(m.birim)
        assertEquals("", m.ad)
    }
}
