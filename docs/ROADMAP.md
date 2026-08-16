# Neydi — Yol Haritası

Tek gerçek kaynak. **16 Ağustos 2026'da köklü pivot:** fiş okuma çıktı, raf
etiketi geldi. Pivottan önceki yol haritası olduğu gibi
[ARSIV-fis-donemi.md](ARSIV-fis-donemi.md)'de duruyor — koddaki `F4.13` gibi
göndermelerin kaynağı orası, silinmez.

**Ürün cümlesi (pivot sonrası):** iki kişilik bir hane için ortak market
listesi — ne alacağınızı hatırlatan ve raf etiketi çektikçe **ürün bazında**
fiyat hafızası biriktiren. Döngü: *liste → markette işaretle → etiket çek →
ürün+marka+market+tarih+fiyat gözlemi → sonraki listede fiyat ipucu*.

**İlerleme:** 0 / 19 pivot adımı. Pivot bitince kalan işler aşağıda
"Pivot sonrası" bölümünde.

---

## Çalışma sözleşmesi

| Kural | Detay |
|---|---|
| **Dal adı** | `pivot/etiket` (pivot boyunca tek dal); sonrası `faz<N>/<kisa-slug>` |
| **PR başlığı** | `[E1] ROADMAP yeniden yazıldı` — adım numarası zorunlu |
| **PR içeriği** | Kod **+** bu dosyadaki ilgili kutunun `- [x]` yapılması. Aynı PR'da, ayrı commit'te değil — yoksa harita koddan sapar. |
| **Kapı 1** | `./gradlew :composeApp:assembleDebug` yeşil. Değilse PR açılmaz. |
| **Kapı 2** | `./gradlew :androidApp:installDebug` — bağlı telefonda APK güncellenir ve değişiklik **gözle doğrulanır**. Cihazda görülmeyen bir şey "bitti" sayılmaz. |
| **PR açıklaması** | Cihazda ne görüldüğü tek cümleyle yazılır. Görsel değişiklik varsa ekran görüntüsü eklenir. |
| **Merge** | Kullanıcı yapar. PR açık, yeşil ve cihazda doğrulanmış bırakılır. |
| **Kod TODO'ları** | Bir TODO kapandığında hem koddan silinir hem burada işaretlenir. |
| **Preview** | Yeni her bileşen `@PreviewLightDark` + `NeydiPreview { }` ile gelir. |
| **Material3 Surface** | Tıklanabilir Material3 `Surface`/`Button`/`Card` **kullanılmaz** — etkileşimli her şey `Modifier.pressable`. |
| **graphify** | post-commit hook kod değişikliklerinde grafı günceller. `docs/` değişince manuel `/graphify --update`. |

### İşaretler

- `- [ ]` yapılmadı · `- [x]` tamamlandı ve merge edildi · `- [~]` kod tamam, cihaz doğrulaması bekliyor
- `(cihazsız)` — telefonda görüntülenemeyen adım, Kapı 2'den muaf

### Cihaz döngüsü

```bash
./gradlew :androidApp:installDebug
```

Ekran görüntüsü: `adb exec-out screencap -p > shot.png`
`adb` yolu: `C:\Users\buroc\AppData\Local\Android\Sdk\platform-tools\adb.exe`

---

## Pivot kaydı — neden fiş çıktı

İki haftalık cihaz ölçümü fişin okunamayacağını değil, **yeterince
okunamayacağını** gösterdi: 60 kalemin 31'i; fiş oranındaki bir nesne 12MP
sensörden en fazla ~694 px genişlik alabiliyor (ölçüldü, tavan fiziksel).
Etrafında biriken makine — parça dikişi, aritmetik kapısı, yön oylaması,
görsel satır gruplaması, ~1.800 satır ayrıştırıcı + 1.612 satır Fiş Kontrol —
projeyi takip edilemez hale getirdi.

