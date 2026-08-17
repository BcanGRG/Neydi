package com.neydi.app.data.ocr

import com.neydi.app.data.parseMinor

/**
 * Raf etiketinden okunan fiyat.
 *
 * @param minor kurus cinsinden tutar. Kurus okunamadiysa lira x 100.
 * @param kurusFromOcr **false ise kurus TAHMIN, olcum degil** - `,00` sayildi.
 *   Onay karti bu bayraga bakip fiyat alanina odaklanacak (E15): kullanicidan
 *   iki hane istemek, yanlis iki haneyi sessizce kaydetmekten iyi.
 */
internal data class TagPrice(
    val minor: Long,
    val kurusFromOcr: Boolean,
)

/**
 * Etiketin birim fiyat satiri: "223,09 / kg".
 *
 * ETIKETTEKI EN TEMIZ SAYI (E12 olcumu, 27 etiketin 10'unda iki ondalik hanesi
 * tam okundu) cunku NORMAL PUNTODA basiliyor - manset fiyat dev punto + kucuk
 * ustsimge. Bir etikette manset fiyata esit cikti, yani manseti dogrulayabilir.
 */
internal data class TagUnitPrice(
    val minor: Long,
    val unit: String,
)

/**
 * Etiket fiyatini okur - `parseMinor` KULLANMADAN, ve sebebi olculdu.
 *
 * ## Neden ayri bir okuyucu
 *
 * `parseMinor` **tam iki ondalik hane** sart kosuyor ve bu bilincli bir kural:
 * fiste iki hanesi olmayan sayi fiyat degil, miktar ya da barkod parcasidir.
 * Raf etiketi o kurali karsilamiyor - E12'de olculdu, **27 gercek BIM
 * etiketinin hicbirinin** manset fiyati `parseMinor`dan gecmiyor:
 *
 * - Lira ayiriciyla BITISIK geliyor: `74,` `19,` `33,` `59,` `53,`
 * - Kurus AYRI bir parca ve ustune ₺ simgesi yapisiyor: `50t` `90%` `75t`
 *   `501` `50%` - simge `t`, `%`, `1`, `:` diye okunuyor
 * - Bircok etikette kurus HIC yok: `149` `60` `34` `219`
 *
 * `parseMinor`i gevsetmek yerine ayri bir okuyucu yazildi cunku o kural fisin
 * kendi dogrusu ve orada hala gecerli; iki farkli yuzeyin iki farkli
 * dogrusunu tek fonksiyona bindirmek ikisini de zayiflatirdi.
 *
 * ## Fiyat neden "en buyuk glif" ile bulunuyor ama "para deseni" ile degil
 *
 * E14'un ilk tarifi *"para desenine uyan parcalar arasindan en buyuk glifli"*
 * diyordu. Olcum gosterdi ki o suzgec kumeyi ONCE bosaltiyor - hicbir parca
 * para desenine uymuyor. O yuzden aday kumesi **rakamla baslayan** parcalar.
 *
 * Bu ayni zamanda ikinci bir olcum sorununu cozuyor: 27 etiketin 6'sinda en
 * buyuk glif fiyat DEGIL, marka adi (`Kar` 1244px, `Krena` 1032px - aktuel
 * etiketlerde marka fiyattan buyuk basiliyor). "Rakamla baslar" sarti onlari
 * kendiliginden eliyor.
 */
internal fun readTagPrice(ocr: TagOcr): TagPrice? {
    val lira = ocr.lines
        .filter { it.text.trimStart().firstOrNull()?.isDigit() == true }
        .maxByOrNull { it.glyphHeight() }
        ?: return null

    val liraDigits = lira.text.trim().takeWhile { it.isDigit() }
    if (liraDigits.isEmpty()) return null
    val liraValue = liraDigits.toLongOrNull() ?: return null

    val kurus = ocr.lines.firstOrNull { it.isKurusFor(lira) }
    val kurusValue = kurus?.text?.trim()?.take(2)?.toLongOrNull()

    return TagPrice(
        minor = liraValue * 100 + (kurusValue ?: 0),
        kurusFromOcr = kurusValue != null,
    )
}

