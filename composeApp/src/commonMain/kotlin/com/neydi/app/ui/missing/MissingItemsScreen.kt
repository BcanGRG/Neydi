package com.neydi.app.ui.missing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Ekran 3 - "Eksik Olabilir" (F6.4).
 *
 * EVDEN CIKMADAN ONCEKI SON KONTROL. Uygulamanin kullaniciya "sen unuttun"
 * diyebildigi tek yer, ve tam bu yuzden en dikkatli davranmasi gereken yer:
 * her satir DUZ TURKCE bir gerekce tasiyor, tahmin bolumu varsayilan kapali.
 *
 * BOS ISE EKRAN HIC ACILMAZ - tasarimin *"en onemli bos-durum karari"*.
 * Bos bir kontrol listesi kullaniciya butonun degersiz oldugunu ogretir, ve
 * bir kez ogrendikten sonra bir daha dokunmaz. [onSkip] o yolu isletiyor.
 */
@Composable
fun MissingItemsRoute(
    /** @param skipped ekran hic gorunmeden atlandiysa true - cagiran taraf toast gosteriyor. */
    onEnterShopping: (skipped: Boolean) -> Unit,
    onCancel: () -> Unit,
) {
    val vm: MissingItemsViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    // Onerilecek bir sey yoksa ekran hic gorunmuyor: dogrudan alisveris
    // moduna geciliyor. Kullanici "Alisverise cikiyorum"a bastigini
    // hatirliyor, arada bos bir ekran gormuyor.
    LaunchedEffect(state.shouldSkip) {
        if (state.shouldSkip) vm.skipToShopping { onEnterShopping(true) }
    }

    // ARA KARE CIZILMIYOR (F11.21). Uc ayri yerde yazili: *"ekran hic
    // acilmaz, dogrudan alisveris moduna girilir, 2 sn'lik toast cikar."*
    //
    // ONCEKI HALI IHLAL EDIYORDU ve sebebi ince: `shouldSkip` = `!loading &&
    // rows.isEmpty()`, yani YUKLENIRKEN henuz false. Ekran o arada sifir
    // satirla ciziliyordu - kullanici "Eksik olabilir (0)" diye bos bir kare
    // gorup sonra atlanidigini yasiyordu.
    //
    // Yerine hicbir sey konmuyor: sozlesme *"kurulum disinda hicbir ekran tam
    // ekran yukleme gostermez"* ve *"bos ekran acilmaz"* diyor. Yukleme bir
    // veritabani sorgusu kadar suruyor; oraya iskelet koymak da bir kare
    // olurdu.
    if (!state.loading && !state.shouldSkip) {
        MissingItemsScreen(
            state = state,
            onToggle = vm::toggle,
            onAdd = { vm.addSelected { onEnterShopping(false) } },
            onCancel = onCancel,
        )
    }
}

@Composable
fun MissingItemsScreen(
    state: MissingState,
    onToggle: (String) -> Unit,
    onAdd: () -> Unit,
    onCancel: () -> Unit,
) {
    val extras = LocalNeydiExtraColors.current
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(NeydiExtraShapes.pill)
                        .pressable(onTap = onCancel)
                        .size(Sizes.minTapTarget),
                    contentAlignment = Alignment.Center,
                ) {
                    NeydiIcon(
                        icon = NeydiIcons.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    // CANLI SAYI baslikta: kullanici kac tanesini listeye
                    // alacagini butona bakmadan biliyor.
                    text = "Eksik olabilir (${state.selectedCount})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            LazyColumn(Modifier.weight(1f)) {
                MissingSection.entries.forEach { section ->
                    val rows = state.rows.filter { it.section == section }
                    // BOS BOLUM CIZILMEZ.
                    if (rows.isEmpty()) return@forEach
                    item(key = "b-${section.name}") {
                        Column(Modifier.padding(top = Spacing.md, bottom = 6.dp)) {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            // Not TEK SATIR ve muted: baslikla yarismiyor.
                            Text(
                                text = section.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                    items(rows, key = { it.productId }) { row ->
                        MissingItemRow(row) { onToggle(row.productId) }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            NeydiButton(
                text = "Ekle (${state.selectedCount})",
                onClick = onAdd,
                enabled = state.selectedCount > 0,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.xs))
            // IKI ESIT AGIRLIKLI BUTON DEGIL: tasarim "Bosver"i metin dugmesi
            // yapiyor. Esit agirlik vermek "eklemek ile vazgecmek ayni sey"
            // demek olurdu; halbuki kullanici buraya eklemek icin geldi.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NeydiExtraShapes.pill)
                    .pressable(onTap = onCancel)
                    .heightIn(min = Sizes.minTapTarget),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Boşver",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

/**
 * Secim satiri.
 *
 * ONAY KUTUSU `ListItemRow`'unkinden AYRI ve bu anlamsal bir zorunluluk:
 * orada isaretli "aldim" demek, burada "listeye ekle" demek. Ayni bileseni
 * kullanmak iki farkli anlami ayni goruntuye baglardi.
 *
 * TURUNCU SOL SERIT yalnizca "gecen sefer unuttun" bolumunde: o satirlar bir
 * OLAYA dayaniyor, digerleri bir cikarima. Serit farki gozle soyluyor.
 */
@Composable
private fun MissingItemRow(row: MissingRow, onToggle: () -> Unit) {
    val extras = LocalNeydiExtraColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(onTap = onToggle)
            .heightIn(min = 68.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (row.section == MissingSection.FORGOTTEN) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(NeydiExtraShapes.pill)
                    .background(extras.accent),
            )
            Spacer(Modifier.width(Spacing.sm))
        }
        SelectTarget(row.selected)
        Spacer(Modifier.width(SizesExtra.checkbox / 2))
        Column(Modifier.weight(1f)) {
            Text(
                text = row.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = row.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Secim hedefi: bos kare -> isaretli squircle. "Alindi" onayindan ayri. */
@Composable
private fun SelectTarget(selected: Boolean) {
    val extras = LocalNeydiExtraColors.current
    Box(
        modifier = Modifier
            .size(SizesExtra.checkbox)
            .clip(if (selected) NeydiExtraShapes.checkChecked else NeydiExtraShapes.checkChecked)
            .background(if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface)
            .border(
                width = 2.dp,
                color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                shape = NeydiExtraShapes.checkChecked,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            NeydiIcon(
                icon = NeydiIcons.Check,
                contentDescription = null,
                size = 16.dp,
                tint = MaterialTheme.colorScheme.onSecondary,
            )
        }
    }
}

// --- Onizlemeler ------------------------------------------------------------

private val sample = MissingState(
    loading = false,
    rows = listOf(
        MissingRow("p1", "Yumurta", "geçen sefer unutmuştun", MissingSection.FORGOTTEN, true),
        MissingRow("p2", "Ekmek", "her seferinde alıyorsun", MissingSection.STAPLE, true),
        MissingRow("p3", "Deterjan", "34 gündür almadın, normalde 28 günde bir", MissingSection.PREDICTED, false),
    ),
)

@PreviewLightDark
@Composable
private fun MissingItemsPreview() = NeydiPreview {
    MissingItemsScreen(state = sample, onToggle = {}, onAdd = {}, onCancel = {})
}
