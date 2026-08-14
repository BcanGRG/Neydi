package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Fis fotografi ve okuma durumu.
 *
 * FOTOGRAF ASLA BLOKLAMAZ: alisveris fis islenmesini beklemeden kapanir,
 * okuma arka planda ilerler. status bu yuzden var - kullanici reyondayken
 * bir yukleme cubugu izlemeyecek.
 *
 * imagePath yerel dosya yolu; fotografin KENDISI kisisel veri ve varsayilan
 * olarak cihazdan cikmaz.
 */
@Entity(tableName = "receipt")
data class Receipt(
    @PrimaryKey val id: String,
    val householdId: String,
    val tripId: String,
    val imagePath: String,
    val capturedAt: Long,
    /** Fisin ustunde yazan ham magaza adi - Store eslemesi ayri is. */
    val storeNameRaw: String? = null,
    /** Fisin TOPLAM'i, kurus. Aritmetik kontrolun referansi. */
    val totalMinor: Long? = null,
    val extractedAt: Long? = null,
    val status: ReceiptStatus = ReceiptStatus.PENDING,
    /** Basarisiz okuma sessizce kaybolmasin; Gecmis ekraninda gorunur. */
    val errorMessage: String? = null,
    val createdAt: Long,
    val deletedAt: Long? = null,
)

enum class ReceiptStatus {
    PENDING,
    READING,
    /** Okundu ve Sigma(satirlar) - indirimler = TOPLAM tutuyor. */
    VERIFIED,
    /** Okundu ama toplam tutmadi - satirlar kullanici onayina dusuyor. */
    MISMATCHED,
    FAILED,
}

/**
 * Fisten cikan tek satir.
 *
 * ARITMETIK DEGISMEZ: Sigma(lineTotalMinor) - indirimler = Receipt.totalMinor,
 * +/- 5 kurus. KDV DAHIL DEGIL CIKARILMAZ - Turk perakendesinde raf fiyati
 * zaten KDV dahildir, ayrica cikarmak toplami bozar.
 *
 * matchedProductId null + needsReview true = kullaniciya soruluyor. Tahmini
 * sessizce kabul etmek fiyat gecmisini kirletir.
 */
@Entity(
    tableName = "receipt_line",
    indices = [Index(value = ["receiptId"]), Index(value = ["matchedProductId"])],
)
data class ReceiptLine(
    @PrimaryKey val id: String,
    val householdId: String,
    val receiptId: String,
    /** Fiste yazan hali, hic dokunulmadan. Yanlis eslemeyi ancak bu geri alabilir. */
    val rawText: String,
    val rawTextNormalized: String,
    val quantity: Double = 1.0,
    /** Kurus. */
    val unitPriceMinor: Long?,
    /** Kurus. Aritmetik kontrol bunu toplar. */
    val lineTotalMinor: Long,
    val matchedProductId: String? = null,
    /** 0..1. Esik altinda kalan satir needsReview olur. */
    val confidence: Double? = null,
    val needsReview: Boolean = true,
    val createdAt: Long,
    val deletedAt: Long? = null,
)

/**
 * Fiyat hafizasi - uygulamanin ikinci varlik sebebi.
 *
 * Kaynak fis olabilir (receiptLineId dolu) ya da kullanici elle girmis olabilir.
 * Ikisi ayni tabloda cunku "gecen sefer ne odedik" sorusu kaynagi umursamiyor.
 *
 * packSize/packUnit AYRI ALANLAR ve bu SART: 1 L sut 900 ml'ye dusup ayni
 * fiyata satildiginda birim fiyat degismemis gorunur. Ambalaj degisimini
 * yakalayamayan bir fiyat hafizasi shrinkflation'i "fiyat sabit" diye rapor
 * eder - yani yalan soyler. PriceHint.PackChanged tam olarak buradan besleniyor.
 */
@Entity(
    tableName = "price_observation",
    indices = [
        // "Bu urunu en son ne kadara aldik" ve trend cizimi bu index'ten gecer.
        // observedAt DESC siralamasi icin urunle birlikte.
        Index(value = ["productId", "observedAt"]),
        Index(value = ["storeId"]),
    ],
)
data class PriceObservation(
    @PrimaryKey val id: String,
    val householdId: String,
    val productId: String,
    val storeId: String?,
    /** Kurus, odenen birim fiyat. */
    val unitPriceMinor: Long,
    /** 1.0 / 900.0 gibi. Bilinmiyorsa null - varsayma. */
    val packSize: Double? = null,
    /** "L", "ml", "kg", "g", "adet". */
    val packUnit: String? = null,
    val observedAt: Long,
    val receiptLineId: String? = null,
    val createdAt: Long,
    val deletedAt: Long? = null,
)
