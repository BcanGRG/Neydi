package com.neydi.app

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import com.neydi.app.nav.Settings
import com.neydi.app.nav.DeleteData
import com.neydi.app.nav.MissingItems
import com.neydi.app.nav.History
import com.neydi.app.nav.Setup
import com.neydi.app.nav.Liste
import com.neydi.app.nav.TagCapture
import com.neydi.app.nav.NeydiSavedStateConfig
import com.neydi.app.ui.settings.DeleteDataRoute
import com.neydi.app.ui.settings.SettingsRoute
import com.neydi.app.ui.missing.MissingItemsRoute
import com.neydi.app.ui.history.HistoryRoute
import com.neydi.app.ui.setup.SetupRoute
import com.neydi.app.ui.capture.TagCaptureRoute
import com.neydi.app.ui.list.ListScreen
import com.neydi.app.ui.theme.NeydiTheme
import org.koin.compose.koinInject

/**
 * Bolum 09'un destinasyon sureleri: acilis 300 ms, kapanis 250 ms
 * ("Geri her zaman acilistan hizlidir"), mesafe tam genisligin %8'i.
 *
 * NEDEN BURADA: NavDisplay'e sure verilmezse kutuphanenin varsayilani
 * devreye giriyor ve o HER PLATFORMDA BASKA bir hareket yapiyor - Android'de
 * solma, desktop'ta hicbir sey, iOS'ta 500 ms'lik tam genislikte kayma. Tek
 * bir sure tasarimda yaziliyken uygulamada uc farkli hareket vardi.
 */
private const val DESTINATION_ENTER_MS = 300
private const val DESTINATION_EXIT_MS = 250
private const val DESTINATION_SLIDE_PERCENT = 8

