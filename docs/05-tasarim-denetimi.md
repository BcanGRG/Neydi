# Tasarım denetimi — kod ↔ tasarım sadakati

**15 Ağustos 2026.** Kaynak: `docs/tasarim/` (bu denetimle birlikte repoya alındı).

## Önce: kayıp sanılan tasarım kaynağı bulundu

Devir paketinin `handoff/README.md`'si ve `Neydi-Tasarim/OKU-BENI.md` *"Claude Design projesi burada YOK"* diyordu ve şunu not düşmüştü:

> Ekran 3 / 5 / 7 / 8 hâlâ yazılmadı ve onları spec metninden değil gerçek tasarımdan üretmek sadakati ciddi biçimde değiştirir.

Proje kaynağı **duruyormuş** — `Neydi Tasarım.zip` içinde. Artık `docs/tasarim/` altında:

| dosya | içerik |
|---|---|
| `Neydi - Tasarim Sistemi.dc.html` | palet, tipografi, bileşen halleri |
| `Neydi - Ekran 1 Liste.dc.html` | Liste: üç mod, boş haller, mod farkları |
| `Neydi - Ekranlar 2-4.dc.html` | Ekle sheet, Eksik Olabilir, Bitir |
| `Neydi - Ekranlar 5-8.dc.html` | Ürün Detayı, Geçmiş, Ayarlar, Kurulum |
| `Neydi - Bos Durumlar.dc.html` | boş durum katalogu |
| `Neydi - Compose Spec.dc.html` | Compose'a çeviri notları |
| `handoff/tokens.json` | **tasarım tarafındaki tek gerçek kaynak** |

Yani **Ekran 3, 5, 7 ve 8'in gerçek tasarımı artık elimizde.** F6.4, F6.6, F6.7 ve Faz 5'in ekran işleri spec metninden değil bunlardan üretilmeli.

## Temiz çıkanlar

`handoff/README.md`'nin "Kod incelemesi listesi"ndeki mekanik kuralların **hepsi** geçti — kodda tek ihlal yok:

- `displayFamily` yalnızca `Type.kt` içinde; Fraunces 24sp altında hiçbir yerde geçmiyor
- `background(extras.accent)` yalnızca `AccentChip.kt` içinde — amber kenarlık sözleşmesi kaçırılamıyor
- `Modifier.clickable` **sıfır** kullanım; her etkileşim `Modifier.pressable`
- `uppercase()` / `capitalize()` yok
- `0.5.dp` yok
- `Modifier.blur`, `ambientColor`, `spotColor` yok
- `dynamicColorScheme` yok
- `AlertDialog`, `BadgedBox`, `NavigationBar` yok

Palet, tipografi ve ölçü token'ları da `tokens.json` ile birebir (`Color.kt`, `Dimens.kt`, `Type.kt`, `Theme.kt` devir paketindeki halleriyle byte-byte aynı).

## Ekran 1 — Liste: yapısal sapmalar

Uygulamanın sürenin %90'ının geçtiği ekranı, ve sapmaların çoğu burada.

| # | Tasarım | Kod | Durum |
|---|---|---|---|
| 1 | Gezinme `more_vert` taşma menüsünde | yatay kaydırmalı çip şeridi | ✅ **düzeltildi** — şerit kaldırıldı, taşma menüsü geldi |
| 2 | Altta birincil buton **"Alışverişe çıkıyorum"** | yok | ✅ **düzeltildi** — girdinin altında, tasarımdaki sırada |
| 3 | Başlık alt satırı *"Son alışveriş: 8 gün önce · 642 TL"* | *"N ürün"* | ✅ **düzeltildi** — `lastTripSummary`, 5 test |
| 4 | Başlık 22sp/700 (`title22`) | `headlineMedium` = 24sp | ✅ **düzeltildi** — iki punto büyüktü |
| 5 | `size/header = 56` token'ı | kodda yok | ✅ **eklendi** — `SizesExtra.header` |
| 6 | Girdinin solunda `add` ikonu | yok | açık |
| 7 | "Her zamankiler" bölümünde `push_pin` ikonu | yeşil nokta | açık |
| 8 | Öneri şeridi: *"Yumurta · 14 gün oldu"* + *"+3 öneri"* | F6.3 yazıldı, şerit var | veri yokken çizilmiyor — doğru davranış, "+N öneri" çipi eksik |

Cihazda doğrulandı: başlık *"Son alışveriş: bugün"* (tutar okunamadığı için yazılmıyor — dürüstlük kuralı), `⋮` menüsü `surfaceVariant` zemin + 24dp kart köşesiyle açılıyor, birincil buton en altta.

**1 ve 2 aynı kararın iki yüzü.** Koddaki yorum çip şeridinin nasıl doğduğunu anlatıyor: *"üç buton ekrana sığmıyordu ve 'Ayarlar' sağ kenarda kesiliyordu… Geçmiş butonu eklenince dörde çıktı"*. Tasarımın **aynı soruna** cevabı zaten var ve farklı: birincil aksiyon (alışverişe çık) altta tek başına, ikincil gezinme `more_vert` içinde. Yani şerit bir çözüm değil, tasarımın çözdüğü sorunun yeniden çözülmüş hali.

Tasarımın kendi gerekçesi:

> Tüm birincil aksiyonlar ekranın alt %40'ında; floating toolbar'da her hedef 56dp.

