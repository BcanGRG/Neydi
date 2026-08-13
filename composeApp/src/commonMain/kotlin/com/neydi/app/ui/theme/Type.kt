package com.neydi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.neydi.app.resources.Res
import com.neydi.app.resources.fraunces_display
import com.neydi.app.resources.plusjakartasans_bold
import com.neydi.app.resources.plusjakartasans_extrabold
import com.neydi.app.resources.plusjakartasans_medium
import com.neydi.app.resources.plusjakartasans_regular
import com.neydi.app.resources.plusjakartasans_semibold
import org.jetbrains.compose.resources.Font

/**
 * Tipografi: Plus Jakarta Sans (UI + govde + tum rakamlar) + Fraunces (SADECE display).
 *
 * HEPSI STATIK INSTANCE, variable font DEGIL. Yol haritasi once PJS'i variable
 * bundle etmeyi ongoruyordu; CMP'de FontVariation.Settings iOS'ta guvenilir degil
 * ve hata vermeden varsayilan instance'a duser. Mac olmadigi icin bunu dogrulayamayiz,
 * o yuzden riski tamamen kaldirdik: her agirlik ayri bir dosya.
 *
 * Fraunces: opsz=72 SOFT=30 WONK=0 wght=600 ayarinda tek instance.
 *   - WONK acik kalsaydi harfler kasitli olarak "egri" davranirdi - istedigimiz sicaklik o degil.
 *   - SOFT=30 koseleri yumusatir; serif'i editoryal olmaktan cikarip sicak yapan sey bu.
 *   - opsz=72 buyuk punto icin; varsayilan opsz=14 kucuk punto kesimi olurdu ve 44sp'de yanlis gorunurdu.
 *
 * Uretim komutu (fontTools):
 *   fonttools varLib.instancer PlusJakartaSans[wght].ttf wght=400 -o plusjakartasans_regular.ttf
 *   fonttools varLib.instancer "Fraunces[SOFT,WONK,opsz,wght].ttf" opsz=72 SOFT=30 WONK=0 wght=600 \
 *     -o fraunces_display.ttf
 *
 * Turkce glif kapsami alti dosyada da dogrulandi: Ğğ İı Şş ÇçÖöÜü tam.
 *
 * FRAUNCES KURALI: yalnizca >= 24sp. Liste satirinda, cipte, butonda, etikette,
 * metadata satirinda ASLA. Kullanim yeri tam olarak dort tane: fiyat gecmisi mansetti,
 * alisveris sonrasi ozet tutari, kurulum baslıklari, bos durum baslıklari.
 * Serif'i yogun listeye sokarsan tasarim aninda dagilir.
 */

/** Govde minimumu 14sp. PJS'te optik boyut ekseni yok, yani kucuk punto optik olarak
 *  telafi edilmiyor; satirin ikinci satiri %60 opaklikla birlikte 13sp'de okunabilirlik
 *  sinirinin altina duser. */
const val BODY_MIN_SP = 14

@Composable
fun neydiUiFamily(): FontFamily = FontFamily(
    Font(Res.font.plusjakartasans_regular, FontWeight.Normal),
    Font(Res.font.plusjakartasans_medium, FontWeight.Medium),
    Font(Res.font.plusjakartasans_semibold, FontWeight.SemiBold),
    Font(Res.font.plusjakartasans_bold, FontWeight.Bold),
    Font(Res.font.plusjakartasans_extrabold, FontWeight.ExtraBold),
)

@Composable
fun neydiDisplayFamily(): FontFamily = FontFamily(
    Font(Res.font.fraunces_display, FontWeight.SemiBold),
)

