package com.neydi.app.data.receipt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Ayristirici elde yazilmis kurallardan olusuyor, yani her kural elle test
 * edilmeli. Bu testler ODEMELI YOLDA YAZILAMAZDI: orada dogrulama ancak canli
 * API'ye karsi, ucret odeyerek ve yavas kosardi.
 */
class ReceiptParserTest {

    @Test
    fun parsesTurkishMoneyToMinor() {
        assertEquals(1250, parseMinor("12,50"))
        assertEquals(500, parseMinor("5,00"))
        assertEquals(123456, parseMinor("1.234,56"))
        // Binlik nokta olmadan da ayni sayi - fisler ikisini de basiyor.
        assertEquals(123456, parseMinor("1234,56"))
        assertEquals(-500, parseMinor("-5,00"))
    }

    /** Kurus hanesi olmayan sayi PARA DEGIL: "%1" ya da "0,850 KG" tutar sanilmamali. */
    @Test
    fun rejectsNonMoneyNumbers() {
        assertNull(parseMinor("1"))
        assertNull(parseMinor("0,850"))
        assertNull(parseMinor("abc"))
    }

    @Test
    fun parsesSimpleReceipt() {
        val reading = parseReceipt(
            listOf(
                "MIGROS TICARET A.S.",
                "TARIH 14.08.2026",
                "EKMEK %1 5,00",
                "SUT 1 L %1 32,50",
                "TOPKDV 2,45",
                "TOPLAM 37,50",
                "NAKIT 40,00",
                "PARA USTU 2,50",
            )
        )
        assertEquals("MIGROS TICARET A.S.", reading.storeName)
        assertEquals(2, reading.rows.size)
        assertEquals("EKMEK", reading.rows[0].name)
        assertEquals(500, reading.rows[0].amountMinor)
        assertEquals("SUT 1 L", reading.rows[1].name)
        assertEquals(3250, reading.rows[1].amountMinor)
        assertEquals(3750, reading.totalMinor)
    }

    /**
     * KDV dokumu URUN DEGIL. Bu satiri urun sayarsak hem sahte bir urun
     * dogar hem de toplam tutmaz - iki hata birden.
     */
    @Test
    fun vatLineIsNotAProduct() {
        val reading = parseReceipt(listOf("MARKET", "EKMEK 5,00", "TOPKDV 0,45", "TOPLAM 5,00"))
        assertEquals(1, reading.rows.size)
        assertEquals("EKMEK", reading.rows[0].name)
    }

    /** "TOPLAM KDV" ikisini de iceriyor; vergi tutari fis toplami sanilmamali. */
    @Test
    fun vatTotalIsNotMistakenForReceiptTotal() {
        val reading = parseReceipt(listOf("MARKET", "EKMEK 5,00", "TOPLAM KDV 0,45", "TOPLAM 5,00"))
        assertEquals(500, reading.totalMinor)
        assertEquals(1, reading.rows.size)
    }

    /** Tartili urun IKI SATIR: ad ustte, agirlik ve tutar altta. */
    @Test
    fun mergesWeighedItemFromTwoLines() {
        val reading = parseReceipt(
            listOf("MARKET", "DOMATES %1", "0,850 KG x 24,90 TL/KG 21,17", "TOPLAM 21,17")
        )
        assertEquals(1, reading.rows.size)
        val row = reading.rows[0]
        assertEquals("DOMATES", row.name)
        assertEquals(0.850, row.quantity)
        assertEquals("kg", row.unit)
        assertEquals(2490, row.unitPriceMinor)
        assertEquals(2117, row.amountMinor)
    }

    /** OCR termal yazicinin ince carpisini bazen yildiz okuyor. */
    @Test
    fun acceptsAllMultiplicationSigns() {
        for (sign in listOf("x", "X", "*", "×")) {
            val reading = parseReceipt(
                listOf("MARKET", "ELMA", "1,000 KG $sign 30,00 TL/KG 30,00", "TOPLAM 30,00")
            )
            assertEquals(1, reading.rows.size, "carpim isareti: $sign")
            assertEquals(3000, reading.rows[0].amountMinor)
        }
    }

    /** Isareti bayrak tasiyor, sayi degil - yoksa toplama iki kez eksi girerdi. */
    @Test
    fun discountStoredAsPositiveAmountPlusFlag() {
        val reading = parseReceipt(listOf("MARKET", "EKMEK 5,00", "INDIRIM -1,00", "TOPLAM 4,00"))
        val discount = reading.rows.single { it.discount }
        assertEquals(100, discount.amountMinor)
        assertTrue(discount.discount)
    }

