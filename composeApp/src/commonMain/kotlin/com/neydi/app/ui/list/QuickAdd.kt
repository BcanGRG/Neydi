package com.neydi.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.suggest.Suggestion
import com.neydi.app.data.suggest.reasonText
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.SuggestionChip
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Alta sabit hizli ekleme alani.
 *
 * ALTA SABIT, ustte degil: bas parmak orada ve klavye acilinca alan klavyenin
 * hemen ustunde kalir. Ustte olsaydi her eklemede goz ekranin iki ucu arasinda
 * gidip gelirdi.
 *
 * ONERILER SKORA GORE sirali (alfabetik DEGIL) - siralamayi ViewModel yapiyor,
 * burasi yalnizca ciziyor.
 *
 * ## ARTIK BIR BUTON, metin alani DEGIL (karar 63)
 *
 * Burada bir `BasicTextField` duruyordu ve tasarimin iki dosyasi birbiriyle
 * celisiyordu: matris bir HEDEF, tasarim sistemi YAZILABILIR bir alan
 * ciziyordu. Karar 63 butonu secti - kokte metin alani yok; odakli hal ve
 * otomatik tamamlama Ekle sheet'inin arama alanina ait.
 *
 * Gerekcesi tekrar: kokteki alan, Ekle sheet'iyle AYNI isi yapan ikinci bir
 * yoldu. Ustelik *"hicbir ekran acilirken klavye acmaz"* kurali en temiz
 * boyle korunuyor - klavyeyi acabilecek dorduncu bir alan kalmiyor.
 *
 * Motor onerileri seridi KALIYOR: o bir girdi degil, listenin kendi teklifi.
 */
@Composable
fun QuickAdd(
    engineSuggestions: List<Suggestion>,
    onEngineSuggestion: (Suggestion) -> Unit,
    onOpenAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalNeydiExtraColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        // TEK SERIT, IKI MOD (F6.3): girdi BOSKEN motorun onerileri, kullanici
        // yazarken otomatik tamamlama. Iki ayri serit ust uste binerdi; modu
        // girdinin bos olup olmamasi seciyor. Serit ALANIN USTUNDE: dokunulacak
        // sey parmagin geldigi yerde olmali, klavyenin arkasinda degil.
        //
        // Motor cipinin gerekcesi DUZ TURKCE ("14 gun oldu") - gerekcesiz cip
        // reklam gibi okunur. Otomatik tamamlama cipi ise birim gosteriyor;
        // o bir oneri degil, yazilani tamamlama.
        // TEK MOD KALDI. Once iki mod vardi - bos girdide motorun onerileri,
        // yazarken otomatik tamamlama - ve modu girdinin doluluğu seciyordu.
        // Kokte girdi kalmayinca ikinci mod da kalmadi: otomatik tamamlama
        // Ekle sheet'inin arama alaninda (karar 63).
        if (engineSuggestions.isNotEmpty()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = Spacing.md,
                    vertical = Spacing.sm,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(engineSuggestions, key = { it.productId }) { s ->
                    SuggestionChip(
                        label = s.name,
                        reason = s.reasonText(),
                        onClick = { onEngineSuggestion(s) },
                    )
                }
            }
        }

        // BUTON: dokunusu Ekle sheet'ini aciyor. Gorunum alanin gorunumunu
        // KORUYOR - ayni yukseklik, ayni koseler, ayni `add` ikonu - cunku
        // degisen sey neye BENZEDIGI degil, ne YAPTIGI.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .padding(bottom = Spacing.sm)
                .heightIn(min = SizesExtra.quickAddField)
                .clip(NeydiExtraShapes.textField)
                .pressable(onTap = onOpenAdd)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, extras.hairline, NeydiExtraShapes.textField)
                .padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeydiIcon(
                icon = NeydiIcons.Add,
                contentDescription = null,
                size = 22.dp,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Ne lazım?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// --- Preview ---------------------------------------------------------------

/**
 * SERIDIN ONERI MODU: motor konusuyor - "Yumurta · 14 gun oldu".
 *
 * ONCE IKI ONIZLEME VARDI, ikincisi otomatik tamamlamayi tutuyordu. O mod
 * kokten kalkti (karar 63) ve onizlemesi de kalkti - artik Ekle sheet'inin
 * isi. Duran bir onizleme, olmayan bir hali cizmeye devam ederdi.
 */
@PreviewLightDark
@Composable
private fun QuickAddEngineSuggestionsPreview() = NeydiPreview {
    QuickAdd(
        engineSuggestions = listOf(
            Suggestion("p1", "Yumurta", 1.9, daysSince = 14, intervalDays = 10, forgottenLastTrip = false),
            Suggestion("p2", "Çay", 1.6, daysSince = 21, intervalDays = 14, forgottenLastTrip = false),
            Suggestion("p3", "Ekmek", 1.5, daysSince = 4, intervalDays = 3, forgottenLastTrip = true),
        ),
        onEngineSuggestion = {},
        onOpenAdd = {},
    )
}

/** Motorun soyleyecegi bir sey yokken: yalnizca buton. */
@PreviewLightDark
@Composable
private fun QuickAddEmptyPreview() = NeydiPreview {
    QuickAdd(engineSuggestions = emptyList(), onEngineSuggestion = {}, onOpenAdd = {})
}
