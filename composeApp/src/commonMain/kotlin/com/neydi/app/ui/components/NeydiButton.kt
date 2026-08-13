package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Uygulamanin tek buton bileseni.
 *
 * NEDEN Material3 Button DEGIL: material3'un `Surface`i indication'i SABIT
 * KODLUYOR - `indication = ripple()`, uc ayri yerde (material3 1.9.0,
 * Surface.kt:229/336/443). Yani `LocalIndication` override'i Button, Card ve
 * tiklanabilir Surface icin hic okunmuyor. Temada ripple'i kaldirmak bu
 * bilesenlere ULASMIYOR; tek cikis yolu Material3'un tiklanabilir Surface'ini
 * hic kullanmamak.
 *
 * Basili hal sozlesmesi tamamen Modifier.pressable'dan geliyor: 0.97 scale +
 * %6 tonal overlay. Sozlesmenin tek uygulanma yeri orasi.
 *
 * Modifier sirasi kasitli:
 *   clip      -> en disda, overlay'i hap seklinde kirpar
 *   pressable -> graphicsLayer(scale) + clickable; icindeki her sey olcekleniyor
 *   background-> olceklenen katmanin icinde, yani hap da kuculuyor
 */
@Composable
fun NeydiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        modifier = modifier
            .clip(NeydiExtraShapes.pill)
            .pressable(enabled = enabled, onTap = onClick)
            .background(container)
            .defaultMinSize(minHeight = Sizes.minTapTarget)
            .padding(horizontal = Spacing.lg, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun NeydiButtonPreview() = NeydiPreview {
    NeydiButton("Alışverişe çıkıyorum", {})
    NeydiButton("Geri", {})
    NeydiButton("Devre dışı", {}, enabled = false)
    NeydiButton(
        text = "İkincil",
        onClick = {},
        container = MaterialTheme.colorScheme.surfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
