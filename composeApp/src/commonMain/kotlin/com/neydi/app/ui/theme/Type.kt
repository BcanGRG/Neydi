package com.neydi.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografi: Plus Jakarta Sans (UI + govde + tum rakamlar) + Fraunces (SADECE display).
 *
 * TODO(font): .ttf dosyalari composeApp/src/commonMain/composeResources/font/ altina
 * konulunca [displayFamily] ve [uiFamily] Font(Res.font.xxx) ile doldurulacak.
 * Simdilik FontFamily.Default - duzen ve olcek dogru, yuz henuz degil.
 *
 * FRAUNCES'I VARIABLE FONT OLARAK BUNDLE ETME. CMP'de FontVariation.Settings
 * iOS'ta guvenilir degil ve hata vermeden varsayilan instance'a duser; Fraunces'in
 * varsayilani opsz=14 (kucuk punto icin optimize, yuksek kontrast) ve 44sp'de yanlis
 * gorunur. fontTools varLib.instancer ile opsz=72 SOFT=30 WONK=0 wght=600
 * ayarinda TEK STATIK TTF uret ve onu bundle et.
 *
 * KURAL: Fraunces yalnizca >= 24sp. Liste satirinda, cipte, butonda, etikette,
 * metadata satirinda ASLA kullanilmaz. Kullanim yeri tam olarak dort tane:
 * fiyat gecmisi mansetti, alisveris sonrasi ozet tutari, kurulum baslıklari,
 * bos durum baslıklari.
 */
private val displayFamily = FontFamily.Default   // -> Fraunces (statik instance)
private val uiFamily = FontFamily.Default        // -> Plus Jakarta Sans

/**
 * Govde minimumu 14sp, 13sp DEGIL. Plus Jakarta Sans'ta optik boyut ekseni yok,
 * yani kucuk punto optik olarak telafi edilmiyor. Satirin ikinci satiri
 * (%60 opaklik) 13sp'de okunabilirlik sinirinin altina duser.
 */
const val BODY_MIN_SP = 14

val NeydiTypography = Typography(
    // display -> Fraunces 600
    displayLarge = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-0.44).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.36).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp, lineHeight = 33.sp, letterSpacing = (-0.30).sp,
    ),

    // headline -> Fraunces 600 (alt sinir 24sp)
    headlineLarge = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = displayFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 29.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),

    // title -> Plus Jakarta Sans 600/700
    titleLarge = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 23.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 21.sp,
    ),

    // body -> Plus Jakarta Sans 400/500
    bodyLarge = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.Medium,
        fontSize = 17.sp, lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.Normal,
        fontSize = BODY_MIN_SP.sp, lineHeight = 20.sp,
    ),

    // label -> Plus Jakarta Sans 500/600
    labelLarge = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 18.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = uiFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
)

/** Liste satirindaki urun adi. Alisveris modunda [ItemNameShopping] ile degisir. */
val ItemName = TextStyle(
    fontFamily = uiFamily, fontWeight = FontWeight.Medium,
    fontSize = 17.sp, lineHeight = 22.sp,
)

/** Alisveris modu: 72dp satir, 20sp / 700. */
val ItemNameShopping = TextStyle(
    fontFamily = uiFamily, fontWeight = FontWeight.Bold,
    fontSize = 20.sp, lineHeight = 25.sp,
)

/**
 * Fiyat ve adet. tabular figures ("tnum") istiyor.
 *
 * TODO(tnum): Skia desteklemedigi OpenType ozelliklerini SESSIZCE yok sayabiliyor.
 * Font baglaninca gercek bir iPhone'da dogrula. Bu yuzden fiyat sutunu ayrica
 * sabit genislik + saga dayali tasarlandi - tnum uygulanmasa da duzen ayakta kalir.
 */
val PriceText = TextStyle(
    fontFamily = uiFamily, fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp, lineHeight = 20.sp,
)

val QuantityBadge = TextStyle(
    fontFamily = uiFamily, fontWeight = FontWeight.ExtraBold,
    fontSize = 20.sp, lineHeight = 24.sp,
)
