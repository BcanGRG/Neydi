package com.neydi.app.data.ocr

/**
 * Bir raf etiketinin ham OCR ciktisi.
 *
 * ## Neden IKI seviye tasiniyor
 *
 * [lines] OCR'in kendi gruplamasi, [words] onun bir alt kirilimi. Ayristirici
 * (E14) satir seviyesinde calisacak - `groupVisualRows` zaten satirlari gorsel
 * satirlara topluyor. Ama **kurus ustsimgesi sorusu ancak kelime seviyesinde
 * cevaplanabilir**: `129⁹⁰` OCR'dan tek parca mi geliyor yoksa `129` ve `90`
 * diye iki parca mi? Satir seviyesinde ikisi de ayni dizeyi verir ve fark
 * kaybolur.
 *
 * Yani ikinci seviye E14'un ihtiyaci degil, **E12'nin sorusunun kaniti**. Fikstur
 * ikisini birden tasiyor cunku fotograflar bir kez cekiliyor - sonradan "keske
 * kelimeleri de dokmusuz" demek yeni bir markete gitmek demek olurdu.
 *
 * @param sourceWidth OCR'a giren bitmap'in genisligi. Kose noktalari bu
 *   cerceveye gore, yani fikstur boyutu bilmeden okunamaz.
 * @param exifOrientation KAYNAK JPEG'in EXIF yon etiketi - **bilgi olarak**
 *   tasiniyor, cunku [lines] yonu ZATEN piksele islenmis bir goruntuden
 *   olculdu (`downscaleForOcr`, F4.20). Fiksturde durmasinin sebebi E12'nin
 *   ikinci sorusu: elde cekimde yon duzeltmesi gercekten gerekiyor mu, ve
 *   duzeltilmis goruntude kose siralamasi ne oluyor.
 */
internal data class TagOcr(
    val lines: List<OcrPiece>,
    val words: List<OcrPiece>,
    val sourceWidth: Int,
    val sourceHeight: Int,
    val exifOrientation: Int,
)

/**
 * Bir etiket fotografini okur - TEK BITMAP, TEK OCR CAGRISI.
 *
 * ŞERIT/YON OYLAMASI/MUKERRER ELEME YOK ve bu bilincli bir sadelestirme. Fis
 * doneminde okuyucu metrelik bir kagidi `BitmapRegionDecoder` ile serit serit
 * okuyor, her seridi dort yonde deneyip oy sayiyor ve ortusen seritlerden gelen
 * mukerrer satirlari eliyordu (F4.5-F4.19). Raf etiketi bir avuc ici; o
 * makinenin tamami ona gereksiz.
 *
 * GIRDI ZATEN DIK OLMALI: cagiran taraf once `downscaleForOcr` ile EXIF'i
 * piksele isliyor. Burada yon duzeltmesi yapilmiyor - iki yerde yapmak "iki
 * kere dondur" hatasina acik kapi olurdu.
 *
 * @param imagePath `downscaleForOcr`in yazdigi dosya. `content://` URI DEGIL -
 *   o ders bir kez odendi (bkz. `downscaleForOcr` KDoc'u).
 * @param sourceExif KAYNAK fotografin EXIF yon etiketi. Cagirandan geliyor cunku
 *   [imagePath] artik EXIF TASIMIYOR - `downscaleForOcr` yonu piksele isleyip
 *   etiketsiz yaziyor. Ilk surum onu bu dosyadan okumaya calisiyordu ve her
 *   fiksture `0` yaziyordu: dogru cevap ama yanlis soruya.
 */
internal expect suspend fun readTag(imagePath: String, sourceExif: Int = 0): TagOcr
