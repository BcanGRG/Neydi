package com.neydi.app.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.neydi.app.data.db.Category
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.SuggestionChip
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.SpacingExtra

/**
 * UC BOS DURUM, ayni metinle gecistirilemez.
 *
 * Ilk gun kullanici "ne yapacagimi bilmiyorum" halinde: yol gosterilmeli ve
 * baslamanin en ucuz iki yolu (kategoriler, pano) onune konmali.
 *
 * Dongu ortasi ise uygulamanin hayatinin COGUNU gecirdigi hal - liste bos
 * cunku alisveris yeni bitti. Burada ayni "hos geldin" metnini gostermek
 * uygulamayi olu gosterir; kullanici bir sey yapmadi, dogru calisti.
 */
@Composable
internal fun EmptyState(
    tur: EmptyKind,
    kategoriler: List<Category>,
    panoVar: Boolean,
    onKategori: (Category) -> Unit,
    onPano: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = SpacingExtra.emptyStateBlock),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = when (tur) {
                EmptyKind.ILK_GUN -> "Listeye başlayalım."
                // "Bos" degil "temiz": ayni gercek, cok farkli his.
                EmptyKind.DONGU_ORTASI -> "Liste tertemiz."
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = when (tur) {
                EmptyKind.ILK_GUN ->
                    "Aşağıya yazabilirsin — \"2 kg elma\" gibi miktarı da anlıyor.\n" +
                        "Ya da bir reyondan başla."
                EmptyKind.DONGU_ORTASI ->
                    "Bir şey bitince buraya yaz, unutmayalım."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.lg),
        )

        // Kategori cipleri YALNIZCA ilk gun: dongu ortasinda kullanici zaten
        // ne yapacagini biliyor, 12 cip gostermek gurultu olur.
        if (tur == EmptyKind.ILK_GUN && kategoriler.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = Spacing.md,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(kategoriler, key = { it.id }) { kategori ->
                    SuggestionChip(
                        label = kategori.name,
                        reason = "reyon",
                        onClick = { onKategori(kategori) },
                    )
                }
            }
        }

        // Pano butonu SADECE panoda liste varsa. Her zaman gostermek
        // dokunulmayan bir butona alan ayirmak demek.
        if (panoVar) {
            NeydiButton("Panodakileri ekle", onPano)
        }
    }
}

// --- Preview ---------------------------------------------------------------

private fun fold(id: String, ad: String) = Category(id, ad, 0, 0xFF6E8B3D)

@PreviewLightDark
@Composable
private fun EmptyFirstDayPreview() = NeydiPreview {
    EmptyState(
        tur = EmptyKind.ILK_GUN,
        kategoriler = listOf(fold("1", "Meyve-Sebze"), fold("2", "Fırın-Ekmek"), fold("3", "Süt-Kahvaltılık")),
        panoVar = true,
        onKategori = {},
        onPano = {},
    )
}

@PreviewLightDark
@Composable
private fun EmptyMidCyclePreview() = NeydiPreview {
    EmptyState(
        tur = EmptyKind.DONGU_ORTASI,
        kategoriler = emptyList(),
        panoVar = false,
        onKategori = {},
        onPano = {},
    )
}
