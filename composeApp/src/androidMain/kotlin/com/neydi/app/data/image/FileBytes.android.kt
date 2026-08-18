package com.neydi.app.data.image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

actual suspend fun writeBytesTo(destPath: String, bytes: ByteArray): Boolean =
    withContext(Dispatchers.IO) {
        runCatching {
            File(destPath).parentFile?.mkdirs()
            File(destPath).writeBytes(bytes)
            true
        }.getOrElse { false }
    }

actual suspend fun deleteFileAt(path: String): Boolean = withContext(Dispatchers.IO) {
    runCatching { File(path).delete() }.getOrElse { false }
}

actual suspend fun deleteFilesIn(dirPath: String): Int = withContext(Dispatchers.IO) {
    runCatching {
        File(dirPath).listFiles()?.count { it.isFile && it.delete() } ?: 0
    }.getOrElse { 0 }
}

actual suspend fun jpegIsComplete(path: String): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        RandomAccessFile(File(path), "r").use { file ->
            // SOI da bakiliyor: sifir baytlik bir dosya "son iki bayt" testinden
            // gecemez ama iki baytlik cop bir dosya gecebilir.
            if (file.length() < MIN_JPEG_BYTES) return@use false
            file.seek(0)
            if (file.read() != 0xFF || file.read() != 0xD8) return@use false
            file.seek(file.length() - 2)
            file.read() == 0xFF && file.read() == 0xD9
        }
    }.getOrElse { false }
}

/** SOI + EOI + arada bir sey: dort bayttan kucugu JPEG olamaz. */
private const val MIN_JPEG_BYTES = 4L
