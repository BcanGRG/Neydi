package com.neydi.app.ui.components

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Elle tasinan ikon setinin kilidi (tasarim karari 32-34).
 *
 * BU TESTIN ISIRDIGI YER, 19 path DIZESININ ELLE YAZILMIS OLMASI. Bir ikon
 * paketinden `Icons.Rounded.Add` cagirirken yanlis yazmak derleme hatasi verir;
 * burada ayni hata SESSIZ. Kirpilmis bir `d` dizesi bos bir vektor uretir,
 * kopyala-yapistir kazasi iki ikona ayni cizimi verir - ikisi de derlenir,
 * ikisi de calisir, ikisi de ekranda yanlis gorunur. Onizlemeye bakan biri
 * fark eder; bakmayan etmez.
 *
 * O yuzden test cizimin GUZELLIGINI degil, VAR OLDUGUNU ve TEKIL oldugunu
 * olcuyor - hand-port'un gercek iki hata modu bunlar.
 */
class NeydiIconsTest {

    /**
     * Envanter karar 34 ile 17'ye sabitlenmisti; karar 64 ekleme akisini iki
     * yola ayirinca `grid_view` ile `keyboard` eklendi ve sayi 19 oldu.
     * Liste burada ELLE yaziliyor:
     * yansima commonMain'de yok, ve olsaydi bile testin `NeydiIcons`ten
     * bagimsiz bir envanter iddiasi olmasi daha iyi - ikisi ayrisirsa test
     * kirilsin diye.
     */
    private val icons: List<Pair<String, ImageVector>> = listOf(
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
        "grid_view" to NeydiIcons.GridView,
        "keyboard" to NeydiIcons.Keyboard,
    )

    @Test
    fun inventoryIsNineteen() {
        assertEquals(19, icons.size, "Karar 34 envanteri 17'ye sabitledi, karar 64 iki ikon ekledi")
    }

    /**
     * HER PATH GERCEKTEN AYRISTI.
     *
     * `addPathNodes` gecersiz girdide patlamiyor, bos liste donduruyor - yani
     * kirpilmis bir dize bos bir ikon uretir ve hicbir sey sikayet etmez.
     * Alt sinir 3: en yalin ikon (`check`) bile bir move + birkac egri tasiyor,
     * tek dugumlu bir sonuc ayristirmanin yarida kaldigi anlamina gelir.
     */
    @Test
    fun everyPathParsed() {
        icons.forEach { (name, icon) ->
            val paths = icon.root.filterIsInstance<VectorPath>()
            assertEquals(1, paths.size, "$name tek `path` tasimali (Phosphor kaynagi oyle)")
            assertTrue(
                paths.single().pathData.size >= 3,
                "$name yalnizca ${paths.single().pathData.size} dugume ayristi - dize kirpilmis olmali",
            )
        }
    }

    /**
     * IKI IKON AYNI CIZIMI TASIMIYOR.
     *
     * On dokuz benzer gorunumlu dizeyi elle tasirken en olasi kaza bu: satiri
     * kopyalayip adi degistirmek, path'i degistirmeyi unutmak. Sonuc derlenir
     * ve `chevron_right` yerine `caret-down` cizer.
     */
    @Test
    fun noTwoIconsShareTheSameDrawing() {
        val byPath = icons.groupBy { (_, icon) ->
            icon.root.filterIsInstance<VectorPath>().single().pathData.toString()
        }
        val duplicates = byPath.values.filter { it.size > 1 }
        assertTrue(
            duplicates.isEmpty(),
            "ayni cizimi paylasan ikonlar: ${duplicates.map { g -> g.map { it.first } }}",
        )
    }

    /**
     * Phosphor izgarasi 256, tasarimin optik boyutu 24dp. Ikisi ayri kavram ve
     * karistirilirsa ikon ya minicik ya devasa cizilir.
     */
    @Test
    fun everyIconUsesThePhosphorGridAndTheDesignSize() {
        icons.forEach { (name, icon) ->
            assertEquals(256f, icon.viewportWidth, "$name viewportWidth")
            assertEquals(256f, icon.viewportHeight, "$name viewportHeight")
            assertEquals(24.dp, icon.defaultWidth, "$name defaultWidth")
            assertEquals(24.dp, icon.defaultHeight, "$name defaultHeight")
        }
    }

    /**
     * RTL'de yalnizca YON tasiyan ikonlar cevrilir.
     *
     * Iki tarafi da iddia ediyoruz: cevrilmesi gerekenler cevriliyor VE
     * digerleri cevrilMIyor. Yalnizca ilkini yazsaydik, bir gun birinin
     * hepsine `autoMirror = true` vermesi testten gecerdi - ve `check` RTL'de
     * ters bakardi.
     */
    @Test
    fun onlyDirectionalIconsAutoMirror() {
        // Delta oklari (`arrow_upward`/`arrow_downward`) bilerek DISARIDA:
        // DIKEY yon tasiyorlar ve RTL'de cevrilmemeliler. Bu testin iki tarafli
        // olmasi tam da bunun icin - hepsine `autoMirror` veren bir degisiklik
        // burada duser.
        val directional = setOf("arrow_back", "chevron_right", "logout")
        icons.forEach { (name, icon) ->
            assertEquals(
                name in directional,
                icon.autoMirror,
                "$name autoMirror=${icon.autoMirror} olmamali",
            )
        }
    }
}
