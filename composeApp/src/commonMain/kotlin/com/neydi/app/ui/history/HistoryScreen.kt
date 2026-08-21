package com.neydi.app.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.neydi.app.data.formatEstimate
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 * Gecmis (Ekran 6).
 *
 * UYGULAMANIN EN UCUZ EKRANI: kapanmis gezileri tarih ve kalem sayisiyla
 * listeliyor, baska bir sey yapmiyor. Fis doneminde bu ekran "yanlis okunmus
 * bir fise donmenin tek yolu"ydu ve satirlari dokunulabilirdi; o hedef yok.
 *
 * GEZI SATIRININ ALTINDA HICBIR SEY YOK (tasarim karari 30): gozlem geziye
 * bagli degil, markete girip hicbir sey almadan uc etiket cekmek mesru bir
 * kullanim. Gozlemler Urun Detayi'nda yasiyor - kullanicinin sorusu
 * ("hangi tarihte kaca almisim") urun ekseninde soruluyor.
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
    // cizmek zorunda (ListContent oyle yapiyor).
    //
    // ONIZLEME BUNU MASKELEDI: NeydiPreview icerigi kendi Surface'ina
    // sariyor, yani preview'de zemin dogru gorunuyordu. Hatanin yalnizca
    // gercek ekranda var olmasinin sebebi bu.
    //
    // safeDrawingPadding da SART: cihazda "Geri" ve baslik durum cubugunun
    // altina girdi. iskelet ekranlarda vardi (o dosya silindi), yeni
    // ekranlarda unutulmustu.
    val extras = LocalNeydiExtraColors.current
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        // BASLIK BLOGU: geri oku + baslik + mini grafik, altinda hairline.
        // Tasarimda blok tek parca ve blogun alt kenari ayirici.
        //
        // Aradaki 14dp tasarimin baslik-grafik boslugu; grafik esigin altinda
        // hic cizilmedigi icin (bkz. [TripTotalChart]) o durumda blok yine tek
        // cocuklu kaliyor ve ara etkisiz oluyor.
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
            TripTotalChart(trips)
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

/**
 * Baslik altindaki mini grafik: son alti gezinin TUTARI (karar 68).
 *
 * CUBUK PARAYI OLCUYOR, kalem sayisini degil - uygulamanin konusu para, ve
 * kalem sayisi gezi satirinda zaten yazili; ayni sayiyi ikinci kez cizmek
 * grafigi bezemeye cevirirdi.
 *
 * TABAN CIZGISI SIFIR: cubugun boyu buyuklugu kodluyor, bu yuzden olcek en
 * kucuk tutardan degil sifirdan basliyor. Sparkline'in min-max olcegi orada
 * dogru (o bir egilim cizgisi), burada olsaydi en ucuz gezi "sifir lira"
 * gorunurdu.
 *
 * TalkBack icin sessiz (Canvas'in semantigi yok): her cubugun tarihi ve
 * tutari hemen asagidaki gezi satirlarinda okunuyor - grafik onlarin gorsel
 * ozeti, yeni bir bilgi degil.
 */
@Composable
private fun TripTotalChart(trips: List<HistoryTrip>, modifier: Modifier = Modifier) {
    val bars = remember(trips) { tripTotalBars(trips) }
    // Esigin altinda bilesen HIC emit etmiyor; Sparkline ile ayni korunma.
    if (bars.isEmpty()) return

    val fill = MaterialTheme.colorScheme.primary
    Canvas(modifier.fillMaxWidth().height(ChartHeight)) {
        val gap = ChartBarGap.toPx()
        val radius = ChartBarCorner.toPx()
        // Cubuklar kalan genisligi esit paylasiyor (tasarimda `flex:1`), yani
        // alti geziden az varsa cubuk kalinlasiyor - grafik dar kalmiyor.
        val barWidth = (size.width - gap * (bars.size - 1)) / bars.size
        val outline = UnknownBarOutline.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))

        bars.forEachIndexed { i, fraction ->
            val left = i * (barWidth + gap)
            val right = left + barWidth
            if (fraction == null) {
                // TUTARI HESAPLANAMAYAN GEZI YERINI KORUYOR: dolgusuz, kesik
                // konturlu, SABIT kisa cubuk. Sifir boy cubuk "bedava
                // alisveris" iddiasi olurdu; cubugu hic cizmemek ise yanindaki
                // gezileri kaydirip aralari bozardi. Kesik kontur karar 50'nin
                // jesti - eksik olan sey bicimle soyleniyor, sayiyla degil.
                //
                // Kontur cubugun kutusunun ICINE cekiliyor (yarim kalinlik
                // pay): ilk cubugun sol kenari x=0'da, ortalanmis firca yarisi
                // tuvalin disinda kalir ve o tek cubuk ince gorunurdu.
                val half = outline / 2f
                drawPath(
                    path = barPath(
                        left = left + half,
                        top = size.height - UnknownBarHeight.toPx() + half,
                        right = right - half,
                        bottom = size.height,
                        radius = radius,
                    ),
                    color = fill,
                    style = Stroke(width = outline, pathEffect = dash),
                )
            } else {
                drawPath(
                    path = barPath(
                        left = left,
                        top = size.height * (1f - fraction),
                        right = right,
                        bottom = size.height,
                        radius = radius,
                    ).apply { close() },
                    color = fill,
                )
            }
        }
    }
}

