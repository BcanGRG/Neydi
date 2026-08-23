package com.neydi.app.ui.components

import androidx.compose.runtime.Immutable
import com.neydi.app.data.formatAge

/**
 * Liste satirinin gorunum modelleri. GECICI: F2'de Room entity'lerinden turetilecek.
 * Simdilik bilesen kutuphanesi bunlarla sahte veriyle calisiyor.
 */

/**
 * Fiyat ipucunun UC VERI DURUMU - tip sisteminde kodlanmis hali.
 *
 * Bu bir sealed interface cunku kural sunlar: tek gozlemden yuzde gosterilmez,
 * ambalaj degistiyse trend gosterilmez, 2 gozlemin altinda grafik cizilmez.
 * Ayri boolean'larla tutulsaydi "1 gozlem + trend" gibi imkansiz bir durum
 * derlenebilirdi. Boyle derlenmiyor.
 */
@Immutable
sealed interface PriceHint {
    /** Hic gozlem yok. Ikinci satir CIZILMEZ - "fiyat yok" da yazilmaz. */
    data object None : PriceHint

    /** Tek gozlem: "son 38,50 TL · A101 · 12 gun once". Yuzde YOK, grafik YOK. */
    @Immutable
    data class Single(val price: String, val store: String, val daysAgo: Int) : PriceHint

    /**
     * 2+ gozlem, ambalaj ayni: trend + delta.
     *
     * SPARKLINE ALANI SILINDI (karar 106). Cizgi listeden kalkinca `history`
     * yalnizca YAZILAN, hic okunmayan bir alan kaldi - ve bedeli gorunmezdi:
     * satir basina korele bir `group_concat` alt sorgusu. Bileseni silip veri
     * hattini ayakta birakmak tam olarak sessiz curume olurdu. Gecmis grafigi
     * Urun Detayi'nda kendi sorgusundan besleniyor.
     */
    @Immutable
    data class Trend(
        val from: String,
        val to: String,
        val deltaPercent: Int,
        val rising: Boolean,
    ) : PriceHint

    /**
     * Ambalaj kuculmus (shrinkflation). Trend BASTIRILIR.
     * 900g -> 800g ayni fiyata satiliyorsa bu bir fiyat dususu DEGILDIR;
     * trend olarak gosterilseydi yesil ok cikardi ve yalan olurdu.
     */
    @Immutable
    data class PackChanged(
        val fromPack: String,
        val toPack: String,
        val note: String,
        /**
         * Ambalaj KUCULDU mu - cumlenin fiili buradan.
         *
         * ⚠ Bu alan cihazda yakalanan bir YALANDAN dogdu: dal iki ambalaj
         * FARKLIYSA atesleniyordu, yonune bakmadan, ama metin her zaman
         * *"ambalaj kuculdu"* yaziyordu. Kullanicinin kendi verisinde
         * `1,5 kg → 3 kg` bir buyume ve satir onu kuculme diye yazdi.
         *
         * Kuculme hali hala ayri bir sey soyluyor (karar 67: shrinkflation
         * bir fiyat dususu DEGILDIR), o yuzden fiil silinmedi - dogru yone
         * baglandi.
         */
        val smaller: Boolean = true,
    ) : PriceHint
}

@Immutable
data class ListRow(
    val name: String,
    /**
     * "2x", "1 kg" ya da yalin "1". HER SATIRDA dolu (karar 103).
     *
     * Rozet artik yalnizca okunan degil DUZENLENEN yer; cizilmeyen rozet
     * duzenlenemeyen miktar demek olurdu.
     */
    val quantity: String = "1",
    /**
     * Miktar varsayilanindan cikarildi mi (karar 107).
     *
     * Rozetin dolgulu mu konturlu mu cizilecegini bu soyluyor: kontur
     * "dokunulmamis", dolgu "bunu ben sectim". Ayrimin degeri listeye hizli
     * bakista: hangi satirlarin miktarini gercekten dusundugunu gorursun.
     */
    val quantityModified: Boolean = false,
    val checked: Boolean = false,
    /** "Her zamankiler" bolumundeki sabit. %70 opaklik + raptiye ile cizilir. */
    val isStaple: Boolean = false,
    /** Yalnizca ES ekledigunde dolu. Kendi ekledigimizde ASLA gosterilmez. */
    val addedByInitial: String? = null,
    val priceHint: PriceHint = PriceHint.None,
    /**
     * Satir tahmine GIRMIYOR, cunku ambalaji bilinmiyor (karar 111).
     *
     * Fiyat cipi yerinde kaliyor - okunan sey bir AMBALAJIN fiyati ve dogru.
     * Yanlis olan sey onu miktarla carpmakti. Meta yuvasi eksikligi yaziyor.
     */
    val packUnknown: Boolean = false,
    /** Satir bir oneriden geldiyse gerekcesi: "12 gundur almadin". */
    val suggestionReason: String? = null,
    val note: String? = null,
    /**
     * "A101'de 34,90". Liste basina EN FAZLA 3 - ustu listeyi reklam yuzeyine cevirir.
     *
     * ⚠ **DOLDURAN TARAF `formatChipMinor` KULLANMALI**, `formatMinor` degil:
     * gezinme sozlesmesi *"yalnizca 24dp cipte TL duser; cumle icinde asla"*
     * diyor. Bu alan bugun hicbir yerden dolmuyor (E16 getirecek) ve kural
     * cagirandan once burada duruyor ki dolduran ona uymak zorunda kalsin.
     *
     * Cipin ESIGI de tasarimdan (karar 41): karsi gozlem **hem %10 hem 5 TL**
     * ucuz olacak ve **14 gunden eski olmayacak**; trendle cakisirsa cip
     * kazanir, trend bastirilir; siralama mutlak TL tasarrufuna gore.
     */
    val cheaperElsewhere: String? = null,
)

