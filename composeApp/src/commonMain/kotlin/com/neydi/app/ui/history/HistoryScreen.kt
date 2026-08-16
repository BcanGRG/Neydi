package com.neydi.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.theme.Sizes
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.data.formatDayMonthYear
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
fun HistoryRoute(onBack: () -> Unit) {
    val vm: HistoryViewModel = koinViewModel()
    val trips by vm.trips.collectAsStateWithLifecycle()
    HistoryScreen(trips = trips, onBack = onBack)
}

@Composable
fun HistoryScreen(
    trips: List<HistoryTrip>,
    onBack: () -> Unit,

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
        }
        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))

        if (trips.isEmpty()) {
            EmptyHistory(Modifier.weight(1f))
            return@Column
        }

        LazyColumn(modifier = Modifier.padding(horizontal = Spacing.md)) {
            items(trips.size, key = { trips[it].id }) { i ->
                TripRow(trips[i])
            }
        }
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
 * BASLIK ARTIK TARIH. Tasarim magazayi baslik yapiyordu ve gerekcesi
 * dogruydu - kullanici bir alisverisi "File Market'teki" diye hatirliyor -
 * ama o adin TEK kaynagi fis kunyesiydi ve pivotla kalmadi. Gozlemlerden
 * gezi-magaza cikarimi yapmak (o gun cekilen etiketlerin marketi) mumkun
 * ama TAHMIN; tasarima soruldu (10-tasarima-pivot.md, "Ekran 6") ve cevap
 * gelene kadar uydurmuyoruz.
 *
 * TUTAR DA GECICI OLARAK YOK - bkz. HistoryTrip KDoc'u. E18 gozlemlerden
 * hesaplanan `~` tahminini getirecek.
 */
@Composable
private fun TripRow(trip: HistoryTrip) {
    val extras = LocalNeydiExtraColors.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = formatDayMonthYear(trip.closedAt),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${trip.itemCount} ürün",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
    }
}

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun HistoryPreview() = NeydiPreview {
    HistoryScreen(
        trips = listOf(
            HistoryTrip(id = "t1", closedAt = 1_755_100_000_000, itemCount = 18),
            HistoryTrip(id = "t2", closedAt = 1_754_900_000_000, itemCount = 3),
        ),
        onBack = {},
    )
}

@PreviewLightDark
@Composable
private fun HistoryEmptyPreview() = NeydiPreview {
    HistoryScreen(trips = emptyList(), onBack = {})
}
