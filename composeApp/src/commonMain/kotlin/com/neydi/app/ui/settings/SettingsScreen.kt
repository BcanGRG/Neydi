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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neydi.app.data.store.SEED_CHAINS
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
                state.householdName?.let { SettingRow(label = "Ad", value = AnnotatedString(it)) }
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
                    value = AnnotatedString(state.joinCode ?: "Faz 7'de açılıyor"),
                    dimmed = state.joinCode == null,
                )
                SettingRow(
                    label = "Üyeler",
                    // Tek kisilik hanede sayi degil DURUM yaziliyor: "1"
                    // kullaniciya hicbir sey soylemiyor.
                    value = AnnotatedString(if (state.members.size <= 1) "Sadece sen" else "${state.members.size} kişi"),
                    trailing = { if (state.members.size > 1) MemberAvatars(state.members) },
                )

                SectionHeader("Her zamankiler")
                SettingRow(
                    label = "Sabit ürünler",
                    value = AnnotatedString("${state.staples.size}/${state.stapleLimit}"),
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

                // BOLUM KOSULSUZ CIZILIYOR (tasarim karari 36).
                //
                // Eskiden gezinme sozlesmesi "1 gozlemin altinda bolum
                // cizilmez" diyordu ama kod hicbir zaman gozleme bakmadi -
                // ve karar 11'in tohumu yedi zincir yazdigi icin esik zaten
                // hic islemiyordu. Iki karar birbirini yiyordu; karar 36
                // esigi kaldirdi.
                //
                // Asagidaki kosul o esik DEGIL: gezinme sozlesmesinin genel
                // degismezi - *bos bolum cizilmez*. Tohum yuzunden bu dal
                // yalnizca kullanici zincirlerin hepsini silerse yasanir.
                if (state.stores.isNotEmpty()) {
                    SectionHeader("Mağazalar")
                    SettingRow(
                        // "TAKIP EDILEN" DUSTU (karar 36): sifir gozlemli bir
                        // kurulumda hicbiri takip edilmiyordu ve satir olmayan
                        // bir seyi iddia ediyordu. Karar 24 uretilmis katilma
                        // kodunu tam bu gerekcyle reddetmisti - deger, henuz
                        // dogru olmayan bir sey vaat edemez.
                        label = "Zincirler",
                        // ADLAR DEGERIN KENDISI, alt satirda, IKI RENKTE.
                        //
                        // Karar 23 "uc bes zincir tek satira sigiyor"
                        // varsayimiyla yazilmisti; tohum bunu gecersiz kildi.
                        // Kararin gerekcesi ayakta (goturecegi ekran yok,
                        // chevron hala yok) - degisen yalnizca geometri.
                        value = chainNames(state.stores),
                        stacked = true,
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

/**
 * Zincir adlari, gozlemi olanlar METIN RENGINDE, otekiler SOLUK
 * (tasarim karari 36).
 *
 * NEDEN RENK, ROZET YA DA IKINCI SATIR DEGIL: karar "tek liste, tek satir,
 * yeni bilesen yok" diyor. Ayrimi tasiyan sey bir isaret degil, adin kendisi -
 * koyu okunan zincirde fiyat kaydettin, soluk olan yalnizca secebilecegin
 * bir secenek.
 *
 * RENK TEK BASINA YETMEZ ve yetmesi de beklenmiyor: siralama gozlemlileri
 * one aliyor (bkz. `SettingsViewModel`), yani bilgi renk gormeyen kullaniciya
 * da konum olarak ulasiyor.
 *
 * AYRAC " · ", virgul DEGIL: tasarimin kendi yazimi. Virgul iki adin tek bir
 * ad oldugunu dusundurebiliyor ("Tarım Kredi, File"), orta nokta ayirmiyor -
 * boluyor.
 */
@Composable
private fun chainNames(stores: List<StoreRow>): AnnotatedString {
    val observed = MaterialTheme.colorScheme.onSurfaceVariant
    val available = MaterialTheme.colorScheme.outline
    return buildAnnotatedString {
        stores.forEachIndexed { i, store ->
            if (i > 0) withStyle(SpanStyle(color = available)) { append(" · ") }
            withStyle(SpanStyle(color = if (store.hasObservation) observed else available)) {
                append(store.name)
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

/**
 * Tasarimin 56dp'lik ayar satiri: etiket solda, deger sagda, altinda hairline.
 *
 * DEGER DE AGIRLIKLI, ve bu bir hata duzeltmesi. Once yalnizca etiket
 * `weight(1f)` tasiyordu; Row agirliksiz cocuklari ONCE ve TAM GENISLIKLE
 * olcuyor, yani uzun bir deger butun satiri yiyip etikete sifir birakiyordu.
 * Etiket de sifir genislikte sarilinca **harf harf alt alta** aktı - cihazda
 * "Takip edilen zincirler" yirmi iki satir boyu uzadi ve degerin uzerine bindi.
 *
 * Iki tarafi da agirliklandirmak sorunu SINIF OLARAK kapatiyor: hangi taraf
 * uzarsa uzasin digerini ac birakamaz. Deger `TextAlign.End` ile kendi
 * yarisinda saga yaslaniyor, yani kisa degerler eskisi gibi sag kenarda duruyor.
 *
 * @param stacked deger etiketin ALTINA, tam genislikte yazilir. Degeri bir
 *   LISTE olan satirlar icin: yedi zincir adi yan yana bir yariya sigmiyor ve
 *   sigdirmaya calismak satiri bes satir yuksekliginde bir blok yapardi.
 */
@Composable
private fun SettingRow(
    label: String,
    /**
     * `AnnotatedString`, duz `String` DEGIL: karar 36 Zincirler satirinda iki
     * rengi ayni degerin icinde istiyor ve tek satirda iki renk baska turlu
     * cizilmiyor. Skaler satirlar `AnnotatedString("...")` ile cagiriyor -
     * ikinci bir parametre eklemek iki kod yolu yaratirdi.
     */
    value: AnnotatedString? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    /** Henuz calismayan ozellik: satir cizilir ama solar (tasarim karari 24). */
    dimmed: Boolean = false,
    stacked: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
) {
    Column {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).alpha(if (dimmed) 0.5f else 1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                if (!stacked) {
                    value?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f),
                        )
                    }
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
            if (stacked) {
                value?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
            // MAGAZALAR TOHUMUN KENDISINDEN (`SEED_CHAINS`), uydurma DEGIL.
            //
            // Onceki hali uc kisa ad uyduruyordu ve o yuzden bu onizleme
            // Mağazalar satirinin cihazdaki bozuk halini HIC GOSTERMEDI:
            // uc ad satira sigiyordu, yedisi sigmiyor. Fikstur, layout'un
            // calistigi veriyi secmisti - kendi kendini onaylayan bir
            // onizleme. Artik gercek tohumu okuyor.
            //
            // ILK IKISINDE GOZLEM VAR (karar 36): renk ayrimi ve "gozlemliler
            // once" siralamasi ancak karisik bir listede gorunur. Hepsi ayni
            // bayragi tasisaydi onizleme yine kendi kendini onaylardi.
            stores = SEED_CHAINS.mapIndexed { i, name ->
                StoreRow("s$i", name, hasObservation = i < 2)
            },
        ),
        onBack = {},
        onRemoveStaple = {},
    )
}

/**
 * Yeni hane: sabit yok, katilma kodu yok, HIC GOZLEM YOK.
 *
 * ONCEKI HALI TERSINI IDDIA EDIYORDU - *"Magazalar bolumu bu onizlemede HIC
 * GORUNMEMELI"* - ve o iddia karar 36 ile duştu. Ustelik zaten yanlisti:
 * tohum yedi zincir yazdigi icin bolum cihazda ilk acilistan beri
 * gorunuyordu, yalnizca onizleme fikstur olarak bos bir liste vererek onu
 * gizliyordu. Iki kez ayni tuzak: fikstur, gormek istedigimizi gosteriyordu.
 *
 * ARTIK ASIL SINANAN SEY BURADA: yedi zincirin hepsi SOLUK cizilmeli, cunku
 * hicbirinde gozlem yok. Dolu onizlemeyle yan yana bakildiginda karar 36'nin
 * renk ayrimi tek bakista gorunuyor.
 */
@PreviewLightDark
@Composable
private fun SettingsEmptyPreview() = NeydiPreview {
    SettingsScreen(
        state = SettingsState(
            householdName = "Adsız hane",
            stores = SEED_CHAINS.mapIndexed { i, name -> StoreRow("s$i", name) },
        ),
        onBack = {},
        onRemoveStaple = {},
    )
}
