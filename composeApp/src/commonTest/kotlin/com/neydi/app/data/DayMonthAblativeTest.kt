package com.neydi.app.data

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

/**
 * Aralik baslangicinin ayrilma ekli hali (karar 67 · 1): "6 Haziran'dan".
 *
 * ELLE YAZILAN HER SEY GIBI TEST EDILIYOR - ek bir tablodan geliyor ve tablo
 * gozle dogrulanabilir olsa da bir gun MONTH_NAMES ile sirasi kayabilir; on iki
 * ayin da burada olmasi o kaymayi derlenmeyen degil KIRILAN bir teste cevirir.
 *
 * SABIT SAAT DILIMI (`Europe/Istanbul`): sistem dilimine birakmak testi kosan
 * makineye gore kirardi ve yil karsilastirmasi gercekten dilime bagli.
 */
@OptIn(ExperimentalTime::class)
class DayMonthAblativeTest {

    private val zone = TimeZone.of("Europe/Istanbul")

    private fun an(yil: Int, ay: Int, gun: Int): Long =
        LocalDateTime(yil, ay, gun, 12, 0).toInstant(zone).toEpochMilliseconds()

    private fun metin(gecmis: Long, simdi: Long) = formatDayMonthAblative(gecmis, simdi, zone)

    /**
     * ON IKI AYIN EKI, sirasiyla.
     *
     * Ek unlu uyumu ISLETILEREK degil tablodan geliyor; "Ağustos'tan" ile
     * "Eylül'den" arasindaki d/t degisimi tam da bir kural motorunun sessizce
     * yanlis kuracagi yer.
     */
    @Test
    fun everyMonthCarriesItsOwnSuffix() {
        val simdi = an(2026, 12, 31)
        assertEquals("6 Ocak'tan", metin(an(2026, 1, 6), simdi))
        assertEquals("6 Şubat'tan", metin(an(2026, 2, 6), simdi))
        assertEquals("6 Mart'tan", metin(an(2026, 3, 6), simdi))
        assertEquals("6 Nisan'dan", metin(an(2026, 4, 6), simdi))
        assertEquals("6 Mayıs'tan", metin(an(2026, 5, 6), simdi))
        assertEquals("6 Haziran'dan", metin(an(2026, 6, 6), simdi))
        assertEquals("6 Temmuz'dan", metin(an(2026, 7, 6), simdi))
        assertEquals("6 Ağustos'tan", metin(an(2026, 8, 6), simdi))
        assertEquals("6 Eylül'den", metin(an(2026, 9, 6), simdi))
        assertEquals("6 Ekim'den", metin(an(2026, 10, 6), simdi))
        assertEquals("6 Kasım'dan", metin(an(2026, 11, 6), simdi))
        assertEquals("6 Aralık'tan", metin(an(2026, 12, 6), simdi))
    }

    /** Gun sayisi oldugu gibi - basina sifir konmuyor. */
    @Test
    fun theDayIsNotPadded() {
        val simdi = an(2026, 8, 19)
        assertEquals("1 Ağustos'tan", metin(an(2026, 8, 1), simdi))
        assertEquals("14 Ağustos'tan", metin(an(2026, 8, 14), simdi))
    }

    /**
     * YIL YALNIZCA FARKLI YILSA, ve eki YILIN OKUNUSU belirliyor.
     *
     * "6 Haziran'dan beri" on dort ay oncesi icin iki ay gibi okunurdu -
     * mansetin tek isi gozlenmis araligi dogru soylemek.
     */
    @Test
    fun aDifferentYearIsWrittenWithItsOwnSuffix() {
        assertEquals("6 Haziran 2025'ten", metin(an(2025, 6, 6), an(2026, 1, 10)))
        assertEquals("6 Haziran 2026'dan", metin(an(2026, 6, 6), an(2027, 1, 10)))
        assertEquals("6 Haziran 2020'den", metin(an(2020, 6, 6), an(2021, 1, 10)))
        assertEquals("6 Haziran 2023'ten", metin(an(2023, 6, 6), an(2024, 1, 10)))
    }

    /** Ayni yil icinde yil YAZILMIYOR: her cumlede tekrarlamak gurultu. */
    @Test
    fun theSameYearIsNotRepeated() {
        assertEquals("6 Haziran'dan", metin(an(2026, 6, 6), an(2026, 8, 19)))
    }
}
