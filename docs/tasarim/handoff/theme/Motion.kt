package com.neydi.app.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
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
 * Theme.kt'nin kendi yorumu da bunu "cagri yerinde Modifier ile eklenir" diyor.
 *
 * Ripple olmadigi icin basili / devre disi / odakli hallerin UCU DE acikca
 * tanimlanmak zorunda; bu dosya o sozlesmenin tek uygulanma yeri.
 */
object Motion {
    /** Kararli oturur, gorunur overshoot yok. */
    fun <T> settle() = spring<T>(dampingRatio = 0.9f, stiffness = 400f)

    /** Isaretleme: daire -> squircle sekil donusumu + 0.96 scale cukuru. */
    const val CHECK_MS = 200

    /** Isaretlenen satirin "Alindi" bolumune kaymasi. Isinlanmaz - goz takip edebilmeli. */
    const val REORDER_MS = 260

    const val PRESSED_SCALE = 0.97f
    const val CHECK_SCALE_DIP = 0.96f
    const val DISABLED_ALPHA = 0.38f
}

/**
 * Her etkilesimli eleman icin basili hal. clickable'i da bu sarar ki
 * bir cagri yerinde scale'i unutmak mumkun olmasin.
 */
@Composable
fun Modifier.pressable(
    enabled: Boolean = true,
    onTap: () -> Unit,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) Motion.PRESSED_SCALE else 1f,
        animationSpec = Motion.settle(),
        label = "pressScale",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .alpha(if (enabled) 1f else Motion.DISABLED_ALPHA)
        .clickableNoRipple(source, enabled, onTap)
}

/** LocalIndication zaten NeydiIndication; indication = null vermiyoruz ki overlay calissin. */
@Composable
private fun Modifier.clickableNoRipple(
    source: MutableInteractionSource,
    enabled: Boolean,
    onTap: () -> Unit,
): Modifier = androidx.compose.foundation.clickable(
    interactionSource = source,
    indication = androidx.compose.foundation.LocalIndication.current,
    enabled = enabled,
    onClick = onTap,
)

/**
 * Odakli hal: 2dp outline halkasi, 2dp bosluk. Ripple olmadigi icin klavye ve
 * erisilebilirlik gezinmesinde gorunur tek isaret bu.
 */
fun Modifier.focusRing(focused: Boolean, shape: Shape, outline: Color): Modifier =
    if (focused) padding(2.dp).border(2.dp, outline, shape) else padding(4.dp)
