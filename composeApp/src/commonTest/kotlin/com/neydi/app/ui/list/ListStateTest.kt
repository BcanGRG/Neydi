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
}
