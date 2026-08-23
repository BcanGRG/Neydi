package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Miktarin yazili hali (karar 103, 109).
 *
 * ## Neden `ui/list` disina tasindi
 *
 * Iddialar `ListStateTest` icindeydi, cunku bicim once yalnizca liste
 * satirinin isiydi. Karar 107-109 ayni sayiyi uc yuzeye yayinca bicim de
 * `data`ya tasindi ve iddialari birlikte geldi - ayri kalsalardi bicim
 * degistiginde nobetci baska bir katmanda kalirdi.
 */
class QuantityFormatTest {

    /**
     * ADET 1 + BIRIM "adet" -> YALIN "1". Rozet ARTIK HER SATIRDA (karar 103).
     *
     * Bu iddia tam tersine cevrildi ve gerekcesi kayda deger: eski kural
     * (*"her satira 1x yazmak gurultu"*) rozet yalnizca OKUNAN bir sey oldugu
     * surece dogruydu. Karar 107 onu miktarin DUZENLENDIGI yer yapinca
     * cizilmeyen rozet, duzenlenemeyen miktar anlamina gelmeye basladi -
     * kullanicinin sikayeti zaten tam buydu.
     *
     * Gurultu itirazi susturarak degil kucultererek cozuldu: "1x" degil yalin
     * "1", ve rozet dolgusuz cizilir (bkz. `quantityModified`).
     */
    @Test
    fun theDefaultQuantityStillDrawsABareBadge() {
        assertEquals("1", quantityLabel(1.0, "adet"))
    }

    @Test
    fun quantityLabels() {
        assertEquals("2x", quantityLabel(2.0, "adet"))
        assertEquals("1 kg", quantityLabel(1.0, "kg"))
        assertEquals("500 g", quantityLabel(500.0, "g"))
    }

    /**
     * TARTILI URUNDE BIRIM GORUNMELI: "0,5" tek basina anlamsiz.
     *
     * Ondalik VIRGULE ceviriliyor (Turkce yazim) ve uc hane korunuyor -
     * tartida gercekten uc hane cikiyor.
     *
     * BU IDDIALAR SILINEN `quantityBadge`'DEN TASINDI (F11.20): o fonksiyon
     * `ui/finish/` icinde yasiyordu ve bunun neredeyse birebir kopyasiydi.
     * Ekran silinirken iddialari birlikte gitmesin diye buraya alindi -
     * silinen tek sey `"ad"` kisaltmasi dali, cunku `normalizeUnit` onu
     * sinirda zaten `"adet"`e ceviriyor; o dal savunma amacli kopyaydi.
     */
    @Test
    fun weighedItemsShowTheirUnit() {
        assertEquals("0,5 kg", quantityLabel(0.5, "kg"))
        assertEquals("0,182 kg", quantityLabel(0.182, "kg"))
        assertEquals("1 L", quantityLabel(1.0, "L"))
    }

    /** Turkce ondalik VIRGUL. Kotlin varsayilani nokta uretir. */
    @Test
    fun decimalComma() {
        assertEquals("1,5 kg", quantityLabel(1.5, "kg"))
        assertEquals("0,5 L", quantityLabel(0.5, "L"))
    }

    /**
     * ISARETLI KESIF HUCRESI MIKTARI YAZIYOR (karar 109).
     *
     * Karar 12 hucreyi pasif yapti; pasif hucre o gune kadar yalnizca *"bu
     * zaten listede"* diyordu. Miktarin kendi evi olunca "ne kadar" sorusu da
     * cevaplanabilir hale geldi - ve cevabi hucrenin kendisi veriyor.
     */
    @Test
    fun theMarkedCellSaysHowMuchIsOnTheList() {
        assertEquals("1 kg listede", inListLabel(1.0, "kg"))
        assertEquals("2x listede", inListLabel(2.0, "adet"))
        assertEquals("1 listede", inListLabel(1.0, "adet"))
    }
}
