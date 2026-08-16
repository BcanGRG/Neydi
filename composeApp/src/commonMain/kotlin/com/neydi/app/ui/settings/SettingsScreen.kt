package com.neydi.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Ayarlar (Ekran 7).
 *
 * TASARIMIN KENDI TANIMI: *"Sifir tasarim yatirimi, duz liste."* Ayarlar bir
 * urun degil bir kacis yolu - kullanicinin gunde bir kez bakacagi yer degil,
 * yilda bir kez arayacagi yer.
 *
 * BOS BOLUM HIC CIZILMEZ ve bu ekranin en onemli kurali. Tasarimin gerekcesi:
 * *"Bos bir bolum basligi, olmayan bir isi varmis gibi gosterir."*
 *
 * - **Magazalar**: yedi zincir tohumlanmis geliyor ve kullanici etiket
 *   cekerken market seciyor (tasarim karari 11, pivotla revize) - satir
 *   yokken bolum cizilmiyor.
 * - **Onerilmeyenler**: `suggestion_block` tablosu semada var (v3) ama hicbir
 *   yazan yok - F6.5 (bastirma) yazilinca dolacak; bolum o gune kadar
 *   cizilmiyor.
 * - **Katilma kodu**: `Household.joinCode` null; satir SOLUK ciziliyor ve
 *   "Faz 7'de acilyor" yaziyor (karar 24) - varligi ozelligin gelecegini,
 *   solukluğu henuz olmadigini soyluyor.
 *
 * Bunlari sahte verilerle doldurmak ya da "yakinda" yazmak, tasarimin tam
 * olarak yasakladigi sey olurdu.
 */
@Composable
fun SettingsRoute(onBack: () -> Unit, onDeleteData: () -> Unit) {
    val vm: SettingsViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onBack = onBack,
        onRemoveStaple = vm::removeStaple,
        onDeleteData = onDeleteData,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onBack: () -> Unit,
    onRemoveStaple: (String) -> Unit,
    onDeleteData: () -> Unit = {},
) {
    val extras = LocalNeydiExtraColors.current
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            // Baslik blogu ve altinda hairline - Gecmis ile ayni kesim.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(NeydiExtraShapes.pill)
                        .pressable(onTap = onBack)
                        .size(Sizes.minTapTarget),
                    contentAlignment = Alignment.Center,
                ) {
                    NeydiIcon(
                        icon = NeydiIcons.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = "Ayarlar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Hairline()

            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.md),
            ) {
                SectionHeader("Hane")
                state.householdName?.let { SettingRow(label = "Ad", value = it) }
                // KATILMA KODU CIZILIYOR AMA SOLUK (tasarim karari 24).
                //
                // Once satir hic cizilmiyordu; karar 24 bunu degistirdi:
                // satirin varligi ozelligin GELECEGINI soyluyor, degeri ise
                // henuz olmadigini. Uretilmis bir kod gostermek en pahali hata
                // turuydu - kullanici kodu esine verir ve karsiliginda hicbir
                // sey olmaz. Kopyalama ikonu da dustu (ikonografi envanteri):
                // kopyalanacak bir sey yok.
                SettingRow(
                    label = "Katılma kodu",
                    value = state.joinCode ?: "Faz 7'de açılıyor",
                    dimmed = state.joinCode == null,
                )
                SettingRow(
                    label = "Üyeler",
                    // Tek kisilik hanede sayi degil DURUM yaziliyor: "1"
                    // kullaniciya hicbir sey soylemiyor.
                    value = if (state.members.size <= 1) "Sadece sen" else "${state.members.size} kişi",
                    trailing = { if (state.members.size > 1) MemberAvatars(state.members) },
                )

                SectionHeader("Her zamankiler")
                SettingRow(
                    label = "Sabit ürünler",
                    value = "${state.staples.size}/${state.stapleLimit}",
                )
                if (state.staples.isEmpty()) {
                    // Tasarimin bos hali: bolum basligi duruyor ama altinda
                    // liste yerine tek satirlik aciklama var. Bu bir CTA degil,
                    // "kendiliginden olacak" bilgisi.
                    Note("Her alışverişte aldığın ürünler birkaç alışverişten sonra kendiliğinden burada birikir.")
                } else {
                    state.staples.forEach { staple ->
                        StapleRowItem(staple) { onRemoveStaple(staple.productId) }
                    }
                }

                // MAGAZALAR SATIR VARKEN CIZILIYOR (tasarim karari 11 + 24).
                //
                // Bos hal HIC CIZILMIYOR: "Ilk gozlemden ogrenilecek" bir vaat,
                // bir deger degil - bolum ilk gozlemden sonra kendiliginden
                // gorunuyor, yani bos hal pratikte hic yasanmiyor.
                //
                // CHEVRON YOK (karar 23): satir kendi basina tamam, adlar
                // degerin icinde. Goturecegi bir ekran olmadigi icin chevron
                // tutulamayacak bir soz olurdu.
                if (state.stores.isNotEmpty()) {
                    SectionHeader("Mağazalar")
                    SettingRow(
                        label = "Takip edilen zincirler",
                        // Adlar DEGERIN kendisi: uc bes zincir tek satira
                        // sigiyor ve ayri bir liste ekrani acmayi gereksiz
                        // kiliyor.
                        value = state.stores.joinToString(", ") { it.name },
                    )
                    Note("Market etiket çekerken seçiliyor; son seçilen bir sonraki çekimde hazır gelir.")
                }

                SectionHeader("Gizlilik")
                // ETIKET FOTOGRAFI SAKLANMIYOR (tasarim karari 29): kayittan
                // hemen sonra siliniyor, yani "cihazinda kalir" demek artik
                // yanlis olurdu - hicbir yerde kalmiyor.
                Note(
                    "Etiket fotoğrafı kaydedildikten sonra siliniyor; yalnızca ürün " +
                        "adı, markası, fiyatı ve market adı hane içinde paylaşılır.",
                )
                Spacer(Modifier.height(10.dp))
                // SATIR CHEVRON KAZANDI ve tam ekran onaya gidiyor (tasarim
                // karari 2). Dialog yasagi bozulmuyor, silme de onaysiz
                // calismiyor.
                DangerRow(label = "Verilerimi sil", onClick = onDeleteData)
                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}

