package com.neydi.app.data.suggest

import com.neydi.app.data.daysBetween
import com.neydi.app.data.db.ProductDao
import com.neydi.app.data.db.ProductStats
import com.neydi.app.data.db.ProductStatsDao
import com.neydi.app.data.db.TakeOutcome
import com.neydi.app.data.db.TripDao
import com.neydi.app.data.db.TripLineDao
import kotlinx.coroutines.flow.first
import kotlin.math.ln

/**
 * Onerilebilirlik esigi: gecikme orani 1,0 = "tam vakti geldi".
 *
 * Esik tam 1,0 DEGIL, biraz alti: medyani 10 gun olan bir urunu 9. gunde
 * onermek erken degil hatirlatma - kullanici zaten yarin alacakti, bugun
 * listeye eklemesi tam olarak istedigimiz davranis. 1,0'i beklemek onerinin
 * hep "gec kalmis" gelmesi demekti.
 */
private const val SCORE_THRESHOLD = 0.85

/** Tasarimin siniri: serit en fazla 5 cip. */
private const val MAX_SUGGESTIONS = 5

/**
 * Tek bir oneri - skor VE gerekce verisi birlikte.
 *
 * Gerekce verisi ayri tasiniyor cunku F6.3'un kurali *"gerekcesiz cip reklam
 * gibi okunur"*: cip "Yumurta · 14 gun oldu" yazacak ve o metni uretmek icin
 * skora degil [daysSince] ve [intervalDays]'e ihtiyaci var. Skoru gosterip
 * gerekceyi atmak, kullaniciya denetleyemedigi bir sayi sunmak olurdu.
 */
data class Suggestion(
    val productId: String,
    val name: String,
    val score: Double,
    /** Son alistan bu yana gecen TAKVIM gunu. */
    val daysSince: Int,
    /** Urunun olculmus temposu (medyan). */
    val intervalDays: Int,
    /** Gecen gezide unutuldu mu - gerekce metnini degistiriyor. */
    val forgottenLastTrip: Boolean,
    /**
     * Kullanicinin SABIT ilan ettigi bir urun mu (F6.8).
     *
     * Ekran 3 onerileri uc bolume ayiriyor ve bu alan "Her zamankiler"
     * bolumunun uyeligini belirliyor. Skor degil BEYAN: kullanici isaretledi.
     */
    val isStaple: Boolean = false,
    /** Olculmus alim sayisi - gerekce cumlesinde kullaniliyor. */
    val purchaseCount: Int = 0,
)

/**
 * Skor formulu (F6.2): siklik + gecikmislik + gecen sefer unutuldu mu.
 *
 * SAF FONKSIYON ve butun agirliklar burada, tek yerde: formul denetlenebilir
 * olmali - "uygulama bunu neden onerdi" sorusunun cevabi bu dosyada okunarak
 * bulunabilmeli. Agirliklari veriye gommek (ya da skoru parcalara dagitmak)
 * o soruyu cevapsiz birakirdi.
 *
 * Bilesenler:
 * - **Gecikmislik** skorun govdesi: `daysSince / interval`. 1,0 = tam vakti.
 *   Orana bolmek mutlak gune bakmaktan onemli - 3 gunde bir alinan ekmegin
 *   4. gunu, 30 günde bir alinan deterjanin 20. gununden daha acil.
 * - **Siklik** kucuk bir katki: `ln(1 + alim) / 10`. Logaritmik, cunku 30 kez
 *   alinmis urun 3 kez alinmistan on kat "daha sabit" degil. Tavani ~0,35 -
 *   siklik hicbir zaman gecikmisligin onune gecmiyor, sadece esitligi bozuyor.
 * - **Gecen gezinin beyani**: FORGOTTEN **+0,5** (kullanici acikca "almam
 *   gerekiyordu" dedi - bir sonraki listede one cikmali), NOT_NEEDED **-1,0**
 *   (acikca "gerekmedi" dedi - bir tempo boyunca sussun). Ikisinin ayni
 *   olmamasi bu kolonun var olma sebebi (F4.12).
 * - **[ProductStats.muAdjust]** oldugu gibi ekleniyor: kullanicinin/motorun
 *   kalici duzeltmesi, ayri kolonda ki sifirlanabilsin.
 *
 * @return skor, ya da urunun olculmus temposu yoksa **null** - tempo olmadan
 *   gecikmislik tanimsiz ve uydurmak yerine oneri uretilmiyor ("bilmiyorum").
 *   Soguk baslangic (katalog yayginligi, kurulum temposu) F6.3/F6.6'nin isi.
 */
