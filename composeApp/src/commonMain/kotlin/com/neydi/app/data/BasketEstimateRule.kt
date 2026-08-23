package com.neydi.app.data

import com.neydi.app.data.store.storeDisplayName
import kotlin.math.ceil
import kotlin.math.roundToLong

/**
 * Tahmine giren tek satirin bildigi her sey.
 *
 * @property unit satirin GECERLI birimi (`unitOverride ?: unit`, karar 108).
 * @property unitPriceMinor son gozlemin manset fiyati; gozlem yoksa `null`.
 * @property packSize gozlemin ambalaj buyuklugu; okunamadiysa `null`.
 * @property chain gozlemin zinciri; marketsiz cekim mumkun oldugu icin `null`
 *   olabilir ve `null`in kendisi karar 113'te bir anlam tasiyor.
 */
internal data class EstimateLine(
    val quantity: Double,
    val unit: String,
    val unitPriceMinor: Long? = null,
    val packSize: Double? = null,
    val packUnit: String? = null,
    val chain: String? = null,
    /**
     * Marketin EKRANDA yazilan adi - kimlik `chain`, gorunen bu.
     *
     * Ikisi ayri, cunku `chain` normalize bir ANAHTAR ("bim") ve ekrana
     * yazilinca kucuk harfle cikiyordu. Donusturmek de yasak: bu projede
     * locale'siz harf donusumu olculmus bir hata ("İNCİR".lowercase() yedi
     * kod noktasi uretiyor). Dogru yol, tabloda zaten duran adi okumak.
     */
    val storeName: String? = null,
)

/** Tahmin satirinin sagindaki cumle (karar 95, 113-114). */
internal sealed interface EstimateSource {
    /** Butun katkilar tek zincirden: *"BIM fiyatlariyla"*. */
    data class Chain(val name: String) : EstimateSource

    /** Karisik, ya da bir tanesi bile marketsiz: *"son fiyatlarla"*. */
    data object Mixed : EstimateSource
}

/** Katlanmis sepet: tutar, pay, payda ve kaynak. */
internal data class BasketTotals(
    val amountMinor: Long = 0,
    /** TOPLAMA GIREN satir sayisi - fiyati olan degil (karar 112). */
    val pricedCount: Int = 0,
    val totalCount: Int = 0,
    val source: EstimateSource = EstimateSource.Mixed,
)

/**
 * Satir SAYILAN mi (karar 96: *"carpan yalniz adet satirinda"*).
 *
 * Tasarimin "adet" demesi bir kelime degil bir SINIF: paket, kutu, demet ve
 * sise de sayiliyor ve katalogda gercekten var (bes `demet` urun). Kural bu
 * yuzden "adet ise" degil "tartilmiyorsa" diye yazildi.
 *
 * TANINMAYAN BIRIM DE SAYILAN: burada `null` donmek satiri dusururdu, oysa
 * bilinmeyen bir birimin adet olma ihtimali tartilma ihtimalinden yuksek -
 * `kavanoz`, `file`, `paket` gibi. Yanlis tarafa dusmenin bedeli farkli:
 * sayilan sanip carpmak en fazla bir carpani yanlis yapar, tartili sanip
 * dusurmek satiri sessizce yok eder.
 */
internal fun isCountLine(unit: String): Boolean =
    when (unitKind(unit)) {
        UnitKind.MASS, UnitKind.VOLUME -> false
        else -> true
    }

/**
 * Kac AMBALAJ aliniyor - karar 96'nin `ceil(miktar / ambalaj)` dali.
 *
 * `null` DONER: iki birim ayni olcege indirilemiyorsa. Karar 111 bunu acikca
 * yaziyor - *"birimler ayni olcege indirilemiyorsa satir ambalajsiz sayilir,
 * bolme yapilmaz"*. Sebep olculdu: satir birimi ile ambalaj birimi farkli
 * kanonlar konusuyor ve ham bolme `2 g` ile `1 kg`i iki pakete cevirirdi.
 *
 * YUKARI YUVARLIYOR ve miktar ambalajdan KUCUK olsa bile en az bir paket:
 * yarim kilo icin 250 g'lik iki paket aliniyor, 1,2 kg icin uc.
 */
