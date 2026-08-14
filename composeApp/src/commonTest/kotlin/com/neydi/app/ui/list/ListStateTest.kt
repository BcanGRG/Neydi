package com.neydi.app.ui.list

import com.neydi.app.data.db.ListRowProjection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListStateTest {

    private var sayac = 0

    private fun satir(
        ad: String,
        kategoriAdi: String = "Meyve-Sebze",
        kategoriSirasi: Int = 0,
        isaretli: Boolean = false,
        adet: Double = 1.0,
        birim: String = "adet",
        sabitMi: Boolean = false,
        ekleyen: String = "ben",
    ) = ListRowProjection(
        satirId = "s${++sayac}",
        urunId = "u$sayac",
        ad = ad,
        adet = adet,
        birim = birim,
        isaretli = isaretli,
        sabitMi = sabitMi,
        kategoriId = kategoriAdi.lowercase(),
        kategoriAdi = kategoriAdi,
        kategoriSirasi = kategoriSirasi,
        ekleyenUyeId = ekleyen,
        notu = null,
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
        val durum = listOf(
            satir("Domates"),
            satir("Elma", isaretli = true),
            satir("Ekmek", kategoriAdi = "Fırın-Ekmek", kategoriSirasi = 1),
        ).toSections(benimUyeId = "ben")

        assertEquals(1, durum.alinanlar.size)
        assertEquals("Elma", durum.alinanlar.single().row.name)
        assertTrue(durum.bolumler.none { b -> b.satirlar.any { it.row.name == "Elma" } })
    }

    /** Bos bolum CIZILMEZ - SectionHeader'in sozlesmesi. */
    @Test
    fun fullyCheckedSectionIsNeverCreated() {
        val durum = listOf(
            satir("Elma", isaretli = true),
            satir("Domates", isaretli = true),
        ).toSections(benimUyeId = "ben")

        assertTrue(durum.bolumler.isEmpty(), "bos bolum olusturuldu: ${durum.bolumler}")
        assertEquals(2, durum.alinanlar.size)
    }

    /** Girdi sirasi SQL'den geliyor; gruplama onu BOZMAMALI. */
    @Test
    fun aisleOrderIsPreserved() {
        val durum = listOf(
            satir("Domates", "Meyve-Sebze", 0),
            satir("Ekmek", "Fırın-Ekmek", 1),
            satir("Süt", "Süt-Kahvaltılık", 2),
            satir("Salatalık", "Meyve-Sebze", 0),
        ).toSections(benimUyeId = "ben")

        assertEquals(
            listOf("Meyve-Sebze", "Fırın-Ekmek", "Süt-Kahvaltılık"),
            durum.bolumler.map { it.baslik },
        )
        assertEquals(2, durum.bolumler.first().satirlar.size)
    }

    /** Avatar YALNIZCA es ekledigunde. Kendi ekledigimizde her satira gurultu. */
    @Test
    fun avatarOnlyDrawnWhenPartnerAdded() {
        val durum = listOf(
            satir("Domates", ekleyen = "ben"),
            satir("Ekmek", kategoriAdi = "Fırın-Ekmek", kategoriSirasi = 1, ekleyen = "es"),
        ).toSections(benimUyeId = "ben")

        assertNull(durum.bolumler[0].satirlar.single().row.addedByInitial)
        assertEquals("E", durum.bolumler[1].satirlar.single().row.addedByInitial)
    }

    /** Satir kimligi ListRow'da degil UiSatir'da; isaretleme ona dayaniyor. */
    @Test
    fun rowIdentityIsPreserved() {
        val kaynak = satir("Domates")
        val durum = listOf(kaynak).toSections(benimUyeId = "ben")
        assertEquals(kaynak.satirId, durum.bolumler.single().satirlar.single().id)
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
        val girdi = listOf(
            satir("Domates"),
            satir("Elma", isaretli = true),
            satir("Salatalik"),
        )

        val planlama = girdi.toSections("ben", alisverisModu = false)
        val alisveris = girdi.toSections("ben", alisverisModu = true)

        // Planlamada tasiniyor...
        assertEquals(1, planlama.alinanlar.size)
        assertEquals(2, planlama.bolumler.single().satirlar.size)

        // ...alisveriste tasinmiyor: uc satir da reyonda, SIRASI BOZULMADAN.
        assertTrue(alisveris.alinanlar.isEmpty(), "alisveris modunda satir Alindi'ya tasindi")
        assertEquals(
            listOf("Domates", "Elma", "Salatalik"),
            alisveris.bolumler.single().satirlar.map { it.row.name },
        )
    }

    /** Alt cubuktaki "kac kaldi" yalnizca isaretsizleri sayar. */
    @Test
    fun remainingCountsUncheckedRows() {
        val durum = listOf(
            satir("Domates"),
            satir("Elma", isaretli = true),
            satir("Salatalik"),
        ).toSections("ben", alisverisModu = true)

        assertEquals(3, durum.toplamSatir)
        assertEquals(2, durum.kalanSatir)
    }

    @Test
    fun emptyKindIsCarried() {
        val durum = emptyList<ListRowProjection>().toSections("ben", bosTur = EmptyKind.DONGU_ORTASI)
        assertEquals(EmptyKind.DONGU_ORTASI, durum.bosTur)
        assertTrue(durum.bosMu)
    }

    @Test
    fun emptyList() {
        val durum = emptyList<ListRowProjection>().toSections(benimUyeId = "ben")
        assertTrue(durum.bosMu)
        assertEquals(0, durum.toplamSatir)
    }
}
