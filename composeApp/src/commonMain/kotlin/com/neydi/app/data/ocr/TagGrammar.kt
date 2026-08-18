package com.neydi.app.data.ocr

import com.neydi.app.data.store.chainKey

/**
 * Bir market zincirinin etiket grameri.
 *
 * ## Neden zincir basina AYRI gramer
 *
 * Tek bir "akilli" ayristirici denemesi olculdu ve basarisiz oldu: E14'un
 * kurallari 27 BIM etiketinden cikarilmisti ve Metro/Migros'ta her etikette bir
 * sayi buluyor ama YANLIS sayiyi (`docs/18-zincir-karsilastirmasi.md`). Uc
 * duzenin de birbirine benzemeyen uc kurali var:
 *
 * | | BIM | Metro | Migros |
 * |---|---|---|---|
 * | Kurus | ustsimge, ayri parca (`50t`) | `TL` liraya yapisik (`79,TL` + `90`) | VIRGULLU parca (`,95`) |
 * | Gramaj | KENDI SATIRI, ad blogunu bitirir | ad satirinin ICINDE | ad satirinin ICINDE |
 * | Ekstra | - | cok-al etiketinde IKI gecerli fiyat | urunu kilo fiyatli manav etiketi |
 *
 * Bunlari tek kurala sigdirmak her birinde biraz yanlis olmak demekti. Ayri
 * gramer, her zincirin kendi gercegini kendi yerinde tutuyor.
 *
 * ## Gramer YOKSA hicbir sey okunmuyor
 *
 * Bilinmeyen zincirde tahmin edilmiyor: kart bos aciliyor ve kullanici fiyati
 * yaziyor. Sessizce yanlis olmak bu ozellikte en pahali hata - karar 26 fiyat
 * gecmisini market+marka cifti uzerine kuruyor ve uydurma bir satir KALICI
 * olarak yaniltir.
 */
internal interface TagGrammar {
    fun readPrice(ocr: TagOcr): TagPrice?
    fun readName(ocr: TagOcr): TagName?
    fun readPack(ocr: TagOcr): TagPack?
}

/**
 * Zincirin grameri - cozulmemisse null.
 *
 * Anahtarlar `chainKey`den TURETILIYOR, elle yazilmiyor: magaza tohumu
 * (`SEED_CHAINS`) ile ayni sozlugu kullanmak zorunda. Elle yazilmis bir dizgi
 * bir gun tohumdan ayrisirsa kapi hic acilmaz ve HICBIR SEY patlamaz - butun
 * alanlar sessizce bos gelir. Testte kilitli.
 */
internal fun grammarFor(chain: String?): TagGrammar? = when (chain) {
    null -> null
    chainKey("BİM") -> BimGrammar
    chainKey("Migros") -> MigrosGrammar
    else -> null
}

/**
 * Grameri COZULMUS zincirlerin GORUNEN adlari.
 *
 * Karar 49'un cumlesi bu listeden kuruluyor - *"BİM ve Migros etiketlerini
 * okuyabiliyoruz"* - ve elle yazilmiyor. Sebep dogrudan: ucuncu gramer
 * eklendiginde elle yazilmis bir cumle SESSIZCE YALAN olur, kullaniciya
 * okuyabildigimiz bir marketi okuyamiyoruz diye soyler. Liste ile
 * [grammarFor] birlikte degismek zorunda ve testte kilitli.
 */
internal val SUPPORTED_CHAINS = listOf("BİM", "Migros")

/**
 * BIM grameri - E12'de 27 gercek etiketten olculdu.
 *
 * Kurallarin kendisi [readTagPrice], [readTagName] ve [readTagPack] icinde
 * duruyor; burasi yalnizca onlari sozlesmeye bagliyor. Ayri durmalarinin
 * sebebi tarihsel degil: her biri kendi olcum gerekcesini KDoc'unda tasiyor ve
 * o gerekceler dosya tasiyarak okunmaz hale gelmemeli.
 */
internal object BimGrammar : TagGrammar {
    override fun readPrice(ocr: TagOcr): TagPrice? = readTagPrice(ocr)
    override fun readName(ocr: TagOcr): TagName? = readTagName(ocr)
    override fun readPack(ocr: TagOcr): TagPack? = readTagPack(ocr)
}

/**
 * Mansetin okunabilir sayildigi TEK yer - butun gramerler bunu cagiriyor.
 *
 * En buyuk glifli, rakamla baslayan satir; ama yalnizca **makul boyda** ise.
 *
 * ## Boy sarti bir olcumden geliyor
 *
 * `183808` bulanik cekildi ve OCR neredeyse hicbir sey okumadi: kose verisi
 * bozuk, yuksekliklerin cogu NEGATIF. En buyuk rakam-baslangicli parcasi 12
 * piksellik bir `86.` ve boyut suzgeci olmadan okuyucu bunu `86 TL` diye
 * donduruyordu - gurultuden uretilmis bir fiyat.
 *
 * Olculen oran: saglam cekimlerde manset kaynak yuksekligin %11'i ile %17'si
 * arasi, bulanik olanda %0,3. [MIN_LIRA_RATIO] ikisinin arasinda.
 *
 * ## RAKAMLA BASLAMA sarti da olculdu
 *
 * 27 BIM etiketinin 6'sinda en buyuk glif fiyat DEGIL, marka adi (aktuel
 * etiketlerde marka fiyattan buyuk basiliyor). Metro'da ayni isi promosyon
 * seridi yapiyor: `cOK AL` h=156, fiyat h=119. Ikisi de harfle basladigi icin
 * bu suzgecten kendiliginden dusuyor.
 *
 * Ortak olmasi sart: iki okuyucu ayni fotograf icin farkli karar verirse -
 * biri fiyat bulup digeri ad bulamazsa - kart tutarsiz dolar.
 */
internal fun TagOcr.readableLira(): OcrPiece? = lines
    .filter { it.text.trimStart().firstOrNull()?.isDigit() == true }
    .maxByOrNull { it.glyphHeight() }
    ?.takeIf { it.glyphHeight() >= sourceHeight * MIN_LIRA_RATIO }

/** Kose noktalarindan glif yuksekligi - `[0] -> [3]` kenari. */
internal fun OcrPiece.glyphHeight(): Int =
    if (corners.size < 4) 0 else corners[3].y - corners[0].y

/** Parcanin alt kenari. */
internal fun OcrPiece.bottomY(): Int = if (corners.size < 4) 0 else corners[3].y
