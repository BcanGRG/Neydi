package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Sepet tahmininin satir kurali (kararlar 95, 96, 111-114).
 *
 * ## Neden bu aritmetigin testi var
 *
 * Bu ekranin tek isi bir SAYI soylemek ve o sayi bugune kadar yanlisti:
 * kullanicinin kendi listesinde `3 kg Yogurt`, bir 3 kg'lik kovanin 192,00 TL
 * fiyatiyla carpilip **576,00 TL** yaziyordu. Yanlis sayi, hicbir sey
 * yazmamaktan kotu - cunku kullanici ona gore para ayiriyor.
 *
 * Ve kural bir uc noktada sessizce 1000x yanlislasabiliyor (bkz.
 * [UnitScaleTest]), o yuzden her dal tek tek kilitli.
 */
class BasketEstimateRuleTest {

    private fun line(
        quantity: Double,
        unit: String,
        price: Long? = null,
        packSize: Double? = null,
        packUnit: String? = null,
        chain: String? = "bim",
        storeName: String? = "BİM",
    ) = EstimateLine(quantity, unit, price, packSize, packUnit, chain, storeName)

    // --- Satirin katkisi ----------------------------------------------------

    /** Sayilan satir duz carpiliyor - karar 96'nin ilk dali. */
    @Test
    fun aCountLineMultipliesByQuantity() {
        assertEquals(3_400L, lineEstimateMinor(line(2.0, "adet", price = 1_700)))
    }

    /**
     * SAYILAN, "adet" DEMEK DEGIL: paket, kutu, demet, sise de sayiliyor.
     *
     * Katalogda bes `demet` urun var; iki demet maydanozu tag fiyatiyla
     * carpmak dogru.
     */
    @Test
    fun everyCountedUnitMultiplies() {
        listOf("adet", "paket", "kutu", "demet", "şişe").forEach {
            assertTrue(isCountLine(it), "$it sayilan bir birim")
        }
        assertFalse(isCountLine("kg"))
        assertFalse(isCountLine("g"))
        assertFalse(isCountLine("L"))
    }

    /**
     * KULLANICININ BILDIRDIGI KUSUR: ambalaji bilinmeyen tartili satir
     * toplama GIRMIYOR (karar 96 + 111).
     *
     * `3 kg Yogurt` x 192,00 TL bugun 576,00 TL yaziyordu. Karar 111 yol
     * haritasinin *"dogrusu 192,00"* cevabini da REDDETTI, cunku o cevap
     * kullanicinin yazdigi 3 kg'i sessizce 1'e indiriyordu.
     *
     * `null` DONUYOR, SIFIR DEGIL: dusen satirin tutari yok, sifir degil.
     * Sifir bir cevap gibi okunur ve satiri paya katardi.
     */
    @Test
    fun aWeighedLineWithAnUnknownPackContributesNothing() {
        assertNull(lineEstimateMinor(line(3.0, "kg", price = 19_200)))
    }

    /** Tartili + ambalaj biliniyor: kac paket gerekiyorsa o kadar. */
    @Test
    fun aWeighedLineBuysWholePacks() {
        // 0,5 kg icin 250 g'lik iki paket.
        assertEquals(2L * 4_900, lineEstimateMinor(line(0.5, "kg", 4_900, 250.0, "gr")))
        // 1 L icin 1 L'lik tek sise.
        assertEquals(6_250L, lineEstimateMinor(line(1.0, "L", 6_250, 1.0, "lt")))
    }

    /** Miktar ambalajdan KUCUK olsa bile en az bir paket aliniyor. */
    @Test
    fun lessThanOnePackIsStillOnePack() {
        assertEquals(1, packsNeeded(0.2, "kg", packSize = 1.0, packUnit = "kg"))
    }

