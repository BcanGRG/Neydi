package com.neydi.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * "Sicak Kiler" paleti.
 *
 * Kontrast oranlari WCAG 2.x sRGB'ye gore hesaplandi; asagidaki NeydiColorTest
 * bunlari dogrular. Bir token'i degistirirsen test kirilir - bu kasitli.
 *
 * KRITIK KURAL: accent (#E0A32E) isik modunda surface uzerinde 2.08:1'dir,
 * yani kendi sinirini TASIYAMAZ. Isik modundaki her accent dolgusu
 * accentOutline (#8A5A00, 5.56:1) ile 1.5-2dp kenarlik tasimak ZORUNDA.
 * Amber isik modunda asla metin rengi degildir.
 */

// --- Isik modu ---
private val LightSurface = Color(0xFFFBF7F2)
private val LightSurfaceVariant = Color(0xFFF1E7DB)
private val LightPrimary = Color(0xFFB34418)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightSecondary = Color(0xFF3F6B54)
private val LightAccent = Color(0xFFE0A32E)
private val LightAccentOutline = Color(0xFF8A5A00)
private val LightOnAccent = Color(0xFF3A2600)
private val LightSuccess = Color(0xFF2E6B45)
private val LightWarning = Color(0xFF96560A)
private val LightError = Color(0xFFB3261E)
private val LightOutline = Color(0xFF8A7666)
private val LightTextPrimary = Color(0xFF221A14)
private val LightTextSecondary = Color(0xFF5C4F45)
private val LightHairline = Color(0xFFE7DACB)

// --- Karanlik mod ---
private val DarkSurface = Color(0xFF13100E)
private val DarkSurfaceVariant = Color(0xFF241E1A)
private val DarkPrimary = Color(0xFFFF9166)
private val DarkOnPrimary = Color(0xFF3B1503)
private val DarkSecondary = Color(0xFF8FC7A2)
private val DarkAccent = Color(0xFFF2C14E)
private val DarkOnAccent = Color(0xFF2A1D00)
private val DarkSuccess = Color(0xFF7FD1A0)
private val DarkWarning = Color(0xFFF0B357)
private val DarkError = Color(0xFFFF8A80)
private val DarkOutline = Color(0xFF8A7A6E)
private val DarkTextPrimary = Color(0xFFF5EDE6)
private val DarkTextSecondary = Color(0xFFC6B6A9)
private val DarkHairline = Color(0xFF2E2621)

/**
 * Karanlik temada ikonun rengi - metninkinden BIR KADEME ACIK (tasarim karari 33).
 *
 * Tasarim once karanlikta `GRAD 100` istemisti: ince acik konturlar koyu zeminde
 * optik olarak inceliyor, GRAD ekseni onlari kalinlastirip yanindaki metinle ayni
 * agirlikta okutuyor. Degisken font paketlenmedigi icin (karar 32) kalinlik
 * ekseni yok; telafi RENK KADEMESINE cevrildi. Ayni ise iki cozum degil - ayni
 * optik sorunun elimizdeki tek araciyla cozumu.
 *
 * DEGER TASARIMIN VERDIGI CIFTTEN: karar 33 "metin `#E4D8C9`, ikon `#F5EDE6`"
 * diyor. Uygulamanin karanlik rampasi ise `#C6B6A9` (ikincil metin) →
 * `#F5EDE6` (birincil metin) ve `#E4D8C9` bu ikisinin arasina, ust basamaga
 * yakin dusuyor. Yani tasarimin verdigi cift iki MUTLAK renk degil, bir
 * ILISKI: ikon, yanindaki metinden bir basamak yukarida. Ikincil metin
 * (`#C6B6A9`) yanindaki ikon o basamaga - `#E4D8C9`'a - cikiyor. Birincil
 * metnin yanindaki ikon zaten rampanin tepesinde (`#F5EDE6`), bir kaldirma
 * tepenin ustune cikamaz.
 *
 * OKUMA TASARIMCA ONAYLANDI: karar 33 bu turda yeniden yazildi ve *"kural
 * mutlak renk degil ILISKI"* diyor - ayni hex ciftiyle (`#C6B6A9` yanindaki
 * ikon `#E4D8C9`) ve ayni gerekceyle ("birincil metin zaten rampanin
 * tepesinde, orada telafi yok"). Kararin *"palet degismiyor"* cumlesi de
 * alternatif okumayi - karanlik govde metnini bir kademe indirmek - acikca
 * reddediyor.
 *
 * ISIK TEMASINDA TELAFI YOK (`GRAD 0`): orada ikon ikincil metinle ayni renkte.
 */
private val DarkIconMuted = Color(0xFFE4D8C9)

val NeydiLightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = Color.White,
    error = LightError,
    onError = Color.White,
    background = LightSurface,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightHairline,
)

val NeydiDarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF10271A),
    error = DarkError,
    onError = Color(0xFF3B0907),
    background = DarkSurface,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkHairline,
)

/**
 * M3 ColorScheme'de karsiligi olmayan marka token'lari.
 * accent'i M3'un tertiary'sine gomup kacmadik, cunku accentOutline kuralinin
 * gorunur ve unutulmaz olmasi gerekiyor.
 */
@Immutable
data class NeydiExtraColors(
    val accent: Color,
    val accentOutline: Color,
    val onAccent: Color,
    val success: Color,
    val warning: Color,
    val hairline: Color,
    /**
     * Muted ikonun rengi - `NeydiIcon`in varsayilani.
     *
     * Isik temasinda ikincil metinle AYNI, karanlikta bir kademe ACIK
     * (tasarim karari 33). Neden ayri bir token oldugu [DarkIconMuted]'ta.
     */
    val iconMuted: Color,
    /** Fiyat artisi (kirmizi ok) */
    val priceUp: Color,
    /** Fiyat dususu (yesil ok) */
    val priceDown: Color,
    /** Isik modunda accent dolgusu kenarlik gerektirir mi */
    val accentNeedsOutline: Boolean,
    /**
     * Tema isik modunda mi (tasarimin `NeydiColors.isLight` alani).
     *
     * `accentNeedsOutline` ile ayni degeri tasiyor ama ANLAMI farkli:
     * biri amber sozlesmesinin kurali, digeri temanin kimligi. Ikisini
     * tek alanda birlestirmek, amber kurali bir gun degistiginde
     * temayi soran her yeri sessizce bozardi.
     */
    val isLight: Boolean,
)

val LightExtraColors = NeydiExtraColors(
    accent = LightAccent,
    accentOutline = LightAccentOutline,
    onAccent = LightOnAccent,
    success = LightSuccess,
    warning = LightWarning,
    hairline = LightHairline,
    // Isik temasinda telafi yok: ikon ikincil metinle ayni renkte.
    iconMuted = LightTextSecondary,
    priceUp = LightError,
    priceDown = LightSuccess,
    accentNeedsOutline = true,
    isLight = true,
)

val DarkExtraColors = NeydiExtraColors(
    accent = DarkAccent,
    accentOutline = DarkAccent,
    onAccent = DarkOnAccent,
    success = DarkSuccess,
    warning = DarkWarning,
    hairline = DarkHairline,
    iconMuted = DarkIconMuted,
    priceUp = DarkError,
    priceDown = DarkSuccess,
    // Karanlik modda accent surface uzerinde 11.29:1 - kenarlik gerekmez.
    accentNeedsOutline = false,
    isLight = false,
)

val LocalNeydiExtraColors = staticCompositionLocalOf { LightExtraColors }
