package com.neydi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Spacing

/**
 * Tasarimin ikon sozlugu - TASARIMDAKI ADLARLA, PHOSPHOR CIZIMLERIYLE.
 *
 * NEDEN ARADA BIR KATMAN VAR: tasarim `more_vert`, `push_pin`, `chevron_right`
 * diyor. Cagri yerlerinde tasarimin adini kullanmak, bir ekrani tasarim
 * dosyasiyla yan yana koyup okumayi mumkun kiliyor - ve set degisirse tek dosya
 * degisiyor. Bu dosya o vaadin sinandigi yer: set gercekten degisti, cagri
 * yerlerinin hicbiri degismedi.
 *
 * ## Set: Phosphor Regular, elle tasinmis (tasarim karari 32)
 *
 * Tasarim once **Material Symbols Rounded / wght 300 / karanlikta GRAD 100**
 * istemisti. Bu **bugunku kodla uygulanamiyordu**: `wght` ve `GRAD` yalnizca
 * DEGISKEN FONTUN eksenleri, `androidx.compose.material.icons` ise derlenmis
 * statik `ImageVector` veriyor - eksen tasimiyor. Yani "A yolu" ucuz gorunuyordu
 * ama ucuzlugu gercek degildi; fontu paketleyip ikonlari `Text` olarak cizmek
 * gerekirdi ve o yol Fraunces'te bir kez reddedilmisti (iOS'ta `FontVariation`
 * guvenilir degil). Geriye kalan iki secenek AYNI mekanik isi istiyordu, o
 * yuzden kimlik kazanci olan taraf secildi.
 *
 * Sonuc: 17 ikon **Phosphor Regular** cizimleriyle elle tasindi.
 * - Bagimlilik YOK - 17 path dizesi, birkac KB kaynak.
 * - Font paketlenmiyor, ikon `Text` olarak cizilmiyor.
 * - `androidx.compose.material.icons` bagimliligi tamamen dustu (o artifact
 *   JetBrains tarafinda 1.7.3'ten sonra yayinlanmiyordu zaten).
 *
 * Cizimler [Phosphor Icons](https://phosphoricons.com) 2.1.1 `regular` setinden,
 * MIT lisansli. viewBox 256x256, tek `path`, dolgu ile cizilmis konturlar -
 * `Icon` zaten tinting yaptigi icin dolgunun rengi onemsiz.
 *
 * ## Envanter: 17 (tasarim karari 34)
 *
 * `check` ile `check_circle` AYRI kaliyor ve bu kasitli: ciplak `check` satirda
 * *"isaretlendi"*, `check_circle` ise cipte/secicide *"secili"* demek. Ikisini
 * tek ikona indirmek iki farkli fiili ayni sozcukle soylemek olurdu.
 *
 * [PushPin] tek DOLGULU ikon (tasarim `FILL 1` cizyor, 12dp sabit urun rozeti).
 * Karar 33'e gore dolgulu ikon karanlik tema telafisi ALMAZ - telafi ince
 * konturun karanlikta kaybolmasina karsi, dolgunun boyle bir sorunu yok.
 */
object NeydiIcons {

    val Add: ImageVector = phosphor(
        "Add",
        "M224,128a8,8,0,0,1-8,8H136v80a8,8,0,0,1-16,0V136H40a8,8,0,0,1,0-16h80V40a8,8,0,0,1,16,0v80h80A8,8,0,0,1,224,128Z",
    )

    /** Etiket cekimi - tasarimda `photo_camera`. */
    val PhotoCamera: ImageVector = phosphor(
        "PhotoCamera",
        "M208,56H180.28L166.65,35.56A8,8,0,0,0,160,32H96a8,8,0,0,0-6.65,3.56L75.71,56H48A24,24,0,0,0,24," +
            "80V192a24,24,0,0,0,24,24H208a24,24,0,0,0,24-24V80A24,24,0,0,0,208,56Zm8,136a8,8,0,0,1-8,8H48a8," +
            "8,0,0,1-8-8V80a8,8,0,0,1,8-8H80a8,8,0,0,0,6.66-3.56L100.28,48h55.43l13.63,20.44A8,8,0,0,0,176," +
            "72h32a8,8,0,0,1,8,8ZM128,88a44,44,0,1,0,44,44A44.05,44.05,0,0,0,128,88Zm0,72a28,28,0,1,1,28-28A28," +
            "28,0,0,1,128,160Z",
    )

    val MoreVert: ImageVector = phosphor(
        "MoreVert",
        "M140,128a12,12,0,1,1-12-12A12,12,0,0,1,140,128ZM128,72a12,12,0,1,0-12-12A12,12,0,0,0,128,72Zm0," +
            "112a12,12,0,1,0,12,12A12,12,0,0,0,128,184Z",
    )

