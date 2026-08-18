package com.neydi.app.data.ocr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [readTagName] + [readTagPack], 27 GERCEK BIM etiketinin ham OCR ciktisina karsi.
 *
 * Beklentiler uydurulmadi: `etiket-fikstur/` altindaki dokumlerden okundu ve her
 * biri fotografta gozle dogrulanabilir. Fikstur degisirse test kirilir, ki
 * kirilmasi gerekir - fikstur bu ozelligin tek yer gercegi.
 */
class TagFieldReaderTest {
    @Test
    fun readsBrandNameAndPack() {
        val cases = mapOf(
            "20260817_183645" to Triple("ETI", "Cici BEBE BEBEK BİSKÜVİsi", 1.0 to "kg"),
            "20260817_183704" to Triple("EFSANE", "BUĞDAY UNU", 2.0 to "kg"),
            "20260817_183711" to Triple("SOFRA", "İYOTLU TUZ", 750.0 to "gr"),
            "20260817_183728" to Triple("CENTRO", "GOFRET FINDIK KREMALI", 142.0 to "gr"),
            "20260817_183746" to Triple("ŞAFAK", "PUDRA ŞEKERİ", 250.0 to "gr"),
            "20260817_183839" to Triple("ARBELLA", "MAKARNA", 500.0 to "gr"),
            "20260817_183847" to Triple("SABAN", "YEŞİL MERCİMEK", 1.0 to "kg"),
            "20260817_184007" to Triple("SEK", "TEREYAĞI", 500.0 to "gr"),
            "20260817_184031" to Triple("DOST", "SÜT %0,1 YAĞLI", 1.0 to "lt"),
            "20260817_184101" to Triple("SOLE", "AYÇİÇEK YAĞI PET ŞİŞE", 5.0 to "lt"),
            "20260817_184138" to Triple("PATOS", "MISIR CİPSİ ACI BAHARATLI", 185.0 to "gr"),
            "20260817_184202" to Triple("DAPHNE", "PASTA KREMASI VANİLİNLİ", 140.0 to "gr"),
            "20260817_184239" to Triple("TORKU BANADA", "KAKAOLU FINDIK KREMASI", 920.0 to "gr"),
        )
        cases.forEach { (tag, expected) ->
            val ocr = TagFixtures.all.getValue(tag)
            val name = readTagName(ocr)
            assertEquals(expected.first, name?.brand, "$tag markasi")
            assertEquals(expected.second, name?.name, "$tag adi")
            val pack = readTagPack(ocr)
            assertEquals(expected.third.first, pack?.size, "$tag gramaji")
            assertEquals(expected.third.second, pack?.unit, "$tag birimi")
        }
    }

    /**
     * ARALIK TASIYAN GRAMAJ gramaj DEGIL: `53-62 G` yumurta boyu araligi.
     *
     * Ad blogunda kaliyor mu diye de bakiliyor - reddetmek onu SILMEK olmamali,
     * kullanici etikette ne yaziyorsa gorsun.
     */
    @Test
    fun rejectsRangePackButKeepsTheNameIntact() {
        val ocr = TagFixtures.all.getValue("20260817_183635")
        assertNull(readTagPack(ocr), "53-62 G bir aralik, tek sayiya indirilmemeli")
        val name = readTagName(ocr)
        assertEquals("BİLİ BİLİ", name?.brand)
        assertEquals("30'LU YUMURTA", name?.name)
    }

    /**
     * `30'LU YUMURTA` GRAMAJ SANILMIYOR.
     *
     * Gevsek bir desen bu satiri gramaj sayardi ve ad blogunu yanlis yerde
     * bitirirdi - urun adini tumden kaybederdik. Ustteki test adin sag kaldigini
     * gosteriyor; bu test SEBEBI kilitliyor.
     */
    @Test
    fun quantityWordIsNotAPackSize() {
        val ocr = TagFixtures.all.getValue("20260817_183635")
        assertTrue(
            ocr.lines.any { it.text.contains("YUMURTA") },
            "fikstur degistiyse bu testin dayanagi kalmadi",
        )
        assertEquals("30'LU YUMURTA", readTagName(ocr)?.name)
    }

