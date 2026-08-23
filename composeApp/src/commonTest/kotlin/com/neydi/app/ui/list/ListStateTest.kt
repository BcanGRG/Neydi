package com.neydi.app.ui.list

import com.neydi.app.data.db.ListRowProjection
import com.neydi.app.data.repo.STAPLE_LIMIT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListStateTest {

    /** Sabit bir "simdi" - fiyat ipucunun yas hesabi testte saate bagli olmasin. */
    private val NOW = 1_000_000_000L


    private var counter = 0

    private fun row(
        name: String,
        categoryName: String = "Meyve-Sebze",
        categoryOrder: Int = 0,
        checked: Boolean = false,
        count: Double = 1.0,
        unit: String = "adet",
        isStaple: Boolean = false,
        addedBy: String = "ben",
        storeId: String? = null,
        storeName: String? = null,
    ) = ListRowProjection(
        rowId = "s${++counter}",
        productId = "u$counter",
        name = name,
        count = count,
        unit = unit,
        checked = checked,
        isStaple = isStaple,
        categoryId = categoryName.lowercase(),
        categoryName = categoryName,
        categoryOrder = categoryOrder,
        addedByMemberId = addedBy,
        takeOutcome = null,
        note = null,
        storeId = storeId,
        storeName = storeName,
    )

    // --- Rozetin dolgusu ----------------------------------------------------

    /** Varsayilan miktar: rozet KONTURLU - kimse dokunmamis. */
    @Test
    fun anUntouchedRowIsNotModified() {
        assertFalse(isQuantityModified(1.0, unitOverride = null))
    }

    /** Sayi degistiyse dolgulu - ilk ve en acik hal. */
    @Test
    fun aChangedCountIsModified() {
        assertTrue(isQuantityModified(4.0, unitOverride = null))
    }

    /**
     * BIRIM DEGISTIYSE SAYI 1 KALSA BILE DOLGULU (karar 108).
     *
     * "1 kg Domates"i "1 adet Domates" yapan biri sayiya hic dokunmuyor. Kural
     * yalnizca sayiya bakiyor olsaydi o satir KONTURLU cizilirdi - oysa
     * miktarini gercekten kullanici secmis olurdu.
     */
    @Test
    fun aUnitOverrideCountsAsModifiedEvenAtOne() {
        assertTrue(isQuantityModified(1.0, unitOverride = "adet"))
    }

    // --- Bolumleme ----------------------------------------------------------

    /**
     * PLANLAMADA "ALINDI" BOLUMU YOK (karar 116).
     *
     * ## Bu iddia tam tersine cevrildi
     *
     * Eskiden *"isaretliler reyondan cikar"* diyordu ve planlamada isaretli
     * satirlar `taken`a iniyordu. Kullanicinin sikayeti bunun ta kendisiydi:
     * *"liste yaparken neden alindi/alinmadi var ki? Zaten alisverise
     * cikiyorum dediginde isaretleme yapiyorum."*
     *
     * Ve bedeli olculdu: cihaz testlerinde YANLISLIKLA yapilan her dokunus bir
     * satiri "Alindi"ya tasidi - satirin tamami isaretleme hedefiydi, yani
     * ekranin en buyuk hedefi en az istenen ise bagliydi.
     */
    @Test
    fun planningHasNoTakenSection() {
        val state = listOf(
            row("Domates"),
            row("Elma", checked = true),
            row("Ekmek", categoryName = "Fırın-Ekmek", categoryOrder = 1),
        ).toSections(myMemberId = "ben", now = NOW)

        assertTrue(state.taken.isEmpty(), "planlamada Alindi bolumu olusturuldu")
        // Uc satirin ucu de reyonunda - isaretli olan dahil.
        assertEquals(3, state.sections.sumOf { it.rows.size })
    }

    /**
     * BOS BOLUM CIZILMEZ - `SectionHeader`in sozlesmesi.
     *
     * Bu testin ESKI vakasi (butun satirlari isaretli bir bolum) artik
     * imkansiz: hicbir satir bolumden cikmiyor. Sozlesme yine de kodda duruyor
     * (`filter { it.rows.isNotEmpty() }`) ve nobetcisiz birakilmamali - bos
     * girdi hala bos bolum uretmemeli.
     */
    @Test
    fun anEmptySectionIsNeverCreated() {
        val state = emptyList<ListRowProjection>().toSections(myMemberId = "ben", now = NOW)
        assertTrue(state.sections.isEmpty(), "bos bolum olusturuldu: ${state.sections}")
    }

    /** Girdi sirasi SQL'den geliyor; gruplama onu BOZMAMALI. */
    @Test
    fun aisleOrderIsPreserved() {
        val state = listOf(
            row("Domates", "Meyve-Sebze", 0),
            row("Ekmek", "Fırın-Ekmek", 1),
            row("Süt", "Süt-Kahvaltılık", 2),
            row("Salatalık", "Meyve-Sebze", 0),
        ).toSections(myMemberId = "ben", now = NOW)

        assertEquals(
            listOf("Meyve-Sebze", "Fırın-Ekmek", "Süt-Kahvaltılık"),
            state.sections.map { it.title },
        )
        assertEquals(2, state.sections.first().rows.size)
    }

    /** Avatar YALNIZCA es ekledigunde. Kendi ekledigimizde her satira gurultu. */
    @Test
    fun avatarOnlyDrawnWhenPartnerAdded() {
        val state = listOf(
            row("Domates", addedBy = "ben"),
            row("Ekmek", categoryName = "Fırın-Ekmek", categoryOrder = 1, addedBy = "es"),
        ).toSections(myMemberId = "ben", now = NOW)

        assertNull(state.sections[0].rows.single().row.addedByInitial)
        assertEquals("E", state.sections[1].rows.single().row.addedByInitial)
    }

    /** Satir kimligi ListRow'da degil UiSatir'da; isaretleme ona dayaniyor. */
    @Test
    fun rowIdentityIsPreserved() {
        val source = row("Domates")
        val state = listOf(source).toSections(myMemberId = "ben", now = NOW)
        assertEquals(source.rowId, state.sections.single().rows.single().id)
    }

    // --- Alisveris modu -----------------------------------------------------

    /**
     * ISARETLI SATIR HER IKI MODDA DA YERINDE KALIYOR.
     *
     * Reyonda gerekcesi baştan beri ayni: hareket eden basparmagin altinda
     * yeniden siralama bu ekranin yapabilecegi en kotu hata - kullanici bir
     * sonrakine dokunacakken liste kayar ve yanlis urunu isaretler.
     *
     * Planlamada gerekce yeni (karar 116): orada zaten isaretlenecek bir sey
     * yok, dolayisiyla tasinacak bir satir da yok.
     */
    @Test
    fun aCheckedRowStaysInPlaceInBothModes() {
        val input = listOf(
            row("Domates"),
            row("Elma", checked = true),
            row("Salatalik"),
        )

        listOf(false, true).forEach { shopping ->
            val state = input.toSections("ben", shoppingMode = shopping, now = NOW)
            assertTrue(state.taken.isEmpty(), "shoppingMode=$shopping: satir Alindi'ya tasindi")
            assertEquals(
                listOf("Domates", "Elma", "Salatalik"),
                state.sections.single().rows.map { it.row.name },
                "shoppingMode=$shopping: reyon sirasi bozuldu",
            )
        }
    }

    /** Alt cubuktaki "kac kaldi" yalnizca isaretsizleri sayar. */
    @Test
    fun remainingCountsUncheckedRows() {
        val state = listOf(
            row("Domates"),
            row("Elma", checked = true),
            row("Salatalik"),
        ).toSections("ben", shoppingMode = true, now = NOW)

        assertEquals(3, state.totalRows)
        assertEquals(2, state.remainingRow)
    }

    @Test
    fun emptyKindIsCarried() {
        val state = emptyList<ListRowProjection>().toSections("ben", emptyKind = EmptyKind.DONGU_ORTASI, now = NOW)
        assertEquals(EmptyKind.DONGU_ORTASI, state.emptyKind)
        assertTrue(state.isEmpty)
    }

    /**
     * BOS GIRDI BOS DURUM URETIYOR.
     *
     * ⚠ ADI ESKIDEN `emptyList` IDI ve bu test degil, dosyanin tamami icin
     * bir tuzakti: ayni dosyadaki her `emptyList()` cagrisi tip argumani
     * verilmedigi anda BU FONKSIYONA baglaniyordu ve hata "beklenen
     * List<String>, gelen Unit" diye cikiyordu - sebebi hicbir yerde
     * gorunmeden.
     */
    @Test
    fun anEmptyInputProducesTheEmptyState() {
        val state = emptyList<ListRowProjection>().toSections(myMemberId = "ben", now = NOW)
        assertTrue(state.isEmpty)
        assertEquals(0, state.totalRows)
    }

    // --- "Her zamankiler" bolumu (F6.8) ------------------------------------

    /**
     * Sabitler KENDI BOLUMUNDE ve EN USTTE - tasarim maketlerindeki gibi.
     *
     * Reyon adi tasimiyorlar cunku bir sabit hangi reyondan olursa olsun bu
     * bolumde toplaniyor; kategori gruplamasinin disinda.
     */
    @Test
    fun staplesGetTheirOwnSectionAtTheTop() {
        val state = listOf(
            row("Domates", "Meyve-Sebze"),
            row("Ekmek", "Fırın-Ekmek", isStaple = true),
            row("Süt", "Süt-Kahvaltılık", isStaple = true),
        ).toSections(myMemberId = "ben", now = NOW)

        assertEquals(STAPLE_SECTION_TITLE, state.sections.first().title)
        assertEquals(listOf("Ekmek", "Süt"), state.sections.first().rows.map { it.row.name })
        // Ve kategori bolumlerinde TEKRAR gorunmuyorlar.
        val digerleri = state.sections.drop(1).flatMap { it.rows }.map { it.row.name }
        assertEquals(listOf("Domates"), digerleri)
    }

    /**
     * ALISVERIS MODUNDA BOLUM YOK - tasarim maketinde de yok.
     *
     * Reyonda sira DONUYOR ve sabit bir urun de sonucta bir reyondan alinacak;
     * onu listenin basina cekmek market yuruyusunu bozardi.
     */
    @Test
    fun noStapleSectionInShoppingMode() {
        val state = listOf(
            row("Domates", "Meyve-Sebze"),
            row("Ekmek", "Fırın-Ekmek", isStaple = true),
        ).toSections(myMemberId = "ben", shoppingMode = true, now = NOW)

        assertTrue(state.sections.none { it.title == STAPLE_SECTION_TITLE })
        assertEquals(2, state.sections.sumOf { it.rows.size })
        // Sabit kendi reyonunda duruyor.
        assertEquals(
            listOf("Fırın-Ekmek"),
            state.sections.filter { b -> b.rows.any { it.row.name == "Ekmek" } }.map { it.title },
        )
    }

    /** Hic sabit yoksa bolum HIC cizilmiyor - bos bolum yasak. */
    @Test
    fun noStapleSectionWhenThereAreNone() {
        val state = listOf(row("Domates")).toSections(myMemberId = "ben", now = NOW)

        assertTrue(state.sections.none { it.title == STAPLE_SECTION_TITLE })
        assertEquals(1, state.sections.size)
    }

    /** Bolum en fazla 12 satir - tasarimin siniri. */
    @Test
    fun stapleSectionIsCappedAtTwelve() {
        val state = (1..15).map { row("Sabit $it", isStaple = true) }
            .toSections(myMemberId = "ben", now = NOW)

        assertEquals(12, state.sections.first { it.title == STAPLE_SECTION_TITLE }.rows.size)
    }

    /**
     * ISARETLENEN SABIT DE BOLUMUNDE KALIYOR (karar 116).
     *
     * Eskiden "Alindi"ya iniyordu. Sabitler bolumu planlamanin bolumu ve
     * planlamada artik isaretlenecek bir sey yok; bir satir gecmis bir
     * alisveristen isaretli gelse bile yerinden oynamiyor.
     */
    @Test
    fun aCheckedStapleStaysInItsSection() {
        val state = listOf(
            row("Ekmek", isStaple = true, checked = true),
            row("Süt", isStaple = true),
        ).toSections(myMemberId = "ben", now = NOW)

        assertEquals(listOf("Ekmek", "Süt"), state.sections.first().rows.map { it.row.name })
        assertTrue(state.taken.isEmpty())
    }

    // --- Baslik alt satiri (Ekran 1 tasarimi) --------------------------------

    /** 15 Agu 2026 12:00 - gunler bu ana gore sayiliyor. */
    private val now = 1786_000_000_000L

    private fun daysAgo(days: Int) = now - days * 24L * 60 * 60 * 1000

    /**
     * TASARIM SAYI SAYMIYOR, HATIRLATIYOR.
     *
     * Onceki hal "N urun" idi ve ekranin kendisi zaten o satirlari
     * gosteriyordu; baslik hicbir sey eklemiyordu.
     */
    @Test
    fun headerRemindsOfLastTrip() {
        // TUTAR TAHMIN BICIMINDE (F5.11): tilde bitisik, kurus yok. Onceki
        // hali "642,00 TL" idi - uygulamada artik kesin tutar diye bir veri
        // olmadigi icin o bicim bir iddia tasiyordu.
        //
        // SEKIZ GUN "geçen hafta" YAZIYOR, "8 gün önce" DEGIL: tarih merdiveni
        // 7-13 gun araligini tek cumleye topluyor. Tasarimin Ekran 1 ornegi
        // hala "8 gün önce" gosteriyor - celiski tasarima soruldu; merdiven
        // daha yeni ve daha acik oldugu icin o esas alindi.
        assertEquals(
            "Son alışveriş: geçen hafta · ~642 TL",
            lastTripSummary(LastTrip(closedAt = daysAgo(8), totalMinor = 64200), now),
        )
        assertEquals(
            "Son alışveriş: 3 gün önce · ~642 TL",
            lastTripSummary(LastTrip(closedAt = daysAgo(3), totalMinor = 64200), now),
        )
    }

    /**
     * MERDIVENIN ILK BASAMAGI SAATLE OLCULUYOR (F5.11).
     *
     * Bu test once "bugün" bekliyordu; tasarimin tarih merdiveni 0-6 saat
     * araligina "az önce" diyor ve `daysAgo(0)` tam olarak `now` demek.
     * "bugün" ancak alti saati gecmis ayni gun icin yaziliyor ve o zaman
     * saati de tasiyor ("bugün 08:05").
     */
    @Test
    fun headerUsesWordsForRecentTrips() {
        assertEquals(
            "Son alışveriş: az önce · ~13 TL",
            lastTripSummary(LastTrip(closedAt = daysAgo(0), totalMinor = 1250), now),
        )
        assertEquals(
            "Son alışveriş: dün · ~13 TL",
            lastTripSummary(LastTrip(closedAt = daysAgo(1), totalMinor = 1250), now),
        )
    }

    /**
     * TUTAR OKUNAMADIYSA HIC YAZILMIYOR.
     *
     * "0 TL" ya da "- TL" yazmak dogrulanmamis bir sayiyi manset yapmak olurdu;
     * F4.11 ayni karari `Trip.totalMinor` icin vermisti.
     */
    @Test
    fun headerOmitsAmountWhenUnread() {
        assertEquals(
            "Son alışveriş: 3 gün önce",
            lastTripSummary(LastTrip(closedAt = daysAgo(3), totalMinor = null), now),
        )
    }

    /** Hic alisveris yoksa sayi degil, durum yaziliyor. */
    @Test
    fun headerSaysNothingBoughtYet() {
        assertEquals("Henüz alışveriş yok", lastTripSummary(null, now))
    }

    /**
     * Cihaz saati geri alinmis: "-2 gün önce" yazmak yerine merdivenin en
     * yakin dogru basamagina dusuluyor.
     */
    @Test
    fun headerClampsFutureTripToJustNow() {
        assertEquals(
            "Son alışveriş: az önce",
            lastTripSummary(LastTrip(closedAt = daysAgo(-2), totalMinor = null), now),
        )
    }

    // --- BEYAN CUMLESI (karar 117) -------------------------------------------

    /**
     * HEDEF YOKSA BEYAN DA YOK - baslik "Son alisveris"e geri duser.
     *
     * "Belli degil" bir HAL, bos bir dizgi degil: kullanici hedefi
     * kaldirabiliyor ve o zaman satirlarin markete dair hicbir iddiasi
     * kalmiyor. `null` dondurmek cagirani da bunu dusunmeye zorluyor -
     * bos dizgi donseydi baslik bos bir satir cizerdi.
     */
    @Test
    fun withoutATargetThereIsNoDeclaration() {
        assertNull(storeDeclaration(targetName = null, deviantStoreNames = emptyList()))
        assertNull(storeDeclaration(targetName = "  ", deviantStoreNames = listOf("A101")))
    }

    /**
     * HEDEF VAR, SAPMA YOK: cumle tek parca ve ayirici cizilmiyor.
     *
     * `deviation`in `null` olmasi cizim tarafinin sozlesmesi - "·" ayiricisi
     * o alan doluyken ciziliyor. Bos dizgi donseydi baslikta ortada asili
     * bir nokta kalirdi.
     */
    @Test
    fun aTargetWithNoExceptionsIsASingleClause() {
        val d = storeDeclaration("BİM", emptyList())
        assertEquals("BİM'e gidiyorsun", d?.target)
        assertNull(d?.deviation)
    }

    /**
     * TEK ZINCIRE SAPMA: maketin birebir cumlesi.
     *
     * *"BIM'e gidiyorsun · 2'si A101'de"* - hem yonelme (`BIM'e`) hem iyelik
     * (`2'si`) hem bulunma (`A101'de`) ayni cumlede, yani uc ek kuralinin
     * hepsi burada bulusuyor.
     */
    @Test
    fun deviationsToOneChainNameThatChain() {
        val d = storeDeclaration("BİM", listOf("A101", "A101"))
        assertEquals("BİM'e gidiyorsun", d?.target)
        assertEquals("2'si A101'de", d?.deviation)
    }

    /**
     * IKI ZINCIRE SAPMA: ZINCIR ADI DUSUYOR, SAYI DUSMUYOR.
     *
     * ⚠ Makette bu hal yok; kod karari (bkz. `storeDeclaration` KDoc'u).
     * Onemli olan sayinin DOGRU kalmasi: en buyuk zinciri yazip otekileri
     * dusurmek "3'u A101'de" derdi ve kullanici A101'de iki satir bulurdu.
     *
     * Testte A101 iki, SOK bir satir: ayni girdi "en buyugu yaz" kuralini da
     * calistirirdi, yani bu test iki davranisi birbirinden AYIRIYOR.
     */
    @Test
    fun deviationsToSeveralChainsKeepTheCountAndDropTheName() {
        val d = storeDeclaration("BİM", listOf("A101", "A101", "ŞOK"))
        assertEquals("3'ü başka marketlerde", d?.deviation)
    }

    /**
     * SAYININ EKI OKUNUSTAN: `1'i`, `3'ü`, `6'sı`.
     *
     * Tek bir ek ("i") secmek uc vakanin ikisini bozardi ve beyan cumlesi
     * her acilista goruluyor - yanlis ek her gun okunur.
     */
    @Test
    fun theDeviationCountCarriesItsOwnSuffix() {
        assertEquals("1'i A101'de", storeDeclaration("BİM", listOf("A101"))?.deviation)
        assertEquals(
            "3'ü A101'de",
            storeDeclaration("BİM", listOf("A101", "A101", "A101"))?.deviation,
        )
        assertEquals(
            "6'sı A101'de",
            storeDeclaration("BİM", listOf("A101", "A101", "A101", "A101", "A101", "A101"))?.deviation,
        )
    }

    // --- SAPMA ISARETI VE MARKET BOLUMU (karar 118) --------------------------

    /**
     * HEDEFI IZLEYEN SATIRDA ISARET YOK.
     *
     * Uc hal ayni sonucu vermeli ve ucu de farkli sebeple: satirin marketi
     * hic yazilmamis (`null`), hedefin KENDISI yazilmis (tekrar), ya da hedef
     * hic yok. Ilk ikisini ayirmak onemli - "BIM'e gidiyorum" derken bir
     * satira da "BIM" demek sapma degildir ve isaret cizmek kullaniciya
     * yapmadigi bir beyani gosterirdi.
     */
    @Test
    fun aRowThatFollowsTheTargetCarriesNoMark() {
        val rows = listOf(
            row("Domates"),
            row("Elma", storeId = "bim", storeName = "BİM"),
        )
        val state = rows.toSections("ben", now = NOW, targetStoreId = "bim", targetStoreName = "BİM")
        assertTrue(state.sections.flatMap { it.rows }.all { it.row.deviantStore == null })

        // Hedef yokken de isaret yok - kiyaslanacak bir sey yok.
        val noTarget = listOf(row("Elma", storeId = "bim", storeName = "BİM"))
            .toSections("ben", now = NOW)
        assertNull(noTarget.sections.single().rows.single().row.deviantStore)
    }

    /**
     * SAPAN SATIR ZINCIRIN ADINI TASIYOR.
     *
     * Ad KIMLIK karsilastirmasindan sonra veriliyor: `storeId` farkli, ad
     * ciziliyor. Adla karsilastirsaydik ayni adi tasiyan iki zincirde
     * satir sessizce "hedefi izliyor" gorunurdu.
     */
    @Test
    fun aDeviatingRowNamesItsChain() {
        val state = listOf(row("Elma", storeId = "a101", storeName = "A101"))
            .toSections("ben", now = NOW, targetStoreId = "bim", targetStoreName = "BİM")

        assertEquals("A101", state.sections.single().rows.single().row.deviantStore)
    }

    /**
     * PLANLAMADA SAPANLAR REYONUNDA KALIYOR, ALISVERISTE CIKIYOR.
     *
     * Iki modun isi farkli: planlamada liste kuruluyor ve reyon hala en
     * yararli gruplama; reyonda ise liste "bu markette hangi sirayla
     * yuruyeceksin" diyor ve baska bir marketten alinacak satir o yuruyusun
     * icinde durursa her reyonda aranir, bulunamaz.
     */
    @Test
    fun deviantsLeaveTheirAisleOnlyWhileShopping() {
        val rows = listOf(
            row("Domates"),
            row("Elma", storeId = "a101", storeName = "A101"),
        )

        val planning = rows.toSections("ben", shoppingMode = false, now = NOW, targetStoreId = "bim")
        assertEquals(1, planning.sections.size)
        assertTrue(planning.sections.none { it.isStore })

        val shopping = rows.toSections("ben", shoppingMode = true, now = NOW, targetStoreId = "bim")
        assertEquals(listOf(false, true), shopping.sections.map { it.isStore })
        assertEquals("A101'de", shopping.sections.last().title)
        assertEquals(listOf("Elma"), shopping.sections.last().rows.map { it.row.name })
    }

    /**
     * ZINCIR BASINA BIR BOLUM, hepsi tek bolumde DEGIL.
     *
     * Baslik zincirin adini yaziyor ("A101'de"); iki zinciri tek baslik
     * altinda toplamak o adi yalan yapardi. Siralama ADA gore - eklenme
     * sirasi listeyi her yeni satirda yeniden dizerdi.
     */
    @Test
    fun eachDeviantChainGetsItsOwnSection() {
        val state = listOf(
            row("Elma", storeId = "sok", storeName = "ŞOK"),
            row("Armut", storeId = "a101", storeName = "A101"),
            row("Erik", storeId = "a101", storeName = "A101"),
        ).toSections("ben", shoppingMode = true, now = NOW, targetStoreId = "bim")

        val stores = state.sections.filter { it.isStore }
        assertEquals(listOf("A101'de", "ŞOK'ta"), stores.map { it.title })
        assertEquals(listOf(2, 1), stores.map { it.rows.size })
    }

    /**
     * ADSIZ MARKET SAPMA SAYILMIYOR.
     *
     * Market silinmisse `storeName` null gelir. Satiri kendi reyonundan
     * cikarip basligi olmayan bir bolume koymak, kullanicinin bulamayacagi
     * bir yere koymak olurdu; beyan cumlesi de onu "1'i ?'de" diye yazamaz.
     */
    @Test
    fun aDeviationToADeletedStoreIsIgnored() {
        val state = listOf(row("Elma", storeId = "silinmis", storeName = null))
            .toSections("ben", shoppingMode = true, now = NOW, targetStoreId = "bim", targetStoreName = "BİM")

        assertTrue(state.sections.none { it.isStore })
        assertNull(state.declaration?.deviation)
    }

    /**
     * BEYAN CIZILMEYEN SATIRLARI DA SAYIYOR.
     *
     * Sabitler bolumu 12 satirla sinirli ve ustu CIZILMIYOR. Cizilmeyen bir
     * satir da hedefinin disinda ve kullanici onu markette arayacak - cumle
     * onu saymak zorunda. Bolumler uzerinden saysaydik on ucuncu sabit
     * cumleden dusrdu.
     */
    @Test
    fun theDeclarationCountsRowsThatAreNotDrawn() {
        val rows = (1..13).map {
            row("Sabit$it", isStaple = true, storeId = "a101", storeName = "A101")
        }
        val state = rows.toSections("ben", now = NOW, targetStoreId = "bim", targetStoreName = "BİM")

        // On iki satir ciziliyor...
        assertEquals(STAPLE_LIMIT, state.sections.single().rows.size)
        // ...ama cumle on ucunu de sayiyor.
        assertEquals("13'ü A101'de", state.declaration?.deviation)
    }
}