/**
 * Kurus parcasi mi?
 *
 * ## Bicim: IKI RAKAM + en fazla bir cop karakter
 *
 * Olculen butun kurus parcalari boyle: `50t` `90%` `75t` `501` `50%`. Sondaki
 * karakter ₺ simgesinin yanlis okunmasi - `t`, `%`, hatta `1` (yani RAKAM)
 * olabiliyor, o yuzden "rakam olmayan tek karakter" demek yetmiyor.
 *
 * AYIRICI TASIYAN ADAY REDDEDILIYOR (`,` `.` `:`) ve bu kurali iki gercek tuzak
 * yazdirdi:
 * - `89,s6` - **eski (ustu cizili) fiyat**, kurustan BUYUK glifli ve tam o
 *   bantta duruyor. Virgul tasidigi icin duser.
 * - `82:` - saat parcasi (`16:01:36` gibi damgalar etikette var).
 *
 * ## Konum: liranin sag yarisinin disinda, dikey olarak onun bandinda
 *
 * Ustsimge sag ustte ama tam hizalanmiyor; `184225`'te lira y=2529 iken kurus
 * y=2701, yani liranin ORTASINDA. Bant bu yuzden genis: `-0.5h` ile `+0.7h`.
 *
 * ## Boyut: liranin %60'indan kucuk
 *
 * Olcumde oran hep 1/4 ile 1/7 arasindaydi (`697/200`, `457/101`, `548/123`).
 * %60 bol bir ust sinir; daha siki bir esik eski fiyati elemek icin
 * gerekmiyordu, onu ayirici kurali zaten eliyor.
 */
private fun OcrPiece.isKurusFor(lira: OcrPiece): Boolean {
    val t = text.trim()
    if (t.length !in 2..3) return false
    if (!t[0].isDigit() || !t[1].isDigit()) return false
    if (t.any { it == ',' || it == '.' || it == ':' }) return false

    val lh = lira.glyphHeight()
    if (lh <= 0 || glyphHeight() >= lh * 0.6) return false

    val lx = lira.corners[0].x
    val lw = lira.corners[1].x - lira.corners[0].x
    val ly = lira.corners[0].y
    return corners[0].x > lx + lw * 0.4 &&
        corners[0].y > ly - lh * 0.5 &&
        corners[0].y < ly + lh * 0.7
}

/**
 * Birim fiyat satiri - `parseMinor`in CALISTIGI yer.
 *
 * Iki ondalik hane burada gercekten var (normal punto), o yuzden ayri bir
 * ayristirici gerekmiyor. Bastaki/sondaki cop temizleniyor: `T06,00 kg`,
 * `E7,50hg`, `239,44B | kg` - para birimi simgesi metne siziyor.
 */
internal fun readTagUnitPrice(ocr: TagOcr): TagUnitPrice? {
    UNITS.forEach { unit ->
        ocr.lines.forEach { piece ->
            val t = piece.text
            if (!t.contains(unit, ignoreCase = true)) return@forEach
            val m = MONEY.find(t) ?: return@forEach
            val minor = parseMinor(m.value) ?: return@forEach
            return TagUnitPrice(minor = minor, unit = unit)
        }
    }
    return null
}

/** Etikette gorulen birim sozcukleri. `itre`/`ikg` OCR copu, olculdu. */
private val UNITS = listOf("kg", "adet", "litre", "lt", "itre", "ikg", "hg")

private val MONEY = Regex("""\d{1,4}[.,]\d{2}""")

/** Kose noktalarindan glif yuksekligi - `[0] -> [3]` kenari. */
private fun OcrPiece.glyphHeight(): Int =
    if (corners.size < 4) 0 else corners[3].y - corners[0].y
