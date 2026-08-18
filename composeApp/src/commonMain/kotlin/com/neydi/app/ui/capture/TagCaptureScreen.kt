package com.neydi.app.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.Store
import com.neydi.app.data.ocr.TagSkip
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.NeydiToast
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Etiket cekim ekrani (Ekran 4).
 *
 * ## KAMERA VE KART TEK EKRAN
 *
 * Onay karti kameranin USTUNDE aciliyor (karar 25) ve disina dokunmak
 * kapatmiyor - kapatmak icin acik bir "Vazgec" var. Kart bir bilgi katmani
 * degil bir IS; yanlislikla kapanmasi cekilen kareyi cope atar.
 *
 * ## PALET TEMADAN BAGIMSIZ
 *
 * Vizor koyu, kart acik (karar 62). Degerler [Flow]'da ve `MaterialTheme`den
 * turemiyorlar - ekranin yarisi canli kamera goruntusu ve onun temasi yok.
 *
 * ## EKRAN SAF
 *
 * ViewModel gormuyor, yalnizca state ve geri cagrilar aliyor. Onizlemeler her
 * hali kamera olmadan kurabiliyor - projede cihaz test kaynak kumesi olmadigi
 * icin bu ekranin tek regresyon agi onlar.
 */
@Composable
internal fun TagCaptureScreen(
    state: TagCaptureState,
    cameraReady: Boolean,
    cameraDenied: Boolean,
    onShutter: () -> Unit,
    onSelectStore: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onProductChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissCard: () -> Unit,
    onToastShown: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    /** Kartta gosterilecek tarih - "bugun" (sartname). */
    today: String = "Bugün",
    cameraContent: @Composable () -> Unit = {},
) {
    Box(modifier.fillMaxSize().background(Flow.viewfinderInk)) {
        cameraContent()

        // IZIN REDDEDILDIYSE SESSIZ SIYAH KALMIYOR. `CaptureController.denied`
        // KDoc'u bunu sart kosuyor ve Android tarafi izin yoksa HICBIR SEY
        // cizmiyor - yani bu metni yazmak tamamen bu ekranin sorumlulugu.
        if (cameraDenied) {
            Text(
                text = "Kamera izni olmadan etiket çekilemez",
                style = MaterialTheme.typography.bodyLarge,
                color = Flow.viewfinderChrome,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(Spacing.lg),
            )
        }

        if (state.card == null) {
            CameraLayer(
                stores = state.stores,
                storeId = state.storeId,
                ready = cameraReady && !cameraDenied,
                onShutter = onShutter,
                onSelectStore = onSelectStore,
                onBack = onBack,
            )
        } else {
            // DONMUS KARE KARARABILIR (karar 62). "Ne cektim" dogrulamasi artik
            // kartin basindaki kirpim; arkadaki goruntunun isi bitmis oluyor.
            Box(Modifier.fillMaxSize().background(Flow.viewfinderInk.copy(alpha = 0.86f)))
            ConfirmCardLayer(
                card = state.card,
                stores = state.stores,
                storeId = state.storeId,
                saving = state.saving,
                today = today,
                onSelectStore = onSelectStore,
                onPriceChange = onPriceChange,
                onProductChange = onProductChange,
                onSave = onSave,
                onDismiss = onDismissCard,
            )
        }

        NeydiToast(
            message = state.toast,
            onShown = onToastShown,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = Spacing.xl),
        )

        state.failure?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = Flow.amber,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(Spacing.md),
            )
        }
    }
}

