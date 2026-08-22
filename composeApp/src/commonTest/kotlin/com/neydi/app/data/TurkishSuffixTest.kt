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
}