    /** Paket carpani (`12Lİ`) ADIN parcasi; raf adedi (`X 34 Adet`) degil. */
    @Test
    fun keepsPackMultiplierButDropsShelfCount() {
        assertEquals(
            "PARF.TUV.KAĞIDI 3 KATLI 12Lİ",
            readTagName(TagFixtures.all.getValue("20260817_184300"))?.name,
        )
        val counted = readTagName(TagFixtures.all.getValue("20260817_184206"))
        assertEquals("DAPHNE", counted?.brand)
        assertEquals("KAKAO", counted?.name)
    }

    /**
     * MAGAZA KODU (`P728`) addan atiliyor.
     *
     * `184300`de kod ad blogunun hizasina basilmis; suzulmezse adin kuyruguna
     * takiliyordu. 27 etiketin hepsinde var, hicbirinde urun adi degil.
     */
    @Test
    fun dropsStoreCode() {
        TagFixtures.of("BIM").forEach { (tag, ocr) ->
            val name = readTagName(ocr) ?: return@forEach
            assertTrue(
                !name.name.contains("P728") && name.brand?.contains("P728") != true,
                "$tag: magaza kodu ada sizdi -> ${name.brand} / ${name.name}",
            )
        }
    }

    /**
     * RAF TABELASI ad sanilmiyor - onu eleyen KOLON suzgeci.
     *
     * Genis cekimde rafin kendi tabelasi kadraja giriyor (`Krena`, `Şekeňm`).
     * Ilk yazdigimda bunu yukseklik esigine bagladim; test isirmasi yanlisladi -
     * esigi kaldirinca bu test AYAKTA kaldi. Gercek sebep: tabela etiketin butun
     * genisligini kapliyor, yani sag kenari fiyat kolonuna tasiyor.
     *
     * Test o yuzden yalnizca sonucu degil GEOMETRIYI de dogruluyor: iddianin
     * dayanagi tabelanin genis olmasi.
     */
    @Test
    fun shelfBannerIsNotTheName() {
        listOf("20260817_184202" to "Krena", "20260817_183746" to "Şekeňm").forEach { (tag, banner) ->
            val ocr = TagFixtures.all.getValue(tag)
            val line = ocr.lines.firstOrNull { it.text == banner }
            assertTrue(line != null, "$tag: tabela satiri fiksturden kaybolmus, test dayanaksiz")
            val lira = ocr.lines
                .filter { it.text.trimStart().firstOrNull()?.isDigit() == true }
                .maxByOrNull { it.corners[3].y - it.corners[0].y }!!
            assertTrue(
                line.corners[1].x >= lira.corners[0].x,
                "$tag: tabela artik fiyat kolonuna tasmiyor, bu testin gerekcesi degisti",
            )
            val name = readTagName(ocr)
            assertTrue(
                !name!!.name.contains(banner) && name.brand != banner,
                "$tag: tabela ada sizdi -> ${name.brand} / ${name.name}",
            )
        }
    }

    /**
     * BASARISIZ CEKIM null donuyor, uydurma bir ad DONMUYOR.
     *
     * `183808` bulanik; en buyuk glifi 12 piksel, yani OCR neredeyse hicbir sey
     * okumamis. Bos degil YANLIS bir ad dondurmek, kullaniciya okunamadigini
     * soylemekten kotu olurdu.
     *
     * Manset boyu oranini da olcuyorum, cunku kural buna dayaniyor: kose verisi
     * bozuk (yukseklikler negatif) ve tek saglam sinyal mansetin kaynaga gore
     * absurt kucuk olmasi.
     */
    @Test
    fun blurredTagReadsNothing() {
        val ocr = TagFixtures.all.getValue("20260817_183808")
        val tallest = ocr.lines.maxOf { it.corners[3].y - it.corners[0].y }
        assertTrue(
            tallest < ocr.sourceHeight * 0.02,
            "cekim artik bulanik degil ($tallest / ${ocr.sourceHeight}), test dayanaksiz",
        )
        assertNull(readTagName(ocr))
        assertNull(readTagPack(ocr))
    }

