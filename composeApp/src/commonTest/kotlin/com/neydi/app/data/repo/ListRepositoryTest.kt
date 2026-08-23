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

class ListRepositoryTest {

    private val home = "h1"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    /** Saat ve id disaridan: test deterministik olsun diye (repository saf). */
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

    private suspend fun prepare(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = home, name = "Bizim ev", createdAt = 0))
    }

    /**
     * "Ayni anda tek aktif alisveris" kurali. Sema bunu ZORLAMIYOR - kismi
     * index gerekiyor ve Room yazamiyor (F2.3) - o yuzden kuralin tek
     * uygulanma yeri burasi ve testi de burada olmali.
     */
    @Test
    fun secondCallDoesNotOpenNewTrip() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)

        val first = r.openOrGetActiveTrip(home, "m1")
        val second = r.openOrGetActiveTrip(home, "m1")

        assertEquals(first.id, second.id, "ikinci cagri yeni bir alisveris acti")
    }

    @Test
    fun newTripOpensAfterFinishing() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)

        val first = r.openOrGetActiveTrip(home, "m1")
        r.closeTrip(first.id, memberId = "m1")
        val fresh = r.openOrGetActiveTrip(home, "m1")

        assertTrue(fresh.id != first.id, "bitmis alisveris hala aktif goruluyor")
    }

    /**
     * IKINCI EKLEME HICBIR SEY DEGISTIRMIYOR (karar 109).
     *
     * ## Bu iddia tam tersine cevrildi
     *
     * Eskiden *"adedi artirmali"* yaziyordu ve savunmasi makuldu: *"iki kisi
     * ayni ekmegi istedi, iki ekmek degil."* Ama o cumle miktarin baska bir evi
     * OLMADIGI dunyada yazilmisti - artirmak, adedi degistirmenin tek yoluydu.
     *
     * Uc ekleme yolu uc turlu davraniyordu: tek tek artiriyor, toplu atliyor
     * (karar 91), sheet'te isaretli hucre pasif oldugu icin hicbir sey
     * olmuyordu. Karar 109 ucunu birlestirdi.
     *
     * HATA HALA VERILMIYOR: kisita carpip *"ekleyemedim"* demek yine yanlis
     * cevap - degisen tek sey, dogru cevabin artik "sessizce ayni kalmak"
     * olmasi.
     */
    @Test
    fun readdingSameProductChangesNothing() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val bread = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")

        r.add(home, trip.id, bread, memberId = "m1")
        r.add(home, trip.id, bread, memberId = "m2")

        val rows = r.rows(home).first()
        assertEquals(1, rows.size, "ikinci ekleme yeni satir acti")
        assertEquals(1.0, rows.single().quantity, "ikinci ekleme miktari degistirdi")
        // Ilk ekleyen korunuyor: "kim ekledi" bilgisi ezilmemeli.
        assertEquals("m1", rows.single().addedByMemberId)
    }

    /**
     * IKINCI EKLEME SESSIZ AMA GORUNMEZ DEGIL: satir yine donuyor.
     *
     * Karar 89'un "az once eklendi" yikamasi bu satiri bulabilmek zorunda -
     * kullanici bir sey ekledigini dusunuyor ve ekranin ona cevap vermesi
     * gerekiyor. `wasNew` ise sayaclara *"bu bir ekleme degildi"* diyor:
     * ikisini ayirmadan ya yikama kaybolur ya sayac yalan soyler.
     */
    @Test
    fun aSecondAddStillReturnsTheRowSoTheWashCanRun() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val bread = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")

        val first = r.add(home, trip.id, bread, memberId = "m1")
        val second = r.add(home, trip.id, bread, memberId = "m1")

        assertTrue(first.wasNew, "ilk ekleme yeni satir acmaliydi")
        assertTrue(!second.wasNew, "ikinci ekleme yeni satir saymamali")
        assertEquals(first.line.id, second.line.id, "yikamanin bakacagi satir ayni olmali")
    }

    /**
     * SESSIZ CANLI HATA: otomatik eklenen sabit, elle eklenince ikiye cikiyordu.
     *
     * `seedStaples` her gezide sabitleri kendiliginden ekliyor. Kullanici
     * "ekmek" yazip ekledigunde eski kural adedi 2 yapiyordu - kimse istemeden,
     * ve kullanici sabitin zaten orada oldugunu bilmeden. Bu hatanin testi
     * yoktu; kural degistigi icin artik yazilabiliyor.
     */
    @Test
    fun anAutoSeededStapleIsNotDoubledByATypedAdd() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")
        r.setStaple(bread.id, true)
        // Sabit, gezi acilirken kendiliginden giriyor.
        val trip = r.openOrGetActiveTrip(home, "m1")

        r.add(home, trip.id, bread, memberId = "m1")

        assertEquals(1.0, r.rows(home).first().single().quantity)
    }

    /** matchKey uzerinden bakiyor: "Ekmek" ile "EKMEK" ayri urun olmamali. */
    @Test
    fun caseDoesNotSplitProducts() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)

        val a = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")
        val b = r.findOrCreateProduct(home, "EKMEK", "firin-ekmek", "adet")

        assertEquals(a.id, b.id)
    }

    @Test
    fun checkAndUncheckFlowThrough() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val milk = r.findOrCreateProduct(home, "Süt", "sut-kahvalti", "L")
        val row = r.add(home, trip.id, milk, memberId = "m1").line

        r.toggleChecked(row.id, true)
        val checked = r.rows(home).first().single()
        assertTrue(checked.checked)
        // checkedAt saklanmali: reyonda mi evde mi isaretlendi sorusu sonra lazim.
        assertNotNull(checked.checkedAt)

        r.remove(row.id)
        assertTrue(r.rows(home).first().isEmpty(), "cikarilan satir hala listede")
    }

    /** Aktif alisveris yoksa satirlar BOS liste - hata degil. */
    @Test
    fun rowsEmptyWithoutActiveTrip() = runTest {
        val db = db(); prepare(db)
        assertTrue(repo(db).rows(home).first().isEmpty())
    }

    /** Silinen satir tombstone; sorgular onu getirmemeli. */
    @Test
    fun deletedRowDoesNotResurrect() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        val product = r.findOrCreateProduct(home, "Yumurta", "sut-kahvalti", "adet")
        val row = r.add(home, trip.id, product, memberId = "m1").line
        r.remove(row.id)

        // Ayni urun tekrar eklenebilmeli - tombstone yeni eklemeyi engellememeli.
        val again = r.add(home, trip.id, product, memberId = "m1").line
        assertTrue(r.rows(home).first().size == 1)
        // Mezardan cikan satir adedi SIFIRDAN baslar, eski adedi tasimaz.
        assertEquals(1.0, again.quantity)
    }

    // --- "Gecen sefer aldiklarini ekle" (Ekran 1 bos durumu) -----------------

    /** Onceki gezide ALINAN urunler bugunun listesine geciyor. */
    @Test
    fun addsTakenItemsFromLastTrip() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")
        val milk = r.findOrCreateProduct(home, "Süt", "sut-kahvalti", "L")
        val past = r.openOrGetActiveTrip(home, "m1")
        val breadLine = r.add(home, past.id, bread, memberId = "m1").line
        val milkLine = r.add(home, past.id, milk, memberId = "m1").line
        r.toggleChecked(breadLine.id, true)
        r.toggleChecked(milkLine.id, true)
        r.closeTrip(past.id, "m1")

        val added = r.addFromLastTrip(home, "m1")

        assertEquals(2, added)
        assertEquals(setOf("Ekmek", "Süt"), r.rows(home).first().map { line ->
            db.productDao().byId(line.productId)?.name
        }.toSet())
    }

    /**
     * ALINMAYAN URUN GERI GELMIYOR.
     *
     * Uc-sonuc secici (F4.12) "gerekmedi" ve "unuttum" satirlarini isaretsiz
     * birakiyor. Kullanicinin acikca "gerekmedi" dedigi bir urunu bir sonraki
     * listeye geri koymak, verdigi karari yok saymak olurdu.
     *
     * Test isiriyor: sorgudan `checked = 1` kosulu kaldirilirsa burasi kirilir.
     */
    @Test
    fun skipsItemsThatWereNotTaken() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")
        val milk = r.findOrCreateProduct(home, "Süt", "sut-kahvalti", "L")
        val past = r.openOrGetActiveTrip(home, "m1")
        val breadLine = r.add(home, past.id, bread, memberId = "m1").line
        r.add(home, past.id, milk, memberId = "m1")
        r.toggleChecked(breadLine.id, true)
        r.closeTrip(past.id, "m1")

        val added = r.addFromLastTrip(home, "m1")

        assertEquals(1, added)
        assertEquals("Ekmek", db.productDao().byId(r.rows(home).first().single().productId)?.name)
    }

    /**
     * ZATEN LISTEDE OLAN IKI KEZ EKLENMIYOR.
     *
     * `add` mevcut satirin adedini artiriyor; filtre olmasaydi butona iki kez
     * dokunan kullanici her urunden iki tane ister hale gelirdi.
     */
    @Test
    fun doesNotDuplicateWhatIsAlreadyOnTheList() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val bread = r.findOrCreateProduct(home, "Ekmek", "firin-ekmek", "adet")
        val past = r.openOrGetActiveTrip(home, "m1")
        val line = r.add(home, past.id, bread, memberId = "m1").line
        r.toggleChecked(line.id, true)
        r.closeTrip(past.id, "m1")

        assertEquals(1, r.addFromLastTrip(home, "m1"))
        assertEquals(0, r.addFromLastTrip(home, "m1"))

        val rows = r.rows(home).first()
        assertEquals(1, rows.size)
        assertEquals(1.0, rows.single().quantity)
    }

    /** Hic kapanmis gezi yoksa sessizce sifir - hata degil, yapacak is yok. */
    @Test
    fun addsNothingWhenNoClosedTripExists() = runTest {
        val db = db(); prepare(db)

        assertEquals(0, repo(db).addFromLastTrip(home, "m1"))
    }
}
