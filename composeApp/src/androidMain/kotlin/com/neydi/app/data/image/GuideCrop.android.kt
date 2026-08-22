package com.neydi.app.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Rehber bolgesinden kirpim - once YON, sonra kirpim, sonra olcek.
 *
 * SIRA ZORUNLU. [GuideBox.inImage] hesabi `PreviewView`in GOSTERDIGI kareye
 * gore yazildi; diskteki JPEG ise yonu EXIF etiketinde tasiyor. Kirpim yonden
 * once yapilsaydi dikdortgen doksan derece yanlis yere duserdi - ve bu sessiz
 * bir hata olurdu, cunku cikan serit yine bir seyler gosterirdi.
 *
 * [downscaleForOcr] olcek ile yonu TEK matriste birlestiriyor; burada
 * birlestirilemiyor cunku kirpim ikisinin ARASINA giriyor. Bedeli bir ara
 * bitmap; `inSampleSize` sayesinde o bitmap zaten tam boy degil.
 */
actual suspend fun cropToGuide(
    source: ByteArray,
    destPath: String,
    guide: GuideBox,
    maxLongEdge: Int,
): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        if (!guide.usable) return@runCatching false

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(source, 0, source.size, bounds)
        val longEdge = maxOf(bounds.outWidth, bounds.outHeight)
        if (longEdge <= 0) return@runCatching false

        // KABA ORNEKLEM SINIRI KIRPIMA GORE, tam kareye gore DEGIL.
        //
        // Rehber karenin yaklasik yarisini kapliyor, yani tam kareyi
        // `maxLongEdge`e indirmek kirpimi yarisina dusururdu. Sinir iki katta
        // tutuluyor: kirpim sonrasi hala yeterli piksel kaliyor ve tam boy
        // bitmap yine hic ayrilmiyor.
        val decodeLimit = maxLongEdge * 2
        var step = 1
        while (longEdge / (step * 2) >= decodeLimit) step *= 2

        val decoded = BitmapFactory.decodeByteArray(
            source, 0, source.size,
            BitmapFactory.Options().apply { inSampleSize = step },
        ) ?: return@runCatching false

        // 1) YON PIKSELE
        val rotation = Matrix().apply { applyExifOrientation(orientationOf(source)) }
        val upright = if (rotation.isIdentity) {
            decoded
        } else {
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, rotation, true)
                .also { if (it !== decoded) decoded.recycle() }
        }

        // 2) REHBERI KAREYE ESLE
        val rect = guide.inImage(upright.width, upright.height)
        if (rect == null) {
            if (upright !== decoded) upright.recycle()
            return@runCatching false
        }

        val cropped = Bitmap.createBitmap(upright, rect.left, rect.top, rect.width, rect.height)
            .also { if (it !== upright) upright.recycle() }

        // 3) OLCEK
        val croppedLong = maxOf(cropped.width, cropped.height)
        val ratio = if (croppedLong > maxLongEdge) maxLongEdge.toFloat() / croppedLong else 1f
        val out = if (ratio == 1f) {
            cropped
        } else {
            Bitmap.createBitmap(
                cropped, 0, 0, cropped.width, cropped.height,
                Matrix().apply { postScale(ratio, ratio) }, true,
            ).also { if (it !== cropped) cropped.recycle() }
        }

        File(destPath).parentFile?.mkdirs()
        // CIKTIDA EXIF YOK - yon piksellerde (bkz. `downscaleForOcr`).
        File(destPath).outputStream().use { out.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        out.recycle()
        true
    }.getOrElse { false }
}
