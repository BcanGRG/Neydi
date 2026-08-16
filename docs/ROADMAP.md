# Neydi — Yol Haritası

Tek gerçek kaynak. Sıradaki iş her zaman **en üstteki işaretlenmemiş kutu**.

**Ürün:** iki kişilik bir hane için ortak market listesi — ne alacağınızı
hatırlatan ve raf etiketi çektikçe **ürün bazında** fiyat hafızası biriktiren.
Döngü: *liste → markette işaretle → etiket çek → ürün + marka + market + tarih
+ fiyat gözlemi → sonraki listede fiyat ipucu*.

**Durum:** Faz E 13/19 · sıradaki **E12** (kullanıcının etiket fotoğrafları bekliyor). Tasarım beşinci turu da
kapattı (**36 karar**), ayna dokuz dosyayla tazelendi. Uygulama derleniyor, cihazda kurulu, **222 test yeşil**,
**sıfır derleyici uyarısı** (F10.10 kapandı). İkon seti Phosphor'a taşındı — `material-icons-extended`
ve `coil` bağımlılıkları düştü.

> Bu dosya yalnızca **yapılacak işi** ve **kalıcı kuralları** taşır.
> Fiş dönemine (16 Ağu 2026 pivotundan öncesi) ait her şey
> [ARSIV-fis-donemi.md](ARSIV-fis-donemi.md)'de dondurulmuş durumda —
> koddaki `F4.13` gibi göndermelerin kaynağı orası. Buraya geri taşınmaz.

---

## Sıradaki üç iş

| # | İş | Neden şimdi | Kimi bekliyor |
|---|---|---|---|
| 1 | **E12** — gerçek etiket fikstürleri | E14'ün ayrıştırıcısı sentetik örnekle yazılamaz | **Kullanıcı** — ~10 etiket fotoğrafı |
| 2 | **E13** — mağaza tohumu | E15'in market seçicisi buna dayanıyor | — |
| 3 | **E14** — TagParser | E15'i açan son teknik parça | E12 |

**Tasarım cevapladı** (16 Ağu): karar defteri 20 maddeye indi, iki yeni dosya
geldi (gezinme sözleşmesi + ikonografi). Kararların kod karşılığı ve kalan beş
soru: [`11-tasarim-kararlari.md`](11-tasarim-kararlari.md). E15 ve E17'nin
spesifikasyonu artık tam — eşikler, geri sırası, hata yolları dahil.

---

## Çalışma sözleşmesi

| Kural | Detay |
|---|---|
| **Dal adı** | `pivot/etiket` (Faz E boyunca tek dal); sonrası `faz<N>/<kisa-slug>` |
| **PR başlığı** | `[E14] Tag parser` — adım numarası zorunlu. **Commit mesajları ve PR metinleri İngilizce**; KDoc/yorum ve `docs/` Türkçe kalır |
| **PR içeriği** | Kod **+** bu dosyadaki kutunun `- [x]` yapılması. Aynı PR'da, ayrı commit'te değil — yoksa harita koddan sapar. |
| **Kapı 1** | `./gradlew :composeApp:assembleDebug` yeşil. Değilse PR açılmaz. |
| **Kapı 2** | `./gradlew :androidApp:installDebug` — bağlı telefonda değişiklik **gözle doğrulanır**. Cihazda görülmeyen bir şey "bitti" sayılmaz. |
| **PR açıklaması** | Cihazda ne görüldüğü tek cümleyle. Görsel değişiklik varsa ekran görüntüsü. |
| **Merge** | Kullanıcı yapar. PR açık, yeşil ve cihazda doğrulanmış bırakılır. |
| **Kod TODO'ları** | Bir TODO kapandığında hem koddan silinir hem burada işaretlenir. |
| **Preview** | Yeni her bileşen `@PreviewLightDark` + `NeydiPreview { }` ile gelir. |
| **Material3 Surface** | Tıklanabilir M3 `Surface`/`Button`/`Card` **kullanılmaz** — etkileşimli her şey `Modifier.pressable`. |
| **graphify** | post-commit hook kodu izliyor. `docs/` değişince manuel `/graphify --update`. |

**İşaretler:** `[ ]` yapılmadı · `[x]` bitti ve cihazda doğrulandı · `[~]` kod
tamam, cihaz doğrulaması bekliyor · `(cihazsız)` Kapı 2'den muaf.

