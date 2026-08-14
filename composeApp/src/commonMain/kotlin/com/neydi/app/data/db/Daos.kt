package com.neydi.app.data.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO'lar SUSPEND-FIRST + Flow. Bloklayan sorgu imzasi hic acilmiyor: Room 2
 * aliskanligi ve sonradan tasima maliyeti cikarir.
 *
 * KAPSAM BILEREK DAR: burada yalnizca Liste ekraninin (Faz 3) ihtiyaci olanlar
 * var. Fis, fiyat ve senkron DAO'lari kendi fazlarinda gelecek - simdi yazmak
 * kullanilmayan koda bakim borcu demek.
 *
 * `deletedAt IS NULL` HER SORGUDA: gercek silme yok, tombstone var. Filtreyi
 * unutan bir sorgu silinmis satirlari geri getirir ve bu, kullanicinin
 * "sildigim urun geri geldi" diye yasadigi seydir.
 */
@Dao
interface CategoryDao {
    /** Market gezme sirasinda. sortOrder alfabetik DEGIL - bkz. CatalogSeedData. */
    @Query("SELECT * FROM category ORDER BY sortOrder")
    fun observeAll(): Flow<List<Category>>
}

@Dao
interface CatalogSeedDao {
    /**
     * Yazarken tamamlama. Yayginliga gore siralaniyor: "ek" yazan biri once
     * Ekmek gormeli, alfabetik olarak once geleni degil.
     */
    @Query(
        """
        SELECT * FROM catalog_seed
        WHERE matchKey LIKE :onEk || '%'
        ORDER BY commonalityRank
        LIMIT :limit
        """,
    )
    suspend fun search(onEk: String, limit: Int = 20): List<CatalogSeed>

    @Query("SELECT COUNT(*) FROM catalog_seed")
    suspend fun count(): Int
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM product WHERE householdId = :householdId AND deletedAt IS NULL ORDER BY name")
    fun observeAll(householdId: String): Flow<List<Product>>

    @Query("SELECT * FROM product WHERE householdId = :householdId AND matchKey = :matchKey AND deletedAt IS NULL LIMIT 1")
    suspend fun findByMatchKey(householdId: String, matchKey: String): Product?

    @Query("SELECT * FROM product WHERE id = :id AND deletedAt IS NULL")
    suspend fun byId(id: String): Product?

    @Query("SELECT * FROM product WHERE householdId = :householdId AND isStaple = 1 AND deletedAt IS NULL ORDER BY name")
    fun observeStaples(householdId: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    /** Gercek silme DEGIL - tombstone. Bkz. Conventions.kt madde 3. */
    @Query("UPDATE product SET deletedAt = :at WHERE id = :id")
    suspend fun softDelete(id: String, at: Long)
}

@Dao
interface TripDao {
    /**
     * Aktif alisveris. "Ayni anda tek aktif trip" kisitini SEMA zorlamiyor -
     * kismi index gerekir ve Room yazamiyor (F2.3). Kural repository'de;
     * bu sorgu LIMIT 1 ile en yenisini alarak bozuk duruma karsi dayanikli.
     */
    @Query(
        """
        SELECT * FROM trip
        WHERE householdId = :householdId AND completedAt IS NULL AND deletedAt IS NULL
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    fun observeActive(householdId: String): Flow<Trip?>

    @Query(
        """
        SELECT * FROM trip
        WHERE householdId = :householdId AND completedAt IS NULL AND deletedAt IS NULL
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    suspend fun activeOrNull(householdId: String): Trip?

    @Query("SELECT * FROM trip WHERE householdId = :householdId AND completedAt IS NOT NULL AND deletedAt IS NULL ORDER BY completedAt DESC LIMIT :limit")
    fun observeHistory(householdId: String, limit: Int = 50): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(trip: Trip)

    @Query("UPDATE trip SET completedAt = :at WHERE id = :id")
    suspend fun complete(id: String, at: Long)
}

@Dao
interface TripLineDao {
    @Query("SELECT * FROM trip_line WHERE tripId = :tripId AND deletedAt IS NULL ORDER BY createdAt")
    fun observeLines(tripId: String): Flow<List<TripLine>>

