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
import com.neydi.app.data.image.GuideBox
import com.neydi.app.data.image.cropToGuide
import com.neydi.app.data.image.downscaleForOcr
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

/** Kirpimin uzun kenari, piksel. */
private const val THUMB_EDGE = 720

/**
 * Seridin yuksekligi - MAKETIN SAYISI, ve artik turetilmis.
 *
 * ## 92 -> 128 -> 92: sayinin degil KAYNAGIN hikayesi
 *
 * Maket bastan beri `height:92px` diyordu. Ilk hali oydu ve cihazda kotu
 * cikti: kullanici *"ustten kesiyor gibi"* diye bildirdi. Sebep yukseklik
 * degildi - kirpim TAM KAREDEN MERKEZ kirpimla aliniyordu, yani serit
 * kullanicinin kadraja oturttugu seyi degil karenin ortasini gosteriyordu.
 * 128dp'ye cikarmak kesileni yariya indirdi ama yanlis kaynagi duzeltmedi;
 * ustelik kartin dikey butcesinden caliyordu (karar 70'in sebebi).
 *
 * Karar 74 asil duzeltmeyi onayladi: kirpim [cropToGuide] ile rehberin
 * bolgesinden aliniyor. Kaynak dogru olunca serit maketin sayisina donebilir -
 * *"92dp'nin isi taninabilirlik, belgeleme degil"*.
 *
 * Sozlesme oran degil KAYNAK: seridin gosterdigi piksel, kullanicinin kadraja
 * oturttugu pikseldir. Bant 3:1, rehber 3:2 - fark artik zararsiz, cunku
 * [ContentScale.Crop] dogru bolgenin ortasini aliyor ve fiyat etiketin
 * merkezinde.
 *
 * ⚠ iOS'ta [cropToGuide] bugun her zaman `false` donuyor (Faz 9), yani orada
 * serit hala merkez kirpimi ve 92dp'de daha cok kesiyor. Kabul edildi: iOS
 * kabugu henuz yok.
 */
private val STRIP_HEIGHT = 92.dp

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
    guide: GuideBox? = null,
) {
    val image by produceState<ImageBitmap?>(initialValue = null, photoPath) {
        value = runCatching {
            val thumbPath = thumbPathOf(photoPath)
            val source = PlatformFile(photoPath).readBytes()
            // REHBER BOLGESINDEN KIRPIM, MERKEZDEN DEGIL (karar 74).
            //
            // Kirpim basarisiz olursa merkez kirpimina DUSULUYOR: yanlis
            // yerden dogru bir serit, hic serit olmamasindan iyi. iOS'ta
            // `cropToGuide` bugun her zaman false donuyor (Faz 9).
            val cropped = guide != null && cropToGuide(source, thumbPath, guide, THUMB_EDGE)
            // KUCULTME BASARISIZ OLURSA KIRPIM CIZILMIYOR - ham kareyi cozmeye
            // kalkmaktansa yer bos kalsin. Kirpim bir dogrulama, bir vaat degil.
            if (!cropped && !downscaleForOcr(source, thumbPath, THUMB_EDGE)) return@runCatching null
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
