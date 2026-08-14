package com.neydi.app.ui.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.formatMinor
import com.neydi.app.data.receipt.UNREADABLE_MESSAGE
import com.neydi.app.data.parseMinorInput
import com.neydi.app.ui.components.AccentChip
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Spacing
import androidx.compose.foundation.text.KeyboardOptions

/**
 * Fis Kontrol ekrani (F4.6).
 *
 * BU EKRAN GUVEN KAPISI: OCR'in ne okudugunu kullaniciya gosteren ve
 * duzelttiren tek yer. Okuma sonucunu sessizce veritabanina yazmak, yanlis
 * fiyatlari kullanici hic gormeden fiyat hafizasina gomerdi - ve "gecen sefer
 * ne odedik" cevabi bir daha guvenilmez olurdu.
 *
 * Her satirin altinda FISTE YAZAN ham metin gri duruyor. Yanlis eslesmeyi
 * geri alabilen tek sey bu: kullanici "Süt" yazan satira bakip fisin aslinda
 * "SUTLU CIKOLATA" dedigini gorebiliyor.
 */
@Composable
fun ReceiptCheckScreen(
    state: CheckState,
    editing: CheckRow?,
    suggestions: List<CatalogSeed>,
    onEdit: (CheckRow) -> Unit,
    onDismissEdit: () -> Unit,
    onConfirm: (CheckRow, String) -> Unit,
    onFixAmount: (CheckRow, Long) -> Unit,
    onReread: () -> Unit,
    onBack: () -> Unit,
) {
    // SURFACE ZORUNLU: ciplak Column karanlik modda themes.xml'deki sabit
    // acik zemini gosteriyordu (bkz. HistoryScreen'deki ayni not). Onizleme
    // bunu maskeliyor cunku NeydiPreview kendi Surface'ini sariyor.
    //
    // safeDrawingPadding da SART: cihazda baslik durum cubugunun ALTINA girdi -
    // saat ve pil gostergesi magaza adinin uzerine bindi. iOS'ta notch ile
    // daha kotu olurdu.
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Spacing.md)) {
        Text(
            text = state.storeName ?: "Fiş",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Spacing.sm))

        when {
            state.loading -> LoadingBlock()
            state.failedMessage != null -> UnreadableNotice(state.failedMessage, onReread)
            else -> {
                GateChip(state)
                state.notice?.let {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    items(state.rows, key = { it.id }) { row ->
                        CheckRowItem(row) { onEdit(row) }
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
                // Yon dogru bulunmus olsa bile buton duruyor: otomatik secim iki
                // fiste dogru bildi, ucuncude yanlis bilirse kullanici tikanmasin.
                RotateRow(onReread)
                Spacer(Modifier.height(Spacing.sm))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Tamam") }
            }
        }
    }

    }

    if (editing != null) {
        CorrectionSheet(
            row = editing,
            suggestions = suggestions,
            onDismiss = onDismissEdit,
            onConfirm = onConfirm,
            onFixAmount = onFixAmount,
        )
    }
}

@Composable
private fun LoadingBlock() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator()
        // Row icinde `height` YATAYDA SIFIR yer kaplar. Ilk hali
        // `Modifier.height(...)` idi ve bosluk metnin basina konan iki
        // gorunmez bosluk karakteriyle "yaklasik dogru" gorunuyordu - bu
        // yuzden kimse fark etmedi. Dogrusu `width`.
        Spacer(Modifier.width(Spacing.sm))
        Text("Fiş okunuyor…", style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Aritmetik kapisinin gorunen yuzu (F4.5).
 *
 * UC DURUM, IKI DEGIL: toplam okunamadiginda "tutmuyor" demek bizim okuma
 * hatamizi kullanicinin hatasi gibi gostermek olurdu.
 */
@Composable
private fun GateChip(state: CheckState) {
    val extras = LocalNeydiExtraColors.current
    val (text, color) = when (state.gateHolds) {
        true -> "Toplam tutuyor · ${formatMinor(state.totalMinor ?: 0)}" to extras.success
        false -> {
            val fark = (state.totalMinor ?: 0) - state.sumMinor
            "Toplam ${formatMinor(fark)} tutmuyor" to extras.warning
        }
        null -> "Toplam okunamadı · satırlar ${formatMinor(state.sumMinor)}" to extras.warning
    }
    Surface(color = color, shape = NeydiExtraShapes.pill) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp),
        )
    }
}

/**
 * Tek fis satiri: uc durumdan biri.
 *
 * eslesti / yeni urun / emin degil. Ucu de ayni yerde ve AYNI DOKUNUSLA
 * duzeltilebiliyor - "emin degil" icin ayri bir onay akisi kurmak kullaniciyi
 * iki farkli sey ogrenmeye zorlardi.
 */
