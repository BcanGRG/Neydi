package com.neydi.app.ui.components

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
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
     * Bandin IC GENISLIGI 360dp'lik cihazda.
     *
     * [bandAt360] bunun fiyat cipi ve araligi dusulmus hali - yani
     * `286 - 92 - 8 = 186`. Iki sayi ayni olcumun iki ucu; birini
     * degistirip otekini birakmak testi kendi icinde yalanci yapar.
     */
    private val bandWidthAt360 = 286.dp

    /** Fiyat cipinin sabit sutunu (karar 104). */
    private val priceColumn = 92.dp

    /**
     * Sapma isaretinin olculen genisligi: `storefront 14 + 4 + "A101"`.
     *
     * "A101" 13sp/600'de yaklasik 30dp - yani isaret 48dp. Uzun zincir
     * adlari ("Tarim Kredi") bunun cok uzerine cikiyor ve butce onlari da
     * dogru saymak zorunda; test kisa olani seciyor cunku kural KISA adda
     * bile kendini gosteriyor.
     */
    private val deviationMark = 48.dp

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
     * BUTCEDEN DUSMEYEN HER UYE, KENDI ARALIGIYLA BIRLIKTE INIYOR.
     *
     * Araligi unutmak uyeyi hic saymamanin yarisi kadar yanlis: bandin
     * `spacedBy(8dp)`'si o bosluklari GERCEKTEN ciziyor, yani meta onlari
     * kullanamiyor. Uc hal de ayri yaziliyor cunku uc ayri toplama.
     */
    @Test
    fun everyFixedMemberBringsItsOwnGap() {
        assertEquals(
            bandWidthAt360 - priceColumn - 8.dp,
            deltaBudget(bandWidthAt360, priceColumn = priceColumn, deviationMark = null),
        )
        assertEquals(
            bandWidthAt360 - deviationMark - 8.dp,
            deltaBudget(bandWidthAt360, priceColumn = null, deviationMark = deviationMark),
        )
        assertEquals(
            bandWidthAt360 - priceColumn - deviationMark - 16.dp,
            deltaBudget(bandWidthAt360, priceColumn = priceColumn, deviationMark = deviationMark),
        )
    }

    /** Dusmeyen uye yoksa bant genisligini oldugu gibi veriyor. */
    @Test
    fun aBandWithNoFixedMembersKeepsItsWholeWidth() {
        assertEquals(
            bandWidthAt360,
            deltaBudget(bandWidthAt360, priceColumn = null, deviationMark = null),
        )
    }

    /**
     * BU DOSYANIN 186dp'SI DELTA BUTCESININ KENDISI.
     *
     * Asagidaki dort test o sayiyi elle yaziyor; burasi onu bandin gercek
     * genisliginden TUReTIYOR. Ikisi ayrilirsa testler yesil kalir ve yine
     * de yanlis seyi olcerler.
     */
    @Test
    fun theChipAndItsGapAreWhatMakeTheBandOneHundredEightySix() {
        assertEquals(
            bandAt360,
            deltaBudget(bandWidthAt360, priceColumn = priceColumn, deviationMark = null),
        )
    }

    /**
     * SAPMA ISARETI TEK BASINA DELTAYI DUSURUYOR (karar 121).
     *
     * Karar 118 isareti banda soktu ama butce onu SAYMIYORDU - isaret
     * `weight`li grubun icindeydi ve bant duzeyindeki hesap yalnizca fiyat
     * cipini biliyordu. Karar 121 iki grubu kardes yapinca eksiklik gorunur
     * hale geldi: ayni kisa cumle, ayni ekran, ama bandin 56dp'si zaten
     * gitmis oluyor ve delta yine de "sigiyorum" diyordu. Kirpilan sey
     * cumleydi - karar 104'un tek yasagi.
     *
     * TESTIN ISIRDIGI YER: sapma bacagini [deltaBudget]'ten cikarin, ikinci
     * iddia duser.
     */
    @Test
    fun theDeviationMarkAloneCanDropTheDelta() {
        val plain = deltaBudget(bandWidthAt360, priceColumn = priceColumn, deviationMark = null)
        val deviant = deltaBudget(
            bandWidthAt360,
            priceColumn = priceColumn,
            deviationMark = deviationMark,
        )
        assertTrue(
            deltaSurvives(plain, metaWidth = shortMeta, deltaWidth = delta),
            "isaretsiz satirda delta yasamaliydi",
        )
        assertFalse(
            deltaSurvives(deviant, metaWidth = shortMeta, deltaWidth = delta),
            "sapma isareti butceden dusulmemis - cumle kirpilacak",
        )
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
