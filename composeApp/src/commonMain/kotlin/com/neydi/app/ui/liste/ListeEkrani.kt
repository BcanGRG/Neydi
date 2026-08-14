package com.neydi.app.ui.liste

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.ui.components.ListRow
import com.neydi.app.ui.components.ListItemRow
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.SectionHeader
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.SpacingExtra
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ListeEkrani(
    onAlisveriseCik: () -> Unit,
    onGecmis: () -> Unit,
    onAyarlar: () -> Unit,
    vm: ListeViewModel = koinViewModel(),
) {
    val durum by vm.durum.collectAsStateWithLifecycle()
    val girdi by vm.girdi.collectAsStateWithLifecycle()
    val oneriler by vm.oneriler.collectAsStateWithLifecycle()

    ListeIcerik(
        durum = durum,
        girdi = girdi,
        oneriler = oneriler,
        onGirdiDegisti = vm::girdiDegisti,
        onEkle = vm::ekle,
        onOneriSecildi = vm::oneridenEkle,
        onIsaretle = vm::isaretle,
        onAlisveriseCik = onAlisveriseCik,
        onGecmis = onGecmis,
        onAyarlar = onAyarlar,
    )
}

/**
 * Durumsuz govde: preview ve test buradan geciyor, ViewModel'siz.
 *
 * ISARETLILER REYONDAN CIKIP "Alindi"ya iner. Reyon icinde kalsalardi liste
 * alisveris ilerledikce delik desik gorunur ve "daha ne kaldi" sorusu gozle
 * cevaplanamazdi.
 */
@Composable
internal fun ListeIcerik(
    durum: ListeDurumu,
    girdi: String,
    oneriler: List<com.neydi.app.data.db.CatalogSeed>,
    onGirdiDegisti: (String) -> Unit,
    onEkle: (String) -> Unit,
    onOneriSecildi: (com.neydi.app.data.db.CatalogSeed) -> Unit,
    onIsaretle: (String, Boolean) -> Unit,
    onAlisveriseCik: () -> Unit,
    onGecmis: () -> Unit,
    onAyarlar: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            // imePadding: klavye acilinca hizli ekleme alani onun ustune cikar.
            // Olmasaydi kullanici yazdigini goremezdi.
            modifier = Modifier.fillMaxSize().imePadding(),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                // YALNIZCA UST inset: liste alt cubugun ALTINDAN kaysin, ama
                // icerik durum cubugunun arkasinda baslamasin. Alt inset
                // HizliEkle'ye ait - o cubugun ALTINDA kalamaz.
                contentPadding = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top)
                    .asPaddingValues(),
            ) {
                item { ListeBasligi(durum.toplamSatir, onAlisveriseCik, onGecmis, onAyarlar) }

                if (durum.bosMu) {
                    item { BosDurum() }
                }

                durum.bolumler.forEach { bolum ->
                    // BOS BOLUM CIZILMEZ - bolumler zaten filtrelenmis geliyor.
                    item(key = "b-${bolum.baslik}") {
                        SectionHeader(bolum.baslik, bolum.satirlar.size)
                    }
                    items(bolum.satirlar, key = { it.id }) { satir ->
                        ListItemRow(row = satir.row, onToggle = { onIsaretle(satir.id, true) })
                    }
                }

                if (durum.alinanlar.isNotEmpty()) {
                    item(key = "b-alindi") { SectionHeader("Alındı", durum.alinanlar.size) }
                    items(durum.alinanlar, key = { it.id }) { satir ->
                        ListItemRow(row = satir.row, onToggle = { onIsaretle(satir.id, false) })
                    }
                }
            }

            HizliEkle(
                // Alt sistem cubugu insetini BURASI tasiyor. Ilk denemede
                // yoktu ve alan gezinme cubugunun altinda kaldi - cihazda
                // goruldu, derleme sessizdi.
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                ),
                girdi = girdi,
                oneriler = oneriler,
                onGirdiDegisti = onGirdiDegisti,
                onEkle = onEkle,
                onOneriSecildi = onOneriSecildi,
            )
        }
    }
}

@Composable
private fun ListeBasligi(
    adet: Int,
    onAlisveriseCik: () -> Unit,
    onGecmis: () -> Unit,
    onAyarlar: () -> Unit,
) {
    Column(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
        Text("Liste", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = if (adet == 0) "Henüz bir şey yok" else "$adet ürün",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            NeydiButton("Alışverişe çık", onAlisveriseCik)
            NeydiButton(
                text = "Geçmiş",
                onClick = onGecmis,
                container = MaterialTheme.colorScheme.surfaceVariant,
                content = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            NeydiButton(
                text = "Ayarlar",
                onClick = onAyarlar,
                container = MaterialTheme.colorScheme.surfaceVariant,
                content = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Uc bos durumun ilki. Digerleri (kurulum atlandi, dongu ortasi) F3.6'da.
 * Bos ekran OLU HISSETTIRMEMELI - ne yapilacagini soyluyor.
 */
@Composable
private fun BosDurum() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = Spacing.lg,
            vertical = SpacingExtra.emptyStateBlock,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Aşağıya yazmaya başla.\n\"2 kg elma\" gibi miktarı da yazabilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// --- Preview ---------------------------------------------------------------

@PreviewLightDark
@Composable
private fun ListeBosPreview() = NeydiPreview(padding = Spacing.xs) {
    ListeIcerik(
        durum = ListeDurumu(yukleniyor = false),
        girdi = "", oneriler = emptyList(),
        onGirdiDegisti = {}, onEkle = {}, onOneriSecildi = {}, onIsaretle = { _, _ -> },
        onAlisveriseCik = {}, onGecmis = {}, onAyarlar = {},
    )
}

@PreviewLightDark
@Composable
private fun ListeDoluPreview() = NeydiPreview(padding = Spacing.xs) {
    ListeIcerik(
        durum = ListeDurumu(
            bolumler = listOf(
                ListeBolumu(
                    "Meyve-Sebze",
                    listOf(
                        UiSatir("1", ListRow("Domates", quantity = "1 kg")),
                        UiSatir("2", ListRow("Salatalık")),
                    ),
                ),
                ListeBolumu(
                    "Fırın-Ekmek",
                    listOf(
                        UiSatir("3", ListRow("Tam Buğday Ekmek", isStaple = true)),
                        UiSatir("4", ListRow("Simit", quantity = "4x", addedByInitial = "A")),
                    ),
                ),
            ),
            alinanlar = listOf(UiSatir("5", ListRow("Süt", quantity = "2x", checked = true))),
            yukleniyor = false,
        ),
        girdi = "", oneriler = emptyList(),
        onGirdiDegisti = {}, onEkle = {}, onOneriSecildi = {}, onIsaretle = { _, _ -> },
        onAlisveriseCik = {}, onGecmis = {}, onAyarlar = {},
    )
}
