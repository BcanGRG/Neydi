package com.neydi.app.data.ocr

import com.neydi.app.data.store.chainKey

/**
 * Bir etiketten okunan her sey - ve okunmadiysa NEDEN okunmadigi.
 *
 * Sebep alani sus payi degil: onay karti eksik alani amber seritle
 * gosterecek ve serit cumlesi bu ayrima dayaniyor. *"Fiyat okunamadi"* ile
 * *"fiyat okundu ama guvenilmez"* kullaniciya ayni gorunse de bize ayni
 * degil.
 */
internal data class TagFields(
    val price: TagPrice?,
    val name: TagName?,
    val pack: TagPack?,
    val skipped: TagSkip? = null,
)

/** Alanlarin neden bos donduguu. */
internal enum class TagSkip {
    /**
     * Zincirin etiket grameri henuz cozulmedi.
     *
     * E14 kurallari 27 BIM etiketinden cikarildi ve 53 Metro/Migros etiketi
     * onlarin BIM'e ozel oldugunu gosterdi (`docs/18-zincir-karsilastirmasi.md`).
     */
    UNSUPPORTED_CHAIN,

    /**
     * Manset fiyat, etiketin KENDI birim fiyat satiriyla celisiyor.
     *
     * Iki sayidan hangisinin dogru oldugunu bilmiyoruz, dolayisiyla ikisi de
     * yazilmiyor.
     */
    PRICE_CONTRADICTS_UNIT_PRICE,
}

/**
 * Etiketi okur - ama YALNIZCA grameri cozulmus bir zincirde.
 *
 * ## Neden zincir kapisi var
 *
 * Olcum (`docs/18`): E14 ayristiricisi Metro ve Migros etiketlerinde her
 * seferinde bir sayi buluyor ama YANLIS sayiyi - Migros'ta patatese 4389,00 TL,
 * sute 799,00 TL (gercegi 43,95 ve 79,50). Sessizce yanlis olmak, bu ozellikte
 * en kotu hata sinifi: karar 26 fiyat gecmisini market+marka cifti uzerine
 * kuruyor ve oraya giren uydurma bir satir KALICI olarak yaniltir.
 *
 * Ayni karar E14'te bulanik cekim icin de verilmisti. Kural tek: **yanlis
 * fiyat, fiyat olmamasindan kotu.**
 *
 * Kapinin disinda kalan zincirde ekran bos alanlarla aciliyor ve kullanici
 * fiyati kendisi yaziyor - tasarimin zaten ongordugu yol (duzenlenebilir fiyat
 * alani, karar 25).
 *
 * @param chain magazanin `Store.chain` degeri; null ise market secilmemis.
 */
internal fun readTagFields(ocr: TagOcr, chain: String?): TagFields {
    if (chain == null || chain != SUPPORTED_CHAIN) {
        return TagFields(price = null, name = null, pack = null, skipped = TagSkip.UNSUPPORTED_CHAIN)
    }

    val name = readTagName(ocr)
    val pack = readTagPack(ocr)
    val price = readTagPrice(ocr)
    if (price != null && contradictsUnitPrice(ocr, price, pack)) {
        return TagFields(
            price = null,
            name = name,
            pack = pack,
            skipped = TagSkip.PRICE_CONTRADICTS_UNIT_PRICE,
        )
    }
    return TagFields(price = price, name = name, pack = pack)
}

/**
 * Manset fiyat, birim fiyat x gramaj ile tutuyor mu?
 *
 * ## Yalnizca GRAMAJ BILINDIGINDE calisiyor
 *
 * Gramaj yoksa carpan bilinmiyor ve celiski iddiasi kurulamiyor. BIM'in
 * `143 TL` / `11,92 TL/adet` etiketi (12'li tuvalet kagidi) DOGRU bir etiket ve
 * orani 12; "oran 1 degilse suphelen" demek onu da elerdi. Cok-paketle gercek
 * hatayi ayiran tek sey gramaj.
 *
 * ## Tolerans OLCULDU
 *
 * BIM'de uc deger de okunabilen 11 etiketin 9'unda oran **%0,13 icinde**
 * (0,99872 ... 1,00001). Kalan ikisinde oran 17,67 ve 7,67 - ve ikisinde de
 * yanlis okunan sey MANSET DEGIL, birim fiyat satiri (`T06,00` aslinda
 * `106,00`, `750/hg` aslinda `57,50/kg`). Migros'un tek capraz-kontrol
 * edilebilir etiketinde oran **tam 100,0** - ustsimge yapismasi, yani asil
 * yakalamak istedigimiz hata.
 *
 * %2 esigi olculen gurultunun on kati, en kucuk gercek sapmanin iki mertebe
 * altinda.
 *
 * ## BEDELI ACIKCA: iki dogru okuma da eleniyor
 *
 * O iki BIM etiketinde manset dogruydu ve kullanici fiyati elle yazacak.
 * Bilincli: hangi sayinin yanlis oldugunu bilmiyoruz, yalnizca ikisinin
 * uyusmadigini biliyoruz. Uyusmayan iki sayidan birini secmek tahmin olurdu.
 */
private fun contradictsUnitPrice(ocr: TagOcr, price: TagPrice, pack: TagPack?): Boolean {
    val unitPrice = readTagUnitPrice(ocr) ?: return false
    val size = pack?.sizeIn(unitPrice.unit) ?: return false
    val expected = unitPrice.minor * size
    if (expected <= 0.0) return false
    val ratio = price.minor / expected
    return ratio < 1.0 - PRICE_TOLERANCE || ratio > 1.0 + PRICE_TOLERANCE
}

/** Ambalaj boyunu birim fiyatin birimine cevirir; cevrilemiyorsa null. */
private fun TagPack.sizeIn(unit: String): Double? = when {
    this.unit == unit -> size
    this.unit == "gr" && unit == "kg" -> size / 1000.0
    this.unit == "ml" && unit == "lt" -> size / 1000.0
    else -> null
}

/**
 * Grameri cozulmus tek zincir.
 *
 * `chainKey`den TURETILIYOR, elle yazilmiyor: magaza tohumu (`SEED_CHAINS`) ile
 * ayni sozlugu kullanmak zorunda, yoksa kapi hicbir zaman acilmaz ve bunu
 * hicbir test yakalamaz.
 */
private val SUPPORTED_CHAIN = chainKey("BİM")

/** Manset ile birim fiyatin uyustugu sayilan bagil fark - olcum icin bkz. [contradictsUnitPrice]. */
private const val PRICE_TOLERANCE = 0.02
