package com.neydi.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Kategori kutucugunun rengi - tohumlanan tondan TURETILIYOR (F6.9).
 *
 * NEDEN TURETME, DOGRUDAN KULLANIM DEGIL. `Category.tintArgb` 12 gercek tonla
 * tohumlaniyor ama hicbir yerde okunmuyordu; dogrudan dolgu olarak kullanmanin
 * iki somut engeli var ve ikisi de olculebilir:
 *
 * 1. TONLAR ORTA-KOYU VE DOYGUN (`#6E8B3D`, `#A3453B`, `#4A7C8C`...).
 *    Uzerine koyu metin koymak okunmaz; acik metin koymak ise ISIK modunda
 *    krem zeminde bir "buton" gibi duruyor, tasarimin istedigi *"56dp sicak
 *    tonlu squircle"* gibi degil.
 * 2. IKI TEMA ICIN TEK TON KUMESI VAR. Krem `#FBF7F2` uzerinde sicak aksan
 *    gibi okunuyorlar; koyu `#13100E` uzerinde bazilari zeminden neredeyse
 *    AYRISMIYOR.
 *
 * Referans tablosu ancak katalog yeniden tohumlanmasiyla degisebilir (F2.7) ve
 * o adim acik. Bu yuzden dogru cevap UI'da turetmek - ve bu dosya o turetmenin
 * yazili hali.
 *
 * TURETME: ton zeminle KARISTIRILIYOR. Isikta hafif bir yikama (%22) sicak
 * pastel veriyor ve uzerine koyu metin rahat oturuyor; karanlikta daha yuksek
 * oran (%40) zeminden ayrismayi sagliyor ve uzerine acik metin oturuyor.
 * Ton bilgisi (hue) iki temada da korunuyor - kaybolan yalnizca doygunluk.
 *
 * KONTRAST IDDIA DEGIL OLCUM: `CategoryTintTest` 12 tonun her birini iki temada
 * da metin rengine karsi olcuyor. Bir ton eklenir ya da oran degistirilirse
 * test bagirir.
 */
object CategoryTint {

    /** Isik modunda zeminle karisim orani. */
    private const val LIGHT_MIX = 0.22f

    /** Karanlik modda oran daha yuksek: dusuk oran koyu zeminde kayboluyor. */
    private const val DARK_MIX = 0.40f

    /**
     * Kutucugun dolgu rengi.
     *
     * @param tintArgb `Category.tintArgb` - tohumlanan ham ton.
     * @param surface temanin zemini; karisimin diger ucu.
     */
    fun fill(tintArgb: Long, surface: Color, isLight: Boolean): Color {
        val seed = Color(tintArgb.toULong() shl 32)
        return blend(seed, surface, if (isLight) LIGHT_MIX else DARK_MIX)
    }

    /**
     * Kutucuk uzerindeki metin rengi.
     *
     * Dolgu zeminin YAKININDA kaldigi icin temanin normal metin rengi
     * gecerli - kutucuga ozel bir "onTint" rengi tanimlamak, 12 ton icin 12
     * ayri karar demek olurdu ve hicbiri olculmezdi.
     */
    fun content(textPrimary: Color): Color = textPrimary

    /** İki rengi dogrusal karistirir; `ratio` = [a]'nin payi. */
    private fun blend(a: Color, b: Color, ratio: Float): Color = Color(
        red = a.red * ratio + b.red * (1 - ratio),
        green = a.green * ratio + b.green * (1 - ratio),
        blue = a.blue * ratio + b.blue * (1 - ratio),
        alpha = 1f,
    )
}
