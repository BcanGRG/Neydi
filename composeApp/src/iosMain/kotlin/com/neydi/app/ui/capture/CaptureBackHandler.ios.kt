package com.neydi.app.ui.capture

import androidx.compose.runtime.Composable

/**
 * DIKKAT: Windows'ta DERLENMEZ, dolayisiyla DOGRULANMAMIS.
 *
 * iOS'ta donanim geri tusu YOK - hicbir sey yakalanmiyor ve bu bos govde
 * durust bir bosluk degil, dogru karsilik. Kart kenardan kaydirma jestiyle
 * kapatilacak; o da Nav3'un kendi Scene isi (F9.2 kapsaminda).
 */
@Composable
actual fun CaptureBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
