package com.neydi.app.ui.product

import androidx.compose.runtime.Immutable
import com.neydi.app.data.db.ObservationRow
import com.neydi.app.data.formatDayMonth
import com.neydi.app.data.formatChipMinor

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
    val price: String,
    /** "4 L" gibi; bilinmiyorsa null ve cizilmiyor. */
    val pack: String?,
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
    val price: String,
)

/**
 * Ekran 5'in fiyat bolumu - gozlemlerden.
 *
 * @param cheapest "Nerede ucuz" satirlari, ucuzdan pahaliya. **Iki
 *   MARKETten azsa BOS**: tek market varken "nerede ucuz" diye bir soru yok,
 *   ve bolum basligini bos cizmek olmayan bir isi varmis gibi gosterir.
 * @param history alim gecmisi, yeniden eskiye.
 * @param sparkline en fazla 9 fiyat, ESKIDEN YENIYE. **Ucten azsa bos** -
 *   iki noktadan cizilen "grafik" bir dogru parcasi ve trend izlenimi verir.
 */
@Immutable
data class PriceSection(
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
 */
internal fun List<ObservationRow>.toPriceSection(): PriceSection {
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
                    price = formatChipMinor(it.unitPriceMinor),
                    pack = packLabel(it.packSize, it.packUnit),
                )
            }
    }

    return PriceSection(
        cheapest = cheapest,
        history = map {
            HistoryRow(
                id = it.id,
                observedAt = it.observedAt,
                date = formatDayMonth(it.observedAt),
                store = it.storeName ?: MARKET_YOK,
                price = formatChipMinor(it.unitPriceMinor),
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
