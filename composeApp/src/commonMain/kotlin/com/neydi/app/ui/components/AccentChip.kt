package com.neydi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.neydi.app.ui.theme.LocalNeydiExtraColors
import com.neydi.app.ui.theme.Sizes

/**
 * Amber sozlesmesinin TEK uygulanma yeri.
 *
 * accent (#E0A32E) isik modunda surface uzerinde 2.08:1 - kendi sinirini tasiyamaz.
 * accentNeedsOutline true iken 1.5dp accentOutline kenarlik ZORUNLU.
 * Amber isik modunda asla metin rengi degildir; metin onAccent'tir.
 *
 * KOD INCELEMESI KURALI: background(extras.accent) cagrisi bu dosya disinda gecmemeli.
 *
 * Kaynak: Claude Design "Neydi Kotlin Multiplatform" projesi, handoff/ui/AccentChip.kt.
 */
@Composable
fun AccentSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit,
) {
    val extras = LocalNeydiExtraColors.current
    Box(
        modifier = modifier
            .clip(shape)
            .background(extras.accent)
            .then(
                if (extras.accentNeedsOutline) {
                    Modifier.border(Sizes.accentOutline, extras.accentOutline, shape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

/** "A101'de 34,90" gibi ince accent cipleri. Liste basina EN FAZLA 3. */
@Composable
fun AccentChip(text: String, modifier: Modifier = Modifier) {
    val extras = LocalNeydiExtraColors.current
    AccentSurface(modifier.heightIn(min = 24.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = extras.onAccent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * "A101'de 34,90" - BASKA MARKETTE UCUZ cipi.
 *
 * ## Neden AMBER DEGIL
 *
 * Amber [AccentChip] ile ayni oturumda gorunuyordu ve iki farkli sey
 * soyluyordu: kartta *"bir sey eksik, doldur"*, listede *"burada ucuz"*.
 * Karar 57 amberi tek anlama kilitledi - eksik/belirsiz - ve ucuzlugu
 * KIREMIDE verdi: ucuzluk ILERI GOTUREN bir is, kiremit zaten o cumle.
 *
 * Nasil gorunecegi tasarimda sayiyla yazili: notr `#FBF7F2` zemin, 1.5dp
 * kiremit kenarlik, kiremit metin. Zemin ve metin temadan geliyor
 * (`surface` / `primary`), kenarlik kalinligi [Sizes.accentOutline] ile ayni
 * cunku ikisi de "ince cipin kenari" isini yapiyor.
 */
@Composable
fun CheaperChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(Sizes.accentOutline, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * Sol serit (fis kontrolunde "yeni urun" ve "emin degil" satirlari).
 * Serit de bir amber DOLGUDUR: isik modunda kenarligi ayni kural geregi tasir.
 */
@Composable
fun AccentStrip(modifier: Modifier = Modifier) {
    AccentSurface(
        modifier = modifier.heightIn(min = 24.dp),
        shape = StripShape,
        content = {},
    )
}

private val StripShape = RoundedCornerShape(2.dp)

// --- Preview ---------------------------------------------------------------

/**
 * Isik modunda amber dolgu kenarlik ALIR, karanlikta ALMAZ. Iki temayi yan yana
 * gormek bu kuralin tek dogrulama yolu - `accentNeedsOutline` bayragi sessizce
 * ters donerse baska hicbir yerde belli olmaz.
 */
@PreviewLightDark
@Composable
private fun AccentPreview() = NeydiPreview {
    // AMBER ARTIK YALNIZCA EKSIK/BELIRSIZ (karar 57); ucuzluk kiremitte.
    AccentChip("3 gündür listede")
    CheaperChip("A101'de 159,90")
    // Serit dar ve dolgusuz; kenarlik kuralini en cok burada kaybetmek kolay.
    AccentStrip(Modifier.width(4.dp).height(40.dp))
}
