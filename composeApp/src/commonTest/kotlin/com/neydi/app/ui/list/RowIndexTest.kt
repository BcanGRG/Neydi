package com.neydi.app.ui.list

import com.neydi.app.ui.components.ListRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Eklenen satiri gorunur kilan kaydirmanin aritmetigi.
 *
 * NEDEN TEST GEREKLI: [rowIndexInList] `ListContent`'in `LazyColumn` icerik
 * sirasini AYNALIYOR ve ayna sessizce bozulur - dizin bir kayarsa uygulama
 * cokmez, yanlis satira kaydirir. Kullanicinin gorecegi sey "ekledigim sey
 * yine gorunmedi" olur, yani duzeltmeye calistigimiz sikayetin ta kendisi.
 *
 * Testin ISIRDIGI kanitlandi: bas kisimdaki sayaclardan biri bozulunca
 * asagidaki vakalar duser.
 */
class RowIndexTest {

    private fun row(id: String) = UiRow(
        id = id,
        productId = "p-$id",
        row = ListRow(name = id, quantity = null, checked = false, isStaple = false),
    )

    private fun state(
        sections: List<ListSection> = emptyList(),
        taken: List<UiRow> = emptyList(),
    ) = ListState(sections = sections, taken = taken, loading = false)

    /**
     * Dolu liste, pano cipi yok.
     * Bas kisim: baslik(0) + tahmin(1). Ilk reyon basligi 2, ilk satir 3.
     */
    @Test
    fun firstRowSitsAfterHeaderEstimateAndSectionTitle() {
        val s = state(listOf(ListSection("Fırın-Ekmek", listOf(row("a"), row("b")))))

        assertEquals(3, rowIndexInList(s, showsClipboardChip = false, rowId = "a"))
        assertEquals(4, rowIndexInList(s, showsClipboardChip = false, rowId = "b"))
    }

    /** Pano cipi bas kismi bir arttiriyor. */
    @Test
    fun clipboardChipShiftsEverythingByOne() {
        val s = state(listOf(ListSection("Fırın-Ekmek", listOf(row("a")))))

        assertEquals(3, rowIndexInList(s, showsClipboardChip = false, rowId = "a"))
        assertEquals(4, rowIndexInList(s, showsClipboardChip = true, rowId = "a"))
    }

    /**
     * IKINCI REYON: her reyon kendi basligini da sayiyor.
     *
     * En cok kayan yer burasi - baslik satirlarini saymayi unutan bir hesap
     * TEK reyonlu listede dogru calisir ve hata ancak ikinci reyonda gorunur.
     */
    @Test
    fun eachSectionCountsItsOwnTitle() {
        val s = state(
            listOf(
                ListSection("Fırın-Ekmek", listOf(row("a"), row("b"))),
                ListSection("Süt-Kahvaltılık", listOf(row("c"))),
            ),
        )

        // 0 baslik · 1 tahmin · 2 reyon1 · 3 a · 4 b · 5 reyon2 · 6 c
        assertEquals(6, rowIndexInList(s, showsClipboardChip = false, rowId = "c"))
    }

    /** "Alındı" bolumu kendi basligiyla en altta. */
    @Test
    fun takenSectionFollowsAllSections() {
        val s = state(
            sections = listOf(ListSection("Fırın-Ekmek", listOf(row("a")))),
            taken = listOf(row("t1"), row("t2")),
        )

        // 0 baslik · 1 tahmin · 2 reyon · 3 a · 4 "Alındı" · 5 t1 · 6 t2
        assertEquals(5, rowIndexInList(s, showsClipboardChip = false, rowId = "t1"))
        assertEquals(6, rowIndexInList(s, showsClipboardChip = false, rowId = "t2"))
    }

    /** Listede olmayan satir null - cagiran taraf kaydirmayi atliyor. */
    @Test
    fun unknownRowReturnsNull() {
        val s = state(listOf(ListSection("Fırın-Ekmek", listOf(row("a")))))

        assertNull(rowIndexInList(s, showsClipboardChip = false, rowId = "yok"))
    }

    /**
     * BOS LISTE: tahmin karti cizilmiyor, bos durum cizilyor - ikisi de bir
     * oge, yani bas kisim yine ayni boyda. Bu vaka o dengeyi kilitliyor.
     */
    @Test
    fun emptyListHasNoRowsButKeepsItsLeadingCount() {
        val s = state()

        assertNull(rowIndexInList(s, showsClipboardChip = false, rowId = "a"))
    }
    /**
     * ALISVERIS MODUNDA TAHMIN KARTI YOK - dizin bir GERI kayiyor.
     *
     * `ListContent` tahmini `!isEmpty && !shoppingMode` ile ciziyor, ayna ise
     * yalnizca `!isEmpty` sayiyordu. Reyonda sheet'ten eklenen her satir bir
     * fazla dizine kaydiriliyordu - yani kullanici ekledigi satiri degil bir
     * sonrakini goruyordu. Ekleme reyonda YALNIZCA sheet'ten yapilabiliyor
     * (kok alan gizli), yani bu tam da en cok gerektigi yerdeki kirikti.
     */
    @Test
    fun theEstimateCardIsAbsentInShoppingMode() {
        val sections = listOf(ListSection("Fırın-Ekmek", listOf(row("a"))))
        val plan = ListState(sections = sections, loading = false)
        val reyon = ListState(sections = sections, loading = false, shoppingMode = true)

        assertEquals(3, rowIndexInList(plan, showsClipboardChip = false, rowId = "a"))
        assertEquals(2, rowIndexInList(reyon, showsClipboardChip = false, rowId = "a"))
    }

    /**
     * OZET KARTI DA BIR OGE (karar 69) - ayna onu hic saymiyordu.
     *
     * Kart alisveris biter bitmez basligin altinda beliriyor ve listenin
     * icinde yasiyor. Sayilmayinca alisveris sonrasi eklenen her satir bir
     * eksik dizine kaydiriliyordu.
     */
    @Test
    fun theSummaryCardShiftsEverythingDown() {
        val s = state(listOf(ListSection("Fırın-Ekmek", listOf(row("a")))))

        assertEquals(3, rowIndexInList(s, showsClipboardChip = false, rowId = "a"))
        assertEquals(
            4,
            rowIndexInList(s, showsClipboardChip = false, rowId = "a", showsSummary = true),
        )
    }
}
