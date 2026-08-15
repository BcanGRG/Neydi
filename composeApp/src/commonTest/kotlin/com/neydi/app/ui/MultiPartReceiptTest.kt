package com.neydi.app.ui

import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptLine
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.db.Trip
import com.neydi.app.ui.history.combineTrips
import com.neydi.app.ui.receipt.sectionMeta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Cok parcali fis tek akis (tasarim karari 4).
 *
 * PARCA HATA HALI DEGIL, NORMAL HAL - testler de bunu olcuyor: parca
 * numaralanıyor mu, iki AYRI magaza fisi yanlislikla parca sayiliyor mu.
 */
class MultiPartReceiptTest {

    private fun trip(id: String) = Trip(
        id = id, householdId = "h1", startedAt = 100,
        completedAt = 500, totalMinor = 64250, createdAt = 100,
    )

    private fun receipt(id: String, at: Long, store: String?) = Receipt(
        id = id, householdId = "h1", tripId = "t1",
        imagePath = "/tmp/$id.jpg", capturedAt = at,
        storeNameRaw = store, status = ReceiptStatus.MISMATCHED, createdAt = at,
    )

    private fun line(id: String, review: Boolean) = ReceiptLine(
        id = id, householdId = "h1", receiptId = "r1",
        rawText = "SUT 1L *42.00", rawTextNormalized = "sut 1l",
        quantity = 1.0, unit = "adet", unitPriceMinor = 4200, lineTotalMinor = 4200,
        needsReview = review, createdAt = 0,
    )

    // --- Bolum basliginin meta'si -------------------------------------------

    /**
     * ONAY BEKLEYEN SATIR VARSA ONU SOYLUYOR. Tasarimin ornegi: birinci parca
     * "18 satır", ikinci parca "16 satır · 1 satır kontrol bekliyor".
     */
    @Test
    fun sectionMetaNamesWaitingRows() {
        assertEquals("2 satır", sectionMeta(listOf(line("1", false), line("2", false))))
        assertEquals(
            "2 satır · 1 satır kontrol bekliyor",
            sectionMeta(listOf(line("1", false), line("2", true))),
        )
    }

    /** Bos parca da bir sey soyluyor: "0 satır" okunamayan bir cekimin hali. */
    @Test
    fun sectionMetaHandlesEmptyPart() {
        assertEquals("0 satır", sectionMeta(emptyList()))
    }

    // --- Gecmis'te parca numaralari -----------------------------------------

    /**
     * AYNI ZINCIRIN ARDISIK CEKIMLERI NUMARALANIYOR.
     *
     * Ikinci parcanin kunyesi okunamiyor - uzun fiste beklenen hal, cunku
     * magaza adi yalnizca ilk karede basili.
     */
    @Test
    fun partsAreNumberedInCaptureOrder() {
        val result = combineTrips(
            trips = listOf(trip("t1")),
            receipts = listOf(
                receipt("r2", at = 300, store = null),
                receipt("r1", at = 200, store = "MIGROS TICARET A.S."),
            ),
        )

        val parts = result.single().receipts.associate { it.id to it.partIndex }
        assertEquals(1, parts["r1"])
        assertEquals(2, parts["r2"])
        // LISTE SIRASI DA CEKIM SIRASI. Cihazda "Parça 2 / Parça 1" diye ters
        // listelendi: numarali bir liste ters siralanınca numaralar okunmaz
        // hale geliyor. Girdi bilerek ters veriliyor.
        assertEquals(listOf(1, 2), result.single().receipts.map { it.partIndex })
    }

    /**
     * IKI AYRI MAGAZA FISI PARCA DEGIL.
     *
     * Cihazda tam olarak bu vardi: bir gezide BIM ve File Market fisi. Onlari
     * "Parça 1 / Parça 2" diye numaralamak, iki ayri alisverisi tek fisin
     * bolumleri gibi gostermek olurdu.
     */
    @Test
    fun differentStoresAreNotParts() {
        val result = combineTrips(
            trips = listOf(trip("t1")),
            receipts = listOf(
                receipt("r1", at = 200, store = "BIM BIRLESIK MAGAZALAR A.S."),
                receipt("r2", at = 300, store = "FiLE MARKET MAGAZACILIK A.S."),
            ),
        )

        result.single().receipts.forEach { assertNull(it.partIndex) }
    }

    /** Tek parcali fiste "Parça 1" diye bir sey YOK. */
    @Test
    fun singleReceiptHasNoPartIndex() {
        val result = combineTrips(
            trips = listOf(trip("t1")),
            receipts = listOf(receipt("r1", at = 200, store = "BIM BIRLESIK MAGAZALAR A.S.")),
        )

        assertNull(result.single().receipts.single().partIndex)
    }
}
