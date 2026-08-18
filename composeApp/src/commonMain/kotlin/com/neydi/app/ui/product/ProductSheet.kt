package com.neydi.app.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.NeydiSwitch
import com.neydi.app.ui.components.Sparkline
import com.neydi.app.ui.components.SectionHeader
import com.neydi.app.ui.components.turkishInitials
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/** Sheet'in gosterdigi urun. */
data class ProductSheetState(
    val productId: String,
    /**
     * Sheet'in acildigi satir - "Listeden cikar" bunu siliyor (karar 38).
     *
     * Nullable, cunku sheet bir gun satirdan bagimsiz da acilabilir (urun
     * gecmisinden). O halde satir cizilmiyor: silinecek bir satir yok.
     */
    val rowId: String? = null,
    val name: String,
    val isStaple: Boolean,
    /** Fiyat bolumu (E17). Bos ise bolum HIC cizilmiyor. */
    val price: PriceSection = PriceSection(),
)

/**
 * Urun Detayi sheet'i - **su an yalnizca sifir-gozlem hali** (Ekran 5).
 *
 * NEDEN SIMDI VE NEDEN BU KADAR: F6.8'in ("her zamankiler"e ekleme) tasarimda
 * belirlenmis giris noktasi bu sheet'teki anahtar. Tasarim maketlerinde
 * *"Her zamankilere ekle"* ve *"Bunu onerme"* anahtarlari sheet'in **uc veri
 * halinin hepsinde** var - sifir gozlemli halde bile. Yani sheet'in bu hali
 * fiyat verisine HIC ihtiyac duymuyor ve Faz 5'i beklemesi gerekmiyor.
 *
 * Anahtari gecici olarak Ayarlar'a koymak alternatifti; tasarimin kendi
 * affordance'ini kullanmak yerine yeni bir yer icat etmek olurdu.
 *
 * F5.3 buraya manset cumlesini, Canvas grafigi, min/ortalama referans
 * cizgilerini, aralik secicisini ve alim gecmisi tablosunu ekleyecek.
 * F6.5 ikinci anahtari (*"Bunu onerme"*) baglayacak - bugun engelleme tablosu
 * var ama DAO'su yok, ve gorunup calismayan bir anahtar calismayan bir anahtardan
 * kotudur.
 */
