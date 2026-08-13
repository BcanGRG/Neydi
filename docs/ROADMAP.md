# Neydi — Yol Haritası

Tek gerçek kaynak. 11 faz, 67 adım. Her adım bir PR.

**İlerleme:** 10 / 67 — *Faz 1 bitti, F2.1 ve F2.2 de merge edildi. Sırada **F2.3** (kritik kısıtlar). Fiş ölçümü paralel izde, sende bekliyor.*

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
- [ ] **0.2 — İlk ölçüm** *(cihazsız)*. Eldeki 2 fişi (farklı zincir) çalıştır. **Satır adı doğruluğunu fiyat doğruluğundan ayrı skorla**, zincir bazında raporla. Ad alanı, karşılaştırma özelliğinin ihtiyaç duyduğu ve başarısız olması beklenen alan.
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
- [ ] **2.4 — `matchKey` normalizasyonu + testi.** Diacritic-folded, **locale-explicit** lowercase. `lowercase()` locale'siz çağrılırsa noktalı/noktasız İ ürünü sessizce ikiye böler ve fiyat geçmişini yok eder.
- [ ] **2.5 — Bundled Türk katalog.** ~250 ürün + 12 kategori taksonomisi, `commonalityRank` ile. Tek hane = hiç işbirlikçi sinyal yok, yani bu **tek** soğuk-başlangıç mekanizması. Faz 0.4 çıktısına göre marketfiyati'den tohumlamayı değerlendir.
- [ ] **2.6 — DAO + repository katmanı.** Flow tabanlı, offline-first.

## Faz 3 — Liste ekranı (uygulamanın kalbi)

> Uygulamada geçirilen sürenin %90'ı burada. Tek mükemmel olması gereken ekran.
>
> **F3.1 yürütme sırasında öne alındı** (3. adım): bileşen kütüphanesi sahte veriyle çalışır, Room'u beklemez, ve cihazda görülen ilk gerçek görsel çıktıdır. Ekranın kalanı (F3.2–3.8) veri katmanından sonra gelir.

- [x] **3.1 — Bileşen kütüphanesi.** Liste satırı (normal / sabit / işaretli / eş-eklemiş), `PriceChip`, delta çipi, Canvas sparkline (24×16dp), öneri çipi, kategori başlığı, kategori kutucuğu + **iki-harf fallback**. Fallback'i **önce** yap — öğelerin %80'i onu gösterecek.
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

- [x] **10.1 — AGP 9'a geçiş.** ✅ *("cihazsız" işareti kaldırıldı — fazlasıyla cihaz gerektirdi.)* AGP **9.3.1**, compileSdk/targetSdk **37**, lifecycle **2.11.0**. **Engel hâlâ duruyor:** `com.android.application` + `org.jetbrains.kotlin.multiplatform` aynı modülde 9.3.1'de de reddediliyor, birebir aynı hatayla. Bypass flag'leri (`android.builtInKotlin=false` + `android.newDsl=false`) çalışıyor ve ölçüldü — ama AGP 10.0'da kaldırılıyorlar ve `newDsl=false` AGP 9'un yeni DSL'ini kapatıyor, yani sürüm 9 DSL 8 olurdu. **Modül ikiye ayrıldı:** `:composeApp` artık `com.android.kotlin.multiplatform.library` ile kütüphane, APK'yı ince `:androidApp` üretiyor (yalnızca `MainActivity` + manifest + tema). JetBrains Mayıs 2026'dan beri KMP sihirbazında zaten bunu üretiyor.

  **Dört tuzak — üçü sessiz:**
  1. **`:androidApp` Compose derleyici eklentisini uygulamak ZORUNDA.** AGP 9'un `builtInKotlin`'i Kotlin'i derliyor ama Compose eklentisini getirmiyor. Onsuz `setContent { App() }` içindeki `@Composable` lambda düz `Function0` olarak derleniyor, kütüphane `Function2` bekliyor. **Derleme sessizce geçiyor**, uygulama açılışta `NoSuchMethodError` ile çöküyor. Dex'i açıp imzaları karşılaştırarak bulundu (`dexdump`).
  2. **`androidResources { enable = true }` zorunlu** — kütüphane modülünde varsayılan kapalı, kapalıyken Compose Resources (bu projede fontlar) çalışma zamanında patlıyor.
  3. **Tüm eklentiler kökte `apply false` ile bildirilmeli.** İki modül aynı AGP'yi sürümle isterse Gradle `already on the classpath with an unknown version` diyip reddediyor.
  4. **Test görevi yeniden adlandı:** `testDebugUnitTest` → `testAndroidHostTest`. Eski adla çağırınca görev bulunamıyor; daha kötüsü, yanlış ada rağmen yeşil görünen bir koşu sıfır test çalıştırabiliyor.

  **Bonus bulgu:** bölünme gizli bir sürüm kaymasını açığa çıkardı — `activity-compose` derlemede 1.12.0, çalışma zamanında 1.12.4'tü. Tek modüldeyken ikisi aynı classpath olduğu için görünmüyordu. 1.12.4'e hizalandı. (Çökmenin sebebi bu değildi — sebep 1. maddeydi — ama gerçek bir kaymaydı.)

  **Ölçüldü:** 8 birim test geçiyor, 79 iOS task'ı hâlâ tanımlı, Room şeması ve KSP üretimi sağlam, cihazda galeri + Room sondası + süreç ölümünden back stack geri dönüşü çalışıyor, çökme yok.
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
| `TODO(splash)` | `androidApp/src/main/res/values/themes.xml` | F8.4 |
| `TODO(ios)` | `iosMain/MainViewController.kt` | F9.3 |
| `TODO(tnum)` | `ui/theme/Type.kt` | F9.4 |

## İlgili dokümanlar

- [`00-isim-onerileri.md`](00-isim-onerileri.md) — isim analizi ve eleme gerekçeleri
- [`01-claude-design-prompt.md`](01-claude-design-prompt.md) — ekran ekran tasarım spec'i, CMP kısıtları
- [`02-logo-splash-prompt.md`](02-logo-splash-prompt.md) — logo konseptleri, ikon/splash teknik gereksinimleri
- [`03-arastirma-bulgulari.md`](03-arastirma-bulgulari.md) — planı değiştiren bulgular, ölçülmemiş varsayımlar
- [`../graphify-out/GRAPH_REPORT.md`](../graphify-out/GRAPH_REPORT.md) — bilgi grafiği raporu
