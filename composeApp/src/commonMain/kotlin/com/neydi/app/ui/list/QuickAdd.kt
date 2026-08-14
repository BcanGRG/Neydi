package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.SuggestionChip
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.Spacing

/**
 * Alta sabit hizli ekleme alani.
 *
 * ALTA SABIT, ustte degil: bas parmak orada ve klavye acilinca alan klavyenin
 * hemen ustunde kalir. Ustte olsaydi her eklemede goz ekranin iki ucu arasinda
 * gidip gelirdi.
 *
 * ONERILER SKORA GORE sirali (alfabetik DEGIL) - siralamayi ViewModel yapiyor,
 * burasi yalnizca ciziyor.
 *
 * Material3 TextField DEGIL BasicTextField: M3'un metin alani kendi kapsayicisini
 * ve etkilesim davranisini getiriyor; bu tasarimda kapsayici ve basili hal
 * temadan gelmek zorunda (bkz. calisma sozlesmesi, Material3 Surface kurali).
 */
@Composable
fun QuickAdd(
    girdi: String,
    oneriler: List<CatalogSeed>,
    onGirdiDegisti: (String) -> Unit,
    onEkle: (String) -> Unit,
    onOneriSecildi: (CatalogSeed) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalNeydiExtraColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Oneri seridi ALANIN USTUNDE: dokunulacak sey parmagin geldigi
        // yerde olmali, klavyenin arkasinda degil.
        if (oneriler.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = Spacing.md,
                    vertical = Spacing.sm,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(oneriler, key = { it.id }) { seed ->
                    SuggestionChip(
                        label = seed.name,
                        reason = seed.defaultUnit,
                        onClick = { onOneriSecildi(seed) },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .padding(bottom = Spacing.sm)
                .heightIn(min = SizesExtra.quickAddField)
                .clip(NeydiExtraShapes.textField)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, extras.hairline, NeydiExtraShapes.textField)
                .padding(horizontal = Spacing.md),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = girdi,
                onValueChange = onGirdiDegisti,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(
                    MaterialTheme.colorScheme.primary,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                // Klavye kapanMIYOR: pes pese ekleme normal davranis.
                // Her eklemeden sonra klavyeyi kapatmak listeyi doldurmayi
                // iki kat yavaslatirdi.
                keyboardActions = KeyboardActions(onDone = { onEkle(girdi) }),
                decorationBox = { icerik ->
                    if (girdi.isEmpty()) {
                        Text(
                            text = "Ne lazım? \"2 kg elma\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    icerik()
                },
            )
        }
    }
}

// --- Preview ---------------------------------------------------------------

private fun seed(id: String, ad: String, birim: String) =
    CatalogSeed(id = id, name = ad, matchKey = ad.lowercase(), categoryId = "temel-gida", commonalityRank = 1, defaultUnit = birim)

@PreviewLightDark
@Composable
private fun QuickAddEmptyPreview() = NeydiPreview {
    QuickAdd("", emptyList(), {}, {}, {})
}

@PreviewLightDark
@Composable
private fun QuickAddWithSuggestionsPreview() = NeydiPreview {
    QuickAdd(
        girdi = "ek",
        oneriler = listOf(
            seed("1", "Ekmek", "adet"),
            seed("2", "Tam Buğday Ekmek", "adet"),
            seed("3", "Ekşi Maya Ekmek", "adet"),
        ),
        onGirdiDegisti = {},
        onEkle = {},
        onOneriSecildi = {},
    )
    Box(Modifier.height(Spacing.sm))
}
