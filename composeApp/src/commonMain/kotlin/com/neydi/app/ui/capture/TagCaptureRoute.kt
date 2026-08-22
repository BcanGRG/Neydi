package com.neydi.app.ui.capture

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.data.image.deleteFileAt
import com.neydi.app.data.image.deleteFilesIn
import com.neydi.app.data.image.jpegIsComplete
import com.neydi.app.data.ocr.dumpImportedPhotos
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
    // GERI SIRASI: once SECICI, sonra KART, sonra ekran.
    //
    // Secici sirada YOKTU ve sonucu sessiz bir hataydi: secici acikken geriye
    // basmak ARKADAKI KARTI kapatiyordu. Secici ekranda kaliyor, ama artik
    // yazacagi bir kart yok - urun secmek `updateCard` uzerinden hicbir sey
    // yapmiyor ve kullanici sectigini saniyor. Cihazda goruldu.
    //
    // Kaydetme SURERKEN geri yutuluyor: sartname *"kaydet sirasinda geri =
    // kayit tamamlanir"* diyor ve yazma zaten `viewModelScope`ta, yani ekrani
    // kapatmamak tek yapilmasi gereken.
    CaptureBackHandler(enabled = state.picker != null || state.card != null || state.saving) {
        when {
            state.saving -> Unit
            state.picker != null -> vm.closePicker()
            else -> vm.dismissCard()
        }
    }

    val dir = remember { FileKit.filesDir / "tags" }
    LaunchedEffect(Unit) {
        dir.createDirectories()
        // YETIM KARELERI SUPUR. Ekran aciliyor ve elimizde kart YOK, yani
        // klasorde ne varsa onceki bir oturumdan kalmis demektir - kaydedilmis
        // ya da vazgecilmis olsaydi silinmis olurdu. Tek kalan yol uygulamanin
        // kart acikken oldurulmesi ve orada silecek kimse yok.
        if (state.card == null) deleteFilesIn(dir.absolutePath())
        // OLCUM ICE AKTARIMI: isaret dosyasi varsa `<tags>-dump/in/` icindeki
        // hazir kareleri okuyup dokumlerini yaziyor. Isaret yoksa hicbir sey
        // yapmiyor - bir kullanicinin cihazinda o dosya hic olusmuyor.
        dumpImportedPhotos(dir.absolutePath() + "-dump")
    }

    TagCaptureScreen(
        state = state,
        cameraReady = controller.ready,
        cameraDenied = controller.denied,
        cameraPermanentlyDenied = controller.permanentlyDenied,
        flashOn = controller.torch,
        onToggleFlash = { controller.torch = !controller.torch },
        onOpenSettings = controller::openSettings,
        onShutter = { guide ->
            scope.launch {
                // Ad CEKIM ANINDAN: iki kare ayni saniyede cekilse bile
                // ViewModel'in `photoPath` karsilastirmasi onlari ayirabilmeli.
                val path = (dir / "tag-${nowStamp()}.jpg").absolutePath()
                // IKI KAPI: denetleyici "yazdim" demeli VE dosya gercekten
                // eksiksiz olmali. Ikincisi olmadan yarim yazilmis bir kare
                // karta girip OCR'a gidiyor - ve eksik olan yer etiketin
                // fiyati olabilir.
                val yazildi = controller.capture(path) && jpegIsComplete(path)
                if (yazildi) {
                    vm.onCaptured(path, guide)
                } else {
                    deleteFileAt(path)
                    vm.captureFailed()
                }
            }
        },
        onSelectStore = vm::selectStore,
        onPriceChange = vm::editPrice,
        onSelectDigit = vm::selectPriceDigit,
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
