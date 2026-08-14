package com.neydi.app.data.fis

/**
 * Fis fotografindan HAM METIN SATIRLARI cikaran cihaz ustu OCR.
 *
 * Fotograf TELEFONDAN CIKMIYOR. Bu bir gizlilik tercihi degil, mimari bir
 * gercek: gonderilecek bir yer yok. Fisler kisisel veri ve baslangictan beri
 * oyle ele alindi (`receipts/` gitignore'da); cihazda OCR bunu koda gomuyor.
 *
 * Yan kazanclar: API anahtari yok, ucret yok, ve markette internet olmadan da
 * calisiyor - kasa kuyrugunda yukleme beklemek yok.
 *
 * Bu arayuz YAPI KURMUYOR, sadece metin donuyor. Yapiyi [fisAyristir] kuruyor
 * ve o saf Kotlin oldugu icin cihazsiz test edilebiliyor.
 */
interface FisOkuyucu {
    /**
     * @return fis sirasinda ham metin satirlari.
     *   Basarisizlik ISTISNA DEGIL [Result]: OCR'in okuyamamasi beklenen bir
     *   durum (buruşuk fis, solmus termal kagit) ve cagiran taraf bunu
     *   kullaniciya gostermeli - cokmemeli.
     */
    suspend fun satirlariOku(gorselYolu: String): Result<List<String>>
}
