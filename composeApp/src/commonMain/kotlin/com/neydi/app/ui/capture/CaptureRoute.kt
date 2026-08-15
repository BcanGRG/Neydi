package com.neydi.app.ui.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Fis cekme oturumunu ekrana baglar.
 *
 * DIZIN BURADA HAZIRLANIYOR: kamera dosyayi dogrudan yaziyor ve olmayan bir
 * dizine yazamaz. Eski akista bunu FileKit'in kendi gecici dizini hallediyordu.
 */
@Composable
fun CaptureRoute(
    tripId: String,
    onDone: (String?) -> Unit,
    onCancel: () -> Unit,
) {
    val receiptsDir = remember { FileKit.filesDir / "receipts" }
    LaunchedEffect(Unit) { receiptsDir.createDirectories() }

    val vm: CaptureViewModel = koinViewModel {
        parametersOf(tripId, receiptsDir.absolutePath())
    }
    val state by vm.state.collectAsStateWithLifecycle()

    CaptureScreen(
        partCount = state.partCount,
        lastFailed = state.lastFailed,
        onCapture = { controller ->
            vm.capture(controller) {}
            true
        },
        onDone = { onDone(state.firstReceiptId) },
        onCancel = onCancel,
    )
}
