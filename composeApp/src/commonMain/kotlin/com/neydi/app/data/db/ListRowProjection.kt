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
    /** GECERLI birim: `COALESCE(unitOverride, unit)` - sorguda cozuluyor. */
    val unit: String,
    /** Kullanici bu satir icin birim SECTI mi (karar 108). `null` = katalogu izliyor. */
    val unitOverride: String? = null,
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

    /**
     * Bu satir icin beyan edilen market (karar 117). `null` = gezinin hedefini
     * izliyor.
     *
     * ⚠ SAPMA OLUP OLMADIGI BURADAN OKUNMAZ: `storeId == trip.storeId` olan bir
     * satir sapma DEGIL, yalnizca hedefi tekrar ediyor. Karsilastirmayi
     * `ListState` yapiyor cunku hedef bu sorguya girmiyor.
     */
    val storeId: String? = null,
    /** [storeId]'nin gorunen adi - yalnizca CIZIM icin. */
    val storeName: String? = null,

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

    // --- Baska markette ucuz (F5.5, karar 41) -------------------------------
    //
    // "Rakip" = SON gozlemin marketinden BASKA bir markette gorulmus, tazelik
    // penceresine giren EN UCUZ gozlem. Esikler burada degil `cheaperCandidate`
    // icinde: SQL veriyi getirir, iddiayi Kotlin kurar.
    //
    // Son gozlem MARKETSIZ kaydedilmisse bu alanlarin hepsi null geliyor -
    // `storeId <> NULL` hicbir satir dondurmuyor. Bu bir kaza degil, dogru
    // cevap: nereden aldigini bilmiyorsak "baska markette ucuz" diyemeyiz.

    /** Rakip marketteki en ucuz fiyat; null = karsilastirilacak gozlem yok. */
    val rivalPriceMinor: Long? = null,
    val rivalStoreName: String? = null,
    val rivalPackSize: Double? = null,
    val rivalPackUnit: String? = null,

)

/**
 * Tahmin hesabinin tek satirlik girdisi (kararlar 95-96, 111-114).
 *
 * Sorgu bunlari GETIRIYOR, yorumu [com.neydi.app.data.foldBasket] yapiyor.
 * [unitPriceMinor] `null` ise satirin gozlemi yok - satir yine de doner ve
 * paydada kalir.
 */
data class EstimateRow(
    val quantity: Double,
    /** GECERLI birim: `COALESCE(unitOverride, unit)` (karar 108). */
    val unit: String,
    val unitPriceMinor: Long? = null,
    val packSize: Double? = null,
    val packUnit: String? = null,
    /** Gozlemin zinciri; marketsiz cekimde `null` ve `null` bir anlam tasiyor (karar 113). */
    val chain: String? = null,
    /** Marketin ekranda yazilan adi - kimlik [chain], gorunen bu. */
    val storeName: String? = null,
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
