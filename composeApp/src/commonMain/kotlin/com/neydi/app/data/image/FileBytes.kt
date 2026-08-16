package com.neydi.app.data.image

/**
 * Baytlari dosyaya yazar.
 *
 * Kucultme basarisiz oldugunda ham fotografi kaydetmek icin var. Bunun
 * platforma ozel olmasi sart: `kotlinx-io` ya da `okio` eklemek tek bir dosya
 * yazma isi icin fazla; Android'de `java.io`, iOS'ta `NSData` yeterli.
 */
expect suspend fun writeBytesTo(destPath: String, bytes: ByteArray): Boolean

/**
 * Dosyayi YOL uzerinden siler.
 *
 * PlatformFile.delete() KULLANILAMADI: kameranin geri verdigi dosya bir
 * `content://` URI ve o URI uzerinden silme cihazda sessizce hicbir sey
 * yapmadi - ham fotograf diskte kaldi. Ham dosyanin gercek yolunu zaten
 * biliyoruz (biz adlandirdik), dolayisiyla yol uzerinden silmek hem calisiyor
 * hem daha az dolayli.
 */
expect suspend fun deleteFileAt(path: String): Boolean
