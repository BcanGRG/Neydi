package com.neydi.app.ui.capture

import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.Store
import com.neydi.app.data.image.GuideBox
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
 * FIYAT SAYI OLARAK TUTULUYOR, metin olarak degil - ve bu karar 73 ile
 * DEGISTI. Serbest yazim doneminde alan metindi cunku kullanici yazarken
 * gecici olarak gecersiz olabiliyordu (`"38,"` gibi). Alan sagdan dolunca
 * gecersiz bir ara hal kalmadi: her tus bir hane ekliyor, deger her an
 * gecerli. Metin tutmanin tek sebebi ortadan kalkti, tasidigi risk kalmadi.
 */
internal data class ConfirmCard(
    /** Cekilen karenin yolu; kaydedince ya da vazgecince SILINIYOR (karar 29). */
    val photoPath: String,
    /**
     * Fiyat KURUS olarak; `0` = alan bos ve ekranda *"— TL"* yaziyor.
     *
     * ALAN SAGDAN DOLUYOR (karar 73), yani metin degil SAYI tutuluyor:
     * `3` → 0,03 · `39` → 0,39 · `3950` → 39,50. Serbest yazim doneminde
     * `parseMinorInput` kullaniliyordu ve sozlesmesi *`"106"` → 106,00* idi;
     * kullanici 39,50 demek isteyip `3950` yazinca kart **3.950,00** kabul
     * ediyordu - yuz kat hata, hicbir uyari yok. Cihaz provasinda yasandi.
     *
     * `parseMinorInput` bu alandan CIKTI (karar 73); etiket metnini okuyan
     * `parseMinor` yerinde duruyor - o baska bir kaynagin dogrusu.
     */
    val priceMinor: Long = 0L,
    /**
     * Kullanici fiyat alanina DOKUNDU mu - iki kurali birden tasiyor.
     *
     * - Karar 73: dolu alanda ILK RAKAM degeri sifirlayip bastan baslatiyor.
     *   Ayrim bu bayrakta; ikinci rakamdan itibaren normal ekleme.
     * - Karar 72: kurus uyarisi ilk duzenlemede susuyor ve o kartta geri
     *   gelmiyor. Uyari yalnizca OCR degeri HIC ELLENMEDIYSE suruyor.
     */
    val priceTouched: Boolean = false,
    /**
     * SECILI HANENIN indeksi - `null` = secim yok (karar 75).
     *
     * Dokunus en yakin haneye TEK ATIMLIK secim koyuyor: yazilan rakam yalniz
     * o haneyi degistiriyor, uzunluk sabit kaliyor ve secim dusuyor. Secim
     * ILERLEMIYOR - ikinci hane ikinci dokunus ister.
     *
     * Karar 73 ile CAKISMIYOR cunku tetikleyicileri ayrik: "dolu alanda ilk
     * rakam sifirlar" yalnizca SECIMSIZ yazimda gecerli. Secim varken sifirlama
     * da, sagdan dolum da devrede degil.
     *
     * Indeks [priceDigits] uzerinde; alan degisince gecersiz kalabilecegi icin
     * her kullanimda sinirlaniyor.
     */
    val priceSelection: Int? = null,
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
    /**
     * Etiketten okunan ambalaj boyu - shrinkflation'in TEK kaynagi (F5.7).
     *
     * ## Kartta GORUNMUYOR ve bu bilincli
     *
     * Sozlesme kartin alanlarini sayiyor: Fiyat, Urun, Marka, Market, Tarih.
     * Gramaj gorunur bir satir olsaydi kullanicidan onaylamasi beklenirdi -
     * oysa dogrulayabilecegi tek an zaten sonrasi: ambalaj degistiginde
     * ipucunun kendisi *"900 gr -> 800 gr"* diye ikisini birden yaziyor.
     *
     * ## Sessizce yazilmasi neden guvenli - OLCULDU
     *
     * Tekrar cekimlerde gramaj KAYMIYOR: `133220/133226/133227` (ayni sut
     * etiketi, uc kare) ucunde de `1.0 lt`, `133247/133248/133249` ucunde de
     * `2.0 kg`. Doksan dokuz fiksturde ayni etiketin iki FARKLI gramaj okudugu
     * tek vaka yok - uydurma bir ambalaj degisiminin kaynagi tam da bu olurdu.
     *
     * Okundu/okunmadi asimetrisi de zararsiz: `toPriceHint` iki gozlemin
     * IKISININ de gramajini istiyor, biri null ise degisim iddia etmiyor.
     * `null` "ayni degil" degil, "bilmiyorum" demek.
     *
     * Celiskili etiketlerde gramaj hic gelmiyor - bkz. `readTagFields`.
     */
    val packSize: Double? = null,
    val packUnit: String? = null,
    /**
     * Cekim anindaki kadraj rehberi - seridin KAYNAGI (karar 74).
     *
     * Serit karenin merkezinden degil, kullanicinin kadraja oturttugu yerden
     * kirpiliyor. `null` = olculemedi; serit o zaman merkez kirpimla ciziliyor
     * (eski davranis) - bos kalmasindansa yanlis yerden dogru.
     */
    val guide: GuideBox? = null,
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
    val canSave: Boolean get() = priceMinor > 0L
}

