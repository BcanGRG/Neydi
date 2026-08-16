package com.neydi.app.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import kotlinx.coroutines.launch

/**
 * Fis cekme ekrani (Ekran 4 · Kamera).
 *
 * NEDEN UYGULAMA ICI KAMERA: sistem kamerasi uc seyi birden veremiyordu.
 * (1) Cerceve rehberi cizilemiyor - sistem yuzeyinin uzerine overlay konamaz.
 * (2) Kare sayaci yok; kullanici kac fotograf ekledigini bilmiyordu ve
 *     cihazda alti karenin dordunun okunamadigi ancak Gecmis'te gorunuyordu.
 * (3) Arka kamera secimi TAVSIYE niteliginde: `FileKitCameraFacing.Back`
 *     veriliyordu ve cihaz yok sayiyordu.
 *
 * TASARIMIN KENDISI ZATEN BUNU TARIF EDIYORDU: altta "1. kare" ve "Bitti"
 * duruyor, yani ekrandan cikmadan arka arkaya kare cekmek. Sistem kamerasina
 * devredince hem tasarim hem akis kayboldu - her parca icin uygulamadan cikip
 * geri donmek gerekiyordu.
 */
@Composable
fun CaptureScreen(
    partCount: Int,
    lastFailed: Boolean,
    onCapture: suspend (CaptureController) -> Boolean,
    onDone: () -> Unit,
    onCancel: () -> Unit,
) {
    val controller = rememberCaptureController()
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    // KAMERA EKRANI HER ZAMAN KOYU, temadan bagimsiz. Onizleme goruntusunun
    // uzerine oturan chrome'un isik modunda krem olmasi hem goruntuyu
    // yikardi hem de tasarimin kendi cercevesi (#221A14) koyu.
    Surface(Modifier.fillMaxSize(), color = CAMERA_SCRIM) {
        Box(Modifier.fillMaxSize()) {
            CameraSurface(controller, Modifier.fillMaxSize())

            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Header(onCancel)

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = Spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                    ) {
                        FrameGuide()
                        Text(
                            // TEK CEKIM ARTIK VARSAYILAN (F4.17). Tasarimin
                            // metni "Uzunsa 2 kare çek" diyordu; fotograf
                            // artik iceride seritlere bolunup her serit kendi
                            // cozunurlugunde okundugu icin ikinci kare
                            // ISTENMIYOR - yalnizca okuma yetmediginde
                            // oneriliyor. Sapma tasarima bulgu olarak yazildi.
                            text = when {
                                controller.denied ->
                                    "Kamera izni yok. Fiş çekmek için izin vermen gerekiyor."
                                lastFailed ->
                                    "Bu kare okunamadı. Fişi düz ser, ışık alsın ve " +
                                        "tamamı kadraja girsin."
                                partCount > 0 ->
                                    "Okunmayan yer kaldıysa o bölümü ayrıca çekebilirsin — " +
                                        "üst üste binmesi sorun değil."
                                else -> "Fişin tamamı kadraja girsin."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = CAMERA_ON_SCRIM,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(280.dp),
                        )
                    }
                }

                BottomBar(
                    partCount = partCount,
                    enabled = controller.ready && !busy,
                    onShutter = {
                        busy = true
                        scope.launch {
                            onCapture(controller)
                            busy = false
                        }
                    },
                    onDone = onDone,
                )
            }
        }
    }
}

/** `close` · "Fiş çek" · flas - tasarimin 56dp'lik ust seridi. */
@Composable
private fun Header(onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(NeydiExtraShapes.pill)
                .pressable(onTap = onCancel)
                .size(Sizes.minTapTarget),
            contentAlignment = Alignment.Center,
        ) {
            NeydiIcon(
                icon = NeydiIcons.Close,
                contentDescription = "Kapat",
                tint = CAMERA_ON_SCRIM,
            )
        }
        Text(
            text = "Fiş çek",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = CAMERA_ON_SCRIM,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        // Flas dugmesinin YERI ayrilmis ama kendisi CIZILMEMIS: acip kapatan
        // bir kod yok ve hicbir sey yapmayan buton cizmek bu projede yasak
        // (tasarim karari 3). Bosluk basligi ortada tutuyor.
        Spacer(Modifier.size(Sizes.minTapTarget))
    }
}

/**
 * Amber koseli cerceve rehberi.
 *
 * DORT KOSE, TAM CERCEVE DEGIL: tasarim ince beyaz bir dikdortgen ve uzerinde
 * amber koseler ciziyor. Koseler gozun hizalayacagi sey; kapali bir cerceve
 * goruntunun kendisiyle yarisir.
 */
