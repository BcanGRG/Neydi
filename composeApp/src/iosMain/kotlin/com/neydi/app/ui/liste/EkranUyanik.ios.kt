package com.neydi.app.ui.liste

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

/**
 * DIKKAT: Windows'ta DERLENMEZ, dolayisiyla DOGRULANMAMIS.
 * iOS karsiligi idleTimerDisabled.
 */
@Composable
actual fun EkraniUyanikTut(aktif: Boolean) {
    DisposableEffect(aktif) {
        UIApplication.sharedApplication.idleTimerDisabled = aktif
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}
