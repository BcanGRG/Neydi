package com.neydi.app.data.db

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO'lar SUSPEND-FIRST + Flow. Bloklayan sorgu imzasi hic acilmiyor: Room 2
 * aliskanligi ve sonradan tasima maliyeti cikarir.
 *
 * KAPSAM BILEREK DAR: burada yalnizca Liste ekraninin (Faz 3) ihtiyaci olanlar
 * var. Fis, fiyat ve senkron DAO'lari kendi fazlarinda gelecek - simdi yazmak
 * kullanilmayan koda bakim borcu demek.
 *
 * `deletedAt IS NULL` HER SORGUDA: gercek silme yok, tombstone var. Filtreyi
 * unutan bir sorgu silinmis satirlari geri getirir ve bu, kullanicinin
 * "sildigim urun geri geldi" diye yasadigi seydir.
 */
@Dao
interface CategoryDao {
    /** Market gezme sirasinda. sortOrder alfabetik DEGIL - bkz. CatalogSeedData. */
    @Query("SELECT * FROM category ORDER BY sortOrder")
    fun observeAll(): Flow<List<Category>>
}

@Dao
interface CatalogSeedDao {
    /**
     * Yazarken tamamlama. Yayginliga gore siralaniyor: "ek" yazan biri once
     * Ekmek gormeli, alfabetik olarak once geleni degil.
     */
    @Query(
        """
        SELECT * FROM catalog_seed
        WHERE matchKey LIKE :onExtra || '%'
        ORDER BY commonalityRank
        LIMIT :limit
        """,
    )
    suspend fun search(onExtra: String, limit: Int = 20): List<CatalogSeed>

    /** Bos durumda bir reyona dokununca: o reyonun EN YAYGIN urunleri. */
    @Query("SELECT * FROM catalog_seed WHERE categoryId = :categoryId ORDER BY commonalityRank LIMIT :limit")
    suspend fun byCategory(categoryId: String, limit: Int = 8): List<CatalogSeed>

    /**
     * Katalogdaki EN YAYGIN N urun - ilk gun bos durumunun cipleri
     * (tasarim karari 5).
     *
     * Sabit bir liste DEGIL: sira katalogdan geldigi icin katalog buyudukce
     * grid kendiliginden tazeleniyor ve ayni bilginin iki ayri kopyasi
     * olmuyor.
     */
    @Query("SELECT * FROM catalog_seed ORDER BY commonalityRank LIMIT :limit")
    suspend fun mostCommon(limit: Int = 12): List<CatalogSeed>

    @Query("SELECT COUNT(*) FROM catalog_seed")
    suspend fun count(): Int
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM product WHERE householdId = :householdId AND deletedAt IS NULL ORDER BY name")
    fun observeAll(householdId: String): Flow<List<Product>>

    @Query("SELECT * FROM product WHERE householdId = :householdId AND matchKey = :matchKey AND deletedAt IS NULL LIMIT 1")
    suspend fun findByMatchKey(householdId: String, matchKey: String): Product?

    @Query("SELECT * FROM product WHERE id = :id AND deletedAt IS NULL")
    suspend fun byId(id: String): Product?

    @Query("SELECT * FROM product WHERE householdId = :householdId AND isStaple = 1 AND deletedAt IS NULL ORDER BY name")
    fun observeStaples(householdId: String): Flow<List<Product>>

    /**
     * Yeni listeye otomatik eklenecek sabitler.
     *
     * Flow surumu Ayarlar'in listesi icin; bu suspend surum yeni gezi acilirken
     * TEK SEFER okunuyor. Flow'u orada toplamak gezi acmayi bir aboneligin
     * ilk yayinina bagimli yapardi.
     */
    @Query("SELECT * FROM product WHERE householdId = :householdId AND isStaple = 1 AND deletedAt IS NULL ORDER BY name")
    suspend fun staples(householdId: String): List<Product>

