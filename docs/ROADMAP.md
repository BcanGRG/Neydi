# Neydi — Yol Haritası

Tek gerçek kaynak. 11 faz, 67 adım. Her adım bir PR.

**İlerleme:** 30 / 67 — *Faz 4 bitti. Fiş okuma uçtan uca cihazda çalışıyor: gerçek fiş → OCR → ayrıştırma → aritmetik kapı → düzeltme → alias öğrenmesi. Sırada Faz 6 (öneri motoru).*

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
| **Preview** | Yeni her bileşen `@PreviewLightDark` + `NeydiPreview { }` ile gelir. Preview "çizildi mi" testi değil; satır sıkıştığında neyin feda edildiğini gösteren nöbetçi. |
| **Material3 Surface** | Tıklanabilir Material3 `Surface`/`Button`/`Card` **kullanılmaz** — ripple'ı sabit kodluyorlar ve tema override'ı onlara ulaşmıyor. Etkileşimli her şey `Modifier.pressable` üzerinden. |
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

## Yürütme sırası

> **Numaralar kimliktir, sıra değildir.** `F3.1` her zaman `F3.1`'dir — PR başlıkları ve geçmiş referanslar bozulmasın diye numaralar sabit. Hangi sırayla yapıldıkları bu bölümde yazar ve değişebilir.

**Karar (13 Ağu 2026): görünür ilerleme öne alındı.** Liste yarısı fiş ölçümünden bağımsız — o kod ölçüm ne çıkarsa çıksın yazılacak. Fiş testi paralel ize taşındı.

### Ana hat

| # | Adım | Neden bu sırada |
|---|---|---|
| 1 | **F1.1** Fontlar | Tipografi metrikleri bileşen yazmadan önce oturmalı, yoksa hepsini yeniden ölçeriz |
| 2 | **F1.6** Edge-to-edge | Cihazda görülen bir hata, ucuz, ve düzeltmesi hemen görünüyor |
| 3 | **F3.1** Bileşen kütüphanesi | **İlk gerçek görsel çıktı.** Sahte veriyle çalışır, Room'u beklemez |
| 4 | **F1.3 · F1.4 · F1.5 · F1.2** | Temel borç temizliği — deprecation, Nav3 saveable, pressable, kontrast testi |
| 5 | **F2.1 → F2.6** | Room + katalog. Listenin kalıcı olması için gerekli |
| 6 | **F3.2 → F3.8** | Gerçek Liste ekranı: bölümler, hızlı ekleme, alışveriş modu, boş durumlar |
| 7 | **Faz 4** → **Faz 6** → **Faz 5** → **Faz 7** → **Faz 8** → **Faz 9** → **Faz 10** | Fiş yakalama, öneri motoru, fiyat hafızası, senkron, marka, iOS, refactor |

**Faz 5 (fiyat hafızası) bilinçli olarak Faz 6'dan sonraya kaydı** — kapsamı F0.5'in kararına bağlı ve o karar paralel izden gelecek.

### Paralel iz — fiş ölçümü

Ana hattı bloklamaz. Sen fiş biriktirdikçe ve kimlik doğrulama hazır olunca ilerler.

**F0.2** → **F0.3** → **F0.4** → **F0.5**

Tek bağımlılık: **F0.5'in kararı Faz 5'in kapsamını belirler.** Faz 1, 2, 3, 4, 6 bu izden tamamen bağımsız — hiçbiri fiş ölçümünü beklemiyor.

---

## Faz 0 — Fiş ölçümü *(paralel iz)*

> **Ürünün yarısının kapsamı buranın sonucuna bağlı.** Fiş satır adları güvenilir çıkmazsa Neydi bir fiyat takipçisi değil, harcama defteri olur — ve bunu 4. ayda değil şimdi öğrenmek gerekiyor.
>
> Bu iz artık **paralel** ilerliyor: kimlik doğrulama ve fiş biriktirme kullanıcı tarafında olduğu için ana hattı bekletmiyor. Çıktısı **yalnızca Faz 5'i** kapılıyor.

- [x] **0.0 — Cihaz kurulumu.** ✅ Samsung Galaxy S10+ (SM-G975F), **Android 12 / API 31**, 1080×2280 @ 420dpi. `installDebug` çalışıyor, uygulama crash'siz açılıyor, açık ve karanlık mod referans görüntüleri alındı. Tema doğrulandı: açık modda krem `#FBF7F2` + terracotta `#B34418`, karanlık modda `#13100E` + somon `#FF9166`. **İki hata yakalandı → F1.6.** (API 31 ayrıca F8.4 için önemli: Android 12+ SplashScreen API'si bu cihazda geçerli.)
- [x] **0.1 — Fiş test koşumu** *(cihazsız)*. ✅ `tools/receipt-eval/` — TypeScript (Node 24, derleme adımı yok; üretimdeki Cloudflare Worker de TS olacağı için istek şekli birebir aynı). Türkçe fiş prompt'u + structured output şeması + ad/fiyat **ayrı** skorlama + aritmetik kapısı. API şekli `claude-api` skill'inden doğrulandı ve **üç varsayımım yanlış çıktı**: (a) Claude Opus 5'te thinking **varsayılan açık**, ve `disabled` yalnızca `effort` ≤ `high` ile kabul ediliyor — `xhigh`/`max` ile 400; (b) structured output `output_config.format` ile veriliyor, `output_format` kullanımdan kalkmış; (c) şema `minimum`/`minLength` kabul etmiyor ve nullable alanlar `anyOf` ile yazılmak zorunda.
- [x] **0.2 — Fiş ayrıştırma ölçümü** ✅ **Sorusu değişti ve başka yolla cevaplandı.** `ANTHROPIC_API_KEY` gerekmedi, `tools/receipt-eval/` hiç kullanılmadı: ölçüm cihazda ML Kit'in ürettiği **ham metin satırları** üzerinden yapıldı ve o satırlar `ReceiptParserTest`'e OCR hataları dahil aynen taşındı. **Ölçüt aritmetik kapısı ve iki fişin ikisi de tutuyor** (225,50 ve 484,58). Ölçümün asıl bulgusu şuydu: ilk parser baştan sona yanlıştı ve **17 sentetik testi geçiyordu** — örnek fişleri de kuralları da ben yazdığım için kendi varsayımlarımı kendime onaylatıyordum. Gerçek fiş üç varsayımı birden çürüttü. **Üçüncü fiş (~60 kalem) ise fiziksel sınırı ölçtü:** tek karede satır başına **4,7 piksel**, ML Kit 60 satırın 2'sini okudu — yazılımla çözülemez, `UNREADABLE_MESSAGE` bu ölçümden doğdu.

