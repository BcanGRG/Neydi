package com.neydi.app.data.ocr

import com.neydi.app.data.store.chainKey
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Ad blogunun UST siniri (`docs/41`).
 *
 * ## Blogun alti yaziliydi, ustu degildi
 *
 * Gramaj satiri blogu asagidan bitiriyordu (`250 G`) ve o kural olculmustu.
 * Yukaridan bitiren hicbir sey yoktu - kadraja giren ne varsa bloga
 * girebiliyordu: etiketin kendi besin degerleri tablosu, urunun ambalaji,
 * raf tabelasi, komsu etiketin kunyesi.
 *
 * ## Ayirac satir arasi bosluk, ve olculdu
 *
 * 46 etiketin (27 BIM + 19 A101) sol kolonu olculdu:
 *
 * | | bosluk / lira boyu |
 * |---|---|
 * | blok ici, olculen en buyuk | **0,62** |
 * | davetsiz, olculen en kucuk | **1,55** |
 *
 * Esik 1,0 - ikisinin ortasi. Gercek bir satiri dusurmek icin araligin %60
 * buyumesi, bir davetsizi kacirmak icin %35 daralmasi gerekiyor.
 *
 * ## Bu testler DAVRANISI yaziyor, esigi degil
 *
 * Esik bir sayi; onemli olan hangi satirin dustugu. Asagidaki vakalar
 * olcumun kendi kanitlari - dordu de gercek fotograflardan.
 */
class NameBlockBoundaryTest {

    private fun name(tag: String, chain: String = TagFixtures.chainOf(tag)) =
        readTagFields(TagFixtures.all.getValue(tag), chainKey(chain)).name

    /**
     * ETIKETIN KENDI BESIN TABLOSU BLOGA GIRMIYOR.
     *
     * `183949`da `Yağlg)` (bozulmus `Yağ (g)`) ad blogunun ILK satiri
     * sayiliyordu ve marka yuvasina oturuyordu. Blogun 1741 piksel
     * uzagindaydi - lira boyunun 3,84 kati.
     *
     * ⚠ Bu vaka ayni zamanda kazanc: dogru marka (`AYCA`) ancak cop
     * cekildikten sonra gorunur oldu.
     */
    @Test
    fun theLabelsOwnNutritionTableStaysOutOfTheNameBlock() {
        val n = name("20260817_183949")
        assertEquals("AYCA", n?.brand)
        assertEquals("PEYNİR CEŞİTLERİ", n?.name)
    }

    /**
     * KADRAJA GIREN URUN AMBALAJI BLOGA GIRMIYOR.
     *
     * `183947`de `İÇİM Süzme Peynir`in ambalaji fotografin ustunde ve OCR onu
     * `Zme` + `aği Taze Peynir` diye parcalamis; ikisi de ada kariyordu.
     * Blogun 1656 piksel uzagindalar (3,89 kat).
     *
     * ⚠ Marka hala kusurlu (`tçİM.` - bozulmus `İÇİM`) ama ARTIK DOGRU SEYI
     * gosteriyor; ad tamamen temizlendi.
     */
    @Test
    fun theProductsOwnPackagingStaysOutOfTheNameBlock() {
        assertEquals("SÜZME PEYNİR TAM YAĞLI", name("20260817_183947")?.name)
    }

    /**
     * RAF TABELASININ KIRINTILARI ADA KARISMIYOR.
     *
     * `184116`da ML Kit raf tabelasindan `U` ve `L` diye iki parca kesmis ve
     * ad `U L FIÇI KORNİŞON TURŞUSU` cikiyordu. Bu, olcumun ARAMADIGI ama
     * bulundugu ucuncu vaka - `docs/40` yalnizca iki marka copu saymisti,
     * cunku burada kirlenen ADdi.
     *
     * Bosluk 771 piksel, lira boyunun 1,55 kati: olculen en KUCUK davetsiz,
     * yani esigin gercek sinavi bu vaka.
     */
    @Test
    fun theShelfSignsCrumbsStayOutOfTheName() {
        assertEquals("KORNİŞON TURŞUSU", name("20260817_184116")?.name)
    }

    /**
     * A101'IN UZUN KIRLI ADLARI DA TEMIZLENDI - ve bu bir yan kazanc.
     *
     * Kural BIM'in blogu icin yazildi, ama `A101Grammar.readName` ayni
     * `readTagName`i cagiriyor. Bes A101 etiketinde ad kisaldi ve hepsi
     * duzeldi; en carpicisi bu: tarih, birim fiyat ve KDV cumlesi adin
     * icindeydi.
     */
    @Test
    fun theA101NamesLoseTheirLeadingClutter() {
        assertEquals(
            "BİRŞAH SÜT YARIM YAĞLI (EN; AZ /o1,5 YAĞLI)",
            name("20260821_133226")?.name,
        )
        assertEquals("PARODONTAX DIŞ MACUNU", name("20260821_133411")?.name)
        assertEquals("TORKU BANADA KAKAOLU FINDIK KREMASI", name("20260821_133211")?.name)
    }

    /**
     * OLCULEN TEK KAYIP YAZILI: `FERRERO` dusuyor.
     *
     * `133214`te uretici adi etiketin TEPESINDE, blogun 954 piksel uzaginda
     * basili (3,37 kat) - yani gercek davetsizlerle AYNI mesafede. `FERRERO`yu
     * tutup `Yağlg)`yi (3,84) duşuren bir esik yok; ikisi ayni uzaklikta.
     *
     * Kayip kabul edildi cunku kucuk: `NUTELLA KAKAOLU FINDIK KRM.` tam bir
     * urun adi ve `NUTELLA` zaten insanlarin soyledigi marka. Test bunu
     * KAYDEDIYOR - bir gun baska bir sinyalle geri kazanilirsa burasi duser.
     */
    @Test
    fun theOneMeasuredLossIsWrittenDown() {
        assertEquals("NUTELLA KAKAOLU FINDIK KRM.", name("20260821_133214")?.name)
    }

    /**
     * KISA BLOKLARA DOKUNULMUYOR.
     *
     * Iki satirlik bir blokta kesecek bir sey yok; kural yalnizca ARAYA giren
     * boslugu ariyor. `183839` (`ARBELLA MAKARNA`) ve `184007` (`SEK
     * TEREYAĞI`) degismeden geciyor.
     */
    @Test
    fun aTightTwoLineBlockIsUntouched() {
        assertEquals("ARBELLA", name("20260817_183839")?.brand)
        assertEquals("MAKARNA", name("20260817_183839")?.name)
        assertEquals("SEK", name("20260817_184007")?.brand)
        assertEquals("TEREYAĞI", name("20260817_184007")?.name)
    }
}
