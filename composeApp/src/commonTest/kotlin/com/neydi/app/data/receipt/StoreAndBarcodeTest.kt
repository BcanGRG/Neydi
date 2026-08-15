package com.neydi.app.data.receipt

import com.neydi.app.ui.receipt.barcodeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tasarim kararlari 13 ve 14'un saf yarisi.
 *
 * Girdiler CIHAZDAKI GERCEK fislerden: sentetik ornek yazmak bu projede iki
 * kez bosa cikti (bkz. ReceiptParser KDoc).
 */
class StoreAndBarcodeTest {

    // --- 13 · Ekranda gosterilecek magaza adi -------------------------------

    /**
     * TICARI UNVAN DEGIL ZINCIR ADI. Cihazda basligin tamamini yiyen ad
     * buydu; tarih ve parca sayisi ekran disinda kaliyordu.
     */
    @Test
    fun takesChainNameFromLegalTitle() {
        assertEquals(
            "AKYURT",
            storeDisplayName("AKYURT SÜPERMARKET GIDA İNS.SAN.VE TİC. A.Ş."),
        )
        assertEquals("FiLE", storeDisplayName("FiLE MARKET MAGAZACILIK ANONIM SIRKETI"))
        assertEquals("BIM", storeDisplayName("BIM BIRLESIK MAGAZALAR A.S."))
    }

    /** Sondaki noktalama atiliyor: "MIGROS." zincir adi degil, cumle sonu. */
    @Test
    fun trimsTrailingPunctuation() {
        assertEquals("MİGROS", storeDisplayName("MİGROS. TİCARET A.Ş."))
    }

    /** Tek harflik parcalar atlaniyor - "A.Ş." unvan kirintisi, zincir degil. */
    @Test
    fun skipsSingleLetterFragments() {
        assertEquals("MARKET", storeDisplayName("A MARKET SAN. TİC."))
    }

    @Test
    fun returnsNullWhenNothingReadable() {
        assertNull(storeDisplayName(null))
        assertNull(storeDisplayName("   "))
    }

    // --- 14 · Adi okunamayan satirin barkodu --------------------------------

    /**
     * AKYURT tutar satiri: sira no, BARKOD, adet, birim fiyat, KDV, tutar.
     * Cihazdaki gercek satir.
     */
    @Test
    fun findsBarcodeInAkyurtRow() {
        assertEquals(
            "8683206511079",
            barcodeOf("3 8683206511079 1 Adet 189,90 %20 189,90"),
        )
    }

    /**
     * SEKIZ HANE ESIGI. Esik olmasaydi tutarin tam kismi ("189") ya da sira
     * no barkod sanilirdi - ve baslikta ürün adi yerine "3" yazardi.
     *
     * Test isiriyor: esik dusurulurse burasi kirilir.
     */
    @Test
    fun shortNumbersAreNotBarcodes() {
        assertNull(barcodeOf("3 1 Adet 189,90 %20 189,90"))
        assertNull(barcodeOf("2 ad X 53.00"))
    }

    /** Ondalik tasiyan parcalar rakam dizisi degil: tutar barkod sanilmiyor. */
    @Test
    fun amountsAreNotBarcodes() {
        assertNull(barcodeOf("KREMA 18YAĞLI 200ML %1. *106.00"))
    }

    /** Birden fazla aday varsa EN UZUNU barkod. */
    @Test
    fun longestDigitRunWins() {
        assertEquals("8690632012407", barcodeOf("12345678 8690632012407 1 Adet"))
    }
}
