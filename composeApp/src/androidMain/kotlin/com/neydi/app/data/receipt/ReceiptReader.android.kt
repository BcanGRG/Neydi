package com.neydi.app.data.receipt

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit Text Recognition v2, Latin betigi - Turkce'yi kapsiyor.
 *
 * Model uygulamaya GOMULU (~4MB). Play Services uzerinden inen varyanti daha
 * kucuk ama ilk kullanimda indirme bekletiyor; kasa kuyrugunda "model
 * indiriliyor" gormek istemiyoruz.
 */
internal class MlKitReceiptReader(private val context: Context) : ReceiptReader {

    private val tanimlayici = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * TEK CEKIM, ICERIDE SERITLERE BOLUNUYOR (F4.17).
     *
     * Once kaba bir kopyayla YON seciliyor, sonra fotograf KENDI cozunurlugunde
     * ust uste binen seritlere bolunup her serit ayri okunuyor.
     *
     * NEDEN ISE YARIYOR - ve neden "kirpmak piksel uretmiyor" itirazi buraya
     * GECMIYOR: piksel zaten sensorde vardi, BIZ atiyorduk. Fotograf OCR'dan
     * once uzun kenari 2576'ya inecek sekilde kucultuluyordu; ML Kit metrelik
     * bir fisi o boyda satir basina bes-alti piksele sikismis halde goruyordu.
     * Serit yontemi ayni kareyi tam cozunurlukte, parca parca okutuyor -
     * kullanicidan dort ayri fotograf istemeden.
     *
     * SERIT BOYU olculmus bir sayidan geliyor: 2576 uzun kenarli kareler bu
     * projede guvenilir okunuyor (butun gercek fis kurgulari o boyda cekildi),
     * o yuzden serit uzunlugu de o civarda tutuluyor.
     *
     * BINDIRME KASITLI: seritler %15 ust uste biniyor ki serit sinirina denk
     * gelen satir ikiye bolunmesin. Ayni satirin iki seritte birden okunmasi
     * sorun DEGIL - `stitchParts` ile ayni kimlik mantigi mukerrerleri eliyor
     * (F4.15).
     */
    override suspend fun readLines(imagePath: String, forceRotation: Int?): Result<List<String>> =
        withContext(Dispatchers.Default) {
            runCatching {
                tiledRead(imagePath, forceRotation)
            }
        }

    private suspend fun tiledRead(imagePath: String, forceRotation: Int?): List<String> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(imagePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) error("gorsel cozulemedi: $imagePath")

        // YON KABA KOPYADAN seciliyor: uc yonu tam cozunurlukte denemek uc kat
        // is demek olurdu ve yon secimi ayrinti gerektirmiyor - olculen sey
        // satirlarin BIRLESIP birlesmedigi, harflerin okunup okunmadigi degil.
        val rotation = forceRotation ?: pickRotation(imagePath)

        val decoder = openRegionDecoder(imagePath) ?: return singlePassRead(imagePath, rotation)
        try {
            val bands = bandsFor(bounds.outWidth, bounds.outHeight)
            if (bands.size <= 1) return singlePassRead(imagePath, rotation)
            val rows = ArrayList<String>()
            for (band in bands) {
                val piece = decoder.decodeRegion(band, BitmapFactory.Options()) ?: continue
                rows += visualRows(recognize(piece, rotation))
                piece.recycle()
            }
            // Seritler bilerek bindigi icin ayni kalem birden fazla seritte
            // okunuyor; mukerrerler burada eleniyor (F4.17).
            return dedupeRepeatedItems(rows)
        } finally {
            decoder.recycle()
        }
    }

    /** Serit yolu kullanilamadiginda eski davranis - tek gecis. */
    private suspend fun singlePassRead(imagePath: String, rotation: Int): List<String> {
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: error("gorsel cozulemedi: $imagePath")
        val rows = visualRows(recognize(bitmap, rotation))
        bitmap.recycle()
        return rows
    }

    /**
     * Yonu KUCULTULMUS kopyayla secer.
     *
     * `inSampleSize` ile kod cozme sirasinda seyreltiliyor, yani tam boy bitmap
     * hic ayrilmiyor - `downscaleForOcr`daki ayni ders.
     */
    private suspend fun pickRotation(imagePath: String): Int {
        val small = BitmapFactory.decodeFile(
            imagePath,
            BitmapFactory.Options().apply { inSampleSize = ROTATION_SAMPLE },
        ) ?: return 0
        val best = listOf(0, 90, 270).maxByOrNull { score(visualRows(recognize(small, it))) } ?: 0
        small.recycle()
        return best
    }

    private suspend fun recognize(
        bitmap: android.graphics.Bitmap,
        rotationDegrees: Int,
    ): Text = suspendCancellableCoroutine { devam ->
        // rotationDegrees ML Kit'e veriliyor, bitmap DONDURULMUYOR: donmus kopya
        // ayirmak bellekte ikinci bir tam boy bitmap demek olurdu ve kacinmaya
        // calistigimiz sey tam olarak o.
        val gorsel = InputImage.fromBitmap(bitmap, rotationDegrees)
        tanimlayici.process(gorsel)
            .addOnSuccessListener { devam.resume(it) }
            .addOnFailureListener { devam.resumeWithException(it) }
    }
}

