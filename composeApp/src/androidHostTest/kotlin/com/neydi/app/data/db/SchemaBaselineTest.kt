package com.neydi.app.data.db

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * YAYINLANMIS SEMA TEMELLERININ NOBETCISI (F10.15).
 *
 * NEDEN VAR: F4.1'de sema temeli **sessizce ezildi**. Hata ayiklarken surum
 * gecici olarak 1'e cekilince Room `1.json`'i yeni kolonlarla uzerine yazdi,
 * iki sema arasindaki diff **bos** cikti ve **hicbir sey yapmayan** bir
 * migration uretildi. Derleme yesildi, 93 test yesildi, ve hata ancak GERCEK
 * v1 verisi olan bir cihaza v2 kurulunca `IllegalStateException: Migration
 * didn't properly handle` olarak patladi.
 *
 * Bu test o kazayi **sessiz ve yikici** olmaktan **gurultulu** olmaya cevirir.
 *
 * NEDEN `androidHostTest`, `commonTest` DEGIL: dosya okumak gerekiyor ve
 * `java.io.File` yalnizca JVM'de var. Bu ayni zamanda projenin ilk
 * androidHostTest dosyasi (bkz. F10.13).
 *
 * NEDEN ELLE YAZILMIS HASH'LER: dosyayi kendisiyle karsilastirmak hicbir sey
 * kanitlamaz. Bu degerler yayinlanmis semalarin **ikinci gercek kaynagi** -
 * biri degisirse ikisi ayrisir ve test bagirir.
 *
 * YENI SURUM EKLERKEN: yeni `<n>.json` uretildikten SONRA hash'i asagiya
 * eklenir. Eski satirlar **asla** guncellenmez - guncellemek nobetciyi
 * anlamsiz kilar.
 */
class SchemaBaselineTest {

    /** Yayinlanmis, DEGISMEZ temeller: surum -> identityHash. */
    private val published = mapOf(
        1 to "79a4b5c5f6f322a4419646c47e027adb",
        2 to "a80e7052abd5ae6f2761d37beb58041a",
        3 to "9a25da10097f10bb5f49f777e7a8c9ae",
        // v4: Receipt.rawOcrText (F4.14). Tek nullable kolon.
        4 to "c49e177df81931853a48b5fd6acc4bcb",
        // v5: PIVOT (E11). `receipt` + `receipt_line` tablolari,
        // `price_observation.receiptLineId` ve `trip.totalMinor` DUSTU;
        // `price_observation.brand` GIRDI. Silmeler `Migration4To5Spec`
        // uzerinden, tamamen otomatik.
        5 to "f3f49c1da71c866c63044e4bd2ccde9a",
        // v6: IKI OTOMATIK EKLEME. `app_settings.catalogSeedVersion` (F2.7 -
        // gomulu katalogun bu haneye yazilmis surumu) ve `suggestion_event`
        // uzerinde `(householdId, productId, outcome)` indeksi (F6.5'in
        // uc-vurus sorgusu). Ikisi de nullable/indeks, veri geri-doldurmasi
        // YOK - toplu bump kuralinin sarti korunuyor.
        6 to "eb22e60bf4e0d69ab72f6b8049ca1734",
    )

    private val schemaDir: File by lazy {
        // Test calisma dizini modulun koku olabilir ya da repo koku olabilir;
        // ikisini de dene ki gorev nereden cagrilirsa calissin.
        val candidates = listOf(
            File("schemas/com.neydi.app.data.db.NeydiDatabase"),
            File("composeApp/schemas/com.neydi.app.data.db.NeydiDatabase"),
        )
        candidates.firstOrNull { it.isDirectory }
            ?: error("sema dizini bulunamadi; denenen: ${candidates.map { it.absolutePath }}")
    }

    private fun identityHashOf(version: Int): String {
        val file = File(schemaDir, "$version.json")
        assertTrue(file.isFile, "sema dosyasi yok: ${file.absolutePath}")
        val match = Regex(""""identityHash"\s*:\s*"([a-f0-9]+)"""").find(file.readText())
            ?: error("$version.json icinde identityHash bulunamadi")
        return match.groupValues[1]
    }

    @Test
    fun publishedSchemaBaselinesAreUnchanged() {
        published.forEach { (version, expected) ->
            assertEquals(
                expected,
                identityHashOf(version),
                "$version.json TEMELI DEGISMIS. Yayinlanmis bir sema dosyasi asla degismez; " +
                    "degistiyse ya surum numarasi geri cekildi ve Room temeli uzerine yazdi " +
                    "(F4.1'in kazasi), ya da entity'ler degistirilip surum artirilmadi. " +
                    "Ikisi de cihazda gercek veriyle patlar.",
            )
        }
    }

    /**
     * Silinmis bir temel, diff'in referansini yok eder ve bir sonraki
     * migration'i sessizce bos uretir.
     */
    @Test
    fun everyPublishedSchemaFileExists() {
        published.keys.sorted().forEach { version ->
            assertTrue(
                File(schemaDir, "$version.json").isFile,
                "$version.json eksik - yayinlanmis temel silinmis",
            )
        }
    }

    /**
     * Diskteki en yuksek sema ile bu testin bildigi en yuksek surum AYNI olmali.
     *
     * Surum artirilip sema uretilmediginde (ya da tersi) burada kiriliyor -
     * ikisinin ayrismasi tam olarak F4.1'in yasadigi sey.
     */
    @Test
    fun highestPublishedSchemaIsTracked() {
        val onDisk = schemaDir.listFiles()
            ?.mapNotNull { it.name.removeSuffix(".json").toIntOrNull() }
            ?.maxOrNull()
            ?: error("hic sema dosyasi yok")
        assertEquals(
            published.keys.max(),
            onDisk,
            "Diskte $onDisk.json var ama bu test en yuksek surumu ${published.keys.max()} " +
                "biliyor. Yeni sema uretildiyse hash'i bu dosyaya eklenmeli.",
        )
    }
}
