package com.neydi.app.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.data.db.Store
import com.neydi.app.data.ocr.TagSkip
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.NeydiToast
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import androidx.compose.ui.tooling.preview.PreviewLightDark

/**
 * Etiket cekim ekrani (Ekran 4).
 *
 * ## KAMERA VE KART TEK EKRAN
 *
 * Onay karti kameranin USTUNDE aciliyor (karar 25) ve disina dokunmak
 * kapatmiyor - kapatmak icin acik bir "Vazgec" gerekiyor. Sebep: kart bir
 * bilgi katmani degil, bir IS; yanlislikla kapanmasi cekilen kareyi cope atar.
 *
 * ## EKRAN SAF
 *
 * ViewModel gormuyor, yalnizca state ve geri cagrilar aliyor. Onizlemeler her
 * hali kamera olmadan kurabiliyor - bu ekranin tek regresyon agi onlar, cunku
 * projede cihaz test kaynak kumesi yok.
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
    /** Kartta gosterilecek tarih - "bugun" (sartname). */
    today: String = "Bugün",
    modifier: Modifier = Modifier,
    cameraContent: @Composable () -> Unit = {},
) {
    Box(modifier.fillMaxSize().background(Color.Black)) {
        cameraContent()

        // IZIN REDDEDILDIYSE SESSIZ SIYAH KALMIYOR. `CaptureController.denied`
        // KDoc'u bunu sart kosuyor ve Android tarafi izin yoksa HICBIR SEY
        // cizmiyor - yani bu metni yazmak tamamen bu ekranin sorumlulugu.
        if (cameraDenied) {
            Text(
                text = "Kamera izni yok.\nEtiket çekmek için izin vermen gerekiyor.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
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
            // Kart aciksa kamerayi KARARTIYORUZ. Tasarim karti fotografin
            // uzerinde gosteriyor; canli onizlemeyi arkada birakmak "bu kare
            // cekildi" yalanini soylerdi - goruntu oynamaya devam ederdi.
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.86f)))
            ConfirmCardLayer(
                card = state.card,
                stores = state.stores,
                storeId = state.storeId,
                saving = state.saving,
                onSelectStore = onSelectStore,
                onPriceChange = onPriceChange,
                onProductChange = onProductChange,
                onSave = onSave,
                onDismiss = onDismissCard,
                today = today,
            )
        }

        // BILDIRIM EKRANIN KENDISINDE: seri cekimde Liste'ye donulmuyor.
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
                color = LocalNeydiExtraColors.current.warning,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(Spacing.md),
            )
        }
    }
}

