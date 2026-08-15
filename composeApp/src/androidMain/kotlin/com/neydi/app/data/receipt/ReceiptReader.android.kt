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

    override suspend fun readLines(imagePath: String, forceRotation: Int?): Result<List<String>> =
        withContext(Dispatchers.Default) {
            runCatching {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                    ?: error("gorsel cozulemedi: $imagePath")
                // EN IYI YONU SECIYORUZ, tahmin ETMIYORUZ. Gerekcesi olculdu:
                // yatay cekilmis fiste tutarlar urun adlarindan tamamen kopuyor
                // (*225.50 dorduncu satirda, urun adlari yirmi sekizinci
                // satirda), dik cevrilmis ayni goruntude siralama neredeyse
                // kusursuz. EXIF'e guvenemiyoruz - kucultme sirasinda yeniden
                // kodlanan JPEG yon bilgisini tasimıyor.
                //
                // Ucu de deneniyor: kullanici uzun fisi yatay cekiyor ama
                // telefonu hangi yone cevirdigi belli degil (90 ya da 270).
                // forceRotation verildiyse OTOMATIK SECIM ATLANIR. Kullanici
                // Fis Kontrol ekranindan yonu elle cevirdiginde bu yol
                // kullaniliyor: otomatik secim iki fiste dogru bildi ama yanlis
                // bilirse kullanici tikanmasin.
                val denenecek = forceRotation?.let { listOf(it) } ?: listOf(0, 90, 270)
                denenecek
                    .map { derece -> visualRows(recognize(bitmap, derece)) }
                    .maxByOrNull { score(it) }
                    ?: emptyList()
            }
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
