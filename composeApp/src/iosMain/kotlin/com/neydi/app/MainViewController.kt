package com.neydi.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS giris noktasi. Windows'ta DERLENMEZ - Gradle host'un desteklemedigi
 * hedeflerin task'lerini calistirmaz. Mac'e gecildiginde tek satir degisiklik
 * gerekmeden derlenmeye baslar.
 *
 * TODO(ios): Mac'e gecildiginde sirasiyla:
 *   1. Status bar stili preferredStatusBarStyle uzerinden ayarlanacak -
 *      common koddan set EDILEMEZ.
 *   2. nav/Destinations.kt'deki SavedStateConfiguration + SerializersModule
 *      isi yapilacak, yoksa back stack sessizce restore olmaz.
 *   3. Fis fotografi PLATFORM TARAFINDA kucultulecek (long edge <= 2576px),
 *      common koda gectikten SONRA degil - CMP iOS'ta UIImage/kamera frame
 *      bellek baskisi bilinen bir sorun.
 */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
