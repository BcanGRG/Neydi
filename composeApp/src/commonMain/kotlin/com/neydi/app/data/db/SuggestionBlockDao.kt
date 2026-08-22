package com.neydi.app.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

/**
 * "Bunu onerme" listesinin okuyucusu (F6.5).
 *
 * Tablo v5 semasindan beri duruyordu ve **hicbir DAO'su yoktu**;
 * [NeydiDatabase]'in kendi KDoc'u bunu bilerek erteledigini yaziyor
 * (*"okuyucularini kendi fazlari yaziyor"*). Bu dosya o fazin kendisi.
 *
 * ## Silme YOK, `unblockedAt` VAR
 *
 * Engel kaldirilinca satir silinmiyor, `unblockedAt` doluyor. Sebep
 * [SuggestionBlock] KDoc'unda yazili: motor ayni urunu **otomatik** engellemeye
 * kalkismadan once kullanicinin kararini gorebilmeli. Satiri silseydik o karar
 * kaybolur ve uc-vurus kurali bir sonraki turda ayni urunu sessizce geri
 * engellerdi - kullanici da "ayar ise yaramiyor" derdi.
 *
 * Bu yuzden burada uc yazma var ve ucu de UPDATE/UPSERT: engelle, kaldir,
 * yeniden engelle. `DELETE` yok.
 */
@Dao
interface SuggestionBlockDao {

    /**
     * YURURLUKTEKI engellerin urun kimlikleri - motorun okudugu tek sey.
     *
     * `unblockedAt IS NULL` yururlukte olmanin sarti; `deletedAt IS NULL`
     * projenin her sorguda tekrarlanan tombstone kurali (Conventions madde 4).
     */
    @Query(
        """
        SELECT productId FROM suggestion_block
        WHERE householdId = :householdId
          AND unblockedAt IS NULL
          AND deletedAt IS NULL
        """,
    )
    suspend fun activeProductIds(householdId: String): List<String>

    /**
     * Ayarlar'daki "Onerilmeyenler" bolumu - urun ADIYLA, ve YENIDEN ESKIYE.
     *
     * JOIN burada: bolum urun adi yaziyor ve satir basina ikinci bir sorgu
     * acmak listeyi urun sayisi kadar sorguya bolerdi. Siralama `blockedAt DESC`
     * cunku kullanicinin en son verdigi karar en ustte durmali - "az once ne
     * yaptim" sorusu listenin basindan cevaplanir.
     */
    @Query(
        """
        SELECT b.productId AS productId, p.name AS name, b.source AS source
        FROM suggestion_block b
        JOIN product p ON p.id = b.productId
        WHERE b.householdId = :householdId
          AND b.unblockedAt IS NULL
          AND b.deletedAt IS NULL
          AND p.deletedAt IS NULL
        ORDER BY b.blockedAt DESC
        """,
    )
    fun observeBlocked(householdId: String): Flow<List<BlockedProduct>>

    /**
     * Engeli koyar - ya da kaldirilmis bir engeli GERI GETIRIR.
     *
     * `REPLACE`, `UNIQUE(householdId, productId)` yuzunden zorunlu: ayni urun
     * ikinci kez engellenirse yeni bir satir DOGMUYOR, eskisi eziliyor. Silmek
     * yerine ezmek, tablonun tek-satir-tek-urun sozlesmesini koruyor.
     *
     * Cagiranin `id`yi eski satirdan tasimasi GEREKMIYOR: birincil anahtar
     * degisse de benzersiz indeks ayni satiri isaret ettigi icin REPLACE eskisini
     * dusuruyor.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(block: SuggestionBlock)

    /**
     * Engeli kaldirir - "Geri al" (karar 37 etiketi).
     *
     * `unblockedAt IS NULL` kosulu tekrar engellemeyi degil, **ikinci kez
     * kaldirmayi** engelliyor: zaten kalkmis bir engelin `unblockedAt`ini
     * tazelemek, kullanicinin kararinin TARIHINI yalanlardi.
     */
    @Query(
        """
        UPDATE suggestion_block
        SET unblockedAt = :at, updatedAt = :at
        WHERE householdId = :householdId
          AND productId = :productId
          AND unblockedAt IS NULL
          AND deletedAt IS NULL
        """,
    )
    suspend fun unblock(householdId: String, productId: String, at: Long)

    /**
     * Hanenin BUTUN engel gecmisi - kaldirilmislar dahil.
     *
     * [observeBlocked]'in aksine `unblockedAt` suzmuyor, cunku okudugu sey
     * "bugun neyi onermiyoruz" degil **"kullanici bugune kadar ne karar
     * verdi"**. Satirlarin silinmek yerine saklanmasinin tek sebebi bu sorgu:
     * uc-vurus otomatik bastirma yazildiginda motor, bir urunu geri
     * engellemeden once kullanicinin onu ELLE serbest biraktigini gorebilmeli
     * ([SuggestionBlock] KDoc'u: *"kullanicinin elle kaldirdigi bir engeli
     * motorun sessizce geri koymasi, ayarin ise yaramadigi hissi verir"*).
     *
     * Bugun yalnizca testler cagiriyor; cagiran kodu AUTO yarisi getirecek.
     */
    @Query(
        """
        SELECT * FROM suggestion_block
        WHERE householdId = :householdId AND deletedAt IS NULL
        ORDER BY blockedAt
        """,
    )
    suspend fun blockHistory(householdId: String): List<SuggestionBlock>

    /** Tek urunun yururlukteki engeli - Urun Detayi anahtarinin baslangic hali. */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM suggestion_block
            WHERE householdId = :householdId
              AND productId = :productId
              AND unblockedAt IS NULL
              AND deletedAt IS NULL
        )
        """,
    )
    suspend fun isBlocked(householdId: String, productId: String): Boolean
}

/**
 * Ayarlar satiri: engellenen urun, adiyla.
 *
 * @property source AUTO mu MANUAL mi. Tasarim bugun ikisini AYNI ciziyor;
 *   ayrim yine de tasiniyor cunku ayirt etmemek bir karar ve o karar
 *   tasarima soruldu (`docs/28`). Tasinmasaydi cevap geldiginde sorgu da
 *   degismek zorunda kalirdi.
 */
data class BlockedProduct(
    val productId: String,
    val name: String,
    val source: BlockSource,
)
