package com.neydi.app.ui.product

import androidx.compose.runtime.Immutable
import com.neydi.app.data.db.ObservationRow
import com.neydi.app.data.daysBetween
import com.neydi.app.data.formatAge
import com.neydi.app.data.formatDayMonth
import com.neydi.app.data.formatDayMonthAblative
import com.neydi.app.data.formatHeadlineMinor
import com.neydi.app.data.formatMinor
import com.neydi.app.di.now
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * "Nerede ucuz" bolumundeki bir satir (E17).
 *
 * KIMLIK MARKET + MARKA CIFTI (tasarim karari 26): ayni marketten iki marka
 * IKI satir. Yalnizca markete gore gruplamak "BIM'de 100 TL" derdi ama hangi
 * marka oldugunu soylemezdi - ve fiyat farkinin buyuk kismi marka farki.
 */
@Immutable
data class CheapRow(
    val store: String,
    val brand: String?,
    /**
     * "100,00 TL" - BIRIMLI.
     *
     * Onceden `formatChipMinor` ile "100,00" yaziliyordu ve bu, kuralin
     * kendisini ters okumakti: birim YALNIZCA 24dp'lik fiyat cipinde duser
     * (Money.kt), cunku orada yer yok. Burasi cip degil, 15sp'lik bir tablo
     * satiri; maket de "100,00 TL" yaziyor.
     */
    val price: String,
    /** "4 L" gibi; bilinmiyorsa null ve cizilmiyor. */
    val pack: String?,
    /**
     * Gozlemin YASI: "dün", "3 gün önce".
     *
     * FIYATIN TEK BASINA ANLAMI YOK. "BİM 100,00 TL" iki hafta onceki bir
     * gozlemse bugunku karsilastirmaya girmemeli; maket bu yuzden alt satira
     * marka ile yasi birlikte koyuyor ("Dost · dün"). Yas olmadan bolum,
     * eskimis bir sayiyi guncelmis gibi gosteriyordu.
     */
    val recency: String,
)

/** Alim gecmisi satiri: tarih · market · fiyat. */
@Immutable
data class HistoryRow(
    /** Uzun dokunusla silinebilmesi icin (karar 46). */
    val id: String,
    val observedAt: Long,
    /**
     * "6 Ağu" - satirin TARIHI, ve bu bir susleme degil.
     *
     * `observedAt` bastan beri tasiniyordu ama HIC CIZILMIYORDU. Karar 46
     * uzun dokunusla gozlem silmeyi getirdiginde bu sessiz eksik bir kusura
     * dondu: ayni markette ayni fiyati iki kez gormus biri, silecegi satiri
     * digerinden ayirt edemiyor. Silme kapisini acmak, satirin KIMLIGINI de
     * gostermeyi zorunlu kiliyor.
     */
    val date: String,
    val store: String,
    /** "41,00 TL" - birimli; gerekcesi [CheapRow.price]'ta. */
    val price: String,
)

/**
 * Ekran 5'in fiyat bolumu - gozlemlerden.
 *
 * @param headline sheet'in MANSET CUMLESI. Uc gozlemden itibaren trend hali
 *   ("32 TL → 41 TL · 6 Haziran'dan beri %28 arttı"), altinda tek gozlem hali
 *   ("Son ödediğin: 138,50 TL"). Gozlem yoksa `null` ve sheet urun adini
 *   yaziyor. Bolum basligi acikca *"okunacak sey grafik degil manset cumlesi"*
 *   diyor; sheet bugune kadar yalnizca urun adini yaziyordu, yani ekranin
 *   merkezindeki cumle hic yoktu.
 * @param headlineSub mansetin altindaki kaynak satiri - "A101 · 12 gün önce ·
 *   600 g". Mansetin tek basina soylemedigi seyi soyluyor: o fiyat NEREDE ve
 *   NE ZAMAN gorulmus.
 * @param cheapest "Nerede ucuz" satirlari, ucuzdan pahaliya. **Iki
 *   MARKETten azsa BOS**: tek market varken "nerede ucuz" diye bir soru yok,
 *   ve bolum basligini bos cizmek olmayan bir isi varmis gibi gosterir.
 * @param history alim gecmisi, yeniden eskiye.
 * @param sparkline en fazla 9 fiyat, ESKIDEN YENIYE. **Ucten azsa bos** -
 *   iki noktadan cizilen "grafik" bir dogru parcasi ve trend izlenimi verir.
 */
@Immutable
data class PriceSection(
    val headline: String? = null,
    val headlineSub: String? = null,
    val cheapest: List<CheapRow> = emptyList(),
    val history: List<HistoryRow> = emptyList(),
    val sparkline: List<Float> = emptyList(),
) {
    /** Hic gozlem yoksa bolum HIC cizilmiyor - "fiyat yok" da yazilmiyor. */
    val isEmpty: Boolean get() = history.isEmpty()
}

