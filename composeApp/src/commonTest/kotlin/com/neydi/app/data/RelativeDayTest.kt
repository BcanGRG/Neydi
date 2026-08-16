package com.neydi.app.data

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

/**
 * Tarih merdiveni (tasarimin gezinme sozlesmesi · dateLadder).
 *
 * SABIT SAAT DILIMI: testler `Europe/Istanbul` ile kosuyor. Sistem dilimine
 * birakmak, testi kosan makineye gore kirilan bir test uretirdi - ve merdiven
 * TAKVIM gunune baglı oldugu icin dilim gercekten sonucu degistiriyor.
 */
@OptIn(ExperimentalTime::class)
class RelativeDayTest {

    private val zone = TimeZone.of("Europe/Istanbul")

    private fun an(
        yil: Int, ay: Int, gun: Int, saat: Int = 12, dakika: Int = 0,
    ): Long = LocalDateTime(yil, ay, gun, saat, dakika).toInstant(zone).toEpochMilliseconds()

    private fun metin(gecmis: Long, simdi: Long) = formatRelativeDay(gecmis, simdi, zone)

    @Test
    fun withinSixHoursIsJustNow() {
        val simdi = an(2026, 8, 16, saat = 15)
        assertEquals("az önce", metin(an(2026, 8, 16, saat = 15), simdi))
        assertEquals("az önce", metin(an(2026, 8, 16, saat = 12), simdi))
        assertEquals("az önce", metin(an(2026, 8, 16, saat = 9, dakika = 30), simdi))
    }

    /** Alti saati gecince ayni gun icin saat yaziliyor. */
    @Test
    fun sameDayShowsTheClock() {
        val simdi = an(2026, 8, 16, saat = 18)
        assertEquals("bugün 08:05", metin(an(2026, 8, 16, saat = 8, dakika = 5), simdi))
    }

    /**
     * SAAT BASAMAGI TAKVIM GUNUNU EZIYOR.
     *
     * Gece 01:00'de, dun 22:00'deki bir an teknik olarak "dün" ama kullanici
     * icin uc saat once. Merdivenin ilk basamagi saatle olculdugu icin
     * "az önce" kazaniyor.
     */
    @Test
    fun threeHoursAgoAcrossMidnightIsStillJustNow() {
        val simdi = an(2026, 8, 17, saat = 1)
        assertEquals("az önce", metin(an(2026, 8, 16, saat = 22), simdi))
    }

    /** Alti saati gecen ve gunu degisen an "dün". */
    @Test
    fun yesterday() {
        val simdi = an(2026, 8, 17, saat = 12)
        assertEquals("dün", metin(an(2026, 8, 16, saat = 12), simdi))
    }

    @Test
    fun twoToSixDaysCountUp() {
        val simdi = an(2026, 8, 17, saat = 12)
        assertEquals("2 gün önce", metin(an(2026, 8, 15), simdi))
        assertEquals("3 gün önce", metin(an(2026, 8, 14), simdi))
        assertEquals("6 gün önce", metin(an(2026, 8, 11), simdi))
    }

    @Test
    fun sevenToThirteenDaysIsLastWeek() {
        val simdi = an(2026, 8, 17, saat = 12)
        assertEquals("geçen hafta", metin(an(2026, 8, 10), simdi))
        assertEquals("geçen hafta", metin(an(2026, 8, 4), simdi))
    }

    /** On dorduncu gunden itibaren tam tarih; ayni yilda yil yazilmiyor. */
    @Test
    fun fourteenDaysOrMoreShowsTheDate() {
        val simdi = an(2026, 8, 17, saat = 12)
        assertEquals("3 Ağustos", metin(an(2026, 8, 3), simdi))
        assertEquals("12 Mayıs", metin(an(2026, 5, 12), simdi))
    }

    @Test
    fun differentYearCarriesTheYear() {
        val simdi = an(2026, 1, 10, saat = 12)
        assertEquals("20 Aralık 2025", metin(an(2025, 12, 20), simdi))
    }

    /**
     * SINIR: alti saat tam olarak "az önce" DEGIL.
     * Merdivende aralik `0-6`, yani altinci saat bir sonraki basamak.
     */
    @Test
    fun sixHoursExactlyLeavesTheJustNowRung() {
        val simdi = an(2026, 8, 16, saat = 18)
        assertEquals("bugün 12:00", metin(an(2026, 8, 16, saat = 12), simdi))
    }

    /**
     * Cihaz saati geriye alinmis olabilir. "-3 gün önce" yazmaktansa en yakin
     * dogru cumleye dusuyoruz.
     */
    @Test
    fun futureTimestampFallsBackToJustNow() {
        val simdi = an(2026, 8, 16, saat = 12)
        assertEquals("az önce", metin(an(2026, 8, 20), simdi))
    }
}
