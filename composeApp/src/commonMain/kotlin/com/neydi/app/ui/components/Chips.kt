package com.neydi.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import com.neydi.app.ui.theme.Motion
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.LocalNeydiTextStyles
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/**
 * Liste satirindaki fiyat cipi. KENDI 44dp DOKUNMA HEDEFI var - satirin geri kalani
 * isaretlemek icin, bu cip fiyat gecmisini acmak icin. Reyonda tek elle basilabilmeli.
 *
 * Sabit 92dp genislik + saga dayali: tabular figures (tnum) Skia'da sessizce
 * yok sayilabildigi icin duzen yalnizca tnum'a guvenmiyor. Rakamlar hizalanmazsa
 * bile sutun hizali kalir.
 */
/**
 * Delta cipindeki okun boyu.
 *
 * 12sp'lik `labelSmall` metnin yanina oturuyor. Tasarim bu olcuyu dp olarak
 * YAZMIYOR ("12sp metinle birlikte" diyor) - deger cihazda gozle ayarlandi,
 * turetilmedi.
 */
private val DELTA_ARROW = 12.dp

/**
 * [DeltaChip]'in metin DISINDAKI genisligi: iki yanda 6dp dolgu + 2dp bosluk
 * + ok.
 *
 * ## Neden disariya aciliyor
 *
 * Karar 104 deltayi ekonomi bandinin tek feda edilebilir ogesi yapti ve
 * kararin olcutu *"cumle tam kalir"*: cipin metayi kirpip kirpmayacagi
 * CIZILMEDEN once bilinmek zorunda. Bilmenin tek yolu genisligini hesaplamak,
 * ve hesap cipin kendi dolgulariyla ayni dosyada durmali - ayri dursaydi
 * dolgu degistiginde sessizce yanlislasirdi.
 */
val DELTA_CHIP_CHROME: Dp = 7.dp + DELTA_ARROW + 2.dp + 7.dp

/** Fiyat cipinin GORSEL yuksekligi (karar 105) - dokunma hedefi degil. */
private val PRICE_CHIP_VISUAL = 26.dp

@Composable
fun PriceChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val styles = LocalNeydiTextStyles.current
    // CIP GERCEKTEN BIR CIP: dolgulu hap, ciplak metin degil.
    //
    // Adi "fiyat CIPI" ama kod yalnizca bir `Text` ciziyordu - ne hap sekli,
    // ne dolgu. Tasarim sistemi onu `height:32px; padding:0 12px;
    // border-radius:999px; background:#F1E7DB` diye veriyor ve DORT halini
    // ayri ayri cizmis; hicbiri ciplak metin degil. Metin rengi de
    // `onSurfaceVariant` yerine `onSurface`: cip artik kendi zeminini
    // tasidigi icin metnin soluk olmasi gerekmiyor.
    // 92dp SABIT SUTUN, SAGA DAYALI (karar 81).
    //
    // `SizesExtra.priceColumn` OLU BIR SABITTI: tasarim sistemi ve alti maket
    // kullanimi 92dp diyordu, Compose Spec `Modifier.width(92.dp) +
    // TextAlign.End` diye yaziyordu, kodda ise hicbir yerde geÃ§miyordu.
    // Sonuc: dort haneli fiyat 99,38dp'ye tasip fazlasini addan caliyordu.
    //
    // YUKSEKLIK ARTIK SATIRDAN (karar 85): cipin kendi 48dp hedefi fiyatli
    // her satiri 56dp yerine 64dp yapiyordu ve "10-11 satir gorunur" hedefini
    // dokuza dusuruyordu. Hedef bir GORSEL BOYUT degil bir isabet sozlesmesi;
    // satir zaten 56dp isabet veriyor.
    // ⚠ DOKUNMA HEDEFI BUGUN 92dp x 26dp - 48dp DEGIL (acik madde).
    //
    // Karar 105 *"gorsel 26dp, hedef 48dp, satirin alt yarisina yayilir, cip
    // gorselini buyutmez"* diyor ve maketin kendi geometrisinde bu UCU BIRDEN
    // saglanamiyor: satir 72dp, kimlik bandi 24dp, aradaki bosluk 5dp; ekonomi
    // bandina 26dp kaliyor ve cip o bandin icinde duruyor (maket raptiyeyi
    // cible ayni dikeyde ciziyor - disari alinca kimlik bandi 92dp daraliyor
    // ve raptiye satirin ortasinda asili kaliyor).
    //
    // Iki standart yol da cihazda denendi ve ikisi de basarisiz:
    //   - Olcum/yerlesim ayirmak (dugumu 48dp olcup 26dp bildirmek): Compose'un
    //     isabet testi ust dugumun BILDIRDIGI boyutu kullaniyor, dolayisiyla
    //     gorselin disina dokunmak satiri isaretledi.
    //   - `minimumInteractiveComponentSize()`: bu surumde YERLESIMI buyutuyor,
    //     yani satir 72dp'yi asti.
    //
    // Bugunku hedef yatayda comert (92dp), dikeyde 26dp. Tasarima soruldu:
    // satir 80dp'ye mi cikmali, yoksa cip kimlik bandiyla ayni katta mi
    // durmali. Cevap gelene kadar GORUNUM maketle birebir, hedef eksik.
    val base = modifier.width(SizesExtra.priceColumn)
    Box(
        modifier = if (onClick != null) base.pressable(onTap = onClick) else base,
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            Modifier
                // GORSEL 26dp (karar 105) - dokunma hedegi degil.
                //
                // Dikey dolguyla (5dp) yaklasik 30dp cikiyordu ve iki bantli
                // satirda bu fark bandi tasiriyordu. Hedef zaten disaridaki
                // kutudan geliyor: 92dp x satir yuksekligi.
                .height(PRICE_CHIP_VISUAL)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = styles.priceChip,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
            )
        }
    }
}

