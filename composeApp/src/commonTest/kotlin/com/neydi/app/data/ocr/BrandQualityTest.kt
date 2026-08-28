package com.neydi.app.data.ocr

import com.neydi.app.data.store.chainKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Marka okumasinin CORPUS OLCUMU - 99 gercek etiket fiksturu.
 *
 * ## Neden tek tek degil corpus
 *
 * Marka kurallari tek tek vakalardan dogdu (`oOoao000`, `Tntkn`, `A.Ş.`,
 * `MENSE ULKE:TURKYE`) ve her biri kendi testini getirdi. Ama bir eleme
 * kuralinin ASIL riski yakalayamadigi cop degil, **beraberinde goturdugu
 * gercek marka** - ve o yalnizca butun corpus sayilinca gorunuyor.
 *
 * Bu yuzden buradaki testler tek bir etiketi degil SAYIYI kilitliyor. Sayi bir
 * hedef degil bir OLCUM: degismesi gereken sayiyi degistirmek serbest, FARK
 * ETMEDEN degistirmek degil.
 *
 * ## Olcumun bugunku hali (24 Agustos 2026, ust sinir kuralindan SONRA)
 *
 * | Zincir | Etiket | Ad okunuyor | Marka | Not |
 * |---|---|---|---|---|
 * | BIM | 27 | 27 | **25** (23 gercek, 2 marka-olmayan) | `docs/41` ust siniri |
 * | A101 | 19 | 19 | 0 | **karar 39** - marka tahmin edilmiyor, ada katiliyor |
 * | Migros | 19 | 0 | 0 | olculmus ret, `MigrosGrammar.readName` |
 * | Metro | 34 | 0 | 0 | grameri yazilmadi |
 *
 * A101 ve Migros'un sifirlari birer KARAR, eksiklik degil; ikisi de kendi
 * dosyalarinda gerekcesiyle yazili.
 */
class BrandQualityTest {

    /**
     * Marka OLMAYAN iki cikti - `docs/41`'de gerekcesiyle.
     *
     * ⚠ Ikisi de ust sinir kuralindan SONRAKI hal ve ikisi de oncekinden
     * zararsiz:
     * - `tçİM.` bozulmus ama DOGRU markayi gosteriyor (`İÇİM`); onceki hali
     *   `Zme` idi, hicbir seyi gostermiyordu.
     * - `FIÇI` adin kendi kelimesi, marka degil - ama satirin adi
     *   (`KORNİŞON TURŞUSU`) artik temiz; onceki hali `U L FIÇI KORNİŞON
     *   TURŞUSU` idi.
     */
    private val markaOlmayan = mapOf(
        "20260817_183947" to "tçİM.",
        "20260817_184116" to "FIÇI",
    )

    private fun brands(): List<Pair<String, String>> = TagFixtures.all.keys.sorted().mapNotNull { tag ->
        val chain = TagFixtures.chainOf(tag)
        val brand = readTagFields(TagFixtures.all.getValue(tag), chainKey(chain)).name?.brand
        brand?.let { tag to it }
    }

    /**
     * CORPUSTA 24 MARKA URETILIYOR VE HEPSI BIM'DEN.
     *
     * A101 markayi bilerek uretmiyor (karar 39), Migros adin kendisini
     * uretmiyor, Metro'nun grameri yok. Yani marka okumasi bugun tek bir
     * zincirin ozelligi - ve bunu bilmek, "marka kalitesi" diye tek bir yuzde
     * konusmaktan daha dogru.
     */
    @Test
    fun theCorpusYieldsTwentyFiveBrandsAndAllOfThemFromBim() {
        val found = brands()
        assertEquals(25, found.size, "corpus marka sayisi degisti: ${found.map { it.second }}")
        assertTrue(
            found.all { TagFixtures.chainOf(it.first) == "BIM" },
            "BIM disinda marka uretildi: ${found.filterNot { TagFixtures.chainOf(it.first) == "BIM" }}",
        )
    }

