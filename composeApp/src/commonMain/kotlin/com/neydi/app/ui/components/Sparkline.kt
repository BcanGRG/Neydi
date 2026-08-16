package com.neydi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.Spacing

/**
 * 24x16dp fiyat sparkline'i. El yazimi Canvas - kutuphane yok.
 *
 * Neden hazir grafik kutuphanesi degil: bu cizim bir polyline, ~30 satir.
 * Bir bagimlilik eklemek tema kontrolunu kaybettirir, CMP'de iOS destegi
 * belirsiz olur ve APK'ya kutuphane girer. Faz 5'teki buyuk fiyat grafigi de
 * ayni sekilde Canvas'ta cizilecek.
 *
 * 2 GOZLEMIN ALTINDA HIC CIZILMEZ. Tek noktadan trend cikarmak bir yalandir;
 * cagiran taraf zaten [PriceHint.Trend] kurmadan buraya gelemez ama bu bilesen
 * de kendini korur.
 */
@Composable
fun Sparkline(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    // ESIK UC GOZLEM (gezinme sozlesmesi · thresholds). Once ikiydi: iki
    // nokta bir cizgi verir ama TREND vermez - iki gozlem arasindaki tek
    // dogru, gurultuyu egilim gibi gosterir. Altinda grafik hic cizilmiyor,
    // mansset cumlesi tek basina kaliyor.
    if (values.size < MIN_OBSERVATIONS) return

    Canvas(modifier = modifier.size(SizesExtra.sparkline)) {
        val min = values.min()
        val max = values.max()
        val span = (max - min).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (values.size - 1)
        // 1dp'lik cizgi kalinliginin yarisi kadar ust/alt pay birak ki
        // en yuksek ve en dusuk nokta kirpilmasin.
        val inset = 1.dp.toPx()
        val usableHeight = size.height - inset * 2

        val points = values.mapIndexed { i, v ->
            Offset(
                x = i * stepX,
                // Ekran koordinatinda y asagi buyur, fiyat yukari - ters cevir.
                y = inset + usableHeight * (1f - (v - min) / span),
            )
        }

        for (i in 0 until points.lastIndex) {
            drawLine(
                color = color,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Son nokta vurgulanir - "su an buradayiz".
        drawCircle(color = color, radius = 1.5.dp.toPx(), center = points.last())
    }
}

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun SparklinePreview() = NeydiPreview {
    val extras = LocalNeydiExtraColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Sparkline(listOf(371f, 375f, 389f, 402f, 431f, 455f), extras.priceUp)
        Sparkline(listOf(455f, 431f, 402f, 389f, 375f, 371f), extras.priceDown)
        Sparkline(listOf(400f, 400f, 400f, 400f), extras.priceUp)   // duz seri
        Sparkline(listOf(400f), extras.priceUp)                     // tek nokta: HIC cizilmez
    }
}

/** Grafigin cizilmesi icin gereken en az gozlem sayisi (tasarim esigi). */
private const val MIN_OBSERVATIONS = 3
