package com.neydi.app.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.Store
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import androidx.compose.ui.tooling.preview.PreviewLightDark

/**
 * Onay kartindan acilan uc secici.
 *
 * ## Ucu de AYNI YUZEY, ayni palet
 *
 * Ucu de vizorun onunde acilan acik renkli bir sheet; [Flow] paletini
 * kullaniyorlar, `MaterialTheme` renklerini DEGIL. Sebep karar 62: etiket
 * akisinin paleti temadan bagimsiz - kullanici koyu temadayken bu sheet'lerin
 * koyu gelmesi akisin ortasinda palet degistirmek olurdu.
 *
 * ## Neden uc ayri bilesen
 *
 * Uc secici uc FARKLI is yapiyor ve tasarim ucunu de ayri cizmis:
 *
 * | Secici | Arama | Liste sekli | Yeni kayit |
 * |---|---|---|---|
 * | Urun | var | 60dp iki satirli | "+ Yeni urun olarak ekle" |
 * | Market | var | 56dp tek satir + onay isareti | "+ Yeni market" (iki dokunus) |
 * | Marka | **yok** | sarilan cipler | yok - havuz OCR'dan buyur |
 *
 * Markada arama YOK ve bu bilincli (karar 52): klavye acmak karar 39'un
 * gerekcesini - hiz ve kirlilik - geri getirirdi. Liste kisa kaliyor.
 */
private const val SHEET_CORNER = 28

/** Maket 44 ciziyor; karar 56 "44 gecen satirlar duzeltildi" diyor. */
private val ROW_STORE = 56.dp
private val ROW_PRODUCT = 60.dp

// ---------------------------------------------------------------- urun secici

/**
 * Urun secici (karar 51).
 *
 * ## Etiket metni KANIT olarak yaziliyor
 *
 * Basliktaki "Etiket metni: DST YGRT 1000G" satiri kullaniciya neye baktigimizi
 * gosteriyor. OCR'in okudugu sey urun ADI degil - ad katalogdan geliyor - ama
 * kullanicinin bizim neyi okudugumuzu gormesi, secimini dogru yapmasinin tek
 * yolu. Etiket okunamadiysa satir hic cizilmiyor; bos bir "Etiket metni:"
 * kullaniciya bir sey soylemez.
 *
 * ## Odak KENDILIGINDEN GITMIYOR
 *
 * Karar 51 bunu ayrica yaziyor ve sozlesmenin *"hicbir ekran acilirken klavye
 * acmaz"* kuralinin parcasi. Reyonda tek elle calisan biri icin acilan klavye,
 * listenin yarisini ortmekten baska is yapmiyor - aday zaten listede.
 */
@Composable
internal fun ProductPicker(
    tagText: String?,
    lastProduct: String?,
    query: String,
    picks: List<CatalogSeed>,
    onQueryChange: (String) -> Unit,
    onPick: (String) -> Unit,
    onBack: () -> Unit,
) {
    // TAM EKRAN, sheet degil: liste uzun ve arama var. Tasarim da bunu tam
    // ekran ciziyor (geri oku + baslik), digerlerini sheet.
    Column(
        Modifier
            .fillMaxSize()
            .background(Flow.cardBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().height(Sizes.minTapTarget),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.clip(NeydiExtraShapes.pill).pressable(onTap = onBack).padding(4.dp)) {
                    NeydiIcon(NeydiIcons.ArrowBack, contentDescription = "geri", size = 24.dp, tint = Flow.text)
                }
                Text(
                    text = "Ürün seç",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Flow.text,
                )
            }
            tagText?.takeIf { it.isNotBlank() }?.let {
                Row {
                    Text("Etiket metni: ", style = MaterialTheme.typography.bodyMedium, color = Flow.cancel)
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Flow.cancel,
                    )
                }
            }
            SearchField(value = query, placeholder = "ürün ara", onChange = onQueryChange)
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(Flow.chipBorder))

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp)) {
            // SON SECILEN URUN EN BASTA - karar 51'in "hazir gelir" niyeti
            // BURADA yasiyor, kartta degil. Karta yazildiginda her cekimde bir
            // ad IDDIA ediyordu ve iddia neredeyse hep yanlisti (bkz.
            // `TagCaptureViewModel.lastProductName`); burada bir TEKLIF ve
            // yanlissa hicbir sey maliyeti yok.
            if (lastProduct != null && query.isBlank()) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .pressable(onTap = { onPick(lastProduct) })
                            .height(ROW_PRODUCT),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(lastProduct, style = MaterialTheme.typography.bodyLarge, color = Flow.text)
                        Text(
                            text = "son seçtiğin",
                            style = MaterialTheme.typography.bodySmall,
                            color = Flow.label,
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Flow.chipBorder))
                }
            }
            items(picks.size) { i ->
                val seed = picks[i]
                Column(
                    Modifier
                        .fillMaxWidth()
                        .pressable(onTap = { onPick(seed.name) })
                        .height(ROW_PRODUCT),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(seed.name, style = MaterialTheme.typography.bodyLarge, color = Flow.text)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Flow.chipBorder))
            }
            item {
                // ARAMA BOSSA CIZILMIYOR: adsiz bir urun yaratmanin yolu olmamali.
                if (query.isNotBlank()) {
                    AddRow(label = "Yeni ürün olarak ekle", onTap = { onPick(query.trim()) })
                }
            }
        }

        Text(
            // v1'DE VAAT EDILMEYEN SEY YAZILMIYOR. Tasarimin dipnoti "secim bu
            // markette bu etiket metnine baglanir; ayni etiket bir daha
            // sorulmaz" diyor - o baglama icin etiket metni -> urun tablosu
            // gerekiyor ve sema isi (karar 51 · Faz 4). Olmayan bir sozu
            // yazmak, kullanicinin bir daha sorulmayacagini sanmasi demek.
            text = "Ürün adı katalogdan gelir; etiketten okunan metin ad olarak kullanılmaz.",
            style = MaterialTheme.typography.bodySmall,
            color = Flow.label,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 34.dp),
        )
    }
}

