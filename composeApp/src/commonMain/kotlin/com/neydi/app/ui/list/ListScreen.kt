package com.neydi.app.ui.list

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlin.time.Clock
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.suggest.Suggestion
import com.neydi.app.data.db.Category
import com.neydi.app.data.looksLikeList
import com.neydi.app.ui.components.ListItemRow
import com.neydi.app.ui.components.ListRow
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.NeydiToast
import com.neydi.app.ui.components.SectionHeader
import com.neydi.app.ui.product.ProductSheetContent
import com.neydi.app.ui.theme.Motion
import com.neydi.app.ui.theme.Elevation
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.pressable
import com.neydi.app.ui.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

// ModalBottomSheet hala @ExperimentalMaterial3Api. Sheet'i kendimiz yazmak
// surukleme, scrim ve geri tusu davranisini yeniden uretmek demekti.
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    /** Gecici bildirim; gosterildikten sonra [onToastShown] cagriliyor. */
    toast: String? = null,
    onToastShown: () -> Unit = {},
    onGoShopping: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onOpenReceipt: (String) -> Unit,
    onFixTaken: (String) -> Unit,
    /** Fis cekme oturumunu acar (Ekran 4). Gezi kimligini tasiyor. */
    onCapture: (String) -> Unit,
    vm: ListViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    // Fis kaydedilir kaydedilmez Fis Kontrol ekranina gecis. Kullaniciyi
    // "fotograf cektim, sonra ne oldu" belirsizliginde birakmak, okuma
    // basarisiz olsa bile en kotu sonuc olurdu.
    val openReceiptId by vm.openReceiptId.collectAsStateWithLifecycle()
    LaunchedEffect(openReceiptId) {
        openReceiptId?.let { id ->
            vm.consumeOpenReceipt()
            onOpenReceipt(id)
        }
    }
    val input by vm.input.collectAsStateWithLifecycle()
    val suggestions by vm.suggestions.collectAsStateWithLifecycle()
    val engineSuggestions by vm.engineSuggestions.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val estimate by vm.estimate.collectAsStateWithLifecycle()
    val sheetOpen by vm.sheetOpen.collectAsStateWithLifecycle()
    val sheetCategory by vm.sheetCategory.collectAsStateWithLifecycle()
    val sheetProducts by vm.sheetProducts.collectAsStateWithLifecycle()
    val summary by vm.summary.collectAsStateWithLifecycle()
    val productSheet by vm.productSheet.collectAsStateWithLifecycle()
    val sheetAddedCount by vm.sheetAddedCount.collectAsStateWithLifecycle()
    val sheetQuery by vm.sheetQuery.collectAsStateWithLifecycle()
    val sheetResults by vm.sheetResults.collectAsStateWithLifecycle()
    val listMatchKeys by vm.listMatchKeys.collectAsStateWithLifecycle()
    val starters by vm.starterProducts.collectAsStateWithLifecycle()

    // Pano bir KEZ, ekran acilirken okunuyor. Her karede okumak hem pahali hem
    // de bazi sistemlerde "pano okundu" bildirimi tetikliyor.
    // Sheet'lere verilecek alt bosluk: BURADA okunuyor, sheet icinde degil.
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val clipboard = LocalClipboardManager.current
    var clipboardText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        clipboardText = clipboard.getText()?.text?.takeIf { looksLikeList(it) }
    }

    ListContent(
        state = state,
        input = input,
        suggestions = suggestions,
        engineSuggestions = engineSuggestions,
        onEngineSuggestion = vm::addFromEngine,
        categories = categories,
        clipboardText = clipboardText,
        onInputChange = vm::onInputChanged,
        onAdd = vm::add,
        onSuggestionSelected = vm::addFromSuggestion,
        onCategorySelected = vm::onCategorySelected,
        onStarterSelected = vm::addFromSheet,
        starters = starters,
        onToggleChecked = vm::toggleChecked,
        onRowLongPress = vm::openProductSheet,
        onClipboard = {
            clipboardText?.let(vm::addFromClipboard)
            clipboardText = null
        },
        onShoppingMode = vm::setShoppingMode,
        onGoShopping = onGoShopping,
        estimate = estimate,
        onOpenSheet = vm::openSheet,
        onFinish = vm::finishShopping,
        onHistory = onHistory,
        onSettings = onSettings,
        onAddFromLastTrip = vm::addFromLastTrip,
        toast = toast,
        onToastShown = onToastShown,
    )

    // Sheet, EKRAN DEGIL: liste arkada gorunur kaliyor.
    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = vm::closeSheet,
            // ZEMIN RENGI ACIKCA VERILIYOR. M3 varsayilani surfaceContainerLow
            // istiyor; bu palet o tonal token'lari TANIMLAMIYOR, o yuzden M3
            // kendi baseline'ina (mor tonlu) dusuyordu ve sheet uygulamanin
            // geri kalanina ait gorunmuyordu. Cihazda goruldu.
            containerColor = MaterialTheme.colorScheme.surface,
            // Inset'i SHEET tasimali, icerik degil: ModalBottomSheet pencere
            // inset'lerini kendisi tuketiyor, o yuzden icerideki
            // windowInsetsPadding sifir goruyor ve kacis butonu gezinme
            // cubugunun altinda kaliyordu. Cihazda goruldu; varsayilan
            // BottomSheetDefaults.windowInsets alt kenari kapsamiyor.
        ) {
            AddSheetContent(
                addedCount = sheetAddedCount,
                query = sheetQuery,
                onQueryChange = vm::onSheetQueryChanged,
                results = sheetResults,
                inList = listMatchKeys,
                // Inset SHEET DISINDA okunup duz bosluk olarak geciliyor.
                // ModalBottomSheet'in kendi contentWindowInsets'i bu agacta
                // etki etmedi (uc farkli deneme, ucu de cihazda kontrol edildi);
                // sheet icerigi ekranin dibine oturup gezinme cubugunun altinda
                // kaliyordu. Disaridan okunan deger belirsizlik birakmiyor.
                bottomPadding = bottomInset,
                categories = categories,
                selected = sheetCategory,
                products = sheetProducts,
                onCategory = vm::selectSheetCategory,
                onBackToCategories = vm::sheetBack,
                onProduct = vm::addFromSheet,
                onFreeText = vm::closeSheet,
            )
        }
    }

    // SISTEM KAMERASI BASLATICISI KALDIRILDI (F4.16). Cekim artik kendi
    // ekraninda (Ekran 4) ve `CaptureViewModel` uzerinden yuruyor; buradaki
    // "Activity yeniden yaratilinca remember sifirlanir" dersi de gecerliligini
    // yitirdi cunku uygulamadan hic cikilmiyor.

    productSheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = vm::closeProductSheet,
            // Zemin rengi ACIKCA veriliyor: bu palet `surfaceContainer*` tonal
            // token'larini tanimlamiyor ve M3 kendi mor baseline'ina dusuyor.
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            ProductSheetContent(
                state = sheet,
                onStapleChange = { vm.setStaple(sheet.productId, it) },
                bottomPadding = bottomInset,
            )
        }
    }

    // Ozet karti TEK SEFERLIK: kapatilinca bir daha acilmiyor.
    summary?.let { o ->
        ModalBottomSheet(
            onDismissRequest = vm::dismissSummary,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            SummaryCard(
                bottomPadding = bottomInset,
                takenCount = o.takenCount,
                totalCount = o.totalCount,
                amountMinor = o.amountMinor,
                durationMinutes = o.durationMinutes,
                onDismiss = vm::dismissSummary,
                onFixTaken = vm.summaryTripId?.let { id -> { onFixTaken(id) } },
                // UYGULAMA ICI KAMERAYA gidiyor, sistem kamerasina DEGIL.
                //
                // Sistem kamerasi uc seyi birden veremiyordu: cerceve rehberi
                // cizilemiyordu, kac kare cekildigi bilinmiyordu, ve arka
                // kamera yalnizca bir ISTEKTI - test cihazi (Samsung) yok
                // sayip on kamerayla aciyordu. Ucu de tasarimin Ekran 4'unde
                // zaten tarif ediliyordu ("1. kare" ... "Bitti").
                onTakeReceipt = vm.summaryTripId?.let { id -> { onCapture(id) } },
            )
        }
    }
}

