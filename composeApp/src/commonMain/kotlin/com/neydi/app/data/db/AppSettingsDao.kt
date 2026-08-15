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
}
