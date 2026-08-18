package com.neydi.app.ui.capture

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.neydi.app.data.image.deleteFileAt
import com.neydi.app.data.image.downscaleForOcr
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

/** Kirpimin uzun kenari, piksel. */
private const val THUMB_EDGE = 720

/**
 * Cekilen karenin kartin BASINDAKI kirpimi (karar 62).
 *
 * ## Bu kirpim "ne cektim"in TEK cevabi
 *
 * Sozlesme bir zamanlar iki mekanizma birden yaziyordu: arkadaki kare
 * *"donar, karartilmaz"* VE kirpim kartin basinda. Karar 62 ikisinin ayni isi
 * iki kez yaptigini soyleyip birini secti - dogrulama KIRPIMDA, arkadaki kare
 * kararabilir. Gerekcesi olcu: iki mekanizma kart yuksekligine mal oluyordu ve
 * kirpim alanlarin YANINDA oldugu icin goz ikisini birlikte okuyor.
 *
 * ## 3:2 ORAN kadraj rehberiyle AYNI
 *
 * Rehber 3:2 ciziyor ve kullanici etiketi ona hizaliyor; kirpim baska bir oran
 * gosterseydi "kadraja oturttugum sey bu mu" sorusu cevapsiz kalirdi.
 * [ContentScale.Crop] ile merkez kirpim, rehberin gosterdigi bolgeyi veriyor.
 *
 * ## KUCULTULMUS bir kopya cozuluyor, ham kare DEGIL
 *
 * Ham kare 12MP; onu `ImageBitmap`e cevirmek ~48MB ayirmak demek - hem de tam
 * kullanici arka arkaya cekim yaparken. [downscaleForOcr] zaten elimizde ve
 * ayni isi yapiyor; kucuk kopya `.thumb` uzantisiyla ham karenin yanina
 * yaziliyor ve onunla birlikte siliniyor ([deleteCapture]).
 */
@Composable
internal fun TagThumbnail(photoPath: String, modifier: Modifier = Modifier) {
    val image by produceState<ImageBitmap?>(initialValue = null, photoPath) {
        value = runCatching {
            val thumbPath = thumbPathOf(photoPath)
            val source = PlatformFile(photoPath).readBytes()
            // KUCULTME BASARISIZ OLURSA KIRPIM CIZILMIYOR - ham kareyi cozmeye
            // kalkmaktansa yer bos kalsin. Kirpim bir dogrulama, bir vaat degil.
            if (!downscaleForOcr(source, thumbPath, THUMB_EDGE)) return@runCatching null
            PlatformFile(thumbPath).readBytes().decodeToImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .clip(RoundedCornerShape(14.dp))
            .background(Flow.chipBackground),
    ) {
        image?.let {
            Image(
                bitmap = it,
                contentDescription = "çekilen etiket",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Ham karenin yanindaki kucuk kopya. */
internal fun thumbPathOf(photoPath: String): String = "$photoPath.thumb"

/**
 * Ham kareyi VE kirpimini siler.
 *
 * Karar 29 fotografi saklamiyor ve bu artik IKI dosya demek. Yalnizca hamini
 * silmek, "fotograf saklanmiyor" sozunu yariya indirirdi - kucuk kopya da
 * etiketin okunabilir bir goruntusu.
 */
internal suspend fun deleteCapture(photoPath: String) {
    deleteFileAt(photoPath)
    deleteFileAt(thumbPathOf(photoPath))
}