**Cihaz döngüsü:**
```bash
./gradlew :androidApp:installDebug
```
Ekran görüntüsü: `adb exec-out screencap -p > shot.png` ·
`adb`: `C:\Users\buroc\AppData\Local\Android\Sdk\platform-tools\adb.exe`

---

# Faz E — Fişten Etikete *(AKTİF)*

Her adım bir commit/PR; **her adımdan sonra uygulama derlenir ve kurulur**.

## E-A · Kurtarma ✅

Fiş silinmeden önce, fişe ait olmayan parçaları çıkarma işi.

- [x] **E1 — Yol haritası, arşiv, README, tasarım bildirimi** *(cihazsız)*
- [x] **E2 — Para/rakam kurtarma** — `parseMinor`→`Money.kt`,
      `normalizeDigits`→`data/ocr/`, `normalizeUnit`→`QuantityParser.kt`
- [x] **E3 — Dosya/görsel kurtarma** — `data/image/` (EXIF dersiyle),
      `VisualRows`→`data/ocr/`
- [x] **E4 — Mağaza adı kurtarma** — `chainKey`+`storeDisplayName`→`data/store/`

## E-B · Yıkım ✅

- [x] **E5 — Fiş Kontrol** (1.612 satır) · [x] **E6 — Çekim akışı + tarayıcı
      bağımlılığı** · [x] **E7 — ListViewModel'in arka plan OCR'ı**
- [x] **E8 — Geçmiş gezi düzeyine indi** (498→215 satır)
- [x] **E9+E10 — Repository fişsiz; `purchaseEvents` tek kaynak**
- [x] **E11 — `data/receipt/` + şema v4→v5** — cihazda `pm clear`'sız doğrulandı

## E-C · Etiket akışı *(sıradaki iş burada)*

### ▸ E12 — Etiket ölçümü · **KULLANICI BEKLENİYOR**

*Faz 0 disiplini: ayrıştırıcı gerçek veriyle yazılır.*

**Kullanıcının yapacağı:** markette ~10 raf etiketi fotoğrafı — en az **BİM,
A101, Migros**; farklı tipler karışsın (ambalajlı ürün, tartı/manav, kampanyalı
sarı etiket, uzun ürün adı).

**Benim yapacağım:** fotoğrafları ML Kit'ten geçirip **ham OCR çıktısını**
`commonTest` fikstürü olarak commit'lemek.

**Bitti sayılır:** şu iki soru kanıtla cevaplı —
1. Kuruş üstsimgesi (`129⁹⁰`) OCR'da tek parça mı, iki parça mı geliyor?
2. Elde çekimde yön düzeltmesi gerekiyor mu?

> ⚠ **Sentetik örnek yasak.** Fiş ayrıştırıcısının ilk sürümü kendi yazdığı
> örneklerle doğrulanmıştı, 17 test geçiyordu ve hiçbiri bir şey kanıtlamıyordu.

### ▸ E13 — Mağaza tohumu ✅ *(cihazda doğrulandı)*

- Bootstrap'te 7 zincir: **BİM · A101 · ŞOK · Migros · CarrefourSA · File ·
  Tarım Kredi** — `chainKey` ile `chain`, `insert` IGNORE (idempotent)
- `PriceObservationDao.lastUsedStoreId(householdId)` — yapışkan seçicinin
  varsayılanı; **şema değişikliği yok**, son gözlemin marketi okunuyor
- `StoreDao.findByChain` zaten var, çağıranı E13'te geliyor
- `StoreDao` KDoc'undaki karar-11 metni revize edilir

**Bitti sayılır:** temiz kurulumda Ayarlar → Mağazalar'da 7 market görünüyor. ✅
Tohum **zincir-farkında**: başka yoldan gelmiş aynı zinciri ikinci kez
yaratmıyor, önce gelen kazanıyor.

> ⚠ **Cihazda fiş dönemi çöpü bulundu.** Eski `rememberStore` künyeden okuduğu
> her şeyi mağaza yazmış: `Kg`, `KDV`, `Term:`, `Adet`, `Kq`, `ECioREM`,
> `RSALIYE`, `(BTECH)`, `Ae`, `DD`… test cihazında 17 satır. Bunlar Ayarlar'ın
> "Takip edilen zincirler" satırını okunamaz yapar. Kod hatası değil, veri
> kalıntısı — temizlik kararı kullanıcının, bkz. **F10.17**.

### ▸ E14 — TagReader + TagParser *(cihazsız · E12'ye bağlı)*