@Composable
fun App() {
    // Katalog tohumlamasi acilista bir kez. Idempotent, ekrani BLOKLAMIYOR -
    // ilk acilista 257 satir yazilirken kullanici bos ekrana bakmamali.
    val bootstrap = koinInject<AppBootstrap>()
    // KURULUM TETIKLEYICISI (tasarim karari 6): `setupCompletedAt` bos VE hane
    // hic urun gormemis. Tohumlama BITTIKTEN sonra sorulmali - katalog
    // tohumlamasi urun yaratmiyor ama hane satirini yaratiyor ve sira
    // karisirsa sorgu bos veritabaninda kosardi.
    var showSetup by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        bootstrap()
        showSetup = bootstrap.needsSetup()
    }

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

        fun back() {
            if (backStack.size > 1) backStack.removeLastOrNull()
        }

        // KURULUM YIGININ KOKUNE OTURUYOR, Liste'nin USTUNE binmiyor.
        //
        // Onceki hal `backStack.add(Setup)` diyordu; altta Liste durdugu icin
        // TEK geri basisi - hangi adimda olursa olsun - kurulumu atlanmis
        // sayip Liste'ye dusuruyordu. Oysa gezinme sozlesmesi (Bolum 02) adim
        // 1/2 icin "Uygulamadan cikar; kurulum bir sonraki acilista bastan
        // gelir" diyor, ve Bolum 01 kurulumu "gezinmeyle ulasilamaz" bir tam
        // ekran akis sayiyor - yani altinda duracak bir ekran yok.
        //
        // Cikisi tek elemanli yigin KENDILIGINDEN sagliyor: NavDisplay geri
        // islemcisini `scene.previousEntries.isNotEmpty()` ile aciyor, altta
        // ekran kalmayinca geri tusu sisteme geciyor. Yani kurulum kokteyken
        // geri, Liste kokteyken oldugu gibi uygulamadan cikiyor.
        LaunchedEffect(showSetup) {
            if (showSetup) {
                // Kok ONCE yaziliyor, fazlasi SONRA atiliyor: NavDisplay bos
                // yigini `require` ile reddediyor, bu sirayla yigin hicbir an
                // bosalmiyor.
                backStack[0] = Setup
                backStack.removeAll(backStack.drop(1))
            }
        }

        NavDisplay(
            backStack = backStack,
            onBack = { back() },
            // ACILIS (300 ms): yeni ekran sagdan %8 kayarak geliyor, eskisi
            // ayni mesafede ters yone cekiliyor. SOLMA DA VAR cunku %8'lik
            // mesafede ekranin %92'si ilk karede zaten yerinde: tek basina
            // kayma, hareket degil kesme gibi okunurdu. Egri "standart
            // hizlanma" (FastOutSlowIn).
            transitionSpec = {
                val enterSlide = slideInHorizontally(
                    tween(DESTINATION_ENTER_MS, easing = FastOutSlowInEasing),
                ) { fullWidth -> fullWidth * DESTINATION_SLIDE_PERCENT / 100 }
                val exitSlide = slideOutHorizontally(
                    tween(DESTINATION_ENTER_MS, easing = FastOutSlowInEasing),
                ) { fullWidth -> -fullWidth * DESTINATION_SLIDE_PERCENT / 100 }
                val enterFade = fadeIn(tween(DESTINATION_ENTER_MS, easing = FastOutSlowInEasing))
                val exitFade = fadeOut(tween(DESTINATION_ENTER_MS, easing = FastOutSlowInEasing))
                (enterSlide + enterFade) togetherWith (exitSlide + exitFade)
            },
            // KAPANIS (250 ms): ayni hareketin tersi ve daha kisasi. Egri
            // "yavaslama" (LinearOutSlowIn) - geri donen ekran hizli baslayip
            // yerine oturuyor.
            popTransitionSpec = {
                val enterSlide = slideInHorizontally(
                    tween(DESTINATION_EXIT_MS, easing = LinearOutSlowInEasing),
                ) { fullWidth -> -fullWidth * DESTINATION_SLIDE_PERCENT / 100 }
                val exitSlide = slideOutHorizontally(
                    tween(DESTINATION_EXIT_MS, easing = LinearOutSlowInEasing),
                ) { fullWidth -> fullWidth * DESTINATION_SLIDE_PERCENT / 100 }
                val enterFade = fadeIn(tween(DESTINATION_EXIT_MS, easing = LinearOutSlowInEasing))
                val exitFade = fadeOut(tween(DESTINATION_EXIT_MS, easing = LinearOutSlowInEasing))
                (enterSlide + enterFade) togetherWith (exitSlide + exitFade)
            },
            entryProvider = entryProvider {
                entry<Liste> {
                    ListScreen(
                        toast = toast,
                        onToastShown = { toast = null },
                        onGoShopping = { go(MissingItems) },
                        onHistory = { go(History) },
                        onSettings = { go(Settings) },
                        onCapture = { go(TagCapture) },
                    )
                }
                entry<TagCapture> {
                    // Toast'i EKRANIN KENDISI gosteriyor: seri cekimde hedef
                    // kapanmiyor, dolayisiyla Liste'ye mesaj tasimanin anlami
                    // yok - ve tasinsaydi kullanici onu hic gormezdi.
                    TagCaptureRoute(onBack = { back() })
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
                entry<History> {
                    HistoryRoute(onBack = { back() })
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
                entry<Setup> {
                    SetupRoute(
                        onFinish = {
                            showSetup = false
                            // "Bitir" ve "Atla" kurulumu Liste ILE DEGISTIRIYOR.
                            // Kurulum kokte durdugu icin donulecek bir ekran yok;
                            // eski `back()` cagrisi (yigin tek elemanliyken)
                            // hicbir sey yapmaz, kullanici kurulumda kalirdi.
                            // Setup yigindan tumuyle ciktigi icin sozlesmenin
                            // "Kurulum bir daha acilmaz" maddesi de saglaniyor.
                            backStack[0] = Liste
                            backStack.removeAll(backStack.drop(1))
                        },
                    )
                }
            },
        )
    }
}
