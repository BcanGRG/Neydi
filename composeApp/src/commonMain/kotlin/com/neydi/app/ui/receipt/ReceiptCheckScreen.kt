package com.neydi.app.ui.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.TakeOutcome
import com.neydi.app.data.formatMinor
import com.neydi.app.data.receipt.UNREADABLE_MESSAGE
import com.neydi.app.data.parseMinorInput
import com.neydi.app.ui.components.AccentChip
import com.neydi.app.ui.components.AccentSurface
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import com.neydi.app.ui.components.OutcomePicker
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.LocalNeydiTextStyles
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
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
    onOutcome: (String, TakeOutcome) -> Unit,
    onReread: () -> Unit,
    /** Uzun fisin sonraki parcasini cek (F4.13). */
    onNextPart: () -> Unit,
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
        // BASLIK EKRANIN ADI, FISIN MAGAZASI DEGIL (tasarim karari 9).
        // Magaza ve tarih alt satirda birlikte duruyor: kullanicinin
        // "hangi fis" sorusunu ikisi birlikte cevapliyor, tek basina
        // magaza adi degil.
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                text = "Fiş kontrol",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        // AD ESNER, TARIH KIRPILMAZ (tasarim karari 13). Tek Text olsaydi
        // uzun magaza adi tarihi ekran disina iterdi - cihazda tam bu oldu.
        if (state.subtitleStore != null || state.subtitleMeta != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                state.subtitleStore?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                state.subtitleMeta?.let {
                    Text(
                        text = if (state.subtitleStore != null) " · $it" else it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.sm))

        when {
            state.loading -> LoadingBlock()
            state.failedMessage != null ->
                UnreadableNotice(state.failedMessage, onReread, onNextPart)
            else -> {
                // MANSET TUTAR (tasarim karari 9).
                //
                // Ekranin isi "bu fis dogru mu" sorusunu tek bakista
                // kapatmak; satir sayisi degisken ve zaten kaydirmali, yani
                // manset olacak sabit tek sey tutar. Fraunces'in dort
                // kullanim yerinden biri "alisveris sonrasi buyuk tutar" -
                // bu onun ayni ani, yeni bir istisna degil.
                //
                // Toplam okunamadiysa manset CIZILMIYOR: dogrulanmamis bir
                // sayiyi 36sp'de manset yapmak, kullanicinin
                // sorgulayamayacagi bir yerde tahmini gercek gibi sunmak.
                // MANSET SLOTU BOS KALMIYOR (tasarim karari 15).
                //
                // Toplam okunamadiysa satirlarin toplami yaziliyor ve onundeki
                // "~" bunun fiste basili rakam DEGIL bizim toplamimiz oldugunu
                // soyluyor - sepet tahminindeki isaretin aynisi. Sessiz kalmak
                // kullaniciyi fisi elle toplamaya birakirdi.
                //
                // Onceki hal daha kotuydu: manset hic cizilmiyor, uzun cumleli
                // cip ("Toplam okunamadi · satirlar 1.085,65 TL") onun yerini
                // alip ekrani kapliyordu. Bir cip manset kadar yer kaplayamaz.
                val headline = state.totalMinor?.let { formatMinor(it) }
                    ?: state.sumMinor.takeIf { it > 0 }?.let { "~${formatMinor(it)}" }
                headline?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(Spacing.sm))
                }
                // CIP MANSETIN ALTINDA, yaninda DEGIL: amber hali
                // ("Toplam 4,20 TL tutmuyor") yan yana dizilişte manseti
                // sikistiriyordu.
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
                // TEK AKIS (tasarim karari 4): parcalar ayri ekran degil, ayni
                // listenin bolum basliklari. Parcayi ayri satir yapmak toplami
                // iki yere bolerdi; parcayi hic gostermemek ise yanlis okunan
                // parcaya erisimi kapatirdi - Gecmis tam olarak o yuzden var.
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    state.sections.forEach { section ->
                        // Tek parcali fiste baslik null ve HIC cizilmiyor.
                        section.title?.let { title ->
                            item(key = "part-${section.receiptId}") {
                                PartHeader(title = title, meta = section.meta)
                            }
                        }
                        items(section.rows, key = { it.id }) { row ->
                            CheckRowItem(row) { onEdit(row) }
                        }
                    }
                    // "DEVAMINI CEK" LISTENIN SONUNDA, alt butonlarin arasinda
                    // DEGIL: yaptigi is son satirin devami, onay degil.
                    if (state.isPart) {
                        item(key = "next-part") { NextPartRow(onNextPart) }
                    }
                }
                if (state.unaccounted.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.sm))
                    UnaccountedSection(state.unaccounted, onOutcome)
                }
                Spacer(Modifier.height(Spacing.sm))
                // Yon dogru bulunmus olsa bile buton duruyor: otomatik secim iki
                // fiste dogru bildi, ucuncude yanlis bilirse kullanici tikanmasin.
                RotateRow(onReread)
                Spacer(Modifier.height(Spacing.sm))
                NeydiButton(
                    // "Tamam" bir kapatma; tasarim burada YAPILAN ISI
                    // adlandiriyor - kullanici satirlari duzeltmis olabilir
                    // ve o duzeltmelerin kaydedildigini gormeli.
                    text = "Onayla ve kaydet",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
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
    // PARCALI FISTE SAYILAR FISIN TAMAMINA AIT, bu parcaya degil - kapi gezi
    // kapsaminda hesaplaniyor (bkz. ReceiptCheckViewModel). Ekranda yalnizca bu
    // parcanin satirlari duruyor, o yuzden hangi kumeden bahsettigimiz yaziyor;
    // yoksa kullanici ekrandaki satirlari toplayip tutturmaya calisirdi.
    val scope = if (state.isPart) "Fişin tamamı" else "Toplam"
    val (text, color) = when (state.gateHolds) {
        true -> "$scope tutuyor · ${formatMinor(state.totalMinor ?: 0)}" to extras.success
        false -> {
            val fark = (state.totalMinor ?: 0) - state.sumMinor
            "$scope ${formatMinor(fark)} tutmuyor" to extras.warning
        }
        // PARCA HALI NOTR, amber DEGIL: toplam son parcada basili, bu parcada
        // olmamasi kullanicinin hatasi degil. Amber "tutmuyor" giydirmek
        // durust ama yanlis yonlendiriciydi.
        null ->
            if (state.isPart) {
                "Parça fişi · toplam son parçada" to MaterialTheme.colorScheme.surfaceVariant
            } else {
                // KISA KALIYOR: rakam artik mansette, cip yalnizca NEREDEN
                // geldigini soyluyor (tasarim karari 15).
                "Satırlardan hesaplandı" to extras.accent
            }
    }
    // Amber dolgu ISIK MODUNDA KENARLIK ZORUNLU - AccentSurface o sozlesmenin
    // tek uygulanma yeri, elle background(accent) yazmak yasak.
    if (color == extras.accent) {
        AccentSurface(shape = NeydiExtraShapes.pill) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp),
            ) {
                NeydiIcon(
                    icon = NeydiIcons.Functions,
                    contentDescription = null,
                    size = 18.dp,
                    tint = extras.onAccent,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = extras.onAccent,
                )
            }
        }
        return
    }
    Surface(color = color, shape = NeydiExtraShapes.pill) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            // Notr (parca) cipte zemin acik - beyaz yazi kaybolurdu.
            color = if (color == MaterialTheme.colorScheme.surfaceVariant) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                Color.White
            },
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
/**
 * Parca bolum basligi (tasarim karari 4).
 *
 * Bir SATIR DEGIL: dokunulamiyor, zemini yok, kart gibi cizilmiyor. Isi
 * satirlari birbirinden ayirmak - girinti ya da kutu eklemek yeni bir bilesen
 * gerektirirdi, halbuki hiyerarsi tek bir tipografi farkiyla soyleniyor.
 */
