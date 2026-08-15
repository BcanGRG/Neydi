package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.LocalNeydiTextStyles
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Liste satirindaki fiyat cipi. KENDI 44dp DOKUNMA HEDEFI var - satirin geri kalani
 * isaretlemek icin, bu cip fiyat gecmisini acmak icin. Reyonda tek elle basilabilmeli.
 *
 * Sabit 92dp genislik + saga dayali: tabular figures (tnum) Skia'da sessizce
 * yok sayilabildigi icin duzen yalnizca tnum'a guvenmiyor. Rakamlar hizalanmazsa
 * bile sutun hizali kalir.
 */
@Composable
fun PriceChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val styles = LocalNeydiTextStyles.current
    val base = modifier
        .width(SizesExtra.priceColumn)
        .heightIn(min = Sizes.minTapTarget)
    Box(
        modifier = if (onClick != null) base.pressable(onTap = onClick) else base,
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = text,
            style = styles.priceChip,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * Fiyat degisim yuzdesi: "%9". Ok anlamsaldir - kirmizi yukari, yesil asagi.
 * Renk tek basina anlam tasimaz; ok isareti renk gormeyen kullanici icin de calisir.
 */
@Composable
fun DeltaChip(
    percent: Int,
    rising: Boolean,
    modifier: Modifier = Modifier,
) {
    val extras = LocalNeydiExtraColors.current
    val color = if (rising) extras.priceUp else extras.priceDown
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = if (rising) "↑" else "↓",
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
        Text(
            text = "%$percent",
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

/**
 * Oneri seridi cipi. GEREKCE CIPIN ICINDE: "Yumurta · 14 gun oldu".
 * Gerekcesiz bir cip reklam gibi okunur; gerekceli olan hafiza yardimi gibi.
 *
 * Animasyon yok, badge yok, nokta yok - hicbir sey dikkat cekmeye calismaz.
 */
@Composable
fun SuggestionChip(
    label: String,
    reason: String,
    modifier: Modifier = Modifier,
    /**
     * Bu urun ZATEN LISTEDE mi (tasarim karari 12).
     *
     * "Bu oturumda eklendi" DEGIL, "bu listede var" demek. Ikisi cakismiyor
     * (oturumda eklenen zaten listede) ama listenin durumunu gostermek
     * sheet'i kapatip acmaya karsi dayanikli: ayni urun ikinci kez
     * isaretsiz gorunmuyor. Oturumun kendi sayaci zaten baslikta.
     */
    checked: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .heightIn(min = SizesExtra.suggestionChip)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            // ISARETLI CIP PASIF: ayni satiri iki kez eklemek bir is degil,
            // ve pasiflik bunu dokunmadan once soyluyor.
            .pressable(enabled = !checked, onTap = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (checked) {
            NeydiIcon(
                icon = NeydiIcons.CheckCircle,
                contentDescription = "listede var",
                size = 18.dp,
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "·",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = reason,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Adet rozeti: "2x", "1 kg". ADET 1 ISE CIZILMEZ - cagiran taraf null gecer.
 * Her satirda "1x" yazmak 20 satirlik bir listede yalnizca gurultudur.
 */
@Composable
fun QuantityBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = SizesExtra.qtyBadgeHeight)
            .heightIn(min = SizesExtra.qtyBadgeHeight)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Esin baş harfi. YALNIZCA es ekledigunde cizilir - kendi ekledigimiz satirda
 * kendi harfimizi gormek bilgi tasimaz.
 */
@Composable
fun MemberAvatar(initial: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 24.dp)
            .heightIn(min = 24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun ChipsPreview() = NeydiPreview {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeltaChip(17, rising = true)
        DeltaChip(6, rising = false)
        QuantityBadge("2x")
        QuantityBadge("1 kg")
        MemberAvatar("A")
    }
    PriceChip("455,00 TL")
    // Dort haneli fiyat: 92dp sutun butcesini zorlayan gercek senaryo.
    PriceChip("1.289,90 TL")
    SuggestionChip("Yumurta", "14 gün oldu") {}
    SuggestionChip("Çay", "genelde 4 alışverişte bir") {}
}
