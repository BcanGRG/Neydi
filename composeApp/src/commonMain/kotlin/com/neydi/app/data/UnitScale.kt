package com.neydi.app.data

/**
 * Bir birimin NE OLCTUGU - sayi mi, kutle mi, hacim mi.
 *
 * Karar 96 `ceil(miktar / ambalaj)` diyor ve bu bolme yalnizca iki taraf AYNI
 * SEYI olcuyorsa anlamli: kilogramı adete bolmek bir sayi uretir ama o sayi
 * hicbir sey degildir.
 */
internal enum class UnitKind { COUNT, MASS, VOLUME }

/**
 * Uc kanonu OKURKEN uzlastiran tablo.
 *
 * ## Neden uc kanon var
 *
 * Projede birim uc ayri yerde ayri yazilmis ve bu BILINCLI:
 *
 * | Kaynak | Yazimi |
 * |---|---|
 * | `QuantityParser.UNITS` (katalog, satir birimi) | `kg` `g` `L` `ml` `adet` |
 * | `normalizeUnit` (OCR / `packUnit`) | `kg` `gr` `lt` `adet` |
 * | `TagPriceReader.unitKey` (birim fiyat satiri) | `kg` `lt` `adet` |
 *
 * `normalizeUnit`in KDoc'u ayrimi acikca savunuyor: *"[UNITS] ile AYNI KANON
 * DEGIL ve bu bilerek boyle birakildi (E2): bu fonksiyonun ciktilari fis
 * satirlarina YAZILMIS durumda ve bir tasima adiminda davranis
 * degistirilmez."*
 *
 * ## Neden yazarak degil OKUYARAK uzlastiriyoruz
 *
 * Kanonlari birlestirmek, tabloda duran satirlari yeniden yazmak demek - yani
 * bir veri gocu. Burasi bir OKUMA katmani: hicbir satir degismiyor, yalnizca
 * ayni seyin iki yazimi ayni sey sayiliyor. E2'nin kurali ayakta kaliyor.
 *
 * ## Neden gerekli oldugu OLCULDU
 *
 * Kullanicinin aktif listesindeki **bes fiyatli satirin sifirinda** satir
 * birimi ile ambalaj birimi ayni dizge: `L`/`lt`, `g`/`kg`, `adet`/`gr`. Yani
 * ham bir `ceil(miktar / ambalaj)` bugun var olan veride bile yanlis:
 * kapali bir gezideki `2 g Cay` satiri `1 kg`lik ambalaja karsi `ceil(2/1)=2`
 * verir - 798,00 TL, dogrusu 399,00. `500 g` olsaydi 199.500,00 TL.
 */
private val KINDS: Map<String, UnitKind> = mapOf(
    // Kutle
    "kg" to UnitKind.MASS, "ka" to UnitKind.MASS, "kilo" to UnitKind.MASS,
    "kilogram" to UnitKind.MASS,
    "g" to UnitKind.MASS, "gr" to UnitKind.MASS, "gram" to UnitKind.MASS,
    // Hacim
    "l" to UnitKind.VOLUME, "lt" to UnitKind.VOLUME, "litre" to UnitKind.VOLUME,
    "ml" to UnitKind.VOLUME, "cc" to UnitKind.VOLUME,
    // Sayilanlar
    "adet" to UnitKind.COUNT, "ad" to UnitKind.COUNT, "tane" to UnitKind.COUNT,
    "paket" to UnitKind.COUNT, "pkt" to UnitKind.COUNT, "kutu" to UnitKind.COUNT,
    "demet" to UnitKind.COUNT, "şişe" to UnitKind.COUNT,
)

/** Temel birim cinsinden buyukluk: gram ve mililitre. */
private val FACTORS: Map<String, Double> = mapOf(
    "kg" to 1_000.0, "ka" to 1_000.0, "kilo" to 1_000.0, "kilogram" to 1_000.0,
    "g" to 1.0, "gr" to 1.0, "gram" to 1.0,
    "l" to 1_000.0, "lt" to 1_000.0, "litre" to 1_000.0,
    "ml" to 1.0, "cc" to 1.0,
)

/**
 * Birimin ne olctugu; taninmiyorsa `null`.
 *
 * TANINMAYAN KELIME `COUNT` SAYILMIYOR ve bu onemli: bilinmeyeni sayilan kabul
 * etmek, onu carpilabilir yapardi - yani en riskli varsayim. Cagiran taraf
 * `null`i kendi baglamina gore yorumluyor.
 */
internal fun unitKind(unit: String): UnitKind? = KINDS[unit.lowercase()]

/**
 * [size] degerini [from] biriminden [to] birimine cevirir.
 *
 * `null` DONER: iki birim ayni seyi olcmuyorsa ya da biri taninmiyorsa.
 * Sifir donmuyor, cunku sifir bir CEVAP gibi okunur - `null` "cevabim yok"
 * diyor ve cagiran taraf onu ambalaji bilinmeyen satir gibi ele alabiliyor.
 */
internal fun convertMagnitude(size: Double, from: String, to: String): Double? {
    val fromKind = unitKind(from) ?: return null
    val toKind = unitKind(to) ?: return null
    if (fromKind != toKind) return null
    // SAYILANLAR ARASINDA DONUSUM YOK: paket ile kutu ayni seyin iki olcegi
    // degil, iki ayri sey. "3 paket" ile "3 kutu" arasinda bir kat yok.
    if (fromKind == UnitKind.COUNT) return if (from.lowercase() == to.lowercase()) size else null
    val f = FACTORS[from.lowercase()] ?: return null
    val t = FACTORS[to.lowercase()] ?: return null
    return size * f / t
}
