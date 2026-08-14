package com.neydi.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.neydi.app.di.AppBootstrap
import com.neydi.app.nav.FinishShopping
import com.neydi.app.nav.Settings
import com.neydi.app.nav.MissingItems
import com.neydi.app.nav.History
import com.neydi.app.nav.Setup
import com.neydi.app.nav.Liste
import com.neydi.app.nav.NeydiSavedStateConfig
import com.neydi.app.ui.screens.FinishShoppingScreen
import com.neydi.app.ui.screens.SettingsScreen
import com.neydi.app.ui.screens.MissingItemsScreen
import com.neydi.app.ui.screens.HistoryScreen
import com.neydi.app.ui.screens.SetupScreen
import com.neydi.app.ui.list.ListScreen
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

        fun go(key: NavKey) {
            backStack.add(key)
        }

        fun back() {
            if (backStack.size > 1) backStack.removeLastOrNull()
        }

        NavDisplay(
            backStack = backStack,
            onBack = { back() },
            entryProvider = entryProvider {
                entry<Liste> {
                    ListScreen(
                        onGoShopping = { go(MissingItems) },
                        onHistory = { go(History) },
                        onSettings = { go(Settings) },
                    )
                }
                entry<MissingItems> {
                    MissingItemsScreen(
                        onAdd = { go(FinishShopping()) },
                        onBosver = { back() },
                    )
                }
                entry<FinishShopping> { key ->
                    FinishShoppingScreen(
                        tripId = key.tripId,
                        onOnayla = {
                            // Alisveris kapandi -> Liste'ye don, ara ekranlari birak
                            backStack.clear()
                            backStack.add(Liste)
                        },
                    )
                }
                entry<History> { HistoryScreen(onBack = { back() }) }
                entry<Settings> { SettingsScreen(onBack = { back() }) }
                entry<Setup> { SetupScreen(onFinish = { back() }) }
            },
        )
    }
}
