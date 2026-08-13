package com.neydi.app.ui.theme

import androidx.compose.runtime.Composable

/**
 * Sistem cubuklarinin (status bar + gezinme cubugu) gorunumunu temaya baglar.
 *
 * NEDEN COMPOSE TARAFINDA, Activity.onCreate'te DEGIL:
 * AndroidManifest'te `configChanges` icinde `uiMode` var, yani karanlik moda
 * gecince Activity YENIDEN YARATILMIYOR. onCreate'te bir kez cagrilan
 * enableEdgeToEdge() acilistaki stilde donup kaliyor — Compose renkleri
 * guncelliyor ama sistem cubuklari eski stilde kaliyor. Sonuc: karanlik modda
 * siyah zemin uzerinde koyu status bar ikonlari ve altta acik renk bant.
 *
 * Burasi temanin darkTheme degerine bagli oldugu icin, tema nasil degisirse
 * degissin (sistem ayari, uygulama ici secim, config degisikligi) cubuklar
 * onu takip eder.
 */
@Composable
expect fun ApplySystemBarAppearance(darkTheme: Boolean)
