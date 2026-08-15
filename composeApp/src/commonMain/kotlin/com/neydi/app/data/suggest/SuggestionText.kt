package com.neydi.app.data.suggest

/**
 * Cipin gerekce metni (F6.3) - "Yumurta · 14 gun oldu"nun ikinci yarisi.
 *
 * GEREKCESIZ CIP REKLAM GIBI OKUNUR - tasarimda her cip duz Turkce bir gerekce
 * tasiyor ve metinler maketlerden birebir: "14 gun oldu", "gecen sefer
 * unutmustun".
 *
 * "Unuttum" beyani gun sayisini EZIYOR: kullanicinin kendi soyledigi sey
 * ("almam gerekiyordu") herhangi bir sayidan guclu gerekce, ve ayni cipte
 * ikisini birden yazmak gurultu.
 */
fun Suggestion.reasonText(): String = when {
    forgottenLastTrip -> "geçen sefer unutmuştun"
    daysSince == 0 -> "bugün almıştın" // Ayni gun tekrar onerilmesi nadir ama muAdjust ile mumkun.
    daysSince == 1 -> "dün almıştın"
    else -> "$daysSince gün oldu"
}

/**
 * Ekran 3'un satir gerekcesi - DUZ TURKCE, TAM CUMLE.
 *
 * `reasonText` cip icin kisa ("12 gun oldu"); burasi bir satirin altinda
 * duran aciklama, yani cumle kurmasi gerekiyor. Tasarimin sarti: *"Her
 * oneri duz Turkce bir gerekce tasir"* - gerekcesiz bir oneri reklam gibi
 * okunuyor ve kullanici onu gormemeye basliyor.
 *
 * SIRA ONEMLI: en spesifik bilgi kazaniyor. "Gecen sefer unuttun" bir
 * OLAY; tempo ise bir ORTALAMA - olay her zaman daha aciklayici.
 */
fun Suggestion.longReasonText(): String = when {
    forgottenLastTrip -> "geçen sefer unutmuştun"
    // Sabitte tempo anlatmak gereksiz: kullanici zaten "her zaman al" dedi.
    isStaple -> "her seferinde alıyorsun"
    // Vakti GECMIS: iki sayiyi birlikte vermek sart - "12 gündür almadın"
    // tek basina cok mu az cok mu bilinmiyor.
    daysSince > intervalDays -> "$daysSince gündür almadın, normalde $intervalDays günde bir"
    else -> "genelde $intervalDays günde bir alıyorsun"
}
