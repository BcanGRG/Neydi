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

    private val tanimlayici = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun readLines(gorselYolu: String): Result<List<String>> = runCatching {
        val gorsel = InputImage.fromFilePath(context, Uri.fromFile(File(gorselYolu)))
        val metin = suspendCancellableCoroutine { devam ->
            tanimlayici.process(gorsel)
                .addOnSuccessListener { devam.resume(it) }
                .addOnFailureListener { devam.resumeWithException(it) }
        }
        linesInReadingOrder(metin)
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
internal fun linesInReadingOrder(metin: Text): List<String> =
    metin.textBlocks
        .flatMap { blok -> blok.lines }
        .sortedBy { satir -> satir.boundingBox?.top ?: Int.MAX_VALUE }
        .map { satir -> satir.text }
