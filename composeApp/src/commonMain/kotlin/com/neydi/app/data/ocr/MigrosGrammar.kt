package com.neydi.app.data.ocr

import com.neydi.app.data.normalizeUnit

/**
 * Migros raf etiketi grameri - 19 gercek etiketten olculdu.
 *
 * ## Manset iki parca, ve KURUS PARCASI VIRGULU TASIYOR
 *
 * ```
 * 44      h=692   <- lira
 *   ,95   h=368   <- kurus, VIRGULLE birlikte, sagda ve kucuk
 * ```
 *
 * BIM'in kurus kurali bunu REDDEDIYOR: orada ayirici tasiyan aday **eski
 * (ustu cizili) fiyat** demekti ve elenmesi gerekiyordu. Migros'ta ayni bicim
 * dogru cevabin ta kendisi. Iki zincirin kurallarinin neden ayri dosyalarda
 * durdugunun en net ornegi bu.
 *
 * ## Kurus bazen liraya YAPISIYOR
 *
 * `22390` (223,90), `2750` (27,50), `14295` (142,95) - ustsimge OCR'da lirayla
 * tek parca oluyor. Dort ya da daha cok haneli tek parcada son iki hane kurus.
 *
 * ## MANAV ETIKETI: manset ile birim fiyat AYNI SAYI
 *
 * Kiloyla satilan urunde etiketin dev rakami zaten kilo fiyati; `BIRIM FIYAT:
 * 43,95 TL/KG` satiri onu ikinci kez basiyor. Ikisi ayni sayi oldugu icin
 * **daha temiz basilani** okunuyor: `211114`te manset `4389` diye yapismis
 * (son hane 5 yerine 9), birim fiyat satiri ise `43,95` diye tertemiz.
 *
 * Sart siki: ambalaj boyu YOKSA ve iki sayi %10 icinde uyusuyorsa. Uyusmuyorsa
 * hangisinin dogru oldugu bilinmiyor demektir ve hicbir sey donmuyor.
 *
 * ## Ad okunabilirlik kapisi
 *
 * `TUDUMrCiCKAG`, `YUDUIi AYLILCNIAU CL` - OCR bazen hicbir seyi dogru
 * okumuyor. Bunlari urun adi diye dondurmek karar 26'ya gore KALICI hayalet
 * urunler yaratirdi (kimlik market+marka cifti). Sesli harf orani dusuk ya da
 * cok kisa adlar reddediliyor; kullanici adi kendisi yaziyor.
 */
internal object MigrosGrammar : TagGrammar {

    override fun readPrice(ocr: TagOcr): TagPrice? {
        val head = headline(ocr) ?: return null
        val unitPrice = readTagUnitPrice(ocr)

        // MANAV YOLU: ambalaj yok + birim fiyat var + iki sayi uyusuyor.
        if (readPack(ocr) == null && unitPrice != null) {
            val raw = separateMinor(ocr, head) ?: fusedMinor(head)
            if (raw != null && raw.toDouble() / unitPrice.minor in 0.90..1.10) {
                return TagPrice(minor = unitPrice.minor, kurusFromOcr = true)
            }
        }

        val separate = separateMinor(ocr, head)
        if (separate != null) return TagPrice(minor = separate, kurusFromOcr = true)
        val fused = fusedMinor(head) ?: return null
        return TagPrice(minor = fused, kurusFromOcr = true)
    }

    /**
     * Hangi sayi MANSET - ve iki fiyatli etikette hangisi ALINMAZ.
     *
     * Migros ayni karta iki fiyat basiyor ve her birinin USTUNDE kendi basligi
     * duruyor:
     *
     * ```
     * NORMAL SATIS FIYATI   h=49  x=225   y=2488
     * 22390                 h=146 x=476   y=2567   <- 79px asagisinda
     * MONEYLI FIYAT         h=49  x=1346  y=2466
     * 6490                  h=191 x=1323  y=2552
     * ```
     *
     * MONEY FIYATI RAF FIYATI DEGIL: etiketin kendisi *"MONEY KARTLAR ILE
     * GECERLIDIR"* yaziyor. Ustelik daha BUYUK basiliyor (191 > 146), yani
     * "en buyuk glif" kurali tam da yanlis olani seciyor - `211430`da 169,95
     * okuyordu, gercegi 379,95.
     *
     * Money basligi varsa NORMAL baslikli aday araniyor; bulunamazsa hicbir
     * sey donmuyor. Kartin olup olmadigini bilmeden iki fiyattan birini secmek
     * tahmin olurdu.
     */
    private fun headline(ocr: TagOcr): OcrPiece? {
        val default = ocr.readableLira() ?: return null
        val moneyCaption = ocr.lines.any { MONEY_CAPTION in fold(it.text) }
        if (!moneyCaption) return default

        val normal = ocr.lines.filter { NORMAL_CAPTION in fold(it.text) }
        if (normal.isEmpty()) return null
        return ocr.lines
            .filter { p -> p.text.trim().let { t -> t.isNotEmpty() && t.all { c -> c.isDigit() } } }
            .filter { p -> normal.any { it.captions(p) } }
            .maxByOrNull { it.glyphHeight() }
    }

