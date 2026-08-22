package com.neydi.app.ui.capture

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
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
 * Seridin yuksekligi.
 *
 * ## Maket 92px diyor, cihaz 128 gosteriyor
 *
 * Maketin sayisi 92px ve ilk hali oydu. Ama o maket kirpimi bir YER TUTUCUYLA
 * cizmis - kutunun icinde *"etiketin kirpilmis goruntusu · gercek fotograf
 * bekleniyor"* yaziyor - yani 92px'in gercek bir etiketi gosterip
 * gostermedigi hic sinanmamis.
 *
 * Sinandi: tam genislikte 92dp yaklasik 4:1 bir bant demek, oysa kadraj
 * rehberi 3:2. [ContentScale.Crop] merkezden aldigi icin etiketin ust ve alt
 * kenarlari kesiliyordu; kullanici bunu *"ustten kesiyor gibi"* diye bildirdi.
 *
 * 128dp yaklasik 3:1 - hala rehberin 1,5:1'inden uzak ama kesilen pay yariya
 * iniyor ve kart ekrana sigmaya devam ediyor. Rehberin oranini birebir vermek
 * 379dp genislikte ~250dp ederdi; o, karti ekrandan tasiran ilk denemenin ta
 * kendisiydi.
 *
 * KALICI COZUM BU DEGIL: dogrusu kucuk kopyayi cekerken REHBERIN BOLGESINDEN
 * kirpmak - o zaman serit ne kadar kisa olursa olsun gosterdigi sey tam olarak
 * kullanicinin kadraja oturttugu sey olur. Bunun icin rehberin karedeki
 * karsiligini hesaplamak gerekiyor (`PreviewView` FILL_CENTER) ve ayri bir is.
 */
private val STRIP_HEIGHT = 128.dp

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
 * ## 92dp SERIT, 3:2 BLOK DEGIL
 *
 * Once tam genislikte 3:2 ciziyordum - kadraj rehberiyle ayni oran olsun diye.
 * Yanlisti ve kullanici cihazda bildirdi: 3:2 serit 239dp yuksekligindeydi ve
 * karti ekrandan tasirip Kaydet ile Vazgec'i asagida birakiyordu.
 *
 * Tasarim bunu zaten cevaplamis - maket `height:92px` diyor. Ustelik karar
 * 62'nin gerekcesi de ayni seyi soyluyor: iki mekanizmayi (arkada donmus kare
 * + kirpim) teke indirmesinin sebebi *"kart yuksekligine mal olmasi"*ydi ve
 * kirpimin isi *"alanlarin YANINDA"* durup gozle birlikte okunmakti. Kartin
 * yarisini yiyen bir blok o isi yapmiyor.
 *
 * [ContentScale.Crop] serit icinde merkez kirpim veriyor: gorulen sey karenin
 * ortasi, yani kadraj rehberinin ortasi.
 *
 * ## KUCULTULMUS bir kopya cozuluyor, ham kare DEGIL
 *
 * Ham kare 12MP; onu `ImageBitmap`e cevirmek ~48MB ayirmak demek - hem de tam
 * kullanici arka arkaya cekim yaparken. [downscaleForOcr] zaten elimizde ve
 * ayni isi yapiyor; kucuk kopya `.thumb` uzantisiyla ham karenin yanina
 * yaziliyor ve onunla birlikte siliniyor ([deleteCapture]).
 *
 * @param collapsed KLAVYE ACIKKEN serit toplaniyor - OLCULMUS bir zorunluluk.
 *
 * Cihazda olculdu: kart icerigi 1669 px, sayisal klavyenin ustunde kalan alan
 * 1198 px. Serit (336 px + 12dp bosluk = 369 px) toplanmazsa Kaydet ile Vazgec
 * klavyenin ALTINDA kaliyor ve gezinme sozlesmesinin *"Kaydet pasif; ilk
 * rakamda etkinlesir"* cumlesinin gozlemlenecek bir karsiligi kalmiyor.
 *
 * Toplandiginda market secili kartta icerik 1148 px'e iniyor, yani HIC
 * KAYDIRMA GEREKMIYOR - her sey ayni anda gorunuyor.
 *
 * Kirpimin isi karar 62'de *"ne cektim"* dogrulamasi ve o is kart ACILIRKEN
 * yapiliyor; kullanici fiyati yazmaya basladiginda dogrulama bitmis oluyor.
 * Klavye kapaninca serit geri geliyor - bitmap COZULMUS halde duruyor, yeniden
 * okunmuyor.
 *
 * ⚠ Tasarim dikey duzende klavye-kart iliskisini YAZMAMIS (yalnizca yatay icin
 * *"klavye kartı asla örtmez"* diyor). Bu davranis olcumden turetildi ve
 * `docs/25`te tasarima soruldu.
 */
@Composable
internal fun TagThumbnail(
    photoPath: String,
    modifier: Modifier = Modifier,
    collapsed: Boolean = false,
) {
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

    // YUKSEKLIK ANIMASYONLU: serit aniden kaybolursa kartin tamami zipliyor ve
    // kullanici neyin degistigini goremiyor. Sure kart acilisiyla ayni (260 ms,
    // hareket sozlesmesi) - iki hareket ayni dilden konusuyor.
    val height by animateDpAsState(if (collapsed) 0.dp else STRIP_HEIGHT, tween(CARD_MS))
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(Flow.chipBackground)
            .border(1.dp, Flow.chipBorder, RoundedCornerShape(14.dp)),
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

/** Serit toplanma/acilma suresi - kart acilisiyla ayni (hareket sozlesmesi). */
private const val CARD_MS = 260
