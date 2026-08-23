package com.neydi.app.data

/**
 * Bir urun icin MAKUL birimler (karar 108).
 *
 * ## Neden urune gore degisiyor
 *
 * Tasarim *"birim cipleri katalogun makul birimleri"* diyor ve makul olmak
 * urune bagli: domatesi adetle almak da kiloyla almak da olagan, yumurtayi
 * kiloyla almak degil. Sabit bir liste ("kg / g / adet") her satirda ayni uc
 * cipi cizerdi ve yumurta satirinda ikisi anlamsiz olurdu.
 *
 * ## Kural
 *
 * Katalogun birimi ONCE - kullanicinin bugunku hali o. Sonra varsa OLCEK
 * KARDESI (kg ile g, L ile ml ayni seyin iki hassasiyeti), sonra `adet`.
 * Tartilan bir sey sayilabilir de: *"3 adet domates"* anlamli bir cumle.
 *
 * TERSI DEGIL: sayilan bir birimden tartiya gecis onerilmiyor. Yumurta
 * katalogda `adet` ve listede yalnizca `adet` cikiyor - tek secenek kalan
 * yerde cip seridi hic cizilmiyor, cunku secim olmayan bir secici gurultu.
 *
 * Kanon [parseQuantity]'nin kanonu (`kg`, `g`, `L`, `ml`, `adet`, ...) -
 * `normalizeUnit` DEGIL. Ikisi ayrisiyor (`g`/`gr`, `L`/`lt`) ve adim tablosu
 * (karar 109) bu kanonla yazildi.
 */
fun unitOptionsFor(catalogUnit: String): List<String> {
    val sibling = when (catalogUnit) {
        "kg" -> "g"
        "g" -> "kg"
        "L" -> "ml"
        "ml" -> "L"
        else -> null
    }
    return buildList {
        add(catalogUnit)
        sibling?.let(::add)
        // Tartilan her sey sayilabilir; sayilan her sey tartilamaz.
        if (sibling != null) add("adet")
    }.distinct()
}
