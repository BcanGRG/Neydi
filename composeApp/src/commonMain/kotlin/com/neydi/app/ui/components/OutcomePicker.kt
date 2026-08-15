package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.TakeOutcome
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Uc-sonuc secici: `[Aldım] [Gerekmedi] [Unuttum]` (F4.12, Ekran 4).
 *
 * UCLUNUN AYRI OLMASI BIR UI TERCIHI DEGIL, skor formulunun girdisi:
 * *"gerekmedi"* oneriyi **bastirmali**, *"unuttum"* **yukseltmeli** (F6.2).
 * Tek bir onay kutusu ikisini ayni sinyal yapiyordu ve motor iki zit davranisi
 * ayirt edemiyordu.
 *
 * Material3 SegmentedButton DEGIL: o da indication'i sabit kodluyor (bkz.
 * [NeydiButton]); ripple bu uygulamada bilerek yok.
 */
@Composable
fun OutcomePicker(
    selected: TakeOutcome,
    onSelect: (TakeOutcome) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(NeydiExtraShapes.pill)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
    ) {
        OutcomeSegment("Aldım", selected == TakeOutcome.TAKEN) { onSelect(TakeOutcome.TAKEN) }
        OutcomeSegment("Gerekmedi", selected == TakeOutcome.NOT_NEEDED) { onSelect(TakeOutcome.NOT_NEEDED) }
        OutcomeSegment("Unuttum", selected == TakeOutcome.FORGOTTEN) { onSelect(TakeOutcome.FORGOTTEN) }
    }
}

@Composable
private fun OutcomeSegment(label: String, selected: Boolean, onTap: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(NeydiExtraShapes.pill)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            // Dokunma hedefi tabani 44dp'nin altina inmesin diye dikeyde
            // dolgu buyutuluyor; uc parcali bir kontrolde her parca ayri hedef.
            .pressable(onTap = onTap)
            .heightIn(min = Sizes.minTapTarget)
            .padding(horizontal = Spacing.sm)
            .wrapContentHeight(align = Alignment.CenterVertically),
    )
}

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun OutcomePickerPreview() = NeydiPreview {
    OutcomePicker(selected = TakeOutcome.TAKEN, onSelect = {})
    OutcomePicker(selected = TakeOutcome.NOT_NEEDED, onSelect = {})
    OutcomePicker(selected = TakeOutcome.FORGOTTEN, onSelect = {})
}
