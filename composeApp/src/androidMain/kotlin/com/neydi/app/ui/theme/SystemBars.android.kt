package com.neydi.app.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

/**
 * API 26'da acik gezinme cubugu ikonlari desteklenmiyor. SystemBarStyle.light'in
 * ikinci parametresi yalnizca orada devreye girer ve ikonlarin okunur kalmasi icin
 * yariseffaf koyu bir zemin cizer. API 27+ bunu hic kullanmaz.
 * Deger AndroidX'in kendi varsayilaniyla ayni.
 */
private val API26_NAV_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

@Composable
actual fun ApplySystemBarAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val activity = view.context.findComponentActivity() ?: return

    SideEffect {
        // Cubuklar seffaf: arkalarinda uygulamanin kendi surface rengi gorunur,
        // yani kontrast paletten gelir. enableEdgeToEdge tekrar cagrilabilir;
        // darkTheme her degistiginde stil yeniden uygulanir.
        val style =
            if (darkTheme) SystemBarStyle.dark(Color.TRANSPARENT)
            else SystemBarStyle.light(Color.TRANSPARENT, API26_NAV_SCRIM)

        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}

/** LocalView'in context'i genelde bir ContextThemeWrapper olur; Activity'ye kadar yuru. */
private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
