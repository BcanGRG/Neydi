package com.neydi.app.ui.liste

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.data.db.CatalogSeed
import com.neydi.app.data.db.Category
import com.neydi.app.data.panoListeMi
import com.neydi.app.ui.components.ListItemRow
import com.neydi.app.ui.components.ListRow
import com.neydi.app.ui.components.NeydiButton
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.SectionHeader
import com.neydi.app.ui.theme.Motion
import com.neydi.app.ui.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

// ModalBottomSheet hala @ExperimentalMaterial3Api. Sheet'i kendimiz yazmak
// surukleme, scrim ve geri tusu davranisini yeniden uretmek demekti.
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
    val kategoriler by vm.kategoriler.collectAsStateWithLifecycle()
    val tahmin by vm.tahmin.collectAsStateWithLifecycle()
    val sheetAcik by vm.sheetAcik.collectAsStateWithLifecycle()
    val sheetKategori by vm.sheetKategori.collectAsStateWithLifecycle()
    val sheetUrunler by vm.sheetUrunler.collectAsStateWithLifecycle()
    val ozet by vm.ozet.collectAsStateWithLifecycle()

    // Pano bir KEZ, ekran acilirken okunuyor. Her karede okumak hem pahali hem
    // de bazi sistemlerde "pano okundu" bildirimi tetikliyor.
    // Sheet'lere verilecek alt bosluk: BURADA okunuyor, sheet icinde degil.
    val altInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val pano = LocalClipboardManager.current
    var panoMetni by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        panoMetni = pano.getText()?.text?.takeIf { panoListeMi(it) }
    }

    ListeIcerik(
        durum = durum,
        girdi = girdi,
        oneriler = oneriler,
        kategoriler = kategoriler,
        panoMetni = panoMetni,
        onGirdiDegisti = vm::girdiDegisti,
        onEkle = vm::ekle,
        onOneriSecildi = vm::oneridenEkle,
        onKategoriSecildi = vm::kategoriSecildi,
        onIsaretle = vm::isaretle,
        onPano = {
            panoMetni?.let(vm::panodanEkle)
            panoMetni = null
        },
        onAlisverisModu = vm::alisverisModunuDegistir,
        tahmin = tahmin,
        onSheetAc = vm::sheetAc,
        onBitir = vm::alisverisiBitir,
        onGecmis = onGecmis,
        onAyarlar = onAyarlar,
    )

    // Sheet, EKRAN DEGIL: liste arkada gorunur kaliyor.
    if (sheetAcik) {
        ModalBottomSheet(
            onDismissRequest = vm::sheetKapat,
            // ZEMIN RENGI ACIKCA VERILIYOR. M3 varsayilani surfaceContainerLow
            // istiyor; bu palet o tonal token'lari TANIMLAMIYOR, o yuzden M3
            // kendi baseline'ina (mor tonlu) dusuyordu ve sheet uygulamanin
            // geri kalanina ait gorunmuyordu. Cihazda goruldu.
            containerColor = MaterialTheme.colorScheme.surface,
            // Inset'i SHEET tasimali, icerik degil: ModalBottomSheet pencere
            // inset'lerini kendisi tuketiyor, o yuzden icerideki
            // windowInsetsPadding sifir goruyor ve kacis butonu gezinme
            // cubugunun altinda kaliyordu. Cihazda goruldu; varsayilan
            // BottomSheetDefaults.windowInsets alt kenari kapsamiyor.
        ) {
            EkleSheetIcerik(
                // Inset SHEET DISINDA okunup duz bosluk olarak geciliyor.
                // ModalBottomSheet'in kendi contentWindowInsets'i bu agacta
                // etki etmedi (uc farkli deneme, ucu de cihazda kontrol edildi);
                // sheet icerigi ekranin dibine oturup gezinme cubugunun altinda
                // kaliyordu. Disaridan okunan deger belirsizlik birakmiyor.
                altBosluk = altInset,
                kategoriler = kategoriler,
                secili = sheetKategori,
                urunler = sheetUrunler,
                onKategori = vm::sheetKategoriSec,
                onGeriKategoriler = vm::sheetGeri,
                onUrun = vm::sheettenEkle,
                onSerbestMetin = vm::sheetKapat,
            )
        }
    }

    // Ozet karti TEK SEFERLIK: kapatilinca bir daha acilmiyor.
    ozet?.let { o ->
        ModalBottomSheet(
            onDismissRequest = vm::ozetiKapat,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            OzetKarti(
                altBosluk = altInset,
                alinanSayisi = o.alinanSayisi,
                toplamSayisi = o.toplamSayisi,
                tutarKurus = o.tutarKurus,
                sureDakika = o.sureDakika,
                onKapat = vm::ozetiKapat,
            )
        }
    }
}

