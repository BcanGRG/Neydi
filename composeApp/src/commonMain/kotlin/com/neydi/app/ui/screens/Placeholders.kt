package com.neydi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.theme.Spacing

/**
 * ISKELET EKRANLARI - hepsi gecici.
 *
 * Claude Design'dan gelen tasarim sistemi ve ekranlar bunlarin yerini alacak.
 * Simdilik amac tek sey: Nav3 grafiginin iki platformda da ayakta oldugunu
 * ve temanin uygulandigini kanitlamak.
 */
@Composable
private fun AppScaffold(
    baslik: String,
    aciklama: String,
    modifier: Modifier = Modifier,
    aksiyonlar: @Composable () -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding() // iOS home indicator + notch: pazarlik konusu degil
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(baslik, style = MaterialTheme.typography.headlineMedium)
            Text(
                aciklama,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            aksiyonlar()
        }
    }
}

@Composable
fun MissingItemsScreen(onEkle: () -> Unit, onBosver: () -> Unit) = AppScaffold(
    baslik = "Eksik olabilir",
    aciklama = "Evden cikmadan onceki son kontrol. Hicbir sey uygun degilse bu ekran ACILMAZ.",
) {
    NeydiButton("Ekle", onEkle)
    NeydiButton("Bosver", onBosver)
}

@Composable
fun FinishShoppingScreen(tripId: String?, onOnayla: () -> Unit) = AppScaffold(
    baslik = "Alisverisi bitir",
    aciklama = if (tripId == null) {
        "Fis cek / fissiz bitir. Fotograf ASLA bloklamaz - alisveris aninda kapanir."
    } else {
        "Gecmisten okuma modu: $tripId"
    },
) {
    NeydiButton("Onayla ve bitir", onOnayla)
}

@Composable
fun HistoryScreen(onGeri: () -> Unit) = AppScaffold(
    baslik = "Gecmis",
    aciklama = "Yanlis okunmus bir fise geri donmenin tek yolu. Uygulamanin en ucuz ekrani.",
) {
    NeydiButton("Geri", onGeri)
}

@Composable
fun SettingsScreen(onGeri: () -> Unit) = AppScaffold(
    baslik = "Ayarlar",
    aciklama = "Hane, her zamankiler, onerilmeyenler, magazalar, gizlilik.",
) {
    NeydiButton("Geri", onGeri)
}

@Composable
fun SetupScreen(onBitir: () -> Unit) = AppScaffold(
    baslik = "Kurulum",
    aciklama = "Uc adim, bir daha gorunmez. Amac: 15. gezide degil 3. gezide akilli hissetmek.",
) {
    NeydiButton("Bitir", onBitir)
}