    val ArrowBack: ImageVector = phosphor(
        "ArrowBack",
        "M224,128a8,8,0,0,1-8,8H59.31l58.35,58.34a8,8,0,0,1-11.32,11.32l-72-72a8,8,0,0,1,0-11.32l72-72a8," +
            "8,0,0,1,11.32,11.32L59.31,120H216A8,8,0,0,1,224,128Z",
        autoMirror = true,
    )

    val Close: ImageVector = phosphor(
        "Close",
        "M205.66,194.34a8,8,0,0,1-11.32,11.32L128,139.31,61.66,205.66a8,8,0,0,1-11.32-11.32L116.69,128,50.34," +
            "61.66A8,8,0,0,1,61.66,50.34L128,116.69l66.34-66.35a8,8,0,0,1,11.32,11.32L139.31,128Z",
    )

    val Search: ImageVector = phosphor(
        "Search",
        "M229.66,218.34l-50.07-50.06a88.11,88.11,0,1,0-11.31,11.31l50.06,50.07a8,8,0,0,0,11.32-11.32ZM40," +
            "112a72,72,0,1,1,72,72A72.08,72.08,0,0,1,40,112Z",
    )

    /** "Bu listede var" - Ekle sheet'indeki isaret (tasarim karari 12). */
    val CheckCircle: ImageVector = phosphor(
        "CheckCircle",
        "M173.66,98.34a8,8,0,0,1,0,11.32l-56,56a8,8,0,0,1-11.32,0l-24-24a8,8,0,0,1,11.32-11.32L112,148.69l50.34-50.35A8," +
            "8,0,0,1,173.66,98.34ZM232,128A104,104,0,1,1,128,24,104.11,104.11,0,0,1,232,128Zm-16,0a88,88,0,1,0-88," +
            "88A88.1,88.1,0,0,0,216,128Z",
    )

    val ChevronRight: ImageVector = phosphor(
        "ChevronRight",
        "M181.66,133.66l-80,80a8,8,0,0,1-11.32-11.32L164.69,128,90.34,53.66a8,8,0,0,1,11.32-11.32l80,80A8,8,0,0,1,181.66,133.66Z",
        autoMirror = true,
    )

    val ExpandMore: ImageVector = phosphor(
        "ExpandMore",
        "M213.66,101.66l-80,80a8,8,0,0,1-11.32,0l-80-80A8,8,0,0,1,53.66,90.34L128,164.69l74.34-74.35a8,8,0,0,1,11.32,11.32Z",
    )

    /** "Alisverisi birak" - moddan cikis (tasarim karari 1). */
    val Logout: ImageVector = phosphor(
        "Logout",
        "M120,216a8,8,0,0,1-8,8H48a8,8,0,0,1-8-8V40a8,8,0,0,1,8-8h64a8,8,0,0,1,0,16H56V208h56A8,8,0,0,1,120," +
            "216Zm109.66-93.66-40-40a8,8,0,0,0-11.32,11.32L204.69,120H112a8,8,0,0,0,0,16h92.69l-26.35,26.34a8," +
            "8,0,0,0,11.32,11.32l40-40A8,8,0,0,0,229.66,122.34Z",
        autoMirror = true,
    )

    /**
     * Flas - etiket kamerasinin sag ust hedefi (tasarim: Ekran 4).
     *
     * HENUZ CAGIRAN YOK, kasitli: envanter karar 34 ile 17'ye SABITLENDI ve set
     * tasimasi setin tanimlandigi an. E15 gelince ikonu ikinci kez elle tasimak
     * gerekmesin diye simdi yaziliyor - F11.4'un "tanimli ama kullanilmayan
     * primitif" denetimi bunu kaza sanmasin.
     */
    val Bolt: ImageVector = phosphor(
        "Bolt",
        "M215.79,118.17a8,8,0,0,0-5-5.66L153.18,90.9l14.66-73.33a8,8,0,0,0-13.69-7l-112,120a8,8,0,0,0,3,13l57.63," +
            "21.61L88.16,238.43a8,8,0,0,0,13.69,7l112-120A8,8,0,0,0,215.79,118.17ZM109.37,214l10.47-52.38a8,8,0,0,0-5-9.06L62," +
            "132.71l84.62-90.66L136.16,94.43a8,8,0,0,0,5,9.06l52.8,19.8Z",
    )

    /** Bilgi - etiket kamerasinin ipucu satiri (tasarim: Ekran 4). Bkz. [Bolt] notu. */
    val Info: ImageVector = phosphor(
        "Info",
        "M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24Zm0,192a88,88,0,1,1,88-88A88.1,88.1,0,0,1,128," +
            "216Zm16-40a8,8,0,0,1-8,8,16,16,0,0,1-16-16V128a8,8,0,0,1,0-16,16,16,0,0,1,16,16v40A8,8,0,0,1,144," +
            "176ZM112,84a12,12,0,1,1,12,12A12,12,0,0,1,112,84Z",
    )

