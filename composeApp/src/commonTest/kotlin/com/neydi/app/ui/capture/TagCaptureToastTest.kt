package com.neydi.app.ui.capture

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Kaydetme bildiriminin BIREBIR metni.
 *
 * Tasarim cumleyi harfi harfine veriyor: `Gözlem kaydedildi · BİM · 24,90 TL`.
 * Ayirici ORTA NOKTA (U+00B7), tire degil; para Turkce yazimla (virgul ondalik).
 * Kod once yalnizca `Fiyat kaydedildi` yaziyordu.
 */
class TagCaptureToastTest {

    @Test
    fun `market ve tutar cumlede gecer`() {
        assertEquals("Gözlem kaydedildi · BİM · 24,90 TL", savedToast("BİM", 2490))
    }

    @Test
    fun `market secilmemisse o parca dusuyor`() {
        // Bos bir alan ya da "-" uydurmaktansa cumle kisaliyor.
        assertEquals("Gözlem kaydedildi · 24,90 TL", savedToast(null, 2490))
    }

    @Test
    fun `binlik ayiriciyi para bicimi getiriyor`() {
        assertEquals("Gözlem kaydedildi · Migros · 1.512,84 TL", savedToast("Migros", 151284))
    }

    /**
     * ENGELLENEN SILMENIN CUMLESI SAYIYI TASIYOR (karar 122).
     *
     * Tasarimin verdigi metin birebir bu. *"Silinemez"* demiyor: sebepsiz bir
     * ret kullaniciya uygulamanin bozuk oldugunu dusundururdu, oysa yol
     * kapali degil - beyan kaldirilinca silme yeniden aciliyor. Sayi
     * kullaniciya kaybedecegi seyin BOYUNU soyluyor.
     */
    @Test
    fun `engellenen silme kac satiri korudugunu yaziyor`() {
        assertEquals("Bu markete giden 3 satır var.", blockedStoreDeleteMessage(3))
        assertEquals("Bu markete giden 1 satır var.", blockedStoreDeleteMessage(1))
    }
}