/** Durumsuz govde: preview ve test buradan geciyor, ViewModel'siz. */
@Composable
internal fun ListeIcerik(
    durum: ListeDurumu,
    girdi: String,
    oneriler: List<CatalogSeed>,
    kategoriler: List<Category>,
    panoMetni: String?,
    onGirdiDegisti: (String) -> Unit,
    onEkle: (String) -> Unit,
    onOneriSecildi: (CatalogSeed) -> Unit,
    onKategoriSecildi: (Category) -> Unit,
    onIsaretle: (String, Boolean) -> Unit,
    onPano: () -> Unit,
    onAlisverisModu: (Boolean) -> Unit,
    tahmin: SepetTahmini,
    onSheetAc: () -> Unit,
    onBitir: () -> Unit,
    onGecmis: () -> Unit,
    onAyarlar: () -> Unit,
) {
    EkraniUyanikTut(durum.alisverisModu)
    val haptik = LocalHapticFeedback.current

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top)
                    .asPaddingValues(),
            ) {
                item {
                    ListeBasligi(
                        durum = durum,
                        onAlisverisModu = onAlisverisModu,
                        onSheetAc = onSheetAc,
                        onGecmis = onGecmis,
                        onAyarlar = onAyarlar,
                    )
                }

                // Tahmin BASLIKTAN sonra, satirlardan once: rakam listeye
                // bakmadan once gorulmeli, alt tarafta kalirsa hic gorulmez.
                if (!durum.bosMu) {
                    item(key = "tahmin") {
                        TahminiSepet(
                            tutarKurus = tahmin.tutarKurus,
                            fiyatliSayisi = tahmin.fiyatliSayisi,
                            toplamSayisi = durum.toplamSatir,
                        )
                    }
                }

                if (durum.bosMu) {
                    item {
                        BosDurum(
                            tur = durum.bosTur,
                            kategoriler = kategoriler,
                            panoVar = panoMetni != null,
                            onKategori = onKategoriSecildi,
                            onPano = onPano,
                        )
                    }
                } else if (panoMetni != null && !durum.alisverisModu) {
                    // Liste doluyken de yapistirilabilir - ama alisveris
                    // modunda ASLA: reyonda toplu ekleme yapilmaz.
                    item {
                        PanoCipi(
                            adet = com.neydi.app.data.panoSatirlari(panoMetni).size,
                            onPano = onPano,
                        )
                    }
                }

                durum.bolumler.forEach { bolum ->
                    item(key = "b-${bolum.baslik}") {
                        // Baslik da animasyonlu: satir kayarken baslik ziplasaydi
                        // hareket iki parcaya bolunur ve daha rahatsiz olurdu.
                        SectionHeader(
                            title = bolum.baslik,
                            count = bolum.satirlar.size,
                            modifier = Modifier.animateItem(
                                placementSpec = tween(Motion.REORDER_MS),
                            ),
                        )
                    }
                    items(bolum.satirlar, key = { it.id }) { satir ->
                        ListItemRow(
                            // ISARETLENEN SATIR "Alindi"ya KAYARAK iner.
                            // Anahtar iki bolumde de ayni (TripLine id), o yuzden
                            // LazyColumn bunu yeni bir oge degil YER DEGISIMI
                            // goruyor ve animateItem araligi enterpole ediyor.
                            // Isinlanmamali: goz satiri takip edebilmeli, yoksa
                            // "ne oldu, nereye gitti" hissi kaliyor.
                            modifier = Modifier.animateItem(
                                placementSpec = tween(Motion.REORDER_MS),
                            ),
                            row = satir.row,
                            shoppingMode = durum.alisverisModu,
                            onToggle = {
                                // Haptik onay: reyonda goz listede degil rafta.
                                // Dokunusun islendigini parmak soyluyor.
                                // SNACKBAR YOK - bir gezide 20 isaretleme var,
                                // her birine snackbar ekrani felce ugratirdi.
                                haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onIsaretle(satir.id, !satir.row.checked)
                            },
                        )
                    }
                }

                if (durum.alinanlar.isNotEmpty()) {
                    item(key = "b-alindi") {
                        SectionHeader(
                            title = "Alındı",
                            count = durum.alinanlar.size,
                            modifier = Modifier.animateItem(
                                placementSpec = tween(Motion.REORDER_MS),
                            ),
                        )
                    }
                    items(durum.alinanlar, key = { it.id }) { satir ->
                        ListItemRow(
                            modifier = Modifier.animateItem(
                                placementSpec = tween(Motion.REORDER_MS),
                            ),
                            row = satir.row,
                            onToggle = {
                                haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onIsaretle(satir.id, false)
                            },
                        )
                    }
                }
            }

            // Alisveris modunda hizli ekleme YOK: reyonda liste yazilmaz,
            // okunur. Klavye ekranin yarisini yerdi.
            if (!durum.alisverisModu) {
                HizliEkle(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    ),
                    girdi = girdi,
                    oneriler = oneriler,
                    onGirdiDegisti = onGirdiDegisti,
                    onEkle = onEkle,
                    onOneriSecildi = onOneriSecildi,
                )
            } else {
                AlisverisAltCubugu(
                    kalan = durum.kalanSatir,
                    onBitir = onBitir,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    ),
                )
            }
        }
    }
}

