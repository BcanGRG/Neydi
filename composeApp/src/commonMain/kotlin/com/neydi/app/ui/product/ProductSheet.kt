package com.neydi.app.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.unit.sp
import com.neydi.app.data.parsePack
import com.neydi.app.data.quantityLabel
import com.neydi.app.data.sanitizeDecimal
import com.neydi.app.data.unitOptionsFor
import com.neydi.app.ui.components.QuantityStepper
import com.neydi.app.ui.components.StepperMetrics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.components.CategoryTile
import com.neydi.app.ui.components.NeydiIcon
import com.neydi.app.ui.components.NeydiIcons
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
    /**
     * "Bunu onerme" anahtarinin hali (F6.5).
     *
     * [isStaple]'in tersi DEGIL ve olmasi da gerekmiyor: bir urun hem her
     * zamanki hem onerilmeyen olabilir - kullanici onu her gezide kendisi
     * yaziyor ama motorun hatirlatmasini istemiyor. Iki ayri beyan.
     */
    val isBlocked: Boolean = false,
    /** Fiyat bolumu (E17). Bos ise bolum HIC cizilmiyor. */
    val price: PriceSection = PriceSection(),
    /**
     * "Bu listedeki miktar" blogunun verisi (karar 108). `null` ise blok
     * cizilmiyor - sheet satirdan bagimsiz acilmis demektir ve duzenlenecek
     * bir satir yoktur.
     */
    val quantity: RowQuantity? = null,
    /**
     * "Nereden alinacak" satirinin verisi (karar 126). `null` ise satir
     * cizilmiyor - bkz. [LineStore].
     */
    val lineStore: LineStore? = null,
)

/**
 * *"Nereden alinacak"* satirinin bildigi her sey (karar 126).
 *
 * ## Neden hedefin adi da tasiniyor
 *
 * Satirin sessiz hali hedefin ADINI yaziyor - *"BIM . hedef"* - yani satir
 * istisna tasimasa bile gezinin beyanini tekrar ediyor. Yalnizca istisnayi
 * tasisaydik o cumleyi yazacak veri olmazdi ve satir bos gorunurdu.
 *
 * ## Neden hedef yokken bu nesne hic dogmuyor
 *
 * Bu bir KOD KARARI ve tasarima sorulacak. Karar 117 hedefi bos birakmayi
 * mesru kildi ("Belli degil"), ama `deviantStoreName`'in ilk sarti hedefin
 * VARLIGI: hedef yokken satirin markete dair bir iddiasi olamaz, cunku
 * istisnanin istisna olabilmesi icin bir kural gerekiyor. Hedef yokken satir
 * cizilseydi dokunusla yazilan istisna HICBIR YERDE gorunmezdi - ne bandin
 * isaretinde, ne alisverisin bolumlemesinde, ne baslik cumlesinde - yani
 * jest, sonucu olmayan bir jest olurdu.
 *
 * @property targetName gezinin hedef marketinin adi.
 * @property deviantName satirin istisnasi; `null` = satir hedefi izliyor.
 *   Adi `deviantStoreName` uretiyor, yani listedeki isaretle AYNI kural.
 */
data class LineStore(
    val targetName: String,
    val deviantName: String? = null,
)

/**
 * Satirin nereden alinacagi - satirin SAG tarafina yazilan cumle.
 *
 * Hedefteyken hedefin adi ve *"hedef"* kelimesi bir arada; aradaki nokta
 * ikisini TEK BIR CUMLE yapiyor, cunku *"BIM"* tek basina bir istisna gibi
 * okunurdu. Istisnadayken yalnizca zincir adi: orada soylenecek ikinci bir
 * sey yok, satirin agirligi ve rengi zaten farkli.
 */
internal fun LineStore.label(): String = deviantName ?: "$targetName · hedef"

/** Satir bir ISTISNA mi anlatiyor - punto, renk ve chevron buna bagli. */
internal val LineStore.isException: Boolean get() = deviantName != null

