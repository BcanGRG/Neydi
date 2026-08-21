package com.neydi.app.data.ocr

import kotlin.math.abs

/**
 * A101 raf etiketi grameri - 19 gercek etiketten olculdu (21 Agustos 2026).
 *
 * ## Olcumun kaynagi farkli, YONTEMI ayni
 *
 * Bu on dokuz kare uygulamanin kendi cekimiyle degil, TELEFONUN KAMERASIYLA
 * cekildi: o gun uygulama her karede yanlis bir urun adi onerdigi icin
 * kullanici turu yarida birakti. Fotograflar sonradan `dumpImportedPhotos` ile
 * ayni ML Kit yolundan gecirildi, yani buradaki her sayi yine olculdu.
 *
 * ## KURUS BURADA AYRI PARCA DEGIL - BIM'in kurali A101'de calismaz
 *
 * BIM'de lira ve kurus iki ayri parca gelir ve [readTagPrice] bunun uzerine
 * kurulu. A101'de kurus **bes ayri sekilde** geliyor ve on dokuz karenin
 * yalnizca ucunde ayri bir parca:
 *
 * | Sekil | Olculen | Kural |
 * |---|---|---|
 * | Parcanin icinde tam para | `19.75`, `99,50` | 1 |
 * | Ayri parca | `43`+`50`, `439`+`,50`, `595`+`50` | 2 |
 * | Liraya YAPISIK | `6450`, `6490`, `11650`, `43-50`, `1585o` | 4 |
 * | Etikette YOK (tam lira) | `188.`, `95`, `135`, `105.`, `169.`, `249.`, `265,` | 5 |
 *
 * Tek bir kurala sigmiyorlar, sirali deneniyorlar. Siralama keyfi degil: en
 * ustte metnin KENDI icinde tam bir para bulundugu hal duruyor, cunku iki
 * haneli ondalik ayiricinin tasidigi kanit hicbir geometrik ipucuyla
 * yarismiyor.
 *
 * ## VIRGUL FIYAT BOYUNDA ve TABAN CIZGISINDE - butun kusurlarin kaynagi
 *
 * `133227`nin fotografi bakildi ve etiketin dizgisi su: `43` normal boyda,
 * hemen ardindan **liranin kendisi kadar buyuk** bir virgul taban cizgisinde,
 * sonra `50` yarim boyda USTSIMGE, sonra `TL`.
 *
 * O buyuk virgul her seyi acikliyor. Ayni etiketin uc cekimi uc ayri sey
 * okuttu:
 *
 * ```
 * 133220   439    h=546 x=1603..2432    ,50  h=241 x=2209   <- virgul 9 okundu
 * 133226   43-50  h=429 x=1348..2532    (yapisik)           <- virgul tire okundu
 * 133227   43     h=525 x=1444..2016     50  h=229 x=2079   <- virgul dustu
 * ```
 *
 * Bu uclu grameri tek basina yazdirdi. `133220`de OCR VIRGULU LIRAYA BIR 9
 * DIYE YAPISTIRMIS - ve BIM'in kurali ayirici tasiyan kurus adayini
 * *reddettigi* icin (orada ayirici "ustu cizili eski fiyat" demekti) dogru
 * cevabi ceviye atardi.
 *
 * ## Yapisan glifi GEOMETRI ayikliyor, metin degil
 *
 * `439`+`,50` ve `595`+`50` ayni kusur: kurusun ilk glifi mansetin kutusuna
 * girmis. Kanit kutularin kendisinde - mansetin SAG kenari kurusun SOL
 * kenarini geciyor:
 *
 * ```
 * 133220  manset 1603..2432, kurus 2209  ->  223px BINDIRME  -> son hane atilir
 * 133322  manset 1557..2320, kurus 2125  ->  195px BINDIRME  -> son hane atilir
 * 133227  manset 1444..2016, kurus 2079  ->   63px BOSLUK    -> hane korunur
 * ```
 *
 * Bindirme her iki vakada da bir glif genisligine denk (276 ve 254 piksel/hane).
 * Metne bakarak ayirt edilemezdi: `439`un son hanesi 9, kurusun ilk hanesi 5.
 *
 * ## KURUS SIFIRSA A101 ONU HIC BASMIYOR - bu bir OCR kusuru DEGIL
 *
 * Dokuz karede kurus parcasi hic cikmadi. Ilk aciklamam "ustsimge sifirlar ML
 * Kit'e gorunmuyor" idi ve YANLISTI: uc etiketin fotografina bakildi ve
 * ucunde de etikette kurus BASILI DEGIL - `265 TL`, `249 TL`, `105 TL`.
 * Tabelanin dizgisi boyle; okunacak bir sey yoktu.
 *
 * Dolayisiyla kurus parcasi yoksa fiyat TAM LIRA ve `kurusFromOcr = true`.
 * Dayanak sayilarla:
 *
 * - Kurusu sifir olan 9 karenin 9'u capraz kontrolle dogrulandi
 *   (`1.253,33 x 150 G = 188,00`, `52,50 x 2 KG = 105,00`, `5,63 x 30 = 169,00`, ...),
 *   ucu ayrica fotograftan.
 * - Kurusu sifir OLMAYAN 10 karenin 10'unda kurus okundu - biri bile
 *   kacmadi. Sasirtici degil: virgul liranin boyunda, kurus yarim boyda.
 *   Bu dizgi sessizce gozden kacacak bir isaret degil.
 *
 * `kurusFromOcr = false` daha temkinli GORUNURDU ama degil: kart o zaman
 * A101 etiketlerinin YARISINDA *"Kurus okunamadi - kontrol et"* derdi,
 * kullanici her seferinde bakip kontrol edecek bir sey bulamazdi ve uyari
 * gercekten onemli oldugu yerde anlamini yitirirdi.
 *
 * Kalan risk aciktir ve capraz kontrol bunu YAKALAMAZ: OCR basili bir `50`yi
 * kacirirsa 43,00 yazilir, oran %1,15 sapar ve %2 toleransin icinde kalir.
 * Iddia [A101GrammarTest.aPrintedKurusIsNeverMissed] ile kilitli - on dokuz
 * olcumde sifir karsi ornek. Karsi ornek cikan gun kirilacak test o.
 */
