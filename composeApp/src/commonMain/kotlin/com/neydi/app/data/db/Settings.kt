package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Hane ayarlari - hane basina TEK satir.
 *
 * NEDEN VAR: semada "kurulum kostu mu" sorusunu cevaplayacak **hicbir sey
 * yoktu**. `Household` yalnizca id/ad/tarih tasiyor, `Member` yalnizca
 * ad/isSelf, ve bir tercihler entity'si hic yoktu. Daha kotusu `bootstrap`
 * **her acilista** haneyi kosulsuz yaratiyor, yani "hane yoksa Kurulum'u
 * goster" **hicbir zaman dogru olamiyordu**.
 *
 * NEDEN AYRI TABLO, `Household`'a kolon DEGIL: bunlar cihaz/hane tercihi,
 * `Household` ise senkron edilen kok agregat. Ayrica F6.5'in engelleme
 * gorunurlugu, F6.6'nin kurulum bayragi ve F6.7'nin gizlilik anahtarlari
 * **ayni** satirda yasayabiliyor - uc ayri sema bump'i yerine bir tane.
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val householdId: String,

    /**
     * Kurulum (Ekran 8) tamamlandiginda damgalaniyor. Null = hic kosmadi.
     *
     * Ucuncu bos durumun ("kurulum atlandi") ayrimi da buradan geliyor:
     * `EmptyKind` bugun iki girdi tasiyor ama kendi KDoc'u **uc** bos durum
     * diyor ve F3.6 ucuncusunu bu adima ertelemisti.
     */
    val setupCompletedAt: Long? = null,

    /**
     * Bu haneye YAZILMIS gomulu katalog surumu (F2.7).
     *
     * Tohumlayici eskiden `SELECT COUNT(*) FROM category > 0` ile kapida
     * duruyordu, yani ilk acilistan sonra `CatalogSeedData`daki hicbir
     * duzeltme o telefona ULASMIYORDU - yeni bir urun, duzeltilmis bir
     * kategori ya da degismis bir `matchKey` kurali yalnizca uygulamayi
     * silip yeniden kuranlara gidiyordu. Sessiz, cunku hicbir sey patlamiyor.
     *
     * `null` = bilinmiyor (v6 oncesi kurulum) ve **yeniden yaz** demek:
     * eski cihazlarin katalogu ilk acilista tazeleniyor.
     */
    val catalogSeedVersion: Int? = null,

    /**
     * Kullanicinin beyan ettigi alisveris temposu, gun.
     *
     * `medianIntervalDays` icin **gercek veri gelmeden onceki tek oncul** - ve
     * `muAdjust` disinda semanin eksik olan en onemli soguk-baslangic girdisi.
     * Kurulum'daki tempo cipinden geliyor (Haftada 1 / 10 gunde bir / 2 haftada
     * bir / Belirsiz). Null = "Belirsiz" ya da hic sorulmadi.
     */
    val tempoDays: Int? = null,

    /**
     * Cekilen fotograflar senkron edilsin mi. **VARSAYILAN HAYIR ve bu bir
     * karar** - kolonun burada olmasi o kararin YAZILI olmasini sagliyor,
     * yoksa gizlilik ozelligi ilk depolama baglamasinda sessizce tersine
     * donerdi.
     *
     * PIVOT SONRASI DURUM: bugun senkron edilecek bir fotograf YOK. Fis
     * gorselleri E11'de tablosuyla birlikte gitti; etiket fotografi da kayittan
     * hemen sonra siliniyor (E15) - etiket bir odeme kaniti degil, yalnizca
     * fiyatin okundugu andir. Kolon yine de duruyor: karari tasiyor ve Faz
     * 7'de senkron kapsamini konusurken tek referans o olacak.
     */
    val syncPhotos: Boolean = false,

    val createdAt: Long,
    val updatedAt: Long? = null,
)
