package com.neydi.app.data.suggest

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Ekran 3'un satir gerekcesi (F6.4).
 *
 * Tasarimin sarti: *"Her öneri düz Türkçe bir gerekçe taşır."* Gerekcesiz bir
 * oneri reklam gibi okunuyor ve kullanici onu gormemeye basliyor - yani
 * gerekce metni ekranin isleyip islememesini belirliyor.
 */
class LongReasonTextTest {

    private fun suggestion(
        daysSince: Int = 5,
        intervalDays: Int = 10,
        forgotten: Boolean = false,
        staple: Boolean = false,
    ) = Suggestion(
        productId = "p1",
        name = "Ekmek",
        score = 1.0,
        daysSince = daysSince,
        intervalDays = intervalDays,
        forgottenLastTrip = forgotten,
        isStaple = staple,
        purchaseCount = 8,
    )

    /**
     * OLAY ORTALAMAYI YENER. "Gecen sefer unuttun" olmus bir sey; tempo bir
     * ortalama. Kullaniciya once olani soylemek gerekiyor.
     */
    @Test
    fun forgottenBeatsEverythingElse() {
        assertEquals(
            "geçen sefer unutmuştun",
            suggestion(forgotten = true, staple = true, daysSince = 40).longReasonText(),
        )
    }

    /** Sabitte tempo anlatmak gereksiz: kullanici zaten "her zaman al" dedi. */
    @Test
    fun stapleSaysItIsAlwaysBought() {
        assertEquals("her seferinde alıyorsun", suggestion(staple = true).longReasonText())
    }

    /**
     * VAKTI GECMISSE IKI SAYI BIRLIKTE.
     *
     * "12 gündür almadın" tek basina cok mu az mi bilinmiyor; normalini
     * yanina koymak cumleyi karar verilebilir yapiyor.
     */
    @Test
    fun overdueGivesBothNumbers() {
        assertEquals(
            "12 gündür almadın, normalde 10 günde bir",
            suggestion(daysSince = 12, intervalDays = 10).longReasonText(),
        )
    }

    /** Vakti gelmemisse yalnizca tempo yaziliyor. */
    @Test
    fun onScheduleSaysThePace() {
        assertEquals(
            "genelde 10 günde bir alıyorsun",
            suggestion(daysSince = 8, intervalDays = 10).longReasonText(),
        )
    }

    /** Tam vaktinde: "gecmis" degil, tempo cumlesi. Sinir dahil degil. */
    @Test
    fun exactlyOnIntervalIsNotOverdue() {
        assertEquals(
            "genelde 10 günde bir alıyorsun",
            suggestion(daysSince = 10, intervalDays = 10).longReasonText(),
        )
    }
}
