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

// `formatDayMonthTime` ("13 Ağustos 18:49") SILINDI: cagirani kalmamisti.
// Gecmis satiri [formatDayMonthYear], liste basligi [formatRelativeDay]
// kullaniyor. Gitmesiyle `hh:mm` bicimlemesi de tek yerde kaldi - iki
// kopyasi vardi ve biri sessizce oteki gibi davranmayabilirdi.
//
// Bicimlemenin ELLE olmasinin sebebi duruyor: commonMain'de locale'e duyarli
// tarih bicimleyici yok ve Ingilizce ay adi uygulama dilini bozardi.
// Ay dizini icin `Month.ordinal` (0 tabanli): `monthNumber` 0.8.0'da
// kullanimdan kaldirildi, `Month.number` commonMain'den erisilmiyor -
// ikisini de derleyici soyledi, tahmin degil.

/**
 * Bir gozlemin ya da alimin YASI: "11 gun once", "2 hafta once".
 *
 * MERDIVENDEN AYRI VE BILEREK (gezinme sozlesmesi, "istisna" satiri).
 * [formatRelativeDay] 7-13 gunu **"gecen hafta"**ya topluyor; bir yasi oyle
 * yazmak kiyaslanabilirligi siler - *"son alim 8 gun once"* ile *"12 gun once"*
 * ayni cumleye donusur, oysa okunan sey tam olarak o farktir. Merdiven TEK
 * BASINA DURAN tarihler icin: gezi tarihi, kayit saati.
 *
 * ON DORT GUNDEN SONRA HAFTA: gun sayisi orada anlamini yitiriyor - "37 gun
 * once" okunabilir bir sey soylemiyor, "5 hafta once" soyluyor.
 *
 * SIFIR VE BIR GUN: "0 gun once" Turkce'de sacma, "1 gun once" de kotu. Bu iki
 * basamak merdivenle ayni sozcukleri kullaniyor ("bugun", "dun") - sozlesme
 * burayi yazmiyor, ama gun HASSASIYETI 7-13 araliginda gerekiyordu ve orada
 * korunuyor; alttaki iki basamakta gun sayisi zaten bilgi tasimiyor.
 */
fun formatAge(days: Int): String = when {
    days <= 0 -> "bugün"
    days == 1 -> "dün"
    days < AGE_WEEK_DAYS -> "$days gün önce"
    else -> "${days / 7} hafta önce"
}

/** Yasin haftaya dondugu esik (gezinme sozlesmesi: "14 gunu gecince"). */
private const val AGE_WEEK_DAYS = 14

/** Epoch millis -> "13 Ağustos 2026". */
@OptIn(ExperimentalTime::class)
fun formatDayMonthYear(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
    return "${t.day} ${MONTH_NAMES[t.month.ordinal]} ${t.year}"
}

/**
 * Gecmis bir ani kullanicinin konustugu gibi yazar - TARIH MERDIVENI.
 *
 * Tasarimin verdigi alti basamak, aynen:
 *
 * | aralik | metin |
 * |---|---|
 * | 0-6 saat | "az önce" |
 * | ayni takvim gunu | "bugün 15:38" |
 * | 1 gun | "dün" |
 * | 2-6 gun | "3 gün önce" |
 * | 7-13 gun | "geçen hafta" |
 * | 14+ gun | "12 Ağustos" (yil yalnizca farkli yilsa) |
 *
 * SAAT YALNIZCA BUGUN YAZILIYOR ve bu merdivenin en ince karari: "3 gün önce
 * 15:38" bir kesinlik iddiasi tasiyor ama kimse uc gun oncesinin saatini
 * umursamiyor. Bugun ise saat ayirt edici - ayni gun iki kez alisverise
 * cikilabilir.
 *
 * SAAT BASAMAGI TAKVIM GUNUNU EZIYOR: gece 01:00'de, dun 22:00'deki bir an
 * "dün" degil "az önce" okunur. Uc saat oncesine "dün" demek teknik olarak
 * dogru ama kullanicinin yasadigi sey degil - merdivenin ilk basamagi bu
 * yuzden saatle olculuyor, gunle degil.
 *
 * GELECEK TARIH "az önce" oluyor. Yazilmamasi gerekiyor (gezinme sozlesmesi:
 * *"gelecek tarihli gozlem yazilmaz"*) ama cihaz saati geriye alinmis bir
 * kullanicida negatif fark cikabilir; "-3 gün önce" yazmaktansa en yakin
 * dogru cumleye dusuyoruz.
 */
@OptIn(ExperimentalTime::class)
fun formatRelativeDay(
    epochMillis: Long,
    nowMillis: Long,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val saatFarki = (nowMillis - epochMillis) / 3_600_000
    if (saatFarki < RECENT_HOURS) return "az önce"

    val gun = daysBetween(epochMillis, nowMillis, zone)
    return when {
        gun <= 0 -> {
            val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
            "bugün ${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
        }
        gun == 1 -> "dün"
        gun < WEEK_DAYS -> "$gun gün önce"
        gun < TWO_WEEKS_DAYS -> "geçen hafta"
        else -> {
            val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
            val simdi = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(zone)
            // YIL YALNIZCA FARKLI YILSA: ayni yil icinde her satirda yili
            // tekrarlamak gurultu, farkli yilda ise atlamak yaniltici.
            if (t.year == simdi.year) formatDayMonth(epochMillis, zone)
            else formatDayMonthYear(epochMillis, zone)
        }
    }
}

/** Epoch millis -> "13 Ağustos". Yilsiz; [formatRelativeDay]'in son basamagi. */
@OptIn(ExperimentalTime::class)
fun formatDayMonth(epochMillis: Long, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
    return "${t.day} ${MONTH_NAMES[t.month.ordinal]}"
}

/** "az önce" esigi, saat. */
private const val RECENT_HOURS = 6

/** Bu gunden itibaren "geçen hafta". */
private const val WEEK_DAYS = 7

/** Bu gunden itibaren tam tarih yaziliyor. */
private const val TWO_WEEKS_DAYS = 14

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
