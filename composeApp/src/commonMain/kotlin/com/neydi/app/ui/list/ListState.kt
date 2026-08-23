package com.neydi.app.ui.list

import com.neydi.app.data.EstimateLine
import com.neydi.app.data.packIsMissing
import com.neydi.app.data.possessiveSuffix
import com.neydi.app.data.turkishDative
import com.neydi.app.data.turkishLocative
import com.neydi.app.data.quantityLabel

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
     *
     * ALISVERIS MODUNDA HEP BOS (karar 124) - orada isaretli satirlar
     * yerinde kaliyor ve blogun yerini genisleMEYEN bir sayac aliyor.
     * Doluysa mod alisveris SONRASI demektir; bolum kapali aciliyor.
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
    /**
     * Hedef market beyani (karar 117). `null` = hedef yok ve baslik
     * [lastTripSummary]'yi yaziyor.
     */
    val declaration: StoreDeclaration? = null,
    /**
     * Hedefin KIMLIGI - secicinin hangi cipi isaretli cizecegi (karar 117).
     *
     * [declaration] yalnizca METIN tasiyor ve secici metinle eslesemez: iki
     * zincir ayni adi tasiyabilir, ustelik hedef silinmis bir markete isaret
     * ediyorsa beyan hic kurulmuyor ama kimlik hala gecerli.
     */
    val targetStoreId: String? = null,
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
 * Basligin alt satiri, HEDEF MARKET seciliyken: *"BIM'e gidiyorsun · 2'si
 * A101'de"* (karar 117).
 *
 * Iki parca AYRI tutuluyor cunku iki farkli tipografi tasiyorlar: hedef
 * 14sp/600 `onSurface`, sapma 14sp/400 `onSurfaceVariant`. Tek dizgi
 * dondurseydik cizim tarafi onu ayirmak icin ayirici karakteri aramak
 * zorunda kalirdi - market adinda bir "·" gecmesi yeterdi.
 *
 * @property target hep dolu - beyan varsa hedef vardir.
 * @property deviation sapan satir yoksa `null` ve ayirici da cizilmiyor.
 */
data class StoreDeclaration(
    val target: String,
    val deviation: String?,
)

/**
 * Beyan cumlesini kurar; hedef yoksa `null` doner ve baslik
 * [lastTripSummary]'ye geri duser.
 *
 * ## Cumle neden "gidiyorsun", "gidiyorum" degil
 *
 * Maketin metni birebir bu. Uygulama kullaniciya SESLENIYOR; birinci sahis
 * ("gidiyorum") uygulamayi kullanicinin yerine konusturmus olurdu ve ayni
 * baslikta "Son alisveris: dun" gibi tarafsiz bir cumleyle yan yana durunca
 * ses tutarsiz olurdu.
 *
 * ## Birden fazla sapan zincir
 *
 * ⚠ MAKETTE YOK: tasarim yalnizca tek zincirli ornegi ciziyor
 * (*"2'si A101'de"*). Iki zincire sapilmis bir listede secenekler
 * "2'si A101'de · 1'i SOK'ta" (baslik butcesini asiyor) ya da en buyuk
 * zinciri yazip otekileri SESSIZCE dusurmek (satir sayisini yalanlar) idi.
 * Ucuncusu secildi: zincir adi birakilir, sayi DOGRU kalir.
 *
 * Bu bir kod karari ve tasarima bildirildi (`docs/38`).
 *
 * @param targetName gezinin hedef marketinin gorunen adi; `null` = "Belli degil".
 * @param deviantStoreNames hedeften SAPAN satirlarin market adlari, satir
 *   basina bir giris (yani tekrarli).
 */
internal fun storeDeclaration(
    targetName: String?,
    deviantStoreNames: List<String>,
): StoreDeclaration? {
    if (targetName.isNullOrBlank()) return null
    val target = "${turkishDative(targetName)} gidiyorsun"
    if (deviantStoreNames.isEmpty()) return StoreDeclaration(target, null)

    val count = deviantStoreNames.size
    val suffix = possessiveSuffix(count)
    val head = if (suffix.isEmpty()) "$count" else "$count'$suffix"
    val chains = deviantStoreNames.distinct()
    val where = if (chains.size == 1) turkishLocative(chains.single()) else "başka marketlerde"
    return StoreDeclaration(target, "$head $where")
}