// -------------------------------------------------------------- market secici

/**
 * Market secici (karar 40 + 59).
 *
 * ## "+ Yeni market" IKI dokunus
 *
 * Ilk dokunus hicbir sey yaratmiyor, yalnizca onay cipini aciyor:
 * `«AKYRUT» diye yeni market`. Ikinci dokunus marketi yaziyor. Gerekce karar
 * 59'da: tek dokunusla yaratilan ve hicbir dokunusla yok edilemeyen varlik
 * olmaz - yazim hatasi sonsuza kadar listede kalirdi.
 *
 * ## Uzun dokunus siler, ama yalnizca gozlemsiz marketi
 *
 * Silme kapisi ViewModel'de: `deleteStore` once `hasObservationsAt` soruyor.
 * Gozlemli bir marketi silmek, o marketin butun fiyat gecmisini yetim
 * birakirdi.
 */
@Composable
internal fun StorePicker(
    stores: List<Store>,
    storeId: String?,
    query: String,
    pendingName: String?,
    onQueryChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onPropose: (String) -> Unit,
    onConfirmNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Arama YALNIZCA suzuyor - eslesme yoksa liste bos kalir ve "+ Yeni market"
    // gorunur hale gelir; tasarimin akisi bu.
    val shown = stores.filter { it.name.contains(query.trim(), ignoreCase = true) }

    PickerSheet(title = "Market", onDismiss = onDismiss) {
        SearchField(value = query, placeholder = "market ara", onChange = onQueryChange)

        Column(Modifier.fillMaxWidth()) {
            shown.forEach { store ->
                Box(Modifier.fillMaxWidth().height(1.dp).background(Flow.chipBorder))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressable(
                            onLongPress = { onDelete(store.id) },
                            onTap = { onSelect(store.id) },
                        )
                        .height(ROW_STORE),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = store.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Flow.text,
                        modifier = Modifier.weight(1f),
                    )
                    if (store.id == storeId) {
                        NeydiIcon(
                            icon = NeydiIcons.CheckCircle,
                            contentDescription = "seçili",
                            size = 22.dp,
                            tint = Flow.storeChipText,
                        )
                    }
                }
            }
        }

        if (pendingName != null) {
            // ONAY CIPI: adin kendisi cumlenin icinde, cunku onaylanan sey
            // "yeni market" degil, TAM OLARAK BU AD.
            Row(
                Modifier
                    .height(Sizes.minTapTarget)
                    .clip(NeydiExtraShapes.pill)
                    .pressable(onTap = onConfirmNew)
                    .background(Flow.brandBackground)
                    .border(1.5.dp, Flow.brandBorder, NeydiExtraShapes.pill)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    // AD YAZILDIGI GIBI - `uppercase()` KALDIRILDI.
                    //
                    // Locale'siz donusum Turkce'de bozuyor: "iskur" -> "ISKUR",
                    // dogrusu "ISKUR" degil "İŞKUR". Tasarim sistemi bunu
                    // denetim listesine yazmis (*"sifir uppercase/lowercase
                    // donusumu - Turkce İ/ı bozulmaz"*) ve burasi kacmisti.
                    //
                    // Maket ornegi buyuk harfli (`«AKYRUT»`) ama o ORNEGIN
                    // kendisi buyuk harfli; bicimlendirme kurali degil. Ustelik
                    // cipin kendi gerekcesi de bunu soyluyor: onaylanan sey
                    // "yeni market" degil TAM OLARAK BU AD - donusturulmus bir
                    // ad o iddiayi cururtuyor.
                    text = "«$pendingName» diye yeni market",
                    style = MaterialTheme.typography.labelLarge,
                    color = Flow.brandText,
                )
            }
        } else if (query.isNotBlank()) {
            AddRow(label = "Yeni market", onTap = { onPropose(query) })
        }
    }
}

