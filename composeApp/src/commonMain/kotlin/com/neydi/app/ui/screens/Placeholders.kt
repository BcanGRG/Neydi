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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
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
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
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
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            actions()
        }
    }
}

@Composable
fun MissingItemsScreen(onAdd: () -> Unit, onCancel: () -> Unit) = AppScaffold(
    title = "Eksik olabilir",
    description = "Evden cikmadan onceki son kontrol. Hicbir sey uygun degilse bu ekran ACILMAZ.",
) {
    NeydiButton("Ekle", onAdd)
    NeydiButton("Bosver", onCancel)
}

@Composable
fun SettingsScreen(onBack: () -> Unit) = AppScaffold(
    title = "Ayarlar",
    description = "Hane, her zamankiler, onerilmeyenler, magazalar, gizlilik.",
) {
    NeydiButton("Geri", onBack)
}

@Composable
fun SetupScreen(onFinish: () -> Unit) = AppScaffold(
    title = "Kurulum",
    description = "Uc adim, bir daha gorunmez. Amac: 15. gezide degil 3. gezide akilli hissetmek.",
) {
    NeydiButton("Bitir", onFinish)
}

// --- Onizlemeler ------------------------------------------------------------
//
// Iskelet olsalar da onizlemeleri VAR: sozlesme her bilesene @PreviewLightDark
// istiyor ve bu dosya tek istisnaydi. Gercek ekranlar geldiginde (F6.4 / F6.6 /
// F6.7) bu onizlemeler de onlarla birlikte degisecek.

@PreviewLightDark
@Composable
private fun MissingItemsPreview() = NeydiPreview {
    MissingItemsScreen(onAdd = {}, onCancel = {})
}

@PreviewLightDark
@Composable
private fun SettingsPreview() = NeydiPreview {
    SettingsScreen(onBack = {})
}

@PreviewLightDark
@Composable
private fun SetupPreview() = NeydiPreview {
    SetupScreen(onFinish = {})
}
