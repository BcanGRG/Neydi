package com.neydi.app.data.db

import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.repo.resolveProduct

/**
 * Onay kartindan gozleme - etiket cekiminin YAZMA yolu.
 *
 * ## Neden ViewModel'in icinde degil
 *
 * `viewModelScope` birim testte bir Main dispatcher istiyor ve bu projede
 * hicbir ViewModel testi yok - o kurulumu yalnizca bu yolu test edebilmek icin
 * getirmek, testi kurulumun kendisine bagimli kilardi. Serbest bir suspend
 * fonksiyon `commonTest`te GERCEK bir bellek-ici Room veritabaniyla dogrudan
 * kosuyor; `insertUnlessRecentDuplicate` da ayni sebeple serbest.
 *
 * Yan fayda daha onemli: yazma yolu artik kameradan da ekrandan da bagimsiz.
 *
 * @param brand `null` MESRU - manavda, dokme urunde marka yok. Bos dizgi
 *   kullanmak "markasiz" ile "okunamadi"yi ayni sepete atardi.
 * @return yazildiysa `true`, 60 sn mukerrer penceresine takildiysa `false`.
 *   Cagiran bu ayrimi GORMEK zorunda: kullanici deklansore basti, bir sey
 *   soylenmeli - ama "yeni gozlem" demek yanlis olurdu.
 */
internal suspend fun writeTagObservation(
    repo: ListRepository,
    catalogSeedDao: CatalogSeedDao,
    priceObservationDao: PriceObservationDao,
    householdId: String,
    productName: String,
    priceMinor: Long,
    storeId: String?,
    brand: String?,
    at: Long,
    newId: () -> String,
): Boolean {
    // URUN ONCE COZULUYOR: alias/katalog eslesmesi ayni ada iki urun
    // yaratmayi engelliyor. Gozlem urun kimligine bagli (karar 26), yani
    // yanlis cozulmus bir ad fiyat gecmisini ikiye bolerdi.
    val product = resolveProduct(
        repo = repo,
        catalogSeedDao = catalogSeedDao,
        householdId = householdId,
        name = productName.trim(),
    )
    return priceObservationDao.insertUnlessRecentDuplicate(
        PriceObservation(
            id = newId(),
            householdId = householdId,
            productId = product.id,
            storeId = storeId,
            unitPriceMinor = priceMinor,
            brand = brand?.trim()?.ifBlank { null },
            observedAt = at,
            createdAt = at,
        ),
    )
}
