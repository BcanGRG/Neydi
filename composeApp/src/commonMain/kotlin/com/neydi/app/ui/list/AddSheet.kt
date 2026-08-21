package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.Category
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.SuggestionChip
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.NeydiShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/** Filtre ve "nadir" cipleri - maket 36px. */
private val FILTER_CHIP = 36.dp

/** Izgara kutucugu - maket `height:56px; border-radius:20px`. */
private val GRID_TILE = 56.dp

/** Arama alani ve alt kacis - maket 48px. */
private val FIELD = 48.dp

/**
 * Kesif sheet'i - ekleme akisinin IKINCI yolu (karar 64, yol 2).
 *
 * ## Reyon kutucuklari OLDU
 *
 * Sheet 12 reyonu 56x56 kutucuk izgarasi olarak ciziyordu ve kullanicinin ilk
 * IKI dokunusunda ekranda hicbir urun adi gorunmuyordu: once kutucuk, sonra o
 * reyonun urunleri. Kullanici bunu cihazda *"hem tasarimi cok kesik kesik
 * duruyor hem de ux acisindan hic kullanisli degil"* diye bildirdi; olcum de
 * ayni yone bakiyordu - kutucuklarin ikisi ayni iki harfe dusuyordu
 * (Temel Gida ve Temizlik, ikisi de "TE") ve reyon basina "18 urun" sayacinin
 * veri karsiligi hic yoktu.
 *
 * Karar 64 yuzeyin isini yeniden tanimladi: *"kesif yuzeyinin isi urun
 * gostermek"*. Artik sheet DOGRUDAN urunle aciliyor; reyon bir hedef degil,
 * ustteki yatay serit icinde bir FILTRE.
 *
 * ## Iki bolum, iki farkli hedef boyu
 *
 * "En sık aldıkların" iki sutunlu kutucuk izgarasi, "Nadir aldıkların" sarilan
 * cip. Fark tesadufi degil: sik dokunulan seyin hedefi buyuk olmali.
 *
 * ## Alt kacis iki yolu birbirine BAGLIYOR
 *
 * "Kendim yazayım" sheet'i kapatip kokteki yazma alanini aciyor - karar 64'un
 * kendi cumlesi. Once bu buton yalnizca sheet'i kapatiyordu (ve ondan da once
 * hicbir sey yapmiyordu); simdi kullaniciyi otekinin ortasina birakiyor.
 */
