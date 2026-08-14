package com.neydi.app

import android.app.Application
import com.neydi.app.di.initKoin
import org.koin.android.ext.koin.androidContext

/**
 * Koin BURADA baslatiliyor, Activity'de degil: veritabani ilk erisimde
 * kuruluyor ve Activity yeniden yaratildiginda tekrar baslatmak
 * "Koin already started" hatasi verirdi.
 */
class NeydiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@NeydiApplication) }
    }
}
