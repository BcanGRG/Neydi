package com.neydi.app.data.db

import androidx.room3.ColumnInfo
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
    /**
     * FISIN USTUNDE BASILI tarih - fotografin cekildigi an DEGIL (F5.8).
     *
     * Fiyat gozleminin `observedAt`'i bunu kullanmak zorunda. Aksi halde bir
     * hafta sonra cekilen bir fis fiyat gecmisine **bir hafta kaymis** girer ve
     * kullanici bunu asla goremez. Daha kotusu: eski fisleri tek gunde toplu
     * cekmek butun gozlemleri **ayni x degerine** yigar ve grafigi tek noktaya
     * duzler.
     *
     * Null = tarih okunamadi. `capturedAt`'e dusmek cagiran tarafin isi.
     */
    val receiptDate: Long? = null,
    /** Fisin ustunde yazan ham magaza adi - Store eslemesi ayri is. */
    val storeNameRaw: String? = null,
    /**
     * OCR'in okudugu GORSEL SATIRLARIN tamami, satir basina bir satir (F4.14).
     *
     * NEDEN SAKLANIYOR: ayristiriciyi degistirmek, eskiden **fotografi yeniden
     * OCR'lamayi** gerektiriyordu - yani bir duzen hatasini duzeltmek icin
     * once cihazda bir fis bulmak, sonra onu yeniden cektirmek gerekiyordu.
     * F4.14 tam olarak buna takildi: AKYURT'un iki satirli duzeni cihazda
     * goruldu ama ham satirlar hicbir yere yazilmadigi icin **kurgu
     * alinamadi**, ve bu projede sentetik fis kurgusu iki kez bosa cikti.
     *
     * F5.7 (ambalaj boyu cikarimi) de ayni seyi istiyor: *"yeniden ayristirma
     * OCR gerektirmesin"*.
     *
     * BICIM: `\n` ile birlestirilmis duz metin, JSON degil. Gorsel satirlar
     * gruplama asamasinda zaten satir sonlarindan arindirilmis oluyor (bkz.
     * `visualRows`), yani ayirici capraz konusamiyor. TypeConverter eklemek
     * semaya kalici bir bagimlilik sokardi; bu alan bir **dokum**, sorgulanan
     * bir yapi degil.
     *
     * Null = eski kayit (v4 oncesi) ya da hic basarili okuma olmadi.
     */
    val rawOcrText: String? = null,
    /** Fisin TOPLAM'i, kurus. Aritmetik kontrolun referansi. */
    val totalMinor: Long? = null,
    val extractedAt: Long? = null,
    val status: ReceiptStatus = ReceiptStatus.PENDING,
    /** Basarisiz okuma sessizce kaybolmasin; Gecmis ekraninda gorunur. */
    val errorMessage: String? = null,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
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
    /**
     * `quantity`'nin BIRIMI - "ad", "kg", "lt". Fiyatin **hangi birim basina**
     * oldugunu belirleyen sey bu.
     *
     * ONCEDEN AYRISTIRILIP ATILIYORDU: `ParsedLine.unit` hesaplaniyor ama
     * kaliciligina yer yoktu, yani olculmus bilgi her yazmada cope gidiyordu.
     * Sonucu somut: sepet tahmini `quantity × unitPriceMinor` carpiyor ve
     * **kg basina bir fiyati adet sayisiyla carpmak** sessiz bir yanlis sonuc
     * uretiyor.
     *
     * Null = ayristirici birim gormedi. Varsayim yapilmiyor - projenin kurali
     * "Bilinmiyorsa null, varsayma".
     */
    val unit: String? = null,
    /** Kurus. */
    val unitPriceMinor: Long?,
    /** Kurus. Aritmetik kontrol bunu toplar. */
    val lineTotalMinor: Long,
    val matchedProductId: String? = null,
    /** 0..1. Esik altinda kalan satir needsReview olur. */
    val confidence: Double? = null,
    val needsReview: Boolean = true,
    /**
     * Bu satir bir INDIRIM mi (F5.6).
     *
     * `ParsedLine.discount` bayragi da kalicilastirmada atiliyordu ve iki gorunmez
     * sonucu vardi: (1) aritmetik kapisi veritabanindan **yeniden hesaplanamiyor**
     * - ayristirici indirimleri cikariyor, ekran hepsini pozitif topluyor, yani
     * indirimli bir fiste ikisi **farkli karar veriyor**; (2) naif bir "her fis
     * satirina bir fiyat gozlemi" yazicisi indirimi **urun fiyati** olarak
     * kaydediyor.
     *
     * Tutar HER ZAMAN pozitif - isareti bu bayrak tasiyor, sayi degil.
     *
     * NOT NULL oldugu icin `defaultValue` ZORUNLU (bkz. ROADMAP "Sema surum plani").
     */
    @ColumnInfo(defaultValue = "0")
    val isDiscount: Boolean = false,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
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
    /**
     * Fiyatin HANGI BIRIM BASINA oldugu - "kg", "L", "adet".
     *
     * Bu kolon olmadan `unitPriceMinor` tek basina anlamsizdi: 690,00 TL bir
     * kilo fiyatiysa 0,182 ile carpilmasi gerekiyor, adet fiyatiysa 1 ile.
     * Sepet tahmini bugun ayrimi bilmeden carpiyor.
     *
     * Null = bilinmiyor. Kaynak `ReceiptLine.unit`.
     */
    val priceUnit: String? = null,
    val observedAt: Long,
    val receiptLineId: String? = null,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
    val deletedAt: Long? = null,
)
