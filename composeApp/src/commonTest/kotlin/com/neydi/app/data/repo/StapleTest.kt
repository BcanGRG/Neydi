package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.bootstrap
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * "Her zamankiler" yazma yolu (F6.8).
 *
 * NEDEN BU ADIM VARDI: `isStaple` **bes yerde okunuyordu, sifir yerde
 * yaziliyordu**. Yani %70 opaklik dali ve raptiye calisan uygulamada
 * erisilemezdi; Ekran 3'un "Her zamankiler" bolumu daima bos kalirdi ve
 * "bos ise ekran acilmaz" kuraliyla birlesince Ekran 3 **hic acilamazdi**.
 */
class StapleTest {

    private val home = DEFAULT_HOUSEHOLD_ID
    private val me = "m1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            productDao = db.productDao(),
            clock = { 1_000L },
            newId = { "id-${++n}" },
        )
    }

    /** Kategoriler tohumlanmali: `observeList` kategori tablosuyla JOIN yapiyor. */
    private suspend fun prepare(db: NeydiDatabase) {
        db.bootstrap(newId = { "seed" }, clock = { 0L })
    }

    private suspend fun urun(r: ListRepository, name: String) =
        r.findOrCreateProduct(home, name, "temel-gida", "adet")

    // --- Yazma yolu ---------------------------------------------------------

    @Test
    fun stapleFlagIsWrittenAndCleared() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = urun(r, "Ekmek")
        assertFalse(bread.isStaple, "yeni urun sabit olmamali")

        r.setStaple(bread.id, true)
        assertTrue(db.productDao().byId(bread.id)!!.isStaple)

        r.setStaple(bread.id, false)
        assertFalse(db.productDao().byId(bread.id)!!.isStaple)
    }

    /** `updatedAt` de yaziliyor - LWW'nin karsilastiracagi damga (v3 kolonu). */
    @Test
    fun stapleWriteStampsUpdatedAt() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = urun(r, "Ekmek")
        assertEquals(null, db.productDao().byId(bread.id)!!.updatedAt)

        r.setStaple(bread.id, true)

        assertEquals(1_000L, db.productDao().byId(bread.id)!!.updatedAt)
    }

    @Test
    fun observeStaplesReturnsOnlyMarkedProducts() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = urun(r, "Ekmek")
        urun(r, "Domates")
        r.setStaple(bread.id, true)

        val staples = db.productDao().observeStaples(home).first()

        assertEquals(listOf("Ekmek"), staples.map { it.name })
    }

    // --- Yeni geziye otomatik ekleme ---------------------------------------

    /**
     * Tasarim bunu ozet kartinda kullaniciya aciktan soyluyor:
     * *"Bir sonraki alisveriste her zamankiler yeniden eklenecek."*
     */
    @Test
    fun staplesAreSeededIntoANewTrip() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = urun(r, "Ekmek")
        val milk = urun(r, "Süt")
        urun(r, "Havyar")
        r.setStaple(bread.id, true)
        r.setStaple(milk.id, true)

        val trip = r.openOrGetActiveTrip(home, me)

        val rows = db.tripLineDao().observeList(trip.id, 0L).first()
        assertEquals(setOf("Ekmek", "Süt"), rows.map { it.name }.toSet())
    }

    /**
     * MEVCUT geziye tekrar EKLEMEZ.
     *
     * `openOrGetActiveTrip` her eklemede cagriliyor; her cagrida tohumlamak
     * ayni sabiti listeye tekrar tekrar yazmayi denerdi. UNIQUE kisiti
     * cokmeyi engelliyor ama adet artardi, yani kullanici bir kere dokunmadigi
     * halde "3 ekmek" gorurdu.
     */
    @Test
    fun secondCallDoesNotSeedAgain() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = urun(r, "Ekmek")
        r.setStaple(bread.id, true)

        val first = r.openOrGetActiveTrip(home, me)
        val second = r.openOrGetActiveTrip(home, me)

        assertEquals(first.id, second.id)
        val rows = db.tripLineDao().observeList(first.id, 0L).first()
        assertEquals(1, rows.size)
        assertEquals(1.0, rows.single().count, "adet artmis - tohumlama iki kez kosmus")
    }

    /** Gezi kapandiktan sonra acilan YENI geziye sabitler yeniden giriyor. */
    @Test
    fun staplesReturnInTheNextTrip() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = urun(r, "Ekmek")
        r.setStaple(bread.id, true)

        val first = r.openOrGetActiveTrip(home, me)
        r.closeTrip(first.id, memberId = me)
        val next = r.openOrGetActiveTrip(home, me)

        assertTrue(next.id != first.id, "yeni gezi acilmadi")
        assertEquals(listOf("Ekmek"), db.tripLineDao().observeList(next.id, 0L).first().map { it.name })
    }

    /**
     * Sinir tasarimdan: en fazla 12 satir.
     *
     * Ustu, listeyi acan kullaniciya kendi yazmadigi 20 satir gostermek olurdu
     * ve "gerekmeyeni sil" isi listeyi kurmaktan pahali hale gelirdi.
     */
    @Test
    fun seedingIsCappedAtTwelve() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        repeat(15) { i ->
            r.setStaple(urun(r, "Sabit $i").id, true)
        }

        val trip = r.openOrGetActiveTrip(home, me)

        assertEquals(STAPLE_LIMIT, db.tripLineDao().observeList(trip.id, 0L).first().size)
    }

    /** Sabit olmayan urun tohumlanmiyor - aksi halde katalog listeye bosalirdi. */
    @Test
    fun nonStaplesAreNotSeeded() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        urun(r, "Havyar")

        val trip = r.openOrGetActiveTrip(home, me)

        assertEquals(emptyList(), db.tripLineDao().observeList(trip.id, 0L).first().map { it.name })
    }
}
