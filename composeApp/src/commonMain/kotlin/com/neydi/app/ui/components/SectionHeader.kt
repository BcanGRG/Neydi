package com.neydi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.neydi.app.ui.theme.Spacing

/**
 * Reyon bolumu basligi: "Fırın-Ekmek  3".
 *
 * ALL-CAPS YOK. Tasarim sisteminde buyuk harf kullanilmiyor cunku Turkce
 * i/İ ve ı/I donusumu locale'siz uppercase()'te bozulur; ustelik M3 Expressive
 * de caps'i biraktı. Ayirt edicilik punto ve renkle saglaniyor.
 *
 * BOS BOLUM CIZILMEZ - cagiran taraf 0 ogeli bolumu hic olusturmaz.
 */
@Composable
fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}
