package com.neydi.app.data

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val MONTH_NAMES = listOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık",
)

/**
 * Epoch millis -> "13 Ağustos 18:49".
 *
 * YIL YOK: gecmis listesinde her satirda yili tekrarlamak gurultu, ve elli
 * gezilik bir listede hepsi ayni yil. Yil gerekliyse (uzun gecmis) cagiran
 * taraf [formatDayMonthYear] kullanir.
 *
 * Bicimleme ELLE: commonMain'de locale'e duyarli tarih bicimleyici yok ve
 * Ingilizce ay adi gostermek uygulama dilini bozardi.
 *
 * Ay dizini icin `Month.ordinal` (0 tabanli): `monthNumber` 0.8.0'da
 * kullanimdan kaldirildi, `Month.number` ise commonMain'den erisilmiyor -
 * ikisini de derleyici soyledi, tahmin degil.
 */
@OptIn(ExperimentalTime::class)
fun formatDayMonthTime(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
    val saat = t.hour.toString().padStart(2, '0')
    val dakika = t.minute.toString().padStart(2, '0')
    return "${t.day} ${MONTH_NAMES[t.month.ordinal]} $saat:$dakika"
}

/** Epoch millis -> "13 Ağustos 2026". */
@OptIn(ExperimentalTime::class)
fun formatDayMonthYear(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
    return "${t.day} ${MONTH_NAMES[t.month.ordinal]} ${t.year}"
}
