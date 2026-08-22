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
 * Ay adlarinin kisaltmasi, [MONTH_NAMES] ile AYNI SIRADA.
 *
 * TABLODAN, `take(3)` ile kesip atarak DEGIL. Kesme Turkce'de tesadufen dogru
 * sonuc verirdi ama kural olarak yanlis: kisaltma bir yazim gelenegi, karakter
 * sayisi degil. Tablo ayrica "Ağu" ile "Ağustos"un yan yana durmasini ve
 * ikisinin ayni indeksten okundugunu gorunur kiliyor.
 */
private val MONTH_SHORT = listOf(
    "Oca", "Şub", "Mar", "Nis", "May", "Haz",
    "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara",
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

/**
 * Epoch millis -> **"22 Ağu"**: DAR VE SABIT bir sutundaki tarih.
 *
 * ## Neden [formatDayMonth]'tan ayri bir bicimleyici
 *
 * Ayirim metnin degil, DURDUGU YERIN ozelligi. Urun Detayi'nin alim gecmisi
 * satirinda tarih sabit genislikli bir sutunda; Gecmis ekraninin gezi
 * satirinda ise esnek bir sutunda. Tasarim ikisini bilerek ayirmis: dar sabit
 * sutunda `6 Ağu`, esnek sutunda `12 Ağustos`.
 *
 * Bunun bedeli cihazda goruldu: tam ay adi 56dp'lik kutuya sigmayinca metin
 * UC SATIRA boluniyor ve satirin ikinci satiri yanindaki market adiyla
 * bitisik okunuyordu - ekranda `22 / AğustoBİM / s` yaziyordu. Kirpilmiyordu
 * bile, cunku sabit genislikli metnin sarmaya karsi kilidi yoktu.
 *
 * Kisaltmak tek basina yetmiyor (%130 yazi olceginde `22 Ağu` da zorlanir);
 * cagiran taraf ayrica `maxLines = 1` koyuyor. Ikisi birlikte kural: **genislik
 * veren her metnin sarmaya karsi kilidi olmali.**
 */
@OptIn(ExperimentalTime::class)
fun formatDayMonthShort(
    epochMillis: Long,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
    return "${t.day} ${MONTH_SHORT[t.month.ordinal]}"
}

/**
 * Epoch millis -> **"6 Haziran'dan"**: bir ARALIGIN BASLANGICI (karar 67).
 *
 * Trend manseti araligini VERIDEN aliyor - *"6 Haziran'dan beri %28 arttı"*,
 * yani kullanilan ilk gozlemin tarihi. Sabit bir *"son 3 ayda"* metni, gozlem
 * penceresi ne olursa olsun ayni cumleyi yazardi ve manset gozlenmemis bir
 * aralik iddia ederdi; DateText haftada bittigi icin o metnin dayanagi da yoktu.
 *
 * AYRILMA EKI ON IKI ADLIK TABLODAN, unlu uyumu ISLETILEREK degil: ay adlari
 * kapali bir kume, tablo bakarak dogrulanabilir. Kural motoru ise "Ağustos'tan"
 * ile "Eylül'den" arasindaki sessiz benzesmesini (d/t) bir gun yanlis kurar ve
 * kimse fark etmez. Ek KESME ISARETIYLE yaziliyor cunku ay adi ozel ad.
 *
 * YIL YALNIZCA FARKLI YILSA - [formatRelativeDay]'in kurali, ayni gerekceyle:
 * ayni yil icinde yili tekrarlamak gurultu, farkli yilda atlamak yaniltici.
 * Gecmis dokuz gozlemle sinirli ama dokuz gozlem rahatca yil sinirini asar ve
 * "6 Haziran'dan beri" on dort aylik bir araligi iki ay gibi okuturdu.
 */
@OptIn(ExperimentalTime::class)
fun formatDayMonthAblative(
    epochMillis: Long,
    nowMillis: Long,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(zone)
    val simdi = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(zone)
    val month = MONTH_NAMES[t.month.ordinal]
    return if (t.year == simdi.year) "${t.day} $month'${MONTH_ABLATIVE[t.month.ordinal]}"
    else "${t.day} $month ${t.year}'${yearAblative(t.year)}"
}

/**
 * Ay adlarinin ayrilma eki, [MONTH_NAMES] ile AYNI SIRADA.
 *
 * Ocak'tan, Subat'tan, Mart'tan, Nisan'dan, Mayis'tan, Haziran'dan,
 * Temmuz'dan, Agustos'tan, Eylul'den, Ekim'den, Kasim'dan, Aralik'tan.
 */
private val MONTH_ABLATIVE = listOf(
    "tan", "tan", "tan", "dan", "tan", "dan",
    "dan", "tan", "den", "den", "dan", "tan",
)

/**
 * Yilin ayrilma eki: 2025 -> `2025'ten`, 2026 -> `2026'dan`.
 *
 * Ek yilin OKUNUSUNA bakiyor, rakamina degil: "iki bin yirmi bes" sonunda
 * *bes* var, o yuzden 'ten. Son hane sifir degilse eki o hane belirliyor;
 * sifirsa onlar basamaginin adi ("yirmi" -> 'den, "otuz" -> 'dan). Onlar da
 * sifirsa okunus "yuz"/"bin" ile bitiyor - [TENS_ABLATIVE]'in sifirinci girdisi.
 */
private fun yearAblative(year: Int): String {
    val ones = year % 10
    return if (ones != 0) ONES_ABLATIVE[ones] else TENS_ABLATIVE[(year / 10) % 10]
}

/** bir, iki, uc, dort, bes, alti, yedi, sekiz, dokuz - sifirinci girdi kullanilmiyor. */
private val ONES_ABLATIVE =
    listOf("", "den", "den", "ten", "ten", "ten", "dan", "den", "den", "dan")

/** yuz/bin, on, yirmi, otuz, kirk, elli, altmis, yetmis, seksen, doksan. */
private val TENS_ABLATIVE =
    listOf("den", "dan", "den", "dan", "tan", "den", "tan", "ten", "den", "dan")

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