    /**
     * YIRMI DORDUN YIRMI IKISI GERCEK MARKA, IKISI COP - VE COPLER ADLI.
     *
     * Test copleri KABUL ETMIYOR, KAYDEDIYOR. Ikisi de bugun uretiliyor ve
     * ikisi de acik is; adlariyla yazili olmalari, bir gun duzeltildiginde bu
     * testin dusup dikkat cekmesi icin.
     *
     * ⚠ ISIRMA NOKTASI TERSINE: bu test cop SAYISI AZALINCA da duser. Dusmesi
     * iyi haber demek - beklenen listeyi guncelleyip gecin.
     */
    @Test
    fun theOnlyTwoNonBrandsAreTheTwoTheMeasurementNamed() {
        val found = brands().toMap()
        markaOlmayan.forEach { (tag, cikti) ->
            assertEquals(cikti, found[tag], "$tag: marka-olmayan cikti degisti")
        }
        val gercek = found.filterKeys { it !in markaOlmayan.keys }
        assertEquals(23, gercek.size)
        // Gercek markalarin hepsi buyuk harf; copler degil. Ayirac olculdu ama
        // KURAL YAPILMADI - tek basina copu marka yuvasindan ad yuvasina
        // TASIYOR, kaldirmiyor (bkz. `docs/40`).
        assertTrue(
            gercek.values.all { b -> b.all { !it.isLetter() || it.isUpperCase() } },
            "buyuk harf olmayan gercek marka cikti: ${gercek.values}",
        )
        // ⚠ AYIRAC ARTIK AYIRMIYOR: `FIÇI` tamamen buyuk harf. Ust sinir
        // kurali copun sinifini degistirdi - kalan iki cikti artik "kadraja
        // giren yabanci metin" degil, "bloktaki ilk satir marka degil".
        // Buyuk harf ayiraci `docs/40`'ta zaten uygulanmamisti; artik
        // ayirmadigi da yazili.
        assertTrue(
            markaOlmayan.values.any { b -> b.all { !it.isLetter() || it.isUpperCase() } },
            "beklenen: en az biri buyuk harf (FIÇI)",
        )
    }

    /**
     * COPUN YANINDAKI AD DOGRU - ve duzeltme bunu bozmamali.
     *
     * `183949`un adi (`AYCA PEYNİR CEŞİTLERİ`) copa RAGMEN dogruydu; yanlis
     * olan yalnizca marka yuvasiydi. Copu eleyen naif bir kural (marka buyuk
     * harf olmali) bu adi `Yağlg) AYCA PEYNİR CEŞİTLERİ` yapiyordu - defekti
     * kaldirmiyor, TASIYORdu. Denendi, olculdu, geri alindi (`docs/40`).
     *
     * Ust sinir kurali (`docs/41`) dogru olani yapti: copu bloktan cikardi ve
     * `AYCA` kendi yuvasina gecti. Bu test o tuzagin nobetcisi.
     */
    @Test
    fun theNameNextToTheScrapSurvivedTheFix() {
        val fields = readTagFields(TagFixtures.all.getValue("20260817_183949"), chainKey("BIM"))
        // Cop cekilince marka (`AYCA`) adin basindan cikip kendi yuvasina
        // gecti - ad hala DOGRU, sadece markasi ayrildi.
        assertEquals("PEYNİR CEŞİTLERİ", fields.name?.name)
        assertEquals("AYCA", fields.name?.brand)
    }

    /**
     * MIGROS AD OKUMUYOR VE BU BIR KARAR.
     *
     * `MigrosGrammar.readName` bilerek `null` donuyor: 19 etikette denenen uc
     * yol da (kunye sozlugu, boy esigi, sesli orani) `NUIK KREMIASI ...`,
     * `Metket Edime OSBFbr ...` gibi ciktilar verdi, cunku Migros
     * fotograflarinin cogunda urunun kendi ambalaji da kadrajda ve ondan
     * BUYUK basili. Fiyat tarafi ayni fiksturlerde 17/19 dogru ve sifir
     * yanlis; ikisini ayirmak bu yuzden mumkun.
     *
     * Test bunu KILITLIYOR ki bir gun "Migros ad okumuyor, ekleyelim" diyen
     * biri once olcumu okusun.
     */
    @Test
    fun migrosReadsPricesButDeliberatelyRefusesNames() {
        val migros = TagFixtures.of("Migros")
        assertEquals(19, migros.size)
        migros.forEach { (tag, ocr) ->
            assertEquals(null, readTagFields(ocr, chainKey("Migros")).name, "$tag: Migros ad uretti")
        }
    }

    /**
     * A101 ADI OKUYOR AMA MARKAYI AYIRMIYOR (karar 39).
     *
     * On dokuz etiketin on dokuzunda ad var, sifirinda marka. Marka atilmiyor
     * - ad metnine katiliyor, cunku *"Etiket metni: LAYS PATATES CIPSI"*
     * kullaniciya *"PATATES CIPSI"*den daha cok sey soyluyor.
     */
    @Test
    fun a101ReadsNamesButNeverSeparatesABrand() {
        val a101 = TagFixtures.of("A101")
        assertEquals(19, a101.size)
        var adli = 0
        a101.forEach { (tag, ocr) ->
            val name = readTagFields(ocr, chainKey("A101")).name
            if (name != null) {
                adli++
                assertEquals(null, name.brand, "$tag: A101'de marka tahmin edildi")
            }
        }
        assertEquals(19, adli, "A101'de ad okunmayan etiket cikti")
    }
}
