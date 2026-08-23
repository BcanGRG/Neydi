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
import androidx.compose.ui.unit.dp
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
    /**
     * MARKET bolumu mu (karar 118) - basliga storefront ikonu ekliyor.
     *
     * Ikon burada bir SUS degil ayrim: "A101'de" bir reyon adi gibi
     * okunabilirdi ve liste zaten reyon adlariyla bolunmus. Ikon o iki
     * bolumlemeyi birbirinden ayiran tek isaret.
     */
    isStore: Boolean = false,
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
        // 5dp MARKET BOLUMUNDE, 8dp reyonda - maketin iki olcusu. Ikonlu
        // baslikta ikon ile ad arasi daha dar, cunku ikisi tek bir isim gibi
        // okunmali.
        horizontalArrangement = Arrangement.spacedBy(
            if (isStore) STORE_ICON_GAP else Spacing.sm,
        ),
    ) {
        if (isStore) {
            NeydiIcon(
                icon = NeydiIcons.Storefront,
                contentDescription = null,
                size = STORE_ICON,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
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

/** Market bolumu basligindaki storefront - maketin olcusu. */
private val STORE_ICON = 15.dp

/** Ikon ile zincir adi arasi (maket: 5px). */
private val STORE_ICON_GAP = 5.dp

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun SectionHeaderPreview() = NeydiPreview {
    SectionHeader("Fırın-Ekmek", 3)
    SectionHeader("Süt-Kahvaltılık", 5)
    SectionHeader("Temizlik", 12)
    SectionHeader("A101'de", 2, isStore = true)
}