Çip şeridi birincil aksiyonu **üstte** tutuyor — tek elle kullanımda erişilmesi en zor yerde.

## Henüz yazılmamış ekranlar — artık tasarımı var

| Ekran | Roadmap | Tasarımda ne var |
|---|---|---|
| **3 — Eksik Olabilir** | F6.4 (açık) | üç bölüm, asimetrik varsayılanlar, `[Ekle (4)]` + `[Boşver]` |
| **5 — Ürün Detayı** | Faz 5 | genişleyebilen sheet, üç veri durumu, ambalaj küçülmesi kırığı, "Nerede ucuz" |
| **7 — Ayarlar** | F6.7 (açık) | Hane / Her zamankiler / Önerilmeyenler / Mağazalar / Gizlilik; boş bölüm hiç çizilmez |
| **8 — Kurulum** | F6.6 (açık) | 3 adım, "Atla", tempo çipleri, "Devam (9 seçildi)" |

Ekran 5'in tasarımı özellikle değerli: F5.7'nin (ambalaj küçülmesi) görsel sözleşmesi tam yazılmış — *"ambalaj kırılmasında dikey kesikli çizgi, iki dönem ayrı polyline; tek çizgi çizmek 5 L ile 4 L'yi aynı ürün gibi göstermek olurdu"*.

## Devir paketinin kapattığını söylediği eksikler

`handoff/README.md` sekiz eksik sayıyor ve üçü hâlâ kodda karşılıksız görünüyor — `Motion.kt` (173 satır), `AccentChip.kt` (178 satır) ve `Dimens.kt` (6 satır) devir paketindeki hallerinden farklı. Farkların hangi yönde olduğu (repo mu ilerlemiş, tasarım parçası mı düşmüş) **henüz incelenmedi** — bu denetimin bir sonraki adımı.

## Sıradaki iş

1. Ekran 1'in 1–5. maddeleri (yapısal, küçük, hemen görünür)
2. `Motion.kt` / `AccentChip.kt` / `Dimens.kt` fark incelemesi
3. Ekran 2 · 4 · 6 sadakat denetimi (yazılmış ekranlar)
4. Ekran 3 / 5 / 7 / 8 — artık gerçek tasarımdan üretilebilir

## Ekran 2 — Ekle sheet

| Tasarım | Kod | Durum |
|---|---|---|
| Başlık **"Ekle"** 18sp/700 | "Ne ekleyelim?" | ✅ düzeltildi |
| **"N ürün eklendi"** sayacı | yok | ✅ eklendi (sheet oturumu başına) |
| Grid **3 sütun sabit** | `GridCells.Adaptive(84dp)` | ✅ düzeltildi |
| Sheet içinde **arama alanı** (`search` + "Ürün ara", 48dp/18dp) | yok | açık |
| Eklenmiş ürünlerde **`check_circle`** | yok | açık |
| **"Nadir aldıkların"** bölümü | yok | açık |
| Kaçış satırı: `add` + *"'kuru kayısı' ekle"* | "Listede yok, kendim yazayım" (buton) | açık — tasarım aranan kelimeyi metnin içine koyuyor |

Arama alanı işlevsel bir eksik: bugün arama yalnızca alttaki hızlı ekleme çubuğunda var, yani kullanıcı sheet'i kapatmadan arayamıyor.

## Ekran 4 — Alışverişi bitir · Fiş kontrol

**Adım 0 (seçim sheet'i + kamera):**

| Tasarım | Kod |
|---|---|
| Başlık "Alışverişi bitir" + açıklama *"Fiş fotoğrafı fiyat geçmişini besler. Elle fiyat yazman gerekmez."* | açıklama yok |
| `photo_camera` + **"Fiş çek"** birincil | var, ikon yok |
| **"Fişsiz bitir"** ikincil · **"Vazgeç"** | var |
| Kamera overlay rehberi: *"Fişin tamamı kadraja girsin. Uzunsa 2 kare çek."* + "1. kare" sayacı | sistem kamerası kullanıldığı için overlay **konamıyor** — F4.13 bunu çekimden önceki metne taşımış (bilinçli sapma) |

**Mod A (Fiş kontrol):**

| Tasarım | Kod | Not |
|---|---|---|
| `arrow_back` + "Fiş kontrol" başlığı | mağaza adı başlık | tasarım ekranın adını, kod fişin mağazasını yazıyor |
| Alt satır: *"Migros Ataşehir · 12 Ağustos 15:31"* | yok | mağaza + basılı tarih birlikte |
| **Büyük toplam** (Fraunces 36sp) + `check_circle` "Toplam tutuyor" | pill çip, küçük | tasarım tutarı manşet yapıyor |
| Satır sonunda `chevron_right` | yok | |
| *"Listede vardı, fişte yok (3)"* + `expand_more` katlanır | var, katlanır değil | |
| `add` **"Eksik satır ekle"** | yok | fişte olmayan satırı elle eklemek |
| **"Onayla ve kaydet"** | "Tamam" | |
| Yanlış eşleşme için **aday sheet'i** (*"Bu satır hangi ürün?"* + "en olası" + `search`) | 3-dokunuş düzeltme sheet'i var | tasarımın 2 dokunuşu hedefliyor |

Fiş kontrol ekranı bugün **çalışıyor ve cihazda doğrulanmış** (F4.6); yukarıdakiler sadakat farkları, işlev kaybı değil.

