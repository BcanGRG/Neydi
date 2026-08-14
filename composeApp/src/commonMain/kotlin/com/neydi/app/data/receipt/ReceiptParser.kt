package com.neydi.app.data.receipt

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
const val TOLERANCE_MINOR: Long = 5L

/**
 * Fisten cikan tek satir.
 *
 * `amountMinor` HER ZAMAN POZITIF; isareti [discount] bayragi tasiyor. Negatif
 * sayi ile bayragi ayni anda kullanmak toplama iki kez eksi soktururdu -
 * aritmetik kapisi da sessizce yanlis calisirdi.
 */
data class ParsedLine(
    /** Fiste yazan hali. 4.6'da gri alt satir olarak gosteriliyor - yanlis eslemeyi ancak bu geri alabilir. */
    val rawText: String,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "adet",
    val unitPriceMinor: Long? = null,
    val amountMinor: Long,
    val discount: Boolean = false,
)

/** Bir fisin tamami. [rawLines] hic dokunulmadan saklaniyor. */
data class ReceiptReading(
    val storeName: String?,
    val rows: List<ParsedLine>,
    val totalMinor: Long?,
    val rawLines: List<String>,
)

// --- Anahtar kelimeler ------------------------------------------------------
// Hepsi matchKey'den geciyor: OCR "İNDİRİM" de "INDIRIM" de basabilir ve
// locale'siz lowercase() Turkce'de bunlari ayirir (bkz. MatchKey.kt).

/** KDV dokumu URUN DEGIL. Turk perakendesinde raf fiyati zaten KDV dahil. */
private val VAT_WORDS = listOf("kdv", "topkdv")

private val TOTAL_WORDS = listOf("toplam", "genel toplam", "tutar")

private val DISCOUNT_WORDS = listOf("indirim", "iskonto", "kampanya")

/** Odeme satirlari: para hareketi, satin alinan urun degil. */
private val PAYMENT_WORDS = listOf(
    "nakit", "kredi karti", "banka karti", "para ustu", "visa", "master",
    "odenen", "kart", "pos",
)

