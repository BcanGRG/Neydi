package com.neydi.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.neydi.app.nav.AlisverisiBitir
import com.neydi.app.nav.Ayarlar
import com.neydi.app.nav.EksikOlabilir
import com.neydi.app.nav.Gecmis
import com.neydi.app.nav.Kurulum
import com.neydi.app.nav.Liste
import com.neydi.app.ui.screens.AlisverisiBitirScreen
import com.neydi.app.ui.screens.AyarlarScreen
import com.neydi.app.ui.screens.EksikOlabilirScreen
import com.neydi.app.ui.screens.GecmisScreen
import com.neydi.app.ui.screens.KurulumScreen
import com.neydi.app.ui.screens.ListeScreen
import com.neydi.app.ui.theme.NeydiTheme

@Composable
fun App() {
    NeydiTheme(darkTheme = isSystemInDarkTheme()) {
        // TODO(saveable): rememberNavBackStack'e gecilecek. Once iOS icin
        // SavedStateConfiguration + polymorphic SerializersModule gerekiyor
        // (bkz. nav/Destinations.kt sonundaki not). Milestone 1'de duz liste.
        val backStack = remember { mutableStateListOf<NavKey>(Liste) }

        fun git(key: NavKey) {
            backStack.add(key)
        }

        fun geri() {
            if (backStack.size > 1) backStack.removeLastOrNull()
        }

        NavDisplay(
            backStack = backStack,
            onBack = { geri() },
            entryProvider = entryProvider {
                entry<Liste> {
                    ListeScreen(
                        onAlisveriseCik = { git(EksikOlabilir) },
                        onGecmis = { git(Gecmis) },
                        onAyarlar = { git(Ayarlar) },
                    )
                }
                entry<EksikOlabilir> {
                    EksikOlabilirScreen(
                        onEkle = { git(AlisverisiBitir()) },
                        onBosver = { geri() },
                    )
                }
                entry<AlisverisiBitir> { key ->
                    AlisverisiBitirScreen(
                        tripId = key.tripId,
                        onOnayla = {
                            // Alisveris kapandi -> Liste'ye don, ara ekranlari birak
                            backStack.clear()
                            backStack.add(Liste)
                        },
                    )
                }
                entry<Gecmis> { GecmisScreen(onGeri = { geri() }) }
                entry<Ayarlar> { AyarlarScreen(onGeri = { geri() }) }
                entry<Kurulum> { KurulumScreen(onBitir = { geri() }) }
            },
        )
    }
}
