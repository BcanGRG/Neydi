package com.neydi.app.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Kurulum (Ekran 8) - IKI ADIM (tasarim karari 6).
 *
 * Tasarim uc adim cizmisti: hane, her zamankiler, tempo. Hane adimi auth ile
 * birlikte Faz 7'de geliyor; var olmayan bir auth icin e-posta alani cizmek
 * tutulamayacak bir soz vermek olurdu.
 *
 * "ATLA" IKI ADIMDA DA VAR. Kurulum bir kapi degil bir kisayol: atlayan
 * kullanici Liste'nin ilk gun bos haline dusuyor ve orada da tek dokunusla
 * ilk satirini yaratabiliyor (tasarim karari 5).
 */
@Composable
fun SetupRoute(onFinish: () -> Unit) {
    val vm: SetupViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    SetupScreen(
        state = state,
        onToggle = vm::toggle,
        onNext = vm::next,
        onChooseTempo = vm::chooseTempo,
        onFinish = { vm.finish(onFinish) },
    )
}

@Composable
fun SetupScreen(
    state: SetupState,
    onToggle: (String) -> Unit,
    onNext: () -> Unit,
    onChooseTempo: (Int?) -> Unit,
    onFinish: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            StepBar(step = state.step, total = state.stepCount)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(top = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${state.step + 1} / ${state.stepCount}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    // Tasarim bu sayaci sicak bir amber tonda ("#8A5A00")
                    // yaziyor; token karsiligi `warning` - ayni ailenin
                    // metin tonu, ve ikisini ayri renk yapmak temanin tek
                    // amber ailesini ikiye bolerdi.
                    color = LocalNeydiExtraColors.current.warning,
                    modifier = Modifier.weight(1f),
                )
                // ATLA: son adimda da duruyor. Tempo zorunlu degil - "Belirsiz"
                // zaten gecerli bir cevap ve atlamak onunla ayni yere cikiyor.
                Box(
                    modifier = Modifier
                        .clip(NeydiExtraShapes.pill)
                        .pressable(onTap = onFinish)
                        .heightIn(min = Sizes.minTapTarget)
                        .padding(horizontal = Spacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Atla",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            when (state.step) {
                0 -> StaplesStep(state, onToggle, onNext, Modifier.weight(1f))
                else -> TempoStep(state, onChooseTempo, onFinish, Modifier.weight(1f))
            }
        }
    }
}

/**
 * Ust ilerleme cubugu: adim SAYISI kadar segment.
 *
 * Tasarimin uc adimli cerceveleri uc segment ciziyor; iki adima inince segment
 * sayisi da iniyor. Uc segment cizip birini hic doldurmamak, kullaniciya
 * gelmeyecek bir adim soz vermek olurdu.
 */
@Composable
private fun StepBar(step: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(NeydiExtraShapes.pill)
                    .background(
                        if (i <= step) MaterialTheme.colorScheme.primary
                        else LocalNeydiExtraColors.current.hairline,
                    ),
            )
        }
    }
}

/** 1 / 2 - "Her alisveriste aldiklariniz". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StaplesStep(
    state: SetupState,
    onToggle: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Her alışverişte aldıklarınız",
                // Fraunces: kurulum basliklari onun gorevli oldugu dort yerden
                // biri.
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Bunlar her yeni listeye otomatik eklenir. Sonra da değiştirebilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        FlowRow(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            state.items.forEach { item ->
                SetupChip(
                    item = item,
                    // Sinira gelindiyse SECILMEMIS cipler sonuyor; secilmis
                    // olanlar dokunulabilir kaliyor ki kullanici fikrini
                    // degistirebilsin.
                    dimmed = state.atLimit && !item.selected,
                    onClick = { onToggle(item.seedId) },
                )
            }
        }
        Footer {
            NeydiButton(
                // SAYI BUTONDA: kullanici kac tane sectigini gormeden devam
                // etmemeli, cunku sinir on iki ve Ayarlar'da ayni sayiyi
                // yeniden gorecek.
                text = "Devam (${state.selectedCount} seçildi)",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 2 / 2 - "Ne siklikla markete gidiyorsunuz?". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TempoStep(
    state: SetupState,
    onChoose: (Int?) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = "Ne sıklıkla markete gidiyorsunuz?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "\"12 gündür almadın\" gibi gerekçeleri buna göre hesaplıyoruz.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                TEMPO_OPTIONS.forEach { option ->
                    TempoChip(
                        label = option.label,
                        selected = state.tempoChosen && state.tempoDays == option.days,
                        onClick = { onChoose(option.days) },
                    )
                }
            }
        }
        Footer {
            NeydiButton(
                text = "Listeme geç",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Alt buton blogu: ustunde hairline, tasarimin 12/16/34 dolgusu. */
@Composable
private fun Footer(content: @Composable () -> Unit) {
    val extras = LocalNeydiExtraColors.current
    Column {
        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
        Box(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) { content() }
    }
}

/**
 * Urun cipi - secilince dolu, secilmeyince konturlu.
 *
 * Secim isareti `check` ikonu: renk tek basina yeterli degil (renk korlugu) ve
 * tasarim da ikonu ciziyor.
 */
@Composable
private fun SetupChip(item: SetupItem, dimmed: Boolean, onClick: () -> Unit) {
    val extras = LocalNeydiExtraColors.current
    Row(
        modifier = Modifier
            .clip(NeydiExtraShapes.pill)
            .pressable(enabled = !dimmed, onTap = onClick)
            .background(
                if (item.selected) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .heightIn(min = 40.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.selected) {
            NeydiIcon(
                icon = NeydiIcons.Check,
                contentDescription = null,
                size = 16.dp,
                tint = MaterialTheme.colorScheme.onSecondary,
            )
        }
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = when {
                item.selected -> MaterialTheme.colorScheme.onSecondary
                dimmed -> extras.hairline
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(start = if (item.selected) 6.dp else 0.dp),
        )
    }
}

/** Tempo cipi - tek secim. */
@Composable
private fun TempoChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(NeydiExtraShapes.pill)
            .pressable(onTap = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .heightIn(min = Sizes.minTapTarget)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

// --- Onizlemeler ------------------------------------------------------------

private val sampleItems = listOf(
    "Ekmek", "Süt", "Yumurta", "Peynir", "Zeytin", "Çay", "Şeker", "Un",
    "Domates", "Salatalık", "Soğan", "Patates", "Elma", "Muz", "Tavuk", "Kıyma",
).mapIndexed { i, name -> SetupItem("s$i", name, selected = i < 5) }

@PreviewLightDark
@Composable
private fun SetupStaplesPreview() = NeydiPreview {
    SetupScreen(
        state = SetupState(loading = false, step = 0, items = sampleItems),
        onToggle = {}, onNext = {}, onChooseTempo = {}, onFinish = {},
    )
}

/** SINIRA GELINMIS hal: secilmemis cipler sonuk, secilmisler hala dokunulabilir. */
@PreviewLightDark
@Composable
private fun SetupStaplesAtLimitPreview() = NeydiPreview {
    SetupScreen(
        state = SetupState(
            loading = false,
            step = 0,
            items = sampleItems.mapIndexed { i, it -> it.copy(selected = i < 12) },
        ),
        onToggle = {}, onNext = {}, onChooseTempo = {}, onFinish = {},
    )
}

@PreviewLightDark
@Composable
private fun SetupTempoPreview() = NeydiPreview {
    SetupScreen(
        state = SetupState(loading = false, step = 1, tempoDays = 10, tempoChosen = true),
        onToggle = {}, onNext = {}, onChooseTempo = {}, onFinish = {},
    )
}
