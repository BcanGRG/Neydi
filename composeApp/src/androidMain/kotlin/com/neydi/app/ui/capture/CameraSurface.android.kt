package com.neydi.app.ui.capture

import android.content.Intent
import android.Manifest
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import android.view.Surface
import android.provider.Settings
import android.net.Uri
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume

/**
 * CameraX onizlemesi ve tam cozunurlukte cekim.
 *
 * ARKA KAMERA BURADA GARANTI. Sistem kamerasina `FileKitCameraFacing.Back`
 * veriliyordu ve cihaz bunu YOK SAYIYORDU - `ACTION_IMAGE_CAPTURE`in lens
 * ipucu tavsiye niteliginde ve uretici kamera uygulamalari cogunlukla
 * dinlemiyor. `CameraSelector.DEFAULT_BACK_CAMERA` bir ipucu degil, secimin
 * kendisi.
 *
 * `CAPTURE_MODE_MAXIMIZE_QUALITY`: etiket okumasinda belirleyici sey karakter
 * basina dusen piksel. Hizli cekim modu JPEG'i daha cok sikistirir ve tam da
 * kaybetmeye tahammulumuz olmayan ayrintiyi - ince, dusuk kontrastli glif
 * kenarlarini - kaybettirir.
 */
@Composable
actual fun CameraSurface(controller: CaptureController, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val activity = LocalActivity.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { allowed ->
        granted = allowed
        controller.denied = !allowed
        // KALICI RET SORMADAN ANLASILMIYOR. `shouldShowRequestRationale`
        // reddin ardindan false donuyorsa sistem bir daha sormayacak demek -
        // "tekrar dene" dugmesi o noktadan sonra hicbir sey yapmiyor ve
        // kullanici uygulamanin bozuk oldugunu dusunuyor.
        controller.permanentlyDenied = !allowed && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
    }

    DisposableEffect(activity) {
        controller.settingsOpener = {
            activity?.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity.packageName, null),
                ),
            )
        }
        onDispose { controller.settingsOpener = null }
    }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // TARGET ROTATION HER KOMPOZISYONDA TAZELENIYOR - ve bu bir veri hatasinin
    // duzeltmesi, bir incelik degil.
    //
    // Manifest `configChanges`e `orientation` yaziyor, yani telefon donunce
    // Activity YENIDEN YARATILMIYOR. `ImageCapture` keyless bir `remember`
    // icinde, `AndroidView` fabrikasi bir kez kosuyor. Yani hicbir sey
    // `targetRotation`i guncellemiyordu ve CameraX kareye BAGLANMA anindaki
    // donusu EXIF olarak yaziyordu. `PreviewView` kendi icinde telafi ettigi
    // icin ONIZLEME dogru gorunuyor - yalan yalnizca dosyada.
    //
    // Bedeli: `downscaleForOcr` o EXIF'i piksele isliyor, ML Kit kareyi YAN
    // goruyor. Fiste olculmustu (F4.20): sayfa sekiz dev satira cokuyor.
    // Donusu GORUNUMDEN okuyoruz, `windowManager.defaultDisplay`den degil:
    // ikincisi API 30'da deprecate edildi ve projenin sifir-uyari kurali var.
    // `View.getDisplay()` deprecate degil ve zaten dogru ekrani veriyor -
    // coklu ekranda pencerenin gercekte hangi ekranda oldugunu bilen o.
    var camera by remember { mutableStateOf<Camera?>(null) }
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    LaunchedEffect(configuration) {
        imageCapture.targetRotation = view.display?.rotation ?: Surface.ROTATION_0
    }

    DisposableEffect(controller) {
        controller.capturer = { destPath -> imageCapture.writeTo(destPath, executor) }
        onDispose {
            controller.capturer = null
            controller.ready = false
            // KAMERAYI SERBEST BIRAKIYORUZ. Onceden yalnizca tutamak
            // birakiliyordu; `Preview` ve `ImageCapture` lifecycle'a BAGLI
            // kaliyordu. Ekrandan cikinca yayinin surmesi hem gizlilik
            // gostergesini yanik birakir hem pil yakar - ve kullanicinin
            // "kamerayi kapattim" beklentisini bosa cikarir.
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
        }
    }

    if (!granted) return

    // FENER, cekim flasi DEGIL (karar 60).
    //
    // `ImageCapture.FLASH_MODE_ON` yalnizca deklansor aninda patlar; kullanici
    // o ana kadar kadraji KARANLIKTA hizalamis olur. Kararin gerekcesi ise
    // dogrudan kadrajla ilgili: *"raf etiketi parlak, market isigi dusuk"* ve
    // olcumdeki tek basarisiz cekim muhtemelen bulanikti. Surekli isik hem
    // onizlemeyi hem kareyi aydinlatiyor - iki hal, tek anlam.
    LaunchedEffect(controller.torch, camera) {
        camera?.cameraControl?.enableTorch(controller.torch)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).also { view ->
                // FILL_CENTER: etiket kadraji doldurmali. FIT_CENTER kenarlarda
                // bos birakip cerceve rehberini yalan soyler hale getirirdi -
                // kullanici cerceveye hizaladigini sanip disina tasar.
                view.scaleType = PreviewView.ScaleType.FILL_CENTER
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build()
                        .also { it.surfaceProvider = view.surfaceProvider }
                    provider.unbindAll()
                    camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture,
                    )
                    controller.ready = true
                }, executor)
            }
        },
    )
}

/**
 * Kareyi diske yazar.
 *
 * DOSYAYA YAZIYOR, bellege DEGIL: kare tam cozunurlukte on megabayti
 * bulabiliyor ve onu bir `ByteArray` olarak tasimak cekim aninda ikinci bir tam
 * kopya demek. Zaten hedef yol bir dosya.
 */
private suspend fun ImageCapture.writeTo(destPath: String, executor: Executor): Boolean =
    suspendCancellableCoroutine { continuation ->
        val file = File(destPath)
        file.parentFile?.mkdirs()
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        takePicture(
            options,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                    continuation.resume(true)
                }

                override fun onError(exception: ImageCaptureException) {
                    // YUTULUYOR ama SESSIZ DEGIL: cagiran taraf false gorup
                    // kullaniciya soyluyor. Burada exception firlatmak cekim
                    // ekranini coketirdi ve kullanici o ana kadar cektigi
                    // kareleri kaybederdi.
                    continuation.resume(false)
                }
            },
        )
    }