/** Alanin ham hane dizisi - `0` bos demek, sifir bir fiyat degil. */
internal fun ConfirmCard.priceDigits(): String = if (priceMinor == 0L) "" else priceMinor.toString()

/**
 * SAGDAN DOLAN fiyat alani (karar 73) - yazar kasa gibi.
 *
 * Alan ham HANE DIZISI tutuyor; gorunen deger ondan turuyor. Etiketteki
 * `39,50` okuma sirasiyla 3-9-5-0 tuslaniyor ve dogru sonuca variyor. Serbest
 * yazimda ayni tuslar 3.950,00 veriyordu.
 *
 * ## Uc kural, ucu de tasarimdan
 *
 * - **Virgul ve nokta yok sayiliyor** - ayirici diye bir kavram kalmadi.
 * - **Dolu alanda ilk rakam bastan baslatiyor**: OCR `24,90` yazmissa ve
 *   kullanici `3` tuslarsa deger `0,03` oluyor, `2490` + `3` degil. Duzeltme
 *   YENIDEN YAZMAK; fiyat uc-bes tus. BOS alanda bu kural islemiyor - orada
 *   sifirlanacak bir sey yok.
 * - **Silme sagdan** ve bastan baslatma kuralina girmiyor: geri tusu rakam
 *   degil, dokunulmus alani sifirlamasi icin sebep yok.
 *
 * Bos alan `0` donuyor ve ekran *"— TL"* ciziyor; Kaydet *"ilk rakamda"*
 * degil DEGER SIFIRDAN CIKINCA etkinlesiyor - `0` tuslamak bir fiyat degil.
 */
internal fun ConfirmCard.withPriceInput(raw: String): ConfirmCard {
    val typed = raw.filter { it.isDigit() }
    val current = priceDigits()
    val grew = typed.length > current.length

    // SECILI HANE VARSA YALNIZ O DEGISIYOR (karar 75).
    //
    // Uzunluk sabit kaliyor, secim dusuyor ve ILERLEMIYOR - ikinci hane ikinci
    // dokunus ister. Ilerleseydi ustteki hanede bir tus fazlasi degeri on kat
    // kaydirirdi; tek atimlik secim bunu imkansiz kiliyor.
    //
    // Sagdan dolum ve sifirlama bu dalda HIC calismiyor: iki kural ayni anda
    // tanimli olamaz, tetikleyicileri ayrik.
    val at = priceSelection
    if (grew && at != null && at in current.indices) {
        val digit = typed.lastOrNull { it.isDigit() } ?: return this
        val replaced = current.replaceRange(at, at + 1, digit.toString())
        // BASTAKI SIFIR ATILMIYOR: `450` -> `050` uzunlugu korumak zorunda,
        // yoksa 50 olur ve kullanici tek hane degistirdigini sanirken deger
        // on kata bolunur. `trimStart('0')` bu dalda BILEREK yok.
        return copy(
            priceMinor = replaced.toLongOrNull() ?: priceMinor,
            priceTouched = true,
            priceSelection = null,
        )
    }
    // SIFIRLAMA YALNIZCA **DOLU** ALANDA. Kural once bosa da uygulaniyordu ve
    // testi kirdi: bos alana tek seferde gelen `3950` son haneye inip `0`
    // oluyordu. Bos alanda sifirlanacak bir sey yok - tasarimin cumlesi de
    // birebir *"dolu alanda ilk rakam"* diyor. Ayrica IME'nin birden cok
    // karakteri tek seferde islemesine karsi da korumali.
    val restart = grew && !priceTouched && priceMinor > 0L
    val next = (if (restart) typed.takeLast(1) else typed)
        .trimStart('0')
        .takeLast(MAX_PRICE_DIGITS)
    // SILME DE SECIMI DUSURUYOR (karar 75) - geri tusu bir hane secimi degil.
    return copy(
        priceMinor = next.toLongOrNull() ?: 0L,
        priceTouched = true,
        priceSelection = null,
    )
}

