package com.neydi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.components.AccentChip
import com.neydi.app.ui.components.CategoryTile
import com.neydi.app.ui.components.DeltaChip
import com.neydi.app.ui.components.ListItemRow
import com.neydi.app.ui.components.ListRow
import com.neydi.app.ui.components.PriceHint
import com.neydi.app.ui.components.SectionHeader
import com.neydi.app.ui.components.SuggestionChip
import com.neydi.app.ui.components.turkishInitials
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * GECICI GELISTIRICI EKRANI. F3.2'de gercek Liste ekrani gelince SILINECEK.
 *
 * Bilesenleri sahte veriyle cihazda gormek icin var - Room'u beklemeden
 * her varyanti tek ekranda karsilastirabilmek, tasarimla kod arasindaki
 * farki erken yakalamanin en ucuz yolu.
 */
@Composable
fun ComponentGalleryScreen(onGeri: () -> Unit) {
    val checked = remember { mutableStateMapOf<String, Boolean>() }
    var shoppingMode by remember { mutableStateOf(false) }

    fun row(r: ListRow) = r.copy(checked = checked[r.name] ?: r.checked)

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Sabit dp yerine gercek sistem cubuklari: alt gezinme cubugunun
            // altinda kalan son satir cihazda goze carpti.
            contentPadding = WindowInsets.safeDrawing
                .add(WindowInsets(top = 8.dp, bottom = 24.dp))
                .asPaddingValues(),
        ) {
            item {
                Column(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
                    Text("Bileşenler", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Sahte veri. Satırlara dokun — işaretleme çalışıyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // --- Kategori kutucugu + iki harf fallback ---
            item { GroupTitle("Kategori kutucuğu · iki-harf fallback") }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    // "incir" -> "İN": Turkce buyuk harf testi. Kotlin'in uppercase()'i
                    // burada "IN" uretirdi ve yanlis olurdu.
                    listOf("Tam Buğday Ekmek", "incir", "ıspanak", "Şeftali", "Ğ test")
                        .forEach { CategoryTile(turkishInitials(it)) }
                }
            }
            item {
                Note("\"incir\" → İN  ·  \"ıspanak\" → IS  — Türkçe i/ı ayrımı korunuyor")
            }

            // --- Fiyat ipucunun uc veri durumu ---
            item { GroupTitle("Fiyat ipucu · üç veri durumu") }
            item {
                Column {
                    ListItemRow(
                        row = row(ListRow(name = "Karabiber")),
                        onToggle = { checked["Karabiber"] = !(checked["Karabiber"] ?: false) },
                    )
                    Note("0 gözlem → ikinci satır hiç çizilmiyor. \"fiyat yok\" da yazmıyor.")

                    ListItemRow(
                        row = row(
                            ListRow(
                                name = "Zeytinyağı 1 L",
                                priceHint = PriceHint.Single("289,00 TL", "A101", 12),
                            ),
                        ),
                        onToggle = { checked["Zeytinyağı 1 L"] = !(checked["Zeytinyağı 1 L"] ?: false) },
                    )
                    Note("1 gözlem → son ödenen. Yüzde yok, grafik yok — tek noktadan trend bir yalan olurdu.")

                    ListItemRow(
                        row = row(
                            ListRow(
                                name = "Ayçiçek Yağı 5 L",
                                priceHint = PriceHint.Trend(
                                    from = "389,00", to = "455,00 TL",
                                    deltaPercent = 17, rising = true,
                                    history = listOf(371f, 375f, 389f, 389f, 402f, 431f, 448f, 455f),
                                ),
                            ),
                        ),
                        onToggle = { checked["Ayçiçek Yağı 5 L"] = !(checked["Ayçiçek Yağı 5 L"] ?: false) },
                    )
                    Note("2+ gözlem → trend + delta + sparkline.")

                    ListItemRow(
                        row = row(
                            ListRow(
                                name = "Pınar Süt",
                                priceHint = PriceHint.PackChanged("1 L", "900 ml", "aynı fiyat"),
                            ),
                        ),
                        onToggle = { checked["Pınar Süt"] = !(checked["Pınar Süt"] ?: false) },
                    )
                    Note("Ambalaj küçülmüş → trend BASTIRILDI. Yeşil ok çıksaydı yalan olurdu.")
                }
            }

            // --- Satir varyantlari ---
            item { GroupTitle("Satır varyantları") }
            item {
                Column {
                    ListItemRow(
                        row = row(ListRow(name = "Yumurta 10'lu", quantity = "2x")),
                        onToggle = { checked["Yumurta 10'lu"] = !(checked["Yumurta 10'lu"] ?: false) },
                    )
                    ListItemRow(
                        row = row(ListRow(name = "Domates", quantity = "1 kg", addedByInitial = "A")),
                        onToggle = { checked["Domates"] = !(checked["Domates"] ?: false) },
                    )
                    Note("Adet 1 değilse rozet çizilir. Avatar YALNIZCA eş eklediyse.")

                    ListItemRow(
                        row = row(ListRow(name = "Tam Buğday Ekmek", isStaple = true)),
                        onToggle = { checked["Tam Buğday Ekmek"] = !(checked["Tam Buğday Ekmek"] ?: false) },
                    )
                    Note("Sabit: %70 opaklık + raptiye. Kullanıcının eklediklerinden görsel olarak hafif.")

                    ListItemRow(
                        row = row(
                            ListRow(
                                name = "Bulaşık Deterjanı",
                                suggestionReason = "12 gündür almadın",
                            ),
                        ),
                        onToggle = { checked["Bulaşık Deterjanı"] = !(checked["Bulaşık Deterjanı"] ?: false) },
                    )
                    ListItemRow(
                        row = row(
                            ListRow(
                                name = "Beyaz Peynir 600 g",
                                priceHint = PriceHint.Single("184,50 TL", "Migros", 9),
                                cheaperElsewhere = "A101'de 159,90",
                            ),
                        ),
                        onToggle = { checked["Beyaz Peynir 600 g"] = !(checked["Beyaz Peynir 600 g"] ?: false) },
                    )
                    Note("Amber çip AccentSurface'ten geliyor — kenarlık kuralı otomatik. Liste başına en fazla 3.")
                }
            }

            // --- Bolum basligi ---
            item { GroupTitle("Bölüm başlığı") }
            item {
                Column {
                    SectionHeader("Fırın-Ekmek", 3)
                    SectionHeader("Süt-Kahvaltılık", 5)
                    Note("ALL-CAPS yok — Türkçe i/İ locale'siz uppercase()'te bozulur.")
                }
            }

            // --- Oneri seridi ---
            item { GroupTitle("Öneri şeridi") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(
                        listOf(
                            "Yumurta" to "14 gün oldu",
                            "Ekmek" to "her seferinde",
                            "Çay" to "genelde 4 alışverişte bir",
                        ),
                    ) { (label, reason) ->
                        SuggestionChip(label, reason) {}
                    }
                }
            }
            item { Note("Gerekçe çipin İÇİNDE. Gerekçesiz çip reklam gibi okunur.") }

            // --- Delta cipleri ---
            item { GroupTitle("Delta çipi") }
            item {
                Row(
                    Modifier.padding(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DeltaChip(17, rising = true)
                    DeltaChip(6, rising = false)
                    AccentChip("A101'de 34,90")
                }
            }
            item { Note("Ok anlamsal — renk görmeyen kullanıcı için de çalışır.") }

            // --- Alisveris modu ---
            item { GroupTitle("Alışveriş modu") }
            item {
                Column(Modifier.padding(horizontal = Spacing.md)) {
                    Text(
                        text = if (shoppingMode) "Açık — kapatmak için dokun" else "Kapalı — açmak için dokun",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .pressable { shoppingMode = !shoppingMode }
                            .padding(vertical = Spacing.sm),
                    )
                }
            }
            item {
                // Alisveris modunda satir kenarlik kazaniyor; galeride ekran kenarina
                // yapismasin diye burada nefes payi var. Gercek Liste kendi padding'ini verecek.
                Column(Modifier.padding(horizontal = Spacing.sm)) {
                    ListItemRow(
                        row = row(ListRow(name = "Ayçiçek Yağı 5 L", quantity = "2x")),
                        shoppingMode = shoppingMode,
                        onToggle = { checked["Ayçiçek Yağı 5 L"] = !(checked["Ayçiçek Yağı 5 L"] ?: false) },
                    )
                    ListItemRow(
                        row = row(
                            ListRow(
                                name = "Beyaz Peynir 600 g",
                                priceHint = PriceHint.Single("184,50 TL", "Migros", 9),
                            ),
                        ),
                        shoppingMode = shoppingMode,
                        onToggle = { checked["Beyaz Peynir 600 g"] = !(checked["Beyaz Peynir 600 g"] ?: false) },
                    )
                    Note("Açıkken: satır 72dp, ad 20sp/700, metadata katlanıyor, container kenarlık kazanıyor.")
                }
            }

            item {
                Text(
                    text = "← Geri",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(Spacing.md)
                        .pressable(onTap = onGeri)
                        .padding(Spacing.sm),
                )
            }
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = Spacing.md, end = Spacing.md, top = Spacing.lg, bottom = Spacing.xs,
        ),
    )
}

@Composable
private fun Note(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
    )
}
