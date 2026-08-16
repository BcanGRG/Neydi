package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tahmin bicimi (gezinme sozlesmesi · formats).
 *
 * Uygulamada **kesin tutar diye bir veri yok** - her tutar gozlemden
 * hesaplaniyor. Bu testin isirdigi yer o kural: tahmin tilde tasiyor ve kurus
 * yazmiyor.
 */
class EstimateFormatTest {

    @Test
    fun tildeIsAttachedAndKurusIsDropped() {
        assertEquals("~642 TL", formatEstimate(64_200))
        assertEquals("~640 TL", formatEstimate(64_000))
    }

    /** Yuvarlama EN YAKINA: asagi yuvarlamak tahmini sistematik dusuk gosterirdi. */
    @Test
    fun roundsToNearestLira() {
        assertEquals("~643 TL", formatEstimate(64_250))
        assertEquals("~642 TL", formatEstimate(64_249))
        assertEquals("~1 TL", formatEstimate(50))
        assertEquals("~0 TL", formatEstimate(49))
    }

    /**
     * Binlik ayirici NOKTA, tahmin biciminde de.
     *
     * `1.085,65` -> `~1.086`, `~1.085` DEGIL: yuvarlama once, kesme sonra.
     * Ilk yazilisinda bu test kesin bicimin ornegini kopyalayip kurusu
     * unutmustu ve KOD DOGRUYDU - tam da "kendi ornegiyle kendini onaylama"
     * hatasinin tersi, testin yanlis oldugu hal.
     */
    @Test
    fun groupsThousands() {
        assertEquals("~1.086 TL", formatEstimate(108_565))
        assertEquals("~12.345 TL", formatEstimate(1_234_500))
    }

    @Test
    fun currencyCanBeDropped() {
        assertEquals("~642", formatEstimate(64_200, currency = ""))
    }

    /**
     * KESIN BICIM DEGISMEDI: `formatMinor` hala kurus yaziyor ve tilde
     * tasimiyor. Ikisi ayri fonksiyon olmasinin sebebi bu - cagri yerinde
     * hangi iddianin tasindigi gorunuyor.
     */
    @Test
    fun exactFormatIsUnchanged() {
        assertEquals("1.085,65 TL", formatMinor(108_565))
        assertEquals("642,50 TL", formatMinor(64_250))
    }
}
