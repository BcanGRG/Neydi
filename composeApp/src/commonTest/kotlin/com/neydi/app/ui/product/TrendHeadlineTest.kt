package com.neydi.app.ui.product

import com.neydi.app.data.db.ObservationRow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

/**
 * Trend mansetinin uc kurali (karar 67).
 *
 * Veritabani YOK, cunku sinanan sey sorgu degil CUMLE: `toPriceSection` gozlem
 * listesini metne ceviriyor ve mansetin uc kurali da o cevrimde yasiyor.
 * Sorgunun kendi testi [com.neydi.app.data.db.ProductPriceSectionTest].
 *
 * GUN SAYISI DEGIL AY ADI SINANIYOR: `toPriceSection` saat dilimi almiyor,
 * sistem dilimiyle bicimliyor. Ogle vakti UTC damgasi her dilimde ayni AYIN
 * icinde kaliyor ama gun numarasi UTC+13'te kayabilir; ekin ve gunun tam hali
 * [com.neydi.app.data.DayMonthAblativeTest]'te sabit dilimle sinaniyor.
 */
@OptIn(ExperimentalTime::class)
class TrendHeadlineTest {

    private fun an(ay: Int, gun: Int, yil: Int = 2026): Long =
        LocalDateTime(yil, ay, gun, 12, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()

    private val now = an(8, 19)

    private fun obs(
        minor: Long,
        at: Long,
        packSize: Double? = null,
        packUnit: String? = null,
    ) = ObservationRow(
        id = "o-$at-$minor",
        observedAt = at,
        unitPriceMinor = minor,
        brand = null,
        storeName = "Migros",
        packSize = packSize,
        packUnit = packUnit,
    )

    /** Gozlemler YENIDEN ESKIYE geliyor - sorgunun kendi sirasi. */
    private fun headline(vararg rows: ObservationRow) =
        rows.toList().toPriceSection(now).headline

    // --- 1 · Aralik veriden -------------------------------------------------

    /**
     * ARALIK KULLANILAN ILK GOZLEMDEN, sabit bir "son 3 ay"dan degil.
     *
     * Eski maket metni ("son 3 ayda %28 arttı") iki haftalik da uc yillik da
     * olsa ayni cumleyi yazardi; oysa yuzde tam olarak o iki ucun arasinda
     * olculuyor.
     */
    @Test
    fun theRangeIsTheDateOfTheOldestObservationUsed() {
        val text = headline(
            obs(4_100, an(8, 14)),
            obs(3_700, an(7, 2)),
            obs(3_200, an(6, 6)),
        )
        assertTrue(text!!.contains("Haziran'dan beri"), text)
        // EN YENI gozlemin ayi degil: aralik nereden BASLADIGINI soyluyor.
        assertFalse(text.contains("Ağustos"), text)
    }

    /** Kararin kendi ornegi, bastan sona. */
    @Test
    fun theWholeSentence() {
        val text = headline(
            obs(4_100, an(8, 14)),
            obs(3_700, an(7, 2)),
            obs(3_200, an(6, 6)),
        )!!
        assertTrue(text.startsWith("32 TL → 41 TL · "), text)
        assertTrue(text.endsWith(" beri %28 arttı"), text)
    }

    /** Fiyat dusmusse cumle de dusuyor - ikonografinin sozcugu ("Fiyat düştü"). */
    @Test
    fun aFallingPriceSaysDustu() {
        val text = headline(
            obs(3_200, an(8, 14)),
            obs(3_700, an(7, 2)),
            obs(4_100, an(6, 6)),
        )!!
        assertTrue(text.startsWith("41 TL → 32 TL · "), text)
        assertTrue(text.endsWith(" beri %22 düştü"), text)
    }

    // --- 2 · Kurus sifirsa yazilmiyor ---------------------------------------

    /** Kurus varsa DURUYOR: elli kurusu yuvarlamak gozlemi degistirmek olurdu. */
    @Test
    fun kurusSurvivesWhenItIsNotZero() {
        val text = headline(
            obs(4_100, an(8, 14)),
            obs(3_700, an(7, 2)),
            obs(3_250, an(6, 6)),
        )!!
        assertTrue(text.startsWith("32,50 TL → 41 TL · "), text)
        assertTrue(text.endsWith(" beri %26 arttı"), text)
    }

    /**
     * TILDE ASLA: gozlenmis fiyat kesin, tilde ise tahmin isareti.
     * `formatEstimate` ayni kurusu dusuruyor ama yanlis sozu veriyor.
     */
    @Test
    fun anObservedPriceNeverCarriesTheTilde() {
        val text = headline(
            obs(4_100, an(8, 14)),
            obs(3_700, an(7, 2)),
            obs(3_200, an(6, 6)),
        )!!
        assertFalse(text.contains("~"), text)
    }

    // --- 3 · Ambalaj degistiyse yuzde yok -----------------------------------

    /**
     * AMBALAJ DEGISTIYSE YUZDE IDDIA EDILMIYOR.
     *
     * 5 L -> 4 L arasindaki fiyat farki bir zam da olabilir indirim de; hangisi
     * oldugunu ambalaj sabit degilken sayi soyleyemez. Manset o yuzden ambalaj
     * cumlesine donuyor.
     */
    @Test
    fun aChangedPackReplacesThePercentage() {
        val text = headline(
            obs(4_100, an(8, 14), packSize = 4.0, packUnit = "L"),
            obs(3_700, an(7, 2), packSize = 5.0, packUnit = "L"),
            obs(3_200, an(6, 6), packSize = 5.0, packUnit = "L"),
        )
        assertEquals("Ambalaj küçüldü: 5 L → 4 L", text)
        assertFalse(text!!.contains("%"), text)
    }

    /** Ambalaj BUYUDUYSE "küçüldü" duz bir yalan olurdu. */
    @Test
    fun aGrownPackSaysBuyudu() {
        val text = headline(
            obs(4_100, an(8, 14), packSize = 5.0, packUnit = "L"),
            obs(3_700, an(7, 2), packSize = 4.0, packUnit = "L"),
            obs(3_200, an(6, 6), packSize = 4.0, packUnit = "L"),
        )
        assertEquals("Ambalaj büyüdü: 4 L → 5 L", text)
    }

    /**
     * BIRIMLER FARKLIYSA YON DE YOK: "900 g → 1 kg"da sayilar 900 ile 1,
     * yani buyukluk kiyasi sayiyla degil birimle yapilirdi.
     */
    @Test
    fun differentUnitsClaimNoDirection() {
        val text = headline(
            obs(4_100, an(8, 14), packSize = 1.0, packUnit = "kg"),
            obs(3_700, an(7, 2), packSize = 900.0, packUnit = "g"),
            obs(3_200, an(6, 6), packSize = 900.0, packUnit = "g"),
        )
        assertEquals("Ambalaj değişti: 900 g → 1 kg", text)
    }

    /**
     * AMBALAJ BILINMIYORSA DEGISIM DE IDDIA EDILMIYOR: `null` "ayni degil"
     * degil, "bilmiyorum" demek. Bilinmeyenden ambalaj degisimi cikarmak,
     * mansetin butun yuzdesini sebepsiz yere susturur.
     */
    @Test
    fun anUnknownPackIsNotAChange() {
        val text = headline(
            obs(4_100, an(8, 14), packSize = 4.0, packUnit = "L"),
            obs(3_700, an(7, 2)),
            obs(3_200, an(6, 6)),
        )!!
        assertTrue(text.startsWith("32 TL → 41 TL · "), text)
    }

    /** Ambalaj ayniysa yuzde normal yaziliyor - kontrol yalnizca DEGISIMI ariyor. */
    @Test
    fun anUnchangedPackDoesNotSuppressTheTrend() {
        val text = headline(
            obs(4_100, an(8, 14), packSize = 4.0, packUnit = "L"),
            obs(3_700, an(7, 2), packSize = 4.0, packUnit = "L"),
            obs(3_200, an(6, 6), packSize = 4.0, packUnit = "L"),
        )!!
        assertTrue(text.endsWith(" beri %28 arttı"), text)
    }

    // --- Esik ve kurulamayan cumle ------------------------------------------

    /** UC GOZLEM ESIGI - sparkline'la ayni. Ikisi bir dogru parcasi cizer. */
    @Test
    fun twoObservationsKeepTheSingleHeadline() {
        val text = headline(
            obs(4_100, an(8, 14)),
            obs(3_200, an(6, 6)),
        )
        assertEquals("Son ödediğin: 41,00 TL", text)
    }

    /** Esigin ALTINDA ambalaj degisimi de manseti degistirmiyor. */
    @Test
    fun packChangeBelowTheThresholdKeepsTheSingleHeadline() {
        val text = headline(
            obs(4_100, an(8, 14), packSize = 4.0, packUnit = "L"),
            obs(3_200, an(6, 6), packSize = 5.0, packUnit = "L"),
        )
        assertEquals("Son ödediğin: 41,00 TL", text)
    }

    /** Ucuncu gozlem esigi geciyor - sinir tam olarak burada. */
    @Test
    fun theThirdObservationTurnsOnTheTrend() {
        val text = headline(
            obs(4_100, an(8, 14)),
            obs(3_700, an(7, 2)),
            obs(3_200, an(6, 6)),
        )!!
        assertFalse(text.startsWith("Son ödediğin"), text)
    }

    /**
     * YUZDE SIFIRA YUVARLANIYORSA CUMLE DE YOK: *"%0 arttı"* okunacak bir sey
     * soylemiyor. Tek gozlem manseti geri geliyor - kararin esik altinda zaten
     * kullandigi cumle, uydurulmus bir "değişmedi" degil.
     */
    @Test
    fun anUnchangedPriceFallsBackToTheSingleHeadline() {
        val text = headline(
            obs(3_200, an(8, 14)),
            obs(3_700, an(7, 2)),
            obs(3_200, an(6, 6)),
        )
        assertEquals("Son ödediğin: 32,00 TL", text)
    }

    /**
     * ILK FIYAT SIFIRSA YUZDE YOK: fiyat alani elle duzenlenebiliyor ve
     * `0,00` yazilabiliyor, yani sifira bolme teorik bir korku degil.
     */
    @Test
    fun aZeroOldestPriceFallsBackToTheSingleHeadline() {
        val text = headline(
            obs(4_100, an(8, 14)),
            obs(3_700, an(7, 2)),
            obs(0, an(6, 6)),
        )
        assertEquals("Son ödediğin: 41,00 TL", text)
    }

    /** Gozlem yoksa manset de yok - sheet urun adini yaziyor. */
    @Test
    fun noObservationsMeansNoHeadline() {
        assertEquals(null, emptyList<ObservationRow>().toPriceSection(now).headline)
    }
}