/**
 * Sheet'in miktar blogunun bildigi her sey (karar 108).
 *
 * ## Neden katalogun birimi de tasiniyor
 *
 * Blok ciplerin altina *"katalog: kg"* yaziyor ve o cumle secimin SATIRA OZEL
 * oldugunu soyluyor - kullanici birimi degistirdiginde katalogun ne dedigi
 * gorunur kaliyor. Yalnizca gecerli birimi tasisaydik, o cumleyi yazacak veri
 * olmazdi ve secim geri alinamaz gibi gorunurdu.
 *
 * @property unit GECERLI birim: `unitOverride ?: unit`.
 * @property catalogUnit urunun kendi varsayilani - DEGISMIYOR.
 */
data class RowQuantity(
    val count: Double,
    val unit: String,
    val catalogUnit: String,
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
 * MANSETIN IKI HALI DE BURADA (karar 67). Tek gozlem hali *"Son ödediğin:
 * 138,50 TL"*, uc gozlemden itibaren trend hali *"32 TL → 41 TL · 6
 * Haziran'dan beri %28 arttı"* - cumleyi [PriceSection] kuruyor, sheet
 * yalnizca ciziyor. Trend cumlesi bir sure GRAFIGE bagli sanildi ve bekletildi;
 * degilmis: araligi grafik degil gozlemlerin kendisi veriyor.
 *
 * F5.3 hala Canvas grafigini, min/ortalama referans cizgilerini ve aralik
 * secicisini ekleyecek.
 * F6.5 ikinci anahtari (*"Bunu onerme"*) BAGLADI. Tablo v5'ten beri vardi ama
 * DAO'su yoktu; anahtari o hafta cizmek, gorunup calismayan bir anahtar
 * uretirdi - calismayan bir anahtardan kotu olan tam olarak budur.
 */
@Composable
fun ProductSheetContent(
    state: ProductSheetState,
    onStapleChange: (Boolean) -> Unit,
    /**
     * "Bunu onerme" (F6.5). Varsayilani BOS: onizlemeler ve anahtari
     * baglamayan cagiranlar icin - anahtar yine cizilir ama hicbir sey yazmaz.
     */
    onBlockChange: (Boolean) -> Unit = {},
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
    /** Miktar sayacinin bir adimi - `true` artir (karar 108). */
    onStepQuantity: (Boolean) -> Unit = {},
    /** Alana yazilan miktar - "buyuk atlamalar" bu yoldan (karar 108). */
    onSetQuantity: (Double) -> Unit = {},
    /** Gozlemin eksik ambalaji yazildi: `(gozlemId, "3 kg")` (karar 111). */
    onSetPack: (String, String) -> Unit = { _, _ -> },
    /** Birim cipi secildi - YALNIZ bu satiri degistirir, katalogu degil. */
    onPickUnit: (String) -> Unit = {},
    /**
     * *"Nereden alinacak"* satirina dokunuldu - beyanin cip izgarasini acar
     * (karar 126). Varsayilani BOS: satir yine cizilir ama hicbir sey acmaz,
     * onizlemeler icin.
     */
    onPickLineStore: () -> Unit = {},
) {
    val extras = LocalNeydiExtraColors.current
    Column(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            // SHEET KAYDIRILIYOR (kullanici bildirdi).
            //
            // Icerik bir `Column`di ve kaydirmasi yoktu: sheet'ten uzun olan
            // her sey sessizce KIRPILIYORDU. Sut satirinda uctan uca goruldu -
            // ucuz cipi, fiyat gecmisi, miktar blogu ve iki anahtar alt alta
            // gelince "Listeden cikar" ekranin disinda kaliyor ve
            // erisilemiyordu.
            //
            // `skipPartiallyExpanded` (bir onceki turda) sheet'i TAM aciyor
            // ama tam acik bir sheet de ekran kadar; icerik ondan uzunsa
            // kaydirma sart. Ikisi ayri sorun, ikisi de gerekliydi.
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // KATEGORI KUTUCUGU BILESENDEN - elle cizilmiyordu ve kaymisti.
            //
            // Burada 44dp'lik kendi kutusu vardi; tasarim sisteminin olcusu
            // 56dp (`Size.categoryTile`) ve `CategoryTile` bileseni tam da bu
            // is icin yazilmis. Ayni gorsel ogenin iki yerde iki boyu olmasi,
            // bilesen katmaninin vaadini bosa cikariyordu - kutucugun tipografisi
            // de farkliydi (labelLarge yerine quantityBadge olmali).
            CategoryTile(initials = turkishInitials(state.name))
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

        // MIKTAR BLOGU KUYRUGUN USTUNDE, AYRILMIS BIR ZEMINDE (karar 108).
        //
        // Karar 38 kuyrugun sirasini sabitlemisti ve bu blok o sirayi
        // BOZMUYOR: 38'in sabitledigi sira URUNE ait satirlarin sirasi
        // ("Her zamankilere ekle", "Bunu onerme", "Listeden cikar"). Miktar
        // urune degil UZUN DOKUNULAN SATIRA ait - basligi da bunu soyluyor.
        //
        // Kendi zemini var, cunku sheet'in geri kalani urun hakkinda ve bu
        // blok listedeki tek bir satir hakkinda; ayni duz zeminde dursaydi
        // ikisi ayni seyin devami gibi okunurdu.
        state.quantity?.let { q ->
            QuantityBlock(
                quantity = q,
                onStep = onStepQuantity,
                onSetCount = onSetQuantity,
                onPickUnit = onPickUnit,
            )
        }

        state.price.headlineSub?.let { sub ->
            Text(
                text = sub,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
        }

        // AMBALAJ ISTEMI (karar 111): tahmine giremeyen satirin eksik olgusu
        // BURADA veriliyor, liste satirinda degil.
        //
        // Sebebi karar 110: satirda ikinci bir 48dp hedef 72dp'ye sigmiyor ve
        // rozet o tek hedefi zaten aldi. Ambalaj da zaten GOZLEMIN ozelligi,
        // satirin degil - o yuzden gozlemin yaninda duruyor. Yol yeni de
        // degil: uzun dokunus satirdan buraya zaten geliyordu (karar 38).
        state.price.packPromptId?.let { observationId ->
            PackPrompt(onSubmit = { text -> onSetPack(observationId, text) })
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
        // KUYRUGUN ILK SATIRI "NEREDEN ALINACAK" (karar 126).
        //
        // Karar 38'in SABIT sirasi tam olarak bu tek ekleme icin acildi.
        // Gerekcesi maketin notunda yazili: *"eylem grubunun basinda, cunku
        // satirin olgusu"* - anahtarlar URUNE ait ("her zamanki mi",
        // "onerilsin mi"), bu satir ise LISTEDEKI SATIRA. Yikici satir sonda
        // kaliyor.
        //
        // Jest neden burada: karar 116 planlamada satir yuzeyinin veri
        // degistirmesini yasakliyor, karar 110 satirda ikinci bir yigilmis
        // hedefe yer birakmiyor - ve uzun dokunus zaten buraya geliyordu.
        // Yeni bir jest icat etmek ucuncu bir karari delerdi.
        state.lineStore?.let { lineStore ->
            DestinationRow(lineStore, onPickLineStore)
            // AYIRICI: maket eylem grubunun HER satirina bir ust cizgi
            // veriyor. Bu satir olmasa "Nereden alinacak" ile "Her
            // zamankilere ekle" tek blok gibi okunurdu - oysa biri satirin,
            // oteki urunun.
            Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(extras.hairline))
        }
        NeydiSwitch(
            label = "Her zamankilere ekle",
            checked = state.isStaple,
            onCheckedChange = onStapleChange,
        )
        // IKINCI ANAHTAR (F6.5). Bu satirin yeri yorumla RESERVE EDILMISTI ve
        // bir sure bos durdu: tablo v5'ten beri vardi ama DAO'su yoktu, ve
        // *gorunup calismayan bir anahtar, olmayan bir anahtardan kotudur*.
        // Artik calisiyor.
        //
        // ALT ACIKLAMA (`supporting`) YOK: tasarimin yedi ciziminin hicbirinde
        // yok. Anahtarin acik halinin ne soyleyecegi de cizilmemis - tasarima
        // soruldu (`docs/28`).
        NeydiSwitch(
            label = "Bunu önerme",
            checked = state.isBlocked,
            onCheckedChange = onBlockChange,
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
 * *"Nereden alinacak"* - eylem grubunun ilk satiri (karar 126).
 *
 * ## Iki hal, iki agirlik
 *
 * Satir SESSIZ kaliyor hedefteyken (`17sp/500`, `onSurfaceVariant`, ikon ve
 * chevron yok) ve YUKSELIYOR istisnadayken (`17sp/600`, `onSurface`,
 * storefront + chevron). Ayrim karar 118'in mantiginin aynisi: *"isaret
 * sapmadir"* - hedefteki satirin soyleyecek ozel bir seyi yok, o yuzden
 * gosterisi de yok.
 *
 * Olculer maketten: h56, etiket `bodyLarge` (17sp/500), storefront 18dp,
 * chevron 22dp, deger kumesinin ic araligi 4dp, etiket ile deger arasi 12dp.
 * Yatay 16dp dolgu sheet govdesinin olcusu.
 *
 * BUTUN SATIR DOKUNULABILIR, chevron degil: chevron 22dp ve tek basina hicbir
 * dokunma hedefi tabanini karsilamaz (karar 56).
 */
@Composable
private fun DestinationRow(store: LineStore, onTap: () -> Unit) {
    val exception = store.isException
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(onTap = onTap)
            .padding(horizontal = Spacing.md)
            .heightIn(min = DESTINATION_ROW_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DESTINATION_LABEL_GAP),
    ) {
        Text(
            text = "Nereden alınacak",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        // KIRPILAN TARAF DEGER DEGIL ETIKET DE DEGIL: bosluk esner.
        // "Nereden alinacak" sabit bir dize, zincir adi ise kisa - ikisi
        // 360dp'de rahat siginiyor ve `weight` koymak uzun bir zincir adinda
        // etiketi kirpardi.
        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DESTINATION_VALUE_GAP),
        ) {
            if (exception) {
                NeydiIcon(
                    icon = NeydiIcons.Storefront,
                    // Satirin etiketi zaten "Nereden alinacak" diyor.
                    contentDescription = null,
                    size = DESTINATION_ICON,
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
            Text(
                text = store.label(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (exception) FontWeight.SemiBold else FontWeight.Medium,
                color = if (exception) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (exception) {
                NeydiIcon(
                    icon = NeydiIcons.ChevronRight,
                    contentDescription = null,
                    size = DESTINATION_CHEVRON,
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

/** "Nereden alinacak" satirinin olculeri - maketten (karar 126). */
private val DESTINATION_ROW_HEIGHT = 56.dp
private val DESTINATION_ICON = 18.dp
private val DESTINATION_CHEVRON = 22.dp
private val DESTINATION_LABEL_GAP = 12.dp
private val DESTINATION_VALUE_GAP = 4.dp

/**
 * "Bu listedeki miktar" (karar 108).
 *
 * Olculer maketten okundu: zemin `surfaceVariant`, 14dp dolgu, 18dp kose,
 * 10dp aralik; baslik 13sp/600; sayac 48x48 ve deger alani 22sp/700, 16dp
 * kose, konturlu; cipler 34dp, yanlarda 14dp dolgu, secili olan kiremit
 * dolgulu.
 */
@Composable
private fun QuantityBlock(
    quantity: RowQuantity,
    onStep: (Boolean) -> Unit,
    onSetCount: (Double) -> Unit,
    onPickUnit: (String) -> Unit,
) {
    val options = unitOptionsFor(quantity.catalogUnit)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md)
            .clip(NeydiShapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // BASLIK "BU LISTEDEKI" DIYOR ve bu bir uslup tercihi degil: sheet'in
        // geri kalani URUN hakkinda, blok ise uzun dokunulan SATIR hakkinda.
        // "Miktar" tek basina, katalogun varsayilanini degistirdigini
        // dusundurebilirdi.
        Text(
            text = "Bu listedeki miktar",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        QuantityStepper(
            metrics = StepperMetrics.Detail,
            onDecrement = { onStep(false) },
            onIncrement = { onStep(true) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            // DEGER ALANI YAZILABILIR - ve blogun asil sebebi bu.
            //
            // Satirdaki sayac tek adim atiyor; 1'den 20'ye gitmek orada on
            // dokuz dokunus demek. Karar 108 *"buyuk atlamalar"*i buraya
            // koyuyor ve yazmadan buyuk atlama olmuyor. Kullanicinin sikayeti
            // de zaten buydu: *"4-5 yapmak istedigimde ya tekrardan yazmam
            // gerekiyor ya da katalogdan surekli ekle-ekle yapmam lazim."*
            //
            // ALAN YALNIZ SAYIYI TASIYOR, birimi DEGIL: birimi hemen altindaki
            // cipler soyluyor ve maket de alanda birim yazisi olmadan "4"
            // ciziyor. Ikisini birlikte yazdirmak, kullanicidan ayristirilacak
            // bir metin beklemek olurdu.
            QuantityField(
                count = quantity.count,
                onCommit = onSetCount,
                modifier = Modifier.weight(1f),
            )
        }

        // TEK SECENEK VARSA CIP SERIDI HIC CIZILMIYOR: yumurta katalogda
        // `adet` ve tartiya cevrilmiyor, yani secilecek bir sey yok. Tek uyeli
        // bir secici, secim varmis gibi gorunup olmayan bir sey vaat ederdi.
        if (options.size > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                options.forEach { unit ->
                    UnitChip(
                        unit = unit,
                        selected = unit == quantity.unit,
                        onClick = { onPickUnit(unit) },
                    )
                }
                Spacer(Modifier.weight(1f))
                // KATALOGUN DEDIGI GORUNUR KALIYOR: secim satira ozel ve bu
                // cumle onu soyluyor - "degistirdigin sey yalnizca bu satir".
                Text(
                    text = "katalog: ${quantity.catalogUnit}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * KESIK kontur - "burasi bos ve bir olgu bekliyor".
 *
 * Miktar alani DUZ konturlu ve dolu bir deger tasiyor; bu alan bos. Ikisi ayni
 * cizilseydi, biri "su an su" digeri "burasi eksik" derken ayni seyi soyluyor
 * gorunurlerdi. Compose'un `border`i kesik cizgi bilmiyor, o yuzden elle.
 */
private fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    val stroke = Stroke(
        width = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(14.dp.toPx()),
    )
}

/**
 * Eksik ambalajin soruldugu alan (karar 111).
 *
 * ## Neden KESIK kontur
 *
 * Miktar alani duz konturlu ve DOLU bir deger tasiyor; bu alan bos ve bir
 * OLGU BEKLIYOR. Kesik cizgi ikisini birbirinden ayiriyor - maket de boyle
 * ciziyor. Ayni sekilde cizilselerdi, biri "su an su" digeri "burasi eksik"
 * derken ayni seyi soyluyor gorunurlerdi.
 *
 * ## Neden serbest metin
 *
 * "3 kg" tek bir sey: sayi ve birim birlikte anlamli. Iki ayri kontrole
 * bolmek (sayac + cip) bir olguyu iki jeste yayardi, oysa kullanici onu tek
 * nefeste biliyor. [parsePack] cozumleyemezse hicbir sey yazilmiyor - yanlis
 * bir ambalaj, hic ambalaj olmamasindan kotu, cunku toplama GIRER.
 */
@Composable
private fun PackPrompt(onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Ambalaj",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BasicTextField(
                value = text,
                onValueChange = { raw ->
                    text = raw
                    // YAZARKEN YAZILIYOR, ayri bir "kaydet" yok: cozumlenen
                    // her hal gecerli bir cevap ve satir aninda tahmine
                    // giriyor. Onay dugmesi, tek olgu icin ikinci bir jest
                    // olurdu.
                    parsePack(raw)?.let { onSubmit(raw) }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(NeydiShapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .dashedBorder(MaterialTheme.colorScheme.outline)
                    .padding(horizontal = 12.dp),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                text = "ambalaj?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                        inner()
                    }
                },
            )
        }
        Text(
            text = "Etiketten okunamadı. Yazarsan bu satır tahmine girer.",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

/**
 * Miktarin yazilabilir alani (karar 108).
 *
 * ## Neden yerel bir metin hali var
 *
 * Kullanici yazarken alan gecerli bir sayi TUTMAYABILIR - "1," ara bir hal ve
 * sayiya cevrilemiyor. Her tusa basista veriye yazsaydik ya yaziyi silerdik ya
 * da yarim sayiyi kaydederdik. Yerel hal yazmaya izin veriyor, veriye
 * yalnizca cozumlenebilen degerler gidiyor.
 *
 * DISARIDAN GELEN DEGISIM DE IZLENIYOR (`remember(count)`): arti ve eksi
 * dugmeleri ayni sayiyi degistiriyor ve alan onlari gormezse iki kontrol
 * birbirinden ayrilirdi.
 *
 * SIFIR KABUL EDILMIYOR (karar 109): silme ayri bir eylem. Yazilan sifir
 * sessizce yok sayiliyor - hata gostermek olmayan bir yanlisi varmis gibi
 * yapmak olurdu; silmek isteyen zaten satiri kaydiriyor.
 */
@Composable
private fun QuantityField(
    count: Double,
    onCommit: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(decimalText(count)) }
    // DISARIDAN GELEN DEGISIM IZLENIYOR - AMA YAZARKEN ARAYA GIRMEDEN.
    //
    // Ilk hali `remember(count)` idi, yani sayi her degistiginde metin
    // sifirlaniyordu. Kullanici yazarken sayi ZATEN her tusta degisiyor ve
    // sifirlama imlecin altindan metni cekiyordu: cihazda "2,5"i silip "1"
    // yazmak "11" uretti.
    //
    // Kosul metnin COZUMLENEBILIR olmasi: yarim bir hal ("", "1,") sayiya
    // cevrilemiyor ve o anda disaridan gelen bir degeri yazmak, kullanicinin
    // yazmakta oldugu seyi ezmek olurdu. Cozumlenebiliyor ve farkliysa
    // degisim disaridan gelmis demektir - arti/eksi dugmelerinden.
    LaunchedEffect(count) {
        val local = text.replace(',', '.').toDoubleOrNull()
        if (local != null && local != count) text = decimalText(count)
    }
    BasicTextField(
        value = text,
        onValueChange = { raw ->
            val cleaned = sanitizeDecimal(raw)
            text = cleaned
            cleaned.replace(',', '.').toDoubleOrNull()
                ?.takeIf { it > 0.0 }
                ?.let(onCommit)
        },
        textStyle = MaterialTheme.typography.titleLarge.copy(
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier
            .height(StepperMetrics.Detail.buttonHeight)
            .clip(NeydiShapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, NeydiShapes.medium),
        decorationBox = { inner -> Box(contentAlignment = Alignment.Center) { inner() } },
    )
}

/** "1,5" / "4" - rozetin bicimiyle ayni ondalik, birimsiz. */
private fun decimalText(count: Double): String =
    if (count % 1.0 == 0.0) count.toInt().toString() else count.toString().replace('.', ',')

/** Birim cipi: secili olan kiremit dolgulu, otekiler konturlu (maketin olcusu). */
@Composable
private fun UnitChip(unit: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .pressable(onTap = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = unit,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
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
                // MAKETIN `gap:10px`'I. Bosluk yokken tarih sardiginda ikinci
                // satirinin sonu market adinin basina BITISIK cikiyordu:
                // ekranda `AğustoBİM` yaziyordu. Sutunlarin arasi, sutunlarin
                // kendisi kadar sutun.
                horizontalArrangement = Arrangement.spacedBy(HISTORY_COLUMN_GAP),
            ) {
                // TARIH ONCE: tasarimin satir sirasi `6 Ağu · Migros · 41,00 TL`
                // ve sira bir tercih degil - satiri AYIRT EDEN sey tarih.
                Text(
                    text = row.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // SABIT GENISLIK VEREN HER METNIN KILIDI OLMALI. Kilitsiz
                    // hali cihazda "22 Ağustos"u uc satira boldu ve satirin
                    // yuksekligini uce katladi.
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(HISTORY_DATE_WIDTH),
                )
                Text(
                    text = row.store,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = row.price,
                    style = MaterialTheme.typography.bodyMedium,
                    // Maket `font-weight:600`. Satirin okunan seyi fiyat;
                    // tarih ve market onu KONUMLANDIRIYOR.
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    // SAGA DAYALI SABIT SUTUN - `Chips.kt`'nin 92dp'lik fiyat
                    // sutunuyla ayni gerekce: rakamlar sutun kenarinda
                    // hizalanmazsa goz her satirda yeniden yer ariyor.
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(HISTORY_PRICE_WIDTH),
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

/**
 * Maket: `width:76px` (390dp'lik cerceve, yani dogrudan 76dp).
 *
 * Onceki 56dp maketten degil TAHMINDEN geliyordu ve "22 Ağustos"u kutuya
 * sigdiramiyordu. Onizleme fikstürleri hatayi GIZLEDI: `"6 Ağu"`, `"14 Ağu"`
 * gibi kisa degerler tasiyorlardi, yani maketin kopyasiydilar - gercek
 * uretim yolunun (`formatDayMonth`) ciktisinin degil.
 */
private val HISTORY_DATE_WIDTH = 76.dp

/** Maket: fiyat sutunu da `width:76px`, saga dayali. */
private val HISTORY_PRICE_WIDTH = 76.dp

/**
 * Maketin `gap:10px`'i. Spacing izgarasinda 10 adimi yok; satir ADIMA DEGIL
 * makete uyuyor - [CHEAP_ROW_HEIGHT] ile ayni gerekce.
 */
private val HISTORY_COLUMN_GAP = 10.dp

/** "Nerede ucuz" kutucugunun yuksekligi (maket: 52px). `min`, cunku %130 yazi
 *  olceginde iki satir 52dp'ye sigmiyor ve tasmak yerine buyumesi gerekiyor. */
private val CHEAP_ROW_HEIGHT = 52.dp

/** Kutucugun ic boslugu (maket: `padding:0 14px`). Spacing izgarasinda 14
 *  adimi yok; kutucuk ADIMA DEGIL makete uyuyor. */
private val CHEAP_ROW_PADDING = 14.dp

// --- Onizlemeler ------------------------------------------------------------

/**
 * "Nereden alinacak" satirinin IKI HALI yan yana (karar 126).
 *
 * Onizlemenin isi "cizildi mi" degil, iki halin GERCEKTEN ayrisip
 * ayrismadigini gostermek: sessiz hal (500/`onSurfaceVariant`, ikonsuz,
 * chevronsuz) ile yukselen hal (600/`onSurface`, storefront + chevron) ayni
 * ekranda gorulmezse fark bir sayidan ibaret kalir.
 */
@PreviewLightDark
@Composable
private fun ProductSheetDestinationPreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState(
            productId = "p5",
            rowId = "s5",
            name = "Kaşar Peyniri 400 g",
            isStaple = false,
            lineStore = LineStore(targetName = "BİM"),
        ),
        onStapleChange = {},
    )
    ProductSheetContent(
        state = ProductSheetState(
            productId = "p6",
            rowId = "s6",
            name = "Süt 1 L",
            isStaple = false,
            lineStore = LineStore(targetName = "BİM", deviantName = "A101"),
        ),
        onStapleChange = {},
    )
}

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

/**
 * TREND MANSETI (karar 67) - ve onizlemenin asil isi mansetin UZUNLUGU.
 *
 * Cumle 24sp Fraunces ile ciziliyor, yaninda 44dp kutucuk var ve iki satirla
 * sinirli. Tek gozlem manseti ("Son ödediğin: 100,00 TL") o sinira hicbir
 * zaman yaklasmiyor, trend cumlesi ise iki fiyat + tarih + yuzde tasiyor;
 * kirpilip kirpilmadigi ancak burada gorunuyor.
 */
@PreviewLightDark
@Composable
private fun ProductSheetTrendPreview() = NeydiPreview {
    ProductSheetContent(
        state = ProductSheetState(
            productId = "p3",
            name = "Süt",
            isStaple = true,
            price = PriceSection(
                headline = "32 TL → 41 TL · 6 Haziran'dan beri %28 arttı",
                headlineSub = "Migros · dün · 1 lt",
                history = listOf(
                    // EN UZUN GERCEK DEGERLER: fikstur maketin degil URETIMIN kopyasi
                    // olmali. Kisa adlarla dolu bir fikstur, tarih sutununun
                    // tasmasini bir sürüm boyunca gizledi.
                    HistoryRow(id = "t1", observedAt = 0, date = "22 Ağu", store = "Tarım Kredi", price = "1.234,56 TL"),
                    HistoryRow(id = "t2", observedAt = 0, date = "2 Tem", store = "Migros", price = "37,00 TL"),
                    HistoryRow(id = "t3", observedAt = 0, date = "6 Haz", store = "Migros", price = "32,00 TL"),
                ),
                sparkline = listOf(3200f, 3700f, 4100f),
            ),
        ),
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