    /**
     * Ayri kurus parcasi: `,95` ya da `95`, mansetin sagi ve bandinda.
     *
     * Olculen boy oranlari 0,53 .. 0,60 - BIM'in %60 ust sinirinin TAM
     * sinirinda. Burada ust sinir %75, cunku Migros kurusu orantili olarak cok
     * daha buyuk basiliyor.
     */
    private fun separateMinor(ocr: TagOcr, head: OcrPiece): Long? {
        val lira = head.text.trim().filter { it.isDigit() }
        if (lira.isEmpty() || lira.length > 4) return null
        val hh = head.glyphHeight()
        if (hh <= 0) return null
        val piece = ocr.lines.firstOrNull { p ->
            if (p === head) return@firstOrNull false
            if (!KURUS.matches(p.text.trim())) return@firstOrNull false
            val ratio = p.glyphHeight().toDouble() / hh
            if (ratio !in 0.20..0.75) return@firstOrNull false
            p.corners[0].x > head.corners[0].x + (head.corners[1].x - head.corners[0].x) * 0.4 &&
                p.corners[0].y > head.corners[0].y - hh * 0.4 &&
                p.corners[0].y < head.corners[0].y + hh * 0.6
        } ?: return null
        val kurus = piece.text.trim().filter { it.isDigit() }
        if (kurus.length != 2) return null
        return lira.toLong() * 100 + kurus.toLong()
    }

    /** Yapisik manset: `22390` -> 223,90. Son iki hane kurus. */
    private fun fusedMinor(head: OcrPiece): Long? {
        val digits = head.text.trim().filter { it.isDigit() }
        if (digits.length !in 4..7) return null
        return digits.toLongOrNull()
    }

    /**
     * AD OKUNMUYOR - ve bu bir eksiklik degil, olculmus bir ret.
     *
     * Denendi: kunye sozlugu, boy esigi, sesli harf orani. 19 etikette
     * ciktilar `NUIK KREMIASI orunun lot ve kapakta yazilidir`,
     * `Metket Edime OSBFbr Isletne Net Ab`, `TO00ge` gibi seyler oldu.
     *
     * Sebep yapisal: Migros fotograflarinin cogunda urunun KENDI AMBALAJI da
     * kadrajda ve ambalaj yazisi etiketin ad satirindan buyuk basiliyor. Boyla
     * ayirmak bu yuzden calismiyor - dogru sinyal metnin ANLAMI, geometri
     * degil.
     *
     * Yarisi dogru bir ad, hic ad olmamasindan KOTU: karar 26 urun kimligini
     * kalici sayiyor, yani `TO00ge` diye bir hayalet urun yaratilir ve
     * kullanici bunu fark etmez. Fiyat alani doluyor, ad alanini kullanici
     * yaziyor - `resolveProduct` yazdigini katalogla zaten eslestiriyor.
     *
     * Fiyat tarafi ayni fiksturlerde 17/19 dogru ve **sifir yanlis**; ikisini
     * ayirmak bu yuzden mumkun.
     */
    override fun readName(ocr: TagOcr): TagName? = null

    override fun readPack(ocr: TagOcr): TagPack? {
        // ADET CARPANI ONCE denenir: `30 LU YUMURTA` bir litre olcusu degil.
        ocr.lines.forEach { p ->
            val t = p.text.trim()
            if (isNoise(t)) return@forEach
            COUNT.find(t)?.let { m ->
                m.groupValues[1].toDoubleOrNull()?.let { return TagPack(size = it, unit = "adet") }
            }
        }
        ocr.lines.forEach { p ->
            val t = p.text.trim()
            if (isNoise(t)) return@forEach
            // TOPLAMALI AMBALAJ REDDEDILIYOR (`500 + 250 ML`): iki sayiyi
            // toplamak da birini secmek de tahmin olurdu.
            if (t.contains('+')) return@forEach
            PACK.find(t)?.let { m ->
                m.groupValues[1].replace(',', '.').toDoubleOrNull()?.let {
                    return TagPack(size = it, unit = normalizeUnit(m.groupValues[3]))
                }
            }
        }
        return null
    }

