package com.neydi.app.data.db

/**
 * Liste ekraninin ihtiyaci olan TAM veri, tek sorguda.
 *
 * Neden @Embedded ile iki entity degil: TripLine ve Product'in ortak sutun
 * adlari var (id, householdId, createdAt, deletedAt) ve prefix'lemek okunmasi
 * zor bir gurultu uretiyor. Ekranin ihtiyaci zaten bu alanlar; projeksiyon
 * hem acik hem dar.
 *
 * kategoriSirasi MARKET GEZME sirasi - bolumler bununla siralaniyor.
 */
data class ListRowProjection(
    val rowId: String,
    val productId: String,
    val name: String,
    val count: Double,
    val unit: String,
    val checked: Boolean,
    val isStaple: Boolean,
    val categoryId: String,
    val categoryName: String,
    val categoryOrder: Int,
    val addedByMemberId: String,
    /** `not` SQL'de ayrilmis kelime - alan adi bilerek `note`. */
    val note: String?,
    /** Kullanicinin beyan ettigi akibet; null = bir sey soylemedi (F4.12). */
    val takeOutcome: TakeOutcome?,

    // --- Fiyat ipucu (E16) --------------------------------------------------
    //
    // Hepsi AYNI sorgudan geliyor. Satir basina ikinci bir sorgu acmak
    // yasak (tek-SQL kurali): yirmi satirlik bir listede yirmi Flow acmak
    // hem her gozlem yaziminda yirmi yeniden yayin uretir hem de satirlar
    // birbirinden bagimsiz zamanlarda guncellenip liste titrer.

    /** En son gozlemin fiyati; null = bu urunun hic gozlemi yok. */
    val lastPriceMinor: Long? = null,
    /** En son gozlemin ani - "kac gun once" bundan cikiyor. */
    val lastObservedAt: Long? = null,
    /** En son gozlemin marketi. Gozlem marketsiz kaydedilmis olabilir. */
    val lastStoreName: String? = null,
    val lastPackSize: Double? = null,
    val lastPackUnit: String? = null,

    /** BIR ONCEKI gozlemin fiyati; null = tek gozlem var, trend hesaplanamaz. */
    val prevPriceMinor: Long? = null,
    val prevPackSize: Double? = null,
    val prevPackUnit: String? = null,

    /**
     * Son sekiz gozlemin fiyatlari, YENIDEN ESKIYE, virgulle ayrilmis.
     *
     * `group_concat` ile geliyor cunku sparkline satirin icinde ciziliyor ve
     * tek-SQL kurali satir basina ikinci bir sorguyu yasakliyor. Dizi yerine
     * dizgi olmasinin sebebi Room'un skaler alt sorgudan koleksiyon
     * dondurememesi; ayristirmasi tek satir.
     */
    val priceHistory: String? = null,
)

/** Gezi basina satir sayisi (Gecmis ekrani). */
data class TripLineCount(
    val tripId: String,
    val lineCount: Int,
)

/**
 * Bir fiyat gozlemi, Ekran 5'in ihtiyaci kadar (E17).
 *
 * `storeName` ve `brand` AYRI ve ikisi de null olabilir - karar 26 satirin
 * kimligini market+marka cifti yapiyor ama gercekte ikisi de eksik olabiliyor:
 * kullanici acele edip market secmemis, ya da manavda marka yok.
 */
data class ObservationRow(
    /** Gozlemin kimligi - silme bu satiri bulabilmeli (karar 46). */
    val id: String,
    val observedAt: Long,
    val unitPriceMinor: Long,
    val brand: String?,
    val storeName: String?,
    val packSize: Double?,
    val packUnit: String?,
)

/**
 * Bir gezinin tahmini tutari (E18).
 *
 * @property pricedCount tutara GIREN urun sayisi. Tahmini gostermenin sarti
 *   bu - fiyati bilinen urun sayisi esigin altindaysa tutar hic yazilmiyor.
 */
data class TripEstimate(
    val tripId: String,
    val estimateMinor: Long,
    val pricedCount: Int,
)
