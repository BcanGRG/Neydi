package com.neydi.app.ui.capture

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * SAGDAN DOLAN fiyat alani (karar 73).
 *
 * Kural bir cihaz provasindan dogdu: serbest yazimda `39,50` demek isteyip
 * `3950` yazan kullanicinin fiyati **3.950,00** olarak kaydedildi - yuz kat
 * hata, hicbir uyari yok. Tasarim alanin yazar kasa gibi sagdan dolmasina
 * karar verdi.
 *
 * Testler tus tus yaziyor, cunku alanin gercekte gordugu sey bu: her tusta
 * `onValueChange` bir kez cagriliyor ve o an alanda yazan METNIN TAMAMI
 * geliyor.
 */
class PriceFieldTest {

    private val empty = ConfirmCard(photoPath = "/x.jpg", reading = false)

    /** Alanin gercek davranisi: her tusta o anki metnin tamami geliyor. */
    private fun ConfirmCard.type(vararg keys: String): ConfirmCard {
        var card = this
        keys.forEach { key -> card = card.withPriceInput(card.priceDigits() + key) }
        return card
    }

    /**
     * ETIKETTEKI `39,50` OKUMA SIRASIYLA TUSLANIYOR ve dogru cikiyor.
     *
     * Serbest yazimda ayni dort tus 3.950,00 veriyordu.
     */
    @Test
    fun theTagsPriceIsTypedInReadingOrder() {
        assertEquals(3_950L, empty.type("3", "9", "5", "0").priceMinor)
    }

    /** Yazar kasa merdiveni: her tus degeri bir basamak kaydiriyor. */
    @Test
    fun eachKeyShiftsTheValueOneDigit() {
        assertEquals(3L, empty.type("3").priceMinor)
        assertEquals(39L, empty.type("3", "9").priceMinor)
        assertEquals(395L, empty.type("3", "9", "5").priceMinor)
    }

    /**
     * VIRGUL VE NOKTA YOK SAYILIYOR.
     *
     * Samsung'un sayisal klavyesi `Number` tipinde bile iki tusu da ciziyor;
     * kullanici aliskanlikla basarsa deger degismemeli.
     */
    @Test
    fun separatorKeysAreIgnored() {
        assertEquals(3_950L, empty.type("3", "9", ",", "5", "0").priceMinor)
        assertEquals(3_950L, empty.type("3", "9", ".", "5", "0").priceMinor)
    }

    /**
     * DOLU ALANDA ILK RAKAM BASTAN BASLATIYOR.
     *
     * OCR `24,90` yazmis; kullanici `3` tusluyor. Ekleme olsaydi 249,03
     * cikardi - kullanicinin yazmak istedigi seyle hicbir ilgisi olmayan bir
     * sayi. Duzeltme YENIDEN YAZMAK.
     */
    @Test
    fun theFirstKeyOnAFilledFieldRestartsIt() {
        val ocr = empty.copy(priceMinor = 2_490L)
        assertEquals(3L, ocr.type("3").priceMinor)
        assertEquals(3_950L, ocr.type("3", "9", "5", "0").priceMinor)
    }

    /**
     * BOS ALANDA SIFIRLAMA KURALI ISLEMIYOR.
     *
     * Kural once bosa da uygulaniyordu ve `3950`i tek seferde alan bir cagri
     * son haneye inip `0` doneyordu. Bos alanda sifirlanacak bir sey yok;
     * kural tasarimda da birebir *"dolu alanda"* diye yaziyor.
     */
    @Test
    fun anEmptyFieldTakesEveryDigitAtOnce() {
        assertEquals(3_950L, empty.withPriceInput("3950").priceMinor)
    }

    /** Silme SAGDAN ve bastan baslatma kuralina girmiyor - geri tusu rakam degil. */
    @Test
    fun backspaceRemovesFromTheRight() {
        val typed = empty.type("3", "9", "5", "0")
        assertEquals(395L, typed.withPriceInput("395").priceMinor)
        assertEquals(0L, typed.withPriceInput("").priceMinor)
    }

    /**
     * BOSALAN ALAN `— TL`YE DONUYOR ve Kaydet PASIFLESIYOR.
     *
     * Kaydet *"ilk rakamda"* degil DEGER SIFIRDAN CIKINCA etkinlesiyor:
     * `0` tuslamak bir rakam ama bir fiyat degil.
     */
    @Test
    fun anEmptiedFieldDisablesSave() {
        assertEquals("", empty.type("3").withPriceInput("").priceDigits())
        assertTrue(!empty.type("0").canSave, "sifir bir fiyat degil")
        assertTrue(empty.type("0", "1").canSave)
    }

