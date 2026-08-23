package com.neydi.app.ui.components

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Satir butcesi ve feda sirasi (karar 80).
 *
 * ## Neden bu aritmetigin testi var
 *
 * Kodun kendi kurali bastan beri *"ad kirpilmasi kabul edilemez"* diyordu ve
 * TUTMUYORDU - olcum 411dp'de ada %35, 360dp'de dokuz karakter kaldigini
 * gosterdi. Kural artik yazili ve sayilarla: taban 120dp, feda sirasi bes
 * uyeli. Yazili bir kuralin nobetcisi yoksa bir sonraki bileşen degisiminde
 * yine sessizce bozulur.
 */
class RowBudgetTest {

    /** Olculmus tipik maliyetler: oge genisligi + ondan onceki bosluk. */
    private val pin = 20.dp
    private val wideBadge = 84.dp // "1,5 kg"
    private val narrowBadge = 56.dp // "2x"
    private val avatar = 32.dp
    private val delta = 61.dp
    private val sparkline = 30.dp

    private fun costs(
        badge: androidx.compose.ui.unit.Dp? = null,
        withPin: Boolean = false,
        withAvatar: Boolean = false,
        withTrend: Boolean = false,
    ) = buildMap {
        if (withTrend) {
            put(RowElement.Sparkline, sparkline)
            put(RowElement.DeltaChip, delta)
        }
        if (withAvatar) put(RowElement.PartnerAvatar, avatar)
        if (withPin) put(RowElement.StaplePin, pin)
        badge?.let { put(RowElement.QuantityBadge, it) }
    }

    /** Yer bolsa HICBIR SEY dusmuyor - kural bir kisitlama degil, bir taban. */
    @Test
    fun nothingIsSacrificedWhenThereIsRoom() {
        val alive = survivingElements(
            available = 400.dp,
            costs = costs(narrowBadge, withPin = true, withAvatar = true),
        )
        assertEquals(3, alive.size)
    }

    /**
     * SIRA TASARIMIN YAZDIGI SIRA: once sparkline, sonra delta, sonra avatar.
     *
     * Gerekcesi bilgi degeri: *"sparkline sus, delta ozeti metada da yasar,
     * avatar baglam."* Sira bozulursa satir once TASIDIGI BILGIYI kaybeder,
     * susunu degil.
     */
    @Test
    fun theSacrificeOrderIsSparklineThenDeltaThenAvatar() {
        val all = costs(wideBadge, withPin = true, withAvatar = true, withTrend = true)
        // Her adimda bir oge daha dussun diye genisligi kademeli daraltiyoruz.
        val wide = survivingElements(340.dp, all)
        val tighter = survivingElements(300.dp, all)
        val tightest = survivingElements(250.dp, all)

        assertTrue(RowElement.Sparkline !in wide, "once sparkline dusmeliydi")
        assertTrue(RowElement.DeltaChip in wide, "delta sparkline'dan once dusmemeli")
        assertTrue(RowElement.DeltaChip !in tighter, "sirada delta vardi")
        assertTrue(RowElement.PartnerAvatar !in tightest, "sirada avatar vardi")
    }

    /**
     * 360dp + GENIS ROZET: tasarimin maketinin birebir yazdigi hal.
     *
     * *"76dp'lik «1,5 kg» tabana hic sigmaz: feda 3-4-5 isler (avatar,
     * raptiye, rozet)."* Yani bu genislikte adet rozeti bile dusuyor - feda
     * sirasinin SONUNCU uyesi, cunku ondan once dusecek bir sey kalmiyor.
     */
    @Test
    fun aWideBadgeOnANarrowScreenCostsEverythingElse() {
        // 360dp ekran: 360 - 32 (dolgu) - 36 (onay + bosluk) - 100 (fiyat
        // cipi + bosluk) = 192dp. Cip butcenin KONUSU DEGIL, cunku dusmuyor.
        val alive = survivingElements(
            available = 192.dp,
            costs = costs(wideBadge, withPin = true, withAvatar = true),
        )
        assertEquals(emptySet(), alive, "uc oge de dusmeliydi")
        // Ve geriye kalan ad genisligi maketin yazdigi sayi: 192dp.
    }

    /**
     * AD TABANI HER ZAMAN KORUNUYOR - ogeler bittigi hal HARIC.
     *
     * Hepsi dustugu halde taban saglanmiyorsa geriye ad ile fiyat cipi kaliyor:
     * ikisi de dusmuyor, ad kirpiliyor. O hal bir kural ihlali degil, ekranin
     * fiziksel siniri - ve fonksiyon onu sessizce dogru yapiyor: bos kume.
     */
    @Test
    fun theNameFloorIsHonouredUntilNothingIsLeftToDrop() {
        val all = costs(wideBadge, withPin = true, withAvatar = true, withTrend = true)
        listOf(400, 340, 300, 260, 200, 140).forEach { width ->
            val alive = survivingElements(width.dp, all)
            val used = alive.fold(0f) { acc, e -> acc + all.getValue(e).value }
            assertTrue(
                alive.isEmpty() || width - used >= NAME_FLOOR.value,
                "$width dp: ad tabani korunmadi (kalan ${width - used}dp)",
            )
        }
    }

    /** Cizilmeyen oge feda edilemez - listeye hic girmiyor. */
    @Test
    fun anAbsentElementIsNeverCounted() {
        val alive = survivingElements(200.dp, costs(badge = null, withPin = true))
        assertTrue(RowElement.QuantityBadge !in alive)
    }
}
