package com.neydi.app.ui.components

import androidx.compose.runtime.Immutable

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

    /** 2+ gozlem, ambalaj ayni: trend + delta + sparkline. */
    @Immutable
    data class Trend(
        val from: String,
        val to: String,
        val deltaPercent: Int,
        val rising: Boolean,
        /** Son 8 gozlem. 2'den azsa sparkline cizilmez. */
        val history: List<Float>,
    ) : PriceHint

    /**
     * Ambalaj kuculmus (shrinkflation). Trend BASTIRILIR.
     * 900g -> 800g ayni fiyata satiliyorsa bu bir fiyat dususu DEGILDIR;
     * trend olarak gosterilseydi yesil ok cikardi ve yalan olurdu.
     */
    @Immutable
    data class PackChanged(val fromPack: String, val toPack: String, val note: String) : PriceHint
}

@Immutable
data class ListRow(
    val name: String,
    /** "2x" veya "1 kg". Adet 1 ise null - rozet cizilmez. */
    val quantity: String? = null,
    val checked: Boolean = false,
    /** "Her zamankiler" bolumundeki sabit. %70 opaklik + raptiye ile cizilir. */
    val isStaple: Boolean = false,
    /** Yalnizca ES ekledigunde dolu. Kendi ekledigimizde ASLA gosterilmez. */
    val addedByInitial: String? = null,
    val priceHint: PriceHint = PriceHint.None,
    /** Satir bir oneriden geldiyse gerekcesi: "12 gundur almadin". */
    val suggestionReason: String? = null,
    val note: String? = null,
    /** "A101'de 34,90". Liste basina EN FAZLA 3 - ustu listeyi reklam yuzeyine cevirir. */
    val cheaperElsewhere: String? = null,
)

/** Satirin ikinci satirinda ne yazacagi. Ayni anda yalnizca BIRI. */
internal sealed interface SecondLine {
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
    priceHint !is PriceHint.None -> SecondLine.Price(priceHint)
    suggestionReason != null -> SecondLine.Reason(suggestionReason)
    note != null -> SecondLine.Note(note)
    else -> SecondLine.Empty
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