@Composable
private fun BoxScopeShutter(onShutter: () -> Unit, enabled: Boolean) {
    Box(
        Modifier
            .size(76.dp)
            .clip(CircleShape)
            .pressable(enabled = enabled, onTap = onShutter)
            .background(if (enabled) Color.White else Color.White.copy(alpha = 0.35f)),
    )
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
    // CERCEVE REHBERI etiketin oraninda. Onizleme FILL_CENTER ciziyor, yani
    // rehberin icinde gorunen sey gercekten cekilen sey.
    Box(
        Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.82f)
            .height(220.dp)
            .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
    )

    Box(
        Modifier
            .align(Alignment.TopStart)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(Spacing.sm)
            .clip(CircleShape)
            .pressable(onTap = onBack)
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(Spacing.sm),
    ) {
        NeydiIcon(NeydiIcons.ArrowBack, contentDescription = "Geri", tint = Color.White)
    }

    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(bottom = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // MARKET CIPI CEKIMDEN ONCE. Yapiskan market yanlissa kullanici burada
        // duzeltiyor; karti bekletmek, yanlis markete yazilmis bir gozlemi
        // sonradan duzeltmek demekti.
        StoreChips(stores, storeId, onSelectStore)
        Spacer(Modifier.height(Spacing.md))
        BoxScopeShutter(onShutter = onShutter, enabled = ready)
    }
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
                color = if (selected) Color.Black else Color.White,
                modifier = Modifier
                    .clip(CircleShape)
                    .pressable(onTap = { onSelect(store.id) })
                    .background(if (selected) Color.White else Color.White.copy(alpha = 0.18f))
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
    onSelectStore: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onProductChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    today: String,
) {
    val extras = LocalNeydiExtraColors.current
    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(Spacing.md)
            .clip(RoundedCornerShape(20.dp))
            .background(CARD_BACKGROUND)
            .padding(Spacing.md),
    ) {
        card.missingFieldMessage()?.let { message ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(extras.warning.copy(alpha = 0.18f))
                    .padding(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NeydiIcon(NeydiIcons.Info, contentDescription = null, size = 18.dp, tint = extras.warning)
                Spacer(Modifier.width(Spacing.xs))
                Text(message, style = MaterialTheme.typography.bodySmall, color = extras.warning)
            }
            Spacer(Modifier.height(Spacing.sm))
        }

        CardField(label = "Fiyat") {
            if (card.reading) {
                Skeleton(width = 120.dp)
            } else {
                BasicTextField(
                    value = card.priceText,
                    onValueChange = onPriceChange,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = CARD_FOREGROUND,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(extras.accent),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    decorationBox = { inner ->
                        if (card.priceText.isEmpty()) {
                            Text(
                                "0,00",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CARD_FOREGROUND.copy(alpha = 0.35f),
                            )
                        }
                        inner()
                    },
                )
            }
        }

        CardField(label = "Ürün") {
            if (card.reading) {
                Skeleton(width = 200.dp)
            } else {
                BasicTextField(
                    value = card.productName,
                    onValueChange = onProductChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = CARD_FOREGROUND),
                    singleLine = true,
                    cursorBrush = SolidColor(extras.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    decorationBox = { inner ->
                        if (card.productName.isEmpty()) {
                            Text(
                                "Ürün adı",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CARD_FOREGROUND.copy(alpha = 0.35f),
                            )
                        }
                        inner()
                    },
                )
            }
        }

        // MARKA KESIK CERCEVEDE - "bu bir tahmin" demenin tasarimdaki yolu
        // (karar 39: marka yalnizca oneri). `Modifier.border` kesikli desen
        // alabiliyor: `Stroke.pathEffect` degil, `BorderStroke` + dashed
        // `PathEffect` gerekmiyor - kenarlik cizimi `drawBehind` ile yapiliyor.
        card.brand?.let { brand ->
            CardField(label = "Marka") {
                Text(
                    text = brand,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CARD_FOREGROUND,
                    modifier = Modifier
                        .drawBehind {
                            drawRoundRect(
                                color = CARD_FOREGROUND.copy(alpha = 0.45f),
                                cornerRadius = CornerRadius(8.dp.toPx()),
                                style = Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(6.dp.toPx(), 4.dp.toPx()),
                                    ),
                                ),
                            )
                        }
                        .padding(horizontal = Spacing.xs, vertical = 2.dp),
                )
            }
        }

        CardField(label = "Market") {
            StoreChips(stores, storeId, onSelectStore)
        }

        // TARIH DOKUNULAMAZ ve cipsiz - etikette basili tarih yok, "simdi" tek
        // dogru cevap (`PriceObservation.observedAt` KDoc'u). Cizilmesinin
        // sebebi kullanicinin NE kaydedildigini gormesi.
        CardField(label = "Tarih") {
            Text(today, style = MaterialTheme.typography.bodyLarge, color = CARD_FOREGROUND)
        }

        Spacer(Modifier.height(Spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            NeydiButton(
                text = "Vazgeç",
                onClick = onDismiss,
                container = Color.White.copy(alpha = 0.12f),
                content = CARD_FOREGROUND,
            )
            NeydiButton(
                text = if (saving) "Kaydediliyor…" else "Kaydet",
                onClick = onSave,
                enabled = card.canSave && !saving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CardField(label: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(vertical = Spacing.xs)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = CARD_FOREGROUND.copy(alpha = 0.55f),
        )
        Spacer(Modifier.height(2.dp))
        content()
    }
}

/** OCR 1,5 sn'yi gecerse alan iskelet cizilir - kart BEKLEMEZ. */
@Composable
private fun Skeleton(width: Dp) {
    Box(
        Modifier
            .width(width)
            .height(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(CARD_FOREGROUND.copy(alpha = 0.15f)),
    )
}

/**
 * Kart HER IKI TEMADA da koyu.
 *
 * Kameranin uzerinde duruyor ve kamera her zaman koyu; acik temada acik bir
 * kart cizmek onu yuzen beyaz bir dikdortgene cevirirdi. Renkler tema
 * tokenlarindan gelmiyor cunku bu yuzey temaya AIT DEGIL.
 */
private val CARD_BACKGROUND = Color(0xFF221A14)
private val CARD_FOREGROUND = Color(0xFFF5EDE6)

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
                priceText = "26,50",
                productName = "PUDRA ŞEKERİ",
                brand = "ŞAFAK",
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
