package com.neydi.app.data

/**
 * Kurus -> "289,00 TL".
 *
 * ONDALIK AYIRICI VIRGUL, binlik NOKTA: Turkce yazim. 1.289,90 gibi.
 * Kotlin'in sayi bicimlendirmesi commonMain'de locale bilmiyor, o yuzden elle.
 */
fun formatMinor(minor: Long, paraBirimi: String = "TL"): String {
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
        if (paraBirimi.isNotEmpty()) { append(' '); append(paraBirimi) }
    }
}
