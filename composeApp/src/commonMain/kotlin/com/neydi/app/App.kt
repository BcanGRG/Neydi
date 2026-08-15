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
import com.neydi.app.nav.ReceiptCheck
import com.neydi.app.nav.Setup
import com.neydi.app.nav.Liste
import com.neydi.app.nav.NeydiSavedStateConfig
import com.neydi.app.ui.finish.FinishShoppingRoute
import com.neydi.app.ui.screens.SettingsScreen
import com.neydi.app.ui.screens.MissingItemsScreen
import com.neydi.app.ui.history.HistoryRoute
import com.neydi.app.ui.screens.SetupScreen
import com.neydi.app.ui.list.ListScreen
import com.neydi.app.ui.receipt.ReceiptCheckRoute
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

        // Ust ogeyi DEGISTIR, ustune ekleme: uzun fisin parca gecisinde geri
        // tusu onceki parcanin ekranina degil Liste/Gecmis'e donmeli - uc
        // parcalik fiste uc kontrol ekrani istiflemek geri tusunu bozar.
        fun replaceTop(key: NavKey) {
            backStack.removeLastOrNull()
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
                        onOpenReceipt = { go(ReceiptCheck(it)) },
                        onFixTaken = { go(FinishShopping(it)) },
                    )
                }
                entry<MissingItems> {
                    MissingItemsScreen(
                        onAdd = { go(FinishShopping()) },
                        onCancel = { back() },
                    )
                }
                entry<FinishShopping> { key ->
                    FinishShoppingRoute(
                        tripId = key.tripId,
                        // Gezi ZATEN KAPALI - bu ekran yalnizca iyimser
                        // mutabakati duzeltiyor, kapatmiyor. O yuzden back
                        // stack temizlenmiyor, sadece geri donuluyor.
                        onDone = { back() },
                    )
                }
                entry<ReceiptCheck> { key ->
                    ReceiptCheckRoute(
                        receiptId = key.receiptId,
                        onBack = { back() },
                        onOpenPart = { replaceTop(ReceiptCheck(it)) },
                    )
                }
                entry<History> {
                    HistoryRoute(
                        onBack = { back() },
                        onOpenReceipt = { go(ReceiptCheck(it)) },
                    )
                }
                entry<Settings> { SettingsScreen(onBack = { back() }) }
                entry<Setup> { SetupScreen(onFinish = { back() }) }
            },
        )
    }
}
