package com.neydi.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.LocalNeydiTextStyles
import com.neydi.app.ui.theme.Motion
import com.neydi.app.ui.theme.NeydiExtraShapes
import com.neydi.app.ui.theme.NeydiShapes
import com.neydi.app.ui.theme.Sizes
import com.neydi.app.ui.theme.SizesExtra
import com.neydi.app.ui.theme.Spacing
import com.neydi.app.ui.theme.SpacingExtra
import com.neydi.app.ui.theme.pressable
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Silme jestinde acilan alanin genisligi (tasarim: "100dp'lik alan cikiyor").
 */
private val SWIPE_REVEAL = 100.dp

/**
 * Silmeyi tetikleyen esik: acilan alanin TAMAMI (kullanici bildirdi).
 *
 * ## Neden %60 degil
 *
 * Once 60dp'ydi ve gerekcesi *"kasitli silmeyi zorlastirma, geri alma zaten
 * var"*di. Kullanicinin yasadigi sey bunun tersiydi: *"bazen yarim cekiyorum
 * ve hala orada duruyor gibi; tam cekince silsin, tam cekemezsem orasi
 * kapansin."*
 *
 * Ara esik jesti IKI ANLAMLI yapiyordu - yarim cekis bazen siliyor bazen geri
 * donuyor ve hangisinin olacagi parmagin nerede durdugundan belli degil.
 * Alanin sonuna kadar cekmek tek anlamli: ya gittin ya gitmedin. Deger zaten
 * [SWIPE_REVEAL]'e kenetli, yani "sonuna kadar" ulasilabilir bir yer.
 */
private val SWIPE_THRESHOLD = SWIPE_REVEAL

/**
 * Liste satiri - uygulamanin en cok gorulen bileseni.
 *
 * ## Iki bant (karar 102)
 *
 * ```
 * [onay] [rozet] [AD 17sp/500] .......... [avatar*] [raptiye*]   <- kimlik
 *        [meta 14sp ...............] [delta*]      [fiyat cipi*] <- ekonomi
 * ```
 *
 * KIMLIK BANDI *ne alinacagini* soyler, EKONOMI BANDI *ne bildigimizi*.
 * Ayrimin sebebi olculmus bir basarisizlik: tek katta sekiz kosullu oge
 * yarisiyordu ve 360dp'de ucu birden dusuyordu - dusenlerden biri adet
 * rozetiydi, yani kural *"yanlis adedin bedeli parayla odenir"* derken adedi
 * siliyordu. Iki bantta kimlik bandi HICBIR genislikte dusmuyor; ekonomi
 * bandinin da tek feda edilebilir ogesi kaldi (delta).
 *
 * Yukseklik: 56dp (yalniz kimlik) / 72dp (iki bant) / 72dp (alisveris modu).
 *
 * SATIRIN TAMAMI isaretleme hedefidir - kucuk bir kutuyu tutturmak gerekmez.
 * Tek istisna fiyat cipi: kendi 44dp hedefi var ve fiyat gecmisini acar.
 */
