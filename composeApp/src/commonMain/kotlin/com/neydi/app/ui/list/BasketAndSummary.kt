package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neydi.app.data.formatMinor
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import com.neydi.app.ui.theme.neydiDisplayFamily

/**
 * "Tahmini sepet" satiri.
 *
 * DURUST OLMAK ZORUNDA: fiyati bilinmeyen urunler toplama girmiyor, yani
 * tahmin her zaman EKSIK yonde yanlis olabilir. Bu yuzden "en az" diyor ve
 * kacinin fiyatini bildigimizi yaziyor. Kesin tutar gibi sunmak, kullanicinin
 * kasada surpriz yasamasi demek - ve bu uygulamanin varlik sebeplerinden biri
 * tam olarak o surprizi engellemek.
 *
 * Hic fiyat bilinmiyorsa satir HIC CIZILMEZ. "0,00 TL" yazmak yalan olurdu.
 */
@Composable
internal fun EstimatedBasket(
    amountMinor: Long,
    pricedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    if (pricedCount == 0) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            .clip(NeydiExtraShapes.card)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Tahmini sepet",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                // Kacinin fiyatini bildigimiz ACIKCA yaziyor: eksik bilgiyi
                // gizlemek tahmini guvenilir gosterir, ki degil.
                text = if (pricedCount < totalCount) {
                    "$totalCount üründen $pricedCount tanesini biliyorum"
                } else {
                    "hepsinin fiyatını biliyorum"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (pricedCount < totalCount) {
                "en az ${formatMinor(amountMinor)}"
            } else {
                formatMinor(amountMinor)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Alisveris sonrasi TEK SEFERLIK ozet karti.
 *
 * Duygusal karsilik ve ekran goruntusu ani burasi: kullanici alisverisi
 * bitirdi, uygulama ona ne yaptigini gosteriyor. Tutar 36sp Fraunces -
 * ekranin geri kalaninda o boyutta baska hicbir sey yok, bakis oraya gidiyor.
 *
 * Tutar bilinmiyorsa (fis okunmadi) sayilar gosteriliyor: "8 urun, 24 dakika".
 * Bu bile bos bir karttan iyi ve fis olmadan da dogru.
 */
@Composable
internal fun SummaryCard(
    bottomPadding: Dp = 0.dp,
    takenCount: Int,
    totalCount: Int,
    amountMinor: Long?,
    durationMinutes: Int?,
    onDismiss: () -> Unit,
    /** null ise fis butonu HIC cizilmez - iOS kucultmesi Faz 8'e kadar yok. */
    onTakeReceipt: (() -> Unit)? = null,
    /**
     * Fissiz mutabakati duzeltme yolu (F4.8).
     *
     * GORUNUR OLMASI SART: kapanista planlananlar alindi yazildi ve kullanici
     * bunu bilmiyor. Duzeltme yolu olmadan iyimser varsayim bir tuzak olurdu -
     * alinmamis urun satin alma gecmisine girer, kullanici gormez, oneri
     * motoru o urunu duzenli aliniyor sanar.
     */
    onFixTaken: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md)
            .clip(NeydiExtraShapes.card)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.lg)
            .padding(top = Spacing.xl, bottom = Spacing.xl + bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = if (takenCount == totalCount) "Liste tamam." else "Alışveriş bitti.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (amountMinor != null) {
            Text(
                text = formatMinor(amountMinor),
                // 36sp Fraunces: ekranda bu boyutta baska hicbir sey yok.
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = neydiDisplayFamily(),
                    fontSize = 36.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = buildString {
                append("$takenCount ürün alındı")
                if (takenCount < totalCount) append(", ${totalCount - takenCount} tanesi kaldı")
                if (durationMinutes != null) append(" · $durationMinutes dakika")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (onTakeReceipt != null) {
            NeydiButton(
                text = "Fiş çek",
                onClick = onTakeReceipt,
                modifier = Modifier
                    .padding(top = Spacing.sm)
                    .fillMaxWidth(),
            )
            // Cekim rehberi tasarimin kamera overlay metninden - sistem
            // kamerasina overlay konamadigi icin BURADA (F4.13). Kullanicinin
            // ana marketi uzun fis basiyor; tavsiye cekimden ONCE gorunmeli,
            // basarisiz okumadan sonra degil.
            Text(
                text = "Fişin tamamı kadraja girsin. Uzunsa parça parça çek — her karede 10–15 satır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }

        if (onFixTaken != null) {
            Text(
                text = "Hepsini almadım",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(top = Spacing.xs)
                    .pressable(onTap = onFixTaken)
                    .padding(Spacing.xs),
            )
        }

        // "Tamam" fisten SONRA ve daha SILIK. Fis cekmek kartin asil isi;
        // ustte ve vurgulu bir "Tamam" olsaydi cogu kullanici once ona basar,
        // fis hic cekilmez ve fiyat hafizasi hic dolmazdi.
        NeydiButton(
            text = "Tamam",
            onClick = onDismiss,
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun EstimatePartialPreview() = NeydiPreview {
    EstimatedBasket(amountMinor = 48750, pricedCount = 5, totalCount = 8)
}

@PreviewLightDark
@Composable
private fun EstimateCompletePreview() = NeydiPreview {
    EstimatedBasket(amountMinor = 128990, pricedCount = 8, totalCount = 8)
}

@PreviewLightDark
@Composable
private fun SummaryWithTotalPreview() = NeydiPreview {
    SummaryCard(takenCount = 8, totalCount = 8, amountMinor = 128990, durationMinutes = 24, onDismiss = {})
}

/** Fis okunmadi: tutar yok ama kart yine de bir sey soyluyor. */
@PreviewLightDark
@Composable
private fun SummaryWithoutTotalPreview() = NeydiPreview {
    SummaryCard(takenCount = 6, totalCount = 8, amountMinor = null, durationMinutes = 31, onDismiss = {})
}
