package com.neydi.app.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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

/**
 * @param darkTheme varsayilani sistem ayari. Varsayilan olmasi sart:
 * `@PreviewLightDark` temayi `uiMode` uzerinden surer ve preview'in tema
 * argumani gecmesi mumkun degil - varsayilan olmazsa previewler hep acik cikar.
 */
@Composable
fun NeydiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NeydiDarkColors else NeydiLightColors
    val extras = if (darkTheme) DarkExtraColors else LightExtraColors

    // Sistem cubuklari temayi takip eder. onCreate'te bir kez cagrilan
    // enableEdgeToEdge yetmiyor: manifest'te configChanges=uiMode oldugu icin
    // karanlik moda gecince Activity yeniden yaratilmiyor ve cubuklar acilistaki
    // stilde kaliyor. Detay: SystemBars.kt
    // ISTEK TEMADAN GELIYOR AMA EKRAN BASTIRABILIYOR.
    //
    // Kamera ekrani her iki temada da KOYU (tasarim: gezinme sozlesmesi,
    // "Karanlik tema - kamera"). Acik temada uygulamanin temasina bakan bir
    // cubuk stili orada siyah ikonlar veriyor ve siyah onizlemenin uzerinde
    // okunmuyor. Cubugun rengi artik ekranin talebine bagli; talep yoksa tema
    // ne diyorsa o.
    //
    // Tutucu BURADA cunku CompositionLocal asagi akar, yukari akmaz: alt bir
    // ekranin istegini efektin durdugu yere tasimanin yolu paylasilan bir state.
    val barsDark = remember { mutableStateOf<Boolean?>(null) }
    ApplySystemBarAppearance(barsDark.value ?: darkTheme)

    // Font aileleri Compose Resources uzerinden @Composable olarak cozuluyor,
    // o yuzden tipografi top-level val degil, burada kuruluyor.
    val typography = rememberNeydiTypography()
    val textStyles = rememberNeydiTextStyles()

    // SIRALAMA KRITIK: provider MaterialTheme'in ICINDE olmali, ustunde degil.
    // MaterialTheme kendi govdesinde `LocalIndication provides ripple()` yapiyor
    // (material3 1.9.0, MaterialTheme.kt:102). Ustte verilen LocalIndication
    // content'e hic ulasmiyordu - ripple'i kaldirma karari yazildigindan beri
    // yururlukte DEGILDI ve hicbir yerde hata vermedigi icin sessizce yanlisti.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = NeydiShapes,
    ) {
        CompositionLocalProvider(
            LocalNeydiExtraColors provides extras,
            LocalSystemBarsDarkRequest provides barsDark,
            LocalNeydiTextStyles provides textStyles,
            LocalIndication provides NeydiIndication,
            content = content,
        )
    }
}

/**
 * Bir ekranin sistem cubuklari icin acik/koyu TALEBI.
 *
 * `null` = talep yok, tema ne diyorsa o. Bir ekran bastirmak isterse
 * [RequestDarkSystemBars] kullaniyor; cikinca kendiliginden geri aliniyor.
 *
 * Neden `MutableState` saglaniyor da deger degil: efekt [NeydiTheme]'in
 * icinde ve CompositionLocal yukari akmiyor. Paylasilan state tek yol.
 */
internal val LocalSystemBarsDarkRequest = staticCompositionLocalOf<MutableState<Boolean?>> {
    error("LocalSystemBarsDarkRequest yalnizca NeydiTheme icinde kullanilabilir")
}

/**
 * Bu ekran acikken sistem cubuklari KOYU zemin varsayar (acik ikonlar).
 *
 * Ekrandan cikilinca talep birakiliyor, yani cubuklar temaya doner - bunu
 * `DisposableEffect` garanti ediyor. `SideEffect` ile yapilsaydi geri alma
 * bir sonraki rastgele recomposition'a kalirdi.
 */
@Composable
fun RequestDarkSystemBars() {
    val request = LocalSystemBarsDarkRequest.current
    DisposableEffect(Unit) {
        request.value = true
        onDispose { request.value = null }
    }
}
