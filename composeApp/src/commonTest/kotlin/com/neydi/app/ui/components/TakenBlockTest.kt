package com.neydi.app.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Alisveristeki *"Alindi"* sayacinin cumlesi (karar 124).
 *
 * ## Neden bu tek satirin testi var
 *
 * Sayac, `docs/38` S4'un cozumunun TAMAMI: karar 116 bolumu alisveriste
 * istiyordu, kod bolumu olculmus bir gerekceyle kaldirmisti (*"hareket eden
 * basparmagin altinda yeniden siralama"*), ve tasarim ikisini bu cumleyle
 * uzlastirdi - satir oynamiyor, ama kullanicinin *"kacini aldim"* sorusu
 * cevapsiz da kalmiyor.
 *
 * Bicim bu yuzden bir uslup tercihi degil kararin kendisi: **iki sayi**,
 * cunku tek sayi ("12 alindi") ilerlemeyi degil yalnizca bir toplami
 * soylerdi.
 */
class TakenBlockTest {

    /** Tasarimin verdigi bicim, harfi harfine: ayirici ORTA NOKTA (U+00B7). */
    @Test
    fun theCounterNamesBothNumbers() {
        assertEquals("Alındı · 12/18", takenCounterText(12, 18))
    }

    /**
     * HICBIRI ALINMAMISKEN DE YAZIYOR.
     *
     * Sifiri gizlemek, blogun alisverisin ortasinda kendiliginden BELIRMESI
     * demekti - listenin sonunda birden yeni bir satir. Bastan durup sayiyi
     * saymasi, hem daha sakin hem de "burasi ilerlemenin yeri" diye
     * ogretiyor.
     */
    @Test
    fun theCounterIsDrawnBeforeAnythingIsTaken() {
        assertEquals("Alındı · 0/18", takenCounterText(0, 18))
    }

    /** Hepsi alindiginda iki sayi esitleniyor - ayri bir "bitti" metni yok. */
    @Test
    fun theCounterJustEqualisesWhenEverythingIsTaken() {
        assertEquals("Alındı · 18/18", takenCounterText(18, 18))
    }

    /**
     * SAYI CUMLENIN ICINDE, SONUNDA DEGIL.
     *
     * Bolum basligi ("Alindi 12") sayiyi AYRI bir metin olarak sagda
     * tasiyor; sayac ise tek bir cumle. Ikisini ayni gostermek, birinin
     * genisledigini otekinin genislemedigini gizlerdi.
     */
    @Test
    fun theCounterIsOneSentenceNotALabelAndACount() {
        assertTrue(takenCounterText(3, 7).endsWith("3/7"))
        assertTrue(takenCounterText(3, 7).startsWith("Alındı · "))
    }
}
