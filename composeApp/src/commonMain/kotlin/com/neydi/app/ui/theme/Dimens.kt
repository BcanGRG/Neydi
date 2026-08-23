package com.neydi.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Theme.kt'deki Spacing / Sizes / NeydiShapes'in tamamlayicisi.
 * Oradakileri TEKRAR ETMEZ, yalnizca tasarimda gecen ama kodda henuz karsiligi
 * olmayan olculeri ekler. Cakisma olursa Theme.kt kazanir.
 *
 * Kaynak: Claude Design "Neydi Kotlin Multiplatform" projesi, handoff/tokens.json.
 */

/** Spacing'de olmayan tek adim: onay kutusu ile urun adi arasi. */
object SpacingExtra {
    /**
     * Bolum basliginin USTUNDEKI bosluk (maket: 14px dolgu + 6px margin).
     *
     * Altindakinden bes kat buyuk olmasi kasitli: baslik kendi satirlarina
     * yapisik, onceki bolumden uzak. Bkz. `SectionHeader`.
     */
    val aboveSectionHeader = 20.dp

    /** Bolum basliginin ALTINDAKI bosluk (maket: 4px). */
    val belowSectionHeader = 4.dp
    // 10dp - maketin kendi olcusu (kimlik bandinin sol boslugu).
    val betweenCheckboxAndName = 10.dp
    val emptyStateBlock = 48.dp
}

object NeydiExtraShapes {
    /** Gecmis'teki harcama cubugu: yalnizca UST koseler yuvarlak. */
    val categoryTile = RoundedCornerShape(24.dp)   // 56dp squircle kutucuk
    val textField = RoundedCornerShape(18.dp)
    val pill = CircleShape                         // cip ve buton
    val checkRest = CircleShape                    // onay hedefi: daire
    val checkChecked = RoundedCornerShape(12.dp)   // isaretlendiginde squircle
    val card = RoundedCornerShape(24.dp)           // ozet karti, bos durum blogu
    val snackbar = RoundedCornerShape(18.dp)       // aksiyonlu gecici serit
    val swipeRow = RoundedCornerShape(20.dp)       // silme jestinde acilan satir
}

object SizesExtra {
    /** Ekran basligi (tokens.json `size/header`). Baslik + alt satir bu yukseklikte. */
    val header = 56.dp
    val checkbox = 24.dp
    val checkboxShopping = 28.dp
    val categoryTile = 56.dp
    val sparkline = DpSize(24.dp, 16.dp) // 2 gozlemin altinda hic cizilmez
    val priceColumn = 92.dp              // tnum uygulanmasa da duzen bozulmasin diye sabit
    // 24dp: rozet kimlik bandinin yuksekligini BELIRLIYOR (karar 103), yani
    // bu sayi satirin ust katinin boyu demek. Maket 24px yaziyor.
    val qtyBadgeHeight = 24.dp

    /** Adet rozetinin taban genisligi - tek haneli "1" de hap gibi dursun diye. */
    val qtyBadgeMinWidth = 30.dp
    val suggestionChip = 40.dp
    val quickAddField = 52.dp
    val rowBorderShopping = 1.5.dp       // alisveris modunda satir container kenarligi
}

/**
 * Safe area ZORUNLU ve simetrik DEGIL. Alt kenara yapisik birincil aksiyon olmaz:
 * floating toolbar bu boslugun ustunde durur.
 */
object SafeArea {
    val top = 44.dp
    val bottom = 34.dp   // iOS home indicator
}

/**
 * Derinlik = ton, golge degil. Blur / frosted glass / vibrancy YOK, renkli golge YOK.
 * Katman: surface -> surfaceVariant -> +1 ton.
 * Kaydirilan icerikte golge YOK; ayrim Sizes.hairline (1dp) ile yapilir.
 */
object Elevation {
    val floatingToolbar = 3.dp   // izinli iki yerden biri
    val addButton = 4.dp         // izinli ikinci yer
    val everythingElse = 0.dp
}
