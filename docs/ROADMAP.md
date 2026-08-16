# Neydi — Yol Haritası

Tek gerçek kaynak. Sıradaki iş her zaman **en üstteki işaretlenmemiş kutu**.

**Ürün:** iki kişilik bir hane için ortak market listesi — ne alacağınızı
hatırlatan ve raf etiketi çektikçe **ürün bazında** fiyat hafızası biriktiren.
Döngü: *liste → markette işaretle → etiket çek → ürün + marka + market + tarih
+ fiyat gözlemi → sonraki listede fiyat ipucu*.

**Durum:** Faz E 11/19 · sıradaki **E12**. Uygulama derleniyor, cihazda kurulu,
183 test yeşil, tek derleyici uyarısı var (F10.10'un kendi işi).

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

**Tasarımdan cevap bekleyen** (paralel, hiçbirini bloklamıyor):
`docs/10-tasarima-pivot.md`'nin 7 sorusu → E15 onay kartı yerleşimi, E17 marka
satırları, E19 karar defteri.

---

## Çalışma sözleşmesi

| Kural | Detay |
|---|---|
| **Dal adı** | `pivot/etiket` (Faz E boyunca tek dal); sonrası `faz<N>/<kisa-slug>` |
| **PR başlığı** | `[E14] TagParser` — adım numarası zorunlu |
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

### ▸ E13 — Mağaza tohumu

- Bootstrap'te 7 zincir: **BİM · A101 · ŞOK · Migros · CarrefourSA · File ·
  Tarım Kredi** — `chainKey` ile `chain`, `insert` IGNORE (idempotent)
- `PriceObservationDao.lastUsedStoreId(householdId)` — yapışkan seçicinin
  varsayılanı; **şema değişikliği yok**, son gözlemin marketi okunuyor
- `StoreDao.findByChain` zaten var, çağıranı E13'te geliyor
- `StoreDao` KDoc'undaki karar-11 metni revize edilir

**Bitti sayılır:** temiz kurulumda Ayarlar → Mağazalar'da 7 market görünüyor.

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
- Onay kartı: **Fiyat** (düzenlenebilir — "elle fiyat girilmez" kuralının tek
  istisnası) · **Ürün** (alias çözüyorsa sıfır soru) · **Marka** (opsiyonel) ·
  **Market** (yapışkan) · **Tarih** (bugün)
- Kaydet → alias insert + `PriceObservation` insert → fotoğraf silinir →
  kamera geri gelir (seri çekim)
- Liste ekranına giriş düğmesi *(yeri tasarımdan gelecek — soru 3)*

**Bitti sayılır:** cihazda 1 etiket → 1 dokunuş → 1 gözlem satırı → listede
**"Tahmini sepet" ilk kez görünüyor** (`EstimatedBasket` sıfır kodla yanacak).

### ▸ E16 — Satır fiyat ipucu

`observeList`'e iki correlated-subselect (son + önceki gözlem) + store join.
**Tek-SQL kuralı geçerli** — satır başına Flow yasak. `toUiRow` dört dalı da
eşler: `None` / `Single` / `Trend` / `PackChanged`.

**Bitti sayılır:** 2 gözlemli ürünün satırında delta çipi çiziliyor.

### ▸ E17 — Ekran 5 fiyat bölümü

`history(productId, 9)`; 0/1/9 gözlem hâlleri; **market + marka + fiyat +
tarih** satırları — kullanıcının yoğurt örneği birebir burada.

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
- [ ] **F5.10 — Mükerrer gözlem koruması** *(cihazsız)*. Senaryo pivotla
      değişti: artık "aynı etiketi iki kez çekmek". Kural yeniden yazılacak —
      aynı (ürün, market, gün) için ikinci gözlem güncelleme mi, ayrı satır mı?
- [ ] **F5.11 — İki biçimlendirici.** (a) tam TL (`~640 TL`) — `formatMinor`
      her zaman iki ondalık basıyor; (b) göreli gün ("12 gün önce").
      ⚠ Tuzak: `(now - then) / 86_400_000` **takvim günü saymaz**; aynı
      aritmetik hem gösterimde hem `medianIntervalDays`'te kullanılmalı.
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
      `syncPhotos` kolonunun kaderi burada karara bağlanır — bugün senkron
      edilecek fotoğraf yok.
- [ ] **Faz 8 — Marka varlıkları** (8.1–8.6). ⚠ Logo konsepti **C ("Fişin
      Kuyruğu") elenmeli** — fiş artık ürünün parçası değil.
- [ ] **Faz 9 — iOS** (9.1 kabuk · 9.2 **etiket hattı**: `downscaleForOcr` +
      `readTag` actual'ları · 9.3 status bar · 9.4 gerçek cihaz · 9.5
      TestFlight). Mac gerektirir.

## Öncelik 5 — Sürekli / refactor

- [ ] **F10.10 — Pano okumasını güncel API'ye taşı.** `LocalClipboardManager`
      → `LocalClipboard`. **Projedeki tek derleyici uyarısı bu**; okuma suspend
      olduğu için davranış değişikliği taşıyor, F3.4 ile birlikte doğrulanmalı.
- [ ] **F10.5 — Sheet yüksekliğindeki sihirli sayı.** `TODO(sheet-yuksekligi)`
- [ ] **F10.2 — Bottom sheet'leri Nav3 Scene'e taşı.** F10.5'e bağlı.
- [ ] **F10.6 — M3 tıklanabilir bileşen sözleşmesi** — artık yalnız `HistoryScreen`.
- [ ] **F10.7 — Odak halkasını bağla.** `Modifier.focusRing` tanımlı, çağıranı yok.
- [ ] **F10.8 — 44dp altındaki üç kontrol.**
- [ ] **F10.9 — Satır silme yolu yok.**
- [ ] **F10.11 — Ölü kod ve ölü token temizliği.** Bilinen adaylar:
      `SafeArea`, `AccentStrip`, `AccentSurface`, `Modifier.focusRing`
      (dördü de tanımlı, sıfır çağıran) — F11.4 ile birlikte karara bağlanır.
- [ ] **F10.3 — `graph.json` takip kararı.**
- **Kapandı:** F6.9 ✅ · F10.1 ✅ · F10.4 ✅ *(03'e arşiv notu düşüldü)* ·
  F10.12 ✅ *(uyarı sayısı 5→1)* · F10.13 ✅ · F10.15 ✅ · F10.16 ✅

## Öncelik 6 — Tasarım sadakati

- [ ] **F11.4 — Tanımlı ama kullanılmayan tasarım primitifleri.** Yukarıdaki
      dört isim; kullanılacak mı silinecek mi tasarımın kararı.
- [ ] **F11.6 — Alışveriş modu satır container'ı.**
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

1. **Kendi örneğiyle kendini onaylama** — sentetik fikstür hiçbir şey kanıtlamaz
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
5. **Etiket fotoğrafı kayıttan sonra silinsin mi** — önerimiz evet; tasarıma
   soruldu (soru 5), E15'ten önce ucuz geri dönüş.

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
| [10-tasarima-pivot.md](10-tasarima-pivot.md) | **Aktif** — tasarıma pivot bildirimi, 7 açık soru |
| [ARSIV-fis-donemi.md](ARSIV-fis-donemi.md) | Pivottan önceki tam harita; F-numaralarının kaynağı |
| [01-claude-design-prompt.md](01-claude-design-prompt.md) | Sekiz ekranın özgün spesifikasyonu |
| [05](05-tasarim-denetimi.md) · [06](06-tasarima-sorular.md) · [07](07-tasarima-sorular-2.md) · [08](08-tasarim-bulgulari.md) · [09](09-tasarima-sorular-3.md) | Önceki tasarım turları — fiş dönemi, arşiv değeri |
| [03-arastirma-bulgulari.md](03-arastirma-bulgulari.md) | ⚠ Fiş iddiaları geçersiz, başında arşiv notu var |
| [00-isim-onerileri.md](00-isim-onerileri.md) · [02-logo-splash-prompt.md](02-logo-splash-prompt.md) | İsim analizi · logo/splash promptları |
| `tasarim/` | Ekran tasarımları, karar defteri, devir paketi |
