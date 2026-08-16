package com.neydi.app.data.receipt

import com.neydi.app.data.db.Receipt
import com.neydi.app.data.db.ReceiptLine

/**
 * Dikis sonucu: hangi satirlar kaldi, kac tanesi bindirdi, ne eksik gorunuyor.
 */
internal data class StitchResult(
    /** Parca basina KALAN satirlar - bindirenler cikarilmis. */
    val kept: Map<Receipt, List<ReceiptLine>>,
    /** Parca basina bindirdigi icin cikarilan satir sayisi. */
    val overlapCount: Map<String, Int>,
    /**
     * Fisin kendi sira numaralarinda ATLANMIS olanlar.
     *
     * Bos liste iki ayri sey demek olabilir - "hicbir sey eksik degil" ya da
     * "sira numarasi olmayan bir duzen"; ayrimi [hasSequence] tasiyor.
     */
    val missingSequences: List<Int>,
    /** Fis sira numarasi basiyor mu - eksik tespiti ancak o zaman anlamli. */
    val hasSequence: Boolean,
)

/**
 * Ayni fiziksel fisin parcalarini birlestirir - BINDIRME CAPA, TEHLIKE DEGIL
 * (F4.15).
 *
 * OLCUMLE DOGDU: kullanicinin AKYURT fisi dort parca cekildi ve fisin kendi
 * sira numaralari sayildiginda **altmis kalemin kirk ikisi iki ayri parcada
 * birden** okunmustu, ustelik alti kalem hic okunmamisti. Bugunku model
 * parcalarin birbirine DEGMEMESINI sart kosuyordu - aritmetik kapisi butun
 * parcalarin satirlarini topluyor, yani her bindiren satir iki kez sayiliyor
 * ve fisin toplamini asiyordu.
 *
 * Kullanicidan temiz kesim istemek yanlis yuk: elde tutulan bir metrelik fisi
 * santim santim bolmek ve hem bindirmemek hem bosluk birakmamak insanin
 * yapabilecegi bir is degil. Dogru olan kasitli bindirme istemek ve dikisi
 * uygulamanin yapmasi.
 *
 * ILK GOREN KAZANIR: bindiren satirin ONCEKI parcadaki hali tutuluyor. Sira
 * keyfi degil - parcalar cekim sirasina gore sirali ve kullanici fisi bastan
 * sona cekiyor, yani bir kalem ilk kez gorundugu karede genellikle kadrajin
 * ortasinda, tekrar gorundugu karede ise kenarindadir. Kenar en bozuk okunan
 * yer.
 *
 * BIR PARCANIN KENDI ICINDE ELEME YOK. Ayni kare icinde ayni kimlik iki kez
 * gorunuyorsa bu bindirme degil, ya gercekten iki kalem ya da bir okuma
 * hatasidir; ikisinde de silmek yanlis olur.
 */
internal fun stitchParts(byReceipt: Map<Receipt, List<ReceiptLine>>): StitchResult {
    val ordered = byReceipt.keys.sortedBy { it.capturedAt }
    val seen = HashSet<String>()
    val kept = LinkedHashMap<Receipt, List<ReceiptLine>>()
    val overlap = HashMap<String, Int>()
    val sequences = HashSet<Int>()

    for (receipt in ordered) {
        val lines = byReceipt[receipt].orEmpty()
        val keptLines = ArrayList<ReceiptLine>(lines.size)
        var dropped = 0
        // Bu parcanin kendi kimlikleri: ayni kare icinde eleme YAPILMASIN diye
        // ayri tutuluyor, tur bitince `seen`e katiliyor.
        val here = HashSet<String>()
        for (line in lines) {
            val identity = lineIdentity(line.rawText)
            if (identity == null) {
                keptLines += line
                continue
            }
            here += identity
            sequenceOf(identity)?.let(sequences::add)
            if (identity in seen) dropped++ else keptLines += line
        }
        seen += here
        kept[receipt] = keptLines
        overlap[receipt.id] = dropped
    }

    return StitchResult(
        kept = kept,
        overlapCount = overlap,
        missingSequences = missingSequences(sequences),
        hasSequence = sequences.isNotEmpty(),
    )
}

/** Kimligin `sira/kod` bicimindeki sira numarasi. */
private fun sequenceOf(identity: String): Int? =
    identity.substringBefore('/').toIntOrNull()

/**
 * Okunan sira numaralari arasindaki BOSLUKLAR.
 *
 * Fis kalemleri kesintisiz numaralandiriyor, yani 1..60 arasinda okunmamis bir
 * numara varsa o kalem hicbir karede gorunmemis demektir. Bu, kullaniciya
 * "sunlari kacirmissin" diyebilmenin tek durust yolu - aksi halde eksigi
 * ancak fisin toplamiyla karsilastirinca, o da toplam okunabildiyse
 * anlayabiliyoruz.
 *
 * EN BUYUK NUMARADAN SONRASI SAYILMIYOR: fisin kac kalemi oldugunu bilmiyoruz.
 * Son kalemi hic cekmediyse bunu buradan goremeyiz ve UYDURMUYORUZ - aritmetik
 * kapisi o durumu zaten yakaliyor.
 */
private fun missingSequences(seen: Set<Int>): List<Int> {
    if (seen.size < MIN_SEQUENCES) return emptyList()
    val first = seen.min()
    val last = seen.max()
    // Sira numaralari makul bir aralikta olmali; OCR bir numarayi 999 okursa
    // aradaki yuzlerce "eksik" kalem uydurma olurdu.
    if (last - first > MAX_SPAN) return emptyList()
    return (first..last).filterNot { it in seen }
}

/** Bu sayidan az numara okunduysa "duzen sira numarali" demek guvenli degil. */
private const val MIN_SEQUENCES = 4

/** Makul bir fisin kalem araligi. Ustu OCR hatasi sayiliyor. */
private const val MAX_SPAN = 200
