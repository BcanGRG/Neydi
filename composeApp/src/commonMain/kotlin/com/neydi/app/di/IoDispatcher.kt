package com.neydi.app.di

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Room sorgularinin kostugu dispatcher.
 *
 * NEDEN expect/actual: `Dispatchers.IO` commonMain'den ERISILEMIYOR - native
 * hedeflerde `internal`. Bunu derleyici soyledi (`Cannot access 'val IO':
 * it is internal in 'kotlinx.coroutines.Dispatchers'`), yani commonMain'de
 * `Dispatchers.IO` yazan kod Android'de derlenir ama iOS'ta derlenmez ve hata
 * yalnizca iOS hedefi ilk kez derlenince ortaya cikar.
 *
 * Android/JVM tarafinda IO havuzu (disk beklemesi icin dogru olan), iOS'ta
 * `Dispatchers.Default` - native'de ayri bir IO havuzu yok ve Room zaten
 * BundledSQLite uzerinden senkron cagiriyor.
 */
expect val ioDispatcher: CoroutineDispatcher
