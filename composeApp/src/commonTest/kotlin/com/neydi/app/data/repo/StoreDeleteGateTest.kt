package com.neydi.app.data.repo

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.db.NeydiDatabaseConstructor
import com.neydi.app.data.db.Store
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Silme kapisinin ikinci sarti ve beyanin yapiskanligi (kararlar 122-123).
 *
 * ## Neden bu iki sorgunun gercek bir veritabaninda testi var
 *
 * `docs/38` S2'nin tarif ettigi hasar SESSIZDI ve hicbir sey patlatmiyordu:
 * etiket ekraninda bir markete uzun dokunus -> `softDelete` -> liste
 * sorgusundaki `deletedAt IS NULL` yuzunden ad `null` doner -> satirdaki
 * sapma isareti kaybolur, basliktaki sayac duser, hedef silinmisse beyan hic
 * cizilmez. Kullanicinin yazdigi plan, ilgisiz bir ekrandaki tek dokunusla
 * **haber verilmeden** imha oluyordu.
 *
 * Kapinin dogru sayiyi vermesi bu yuzden bir SQL detayi degil, kararin
 * kendisi - ve ikinci bacagi (hedefi izleyen satirlar) yalnizca bir JOIN ile
 * gorunuyor, yani gozden kacmaya en musait yer orasi.
 */
class StoreDeleteGateTest {

    private val home = "h1"
    private val bim = "s-bim"
    private val a101 = "s-a101"

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private fun repo(db: NeydiDatabase, clock: () -> Long = { 1_000L }): ListRepository {
        var n = 0
        return ListRepository(
            tripDao = db.tripDao(),
            tripLineDao = db.tripLineDao(),
            productDao = db.productDao(),
            clock = clock,
            newId = { "id-${++n}" },
        )
    }

    private suspend fun prepare(db: NeydiDatabase) {
        db.householdDao().upsert(Household(id = home, name = "Bizim ev", createdAt = 0))
        db.storeDao().insert(Store(id = bim, householdId = home, name = "BİM", chain = "BİM", createdAt = 0))
        db.storeDao().insert(Store(id = a101, householdId = home, name = "A101", chain = "A101", createdAt = 0))
    }

    /**
     * BEYANSIZ MARKET SILINEBILIR - karar 59 yerinde duruyor.
     *
     * Karar 122 bir kapi EKLEDI, var olani sertlestirmedi: hicbir seye
     * baglanmamis bir zincir (yazim hatasi, yanlis dokunus) hala tek uzun
     * dokunusla gidiyor. *"Tek dokunusla yaratilan, hicbir dokunusla yok
     * edilemeyen varlik olmaz."*
     */
    @Test
    fun aChainNothingPointsAtIsStillDeletable() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        r.openOrGetActiveTrip(home, "m1")

