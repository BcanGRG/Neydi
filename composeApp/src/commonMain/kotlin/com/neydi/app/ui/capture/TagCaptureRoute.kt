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
import com.neydi.app.ui.theme.RequestDarkSystemBars
import org.koin.compose.viewmodel.koinViewModel

/**
 * Etiket cekiminin baglantilari: kamera tutamagi, dosya yolu, ViewModel.
 *
 * ## Dosya yolunu ROUTE uretiyor
 *
 * ViewModel dosya sistemini bilmiyor, yalnizca bir yol aliyor - `commonTest`
 * cekimden gozleme kadar butun yolu cihazsiz kosturabilsin diye.
 *
 * ## Kaydettikten sonra EKRANDAN CIKILMIYOR
 *
 * Bildirim gezinme katmanina verilmiyor, bu ekranda gosteriliyor - seri cekim
 * icin ekranin acik kalmasi sart (bkz. [TagCaptureViewModel.save]).
 */
@Composable
internal fun TagCaptureRoute(onBack: () -> Unit) {
    val vm: TagCaptureViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val controller = rememberCaptureController()
    val scope = rememberCoroutineScope()

    // CUBUKLAR KOYU: bu ekran her iki temada da koyu (tasarim). Acik temada
    // temanin siyah ikonlari siyah onizlemenin uzerinde okunmuyordu.
    RequestDarkSystemBars()

    // GERI SIRASI: once KART, sonra destinasyon (gezinme sozlesmesi).
    //
    // Kart aciksa geri tusu ekrandan cikmamali - cekilen kare cope giderdi.
    // Kaydetme SURERKEN geri de yutuluyor: sartname *"kaydet sirasinda geri =
    // kayit tamamlanir"* diyor ve yazma zaten `viewModelScope`ta, yani ekrani
    // kapatmamak tek yapilmasi gereken.
    CaptureBackHandler(enabled = state.card != null || state.saving) {
        if (!state.saving) vm.dismissCard()
    }

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
                if (controller.capture(path)) vm.onCaptured(path) else vm.captureFailed()
            }
        },
        onSelectStore = vm::selectStore,
        onPriceChange = vm::editPrice,
        onSave = vm::save,
        onToastShown = vm::toastShown,
        onFailureShown = vm::failureShown,
        onOpenPicker = vm::openPicker,
        onClosePicker = vm::closePicker,
        onPickProduct = vm::pickProduct,
        onSearchProducts = vm::searchProducts,
        onPickBrand = vm::pickBrand,
        onSearchStores = vm::searchStores,
        onProposeStore = vm::proposeStore,
        onConfirmNewStore = vm::confirmNewStore,
        onDeleteStore = vm::deleteStore,
        onDismissCard = vm::dismissCard,
        onBack = onBack,
        modifier = Modifier.fillMaxSize(),
        cameraContent = { CameraSurface(controller, Modifier.fillMaxSize()) },
    )
}

/** Dosya adi icin artan damga - saat degil, cakismasin diye. */
private fun nowStamp(): String = com.neydi.app.di.now().toString()
