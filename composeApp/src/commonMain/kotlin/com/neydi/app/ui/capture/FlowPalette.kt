package com.neydi.app.ui.capture

import androidx.compose.ui.graphics.Color

/**
 * Etiket akisinin paleti - TEMADAN BAGIMSIZ (tasarim karari 62).
 *
 * ## Neden temaya bagli degil
 *
 * Tasarim akisi tek palete sabitledi: **vizor koyu** (goz karanlik rafa uyumlu
 * kalir), **kart acik**. Tema degistirmek bu akisi etkilemiyor, cunku ekranin
 * yarisi zaten canli kamera goruntusu ve onun bir temasi yok.
 *
 * ## Kart ONCE KOYUYDU ve bu bir tur eskimisti
 *
 * Kod kartı her iki temada koyu ciziyordu ve o zaman DOGRUYDU: altinci tur
 * sozlesmesi *"kamera ve onay karti her iki temada da koyu"* diyordu. Yedinci
 * turda o satir kaldirildi ve yerine karar 62 geldi. Kullanici koyu karti iki
 * kez hata sanarak bildirdi - eski sozlesmeye gore hakli degildi, yenisine
 * gore hakli.
 *
 * Degerler tasarimin maketinden birebir; MaterialTheme'den TUREMIYORLAR ve
 * turememeliler.
 */
internal object Flow {
    /** Kart zemini. */
    val cardBackground = Color(0xFFFBF7F2)

    /** Kart metni - fiyat manseti, deger cipleri. */
    val text = Color(0xFF221A14)

    /** Satir etiketi (`Urun`, `Marka`), ve tarih degeri. */
    val label = Color(0xFF8A7666)

    /** Deger cipi: notr. */
    val chipBackground = Color(0xFFF1E7DB)
    val chipBorder = Color(0xFFE7DACB)

    /** Market cipi YESIL - secili bir marketi digerlerinden ayiran tek isaret. */
    val storeChipBackground = Color(0xFFE3EFE7)
    val storeChipBorder = Color(0xFFBFD9C8)
    val storeChipText = Color(0xFF2E6B45)

    /**
     * Marka cipi KESIK CERCEVE - "bu bir tahmin" demenin tasarimdaki yolu
     * (karar 39). Marka yalnizca oneri; kullanici degistirebiliyor.
     */
    val brandBackground = Color(0xFFFBF3E2)
    val brandBorder = Color(0xFF8A5A00)
    val brandText = Color(0xFF3A2600)

    /**
     * Kaydet YESIL, kiremit DEGIL.
     *
     * Sozlesmenin renk sozlugu kiremidi *"asla uyari ya da hata"* diye
     * ayirmis ve yesili onay/bitirme aksiyonuna vermis (karar 42). Kod
     * `NeydiButton`in varsayilanini aliyordu, yani kiremit ciziyordu.
     */
    val save = Color(0xFF3F6B54)
    val onSave = Color(0xFFFFFFFF)

    /** Kaydet pasifken: dolgu da metin de degisir - yalnizca dokunulmazlik degil. */
    val saveDisabled = Color(0xFFD9CDBE)
    val onSaveDisabled = Color(0xFF8A7666)

    /** Vazgec - metin hedefi, dolgusuz. */
    val cancel = Color(0xFF5C4F45)

    /**
     * AMBER = "bir sey eksik ya da emin degiliz" (sozlesme renk sozlugu).
     *
     * Cerceve rehberi ve eksik alan seridi ayni amberi kullaniyor. Kod
     * ikisinde de `warning` token'ini kullaniyordu; o token amber METNIN
     * rengi, amberin kendisi degil.
     */
    val amber = Color(0xFFE0A32E)
    val amberText = Color(0xFF96560A)

    /**
     * `+ Yeni urun` / `+ Yeni market` satirlarinin KIREMIDI.
     *
     * Kiremit "ileri goturen is" demek (karar 42/57) ve yeni bir kayit
     * yaratmak tam olarak o. Kaydet'in yesiliyle karismiyor: yesil BITIRIYOR,
     * kiremit BASLATIYOR.
     */
    val addAction = Color(0xFFB34418)

    /** Vizorun uzerindeki krom: cerceve rehberi disindaki her sey. */
    val viewfinderChrome = Color(0xFFF5EDE6)
    val viewfinderInk = Color(0xFF221A14)
}
