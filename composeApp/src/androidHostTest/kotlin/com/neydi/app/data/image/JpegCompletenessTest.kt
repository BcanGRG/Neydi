package com.neydi.app.data.image

import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * "Yazdim" diyen bir denetleyicinin yarim dosya birakmasi.
 *
 * ## Bu test OLCULEN bir olaydan dogdu
 *
 * Cihazda cekilen bir kare 4.127.687 bayttti ve EOI (`FFD9`) TASIMIYORDU;
 * ayni oturumdaki digerleri 5,4-5,9 MB ve duzgun bitiyordu. `ImageCapture`
 * `onImageSaved` cagirmis, `capture()` true donmus, kart acilmisti.
 *
 * Bozulmanin gorunmez kalmasinin sebebi: `BitmapFactory` eksik veriyi
 * okuyabildigi kadar okuyup gerisini griyle dolduruyor ve `downscaleForOcr`
 * bunu GECERLI bir JPEG olarak yeniden yaziyor. Kucultulmus kopyaya bakan
 * hicbir sey bir hata oldugunu anlayamiyor - kirpim da, OCR da.
 *
 * Fikstur ELLE KURULUYOR, cihazdan gelen 4 MB'lik kare depoya konmuyor:
 * kuralin ilgilendigi sey son iki bayt ve onu on bes baytlik bir dosyayla
 * anlatmak, dort megabaytla anlatmaktan hem hizli hem acik.
 */
class JpegCompletenessTest {

    private fun tempFile(name: String, bytes: ByteArray): String {
        val f = File.createTempFile(name, ".jpg")
        f.deleteOnExit()
        f.writeBytes(bytes)
        return f.absolutePath
    }

    private val soi = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    private val eoi = byteArrayOf(0xFF.toByte(), 0xD9.toByte())
    private val govde = ByteArray(64) { 0x42 }

    @Test
    fun `eksiksiz jpeg gecer`() = runTest {
        assertTrue(jpegIsComplete(tempFile("tam", soi + govde + eoi)))
    }

    /** OLCULEN HATA: yazma yarida kesilmis, EOI hic yazilmamis. */
    @Test
    fun `yarida kesilmis jpeg gecmez`() = runTest {
        assertFalse(jpegIsComplete(tempFile("yarim", soi + govde)))
    }

    /**
     * SOI DE KONTROL EDILIYOR.
     *
     * Yalnizca son iki bayta baksaydik, icerigi tamamen alakasiz ama sonu
     * tesadufen `FFD9` olan bir dosya gecerdi. Ikisi birden JPEG olmayan bir
     * seyin gecme ihtimalini pratikte sifira indiriyor.
     */
    @Test
    fun `jpeg olmayan ama dogru biten dosya gecmez`() = runTest {
        assertFalse(jpegIsComplete(tempFile("sahte", govde + eoi)))
    }

    @Test
    fun `bos dosya gecmez`() = runTest {
        assertFalse(jpegIsComplete(tempFile("bos", ByteArray(0))))
    }

    /** Olmayan dosya bir CEVAP degil, ama iddia da etmemeli. */
    @Test
    fun `olmayan dosya gecmez`() = runTest {
        assertFalse(jpegIsComplete("/olmayan/klasor/olmayan.jpg"))
    }
}
