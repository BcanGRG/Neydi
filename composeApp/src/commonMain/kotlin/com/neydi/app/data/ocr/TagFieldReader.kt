package com.neydi.app.data.ocr

import com.neydi.app.data.normalizeUnit

/**
 * Etiketin sol kolonundan okunan urun kimligi.
 *
 * @param brand ad blogunun ILK satiri, yalnizca ONERI (tasarim karari 39).
 *   Manavda marka yok; `null` mesru bir cevap.
 * @param name kalan ad satirlari, bosluklarla birlesik.
 */
internal data class TagName(
    val brand: String?,
    val name: String,
)

/** Ambalaj boyu: `750 G` -> 750.0 + "gr". */
internal data class TagPack(
    val size: Double,
    val unit: String,
)

/**
 * Etiketin ad blogunu okur.
 *
 * ## Etiketin yerlesimi OLCULDU, varsayilmadi
 *
 * 27 gercek BIM etiketinin 25'inde duzen birebir ayni ve sol kolon **asagi
 * dogru** okunuyor:
 *
 * ```
 * ŞAFAK              <- marka       (kucuk)
 * PUDRA ŞEKERİ       <- ad          (biraz buyuk)
 * 250 G              <- GRAMAJ      (kucuk)  ... blogu BITIREN satir
 *                          26,  50t <- fiyat (dev, SAG kolonda)
 * P728 / F.D.Tarihi / KDV Dahil / YERLİ ÜRETİM
 * ```
 *
 * Gramaj satiri blogun sonunu isaretliyor ve bu tesadufi degil: etiketin
 * basiminda ad bloguyla alt kunye arasindaki tek ayrim o satir.
 *
 * ## Raf tabelasini eleyen KOLON suzgeci, yukseklik degil
 *
 * Genis cekimde rafin kendi tabelasi da kadraja giriyor - `Krena` (h=1032),
 * `Şekeňm` (h=927), `PUura` (h=724). Once bunlari "ad satirindan on kat buyuk"
 * diye yukseklikle elediigimi yazmistim; olcum yanlisladi. Tabela satirlari
 * etiketin **BUTUN genisligini** kapliyor: `Krena` x=132..3060, liranin x'i
 * 1979. Sag kenari fiyat kolonunu astigi icin `left` suzgecinden zaten
 * geciyorlar.
 *
 * Kural test isirmasiyla yakalandi: yukseklik esigini kaldirdim ve tabela
 * testleri AYAKTA kaldi - yani o esik tabelayi hic elemiyordu.
 *
 * ## BASARISIZ CEKIM lira boyuyla eleniyor
 *
 * `183808` bulanik ve OCR neredeyse hicbir sey okumamis: kose verisi bozuk,
 * yuksekliklerin cogu **negatif** (h=-1, -2). En buyuk glifi 12 piksel, oysa
 * saglam etiketlerde manset 447..697 piksel - kaynak yuksekligin %11'i ile
 * %17'si arasi. 12 piksel %0,3.
 *
 * Esik bu iki obek arasinda duruyor. Onceki halinde bu vaka *kazara* eleniyordu
 * (medyan yukseklik negatif cikiyor, carpim daha da negatif oluyor, her satir
 * dusuyordu) - dogru sonuc, tesadufi sebep.
 *
 * ## `groupVisualRows` KULLANILMIYOR ve bu bir plan sapmasi
 *
 * E14'un tarifi onu yeniden kullanmayi soyluyordu. Olcum gosterdi ki etiket
 * **kolonlu**, satirli degil: ad solda bir blok, fiyat sagda tek parca, kunye
 * altta. ML Kit satirlari zaten temiz geliyor ve gorsel satira toplamak
 * `KDV Dahil` ile `0401744- 15 F.D.Tarihi:`i birlestirmekten baska bir sey
 * yapmiyor - ayni y bandinda, ayri kolonlarda.
 *
 * Fiste gerekliydi cunku orada ad ile tutar AYNI gorsel satirin iki ucuydu ve
 * ML Kit'in dizilisi guvenilmezdi. Etikette o sorun yok.
 */
internal fun readTagName(ocr: TagOcr): TagName? {
    val lira = ocr.readableLira() ?: return null
    val priceX = lira.corners[0].x
    val left = ocr.lines
        .filter { it.corners[1].x < priceX && it.text.isNotBlank() }
        .sortedBy { it.corners[0].y }
    if (left.isEmpty()) return null

    val pack = left.lastOrNull { PACK.matches(it.text.trim()) && it.corners[0].y < lira.bottom() }
    val limit = pack?.corners?.get(0)?.y ?: lira.bottom()

    val block = left.filter {
        it.corners[0].y < limit &&
            it !== pack &&
            !it.text.isStoreCode() &&
            !it.text.looksLikeCount()
    }
    if (block.isEmpty()) return null

    // MARKA AD BLOGUNUN ILK SATIRI ve yalnizca oneri: 25 etikette ilk satir
    // gercekten marka (`ŞAFAK`, `ETI`, `SOFRA`, `CENTRO`, `SABAN`, `DAPHNE`).
    // Tek satirlik blokta marka YOK - o satirin kendisi ad, cunku bir seyi
    // hem marka hem ad saymak ikisini de bozar.
    // TEK HARFLIK ILK SATIR MARKA DEGIL: `184116`da ML Kit `TURŞU`nun bas harfini
    // ayri bir satir olarak kesmis (`U`). Marka bos kaliyor ve o parca ADA
    // katiliyor - uydurma bir marka, marka olmamasindan KOTU, cunku karar 26
    // satirin kimligini market+marka cifti yapiyor: yanlis marka kalici bir
    // ayrisma demek.
    val first = block.first().text.trim()
    val brandable = block.size >= 2 && first.length > 1
    val brand = if (brandable) first else null
    val nameLines = if (brandable) block.drop(1) else block
    return TagName(
        brand = brand,
        name = nameLines.joinToString(" ") { it.text.trim() },
    )
}

