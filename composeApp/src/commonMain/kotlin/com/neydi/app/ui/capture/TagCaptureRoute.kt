package com.neydi.app.ui.capture

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Etiket cekiminin baglantilari: kamera tutamagi, dosya yolu, ViewModel.
 *
 * ## Dosya yolunu ROUTE uretiyor
 *
 * ViewModel dosya sistemini bilmiyor, yalnizca bir yol aliyor - `commonTest`
 * cekimden gozleme kadar butun yolu cihazsiz kosturabilsin diye.
 *
 * @param onSaved toast metnini gezinme katmanina tasiyor: mesaji URETEN yer
 *   (bu ekran) ile GOSTEREN yer (Liste) farkli destinasyonlar.
 */
@Composable
internal fun TagCaptureRoute(onBack: () -> Unit, onSaved: (String) -> Unit) {
    val vm: TagCaptureViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val controller = rememberCaptureController()
    val scope = rememberCoroutineScope()

    val dir = remember { FileKit.filesDir / "tags" }
    LaunchedEffect(Unit) { dir.createDirectories() }

    TagCaptureScreen(
        state = state,
        cameraReady = controller.ready,
        cameraDenied = controller.denied,
        onShutter = {
            scope.launch {
                // Ad CEKIM ANINDAN: iki kare ayni saniyede cekilse bile
                // ViewModel'in `photoPath` karsilastirmasi onlari ayirabilmeli.
                val path = (dir / "tag-${nowStamp()}.jpg").absolutePath()
                if (controller.capture(path)) vm.onCaptured(path)
            }
        },
        onSelectStore = vm::selectStore,
        onPriceChange = vm::editPrice,
        onProductChange = vm::editProductName,
        onSave = { vm.save { message -> onSaved(message); onBack() } },
        onDismissCard = vm::dismissCard,
        onBack = onBack,
        modifier = Modifier.fillMaxSize(),
        cameraContent = { CameraSurface(controller, Modifier.fillMaxSize()) },
    )
}

/** Dosya adi icin artan damga - saat degil, cakismasin diye. */
private fun nowStamp(): String = com.neydi.app.di.now().toString()
