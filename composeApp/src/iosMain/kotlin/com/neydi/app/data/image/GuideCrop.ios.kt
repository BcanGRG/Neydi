package com.neydi.app.data.image

/**
 * iOS rehber kirpimi Faz 9'da gelecek (`CGImageCreateWithImageInRect`).
 *
 * false donuyor, ISTISNA ATMIYOR - `downscaleForOcr` ile ayni sozlesme:
 * "kirpamadim, kirpimsiz kucultmeye dus". Serit o zaman merkez kirpimla
 * cizilir; gosterdigi sey kullanicinin kadraja oturttugu sey olmaz ama serit
 * bos da kalmaz.
 *
 * DIKKAT (Faz 9): sira Android'deki gibi ZORUNLU - once yon piksele, sonra
 * kirpim. `UIImage.imageOrientation` etiketini birakip `CGImage`i dogrudan
 * kirpmak dikdortgeni doksan derece yanlis yere koyar ve bu SESSIZ bir hata
 * olur: cikan serit yine bir seyler gosterir.
 */
actual suspend fun cropToGuide(
    source: ByteArray,
    destPath: String,
    guide: GuideBox,
    maxLongEdge: Int,
): Boolean = false