    /**
     * YEDI HANEDEN UZUN GIRDI TASMIYOR.
     *
     * Sinirsiz birakilsaydi uzun basili kalmis bir tus `Long` tasmasi uretip
     * NEGATIF fiyat yazardi - sessiz ve kalici.
     */
    @Test
    fun anAbsurdlyLongEntryDoesNotOverflow() {
        val card = empty.withPriceInput("9".repeat(40))
        assertEquals(9_999_999L, card.priceMinor)
    }

    // --- KARAR 75 · hane secimi -------------------------------------------

    /**
     * KULLANICININ KENDI ORNEGI: `450,99` -> `460,99`.
     *
     * *"5'in oraya dokunup onu 6 yapabilmeliyim."* Karar 73 doneminde bu bes
     * tusluk yeniden yazimdi (`4-6-0-9-9`) ve kararin kabul ettigi sinirin tam
     * ucundaydi; karar 75 tek dokunus + tek tusa indirdi.
     */
    @Test
    fun tappingADigitAndTypingReplacesOnlyThatDigit() {
        val card = empty.copy(priceMinor = 45_099L)
        val edited = card.withPriceSelection(1).withPriceInput("450996")
        assertEquals(46_099L, edited.priceMinor)
    }

    /** Uzunluk SABIT kaliyor - hane degisiyor, deger on kata kaymiyor. */
    @Test
    fun replacingADigitKeepsTheLength() {
        val card = empty.copy(priceMinor = 45_099L)
        assertEquals(5, card.withPriceSelection(3).withPriceInput("4509 91").priceDigits().length)
    }

    /**
     * BASTAKI HANE SIFIRLANABILIYOR ve deger on kata BOLUNMUYOR.
     *
     * `450` (4,50) icin bas hane `0` yapilirsa `050` olmali, `50` degil -
     * yoksa kullanici tek hane degistirdigini sanirken fiyat 0,50'ye duser.
     * `trimStart('0')` bu dalda bilerek uygulanmiyor.
     */
    @Test
    fun zeroingTheLeadingDigitDoesNotShiftTheValue() {
        val card = empty.copy(priceMinor = 450L)
        val edited = card.withPriceSelection(0).withPriceInput("4500")
        assertEquals(50L, edited.priceMinor, "0,50 bekleniyordu")
        assertEquals(0, edited.priceMinor.toString().length - 2, "kurus haneleri korunmali")
    }

    /**
     * SECIM TEK ATIMLIK ve ILERLEMIYOR - ikinci hane ikinci dokunus ister.
     *
     * Ilerleseydi ustteki hanede bir tus fazlasi degeri on kat kaydirirdi.
     * Ikinci rakam secimsiz geldigi icin karar 73'e dusuyor.
     */
    @Test
    fun theSelectionIsSingleShotAndDoesNotAdvance() {
        val card = empty.copy(priceMinor = 45_099L).withPriceSelection(1)
        val once = card.withPriceInput("450996")
        assertEquals(null, once.priceSelection, "secim dusmeliydi")
        // Ikinci rakam artik karar 73: secimsiz, dolu alan, DOKUNULMUS ->
        // sifirlama yok, sagdan ekleme.
        assertEquals(460_997L, once.withPriceInput("460997").priceMinor)
    }

    /**
     * SECIM VARKEN "dolu alanda ilk rakam sifirlar" KURALI CALISMIYOR.
     *
     * Iki kural ayni anda tanimli olamaz; tasarim tetikleyicileri ayirdi:
     * sifirlama yalnizca SECIMSIZ ilk rakamda.
     */
    @Test
    fun selectionSuppressesTheRestartRule() {
        val ocr = empty.copy(priceMinor = 2_490L)          // dokunulmamis
        assertEquals(3L, ocr.withPriceInput("24903").priceMinor, "secimsiz: sifirlar")
        assertEquals(2_493L, ocr.withPriceSelection(3).withPriceInput("24903").priceMinor)
    }

    /** Silme secimi dusuruyor - geri tusu bir hane secimi degil. */
    @Test
    fun backspaceClearsTheSelection() {
        val card = empty.copy(priceMinor = 45_099L).withPriceSelection(2)
        val after = card.withPriceInput("4509")
        assertEquals(null, after.priceSelection)
        assertEquals(4_509L, after.priceMinor)
    }

    /** Alan disi ya da bos alanda secim TUTMUYOR - secilecek hane yok. */
    @Test
    fun anOutOfRangeSelectionIsRefused() {
        assertEquals(null, empty.copy(priceMinor = 450L).withPriceSelection(9).priceSelection)
        assertEquals(null, empty.withPriceSelection(0).priceSelection, "bos alanda hane yok")
        assertEquals(null, empty.copy(priceMinor = 450L).withPriceSelection(null).priceSelection)
    }

    /** Ilk duzenleme kurus uyarisini susturuyor (karar 72) - tek tusla bile. */
    @Test
    fun theFirstKeyMarksTheFieldTouched() {
        assertTrue(!empty.priceTouched)
        assertTrue(empty.type("3").priceTouched)
    }
}