    /** "Isaretlendi" - satirin onay tiki. [CheckCircle] ile ayni sey DEGIL. */
    val Check: ImageVector = phosphor(
        "Check",
        "M229.66,77.66l-128,128a8,8,0,0,1-11.32,0l-56-56a8,8,0,0,1,11.32-11.32L96,188.69,218.34,66.34a8,8,0,0,1,11.32,11.32Z",
    )

    /**
     * Sabit urun rozeti. TEK DOLGULU IKON - tasarim `FILL 1` cizyor.
     * Karanlik tema telafisi almaz (karar 33); bkz. [NeydiIcon].
     */
    val PushPin: ImageVector = phosphor(
        "PushPin",
        "M235.33,104l-53.47,53.65c4.56,12.67,6.45,33.89-13.19,60A15.93,15.93,0,0,1,157,224c-.38,0-.75,0-1.13,0a16," +
            "16,0,0,1-11.32-4.69L96.29,171,53.66,213.66a8,8,0,0,1-11.32-11.32L85,159.71l-48.3-48.3A16,16,0,0,1,38," +
            "87.63c25.42-20.51,49.75-16.48,60.4-13.14L152,20.7a16,16,0,0,1,22.63,0l60.69,60.68A16,16,0,0,1,235.33,104Z",
    )

    /**
     * Fiyat artisi oku - delta cipinde (tasarim karari 34).
     *
     * `autoMirror` KAPALI ve bu sessizce onemli: bu ok DIKEY yon tasiyor,
     * RTL'de cevrilmesi anlamsiz olurdu. Dosyadaki uc yatay ikon
     * ([ArrowBack], [ChevronRight], [Logout]) cevriliyor, bu ikisi cevrilmiyor.
     *
     * ONCE UNICODE GLIFIYDI (`↑`), ikon degil. Karar 32 *"ikonlar `Text` olarak
     * cizilmiyor"* diyor; ustelik glif sistem fontundan cozuluyordu ve Skia'nin
     * yedek zinciri iki platformda ayni sekli vermiyor - kalinlik ve optik boy
     * da yanindaki metinle eslesmiyordu.
     */
    val ArrowUpward: ImageVector = phosphor(
        "ArrowUpward",
        "M205.66,117.66a8,8,0,0,1-11.32,0L136,59.31V216a8,8,0,0,1-16,0V59.31L61.66,117.66a8,8,0,0,1-11.32-11.32l72-72a8," +
            "8,0,0,1,11.32,0l72,72A8,8,0,0,1,205.66,117.66Z",
    )

    /** Fiyat dususu oku. Bkz. [ArrowUpward] - `autoMirror` burada da kapali. */
    val ArrowDownward: ImageVector = phosphor(
        "ArrowDownward",
        "M205.66,149.66l-72,72a8,8,0,0,1-11.32,0l-72-72a8,8,0,0,1,11.32-11.32L120,196.69V40a8,8,0,0,1,16," +
            "0V196.69l58.34-58.35a8,8,0,0,1,11.32,11.32Z",
    )

    /** Panodaki listeyi yapistir (tasarim: `content_paste`). */
    val ContentPaste: ImageVector = phosphor(
        "ContentPaste",
        "M168,152a8,8,0,0,1-8,8H96a8,8,0,0,1,0-16h64A8,8,0,0,1,168,152Zm-8-40H96a8,8,0,0,0,0,16h64a8,8,0,0,0,0-16Zm56-64V216a16," +
            "16,0,0,1-16,16H56a16,16,0,0,1-16-16V48A16,16,0,0,1,56,32H92.26a47.92,47.92,0,0,1,71.48,0H200A16,16,0,0,1,216," +
            "48ZM96,64h64a32,32,0,0,0-64,0ZM200,48H173.25A47.93,47.93,0,0,1,176,64v8a8,8,0,0,1-8,8H88a8,8,0,0,1-8-8V64a47.93," +
            "47.93,0,0,1,2.75-16H56V216H200Z",
    )
}

/**
 * Phosphor `path`'ini `ImageVector`e cevirir.
 *
 * `viewportWidth/Height = 256` cunku Phosphor'un cizim izgarasi o; `defaultWidth/
 * Height = 24.dp` cunku tasarimin optik boyutu o (`iconography/opticalSize`).
 * Ikisi ayri kavram - biri kaynagin olcegi, digeri ekrandaki boy.
 *
 * DOLGU SIYAH ve bu onemsiz: `Icon` her zaman `tint` uyguluyor, dolgunun rengi
 * yalnizca tint verilmemis bir yerde gorunurdu. Material'in kendi ikonlari da
 * ayni sekilde siyah dolguyla uretiliyor.
 *
 * `SABIT val`, `get()` DEGIL: `ImageVector` salt veri ve `rememberVectorPainter`
 * ornek kimligine gore onbellekliyor. Her cagriya yeni ornek uretmek painter
 * onbellegini her recomposition'da bosa cikarirdi.
 *
 * @param autoMirror RTL'de yatay cevrilir. Yalnizca YON tasiyan ikonlarda acik:
 *   geri oku, cikis oku, chevron. Uygulama bugun tek dilli ama bunu sonradan
 *   eklemek, unutulan tek bir ikonun ters bakmasi demek olurdu.
 */