internal object A101Grammar : TagGrammar {

    override fun readPrice(ocr: TagOcr): TagPrice? {
        val head = ocr.readableLira() ?: return null
        val raw = head.text.trim().zeroFolded()
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty() || digits.length > MAX_PRICE_DIGITS) return null

        // 1) PARCANIN ICINDE tam bir para: `19.75`, `99,50`.
        MONEY.find(raw)?.let { m ->
            moneyMinor(m.value)?.let { return TagPrice(minor = it, kurusFromOcr = true) }
        }

        // 2) AYRI kurus parcasi - yapisan glif varsa ayiklanarak.
        minorPiece(ocr, head)?.let { piece ->
            val lira = liraDigits(head, piece, digits).toLongOrNull()
            val kurus = piece.text.filter { it.isDigit() }.toLongOrNull()
            if (lira != null && kurus != null) {
                return TagPrice(minor = lira * 100 + kurus, kurusFromOcr = true)
            }
        }

        // 3) BINLIK AYIRICI var ama kurus yok: hepsi lira (bkz. [THOUSANDS]).
        if (THOUSANDS.containsMatchIn(raw)) {
            return digits.toLongOrNull()?.let { TagPrice(minor = it * 100, kurusFromOcr = false) }
        }

        // 4) YAPISIK: son iki hane kurus.
        if (digits.length >= FUSED_MIN_DIGITS) {
            return digits.toLongOrNull()?.let { TagPrice(minor = it, kurusFromOcr = true) }
        }

