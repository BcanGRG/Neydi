package com.neydi.app.data.ocr

import android.content.Context
import com.neydi.app.data.image.downscaleForOcr
import java.io.File

/**
 * GECICI KOSUM (E12) - fikstur uretildikten sonra SILINECEK.
 *
 * Gercek etiket fotograflarini ML Kit'ten gecirip ham ciktiyi diske doker.
 * Yeni bir gradle kaynak kumesi acilmadi: ML Kit bagimliligi zaten
 * `composeApp/androidMain`in sinifinda, uygulama zaten cihazda kosuyor ve
 * eksik olan tek sey bir TETIKLEYICI.
 *
 * ## Girdi / cikti
 *
 * Girdi: `etiket-in/` altindaki jpg'ler, `<externalFilesDir>` icinde (adb ile
 * push - uygulama-ozel
 * dizin, izin gerektirmiyor). Cikti: `<externalFilesDir>/etiket-out/`.
 *
 * ## `downscaleForOcr` FALSE DONERSE FOTOGRAF ATLANIYOR
 *
 * Sozlesme *"false = kucultme basarisiz; cagiran HAM BAYTLARI ayni yola
 * yazmali"* diyor ve o kural KULLANICININ TEK KANITINI korumak icin var - bir
 * kareyi kucultemedik diye atmak kabul edilemez. Ama burada is fikstur uretmek,
 * kanit korumak degil: ham baytlar EXIF tasiyor ve `BitmapFactory.decodeFile`
 * EXIF'i yok sayiyor, yani o fotograf ML Kit'e YAN girer ve dosyaya `exif`
 * degeriyle birlikte sessizce yazilir. Sonra JVM'de yesil bir test o donmus
 * geometriyi DONDURUR.
 *
 * Yani atlamak dogrusu, ve GURULTUYLE atliyor: cikti dosyasina `SKIPPED`
 * satiri dusuyor. Bu kodda `false` yalnizca gercek bir cozme hatasinda
 * donuyor (bkz. `OcrImage.android.kt`) - olcek gerekmese de EXIF piksele
 * isleniyor, yani beklenen bir hal degil.
 */
suspend fun dumpTagFixtures(context: Context): String {
    val inDir = File(context.getExternalFilesDir(null), "etiket-in")
    val outDir = File(context.getExternalFilesDir(null), "etiket-out").apply { mkdirs() }
    val photos = inDir.listFiles { f -> f.extension.lowercase() in setOf("jpg", "jpeg") }
        ?.sortedBy { it.name }
        ?: return "etiket-in yok: ${inDir.absolutePath}"

    val log = StringBuilder("photos=${photos.size}\n")
    photos.forEach { photo ->
        val staged = File(outDir, "${photo.nameWithoutExtension}.staged.jpg")
        val ok = downscaleForOcr(photo.readBytes(), staged.absolutePath)
        if (!ok) {
            File(outDir, "${photo.nameWithoutExtension}.txt")
                .writeText("tag=${photo.nameWithoutExtension}\nSKIPPED=downscaleForOcr-failed\n")
            log.append("SKIP ${photo.name}\n")
            return@forEach
        }
        // EXIF KAYNAKTAN OKUNUYOR, hazirlanmis dosyadan DEGIL: `downscaleForOcr`
        // yonu piksele isleyip etiketsiz yaziyor, yani orada okunacak bir sey
        // yok. Ilk surum oradan okuyordu ve her fiksture `0` yaziyordu.
        val ocr = runCatching { readTag(staged.absolutePath, exifOf(photo)) }
        staged.delete()
        ocr.fold(
            onSuccess = { r ->
                File(outDir, "${photo.nameWithoutExtension}.txt").writeText(serialize(photo.name, r))
                log.append("OK ${photo.name} lines=${r.lines.size} words=${r.words.size}\n")
            },
            onFailure = { e ->
                File(outDir, "${photo.nameWithoutExtension}.txt")
                    .writeText("tag=${photo.nameWithoutExtension}\nFAILED=${e.message}\n")
                log.append("FAIL ${photo.name} ${e.message}\n")
            },
        )
    }
    File(outDir, "_summary.txt").writeText(log.toString())
    return log.toString()
}

/**
 * SATIR TABANLI METIN, JSON degil.
 *
 * Fikstur bir yil sonra da okunacak ve `git diff`i anlamli olmali; JSON'da tek
 * bir kose noktasi degisince butun satir yeniden sarilir. Ayrica JVM tarafinda
 * sifir bagimlilikla ayristirilabiliyor - projede hicbir yerde JSON okuyucu yok.
 *
 * Kose noktalari HAM gorsel eksenlerinde ve `x,y` ciftleri bosluklu; sira
 * ML Kit'in verdigi sira, DEGISTIRILMIYOR - bu dosyanin butun degeri ML Kit'in
 * gercekte ne verdigini kaydetmesi.
 */
private fun serialize(sourceName: String, r: TagOcr): String = buildString {
    appendLine("tag=${sourceName.substringBeforeLast('.')}")
    appendLine("source=$sourceName")
    appendLine("exif=${r.exifOrientation}")
    appendLine("size=${r.sourceWidth}x${r.sourceHeight}")
    appendLine("lines=${r.lines.size}")
    appendLine("words=${r.words.size}")
    appendLine("--- lines")
    r.lines.forEach { appendLine(pieceLine(it)) }
    appendLine("--- words")
    r.words.forEach { appendLine(pieceLine(it)) }
}

private fun pieceLine(p: OcrPiece): String {
    val corners = p.corners.joinToString(" ") { "${it.x},${it.y}" }
    val flag = if (p.orientationKnown) "" else " NOCORNERS"
    // Metin SONDA ve sekmeyle ayrilmis: icinde bosluk olabilir, sekme olmaz.
    return "$corners$flag\t${p.text}"
}

/** Kaynak JPEG'in EXIF yon etiketi; okunamazsa 1 (duz). */
private fun exifOf(file: File): Int = runCatching {
    android.media.ExifInterface(file.absolutePath).getAttributeInt(
        android.media.ExifInterface.TAG_ORIENTATION,
        android.media.ExifInterface.ORIENTATION_NORMAL,
    )
}.getOrDefault(android.media.ExifInterface.ORIENTATION_NORMAL)
