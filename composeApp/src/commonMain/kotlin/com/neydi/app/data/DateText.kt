package com.neydi.app.data

import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
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

/**
 * Iki an arasindaki **TAKVIM GUNU** sayisi.
 *
 * NEDEN `(b - a) / 86_400_000` DEGIL: o 24 saatlik blok sayar, takvim gunu
 * saymaz. Butun zaman damgalari UTC epoch millis (Conventions madde 5) ama
 * kullanicinin "gun" kavrami Europe/Istanbul. Yerel saatle dun 22:00'deki bir
 * alisveris, bugun 22:00'ye kadar "0 gun once" okunur.
 *
 * Temposu ~10 gun olan bir uygulamada bir gunluk kayma, *"12 gundur almadin,
 * normalde 10 gunde bir"* onerisinin **tetiklenmesi ile tetiklenmemesi**
 * arasindaki fark. O yuzden hem gosterim katmani hem `medianIntervalDays`
 * hesabi AYNI aritmetigi kullanmak zorunda; yoksa ekran ile skor birbirinden
 * farkli konusur.
 */
@OptIn(ExperimentalTime::class)
fun daysBetween(
    fromEpochMillis: Long,
    toEpochMillis: Long,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): Int {
    val from = Instant.fromEpochMilliseconds(fromEpochMillis).toLocalDateTime(zone).date
    val to = Instant.fromEpochMilliseconds(toEpochMillis).toLocalDateTime(zone).date
    return from.daysUntil(to)
}
