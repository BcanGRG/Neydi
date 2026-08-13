package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Cevrimdisi giden kutusu.
 *
 * Yazma once yerel veritabanina gider, buraya bir kayit dusulur, senkron
 * musait oldugunda gonderilir. Kullanici marketin bodrumunda sebeke yokken de
 * isaretleme yapabilmeli ve bunun "kaydedilemedi" diye geri alinmamasi lazim.
 *
 * attempts + lastError: sonsuza kadar deneyip pil yakmasin, ve neden
 * takildigini soyleyebilelim.
 */
@Entity(tableName = "pending_op")
data class PendingOp(
    @PrimaryKey val id: String,
    val householdId: String,
    val entityTable: String,
    val entityId: String,
    val opType: OpType,
    /** Serilestirilmis govde. Sema degistiginde eski kayitlar okunabilir kalsin diye JSON. */
    val payloadJson: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String? = null,
)

enum class OpType { EKLE, GUNCELLE, SIL }

/**
 * Senkron imleci. Hane basina tek satir.
 *
 * lastPulledAt sunucu saatidir, cihaz saati DEGIL: telefon saati yanlissa
 * cihaz saatiyle imlec tutmak degisiklikleri sessizce atlar.
 */
@Entity(tableName = "sync_meta")
data class SyncMeta(
    @PrimaryKey val householdId: String,
    val lastPulledAt: Long? = null,
    val lastPushedAt: Long? = null,
    val cursor: String? = null,
)
