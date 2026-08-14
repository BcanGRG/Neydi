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
    val mevcut = useWriterConnection { t ->
        t.usePrepared("SELECT COUNT(*) FROM category") { it.step(); it.getLong(0) }
    }
    if (mevcut > 0) return CatalogSeedResult(atlandi = true, kategori = 0, urun = 0)

    useWriterConnection { transactor ->
        transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
            usePrepared(
                "INSERT INTO category (id, name, sortOrder, tintArgb) VALUES (?, ?, ?, ?)",
            ) { st ->
                SEED_CATEGORIES.forEach { k ->
                    st.bindText(1, k.id)
                    st.bindText(2, k.ad)
                    st.bindInt(3, k.sira)
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
                    st.bindText(1, "seed-${u.yayginlik}")
                    st.bindText(2, u.ad)
                    st.bindText(3, matchKey(u.ad))
                    st.bindText(4, u.kategoriId)
                    st.bindInt(5, u.yayginlik)
                    st.bindText(6, u.birim)
                    st.step()
                    st.reset()
                }
            }
        }
    }
    return CatalogSeedResult(
        atlandi = false,
        kategori = SEED_CATEGORIES.size,
        urun = SEED_PRODUCTS.size,
    )
}

data class CatalogSeedResult(
    val atlandi: Boolean,
    val kategori: Int,
    val urun: Int,
)
