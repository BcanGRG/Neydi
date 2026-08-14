package com.neydi.app.ui.liste

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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neydi.app.data.kurusFormatla
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Spacing
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
internal fun TahminiSepet(
    tutarKurus: Long,
    fiyatliSayisi: Int,
    toplamSayisi: Int,
    modifier: Modifier = Modifier,
) {
    if (fiyatliSayisi == 0) return

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
                text = if (fiyatliSayisi < toplamSayisi) {
                    "$toplamSayisi üründen $fiyatliSayisi tanesini biliyorum"
                } else {
                    "hepsinin fiyatını biliyorum"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (fiyatliSayisi < toplamSayisi) {
                "en az ${kurusFormatla(tutarKurus)}"
            } else {
                kurusFormatla(tutarKurus)
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
internal fun OzetKarti(
    altBosluk: Dp = 0.dp,
    alinanSayisi: Int,
    toplamSayisi: Int,
    tutarKurus: Long?,
    sureDakika: Int?,
    onKapat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md)
            .clip(NeydiExtraShapes.card)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.lg)
            .padding(top = Spacing.xl, bottom = Spacing.xl + altBosluk),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = if (alinanSayisi == toplamSayisi) "Liste tamam." else "Alışveriş bitti.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (tutarKurus != null) {
            Text(
                text = kurusFormatla(tutarKurus),
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
                append("$alinanSayisi ürün alındı")
                if (alinanSayisi < toplamSayisi) append(", ${toplamSayisi - alinanSayisi} tanesi kaldı")
                if (sureDakika != null) append(" · $sureDakika dakika")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        NeydiButton("Tamam", onKapat, modifier = Modifier.padding(top = Spacing.sm))
    }
}

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun TahminEksikPreview() = NeydiPreview {
    TahminiSepet(tutarKurus = 48750, fiyatliSayisi = 5, toplamSayisi = 8)
}

@PreviewLightDark
@Composable
private fun TahminTamPreview() = NeydiPreview {
    TahminiSepet(tutarKurus = 128990, fiyatliSayisi = 8, toplamSayisi = 8)
}

@PreviewLightDark
@Composable
private fun OzetTutarliPreview() = NeydiPreview {
    OzetKarti(alinanSayisi = 8, toplamSayisi = 8, tutarKurus = 128990, sureDakika = 24, onKapat = {})
}

/** Fis okunmadi: tutar yok ama kart yine de bir sey soyluyor. */
@PreviewLightDark
@Composable
private fun OzetTutarsizPreview() = NeydiPreview {
    OzetKarti(alinanSayisi = 6, toplamSayisi = 8, tutarKurus = null, sureDakika = 31, onKapat = {})
}