    /** OCR Turkce karakter basmayabilir; iki yazim da ayni satiri bulmali. */
    @Test
    fun findsDiscountInTurkishAndAsciiSpelling() {
        for (spelling in listOf("İNDİRİM", "INDIRIM", "indirim")) {
            val reading = parseReceipt(listOf("MARKET", "EKMEK 5,00", "$spelling -1,00", "TOPLAM 4,00"))
            assertEquals(1, reading.rows.count { it.discount }, "yazim: $spelling")
        }
    }

    @Test
    fun dropsPaymentAndHeaderLines() {
        val reading = parseReceipt(
            listOf(
                "A101",
                "FIS NO 0042",
                "EKMEK 5,00",
                "NAKIT 10,00",
                "PARA USTU 5,00",
                "TESEKKUR EDERIZ",
                "TOPLAM 5,00",
            )
        )
        assertEquals(1, reading.rows.size)
        assertEquals("EKMEK", reading.rows[0].name)
    }

    // --- Aritmetik kapisi ---------------------------------------------------

    @Test
    fun acceptsReceiptWhoseTotalAddsUp() {
        val reading = parseReceipt(listOf("MARKET", "EKMEK 5,00", "SUT 32,50", "TOPLAM 37,50"))
        assertEquals(true, arithmeticHolds(reading))
    }

    /** Kacirilan satir toplami bozar - kapinin butun varlik sebebi bu. */
    @Test
    fun catchesMissingLine() {
        val reading = ReceiptReading(
            storeName = null,
            rows = listOf(ParsedLine(rawText = "EKMEK 5,00", name = "EKMEK", amountMinor = 500)),
            totalMinor = 3750,
            rawLines = emptyList(),
        )
        assertEquals(false, arithmeticHolds(reading))
    }

    /** Tartili urun yuvarlamasi 5 kurusa kadar oynatabilir; bu fis TUTARSIZ degil. */
    @Test
    fun allowsFiveMinorTolerance() {
        val reading = ReceiptReading(
            storeName = null,
            rows = listOf(ParsedLine(rawText = "", name = "DOMATES", amountMinor = 2117)),
            totalMinor = 2120,
            rawLines = emptyList(),
        )
        assertEquals(true, arithmeticHolds(reading))
    }

    @Test
    fun rejectsBeyondTolerance() {
        val reading = ReceiptReading(
            storeName = null,
            rows = listOf(ParsedLine(rawText = "", name = "DOMATES", amountMinor = 2117)),
            totalMinor = 2130,
            rawLines = emptyList(),
        )
        assertEquals(false, arithmeticHolds(reading))
    }

    @Test
    fun subtractsDiscountFromTotal() {
        val reading = parseReceipt(
            listOf("MARKET", "EKMEK 5,00", "SUT 32,50", "INDIRIM -2,50", "TOPLAM 35,00")
        )
        assertEquals(true, arithmeticHolds(reading))
    }

    /** "Dogrulanamadi" ile "tutmadi" AYRI SEYLER - null bunu tasiyor. */
    @Test
    fun returnsNullWhenTotalUnreadable() {
        val reading = parseReceipt(listOf("MARKET", "EKMEK 5,00"))
        assertNull(arithmeticHolds(reading))
    }

    /** Gercekci bir fis: tartili urun, KDV dokumu, indirim ve odeme bir arada. */
    @Test
    fun realisticReceiptAddsUp() {
        val reading = parseReceipt(
            listOf(
                "MIGROS TICARET A.S.",
                "SUBE 0421  KASA 03",
                "TARIH 14.08.2026 SAAT 18:42",
                "DOMATES %1",
                "0,850 KG x 24,90 TL/KG 21,17",
                "EKMEK 200 GR %1 8,50",
                "SUT 1 L %1 32,50",
                "PEYNIR 500 GR %8 149,90",
                "INDIRIM -12,07",
                "TOPKDV 18,44",
                "TOPLAM 200,00",
                "KREDI KARTI 200,00",
                "TESEKKUR EDERIZ",
            )
        )
        assertEquals("MIGROS TICARET A.S.", reading.storeName)
        assertEquals(4, reading.rows.count { !it.discount })
        assertEquals(1, reading.rows.count { it.discount })
        assertEquals(20000, reading.totalMinor)
        assertEquals(true, arithmeticHolds(reading))
    }
}
