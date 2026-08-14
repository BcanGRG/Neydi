package com.neydi.app.ui.finish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.ui.components.ListItemRow
import com.neydi.app.ui.components.ListRow
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FinishShoppingRoute(tripId: String?, onDone: () -> Unit) {
    val vm: FinishShoppingViewModel = koinViewModel { parametersOf(tripId) }
    val rows by vm.rows.collectAsStateWithLifecycle()
    FinishShoppingScreen(rows = rows, onToggle = vm::setTaken, onDone = onDone)
}

/**
 * Fissiz mutabakatin duzeltme ekrani (F4.8).
 *
 * VARSAYILAN ZATEN "ALINDI": gezi kapanirken planlananlar alindi yazildi ve
 * hicbir sey yapmayan kullanici icin dogru sonuc bu. Buradaki is yalnizca
 * ISTISNAYI isaretlemek - "bunu bulamadim".
 *
 * Iyimser varsayimi duzeltilebilir yapmadan birakmak onu bir TUZAGA cevirirdi:
 * kullanici hic almadigi urunun satin alma gecmisine girdigini gormez, oneri
 * motoru da o urunu duzenli aliniyor sanip yanlis araliktan onerir.
 *
 * Satirlar Liste ekraninin AYNI bilesenini kullaniyor: onay hedefi, basili
 * hali ve olculeri orada zaten tasarlandi, ikinci bir satir bileseni iki yerde
 * ayrisan iki gorunum demek olurdu.
 */
@Composable
fun FinishShoppingScreen(
    rows: List<FinishRow>,
    onToggle: (String, Boolean) -> Unit,
    onDone: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Spacing.md)) {
            Text(
                text = "Almadıklarını işaretle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Hepsi alındı sayıldı. Bulamadıklarının işaretini kaldır.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.md))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                items(rows.size, key = { rows[it].id }) { i ->
                    val row = rows[i]
                    ListItemRow(
                        row = ListRow(
                            name = row.name,
                            quantity = quantityBadge(row.count, row.unit),
                            checked = row.taken,
                        ),
                        onToggle = { onToggle(row.id, !row.taken) },
                    )
                }
            }

            NeydiButton(
                text = "Tamam",
                onClick = onDone,
                modifier = Modifier.padding(top = Spacing.sm).fillMaxWidth(),
            )
        }
    }
}

/**
 * Adet rozetinin metni. Adet 1 ise null - rozet CIZILMEZ.
 *
 * "1 adet ekmek" yazmak her satira ayni bilgiyi ekler ve gozun aradigi sey
 * (2'li, 0,5 kg'lik istisnalar) gurultunun icinde kaybolur.
 */
internal fun quantityBadge(count: Double, unit: String): String? {
    if (count == 1.0 && (unit == "adet" || unit == "ad")) return null
    val text = if (count % 1.0 == 0.0) count.toInt().toString() else count.toString().replace('.', ',')
    return if (unit == "adet" || unit == "ad") "${text}x" else "$text $unit"
}

// --- Onizlemeler ------------------------------------------------------------

private val sampleRows = listOf(
    FinishRow("1", "Ekmek", 1.0, "adet", true),
    FinishRow("2", "Süt", 2.0, "L", true),
    FinishRow("3", "Domates", 0.5, "kg", false),
)

@PreviewLightDark
@Composable
private fun FinishShoppingPreview() = NeydiPreview {
    FinishShoppingScreen(rows = sampleRows, onToggle = { _, _ -> }, onDone = {})
}

@PreviewLightDark
@Composable
private fun FinishShoppingEmptyPreview() = NeydiPreview {
    FinishShoppingScreen(rows = emptyList(), onToggle = { _, _ -> }, onDone = {})
}
