package com.neydi.app.data.catalog

import androidx.room3.Transactor
import androidx.room3.useWriterConnection
import com.neydi.app.data.db.NeydiDatabase
import com.neydi.app.data.matchKey

/**
 * GOMULU KATALOGUN SURUMU (F2.7).
 *
 * ## Bu sabit elle artiriliyor ve unutulursa sessizce hicbir sey olmuyor
 *
 * `SEED_CATEGORIES` ya da `SEED_PRODUCTS` degistiginde **bu sayi da bir
 * artmali**. Artirilmazsa kurulu telefonlar eski katalogla kalir - degisiklik
 * yalnizca uygulamayi silip yeniden kuranlara gider.
 *
 * Otomatik bir imza (ornegin listelerin hash'i) unutmayi imkansiz kilardi ama
 * her derlemede degisen bir degeri semaya yazmak demekti: bir bosluk
 * duzeltmesi butun cihazlarda 257 satirlik bir yeniden yazma tetiklerdi.
 * Elle artirmanin bedeli unutmak, otomatigin bedeli her seferinde yazmak;
 * ikincisi daha pahali.
 */
internal const val CATALOG_SEED_VERSION = 1

/**
 * Gomulu katalogu veritabanina yazar - ve **surum degistiyse yeniden yazar**.
 *
 * ## Once neden yeniden yazmiyordu ve bu neden bir kullanici hatasiydi
 *
 * Kapida `SELECT COUNT(*) FROM category > 0` vardi: kategori tablosu bir kez
 * doldu mu, tohumlayici bir daha hicbir sey yapmiyordu. Yani ilk acilistan
 * sonra `CatalogSeedData`daki hicbir duzeltme o telefona **ULASMIYORDU** -
 * yeni bir urun, duzeltilmis bir kategori, degismis bir `matchKey` kurali,
 * hicbiri. Sessiz, cunku hicbir sey patlamiyor; kullanici yalnizca
 * "guncellemeyle bir sey degismedi" diye dusunuyor.
 *
 * Artik kapi SURUME bakiyor ([CATALOG_SEED_VERSION]). Eski kurulumlarda kolon
 * `null` ve bu **"bilinmiyor, yeniden yaz"** demek.
 *
 * `householdId` ZORUNLU ve varsayilansiz. Bir ara nullable yazilmisti ve o
 * hali sessiz bir "her acilista yeniden yaz" modu uretiyordu: damga
 * okunamayinca kapi hep aciliyordu. Zorunlu olmasi cagirani damgayi dusunmeye
 * mecbur ediyor.
 *
 * ## SILME YOK, UZERINE YAZMA VAR
 *
 * Yeniden tohumlama `DELETE` + `INSERT` DEGIL, `INSERT OR REPLACE`. Sebep
 * yikici: `product.categoryId` kategorilere bakiyor ve kullanicinin kendi
 * urunleri de o kategorilerde. Kategorileri silip yeniden yazmak, aradaki o
 * anda butun kullanici urunlerini sahipsiz birakirdi - ve `matchKey`i
 * degismis bir tohum urunu silinseydi, ona bagli `product_alias` satirlari da
 * kopardi.
 *
 * Uzerine yazmanin guvenli olmasinin sarti id'lerin DETERMINISTIK olmasi:
 * kategoriler sabit id tasiyor, tohum urunleri `seed-<yayginlik>`. Ayni
 * katalog her cihazda ayni id'leri uretiyor, yani "ayni satir" kavrami
 * gercek.
 *
 * ## Kullanicinin duzeltmeleri EZILIYOR mu
 *
 * Hayir - bu iki tablo da **salt tohum**. Kullanicinin urunleri `product`
 * tablosunda, listesi `trip_line`da; ikisine de dokunulmuyor. `catalog_seed`
 * yalnizca kesif sheet'inin ve ad cozumlemesinin okudugu bir sozluk.
 *
 * ## TEK ISLEMDE
 *
 * 257 satir tek transaction icinde. Tek tek yazsaydik ilk acilis gozle
 * gorulur sekilde yavaslardi ve yarida kesilirse katalog eksik kalirdi -
 * "yarim tohumlanmis" hal, hic tohumlanmamistan kotudur cunku surum damgasi
 * dolmus olurdu.
 *
 * matchKey BURADA TURETILIYOR, veri dosyasinda saklanmiyor: normalizasyon
 * kurali (F2.4) tek yerde kalsin. Kural degisirse [CATALOG_SEED_VERSION]
 * artiriliyor ve katalog yeniden tohumlaniyor - iki ayri gercek kaynagi
 * olusmuyor.
 */
suspend fun NeydiDatabase.seedCatalog(
    householdId: String,
    /** Damga satiri henuz yoksa `createdAt`i icin - NOT NULL. */
    clock: () -> Long = { 0L },
): CatalogSeedResult {
    val written = appSettingsDao().byHousehold(householdId)?.catalogSeedVersion
    val empty = useWriterConnection { t ->
        t.usePrepared("SELECT COUNT(*) FROM category") { it.step(); it.getLong(0) } == 0L
    }
    // GUNCEL SAYILMANIN IKI SARTI: damga guncel VE tablo dolu. Ikincisi
    // olmasaydi "Verilerimi sil"den sonra damga kalir, katalog bos kalirdi.
    if (!empty && written == CATALOG_SEED_VERSION) {
        return CatalogSeedResult(skipped = true, category = 0, product = 0)
    }

    useWriterConnection { transactor ->
        transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
            usePrepared(
                "INSERT OR REPLACE INTO category (id, name, sortOrder, tintArgb) VALUES (?, ?, ?, ?)",
            ) { st ->
                SEED_CATEGORIES.forEach { k ->
                    st.bindText(1, k.id)
                    st.bindText(2, k.name)
                    st.bindInt(3, k.order)
                    st.bindLong(4, k.tintArgb)
                    st.step()
                    st.reset()
                }
            }
            usePrepared(
                """
                INSERT OR REPLACE INTO catalog_seed
                    (id, name, matchKey, categoryId, commonalityRank, defaultUnit)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ) { st ->
                SEED_PRODUCTS.forEach { u ->
                    // id yayginliktan turetiliyor: deterministik, yani ayni katalog
                    // her cihazda ayni id'leri uretir. Senkron acilinca iki telefon
                    // ayni tohum urununu ayri urun sanmaz - ve yeniden tohumlama
                    // "ayni satiri" bulabilir.
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
    // DAMGA EN SONDA: transaction yarida kesilirse damga da yazilmiyor ve bir
    // sonraki acilis yeniden deniyor.
    appSettingsDao().ensureRow(householdId, clock())
    appSettingsDao().stampCatalogSeedVersion(householdId, CATALOG_SEED_VERSION)
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
