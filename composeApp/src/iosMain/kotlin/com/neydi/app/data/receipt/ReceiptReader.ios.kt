package com.neydi.app.data.receipt

/**
 * iOS OCR HENUZ YOK - Faz 8'de Apple Vision framework ile gelecek
 * (VNRecognizeTextRequest; o da ucretsiz ve cihazda calisiyor).
 *
 * Bilerek SESSIZ BASARISIZLIK DEGIL: acik bir hata donuyor ki iOS'ta fis
 * okuma "calismiyor gibi" degil, "henuz yok" gorunsun. Vision'in Turkce
 * destegi Faz 8'de DOGRULANMALI - simdiden soz verilmiyor.
 */
internal class IosReceiptReader : ReceiptReader {
    override suspend fun readLines(imagePath: String): Result<List<String>> =
        Result.failure(NotImplementedError("Fis okuma iOS'ta henuz yok (Faz 8)."))
}