/**
 * Ambalaj boyunu okur: `750 G`, `1 KG`, `53-62 G`.
 *
 * ARALIK TASIYAN GRAMAJ (`53-62 G`, yumurta boyu) **null** donuyor: tek bir
 * sayiya indirmek gerekirdi ve hangisi dogru cevap belli degil. Etiket
 * gostersin diye ad blogunda kaliyor; birim fiyat hesabina girmemesi de
 * dogrusu - 53 g ile 62 g arasindaki fark birim fiyata dogrudan yansir.
 */
internal fun readTagPack(ocr: TagOcr): TagPack? {
    val lira = ocr.readableLira() ?: return null
    val priceX = lira.corners[0].x
    val line = ocr.lines
        .filter { it.corners[1].x < priceX && it.corners[0].y < lira.bottom() }
        .lastOrNull { PACK.matches(it.text.trim()) }
        ?: return null

    val t = line.text.trim()
    if (t.contains('-') || t.contains('–')) return null

    val m = PACK_PARTS.find(t) ?: return null
    val size = m.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
    // 3. GRUP birim: 1. sayi, 2. onun opsiyonel ondalik kuyrugu, 3. birim.
    return TagPack(size = size, unit = normalizeUnit(m.groupValues[3]))
}

/**
 * Gramaj satiri: SADECE sayi + birim, fazladan kelime YOK.
 *
 * Kelime yasagi bir gercek tuzaktan geliyor: `30'LU YUMURTA` gevsek bir desene
 * uyuyor ama o satir ADIN kendisi (otuzluk yumurta), gramaj degil - gercek
 * gramaj alt satirdaki `53-62 G`. Blogu yanlis yerden bitirmek adi yarim
 * keserdi.
 */
private val PACK = Regex(
    """\d+([.,]\d+)?(\s*[-–]\s*\d+)?\s*(G|GR|KG|ML|L|LT|CC)""",
    RegexOption.IGNORE_CASE,
)

private val PACK_PARTS = Regex("""(\d+([.,]\d+)?)\s*(G|GR|KG|ML|L|LT|CC)""", RegexOption.IGNORE_CASE)

/**
 * Mansetin okunabilir sayildigi tek yer.
 *
 * En buyuk glifli sayi-baslangicli satir, ama sadece **makul boyda** ise.
 * Boyut suzgeci olmadan bulanik bir etiket uydurma bir ad donduruyordu.
 *
 * Esik [MIN_LIRA_RATIO], `TagPriceReader` ile PAYLASILIYOR: iki okuyucunun ayni
 * fikstur icin farkli karar vermesi tutarsizlik olurdu.
 */
private fun TagOcr.readableLira(): OcrPiece? = lines
    .filter { it.text.trimStart().firstOrNull()?.isDigit() == true }
    .maxByOrNull { it.height() }
    ?.takeIf { it.height() >= sourceHeight * MIN_LIRA_RATIO }

private fun OcrPiece.height(): Int = if (corners.size < 4) 0 else corners[3].y - corners[0].y

private fun OcrPiece.bottom(): Int = if (corners.size < 4) 0 else corners[3].y

/**
 * Magaza/yazici kodu mu (`P728`)?
 *
 * 27 etiketin HEPSINDE var ve hicbirinde urun adinin parcasi degil. Ad blogunun
 * icine sizdigi bir vaka olculdu (`QUEEN / PARF.TUV.KAGIDI 3 KATLI 12Li P728`),
 * cunku o etikette kod ad blogunun hizasinda basilmis.
 */
private fun String.isStoreCode(): Boolean = Regex("""^P\d{2,4}$""").matches(trim())

/**
 * Raf adedi (`X 34 Adet`) mi?
 *
 * BIM etiketinin ustune rafta kalan adet elle/ayri basiliyor ve ad blogunun
 * hizasina dusuyor. Urun kimligi TASIMIYOR, dolayisiyla ne marka ne ad.
 *
 * ONDEKI `X` SART. Once `X`siz sayilari da eledim ve desen `12Lİ`yi yedi -
 * oysa o `PARF.TUV.KAĞIDI 3 KATLI 12Lİ`nin parcasi, koli adedi degil. Paket
 * carpani ADIN kendisi; raf adedini ondan ayiran tek isaret bastaki `X`.
 */
private fun String.looksLikeCount(): Boolean =
    Regex("""^[Xx]\s*\d+\s*(Adet|ADET|adet)?$""").matches(trim())
