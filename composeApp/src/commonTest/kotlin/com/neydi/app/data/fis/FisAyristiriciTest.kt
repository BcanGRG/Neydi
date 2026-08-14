package com.neydi.app.data.fis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Ayristirici elde yazilmis kurallardan olusuyor, yani her kural elle test
 * edilmeli. Bu testler ODEMELI YOLDA YAZILAMAZDI: orada dogrulama ancak canli
 * API'ye karsi, ucret odeyerek ve yavas kosardi.
 */
class FisAyristiriciTest {

    @Test
    fun paraTurkceBicimdenKurusaCevriliyor() {
        assertEquals(1250, paraKurusa("12,50"))
        assertEquals(500, paraKurusa("5,00"))
        assertEquals(123456, paraKurusa("1.234,56"))
        // Binlik nokta olmadan da ayni sayi - fisler ikisini de basiyor.
        assertEquals(123456, paraKurusa("1234,56"))
        assertEquals(-500, paraKurusa("-5,00"))
    }

    /** Kurus hanesi olmayan sayi PARA DEGIL: "%1" ya da "0,850 KG" tutar sanilmamali. */
    @Test
    fun paraOlmayanSayilarReddediliyor() {
        assertNull(paraKurusa("1"))
        assertNull(paraKurusa("0,850"))
        assertNull(paraKurusa("abc"))
    }

    @Test
    fun basitFisAyristiriliyor() {
        val okuma = fisAyristir(
            listOf(
                "MIGROS TICARET A.S.",
                "TARIH 14.08.2026",
                "EKMEK %1 5,00",
                "SUT 1 L %1 32,50",
                "TOPKDV 2,45",
                "TOPLAM 37,50",
                "NAKIT 40,00",
                "PARA USTU 2,50",
            )
        )
        assertEquals("MIGROS TICARET A.S.", okuma.magazaAdi)
        assertEquals(2, okuma.satirlar.size)
        assertEquals("EKMEK", okuma.satirlar[0].ad)
        assertEquals(500, okuma.satirlar[0].tutarKurus)
        assertEquals("SUT 1 L", okuma.satirlar[1].ad)
        assertEquals(3250, okuma.satirlar[1].tutarKurus)
        assertEquals(3750, okuma.toplamKurus)
    }

    /**
     * KDV dokumu URUN DEGIL. Bu satiri urun sayarsak hem sahte bir urun
     * dogar hem de toplam tutmaz - iki hata birden.
     */
    @Test
    fun kdvSatiriUrunSayilmiyor() {
        val okuma = fisAyristir(listOf("MARKET", "EKMEK 5,00", "TOPKDV 0,45", "TOPLAM 5,00"))
        assertEquals(1, okuma.satirlar.size)
        assertEquals("EKMEK", okuma.satirlar[0].ad)
    }

    /** "TOPLAM KDV" ikisini de iceriyor; vergi tutari fis toplami sanilmamali. */
    @Test
    fun toplamKdvFisToplamiSanilmiyor() {
        val okuma = fisAyristir(listOf("MARKET", "EKMEK 5,00", "TOPLAM KDV 0,45", "TOPLAM 5,00"))
        assertEquals(500, okuma.toplamKurus)
        assertEquals(1, okuma.satirlar.size)
    }

    /** Tartili urun IKI SATIR: ad ustte, agirlik ve tutar altta. */
    @Test
    fun tartiliUrunIkiSatirdanBirlesiyor() {
        val okuma = fisAyristir(
            listOf("MARKET", "DOMATES %1", "0,850 KG x 24,90 TL/KG 21,17", "TOPLAM 21,17")
        )
        assertEquals(1, okuma.satirlar.size)
        val satir = okuma.satirlar[0]
        assertEquals("DOMATES", satir.ad)
        assertEquals(0.850, satir.miktar)
        assertEquals("kg", satir.birim)
        assertEquals(2490, satir.birimFiyatKurus)
        assertEquals(2117, satir.tutarKurus)
    }

    /** OCR termal yazicinin ince carpisini bazen yildiz okuyor. */
    @Test
    fun carpimIsaretininVaryantlariKabulEdiliyor() {
        for (isaret in listOf("x", "X", "*", "×")) {
            val okuma = fisAyristir(
                listOf("MARKET", "ELMA", "1,000 KG $isaret 30,00 TL/KG 30,00", "TOPLAM 30,00")
            )
            assertEquals(1, okuma.satirlar.size, "carpim isareti: $isaret")
            assertEquals(3000, okuma.satirlar[0].tutarKurus)
        }
    }

    /** Isareti bayrak tasiyor, sayi degil - yoksa toplama iki kez eksi girerdi. */
    @Test
    fun indirimPozitifTutarVeBayrakOlarakSaklaniyor() {
        val okuma = fisAyristir(listOf("MARKET", "EKMEK 5,00", "INDIRIM -1,00", "TOPLAM 4,00"))
        val indirim = okuma.satirlar.single { it.indirim }
        assertEquals(100, indirim.tutarKurus)
        assertTrue(indirim.indirim)
    }

