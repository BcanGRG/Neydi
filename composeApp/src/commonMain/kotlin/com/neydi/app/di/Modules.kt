package com.neydi.app.di

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.bootstrap
import com.neydi.app.data.repo.DataWipe
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.receipt.ReceiptProcessor
import com.neydi.app.data.stats.ProductStatsRebuilder
import com.neydi.app.data.suggest.SuggestionEngine
import com.neydi.app.ui.finish.FinishShoppingViewModel
import com.neydi.app.ui.history.HistoryViewModel
import com.neydi.app.ui.list.ListViewModel
import com.neydi.app.ui.missing.MissingItemsViewModel
import com.neydi.app.ui.settings.DeleteDataViewModel
import com.neydi.app.ui.settings.SettingsViewModel
import com.neydi.app.ui.setup.SetupViewModel
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
            .setQueryCoroutineContext(ioDispatcher)
            .build()
    }

    single { get<NeydiDatabase>().householdDao() }
    single { get<NeydiDatabase>().memberDao() }
    single { get<NeydiDatabase>().categoryDao() }
    single { get<NeydiDatabase>().catalogSeedDao() }
    single { get<NeydiDatabase>().productDao() }
    single { get<NeydiDatabase>().tripDao() }
    single { get<NeydiDatabase>().tripLineDao() }
    single { get<NeydiDatabase>().receiptDao() }
    single { get<NeydiDatabase>().receiptLineDao() }
    single { get<NeydiDatabase>().productAliasDao() }
    single { get<NeydiDatabase>().storeDao() }
    single { get<NeydiDatabase>().dataWipeDao() }
    single { get<NeydiDatabase>().appSettingsDao() }
    single { get<NeydiDatabase>().priceObservationDao() }
    single { get<NeydiDatabase>().productStatsDao() }

    // Acilis hazirligini saat/id ile birlikte tasiyan kucuk sarmalayici:
    // App() bunlari kendi uretmek zorunda kalmasin.
    single { AppBootstrap(db = get(), clock = ::now, newId = ::newUuid) }

    viewModel {
        ListViewModel(
            repo = get(), tripDao = get(), tripLineDao = get(), memberDao = get(),
            productDao = get(), catalogSeedDao = get(), categoryDao = get(),
            priceObservationDao = get(),
            statsRebuilder = get(),
            suggestionEngine = get(),
        )
    }

    viewModel {
        MissingItemsViewModel(
            engine = get(), repo = get(),
            productDao = get(), memberDao = get(),
        )
    }

    viewModel {
        SettingsViewModel(
            householdDao = get(), memberDao = get(),
            productDao = get(), storeDao = get(), clock = ::now,
        )
    }

    viewModel { DeleteDataViewModel(wipe = get(), memberDao = get()) }

    viewModel {
        SetupViewModel(
            catalogSeedDao = get(), settingsDao = get(),
            memberDao = get(), repo = get(), clock = ::now,
        )
    }

    viewModel {
        HistoryViewModel(tripDao = get(), tripLineDao = get())
    }

    viewModel { (tripId: String?) ->
        FinishShoppingViewModel(tripId = tripId, tripLineDao = get(), repo = get(), statsRebuilder = get())
    }

    single { DataWipe(dao = get()) }
    single { ProductStatsRebuilder(statsDao = get(), clock = ::now) }
    single {
        SuggestionEngine(
            statsDao = get(), productDao = get(), tripDao = get(),
            tripLineDao = get(), clock = ::now,
        )
    }

    single {
        ReceiptProcessor(
            reader = get(),
            receiptDao = get(),
            receiptLineDao = get(),
            productDao = get(),
            aliasDao = get(),
            tripDao = get(),
            storeDao = get(),
            statsRebuilder = get(),
            clock = ::now,
            newId = ::newUuid,
        )
    }

    single {
        ListRepository(
            tripDao = get(),
            tripLineDao = get(),
            receiptDao = get(),
            productDao = get(),
            // Saat ve id URETIMI disaridan: repository saf kalsin ve testte
            // deterministik olabilsin.
            clock = ::now,
            newId = ::newUuid,
        )
    }
}

/**
 * Satir kimligi - UUID **v7**.
 *
 * Ilk hali `Uuid.generateV7()` cagiriyordu ve o **v4** uretiyor (kotlin-stdlib
 * 2.4.10, `Uuid.kt:581`: `random() = generateV4()`), yani Conventions.kt madde
 * 1'in "v7, v4 DEGIL" kurali kodda tam tersine calisiyordu. Uygulamada
 * yazilmis her satir rastgele onekli bir id aldi.
 *
 * NEDEN ONEMLI: Room id'leri TEXT saklıyor, yani siralama sozluk sirasi -
 * v7'nin onundeki zaman damgasi gercekten siralaniyor, v4 siralanmiyor; birincil
 * anahtar index'i sona ekleniyor, ortasina serpistirilmiyor. Ustelik iki cihaz
 * kimlikleri bagimsiz uretecek (Faz 7) ve karisik nesilli bir id uzayi kalici
 * olur. Simdi bir satir, Faz 7'den sonra sonsuza kadar.
 *
 * Mevcut v4 satirlar GECERLI kaliyor - bu bir migration degil, yalnizca yeni
 * id'ler degisiyor. Katalog tohum id'leri (`seed-<yayginlik>`) bilerek
 * deterministik ve bundan etkilenmiyor.
 */
@OptIn(ExperimentalUuidApi::class)
internal fun newUuid(): String = Uuid.generateV7().toString()

@OptIn(ExperimentalTime::class)
internal fun now(): Long = Clock.System.now().toEpochMilliseconds()

/** Acilis hazirligi. Idempotent - bkz. Bootstrap.kt */
class AppBootstrap(
    private val db: NeydiDatabase,
    private val clock: () -> Long,
    private val newId: () -> String,
) {
    suspend operator fun invoke() = db.bootstrap(newId = newId, clock = clock)

    /**
     * Kurulum (Ekran 8) acilacak mi (tasarim karari 6).
     *
     * IKI KOSUL VE IKISI DE SART. `setupCompletedAt` tek basina yetmiyor:
     * mevcut kurulumlarda o alan bos ve dolu bir veritabaninin ustune
     * onboarding acilirdi - kullanici uygulamayi aylardir kullaniyor olsa bile.
     * "Hane hic urun gormedi" bunu tek sorguyla kesiyor.
     *
     * `bootstrap()` HANEYI HER ACILISTA KOSULSUZ YARATIYOR, yani "hane yoksa
     * Kurulum'u goster" hicbir zaman dogru olamazdi - bu yuzden bayrak ayri bir
     * tabloda (`app_settings`) duruyor.
     */
    suspend fun needsSetup(): Boolean =
        db.appSettingsDao().byHousehold(DEFAULT_HOUSEHOLD_ID)?.setupCompletedAt == null &&
            db.productDao().count(DEFAULT_HOUSEHOLD_ID) == 0
}

fun initKoin(extra: KoinApplication.() -> Unit = {}): KoinApplication = startKoin {
    modules(platformModule(), dataModule)
    extra()
}
