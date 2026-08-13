package com.neydi.app.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.neydi.app.data.db.NEYDI_DB_FILE
import com.neydi.app.data.db.NeydiDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * Veritabani Documents altina yaziliyor: iCloud yedegine girer ve sistem
 * tarafindan temizlenmez. Caches altina koymak "telefonu bir sure kullanmadim,
 * listem gitti" demektir.
 *
 * DIKKAT: bu dosya Windows'ta DERLENMEZ, dolayisiyla DOGRULANMAMIS.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun platformModule(): Module = module {
    single<RoomDatabase.Builder<NeydiDatabase>> {
        val documents: NSURL = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        ) ?: error("iOS Documents dizini alinamadi")

        Room.databaseBuilder<NeydiDatabase>(
            name = requireNotNull(documents.URLByAppendingPathComponent(NEYDI_DB_FILE)?.path) {
                "Veritabani yolu olusturulamadi"
            },
        )
    }
}
