package com.neydi.app.ui.list

import com.neydi.app.data.daysBetween
import com.neydi.app.data.db.ListRowProjection
import com.neydi.app.data.formatEstimate
import com.neydi.app.data.formatRelativeDay
import com.neydi.app.ui.components.ListRow
import com.neydi.app.data.repo.STAPLE_LIMIT
import com.neydi.app.ui.components.turkishInitials

/**
 * Ekranin cizecegi tam sey. Bolumler ZATEN siralanmis ve BOS OLANLAR YOK -
 * SectionHeader'in sozlesmesi bos bolum cizilmemesini istiyor ve bunu
 * cizim aninda kontrol etmek her karede tekrar eder.
 */
data class ListState(
    val sections: List<ListSection> = emptyList(),
    /**
     * "Alindi" bolumu ayri: reyon gruplamasinin disinda, en altta.
     * ALISVERIS MODUNDA HEP BOS - orada isaretli satirlar yerinde kalir.
     */
    val taken: List<UiRow> = emptyList(),
    val loading: Boolean = true,
    val shoppingMode: Boolean = false,
    /** Bos durumu hangi metinle cizecegimizi belirler. */
    val emptyKind: EmptyKind = EmptyKind.ILK_GUN,
    /** Basligin alt satiri icin son kapanmis gezi; hic yoksa null. */
    val lastTrip: LastTrip? = null,
    /**
     * Basliktaki avatarin bas harfleri (tasarim karari 10).
     *
     * TEK KULLANICILI HANEDE DE CIZILIYOR - kendi bas harfi, nokta gri.
     * Gizlemek, ikinci kisi eklendigi gun basligi yeniden ogretmek olurdu;
     * avatar basligin sabit bir parcasi ve es eklenince yalnizca NOKTA
     * yesile donuyor, yerlesim degismiyor.
     */
    val selfInitials: String? = null,
    /** Hanede baska uye var mi - avatarin varlik noktasinin rengi. */
    val hasPartner: Boolean = false,
) {
    val isEmpty: Boolean get() = !loading && sections.isEmpty() && taken.isEmpty()
    val totalRows: Int get() = sections.sumOf { it.rows.size } + taken.size
    val remainingRow: Int get() = sections.sumOf { b -> b.rows.count { !it.row.checked } }

    /**
     * Alisveris ilerlemesi: kac satir alindi.
     *
     * TEK YERDE HESAPLANIYOR cunku ayni ekran iki yerde gosteriyor - baslik
     * ("12/18 alindi") ve alt cubuk ("Bitir (12/18)"). Alt cubuk bu farki
     * kendi icinde cikariyordu; baslik ise TERS metrigi ("N kaldi") yaziyordu,
     * yani ayni ilerleme iki farkli dille anlatiliyordu.
     *
     * Iki modda da dogru: planlamada isaretli satirlar [taken]'a iner,
     * alisveris modunda bolumlerinde kalir - fark her iki halde de alinan
     * satir sayisini veriyor.
     */
    val takenRows: Int get() = totalRows - remainingRow
}

/**
 * Basligin alt satirindaki son alisveris hatirlatmasi (Ekran 1 tasarimi).
 *
 * @property closedAt gezinin kapandigi an, epoch millis.
 * @property totalMinor gozlemlerden hesaplanan TAHMIN, kurus; `~` ile
 *   gosterilir. E18'e kadar her zaman null - fis toplami E11'de silindi ve
 *   yerine gecen hesap henuz yazilmadi.
 */
data class LastTrip(val closedAt: Long, val totalMinor: Long?)

/**
 * Basligin alt satiri: *"Son alisveris: 8 gun once · 642 TL"*.
 *
 * TASARIM SAYI SAYMIYOR, HATIRLATIYOR. Onceki hali "N urun" idi ve ekranin
 * kendisi zaten o satirlari gosteriyordu - baslik ayni bilgiyi tekrar ediyor,
 * hicbir sey eklemiyordu. Tasarimin sectigi bilgi listede OLMAYAN tek sey:
 * en son ne zaman ve ne kadara alisveris yapildigi.
 *
 * TUTAR OKUNAMADIYSA YAZILMIYOR: "642 TL" yerine "0 TL" ya da "- TL" yazmak
 * dogrulanmamis bir sayiyi manset yapmak olurdu (bkz. F4.11'in ayni karari).
 *
 * @param now cagiran taraftan geliyor ki test deterministik olsun.
 */
internal fun lastTripSummary(lastTrip: LastTrip?, now: Long): String {
    if (lastTrip == null) return "Henüz alışveriş yok"
    // TARIH MERDIVENI ORTAK (F5.11): buradaki uc dallı hal tasarimin alti
    // basamakli merdiveninin bir alt kumesiydi ve iki yer iki farkli cumle
    // uretiyordu. Tek fonksiyon, tek dil.
    val whenText = formatRelativeDay(lastTrip.closedAt, now)
    // TAHMIN BICIMI: uygulamada kesin tutar diye bir veri yok, her tutar
    // gozlemden hesaplaniyor - tilde ve kurussuz (gezinme sozlesmesi).
    val amount = lastTrip.totalMinor?.let { " · ${formatEstimate(it)}" } ?: ""
    return "Son alışveriş: $whenText$amount"
}

/**
 * Uc bos durum. Ayni metni ucune de gostermek en kotu secenek: ilk gun
 * "ne yapacagimi bilmiyorum", dongu ortasi ise "uygulama olmus mu" hissi verir.
 */
