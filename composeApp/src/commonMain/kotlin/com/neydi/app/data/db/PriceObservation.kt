package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Fiyat hafizasi - uygulamanin ikinci varlik sebebi.
 *
 * BIR SATIR = BIR RAF ETIKETI CEKIMI. Kullanici markette bir etiket cekiyor,
 * onay kartinda dogruluyor, satir buraya yaziliyor: hangi urun, hangi marka,
 * hangi market, kac lira, ne zaman. Kullanicinin kendi cumlesiyle: *"yogurdu
 * hangi marketten, hangi marka, hangi tarihte, kac TL'ye almisim."*
 *
 * TABLO V1'DEN BERI VARDI VE BOSTU. Fis doneminde hicbir yazani olmadi -
 * `receiptLineId` kolonu o donemin izidir ve E11'de dustu. Pivotun sansi buydu:
 * inecek yer hazirdi, goc riski sifirdi.
 *
 * MARKA URUNUN DEGIL GOZLEMIN ALANI (pivot karari 2) ve karsilastirmanin
 * tamami buna bagli: urun jenerik kaliyor ("yogurt"), marka her gozlemde
 * degisiyor. Tersi secilseydi "Dost Yogurt" ile "Pinar Yogurt" AYRI urun
 * olurdu ve *"yogurt kaca"* diye tek bir yerden bakmak imkansizlasirdi.
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
    /** Kurus - etiketin BUYUK rakami. */
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
     * Kaynak: etiketin kucuk puntolu birim fiyat satiri ("1 KG = 249,90").
     */
    val priceUnit: String? = null,
    /**
     * Gozlemin markasi - "Dost", "Pinar". NULL MESRU ve bu bir kacamak degil:
     * manavda, dokme urunde, market kendi ekmeginde marka YOK. Bos dizgi
     * kullanmak "markasiz" ile "okunamadi"yi ayni sepete atardi.
     *
     * Ayristirici oneri uretiyor (etiket adinin ilk kelimesi) ama SESSIZCE
     * yazmiyor: kullanici onay kartinda goruyor ve degistirebiliyor.
     */
    val brand: String? = null,
    /** Cekim ani. Etikette basili tarih yok - "simdi" tek dogru cevap. */
    val observedAt: Long,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
    val deletedAt: Long? = null,
)