/** Fis kunyesi. Hicbiri urun degil. */
private val HEADER_WORDS = listOf(
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
private val AMOUNT_SUFFIX = Regex("""^(.*?)\s+(-?\d{1,3}(?:\.\d{3})*,\d{2}|-?\d+,\d{2})\s*[A-Za-z*#]?$""")

/**
 * Tartili urunun IKINCI satiri: "0,850 KG x 24,90 TL/KG".
 *
 * Carpim isareti dort ayri sekilde cikiyor (x, X, *, ×) cunku OCR termal
 * yazicinin ince carpisini bazen yildiz okuyor.
 */
private val WEIGHT_LINE = Regex(
    """^\s*(\d+(?:[.,]\d+)?)\s*(KG|GR|G|LT|L|ML|ADET|AD)\s*[xX*×]\s*(\d+(?:[.,]\d+)?)\s*(?:TL)?\s*/?\s*(KG|GR|G|LT|L|ML|ADET|AD)?""",
    RegexOption.IGNORE_CASE,
)

/** Urun adinin sonundaki KDV orani isareti: "EKMEK %1" -> "EKMEK". */
private val VAT_MARK_SUFFIX = Regex("""\s*[%*#]\s*\d{1,2}\s*$""")

/**
 * Kurusa cevirir. "1.234,56" -> 123456, "12,50" -> 1250.
 *
 * Binlik NOKTA silinip ondalik VIRGUL ayrilıyor; ondalik olarak nokta
 * kullanan bir bicimi bilerek DESTEKLEMIYORUZ - Turkiye'de fis oyle basmiyor
 * ve desteklemek "1.234" sayisini 1,234 TL sanmak demek olurdu.
 */
internal fun parseMinor(text: String): Long? {
    val cleaned = text.trim().replace(".", "")
    val negative = cleaned.startsWith("-")
    val parts = cleaned.removePrefix("-").split(",")
    if (parts.size != 2 || parts[1].length != 2) return null
    val major = parts[0].toLongOrNull() ?: return null
    val minor = parts[1].toLongOrNull() ?: return null
    val total = major * 100 + minor
    return if (negative) -total else total
}

private fun containsKeyword(key: String, kelimeler: List<String>): Boolean =
    kelimeler.any { key == it || key.startsWith("$it ") || key.contains(" $it") }

/**
 * Ham OCR satirlarini fis okumasina cevirir.
 *
 * Satir sirasi ONEMLI: tartili urunun adi bir satirda, agirligi ve tutari bir
 * sonrakinde. Satirlari tek tek bagimsiz islemek tartili her urunu dusururdu.
 */
fun parseReceipt(rawLines: List<String>): ReceiptReading {
    val rows = rawLines.map { it.trim() }.filter { it.isNotBlank() }
    val parsed = mutableListOf<ParsedLine>()
    var total: Long? = null
    var store: String? = null

    var i = 0
    while (i < rows.size) {
        val raw = rows[i]
        val key = matchKey(raw)

        // Magaza adi: ilk anlamli satir. Kunye satirlari zaten eleniyor.
        if (store == null && key.isNotBlank() && !containsKeyword(key, HEADER_WORDS) &&
            AMOUNT_SUFFIX.matchEntire(raw) == null
        ) {
            store = raw
            i++
            continue
        }

        val match = AMOUNT_SUFFIX.matchEntire(raw)

        // KDV dokumunu TOPLAM'dan ONCE eliyoruz: "TOPLAM KDV" ikisini de
        // iceriyor ve once toplam diye bakarsak vergi tutarini fis toplami
        // sanardik - butun aritmetik kapisi bunun uzerine kurulu.
        if (containsKeyword(key, VAT_WORDS)) { i++; continue }

        if (containsKeyword(key, TOTAL_WORDS)) {
            match?.let { total = parseMinor(it.groupValues[2]) }
            i++
            continue
        }

        if (containsKeyword(key, PAYMENT_WORDS) || containsKeyword(key, HEADER_WORDS)) { i++; continue }

        if (containsKeyword(key, DISCOUNT_WORDS)) {
            val amount = match?.let { parseMinor(it.groupValues[2]) }
            if (amount != null) {
                parsed += ParsedLine(
                    rawText = raw,
                    name = match.groupValues[1].trim().ifBlank { "İndirim" },
                    // Isareti bayrak tasiyor, sayi degil - mutlak degere aliyoruz.
                    amountMinor = if (amount < 0) -amount else amount,
                    discount = true,
                )
            }
            i++
            continue
        }

        // Ayni satirda ad + tutar: "EKMEK %1 5,00"
        if (match != null) {
            val amount = parseMinor(match.groupValues[2])
            val name = match.groupValues[1].replace(VAT_MARK_SUFFIX, "").trim()
            if (amount != null && name.isNotBlank()) {
                parsed += ParsedLine(rawText = raw, name = name, amountMinor = amount)
            }
            i++
            continue
        }

        // Tutarsiz satir: TARTILI URUNUN ADI olabilir. Bir sonrakine bak.
        val next = rows.getOrNull(i + 1)
        val weight = next?.let { WEIGHT_LINE.find(it) }
        if (weight != null) {
            val nextAmount = AMOUNT_SUFFIX.matchEntire(next)?.let { parseMinor(it.groupValues[2]) }
            val quantity = weight.groupValues[1].replace(",", ".").toDoubleOrNull()
            val unitPrice = parseMinor(weight.groupValues[3])
            val name = raw.replace(VAT_MARK_SUFFIX, "").trim()
            if (nextAmount != null && quantity != null && name.isNotBlank()) {
                parsed += ParsedLine(
                    rawText = "$raw / $next",
                    name = name,
                    quantity = quantity,
                    unit = weight.groupValues[2].lowercase(),
                    unitPriceMinor = unitPrice,
                    amountMinor = nextAmount,
                )
                i += 2
                continue
            }
        }

        i++
    }

    return ReceiptReading(
        storeName = store,
        rows = parsed,
        totalMinor = total,
        rawLines = rawLines,
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
fun arithmeticHolds(reading: ReceiptReading): Boolean? {
    val total = reading.totalMinor ?: return null
    val computed = reading.rows.sumOf { if (it.discount) -it.amountMinor else it.amountMinor }
    val diff = computed - total
    return (if (diff < 0) -diff else diff) <= TOLERANCE_MINOR
}