@Composable
fun ListItemRow(
    row: ListRow,
    modifier: Modifier = Modifier,
    shoppingMode: Boolean = false,
    /**
     * Bu satir AZ ONCE mi eklendi (karar 89) - ve KACINCI ekleme oldugu.
     *
     * Sayi bir sira degil TETIKLEYICI: ayni urun ikinci kez eklendiginde satir
     * kimligi degismiyor (adet artiyor, karar 92) ve ekranin bunu yeni bir
     * olay olarak gorebilmesinin tek yolu bu.
     *
     * `null` = bu satir az once eklenmedi.
     */
    justAddedSeq: Long? = null,
    onToggle: () -> Unit = {},
    /**
     * Uzun basma - Urun Detayi sheet'ini aciyor.
     *
     * NEDEN UZUN BASMA: tasarim planlama modunda sheet'i **fiyat cipinden**
     * aciyor, ama fiyat cipi ancak F5.2 fiyat hafizasini bagladiginda gorunecek
     * (bugun `priceHint` hep `None`). Alisveris modundaki chevron da ayri bir
     * baglam - reyonda sabit isaretlenmiyor. Yani bugun tasarimin belirledigi
     * acici mevcut degil ve uzun basma yeni piksel eklemeyen, geri alinabilir
     * bir ara cozum. F5.2 gelince cip asil acici olur.
     */
    onLongPress: (() -> Unit)? = null,

    /**
     * Miktar sayaci BU SATIRDA acik mi (karar 107).
     *
     * Satirin kendi state'i DEGIL: `LazyColumn` gorunmeyen satiri geri
     * donusturuyor ve satir-yerel bir `remember` kaydirmada olurdu - oysa
     * kaydirmanin yapmasi gereken sey sayaci KAPATMAK, kaybetmek degil.
     */
    stepperOpen: Boolean = false,
    /** Adet rozetine dokunuldu - sayaci acar. */
    onBadgeTap: (() -> Unit)? = null,
    /** `+` (true) ya da `-` (false). */
    onStep: ((up: Boolean) -> Unit)? = null,
    /**
     * Sagdan sola cekince siler (tasarim karari 37).
     *
     * KAPI CAGIRAN TARAFTA, burada degil: jest **yalnizca plan modunda ve
     * alinmamis satirda** var. Alisveris modunda reyondasin ve yanlislikla
     * silmenin bedeli yuksek; "Alindi" bolumundeki satir da zaten alinmis.
     * Kosulu burada kurmak, satirin kendi baglamini bilmesini istemek olurdu.
     *
     * `null` ise jest hic baglanmiyor - kapali bir jest degil, olmayan bir jest.
     */
    onSwipeDelete: (() -> Unit)? = null,
) {
    val styles = LocalNeydiTextStyles.current
    val extras = LocalNeydiExtraColors.current

    // "YAPILDI" YIKAMASI (karar 89): 1.200 ms dolu, 400 ms sonme.
    //
    // GIRISI ANI, cikisi yumusak. Ekleme aninin kendisi zaten olay; yikamanin
    // yavas belirmesi olayi gecmise iterdi. Cikis yumusak cunku bitisin bir ani
    // yok - vurgu isini bitirip cekiliyor.
    //
    // RENK AMBER DEGIL: maket amber-krem ciziyordu ama karar 57 amberi
    // "eksik / emin degiliz"e kilitledi ve ekleme onayi tam tersini soyluyor.
    val wash = remember { Animatable(0f) }
    LaunchedEffect(justAddedSeq) {
        if (justAddedSeq == null) return@LaunchedEffect
        wash.snapTo(1f)
        delay(Motion.JUST_ADDED_MS.toLong())
        wash.animateTo(0f, tween(Motion.JUST_ADDED_FADE_MS))
    }
    val second = row.secondLine()
    // Ucuz-alternatif cipi de ikinci satirin sakini: ana satirda kardes olursa
    // yatay genisligi calar ve URUN ADINI kirpar. Ad kirpilmasi kabul edilemez -
    // fiyat ipucu yardimci bilgi, ad ise satirin varlik sebebi.
    val cheaper = row.cheaperElsewhere.takeUnless { shoppingMode }
    // SAPMA TEK BASINA BANDI VAR EDIYOR (karar 118): fiyati, gecmisi, hicbir
    // seyi olmayan bir satir da "burayi A101'den alacagim" diyorsa 56dp'den
    // 72dp'ye cikiyor. Beyan bir gurultu degil, kullanicinin kendi yazdigi sey.
    //
    // ALISVERIS MODUNDA CIZILMIYOR (bandin kendi kurali): orada sapanlar zaten
    // kendi bolumune ayriliyor, yani satir basina tekrar etmek gereksiz.
    val deviantStore = row.deviantStore.takeUnless { shoppingMode }
    val hasSecondLine = second != SecondLine.Empty || cheaper != null || deviantStore != null

    val height = when {
        shoppingMode -> Sizes.rowShopping
        hasSecondLine -> Sizes.rowWithMeta
        else -> Sizes.rowCollapsed
    }

    // Sabitler kullanicinin kendi ekledigi satirlardan gorsel olarak HAFIF olmali,
    // yoksa "uygulama benim agzima laf koydu" hissi verir.
    val rowAlpha = when {
        row.checked -> 0.55f
        row.isStaple -> 0.70f
        else -> 1f
    }

    // --- Silme jesti (tasarim karari 37) ---------------------------------
    //
    // COP KUTUSU IKONU YOK, "Sil" KELIMESI VAR. Ikonografi envanterinde cop
    // kutusu hic yok ve tasarim bunu acikca yaziyor: acilan alanda tek kelime
    // duruyor. Ikon eklemek envanteri bir ikon buyutmek olurdu - hem de en
    // yikici islem icin, yani en cok yanlis anlasilabilecek yerde.
    //
    // SOL KENARA DOKUNULMUYOR: sozlesme *"sol kenar iOS'ta geri gitmeye
    // ayrilmistir"* diyor. Jest yalnizca sagdan sola.
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    val revealPx = with(density) { SWIPE_REVEAL.toPx() }
    val thresholdPx = with(density) { SWIPE_THRESHOLD.toPx() }
    val surface = MaterialTheme.colorScheme.surface

    // ROZETIN 48dp'LIK HEDEFI ICIN OLCUM (karar 107 + 56).
    //
    // ## Neden ustte ayri bir kutu
    //
    // Rozet 24dp'lik kimlik bandinin icinde ve orada 48dp'lik bir dugum
    // BANDI 48dp yapardi - yani satiri 72dp'nin uzerine cikarirdi. `docs/35`
    // iki standart yolun da cihazda basarisiz oldugunu kaydediyor: olcum ile
    // yerlesimi ayirmak isabet testini genisletmiyor (Compose ust dugumun
    // BILDIRDIGI boyutu kullaniyor), `minimumInteractiveComponentSize()` ise
    // yerlesimi buyutuyor.
    //
    // Ucuncu yol: hedefi bandin DISINA, satirin kok kutusuna koymak. Orada
    // dugum gercekten 48dp - hicbir dar ebeveyn onu kirpmiyor - ve satirin
    // yuksekligine dokunmuyor, cunku `Box` icinde serbest konumlandiriliyor.
    // Rozetin nerede oldugunu olcumden ogreniyoruz; sabit bir sayi yazmak
    // rozetin genisligi degistiginde ("1" ile "1,5 kg" arasinda 54dp fark)
    // hedefi kaydirirdi.
    var badgeInRoot by remember { mutableStateOf<Rect?>(null) }
    var rowInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { rowInRoot = it.positionInRoot() },
    ) {
        if (onSwipeDelete != null && offsetX.value < 0f) {
            // Kirmizi zemin YALNIZCA surukleme sirasinda ciziliyor. Her zaman
            // cizilseydi satirin arkasinda gorunmeyen bir katman dururdu ve
            // yuvarlak koseler arasindan sizardi.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(NeydiExtraShapes.swipeRow)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier.width(SWIPE_REVEAL).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Sil",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        }

    Row(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .then(
                if (onSwipeDelete == null) {
                    Modifier
                } else {
                    Modifier.draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            // Yalnizca sola: saga cekmek satiri yerinden
                            // oynatmiyor, cunku sagda gosterilecek bir sey yok.
                            scope.launch {
                                offsetX.snapTo((offsetX.value + delta).coerceIn(-revealPx, 0f))
                            }
                        },
                        onDragStopped = {
                            if (offsetX.value <= -thresholdPx) {
                                onSwipeDelete()
                                // Yerine DONDURULUYOR: satir listeden zaten
                                // cikiyor, ama "Geri al" ile geri gelirse
                                // acilmis halde dogmamali.
                                offsetX.snapTo(0f)
                            } else {
                                // KAPANIYOR: esige varilmadiysa satir yerine
                                // doner - acik yarim bir hal birakmiyor.
                                offsetX.animateTo(0f, tween(Motion.ROW_DELETE_MS))
                            }
                        },
                    )
                },
            )
            // Surukleme sirasinda satirin KENDI zemini olmali, yoksa arkasindaki
            // kirmizi satirin icinden gorunur ve metin okunamaz hale gelir.
            .then(
                if (offsetX.value < 0f) {
                    Modifier.clip(NeydiExtraShapes.swipeRow).background(surface)
                } else {
                    Modifier
                },
            )
            .fillMaxWidth()
            .heightIn(min = height)
            .clip(NeydiShapes.large)
            // "YAPILDI" YIKAMASI, satirin KENDI zemininin ustunde ve butun
            // icerigin ALTINDA - metnin okunurlugu degismiyor, yalnizca zemin
            // bir sure yesile caliyor.
            // SAYAC ACIKKEN SATIRIN KENDI ZEMINI VAR (karar 107).
            //
            // Maket acik satiri ayri bir zemine oturtuyor ve sebebi jestin
            // kendisi: uc saniyelik bir kontrol, hangi satira ait oldugunu
            // kendisi soylemek zorunda. Sayac ekranin ortasindaysa ve satirin
            // siniri gorunmuyorsa "+" hangi urune gidiyor belli olmaz.
            .then(
                if (stepperOpen) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                } else Modifier,
            )
            .then(
                if (wash.value > 0f) {
                    Modifier.background(extras.successSurface.copy(alpha = wash.value))
                } else Modifier,
            )
            .then(
                // ALISVERIS SATIRININ DOLGUSU - YALNIZCA KARANLIKTA.
                //
                // Fark tablosu "satir container'i - plan: dolgusuz, kenarliksiz
                // -> alisveris: surfaceVariant + 1.5dp kenarlik" diyor ve kodda
                // yalnizca kenarlik vardi; dolgu hic uygulanmiyordu. Ama ayni
                // sayfanin gerekcesi dolguyu ACIKCA karanliga bagliyor:
                // "karanlik modda kontrast artisi beyaza cikisla degil ters
                // yonle yapilir - satir container'i surface #13100E'den
                // surfaceVariant #241E1A'ya dolgulanir".
                //
                // Isik maketinde alisveris satirlari gercekten DOLGUSUZ (yalniz
                // 1.5px #E7DACB kenarlik); oradaki kontrast ekranin kendi
                // zemininin #FFFFFF'e cikmasindan geliyor. Isikta da dolgu
                // koymak, ayni maketin ISARETLI satirina ayrilmis #F1E7DB'yi
                // butun satirlara dagitir ve isaretin tek gorsel farkini silerdi.
                if (shoppingMode && !extras.isLight) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                } else Modifier,
            )
            .then(
                // Kenarlik iki temada da var: kol mesafesinden ve kotu isikta
                // satir sinirlari gorunur olmali.
                if (shoppingMode) {
                    Modifier.border(
                        SizesExtra.rowBorderShopping,
                        extras.hairline,
                        NeydiShapes.large,
                    )
                } else Modifier,
            )
            .pressable(onLongPress = onLongPress, onTap = onToggle)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .alpha(rowAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ONAY DAIRESI YALNIZ ALISVERISTE (karar 116).
        //
        // Planlamada isaretlenecek bir sey yok - liste kuruluyor, tuketilmiyor.
        // Daire (24dp + 10dp bosluk) dusunce kimlik bandi 34dp KAZANIYOR:
        // 360dp'de ada 180dp yerine 214dp kaliyor.
        if (shoppingMode) {
            CheckTarget(checked = row.checked, shoppingMode = true)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                // Daire yoksa onun bosluğu da yok - ad satirin solundan basliyor.
                .padding(
                    start = if (shoppingMode) SpacingExtra.betweenCheckboxAndName else 0.dp,
                ),
            // BANTLAR ARASI 5dp - maketin olcusu. Sifir birakilsaydi meta ada
            // yapisir ve iki bant tek blok gibi okunurdu; buyutmek de satiri
            // 72dp'nin uzerine cikarirdi.
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        ) {
            // --- KIMLIK BANDI: ne alinacak (karar 103) ---------------------
            //
            // BU BANDA FEDA SIRASI DOKUNMAZ. Dort ogenin dordu de her
            // genislikte ciziliyor; eski duzende 360dp'de ucu birden
            // dusuyordu ve dusenlerden biri adet rozetiydi - yani kural
            // "yanlis adedin bedeli parayla odenir" derken adedi siliyordu.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // ROZET BASTA VE HER SATIRDA (karar 103): miktar, adin
                // solunda okunan ilk sey - ve dokununca duzenlenen yer.
                QuantityBadge(
                    row.quantity,
                    modifier = Modifier.onGloballyPositioned { badgeInRoot = it.boundsInRoot() },
                    modified = row.quantityModified,
                )

                Text(
                    modifier = Modifier.weight(1f),
                    text = row.name,
                    style = if (shoppingMode) styles.itemNameShopping else styles.itemName,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (row.checked) TextDecoration.LineThrough else null,
                )

                if (row.addedByInitial != null) MemberAvatar(row.addedByInitial)
                if (row.isStaple) StaplePin()
            }

            // --- EKONOMI BANDI: ne biliyoruz (karar 104) -------------------
            //
            // Yalnizca ICERIK VARSA ciziliyor - bos bir bant satiri 56dp'den
            // 72dp'ye cikarir ve hicbir sey soylemez.
            //
            // Alisveris modunda hic cizilmiyor: reyonda gerekli tek bilgi urun
            // adi, ve 8-9 satir yerine 7-8 satir gormek burada bedel.
            // ⚠ SAYAC ALISVERIS MODUNDA ACILMIYOR (acik madde, tasarima soruldu).
            //
            // Cihazda goruldu: kosul yalnizca `stepperOpen` olunca sayac reyon
            // satirinda da beliriyordu. Ekonomi bandi zaten `!shoppingMode` ile
            // korunuyor ve gerekcesi burada da gecerli - *"reyonda gerekli tek
            // bilgi urun adi"*. Karar 107'nin kendi gerekcesi de bir PLANLAMA
            // cumlesi: *"miktari duzenlerken kimse onceki 324,00 TL'ye
            // bakmiyor."*
            //
            // Rozet reyonda yine ciziliyor, ama okunan bir sey olarak.
            if (stepperOpen && onStep != null && !shoppingMode) {
                // SAYAC EKONOMI BANDININ YERINE GECIYOR (karar 107).
                //
                // KIMLIK BANDI YERINDE KALIYOR: rozet, ad, avatar ve raptiye
                // kipirdamiyor - sayac yalnizca ALT bandi devraliyor. Meta ile
                // fiyat o uc saniye boyunca gizli, cunku miktari duzenleyen
                // kimse "onceki 324,00 TL"ye bakmiyor.
                //
                // Satir 72dp'den 77dp'ye cikiyor (sayac bandi 32dp, meta bandi
                // 26dp) ve bu maketin kendi olcusu - "kimlik bandi bozulmaz"
                // cumlesi pikselde donmayi degil, BILESIMIN korunmasini
                // soyluyor.
                RowStepper(
                    value = row.quantity,
                    onDecrement = { onStep(false) },
                    onIncrement = { onStep(true) },
                )
            } else if (hasSecondLine && !shoppingMode) {
                // FIYAT CIPI EKONOMI BANDININ ICINDE - kolonun disinda degil.
                //
                // Disarida durursa kimlik bandi 92dp daralir ve raptiye ile
                // avatar satirin ORTASINDA asili kalir; maket ikisini bandin
                // TAM SAG UCUNDA, fiyat cipiyle ayni dikeyde ciziyor. Cipin
                // 48dp'lik hedefi bu yuzden yukseklikten degil kendi olcum
                // hilesinden geliyor (bkz. `PriceChip`).
                EconomyBand(
                    second = second,
                    cheaper = cheaper,
                    deviantStore = deviantStore,
                    priceText = (row.priceHint as? PriceHint.Single)
                        ?.takeIf { it.daysAgo <= FRESH_DAYS }?.price
                        ?: (row.priceHint as? PriceHint.Trend)?.to,
                )
            }
        }

    }

        // ROZETIN GERCEK 48dp HEDEFI - gorunmez, satirin yuksekligine
        // dokunmuyor ve rozetin GENISLIGINI aliyor.
        //
        // Rozetin ustunde/altinda kalan alan kimlik bandinin bos payi; orada
        // dokunulacak baska bir sey yok, yani calinan bir hedef de yok.
        // SATIRIN TEK YIGILMIS HEDEFI ROZET (karar 110).
        if (onBadgeTap != null) TouchTarget(badgeInRoot, rowInRoot, onBadgeTap)
    }
}

