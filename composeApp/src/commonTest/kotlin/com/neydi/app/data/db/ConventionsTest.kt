package com.neydi.app.data.db

import com.neydi.app.di.newUuid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Conventions.kt madde 1'in TESTI.
 *
 * Kural yazilidiydi ("id = UUID v7 metni. v4 degil") ve kod tam tersini
 * yapiyordu: `Uuid.random()` v4 uretiyor (kotlin-stdlib 2.4.10, `Uuid.kt:581`:
 * `random() = generateV4()`). Bir sozlesme yalnizca yorumda yasarsa sessizce
 * ihlal edilebilir - bu dosyanin varlik sebebi o sessizligi kaldirmak.
 */
class ConventionsTest {

    /**
     * UUID metninin 15. karakteri SURUM NIBBLE'I.
     *
     * `xxxxxxxx-xxxx-Mxxx-...` biciminde M surumu veriyor: v4 ise '4', v7 ise
     * '7'. Yani bu tek karakter kurali dogrudan olcuyor.
     */
    @Test
    fun generatedIdsAreUuidV7() {
        repeat(20) {
            val id = newUuid()
            assertEquals(36, id.length, "beklenmeyen uzunluk: $id")
            assertEquals('7', id[14], "v7 degil (surum nibble'i '${id[14]}'): $id")
        }
    }

    /**
     * v7'NIN TEK VAADI: zaman siralanabilirlik.
     *
     * Room id'leri TEXT sakliyor, yani siralama sozluk sirasi. v7'nin onundeki
     * zaman damgasi bu siralamada gercekten artiyor; v4'te artmiyor ve birincil
     * anahtar index'i sona eklenmek yerine ortasina serpistiriliyor. Kurali
     * "surum nibble'i dogru" diye test etmek yetmez - iddia edilen DAVRANIS
     * test edilmeli.
     */
    @Test
    fun idsSortLexicographicallyInGenerationOrder() {
        val ids = List(50) { newUuid() }
        assertEquals(ids, ids.sorted(), "id'ler uretim sirasinda sozluk sirasinda degil")
    }

    /** Ayni id iki kez uretilmemeli. */
    @Test
    fun idsAreUnique() {
        val ids = List(500) { newUuid() }
        assertEquals(ids.size, ids.toSet().size)
    }

    /** RFC 9562 variant biti: 17. karakter 8/9/a/b olmali. */
    @Test
    fun variantBitsAreRfcCompliant() {
        repeat(20) {
            val id = newUuid()
            assertTrue(id[19] in "89ab", "variant biti hatali ('${id[19]}'): $id")
        }
    }
}
