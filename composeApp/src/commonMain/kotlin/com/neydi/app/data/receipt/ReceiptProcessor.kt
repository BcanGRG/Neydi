package com.neydi.app.data.receipt

import com.neydi.app.data.db.ProductAliasDao
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.db.ReceiptDao
import com.neydi.app.data.db.ReceiptLine
import com.neydi.app.data.db.ReceiptLineDao
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.db.TripDao
import com.neydi.app.data.matchKey

/**
 * OKUNAMAYAN FIS ESIGI.
 *
 * Bunun altinda kalan okuma "fis okundu ama bos" degil, "FOTOGRAF OKUNAMADI"
 * demek ve kullaniciya oyle soylenmeli.
 *
 * Gerekcesi olculdu: ~60 kalemlik bir fis tek kareye sigdirilinca satir basina
 * **4,7 piksel** dusuyor ve ML Kit 60 satirin ikisini okuyabildi. Bu yazilimla
 * cozulemez - kucultme siniriyla da ilgisi yok, ham kamera cozunurlugu bile
 * ~7 px/satir verirdi. Bir metrelik fisi tek karede okutmak fiziksel olarak
 * mumkun degil; dogru cevap kullaniciya "parca parca cek" demek, ve uygulama
 * ayni geziye birden fazla fis baglamayi zaten destekliyor.
 *
 * Sessizce bos sonuc gostermek en kotusu olurdu: kullanici fisi cekmis
 * sanardi, fiyat hafizasi hic dolmazdi ve nedenini asla ogrenemezdi.
 */
private const val MIN_USABLE_LINES = 6

/** Okunamayan fiste kullaniciya gosterilecek metin. */
const val UNREADABLE_MESSAGE =
    "Fiş okunamadı - yazılar çok küçük kalmış. Uzun fişleri parça parça çekmeyi dene: " +
        "her karede 10-15 satır görünsün, aynı alışverişe birden fazla fiş ekleyebilirsin."

/** [ReceiptProcessor.process] sonucu. */
enum class ReceiptReadOutcome {
    /** Okundu ve satirlar yazildi. */
    PARSED,

    /** Okunamadi ve korunacak onceki okuma da yoktu. */
    UNREADABLE,

    /**
     * Yeni okuma kullanilamazdi ama ONCEKI IYI OKUMA KORUNDU.
     *
     * Bu durumun ayri olmasi sart: elle yon cevirme iyi bir okumayi ASLA
     * bozmamali. Cihazda yasandi - dik gorunen bir fis aslinda dondurulmus
     * cekilmisti, "Duz oku" 0 dereceyi zorlayinca okuma iki satira dustu ve
     * dogru ayristirilmis alti satir erisilemez oldu. Kullanici bir butona
     * basarak calisan bir seyi kaybetmemeli.
     */
    KEPT_PREVIOUS,

    /** Fis kaydi yok. */
    MISSING,
}

/**
 * Kuyruktaki fisi okur, ayristirir, satirlarini yazar ve durumunu belirler.
 *
 * OCR BURADA KOSUYOR, fotograf cekilirken DEGIL - F4.2'nin kurali "fotograf asla
 * bloklamaz". Bu islem Fis Kontrol ekrani acilinca ya da arka planda tetikleniyor.
 */
