package com.neydi.app.ui.capture

import androidx.compose.runtime.Composable

/**
 * iOS'ta belge tarayici HENUZ YOK (F9.2).
 *
 * Karsiligi var ve yazilacak: `VNDocumentCameraViewController` ayni isi yapiyor
 * - kenar algilama, otomatik cekim, kirpma. Bugun yalnizca `onUnavailable`
 * cagriliyor, yani ekran kendi kamerasina dusuyor; o da iOS'ta henuz onizleme
 * cizmiyor ve durumu durustce soyluyor.
 */
@Composable
actual fun rememberReceiptScanner(
    onResult: (ScanResult) -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit = { onUnavailable() }
