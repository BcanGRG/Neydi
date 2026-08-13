package com.neydi.app.ui.theme

import androidx.compose.runtime.Composable

/**
 * iOS'ta karsiligi YOK — ve bu kasitli bir bosluk, unutulmus bir implementasyon degil.
 *
 * Status bar stili iOS'ta barindiran view controller'in `preferredStatusBarStyle`
 * ozelliginden gelir; Compose common kodundan set edilemez. Cozum Kotlin/Native
 * tarafinda, ComposeUIViewController'i saran bir controller ile yazilacak.
 *
 * Bu yuzden bu is bilincli olarak Faz 1'e kondu: Android'de tek satirlik bir
 * duzeltme gibi gorunuyor ama iOS'ta karsiligi olmayan bir alan, o yuzden cozum
 * bastan platform-ayrik kurgulanmali. Sonradan yamamak zor.
 *
 * TODO(ios-statusbar): F9.3 — preferredStatusBarStyle'i darkTheme'e bagla.
 * iOS'ta ayrica alt gezinme cubugu diye bir sey yok; home indicator alanini
 * SafeArea.bottom (34dp) zaten koruyor.
 */
@Composable
actual fun ApplySystemBarAppearance(darkTheme: Boolean) {
    // Kasitli olarak bos - bkz. yukaridaki not.
}
