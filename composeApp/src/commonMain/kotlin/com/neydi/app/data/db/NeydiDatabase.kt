package com.neydi.app.data.db

import androidx.room3.AutoMigration
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.DeleteColumn
import androidx.room3.DeleteTable
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.AutoMigrationSpec

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
        PriceObservation::class,
        SuggestionEvent::class,
        SuggestionBlock::class,
        AppSettings::class,
        PendingOp::class,
        SyncMeta::class,
    ],
    version = 5,
    exportSchema = true,
    // TAMAMEN OTOMATIK migration - spec yok, veri geri-doldurmasi GEREKMIYOR.
    //
    // Ilk tasarimda gerekiyordu: "kapali mi" sorusunu status = 'CLOSED'
    // cevapliyordu, dolayisiyla surum 1'in bitmis gezileri elle CLOSED'a
    // cekilmeliydi. Onu yazacak `connection.execSQL` ise commonMain'de yok
    // (androidx.sqlite onu ortak API'den bilerek cikarmis: web varyanti
    // suspend, nonWeb degil). Kapaliligi `completedAt`e baglayinca sorun
    // ortadan kalkti - eski satirlarda o alan zaten dogru.
    // v2 -> v3: **TEK TOPLU BUMP** ve bu bir tercih degil.
    //
    // Faz 5, 6 ve 7'nin dokuz ayri sema ihtiyaci vardi. Her bump bir elle cihaz
    // dansi demek (v2 kur -> veri ekle -> v3 kur, `pm clear` YAPMADAN) ve
    // F4.1'in sessiz kazasini her seferinde tekrarlayabilir: o kazada surum
    // hata ayiklamak icin gecici olarak 1'e cekilince Room `1.json` temelini
    // uzerine yazdi, diff bos cikti ve **hicbir sey yapmayan** bir migration
    // uretildi - derleme ve 93 test yesil kaldi. Dokuz degisiklik uc faza
    // dagilsa dokuz korumasiz bump olurdu.
    //
    // VE ERKEN OLMAK ZORUNDAYDI: `price_observation`, `suggestion_event` ve
    // `pending_op` bugun BOS. Tablo bosken semasindaki hata bedava. F5.1
    // kullanicinin telefonunda bir kez kostuktan sonra `price_observation`
    // hanenin tek fiyat gecmisini tutuyor ve onu yeniden sekillendirecek
    // `execSQL` YOK.
    //
    // TAMAMEN OTOMATIK KALMASININ KURALI: eklenen her NOT NULL kolon bir
    // `@ColumnInfo(defaultValue = ...)` tasiyor (`ReceiptLine.isDiscount`,
    // `ProductStats.muAdjust`), geri kalani nullable. Veri geri-doldurmasi
    // gerektiren hicbir sey eklenmedi - eklenemezdi.
    //
    // DAO'LAR BU BUMP'A DAHIL DEGIL: yeni tablolarin (`suggestion_block`,
    // `app_settings`) ve yeni kolonlarin okuyucularini kendi fazlari yaziyor
    // (F5.1, F6.1, F6.5, F6.6). Sema ile erisim ayri isler; burada yalnizca
    // tablolar bosken sekli dogru olsun diye sema tasiniyor.
    // v3 -> v4: TEK NULLABLE KOLON, `Receipt.rawOcrText` (F4.14).
    //
    // Toplu bump kurali burada UYGULANMIYOR ve sebebi var: v2->v3 dokuz
    // ihtiyaci topladi cunku hepsi biliniyordu ve tablolar bostu. Bu kolon ise
    // bir ONKOSUL - ayristirici duzeltmesi ancak ham satirlar saklanmaya
    // baslayinca dogrulanabiliyor, yani beklemek isin kendisini bloklar.
    //
    // Tamamen otomatik kalma kurali korunuyor: kolon nullable, veri
    // geri-doldurmasi GEREKMIYOR. Eski fislerde null kaliyor ve bu dogru -
    // onlarin ham satirlari gercekten bilinmiyor.
    // v4 -> v5: PIVOT. Fis tablolari dusuyor, `brand` giriyor (E11).
    //
    // Silme de tamamen otomatik: `@DeleteTable`/`@DeleteColumn` birer
    // ANNOTASYON, icinde SQL yok - yani commonMain'de `execSQL` olmamasi engel
    // degil. Room tabloyu yeniden yaratip veriyi kopyalarken silinenleri
    // disarida birakiyor.
    //
    // DORT SILME, DORT AYRI GEREKCE:
    //  - `receipt`, `receipt_line`: kaynak akis yok, yazan kod yok.
    //  - `price_observation.receiptLineId`: fis donemi izi. Tablo BOS oldugu
    //    icin bedava; dolu olsaydi da anlami kalmazdi.
    //  - `trip.totalMinor`: tek yazani `ReceiptProcessor.rollUpTripTotal`di.
    //    Ayni bump'a alindi cunku okuyanlari E8'de temizlendi ve ayri bir v6
    //    yalnizca ikinci bir cihaz dansi demek olurdu.
    //
    // `brand` NULLABLE, yani veri geri-doldurmasi gerekmiyor - toplu bump
    // kuralinin sartı korunuyor.
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = Migration4To5Spec::class),
    ],
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
    abstract fun productAliasDao(): ProductAliasDao
    abstract fun storeDao(): StoreDao
    abstract fun dataWipeDao(): DataWipeDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun priceObservationDao(): PriceObservationDao
    abstract fun productStatsDao(): ProductStatsDao
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

/**
 * v4 -> v5 silme listesi. Govdesi BOS ve oyle kalmali: `onPostMigrate`
 * gerekmiyor cunku hicbir veri tasinmiyor, yalnizca birakiliyor.
 */
@DeleteTable(tableName = "receipt")
@DeleteTable(tableName = "receipt_line")
@DeleteColumn(tableName = "price_observation", columnName = "receiptLineId")
@DeleteColumn(tableName = "trip", columnName = "totalMinor")
class Migration4To5Spec : AutoMigrationSpec

internal const val NEYDI_DB_FILE = "neydi.db"
