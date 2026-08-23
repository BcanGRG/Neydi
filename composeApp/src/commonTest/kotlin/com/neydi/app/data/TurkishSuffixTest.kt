package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Turkce bulunma hali eki - "baska markette ucuz" cipinin metni buradan cikiyor.
 *
 * Testin cogunlugu TOHUMDAKI YEDI ZINCIR, cunku cipin gunluk hayatta
 * uretecegi metinler tam olarak bunlar. Kalan vakalar kuralin kendi
 * sinirlari: rakamla biten ad, unlusuz ad, bos dizgi.
 */
class TurkishSuffixTest {

    /**
     * YEDI ZINCIRIN HEPSI, tek tek.
     *
     * Hepsini birden yazmanin sebebi: kural iki eksende (unlu uyumu + unsuz
     * benzesmesi) calisiyor ve yedi zincir dort kombinasyonun hepsine ornek
     * veriyor. Biri bozulursa hangi eksenin bozuldugu tek bakista gorunur.
     */
    @Test
    fun theSevenSeedChainsGetTheirCorrectSuffix() {
        assertEquals("BİM'de", turkishLocative("BİM")) // ince + yumusak
        assertEquals("ŞOK'ta", turkishLocative("ŞOK")) // kalin + sert
        assertEquals("Migros'ta", turkishLocative("Migros")) // kalin + sert
        assertEquals("File'de", turkishLocative("File")) // ince + unlu
        assertEquals("CarrefourSA'da", turkishLocative("CarrefourSA")) // kalin + unlu
        assertEquals("Tarım Kredi'de", turkishLocative("Tarım Kredi")) // ince + unlu
    }

    /**
     * "A101'de" - TASARIMIN KENDI MAKETINDEKI metin.
     *
     * Ek harften degil, son rakamin OKUNUSUNDAN cikiyor: *"yuz bir"* -> `i`
     * ince, `r` yumusak. Harfe bakan bir kural "A101'da" yazardi ve maketi
     * yalanlardi.
     */
    @Test
    fun aNameEndingInADigitFollowsHowTheDigitIsRead() {
        assertEquals("A101'de", turkishLocative("A101"))
    }

    /** On rakamin okunusu, tek tek - kuralin tablosu. */
    @Test
    fun everyDigitEndingIsCorrect() {
        assertEquals("0'da", turkishLocative("0")) // sıfır
        assertEquals("1'de", turkishLocative("1")) // bir
        assertEquals("2'de", turkishLocative("2")) // iki
        assertEquals("3'te", turkishLocative("3")) // üç
        assertEquals("4'te", turkishLocative("4")) // dört
        assertEquals("5'te", turkishLocative("5")) // beş
        assertEquals("6'da", turkishLocative("6")) // altı
        assertEquals("7'de", turkishLocative("7")) // yedi
        assertEquals("8'de", turkishLocative("8")) // sekiz
        assertEquals("9'da", turkishLocative("9")) // dokuz
    }

    /**
     * BUYUK/KUCUK HARF AYRIMI EK'I DEGISTIRMEZ.
     *
     * Proje locale'siz `uppercase()`/`lowercase()` yasakliyor; kural bu yuzden
     * harf kumelerini iki halde de sayiyor. Yasak delinirse `I` ile `i`
     * karisir ve tam da burada patlar.
     */
    @Test
    fun caseDoesNotChangeTheSuffix() {
        assertEquals("bim'de", turkishLocative("bim"))
        assertEquals("şok'ta", turkishLocative("şok"))
        assertEquals("MIGROS'ta", turkishLocative("MIGROS"))
    }

    /** Unlusuz ad ve bos dizgi - kural kirilmadan geciyor. */
    @Test
    fun edgeCasesDoNotCrash() {
        assertEquals("MNG'de", turkishLocative("MNG"))
        assertEquals("", turkishLocative("   "))
        assertEquals("BİM'de", turkishLocative("  BİM  "))
    }

