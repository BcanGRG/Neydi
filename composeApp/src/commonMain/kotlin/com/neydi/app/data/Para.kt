package com.neydi.app.data

/**
 * Kurus -> "289,00 TL".
 *
 * ONDALIK AYIRICI VIRGUL, binlik NOKTA: Turkce yazim. 1.289,90 gibi.
 * Kotlin'in sayi bicimlendirmesi commonMain'de locale bilmiyor, o yuzden elle.
 */
fun kurusFormatla(kurus: Long, paraBirimi: String = "TL"): String {
    val negatif = kurus < 0
    val mutlak = if (negatif) -kurus else kurus
    val lira = mutlak / 100
    val kalan = mutlak % 100

    val liraMetni = buildString {
        val basamaklar = lira.toString()
        basamaklar.forEachIndexed { i, c ->
            if (i > 0 && (basamaklar.length - i) % 3 == 0) append('.')
            append(c)
        }
    }
    val kurusMetni = if (kalan < 10) "0$kalan" else kalan.toString()
    return buildString {
        if (negatif) append('-')
        append(liraMetni); append(','); append(kurusMetni)
        if (paraBirimi.isNotEmpty()) { append(' '); append(paraBirimi) }
    }
}
