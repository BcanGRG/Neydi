package com.neydi.app.data.fis

import com.neydi.app.data.matchKey

/**
 * Ham OCR metninden Turkce market fisi ayristirici.
 *
 * NEDEN CIHAZDA VE ELDE YAZILMIS:
 * Bir dil modeline fis okutmak zincire gore degisen duzenleri daha iyi
 * soguruyor - ama fotografi cihazdan cikarmayi, API anahtari tasimayi ve
 * her cagriyi odemeyi gerektiriyordu. Fis KISISEL VERI; telefondan hic
 * cikmamasi bir tercih degil, mimari bir kazanc. Ustelik bu ayristirici saf
 * Kotlin oldugu icin cihazsiz, ucretsiz ve hizli test edilebiliyor.
 *
 * TAVIZ ACIK: elde yazilmis kurallar BIM/A101/SOK/Migros'un farkli duzenlerini
 * bir modelin sogurdugu kadar soguramaz. Bunu bilerek kabul ediyoruz cunku
 * yanlis ayristirma SESSIZCE gecmiyor: F4.5 aritmetik kapisi satirlarin
 * toplami fis toplamini tutmuyorsa fisi TUTARSIZ isaretliyor ve kullanici
 * onayina dusuruyor. Kotu okuma, sessiz veri bozulmasi degil, gorunur bir is.
 */

/** Aritmetik kapinin toleransi. Tartili urun yuvarlamasi bu kadar oynatabilir. */
const val TOLERANS_KURUS: Long = 5L

/**
 * Fisten cikan tek satir.
 *
 * `tutarKurus` HER ZAMAN POZITIF; isareti [indirim] bayragi tasiyor. Negatif
 * sayi ile bayragi ayni anda kullanmak toplama iki kez eksi soktururdu -
 * aritmetik kapisi da sessizce yanlis calisirdi.
 */
data class FisSatiri(
    /** Fiste yazan hali. 4.6'da gri alt satir olarak gosteriliyor - yanlis eslemeyi ancak bu geri alabilir. */
    val hamMetin: String,
    val ad: String,
    val miktar: Double = 1.0,
    val birim: String = "adet",
    val birimFiyatKurus: Long? = null,
    val tutarKurus: Long,
    val indirim: Boolean = false,
)

/** Bir fisin tamami. [hamSatirlar] hic dokunulmadan saklaniyor. */
data class FisOkumasi(
    val magazaAdi: String?,
    val satirlar: List<FisSatiri>,
    val toplamKurus: Long?,
    val hamSatirlar: List<String>,
)

// --- Anahtar kelimeler ------------------------------------------------------
// Hepsi matchKey'den geciyor: OCR "İNDİRİM" de "INDIRIM" de basabilir ve
// locale'siz lowercase() Turkce'de bunlari ayirir (bkz. MatchKey.kt).

/** KDV dokumu URUN DEGIL. Turk perakendesinde raf fiyati zaten KDV dahil. */
private val KDV_KELIMELERI = listOf("kdv", "topkdv")

private val TOPLAM_KELIMELERI = listOf("toplam", "genel toplam", "tutar")

private val INDIRIM_KELIMELERI = listOf("indirim", "iskonto", "kampanya")

/** Odeme satirlari: para hareketi, satin alinan urun degil. */
private val ODEME_KELIMELERI = listOf(
    "nakit", "kredi karti", "banka karti", "para ustu", "visa", "master",
    "odenen", "kart", "pos",
)

/** Fis kunyesi. Hicbiri urun degil. */
private val KUNYE_KELIMELERI = listOf(
    "fis no", "tarih", "saat", "eku", "z no", "mersis", "vergi dairesi",
    "vno", "tesekkur", "musteri", "kasiyer", "kasa", "belge", "sube",
    "tel", "adres", "www", "mah", "cad", "sok",
)

// --- Bicim tanimlari --------------------------------------------------------

