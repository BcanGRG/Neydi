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
data class Miktar(
    val adet: Double,
    /** Taninan birim; yoksa null ve cagiran taraf urunun varsayilanini kullanir. */
    val birim: String?,
    val ad: String,
)

/**
 * Taninan birimler. Fis ve gunluk konusma yazimlarinin ikisi de var
 * ("gr" ve "g", "lt" ve "l"), hepsi kanonik hale esleniyor.
 */
private val BIRIMLER: Map<String, String> = mapOf(
    "kg" to "kg", "kilo" to "kg", "kilogram" to "kg",
    "g" to "g", "gr" to "g", "gram" to "g",
    "l" to "L", "lt" to "L", "litre" to "L",
    "ml" to "ml",
    "adet" to "adet", "tane" to "adet",
    "paket" to "paket", "kutu" to "kutu", "demet" to "demet", "şişe" to "şişe",
)

/**
 * Bastaki sayi + opsiyonel birim + kalan ad.
 *
 * ONDALIK AYIRICI VIRGUL: Turkce'de "1,5 kg" yazilir. Nokta da kabul ediliyor
 * cunku klavye duzenine gore ikisi de cikabiliyor.
 */
private val DESEN = Regex(
    """^\s*(\d+(?:[.,]\d+)?)\s*([\p{L}]+)?\s+(.+)$""",
)

fun miktarAyristir(girdi: String): Miktar {
    val temiz = girdi.trim()
    val eslesme = DESEN.find(temiz) ?: return Miktar(1.0, null, temiz)

    val sayi = eslesme.groupValues[1].replace(',', '.').toDoubleOrNull()
        ?: return Miktar(1.0, null, temiz)
    // Sifir ya da negatif miktar anlamsiz; ayristirma, ad kabul et.
    if (sayi <= 0.0) return Miktar(1.0, null, temiz)

    val olasiBirim = eslesme.groupValues[2].takeIf { it.isNotEmpty() }
    val kalan = eslesme.groupValues[3].trim()

    // Birim tanindiysa ayir. Taninmadiysa o kelime ADIN PARCASI:
    // "2 tam bugday ekmek" -> "tam" birim degil.
    val kanonik = olasiBirim?.let { BIRIMLER[it.lowercase()] }
    val ad = when {
        kanonik != null -> kalan
        olasiBirim == null -> kalan
        else -> "$olasiBirim $kalan"
    }

    // Geriye URUN ADI kalmadiysa ayristirma. "2 kg" yazan biri iki kilo
    // "kg" istemiyor - ortada urun yok, tum metin ad kabul edilmeli ki
    // kullanici ne yazdiysa onu gorsun.
    if (ad.isBlank() || BIRIMLER.containsKey(ad.lowercase())) return Miktar(1.0, null, temiz)

    return Miktar(sayi, kanonik, ad)
}