/**
 * Gozlemleri Ekran 5'in fiyat bolumune cevirir.
 *
 * ## Esikler tasarimdan, ve her biri BIR seyi engelliyor
 *
 * - **Bolum 2 SATIR**: karsilastirilacak iki secenek. Once iki MARKET
 *   sayiliyordu ve bu, bolumun kendi ciziminden farkliydi: bolum market+marka
 *   ciftlerini satir satir ciziyor, esik ise marketleri sayiyordu. Karar 58
 *   ikisini esitledi - *"tek markette iki marka gecerli bir
 *   karsilastirmadir"*, cunku kullanicinin sorusu "hangisini alayim".
 * - **Sparkline 3 gozlem**: iki nokta bir dogru parcasi cizer ve olmayan bir
 *   trendi varmis gibi gosterir.
 * - **Delta cipi 2 gozlem**: E16'da, satir tarafinda.
 *
 * ## Her cift icin EN SON fiyat
 *
 * Ortalama DEGIL: kullanicinin sorusu "simdi nerede ucuz", "gecen yil
 * ortalamada neresi ucuzdu" degil. Ortalama almak zamli bir marketi ucuz
 * gostermeye devam ederdi.
 *
 * @param nowMillis gozlem YASLARININ olculdugu an. Varsayilani gercek saat
 *   olmak ZORUNDA: tek cagiran ([com.neydi.app.ui.list.ListViewModel]) bir saat
 *   tasimiyor ve yas, kaydedilen bir veri degil ekranin o andaki okumasi.
 *   Testler degeri gecerek deterministik kalabilir.
 */
internal fun List<ObservationRow>.toPriceSection(nowMillis: Long = now()): PriceSection {
    if (isEmpty()) return PriceSection()

    // Gozlemler YENIDEN ESKIYE geliyor, yani her ciftin ILK gorulen kaydi en
    // sonuncusu. `groupBy` giris sirasini korudugu icin fazladan siralama yok.
    val byPair = groupBy { it.storeName to it.brand }
    val cheapest = if (byPair.size < MIN_ROWS) {
        emptyList()
    } else {
        byPair.values
            .map { it.first() }
            .sortedBy { it.unitPriceMinor }
            .map {
                CheapRow(
                    store = it.storeName ?: MARKET_YOK,
                    brand = it.brand,
                    price = formatMinor(it.unitPriceMinor),
                    pack = packLabel(it.packSize, it.packUnit),
                    recency = formatAge(daysBetween(it.observedAt, nowMillis)),
                )
            }
    }

    // MANSET EN SON GOZLEMDEN: liste yeniden eskiye geliyor, yani `first()`
    // kullanicinin en son odedigi fiyat. Trend cumlesi (karar 67) uc gozlemden
    // itibaren onun YERINE geciyor; kuramadigi her durumda tek gozlem manseti
    // geri geliyor - bir cumle yazamamak, dayanaksiz bir yuzde yazmaktan iyi.
    val newest = first()
    return PriceSection(
        headline = trendHeadline(nowMillis)
            ?: "Son ödediğin: ${formatMinor(newest.unitPriceMinor)}",
        // MARKET BILINMIYORSA SATIRDAN DUSUYOR, "market yok" YAZILMIYOR:
        // gecmis tablosunda o metin bos bir SUTUNU dolduruyor, burada ise
        // cumlenin ortasina girip hata gibi okunurdu.
        headlineSub = listOfNotNull(
            newest.storeName,
            formatAge(daysBetween(newest.observedAt, nowMillis)),
            packLabel(newest.packSize, newest.packUnit),
        ).joinToString(" · "),
        cheapest = cheapest,
        history = map {
            HistoryRow(
                id = it.id,
                observedAt = it.observedAt,
                date = formatDayMonth(it.observedAt),
                store = it.storeName ?: MARKET_YOK,
                price = formatMinor(it.unitPriceMinor),
            )
        },
        sparkline = if (size < MIN_TREND) {
            emptyList()
        } else {
            map { it.unitPriceMinor.toFloat() }.reversed()
        },
    )
}

