package com.neydi.app.ui

import com.neydi.app.data.db.Trip
import com.neydi.app.data.parseMinorInput
import com.neydi.app.ui.finish.quantityBadge
import com.neydi.app.ui.history.combineTrips
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull

/**
 * Ekranlarin ViewModel'siz saf mantigi.
 *
 * Compose testi degil: burada test edilenler bir ekrana bagli olmayan
 * donusumler ve tam bu yuzden ayri fonksiyonlar - Composable icinde kalsalardi
 * ancak enstrumentasyon testiyle dogrulanabilirlerdi.
 */
class ListUiLogicTest {

    // --- Kullanici tutar girdisi --------------------------------------------

    /** Klavye virgul veriyor, fisin kendisi nokta basiyor: IKISI DE kabul. */
    @Test
    fun acceptsBothDecimalSeparators() {
        assertEquals(10600, parseMinorInput("106,00"))
        assertEquals(10600, parseMinorInput("106.00"))
        assertEquals(10600, parseMinorInput("106"))
        assertEquals(4858, parseMinorInput("48,58"))
    }

    @Test
    fun padsSingleDecimalDigit() {
        assertEquals(1250, parseMinorInput("12,5"))
        assertEquals(50, parseMinorInput("0,5"))
    }

    @Test
    fun trimsWhitespace() {
        assertEquals(10600, parseMinorInput("  106,00 "))
        assertEquals(10600, parseMinorInput("1 06,00"))
    }

    /**
     * ANLASILMAYAN GIRDI NULL, SIFIR DEGIL.
     *
     * Sifir saymak yazim hatasini bedava bir satira cevirirdi ve aritmetik
     * kapisi bunu "toplam tutmuyor" diye rapor edip kullaniciyi kendi
     * hatasini aramaya gonderirdi.
     */
    @Test
    fun rejectsGarbage() {
        assertNull(parseMinorInput(""))
        assertNull(parseMinorInput("   "))
        assertNull(parseMinorInput("abc"))
        assertNull(parseMinorInput("12,34,56"))
        // Iki haneden fazla ondalik: tutar degil, yazim hatasi.
        assertNull(parseMinorInput("12,345"))
    }

    @Test
    fun handlesNegative() {
        assertEquals(-500, parseMinorInput("-5,00"))
    }

    // --- Adet rozeti --------------------------------------------------------

    /** Adet 1 ise rozet CIZILMEZ: her satira ayni bilgiyi eklemek gurultu. */
    @Test
    fun singleUnitHasNoBadge() {
        assertNull(quantityBadge(1.0, "adet"))
        assertNull(quantityBadge(1.0, "ad"))
    }

    @Test
    fun countedItemsUseMultiplier() {
        assertEquals("2x", quantityBadge(2.0, "adet"))
        assertEquals("3x", quantityBadge(3.0, "ad"))
    }

    /** Tartili urunde birim GORUNMELI: "0,5" tek basina anlamsiz. */
    @Test
    fun weighedItemsShowUnit() {
        assertEquals("0,5 kg", quantityBadge(0.5, "kg"))
        assertEquals("0,182 kg", quantityBadge(0.182, "kg"))
        assertEquals("1 L", quantityBadge(1.0, "L"))
    }

    // --- Gecmis birlestirmesi ----------------------------------------------
    //
    // FIS TESTLERI E8'DE OLDU (gruplama, parca tespiti, durum ikonu): ikisi de
    // kaynaksiz kaldi. Gezi DUZEYINDEKI iki davranis kaldi ve degerini
    // koruyor - `combineTrips` hala kapanis zamani ile kalem sayisini
    // eslestiriyor.

    private fun trip(id: String, closedAt: Long?) = Trip(
        id = id,
        householdId = "h",
        startedAt = 1_000,
        completedAt = closedAt,
        createdAt = 1_000,
    )

    @Test
    fun tripAppearsWithItsItemCount() {
        val out = combineTrips(
            trips = listOf(trip("t1", 5_000)),
            lineCounts = mapOf("t1" to 18),
        )
        assertEquals(1, out.size)
        assertEquals(5_000, out[0].closedAt)
        assertEquals(18, out[0].itemCount)
    }

    /** Kalem sayisi bilinmiyorsa 0 - satir yine cizilir, gezi kaybolmaz. */
    @Test
    fun tripWithoutItemCountStillAppears() {
        val out = combineTrips(trips = listOf(trip("t1", 5_000)))
        assertEquals(1, out.size)
        assertEquals(0, out[0].itemCount)
    }

    /** completedAt bos gelirse startedAt'e dusuluyor - sira anahtari kaybolmasin. */
    @Test
    fun fallsBackToStartedAtWhenCompletedAtMissing() {
        val out = combineTrips(trips = listOf(trip("t1", null)))
        assertEquals(1_000, out[0].closedAt)
    }
}