/** Durumsuz govde: preview ve test buradan geciyor, ViewModel'siz. */
@Composable
internal fun ListContent(
    state: ListState,
    input: String,
    suggestions: List<CatalogSeed>,
    /** Motorun onerileri - girdi bosken cizilen serit (F6.3). */
    engineSuggestions: List<Suggestion>,
    onEngineSuggestion: (Suggestion) -> Unit,
    categories: List<Category>,
    clipboardText: String?,
    onInputChange: (String) -> Unit,
    onAdd: (String) -> Unit,
    onSuggestionSelected: (CatalogSeed) -> Unit,
    onCategorySelected: (Category) -> Unit,
    /** Ilk gun cipinden ekleme (tasarim karari 5). */
    onStarterSelected: (CatalogSeed) -> Unit = {},
    starters: List<CatalogSeed> = emptyList(),
    onToggleChecked: (String, Boolean) -> Unit,
    /** Satira uzun basma - Urun Detayi sheet'ini aciyor (F6.8). */
    onRowLongPress: (String) -> Unit,
    onClipboard: () -> Unit,
    onShoppingMode: (Boolean) -> Unit,
    /** Ekran 3'e gider (evden cikmadan son kontrol). */
    onGoShopping: () -> Unit,
    estimate: BasketEstimate,
    onOpenSheet: () -> Unit,
    onFinish: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onAddFromLastTrip: () -> Unit = {},
    toast: String? = null,
    onToastShown: () -> Unit = {},
    /**
     * Simdiki an - basligin "8 gun once" hesabi icin.
     *
     * DISARIDAN GELIYOR ki preview ve test deterministik olsun; composable
     * icinde saat okumak her yeniden cizimde farkli sonuc verirdi.
     */
    now: Long = Clock.System.now().toEpochMilliseconds(),
) {
    KeepScreenOn(state.shoppingMode)
    val haptics = LocalHapticFeedback.current

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top)
                    .asPaddingValues(),
            ) {
                item {
                    ListHeader(
                        state = state,
                        now = now,
                        onOpenSheet = onOpenSheet,
                        onHistory = onHistory,
                        onSettings = onSettings,
                        onLeaveShopping = { onShoppingMode(false) },
                    )
                }

                // Tahmin BASLIKTAN sonra, satirlardan once: rakam listeye
                // bakmadan once gorulmeli, alt tarafta kalirsa hic gorulmez.
                if (!state.isEmpty) {
                    item(key = "tahmin") {
                        EstimatedBasket(
                            amountMinor = estimate.amountMinor,
                            pricedCount = estimate.pricedCount,
                            totalCount = state.totalRows,
                        )
                    }
                }

                if (state.isEmpty) {
                    item {
                        EmptyState(
                            kind = state.emptyKind,
                            starters = starters,
                            hasClipboard = clipboardText != null,
                            onStarter = onStarterSelected,
                            onClipboard = onClipboard,
                            // Tasarimin metni: "Son alisveris 3 gun once, 642 TL."
                            lastTripLine = state.lastTrip?.let {
                                lastTripSummary(it, now).removePrefix("Son alışveriş: ")
                                    .let { text -> "Son alışveriş $text." }
                            },
                            // Hic kapanmis gezi yoksa buton HIC CIZILMIYOR -
                            // dokunuldugunda hicbir sey yapmayacak bir butonu
                            // gostermek tasarimin "bos bolum cizilmez"
                            // kuralinin ayni sinifi.
                            onAddFromLastTrip = if (state.lastTrip != null) onAddFromLastTrip else null,
                        )
                    }
                } else if (clipboardText != null && !state.shoppingMode) {
                    // Liste doluyken de yapistirilabilir - ama alisveris
                    // modunda ASLA: reyonda toplu ekleme yapilmaz.
                    item {
                        ClipboardChip(
                            count = com.neydi.app.data.clipboardLines(clipboardText).size,
                            onClipboard = onClipboard,
                        )
                    }
                }

                state.sections.forEach { section ->
                    item(key = "b-${section.title}") {
                        // Baslik da animasyonlu: satir kayarken baslik ziplasaydi
                        // hareket iki parcaya bolunur ve daha rahatsiz olurdu.
                        SectionHeader(
                            title = section.title,
                            count = section.rows.size,
                            modifier = Modifier.animateItem(
                                placementSpec = tween(Motion.REORDER_MS),
                            ),
                        )
                    }
                    items(section.rows, key = { it.id }) { row ->
                        ListItemRow(
                            // ISARETLENEN SATIR "Alindi"ya KAYARAK iner.
                            // Anahtar iki bolumde de ayni (TripLine id), o yuzden
                            // LazyColumn bunu yeni bir oge degil YER DEGISIMI
                            // goruyor ve animateItem araligi enterpole ediyor.
                            // Isinlanmamali: goz satiri takip edebilmeli, yoksa
                            // "ne oldu, nereye gitti" hissi kaliyor.
                            modifier = Modifier.animateItem(
                                placementSpec = tween(Motion.REORDER_MS),
                            ),
                            row = row.row,
                            shoppingMode = state.shoppingMode,
                            onToggle = {
                                // Haptik onay: reyonda goz listede degil rafta.
                                // Dokunusun islendigini parmak soyluyor.
                                // SNACKBAR YOK - bir gezide 20 isaretleme var,
                                // her birine snackbar ekrani felce ugratirdi.
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleChecked(row.id, !row.row.checked)
                            },
                            // Uzun basma Urun Detayi sheet'ini aciyor (F6.8).
                            // Tasarimin belirledigi acici fiyat cipi ama o
                            // ancak F5.2 ile gorunecek - bkz. ListItemRow.
                            onLongPress = { onRowLongPress(row.productId) },
                        )
                    }
                }

                if (state.taken.isNotEmpty()) {
                    item(key = "b-alindi") {
                        SectionHeader(
                            title = "Alındı",
                            count = state.taken.size,
                            modifier = Modifier.animateItem(
                                placementSpec = tween(Motion.REORDER_MS),
                            ),
                        )
                    }
                    items(state.taken, key = { it.id }) { row ->
                        ListItemRow(
                            modifier = Modifier.animateItem(
                                placementSpec = tween(Motion.REORDER_MS),
                            ),
                            row = row.row,
                            onToggle = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleChecked(row.id, false)
                            },
                            onLongPress = { onRowLongPress(row.productId) },
                        )
                    }
                }
            }

            // TOAST TOOLBAR'IN USTUNDE (tasarim: "floating toolbar'in 12dp
            // ustu; toolbar yoksa safe area + 12dp"). Alt blogun disinda
            // duruyor ki iki modda da ayni yere dussun.
            NeydiToast(
                message = toast,
                onShown = onToastShown,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Alisveris modunda hizli ekleme YOK: reyonda liste yazilmaz,
            // okunur. Klavye ekranin yarisini yerdi.
            if (!state.shoppingMode) {
                Column(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    ),
                ) {
                    QuickAdd(
                        input = input,
                        suggestions = suggestions,
                        engineSuggestions = engineSuggestions,
                        onInputChange = onInputChange,
                        onAdd = onAdd,
                        onSuggestionSelected = onSuggestionSelected,
                        onEngineSuggestion = onEngineSuggestion,
                    )
                    // BIRINCIL AKSIYON EN ALTTA (tasarim: "tum birincil
                    // aksiyonlar ekranin alt %40'inda"). Onceden yalnizca
                    // basliktaki cip seridinde vardi - yani tek elle tutulan
                    // telefonda erisilmesi en zor yerde.
                    //
                    // BOS LISTEDE CIZILMIYOR: alinacak bir sey yokken
                    // "Alisverise cikiyorum" demek, uygulamanin kendi bos
                    // durumunu gormemesi olurdu; bos hal zaten kendi
                    // yonlendirmesini gosteriyor.
                    if (!state.isEmpty) {
                        NeydiButton(
                            text = "Alışverişe çıkıyorum",
                            // EKRAN 3'E GIDIYOR, dogrudan moda GECMIYOR:
                            // tasarimin akisi "evden cikmadan son kontrol".
                            // Onerilecek bir sey yoksa Ekran 3 kendini
                            // atliyor ve moda geciyor (bkz. MissingItemsRoute).
                            onClick = onGoShopping,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md)
                                .padding(bottom = Spacing.sm),
                        )
                    }
                }
            } else {
                ShoppingBottomBar(
                    taken = state.totalRows - state.remainingRow,
                    total = state.totalRows,
                    onAdd = onOpenSheet,
                    onFinish = onFinish,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    ),
                )
            }
        }
    }
}

