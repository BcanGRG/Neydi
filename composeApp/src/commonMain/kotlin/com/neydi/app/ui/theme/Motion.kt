package com.neydi.app.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Theme.kt'deki NeydiIndication basili halin TONAL OVERLAY yarisini global olarak
 * hallediyor (%6 siyah). Eksik olan diger yarisi burada: 0.97 scale dususu.
 *
 * Ripple olmadigi icin basili / devre disi / odakli hallerin UCU DE acikca
 * tanimlanmak zorunda; bu dosya o sozlesmenin tek uygulanma yeri.
 *
 * Kaynak: Claude Design "Neydi Kotlin Multiplatform" projesi, handoff/theme/Motion.kt.
 */
object Motion {
    /** Kararli oturur, gorunur overshoot yok. */
    fun <T> settle() = spring<T>(dampingRatio = 0.9f, stiffness = 400f)

    /** Isaretleme: daire -> squircle sekil donusumu + 0.96 scale cukuru. */
    const val CHECK_MS = 200

    /** Isaretlenen satirin "Alindi" bolumune kaymasi. Isinlanmaz - goz takip edebilmeli. */
    const val REORDER_MS = 260

    /**
     * Silme jesti: esik gecilmeden birakilan satirin yerine donusu, ve silinen
     * satirin yukseklik daralmasi (tasarim: "200 ms'de yerine donuyor").
     *
     * [CHECK_MS] ILE AYNI SAYI AMA AYRI SABIT. O isaretleme animasyonu; bu
     * silme. Ikisini tek sabite baglamak, biri degistiginde otekini de sessizce
     * degistirmek demek olurdu - Motion.kt'nin butun dosyasi bu ayrimin uzerine
     * kurulu (bkz. REORDER_MS'in CHECK_MS'ten farkli olmasi).
     */
    const val ROW_DELETE_MS = 200

    /**
     * AZ ONCE EKLENEN SATIRIN vurgusunun sonme suresi.
     *
     * ⚠ **BU SAYI TASARIMDAN DEGIL.** Maket vurgunun KENDISINI ciziyor (Ekran
     * 2 · "Hızlı yazma · kök": Enter'lanan satir `#F6E7D2` amber-krem dolgu
     * tasiyor, komsulari tasimiyor) ama ne suresini ne egrisini ne de adini
     * hicbir tasarim dosyasi yaziyor. Yani piksel onlarin, zamanlama bizim -
     * ve bu sabit tam da o bosluga isaret ediyor.
     *
     * Secim gerekcesi: vurgu bir OLAY bildirimi, durum degil; toast'in 2 sn'si
     * ile ayni sinifta ama ondan kisa olmali cunku ekranin merkezinde ve
     * kullanicinin gozu zaten orada. Girisi ANI (ekleme ani), cikisi yumusak.
     *
     * Tasarima soruldu (`docs/29`); cevap gelince degisecek TEK yer burasi.
     */
    const val JUST_ADDED_MS = 1_200

    /** Vurgunun sonusu - girisi ani oldugu icin yalniz cikis egrisi var. */
    const val JUST_ADDED_FADE_MS = 400

    const val PRESSED_SCALE = 0.97f
    const val CHECK_SCALE_DIP = 0.96f
    const val DISABLED_ALPHA = 0.38f
}

/**
 * Her etkilesimli eleman icin basili hal. clickable'i da bu sarar ki
 * bir cagri yerinde scale'i unutmak mumkun olmasin.
 */
/**
 * @param onLongPress verilirse uzun basma da yakalaniyor. Basili hal (scale +
 *   overlay) ikisinde de ayni, yani kullanici iki hareketin de kaydedildigini
 *   ayni gorsel dille goruyor.
 */
@Composable
fun Modifier.pressable(
    enabled: Boolean = true,
    onLongPress: (() -> Unit)? = null,
    onTap: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) Motion.PRESSED_SCALE else 1f,
        animationSpec = Motion.settle(),
        label = "pressScale",
    )
    // LocalIndication zaten NeydiIndication; indication = null VERMIYORUZ ki
    // %6 tonal overlay calissin. Scale ile overlay birlikte basili hali olusturur.
    val indication = LocalIndication.current
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .alpha(if (enabled) 1f else Motion.DISABLED_ALPHA)
        .combinedClickable(
            interactionSource = source,
            indication = indication,
            enabled = enabled,
            onLongClick = onLongPress,
            onClick = onTap,
        )
}

/**
 * Odakli hal: 2dp outline halkasi, 2dp bosluk. Ripple olmadigi icin klavye ve
 * erisilebilirlik gezinmesinde gorunur tek isaret bu.
 *
 * Odaklı ve odaksiz hal ayni toplam 4dp inset'i kullanir, boylece odaklanma
 * layout'u kaydirmaz.
 */
fun Modifier.focusRing(focused: Boolean, shape: Shape, outline: Color): Modifier =
    if (focused) padding(2.dp).border(2.dp, outline, shape) else padding(4.dp)