    /** OCR Turkce karakter basmayabilir; iki yazim da ayni satiri bulmali. */
    @Test
    fun indirimTurkceVeAsciiYazimlaBulunuyor() {
        for (yazim in listOf("İNDİRİM", "INDIRIM", "indirim")) {
            val okuma = fisAyristir(listOf("MARKET", "EKMEK 5,00", "$yazim -1,00", "TOPLAM 4,00"))
            assertEquals(1, okuma.satirlar.count { it.indirim }, "yazim: $yazim")
        }
    }

    @Test
    fun odemeVeKunyeSatirlariEleniyor() {
        val okuma = fisAyristir(
            listOf(
                "A101",
                "FIS NO 0042",
                "EKMEK 5,00",
                "NAKIT 10,00",
                "PARA USTU 5,00",
                "TESEKKUR EDERIZ",
                "TOPLAM 5,00",
            )
        )
        assertEquals(1, okuma.satirlar.size)
        assertEquals("EKMEK", okuma.satirlar[0].ad)
    }

    // --- Aritmetik kapisi ---------------------------------------------------

    @Test
    fun aritmetikTutanFisiOnayliyor() {
        val okuma = fisAyristir(listOf("MARKET", "EKMEK 5,00", "SUT 32,50", "TOPLAM 37,50"))
        assertEquals(true, aritmetikTutuyorMu(okuma))
    }

    /** Kacirilan satir toplami bozar - kapinin butun varlik sebebi bu. */
    @Test
    fun eksikSatirYakalaniyor() {
        val okuma = FisOkumasi(
            magazaAdi = null,
            satirlar = listOf(FisSatiri(hamMetin = "EKMEK 5,00", ad = "EKMEK", tutarKurus = 500)),
            toplamKurus = 3750,
            hamSatirlar = emptyList(),
        )
        assertEquals(false, aritmetikTutuyorMu(okuma))
    }

    /** Tartili urun yuvarlamasi 5 kurusa kadar oynatabilir; bu fis TUTARSIZ degil. */
    @Test
    fun besKurusTolerans() {
        val okuma = FisOkumasi(
            magazaAdi = null,
            satirlar = listOf(FisSatiri(hamMetin = "", ad = "DOMATES", tutarKurus = 2117)),
            toplamKurus = 2120,
            hamSatirlar = emptyList(),
        )
        assertEquals(true, aritmetikTutuyorMu(okuma))
    }

    @Test
    fun toleransDisiReddediliyor() {
        val okuma = FisOkumasi(
            magazaAdi = null,
            satirlar = listOf(FisSatiri(hamMetin = "", ad = "DOMATES", tutarKurus = 2117)),
            toplamKurus = 2130,
            hamSatirlar = emptyList(),
        )
        assertEquals(false, aritmetikTutuyorMu(okuma))
    }

    @Test
    fun indirimToplamdanDusuluyor() {
        val okuma = fisAyristir(
            listOf("MARKET", "EKMEK 5,00", "SUT 32,50", "INDIRIM -2,50", "TOPLAM 35,00")
        )
        assertEquals(true, aritmetikTutuyorMu(okuma))
    }

    /** "Dogrulanamadi" ile "tutmadi" AYRI SEYLER - null bunu tasiyor. */
    @Test
    fun toplamOkunamadiysaNullDonuyor() {
        val okuma = fisAyristir(listOf("MARKET", "EKMEK 5,00"))
        assertNull(aritmetikTutuyorMu(okuma))
    }

    /** Gercekci bir fis: tartili urun, KDV dokumu, indirim ve odeme bir arada. */
    @Test
    fun gercekciFisUctenUcteTutuyor() {
        val okuma = fisAyristir(
            listOf(
                "MIGROS TICARET A.S.",
                "SUBE 0421  KASA 03",
                "TARIH 14.08.2026 SAAT 18:42",
                "DOMATES %1",
                "0,850 KG x 24,90 TL/KG 21,17",
                "EKMEK 200 GR %1 8,50",
                "SUT 1 L %1 32,50",
                "PEYNIR 500 GR %8 149,90",
                "INDIRIM -12,07",
                "TOPKDV 18,44",
                "TOPLAM 200,00",
                "KREDI KARTI 200,00",
                "TESEKKUR EDERIZ",
            )
        )
        assertEquals("MIGROS TICARET A.S.", okuma.magazaAdi)
        assertEquals(4, okuma.satirlar.count { !it.indirim })
        assertEquals(1, okuma.satirlar.count { it.indirim })
        assertEquals(20000, okuma.toplamKurus)
        assertEquals(true, aritmetikTutuyorMu(okuma))
    }
}
