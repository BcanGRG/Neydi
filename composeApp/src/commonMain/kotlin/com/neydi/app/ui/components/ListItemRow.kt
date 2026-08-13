package com.neydi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.LocalNeydiTextStyles
import com.neydi.app.ui.theme.Motion
import com.neydi.app.ui.theme.NeydiShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.SpacingExtra
import com.neydi.app.ui.theme.pressable

/**
 * Liste satiri - uygulamanin en cok gorulen bileseni.
 *
 * Anatomi:
 *   [onay 24dp] [adet rozeti*] [AD 17sp/500] ......... [fiyat cipi] [es avatari*]
 *                              [ikinci satir 14sp %60*]
 *   (* = kosullu)
 *
 * Yukseklik: 56dp (tek satir) / 68dp (ikinci satirli) / 72dp (alisveris modu).
 *
 * SATIRIN TAMAMI isaretleme hedefidir - kucuk bir kutuyu tutturmak gerekmez.
 * Tek istisna fiyat cipi: kendi 44dp hedefi var ve fiyat gecmisini acar.
 */
@Composable
fun ListItemRow(
    row: ListRow,
    modifier: Modifier = Modifier,
    shoppingMode: Boolean = false,
    onToggle: () -> Unit = {},
    onPriceClick: (() -> Unit)? = null,
) {
    val styles = LocalNeydiTextStyles.current
    val extras = LocalNeydiExtraColors.current
    val second = row.secondLine()
    // Ucuz-alternatif cipi de ikinci satirin sakini: ana satirda kardes olursa
    // yatay genisligi calar ve URUN ADINI kirpar. Ad kirpilmasi kabul edilemez -
    // fiyat ipucu yardimci bilgi, ad ise satirin varlik sebebi.
    val cheaper = row.cheaperElsewhere.takeUnless { shoppingMode }
    val hasSecondLine = second != SecondLine.Empty || cheaper != null

    val height = when {
        shoppingMode -> Sizes.rowShopping
        hasSecondLine -> Sizes.rowWithMeta
        else -> Sizes.rowCollapsed
    }

    // Sabitler kullanicinin kendi ekledigi satirlardan gorsel olarak HAFIF olmali,
    // yoksa "uygulama benim agzima laf koydu" hissi verir.
    val rowAlpha = when {
        row.checked -> 0.55f
        row.isStaple -> 0.70f
        else -> 1f
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .clip(NeydiShapes.large)
            .then(
                // Alisveris modunda satir container'i kenarlik kazanir: kol mesafesinden
                // ve kotu isikta satir sinirlari gorunur olmali.
                if (shoppingMode) {
                    Modifier.border(
                        SizesExtra.rowBorderShopping,
                        extras.hairline,
                        NeydiShapes.large,
                    )
                } else Modifier,
            )
            .pressable(onTap = onToggle)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .alpha(rowAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckTarget(checked = row.checked, shoppingMode = shoppingMode)

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = SpacingExtra.betweenCheckboxAndName),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (row.isStaple) StaplePin()
            if (row.quantity != null) QuantityBadge(row.quantity)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.name,
                    style = if (shoppingMode) styles.itemNameShopping else styles.itemName,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (row.checked) TextDecoration.LineThrough else null,
                )
                // Alisveris modunda ikincil metadata katlanir: reyonda 10-11 degil
                // 7-8 satir gorunmeli, ve gerekli olan tek bilgi urun adi.
                if (hasSecondLine && !shoppingMode) {
                    SecondLineContent(second, cheaper, extras.priceUp, extras.priceDown)
                }
            }
        }

        val priceText = (row.priceHint as? PriceHint.Single)?.price
            ?: (row.priceHint as? PriceHint.Trend)?.to
        if (priceText != null && !shoppingMode) {
            PriceChip(priceText, onClick = onPriceClick)
        }

        if (row.addedByInitial != null) {
            MemberAvatar(row.addedByInitial, Modifier.padding(start = Spacing.sm))
        }
    }
}

/**
 * Onay hedefi. Isaretlenince daire -> 12dp squircle'a donusur (200ms).
 * Sekil degisimi, renk degisiminden bagimsiz bir sinyal: renk gormeyen kullanici
 * icin de "isaretlendi" okunur.
 */
@Composable
private fun CheckTarget(checked: Boolean, shoppingMode: Boolean) {
    val size = if (shoppingMode) SizesExtra.checkboxShopping else SizesExtra.checkbox
    val corner by animateDpAsState(
        targetValue = if (checked) 12.dp else size / 2,
        animationSpec = Motion.settle(),
        label = "checkCorner",
    )
    val fill by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = Motion.settle(),
        label = "checkFill",
    )
    val outline = LocalNeydiExtraColors.current.hairline

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(fill)
            .border(1.5.dp, if (checked) fill else outline, RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

/** "Her zamankiler" raptiyesi - 12dp, satirin sabit oldugunu gosterir. */
@Composable
private fun StaplePin() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
    )
}

