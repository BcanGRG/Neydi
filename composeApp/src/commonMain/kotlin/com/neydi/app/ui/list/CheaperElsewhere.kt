package com.neydi.app.ui.list

import com.neydi.app.data.db.ListRowProjection
import com.neydi.app.data.formatChipMinor
import com.neydi.app.data.turkishLocative

/**
 * "Baska markette ucuz" cipi (F5.5, tasarim karari 41).
 *
 * ## Cipin trendden farki: biri bilgi, oteki eylem
 *
 * Trend *"gecen ay 38,50'ydi"* der; cip *"burada 12 TL fazla veriyorsun"*
 * der. Reyonda duran biri icin ikincisi acil. Tasarim bu yuzden ikisi ayni
 * anda dogruysa **cipi kazandiriyor, trendi bastiriyor** - ayni satirda iki
 * fiyat cumlesi yan yana durursa hangisine gore hareket edilecegi belirsiz
 * kalir.
 *
 * ## Ucu birden gerekiyor
 *
 * 1. **Hem %10 hem 5 TL** ucuz olacak. Tek basina yuzde, 3 TL'lik urunu 30
 *    kurus icin ihbar eder; tek basina mutlak TL, pahali urunde %2'yi buyuk
 *    gosterir.
 * 2. **14 gunden eski olmayacak.** 24dp'lik cipte tarih yazacak yer yok;
 *    bayatligi gosteremiyorsak hic gostermemek gerekiyor. Bu esik
 *    [com.neydi.app.data.db.TripLineDao.observeList] icinde, SQL tarafinda.
 * 3. **Ambalajin ayni oldugu KANITLI olacak** ([provablySamePack]). 1 lt ile
 *    5 lt arasindaki farki "ucuz" diye ihbar etmek, trendin ambalaj dalinin
 *    engelledigi yalanin aynisi olurdu - sadece zaman ekseninde degil, market
 *    ekseninde. Trendden farkli olarak burada `null` da yetmiyor: cip yeni bir
 *    iddia kuruyor ve iddianin ispat yuku daha agir.
 *
 * ## Liste basina en fazla uc
 *
 * Ustu listeyi reklam yuzeyine cevirir. Siralama MUTLAK TL tasarrufuna gore,
 * yuzdeye gore degil: cebe giren para mutlak.
 */
internal object CheaperChip {
    /** Karar 41: karsi gozlem en az bu kadar ucuz olacak. */
    const val MIN_RATIO = 0.10

    /** Karar 41: ve ayni anda en az bu kadar (kurus cinsinden 5 TL). */
    const val MIN_SAVING_MINOR = 500L

    /** Karar 41: liste basina en fazla bu kadar cip. */
    const val MAX_PER_LIST = 3

    /** Karar 41: karsi gozlem bundan eskiyse cip cizilmiyor. */
    const val FRESH_MS = 14L * 24 * 60 * 60 * 1000
}

/**
 * Satirin cip adayi - ya da esikleri gecmiyorsa `null`.
 *
 * @return cip metni ("A101'de 36,00") ve tasarrufu; siralama tasarrufa gore
 *   yapilacagi icin ikisi birlikte doner.
 */
internal fun ListRowProjection.cheaperCandidate(): CheaperCandidate? {
    val paid = lastPriceMinor ?: return null
    val rival = rivalPriceMinor ?: return null
    val store = rivalStoreName ?: return null

    // AMBALAJ FILTRESI ESIKTEN ONCE (karar 58'in "Nerede ucuz" ilkesi, ayni
    // sebeple burada da): karsilastirilamayan iki boyun farki tasarruf degil.
    //
    // KANIT ISTIYOR, `comparablePack`in gevsekligini PAYLASMIYOR. Kullanicinin
    // kendi verisinde bunun bedeli olculdu: 22 Agustos'ta yogurdun son gozlemi
    // ambalajsiz (192,00), A101'deki en ucuz gozlemi 250 ml'lik 49,00'di.
    // Gevsek kural o satira *"A101'de 49,00"* yazdiracakti - 3 kg'lik kovayla
    // 250 ml'lik kaseyi karsilastiran bir cumle. Sessizlik dogru cevap.
    if (!provablySamePack(lastPackSize, lastPackUnit, rivalPackSize, rivalPackUnit)) return null

    val saving = paid - rival
    if (saving < CheaperChip.MIN_SAVING_MINOR) return null
    // Sifir bolme korumasi teorik degil: fiyat alani elle duzenlenebiliyor.
    if (paid <= 0L) return null
    if (saving.toDouble() / paid < CheaperChip.MIN_RATIO) return null

    return CheaperCandidate(
        text = "${turkishLocative(store)} ${formatChipMinor(rival)}",
        savingMinor = saving,
    )
}

/** @property savingMinor siralama bunun uzerinden - mutlak TL tasarrufu. */
internal data class CheaperCandidate(val text: String, val savingMinor: Long)

/**
 * Listenin cip haritasi: satir kimligi -> cip metni.
 *
 * Neden satir satir degil de LISTE seviyesinde: "en fazla 3" ve "tasarrufa
 * gore sirali" kurallarinin ikisi de satirin tek basina bilemeyecegi seyler.
 * Satir kendi adayini uretebilir, ama kendisinin ilk uce girip girmedigini
 * ancak digerlerini gorerek bilir.
 */
internal fun List<ListRowProjection>.cheaperChips(): Map<String, String> =
    mapNotNull { row -> row.cheaperCandidate()?.let { row.rowId to it } }
        .sortedByDescending { (_, candidate) -> candidate.savingMinor }
        .take(CheaperChip.MAX_PER_LIST)
        .associate { (rowId, candidate) -> rowId to candidate.text }
