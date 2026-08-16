package com.neydi.app.data.image

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

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
