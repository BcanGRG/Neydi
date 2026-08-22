package com.neydi.app.data.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Rehberin kareye eslenmesi (karar 74) - `PreviewView` FILL_CENTER'in tersi.
 *
 * Sayilar gercek cihazdan: vizor 1080x2047, kare 3024x4032 (dik). Bu testin
 * isi bir formulu ezberlemek degil, kirpimin KAYNAGINI kilitlemek: serit
 * kullanicinin kadraja oturttugu pikseli gostermek zorunda, karenin merkezini
 * degil.
 */
class GuideCropTest {

    /** Cihazdan olculen vizor; rehber tam genislikte ve 3:2. */
    private fun deviceGuide(
        previewW: Int = 1080,
        previewH: Int = 2047,
        left: Int = 22,
        top: Int = 700,
    ) = GuideBox(
        previewWidth = previewW,
        previewHeight = previewH,
        left = left,
        top = top,
        width = previewW - 2 * left,
        height = (previewW - 2 * left) * 2 / 3,
    )

    /**
     * FILL_CENTER DIKEY DOLDURUYOR, YATAY KIRPIYOR - ve hesap bunu biliyor.
     *
     * 1080x2047 vizorde 3024x4032 kare: olcek `max(1080/3024, 2047/4032)` =
     * 0,5077 (yukseklikten). Gorunen genislik 1080/0,5077 = 2127 piksel, yani
     * karenin 3024 pikselinin 897'si - her yandan 448 - HIC GORUNMUYOR.
     *
     * Rehber bu yuzden `left = 22`de baslasa bile karede 448 + 43 = 491
     * civarinda basliyor.
     */
    @Test
    fun theCropStartsWhereTheHiddenMarginEnds() {
        val rect = assertNotNull(deviceGuide().inImage(3024, 4032))
        assertTrue(rect.left in 480..500, "sol kenar ${rect.left}")
        assertTrue(rect.width in 2020..2130, "genislik ${rect.width}")
    }

    /**
     * KIRPIM REHBERIN ORANINI KORUYOR - 3:2.
     *
     * Serit ne kadar kisa cizilirse cizilsin gosterdigi bolge rehberin
     * kendisi; oran kaymasi seridin baska bir seyi gostermesi demek olurdu.
     */
    @Test
    fun theCropKeepsTheGuidesThreeToTwoRatio() {
        val rect = assertNotNull(deviceGuide().inImage(3024, 4032))
        val ratio = rect.width.toDouble() / rect.height
        assertTrue(ratio in 1.45..1.55, "oran $ratio")
    }

    /**
     * REHBER YUKARIDAYSA KIRPIM DA YUKARIDA - merkez kirpimin YAPMADIGI sey.
     *
     * Eski davranis her zaman karenin ortasini aliyordu. Iki farkli dikey
     * konumun ayni dikdortgeni vermesi, kirpimin rehberi hic dinlemedigi
     * anlamina gelirdi.
     */
    @Test
    fun movingTheGuideMovesTheCrop() {
        val high = assertNotNull(deviceGuide(top = 300).inImage(3024, 4032))
        val low = assertNotNull(deviceGuide(top = 1200).inImage(3024, 4032))
        assertTrue(low.top > high.top + 1500, "yukarida ${high.top}, asagida ${low.top}")
        assertEquals(high.width, low.width, "yatay konum degismemeliydi")
    }

    /**
     * DIKDORTGEN KARENIN DISINA TASMIYOR.
     *
     * `Bitmap.createBitmap` tasan bir dikdortgende ISTISNA atiyor ve o istisna
     * seridi tamamen goturur. Rehber vizorun kenarina dayandiginda bolme
     * yuvarlamasi kareyi birkac piksel asabiliyor.
     */
    @Test
    fun theRectNeverLeavesTheImage() {
        val edge = GuideBox(
            previewWidth = 1080, previewHeight = 2047,
            left = 0, top = 0, width = 1080, height = 2047,
        )
        val rect = assertNotNull(edge.inImage(3024, 4032))
        assertTrue(rect.left >= 0 && rect.top >= 0)
        assertTrue(rect.left + rect.width <= 3024, "sag kenar tasti")
        assertTrue(rect.top + rect.height <= 4032, "alt kenar tasti")
    }

    /**
     * OLCULMEMIS KUTU KIRPIM URETMIYOR.
     *
     * `null` "kirpma" demek ve cagiran taraf merkez kirpimina dusuyor. Sifir
     * boyutlu bir dikdortgen uretmek seridi tamamen bos birakirdi.
     */
    @Test
    fun anUnmeasuredBoxProducesNothing() {
        assertNull(GuideBox(0, 0, 0, 0, 0, 0).inImage(3024, 4032))
        assertNull(deviceGuide().inImage(0, 0))
        assertTrue(!GuideBox(1080, 2047, 22, 700, 0, 0).usable)
    }

    /**
     * VIZOR KAREDEN GENIS OLURSA (yatay) hesap yine calisiyor.
     *
     * Yatay duzen henuz yok (karar 61) ama kutu geldiginde bu fonksiyon
     * degismemeli: FILL_CENTER'in kurali yonden bagimsiz.
     */
    @Test
    fun aLandscapeViewfinderCropsVerticallyInstead() {
        val landscape = GuideBox(
            previewWidth = 2047, previewHeight = 1080,
            left = 400, top = 100, width = 1200, height = 800,
        )
        val rect = assertNotNull(landscape.inImage(4032, 3024))
        assertTrue(rect.left > 0 && rect.width in 2200..2500, "genislik ${rect.width}")
        assertTrue(rect.top + rect.height <= 3024)
    }
}
