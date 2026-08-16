package com.neydi.app.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Nav3 hedefleri.
 *
 * IA'nin temel kurali: LISTE uygulamanin kendisidir, diger her hedef oraya donen
 * bir sapmadir. Bottom navigation YOK, tab YOK, coklu back stack YOK.
 *
 * Liste ekranindaki uc mod (planlama / alisveris / alisveris sonrasi) BURADA
 * hedef DEGILDIR - ayni ekranin state'idir. Onlari NavKey yaparsan ekran sayisi
 * uce katlanir ve tasarimin en buyuk tasarrufu kaybolur.
 *
 * EKLE ve URUN DETAYI de hedef degil, Liste uzerinde acilan bottom sheet'lerdir.
 * Nav3'te bunlar ileride custom Scene olarak eklenecek (bkz. navigation-3
 * bottomsheet recipe); simdilik ekranin kendi state'inde tutuluyorlar.
 */
@Serializable
sealed interface NeydiKey : NavKey

@Serializable
data object Liste : NeydiKey

@Serializable
data object MissingItems : NeydiKey

/** tripId null ise aktif alisveris kapatiliyor; dolu ise Gecmis'ten okuma/duzenleme modu. */
@Serializable
data class FinishShopping(val tripId: String? = null) : NeydiKey

// Cekim hedefi E6'da oldu; yerine E15'te `TagCapture` geliyor - PARAMETRESIZ,
// cunku etiket cekimi geziden bagimsiz (pivot karari 3).

@Serializable
data object History : NeydiKey

@Serializable
data object Settings : NeydiKey

/**
 * "Verilerimi sil" onayi (tasarim karari 2).
 *
 * DIALOG DEGIL HEDEF olmasi kararin kendisi: v1'in "sifir modal dialog" kurali
 * bozulmadan silme onaya baglaniyor, ve geri tusu dogal bir vazgecme yolu
 * oluyor.
 */
@Serializable
data object DeleteData : NeydiKey

@Serializable
data object Setup : NeydiKey

/**
 * Back stack'in surec olumunden sonra geri yuklenmesi icin gereken serializer kaydi.
 *
 * `rememberNavBackStack`in Android'e ozel bir asiri yuklemesi daha var; o reflection
 * kullanip alt tipleri kendisi buluyor - ama SADECE Android'de. Buradaki
 * SavedStateConfiguration'li asiri yukleme her platformda calisan tek surum.
 *
 * KAYIT UNUTULURSA NE OLUR: `rememberNavBackStack` varsayilan SerializersModule'u
 * `require` ile reddediyor, yani modulu hic vermezsen uygulama ACILIR ACILMAZ
 * IllegalArgumentException ile patliyor - Android dahil her platformda ayni sekilde.
 * Tek bir alt tipi kaydetmeyi unutursan da o hedef back stack'teyken surec olumunden
 * donunce SerializationException aliyorsun. Ikisi de gurultulu; sessiz bozulma yok.
 *
 * (Bu adimin roadmap'teki ilk gerekcesi "iOS'ta sessizce back stack restore olmaz"
 * idi. Yanlisti: navigation3-runtime 1.1.1 kaynagi, RememberNavBackStack.kt:64,
 * kosulsuz `require`. Gerekce yanlis olsa da is gerekliydi - o asiri yukleme
 * olmadan uygulama iOS'ta hic derlenmezdi.)
 */
val NeydiSavedStateConfig: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Liste::class)
            subclass(MissingItems::class)
            subclass(FinishShopping::class)
            subclass(History::class)
            subclass(Settings::class)
            subclass(DeleteData::class)
            subclass(Setup::class)
        }
    }
}

/**
 * BU FONKSIYONUN TEK ISI DERLEMEYI BOZMAK.
 *
 * NeydiKey sealed oldugu icin asagidaki `when` derleyici tarafindan exhaustive
 * kontrol ediliyor. Yeni bir hedef eklendiginde burasi derlenmez ve seni tam da
 * kaydin yapilmasi gereken dosyaya getirir. Kaydi unutmanin bedeli calisma
 * zamaninda bir kilitlenme; o bedeli derleme zamanina cekiyoruz.
 *
 * Dogrulandi: gecici bir `SondaHedef` eklenince derleyici
 * "'when' expression must be exhaustive. Add the 'SondaHedef' branch" diyor.
 *
 * Yeni hedef eklerken: once yukariya `subclass(...)`, sonra buraya bir dal.
 */
@Suppress("unused")
private fun NeydiKey.hasSerializerRegistration(): Unit = when (this) {
    is Liste -> Unit
    is MissingItems -> Unit
    is FinishShopping -> Unit
    is History -> Unit
    is Settings -> Unit
    is DeleteData -> Unit
    is Setup -> Unit
}