- [ ] **0.3 — Aritmetik değişmez** *(cihazsız)*. `Σ(satır) − İNDİRİM = TOPLAM`, ±0,05 TL tolerans (tartılı ürün yuvarlaması). Araştırmanın ilk yazdığı `+KDV` formülü **yanlıştı** — Türkiye'de perakende fiyatları kanunen KDV dahil, TOPKDV toplamın içindeki verginin dökümü. O formül %100 fişi manuel düzeltmeye yollardı.
- [ ] **0.4 — Kanonik ürün kimliği** *(cihazsız)*. Çıkan satır adlarını marketfiyati `/api/v2/search` ile bulanık eşleştir. `User-Agent` header'ı zorunlu — yoksa endpoint 404 döner. Barkod yolu **yok**: `searchByIdentity` gerçek EAN-13'lerde 6/6 boş döndü, katalogda barkod alanı hiç bulunmuyor.
- [ ] **0.5 — Kapsam kararı** *(cihazsız)*. Fiyat takipçisi mi, harcama defteri mi? Sonucu `docs/03-arastirma-bulgulari.md`'ye ek olarak yaz ve **Faz 5'in kapsamını buna göre daralt veya genişlet.**

## Faz 1 — Temel borçlar

- [x] **1.1 — Fontları bundle et.** ✅ *Cihazda doğrulandı — Galaxy S10+, açık ve karanlık modda Fraunces başlık + Plus Jakarta Sans gövde, crash yok.* Plus Jakarta Sans **5 statik ağırlık** (400/500/600/700/800) + Fraunces statik instance (`opsz 72 · SOFT 30 · WONK 0 · wght 600`), hepsi `fontTools varLib.instancer` ile üretildi. **Değişiklik:** PJS de variable değil statik bundle edildi — `FontVariation.Settings` iOS'ta güvenilir değil ve Mac olmadığı için doğrulayamayız, statikte o risk tamamen yok. Türkçe glif kapsamı 6 dosyada da doğrulandı (`Ğğ İı Şş ÇçÖöÜü` tam). `Font()` Compose Resources'ta `@Composable` olduğu için tipografi top-level `val`'den `rememberNeydiTypography()`'ye taşındı; M3'te karşılığı olmayan stiller (`itemName`, `priceChip`, …) `LocalNeydiTextStyles` ile sağlanıyor. APK +0,27 MB. → `Type.kt` TODO(font) kapandı.
- [x] **1.2 — Kontrast birim testi** *(cihazsız)*. ✅ 8 test, `commonTest`. **Amber kuralı türetilerek kilitlendi:** `accentNeedsOutline` bağımsız iddia edilmiyor, ölçümden türetiliyor (`assertEquals(oran < 3.0, bayrak)`) — ikisi birlikte yanlış olamasın diye. Belgelenen sayılar (2.08 / 5.56 / 11.29) da teste bağlandı, dokümantasyon çürüyemez. **Testin kırılabildiği kanıtlandı:** accent koyulaştırılınca 3 test, ikincil metin soluklaştırılınca 1 test kırıldı (`isik onSurfaceVariant/surface kontrasti 3.08:1 - en az 4.5:1 olmali`). **Test bir hata buldu ve düzeltildi:** `MetaText` zaten soluk olan `onSurfaceVariant`'a bir de %75 alfa uyguluyordu; efektif oran ışık modunda **3.98:1** — 14sp normal metin için AA sınırının (4.5:1) altında. Alfa kaldırıldı, hiyerarşiyi token sağlıyor: 7.40:1 / 9.62:1. Cihazda doğrulandı, en koyu metadata pikseli RGB(132,121,113) → RGB(92,79,69) = `#5C4F45` token'ın kendisi. **Bilinçli muafiyet:** işaretli satırlar (alfa 0.55) ışıkta 3.80:1 ile AA'nın altında — üzeri çizili ve "alındı" anlamında, WCAG 1.4.3 etkisiz bileşenleri dışarıda bırakıyor; yine de 3:1 tabanı teste bağlandı ki satır tamamen kaybolmasın.
- [x] **1.3 — CMP deprecation temizliği.** ✅ Kısayollar katalogdan açık koordinatlara taşındı; çözülen sürümler bit bit aynı kaldı, cihazda doğrulandı. **İki tuzak çıktı:** **(a)** `compose.material3` 1.11.1'i değil `ComposeBuildConfig.composeMaterial3Version`'ı yani **1.9.0**'ı çözüyordu — material3 CMP'de ayrı sürümleniyor ve 1.11.1 diye bir sürümü hiç yok, katalogda `composeMaterial3` ayrı anahtar. **(b)** `compose.components.uiToolingPreview`'in `ReplaceWith`'i yanlış artifact'i öneriyor (`ui:ui-tooling-preview` diyor ama accessor `components-ui-tooling-preview`'i çözüyor) — IDE quick-fix'ine körlemesine uymak artifact'i sessizce değiştirirdi. **Not:** uyarılar Gradle'da görünmüyor (Kotlin DSL script uyarıları yüzeye çıkmıyor), yalnızca IDE'de. Kaynak: `compose-gradle-plugin-1.11.1-sources.jar`.
- [~] **1.3b — `@Preview` altyapısı.** `org.jetbrains.compose.ui:ui-tooling-preview` commonMain'e, `ui-tooling` debug-only Android'e. Anotasyon `androidx.compose.ui.tooling.preview.Preview` — 13 parametre + `@PreviewLightDark`; `components-ui-tooling-preview`'deki 7 parametreli sürümde `uiMode` yok, karanlık preview çizilemezdi. `NeydiTheme.darkTheme` artık `isSystemInDarkTheme()` varsayılanlı, `NeydiPreview { }` kabuğu tema/tipografi/indication'ı sarıyor. Altı bileşende 11 preview. **Cihaz doğrulaması yerine IDE doğrulaması:** preview'ler yalnızca Android Studio'da çizilir, CLI'dan görülemez — Studio'da açılıp doğrulanması gerekiyor. Asıl risk: compose-resources fontları (`Font()` `@Composable`) preview renderer'ında çözülüyor mu.
- [x] **1.4 — Nav3 saveable back stack.** ✅ `rememberNavBackStack(NeydiSavedStateConfig, Liste)` + polymorphic `SerializersModule`. İki TODO da kapandı. **Gerekçe düzeltmesi:** bu adım "iOS'ta sessizce tezahür eder" diye yazılmıştı — yanlış. `navigation3-runtime` 1.1.1 kaynağı (`RememberNavBackStack.kt:64`) varsayılan `SerializersModule`'ü koşulsuz `require` ile reddediyor: modül verilmezse uygulama **her platformda** açılır açılmaz `IllegalArgumentException` ile patlıyor. Sessiz bozulma yok. İş yine de gerekliydi — Android'e özel reflection'lı aşırı yükleme iOS'ta yok, o olmadan uygulama orada derlenmezdi. **Kayıt unutmaya karşı derleyici bekçisi:** `NeydiKey` sealed olduğu için `serializerKaydiVarMi()` içindeki `when` exhaustive kontrol ediliyor; yeni hedef ekleyip kaydetmeyi unutursan derleme kırılır (geçici bir hedefle test edildi). **Cihazda ölçüldü:** `am kill` ile gerçek süreç ölümü — eski kod `Liste`'ye düşüyordu, yeni kod `Geçmiş`'te kalıyor ve geri tuşu `Liste`'ye dönüyor, yani iki katmanlı yığının tamamı geri geliyor.
- [x] **1.5 — `Modifier.pressable` zorunluluğu.** ✅ Ham `clickable` zaten yoktu; sorun 10 Material3 `Button`'daydı ve tahmin edilenden büyük çıktı. **Kök bulgu:** material3'ün `Surface`'ı indication'ı **sabit kodluyor** — `indication = ripple()`, üç ayrı yerde (material3 1.9.0, `Surface.kt:229/336/443`). Yani `LocalIndication` override'ı `Button`, `Card` ve tıklanabilir `Surface` için **hiç okunmuyor**; temada ripple'ı kaldırmak bu bileşenlere ulaşmıyor. Tek çıkış yolu Material3'ün tıklanabilir Surface'ini hiç kullanmamak. `NeydiButton` `Modifier.pressable` üzerine kuruldu. **Cihazda ölçüldü** (primary `#B34418` = RGB 179,68,24): Material3 basılı (202.3, 125.2, 94.4) — beyaz ripple, rengi **açıyor**; NeydiButton boşta (185.0, 83.0, 42.3) → basılı (174.7, 78.7, 40.6), yani **%5.6 karartma** — belgelenen %6 tonal overlay.
- [x] **1.6 — Edge-to-edge sistem çubukları.** ✅ *Üç senaryoda da cihazda doğrulandı: açık mod, karanlıkta temiz açılış, ve **uygulama açıkken** karanlığa geçiş.* **Kök neden:** manifest'te `configChanges` içinde `uiMode` var → karanlık moda geçince Activity yeniden yaratılmıyor → `onCreate`'te bir kez çağrılan `enableEdgeToEdge()` açılıştaki stilde donuyor. Compose renkleri güncelliyordu ama sistem çubukları güncellenmiyordu. **Çözüm:** çubuk görünümü `ApplySystemBarAppearance(darkTheme)` ile Compose'dan, temaya bağlı sürülüyor (`expect`/`actual`). Android'de `SystemBarStyle` + şeffaf çubuklar; iOS `actual`'ı **kasıtlı olarak boş** — orada karşılığı `preferredStatusBarStyle` ve common koddan set edilemiyor (F9.3). *Aşağıdaki orijinal tanım kayıt için bırakıldı:* `enableEdgeToEdge()` çağrılıyor ama iki sorun var: **(a)** karanlık modda status bar ikonları koyu kalıyor, siyah zemin üstünde neredeyse okunmuyor; **(b)** alt gezinme çubuğu şeridi karanlık modda açık renk kalıyor, ekranın altında beyaz bant bırakıyor. `SystemBarStyle` temaya bağlanacak. Bu iş iOS'ta karşılığı olmayan bir alan — `preferredStatusBarStyle` common koddan set edilemiyor (bkz. F9.3), o yüzden çözüm baştan platform-ayrık kurgulanmalı.

