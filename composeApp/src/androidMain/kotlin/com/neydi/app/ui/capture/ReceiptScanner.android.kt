package com.neydi.app.ui.capture

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File

/**
 * ML Kit Belge Tarayicisi (F4.18).
 *
 * MOD SECIMI OLCULMEDI - bkz. [SCANNER_MODE].
 *
 * GALERIDEN SECIM KAPALI: bu ekranin isi FIS CEKMEK. Galeriden secim, fisin
 * ne zaman cekildigi bilgisini (F5.8'in fiyat gozlemi icin kullandigi tarih)
 * belirsiz hale getirirdi.
 *
 * SAYFA SINIRI ALTI: uzun fis birden fazla sayfa olabiliyor ama bu AYNI
 * tarayici oturumunda oluyor - kullanici uygulamadan cikmiyor, sayfalari arka
 * arkaya cekiyor. Parca kavraminin istisnaya inmesi bu (F4.17); sinir yine de
 * gerekli, cunku sinirsiz sayfa kazayla yuzlerce kare demek.
 *
 * PDF ISTENMIYOR: yalnizca JPEG. PDF uretmek tarayiciya fazladan is yaptiriyor
 * ve bizim onunla yapacagimiz hicbir sey yok - OCR bitmap istiyor.
 */
@Composable
actual fun rememberReceiptScanner(
    onResult: (ScanResult) -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scanner = remember {
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(false)
                .setPageLimit(PAGE_LIMIT)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .setScannerMode(SCANNER_MODE)
                .build(),
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        if (activityResult.resultCode != Activity.RESULT_OK) {
            onResult(ScanResult(emptyList()))
            return@rememberLauncherForActivityResult
        }
        val scanned = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        onResult(ScanResult(scanned.copyPagesInto(context)))
    }

    return {
        val activity = context.findActivity()
        if (activity == null) {
            onUnavailable()
        } else {
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener { sender ->
                    launcher.launch(IntentSenderRequest.Builder(sender).build())
                }
                // TARAYICI ACILAMAZSA SESSIZ KALMIYOR: Play Services yoksa ya
                // da cihaz desteklemiyorsa kendi kameramiza dusuyoruz.
                .addOnFailureListener { onUnavailable() }
        }
    }
}

/**
 * Tarayicinin verdigi sayfalari UYGULAMANIN kendi dizinine kopyalar.
 *
 * NEDEN KOPYA: donen `content://` URI'leri gecici izinlerle geliyor ve
 * uygulama yeniden baslayinca cozulemez hale geliyor. `Receipt.imagePath`
 * kalici bir yol tasimak zorunda - bu ders F4.2'de bir kez odendi.
 */
private fun GmsDocumentScanningResult?.copyPagesInto(context: Context): List<String> {
    val pages = this?.pages.orEmpty()
    val dir = File(context.filesDir, "receipts").apply { mkdirs() }
    return pages.mapIndexedNotNull { index, page ->
        runCatching {
            val dest = File(dir, "fis-${System.currentTimeMillis()}-$index.jpg")
            context.contentResolver.openInputStream(page.imageUri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            logPageMeasurement(dest)
            dest.absolutePath
        }.getOrNull()
    }
}

/**
 * OLCUM KAYDI (gecici, F4.20 olcumu icin).
 *
 * BURADA, cunku tek sansimiz burasi: `attachReceiptToTrip` sayfayi AYNI yola
 * yeniden yaziyor (`destPath == path`), yani tarayicinin verdigi orijinal bu
 * satirdan sonra kayboluyor. Karsilastirilacak sayi ham piksel degil, SATIR
 * BASINA piksel - okuma tarafindaki kayit (`NeydiOlcum okuma ...`) ile birlikte
 * okunmali.
 *
 * KISISEL VERI YAZILMIYOR: yalnizca olculer, dosya adi ya da icerik degil.
 */
private fun logPageMeasurement(dest: File) {
    val boyut = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(dest.absolutePath, boyut)
    Log.i(
        "NeydiOlcum",
        "tarayici mod=$SCANNER_MODE ${boyut.outWidth}x${boyut.outHeight} bayt=${dest.length()}",
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Tek oturumda cekilebilecek en fazla sayfa. */
private const val PAGE_LIMIT = 6

/**
 * Tarayici modu. **OLCULMEDI** - olcum icin degistirilecek tek yer burasi.
 *
 * SECILDIGINDE GEREKCE SUYDU: `SCANNER_MODE_FULL` golge, leke ve parmak
 * temizligini de yapiyor; termal fis zaten dusuk kontrastli ve kagidin
 * uzerindeki golge OCR'in en cok takildigi seylerden biri, "bunu bedavaya
 * alabiliyoruz" denmisti. Iki tarafi da olculmedi:
 *
 *  1. **Bedava degil.** FULL, ek ML modeli INDIRMESI gereken tek mod. Bu,
 *     `MlKitReceiptReader`da yazili karara dogrudan zit: metin modeli bilerek
 *     GOMULU secildi cunku *"kasa kuyrugunda 'model indiriliyor' gormek
 *     istemiyoruz"*. Ayni odunlesme, iki dosyada ters karar.
 *  2. **Temizlik zarar da verebilir.** FULL'un leke/parmak silmesi bir
 *     doldurma (inpainting) modeli, ve termal fisin ince, soluk, noktali
 *     glifleri bir temizleyiciye lekeye BENZER. Silinen sey harf olabilir.
 *
 * Uc mod da ayni fiziksel fisle denenmeli; karsilastirilacak sayi ham piksel
 * degil, **satir basina piksel** ve **ayristirilan kalem sayisi**. Cikti
 * cozunurlugunu Google mod basina belgelemiyor - bakilacak bir cevap yok,
 * gercekten olculmesi gerekiyor.
 *
 * Kayitlar: `adb logcat -s NeydiOlcum`.
 */
private const val SCANNER_MODE = GmsDocumentScannerOptions.SCANNER_MODE_FULL
