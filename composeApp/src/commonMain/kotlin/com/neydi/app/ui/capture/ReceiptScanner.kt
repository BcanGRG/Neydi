package com.neydi.app.ui.capture

import androidx.compose.runtime.Composable

/**
 * Belge tarayici sonucu.
 *
 * [pagePaths] kirpilmis, duzeltilmis ve temizlenmis sayfalarin DOSYA yollari -
 * cekim sirasinda. Bos liste = kullanici vazgecti.
 */
data class ScanResult(val pagePaths: List<String>)

/**
 * Fis tarayicisini acar (F4.18).
 *
 * NEDEN HAZIR TARAYICI: kullanicinin istedigi uc sey - fisin kenarlarini bulup
 * arka plani atmak, kirpilmis sonucu GOSTERIP duzelttirmek, ve termal fisteki
 * golge/lekeyi temizlemek - kendi kameramizda sifirdan yazilacak isler.
 * Ustelik kullanicinin asil sikayeti "cektigimi anlamiyorum"du; kirpilmis
 * sonucun ekranda gorunmesi tam olarak o sorunun cevabi.
 *
 * BEDELI KAYDA GECSIN: tarayici KENDI tam ekran arayuzunu getiriyor, yani
 * tasarima birebir yazilan kamera ekraninin (Ekran 4) yerine geciyor. Bu
 * bilincli bir sapma ve tasarima bulgu olarak yazildi. Kendi kameramiz
 * tarayici acilamadiginda (Play Services yoksa) yedek olarak duruyor.
 *
 * @return tarayiciyi baslatan lambda. Sonuc [onResult] ile geliyor.
 */
@Composable
expect fun rememberReceiptScanner(
    onResult: (ScanResult) -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit
