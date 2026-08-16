package com.neydi.app.data.receipt

import kotlin.math.abs
import kotlin.math.sqrt

/** OCR kutusunun bir kosesi, HAM gorsel eksenlerinde. */
internal data class OcrPoint(val x: Int, val y: Int)

/**
 * OCR'in okudugu bir metin parcasi ve dort kosesi.
 *
 * KOSELER METNIN KENDI YONUNDE sirali: [corners]`[0]` metnin sol-ustu, `[1]`
 * sag-ustu, `[2]` sag-alti, `[3]` sol-alti. Konumlar HAM gorsel eksenlerinde
 * ama SIRA metne gore - ve bu dosyanin tamami buna dayaniyor: `[0] -> [1]`
 * vektoru okuma yonunu DOGRUDAN veriyor, tahmin gerekmiyor.
 *
 * @param orientationKnown false ise koseler gercek degil, eksen-hizali
 *   kutudan uretildi; o parca konumuyla sayilir ama YON OYUNA KATILMAZ.
 */
internal data class OcrPiece(
    val text: String,
    val corners: List<OcrPoint>,
    val orientationKnown: Boolean = true,
)

/**
 * OCR parcalarini GORSEL SATIRLARA gruplar; her grubu okuma yonunde birlestirir.
 *
 * NEDEN SIRAYA GUVENILMIYOR: fis iki kolon - solda aciklama, sagda tutar - ve
 * ikisi AYNI gorsel satirda. ML Kit onlari ayri "line" olarak donduruyor,
 * dikey konumlari da neredeyse ayni. Dolayisiyla dizilis rastgele bozuluyor;
 * gercek fiste olculdu:
 *
 *     *125.58
 *     *47.00                     <- tutar, adindan ONCE
 *     HARRAS SUTLU CIK.80G %1.   <- adi, tutarindan SONRA
 *
 * Yani sirayi duzeltmeye calisan hicbir sezgi ise yaramaz, cunku SIRANIN
 * KENDISI guvenilir degil. Dogru islem eslestirmeyi GEOMETRIDEN yapmak.
 *
 * YON PARAMETREYLE DEGIL, PARCALARIN KENDISINDEN OLCULUYOR - ve bu dosyanin
 * var olma sebebi bu. Onceki hali gruplama eksenini `rotationDegrees`e gore
 * seciyordu ve iki ayri yerden birden yaniliyordu:
 *
 *  1. Verilen aci yanlissa (yon secimi kor kaldiginda oluyordu) eksen de
 *     yanlis seciliyor, yan cekilmis fisin butun satirlari birbirine yakin bir
 *     merkez degeri paylastigi icin fis birkac DEV satira cokuyordu. Cihazda
 *     olculdu: otuz dokuz kalemin adi, barkodu, tutari metinde vardi ama sekiz
 *     satira cokmustu ve ayristirici hicbirini goremedi.
 *  2. 90 ile 270 birbirinin 180 derece donmusu, yani dogru bir uygulamada
 *     HEM satir sirasinda HEM satir ici sirada ayrismalari gerekir. Eski kod
 *     yalnizca satir icinde ayrisiyordu; ikisinden biri satirlari ters sirada
 *     uretiyordu. Ayristirici fena halde siraya bagli oldugu icin (ad bir ALT
 *     satirda, miktar satiri urunden ONCE, tarih ve kunye ILK eslesenden) ters
 *     sira ~sifir urun demek - ve o yon yon oylamasini asla kazanamiyordu.
 *
 * Kose noktalari okuma yonunu olctugu icin ikisi de ortadan kalkiyor: 0, 90,
 * 180, 270 ve aradaki egiklikler ayni kodla dogru cikiyor, ve fonksiyon
 * platformdan bagimsiz oldugu icin CIHAZSIZ test edilebiliyor.
 */
internal fun groupVisualRows(pieces: List<OcrPiece>): List<String> {
    val gecerli = pieces.filter { it.corners.size == 4 && it.text.isNotBlank() }
    if (gecerli.isEmpty()) return emptyList()

    // OKUMA YONU parcalarin toplamindan: uzun satirlar daha uzun vektor verdigi
    // icin toplam kendiliginden agirlikli, ve tek tuk yanlis okunmus bir parca
    // sonucu cevirmiyor.
    var toplamX = 0.0
    var toplamY = 0.0
    for (p in gecerli.filter { it.orientationKnown }) {
        toplamX += (p.corners[1].x - p.corners[0].x).toDouble()
        toplamY += (p.corners[1].y - p.corners[0].y).toDouble()
    }
    val boy = sqrt(toplamX * toplamX + toplamY * toplamY)
    // Olculebilir yon yoksa duz yatay varsayiliyor - eski davranis.
    val okumaX = if (boy == 0.0) 1.0 else toplamX / boy
    val okumaY = if (boy == 0.0) 0.0 else toplamY / boy
    // Ekran ekseninde (y asagi dogru) okuma yonunun 90 derece SAGI, sayfada
    // asagi demek. Satirlar bu eksende siralanir.
    val altX = -okumaY
    val altY = okumaX

    val olculen = gecerli.map { p ->
        val merkezX = p.corners.sumOf { it.x.toDouble() } / 4.0
        val merkezY = p.corners.sumOf { it.y.toDouble() } / 4.0
        val izdusum = p.corners.map { it.x * altX + it.y * altY }
        Olculen(
            text = p.text.trim(),
            satir = merkezX * altX + merkezY * altY,
            kolon = merkezX * okumaX + merkezY * okumaY,
            // Satirin kalinligi KENDI cercevesinde olculuyor; eksen-hizali
            // kutunun yuksekligi yan fiste yanlis sayiyi verirdi.
            kalinlik = (izdusum.maxOrNull() ?: 0.0) - (izdusum.minOrNull() ?: 0.0),
        )
    }

    // Tolerans satir kalinliginin ORANI: sabit piksel degeri farkli
    // cozunurluklerde ya da cok kucuk ya da cok buyuk olurdu.
    val ortaKalinlik = olculen.map { it.kalinlik }.sorted()[olculen.size / 2]
    val tolerans = (ortaKalinlik * ROW_TOLERANCE).coerceAtLeast(1.0)

    val sirali = olculen.sortedBy { it.satir }
    val gruplar = mutableListOf<MutableList<Olculen>>()
    for (p in sirali) {
        val grup = gruplar.lastOrNull()
        // Kiyas grubun SON uyesiyle: fis hafif egik cekildiginde satir boyunca
        // kayiyor ve gruba gore sabit bir merkez tutmak o kaymayi kopartirdi.
        if (grup != null && abs(p.satir - grup.last().satir) <= tolerans) {
            grup.add(p)
        } else {
            gruplar.add(mutableListOf(p))
        }
    }

    return gruplar.map { grup -> grup.sortedBy { it.kolon }.joinToString(" ") { it.text } }
}

/** Bir parcanin satir/kolon ekseni uzerindeki izdusumu. */
private class Olculen(
    val text: String,
    val satir: Double,
    val kolon: Double,
    val kalinlik: Double,
)

/** Ayni satir sayilmak icin izin verilen kayma, satir kalinliginin orani. */
private const val ROW_TOLERANCE = 0.6
