package com.neydi.app.ui.capture

import com.neydi.app.data.ocr.SUPPORTED_CHAINS
import com.neydi.app.data.ocr.TagSkip
import com.neydi.app.data.ocr.grammarFor
import com.neydi.app.data.store.chainKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Kartin kullaniciya soyledigi cumleler.
 *
 * Kart iki farkli yuzeyden konusuyor ve ikisi AYNI ANDA konusmamali:
 * desteklenmeyen zincirde kartin basindaki cumle, digerlerinde amber serit.
 */
class ConfirmCardMessageTest {

    private fun card(
        price: String = "24,90",
        product: String = "Süt 1 L",
        skipped: TagSkip? = null,
        kurus: Boolean = true,
    ) = ConfirmCard(
        photoPath = "/x.jpg",
        priceText = price,
        productName = product,
        reading = false,
        kurusFromOcr = kurus,
        skipped = skipped,
    )

    /**
     * KARAR 49'UN CUMLESI ELLE YAZILMIYOR.
     *
     * Bu testin isi metni ezberlemek degil - zincir adlarinin [SUPPORTED_CHAINS]'den
     * geldigini kanitlamak. Elle yazilmis bir cumle, ucuncu gramer eklendiginde
     * SESSIZCE yalan olur: okuyabildigimiz bir marketi okuyamiyoruz diye soyler
     * ve kullaniciyi gereksiz yere elle yazmaya gonderir.
     */
    @Test
    fun `desteklenmeyen zincir cumlesi gramer kaydindan kuruluyor`() {
        val c = card(price = "", skipped = TagSkip.UNSUPPORTED_CHAIN)
        assertEquals(
            "A101, BİM ve Migros etiketlerini okuyabiliyoruz; burada fiyatı sen yaz.",
            c.unsupportedChainMessage(),
        )
        // DORDUNCU ZINCIR: cumle sabit yazilmis olsaydi bu satir gecmezdi.
        // Yer tutucu LISTEDE OLMAYAN bir ad olmali - burada once "A101"
        // yaziyordu ve A101 grameri gercekten yazilinca ad listeye girdi, yani
        // test kendi yer tutucusunu kaybetti.
        assertEquals(
            "A101, BİM, Migros ve ŞOK etiketlerini okuyabiliyoruz; burada fiyatı sen yaz.",
            c.unsupportedChainMessage(SUPPORTED_CHAINS + "ŞOK"),
        )
        // TEK ZINCIR: virgulsuz, "ve"siz.
        assertEquals(
            "BİM etiketlerini okuyabiliyoruz; burada fiyatı sen yaz.",
            c.unsupportedChainMessage(listOf("BİM")),
        )
    }

    /** Liste ile [grammarFor] birlikte degismek ZORUNDA - biri otekini yalanlayamaz. */
    @Test
    fun `listedeki her zincirin grameri gercekten var`() {
        SUPPORTED_CHAINS.forEach {
            assertTrue(grammarFor(chainKey(it)) != null, "$it listede ama grameri yok")
        }
    }

    /**
     * DESTEKLENMEYEN ZINCIRDE SERIT YOK (karar 49).
     *
     * Cumle seridin isini zaten yapiyor; ikisi birden cizilseydi kullaniciya
     * iki ayri is varmis gibi gorunurdu. Kartin kendi kurali "birden cok alan
     * bossa yalnizca ilki" - burada o kural konu disi kaliyor.
     */
    @Test
    fun `desteklenmeyen zincirde amber serit cizilmiyor`() {
        val c = card(price = "", skipped = TagSkip.UNSUPPORTED_CHAIN)
        assertNull(c.missingFieldMessage())
    }

    @Test
    fun `celiskili fiyatin kendi cumlesi var`() {
        val c = card(price = "", skipped = TagSkip.PRICE_CONTRADICTS_UNIT_PRICE)
        assertEquals("Okunan fiyat etiketin birim fiyatıyla uyuşmuyor — doğrula", c.missingFieldMessage())
        assertNull(c.unsupportedChainMessage(), "iki yuzey ayni anda konusuyor")
    }

    /**
     * "OKUNAMADI" DEGIL "SECILMEDI".
     *
     * OCR metni artik urun adi olmuyor (karar 51), yani adin bos olmasi bir
     * okuma hatasi degil. Eski cumle kullaniciyi etikete bakmaya gonderirdi;
     * oysa yapmasi gereken sey listeden secmek.
     */
    @Test
    fun `urun adi eksikse secim isteniyor okuma degil`() {
        assertEquals("Ürün seçilmedi — seç", card(product = "").missingFieldMessage())
    }

    /**
     * KAYDET ILK RAKAMDA ETKINLESIR - sozlesmenin birebir kurali.
     *
     * Urun adi SART DEGIL: adsiz kalabilecegi tek hal ilk kurulumun ilk cekimi
     * ve orada `save()` urun secicisini aciyor.
     */
    @Test
    fun `kaydet yalnizca fiyata bakiyor`() {
        assertTrue(card(product = "").canSave)
        assertTrue(!card(price = "").canSave)
    }
}
