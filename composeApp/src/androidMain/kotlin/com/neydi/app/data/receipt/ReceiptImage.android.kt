package com.neydi.app.data.receipt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File

/**
 * IKI ASAMALI kucultme: once kaba (inSampleSize), sonra ince (matris).
 *
 * Tek asamada tam boy bitmap'i bellege alip olceklemek tam da kacinmak
 * istedigimiz OutOfMemory'yi uretirdi - 12MP bir kare ~48MB. inSampleSize kod
 * cozme SIRASINDA orneklem seyreltiyor, yani buyuk bitmap hic ayrilmiyor.
 * Ikinci asama kalan farki kapatiyor cunku inSampleSize yalnizca 2'nin
 * kuvvetleri kadar kucultebiliyor.
 *
 * YON PIKSELE ISLENIYOR (F4.20) ve bu, "kendi kameramizin karesi sekiz satira
 * cokuyor" hatasinin KOKUNDEKI duzeltme.
 *
 * CameraX `ImageCapture` dosyaya yazarken yonu EXIF ETIKETINE koyuyor,
 * pikselleri dondurmuyor - `setTargetRotation` cagrilsin ya da cagrilmasin.
 * Yani telefon dik tutulup cekilen fis diskte YAN yatmis piksellerle duruyor ve
 * dogru yon yalnizca etikette. Buradaki eski kod ise `decodeByteArray` ile
 * cozuyordu (EXIF'i yok sayar) ve `Bitmap.compress` ile yaziyordu (EXIF yazmaz):
 * yon bilgisi tam bu iki satir arasinda kayboluyordu. Sonra okuma tarafi onu
 * tahmin etmeye calisiyor, kucultulmus kopyada hicbir sey okuyamayip 0 dereceye
 * dusuyor ve fis yan haliyle okunuyordu.
 *
 * Tarayici yolunda bu hic gorunmedi cunku belge tarayicisi sayfayi zaten dik
 * veriyor - "tarayici calisiyor, kendi kameramiz cokuyor" farkinin tamami buydu.
 *
 * DONDURMEK NEDEN BURADA, okurken degil: diskteki dosyayi HER tuketen dogru
 * gormek zorunda - OCR, `BitmapRegionDecoder` (EXIF'i hic okumaz), fis
 * gorselini gosteren ekran ve yarin eklenecek her sey. Etiketi tasimak bu
 * sozlesmeyi her tuketiciye ayri ayri yuklerdi.
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
        val oran = if (kabaUzun > maxLongEdge) maxLongEdge.toFloat() / kabaUzun else 1f

        // OLCEK VE YON TEK MATRISTE: ikisini ayri ayri uygulamak araya fazladan
        // bir tam boy bitmap sokardi ve bu dosyanin butun amaci o kopyayi
        // ayirmamak. Matris bos kalirsa hic kopya cikarilmiyor.
        val donusum = Matrix().apply {
            if (oran != 1f) postScale(oran, oran)
            applyExifOrientation(orientationOf(source))
        }
        val cikti = if (donusum.isIdentity) {
            kaba
        } else {
            Bitmap.createBitmap(kaba, 0, 0, kaba.width, kaba.height, donusum, true)
                .also { if (it !== kaba) kaba.recycle() }
        }

        File(destPath).parentFile?.mkdirs()
        // JPEG 90: fis metni icin fazlasiyla yeterli, PNG'nin uctenbir boyutu.
        // Daha dusuk kalite termal fisin zaten zayif kontrastini bozuyor.
        //
        // CIKAN DOSYADA EXIF YOK ve olmasi da gerekmiyor: yon artik piksellerde.
        // Etiket birakmak "iki kere dondur" hatasina acik kapi olurdu.
        File(destPath).outputStream().use { cikti.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        cikti.recycle()
        true
    }.getOrElse { false }
}

/**
 * JPEG'in EXIF yon etiketi; okunamazsa "duz".
 *
 * `android.media.ExifInterface` YETIYOR: yalnizca JPEG isliyoruz ve akis alan
 * kurucu API 24'ten beri var (minSdk 26). AndroidX surumu daha genis bicim
 * destegi icin duruyor, burada karsiligi olmayan bir bagimlilik olurdu.
 */
private fun orientationOf(source: ByteArray): Int = runCatching {
    ExifInterface(ByteArrayInputStream(source))
        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
}.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

/**
 * EXIF yon etiketini matrise cevirir.
 *
 * AYNALAMALAR DA VAR: on kameradan gelen kare yatay aynalanmis olabiliyor ve
 * yalnizca donmeyi ele almak o kareyi ters cevrilmis metinle birakirdi. Fis
 * cekimi arka kamerayla yapiliyor ama bu fonksiyonun girdisi galeriden de
 * gelebilir.
 */
private fun Matrix.applyExifOrientation(orientation: Int) {
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            postRotate(90f)
            postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            postRotate(270f)
            postScale(-1f, 1f)
        }
    }
}