    /**
     * 1000x MAYINI: gram satiri, kilo ambalaja karsi.
     *
     * Ham `⌈2 ÷ 1⌉` iki paket verir - 798,00 TL, dogrusu 399,00. Kapali bir
     * gezide `2 g Cay` satiri GERCEKTEN var. `500 g` olsaydi bes yuz paket,
     * yani 199.500,00 TL.
     */
    @Test
    fun gramsDividedByAKiloPackDoNotBecomeAThousandPacks() {
        assertEquals(1, packsNeeded(2.0, "g", packSize = 1.0, packUnit = "kg"))
        assertEquals(1, packsNeeded(500.0, "g", packSize = 1.0, packUnit = "kg"))
        assertEquals(2, packsNeeded(1_500.0, "g", packSize = 1.0, packUnit = "kg"))
    }

    /**
     * OLCEK TUTMUYORSA SATIR AMBALAJSIZ SAYILIYOR (karar 111).
     *
     * Kilo satirina "adet" ambalaj gelirse bolme yapilmiyor - bir sayi
     * uretilebilirdi ama o sayi hicbir sey olmazdi.
     */
    @Test
    fun aPackInAnotherKindOfUnitIsNotAPack() {
        assertNull(packsNeeded(1.0, "kg", packSize = 6.0, packUnit = "adet"))
        assertNull(lineEstimateMinor(line(1.0, "kg", 19_200, 6.0, "adet")))
    }

    /** Gozlemi olmayan satir ne toplama ne paya giriyor. */
    @Test
    fun aLineWithNoPriceContributesNothing() {
        assertNull(lineEstimateMinor(line(1.0, "kg")))
    }

    /**
     * KURUS EN YAKINA YUVARLANIYOR.
     *
     * Bugun carpim SQL'de ve `SUM(REAL)` Room'a `Long` okundugu icin kurus
     * ASAGI kirpiliyor: 0,75 x 62,50 = 46,875 TL, 46,87 kaydediliyordu.
     * `formatEstimate`in kendi kurali "yuvarlama en yakina".
     */
    @Test
    fun aFractionalQuantityRoundsToTheNearestKurus() {
        assertEquals(4_688L, lineEstimateMinor(line(0.75, "adet", price = 6_250)))
    }

    // --- Pay, payda, esik ---------------------------------------------------

    /**
     * DUSEN SATIR PAYDAN CIKIYOR, PAYDADA KALIYOR (karar 96).
     *
     * Bu ayrim hesabin SQL'den Kotlin'e tasinmasinin sebebi: bugunku sorgu
     * `SUM` ile `COUNT`u ayni `GROUP BY` uzerinde hesapliyor, yani toplamdan
     * cikan satir sayidan da cikiyordu.
     */
    @Test
    fun aDroppedLineLeavesTheNumeratorAndStaysInTheDenominator() {
        val totals = foldBasket(
            listOf(
                line(1.0, "adet", price = 1_700),
                line(3.0, "kg", price = 19_200), // ambalaj bilinmiyor - duser
                line(1.0, "adet", price = 14_500),
            ),
        )
        assertEquals(16_200L, totals.amountMinor)
        assertEquals(2, totals.pricedCount)
        assertEquals(3, totals.totalCount)
    }

    /**
     * KULLANICININ GERCEK SEPETI, bastan sona.
     *
     * Bugun 1.199,50 TL yaziyor. Karar 96 + 111'den sonra 623,50 TL ve 4/7 -
     * aradaki 576,00 TL, tek bir hayalet satirdi.
     */
    @Test
    fun theUsersRealBasketAddsUpTheWayTheDecisionSays() {
        val totals = foldBasket(
            listOf(
                line(1.0, "adet", 1_700, 500.0, "gr"), // Makarna
                line(1.0, "L", 6_250, 1.0, "lt"), // Süt
                line(3.0, "kg", 19_200), // Yoğurt - ambalaj yok, duser
                line(1.0, "adet", 14_500), // Yumurta
                line(1.0, "g", 39_900, 1.0, "kg"), // Çay
                line(1.0, "kg"), // Domates - gozlem yok
                line(1.0, "adet"), // Ekmek - gozlem yok
            ),
        )
        assertEquals(62_350L, totals.amountMinor)
        assertEquals(4, totals.pricedCount)
        assertEquals(7, totals.totalCount)
    }

