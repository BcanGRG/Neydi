package com.neydi.app.ui.list

import com.neydi.app.data.clipboardLines
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TOPLU EKLEMENIN SAYISI (karar 91).
 *
 * Bu dosya `clipboardLines`in SAF kuralini kilitliyor; ViewModel'in kendisi
 * Room ve Main dispatcher istiyor, oysa sinanan sey bir SAYI.
 *
 * Sayinin onemi tasarimin gerekcesinde: toplu ekleme once sayiyi hesaplayip
 * ATIYORDU - on iki satir yapistirildiginda da sifir yapistirildiginda da
 * ekranda hicbir sey yazmiyordu. Ve sifir hali daha onemli: *"hicbir seyin
 * olmadigi ekran, calismayan uygulamadan ayirt edilemez."*
 */
class BulkAddTest {

    private val newline = "\n"

    /** Panodan gelen satirlar sayilabiliyor - toast'in rakami buradan. */
    @Test
    fun clipboardLinesAreCounted() {
        val text = listOf("Süt", "Ekmek", "Yumurta").joinToString(newline)
        assertEquals(3, clipboardLines(text).size)
    }

    /** Cozumlenemeyen pano SIFIR satir veriyor - toast'in ikinci cumlesi. */
    @Test
    fun anUnparseableClipboardYieldsNothing() {
        assertEquals(0, clipboardLines("   ").size)
    }
}
