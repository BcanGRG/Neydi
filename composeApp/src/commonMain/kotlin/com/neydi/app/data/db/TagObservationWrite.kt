package com.neydi.app.data.db

import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.matchKey
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
 * @param packSize etiketten okunan ambalaj boyu; `null` = OKUNAMADI, "1 paket"
 *   DEGIL. Ayrim shrinkflation'in tamami: `toPriceHint` iki gozlemin ikisinin
 *   de boyunu istiyor ve biri bilinmiyorsa degisim IDDIA ETMIYOR. Varsayilan
 *   bir deger koymak - `1.0` gibi - her okunamayan etiketi sahte bir ambalaj
 *   degisimine cevirirdi.
 * @param packUnit `"gr"`, `"kg"`, `"ml"`, `"lt"`, `"adet"` - `normalizeUnit`
 *   ciktisi. [packSize] ile birlikte anlamli; biri olmadan digeri yazilmiyor.
 *
 * ⚠ **AMBALAJ PARAMETRELERININ VARSAYILANI YOK ve bu bilincli.** F5.7'nin
 * kusuru tam olarak "bir alan yolun ortasinda dusuruldu" idi ve varsayilan
 * degerler o hatanin SESSIZ kalmasini saglayan seydi. Zorunlu olduklarinda
 * atlanmalari derleme hatasi - `save()`in cagri yeri ViewModel govdesinde
 * duruyor ve orasi birim testle korunamiyor (`viewModelScope` bir Main
 * dispatcher istiyor), yani o satirin tek nobetcisi derleyici.
 * @return yazildiysa `true`, 60 sn mukerrer penceresine takildiysa `false`.
 *   Cagiran bu ayrimi GORMEK zorunda: kullanici deklansore basti, bir sey
 *   soylenmeli - ama "yeni gozlem" demek yanlis olurdu.
 */
internal suspend fun writeTagObservation(
    repo: ListRepository,
    catalogSeedDao: CatalogSeedDao,
    priceObservationDao: PriceObservationDao,
    productAliasDao: ProductAliasDao,
    householdId: String,
    productName: String,
    priceMinor: Long,
    storeId: String?,
    brand: String?,
    packSize: Double?,
    packUnit: String?,
    /**
     * ETIKETTEN OKUNAN ad - urune baglanacak anahtar (F4.7).
     *
     * `null` = OCR ad okuyamadi; o zaman baglanacak bir sey yok ve bir sonraki
     * cekimde urun yine soruluyor. Uydurma bir anahtar yazmak daha kotu olurdu:
     * yanlis metin yanlis urune baglanir ve o esleme KALICI.
     */
    tagText: String?,
    /** `Store.chain` - esleme zincir bazinda kuruluyor. */
    chain: String?,
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
    // ETIKET METNI URUNE BAGLANIYOR (F4.7) - "ayni etiket bir daha sorulmaz".
    //
    // Gezinme sozlesmesi bunu dort ayri yerde soyluyor: *"Secim bu markette bu
    // etiket metnine baglanir; ayni etiket bir daha sorulmaz."* Tablo, tekil
    // indeks ve sorgu fis doneminden beri duruyordu - HIC CAGRILMIYORDU. Sonuc
    // kullanicinin markette yasadigi sey oldu: her cekimde urunu yeniden
    // secmek, ayni yogurdu ucuncu kez cekerken bile.
    //
    // ESLEME ZINCIR BAZINDA (`storeChain`), sube bazinda degil: ayni etiket
    // metni ayni zincirin her subesinde ayni urun. `Shopping.kt` bunu zaten
    // soyluyor - fiyat karsilastirmasi da zincir bazinda anlamli.
    //
    // KULLANICI SECTIGI ICIN ONAYLI (`confirmedAt`): tahmin degil, karar.
    if (tagText != null && chain != null) {
        val normalized = matchKey(tagText)
        if (normalized.isNotBlank()) {
            productAliasDao.insert(
                ProductAlias(
                    id = newId(),
                    householdId = householdId,
                    storeChain = chain,
                    rawTextNormalized = normalized,
                    productId = product.id,
                    confirmedAt = at,
                    createdAt = at,
                ),
            )
        }
    }

    return priceObservationDao.insertUnlessRecentDuplicate(
        PriceObservation(
            id = newId(),
            householdId = householdId,
            productId = product.id,
            storeId = storeId,
            unitPriceMinor = priceMinor,
            brand = brand?.trim()?.ifBlank { null },
            // IKISI BIRLIKTE ya da HIC. Boyu birimsiz yazmak `900` diye bir
            // sayi birakirdi ve gram mi mililitre mi belli olmazdi; sonraki
            // gozlemle karsilastirildiginda "900 -> 900" tutar ama iki ayri
            // birim olabilir. `packLabel` zaten ikisini birden istiyor,
            // yarim veri yazmak yalnizca kolonu kirletirdi.
            packSize = packSize?.takeIf { !packUnit.isNullOrBlank() },
            packUnit = packUnit?.trim()?.ifBlank { null }?.takeIf { packSize != null },
            observedAt = at,
            createdAt = at,
        ),
    )
}