/**
 * Trend manseti (karar 67): **"32 TL → 41 TL · 6 Haziran'dan beri %28 arttı"**.
 *
 * Kuramiyorsa `null` ve manset tek gozlem haline dusuyor. Uc dal, ucu de
 * kararin uc kurali:
 *
 * 1. **Aralik VERIDEN.** Cumlenin tarihi kullanilan ILK gozlemin tarihi -
 *    liste yeniden eskiye geldigi icin `last()`. Sabit bir *"son 3 ayda"*
 *    iddiasi, iki haftalik da uc yillik da olsa ayni seyi yazardi.
 * 2. **Kurus sifirsa yazilmiyor** ([formatHeadlineMinor]). Tilde YOK:
 *    `formatEstimate` de kurusu dusuruyor ama tahmin isaretiyle, oysa bu iki
 *    sayi kullanicinin etiketten okudugu kesin fiyatlar.
 * 3. **Ambalaj degistiyse yuzde IDDIA EDILMIYOR** ve manset ambalaj cumlesine
 *    donuyor. Kontrol yuzdenin ONUNDE, `PriceHintMapping` ile ayni sirada ve
 *    ayni sebeple: 5 L -> 4 L ayni fiyata satiliyorsa bu bir dusus degil gizli
 *    zam, yuzde ise onu dusus diye yazardi.
 *
 * Ambalajlardan biri BILINMIYORSA degisim iddia edilmiyor - `null` "ayni degil"
 * degil, "bilmiyorum" demek; bilinmeyenden zam cikarmak uydurma olurdu.
 *
 * YUZDE SIFIRA YUVARLANIYORSA cumle de yok: *"%0 arttı"* okunacak bir sey
 * soylemiyor ve iki ucu esit fiyat trend degil. Onceki fiyat sifir ya da eksi
 * ise ayni sekilde - fiyat alani elle duzenlenebiliyor ve `0,00` yazilabiliyor,
 * yani sifira bolme teorik degil.
 */
private fun List<ObservationRow>.trendHeadline(nowMillis: Long): String? {
    if (size < MIN_TREND) return null
    val newest = first()
    val oldest = last()

    val fromPack = packLabel(oldest.packSize, oldest.packUnit)
    val toPack = packLabel(newest.packSize, newest.packUnit)
    if (fromPack != null && toPack != null && fromPack != toPack) {
        return "${packVerb(oldest, newest)}: $fromPack → $toPack"
    }

    val from = oldest.unitPriceMinor
    val to = newest.unitPriceMinor
    if (from <= 0L) return null
    val percent = abs((to - from).toDouble() / from * 100).roundToInt()
    if (percent == 0) return null

    // "düştü", ikonografinin asagi ok icin kullandigi sozcuk ("Fiyat düştü");
    // "arttı" maketin kendi cumlesinden.
    val direction = if (to > from) "arttı" else "düştü"
    val since = formatDayMonthAblative(oldest.observedAt, nowMillis)
    return "${formatHeadlineMinor(from)} → ${formatHeadlineMinor(to)} · " +
        "$since beri %$percent $direction"
}

/**
 * Ambalaj cumlesinin FIILI: "Ambalaj küçüldü" / "büyüdü" / "değişti".
 *
 * Kararin ornegi kuculme ("5 L → 4 L") ama tek fiil yazmak, buyuyen ambalajda
 * dogrudan yalan olurdu - ve 4 L -> 5 L en az oteki kadar sik.
 *
 * YON ANCAK AYNI BIRIMDE OKUNUR: "900 g → 1 kg"da sayilar 900 ile 1, yani
 * kiyaslanan sey sayi degil birim; orada yonsuz "değişti" tek dogru cumle.
 * Birim metinleri HARFI HARFINE kiyaslaniyor: locale'siz buyutme/kucultme
 * Turkce'de yasak (noktali ve noktasiz i birbirine donusur) ve zaten farkli
 * yazilan bir birim gercekten farkli birim olabilir.
 */
private fun packVerb(from: ObservationRow, to: ObservationRow): String {
    val before = from.packSize
    val after = to.packSize
    if (before == null || after == null || from.packUnit != to.packUnit) return "Ambalaj değişti"
    return if (after < before) "Ambalaj küçüldü" else "Ambalaj büyüdü"
}

/** "4 L" / "900 gr"; ikisinden biri bilinmiyorsa null. */
private fun packLabel(size: Double?, unit: String?): String? {
    if (size == null || unit.isNullOrBlank()) return null
    val number = if (size % 1.0 == 0.0) size.toInt().toString() else size.toString().replace('.', ',')
    return "$number $unit"
}

/** Bolumun cizilmesi icin gereken en az SATIR - market+marka cifti (karar 58). */
private const val MIN_ROWS = 2

/**
 * Sparkline ile trend mansetinin ORTAK esigi: uc gozlem.
 *
 * Karar 67 esigi acikca sparkline'a bagliyor ve tek sabit olmasi bunun
 * kendisi: iki ayri sayi olsaydi ayni urunde grafik cizilirken manset
 * cizilmeyebilirdi - ayni ekranin iki yarisi farkli konusurdu. Gerekce de
 * ortak: iki nokta bir dogru parcasi cizer ve olmayan bir trendi varmis gibi
 * gosterir.
 */
private const val MIN_TREND = 3

/** Marketi secilmemis gozlem - kullanici acele etmis. */
private const val MARKET_YOK = "market yok"
