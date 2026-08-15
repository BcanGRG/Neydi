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
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
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
    onGoShopping: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onOpenReceipt: (String) -> Unit,
    onFixTaken: (String) -> Unit,
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
        onToggleChecked = vm::toggleChecked,
        onRowLongPress = vm::openProductSheet,
        onClipboard = {
            clipboardText?.let(vm::addFromClipboard)
            clipboardText = null
        },
        onShoppingMode = vm::setShoppingMode,
        estimate = estimate,
        onOpenSheet = vm::openSheet,
        onFinish = vm::finishShopping,
        onHistory = onHistory,
        onSettings = onSettings,
        onAddFromLastTrip = vm::addFromLastTrip,
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

    // KAMERA BASLATICISI SHEET'IN DISINDA. Icinde remember edilseydi sheet
    // kapaninca composable yok olur ve kamera donusunde (Android bu arada
    // Activity'yi yeniden olusturabilir) sonucu teslim edecek yer kalmazdi.
    val receiptsDir = remember { FileKit.filesDir / "receipts" }
    val cameraLauncher = rememberCameraPickerLauncher { file ->
        if (file != null) {
            // HEDEF YOL DURUMDAN DEGIL, KAYNAK ADINDAN turetiliyor: "ham-X.jpg"
            // -> "fis-X.jpg".
            //
            // Ilk halde hedefi `remember` icinde tutuyordum ve CIHAZDA HIC
            // CALISMADI: kamera on plandayken Android Activity'yi yeniden
            // olusturuyor, `remember` sifirlaniyor, hedef null oluyor ve fis
            // sessizce hic kaydedilmiyordu - diskte yalnizca ham dosya kalmisti.
            // `rememberSaveable` da cozerdi ama durumu tamamen kaldirmak daha
            // saglam: kurtarilacak bir sey yok.
            val dest = receiptsDir / ("fis-" + file.name.removePrefix("ham-"))
            // `raw` BIZIM kurdugumuz PlatformFile, yani gercek dosya yolu.
            // `file.absolutePath()` ise `content://` URI donuyor - onunla ne
            // okuma ne silme calisiyor.
            val raw = receiptsDir / file.name
            vm.attachReceipt(
                source = file,
                destPath = dest.absolutePath(),
                rawPath = raw.absolutePath(),
            )
        }
    }

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
                onTakeReceipt = {
                    receiptsDir.createDirectories()
                    // Ad zaman damgasi: iki fis birbirini EZMESIN. Sabit ad
                    // kullanmak ayni gezide ikinci fisi (uzun fis iki parca
                    // basildiginda oluyor) birincinin ustune yazardi.
                    val stamp = Clock.System.now().toEpochMilliseconds()
                    // KAMERA GECICI YOLA yaziyor, kucultme NIHAI yola.
                    //
                    // Ilk halde ikisi AYNI dosyaydi: kamera dogrudan hedefe
                    // yaziyor, yani kucultme kaynagi ile hedefi cakisiyordu ve
                    // cihazda sessizce ise yaramadi - gorsel 2944px kaldi,
                    // sinir 2576 oldugu halde. Testler bunu goremezdi; ancak
                    // ciktinin gercek boyutunu olcmek yakaladi.
                    val temp = receiptsDir / "ham-$stamp.jpg"
                    // Konumsal cagri: Kotlin parametre adlari bytecode'da yok
                    // ve 'facing' tahminim yanlisti - derleyici soyledi.
                    //
                    // ARKA KAMERA yalnizca bir ISTEK. Sistem kamerasi yoksayabiliyor
                    // ve test cihazi (Samsung) yoksayiyor: on kamerayla aciliyor,
                    // kullanici tek dokunusla ceviriyor. Duzeltemedigimiz bir yer.
                    cameraLauncher.launch(FileKitCameraType.Photo, FileKitCameraFacing.Back, temp)
                },
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
    onToggleChecked: (String, Boolean) -> Unit,
    /** Satira uzun basma - Urun Detayi sheet'ini aciyor (F6.8). */
    onRowLongPress: (String) -> Unit,
    onClipboard: () -> Unit,
    onShoppingMode: (Boolean) -> Unit,
    estimate: BasketEstimate,
    onOpenSheet: () -> Unit,
    onFinish: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onAddFromLastTrip: () -> Unit = {},
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
                            categories = categories,
                            hasClipboard = clipboardText != null,
                            onCategory = onCategorySelected,
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
                            onClick = { onShoppingMode(true) },
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
        // Alisveris modunda gezinme GIZLI: reyonda yanlislikla Ayarlar'a
        // dusmek listeyi kaybetmek gibi hissettirir.
        if (!state.shoppingMode) {
            OverflowMenu(
                onOpenSheet = onOpenSheet,
                onHistory = onHistory,
                onSettings = onSettings,
            )
        }
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
    onOpenSheet: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
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
            OverflowItem("Reyonlardan ekle") { open = false; onOpenSheet() }
            OverflowItem("Geçmiş") { open = false; onHistory() }
            OverflowItem("Ayarlar") { open = false; onSettings() }
        }
    }
}

@Composable
private fun OverflowItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(onTap = onClick)
            .heightIn(min = Sizes.minTapTarget)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
