package com.neydi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.SpacingExtra

/**
 * Reyon bolumu basligi: "Fırın-Ekmek  3".
 *
 * ALL-CAPS YOK. Tasarim sisteminde buyuk harf kullanilmiyor cunku Turkce
 * i/İ ve ı/I donusumu locale'siz uppercase()'te bozulur; ustelik M3 Expressive
 * de caps'i biraktı. Ayirt edicilik punto ve renkle saglaniyor.
 *
 * BOS BOLUM CIZILMEZ - cagiran taraf 0 ogeli bolumu hic olusturmaz.
 *
 * ## BASLIK SIMETRIK DEGIL: ustunde 20dp, altinda 4dp
 *
 * Baslik kendi satirlarina YAPISIK, onceki bolumden UZAK durmali. Simetrik
 * dolgu (8dp/8dp) basligi iki bolumun tam ortasina koyuyordu ve o zaman
 * hicbirine ait gorunmuyor - kullanicinin *"listedeki itemlar cok ic ice
 * gibi duruyor, karisik gibi"* dedigi sey buydu.
 *
 * Olculdu: satirlarin kendisi dogruydu (56/72dp, bantlar arasi 5dp). Tek
 * duz olan yerdi basligin cevresi - cihazda bolum-arasi ile satir-arasi
 * bosluk BIRBIRINE ESITTI, yani listede uc kademe (bant < satir < bolum)
 * yerine tek kademe vardi.
 *
 * Maketin olcusu: baslik kutusunun ustunde 14px dolgu + 6px margin = 20px,
 * altinda 4px. Ilk basligin ustunde 10px, cunku orada ayrilacak bir bolum
 * yok - ama bunu ayri bir parametreye baglamak, kazanci (10dp) tasidigi
 * kosula degmeyecek kadar kucuk.
 */
@Composable
fun SectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.md,
                end = Spacing.md,
                top = SpacingExtra.aboveSectionHeader,
                bottom = SpacingExtra.belowSectionHeader,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun SectionHeaderPreview() = NeydiPreview {
    SectionHeader("Fırın-Ekmek", 3)
    SectionHeader("Süt-Kahvaltılık", 5)
    SectionHeader("Temizlik", 12)
}
