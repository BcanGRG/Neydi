package com.neydi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Motion
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Etiketli açık/kapalı anahtarı.
 *
 * NEDEN MATERIAL3 `Switch` DEGIL: Material3'un etkilesimli bilesenleri
 * indication'i sabit kodluyor (bkz. [NeydiButton] basligi - `Surface.kt`'de uc
 * ayri yerde `indication = ripple()`), yani temadaki `LocalIndication`
 * override'i onlara hic ulasmiyor. Ripple bu uygulamada bilerek kaldirildi;
 * yeni bir Material3 kontrolu eklemek onu geri getirirdi.
 *
 * TUM SATIR DOKUNULABILIR, yalnizca anahtar degil: 52x32dp bir track tek basina
 * 44dp dokunma hedefi tabanini karsilamaz, ve etiketi okuyan parmak dogal
 * olarak etikete gidiyor.
 */
@Composable
fun NeydiSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    val extras = LocalNeydiExtraColors.current
    val track by animateColorAsState(
        targetValue = if (checked) extras.success else MaterialTheme.colorScheme.surfaceVariant,
        label = "switchTrack",
    )
    // Baspar mak hareketi yerine RENK + KONUM degisiyor; ikisi birlikte
    // "acildi" hissini yaratiyor. Ayni yay egrisi butun uygulamada.
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = Motion.settle(),
        label = "switchThumb",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.minTapTarget)
            .pressable { onCheckedChange(!checked) }
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (supporting != null) {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = Spacing.sm),
            )
        }
        Box(
            Modifier
                .width(48.dp)
                .heightIn(min = 28.dp)
                .clip(CircleShape)
                .background(track),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .padding(start = thumbOffset)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
            )
        }
    }
}

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun NeydiSwitchPreview() = NeydiPreview {
    NeydiSwitch(label = "Her zamankilere ekle", checked = true, onCheckedChange = {})
    NeydiSwitch(label = "Bunu önerme", checked = false, onCheckedChange = {})
    NeydiSwitch(
        label = "Çok uzun bir ayar etiketi kırpılmalı ve anahtarı ezmemeli",
        checked = false,
        onCheckedChange = {},
        supporting = "9/12",
    )
}