/**
 * Grafigin cubuklari: en yeni [CHART_BAR_COUNT] gezi, ESKIDEN YENIYE.
 *
 * Donen listede `null` = tutari hesaplanamayan gezi (kesik konturlu cubuk);
 * digerleri penceredeki en buyuk tutara gore 0..1 arasi oran. Esigin altinda
 * BOS liste donuyor, yani grafik hic cizilmiyor.
 *
 * AYRI VE SAF FONKSIYON: esik ile "yeri korunan gezi" kuralinin ikisi de
 * gozle dogrulanamayacak seyler - bir cubugun eksilmesi ya da esigin bir
 * kayamasi ekranda "biraz farkli bir grafik" gibi gorunur, halbuki
 * kullaniciya yanlis bir olcek gosterir. Test bu yuzden burayi tutuyor.
 */
internal fun tripTotalBars(trips: List<HistoryTrip>): List<Float?> {
    // Liste en yeniden eskiye geliyor (observeHistory: `completedAt DESC`);
    // grafik zaman eksenini soldan saga okuttugu icin pencere ters ceviriliyor.
    val window = trips.take(CHART_BAR_COUNT).reversed()
    val totals = window.mapNotNull { it.estimateMinor }
    // ESIK: UC TUTARLI GEZI (esik tablosu · "Gecmis mini grafigi"). Altinda
    // grafik hic cizilmiyor - "tek cubuklu grafik grafik degildir". Esik
    // PENCEREYE bakiyor, tum gecmise degil: alti gezinin besi tutarsizsa
    // ekranda kalan tek dolu cubuk yine grafik olmazdi.
    if (totals.size < MIN_TRIPS_WITH_TOTAL) return emptyList()
    // Tum tutarlar sifirsa hepsi sifir boy cubuk olurdu; o da "bedava
    // alisveris" iddiasi. Boyle bir olcek yerine grafik hic cizilmiyor.
    val max = totals.max().takeIf { it > 0L } ?: return emptyList()
    return window.map { trip -> trip.estimateMinor?.let { it.toFloat() / max.toFloat() } }
}

/**
 * Ust koseleri [radius] yuvarlak, TABANI ACIK cubuk yolu.
 *
 * drawRoundRect kullanilamiyor cunku o dort koseyi birden yuvarliyor; cubuk
 * taban cizgisine oturdugu icin alt koseler kare olmak zorunda, yoksa cubuk
 * zeminden kopmus gorunur. Yol acik biraktigi icin ayni sekil hem `close()`
 * ile doldurulabiliyor hem de tutari bilinmeyen gezide tabani cizilmeden
 * konturlanabiliyor.
 */
