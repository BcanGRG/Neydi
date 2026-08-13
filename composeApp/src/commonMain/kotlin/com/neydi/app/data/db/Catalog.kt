package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Reyon taksonomisi - 12 kategori, uygulamayla gelir.
 *
 * householdId YOK: referans verisi, senkron olmuyor, kimseye ait degil.
 * (Sozlesme.kt madde 2'deki bilincli istisna.)
 *
 * sortOrder MARKET GEZME SIRASI, alfabetik degil. Liste reyon sirasina gore
 * bolunecek; alfabetik siralamak insani markette ileri geri yurutur.
 */
@Entity(tableName = "category")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val sortOrder: Int,
    /** Kategori kutucugunun tonu (ARGB). Ikon yoksa iki-harf fallback bunun uzerine biner. */
    val tintArgb: Long,
)

/**
 * Gomulu Turk urun katalogu - ~250 urun.
 *
 * TEK SOGUK-BASLANGIC MEKANIZMASI. Tek hane oldugu icin isbirlikci filtreleme
 * yapacak baska kullanici verisi yok; ilk gezilerde uygulamanin bir sey
 * bilmesinin baska yolu bulunmuyor.
 *
 * commonalityRank = Turkiye'de ne kadar yaygin (1 = en yaygin). Oneri sirasi ve
 * yazarken tamamlama sirasi bundan cikiyor.
 */
@Entity(tableName = "catalog_seed")
data class CatalogSeed(
    @PrimaryKey val id: String,
    val name: String,
    /** Bkz. Sozlesme.kt madde 6. Dolduran normalizasyon F2.4'te. */
    val matchKey: String,
    val categoryId: String,
    val commonalityRank: Int,
    /** "adet", "kg", "L" - varsayilan, kullanici degistirebilir. */
    val defaultUnit: String,
)
