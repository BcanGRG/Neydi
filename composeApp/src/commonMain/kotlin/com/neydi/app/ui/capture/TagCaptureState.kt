package com.neydi.app.ui.capture

import com.neydi.app.data.db.Store
import com.neydi.app.data.ocr.TagSkip

/**
 * Etiket cekim ekraninin cizilecek TAM hali.
 *
 * `ListState`in ikizi: ViewModel'siz cizilebiliyor, dolayisiyla onizlemeler ve
 * testler gercek bir kamera olmadan her hali kurabiliyor.
 */
internal data class TagCaptureState(
    /** Kart aciksa cekim yapilmis demektir; null ise kamera serbest. */
    val card: ConfirmCard? = null,
    val stores: List<Store> = emptyList(),
    /** Yapiskan market - son gozlemin marketi (bkz. TagCaptureViewModel). */
    val storeId: String? = null,
    val saving: Boolean = false,
    /** Cekim ya da yazma basarisiz oldu; kullaniciya soylenmek zorunda. */
    val failure: String? = null,
)

/**
 * Onay kartinin icerigi.
 *
 * FIYAT METIN OLARAK TUTULUYOR, sayi olarak degil. Kullanici yaziyorken alan
 * gecici olarak gecersiz oluyor ("38," gibi) ve bunu sayiya zorlamak imleci
 * kaybettirir ya da yazilani sessizce degistirir. Sayiya cevirme kaydetme
 * aninda, tek yerde.
 */
internal data class ConfirmCard(
    /** Cekilen karenin yolu; kaydedince ya da vazgecince SILINIYOR (karar 29). */
    val photoPath: String,
    val priceText: String = "",
    val productName: String = "",
    val brand: String? = null,
    /** OCR hala kosuyor - alanlar iskelet cizilecek. */
    val reading: Boolean = true,
    /**
     * Fiyat OCR'dan mi geldi, yoksa bos mu birakildi.
     *
     * Kurus okunmadiysa (`kurusFromOcr = false`) tasarim fiyat alanina
     * odaklanmayi istiyor - kullanicidan iki hane istemek, yanlis iki haneyi
     * sessizce kaydetmekten iyi.
     */
    val kurusFromOcr: Boolean = false,
    /** Okuyucu neden sustu - amber seridin cumlesi buna dayaniyor. */
    val skipped: TagSkip? = null,
) {
    /** Kaydedilebilir mi: fiyat ve urun adi olmadan gozlem yazilmaz. */
    val canSave: Boolean get() = priceText.isNotBlank() && productName.isNotBlank()
}

/**
 * Eksik alanin amber seritte gorunecek cumlesi - ya da null.
 *
 * BIRDEN COK ALAN BOSSA YALNIZCA ILKI donuyor (tasarim). Iki uyari ust uste
 * kullaniciya iki is varmis gibi gorunur; oysa is tek: karti tamamlamak.
 *
 * Sira tesadufi degil - kartin kendi okuma sirasi: once fiyat, sonra urun.
 */
internal fun ConfirmCard.missingFieldMessage(): String? = when {
    reading -> null
    priceText.isBlank() && skipped == TagSkip.UNSUPPORTED_CHAIN ->
        "Bu marketin etiketi henüz okunmuyor — fiyatı yaz"
    priceText.isBlank() && skipped == TagSkip.PRICE_CONTRADICTS_UNIT_PRICE ->
        "Etiketteki iki fiyat uyuşmuyor — doğrusunu yaz"
    priceText.isBlank() -> "Fiyat okunamadı — yaz"
    productName.isBlank() -> "Ürün adı okunamadı — yaz"
    !kurusFromOcr -> "Kuruş okunamadı — kontrol et"
    else -> null
}