// --------------------------------------------------------------- marka sheeti

/**
 * Marka sheet'i - KLAVYESIZ (karar 52).
 *
 * Aday havuzu "bu markette gorulmus TUM markalar"; onceki hali "bu urun icin
 * gorulmus"tu ve kisir dongu uretiyordu - yeni bir marka dogru adiyla
 * girebilmek icin once ayni urunde bir kez okunmus olmasi gerekiyordu.
 *
 * `Marka yok` da bir secim: markasiz urun (acik reyon, manav) markasi
 * BILINMEYEN urunden farkli, ve kullanici bu farki soyleyebilmeli.
 */
@Composable
internal fun BrandPicker(
    productName: String,
    selected: String?,
    pool: List<String>,
    onPick: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    PickerSheet(
        title = "Marka",
        subtitle = if (productName.isBlank()) "bu markette görülenler" else "$productName · bu markette görülenler",
        onDismiss = onDismiss,
    ) {
        // Sarilan cip izgarasi: liste kisa oldugu icin kaydirma yerine sarma.
        // FlowRow yerine elle satirlama YOK - FlowRow deneysel degil ve
        // projede zaten kullaniliyor olsaydi onu kullanirdim; burada tek
        // sutunlu Column + Row yerine dogrudan sarma gerekiyor.
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            pool.forEach { brand ->
                BrandChip(text = brand, selected = brand == selected, onTap = { onPick(brand) })
            }
            // "Marka yok" HER ZAMAN var, havuz bos olsa bile: bos bir sheet
            // kullaniciya cikis birakmazdi.
            Row(
                Modifier
                    .height(Sizes.minTapTarget)
                    .clip(NeydiExtraShapes.pill)
                    .pressable(onTap = { onPick(null) })
                    .background(Flow.cardBackground)
                    .border(1.dp, Flow.label, NeydiExtraShapes.pill)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Marka yok", style = MaterialTheme.typography.labelLarge, color = Flow.cancel)
            }
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(Flow.chipBorder))
        Text(
            text = "Markalar etiketten okundukça çoğalır; buraya yazamazsın.",
            style = MaterialTheme.typography.bodySmall,
            color = Flow.label,
        )
    }
}

/** Secili marka KESIK CERCEVE - "bu bir oneri" jesti (karar 39). */
@Composable
private fun BrandChip(text: String, selected: Boolean, onTap: () -> Unit) {
    Row(
        Modifier
            .height(Sizes.minTapTarget)
            .clip(NeydiExtraShapes.pill)
            .pressable(onTap = onTap)
            .background(if (selected) Flow.brandBackground else Flow.chipBackground)
            .then(
                if (selected) Modifier.border(1.5.dp, Flow.brandBorder, NeydiExtraShapes.pill)
                else Modifier.border(1.dp, Flow.chipBorder, NeydiExtraShapes.pill),
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Flow.brandText else Flow.text,
        )
    }
}

// ------------------------------------------------------------------- ortaklar

/**
 * Alta yapisik, yalnizca UST koseleri yuvarlak sheet - kartin ikizi.
 *
 * ## KARARTMA VE DISARI DOKUNUSU
 *
 * Ilk halinde sheet yalnizca CIZIYORDU: ne karartma vardi, ne disari
 * dokununca kapanma, ne de dokunusu durduran bir sey. Iki sonucu birden
 * uretiyordu ve ikincisi sessizdi:
 *
 * 1. Sheet'ten cikmanin tek yolu bir secim yapmakti - "fikrimi degistirdim"
 *    diye bir cikis yoktu.
 * 2. Sheet'in USTUNDEKI bosluga dokunmak, dokunusu ALTTAKI karta geciriyordu:
 *    marka sheet'i acikken bosluga basmak kartin urun satirina denk gelip
 *    urun secicisini aciyordu. Kullanici bir sheet kapatmak isterken baska
 *    bir sheet aciyordu.
 *
 * Karartma ayni anda ucuncu isi yapiyor: sheet'in altindakinin ETKISIZ
 * oldugunu soyluyor. Tasarimin sheet golgesi (`0 -8px 24px rgba(0,0,0,.28)`)
 * zaten bunu varsayiyor.
 */
