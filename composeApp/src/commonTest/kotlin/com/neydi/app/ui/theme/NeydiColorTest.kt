package com.neydi.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * "Sicak Kiler" paletinin kontrast kilidi.
 *
 * Bu test bir tik kutusu degil: renk degistiren birinin degistirdigi seyi
 * OLCMEDEN gecmesini engelliyor. Palet dosyasindaki her kontrast iddiasinin
 * burada bir karsiligi var; iddia yanlissa test kirilir.
 *
 * WCAG 2.x sRGB bagil parlaklik. Esikler:
 *   4.5:1  normal metin (AA)          - 14sp bu kategoriye girer, "buyuk metin" DEGIL
 *   3.0:1  buyuk metin ve metin-disi UI sinirlari (AA)
 */
class NeydiColorTest {

    // --- WCAG hesabi ---------------------------------------------------------

    private fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun Color.luminance(): Double =
        0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)

    /** WCAG kontrast orani. Sira onemsiz - hangisi acik kendisi buluyor. */
    private fun contrast(a: Color, b: Color): Double {
        val la = a.luminance()
        val lb = b.luminance()
        val hi = maxOf(la, lb)
        val lo = minOf(la, lb)
        return (hi + 0.05) / (lo + 0.05)
    }

    private fun assertAtLeast(expected: Double, a: Color, b: Color, label: String) {
        val actual = contrast(a, b)
        assertTrue(
            actual >= expected,
            "$label kontrasti ${fmt(actual)}:1 - en az ${fmt(expected)}:1 olmali",
        )
    }

    private fun fmt(d: Double): String {
        val r = (d * 100).toInt() / 100.0
        return r.toString()
    }

    // --- Govde metni: AA (4.5:1) --------------------------------------------

    @Test
    fun isikModundaGovdeMetniAAGecer() {
        val s = NeydiLightColors
        assertAtLeast(4.5, s.onSurface, s.surface, "isik onSurface/surface")
        assertAtLeast(4.5, s.onSurfaceVariant, s.surface, "isik onSurfaceVariant/surface")
        assertAtLeast(4.5, s.onSurfaceVariant, s.surfaceVariant, "isik onSurfaceVariant/surfaceVariant")
        assertAtLeast(4.5, s.onPrimary, s.primary, "isik onPrimary/primary")
        assertAtLeast(4.5, LightExtraColors.onAccent, LightExtraColors.accent, "isik onAccent/accent")
    }

    @Test
    fun karanlikModdaGovdeMetniAAGecer() {
        val s = NeydiDarkColors
        assertAtLeast(4.5, s.onSurface, s.surface, "karanlik onSurface/surface")
        assertAtLeast(4.5, s.onSurfaceVariant, s.surface, "karanlik onSurfaceVariant/surface")
        assertAtLeast(4.5, s.onSurfaceVariant, s.surfaceVariant, "karanlik onSurfaceVariant/surfaceVariant")
        assertAtLeast(4.5, s.onPrimary, s.primary, "karanlik onPrimary/primary")
        assertAtLeast(4.5, DarkExtraColors.onAccent, DarkExtraColors.accent, "karanlik onAccent/accent")
    }

    /**
     * Fiyat oklari ve sparkline renk TASIYAN sinyal. Ok sekli anlami zaten
     * tasiyor (renk gormeyen kullanici icin), ama renkler yine de metin-disi
     * UI sinirini gecmeli.
     */
    @Test
    fun fiyatRenkleriMetinDisiEsigiGecer() {
        assertAtLeast(3.0, LightExtraColors.priceUp, NeydiLightColors.surface, "isik priceUp/surface")
        assertAtLeast(3.0, LightExtraColors.priceDown, NeydiLightColors.surface, "isik priceDown/surface")
        assertAtLeast(3.0, DarkExtraColors.priceUp, NeydiDarkColors.surface, "karanlik priceUp/surface")
        assertAtLeast(3.0, DarkExtraColors.priceDown, NeydiDarkColors.surface, "karanlik priceDown/surface")
    }

    // --- AMBER KURALI: testin asil sebebi -----------------------------------

    /**
     * `accentNeedsOutline` elle set edilen bir bayrak DEGIL, olculebilir bir
     * gercegin kaydi olmali: accent kendi sinirini surface uzerinde tasiyabiliyor mu.
     *
     * Bayragi olcumden TUREYEREK dogruluyoruz. Biri accent'i degistirip bayragi
     * guncellemezse - ya da tersi - test kirilir. Iki tarafi ayri ayri iddia
     * etseydik ikisi birlikte yanlis olabilirdi.
     */
    @Test
    fun accentKenarlikBayragiOlcumeUyar() {
        val isikOrani = contrast(LightExtraColors.accent, NeydiLightColors.surface)
        assertEquals(
            isikOrani < 3.0,
            LightExtraColors.accentNeedsOutline,
            "isik accent/surface ${fmt(isikOrani)}:1 ama accentNeedsOutline=" +
                "${LightExtraColors.accentNeedsOutline}. Bayrak olcumu takip etmeli.",
        )

        val karanlikOrani = contrast(DarkExtraColors.accent, NeydiDarkColors.surface)
        assertEquals(
            karanlikOrani < 3.0,
            DarkExtraColors.accentNeedsOutline,
            "karanlik accent/surface ${fmt(karanlikOrani)}:1 ama accentNeedsOutline=" +
                "${DarkExtraColors.accentNeedsOutline}. Bayrak olcumu takip etmeli.",
        )
    }

    /** Kenarlik zorunluysa kenarligin KENDISI gorunur olmali, yoksa kural bos. */
    @Test
    fun accentKenarligiSiniriCizecekKadarKoyu() {
        assertAtLeast(
            3.0,
            LightExtraColors.accentOutline,
            NeydiLightColors.surface,
            "isik accentOutline/surface",
        )
    }

    /**
     * Color.kt'nin yorumunda yazan sayilar. Dokumantasyon curur; bu test
     * curumesine izin vermez.
     */
    @Test
    fun belgelenenOranlarHalaDogru() {
        val beklenen = listOf(
            Triple("accent/surface (isik)", contrast(LightExtraColors.accent, NeydiLightColors.surface), 2.08),
            Triple("accentOutline/surface (isik)", contrast(LightExtraColors.accentOutline, NeydiLightColors.surface), 5.56),
            Triple("accent/surface (karanlik)", contrast(DarkExtraColors.accent, NeydiDarkColors.surface), 11.29),
        )
        beklenen.forEach { (ad, olculen, yazan) ->
            assertTrue(
                abs(olculen - yazan) < 0.01,
                "$ad: Color.kt $yazan:1 diyor, olculen ${fmt(olculen)}:1",
            )
        }
    }

    // --- Alfa bilesikleri: kullanicinin GERCEKTEN gordugu --------------------

    /**
     * Token oranlari yalnizca alfa uygulanmadiginda gecerli. ListItemRow
     * satirlara alfa uyguluyor ve okunan renk o alfadan sonraki bilesik renk.
     * Token testleri bu satirlari kacirir; bu yuzden ayrica olculuyor.
     */
    @Test
    fun sabitSatirlarHalaAAGecer() {
        listOf(
            "isik" to (NeydiLightColors.onSurface to NeydiLightColors.surface),
            "karanlik" to (NeydiDarkColors.onSurface to NeydiDarkColors.surface),
        ).forEach { (tema, renkler) ->
            val (fg, bg) = renkler
            val bilesik = fg.copy(alpha = STAPLE_ALPHA).compositeOver(bg)
            assertAtLeast(4.5, bilesik, bg, "$tema sabit satir adi (alfa $STAPLE_ALPHA)")
        }
    }

    /**
     * Isaretli satirlar AA'dan MUAF - kasitli.
     *
     * Uzeri cizili, "alindi" anlaminda ve birincil okuma hedefi degil; WCAG 1.4.3
     * etkisiz durumdaki bilesenleri disarida birakiyor. Yine de bir tabani var:
     * 3:1'in altina duserse satir gozden tamamen kaybolur ve "aldim mi almadim mi"
     * sorusu cevapsiz kalir. Isik modunda su an 3.80:1 - taban bilincli secildi.
     */
    @Test
    fun isaretliSatirlarKaybolmaz() {
        listOf(
            "isik" to (NeydiLightColors.onSurface to NeydiLightColors.surface),
            "karanlik" to (NeydiDarkColors.onSurface to NeydiDarkColors.surface),
        ).forEach { (tema, renkler) ->
            val (fg, bg) = renkler
            val bilesik = fg.copy(alpha = CHECKED_ALPHA).compositeOver(bg)
            assertAtLeast(3.0, bilesik, bg, "$tema isaretli satir adi (alfa $CHECKED_ALPHA)")
        }
    }

    private companion object {
        /** ListItemRow.rowAlpha ile ayni. Degisirse test de degismeli. */
        const val STAPLE_ALPHA = 0.70f
        const val CHECKED_ALPHA = 0.55f
    }
}
