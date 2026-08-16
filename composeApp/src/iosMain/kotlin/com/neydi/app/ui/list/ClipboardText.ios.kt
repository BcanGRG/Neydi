package com.neydi.app.ui.list

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.getPlainText
import androidx.compose.ui.platform.hasPlainText

/**
 * DIKKAT: Windows'ta DERLENMEZ, dolayisiyla DOGRULANMAMIS.
 * iOS karsiligi `UIPasteboard`, Compose'un `getPlainText()` sarmalayicisiyla.
 *
 * `clipMetadata`ya BAKILMIYOR: iOS actual'inda o getter `TODO()` atiyor, yani
 * "metin var mi" sorusunu oradan sormak calisma zamaninda coker. `hasPlainText()`
 * ayni soruyu cokmeden cevapliyor.
 *
 * Yuzey bilerek IKI CAGRIYLA sinirli: bu dosya ancak Mac'te derlenecegi icin
 * ne kadar kucukse yazim hatasi riski o kadar dusuk.
 */
@OptIn(ExperimentalComposeUiApi::class)
actual fun ClipEntry.plainTextOrNull(): String? =
    if (hasPlainText()) getPlainText() else null
