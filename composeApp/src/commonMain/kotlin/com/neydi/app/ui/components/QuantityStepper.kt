package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.pressable

/**
 * Miktar sayacinin UC BOYUTU (karar 107-109).
 *
 * ## Neden tek bilesen, uc olcu
 *
 * Ayni kontrol uc yerde duruyor - satirda, Urun Detayi'nda, kesif hucresinde -
 * ve ucu de ayni SEYI yapiyor. Uc ayri bilesen yazmak, birinin adimi ya da
 * ikonu digerinden ayrilmasina davet olurdu; nitekim tasarim ucunu de ayni
 * dille ciziyor ve yalnizca boyutlari degistiriyor.
 *
 * Olculer maketten okundu (`getComputedStyle`), goz karari degil.
 */
@Immutable
data class StepperMetrics(
    val button: Dp,
    val buttonHeight: Dp,
    val icon: Dp,
    val gap: Dp,
) {
    companion object {
        /** Satirdaki hizli sayac: dugmeler 44x32, ikon 20dp. */
        val Row = StepperMetrics(button = 44.dp, buttonHeight = 32.dp, icon = 20.dp, gap = 6.dp)

        /** Urun Detayi'ndaki miktar blogu: 48x48 - tam dokunma hedefi. */
        val Detail = StepperMetrics(button = 48.dp, buttonHeight = 48.dp, icon = 22.dp, gap = 10.dp)

        /**
         * Kesif hucresindeki sayac: 26x26.
         *
         * ⚠ 48dp'nin ALTINDA ve bu tasarimin kendi olcusu: hucre 100x76 ve
         * *"izgara ritmi ve hucre boyu degismez"*. Sayac hucrenin icine
         * sigmak zorunda, o yuzden hedef burada hucrenin kendisinden geliyor -
         * kontrol yalnizca uzun dokunusla aciliyor ve acikken hucrede baska
         * dokunulacak bir sey yok.
         */
        val Cell = StepperMetrics(button = 26.dp, buttonHeight = 26.dp, icon = 15.dp, gap = 4.dp)
    }
}

/**
 * `-  deger  +`.
 *
 * DEGER BIR SLOT, cunku uc yuzeyde uc farkli sey: satirda duz metin
 * ("4 kg"), Urun Detayi'nda YAZILABILIR bir alan, hucrede kucuk bir sayi.
 * Ortak olan sey dugmeler ve aralarindaki mesafe; degerin kendisi degil.
 *
 * @param onDecrement `-` dugmesi. Alt sinir aritmetigi cagirana ait
 *   ([com.neydi.app.data.decrementQuantity]) - bilesen bir sayi bilmiyor.
 */
@Composable
fun QuantityStepper(
    metrics: StepperMetrics,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    value: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.gap),
    ) {
        StepperButton(NeydiIcons.Remove, "azalt", filled = false, metrics = metrics, onClick = onDecrement)
        value()
        StepperButton(NeydiIcons.Add, "artır", filled = true, metrics = metrics, onClick = onIncrement)
    }
}

/**
 * ARTI DOLU, EKSI KONTURLU - ve bu asimetri kasitli.
 *
 * Maketin ucunde de ayni: `+` kiremit dolgulu, `-` yalniz konturlu. Renk
 * sozlugunun kendi ayrimi bunu soyluyor (karar 42): kiremit ILERI GOTUREN is.
 * Listeye daha cok koymak ileri gitmek, azaltmak ise geri almak - ikisini ayni
 * agirlikta cizmek, kullanicinin %90 durumda istedigi yonu gizlerdi.
 */
@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    filled: Boolean,
    metrics: StepperMetrics,
    onClick: () -> Unit,
) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .size(width = metrics.button, height = metrics.buttonHeight)
            .clip(shape)
            .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            // KONTUR `outline`, maketin `#C9B9A5`'i DEGIL.
            //
            // O deger tasarim sisteminin paletinde yok; en yakin iki token
            // `hairline` (#E7DACB) ve `outline` (#8A7666). `hairline` secilirse
            // dugme pratikte GORUNMEZ olur - surface uzerinde ~1.2:1 - ve bu
            // hata onay hedefinde bir kez yapildi: dokunulacak seyin kendisi
            // gorunmuyordu. Konturun tek isi dugmenin sinirini soylemek.
            .border(
                width = if (filled) 0.dp else 1.dp,
                color = if (filled) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = shape,
            )
            .pressable(onTap = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NeydiIcon(
            icon = icon,
            contentDescription = contentDescription,
            size = metrics.icon,
            tint = if (filled) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}
