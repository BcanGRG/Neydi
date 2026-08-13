package com.neydi.app.data.db

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/**
 * Veritabani dosyasinin YOLU platforma ozel; kurulumun geri kalani degil.
 * Ortak olan her sey burada kalsin ki iki platform sessizce farklilasmasin -
 * ozellikle surucu: BundledSQLiteDriver ayni SQLite'i her iki platforma da
 * getiriyor, yoksa Android sistem SQLite'ini, iOS baskasini kullanir ve
 * "bende calisiyor" hatalari baslar.
 */
expect fun neydiDatabaseBuilder(): RoomDatabase.Builder<NeydiDatabase>

fun buildNeydiDatabase(): NeydiDatabase =
    neydiDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
