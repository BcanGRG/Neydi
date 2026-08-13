package com.neydi.app.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room 3'te DAO'lar SUSPEND-FIRST yaziliyor. Bloklayan sorgu imzalari Room 2
 * aliskanligi; burada bilerek hic acilmiyor ki sonradan tasima gerekmesin.
 * Gozlem icin Flow - Room 3 invalidation'i kendisi baglar.
 */
@Dao
interface HouseholdDao {

    @Query("SELECT * FROM household WHERE deletedAt IS NULL LIMIT 1")
    fun observeActive(): Flow<Household?>

    @Query("SELECT * FROM household WHERE deletedAt IS NULL LIMIT 1")
    suspend fun getActive(): Household?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(household: Household)

    /** Gercek silme degil - tombstone. Bkz. Household KDoc. */
    @Query("UPDATE household SET deletedAt = :at WHERE id = :id")
    suspend fun softDelete(id: String, at: Long)
}
