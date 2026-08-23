package com.neydi.app.ui.components

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Ekonomi bandinin tek feda kurali (karar 104).
 *
 * ## Neden bu aritmetigin testi var
 *
 * Once bes uyeli bir feda sirasi vardi ve kendi gerekcesini ihlal ediyordu:
 * 360dp'de adet rozeti bile dusuyordu - yani *"yanlis adedin bedeli parayla
 * odenir"* diyen kural, adedi siliyordu. Karar 102 anatomiyi bolerek yarisi
 * kaldirdi; geriye tek bir soru kaldi ve o sorunun cevabi hala bir SOZ:
 * *"cumle tam kalir."*
 *
 * Yazili bir sozun nobetcisi yoksa bir sonraki bilesen degisiminde sessizce
 * bozulur - eskisi tam olarak boyle bozulmustu.
 */
class RowBudgetTest {

    /** Tasarimin 360dp olcumu: cip dusuldukten sonra banda kalan. */
    private val bandAt360 = 186.dp

    /** *"Fiyat 92dp + delta 46dp, metaya 140dp kaliyor."* */
    private val delta = 46.dp

    /** *"«onceki 324,00 TL» 92dp."* */
    private val shortMeta = 92.dp

    /** *"Altta 30 karakterlik meta"* - ambalaj cumlesi bu uzunlukta. */
    private val longMeta = 170.dp

    /**
     * 360dp'DE KISA META ILE DELTA BIRLIKTE YASIYOR.
     *
     * Tasarimin maketi ust satirda ikisini birden ciziyor ve altini ciziyor:
     * *"alti oge birden ... hicbiri dusmuyor."*
     */
    @Test
    fun aShortSentenceLeavesRoomForTheDelta() {
        assertTrue(deltaSurvives(bandAt360, metaWidth = shortMeta, deltaWidth = delta))
    }

    /**
     * UZUN CUMLE DELTAYI DUSURUR - TERSI DEGIL.
     *
     * Kararin kendi cumlesi: *"yalniz delta duser, cumle tam kalir."* Delta
     * bir OZET; ozetin kendisi zaten metanin icinde yasiyor ("onceki 324,00
     * TL" ile guncel fiyat yan yana duruyor). Cumleyi kirpip ozeti tutmak,
     * bilgiyi atip susu tutmak olurdu.
     */
    @Test
    fun aLongSentenceDropsTheDelta() {
        assertFalse(deltaSurvives(bandAt360, metaWidth = longMeta, deltaWidth = delta))
    }

    /**
     * BOS BAND: meta yoksa delta her zaman yasar.
     *
     * `Trend` dalinda meta *"onceki ..."* yaziyor, ama `cheaperElsewhere`
     * bastirildiginda ya da band yalnizca delta tasidiginda genislik sifir
     * olur. Sifirin delta dusurmesi anlamsiz olurdu.
     */
    @Test
    fun anEmptyBandAlwaysKeepsTheDelta() {
        assertTrue(deltaSurvives(bandAt360, metaWidth = 0.dp, deltaWidth = delta))
    }

    /**
     * DAR EKRAN: band kucuduginde ayni kisa cumle bile deltayi dusurur.
     *
     * Kural bir esik degil bir KARSILASTIRMA - bu yuzden ekran genisligi
     * degistiginde cevap da degisiyor. Sabit bir taban koysaydik 320dp'lik
     * cihazda cumle kirpilir, delta kalirdi.
     */
    @Test
    fun aNarrowerBandDropsTheDeltaEvenForAShortSentence() {
        assertFalse(deltaSurvives(130.dp, metaWidth = shortMeta, deltaWidth = delta))
    }
}
