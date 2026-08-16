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
 * `tripId` TASINIYOR cunku tekilligin anahtari o: ayni urun bir geziye iki
 * satir olarak girebilir ve bu **bir** alistir, iki degil.
 */
data class PurchaseEvent(
    val productId: String,
    val tripId: String,
    val purchasedAt: Long,
)

/**
 * Turetilmis istatistik tablosunun DAO'su (F6.1).
 *
 * TABLO BIR ONBELLEK, kaynak degil: `trip_line` satirlarindan yeniden
 * kurulabiliyor, o yuzden senkron edilmiyor ve tombstone tasimiyor. Bozulursa
 * silinip yeniden uretiliyor - `ProductStats` KDoc'u bunu yaziyor.
 */
@Dao
interface ProductStatsDao {

    /**
     * SATIN ALMA OLAYLARI - istatistigin tek girdisi.
     *
     * **TEK KAYNAK: LISTEDE ISARETLENMIS SATIRLAR.** Fis donemi buraya ikinci
     * bir kol eklemisti (`receipt_line`), gerekcesi de yaziliydi: "fis, listeye
     * yazilmasa bile ekmegi iceriyor" - yani kullanicinin listeye yazmayi
     * UNUTTUGU urunleri de saymak. O kol E10'da kaynagiyla birlikte silindi.
     *
     * KAYBEDILEN SEY GERCEK ve kayda geciyor: listeye hic yazilmadan alinan
     * urun artik satin alma sayilmiyor, yani oneri motoru onu ogrenemiyor.
     * Etiket cekimi bunun yerine GECMIYOR - etiket bir FIYAT gozlemi, satin
     * alma kaniti degil (kullanici fiyata bakip almayabilir de). Iyimser
     * mutabakat (F4.8) bu bosluğun asil karsiligi: kapanista isaretlenmemis
     * her satir alindi sayiliyor, yani listeye yazilan hicbir sey kaybolmuyor.
     *
     * **TEKILLESTIRME (productId, tripId) UZERINDE ve tek kolda bile sart:**
     * ayni urun bir geziye iki satir olarak girebilir. Tekillestirilmezse
     * `purchaseCount` sisiyor ve `medianIntervalDays` **duşuyor** - yani
     * uygulama her seyi daha sik onermeye basliyor. Sessiz, yavas ve geri
     * alinmasi zor bir bozulma.
     *
     * **ZAMAN DAMGASI `trip.completedAt`, `trip_line.checkedAt` DEGIL.**
     * Iyimser mutabakat kapanista isaretlenmemis **her** satira kapanis
     * damgasini yaziyor, yani tembel kullanimda - beklenen yaygin durum -
     * butun gezinin `checkedAt`'i ayni. O alan satin alma ani olarak
     * kullanilamaz.
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
     * (satir silindi, mutabakat geri alindi) sayaci hangi
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
