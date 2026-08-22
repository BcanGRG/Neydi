package com.neydi.app.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * Hane ayarlari DAO'su - `app_settings` v3'te semaya girmisti ama HICBIR
 * OKUYANI YOKTU (bkz. NeydiDatabase KDoc: "DAO'lar bu bump'a dahil degil").
 *
 * Ilk okuyani Kurulum (tasarim karari 6): `setupCompletedAt` kurulumun bir daha
 * acilmamasini, `tempoDays` de oneri motorunun soguk baslangicini tasiyor.
 */
@Dao
interface AppSettingsDao {

    @Query("SELECT * FROM app_settings WHERE householdId = :householdId")
    suspend fun byHousehold(householdId: String): AppSettings?

    @Query("SELECT * FROM app_settings WHERE householdId = :householdId")
    fun observe(householdId: String): Flow<AppSettings?>

    /**
     * REPLACE: hane basina TEK satir var, ikinci yazma birincinin uzerine
     * gelmeli. IGNORE olsaydi kurulum ikinci kez kosunca tempo degisikligi
     * sessizce yutulurdu.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettings)

    /**
     * Gomulu katalogun bu haneye yazilmis surumunu damgalar (F2.7).
     *
     * `INSERT OR IGNORE` + `UPDATE` cifti, cunku satir HENUZ OLMAYABILIR:
     * `app_settings` yalnizca Kurulum tamamlaninca yaziliyor ve tohumlama
     * ondan once kosuyor. Tek basina `UPDATE` sessizce sifir satir gunceller
     * ve damga hic dusmezdi - katalog her acilista bastan yazilirdi.
     *
     * ⚠ **ZORUNLU KOLONLAR ACIKCA VERILIYOR** ve bu bir duzeltme: satir once
     * yalnizca `householdId` ile aciliyordu, `syncPhotos` ve `createdAt` ise
     * NOT NULL. `OR IGNORE` o ihlali **sessizce yutuyordu** - satir hic
     * dogmuyor, damga hic dusmuyor ve katalog her acilista yeniden
     * yaziliyordu. Hicbir sey patlamadan.
     */
    @Query(
        """
        INSERT OR IGNORE INTO app_settings (householdId, syncPhotos, createdAt)
        VALUES (:householdId, 0, :at)
        """,
    )
    suspend fun ensureRow(householdId: String, at: Long)

    @Query("UPDATE app_settings SET catalogSeedVersion = :version WHERE householdId = :householdId")
    suspend fun stampCatalogSeedVersion(householdId: String, version: Int)
}