@Composable
private fun ListeBasligi(
    durum: ListeDurumu,
    onAlisverisModu: (Boolean) -> Unit,
    onSheetAc: () -> Unit,
    onGecmis: () -> Unit,
    onAyarlar: () -> Unit,
) {
    Column(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
        Text(
            text = if (durum.alisverisModu) "Alışveriş" else "Liste",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = when {
                durum.alisverisModu -> "${durum.kalanSatir} kaldı"
                durum.toplamSatir == 0 -> "Henüz bir şey yok"
                else -> "${durum.toplamSatir} ürün"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Alisveris modunda gezinme butonlari GIZLI: reyonda yanlislikla
        // Ayarlar'a dusmek listeyi kaybetmek gibi hissettirir.
        if (!durum.alisverisModu) {
            Row(
                modifier = Modifier.padding(top = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                NeydiButton("Alışveriş modu", { onAlisverisModu(true) })
                NeydiButton(
                    text = "Reyonlardan ekle",
                    onClick = onSheetAc,
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
}

/**
 * Panoda liste varsa tek dokunusluk cip.
 *
 * Ucuncu satir esigi PanoAyristirici'da: iki satirlik pano cogu zaman
 * kopyalanmis bir cumle, liste degil.
 */
@Composable
private fun PanoCipi(adet: Int, onPano: () -> Unit) {
    Column(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
        NeydiButton(
            text = "Panodaki $adet satırı ekle",
            onClick = onPano,
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Alisveris modu alt cubugu.
 *
 * Alt %40'ta: bas parmak orada. Ustte olsaydi eli dolu bir insan telefonu
 * elinde cevirmek zorunda kalirdi.
 */
@Composable
private fun AlisverisAltCubugu(kalan: Int, onBitir: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        NeydiButton(
            text = if (kalan == 0) "Hepsi tamam — bitir" else "Alışverişi bitir",
            onClick = onBitir,
        )
    }
}

// --- Preview ---------------------------------------------------------------

private fun s(id: String, ad: String, adet: String? = null, isaretli: Boolean = false) =
    UiSatir(id, ListRow(ad, quantity = adet, checked = isaretli))

private val ORNEK = ListeDurumu(
    bolumler = listOf(
        ListeBolumu("Meyve-Sebze", listOf(s("1", "Domates", "1 kg"), s("2", "Salatalık"))),
        ListeBolumu("Fırın-Ekmek", listOf(s("3", "Tam Buğday Ekmek"))),
    ),
    yukleniyor = false,
)

@Composable
private fun Onizleme(
    durum: ListeDurumu,
    pano: String? = null,
    tahmin: SepetTahmini = SepetTahmini(),
) = ListeIcerik(
    durum = durum, girdi = "", oneriler = emptyList(), kategoriler = emptyList(),
    panoMetni = pano,
    onGirdiDegisti = {}, onEkle = {}, onOneriSecildi = {}, onKategoriSecildi = {},
    onIsaretle = { _, _ -> }, onPano = {}, onAlisverisModu = {},
    tahmin = tahmin, onSheetAc = {}, onBitir = {}, onGecmis = {}, onAyarlar = {},
)

/** Tahmin satiri: fiyat verisi geldiginde boyle gorunecek. */
@PreviewLightDark
@Composable
private fun TahminliPreview() = NeydiPreview(padding = Spacing.xs) {
    Onizleme(ORNEK, tahmin = SepetTahmini(tutarKurus = 48750, fiyatliSayisi = 2))
}

@PreviewLightDark
@Composable
private fun PlanlamaPreview() = NeydiPreview(padding = Spacing.xs) { Onizleme(ORNEK) }

/** Alisveris modu: 72dp satir, gezinme gizli, alt cubuk gorunur. */
@PreviewLightDark
@Composable
private fun AlisverisModuPreview() = NeydiPreview(padding = Spacing.xs) {
    Onizleme(
        ORNEK.copy(
            alisverisModu = true,
            bolumler = listOf(
                ListeBolumu("Meyve-Sebze", listOf(s("1", "Domates", "1 kg", isaretli = true), s("2", "Salatalık"))),
                ListeBolumu("Fırın-Ekmek", listOf(s("3", "Tam Buğday Ekmek"))),
            ),
        ),
    )
}

@PreviewLightDark
@Composable
private fun PanoCipiPreview() = NeydiPreview(padding = Spacing.xs) {
    Onizleme(ORNEK, pano = "ekmek\nsüt\nyumurta\nzeytin")
}
