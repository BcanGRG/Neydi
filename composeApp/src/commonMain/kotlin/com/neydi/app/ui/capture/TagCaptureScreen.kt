package com.neydi.app.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.NeydiToast
import com.neydi.app.ui.theme.Sizes
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
    cameraPermanentlyDenied: Boolean,
    flashOn: Boolean,
    onShutter: () -> Unit,
    onSelectStore: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onOpenPicker: (TagPicker) -> Unit,
    onClosePicker: () -> Unit,
    onPickProduct: (String) -> Unit,
    onSearchProducts: (String) -> Unit,
    onPickBrand: (String?) -> Unit,
    onSearchStores: (String) -> Unit,
    onProposeStore: (String) -> Unit,
    onConfirmNewStore: () -> Unit,
    onDeleteStore: (String) -> Unit,
    onSave: () -> Unit,
    onDismissCard: () -> Unit,
    onToastShown: () -> Unit,
    onFailureShown: () -> Unit,
    onToggleFlash: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    /** Kartta gosterilecek tarih - "bugun" (sartname). */
    today: String = "Bugün",
    cameraContent: @Composable () -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current

    // DEKLANSOR FLASI: 120 ms ortucu (karar 55).
    //
    // Kullanicinin bildirdigi eksik buydu - deklansore basinca hicbir sey
    // degismiyordu ve ekran donmus gibi hissettiriyordu. Olculen bosluk
    // gercek: cekimden karta ~1,15 sn geciyor (docs/17) ve o surede tek
    // hareket `pressable`in %97 olcek darbesiydi, yani BASMA anini
    // gosteriyordu, CEKIMIN oldugunu degil.
    //
    // Sayac ekranin kendi hali: flas bir sunum olayi, state'e girmesi
    // gerekmiyor - ViewModel'in bundan haberi olmamali.
    var shutterCount by remember { mutableIntStateOf(0) }
    val flash = remember { Animatable(0f) }
    LaunchedEffect(shutterCount) {
        if (shutterCount == 0) return@LaunchedEffect
        flash.snapTo(1f)
        flash.animateTo(0f, tween(FLASH_MS))
    }

    Box(modifier.fillMaxSize().background(Flow.viewfinderInk)) {
        cameraContent()

        // IZIN REDDEDILDIYSE SESSIZ SIYAH KALMIYOR. `CaptureController.denied`
        // KDoc'u bunu sart kosuyor ve Android tarafi izin yoksa HICBIR SEY
        // cizmiyor - yani bu metni yazmak tamamen bu ekranin sorumlulugu.
        if (cameraDenied) {
            Column(
                Modifier.align(Alignment.Center).padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                // CUMLE SOZLESMEDEN ALINIYOR VE IKI HAL AYRI KONUSUYOR.
                //
                // Kod ikisine de "Etiket cekmek icin kamera izni gerekiyor"
                // yaziyordu; o cumle sozlesmenin hicbir yerinde gecmiyor ve iki
                // farkli durumu tek cumleye bindiriyordu. Kalici rette sistem
                // BIR DAHA SORMUYOR - soylenecek sey bir DURUM ("izin kapali")
                // ve tek cikis alttaki Ayarlar dugmesi. Gecici rette sistem
                // tekrar soruyor - soylenecek sey neyin yapilamadigi.
                Text(
                    text = if (cameraPermanentlyDenied) "Kamera izni kapalı"
                    else "Kamera izni olmadan etiket çekilemez",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Flow.viewfinderChrome,
                    textAlign = TextAlign.Center,
                )
                // KALICI RETTE AYARLAR, geciciDE degil. Gecici retten sonra
                // sistem tekrar soruyor - ekrana donmek yetiyor; kalici retten
                // sonra sistem BIR DAHA SORMUYOR ve Ayarlar tek yol. Ikisine
                // ayni dugmeyi koymak, birinde hicbir sey yapmayan bir dugme
                // demekti.
                if (cameraPermanentlyDenied) {
                    Box(
                        Modifier
                            .height(Sizes.minTapTarget)
                            .clip(NeydiExtraShapes.pill)
                            .pressable(onTap = onOpenSettings)
                            .background(Flow.viewfinderChrome)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Ayarları aç",
                            style = MaterialTheme.typography.labelLarge,
                            color = Flow.viewfinderInk,
                        )
                    }
                }
            }
        }

        if (state.card == null) {
            CameraLayer(
                stores = state.stores,
                storeId = state.storeId,
                ready = cameraReady && !cameraDenied,
                flashOn = flashOn,
                onToggleFlash = onToggleFlash,
                onShutter = {
                    // HAPTIK UC OLAYDA SAYILIYOR (sozlesme): isaretleme, CEKIM,
                    // kaydet. Cekim ve kaydet ayni darbeyi kullaniyor cunku
                    // ikisi de "bir sey yazildi" diyor; isaretleme listede
                    // daha hafif olani kullaniyor.
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    shutterCount++
                    onShutter()
                },
                onOpenPicker = { onOpenPicker(TagPicker.STORE) },
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
                onPriceChange = onPriceChange,
                onOpenPicker = onOpenPicker,
                onSave = onSave,
                onDismiss = onDismissCard,
            )
        }

        // SECICI KATMANI: kartin uzerinde, cunku secici karta cevap veriyor -
        // altinda kalsaydi kullanici hangi karta secim yaptigini goremezdi.
        when (state.picker) {
            TagPicker.PRODUCT -> ProductPicker(
                tagText = state.card?.tagText,
                query = state.productQuery,
                picks = state.productPicks,
                onQueryChange = onSearchProducts,
                onPick = onPickProduct,
                onBack = onClosePicker,
            )

            TagPicker.STORE -> StorePicker(
                stores = state.stores,
                storeId = state.storeId,
                query = state.storeQuery,
                pendingName = state.pendingStoreName,
                onQueryChange = onSearchStores,
                onSelect = onSelectStore,
                onDelete = onDeleteStore,
                onPropose = onProposeStore,
                onConfirmNew = onConfirmNewStore,
                onDismiss = onClosePicker,
            )

            TagPicker.BRAND -> BrandPicker(
                productName = state.card?.productName.orEmpty(),
                selected = state.card?.brand,
                pool = state.brandPool,
                onPick = onPickBrand,
                onDismiss = onClosePicker,
            )

            null -> Unit
        }

        // FLAS EN USTTE: karti ve toast'i da bir an icin ortuyor, cunku
        // ortucu flasi fiziksel bir olayin taklidi - kamera bir kare aldi.
        if (flash.value > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Flow.viewfinderChrome.copy(alpha = flash.value)),
            )
        }

        // GECICI BILDIRIM TEK YUZEY: hata da toast'tan gecer.
        //
        // Hata ayri bir SERIT olarak ekranin USTUNE ve 4 sn ciziliyordu; ikisi
        // de sozlesmeye aykiri. Sozlesme gecici bildirim icin tek bir sey
        // taniyor - *"Toast: 2 sn, aksiyonsuz, kuyruksuz"* - ve cekim hatalarini
        // ("Kamera su an kullanilamiyor", "Yer kalmadi, fotograf alinamadi")
        // adiyla toast sayiyor. Iki ayri yuzey, ayni isi yapan iki bildirim
        // dili demekti: basarili kayit altta ve 2 sn, basarisiz cekim ustte ve
        // 4 sn. Sure de artik tek yerde ([NeydiToast]); FAILURE_MS sabiti KDoc'u
        // "toast ile ayni omur" derken 4000 tutuyordu, yani yorum yalan
        // soyluyordu.
        //
        // CAKISMADA HATA ONDE: kaydetme bildirimi gorunurken cekim basarisiz
        // olabiliyor (kart kapandiktan sonraki ilk deklansor). O anda ekranda
        // "kaydedildi" durup hatanin susmasi, kullaniciya kare alinmis gibi
        // gorunurdu. Ustu ortulen mesaj CIZILMEDEN dusuruluyor - kuyruk yok;
        // state'te birakilsaydi hatadan sonra gecikmeli olarak cizilirdi.
        NeydiToast(
            message = state.failure ?: state.toast,
            onShown = {
                if (state.toast != null) onToastShown()
                if (state.failure != null) onFailureShown()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = Spacing.xl),
        )
    }
}