/**
 * Ekran 1 basligi (tasarim: 56dp, baslik 22sp/700, alt satir 14sp/400).
 *
 * GEZINME CIP SERIDI KALDIRILDI ve yerine tasarimin kendi cozumu kondu.
 * Serit tasarimda HIC YOK; kodda su sekilde buyumustu: once uc buton, sonra
 * "ekrana sigmiyor" olcumu, sonra yatay kaydirma, sonra dorduncu buton. Yani
 * tasarimin coktan cozdugu bir sorun ikinci kez cozuluyordu.
 *
 * Tasarimin cozumu ikiye ayiriyor: BIRINCIL aksiyon (alisverise cik) ekranin
 * ALTINDA tek basina - kurali "tum birincil aksiyonlar ekranin alt %40'inda",
 * cunku tek elle tutulan telefonda ust kenar en zor erisilen yer. IKINCIL
 * gezinme (reyonlardan ekle / gecmis / ayarlar) sag ustteki tasma menusunde.
 */
@Composable
private fun ListHeader(
    state: ListState,
    now: Long,
    onOpenSheet: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onLeaveShopping: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SizesExtra.header)
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = if (state.shoppingMode) "Alışveriş" else "Liste",
                // titleLarge = title22 (700/22sp) - tasarimdaki baslik bu.
                // Onceki headlineMedium 24sp'ydi ve token esleme tablosunda
                // headline24'e denk geliyor, yani iki punto buyuktu.
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (state.shoppingMode) {
                    "${state.remainingRow} kaldı"
                } else {
                    lastTripSummary(state.lastTrip, now)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // AVATAR BASLIGIN SABIT PARCASI (tasarim karari 10). Tek kullanicili
        // hanede de ciziliyor; es eklenince yalnizca NOKTA yesile donuyor,
        // yerlesim degismiyor. Gizlemek, ikinci kisi eklendigi gun basligi
        // yeniden ogretmek olurdu.
        state.selfInitials?.let { initials ->
            HeaderAvatar(initials = initials, hasPartner = state.hasPartner)
            Spacer(Modifier.width(Spacing.sm))
        }

        // ALISVERIS MODUNDA DA MENU VAR AMA TEK MADDELI (tasarim karari 1).
        //
        // Reyonda gezinme yine kapali - Ayarlar'a yanlislikla dusulemiyor,
        // ki kural buydu. Ama moddan CIKIS yolu yoktu: mod ekranin degil
        // GEZININ durumu, yani kalici; geri tusu da uygulamayi kapatmak da
        // cikarmiyordu. Tek cikis "Bitir"di, o da YAPILMAMIS bir alisverisi
        // kapatmak demekti.
        //
        // Moda girmek onaysiz kaliyor: ucuz hatanin karsiligi ucuz geri
        // donus, ikinci bir onay ekrani degil.
        OverflowMenu(
            shoppingMode = state.shoppingMode,
            onOpenSheet = onOpenSheet,
            onHistory = onHistory,
            onSettings = onSettings,
            onLeaveShopping = onLeaveShopping,
        )
    }
}

