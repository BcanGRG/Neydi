package com.neydi.app.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Sayacin adimi ve alt siniri (karar 109).
 *
 * ## Neden bu aritmetigin testi var
 *
 * Adim UC YUZEYDE birden kullaniliyor - satirdaki hizli sayac, Urun
 * Detayi'ndaki miktar blogu ve kesif sheet'indeki hucre sayaci. Ucu ayri ayri
 * hesaplasaydi biri digerinden ayrilir ve kullanici ayni urunu iki yerden
 * farkli adimlarla artirirdi. Tek kaynak var, ve tek kaynagin nobetcisi bu.
 */
class QuantityStepTest {

    /** Tasarimin yazdigi dort adim, birebir. */
    @Test
    fun theStepsAreTheOnesTheDesignWrote() {
        assertEquals(1.0, stepFor("adet"))
        assertEquals(0.5, stepFor("kg"))
        assertEquals(100.0, stepFor("g"))
        assertEquals(0.5, stepFor("L"))
    }

    /**
     * SAYILAN HER BIRIM 1'ER ARTIYOR - yalniz "adet" degil.
     *
     * Kural "adet ise 1" diye yazilsaydi paket/kutu/demet/sise 0,5'er artardi
     * ve yarim paket makarna onerilirdi. `parseQuantity` bu dort birimi zaten
     * taniyor, yani satirlarda gercekten cikiyorlar.
     */
    @Test
    fun everyCountedUnitStepsByOne() {
        listOf("paket", "kutu", "demet", "şişe").forEach {
            assertEquals(1.0, stepFor(it), "$it sayilan bir birim")
        }
    }

    /** Artirma duz toplama - ust sinir yok. */
    @Test
    fun incrementAddsOneStep() {
        assertEquals(1.5, incrementQuantity(1.0, "kg"))
        assertEquals(3.0, incrementQuantity(2.0, "adet"))
        assertEquals(600.0, incrementQuantity(500.0, "g"))
    }

    /** Azaltma da duz cikarma - taban asilmadigi surece. */
    @Test
    fun decrementSubtractsOneStep() {
        assertEquals(1.5, decrementQuantity(2.0, "kg"))
        assertEquals(2.0, decrementQuantity(3.0, "adet"))
    }

    /**
     * TABAN BIR ADIM VE SIFIRA ASLA INMIYOR.
     *
     * *"Silme ayri bir eylemdir"* - kaydirarak sil + snackbar. Sifira inebilen
     * bir sayac, geri alinabilir bir silmeyi geri alinamaz bir "sifir adet"
     * satirina cevirirdi.
     */
    @Test
    fun theFloorIsOneStepNeverZero() {
        assertEquals(1.0, decrementQuantity(1.0, "adet"))
        assertEquals(0.5, decrementQuantity(0.5, "kg"))
        assertEquals(100.0, decrementQuantity(100.0, "g"))
    }

    /**
     * EKSI TUSU ASLA ARTIRMAZ - taban altindaki deger YERINDE KALIR.
     *
     * Bu kural cihazda bulundu. Once taban duz donuyordu, yani zaten tabanin
     * altinda olan bir deger eksiye basildiginda YUKARI cikiyordu: katalogda
     * "1 g" Cay var ve eksi tusu onu 100 g yapiyordu. Tabanin isi asagi
     * inmeyi durdurmak, yukari itmek degil.
     */
    @Test
    fun minusNeverIncreases() {
        assertEquals(1.0, decrementQuantity(1.0, "g"), "eksi tusu sayiyi artirdi")
        assertEquals(0.182, decrementQuantity(0.182, "kg"), "eksi tusu sayiyi artirdi")
    }

    /** Taban USTUNDEKI deger yine tabana iniyor - iki adim atlamadan. */
    @Test
    fun aValueAboveTheFloorStillLandsOnIt() {
        assertEquals(100.0, decrementQuantity(150.0, "g"))
        assertEquals(0.5, decrementQuantity(0.9, "kg"))
    }
}