private fun phosphor(
    name: String,
    pathData: String,
    autoMirror: Boolean = false,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = DEFAULT_ICON,
    defaultHeight = DEFAULT_ICON,
    viewportWidth = 256f,
    viewportHeight = 256f,
    autoMirror = autoMirror,
).apply {
    addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
}.build()

/** Tasarimin varsayilan ikon boyutu (`iconography/opticalSize`). */
private val DEFAULT_ICON = 24.dp

/**
 * Ikon cizer.
 *
 * VARSAYILAN RENK `iconMuted`, metnin `onSurfaceVariant`i DEGIL - ve fark
 * karanlik temada gercek (tasarim karari 33). Tasarim once karanlikta `GRAD 100`
 * istemisti: ince acik konturlar koyu zeminde optik olarak inceliyor, GRAD onlari
 * kalinlastirip metinle ayni agirlikta okutuyor. Degisken font olmadan kalinlik
 * ekseni yok, o yuzden telafi **renk kademesine** cevrildi - ikon, yanindaki
 * metinden bir kademe acik cizilir. Isik temasinda telafi yok (`GRAD 0`) ve
 * `iconMuted` orada metin rengiyle ayni.
 *
 * DOLGULU IKON TELAFI ALMAZ: [NeydiIcons.PushPin] cagrisi rengini kendisi
 * veriyor. Telafi ince konturun kaybolmasina karsi; dolgunun boyle bir derdi yok.
 *
 * @param contentDescription null ise ikon DEKORATIF sayilir ve ekran okuyucu
 *   atlar. Yanindaki metin ayni seyi soyluyorsa dogrusu budur - iki kez
 *   okutmak gurultu.
 */
@Composable
fun NeydiIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = DEFAULT_ICON,
    tint: Color = LocalNeydiExtraColors.current.iconMuted,
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}

// --- Onizlemeler ------------------------------------------------------------

/**
 * On yedi ikonun atlasi.
 *
 * TESTIN OLCEMEDIGI SEYI BU GOSTERIYOR: `NeydiIconsTest` her path'in ayristigini
 * ve tekil oldugunu kanitliyor ama CIZIMIN NE OLDUGUNU bilmiyor. Yanlis Phosphor
 * dosyasindan kopyalanmis gecerli bir path testten gecer, burada gorunur.
 *
 * Karanlik cerceve ayni zamanda karar 33'un telafisini gosteriyor: ust sira
 * `iconMuted`, alt sira yanindaki metnin rengi (`onSurfaceVariant`). Ikisi
 * yan yana olmasa fark bir kademeyi ayirt edecek kadar belirgin degil.
 */
@PreviewLightDark
@Composable
private fun NeydiIconAtlasPreview() = NeydiPreview {
    val all = listOf(
        "add" to NeydiIcons.Add,
        "photo_camera" to NeydiIcons.PhotoCamera,
        "more_vert" to NeydiIcons.MoreVert,
        "arrow_back" to NeydiIcons.ArrowBack,
        "close" to NeydiIcons.Close,
        "search" to NeydiIcons.Search,
        "check_circle" to NeydiIcons.CheckCircle,
        "chevron_right" to NeydiIcons.ChevronRight,
        "expand_more" to NeydiIcons.ExpandMore,
        "logout" to NeydiIcons.Logout,
        "bolt" to NeydiIcons.Bolt,
        "info" to NeydiIcons.Info,
        "check" to NeydiIcons.Check,
        "push_pin" to NeydiIcons.PushPin,
        "content_paste" to NeydiIcons.ContentPaste,
        "arrow_upward" to NeydiIcons.ArrowUpward,
        "arrow_downward" to NeydiIcons.ArrowDownward,
    )
    Column(
        modifier = Modifier.padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        all.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                row.forEach { (name, icon) ->
                    Column(
                        modifier = Modifier.width(60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        NeydiIcon(icon = icon, contentDescription = null)
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        // Telafiyi gorunur kilan satir: ayni ikon, solda ikon rengi, sagda metin rengi.
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeydiIcon(icon = NeydiIcons.ChevronRight, contentDescription = null)
            NeydiIcon(
                icon = NeydiIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "iconMuted · onSurfaceVariant",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
