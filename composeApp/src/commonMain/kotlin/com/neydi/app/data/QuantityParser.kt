package com.neydi.app.data

/**
 * "2 kg elma" -> 2 kg, "elma".
 *
 * Kullanici zaten miktari yaziyor; ayri bir adet alani acmak yazmayi
 * yavaslatir ve %90 durumda 1 yazdirir. Ayristirici o alani gereksiz kilar.
 *
 * TUTUCU DAVRANIYOR: emin olmadiginda AYRISTIRMAZ, tum metni ad kabul eder.
 * Yanlis ayristirmak, hic ayristirmamaktan kotudur - kullanici "Yumurta 10'lu"
 * yazip listede "10 adet Yumurta" gormek istemez.
 */
data class Quantity(
    val count: Double,
    /** Taninan birim; yoksa null ve cagiran taraf urunun varsayilanini kullanir. */
    val unit: String?,
    val name: String,
)

/**
 * Taninan birimler. Fis ve gunluk konusma yazimlarinin ikisi de var
 * ("gr" ve "g", "lt" ve "l"), hepsi kanonik hale esleniyor.
 */
private val UNITS: Map<String, String> = mapOf(
    "kg" to "kg", "kilo" to "kg", "kilogram" to "kg",
    "g" to "g", "gr" to "g", "gram" to "g",
    "l" to "L", "lt" to "L", "litre" to "L",
    "ml" to "ml",
    "adet" to "adet", "tane" to "adet",
    "paket" to "paket", "kutu" to "kutu", "demet" to "demet", "şişe" to "şişe",
)

/**
 * OCR'dan gelen birimi normalize eder: `Ka` gercek fiste `Kg`nin okunmus hali.
 *
 * [UNITS] ile AYNI KANON DEGIL ve bu bilerek boyle birakildi (E2): bu
 * fonksiyonun ciktilari ("gr", "lt") fis satirlarina yazilmis durumda ve bir
 * tasima adiminda davranis degistirilmez. Etiket ayristiricisi (E14) birim
 * kanonunu [UNITS] uzerinden kuracak; o gun bu fonksiyon ya ona katilir ya
 * olur.
 */
internal fun normalizeUnit(raw: String): String = when (raw.lowercase()) {
    "ka", "kg" -> "kg"
    "ad", "adet" -> "adet"
    "gr", "g" -> "gr"
    "lt", "l" -> "lt"
    "pkt", "paket" -> "paket"
    else -> raw.lowercase()
}

/**
 * Bastaki sayi + opsiyonel birim + kalan ad.
 *
 * ONDALIK AYIRICI VIRGUL: Turkce'de "1,5 kg" yazilir. Nokta da kabul ediliyor
 * cunku klavye duzenine gore ikisi de cikabiliyor.
 */
private val PATTERN = Regex(
    """^\s*(\d+(?:[.,]\d+)?)\s*([\p{L}]+)?\s+(.+)$""",
)

fun parseQuantity(input: String): Quantity {
    val cleaned = input.trim()
    val match = PATTERN.find(cleaned) ?: return Quantity(1.0, null, cleaned)

    val number = match.groupValues[1].replace(',', '.').toDoubleOrNull()
        ?: return Quantity(1.0, null, cleaned)
    // Sifir ya da negatif miktar anlamsiz; ayristirma, ad kabul et.
    if (number <= 0.0) return Quantity(1.0, null, cleaned)

    val maybeUnit = match.groupValues[2].takeIf { it.isNotEmpty() }
    val remaining = match.groupValues[3].trim()

    // Birim tanindiysa ayir. Taninmadiysa o kelime ADIN PARCASI:
    // "2 tam bugday ekmek" -> "tam" birim degil.
    val canonical = maybeUnit?.let { UNITS[it.lowercase()] }
    val name = when {
        canonical != null -> remaining
        maybeUnit == null -> remaining
        else -> "$maybeUnit $remaining"
    }

    // Geriye URUN ADI kalmadiysa ayristirma. "2 kg" yazan biri iki kilo
    // "kg" istemiyor - ortada urun yok, tum metin ad kabul edilmeli ki
    // kullanici ne yazdiysa onu gorsun.
    if (name.isBlank() || UNITS.containsKey(name.lowercase())) return Quantity(1.0, null, cleaned)

    return Quantity(number, canonical, name)
}
