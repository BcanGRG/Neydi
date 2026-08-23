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

/**
 * Sayiya IYELIK EKI: `2` -> `"2'si"`, `3` -> `"3'ü"` (karar 117).
 *
 * Beyan cumlesinin ikinci yarisi bunu kullaniyor: *"2'si A101'de"*.
 *
 * ## Neden bir tablo, [turkishLocative] gibi iki kural degil
 *
 * Bulunma halinde ek iki degiskenden (kalinlik, sertlik) tureiyordu ve dort
 * ihtimali vardi. Iyelik ekinde ise unlunun kendisi ekin icine giriyor -
 * "iki" -> `si`, "uc" -> `ü`, "dokuz" -> `u`, "alti" -> `sı` - yani dort
 * degil yedi ayri sonuc var ve hicbir ikili bayrak bunlari uretmiyor.
 * Okunuslar zaten sabit, dolayisiyla tablo kuraldan daha durust.
 *
 * Tek ek secmek ("2'i", "3'i") coğu sayida yanlis olurdu ve yanlis ek
 * cumleyi cevrilmis gibi okutur - uygulamanin dili Turkce.
 *
 * ## Neden yalnizca 1-99
 *
 * Ek, sayinin SON kelimesini izliyor: "yirmi bir" -> `bir` -> `i`. Yuzun
 * ustunde de ayni kural islerdi ama bir alisveris listesinde sapan satir
 * sayisi iki haneyi gecmiyor; ustu gelirse ek yazilmiyor ve cumle yine
 * okunuyor ("100 A101'de").
 */
fun possessiveSuffix(n: Int): String {
    if (n <= 0 || n > 99) return ""
    // ONCE BIRLER: "yirmi" ile "yirmi bir" farkli bitiyor, yani birler
    // basamagi varsa ek ONU izliyor.
    val ones = n % 10
    return if (ones != 0) ONES_POSSESSIVE[ones] else TENS_POSSESSIVE[n / 10]
}

/** Birler basamaginin okunusuna gore iyelik eki. */
private val ONES_POSSESSIVE = listOf(
    "", // -
    "i", // bir
    "si", // iki
    "ü", // üç
    "ü", // dört
    "i", // beş
    "sı", // altı
    "si", // yedi
    "i", // sekiz
    "u", // dokuz
)

/** Tam onluklarin okunusuna gore iyelik eki. */
private val TENS_POSSESSIVE = listOf(
    "", // -
    "u", // on
    "si", // yirmi
    "u", // otuz
    "ı", // kırk
    "si", // elli
    "sı", // altmış
    "si", // yetmiş
    "i", // seksen
    "u", // doksan
)

/**
 * Turkce YONELME HALI eki: "BIM" -> "BIM'e", "File" -> "File'ye".
 *
 * Beyan cumlesinin ilk yarisi bunu kullaniyor: *"BIM'e gidiyorsun"* (karar 117).
 *
 * ## [turkishLocative]'ten farki: KAYNASTIRMA UNSUZU
 *
 * Bulunma halinde ek her zaman bir unsuzle basliyordu (`-de`/`-ta`), yani
 * onceki harfin unlu olmasi sorun degildi. Yonelme eki ise TEK BIR UNLU
 * (`-e`/`-a`) ve iki unlu yan yana gelemez - araya `y` giriyor:
 * "File'ye", "CarrefourSA'ya", "Tarim Kredi'ye". Yedi tohum zincirinden
 * UCU bu durumda, yani kural istisna degil.
 *
 * Sertlik/yumusaklik BURADA ROL OYNAMIYOR (ekte d/t yok) - bu yuzden
 * [turkishLocative]'in iki ekseninden yalnizca biri, unlu uyumu, geciyor.
 *
 * Rakamla biten adlar yine OKUNUSTAN: "A101" -> *"yuz bir"* -> `r` ile
 * bitiyor, ince -> **A101'e**. "A102" olsaydi *"iki"* unluyle biterdi ve
 * kaynastirma gerekirdi -> "A102'ye".
 */
fun turkishDative(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return trimmed
    val last = trimmed.last()
    val (back, endsWithVowel) = if (last.isDigit()) {
        digitEnding(last).first to (last in VOWEL_FINAL_DIGITS)
    } else {
        lastVowelIsBack(trimmed) to (last in BACK_VOWELS || last in FRONT_VOWELS)
    }
    val buffer = if (endsWithVowel) "y" else ""
    val vowel = if (back) 'a' else 'e'
    return "$trimmed'$buffer$vowel"
}

/**
 * Okunusu UNLUYLE biten rakamlar: iki, alti, yedi.
 *
 * Otekiler unsuzle bitiyor (sifir/bir `r`, uc `c`, dort `t`, bes `s`,
 * sekiz/dokuz `z`) ve kaynastirma istemiyor.
 */
private const val VOWEL_FINAL_DIGITS = "267"

/** Kalin unluler, iki halde de. */
private const val BACK_VOWELS = "aouıAOUI"

/** Ince unluler, iki halde de. */
private const val FRONT_VOWELS = "eiöüEİÖÜ"

/** Sert unsuzler ("fistikci sahap"), iki halde de. */
private const val VOICELESS = "fstkçşhpFSTKÇŞHP"
