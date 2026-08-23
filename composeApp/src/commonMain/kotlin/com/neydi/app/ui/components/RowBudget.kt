package com.neydi.app.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ekonomi bandinin yer bulamadiginda DUSURDUGU tek oge (karar 104).
 *
 * ## Neden bes uye degil bir uye
 *
 * Karar 80 bes uyeli bir feda sirasi kurmustu - sparkline, delta, avatar,
 * raptiye, rozet - ve olculdugunde kendi gerekcesini ihlal ediyordu: 360dp'de
 * ucu birden dusuyordu, en pahalisi (adet rozeti) dahil. Tasarim bunu bir ARA
 * ADIM ilan etti (karar 102): *"kural en pahali ogeyi de feda edebiliyordu,
 * cunku feda edecek baska sey kalmiyordu."*
 *
 * Cozum siralamayi duzeltmek degil YARISI KALDIRMAK oldu. Satir iki banda
 * bolundu; kimlik bandi (rozet, ad, avatar, raptiye) hicbir genislikte
 * dusmuyor, ekonomi bandinda ise fiyat cipi ile meta da dusmuyor. Geriye tek
 * feda edilebilir oge kaliyor: delta cipi. Sparkline silindi (karar 106).
 *
 * Bu yuzden burada artik bir enum ve bir siralama yok - tek bir soru var.
 */

/**
 * Delta cipinin yaninda metanin KIRPILMADAN sigacagi genislik.
 *
 * Tasarimin 360dp olcumu: fiyat 92dp + delta 46dp, metaya 140dp kaliyor ve
 * *"onceki 324,00 TL"* 92dp tutuyor - yani sigiyor. Bir alt hal de yazili:
 * *"altta 30 karakterlik meta: yalniz delta duser, cumle tam kalir."*
 *
 * Kural bu yuzden bir TABAN GENISLIGI degil bir KARSILASTIRMA: delta, metanin
 * kendi olculen genisligini yemiyorsa kaliyor. Sabit bir taban koymak uzun
 * cumleyi kisa cumleyle ayni muameleye tabi tutardi.
 */
private val DELTA_GAP: Dp = 6.dp

/**
 * Delta cipi cizilecek mi (karar 104).
 *
 * ## Neden saf bir fonksiyon
 *
 * *"Cumle tam kalir"* olculebilir bir soz: metin kirpilirsa soz tutulmamis
 * demektir. Compose kurmadan sinanabilmesi, tasarimin verdigi sayilarin
 * (360dp'de meta 140dp, *"onceki 324,00 TL"* 92dp) dogrudan test olarak
 * yazilabilmesi anlamina geliyor.
 *
 * @param available ekonomi bandinin ic genisliginden **fiyat cipi dusulmus**
 *   hali. Cip 92dp'lik sabit sutun ve asla dusmuyor - yani butcenin konusu
 *   degil, sadece bir eksiltme. Onu unutmak tasarimin kendi sayilarini
 *   tutturamamak demek.
 * @param metaWidth meta cumlesinin OLCULEN (kirpilmamis) genisligi. Sifir ise
 *   band bos demektir ve delta rahatca yasar.
 * @param deltaWidth delta cipinin olculen genisligi.
 */
fun deltaSurvives(available: Dp, metaWidth: Dp, deltaWidth: Dp): Boolean =
    available - deltaWidth - DELTA_GAP >= metaWidth
