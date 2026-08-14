package com.neydi.app.ui.liste

import com.neydi.app.data.db.ListeSatiri
import com.neydi.app.ui.components.ListRow
import com.neydi.app.ui.components.turkishInitials

/**
 * Ekranin cizecegi tam sey. Bolumler ZATEN siralanmis ve BOS OLANLAR YOK -
 * SectionHeader'in sozlesmesi bos bolum cizilmemesini istiyor ve bunu
 * cizim aninda kontrol etmek her karede tekrar eder.
 */
data class ListeDurumu(
    val bolumler: List<ListeBolumu> = emptyList(),
    /**
     * "Alindi" bolumu ayri: reyon gruplamasinin disinda, en altta.
     * ALISVERIS MODUNDA HEP BOS - orada isaretli satirlar yerinde kalir.
     */
    val alinanlar: List<UiSatir> = emptyList(),
    val yukleniyor: Boolean = true,
    val alisverisModu: Boolean = false,
    /** Bos durumu hangi metinle cizecegimizi belirler. */
    val bosTur: BosTur = BosTur.ILK_GUN,
) {
    val bosMu: Boolean get() = !yukleniyor && bolumler.isEmpty() && alinanlar.isEmpty()
    val toplamSatir: Int get() = bolumler.sumOf { it.satirlar.size } + alinanlar.size
    val kalanSatir: Int get() = bolumler.sumOf { b -> b.satirlar.count { !it.row.checked } }
}

/**
 * Uc bos durum. Ayni metni ucune de gostermek en kotu secenek: ilk gun
 * "ne yapacagimi bilmiyorum", dongu ortasi ise "uygulama olmus mu" hissi verir.
 */
enum class BosTur {
    /** Hic urun gecmisi yok - ne yapilacagini GOSTERMEK gerekiyor. */
    ILK_GUN,
    /** Gecmiste urun var ama liste su an bos - olu hissettirmemeli. */
    DONGU_ORTASI,
}

data class ListeBolumu(
    val baslik: String,
    val satirlar: List<UiSatir>,
)

/**
 * Ekran satiri: tasarim modeli + KIMLIK.
 *
 * ListRow bilerek kimlik TASIMIYOR - o tasarim sistemine ait, sahte veriyle de
 * calisabilmeli. Isaretleme ve cikarma icin gereken satir kimligi burada
 * tasiniyor; ListRow'a id eklemek tasarim modelini veri modeline baglardi.
 */
data class UiSatir(
    val id: String,
    val row: ListRow,
)

/**
 * Veri satirini ekran satirina cevirir.
 *
 * @param benimUyeId es avatarinin kuralini uygular: avatar YALNIZCA es
 *   ekledigunde cizilir. Kendi ekledigimizde cizmek her satira gurultu ekler
 *   ve hicbir sey soylemez.
 */
internal fun ListeSatiri.uiSatiri(benimUyeId: String?): UiSatir = UiSatir(
    id = satirId,
    row = ListRow(
        name = ad,
        quantity = adetEtiketi(adet, birim),
        checked = isaretli,
        isStaple = sabitMi,
        addedByInitial = if (ekleyenUyeId != benimUyeId) turkishInitials(ad).take(1) else null,
        note = notu,
    ),
)

/**
 * "2x", "1,5 kg", ya da adet 1 ve birim "adet" ise null (rozet cizilmez).
 *
 * Ondalik AYIRICI VIRGUL: Turkce'de 1.5 kg diye yazilmaz. Kotlin'in
 * varsayilan toString'i nokta uretir, o yuzden elle degistiriliyor.
 */
internal fun adetEtiketi(adet: Double, birim: String): String? {
    if (adet == 1.0 && birim == "adet") return null
    val sayi = if (adet % 1.0 == 0.0) {
        adet.toInt().toString()
    } else {
        adet.toString().replace('.', ',')
    }
    return if (birim == "adet") "${sayi}x" else "$sayi $birim"
}

/**
 * Satirlari bolumlere ayirir.
 *
 * ISARETLILER REYONDAN CIKAR: "Alindi" bolumune tasinirlar. Reyon icinde
 * kalsalardi liste alisveris ilerledikce delik desik gorunurdu ve
 * "daha ne kaldi" sorusu gozle cevaplanamazdi.
 *
 * Girdi ZATEN kategori sirasinda geliyor (SQL ORDER BY), o yuzden burada
 * yeniden siralama yok - sadece gruplama.
 */
internal fun List<ListeSatiri>.bolumlere(
    benimUyeId: String?,
    alisverisModu: Boolean = false,
    bosTur: BosTur = BosTur.ILK_GUN,
): ListeDurumu {
    // ALISVERIS MODUNDA REYON SIRASI DONAR. Isaretlenen satir YERINDE kalir,
    // "Alindi"ya inmez. Hareket eden basparmagin altinda yeniden siralama bu
    // ekranin yapabilecegi en kotu hata: kullanici bir sonrakine dokunacakken
    // liste kayar ve yanlis urunu isaretler. Planlamada tasima dogru, reyonda
    // felaket.
    val (alinan, kalan) = if (alisverisModu) emptyList<ListeSatiri>() to this else partition { it.isaretli }

    val bolumler = kalan
        .groupBy { it.kategoriAdi }
        .map { (baslik, satirlar) -> ListeBolumu(baslik, satirlar.map { it.uiSatiri(benimUyeId) }) }
        .filter { it.satirlar.isNotEmpty() }
    return ListeDurumu(
        bolumler = bolumler,
        alinanlar = alinan.map { it.uiSatiri(benimUyeId) },
        yukleniyor = false,
        alisverisModu = alisverisModu,
        bosTur = bosTur,
    )
}