@Composable
fun AddSheetContent(
    /** Gezinme cubugu yuksekligi - sheet DISINDA okunup buraya geciliyor. */
    bottomPadding: Dp,
    categories: List<Category>,
    selected: Category?,
    body: DiscoveryBody,
    onFilter: (Category?) -> Unit,
    onPick: (DiscoveryItem) -> Unit,
    onFreeText: () -> Unit,
    modifier: Modifier = Modifier,
    /** Bu sheet acik kalirken eklenen urun sayisi (tasarim: "3 urun eklendi"). */
    addedCount: Int = 0,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    results: List<CatalogSeed> = emptyList(),
    onPickResult: (CatalogSeed) -> Unit = {},
    /** Zaten listede olan urunlerin `matchKey`leri (tasarim karari 12). */
    inList: Set<String> = emptySet(),
) {
    val extras = LocalNeydiExtraColors.current

    Column(modifier = modifier.fillMaxWidth().padding(bottom = bottomPadding)) {
        // BASLIK BLOGU SABIT: arama ve sayac kaydirmayla kaybolmamali.
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .padding(bottom = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Ekle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (addedCount > 0) {
                    // "N urun eklendi" - sheet kapanmadigi icin kullanici
                    // listeye bakamiyor; sayac tek geri bildirim.
                    Text(
                        text = "$addedCount ürün eklendi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            SheetSearch(value = query, onChange = onQueryChange, extras.hairline)
        }

        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))

        if (query.isNotBlank()) {
            // ARAMA HER SEYI EZIYOR: yaziyorsa aradigi sey hangi reyonda
            // oldugundan bagimsiz. Sonuc yoksa liste bos kaliyor ve alttaki
            // kacis satiri aranan kelimeyi tasiyor.
            LazyColumn(
                Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(results, key = { it.id }) { seed ->
                    SuggestionChip(
                        label = seed.name,
                        reason = seed.defaultUnit,
                        checked = seed.matchKey in inList,
                        onClick = { onPickResult(seed) },
                    )
                }
            }
        } else {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                FilterStrip(categories = categories, selected = selected, onFilter = onFilter)

                if (body.frequent.isNotEmpty()) {
                    // BASLIK GOVDENIN KAYNAGINA GORE: "En sık aldıkların"
                    // yalnizca gercekten alinmis urunler icin dogru bir cumle.
                    // Ilk gun govde katalogdan geliyor ve o cumle yalan olurdu.
                    SectionLabel(if (body.fromCatalog) "Sık alınanlar" else "En sık aldıkların")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.heightIn(max = GRID_TILE * 3 + Spacing.sm * 2),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        items(body.frequent.size) { i ->
                            val item = body.frequent[i]
                            DiscoveryTile(
                                item = item,
                                inList = item.matchKey in inList,
                                onTap = { onPick(item) },
                            )
                        }
                    }
                }

                if (body.rare.isNotEmpty()) {
                    SectionLabel("Nadir aldıkların")
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        body.rare.take(RARE_LIMIT).forEach { item ->
                            RareChip(text = item.name, onTap = { onPick(item) })
                        }
                    }
                }
            }
        }

        // ALT KACIS: katalog 245 urun, Turkiye'deki her urun degil. Katalogda
        // olmayan bir sey isteyen kullanici burada tikanirsa sheet bir duvar
        // olur.
        Column(
            Modifier.fillMaxWidth().padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = FIELD)
                    .clip(NeydiExtraShapes.pill)
                    .pressable(onTap = onFreeText)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, NeydiExtraShapes.pill),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NeydiIcon(
                    icon = NeydiIcons.Keyboard,
                    contentDescription = null,
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (query.isNotBlank()) "\"$query\" ekle" else "Kendim yazayım",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/** Reyon filtresi - ilk cip her zaman "Tümü". */
@Composable
private fun FilterStrip(categories: List<Category>, selected: Category?, onFilter: (Category?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        item { FilterChip(text = "Tümü", selected = selected == null, onTap = { onFilter(null) }) }
        items(categories, key = { it.id }) { c ->
            FilterChip(text = c.name, selected = selected?.id == c.id, onTap = { onFilter(c) })
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onTap: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = FILTER_CHIP)
            .clip(NeydiExtraShapes.pill)
            .pressable(onTap = onTap)
            // SECILI CIP YESIL DOLGU: maket `background:#3F6B54` yani
            // `secondary`. Secim bir ONAY hali ve renk sozlugu yesili ona
            // ayirmis (karar 42).
            .background(
                if (selected) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.surface,
            )
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, LocalNeydiExtraColors.current.hairline, NeydiExtraShapes.pill),
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Izgara kutucugu: ad (listede varsa dolu check_circle ile) + birim. */
@Composable
private fun DiscoveryTile(item: DiscoveryItem, inList: Boolean, onTap: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = GRID_TILE)
            .clip(NeydiShapes.large)
            // ISARETLI KUTUCUK PASIF: ayni satiri iki kez eklemek bir is degil.
            .pressable(enabled = !inList, onTap = onTap)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, LocalNeydiExtraColors.current.hairline, NeydiShapes.large)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (inList) {
                NeydiIcon(
                    icon = NeydiIcons.CheckCircle,
                    contentDescription = "listede var",
                    size = 16.dp,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = item.unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RareChip(text: String, onTap: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = FILTER_CHIP)
            .clip(NeydiExtraShapes.pill)
            .pressable(onTap = onTap)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, LocalNeydiExtraColors.current.hairline, NeydiExtraShapes.pill)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SheetSearch(value: String, onChange: (String) -> Unit, hairline: androidx.compose.ui.graphics.Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = FIELD)
            .clip(NeydiExtraShapes.textField)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, hairline, NeydiExtraShapes.textField)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NeydiIcon(
            icon = NeydiIcons.Search,
            contentDescription = null,
            size = 22.dp,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BasicTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = "Ürün ara",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                inner()
            },
        )
    }
}

/**
 * "Nadir aldıkların" cip sayisi tavani.
 *
 * Bolumun isi bir HATIRLATMA, tam bir envanter degil; yuzlerce cip sarilirsa
 * sheet bir arsive donusur ve arama alani zaten oradadir.
 */
private const val RARE_LIMIT = 24

// --- Preview ---------------------------------------------------------------

private fun k(id: String, name: String) = Category(id, name, 0, 0xFF6E8B3D)
private fun d(id: String, name: String, unit: String) = DiscoveryItem(id, name, unit, name.lowercase())

@PreviewLightDark
@Composable
private fun AddSheetPreview() = NeydiPreview {
    AddSheetContent(
        bottomPadding = 0.dp,
        categories = listOf(k("1", "Meyve-Sebze"), k("2", "Süt-Kahvaltılık"), k("3", "Temel Gıda")),
        selected = null,
        body = DiscoveryBody(
            frequent = listOf(
                d("1", "Ekmek", "adet"), d("2", "Süt", "lt"),
                d("3", "Yumurta", "adet"), d("4", "Domates", "kg"),
            ),
            rare = listOf(d("5", "Labne", "adet"), d("6", "Kaymak", "adet")),
        ),
        onFilter = {}, onPick = {}, onFreeText = {},
        addedCount = 3,
        inList = setOf("süt"),
    )
}
