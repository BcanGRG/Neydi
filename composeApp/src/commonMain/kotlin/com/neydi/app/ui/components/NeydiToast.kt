package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Spacing
import kotlinx.coroutines.delay

/** Tasarimin verdigi sure: 2 saniye. */
private const val TOAST_MS = 2_000L

/**
 * Toast - "oldu bitti, dokunacak bir sey yok" bildirimi (tasarim kararı 8).
 *
 * SNACKBAR'DAN AYRI BIR BILESEN ve ayrimi tasarim acikca ciziyor:
 * snackbar AKSIYON tasiyor ve uygulamada **iki yerde** yasiyor - alisveris
 * kendiliginden kapandiginda "Gecmis'te gor", satir silindiginde "Geri al"
 * ([NeydiSnackbar]). Karar 8'in gerekcesi bir sure "tek bir yerde" diyordu ve
 * o cumle satir silmeyi imkansiz kiliyordu; altinci turda duzeltildi. Burada
 * dokunulacak bir sey yok. Ikisini ayni bilesene bindirmek, isaretlemede snackbar
 * yasagini da bulanıklastirirdi - "bir gezide 20 isaretleme var".
 *
 * DOKUNMA HEDEFI YOK: kullanicinin yapacagi bir sey olmadigi icin tiklanabilir
 * olmasi yalniz yanlis beklenti uretirdi.
 *
 * KUYRUK YOK: iki mesaj cakisirsa ikincisi HIC cizilmiyor. Kuyruk, saniyeler
 * suren bir bildirim zinciri demek olurdu ve kullanici ekrandan cikmis olurdu.
 *
 * Konum cagiran tarafta: tasarim "floating toolbar'in 12dp ustu; toolbar
 * yoksa safe area + 12dp" diyor, yani toolbar'i bilen yer yerlestirmeli.
 *
 * @param message null ise hicbir sey cizilmiyor.
 * @param onShown sure dolunca cagriliyor; cagiran taraf mesaji temizliyor.
 */
@Composable
fun NeydiToast(
    message: String?,
    onShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (message == null) return

    LaunchedEffect(message) {
        delay(TOAST_MS)
        onShown()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .clip(NeydiExtraShapes.textField)
                // Zemin textPrimary, metin surface: tasarimin TERS CIFTI.
                // Golge YOK - derinlik ton farkiyla veriliyor.
                .background(MaterialTheme.colorScheme.onSurface)
                .padding(horizontal = Spacing.md, vertical = 14.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NeydiToastPreview() = NeydiPreview {
    NeydiToast(message = "Liste hazır, eksik görünmüyor", onShown = {})
}
