# Altıncı tur — aynadan çıkan iş listesi

**17 Ağustos 2026.** Dokuz tasarım dosyası tazelendi, **58 kural** çıkarıldı.
Aşağısı otomatik çıkarımın ham hâli; ROADMAP'e işlenen özeti maddelerin
kendisinde.

---

## 1 · KARAR 37–45 — ne diyor, kodda ne değişiyor

| # | Kararın çekirdeği | Dokunulacak dosya (mutlak yol `…\Neydi\composeApp\src\commonMain\kotlin\com\neydi\app\`) | İş kalemi |
|---|---|---|---|
| **37** | Snackbar **ikinci** kullanımını alıyor: satır silinince 5 sn, tek aksiyon `Geri al`. Swipe **yalnız** plan modu **ve** alınmamış satır. "Geri al" = tek-kelime kuralının bilinen tek istisnası | `ui\list\ListScreen.kt` (snackbar host + jest kapısı), `ui\list\ListViewModel.kt` (silme + 5 sn pencere), `ui\components\ListItemRow.kt` (jest sarmalı), `ui\components\NeydiToast.kt` (KDoc bayat + aksiyonlu varyant) | **F10.9** |
| **38** | Jestsiz eş **taşma menüsünde değil**, Ürün Detayı'nın son satırı: error renkli "Listeden çıkar". Geçmiş'ten açılınca aynı slotta kiremit "Listeye ekle" | `ui\product\ProductSheet.kt` (+ açılış kaynağı parametresi), `ui\list\ListScreen.kt` (menüye **eklenmeyecek** — yasak olarak kayda geçer) | **F10.9** |
| **39** | Marka satırı klavyesiz çip sheet'i açıyor: bu ürün için görülmüş markalar (en sık önde) + "Marka yok". OCR tahmini kesik çerçeveli çip, listenin başında | `ui\capture\` (yeni `BrandPickerSheet`), `data\db\Daos.kt` (yeni `DISTINCT brand` sorgusu — bugün **yok**), `ui\components\Chips.kt` (kesik çerçeve varyantı) | **E15** (A1 bloke kalemi) |
| **40** | Market seçici ürün seçicinin ikizi: başında arama alanı, `+ Yeni market «AKYURT»` tek dokunuşla ekliyor. Tek-klavye istisnası artık **"arama alanları"** (Ekle, ürün seçici, market seçici). Marka seçicide arama **yok** | `ui\capture\` (yeni market seçici), `data\store\StoreName.kt` (`chainKey` zaten var — sessiz eşleme buna oturuyor), `ui\list\AddSheet.kt:165` (arama alanı modeli yeniden kullanılacak) | **E15** (A2 bloke kalemi) · E13'e dayanıyor |
| **41** | Çip **iki koşulu birden** ister: ≥%10 **ve** ≥5 TL ucuz, **ve** karşı gözlem 14 günden eski değil. Trendle çakışırsa çip kazanır, trend bastırılır. Sıralama mutlak TL, liste başına en fazla 3 | `ui\list\ListViewModel.kt` veya `data\repo\ListRepository.kt` (`observeList` tek-SQL kuralı geçerli), `ui\components\RowModel.kt:62` (`cheaperElsewhere` alanı tanımlı, **hiç doldurulmuyor**) | **F5.5** (E16'ya bağlı) |
| **42** | `#B34418` dolgu = ileri götüren birincil; `#3F6B54` dolgu = onay/bitirme; `#8A7666` kenarlık = üçüncü. `#2E6B45` buton zemini **hiç** değil | `ui\components\NeydiButton.kt`, `ui\theme\Color.kt` — **kod zaten uyuyor** (bkz. §2) | **iş yok** |
| **43** | Delta çipi ve trend oku kırmızı/yeşil: artış `#B3261E`, düşüş `#2E6B45` | `ui\theme\Color.kt:163-164` (`priceUp = LightError`, `priceDown = LightSuccess`) — **kod zaten uyuyor** | **iş yok** (ok çizimi hariç → §4) |
| **44** | "Nerede ucuz"da ambalaj boyu **filtre**: yalnız eski boydan gözlemi olan market düşer; geriye **2 market** kalmazsa bölüm hiç çizilmez. Grafik aralık çipleri bölümü daraltmaz | E17'nin `history(productId, 9)` sorgusu. **Şema hazır** — `data\db\PriceObservation.kt:46-48` `packSize: Double?` + `packUnit: String?` | **E17** |
| **45** | 36sp manşet düşüyorsa **kart hiç görünmüyor**. İlk gezide "Geçen sefer", gözlemsiz gezide "En çok artan" satırları da sessizce düşer | `ui\list\BasketAndSummary.kt:141-152` — **bugün tersini yapıyor** (§5'e bak) | **E18 ile birlikte** |

**Düzeltilen üç eski karar**

| # | Değişen | Kod karşılığı |
|---|---|---|
| **8** | Gerekçe "tek bir yerde" → **"iki yerde"** (Geçmiş'te gör + Geri al). Toast kararının kendisi değişmedi | `ui\components\NeydiToast.kt:27` KDoc'u **bayat** — "uygulamada tek bir yerde kullaniliyor" cümlesi düzeltilecek. Bileşenin kendisi değişmez |
| **32** | Elle taşınacak ImageVector sayısı 15 → **17** | `ui\components\NeydiIcon.kt` s.46, 47, 56, 260 ve `all` listesi (s.273) |
| **34** | Envanter **17**; eksik ikon kuralı "kodda **ve maketlerde**" karşılığı olan beş ikon | `NeydiIcon.kt` + `composeApp\src\commonTest\kotlin\com\neydi\app\ui\components\NeydiIconsTest.kt:51` (`assertEquals(15, …)` → 17) |

---

## 2 · BİZİM DÖRT KARARIMIZ — dördü de onaylandı, biri sayısallaştırıldı

| Bizim (B) | Tasarımın cevabı | Durum | Kanıt |
|---|---|---|---|
| **B1** kiremit dolgu birincil | **Karar 42** | ✅ **Aynen onaylandı** | Karar 42 metni: *"Maketler geçerli. #B34418 dolgu ileri götüren birincil aksiyon (üstünde onPrimary) … §11'in kiremit «asla» satırı **düzeltildi**"*. Gerekçemiz de birebir benimsendi: *"onPrimary rolünün varlığı da dolgulu birincil butonu ispatlıyor — aksi hâlde o rol boşta kalırdı"*. Gezinme Sözleşmesi `colorMeaning` satır 569 yeniden yazıldı: Kiremit *"dolgulu buton zemini (üstünde onPrimary)"*, eski `Asla: Dolgulu buton zemini` düştü |
| **B2** fiyat yönü kırmızı/yeşil | **Karar 43** | ✅ **Aynen onaylandı** | Karar 43: *"§11'in kırmızı «asla» satırındaki «fiyat artışı» kalktı; yeşilin yasağı **rozet yasağı** olarak duruyor"* — "rozetle değil sırayla anlatma" okumamız kelimesi kelimesine alındı. `colorMeaning` satır 570: Kırmızı `never` artık yalnızca *"Hata mesajı, doğrulama"* |
| **B3** ambalaj boyu = filtre | **Karar 44** | ✅ **Onaylandı + eşik sayısallaştırıldı** | Biz *"bölümün kendi eşiğinin altında market kalırsa"* demiştik (eşiği adlandırmadan). Karar 44 sayıyı yazdı: *"geriye **iki market** kalmazsa bölüm hiç çizilmiyor"* — `thresholds` satır 575'teki mevcut "2 market" eşiğiyle aynı. "Aralık çipleri bölümü daraltmaz" ve "karar 26'daki sıralama belirsizliği kendiliğinden düşüyor" gerekçelerimiz de alındı |
| **B4** hesaplanamayan özet kartı çizilmiyor | **Karar 45** | ✅ **Aynen onaylandı** | Karar 45: *"36sp manşet düşüyorsa kart hiç görünmüyor, yerine hiçbir şey konmuyor. İlk gezide «Geçen sefer», gözlemsiz gezide «En çok artan» satırları da sessizce düşüyor."* İki alt satırın sessizce düşmesi bizim eklememizdi, aynen girdi |

**Sonuç: sıfır itiraz, sıfır değişiklik.** F11.22 ("dört kararı biz verip geçtik, itiraz gelirse geri alınır") **kapanabilir** — itiraz gelmedi, dördü de karar defterine numaralı madde olarak girdi.

⚠ **Bir belirsizlik (kod işi değil, ayna işi):** Karar 43 düşüş için `#2E6B45` diyor; Gezinme Sözleşmesi `colorMeaning` satır 568'de "fiyat düşüşü yönü" **`#3F6B54` swatch'ının** altında yazılı. `colorMeaning` yalnızca beş renk taşıyor ve `#2E6B45`'in ayrı bir kartı yok, yani muhtemelen swatch kabalığı. **Kod karar 43'e uyuyor** (`Color.kt:164 priceDown = LightSuccess = 0xFF2E6B45`), değiştirilecek bir şey yok — ama iki belge iki hex söylüyor, kayda geçmeli.

---

## 3 · F10.9 YAZILABİLİR Mİ? — Evet, tek gerçek engel kalktı; üç parça yazılacak

**Engel kalktı:** ROADMAP F10.9'un "tek engel" diye yazdığı şey karar 8'in *"tek bir yerde"* cümlesiydi. Karar 37 + karar 8'in düzeltilmiş gerekçesi + Gezinme Sözleşmesi `invariants` (*"Snackbar iki yerde yaşar… üçüncü bir aksiyonlu geçici yüzey yok"*) bunu kapattı. Dört spesifikasyon sorusunun (jest kapsamı, "Alındı" bölümü, aksiyon etiketi, jestsiz eş) **dördü de** cevaplandı.

### Kodda BUGÜN VAR
| Parça | Yer | Not |
|---|---|---|
| Soft delete yazma yolu | `data\repo\ListRepository.kt:264` → `tripLineDao.softDelete(rowId, clock())` | Tek satır, çalışıyor |
| DAO | `data\db\Daos.kt:397-398` `UPDATE trip_line SET deletedAt = :at` | |
| ViewModel kancası | `ui\list\ListViewModel.kt:501-503` `fun remove(rowId)` | **ÇAĞIRANI YOK** — bugün ölü kod. UI'da hiçbir silme yolu yok |
| Mezar kazma | `ListRepository.kt:186-205` `findIncludingDeleted` + `deletedAt = null` | ⚠ Bu **restore değil, yeniden ekleme**: `quantity`, `checked`, `checkedAt`, `addedByMemberId`, `fromSuggestion`, `createdAt` **sıfırlanıyor** |
| Uzun dokunuş → Ürün Detayı | `ui\list\ListScreen.kt:444` ve `:469` → `vm::openProductSheet` | Delta "belirsiz, doğrulanmalı" diyordu — **var ve çalışıyor**, karar 38'in dayandığı yol hazır |
| Taşma menüsü | `ListScreen.kt:658-700` `OverflowMenu` | Ekran düzeyinde, satır kimliği taşımıyor. Karar 38 buraya **eklenmemesini** istiyor → değişiklik yok, yasak kayda geçer |
| 200 ms sabiti | `ui\theme\Motion.kt:36` `CHECK_MS = 200` | ⚠ Bu **işaretleme** animasyonu. Silme geri dönüşü için ayrı bir sabit gerekiyor (aynı sayı, farklı iş — tek sabiti iki işe bağlamak Motion.kt'nin kendi kuralına aykırı) |

### Kodda BUGÜN YOK
| Eksik | Neden gerekli |
|---|---|
| **Snackbar bileşeni — hiç yok** | `grep SnackbarHost\|SnackbarHostState\|showSnackbar` → **sıfır sonuç**. Kapanış snackbar'ı da yazılmamış. `NeydiToast` (tek geçici yüzey, `ListScreen.kt:478`) aksiyonsuz ve dokunma hedefi yok. Yani karar 37 uygulamanın **ilk** aksiyonlu geçici yüzeyini yazdırıyor. Maket hazır: `#221A14` zemin, 18dp köşe, 14/16dp iç boşluk, maks 360dp, metin `#F5EDE6` 500/14sp, aksiyon `#FF9166` 600/14sp, alt şeridin 12dp üstünde |
| **Gerçek restore yolu** | `undoDelete(rowId)` için `deletedAt = null` **ama diğer alanlar korunarak**. Mevcut `add()` yolu miktarı ve "kim ekledi"yi ezer → "Geri al" 2 kg elmayı 1 kg yapardı. Yeni bir `TripLineDao.restore(id)` veya `ListRepository.undoRemove(line)` gerekiyor |
| **Swipe jesti** | `ui\components\ListItemRow.kt:111` bugün yalnızca `.pressable(onLongPress, onTap)` — hiç `pointerInput`/`draggable`/`anchoredDraggable` yok. Sağ kenardan yatay sürükleme + 100dp `#B3261E` zemin + beyaz **"Sil" KELİMESİ** (çöp kutusu ikonu envanterde yok, İkonografi'nin "Yıkıcı iş" kuralı hâlâ geçerli) |
| **Jest kapısı** | `mode == Plan && !row.isChecked` koşulu. "Alındı" bölümündeki satırlar jesti **hiç** almayacak |
| **"Listeden çıkar" satırı** | `ui\product\ProductSheet.kt` — dosyada "Listeden"/"çıkar"/"sil" hiç geçmiyor. 56dp, üstünde 1px ayırıcı, `colorScheme.error`, ikon yok, sağda kontrol yok |
| **Sheet'e kaynak parametresi** | Karar 38 "Geçmiş'ten açılınca kiremit «Listeye ekle»" istiyor → `ProductSheetState`'e/`ProductSheetContent`'e açılış kaynağı |
| **200 ms geri dönüş sabiti** | Motion.kt'ye yeni sabit |
| **Kesinleşme yolu** | 5 sn dolunca kalıcı; ikinci onay yok; kategori sayacı **silme anında** düşer (snackbar beklemeden) |

### ⚠ İki bağımlı davranış
1. **Silmeyle boşalan liste** → Boş Durumlar çerçeve 01: `EmptyKind.DONGU_ORTASI` çizilir ve üstünde snackbar durur. `ui\list\EmptyStates.kt` + `ListState.kt:85` hazır, yalnız silme akışıyla bağlanacak.
2. **ProductSheet bugün tek anahtar taşıyor** (`ProductSheet.kt:114` yalnızca "Her zamankilere ekle"). Maketlerin "iki anahtar"ı yok — ikincisi ("Bunu önerme") **F6.5**. "Listeden çıkar" satırı F6.5'i beklemeden eklenebilir; sıralama "iki anahtarın ardından" olacağı için F6.5 geldiğinde araya girer.

**Karar: F10.9 yazılabilir.** Eksik kalan hiçbir *tasarım* sorusu yok. Kalan iş tamamen implementasyon.

---

## 4 · İKON ENVANTERİ 15 → 17

**Bugün DeltaChip okunu kendisi çiziyor — ve bunu Unicode METİN glifi olarak yapıyor:**

`composeApp\src\commonMain\kotlin\com\neydi\app\ui\components\Chips.kt:78-83`
```kotlin
Text(
    text = if (rising) "↑" else "↓",
    style = MaterialTheme.typography.labelSmall,
    color = color,
)
```

**Tarih düzeltmesi:** oklar **F11.11'de silinmedi**. `git log -S ArrowUpward` iki commit gösteriyor: `273305f [F3.1]` eklenmiş, `95c0f45 [tasarım] Pivot turu karşılandı` silinmiş — *"Ikon envanteri 23 → 13: ArrowUpward/ArrowDownward (DeltaChip kendi okunu çiziyor)"*. O sırada `Icons.Rounded.ArrowUpward` idi, yani material-icons-extended sarmalı. F11.11 (`f63e0bf`) **sonra** geldi ve kalan 15'i Phosphor'a taşıdı. Yani **oklar Phosphor formunda hiç var olmadı**. DeltaChip'in Unicode oku ise `aa4a36e [F3.1]`'den beri, silmeden de önce oradaydı — silme gerekçesi doğruydu.

**Mevcut çizim yeterli mi? HAYIR — iki Phosphor path'i taşınacak.** Üç bağımsız sebep:

1. **Karar 32 doğrudan yasaklıyor:** *"ikonlar `Text` olarak çizilmiyor"*. Bugünkü satır tam olarak bunu yapıyor — kural F11.11'de 15 ikon için uygulandı, bu iki ok istisna kaldı.
2. **Karar 34 envanteri 17 diyor** ve İkonografi çizim kaynağını sabitledi: `ph-arrow-up` / `ph-arrow-down` (Phosphor 2.1.1 `regular`, viewBox 256×256, tek path).
3. **Render riski gerçek:** `↑`/`↓` sistem fontuna bağlı; Skia'nın fallback zinciri iOS ve Android'de aynı gliffi vermiyor ve ağırlık/optik boy `labelSmall` ile eşleşmiyor.

**Somut iş listesi:**
- `ui\components\NeydiIcon.kt`: `NeydiIcons`'a iki `phosphor(...)` çağrısı. **`autoMirror` KAPALI** — dikey yön taşıyorlar, RTL'de çevrilmemeliler (dosyada `autoMirror` yalnız `ArrowBack`/`ChevronRight`/`Logout`'ta açık).
- Aynı dosyada KDoc sayıları: s.46 *"Sonuc: 15 ikon"*, s.47 *"15 path dizesi"*, s.56 *"## Envanter: 15"*, s.260 *"On bes ikonun atlasi"* → 17. `all` listesi (s.273+) iki satır büyür.
- `Chips.kt:78-83` → `NeydiIcon(icon = if (rising) NeydiIcons.ArrowUpward else NeydiIcons.ArrowDownward, contentDescription = null, tint = color, size = <12sp metne oturan dp>)`. `tint = color` zaten `priceUp`/`priceDown` taşıdığı için İkonografi 05'in *"ikon içinde bulunduğu metnin rengini alır"* kuralı korunuyor, amber şerit dışında yeni istisna doğmuyor.
- `commonTest\...\NeydiIconsTest.kt:51`: `assertEquals(15, icons.size, "Karar 34 envanteri 15'e sabitledi")` → 17; s.26'daki elle yazılan eşleme listesi iki satır büyür.
- ⚠ **Belirsiz:** 12sp `labelSmall` metnine oturan ikon boyu dp olarak yazılı değil. İkonografi *"12sp metinle birlikte"* diyor ama dp vermiyor — cihazda gözle ayarlanacak.

---

## 5 · YENİ İŞ KALEMLERİ (ROADMAP'te karşılığı olmayan)

Numaralar öneri; bugünkü en yüksek F11 numarası **F11.22**.

| Öneri | İş | Neden yeni |
|---|---|---|
| **F11.23** | **`SummaryCard` tutar yokken kartı çiziyor — karar 45'in tersi.** `ui\list\BasketAndSummary.kt:141` `if (amountMinor != null)` yalnız **manşeti** koşula bağlıyor; kart ve "8 ürün · 24 dakika" satırı her hâlde çiziliyor. Dosyanın kendi KDoc'u bunu savunuyor: *"Tutar bilinmiyorsa sayilar gosteriliyor… Bu bile bos bir karttan iyi."* Karar 45 tam tersini söylüyor. ⚠ **Sıralama tuzağı:** aynı KDoc *"E18'e kadar tutar HER ZAMAN bilinmiyor"* diyor — karar 45 bugün uygulanırsa özet kartı **tamamen kaybolur**. Bu yüzden **E18'le aynı PR'da** yapılmalı | F11.22 kararı bildiriyor ama koddaki çelişkiyi kimse kaydetmemiş |
| **F11.24** | **`NeydiToast.kt:27` KDoc'u bayat.** *"snackbar AKSIYON tasiyor ve uygulamada **tek bir yerde** kullaniliyor"* → "iki yerde" (karar 8 düzeltildi). Tek satırlık iş, F10.9 PR'ına sığar | Karar 8'in gerekçesi bu turda değişti |
| **F11.25** | **"Yaş" biçimlendiricisi.** Gezinme Sözleşmesi `dateLadder`'a **istisna** satırı girdi: bir gözlemin/alımın **yaşı** gün sayısıyla yazılır ("8 gün önce", "son alım 11 gün önce"), 14 günü geçince hafta ("2 hafta önce") — çünkü "geçen hafta" yedi ile on üç arasını siler. Merdiven yalnız tek başına duran tarihler için. `data\DateText.kt`'de bugün `formatRelativeDay` + `formatDayMonth*` + `daysBetween` var, **yaş modu yok**. Çağıranlar: `ui\components\ListItemRow.kt` alt satırı, `ui\product\ProductSheet.kt`, `ui\missing\MissingItemsScreen.kt` | F5.11 iki biçimlendiriciyle kapanmıştı; bu üçüncüsü |
| **F11.26** | **Çip para biçimi.** `formats`'a yeni satır: *"satır çipi — A101'de 89,00 · yalnızca 24dp çipte TL düşer; cümle içinde asla."* `data\Money.kt`'de `formatMinor` ("289,00 TL") ve `formatEstimate` ("~642 TL") var; **TL sonekini düşüren üçüncü varyant yok**. Çağıran: `Chips.kt` + satır fiyat ipucu | E16'nın parçası ama biçim kuralı yeni |
| **F11.27** | **Liste başlığına kalıcı kamera hedefi.** Boş Durumlar dört çerçevede (01/03/04/08) avatar ile `more_vert` arasına 22px `photo_camera` koydu. `ui\list\ListScreen.kt:593-615` bugün yalnız avatar + `OverflowMenu`. `NeydiIcons.PhotoCamera` **tanımlı ama çağıranı yok** (`NeydiIcon.kt:74`). E15'in giriş noktası bu — karar 27'nin tek kapısı | ROADMAP E15 maddesi *"liste başlığında kalıcı kamera hedefi"* diyor ama ayrı kutu değil; **E15'in içinde sayılabilir**, ayrı kalem gerekmeyebilir |
| **F11.28** | **Alışveriş modu başlığından market adı düşüyor.** Boş Durumlar çerçeve 03: *"Migros Ataşehir · 0/18 alındı"* → *"0/18 alındı · henüz etiket çekilmedi"* (karar 28: adın kaynağı o gezide çekilen son etiket). `ui\list\ListState.kt:70-80` `lastTripSummary` bu satırı üretiyor | Karar 28 eskiydi, maket bu turda uydu |

**Ayna tarafında kalan, koda dokunmayan iki tutarsızlık** (ayna bozulmasın diye düzeltilmedi; kayıt için):
- `Neydi - Kararlar.dc.html` "Defterin bugünkü hâli" kutusu hâlâ *"yirmi altı karar"* diyor, başlık *"Otuz dört"*.
- `Neydi - Ikonografi.dc.html:78` `compare` listesinin `hint-placeholder-count="15"` kaldı, liste artık 17 satır (`inventory` 16→18 oldu).
- `Neydi - Ekranlar 5-8.dc.html:178` çerçeve altyazısı hâlâ *"tempo · iki anahtar"* diyor, sheet'te artık üç satır var (`Listeden çıkar` s.311'de).

---

## 6 · KAPANANLAR

| Madde | Durum | Kanıt |
|---|---|---|
| **F11.17** — Ekran 1'in beşinci çerçevesi tildesiz | ✅ **KAPANDI** | Boş Durumlar çerçeve 04 başlık altsatırı *"642,50 TL"* → *"~642 TL"*; Ekran 1 özet kartı *"Geçen sefer 601,00 TL"* → *"~601 TL"*, boş hal *"642 TL"* → *"~642 TL"*; Ekranlar 5-8 Geçmiş'te yedi gezi tutarının hepsi tilde + kuruşsuz. **Kod işi yok** — `ListState.kt:77` zaten `formatEstimate()` çağırıyordu, maket koda uydu |
| **F11.18** — İkonografi karar 33'ü eski çiftle örnekliyor | ✅ **KAPANDI** | İkonografi karar 04 paragrafı yeniden yazıldı: *"ikincil metin #C6B6A9 iken ikon #E4D8C9; birincil metin (#F5EDE6) rampanın tepesinde olduğu için orada telafi yok"*. Kod zaten böyleydi (`Color.kt:51 DarkTextSecondary=0xFFC6B6A9`, `:81 DarkIconMuted=0xFFE4D8C9`, `:176 iconMuted=DarkIconMuted`). **Artık: `NeydiIcon.kt` s.63-74'teki "neden saptık" savunma paragrafı gereksiz** — belge koda uydu, paragraf karar 33'e atıf yapan tek cümleye inebilir (küçük temizlik) |
| **F11.22** — Dört kararı biz verip geçtik | ✅ **KAPANDI** | Dördü de karar 42/43/44/45 olarak deftere girdi, **sıfır itiraz**. Karar 44 eşiği ("iki market") sayısallaştırarak onayladı |
| **Altıncı tur §1** — snackbar kaç yerde | ✅ **KAPANDI** | Karar 37 + karar 8 gerekçesi. Dört alt sorunun dördü de cevaplandı: (1) evet ikinci kullanım, (2) "Geri al", tek-kelime kuralının bilinen tek istisnası, (3) alışveriş modunda swipe **yok**, (4) "Alındı" bölümünde swipe **yok** |
| **Altıncı tur A1** — marka satırı | ✅ **KAPANDI** | Karar 39: (a) şıkkı — klavyesiz çip sheet'i. E15 bloğu kalktı |
| **Altıncı tur A2** — "+ Yeni market" | ✅ **KAPANDI** | Karar 40: arama alanı + inline ekleme + sessiz yazım eşlemesi. E15 bloğu kalktı |
| **Altıncı tur A3** — silmenin jestsiz eşi | ✅ **KAPANDI** | Karar 38: Ürün Detayı, taşma menüsü **değil**. Gerekçe de yazıldı: menü ekran düzeyinde yaşıyor, satırı bilmiyor |
| **Altıncı tur A4** — çip eşiği ve önceliği | ✅ **KAPANDI** | Karar 41: %10 **ve** 5 TL, 14 gün bayatlık, çip trendi bastırır, mutlak TL sıralaması, liste başına 3 |
| **F10.9** — satır silme yolu yok | ⏳ **AÇIK ama artık bloke değil** | Tek engel (karar 8 çelişkisi) kalktı. §3'teki yedi parça yazılacak |
| **F5.5** — "Başka markette ucuz" çipi | ⏳ **AÇIK, spesifikasyonu tamamlandı** | Karar 41 eşiği verdi. Hâlâ **E16'yı bekliyor** — `RowModel.cheaperElsewhere` dolduran sorgu yok, gözlem üretebilen yüzey de yok (E15) |

**Kapanmayan, tasarımın hâlâ dokunmadığı:** F11.19 (karar 36 renk ayrımı cihazda görülmedi — E15 bekliyor), F11.20 (`FinishShoppingScreen` sözleşmede yok — bu turda da geçmedi), F11.21 (Ekran 3 ara karesi ihlali — bu turda da geçmedi), F11.4 (`AccentStrip`'in kaderi — soruldu mu belirsiz, bu turun deltasında yok).
