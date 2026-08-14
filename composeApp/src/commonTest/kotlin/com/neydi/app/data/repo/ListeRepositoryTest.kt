package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ListeRepositoryTest {

    private val ev = "h1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    /** Saat ve id disaridan: test deterministik olsun diye (repository saf). */
    private fun repo(db: NeydiDatabase): ListeRepository {
        var n = 0
        return ListeRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            productDao = db.productDao(),
            saat = { 1_000L },
            yeniId = { "id-${++n}" },
        )
    }

    private suspend fun hazirla(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = ev, name = "Bizim ev", createdAt = 0))
    }

    /**
     * "Ayni anda tek aktif alisveris" kurali. Sema bunu ZORLAMIYOR - kismi
     * index gerekiyor ve Room yazamiyor (F2.3) - o yuzden kuralin tek
     * uygulanma yeri burasi ve testi de burada olmali.
     */
    @Test
    fun ikinciCagriYeniAlisverisACMAZ() = runTest {
        val db = db(); hazirla(db)
        val r = repo(db)

        val ilk = r.aktifAlisverisiAcVeyaAl(ev)
        val ikinci = r.aktifAlisverisiAcVeyaAl(ev)

        assertEquals(ilk.id, ikinci.id, "ikinci cagri yeni bir alisveris acti")
    }

    @Test
    fun bitirdiktenSonraYeniAlisverisAcilir() = runTest {
        val db = db(); hazirla(db)
        val r = repo(db)

        val ilk = r.aktifAlisverisiAcVeyaAl(ev)
        r.alisverisiBitir(ilk.id)
        val yeni = r.aktifAlisverisiAcVeyaAl(ev)

        assertTrue(yeni.id != ilk.id, "bitmis alisveris hala aktif goruluyor")
    }

    /**
     * ASIL DAVRANIS: es zaten ekmek eklemisse ikinci ekleme HATA VERMEMELI,
     * adedi artirmali. UNIQUE(tripId, productId) ikinci satiri zaten
     * engelliyor; kisita carpip "ekleyemedim" demek yanlis cevap olurdu.
     */
    @Test
    fun ayniUrunuTekrarEklemekAdetArtirir() = runTest {
        val db = db(); hazirla(db)
        val r = repo(db)
        val trip = r.aktifAlisverisiAcVeyaAl(ev)
        val ekmek = r.urunBulVeyaOlustur(ev, "Ekmek", "firin-ekmek", "adet")

        r.ekle(ev, trip.id, ekmek, memberId = "m1")
        r.ekle(ev, trip.id, ekmek, memberId = "m2")

        val satirlar = r.satirlar(ev).first()
        assertEquals(1, satirlar.size, "ikinci ekleme yeni satir acti")
        assertEquals(2.0, satirlar.single().quantity)
        // Ilk ekleyen korunuyor: "kim ekledi" bilgisi ezilmemeli.
        assertEquals("m1", satirlar.single().addedByMemberId)
    }

    /** matchKey uzerinden bakiyor: "Ekmek" ile "EKMEK" ayri urun olmamali. */
    @Test
    fun buyukKucukHarfAyriUrunUretmez() = runTest {
        val db = db(); hazirla(db)
        val r = repo(db)

        val a = r.urunBulVeyaOlustur(ev, "Ekmek", "firin-ekmek", "adet")
        val b = r.urunBulVeyaOlustur(ev, "EKMEK", "firin-ekmek", "adet")

        assertEquals(a.id, b.id)
    }

    @Test
    fun isaretlemeVeCikarmaAkisaYansir() = runTest {
        val db = db(); hazirla(db)
        val r = repo(db)
        val trip = r.aktifAlisverisiAcVeyaAl(ev)
        val sut = r.urunBulVeyaOlustur(ev, "Süt", "sut-kahvalti", "L")
        val satir = r.ekle(ev, trip.id, sut, memberId = "m1")

        r.isaretle(satir.id, true)
        val isaretli = r.satirlar(ev).first().single()
        assertTrue(isaretli.checked)
        // checkedAt saklanmali: reyonda mi evde mi isaretlendi sorusu sonra lazim.
        assertNotNull(isaretli.checkedAt)

        r.cikar(satir.id)
        assertTrue(r.satirlar(ev).first().isEmpty(), "cikarilan satir hala listede")
    }

    /** Aktif alisveris yoksa satirlar BOS liste - hata degil. */
    @Test
    fun aktifAlisverisYokkenSatirlarBos() = runTest {
        val db = db(); hazirla(db)
        assertTrue(repo(db).satirlar(ev).first().isEmpty())
    }

    /** Silinen satir tombstone; sorgular onu getirmemeli. */
    @Test
    fun silinenSatirGeriGelmez() = runTest {
        val db = db(); hazirla(db)
        val r = repo(db)
        val trip = r.aktifAlisverisiAcVeyaAl(ev)
        val urun = r.urunBulVeyaOlustur(ev, "Yumurta", "sut-kahvalti", "adet")
        val satir = r.ekle(ev, trip.id, urun, memberId = "m1")
        r.cikar(satir.id)

        // Ayni urun tekrar eklenebilmeli - tombstone yeni eklemeyi engellememeli.
        val yeni = r.ekle(ev, trip.id, urun, memberId = "m1")
        assertTrue(r.satirlar(ev).first().size == 1)
        // Mezardan cikan satir adedi SIFIRDAN baslar, eski adedi tasimaz.
        assertEquals(1.0, yeni.quantity)
    }
}
