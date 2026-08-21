package com.neydi.app.ui.capture

import com.neydi.app.data.ocr.TagFixtures
import com.neydi.app.data.ocr.readTagFields
import com.neydi.app.data.store.chainKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Okunan alanlar karta tasiniyor mu - GERCEK etiketlerle.
 *
 * Bu testin var olma sebebi bir kusur. F5.7'ye kadar kopya gramaji hic
 * tasimiyordu: sema kolonlari, sorgunun alt sorgulari ve
 * `PriceHint.PackChanged` dali hazirdi, `readTagPack` gramaji okuyordu, ama
 * ViewModel'de `pack` kelimesi HIC GECMIYORDU. Her gozlem iki NULL kolonla
 * yaziliyordu ve shrinkflation asla atesleyemiyordu.
 *
 * Hicbir test bunu soylemedi cunku ViewModel'in govdesi test edilmiyordu.
 * Kopya artik serbest bir fonksiyon ([readFrom]) ve bir alanin sessizce
 * dusurulmesi buradan kirmizi yakiyor.
 */
class ConfirmCardReadTest {

    private val a101 = chainKey("A101")
    private val bim = chainKey("BİM")
    private val blank = ConfirmCard(photoPath = "/tmp/x.jpg")

    private fun cardFor(tag: String, chain: String) =
        blank.readFrom(readTagFields(TagFixtures.all.getValue(tag), chain))

    /**
     * GRAMAJ KARTA ULASIYOR - F5.7'nin kirilan halkasi tam olarak buydu.
     *
     * `132811` LAYS 125 G. Okumanin kendisi de dogrulaniyor ki fikstur
     * degistiginde test dayanaksiz kalmasin.
     */
    @Test
    fun theTagsPackReachesTheCard() {
        val card = cardFor("20260821_132811", a101)
        assertEquals(125.0, card.packSize)
        assertEquals("gr", card.packUnit)
    }

    /**
     * CELISKILI ETIKETTE NE FIYAT NE GRAMAJ.
     *
     * `133411` kadrajinda iki etiket var; manset birinin, birim fiyat ve
     * gramaj otekinin. Kart bos aciliyor ve amber serit sebebini soyluyor.
     */
    @Test
    fun aContradictedTagFillsNeitherPriceNorPack() {
        val card = cardFor("20260821_133411", a101)
        assertEquals("", card.priceText)
        assertNull(card.packSize)
        assertNull(card.packUnit)
        assertNotNull(card.skipped, "amber serit sebepsiz kalir")
    }

    /**
     * GRAMAJI OKUNMAYAN ETIKET KARTI BOS BIRAKIYOR - varsayilan KOYMUYOR.
     *
     * `183808` bulanik cekim. `1.0` gibi bir varsayilan konsaydi her okunamayan
     * etiket bir sonraki gercek olcumle karsilastirildiginda sahte bir ambalaj
     * degisimi uretirdi.
     */
    @Test
    fun anUnreadPackIsLeftNullNotDefaulted() {
        val card = cardFor("20260817_183808", bim)
        assertNull(card.packSize)
        assertNull(card.packUnit)
    }

    /**
     * OCR PATLARSA kart yine de ACILIYOR ve iskelet KAPANIYOR.
     *
     * `readFields` firlatirsa cagiran `null` geciyor. `reading` acik kalsaydi
     * kart sonsuza kadar iskelet cizerdi ve kullanici Vazgec'ten baska hicbir
     * sey yapamazdi - fiyati elle yazamazdi bile.
     */
    @Test
    fun aFailedReadStillOpensAnEmptyCard() {
        val card = blank.readFrom(null)
        assertTrue(!card.reading)
        assertEquals("", card.priceText)
        assertNull(card.packSize)
        assertNull(card.tagText)
    }

    /** OCR metni urun adi OLMUYOR (karar 51) - kanit olarak ayri alanda. */
    @Test
    fun theTagTextNeverBecomesTheProductName() {
        val card = cardFor("20260821_132811", a101)
        assertEquals("", card.productName)
        assertNotNull(card.tagText, "etiket metni ipucu kayboldu")
    }
}
