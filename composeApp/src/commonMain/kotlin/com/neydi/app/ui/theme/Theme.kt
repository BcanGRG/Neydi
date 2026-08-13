package com.neydi.app.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier

/** 8dp grid. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

/** Satir yukseklikleri - alisveris modunda 56/68 -> 72dp. */
object Sizes {
    val rowCollapsed = 56.dp
    val rowWithMeta = 68.dp
    val rowShopping = 72.dp
    val minTapTarget = 44.dp
    val toolbarAction = 56.dp
    val hairline = 1.dp          // 0.5dp ASLA - iOS 3x'te alt-piksele dusup kaybolur
    val accentOutline = 1.5.dp   // isik modunda amber dolgu icin ZORUNLU
}

val NeydiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),     // thumbnail
    large = RoundedCornerShape(20.dp),      // liste satiri container
    extraLarge = RoundedCornerShape(28.dp), // bottom sheet ust / dialog
)

/**
 * Material ripple'i GLOBAL olarak degistirir.
 *
 * Ripple, iOS'ta en yuksek sesli "bu bir Android uygulamasi" isaretidir.
 * Yerine tonal overlay (scale dusumu cagri yerinde Modifier ile eklenir).
 * Bu yuzden tasarimda her etkilesimli elemanin basili hali ACIKCA tanimlanmak
 * zorunda - "varsayilan ripple" bir cevap degil.
 */
private object NeydiIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        NeydiIndicationNode(interactionSource)

    override fun hashCode(): Int = -1
    override fun equals(other: Any?): Boolean = other === this
}

private class NeydiIndicationNode(
    private val interactionSource: InteractionSource,
) : Modifier.Node(), DrawModifierNode {

    private var pressed = false

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                val next = when (interaction) {
                    is PressInteraction.Press -> true
                    is PressInteraction.Release, is PressInteraction.Cancel -> false
                    else -> pressed
                }
                if (next != pressed) {
                    pressed = next
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (pressed) {
            drawRect(color = Color.Black.copy(alpha = 0.06f), size = size)
        }
    }
}

@Composable
fun NeydiTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NeydiDarkColors else NeydiLightColors
    val extras = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(
        LocalNeydiExtraColors provides extras,
        LocalIndication provides NeydiIndication,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NeydiTypography,
            shapes = NeydiShapes,
            content = content,
        )
    }
}
