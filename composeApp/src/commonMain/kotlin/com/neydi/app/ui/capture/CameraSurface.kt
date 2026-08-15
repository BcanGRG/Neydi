package com.neydi.app.ui.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Canli onizlemeyi ekranin geri kalanina baglayan tutamak.
 *
 * NEDEN BIR SINIF, DOGRUDAN CAGRI DEGIL: cekim islevi platformdan geliyor ama
 * onu TETIKLEYEN dugme ortak katmanda. Ikisi arasinda bir tutamak olmadan ya
 * butun ekran platforma inecekti (tasarim iki kez yazilirdi) ya da cekim
 * ortak katmana cikacakti (mumkun degil).
 */
@Stable
class CaptureController {

    /** Onizleme acildi ve cekim yapilabilir. */
    var ready: Boolean by mutableStateOf(false)
        internal set

    /** Kamera izni reddedildi - ekran bunu SOYLEMEK zorunda, sessiz siyah kalmamali. */
    var denied: Boolean by mutableStateOf(false)
        internal set

    internal var capturer: (suspend (String) -> Boolean)? = null

    /**
     * Tam cozunurlukte bir kare yakalar ve [destPath]'e yazar.
     *
     * @return yazildi mi. false = kamera hazir degil ya da yazma basarisiz;
     *   cagiran taraf bunu kullaniciya soylemek zorunda.
     */
    suspend fun capture(destPath: String): Boolean = capturer?.invoke(destPath) ?: false
}

@Composable
fun rememberCaptureController(): CaptureController = remember { CaptureController() }

/**
 * Canli kamera onizlemesi.
 *
 * Android'de CameraX; iOS'ta HENUZ YOK ve bu durust bir bosluk - `denied`
 * yerine kendi mesajini veriyor, cunku "izin reddedildi" demek yanlis olurdu
 * (bkz. F9.2).
 */
@Composable
expect fun CameraSurface(controller: CaptureController, modifier: Modifier = Modifier)