/**
 * Fiyat degisim yuzdesi: "%9". Ok anlamsaldir - kirmizi yukari, yesil asagi.
 * Renk tek basina anlam tasimaz; ok isareti renk gormeyen kullanici icin de calisir.
 */
@Composable
fun DeltaChip(
    percent: Int,
    rising: Boolean,
    modifier: Modifier = Modifier,
) {
    val extras = LocalNeydiExtraColors.current
    val color = if (rising) extras.priceUp else extras.priceDown
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            // 22dp / yanlarda 7dp - maketin olcusu. Dikey dolgu YOK: yukseklik
            // sabit olunca cip, yanindaki 26dp'lik fiyat hapiyla ayni optik
            // ritmi tutuyor.
            .height(22.dp)
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // IKON, UNICODE GLIFI DEGIL. Once `Text("â†‘")` yaziyordu ve iki sorunu
        // vardi: karar 32 *"ikonlar `Text` olarak cizilmiyor"* diyor, ve glif
        // sistem fontundan cozuldugu icin Skia'nin yedek zinciri Android ile
        // iOS'ta ayni sekli vermiyordu - kalinligi da yanindaki 12sp metinle
        // eslesmiyordu.
        //
        // `tint = color` cipin kendi rengini tasiyor (priceUp / priceDown),
        // yani Ikonografi'nin *"ikon icinde bulundugu metnin rengini alir"*
        // kurali korunuyor.
        NeydiIcon(
            icon = if (rising) NeydiIcons.ArrowUpward else NeydiIcons.ArrowDownward,
            contentDescription = null,
            size = DELTA_ARROW,
            tint = color,
        )
        Text(
            text = "%$percent",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

/**
 * Oneri seridi cipi. GEREKCE CIPIN ICINDE: "Yumurta Â· 14 gun oldu".
 * Gerekcesiz bir cip reklam gibi okunur; gerekceli olan hafiza yardimi gibi.
 *
 * Animasyon yok, badge yok, nokta yok - hicbir sey dikkat cekmeye calismaz.
 */
@Composable
fun SuggestionChip(
    label: String,
    reason: String,
    modifier: Modifier = Modifier,
    /**
     * Bu urun ZATEN LISTEDE mi (tasarim karari 12).
     *
     * "Bu oturumda eklendi" DEGIL, "bu listede var" demek. Ikisi cakismiyor
     * (oturumda eklenen zaten listede) ama listenin durumunu gostermek
     * sheet'i kapatip acmaya karsi dayanikli: ayni urun ikinci kez
     * isaretsiz gorunmuyor. Oturumun kendi sayaci zaten baslikta.
     */
    checked: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .heightIn(min = SizesExtra.suggestionChip)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            // SAC TELI KENARLIK: tasarim dolguyu VE 1px kenarligi birlikte
            // veriyor, dort halinde de. Kenarliksiz cipin siniri yalnizca
            // surface/surfaceVariant ton farki - o da bir ton adimi kadar.
            .border(Sizes.hairline, LocalNeydiExtraColors.current.hairline, CircleShape)
            // ISARETLI CIP PASIF: ayni satiri iki kez eklemek bir is degil,
            // ve pasiflik bunu dokunmadan once soyluyor.
            .pressable(enabled = !checked, onTap = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (checked) {
            NeydiIcon(
                icon = NeydiIcons.CheckCircle,
                contentDescription = "listede var",
                size = 18.dp,
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Â·",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = reason,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Adet rozeti: "2x", "1 kg". ADET 1 ISE CIZILMEZ - cagiran taraf null gecer.
 * Her satirda "1x" yazmak 20 satirlik bir listede yalnizca gurultudur.
 */
@Composable
fun QuantityBadge(
    text: String,
    modifier: Modifier = Modifier,
    /**
     * Miktar varsayilanindan cikarildi mi (karar 107): dolgu mu kontur mu.
     *
     * Rozet artik HER SATIRDA cizildigi icin dolgu tek basina bir sey
     * soylemez oldu - onceden zaten "1 degil" demekti. Ayrim yuzeye tasindi:
     * kontur "dokunulmamis", dolgu "bunu ben sectim".
     */
    modified: Boolean = true,
) {
    // DEGISIMDE 150 ms OLCEK VURGUSU (karar 92).
    //
    // Rozet artik her satirda cizildigi icin (karar 103) vurgu gercekten bir
    // VURGU: yerinde duran bir sayinin degistigini soyluyor. Onceden rozet
    // adet 1 iken hic cizilmiyordu ve ikinci eklemede birdenbire beliriyordu -
    // o bir sicramaydi. Ayni hareket sheet sayacinda da var (karar 89); ikisi
    // "bir sayi degisti" diyor ve ayni dili konusmalari tesadufi degil.
    val extras = LocalNeydiExtraColors.current
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(text) {
        pulse.snapTo(Motion.PULSE_SCALE)
        pulse.animateTo(1f, tween(Motion.PULSE_MS))
    }
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = pulse.value; scaleY = pulse.value }
            .defaultMinSize(minWidth = SizesExtra.qtyBadgeMinWidth)
            .heightIn(min = SizesExtra.qtyBadgeHeight)
            .clip(CircleShape)
            // DOLGU `hairline`, `surfaceVariant` DEGIL.
            //
            // Maket rozete #EADCCB veriyor - fiyat cipinin zemininden (#F1E7DB)
            // bir tik koyu, ki ayni satirdaki iki hap birbirinden ayrilsin.
            // Palette bu deger YOK; en yakin token `hairline` (#E7DACB) ve fark
            // gozle secilmiyor. Yeni bir renk eklemek, karar 101'in az once
            // sildigi renk cogalmasini geri getirirdi.
            .background(if (modified) extras.hairline else Color.Transparent)
            .border(
                width = if (modified) 0.dp else 1.5.dp,
                color = if (modified) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        // 14sp/700 - ADA RAKIP OLMASIN DIYE 20sp'DEN INDI (karar 103).
        //
        // 20sp/800 ile ciziliyordu ve rozet her satirda olmadigi surece bu
        // dogruydu: nadir gorunen bir sey dikkat cekmeliydi. Rozet her satira
        // gelince ayni agirlik satirin en buyuk seyi ile - urun adiyla -
        // yarisir oldu. Tasarimin cumlesi: *"rozet ada rakip olmasin diye
        // 17sp'den 14sp'ye indi."*
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Esin baÅŸ harfi. YALNIZCA es ekledigunde cizilir - kendi ekledigimiz satirda
 * kendi harfimizi gormek bilgi tasimaz.
 */
@Composable
fun MemberAvatar(initial: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            // 20dp (karar 103). Onceki 24dp+ hali kimlik bandinda addan yer
            // caliyordu; avatar bir BAGLAM isareti, bir kimlik degil.
            .size(20.dp)
            .clip(CircleShape)
            // DOLU YESIL, %18 DEGIL. Maketin hepsi `background:#3F6B54` +
            // `color:#fff` ciziyor. Soluk zemin uzerindeki yesil harf 20dp'de
            // okunmuyordu; dolu daire hem 20dp'de secilebiliyor hem de "bunu
            // ES ekledi" isaretini bir bakista veriyor.
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary,
        )
    }
}

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun ChipsPreview() = NeydiPreview {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeltaChip(17, rising = true)
        DeltaChip(6, rising = false)
        QuantityBadge("2x")
        QuantityBadge("1 kg")
        MemberAvatar("A")
    }
    PriceChip("455,00 TL")
    // Dort haneli fiyat: 92dp sutun butcesini zorlayan gercek senaryo.
    PriceChip("1.289,90 TL")
    SuggestionChip("Yumurta", "14 gÃ¼n oldu") {}
    SuggestionChip("Ã‡ay", "genelde 4 alÄ±ÅŸveriÅŸte bir") {}
}