/**
 * Ikinci satir. GUNCEL FIYATI YAZMAZ - onu sagdaki fiyat cipi tasiyor.
 * Burada sadece cipin soyleyemedigi sey var: nerede, ne zaman, oncesinde kacti.
 * Ayni sayiyi iki kez yazmak satiri kalabaliklastirip hicbir sey eklemez.
 */
@Composable
private fun SecondLineContent(
    second: SecondLine,
    cheaper: String?,
    priceUp: Color,
    priceDown: Color,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when (second) {
            is SecondLine.Reason -> MetaText(second.text, muted, Modifier.weight(1f, false))
            is SecondLine.Note -> MetaText(second.text, muted, Modifier.weight(1f, false))
            SecondLine.Empty -> Unit
            is SecondLine.Price -> when (val h = second.hint) {
                PriceHint.None -> Unit
                is PriceHint.Single ->
                    MetaText("${h.store} · ${h.daysAgo} gün önce", muted, Modifier.weight(1f, false))

                is PriceHint.Trend -> {
                    MetaText("önce ${h.from}", muted, Modifier.weight(1f, false))
                    DeltaChip(h.deltaPercent, h.rising)
                    Sparkline(
                        values = h.history,
                        color = if (h.rising) priceUp else priceDown,
                    )
                }
                // Ambalaj degismisse TREND YOK. 900g -> 800g ayni fiyata satiliyorsa
                // bu bir fiyat dususu degil; yesil ok cikarsa yalan soylemis oluruz.
                is PriceHint.PackChanged ->
                    MetaText(
                        "${h.fromPack} → ${h.toPack} · ${h.note}",
                        muted,
                        Modifier.weight(1f, false),
                    )
            }
        }

        if (cheaper != null) AccentChip(cheaper)
    }
}

@Composable
private fun MetaText(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        // 14sp - govde minimumu. 13sp %60 opaklikla okunabilirlik sinirinin altina duser.
        style = MaterialTheme.typography.bodySmall,
        color = color.copy(alpha = 0.75f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

// --- Preview ---------------------------------------------------------------
// Sahte veri kasten gercekci: kisa/uzun ad, Turkce karakter, ve satirin
// yatay butcesini gercekten zorlayan bir kombinasyon. Preview'in isi
// "cizildi mi" degil, "sikistiginda ne feda ediliyor" sorusunu gostermek.

@PreviewLightDark
@Composable
private fun ListItemRowStatesPreview() = NeydiPreview {
    ListItemRow(ListRow("Karabiber"))
    ListItemRow(ListRow("Yumurta 10'lu", quantity = "2x"))
    ListItemRow(ListRow("Domates", quantity = "1 kg", addedByInitial = "A"))
    ListItemRow(ListRow("Tam Buğday Ekmek", isStaple = true))
    ListItemRow(ListRow("Zeytinyağı 1 L", checked = true))
}

@PreviewLightDark
@Composable
private fun ListItemRowPriceHintsPreview() = NeydiPreview {
    ListItemRow(
        ListRow("Zeytinyağı 1 L", priceHint = PriceHint.Single("289,00 TL", "A101", 12)),
    )
    ListItemRow(
        ListRow(
            "Ayçiçek Yağı 5 L",
            priceHint = PriceHint.Trend(
                from = "389,00", to = "455,00 TL", deltaPercent = 17, rising = true,
                history = listOf(371f, 375f, 389f, 389f, 402f, 431f, 448f, 455f),
            ),
        ),
    )
    ListItemRow(
        ListRow("Pınar Süt", priceHint = PriceHint.PackChanged("1 L", "900 ml", "aynı fiyat")),
    )
    ListItemRow(ListRow("Bulaşık Deterjanı", suggestionReason = "12 gündür almadın"))
}

/**
 * Satirin en dar hali: uzun ad + fiyat + ucuz-alternatif cipi ayni anda.
 * Bu preview F3.1'de cihazda yakalanan hatanin nobetcisi - cip ana satirda
 * kardes oldugunda urun adi "Beyaz Peynir …" diye kirpiliyordu.
 */
@PreviewLightDark
@Composable
private fun ListItemRowCrowdedPreview() = NeydiPreview {
    ListItemRow(
        ListRow(
            "Beyaz Peynir 600 g Tam Yağlı",
            priceHint = PriceHint.Single("184,50 TL", "Migros", 9),
            cheaperElsewhere = "A101'de 159,90",
        ),
    )
}

@PreviewLightDark
@Composable
private fun ListItemRowShoppingModePreview() = NeydiPreview {
    ListItemRow(ListRow("Ayçiçek Yağı 5 L", quantity = "2x"), shoppingMode = true)
    ListItemRow(
        ListRow("Beyaz Peynir 600 g", priceHint = PriceHint.Single("184,50 TL", "Migros", 9)),
        shoppingMode = true,
    )
    ListItemRow(ListRow("Tam Buğday Ekmek", isStaple = true, checked = true), shoppingMode = true)
}