/**
 * Dokunulan haneyi seciyor - TEK ATIMLIK (karar 75).
 *
 * `450,99`da 5'e dokunup 6 yazmak 460,99 veriyor. Serbest yazim doneminde bu
 * bes tusluk yeniden yazimdi ve kararin kendi kabul ettigi sinirin (3-5 tus)
 * tam ucundaydi; kullanici cihazda bunu bildirdi.
 *
 * AYRAC SECILEMIYOR: alanda sabit duruyor, tasinamiyor, silinemiyor. Yuz kat
 * hata onun YOKLUGUNDAN doguyordu, haneye dokunmaktan degil - bu yuzden karar
 * 73'un korudugu sey bozulmuyor.
 *
 * @param index [priceDigits] uzerindeki hane; sinir disi ya da `null` ise
 *   secim dusuyor.
 */
internal fun ConfirmCard.withPriceSelection(index: Int?): ConfirmCard {
    val digits = priceDigits()
    val valid = index?.takeIf { it in digits.indices }
    return if (valid == priceSelection) this else copy(priceSelection = valid)
}

/**
 * Alanin tasiyabilecegi en cok hane.
 *
 * Yedi hane 99.999,99 TL demek - bir raf etiketinde gorulebilecek her seyin
 * ustunde. Sinir tasmayi degil, KAZA ile uzun basili kalmis bir tusu
 * kesiyor: sinirsiz birakilsaydi `Long` tasmasi sessizce negatif fiyat
 * yazardi.
 */
private const val MAX_PRICE_DIGITS = 7

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
    priceMinor == 0L && skipped == TagSkip.PRICE_CONTRADICTS_UNIT_PRICE ->
        "Okunan fiyat etiketin birim fiyatıyla uyuşmuyor — doğrula"
    priceMinor == 0L -> "Fiyat okunamadı — yaz"
    // "OKUNAMADI" DEGIL "SECILMEDI": OCR metni artik urun adi olmuyor
    // (karar 51), yani adin bos olmasi bir okuma hatasi degil - henuz
    // secilmemis olmasi. Eski cumle okunamayan bir sey varmis gibi
    // soyluyordu ve kullaniciyi etikete bakmaya gonderirdi.
    productName.isBlank() -> "Ürün seçilmedi — seç"
    // KURUS UYARISI ILK DUZENLEMEDE SUSUYOR (karar 72) ve o kartta geri
    // gelmiyor. Uyarinin isi kullaniciyi fiyata BAKTIRMAK; duzenleme bakisin
    // kanitidir. Cevaptan sonra da bagiran uyari yalanci coban olur.
    // Karar 73'ten sonra elle giren her deger zaten kuruslu, yani bu satir
    // fiilen yalnizca ELLENMEMIS OCR degeri icin kaliyor.
    !kurusFromOcr && !priceTouched -> "Kuruş okunamadı — kontrol et"
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
    if (supported.isEmpty()) return null
    // SON ZINCIR "ve" ILE, ONCEKILER VIRGULLE. Onceki hali hepsini " ve " ile
    // bagliyordu; iki zincirle dogru gorunuyordu ve UCUNCUSU eklendigi gun
    // "A101 ve BİM ve Migros" cikti. Turkce liste baglaci boyle kurulmaz.
    val chains = if (supported.size == 1) {
        supported.first()
    } else {
        supported.dropLast(1).joinToString(", ") + " ve " + supported.last()
    }
    return "$chains etiketlerini okuyabiliyoruz; burada fiyatı sen yaz."
}