internal fun packsNeeded(
    quantity: Double,
    unit: String,
    packSize: Double,
    packUnit: String,
): Int? {
    if (packSize <= 0.0) return null
    val packInLineUnit = convertMagnitude(packSize, packUnit, unit) ?: return null
    if (packInLineUnit <= 0.0) return null
    return ceil(quantity / packInLineUnit).toInt().coerceAtLeast(1)
}

/**
 * Satirin toplama KATKISI - karar 96 ve 111.
 *
 * `null` = satir toplama girmiyor. Uc ayri sebeple olabiliyor ve ucu de ayni
 * seye varir: *"tutari yok, sifir degil"*. Sifir donseydi cagiran taraf onu
 * bir CEVAP sayardi ve satiri paya katardi.
 *
 * | Satir | Katki |
 * |---|---|
 * | gozlem yok | `null` |
 * | sayilan birim | `miktar x fiyat` |
 * | tartili, ambalaj biliniyor | `⌈miktar ÷ ambalaj⌉ x fiyat` |
 * | tartili, ambalaj bilinmiyor ya da olcek tutmuyor | `null` |
 *
 * EN YAKIN KURUSA yuvarlaniyor. Bugun bu carpim SQL'de yapiliyor ve
 * `SUM(REAL)` Room'a `Long` olarak okundugu icin kurus ASAGI kirpiliyordu -
 * 0,75 x 62,50 = 46,875 TL, 46,87 diye kaydediliyordu. Tek kurus, ama
 * `formatEstimate`in kendi kurali *"yuvarlama en yakina"* diyor.
 */
internal fun lineEstimateMinor(line: EstimateLine): Long? {
    val price = line.unitPriceMinor ?: return null
    if (isCountLine(line.unit)) return (line.quantity * price).roundToLong()
    val size = line.packSize ?: return null
    val packUnit = line.packUnit ?: return null
    val packs = packsNeeded(line.quantity, line.unit, size, packUnit) ?: return null
    return packs.toLong() * price
}

/**
 * Tahmin satirinin sagindaki cumle (karar 95, 113-114).
 *
 * ## Yalniz TOPLAMA GIREN satirlar oy veriyor (karar 114)
 *
 * Karar 96'dan sonra ikisi ayrisiyor: toplamdan dusen bir A101 satiri,
 * katkida bulunmadigi bir tutari *"son fiyatlarla"* diye etiketletebilirdi.
 * Cumle yanindaki TUTARI anlatiyor, listeyi degil.
 *
 * ## Tek marketsiz gozlem zincir adini dusuruyor (karar 113)
 *
 * `storeId` nullable ve marketsiz cekim mesru. *"BIM fiyatlariyla"* bir IDDIA;
 * iceride nereden geldigi bilinmeyen bir fiyat varsa iddia kanitlanmis degil.
 * `provablySamePack` ile ayni katilik.
 */
internal fun estimateSource(contributing: List<EstimateLine>): EstimateSource {
    if (contributing.isEmpty()) return EstimateSource.Mixed
    // KIMLIK `chain`, GORUNEN `storeName`: ilki normalize bir anahtar ("bim"),
    // ikincisi tablodaki ad ("BİM"). Anahtarla karsilastirip adi yazmak,
    // iki subenin ayni zincir sayilmasini saglarken ekrana kucuk harf
    // dusurmuyor.
    //
    // KARAR 113 AYRI BIR DAL DEGIL, BU KURALIN DOGAL SONUCU: `null` da bir
    // deger ve tekillige katiliyor. "BIM + marketsiz" iki farkli deger, yani
    // tek zincir degil. Ayri bir `if (chain == null)` yazmak, ayni seyi iki
    // kez soylemekti - nitekim silindiginde hicbir test kirilmadi.
    contributing.map { it.chain }.distinct().singleOrNull() ?: return EstimateSource.Mixed
    val label = contributing.firstNotNullOfOrNull { storeDisplayName(it.storeName) }
        ?: return EstimateSource.Mixed
    return EstimateSource.Chain(label)
}

