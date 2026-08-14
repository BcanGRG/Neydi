package com.neydi.app.data.db

/**
 * TUM ENTITY'LERDE GECERLI SOZLESME. Yeni tablo eklerken once burayi oku.
 *
 * 1. id = UUID v7 metni.
 *    v4 degil: v7 zaman siralanabilir, yani birincil anahtar index'i sona ekler,
 *    ortasina serpistirmez. Iki cihaz cevrimdisi uretecegi icin auto-increment
 *    zaten mumkun degil.
 *
 * 2. householdId HER KULLANICI VERISI SATIRINDA var.
 *    Supabase'e gecince satir seviyesi guvenlik (RLS) bu alana dayanacak.
 *    Sonradan eklemek her tabloyu ve her sorguyu yeniden yazmak demek.
 *    ISTISNA: referans verisi (Category, CatalogSeed) uygulamayla birlikte
 *    geliyor, senkron olmuyor ve kimseye ait degil - onlarda householdId YOK.
 *    Bu istisna bilincli; kurali sessizce delmiyoruz.
 *
 * 3. GERCEK SILME YOK. deletedAt tombstone.
 *    Iki telefon cevrimdisiyken gercek silme "esimin ekledigi urun kayboldu"
 *    diye tezahur eder ve senkronun uzlastiracagi bir sey kalmaz.
 *
 * 4. PARA = Long, kurus cinsinden. Double DEGIL.
 *    289,00 TL -> 28900. Kayan noktali para toplama hatasi biriktirir ve bu
 *    uygulamanin isi tam olarak fiyat toplamak. Formatlama sunum katmaninda.
 *
 * 5. ZAMAN = Long, epoch millis, UTC.
 *    kotlinx-datetime Instant donusturucusu F2.4'te baglanacak; sema
 *    degismesin diye alan tipi simdiden Long.
 *
 * 6. matchKey = diacritic-folded, locale-acik kucuk harf.
 *    Normalizasyonun kendisi ve testi F2.4. `lowercase()` locale'siz
 *    cagrilirsa noktali/noktasiz I urunu ikiye boler ve fiyat gecmisini yok
 *    eder - bu yuzden alan var ama doldurma kurali ayri adimda kilitleniyor.
 */
internal object Contract
