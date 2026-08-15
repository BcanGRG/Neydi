package com.neydi.app.ui

import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptStatus
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
 * Fis ekranlarinin ViewModel'siz saf mantigi.
 *
 * Compose testi degil: burada test edilenler bir ekrana bagli olmayan
 * donusumler ve tam bu yuzden ayri fonksiyonlar - Composable icinde kalsalardi
 * ancak enstrumentasyon testiyle dogrulanabilirlerdi.
 */
class ReceiptUiLogicTest {

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

    private fun trip(id: String, closedAt: Long?, total: Long?) = Trip(
        id = id, householdId = "h1", startedAt = 100,
        completedAt = closedAt, totalMinor = total, createdAt = 100,
    )

    private fun receipt(id: String, tripId: String, status: ReceiptStatus) = Receipt(
        id = id, householdId = "h1", tripId = tripId,
        imagePath = "/tmp/$id.jpg", capturedAt = 200, status = status, createdAt = 200,
    )

    @Test
    fun groupsReceiptsUnderTheirTrip() {
        val result = combineTrips(
            trips = listOf(trip("t1", 500, 22550), trip("t2", 400, null)),
            receipts = listOf(
                receipt("r1", "t1", ReceiptStatus.VERIFIED),
                receipt("r2", "t1", ReceiptStatus.FAILED),
                receipt("r3", "t2", ReceiptStatus.PENDING),
            ),
        )

        assertEquals(listOf(2, 1), result.map { it.receipts.size })
        assertEquals(500, result[0].closedAt)
    }

    /**
     * BASARISIZ FISLER DE LISTEDE.
     *
     * Gecmis, yanlis okunmus bir fise geri donmenin TEK yolu; FAILED satirlari
     * elemek onlari erisilemez yapardi - fotograf diskte durur, kullanici bir
     * daha ulasamaz ve sebebini ogrenemez.
     */
    @Test
    fun failedReceiptsSurvive() {
        val result = combineTrips(
            trips = listOf(trip("t1", 500, null)),
            receipts = listOf(receipt("r1", "t1", ReceiptStatus.FAILED)),
        )

        assertEquals(ReceiptStatus.FAILED, result.single().receipts.single().status)
    }

    /** Fissiz gezi (Mod B) listede GORUNMELI: kapanmis alisveris kaybolmaz. */
    @Test
    fun tripWithoutReceiptsStillAppears() {
        val result = combineTrips(trips = listOf(trip("t1", 500, null)), receipts = emptyList())

        assertEquals(1, result.size)
        assertEquals(emptyList(), result.single().receipts)
        // totalMinor null: fis okunmadi, yani BILMIYORUZ. 0 "bedava" demek olurdu.
        assertNull(result.single().totalMinor)
    }

    /** completedAt yoksa startedAt'e dusuluyor - liste sira anahtarini kaybetmesin. */
    @Test
    fun fallsBackToStartedAtWhenCompletedAtMissing() {
        val result = combineTrips(trips = listOf(trip("t1", null, null)), receipts = emptyList())

        assertEquals(100, result.single().closedAt)
    }

    // --- Parca tespiti (F4.13) ----------------------------------------------

    /**
     * Toplami okunamamis fis, ayni gezide BASKA fisler varken PARCA sayilir -
     * toplam yalnizca son parcada basili, kullanici hata yapmadi. Amber
     * "toplam tutmuyor" giydirmek durust ama yanlis yonlendiriciydi.
     */
    @Test
    fun middlePartIsLabelledAsPart() {
        val result = combineTrips(
            trips = listOf(trip("t1", 500, null)),
            receipts = listOf(
                receipt("parca", "t1", ReceiptStatus.MISMATCHED),
                receipt("son", "t1", ReceiptStatus.VERIFIED).copy(totalMinor = 48458),
            ),
        )

        val rows = result.single().receipts
        assertEquals(2, rows.size)
        assertTrue(rows.single { it.id == "parca" }.isPart)
        assertFalse(rows.single { it.id == "son" }.isPart)
    }

    /** TEK fisli gezide toplami okunamamis fis parca DEGIL - gercek bir sorun. */
    @Test
    fun soloUnreadableTotalIsNotAPart() {
        val result = combineTrips(
            trips = listOf(trip("t1", 500, null)),
            receipts = listOf(receipt("r1", "t1", ReceiptStatus.MISMATCHED)),
        )

        assertFalse(result.single().receipts.single().isPart)
    }

    /** Toplami OKUNMUS ama tutmayan fis parca degil - o gercekten tutmuyor. */
    @Test
    fun genuineMismatchIsNotAPart() {
        val result = combineTrips(
            trips = listOf(trip("t1", 500, null)),
            receipts = listOf(
                receipt("r1", "t1", ReceiptStatus.MISMATCHED).copy(totalMinor = 10000),
                receipt("r2", "t1", ReceiptStatus.VERIFIED).copy(totalMinor = 48458),
            ),
        )

        assertFalse(result.single().receipts.single { it.id == "r1" }.isPart)
    }
}
