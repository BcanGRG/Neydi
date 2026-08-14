package com.neydi.app.data.receipt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * IKI ASAMALI kucultme: once kaba (inSampleSize), sonra ince (createScaledBitmap).
 *
 * Tek asamada tam boy bitmap'i bellege alip olceklemek tam da kacinmak
 * istedigimiz OutOfMemory'yi uretirdi - 12MP bir kare ~48MB. inSampleSize kod
 * cozme SIRASINDA orneklem seyreltiyor, yani buyuk bitmap hic ayrilmiyor.
 * Ikinci asama kalan farki kapatiyor cunku inSampleSize yalnizca 2'nin
 * kuvvetleri kadar kucultebiliyor.
 */
actual suspend fun downscaleForOcr(
    source: ByteArray,
    destPath: String,
    maxLongEdge: Int,
): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        val boyut = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(source, 0, source.size, boyut)
        val uzunKenar = maxOf(boyut.outWidth, boyut.outHeight)
        if (uzunKenar <= 0) return@runCatching false

        var adim = 1
        while (uzunKenar / (adim * 2) >= maxLongEdge) adim *= 2

        val kaba = BitmapFactory.decodeByteArray(
            source, 0, source.size,
            BitmapFactory.Options().apply { inSampleSize = adim },
        ) ?: return@runCatching false

        val kabaUzun = maxOf(kaba.width, kaba.height)
        val cikti = if (kabaUzun > maxLongEdge) {
            val oran = maxLongEdge.toFloat() / kabaUzun
            Bitmap.createScaledBitmap(
                kaba,
                (kaba.width * oran).toInt().coerceAtLeast(1),
                (kaba.height * oran).toInt().coerceAtLeast(1),
                true,
            ).also { if (it !== kaba) kaba.recycle() }
        } else {
            kaba
        }

        File(destPath).parentFile?.mkdirs()
        // JPEG 90: fis metni icin fazlasiyla yeterli, PNG'nin uctenbir boyutu.
        // Daha dusuk kalite termal fisin zaten zayif kontrastini bozuyor.
        File(destPath).outputStream().use { cikti.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        cikti.recycle()
        true
    }.getOrElse { false }
}
