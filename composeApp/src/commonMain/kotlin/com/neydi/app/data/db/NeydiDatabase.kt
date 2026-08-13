package com.neydi.app.data.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/**
 * Neydi'nin yerel veritabani. OFFLINE-FIRST: bu dosya kaynak, bulut kopya.
 *
 * F2.1'de tek entity var (Household). Kalan 15'i F2.2'de gelecek; bu adimin
 * isi zincirin ucundan ucuna calistigini kanitlamak - KSP her hedefte kod
 * uretiyor mu, veritabani cihazda gercekten aciliyor mu.
 */
@Database(
    entities = [Household::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(NeydiDatabaseConstructor::class)
abstract class NeydiDatabase : RoomDatabase() {
    abstract fun householdDao(): HouseholdDao
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
