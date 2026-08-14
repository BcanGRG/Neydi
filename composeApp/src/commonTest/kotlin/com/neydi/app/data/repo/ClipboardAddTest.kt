package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.matchKey
import com.neydi.app.data.parseQuantity
import com.neydi.app.data.clipboardLines
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Panodan toplu eklemenin veritabanina kadar olan yolu.
 *
 * ViewModel'in `addFromClipboard`si tam olarak bunu yapiyor; buradaki tek fark
 * `LocalClipboardManager` okumasinin disarida kalmasi - onu cihazda adb ile
 * set etmenin guvenilir yolu yok (`cmd clipboard` bu cihazda mevcut degil),
 * o yuzden pano OKUMASI dogrulanmamis durumda.
 */
class ClipboardAddTest {

    private val ev = "h1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            productDao = db.productDao(),
            saat = { 1_000L },
            yeniId = { "id-${++n}" },
        )
    }

    /** ViewModel.panodanEkle ile ayni sira: ayristir -> her satiri ekle. */
    private suspend fun paste(r: ListRepository, tripId: String, metin: String) {
        clipboardLines(metin).forEach { satir ->
            val m = parseQuantity(satir)
            if (m.ad.isBlank()) return@forEach
            val urun = r.findOrCreateProduct(ev, m.ad, "temel-gida", m.birim ?: "adet")
            r.add(ev, tripId, urun, memberId = "m1", adet = m.adet)
        }
    }

    @Test
    fun whatsappListIsAdded() = runTest {
        val db = db()
        db.householdDao().upsert(Household(ev, "Bizim ev", 0))
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(ev)

        paste(
            r, trip.id,
            """
            - ekmek
            - 2 kg elma
            • süt
            1. deterjan
            """.trimIndent(),
        )

        val satirlar = r.satirlar(ev).first()
        assertEquals(4, satirlar.size)
        // Miktar panodan da ayristiriliyor.
        val elma = satirlar.first { it.unit == "kg" }
        assertEquals(2.0, elma.quantity)
    }

    /**
     * AYNI URUN IKI KEZ GECEN PANO. Kopyalanan listelerde sik olur ve
     * UNIQUE(tripId, productId) ikinci satiri reddeder - repository adedi
     * artirarak bunu dogru cozmeli, hata vermemeli.
     */
    @Test
    fun repeatedLineIncrementsQuantity() = runTest {
        val db = db()
        db.householdDao().upsert(Household(ev, "Bizim ev", 0))
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(ev)

        paste(r, trip.id, "ekmek\nsüt\nekmek")

        val satirlar = r.satirlar(ev).first()
        assertEquals(2, satirlar.size, "tekrar eden satir ikinci kez eklendi")
        val ekmek = satirlar.first { it.productId == r.findOrCreateProduct(ev, "ekmek", "temel-gida", "adet").id }
        assertEquals(2.0, ekmek.quantity)
    }

    /** Buyuk/kucuk harf farki ayri urun uretmemeli - matchKey ayni. */
    @Test
    fun caseDifferenceIsOneProduct() = runTest {
        val db = db()
        db.householdDao().upsert(Household(ev, "Bizim ev", 0))
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(ev)

        paste(r, trip.id, "Ekmek\nEKMEK\nekmek")

        assertEquals(1, r.satirlar(ev).first().size)
        assertEquals("ekmek", matchKey("EKMEK"))
    }
}