    /**
     * URETILEN HER MARKA VE AD SOZCUK GORUNUMLU.
     *
     * ## Bu testin kaniti FIKSTURDE DEGIL, CIHAZDA
     *
     * Kural gercek bir BIM turundan geldi: uygulamadan cekilen 12 gozlemin
     * markalari arasinda `oOoao000`, `Tntkn` ve `A.Ş.` vardi, ve bir muz
     * etiketi `KG` adiyla katalogda kalici bir urun yaratti. O fotograflar
     * fiksturde YOK - silindiler (karar 29).
     *
     * Yani bu test kurali KANITLAMIYOR, KORUYOR: 27 BIM etiketinin urettigi
     * hicbir marka/ad kuralin disina dusmuyor, dolayisiyla kural mevcut dogru
     * ciktilari kirmadan duruyor. Kanit cihaz turunun kendisi.
     *
     * Ayrimi yaziyorum cunku "test var" ile "kural olculdu" ayni sey degil ve
     * bu projede o ayrim daha once pahaliya patladi.
     */
    @Test
    fun everyEmittedBrandAndNameLooksLikeAWord() {
        TagFixtures.of("BIM").forEach { (tag, ocr) ->
            val name = readTagName(ocr) ?: return@forEach
            name.brand?.let { brand ->
                assertTrue(brand.none(Char::isDigit), "$tag: markada rakam -> $brand")
                assertTrue(brand.count(Char::isLetter) >= 3, "$tag: marka cok kisa -> $brand")
            }
            assertTrue(name.name.count(Char::isLetter) >= 3, "$tag: ad cok kisa -> ${name.name}")
        }
    }

    /**
     * RAKAM ADDA MESRU, MARKADA DEGIL.
     *
     * Ilk yazdigimda tek kural vardi ve `30'LU YUMURTA`, `SÜT %0,1 YAĞLI`,
     * `PARF.TUV.KAĞIDI 3 KATLI 12Lİ` gibi gercek adlari reddetti - dort test
     * birden kirmizi yandi. Rakam markada suphe isareti, adda siradan.
     */
    @Test
    fun digitsAreOrdinaryInNamesEvenThoughTheyAreSuspectInBrands() {
        listOf(
            "20260817_183635" to "30'LU YUMURTA",
            "20260817_184031" to "SÜT %0,1 YAĞLI",
            "20260817_184300" to "PARF.TUV.KAĞIDI 3 KATLI 12Lİ",
        ).forEach { (tag, expected) ->
            assertEquals(expected, readTagName(TagFixtures.all.getValue(tag))?.name, tag)
        }
    }

    /**
     * 27 ETIKETIN KACINDA CALISIYOR - olculen oran, iddia degil.
     *
     * Sayiyi teste yaziyorum ki bir degisiklik onu SESSIZCE dusuremesin.
     *
     * Ad 26/27: eksik olan `183808`, bulanik cekim.
     *
     * Gramaj 23/27 ve DORT eksigin her biri gerekceli:
     * - `183635` - `53-62 G` aralik, bilincli reddediliyor
     * - `183808` - bulanik cekim
     * - `183830` - aktuel etiket, duzeni farkli
     * - `184300` - etikette gramaj YOK, `12Lİ` adet carpani
     *
     * Once buraya 24 yazmistim, olcum 23 dedi. Sayiyi tahmin etmenin bedeli
     * tam olarak bu: yaniltici bir esik, esik olmamasindan kotu.
     *
     * **YALNIZCA BIM.** Fikstur seti sonradan Metro ve Migros partileriyle
     * genisledi ve bu okuyucu o zincirlerde CALISMIYOR (Metro'da 34 etikette 0
     * gramaj, Migros'ta 19'da 1). Sebep yapisal: iki zincirde de gramaj ayri bir
     * satir degil, ad satirinin sonunda. Olcum `docs/18-zincir-karsilastirmasi.md`
     * altinda; kapsami BIM'e daraltmak o bosluğu gizlemek DEGIL, nerede
     * durdugunu isaretlemek.
     */
    @Test
    fun measuredCoverage() {
        val bim = TagFixtures.of("BIM")
        val withName = bim.count { (_, ocr) -> readTagName(ocr) != null }
        val withPack = bim.count { (_, ocr) -> readTagPack(ocr) != null }
        assertEquals(27, bim.size, "BIM fikstur sayisi degisti")
        assertEquals(26, withName, "ad okunan etiket sayisi dustu")
        assertEquals(23, withPack, "gramaj okunan etiket sayisi dustu")
    }
}
