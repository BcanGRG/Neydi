package com.neydi.app.data.repo

import com.neydi.app.data.db.Product
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptDao
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.db.TakeOutcome
import com.neydi.app.data.db.Trip
import com.neydi.app.data.db.TripStatus
import com.neydi.app.data.db.TripDao
import com.neydi.app.data.db.TripLine
import com.neydi.app.data.db.TripLineDao
import com.neydi.app.data.matchKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Liste ekraninin tek veri kapisi. OFFLINE-FIRST: her yazma once yerel
 * veritabanina gider, okuma her zaman yerelden. Senkron (Faz 7) bunun
 * uzerine PendingOp kuyruguyla gelecek; ekran katmani degismeyecek.
 *
 * Zaman ve id URETIMI disaridan veriliyor (saat/uuid). Boylece repository
 * saf kalir ve testte deterministik olur - `Clock.System.now()` cagiran bir
 * repository "gecen sefer ne zaman aldik" mantigini test edilemez yapar.
 */
/** "Her zamankiler" bolumunun tasarimdaki ust siniri. */
const val STAPLE_LIMIT = 12

class ListRepository(
    private val tripDao: TripDao,
    private val tripLineDao: TripLineDao,
    private val receiptDao: ReceiptDao,
    private val productDao: ProductDao,
    private val clock: () -> Long,
    private val newId: () -> String,
) {

    fun activeTrip(householdId: String): Flow<Trip?> = tripDao.observeActive(householdId)

    /**
     * Aktif alisverisin satirlari. Aktif alisveris yoksa BOS liste - hata degil:
     * planlama modunda henuz gezi baslamamis olabilir.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun rows(householdId: String): Flow<List<TripLine>> =
        tripDao.observeActive(householdId).flatMapLatest { trip ->
            if (trip == null) flowOf(emptyList()) else tripLineDao.observeLines(trip.id)
        }

    /**
     * Aktif alisverisi dondurur, yoksa acar.
     *
     * "Ayni anda tek aktif alisveris" kuralinin uygulandigi TEK yer burasi:
     * kisit kismi index gerektirdigi icin semada ifade EDILEMIYOR (F2.3).
     * Yeni gezi acmadan once mutlaka buradan gecilmeli.
     */
    suspend fun openOrGetActiveTrip(householdId: String, memberId: String): Trip {
        tripDao.activeOrNull(householdId)?.let { return it }
        val trip = Trip(
            id = newId(),
            householdId = householdId,
            startedAt = clock(),
            createdAt = clock(),
        )
        tripDao.insert(trip)
        // SABITLER YENI LISTEYE OTOMATIK GIRIYOR (F6.8).
        //
        // Tasarim bunu ozet kartinda kullaniciya aciktan soyluyor:
        // *"Bir sonraki alisveriste her zamankiler yeniden eklenecek."*
        //
        // NEDEN BURADA, ayri bir fonksiyonda DEGIL: gezinin dogdugu TEK yer bu
        // ve KDoc'u da "yeni gezi acmadan once mutlaka buradan gecilmeli" diyor.
        // Tohumlamayi cagiranin sorumluluguna birakmak, bu projede daha once
        // defalarca yasanan "cagirmayi unut" hatasini davet ederdi - ve unutulsa
        // sessizce yalnizca bos liste gorunurdu.
        //
        // `memberId` parametresi tam bu yuzden ZORUNLU ve varsayilansiz: sabit
        // satirlarin da bir ekleyeni olmak zorunda (`addedByMemberId` NOT NULL)
        // ve varsayilan bir deger koymak tohumlamayi yeniden atlanabilir yapardi.
        seedStaples(householdId, trip.id, memberId)
        return trip
    }

    /**
     * Sabitleri yeni geziye ekler. En fazla [STAPLE_LIMIT] satir.
     *
     * Sinir tasarimdan: "Her zamankiler" bolumu en fazla 12 satir. Ustu, bir
     * listeyi acan kullaniciya kendi yazmadigi 20 satir gostermek olurdu ve
     * "gerekmeyeni sil" isi listeyi kurmaktan pahali hale gelirdi.
     */
    private suspend fun seedStaples(householdId: String, tripId: String, memberId: String) {
        productDao.staples(householdId).take(STAPLE_LIMIT).forEach { product ->
            add(householdId = householdId, tripId = tripId, product = product, memberId = memberId)
        }
    }

    /**
     * Alisveris moduna girer ya da cikar. KALICI - ekran durumu degil.
     *
     * Surum 1'de bu yalnizca ViewModel'de bir bayrakti; uygulama kapaninca
     * kayboluyordu ve (Faz 7'de) eslerden biri markete girdiginde otekinin
     * ekrani bunu hic gormeyecekti. Gezinin durumuna yazmak ikisini de cozuyor.
     */
    suspend fun setShoppingMode(tripId: String, enabled: Boolean) {
        tripDao.setStatus(tripId, if (enabled) TripStatus.SHOPPING else TripStatus.PLANNING)
    }

    /**
     * Cekilen fisi KUYRUGA ALIR. OCR burada KOSMUYOR.
     *
     * Bu, F4.2'nin butun kurali: fotograf asla bloklamaz. Kullanici kasa
     * kuyrugunda telefonun basinda spinner beklemez - satir PENDING olarak
     * yaziliyor, gezi zaten kapanmis durumda, ve okuma sonra olur.
     *
     * Kucultme BASARISIZ OLURSA orijinal yol saklaniyor. Fotograf
     * kullanicinin tek kaniti; bicimlendirme takildi diye onu atmak kabul
     * edilemez, buyuk gorsel her zaman gorselsizden iyi.
     */
    suspend fun enqueueReceipt(
        householdId: String,
        tripId: String,
        imagePath: String,
    ): Receipt {
        val receipt = Receipt(
            id = newId(),
            householdId = householdId,
            tripId = tripId,
            imagePath = imagePath,
            capturedAt = clock(),
            status = ReceiptStatus.PENDING,
            createdAt = clock(),
        )
        receiptDao.insert(receipt)
        return receipt
    }

    /**
     * Geziyi kapatir. TEK CIHAZ KAPATIR.
     *
     * @return true = bu cagri kapatti; false = gezi zaten kapaliydi ve
     *   HICBIR SEY yazilmadi. Cagiran taraf bu ayrimi gormek zorunda: ikinci
     *   kapanis mutabakati yeniden yaparsa satin almalar cift sayilir
     *   (bkz. [Trip.ownerMemberId]).
     */
    suspend fun closeTrip(tripId: String, memberId: String): Boolean =
        tripDao.closeIfOpen(tripId, memberId, clock()) == 1

    /**
     * Fissiz mutabakat (F4.8): planlananlar alindi sayilir.
     *
     * YALNIZCA [closeTrip] true dondurunce cagirilmali - bkz.
     * [TripLineDao.markAllTaken]. Ikinci kez calisirsa satin almalar cift
     * sayilir.
     *
     * @return alindi yazilan satir sayisi.
     */
    suspend fun reconcileOptimistically(tripId: String): Int =
        tripLineDao.markAllTaken(tripId, clock())

    /**
     * Urunu "her zamankiler"e ekler ya da cikarir (F6.8).
     *
     * Etkisi bir sonraki gezide gorunuyor: [openOrGetActiveTrip] yeni liste
     * acarken sabitleri otomatik ekliyor. Tasarim bunu ozet kartinda aciktan
     * soyluyor - *"Bir sonraki alisveriste her zamankiler yeniden eklenecek."*
     */
    suspend fun setStaple(productId: String, isStaple: Boolean) {
        productDao.setStaple(productId, isStaple, clock())
    }

    /**
     * Satirin akibetini kaydeder (F4.12).
     *
     * Uc sonucun AYNI puani almamasi F6.2'nin sarti: *"gerekmedi"* oneriyi
     * bastirmali, *"unuttum"* yukseltmeli. Tek bir boolean ikisini ayni sey
     * yapiyordu.
     *
     * [TakeOutcome.TAKEN] disindaki ikisi satiri isaretsiz birakiyor, yani
     * `ProductStats` onlari alim SAYMIYOR - istatistik `checked = 1` okuyor.
     * `checkedAt` de temizleniyor ki "alindi" izi kalmasin.
     */
    suspend fun setOutcome(rowId: String, outcome: TakeOutcome) {
        val taken = outcome == TakeOutcome.TAKEN
        tripLineDao.setOutcome(
            id = rowId,
            checked = taken,
            at = if (taken) clock() else null,
            outcome = outcome,
        )
    }

    /** Bitir ekranindan geri alma: bu satir aslinda alinmadi. */
    suspend fun setTaken(lineId: String, taken: Boolean) {
        tripLineDao.setChecked(lineId, taken, if (taken) clock() else null)
    }

    /**
     * Urunu listeye ekler. ZATEN VARSA adet artirir, ikinci satir ACMAZ.
     *
     * UNIQUE(tripId, productId) bunu zaten engelliyor ama kisita carpip hata
     * almak kullaniciya "ekleyemedim" demek olurdu. Dogru davranis: es zaten
     * eklemisse adedi artir - iki kisi ayni ekmegi istedi, iki ekmek degil.
     */
    suspend fun add(
        householdId: String,
        tripId: String,
        product: Product,
        memberId: String,
        count: Double = 1.0,
        isFromSuggestion: Boolean = false,
    ): TripLine {
        // SILINMISLERE DE bakiyoruz: tombstone satiri tabloda kaliyor ve
        // UNIQUE(tripId, productId) deletedAt'i bilmiyor. Yalnizca canli
        // satirlara baksaydik "cikardim, geri ekledim" akisi kisita carpip
        // uygulamayi cokertirdi - kullanicinin yapacagi en dogal ikinci hareket.
        val existing = tripLineDao.findIncludingDeleted(tripId, product.id)
        if (existing != null) {
            val current = if (existing.deletedAt != null) {
                // Mezardan cikar: yeni satir gibi davran ama AYNI id'yi koru.
                // Yeni id uretmek senkronda "silindi" ve "eklendi" olaylarini
                // birbirinden kopuk iki satira baglardi.
                existing.copy(
                    deletedAt = null,
                    quantity = count,
                    checked = false,
                    checkedAt = null,
                    addedByMemberId = memberId,
                    fromSuggestion = isFromSuggestion,
                    createdAt = clock(),
                )
            } else {
                // Es zaten eklemis: adedi artir, "kim ekledi"yi EZME.
                existing.copy(quantity = existing.quantity + count)
            }
            tripLineDao.update(current)
            return current
        }
        val row = TripLine(
            id = newId(),
            householdId = householdId,
            tripId = tripId,
            productId = product.id,
            quantity = count,
            unit = product.defaultUnit,
            addedByMemberId = memberId,
            fromSuggestion = isFromSuggestion,
            createdAt = clock(),
        )
        tripLineDao.insert(row)
        return row
    }

    /** checkedAt SAKLANIYOR: reyonda mi evde mi isaretlendi sorusu sonra lazim olacak. */
    suspend fun toggleChecked(rowId: String, checked: Boolean) =
        tripLineDao.setChecked(rowId, checked, if (checked) clock() else null)

    suspend fun remove(rowId: String) = tripLineDao.softDelete(rowId, clock())

    /**
     * Adiyla urun bulur, yoksa olusturur. matchKey uzerinden bakiyor ki
     * "Ekmek" ile "EKMEK" ayri urun olmasin (F2.4).
     */
    suspend fun findOrCreateProduct(
        householdId: String,
        name: String,
        categoryId: String,
        unit: String,
    ): Product {
        val key = matchKey(name)
        productDao.findByMatchKey(householdId, key)?.let { return it }
        val product = Product(
            id = newId(),
            householdId = householdId,
            name = name,
            matchKey = key,
            categoryId = categoryId,
            defaultUnit = unit,
            createdAt = clock(),
        )
        productDao.insert(product)
        return product
    }
}
