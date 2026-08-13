# Neydi — Yol Haritası

Tek gerçek kaynak. 11 faz, 67 adım. Her adım bir PR.

**İlerleme:** 2 / 67 — *F0.1 tamamlandı. Sırada F0.2: fişleri koşuma vermek (kimlik doğrulama gerekiyor).*

---

## Çalışma sözleşmesi

| Kural | Detay |
|---|---|
| **Dal adı** | `faz<N>/<kisa-slug>` — örn. `faz1/fontlari-bundle-et` |
| **PR başlığı** | `[F1.1] Fontları bundle et` — faz.adım numarası zorunlu |
| **PR içeriği** | Kod **+** bu dosyadaki ilgili kutunun `- [x]` yapılması. Aynı PR'da, ayrı commit'te değil — yoksa harita koddan sapar. |
| **Kapı 1** | `./gradlew :composeApp:assembleDebug` yeşil. Değilse PR açılmaz. |
| **Kapı 2** | `./gradlew :composeApp:installDebug` — bağlı telefonda APK güncellenir ve değişiklik **gözle doğrulanır**. Cihazda görülmeyen bir şey "bitti" sayılmaz. |
| **PR açıklaması** | Cihazda ne görüldüğü tek cümleyle yazılır. Görsel değişiklik varsa ekran görüntüsü eklenir. |
| **Merge** | Kullanıcı yapar. PR açık, yeşil ve cihazda doğrulanmış bırakılır. |
| **Kod TODO'ları** | Bir TODO kapandığında hem koddan silinir hem burada işaretlenir. |
| **graphify** | post-commit hook kod değişikliklerinde grafı günceller. `docs/` değişince manuel `/graphify --update`. |

### İşaretler

- `- [ ]` yapılmadı · `- [x]` tamamlandı ve merge edildi · `- [~]` kod tamam, cihaz doğrulaması bekliyor
- `(cihazsız)` — telefonda görüntülenemeyen adım, Kapı 2'den muaf

### Cihaz döngüsü

```bash
./gradlew :composeApp:installDebug
```

Ekran görüntüsü: `adb exec-out screencap -p > shot.png`
`adb` yolu: `C:\Users\buroc\AppData\Local\Android\Sdk\platform-tools\adb.exe`

---

## Faz 0 — Risk azaltma (kod yazmadan önce)

> Araştırmanın "tek satır Kotlin yazmadan önce yapılacak en değerli iş" dediği yer. **Ürünün yarısının kapsamı buranın sonucuna bağlı.** Fiş satır adları güvenilir çıkmazsa Neydi bir fiyat takipçisi değil, harcama defteri olur — ve bunu 4. ayda değil şimdi öğrenmek gerekiyor.

- [x] **0.0 — Cihaz kurulumu.** ✅ Samsung Galaxy S10+ (SM-G975F), **Android 12 / API 31**, 1080×2280 @ 420dpi. `installDebug` çalışıyor, uygulama crash'siz açılıyor, açık ve karanlık mod referans görüntüleri alındı. Tema doğrulandı: açık modda krem `#FBF7F2` + terracotta `#B34418`, karanlık modda `#13100E` + somon `#FF9166`. **İki hata yakalandı → F1.6.** (API 31 ayrıca F8.4 için önemli: Android 12+ SplashScreen API'si bu cihazda geçerli.)
- [x] **0.1 — Fiş test koşumu** *(cihazsız)*. ✅ `tools/receipt-eval/` — TypeScript (Node 24, derleme adımı yok; üretimdeki Cloudflare Worker de TS olacağı için istek şekli birebir aynı). Türkçe fiş prompt'u + structured output şeması + ad/fiyat **ayrı** skorlama + aritmetik kapısı. API şekli `claude-api` skill'inden doğrulandı ve **üç varsayımım yanlış çıktı**: (a) Claude Opus 5'te thinking **varsayılan açık**, ve `disabled` yalnızca `effort` ≤ `high` ile kabul ediliyor — `xhigh`/`max` ile 400; (b) structured output `output_config.format` ile veriliyor, `output_format` kullanımdan kalkmış; (c) şema `minimum`/`minLength` kabul etmiyor ve nullable alanlar `anyOf` ile yazılmak zorunda.
- [ ] **0.2 — İlk ölçüm** *(cihazsız)*. Eldeki 2 fişi (farklı zincir) çalıştır. **Satır adı doğruluğunu fiyat doğruluğundan ayrı skorla**, zincir bazında raporla. Ad alanı, karşılaştırma özelliğinin ihtiyaç duyduğu ve başarısız olması beklenen alan.
- [ ] **0.3 — Aritmetik değişmez** *(cihazsız)*. `Σ(satır) − İNDİRİM = TOPLAM`, ±0,05 TL tolerans (tartılı ürün yuvarlaması). Araştırmanın ilk yazdığı `+KDV` formülü **yanlıştı** — Türkiye'de perakende fiyatları kanunen KDV dahil, TOPKDV toplamın içindeki verginin dökümü. O formül %100 fişi manuel düzeltmeye yollardı.
- [ ] **0.4 — Kanonik ürün kimliği** *(cihazsız)*. Çıkan satır adlarını marketfiyati `/api/v2/search` ile bulanık eşleştir. `User-Agent` header'ı zorunlu — yoksa endpoint 404 döner. Barkod yolu **yok**: `searchByIdentity` gerçek EAN-13'lerde 6/6 boş döndü, katalogda barkod alanı hiç bulunmuyor.
- [ ] **0.5 — Kapsam kararı** *(cihazsız)*. Fiyat takipçisi mi, harcama defteri mi? Sonucu `docs/03-arastirma-bulgulari.md`'ye ek olarak yaz ve **Faz 5'in kapsamını buna göre daralt veya genişlet.**