- `expect fun readTag(imagePath): TagOcr` + Android actual: **tek bitmap, tek
  ML Kit çağrısı**. Şerit/yön oylaması/mükerrer eleme YOK — o makine metrelik
  fiş içindi
- `groupVisualRows` yeniden kullanılıyor (köşe noktası geometrisi)
- **Fiyat** = para desenine uyan parçalar arasından en büyük glifli
- **Birim fiyat satırı** → `priceUnit` + `packSize` çıkarımı; yoksa null
- **Marka** = ad satırının ilk kelimesi, yalnızca ÖNERİ

**Bitti sayılır:** E12'nin her fikstürü doğru fiyatı veriyor.

### ▸ E15 — TagCapture ekranı *(pivotun canlandığı adım)*

- `TagCapture` nav key — **parametresiz** (çekim geziden bağımsız)
- Kamera durumu: `CameraSurface` + `CaptureController` (E6'dan beri hazır
  bekliyor), etiket oranında çerçeve rehberi
- Onay kartı **fotoğrafın üstünde**, dışına dokunmak kapatmaz (karar 25):
  **Fiyat** (düzenlenebilir — "elle fiyat girilmez" kuralının tek istisnası) ·
  **Ürün** (alias çözüyorsa sıfır soru) · **Marka** (kesik çerçeve = tahmin) ·
  **Market** (yapışkan) · **Tarih** (bugün)
- Eksik alan **amber şerit + tek cümle** ("fiyat okunamadı — yaz"); birden çok
  alan boşsa **yalnızca ilki** vurgulanır
- Kart hemen açılır; OCR 1,5 sn'yi geçerse alanlar **iskelet**, kart beklemez
- Kaydet → alias + `PriceObservation` → fotoğraf silinir (karar 29) → kamera
  **300 ms** içinde hazır → toast 2 sn. Kaydet sırasında geri = kayıt tamamlanır
- Aynı market+ürün+fiyat **60 sn** içinde tekrarlanırsa ikinci gözlem yazılmaz
- Giriş: **liste başlığında kalıcı kamera hedefi**, her iki modda (karar 27)
- Geri sırası: klavye → sheet → kart → menü → destinasyon → çıkış

**Bitti sayılır:** cihazda 1 etiket → 1 dokunuş → 1 gözlem satırı → listede
**"Tahmini sepet" ilk kez görünüyor** (`EstimatedBasket` sıfır kodla yanacak).

### ▸ E16 — Satır fiyat ipucu

`observeList`'e iki correlated-subselect (son + önceki gözlem) + store join.
**Tek-SQL kuralı geçerli** — satır başına Flow yasak. `toUiRow` dört dalı da
eşler: `None` / `Single` / `Trend` / `PackChanged`.

**Bitti sayılır:** 2 gözlemli ürünün satırında delta çipi çiziliyor.

### ▸ E17 — Ekran 5 fiyat bölümü

`history(productId, 9)`; 0/1/9 gözlem hâlleri. **"Nerede ucuz" satırının
kimliği market + marka çifti** (karar 26): aynı marketten iki marka = iki
satır, en son fiyat, sıralama fiyata göre en ucuz üstte. Eşikler: sparkline
3 gözlem, bölüm 2 market, delta çipi 2 gözlem.

**Bitti sayılır:** `BİM · Dost · 100 TL` / `Migros · Pınar · 130 TL` çiziliyor.

### ▸ E18 — `~` tahminleri

`LastTrip` ve `HistoryTrip` tutarları gözlemlerden, **her zaman tilde ile**.
E8 ve E11'de bilerek boşa düşürülen üç yerin geri gelmesi: başlık alt satırı,
Geçmiş satırı, özet kartı manşeti.

**Bitti sayılır:** başlık *"Son alışveriş: dün · ~642 TL"*.

### ▸ E19 — Tasarım revizyonu

`10-tasarima-pivot.md`'nin cevabıyla karar defteri güncellenir, ölen ekranlar
silinir.

**Bitti sayılır:** hiçbir tasarım dokümanı var olmayan bir ekranı anlatmıyor.

---

# Faz E sonrası

Numaralar **kimliktir, sıra değildir** — eski F-numaraları PR geçmişi ve kod
göndermeleri bozulmasın diye korunuyor. Ayrıntıları arşivde.

## Öncelik 1 — Fiyat hafızasını tamamlayan işler

