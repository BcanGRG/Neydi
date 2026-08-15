package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Ne onerdik ve ne oldu.
 *
 * Bu tablo olmadan oneri motoru KENDI ISABETINI OLCEMEZ. Surekli reddedilen
 * bir oneriyi susturmanin tek yolu reddedildigini kaydetmis olmak; aksi halde
 * uygulama ayni yanlisi her gezide tekrarlar ve kullanici oneri seridini
 * tamamen gormezden gelmeye baslar.
 */
@Entity(tableName = "suggestion_event")
data class SuggestionEvent(
    @PrimaryKey val id: String,
    val householdId: String,
    val productId: String,
    val tripId: String?,
    val suggestedAt: Long,
    /** Kullaniciya GOSTERILEN gerekce - "12 gundur almadin". Gerekcesiz oneri reklam gibi okunur. */
    val reason: String,
    val outcome: SuggestionOutcome = SuggestionOutcome.SHOWN,
    val respondedAt: Long? = null,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
    /**
     * Tombstone - Conventions madde 3 bu tabloda da gecerli.
     *
     * Eskiden YOKTU ve bu bir tutarsizlikti: tablo `householdId` tasiyor, yani
     * madde 2'ye gore senkron edilen kullanici verisi, ama `deletedAt`'i yoktu
     * ve yazili bir muafiyeti de yoktu. Ustelik `Daos.kt`'nin sert kurali
     * "`deletedAt IS NULL` HER SORGUDA" bu tabloda **yazilamiyordu**.
     * (`ProductStats` muaf ve muafiyeti YAZILI: turetilmis cache.)
     */
    val deletedAt: Long? = null,
)

/**
 * Enum girdileri TEXT olarak saklaniyor, yani bu adlar **sema**.
 *
 * Turkce yazilmislardi (`GOSTERILDI/EKLENDI/REDDEDILDI/YOKSAYILDI`) ve yayinlanmis
 * v2 semasinda oyle duruyorlardi. Yeniden adlandirma **yalnizca tablo bos oldugu
 * icin** bedava oldu: DAO'su ve yazicisi yoktu. Ilk satir yazildiktan sonra bir
 * girdiyi yeniden adlandirmak mevcut satirlari **yetim** birakirdi - Room'un enum
 * donusturucusu bilinmeyen adi **okurken atar**, yani uygulama kendi gecmisini
 * okurken coker.
 */
enum class SuggestionOutcome {
    /** Gosterildi, henuz bir sey olmadi. */
    SHOWN,
    ADDED,
    /** Kullanici acikca reddetti - bu urun bir sure onerilmemeli. */
    REJECTED,
    /** Gorup dokunmadi. Reddetmekten ZAYIF bir sinyal, ayni sayilmamali. */
    IGNORED,
}

/**
 * Onerilmeyecek urunler (F6.5).
 *
 * NEDEN AYRI TABLO, olay gunlugunden turetme DEGIL: tasarim bu listenin
 * Ayarlar'da **gorunur** ve her satirinin **tek dokunusla geri alinabilir**
 * olmasini sart kosuyor - *"kara delik olmamali"*. Append-only bir olay
 * gunlugunde temiz bir "engeli kaldir" yazmasi yok; karsi-olay eklemek ve her
 * okumayi onu katlamayi ogretmek gerekirdi.
 *
 * `source` ayrimi onemli: uc-vurus sessiz otomatik bastirma (AUTO) ile
 * kullanicinin acik karari (MANUAL) ayni sey degil. Kullanicinin elle kaldirdigi
 * bir engeli motorun sessizce geri koymasi, ayarin ise yaramadigi hissi verir.
 *
 * `unblockedAt` doldurulunca engel kalkiyor; satir SILINMIYOR ki motor ayni
 * urunu tekrar otomatik engellemeye kalkismadan once kullanicinin karari
 * gorulebilsin.
 */
@Entity(
    tableName = "suggestion_block",
    indices = [
        // Okuma her zaman "bu hanede bu urun engelli mi": tek satir dokunuluyor.
        Index(value = ["householdId", "productId"], unique = true),
    ],
)
data class SuggestionBlock(
    @PrimaryKey val id: String,
    val householdId: String,
    val productId: String,
    val source: BlockSource,
    val blockedAt: Long,
    /** Dolu ise engel KALDIRILMIS. Satir kayit icin duruyor. */
    val unblockedAt: Long? = null,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
    val deletedAt: Long? = null,
)

enum class BlockSource {
    /** Uc-vurus kurali kendiliginden bastirdi. */
    AUTO,

    /** Kullanici "bunu onerme" dedi. */
    MANUAL,
}