/**
 * Satirin SONUNDAKI para tutari.
 *
 * Binlik nokta OPSIYONEL cunku fisler ikisini de basiyor: "1.234,56" ve
 * "1234,56". Kurus HANESI ZORUNLU (`,\d{2}`) - bu sayede "%1" ya da "0,850 KG"
 * gibi paradan baska sayilar tutar sanilmiyor.
 */
private val TUTAR_SONDA = Regex("""^(.*?)\s+(-?\d{1,3}(?:\.\d{3})*,\d{2}|-?\d+,\d{2})\s*[A-Za-z*#]?$""")

/**
 * Tartili urunun IKINCI satiri: "0,850 KG x 24,90 TL/KG".
 *
 * Carpim isareti dort ayri sekilde cikiyor (x, X, *, ×) cunku OCR termal
 * yazicinin ince carpisini bazen yildiz okuyor.
 */
private val AGIRLIK_SATIRI = Regex(
    """^\s*(\d+(?:[.,]\d+)?)\s*(KG|GR|G|LT|L|ML|ADET|AD)\s*[xX*×]\s*(\d+(?:[.,]\d+)?)\s*(?:TL)?\s*/?\s*(KG|GR|G|LT|L|ML|ADET|AD)?""",
    RegexOption.IGNORE_CASE,
)

/** Urun adinin sonundaki KDV orani isareti: "EKMEK %1" -> "EKMEK". */
private val KDV_ISARETI_SONDA = Regex("""\s*[%*#]\s*\d{1,2}\s*$""")

/**
 * Kurusa cevirir. "1.234,56" -> 123456, "12,50" -> 1250.
 *
 * Binlik NOKTA silinip ondalik VIRGUL ayrilıyor; ondalik olarak nokta
 * kullanan bir bicimi bilerek DESTEKLEMIYORUZ - Turkiye'de fis oyle basmiyor
 * ve desteklemek "1.234" sayisini 1,234 TL sanmak demek olurdu.
 */
internal fun paraKurusa(metin: String): Long? {
    val temiz = metin.trim().replace(".", "")
    val negatif = temiz.startsWith("-")
    val parcalar = temiz.removePrefix("-").split(",")
    if (parcalar.size != 2 || parcalar[1].length != 2) return null
    val lira = parcalar[0].toLongOrNull() ?: return null
    val kurus = parcalar[1].toLongOrNull() ?: return null
    val toplam = lira * 100 + kurus
    return if (negatif) -toplam else toplam
}

private fun icerirMi(anahtar: String, kelimeler: List<String>): Boolean =
    kelimeler.any { anahtar == it || anahtar.startsWith("$it ") || anahtar.contains(" $it") }

/**
 * Ham OCR satirlarini fis okumasina cevirir.
 *
 * Satir sirasi ONEMLI: tartili urunun adi bir satirda, agirligi ve tutari bir
 * sonrakinde. Satirlari tek tek bagimsiz islemek tartili her urunu dusururdu.
 */