@Composable
private fun PartHeader(title: String, meta: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        meta?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * *"Bu fişin devamını çek"* (tasarim karari 4).
 *
 * DOLGULU BUTON DEGIL: dolgulu hali "Onayla ve kaydet" ile ayni agirliktaydi
 * ve iki farkli isi ayni sesle soyluyordu. Burada yapilan is son satirin
 * devami - metin ve ikon, primary renginde.
 */
@Composable
private fun NextPartRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NeydiExtraShapes.pill)
            .pressable(onTap = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp)
            .heightIn(min = Sizes.minTapTarget),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeydiIcon(
            icon = NeydiIcons.PhotoCamera,
            contentDescription = null,
            size = 20.dp,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text = "Bu fişin devamını çek",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CheckRowItem(row: CheckRow, onClick: () -> Unit) {
    val extras = LocalNeydiExtraColors.current
    val styles = LocalNeydiTextStyles.current
    // ADI OKUNAMAYAN SATIR (tasarim karari 14): baslik slotunda barkod
    // duruyor, hata mesaji DEGIL. "Eslesmedi" yedi satirin yedisine ayni
    // cumleyi yazinca ekran hicbir sey soylemiyordu; barkod en azindan
    // satirlari birbirinden ayiriyor ve fiste gercekten yazan sey.
    val nameless = row.productName == null && row.barcode != null

    // SURFACE(onClick) DEGIL, pressable: tiklanabilir Material3 Surface
    // ripple'i sabit kodluyor ve tema override'i ona ulasmiyor (calisma
    // sozlesmesi, "Material3 Surface" maddesi).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .pressable(onTap = onClick)
            .background(
                if (row.needsReview) extras.accent.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .padding(Spacing.sm)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 4dp amber serit: "bu satir onay bekliyor" gozle soyleniyor.
        if (row.needsReview) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(NeydiExtraShapes.pill)
                    .background(extras.accent),
            )
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.productName ?: row.barcode ?: "Eşleşmedi",
                    // BARKOD AD GIBI DEGIL: bir kademe kucuk (15sp) ve
                    // ikincil renkte, yani "bu bir ad degil" bakista
                    // anlasiliyor.
                    style = if (nameless) styles.priceChip else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (row.needsReview) FontWeight.Normal else FontWeight.Medium,
                    color = if (nameless) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                // Adsiz satirda cip YOK: ikinci satir zaten ne oldugunu ve
                // ne yapilacagini soyluyor, cip ucuncu kez tekrar ederdi.
                if (row.needsReview && !nameless) {
                    AccentChip(
                        text = if (row.productName == null) "yeni" else "emin değil",
                        modifier = Modifier.padding(start = Spacing.xs),
                    )
                }
            }
            Text(
                // Adsiz satirda NE OLDUGUNU ve CIKIS YOLUNU ayni cumle
                // veriyor. Diger satirlarda fiste yazan hali duruyor -
                // yanlis eslesmeyi geri alabilen tek ipucu o.
                text = if (nameless) "adı okunamadı — dokun, ürünü seç" else row.rawText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = formatMinor(row.amountMinor, currency = ""),
            // `priceRow`, `priceChip` DEGIL (tasarim karari 10). Fis
            // satirindaki fiyat DOKUNULABILIR ve duzeltilebilir; bir kademe
            // buyuk olmasi (17sp) dokunulabilirligin kendisi, sus degil.
            style = styles.priceRow,
            modifier = Modifier.padding(start = Spacing.sm),
        )
        if (nameless) {
            Spacer(Modifier.width(Spacing.xs))
            NeydiIcon(icon = NeydiIcons.ChevronRight, contentDescription = null, size = 22.dp)
        }
    }
}

