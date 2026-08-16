package com.neydi.app.data.receipt

import com.neydi.app.data.db.Receipt
import com.neydi.app.data.image.deleteFileAt
import com.neydi.app.data.image.downscaleForOcr
import com.neydi.app.data.image.writeBytesTo
import com.neydi.app.data.repo.ListRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

/**
 * Cekilen fotografi kuculterek kaydeder ve fis kuyruguna alir.
 *
 * IKI GIRIS KAPISI PAYLASIYOR (F4.13): ozet kartindaki "Fis cek" ve Fis
 * Kontrol'deki "Sonraki parcayi cek". Ayni mantigi iki yerde yazmak, birinde
 * `content://` dersini unutmak demekti - asagidaki her yorum cihazda odenmis
 * bir bedelin kaydi.
 *
 * OCR BURADA KOSMUYOR: fotograf asla bloklamaz (F4.2). Isleme, Fis Kontrol
 * ekrani acilinca ya da Gecmis'ten dokununca kosuyor.
 */
suspend fun attachReceiptToTrip(
    repo: ListRepository,
    householdId: String,
    tripId: String,
    source: PlatformFile,
    destPath: String,
    rawPath: String,
): Receipt {
    // Baytlari BURADA okuyoruz: kaynak `content://` URI de olabilir (kamera
    // FileProvider uzerinden yaziyor) ve PlatformFile ikisini de cozuyor.
    // Kucultmeye yol vermek cihazda sessizce basarisizdi.
    val bytes = source.readBytes()
    val ok = downscaleForOcr(bytes, destPath)
    if (!ok) {
        // Kucultemedik: HAM BAYTLARI ayni yola yaz. Kayitli yol HER ZAMAN
        // bizim dosyamiz - `content://` URI saklamak yanlis olurdu, izin
        // baglari kalici degil ve yol yarin cozulmez.
        writeBytesTo(destPath, bytes)
    }
    // Ham dosyayi SIL - fis fotografi kisisel veri, iki kopya gereksiz
    // maruziyet. YOL uzerinden: `PlatformFile.delete()` `content://` URI'de
    // cihazda sessizce hicbir sey yapmadi.
    if (destPath != rawPath) deleteFileAt(rawPath)
    return repo.enqueueReceipt(householdId = householdId, tripId = tripId, imagePath = destPath)
}
