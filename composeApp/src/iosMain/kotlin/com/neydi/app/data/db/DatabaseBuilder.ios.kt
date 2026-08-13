package com.neydi.app.data.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS'ta veritabani Documents altina yaziliyor: iCloud yedegine girer ve
 * sistem tarafindan temizlenmez. Caches altina koymak "telefonu bir sure
 * kullanmadim, listem gitti" demektir.
 *
 * DIKKAT: bu dosya Windows'ta DERLENMEZ. Mac'e gecilene kadar dogrulanmamis
 * sayilir - hedef tanimli ama Gradle host'un desteklemedigi task'i calistirmaz.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun neydiDatabaseBuilder(): RoomDatabase.Builder<NeydiDatabase> {
    val documents: NSURL = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    ) ?: error("iOS Documents dizini alinamadi")

    return Room.databaseBuilder<NeydiDatabase>(
        name = requireNotNull(documents.URLByAppendingPathComponent(NEYDI_DB_FILE)?.path) {
            "Veritabani yolu olusturulamadi"
        },
    )
}
