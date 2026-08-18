package com.neydi.app.data.store

import com.neydi.app.data.db.Store
import com.neydi.app.data.db.StoreDao

/**
 * Turkiye'nin yaygin market zincirleri - etiket cekiminde secilecek liste.
 *
 * NEDEN TOHUMLANIYOR (tasarim karari 11, pivotla revize): magazanin eskiden tek
 * kaynagi fis kunyesiydi ve o kaynak kalmadi. Kullanici artik etiket cekerken
 * market SECIYOR, yani secenegin cekimden once orada olmasi gerekiyor - bos bir
 * listeyle karsilasan kullanicinin ilk isi veri girmek olurdu.
 *
 * KARARIN OLCUTU DEGISMEDI, cevabi degisti. Karar 11 "elle eklenen magaza
 * karsilastirmaya veri katmiyorsa cizilmez" diyor. Artik katiyor: gozlemin
 * marketi kullanicinin seciminden geliyor ve karsilastirmanin ekseni o.
 *
 * ADLAR ETIKETTE YAZDIGI GIBI (karar 22): BIM, A101, SOK degil "BİM", "A101",
 * "ŞOK". Baslik duzenine cevirmek locale gerektirirdi ve bu projede locale'siz
 * harf donusumu yasak.
 *
 * ID'LER DETERMINISTIK: katalog tohumlarindaki (`seed-<yayginlik>`) ayni desen.
 * Rastgele id verilseydi her acilis yeni satir yazardi - `insert` IGNORE olsa
 * bile cakisma birincil anahtarda degil, hicbir yerde olmazdi.
 *
 * `internal`, private DEGIL - ve sebebi bir hata: Ayarlar onizlemesi uc kisa
 * ad uyduruyordu ("MİGROS, A101, BİM") ve o fikstur satira tam sigdigi icin
 * gercekte YEDI zincirle olusan bozulmayi hic gostermedi. Onizleme artik
 * tohumun kendisini okuyor; fikstur ile gercek bir daha ayrisamaz.
 */
internal val SEED_CHAINS = listOf(
    "BİM",
    "A101",
    "ŞOK",
    "Migros",
    "CarrefourSA",
    "File",
    "Tarım Kredi",
    // TOPTANCILAR KULLANICIDAN GELDI ve tohuma girmelerinin sebebi pratik:
    // uygulamada market EKLEME yolu yok (`storeDao.insert` yalnizca burasi),
    // yani tohumda olmayan bir markette cekim yapmak demek gozlemi marketsiz
    // ya da YANLIS marketle yazmak demek. Ikisi de karar 26'nin market+marka
    // kimligini bozar - ve toplu alisveris tam da fiyat gecmisinin en cok ise
    // yaradigi yer.
    "Fullgross",
    "Gimat",
    // METRO TOHUMA GIRMIYOR ve bu tasarimdan BILINCLI bir sapma. Karar 48
    // Metro'yu sekizinci zincir yapiyor, olcutu "kullanicinin gercekten etiket
    // cektigi zincirler" - ve kullanici orada 34 etiket cekmisti. Ama o cekim
    // tek seferlikti: kullanici Metro'ya duzenli gitmedigini soyledi ve oraya
    // efor harcanmamasini istedi. Olcut kullanicinin kendi aliskanligi
    // oldugu icin, olcutun cevabini da kullanici veriyor.
)

/**
 * Zincirleri tohumlar. IKI KATMANLI IDEMPOTENSI ve ikisi de gerekli:
 *
 * 1. **Id deterministik** (`store-seed-<zincir>`) + `insert` IGNORE →
 *    tohumun kendisi her acilista tekrar kosabiliyor.
 * 2. **Zincir zaten varsa hic yazilmiyor** → tohum, BASKA bir yoldan gelmis
 *    ayni zinciri ikinci kez yaratmiyor.
 *
 * Ikincisi teorik degil: fis doneminde `rememberStore` kunyeden okudugu adi
 * magaza olarak yaziyordu ve cihazda `BIM` ile `FiLE` satirlari o yoldan
 * dogmustu. Yalnizca id'ye bakan bir tohum onlarin yanina ikinci bir `bim`
 * satiri koyardi; `findByChain` LIMIT 1 oldugu icin hangisinin donecegi de
 * belirsiz olurdu - alias ogrenmesi ile gozlemin marketi ayrisirdi.
 *
 * ONCE GELEN KAZANIR: kullanicinin kendi eklemis oldugu bir "Migros" satirini
 * tohum ezmiyor. Tohum bir baslangic kolayligi, dayatma degil.
 *
 * KULLANICI SILERSE GERI GELMEZ: silinen satir `deletedAt` tasiyor ama
 * birincil anahtari duruyor, IGNORE onu diriltmiyor.
 */
internal suspend fun seedStores(dao: StoreDao, householdId: String, clock: () -> Long) {
    val now = clock()
    SEED_CHAINS.forEach { name ->
        val chain = chainKey(name)
        if (dao.findByChain(householdId, chain) != null) return@forEach
        dao.insert(
            Store(
                id = "store-seed-$chain",
                householdId = householdId,
                name = name,
                chain = chain,
                createdAt = now,
            ),
        )
    }
}
