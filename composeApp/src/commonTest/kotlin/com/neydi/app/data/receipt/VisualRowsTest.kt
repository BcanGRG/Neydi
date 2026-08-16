package com.neydi.app.data.receipt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Gorsel satir gruplamasinin YONDEN BAGIMSIZ oldugunu isiran testler.
 *
 * NEDEN VAR: bu katman projenin en pahali sessiz hatasini uretti - fis yan
 * cekildiginde butun satirlar tek merkez degerini paylasiyor, gruplama fisi
 * birkac dev satira cokuruyor, metnin tamami okunmus oldugu halde ayristirici
 * hicbir kalem goremiyordu. Duzeltmesi de olculmemisti: 90 ile 270 birbirinin
 * 180 derece donmusu oldugu icin HEM satir sirasinda HEM satir ici sirada
 * ayrismalari gerekirken kod yalnizca birinde ayrisiyordu.
 *
 * Testin isirdigi yer tam olarak orasi: ayni fis dort yonde de AYNI satirlari
 * vermek zorunda. Geometri `commonMain`'e tasindigi icin bu, cihaz gerektirmeyen
 * bir dogrulama - `score`un ayni sebeple tasinmasinin (F10.13) devami.
 */
class VisualRowsTest {

    // Dik duran ornek fis: iki kolon, alt alta dort satir. Olculer gercek bir
    // termal fisin oranlarina yakin - dar satir, genis bosluk.
    private val genislik = 600
    private val yukseklik = 400
    private val dikFis = listOf(
        parca("AKYURT MARKET", 40, 20, 320, 44),
        parca("KREMA 18YAGLI 200ML %1.", 40, 90, 360, 114),
        parca("*106,00", 460, 90, 560, 114),
        parca("HARRAS SUTLU CIK.80G %1.", 40, 150, 380, 174),
        parca("*47,00", 460, 150, 560, 174),
        parca("Odenecek KDV Dahil Tutar", 40, 230, 400, 254),
        parca("*225,50", 460, 230, 560, 254),
    )

    private val beklenen = listOf(
        "AKYURT MARKET",
        "KREMA 18YAGLI 200ML %1. *106,00",
        "HARRAS SUTLU CIK.80G %1. *47,00",
        "Odenecek KDV Dahil Tutar *225,50",
    )

    @Test
    fun dikFisIkiKolonuTekSatirdaBirlestirir() {
        assertEquals(beklenen, groupVisualRows(dikFis))
    }

    @Test
    fun dortYonDeAyniSatirlariVerir() {
        // ESKI KODUN KIRILDIGI TEST. 90 ve 270 icin satir sirasi ya da satir
        // ici sira ters cikiyordu; 180 hicbir zaman ele alinmamisti.
        for (aci in listOf(0, 90, 180, 270)) {
            assertEquals(
                beklenen,
                groupVisualRows(dikFis.dondur(aci)),
                "$aci derecede satirlar dik haldekinden farkli cikti",
            )
        }
    }

    @Test
    fun yanFisTekSatiraCokmez() {
        // Cokmenin kendisini oluyor: 90 derecede eksen-hizali kutularin
        // merkez-Y'leri birbirine yakin, eski kod hepsini tek gruba atardi.
        val yan = dikFis.dondur(90)
        assertEquals(4, groupVisualRows(yan).size)
    }

    @Test
    fun egikCekilmisFisAyniSatirlariVerir() {
        // Elde cekilen fis hicbir zaman tam dik degil. Okuma yonu parcalardan
        // olculdugu icin kucuk egiklik satirlari koparmamali.
        val egik = dikFis.map { p ->
            OcrPiece(p.text, p.corners.map { OcrPoint(it.x, it.y + it.x / 25) })
        }
        assertEquals(beklenen, groupVisualRows(egik))
    }

    @Test
    fun kosesiOlmayanParcaYonOyunaKatilmaz() {
        // Kutudan uretilmis koseler gercek yonu bilmiyor; hepsi yatay gorunur.
        // Yan cekilmis bir fiste bunlarin yonu bozmasi, tek bir eksik parcanin
        // butun okumayi cokertmesi demek olurdu.
        val yan = dikFis.dondur(90).mapIndexed { index, p ->
            if (index == 0) p.copy(orientationKnown = false) else p
        }
        assertEquals(beklenen, groupVisualRows(yan))
    }

    @Test
    fun bosGirdiBosDoner() {
        assertEquals(emptyList(), groupVisualRows(emptyList()))
        assertEquals(emptyList(), groupVisualRows(listOf(parca("   ", 0, 0, 10, 10))))
    }

    @Test
    fun eksikKoseliParcaAtlanir() {
        val bozuk = OcrPiece("YARIM", listOf(OcrPoint(0, 0), OcrPoint(10, 0)))
        val cikti = groupVisualRows(dikFis + bozuk)
        assertEquals(beklenen, cikti)
        assertTrue(cikti.none { it.contains("YARIM") })
    }

    /** Dik duran, eksen-hizali bir kutudan parca uretir. */
    private fun parca(text: String, left: Int, top: Int, right: Int, bottom: Int) = OcrPiece(
        text = text,
        corners = listOf(
            OcrPoint(left, top),
            OcrPoint(right, top),
            OcrPoint(right, bottom),
            OcrPoint(left, bottom),
        ),
    )

    /**
     * Butun parcalari gorselle birlikte dondurur.
     *
     * KOSE SIRASI KORUNUYOR cunku koseler METNE gore sirali: gorsel donunce
     * metnin sol-ustu de birlikte doner, indeks 0 yine sol-ust kalir. Testin
     * gercek OCR ciktisini taklit etmesi tam olarak buna bagli.
     */
    private fun List<OcrPiece>.dondur(degrees: Int) = map { p ->
        p.copy(corners = p.corners.map { it.dondur(degrees) })
    }

    private fun OcrPoint.dondur(degrees: Int): OcrPoint = when (degrees) {
        90 -> OcrPoint(yukseklik - 1 - y, x)
        180 -> OcrPoint(genislik - 1 - x, yukseklik - 1 - y)
        270 -> OcrPoint(y, genislik - 1 - x)
        else -> this
    }
}