- [ ] **F5.7 — Ambalaj boyu çıkarımı.** E14 etiketin birim-fiyat satırından
      `packSize`/`packUnit` çıkarıyor; kalan iş `PriceHint.PackChanged`'i
      besleyip shrinkflation'ı gerçekten yakalamak. *Bu olmadan 1 L → 900 ml
      düşüşü "fiyat sabit" diye raporlanır.*
- **F5.10 ✅ (yerel yarısı) — Mükerrer gözlem koruması.** Tasarımın kuralı:
      *"aynı market + ürün + fiyat 60 sn içinde tekrarlanırsa ikinci gözlem
      yazılmaz"*. `countRecentDuplicates` + `insertUnlessRecentDuplicate`,
      10 test. **E15'ten ÖNCE yazıldı** ve bu kasıtlı: kural yazma yolundan
      önce var olursa çağıran ona uymak zorunda kalır, sonra eklenirse
      "zaten çalışıyordu" sanılan bir şey için ısırdığı hiç görülmeyen bir
      test yazılır.
      SQL `storeId IS :storeId` kullanıyor, `=` değil: `NULL = NULL` yanlıştır,
      yani `=` ile marketi seçilmemiş çekimler **sessizce** korumasız kalırdı.
      Testin ısırdığı kanıtlandı — `=`'e çevirince yalnızca o vaka düştü.
      **Kalan yarı:** "eşitlemede aynı dakika = tek gözlem" senkron motoru
      gelince (Faz 7); bugün `pending_op`'a yazan kod yok.
- [x] **F5.11 — İki biçimlendirici.** `formatEstimate` (`~642 TL`, tilde
      bitişik, kuruşsuz, en yakına yuvarlar) ve `formatRelativeDay` (altı
      basamaklı tarih merdiveni). Başlık, sepet tahmini ve özet manşeti
      bağlandı. Takvim-günü tuzağı zaten `daysBetween`'de çözülmüştü.
- [~] **F6.4 — Eksik Olabilir (Ekran 3).** "Son ödenen fiyat" kolonu artık
      gözlemden besleniyor.
- [ ] **F6.5 — Sabit terfisi + bastırma.** "Bunu önerme" listesinin giriş
      noktası; Ekran 5'e bağlı.

## Öncelik 2 — Dış veri

- [ ] **F0.4 — Kanonik ürün kimliği** *(cihazsız)*. marketfiyati fuzzy match;
      F5.4 ve F5.5'i açar.
- [ ] **F2.7 — Katalog yeniden tohumlanabilir olmalı.** F0.4 ve F3.9'u açar.
- [ ] **F3.9 — "Diğer" kategorisi.** F2.7'ye bağlı.
- [ ] **F5.4 — marketfiyati entegrasyonu.** `/api/v2/search`, `User-Agent`
      zorunlu, agresif cache. **Çevrimdışıysa blok sessizce yok olur** —
      reyonda elleri dolu birine ağ hatası göstermek özelliği zararlı yapar.
      Repoda `HttpClient` yok; ktor katalogda hazır ama bağımlılık değil.
- [ ] **F5.5 — "Başka markette ucuz" çipi.** Çip çizili ve hazır; eksik olan
      kural: **liste başına en fazla 3**, mutlak TL tasarrufuna göre sıralı.
      Üstü listeyi reklam yüzeyine çevirir.

## Öncelik 3 — Cihaz doğrulaması bekleyenler

- [~] **F1.3b — `@Preview` altyapısı**
- [~] **F3.3 — Hızlı ekleme**
- [~] **F3.4 — Pano yapıştırma** *(pano cihazsız doğrulanamıyor)*

## Öncelik 4 — Platform ve yayın

