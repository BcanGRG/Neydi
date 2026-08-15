package com.neydi.app.data.receipt

import com.neydi.app.data.matchKey

/**
 * Turkce market fisi ayristirici. GERCEK fis cikitisina gore yazildi.
 *
 * ONCEKI HALI SENTETIKTI VE HEPSI YANLISTI. Kendi yazdigim ornek fislere gore
 * kurmustum ve 17 test gecmisti; iki gercek fis (BIM ve File Market) uc
 * varsayimi birden curuttu:
 *
 *   - ondalik ayirici olarak NOKTA kullaniliyor. Ben noktayi acikca reddetmis
 *     ve gerekce olarak "Turkiye'de fis oyle basmiyor" yazmistim. Iki zincirin
 *     ikisi de nokta basiyor; o tek varsayim butun tutarlarin okunmamasi demekti.
 *   - toplam satiri "Odenecek KDV Dahil Tutar" ve icinde KDV GECIYOR. Ben
 *     KDV'yi TOPLAM'dan once eliyordum - hem de bir tuzagi onlemek icin
 *     bilincli olarak - ve bu, gercek toplam satirini eliyordu.
 *   - miktar satiri ayri bir satir ve ait oldugu urunden ONCE geliyor.
 *
 * Alinan ders yontemle ilgili: sentetik ornek yazmak, kendi kurallarimi kendime
 * onaylatmakti. Bu dosyanin testleri artik gercek fislerin OCR CIKTISINDAN
 * geliyor (bkz. ReceiptParserTest).
 */

/** Aritmetik kapinin toleransi. Tartili urun yuvarlamasi bu kadar oynatabilir. */
const val TOLERANCE_MINOR: Long = 5L

/**
 * Fisten cikan tek satir.
 *
 * `amountMinor` HER ZAMAN POZITIF; isareti [discount] tasiyor. Negatif sayi ile
 * bayragi ayni anda kullanmak toplama iki kez eksi soktururdu.
 */
data class ParsedLine(
    /** Fiste yazan hali. 4.6'da gri alt satir olarak gosteriliyor. */
    val rawText: String,
    val name: String,
    val count: Double = 1.0,
    val unit: String = "adet",
    val unitPriceMinor: Long? = null,
    val amountMinor: Long,
    val discount: Boolean = false,
)

/** Bir fisin tamami. [rawLines] hic dokunulmadan saklaniyor. */
data class ReceiptReading(
    val storeName: String?,
    val lines: List<ParsedLine>,
    val totalMinor: Long?,
    /**
     * Fisin BASILI tarihi, epoch millis (F5.8). Null = okunamadi.
     *
     * Fiyat gecmisinin `observedAt`'i bunu kullanmali - fotografin cekildigi
     * an degil. Eski fisleri tek gunde toplu cekmek, tarih olmadan butun
     * gozlemleri ayni x degerine yigar ve grafigi tek noktaya duzler.
     */
    val receiptDate: Long? = null,
    val rawLines: List<String>,
)

// --- Anahtar kelimeler ------------------------------------------------------
// Hepsi matchKey'den geciyor: OCR "İNDİRİM" de "INDIRIM" de basiyor ve
// locale'siz lowercase() Turkce'de bunlari ayirir (bkz. MatchKey.kt).

/**
 * TOPLAM SATIRI. Gercek fiste "Odenecek KDV Dahil Tutar" yaziyor.
 *
 * KDV kelimelerinden ONCE bakilmasi ZORUNLU: bu satir "KDV" iceriyor ve once
 * vergi satiri diye elenirse fisin toplami HIC bulunamaz - butun aritmetik
 * kapisi o sayinin uzerine kurulu. Onceki surum tam bu hatayi yapiyordu.
 */
private val TOTAL_WORDS = listOf("odenecek", "dahil tutar", "genel toplam")

/** KDV dokumu URUN DEGIL. Turk perakendesinde raf fiyati zaten KDV dahil. */
private val VAT_WORDS = listOf("toplam kdv", "kdv matrah", "kdv tutar", "kdv dahil")

private val DISCOUNT_WORDS = listOf("indirim", "iskonto", "kampanya")

/** Odeme satirlari: para hareketi, satin alinan urun degil. */
private val PAYMENT_WORDS = listOf(
    "kredi karti", "banka karti", "nakit", "para ustu", "pos", "bankasi",
    "visa", "master", "odenen",
)

/** Fis kunyesi. Hicbiri urun degil. */
/**
 * Sirket/kunye satirini adres satirindan ayiran kelimeler.
 *
 * Bunlar HEADER_WORDS'te de var (kunye gurultusu olarak) - burada ters yonde
 * kullaniliyorlar: magaza adini secerken sirket satirinin ISARETI oluyorlar.
 */
