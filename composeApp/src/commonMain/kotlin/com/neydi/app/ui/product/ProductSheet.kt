package com.neydi.app.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.components.NeydiPreview
import com.neydi.app.ui.components.NeydiSwitch
import com.neydi.app.ui.components.Sparkline
import com.neydi.app.ui.components.SectionHeader
import com.neydi.app.ui.components.turkishInitials
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.NeydiShapes
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.pressable

/** Sheet'in gosterdigi urun. */
data class ProductSheetState(
    val productId: String,
    /**
     * Sheet'in acildigi satir - "Listeden cikar" bunu siliyor (karar 38).
     *
     * Nullable, cunku sheet bir gun satirdan bagimsiz da acilabilir (urun
     * gecmisinden). O halde satir cizilmiyor: silinecek bir satir yok.
     */
    val rowId: String? = null,
    val name: String,
    val isStaple: Boolean,
    /** Fiyat bolumu (E17). Bos ise bolum HIC cizilmiyor. */
    val price: PriceSection = PriceSection(),
)

/**
 * Urun Detayi sheet'i (Ekran 5) - **grafiksiz hali**.
 *
 * Basligin eski hali *"su an yalnizca sifir-gozlem hali"* diyordu ve bu E17'den
 * beri dogru degildi: sheet fiyat bolumunu de manseti de ciziyor. Eksik olan
 * sifir-gozlem/gozlemli ayrimi degil, GRAFIK.
 *
 * NEDEN SIMDI VE NEDEN BU KADAR: F6.8'in ("her zamankiler"e ekleme) tasarimda
 * belirlenmis giris noktasi bu sheet'teki anahtar. Tasarim maketlerinde
 * *"Her zamankilere ekle"* ve *"Bunu onerme"* anahtarlari sheet'in **uc veri
 * halinin hepsinde** var - sifir gozlemli halde bile. Yani sheet'in bu hali
 * fiyat verisine HIC ihtiyac duymuyor ve Faz 5'i beklemesi gerekmiyor.
 *
 * Anahtari gecici olarak Ayarlar'a koymak alternatifti; tasarimin kendi
 * affordance'ini kullanmak yerine yeni bir yer icat etmek olurdu.
 *
 * MANSETIN YALNIZCA BIR YARISI BURADA. Tasarimin iki manseti var: *"Son
 * ödediğin: 138,50 TL"* (tek gozlem hali) ve trend cumlesi *"Süt 32 TL → 41 TL
 * · son 3 ayda %28 arttı"*. Birincisi ciziliyor; ikincisi F5.3'te, cunku
 * grafigin aritmetigine bagli - ay araligi ve ambalaj degisiminde iki donemi
 * ayirma. Ikisini de bekletmek, ekranin merkezindeki cumleyi hic olmayan bir
 * grafigin arkasinda tutmak olurdu.
 *
 * F5.3 ayrica Canvas grafigi, min/ortalama referans cizgilerini ve aralik
 * secicisini ekleyecek.
 * F6.5 ikinci anahtari (*"Bunu onerme"*) baglayacak - bugun engelleme tablosu
 * var ama DAO'su yok, ve gorunup calismayan bir anahtar calismayan bir anahtardan
 * kotudur.
 */
