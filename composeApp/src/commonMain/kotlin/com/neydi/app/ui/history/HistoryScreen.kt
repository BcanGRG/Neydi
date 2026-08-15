package com.neydi.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.pressable
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
    // SURFACE ZORUNLU, sadece Column DEGIL.
    //
    // Ilk hali ciplak bir Column'du ve karanlik modda ekranin zemini
    // themes.xml'deki SABIT `windowBackground = #FBF7F2`'den geliyordu:
    // acik krem zemin uzerinde karanlik paletin beyaza yakin metni. NeydiTheme
    // bir Surface saglamiyor, NavDisplay de zemin cizmiyor - ekranin kendisi
    // cizmek zorunda (Placeholders ve ListContent oyle yapiyor).
    //
    // ONIZLEME BUNU MASKELEDI: NeydiPreview icerigi kendi Surface'ina
    // sariyor, yani preview'de zemin dogru gorunuyordu. Hatanin yalnizca
    // gercek ekranda var olmasinin sebebi bu.
    //
    // safeDrawingPadding da SART: cihazda "Geri" ve baslik durum cubugunun
    // altina girdi. Placeholders'taki iskelet ekranlarda vardi, yeni
    // ekranlarda unutulmustu.
    val extras = LocalNeydiExtraColors.current
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        // BASLIK BLOGU: geri oku + baslik + mini grafik, altinda hairline.
        // Tasarimda ucu tek bir blok ve blogun alt kenari ayirici.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.md, end = Spacing.md, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
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
                    text = "Geçmiş",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            SpendBars(trips)
        }
        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))

        if (trips.isEmpty()) {
            EmptyHistory(Modifier.weight(1f))
            return@Column
        }

        LazyColumn(modifier = Modifier.padding(horizontal = Spacing.md)) {
            items(trips.size, key = { trips[it].id }) { i ->
                TripRow(trips[i]) { onOpenReceipt(it) }
            }
        }
    }
    }
}

/**
 * Son alti alisverisin toplami - EKSENSIZ, ACIKLAMASIZ, ETKILESIMSIZ.
 *
 * Tasarimin kendi tanimi bu. Bir grafik degil, bir RITIM: harcamanin son
 * birkac alisverisde nereye gittigini tek bakista veriyor; sayilari zaten
 * altindaki liste yaziyor.
 *
 * IKI ALISVERISTEN AZSA HIC CIZILMIYOR - tasarimin kurali: *"tek cubuklu
 * grafik grafik degildir"*. Toplami okunamamis geziler de disarida: null bir
 * tutari sifir cubuk olarak cizmek "bedava alisveris" demek olurdu.
 */
@Composable
private fun SpendBars(trips: List<HistoryTrip>) {
    val totals = trips.take(6).mapNotNull { it.totalMinor }.reversed()
    if (totals.size < 2) return

    val max = totals.max().coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(96.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        totals.forEach { total ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    // En kucuk cubuk bile GORUNUR kalsin: oransal yukseklik
                    // sifira yaklastiginda cubuk kaybolur ve grafik eksik
                    // veri varmis gibi okunur.
                    .fillMaxHeight(fraction = (total.toFloat() / max).coerceAtLeast(0.06f))
                    .clip(NeydiExtraShapes.barTop)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

/** Tasarimin bos hali: tek satir aciklama, CTA yok. */
@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterVertically),
    ) {
        Text(
            text = "Henüz alışveriş yok",
            // Fraunces: bos durum basliklari onun gorevli oldugu dort yerden biri.
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "İlk listeni tamamladığında burada görünecek.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Bir gezinin Gecmis satiri (tasarim: 72dp, altinda hairline).
 *
 * TASARIM MAGAZAYI BASLIK YAPIYOR, tarihi degil: kullanicinin bir alisverisi
 * hatirlama bicimi "gecen carsamba" degil "File Market'teki". Tarih ve urun
 * sayisi ikinci satirda birlikte duruyor.
 *
 * COK PARCALI FISTE PARCALAR ALTTA LISTELENIYOR - tasarim bu durumu
 * kapsamiyor cunku parca parca cekim (F4.13) tasarimdan SONRA geldi. Tek
 * satira indirmek her parcayi erisilemez yapardi ve bu ekranin varlik sebebi
 * tam olarak "yanlis okunmus fise donmenin tek yolu" olmak.
 */
@Composable
private fun TripRow(trip: HistoryTrip, onOpenReceipt: (String) -> Unit) {
    val extras = LocalNeydiExtraColors.current
    val single = trip.receipts.singleOrNull()
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    // Tek fisli gezide satirin kendisi fise goturuyor; cok
                    // parcalida asagidaki parca satirlari goturuyor.
                    if (single != null) Modifier.pressable(onTap = { onOpenReceipt(single.id) })
                    else Modifier,
                )
                .heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    // UC AYRI HAL, IKI DEGIL: fis yok / fis var ama magaza
                    // okunamadi / magaza okundu. Ikisini "Fis eklenmedi"de
                    // birlestirmek, okunamamis bir fisi HIC CEKILMEMIS gibi
                    // gosterirdi - kullanici fotografi cektigini biliyor ve
                    // uygulamaya guvenini tam orada kaybederdi.
                    text = trip.storeName
                        ?: if (trip.receipts.isEmpty()) "Fiş eklenmedi" else "Mağaza okunamadı",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatDayMonthYear(trip.closedAt)} · ${trip.itemCount} ürün",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                // null ise "-": fis okunmadigi icin BILMIYORUZ. Sifir yazmak
                // "bedava alisveris" demek olurdu.
                text = trip.totalMinor?.let { formatMinor(it) } ?: "—",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                // Sabit genislik: tnum uygulanmasa da sutun kaymasin.
                modifier = Modifier.width(SizesExtra.priceColumn),
            )
            Spacer(Modifier.width(Spacing.sm))
            ReceiptStatusIcon(trip.receipts)
        }
        // Parcalar: yalnizca birden fazla fis varsa.
        if (trip.receipts.size > 1) {
            trip.receipts.forEach { receipt ->
                PartRow(receipt) { onOpenReceipt(receipt.id) }
            }
        }
        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
    }
}