@Composable
private fun BoxScope.CameraLayer(
    stores: List<Store>,
    storeId: String?,
    ready: Boolean,
    flashOn: Boolean,
    onShutter: () -> Unit,
    onToggleFlash: () -> Unit,
    onOpenPicker: () -> Unit,
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
                Modifier.size(Sizes.minTapTarget).pressable(onTap = onBack),
                contentAlignment = Alignment.Center,
            ) {
                NeydiIcon(NeydiIcons.Close, contentDescription = "kapat", size = 24.dp, tint = Flow.viewfinderChrome)
            }

            // MARKET CIPI CEKIMDEN ONCE ve tek parca - yapiskan market yanlissa
            // kullanici burada duzeltiyor. Karti bekletmek, yanlis markete
            // yazilmis bir gozlemi sonradan duzeltmek demekti.
            StorePill(stores.firstOrNull { it.id == storeId }?.name, onTap = onOpenPicker)

            // FLAS: iki hal, oturumluk (karar 60). ACIK hal DOLU bir daireyle
            // gosteriliyor - ikon rengini degistirmek yetmezdi: canli kamera
            // goruntusunun uzerinde "biraz daha parlak sari" bir hal degildir.
            Box(
                Modifier
                    .size(Sizes.minTapTarget)
                    .clip(NeydiExtraShapes.pill)
                    .pressable(onTap = onToggleFlash)
                    .background(if (flashOn) Flow.viewfinderChrome else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                NeydiIcon(
                    icon = NeydiIcons.Bolt,
                    contentDescription = if (flashOn) "flaşı kapat" else "flaşı aç",
                    size = 24.dp,
                    tint = if (flashOn) Flow.viewfinderInk else Flow.viewfinderChrome,
                )
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

}

/**
 * Ust seritteki market cipi: ad + `expand_more`, DOKUNUNCA SECICI ACILIR.
 *
 * `expand_more` bir SOZ: "buraya dokun, bir liste acilacak". Hap o oku
 * cizmesine ragmen hicbir sey yapmiyordu - secim, deklansorun uzerine cizilen
 * ayri bir cip seridinden yapiliyordu. O serit iki sorun birden uretiyordu:
 * ikonun sozunu bos cikariyordu VE deklansorun ortasini kapatiyordu, yani
 * dugmeye basinca fotograf cekilmiyor, market degisiyordu. Cihazda goruldu.
 */
@Composable
private fun StorePill(name: String?, onTap: () -> Unit) {
    Row(
        Modifier
            .heightIn(min = Sizes.minTapTarget)
            .clip(CircleShape)
            .pressable(onTap = onTap)
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
private fun BoxScope.ConfirmCardLayer(
    card: ConfirmCard,
    stores: List<Store>,
    storeId: String?,
    saving: Boolean,
    today: String,
    onPriceChange: (String) -> Unit,
    onOpenPicker: (TagPicker) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            // KART EKRANA YAPISIK ve yalnizca UST koseleri yuvarlak. Kod onu
            // her yandan 16dp bosluklu yuzen bir kutu olarak ciziyordu.
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Flow.cardBackground)
            // KAYDIRMA VAR AMA YUKSEKLIK ZORLANMIYOR.
            //
            // Kart normalde ekrana SIGIYOR ve sigdigi kadar yer kapliyor -
            // kirpim 92dp'lik bir serit oldugu icin (bkz. [TagThumbnail]).
            // Ilk denemem kirpimi 3:2 cizmis, karti ekrandan tasirmis ve ben
            // de sabit %86 yukseklik + kaydirma ile ortmustum; kullanici
            // cihazda "geri kalanlar asagida kaliyor" diye bildirdi ve
            // haliydi - onay karti reyonda tek elle kullaniliyor, Kaydet'e
            // ulasmak icin kaydirmak istemezsin.
            //
            // `verticalScroll` yine de duruyor: erisilebilirlik yazi olceginde
            // ya da kucuk ekranda kart tasabilir ve o zaman kaydirmak, bir
            // dugmeyi ulasilmaz birakmaktan iyi. Sigdiginda hic devreye
            // girmiyor.
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // KIRPIM KARTIN BASINDA (karar 62) - "ne cektim"in tek cevabi.
        TagThumbnail(photoPath = card.photoPath)

        // DESTEKLENMEYEN ZINCIR CUMLESI kartin BASINDA, seridin YERINE
        // (karar 49). Amber serit bu halde hic cizilmiyor - cumle onun isini
        // zaten yapiyor ve iki uyari ust uste iki is varmis gibi gorunurdu.
        card.unsupportedChainMessage()?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Flow.cancel,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            )
        }

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

        // UC SATIR UC SECICI ACIYOR - satir ici duzenleyici ve cip seridi
        // KALKTI. Sebep tasarimin kendi akisi: urun kimligi katalogdan gelmek
        // zorunda (karar 51), marka klavyesiz bir cip sheet'i (karar 52),
        // market ise arama alanli bir liste (karar 40). Uc is de kartin
        // icindeki 56dp'lik satira sigmiyordu.
        CardRow(
            label = "Ürün",
            value = card.productName.ifBlank { "—" },
            reading = card.reading,
            onTap = { onOpenPicker(TagPicker.PRODUCT) },
        )

        // MARKA SATIRI HER ZAMAN VAR. Once yalnizca OCR bir marka onerdiyse
        // ciziliyordu ve bu kisir dongunun ta kendisiydi: markayi degistirmek
        // icin once dogru okunmus olmasi gerekiyordu.
        CardRow(
            label = "Marka",
            value = card.brand ?: "—",
            reading = card.reading,
            dashed = card.brand != null,
            onTap = { onOpenPicker(TagPicker.BRAND) },
        )

        CardRow(
            label = "Market",
            value = stores.firstOrNull { it.id == storeId }?.name ?: "—",
            reading = false,
            store = true,
            onTap = { onOpenPicker(TagPicker.STORE) },
        )

        // TARIH DOKUNULAMAZ ve chevron TASIMIYOR - etikette basili tarih yok,
        // "simdi" tek dogru cevap (`PriceObservation.observedAt`).
        CardRow(label = "Tarih", value = today, reading = false, plain = true)

        val haptics = LocalHapticFeedback.current
        SaveButton(
            enabled = card.canSave && !saving,
            saving = saving,
            onSave = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onSave()
            },
        )

        // VAZGEC GERI GELDI. Kaldirmistim cunku maket yalnizca Kaydet
        // ciziyordu; yanlis olan yari maketmis - sozlesme onu baştan beri iki
        // yerde saydigi icin tasarim maketi duzeltti (karar C1).
        Box(
            Modifier
                .fillMaxWidth()
                .height(Sizes.minTapTarget)
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
 *
 * ## KLAVYEYI KENDILIGINDEN ACAN TEK ALAN
 *
 * Sozlesmenin "Ilk odak" kurali *"Hicbir ekran acilirken klavye acmaz. Tek
 * istisna: fiyati okunamamis onay karti."* diyor; kart kurali ayni seyi ters
 * yonden tekrarliyor: *"Alan dolu gelirse dokunusla duzeltilir, bos gelirse
 * klavye kendiliginden acilir."* Kodda hicbir [FocusRequester] yoktu, yani
 * fiyat okunamadiginda kullanici amber seridi okuyup ayrica alana dokunmak
 * zorundaydi - tek elle reyonda calisirken bir dokunus fazla.
 *
 * Odak alanin ILK bilesiminde isteniyor ve o an okumanin bittigi an: OCR
 * kosarken bu bilesen hic cizilmiyor, yerinde iskelet var (bkz. cagiran).
 * Kosul `card.priceText` ile canli baglansaydi, kullanici yazdigini silip alani
 * bosalttiginda klavye kendini yeniden zorlardi - oysa o an odak zaten onda.
 */
@Composable
private fun PriceField(card: ConfirmCard, onPriceChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    // ACILIS HALI SAKLANIYOR: sart "su an bos" degil, "BOS ACILDI".
    val openedEmpty = remember { card.priceText.isBlank() }
    LaunchedEffect(Unit) {
        if (openedEmpty) focusRequester.requestFocus()
    }
    BasicTextField(
        value = card.priceText,
        onValueChange = onPriceChange,
        modifier = Modifier.focusRequester(focusRequester),
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

/** Ortucu flasinin suresi (karar 55). */
private const val FLASH_MS = 120

/** Kart satiri - tasarimin `min-height:56px`i. */
private val ROW_HEIGHT = 56.dp

@PreviewLightDark
@Composable
private fun TagCaptureCameraPreview() = NeydiPreview {
    TagCaptureScreen(
        state = TagCaptureState(stores = previewStores(), storeId = "s1"),
        cameraReady = true,
        cameraDenied = false, cameraPermanentlyDenied = false, flashOn = false,
        onShutter = {}, onSelectStore = {}, onPriceChange = {},
        onSave = {}, onDismissCard = {}, onToastShown = {}, onFailureShown = {}, onBack = {},
        onToggleFlash = {}, onOpenSettings = {},
        onOpenPicker = {}, onClosePicker = {}, onPickProduct = {}, onSearchProducts = {},
        onPickBrand = {}, onSearchStores = {}, onProposeStore = {}, onConfirmNewStore = {},
        onDeleteStore = {},
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
        cameraReady = true, cameraDenied = false, cameraPermanentlyDenied = false, flashOn = false,
        onShutter = {}, onSelectStore = {}, onPriceChange = {},
        onSave = {}, onDismissCard = {}, onToastShown = {}, onFailureShown = {}, onBack = {},
        onToggleFlash = {}, onOpenSettings = {},
        onOpenPicker = {}, onClosePicker = {}, onPickProduct = {}, onSearchProducts = {},
        onPickBrand = {}, onSearchStores = {}, onProposeStore = {}, onConfirmNewStore = {},
        onDeleteStore = {},
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
        cameraReady = true, cameraDenied = false, cameraPermanentlyDenied = false, flashOn = false,
        onShutter = {}, onSelectStore = {}, onPriceChange = {},
        onSave = {}, onDismissCard = {}, onToastShown = {}, onFailureShown = {}, onBack = {},
        onToggleFlash = {}, onOpenSettings = {},
        onOpenPicker = {}, onClosePicker = {}, onPickProduct = {}, onSearchProducts = {},
        onPickBrand = {}, onSearchStores = {}, onProposeStore = {}, onConfirmNewStore = {},
        onDeleteStore = {},
    )
}

private fun previewStores(): List<Store> = listOf(
    Store(id = "s1", householdId = "h", name = "BİM", chain = "bim", createdAt = 0),
    Store(id = "s2", householdId = "h", name = "Migros", chain = "migros", createdAt = 0),
)
