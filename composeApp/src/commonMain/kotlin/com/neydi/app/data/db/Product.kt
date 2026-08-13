package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Hanenin gercek urunu. CatalogSeed'den turemis olabilir de olmayabilir de -
 * kullanici katalogda olmayan bir sey yazdiginda seedId null kalir.
 */
@Entity(
    tableName = "product",
    indices = [
        // Yazarken tamamlama ve fis eslemesi matchKey uzerinden arar.
        // UNIQUE DEGIL: ayni matchKey'e sahip iki urun mesru olabilir
        // (farkli kategori, farkli ambalaj). Tekillestirme urun birlestirme
        // isidir, sema kisiti degil.
        Index(value = ["householdId", "matchKey"]),
        Index(value = ["categoryId"]),
    ],
)
data class Product(
    @PrimaryKey val id: String,
    val householdId: String,
    val name: String,
    val matchKey: String,
    val categoryId: String,
    /** Katalogdan geldiyse kaynagi. Kullanici yazdiysa null. */
    val seedId: String? = null,
    val defaultUnit: String,
    /**
     * "Her zamanki". Kullanici bunu ELLE isaretler; oneri motoru kendi basina
     * set ETMEZ. Uygulamanin kullanicinin agzina laf koymamasi icin: sabitler
     * listede %70 opaklikla ve raptiyeyle cizilir, yani "bunu ben ekledim"
     * ile "uygulama ekledi" gorsel olarak ayrilir.
     */
    val isStaple: Boolean = false,
    val createdAt: Long,
    val deletedAt: Long? = null,
)

/**
 * Fisteki ham metni bir urune baglar - MAGAZA ZINCIRI BAZINDA.
 *
 * Zincir bazinda olmasinin sebebi: A101 "T.BUGDAY EKMEK 500G" yazarken Migros
 * "TAM BUGDAY EKMEGI" yaziyor. Ayni urun, tamamen farkli iki ham metin. Tek bir
 * global esleme tablosu ikisini birbirine karistirir.
 *
 * confirmedAt null ise esleme TAHMIN; kullanici onaylayinca dolar. Onaylanmamis
 * eslemeler fiyat gecmisine yazilmamali - yanlis esleme fiyat trendini bozar ve
 * bozuk trend, hic trend olmamasindan kotudur.
 *
 * F2.3: UNIQUE(householdId, storeChain, rawTextNormalized)
 */
@Entity(
    tableName = "product_alias",
    indices = [
        // Ayni zincirde ayni ham metin TEK bir urune baglanabilir. Ikinci bir
        // esleme girerse hangisinin dogru oldugu belirsizlesir ve fiyat
        // gecmisi iki urune bolunur.
        Index(value = ["householdId", "storeChain", "rawTextNormalized"], unique = true),
        // Fis okurken en sik sorgu: bu zincirde bu metin daha once gorulmus mu.
        Index(value = ["productId"]),
    ],
)
data class ProductAlias(
    @PrimaryKey val id: String,
    val householdId: String,
    /** Normalize zincir adi: "A101", "MIGROS", "BIM". Sube degil, zincir. */
    val storeChain: String,
    val rawTextNormalized: String,
    val productId: String,
    val confirmedAt: Long? = null,
    val createdAt: Long,
    val deletedAt: Long? = null,
)

/**
 * Oneri motorunun okudugu turetilmis istatistik. Kaynak degil, ONBELLEK -
 * TripLine'lardan yeniden hesaplanabilir. Bu yuzden senkron edilmez ve
 * tombstone tasimaz; bozulursa silinip yeniden uretilir.
 *
 * medianIntervalDays ORTALAMA DEGIL MEDYAN: bir kez 40 gun almayi unutmak
 * ortalamayi kaydirir ve "10 gunde bir aliyoruz" gercegini gizler.
 */
@Entity(tableName = "product_stats")
data class ProductStats(
    @PrimaryKey val productId: String,
    val householdId: String,
    val purchaseCount: Int,
    val lastPurchasedAt: Long?,
    /** Yeterli gozlem yoksa null - uydurma yerine "bilmiyorum". */
    val medianIntervalDays: Int?,
    val updatedAt: Long,
)
