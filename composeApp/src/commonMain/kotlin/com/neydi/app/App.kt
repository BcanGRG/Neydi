package com.neydi.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.neydi.app.nav.AlisverisiBitir
import com.neydi.app.nav.Ayarlar
import com.neydi.app.nav.Bilesenler
import com.neydi.app.nav.EksikOlabilir
import com.neydi.app.nav.Gecmis
import com.neydi.app.nav.Kurulum
import com.neydi.app.nav.Liste
import com.neydi.app.nav.NeydiSavedStateConfig
import com.neydi.app.ui.screens.AlisverisiBitirScreen
import com.neydi.app.ui.screens.AyarlarScreen
import com.neydi.app.ui.screens.ComponentGalleryScreen
import com.neydi.app.ui.screens.EksikOlabilirScreen
import com.neydi.app.ui.screens.GecmisScreen
import com.neydi.app.ui.screens.KurulumScreen
import com.neydi.app.ui.screens.ListeScreen
import com.neydi.app.ui.theme.NeydiTheme

@Composable
fun App() {
    NeydiTheme(darkTheme = isSystemInDarkTheme()) {
        // Surec olumunu ve konfigurasyon degisimini asar. Serializer kaydi
        // Destinations.kt'de; oradaki `when` kaydi unutmayi derleme hatasina cevirir.
        val backStack = rememberNavBackStack(NeydiSavedStateConfig, Liste)

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
                        onBilesenler = { git(Bilesenler) },
                    )
                }
                // GECICI: F3.2'de gercek Liste ekrani gelince kaldirilacak.
                entry<Bilesenler> { ComponentGalleryScreen(onGeri = { geri() }) }
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
