package com.neydi.app.data.db

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Magaza. chain ve name AYRI: "Migros" zincir, "Migros Bahcelievler" sube.
 * Fiyat karsilastirmasi zincir bazinda anlamli, alias eslemesi de oyle.
 */
@Entity(tableName = "store")
data class Store(
    @PrimaryKey val id: String,
    val householdId: String,
    val name: String,
    /** Normalize zincir adi. ProductAlias.storeChain ile AYNI sozlugu kullanir. */
    val chain: String,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
    val deletedAt: Long? = null,
)

/**
 * Bir alisveris. completedAt null ise AKTIF.
 *
 * Ayni anda birden fazla aktif trip olmamali ama bunu SEMA zorlamıyor -
 * kismi unique index (WHERE completedAt IS NULL) gerekir ve o F2.3'un isi.
 *
 * storeId null olabilir: alisverise cikarken nereye gidilecegi her zaman
 * belli degil, fis okununca doluyor.
 */
@Entity(
    tableName = "trip",
    indices = [
        // "Ayni anda tek aktif trip" KISMI bir unique index gerektirir
        // (WHERE completedAt IS NULL) ve Room'un @Index'i kismi index ifade
        // EDEMIYOR. Sema bunu zorlamiyor; kural repository katmaninda (F2.6).
        // Burada yalnizca sorgu indexi var.
        Index(value = ["householdId", "completedAt"]),
    ],
)
data class Trip(
    @PrimaryKey val id: String,
    val householdId: String,
    val storeId: String? = null,
    val startedAt: Long,
    /**
     * Gezinin asamasi.
     *
     * KAPALILIGIN OTORITESI BU DEGIL, `completedAt`. Sorgular "acik mi" diye
     * `completedAt IS NULL` soruyor; `status` yalnizca acik bir gezinin
     * PLANNING mi SHOPPING mi oldugunu ayirir. Boyle kurulmasinin iki sebebi
     * var: (1) surum 1 satirlari geri-doldurma GEREKTIRMIYOR, cunku
     * completedAt onlarda zaten dogru; (2) tek bir alan kapaliligi tanimladigi
     * icin iki alanin birbirine dusme ihtimali yok.
     *
     * Degismez kural yine gecerli - `status == CLOSED` <=> `completedAt != null`
     * - ve onu tek bir yer koruyor: [TripDao.closeIfOpen] ikisini AYNI
     * ifadede yaziyor.
     */
    @ColumnInfo(defaultValue = "PLANNING")
    val status: TripStatus = TripStatus.PLANNING,
    /**
     * Geziyi KAPATAN uye. Kapanana kadar null.
     *
     * NEDEN VAR: mutabakati tek cihaz yapmali. Iki cihaz ayni geziyi
     * kapatirsa satin almalar CIFT sayilir - ve o sayi oneri motorunun
     * `medianIntervalDays` hesabina giriyor, yani her aralik tahmini yariya
     * duser ve uygulama her seyi iki kat sik onermeye baslar. Sessiz, yavas
     * ve geri alinmasi zor bir bozulma.
     */
    val ownerMemberId: String? = null,
    val completedAt: Long? = null,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
    val deletedAt: Long? = null,
)

/**
 * Listedeki bir satir.
 *
 * F2.3: UNIQUE(tripId, productId). Kritik: iki es de ekmek eklerse istatistik
 * katmani TEK satin alma gormeli. Iki satir olursa medianIntervalDays yariya
 * duser ve uygulama ekmegi iki kat sik onermeye baslar.
 *
 * checked ve checkedAt birlikte: yalnizca bayrak tutulursa "reyonda mi
 * isaretlendi yoksa evde mi" ayrimi kaybolur, o da alisveris suresi
 * istatistigini imkansiz kilar.
 */
