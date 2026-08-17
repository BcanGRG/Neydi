package com.neydi.app.data.ocr

/**
 * DIKKAT: Windows'ta DERLENMEZ, dolayisiyla DOGRULANMAMIS.
 *
 * iOS karsiligi **yazilmadi** ve bu bilincli: ROADMAP'te **F9.2** (*"etiket
 * hatti: `downscaleForOcr` + `readTag` actual'lari"*) olarak duruyor ve Mac
 * gerektiriyor. Karsiligi `Vision` cercevesinin `VNRecognizeTextRequest`'i
 * olacak; `VNRecognizedTextObservation` dort kose noktasi veriyor, yani
 * [OcrPiece] sozlesmesi orada da karsilanabiliyor.
 *
 * BOS BIR LISTE DONDURMEK YERINE PATLIYOR. Sessizce bos donen bir okuyucu, iOS
 * kullanicisina "etiket okunamadi" diye gorunurdu - yani var olmayan bir
 * ozelligi bozuk bir ozellik gibi gostermek. Patlamak, eksigin eksik oldugunu
 * soyluyor.
 */
internal actual suspend fun readTag(imagePath: String): TagOcr =
    throw NotImplementedError("readTag iOS actual'i F9.2'de yazilacak (Vision / VNRecognizeTextRequest)")
