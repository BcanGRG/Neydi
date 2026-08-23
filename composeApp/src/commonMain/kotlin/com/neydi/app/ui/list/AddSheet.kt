package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.neydi.app.ui.components.StepperMetrics
import com.neydi.app.ui.components.QuantityStepper
import com.neydi.app.data.quantityLabel
import com.neydi.app.data.decrementQuantity
import com.neydi.app.data.incrementQuantity
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import com.neydi.app.ui.theme.Motion
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
    /**
     * Zaten listede olan urunler: `matchKey` -> miktar etiketi ("1 kg").
     *
     * ONCE YALNIZ ANAHTAR KUMESIYDI (karar 12: isaretli hucre pasif). Karar
     * 109 hucreye miktari da yazdiriyor - *"1 kg listede"* - cunku pasiflik
     * artik bir gerekceye kavustu: hucre pasif, cunku miktarin kendi evi var.
     * Miktari yazmadan pasiflik hala "burada bir sey var ama ne kadar
     * bilmiyorsun" demek olurdu.
     */
    inList: Map<String, String> = emptyMap(),
    /** Uzun dokunusla secilen miktarla ekleme (karar 109). */
    onPickWithQuantity: (DiscoveryItem, Double) -> Unit = { _, _ -> },
) {
    val extras = LocalNeydiExtraColors.current

    // HUCRE SAYACININ SAHIBI SHEET, KUTUCUK DEGIL (karar 109).
    //
    // `LazyVerticalGrid` gorunmeyen kutucugu geri donusturuyor; kutucuk-yerel
    // bir `remember` kaydirmada olurdu ve kullanicinin sectigi miktar hic
    // eklenmeden kaybolurdu. Ayrica tek sahip olmadan "yalniz bir hucre acik"
    // kurali yazilamaz.
    var openCell by remember { mutableStateOf<CellStepper?>(null) }
    var cellSeq by remember { mutableStateOf(0L) }

    // "BIRAKINCA O MIKTARLA EKLENIR" - ve birakmak UC SANIYE SUSMAK demek.
    //
    // Tasarimin cumlesi harfiyen "parmagini kaldirinca" gibi okunuyor ama o
    // imkansiz: sayacin `+` ve `-` dugmeleri var, yani parmak zaten kalkmis
    // olmali. Kalan tek anlam "elini cektiginde" - satirdaki sayacin ayni
    // uc saniyesi (karar 107). Iki yuzey ayni sureyi paylasiyor.
    LaunchedEffect(openCell) {
        val open = openCell ?: return@LaunchedEffect
        delay(Motion.QTY_STEPPER_MS.toLong())
        onPickWithQuantity(open.item, open.count)
        openCell = null
    }

    fun commitCell() {
        openCell?.let { onPickWithQuantity(it.item, it.count) }
        openCell = null
    }

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
                    // listeye bakamiyor; sayac tek METIN geri bildirimi.
                    //
                    // DEGISIMDE 150 ms OLCEK VURGUSU (karar 89): animasyonsuz
                    // artan bir rakam, ekranin karsi kosesinde, parmagin ve
                    // gozun ortadaki izgarada oldugu bir anda hicbir sey
                    // soylemiyordu. Ayni hareket adet rozetinde de var.
                    val pulse = remember { Animatable(1f) }
                    LaunchedEffect(addedCount) {
                        pulse.snapTo(Motion.PULSE_SCALE)
                        pulse.animateTo(1f, tween(Motion.PULSE_MS))
                    }
                    Text(
                        text = "$addedCount ürün eklendi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.graphicsLayer {
                            scaleX = pulse.value
                            scaleY = pulse.value
                        },
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
                                inListLabel = inList[item.matchKey],
                                stepper = openCell?.takeIf { it.item.matchKey == item.matchKey },
                                onTap = { if (openCell != null) commitCell() else onPick(item) },
                                onLongPress = {
                                    openCell = CellStepper(item, 1.0, ++cellSeq)
                                },
                                onStep = { up ->
                                    openCell = openCell?.let { open ->
                                        open.copy(
                                            count = if (up) {
                                                incrementQuantity(open.count, item.unit)
                                            } else {
                                                decrementQuantity(open.count, item.unit)
                                            },
                                            seq = ++cellSeq,
                                        )
                                    }
                                },
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
                            RareChip(
                                text = item.name,
                                inList = item.matchKey in inList,
                                onTap = { onPick(item) },
                            )
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

/**
 * Acik olan hucre sayaci (karar 109) - hangi urun, kac tane, kacinci dokunus.
 *
 * [seq] satirdaki sayacla ayni isi yapiyor: her dokunus uc saniyeyi yeniden
 * kuruyor ve bunun tek yolu eski zamanlayicinin kendini gecersiz bilmesi.
 */
private data class CellStepper(val item: DiscoveryItem, val count: Double, val seq: Long)

/** Izgara kutucugu: ad (listede varsa dolu check_circle ile) + birim ya da sayac. */
@Composable
private fun DiscoveryTile(
    item: DiscoveryItem,
    inListLabel: String?,
    stepper: CellStepper?,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onStep: (Boolean) -> Unit,
) {
    val inList = inListLabel != null
    Column(
        Modifier
            .fillMaxWidth()
            // IZGARA RITMI VE HUCRE BOYU DEGISMIYOR (karar 109): sayac
            // acildiginda kutucuk buyurse butun izgara zipliyor ve
            // kullanicinin parmaginin altindaki hedef kayiyor. Sayac ikinci
            // satirin YERINE geciyor, altina degil.
            .heightIn(min = GRID_TILE)
            .clip(NeydiShapes.large)
            // ISARETLI KUTUCUK PASIF ama SONMUYOR (karar 89).
            //
            // `pressable(enabled = false)` %38 opakliga dusuruyordu ve o
            // "devre disi" sozlugunun rengi - yapilmis bir isi YAPILAMAZ is
            // gibi gosteriyordu. Pasiflik dokunma tarafinda kaliyor, gorunum
            // tarafinda "yapildi" diyor: successSurface dolgu + isaret.
            // ISARETLI HUCRE HER IKI JESTE DE PASIF (karar 12 + 109).
            //
            // Bir ara uzun dokunusu acik birakmistim - "miktari buradan da
            // secebilsin" diye. Yanlisti ve karar 109'un kendi cumlesi
            // soyluyor: *"hucre pasif, cunku miktarin kendi evi var."*
            // Listedeki bir urunun miktari satirdan ya da Urun Detayi'ndan
            // degisiyor; sheet'ten degistirmek ucuncu bir ev acmak olurdu.
            //
            // Ve calismazdi: ekleme artik idempotent (karar 109), yani zaten
            // listede olan bir urune secilen miktarla "eklemek" hicbir sey
            // yapmazdi. Kullanici sayaci cevirir, birakir, hicbir sey olmaz.
            .pressable(
                enabled = !inList,
                dimWhenDisabled = false,
                onLongPress = onLongPress,
                onTap = onTap,
            )
            .background(
                if (stepper != null) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else if (inList) {
                    LocalNeydiExtraColors.current.successSurface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            )
            .border(
                1.dp,
                // ACIK SAYAC KENDINI ISARETLIYOR: maket kiremit kenarlik
                // ciziyor ve sebebi jestin kendisi - uc saniyelik bir kontrol,
                // hangi hucreye ait oldugunu kendisi soylemek zorunda.
                if (stepper != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    LocalNeydiExtraColors.current.hairline
                },
                NeydiShapes.large,
            )
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
        // ALT SATIR UC HALDEN BIRI: sayac, "1 kg listede", ya da birim.
        //
        // Karar 66 alt satiri BIRIME ayirmisti ve o kural sadeceki hal icin
        // hala gecerli; karar 109 ustune iki hal daha ekledi ve ucu de AYNI
        // satirda duruyor - kutucugun boyu degismesin diye.
        when {
            stepper != null -> QuantityStepper(
                metrics = StepperMetrics.Cell,
                onDecrement = { onStep(false) },
                onIncrement = { onStep(true) },
            ) {
                Text(
                    text = quantityLabel(stepper.count, item.unit),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }

            inListLabel != null -> Text(
                text = inListLabel,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LocalNeydiExtraColors.current.success,
                maxLines = 1,
            )

            else -> Text(
                text = item.unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * "Nadir aldiklarin" cipi - KARAR 12'NIN UCUNCU YUZEYI.
 *
 * Izgara kutucugu ve arama sonucu cipi isareti bastan beri tasiyordu; bu cip
 * tasimiyordu. Sonucu sessiz bir yalandi: kullanici ayni cipe uc kez
 * dokununca urunun ADEDI uce cikiyor, cipte hicbir sey degismiyor ve
 * sheet'in sayaci *"3 ürün eklendi"* yaziyor - oysa uc urun degil bir urunun
 * adedi artmis.
 *
 * Karar 12'nin cumlesi zaten yuzeyden bagimsiz: *"Isaret 'bu listede var'
 * demek… Pasif olmasi da ayni seyi soyluyor - ayni satiri iki kez eklemek bir
 * is degil."*
 */
@Composable
private fun RareChip(text: String, inList: Boolean, onTap: () -> Unit) {
    Row(
        Modifier
            .heightIn(min = FILTER_CHIP)
            .clip(NeydiExtraShapes.pill)
            .pressable(enabled = !inList, onTap = onTap)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, LocalNeydiExtraColors.current.hairline, NeydiExtraShapes.pill)
            .padding(horizontal = 14.dp),
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

private fun k(id: String, name: String) = Category(id, name, 0)
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
        inList = mapOf("süt" to "1 L listede"),
    )
}