@Composable
private fun BoxScope.CameraLayer(
    stores: List<Store>,
    storeId: String?,
    ready: Boolean,
    onShutter: () -> Unit,
    onSelectStore: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // UST SERIT: kapat · market cipi · flas.
        //
        // KAPAT IKONU `close`, `arrow_back` DEGIL: sozlesme *"basligta ← yerine
        // ✕; ikisi de ayni isi yapar"* diyor ve tam ekran yuzeylerde ✕ dogru
        // olan. Kod geri okunu ciziyordu.
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier.size(TAP_TARGET).pressable(onTap = onBack),
                contentAlignment = Alignment.Center,
            ) {
                NeydiIcon(NeydiIcons.Close, contentDescription = "kapat", size = 24.dp, tint = Flow.viewfinderChrome)
            }

            // MARKET CIPI CEKIMDEN ONCE ve tek parca - yapiskan market yanlissa
            // kullanici burada duzeltiyor. Karti bekletmek, yanlis markete
            // yazilmis bir gozlemi sonradan duzeltmek demekti.
            StorePill(stores.firstOrNull { it.id == storeId }?.name)

            // FLAS: iki hal, oturumluk (karar 60). Bugun yalnizca cizilmis
            // durumda - davranisi CameraSurface'a bagli ve o F9.2 kuyrugunda.
            Box(
                Modifier.size(TAP_TARGET),
                contentAlignment = Alignment.Center,
            ) {
                NeydiIcon(NeydiIcons.Bolt, contentDescription = "flaş", size = 24.dp, tint = Flow.viewfinderChrome)
            }
        }

        // CERCEVE REHBERI AMBER ve 3:2. Amber sozlesmede "bir sey eksik ya da
        // emin degiliz" demek - rehber tam olarak o: etiket henuz kadraja
        // oturmadi. Kod beyaz ciziyordu ve orani sabit 220dp'ye baglamisti.
        Box(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 2f)
                    .border(3.dp, Flow.amber, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    text = "Etiket kadraja otursun.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Flow.viewfinderChrome,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 34.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Shutter(onShutter = onShutter, enabled = ready)
        }
    }

    // Market secici acilinca cipler kartin ustunde degil, seridin altinda.
    if (stores.isNotEmpty()) {
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp)) {
            StoreChips(stores, storeId, onSelectStore)
        }
    }
}

/** Ust seritteki market cipi: ad + `expand_more`. */
@Composable
private fun StorePill(name: String?) {
    Row(
        Modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(Flow.viewfinderChrome.copy(alpha = 0.14f))
            .border(1.dp, Flow.viewfinderChrome.copy(alpha = 0.34f), CircleShape)
            .padding(start = 14.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = name ?: "Market seç",
            style = MaterialTheme.typography.labelLarge,
            color = Flow.viewfinderChrome,
        )
        NeydiIcon(NeydiIcons.ExpandMore, contentDescription = null, size = 18.dp, tint = Flow.viewfinderChrome)
    }
}

/**
 * Deklansor: 72dp, koyu halka + acik dis kontur.
 *
 * Halkalar tasarimin kendi cizimi ve isleri var - dugme canli goruntunun
 * uzerinde duruyor, tek renk bir daire acik bir rafta kaybolurdu.
 */
@Composable
private fun Shutter(onShutter: () -> Unit, enabled: Boolean) {
    Box(
        Modifier
            .size(78.dp)
            .clip(CircleShape)
            .background(Flow.viewfinderChrome)
            .padding(3.dp)
            .clip(CircleShape)
            .background(Flow.viewfinderInk)
            .padding(4.dp)
            .clip(CircleShape)
            .pressable(enabled = enabled, onTap = onShutter)
            .background(if (enabled) Flow.viewfinderChrome else Flow.viewfinderChrome.copy(alpha = 0.35f)),
    )
}

@Composable
private fun StoreChips(stores: List<Store>, storeId: String?, onSelect: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        contentPadding = PaddingValues(horizontal = Spacing.md),
    ) {
        items(stores.size) { i ->
            val store = stores[i]
            val selected = store.id == storeId
            Text(
                text = store.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Flow.viewfinderInk else Flow.viewfinderChrome,
                modifier = Modifier
                    .clip(CircleShape)
                    .pressable(onTap = { onSelect(store.id) })
                    .background(
                        if (selected) Flow.viewfinderChrome else Flow.viewfinderChrome.copy(alpha = 0.18f),
                    )
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
            )
        }
    }
}

