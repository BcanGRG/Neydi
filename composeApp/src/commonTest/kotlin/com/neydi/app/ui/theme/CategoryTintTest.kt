package com.neydi.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.neydi.app.data.catalog.SEED_CATEGORIES
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Kategori kutucugu tonlarinin KONTRAST NOBETCISI (F6.9).
 *
 * Tohumlanan 12 ton orta-koyu ve doygun; dogrudan dolgu olarak kullanilsalardi
 * uzerlerindeki metin okunmazdi. `CategoryTint` onlari zeminle karistiriyor ve
 * bu dosya karisimin GERCEKTEN okunabilir kaldigini olcuyor.
 *
 * OLCUM, IDDIA DEGIL: bir ton eklenir, degistirilir ya da karisim orani
 * oynatilirsa burasi bagirir. Palet testinin (`NeydiColorTest`) ayni yontemi.
 */
class CategoryTintTest {

    /**
     * Govde metni esigi. WCAG AA buyuk metin icin 3:1 ister; kutucuktaki
     * iki harf 20sp/800, yani "buyuk metin" tanimina giriyor.
     */
    private val minContrast = 3.0

    private val lightSurface = Color(0xFFFBF7F2)
    private val lightText = Color(0xFF221A14)
    private val darkSurface = Color(0xFF13100E)
    private val darkText = Color(0xFFF5EDE6)

    /** sRGB kanalini dogrusal isiga cevirir (WCAG 2.x). */
    private fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun Color.luminance(): Double =
        0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)

    private fun contrast(a: Color, b: Color): Double {
        val hi = maxOf(a.luminance(), b.luminance())
        val lo = minOf(a.luminance(), b.luminance())
        return (hi + 0.05) / (lo + 0.05)
    }

    /** Isik modunda 12 tonun 12'si de govde metnini tasiyabilmeli. */
    @Test
    fun everyCategoryTintIsReadableInLightTheme() {
        SEED_CATEGORIES.forEach { category ->
            val fill = CategoryTint.fill(category.tintArgb, lightSurface, isLight = true)
            val ratio = contrast(fill, lightText)
            assertTrue(
                ratio >= minContrast,
                "${category.name} isik modunda ${format(ratio)}:1 - en az $minContrast:1 olmali",
            )
        }
    }

    /** Karanlik modda da ayni sart. */
    @Test
    fun everyCategoryTintIsReadableInDarkTheme() {
        SEED_CATEGORIES.forEach { category ->
            val fill = CategoryTint.fill(category.tintArgb, darkSurface, isLight = false)
            val ratio = contrast(fill, darkText)
            assertTrue(
                ratio >= minContrast,
                "${category.name} karanlik modda ${format(ratio)}:1 - en az $minContrast:1 olmali",
            )
        }
    }

    /**
     * KUTUCUK ZEMINDEN AYRISMALI.
     *
     * Roadmap'in F6.9'da kaydettigi ikinci tuzak: karanlik temada bazi tonlar
     * zeminden "neredeyse ayrismiyor". Kutucuk gorunmuyorsa rengi dogru olsa
     * bile bir kutucuk degil.
     */
    @Test
    fun tilesSeparateFromTheirBackground() {
        SEED_CATEGORIES.forEach { category ->
            listOf(
                Triple(lightSurface, true, "ışık"),
                Triple(darkSurface, false, "karanlık"),
            ).forEach { (surface, isLight, label) ->
                val fill = CategoryTint.fill(category.tintArgb, surface, isLight)
                val ratio = contrast(fill, surface)
                assertTrue(
                    ratio >= 1.12,
                    "${category.name} $label modunda zeminden ayrışmıyor (${format(ratio)}:1)",
                )
            }
        }
    }

    /** Iki farkli kategori ayni rengi almamali - ton bilgisi korunmali. */
    @Test
    fun distinctCategoriesKeepDistinctTints() {
        val fills = SEED_CATEGORIES.map { CategoryTint.fill(it.tintArgb, lightSurface, isLight = true) }

        assertTrue(fills.toSet().size == SEED_CATEGORIES.size, "tonlar birbirine cokmus")
    }

    private fun format(d: Double): String = ((d * 100).toInt() / 100.0).toString()
}
