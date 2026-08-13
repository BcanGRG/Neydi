package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Ne onerdik ve ne oldu.
 *
 * Bu tablo olmadan oneri motoru KENDI ISABETINI OLCEMEZ. Surekli reddedilen
 * bir oneriyi susturmanin tek yolu reddedildigini kaydetmis olmak; aksi halde
 * uygulama ayni yanlisi her gezide tekrarlar ve kullanici oneri seridini
 * tamamen gormezden gelmeye baslar.
 */
@Entity(tableName = "suggestion_event")
data class SuggestionEvent(
    @PrimaryKey val id: String,
    val householdId: String,
    val productId: String,
    val tripId: String?,
    val suggestedAt: Long,
    /** Kullaniciya GOSTERILEN gerekce - "12 gundur almadin". Gerekcesiz oneri reklam gibi okunur. */
    val reason: String,
    val outcome: SuggestionOutcome = SuggestionOutcome.GOSTERILDI,
    val respondedAt: Long? = null,
    val createdAt: Long,
)

enum class SuggestionOutcome {
    GOSTERILDI,
    EKLENDI,
    /** Kullanici acikca reddetti - bu urun bir sure onerilmemeli. */
    REDDEDILDI,
    /** Gorup dokunmadi. Reddetmekten ZAYIF bir sinyal, ayni sayilmamali. */
    YOKSAYILDI,
}