enum class EmptyKind {
    /** Hic urun gecmisi yok - ne yapilacagini GOSTERMEK gerekiyor. */
    ILK_GUN,
    /** Gecmiste urun var ama liste su an bos - olu hissettirmemeli. */
    DONGU_ORTASI,
}

/**
 * Sabit bolumunun basligi.
 *
 * Reyon adi DEGIL, o yuzden kategori gruplamasinin disinda: bir sabit hangi
 * reyondan olursa olsun bu bolumde toplaniyor.
 */
const val STAPLE_SECTION_TITLE = "Her zamankiler"

data class ListSection(
    val title: String,
    val rows: List<UiRow>,
)

/**
 * Ekran satiri: tasarim modeli + KIMLIK.
 *
 * ListRow bilerek kimlik TASIMIYOR - o tasarim sistemine ait, sahte veriyle de
 * calisabilmeli. Isaretleme ve cikarma icin gereken satir kimligi burada
 * tasiniyor; ListRow'a id eklemek tasarim modelini veri modeline baglardi.
 */
data class UiRow(
    val id: String,
    /**
     * Urunun kimligi - satirin degil.
     *
     * Satir bir geziye ait, urun haneye. Urun Detayi sheet'i ve
     * "her zamankiler"e ekleme URUNE bakiyor: sabitlik gezi degistiginde de
     * gecerli kalmali. [id] ile karistirilmamali.
     */
    val productId: String,
    val row: ListRow,
)

/**
 * Veri satirini ekran satirina cevirir.
 *
 * @param benimUyeId es avatarinin kuralini uygular: avatar YALNIZCA es
 *   ekledigunde cizilir. Kendi ekledigimizde cizmek her satira gurultu ekler
 *   ve hicbir sey soylemez.
 */
internal fun ListRowProjection.toUiRow(myMemberId: String?, now: Long): UiRow = UiRow(
    id = rowId,
    productId = productId,
    row = ListRow(
        name = name,
        quantity = quantityLabel(count, unit),
        checked = checked,
        isStaple = isStaple,
        addedByInitial = if (addedByMemberId != myMemberId) turkishInitials(name).take(1) else null,
        note = note,
        priceHint = toPriceHint(now),
    ),
)

/**
 * "2x", "1,5 kg", ya da adet 1 ve birim "adet" ise null (rozet cizilmez).
 *
 * Ondalik AYIRICI VIRGUL: Turkce'de 1.5 kg diye yazilmaz. Kotlin'in
 * varsayilan toString'i nokta uretir, o yuzden elle degistiriliyor.
 */
internal fun quantityLabel(count: Double, unit: String): String? {
    if (count == 1.0 && unit == "adet") return null
    val number = if (count % 1.0 == 0.0) {
        count.toInt().toString()
    } else {
        count.toString().replace('.', ',')
    }
    return if (unit == "adet") "${number}x" else "$number $unit"
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
internal fun List<ListRowProjection>.toSections(
    myMemberId: String?,
    shoppingMode: Boolean = false,
    emptyKind: EmptyKind = EmptyKind.ILK_GUN,
    /**
     * Fiyat ipucunun "kac gun once"si icin gecerli an.
     *
     * VARSAYILANI YOK ve bu bilincli: sifir verilseydi butun gozlemler "bugun"
     * gorunurdu ve hicbir sey patlamazdi. Zorunlu parametre, cagirani saati
     * vermeye mecbur ediyor.
     */
    now: Long,
): ListState {
    // ALISVERIS MODUNDA REYON SIRASI DONAR. Isaretlenen satir YERINDE kalir,
    // "Alindi"ya inmez. Hareket eden basparmagin altinda yeniden siralama bu
    // ekranin yapabilecegi en kotu hata: kullanici bir sonrakine dokunacakken
    // liste kayar ve yanlis urunu isaretler. Planlamada tasima dogru, reyonda
    // felaket.
    val (alinan, remaining) = if (shoppingMode) emptyList<ListRowProjection>() to this else partition { it.checked }

    // "HER ZAMANKILER" EN USTE, VE YALNIZCA PLANLAMA MODUNDA (F6.8).
    //
    // Tasarim maketlerinde bolum planlama modunda en ustte, sayisiyla ve satir
    // basina raptiyeyle duruyor; ALISVERIS MODU MAKETINDE HIC YOK. Sebebi
    // ayni ekranin iki modunun farkli isi: reyonda sira DONUYOR ve sabit bir
    // urun de sonucta bir reyondan alinacak, yani onu listenin basina cekmek
    // market yuruyusunu bozardi. Planlamada ise "her zaman aldiklarimiz"
    // ayri durmasi gereken bir kume.
    val (staples, others) = if (shoppingMode) {
        emptyList<ListRowProjection>() to remaining
    } else {
        remaining.partition { it.isStaple }
    }

    val categorySections = others
        .groupBy { it.categoryName }
        .map { (title, rows) -> ListSection(title, rows.map { it.toUiRow(myMemberId, now) }) }
        .filter { it.rows.isNotEmpty() }

    // Sinir tasarimdan: en fazla 12 satir. Ustu, listeyi acan kullaniciya kendi
    // yazmadigi 20 satir gostermek olurdu.
    val stapleSection = staples
        .take(STAPLE_LIMIT)
        .map { it.toUiRow(myMemberId, now) }
        .takeIf { it.isNotEmpty() }
        ?.let { ListSection(STAPLE_SECTION_TITLE, it) }

    val sections = listOfNotNull(stapleSection) + categorySections
    return ListState(
        sections = sections,
        taken = alinan.map { it.toUiRow(myMemberId, now) },
        loading = false,
        shoppingMode = shoppingMode,
        emptyKind = emptyKind,
    )
}
