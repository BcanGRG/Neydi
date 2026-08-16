package com.neydi.app.data.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.neydi.app.data.ocr.OcrPiece
import com.neydi.app.data.ocr.OcrPoint
import com.neydi.app.data.ocr.groupVisualRows
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
     * Fotograf KENDI cozunurlugunde ust uste binen seritlere bolunup her serit
     * ayri okunuyor; yon de bu seritlerden birinde, tam cozunurlukte seciliyor.
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

        val decoder = openRegionDecoder(imagePath)
            ?: return singlePassRead(imagePath, forceRotation)
        try {
            val bands = bandsFor(bounds.outWidth, bounds.outHeight)

            // YON SECEN SERIT ORTADAN (cift sayida seritte alt-orta): kalem
            // satirlarinin bulundugu yer orasi. Puanlayici ayristirici ciktisina
            // baktigi icin (bkz. ReadingScore) yalnizca kunye tasiyan bas serit
            // yaniltirdi - hicbir yonde urun bulamaz, uc puan da esitlenirdi.
            val probeIndex = bands.size / 2
            val perBand = arrayOfNulls<List<String>>(bands.size)

            // Yon secimi zaten bu seridi okuyor; ayni geciste cikan satirlar da
            // kullaniliyor, yani serit ikinci kez cozulmuyor.
            val probe = decoder.decodeRegion(bands[probeIndex], BitmapFactory.Options())
            val pick = if (probe == null) {
                RotationPick(forceRotation ?: 0, emptyList())
            } else {
                try {
                    readWithRotation(probe, forceRotation)
                } finally {
                    probe.recycle()
                }
            }
            perBand[probeIndex] = pick.rows

            for ((index, band) in bands.withIndex()) {
                if (index == probeIndex) continue
                val piece = decoder.decodeRegion(band, BitmapFactory.Options()) ?: continue
                perBand[index] = visualRows(recognize(piece, pick.degrees), pick.degrees)
                piece.recycle()
            }

            // Seritler bilerek bindigi icin ayni kalem birden fazla seritte
            // okunuyor; mukerrerler burada eleniyor (F4.17). Tek seritte de
            // zararsiz: kimlik SIRA NUMARASI tasiyor, yani ayni fisteki iki
            // ozdes urun ayri kimlik aliyor.
            val rows = perBand.filterNotNull().flatten()
            logMeasurement(bounds, bands.size, pick.degrees, rows.size)
            return dedupeRepeatedItems(rows)
        } finally {
            decoder.recycle()
        }
    }

    /** Serit yolu kullanilamadiginda eski davranis - tek gecis. */
    private suspend fun singlePassRead(imagePath: String, forceRotation: Int?): List<String> {
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: error("gorsel cozulemedi: $imagePath")
        return try {
            // AYNI BITMAP hem yon secimine hem okumaya gidiyor: bolgesel cozucu
            // yokken gorseli ikinci kez cozmek bellekte ikinci bir tam boy kopya
            // demek olurdu ve kacindigimiz sey tam olarak o.
            readWithRotation(bitmap, forceRotation).rows
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Bir gorseli okur; yon verilmemisse ayni geciste secer.
     *
     * Iki cagiran da (serit yolu ve tek gecis yolu) ayni sozu tasiyor: yon
     * secimi FAZLADAN bir kod cozme yapmaz, elindeki bitmap'i kullanir.
     */
    private suspend fun readWithRotation(bitmap: Bitmap, forceRotation: Int?): RotationPick =
        if (forceRotation == null) {
            pickRotation(bitmap)
        } else {
            RotationPick(forceRotation, visualRows(recognize(bitmap, forceRotation), forceRotation))
        }

    /**
     * Yonu TAM COZUNURLUKLU BIR SERITTEN secer.
     *
     * ONCEDEN KUCULTULMUS KOPYADAN seciliyordu (`inSampleSize = 4`) ve gerekcesi
     * *"yon secimi ayrinti gerektirmiyor"*du. O gerekce F4.14b'de gecersiz oldu:
     * puanlayici artik satirin SEKLINE degil, o okumadan kac URUN cikacagina
     * bakiyor - yani AYRISTIRICIYI calistiriyor, ve ayristirici harflerin
     * okunmasini gerektiriyor. Sonuc: puanlayici okuma geciginin dortte bir
     * boyunda, yani onaltida bir pikselle ayni soruyu cevaplamaya calisiyordu.
     *
     * Uzun fiste hicbir yonde hicbir sey okunamiyor, uc puan da sifir cikiyor,
     * `maxByOrNull` esitlikte ILK elemani donduruyor ve cevap her seferinde 0
     * derece oluyordu - yan cekilmis kare icin tam olarak yanlis cevap. Hata
     * fis KISALDIKCA kayboldugu icin de uzun sure gorunmedi: yon secimi tam da
     * serit yonteminin var olma sebebi olan fislerde bozuluyordu.
     *
     * 180 DE DENENIYOR: eskiden yoktu, yani ters cekilmis fisin otomatik yolda
     * dogru cevabi bulunmuyordu. Fis Kontrol'deki elle listede 180 zaten vardi.
     *
     * ILK YETERLI ADAYDA DURULUYOR: tek seritten bu kadar urun cikiyorsa yon
     * dogrudur ve uc gecis daha yapmanin kazanci yok. 0 basta cunku dogru
     * yazilmis fotograflarin cogunda cevap o.
     */
    private suspend fun pickRotation(probe: Bitmap): RotationPick {
        var best = RotationPick(0, emptyList())
        var bestScore = -1
        for (degrees in ROTATION_CANDIDATES) {
            val rows = visualRows(recognize(probe, degrees), degrees)
            val puan = score(rows)
            if (puan > bestScore) {
                bestScore = puan
                best = RotationPick(degrees, rows)
            }
            if (puan >= CONFIDENT_SCORE) break
        }
        return best
    }

    private suspend fun recognize(
        bitmap: Bitmap,
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

/** Bir yon denemesinin sonucu: secilen aci ve o acida okunan satirlar. */
private data class RotationPick(val degrees: Int, val rows: List<String>)

/**
 * ML Kit ciktisini gorsel satirlara cevirir.
 *
 * ISIN TAMAMI `groupVisualRows`da (commonMain) - burada yalnizca ML Kit'in
 * sekli ortak sekle donuyor. Geometri oraya tasindi cunku `androidMain`de
 * TEST EDILEMIYORDU ve bu katman projenin en pahali sessiz hatasini uretmisti;
 * `score`un ayni sebeple tasinmasinin (F10.13) devami.
 *
 * KUTULAR DONDURULMEMIS KOORDINATTA GELIYOR: ML Kit metni `rotationDegrees` ile
 * dogru okuyor ama koordinatlari HAM bitmap eksenlerinde veriyor. Kose noktalari
 * ise metnin KENDI yonunde sirali geldigi icin okuma yonunu dogrudan tasiyorlar
 * - gruplama ekseninin `rotationDegrees`den tahmin edilmesi bu sayede bitti.
 */
internal fun visualRows(text: Text, rotationDegrees: Int = 0): List<String> =
    groupVisualRows(text.textBlocks.flatMap { it.lines }.mapNotNull { it.toOcrPiece() })

private fun Text.Line.toOcrPiece(): OcrPiece? {
    val koseler = cornerPoints
    if (koseler != null && koseler.size == 4) {
        return OcrPiece(text, koseler.map { OcrPoint(it.x, it.y) })
    }
    // YEDEK: kose noktasi gelmezse eksen-hizali kutu kullaniliyor. Parca
    // konumuyla sayilir ama `orientationKnown = false` ile yon oyununa
    // KATILMAZ - yoksa tek eksik parca butun okumayi yatay sanabilirdi.
    val kutu = boundingBox ?: return null
    return OcrPiece(
        text = text,
        corners = listOf(
            OcrPoint(kutu.left, kutu.top),
            OcrPoint(kutu.right, kutu.top),
            OcrPoint(kutu.right, kutu.bottom),
            OcrPoint(kutu.left, kutu.bottom),
        ),
        orientationKnown = false,
    )
}

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

/** Denenecek acilar. 0 BASTA: dogru yazilmis fotografta cevap o. */
private val ROTATION_CANDIDATES = listOf(0, 90, 270, 180)

/**
 * Bir yonu tartismasiz kabul etmeye yeten puan.
 *
 * TEK SERITTEN cikan urun sayisi bu; `MIN_USABLE_LINES` (6) butun fis icin
 * konmus bir tabandi, bir seritten bu kadar urun cikiyorsa yon dogrudur.
 */
private const val CONFIDENT_SCORE = 10

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
private fun openRegionDecoder(path: String): BitmapRegionDecoder? = runCatching {
    @Suppress("DEPRECATION")
    BitmapRegionDecoder.newInstance(path, false)
}.getOrNull()

/**
 * OLCUM KAYDI (gecici, F4.20 olcumu icin).
 *
 * Tarayici ciktisiyla kendi kameramizin karesini karsilastirabilmek icin okuma
 * basina bir satir yaziyor. KISISEL VERI YAZILMIYOR - dosya adi da dahil hicbir
 * icerik degil, yalnizca olculer. Olcum bitince silinecek.
 */
private fun logMeasurement(
    bounds: BitmapFactory.Options,
    bandCount: Int,
    rotation: Int,
    rowCount: Int,
) {
    Log.i(
        MEASUREMENT_TAG,
        "okuma ${bounds.outWidth}x${bounds.outHeight} " +
            "serit=$bandCount yon=$rotation satir=$rowCount",
    )
}

/** Olcum satirlarinin ortak etiketi: `adb logcat -s NeydiOlcum`. */
internal const val MEASUREMENT_TAG = "NeydiOlcum"