/**
 * Bu satir hedeften SAPIYOR mu (karar 117-118).
 *
 * ## Uc sart, ucu de ayri sebeple
 *
 * 1. **Hedef var** - "istisna"nin istisna olabilmesi icin bir kural gerekiyor.
 *    Hedef "Belli degil"ken satirin markete dair bir IDDIASI yok; tasarimin
 *    cumlesi de bunu soyluyor: *"hedef de yoksa liste hic bolunmuyor"*.
 *    ⚠ Satirin verisi SILINMIYOR - hedef geri secilince isaret geri geliyor.
 * 2. **Satirin marketi var** - `null` zaten "hedefi izliyor" demek.
 * 3. **Hedefin KENDISI degil** - "BIM'e gidiyorum" derken bir satira da "BIM"
 *    demek sapma degil, hedefin tekrari.
 * 4. **Adi biliniyor** - market silinmisse satiri reyonundan cikarip basligi
 *    olmayan bir bolume koymak, onu bulunamaz yapardi.
 *
 * ## Neden tek fonksiyon
 *
 * Ayni soru DORT yerde soruluyor: satirin isareti, alisveristeki bolumleme,
 * beyan cumlesinin sayisi, ve karar 126'dan beri Urun Detayi'ndaki *"Nereden
 * alinacak"* satiri. Dordu ayri yazilsaydi biri otekinden ayrilir ve cumle
 * "2'si A101'de" derken listede uc satir isaretli gorunurdu.
 *
 * KIMLIKLE, ADLA DEGIL: iki zincir ayni adi tasiyabilir ve ad kullanicinin
 * duzenledigi bir alan.
 */
internal fun ListRowProjection.deviatesFrom(targetStoreId: String?): Boolean =
    deviantStoreName(targetStoreId, storeId, storeName) != null

/**
 * *"Nereden alinacak"* izgarasinin acilis anindaki hali (karar 126).
 *
 * @property rowId istisnanin yazilacagi satir.
 * @property selectedId o an isaretli cip - istisna varsa o, yoksa HEDEF.
 */
data class LineStorePick(val rowId: String, val selectedId: String?)

/**
 * Sapmanin CIZILECEK ADI, sapma yoksa `null` (karar 117-118).
 *
 * [deviatesFrom]'un govdesi. Ayri durmasinin tek sebebi Urun Detayi:
 * o yuzey bir [ListRowProjection] gormuyor - elinde gezinin hedefi ve
 * satirin kendi `storeId`'si var - ama AYNI soruyu soruyor. Kurali orada
 * yeniden yazmak, karar 126'nin satirini bir gun listenin isaretiyle
 * celisir hale getirirdi: sheet "A101" derken satir hicbir sey cizmezdi.
 *
 * Dort sart, dordu de [deviatesFrom]'un KDoc'unda gerekcesiyle yazili.
 */
