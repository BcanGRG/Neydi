package com.neydi.app.ui.capture

import androidx.compose.runtime.Composable

/**
 * Geri tusunu bu ekranda yakalar.
 *
 * ## Neden expect/actual
 *
 * Compose Multiplatform 1.11'de ortak bir `BackHandler` YOK
 * (`androidx.compose.ui.backhandler` bu surumde cozulmuyor - denendi).
 * Android tarafinda `androidx.activity.compose.BackHandler` var; iOS'ta geri
 * TUSU yok, kaydirma jesti sistemin kendi isi.
 *
 * ## Neden gerekli
 *
 * Gezinme sozlesmesinin geri sirasi *klavye -> sheet -> KART -> menu ->
 * destinasyon -> cikis* diyor. Kart aciksa geri tusu ekrandan cikarsa cekilen
 * kare cope gider - kullanicinin en cok is yaptigi anda en pahali kayip.
 *
 * @param enabled false ise sistem varsayilani calisir (destinasyondan cikis).
 */
@Composable
expect fun CaptureBackHandler(enabled: Boolean, onBack: () -> Unit)