    /**
     * Etiketin kendi kunyesi - urun adi DEGIL.
     *
     * ASCII'YE KATLANARAK karsilastiriliyor: OCR `MENŞE` yerine `MENSE`,
     * `FIYAT` yerine `FIVAT`/`FNAT` uretiyor ve tam eslesme bunlari kaciriyordu.
     */
    private fun isNoise(text: String): Boolean {
        val f = fold(text)
        return NOISE.any { f.contains(it) } || f.count { it.isDigit() } > f.length / 2
    }

    /**
     * Bu bir sozcuk mu, yoksa OCR gurultusu mu?
     *
     * Sesli harf orani kaba bir olcut ama olculen uc vakayi da yakaliyor.
     * Yanlis bir ad, ad olmamasindan KOTU: kullanici bos alani doldurur,
     * hayalet urunu fark etmez - ve karar 26'da o kimlik kalicidir.
     */
    private fun readable(name: String): Boolean {
        val letters = name.filter { it.isLetter() }
        if (letters.length < 4) return false
        val vowels = letters.count { fold(it.toString()).first() in "AEIOU" }
        return vowels.toDouble() / letters.length >= 0.20
    }
}

private fun fold(s: String): String = s.uppercase().map {
    when (it) {
        'Ş' -> 'S'
        'İ' -> 'I'
        'Ğ' -> 'G'
        'Ü' -> 'U'
        'Ö' -> 'O'
        'Ç' -> 'C'
        else -> it
    }
}.joinToString("")

private val NOISE = listOf(
    "MIGROS", "MENSE", "ULKE", "KDV", "DAHIL", "BIRIM", "FIYAT", "FIVAT", "FNAT",
    "TARIH", "YERLI", "URETIM", "ORETIM", "TARIM", "INDIRIM", "SATIS", "NORMAL",
    "STOK", "ADEDI", "MIKTAR", "ALIS", "MONEY", "KART", "SERTIFIKA", "ISLETME", "MUHAFAZA", "TUKETIM",
)

/** Kurus parcasi: `,95` ya da `95` - virgul BURADA mesru. */
private val KURUS = Regex("""[.,]?\d{2}""")

/**
 * Adet carpani: `30 LU`, `30'LU`, `32 ADET`.
 *
 * TURKCE HARFLER SINIFA ELLE YAZILDI. Ilk hali `L[UI]` idi ve `30 Lİ`yi
 * kaciriyordu; kacirinca asagidaki [PACK] deseni ayni satiri `30 L` diye
 * okuyor, yani otuz yumurtayi **otuz litre** yapiyordu. ASCII varsayimi
 * Turkce metinde sessiz bir hata kaynagi.
 */
private val COUNT = Regex("""(\d{1,3})\s*['’]?\s*(?:L[UÜİIı]|ADET)""", RegexOption.IGNORE_CASE)

/** Ambalaj boyu - adet carpanindan SONRA denenir. */
private val PACK = Regex(
    """(\d+([.,]\d+)?)\s*(KG|GR|ML|LT|CC|G|L)(?![A-Za-zÇĞİÖŞÜçğıöşü])""",
    RegexOption.IGNORE_CASE,
)

/**
 * Bu satir [price] parcasinin BASLIGI mi - hemen ustunde ve ayni kolonda mi?
 *
 * Olculen mesafe 75..90 piksel; esik fiyatin kendi boyuna baglaniyor ki
 * cozunurluge gore kaymasin.
 */
private fun OcrPiece.captions(price: OcrPiece): Boolean {
    val gap = price.corners[0].y - bottomY()
    if (gap < 0 || gap > price.glyphHeight()) return false
    return corners[0].x < price.corners[1].x && corners[1].x > price.corners[0].x
}

private const val MONEY_CAPTION = "MONEY"
private const val NORMAL_CAPTION = "NORMAL SATIS"

/** Ad satiri, blogun en buyuk satirinin bu kadarindan kucuk olamaz. */
private const val NAME_HEIGHT_FLOOR = 0.55