internal fun deviantStoreName(
    targetStoreId: String?,
    storeId: String?,
    storeName: String?,
): String? = storeName?.takeIf {
    targetStoreId != null && storeId != null && storeId != targetStoreId
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
    /**
     * Bu bir MARKET bolumu mu - reyon degil (karar 118).
     *
     * Alisveris modunda hedeften sapan satirlar reyonlarindan cikip zincir
     * basina tek bolumde topluyor ve o bolumun basligi storefront ikonu
     * tasiyor. Bayrak basligin cizimini degistiriyor, siralamayi degil.
     */
    val isStore: Boolean = false,
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
 * @param cheaper "A101'de 36,00" - listenin ilk uc adayindan biriyse dolu.
 *   Doluysa trend BASTIRILIYOR (karar 41).
 * @param targetStoreId gezinin hedef marketi. Satirin marketi BUNDAN farkliysa
 *   sapma isareti ciziliyor; ayni ise satir yalnizca hedefi tekrar ediyor ve
 *   isaret cizmek gurultu olurdu.
 */
internal fun ListRowProjection.toUiRow(
    myMemberId: String?,
    now: Long,
    cheaper: String? = null,
    targetStoreId: String? = null,
): UiRow = UiRow(
    id = rowId,
    productId = productId,
    row = ListRow(
        name = name,
        quantity = quantityLabel(count, unit),
        quantityModified = isQuantityModified(count, unitOverride),
        checked = checked,
        isStaple = isStaple,
        addedByInitial = if (addedByMemberId != myMemberId) turkishInitials(name).take(1) else null,
        note = note,
        priceHint = toPriceHint(now, chipWins = cheaper != null),
        // AYNI KURAL, IKI YERDE OKUNUYOR: tahmin toplarken de satir cizerken
        // de `packIsMissing`. Ikisi ayri yazilsaydi biri digerinden ayrilir ve
        // satir "ambalaj bilinmiyor" derken toplam onu yine sayabilirdi.
        packUnknown = packIsMissing(
            EstimateLine(
                quantity = count,
                unit = unit,
                unitPriceMinor = lastPriceMinor,
                packSize = lastPackSize,
                packUnit = lastPackUnit,
            ),
        ),
        cheaperElsewhere = cheaper,
        // TEK KURAL, UC OKUMA (bkz. `deviatesFrom`): isaret, bolumleme ve
        // beyan cumlesinin sayisi ayni soruyu soruyor.
        deviantStore = storeName.takeIf { deviatesFrom(targetStoreId) },
    ),
)

/**
 * Rozet DOLGULU mu cizilecek - yani kullanici bu satirin miktarina dokundu mu
 * (karar 107).
 *
 * ## Neden birim de sayiliyor
 *
 * Once yalnizca `count != 1.0`di ve karar 108'e kadar yetiyordu: miktari
 * degistirmenin tek yolu sayiyi degistirmekti. Birim de secilebilir olunca
 * "1 kg Domates"i "1 adet Domates" yapan biri, sayiya dokunmadigi icin
 * KONTURLU bir rozet gorurdu - oysa o satirin miktarini gercekten kendisi
 * secmis olurdu.
 *
 * `unitOverride`a bakiliyor, `unit != katalogVarsayilani`ya DEGIL: katalog
 * `INSERT OR REPLACE` ile yenileniyor ve karsilastirma yapsaydik, katalogun
 * varsayilani degistigi gun kullanicinin secimi sessizce "varsayilan"a donerdi.
 */
internal fun isQuantityModified(count: Double, unitOverride: String?): Boolean =
    count != 1.0 || unitOverride != null

/**
 * Sheet'in "N urun eklendi" sayacinin bu eklemeden ne kadar artacagi.
 *
 * ## Neden saf bir fonksiyon
 *
 * Sayacin tek isi kullaniciya YALAN SOYLEMEMEK: sheet acikken liste
 * gorunmuyor, yani rakamin dogrulugunu kontrol edecek baska bir sey yok.
 * Karar 109 ikinci eklemeyi sessiz yapinca sayacin da susmasi gerekti -
 * yoksa uc kez ayni urune dokunan biri *"3 ürün eklendi"* okurdu.
 *
 * Iki kosul da gerekli: sheet kapaliyken sayacin anlami yok (liste zaten
 * gorunuyor), ve eklenmemis bir satir sayilmamali.
 */
internal fun sheetAddedDelta(sheetOpen: Boolean, wasNew: Boolean): Int =
    if (sheetOpen && wasNew) 1 else 0

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
    /** Gezinin hedef marketi (karar 117); `null` = "Belli degil". */
    targetStoreId: String? = null,
    /** [targetStoreId]'nin gorunen adi - beyan cumlesi bunu yaziyor. */
    targetStoreName: String? = null,
): ListState {
    // PLANLAMADA "ALINDI" BOLUMU YOK (karar 116).
    //
    // Once tam tersiydi: planlamada isaretli satirlar "Alindi"ya iniyordu,
    // alisveriste yerinde kaliyordu. Kullanicinin sikayeti bunun ta kendisiydi:
    // *"liste yaparken neden alindi/alinmadi var ki? Zaten alisverise cikiyorum
    // dediğimde isaretleme yapiyorum."*
    //
    // Ve bedeli olculdu: cihaz testlerinde YANLISLIKLA yapilan her dokunus bir
    // satiri "Alindi"ya tasidi. Planlamada satirin TAMAMI isaretleme hedefiydi -
    // yani ekranin en buyuk hedefi, en az istenen ise bagliydi.
    //
    // ALISVERIS MODUNDA REYON SIRASI YINE DONUYOR: isaretlenen satir yerinde
    // kalir. Hareket eden basparmagin altinda yeniden siralama bu ekranin
    // yapabilecegi en kotu hata - kullanici bir sonrakine dokunacakken liste
    // kayar ve yanlis urunu isaretler.
    //
    // AMA SONRASINDA INIYOR (karar 124).
    //
    // Bir tur boyunca kod bolumu UC modda birden kaldirmisti ve `docs/38` S4
    // bunu tasarima sordu. Cevap ikisini ayirdi ve ayrimin ekseni PARMAK:
    //
    // - **Alisveriste** basparmak isaretliyor, yani satirin oynamasi yanlis
    //   urunu isaretletir. Satir yerinde kalir; listenin sonunda yalnizca bir
    //   SAYAC durur ("Alindi . 12/18", chevron yok).
    // - **Sonrasinda** isaretlenecek bir sey kalmadi. Alinanlar artik
    //   yapilacak is degil KAYIT ve listenin isi geriye kalani gostermek -
    //   bolum kapali olarak en altta topluyor.
    //
    // UCUNCU MOD SAKLANMIYOR, TURETILIYOR: planlamada isaretlenecek bir sey
    // YOK (karar 116 onay dairesini ve satirin onay hedefini kaldirdi), yani
    // "isaretli satir var" cumlesi zaten "alisveris yapildi" demek. Ucuncu
    // bir bayrak, ayni olguyu ikinci kez saklamak olurdu.
    val (alinan, remaining) = if (shoppingMode) {
        emptyList<ListRowProjection>() to this
    } else {
        partition { it.checked }
    }

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

    // ALISVERISTE SAPANLAR REYONDAN CIKIYOR (karar 118).
    //
    // Gerekcesi reyon sirasinin ne ise yaradigi: liste "bu markette hangi
    // sirayla yuruyeceksin" diyor. Baska bir marketten alinacak satir o
    // yuruyusun icinde durursa, kullanici onu her reyonda arar ve bulamaz.
    //
    // PLANLAMADA AYRILMIYORLAR: orada liste kuruluyor ve satirin hangi
    // reyondan geldigi hala en yararli gruplama. Isaret (karar 118'in ilk
    // yarisi) planlamada satirin uzerinde zaten duruyor.
    val (deviant, onTarget) = if (shoppingMode) {
        others.partition { it.deviatesFrom(targetStoreId) }
    } else {
        emptyList<ListRowProjection>() to others
    }

    // "BASKA MARKETTE UCUZ" ADAYLARI, ALINMAMIS SATIRLAR UZERINDEN (karar 41).
    //
    // Isaretli satir kapsam disi ve bu, "en fazla 3" sinirinin nereye
    // harcandigi meselesi: cip bir EYLEM cagrisi ("oraya ugra"), sepete
    // girmis urunde ise eylem bitmis. Uc kontenjani bitmis islere harcamak,
    // hala alinacak ucuz alternatifi olan satiri sessiz birakirdi.
    //
    // Harita BOLUMLENMEDEN ONCE kuruluyor: sinir liste geneline ait, bolume
    // degil. Bolum basina uygulansaydi alti bolumlu bir liste on sekiz cip
    // cizerdi.
    val chips = remaining.cheaperChips()

    val categorySections = onTarget
        .groupBy { it.categoryName }
        .map { (title, rows) ->
            ListSection(
                title,
                rows.map { it.toUiRow(myMemberId, now, chips[it.rowId], targetStoreId) },
            )
        }
        .filter { it.rows.isNotEmpty() }

    // Sinir tasarimdan: en fazla 12 satir. Ustu, listeyi acan kullaniciya kendi
    // yazmadigi 20 satir gostermek olurdu.
    val stapleSection = staples
        .take(STAPLE_LIMIT)
        .map { it.toUiRow(myMemberId, now, chips[it.rowId], targetStoreId) }
        .takeIf { it.isNotEmpty() }
        ?.let { ListSection(STAPLE_SECTION_TITLE, it) }

    // ZINCIR BASINA BIR BOLUM, hepsi tek bolumde DEGIL: baslik zincirin adini
    // yaziyor ("A101'de") ve iki zinciri tek baslik altinda toplamak o adi
    // yalan yapardi.
    //
    // SIRALAMA ADA GORE: reyon sirasi gibi bir "dogru sira" yok ve eklenme
    // sirasi listeyi her yeni satirda yeniden dizerdi.
    val storeSections = deviant
        .groupBy { it.storeName!! }
        .toSortedMap()
        .map { (store, rows) ->
            ListSection(
                title = turkishLocative(store),
                rows = rows.map { it.toUiRow(myMemberId, now, chips[it.rowId], targetStoreId) },
                isStore = true,
            )
        }

    // MARKET BOLUMLERI EN ALTTA: reyon sirasi bu marketin yuruyusu ve onu
    // baska bir marketin satirlariyla bolmek yuruyusu bozardi.
    val sections = listOfNotNull(stapleSection) + categorySections + storeSections

    // BEYAN SAPAN SATIRLARI SAYIYOR, BOLUMLERI DEGIL.
    //
    // `sections` uzerinden saymak yanlis olurdu: sabitler bolumu 12 satirla
    // sinirli ve ustu CIZILMIYOR. Cizilmeyen bir satir da hedefinin disinda
    // ve kullanici onu markette arayacak - cumle onu saymak zorunda.
    val deviants = filter { it.deviatesFrom(targetStoreId) }

    return ListState(
        sections = sections,
        taken = alinan.map { it.toUiRow(myMemberId, now, targetStoreId = targetStoreId) },
        loading = false,
        shoppingMode = shoppingMode,
        emptyKind = emptyKind,
        targetStoreId = targetStoreId,
        declaration = storeDeclaration(
            targetName = targetStoreName,
            deviantStoreNames = deviants.mapNotNull { it.storeName },
        ),
    )
}