    /** Esik MUTLAK ve toplama GIREN satiri sayiyor (karar 112). */
    @Test
    fun theRowIsHiddenUntilThreeLinesActuallyContribute() {
        assertFalse(basketIsShown(2))
        assertTrue(basketIsShown(3))
    }

    // --- Kaynak cumlesi -----------------------------------------------------

    /** Hepsi tek zincirdense zincir adi yaziliyor (karar 95). */
    @Test
    fun oneChainNamesItself() {
        val totals = foldBasket(
            listOf(
                line(1.0, "adet", price = 1_700),
                line(2.0, "adet", price = 900),
                line(1.0, "adet", price = 500),
            ),
        )
        assertEquals(EstimateSource.Chain("BİM"), totals.source)
    }

    /**
     * ZINCIR ANAHTARLA KARSILASTIRILIYOR, ADLA YAZILIYOR.
     *
     * `chain` normalize bir anahtar ("bim") ve ekrana yazilinca KUCUK HARFLE
     * cikiyordu - cihazda goruldu. Donusturmek de yasak: bu projede
     * locale'siz harf donusumu olculmus bir hata. Dogru yol tablodaki adi
     * okumak, ve iki SUBE de ayni zincir sayilmali.
     */
    @Test
    fun theChainIsComparedByKeyAndWrittenByName() {
        val totals = foldBasket(
            listOf(
                line(1.0, "adet", price = 1_700, chain = "migros", storeName = "Migros Bahçelievler"),
                line(1.0, "adet", price = 900, chain = "migros", storeName = "Migros Ataşehir"),
                line(1.0, "adet", price = 500, chain = "migros", storeName = "Migros Ataşehir"),
            ),
        )
        assertEquals(EstimateSource.Chain("Migros"), totals.source)
    }

    /** Karisiksa cumle zincir adi vermiyor. */
    @Test
    fun twoChainsBecomeLastPrices() {
        val totals = foldBasket(
            listOf(
                line(1.0, "adet", price = 1_700, chain = "bim", storeName = "BİM"),
                line(1.0, "adet", price = 900, chain = "a101", storeName = "A101"),
            ),
        )
        assertEquals(EstimateSource.Mixed, totals.source)
    }

    /**
     * TEK MARKETSIZ GOZLEM ZINCIR ADINI DUSURUYOR (karar 113).
     *
     * *"BIM fiyatlariyla"* bir IDDIA; iceride nereden geldigi bilinmeyen bir
     * fiyat varsa iddia kanitlanmis degil. `provablySamePack` ile ayni
     * katilik.
     */
    @Test
    fun aStorelessObservationBreaksTheClaim() {
        val totals = foldBasket(
            listOf(
                line(1.0, "adet", price = 1_700),
                line(1.0, "adet", price = 900, chain = null, storeName = null),
            ),
        )
        assertEquals(EstimateSource.Mixed, totals.source)
    }

    /**
     * YALNIZ TOPLAMA GIREN SATIRLAR OY VERIYOR (karar 114).
     *
     * Dusen bir A101 satiri, katkida bulunmadigi bir tutari "son fiyatlarla"
     * diye etiketletemiyor. Cumle yanindaki TUTARI anlatiyor.
     */
    @Test
    fun onlyContributingLinesVoteForTheLabel() {
        val totals = foldBasket(
            listOf(
                line(1.0, "adet", price = 1_700),
                line(1.0, "adet", price = 900),
                line(1.0, "adet", price = 500),
                // Ambalaji bilinmeyen A101 satiri: toplama girmiyor, oy da vermiyor.
                line(3.0, "kg", price = 19_200, chain = "a101", storeName = "A101"),
            ),
        )
        assertEquals(EstimateSource.Chain("BİM"), totals.source)
        assertEquals(3, totals.pricedCount)
        assertEquals(4, totals.totalCount)
    }
}
