package com.neydi.app.ui.list

import com.neydi.app.data.db.ListRowProjection
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

    @Test
    fun emptyList() {
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
}