@Composable
private fun CheckRowItem(row: CheckRow, onClick: () -> Unit) {
    val extras = LocalNeydiExtraColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (row.needsReview) extras.accent.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm).heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = row.productName ?: "Eşleşmedi",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (row.needsReview) FontWeight.Normal else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (row.needsReview) {
                        AccentChip(
                            text = if (row.productName == null) "yeni" else "emin değil",
                            modifier = Modifier.padding(start = Spacing.xs),
                        )
                    }
                }
                // FISTE YAZAN HALI. Bu satir silinemez: yanlis eslesmeyi geri
                // alabilen tek ipucu bu.
                Text(
                    text = row.rawText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = formatMinor(row.amountMinor, currency = ""),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = Spacing.sm),
            )
        }
    }
}

/** Okunamayan fis: sebebi ve cikis yolu birlikte (F4.4 olcumu). */
@Composable
private fun UnreadableNotice(message: String, onReread: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = NeydiExtraShapes.card,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Spacing.md),
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        RotateRow(onReread)
    }
}

/**
 * Yonu elle cevirip yeniden okuma - TEK BUTON.
 *
 * Ilk halinde "Duz oku" (0 derece) diye ikinci bir buton vardi ve cihazda
 * calisan bir okumayi bozdu: fis dondurulmus cekilmis, 0 derece iki satir
 * okuyor. Kullaniciya hangi acinin dogru oldugunu sormak anlamsiz - onun
 * bildigi tek sey "yanlis gorunuyor, baskasini dene".
 */
@Composable
private fun RotateRow(onReread: () -> Unit) {
    OutlinedButton(onClick = onReread) { Text("↻ Başka yönde oku") }
}

/**
 * Duzeltme sheet'i - UC DOKUNUS SOZLESMESI.
 *
 * Satira dokun (1) -> oneriden sec (2) -> bitti. Ad yazmak ya da tutari
 * duzeltmek de ayni sheet'te; kullanicinin iki ayri ekran ogrenmesi gerekmiyor.
 *
 * Tutar alani uygulamanin TEK sayisal klavyesi. Baska hicbir yerde sayi
 * yazdirmiyoruz - miktar bile +/- ile giriliyor.
 */
// ModalBottomSheet hala @ExperimentalMaterial3Api - Liste ekraninda da ayni
// gerekce: sheet'i kendimiz yazmak daha buyuk bir risk.
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CorrectionSheet(
    row: CheckRow,
    suggestions: List<CatalogSeed>,
    onDismiss: () -> Unit,
    onConfirm: (CheckRow, String) -> Unit,
    onFixAmount: (CheckRow, Long) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // ZEMIN RENGI ACIKCA VERILIYOR: bu palet `surfaceContainer*` tonal
        // token'larini tanimlamiyor ve M3 kendi mor baseline'ina dusuyor.
        // Ekle sheet'i ile ozet karti bunun icin duzeltilmisti, bu sheet
        // atlanmisti (bkz. ListScreen).
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                // KAYDIRMA + YUKSEKLIK BUTCESI ZORUNLU.
                //
                // Kismi acik ModalBottomSheet icerigi SINIRSIZ yukseklikle
                // olcuyor ve tasan icerigi kaydirmiyor, KIRPIYOR (F3.7'de bes
                // denemeyle ogrenildi, F10.5 acik). Bu sheet en kotu halde 8
                // oneri + 2 metin alani + buton tasiyor (~520dp) ve en altta
                // duran "Kaydet" - yani F4.7 alias ogrenmesini yazan TEK
                // dugme - ilk kirpilan sey olur.
                .heightIn(max = SHEET_MAX_HEIGHT)
                .verticalScroll(rememberScrollState())
                // Uygulamanin TEK sayisal klavyesi burada; imePadding olmadan
                // klavye tutar alanini kapatiyor.
                .imePadding()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(Spacing.md),
        ) {
            Text(
                text = row.rawText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.sm))

            suggestions.forEach { seed ->
                Surface(
                    onClick = { onConfirm(row, seed.name) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs),
                ) {
                    Text(
                        text = seed.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(Spacing.sm),
                    )
                }
            }

            var name by remember(row.id) { mutableStateOf(row.productName ?: "") }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ürün adı") },
                singleLine = true,
                shape = NeydiExtraShapes.textField,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.xs))

            var amount by remember(row.id) {
                mutableStateOf(formatMinor(row.amountMinor, currency = ""))
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Tutar") },
                singleLine = true,
                shape = NeydiExtraShapes.textField,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.sm))

            Button(
                onClick = {
                    // Tutar degistiyse once onu yaz: ad onayi sheet'i kapatiyor,
                    // ters sirada tutar duzeltmesi sessizce kaybolurdu.
                    parseMinorInput(amount)
                        ?.takeIf { it != row.amountMinor }
                        ?.let { onFixAmount(row, it) }
                    if (name.isNotBlank() && name != row.productName) onConfirm(row, name.trim())
                    else onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Kaydet") }
        }
    }
}


/**
 * Hedef ile ekran arasindaki bag.
 *
 * ViewModel'i receiptId PARAMETRESIYLE aliyor: hangi fisi kontrol ettigimiz
 * hedefin kendisinde yaziyor. Bunu bir global "secili fis" state'i uzerinden
 * yapmak, Gecmis'ten iki fisi ust uste acmayi bozardi.
 */
