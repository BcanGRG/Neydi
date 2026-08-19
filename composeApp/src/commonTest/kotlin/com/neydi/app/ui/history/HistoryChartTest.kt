package com.neydi.app.ui.history

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Gecmis mini grafiginin iki sessiz kurali (karar 68).
 *
 * NEDEN TEST GEREKLI: ikisi de bozuldugunda ekran COKMUYOR, sadece yanlis bir
 * olcek gosteriyor. Esik bir kayarsa iki gezilik "grafik" cikiyor - tek
 * cubuklu grafik grafik degildir; tutari bilinmeyen gezi listeden dusurulurse
 * kalan cubuklar kayiyor ve kullanici yanindaki gezinin cubuguna bakip baska
 * bir gezinin tutarini okuyor. Ikisi de goz denetiminden gecer.
 *
 * Esik sayilari BILE BILE elle yaziliyor: sabiti okusaydik test sabitin
 * degismesini degil, yalnizca kendi kendine tutarli kalmasini dogrulardi.
 */
class HistoryChartTest {

    /**
     * Gezileri ekrana geldikleri sirada kurar: EN YENI BASTA
     * (`observeHistory` `completedAt DESC` donduruyor).
     */
    private fun trips(vararg totals: Long?): List<HistoryTrip> =
        totals.mapIndexed { i, total ->
            HistoryTrip(
                id = "t$i",
                closedAt = 1_755_000_000_000L - i * 86_400_000L,
                itemCount = 5,
                estimateMinor = total,
            )
        }

    // --- Esik ---------------------------------------------------------------

    /** Iki tutarli gezi: esigin altinda, grafik HIC cizilmiyor. */
    @Test
    fun chartIsNotDrawnBelowThreeTripsWithTotals() {
        assertTrue(tripTotalBars(trips(40_000L, null, 30_000L, null)).isEmpty())
    }

    /** Uc tutarli gezi esigi tam karsiliyor - grafik ciziliyor. */
    @Test
    fun threeTripsWithTotalsDrawTheChart() {
        assertEquals(3, tripTotalBars(trips(40_000L, 30_000L, 20_000L)).size)
    }

    /**
     * Esik PENCEREYE bakiyor, tum gecmise degil.
     *
     * Son alti gezinin yalnizca ikisinin tutari var; daha eski gezilerde tutar
     * bulunmasi grafigi kurtarmiyor, cunku cizilecek olan sey o alti cubuk.
     */
    @Test
    fun olderTripsOutsideTheWindowDoNotSatisfyTheThreshold() {
        val bars = tripTotalBars(
            trips(null, 40_000L, null, null, 30_000L, null, 20_000L, 25_000L),
        )
        assertTrue(bars.isEmpty())
    }

    /**
     * Tutarlarin hepsi sifirsa grafik cizilmiyor.
     *
     * Sifir boy cubuklardan olusan bir grafik "bedava alisveris" iddiasidir -
     * gezi satirinin "0 TL yazmiyoruz" kuraliyla ayni sebep.
     */
    @Test
    fun allZeroTotalsDrawNoChart() {
        assertTrue(tripTotalBars(trips(0L, 0L, 0L, 0L)).isEmpty())
    }

    // --- Tutari bilinmeyen gezi ---------------------------------------------

    /**
     * Tutari hesaplanamayan gezi YERINI KORUYOR: listeden dusmuyor, `null`
     * olarak duruyor (cizimde dolgusuz kesik konturlu sabit cubuk).
     *
     * Ayni vaka pencerenin YONUNU de tutuyor: gezi listesi en yeniden eskiye
     * geliyor, grafik ise zamani soldan saga okutuyor.
     */
    @Test
    fun tripWithoutTotalKeepsItsPlace() {
        // En yeni -> en eski: 40_000, tutarsiz, 30_000, 20_000.
        val bars = tripTotalBars(trips(40_000L, null, 30_000L, 20_000L))

        // Soldan saga: en eski (20_000), 30_000, tutarsiz, en yeni (40_000).
        assertEquals(listOf(0.5f, 0.75f, null, 1f), bars)
    }

    /** Tutarsiz gezi esigi saymaya girmiyor ama cubugunu kaybetmiyor. */
    @Test
    fun unknownTotalDoesNotCountTowardTheThreshold() {
        val bars = tripTotalBars(trips(null, 30_000L, 20_000L, 10_000L, null))

        assertEquals(5, bars.size)
        assertEquals(listOf(true, false, false, false, true), bars.map { it == null })
    }

    // --- Pencere -------------------------------------------------------------

    /** Grafik alti cubuk: daha eski geziler pencereye girmiyor. */
    @Test
    fun windowIsTheSixMostRecentTrips() {
        val bars = tripTotalBars(trips(60_000L, 50_000L, 40_000L, 30_000L, 20_000L, 10_000L, 90_000L))

        assertEquals(6, bars.size)
        // Pencerenin disinda kalan 90_000 olcegi belirlese en buyuk cubuk
        // 60_000 icin 1f olmazdi - eski gezi grafigin tamamini kisaltirdi.
        assertEquals(1f, bars.last())
    }
}
