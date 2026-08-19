package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
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
import com.neydi.app.ui.theme.pressable

/** Katalog dugmesi kare: alanla ayni yukseklik, ayni kose (maket 52x52). */
private val CATALOG_BUTTON = 52.dp

/** Yazarken cikan tamamlama cipi - motor cipinden KISA (36 / 40). */
private val COMPLETION_CHIP = 36.dp

/**
 * Alta sabit hizli ekleme alani - YAZMA YOLU (karar 64, yol 1).
 *
 * ALTA SABIT, ustte degil: bas parmak orada ve klavye acilinca alan klavyenin
 * hemen ustunde kalir. Ustte olsaydi her eklemede goz ekranin iki ucu arasinda
 * gidip gelirdi.
 *
 * ## Hedef, dokununca YERINDE alana donusuyor
 *
 * Bir tur once burasi bir BUTONDU (karar 63) ve buton Ekle sheet'ini aciyordu.
 * O karar geri alindi - defterden dustu - cunku olctugumuz sey ortadaydi:
 * yazarak ekleme 1 dokunustan 3'e cikmisti (dokun, sheet, arama alanina dokun),
 * yani on kalemlik bir turda otuz fazladan dokunus. Karar 64 teshisi de
 * duzeltiyor: *"iki yol ayni isi yapmiyor - biri BILDIGINI yazmak, oteki NE
 * ALACAGINI hatirlamak"*.
 *
 * Simdi tek bir alan var ve iki hali kendi icinde tasiyor: dinlenirken
 * `Ne lazım?` hedefi gibi gorunuyor, dokununca beyaza donup yesil odak
 * cercevesini aliyor. Ayni yerde, ayni yukseklikte - hicbir sey acilmiyor,
 * bir sey DONUSUYOR.
 *
 * ## Klavye kurali bozulmuyor
 *
 * *"Hicbir ekran acilirken klavye acmaz"* hala gecerli: klavyeyi EKRAN degil
 * kullanicinin dokunusu aciyor. Karar 64 bunu ayrica yaziyor.
 *
 * ## Enter ekler, alan ACIK KALIR
 *
 * Seri ekleme icin: liste yazarken kullanici alti kalemi arka arkaya yaziyor.
 * Her eklemeden sonra klavyeyi kapatmak listeyi doldurmayi iki katina cikarir.
 *
 * ## Serit yine tek, ama iki modu geri geldi
 *
 * Girdi BOSKEN motorun onerileri ("Yumurta · 14 gun oldu"), kullanici yazarken
 * otomatik tamamlama. Karar 63 ikinci modu oldurmustu; kok alani geri gelince
 * modu da geri geliyor. Serit ALANIN USTUNDE: dokunulacak sey parmagin geldigi
 * yerde olmali, klavyenin arkasinda degil.
 */
