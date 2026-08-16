package com.neydi.app.data.store

import com.neydi.app.data.matchKey

/**
 * Magaza adindan zincir anahtari.
 *
 * SUBE DEGIL ZINCIR: alias "BIM"e baglanmali, "BIM BADEMLIK SUBESI"ne degil -
 * yoksa her sube icin ayni duzeltmeyi tekrar yapmak gerekirdi. Ilk kelime
 * pratikte zinciri veriyor (BIM, FILE, AKYURT, MIGROS).
 *
 * Fis doneminde girdisi kunyeden okunuyordu; etiket doneminde (E13) magaza
 * tohumlarinin `chain` kolonunu ve "+ Yeni market" tekillestirmesini ayni
 * fonksiyon uretiyor - anahtar vokabuler DEGISMEDI, alias'lar gecerli kaliyor.
 */
internal fun chainKey(storeName: String?): String =
    storeName?.let { matchKey(it).split(" ").firstOrNull { p -> p.length > 1 } } ?: "bilinmiyor"

/**
 * EKRANDA GOSTERILECEK magaza adi (tasarim karari 13).
 *
 * TICARI UNVAN HICBIR YERDE CIZILMIYOR. Fiste basili olan
 * `AKYURT SÜPERMARKET GIDA İNS.SAN.VE TİC. A.Ş.` kullaniciya hicbir sey
 * ogretmiyor ve basligin asil ayirt edici bilgisini - tarihi - ekran disina
 * itiyor. Cihazda tam bu goruldu: alt satirda yalnizca unvan kaldi.
 *
 * ZINCIR ADI, ILK IKI KELIME DEGIL: kesim keyfi olmasin diye ayni anahtar
 * kullaniliyor - `chainKey` zaten alias ogrenmesinin (F4.7) kullandigi ad,
 * yani ayni marketin iki ayri adi olmuyor.
 *
 * BUYUK/KUCUK HARF OLDUGU GIBI BIRAKILIYOR ve bu bilincli. Tasarim ornekte
 * `FiLE` icin "File", `MİGROS` icin "Migros" yaziyor - yani baslik duzeni.
 * Ama bu projede locale'siz harf donusumu YASAK ve sebebi olculmus:
 * `"İNCİR".lowercase()` bes harf yerine yedi kod noktasi uretiyor
 * (bkz. MatchKey.kt). Fisin yazdigi hali gostermek, yanlis bir donusumden
 * daha durust; sapma tasarima soru olarak kaydedildi.
 *
 * @return zincir adi, ya da hicbir sey okunamadiysa null.
 */
internal fun storeDisplayName(storeNameRaw: String?): String? =
    storeNameRaw
        ?.split(" ", "\t")
        ?.map { it.trim() }
        // Tek harflik parcalar atlaniyor: "A.Ş." ve "A S" gibi unvan
        // kirintilari zincir adi degil.
        ?.firstOrNull { it.length > 1 && it.any(Char::isLetter) }
        ?.trimEnd('.', ',')
        ?.takeIf { it.isNotBlank() }