/** Satirin ikinci satirinda ne yazacagi. Ayni anda yalnizca BIRI. */
internal sealed interface SecondLine {
    /**
     * *"3 kg · ambalaj bilinmiyor"* (karar 111) - fiyat ipucunun ONUNDE.
     *
     * Once geliyor, cunku bu satirda okunacak en onemli sey artik fiyatin
     * nereden geldigi degil, tahmine NEDEN girmedigi. Yeni bir oge de degil:
     * ayni meta yuvasi, farkli cumle - karar 83'un "tek icerik" kurali
     * duruyor.
     */
    data class PackUnknown(val text: String) : SecondLine

    data class Price(val hint: PriceHint) : SecondLine
    data class Reason(val text: String) : SecondLine
    data class Note(val text: String) : SecondLine
    data object Empty : SecondLine
}

/**
 * Ikinci satir onceligi: (1) fiyat ipucu, (2) oneri gerekcesi, (3) not, (4) hicbir sey.
 * Asla iki satir metadata olmaz - bu yuzden secim tek bir yerde yapiliyor.
 */
internal fun ListRow.secondLine(): SecondLine = when {
    packUnknown -> SecondLine.PackUnknown("$quantity · ambalaj bilinmiyor")
    priceHint !is PriceHint.None -> SecondLine.Price(priceHint)
    suggestionReason != null -> SecondLine.Reason(suggestionReason)
    note != null -> SecondLine.Note(note)
    else -> SecondLine.Empty
}

/**
 * Bir gozlemin "guncel" sayildigi sinir (karar 105).
 *
 * ## Neden bir sinir var
 *
 * Fiyat cipi GUNCEL fiyati tasiyor. Iki hafta once gorulmus bir fiyati cipe
 * koymak, onu bugunku fiyat gibi gostermek olurdu - satirin en cok bakilan
 * yerinde, en kolay yanlis anlasilacak bicimde. Sinirin otesinde cip HIC
 * cizilmiyor ve fiyat cumlenin icine giriyor: *"son 24,90 TL · Migros · 8 gun
 * once"*. Ayni sayi, ama artik bir HATIRLAMA olarak isaretlenmis.
 *
 * Yedi gun, market fiyatlarinin haftalik kampanya dongusuyle degistigi
 * gercegine dayaniyor: bir haftadan eski etiket baska bir kampanyaya ait
 * olabilir.
 */
const val FRESH_DAYS: Int = 7

/**
 * Ekonomi bandinin cumlesi (karar 104-105). Bos ise bant cizilmez.
 *
 * ## Neden saf bir fonksiyon
 *
 * Bu cumle bir SOZ tasiyor: *"fiyat iki yerde asla yazilmaz."* Cipin ne zaman
 * cizildigi ile cumlenin ne yazdigi ayri yerlerde kararlastirilirsa, ikisinin
 * ayni anda fiyat yazdigi bir hal er ya da gec ortaya cikar - nitekim eskiden
 * `Single` dalinda tam olarak o oluyordu. Saf fonksiyon o sozu Compose
 * kurmadan sinanabilir kiliyor.
 */
internal fun SecondLine.metaText(): String = when (this) {
    is SecondLine.PackUnknown -> text
    is SecondLine.Reason -> text
    is SecondLine.Note -> text
    SecondLine.Empty -> ""
    is SecondLine.Price -> when (val h = hint) {
        PriceHint.None -> ""

        // SINGLE'IN IKI TAZELIK HALI (karar 105).
        //
        // Karar 83 ile maket celismiyordu, iki FARKLI hali ciziyordu ve kod
        // yalnizca birini biliyordu. Taze gozlemde fiyat cipte durur, bant
        // yalniz zincir + yasi yazar. Eskimis gozlemde guncel fiyat YOKTUR,
        // o yuzden cip de yoktur - hatirlanan fiyat cumleye girer.
        //
        // YAS BICIMI, merdiven DEGIL (F11.25): burada okunan sey gozlemin yasi
        // ve "gecen hafta" yedi ile on uc arasini silerdi - oysa "8 gun once"
        // ile "12 gun once" arasindaki fark tam da kullanicinin baktigi sey.
        is PriceHint.Single ->
            if (h.daysAgo <= FRESH_DAYS) "${h.store} · ${formatAge(h.daysAgo)}"
            else "son ${h.price} · ${h.store} · ${formatAge(h.daysAgo)}"

        // GUNCEL FIYAT YAZMIYOR (karar 83): meta GECMISI anlatir, guncel fiyat
        // her zaman ve yalniz fiyat cipindedir.
        is PriceHint.Trend -> "önce ${h.from}"
        is PriceHint.PackChanged ->
            "${if (h.smaller) "ambalaj küçüldü" else "ambalaj büyüdü"}: ${h.fromPack} → ${h.toPack}"
    }
}

/**
 * Kategori kutucugu fallback'i icin urunun ilk iki harfi, TURKCE buyuk harfle.
 *
 * Kotlin'in uppercase()'i Unicode varsayilanini kullanir ve 'i' -> 'I' yapar;
 * Turkce'de dogrusu 'i' -> 'İ'. "incir" -> "IN" yanlis, "İN" dogru.
 * ('ı' -> 'I' donusumu Unicode varsayilaninda zaten dogru.)
 *
 * Bu yuzden harfler burada acikca eslenip cizim zamaninda degil, kaynakta buyutulur.
 */
fun turkishInitials(name: String): String =
    name.trim().take(2).map { it.turkishUppercaseChar() }.joinToString("")

private fun Char.turkishUppercaseChar(): Char = if (this == 'i') 'İ' else uppercaseChar()
