package com.neydi.app.data.db

/**
 * Mukerrer gozlem penceresi: **60 saniye** (tasarim: etiket akisi sozlesmesi).
 *
 * Tasarimin ifadesi: *"Ayni market + urun + fiyat 60 sn icinde tekrarlanirsa
 * ikinci gozlem yazilmaz."*
 *
 * NEDEN BIR PENCERE, "hep engelle" DEGIL: ayni urunun ayni markette ayni fiyati
 * iki hafta sonra tekrar gorulmesi GERCEK bir gozlem - fiyatin degismedigini
 * soyluyor ve `medianIntervalDays` ile fiyat gecmisi ondan besleniyor. Kalici
 * tekillestirme o bilgiyi silerdi.
 *
 * NEDEN 60 SANIYE: korunan sey kullanicinin kendi tekrari - deklansore iki kez
 * basmak, kaydettim mi diye emin olamayip tekrar cekmek, ya da rafta bir adim
 * geri gidip ayni etikete yeniden bakmak. Bunlarin hepsi saniyeler icinde olur.
 */
const val DUPLICATE_WINDOW_MS = 60_000L

/**
 * Gozlemi yazar - AYNISI az once yazilmadiysa.
 *
 * @return yazildiysa `true`, mukerrer diye atlandiysa `false`. Cagiran taraf bu
 *   ayrimi gormek zorunda: atlanan bir cekimde de toast cikmali (kullanici
 *   deklansore basti, bir sey olmali) ama "yeni gozlem" demek yanlis olurdu.
 *
 * NEDEN DAO'DA DEGIL DE BURADA: Room `@Query` sart kosuyor, kosullu yazma
 * degil. `INSERT ... WHERE NOT EXISTS` ile tek SQL'e indirmek mumkun ama o
 * zaman "yazildi mi" bilgisini geri almak icin ikinci bir sorgu gerekirdi -
 * ve bu yol, kuralin NEDENINI okunabilir bir yerde tutuyor.
 *
 * YARIS KOSULU YOK ve olmasinin sebebi kod degil kullanim: cekim sirasi tek
 * kullanicili ve seri, gezinme sozlesmesi *"onceki kart kapanmadan kamera
 * calismaz"* diyor. Iki cihaz ayni anda ayni etiketi cekerse iki gozlem yazilir;
 * bu bir hata degil - iki kisi gercekten iki kez gormus demektir ve senkron
 * bunlari LWW ile degil, ayri satirlar olarak tasir.
 */
suspend fun PriceObservationDao.insertUnlessRecentDuplicate(
    observation: PriceObservation,
    windowMs: Long = DUPLICATE_WINDOW_MS,
): Boolean {
    val duplicates = countRecentDuplicates(
        householdId = observation.householdId,
        productId = observation.productId,
        storeId = observation.storeId,
        unitPriceMinor = observation.unitPriceMinor,
        since = observation.observedAt - windowMs,
    )
    if (duplicates > 0) return false
    insert(observation)
    return true
}
