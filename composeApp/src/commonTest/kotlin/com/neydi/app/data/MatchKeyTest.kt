package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MatchKeyTest {

    /**
     * ADIMIN VARLIK SEBEBI. Once naif yolun BOZUK oldugunu gosteriyoruz,
     * sonra bizimkinin duzelttigini. Ilk iddia dusürse `lowercase()` artik
     * guvenli demektir ve bu dosyanin yarisi gereksizlesir - o zaman da
     * bilerek silinir, kazara degil.
     */
    @Test
    fun naiveLowercaseBreaksTurkish() {
        val naive = "İNCİR".lowercase()

        assertNotEquals(
            "incir",
            naive,
            "lowercase() artik Turkce'yi dogru katliyor - matchKey sadelestirilebilir",
        )
        // "İNCİR" bes harf; her İ'nin ardina U+0307 (775) eklendigi icin
        // sonuc YEDI kod noktasi: [105, 775, 110, 99, 105, 775, 114].
        assertEquals(7, naive.length, "beklenen bozulma bu degil: ${naive.map { it.code }}")
        assertTrue(naive.contains('̇'), "birlestirici nokta bekleniyordu")
    }

    @Test
    fun matchKeyReducesOneProductToOneKey() {
        assertEquals("incir", matchKey("İNCİR"))
        assertEquals("incir", matchKey("İncir"))
        assertEquals("incir", matchKey("incir"))
        assertEquals("incir", matchKey("INCIR"))
        assertEquals("incir", matchKey("ıncır"))
    }

    /** Fisin yazdigi ile kullanicinin yazdigi bulusmali - uygulamanin asil derdi. */
    @Test
    fun receiptTextAndUserTextShareKey() {
        val receipt = matchKey("AYCICEK YAGI 5 L")
        val user = matchKey("Ayçiçek Yağı 5 L")
        assertEquals(receipt, user)
        assertEquals("aycicek yagi 5 l", receipt)
    }

    @Test
    fun allTurkishLettersAreFolded() {
        assertEquals("sgucoi", matchKey("şğüçöı"))
        assertEquals("sgucoi", matchKey("ŞĞÜÇÖI"))
        assertEquals("bugday", matchKey("Buğday"))
        assertEquals("seftali", matchKey("Şeftali"))
        assertEquals("ispanak", matchKey("Ispanak"))
        assertEquals("ispanak", matchKey("ıspanak"))
    }

    /**
     * Noktalama BOSLUGA cevrilir, silinmez. Silinseydi "t bugday" yerine
     * "tbugday" cikardi ve hicbir kullanici girdisiyle eslesmezdi.
     */
    @Test
    fun punctuationBecomesSpace() {
        assertEquals("t bugday ekmek 500g", matchKey("T.BUGDAY EKMEK 500G"))
        // "%1" ve "1 L" iki AYRI sayi; ikisi de anahtarda kalir.
        assertEquals("pinar sut 1 1 l", matchKey("Pınar Süt %1 - 1 L"))
        assertEquals("kasar peyniri", matchKey("  Kaşar   Peyniri  "))
    }

    @Test
    fun emptyInputGivesEmptyKey() {
        assertEquals("", matchKey(""))
        assertEquals("", matchKey("   "))
        assertEquals("", matchKey("... --- ..."))
    }

    /**
     * Bilincli tavizin kaydi: ı ve i carpisiyor. Bu bir HATA DEGIL, secim -
     * ve secimin degistigi gun bu test kirilarak haber verir.
     */
    @Test
    fun dottedAndDotlessIcollideDeliberately() {
        assertEquals(matchKey("ısırgan"), matchKey("isirgan"))
    }

    /** Anahtar deterministik olmali; ayni girdi hep ayni cikti. */
    @Test
    fun deterministic() {
        val input = "Tam Buğday Ekmeği 500 g"
        assertEquals(matchKey(input), matchKey(input))
        assertEquals("tam bugday ekmegi 500 g", matchKey(input))
    }
}