- [ ] **Faz 7 — Senkron** (7.1 Supabase+RLS · 7.2 Auth · 7.3 Realtime v1 ·
      7.4 `updated_at` · 7.5 outbox+tombstone+add-beats-remove · 7.6 keep-alive).
      **Supabase projesi açıldı:** `vjinflzmjcsaicaeatic`, **eu-central-1**
      (Frankfurt — bölge sonradan değiştirilemiyor), ücretsiz plan. Şema
      **boş**, hiçbir migration uygulanmadı.
      **Tasarım turu bitti** → [`15-faz7-sema-plani.md`](15-faz7-sema-plani.md):
      üç bağımsız öneri, her biri ayrı yargıçla çürütülmeye çalışıldı,
      **üçünün de doğruluk puanı düşük çıktı** (4 · 5 · 3) — hiçbiri olduğu
      gibi uygulanabilir değildi. Yirmi beş ölümcül kusur kayıtlı.
      ⚠ **En ağırı mimari:** iki kişi çevrimdışıyken aynı geziye aynı ürünü
      eklerse `(tripId, productId)` UNIQUE çakışıyor, ikinci push 23505
      alıyor ve `pending_op` FIFO olduğu için **outbox kalıcı olarak
      tıkanıyor** — hem de uygulamanın en olası eşzamanlı eylemi bu.
      Çözüm yönü doğal anahtardan türetilen deterministik id, yani çakışma
      red değil upsert olur. F7.5'in "add-beats-remove"u zaten oraya
      işaret ediyordu.
      `syncPhotos` kolonu **ölü** (karar 29 fotoğrafı siliyor); sunucuya
      taşınmıyor, yerelden de düşürülebilir.
- [ ] **Faz 8 — Marka varlıkları** (8.1–8.6). ⚠ Logo konsepti **C ("Fişin
      Kuyruğu") elenmeli** — fiş artık ürünün parçası değil.
- [ ] **Faz 9 — iOS** (9.1 kabuk · 9.2 **etiket hattı**: `downscaleForOcr` +
      `readTag` actual'ları · 9.3 status bar · 9.4 gerçek cihaz · 9.5
      TestFlight). Mac gerektirir.

## Öncelik 5 — Sürekli / refactor

- **F10.10 ✅ — Pano okuması güncel API'ye taşındı.** `LocalClipboardManager` →
      `LocalClipboard`. Yenisi metni `ClipEntry` olarak veriyor ve `ClipEntry`'nin
      commonMain'de metin okuyan public üyesi **yok** (Compose'un kendi `readText()`
      yardımcısı `internal`), o yüzden küçük bir `expect/actual` gerekti:
      `plainTextOrNull()`. Android'de `ClipData`, iOS'ta `getPlainText()`.
      **Projenin tek derleyici uyarısı kapandı** — zorlanmış tam derlemede sıfır.
- [ ] **F10.5 — Sheet yüksekliğindeki sihirli sayı.** `TODO(sheet-yuksekligi)`
- [ ] **F10.2 — Bottom sheet'leri Nav3 Scene'e taşı.** F10.5'e bağlı.
- **F10.6 ✅ — M3 tıklanabilir bileşen sözleşmesi.** Kod zaten temiz: tıklanabilir
      `Button`/`Card`/`ListItem` sıfır, dokuz `Surface`'ın hiçbiri tıklanamaz.
      `HistoryScreen` satırları da fiş döneminde dokunulabilirdi, karar 30 o hedefi
      kaldırdı — madde koda değil, kendi kaydına takılı kalmış.
- [ ] **F10.7 — Odak halkasını bağla.** `Modifier.focusRing` tanımlı, çağıranı yok.
- [ ] **F10.8 — 44dp altındaki üç kontrol.**
- [ ] **F10.9 — Satır silme yolu yok.** ⚠ **Tasarım bunu zaten tarif etmiş** —
      gezinme sözleşmesinin "Jest" satırı: *"sağdan sola swipe listede satırı
      siler (geri alma 'Alındı' bölümünde değil, 5 sn'lik snackbar'da)"*, artı
      200 ms yükseklik daralması animasyonu. Veri tarafı da hazır (`remove()` →
      `softDelete`). **Tek engel:** geri alma snackbar'ı uygulamanın İLK aksiyon
      taşıyan snackbar'ı olur, oysa **karar 8** snackbar'ın *"uygulamada tek bir
      yerde"* kullanıldığını yazıyor. İki doküman çelişiyor → altıncı turda
      soruldu (`14-tasarima-sorular-6.md`).
      **Ders:** bu madde "tasarlanmamış" diye duruyordu; aslında sözleşmede
      yazılıydı ve biz aynayı tazeleyip içini taramamıştık.
- [ ] **F10.11 — Ölü kod ve ölü token temizliği.** *(bir bölümü yapıldı)*
      **Silinenler:** `ui/screens/Placeholders.kt` (86 satır, hiçbir dosya import
      etmiyordu), `ListScreen`'deki altı FileKit import'u, `formatDayMonthTime`,
      `NeydiExtraShapes.barTop`, katalogdan `coil`/`coil-compose`.
      **Kalan adaylar:** `SafeArea`, `AccentStrip`, `Modifier.focusRing`.
      ⚠ **`AccentSurface` bu listeden CIKTI:** "sıfır çağıran" iddiası yanlıştı —
      `AccentChip.kt:62` ve `:78`'den çağrılıyor, `AccentChip` da üretimde canlı
      (`ListItemRow.kt:256`).