/** Basliktaki 28dp avatar + 6dp varlik noktasi (tasarim karari 10). */
@Composable
private fun HeaderAvatar(initials: String, hasPartner: Boolean) {
    val extras = LocalNeydiExtraColors.current
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(NeydiExtraShapes.pill)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary,
            )
        }
        // NOKTA YESIL DEGIL GRI, es yokken: "birileri var" demek yanlis
        // olurdu. Es eklendigi an ayni yerde yesile donuyor.
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(NeydiExtraShapes.pill)
                .background(
                    if (hasPartner) extras.success else MaterialTheme.colorScheme.outline,
                ),
        )
    }
}

/**
 * Sag ustteki `more_vert` tasma menusu - ikincil gezinmenin tamami.
 *
 * MENU OGELERI M3 `DropdownMenuItem` DEGIL: o da kendi ripple'ini getiriyor ve
 * tema override'i ona ulasmiyor (bkz. calisma sozlesmesi "Material3 Surface"
 * maddesi). Ogeler `Modifier.pressable` uzerine kuruldu, yani basili hal
 * uygulamanin geri kalaniyla ayni: %6 tonal overlay + 0.97 olcek.
 */
@Composable
private fun OverflowMenu(
    shoppingMode: Boolean,
    onOpenSheet: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onLeaveShopping: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier = Modifier
                .clip(NeydiExtraShapes.pill)
                .pressable(onTap = { open = true })
                .size(Sizes.minTapTarget),
            contentAlignment = Alignment.Center,
        ) {
            NeydiIcon(
                icon = NeydiIcons.MoreVert,
                contentDescription = "Menü",
                size = 22.dp,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = NeydiExtraShapes.card,
        ) {
            if (shoppingMode) {
                // TEK MADDE. Gezi KAPANMIYOR, mutabakat kosmuyor, liste ve
                // isaretlemeler oldugu gibi kaliyor - yalnizca mod birakiliyor.
                OverflowItem("Alışverişi bırak", NeydiIcons.Logout) {
                    open = false
                    onLeaveShopping()
                }
            } else {
                OverflowItem("Reyonlardan ekle") { open = false; onOpenSheet() }
                OverflowItem("Geçmiş") { open = false; onHistory() }
                OverflowItem("Ayarlar") { open = false; onSettings() }
            }
        }
    }
}

