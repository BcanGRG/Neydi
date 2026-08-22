package com.neydi.app.data

/**
 * Turkce BULUNMA HALI eki: "A101" -> "A101'de", "SOK" -> "SOK'ta".
 *
 * ## Neden elle yazilmis bir ek, hazir bir kutuphane degil
 *
 * Market adi kullanicinin yazdigi herhangi bir dizgi olabilir - tohumdaki yedi
 * zincir degil sadece. "Migros'te" ya da "SOK'de" yazan bir uygulama Turkce
 * bilmiyor demektir ve bunu her satirda tekrarlar. Kural iki adimda bitiyor,
 * yani bedeli de kucuk.
 *
 * ## Iki kural
 *
 * 1. **Unlu uyumu** son UNLUDEN: `a i o u` kalin -> `-da`, `e i o u` ince ->
 *    `-de`.
 * 2. **Unsuz benzesmesi** son HARFTEN: sert unsuzden sonra `d` sertlesip `t`
 *    oluyor (`fstkcshp`).
 *
 * ## Rakamla biten adlar
 *
 * "A101"in eki okunusundan cikiyor: *"yuz bir"* -> son unlu `i`, son harf `r`
 * -> **A101'de**. Tasarimin maketi de birebir boyle yaziyor, yani bu satir
 * hem kuralin hem maketin sinavi. Son RAKAMIN okunusu yetiyor; onceki
 * basamaklar eki degistirmiyor.
 *
 * ## Kesme isareti
 *
 * Ozel ada gelen cekim eki kesme ile ayrilir - market adi ozel addir.
 *
 * BUYUK/KUCUK HARF CEVIRIMI YOK: proje kurali locale'siz cevirimi yasakliyor
 * (`I/i/I/i` bozulur). Onun yerine harf kumeleri iki hali de sayiyor.
 */
fun turkishLocative(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return trimmed
    val last = trimmed.last()
    val (back, voiceless) = if (last.isDigit()) {
        digitEnding(last)
    } else {
        lastVowelIsBack(trimmed) to (last in VOICELESS)
    }
    val consonant = if (voiceless) 't' else 'd'
    val vowel = if (back) 'a' else 'e'
    return "$trimmed'$consonant$vowel"
}

/** Kalin unlu mu (son unluye gore); hic unlu yoksa ince kabul ediliyor. */
private fun lastVowelIsBack(text: String): Boolean {
    for (ch in text.reversed()) {
        if (ch in BACK_VOWELS) return true
        if (ch in FRONT_VOWELS) return false
    }
    // Unlusuz ad ("MNG" gibi) - ince varsayiliyor, cunku harfler tek tek
    // okunurken sonuncusu genelde ince biter ("MNG'de").
    return false
}

/**
 * Son rakamin OKUNUSUNDAN (kalinlik, sertlik) ciftini verir.
 *
 * Okunuslar: sifir, bir, iki, uc, dort, bes, alti, yedi, sekiz, dokuz.
 */
private fun digitEnding(digit: Char): Pair<Boolean, Boolean> = when (digit) {
    '0' -> true to false // sifir  - son unlu i (kalin), r yumusak
    '1' -> false to false // bir   - i, r
    '2' -> false to false // iki   - i, unluyle bitiyor
    '3' -> false to true // uc     - u, c sert
    '4' -> false to true // dort   - o, t sert
    '5' -> false to true // bes    - e, s sert
    '6' -> true to false // alti   - i, unluyle bitiyor
    '7' -> false to false // yedi  - i, unluyle bitiyor
    '8' -> false to false // sekiz - i, z yumusak
    else -> true to false // dokuz - u, z yumusak
}

/** Kalin unluler, iki halde de. */
private const val BACK_VOWELS = "aouıAOUI"

/** Ince unluler, iki halde de. */
private const val FRONT_VOWELS = "eiöüEİÖÜ"

/** Sert unsuzler ("fistikci sahap"), iki halde de. */
private const val VOICELESS = "fstkçşhpFSTKÇŞHP"
