package com.neydi.app.data.receipt

/**
 * Bir fis satirinin FISIN KENDI VERDIGI kimligi - parca dikisi icin (F4.15).
 *
 * NEDEN GEREKLI, OLCUMLE: kullanicinin AKYURT fisi dort parca cekildi ve
 * fisin kendi sira numaralari sayildiginda **altmis kalemin kirk ikisi iki
 * ayri parcada birden** okunmustu. Bugunku model parcalarin birbirine
 * DEGMEMESINI sart kosuyor - aritmetik kapisi butun parcalarin satirlarini
 * topluyor, yani her bindiren satir iki kez sayiliyor. Kullanicidan temiz
 * kesim istemek yanlis yuk: elde tutulan bir fisi santim santim boluyor ve
 * yine de alti kalem hic okunmamis oluyor.
 *
 * Dogru cevap bindirmeyi YASAK degil CAPA yapmak - belge tarayicilarinin
 * panoramada yaptigi sey. Bu dosya o capanin kimlik tarafi.
 *
 * KIMLIK FISTEN GELIYOR, BIZDEN DEGIL. AKYURT her kalemi bir SIRA NUMARASIYLA
 * basiyor ve numara fis icinde tekil - ayni urunden iki kez alinsa bile iki
 * ayri numara aliyor. Yani "ayni numara + ayni barkod" iki kez gorunduyse o
 * bir bindirme, iki ayri alim degil.
 *
 * SIRA NUMARASI YOKSA KIMLIK DE YOK ve bu bilincli: BIM/File duzeninde
 * (`AD ... *TUTAR`) satirin fisten gelen bir kimligi yok, elimizde yalnizca ad
 * ve tutar var. Ayni fiste ayni urunden iki adet ayri satir olarak basiliyorsa
 * onlari bindirme sanip birini SILMEK, kullanicinin gercekten aldigi bir seyi
 * yok etmek olurdu. Silmemek en kotu ihtimalle mukerrer gosterir; silmek veri
 * kaybettirir - ikisi ayni agirlikta degil.
 */
internal fun lineIdentity(rawText: String): String? {
    val match = ITEM_HEAD.find(rawText) ?: return null
    val sequence = normalizeDigits(match.groupValues[1])
    val code = normalizeDigits(match.groupValues[2])
    return "$sequence/$code"
}

/**
 * Kalemli satirin BASI: sira numarasi ve barkod/tarti kodu.
 *
 * `ITEMISED_LINE`in tamami DEGIL: kimlik satirin gerisine - miktara, birime,
 * tutara - bakmamali. Ayni kalem iki parcada FARKLI okunabiliyor (birinde
 * `176,31`, digerinde `176,3l`) ve kimlik ona takilirsa bindirme kacar. Bas
 * kismi ise iki karede de ayni: OCR sira numarasini ve barkodu daha guvenilir
 * okuyor cunku ikisi de duz rakam dizisi.
 */
private val ITEM_HEAD = Regex("""^\s*([0-9OSIl]{1,3})\s+([0-9OSIl]{5,})\s""")

/**
 * OCR'in harfe cevirdigi rakamlari geri alir.
 *
 * Gercek fiste goruldu: `S0` (50), `869O508101426` (sifir yerine harf O).
 * Kimlik bu duzeltmeyi yapmazsa ayni kalem iki parcada iki AYRI kimlik alir ve
 * bindirme goze gorunmez kalir.
 */
/**
 * Ust uste binen SERITLERDEN gelen mukerrer satirlari eler (F4.17).
 *
 * Tek fotograf iceride seritlere bolunup her serit ayri okunuyor ve seritler
 * bilerek %15 biniyor - serit sinirina denk gelen satir ikiye bolunmesin diye.
 * Bindirmenin bedeli mukerrer satir; bu fonksiyon o bedeli odiyor.
 *
 * AD SATIRI DA BIRLIKTE ATILIYOR. AKYURT duzeninde ad, tutar satirinin bir
 * ALTINDA (F4.14). Mukerrer tutar satirini atip adini birakmak, ayristiricinin
 * eslestirmesini kaydirirdi: kalan ad bir sonraki kalemin adi sanilirdi -
 * yani tek bir mukerrer satir butun fisi bir kaydirirdi.
 *
 * KIMLIKSIZ SATIRA DOKUNULMUYOR. Bas/kunye satirlari, KDV dokumu, toplam:
 * hicbiri sira numarasi tasimiyor ve zaten ayristirici tarafinda eleniyor.
 */
internal fun dedupeRepeatedItems(rows: List<String>): List<String> {
    val seen = HashSet<String>()
    val kept = ArrayList<String>(rows.size)
    var index = 0
    while (index < rows.size) {
        val row = rows[index]
        val identity = lineIdentity(row)
        if (identity == null) {
            kept += row
            index++
            continue
        }
        if (seen.add(identity)) {
            kept += row
            index++
        } else {
            // Mukerrer kalem: kendisi ve - varsa - adi birlikte atiliyor.
            index++
            if (index < rows.size && lineIdentity(rows[index]) == null &&
                rows[index].any(Char::isLetter)
            ) {
                index++
            }
        }
    }
    return kept
}

private fun normalizeDigits(raw: String): String = raw.map { char ->
    when (char) {
        'O', 'o' -> '0'
        'S', 's' -> '5'
        'I', 'l', 'i' -> '1'
        else -> char
    }
}.joinToString("")