    /**
     * Liste ekraninin tek sorgusu. JOIN Kotlin tarafinda birlestirmekten iyi:
     * ucu ayri Flow'u combine etmek her degisimde uc yeniden yayin uretir ve
     * satirlar bir kare bosluklu gorunur.
     *
     * Siralama: once REYON (market gezme sirasi), sonra eklenme zamani.
     * Alfabetik siralamak insani markette ileri geri yurutur.
     */
    @Query(
        """
        SELECT
            tl.id            AS satirId,
            p.id             AS urunId,
            p.name           AS ad,
            tl.quantity      AS adet,
            tl.unit          AS birim,
            tl.checked       AS isaretli,
            p.isStaple       AS sabitMi,
            c.id             AS kategoriId,
            c.name           AS kategoriAdi,
            c.sortOrder      AS kategoriSirasi,
            tl.addedByMemberId AS ekleyenUyeId,
            tl.note          AS notu
        FROM trip_line tl
        JOIN product p  ON p.id = tl.productId
        JOIN category c ON c.id = p.categoryId
        WHERE tl.tripId = :tripId AND tl.deletedAt IS NULL AND p.deletedAt IS NULL
        ORDER BY c.sortOrder, tl.createdAt
        """,
    )
    fun observeListe(tripId: String): Flow<List<ListeSatiri>>

    @Query("SELECT * FROM trip_line WHERE tripId = :tripId AND productId = :productId AND deletedAt IS NULL LIMIT 1")
    suspend fun find(tripId: String, productId: String): TripLine?

    /**
     * SILINMISLERI DE GETIRIR - ve bu sart.
     *
     * Tombstone satiri tabloda KALIYOR, UNIQUE(tripId, productId) ise
     * deletedAt'i bilmiyor. Yani "listeden cikardim, sonra geri ekledim"
     * akisinda normal insert kisita carpar ve uygulama coker. Cagiran taraf
     * once buraya bakip mezardan cikarmali.
     *
     * Kismi unique index (WHERE deletedAt IS NULL) sorunu semada cozerdi ama
     * Room yazamiyor (F2.3).
     */
    @Query("SELECT * FROM trip_line WHERE tripId = :tripId AND productId = :productId LIMIT 1")
    suspend fun findIncludingDeleted(tripId: String, productId: String): TripLine?

    /**
     * ABORT, REPLACE DEGIL. UNIQUE(tripId, productId) ihlali GORULMELI:
     * REPLACE olsaydi esin ekledigi satir sessizce ezilir, adet ve
     * "kim ekledi" bilgisi kaybolurdu. Cagiran taraf once find() ile bakip
     * varsa adet artirmali - F2.3'un korudugu davranis tam olarak bu.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(line: TripLine)

    @Update
    suspend fun update(line: TripLine)

    @Query("UPDATE trip_line SET checked = :checked, checkedAt = :at WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean, at: Long?)

    @Query("UPDATE trip_line SET deletedAt = :at WHERE id = :id")
    suspend fun softDelete(id: String, at: Long)

    @Delete
    suspend fun delete(line: TripLine)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM member WHERE householdId = :householdId AND deletedAt IS NULL ORDER BY displayName")
    fun observeAll(householdId: String): Flow<List<Member>>

    @Query("SELECT * FROM member WHERE householdId = :householdId AND isSelf = 1 AND deletedAt IS NULL LIMIT 1")
    suspend fun self(householdId: String): Member?

    /**
     * Flow olarak da var, cunku uyeyi bootstrap YARATIYOR ve ekran ondan ONCE
     * acilabiliyor. Tek seferlik okuma o yaristas null donuyor ve sonra hic
     * guncellenmiyordu - her ekleme sessizce hicbir sey yapmiyordu.
     */
    @Query("SELECT * FROM member WHERE householdId = :householdId AND isSelf = 1 AND deletedAt IS NULL LIMIT 1")
    fun observeSelf(householdId: String): Flow<Member?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: Member)
}