/** Cok parcali fisin tek parcasi - girintili, sonuk, ama dokunulabilir. */
@Composable
private fun PartRow(receipt: HistoryReceipt, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(onTap = onClick)
            .heightIn(min = Sizes.minTapTarget)
            .padding(start = Spacing.md, bottom = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (receipt.isPart) "parça" else statusLabel(receipt.status) ?: "fiş",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = receipt.totalMinor?.let { formatMinor(it) } ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Spacing.sm))
        SingleStatusIcon(receipt)
    }
}

/**
 * Gezinin fis durumu, TEK IKONLA (tasarim: dort hal).
 *
 * IYI DURUM SESSIZ OLMALI ilkesi burada da gecerli - ama tasarim "fis var"i
 * yine de gosteriyor, cunku yoklugu ("fis yok") anlamli bir bilgi ve ikisi
 * ancak yan yana okunabiliyor. Ayrim renkte: dolgulu yesil vs sonuk kontur.
 *
 * Cok parcali gezide EN KOTU hal kazaniyor: bir parca okunamadiysa gezinin
 * durumu odur, digerleri okunmus olsa bile.
 */
@Composable
private fun ReceiptStatusIcon(receipts: List<HistoryReceipt>) {
    if (receipts.isEmpty()) {
        NeydiIcon(
            icon = NeydiIcons.ReceiptLong,
            contentDescription = "Fiş yok",
            size = 22.dp,
            tint = MaterialTheme.colorScheme.outline,
        )
        return
    }
    val worst = receipts.minByOrNull { severity(it.status) } ?: receipts.first()
    SingleStatusIcon(worst)
}

@Composable
private fun SingleStatusIcon(receipt: HistoryReceipt) {
    val extras = LocalNeydiExtraColors.current
    val (icon, tint, label) = when (receipt.status) {
        ReceiptStatus.VERIFIED ->
            Triple(NeydiIcons.ReceiptLong, extras.success, "Fiş okundu")
        ReceiptStatus.PENDING, ReceiptStatus.READING ->
            Triple(NeydiIcons.HourglassTop, MaterialTheme.colorScheme.onSurfaceVariant, "İşleniyor")
        // Parca amber giymiyor: toplam son parcada basili, kullanici hata
        // yapmadi (bkz. F4.13b).
        ReceiptStatus.MISMATCHED -> if (receipt.isPart) {
            Triple(NeydiIcons.ReceiptLong, MaterialTheme.colorScheme.outline, "Parça fişi")
        } else {
            Triple(NeydiIcons.Error, extras.warning, "Kontrol bekliyor")
        }
        ReceiptStatus.FAILED ->
            Triple(NeydiIcons.Error, extras.warning, "Okunamadı")
    }
    NeydiIcon(icon = icon, contentDescription = label, size = 22.dp, tint = tint)
}

/** Kucuk = daha kotu. Cok parcali gezide gosterilecek hali secer. */
private fun severity(status: ReceiptStatus): Int = when (status) {
    ReceiptStatus.FAILED -> 0
    ReceiptStatus.MISMATCHED -> 1
    ReceiptStatus.PENDING, ReceiptStatus.READING -> 2
    ReceiptStatus.VERIFIED -> 3
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

/**
 * Gorulmeyen durumlar: bekliyor / okunuyor / toplam tutmuyor.
 *
 * Ilk onizlemeler yalnizca VERIFIED ve FAILED tasiyordu, yani amber tonlu
 * MISMATCHED satiri ve iki gecici durum hic cizilmemisti.
 */
@PreviewLightDark
@Composable
private fun HistoryAllStatusesPreview() = NeydiPreview {
    HistoryScreen(
        trips = listOf(
            HistoryTrip(
                id = "t9",
                closedAt = 1_755_100_000_000,
                totalMinor = 48458,
                receipts = listOf(
                    HistoryReceipt("a", ReceiptStatus.PENDING, null, null, 1_755_100_000_000),
                    HistoryReceipt("b", ReceiptStatus.READING, null, "FiLE MARKET", 1_755_099_000_000),
                    HistoryReceipt("c", ReceiptStatus.MISMATCHED, 48458, "FiLE OVACIK / KEÇiÖREN", 1_755_098_000_000),
                    HistoryReceipt("d", ReceiptStatus.VERIFIED, 22550, "BIM", 1_755_097_000_000),
                ),
            ),
        ),
        onBack = {},
        onOpenReceipt = {},
    )
}
