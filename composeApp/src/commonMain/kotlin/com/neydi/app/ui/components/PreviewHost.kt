package com.neydi.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.neydi.app.ui.theme.NeydiTheme
import com.neydi.app.ui.theme.Spacing

/**
 * Preview'lerin ortak kabugu. Temasiz preview yaniltir: bu uygulamada renk,
 * tipografi ve basili hal TAMAMEN NeydiTheme'den geliyor - ripple bile global
 * olarak degistirilmis durumda. Tema disinda cizilen bir bilesen Material'in
 * varsayilanlariyla gorunur ve cihazdaki haliyle ilgisi olmaz.
 *
 * KULLANIM: `@PreviewLightDark` ile birlikte. O anotasyon `uiMode`'u surer,
 * NeydiTheme'in `darkTheme` varsayilani `isSystemInDarkTheme()` oldugu icin
 * karanlik varyant kendiliginden dogru cikar.
 *
 * Neden `@PreviewWrapper` degil: CMP 1.11.1 onu yeni tanitti (ui-tooling-preview
 * icinde) ve IDE destegi heryerde ayni degil. Destegi oturunca buradaki her
 * `NeydiPreview { }` cagrisi tek anotasyona indirgenebilir.
 */
@Composable
internal fun NeydiPreview(
    padding: Dp = Spacing.sm,
    content: @Composable () -> Unit,
) {
    NeydiTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(padding)) { content() }
        }
    }
}
