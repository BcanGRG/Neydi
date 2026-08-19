package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.theme.Sizes
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import com.neydi.app.ui.theme.NeydiExtraShapes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.Category
import com.neydi.app.ui.components.CategoryTile
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.SuggestionChip
import com.neydi.app.ui.components.turkishInitials
import com.neydi.app.ui.theme.CategoryTint
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Ekle sheet (Ekran 2). SHEET, EKRAN DEGIL - liste arkada gorunur kalir.
 *
 * Neden onemli: kullanici bir seyi eklerken listede NE OLDUGUNU unutmamali.
 * Tam ekran bir "urun ekle" sayfasi baglami koparir ve geri donunce
 * "neredeydim" sorusu dogar. IA'nin temel kurali da bunu soyluyor: LISTE
 * uygulamanin kendisi, diger her sey onun uzerinde bir sapma.
 *
 * Iki katmanli: once reyon grid'i, reyon secilince o reyonun urun cipleri.
 * Cipler YAYGINLIGA gore sirali, alfabetik degil.
 */
@Composable
internal fun AddSheetContent(
    /** Gezinme cubugu yuksekligi - sheet DISINDA okunup buraya geciliyor. */
    bottomPadding: Dp,
    categories: List<Category>,
    selected: Category?,
    products: List<CatalogSeed>,
    onCategory: (Category) -> Unit,
    onBackToCategories: () -> Unit,
    onProduct: (CatalogSeed) -> Unit,
    onFreeText: () -> Unit,
    modifier: Modifier = Modifier,
    /** Bu sheet acik kalirken eklenen urun sayisi (tasarim: "3 urun eklendi"). */
    addedCount: Int = 0,
    /** Sheet ici arama metni (tasarim: "Urun ara"). */
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    /** Aramanin sonuclari; bos sorguda bos liste. */
    results: List<CatalogSeed> = emptyList(),
    /** Zaten listede olan urunlerin `matchKey`leri (tasarim karari 12). */
    inList: Set<String> = emptySet(),
) {
    // SHEET'IN YUKSEKLIGI SABIT, ICERIK ONU PAYLASIYOR.
    //
    // ## Sihirli sayi tutmadi ve bunu KENDI KDoc'u ongormustu
    //
    // Onceki cozum grid'e ekranin bir orani kadar tavan veriyordu
    // (`GRID_RATIO`) ve o sabitin KDoc'u aynen sunu yaziyordu: *"Bu bir SIHIRLI
    // SAYI, cozum degil. Baska ekran oraninda... yeniden tasar - ve SESSIZCE
    // tasar."* Tasti: SM-G975F'te ucuncu sira kutucuklari 70px yerine 22px'e
    // kirpildi, etiketleri hic cizilmedi ve kacis butonu `bounds=[0,0][0,0]`
    // dondu - yani DOKUNULAMAZ hale geldi. Katalogda olmayan bir urunu
    // eklemenin tek yolu o buton.
    //
    // ## Neden oran degil de weight
    //
    // Kok hatanin kendisi KDoc'ta yaziliydi: *"Sheet tasan icerigi
    // kaydirmiyor, KIRPIYOR"*. Kirpan bir kapta hicbir sabit oran sonsuza
    // kadar dogru kalamaz. Cozum kabin yuksekligini BELIRLI yapmak:
    //
    //  - kok Column ekranin %72'sini aliyor -> sheet artik "sinirsiz
    //    yukseklikle olculen" bir sey degil; `weight` calisabilir hale geliyor
    //    (eski 2. denemenin basarisiz olma sebebi tam olarak buydu)
    //  - grid `weight(1f)` ile KALAN yeri aliyor ve kendi icinde KAYIYOR
    //  - kacis butonu weight'in DISINDA, yani her zaman ve her ekranda altta
    //
    // %72: liste arkada gorunur kalmali (sheet'in kendi kurali) ama uc siralik
    // grid + arama + buton sigmali. Tavan degil PAY: buyutulmus yazi tipinde
    // grid kayiyor, buton yerinde kaliyor.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = Spacing.md)
            .padding(bottom = Spacing.lg + bottomPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                // KOKTE "Ekle" - tasarimin basligi bu. "Ne ekleyelim?" bir
                // soru soruyordu; sheet zaten kullanicinin actigi bir sey,
                // sormasi gereken bir sey yok. Reyona girildiginde reyon adi
                // basliga geciyor: orada kullanicinin NEREDE oldugu bilgisi
                // sorudan daha degerli.
                text = selected?.name ?: "Ekle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (selected == null && addedCount > 0) {
                // "N urun eklendi" - sheet acik kalirken kac tane eklendigini
                // sayiyor. Tasarimin karari ve dogru: sheet kapanmadigi icin
                // kullanici listeye bakamiyor, sayac tek geri bildirim.
                Text(
                    text = "$addedCount ürün eklendi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected != null) {
                Text(
                    text = "← Reyonlar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.pressable(onTap = onBackToCategories).padding(Spacing.xs),
                )
            }
        }

        // ARAMA ALANI - tasarimin sheet basligindaki ikinci satiri.
        //
        // ISLEVSEL BIR EKSIKTI: arama yalnizca alttaki hizli ekleme
        // cubugunda vardi, yani kullanici aradigi urunu bulmak icin sheet'i
        // KAPATMAK zorundaydi. Sheet'in butun amaci "dokunma = ekle, sheet
        // acik kalir" oldugu icin bu tam tersi yonde calisiyordu.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(NeydiExtraShapes.textField)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    Sizes.hairline,
                    LocalNeydiExtraColors.current.hairline,
                    NeydiExtraShapes.textField,
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NeydiIcon(
                    icon = NeydiIcons.Search,
                    contentDescription = null,
                    size = 22.dp,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
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

        if (query.isNotBlank()) {
            // ARAMA REYON SECIMINI EZIYOR: kullanici yaziyorsa aradigi sey
            // hangi reyonda oldugundan bagimsiz. Sonuc yoksa liste bos
            // kaliyor ve alttaki kacis satiri aranan kelimeyi tasiyor.
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(results, key = { it.id }) { seed ->
                    SuggestionChip(
                        label = seed.name,
                        reason = seed.defaultUnit,
                        checked = seed.matchKey in inList,
                        onClick = { onProduct(seed) },
                    )
                }
            }
        } else if (selected == null) {
            LazyVerticalGrid(
                // UC SUTUN SABIT, uyarlanabilir DEGIL: tasarim 3x4 grid
                // istiyor ve kutucuk boyutu (56dp) zaten sabit. Adaptive
                // genis ekranda dorde cikip tasarimin ritmini bozuyordu.
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(categories, key = { it.id }) { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        modifier = Modifier.pressable(onTap = { onCategory(category) }),
                    ) {
                        // TON TOHUMDAN GELIYOR (F6.9). Onceden 12 kategori
                        // de ayni griydi: `Category.tintArgb` tohumlaniyor ama
                        // hicbir yerde okunmuyordu. Ham ton dogrudan dolgu
                        // olamaz (orta-koyu ve doygun); zeminle karistirmasi
                        // ve kontrast olcumu CategoryTint'te.
                        CategoryTile(
                            initials = turkishInitials(category.name),
                            tint = CategoryTint.fill(
                                tintArgb = category.tintArgb,
                                surface = MaterialTheme.colorScheme.surface,
                                isLight = LocalNeydiExtraColors.current.isLight,
                            ),
                            contentColor = CategoryTint.content(MaterialTheme.colorScheme.onSurface),
                        )
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(products, key = { it.id }) { product ->
                    SuggestionChip(
                        label = product.name,
                        reason = product.defaultUnit,
                        checked = product.matchKey in inList,
                        onClick = { onProduct(product) },
                    )
                }
            }
        }

        // SERBEST METIN KACIS YOLU. Katalog 245 urun; Turkiye'deki her urun
        // degil. Katalogda olmayan bir sey isteyen kullanici burada tikanirsa
        // sheet bir duvar olur.
        NeydiButton(
            // TASARIM ARANAN KELIMEYI METNIN ICINE KOYUYOR: '"kuru kayisi"
            // ekle'. Kullanicinin yazdigi seyi geri gostermek, butonun ne
            // yapacagini tahmin ettirmiyor - soyluyor.
            text = if (query.isNotBlank()) "\"$query\" ekle" else "Listede yok, kendim yazayım",
            onClick = onFreeText,
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Sheet'in ekran yuksekligine orani.
 *
 * GRID'IN degil SHEET'IN orani - ve fark her sey. Eskisi grid'e tavan
 * veriyordu ve kalan her seyin (kacis butonu) o tavandan ARTAN yere sigmasini
 * umuyordu; umut tutmadi. Bu oran kabin kendisini belirliyor, icindekiler
 * `weight` ile paylasiyor, yani hicbir eleman sifira kirpilamiyor.
 *
 * Buyutulmus yazi tipi olceginde ya da 12'den fazla reyonda artik TASMIYOR,
 * grid kendi icinde KAYIYOR - cunku `LazyVerticalGrid`in belirli bir yuksekligi
 * var. Onceki sabitin TODO'su tam olarak bu senaryolar icin yazilmisti.
 */
private const val SHEET_RATIO = 0.72f

// --- Preview ---------------------------------------------------------------

private fun k(id: String, name: String) = Category(id, name, 0, 0xFF6E8B3D)
private fun u(id: String, name: String, unit: String) =
    CatalogSeed(id, name, name.lowercase(), "meyve-sebze", 1, unit)

@PreviewLightDark
@Composable
private fun AddSheetCategoriesPreview() = NeydiPreview {
    AddSheetContent(
        bottomPadding = 0.dp,
        categories = listOf(
            k("1", "Meyve-Sebze"), k("2", "Fırın-Ekmek"), k("3", "Süt-Kahvaltılık"),
            k("4", "Et-Tavuk-Balık"), k("5", "Temizlik"), k("6", "İçecek"),
        ),
        selected = null,
        products = emptyList(),
        onCategory = {}, onBackToCategories = {}, onProduct = {}, onFreeText = {},
    )
}

@PreviewLightDark
@Composable
private fun AddSheetProductsPreview() = NeydiPreview {
    AddSheetContent(
        bottomPadding = 0.dp,
        categories = emptyList(),
        selected = k("1", "Meyve-Sebze"),
        products = listOf(
            u("1", "Domates", "kg"), u("2", "Salatalık", "kg"), u("3", "Elma", "kg"),
        ),
        onCategory = {}, onBackToCategories = {}, onProduct = {}, onFreeText = {},
    )
}
