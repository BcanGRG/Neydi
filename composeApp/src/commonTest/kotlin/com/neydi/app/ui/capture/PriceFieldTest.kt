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

    /** Ilk duzenleme kurus uyarisini susturuyor (karar 72) - tek tusla bile. */
    @Test
    fun theFirstKeyMarksTheFieldTouched() {
        assertTrue(!empty.priceTouched)
        assertTrue(empty.type("3").priceTouched)
    }
}
