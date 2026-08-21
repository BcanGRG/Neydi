package com.neydi.app.data.ocr

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