/** Ekonomi bandinin taban yuksekligi - fiyatli ve fiyatsiz satir ayni hizada dursun diye. */
private val ECONOMY_BAND_MIN = 28.dp

/** Sapma isaretinin olculeri (karar 118) - maketten. */
private val DEVIATION_ICON = 14.dp
private val DEVIATION_TEXT = 13.sp
private val DEVIATION_GAP = 4.dp

/** En kucuk dokunma hedefi (karar 56): tek sayi, 48dp. */
private val TOUCH_TARGET = 48.dp

/**
 * Adet rozetinin uzerine 48dp'lik GORUNMEZ dokunma hedefi koyar.
 *
 * ## Neden dar bandin disinda
 *
 * Rozet 24dp'lik kimlik bandinda ve orada 48dp'lik bir dugum BANDI buyuturdu -
 * yani satiri 72dp'nin uzerine cikarirdi. `docs/35` iki standart yolun da
 * cihazda basarisiz oldugunu kaydetti: olcum ile yerlesimi ayirmak isabet
 * testini genisletmiyor (Compose ust dugumun BILDIRDIGI boyutu kullaniyor),
 * `minimumInteractiveComponentSize()` ise yerlesimi buyutuyor.
 *
 * Calisan yol hedefi dar ebeveynin DISINA, satirin kok kutusuna koymak: orada
 * dugum gercekten 48dp ve satirin yuksekligine dokunmuyor.
 *
 * ## Neden YALNIZCA rozet
 *
 * Ayni yol fiyat cipi icin de yazilmisti ve calisiyordu - ama karar 110 soruyu
 * baska yerden cozdu: *"asil celiski 72dp satirda IKI yiginlmis 48dp hedef
 * istenmesiydi (rozet + cip; 2 x 48 = 96)."* Cip artik dokunulabilir degil,
 * ekonomi bandinin tamami bilgi. Karar 84'un *"cip dokunusu Urun Detayi acar"*
 * fikrasi da geri cekildi - uzun basma zaten ayni seyi yapiyordu.
 *
 * Yani cozum teknik degil MIKTARSAL: bir satirda bir yigilmis hedef var.
 *
 * @param bounds rozetin koke gore siniri; henuz olculmediyse `null` ve hedef
 *   cizilmez (ilk kareyi kacirmak, yanlis yere hedef koymaktan iyi).
 */
