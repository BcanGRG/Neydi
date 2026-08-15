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
)
