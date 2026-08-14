package com.neydi.app.data.receipt

/** Fis gorseli icin ust sinir: uzun kenar bu kadar piksel. */
const val MAX_LONG_EDGE = 2576

/**
 * Fis fotografini OCR icin kucultup hedefe yazar.
 *
 * PLATFORM TARAFINDA yapiliyor, ortak kodda degil: goruntu kod cozme her iki
 * platformda da yerel API gerektiriyor ve iOS'ta UIImage/kamera karesi bellek
 * baskisi bilinen bir sorun - orada dikkatli olmak gereken yer bu.
 *
 * SINIRIN SEBEBI DEGISTI. Once 2576 piksel Claude vision'in kabul ettigi en
 * buyuk cozunurluktu, yani bir MALIYET siniriydi. ML Kit cihazda calistigi
 * icin artik token maliyeti yok; sinir simdi BELLEK icin: ML Kit tam boy
 * bitmap'i bellege aliyor ve eski telefonlarda 12MP bir kare rahatca
 * OutOfMemory veriyor. 2576 fis metni icin bol bol yeterli - termal fisler
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
 *   HAM BAYTLARI ayni yola yazmali. Fis fotografi kullanicinin tek kaniti,
 *   kucultemedik diye onu atmak kabul edilemez.
 */
expect suspend fun downscaleForOcr(
    source: ByteArray,
    destPath: String,
    maxLongEdge: Int = MAX_LONG_EDGE,
): Boolean
