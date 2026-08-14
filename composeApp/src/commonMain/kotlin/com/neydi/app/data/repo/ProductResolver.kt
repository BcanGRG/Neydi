package com.neydi.app.data.repo

import com.neydi.app.data.db.CatalogSeedDao
import com.neydi.app.data.db.Product
import com.neydi.app.data.matchKey

/** Katalogda eslesmeyen serbest urunler buraya duser. */
const val DEFAULT_CATEGORY = "temel-gida"

/**
 * Serbest bir addan urun uretir: katalogda ayni matchKey varsa KANONIK adi ve
 * kategorisini kullanir.
 *
 * IKI GIRIS KAPISI PAYLASIYOR: elle/panodan ekleme (F3.x) ve fis duzeltmesi
 * (F4.7). Ayni mantigi iki yerde yazmak, birinde "sut" -> "Süt" duzeltmesi
 * yapip otekinde yapmamak demekti; o zaman ayni urun iki farkli yazimla iki
 * kez olusurdu ve matchKey ikisini ayni saydigi icin sonradan da
 * duzeltilemezdi.
 *
 * @param categoryId cagiran taraf kategoriyi biliyorsa (katalogdan secildi)
 *   verilir ve katalog aramasi hic yapilmaz.
 */
suspend fun resolveProduct(
    repo: ListRepository,
    catalogSeedDao: CatalogSeedDao,
    householdId: String,
    name: String,
    categoryId: String? = null,
    unit: String? = null,
): Product {
    val key = matchKey(name)
    val seed = if (categoryId != null) {
        null
    } else {
        // TAM eslesme sarti: search() on-ek de dondurur, "sut" aramasi "sutlu
        // kek" getirebilir. Onu kanonik ad sanmak urunu yanlis adla olustururdu.
        catalogSeedDao.search(key, limit = 1).firstOrNull { it.matchKey == key }
    }
    return repo.findOrCreateProduct(
        householdId = householdId,
        name = seed?.name ?: name,
        categoryId = categoryId ?: seed?.categoryId ?: DEFAULT_CATEGORY,
        unit = unit ?: seed?.defaultUnit ?: "adet",
    )
}