/**
 * "Listede vardi, fiste yok (N)" (F4.12, Ekran 4).
 *
 * VARSAYILAN KAPALI (tasarimda `expand_more` ile katlanmis): fisin isi bitmis
 * satirlari gostermek degil, kapanmamis hesabi sormak - ama cogu gezide bu
 * kume bos ya da kucuk ve ekrani devralmamali.
 *
 * Uc sonucun ayri olmasi F6.2'nin sarti: "gerekmedi" oneriyi bastirir,
 * "unuttum" yukseltir. Tek onay kutusu ikisini ayni sinyal yapiyordu.
 */
@Composable
private fun UnaccountedSection(
    rows: List<UnaccountedRow>,
    onOutcome: (String, TakeOutcome) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.sm)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .pressable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Listede vardı, fişte yok (${rows.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (expanded) {
                rows.forEach { row ->
                    Column(Modifier.fillMaxWidth().padding(top = Spacing.xs)) {
                        Text(
                            text = row.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        OutcomePicker(
                            selected = row.outcome,
                            onSelect = { onOutcome(row.rowId, it) },
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Okunamayan fis: sebebi ve cikis yolu BIRLIKTE (F4.4 olcumu + F4.13).
 *
 * "Parca parca cek" tavsiyesinin yanina TEK DOKUNUSLUK yol: kullanicinin ana
 * marketi uzun fis basiyor, yani bu ekran onun icin ana akis. Tavsiyeden sonra
 * Liste'ye donup ozet kartini yeniden bulmak zorunda kalmamali.
 */
@Composable
private fun UnreadableNotice(message: String, onReread: () -> Unit, onNextPart: () -> Unit) {
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
        NeydiButton(
            text = "Parça parça çek",
            onClick = onNextPart,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.xs))
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
fun ReceiptCheckRoute(
    receiptId: String,
    onBack: () -> Unit,
    /** Sonraki parca cekildi - ekran YENI fise gecmeli (ust oge degisir). */
    onOpenPart: (String) -> Unit,
) {
    val vm: ReceiptCheckViewModel = koinViewModel { parametersOf(receiptId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val editing by vm.editing.collectAsStateWithLifecycle()
    val suggestions by vm.suggestions.collectAsStateWithLifecycle()

    // SONRAKI PARCA KAMERASI (F4.13) - ListScreen'dekiyle ayni desen ve ayni
    // dersler: hedef yol durumdan degil KAYNAK ADINDAN turetiliyor (Activity
    // yeniden yaratilinca `remember` sifirlaniyor ve fis sessizce kayboluyordu),
    // ham dosya yol uzerinden siliniyor (`content://` uzerinden silme cihazda
    // hicbir sey yapmadi).
    val receiptsDir = remember { FileKit.filesDir / "receipts" }
    val partCamera = rememberCameraPickerLauncher { file ->
        if (file != null) {
            val dest = receiptsDir / ("fis-" + file.name.removePrefix("ham-"))
            val raw = receiptsDir / file.name
            vm.attachNextPart(
                source = file,
                destPath = dest.absolutePath(),
                rawPath = raw.absolutePath(),
            )
        }
    }

    val nextPartId by vm.nextPartId.collectAsStateWithLifecycle()
    LaunchedEffect(nextPartId) {
        nextPartId?.let { id ->
            vm.consumeNextPart()
            onOpenPart(id)
        }
    }

    ReceiptCheckScreen(
        state = state,
        editing = editing,
        suggestions = suggestions,
        onEdit = vm::edit,
        onDismissEdit = vm::dismissEdit,
        onConfirm = vm::confirm,
        onFixAmount = vm::fixAmount,
        onOutcome = vm::setOutcome,
        onReread = vm::rereadNextRotation,
        onNextPart = {
            receiptsDir.createDirectories()
            // Ad zaman damgasi: parcalar birbirini EZMESIN.
            val stamp = Clock.System.now().toEpochMilliseconds()
            val temp = receiptsDir / "ham-$stamp.jpg"
            partCamera.launch(FileKitCameraType.Photo, FileKitCameraFacing.Back, temp)
        },
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
    CheckRow("1", "r1", "Krema", 10600, 2.0, "KREMA 18YAĞLI 200ML %1. *106.00", false),
    CheckRow("2", "r1", null, 8450, 1.0, "TURŞU KORNI ŞON 670G 21. *84.50", true),
    CheckRow("3", "r1", "Poşet", 100, 1.0, "ALIŞVERIŞ POŞETi BiM 220 *1.00", false),
)

/** Tek parcali fis: baslik yok, tek bolum. */
private val sampleSections = listOf(
    CheckSection(receiptId = "r1", title = null, meta = null, rows = sampleRows),
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
            sections = sampleSections,
        ),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onOutcome = { _, _ -> }, onReread = {}, onNextPart = {}, onBack = {},
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
            sections = sampleSections,
        ),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onOutcome = { _, _ -> }, onReread = {}, onNextPart = {}, onBack = {},
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
        onFixAmount = { _, _ -> }, onOutcome = { _, _ -> }, onReread = {}, onNextPart = {}, onBack = {},
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
        onFixAmount = { _, _ -> }, onOutcome = { _, _ -> }, onReread = {}, onNextPart = {}, onBack = {},
    )
}

/**
 * COK PARCALI FIS TEK AKISTA (tasarim karari 4).
 *
 * Onizlemesi olmadan bu hal cihazda ancak iki kare cekilerek gorulebilir -
 * yani pratikte hic gorulmezdi. Bolum basliklari, "devamini cek" satiri ve
 * fisin TAMAMINA ait manset burada bir arada.
 */
@PreviewLightDark
@Composable
private fun ReceiptCheckMultiPartPreview() = NeydiPreview {
    ReceiptCheckScreen(
        state = CheckState(
            loading = false,
            subtitleStore = "MİGROS",
            subtitleMeta = "12 Ağustos 15:31 · 2 parça",
            totalMinor = 64250,
            sumMinor = 64250,
            gateHolds = true,
            isPart = true,
            sections = listOf(
                CheckSection(
                    receiptId = "r1",
                    title = "Parça 1",
                    meta = "2 satır",
                    rows = sampleRows.take(2),
                ),
                CheckSection(
                    receiptId = "r2",
                    title = "Parça 2",
                    meta = "1 satır · 1 satır kontrol bekliyor",
                    rows = listOf(
                        CheckRow("9", "r2", null, 16400, 1.0, "KSR PYNR 400G *164.00", true),
                    ),
                ),
            ),
        ),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onOutcome = { _, _ -> }, onReread = {}, onNextPart = {}, onBack = {},
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
            sections = sampleSections,
        ),
        editing = null,
        suggestions = emptyList(),
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onOutcome = { _, _ -> }, onReread = {}, onNextPart = {}, onBack = {},
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
        state = CheckState(loading = false, storeName = "FiLE MARKET", sections = sampleSections),
        editing = sampleRows[1],
        suggestions = List(8) { i ->
            CatalogSeed(
                id = "seed-$i", name = "Turşu ${i + 1}", matchKey = "tursu$i",
                categoryId = "konserve-salca", commonalityRank = i + 1, defaultUnit = "adet",
            )
        },
        onEdit = {}, onDismissEdit = {}, onConfirm = { _, _ -> },
        onFixAmount = { _, _ -> }, onOutcome = { _, _ -> }, onReread = {}, onNextPart = {}, onBack = {},
    )
}
