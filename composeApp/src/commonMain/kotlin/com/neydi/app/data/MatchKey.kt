package com.neydi.app.data

/**
 * Urun adlarini eslestirilebilir tek bir anahtara indirger.
 *
 * ISIN OZU: fisin yazdigi metinle kullanicinin yazdigi metin ayni urunu
 * gostermeli. Fis genelde ASCII ve buyuk harf ("AYCICEK YAGI 5 L"), kullanici
 * ise Turkce yaziyor ("Ayçiçek Yağı 5 L"). Ikisi de `aycicek yagi 5 l` olmali.
 *
 * NEDEN `lowercase()` DEGIL:
 * Kotlin'in `lowercase()`i locale'siz (Unicode root) calisiyor ve Turkce'de
 * yanlis sonuc veriyor:
 *
 *   "İNCİR".lowercase()  ->  i + U+0307 + n + c + i + U+0307 + r
 *
 * yani BES harf yerine SEKIZ kod noktasi; sondaki birlestirici noktalar
 * yuzunden `== "incir"` FALSE. Kullanici "İncir" yazip fis "INCIR" yazdiginda
 * uygulama bunlari iki ayri urun sanar, fiyat gecmisi ikiye boluner ve trend
 * yok olur. Hata vermez - sadece hafiza kaybolur.
 *
 * `lowercase(Locale)` JVM'e ozel; commonMain'de yok. O yuzden esleme ELLE.
 *
 * BILINCLI TAVIZ: ı ve i ayni anahtara katlaniyor. Turkce'de bunlar AYRI
 * harfler, yani "ısırgan" ile "isirgan" burada carpisir. Kabul ediyoruz:
 * fis yazicilarinin cogu Turkce karakter basmiyor ve "ISPANAK" cikiyor -
 * ayirmakta israr etmek her urunu buyuk/kucuk harf varyantlarina bolerdi.
 * Carpisma nadir; bolunme her aliverişte olurdu.
 */
private val TURKISH_FOLDING: Map<Char, Char> = mapOf(
    'İ' to 'i', 'I' to 'i', 'ı' to 'i',
    'Ş' to 's', 'ş' to 's',
    'Ğ' to 'g', 'ğ' to 'g',
    'Ü' to 'u', 'ü' to 'u',
    'Ö' to 'o', 'ö' to 'o',
    'Ç' to 'c', 'ç' to 'c',
    // Fis yazicilarinda ara sira gorulen diger aksanlar.
    'Â' to 'a', 'â' to 'a', 'Î' to 'i', 'î' to 'i', 'Û' to 'u', 'û' to 'u',
)

/**
 * @return diacritic'siz, kucuk harfli, tek boslukla ayrilmis anahtar.
 *   Bos girdi bos anahtar dondurur - cagiran taraf bunu urun olarak KAYDETMEMELI.
 */
fun matchKey(raw: String): String {
    val sb = StringBuilder(raw.length)
    var oncekiBosluk = true // bastaki bosluklari yut

    for (ch in raw) {
        val katlanmis = TURKISH_FOLDING[ch] ?: ch
        when {
            katlanmis.isLetterOrDigit() -> {
                // Kalan harfler icin lowercase() guvenli: Turkce'ye ozel olanlari
                // yukaridaki tablo zaten ele aldi.
                sb.append(katlanmis.lowercaseChar())
                oncekiBosluk = false
            }
            // Noktalama BOSLUGA cevriliyor, silinmiyor: "T.BUGDAY" -> "t bugday".
            // Silseydik "tbugday" olurdu ve "TAM BUGDAY" ile hic eslesmezdi.
            !oncekiBosluk -> {
                sb.append(' ')
                oncekiBosluk = true
            }
        }
    }
    return sb.toString().trimEnd()
}