/**
 * Sepeti katlar (karar 95, 96, 111, 112).
 *
 * PAY ILE PAYDA AYRISIYOR ve bu, hesabin SQL'den Kotlin'e tasinmasinin
 * sebebi: bugunku sorgu `SUM(...)` ile `COUNT(*)`i ayni `GROUP BY` uzerinde
 * hesapliyor, yani toplamdan cikan satir sayidan da cikiyor. Karar 96 tam
 * tersini istiyor - *"toplamdan duser, PAYDA KALIR"*.
 */
internal fun foldBasket(lines: List<EstimateLine>): BasketTotals {
    val contributions = lines.map { it to lineEstimateMinor(it) }
    val counted = contributions.filter { it.second != null }
    return BasketTotals(
        amountMinor = counted.sumOf { it.second ?: 0L },
        pricedCount = counted.size,
        totalCount = lines.size,
        source = estimateSource(counted.map { it.first }),
    )
}

/**
 * Satir tahmine GIRMIYOR mu - ve sebebi AMBALAJ mi (karar 111).
 *
 * ## Neden ayri bir soru
 *
 * [lineEstimateMinor] `null` donuyor ama sebebini soylemiyor: gozlem yok da
 * olabilir, ambalaj bilinmiyor da. Satirin metasi ikisini AYIRMAK zorunda -
 * gozlemi olmayan satir zaten hicbir sey yazmiyor, ambalaji bilinmeyen satir
 * ise *"3 kg · ambalaj bilinmiyor"* yaziyor ve fiyatini gostermeye devam
 * ediyor.
 *
 * Karar 111'in cumlesi: *"Yogurt satiri fiyatini gosteriyor ama toplama
 * girmiyor: 192,00 TL bir ambalajin fiyati, 3 kg'in degil. Eksikligi meta
 * yuvasi yaziyor."*
 */
internal fun packIsMissing(line: EstimateLine): Boolean =
    line.unitPriceMinor != null &&
        !isCountLine(line.unit) &&
        lineEstimateMinor(line) == null

/**
 * Tahmin satiri CIZILIYOR mu (karar 112).
 *
 * ## Esik MUTLAK, yuzde degil
 *
 * Tasarim sistemi bir sure *"%60'indan azi -> %40 opaklik ve `~` oneki;
 * %30'un altinda hic gosterilmez"* diyordu ve kod karar 53'un mutlak ucunu
 * kullaniyordu. Karar 96 PAYI dusurunce ikisi ilk kez cakisti; karar 112
 * yuzde fikralarinin ucunu de defterden dusurdu.
 *
 * SAYAC ZATEN KAPSAMI YAZIYOR (karar 95): *"BIM fiyatlariyla · 4/7"* okuyan
 * biri neyin bilinmedigini de okumus oluyor. Opaklikla ikinci kez soylemek,
 * ayni seyi iki dille tekrarlamakti.
 *
 * ESIGIN ALTINDA SESSIZLIK: *"yeterli veri yok"* diye bir yuzey yazilmiyor.
 * Olmayan bir isi varmis gibi gostermek, tasarimin genel degismezi.
 */
internal fun basketIsShown(pricedCount: Int): Boolean = pricedCount >= MIN_PRICED_ITEMS

/** Tahmin satirinin gorunme esigi (karar 53, karar 112 ile teyit edildi). */
internal const val MIN_PRICED_ITEMS = 3
