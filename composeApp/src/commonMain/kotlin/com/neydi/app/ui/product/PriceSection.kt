package com.neydi.app.ui.product

import androidx.compose.runtime.Immutable
import com.neydi.app.data.db.ObservationRow
import com.neydi.app.data.daysBetween
import com.neydi.app.data.formatAge
import com.neydi.app.data.formatDayMonth
import com.neydi.app.data.formatMinor
import com.neydi.app.di.now

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
 * @param headline sheet'in MANSET CUMLESI - "Son ödediğin: 138,50 TL".
 *   Gozlem yoksa `null` ve sheet urun adini yaziyor. Bolum basligi acikca
 *   *"okunacak sey grafik degil manset cumlesi"* diyor; sheet bugune kadar
 *   yalnizca urun adini yaziyordu, yani ekranin merkezindeki cumle hic yoktu.
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
    // kullanicinin en son odedigi fiyat. Trend cumlesi ("32 TL -> 41 TL · son
    // 3 ayda %28 arttı") BILEREK YOK - o cumle grafigin aritmetigini (ay
    // araligi, ambalaj normalizasyonu) gerektiriyor ve grafik henuz yok;
    // dayanagi olmayan bir yuzde yazmak, bu ekranin kacindigi tek sey.
    val newest = first()
    return PriceSection(
        headline = "Son ödediğin: ${formatMinor(newest.unitPriceMinor)}",
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
        sparkline = if (size < MIN_SPARKLINE) {
            emptyList()
        } else {
            map { it.unitPriceMinor.toFloat() }.reversed()
        },
    )
}

/** "4 L" / "900 gr"; ikisinden biri bilinmiyorsa null. */
private fun packLabel(size: Double?, unit: String?): String? {
    if (size == null || unit.isNullOrBlank()) return null
    val number = if (size % 1.0 == 0.0) size.toInt().toString() else size.toString().replace('.', ',')
    return "$number $unit"
}

/** Bolumun cizilmesi icin gereken en az SATIR - market+marka cifti (karar 58). */
private const val MIN_ROWS = 2

/** Sparkline'in cizilmesi icin gereken en az gozlem (tasarim). */
private const val MIN_SPARKLINE = 3

/** Marketi secilmemis gozlem - kullanici acele etmis. */
private const val MARKET_YOK = "market yok"
