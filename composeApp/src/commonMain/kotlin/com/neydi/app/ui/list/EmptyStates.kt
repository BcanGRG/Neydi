package com.neydi.app.ui.list

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.neydi.app.data.db.Category
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.SuggestionChip
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Sizes
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
    kind: EmptyKind,
    categories: List<Category>,
    hasClipboard: Boolean,
    onCategory: (Category) -> Unit,
    onClipboard: () -> Unit,
    modifier: Modifier = Modifier,
    /** *"Son alışveriş 3 gün önce, 642 TL."* - yoksa null. */
    lastTripLine: String? = null,
    /** Dongu ortasinin hayalet butonu; hic kapanmis gezi yoksa null. */
    onAddFromLastTrip: (() -> Unit)? = null,
) {
    // TASARIM SOLA HIZALI, ORTALI DEGIL. Ortalanan metin bir "karsilama
    // ekrani" gibi okunuyor; sola hizali metin ekranin normal bir hali gibi
    // okunuyor - ve dongu ortasi tam olarak normal bir hal.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = SpacingExtra.emptyStateBlock),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = when (kind) {
                EmptyKind.ILK_GUN -> "Neyle başlayalım?"
                // "Bos" kelimesi tasarimin kendi secimi ve bilincli: metnin
                // devami ("Son alisveris ... TL") o boslugun sebebini hemen
                // soyluyor, yani bosluk bir eksiklik degil bir sonuc.
                EmptyKind.DONGU_ORTASI -> "Liste boş"
            },
            // Fraunces. Tasarimda bos durum basliklari Fraunces'in gorevli
            // oldugu dort yerden biri (24sp alt siniri).
            style = when (kind) {
                EmptyKind.ILK_GUN -> MaterialTheme.typography.headlineMedium
                EmptyKind.DONGU_ORTASI -> MaterialTheme.typography.displaySmall
            },
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = when (kind) {
                EmptyKind.ILK_GUN -> "Dokun, listeye düşsün."
                EmptyKind.DONGU_ORTASI -> lastTripLine ?: "Bir şey bitince buraya yaz, unutmayalım."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // DONGU ORTASININ TEK HAYALET BUTONU. Tasarim burada tek bir cikis
        // yolu birakiyor: bos ekranin en ucuz cevabi "gecen sefer ne
        // aldiysan onlar". Sifir illustrasyon, sifir CTA yigini.
        if (kind == EmptyKind.DONGU_ORTASI && onAddFromLastTrip != null) {
            NeydiButton(
                text = "Geçen sefer aldıklarını ekle",
                onClick = onAddFromLastTrip,
                container = MaterialTheme.colorScheme.surface,
                content = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.border(
                    Sizes.hairline,
                    MaterialTheme.colorScheme.outline,
                    NeydiExtraShapes.pill,
                ),
            )
        }

        // Kategori cipleri YALNIZCA ilk gun: dongu ortasinda kullanici zaten
        // ne yapacagini biliyor, 12 cip gostermek gurultu olur.
        if (kind == EmptyKind.ILK_GUN && categories.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = Spacing.md,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(categories, key = { it.id }) { category ->
                    SuggestionChip(
                        label = category.name,
                        reason = "reyon",
                        onClick = { onCategory(category) },
                    )
                }
            }
        }

        // Pano butonu SADECE panoda liste varsa. Her zaman gostermek
        // dokunulmayan bir butona alan ayirmak demek.
        //
        // METIN TASARIMDAN: "WhatsApp'tan listeni yapistir". Onceki hali
        // "Panodakileri ekle" idi - dogru ama soyut; tasarim listenin NEREDEN
        // geldigini soyluyor, cunku kullanicinin kafasindaki sey pano degil
        // esinin gonderdigi mesaj.
        if (hasClipboard) {
            NeydiButton(
                text = "WhatsApp'tan listeni yapıştır",
                onClick = onClipboard,
                container = MaterialTheme.colorScheme.surface,
                content = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.border(
                    Sizes.hairline,
                    MaterialTheme.colorScheme.outline,
                    NeydiExtraShapes.pill,
                ),
            )
        }
    }
}

// --- Preview ---------------------------------------------------------------

private fun fold(id: String, name: String) = Category(id, name, 0, 0xFF6E8B3D)

@PreviewLightDark
@Composable
private fun EmptyFirstDayPreview() = NeydiPreview {
    EmptyState(
        kind = EmptyKind.ILK_GUN,
        categories = listOf(fold("1", "Meyve-Sebze"), fold("2", "Fırın-Ekmek"), fold("3", "Süt-Kahvaltılık")),
        hasClipboard = true,
        onCategory = {},
        onClipboard = {},
    )
}

@PreviewLightDark
@Composable
private fun EmptyMidCyclePreview() = NeydiPreview {
    EmptyState(
        kind = EmptyKind.DONGU_ORTASI,
        categories = emptyList(),
        hasClipboard = false,
        onCategory = {},
        onClipboard = {},
    )
}