@Composable
private fun BoxScope.ConfirmCardLayer(
    card: ConfirmCard,
    stores: List<Store>,
    storeId: String?,
    saving: Boolean,
    today: String,
    onSelectStore: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onProductChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Hangi satir acik - bir seferde YALNIZCA biri, varsayilan hicbiri: kart
    // acilinca kullanici once OKUYOR, duzeltme istisna.
    var open by remember(card.photoPath) { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            // KART EKRANA YAPISIK ve yalnizca UST koseleri yuvarlak. Kod onu
            // her yandan 16dp bosluklu yuzen bir kutu olarak ciziyordu.
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Flow.cardBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // AMBER SERIT: fiyat blogunun SOLUNDA 4dp'lik dikey cubuk, ustunde
        // kutu degil. Kod tam genislikte dolgulu bir kutu ciziyordu ve
        // `warning` token'ini kullaniyordu - o amber METNIN rengi.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            card.missingFieldMessage()?.let {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Flow.amber),
                )
            }
            Column(Modifier.weight(1f)) {
                if (card.reading) {
                    Skeleton(width = 160.dp, height = 38.dp)
                } else {
                    PriceField(card = card, onPriceChange = onPriceChange)
                }
                card.missingFieldMessage()?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Flow.amberText,
                    )
                }
            }
            if (!card.reading && card.missingFieldMessage() == null) {
                Text(
                    text = "dokun, düzelt",
                    style = MaterialTheme.typography.bodySmall,
                    color = Flow.label,
                )
            }
        }

        CardRow(
            label = "Ürün",
            value = card.productName.ifBlank { "—" },
            reading = card.reading,
            onTap = { open = if (open == "urun") null else "urun" },
        )
        if (open == "urun") {
            InlineEditor(
                value = card.productName,
                placeholder = "Ürün adı",
                onChange = onProductChange,
            )
        }

        card.brand?.let { brand ->
            CardRow(label = "Marka", value = brand, reading = card.reading, dashed = true)
        }

        CardRow(
            label = "Market",
            value = stores.firstOrNull { it.id == storeId }?.name ?: "—",
            reading = false,
            store = true,
            onTap = { open = if (open == "market") null else "market" },
        )
        if (open == "market") {
            StoreChips(stores, storeId) { onSelectStore(it); open = null }
        }

        // TARIH DOKUNULAMAZ ve chevron TASIMIYOR - etikette basili tarih yok,
        // "simdi" tek dogru cevap (`PriceObservation.observedAt`).
        CardRow(label = "Tarih", value = today, reading = false, plain = true)

        SaveButton(enabled = card.canSave && !saving, saving = saving, onSave = onSave)

        // VAZGEC GERI GELDI. Kaldirmistim cunku maket yalnizca Kaydet
        // ciziyordu; yanlis olan yari maketmis - sozlesme onu baştan beri iki
        // yerde saydigi icin tasarim maketi duzeltti (karar C1).
        Box(
            Modifier
                .fillMaxWidth()
                .height(TAP_TARGET)
                .pressable(onTap = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Vazgeç",
                style = MaterialTheme.typography.labelLarge,
                color = Flow.cancel,
            )
        }
    }
}

/**
 * FIYAT MANSET: Fraunces 36sp.
 *
 * Etiketli bir alan degil - kartin isini tek bakista soyluyor: kaydedilecek
 * sey FIYAT, gerisi onun kimligi. Kod `headlineMedium` (24sp) kullaniyordu.
 */
@Composable
private fun PriceField(card: ConfirmCard, onPriceChange: (String) -> Unit) {
    BasicTextField(
        value = card.priceText,
        onValueChange = onPriceChange,
        textStyle = MaterialTheme.typography.displayLarge.copy(color = Flow.text),
        singleLine = true,
        cursorBrush = SolidColor(Flow.amber),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
        ),
        decorationBox = { inner ->
            if (card.priceText.isEmpty()) {
                Text(
                    text = "— TL",
                    style = MaterialTheme.typography.displayLarge,
                    color = Flow.label,
                )
            }
            inner()
        },
    )
}

@Composable
private fun InlineEditor(value: String, placeholder: String, onChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Flow.text),
        singleLine = true,
        cursorBrush = SolidColor(Flow.amber),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = Flow.label)
            }
            inner()
        },
    )
}

/**
 * Kartin bir satiri: etiket SOLDA sabit 74dp, deger SAGDA cip olarak.
 *
 * @param store market cipi yesil cizilir - secili marketi ayiran tek isaret.
 * @param dashed deger kesik cerceveye alinir (marka, karar 39).
 * @param plain cip yok, duz metin (tarih).
 */
@Composable
private fun CardRow(
    label: String,
    value: String,
    reading: Boolean,
    store: Boolean = false,
    dashed: Boolean = false,
    plain: Boolean = false,
    onTap: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onTap != null) Modifier.pressable(onTap = onTap) else Modifier)
            .height(ROW_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Flow.label,
            modifier = Modifier.width(74.dp),
        )
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            when {
                reading -> Skeleton(width = 120.dp, height = 20.dp)
                plain -> Text(value, style = MaterialTheme.typography.bodyLarge, color = Flow.label)
                dashed -> ValueChip(
                    value = value,
                    background = Flow.brandBackground,
                    textColor = Flow.brandText,
                    dashedBorder = Flow.brandBorder,
                )
                store -> ValueChip(
                    value = value,
                    background = Flow.storeChipBackground,
                    textColor = Flow.storeChipText,
                    border = Flow.storeChipBorder,
                )
                else -> ValueChip(
                    value = value,
                    background = Flow.chipBackground,
                    textColor = Flow.text,
                    border = Flow.chipBorder,
                )
            }
        }
        if (onTap != null) {
            NeydiIcon(
                icon = NeydiIcons.ChevronRight,
                contentDescription = null,
                size = 22.dp,
                tint = Flow.label,
            )
        }
    }
}

