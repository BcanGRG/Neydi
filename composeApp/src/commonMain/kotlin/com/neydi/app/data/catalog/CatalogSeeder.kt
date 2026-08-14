package com.neydi.app.data.catalog

import androidx.room3.Transactor
import androidx.room3.useWriterConnection
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.matchKey

/**
 * Gomulu katalogu ilk acilista veritabanina yazar.
 *
 * IDEMPOTENT: her acilista cagrilabilir. Kategori tablosu doluysa hicbir sey
 * yapmaz - yoksa uygulama her acildiginda 245 satiri yeniden yazardi.
 *
 * TEK ISLEMDE: 257 satir tek transaction icinde gidiyor. Tek tek yazsaydik
 * ilk acilis gozle gorulur sekilde yavaslardi ve yarida kesilirse katalog
 * eksik kalirdi - "yarim tohumlanmis" hal, hic tohumlanmamistan kotudur cunku
 * idempotency kontrolu doluymus gibi gorur.
 *
 * matchKey BURADA TURETILIYOR, veri dosyasinda saklanmiyor: normalizasyon
 * kurali (F2.4) tek yerde kalsin. Kural degisirse katalog yeniden tohumlanir,
 * iki ayri gercek kaynagi olusmaz.
 */
suspend fun NeydiDatabase.tohumlaKatalog(): CatalogSeedResult {
    val existing = useWriterConnection { t ->
        t.usePrepared("SELECT COUNT(*) FROM category") { it.step(); it.getLong(0) }
    }
    if (existing > 0) return CatalogSeedResult(skipped = true, category = 0, product = 0)

    useWriterConnection { transactor ->
        transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
            usePrepared(
                "INSERT INTO category (id, name, sortOrder, tintArgb) VALUES (?, ?, ?, ?)",
            ) { st ->
                SEED_CATEGORIES.forEach { k ->
                    st.bindText(1, k.id)
                    st.bindText(2, k.name)
                    st.bindInt(3, k.order)
                    st.bindLong(4, k.tonArgb)
                    st.step()
                    st.reset()
                }
            }
            usePrepared(
                """
                INSERT INTO catalog_seed (id, name, matchKey, categoryId, commonalityRank, defaultUnit)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ) { st ->
                SEED_PRODUCTS.forEach { u ->
                    // id yayginliktan turetiliyor: deterministik, yani ayni katalog
                    // her cihazda ayni id'leri uretir. Senkron acilinca iki telefon
                    // ayni tohum urununu ayri urun sanmaz.
                    st.bindText(1, "seed-${u.commonality}")
                    st.bindText(2, u.name)
                    st.bindText(3, matchKey(u.name))
                    st.bindText(4, u.categoryId)
                    st.bindInt(5, u.commonality)
                    st.bindText(6, u.unit)
                    st.step()
                    st.reset()
                }
            }
        }
    }
    return CatalogSeedResult(
        skipped = false,
        category = SEED_CATEGORIES.size,
        product = SEED_PRODUCTS.size,
    )
}

data class CatalogSeedResult(
    val skipped: Boolean,
    val category: Int,
    val product: Int,
)
