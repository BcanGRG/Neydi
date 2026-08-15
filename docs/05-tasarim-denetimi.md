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
| 6 | Girdinin solunda `add` ikonu | yok | ✅ **düzeltildi** |
| 7 | "Her zamankiler" bölümünde `push_pin` ikonu | yeşil nokta | ✅ **düzeltildi** — nokta "sabit" anlamını taşımıyordu |
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

## Devir paketinin kapattığını söylediği eksikler — incelendi

`handoff/README.md` sekiz eksik sayıyor. **Sekizi de kodda var**: `Modifier.pressable`, `Modifier.focusRing`, `SafeArea`, `Elevation`, `NeydiExtraShapes`, `AccentSurface`, `SpacingExtra`, `Motion.settle`. Dosya farkları (Motion 173, AccentChip 178 satır) yalnızca isim ve önizleme farkı; API yüzeyleri aynı.

**Asıl bulgu başka:** beşi **tanımlı ama hiçbir yerde kullanılmıyordu** — `Category.tintArgb`/F6.9 ile aynı sınıf hata. Bunlardan `Elevation.floatingToolbar` alışveriş çubuğuna bağlandı; `focusRing`, `AccentSurface` ve `AccentStrip` hâlâ açık (ROADMAP F11.4). `SafeArea` için sapma bilinçli: ekranlar sabit 44/34dp yerine gerçek `WindowInsets.safeDrawing` kullanıyor — Android'de doğrusu bu.

## Yapılanlar ve sıradaki iş

**15 Ağustos'ta kapananlar:** ikon seti (11.1), Ekran 1 yapısal sapmaları (11.2), boş durumlar (11.3), Ekran 6 Geçmiş + mini grafik, F6.9 kategori tonları, F6.7 Ayarlar, F6.4 Ekran 3, Ekran 2'nin başlık/sayaç/grid farkları.

**Sıradaki:**
1. Ekran 2'nin arama alanı (işlevsel eksik) ve `check_circle`
2. Ekran 4 — tutarı manşet yapmak, "Eksik satır ekle", aday sheet'i
3. `focusRing` / `AccentSurface` / `AccentStrip` bağlantısı (F11.4)
4. Ekran 8 Kurulum (F6.6) — 1. adım auth'a bağlı, 2–3. adımlar yazılabilir
5. Ekran 5 Ürün Detayı — Faz 5'e bağlı

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

