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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.NeydiSwitch
import com.neydi.app.ui.components.turkishInitials
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Spacing

/** Sheet'in gosterdigi urun. */
data class ProductSheetState(
    val productId: String,
    val name: String,
    val isStaple: Boolean,
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
) {
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

        // SIFIR GOZLEM: grafik yok, manset yok, yuzde yok. Tasarimin kurali
        // "yanlis bir sey gostermektense hicbir sey gostermemek" ve tek
        // noktadan trend cizmek yalan olurdu.
        Text(
            text = "Etiket çektikçe burada fiyat geçmişi birikecek.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )

        Spacer(Modifier.height(Spacing.md))

        NeydiSwitch(
            label = "Her zamankilere ekle",
            checked = state.isStaple,
            onCheckedChange = onStapleChange,
        )
    }
}

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun ProductSheetStaplePreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState("p1", "Beyaz Peynir 600 g", isStaple = true),
        onStapleChange = {},
    )
}

@PreviewLightDark
@Composable
private fun ProductSheetPlainPreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState("p2", "Kuru Kayısı", isStaple = false),
        onStapleChange = {},
    )
}
