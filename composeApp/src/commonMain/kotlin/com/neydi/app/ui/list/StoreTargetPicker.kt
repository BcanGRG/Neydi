package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.pressable
import com.neydi.app.ui.theme.Spacing

/**
 * Hedef market secici: *"Nereye gidiyorsun?"* (karar 117).
 *
 * ## Neden karar 59'un secicisi degil
 *
 * Etiket cekimindeki [com.neydi.app.ui.capture.StorePicker] ile ayni soruyu
 * sormuyor. Orada soru *"bu etiket nerede cekildi"* ve cevap kumesi ACIK
 * UCLU - arama alani ve iki adimli "+ Yeni market" akisi var, cunku kullanici
 * markette bilinmeyen bir zincirin onunde duruyor olabilir.
 *
 * Burada soru *"bugun nereye gidiyorsun"* ve cevap kumesi KAPALI: hanenin
 * bildigi zincirler. Yeni market yaratmak planlama masasinin isi degil ve
 * arama alani da klavye acardi - dort ila sekiz cip icin.
 *
 * Ayrica o secici `Flow` paletine bagli (vizor ekraninin kendi renkleri);
 * burasi normal temada.
 *
 * ## "Belli degil" bir SECENEK, bos bir hal degil
 *
 * Ayirici cizginin altinda, cip degil satir olarak duruyor - cunku bir marketi
 * secmiyor, secimi KALDIRIYOR. Cip yapsaydik "Belli degil" adinda sekizinci
 * bir market gibi okunurdu.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StoreTargetPicker(
    stores: List<StoreOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    val extras = LocalNeydiExtraColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = SHEET_PADDING),
        verticalArrangement = Arrangement.spacedBy(SHEET_GAP),
    ) {
        Text(
            text = "Nereye gidiyorsun?",
            // 20sp/700 - maketin olcusu. `titleLarge` 22sp ve bu sheet'in
            // basligi ekranin mansetinden kucuk olmali.
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // CIPLER SARIYOR: yedi zincirin adlari 360dp'ye tek sirada sigmiyor
        // ve yatay kaydirma, gorunmeyen bir cipi kesfedilemez yapardi.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CHIP_GAP),
            verticalArrangement = Arrangement.spacedBy(CHIP_GAP),
        ) {
            stores.forEach { store ->
                val selected = store.id == selectedId
                Row(
                    modifier = Modifier
                        .height(CHIP_HEIGHT)
                        .clip(NeydiExtraShapes.pill)
                        .pressable(onTap = { onSelect(store.id) })
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.inverseSurface
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        )
                        // KENARLIK YALNIZCA SECILI OLMAYANDA. Secili cip zaten
                        // zeminiyle ayrisiyor; ona kenarlik koymak ayrimi
                        // zayiflatirdi.
                        .then(
                            if (selected) {
                                Modifier
                            } else {
                                Modifier.border(CHIP_BORDER, extras.hairline, NeydiExtraShapes.pill)
                            },
                        )
                        .padding(horizontal = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = store.name,
                        fontSize = 15.sp,
                        // SECILI OLAN DAHA KALIN (600 vs 500): renk tek basina
                        // tasiyabilirdi ama renk korlugunde ayrim kalmazdi.
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.inverseOnSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HAIRLINE)
                .background(extras.hairline),
        ) {}

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CLEAR_ROW_HEIGHT)
                .pressable(onTap = { onSelect(null) }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Belli değil",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                // IKINCIL RENK: bu bir secim degil, secimin KALDIRILMASI.
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val SHEET_PADDING = 16.dp
private val SHEET_GAP = 14.dp
private val CHIP_GAP = 8.dp
private val CHIP_HEIGHT = 44.dp
private val CHIP_BORDER = 0.8.dp
private val HAIRLINE = 1.dp

/** "Belli degil" satiri 48dp - en kucuk dokunma hedefi (karar 56). */
private val CLEAR_ROW_HEIGHT = 48.dp

@PreviewLightDark
@Composable
private fun StoreTargetPickerPreview() = NeydiPreview {
    StoreTargetPicker(
        stores = listOf(
            StoreOption("1", "BİM"),
            StoreOption("2", "A101"),
            StoreOption("3", "Migros"),
            StoreOption("4", "ŞOK"),
            StoreOption("5", "CarrefourSA"),
            StoreOption("6", "File"),
            StoreOption("7", "Tarım Kredi"),
        ),
        selectedId = "1",
        onSelect = {},
    )
}
