package com.neydi.app.data.db

import androidx.room3.Room
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * F2.3 kisitlarinin GERCEKTEN tetiklendigini kanitlar.
 *
 * Neden DAO uzerinden degil HAM SQL: test edilen sey SEMA kisiti. DAO
 * `OnConflictStrategy.REPLACE` ile yazsaydi ikinci satir sessizce ustune biner
 * ve kisit hic denenmemis olurdu - test yesil kalir, koruma olmaz. Ham INSERT
 * kisitin kendisini zorluyor.
 */
class ConstraintTest {

    private fun db() = Room.inMemoryDatabaseBuilder<NeydiDatabase>(
        factory = { NeydiDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver()).build()

    private suspend fun NeydiDatabase.calistir(sql: String) =
        useWriterConnection { it.usePrepared(sql) { st -> st.step() } }

    private suspend fun NeydiDatabase.sayi(sql: String): Long =
        useWriterConnection { it.usePrepared(sql) { st -> st.step(); st.getLong(0) } }

    // --- TripLine: UNIQUE(tripId, productId) --------------------------------

    private fun tripLine(id: String, tripId: String, productId: String) = """
        INSERT INTO trip_line
          (id, householdId, tripId, productId, quantity, unit, checked, checkedAt,
           addedByMemberId, fromSuggestion, note, createdAt, deletedAt)
        VALUES
          ('$id', 'h1', '$tripId', '$productId', 1.0, 'adet', 0, NULL,
           'm1', 0, NULL, 0, NULL)
    """.trimIndent()

    /**
     * Senaryonun kendisi: iki es de ayni gezide ekmek ekliyor.
     *
     * Ikinci satir GECMEMELI. Gecerse ProductStats iki ayri satin alma sayar,
     * medianIntervalDays yariya duser ve uygulama ekmegi iki kat sik onermeye
     * baslar - hata vermeden, sadece rahatsiz ederek.
     */
    @Test
    fun sameProductCannotBeAddedTwicePerTrip() = runTest {
        val db = db()
        db.calistir(tripLine("tl1", "trip1", "ekmek"))

        val hata = runCatching { db.calistir(tripLine("tl2", "trip1", "ekmek")) }.exceptionOrNull()
            ?: fail("Ikinci satir kabul edildi - UNIQUE(tripId, productId) tetiklenmedi")

        // Istisnanin MESAJINA bakmiyoruz: bundled surucu bu yolda mesajsiz bir
        // SQLException atiyor ve metne dayanan bir iddia surucu detayina bagimli
        // olurdu. Onemli olan davranis - yazma reddedildi ve tabloda tek satir kaldi.
        assertTrue(
            hata::class.simpleName?.contains("SQL") == true,
            "SQL kaynakli bir hata bekleniyordu, gelen: ${hata::class.simpleName}: ${hata.message}",
        )
        assertEquals(1L, db.sayi("SELECT COUNT(*) FROM trip_line"))
    }

    /** Kisit gezi basina - AYNI urun BASKA gezide serbest olmali. */
    @Test
    fun sameProductAllowedOnAnotherTrip() = runTest {
        val db = db()
        db.calistir(tripLine("tl1", "trip1", "ekmek"))
        db.calistir(tripLine("tl2", "trip2", "ekmek"))
        assertEquals(2L, db.sayi("SELECT COUNT(*) FROM trip_line"))
    }

    // --- ProductAlias: UNIQUE(householdId, storeChain, rawTextNormalized) ---

    private fun alias(id: String, chain: String, raw: String, productId: String) = """
        INSERT INTO product_alias
          (id, householdId, storeChain, rawTextNormalized, productId, confirmedAt,
           createdAt, deletedAt)
        VALUES
          ('$id', 'h1', '$chain', '$raw', '$productId', NULL, 0, NULL)
    """.trimIndent()

    @Test
    fun sameRawTextCannotMapToTwoProductsInOneChain() = runTest {
        val db = db()
        db.calistir(alias("a1", "A101", "t.bugday ekmek 500g", "ekmek"))

        runCatching { db.calistir(alias("a2", "A101", "t.bugday ekmek 500g", "beyazekmek")) }
            .exceptionOrNull()
            ?: fail("Ikinci esleme kabul edildi - fiyat gecmisi iki urune bolunurdu")

        assertEquals(1L, db.sayi("SELECT COUNT(*) FROM product_alias"))
    }

    /**
     * Kisit ZINCIR bazinda. A101 "T.BUGDAY EKMEK 500G" yazarken Migros
     * "TAM BUGDAY EKMEGI" yaziyor; ayni ham metnin iki zincirde ayri eslemesi
     * mesru ve engellenmemeli.
     */
    @Test
    fun sameRawTextIsFreeAcrossChains() = runTest {
        val db = db()
        db.calistir(alias("a1", "A101", "tam bugday ekmek", "ekmek"))
        db.calistir(alias("a2", "MIGROS", "tam bugday ekmek", "ekmek"))
        assertEquals(2L, db.sayi("SELECT COUNT(*) FROM product_alias"))
    }
}