**Yeni model:** kullanıcı tek tek **raf etiketi** çeker. Her çekim tek bir
tarihli fiyat gözlemi: ürün + marka + market + fiyat + tarih. ("BİM'den
yoğurt, Dost, 100 TL, 16 Ağustos.")

Kullanıcının dört kararı (tartışma kapalı):

1. Çekilen şey **raf etiketi** — fiyatı taşıyan tek yüzey; ambalaj değil.
2. **Marka gözlemin alanı**, ürün kimliğinin değil. Ürün jenerik ("yoğurt");
   markalar arası karşılaştırma böyle mümkün.
3. Etiket çekimi **listeden/geziden bağımsız** — her an, her markette.
4. "Ne ödedik" **gözlemlerden tahmine** döner, her zaman `~` ile.

**Şanslı başlangıç:** `price_observation` tablosu v1'den beri var ve boş —
hiçbir şey yazmıyordu. Tüketicileri (`EstimatedBasket`, `PriceHint`+çipler,
Ekran 5 tasarımı) hazır ve veri bekliyor. Bu bir yeniden yazım değil; ölü bir
organa kan vermek + etrafındaki ölü dokuyu almak.

**Ölen maddeler** (ayrıntıları arşivde): F0.1–0.3, F0.5, F4.2–4.6, F4.11,
F4.13, F4.13b, F4.14, F4.15, F5.6, F5.8, F5.9a. **Dönüşenler:** F5.1→E15,
F5.2→E16, F5.3→E17, F5.9→E13. **Tasarım kararlarından ölenler:** 4, 9, 13,
14, 15; **revize:** 11 (mağaza artık etiket çekiminde seçiliyor), 2 (silme
kapsamında fiş fotoğrafı kalmadı). Tasarıma bildirim: `10-tasarima-pivot.md`.

---

## Faz E — Fişten Etikete (AKTİF)

> Sıra bağlayıcı: önce kurtarma, sonra yukarıdan aşağı yıkım, şema en son,
> sonra inşa. **Her adımdan sonra uygulama derlenir ve kurulur** — istenirse
> herhangi bir adımda durulabilir. Her adım bir commit/PR.

### E-A · Kurtarma (fiş silinmeden önce taşınanlar)

- [x] **E1 — ROADMAP + arşiv + README + tasarım pivot promptu.** *(cihazsız)*
  Bu dosya; eski harita arşive; README ürün cümlesi/teknoloji; tasarıma yeni
  yapıyı anlatan ve yeni yüzeyleri isteyen `10-tasarima-pivot.md`.
  **Bitti:** haritada işaretlenmemiş fiş maddesi yok, prompt tasarıma hazır.
- [ ] **E2 — Para/rakam kurtarma.** *(cihazsız)* `parseMinor`→`Money.kt`,
  `normalizeDigits`→`data/ocr/OcrDigits.kt`, `normalizeUnit`→`QuantityParser.kt`;
  test taşıma (`MoneyParseTest`, `OcrDigitsTest`).
  **Bitti:** yeni test dosyaları yeşil.
- [ ] **E3 — Dosya/görsel kurtarma.** *(cihazsız)* `ReceiptBytes`→`data/image/FileBytes.kt`,
  `ReceiptImage`/`downscaleForOcr`→`data/image/OcrImage.kt` (EXIF dersiyle),
  `VisualRows.kt`+testi→`data/ocr/`; `DataWipe` importu düzelir.
  **Bitti:** `data/receipt/` dışarıdan import edilmiyor.
- [ ] **E4 — Mağaza adı kurtarma.** *(cihazsız)* `chainKey`+`storeDisplayName`
  →`data/store/StoreName.kt`; `StoreNameTest`.
  **Bitti:** History ve Processor yeni yerden import ediyor.

### E-B · Yıkım (her adımda kurulabilir uygulama)

- [ ] **E5 — ReceiptCheck gider.** Ekran+VM+nav key (`Destinations.kt` iki
  liste + guard birlikte)+DI+`App.kt` entry; `onOpenReceipt` paramları.
  **Bitti:** uygulama kurulur, Geçmiş satırları dokunuşsuz.
- [ ] **E6 — Capture (fiş) gider.** `Capture` nav key, `CaptureRoute/Screen/
  ViewModel`, `ReceiptScanner*` silinir; `CameraSurface`+`CaptureController`
  **kalır** (geçici referanssız). **Bitti:** uygulamada kamera girişi yok.
- [ ] **E7 — ListViewModel temizliği.** `receiptDao`/`processor`/
  `attachReceiptToTrip` + pending-işleme init bloğu çıkar.
  **Bitti:** `ListViewModel.kt` sıfır fiş importu.
- [ ] **E8 — Geçmiş ameliyatı.** Parça satırları, durum ikonları,
  `physicalReceipts`, tutar okuması çıkar; `LastTrip` tutarsız kalır (geçici,
  E18 geri getirir). **Bitti:** Geçmiş = tarih + kalem sayısı.
- [ ] **E9 — ListRepository temizliği.** `receiptDao` + `enqueueReceipt` çıkar.
  **Bitti:** repo yapıcısı fişsiz.
- [ ] **E10 — purchaseEvents tek kaynak.** *(cihazsız)* UNION'ın fiş kolu
  silinir — liste kanıtı belgelenmiş fallback. **Bitti:** stats testleri
  fikstürsüz yeşil.
- [ ] **E11 — `data/receipt/` yıkımı + şema v5.** Bilinçli büyük commit
  (Room DAO SQL'i entity listesine karşı derler, bölünemez): yedi pipeline
  dosyası + `ReceiptReader.*` + kalan fiş testleri; `Receipt`+`ReceiptLine`
  entity+DAO; DataWipe fiş sorguları; `Trip.totalMinor`;
  `PriceObservation.brand` girer / `receiptLineId` çıkar; `Migration4To5Spec`
  (`@DeleteTable`×2 + `@DeleteColumn`×2, SQL'siz); `5.json` + baseline hash.
  **Bitti:** testler yeşil VE cihazda v4→v5 göçü (`pm clear` YOK) veri
  kaybetmeden geçer.

### E-C · Etiket akışı (inşa)

- [ ] **E12 — Etiket ölçümü.** *(paralel iz, Faz 0 disiplini)* ~10 gerçek
  etiket (en az BİM/A101/Migros), ham OCR çıktısı `commonTest` fikstürü
  olarak commit'lenir. Sentetik örnek YASAK — ders arşivde kayıtlı.
  **Bitti:** "kuruş üstsimgesi bölünüyor mu" kanıtla cevaplı.
- [ ] **E13 — Mağaza tohumu.** BİM, A101, ŞOK, Migros, CarrefourSA, File,
  Tarım Kredi bootstrap'te (IGNORE, idempotent); `lastUsedStoreId`;
  karar-11 KDoc revizyonu. **Bitti:** temiz kurulumda Ayarlar'da 7 market.
- [ ] **E14 — TagReader + TagParser.** *(cihazsız)* `expect readTag` + tek
  geçiş ML Kit + `groupVisualRows`; fiyat = en büyük glifli para deseni;
  birim fiyat satırından `priceUnit`/`packSize`; marka = ilk kelime önerisi.
  **Bitti:** her fikstür doğru fiyatı veriyor.
- [ ] **E15 — TagCapture ekranı.** Nav key; kamera + onay kartı
  (fiyat/ürün/marka/market/tarih); VM: alias + gözlem yazar, fotoğrafı siler,
  kameraya döner (seri çekim); Liste'ye giriş düğmesi.
  **Bitti:** cihazda 1 etiket → 1 dokunuş → 1 gözlem satırı → listede
  "Tahmini sepet" İLK KEZ görünür.
- [ ] **E16 — priceHint JOIN'i.** *(eski F5.2)* `observeList`'e iki
  correlated-subselect (son+önceki gözlem) + store; projeksiyon kolonları;
  `toUiRow` dört dal (`None`/`Single`/`Trend`/`PackChanged`).
  **Bitti:** 2 gözlemli ürün satırında delta çipi.
- [ ] **E17 — Ekran 5 fiyat bölümü.** *(eski F5.3)* `history(productId, 9)`;
  0/1/9 gözlem hâlleri; mağaza+marka satırları — "BİM · Dost · 100 TL /
  Migros · Pınar · 130 TL" örneği birebir burada.
  **Bitti:** yoğurt örneği üç mağaza satırı olarak çizilir.
- [ ] **E18 — `~` tahminleri.** `LastTrip`/`HistoryTrip` tutarları
  gözlemlerden, tilde ile. **Bitti:** başlık "Son alışveriş: dün · ~642 TL".
- [ ] **E19 — Tasarım dokümanı revizyonu.** `10-tasarima-pivot.md`'nin
  cevabıyla karar defteri güncellenir, ölen ekranlar silinir.
  **Bitti:** hiçbir tasarım dokümanı var olmayan ekranı anlatmıyor.

**Durak noktaları:** E-B'nin her adımından sonra uygulama "fiş özelliğini
kaybetmiş çalışan liste uygulaması"; E15'ten sonra pivot canlı; E16–E18
kazanç artışları.

**Açık belirsizlikler:** kuruş üstsimgesinin OCR'da nasıl döndüğü (E12
çözer); elde çekimde yön gerekirse git geçmişinden yalnızca `pickRotation`
diriltilir, şerit makinesi asla; fotoğrafın kayıttan sonra silinmesi öneri —
E15'ten önce ucuz geri dönüş.

---

## Pivot sonrası işler (kimlikleri korunur, ayrıntı arşivde)

- [ ] **F0.4 — Kanonik ürün kimliği** *(cihazsız)* — marketfiyati fuzzy match;
  F5.4/F5.5'i besler.
- [ ] **F2.7 — Katalog yeniden tohumlanabilir olmalı** — F0.4 ve F3.9'u açar.
- [ ] **F3.9 — "Diğer" kategorisi.**
- [~] **F3.3 / F3.4 — Hızlı ekleme / pano** — cihaz doğrulaması bekliyor.
- [ ] **F5.4 — marketfiyati entegrasyonu** — çevrimdışıysa blok sessizce yok
  olur; `HttpClient` sıfırdan.
- [ ] **F5.5 — "Başka markette ucuz" çipi** — liste başına en fazla 3.
- [ ] **F5.7 — Ambalaj boyu çıkarımı** — E14 etiketin birim-fiyat satırından
  `packSize` çıkarır; kalan iş `PackChanged`'in beslenmesini tamamlamak.
- [ ] **F5.10 — Mükerrer gözlem koruması** *(cihazsız)* — senaryo artık "aynı
  etiketi iki kez çekmek"; kural yeniden yazılacak.
- [ ] **F5.11 — İki küçük biçimlendirici** — tam TL + göreli gün; tahmin
  yuvarlar, gerçek tutar asla (artık bütün tutarlar tahmin — `~` zorunlu).
- [ ] **F6.4 — Eksik Olabilir (Ekran 3)** — "son ödenen" kolonu artık
  gözlemden. · [ ] **F6.5 — Sabit terfisi + bastırma.**
- [ ] **Faz 7 — Senkron** (7.1–7.6) — açık karar #8 (fiş foto senkronu) düştü;
  `syncPhotos` kolonu E11'de ölmedi ama anlamsızlaştı, Faz 7'de kaldırılır.
- [ ] **Faz 8 — Marka varlıkları** (8.1–8.6) — logo konsegi C ("Fişin
  Kuyruğu") elenmeli, fiş artık ürünün parçası değil.
- [ ] **Faz 9 — iOS** (9.1–9.5) — 9.2 artık "iOS **etiket** hattı":
  `downscaleForOcr`, `readTag` actual'ları.
- [ ] **Faz 10 — Sürekli** — 10.2, 10.5, 10.7–10.12 duruyor; 10.6 artık
  yalnız History; 10.13 kapandı (VisualRows commonTest'te); 10.4'ün fiş
  iddiaları düştü, `03-arastirma-bulgulari.md` başına arşiv notu yeter.
- [ ] **F11.4 / F11.6 — Tasarım primitifleri / alışveriş modu container'ı.**

---

## Kurallar ve dersler (pivottan bağımsız, kalıcı)

**Şema kuralı:** `execSQL` commonMain'de yok → göçler tamamen otomatik kalır;
yeni NOT NULL kolon `defaultValue` taşır, gerisi nullable. Nöbetçi:
`SchemaBaselineTest` (v1–v4 hash'leri + `<n>.json` varlığı; E11 v5 ekler).
Boş tablonun şema hatası bedava — bump, ilk yazandan önce gelir.

**Altı sessiz hata sınıfı** (tamamı arşivdeki "Öğrenilenler"de örnekli):
kendi örneğiyle kendini onaylama · ısırdığı kanıtlanmamış test test değildir ·
kelime sınırsız önek eşleşmesi · SQL dizgisi koddur · locale'siz harf dönüşümü
· ekranda görünmeyen "bitti" değildir.

**Riskler:** ölçek riski YOK ve bu bir karar · Türkçe yerelleştirme değil
doğruluk kısıtı · takvim günü ≠ 86.4M ms bloğu · (yeni) **etiket çekim yükü**
— değer eğrisi artık "kullanıcı kaç etiket çekerse ayakta kalır" sorusuna
bağlı; E15'in seri çekim akışı bu riskin ilk cevabı, ölçümü Faz 7 öncesi.

**Açık kararlar:** fiyat gözlem birimi (paket mi kg mi — E14'te `priceUnit`
ikisini de taşır, gösterim kararı E17'de) · katalog fiyatı ile ödenen fiyat
aynı tabloda mı (F5.4) · blok listesi olay mı tablo mu (F6.5) · hane yeniden
anahtarlama (Faz 7) · etiket fotoğrafı kayıttan sonra silinsin mi (E15).

## Bayat adlar — harita ≠ kod

| Haritada/dokümanda | Kodda |
|---|---|
| fiş çekme akışı | `TagCapture` (E15'ten sonra) |
| `ReceiptReader` | `readTag` (E14'ten sonra) |
| `attachReceiptToTrip` | yok — gözlem geziye bağlanmaz (karar 3) |
| `Fiş Kontrol` | yok — onay kartı `TagCapture`'ın içinde |
| eski tablo: `ARSIV-fis-donemi.md` "Bayat adlar" | değişmedi, geçerli |

## Kod TODO eşlemesi

`sheet-yuksekligi`→F10.5 · `kategori-tonlari`→F6.9(✅ arşiv) · `splash`→F8.4 ·
`ios-statusbar`→F9.3 · `ios`→F9.3 · `tnum`→F9.4 — fiş TODO'su yok, E11
sırasında çıkan olursa buraya yazılır.

## İlgili dokümanlar

- [ARSIV-fis-donemi.md](ARSIV-fis-donemi.md) — pivottan önceki tam harita
- [10-tasarima-pivot.md](10-tasarima-pivot.md) — tasarıma pivot anlatımı + yeni yüzey istekleri
- [08-tasarim-bulgulari.md](08-tasarim-bulgulari.md) · [09-tasarima-sorular-3.md](09-tasarima-sorular-3.md) — fiş dönemi bulguları (arşiv değeri)
- [05-tasarim-denetimi.md](05-tasarim-denetimi.md) · [06](06-tasarima-sorular.md) · [07](07-tasarima-sorular-2.md) — önceki turlar
- [03-arastirma-bulgulari.md](03-arastirma-bulgulari.md) — ⚠ fiş iddiaları geçersiz, arşiv notuyla okunmalı
- `tasarim/` — ekran tasarımları + karar defteri + devir paketi
