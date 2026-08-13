package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Hanedeki kisi. Bu uygulamada iki tane: kullanici ve esi.
 *
 * Avatar harfi SAKLANMIYOR, displayName'den turetiliyor - iki yerde tutulursa
 * biri guncellenmeyi unutur. Turetme turkishInitials() ile yapiliyor cunku
 * "Ilknur" -> locale'siz uppercase() "ILKNUR" verir, dogrusu "İLKNUR".
 */
@Entity(tableName = "member")
data class Member(
    @PrimaryKey val id: String,
    val householdId: String,
    val displayName: String,
    /** Bu cihazin sahibi mi. Satir "es ekledi" avatarini yalnizca digerine cizer. */
    val isSelf: Boolean,
    val createdAt: Long,
    val deletedAt: Long? = null,
)
