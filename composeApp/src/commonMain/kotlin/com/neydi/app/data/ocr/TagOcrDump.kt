package com.neydi.app.data.ocr

import com.neydi.app.data.image.deleteFileAt
import com.neydi.app.data.image.downscaleForOcr
import com.neydi.app.data.image.listFilesIn
import com.neydi.app.data.image.writeBytesTo
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

/**
 * OCR ciktisini FIKSTUR BICIMINDE diske yazar - yeni zincir grameri icin.
 *
 * ## Neden bu var
 *
 * Bir zincirin grameri FOTOGRAFTAN yazilamiyor. `docs/18`'in ogrettigi sey
 * buydu: E14'un kurallari 27 BIM etiketinden OLCULDU ve 53 Metro/Migros
 * etiketi onlarin BIM'e ozel oldugunu gosterdi. Kurallar metnin kendisine
 * degil GEOMETRISINE dayaniyor - hangi satir en buyuk, hangi sutunda,
 * kaynagin yuzde kaci. Bunlar bir fotografa bakarak kestirilemez.
 *
 * A101 icin elde 19 fotograf var ve etiket yapisi BIM sinifi gorunuyor, ama
 * "gorunuyor" bir olcum degil. Bu dokum, kullanicinin normal cekimi sirasinda
 * ML Kit'in gercek ciktisini yakalayip [TagFixtures]'a DOGRUDAN yapistirilabilir
 * Kotlin olarak yaziyor.
 *
 * ## Kapisi bir ISARET DOSYASI, derleme bayragi degil
 *
 * Dokum yalnizca `<dir>/ENABLE` dosyasi varsa yaziliyor. Sebep: bu bir
 * TESHIS, urun ozelligi degil - kullanicinin haberi olmadan cihazinda metin
 * biriktirmemeli. Isaret dosyasini olcum yaparken adb ile ben koyuyorum; bir
 * kullanicinin cihazinda hicbir zaman olusmuyor.
 *
 * Derleme bayragina (BuildConfig.DEBUG) baglamak daha az guvenli olurdu:
 * `composeApp` bir KMP kutuphane modulu ve oradan BuildConfig okumak ek
 * kurulum ister; ustelik debug surumu de kullanicinin elinde.
 */
internal suspend fun dumpTagOcr(ocr: TagOcr, dirPath: String, name: String): Boolean {
    if (!dumpEnabled(dirPath)) return false
    return writeBytesTo("$dirPath/$name.kt.txt", renderFixture(ocr, name).encodeToByteArray())
}

/**
 * Isaret dosyasi var mi.
 *
 * OKUMAYI DENEYEREK bakiyor: FileKit'in "var mi" sorgusu ortak katmanda
 * garanti degil, ama okuma zaten iki platformda da var ve yoksa firlatiyor.
 * Tek maliyeti bir dosya acma denemesi ve o da cekim basina bir kez.
 */
private suspend fun dumpEnabled(dirPath: String): Boolean =
    runCatching { PlatformFile("$dirPath/ENABLE").readBytes() }.isSuccess

/**
 * [TagFixtures]'in kendi bicimi - kopyala, yapistir, calisir.
 *
 * Bilerek AYNI bicim: olcum ile fikstur arasinda elle bir donusturme adimi
 * olsaydi, o adim hatanin girecegi yer olurdu.
 */
private fun renderFixture(ocr: TagOcr, name: String): String = buildString {
    append("        \"").append(name).append("\" to TagOcr(\n")
    append("            lines = listOf(\n")
    ocr.lines.forEach { piece ->
        append("                OcrPiece(\"").append(piece.text.replace("\"", "\\\"")).append("\", listOf(")
        append(piece.corners.joinToString(", ") { "OcrPoint(${it.x}, ${it.y})" })
        append(")),\n")
    }
    append("            ),\n")
    append("            words = emptyList(),\n")
    append("            sourceWidth = ").append(ocr.sourceWidth)
    append(", sourceHeight = ").append(ocr.sourceHeight)
    append(", exifOrientation = ").append(ocr.exifOrientation).append(",\n")
    append("        ),\n")
}

/**
 * Klasordeki HAZIR fotograflari okuyup dokumlerini yazar.
 *
 * ## Neden gerekti
 *
 * Kullanici A101'de on dokuz etiket cekmisti - ama telefonun kendi
 * kamerasiyla, cunku o sirada uygulamanin urun alani her karede yanlis bir ad
 * gosteriyordu. "Fotograflar ise yaramaz" demistim; DOGRUSU su: fotografa
 * BAKARAK gramer yazilamaz, ama OCR o fotograflarin uzerinde kosturulabilirse
 * olcum aynen elde edilir. Ikinci bir market turu gerekmiyor.
 *
 * Kural degismiyor - gramer hala OLCULMUS geometriden yaziliyor. Degisen tek
 * sey geometrinin nereden geldigi: canli cekim yerine diskteki bir kare.
 *
 * ## Ayni kapi, ayni sebep
 *
 * Isaret dosyasi olmadan hicbir sey yapmiyor ([dumpTagOcr] ile ayni gerekce).
 * Girdi klasoru bir kez okunuyor ve okunan kare SILINMIYOR: olcumu tekrar
 * kosturmak - kural degistiginde - ayni girdiyi gerektiriyor.
 *
 * @param stage kucultulmus kopyanin yazilacagi gecici yol; cagiran veriyor
 *   cunku `readTag` DIK bir kare bekliyor ve EXIF'i piksele isleyen adim
 *   [downscaleForOcr] (bkz. `readFieldsFromPhoto`).
 */
internal suspend fun dumpImportedPhotos(dirPath: String): Int {
    if (!dumpEnabled(dirPath)) return 0
    val inbox = "$dirPath/in"
    var written = 0
    listFilesIn(inbox).filter { it.endsWith(".jpg", ignoreCase = true) }.forEach { path ->
        val name = path.substringAfterLast('/')
        val staged = "$dirPath/$name.ocr.jpg"
        runCatching {
            val bytes = PlatformFile(path).readBytes()
            val ok = downscaleForOcr(bytes, staged)
            val ocr = if (ok) readTag(staged) else readTag(path)
            deleteFileAt(staged)
            if (writeBytesTo("$dirPath/$name.kt.txt", renderFixture(ocr, name).encodeToByteArray())) written++
        }
    }
    return written
}
