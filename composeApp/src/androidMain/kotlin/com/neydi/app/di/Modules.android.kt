package com.neydi.app.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.neydi.app.data.db.NEYDI_DB_FILE
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.receipt.ReceiptReader
import com.neydi.app.data.receipt.MlKitReceiptReader
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Context artik `lateinit var` DEGIL, Koin'den geliyor. Onceki hal
 * Application.onCreate ile Activity.onCreate arasinda kurulumu unutmaya acikti
 * ve unutulursa hata veritabanina ILK erisimde, cok sonra patliyordu.
 */
actual fun platformModule(): Module = module {
    single<ReceiptReader> { MlKitReceiptReader(androidContext()) }

    single<RoomDatabase.Builder<NeydiDatabase>> {
        val context: Context = androidContext()
        Room.databaseBuilder<NeydiDatabase>(
            context = context,
            name = context.getDatabasePath(NEYDI_DB_FILE).absolutePath,
        )
    }
}

/** Disk beklemesi icin dogru havuz: JVM'de IO. */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