@Composable
private fun TouchTarget(bounds: Rect?, rowInRoot: Offset, onTap: () -> Unit) {
    if (bounds == null) return
    val density = LocalDensity.current
    val half = with(density) { (TOUCH_TARGET / 2).toPx() }
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (bounds.left - rowInRoot.x).roundToInt(),
                    (bounds.center.y - rowInRoot.y - half).roundToInt(),
                )
            }
            .size(width = with(density) { bounds.width.toDp() }, height = TOUCH_TARGET)
            .pressable(onTap = onTap),
    )
}

/**
 * Onay hedefi. Isaretlenince daire -> 12dp squircle'a donusur (200ms).
 * Sekil degisimi, renk degisiminden bagimsiz bir sinyal: renk gormeyen kullanici
 * icin de "isaretlendi" okunur.
 */
@Composable
private fun CheckTarget(checked: Boolean, shoppingMode: Boolean) {
    val size = if (shoppingMode) SizesExtra.checkboxShopping else SizesExtra.checkbox
    val corner by animateDpAsState(
        targetValue = if (checked) 12.dp else size / 2,
        animationSpec = Motion.settle(),
        label = "checkCorner",
    )
    // ISARETLI DOLGU YESIL, KIREMIT DEGIL.
    //
    // Maketlerin hepsi `background:#3F6B54` (karanlikta `#8FC7A2`) yani
    // `secondary` ciziyor; kod `primary` aliyordu ve her isaretli satir kiremit
    // bir kare gosteriyordu. Renk sozlugunun kendi ayrimi da bunu soyluyor:
    // kiremit ILERI GOTUREN is, yesil ONAY/BITIRME (karar 42). Isaretlemek
    // bitirmektir.
    val fill by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.secondary else Color.Transparent,
        animationSpec = Motion.settle(),
        label = "checkFill",
    )
    // BOS HALKA `outline`, `hairline` DEGIL - ve fark gorunur olmakla
    // olmamak arasinda.
    //
    // `hairline` (#E7DACB) surface (#FBF7F2) uzerinde yaklasik 1.2:1; yani
    // dokunulacak hedefin kendisi pratikte GORUNMUYORDU. Maketlerin hepsi
    // `border:2px solid #8A7666` yaziyor - o `outline` token'i, tasarim
    // sisteminde 15, dosya setinde 34 kez.
    val ring = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(fill)
            .border(
                width = if (checked) 1.5.dp else 2.dp,
                color = if (checked) fill else ring,
                shape = RoundedCornerShape(corner),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            // IKON, UNICODE GLIFI DEGIL.
            //
            // `Text("✓")` glifi sistem fontunun yedek zincirinden aliyordu:
            // sekli ve kalinligi cihazdan cihaza degisiyor, olcegi yazi tipi
            // ayarina bagli buyuyor. Karar 32 bunu iki kez yasakliyor -
            // *"ikonlar Text olarak cizilmiyor"* ve *"emoji ikonografi olarak
            // kullanilmaz"*.
            NeydiIcon(
                icon = NeydiIcons.Check,
                contentDescription = null,
                size = 16.dp,
                tint = MaterialTheme.colorScheme.onSecondary,
            )
        }
    }
}