private fun barPath(left: Float, top: Float, right: Float, bottom: Float, radius: Float): Path {
    // Cok kisa ya da cok dar cubukta yaricap kutuya sigmaz; sigdirilmazsa
    // yaylar birbirinin uzerine biner ve sekil bozulur.
    val r = radius.coerceAtMost((right - left) / 2f).coerceAtMost(bottom - top)
    return Path().apply {
        moveTo(left, bottom)
        lineTo(left, top + r)
        arcTo(Rect(left, top, left + 2 * r, top + 2 * r), 180f, 90f, false)
        lineTo(right - r, top)
        arcTo(Rect(right - 2 * r, top, right, top + 2 * r), 270f, 90f, false)
        lineTo(right, bottom)
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
 * TUTAR ARTIK VAR (E18) ve gozlemlerden hesaplaniyor. HER ZAMAN TILDE ILE:
 * uygulamada kesin tutar diye bir veri yok. Tahmin esigin altindaysa satirda
 * HIC yazilmiyor - "0 TL" yazmak bedava alisveris demek olurdu.
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
            trip.estimateMinor?.let { minor ->
                Text(
                    text = formatEstimate(minor),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
    }
}

// --- Grafik olculeri (Ekranlar 5-8 · "6 Gecmis" cercevesi) -------------------
// Tema dosyasina girmediler cunku hicbiri paylasilan bir olcu degil: hepsi
// yalnizca bu grafigi tarif ediyor. Sizes'a konsalardi orasi tek kullanimlik
// sayilarla dolar ve "paylasilan olcu" anlamini kaybederdi.

/** Grafik blogunun yuksekligi. */
private val ChartHeight = 96.dp

/** Cubuklar arasi bosluk. */
private val ChartBarGap = 10.dp

/** Cubugun UST koselerinin yaricapi; alt koseler kare - cubuk tabana oturuyor. */
private val ChartBarCorner = 8.dp

/** Tutari bilinmeyen gezinin SABIT boyu (96dp'lik blokta 24dp). */
private val UnknownBarHeight = 24.dp

/** Kesik konturun kalinligi (tasarimda `1.5px dashed`). */
private val UnknownBarOutline = 1.5.dp

/** Grafigin gosterdigi gezi sayisi - tasarimda alti cubuk. */
private const val CHART_BAR_COUNT = 6

/** Grafigin cizilmesi icin gereken en az TUTARLI gezi sayisi (esik tablosu). */
private const val MIN_TRIPS_WITH_TOTAL = 3

// --- Onizlemeler ------------------------------------------------------------

/** Iki gezi: esigin altinda, yani grafiksiz hal. */
@PreviewLightDark
@Composable
private fun HistoryPreview() = NeydiPreview {
    HistoryScreen(
        trips = listOf(
            HistoryTrip(id = "t1", closedAt = 1_755_100_000_000, itemCount = 18, estimateMinor = 64_250),
            HistoryTrip(id = "t2", closedAt = 1_754_900_000_000, itemCount = 3),
        ),
        onBack = {},
    )
}

/** Grafikli hal: alti cubuk ve aralarinda tutari bilinmeyen bir gezi. */
@PreviewLightDark
@Composable
private fun HistoryWithChartPreview() = NeydiPreview {
    HistoryScreen(
        trips = listOf(
            HistoryTrip(id = "t1", closedAt = 1_755_100_000_000, itemCount = 18, estimateMinor = 88_400),
            HistoryTrip(id = "t2", closedAt = 1_754_900_000_000, itemCount = 12, estimateMinor = 62_150),
            HistoryTrip(id = "t3", closedAt = 1_754_700_000_000, itemCount = 21, estimateMinor = 78_900),
            HistoryTrip(id = "t4", closedAt = 1_754_500_000_000, itemCount = 2),
            HistoryTrip(id = "t5", closedAt = 1_754_300_000_000, itemCount = 9, estimateMinor = 68_300),
            HistoryTrip(id = "t6", closedAt = 1_754_100_000_000, itemCount = 7, estimateMinor = 52_000),
        ),
        onBack = {},
    )
}

/**
 * Grafik tek basina: dolgulu cubuklarin arasinda tutari bilinmeyen gezinin
 * kesik konturlu kisa cubugu - ekran onizlemesinde 96dp'lik blok kuculuyor.
 */
@PreviewLightDark
@Composable
private fun TripTotalChartPreview() = NeydiPreview {
    TripTotalChart(
        trips = listOf(
            HistoryTrip(id = "c1", closedAt = 1_755_100_000_000, estimateMinor = 88_400),
            HistoryTrip(id = "c2", closedAt = 1_754_900_000_000, estimateMinor = 62_150),
            HistoryTrip(id = "c3", closedAt = 1_754_700_000_000, estimateMinor = 78_900),
            HistoryTrip(id = "c4", closedAt = 1_754_500_000_000),
            HistoryTrip(id = "c5", closedAt = 1_754_300_000_000, estimateMinor = 68_300),
            HistoryTrip(id = "c6", closedAt = 1_754_100_000_000, estimateMinor = 52_000),
        ),
        modifier = Modifier.padding(Spacing.md),
    )
}

@PreviewLightDark
@Composable
private fun HistoryEmptyPreview() = NeydiPreview {
    HistoryScreen(trips = emptyList(), onBack = {})
}
