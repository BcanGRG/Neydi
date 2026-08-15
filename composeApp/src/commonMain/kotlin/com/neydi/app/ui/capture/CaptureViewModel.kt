package com.neydi.app.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.receipt.attachReceiptToTrip
import com.neydi.app.data.repo.ListRepository
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CaptureState(
    /** Bu oturumda cekilip geziye baglanmis kare sayisi. */
    val partCount: Int = 0,
    /** Son kare diske yazilamadi - ekran bunu SOYLEMEK zorunda. */
    val lastFailed: Boolean = false,
    /** Ilk kare; "Bitti" buraya goturuyor. */
    val firstReceiptId: String? = null,
)

/**
 * Fis cekme oturumu (Ekran 4).
 *
 * OTURUM KAVRAMI BURADA YENI: eskiden her parca ayri bir sistem kamerasi
 * yolculuguydu ve uygulamanin "kac kare cekildi" diye bir bilgisi yoktu -
 * kullanicinin da yoktu. Simdi sayac ekranda duruyor.
 *
 * HER KARE ANINDA GEZIYE BAGLANIYOR, "Bitti"de toplu degil. Kullanici uc kare
 * cekip uygulamayi kapatirsa uc kare de duruyor; toplu yazma o durumda hepsini
 * kaybederdi ve kaybi kimse fark etmezdi.
 */
class CaptureViewModel(
    private val tripId: String,
    private val repo: ListRepository,
    private val receiptsDirPath: String,
    private val now: () -> Long,
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureState())
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    /**
     * Bir kare ceker, kucultur ve geziye baglar.
     *
     * KUCULTME YOLU DEGISMIYOR: `attachReceiptToTrip` `content://` ve
     * kucultme derslerini tek yerde tutuyor (F4.13). Kamera yalnizca HAM
     * dosyayi uretiyor, gerisi ayni.
     */
    fun capture(controller: CaptureController, onAttached: (String) -> Unit) {
        viewModelScope.launch {
            val stamp = now()
            val rawPath = "$receiptsDirPath/ham-$stamp.jpg"
            val destPath = "$receiptsDirPath/fis-$stamp.jpg"

            if (!controller.capture(rawPath)) {
                _state.value = _state.value.copy(lastFailed = true)
                return@launch
            }
            val receipt = attachReceiptToTrip(
                repo = repo,
                householdId = DEFAULT_HOUSEHOLD_ID,
                tripId = tripId,
                source = PlatformFile(rawPath),
                destPath = destPath,
                rawPath = rawPath,
            )
            _state.value = _state.value.copy(
                partCount = _state.value.partCount + 1,
                lastFailed = false,
                firstReceiptId = _state.value.firstReceiptId ?: receipt.id,
            )
            onAttached(receipt.id)
        }
    }
}
