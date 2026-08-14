package com.neydi.app.data.receipt

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit Text Recognition v2, Latin betigi - Turkce'yi kapsiyor.
 *
 * Model uygulamaya GOMULU geliyor (~4MB). Play Services uzerinden indirilen
 * varyanti ~260KB ama ilk kullanimda indirme bekliyor; kasa kuyrugunda
 * "model indiriliyor" gormek istemiyoruz.
 */
internal class MlKitReceiptReader(private val context: Context) : ReceiptReader {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun readLines(imagePath: String): Result<List<String>> = runCatching {
        val image = InputImage.fromFilePath(context, Uri.fromFile(File(imagePath)))
        val text = suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
        linesInReadingOrder(text)
    }
}

/**
 * SATIRLARI Y KOORDINATINA GORE SIRALIYOR, blok sirasina gore DEGIL.
 *
 * ML Kit metni once bloklara ayiriyor ve blok sirasi sayfadaki okuma sirasini
 * garanti etmiyor. Fis tek sutun oldugu icin dikey konum dogru siralamayi
 * veriyor. Blok sirasina guvenseydik TOPLAM satiri urunlerin arasina
 * karisabilir, tartili urunun adi ile agirlik satiri ayrilabilirdi - ve
 * ayristirici o ikisinin YAN YANA olmasina dayaniyor.
 */
internal fun linesInReadingOrder(text: Text): List<String> =
    text.textBlocks
        .flatMap { block -> block.lines }
        .sortedBy { row -> row.boundingBox?.top ?: Int.MAX_VALUE }
        .map { row -> row.text }
