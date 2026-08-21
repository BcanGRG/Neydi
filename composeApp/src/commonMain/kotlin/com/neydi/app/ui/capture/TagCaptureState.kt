package com.neydi.app.ui.capture

import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.Store
import com.neydi.app.data.ocr.SUPPORTED_CHAINS
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
    /**
     * Kaydetme bildirimi - EKRANIN KENDISINDE gosteriliyor.
     *
     * Once mesaj gezinme katmanina veriliyor ve Liste gosteriyordu; o tasarim
     * ekrandan CIKMAYI varsayiyordu. Seri cekimde ekrandan cikilmiyor, yani
     * bildirimin de burada durmasi gerekiyor.
     */
    val toast: String? = null,
    /** Acik secici - BIR SEFERDE EN FAZLA BIRI (gezinme sozlesmesi). */
    val picker: TagPicker? = null,
    val storeQuery: String = "",
    /**
     * "«AKYURT» diye yeni market" onay cipi bekliyor (karar 59).
     *
     * IKINCI DOKUNUS: ilk dokunus adi buraya koyuyor, cipi basmak marketi
     * gercekten yaratiyor. Yazim hatasi kaynaginda yakalaniyor.
     */
    val pendingStoreName: String? = null,
    val productQuery: String = "",
    val productPicks: List<CatalogSeed> = emptyList(),
    /**
     * Son secilen urun - secicinin EN BASINDA, kartta DEGIL.
     *
     * Karttan cikarilmasinin gerekcesi `TagCaptureViewModel.lastProductName`
     * KDoc'unda: urun markette tekrarlamiyor, o yuzden onu karta yazmak
     * neredeyse her cekimde yanlis bir ad iddia etmek oluyordu.
     */
    val lastProduct: String? = null,
    /** Bu markette gorulmus markalar - marka sheet'inin havuzu (karar 52). */
    val brandPool: List<String> = emptyList(),
)

/** Onay kartindan acilabilen uc secici. */
internal enum class TagPicker { PRODUCT, STORE, BRAND }

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
    /**
     * ETIKETTEN OKUNAN ad - KANIT, urun adi DEGIL (karar 51).
     *
     * Urun seciciye "Etiket metni: DST YGRT 1000G" diye yaziliyor ki kullanici
     * neye baktigimizi gorsun. Kimlik katalogdan geliyor.
     */
    val tagText: String? = null,
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
    /**
     * Kaydedilebilir mi - YALNIZCA FIYATA bakiyor.
     *
     * Sozlesme birebir soyle diyor: *"Kaydet pasif; ilk rakamda etkinlesir."*
     * Kod urun adini da sart kosuyordu ve o zaman DOGRUYDU: urun secici yoktu,
     * yani ad bos gecilebilseydi adsiz bir gozlem yazilirdi.
     *
     * Simdi urun secici var ve ad neredeyse her zaman dolu geliyor (son secilen
     * urun hazir, karar 51). Kalan tek bos hal ilk kurulumdaki ilk cekim; onu
     * `TagCaptureViewModel.save` yakaliyor ve urun secicisini aciyor - butonu
     * pasif birakip kullaniciyi neyin eksik oldugunu tahmin etmeye birakmak
     * yerine, dogrudan eksik olan seyi soruyor.
     */
    val canSave: Boolean get() = priceText.isNotBlank()
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
    // DESTEKLENMEYEN ZINCIRDE SERIT CIZILMIYOR (karar 49): onun yerine kartin
    // BASINDA tek cumle var ([unsupportedChainMessage]). Iki yuzeyin ayni seyi
    // soylemesi kullaniciya iki is varmis gibi gorunurdu.
    skipped == TagSkip.UNSUPPORTED_CHAIN -> null
    priceText.isBlank() && skipped == TagSkip.PRICE_CONTRADICTS_UNIT_PRICE ->
        "Okunan fiyat etiketin birim fiyatıyla uyuşmuyor — doğrula"
    priceText.isBlank() -> "Fiyat okunamadı — yaz"
    // "OKUNAMADI" DEGIL "SECILMEDI": OCR metni artik urun adi olmuyor
    // (karar 51), yani adin bos olmasi bir okuma hatasi degil - henuz
    // secilmemis olmasi. Eski cumle okunamayan bir sey varmis gibi
    // soyluyordu ve kullaniciyi etikete bakmaya gonderirdi.
    productName.isBlank() -> "Ürün seçilmedi — seç"
    !kurusFromOcr -> "Kuruş okunamadı — kontrol et"
    else -> null
}

/**
 * Grameri cozulmemis zincirin KARTIN BASINDAKI cumlesi (karar 49).
 *
 * Kart burada sessizce bos aciliyordu ve kullanici OCR'i bozuk saniyordu -
 * yani sessiz bosluk, kendi hatasi gibi gorunuyordu. Cumle hem ne
 * okuyabildigimizi ogretiyor hem de fiyati kimin yazacagini soyluyor.
 *
 * Zincir adlari [SUPPORTED_CHAINS]'den geliyor, elle yazilmiyor.
 */
internal fun ConfirmCard.unsupportedChainMessage(
    // PARAMETRE YALNIZCA TEST ICIN, ve bunun yazilmasi gerekiyor: liste sabit
    // kalsaydi "cumle listeden kuruluyor" iddiasi kanitlanamazdi - bugunku
    // listeyle elle yazilmis bir cumle de ayni sonucu verir. Testin ucuncu bir
    // zincir verebilmesi, iddiayi ISIRILABILIR yapan tek sey.
    supported: List<String> = SUPPORTED_CHAINS,
): String? {
    if (reading || skipped != TagSkip.UNSUPPORTED_CHAIN) return null
    val chains = supported.joinToString(" ve ")
    return "$chains etiketlerini okuyabiliyoruz; burada fiyatı sen yaz."
}
