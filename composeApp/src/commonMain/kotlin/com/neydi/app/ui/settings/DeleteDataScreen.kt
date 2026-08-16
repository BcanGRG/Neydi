package com.neydi.app.ui.settings

import androidx.compose.foundation.background
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
 * "Verilerimi sil" onayi - DIALOG DEGIL, DESTINASYON (tasarim karari 2).
 *
 * v1'in "sifir modal dialog" kurali bozulmuyor, silme de onaysiz calismiyor.
 * Destinasyon uc isi birden yapiyor: geri tusu dogal bir vazgecme yolu oluyor,
 * ekran neyin gidecegini SAYIYLA yaziyor, ve "Verileri sil" tek basina duruyor -
 * yani yanlislikla basilabilecek bir komsusu yok.
 *
 * Geri alinamaz bir isi snackbar suresine sikistirmak olmazdi; satir ici iki
 * asamali dokunus ise kazayi yalnizca bir dokunus uzaga tasir.
 */
@Composable
fun DeleteDataRoute(onBack: () -> Unit, onDeleted: () -> Unit) {
    val vm: DeleteDataViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    DeleteDataScreen(
        state = state,
        onBack = onBack,
        onDelete = { vm.delete(onDeleted) },
    )
}

@Composable
fun DeleteDataScreen(
    state: DeleteDataState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
) {
    val extras = LocalNeydiExtraColors.current
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(NeydiExtraShapes.pill)
                        .pressable(onTap = onBack)
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
                    text = "Verilerimi sil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Hairline()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg)
                    .padding(top = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(
                    text = "Bunlar geri gelmez",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Silme hanenin tamamı için çalışır ve anında yapılır. " +
                        "Geri alma yok, yedek yok.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (state.rows.isEmpty()) {
                    // SILINECEK SEY YOKKEN SAYI LISTESI CIZILMIYOR. Bos bir
                    // "gidecekler" listesi, silmeyi bir sey yapacakmis gibi
                    // gosterirdi.
                    Text(
                        text = "Şu an silinecek bir veri yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column {
                        state.rows.forEach { row ->
                            Hairline()
                            Row(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = row.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = row.count,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                Hairline()
                Text(
                    text = buildString {
                        // FOTOGRAF CUMLESI KALKTI (tasarim karari 29):
                        // etiket fotografi kayittan hemen sonra siliniyor,
                        // yani silinecek bir fotograf zaten yok. Olmayan bir
                        // seyi silineceklerin arasinda saymak, ekranin kendi
                        // kuralini bozardi.
                        append("Bu işlem geri alınamaz.")
                        // Es uyarisi YALNIZCA iki kisilik hanede. Tek kisilik
                        // hanede olmayan bir riski varmis gibi gostermek olurdu.
                        if (state.warnsSpouse) {
                            append(" Eşinin cihazındaki veri de silinir; hane dağılır.")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
                Spacer(Modifier.height(Spacing.md))
            }

            Hairline()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NeydiButton(
                    text = "Verileri sil",
                    onClick = onDelete,
                    // Silinecek bir sey yokken dugme KAPALI: hicbir sey
                    // yapmayan bir kirmizi dugme, kullaniciya yaptigi seyin
                    // ne oldugu hakkinda yalan soyler.
                    enabled = !state.deleting && state.rows.isNotEmpty(),
                    container = MaterialTheme.colorScheme.error,
                    content = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .clip(NeydiExtraShapes.pill)
                        .pressable(onTap = onBack)
                        .fillMaxWidth()
                        .heightIn(min = Sizes.minTapTarget),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Vazgeç",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Hairline() {
    val extras = LocalNeydiExtraColors.current
    Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
}

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun DeleteDataPreview() = NeydiPreview {
    DeleteDataScreen(
        state = DeleteDataState(
            rows = listOf(
                DeleteRow("Alışveriş", "18"),
                DeleteRow("Ürün ve fiyat geçmişi", "64 + 214"),
                DeleteRow("Her zamankiler, önerilmeyenler", "9 + 2"),
            ),
            warnsSpouse = true,
        ),
        onBack = {},
        onDelete = {},
    )
}

/** Yeni hane: silinecek bir sey yok, kirmizi dugme kapali. */
@PreviewLightDark
@Composable
private fun DeleteDataEmptyPreview() = NeydiPreview {
    DeleteDataScreen(state = DeleteDataState(), onBack = {}, onDelete = {})
}
