package com.neydi.app.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Nav3 hedefleri.
 *
 * IA'nin temel kurali: LISTE uygulamanin kendisidir, diger her hedef oraya donen
 * bir sapmadir. Bottom navigation YOK, tab YOK, coklu back stack YOK.
 *
 * Liste ekranindaki uc mod (planlama / alisveris / alisveris sonrasi) BURADA
 * hedef DEGILDIR - ayni ekranin state'idir. Onlari NavKey yaparsan ekran sayisi
 * uce katlanir ve tasarimin en buyuk tasarrufu kaybolur.
 *
 * EKLE ve URUN DETAYI de hedef degil, Liste uzerinde acilan bottom sheet'lerdir.
 * Nav3'te bunlar ileride custom Scene olarak eklenecek (bkz. navigation-3
 * bottomsheet recipe); simdilik ekranin kendi state'inde tutuluyorlar.
 */
@Serializable
sealed interface NeydiKey : NavKey

@Serializable
data object Liste : NeydiKey

@Serializable
data object EksikOlabilir : NeydiKey

/** tripId null ise aktif alisveris kapatiliyor; dolu ise Gecmis'ten okuma/duzenleme modu. */
@Serializable
data class AlisverisiBitir(val tripId: String? = null) : NeydiKey

@Serializable
data object Gecmis : NeydiKey

@Serializable
data object Ayarlar : NeydiKey

@Serializable
data object Kurulum : NeydiKey

/**
 * TODO(ios-serialization): rememberNavBackStack varsayilan olarak JVM'de reflection
 * tabanli serialization kullaniyor. iOS ve web'de reflection YOK - orada back stack
 * kalicilığı icin SavedStateConfiguration'a acik bir SerializersModule verip
 * NeydiKey alt tiplerini polymorphic olarak kaydetmek gerekiyor.
 *
 * Kaynak: kotlinlang.org/docs/multiplatform/compose-navigation-3.html
 *
 * Mac'e gecip iOS'u ilk kez derledigimizde yapilacak ilk is bu. Android'de
 * simdiden calisiyor, o yuzden bugun bloklayici degil - ama iOS'ta sessizce
 * "back stack restore olmuyor" seklinde tezahur eder, o yuzden buraya yazildi.
 */
