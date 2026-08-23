package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Birim ciplerinin havuzu (karar 108).
 *
 * ## Neden bu secimin testi var
 *
 * *"Katalogun makul birimleri"* bir yargi cumlesi ve yargi kodda bir listeye
 * donusuyor. Liste sessizce genisledigi gun yumurta satirinda "kg" cipi
 * belirir - ve kullanici yumurtayi kiloyla almaya davet edilmis olur.
 */
class QuantityUnitsTest {

    /** Tartilan urun: katalog birimi, olcek kardesi, sonra adet. */
    @Test
    fun aWeighedProductOffersItsScaleSiblingAndCount() {
        assertEquals(listOf("kg", "g", "adet"), unitOptionsFor("kg"))
        assertEquals(listOf("L", "ml", "adet"), unitOptionsFor("L"))
    }

    /** Katalogun birimi HER ZAMAN basta - kullanicinin bugunku hali o. */
    @Test
    fun theCatalogueUnitComesFirst() {
        assertEquals("g", unitOptionsFor("g").first())
        assertEquals("ml", unitOptionsFor("ml").first())
    }

    /**
     * SAYILAN URUN TARTIYA CEVRILMIYOR - ve tek secenek kalinca cip yok.
     *
     * Yumurtayi kiloyla almak diye bir sey yok. Tek uyeli bir liste, cagiran
     * tarafa "burada secim yok" demenin yolu: secim olmayan bir secici gurultu.
     */
    @Test
    fun aCountedProductHasNothingToChooseFrom() {
        assertEquals(listOf("adet"), unitOptionsFor("adet"))
        assertEquals(listOf("paket"), unitOptionsFor("paket"))
        assertTrue(unitOptionsFor("demet").size == 1)
    }
}