        assertEquals(0, r.linesHeadedTo(home, a101))
    }

    /**
     * SATIRIN KENDI ISTISNASI SAYILIYOR - kapinin birinci bacagi.
     *
     * *"2-3 tanesini A101'den alacagim"* diyen kullanici A101'e hic etiket
     * cekmemis olabilir; karar 117 gozlemsiz zincire gitmeyi mesru kildi.
     * Gozleme bakan eski kapi bu satirlari GORMUYORDU.
     */
    @Test
    fun aLineWithItsOwnExceptionCountsTowardThatChain() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        r.setTripStore(home, "m1", bim)
        val milk = r.findOrCreateProduct(home, "Süt", "sut", "adet")
        val line = r.add(home, trip.id, milk, memberId = "m1").line
        r.setLineStore(rowId = line.id, storeId = a101, targetStoreId = bim)

        assertEquals(1, r.linesHeadedTo(home, a101), "istisna sayilmadi")
    }

    /**
     * HEDEFI IZLEYEN SATIRLAR DA SAYILIYOR - kapinin IKINCI bacagi.
     *
     * Bu bacak olmadan kapi yarim kalirdi ve hasarin **en buyugu** tam
     * buradan gecerdi: hedefi BIM olan bes satirlik bir liste varken BIM
     * silinebilir olurdu, `deviatesFrom`'un ilk sarti (hedef var) duserdi ve
     * listedeki BUTUN sapma isaretleri ayni anda kaybolurdu.
     *
     * ISIRMA NOKTASI: sorgudaki `OR (l.storeId IS NULL AND t.storeId = ...)`
     * dalini silin - bu test duser, otekiler yesil kalir.
     */
    @Test
    fun linesThatFollowTheTargetCountTowardTheTarget() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        r.setTripStore(home, "m1", bim)
        for (name in listOf("Süt", "Ekmek", "Yumurta")) {
            r.add(home, trip.id, r.findOrCreateProduct(home, name, name.lowercase(), "adet"), memberId = "m1")
        }

        assertEquals(3, r.linesHeadedTo(home, bim), "hedefi izleyen satirlar sayilmadi")
    }

    /**
     * ISTISNA HEDEFTEN DUSUYOR - satir iki markete birden gitmiyor.
     *
     * Ayni satir hem "BIM'e gidiyor" hem "A101'e gidiyor" sayilsaydi cumle
     * satir sayisini yalanlardi ve kullaniciya kaybedecegi seyin boyu yanlis
     * soylenirdi.
     */
    @Test
    fun anExceptionLeavesTheTargetsCount() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        r.setTripStore(home, "m1", bim)
        val milk = r.findOrCreateProduct(home, "Süt", "sut", "adet")
        val bread = r.findOrCreateProduct(home, "Ekmek", "ekmek", "adet")
        val milkLine = r.add(home, trip.id, milk, memberId = "m1").line
        r.add(home, trip.id, bread, memberId = "m1")
        r.setLineStore(rowId = milkLine.id, storeId = a101, targetStoreId = bim)

        assertEquals(1, r.linesHeadedTo(home, bim), "istisna hala hedefte sayiliyor")
        assertEquals(1, r.linesHeadedTo(home, a101))
    }

    /**
     * SILINEN SATIR PLANIN PARCASI DEGIL.
     *
     * Kullanicinin listeden cikardigi bir satir, bir zinciri sonsuza kadar
     * silinemez yapamaz - yoksa kapi zamanla her zinciri kilitlerdi ve karar
     * 59'un jesti kagitta kalirdi.
     */
    @Test
    fun aRemovedLineStopsHoldingItsChainHostage() = runTest {
        val db = db(); prepare(db)
        val r = repo(db)
        val trip = r.openOrGetActiveTrip(home, "m1")
        r.setTripStore(home, "m1", bim)
        val milk = r.findOrCreateProduct(home, "Süt", "sut", "adet")
        val line = r.add(home, trip.id, milk, memberId = "m1").line
        r.setLineStore(rowId = line.id, storeId = a101, targetStoreId = bim)
        assertEquals(1, r.linesHeadedTo(home, a101))

        r.remove(line.id)

        assertEquals(0, r.linesHeadedTo(home, a101), "silinmis satir hala sayiliyor")
    }

    /**
     * YAPISKANLIK BEYANDAN GELIYOR, ETIKETTEN DEGIL (karar 123).
     *
     * Tasarimin uyarisi tek cumleydi: tek degere baglamak, **A101'de cekilen
     * bir etiketin BIM gezisinin hedefini degistirmesi** demekti. Bu sorgu
     * `trip` tablosuna bakiyor - `price_observation`a degil - ve testin
     * korudugu sey o ayrim.
     */
    @Test
    fun theStickyChainComesFromTheLastDeclarationNotTheLastTag() = runTest {
        val db = db(); prepare(db)
        // SAAT ILERLIYOR ve bu testin GEREGI: "en son beyan" bir SIRALAMA
        // iddiasi ve iki gezi ayni damgayi tasirsa siralamanin yonu hicbir
        // sey kanitlamaz. Sabit saatle yazilmis ilk hali, sorgunun yonu
        // tersine cevrildiginde bile yesil kaliyordu.
        var now = 1_000L
        val r = repo(db) { now += 1_000; now }

        assertNull(r.lastDeclaredStoreId(home), "hic beyan yokken bir sey oneriliyor")

        val first = r.openOrGetActiveTrip(home, "m1")
        r.setTripStore(home, "m1", a101)
        assertEquals(a101, r.lastDeclaredStoreId(home))

        // Yeni gezi: hedefi bos, ama gecen seferki beyan hala hatirlaniyor.
        r.closeTrip(first.id, memberId = "m1")
        r.openOrGetActiveTrip(home, "m1")
        assertEquals(a101, r.lastDeclaredStoreId(home), "onceki beyan unutuldu")

        // YENI BEYAN ESKISINI GECERSIZ KILIYOR - siralamanin yonunu isiran
        // iddia bu. Eskisi kazansaydi kullanici her gezide bir onceki
        // markete gitmeyi onerilmis gorurdu.
        r.setTripStore(home, "m1", bim)
        assertEquals(bim, r.lastDeclaredStoreId(home), "eski beyan yenisini yendi")
    }
}
