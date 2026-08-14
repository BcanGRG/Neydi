package com.neydi.app.di

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.bootstrap
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.ui.list.ListViewModel
import kotlinx.coroutines.Dispatchers
// kotlinx.datetime.Clock artik kotlin.time.Clock'a deprecate typealias.
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Veritabani dosyasinin YOLU platforma ozel; kurulumun geri kalani degil.
 * Ozellikle surucu ortak: BundledSQLiteDriver ayni SQLite'i iki platforma da
 * getiriyor, yoksa Android sistem SQLite'ini kullanir ve "bende calisiyor"
 * hatalari baslar.
 */
expect fun platformModule(): Module

@OptIn(ExperimentalTime::class)
val dataModule = module {
    single<NeydiDatabase> {
        get<RoomDatabase.Builder<NeydiDatabase>>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<NeydiDatabase>().householdDao() }
    single { get<NeydiDatabase>().memberDao() }
    single { get<NeydiDatabase>().categoryDao() }
    single { get<NeydiDatabase>().catalogSeedDao() }
    single { get<NeydiDatabase>().productDao() }
    single { get<NeydiDatabase>().tripDao() }
    single { get<NeydiDatabase>().tripLineDao() }
    single { get<NeydiDatabase>().priceObservationDao() }

    // Acilis hazirligini saat/id ile birlikte tasiyan kucuk sarmalayici:
    // App() bunlari kendi uretmek zorunda kalmasin.
    single { AppBootstrap(db = get(), clock = ::now, newId = ::newUuid) }

    viewModel {
        ListViewModel(
            repo = get(), tripLineDao = get(), memberDao = get(),
            productDao = get(), catalogSeedDao = get(), categoryDao = get(),
            priceObservationDao = get(),
        )
    }

    single {
        ListRepository(
            tripDao = get(),
            tripLineDao = get(),
            productDao = get(),
            // Saat ve id URETIMI disaridan: repository saf kalsin ve testte
            // deterministik olabilsin.
            clock = ::now,
            newId = ::newUuid,
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun newUuid(): String = Uuid.random().toString()

@OptIn(ExperimentalTime::class)
internal fun now(): Long = Clock.System.now().toEpochMilliseconds()

/** Acilis hazirligi. Idempotent - bkz. Bootstrap.kt */
class AppBootstrap(
    private val db: NeydiDatabase,
    private val clock: () -> Long,
    private val newId: () -> String,
) {
    suspend operator fun invoke() = db.bootstrap(newId = newId, clock = clock)
}

fun initKoin(extra: KoinApplication.() -> Unit = {}): KoinApplication = startKoin {
    modules(platformModule(), dataModule)
    extra()
}
