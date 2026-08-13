package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Sizes

/**
 * Amber sozlesmesinin TEK uygulanma yeri.
 *
 * accent (#E0A32E) isik modunda surface uzerinde 2.08:1 - kendi sinirini tasiyamaz.
 * accentNeedsOutline true iken 1.5dp accentOutline kenarlik ZORUNLU.
 * Amber isik modunda asla metin rengi degildir; metin onAccent'tir.
 *
 * KOD INCELEMESI KURALI: background(extras.accent) cagrisi bu dosya disinda gecmemeli.
 *
 * Kaynak: Claude Design "Neydi Kotlin Multiplatform" projesi, handoff/ui/AccentChip.kt.
 */
@Composable
fun AccentSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit,
) {
    val extras = LocalNeydiExtraColors.current
    Box(
        modifier = modifier
            .clip(shape)
            .background(extras.accent)
            .then(
                if (extras.accentNeedsOutline) {
                    Modifier.border(Sizes.accentOutline, extras.accentOutline, shape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

/** "A101'de 34,90" gibi ince accent cipleri. Liste basina EN FAZLA 3. */
@Composable
fun AccentChip(text: String, modifier: Modifier = Modifier) {
    val extras = LocalNeydiExtraColors.current
    AccentSurface(modifier.heightIn(min = 24.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = extras.onAccent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * Sol serit (fis kontrolunde "yeni urun" ve "emin degil" satirlari).
 * Serit de bir amber DOLGUDUR: isik modunda kenarligi ayni kural geregi tasir.
 */
@Composable
fun AccentStrip(modifier: Modifier = Modifier) {
    AccentSurface(
        modifier = modifier.heightIn(min = 24.dp),
        shape = StripShape,
        content = {},
    )
}

private val StripShape = RoundedCornerShape(2.dp)