internal fun score(
    stats: ProductStats,
    daysSince: Int,
    lastOutcome: TakeOutcome?,
): Double? {
    val interval = stats.medianIntervalDays?.takeIf { it > 0 } ?: return null
    val overdue = daysSince.toDouble() / interval
    val frequency = ln(1.0 + stats.purchaseCount) / 10.0
    val outcome = when (lastOutcome) {
        TakeOutcome.FORGOTTEN -> 0.5
        TakeOutcome.NOT_NEEDED -> -1.0
        TakeOutcome.TAKEN, null -> 0.0
    }
    return overdue + frequency + outcome + stats.muAdjust
}

/**
 * Oneri ureticisi (F6.2).
 *
 * YALNIZCA VERI URETIYOR - serit, cip, metin F6.3'un isi. Burada uc kural var:
 *
 * 1. **Aktif listede olan urun onerilmez.** Kullanici zaten yazmis; onermek
 *    "uygulama listemi okumuyor" hissi verir.
 * 2. **Esik [SCORE_THRESHOLD]:** vakti gelmemis urun listelenmez, skora gore
 *    "en iyi 5" bile olsa. Bos serit, alakasiz seritten iyidir - tasarim bos
 *    durumu zaten kabul ediyor.
 * 3. **En fazla [MAX_SUGGESTIONS]:** tasarimin siniri. Ustu, seridi reklam
 *    yuzeyine cevirir.
 */
class SuggestionEngine(
    private val statsDao: ProductStatsDao,
    private val productDao: ProductDao,
    private val tripDao: TripDao,
    private val tripLineDao: TripLineDao,
    private val clock: () -> Long,
) {

    suspend fun suggestions(householdId: String): List<Suggestion> {
        val allStats = statsDao.observeAll(householdId).first()
        if (allStats.isEmpty()) return emptyList()

        // Aktif listedekiler haric: zaten yazilmis.
        val activeTripIds = tripDao.activeOrNull(householdId)?.let { trip ->
            tripLineDao.observeLines(trip.id).first().map { it.productId }.toSet()
        }.orEmpty()

        val now = clock()
        // Sequence DEGIL: mapNotNull icinde suspend cagri var ve sequence
        // lambda'lari suspend olamiyor. 80 urunluk olcekte fark da yok.
        return allStats
            .filter { it.productId !in activeTripIds }
            .mapNotNull { stats -> toSuggestion(stats, now) }
            .filter { it.score >= SCORE_THRESHOLD }
            .sortedByDescending { it.score }
            .take(MAX_SUGGESTIONS)
    }

    private suspend fun toSuggestion(stats: ProductStats, now: Long): Suggestion? {
        val last = stats.lastPurchasedAt ?: return null
        val product = productDao.byId(stats.productId) ?: return null
        val lastOutcome = tripLineDao.lastOutcome(stats.productId)
        val daysSince = daysBetween(last, now)
        val value = score(stats, daysSince, lastOutcome) ?: return null
        return Suggestion(
            productId = stats.productId,
            name = product.name,
            score = value,
            daysSince = daysSince,
            intervalDays = stats.medianIntervalDays ?: return null,
            forgottenLastTrip = lastOutcome == TakeOutcome.FORGOTTEN,
            isStaple = product.isStaple,
            purchaseCount = stats.purchaseCount,
        )
    }
}