        // 5) LIRA TEK BASINA: etikette kurus BASILI DEGIL, fiyat tam lira.
        return digits.toLongOrNull()?.let { TagPrice(minor = it * 100, kurusFromOcr = true) }
    }

    /**
     * BIM'in ad okuyucusu, AMA once A101'in kunye kodu ayikliniyor.
     *
     * Iki duzen de KOLONLU - ad solda bir blok, fiyat sagda - ve `readTagName`
     * A101'de de dogru bloga denk geliyor. Devretmek bu yuzden dogru; ayri bir
     * govde yazmak ayni kurali iki dosyada tutmak ve bir gun sessizce
     * ayrismalarina izin vermek olurdu.
     *
     * DEVIR TEK BASINA YETMEDI ve bunu olcum soyledi. Ilk hali dogrudan
     * `readTagName(ocr)` idi; on dokuz adin ON DOKUZU da kunye koduyla
     * basliyordu:
     *
     * ```
     * 0430 2605091327 BIRSAH YARIM YAGLI YOGURT
     * 0430 26022 10908-Z520 ULKER ALBENI BAR CIKOLATA KARAMELLI
     * ```
     *
     * BIM'in `isStoreCode` suzgeci yalnizca `P728` bicimini taniyor - A101
     * bambaska bir sey basiyor. Kod ad blogunun ILK satiri oldugu icin ikinci
     * bir zarar daha veriyordu: marka o satirdan secilmeye calisiliyor,
     * rakamli oldugu icin eleniyor ve blogun TAMAMI ad sayiliyordu.
     *
     * ## MARKA DONDURULMUYOR - olculdu ve guvenilmez cikti
     *
     * Kod ayiklandiktan sonra marka kutusu doldu ve sekizi dogru geldi (`LAYS`,
     * `TUKAS`, `PETEK`, `BIRSAH`, `NIMET`, ...). Ama ALTISI yanlisti:
     *
     * ```
     * VEGAN                     <- urunun AMBALAJINDAN, etiketten degil
     * AMET                      <- `NAMET`in bas harfi dusmus
     * ada                       <- gurultu
     * FIYAT GECERLILIK TARIHI   <- etiketin kendi kunyesi
     * Urt. yeri:Turkiye         <- etiketin kendi kunyesi
     * ```
     *
     * Sebep yapisal ve duzeltilemez: A101 fotograflarinin cogunda urunun KENDI
     * AMBALAJI da kadrajda ve ambalaj yazisi etiketin ad satiriyla ayni
     * kolonda. `VEGAN` ile `LAYS`i BICIMDEN ayirt etmenin yolu yok - ikisi de
     * bes harfli, sesli tasiyan, buyuk harfli sozcukler.
     *
     * Karar 26 satirin kimligini market+marka cifti yapiyor: yanlis marka
     * KALICI bir ayrisma, ayni urun ikinci cekimde baska bir satira duser.
     * Karar 39 ise markayi yalnizca ONERI sayiyor ve `null`u mesru cevap
     * kabul ediyor - suphede kalmak bu yuzden ucuz. Kullanici markayi karar
     * 52'nin cip sheet'inden seciyor.
     *
     * Okunan marka YINE DE ATILMIYOR: ipucu metnine geri katiliyor, cunku
     * *"Etiket metni: LAYS PATATES CIPSI"* kullaniciya *"PATATES CIPSI"*den
     * daha cok sey soyluyor. Ipucu kalici degil, kimlik kalici.
     */
    override fun readName(ocr: TagOcr): TagName? =
        readTagName(ocr.withoutArticleCodes())?.let { read ->
            TagName(brand = null, name = listOfNotNull(read.brand, read.name).joinToString(" "))
        }

    /**
     * Gramaj BIM'in okuyucusundan - olculdu ve DOKUNULMASI gerekmedi.
     *
     * On dokuz karenin on sekizinde dogru (`125 G`, `2 KG`, `1L`, `50 ML`),
     * kalan biri yumurta etiketi ve orada `53-62 G` araligi BILEREK
     * reddediliyor. A101 da BIM gibi gramaji KENDI SATIRINA basiyor; Migros'un
     * ad satirinin icine gomme aliskanligi burada yok.
     */
    override fun readPack(ocr: TagOcr): TagPack? = readTagPack(ocr)

    /**
     * Mansetin sagindaki, ustune hizali, kucuk iki haneli parca.
     *
     * Uc olcum de dar: boy orani 0,44 / 0,44 / 0,47 ve dikey kayma mansetin
     * boyunun %4'unden az (-4, +19, +17 piksel; boylar 546, 525, 419). A101
     * kurusu USTSIMGE basiyor, yani lira ile tepeden hizali.
     *
     * Bant BIM'inkinden (`-0,5h .. +0,7h`) cok daha dar ve bu bilincli: orada
     * bir fikstur kurusu liranin ORTASINDA tasiyordu, burada uc olcumun ucu de
     * tepede. Dar bant `TL` parcasini ve tarih kirintilarini kurus sanmayi
     * engelliyor.
     */
    private fun minorPiece(ocr: TagOcr, head: OcrPiece): OcrPiece? {
        val hh = head.glyphHeight()
        if (hh <= 0) return null
        val headWidth = head.corners[1].x - head.corners[0].x
        return ocr.lines.firstOrNull { p ->
            p !== head &&
                p.glyphHeight().toDouble() / hh in MINOR_HEIGHT_RANGE &&
                MINOR.matches(p.text.trim()) &&
                p.corners[0].x > head.corners[0].x + headWidth * 0.4 &&
                abs(p.corners[0].y - head.corners[0].y) < hh * MINOR_BAND
        }
    }

    /** Manset kutusu kurusun soluna tasiyorsa son hane kurusun glifi - atilir. */
    private fun liraDigits(head: OcrPiece, minor: OcrPiece, digits: String): String {
        val overlaps = head.corners[1].x > minor.corners[0].x
        return if (overlaps && digits.length > 1) digits.dropLast(1) else digits
    }
}

