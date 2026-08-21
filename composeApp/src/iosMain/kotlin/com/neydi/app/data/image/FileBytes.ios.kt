package com.neydi.app.data.image

/** iOS dosya yazimi Faz 8'de - kucultme de orada gelecek. */
actual suspend fun writeBytesTo(destPath: String, bytes: ByteArray): Boolean = false

/** iOS dosya silme Faz 8'de. */
actual suspend fun deleteFileAt(path: String): Boolean = false

/** iOS klasor temizligi Faz 8'de - digerlerinin ikizi. */
actual suspend fun deleteFilesIn(dirPath: String): Int = 0

/**
 * iOS dosya yollari Faz 8'de.
 *
 * TRUE DONUYOR, false degil: iOS'ta cekim yolu HENUZ YOK, yani burada false
 * demek olmayan bir kareyi bozuk ilan etmek olurdu. Faz 8'de gercek kontrol
 * gelene kadar bu fonksiyon hicbir sey iddia etmiyor.
 */
actual suspend fun jpegIsComplete(path: String): Boolean = true

/** iOS dosya yollari Faz 8'de; olcum ice aktarimi Android'de yapiliyor. */
actual suspend fun listFilesIn(dirPath: String): List<String> = emptyList()
