package com.neydi.app.data.image

/**
 * Kadraj rehberinin VIZORDEKI yeri - piksel.
 *
 * Kirpim bunu karenin koordinatlarina esliyor (karar 74). Ekran olcuyor,
 * goruntu katmani cozuyor: rehberin nerede oldugunu yalnizca yerlesim bilir,
 * karenin ne kadar buyuk oldugunu yalnizca kod cozucu.
 */
data class GuideBox(
    val previewWidth: Int,
    val previewHeight: Int,
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
) {
    /** Olculmemis ya da bozuk bir kutu kirpim yapmamali. */
    val usable: Boolean
        get() = previewWidth > 0 && previewHeight > 0 && width > 0 && height > 0
}

/** Karenin uzerindeki dikdortgen - piksel, DIK kare uzerinde. */
data class CropRect(val left: Int, val top: Int, val width: Int, val height: Int)

/**
 * Rehberin DIK karedeki karsiligi - `PreviewView` FILL_CENTER'in tersi.
 *
 * ## Neden bu hesap gerekiyor
 *
 * Serit once tam kareden MERKEZ kirpimla aliniyordu ve gosterdigi sey
 * kullanicinin kadraja oturttugu sey degildi. Maketin 92dp'si etiketin ust ve
 * alt kenarlarini kesiyordu; kod 128dp'ye cikardi ve kesilme yariya indi ama
 * kaynak hala yanlisti. Tasarim karar 74'te bunu *"bant oran degil KAYNAK
 * sozlesmesi"* diye ozetledi: serit ne kadar kisa olursa olsun gosterdigi
 * piksel, kullanicinin kadraja oturttugu piksel olmali.
 *
 * ## FILL_CENTER'in tersi
 *
 * `PreviewView` FILL_CENTER kareyi vizoru DOLDURACAK kadar buyutup ortaliyor,
 * yani tasan kenarlar gorunmuyor. Olcek iki orandan BUYUGU:
 *
 * ```
 * olcek   = max(vizorGenislik / kareGenislik, vizorYukseklik / kareYukseklik)
 * gorunen = vizor / olcek        <- karenin gercekten gorunen bolgesi
 * pay     = (kare - gorunen) / 2 <- her iki yandan kirpilan
 * ```
 *
 * Rehberin vizordeki `left` degeri `pay + left / olcek` ile kareye dusuyor.
 *
 * ## KARE DIK OLMALI
 *
 * Hesap `PreviewView`in GOSTERDIGI kareye gore; diskteki JPEG ise yonu EXIF
 * etiketinde tasiyor. Cagiran taraf bu fonksiyonu yonu piksele isledikten
 * SONRA cagirmak zorunda (bkz. `downscaleForOcr` KDoc'u, F4.20) - yoksa
 * dikdortgen doksan derece yanlis yere duser.
 *
 * @return kare disina tasmayan bir dikdortgen; kutu olculmemisse `null`.
 */
internal fun GuideBox.inImage(imageWidth: Int, imageHeight: Int): CropRect? {
    if (!usable || imageWidth <= 0 || imageHeight <= 0) return null

    val scale = maxOf(
        previewWidth.toDouble() / imageWidth,
        previewHeight.toDouble() / imageHeight,
    )
    if (scale <= 0.0) return null

    val visibleW = previewWidth / scale
    val visibleH = previewHeight / scale
    val padX = (imageWidth - visibleW) / 2.0
    val padY = (imageHeight - visibleH) / 2.0

    // KIRPMA, TASMA DEGIL: rehber vizorun kenarina dayaniyorsa hesap kareyi
    // birkac piksel asabiliyor (bolme yuvarlamasi). Disari tasan bir
    // dikdortgen `Bitmap.createBitmap`i patlatir - burada kesiliyor.
    val left = (padX + left / scale).coerceIn(0.0, imageWidth.toDouble())
    val top = (padY + top / scale).coerceIn(0.0, imageHeight.toDouble())
    val right = (padX + (this.left + width) / scale).coerceIn(0.0, imageWidth.toDouble())
    val bottom = (padY + (this.top + height) / scale).coerceIn(0.0, imageHeight.toDouble())

    val w = (right - left).toInt()
    val h = (bottom - top).toInt()
    if (w <= 0 || h <= 0) return null
    return CropRect(left = left.toInt(), top = top.toInt(), width = w, height = h)
}

/**
 * Kareyi REHBERIN BOLGESINDEN kirpip kucultur ve hedefe yazar (karar 74).
 *
 * `downscaleForOcr` ile ayni yon sozlesmesi: EXIF piksele isleniyor, cikti yon
 * etiketi tasimiyor. Fark yalnizca kirpim - ve o kirpim seridin gosterdigi
 * seyi kullanicinin kadraja oturttugu seye esitliyor.
 *
 * OCR BU YOLU KULLANMIYOR ve kullanmamali: okuyucu etiketin cevresindeki
 * satirlara da bakiyor (birim fiyat, gramaj, kunye) ve rehbere sigmayan bir
 * satir kirpimda yok olurdu. Kirpim yalnizca INSANIN baktigi serit icin.
 *
 * @return true = yazildi. false = kirpilamadi; cagiran taraf kirpimsiz
 *   kucultmeye DUSMELI, seridi bos birakmamali.
 */
expect suspend fun cropToGuide(
    source: ByteArray,
    destPath: String,
    guide: GuideBox,
    maxLongEdge: Int = MAX_LONG_EDGE,
): Boolean
