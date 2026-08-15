package com.neydi.app.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Bir urunun tek bir gezideki satin alinma olayi.
 *
 * `tripId` TASINIYOR cunku tekilligin anahtari o: ayni urun hem listede
 * isaretli hem fiste eslesmis olabilir ve bu **bir** alistir, iki degil.
 */
data class PurchaseEvent(
    val productId: String,
    val tripId: String,
    val purchasedAt: Long,
)

/**
 * Turetilmis istatistik tablosunun DAO'su (F6.1).
 *
 * TABLO BIR ONBELLEK, kaynak degil: TripLine ve ReceiptLine'lardan yeniden
 * kurulabiliyor, o yuzden senkron edilmiyor ve tombstone tasimiyor. Bozulursa
 * silinip yeniden uretiliyor - `ProductStats` KDoc'u bunu yaziyor.
 */
@Dao
interface ProductStatsDao {

    /**
     * SATIN ALMA OLAYLARI - istatistigin tek girdisi.
     *
     * **IKI KAYNAK BIRLESIYOR ve bu bilincli bir karar:** listede isaretlenmis
     * satirlar **ve** fiste bir urune baglanmis satirlar. Yalnizca `trip_line`
     * okumak, tam olarak kullanicinin **listeye yazmayi unuttugu** urunleri
     * saymamak demekti - yani Faz 4'un var olma sebebini ("fis, listeye
     * yazilmasa bile ekmegi iceriyor") es gecmek.
     *
     * **TEKILLESTIRME (productId, tripId) UZERINDE ve bu sart:** ayni urun hem
     * listede isaretli hem fiste eslesmis olabilir. Tekillestirilmezse
     * `purchaseCount` ikiye katlanir ve `medianIntervalDays` **yariya duser** -
     * yani uygulama her seyi iki kat sik onermeye baslar. Sessiz, yavas ve geri
     * alinmasi zor bir bozulma; ayni tehlike gezi kapanisinin cift kosmasinda da
     * kayitli.
     *
     * **ZAMAN DAMGASI `trip.completedAt`, `trip_line.checkedAt` DEGIL.**
     * Iyimser mutabakat (F4.8) kapanista isaretlenmemis **her** satira kapanis
     * damgasini yaziyor, yani tembel kullanimda - F4.8'in bekledigi yaygin
     * durum - butun gezinin `checkedAt`'i ayni. O alan satin alma ani olarak
     * kullanilamaz.
     *
     * **MIN() ile fis tarihi kazaniyor:** fiste basili tarih satin almanin
     * gerceklestigi an, gezinin kapanmasi ise kullanicinin "bitir"e bastigi an.
     * Ikisi ayni gun olsa da fis daha guvenilir kayit.
     */
    @Query(
        """
        SELECT productId, tripId, MIN(purchasedAt) AS purchasedAt FROM (
            SELECT tl.productId AS productId, t.id AS tripId, t.completedAt AS purchasedAt
            FROM trip_line tl
            JOIN trip t ON t.id = tl.tripId
            WHERE t.householdId = :householdId
              AND t.completedAt IS NOT NULL AND t.deletedAt IS NULL
              AND tl.checked = 1 AND tl.deletedAt IS NULL

            UNION ALL

            SELECT rl.matchedProductId AS productId, t.id AS tripId,
                   COALESCE(r.receiptDate, t.completedAt) AS purchasedAt
            FROM receipt_line rl
            JOIN receipt r ON r.id = rl.receiptId
            JOIN trip t ON t.id = r.tripId
            WHERE t.householdId = :householdId
              AND t.completedAt IS NOT NULL AND t.deletedAt IS NULL
              AND rl.matchedProductId IS NOT NULL AND rl.needsReview = 0
              AND rl.deletedAt IS NULL AND r.deletedAt IS NULL
        )
        GROUP BY productId, tripId
        ORDER BY productId, purchasedAt
        """,
    )
    suspend fun purchaseEvents(householdId: String): List<PurchaseEvent>

    @Query("SELECT * FROM product_stats WHERE productId = :productId")
    suspend fun byProduct(productId: String): ProductStats?

    @Query("SELECT * FROM product_stats WHERE householdId = :householdId")
    fun observeAll(householdId: String): Flow<List<ProductStats>>

    @Query("DELETE FROM product_stats WHERE householdId = :householdId")
    suspend fun clear(householdId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<ProductStats>)

    /**
     * TAM YENIDEN KURULUM, tek transaction'da. Asla incremental.
     *
     * Turetilmis bir onbellegi artimli guncellemek, kaynak veri her degistiginde
     * (satir silindi, fis yeniden okundu, mutabakat geri alindi) sayaci hangi
     * yonde duzeltecegini bilmek demekti. Bu olcekte (80 urun x 60 gezi) tam
     * kurulum milisaniyeler suruyor ve **her zaman dogru**.
     *
     * `muAdjust` KORUNUYOR: kullanicinin/motorun duzeltmesi turetilmis veri
     * degil, o yuzden yeniden kurulumda sifirlanmamali - cagiran taraf onu
     * mevcut satirdan tasiyor.
     */
    @Transaction
    suspend fun rebuild(householdId: String, rows: List<ProductStats>) {
        clear(householdId)
        insertAll(rows)
    }
}
