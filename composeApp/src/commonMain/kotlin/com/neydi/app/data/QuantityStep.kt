package com.neydi.app.data

/**
 * Miktar sayacinin ADIMI - birime gore (karar 109).
 *
 * ## Neden birim basina degisiyor
 *
 * Tek bir adim sayisi butun birimler icin yanlis olurdu: 1'er artan bir kg
 * sayaci yarim kiloyu imkansiz kilar, 0,5'er artan bir adet sayaci ise yarim
 * yumurta onerir. Tasarim adimi bu yuzden birime bagladi.
 *
 * SAYILAN BIRIMLER 1'ER ARTIYOR: "adet birimlerinde 1" derken tasarim tek bir
 * kelimeden degil bir SINIFTAN bahsediyor - paket, kutu, demet ve sise de
 * sayiliyor, tartilmiyor. Bu yuzden kural "adet ise 1" degil "tartilmiyorsa
 * 1" diye yazildi; yeni bir sayilan birim eklendiginde kendiliginden dogru
 * davranir.
 *
 * ⚠ `ml` tasarimin listesinde YOK. `g` ile ayni mantiktan turetildi (100'er),
 * cunku ikisi de kucuk olcek birimi ve 0,5 ml'lik bir adim anlamsiz olurdu.
 */
fun stepFor(unit: String): Double = when (unit) {
    "kg", "L" -> 0.5
    "g", "ml" -> 100.0
    else -> 1.0
}

/**
 * Sayacin ALT SINIRI: bir adim. ASLA sifir.
 *
 * Tasarimin gerekcesi tek cumle: *"silme ayri bir eylemdir"* - kaydirarak sil
 * + snackbar. Sifira inebilen bir sayac, geri alinabilir bir silme yolunu
 * geri alinamaz bir "sifir adet" satirina cevirirdi; listede duran ama hicbir
 * sey ifade etmeyen bir satir kalirdi.
 */
fun decrementQuantity(count: Double, unit: String): Double {
    val step = stepFor(unit)
    val next = count - step
    return if (next < step) step else next
}

/** Sayacin ust siniri yok - kimse "20 kg domates alamazsin" demiyor. */
fun incrementQuantity(count: Double, unit: String): Double = count + stepFor(unit)