/** "Her zamankiler" raptiyesi - 12dp, satirin sabit oldugunu gosterir. */
@Composable
private fun StaplePin() {
    // TASARIMIN IKONU: push_pin, textSecondary. Onceki hali 12dp'lik yesil bir
    // NOKTAYDI - "sabit" anlamini tasimiyordu, yalnizca satirin farkli
    // oldugunu soyluyordu. Raptiye ise ne oldugunu kendisi anlatiyor.
    NeydiIcon(
        icon = NeydiIcons.PushPin,
        // Bolum basligi ("Her zamankiler") zaten ayni seyi soyluyor; ekran
        // okuyucuya iki kez okutmak gurultu.
        contentDescription = null,
        // 14dp (karar 103): kimlik bandinda artik feda edilmedigi icin
        // okunakli olmasi gerekiyor - 12dp'de ne oldugu secilmiyordu.
        size = 14.dp,
        // `outline` (#8A7666), `onSurfaceVariant` (#5C4F45) DEGIL: maket
        // raptiyeyi metadan bir tik soluk ciziyor. Raptiye bir DURUM isareti,
        // okunacak bir cumle degil - metayla ayni koyulukta olmasi ikisini
        // esit onemde gosterirdi.
        tint = MaterialTheme.colorScheme.outline,
    )
}

