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

/**
 * Bir klasordeki HER SEYI siler; kac dosya silindigini doner.
 *
 * ## Neden gerekti
 *
 * Karar 29 fotografi saklamiyor ve kod bunu iki yerde tutuyordu: kaydedince ve
 * vazgecince. Ucuncu bir yol var ve orada kimse silmiyordu - kart acikken
 * uygulamanin OLDURULMESI. Cihazda dort yetim kare bulundu, toplam 25 MB, en
 * eskisi dort saatlik. "Fotograf saklanmiyor" sozu o dosyalar dururken
 * yaridan fazlasi kadar dogruydu.
 *
 * Sureci olen kartin durumu ZATEN kaybediliyor (`SavedStateHandle` yok), yani
 * ekran acilirken klasorde bulunan her sey tanimi geregi yetim: hicbir kart
 * onlari sahiplenmiyor.
 */
expect suspend fun deleteFilesIn(dirPath: String): Int

/**
 * Diskteki JPEG EKSIKSIZ MI - son iki bayti `FFD9` (EOI) mi.
 *
 * ## Neden gerekiyor: "basardim" diyen bir yalan
 *
 * `ImageCapture.OnImageSavedCallback.onImageSaved` cagrildi, `capture()` true
 * dondu, kart acildi - ve dosya YARIM YAZILMISTI. Cihazda olculdu: kullanicinin
 * cektigi kare 4.127.687 bayt ve EOI TASIMIYOR; ayni oturumdaki digerleri
 * 5,4-5,9 MB ve duzgun bitiyor. Aralikli, ama sessiz.
 *
 * Bozulmanin tehlikeli tarafi KENDINI TEMIZLEMESI: `BitmapFactory` eksik
 * veriyi okuyabildigi kadar okuyup gerisini duz griyle dolduruyor, sonra
 * `downscaleForOcr` bunu GECERLI bir JPEG olarak yeniden yaziyor. Yani
 * kucultulmus kopya saglam gorunuyor ve asagi akista hicbir tuketici -
 * ne OCR, ne kirpim - bir sey oldugunu anlayamiyor. Raf etiketinde kaybolan
 * alt ucte bir FIYATIN KENDISI olabilir; "yanlis fiyat, fiyat olmamasindan
 * kotu" kurali tam olarak bunu yasakliyor.
 *
 * SON IKI BAYT OKUNUYOR, dosyanin tamami DEGIL: kare bes megabayt ve onu
 * yalnizca kontrol icin bellege almak, tam da kacinmak istedigimiz ikinci
 * kopya olurdu.
 *
 * Bu kontrol JPEG'in ICERIGINI dogrulamiyor - bozuk ama EOI'li bir dosya
 * gecer. Kapatabildigi sey OLCULEN hata: yarida kesilmis yazma.
 */
expect suspend fun jpegIsComplete(path: String): Boolean

/**
 * Klasordeki dosya YOLLARI - yalnizca olcum ice aktarimi icin.
 *
 * `deleteFilesIn`in ikizi ve ayni gerekceyle platformda: `java.io.File` ve
 * `NSFileManager` disinda ortak bir listeleme yok, tek bir teshis yolu icin
 * `kotlinx-io` eklemek fazla.
 */
expect suspend fun listFilesIn(dirPath: String): List<String>
