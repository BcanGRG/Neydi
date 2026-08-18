package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
) {
    // ESIK UC FIYATLI URUN (gezinme sozlesmesi · thresholds). Tek ya da iki
    // fiyat bilinen bir sepette tahmin, tahminden cok yanilgi uretiyor:
    // "~40 TL" yazan bir satir, on sekiz urunluk bir sepetin yaninda yanlis
    // bir guven veriyor. Altinda satir HIC gorunmuyor.
    if (pricedCount < MIN_PRICED_ITEMS) return

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
        Column {
            Text(
                text = "Tahmini sepet",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                // Kacinin fiyatini bildigimiz ACIKCA yaziyor: eksik bilgiyi
                // gizlemek tahmini guvenilir gosterir, ki degil.
                text = if (pricedCount < totalCount) {
                    "$totalCount üründen $pricedCount tanesini biliyorum"
                } else {
                    "hepsinin fiyatını biliyorum"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (pricedCount < totalCount) {
                "en az ${formatEstimate(amountMinor)}"
            } else {
                formatEstimate(amountMinor)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Alisveris sonrasi TEK SEFERLIK ozet karti.
 *
 * Duygusal karsilik ve ekran goruntusu ani burasi: kullanici alisverisi
 * bitirdi, uygulama ona ne yaptigini gosteriyor. Tutar 36sp Fraunces -
 * ekranin geri kalaninda o boyutta baska hicbir sey yok, bakis oraya gidiyor.
 *
 * Tutar bilinmiyorsa sayilar gosteriliyor: "8 urun, 24 dakika". Bu bile bos
 * bir karttan iyi. E18'e kadar tutar HER ZAMAN bilinmiyor - gozlemlerden
 * hesaplanan `~` tahmini henuz yazilmadi.
 */
@Composable
internal fun SummaryCard(
    bottomPadding: Dp = 0.dp,
    takenCount: Int,
    totalCount: Int,
    amountMinor: Long?,
    durationMinutes: Int?,
    onDismiss: () -> Unit,
    /**
     * Fissiz mutabakati duzeltme yolu (F4.8).
     *
     * GORUNUR OLMASI SART: kapanista planlananlar alindi yazildi ve kullanici
     * bunu bilmiyor. Duzeltme yolu olmadan iyimser varsayim bir tuzak olurdu -
     * alinmamis urun satin alma gecmisine girer, kullanici gormez, oneri
     * motoru o urunu duzenli aliniyor sanar.
     */
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md)
            .clip(NeydiExtraShapes.card)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.lg)
            .padding(top = Spacing.xl, bottom = Spacing.xl + bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = if (takenCount == totalCount) "Liste tamam." else "Alışveriş bitti.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (amountMinor != null) {
            Text(
                text = formatEstimate(amountMinor),
                // 36sp Fraunces: ekranda bu boyutta baska hicbir sey yok.
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = neydiDisplayFamily(),
                    fontSize = 36.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = buildString {
                append("$takenCount ürün alındı")
                if (takenCount < totalCount) append(", ${totalCount - takenCount} tanesi kaldı")
                if (durationMinutes != null) append(" · $durationMinutes dakika")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // "Fis cek" butonu E6'da kalkti ve yerine bir sey GELMEYECEK.
        // Tasarim karari 27 girisi tek bir yere koydu: liste basligindaki
        // kalici kamera hedefi. Gerekcesi ozet kartini dogrudan eliyor -
        // cekim geziden bagimsiz (karar 3), ozet karti ise yalnizca alisveris
        // sonrasi gorunuyor; buraya bir dugme koymak cekimi yeniden geziye
        // baglardi. Ikinci bir giris noktasi da yasak: "ayni ise iki kapi
        // acip ikisini de zayiflatirdi".

        // "HEPSINI ALMADIM" HEDEFI KALKTI (F11.20).
        //
        // Bos Durumlar cerceve 04'un basligi birebir *"Alisveris kapanisi ·
        // acilmaz"*: kontrol edilecek bir belge yok, liste kapanista
        // kendiliginden temizleniyor. Karar 31 pivot turunda bunu yeniden ele
        // alip teyit etti.
        //
        // ISARETLENMEMIS SATIRLARIN CEVABI VAR, sadece baska zamanda: bir
        // SONRAKI gezinin basinda "Eksik olabilir" ekraninda *"gecen sefer
        // unuttun"* diye geri geliyorlar. Duzeltmeyi kapanis aninda istemek,
        // kullaniciyi kasadan cikarken bir forma oturtmak olurdu.

        // "Tamam" en altta ve SILIK: kartin asil isi bir sey ANLATMAK, bir
        // sey yaptirmak degil. Ustte ve vurgulu bir "Tamam", karti okunmadan
        // kapatilan bir dialog'a cevirirdi.
        //
        // Eskiden burada "Fis cek" butonu vardi ve "Tamam"in silikligi onu
        // one cikarmak icindi. O buton E6'da kalkti (pivot karari 3: cekim
        // geziden bagimsiz, girisi liste basliginda - karar 27).
        NeydiButton(
            text = "Tamam",
            onClick = onDismiss,
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

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
    SummaryCard(takenCount = 8, totalCount = 8, amountMinor = 128990, durationMinutes = 24, onDismiss = {})
}

/** Fis okunmadi: tutar yok ama kart yine de bir sey soyluyor. */
@PreviewLightDark
@Composable
private fun SummaryWithoutTotalPreview() = NeydiPreview {
    SummaryCard(takenCount = 6, totalCount = 8, amountMinor = null, durationMinutes = 31, onDismiss = {})
}

/** Sepet tahmininin cizilmesi icin gereken en az fiyatli urun (tasarim esigi). */
internal const val MIN_PRICED_ITEMS = 3
