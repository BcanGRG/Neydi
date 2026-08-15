package com.neydi.app.data.receipt

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Yon puanlayicisi (F4.14b) - ARTIK TEST EDILEBILIR.
 *
 * Eski hali `androidMain`'deydi ve commonTest'ten erisilemiyordu (F10.13);
 * girdisi yalnizca metin satirlari oldugu icin orada olmasinin bir sebebi de
 * yoktu. Tasindi.
 *
 * Girdiler GERCEK: BIM satirlari `ReceiptProcessorTest`teki cihaz ciktisi,
 * AKYURT satirlari `AkyurtLayoutTest`teki `rawOcrText` dokumu.
 */
class ReadingScoreTest {

    private val bimLines = listOf(
        "BIM BIRLESIK MAGAZALAR A.S.",
        "13.08.2026 18:49 Sira No : 218",
        "2 ad X 53.00",
        "KREMA 18YAĞLI 200ML %1. *106.00",
        "TURŞU KORNI ŞON 670G 21. *84.50",
        "ALIŞVERIŞ POŞETi BiM 220 *1.00",
        "GOFRET FIND KREM142G %1. *34.00",
        "TOPLAM KDV *2.39",
        "Odenecek KDV Dahil Tutar *225.50",
        "Banka Kredi Kartı (1) *225.50",
    )

    /** AKYURT'un kalemli satirlari - HEPSI RAKAMLA BASLIYOR. */
    private val akyurtLines = listOf(
        "42 2980889 1,764 Kg 99,95 %01 176,31 t",
        "TAVUK SENPILİÇ POŞETLİ KG",
        "43 8690576029431 1 Adet 28,50 %01 28,50",
        "ANKARA MAK.500 GR.KALEM",
        "44 8690576029257 1 Adet 28,50 %01 28,50",
        "ANKARA MAK,500 GR. MANTI",
    )

    /**
     * ESKI PUANLAYICININ KOR NOKTASI.
     *
     * Eski olcut *"solunda metin, sagında tutar"* olan satirlari sayiyordu ve
     * **rakamla baslayan satirlari hic saymiyordu**. AKYURT'ta her urun satiri
     * sira numarasiyla, yani rakamla basliyor - o fisler SIFIR puan aliyor ve
     * yon secimi rastgeleye donuyordu. Cihazda goruldu: ayni fotograf pes pese
     * okumada bir 19 satir, bir 3 satir, bir hic verdi.
     */
    @Test
    fun itemisedLayoutScoresAboveZero() {
        assertTrue(score(akyurtLines) > 0, "AKYURT duzeni puan almiyor: ${score(akyurtLines)}")
    }

    /** Klasik duzen de puanlaniyor - tasima kimseyi kaybetmedi. */
    @Test
    fun classicLayoutStillScores() {
        assertTrue(score(bimLines) > 0)
    }

    /**
     * DOGRU YON YANLIS YONDEN IYI PUAN ALMALI.
     *
     * Yanlis yonun temsili: gorsel gruplama bozulunca tutarlar adlarindan
     * kopar ve satirlar tek tek kalir. Puanlayicinin isi tam olarak bu iki
     * hali ayirmak.
     */
    @Test
    fun scatteredRowsScoreLowerThanGroupedOnes() {
        val scattered = listOf(
            "KREMA 18YAĞLI 200ML", "TURŞU KORNI ŞON 670G", "ALIŞVERIŞ POŞETi BiM",
            "*106.00", "*84.50", "*1.00",
        )
        assertTrue(
            score(bimLines) > score(scattered),
            "gruplu ${score(bimLines)} vs dagilmis ${score(scattered)}",
        )
    }
}