/**
 * Ekonomi bandi - satirin *"ne biliyoruz"* kati (karar 104).
 *
 * Ucu de burada: gecmis metasi, delta cipi, fiyat cipi. Fiyat cipi cizim
 * olarak satirin kendisinde duruyor (dokunma hedefi satir yuksekliginden
 * gelsin diye) ama HIZASI bu bant.
 *
 * GUNCEL FIYATI YAZMAZ - onu cip tasiyor. Burada sadece cipin soyleyemedigi
 * sey var: nerede, ne zaman, oncesinde kacti. Tek istisna cipin HIC OLMADIGI
 * hal: gozlem eskimisse guncel fiyat yoktur, hatirlanan fiyat cumleye girer
 * (karar 105) - ve fiyat yine tek yerde yazilmis olur.
 */
@Composable
private fun EconomyBand(
    second: SecondLine,
    cheaper: String?,
    priceText: String?,
    deviantStore: String? = null,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    // BANT TEK ICERIK TASIR (karar 83-84-86): gecmis metasi YA ucuz cipi YA
    // oneri gerekcesi - asla ikisi.
    //
    // Birlikteligin dislanmasi YENI BIR KISIT DEGIL, var olan kurallarin
    // sonucu: cip varken trend bastiriliyor (karar 41) ve `PackChanged` cipi
    // zaten imkansiz kiliyor (kanitli ayni ambalaj sarti). Kod bunu artik
    // veriyle degil KURALLA biliyor.
    val text = if (cheaper != null) "" else second.metaText()
    // SPARKLINE SILINDI (karar 106): 24x14dp'de okunmuyordu ve tam da bu
    // yuzden feda sirasinin ilk uyesiydi. Siralamak yerine kaldirildi; yeri
    // Urun Detayi'ndaki grafik, orada gercekten okunuyor.
    val trend = (second as? SecondLine.Price)?.hint as? PriceHint.Trend

    BoxWithConstraints {
        // DELTA CIZILMEDEN ONCE OLCULUYOR (karar 104).
        //
        // Bandin tek feda edilebilir ogesi delta ve olcut *"cumle tam
        // kalir"*: cip cumleyi kirpacaksa cip duser, cumle degil. Bunu
        // bilmenin tek yolu ikisinin genisligini cizimden ONCE hesaplamak -
        // `weight` ile birakilsaydi kaybeden hep cumle olurdu, cunku esneyen
        // taraf o.
        val measurer = rememberTextMeasurer()
        val metaStyle = MaterialTheme.typography.bodySmall
        val deltaStyle = MaterialTheme.typography.labelSmall
        val density = LocalDensity.current
        // CIPIN 92dp'SI BUTCEDEN DUSULUYOR: cip asla dusmuyor, yani butcenin
        // konusu degil - sadece bir eksiltme. Unutulursa 360dp'lik cihazda
        // hesap "delta rahat sigar" der, oysa cumle kirpilir.
        val room = if (priceText != null) maxWidth - SizesExtra.priceColumn - 6.dp else maxWidth
        val visibleTrend = trend?.takeIf {
            with(density) {
                deltaSurvives(
                    available = room,
                    metaWidth = measurer.measure(text, metaStyle).size.width.toDp(),
                    deltaWidth = measurer
                        .measure("%${it.deltaPercent}", deltaStyle)
                        .size.width.toDp() + DELTA_CHIP_CHROME,
                )
            }
        }

        Row(
            // 28dp TABAN (karar 110 turunda eklendi).
            //
            // Bandin en uzun uyesi fiyat cipi (26dp) ve meta tek basina
            // kaldiginda bant 17dp'ye dusuyordu - yani ayni satirin ekonomi
            // kati, fiyat olup olmamasina gore iki farkli yukseklikte
            // ciziliyordu. Taban ikisini ayni hizaya oturtuyor.
            modifier = Modifier.heightIn(min = ECONOMY_BAND_MIN),
            verticalAlignment = Alignment.CenterVertically,
            // BANDIN ARALIGI 8dp - maketin olcusu (`gap:8px`). Kodda 6dp
            // yaziyordu ve bu, sapma isaretini olcerken yakalanan eski bir
            // sapmaydi; sapmasiz satirlari da ilgilendiriyor.
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // SAPMA ISARETI BANDIN BASINDA VE ASLA KIRPILMIYOR (karar 118).
            //
            // "Tek icerik" kuralinin (karar 83-84-86) DISINDA, cunku o kural
            // uygulamanin SOYLEDIKLERI arasinda seciyor - gecmis metasi mi,
            // ucuz cipi mi. Sapma ise kullanicinin KENDI BEYANI; onu bir
            // ipucuyla yarisa sokmak, kullanicinin yazdigini uygulamanin
            // tahminine yenik dusurmek olurdu.
            //
            // ISARET VE META TEK GRUP, 4dp icerideyken bant 8dp: maket bunu
            // ic ice iki flex ile ciziyor. Duz bir sirada tek aralik
            // olsaydi ikon zincir adindan, zincir adi da metadan esit uzakta
            // dururdu - oysa ilk ikisi TEK BIR ISIM gibi okunmali.
            if (deviantStore != null) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DEVIATION_GAP),
                ) {
                    NeydiIcon(
                        icon = NeydiIcons.Storefront,
                        contentDescription = null,
                        size = DEVIATION_ICON,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = deviantStore,
                        fontSize = DEVIATION_TEXT,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    // AYIRICI YALNIZCA META VARSA: sapma tek basinaysa cumle
                    // orada bitiyor ve asili bir nokta kalmamali. Maket iki
                    // hali de ciziyor.
                    if (text.isNotEmpty()) {
                        Text(
                            text = "·",
                            fontSize = DEVIATION_TEXT,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        // KIRPILAN TARAF META: `weight(1f)` onda, isarette
                        // degil - beyan kullanicinin yazdigi, meta bizim
                        // hatirlattigimiz sey.
                        MetaText(text, muted, Modifier.weight(1f))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            } else if (cheaper != null) {
                // BANT TEK ICERIK TASIR (karar 83-84-86): gecmis metasi YA
                // ucuz cipi - asla ikisi. Birliktelik YENI BIR KISIT DEGIL,
                // var olan kurallarin sonucu: cip varken trend bastiriliyor
                // (karar 41) ve `PackChanged` cipi zaten imkansiz kiliyor.
                //
                // ⚠ SAPMA CIPI DE BASTIRIYOR ve bu makette YOK (docs/38):
                // cip *"istersen A101'e ugra"* diyor, sapma ise kullanicinin
                // *"zaten A101'den alacagim"* karari. Kapanmis bir soruyu
                // yeniden sormak, ustelik 92dp'lik cipi 50dp'lik isaretin
                // yanina koyup 360dp'de ikisini birden kirpmak pahasina.
                CheaperChip(cheaper)
                Spacer(Modifier.weight(1f))
            } else if (text.isNotEmpty()) {
                MetaText(text, muted, Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (visibleTrend != null) DeltaChip(visibleTrend.deltaPercent, visibleTrend.rising)
            if (priceText != null) PriceChip(priceText)
        }
    }
}

/**
 * Satirdaki uc saniyelik miktar sayaci (karar 107).
 *
 * Olculer maketten: bant 32dp, aralik 6dp, dugmeler 44x32, deger 64dp
 * genisliginde 17sp/700 - yani ADIN puntosuyla ayni. Deger sayacin konusu,
 * kucultmek onu ikincil bir bilgi gibi gosterirdi.
 */
@Composable
private fun RowStepper(
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        QuantityStepper(
            metrics = StepperMetrics.Row,
            onDecrement = onDecrement,
            onIncrement = onIncrement,
        ) {
            Text(
                modifier = Modifier.width(STEPPER_VALUE_WIDTH),
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Spacer(Modifier.weight(1f))
        // IPUCU YENI BIR JEST TARIF ETMIYOR: uzun basma zaten Urun Detayi'ni
        // aciyor ve birim cipleri karar 108 ile orada. Cumle var olan yolu
        // ADLANDIRIYOR - "gorunmeyen kontrol yok" kuralinin ucuz karsiligi.
        Text(
            text = "birim için basılı tut",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
        )
    }
}

/** Sayacin deger sutunu - maketin olcusu; sayi degisince dugmeler kaymasin diye sabit. */
private val STEPPER_VALUE_WIDTH = 64.dp

@Composable
private fun MetaText(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        // 13sp - maketin olcusu.
        //
        // Burada eskiden 14sp vardi ve gerekcesi *"13sp %60 opaklikla
        // okunabilirlik sinirinin altina duser"*di. O gerekce ALFAYLA
        // BIRLIKTE dustu: metin artik `onSurfaceVariant` ile ciziliyor ve
        // ustunde opaklik yok (isikta 7.40:1). Alfa gidince punto itirazinin
        // dayanagi da gitti.
        style = MaterialTheme.typography.bodySmall,
        fontSize = 13.sp,
        // ALFA YOK. onSurfaceVariant ZATEN soluklastirma token'i (isikta 7.40:1,
        // onSurface 16.06:1). Ustune 0.75 alfa koymak cift-soluklastiriyordu ve
        // efektif orani 3.98:1'e dusuruyordu - 14sp normal metin icin AA sinirinin
        // (4.5:1) altinda. Hiyerarsiyi token sagliyor, alfa gereksizdi.
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

// --- Preview ---------------------------------------------------------------
// Sahte veri kasten gercekci: kisa/uzun ad, Turkce karakter, ve satirin
// yatay butcesini gercekten zorlayan bir kombinasyon. Preview'in isi
// "cizildi mi" degil, "sikistiginda ne feda ediliyor" sorusunu gostermek.

@PreviewLightDark
@Composable
private fun ListItemRowStatesPreview() = NeydiPreview {
    ListItemRow(ListRow("Karabiber"))
    ListItemRow(ListRow("Yumurta 10'lu", quantity = "2x"))
    ListItemRow(ListRow("Domates", quantity = "1 kg", addedByInitial = "A"))
    ListItemRow(ListRow("Tam Buğday Ekmek", isStaple = true))
    ListItemRow(ListRow("Zeytinyağı 1 L", checked = true))
}

@PreviewLightDark
@Composable
private fun ListItemRowPriceHintsPreview() = NeydiPreview {
    ListItemRow(
        ListRow("Zeytinyağı 1 L", priceHint = PriceHint.Single("289,00 TL", "A101", 12)),
    )
    ListItemRow(
        ListRow(
            "Ayçiçek Yağı 5 L",
            priceHint = PriceHint.Trend(
                from = "389,00", to = "455,00 TL", deltaPercent = 17, rising = true,
            ),
        ),
    )
    ListItemRow(
        ListRow("Pınar Süt", priceHint = PriceHint.PackChanged("1 L", "900 ml", "aynı fiyat")),
    )
    ListItemRow(ListRow("Bulaşık Deterjanı", suggestionReason = "12 gündür almadın"))
}

/**
 * Satirin en dar hali: uzun ad + fiyat + ucuz-alternatif cipi ayni anda.
 * Bu preview F3.1'de cihazda yakalanan hatanin nobetcisi - cip ana satirda
 * kardes oldugunda urun adi "Beyaz Peynir …" diye kirpiliyordu.
 */
@PreviewLightDark
@Composable
private fun ListItemRowCrowdedPreview() = NeydiPreview {
    ListItemRow(
        ListRow(
            "Beyaz Peynir 600 g Tam Yağlı",
            priceHint = PriceHint.Single("184,50 TL", "Migros", 9),
            cheaperElsewhere = "A101'de 159,90",
        ),
    )
}

@PreviewLightDark
@Composable
private fun ListItemRowShoppingModePreview() = NeydiPreview {
    ListItemRow(ListRow("Ayçiçek Yağı 5 L", quantity = "2x"), shoppingMode = true)
    ListItemRow(
        ListRow("Beyaz Peynir 600 g", priceHint = PriceHint.Single("184,50 TL", "Migros", 9)),
        shoppingMode = true,
    )
    ListItemRow(ListRow("Tam Buğday Ekmek", isStaple = true, checked = true), shoppingMode = true)
}
