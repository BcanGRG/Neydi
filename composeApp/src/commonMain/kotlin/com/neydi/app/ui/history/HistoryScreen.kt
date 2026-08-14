package com.neydi.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.data.db.ReceiptStatus
import com.neydi.app.data.formatDayMonthTime
import com.neydi.app.data.formatDayMonthYear
import com.neydi.app.data.formatMinor
import com.neydi.app.ui.components.AccentChip
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * Gecmis (F4.9).
 *
 * YANLIS OKUNMUS BIR FISE GERI DONMENIN TEK YOLU - ve uygulamanin en ucuz
 * ekrani. Bir fis burada gorunmuyorsa erisilemez demektir: fotograf diskte
 * durur, para hafizaya girmez, kullanici sebebini hic ogrenemez. Bu yuzden
 * BASARISIZ fisler de listede ve dokunulabilir.
 */
@Composable
fun HistoryRoute(onBack: () -> Unit, onOpenReceipt: (String) -> Unit) {
    val vm: HistoryViewModel = koinViewModel()
    val trips by vm.trips.collectAsStateWithLifecycle()
    HistoryScreen(trips = trips, onBack = onBack, onOpenReceipt = onOpenReceipt)
}

@Composable
fun HistoryScreen(
    trips: List<HistoryTrip>,
    onBack: () -> Unit,
    onOpenReceipt: (String) -> Unit,
) {
    // safeDrawingPadding SART: cihazda "Geri" ve baslik durum cubugunun altina
    // girdi. Placeholders'taki iskelet ekranlarda vardi, yeni ekranlarda
    // unutulmustu.
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Geri") }
            Text(
                text = "Geçmiş",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(Spacing.sm))

        if (trips.isEmpty()) {
            Text(
                text = "Henüz kapanmış alışveriş yok.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            items(trips.size, key = { trips[it].id }) { i ->
                TripBlock(trips[i], onOpenReceipt)
            }
        }
    }
}

@Composable
private fun TripBlock(trip: HistoryTrip, onOpenReceipt: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatDayMonthYear(trip.closedAt),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                // null ise "-": fis okunmadigi icin BILMIYORUZ. Sifir yazmak
                // "bedava alisveris" demek olurdu.
                text = trip.totalMinor?.let { formatMinor(it) } ?: "—",
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        if (trip.receipts.isEmpty()) {
            Text(
                text = "Fiş eklenmedi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            trip.receipts.forEach { receipt ->
                ReceiptRow(receipt) { onOpenReceipt(receipt.id) }
                Spacer(Modifier.height(Spacing.xs))
            }
        }
    }
}

@Composable
private fun ReceiptRow(receipt: HistoryReceipt, onClick: () -> Unit) {
    val extras = LocalNeydiExtraColors.current
    val sorunlu = receipt.status == ReceiptStatus.FAILED ||
        receipt.status == ReceiptStatus.MISMATCHED
    Surface(
        onClick = onClick,
        shape = NeydiExtraShapes.card,
        color = if (sorunlu) extras.accent.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm).heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = receipt.storeName ?: "Mağaza okunamadı",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatDayMonthTime(receipt.capturedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            statusLabel(receipt.status)?.let {
                AccentChip(text = it, modifier = Modifier.padding(end = Spacing.xs))
            }
            Text(
                text = receipt.totalMinor?.let { formatMinor(it, currency = "") } ?: "—",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                // Cihazda olculdu: cip yokken (VERIFIED fis) magaza adi butun
                // genisligi doldurup tutara SIFIR boslukla degiyordu -
                // "...ANKARA484,58" tek kelime gibi okunuyordu. Ad kirpilmiyor,
                // sadece araya nefes giriyor.
                modifier = Modifier.padding(start = Spacing.sm),
            )
        }
    }
}

/**
 * Sorunlu durumlar ETIKET ALIR, saglikli olanlar ALMAZ.
 *
 * VERIFIED'a "tamam" cipi koymak listeyi gurultuye cevirirdi: iyi durum
 * sessiz olmali, gozun aradigi sey istisna.
 */
private fun statusLabel(status: ReceiptStatus): String? = when (status) {
    ReceiptStatus.PENDING -> "bekliyor"
    ReceiptStatus.READING -> "okunuyor"
    ReceiptStatus.MISMATCHED -> "toplam tutmuyor"
    ReceiptStatus.FAILED -> "okunamadı"
    ReceiptStatus.VERIFIED -> null
}

// --- Onizlemeler ------------------------------------------------------------

private val sampleTrip = HistoryTrip(
    id = "t1",
    closedAt = 1_755_100_000_000,
    totalMinor = 22550,
    receipts = listOf(
        HistoryReceipt("r1", ReceiptStatus.VERIFIED, 22550, "BIM BIRLESIK MAGAZALAR", 1_755_100_000_000),
        HistoryReceipt("r2", ReceiptStatus.FAILED, null, null, 1_755_099_000_000),
    ),
)

@PreviewLightDark
@Composable
private fun HistoryPreview() = NeydiPreview {
    HistoryScreen(
        trips = listOf(
            sampleTrip,
            HistoryTrip("t2", 1_754_900_000_000, null, emptyList()),
        ),
        onBack = {},
        onOpenReceipt = {},
    )
}

@PreviewLightDark
@Composable
private fun HistoryEmptyPreview() = NeydiPreview {
    HistoryScreen(trips = emptyList(), onBack = {}, onOpenReceipt = {})
}
