package com.neydi.app.data.repo

import com.neydi.app.data.db.DataWipeDao

/**
 * Silme ekraninin sayfa sayfa okudugu rakamlar (tasarim karari 2).
 *
 * BIRLESIK SATIRLAR IKI RAKAM TASIYOR ("18 + 12"), tek bir toplam degil.
 * Tasarim bu bicimi kendi cerceve verisinde zaten kullaniyor
 * ("Her zamankiler, önerilmeyenler · 9 + 2"); adi iki seyi sayan bir satirda
 * tek rakam yazmak, gidecek seyin yarisini gizlemek olurdu.
 */
data class WipeCounts(
    val trips: Int = 0,
    val products: Int = 0,
    val prices: Int = 0,
    val staples: Int = 0,
    val blocks: Int = 0,
) {
    /** Hicbir sey yoksa ekran sayilari degil, "silinecek bir sey yok" halini cizer. */
    val isEmpty: Boolean
        get() = trips + products + prices + staples + blocks == 0
}

/**
 * Hanenin verisini siler - VERITABANINDAN VE DISKTEN (tasarim karari 2).
 *
 * FOTOGRAFLAR ONCE SILINIYOR ve bu sira sart: satirlar once gitseydi
 * `imagePath` degerlerini bir daha okuyamazdik ve fotograflar cihazda
 * OKSUZ kalirdi - kullaniciya "fis fotograflari cihazdan da silinir" diye soz
 * verilmisken diskte oldugu gibi durur, ustelik onlari gosterecek hicbir ekran
 * kalmadigi icin kimse fark etmezdi. Gizlilik ekraninda verilen sozun sessizce
 * tutulmamasi, en kotu hata sinifi.
 *
 * ISLEM (transaction) YOK, bilerek: dosya silme veritabani isleminin icine
 * giremez - geri alinamaz. Yarim kalirsa yeniden calistirmak guvenli, cunku her
 * adim idempotent (silinmis satiri silmek, olmayan dosyayi silmek).
 */
class DataWipe(
    private val dao: DataWipeDao,
) {

    suspend fun counts(householdId: String): WipeCounts = WipeCounts(
        trips = dao.countTrips(householdId),
        products = dao.countProducts(householdId),
        prices = dao.countPrices(householdId),
        staples = dao.countStaples(householdId),
        blocks = dao.countBlocks(householdId),
    )

    suspend fun wipe(householdId: String) {

        dao.deleteTripLines(householdId)
        dao.deleteTrips(householdId)
        dao.deletePrices(householdId)
        dao.deleteAliases(householdId)
        dao.deleteStats(householdId)
        dao.deleteSuggestionEvents(householdId)
        dao.deleteSuggestionBlocks(householdId)
        dao.deleteProducts(householdId)
        dao.deleteStores(householdId)
    }
}
