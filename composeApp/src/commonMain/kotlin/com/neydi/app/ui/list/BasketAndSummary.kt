package com.neydi.app.ui.list

import androidx.compose.foundation.layout.Box
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.pressable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.theme.Sizes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.neydi.app.data.basketIsShown
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neydi.app.data.formatEstimate
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import com.neydi.app.ui.theme.neydiDisplayFamily

/**
 * "Tahmini sepet" satiri.
 *
 * DURUST OLMAK ZORUNDA: fiyati bilinmeyen urunler toplama girmiyor, yani
 * tahmin her zaman EKSIK yonde yanlis olabilir. Bu yuzden "en az" diyor ve
 * kacinin fiyatini bildigimizi yaziyor. Kesin tutar gibi sunmak, kullanicinin
 * kasada surpriz yasamasi demek - ve bu uygulamanin varlik sebeplerinden biri
 * tam olarak o surprizi engellemek.
 *
 * UCTEN AZ FIYAT BILINIYORSA SATIR HIC CIZILMEZ (gezinme sozlesmesi esigi).
 * "0,00 TL" yazmak yalan olurdu; iki fiyattan tahmin uretmek de oyle.
 */
@Composable
internal fun EstimatedBasket(
    amountMinor: Long,
    pricedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    /** Butun katkilar tek zincirdense o zincir; aksi halde `null` (karar 95). */
    chain: String? = null,
) {
    // ESIK UC SATIR VE MUTLAK (karar 53, karar 112 ile teyit edildi).
    //
    // Tek ya da iki fiyat bilinen bir sepette tahmin, tahminden cok yanilgi
    // uretiyor: "~40 TL" yazan bir satir, on sekiz urunluk bir sepetin
    // yaninda yanlis bir guven veriyor. Altinda satir HIC gorunmuyor - ve
    // yerine "yeterli veri yok" gibi bir yuzey de yazilmiyor.
    //
    // SAYDIGI SEY ARTIK TOPLAMA GIREN SATIR (karar 112): ambalaji bilinmeyen
    // tartili satir fiyati OLMASINA ragmen toplama girmiyor, yani esigi de
    // dolduramaz. Eskiden "fiyati var mi" diye sayiliyordu.
    if (!basketIsShown(pricedCount)) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            .clip(NeydiExtraShapes.card)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // SOL TUTAR, SAG KAYNAK - TEK SATIR (karar 95).
        //
        // Once iki katliydi: solda "Tahmini sepet" + *"8 üründen 5 tanesini
        // biliyorum"*, sagda *"en az ~1.200 TL"*. Karar 95 ikisini de emekli
        // etti ve sebebi ayni cumleyi iki kez soylemeleri: sayac (`4/7`) zaten
        // kapsamı yaziyor, `~` zaten "kesin degil" diyor. `en az` oneki
        // ucuncu kez ayni seyi soyluyordu.
        Text(
            text = "Tahmini sepet ${formatEstimate(amountMinor)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        // KAYNAK CUMLESI: hepsi tek zincirdense zincir adi, aksi halde "son
        // fiyatlarla" (karar 95, 113-114). Sayac PAY/PAYDA: pay toplama giren
        // satir, payda listedeki her satir - ambalaji bilinmeyen tartili satir
        // aradaki farkta duruyor (karar 96).
        Text(
            text = "${chain?.let { "$it fiyatlarıyla" } ?: "son fiyatlarla"} · $pricedCount/$totalCount",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}

/**
 * Alisveris sonrasi ozet - LISTENIN ICINDE bir kart (karar 69).
 *
 * ## Sheet degil kart, ve fark iliskiyi ters cevirmekti
 *
 * Ozet bir `ModalBottomSheet` icinde aciliyordu: karartma, listeyi kapatan bir
 * yuzey, kapatmak icin "Tamam" butonu. Denetimin tek YAPISAL bulgusu buydu ve
 * karar 69 maketi onayladi: *"alisveris bitince gorulen sey listenin son hali
 * olmali; ozet ona ilistirilmis bir yorum, ekrani ele geciren bir yuzey
 * degil"*. Sheet tam tersini yapiyordu - yorum ekrani kapatiyor, asil sey
 * (liste) arkada kaliyordu.
 *
 * ## Kapatinca KALICI gidiyor
 *
 * Ve bu mesru, cunku kart bir BILDIRIM degil - yeniden uretilebilir bir ozet.
 * Kaynagi Gecmis'te duruyor: ayni bilgi ("N urun + tutar") gezi satirinda
 * yasiyor.
 *
 * ## Tutar yoksa kart HIC acilmiyor
 *
 * Karar 45, degismedi. Cagiran taraf (`ListViewModel.finishShopping`) tutar
 * hesaplanamiyorsa `_summary`yi hic doldurmuyor.
 */
@Composable
internal fun SummaryCard(
    takenCount: Int,
    totalCount: Int,
    amountMinor: Long,
    previous: PreviousTrip?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md)
            .clip(NeydiExtraShapes.card)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, LocalNeydiExtraColors.current.hairline, NeydiExtraShapes.card)
            .padding(Spacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(
                text = "Bu alışveriş",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatEstimate(amountMinor),
                // 36sp Fraunces: ekranda bu boyutta baska hicbir sey yok.
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = neydiDisplayFamily(),
                    fontSize = 36.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(Sizes.hairline)
                    .background(LocalNeydiExtraColors.current.hairline),
            )
            Text(
                // ALINAN SAYISI VE ONCEKI GEZI AYNI SATIRDA DEGIL: maket alt
                // satiri yalnizca onceki geziye ayirmis. Kac urun alindigi
                // artik Gecmis gezi satirinda yasiyor (karar 69).
                text = previous?.let { "Geçen sefer ${formatEstimate(it.amountMinor)} (${it.ago})" }
                    ?: "$takenCount ürün alındı",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // KAPATMA IKONU SAG USTTE, alttaki "Tamam" butonunun yerine.
        //
        // Buton kartin sonunu bir AKSIYONA baglıyordu - okunup kapatilan bir
        // dialog gibi. Kart artik listenin icinde duruyor ve kapatmak onu
        // gormezden gelmek kadar hafif olmali.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(Sizes.minTapTarget)
                .clip(NeydiExtraShapes.pill)
                .pressable(onTap = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            NeydiIcon(
                icon = NeydiIcons.Close,
                contentDescription = "özeti kapat",
                size = 20.dp,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Ozet kartinin alt satiri: bir onceki gezinin tutari ve ne kadar once oldugu. */
data class PreviousTrip(val amountMinor: Long, val ago: String)

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun EstimatePartialPreview() = NeydiPreview {
    EstimatedBasket(amountMinor = 48750, pricedCount = 5, totalCount = 8)
}

@PreviewLightDark
@Composable
private fun EstimateCompletePreview() = NeydiPreview {
    EstimatedBasket(amountMinor = 128990, pricedCount = 8, totalCount = 8)
}

@PreviewLightDark
@Composable
private fun SummaryWithTotalPreview() = NeydiPreview {
    SummaryCard(
        takenCount = 8, totalCount = 8, amountMinor = 64_250,
        previous = PreviousTrip(60_100, "18 gün önce"), onDismiss = {},
    )
}

/**
 * ILK GEZI: karsilastirilacak onceki gezi yok, alt satir alinan sayiya doner.
 *
 * ONCEKI ONIZLEME "tutar yok ama kart yine de bir sey soyluyor" halini
 * tutuyordu ve o hal ARTIK YOK: karar 45 tutar hesaplanamiyorsa karti hic
 * acmiyor, karar 69 de bunu teyit etti. Olmayan bir hali cizen onizleme,
 * incelemeye yanlis bir sey gosterir.
 */
@PreviewLightDark
@Composable
private fun SummaryFirstTripPreview() = NeydiPreview {
    SummaryCard(
        takenCount = 6, totalCount = 8, amountMinor = 64_250,
        previous = null, onDismiss = {},
    )
}

/** Sepet tahmininin cizilmesi icin gereken en az fiyatli urun (tasarim esigi). */
internal const val MIN_PRICED_ITEMS = 3