@Composable
fun ProductSheetContent(
    state: ProductSheetState,
    onStapleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    /**
     * Satiri listeden cikarir - silme jestinin JESTSIZ ESI (tasarim karari 38).
     *
     * NEDEN TASMA MENUSUNDE DEGIL: tasarim sistemi bir sure *"her yikici islem
     * icin tasma menusunde jest olmayan bir yol"* diyordu ve o vaat mekanik
     * olarak tutulamiyordu - **menu ekran duzeyinde yasiyor, silme satir
     * duzeyinde bir is**; menu hangi satirda oldugumuzu bilmiyor. Karar 38
     * bunu Urun Detayi'na tasidi: sheet zaten BIR SATIRDAN aciliyor, yani
     * baglami tasiyor.
     *
     * BU SATIR ERISILEBILIRLIGIN KENDISI: TalkBack ve switch access swipe
     * uretemiyor. Olmasaydi silme, o kullanicilar icin var olmayan bir ozellik
     * olurdu.
     *
     * `null` ise cizilmiyor. Bugun tek cagiran liste ekrani; karar 38 Gecmis'ten
     * acilinca ayni yuvada kiremit *"Listeye ekle"* istiyor ama Gecmis satiri
     * dokunulabilir DEGIL (karar 30), yani o dal bugun ulasilamaz - varmis gibi
     * parametre acmak olu kod olurdu.
     */
    onRemoveFromList: (() -> Unit)? = null,
    /**
     * Yazilmis bir gozlemi siler (karar 46) - uzun dokunusla aciliyor.
     *
     * Varsayilani BOS DEGIL ama zararsiz: onizlemeler ve gozlemsiz cagiranlar
     * gecmis satiri hic cizmiyor, dolayisiyla hicbir zaman cagrilmiyor.
     */
    onDeleteObservation: (String) -> Unit = {},
) {
    val extras = LocalNeydiExtraColors.current
    Column(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(bottom = bottomPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Iki-harf fallback: ikon sistemi yok ve urunlerin %80'i onu
            // gosterecek. `turkishInitials` cunku locale'siz uppercase()
            // "incir" -> "IN" verir, dogrusu "İN".
            Box(
                Modifier
                    .size(44.dp)
                    .clip(NeydiExtraShapes.categoryTile)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = turkishInitials(state.name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // MANSET, VARSA URUN ADININ YERINE GECER - yanina degil.
            //
            // Bolum basligi "okunacak sey grafik degil manset cumlesi" diyor ve
            // maketlerin ucunde de bu satirda urun adi YOK: adi kutucuktaki iki
            // harf ile arkadaki satir zaten soyluyor, cumle ise ancak tek
            // basinaysa manset olabiliyor. Ad hala yazilan hal, gozlemsiz hal.
            val headline = state.price.headline
            if (headline == null) {
                Text(
                    text = state.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = Spacing.sm),
                )
            } else {
                Text(
                    text = headline,
                    // headlineMedium = Fraunces 24sp, tasarimin manset stili.
                    // Fraunces'in alt siniri 24sp (Type.kt) ve manset o sinirin
                    // uzerindeki dort kullanimdan biri.
                    style = MaterialTheme.typography.headlineMedium,
                    // IKI SATIR SERBEST: gezinme sozlesmesi dinamik yazi icin
                    // "mansetler 2 satira iner, olculer degismez" diyor, yani
                    // kirpmak degil sarmak dogru davranis.
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = Spacing.sm),
                )
            }
        }

        state.price.headlineSub?.let { sub ->
            Text(
                text = sub,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
        }

        if (state.price.isEmpty) {
            // SIFIR GOZLEM: grafik yok, manset yok, yuzde yok. Tasarimin kurali
            // "yanlis bir sey gostermektense hicbir sey gostermemek" ve tek
            // noktadan trend cizmek yalan olurdu.
            Text(
                text = "Etiket çektikçe burada fiyat geçmişi birikecek.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
        } else {
            PriceBlock(state.price, onDeleteObservation)
        }

        Spacer(Modifier.height(Spacing.md))

        // KUYRUGUN SIRASI TASARIMDA SABIT ve bir tercih degil: once anahtarlar
        // ("Her zamankilere ekle", "Bunu onerme"), EN SONDA yikici satir - her
        // biri ustunde bir ayirici ile. Kirmizi satir once ciziliyordu, yani
        // icerikle anahtar arasina giriyordu: kuyrugu tarayan goz once ona
        // carpiyor ve sheet bir ayar yuzeyi degil "sil" ekrani gibi okunuyordu.
        // Geri alinamaz is, listenin sonunda durur.
        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
        NeydiSwitch(
            label = "Her zamankilere ekle",
            checked = state.isStaple,
            onCheckedChange = onStapleChange,
        )

        onRemoveFromList?.let { remove ->
            // 56dp, ustunde ayirici, error renginde, IKON YOK, sagda kontrol yok.
            // Yikici satirin tek isareti RENK - tasarimin renk sozlesmesi
            // kirmiziyi zaten "yalnizca geri alinamaz is" diye ayirmis durumda.
            Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
            Text(
                text = "Listeden çıkar",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .pressable(onTap = remove)
                    .padding(horizontal = Spacing.md)
                    .heightIn(min = 56.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
            )
        }
    }
}


/**
 * Fiyat bolumu: "Nerede ucuz" + alim gecmisi (E17).
 *
 * ## Bos bolum BASLIGIYLA BIRLIKTE yok
 *
 * "Nerede ucuz" tek market varken cevabi olmayan bir soru, ve tasarimin genel
 * degismezi *"bos bir bolum basligi, olmayan bir isi varmis gibi gosterir"*.
 * Esik verinin kendisinde ([PriceSection]), cizimde degil - ekran yalnizca
 * gelen listeyi ciziyor.
 */
@Composable
private fun PriceBlock(price: PriceSection, onDeleteObservation: (String) -> Unit) {
    val extras = LocalNeydiExtraColors.current

    if (price.cheapest.isNotEmpty()) {
        SectionHeader(title = "Nerede ucuz", count = price.cheapest.size)
        // SATIR DEGIL KUTUCUK: maket her satiri 52dp'lik dolgulu bir kart
        // yapiyor - surfaceVariant zemin, 1dp hairline kenarlik, 16dp kose.
        // Dolgusuz hali bu satirlari hemen altlarindaki gecmis tablosundan
        // ayirt ettirmiyordu; ikisi ayni ritimde okununca "Nerede ucuz" bir
        // karsilastirma olmaktan cikip listenin devami gibi gorunuyordu.
        Column(
            modifier = Modifier.padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            price.cheapest.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NeydiShapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(Sizes.hairline, extras.hairline, NeydiShapes.medium)
                        .heightIn(min = CHEAP_ROW_HEIGHT)
                        .padding(horizontal = CHEAP_ROW_PADDING),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = row.store,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        // MARKA, AMBALAJ VE YAS ALT SATIRDA: karar 26 kimligi
                        // market+marka cifti yapiyor, yani marka satirin bir
                        // suslemesi degil AYIRT EDICI bilgisi.
                        //
                        // YAS ARTIK HER ZAMAN VAR ve satir bu yuzden hic
                        // dusmuyor. Onceden marka da ambalaj da bilinmiyorsa
                        // (manavda ikisi de yok) alt satir tumden cizilmiyordu
                        // ve geriye yalnizca fiyat kaliyordu: iki hafta onceki
                        // bir gozlem, bugunkuyle ayni gorunuyordu.
                        //
                        // AMBALAJ MAKETTE SATIRDA DEGIL, bolum basliginda
                        // ("14:20 itibarıyla · 4 L") - orada butun satirlar
                        // ayni ambalajdan oldugu icin. Bizim satirlarimiz
                        // karisik olabiliyor ve SectionHeader'in oyle bir yuvasi
                        // yok; ambalaji dusurmek iki fiyati kiyaslanamaz
                        // kilardi, o yuzden satirda kaliyor.
                        Text(
                            text = listOfNotNull(row.brand, row.pack, row.recency)
                                .joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = row.price,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = Spacing.sm),
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.sm))
    }

    SectionHeader(title = "Alım geçmişi", count = price.history.size)

    // UZUN DOKUNUS SILME KAPISINI ACIYOR (karar 46).
    //
    // Yazilmis bir gozleme dokunan hicbir yuzey yoktu: yanlis bir sayinin tek
    // caresi Ayarlar'daki "Verilerimi sil"di. Kapi UZUN dokunusta cunku
    // gecmis satirlari OKUNMAK icin var; kisa dokunusa silme koymak, listeyi
    // gozden gecirirken yanlislikla silmek demekti.
    //
    // BIR SEFERDE TEK SATIR aciliyor: iki kirmizi satir ust uste, hangisinin
    // silinecegini belirsizlestirirdi.
    var armed by remember(price.history) { mutableStateOf<String?>(null) }

    price.history.forEach { row ->
        if (armed == row.id) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressable(
                        onLongPress = { armed = null },
                        onTap = { onDeleteObservation(row.id); armed = null },
                    )
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    .heightIn(min = Sizes.minTapTarget),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Bu gözlemi sil",
                    style = MaterialTheme.typography.bodyMedium,
                    // ERROR RENGI, kiremit DEGIL: kiremit ileri goturen isin
                    // rengi (karar 42) ve silme geri goturuyor.
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${row.store} · ${row.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressable(onLongPress = { armed = row.id }, onTap = {})
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    .heightIn(min = Sizes.minTapTarget),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // TARIH ONCE: tasarimin satir sirasi `6 Ağu · Migros · 41,00 TL`
                // ve sira bir tercih degil - satiri AYIRT EDEN sey tarih.
                Text(
                    text = row.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(HISTORY_DATE_WIDTH),
                )
                Text(
                    text = row.store,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = row.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    if (price.sparkline.isNotEmpty()) {
        Box(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
            Sparkline(values = price.sparkline, color = extras.priceUp)
        }
    }
}

private val HISTORY_DATE_WIDTH = 56.dp

/** "Nerede ucuz" kutucugunun yuksekligi (maket: 52px). `min`, cunku %130 yazi
 *  olceginde iki satir 52dp'ye sigmiyor ve tasmak yerine buyumesi gerekiyor. */
private val CHEAP_ROW_HEIGHT = 52.dp

/** Kutucugun ic boslugu (maket: `padding:0 14px`). Spacing izgarasinda 14
 *  adimi yok; kutucuk ADIMA DEGIL makete uyuyor. */
private val CHEAP_ROW_PADDING = 14.dp

// --- Onizlemeler ------------------------------------------------------------

@PreviewLightDark
@Composable
private fun ProductSheetStaplePreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState(productId = "p1", name = "Beyaz Peynir 600 g", isStaple = true),
        onStapleChange = {},
    )
}

@PreviewLightDark
@Composable
private fun ProductSheetPlainPreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState(productId = "p2", name = "Kuru Kayısı", isStaple = false),
        onStapleChange = {},
    )
}

@PreviewLightDark
@Composable
private fun ProductSheetPricePreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState(
            productId = "p1",
            name = "Ayçiçek Yağı",
            isStaple = true,
            price = PriceSection(
                headline = "Son ödediğin: 100,00 TL",
                headlineSub = "BİM · dün · 4 lt",
                cheapest = listOf(
                    CheapRow(store = "BİM", brand = "Dost", price = "100,00 TL", pack = "4 lt", recency = "dün"),
                    CheapRow(store = "Migros", brand = "Pınar", price = "130,00 TL", pack = null, recency = "3 gün önce"),
                ),
                history = listOf(
                    HistoryRow(id = "h-BİM-10000", observedAt = 0, date = "6 Ağu", store = "BİM", price = "100,00 TL"),
                    HistoryRow(id = "h-Migros-13000", observedAt = 0, date = "6 Ağu", store = "Migros", price = "130,00 TL"),
                    HistoryRow(id = "h-BİM-9500", observedAt = 0, date = "6 Ağu", store = "BİM", price = "95,00 TL"),
                ),
                sparkline = listOf(95f, 130f, 100f),
            ),
        ),
        onStapleChange = {},
        // KUYRUGUN SIRASI ANCAK BURADA GORUNUYOR: diger iki onizleme
        // `onRemoveFromList` gecmiyor, yani kirmizi satiri hic cizmiyor ve
        // satirin yanlis yerde durdugu bir onizlemede fark edilemezdi.
        onRemoveFromList = {},
    )
}
