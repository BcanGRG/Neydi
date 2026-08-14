package com.neydi.app.data.receipt

/** iOS dosya yazimi Faz 8'de - kucultme de orada gelecek. */
actual suspend fun writeBytesTo(destPath: String, bytes: ByteArray): Boolean = false

/** iOS dosya silme Faz 8'de. */
actual suspend fun deleteFileAt(path: String): Boolean = false