@Composable
fun rememberNeydiTypography(): Typography {
    val ui = neydiUiFamily()
    val display = neydiDisplayFamily()
    return remember(ui, display) {
        Typography(
            // display -> Fraunces 600
            displayLarge = TextStyle(
                fontFamily = display, fontWeight = FontWeight.SemiBold,
                fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-0.44).sp,
            ),
            displayMedium = TextStyle(
                fontFamily = display, fontWeight = FontWeight.SemiBold,
                fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.36).sp,
            ),
            displaySmall = TextStyle(
                fontFamily = display, fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp, lineHeight = 33.sp, letterSpacing = (-0.30).sp,
            ),

            // headline -> Fraunces 600, alt sinir 24sp
            headlineLarge = TextStyle(
                fontFamily = display, fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp, lineHeight = 34.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = display, fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp, lineHeight = 29.sp,
            ),
            // headlineSmall 22sp -> 24sp esiginin ALTINDA, o yuzden Fraunces DEGIL. Kasitli.
            headlineSmall = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Bold,
                fontSize = 22.sp, lineHeight = 28.sp,
            ),

            // title -> Plus Jakarta Sans 600/700
            titleLarge = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Bold,
                fontSize = 22.sp, lineHeight = 28.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp, lineHeight = 23.sp,
            ),
            titleSmall = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp, lineHeight = 21.sp,
            ),

            // body -> Plus Jakarta Sans 400/500
            bodyLarge = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Medium,
                fontSize = 17.sp, lineHeight = 25.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Normal,
                fontSize = 15.sp, lineHeight = 22.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Normal,
                fontSize = BODY_MIN_SP.sp, lineHeight = 20.sp,
            ),

            // label -> Plus Jakarta Sans 500/600
            labelLarge = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp, lineHeight = 20.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Medium,
                fontSize = 14.sp, lineHeight = 18.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Medium,
                fontSize = 12.sp, lineHeight = 16.sp,
            ),
        )
    }
}

/**
 * M3 Typography'de karsiligi olmayan, uygulamaya ozgu metin stilleri.
 * Font aileleri @Composable olarak cozuldugu icin bunlar da CompositionLocal ile
 * saglanir - LocalNeydiExtraColors ile ayni desen.
 */
@Immutable
data class NeydiTextStyles(
    /** Liste satirindaki urun adi. */
    val itemName: TextStyle,
    /** Alisveris modu: 72dp satir, 20sp / 700. */
    val itemNameShopping: TextStyle,
    /** Adet rozeti. */
    val quantityBadge: TextStyle,
    /** Liste satirindaki fiyat cipi. Kendi 44dp dokunma hedefi var. */
    val priceChip: TextStyle,
    /** Fis kontrol ekranindaki satir fiyati - duzeltilebilir, o yuzden daha buyuk. */
    val priceRow: TextStyle,
)

/**
 * TODO(tnum): Fiyat ve adet stilleri tabular figures ("tnum") istiyor. Compose'un
 * ortak API'sinde OpenType feature ayari yok; Android'de PlatformTextStyle /
 * fontFeatureSettings ile, iOS'ta Skia uzerinden verilecek. Skia desteklemedigi
 * ozellikleri SESSIZCE yok sayabiliyor - gercek bir iPhone'da dogrulanacak (F9.4).
 * Bu yuzden fiyat sutunu ayrica SizesExtra.priceColumn (92dp) sabit genislik +
 * TextAlign.End ile kuruluyor; tnum uygulanmasa da duzen ayakta kalir.
 */
@Composable
fun rememberNeydiTextStyles(): NeydiTextStyles {
    val ui = neydiUiFamily()
    return remember(ui) {
        NeydiTextStyles(
            itemName = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Medium,
                fontSize = 17.sp, lineHeight = 22.sp,
            ),
            itemNameShopping = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.Bold,
                fontSize = 20.sp, lineHeight = 25.sp,
            ),
            quantityBadge = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp, lineHeight = 24.sp,
            ),
            priceChip = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, lineHeight = 19.sp,
            ),
            priceRow = TextStyle(
                fontFamily = ui, fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp, lineHeight = 22.sp,
            ),
        )
    }
}

val LocalNeydiTextStyles = staticCompositionLocalOf<NeydiTextStyles> {
    error("NeydiTextStyles saglanmadi. NeydiTheme icinde misin?")
}
