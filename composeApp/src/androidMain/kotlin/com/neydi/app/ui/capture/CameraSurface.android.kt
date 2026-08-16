package com.neydi.app.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { allowed ->
        granted = allowed
        controller.denied = !allowed
    }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(controller) {
        controller.capturer = { destPath -> imageCapture.writeTo(destPath, executor) }
        onDispose {
            controller.capturer = null
            controller.ready = false
        }
    }

    if (!granted) return

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
                    provider.bindToLifecycle(
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
