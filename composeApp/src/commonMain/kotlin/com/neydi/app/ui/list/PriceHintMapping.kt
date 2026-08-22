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
 * @param chipWins satirda "baska markette ucuz" cipi cizilecekse `true`;
 *   trend dali atlanir ve [PriceHint.Single] doner (karar 41). Kararin
 *   kendisi burada VERILMIYOR - "en fazla 3" kurali listeyi gorenin isi,
 *   bkz. [cheaperChips].
 */
internal fun ListRowProjection.toPriceHint(now: Long, chipWins: Boolean = false): PriceHint {
    val last = lastPriceMinor ?: return PriceHint.None
    val single = PriceHint.Single(
        price = formatChipMinor(last),
        store = lastStoreName ?: BILINMEYEN_MARKET,
        daysAgo = daysBetween(lastObservedAt, now),
    )
    val prev = prevPriceMinor ?: return single

    val fromPack = packLabel(prevPackSize, prevPackUnit)
    val toPack = packLabel(lastPackSize, lastPackUnit)
    if (!comparablePack(prevPackSize, prevPackUnit, lastPackSize, lastPackUnit) &&
        fromPack != null && toPack != null
    ) {
        return PriceHint.PackChanged(
            fromPack = fromPack,
            toPack = toPack,
            note = formatChipMinor(last),
        )
    }

    // CIP KAZANIRSA TREND BASTIRILIYOR (karar 41). Ambalaj dali bunun USTUNDE
    // kaldi ve sirasi bilincli: ambalaj degisimi cipten de once gelen bir
    // gercek - "5 L -> 4 L" satiri, baska markette ucuz olsa bile yazilmali.
    //
    // Geriye Single donuyor, None degil: satirin ne odendigi bilgisi cipin
    // getirdigi bilgiden BAGIMSIZ ve ikisi ayni satirda yan yana duruyor.
    // None donseydi cip kazandiginda satir kendi fiyatini unuturdu.
    if (chipWins) return single

    // YUZDE ONCEKI FIYATA GORE. Sifir bolmeye karsi koruma teorik degil:
    // fiyat alani elle duzenlenebiliyor ve `0,00` yazilabiliyor.
    if (prev <= 0L) return single
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

/**
 * Iki ambalaj KARSILASTIRILABILIR mi - trendin de cipin de dayandigi ONERME.
 *
 * Trend *"su kadar zam"* der, cip *"orada su kadar ucuz"* der; ikisi de ancak
 * karsilastirilan iki seyin ayni boy olmasi halinde dogru. Onerme ortak
 * oldugu icin kural da tek yerde: iki dal ayri ayri yazilsaydi biri
 * degistiginde oteki sessizce geride kalirdi.
 *
 * ## Biri bilinmiyorsa KARSILASTIRILABILIR sayiliyor
 *
 * `null` "ayni degil" demek degil, "bilmiyorum" demek. Bilinmeyenden ambalaj
 * degisimi cikarmak uydurma olurdu.
 *
 * ⚠ Ama bu, madalyonun oteki yuzunu acikta birakiyor: **bilinmeyenden trend
 * cikarmak da ayni uydurma.** Bir yanin ambalaji okunmus, oteki okunmamissa
 * (1,5 kg -> `null`) bugun trend dali cekinmeden yuzde yaziyor. Gercek
 * cihazda bu bir kez oldu: ayni markette bir dakika arayla cekilen iki farkli
 * boy yogurt *"%88 zam"* diye gorundu. Kural tasarima soruldu
 * (`docs/27-tasarima-sorular-12.md`); cevap gelince degisecek TEK yer burasi.
 *
 * Cip bu gevsekligi PAYLASMIYOR - bkz. [provablySamePack].
 */
internal fun comparablePack(
    aSize: Double?,
    aUnit: String?,
    bSize: Double?,
    bUnit: String?,
): Boolean {
    val a = packLabel(aSize, aUnit) ?: return true
    val b = packLabel(bSize, bUnit) ?: return true
    return a == b
}

/**
 * Iki ambalajin AYNI oldugu KANITLI mi - [comparablePack]'in kati ikizi.
 *
 * ## Neden cip trendden daha kati
 *
 * Ikisi de "karsilastirilabilir mi" diye soruyor ama YANLIS CEVABIN BEDELI
 * ayni degil:
 *
 * - Trend zaten elindeki iki gozlemi anlatiyor; yanlissa satirda fazladan bir
 *   yuzde durur.
 * - Cip **yeni bir iddia** kuruyor: *"su urun orada su fiyata"*. Yanlissa
 *   kullaniciyi baska bir markete yollar ve orada baska bir sey bulur.
 *
 * Tasarimin kendi tercihi de bu yonde: "Nerede ucuz" boyu bilinmeyeni listeden
 * DUSURUYOR ve geriye iki satir kalmazsa bolumu hic cizmiyor (karar 58) -
 * karsilastirilamayan bir karsilastirmadansa sessizlik.
 *
 * ## Bunun bedeli acikca kabul ediliyor
 *
 * Ambalaj her etikette okunmuyor; kati kural cipin KAPSAMINI daraltiyor.
 * Daraltmak dogru olan: cip zaten "liste basina en fazla 3" ile sinirli, yani
 * bol degil SAGLAM olmasi isteniyor.
 */
internal fun provablySamePack(
    aSize: Double?,
    aUnit: String?,
    bSize: Double?,
    bUnit: String?,
): Boolean {
    val a = packLabel(aSize, aUnit) ?: return false
    val b = packLabel(bSize, bUnit) ?: return false
    return a == b
}

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
