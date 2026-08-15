package com.neydi.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.neydi.app.di.AppBootstrap
import com.neydi.app.nav.FinishShopping
import com.neydi.app.nav.Settings
import com.neydi.app.nav.DeleteData
import com.neydi.app.nav.MissingItems
import com.neydi.app.nav.History
import com.neydi.app.nav.ReceiptCheck
import com.neydi.app.nav.Setup
import com.neydi.app.nav.Liste
import com.neydi.app.nav.NeydiSavedStateConfig
import com.neydi.app.ui.finish.FinishShoppingRoute
import com.neydi.app.ui.settings.DeleteDataRoute
import com.neydi.app.ui.settings.SettingsRoute
import com.neydi.app.ui.missing.MissingItemsRoute
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

        // TOAST BURADA DURUYOR, ekranin icinde degil: mesaji URETEN yer
        // (Ekran 3'un atlama yolu) ile GOSTEREN yer (Liste) farkli
        // destinasyonlar. Gezinme ikisini de goren tek katman.
        var toast by remember { mutableStateOf<String?>(null) }

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
                        toast = toast,
                        onToastShown = { toast = null },
                        onGoShopping = { go(MissingItems) },
                        onHistory = { go(History) },
                        onSettings = { go(Settings) },
                        onOpenReceipt = { go(ReceiptCheck(it)) },
                        onFixTaken = { go(FinishShopping(it)) },
                    )
                }
                entry<MissingItems> {
                    // IKI TEL HATASI DUZELTILDI (F6.4):
                    // (1) `onAdd` Ekran 4'e (FinishShopping) gidiyordu -
                    //     yani "eksikleri ekle" demek alisverisi BITIRMEK
                    //     anlamina geliyordu. Ustelik FinishShopping(null)
                    //     bos listeye dusuyor, o ekran kalici olarak bos
                    //     ciziliyordu.
                    // (2) Tasarimin istedigi: secilenleri ekle ve ALISVERIS
                    //     MODUNA GIR. Artik oyle.
                    MissingItemsRoute(
                        // Mod gecisini ViewModel yapiyor (gezinin durumu,
                        // ekranin degil); burada yalnizca geri donuluyor.
                        onEnterShopping = { skipped ->
                            // Ekran hic gorunmediyse kullaniciya NE OLDUGUNU
                            // soyleyen tek sey bu toast (tasarim karari 8).
                            if (skipped) toast = "Liste hazır, eksik görünmüyor"
                            back()
                        },
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
                entry<Settings> {
                    SettingsRoute(onBack = { back() }, onDeleteData = { go(DeleteData) })
                }
                entry<DeleteData> {
                    DeleteDataRoute(
                        onBack = { back() },
                        // SILME BITINCE LISTEYE DONULUYOR, Ayarlar'a degil:
                        // arkadaki ekranlarin hepsi artik olmayan veriyi
                        // gosteriyor. Liste tek dogru varis - hem uygulamanin
                        // kendisi hem de silmeden sonra dogru olan tek ekran.
                        onDeleted = {
                            backStack.removeAll(backStack.drop(1))
                            toast = "Verilerin silindi"
                        },
                    )
                }
                entry<Setup> { SetupScreen(onFinish = { back() }) }
            },
        )
    }
}
