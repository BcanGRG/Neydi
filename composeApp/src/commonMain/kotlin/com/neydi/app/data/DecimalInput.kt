package com.neydi.app.data

/**
 * Yazilan miktar metnini TEMIZLER - rakamlar ve TEK ondalik ayirici (karar 108).
 *
 * ## Neden saf bir fonksiyon
 *
 * Alan yazilabilir oldugu an kullanicinin klavyesi bir sozlesme haline geliyor:
 * "1,,5" ya da "1.2,3" gibi seyler yazilabiliyor ve alan onlari kabul ederse
 * sayiya cevrilemez, sessizce reddederse de yazi kayboluyor gibi gorunuyor.
 * Kural burada ve sinanabilir.
 *
 * NOKTA DA KABUL EDILIYOR: Turkce'de ondalik virgulle yazilir ama klavye
 * duzenine gore nokta da cikabiliyor - [parseQuantity] ile ayni tolerans.
 * Ciktida her zaman virgul var, cunku gosterilen bicim o.
 */
fun sanitizeDecimal(raw: String): String {
    val kept = raw.filter { it.isDigit() || it == ',' || it == '.' }.replace('.', ',')
    val first = kept.indexOf(',')
    if (first < 0) return kept
    // IKINCI VE SONRAKI AYIRICILAR DUSUYOR, yazi kesilmiyor: "1,2,3" -> "1,23".
    // Kesmek, kullanicinin yazdigi rakamlari yutmus olurdu.
    return kept.take(first + 1) + kept.drop(first + 1).filter { it != ',' }
}
