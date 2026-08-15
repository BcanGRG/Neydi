package com.neydi.app.ui.capture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.neydi.app.ui.theme.Spacing

/**
 * iOS'ta canli onizleme HENUZ YOK (F9.2).
 *
 * DURUST BOSLUK: `controller.denied` isaretlenmiyor cunku izin reddedilmedi -
 * yol hic yazilmadi. Ikisini karistirmak kullaniciyi ayarlara gonderip orada
 * kapatacak bir sey bulamamasina yol acardi.
 *
 * `ready` false kaldigi icin cekim dugmesi zaten pasif.
 */
@Composable
actual fun CameraSurface(controller: CaptureController, modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Kamera bu platformda henüz yok.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(Spacing.lg),
        )
    }
}