@Composable
private fun OverflowItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(onTap = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            NeydiIcon(icon = it, contentDescription = null, size = 20.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Panoda liste varsa tek dokunusluk cip.
 *
 * Ucuncu satir esigi PanoAyristirici'da: iki satirlik pano cogu zaman
 * kopyalanmis bir cumle, liste degil.
 */
@Composable
private fun ClipboardChip(count: Int, onClipboard: () -> Unit) {
    Column(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
        NeydiButton(
            text = "Panodaki $count satırı ekle",
            onClick = onClipboard,
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Alisveris modu alt cubugu.
 *
 * Alt %40'ta: bas parmak orada. Ustte olsaydi eli dolu bir insan telefonu
 * elinde cevirmek zorunda kalirdi.
 */
@Composable
private fun ShoppingBottomBar(
    taken: Int,
    total: Int,
    onAdd: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalNeydiExtraColors.current
    // TASARIMIN FLOATING TOOLBAR'I: pill kapsayici, surface zemin, 1dp hairline,
    // 6dp ic bosluk ve 3dp golge. Golge uygulamada IZINLI IKI YERDEN BIRI
    // (`Elevation.floatingToolbar`) - kaydirilan icerikte golge yok, ayrim
    // hairline ile yapiliyor. Onceki hali duz bir butondu, kapsayici yoktu.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm)
            .shadow(Elevation.floatingToolbar, NeydiExtraShapes.pill)
            .clip(NeydiExtraShapes.pill)
            .background(MaterialTheme.colorScheme.surface)
            .border(Sizes.hairline, extras.hairline, NeydiExtraShapes.pill)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Reyonda hizli ekleme alani GIZLI (F3.5: "reyonda liste yazilmaz,
        // okunur, klavye ekranin yarisini yer") - ama tasarim ekleme yolunu
        // kapatmiyor, klavyesiz bir hedefe donusturuyor: 56dp'lik buton Ekle
        // sheet'ini aciyor.
        ToolbarAction(
            icon = NeydiIcons.Add,
            description = "Ürün ekle",
            onClick = onAdd,
            container = MaterialTheme.colorScheme.primary,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
        NeydiButton(
            // Sayac tasarimdan: kac tanesi alindi / kac tane var.
            text = "Bitir ($taken/$total)",
            onClick = onFinish,
            modifier = Modifier.weight(1f),
            container = MaterialTheme.colorScheme.secondary,
            content = MaterialTheme.colorScheme.onSecondary,
        )
    }
}

/** Floating toolbar'in 56dp'lik dairesel hedefi (tasarim: `size/toolbarAction`). */
@Composable
private fun ToolbarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    container: Color = Color.Transparent,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = Modifier
            .size(Sizes.toolbarAction)
            .clip(NeydiExtraShapes.pill)
            .pressable(onTap = onClick)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        NeydiIcon(icon = icon, contentDescription = description, size = 26.dp, tint = tint)
    }
}

// --- Preview ---------------------------------------------------------------

private fun s(
    id: String,
    name: String,
    count: String? = null,
    checked: Boolean = false,
    isStaple: Boolean = false,
) = UiRow(
    id = id,
    productId = "p-$id",
    row = ListRow(name, quantity = count, checked = checked, isStaple = isStaple),
)

private val SAMPLE = ListState(
    sections = listOf(
        ListSection("Meyve-Sebze", listOf(s("1", "Domates", "1 kg"), s("2", "Salatalık"))),
        ListSection("Fırın-Ekmek", listOf(s("3", "Tam Buğday Ekmek"))),
    ),
    loading = false,
)

@Composable
private fun ListPreviewHost(
    state: ListState,
    clipboard: String? = null,
    estimate: BasketEstimate = BasketEstimate(),
) = ListContent(
    state = state, input = "", suggestions = emptyList(),
    engineSuggestions = emptyList(), onEngineSuggestion = {}, categories = emptyList(),
    clipboardText = clipboard,
    onInputChange = {}, onAdd = {}, onSuggestionSelected = {}, onCategorySelected = {},
    onToggleChecked = { _, _ -> }, onRowLongPress = {}, onClipboard = {}, onShoppingMode = {},
    onGoShopping = {},
    estimate = estimate, onOpenSheet = {}, onFinish = {}, onHistory = {}, onSettings = {},
)

/** Tahmin satiri: fiyat verisi geldiginde boyle gorunecek. */
@PreviewLightDark
@Composable
private fun WithEstimatePreview() = NeydiPreview(padding = Spacing.xs) {
    ListPreviewHost(SAMPLE, estimate = BasketEstimate(amountMinor = 48750, pricedCount = 2))
}

@PreviewLightDark
@Composable
private fun PlanningPreview() = NeydiPreview(padding = Spacing.xs) { ListPreviewHost(SAMPLE) }

/** Alisveris modu: 72dp satir, gezinme gizli, alt cubuk gorunur. */
@PreviewLightDark
@Composable
private fun ShoppingModePreview() = NeydiPreview(padding = Spacing.xs) {
    ListPreviewHost(
        SAMPLE.copy(
            shoppingMode = true,
            sections = listOf(
                ListSection("Meyve-Sebze", listOf(s("1", "Domates", "1 kg", checked = true), s("2", "Salatalık"))),
                ListSection("Fırın-Ekmek", listOf(s("3", "Tam Buğday Ekmek"))),
            ),
        ),
    )
}

@PreviewLightDark
@Composable
private fun ClipboardChipPreview() = NeydiPreview(padding = Spacing.xs) {
    ListPreviewHost(SAMPLE, clipboard = "ekmek\nsüt\nyumurta\nzeytin")
}