## Faz 2 — Veri katmanı

- [x] **2.1 — Room 3.0.1 kurulumu.** ✅ `androidx.room3` + KSP2 2.3.11 + `sqlite-bundled` 2.7.0, üç hedefe de KSP kaydı (`kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64`). **Koordinat değişikliği tek tuzak değilmiş:** Gradle eklentisi kendini **`room3`** adıyla kaydediyor, `room` değil (`RoomGradlePlugin`: `ExtensionContainer.create("room3", …)`) — Room 2 için yazılmış her örnek `room { }` diyor ve `Unresolved reference 'room'` ile patlıyor. **Cihazda uçtan uca doğrulandı:** KSP üç dosyayı da üretti (`NeydiDatabase_Impl`, `HouseholdDao_Impl`, `NeydiDatabaseConstructor` actual'ı), şema `composeApp/schemas/` altına çıktı ve commit edildi, veritabanı telefonda açıldı, yazma işledi, Flow gözlemi tetiklendi. Cihazdan çekilen dosya diskte gerçekten kalıcı: `('0198f2a1-…-0001', 'Bizim ev', 0, null)` — oturum içi okuma diske hiç inmeden de dönebilirdi, o yüzden ayrıca doğrulandı. **F2.2'ye devir:** tek entity var (`Household`, kök agregat) — atılacak sahte veri değil, kalan 15'in üzerine geleceği taban. `DbProbe` + `AyarlarScreen`'deki satır **geçici**, F2.6'da repository gelince kalkacak.
- [x] **2.2 — Entity'ler.** ✅ 16 tablo, cihazda oluştuğu doğrulandı (17 = 16 + Room'un `room_master_table`'ı). Ortak sözleşme `Conventions.kt`'de tek yerde: UUID v7, her kullanıcı satırında `householdId`, tombstone, **para = `Long` kuruş** (`Double` değil — bu uygulamanın işi fiyat toplamak, kayan nokta hata biriktirir), zaman = epoch millis. **Bilinçli istisna kayda geçti:** `Category` ve `CatalogSeed` referans verisi — uygulamayla geliyor, senkron olmuyor, kimseye ait değil, `householdId` taşımıyorlar. **Modelin taşıdığı üç karar:** `ProductAlias` mağaza *zinciri* bazında (A101 "T.BUGDAY EKMEK 500G" derken Migros "TAM BUGDAY EKMEGI" diyor — tek global eşleme ikisini karıştırır); `PriceObservation.packSize`/`packUnit` ayrı alanlar (1 L → 900 ml aynı fiyata satılınca birim fiyat değişmemiş görünür, ambalajı yakalayamayan fiyat hafızası shrinkflation'ı "fiyat sabit" diye raporlar — yani yalan söyler); `ProductStats.medianIntervalDays` medyan, ortalama değil (bir kez 40 gün unutmak ortalamayı kaydırıp "10 günde bir alıyoruz" gerçeğini gizler). **Kısıtlar henüz YOK** — `UNIQUE(tripId, productId)` ve `UNIQUE(householdId, storeChain, rawTextNormalized)` F2.3'te; sürüm 1 ve yayınlanmış veri olmadığı için o adım migration değil şema yenilemesi olacak.
- [x] **2.3 — Kritik kısıtlar.** ✅ İki `UNIQUE` + yedi sorgu indeksi, şema yenilendi (sürüm 1, yayınlanmış veri yok). **Kısıtların tetiklendiği kanıtlandı** — `KisitTest`, 4 test, **DAO üzerinden değil ham SQL ile**: DAO `OnConflictStrategy.REPLACE` ile yazsaydı ikinci satır sessizce üstüne biner, test yeşil kalır ve koruma olmazdı. Testler hem ihlali hem de **meşru olanı** kapsıyor: aynı ürün başka gezide serbest, aynı ham metin farklı zincirde serbest. **Test ısırıyor:** `unique = true` kaldırılınca `Ikinci satir kabul edildi - UNIQUE(tripId, productId) tetiklenmedi` diyor. **Zorunlu ortam düzeltmesi:** host testleri JVM'de koşuyor ve `sqlite-bundled`'ın Android varyantı JNI'yi cihazdan yüklüyor (`no sqliteJni in java.library.path`); `sqlite-bundled-jvm` yalnızca test sınıfyoluna eklendi. **İfade edilemeyen kısıt:** "aynı anda tek aktif alışveriş" kısmi indeks gerektiriyor (`WHERE completedAt IS NULL`) ve Room'un `@Index`'i kısmi indeks yazamıyor — kural F2.6'da repository katmanında, `Trip` KDoc'unda not düşüldü.
- [x] **2.4 — `matchKey` normalizasyonu + testi.** ✅ 8 test. **Tuzak ölçüldü:** `"İNCİR".lowercase()` beş harf yerine **yedi kod noktası** üretiyor — `[105, 775, 110, 99, 105, 775, 114]`, her İ'nin ardına U+0307 (birleştirici nokta) ekleniyor, sonuç `== "incir"` **false**. Kullanıcı "İncir" yazıp fiş "INCIR" yazdığında uygulama iki ayrı ürün sanardı; hata vermeden fiyat geçmişini ikiye bölerdi. `lowercase(Locale)` JVM'e özel, commonMain'de yok — eşleme **elle** yazıldı. **Bir test naif yolun bozuk olduğunu iddia ediyor** (`assertNotEquals`), yani `lowercase()` bir gün düzelirse test kırılır ve `matchKey` bilerek sadeleştirilir, kazara değil. **Bilinçli taviz kayda geçti:** ı ve i aynı anahtara katlanıyor, yani "ısırgan"/"isirgan" çarpışıyor — fiş yazıcılarının çoğu Türkçe karakter basmadığı için ayırmakta ısrar etmek her ürünü büyük/küçük harf varyantlarına bölerdi; çarpışma nadir, bölünme her alışverişte olurdu. **Noktalama boşluğa çevriliyor, silinmiyor:** `T.BUGDAY` → `t bugday`, silinseydi `tbugday` olur ve hiçbir kullanıcı girdisiyle eşleşmezdi.
- [x] **2.5 — Bundled Türk katalog.** ✅ **245 ürün + 12 kategori**, cihazda doğrulandı. Kategoriler **market gezme sırasında**, alfabetik değil — alfabetik sıralamak insanı markette ileri geri yürütür. `commonalityRank` **global** (1 = Ekmek), kategori içi değil: kullanıcı "ek" yazınca önce Ekmek gelmeli, alfabetik olarak önce gelen bir şey değil. **matchKey saklanmıyor, ekleme anında F2.4 ile türetiliyor** ki normalizasyon kuralı tek yerde kalsın; kural değişirse iki ayrı gerçek kaynağı oluşmaz. **Tohum id'leri deterministik** (`seed-<yayginlik>`) — senkron açılınca iki telefon aynı tohum ürününü ayrı ürün sanmaz. **Idempotent ve tek transaction:** 257 satır tek `IMMEDIATE` transaction'da; yarıda kesilirse "yarım tohumlanmış" hal kalırdı ve idempotency kontrolü onu doluymuş gibi görürdü. Cihazda: ilk açılış `katalog: 245 urun / 12 kategori`, ikinci açılış `katalog zaten yuklu`. **7 test**, aralarında 245 ürünün hiçbirinin aynı `matchKey`'e düşmediği kontrolü — düşselerdi arama hangisini göstereceğini bilemezdi. Üretici betik `tools/catalog/gen_catalog.py`.
- [x] **2.6 — DAO + repository katmanı.** ✅ 7 DAO + `ListeRepository`, hepsi suspend-first + Flow. **Kapsam bilerek dar:** yalnızca Liste ekranının (Faz 3) ihtiyacı olanlar; fiş/fiyat/senkron DAO'ları kendi fazlarında gelecek — şimdi yazmak kullanılmayan koda bakım borcu demek. **Koin bağlandı:** `lateinit var androidAppContext` gitti, Context artık Koin'den; eski hal `Application.onCreate` ile `Activity.onCreate` arasında kurulumu unutmaya açıktı ve unutulursa hata veritabanına **ilk erişimde**, çok sonra patlıyordu. **Geçiciler silindi:** `DbProbe` ve `AyarlarScreen`'deki satır. Tohumlama `App`'e taşındı — commonMain'de, çünkü Android'e özel bir yere koysaydık **iOS sessizce katalogsuz açılırdı** ve bu Mac'e geçene kadar fark edilmezdi. **Saat ve id üretimi repository'ye dışarıdan veriliyor**, böylece saf kalıyor ve testte deterministik oluyor.

  **Test gerçek bir hata buldu — ilk gerçekçi kullanımda çıkacak cinsten.** Tombstone satırı tabloda kalıyor ama `UNIQUE(tripId, productId)` `deletedAt`'i bilmiyor: "listeden çıkardım, sonra geri ekledim" akışı kısıta çarpıp uygulamayı **çökertiyordu**. Kullanıcının yapacağı en doğal ikinci hareket. Düzeltme: `findIncludingDeleted` ile mezardan çıkarma, **aynı id korunarak** — yeni id üretmek senkronda "silindi" ve "eklendi" olaylarını kopuk iki satıra bağlardı. Kısmi unique index (`WHERE deletedAt IS NULL`) bunu şemada çözerdi ama Room yazamıyor (F2.3).

  **34 test, 0 hata.** Cihazda: 12 kategori, 245 ürün, yaygınlık sırası doğru, `matchKey` türetilmiş, sıfır çökme.

## Faz 3 — Liste ekranı (uygulamanın kalbi)

> Uygulamada geçirilen sürenin %90'ı burada. Tek mükemmel olması gereken ekran.
>
> **F3.1 yürütme sırasında öne alındı** (3. adım): bileşen kütüphanesi sahte veriyle çalışır, Room'u beklemez, ve cihazda görülen ilk gerçek görsel çıktıdır. Ekranın kalanı (F3.2–3.8) veri katmanından sonra gelir.

- [x] **3.1 — Bileşen kütüphanesi.** Liste satırı (normal / sabit / işaretli / eş-eklemiş), `PriceChip`, delta çipi, Canvas sparkline (24×16dp), öneri çipi, kategori başlığı, kategori kutucuğu + **iki-harf fallback**. Fallback'i **önce** yap — öğelerin %80'i onu gösterecek.
- [x] **3.2 — Planlama modu.** ✅ Gerçek `ListeEkrani`, `ComponentGallery` ve `Bilesenler` hedefi silindi. Bölümler **reyon sırasında** — sıralama SQL'de (`ORDER BY c.sortOrder`), gruplama Kotlin'de. **İşaretliler reyondan çıkıp "Alındı"ya iniyor:** reyon içinde kalsalardı liste alışveriş ilerledikçe delik deşik görünür ve "daha ne kaldı" gözle cevaplanamazdı. **Eşleştirme tek SQL sorgusunda** (TripLine+Product+Category JOIN), üç Flow'u `combine` etmek her değişimde üç yeniden yayın üretir ve liste bir kare boşluklu görünürdü. `ListRow` kimlik taşımıyor (tasarım modeli); kimlik `UiSatir`'da.
- [~] **3.3 — Hızlı ekleme.** Alta sabit alan (BasicTextField — M3 TextField kendi kapsayıcısını getiriyor, Surface kuralı), **skora göre sıralı** otomatik tamamlama, inline miktar ayrıştırma. `miktarAyristir` **tutucu**: tanımadığı kelimeyi birim sanmıyor (`2 tam buğday ekmek` → miktar 2, ad "tam buğday ekmek"), sayıyla başlamayan metne hiç dokunmuyor (`Yumurta 10'lu` 10 adet değil). 9 test. **Katalogda eşleşme varsa kanonik ad kullanılıyor:** `sut` yazınca listede `Süt` görünüyor — yazıldığı gibi bırakmak ilk yazanın yazımını kalıcı yapardı ve `matchKey` ikisini aynı ürün saydığı için sonradan düzeltilemezdi. **Eksik:** satırda son ödenen fiyat — fiyat hafızası Faz 5'te, o gelince `PriceHint` bağlanacak.
- [~] **3.4 — Pano yapıştırma.** Madde işaretleri (`- • 1. 2)`), onay emojileri ve boş satırlar temizleniyor; her satır miktar ayrıştırıcıdan geçiyor, yani `- 2 kg elma` panodan da doğru düşüyor. **Üç satır eşiği:** iki satırlık pano çoğu zaman kopyalanmış bir cümle, liste değil — her metin parçasında çip çıkarmak çipi gürültüye çevirir ve göz onu görmemeye başlar. Alışveriş modunda çip **hiç** çıkmıyor: reyonda toplu ekleme yapılmaz. 9 test, aynı ürünü iki kez içeren pano dahil (adet artıyor, `UNIQUE` kısıtına çarpmıyor). **`[~]` çünkü panonun kendisi cihazda doğrulanamadı:** bu cihazda `cmd clipboard` yok ve adb'den pano set etmenin güvenilir yolu bulunamadı. Ayrıştırma ve ekleme yolu testli; doğrulanmamış olan yalnızca `LocalClipboardManager.getText()` okuması.
- [x] **3.5 — Alışveriş modu.** ✅ **Reyon sırasının donduğu ölçüldü:** bir satır işaretlendi, altındaki üç satırın piksel farkı **0/60000** — hiçbir şey kıpırdamadı. Planlamada işaretli satır "Alındı"ya iner, alışverişte yerinde kalır; aynı veri, iki ayrı doğru davranış. 72dp satır + kenarlık, 20sp/700 ad, metadata katlanıyor. **Gezinme butonları gizli:** reyonda yanlışlıkla Ayarlar'a düşmek listeyi kaybetmek gibi hissettirir. **Hızlı ekleme de gizli** — reyonda liste yazılmaz, okunur; klavye ekranın yarısını yerdi. Ekran uyanık kalıyor (`expect/actual`, `DisposableEffect` ile geri alınıyor — bırakılsaydı moddan çıkınca da kararmazdı). Haptik onay var, **snackbar yok**: bir gezide 20 işaretleme var.
- [x] **3.6 — Üç boş durum.** ✅ Ayrım **ürün geçmişinden** türetiliyor (hane hiç ürün görmediyse ilk gün, gördüyse döngü ortası) — kurulum ekranı henüz yok, o gelince "kurulum atlandı" ayrımı buraya eklenir. İlk gün: yönlendirme + reyon çipleri + (varsa) pano butonu. Döngü ortası: *"Liste tertemiz"* — **"boş" değil "temiz"**; aynı gerçek, çok farklı his. Uygulamanın hayatının çoğunu geçirdiği hal bu ve ölü hissettirmemeli. Reyon çipleri yalnızca ilk günde: döngü ortasında kullanıcı ne yapacağını zaten biliyor, 12 çip gürültü olur.
- [x] **3.7 — Ekle sheet (Ekran 2).** ✅ İki katmanlı: reyon grid'i → reyonun ürün çipleri (yaygınlığa göre, alfabetik değil). Serbest metin kaçış yolu altta — katalog 245 ürün, Türkiye'deki her ürün değil; katalogda olmayanı isteyen kullanıcı tıkanırsa sheet bir duvara dönüşür. **Liste arkada görünür kalıyor** — bu şartı korumak yerleşimin en zor kısmı oldu. **Beş deneme, hepsi cihazda görüldü:** (1) sabit 340dp grid → kaçış butonu çubuğun altında; (2) `weight(1f)` → kısmi açık sheet içeriği **sınırsız** yükseklikle ölçüyor, grid tüm alanı alıyor, buton kayboluyor; (3) `skipPartiallyExpanded` → buton görünüyor ama sheet **ekranı kaplıyor**, "liste arkada kalsın" kuralı bozuluyor; (4) `contentWindowInsets` → sheet içinde `safeDrawing` sıfır dönüyor, etkisiz; (5) dışarıdan geçilen alt boşluk → buton zaten taşmış, altına boşluk eklemek onu yukarı çekmiyor. **Doğrusu ölçümle bulundu:** `uiautomator dump` üçüncü sıranın etiketlerini ve butonu `bounds="[0,0][0,0]"` gösterdi — sıfır yüksekliğe kırpılmışlardı; sheet taşan içeriği kaydırmıyor, **kırpıyor**. Grid bütçesi ekran yüksekliğinin %24'ü, buton `y 1993→2047` ile çubuğun üstünde. **Sheet zemini açıkça veriliyor:** palet `surfaceContainer*` tonal token'larını tanımlamıyor, M3 kendi mor baseline'ına düşüyordu.
- [~] **3.8 — Sepet tahmini + özet kartı.** Yapı kuruldu, **veri Faz 4/5'ten gelecek.** Tahmin her ürünün **en son** gözlenen fiyatından hesaplanıyor (ortalama değil — zamla birlikte gerçeğin gerisinde kalır). **Dürüst olmak zorunda:** fiyatı bilinmeyen ürünler toplama girmiyor, o yüzden "en az X TL" diyor ve kaçının fiyatını bildiğini yazıyor; hiç fiyat yoksa satır **hiç çizilmiyor** — "0,00 TL" yalan olurdu. Özet kartı tutar bilinmese de anlamlı: "6 ürün alındı, 2 tanesi kaldı". Tutar 36sp Fraunces. `kurusFormatla` Türkçe yazım (ondalık virgül, binlik nokta), 5 test. **`[~]` çünkü hiçbiri gerçek veriyle görülmedi:** `price_observation` boş (Faz 5) ve `Trip.totalMinor` fişten gelecek (Faz 4). Preview'lerde ve testlerde doğru, cihazda henüz gösterecek veri yok.

## Faz 4 — Alışveriş kapatma ve fiş

> Verinin dürüst kalmasını sağlayan faz. Ekmek problemini asıl çözen katman burası: fiş, listeye yazılmasa bile ekmeği içeriyor.

- [x] **4.1 — Trip yaşam döngüsü.** ✅ `PLANNING → SHOPPING → CLOSED` + `ownerMemberId`. **Tek cihaz kapatır** — `closeIfOpen` bir **karşılaştır-ve-yaz**: `WHERE id = :id AND completedAt IS NULL`. İkinci kapatma **sıfır satır** günceller, `ownerMemberId` ilk kapatanda kalır, `completedAt` ilerlemez, ve dönen sayı çağırana "ben kapattım" ile "zaten kapanmış" farkını söyler. Önce-oku-sonra-yaz iki adıma bölünse yarış penceresi geri gelirdi. **Kapalılığın otoritesi `completedAt`**, `status` değil: böylece sürüm 1 satırları geri-doldurma gerektirmiyor ve migration tamamen otomatik. **Alışveriş modu artık kalıcı** — ekran durumu değil gezinin durumu; uygulama öldürülüp açılınca korunuyor (cihazda doğrulandı) ve Faz 7'de eşler aynı modu görecek. **Migration eski veriyle cihazda sınandı**: v1 kurulup veri eklendi, üstüne v2 `pm clear` yapılmadan kuruldu. İlk denemede patladı — hata ayıklarken sürümü 1'e çekmiştim ve Room `1.json` şema temelini yeni kolonlarla üzerine yazmıştı, fark boş çıkıp boş migration üretmişti. Yeşil test koşumu bunu **hiç** yakalamazdı.
- [x] **4.2 — Kamera.** ✅ FileKit 0.14.2, sistem kamerası (`ACTION_IMAGE_CAPTURE`) — **CAMERA izni gerekmiyor**, cihazda doğrulandı. **Fotoğraf bloklamıyor**: gezi çoktan kapanmış oluyor, fiş `PENDING` olarak kuyruğa giriyor, OCR sonra koşacak. Görsel platform tarafında küçültülüyor — cihazda ölçüldü: **2944×2208 / 672KB → 2576×1932 / 322KB**, sınır tam tutuyor. Ham dosya sonra siliniyor (fiş kişisel veri; iki kopya hem yer hem gereksiz maruziyet).
  **Üç hata cihazda çıktı ve hiçbiri testle görünmezdi.** (1) Kamera doğrudan hedef dosyaya yazıyor, yani küçültmenin kaynağı ile hedefi **aynı dosya** oluyordu — sessizce işe yaramadı, görsel 2576 yerine 2944 kaldı; bunu ancak **çıktının gerçek çözünürlüğünü ölçmek** yakaladı. (2) Hedef yolu `remember` içinde tutuyordum; kamera ön plandayken Android Activity'yi yeniden oluşturuyor, durum sıfırlanıyor ve fiş **hiç kaydedilmiyordu**. Durumu tamamen kaldırdım — hedef kaynak adından türüyor, kurtarılacak bir şey yok. (3) `PlatformFile.absolutePath()` bir `content://` URI dönüyor (FileProvider), `BitmapFactory.decodeFile` onu okuyamıyor. **İki kez tahmin ettim, ikisi de yanlış çıktı; bir kez günlükleyip ölçtüm ve cevap tek seferde geldi.** Sözleşme artık yol değil **bayt** alıyor, böylece URI/dosya ayrımı sınırın dışında kalıyor.
  **Düzeltemediğim bir yer:** arka kamera `FileKitCameraFacing.Back` ile isteniyor ama sistem kamerası bunu yoksayabiliyor — test cihazı (Samsung) yoksayıyor ve ön kamerayla açılıyor; kullanıcı tek dokunuşla çeviriyor.

- [ ] **4.3 — ~~Cloudflare Worker proxy~~ → GEREKMİYOR.** ML Kit cihazda çalıştığı için ortada API anahtarı, proxy, secret yönetimi **yok**. Faz 7'de Supabase keep-alive'ı için Worker yine gerekebilir — o iş **7.6**'da zaten var. Bu madde kapandı; mimari bir bağımlılık eksildi.
- [x] **4.4 — Cihazda OCR + Türkçe fiş ayrıştırıcı.** ✅ ML Kit Text Recognition v2 (Android) / Vision (iOS, Faz 8) — **fotoğraf telefondan çıkmıyor**, API anahtarı yok, ağ yok, ücret yok. **İki gerçek fişte (BİM ve File Market) doğrulandı**, sentetik örnekte değil.
  **İlk sürüm baştan sona yanlıştı ve 17 testi geçiyordu.** Örnek fişleri de kuralları da ben yazmıştım, yani kendi varsayımlarımı kendime onaylatıyordum. İki gerçek fiş üç varsayımı birden çürüttü: (1) **ondalık ayırıcı nokta** — noktayı açıkça reddetmiş ve gerekçe olarak *"Türkiye'de fiş öyle basmıyor"* yazmıştım; iki zincirin ikisi de nokta basıyor ve tek başına bu, hiçbir tutarın okunmaması demekti. (2) **toplam satırı "Ödenecek KDV Dahil Tutar" ve içinde KDV geçiyor** — ben KDV'yi TOPLAM'dan önce eliyordum, hem de bir tuzağı önlemek için bilinçli olarak, ve bu gerçek toplam satırını eliyordu. (3) **miktar satırı ayrı ve üründen önce** geliyor; ben adın ardından bekliyordum, yani hiçbir tartılı ürünü yakalayamazdım.
  **Satır sırası güvenilir değil, bu yüzden GÖRSEL SATIR gruplaması yapılıyor.** Fiş iki kolon (solda açıklama, sağda tutar) ve ikisi aynı görsel satırda; ML Kit onları ayrı "line" döndürüyor ve dikey konumları eşit olduğu için diziliş rastgele bozuluyor — ölçüldü: `*47.00` kendi ürün adından **önce** geliyordu. Çözüm eşleştirmeyi dikey örtüşmeden yapmak: aynı satıra düşen parçalar birleşiyor, grup içinde X'e göre sıralanıyor. Satır sayısı 47'den 25'e düştü ve ayrıştırıcının beklediği biçim çıktı.
  **Yön otomatik bulunuyor.** Yatay çekilmiş fişte tutarlar ürünlerden tamamen kopuyor; 0°/90°/270° denenip **eşleştirme başarısı** puanlanıyor ve en iyisi seçiliyor. Ölçüm: yatay çekim 90°'de 8 puan / 0°'de 0 puan, dik çekim tersi. Sonuç: **aynı fiş iki yönde de aynı 25 satırı veriyor.** EXIF'e güvenilemiyor, küçültme sırasında yeniden kodlanan JPEG yön bilgisini taşımıyor.
  **Bir hata daha, aynı sınıftan:** `"ALIŞVERİŞ POŞETİ"` ödeme satırı sanılıp atılıyordu — `matchKey` "poseti" üretiyor ve aradığım `" pos"` alt dizgisi onun içinde. Poşet (1,00 TL) toplamdan düşüyor, aritmetik kapısı haksız yere tutmuyordu. Kelime sınırı eklendi. (Bu, aynı oturumda import'ta yaptığım önek-eşleşmesi hatasının aynısı.)

- [x] **4.5 — Aritmetik kapı.** ✅ Σ(ürün) − Σ(indirim) = TOPLAM, ±5 kuruş. **KDV eklenmiyor ve çıkarılmıyor — ve bu gerçek fişle doğrulandı:** iki fişin ikisinde de satırların toplamı "Ödenecek KDV Dahil Tutar" ile birebir tutuyor (**225,50** ve **484,58**), "TOPLAM KDV" ise o tutarın *içindeki* verginin dökümü. Araştırmanın ilk yazdığı `+KDV` formülü her fişi manuel düzeltmeye yollardı. Üç durumlu dönüyor: tuttu / tutmadı / **toplam okunamadı** — "doğrulanamadı" ile "tutmadı" ayrı şeyler. Yeşil/amber çip 4.6 ekranıyla gelecek.

- [x] **4.6 — Fiş Kontrol ekranı.** ✅ Üç satır hali, **ham OCR metni gri alt satır**, 3-dokunuş düzeltme, uygulamadaki tek sayısal klavye, yeşil/amber aritmetik çipi. **Gerçek fişle cihazda doğrulandı:** File Market fişi ekranda 6 satır, yeşil *"Toplam tutuyor · 484,58 TL"*, her satırın altında fişte yazan hali.
  **Elle yön çevirme iyi bir okumayı BOZUYORDU ve bunu ancak cihazda gördüm.** İlk halinde iki buton vardı: *"Çevir ve tekrar oku"* (90°) ve *"Düz oku"* (0°). Dik görünen fiş aslında döndürülmüş çekilmişti; 0° zorlandığında okuma **2 satıra** düştü, okunamadı mesajı ekranı devraldı ve **doğru ayrıştırılmış 6 satır erişilemez oldu** — halbuki veritabanında duruyorlardı. İki ayrı hata: (1) başarısız okuma önceki iyi okumanın DURUMUNU eziyordu, (2) ekran satır varken bile hata mesajını gösteriyordu. Düzeltme: `ReceiptReadOutcome.KEPT_PREVIOUS` — yeni okuma kullanılamazsa eski durum geri alınıyor; ekran ise satır varsa hata mesajını hiç göstermiyor. İki buton tek *"Başka yönde oku"*ya indi ve açıları sırayla deniyor (90→270→180→0, 0 en sonda çünkü otomatik seçim onu zaten denedi). Regresyon testi yazıldı.
  **`safeDrawingPadding` unutulmuştu:** başlık durum çubuğunun altına giriyordu, saat ve pil göstergesi mağaza adının üzerine biniyordu. İskelet ekranlarda vardı, yeni ekranlarda yoktu. iOS'ta notch ile daha kötü olurdu.

- [x] **4.7 — `ProductAlias` öğrenmesi.** ✅ Her düzeltme bir alias yazıyor; eşleştirme **önce alias**, sonra `matchKey` — tahmini eşleştirme yok, emin olunamayan satır onaya düşüyor. Alias **şube değil zincir** bazlı (`chainKey`), yoksa her şube için aynı düzeltme tekrar istenirdi.
  **Cihazda uçtan uca kanıtlandı:** `SRIRACHA SOS 230 GR` satırı *"Sos"* olarak düzeltildi, sonra fiş **90°'de sıfırdan yeniden okundu** (satırlar silinip yeniden yazıldı) ve aynı satır **sorulmadan** *"Sos"* olarak geldi, "yeni" çipi olmadan.
  **`OnConflictStrategy.IGNORE` sessiz bir hataydı.** Tekil index (householdId, storeChain, rawTextNormalized) üzerinde IGNORE, kullanıcının **ikinci kararını yutuyordu: bir metni önce yanlış ürüne bağlayıp sonra düzeltirsen düzeltme hiç yazılmaz, eski alias sonsuza kadar kalır ve her fişte aynı yanlış eşleşmeyi üretir — üstelik kullanıcı düzeltmeyi yaptığı için bir daha bakmaz. REPLACE oldu: öğrenen bir sistemde son karar kazanır.

- [x] **4.8 — Mod B (fişsiz mutabakat).** ✅ Varsayılan-iyimser kapanış: `TripLineDao.markAllTaken` işaretlenmemiş planlı satırları alındı yazıyor. **Mutabakat hiç çalışmıyordu** — `finishShopping()` geziyi kapatıyor ama planlananları alındı olarak yazmıyordu, yani fişsiz gezi öneri motoruna hiç veri üretmiyordu.
  **Cihazda doğrulandı:** 3 ürünlük liste, alışveriş modunda hiçbiri işaretlenmedi, *"Alışverişi bitir"* → özet kartı **"3 ürün alındı"** dedi. Yanlış yönde iyimser olması bilinçli: "aldım sandım ama almadım" tek dokunuşla geri alınabilir, "almadım sandım ama aldım" hiçbir yerde görünmez ve gecmişte kalıcı boşluk bırakır. Geri alma yolu (*"Hepsini almadım"* → **Bitir ekranı**) o yüzden görünür olmak zorunda.
  **İki kez çalışmıyor:** yalnızca `closeIfOpen` 1 döndürünce çağırılıyor. İki cihaz aynı geziyi kapatsa satın almalar çift sayılır, `medianIntervalDays` yarıya düşer ve uygulama her şeyi iki kat sık önermeye başlar — sessiz ve geri alınması zor bir bozulma.

- [x] **4.9 — Geçmiş ekranı (Ekran 6).** ✅ Kapanmış geziler, altlarında fişleri; **başarısız fişler de listede ve dokunulabilir** — Geçmiş yanlış okunmuş bir fişe dönmenin tek yolu, FAILED satırlarını elemek onları erişilemez yapardı. Sorunlu durumlar etiket alıyor, VERIFIED **almıyor**: iyi durum sessiz olmalı.
  **Ekran erişilemezdi ve bu 4.9'un tamamını boşa çıkarırdı.** `onHistory` aşağıya geçiliyordu ama **hiçbir yerde kullanılmıyordu** — Geçmiş butonu yoktu. Üstelik mevcut üç başlık butonu da ekrana sığmıyordu ve *"Ayarlar"* sağ kenarda **kesiliyordu**: `Row` taşan içeriği kırpıyor, kaydırmıyor. `horizontalScroll` eklendi, Geçmiş butonu geldi — cihazda dördü de tam görünüyor.

- [x] **4.10 — iOS derlemesi düzeltildi** *(Faz 4 kapsamı dışı ama Faz 4'ü bloklıyordu)*. `:composeApp:compileKotlinIosSimulatorArm64` **HEAD'de kırıktı** (`git stash` ile doğrulandı, benim değişikliklerimden önce): `Dispatchers.IO` commonMain'den erişilemiyor, native hedeflerde `internal`. Yani commonMain'de `Dispatchers.IO` yazan kod Android'de derleniyor, iOS'ta derlenmiyor ve hata yalnızca iOS hedefi ilk kez derlenince çıkıyor. `expect/actual ioDispatcher` eklendi (JVM'de IO, native'de Default). Bu `allTests`'i de bloklıyordu, yani ortak test paketi hiç koşmuyordu.

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

- [x] **10.1 — AGP 9'a geçiş.** ✅ *("cihazsız" işareti kaldırıldı — fazlasıyla cihaz gerektirdi.)* AGP **9.3.1**, compileSdk/targetSdk **37**, lifecycle **2.11.0**. **Engel hâlâ duruyor:** `com.android.application` + `org.jetbrains.kotlin.multiplatform` aynı modülde 9.3.1'de de reddediliyor, birebir aynı hatayla. Bypass flag'leri (`android.builtInKotlin=false` + `android.newDsl=false`) çalışıyor ve ölçüldü — ama AGP 10.0'da kaldırılıyorlar ve `newDsl=false` AGP 9'un yeni DSL'ini kapatıyor, yani sürüm 9 DSL 8 olurdu. **Modül ikiye ayrıldı:** `:composeApp` artık `com.android.kotlin.multiplatform.library` ile kütüphane, APK'yı ince `:androidApp` üretiyor (yalnızca `MainActivity` + manifest + tema). JetBrains Mayıs 2026'dan beri KMP sihirbazında zaten bunu üretiyor.

  **Dört tuzak — üçü sessiz:**
  1. **`:androidApp` Compose derleyici eklentisini uygulamak ZORUNDA.** AGP 9'un `builtInKotlin`'i Kotlin'i derliyor ama Compose eklentisini getirmiyor. Onsuz `setContent { App() }` içindeki `@Composable` lambda düz `Function0` olarak derleniyor, kütüphane `Function2` bekliyor. **Derleme sessizce geçiyor**, uygulama açılışta `NoSuchMethodError` ile çöküyor. Dex'i açıp imzaları karşılaştırarak bulundu (`dexdump`).
  2. **`androidResources { enable = true }` zorunlu** — kütüphane modülünde varsayılan kapalı, kapalıyken Compose Resources (bu projede fontlar) çalışma zamanında patlıyor.
  3. **Tüm eklentiler kökte `apply false` ile bildirilmeli.** İki modül aynı AGP'yi sürümle isterse Gradle `already on the classpath with an unknown version` diyip reddediyor.
  4. **Test görevi yeniden adlandı:** `testDebugUnitTest` → `testAndroidHostTest`. Eski adla çağırınca görev bulunamıyor; daha kötüsü, yanlış ada rağmen yeşil görünen bir koşu sıfır test çalıştırabiliyor.

  **Bonus bulgu:** bölünme gizli bir sürüm kaymasını açığa çıkardı — `activity-compose` derlemede 1.12.0, çalışma zamanında 1.12.4'tü. Tek modüldeyken ikisi aynı classpath olduğu için görünmüyordu. 1.12.4'e hizalandı. (Çökmenin sebebi bu değildi — sebep 1. maddeydi — ama gerçek bir kaymaydı.)

  **Ölçüldü:** 8 birim test geçiyor, 79 iOS task'ı hâlâ tanımlı, Room şeması ve KSP üretimi sağlam, cihazda galeri + Room sondası + süreç ölümünden back stack geri dönüşü çalışıyor, çökme yok.
- [ ] **10.5 — Sheet yüksekliği: sihirli sayıyı kaldır** *(cihazsız değil — cihazda ölçülmeli)*. `EkleSheet.GRID_ORANI = 0.24f` **ayarlanmış bir sabit, çözüm değil.** Bu telefonda ölçüldü (`uiautomator`, buton `y 1993→2047`); başka bir ekran oranında, katlanabilir cihazda, yazı tipi ölçeği büyütülmüş bir kullanıcıda ya da 12'den fazla reyon olduğunda **yeniden taşabilir** — ve taştığında sessizce taşar, çünkü sheet kırpıyor, hata vermiyor. **Kök sebep:** kısmi açık `ModalBottomSheet` içeriği sınırsız yükseklikle ölçüyor, o yüzden `weight` çalışmıyor; taşan içeriği de kaydırmıyor, kırpıyor. **Doğru çözüm adayları:** (a) F10.2'deki Nav3 custom Scene'e geçerken yüksekliği kendimiz sınırlayan bir kapsayıcı yazmak — ikisi aynı işte buluşuyor; (b) grid'i kendi `verticalScroll`'una alıp butonu `Box` içinde alta sabitlemek; (c) `SubcomposeLayout` ile önce butonu ölçüp kalanı grid'e vermek. **Regresyon nöbetçisi gerekiyor:** hangi çözüm seçilirse seçilsin, butonun sıfır olmayan sınırlara sahip olduğunu doğrulayan bir kontrol olmalı — `bounds="[0,0][0,0]"` derlemede görünmüyor.
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
| `TODO(sheet-yuksekligi)` | `ui/liste/EkleSheet.kt` | F10.5 |
| `TODO(splash)` | `androidApp/src/main/res/values/themes.xml` | F8.4 |
| `TODO(ios)` | `iosMain/MainViewController.kt` | F9.3 |
| `TODO(tnum)` | `ui/theme/Type.kt` | F9.4 |

## İlgili dokümanlar

- [`00-isim-onerileri.md`](00-isim-onerileri.md) — isim analizi ve eleme gerekçeleri
- [`01-claude-design-prompt.md`](01-claude-design-prompt.md) — ekran ekran tasarım spec'i, CMP kısıtları
- [`02-logo-splash-prompt.md`](02-logo-splash-prompt.md) — logo konseptleri, ikon/splash teknik gereksinimleri
- [`03-arastirma-bulgulari.md`](03-arastirma-bulgulari.md) — planı değiştiren bulgular, ölçülmemiş varsayımlar
- [`../graphify-out/GRAPH_REPORT.md`](../graphify-out/GRAPH_REPORT.md) — bilgi grafiği raporu