@Composable
private fun FrameGuide() {
    val accent = LocalNeydiExtraColors.current.accent
    Box(
        modifier = Modifier
            // CERCEVE FISIN ORANINDA, KARE DEGIL (F4.18).
            //
            // Once 440dp yuksekliginde, TAM GENISLIKTE bir kutuydu - yani
            // yaklasik 1:1,2. Fis ise 1:4 ile 1:12 arasinda ince uzun bir
            // serit. Kullanici fisi o kutuya hizalayinca kare ortasinda ince
            // bir cizgi olarak kaliyor ve sensorun genisliginin buyuk kismi
            // ARKA PLANA gidiyordu. "Tek kareye sigmiyor" sikayetinin bir
            // parcasi tam olarak buydu: sigmiyor degil, kadraji doldurmuyordu.
            //
            // Dar ve uzun cerceve fisi kadraja OTURTUYOR; ayni sensorden satir
            // basina cok daha fazla piksel dusuyor.
            .fillMaxHeight()
            .aspectRatio(RECEIPT_ASPECT)
            .border(2.dp, CAMERA_ON_SCRIM.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
            .padding(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Corner(accent, top = true, start = true)
                Corner(accent, top = true, start = false)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Corner(accent, top = false, start = true)
                Corner(accent, top = false, start = false)
            }
        }
    }
}

/**
 * Tek kose: IKI KENARI cizili 28dp'lik alan - dolu kare DEGIL.
 *
 * Olculer acikca veriliyor (`size(28.dp, 3.dp)` ve `size(3.dp, 28.dp)`);
 * ilk halinde `fillMaxWidth`/`fillMaxSize` ile yazilmisti ve cihazda ikisi de
 * kareyi tamamen dolduruyordu - kose isareti yerine amber blok cikti.
 */
@Composable
private fun Corner(color: Color, top: Boolean, start: Boolean) {
    Box(Modifier.size(28.dp)) {
        Box(
            Modifier
                .size(width = 28.dp, height = 3.dp)
                .align(if (top) Alignment.TopStart else Alignment.BottomStart)
                .background(color),
        )
        Box(
            Modifier
                .size(width = 3.dp, height = 28.dp)
                .align(if (start) Alignment.TopStart else Alignment.TopEnd)
                .background(color),
        )
    }
}

/**
 * "N. kare" · deklansor · "Bitti".
 *
 * SAYAC SOLDA VE HER ZAMAN GORUNUR - bu ekranin var olma sebeplerinden biri:
 * kullanici kac kare cektigini bilmiyordu.
 */
@Composable
private fun BottomBar(
    partCount: Int,
    enabled: Boolean,
    onShutter: () -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .padding(bottom = Spacing.lg, top = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${partCount + 1}. kare",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = CAMERA_ON_SCRIM.copy(alpha = 0.75f),
            modifier = Modifier.width(72.dp),
        )
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(NeydiExtraShapes.pill)
                    .pressable(enabled = enabled, onTap = onShutter)
                    .background(if (enabled) CAMERA_ON_SCRIM else CAMERA_ON_SCRIM.copy(alpha = 0.4f))
                    .padding(4.dp),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(NeydiExtraShapes.pill)
                        .background(CAMERA_SCRIM),
                )
            }
        }
        // "BITTI" ANCAK BIR KARE VARKEN: sifir kareyle bitirmek Geziye bos bir
        // fis baglamak ya da hicbir sey yapmamak olurdu; ikisi de kullaniciya
        // yalan soyler. Kare yokken cikis yolu basliktaki `close`.
        Box(
            modifier = Modifier
                .width(72.dp)
                .clip(NeydiExtraShapes.pill)
                .pressable(enabled = partCount > 0, onTap = onDone)
                .heightIn(min = Sizes.minTapTarget),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = "Bitti",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (partCount > 0) CAMERA_ON_SCRIM else CAMERA_ON_SCRIM.copy(alpha = 0.4f),
            )
        }
    }
}

/**
 * Cerceve rehberinin en-boy orani.
 *
 * Turk market fisi ~8cm genisliginde; uzunlugu alisverise gore 25-100cm. 0,32
 * orta boy bir fisi (~25cm) kadraja oturtuyor ve daha uzun fiste kullaniciyi
 * geriye cekilmeye degil, fisi katlamaya ya da ikinci kare cekmeye birakiyor -
 * cerceveyi daha da inceltmek kisa fiste kadrajin buyuk kismini bosa
 * harcardi.
 */
private const val RECEIPT_ASPECT = 0.32f

/** Tasarimin kamera zemini (#221A14) ve uzerindeki metin rengi (#F5EDE6). */
private val CAMERA_SCRIM = Color(0xFF221A14)
private val CAMERA_ON_SCRIM = Color(0xFFF5EDE6)

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun CaptureFirstFramePreview() = NeydiPreview {
    CaptureScreen(
        partCount = 0,
        lastFailed = false,
        onCapture = { true },
        onDone = {},
        onCancel = {},
    )
}

/** Ikinci kare: sayac ilerledi, "Bitti" aktif, metin bindirmeyi istiyor. */
@PreviewLightDark
@Composable
private fun CaptureSecondFramePreview() = NeydiPreview {
    CaptureScreen(
        partCount = 1,
        lastFailed = false,
        onCapture = { true },
        onDone = {},
        onCancel = {},
    )
}

/** Son kare okunamadi: rehber metni ne yapilacagini soyluyor. */
@PreviewLightDark
@Composable
private fun CaptureAfterFailurePreview() = NeydiPreview {
    CaptureScreen(
        partCount = 2,
        lastFailed = true,
        onCapture = { true },
        onDone = {},
        onCancel = {},
    )
}
