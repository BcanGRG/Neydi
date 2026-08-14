package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClipboardParserTest {

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
    fun bulletsAreStripped() {
        assertEquals(
            listOf("ekmek", "2 kg elma", "süt", "yumurta", "deterjan", "peçete"),
            clipboardLines(whatsapp),
        )
    }

    /** Isaretlenmis listeler kopyalaniyor; onay isaretleri ada ait degil. */
    @Test
    fun checkMarksAreStripped() {
        assertEquals(
            listOf("ekmek", "süt"),
            clipboardLines("✅ ekmek\n✔ süt"),
        )
    }

    @Test
    fun blankLinesAreDropped() {
        assertEquals(listOf("ekmek", "süt"), clipboardLines("ekmek\n\n   \nsüt\n"))
    }

    /**
     * ESIK UC SATIR. Iki satirlik pano cogu zaman kopyalanmis bir cumle;
     * her metin parcasinda cip cikarmak cipi gurultuye cevirir ve goz onu
     * gormemeye baslar.
     */
    @Test
    fun threeLineThreshold() {
        assertFalse(looksLikeList("ekmek\nsüt"))
        assertTrue(looksLikeList("ekmek\nsüt\nyumurta"))
        assertFalse(looksLikeList(null))
        assertFalse(looksLikeList(""))
    }

    /** Kopyalanan bir paragraf liste DEGILDIR. */
    @Test
    fun longLinesAreDropped() {
        val paragraph = "a".repeat(120)
        assertTrue(clipboardLines("$paragraph\n$paragraph\n$paragraph").isEmpty())
    }

    /** Panodan gelen satirlar da miktar ayristiricidan gecmeli. */
    @Test
    fun clipboardLinesMayCarryQuantity() {
        val rows = clipboardLines(whatsapp)
        val apple = parseQuantity(rows[1])
        assertEquals(2.0, apple.count)
        assertEquals("kg", apple.unit)
        assertEquals("elma", apple.name)
    }
}
