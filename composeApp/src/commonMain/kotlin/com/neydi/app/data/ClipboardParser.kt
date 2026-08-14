package com.neydi.app.data

/**
 * Panodaki metni liste satirlarina cevirir.
 *
 * MEVCUT ALISKANLIGI DEGISTIREN EN UCUZ KAZANC: liste bugun WhatsApp'ta
 * yaziliyor. Kopyalayip yapistirabilmek 12 urunu tek tek yazmaya gore
 * dakikalar kazandiriyor - ve kullaniciyi aliskanligini degistirmeye
 * zorlamadan uygulamaya sokuyor.
 *
 * Madde isaretleri ve numaralandirma temizleniyor: WhatsApp listelerinde
 * "- ekmek", "1. ekmek", "• ekmek" hepsi cikiyor ve hicbiri urun adinin
 * parcasi degil.
 */
private val BULLET_PREFIX = Regex("""^\s*(?:[-*•·–—]+|\d+[.)])\s*""")

/** Emoji ve onay isaretleri: kopyalanan listelerde sik ve ada ait degil. */
private val BULLETS = Regex("""[\u2705\u2714\u274C\u2611\u25A0\u25A1\u2610\uD83D\uDFE2\uD83D\uDD34]""")

fun clipboardLines(text: String): List<String> =
    text.lineSequence()
        .map { it.replace(BULLETS, "").replace(BULLET_PREFIX, "").trim() }
        .filter { it.isNotBlank() }
        // Tek kelimelik cok uzun satirlar URL ya da yapistirma kazasi olur.
        .filter { it.length <= 60 }
        .toList()

/**
 * Panonun liste GIBI gorunup gorunmedigi. Esik UC satir: iki satirlik bir
 * pano cogu zaman kopyalanmis bir cumle, liste degil - her metin parcasinda
 * "yapistir" cipi cikarmak cipi gurultuye cevirir ve goz onu gormemeye baslar.
 */
fun looksLikeList(text: String?): Boolean =
    text != null && clipboardLines(text).size >= 3
