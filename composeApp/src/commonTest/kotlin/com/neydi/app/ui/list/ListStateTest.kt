package com.neydi.app.ui.list

import com.neydi.app.data.db.ListRowProjection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListStateTest {

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

    // --- Adet etiketi -------------------------------------------------------

    /** Adet 1 ve birim "adet" ise rozet CIZILMEZ - her satira "1x" yazmak gurultu. */
    @Test
    fun singleUnitProducesNoBadge() {
        assertNull(quantityLabel(1.0, "adet"))
    }

    @Test
    fun quantityLabels() {
        assertEquals("2x", quantityLabel(2.0, "adet"))
        assertEquals("1 kg", quantityLabel(1.0, "kg"))
        assertEquals("500 g", quantityLabel(500.0, "g"))
    }

    /**
     * TARTILI URUNDE BIRIM GORUNMELI: "0,5" tek basina anlamsiz.
     *
     * Ondalik VIRGULE ceviriliyor (Turkce yazim) ve uc hane korunuyor -
     * tartida gercekten uc hane cikiyor.
     *
     * BU IDDIALAR SILINEN `quantityBadge`'DEN TASINDI (F11.20): o fonksiyon
     * `ui/finish/` icinde yasiyordu ve bunun neredeyse birebir kopyasiydi.
     * Ekran silinirken iddialari birlikte gitmesin diye buraya alindi -
     * silinen tek sey `"ad"` kisaltmasi dali, cunku `normalizeUnit` onu
     * sinirda zaten `"adet"`e ceviriyor; o dal savunma amacli kopyaydi.
     */
    @Test
    fun weighedItemsShowTheirUnit() {
        assertEquals("0,5 kg", quantityLabel(0.5, "kg"))
        assertEquals("0,182 kg", quantityLabel(0.182, "kg"))
        assertEquals("1 L", quantityLabel(1.0, "L"))
    }

    /** Turkce ondalik VIRGUL. Kotlin varsayilani nokta uretir. */
    @Test
    fun decimalComma() {
        assertEquals("1,5 kg", quantityLabel(1.5, "kg"))
        assertEquals("0,5 L", quantityLabel(0.5, "L"))
    }

    // --- Bolumleme ----------------------------------------------------------

    /**
     * ISARETLILER REYONDAN CIKAR. Reyon icinde kalsalardi liste alisveris
     * ilerledikce delik desik gorunur ve "daha ne kaldi" gozle cevaplanamazdi.
     */
    @Test
    fun checkedRowsMoveToTaken() {
        val state = listOf(
            row("Domates"),
            row("Elma", checked = true),
            row("Ekmek", categoryName = "Fırın-Ekmek", categoryOrder = 1),
        ).toSections(myMemberId = "ben")

        assertEquals(1, state.taken.size)
        assertEquals("Elma", state.taken.single().row.name)
        // Alinanlar tarafi yukarida pinli ama bolumler tarafi pinsizdi: iki
        // isaretsiz satir tamamen kaybolsa `none {}` bos bolumler uzerinde
        // yine true donerdi.
        assertEquals(2, state.sections.sumOf { it.rows.size })
        assertTrue(state.sections.none { b -> b.rows.any { it.row.name == "Elma" } })
    }

    /** Bos bolum CIZILMEZ - SectionHeader'in sozlesmesi. */
    @Test
    fun fullyCheckedSectionIsNeverCreated() {
        val state = listOf(
            row("Elma", checked = true),
            row("Domates", checked = true),
        ).toSections(myMemberId = "ben")

        assertTrue(state.sections.isEmpty(), "bos bolum olusturuldu: ${state.sections}")
        assertEquals(2, state.taken.size)
    }

    /** Girdi sirasi SQL'den geliyor; gruplama onu BOZMAMALI. */
    @Test
    fun aisleOrderIsPreserved() {
        val state = listOf(
            row("Domates", "Meyve-Sebze", 0),
            row("Ekmek", "Fırın-Ekmek", 1),
            row("Süt", "Süt-Kahvaltılık", 2),
            row("Salatalık", "Meyve-Sebze", 0),
        ).toSections(myMemberId = "ben")

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
        ).toSections(myMemberId = "ben")

        assertNull(state.sections[0].rows.single().row.addedByInitial)
        assertEquals("E", state.sections[1].rows.single().row.addedByInitial)
    }

    /** Satir kimligi ListRow'da degil UiSatir'da; isaretleme ona dayaniyor. */
    @Test
    fun rowIdentityIsPreserved() {
        val source = row("Domates")
        val state = listOf(source).toSections(myMemberId = "ben")
        assertEquals(source.rowId, state.sections.single().rows.single().id)
    }

    // --- Alisveris modu -----------------------------------------------------

    /**
     * REYON SIRASI DONAR. Isaretlenen satir YERINDE kalir, "Alindi"ya inmez.
     * Hareket eden basparmagin altinda yeniden siralama bu ekranin
     * yapabilecegi en kotu hata: kullanici bir sonrakine dokunacakken liste
     * kayar ve yanlis urunu isaretler.
     */
    @Test
    fun checkedRowStaysInPlaceInShoppingMode() {
        val input = listOf(
            row("Domates"),
            row("Elma", checked = true),
            row("Salatalik"),
        )

        val planning = input.toSections("ben", shoppingMode = false)
        val trip = input.toSections("ben", shoppingMode = true)

        // Planlamada tasiniyor...
        assertEquals(1, planning.taken.size)
        assertEquals(2, planning.sections.single().rows.size)

        // ...alisveriste tasinmiyor: uc satir da reyonda, SIRASI BOZULMADAN.
        assertTrue(trip.taken.isEmpty(), "alisveris modunda satir Alindi'ya tasindi")
        assertEquals(
            listOf("Domates", "Elma", "Salatalik"),
            trip.sections.single().rows.map { it.row.name },
        )
    }

    /** Alt cubuktaki "kac kaldi" yalnizca isaretsizleri sayar. */
    @Test
    fun remainingCountsUncheckedRows() {
        val state = listOf(
            row("Domates"),
            row("Elma", checked = true),
            row("Salatalik"),
        ).toSections("ben", shoppingMode = true)

        assertEquals(3, state.totalRows)
        assertEquals(2, state.remainingRow)
    }

    @Test
    fun emptyKindIsCarried() {
        val state = emptyList<ListRowProjection>().toSections("ben", emptyKind = EmptyKind.DONGU_ORTASI)
        assertEquals(EmptyKind.DONGU_ORTASI, state.emptyKind)
        assertTrue(state.isEmpty)
    }

    @Test
    fun emptyList() {
        val state = emptyList<ListRowProjection>().toSections(myMemberId = "ben")
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
        ).toSections(myMemberId = "ben")

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
        ).toSections(myMemberId = "ben", shoppingMode = true)

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
        val state = listOf(row("Domates")).toSections(myMemberId = "ben")

        assertTrue(state.sections.none { it.title == STAPLE_SECTION_TITLE })
        assertEquals(1, state.sections.size)
    }

    /** Bolum en fazla 12 satir - tasarimin siniri. */
    @Test
    fun stapleSectionIsCappedAtTwelve() {
        val state = (1..15).map { row("Sabit $it", isStaple = true) }
            .toSections(myMemberId = "ben")

        assertEquals(12, state.sections.first { it.title == STAPLE_SECTION_TITLE }.rows.size)
    }

    /** Isaretlenen sabit "Alindi"ya iniyor, bolumde kalmiyor. */
    @Test
    fun checkedStapleMovesToTaken() {
        val state = listOf(
            row("Ekmek", isStaple = true, checked = true),
            row("Süt", isStaple = true),
        ).toSections(myMemberId = "ben")

        assertEquals(listOf("Süt"), state.sections.first().rows.map { it.row.name })
        assertEquals(listOf("Ekmek"), state.taken.map { it.row.name })
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