@Composable
fun ProductSheetContent(
    state: ProductSheetState,
    onStapleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    /**
     * Satiri listeden cikarir - silme jestinin JESTSIZ ESI (tasarim karari 38).
     *
     * NEDEN TASMA MENUSUNDE DEGIL: tasarim sistemi bir sure *"her yikici islem
     * icin tasma menusunde jest olmayan bir yol"* diyordu ve o vaat mekanik
     * olarak tutulamiyordu - **menu ekran duzeyinde yasiyor, silme satir
     * duzeyinde bir is**; menu hangi satirda oldugumuzu bilmiyor. Karar 38
     * bunu Urun Detayi'na tasidi: sheet zaten BIR SATIRDAN aciliyor, yani
     * baglami tasiyor.
     *
     * BU SATIR ERISILEBILIRLIGIN KENDISI: TalkBack ve switch access swipe
     * uretemiyor. Olmasaydi silme, o kullanicilar icin var olmayan bir ozellik
     * olurdu.
     *
     * `null` ise cizilmiyor. Bugun tek cagiran liste ekrani; karar 38 Gecmis'ten
     * acilinca ayni yuvada kiremit *"Listeye ekle"* istiyor ama Gecmis satiri
     * dokunulabilir DEGIL (karar 30), yani o dal bugun ulasilamaz - varmis gibi
     * parametre acmak olu kod olurdu.
     */
    onRemoveFromList: (() -> Unit)? = null,
) {
    val extras = LocalNeydiExtraColors.current
    Column(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(bottom = bottomPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Iki-harf fallback: ikon sistemi yok ve urunlerin %80'i onu
            // gosterecek. `turkishInitials` cunku locale'siz uppercase()
            // "incir" -> "IN" verir, dogrusu "İN".
            Box(
                Modifier
                    .size(44.dp)
                    .clip(NeydiExtraShapes.categoryTile)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = turkishInitials(state.name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = state.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = Spacing.sm),
            )
        }

        if (state.price.isEmpty) {
            // SIFIR GOZLEM: grafik yok, manset yok, yuzde yok. Tasarimin kurali
            // "yanlis bir sey gostermektense hicbir sey gostermemek" ve tek
            // noktadan trend cizmek yalan olurdu.
            Text(
                text = "Etiket çektikçe burada fiyat geçmişi birikecek.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
        } else {
            PriceBlock(state.price)
        }

        Spacer(Modifier.height(Spacing.md))

        onRemoveFromList?.let { remove ->
            // 56dp, ustunde ayirici, error renginde, IKON YOK, sagda kontrol yok.
            // Yikici satirin tek isareti RENK - tasarimin renk sozlesmesi
            // kirmiziyi zaten "yalnizca geri alinamaz is" diye ayirmis durumda.
            Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
            Text(
                text = "Listeden çıkar",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .pressable(onTap = remove)
                    .padding(horizontal = Spacing.md)
                    .heightIn(min = 56.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
            )
        }

        NeydiSwitch(
            label = "Her zamankilere ekle",
            checked = state.isStaple,
            onCheckedChange = onStapleChange,
        )
    }
}


/**
 * Fiyat bolumu: "Nerede ucuz" + alim gecmisi (E17).
 *
 * ## Bos bolum BASLIGIYLA BIRLIKTE yok
 *
 * "Nerede ucuz" tek market varken cevabi olmayan bir soru, ve tasarimin genel
 * degismezi *"bos bir bolum basligi, olmayan bir isi varmis gibi gosterir"*.
 * Esik verinin kendisinde ([PriceSection]), cizimde degil - ekran yalnizca
 * gelen listeyi ciziyor.
 */
@Composable
private fun PriceBlock(price: PriceSection) {
    val extras = LocalNeydiExtraColors.current

    if (price.cheapest.isNotEmpty()) {
        SectionHeader(title = "Nerede ucuz", count = price.cheapest.size)
        price.cheapest.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = row.store,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    // MARKA VE AMBALAJ ALT SATIRDA: karar 26 kimligi
                    // market+marka cifti yapiyor, yani marka satirin bir
                    // suslemesi degil AYIRT EDICI bilgisi. Ikisi de eksik
                    // olabiliyor (manavda marka yok) - o zaman satir cizilmiyor.
                    listOfNotNull(row.brand, row.pack).takeIf { it.isNotEmpty() }?.let { parts ->
                        Text(
                            text = parts.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = row.price,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Spacer(Modifier.height(Spacing.sm))
    }

    SectionHeader(title = "Alım geçmişi", count = price.history.size)
    price.history.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = row.store,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = row.price,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    if (price.sparkline.isNotEmpty()) {
        Box(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
            Sparkline(values = price.sparkline, color = extras.priceUp)
        }
    }
}

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun ProductSheetStaplePreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState(productId = "p1", name = "Beyaz Peynir 600 g", isStaple = true),
        onStapleChange = {},
    )
}

@PreviewLightDark
@Composable
private fun ProductSheetPlainPreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState(productId = "p2", name = "Kuru Kayısı", isStaple = false),
        onStapleChange = {},
    )
}

@PreviewLightDark
@Composable
private fun ProductSheetPricePreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState(
            productId = "p1",
            name = "Ayçiçek Yağı",
            isStaple = true,
            price = PriceSection(
                cheapest = listOf(
                    CheapRow(store = "BİM", brand = "Dost", price = "100,00", pack = "4 lt"),
                    CheapRow(store = "Migros", brand = "Pınar", price = "130,00", pack = null),
                ),
                history = listOf(
                    HistoryRow(observedAt = 0, store = "BİM", price = "100,00"),
                    HistoryRow(observedAt = 0, store = "Migros", price = "130,00"),
                    HistoryRow(observedAt = 0, store = "BİM", price = "95,00"),
                ),
                sparkline = listOf(95f, 130f, 100f),
            ),
        ),
        onStapleChange = {},
    )
}