private val COMPANY_WORDS = listOf("magazacilik", "magazalar", "market", "sirket", "a s", "ticaret")

private val HEADER_WORDS = listOf(
    "fatura", "sira no", "tckn", "vkn", "ettn", "vdm", "onay no", "ref no",
    "tesekkur", "musteri", "kasiyer", "kasa", "mah", "cad", "sok", "bulvari",
    "mahallesi", "tuketici", "magazalar", "magazacilik", "market", "sirket",
    "mukellefler", "arsiv",
)

// --- Bicim tanimlari --------------------------------------------------------

/**
 * Satirin SONUNDAKI para tutari.
 *
 * ONEK TEK KARAKTER VE SERBEST: gercek fiste `*106.00`, `x484.58` ve cift
 * onekli olmayan `4.80` uc hali de goruldu - OCR yildizi bazen `x` okuyor,
 * bazen hic okumuyor. Oneki zorunlu tutmak ya da yalnizca `*` kabul etmek
 * satirlarin bir kismini dusururdu.
 *
 * Ondalik hane ZORUNLU (iki basamak): bu sayede `%1.` ya da `2 ad` gibi
 * paradan baska sayilar tutar sanilmiyor.
 */
private val AMOUNT_SUFFIX =
    Regex("""^(.*?)[\s*xX×]*(\d{1,3}(?:[.,]\d{3})*[.,]\d{2})\s*$""")

/**
 * MIKTAR SATIRI: "2 ad X 53.00", "0.182 kg X 690.00", "2 ad % 25.50".
 *
 * Ait oldugu urunden ONCE geliyor - gercek fiste olculdu, ve iki zincirde de
 * boyle. Onceki surum bunu tersine kurmustu (adin ARDINDAN agirlik satiri
 * bekliyordu) ve hicbir tartili urunu yakalayamazdi.
 *
 * Carpim isareti `%` de olabiliyor: OCR ince carpiyi boyle okuyor.
 */
private val QUANTITY_LINE = Regex(
    """^\s*(\d+(?:[.,]\d+)?)\s*(ad|adet|kg|gr|g|lt|l|ml)\s*[xX*%×]\s*[*xX×]?\s*(\d+(?:[.,]\d+)?)\s*$""",
    RegexOption.IGNORE_CASE,
)

/**
 * Urun adinin sonundaki KDV orani cop'u: "%1.", "21.", "220".
 *
 * OCR yuzde isaretini guvenilmez okuyor - gercek fiste `%1.`, `21.`, `Z1.` ve
 * `%20` yerine `220` goruldu. O yuzden isareti ARAMIYORUZ, adin sonundaki
 * kisa sayisal copu atiyoruz. Uc haneye kadar: `220` da gecmeli.
 */
private val VAT_MARK_SUFFIX = Regex("""[\s%*#]*[%A-Z]?\d{1,3}\s*[.,]?\s*$""")

/**
 * Kurusa cevirir. Hem NOKTA hem VIRGUL ondalik kabul ediyor.
 *
 * Ayrimi son ayiricinin ARDINDAN KAC BASAMAK geldigine bakarak yapiyor: iki
 * basamak varsa o ayirici ondalik, digerleri binlik. Boylece `106.00`, `12,50`,
 * `1.234,56` ve `1,234.56` dordu de dogru cozuluyor.
 *
 * Ilk surum yalnizca virgul kabul ediyordu ve gerekce olarak "Turkiye'de fis
 * nokta basmiyor" yazilmisti. Yanlisti; BIM ve File Market nokta basiyor.
 */
internal fun parseMinor(text: String): Long? {
    val clean = text.trim().trimStart('*', 'x', 'X', '×', ' ')
    val negative = clean.startsWith("-")
    val body = clean.removePrefix("-")
    val lastDot = body.lastIndexOf('.')
    val lastComma = body.lastIndexOf(',')
    val sep = maxOf(lastDot, lastComma)
    if (sep < 0) return null
    val fraction = body.substring(sep + 1)
    if (fraction.length != 2 || fraction.any { !it.isDigit() }) return null
    val whole = body.substring(0, sep).filter { it.isDigit() }
    if (whole.isEmpty()) return null
    val major = whole.toLongOrNull() ?: return null
    val minor = fraction.toLongOrNull() ?: return null
    val total = major * 100 + minor
    return if (negative) -total else total
}