@Composable
fun ReceiptCheckRoute(receiptId: String, onBack: () -> Unit) {
    val vm: ReceiptCheckViewModel = koinViewModel { parametersOf(receiptId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val editing by vm.editing.collectAsStateWithLifecycle()
    val suggestions by vm.suggestions.collectAsStateWithLifecycle()

    ReceiptCheckScreen(
        state = state,
        editing = editing,
        suggestions = suggestions,
        onEdit = vm::edit,
        onDismissEdit = vm::dismissEdit,
        onConfirm = vm::confirm,
        onFixAmount = vm::fixAmount,
        onReread = vm::rereadNextRotation,
        onBack = onBack,
    )
}

/**
 * Duzeltme sheet'inin yukseklik tavani.
 *
 * F10.5 ile ayni sinifta bir SIHIRLI SAYI, ama kirpilmaya tercih edilir:
 * kaydirma da eklendigi icin tavana carpan icerik kayboluyor degil,
 * kaydiriliyor. Kalici cozum F10.2'deki Nav3 Scene gecisi.
 */
private val SHEET_MAX_HEIGHT = 520.dp

// --- Onizlemeler ------------------------------------------------------------

private val sampleRows = listOf(
    CheckRow("1", "Krema", 10600, 2.0, "KREMA 18YAĞLI 200ML %1. *106.00", false),
    CheckRow("2", null, 8450, 1.0, "TURŞU KORNI ŞON 670G 21. *84.50", true),
    CheckRow("3", "Poşet", 100, 1.0, "ALIŞVERIŞ POŞETi BiM 220 *1.00", false),
)

@PreviewLightDark
@Composable
private fun ReceiptCheckHoldsPreview() = NeydiPreview {
    ReceiptCheckScreen(
        state = CheckState(
            loading = false,
            storeName = "BIM BIRLESIK MAGAZALAR A.S.",
            totalMinor = 19150,
            sumMinor = 19150,
            gateHolds = true,
            rows = sampleRows,
        ),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onReread = {}, onBack = {},
    )
}

@PreviewLightDark
@Composable
private fun ReceiptCheckMismatchPreview() = NeydiPreview {
    ReceiptCheckScreen(
        state = CheckState(
            loading = false,
            storeName = "FiLE MARKET",
            totalMinor = 22550,
            sumMinor = 19150,
            gateHolds = false,
            rows = sampleRows,
        ),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onReread = {}, onBack = {},
    )
}

@PreviewLightDark
@Composable
private fun ReceiptCheckUnreadablePreview() = NeydiPreview {
    ReceiptCheckScreen(
        state = CheckState(loading = false, failedMessage = UNREADABLE_MESSAGE),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onReread = {}, onBack = {},
    )
}

/**
 * ONIZLEMESI OLMAYAN HALLER BU OTURUMDA UC HATA SAKLADI, o yuzden ucu de
 * burada: yukleme satirinda `height` ile yatay bosluk verilmisti (Row icinde
 * hicbir sey yapmaz), duzeltme sheet'i M3'un mor zeminine dusuyordu ve
 * kirpilma riski tasiyordu, kapinin ucuncu hali (toplam okunamadi) hic
 * cizilmemisti.
 */
@PreviewLightDark
@Composable
private fun ReceiptCheckLoadingPreview() = NeydiPreview {
    ReceiptCheckScreen(
        state = CheckState(loading = true, storeName = "BIM BIRLESIK MAGAZALAR A.S."),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onReread = {}, onBack = {},
    )
}

/** Kapinin UCUNCU hali: toplam okunamadi - "tutmadi" ile ayni sey DEGIL. */
@PreviewLightDark
@Composable
private fun ReceiptCheckTotalUnreadablePreview() = NeydiPreview {
    ReceiptCheckScreen(
        state = CheckState(
            loading = false,
            storeName = "AKYURT",
            totalMinor = null,
            sumMinor = 19150,
            gateHolds = null,
            rows = sampleRows,
        ),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onReread = {}, onBack = {},
    )
}

/**
 * Duzeltme sheet'i EN KOTU HALIYLE: 8 oneri + iki alan + buton.
 *
 * Kirpilma riskini gorunur kilan tek sey bu - onizleme olmadigi icin sheet
 * cihazda hic bu doluluktta gorulmedi.
 */
@PreviewLightDark
@Composable
private fun ReceiptCheckCorrectionPreview() = NeydiPreview {
    ReceiptCheckScreen(
        state = CheckState(loading = false, storeName = "FiLE MARKET", rows = sampleRows),
        editing = sampleRows[1],
        suggestions = List(8) { i ->
            CatalogSeed(
                id = "seed-$i", name = "Turşu ${i + 1}", matchKey = "tursu$i",
                categoryId = "konserve-salca", commonalityRank = i + 1, defaultUnit = "adet",
            )
        },
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onReread = {}, onBack = {},
    )
}
