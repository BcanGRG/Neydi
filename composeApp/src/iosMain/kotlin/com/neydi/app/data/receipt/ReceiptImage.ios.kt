package com.neydi.app.data.receipt

/**
 * iOS kucultmesi Faz 8'de gelecek (UIImage + CGImage).
 *
 * false donuyor, ISTISNA ATMIYOR: sozlesme "kucultemedim, orijinali kullan"
 * demek ve fis akisinin iOS'ta da calismasi gerekiyor - yalnizca gorsel daha
 * buyuk olacak. Burada patlamak butun fis okumayi iOS'ta olduren bir seyi
 * bir bicimlendirme detayina baglamak olurdu.
 *
 * DIKKAT (Faz 8): CMP iOS'ta UIImage ve kamera karesi bellek baskisi bilinen
 * bir sorun; kucultme burada Android'den DAHA kritik.
 */
actual suspend fun downscaleForOcr(
    source: ByteArray,
    destPath: String,
    maxLongEdge: Int,
): Boolean = false
