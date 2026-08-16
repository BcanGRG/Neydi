package com.neydi.app.data.store

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Zincir anahtari ve gorunen magaza adi.
 *
 * Vakalar gercek fis kunyelerinden (E4 ile fis testlerinden tasindi). Etiket
 * doneminde ayni fonksiyonlar magaza tohumunu ve "+ Yeni market"
 * tekillestirmesini besliyor - vokabuler degismedi, alias'lar gecerli.
 */
class StoreNameTest {

    /** Sube degil zincir: alias "bim"e baglanmali, subeye degil. */
    @Test
    fun chainKeyIgnoresBranch() {
        assertEquals("bim", chainKey("BIM BIRLESIK MAGAZALAR A.S."))
        assertEquals("bim", chainKey("BIM BADEMLIK SUBESI"))
        assertEquals("file", chainKey("FiLE MARKET MAĞAZACIL IK"))
        assertEquals("bilinmiyor", chainKey(null))
    }

    /** Ticari unvan degil zincir adi gosterilir (karar 13). */
    @Test
    fun displayNameTakesFirstMeaningfulToken() {
        assertEquals("AKYURT", storeDisplayName("AKYURT SÜPERMARKET GIDA İNS.SAN.VE TİC. A.Ş."))
        assertEquals("FiLE", storeDisplayName("FiLE MARKET MAGAZACILIK ANONIM SIRKETI"))
        assertEquals("BIM", storeDisplayName("BIM BIRLESIK MAGAZALAR A.S."))
    }

    /** Sondaki noktalama atiliyor: "MIGROS." zincir adi degil, cumle sonu. */
    @Test
    fun displayNameTrimsTrailingPunctuation() {
        assertEquals("MİGROS", storeDisplayName("MİGROS. TİCARET A.Ş."))
    }

    /** Tek harflik parcalar atlaniyor - "A.Ş." unvan kirintisi, zincir degil. */
    @Test
    fun displayNameSkipsSingleLetterTokens() {
        assertEquals("MARKET", storeDisplayName("A MARKET SAN. TİC."))
    }

    @Test
    fun displayNameReturnsNullWhenNothingReadable() {
        assertNull(storeDisplayName(null))
        assertNull(storeDisplayName("   "))
    }
}