@Composable
fun QuickAdd(
    input: String,
    suggestions: List<CatalogSeed>,
    engineSuggestions: List<Suggestion>,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSuggestionSelected: (CatalogSeed) -> Unit,
    onEngineSuggestion: (Suggestion) -> Unit,
    onOpenCatalog: () -> Unit,
    modifier: Modifier = Modifier,
    /** Kesif sheet'indeki "Kendim yazayım" buraya odak istiyor (karar 64). */
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val extras = LocalNeydiExtraColors.current
    var focused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // MOD SECIMI GIRDININ DOLULUGUNA bagli, odaga DEGIL: bos bir alana
        // odaklanmis kullanici hala "ne alacagimi hatirlat" halinde.
        if (input.isBlank() && engineSuggestions.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(engineSuggestions, key = { it.productId }) { s ->
                    // Motor cipinin gerekcesi DUZ TURKCE ("14 gun oldu") -
                    // gerekcesiz cip reklam gibi okunur.
                    SuggestionChip(
                        label = s.name,
                        reason = s.reasonText(),
                        onClick = { onEngineSuggestion(s) },
                    )
                }
            }
        } else if (suggestions.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(suggestions, key = { it.id }) { seed ->
                    CompletionChip(text = seed.name, onTap = { onSuggestionSelected(seed) })
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .padding(bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = SizesExtra.quickAddField)
                    .clip(NeydiExtraShapes.textField)
                    // ODAKLI HAL BEYAZ DEGIL `surface`: maket #FFFFFF ciziyor
                    // ama palet o degeri token'lamiyor (yalnizca alisveris
                    // modunun "surface beyaza cikar" kurali var). `surface`,
                    // dinlenen `surfaceVariant`tan bir kademe ASAGI - alan
                    // "yazilacak bir oyuk" gibi okunuyor ve fark iki temada da
                    // ayni yonde calisiyor.
                    .background(
                        if (focused) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .border(
                        width = if (focused) 2.dp else 1.dp,
                        color = if (focused) MaterialTheme.colorScheme.secondary else extras.hairline,
                        shape = NeydiExtraShapes.textField,
                    )
                    .padding(horizontal = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // IKON YALNIZCA DINLENIRKEN: odaklandiginda imlecin yani sira
                // bir `add` ikonu durmasi, alanin ne oldugunu ikinci kez
                // soylemek olurdu - klavye zaten acik.
                if (!focused) {
                    NeydiIcon(
                        icon = NeydiIcons.Add,
                        contentDescription = null,
                        size = 22.dp,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(10.dp))
                }
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focused = it.isFocused },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    // KLAVYE KAPANMIYOR: `onSubmit` satiri ekliyor ve alan
                    // acik kaliyor - seri ekleme karar 64'un kendi sarti.
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    decorationBox = { inner ->
                        if (input.isEmpty()) {
                            Text(
                                text = "Ne lazım?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        inner()
                    },
                )
            }

            // KATALOG DUGMESI YALNIZCA ODAKLIYKEN: dinlenen hal tek bir hedef
            // olmali - iki dugme yan yana, kullaniciya "hangisi" diye
            // sordururdu. Kullanici alana dokununca iki yol da onunde.
            if (focused) {
                Box(
                    modifier = Modifier
                        .size(CATALOG_BUTTON)
                        .clip(NeydiExtraShapes.textField)
                        .pressable(onTap = onOpenCatalog)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, extras.hairline, NeydiExtraShapes.textField),
                    contentAlignment = Alignment.Center,
                ) {
                    NeydiIcon(
                        icon = NeydiIcons.GridView,
                        contentDescription = "katalog",
                        size = 24.dp,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Otomatik tamamlama cipi - motor cipinden AYRI bir sey.
 *
 * Motor cipi bir ONERI ve gerekcesini tasiyor ("14 gun oldu"); bu cip
 * yazilani TAMAMLIYOR ve gerekcesi yok. Maket ikisini ayri ciziyor: motor
 * cipi 40dp ve dolgulu, tamamlama cipi 36dp ve zemin rengi.
 */
@Composable
private fun CompletionChip(text: String, onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = COMPLETION_CHIP)
            .clip(NeydiExtraShapes.pill)
            .pressable(onTap = onTap)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, LocalNeydiExtraColors.current.hairline, NeydiExtraShapes.pill)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// --- Preview ---------------------------------------------------------------

private fun seed(id: String, name: String, unit: String) = CatalogSeed(
    id = id, name = name, matchKey = name.lowercase(),
    categoryId = "temel-gida", commonalityRank = 1, defaultUnit = unit,
)

/** SERIDIN ONERI MODU: girdi bos, motor konusuyor. */
@PreviewLightDark
@Composable
private fun QuickAddIdlePreview() = NeydiPreview {
    QuickAdd(
        input = "",
        suggestions = emptyList(),
        engineSuggestions = listOf(
            Suggestion("p1", "Yumurta", 1.9, daysSince = 14, intervalDays = 10, forgottenLastTrip = false),
            Suggestion("p2", "Çay", 1.6, daysSince = 21, intervalDays = 14, forgottenLastTrip = false),
        ),
        onInputChange = {}, onSubmit = {}, onSuggestionSelected = {},
        onEngineSuggestion = {}, onOpenCatalog = {},
    )
}

/** SERIDIN TAMAMLAMA MODU: kullanici yaziyor - karar 64 ile geri geldi. */
@PreviewLightDark
@Composable
private fun QuickAddTypingPreview() = NeydiPreview {
    QuickAdd(
        input = "yum",
        suggestions = listOf(seed("1", "Yumurta", "adet"), seed("2", "Yumuşatıcı", "adet")),
        engineSuggestions = emptyList(),
        onInputChange = {}, onSubmit = {}, onSuggestionSelected = {},
        onEngineSuggestion = {}, onOpenCatalog = {},
    )
}
