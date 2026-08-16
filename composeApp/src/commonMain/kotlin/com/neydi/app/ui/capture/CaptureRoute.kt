package com.neydi.app.ui.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Fis cekme akisi (F4.18).
 *
 * ONCE BELGE TARAYICI, olmazsa KENDI KAMERAMIZ. Tarayici kullanicinin istedigi
 * uc seyi birden veriyor: fisin kenarlarini bulup arka plani atiyor, kirpilmis
 * sonucu GOSTERIP koseleri duzelttiriyor, ve termal fisteki golge/lekeyi
 * temizliyor. Asil sikayet "cektigimi anlamiyorum"du; kirpilmis sonucun
 * ekranda gorunmesi tam olarak onun cevabi.
 *
 * KENDI KAMERAMIZ SILINMEDI, YEDEK: Play Services'i olmayan ya da tarayiciyi
 * desteklemeyen cihazda akis tikanmamali. Bu tahmini bir ihtimal degil -
 * `getStartScanIntent` basarisiz olabilen bir cagri ve sessiz kalmasi
 * kullaniciyi bos ekranda birakirdi.
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

    // Tarayici acilamadiysa kendi kameramiza dusuyoruz - bir kez.
    var useOwnCamera by remember { mutableStateOf(false) }

    val scan = rememberReceiptScanner(
        onResult = { result ->
            if (result.pagePaths.isEmpty()) {
                // Kullanici vazgecti; hicbir sey cekilmediyse geri donuyoruz.
                if (state.partCount == 0) onCancel() else onDone(state.firstReceiptId)
            } else {
                vm.attachScanned(result.pagePaths) { first -> onDone(first) }
            }
        },
        onUnavailable = { useOwnCamera = true },
    )

    // TARAYICI EKRAN ACILIR ACILMAZ BASLIYOR: bu hedefin baska bir icerigi yok,
    // arada bir "tara" dugmesi gostermek fazladan bir dokunus olurdu.
    LaunchedEffect(useOwnCamera) {
        if (!useOwnCamera) scan()
    }

    if (useOwnCamera) {
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
}
