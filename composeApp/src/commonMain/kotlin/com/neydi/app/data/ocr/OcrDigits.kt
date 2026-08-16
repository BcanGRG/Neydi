package com.neydi.app.data.ocr

/**
 * OCR'in harfe cevirdigi rakamlari geri alir.
 *
 * Gercek fiste goruldu: `S0` (50), `869O508101426` (sifir yerine harf O).
 * Rakam karisikligi OCR'in evrensel huyu - fis doneminden etiket donemine
 * degismeden tasindi (E2). SADECE rakam beklenen metne uygulanir; ada
 * uygulamak "Iskender"i "1skender" yapar.
 */
internal fun normalizeDigits(raw: String): String = raw.map { char ->
    when (char) {
        'O', 'o' -> '0'
        'S', 's' -> '5'
        'I', 'l', 'i' -> '1'
        else -> char
    }
}.joinToString("")