fun fisAyristir(hamSatirlar: List<String>): FisOkumasi {
    val satirlar = hamSatirlar.map { it.trim() }.filter { it.isNotBlank() }
    val cikan = mutableListOf<FisSatiri>()
    var toplam: Long? = null
    var magaza: String? = null

    var i = 0
    while (i < satirlar.size) {
        val ham = satirlar[i]
        val anahtar = matchKey(ham)

        // Magaza adi: ilk anlamli satir. Kunye satirlari zaten eleniyor.
        if (magaza == null && anahtar.isNotBlank() && !icerirMi(anahtar, KUNYE_KELIMELERI) &&
            TUTAR_SONDA.matchEntire(ham) == null
        ) {
            magaza = ham
            i++
            continue
        }

        val eslesme = TUTAR_SONDA.matchEntire(ham)

        // KDV dokumunu TOPLAM'dan ONCE eliyoruz: "TOPLAM KDV" ikisini de
        // iceriyor ve once toplam diye bakarsak vergi tutarini fis toplami
        // sanardik - butun aritmetik kapisi bunun uzerine kurulu.
        if (icerirMi(anahtar, KDV_KELIMELERI)) { i++; continue }

        if (icerirMi(anahtar, TOPLAM_KELIMELERI)) {
            eslesme?.let { toplam = paraKurusa(it.groupValues[2]) }
            i++
            continue
        }

        if (icerirMi(anahtar, ODEME_KELIMELERI) || icerirMi(anahtar, KUNYE_KELIMELERI)) { i++; continue }

        if (icerirMi(anahtar, INDIRIM_KELIMELERI)) {
            val tutar = eslesme?.let { paraKurusa(it.groupValues[2]) }
            if (tutar != null) {
                cikan += FisSatiri(
                    hamMetin = ham,
                    ad = eslesme.groupValues[1].trim().ifBlank { "İndirim" },
                    // Isareti bayrak tasiyor, sayi degil - mutlak degere aliyoruz.
                    tutarKurus = if (tutar < 0) -tutar else tutar,
                    indirim = true,
                )
            }
            i++
            continue
        }

        // Ayni satirda ad + tutar: "EKMEK %1 5,00"
        if (eslesme != null) {
            val tutar = paraKurusa(eslesme.groupValues[2])
            val ad = eslesme.groupValues[1].replace(KDV_ISARETI_SONDA, "").trim()
            if (tutar != null && ad.isNotBlank()) {
                cikan += FisSatiri(hamMetin = ham, ad = ad, tutarKurus = tutar)
            }
            i++
            continue
        }

        // Tutarsiz satir: TARTILI URUNUN ADI olabilir. Bir sonrakine bak.
        val sonraki = satirlar.getOrNull(i + 1)
        val agirlik = sonraki?.let { AGIRLIK_SATIRI.find(it) }
        if (agirlik != null) {
            val sonrakiTutar = TUTAR_SONDA.matchEntire(sonraki)?.let { paraKurusa(it.groupValues[2]) }
            val miktar = agirlik.groupValues[1].replace(",", ".").toDoubleOrNull()
            val birimFiyat = paraKurusa(agirlik.groupValues[3])
            val ad = ham.replace(KDV_ISARETI_SONDA, "").trim()
            if (sonrakiTutar != null && miktar != null && ad.isNotBlank()) {
                cikan += FisSatiri(
                    hamMetin = "$ham / $sonraki",
                    ad = ad,
                    miktar = miktar,
                    birim = agirlik.groupValues[2].lowercase(),
                    birimFiyatKurus = birimFiyat,
                    tutarKurus = sonrakiTutar,
                )
                i += 2
                continue
            }
        }

        i++
    }

    return FisOkumasi(
        magazaAdi = magaza,
        satirlar = cikan,
        toplamKurus = toplam,
        hamSatirlar = hamSatirlar,
    )
}

/**
 * F4.5 ARITMETIK KAPISI: Sigma(urun) - Sigma(indirim) = TOPLAM, +/- 5 kurus.
 *
 * KDV EKLENMEZ VE CIKARILMAZ. Arastirmanin ilk yazdigi `+KDV` formulu
 * yanlisti: Turkiye'de perakende fiyatlari kanunen KDV DAHIL, TOPKDV ise
 * toplamin icindeki verginin dokumu. O formul her fisi manuel duzeltmeye
 * yollardi.
 *
 * Bu kapi ML Kit yolunun emniyet supabi: elde yazilmis ayristirici bir
 * satiri kacirdiginda ya da yanlis okudugunda toplam TUTMAZ ve fis
 * kullanici onayina duser. Sessiz veri bozulmasi olmuyor.
 *
 * @return toplam bilinmiyorsa null - "dogrulanamadi" ile "tutmadi" ayri seyler.
 */
fun aritmetikTutuyorMu(okuma: FisOkumasi): Boolean? {
    val toplam = okuma.toplamKurus ?: return null
    val hesaplanan = okuma.satirlar.sumOf { if (it.indirim) -it.tutarKurus else it.tutarKurus }
    val fark = hesaplanan - toplam
    return (if (fark < 0) -fark else fark) <= TOLERANS_KURUS
}
