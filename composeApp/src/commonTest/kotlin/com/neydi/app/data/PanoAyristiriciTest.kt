package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PanoAyristiriciTest {

    /** Gercek bir WhatsApp listesi neye benziyor. */
    private val whatsapp = """
        - ekmek
        - 2 kg elma
        - süt
        • yumurta
        1. deterjan
        2) peçete
    """.trimIndent()

    @Test
    fun maddeIsaretleriTemizlenir() {
        assertEquals(
            listOf("ekmek", "2 kg elma", "süt", "yumurta", "deterjan", "peçete"),
            panoSatirlari(whatsapp),
        )
    }

    /** Isaretlenmis listeler kopyalaniyor; onay isaretleri ada ait degil. */
    @Test
    fun onayIsaretleriTemizlenir() {
        assertEquals(
            listOf("ekmek", "süt"),
            panoSatirlari("✅ ekmek\n✔ süt"),
        )
    }

    @Test
    fun bosSatirlarAtilir() {
        assertEquals(listOf("ekmek", "süt"), panoSatirlari("ekmek\n\n   \nsüt\n"))
    }

    /**
     * ESIK UC SATIR. Iki satirlik pano cogu zaman kopyalanmis bir cumle;
     * her metin parcasinda cip cikarmak cipi gurultuye cevirir ve goz onu
     * gormemeye baslar.
     */
    @Test
    fun ucSatirEsigi() {
        assertFalse(panoListeMi("ekmek\nsüt"))
        assertTrue(panoListeMi("ekmek\nsüt\nyumurta"))
        assertFalse(panoListeMi(null))
        assertFalse(panoListeMi(""))
    }

    /** Kopyalanan bir paragraf liste DEGILDIR. */
    @Test
    fun uzunSatirlarAtilir() {
        val paragraf = "a".repeat(120)
        assertTrue(panoSatirlari("$paragraf\n$paragraf\n$paragraf").isEmpty())
    }

    /** Panodan gelen satirlar da miktar ayristiricidan gecmeli. */
    @Test
    fun panoSatirlariMiktarTasiyabilir() {
        val satirlar = panoSatirlari(whatsapp)
        val elma = miktarAyristir(satirlar[1])
        assertEquals(2.0, elma.adet)
        assertEquals("kg", elma.birim)
        assertEquals("elma", elma.ad)
    }
}
