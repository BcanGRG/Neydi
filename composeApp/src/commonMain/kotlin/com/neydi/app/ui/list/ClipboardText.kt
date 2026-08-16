package com.neydi.app.ui.list

import androidx.compose.ui.platform.ClipEntry

/**
 * Panodaki duz metin, yoksa null.
 *
 * NEDEN EXPECT/ACTUAL GEREKTI: `LocalClipboardManager` kullanimdan kaldirildi ve
 * yerine gelen `LocalClipboard` metni `ClipEntry` olarak veriyor - ama `ClipEntry`
 * commonMain'de `expect class` ve **metin okuyan ortak bir public uyesi yok**.
 * Eski API'nin `getText()`i her platformda calisiyordu; yenisinde o kolaylik
 * yerini platform tipine birakti.
 *
 * Android'de icerik bir `ClipData`, iOS'ta `getPlainText()`. Compose'un kendi
 * `readText()` yardimcisi var ama `internal` - disaridan cagrilamiyor.
 *
 * Metin yoksa (resim, URI, bos pano) **null**: cagiran taraf zaten
 * `looksLikeList` ile eliyor, buradan bos dize donmek "pano bos mu yoksa
 * anlasilmadi mi" ayrimini kaybettirirdi.
 */
expect fun ClipEntry.plainTextOrNull(): String?
