package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.db.TripStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * F4.2: fis kuyruga alinir, OCR SONRA kosar.
 *
 * Kuralin adi "fotograf asla bloklamaz". Testlerin kanitladigi sey bunun
 * yapisal karsiligi: fis KAPANMIS bir geziye eklenebiliyor ve eklemek geziyi
 * yeniden acmiyor. Yani kapanis fotografi beklemiyor - kullanici kasa
 * kuyrugunda spinner gormuyor.
 */
class ReceiptQueueTest {

    private val home = "h1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            receiptDao = db.receiptDao(),
            productDao = db.productDao(),
            clock = { 1_000L },
            newId = { "id-${++n}" },
        )
    }

    private suspend fun prepare(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = home, name = "Bizim ev", createdAt = 0))
    }

    /** Fis PENDING olarak giriyor: OCR henuz kosmadi ve bu gorunur olmali. */
    @Test
    fun receiptEntersQueueAsPending() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home)

        val receipt = r.enqueueReceipt(home, trip.id, "/veri/fis-1.jpg")

        assertEquals(ReceiptStatus.PENDING, receipt.status)
        assertEquals("/veri/fis-1.jpg", receipt.imagePath)
        assertEquals(listOf(receipt.id), db.receiptDao().pending().map { it.id })
    }

    /**
     * ISIN OZU: fis KAPANMIS geziye eklenebilir ve gezi kapali KALIR.
     *
     * Kapanis fotografi beklemedigi icin gercek akis tam bu: gezi kapanir, ozet
     * karti cikar, kullanici sonra fis ceker. Eklemek geziyi yeniden acsaydi
     * gecmisten kaybolur ve bir sonraki urun yeni gezi yerine ona yazilirdi.
     */
    @Test
    fun receiptAttachesToClosedTripWithoutReopeningIt() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home)
        assertTrue(r.closeTrip(trip.id, memberId = "m1"))

        r.enqueueReceipt(home, trip.id, "/veri/fis-1.jpg")

        val still = assertNotNull(db.tripDao().byId(trip.id))
        assertEquals(TripStatus.CLOSED, still.status, "fis eklemek geziyi yeniden acmamali")
        assertEquals(1, db.receiptDao().observeForTrip(trip.id).first().size)
    }

    /**
     * Ayni geziye IKI fis eklenebilmeli.
     *
     * Uzun fisler iki parca basiliyor ve kullanici ikisini de cekmek zorunda;
     * ikincisi birincisini ezerse alisverisin yarisi kaybolur.
     */
    @Test
    fun twoReceiptsCanBelongToOneTrip() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home)

        r.enqueueReceipt(home, trip.id, "/veri/fis-1.jpg")
        r.enqueueReceipt(home, trip.id, "/veri/fis-2.jpg")

        val hepsi = db.receiptDao().observeForTrip(trip.id).first()
        assertEquals(2, hepsi.size)
        assertEquals(
            setOf("/veri/fis-1.jpg", "/veri/fis-2.jpg"),
            hepsi.map { it.imagePath }.toSet(),
        )
    }
}
