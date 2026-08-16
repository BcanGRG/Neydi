package com.neydi.app.data.db

import androidx.room3.Dao
import androidx.room3.Query

/**
 * "Verilerimi sil" icin sayim ve silme (tasarim karari 2).
 *
 * SOZLESME 3'UN BILINCLI ISTISNASI. Conventions.kt "GERCEK SILME YOK, deletedAt
 * tombstone" diyor ve gerekcesi dogru: iki telefon cevrimdisiyken satir bazinda
 * gercek silme "esimin ekledigi urun kayboldu" diye tezahur eder. Ama o kural
 * TEK SATIRIN silinmesini koruyor; burada hanenin TAMAMI gidiyor ve uzlastirilacak
 * bir sey kalmiyor. Ustelik kullaniciya "geri alma yok, yedek yok" diye soz
 * verilen bir ekranda her satiri diskte tutmak, yazili sozun tersini yapmak
 * olurdu - gizlilik ekraninda soylenen sey en cok tutulmasi gereken sozdur.
 *
 * KAPSAM DISI: `household`, `member`, `category`, `catalog_seed` ve
 * `app_settings`. Ilk ikisi hanenin kendisi (silme "hesabi kapat" degil,
 * "verilerimi sil"); sonraki ikisi uygulamayla gelen referans verisi, kimseye
 * ait degil; sonuncusu da karar 2'nin saydigi kapsamda gecmiyor - kurulum
 * durumunu silmek kullaniciyi ilk gune geri atardi, oysa istedigi sey verisinin
 * gitmesi.
 */
@Dao
interface DataWipeDao {

    // --- Sayim: ekran neyin gidecegini RAKAMLA yaziyor ----------------------

    @Query("SELECT COUNT(*) FROM trip WHERE householdId = :householdId AND deletedAt IS NULL")
    suspend fun countTrips(householdId: String): Int

    @Query("SELECT COUNT(*) FROM product WHERE householdId = :householdId AND deletedAt IS NULL")
    suspend fun countProducts(householdId: String): Int

    @Query("SELECT COUNT(*) FROM price_observation WHERE householdId = :householdId AND deletedAt IS NULL")
    suspend fun countPrices(householdId: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM product
        WHERE householdId = :householdId AND deletedAt IS NULL AND isStaple = 1
        """,
    )
    suspend fun countStaples(householdId: String): Int

    @Query("SELECT COUNT(*) FROM suggestion_block WHERE householdId = :householdId AND deletedAt IS NULL")
    suspend fun countBlocks(householdId: String): Int

    // --- Silme --------------------------------------------------------------
    //
    // Sira COCUKTAN EBEVEYNE: bugun sema yabanci anahtar tasimasa da satirlari
    // once cocuklardan temizlemek, kisit eklendigi gun bu kodun sessizce
    // bozulmasini engelliyor.

    @Query("DELETE FROM trip_line WHERE householdId = :householdId")
    suspend fun deleteTripLines(householdId: String)

    @Query("DELETE FROM trip WHERE householdId = :householdId")
    suspend fun deleteTrips(householdId: String)

    @Query("DELETE FROM price_observation WHERE householdId = :householdId")
    suspend fun deletePrices(householdId: String)

    @Query("DELETE FROM product_alias WHERE householdId = :householdId")
    suspend fun deleteAliases(householdId: String)

    @Query("DELETE FROM product_stats WHERE householdId = :householdId")
    suspend fun deleteStats(householdId: String)

    @Query("DELETE FROM suggestion_event WHERE householdId = :householdId")
    suspend fun deleteSuggestionEvents(householdId: String)

    @Query("DELETE FROM suggestion_block WHERE householdId = :householdId")
    suspend fun deleteSuggestionBlocks(householdId: String)

    @Query("DELETE FROM product WHERE householdId = :householdId")
    suspend fun deleteProducts(householdId: String)

    @Query("DELETE FROM store WHERE householdId = :householdId")
    suspend fun deleteStores(householdId: String)
}
