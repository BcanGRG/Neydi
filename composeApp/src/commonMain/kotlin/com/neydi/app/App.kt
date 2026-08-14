package com.neydi.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.neydi.app.di.AppBootstrap
import com.neydi.app.nav.AlisverisiBitir
import com.neydi.app.nav.Ayarlar
import com.neydi.app.nav.EksikOlabilir
import com.neydi.app.nav.Gecmis
import com.neydi.app.nav.Kurulum
import com.neydi.app.nav.Liste
import com.neydi.app.nav.NeydiSavedStateConfig
import com.neydi.app.ui.screens.AlisverisiBitirScreen
import com.neydi.app.ui.screens.AyarlarScreen
import com.neydi.app.ui.screens.EksikOlabilirScreen
import com.neydi.app.ui.screens.GecmisScreen
import com.neydi.app.ui.screens.KurulumScreen
import com.neydi.app.ui.liste.ListeEkrani
import com.neydi.app.ui.theme.NeydiTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    // Katalog tohumlamasi acilista bir kez. Idempotent, ekrani BLOKLAMIYOR -
    // ilk acilista 257 satir yazilirken kullanici bos ekrana bakmamali.
    val bootstrap = koinInject<AppBootstrap>()
    LaunchedEffect(Unit) { bootstrap() }

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
                    ListeEkrani(
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
