package com.neydi.app.ui.list

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Miktar sayacinin acilma / kapanma kurallari (karar 107).
 *
 * ## Neden bu gecislerin testi var
 *
 * Dordu de ZAMANA ve SIRAYA bagli: *"her dokunus sayaci yeniden kurar;
 * kaydirma ya da baska yere dokunma aninda kapatir."* Zamana bagli kurallar
 * cihazda elle sinandiginda en kolay kacan seylerdir - uc saniye beklemek
 * gerekiyor ve bekleyen goz kendini kandirir. Burada beklemek gerekmiyor.
 */
class StepperStateTest {

    private val a = "row-a"
    private val b = "row-b"

    private fun open(rowId: String = a, seq: Long = 1) = OpenStepper(rowId, seq)

    /** Rozete dokunmak aciyor - baska hicbir yol yok (*"gorunmeyen kontrol yok"*). */
    @Test
    fun tappingTheBadgeOpensTheStepper() {
        assertEquals(open(a, 7), reduceStepper(null, StepperEvent.BadgeTap(a), nextSeq = 7))
    }

    /**
     * HER DOKUNUS SAYACI YENIDEN KURUYOR.
     *
     * Kurmanin tek yolu zamanlayici kimligini degistirmek: eski zamanlayici
     * yasamaya devam ediyor ve tek savunmasi kendini bayat tanimasi.
     */
    @Test
    fun everyTouchRestartsTheTimer() {
        val afterStep = reduceStepper(open(a, 1), StepperEvent.Step(a), nextSeq = 2)
        assertEquals(open(a, 2), afterStep)
        val afterBadge = reduceStepper(afterStep, StepperEvent.BadgeTap(a), nextSeq = 3)
        assertEquals(open(a, 3), afterBadge)
    }

    /**
     * BAYAT ZAMANLAYICI YENI SAYACI KAPATMIYOR.
     *
     * Bu, sayacin en sinsi hatasi olurdu: kullanici iki kez artiriyor, ilk
     * dokunustan uc saniye sonra sayac kendiliginden kapaniyor ve ikinci
     * dokunusun uzattigi sure hic yasanmiyor. Kimlik olmadan iki zamanlayici
     * birbirinden ayirt edilemez.
     */
    @Test
    fun aStaleTimeoutIsIgnored() {
        val extended = open(a, seq = 2)
        assertEquals(extended, reduceStepper(extended, StepperEvent.Timeout(seq = 1), nextSeq = 9))
    }

    /** Kendi zamanlayicisi ise kapatiyor - kural yine de isliyor. */
    @Test
    fun itsOwnTimeoutClosesIt() {
        assertNull(reduceStepper(open(a, 2), StepperEvent.Timeout(seq = 2), nextSeq = 9))
    }

    /** Kaydirma ve baska yere dokunma ANINDA kapatiyor - sure beklemeden. */
    @Test
    fun scrollingAndTappingElsewhereCloseItAtOnce() {
        assertNull(reduceStepper(open(), StepperEvent.Scroll, nextSeq = 9))
        assertNull(reduceStepper(open(), StepperEvent.TapElsewhere, nextSeq = 9))
    }

    /**
     * YALNIZ BIR SATIR ACIK KALIYOR.
     *
     * Iki sayac ayni anda acik olsaydi *"+ hangisine gidiyor"* sorusu ekrana
     * bakarak cevaplanamazdi - ikisi de ayni anda canli deger yaziyor olurdu.
     */
    @Test
    fun onlyOneRowIsEverOpen() {
        assertEquals(open(b, 5), reduceStepper(open(a, 1), StepperEvent.BadgeTap(b), nextSeq = 5))
    }

    /**
     * KAPANMIS SAYACIN GECIKMIS ADIMI SATIRI YENIDEN ACMIYOR.
     *
     * Parmak kalkarken gelen bir dokunus, kapanmis bir sayaci diriltip
     * kullanicinin gormedigi bir kontrolun miktari degistirmesine yol acardi.
     */
    @Test
    fun aStepFromAClosedRowDoesNothing() {
        assertNull(reduceStepper(null, StepperEvent.Step(a), nextSeq = 9))
        assertEquals(open(b, 1), reduceStepper(open(b, 1), StepperEvent.Step(a), nextSeq = 9))
    }
}
