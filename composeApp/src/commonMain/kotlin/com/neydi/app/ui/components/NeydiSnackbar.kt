package com.neydi.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import kotlinx.coroutines.delay

/** Seridin ekranda kalma suresi (tasarim karari 37: "5 sn'lik snackbar"). */
const val SNACKBAR_MS = 5_000L

/**
 * Aksiyon tasiyan gecici serit - uygulamanin **ikinci** ve son gecici yuzeyi.
 *
 * ## Neden [NeydiToast]'tan ayri bir bilesen
 *
 * Karar 8 ikisini bilerek ayirdi: *"Snackbar AKSIYON tasiyor... bu mesajda
 * dokunulacak bir sey yok."* Toast'in dokunma hedefi YOK ve olmamali; isaretleme
 * gibi saniyede bir olabilen islerde dokunulabilir bir serit ekrani felce
 * ugratirdi. Ikisini tek bilesene bindirmek o yasagi bulanistirirdi.
 *
 * ## Ikinci kullanim, bu turda acildi
 *
 * Karar 8 uzun sure *"uygulamada tek bir yerde kullaniliyor"* diyordu ve o
 * cumle satir silmeyi imkansiz kiliyordu. Altinci turda gerekce duzeltildi:
 * snackbar artik **iki yerde** yasiyor - alisveris kendiliginden kapandiginda
 * *"Gecmis'te gor"* ve satir silindiginde *"Geri al"*. Sozlesmenin degismezi
 * ucuncusunu yasakliyor: *"ucuncu bir aksiyonlu gecici yuzey yok."*
 *
 * ## Serit her iki temada da KOYU
 *
 * Renkler tema semasindan DEGIL, sabit okunuyor - bkz. [SNACKBAR_SURFACE].
 *
 * @param actionLabel tek kelime kurali burada bilerek delik: *"Geri al"* iki
 *   kelime ve karar 37 bunu **bilinen tek istisna** olarak yaziyor.
 */
@Composable
fun NeydiSnackbar(
    message: String?,
    actionLabel: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Sure MESAJA bagli, gorunurluge degil: art arda iki silmede ikinci mesaj
    // sayaci sifirdan baslatmali, yoksa ikinci serit birincisinden artan
    // sureyi kullanir ve bir anda kaybolur.
    LaunchedEffect(message) {
        if (message != null) {
            delay(SNACKBAR_MS)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier,
    ) {
        // `message` animasyon cikarken null olabiliyor; son gorunen metni
        // tutmak icin sabitleniyor, yoksa serit kapanirken bosaliyor.
        val text = message ?: return@AnimatedVisibility
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .widthIn(max = 360.dp)
                .clip(NeydiExtraShapes.snackbar)
                .background(SNACKBAR_SURFACE)
                .padding(horizontal = Spacing.md, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SNACKBAR_TEXT,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = SNACKBAR_ACTION,
                maxLines = 1,
                modifier = Modifier
                    .padding(start = Spacing.sm)
                    .clip(NeydiExtraShapes.pill)
                    .pressable(onTap = onAction)
                    .padding(horizontal = Spacing.xs, vertical = 4.dp),
            )
        }
    }
}

/**
 * Serit HER IKI TEMADA DA KOYU - bu bir eksiklik degil, tasarimin karari.
 *
 * Maketlerde acik temada da `#221A14` zemin duruyor: gecici yuzey arkasindaki
 * ekrana AIT DEGIL, onun ustunde duran ayri bir katman. Temayla birlikte
 * renk degistirseydi acik temada zeminden ayrisamazdi.
 */
private val SNACKBAR_SURFACE = Color(0xFF221A14)
private val SNACKBAR_TEXT = Color(0xFFF5EDE6)
private val SNACKBAR_ACTION = Color(0xFFFF9166)

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun NeydiSnackbarPreview() = NeydiPreview {
    NeydiSnackbar(
        message = "Maydanoz silindi",
        actionLabel = "Geri al",
        onAction = {},
        onDismiss = {},
    )
}

@PreviewLightDark
@Composable
private fun NeydiSnackbarLongPreview() = NeydiPreview {
    NeydiSnackbar(
        message = "Tarım Kredi Kooperatifi zeytinyağı silindi",
        actionLabel = "Geri al",
        onAction = {},
        onDismiss = {},
    )
}