## Faz 1 — Temel borçlar

- [ ] **1.1 — Fontları bundle et.** Plus Jakarta Sans variable (wght ekseni) + Fraunces **statik instance** (`opsz 72 · SOFT 30 · WONK 0 · wght 600`, `fontTools varLib.instancer` ile üretilir). Variable olarak bundle edilmez: CMP'de `FontVariation.Settings` iOS'ta güvenilir değil ve sessizce `opsz 14` varsayılanına düşer, 44sp'de yanlış görünür. → `Type.kt` TODO(font) kapanır.
- [ ] **1.2 — Kontrast birim testi** *(cihazsız)*. `Color.kt` token'ları üzerinden WCAG oranlarını hesaplayan test. Amber kuralı (accent ışık modunda 2.08:1, kenarlık zorunlu) regresyona karşı kilitlenir.
- [ ] **1.3 — CMP deprecation temizliği.** `compose.runtime` / `foundation` / `material3` / `components.resources` / `preview` kısayolları yerine katalogdan açık koordinatlar. Şu an 7 deprecation uyarısı üretiyor.
- [ ] **1.4 — Nav3 saveable back stack.** `rememberNavBackStack` + `SavedStateConfiguration` + polymorphic `SerializersModule`. iOS ve web'de reflection tabanlı serialization yok; bu yapılmazsa iOS'ta "back stack restore olmuyor" diye sessizce tezahür eder. → `App.kt` TODO(saveable) ve `Destinations.kt` TODO(ios-serialization) kapanır.
- [ ] **1.5 — `Modifier.pressable` zorunluluğu.** `Placeholders.kt`'deki ham `clickable`/`Button` kullanımlarını taşı. Ripple'ı global olarak kaldırdığımız için basılı hal sözleşmesi tek yerden uygulanmalı.
- [ ] **1.6 — Edge-to-edge sistem çubukları.** *(F0.0'da cihazda bulundu.)* `enableEdgeToEdge()` çağrılıyor ama iki sorun var: **(a)** karanlık modda status bar ikonları koyu kalıyor, siyah zemin üstünde neredeyse okunmuyor; **(b)** alt gezinme çubuğu şeridi karanlık modda açık renk kalıyor, ekranın altında beyaz bant bırakıyor. `SystemBarStyle` temaya bağlanacak. Bu iş iOS'ta karşılığı olmayan bir alan — `preferredStatusBarStyle` common koddan set edilemiyor (bkz. F9.3), o yüzden çözüm baştan platform-ayrık kurgulanmalı.

## Faz 2 — Veri katmanı

- [ ] **2.1 — Room 3.0.1 kurulumu.** `androidx.room3` (2.x **değil** — koordinat değişti, eski tutorial'lar yanlış import ettirir) + KSP2 + `sqlite-bundled` 2.7.0. KSP sürümü kurulum sırasında doğrulanır.
- [ ] **2.2 — Entity'ler.** `Household`, `Member`, `Category`, `CatalogSeed`, `Product`, `Store`, `Trip`, `TripLine`, `Receipt`, `ReceiptLine`, `ProductAlias`, `PriceObservation`, `ProductStats`, `SuggestionEvent`, `PendingOp`, `SyncMeta`.
- [ ] **2.3 — Kritik kısıtlar.** `TripLine` üzerinde `UNIQUE(tripId, productId)` — iki eş de ekmek eklerse istatistik katmanı **tek** satın alma görmeli. `ProductAlias` üzerinde `UNIQUE(householdId, storeChain, rawTextNormalized)`. Her satırda `householdId` (RLS için). Hard delete yok, `deletedAt` tombstone. id = UUID v7.
- [ ] **2.4 — `matchKey` normalizasyonu + testi.** Diacritic-folded, **locale-explicit** lowercase. `lowercase()` locale'siz çağrılırsa noktalı/noktasız İ ürünü sessizce ikiye böler ve fiyat geçmişini yok eder.
- [ ] **2.5 — Bundled Türk katalog.** ~250 ürün + 12 kategori taksonomisi, `commonalityRank` ile. Tek hane = hiç işbirlikçi sinyal yok, yani bu **tek** soğuk-başlangıç mekanizması. Faz 0.4 çıktısına göre marketfiyati'den tohumlamayı değerlendir.
- [ ] **2.6 — DAO + repository katmanı.** Flow tabanlı, offline-first.

## Faz 3 — Liste ekranı (uygulamanın kalbi)

> Uygulamada geçirilen sürenin %90'ı burada. Tek mükemmel olması gereken ekran.

- [ ] **3.1 — Bileşen kütüphanesi.** Liste satırı (normal / sabit / işaretli / eş-eklemiş), `PriceChip`, delta çipi, Canvas sparkline (24×16dp), öneri çipi, kategori başlığı, kategori kutucuğu + **iki-harf fallback**. Fallback'i **önce** yap — öğelerin %80'i onu gösterecek.
- [ ] **3.2 — Planlama modu.** Bölümler, reyon gruplama, "Her zamankiler" (%70 opaklık + raptiye — kullanıcı eklediklerinden görsel olarak hafif olmalı ki "uygulama ağzıma laf koydu" hissi vermesin), "Alındı" bölümü.
- [ ] **3.3 — Hızlı ekleme.** Alta sabit alan, **skora göre sıralı** otomatik tamamlama (alfabetik **değil** — algılanan zekânın ~%40'ı bu, ve sıfıra mal oluyor), satırda son ödenen fiyat, inline miktar ayrıştırma (`2 kg elma`).
- [ ] **3.4 — Pano yapıştırma.** Panoda 3+ satır varsa tek dokunuşluk çip. Mevcut WhatsApp akışını doğrudan değiştiren en ucuz 1. gün kazanımı.
- [ ] **3.5 — Alışveriş modu.** 72dp satır, 20sp/700 ad, **reyon sırası donar** (hareket eden başparmağın altında yeniden sıralama en kötü hata), ekran uyanık kalır, floating toolbar alt %40'ta, haptik onay, **işaretlemede snackbar yok** (bir gezide 20 işaretleme var).
- [ ] **3.6 — Üç boş durum.** 1. gün kurulumlu · 1. gün kurulum atlandı (12 çip + pano butonu) · döngü ortası (uygulamanın hayatının çoğunu geçirdiği hal — ölü hissettirmemeli).
- [ ] **3.7 — Ekle sheet (Ekran 2).** Kategori grid, skora göre sıralı ürün çipleri, serbest metin kaçış yolu. Sheet, ekran değil — liste arkada görünür kalsın.
- [ ] **3.8 — Sepet tahmini + özet kartı.** Sabit "Tahmini sepet" satırı; alışveriş sonrası tek seferlik özet kartı (tutar 36sp Fraunces). Duygusal karşılık ve ekran görüntüsü anı burası.

## Faz 4 — Alışveriş kapatma ve fiş

> Verinin dürüst kalmasını sağlayan faz. Ekmek problemini asıl çözen katman burası: fiş, listeye yazılmasa bile ekmeği içeriyor.

- [ ] **4.1 — Trip yaşam döngüsü.** `PLANNING → SHOPPING → CLOSED` + `ownerMemberId`. **Tek cihaz kapatır** — iki cihaz aynı gezinin mutabakatını yaparsa satın almalar çift sayılır ve her aralık tahmini yarıya iner.
- [ ] **4.2 — Kamera.** FileKit 0.14.2 (Peekaboo 28 aydır ölü, CameraK Maven'da 0 artifact). Fotoğraf **asla bloklamaz**: yerel kaydet, kuyruğa al, alışveriş anında kapansın — kullanıcı kasa kuyruğunda spinner beklemez. Görsel **platform tarafında** küçültülür (long edge ≤ 2576px); CMP iOS'ta UIImage/kamera frame bellek baskısı bilinen sorun.
- [ ] **4.3 — Cloudflare Worker proxy** *(cihazsız)*. API anahtarı Worker secret'ında, repoda **asla** değil. Keep-alive cron'u da buraya.
- [ ] **4.4 — Claude vision çağrısı.** JSON şeması + Türkçe fiş prompt'u (virgüllü ondalık, KDV/TOPKDV ürün değil, İNDİRİM satırları, `x,xxx KG × yy,yy TL/KG` ağırlık formu). `thinking` **kapalı**. Structured output şeması 24 saat cache'leniyor; ayda 3 fişte cache **her zaman** soğuk olacak — gecikme hiç iyileşmeyecek, UX'te buna yer bırak.
- [ ] **4.5 — Aritmetik kapı.** 0.3'te doğrulanmış formül. Yeşil "Toplam tutuyor" / amber "Toplam X TL tutmuyor" çipi.
- [ ] **4.6 — Fiş Kontrol ekranı.** Üç satır hali (eşleşti / yeni ürün / emin değil), **ham OCR metni gri alt satır** (kullanıcının kağıt fişi tekrar okumadan doğrulama yolu), 3-dokunuş düzeltme sözleşmesi, **uygulamadaki tek sayısal klavye**.
- [ ] **4.7 — `ProductAlias` öğrenmesi.** Her düzeltme bir alias yazar. Ay 2'yi ay 1'den ölçülebilir biçimde iyi yapan tek mekanizma — herhangi bir benzerlik eşiği ayarından daha değerli. 1. günden itibaren doldur.
- [ ] **4.8 — Mod B (fişsiz mutabakat).** BİM/A101/ŞOK gezileri ve unutulan fotoğraf için. Varsayılan-iyimser kapanış: hiçbir şeye dokunulmazsa planlananlar alındı sayılır. Tembel kullanım da kullanılabilir veri üretmeli.
- [ ] **4.9 — Geçmiş ekranı (Ekran 6).** Yanlış okunmuş bir fişe dönmenin **tek** yolu. Uygulamanın en ucuz ekranı ama kurtarılabilirlik için zorunlu.

## Faz 5 — Fiyat hafızası

> Kapsam **Faz 0.5**'in kararına bağlı. Ölçüm kötü çıkarsa bu faz daralır.

- [ ] **5.1 — `PriceObservation` yazımı.** Fişten, her satırda `packSize` ile. `TripLine`'dan ayrı tutulur ki katalog fiyatları (marketfiyati) ile gerçekten ödenen fiyatlar tek karşılaştırılabilir seride yaşasın, karışmadan.
- [ ] **5.2 — Satır fiyat ipucu.** Üç veri durumu: 0 gözlem → **hiçbir şey** (asla "fiyat yok" yazma) · 1 gözlem → son ödenen · 2+ → trend + delta + sparkline. **Shrinkflation koruması:** ambalaj boyu değiştiyse trend bastırılır, `900g → 800g · aynı fiyat` yazılır. Ambalaj küçülmesi asla fiyat düşüşü gibi görünmemeli.
- [ ] **5.3 — Ürün Detayı sheet (Ekran 5).** 24sp Fraunces manşet cümlesi (okunacak ve ekran görüntüsü alınacak şey grafik değil bu cümle), Canvas çizgi grafik, min/ortalama referans çizgileri, aralık seçici 1 ay / 6 ay / 1 yıl — **"1 hafta" yok**, 10 günlük tempo haftalık çözünürlük taşımaz.
- [ ] **5.4 — marketfiyati entegrasyonu.** `/api/v2/search`, User-Agent zorunlu, agresif cache. Endpoint dokümante değil — **her an kapanabilir kabul et**. "Nerede ucuz" bloğu çevrimdışıyken **sessizce yok olur**, market reyonunda hata mesajı göstermez.
- [ ] **5.5 — "Başka markette ucuz" çipi.** Liste başına **en fazla 3**, mutlak TL tasarrufuna göre sıralı. Üstü listeyi reklam yüzeyine çevirir ve özellik kesilir.

## Faz 6 — Öneri motoru

- [ ] **6.1 — `ProductStats` hesabı.** Trip close'da tek Room transaction'ında **tam** yeniden kurulum (~5ms, 80 ürün × 60 gezi). Asla incremental — türetilmiş cache.
- [ ] **6.2 — Skor formülü.** Sıklık + gecikmişlik + geçen sefer unutuldu mu. `muAdjust` ayrı kolonda tutulur ki denetlenebilir ve sıfırlanabilir olsun.
- [ ] **6.3 — Öneri şeridi.** En fazla 5 çip, **her çip düz Türkçe gerekçe taşır** ("Yumurta · 14 gün oldu"). Gerekçesiz çip reklam gibi okunur. Animasyon yok, badge yok, nokta yok.
- [ ] **6.4 — Eksik Olabilir (Ekran 3).** 3 bölüm; "geçen sefer unuttun" ve "her zamankiler" varsayılan **açık**, tahmin bölümü varsayılan **kapalı** (uygulamanın tahmin yürüttüğü tek yer, varsayılan-açık muamelesi görmez). **Hiçbir şey uygun değilse ekran açılmaz** — boş kontrol listesi kullanıcıya butonun değersiz olduğunu öğretir.
- [ ] **6.5 — Sabit terfisi + bastırma.** Staple promotion kartı, üç-vuruş sessiz otomatik bastırma (hiç "önerme" demeyen kullanıcı için kendi kendini iyileştirir), kalıcı engelleme listesi Ayarlar'da **görünür** — kara delik olmamalı.
- [ ] **6.6 — Kurulum (Ekran 8).** 3 adım, ~40 ürünlük grid, tempo çipi. Var olma sebebi tek: 15. gezide değil **3. gezide** akıllı hissetmek.
- [ ] **6.7 — Ayarlar (Ekran 7).** Hane + katılma kodu, her zamankiler, önerilmeyenler, mağazalar, gizlilik (KVKK — düz dil + "Verilerimi sil").

## Faz 7 — Senkron

- [ ] **7.1 — Supabase projesi + şema + RLS** *(cihazsız)*. `householdId` üzerinden satır düzeyi güvenlik.
- [ ] **7.2 — Auth.** E-posta OTP (6 haneli kod) — magic link **değil**: universal link / App Links yapılandırmasından ve Apple Guideline 4.8'den kaçınır. + 6 karakterlik hane katılma kodu.
- [ ] **7.3 — v1 senkron: Realtime `postgres_changes` + yerel cache, outbox YOK.** ~50 satır. Çevrimdışı düzenleme kaybı bilinçli olarak kabul edilir. İki kişilik, sinyalli bir markette kullanılan liste için yeterli.
- [ ] **7.4 — `updated_at` trigger'ı** *(cihazsız)*. Postgres UPDATE trigger'ı ile, **asla istemciden**. Tek bir istemci timestamp'i LWW'yi kalıcı ve izsiz biçimde bozar.
- [ ] **7.5 — Outbox + tombstone + add-beats-remove.** **Yalnızca gerçekten bir düzenleme kaybı gözlemlenirse.** Erken yapmak 300–500 satırlık tahmini ikiye katlayan fotoğraf-yükleme kuplajını getirir.
- [ ] **7.6 — Keep-alive** *(cihazsız)*. `pg_cron` veya Cloudflare Worker cron. **GitHub Actions ile değil**: 60 gün sessiz repoda zamanlanmış workflow devre dışı kalır → önce keep-alive sessizce ölür, sonra veritabanı duraklar. İki sessiz hata üst üste.

## Faz 8 — Marka varlıkları

- [ ] **8.1 — Logo üretimi.** Recraft ile, `docs/02-logo-splash-prompt.md`'deki 4 konsept. **Tek renkli siluet önce**, renk sonra — Android monochrome ve iOS tinted zaten siluet istiyor.
- [ ] **8.2 — 66dp monochrome testi.** Basitleştirme merdiveni (66dp ve 24dp varyantları). Burada okunmuyorsa konsept ölü, Figma'da ne kadar iyi durduğunun önemi yok.
- [ ] **8.3 — Android app icon.** Adaptive icon (108/72/**66dp garanti daire**) + **monochrome katmanı** (Android 13+ temalı ikonlar için zorunlu) + legacy mipmap'ler + 512×512 Play PNG.
- [ ] **8.4 — Android splash.** `values-v31/themes.xml`: tek düz opak renk (`#FBF7F2`, ilk Compose karesiyle **birebir aynı** olmalı yoksa flaş olur), 288dp canvas / 192dp görünür daire, metin yok, branding image yok. → `themes.xml` TODO(splash) kapanır.
- [ ] **8.5 — iOS ikon varlıkları.** 1024 PNG, **alpha kanalı yok** (otomatik App Store reddi), light/dark/tinted varyantlar. Xcode entegrasyonu Faz 9'da.

## Faz 9 — iOS (Mac gerektirir)

> Windows'ta yapılamaz. Kod baştan doğru yazılıyor; bu faz yalnızca derleme ve doğrulama bekliyor. **Kapı 2 burada iPhone'a dönüşür.**

- [ ] **9.1 — İlk iOS derlemesi.** Simülatörde çalıştırma, Xcode/SwiftPM export ayarı.
- [ ] **9.2 — Kamera native dikişi.** Alan raporları tutarlı biçimde burada SwiftUI'a inildiğini söylüyor — sürpriz olmasın diye bütçelendi.
- [ ] **9.3 — Status bar + safe area.** `preferredStatusBarStyle` common koddan **set edilemez**, barındıran view controller'da ayarlanır. → `MainViewController.kt` TODO(ios) kapanır.
- [ ] **9.4 — Gerçek iPhone doğrulaması.** `tnum` gerçekten uygulanıyor mu (Skia desteklemediği OpenType özelliklerini **sessizce** yok sayabiliyor), variable font eksenleri varsayılana düşüyor mu. → `Type.kt` TODO(tnum) kapanır.
- [ ] **9.5 — TestFlight internal.** ~85 günde bir yeniden yükleme gerekiyor — takvime al. Araştırmanın "projenin durma noktası" dediği yer: 4. ayda ilk TestFlight süresi dolarken grafikler hâlâ boşsa uygulama yeniden yüklemeye değmez hale gelir.

## Faz 10 — Sürekli / refactor

- [ ] **10.1 — AGP 9'a geçiş** *(cihazsız)*. KMP ekosistemi hazır olunca. Şu an `com.android.application` + `org.jetbrains.kotlin.multiplatform` aynı modülde uygulanamıyor.
- [ ] **10.2 — Bottom sheet'leri Nav3 Scene'e taşı.** Şu an ekran state'i; Nav3'ün custom Scene API'si doğru yer.
- [ ] **10.3 — `graph.json` takip kararı** *(cihazsız)*. Mac'e geçişte yeniden değerlendir — merge driver kurulu ama graph.json gitignore'da olduğu için şu an atıl.
- [ ] **10.4 — Araştırma güncellemesi** *(cihazsız)*. Faz 0 sonucunu `docs/03-arastirma-bulgulari.md`'ye işle; çürütülen varsayımları güncelle.

---

## Kod TODO eşlemesi

| TODO | Dosya | Kapatan adım |
|---|---|---|
| `TODO(font)` | `ui/theme/Type.kt` | F1.1 |
| `TODO(saveable)` | `App.kt` | F1.4 |
| `TODO(ios-serialization)` | `nav/Destinations.kt` | F1.4 |
| `TODO(splash)` | `androidMain/res/values/themes.xml` | F8.4 |
| `TODO(ios)` | `iosMain/MainViewController.kt` | F9.3 |
| `TODO(tnum)` | `ui/theme/Type.kt` | F9.4 |

## İlgili dokümanlar

- [`00-isim-onerileri.md`](00-isim-onerileri.md) — isim analizi ve eleme gerekçeleri
- [`01-claude-design-prompt.md`](01-claude-design-prompt.md) — ekran ekran tasarım spec'i, CMP kısıtları
- [`02-logo-splash-prompt.md`](02-logo-splash-prompt.md) — logo konseptleri, ikon/splash teknik gereksinimleri
- [`03-arastirma-bulgulari.md`](03-arastirma-bulgulari.md) — planı değiştiren bulgular, ölçülmemiş varsayımlar
- [`../graphify-out/GRAPH_REPORT.md`](../graphify-out/GRAPH_REPORT.md) — bilgi grafiği raporu
