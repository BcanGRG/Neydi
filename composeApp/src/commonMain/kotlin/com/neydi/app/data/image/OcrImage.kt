package com.neydi.app.data.image

/**
 * OCR'a gidecek gorsel icin ust sinir: uzun kenar bu kadar piksel.
 *
 * 2576'DAN 4096'YA CIKARILDI (F4.17) ve bu, tek cekimin onkosuluydu.
 *
 * Eski sinir OCR'a giden goruntuyu sensorun verdiginin yaklasik ucte ikisine
 * indiriyordu - yani "goruntu tek kareye sigmiyor" itirazinin buyuk
 * bir kismi FIZIK DEGIL, bizim kirptigimiz pikseldi. 12MP bir karede uzun
 * kenar ~4000; 2576'ya inince satir basina dusen piksel de ayni oranda
 * eriyordu.
 *
 * Bellek gerekcesi hala gecerli ama artik baska turlu karsilaniyor: OCR
 * fotografi tek parca belege almiyor, `BitmapRegionDecoder` ile serit serit
 * okuyor (bkz. ReceiptReader.android.kt). Yani tam boy bitmap hicbir zaman
 * ayrilmiyor ve sinir yalnizca DISK icin duruyor.
 */
const val MAX_LONG_EDGE = 4096

/**
 * Cekilen kareyi OCR icin kucultup, YONUNU DUZELTIP hedefe yazar.
 *
 * YON SOZLESMENIN PARCASI (F4.20): yazilan dosya dik durmak zorunda, yani
 * kaynaktaki EXIF/`imageOrientation` bilgisi PIKSELE islenmeli ve cikti bir yon
 * etiketi TASIMAMALI. Bedeli olculdu: CameraX yonu yalnizca EXIF'e yaziyor,
 * eski kod da etiketi okumadan cozup etiketsiz yazdigi icin bilgi tam burada
 * kayboluyordu; kare yan haliyle okunuyor ve gorsel satir gruplamasi butun sayfayi
 * birkac dev satira cokuruyordu. Diskteki dosyayi OCR, `BitmapRegionDecoder` ve
 * ekran ayri ayri tuketiyor - etiketi tasimak sozlesmeyi hepsine yuklerdi.
 *
 * PLATFORM TARAFINDA yapiliyor, ortak kodda degil: goruntu kod cozme her iki
 * platformda da yerel API gerektiriyor ve iOS'ta UIImage/kamera karesi bellek
 * baskisi bilinen bir sorun - orada dikkatli olmak gereken yer bu.
 *
 * SINIRIN SEBEBI DEGISTI. Once 2576 piksel Claude vision'in kabul ettigi en
 * buyuk cozunurluktu, yani bir MALIYET siniriydi. ML Kit cihazda calistigi
 * icin artik token maliyeti yok; sinir simdi BELLEK icin: ML Kit tam boy
 * bitmap'i bellege aliyor ve eski telefonlarda 12MP bir kare rahatca
 * OutOfMemory veriyor. 2576 basili metin icin bol bol yeterli - termal baski
 * zaten dusuk kontrastli ve kucuk puntolu, cozunurlugu artirmak bir yerden
 * sonra okumayi iyilestirmiyor.
 *
 * NEDEN YOL DEGIL BAYT ALIYOR: ilk hali kaynak YOLU aliyordu ve cihazda hic
 * calismadi. Kamera FileProvider uzerinden yaziyor, dolayisiyla geri donen
 * dosya bir `content://` URI olarak geliyor - dosya sistemi yolu degil.
 * `BitmapFactory.decodeFile` onu okuyamiyor, sessizce basarisiz oluyordu.
 * Bayt almak URI/dosya ayrimini tamamen sinirin dışında birakiyor: dosyayi
 * cozmek cagiranin isi, gorseli olceklemek buranin.
 *
 * @return true = [destPath] yazildi. false = kucultme basarisiz; cagiran taraf
 *   HAM BAYTLARI ayni yola yazmali. Kare kullanicinin tek kaniti,
 *   kucultemedik diye onu atmak kabul edilemez.
 */
expect suspend fun downscaleForOcr(
    source: ByteArray,
    destPath: String,
    maxLongEdge: Int = MAX_LONG_EDGE,
): Boolean
