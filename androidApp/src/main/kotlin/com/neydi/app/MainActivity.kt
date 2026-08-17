package com.neydi.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.neydi.app.data.ocr.dumpTagFixtures
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // GECICI (E12): `etiket-in/RUN` dosyasi varsa etiket fotograflarini
        // OCR'dan gecirip fikstur doker. Fikstur uretildikten sonra bu blok ve
        // `TagFixtureDump.android.kt` SILINECEK.
        //
        // Tetikleyici bir DOSYA, cunku ekran ya da menu maddesi eklemek
        // uygulamaya kalici bir yuzey sokardi; dosya `adb push` ile konuyor ve
        // dokum bitince siliniyor, yani izi kalmiyor.
        if (File(getExternalFilesDir(null), "etiket-in/RUN").exists()) {
            lifecycleScope.launch {
                Log.i("E12", "dump basliyor")
                Log.i("E12", dumpTagFixtures(this@MainActivity))
                File(getExternalFilesDir(null), "etiket-in/RUN").delete()
                Log.i("E12", "dump bitti")
            }
        }

        setContent { App() }
    }
}
