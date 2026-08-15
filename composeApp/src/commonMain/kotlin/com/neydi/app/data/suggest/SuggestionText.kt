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