/**
 * Anahtar kelimeyi TAM KELIME olarak arar, alt dizgi olarak DEGIL.
 *
 * Onek eslesmesi gercek fiste bir urunu yok etti: "ALISVERIS POSETI BIM"
 * matchKey'den "alisveris poseti bim" olarak cikiyor ve " pos" alt dizgisi
 * " poseti" icinde bulundugu icin satir ODEME satiri sanilip eleniyordu -
 * poset (1,00 TL) toplamdan dusuyor ve aritmetik kapisi haksiz yere tutmuyordu.
 *
 * Iki yandan bosluklu arama bunu keser: " pos " artik " poseti " ile
 * eslesmiyor. Cok kelimeli kaliplar ("kredi karti") ayni bicimde calisiyor.
 */
private fun contains(key: String, words: List<String>): Boolean {
    val padded = " $key "
    return words.any { padded.contains(" $it ") }
}

/**
 * Gruplanmis OCR satirlarini fis okumasina cevirir.
 *
 * Girdinin GORSEL SATIRLARA gruplanmis olmasi sart - aciklama ve tutar ayni
 * satirda. Ham ML Kit ciktisi bunu vermiyor; gruplama platform tarafinda
 * yapiliyor (bkz. ReceiptReader.android.kt), cunku Y/X geometrisi orada.
 */
fun parseReceipt(rawLines: List<String>): ReceiptReading {
    val lines = rawLines.map { it.trim() }.filter { it.isNotBlank() }
    val out = mutableListOf<ParsedLine>()
    var total: Long? = null
    var storeCompany: String? = null
    var storeFallback: String? = null
    // Miktar satiri urunden ONCE geldigi icin bekletiliyor.
    var pendingCount: Double? = null
    var pendingUnit: String? = null
    var pendingUnitPrice: Long? = null

    var receiptDate: Long? = null

    for (raw in lines) {
        val key = matchKey(raw)
        // TARIH SATIRI MAGAZA ADI OLAMAZ. Yedek aday secimi bunu bilmek
        // zorunda: kunye satiri okunamadiginda ("13.08.2026 18:49 Sira No :
        // 218") satiri yedege giriyordu ve karar 11'den sonra bu KALICI bir
        // hataya donusuyor - Ayarlar'daki Magazalar bolumune "Sira" adinda
        // bir zincir yaziliyor ve onu silecek bir yuzey yok.
        val isDateLine = RECEIPT_DATE.containsMatchIn(raw)

        // FISIN BASILI TARIHI (F5.8): "13.08.2026 18:49" gibi. Gg.aa.yyyy
        // bicimi Turk fislerinde standart; iki gercek fiste de bu bicim.
        // Ilk eslesen kazaniyor - fisin ust kunyesindeki tarih islem tarihi,
        // alt kisimdaki POS satirlarindaki tekrarlar degil.
        if (receiptDate == null) {
            RECEIPT_DATE.find(raw)?.let { m ->
                receiptDate = parseReceiptDate(
                    day = m.groupValues[1].toInt(),
                    month = m.groupValues[2].toInt(),
                    year = m.groupValues[3].toInt(),
                    hour = m.groupValues[4].toIntOrNull() ?: 12,
                    minute = m.groupValues[5].toIntOrNull() ?: 0,
                )
            }
        }

        // Magaza adi: SIRKET satiri adres satirina TERCIH ediliyor.
        //
        // Eski kural "ilk anlamli satir"di ve gercek File fisinde ADRESI
        // yakaliyordu: "FiLE OVACIK / KEC1OREN/ ANKARA" sirket satirindan
        // ("FiLE MARKET MAGAZACILIK ANONIM SIRKETI") once geliyor. Cihazda Fis
        // Kontrol basliginda adres gorunuyordu ve Store.name adres olacakti.
        //
        // Simdi: sirket iseareti tasiyan satir ("magazacilik", "magazalar",
        // "market", "a s", "sirket") gorulene kadar ilk aday YEDEKTE tutuluyor;
        // kunye satiri gelirse o kazaniyor, hic gelmezse yedek kullaniliyor.
        // chainKey ikisinden de ayni zinciri urettigi icin alias'lar etkilenmez;
        // degisen yalnizca gorunen ad.
        if (storeCompany == null && key.isNotBlank() && raw.length > 6 &&
            AMOUNT_SUFFIX.matchEntire(raw) == null &&
            !contains(key, listOf("arsiv", "fatura"))
        ) {
            if (contains(key, COMPANY_WORDS)) {
                storeCompany = raw
            } else if (storeFallback == null && !isDateLine) {
                storeFallback = raw
            }
            continue
        }

        QUANTITY_LINE.matchEntire(raw)?.let { m ->
            pendingCount = m.groupValues[1].replace(",", ".").toDoubleOrNull()
            pendingUnit = m.groupValues[2].lowercase()
            pendingUnitPrice = parseMinor(m.groupValues[3])
            return@let
        }
        if (QUANTITY_LINE.matchEntire(raw) != null) continue

        val match = AMOUNT_SUFFIX.matchEntire(raw)
        val amount = match?.let { parseMinor(it.groupValues[2]) }

        // TOPLAM, KDV'DEN ONCE. Toplam satiri "Odenecek KDV Dahil Tutar" ve
        // icinde KDV geciyor; ters sirada elenip toplam hic bulunamiyordu.
        if (contains(key, TOTAL_WORDS)) {
            if (amount != null) total = amount
            continue
        }
        if (contains(key, VAT_WORDS)) continue
        if (contains(key, PAYMENT_WORDS) || contains(key, HEADER_WORDS)) continue

        if (contains(key, DISCOUNT_WORDS)) {
            if (amount != null) {
                out += ParsedLine(
                    rawText = raw,
                    name = match!!.groupValues[1].trim().ifBlank { "İndirim" },
                    // Isareti bayrak tasiyor, sayi degil.
                    amountMinor = if (amount < 0) -amount else amount,
                    discount = true,
                )
            }
            continue
        }

        if (amount != null) {
            val name = match!!.groupValues[1]
                .replace(VAT_MARK_SUFFIX, "")
                .trim()
            if (name.isNotBlank()) {
                out += ParsedLine(
                    rawText = raw,
                    name = name,
                    count = pendingCount ?: 1.0,
                    unit = pendingUnit ?: "adet",
                    unitPriceMinor = pendingUnitPrice,
                    amountMinor = amount,
                )
            }
            pendingCount = null
            pendingUnit = null
            pendingUnitPrice = null
        }
    }

    return ReceiptReading(
        storeName = storeCompany ?: storeFallback,
        lines = out,
        totalMinor = total,
        rawLines = rawLines,
        receiptDate = receiptDate,
    )
}

