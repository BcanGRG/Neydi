package com.neydi.app.ui.product

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * *"Nereden alinacak"* satirinin cumlesi (karar 126).
 *
 * ## Neden bu cumlelerin testi var
 *
 * Satir bir fiili karsiliyor - kullanicinin *"2-3 tanesini de A101'den
 * alacagim diye **isaretlerim**"* cumlesindeki tek fiil - ve o fiil `docs/38`
 * S6 yazilana kadar **karsiliksizdi**: veri alani vardi (sema v8), gosterimi
 * vardi (karar 118), yazma yolu vardi (`setLineStore`) ama hicbiri
 * cagrilmiyordu.
 *
 * Simdi cagriliyor, ve satirin SOYLEDIGI sey iki farkli yerde
 * kararlastiriliyor: ne yazdigi burada, nasil gorundugu cizimde. Ikisi
 * ayrilirsa satir "BIM . hedef" derken kalin ve chevronlu cizilir - yani
 * hedefteki satir istisna gibi okunur.
 */
class DestinationRowTest {

    /**
     * HEDEFI IZLEYEN SATIR, HEDEFIN ADINI VE "hedef" KELIMESINI BIRLIKTE
     * YAZIYOR.
     *
     * Zincir adi tek basina bir ISTISNA gibi okunurdu - satirin sag tarafinda
     * "BIM" gormek, kullaniciya o satir icin ayrica bir sey sectigini
     * soylerdi. Aradaki nokta ikisini tek cumle yapiyor.
     */
    @Test
    fun aRowThatFollowsTheTargetSaysSoNextToTheTargetsName() {
        val store = LineStore(targetName = "BİM")
        assertEquals("BİM · hedef", store.label())
        assertFalse(store.isException, "hedefi izleyen satir istisna sayilmis")
    }

    /**
     * ISTISNA YALNIZCA ZINCIRIN ADINI YAZIYOR.
     *
     * Burada soylenecek ikinci bir sey yok: satirin agirligi (600), rengi
     * (`onSurface`) ve chevronu zaten "bu satir farkli" diyor. "A101'den"
     * gibi bir ek, ayni seyi ikinci kez soylerdi.
     */
    @Test
    fun anExceptionNamesOnlyTheChain() {
        val store = LineStore(targetName = "BİM", deviantName = "A101")
        assertEquals("A101", store.label())
        assertTrue(store.isException, "istisna tasiyan satir sessiz halde cizilecek")
    }

    /**
     * HEDEFIN KENDISI ISTISNA DEGIL - VE BU KURAL BURADA DEGIL, DEPODA.
     *
     * `ListRepository.setLineStore` hedefin kendisini secmeyi `null`'a
     * ceviriyor; yani bu yuzeye "istisna hedefin adiyla ayni" diye bir hal
     * HIC ulasmiyor. Test o sozlesmeyi yaziyor: ulasirsa satir, hedefi
     * izleyen bir satiri istisna gibi gosterirdi.
     *
     * Kural yazili olmasaydi bir gun cagiran taraf `deviantName`'i hedefin
     * adiyla doldurabilirdi ve hicbir sey sikayet etmezdi.
     */
    @Test
    fun theTargetItselfNeverArrivesAsAnException() {
        // Cagiranin urettigi hal: istisna yok, cunku hedefin kendisi secildi.
        val store = LineStore(targetName = "BİM", deviantName = null)
        assertEquals("BİM · hedef", store.label())
    }

    /**
     * ZINCIR ADI KULLANICININ VERISI - satir onu OLDUGU GIBI yaziyor.
     *
     * Turkce buyuk/kucuk harf cevrimi bu projede locale'siz YASAK
     * (`Conventions`), ama asil mesele o degil: ad kullanicinin duzenledigi
     * bir alan ve satirin isi onu tekrar etmek, duzeltmek degil.
     */
    @Test
    fun theChainNameIsWrittenExactlyAsTheHouseholdTypedIt() {
        assertEquals("Tarım Kredi", LineStore("BİM", "Tarım Kredi").label())
        assertEquals("ŞOK", LineStore("BİM", "ŞOK").label())
    }
}
