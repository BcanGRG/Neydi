package com.neydi.app.ui.list

/**
 * Acik olan miktar sayaci - hangi satirda ve kacinci kez acildigi (karar 107).
 *
 * [seq] bir sayac degil ZAMANLAYICI KIMLIGI: her dokunus uc saniyeyi yeniden
 * kuruyor ve bunun tek yolu eski zamanlayicinin artik kendini gecersiz
 * bilmesi. Sayi olmasaydi, birinci dokunustan uc saniye sonra gelen kapatma
 * emri, ikinci dokunusla uzatilmis sayaci kapatirdi.
 *
 * Ayni bicim `AddedRow`da da var ve ayni sebeple: orada da "yeni bir olay
 * oldu" bilgisi kimlikten cikmiyor.
 */
data class OpenStepper(val rowId: String, val seq: Long)

/** Sayaci acan, uzatan ya da kapatan olaylar. */
sealed interface StepperEvent {
    /** Adet rozetine dokunuldu - aciyor ya da uzatiyor. */
    data class BadgeTap(val rowId: String) : StepperEvent

    /** `+` ya da `-`. Miktari bu olay DEGISTIRMEZ, yalnizca sureyi uzatir. */
    data class Step(val rowId: String) : StepperEvent

    /** Satirin govdesine ya da ekranda baska bir yere dokunuldu. */
    data object TapElsewhere : StepperEvent

    /** Liste kaydirildi. */
    data object Scroll : StepperEvent

    /** Uc saniye doldu - ama HANGI acilisin uc saniyesi. */
    data class Timeout(val seq: Long) : StepperEvent
}

/**
 * Sayacin durum gecisleri (karar 107).
 *
 * ## Neden saf bir fonksiyon
 *
 * Kuralin dordu de zamana ve siraya bagli - *"her dokunus sayaci yeniden
 * kurar; kaydirma ya da baska yere dokunma aninda kapatir"* - ve zamana bagli
 * kurallar cihazda elle sinandiginda en kolay kacan seylerdir: uc saniye
 * beklemek gerekiyor ve bekleyen goz yanilir. Burada beklemek gerekmiyor.
 *
 * ## Neden ViewModel'de tutuluyor, satirda degil
 *
 * `LazyColumn` gorunmeyen satiri geri donusturuyor; satir-yerel bir
 * `remember` kaydirmada olur - oysa kaydirmanin YAPMASI GEREKEN sey sayaci
 * kapatmak, kaybolmak degil. Ve tek sahip olmadan *"yalniz bir satir acik
 * kalir"* kurali yazilamaz.
 *
 * @param nextSeq yeni bir zamanlayici kimligi. Cagiran taraf artan bir sayac
 *   veriyor; fonksiyonun kendi sayaci olsaydi saf olmazdi.
 */
fun reduceStepper(
    current: OpenStepper?,
    event: StepperEvent,
    nextSeq: Long,
): OpenStepper? = when (event) {
    // ROZETE DOKUNMAK KAPATMIYOR, UZATIYOR.
    //
    // Acik sayacta rozete tekrar dokunmak "kapat" gibi de okunabilirdi ama
    // tasarim *"her dokunus sayaci yeniden kurar"* diyor ve rozet de bir
    // dokunus. Kapali kalmadigi icin tuzak da degil: uc saniye sonra
    // kendiliginden kapaniyor.
    //
    // BASKA SATIRIN ROZETI ACIK OLANI DEVRALIYOR: iki sayac ayni anda acik
    // olsaydi "+" hangisine gidiyor sorusu ekrana bakarak cevaplanamazdi.
    is StepperEvent.BadgeTap -> OpenStepper(event.rowId, nextSeq)

    // ADIM YALNIZCA ACIK SATIRDAN GELIYORSA sayiyor - ve baska satirdan
    // gelirse ACIK OLANI KAPATMIYOR, sadece yok sayiliyor.
    //
    // Ikisi ayni sey degil ve fark testte yakalandi: `takeIf` ile yazilinca
    // B satirinin sayaci acikken A'dan gelen bayat bir dokunus B'yi
    // kapatiyordu. Kullanicinin gordugu sey, elini surmedigi bir sayacin
    // birdenbire kaybolmasi olurdu.
    is StepperEvent.Step ->
        if (current?.rowId == event.rowId) current.copy(seq = nextSeq) else current

    StepperEvent.TapElsewhere, StepperEvent.Scroll -> null

    // BAYAT ZAMANLAYICI KENDINI TANIYOR: uzatilmis bir sayaci, ondan onceki
    // acilisin zamanlayicisi kapatamaz.
    is StepperEvent.Timeout -> current?.takeUnless { it.seq == event.seq }
}
