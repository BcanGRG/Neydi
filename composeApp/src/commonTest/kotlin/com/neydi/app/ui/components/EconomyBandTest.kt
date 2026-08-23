package com.neydi.app.ui.components

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Ekonomi bandinin cumlesi (karar 104-105).
 *
 * ## Neden bu cumlelerin testi var
 *
 * Bandin tasidigi soz tek satirlik: *"fiyat iki yerde asla yazilmaz."* Cipin
 * ne zaman cizildigi (satirin govdesinde) ile cumlenin ne yazdigi (burada)
 * ayri yerlerde kararlastiriliyor - yani sozun bozulmasi icin birinin
 * degismesi yetiyor. Nitekim `Single` dalinda tam olarak o olmustu: karar 83
 * *"ikinci satir yok"* diyordu, maket *"son 24,90 TL · Migros · 8 gun once"*
 * ciziyordu ve kod ucuncu bir sey yaziyordu.
 *
 * Karar 105 celiskiyi cozdu - ikisi ayni seyin IKI TAZELIK HALIYMIS - ve
 * asagidaki iki vaka o iki hali kilitliyor.
 */
class EconomyBandTest {

    private fun price(hint: PriceHint) = SecondLine.Price(hint).metaText()

    /**
     * TAZE GOZLEM: cumle fiyat YAZMAZ - fiyat cipte durur.
     *
     * Tasarimin maketi: *"Salatalik / BIM · bugun / 24,90 TL"*.
     */
    @Test
    fun aFreshObservationLeavesThePriceToTheChip() {
        assertEquals("BİM · bugün", price(PriceHint.Single("24,90 TL", "BİM", 0)))
    }

    /**
     * ESKIMIS GOZLEM: cip yok, o yuzden fiyat CUMLEYE girer.
     *
     * Guncel fiyat bilinmiyor; bilinen sey hatirlanan bir fiyat ve cumle onu
     * "son" diye isaretliyor. Cip cizilseydi eski fiyat bugunku fiyat gibi
     * okunurdu - satirin en cok bakilan yerinde.
     */
    @Test
    fun aStaleObservationMovesThePriceIntoTheSentence() {
        assertEquals(
            "son 24,90 TL · Migros · 8 gün önce",
            price(PriceHint.Single("24,90 TL", "Migros", 8)),
        )
    }

    /**
     * SINIR TAM OLARAK YEDI GUN VE ICE KAPALI.
     *
     * Yedinci gun hala taze, sekizinci gun degil. Sinirin hangi tarafta
     * oldugunu yazmayan bir kural, iki uygulamada iki farkli cevap verir -
     * ve fark tam da cipin cizilip cizilmemesi.
     */
    @Test
    fun theFreshnessBoundaryIsInclusive() {
        assertTrue(price(PriceHint.Single("10,00 TL", "A101", FRESH_DAYS)).startsWith("A101"))
        assertTrue(price(PriceHint.Single("10,00 TL", "A101", FRESH_DAYS + 1)).startsWith("son "))
    }

    /** Trend dalinda cumle GECMISI anlatir; guncel fiyat yine yalniz cipte. */
    @Test
    fun theTrendSentenceNeverRepeatsTheCurrentPrice() {
        val meta = price(
            PriceHint.Trend(
                from = "324,00 TL", to = "369,00 TL", deltaPercent = 14, rising = true,
            ),
        )
        assertEquals("önce 324,00 TL", meta)
        assertFalse(meta.contains("369"), "guncel fiyat cumleye sizmis")
    }

    /**
     * BOSLUK GRUPLARI AYIRIR, NOKTA GRUP ICINI BAGLAR (karar 121).
     *
     * Bant iki grup tasiyor - beyan (`storefront + A101`) ve gozlem
     * (`BIM . bugun`) - ve aralarindaki orta nokta SILINDI. Silmenin bir
     * anlami olmasinin tek sarti gruplar arasi araligin grup ici araliktan
     * BUYUK olmasi: esitlenirlerse iki zincir adi yine tek bir dizi gibi
     * okunur ve noktayi kaldirmak hicbir sey kazandirmamis olur.
     *
     * Iki sayi da maketten (`gap:8px` / `gap:4px`); test ikisini de yaziyor
     * ama asil korudugu sey ARALARINDAKI SIRA.
     */
    @Test
    fun spaceSeparatesTheGroupsAndTheDotOnlyBindsWithinThem() {
        assertEquals(8.dp, ECONOMY_BAND_GAP, "gruplar arasi aralik maketin 8px'i degil")
        assertEquals(4.dp, DEVIATION_GAP, "grup ici aralik maketin 4px'i degil")
        assertTrue(
            ECONOMY_BAND_GAP > DEVIATION_GAP,
            "gruplar arasi aralik grup icinden buyuk degil - iki grup kaynasir",
        )
    }

    /**
     * TAZE GOZLEMIN ZINCIR ADI YERINDE DURUYOR.
     *
     * `docs/38` S1'in (b) secenegi sapma varken gozlemin zincir adini
     * dusurmeyi oneriyordu; tasarim REDDETTI. Iki ad birlikte bir celiski
     * gosteriyor - *"A101'den alacagim ama fiyati BIM'de gordum"* - ve
     * karar 119 tam o sinyali koruyor. Cumle sapmadan haberdar degil ve
     * olmamali; bu test o bilgisizligi kilitliyor.
     */
    @Test
    fun theFreshSentenceKeepsItsChainNameEvenWhenTheRowDeviates() {
        assertEquals("BİM · bugün", price(PriceHint.Single("24,90 TL", "BİM", 0)))
    }

    /** Gozlem yoksa bant hic cizilmez - "fiyat yok" da yazilmaz. */
    @Test
    fun noObservationWritesNothing() {
        assertEquals("", price(PriceHint.None))
    }

    /**
     * AMBALAJ BUYUDUYSE CUMLE "BUYUDU" DIYOR.
     *
     * Cihazda yakalandi: dal iki ambalaj FARKLIYSA atesleniyor, yonune
     * bakmadan - ama metin her zaman *"kuculdu"* yaziyordu. Kullanicinin
     * kendi verisinde `1,5 kg → 3 kg` bir buyume ve satir onu kuculme diye
     * yazdi. Satirin isi dogruyu soylemek; bir fiil yuzunden yalan soyluyordu.
     */
    @Test
    fun aGrownPackIsNotCalledShrunk() {
        val meta = price(PriceHint.PackChanged("1,5 kg", "3 kg", "192,00 TL", smaller = false))
        assertEquals("ambalaj büyüdü: 1,5 kg → 3 kg", meta)
    }

    /**
     * KUCULME HALI YERINDE DURUYOR - karar 67'nin uyarisi silinmedi.
     *
     * Shrinkflation bir fiyat dususu DEGILDIR ve satirin bunu soylemesi
     * gerekiyor; degisen tek sey, fiilin dogru yone baglanmasi.
     */
    @Test
    fun aShrunkPackStillSaysShrunk() {
        val meta = price(PriceHint.PackChanged("900 g", "800 g", "45,00 TL", smaller = true))
        assertEquals("ambalaj küçüldü: 900 g → 800 g", meta)
    }
}
