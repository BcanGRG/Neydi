package com.neydi.app.data.stats

import com.neydi.app.data.daysBetween
import com.neydi.app.data.db.ProductStats
import com.neydi.app.data.db.ProductStatsDao
import com.neydi.app.data.db.PurchaseEvent

/**
 * Medyanin anlamli olmasi icin gereken en az ALIM sayisi.
 *
 * Uc alim = iki aralik. **Iki alim (tek aralik) yetmez** cunku tek ornegin
 * medyani o ornegin kendisidir: bir kez 40 gun unutmak "normalde 40 gunde bir
 * aliyorsun" olur. Medyanin butun varlik sebebi tam olarak buna direnmek -
 * ortalama yerine medyan secilmesinin gerekcesi de bu (`ProductStats` KDoc).
 *
 * Yetmiyorsa alan **null** kaliyor: *"uydurma yerine bilmiyorum"*.
 */
private const val MIN_PURCHASES_FOR_MEDIAN = 3

/**
 * `product_stats`'i sifirdan kurar (F6.1).
 *
 * ASLA INCREMENTAL: turetilmis bir onbellegi artimli guncellemek, kaynak veri
 * her degistiginde (satir silindi, mutabakat geri alindi) sayaci hangi yonde
 * duzeltecegini bilmek demekti. Bu olcekte (80 urun x 60
 * gezi) tam kurulum milisaniyeler suruyor ve **her zaman dogru**.
 *
 * TEK YERDEN TETIKLENIYOR: gezi kapanisi - iyimser mutabakattan **sonra**,
 * yoksa yeni kapanan gezinin alindi yazilan satirlari kendi tetikledigi
 * yeniden kurulumun disinda kalir.
 *
 * FIS DONEMINDE IKI TETIKLEYICI DAHA VARDI (fis islendikten sonra ve Fis
 * Kontrol'de satir urune baglandiktan sonra) cunku fis gezi kapandiktan SONRA
 * cekiliyordu. Etiket boyle bir gecikme uretmiyor: gozlem bir FIYAT kaydi,
 * satin alma kaniti degil - istatistigin girdisi yalnizca isaretlenmis liste
 * satirlari (bkz. ProductStatsDao.purchaseEvents).
 */
class ProductStatsRebuilder(
    private val statsDao: ProductStatsDao,
    private val clock: () -> Long,
) {

    /** @return yazilan satir sayisi. */
    suspend fun rebuild(householdId: String): Int {
        val events = statsDao.purchaseEvents(householdId)
        // `muAdjust` TURETILMIS VERI DEGIL: kullanicinin/motorun duzeltmesi.
        // Yeniden kurulumda sifirlanmasi, yanlis ogrenmeyi geri almanin tek
        // yolunu butun istatistigi silmek yapardi. O yuzden mevcut satirlardan
        // tasiniyor.
        val existingAdjust = events.map { it.productId }.distinct()
            .associateWith { statsDao.byProduct(it)?.muAdjust ?: 0.0 }

        val rows = events
            .groupBy { it.productId }
            .map { (productId, purchases) -> toStats(householdId, productId, purchases, existingAdjust) }

        statsDao.rebuild(householdId, rows)
        return rows.size
    }

    private fun toStats(
        householdId: String,
        productId: String,
        purchases: List<PurchaseEvent>,
        existingAdjust: Map<String, Double>,
    ): ProductStats {
        val times = purchases.map { it.purchasedAt }.sorted()
        return ProductStats(
            productId = productId,
            householdId = householdId,
            purchaseCount = times.size,
            lastPurchasedAt = times.lastOrNull(),
            medianIntervalDays = medianIntervalDays(times),
            muAdjust = existingAdjust[productId] ?: 0.0,
            updatedAt = clock(),
        )
    }
}

/**
 * Ardisik alimlar arasindaki **takvim gunu** araliklarinin medyani.
 *
 * `daysBetween` kullaniliyor, cikarma degil: 24 saatlik blok saymak ile takvim
 * gunu saymak farkli seyler ve tempo ~10 gun oldugu icin bir gunluk kayma
 * onerinin tetiklenip tetiklenmemesini belirliyor (bkz. `daysBetween` notu).
 *
 * Ayni gun icinde iki alim 0 gunluk aralik uretir; bu **atilmiyor** cunku
 * gerceklesmis bir davranis - iki ayri fisle ayni gun alisveris.
 *
 * @return medyan, ya da [MIN_PURCHASES_FOR_MEDIAN] alimdan az varsa null.
 */
internal fun medianIntervalDays(sortedTimes: List<Long>): Int? {
    if (sortedTimes.size < MIN_PURCHASES_FOR_MEDIAN) return null
    val intervals = sortedTimes.zipWithNext { a, b -> daysBetween(a, b) }.sorted()
    val mid = intervals.size / 2
    return if (intervals.size % 2 == 1) {
        intervals[mid]
    } else {
        // Cift sayida aralik: iki ortanin ortalamasi, yukari yuvarlanmadan.
        // Tam sayiya yuvarlamak zorunlu cunku alan Int - ve "10,5 gunde bir"
        // gibi bir hassasiyet iddiasi bu veriyle desteklenmiyor.
        (intervals[mid - 1] + intervals[mid]) / 2
    }
}
