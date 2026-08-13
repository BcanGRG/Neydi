package com.neydi.app.data.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/**
 * Neydi'nin yerel veritabani. OFFLINE-FIRST: bu dosya kaynak, bulut kopya.
 *
 * 16 entity. Ortak sozlesme Conventions.kt'de - yeni tablo eklemeden ONCE oku.
 *
 * KISITLAR HENUZ YOK: UNIQUE(tripId, productId) ve
 * UNIQUE(householdId, storeChain, rawTextNormalized) F2.3'te geliyor. O zamana
 * kadar sema bunlari ZORLAMIYOR - iki es ayni urunu eklerse veritabani bugun
 * kabul eder.
 *
 * Surum 1 ve henuz yayinlanmis veri yok, o yuzden F2.3 migration degil sema
 * yenilemesi olacak.
 */
@Database(
    entities = [
        Household::class,
        Member::class,
        Category::class,
        CatalogSeed::class,
        Product::class,
        ProductAlias::class,
        ProductStats::class,
        Store::class,
        Trip::class,
        TripLine::class,
        Receipt::class,
        ReceiptLine::class,
        PriceObservation::class,
        SuggestionEvent::class,
        PendingOp::class,
        SyncMeta::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(NeydiDatabaseConstructor::class)
abstract class NeydiDatabase : RoomDatabase() {
    abstract fun householdDao(): HouseholdDao
    abstract fun memberDao(): MemberDao
    abstract fun categoryDao(): CategoryDao
    abstract fun catalogSeedDao(): CatalogSeedDao
    abstract fun productDao(): ProductDao
    abstract fun tripDao(): TripDao
    abstract fun tripLineDao(): TripLineDao
}

/**
 * `actual`ini Room'un KSP islemcisi HER hedef icin kendisi uretir - elle
 * yazilmaz. Bir hedef icin ksp bagimliligi eklenmemisse "actual bulunamadi"
 * hatasini o hedef derlenirken alirsin; iOS'unki Mac'e gecene kadar gorunmez.
 */
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object NeydiDatabaseConstructor : RoomDatabaseConstructor<NeydiDatabase> {
    override fun initialize(): NeydiDatabase
}

internal const val NEYDI_DB_FILE = "neydi.db"
