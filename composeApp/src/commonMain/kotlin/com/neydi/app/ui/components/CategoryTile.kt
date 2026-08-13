package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.neydi.app.ui.theme.LocalNeydiTextStyles
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.SizesExtra

/**
 * 56dp squircle kategori kutucugu, iki harfli fallback ile.
 *
 * Bu bilesen ONCE yazildi cunku ogelerin ~%80'i ikonlu degil bu fallback'i
 * gosterecek: kullanicinin elle ekledigi urunlerin cogu icin cizilmis bir ikon
 * olmayacak. Illustrasyonlu setin kapsamadigi her sey buraya duser, yani bu
 * "yedek" degil, ASIL gorunum.
 *
 * @param initials Zaten buyutulmus iki harf. [turkishInitials] ile uret -
 *   runtime'da uppercase() cagirma, Turkce i/I bozulur.
 * @param tint Kategoriye ozgu zemin tonu. Tasarim sistemi kategori tonlarini
 *   verene kadar varsayilan surfaceVariant.
 *   TODO(kategori-tonlari): 12 kategori icin ton paleti tasarimdan gelecek.
 */
@Composable
fun CategoryTile(
    initials: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier
            .size(SizesExtra.categoryTile)
            .clip(NeydiExtraShapes.categoryTile)
            .background(tint),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            // 20sp / 800 - tipografi olceginde quantityBadge ile ayni kesim.
            style = LocalNeydiTextStyles.current.quantityBadge,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}
