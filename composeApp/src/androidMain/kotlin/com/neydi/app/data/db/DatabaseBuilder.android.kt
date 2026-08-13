package com.neydi.app.data.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

/**
 * Application context Koin'den degil, Application.onCreate'te bir kez set
 * ediliyor - Koin F2.6'da devreye girecek ve o zaman burasi sadelesir.
 */
lateinit var androidAppContext: Context
    private set

fun initAndroidDbContext(context: Context) {
    androidAppContext = context.applicationContext
}

actual fun neydiDatabaseBuilder(): RoomDatabase.Builder<NeydiDatabase> =
    Room.databaseBuilder<NeydiDatabase>(
        context = androidAppContext,
        name = androidAppContext.getDatabasePath(NEYDI_DB_FILE).absolutePath,
    )
