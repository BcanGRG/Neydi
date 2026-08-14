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
    val satirId: String,
    val urunId: String,
    val ad: String,
    val adet: Double,
    val birim: String,
    val isaretli: Boolean,
    val sabitMi: Boolean,
    val kategoriId: String,
    val kategoriAdi: String,
    val kategoriSirasi: Int,
    val ekleyenUyeId: String,
    /** `not` SQL'de ayrilmis kelime - alan adi bilerek `notu`. */
    val notu: String?,
)