- [ ] **F10.3 — `graph.json` takip kararı.**
- **F10.17 ✅ — Fiş dönemi mağaza kalıntısı.** Test cihazında 17 çöp `store`
      satırı vardı (eski `rememberStore` her yanlış okunan künye satırını
      mağaza yazmış). Silindi, 10 `trip.storeId` referansı boşaltıldı.
      **Temizlik göçü yazılmadı ve bu bilinçli:** fiş ayrıştırıcısı öldüğü için
      yeni çöp üretilemez; tek seferlik bir iş için kalıcı göç yazmak sonsuza
      kadar taşınacak ölü kod olurdu.
- **Kapandı:** F6.9 ✅ · F10.1 ✅ · F10.4 ✅ *(03'e arşiv notu düşüldü)* ·
  F10.12 ✅ *(uyarı sayısı 5→1)* · F10.13 ✅ · F10.15 ✅ · F10.16 ✅

- **F3.12 ✅ — Eklenen satır klavyenin altında kalıyordu** *(kullanıcı bildirdi)*.
      Klavye açıkken ekleme yapınca satır kendi reyonuna düşüyor; o reyon ekranın
      altındaysa girdi temizleniyor ama liste kıpırdamıyordu — eklendi mi
      eklenmedi mi belli olmuyordu. **Satır taşınmıyor, kamera taşınıyor:**
      yeni satırı en üste almak listenin reyon düzenini bozardı ve o düzen
      markette gezerken işin tamamı. Zaten tam görünürse kıpırdamıyor.
      Tetikleyici `AddedRow(rowId, seq)` — `seq` şart, çünkü aynı ürünü ikinci
      kez eklemek yeni satır açmıyor, adedi artırıyor: yalnızca id'ye bakan bir
      ekran tam da ikinci eklemede sessiz kalırdı.
      Dizin `layoutInfo`dan değil VERİDEN hesaplanıyor (`rowIndexInList`):
      `layoutInfo` yalnızca bestelenmiş öğeleri tanır ve satır ekranın epey
      altındaysa orada bulunamaz — tam da kaydırmanın gerektiği durumda.
      **İlk sürüm eksikti, kullanıcı bildirdi.** İki hata vardı: (1) sinyal
      `repo.add` döner dönmez düşüyor ama `state` veritabanı akışından geriden
      geliyordu — etki çalıştığında satır henüz listede yok, dizin `null`, ve
      etki bir daha denenmiyordu. Yarış olduğu için kararsızdı: satır zaten
      görünür bir reyona düştüyse fark edilmiyordu, YENİ reyon açıldığında
      görünüyordu. `snapshotFlow` artık satırın listeye düşmesini bekliyor,
      bekleme 2 sn ile sınırlı. (2) `addFromEngine` `repo.add`'i doğrudan
      çağırıyordu ve sinyali kimse yazmıyordu — öneri şeridinden ekleme hiç
      kaydırmıyordu. Sinyal artık tek kapıdan (`signalAdded`) geçiyor.

## Öncelik 6 — Tasarım sadakati

- [ ] **F11.4 — Tanımlı ama kullanılmayan tasarım primitifleri.** Artık **üç**
      isim (`SafeArea`, `AccentStrip`, `focusRing`) — `AccentSurface` yanlışlıkla
      listedeymiş, çağrılıyor. `AccentStrip`'in kaderi tasarıma bağlı: amber şerit
      3dp ve amber sözleşmesi 1.5dp kenarlık şart koşuyor, yani iki yandan kenarlık
      konunca iç dolgu 0dp kalıyor ve amber tamamen kayboluyor. Sorulacak.
- [ ] **F11.6 — Alışveriş modu satır container'ı.**
- **F11.11 ✅ — İkon seti Phosphor'a taşındı (karar 32–34).** 15 ikon
      Phosphor Regular 2.1.1 çizimleriyle elle `ImageVector`. Değişken font
      paketlenmedi, `Text` olarak çizilmedi ve **`material-icons-extended`
      bağımlılığı tamamen düştü** (sürüm kataloğundan da silindi).
      Çağrı yerlerinin hiçbiri değişmedi — `NeydiIcons` katmanının vaadi
      sınandı ve tuttu. Karanlık tema telafisi renk kademesi olarak geldi
      (`iconMuted`, karar 33). Yeni testler: her path'in gerçekten ayrıştığı,
      iki ikonun aynı çizimi taşımadığı, yalnızca yön taşıyanların
      `autoMirror` olduğu. Cihazda beş ikon gözle doğrulandı; on beşinin
      atlası `NeydiIcon.kt`'de `@PreviewLightDark` olarak duruyor.
- **F11.12 ✅ · F11.13 ✅ · F11.14 ✅ · F11.15 ✅ · F11.16 ✅ — beşinci tur kapandı.**
      Ayna dokuz dosyayla tazelendi (**İkonografi ilk kez geldi**), Ekran 1
      başlık örneği merdivene uydu, karar 33 ilişki olarak yeniden yazıldı ve
      okumamız birebir benimsendi, ekran haritasındaki ölü fiş yolları düştü.
      Eşik çelişkisini **karar 36** kapattı → aşağıda F11.19.
- [ ] **F11.17 — Ekran 1'in beşinci çerçevesi tildesiz.** Dört maket `~642 TL`
      oldu ama biri hâlâ *"Son alışveriş: bugün · 642,50 TL"* — tilde yok,
      kuruş var. Türetilmiş tutar, biçim kuralına aykırı. Tasarıma sorulacak.
- [ ] **F11.18 — İkonografi, karar 33'ü eski çiftiyle örnekliyor.** Karar
      defteri ilişkiyi doğru yazıyor; İkonografi aynı kuralı hâlâ *"metin
      `#E4D8C9`, ikon `#F5EDE6`"* diye örnekliyor. Defter esas alındı.
- [ ] **F11.19 — Karar 36'nın renk ayrımı cihazda görülmedi.** Kod, testler
      (8 yeni) ve önizleme yerinde; ama uygulamada bugün **gözlem üretebilen
      bir yüzey yok**, yani karışık liste (kimi koyu, kimi soluk) çalışan
      uygulamada ulaşılamaz bir hâl. Sıfır gözlemli hâl cihazda doğrulandı.
      **E15** gelince gözle bakılacak.
- **F11.10 → E19'a devroldu.**

---

# Kalıcı kurallar ve dersler

*Pivottan bağımsız; fazlar bitince de silinmez.*

## Şema kuralı

`execSQL` **commonMain'de yok** → bütün göçler tamamen otomatik kalmak zorunda:
yeni NOT NULL kolon `@ColumnInfo(defaultValue = …)` taşır, gerisi nullable.
Tablo/kolon **silmek** de otomatik: `@DeleteTable` / `@DeleteColumn` birer
annotasyon, içlerinde SQL yok (`Migration4To5Spec` örneği).

**Nöbetçi:** `SchemaBaselineTest` v1–v5 identityHash'lerini kilitliyor ve
`<n>.json` varlığını arıyor. Şema değişince aynı commit'te yeni hash girer.

**Boş tablonun şema hatası bedavadır** — bump, ilk yazandan önce gelir.
`price_observation` bu kuralın canlı kanıtı: v1'den beri boş durdu, pivotta
`brand` eklemek sıfır riskliydi.

**Cihaz protokolü:** eski sürümü kur → veri ekle → yeni sürümü kur,
**`pm clear` YAPMADAN**. ⚠ Doğrulama yaparken `neydi.db` tek başına çekilirse
göç öncesi hâli görünür — WAL henüz checkpoint edilmemiş olur; `-wal` ve
`-shm` ile birlikte çekilmeli.

## Altı sessiz hata sınıfı

Örnekleri arşivdeki "Öğrenilenler"de:

1. **Kendi örneğiyle kendini onaylama** — sentetik fikstür hiçbir şey kanıtlamaz.
   **Önizleme fikstürü de fikstürdür:** Ayarlar önizlemesi üç kısa mağaza adı
   uyduruyordu, gerçek tohum yedi ad üretiyor. Üçü satıra sığdığı için önizleme
   yeşil görünürken cihazda etiket **harf harf alt alta** akıyordu. Fikstür,
   layout'un çalıştığı veriyi seçmişti. Kural: fikstür gerçeğin **kaynağını**
   okusun (`SEED_CHAINS` gibi), taklidini değil
2. **Isırdığı kanıtlanmamış test test değildir** — düzeltmeyi geri alıp kırmızıya düştüğü görülmeli
3. **Kelime sınırsız önek eşleşmesi** — `" pos"` `" poseti"` içinde bulunur
4. **SQL dizgisi koddur** — kolon adı, bağ değişkeni ve takma ad Kotlin ile sözleşmedir
5. **Locale'siz harf dönüşümü** — `"İNCİR".lowercase()` yedi kod noktası üretir
6. **Ekranda görünmeyen "bitti" değildir** — Kapı 2 bu yüzden var

## Riskler

- **Ölçek riski YOK ve bu bir karar** — iki kişilik hane, elli gezi
- **Türkçe yerelleştirme değil, doğruluk kısıtı**
- **Takvim günü ≠ 86.4M ms bloğu**
- **⚠ Yeni: etiket çekim yükü.** Değer eğrisi artık "kullanıcı kaç etiket
  çekerse ayakta kalır" sorusuna bağlı. E15'in seri çekim akışı bu riskin ilk
  cevabı; ölçümü Faz 7 öncesi yapılmalı.

## Açık kararlar

1. **Fiyat gözlem birimi** — paket mi kg mı? E14 `priceUnit` ile ikisini de
   taşıyor; *gösterim* kararı E17'de.
2. **Katalog fiyatı ile gözlem aynı tabloda mı** (F5.4).
3. **Blok listesi olay mı tablo mu** (F6.5).
4. **Hane yeniden anahtarlama** (Faz 7).
5. ~~Etiket fotoğrafı kayıttan sonra silinsin mi~~ — **kapandı**: karar 29
   evet diyor, gerekçesi de aynı (etiket ödeme kanıtı değil, fiyatın okunduğu
   an). Hiçbir yüzey fotoğraf çizmiyor.

## Bayat adlar — harita ≠ kod

| Dokümanda | Kodda |
|---|---|
| fiş çekme akışı | `TagCapture` *(E15'ten sonra)* |
| `ReceiptReader` | `readTag` *(E14'ten sonra)* |
| Fiş Kontrol | yok — onay kartı `TagCapture`'ın içinde |
| `attachReceiptToTrip` | yok — gözlem geziye bağlanmıyor (pivot karar 3) |
| `ListeEkrani` / `kurusFormatla` | `ListScreen` / `formatMinor` |

## Kod TODO eşlemesi

| TODO | Kapatan adım |
|---|---|
| `sheet-yuksekligi` | F10.5 |
| `tnum` | F9.4 |
| `kategori-tonlari` | F6.9 *(kapandı, TODO silinecek)* |
| `splash` | F8.4 |
| `ios` · `ios-statusbar` | F9.1 · F9.3 |

---

## İlgili dokümanlar

| Dosya | Ne işe yarar |
|---|---|
| [12-tasarima-sorular-4.md](12-tasarima-sorular-4.md) | **Aktif** — tasarıma dördüncü tur: boş durum atlası, ikon envanteri, ikon ekseni teknik engeli |
| [11-tasarim-kararlari.md](11-tasarim-kararlari.md) | **Aktif** — 20 kararın kod durumu, gezinme sözleşmesi sabitleri, ikonografi |
| [10-tasarima-pivot.md](10-tasarima-pivot.md) | Tasarıma pivot bildirimi — cevaplandı, arşiv değeri |
| [ARSIV-fis-donemi.md](ARSIV-fis-donemi.md) | Pivottan önceki tam harita; F-numaralarının kaynağı |
| [01-claude-design-prompt.md](01-claude-design-prompt.md) | Sekiz ekranın özgün spesifikasyonu |
| [05](05-tasarim-denetimi.md) · [06](06-tasarima-sorular.md) · [07](07-tasarima-sorular-2.md) · [08](08-tasarim-bulgulari.md) · [09](09-tasarima-sorular-3.md) | Önceki tasarım turları — fiş dönemi, arşiv değeri |
| [03-arastirma-bulgulari.md](03-arastirma-bulgulari.md) | ⚠ Fiş iddiaları geçersiz, başında arşiv notu var |
| [00-isim-onerileri.md](00-isim-onerileri.md) · [02-logo-splash-prompt.md](02-logo-splash-prompt.md) | İsim analizi · logo/splash promptları |
| `tasarim/` | Ekran tasarımları, karar defteri, devir paketi |
