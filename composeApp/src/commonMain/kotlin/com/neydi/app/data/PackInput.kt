package com.neydi.app.data

/**
 * Kullanicinin elle yazdigi ambalaj: `"3 kg"` -> 3.0 + `"kg"` (karar 111).
 *
 * ## Neden `parseQuantity` kullanilmiyor
 *
 * O ayristirici bir AD bekliyor - kaliplarindaki son grup *"2 kg elma"*daki
 * "elma". Burada ad yok ve olmasi da gerekmiyor; kullanici yalnizca bir olcu
 * yaziyor. Ayni fonksiyonu zorlamak, kaliba sahte bir kelime eklemek olurdu.
 *
 * KANON [UnitScale]'DEN: `gr` ile `g`, `lt` ile `L` ayni sey sayiliyor ve
 * yazilan hal oldugu gibi saklaniyor. Donusturup "duzeltmek", kullanicinin
 * yazdigi seyi degistirmek olurdu - ve zaten okuma tarafi ikisini de taniyor.
 *
 * `null` DONER: sayi yoksa, sifir/negatifse, ya da birim taninmiyorsa.
 * Yaniltici bir varsayim yerine sessiz reddetme: yanlis bir ambalaj, hic
 * ambalaj olmamasindan kotu - cunku toplama GIRER ve yanlis bir sayi uretir.
 */
fun parsePack(text: String): Pair<Double, String>? {
    val cleaned = text.trim()
    val match = PACK_PATTERN.find(cleaned) ?: return null
    val size = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
    if (size <= 0.0) return null
    val unit = match.groupValues[2].takeIf { it.isNotBlank() } ?: return null
    // OLCEK BIRIMI SART: "3 adet"lik bir ambalaj, tartili satirin bolenine
    // cevrilemez ve karar 111'e gore satir yine ambalajsiz sayilirdi. Burada
    // reddetmek, kullaniciya yazdigi seyin ise yaramadigini SESSIZCE degil
    // ANINDA soyluyor - alan dolmuyor.
    val kind = unitKind(unit) ?: return null
    if (kind == UnitKind.COUNT) return null
    return size to unit
}

/** Bastaki sayi + birim; arada bosluk olabilir de olmayabilir de ("3kg", "1,5 lt"). */
private val PACK_PATTERN = Regex("""^(\d+(?:[.,]\d+)?)\s*([\p{L}]+)$""")
