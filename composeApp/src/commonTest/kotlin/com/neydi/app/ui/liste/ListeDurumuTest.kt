package com.neydi.app.ui.liste

import com.neydi.app.data.db.ListeSatiri
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListeDurumuTest {

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
    ) = ListeSatiri(
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
    fun tekAdetRozetUretmez() {
        assertNull(adetEtiketi(1.0, "adet"))
    }

    @Test
    fun adetEtiketleri() {
        assertEquals("2x", adetEtiketi(2.0, "adet"))
        assertEquals("1 kg", adetEtiketi(1.0, "kg"))
        assertEquals("500 g", adetEtiketi(500.0, "g"))
    }

    /** Turkce ondalik VIRGUL. Kotlin varsayilani nokta uretir. */
    @Test
    fun ondalikVirgulle() {
        assertEquals("1,5 kg", adetEtiketi(1.5, "kg"))
        assertEquals("0,5 L", adetEtiketi(0.5, "L"))
    }

    // --- Bolumleme ----------------------------------------------------------

    /**
     * ISARETLILER REYONDAN CIKAR. Reyon icinde kalsalardi liste alisveris
     * ilerledikce delik desik gorunur ve "daha ne kaldi" gozle cevaplanamazdi.
     */
    @Test
    fun isaretliSatirlarAlindiyaTasinir() {
        val durum = listOf(
            satir("Domates"),
            satir("Elma", isaretli = true),
            satir("Ekmek", kategoriAdi = "Fırın-Ekmek", kategoriSirasi = 1),
        ).bolumlere(benimUyeId = "ben")

        assertEquals(1, durum.alinanlar.size)
        assertEquals("Elma", durum.alinanlar.single().row.name)
        assertTrue(durum.bolumler.none { b -> b.satirlar.any { it.row.name == "Elma" } })
    }

    /** Bos bolum CIZILMEZ - SectionHeader'in sozlesmesi. */
    @Test
    fun tumSatirlariIsaretliBolumHicOlusmaz() {
        val durum = listOf(
            satir("Elma", isaretli = true),
            satir("Domates", isaretli = true),
        ).bolumlere(benimUyeId = "ben")

        assertTrue(durum.bolumler.isEmpty(), "bos bolum olusturuldu: ${durum.bolumler}")
        assertEquals(2, durum.alinanlar.size)
    }

    /** Girdi sirasi SQL'den geliyor; gruplama onu BOZMAMALI. */
    @Test
    fun reyonSirasiKorunur() {
        val durum = listOf(
            satir("Domates", "Meyve-Sebze", 0),
            satir("Ekmek", "Fırın-Ekmek", 1),
            satir("Süt", "Süt-Kahvaltılık", 2),
            satir("Salatalık", "Meyve-Sebze", 0),
        ).bolumlere(benimUyeId = "ben")

        assertEquals(
            listOf("Meyve-Sebze", "Fırın-Ekmek", "Süt-Kahvaltılık"),
            durum.bolumler.map { it.baslik },
        )
        assertEquals(2, durum.bolumler.first().satirlar.size)
    }

    /** Avatar YALNIZCA es ekledigunde. Kendi ekledigimizde her satira gurultu. */
    @Test
    fun avatarSadeceEsEkledigundeCizilir() {
        val durum = listOf(
            satir("Domates", ekleyen = "ben"),
            satir("Ekmek", kategoriAdi = "Fırın-Ekmek", kategoriSirasi = 1, ekleyen = "es"),
        ).bolumlere(benimUyeId = "ben")

        assertNull(durum.bolumler[0].satirlar.single().row.addedByInitial)
        assertEquals("E", durum.bolumler[1].satirlar.single().row.addedByInitial)
    }

    /** Satir kimligi ListRow'da degil UiSatir'da; isaretleme ona dayaniyor. */
    @Test
    fun satirKimligiKorunur() {
        val kaynak = satir("Domates")
        val durum = listOf(kaynak).bolumlere(benimUyeId = "ben")
        assertEquals(kaynak.satirId, durum.bolumler.single().satirlar.single().id)
    }

    @Test
    fun bosListe() {
        val durum = emptyList<ListeSatiri>().bolumlere(benimUyeId = "ben")
        assertTrue(durum.bosMu)
        assertEquals(0, durum.toplamSatir)
    }
}
