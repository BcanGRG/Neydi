package com.neydi.app.ui.list

import com.neydi.app.data.db.ListRowProjection
import com.neydi.app.data.db.TripEstimate
import com.neydi.app.data.formatChipMinor
import com.neydi.app.ui.components.PriceHint
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Gozlemlerden satirin fiyat ipucunu kurar (E16).
 *
 * ## Dort dal, ve hangisinin secildigi VERININ kendisiyle belli
 *
 * - Gozlem yok -> [PriceHint.None]; ikinci satir hic cizilmiyor.
 * - Tek gozlem -> [PriceHint.Single]; yuzde YOK, cunku neyle
 *   karsilastirilacagi yok.
 * - Iki gozlem, **ambalaj degismis** -> [PriceHint.PackChanged]; trend
 *   BASTIRILIYOR.
 * - Iki gozlem, ambalaj ayni -> [PriceHint.Trend].
 *
 * ## Ambalaj kontrolu neden trendin ONUNDE
 *
 * 900 g -> 800 g ayni fiyata satiliyorsa bu bir fiyat dususu DEGIL, gizli bir
 * zam. Trend dali once secilseydi yesil bir asagi ok cizerdi ve kullaniciya
 * gercegin tersini soylerdi. Sira bu yuzden tesadufi degil.
 *
 * Ambalajlardan biri BILINMIYORSA degisim iddia edilmiyor: `null` "ayni degil"
 * demek degil, "bilmiyorum" demek. Bilinmeyenden zam cikarmak uydurma olurdu.
 *
 * @param now `daysAgo` icin gecerli an; disaridan geliyor ki test saat
 *   kurmadan kosabilsin.
 */
internal fun ListRowProjection.toPriceHint(now: Long): PriceHint {
    val last = lastPriceMinor ?: return PriceHint.None
    val prev = prevPriceMinor
        ?: return PriceHint.Single(
            price = formatChipMinor(last),
            store = lastStoreName ?: BILINMEYEN_MARKET,
            daysAgo = daysBetween(lastObservedAt, now),
        )

    val fromPack = packLabel(prevPackSize, prevPackUnit)
    val toPack = packLabel(lastPackSize, lastPackUnit)
    if (fromPack != null && toPack != null && fromPack != toPack) {
        return PriceHint.PackChanged(
            fromPack = fromPack,
            toPack = toPack,
            note = formatChipMinor(last),
        )
    }

    // YUZDE ONCEKI FIYATA GORE. Sifir bolmeye karsi koruma teorik degil:
    // fiyat alani elle duzenlenebiliyor ve `0,00` yazilabiliyor.
    if (prev <= 0L) return PriceHint.Single(
        price = formatChipMinor(last),
        store = lastStoreName ?: BILINMEYEN_MARKET,
        daysAgo = daysBetween(lastObservedAt, now),
    )
    val delta = (last - prev).toDouble() / prev
    return PriceHint.Trend(
        from = formatChipMinor(prev),
        to = formatChipMinor(last),
        deltaPercent = abs(delta * 100).roundToInt(),
        rising = last > prev,
        history = parseHistory(priceHistory),
    )
}

/**
 * `group_concat` ciktisini sparkline degerlerine cevirir.
 *
 * SQL yeniden eskiye siraliyor, sparkline ise soldan saga ZAMAN okuyor -
 * dolayisiyla ters cevriliyor. Ters cevrilmezse grafik zamanda geriye akardi
 * ve yukselen bir fiyat dusuyormus gibi gorunurdu.
 *
 * Bozuk parca sessizce ATLANIYOR: sparkline bir suslemedir, tek bir okunamayan
 * deger yuzunden satirin tamamini dusurmek orantisiz olurdu.
 */
internal fun parseHistory(raw: String?): List<Float> =
    raw?.split(',')
        ?.mapNotNull { it.trim().toFloatOrNull() }
        ?.reversed()
        .orEmpty()

/** "900 gr" / "1 lt"; ikisinden biri bilinmiyorsa null. */
private fun packLabel(size: Double?, unit: String?): String? {
    if (size == null || unit.isNullOrBlank()) return null
    val number = if (size % 1.0 == 0.0) size.toInt().toString() else size.toString().replace('.', ',')
    return "$number $unit"
}

/**
 * Iki an arasindaki TAM gun sayisi.
 *
 * Takvim gunu degil, 24 saatlik blok - ve bu bir sadelestirme. Projede takvim
 * gunu ile blok ayrimi daha once bir hata kaynagi olmustu; burada tolere
 * edilebilir cunku okunan sey "kac gun once" ve bir saatlik kayma o cumleyi
 * degistirmiyor.
 */
private fun daysBetween(from: Long?, now: Long): Int {
    if (from == null) return 0
    val diff = now - from
    return if (diff <= 0) 0 else (diff / DAY_MS).toInt()
}

private const val DAY_MS = 24L * 60 * 60 * 1000

/** Gozlem marketsiz kaydedilmis - kullanici acele etmis, secmemis. */
private const val BILINMEYEN_MARKET = "market yok"

/**
 * Gezinin tahmini tutari - ESIGI GECIYORSA (E18).
 *
 * `null` iki AYRI sebeple donebilir ve ikisi de dogru cevabi ayni yapiyor:
 * gezinin hic fiyatlanmis urunu yok, ya da esigin altinda var. Ikisinde de
 * ekranda tutar YAZILMIYOR - "0 TL" yazmak bedava alisveris demek, esigin
 * altindaki bir toplami yazmak ise on sekiz urunluk sepetin yanina
 * *"~40 TL"* koymak olurdu.
 *
 * Esik `EstimatedBasket` ile AYNI sabit: iki yerde iki farkli sayi olsaydi
 * ayni gezi listede tutarli, baslikta tutarsiz gorunurdu.
 */
internal fun TripEstimate?.shownMinor(): Long? =
    this?.takeIf { it.pricedCount >= MIN_PRICED_ITEMS }?.estimateMinor