    /**
     * "Her zamankiler"e ekle / cikar (F6.8).
     *
     * `isStaple` BES YERDE OKUNUYORDU, SIFIR YERDE YAZILIYORDU - yani %70
     * opaklik dali ve raptiye calisan uygulamada erisilemezdi, Ekran 3'un
     * "Her zamankiler" bolumu daima bos kalirdi ve "bos ise ekran acilmaz"
     * kuraliyla birlesince Ekran 3 **hic acilamazdi**.
     *
     * KULLANICI ISARETLER, MOTOR DEGIL: `Product.isStaple` KDoc'u bunu sart
     * kosuyor - *"oneri motoru kendi basina set ETMEZ"*. Sabitlik bir cikarim
     * degil, beyan.
     *
     * `updatedAt` de yaziliyor (v3 kolonu): LWW'nin karsilastiracagi damga
     * olmadan senkron bu yazmayi kaybedebilirdi.
     */
    @Query("UPDATE product SET isStaple = :isStaple, updatedAt = :at WHERE id = :id")
    suspend fun setStaple(id: String, isStaple: Boolean, at: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    /** Gercek silme DEGIL - tombstone. Bkz. Conventions.kt madde 3. */
    @Query("UPDATE product SET deletedAt = :at WHERE id = :id")
    suspend fun softDelete(id: String, at: Long)
}

@Dao
interface TripDao {
    /**
     * Aktif alisveris. "Ayni anda tek aktif trip" kisitini SEMA zorlamiyor -
     * kismi index gerekir ve Room yazamiyor (F2.3). Kural repository'de;
     * bu sorgu LIMIT 1 ile en yenisini alarak bozuk duruma karsi dayanikli.
     */
    @Query(
        """
        SELECT * FROM trip
        WHERE householdId = :householdId AND completedAt IS NULL AND deletedAt IS NULL
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    fun observeActive(householdId: String): Flow<Trip?>

    @Query(
        """
        SELECT * FROM trip
        WHERE householdId = :householdId AND completedAt IS NULL AND deletedAt IS NULL
        ORDER BY startedAt DESC LIMIT 1
        """,
    )
    suspend fun activeOrNull(householdId: String): Trip?

    @Query("SELECT * FROM trip WHERE householdId = :householdId AND completedAt IS NOT NULL AND deletedAt IS NULL ORDER BY completedAt DESC LIMIT :limit")
    fun observeHistory(householdId: String, limit: Int = 50): Flow<List<Trip>>

    /** En son kapanan gezi - "Gecen sefer aldiklarini ekle" bunun uzerinden calisiyor. */
    @Query(
        """
        SELECT * FROM trip
        WHERE householdId = :householdId AND completedAt IS NOT NULL AND deletedAt IS NULL
        ORDER BY completedAt DESC LIMIT 1
        """,
    )
    suspend fun lastClosed(householdId: String): Trip?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(trip: Trip)

    @Query("SELECT * FROM trip WHERE id = :id")
    suspend fun byId(id: String): Trip?

    /**
     * PLANNING <-> SHOPPING. Kapanmis geziye DOKUNMUYOR.
     *
     * completedAt IS NULL kosulu sart: kullanici ozet kartini kapatirken alisveris
     * modu anahtari da sifirlaniyor ve o yazma kapanmis geziyi SHOPPING'e geri
     * cekebilirdi - gezi yeniden "acik" gorunur, gecmisten kaybolur, ve bir
     * sonraki eklemede yeni gezi acilmaz.
     *
     * @return guncellenen satir sayisi; 0 = gezi kapanmis, gecis yapilmadi.
     */
    @Query("UPDATE trip SET status = :status WHERE id = :id AND completedAt IS NULL")
    suspend fun setStatus(id: String, status: TripStatus): Int

    /**
     * Gezinin odenen tutari - fislerden devrediliyor (F4.11).
     *
     * `completedAt IS NULL` KOSULU YOK, bilerek: fis ancak gezi kapandiktan
     * sonra ozet kartindan eklenebiliyor, yani bu yazma her zaman kapanmis bir
     * geziye gidiyor. Diger UPDATE'lerdeki kosulu buraya kopyalamak yazmayi
     * sessizce hicbir seye cevirirdi.
     */
    @Query("UPDATE trip SET totalMinor = :total WHERE id = :id")
    suspend fun setTotal(id: String, total: Long?)

    /**
     * Geziyi fisten okunan magazaya baglar (tasarim karari 11).
     *
     * `storeId IS NULL` KOSULU VAR: ilk okunan magaza kaliyor. Cok parcali bir
     * fisin ikinci parcasi yanlis okunursa gezi mağaza degistirmemeli - ve
     * kullanicinin elle duzeltmesi de (o yuzey yazilinca) bu yazmayla
     * ezilmemeli.
     */
    @Query("UPDATE trip SET storeId = :storeId WHERE id = :id AND storeId IS NULL")
    suspend fun setStoreIfAbsent(id: String, storeId: String)

    /**
     * KARSILASTIR-VE-YAZ ile kapatir. "TEK CIHAZ KAPATIR" kuralini ZORLAYAN yer.
     *
     * completedAt IS NULL sayesinde ikinci kapatma denemesi SIFIR satir
     * gunceller: `ownerMemberId` ilk kapatanda kalir, `completedAt` ilerlemez,
     * ve cagiran taraf donen sayidan "ben kapattim" ile "zaten kapanmis"
     * arasindaki farki gorur. Bunu iki adima (once oku, sonra yaz) bolmek
     * aradaki yaris penceresini geri getirirdi.
     *
     * status ve completedAt AYNI ifadede yaziliyor - Trip'teki degismez kural
     * (CLOSED <=> completedAt != null) ancak boyle korunur.
     *
     * @return 1 = bu cagri kapatti, 0 = zaten kapaliydi.
     */
    @Query(
        """
        UPDATE trip SET status = 'CLOSED', ownerMemberId = :memberId, completedAt = :at
        WHERE id = :id AND completedAt IS NULL
        """,
    )
    suspend fun closeIfOpen(id: String, memberId: String, at: Long): Int
}

@Dao
interface TripLineDao {
    @Query("SELECT * FROM trip_line WHERE tripId = :tripId AND deletedAt IS NULL ORDER BY createdAt")
    fun observeLines(tripId: String): Flow<List<TripLine>>

    /**
     * Bir gezide GERCEKTEN ALINAN satirlar - "Gecen sefer aldiklarini ekle"
     * bos durum butonunun kaynagi (Ekran 1 tasarimi).
     *
     * `checked = 1` tek olcut ve yeterli: uc-sonuc secici (F4.12)
     * NOT_NEEDED ve FORGOTTEN satirlarini isaretsiz birakiyor, iyimser
     * mutabakat (F4.8) ise kapanista alinanlari isaretliyor. Yani
     * "gerekmedi" dedigi bir urunu bir sonraki listeye geri koymuyoruz -
     * kullanicinin acikca reddettigi seyi tekrar onune surmek olurdu.
     */
    @Query(
        """
        SELECT * FROM trip_line
        WHERE tripId = :tripId AND checked = 1 AND deletedAt IS NULL
        ORDER BY createdAt
        """,
    )
    suspend fun takenForTrip(tripId: String): List<TripLine>

    /** Gezideki CANLI satirlarin urun kimlikleri - iki kez eklemeyi onlemek icin. */
    @Query("SELECT productId FROM trip_line WHERE tripId = :tripId AND deletedAt IS NULL")
    suspend fun productIdsForTrip(tripId: String): List<String>

    /**
     * Gezi basina satir sayisi - Gecmis satirindaki *"14 Agu · 18 urun"*.
     *
     * TEK SORGU, gezi basina bir sorgu DEGIL: elli gezilik bir gecmiste N+1
     * sorgu acmak listeyi acilirken kilitlerdi.
     */
    @Query(
        """
        SELECT tl.tripId AS tripId, COUNT(*) AS lineCount
        FROM trip_line tl
        JOIN trip t ON t.id = tl.tripId
        WHERE t.householdId = :householdId AND tl.deletedAt IS NULL AND t.deletedAt IS NULL
        GROUP BY tl.tripId
        """,
    )
    fun observeLineCounts(householdId: String): Flow<List<TripLineCount>>

    /**
     * Liste ekraninin tek sorgusu. JOIN Kotlin tarafinda birlestirmekten iyi:
     * ucu ayri Flow'u combine etmek her degisimde uc yeniden yayin uretir ve
     * satirlar bir kare bosluklu gorunur.
     *
     * Siralama: once REYON (market gezme sirasi), sonra eklenme zamani.
     * Alfabetik siralamak insani markette ileri geri yurutur.
     */
    @Query(
        """
        SELECT
            tl.id            AS rowId,
            p.id             AS productId,
            p.name           AS name,
            tl.quantity      AS count,
            tl.unit          AS unit,
            tl.checked       AS checked,
            p.isStaple       AS isStaple,
            c.id             AS categoryId,
            c.name           AS categoryName,
            c.sortOrder      AS categoryOrder,
            tl.addedByMemberId AS addedByMemberId,
            tl.note          AS note,
            tl.takeOutcome   AS takeOutcome
        FROM trip_line tl
        JOIN product p  ON p.id = tl.productId
        JOIN category c ON c.id = p.categoryId
        WHERE tl.tripId = :tripId AND tl.deletedAt IS NULL AND p.deletedAt IS NULL
        ORDER BY c.sortOrder, tl.createdAt
        """,
    )
    fun observeList(tripId: String): Flow<List<ListRowProjection>>

    @Query("SELECT * FROM trip_line WHERE tripId = :tripId AND productId = :productId AND deletedAt IS NULL LIMIT 1")
    suspend fun find(tripId: String, productId: String): TripLine?

    /**
     * SILINMISLERI DE GETIRIR - ve bu sart.
     *
     * Tombstone satiri tabloda KALIYOR, UNIQUE(tripId, productId) ise
     * deletedAt'i bilmiyor. Yani "listeden cikardim, sonra geri ekledim"
     * akisinda normal insert kisita carpar ve uygulama coker. Cagiran taraf
     * once buraya bakip mezardan cikarmali.
     *
     * Kismi unique index (WHERE deletedAt IS NULL) sorunu semada cozerdi ama
     * Room yazamiyor (F2.3).
     */
    @Query("SELECT * FROM trip_line WHERE tripId = :tripId AND productId = :productId LIMIT 1")
    suspend fun findIncludingDeleted(tripId: String, productId: String): TripLine?

    /**
     * ABORT, REPLACE DEGIL. UNIQUE(tripId, productId) ihlali GORULMELI:
     * REPLACE olsaydi esin ekledigi satir sessizce ezilir, adet ve
     * "kim ekledi" bilgisi kaybolurdu. Cagiran taraf once find() ile bakip
     * varsa adet artirmali - F2.3'un korudugu davranis tam olarak bu.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(line: TripLine)

    @Update
    suspend fun update(line: TripLine)

    /**
     * VARSAYILAN-IYIMSER MUTABAKAT (F4.8).
     *
     * Isaretlenmemis butun planli satirlari ALINDI yaziyor. Gerekcesi: fis
     * cekilmeyen gezide (BIM/A101/SOK ya da unutulan fotograf) tek bilgi
     * kaynagi listenin kendisi, ve kullanicinin reyonda her satiri tek tek
     * isaretlemesini beklemek gercekci degil - tembel kullanim da
     * KULLANILABILIR VERI uretmeli, yoksa oneri motoru hic beslenmez ve
     * uygulamanin ikinci varlik sebebi hic calismaz.
     *
     * Yanlis yonde iyimser olmasi bilincli: "aldim sandim ama almadim"
     * kullanicinin Bitir ekranindan tek dokunusla geri alabilecegi bir hata;
     * "almadim sandim ama aldim" ise hicbir yerde gorunmez ve satin alma
     * gecmisinde kalici bir bosluk birakir.
     *
     * IKI KEZ CALISMAMALI: cagiran taraf yalnizca closeIfOpen 1 dondurunce
     * cagiriyor. Iki cihaz ayni geziyi kapatirsa satin almalar cift sayilir ve
     * medianIntervalDays yariya duser.
     *
     * @return alindi yazilan satir sayisi.
     */
    @Query(
        """
        UPDATE trip_line SET checked = 1, checkedAt = :at
        WHERE tripId = :tripId AND checked = 0 AND deletedAt IS NULL
        """,
    )
    suspend fun markAllTaken(tripId: String, at: Long): Int

    /**
     * Urunun SON kapali gezideki beyani (F6.2).
     *
     * Skor formulunun "gecen sefer unutuldu mu" girdisi. Yalnizca kapali
     * gezilere bakiyor: acik gezideki satirin akibeti henuz yok.
     */
    @Query(
        """
        SELECT tl.takeOutcome FROM trip_line tl
        JOIN trip t ON t.id = tl.tripId
        WHERE tl.productId = :productId
          AND t.completedAt IS NOT NULL AND t.deletedAt IS NULL
          AND tl.deletedAt IS NULL
        ORDER BY t.completedAt DESC LIMIT 1
        """,
    )
    suspend fun lastOutcome(productId: String): TakeOutcome?

    /**
     * UC SONUCU TEK YAZMADA (F4.12).
     *
     * `checked` ve `takeOutcome` AYNI ifadede yaziliyor cunku ikisi tek bir
     * gercegin iki yuzu: "gerekmedi" ya da "unuttum" demek **alinmadi** demek.
     * Iki ayri yazma, arada kesilirse "alindi ama unuttum" gibi kendisiyle
     * celisen bir satir birakirdi - ve o satir hem `ProductStats`'a alim olarak
     * girer hem skorda unutulmus sayilirdi.
     */
    @Query(
        """
        UPDATE trip_line SET checked = :checked, checkedAt = :at, takeOutcome = :outcome
        WHERE id = :id
        """,
    )
    suspend fun setOutcome(id: String, checked: Boolean, at: Long?, outcome: TakeOutcome)

    @Query("UPDATE trip_line SET checked = :checked, checkedAt = :at WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean, at: Long?)

    @Query("UPDATE trip_line SET deletedAt = :at WHERE id = :id")
    suspend fun softDelete(id: String, at: Long)

    @Delete
    suspend fun delete(line: TripLine)
}

@Dao
interface PriceObservationDao {
    /**
     * Aktif alisverisin TAHMINI tutari, kurus.
     *
     * Her urun icin EN SON gozlenen birim fiyat x adet. "En son" olmasi sart:
     * ortalama almak zamla birlikte gercegin gerisinde kalir ve tahmin surekli
     * dusuk cikar.
     *
     * Fiyati HIC bilinmeyen urunler toplama girmez - bu yuzden tahmin her zaman
     * eksik yonde yanlis olabilir. Ekran bunu "en az su kadar" diye sunmali,
     * kesin tutar gibi degil.
     */
    @Query(
        """
        SELECT COALESCE(SUM(tl.quantity * po.unitPriceMinor), 0)
        FROM trip_line tl
        JOIN price_observation po ON po.id = (
            SELECT id FROM price_observation
            WHERE productId = tl.productId AND deletedAt IS NULL
            ORDER BY observedAt DESC LIMIT 1
        )
        WHERE tl.tripId = :tripId AND tl.deletedAt IS NULL
        """,
    )
    fun observeEstimate(tripId: String): Flow<Long>

    /** Tahmine giren urun sayisi: kacinin fiyatini bildigimizi soylemek icin. */
    @Query(
        """
        SELECT COUNT(*) FROM trip_line tl
        WHERE tl.tripId = :tripId AND tl.deletedAt IS NULL
          AND EXISTS (SELECT 1 FROM price_observation po
                      WHERE po.productId = tl.productId AND po.deletedAt IS NULL)
        """,
    )
    fun observePricedCount(tripId: String): Flow<Int>
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM member WHERE householdId = :householdId AND deletedAt IS NULL ORDER BY displayName")
    fun observeAll(householdId: String): Flow<List<Member>>

    @Query("SELECT * FROM member WHERE householdId = :householdId AND isSelf = 1 AND deletedAt IS NULL LIMIT 1")
    suspend fun self(householdId: String): Member?

    /**
     * Flow olarak da var, cunku uyeyi bootstrap YARATIYOR ve ekran ondan ONCE
     * acilabiliyor. Tek seferlik okuma o yaristas null donuyor ve sonra hic
     * guncellenmiyordu - her ekleme sessizce hicbir sey yapmiyordu.
     */
    @Query("SELECT * FROM member WHERE householdId = :householdId AND isSelf = 1 AND deletedAt IS NULL LIMIT 1")
    fun observeSelf(householdId: String): Flow<Member?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: Member)
}

/**
 * Fis DAO'su. Sema DEGISMIYOR - Receipt zaten kayitli, yalnizca erisim eklendi.
 */
@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(receipt: Receipt)

    @Query("SELECT * FROM receipt WHERE tripId = :tripId AND deletedAt IS NULL ORDER BY capturedAt DESC")
    fun observeForTrip(tripId: String): Flow<List<Receipt>>

    /**
     * OKUNMAYI BEKLEYENLER. Fotograf cekildiginde satir PENDING olarak
     * yaziliyor ve OCR SONRA kosuyor - kullanici kasa kuyrugunda spinner
     * beklemesin diye. Bu sorgu o kuyrugu okuyor.
     */
    @Query("SELECT * FROM receipt WHERE status = 'PENDING' AND deletedAt IS NULL ORDER BY capturedAt")
    suspend fun pending(): List<Receipt>

    @Query("SELECT * FROM receipt WHERE id = :id")
    suspend fun byId(id: String): Receipt?

    /** Gezideki fis sayisi - "bu bir parca mi" sorusunun yarisi (F4.13). */
    @Query("SELECT COUNT(*) FROM receipt WHERE tripId = :tripId AND deletedAt IS NULL")
    suspend fun countForTrip(tripId: String): Int

    /**
     * Gezideki fisler, cekim sirasinda - fiziksel fis gruplamasinin girdisi.
     *
     * `observeForTrip`'in tek seferlik hali: kapi hesabi anlik bir goruntu
     * istiyor, akis degil.
     */
    @Query("SELECT * FROM receipt WHERE tripId = :tripId AND deletedAt IS NULL ORDER BY capturedAt")
    suspend fun forTrip(tripId: String): List<Receipt>

    @Query("UPDATE receipt SET status = :status, errorMessage = :error WHERE id = :id")
    suspend fun setStatus(id: String, status: ReceiptStatus, error: String? = null)

    @Query("UPDATE receipt SET totalMinor = :total, storeNameRaw = :store, extractedAt = :at WHERE id = :id")
    suspend fun setTotal(id: String, total: Long?, store: String?, at: Long)

    @Query("UPDATE receipt SET receiptDate = :date WHERE id = :id")
    suspend fun setReceiptDate(id: String, date: Long?)

    /**
     * Bir gezinin fislerinde OKUNABILMIS toplamlarin toplami.
     *
     * TOPLAM, TEK FISIN KOPYASI DEGIL: uzun fis parca parca cekilebiliyor ve
     * ayni geziye birden fazla fis baglanabiliyor (F4.4 olcumu: ~60 kalem tek
     * kareye sigmiyor). Tek fisi geziye yazmak iki parcali alisverisin yarisini
     * gostermek olurdu.
     *
     * @return kurus, ya da hicbir fisin toplami okunamadiysa null. SUM() bos
     *   kumede zaten NULL doner - 0 DONMEZ, ve bu tam istedigimiz: "bilmiyoruz"
     *   ile "bedava" ayri seyler.
     */
    @Query(
        """
        SELECT SUM(totalMinor) FROM receipt
        WHERE tripId = :tripId AND deletedAt IS NULL AND totalMinor IS NOT NULL
        """,
    )
    suspend fun sumTotalsForTrip(tripId: String): Long?

    /**
     * Gecmis ekraninin besledigi sorgu (F4.9).
     *
     * BASARISIZ FISLER DE DONUYOR: Gecmis, yanlis okunmus bir fise geri
     * donmenin TEK yolu. FAILED satirlari filtrelemek onlari erisilemez
     * yapardi - fotograf diskte durur, kullanici bir daha ulasamaz.
     */
    @Query(
        """
        SELECT * FROM receipt
        WHERE householdId = :householdId AND deletedAt IS NULL
        ORDER BY capturedAt DESC LIMIT :limit
        """,
    )
    fun observeForHousehold(householdId: String, limit: Int = 100): Flow<List<Receipt>>
}

/** Fisten cikan satirlarin DAO'su. Sema degismiyor - ReceiptLine zaten kayitli. */
@Dao
interface ReceiptLineDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(lines: List<ReceiptLine>)

    @Query("SELECT * FROM receipt_line WHERE receiptId = :receiptId AND deletedAt IS NULL ORDER BY createdAt")
    fun observeForReceipt(receiptId: String): Flow<List<ReceiptLine>>

    @Query("SELECT * FROM receipt_line WHERE receiptId = :receiptId AND deletedAt IS NULL ORDER BY createdAt")
    suspend fun forReceipt(receiptId: String): List<ReceiptLine>

    /** Kullanici duzeltmesi: urun baglandi, artik onay beklemiyor. */
    @Query("UPDATE receipt_line SET matchedProductId = :productId, needsReview = 0 WHERE id = :id")
    suspend fun confirmMatch(id: String, productId: String)

    @Query("UPDATE receipt_line SET lineTotalMinor = :amountMinor WHERE id = :id")
    suspend fun setAmount(id: String, amountMinor: Long)

    @Query("DELETE FROM receipt_line WHERE receiptId = :receiptId")
    suspend fun clearForReceipt(receiptId: String)

    /**
     * Fis basina satir ve onay bekleyen satir sayisi (tasarim karari 4).
     *
     * TEK SORGU, fis basina bir sorgu DEGIL: `observeLineCounts` ile ayni
     * gerekce - elli gezilik bir gecmiste N+1 sorgu listeyi acilirken
     * kilitlerdi.
     */
    @Query(
        """
        SELECT rl.receiptId AS receiptId,
               COUNT(*) AS lineCount,
               SUM(CASE WHEN rl.needsReview THEN 1 ELSE 0 END) AS reviewCount
        FROM receipt_line rl
        JOIN receipt r ON r.id = rl.receiptId
        WHERE r.householdId = :householdId AND rl.deletedAt IS NULL AND r.deletedAt IS NULL
        GROUP BY rl.receiptId
        """,
    )
    fun observeLineCounts(householdId: String): Flow<List<ReceiptLineCount>>

    /**
     * VERILEN FISLERIN satirlarinin toplami, kurus (F4.13 duzeltmesi).
     *
     * ARITMETIK DEGISMEZ FIZIKSEL FISE AIT, FOTOGRAFA DEGIL. Uzun fis parca
     * parca cekilince TOPLAM yalnizca SON parcada basili oluyor ama o parca
     * fisin yalnizca bir bolumunun satirlarini tasiyor. Kapiyi tek fotograf
     * kapsaminda hesaplamak son parcada **yapisal olarak** tutmuyor cikariyordu:
     * her uzun fiste, her seferinde amber "tutmuyor" - kullanici hicbir hata
     * yapmadan.
     *
     * KAPSAM GEZI DEGIL, FIZIKSEL FIS: id kumesini `samePhysicalReceipt`
     * veriyor. Gezi kapsami cihazda olculdu ve YANLISTI - bir gezide iki ayri
     * magaza fisi olabiliyor ve onlari toplamak dogru okunmus bir fisi
     * digerinin hatasiyla amber yapiyordu.
     *
     * YAN KAZANC: bindiren cekimi yakaliyor. Kullanici ayni satirlari iki
     * parcada birden cekerse toplam fisin toplamini ASAR ve kapi bunu soyler -
     * bugun hicbir sey soylemiyor.
     *
     * Isaret [ReceiptLine.isDiscount]'tan geliyor, sayidan degil - tutar her
     * zaman pozitif saklaniyor (bkz. `ParsedLine` KDoc).
     *
     * @return kurus, ya da hic satir yoksa null. SUM() bos kumede NULL doner ve
     *   bu istenen: "hic satir yok" ile "toplami sifir" ayri seyler.
     */
    @Query(
        """
        SELECT SUM(CASE WHEN isDiscount THEN -lineTotalMinor ELSE lineTotalMinor END)
        FROM receipt_line
        WHERE receiptId IN (:receiptIds) AND deletedAt IS NULL
        """,
    )
    suspend fun sumLinesForReceipts(receiptIds: List<String>): Long?
}

/** ProductAlias DAO'su - F4.7 ogrenmesi buradan geciyor. */
@Dao
interface ProductAliasDao {
    /**
     * REPLACE, IGNORE DEGIL.
     *
     * (householdId, storeChain, rawTextNormalized) uzerinde tekil index var ve
     * IGNORE ile kullanicinin IKINCI karari sessizce yutuluyordu: bir fis
     * metnini once yanlis urune baglayip sonra duzeltirsen duzeltme yazilmaz,
     * eski alias sonsuza kadar kalir ve her fiste ayni yanlis eslesmeyi
     * uretir - ustelik kullanici duzeltmeyi yapmis oldugu icin bir daha
     * bakmaz. Ogrenen bir sistemde son karar kazanmali.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alias: ProductAlias)

    /**
     * Zincir + ham metin -> urun. F4.7'nin butun degeri bu sorguda: ayni fis
     * metni bir kez duzeltildikten sonra bir daha sorulmuyor.
     */
    @Query(
        """
        SELECT * FROM product_alias
        WHERE householdId = :householdId AND storeChain = :chain
          AND rawTextNormalized = :normalized AND deletedAt IS NULL
        LIMIT 1
        """,
    )
    suspend fun find(householdId: String, chain: String, normalized: String): ProductAlias?
}

/**
 * Store DAO'su - magaza satiri FISTEN DOGUYOR (tasarim karari 11).
 *
 * ELLE MAGAZA EKLEME YOK ve bu bilincli: elle girilen bir magaza fiyat
 * karsilastirmasina hicbir veri katmiyor, karsilastirmayi besleyen sey fisin
 * kendisi. Bu yuzden burada `insert` ve `observeAll` disinda bir sey yok -
 * guncelleme, silme, yeniden adlandirma cagrilari YAZILMADI cunku onlari
 * cagiracak bir yuzey yok.
 */
@Dao
interface StoreDao {
    /**
     * ZINCIR BAZINDA TEKIL. Ayni zincirin ikinci fisi yeni satir DOGURMUYOR:
     * "Migros"tan on fis cekmek Ayarlar'a on satir yazsaydi bolum bir liste
     * degil bir gunluk olurdu.
     */
    @Query(
        """
        SELECT * FROM store
        WHERE householdId = :householdId AND chain = :chain AND deletedAt IS NULL
        LIMIT 1
        """,
    )
    suspend fun findByChain(householdId: String, chain: String): Store?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(store: Store)

    /** Ayarlar'daki Magazalar bolumu. Bos donerse bolum HIC cizilmiyor. */
    @Query(
        """
        SELECT * FROM store
        WHERE householdId = :householdId AND deletedAt IS NULL
        ORDER BY createdAt
        """,
    )
    fun observeAll(householdId: String): Flow<List<Store>>
}
