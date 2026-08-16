package com.neydi.app.ui.list

import androidx.compose.ui.platform.ClipEntry

/**
 * `ClipData`'nin ilk ogesinin metni.
 *
 * `itemCount` KONTROLU SART, susleme degil: `getItemAt(0)` bos bir `ClipData`'da
 * `IndexOutOfBoundsException` atar. Eski `ClipboardManager.getText()` bu kontrolu
 * kendi icinde yapiyordu; yeni `getClip()` ham `ClipData`'yi verdigi icin kontrol
 * artik CAGIRANA dustu. Tasima sirasinda gozden kacmasi kolay ve bedeli cokme.
 *
 * `coerceToText(context)` KULLANILMIYOR: `Context` istiyor, URI'leri cozmek icin
 * `ContentProvider`a gidiyor ve bu ekranin ihtiyaci olan sey yalnizca duz metin.
 */
actual fun ClipEntry.plainTextOrNull(): String? {
    if (clipData.itemCount == 0) return null
    return clipData.getItemAt(0)?.text?.toString()
}
