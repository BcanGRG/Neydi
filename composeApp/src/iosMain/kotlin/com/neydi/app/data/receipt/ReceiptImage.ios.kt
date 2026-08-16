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
 *
 * DIKKAT (Faz 8): sozlesme YONU DE PIKSELE ISLEMEYI istiyor. iOS'ta karsiligi
 * `UIImage.imageOrientation`; kareyi olceklerken bir baglama cizmek yonu zaten
 * duzeltiyor ama `CGImage`i dogrudan yazmak ETIKETI biraktigi icin ayni hatayi
 * uretir - Android'de o hata fisin sekiz satira cokmesi olarak odendi (F4.20).
 */
actual suspend fun downscaleForOcr(
    source: ByteArray,
    destPath: String,
    maxLongEdge: Int,
): Boolean = false