/**
 * A101'in kunye kodu satirlarini atar.
 *
 * Kod her etikette var ve ad blogunun ustunde duruyor: `0430 2605091327`,
 * `0430_2608021225`, `0430260R190943`, `0430 26O6231735-Z520-Z5`. Bicim
 * degisken - bosluk, alt cizgi, tire, araya karisan harfler - ama HEPSI yariyi
 * gecen oranda rakam.
 *
 * `0430` ONEKI KULLANILMADI: o A101'in magaza numarasi ve baska bir subede
 * baskadir. Suzgec bicime degil orana bakiyor.
 *
 * SEKIZ KARAKTER ALT SINIRI MANSETI KORUYOR: fiyat parcalari da rakam agirlikli
 * (`6450`, `11650`, `1585o`) ve ayni suzgecten dusselerdi ad okuyucusu fiyati
 * bulamazdi. Olculen en uzun manset bes karakter, en kisa kunye kodu on -
 * esik ikisinin arasinda ve iki obek de sinirdan uzak.
 */
private fun TagOcr.withoutArticleCodes(): TagOcr =
    copy(lines = lines.filterNot { it.text.trim().isArticleCode() })

private fun String.isArticleCode(): Boolean =
    length >= ARTICLE_CODE_MIN_LENGTH && count { it.isDigit() } * 2 > length

/** Kunye kodu bu uzunluktan kisa olmuyor; manset ise bu kadar uzun olmuyor. */
private const val ARTICLE_CODE_MIN_LENGTH = 8

/**
 * `o` ve `O` sifira katlanir - yalnizca manset parcasinda.
 *
 * `133211`de 158,50 TL `1585o` diye geldi: sondaki sifir `o` okundu. Katlama
 * olmadan hane dizisi `1585` olur ve fiyat 15,85 diye yazilirdi - on kat
 * yanlis. Manset baglaminda harf zaten olamaz, o yuzden katlama burada
 * guvenli; ada ya da gramaja uygulanmiyor.
 */
private fun String.zeroFolded(): String =
    map { if (it == 'o' || it == 'O') '0' else it }.joinToString("")

/** Kurus adayi: `,50` ya da `50`. */
private val MINOR = Regex("""[.,]?\d{2}""")

/**
 * Binlik ayiricili sayi: `1.250`, `2 330`.
 *
 * Bu kural on dokuz olcumun HICBIRINI degistirmiyor - manseti bin liranin
 * ustunde bir etiket cekilmedi - ve testte oyle kilitli. Yazilma sebebi
 * baska: bin liranin ustunde bir A101 etiketinde kurus `00` ise (dokuz karede
 * olculen hal) manset `1.250.` diye gelir ve 4. kural son iki haneyi kurus
 * sanip 12,50 yazardi.
 *
 * Dayanak olculmus: ML Kit A101'in binlik noktalarini birim fiyat satirinda
 * KORUYOR - `1 KG = 1.253,33 TL` (`133036`), `1 LT 2.330,00 TL` (`133411`).
 * Kural yalnizca ACIK bir ayirici gorunce ateslendigi icin muhafazakar.
 */
private val THOUSANDS = Regex("""\d{1,3}[.\s]\d{3}""")

/** Ayri kurus parcasinin manset boyuna orani - olculen 0,44 / 0,44 / 0,47. */
private val MINOR_HEIGHT_RANGE = 0.25..0.70

/** Kurusun mansete gore dikey kaymasi, manset boyunun kati - olculen en fazla 0,04. */
private const val MINOR_BAND = 0.25

/** Bu kadar haneden itibaren son iki hane kurus sayilir. */
private const val FUSED_MIN_DIGITS = 4

/** Raf fiyatinda bundan fazla hane yok; fazlasi gurultudur. */
private const val MAX_PRICE_DIGITS = 7