/**
 * F4.5 ARITMETIK KAPISI: Sigma(urun) - Sigma(indirim) = TOPLAM, +/- 5 kurus.
 *
 * KDV EKLENMEZ VE CIKARILMAZ - ve bu GERCEK FISLE DOGRULANDI. Iki fisin ikisinde
 * de satirlarin toplami "Odenecek KDV Dahil Tutar" ile birebir tutuyor (225,50
 * ve 484,58), "TOPLAM KDV" ise o tutarin ICINDEKI verginin dokumu. Arastirmanin
 * ilk yazdigi `+KDV` formulu her fisi manuel duzeltmeye yollardi.
 *
 * @return toplam okunamadiysa null - "dogrulanamadi" ile "tutmadi" ayri seyler.
 */
fun arithmeticHolds(reading: ReceiptReading): Boolean? {
    val total = reading.totalMinor ?: return null
    val computed = reading.lines.sumOf { if (it.discount) -it.amountMinor else it.amountMinor }
    val diff = computed - total
    return (if (diff < 0) -diff else diff) <= TOLERANCE_MINOR
}

/**
 * Fis tarihi: `13.08.2026 18:49` ya da saatsiz `13.08.2026`.
 *
 * Yil DORT hane sart: "13.08.26" gibi kisaltmalar POS referans numaralarinin
 * icinde de gorunebiliyor ve yanlis pozitif uretir.
 */
private val RECEIPT_DATE = Regex("""\b(\d{2})\.(\d{2})\.(\d{4})(?:\s+(\d{2}):(\d{2}))?""")

/**
 * Basili tarihi epoch millis'e cevirir - CIHAZIN saat diliminde.
 *
 * Fis yerel saatte basiliyor; UTC varsaymak Istanbul'da 3 saatlik kayma olurdu
 * ve gun sinirindaki alisverisler yanlis gune duserdi (bkz. `daysBetween`
 * gerekcesi).
 *
 * @return millis, ya da tarih gecersizse (ay 13, gun 32...) null - bozuk OCR
 *   tarihinden uydurma bir damga uretmek en kotusu olurdu.
 */
internal fun parseReceiptDate(day: Int, month: Int, year: Int, hour: Int, minute: Int): Long? {
    if (month !in 1..12 || day !in 1..31 || year !in 2000..2100) return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return try {
        with(kotlinx.datetime.TimeZone.currentSystemDefault()) {
            kotlinx.datetime.LocalDateTime(year, month, day, hour, minute)
                .toInstant()
                .toEpochMilliseconds()
        }
    } catch (_: IllegalArgumentException) {
        // 31 Nisan gibi takvimde olmayan gun.
        null
    }
}
