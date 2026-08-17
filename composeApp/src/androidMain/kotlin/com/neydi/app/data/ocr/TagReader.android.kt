package com.neydi.app.data.ocr

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit Text Recognition v2, Latin betigi - cihazda, ucretsiz, Turkce kapsiyor.
 *
 * `fromBitmap(bmp, 0)`, `fromFilePath` DEGIL: `fromFilePath` EXIF'i kendisi
 * okuyup dondurmeye calisiyor ve girdimizde EXIF ZATEN piksele islenmis
 * durumda (`downscaleForOcr`). Ikisi birlesince kare iki kez donerdi - F4.20'nin
 * tam tersi yonde ayni hata. `rotationDegrees = 0` vermek "bu goruntu dik"
 * demek ve dogrusu bu.
 *
 * `decodeFile` EXIF'i yok sayiyor ve burada bu ISTENEN davranis: dosyada zaten
 * EXIF yok (`downscaleForOcr` etiketsiz yaziyor), olsaydi da piksellerle
 * cakisirdi.
 */
internal actual suspend fun readTag(imagePath: String, sourceExif: Int): TagOcr = withContext(Dispatchers.IO) {
    val bitmap = BitmapFactory.decodeFile(imagePath)
        ?: error("Fotograf cozulemedi: $imagePath")

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val text = try {
        suspendCancellableCoroutine { cont ->
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    } finally {
        recognizer.close()
    }

    val lines = mutableListOf<OcrPiece>()
    val words = mutableListOf<OcrPiece>()
    text.textBlocks.forEach { block ->
        block.lines.forEach { line ->
            lines += line.toPiece(line.text)
            line.elements.forEach { element -> words += element.toPiece(element.text) }
        }
    }

    TagOcr(
        lines = lines,
        words = words,
        sourceWidth = bitmap.width,
        sourceHeight = bitmap.height,
        exifOrientation = sourceExif,
    ).also { bitmap.recycle() }
}

/**
 * ML Kit kutusunu [OcrPiece]'e cevirir.
 *
 * KOSE NOKTALARI YOKSA eksen-hizali kutudan uretilip `orientationKnown = false`
 * isaretleniyor - `groupVisualRows` o parcayi konumuyla sayiyor ama yon oyuna
 * katmiyor. Uydurma bir yon vermek, olcumu sessizce bozmaktan daha kotu olurdu.
 */
private fun com.google.mlkit.vision.text.Text.Line.toPiece(content: String): OcrPiece =
    pieceFrom(content, cornerPoints, boundingBox)

private fun com.google.mlkit.vision.text.Text.Element.toPiece(content: String): OcrPiece =
    pieceFrom(content, cornerPoints, boundingBox)

private fun pieceFrom(
    content: String,
    corners: Array<android.graphics.Point>?,
    box: android.graphics.Rect?,
): OcrPiece {
    val real = corners?.takeIf { it.size == 4 }?.map { OcrPoint(it.x, it.y) }
    if (real != null) return OcrPiece(text = content, corners = real)

    val r = box ?: android.graphics.Rect(0, 0, 0, 0)
    return OcrPiece(
        text = content,
        corners = listOf(
            OcrPoint(r.left, r.top),
            OcrPoint(r.right, r.top),
            OcrPoint(r.right, r.bottom),
            OcrPoint(r.left, r.bottom),
        ),
        orientationKnown = false,
    )
}
