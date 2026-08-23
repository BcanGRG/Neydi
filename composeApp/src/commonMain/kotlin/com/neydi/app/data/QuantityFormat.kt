package com.neydi.app.data

/**
 * Miktarin YAZILI hali - rozette, sayacta ve kesif hucresinde ayni bicim.
 *
 * ## Neden `ui/list` disinda
 *
 * Bu bicim once liste satirinin ozel isiydi. Karar 107-109 ayni sayiyi UC
 * yuzeye birden yaydi - satirdaki rozet, Urun Detayi'ndaki miktar blogu ve
 * kesif hucresinin *"1 kg listede"* satiri - ve ikisi `ui/list` paketinin
 * disinda. Bicimi orada birakmak, ya `internal`i delmek ya da ikinci bir
 * bicimlendirici yazmak demekti; ikincisi "1,5 kg" ile "1.5 kg"in ayni ekranda
 * gorunmesine giden yol.
 */

/**
 * "2x", "1,5 kg", ya da yalin "1" - rozet ARTIK HER SATIRDA (karar 103).
 *
 * ## Neden bos donmuyor
 *
 * Eskiden adet 1 + birim "adet" ise `null` donuyordu, gerekcesi *"her satira
 * 1x yazmak gurultu"*du ve dogruydu - rozet o zaman yalnizca OKUNAN bir seydi.
 * Karar 107 onu DUZENLENEN yer yapti: dokununca miktar sayaci aciliyor.
 * Cizilmeyen rozet, duzenlenemeyen miktar demek olurdu ve kullanicinin sikayeti
 * tam buydu - *"4-5 yapmak istedigimde ya tekrardan yazmam gerekiyor ya da
 * katalogdan surekli ekle-ekle yapmam lazim."*
 *
 * Gurultu itirazi da cozuldu, susturarak degil KUCULTEREK: varsayilan miktarli
 * rozet dolgusuz ve yalin sayi yaziyor ("1", "1x" degil), degistirilmis olan
 * dolgulaniyor. Yani satir hala hangi miktarin elle secildigini tek bakista
 * soyluyor.
 *
 * Ondalik AYIRICI VIRGUL: Turkce'de 1.5 kg diye yazilmaz. Kotlin'in
 * varsayilan toString'i nokta uretir, o yuzden elle degistiriliyor.
 */
fun quantityLabel(count: Double, unit: String): String {
    // YALIN "1": adet birimli varsayilan satirda carpi isareti bir sey
    // soylemiyor - "1x elma" diye konusulmuyor. Tasarimin maketi de bu hali
    // "yalniz konturlu 1" diye ciziyor.
    if (count == 1.0 && unit == "adet") return "1"
    val number = if (count % 1.0 == 0.0) {
        count.toInt().toString()
    } else {
        count.toString().replace('.', ',')
    }
    return if (unit == "adet") "${number}x" else "$number $unit"
}

/**
 * Kesif hucresindeki isaretli hal: *"1 kg listede"* (karar 109).
 *
 * ## Neden hucre artik miktari yaziyor
 *
 * Karar 12 isaretli hucreyi PASIF yapti ve dogru yapti - ama pasif hucre o
 * gune kadar yalnizca *"bu zaten listede"* diyordu. Miktarin kendi evi
 * olmadigi surece kullanicinin elinde tek yol tekrar tekrar dokunmakti; o yol
 * kapaninca hucre "ne kadar" sorusunu cevapsiz birakmis oldu. Artik cevabi
 * kendisi yaziyor.
 */
fun inListLabel(count: Double, unit: String): String = "${quantityLabel(count, unit)} listede"
