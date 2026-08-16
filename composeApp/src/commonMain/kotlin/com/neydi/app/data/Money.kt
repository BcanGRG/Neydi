package com.neydi.app.data

/**
 * Kurus -> "289,00 TL".
 *
 * ONDALIK AYIRICI VIRGUL, binlik NOKTA: Turkce yazim. 1.289,90 gibi.
 * Kotlin'in sayi bicimlendirmesi commonMain'de locale bilmiyor, o yuzden elle.
 */
fun formatMinor(minor: Long, currency: String = "TL"): String {
    val negative = minor < 0
    val absolute = if (negative) -minor else minor
    val major = absolute / 100
    val remaining = absolute % 100

    val majorText = buildString {
        val digits = major.toString()
        digits.forEachIndexed { i, c ->
            if (i > 0 && (digits.length - i) % 3 == 0) append('.')
            append(c)
        }
    }
    val minorText = if (remaining < 10) "0$remaining" else remaining.toString()
    return buildString {
        if (negative) append('-')
        append(majorText); append(','); append(minorText)
        if (currency.isNotEmpty()) { append(' '); append(currency) }
    }
}

/**
 * OCR metnindeki tutari kurusa cevirir: "*106.00" / "x484,58" / "1.234,56".
 *
 * [parseMinorInput]'tan farki girdinin kaynagi: bu fonksiyon KAMERADAN gelen
 * metni okur, o yuzden OCR copunu tolere eder (`*`/`x` oneki) ve TAM IKI
 * ondalik hane sart kosar - iki hanesi olmayan sayi fiyat degil, miktar ya da
 * barkod parcasidir. Kullanici girdisi ise "106" yazabilir; o esneklik
 * [parseMinorInput]'ta.
 *
 * SON ayirici ondalik sayilir: "1.234,56" da "1,234.56" da 123456 kurus.
 * Fis ayristiricisindan tasindi (E2) - kural fise ozgu degil, evrensel.
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
 * Kullanicinin yazdigi tutari kurusa cevirir: "106,00" / "106.00" / "106".
 *
 * ONDALIK AYIRICI OLARAK IKISI DE KABUL: klavye virgul veriyor ama etiketin
 * kendisi nokta basiyor ve kullanici gordugunu yaziyor. Birini reddetmek
 * duzeltmeyi sessizce yutardi - ve bu ekranin butun amaci duzeltme.
 *
 * @return kurus, ya da anlasilmadiysa null. Null'i "0" saymak yazim hatasini
 *   bedava bir satira cevirirdi.
 */
fun parseMinorInput(text: String): Long? {
    val cleaned = text.trim().replace(" ", "").replace(".", ",")
    if (cleaned.isEmpty()) return null
    val negative = cleaned.startsWith("-")
    val digits = cleaned.removePrefix("-")
    val parts = digits.split(",")
    if (parts.size > 2 || parts.any { !it.all(Char::isDigit) }) return null
    val major = parts[0].ifEmpty { "0" }.toLongOrNull() ?: return null
    val minor = when (val f = parts.getOrNull(1)) {
        null, "" -> 0L
        else -> {
            // Iki haneden fazlasi tutar DEGIL: "12,345" bir yazim hatasi,
            // sessizce 12,34 yapmak kullanicinin gormedigi bir kayip olurdu.
            if (f.length > 2) return null
            f.padEnd(2, '0').toLong()
        }
    }
    val total = major * 100 + minor
    return if (negative) -total else total
}
