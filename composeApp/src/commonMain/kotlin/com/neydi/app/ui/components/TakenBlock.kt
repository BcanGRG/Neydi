package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Alinan satirlarin sayaci - *"Alindi . 12/18"* (karar 124).
 *
 * ## Neden bir SAYAC, bir bolum degil
 *
 * `docs/38` S4 engelleyiciydi ve iki dogru cumle birbirini kesiyordu: karar
 * 116 *"Alindi"* bolumunu alisveriste ISTIYORDU, kod ise bolumu kaldirmisti
 * ve gerekcesi olculmustu - alisveriste isaretlenen satirin yer degistirmesi
 * *"hareket eden basparmagin altinda yeniden siralama"* demek; kullanici bir
 * sonrakine dokunacakken liste kayar ve YANLIS urunu isaretler.
 *
 * Tasarim kodun gerekcesini KABUL etti ve ucuncu bir yol yazdi: satir yerinde
 * kalir, ama listenin sonunda genisleMEYEN bir sayac durur. Boylece
 * kullanicinin *"kacini aldim"* sorusu cevap bulur ve hicbir satir oynamaz.
 *
 * ## Chevron CIZILMIYOR ve bu bir susleme karari degil
 *
 * Chevron bir VAATTIR: *"dokun, acilir."* Burada acilacak bir sey yok -
 * satirlar zaten listenin icinde, yerlerinde duruyorlar. Cizilseydi dokunan
 * kullanici hicbir sey olmadigini gorurdu.
 *
 * Sapan zincir bolumleri bu blogun USTUNDE kaliyor, yani karar 118'in
 * *"Alindi'nin ustunde"* capasi yeniden yazilmadan yerini buluyor.
 */
@Composable
fun TakenCounter(taken: Int, total: Int, modifier: Modifier = Modifier) {
    TakenBar(modifier) {
        Text(
            text = takenCounterText(taken, total),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Alisveris SONRASINDA ayni blok katlanabilir bir bolume donuyor (karar 124).
 *
 * ## Neden yalnizca burada katlaniyor
 *
 * Alisveris bittiginde alinan satirlar artik *"yapilacak is"* degil KAYIT.
 * Listenin isi geriye kalani gostermek, o yuzden blok KAPALI aciliyor -
 * maketin cizdigi hal de bu: `Alindi (12)` + `expand_more`, satirlar
 * gorunmuyor.
 *
 * Katlanabilirligin alisveriste OLMAMASININ sebebi ayni madde: orada
 * satirlar bolumde degil yerlerinde duruyor, yani katlanacak bir sey yok.
 *
 * ⚠ TEK IKON, IKI YON: `expand_less` envantere GIRMEDI - acikken
 * [NeydiIcons.ExpandMore] 180 derece donuyor. Ikon envanteri sayili
 * (Ikonografi) ve ayni sekli iki adla tasimak onu sisirirdi.
 */
@Composable
fun TakenSection(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TakenBar(modifier.pressable(onTap = onToggle)) {
        Text(
            text = "Alındı",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = Spacing.sm),
        )
        Spacer(Modifier.weight(1f))
        NeydiIcon(
            icon = NeydiIcons.ExpandMore,
            // Bolumun ADI zaten "Alindi" yaziyor; ikon durumu tekrar ediyor.
            contentDescription = null,
            size = CHEVRON,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.rotate(if (expanded) 180f else 0f),
        )
    }
}

/**
 * Iki halin ortak kabugu: ustunde sac teli, maketin dolgulari.
 *
 * Sac teli bir SUS degil AYRIM: blok listenin devami degil, listenin
 * KAPANISI. Cizgi olmadan son reyon bolumunun kuyrugu gibi okunuyordu -
 * bolum basliklarinda ayni hata bir tur once cihazda yakalanmisti.
 */
@Composable
private fun TakenBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val extras = LocalNeydiExtraColors.current
    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .height(Sizes.hairline)
                .background(extras.hairline),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.md,
                    end = Spacing.md,
                    top = ABOVE_LABEL,
                    bottom = BELOW_LABEL,
                ),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** *"Alindi . 12/18"* - kararin verdigi bicim (karar 124). */
internal fun takenCounterText(taken: Int, total: Int): String = "Alındı · $taken/$total"

/** Maketin olculeri: cizginin altinda 12px, etiketin altinda 4px. */
private val ABOVE_LABEL = 12.dp
private val BELOW_LABEL = 4.dp

/** `expand_more` 20dp - maketin olcusu. */
private val CHEVRON = 20.dp

// --- Onizlemeler ------------------------------------------------------------
// Uc hal yan yana: sayac (alisveris), kapali bolum ve acik bolum (sonrasi).
// Ayni blogun iki modda iki farkli VAAT tasidigini ancak birlikte gorulunce
// anlasiliyor - birinde chevron yok, otekinde donuyor.

@PreviewLightDark
@Composable
private fun TakenBlockPreview() = NeydiPreview {
    TakenCounter(taken = 12, total = 18)
    TakenSection(count = 12, expanded = false, onToggle = {})
    TakenSection(count = 12, expanded = true, onToggle = {})
}
