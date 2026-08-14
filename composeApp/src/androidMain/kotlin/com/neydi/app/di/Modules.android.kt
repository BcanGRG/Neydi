package com.neydi.app.di

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.neydi.app.data.db.NEYDI_DB_FILE
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.fis.FisOkuyucu
import com.neydi.app.data.fis.MlKitFisOkuyucu
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Context artik `lateinit var` DEGIL, Koin'den geliyor. Onceki hal
 * Application.onCreate ile Activity.onCreate arasinda kurulumu unutmaya acikti
 * ve unutulursa hata veritabanina ILK erisimde, cok sonra patliyordu.
 */
actual fun platformModule(): Module = module {
    single<FisOkuyucu> { MlKitFisOkuyucu(androidContext()) }

    single<RoomDatabase.Builder<NeydiDatabase>> {
        val context: Context = androidContext()
        Room.databaseBuilder<NeydiDatabase>(
            context = context,
            name = context.getDatabasePath(NEYDI_DB_FILE).absolutePath,
        )
    }
}