@Composable
private fun ValueChip(
    value: String,
    background: Color,
    textColor: Color,
    border: Color? = null,
    dashedBorder: Color? = null,
) {
    Box(
        Modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(background)
            .then(
                when {
                    border != null -> Modifier.border(1.dp, border, CircleShape)
                    dashedBorder != null -> Modifier.drawBehind {
                        drawRoundRect(
                            color = dashedBorder,
                            cornerRadius = CornerRadius(size.height / 2f),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(6.dp.toPx(), 4.dp.toPx()),
                                ),
                            ),
                        )
                    }
                    else -> Modifier
                },
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            maxLines = 1,
        )
    }
}

/**
 * Kaydet: 52dp, YESIL, pasifken dolgusu da metni de degisir.
 *
 * `NeydiButton` kullanilmiyor cunku o `enabled`i yalnizca `pressable`a
 * veriyor - pasif hal gorunmuyordu. Tasarim pasif icin ayri iki renk
 * veriyor ve "Kaydet pasif; ilk rakamda etkinlesir" sozlesmenin sarti.
 */
@Composable
private fun SaveButton(enabled: Boolean, saving: Boolean, onSave: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .pressable(enabled = enabled, onTap = onSave)
            .background(if (enabled) Flow.save else Flow.saveDisabled),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            // TEK KELIME: sozlesmenin ses kurali *"buton · fiil ve tek kelime"*.
            // Kod "Kaydediliyor..." yaziyordu; kaydetme hali artik butonun
            // pasifligiyle anlatiliyor.
            text = "Kaydet",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled && !saving) Flow.onSave else Flow.onSaveDisabled,
        )
    }
}

/** OCR donene kadar alan iskelet cizilir - ESIK YOK (karar 62). */
@Composable
private fun Skeleton(width: Dp, height: Dp = 22.dp) {
    Box(
        Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(Flow.chipBackground),
    )
}

/** En kucuk dokunma hedefi TEK SAYI 48dp (karar 56). */
private val TAP_TARGET = 48.dp

/** Kart satiri - tasarimin `min-height:56px`i. */
private val ROW_HEIGHT = 56.dp

@PreviewLightDark
@Composable
private fun TagCaptureCameraPreview() = NeydiPreview {
    TagCaptureScreen(
        state = TagCaptureState(stores = previewStores(), storeId = "s1"),
        cameraReady = true,
        cameraDenied = false,
        onShutter = {}, onSelectStore = {}, onPriceChange = {}, onProductChange = {},
        onSave = {}, onDismissCard = {}, onToastShown = {}, onBack = {},
    )
}

@PreviewLightDark
@Composable
private fun TagCaptureCardPreview() = NeydiPreview {
    TagCaptureScreen(
        state = TagCaptureState(
            card = ConfirmCard(
                photoPath = "/x.jpg",
                priceText = "24,90",
                productName = "Yoğurt 1 kg",
                brand = "Dost",
                reading = false,
                kurusFromOcr = true,
            ),
            stores = previewStores(),
            storeId = "s1",
        ),
        cameraReady = true, cameraDenied = false,
        onShutter = {}, onSelectStore = {}, onPriceChange = {}, onProductChange = {},
        onSave = {}, onDismissCard = {}, onToastShown = {}, onBack = {},
    )
}

@PreviewLightDark
@Composable
private fun TagCaptureUnreadChainPreview() = NeydiPreview {
    TagCaptureScreen(
        state = TagCaptureState(
            card = ConfirmCard(
                photoPath = "/x.jpg",
                reading = false,
                skipped = TagSkip.UNSUPPORTED_CHAIN,
            ),
            stores = previewStores(),
            storeId = "s2",
        ),
        cameraReady = true, cameraDenied = false,
        onShutter = {}, onSelectStore = {}, onPriceChange = {}, onProductChange = {},
        onSave = {}, onDismissCard = {}, onToastShown = {}, onBack = {},
    )
}

private fun previewStores(): List<Store> = listOf(
    Store(id = "s1", householdId = "h", name = "BİM", chain = "bim", createdAt = 0),
    Store(id = "s2", householdId = "h", name = "Migros", chain = "migros", createdAt = 0),
)
