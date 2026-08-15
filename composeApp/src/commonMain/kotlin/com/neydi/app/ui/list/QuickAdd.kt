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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import com.neydi.app.data.suggest.Suggestion
import com.neydi.app.data.suggest.reasonText
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
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
    input: String,
    suggestions: List<CatalogSeed>,
    engineSuggestions: List<Suggestion>,
    onInputChange: (String) -> Unit,
    onAdd: (String) -> Unit,
    onSuggestionSelected: (CatalogSeed) -> Unit,
    onEngineSuggestion: (Suggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalNeydiExtraColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        // TEK SERIT, IKI MOD (F6.3): girdi BOSKEN motorun onerileri, kullanici
        // yazarken otomatik tamamlama. Iki ayri serit ust uste binerdi; modu
        // girdinin bos olup olmamasi seciyor. Serit ALANIN USTUNDE: dokunulacak
        // sey parmagin geldigi yerde olmali, klavyenin arkasinda degil.
        //
        // Motor cipinin gerekcesi DUZ TURKCE ("14 gun oldu") - gerekcesiz cip
        // reklam gibi okunur. Otomatik tamamlama cipi ise birim gosteriyor;
        // o bir oneri degil, yazilani tamamlama.
        val showEngine = input.isBlank() && engineSuggestions.isNotEmpty()
        if (showEngine) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = Spacing.md,
                    vertical = Spacing.sm,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(engineSuggestions, key = { it.productId }) { s ->
                    SuggestionChip(
                        label = s.name,
                        reason = s.reasonText(),
                        onClick = { onEngineSuggestion(s) },
                    )
                }
            }
        } else if (suggestions.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = Spacing.md,
                    vertical = Spacing.sm,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(suggestions, key = { it.id }) { seed ->
                    SuggestionChip(
                        label = seed.name,
                        reason = seed.defaultUnit,
                        onClick = { onSuggestionSelected(seed) },
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
            Row(verticalAlignment = Alignment.CenterVertically) {
            // TASARIMIN `add` IKONU (22sp, outline rengi). Alanin ne ise
            // yaradigini yer isaretinden ONCE soyluyor: klavye acildiginda
            // yer isareti kayboluyor ama ikon kaliyor.
            NeydiIcon(
                icon = NeydiIcons.Add,
                contentDescription = null,
                size = 22.dp,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = input,
                onValueChange = onInputChange,
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
                keyboardActions = KeyboardActions(onDone = { onAdd(input) }),
                decorationBox = { content ->
                    if (input.isEmpty()) {
                        Text(
                            text = "Ne lazım? \"2 kg elma\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    content()
                },
            )
            }
        }
    }
}

// --- Preview ---------------------------------------------------------------

private fun seed(id: String, name: String, unit: String) =
    CatalogSeed(id = id, name = name, matchKey = name.lowercase(), categoryId = "temel-gida", commonalityRank = 1, defaultUnit = unit)

@PreviewLightDark
@Composable
private fun QuickAddEmptyPreview() = NeydiPreview {
    QuickAdd(
        input = "",
        suggestions = emptyList(),
        engineSuggestions = emptyList(),
        onInputChange = {}, onAdd = {}, onSuggestionSelected = {}, onEngineSuggestion = {},
    )
}

/**
 * SERIDIN ONERI MODU: girdi bos, motor konusuyor - "Yumurta · 14 gun oldu".
 * Bu onizleme tek seridin iki modundan gorunmeyenini tutuyor; cihazda ancak
 * gunler arayla alisveris birikince cizilebilir.
 */
@PreviewLightDark
@Composable
private fun QuickAddEngineSuggestionsPreview() = NeydiPreview {
    QuickAdd(
        input = "",
        suggestions = emptyList(),
        engineSuggestions = listOf(
            Suggestion("p1", "Yumurta", 1.9, daysSince = 14, intervalDays = 10, forgottenLastTrip = false),
            Suggestion("p2", "Çay", 1.6, daysSince = 21, intervalDays = 14, forgottenLastTrip = false),
            Suggestion("p3", "Ekmek", 1.5, daysSince = 4, intervalDays = 3, forgottenLastTrip = true),
        ),
        onInputChange = {}, onAdd = {}, onSuggestionSelected = {}, onEngineSuggestion = {},
    )
}

@PreviewLightDark
@Composable
private fun QuickAddWithSuggestionsPreview() = NeydiPreview {
    QuickAdd(
        input = "ek",
        suggestions = listOf(
            seed("1", "Ekmek", "adet"),
            seed("2", "Tam Buğday Ekmek", "adet"),
            seed("3", "Ekşi Maya Ekmek", "adet"),
        ),
        engineSuggestions = emptyList(),
        onInputChange = {},
        onAdd = {},
        onSuggestionSelected = {},
        onEngineSuggestion = {},
    )
    Box(Modifier.height(Spacing.sm))
}
