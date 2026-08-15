package com.neydi.app.data.receipt

import com.neydi.app.data.db.Receipt
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Fiziksel fis gruplamasi (F4.13 duzeltmesi).
 *
 * Kurgular CIHAZDAKI GERCEK veritabanindan geliyor: kullanicinin bir gezisinde
 * AKYURT'un iki parcasi, bir baskasinda BIM ve File Market'in AYRI fisleri
 * vardi. Ikisini ayni kuralla dogru ayirmak bu dosyanin isi.
 */
class ReceiptGroupingTest {

    private fun receipt(id: String, store: String?, at: Long) = Receipt(
        id = id, householdId = "h1", tripId = "t1",
        imagePath = "/tmp/$id.jpg", capturedAt = at,
        storeNameRaw = store, createdAt = at,
    )

    /**
     * UZUN FISIN PARCALARI TEK GRUP.
     *
     * Kunye yalnizca fisin BASINDA basili, o yuzden ilk parca magaza adini
     * tasiyor, sonrakiler tasimiyor. Cihazdaki AKYURT gezisinin tam hali.
     */
    @Test
    fun partsOfOneLongReceiptStayTogether() {
        val groups = physicalReceipts(
            listOf(
                receipt("p1", "AKYURT SÜPERMARKET", 100),
                receipt("p2", null, 200),
                receipt("p3", null, 300),
            ),
        )

        assertEquals(1, groups.size)
        assertEquals(listOf("p1", "p2", "p3"), groups.single().map { it.id })
    }

    /**
     * IKI AYRI MAGAZA FISI AYRI GRUPLAR - duzeltmenin ilk halinin hatasi buydu.
     *
     * Gezi kapsaminda toplamak, File Market'in DOGRU okunmus fisini BIM'in
     * ayristirma hatasiyla amber'a ceviriyordu. Cihazda olculdu: BIM satirlari
     * 227,89 / basili 225,50 (fark 2,39 = KDV satiri urun sanilmis), File
     * Market 484,58 / 484,58 tam tutuyor.
     *
     * Test isiriyor: gruplama magaza adini gozardi ederse tek grup doner.
     */
    @Test
    fun separateStoresInOneTripAreSeparateReceipts() {
        val groups = physicalReceipts(
            listOf(
                receipt("bim", "BIN BIRLESIK MAGAZALAR A.S.", 100),
                receipt("file", "FiLE MARKET MAGAZACILIK ANONIM SIRKETI", 200),
            ),
        )

        assertEquals(2, groups.size)
        assertEquals(listOf("bim"), groups[0].map { it.id })
        assertEquals(listOf("file"), groups[1].map { it.id })
    }

    /** Ayri magazalarin kendi parcalari da kendi gruplarinda kaliyor. */
    @Test
    fun eachStoreKeepsItsOwnParts() {
        val groups = physicalReceipts(
            listOf(
                receipt("bim1", "BIM BIRLESIK MAGAZALAR A.S.", 100),
                receipt("bim2", null, 200),
                receipt("file1", "FiLE MARKET MAGAZACILIK", 300),
                receipt("file2", null, 400),
            ),
        )

        assertEquals(2, groups.size)
        assertEquals(listOf("bim1", "bim2"), groups[0].map { it.id })
        assertEquals(listOf("file1", "file2"), groups[1].map { it.id })
    }

    /**
     * SUBE DEGIL ZINCIR: ayni zincirin iki subesi tek fis sayilir.
     *
     * `chainKey` ilk anlamli kelimeyi aliyor (bkz. KDoc'u) - alias ogrenmesi
     * de ayni anahtardan geciyor, yani burada baska bir kural kullanmak iki
     * ayri "ayni magaza" tanimi uretirdi.
     */
    @Test
    fun sameChainDifferentBranchIsOneReceipt() {
        val groups = physicalReceipts(
            listOf(
                receipt("a", "BIM BADEMLIK SUBESI", 100),
                receipt("b", "BIM BIRLESIK MAGAZALAR A.S.", 200),
            ),
        )

        assertEquals(1, groups.size)
    }

    /** Cekim SIRASI esas, ekleme sirasi degil - parcalar birbirini izliyor. */
    @Test
    fun ordersByCaptureTime() {
        val groups = physicalReceipts(
            listOf(
                receipt("later", null, 300),
                receipt("earlier", "AKYURT SÜPERMARKET", 100),
            ),
        )

        assertEquals(listOf("earlier", "later"), groups.single().map { it.id })
    }

    /** Magaza adi HIC okunamamis fisler tek grup - ayirt edecek veri yok. */
    @Test
    fun unreadableStoresGroupTogether() {
        val groups = physicalReceipts(listOf(receipt("a", null, 100), receipt("b", null, 200)))

        assertEquals(1, groups.size)
    }

    @Test
    fun targetLookupReturnsItsOwnGroup() {
        val receipts = listOf(
            receipt("bim", "BIM BIRLESIK MAGAZALAR", 100),
            receipt("file", "FiLE MARKET MAGAZACILIK", 200),
            receipt("file-part", null, 300),
        )

        assertEquals(listOf("bim"), samePhysicalReceipt(receipts, "bim").map { it.id })
        assertEquals(
            listOf("file", "file-part"),
            samePhysicalReceipt(receipts, "file-part").map { it.id },
        )
    }

    /** Bilinmeyen id bos liste - cagiran taraf kendi toplamina dusuyor. */
    @Test
    fun unknownIdYieldsNothing() {
        assertEquals(emptyList(), samePhysicalReceipt(listOf(receipt("a", null, 100)), "yok"))
    }
}
