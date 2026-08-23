package com.neydi.app.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liste satirinda yer yetmediginde DUSEN ogeler ve sirasi (karar 80).
 *
 * Sira bilgi degerine gore ve tasarimin gerekcesi bunu tek tek yaziyor:
 * *"sparkline sus, delta ozeti metada da yasar, avatar baglam, raptiye bolum
 * basliginin tekrari; rozet ise miktar - yanlis adedin bedeli parayla
 * odenir."*
 *
 * FIYAT CIPI VE AD LISTEDE YOK: ikisi de asla dusmuyor.
 */
enum class RowElement {
    Sparkline,
    DeltaChip,
    PartnerAvatar,
    StaplePin,
    QuantityBadge,
}

/**
 * Adin garanti edilen taban genisligi (karar 80): ~13 karakter.
 *
 * Kodun kendi kurali bastan beri *"ad kirpilmasi kabul edilemez - fiyat ipucu
 * yardimci bilgi, ad ise satirin varlik sebebi"* diyordu ve TUTMUYORDU: 411dp
 * ekranda ada %35 kaliyordu, 360dp'de dokuz karakter. Taban tek basina kimin
 * dusecegini soylemiyor, sira tek basina adin ne kadar korunacagini; ikisi
 * birlikte tam kural.
 */
val NAME_FLOOR: Dp = 120.dp

/**
 * Yer yetmediginde HANGI ogeler cizilecek.
 *
 * ## Neden saf bir fonksiyon
 *
 * Karar 80'in aritmetigi bir yerlesim ayrintisi degil bir SOZLESME: 360dp'lik
 * bir cihazda adin 13 karakterin altina inmemesi soz verilmis bir sey. Saf
 * fonksiyon o sozu Compose kurmadan sinanabilir kiliyor - ve maketin verdigi
 * sayilar (411dp'de ad 135dp, 360dp'de 192dp) dogrudan test olarak yazilabiliyor.
 *
 * ## Nasil calisiyor
 *
 * Once her sey cizilmis varsayiliyor; ad tabani saglanana kadar [RowElement]
 * sirasiyla ogeler dusuruluyor. Hepsi dustugu halde taban hala saglanmiyorsa
 * geriye ad ile fiyat cipi kaliyor - ikisi de dusmuyor, ad kirpiliyor. O hal
 * bir kural ihlali degil, ekranin fiziksel siniri.
 *
 * @param available adin ve feda edilebilir ogelerin PAYLASTIGI genislik.
 *   Satirin ic genisliginden **onay hedefi ve fiyat cipi dusulmus** olmali -
 *   ikisi de asla dusmuyor, yani butcenin konusu degiller. Fiyat cipini
 *   unutmak, tasarimin kendi maket sayilarini tutturamamak demek: 360dp'lik
 *   cihazda cipsiz hesap "hicbir sey dusmesin" derken maket uc ogenin birden
 *   dustugunu yaziyor.
 * @param costs her ogenin genisligi + ondan onceki bosluk. Sifir olan oge
 *   zaten cizilmiyor demektir ve listeye girmez.
 */
fun survivingElements(available: Dp, costs: Map<RowElement, Dp>): Set<RowElement> {
    val present = costs.filterValues { it > 0.dp }
    val alive = present.keys.toMutableSet()
    // SIRA `RowElement`IN KENDI SIRASI: enum'un beyan sirasi feda sirasidir ve
    // ikisini ayirmak, birini degistirip otekini unutmaya davet olurdu.
    for (element in RowElement.entries) {
        if (available - alive.totalWidth(present) >= NAME_FLOOR) break
        alive.remove(element)
    }
    return alive
}

private fun Set<RowElement>.totalWidth(costs: Map<RowElement, Dp>): Dp =
    fold(0.dp) { acc, e -> acc + (costs[e] ?: 0.dp) }