    /**
     * IYELIK EKI: BEYAN CUMLESININ IKINCI YARISI (karar 117).
     *
     * *"BİM'e gidiyorsun · 2'si A101'de"* - buradaki `2'si`. Birler
     * basamaginin okunusu eki belirliyor ve dokuz rakamin uretttigi ek
     * BES AYRI: `i`, `si`, `ü`, `sı`, `u`. Tek ek secmek (hep `i`) dokuz
     * vakanin altisinda yanlis olurdu, yani bu test tabloyu tek tek tutuyor.
     */
    @Test
    fun everySingleDigitGetsItsOwnPossessiveSuffix() {
        assertEquals("i", possessiveSuffix(1)) // bir
        assertEquals("si", possessiveSuffix(2)) // iki
        assertEquals("ü", possessiveSuffix(3)) // üç
        assertEquals("ü", possessiveSuffix(4)) // dört
        assertEquals("i", possessiveSuffix(5)) // beş
        assertEquals("sı", possessiveSuffix(6)) // altı
        assertEquals("si", possessiveSuffix(7)) // yedi
        assertEquals("i", possessiveSuffix(8)) // sekiz
        assertEquals("u", possessiveSuffix(9)) // dokuz
    }

    /**
     * EK SON KELIMEYI IZLIYOR, sayinin tamamini degil.
     *
     * "yirmi bir" -> `bir` -> `i`, yani `21'i`. Onluga BAKAN bir kural
     * (`21` -> yirmi -> `si`) burada yanlis cevap verirdi ve fark yalnizca
     * birler basamagi sifirdan farkliyken goruluyor - bu yuzden ikisi de
     * ayni testte.
     */
    @Test
    fun theSuffixFollowsTheLastSpokenWord() {
        assertEquals("u", possessiveSuffix(10)) // on
        assertEquals("i", possessiveSuffix(11)) // on bir
        assertEquals("si", possessiveSuffix(20)) // yirmi
        assertEquals("si", possessiveSuffix(22)) // yirmi iki
        assertEquals("u", possessiveSuffix(30)) // otuz
        assertEquals("ı", possessiveSuffix(40)) // kırk
        assertEquals("u", possessiveSuffix(99)) // doksan dokuz
    }

    /**
     * SINIR DISINDA EK YAZILMIYOR - cumle eksiz de okunuyor ("100 A101'de").
     *
     * Sifir ve negatif hic cizilmiyor (sapan satir yoksa cumlenin o yarisi
     * kurulmuyor), ama fonksiyon yine de bos donmeli: cagiran tarafta bir
     * hata olursa ekranda `null` ya da cop bir ek degil, hicbir sey cikar.
     */
    @Test
    fun outOfRangeCountsGetNoSuffix() {
        assertEquals("", possessiveSuffix(0))
        assertEquals("", possessiveSuffix(-3))
        assertEquals("", possessiveSuffix(100))
    }

    /**
     * YONELME HALI - beyan cumlesinin ilk yarisi: *"BIM'e gidiyorsun"*.
     *
     * YEDI ZINCIRIN UCU KAYNASTIRMA ISTIYOR (File, CarrefourSA, Tarim Kredi),
     * yani `y`siz bir kural yedide ucunu bozardi - istisna degil, cogunluga
     * yakin bir hal. Bu yuzden hepsi tek tek yaziliyor.
     */
    @Test
    fun theSevenSeedChainsGetTheirCorrectDativeSuffix() {
        assertEquals("BİM'e", turkishDative("BİM")) // ince, unsuz
        assertEquals("ŞOK'a", turkishDative("ŞOK")) // kalin, unsuz
        assertEquals("Migros'a", turkishDative("Migros")) // kalin, unsuz
        assertEquals("A101'e", turkishDative("A101")) // "bir" -> ince, unsuz
        assertEquals("File'ye", turkishDative("File")) // ince, UNLU
        assertEquals("CarrefourSA'ya", turkishDative("CarrefourSA")) // kalin, UNLU
        assertEquals("Tarım Kredi'ye", turkishDative("Tarım Kredi")) // ince, UNLU
    }

    /**
     * RAKAMIN OKUNUSU KAYNASTIRMAYI DA BELIRLIYOR, harfin kendisi degil.
     *
     * "A102" -> *"iki"* -> unluyle bitiyor -> `y` giriyor. Rakama bakan bir
     * kural burada "A102'e" yazardi; harfe bakan bir kural ise `2` unlu
     * olmadigi icin yine "A102'e" yazardi. Ikisi de yanlis.
     */
    @Test
    fun aDigitReadAsEndingInAVowelTakesTheBufferConsonant() {
        assertEquals("A102'ye", turkishDative("A102")) // iki
        assertEquals("A106'ya", turkishDative("A106")) // altı - kalin
        assertEquals("A107'ye", turkishDative("A107")) // yedi
        assertEquals("A103'e", turkishDative("A103")) // üç - unsuz
        assertEquals("A109'a", turkishDative("A109")) // dokuz - kalin, unsuz
    }
}
