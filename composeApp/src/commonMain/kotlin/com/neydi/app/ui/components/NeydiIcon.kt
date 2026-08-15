package com.neydi.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tasarimin ikon sozlugu - TASARIMDAKI ADLARLA.
 *
 * NEDEN ARADA BIR KATMAN VAR: tasarim `more_vert`, `push_pin`, `receipt_long`
 * diyor; Compose `Icons.Rounded.MoreVert` diyor. Cagri yerlerinde tasarimin
 * adini kullanmak, bir ekrani tasarim dosyasiyla yan yana koyup okumayi
 * mumkun kiliyor - ve ikon setini degistirmek gerekirse tek dosya degisiyor.
 *
 * SET SECIMI VE SAPMA KAYDA GECSIN: tasarim **Material Symbols Rounded**
 * istiyor (weight 400, opsz 24, rest FILL 0). O font paketlenmis bir Compose
 * artifact'i olarak YAYINLANMIYOR ve ikon fontunu repoya indirmek bu ortamda
 * mumkun degildi. En yakin karsilik `Icons.Rounded.*`: ayni cizim dili (yuvarlak
 * uclar), ayni 24dp optik boyut, ayni dolgusuz varsayilan. Farklar tek tek
 * glifte birkac pikselle sinirli.
 *
 * SURUM NOTU: ikon artifact'i JetBrains tarafinda 1.7.3'ten sonra yayinlanmadi.
 * Ikonlar salt veri (`ImageVector`) oldugu icin surum farki calisma zamaninda
 * bir sey baglamiyor; derleme ve cihaz kosumu dogrulandi.
 */
object NeydiIcons {
    val MoreVert: ImageVector get() = Icons.Rounded.MoreVert
    val PushPin: ImageVector get() = Icons.Rounded.PushPin
    val Add: ImageVector get() = Icons.Rounded.Add
    val ChevronRight: ImageVector get() = Icons.Rounded.ChevronRight
    val Check: ImageVector get() = Icons.Rounded.Check
    val Close: ImageVector get() = Icons.Rounded.Close
    val ArrowBack: ImageVector get() = Icons.Rounded.ArrowBack
    val ArrowUpward: ImageVector get() = Icons.Rounded.ArrowUpward
    val ArrowDownward: ImageVector get() = Icons.Rounded.ArrowDownward
    val ExpandMore: ImageVector get() = Icons.Rounded.ExpandMore
    val Undo: ImageVector get() = Icons.Rounded.Undo
    val FilterList: ImageVector get() = Icons.Rounded.FilterList
    val ContentPaste: ImageVector get() = Icons.Rounded.ContentPaste
    val ContentCopy: ImageVector get() = Icons.Rounded.ContentCopy
    val Search: ImageVector get() = Icons.Rounded.Search
    val LightMode: ImageVector get() = Icons.Rounded.LightMode
    val DragIndicator: ImageVector get() = Icons.Rounded.DragIndicator
    val ReceiptLong: ImageVector get() = Icons.Rounded.ReceiptLong
    val HourglassTop: ImageVector get() = Icons.Rounded.HourglassTop

    /** Fis "kontrol bekliyor" hali. Dolgusuz varyant - tasarim rest FILL 0 diyor. */
    val Error: ImageVector get() = Icons.Rounded.ErrorOutline
}

/** Tasarimin varsayilan ikon boyutu (`iconography/opticalSize`). */
private val DEFAULT_ICON = 24.dp

/**
 * Ikon cizer.
 *
 * VARSAYILAN RENK `onSurfaceVariant`, `onSurface` DEGIL: tasarimda ikonlarin
 * neredeyse tamami ikincil (metadata, chevron, muted aksiyon). Vurgulu olmasi
 * gereken yerler rengi acikca veriyor.
 *
 * @param contentDescription null ise ikon DEKORATIF sayilir ve ekran okuyucu
 *   atlar. Yanindaki metin ayni seyi soyluyorsa dogrusu budur - iki kez
 *   okutmak gurultu.
 */
@Composable
fun NeydiIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = DEFAULT_ICON,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}
