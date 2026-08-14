package com.neydi.app.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
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
) {
    // GRID YUKSEKLIGI EKRANA ORANLI, sabit dp DEGIL.
    //
    // Uc deneme gerekti, ucu de cihazda goruldu:
    //  1) sabit 340dp     -> icerik sheet'ten uzun, kacis butonu cubuk altinda
    //  2) weight(1f)      -> kismi acik sheet icerigi SINIRSIZ yukseklikle
    //                        olcuyor, grid butun alani aliyor, buton kayboluyor
    //  3) tam acik sheet  -> buton goruniyor ama sheet EKRANI KAPLIYOR ve
    //                        "liste arkada gorunur kalsin" kurali bozuluyor
    // Dogrusu: grid ekran yuksekliginin bir orani kadar, sheet kismi kaliyor,
    // buton hep altta.
    val screenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
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
                text = selected?.name ?: "Ne ekleyelim?",
                style = MaterialTheme.typography.titleMedium,
            )
            if (selected != null) {
                Text(
                    text = "← Reyonlar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.pressable(onTap = onBackToCategories).padding(Spacing.xs),
                )
            }
        }

        if (selected == null) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 84.dp),
                modifier = Modifier.heightIn(max = screenHeight * GRID_RATIO),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(categories, key = { it.id }) { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        modifier = Modifier.pressable(onTap = { onCategory(category) }),
                    ) {
                        CategoryTile(turkishInitials(category.name))
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
                modifier = Modifier.heightIn(max = screenHeight * GRID_RATIO),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(products, key = { it.id }) { product ->
                    SuggestionChip(
                        label = product.name,
                        reason = product.defaultUnit,
                        onClick = { onProduct(product) },
                    )
                }
            }
        }

        // SERBEST METIN KACIS YOLU. Katalog 245 urun; Turkiye'deki her urun
        // degil. Katalogda olmayan bir sey isteyen kullanici burada tikanirsa
        // sheet bir duvar olur.
        NeydiButton(
            text = "Listede yok, kendim yazayım",
            onClick = onFreeText,
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Grid'in ekran yuksekligine orani.
 *
 * Deger uiautomator ile OLCULEREK bulundu, goz karariyla degil: 0.34'te
 * ucuncu sirasinin etiketleri ve kacis butonu `bounds="[0,0][0,0]"` doniyordu -
 * yani sifir yukseklige kirpilmislardi. Sheet tasan icerigi kaydirmiyor,
 * kirpiyor; o yuzden grid butcesi butona yer BIRAKMAK zorunda.
 *
 * TODO(sheet-yuksekligi): Bu bir SIHIRLI SAYI, cozum degil. Baska ekran
 * oraninda, katlanabilir cihazda, buyutulmus yazi tipi olceginde ya da 12'den
 * fazla reyon oldugunda yeniden tasar - ve SESSIZCE tasar. Kalici cozum
 * F10.5'te; F10.2 (Nav3 custom Scene) ile ayni iste bulusuyor.
 */
private const val GRID_RATIO = 0.24f

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
