package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Uc birim kanonunun okuma tarafinda uzlasmasi (karar 96'nin on kosulu).
 *
 * ## Neden bu tablonun testi var
 *
 * Kural olculmus bir tehlikeye karsi yazildi: kullanicinin aktif listesindeki
 * **bes fiyatli satirin sifirinda** satir birimi ile ambalaj birimi ayni dizge.
 * Ham bir bolme, bugunku veride bile `2 g Cay`yi `1 kg` ambalaja karsi iki
 * pakete cevirir - ve `500 g` olsaydi bes yuz pakete.
 *
 * Yani bu dosya bir bicimlendirme testi degil, bir **1000x hata** nobetcisi.
 */
class UnitScaleTest {

    /** Iki yazim da kutle: katalogun "g"si ile OCR'in "gr"si ayni sey. */
    @Test
    fun gramsAndKilosAreTheSameKind() {
        assertEquals(UnitKind.MASS, unitKind("g"))
        assertEquals(UnitKind.MASS, unitKind("gr"))
        assertEquals(UnitKind.MASS, unitKind("kg"))
        assertEquals(UnitKind.MASS, unitKind("KG"))
    }

    /** Katalogun "L"si ile OCR'in "lt"si de ayni sey - ve "ml" onlarla ayni ailede. */
    @Test
    fun litresSpelledTwoWaysAreOneKind() {
        assertEquals(UnitKind.VOLUME, unitKind("L"))
        assertEquals(UnitKind.VOLUME, unitKind("lt"))
        assertEquals(UnitKind.VOLUME, unitKind("ml"))
    }

    /**
     * ASIL SAYI: 1 kg = 1000 g. Kanonlar farkli yazsa da kat ayni.
     *
     * Bu satir olmasaydi `2 g` ile `1 kg` bolusur ve iki paket cikardi.
     */
    @Test
    fun aKiloIsAThousandGramsAcrossCanons() {
        assertEquals(1_000.0, convertMagnitude(1.0, "kg", "g"))
        assertEquals(1_000.0, convertMagnitude(1.0, "kg", "gr"))
        assertEquals(0.5, convertMagnitude(500.0, "gr", "kg"))
        assertEquals(1_500.0, convertMagnitude(1.5, "lt", "ml"))
    }

    /**
     * TURLER BIRBIRINE CEVRILMIYOR.
     *
     * Kilogramı adete bolmek bir sayi uretir ama o sayi hicbir sey degildir.
     * `null` "cevabim yok" diyor; sifir donseydi bir CEVAP gibi okunurdu.
     */
    @Test
    fun kindsNeverConvertIntoEachOther() {
        assertNull(convertMagnitude(1.0, "kg", "adet"))
        assertNull(convertMagnitude(1.0, "L", "g"))
    }

    /**
     * SAYILAN BIRIMLER ARASINDA DA KAT YOK.
     *
     * Paket ile kutu ayni seyin iki olcegi degil, iki ayri sey - "3 paket" ile
     * "3 kutu" arasinda bir carpan aramak anlamsiz.
     */
    @Test
    fun countedUnitsHaveNoRatioBetweenThem() {
        assertEquals(3.0, convertMagnitude(3.0, "adet", "adet"))
        assertNull(convertMagnitude(3.0, "paket", "kutu"))
    }

    /**
     * TANINMAYAN KELIME SAYILAN SAYILMIYOR.
     *
     * Bilinmeyeni "adet" kabul etmek onu CARPILABILIR yapardi - yani en riskli
     * varsayim. Cagiran taraf `null`i ambalaji bilinmeyen satir gibi ele
     * alabiliyor.
     */
    @Test
    fun anUnknownWordIsNotQuietlyCounted() {
        assertNull(unitKind("kavanoz"))
        assertNull(convertMagnitude(1.0, "kavanoz", "adet"))
    }
}
