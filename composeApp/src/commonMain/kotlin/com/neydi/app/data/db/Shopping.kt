package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Magaza. chain ve name AYRI: "Migros" zincir, "Migros Bahcelievler" sube.
 * Fiyat karsilastirmasi zincir bazinda anlamli, alias eslemesi de oyle.
 */
@Entity(tableName = "store")
data class Store(
    @PrimaryKey val id: String,
    val householdId: String,
    val name: String,
    /** Normalize zincir adi. ProductAlias.storeChain ile AYNI sozlugu kullanir. */
    val chain: String,
    val createdAt: Long,
    val deletedAt: Long? = null,
)

/**
 * Bir alisveris. completedAt null ise AKTIF.
 *
 * Ayni anda birden fazla aktif trip olmamali ama bunu SEMA zorlamıyor -
 * kismi unique index (WHERE completedAt IS NULL) gerekir ve o F2.3'un isi.
 *
 * storeId null olabilir: alisverise cikarken nereye gidilecegi her zaman
 * belli degil, fis okununca doluyor.
 */
@Entity(
    tableName = "trip",
    indices = [
        // "Ayni anda tek aktif trip" KISMI bir unique index gerektirir
        // (WHERE completedAt IS NULL) ve Room'un @Index'i kismi index ifade
        // EDEMIYOR. Sema bunu zorlamiyor; kural repository katmaninda (F2.6).
        // Burada yalnizca sorgu indexi var.
        Index(value = ["householdId", "completedAt"]),
    ],
)
data class Trip(
    @PrimaryKey val id: String,
    val householdId: String,
    val storeId: String? = null,
    val startedAt: Long,
    val completedAt: Long? = null,
    /** Kurus. Fis okunmadiysa null - 0 DEGIL, "bilmiyorum" ile "bedava" ayri seyler. */
    val totalMinor: Long? = null,
    val createdAt: Long,
    val deletedAt: Long? = null,
)

/**
 * Listedeki bir satir.
 *
 * F2.3: UNIQUE(tripId, productId). Kritik: iki es de ekmek eklerse istatistik
 * katmani TEK satin alma gormeli. Iki satir olursa medianIntervalDays yariya
 * duser ve uygulama ekmegi iki kat sik onermeye baslar.
 *
 * checked ve checkedAt birlikte: yalnizca bayrak tutulursa "reyonda mi
 * isaretlendi yoksa evde mi" ayrimi kaybolur, o da alisveris suresi
 * istatistigini imkansiz kilar.
 */
@Entity(
    tableName = "trip_line",
    indices = [
        // ISTATISTIK KATMANININ DAYANDIGI KISIT. Iki es de ekmek eklerse
        // veritabani TEK satir tutmali: iki satir olursa ProductStats iki ayri
        // satin alma sayar, medianIntervalDays yariya duser ve uygulama ekmegi
        // iki kat sik onermeye baslar. Yani bu kisit olmadan oneri motoru
        // sessizce yanlis calisir - hata vermez, sadece rahatsiz eder.
        Index(value = ["tripId", "productId"], unique = true),
        // Aktif alisverisin satirlarini cekmek en sik sorgu.
        Index(value = ["tripId"]),
        Index(value = ["productId"]),
    ],
)
data class TripLine(
    @PrimaryKey val id: String,
    val householdId: String,
    val tripId: String,
    val productId: String,
    /** Adet ya da miktar. 1.5 kg mumkun oldugu icin Double; para DEGIL, kural gecerli degil. */
    val quantity: Double = 1.0,
    val unit: String,
    val checked: Boolean = false,
    val checkedAt: Long? = null,
    /** Kim ekledi - satirdaki es avatari bundan cikiyor. */
    val addedByMemberId: String,
    /** Oneriden mi geldi. SuggestionEvent ile birlikte oneri isabetini olcer. */
    val fromSuggestion: Boolean = false,
    val note: String? = null,
    val createdAt: Long,
    val deletedAt: Long? = null,
)
