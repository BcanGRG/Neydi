package com.neydi.app.data.fis

/**
 * iOS OCR HENUZ YOK - Faz 8'de Apple Vision framework ile gelecek
 * (VNRecognizeTextRequest; o da ucretsiz ve cihazda calisiyor).
 *
 * Bilerek SESSIZ BASARISIZLIK DEGIL: acik bir hata donuyor ki iOS'ta fis
 * okuma "calismiyor gibi" degil, "henuz yok" gorunsun. Vision'in Turkce
 * destegi Faz 8'de DOGRULANMALI - simdiden soz verilmiyor.
 */
internal class IosFisOkuyucu : FisOkuyucu {
    override suspend fun satirlariOku(gorselYolu: String): Result<List<String>> =
        Result.failure(NotImplementedError("Fis okuma iOS'ta henuz yok (Faz 8)."))
}