@Entity(
    tableName = "trip_line",
    indices = [
        // ISTATISTIK KATMANININ DAYANDIGI KISIT. Iki es de ekmek eklerse
        // veritabani TEK satir tutmali: iki satir olursa ProductStats iki ayri
        // satin alma sayar, medianIntervalDays yariya duser ve uygulama ekmegi
        // iki kat sik onermeye baslar. Yani bu kisit olmadan oneri motoru
        // sessizce yanlis calisir - hata vermez, sadece rahatsiz eder.
        Index(value = ["tripId", "productId"], unique = true),
        // Aktif alisverisin satirlarini cekmek en sik sorgu.
        Index(value = ["tripId"]),
        Index(value = ["productId"]),
    ],
)
data class TripLine(
    @PrimaryKey val id: String,
    val householdId: String,
    val tripId: String,
    val productId: String,
    /** Adet ya da miktar. 1.5 kg mumkun oldugu icin Double; para DEGIL, kural gecerli degil. */
    val quantity: Double = 1.0,
    val unit: String,
    val checked: Boolean = false,
    val checkedAt: Long? = null,
    /** Kim ekledi - satirdaki es avatari bundan cikiyor. */
    val addedByMemberId: String,
    /** Oneriden mi geldi. SuggestionEvent ile birlikte oneri isabetini olcer. */
    val fromSuggestion: Boolean = false,
    val note: String? = null,
    /**
     * ALINMADIYSA NEDEN - oneri motorunun bekledigi ayrim (F6.2).
     *
     * `checked` tek basina yetmiyor: kapanista iyimser mutabakat (F4.8)
     * isaretlenmemis her satiri alindi yaziyor, yani kapali bir gezide
     * `checked = 0` ancak kullanici Bitir ekranindan **geri aldiysa** kaliyor.
     * Ama o geri alma iki farkli sey olabilir ve **ayni puani almamalari SART**:
     * "gerekmedi" oneriyi **bastirmali**, "unuttum" **yukseltmeli**. Tek bir
     * boolean ikisini ayni sey yapiyordu.
     *
     * NULL = kullanici bir sey soylemedi (yaygin durum: dokunmadi, iyimser
     * mutabakat alindi yazdi). Kolondan **once** kapanmis geziler sonsuza kadar
     * null kalir - commonMain'de `execSQL` olmadigi icin geri-doldurma
     * imkansiz, o yuzden nullable olmak zorunda.
     */
    val takeOutcome: TakeOutcome? = null,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
    val deletedAt: Long? = null,
)

/**
 * Bir planli satirin akibeti (F4.12 / F6.2).
 *
 * Tasarim Bitir ekraninda satir basina uc dugme istiyor: `[Aldim] [Gerekmedi]
 * [Unuttum]`. Uclunun ayri olmasi bir UI tercihi degil, skor formulunun girdisi.
 */
enum class TakeOutcome {
    /** Alindi. `checked = 1` ile birlikte anlamli. */
    TAKEN,

    /** Gerekmedi - bilincli olarak alinmadi. Oneriyi BASTIRIR. */
    NOT_NEEDED,

    /** Unutuldu - alinmasi gerekiyordu. Oneriyi YUKSELTIR. */
    FORGOTTEN,
}

/**
 * Gezinin yasam dongusu: PLANNING -> SHOPPING -> CLOSED.
 *
 * PLANNING ve SHOPPING arasinda ILERI GERI gidilebilir - kullanici alisveris
 * modundan cikabilir. CLOSED ise TERMINAL: kapanmis gezi yeniden acilmaz,
 * cunku kapanis mutabakati fiyat gozlemleri ve satin alma sayaclari yaziyor;
 * yeniden acmak onlari ikinci kez yazma riski demek.
 */
enum class TripStatus {
    /** Liste yapiliyor, henuz markete gidilmedi. */
    PLANNING,

    /** Marketteyiz; satirlar isaretleniyor. */
    SHOPPING,

    /** Mutabakat yapildi, gezi kapandi. Terminal. */
    CLOSED,
}
