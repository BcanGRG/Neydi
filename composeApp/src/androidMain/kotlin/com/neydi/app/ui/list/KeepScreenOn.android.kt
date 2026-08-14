package com.neydi.app.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
actual fun KeepScreenOn(aktif: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    // DisposableEffect: ekrandan cikildiginda bayrak GERI ALINIR. Birakilsaydi
    // kullanici listeden ciktiktan sonra da telefon kararmazdi.
    DisposableEffect(aktif) {
        view.keepScreenOn = aktif
        onDispose { view.keepScreenOn = false }
    }
}
