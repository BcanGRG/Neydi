package com.neydi.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum

// PROBE 1: bare PreviewParameterProvider + getDisplayName override
private class ChipTextProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String> = sequenceOf("A101'de 159,90", "3 gündür listede")
    override fun getDisplayName(index: Int): String? = "Case ${index + 1}"
}

// PROBE 2: count default member
private val probeCount: Int = ChipTextProvider().count

// PROBE 3: CollectionPreviewParameterProvider subclass
private class RowProvider : CollectionPreviewParameterProvider<String>(
    listOf("bir", "iki", "uc")
)

// PROBE 4: LoremIpsum data source
private class TextProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String> = LoremIpsum(8).values
}

@Preview
@Composable
private fun ProbeA(@PreviewParameter(ChipTextProvider::class) text: String) =
    NeydiPreview { AccentChip(text) }

@PreviewLightDark
@Composable
private fun ProbeB(@PreviewParameter(RowProvider::class, limit = 2) s: String) =
    NeydiPreview { Text(s + probeCount) }

@Preview
@Composable
private fun ProbeC(@PreviewParameter(TextProvider::class) s: String) =
    NeydiPreview { Text(s) }