/** Bolum basligi - 13sp/600, ustunde genis bosluk. */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.md, bottom = 6.dp),
    )
}

/** Tasarimin 56dp'lik ayar satiri: etiket solda, deger sagda, altinda hairline. */
@Composable
private fun SettingRow(
    label: String,
    value: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    /** Henuz calismayan ozellik: satir cizilir ama solar (tasarim karari 24). */
    dimmed: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).alpha(if (dimmed) 0.5f else 1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            icon?.let {
                Spacer(Modifier.width(Spacing.sm))
                NeydiIcon(icon = it, contentDescription = null, size = 20.dp)
            }
            trailing?.let {
                Spacer(Modifier.width(Spacing.sm))
                it()
            }
        }
        Hairline()
    }
}

/**
 * Yikici satir: metin ve chevron error renginde, ustunde hairline.
 *
 * `SettingRow` DEGIL: o satirin bir degeri var ve dokunulamaz. Bu satirin isi
 * dokunulmak - ve renk, dokunmanin bedelini soyleyen tek isaret.
 */
@Composable
private fun DangerRow(label: String, onClick: () -> Unit) {
    Column {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NeydiExtraShapes.pill)
                .pressable(onTap = onClick)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            NeydiIcon(
                icon = NeydiIcons.ChevronRight,
                contentDescription = null,
                size = 22.dp,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/** Sabit urun satiri: ad + kaldirma dugmesi. */
@Composable
private fun StapleRowItem(staple: StapleRow, onRemove: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = staple.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(NeydiExtraShapes.pill)
                    .pressable(onTap = onRemove)
                    .size(Sizes.minTapTarget),
                contentAlignment = Alignment.Center,
            ) {
                NeydiIcon(
                    icon = NeydiIcons.Close,
                    contentDescription = "${staple.name} sabitlerden çıkar",
                    size = 20.dp,
                )
            }
        }
        Hairline()
    }
}

/** Bolum alti aciklama - is yaptirmaz, ne olacagini soyler. */
@Composable
private fun Note(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun Hairline() {
    val extras = LocalNeydiExtraColors.current
    Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
}

/** Uye avatarlari - 28dp daire, bas harfler. */
@Composable
private fun MemberAvatars(members: List<MemberRow>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        members.take(4).forEach { member ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(NeydiExtraShapes.pill)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = member.initials,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    }
}

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun SettingsFilledPreview() = NeydiPreview {
    SettingsScreen(
        state = SettingsState(
            householdName = "Bizim Ev",
            members = listOf(
                MemberRow("m1", "BC", isSelf = true),
                MemberRow("m2", "EL", isSelf = false),
            ),
            staples = listOf(
                StapleRow("p1", "Ekmek"),
                StapleRow("p2", "Süt"),
                StapleRow("p3", "Yumurta"),
            ),
            stores = listOf(
                StoreRow("s1", "MİGROS"),
                StoreRow("s2", "A101"),
                StoreRow("s3", "BİM"),
            ),
        ),
        onBack = {},
        onRemoveStaple = {},
    )
}

/**
 * Yeni hane: sabit yok, magaza yok, katilma kodu yok.
 *
 * Magazalar bolumu bu onizlemede HIC GORUNMEMELI - karar 11'in gorsel
 * karsiligi tam olarak bu.
 */
@PreviewLightDark
@Composable
private fun SettingsEmptyPreview() = NeydiPreview {
    SettingsScreen(
        state = SettingsState(householdName = "Adsız hane"),
        onBack = {},
        onRemoveStaple = {},
    )
}
