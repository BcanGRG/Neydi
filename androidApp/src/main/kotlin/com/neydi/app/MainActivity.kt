package com.neydi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.neydi.app.data.db.initAndroidDbContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // F2.6'da Koin devralacak; simdilik veritabaninin ihtiyaci olan tek sey bu.
        initAndroidDbContext(this)
        setContent { App() }
    }
}