/**
 * Fisin bir gorsel satirinda okunan parca.
 */
private data class Parca(val text: String, val kutu: Rect)

/**
 * OCR satirlarini GORSEL SATIRLARA gruplar; her grubu soldan saga birlestirir.
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
 * Yani sirayi duzeltmeye calisan hicbir sezgi ise yaramaz, cunku SIRANIN KENDISI
 * guvenilir degil. Dogru islem eslestirmeyi dikey ORTUSMEDEN yapmak: ayni satira
 * dusen parcalar birlesir, grup icinde X'e gore sıralanır. Sonuc ayristiricinin
 * bekledigi bicim:
 *
 *     KREMA 18YAGLI 200ML %1.      *106.00
 *     Odenecek KDV Dahil Tutar     *225.50
 *
 * Geometri platforma ozgu veri oldugu icin bu is BURADA yapiliyor; ayristirici
 * dizgi uzerinde calismaya devam ediyor ve cihazsiz test edilebilir kaliyor.
 */
internal fun visualRows(text: Text): List<String> {
    val parcalar = text.textBlocks
        .flatMap { blok -> blok.lines }
        .mapNotNull { satir -> satir.boundingBox?.let { Parca(satir.text, it) } }
    if (parcalar.isEmpty()) return emptyList()

    // Tolerans satir yuksekliginin ORANI: sabit piksel degeri farkli
    // cozunurluklerde ya da cok kucuk ya da cok buyuk olurdu.
    val ortaYukseklik = parcalar.map { it.kutu.height() }.sorted()[parcalar.size / 2]
    val tolerans = (ortaYukseklik * 0.6f).toInt().coerceAtLeast(1)

    val siralı = parcalar.sortedBy { it.kutu.centerY() }
    val gruplar = mutableListOf<MutableList<Parca>>()
    for (p in siralı) {
        val grup = gruplar.lastOrNull()
        val ayniSatir = grup != null &&
            kotlin.math.abs(p.kutu.centerY() - grup.last().kutu.centerY()) <= tolerans
        if (ayniSatir && grup != null) grup.add(p) else gruplar.add(mutableListOf(p))
    }

    return gruplar.map { grup ->
        grup.sortedBy { it.kutu.left }.joinToString(" ") { it.text.trim() }
    }
}

// Yon puanlayicisi commonMain'e tasindi (ReadingScore.kt) - artik AYRISTIRICIYI
// calistirarak puanliyor ve bu sayede test edilebilir hale geldi.

/**
 * Bir seridin hedef uzunlugu, piksel.
 *
 * OLCULMUS BIR SAYI, tahmin degil: bu projedeki butun gercek fis kurgulari
 * uzun kenari 2576 olan karelerden cikti ve o boyda guvenilir okundu.
 */
private const val TARGET_BAND = 2400

/**
 * Seritlerin ust uste binme orani.
 *
 * BINDIRME KASITLI: serit sinirina denk gelen satir ikiye bolunmesin. Ayni
 * satirin iki seritte okunmasi sorun degil - `stitchParts` mukerreri eliyor.
 */
private const val BAND_OVERLAP = 0.15f

/** Yon secerken kullanilan seyreltme - ayrinti gerekmiyor, hiz gerekiyor. */
private const val ROTATION_SAMPLE = 4

/**
 * Fotografi UZUN EKSENI boyunca ust uste binen seritlere boler.
 *
 * Fis uzun ekseni doldurur - telefon hangi yone cevrilmis olursa olsun. Kisa
 * eksende bolmek fisi dikey kesip satirlari ortadan ikiye ayirirdi.
 */
internal fun bandsFor(width: Int, height: Int): List<Rect> {
    val horizontal = width >= height
    val longEdge = if (horizontal) width else height
    if (longEdge <= TARGET_BAND) return listOf(Rect(0, 0, width, height))

    val count = ((longEdge + TARGET_BAND - 1) / TARGET_BAND).coerceAtLeast(2)
    val step = longEdge.toFloat() / count
    val pad = (step * BAND_OVERLAP).toInt()
    return (0 until count).map { i ->
        val start = (i * step).toInt().minus(pad).coerceAtLeast(0)
        val end = ((i + 1) * step).toInt().plus(pad).coerceAtMost(longEdge)
        if (horizontal) Rect(start, 0, end, height) else Rect(0, start, width, end)
    }
}

/**
 * Bolgesel kod cozucu - seridi TAM COZUNURLUKTE, tam boy bitmap ayirmadan
 * okur. Bu yontemin bellek tarafindaki butun degeri burada.
 */
private fun openRegionDecoder(path: String): android.graphics.BitmapRegionDecoder? = runCatching {
    @Suppress("DEPRECATION")
    android.graphics.BitmapRegionDecoder.newInstance(path, false)
}.getOrNull()