class ReceiptProcessor(
    private val reader: ReceiptReader,
    private val receiptDao: ReceiptDao,
    private val receiptLineDao: ReceiptLineDao,
    private val productDao: ProductDao,
    private val aliasDao: ProductAliasDao,
    private val tripDao: TripDao,
    private val clock: () -> Long,
    private val newId: () -> String,
) {

    suspend fun process(receiptId: String, forceRotation: Int? = null): ReceiptReadOutcome {
        val receipt = receiptDao.byId(receiptId) ?: return ReceiptReadOutcome.MISSING
        // Onceki okuma: yeni deneme basarisiz olursa buna GERI DONULUYOR.
        val previous = receiptLineDao.forReceipt(receiptId)
        val previousStatus = receipt.status
        receiptDao.setStatus(receiptId, ReceiptStatus.READING)

        val okuma = reader.readLines(receipt.imagePath, forceRotation).getOrElse { hata ->
            return giveUp(receiptId, previous, previousStatus, hata.message ?: "okunamadi")
        }

        if (okuma.size < MIN_USABLE_LINES) {
            return giveUp(receiptId, previous, previousStatus, UNREADABLE_MESSAGE)
        }

        val reading = parseReceipt(okuma)
        // Yeniden isleme: onceki satirlar silinip yeniden yaziliyor. Ustune
        // eklemek ayni fisi iki kez sayardi ve aritmetik kapisi bunu ancak
        // rastgele yakalardi.
        receiptLineDao.clearForReceipt(receiptId)

        val chain = chainKey(reading.storeName)
        val rows = reading.lines.map { line ->
            val normalized = matchKey(line.name)
            val eslesme = matchLine(receipt.householdId, chain, normalized)
            ReceiptLine(
                id = newId(),
                householdId = receipt.householdId,
                receiptId = receiptId,
                rawText = line.rawText,
                rawTextNormalized = normalized,
                quantity = line.count,
                unitPriceMinor = line.unitPriceMinor,
                lineTotalMinor = line.amountMinor,
                matchedProductId = eslesme?.first,
                confidence = eslesme?.second,
                // ESLESME VARSA ONAY ISTENMIYOR. Alias'tan gelen eslesme
                // kullanicinin daha once verdigi karar; tekrar sormak F4.7'nin
                // butun degerini yok ederdi.
                needsReview = eslesme == null,
                createdAt = clock(),
            )
        }
        receiptLineDao.insertAll(rows)

        val tutuyor = arithmeticHolds(reading)
        val durum = when (tutuyor) {
            true -> ReceiptStatus.VERIFIED
            false -> ReceiptStatus.MISMATCHED
            // Toplam okunamadi: "tutmadi" DEMEK DEGIL. Fisi tutarsiz
            // isaretlemek kullaniciya bizim okuma hatamizi onun hatasi gibi
            // gosterirdi.
            null -> ReceiptStatus.MISMATCHED
        }
        receiptDao.setStatus(receiptId, durum)
        receiptDao.setTotal(receiptId, reading.totalMinor, reading.storeName, clock())
        rollUpTripTotal(receipt.tripId)
        return ReceiptReadOutcome.PARSED
    }

    /**
     * Fis toplamlarini geziye devreder (F4.11).
     *
     * BU DEVIR OLMADAN 36sp'LIK TUTAR HIC GORUNMUYORDU. Ozet karti ve Gecmis
     * ekrani `Trip.totalMinor` okuyor, fis ise `Receipt.totalMinor` yaziyordu;
     * arada bagi kuran kod hic yazilmamisti, yani ikisi de kalici olarak "-"
     * gosteriyordu. Derleme ve testler bunu goremezdi - iki alan da gecerli,
     * sadece biri hic dolmuyordu.
     *
     * FISTE YAZAN TOPLAM ESAS ALINIYOR, satirlarin toplami DEGIL. Kullanici bir
     * satirin tutarini duzeltince odenen para degismiyor, yalnizca bizim OCR
     * hatamiz duzeliyor; satir toplamini yazmak "ne odedik" sorusuna bizim
     * okumamizi cevap vermek olurdu. Toplam hic okunamadiysa alan null kaliyor:
     * dogrulanmamis bir sayiyi 36sp'de manset yapmak, kullanicinin
     * sorgulayamayacagi bir yerde tahmini gercek gibi sunmak demek. O durumda
     * satirlarin toplami Fis Kontrol ekraninda zaten gorunuyor.
     */
    private suspend fun rollUpTripTotal(tripId: String) {
        tripDao.setTotal(tripId, receiptDao.sumTotalsForTrip(tripId))
    }

    /**
     * Okuma basarisiz: onceki iyi okuma varsa ONA GERI DON, yoksa FAILED yaz.
     *
     * Satirlari burada SILMIYORUZ - silme yalnizca basarili ayristirmadan hemen
     * once yapiliyor. Yani bu yol veriye dokunmuyor, sadece durumu geri aliyor.
     */
    private suspend fun giveUp(
        receiptId: String,
        previous: List<ReceiptLine>,
        previousStatus: ReceiptStatus,
        message: String,
    ): ReceiptReadOutcome {
        if (previous.isNotEmpty()) {
            receiptDao.setStatus(receiptId, previousStatus)
            return ReceiptReadOutcome.KEPT_PREVIOUS
        }
        receiptDao.setStatus(receiptId, ReceiptStatus.FAILED, message)
        return ReceiptReadOutcome.UNREADABLE
    }

    /**
     * Ham fis metnini urune baglar.
     *
     * Sira ONEMLI: once ALIAS (kullanicinin daha once verdigi karar), sonra
     * matchKey uzerinden birebir eslesme. Tahmini eslestirme YOK - yanlis
     * tahmini sessizce kabul etmek fiyat gecmisini kirletir ve kullanici bunu
     * asla gormez. Emin olamadigimizda satir onaya dusuyor.
     *
     * @return (productId, confidence) ya da null.
     */
    private suspend fun matchLine(
        householdId: String,
        chain: String,
        normalized: String,
    ): Pair<String, Double>? {
        aliasDao.find(householdId, chain, normalized)?.let { return it.productId to 1.0 }
        productDao.findByMatchKey(householdId, normalized)?.let { return it.id to 0.9 }
        return null
    }
}

/**
 * Magaza adindan zincir anahtari.
 *
 * SUBE DEGIL ZINCIR: alias "BIM"e baglanmali, "BIM BADEMLIK SUBESI"ne degil -
 * yoksa her sube icin ayni duzeltmeyi tekrar yapmak gerekirdi. Ilk kelime
 * pratikte zinciri veriyor (BIM, FILE, AKYURT, MIGROS).
 */
internal fun chainKey(storeName: String?): String =
    storeName?.let { matchKey(it).split(" ").firstOrNull { p -> p.length > 1 } } ?: "bilinmiyor"