@Composable
private fun PickerSheet(
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            // KARARTMA HEM KAPATIYOR HEM DURDURUYOR: `pressable` degil cunku
            // karartmanin bir basma animasyonu olmamali - dokunulan sey o
            // degil, kapattigi sey o.
            .background(Flow.viewfinderInk.copy(alpha = 0.46f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = SHEET_CORNER.dp, topEnd = SHEET_CORNER.dp))
                // SHEET DOKUNUSU YUTUYOR: bos bir `detectTapGestures`, altindaki
                // karartmanin "kapat"ini gormesin diye. Olmasaydi sheet'in
                // kendi bosluguna basmak da onu kapatirdi.
                .pointerInput(Unit) { detectTapGestures { } }
                .background(Flow.cardBackground)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.padding(start = 2.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Flow.text,
                )
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = Flow.cancel)
                }
            }
            content()
        }
    }
}

/** Sheet basindaki arama alani - odak KENDILIGINDEN GITMIYOR (karar 51). */
@Composable
private fun SearchField(value: String, placeholder: String, onChange: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(Sizes.minTapTarget)
            .clip(RoundedCornerShape(14.dp))
            .background(Flow.chipBackground)
            .border(1.dp, Flow.chipBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        NeydiIcon(NeydiIcons.Search, contentDescription = null, size = 20.dp, tint = Flow.label)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Flow.text),
            singleLine = true,
            cursorBrush = SolidColor(Flow.amber),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = Flow.label)
                }
                inner()
            },
        )
    }
}

/** `+ ...` satiri - tek ekleme ikonu, kiremit metin (Ikonografi). */
@Composable
private fun AddRow(label: String, onTap: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .pressable(onTap = onTap)
            .height(Sizes.minTapTarget),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        NeydiIcon(NeydiIcons.Add, contentDescription = null, size = 20.dp, tint = Flow.addAction)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Flow.addAction,
        )
    }
}

@PreviewLightDark
@Composable
private fun StorePickerPreview() = NeydiPreview {
    Box(Modifier.fillMaxSize().background(Flow.viewfinderInk)) {
        StorePicker(
            stores = listOf(
                Store(id = "1", householdId = "h", name = "BİM", chain = "BİM", createdAt = 0),
                Store(id = "2", householdId = "h", name = "Migros", chain = "Migros", createdAt = 0),
            ),
            storeId = "1",
            query = "",
            pendingName = null,
            onQueryChange = {}, onSelect = {}, onDelete = {}, onPropose = {}, onConfirmNew = {}, onDismiss = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun StorePickerPendingPreview() = NeydiPreview {
    Box(Modifier.fillMaxSize().background(Flow.viewfinderInk)) {
        StorePicker(
            stores = emptyList(),
            storeId = null,
            query = "akyrut",
            pendingName = "akyrut",
            onQueryChange = {}, onSelect = {}, onDelete = {}, onPropose = {}, onConfirmNew = {}, onDismiss = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun BrandPickerPreview() = NeydiPreview {
    Box(Modifier.fillMaxSize().background(Flow.viewfinderInk)) {
        BrandPicker(
            productName = "Yoğurt 1 kg",
            selected = "Dost",
            pool = listOf("Dost", "Sütaş", "Pınar", "İçim"),
            onPick = {},
            onDismiss = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ProductPickerPreview() = NeydiPreview {
    ProductPicker(
        tagText = "DST YGRT 1000G",
        lastProduct = "Süt 1 L",
        query = "yoğurt",
        picks = listOf(
            CatalogSeed(id = "1", name = "Yoğurt 1 kg", matchKey = "yogurt 1 kg", categoryId = "sut", commonalityRank = 1, defaultUnit = "kg"),
            CatalogSeed(id = "2", name = "Yoğurt 500 g", matchKey = "yogurt 500 g", categoryId = "sut", commonalityRank = 2, defaultUnit = "kg"),
        ),
        onQueryChange = {}, onPick = {}, onBack = {},
    )
}
