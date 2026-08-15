# Neydi — Yol Haritası

Tek gerçek kaynak. 11 faz, **96 adım**. Her adım bir PR.

*Sayı bu oturumda mekanik olarak yeniden sayıldı. Eskiden 67 yazıyordu; fark 0.0, 1.3b ve 4.10 gibi numaralandırma şemasının öngörmediği ama meşru adımlardan ve bu oturumda eklenen yeni maddelerden geliyor.*

**İlerleme:** 57 / 104 — *55 kapandı · 2 kod tamam, cihaz doğrulaması bekliyor · 47 açık.*

**Faz 11 (tasarım sadakati) neredeyse bitti.** Tasarımın **15 maddelik karar defteri** geldi ve on beşinin on üçü uygulandı: alışveriş modundan çıkış, toolbar, ilk gün çipleri, avatar, toast, Fiş Kontrol manşeti, zincir adı, barkodlu satır, `~` manşeti, *"Verilerimi sil"* destinasyonu, çok parçalı fiş tek akış, Mağazalar bölümü. Karar defterinin kapattığı yan ürünler: **F5.9** (Store yazımı) ve **F10.16** (`PriceText`) da bitti.

**Kuyruğun başı:** **F4.14** — AKYURT iki satırlı düzen. Kullanıcının ana marketi bu ve adlar hâlâ barkod çıkıyor; karar 14 bunu *görünür* kıldı ama *çözmedi*. Sonrası **F5.1** (`PriceObservation` yazımı) — fiyat hafızasının kilit taşı, Faz 5'in geri kalanı ve Ekran 1'in fiyat ipucu ona bakıyor.

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
| 7 | **Faz 4** ✅ | Fiş yakalama — bitti, uçtan uca cihazda doğrulandı |
| 8 | **Şema v2 → v3** *(tek bump)* ✅ **F2.8** | Faz 5, 6 ve 7'nin dokuz şema değişikliği; cihazda gerçek v2 verisiyle doğrulandı. Faz 5/6 artık veri yazabilir |
| 9 | **F2.7 · F3.9** | Katalog yeniden tohumlanabilir olsun + "Diğer" kategorisi. İkisi birbirine bağlı ve **F0.4 ile F6.x onları bekliyor** |
| 10 | **Faz 6** (6.8 önce) | Öneri motoru. `isStaple` yazma yolu (**F6.8**) en başta: onsuz Ekran 3 hiç açılamaz |
| 11 | **Faz 5** | Fiyat hafızası. F0.5 kararı verildi (**fiyat takipçisi**), 5.4/5.5 F0.4'e bağlı ve en sonda |
| 12 | **Faz 7** → **Faz 8** → **Faz 9** → **Faz 10** | Senkron, marka, iOS, refactor |

**Faz 5 hâlâ Faz 6'dan sonra** — ama sebebi değişti. Eskiden "kapsamı F0.5'e bağlı, o karar beklenecek" diyordu; **F0.5 kararı artık verildi**. Yeni sebep: bağımlılıkların bir kısmı **ters yönde akıyor**. Ekran 3'ün satırları son ödenen fiyatı gösteriyor (F5.1'e bağlı) ama Ekran 3'ün var olabilmesi `isStaple` yazma yoluna bağlı (F6.8); ve satırın ikinci satırında **fiyat ipucu gerekçeyi bastırıyor** (`secondLine()` önceliği: fiyat > gerekçe), yani ikisi aynı satırda yarışıyor ve hangisinin kazandığı **Faz 6'nın tasarım kararı**. O yüzden öneri motoru önce.

**Yeni ve sert bir sıra kuralı:** üç tablo (`price_observation`, `suggestion_event`, `pending_op`) bugün **boş**. Tablo boşken şemasındaki hata bedava; ilk satır yazıldıktan sonra onu yeniden şekillendirecek `execSQL` **yok**. Bu yüzden **şema bump'ı Faz 5/6'nın ilk yazmasından önce** gelmek zorunda — adım 8, adım 10'dan önce.

### Paralel iz — fiş ölçümü

Ana hattı bloklamaz. Sen fiş biriktirdikçe ve kimlik doğrulama hazır olunca ilerler.

**F0.2** ✅ → **F0.3** ✅ → **F0.5** ✅ → **F0.4** *(tek kalan)*

**İz büyük ölçüde kapandı ve beklenenden farklı bitti.** F0.2 ölçümü API ile değil **cihazda ML Kit'in ham çıktısıyla** yapıldı; F0.3 zaten F4.5 ile yazılmıştı; F0.5 kararı **iki gerçek fişin 10 satırının 10'unun doğru okunmasıyla** verildi: **fiyat takipçisi**.

Kalan tek madde **F0.4** (marketfiyati ile kanonik ürün kimliği) ve artık fiş okumayı değil **yalnızca F5.4/F5.5'i** kapılıyor. Ayrıca **F2.7'ye bağlı**: katalog yeniden tohumlanamadığı sürece marketfiyati tohumlaması ilk açılışını yapmış bir telefona **hiç ulaşamaz**.

---

## Faz 0 — Fiş ölçümü *(paralel iz)*

> **Ürünün yarısının kapsamı buranın sonucuna bağlı.** Fiş satır adları güvenilir çıkmazsa Neydi bir fiyat takipçisi değil, harcama defteri olur — ve bunu 4. ayda değil şimdi öğrenmek gerekiyor.
>
> Bu iz artık **paralel** ilerliyor: kimlik doğrulama ve fiş biriktirme kullanıcı tarafında olduğu için ana hattı bekletmiyor. Çıktısı **yalnızca Faz 5'i** kapılıyor.

- [x] **0.0 — Cihaz kurulumu.** ✅ Samsung Galaxy S10+ (SM-G975F), **Android 12 / API 31**, 1080×2280 @ 420dpi. `installDebug` çalışıyor, uygulama crash'siz açılıyor, açık ve karanlık mod referans görüntüleri alındı. Tema doğrulandı: açık modda krem `#FBF7F2` + terracotta `#B34418`, karanlık modda `#13100E` + somon `#FF9166`. **İki hata yakalandı → F1.6.** (API 31 ayrıca F8.4 için önemli: Android 12+ SplashScreen API'si bu cihazda geçerli.)
- [x] **0.1 — Fiş test koşumu** *(cihazsız)*. ✅ `tools/receipt-eval/` — TypeScript (Node 24, derleme adımı yok; üretimdeki Cloudflare Worker de TS olacağı için istek şekli birebir aynı). Türkçe fiş prompt'u + structured output şeması + ad/fiyat **ayrı** skorlama + aritmetik kapısı. API şekli `claude-api` skill'inden doğrulandı ve **üç varsayımım yanlış çıktı**: (a) Claude Opus 5'te thinking **varsayılan açık**, ve `disabled` yalnızca `effort` ≤ `high` ile kabul ediliyor — `xhigh`/`max` ile 400; (b) structured output `output_config.format` ile veriliyor, `output_format` kullanımdan kalkmış; (c) şema `minimum`/`minLength` kabul etmiyor ve nullable alanlar `anyOf` ile yazılmak zorunda.
- [x] **0.2 — Fiş ayrıştırma ölçümü** ✅ **Sorusu değişti ve başka yolla cevaplandı.** `ANTHROPIC_API_KEY` gerekmedi, `tools/receipt-eval/` hiç kullanılmadı: ölçüm cihazda ML Kit'in ürettiği **ham metin satırları** üzerinden yapıldı ve o satırlar `ReceiptParserTest`'e OCR hataları dahil aynen taşındı. **Ölçüt aritmetik kapısı ve iki fişin ikisi de tutuyor** (225,50 ve 484,58). Ölçümün asıl bulgusu şuydu: ilk parser baştan sona yanlıştı ve **17 sentetik testi geçiyordu** — örnek fişleri de kuralları da ben yazdığım için kendi varsayımlarımı kendime onaylatıyordum. Gerçek fiş üç varsayımı birden çürüttü. **Üçüncü fiş (~60 kalem) ise fiziksel sınırı ölçtü:** tek karede satır başına **4,7 piksel**, ML Kit 60 satırın 2'sini okudu — yazılımla çözülemez, `UNREADABLE_MESSAGE` bu ölçümden doğdu.

- [x] **0.3 — Aritmetik değişmez** *(cihazsız)*. ✅ **F4.5 ile birlikte yapıldı, bu kutu işaretlenmeyi bekliyordu.** `arithmeticHolds` (`ReceiptParser.kt`) tam olarak `Σ(satır) − İNDİRİM = TOPLAM`, tolerans `TOLERANCE_MINOR = 5` kuruş. Araştırmanın ilk yazdığı `+KDV` formülü **yanlıştı** — Türkiye'de perakende fiyatları kanunen KDV dahil, "TOPLAM KDV" o tutarın *içindeki* verginin dökümü. O formül %100 fişi manuel düzeltmeye yollardı. **Gerçek fişte doğrulandı:** iki fişin ikisinde de satır toplamı "Ödenecek KDV Dahil Tutar" ile birebir tutuyor (225,50 ve 484,58).
  **Kalan iki açık uç, ikisi de ölçülmemiş:** (a) **±5 kuruş toleransı hiç sınanmadı** — iki gerçek fişte fark tam **0**, yani tolerans hiç devreye girmedi; onu doğrulayacak şey tartılı ürün yuvarlaması olan bir fiş ve elimizde yok. Testlerdeki tolerans sınaması (`allowsFiveMinorTolerance`) toplamı **elle** oynatıyor, gerçek yuvarlamayı değil. (b) **İki ayrı kapı birbirinden habersiz:** `arithmeticHolds` indirimleri **çıkarıyor**, Fiş Kontrol ekranı ise ekrandaki kapıyı `lines.sumOf { it.lineTotalMinor }` ile hepsi pozitif toplayarak yeniden hesaplıyor. İndirim satırı içeren bir fişte **ikisi farklı karar verir**. Hiçbir gerçek fişimizde indirim satırı yok, o yüzden hiçbir test bunu görmüyor. → **F5.6**

- [ ] **0.4 — Kanonik ürün kimliği** *(cihazsız)*. **Öncülü değişti, adım duruyor.** Artık "fiş okuma için gerekli" değil — F4.7 alias öğrenmesi cihazda çalışıyor ve eşleştirmeyi kullanıcının kendi kararı besliyor. Bu adım şimdi **yalnızca F5.4/F5.5'in önkoşulu**: fiyat karşılaştırması için harici bir kanonik katalog gerekiyor.
  Kalan iş: çıkan satır adlarını marketfiyati `/api/v2/search` ile bulanık eşleştir. `User-Agent` header'ı **zorunlu** — yoksa endpoint 404 döner. Barkod yolu **yok**: `searchByIdentity` gerçek EAN-13'lerde 6/6 boş döndü (o metot marketfiyati'nın kendi 4 karakterlik iç token'ını alıyor, EAN almıyor) ve katalogda barkod alanı hiç bulunmuyor.
  **Repoda tek satır ağ kodu yok** ve bu ölçülmedi: `io.ktor`/`HttpClient` aramaları boş dönüyor. ktor **3.5.2** katalogda beş artifact'la hazır bekliyor (`client-core`, `client-okhttp`, `client-darwin`, `content-negotiation`, `serialization-kotlinx-json`) ama `composeApp/build.gradle.kts` hiçbirini bağımlılık olarak almıyor. Pinler 13 Ağu 2026'da kondu; Faz 5 başladığında **yeniden doğrulanmalı, güvenilmemeli**.
  **Bir yan kazanç:** marketfiyati'nın döndürdüğü `gramaj` alanı, `packSize` için elimizdeki **tek** dış kaynak — fiş metninden ambalaj boyu çıkarmak F5.7'nin işi ve orada bir tuzak var (bkz. F5.7).

- [x] **0.5 — Kapsam kararı** *(cihazsız)*. ✅ **KARAR: fiyat takipçisi.** Gerekçe ölçümden geliyor, tahminden değil.
  F0.1'de yazılan skorlama eşiği (`tools/receipt-eval/score.ts:193`) satır **adı** doğruluğunu ölçüt alıyordu: ≥0,80 → "fiyat takipçisi uygulanabilir". İki gerçek fişte **10 ürün satırının 10'u da doğru ad ve doğru fiyatla okundu** (BİM 4, File 6; `ReceiptParserTest` bunları cihaz çıktısı olarak sabitliyor). Aritmetik kapısı ikisinde de tuttu. Yani ölçüt karşılandı.
  **Ama iki uyarıyı kayda geçirerek:** (1) İki fiş bir **izlenim**, ölçüm değil — `score.ts:202` bunu kendisi söylüyor. (2) Okunan ad **fişin yazdığı ad** (`"TURŞU KORNI ŞON 670G"`), kanonik ürün adı değil; onu ürüne bağlayan şey F4.7 alias'ı, yani **kullanıcının emeği**. Karar bu yüzden şartlı: fiyat takipçisi, **ama alias öğrenmesi ve (tercihen) F0.4 katalog tohumlaması ile birlikte**. Alias'sız saf otomatik eşleştirme ölçülmedi ve hedef de değil.
  **Faz 5'in kapsamına etkisi:** 5.1 (fiyat yazımı) ve 5.2 (satır ipucu) **kesin kapsamda** — harcama defteri seçeneğinde de gerekliydiler. 5.3 (Ürün Detayı grafiği) **kapsamda**. 5.4/5.5 (marketfiyati + "başka markette ucuz") **F0.4'e bağlı ve son sıraya** alınıyor: dokümante edilmemiş bir endpoint'e bağlı, her an kapanabilir, ve uygulamanın çalışması için gerekli değil.
  **Değer eğrisi hâlâ ölçülmemiş:** araştırmanın "~100 ürün, 6 ayda yalnızca 10-20 trend edilebilir SKU" aritmetiği doğrulanmadı ve doğrulanması için altı ay veri gerekiyor. Bu, F9.5'teki 4. ay riskinin ta kendisi (bkz. **Riskler**).

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

- [ ] **2.7 — Katalog yeniden tohumlanabilir olmalı** *(F0.4 ve "Diğer" kategorisi buna bağlı)*. **Ölçülmüş sorun:** `seedCatalog` `SELECT COUNT(*) FROM category > 0` ile kapıda duruyor, yani **ilk açılıştan sonra** `CatalogSeedData`'daki herhangi bir değişiklik o telefonda **kalıcı olarak görünmez**. Dosyanın kendi KDoc'u tam tersini vaat ediyor: *"Kural değişirse **katalog yeniden tohumlanır**, iki ayrı gerçek kaynağı oluşmaz"* — ve yeniden tohumlama mekanizması **yok**, yani o vaat tutulamıyor.
  **Neyi bloklar:** F0.4'ün marketfiyati tohumlaması, `Diğer` kategorisinin eklenmesi (F3.9), herhangi bir kategori düzeltmesi, ve `matchKey` kuralının değişmesi.
  **⚠️ Sıra kuralı:** tohum id'leri **sıradan türetiliyor** (`"seed-${u.commonality}"`). Bugün zararsız çünkü `Product.seedId` hiç doldurulmuyor — ama F6.2 onu doldurduğu an sıralar **sonsuza kadar donuyor**, ya da bir yeniden tohumlama hane ürünlerini **sessizce yanlış katalog kaydına** yönlendiriyor. Yani: **ya id'ler `seedId` doldurulmadan ÖNCE addan türetilir, ya sıralar değişmez ilan edilir** — ikincisi otomatik tamamlama sırasını, Ekle sheet'i çip sırasını ve Kurulum grid'ini iyileştirme imkânını sonsuza kadar kapatır.

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

- [x] **2.8 — Şema v3: Faz 5/6/7'nin dokuz değişikliği tek bump'ta.** ✅ *Cihazda gerçek v2 verisi üzerine kuruldu, `pm clear` yapılmadan; migration sorunsuz, verinin tamamı sağ.* `version = 3`, `AutoMigration(2, 3)`, `3.json` dışa aktarıldı. Üretilen migration **20 `ALTER` + 2 `CREATE TABLE` + 1 `UNIQUE INDEX`** — boş değil ve bu ayrıca kontrol edildi.
  **Neden tek bump:** dokuz değişiklik üç faza dağılsa **dokuz korumasız bump** olurdu ve her biri F4.1'in sessiz kazasını tekrarlayabilirdi. **Neden şimdi:** `price_observation`, `suggestion_event` ve `pending_op` bugün **boş** — tablo boşken şemasındaki hata bedava, ilk satırdan sonra onu yeniden şekillendirecek `execSQL` yok.
  **Nöbetçi bump'tan ÖNCE yazıldı** (`SchemaBaselineTest`, projenin ilk `androidHostTest` dosyası) ve bump'tan sonra hâlâ yeşil kaldı — temellerin dokunulmadığı iddia edilmedi, **ölçüldü**.
  **İki enum yeniden adlandırma penceresi de bu bump'ta kapandı:** `OpType` ve `SuggestionOutcome` girdileri Türkçeydi ve yayınlanmış şemada **TEXT** olarak duruyorlardı; iki tablo da boş olduğu için yeniden adlandırma bedavaydı. İlk satır yazıldıktan sonra bir girdiyi değiştirmek mevcut satırları yetim bırakırdı — Room'un enum dönüştürücüsü bilinmeyen adı **okurken atar**, yani uygulama kendi geçmişini okurken çökerdi.
  **Tam liste, gerekçeleri ve kural** → **Şema sürüm planı** bölümü.

- [ ] **3.9 — "Diğer" kategorisi** *(F2.7'ye bağlı; birlikte yapılmalı)*. `DEFAULT_CATEGORY = "temel-gida"` — katalogda eşleşmeyen **her** serbest ürün ve **her** fiş düzeltmesi oraya düşüyor. Tasarımın reyon sırası sonda bir **`Diğer`** ile bitiyor; tohumlanan 12 kategoride öyle bir şey **yok**.
  **Neden sessizce bozuyor:** `temel-gida` 245 tohumun **40**'ını zaten tutuyor (en kalabalık ikinci). Katalog *"Türkiye'deki her ürün değil"* ve araştırma 6 ayda ~100 farklı ürün öngörüyor, çoğu **kullanıcının yazdığı**. Hepsi + fiş düzeltmelerinden doğan her ürün **market yürüyüşünün ortasındaki** 6. sıraya dosyalanıyor. Bölümlerin var olma sebebi tek: alfabetik sıra *"insanı markette ileri geri yürütür"*. 6. sırada alakasız ürün biriktiren bir bölüm **tam olarak o sorunu geri getiriyor** — ve hata vermeden, test kırmadan, görünür bir kırılma anı olmadan.
  **İki iş, biri değil:** (1) `Diğer` kategorisi sıra 12'de + `DEFAULT_CATEGORY` onu göstersin; (2) **ürünün kategorisini değiştirme yolu** — bugün hiçbir yerde yok (`ProductDao.update` var, `categoryId`'yi değiştiren çağıran yok). Doğal yeri Ekran 5, tasarımın iki anahtarı koyduğu yerin yanı.

## Faz 4 — Alışveriş kapatma ve fiş

> Verinin dürüst kalmasını sağlayan faz. Ekmek problemini asıl çözen katman burası: fiş, listeye yazılmasa bile ekmeği içeriyor.

- [x] **4.1 — Trip yaşam döngüsü.** ✅ `PLANNING → SHOPPING → CLOSED` + `ownerMemberId`. **Tek cihaz kapatır** — `closeIfOpen` bir **karşılaştır-ve-yaz**: `WHERE id = :id AND completedAt IS NULL`. İkinci kapatma **sıfır satır** günceller, `ownerMemberId` ilk kapatanda kalır, `completedAt` ilerlemez, ve dönen sayı çağırana "ben kapattım" ile "zaten kapanmış" farkını söyler. Önce-oku-sonra-yaz iki adıma bölünse yarış penceresi geri gelirdi. **Kapalılığın otoritesi `completedAt`**, `status` değil: böylece sürüm 1 satırları geri-doldurma gerektirmiyor ve migration tamamen otomatik. **Alışveriş modu artık kalıcı** — ekran durumu değil gezinin durumu; uygulama öldürülüp açılınca korunuyor (cihazda doğrulandı) ve Faz 7'de eşler aynı modu görecek. **Migration eski veriyle cihazda sınandı**: v1 kurulup veri eklendi, üstüne v2 `pm clear` yapılmadan kuruldu. İlk denemede patladı — hata ayıklarken sürümü 1'e çekmiştim ve Room `1.json` şema temelini yeni kolonlarla üzerine yazmıştı, fark boş çıkıp boş migration üretmişti. Yeşil test koşumu bunu **hiç** yakalamazdı.
- [x] **4.2 — Kamera.** ✅ FileKit 0.14.2, sistem kamerası (`ACTION_IMAGE_CAPTURE`) — **CAMERA izni gerekmiyor**, cihazda doğrulandı. **Fotoğraf bloklamıyor**: gezi çoktan kapanmış oluyor, fiş `PENDING` olarak kuyruğa giriyor, OCR sonra koşacak. Görsel platform tarafında küçültülüyor — cihazda ölçüldü: **2944×2208 / 672KB → 2576×1932 / 322KB**, sınır tam tutuyor. Ham dosya sonra siliniyor (fiş kişisel veri; iki kopya hem yer hem gereksiz maruziyet).
  **Üç hata cihazda çıktı ve hiçbiri testle görünmezdi.** (1) Kamera doğrudan hedef dosyaya yazıyor, yani küçültmenin kaynağı ile hedefi **aynı dosya** oluyordu — sessizce işe yaramadı, görsel 2576 yerine 2944 kaldı; bunu ancak **çıktının gerçek çözünürlüğünü ölçmek** yakaladı. (2) Hedef yolu `remember` içinde tutuyordum; kamera ön plandayken Android Activity'yi yeniden oluşturuyor, durum sıfırlanıyor ve fiş **hiç kaydedilmiyordu**. Durumu tamamen kaldırdım — hedef kaynak adından türüyor, kurtarılacak bir şey yok. (3) `PlatformFile.absolutePath()` bir `content://` URI dönüyor (FileProvider), `BitmapFactory.decodeFile` onu okuyamıyor. **İki kez tahmin ettim, ikisi de yanlış çıktı; bir kez günlükleyip ölçtüm ve cevap tek seferde geldi.** Sözleşme artık yol değil **bayt** alıyor, böylece URI/dosya ayrımı sınırın dışında kalıyor.
  **Düzeltemediğim bir yer:** arka kamera `FileKitCameraFacing.Back` ile isteniyor ama sistem kamerası bunu yoksayabiliyor — test cihazı (Samsung) yoksayıyor ve ön kamerayla açılıyor; kullanıcı tek dokunuşla çeviriyor.

- [x] **4.3 — ~~Cloudflare Worker proxy~~ → GEREKMİYOR.** ML Kit cihazda çalıştığı için ortada API anahtarı, proxy, secret yönetimi **yok**. Faz 7'de Supabase keep-alive'ı için Worker yine gerekebilir — o iş **7.6**'da zaten var. Bu madde kapandı; mimari bir bağımlılık eksildi.
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

- [x] **4.13 — Uzun fiş: parça-parça akışı tek dokunuşa indir.** ✅ *Cihazda doğrulandı — ve kullanıcının canlı hata raporuyla kapsamı büyüdü.*
  **Rapor: "parça parça çek demedi hiç."** Veritabanı analizi sebebini buldu: **iki fiş `PENDING`'de takılıydı, işleme hiç başlamamıştı** — çekimden sonra Fiş Kontrol ekranı açılmadan süreç ölürse (bu cihazda belgeli Activity yeniden-yaratma davranışı) fiş sonsuza kadar "bekliyor"da kalıyor ve tavsiye mesajını üreten kod **hiç koşmuyordu**. Mesaj çalışıyordu; ona giden yol kopuktu.
  **Düzeltme 1 — kendi kendine iyileşme:** `ListViewModel` açılışta `receiptDao.pending()`'i işliyor. İşleme idempotent (satırlar silinip yeniden yazılıyor), o yüzden sessizce koşmak güvenli. Cihazda: uygulama açılır açılmaz `PENDING: 0` — ve takılı fişlerden biri **AKYURT'un okunmuş bir parçası** çıktı (1.085,65 TL'lik satır, aşağıda F4.14).
  **Düzeltme 2 — tek dokunuşluk sonraki parça:** okunamayan fişte **"Parça parça çek"**, okunmuş parçada **"Sonraki parçayı çek"** düğmesi — kamera doğrudan açılıyor, yeni parça **aynı geziye** bağlanıyor ve ekran yeni fişe geçiyor (`replaceTop`: üç parçalık fişte üç kontrol ekranı istiflemek geri tuşunu bozardı; F6.4'ün de beklediği primitive bu vesileyle geldi). Ekleme mantığı iki giriş kapısı için `attachReceiptToTrip`'e çıkarıldı — `content://` ve küçültme dersleri artık tek yerde.
  **Düzeltme 3 — parça hata gibi görünmüyor:** toplamı okunamamış fiş, aynı gezide başka fiş varken **"Parça fişi · toplam son parçada"** (nötr) gösteriyor; Geçmiş'te amber çip yerine sönük "parça" etiketi. Tek fişli gezide ya da toplamı okunmuş-ama-tutmayan fişte amber duruyor — o gerçek bir sorun. Üç durumun üçü de testli.
  **Düzeltme 4 — rehber çekimden ÖNCE:** özet kartındaki "Fiş çek"in altına tasarımın kamera overlay metni kondu (*"Uzunsa parça parça çek — her karede 10–15 satır"*) — sistem kamerasına overlay konamıyor, tavsiye başarısız okumadan sonra değil çekimden önce görünmeli.

- [ ] **4.14 — AKYURT iki satırlı düzen: ad, tutar satırından AYRI** *(kullanıcının ana marketi; cihazdaki gerçek parçayla keşfedildi)*. Kullanıcının gözlemi doğrulandı: AKYURT tutar satırında `sıra no + barkod + adet + birim fiyat + %KDV + tutar` basıyor (`3 8683206511079 1 Adet 189,90 %20 189,90`), **ürün adı bir üstteki/alttaki ayrı satırda**. Mevcut ayrıştırıcı "ad + tutar aynı görsel satırda" varsayıyor, o yüzden okunan parçada her satırın adı **barkod** çıktı ve hepsi "yeni" düştü.
  **Yaklaşım kararı (kullanıcının sorusuna cevap):** market başına ayrı ayrıştırıcı **yazılmıyor** — tek ayrıştırıcı + düzen desenleri. Zincire özgü **ad** farklılıklarını zaten zincir-bazlı `ProductAlias` taşıyor; bu ise bir **düzen** farkı ve deseni tek ayrıştırıcıya eklemek, N ayrı ayrıştırıcının bakım yükünü almaktan hem ucuz hem güvenli (desenler birbirini dışlamıyor: tutarlı satır eşleşirse hangi düzen olduğu satırın kendisinden belli).
  **Desen:** tutar satırının adı barkod-ağırlıklıysa (çoğunluğu rakam), komşu **tutarsız** görsel satır ad olarak eşlenir. Barkod ayrıca **saklanmaya değer** — kanonik eşleşme için addan iyi bir anahtar (F0.4'ün "katalogda barkod yok" bulgusu marketfiyati içindi, kendi veritabanımız için değil).
  **Önkoşul — kurgu gerçek OCR'dan gelmek zorunda ve şu an alınamıyor:** `ReceiptReading.rawLines` **kalıcılaşmıyor**, yani cihazdaki parçanın ham satırları geri okunamıyor. Ya geçici bir döküm ile alınacak ya da ham satırlar fişe kaydedilecek (ikincisi F5.7'nin "yeniden ayrıştırma OCR gerektirmesin" ihtiyacına da hizmet ediyor). **Sentetik kurgu yazılmayacak** — o yol bu projede iki kez boşa çıktı.
  **Kabul ölçütü:** kullanıcının AKYURT parçası gerçek ürün adlarıyla ayrışır, satırlar barkod değil ad taşır, ve adlar alias öğrenmesine girer.

- [x] **4.11 — Fiş toplamını geziye devret.** ✅ *(bu oturumda yapıldı ve cihazda doğrulandı: Geçmiş'te gezi başlığı "—" yerine **484,58 TL** gösteriyor.)* Hiçbir şey `Trip.totalMinor`'ı yazmıyordu; özet kartının 36sp'lik tutarı ve Geçmiş'teki gezi toplamı ikisi de onu okuyor, fiş hattı ise `Receipt.totalMinor` yazıyordu — **arada bağı kuran kod hiç yazılmamıştı**, yani ikisi de kalıcı olarak "—" gösteriyordu. Ne derleyici ne testler görebilirdi: iki alan da geçerli, sadece biri hiç dolmuyordu.
  **Fişlerin toplamı alınıyor, tek fiş kopyalanmıyor** — uzun fiş parça parça çekiliyor ve bir geziye birden fazla fiş bağlanabiliyor. `SUM()` boş kümede `NULL` dönüyor, **0 değil**, ve bu tam istenen: "bilmiyoruz" ile "bedava" ayrı şeyler.
  **Fişte yazan toplam esas, satırların toplamı değil:** kullanıcı bir satırı düzeltince ödenen para değişmiyor, yalnızca bizim OCR hatamız düzeliyor. Toplam okunamadıysa alan **null kalıyor** — doğrulanmamış bir sayıyı 36sp'de manşet yapmak, kullanıcının sorgulayamayacağı bir yerde tahmini gerçek gibi sunmaktı.

- [x] **4.12 — Üç-sonuç seçici: `[Aldım] [Gerekmedi] [Unuttum]`.** ✅ *Cihazda doğrulandı: Bitir ekranında üç düğme; "Gerekmedi"ye dokununca satır `checked=0, takeOutcome=NOT_NEEDED` oldu ve Ekmek'in `purchaseCount`'u 4'ten 3'e düştü. Fiş Kontrol'de **"Listede vardı, fişte yok (2)"** katlanır bölümü gerçek File fişiyle göründü.*
  **Tasarım planımı düzeltti:** üç düğme Bitir ekranının geneline değil, **fişin aklamadığı** satırlara ait — Mod A'da *"Listede vardı, fişte yok (N)"* (varsayılan katlı), Mod B'de *"Listede vardı, işaretlemedin"*. Fiş neyin alındığını zaten kanıtlıyor; üç-sonuç sorusu yalnızca fişin doğrulamadığı satırlar için anlamlı.
  **`checked` ve `takeOutcome` TEK yazmada** (`setOutcome`): iki ayrı yazma arada kesilirse "alındı ama unuttum" gibi kendisiyle çelişen bir satır bırakırdı — hem istatistiğe alım olarak girer hem skorda unutulmuş sayılırdı. NOT_NEEDED/FORGOTTEN satırı işaretsiz bırakıyor, yani her sonuç yazımından sonra tetiklenen istatistik yeniden kurulumu onu alım saymıyor.
  **Yeni bileşen `OutcomePicker`** (`Modifier.pressable` üzerine) — Material3 `SegmentedButton` da indication'ı sabit kodluyor.
  **Spec'ten hâlâ açık kalanlar:** `[Elle sadece toplam gir]` yolu (yeni klavye olamaz — tek klavye fiş tutarı alanı) ve süre satırı (`durationMinutes` hep null; alışveriş modunun başlangıç zamanı saklanmıyor). Bunlar F6.7/F10 tarafında küçük işler olarak duruyor.

- [x] **4.13b — Aritmetik kapısı parçalı fişte yanlış alarm veriyordu.** ✅ *Cihazda gerçek veriyle doğrulandı.*
  **Hata:** kapı her **fotoğrafa** ayrı uygulanıyordu, halbuki aritmetik değişmez **fiziksel fişe** ait. Uzun fişte TOPLAM yalnızca son parçada basılı ama o parça satırların yalnızca bir bölümünü taşıyor — yani son parçada kapı **yapısal olarak** tutmuyor çıkıyordu. Her uzun fişte, her seferinde, kullanıcı hiçbir hata yapmadan amber *"toplam tutmuyor"*. Ara parçalar bundan yalnızca toplamları `null` olduğu için kurtuluyordu: `isPart = total == null && tripReceiptCount > 1` koşulu tam olarak son parçayı dışarıda bırakıyordu.
  **İlk düzeltme yanlıştı ve cihaz onu çürüttü.** Kapıyı **gezi** kapsamına almak denendi; kullanıcının veritabanında tek gezide **iki ayrı mağaza** fişi çıktı (BİM 225,50 + File Market 484,58). Gezi kapsamı, File Market'in doğru okunmuş **sessiz** halini BİM'in ayrıştırma hatasıyla amber'a çeviriyordu — düzeltilmek istenen hatanın yer değiştirmiş hali.
  **Doğru kapsam fiziksel fiş** (`samePhysicalReceipt`): künye yalnızca fişin **başında** basılı olduğu için uzun fişin ilk parçası mağaza adını taşıyor, sonrakiler taşımıyor; iki ayrı mağaza fişiyse ikisi de kendi adını taşıyor ve adlar farklı. Gruplama `chainKey` üzerinden — alias öğrenmesiyle **aynı** anahtar, yoksa iki ayrı "aynı mağaza" tanımı oluşurdu.
  **Yan kazanç:** bindiren çekim artık yakalanıyor. Kullanıcı aynı satırları iki parçada birden çekerse toplam fişin toplamını **aşar** ve kapı bunu söyler; bugün hiçbir şey söylemiyordu.
  **Cihazda:** AKYURT parçası nötr *"parça"*, FiLE MARKET çipsiz/sessiz, BİM amber *"toplam tutmuyor"* — sonuncusu **hak edilmiş** (satırlar 227,89, basılı 225,50; fark 2,39 = `TOPLAM KDV` satırı ürün sanılmış, ayrı bir ayrıştırıcı hatası).
  10 yeni test (`ReceiptGroupingTest` + kapsam testleri), 206 test 0 hata.

- [ ] **4.15 — Parça dikişi: bindirmeyi tehlike değil çapa yap.** *(kullanıcı önerisinden doğdu)*
  **Kullanıcının önerisi:** tek fotoğraf çekilsin, arkada uygun okuma düzenine göre parçalansın; parçalama sınırları net olsun ki aynı satır iki kez okunmasın.
  **Birinci yarısı fizik nedeniyle olmuyor:** kırpmak piksel üretmiyor. Ölçüm kayıtlı (`ReceiptProcessor.MIN_USABLE_LINES` gerekçesi): ~60 kalemlik fiş tek karede satır başına **4,7 piksel**, ML Kit 60 satırın **2'sini** okuyabildi — ve *"ham kamera çözünürlüğü bile ~7 px/satır verirdi"*. Yani çok kare **zorunlu**; `MAX_LONG_EDGE` kaldırılsa da değişmiyor.
  **İkinci yarısı haklı ve çözülmemiş:** bugün parçalar birbirinden habersiz. Kullanıcıdan **temiz kesim** istemek yanlış yük — doğru olan kasıtlı **bindirme** istemek ve dikişi uygulamanın yapması: *"bir önceki karenin son 2-3 satırı görünsün"*. Bindiren satırlar dikişin **çapası** olur, tehlikesi değil; belge tarayıcılarının panoramada yaptığı bu.
  **Ne kazandırır:** tek okuma → aritmetik kapısı fişin tamamını gerçekten doğrular (4.13b bunun yarısını zaten getirdi), satırlar bir kez sayılır, kullanıcı tek kontrol ekranı görür.
  **Bugün ne koruyor, ne korumuyor:** `purchaseCount` güvende — `ProductStatsDao.purchaseEvents` `GROUP BY productId, tripId` yapıyor, yani aynı ürün iki parçada okunsa da bir kez sayılıyor. **F5.1'in fiyat gözlemlerinde bu koruma yok**; dikiş ya da tekilleştirme F5.1'den önce gelmeli, yoksa çift fiyat kaydı yazılır.
  **Önkoşul F4.14 ile ortak:** ham satırlar kalıcı olmadan dikiş yazılamaz.

- [ ] **5.1 — `PriceObservation` yazımı.** *Fazın kilit taşı; her şey buna bakıyor.*
  **Hazır olan:** entity `Receipt.kt:91` (11 alan, `(productId, observedAt)` ve `storeId` index'leri, şema **sürüm 2**'de yayında). **Eksik olan:** `PriceObservationDao` (`Daos.kt:254`) **yalnızca iki okuma sorgusu içeriyor — `@Insert`, `@Upsert`, `@Update`, `@Delete` hiçbiri yok**. Repo genelinde `PriceObservation(` yapıcısı **sıfır** yerde çağrılıyor.
  **ReceiptLine → PriceObservation eşlemesi, alan alan:**
  - `householdId` / `receiptLineId` → doğrudan.
  - `productId` **NOT NULL** ← `matchedProductId` **nullable**. Süzgeç zorunlu ve kuralı zaten yazılı (`Product.kt:49`): *"Onaylanmamış eşlemeler fiyat geçmişine yazılmamalı — yanlış eşleme fiyat trendini bozar ve bozuk trend, hiç trend olmamasından kötüdür."* Pratik süzgeç: `matchedProductId != null && !needsReview`. **Karar gerekiyor:** `confidence` 1,0 (alias) ile 0,9 (çıplak `matchKey`) arasında ayrım yapılacak mı — bugün 0,9 da `needsReview = false` ile otomatik kabul ediliyor.
  - `observedAt` ← **`ReceiptLine.createdAt` DEĞİL.** O alan OCR yazma zamanı ve **her yeniden okumada değişiyor**. Doğrusu satın alma zamanı; en yakını `Receipt.capturedAt`. Fişin **bastığı tarih** ise hiç ayrıştırılmıyor → **F5.8**.
  - `unitPriceMinor` **NOT NULL** ← `ReceiptLine.unitPriceMinor` varsa o, yoksa `lineTotalMinor / quantity`. **Ölçüldü: bölme yolu istisna değil, ASIL yol** — iki gerçek fişin 10 ürün satırından yalnızca **4'ünde** birim fiyat var (yalnızca ürün satırından hemen önce bir `QUANTITY_LINE` geldiğinde doluyor). Tartılı üründe `quantity` kesirli (0,182) — bölme **tam sayıya kırpılmamalı**.
  - `storeId` → **bugün dürüst tek değer null** (bkz. F5.9).
  - `packSize` / `packUnit` → **bugün null** (bkz. F5.7).
  **Nereye bağlanacak:** F6.1 ile aynı yere — gezi kapanışı. Bu varsayım şemada zaten yazılı: `TripStatus.CLOSED` KDoc'u *"kapanış mutabakatı fiyat gözlemleri ve satın alma sayaçları yazıyor; yeniden açmak onları ikinci kez yazma riski demek"* diyor — yani tasarım bunu **çoktan varsayıyor, kod hiçbir yerde yapmıyor**.
  **Kabul ölçütü (cihazda görülebilir, UI değişikliği gerektirmeden):** `EstimatedBasket` satırı ilk kez çizilir. Bugün `pricedCount == 0` olduğu için o satır **hiç** çizilmiyor (`BasketAndSummary.kt:47` erken dönüyor: *"Hiç fiyat bilinmiyorsa satır HİÇ ÇİZİLMEZ; 0,00 TL yazmak yalan olurdu"*). İlk `PriceObservation` satırı yazıldığı an görünür — F3.8'in `[~]` işareti tam bu yüzden duruyor.
  **Karar gerekiyor — senkron:** `PriceObservation` `householdId` + `deletedAt` taşıyor, yani Conventions kuralı 2'ye göre **senkron edilecek kullanıcı verisi**. `PendingOp` outbox entity'si var (`Sync.kt:17`) ama **DAO'su yok ve hiçbir yazma kuyruk kaydı üretmiyor**. Ya yalnızca yerel yazıp Faz 7'ye bir geri-doldurma borcu bırakılacak, ya da "yerele yaz + kuyruğa ekle" deseni **burada** kurulacak.

- [x] **5.6 — İki aritmetik kapısı TEK'e indi.** ✅ Ayrıştırıcının kuralı (indirimler **çıkarılır**) artık ekranda da geçerli: `ParsedLine.discount` ve `ParsedLine.unit` v3 kolonlarına **kalıcılaşıyor** (eskiden ölçülüp atılıyordu) ve ekran kapısı `isDiscount` üzerinden aynı hesabı veritabanından yeniden kuruyor. İndirimli fişte işlemcinin yazdığı durum ile ekrandaki çip artık çelişemez.
  **⚠️ İndirim kurgusu SENTETİK ve bu yazılı:** gerçek fişlerimizin hiçbirinde indirim satırı yok — testin kendi yorumu, gerçek indirimli fiş geldiğinde kurguyla değiştirilmesini söylüyor (kendi örneğiyle kendini onaylama tuzağı F4.4'te kayıtlı).

- [x] **5.8 — Fişin basılı tarihi ayrıştırılıyor.** ✅ *Cihazda doğrulandı ve alanın var olma sebebi ilk gerçek fişte göründü: fiş **12.08.2026 18:46** basıyor, fotoğraf 14'ünde çekilmişti — tarih olmasaydı fiyat geçmişi iki gün kayacaktı.*
  `gg.aa.yyyy [ss:dd]` deseni; **yıl dört hane şart** (POS referans numaralarındaki kısaltmalar yanlış pozitif üretir). Geçersiz takvim değerleri (ay 13, 31 Nisan) **null** — bozuk OCR'dan uydurma damga üretilmez. Dönüşüm **cihazın saat diliminde**: fiş yerel saatte basılıyor, UTC varsaymak İstanbul'da 3 saat kaydırırdı.
  **Dürüst süreç notu:** alan ataması sessiz bir eşleşmeyen-replace yüzünden düşmüştü ve yeni test yakaladı — regex ve dönüşüm tek başına doğruyken `parseReceipt` null döndü. Tek satırlık düzeltme; test işini yaptı.

- [x] **5.9a — Mağaza adı: şirket satırı adrese tercih ediliyor.** ✅ *(F5.9'un ayrıştırıcı yarısı; `Store` tablosu/DAO'su hâlâ F5.9'da.)* Eski kural "ilk anlamlı satır"dı ve gerçek File fişinde **adresi** yakalıyordu. Şimdi şirket işareti taşıyan satır (`magazacilik/magazalar/market/sirket/a s/ticaret`) görülene kadar ilk aday yedekte tutuluyor. Cihazda başlık artık **"FiLE MARKET MAĞAZACILIK ANONİM ŞİRKETİ"**. `chainKey` ikisinden de aynı zinciri ürettiği için alias'lar etkilenmedi. Dosyanın **ilk mağaza adı testleri** de bununla geldi — önceden sıfırdı.

- [ ] **5.10 — Mükerrer gözlem koruması** *(cihazsız, F5.1 ile birlikte)*. **Bugün hiçbir şey iki kez yazılmasını engellemiyor.** `price_observation` tablosunda **tek bir UNIQUE kısıt yok** (yalnızca iki tekil-olmayan index) ve şemada **hiç foreign key yok**, yani cascade de yok.
  **Somut senaryo, kullanıcı erişimindeki bir düğmeyle tetiklenir:** Fiş Kontrol'deki *"Başka yönde oku"* `processor.process()`'i yeniden çağırıyor; o da `clearForReceipt` ile satırları **sert siliyor** ve **yeni id'lerle** yeniden yazıyor. Eski satırlara bağlı gözlemler **yetim** kalır, yeni satırlar için **ikinci** bir gözlem kümesi yazılır. Gözlemin ikiye katlanması hem trendi hem sepet tahminini bozar.
  **Üç seçenek, biri seçilmeli:** (a) `UNIQUE(receiptLineId)`; (b) gözlem yazımını `receiptId` kapsamında sil-ve-yeniden-yaz (idempotent); (c) gözlemleri yalnızca **tekrarlanamayan** tek bir anda yazmak (gezi kapanışı) ve yeniden okumanın onları yeniden yazmasını yasaklamak.
  **Test deseni hazır ve taklit edilmeli:** `ConstraintTest` kısıtları **DAO üzerinden değil ham SQL ile** sınıyor — çünkü DAO `OnConflictStrategy.REPLACE` ile yazsaydı ikinci satır sessizce üstüne biner ve test yeşil kalırdı, koruma da olmazdı.

- [ ] **5.2 — Satır fiyat ipucu.** *Çizim tarafı **bitmiş**; eksik olan veri kaynağı ve bir eşleyici.*
  **Hazır olan — dokunulmayacak:** `PriceHint` (`RowModel.kt:18`) dört varyantlı **sealed interface**: `None` (0 gözlem — *hiçbir şey* çizilmez, "fiyat yok" **asla** yazılmaz) · `Single(price, store, daysAgo)` · `Trend(from, to, deltaPercent, rising, history)` · `PackChanged(fromPack, toPack, note)`. Sealed olmasının gerekçesi dosyanın kendi KDoc'unda: *"1 gözlem + trend" temsil edilebilir olmamalı*. `ListItemRow.SecondLineContent` dördünü de **exhaustive** çiziyor, `DeltaChip`, `Sparkline` (2 gözlemin altında kendini çizmiyor) ve ikinci satır önceliği (`secondLine()`: fiyat > gerekçe > not) hepsi yerinde. Alışveriş modunda metadata **otomatik gizleniyor** — reyonda fiyat ipucu çıkmıyor.
  **Eksik olan — üretici taraf:** tek üretim eşleyicisi `ListRowProjection.toUiRow` (`ListState.kt:64`) ve `priceHint`'i **hiç set etmiyor**; varsayılan `None` kalıyor. `ListRowProjection` da hiç fiyat kolonu taşımıyor. Yani üç şey gerekiyor: (1) SQL projeksiyonunu genişletmek **ya da** tek anahtarlı bir harita, (2) `formatMinor` ile **biçimlenmiş metin** üretmek (varyantlar `Long` kuruş değil `String` taşıyor), (3) `daysAgo` için bir **saat** — bugünkü eşleyici saf ve saat taşımıyor; proje kuralı saati enjekte etmek (`clock: () -> Long`).
  **Mimari kısıt, ihlal edilmemeli:** liste satırları bilerek **TEK** SQL ifadesinde çekiliyor (`observeList`) ve gerekçesi yazılı: *üç Flow'u `combine` etmek her değişimde üç yeniden yayın üretir ve satırlar bir kare boşluklu görünür*. Yani **satır başına fiyat Flow'u yasak**; ipucu ya sorguya JOIN edilecek ya da tek seferde anahtarlı harita olarak gelip bir kez birleştirilecek.
  **Eksik sorgular** (hepsi `PriceObservationDao`'da yok): son ödenen tek satır; ürün başına son N gözlem serisi ("Son 8 gözlem"); pencere içi min/ortalama; mağaza bazlı karşılaştırma; aralık kapsamlı okuma; **liste için tek seferde toplu okuma**.
  **Dormant kırpılma hatası burada uyanıyor:** `PriceChip` sabit **92dp** genişlikte ve `maxLines`/`overflow` **taşımıyor** — dört haneli bir fiyat (repo'nun kendi önizlemesi `PriceChip("1.289,90 TL")` ile *"92dp bütçesini zorlayan gerçek senaryo"* diye işaretliyor) ikinci satıra **sarar** ve satır yüksekliği 56/68/72dp merdiveninin dışına çıkar. Bu faz başlarken `maxLines = 1` + `Ellipsis` eklenmeli.
  **Kabul ölçütü:** cihazda, gerçek bir fişten sonra, bir listede tek gözlemli ürün "son X TL · mağaza · N gün önce" gösterir; ikinci alışverişten sonra aynı satır trend + delta çipi + sparkline gösterir.

- [x] **5.9 — Mağaza çözümlemesi (`Store` yazımı).** ✅ *Cihazda doğrulandı (tasarım kararı 11).* Entity v1'den beri duruyordu ve **hiçbir yazanı yoktu**; `Trip.storeId` her satırda null'dı.
  **Satır fişten doğuyor, elle eklenmiyor:** elle girilen bir mağaza fiyat karşılaştırmasına hiçbir veri katmıyor — karşılaştırmayı besleyen şey fişin kendisi. Bu yüzden `StoreDao` yalnızca `insert` + `findByChain` + `observeAll` taşıyor; güncelleme/silme çağrıları onları çağıracak bir yüzey olmadığı için **yazılmadı**.
  **Tekillik zincirde:** aynı marketin iki şubesi tek satır üretiyor (`chainKey`), çünkü fiyat karşılaştırması zaten zincir bazında anlamlı. Ad `storeDisplayName` ile saklanıyor (karar 13) — Ayarlar'da ticari unvan çizmek Fiş Kontrol'de düzeltilen şeyi geri getirirdi.
  **Yan ürün olmayan bir ayrıştırıcı düzeltmesi:** künye okunamadığında yedek mağaza adayı **tarih satırını** alıyordu. Ekranda geçici bir yanlışlıktı; bu adımdan sonra kalıcı olurdu.
  `TripDao.setStoreIfAbsent` — ilk okunan mağaza kalıyor, ikinci parça yanlış okunursa gezi mağaza değiştirmiyor.
  **Yarısı hazır:** `chainKey()` (`ReceiptProcessor.kt:180`) ham mağaza adını **zincire** indiriyor ve testli (`"BIM BIRLESIK MAGAZALAR A.S." → "bim"`, `"BIM BADEMLIK SUBESI" → "bim"`). Ama yalnızca **alias anahtarı** olarak kullanılıyor.
  **⚠️ Ölçülmüş hata — bu adımın ilk işi:** mağaza adı yakalaması (`ReceiptParser.kt:191`) "6 karakterden uzun, sonunda tutar yok, içinde arşiv/fatura yok" ilk satırı alıyor. Gerçek File fişinde bu **`FiLE OVACIK / KEÇİÖREN/ ANKARA`** — yani **adres satırı**, bir alt satırdaki `FiLE MARKET MAĞAZACILIK ANONİM ŞİRKETİ` değil. Cihazda Fiş Kontrol başlığında aynen böyle görünüyor. `chainKey` ikisinden de "file" ürettiği için alias etkilenmiyor, ama `Store.name` adres olur. **Ve bunu hiçbir test tutmuyor:** `ReceiptParserTest` içinde `storeName` üzerine **tek assertion yok**.
  **Karar gerekiyor:** `chainKey` şubeyi bilerek atıyor, ama `Store` KDoc'u `name`'in şube, `chain`'in zincir olduğunu söylüyor. Görünen adı ne besleyecek? Ayrıca `chainKey(null) == "bilinmiyor"` için: sentinel bir Store satırı mı, `storeId = null` mı?
  **İş:** `StoreDao` (`insert` + `findByChain` + `observeAll`) + accessor + Koin; `findOrCreateStore` — deseni `findOrCreateProduct`'tan **birebir kopyala**; çağrı yeri `ReceiptProcessor.process` içinde `setTotal`'ın yanı (orada hem `reading.storeName` hem `chain` zaten elde); `TripDao.setStoreId`.

- [ ] **5.7 — Ambalaj boyu çıkarımı (`packSize` / `packUnit`).** *Shrinkflation korumasının tek besleyicisi.*
  **Bugün hiç çıkarılmıyor** ve bu yüzden `PriceHint.PackChanged` **asla tetiklenemez**. `ParsedLine`'da ambalaj kavramı yok; oradaki `unit` alanı **kaç alındığını** tutuyor (2 ad, 0,182 kg), bir paketin **ne kadar** olduğunu değil — ve o alan bile kalıcılaştırmada atılıyor (`ReceiptLine`'da `unit` kolonu yok).
  **Veri aslında orada, ürün adının içinde serbest metin olarak:** `TURŞU KORNI ŞON 670G`, `SRIRACHA S0S 230 GR`, `İNCEYULAF350G`, `KREMA 18YAĞLI 200ML`.
  **⚠️ Somut tuzak, sırası kritik:** `VAT_MARK_SUFFIX` (`ReceiptParser.kt:122`) bozuk KDV işaretlerini temizlemek için addaki **sondaki üç haneye kadar sayısal artığı** siliyor — ölçülmüş örnek: `ALIŞVERIŞ POŞETi BiM 220` → `ALIŞVERIŞ POŞETi BiM`. Yani çıplak sayıyla yazılmış bir ambalaj boyu (`PEYNIR 600`) **ad temizlenmeden önce yok ediliyor**. Çıkarım `match.groupValues[1]` üzerinde ya da `rawText` üzerinde **temizlikten ÖNCE** koşmak zorunda. Temizleyicinin bugünkü davranışını tutan bir test var, yani onu zayıflatmak yakalanır.
  **Yeniden kullanılacak:** `QuantityParser.UNITS` haritası ambalaj biriminin ihtiyaç duyduğu sözlüğü **zaten** kanonikleştiriyor (kg/kilo→kg, gr/gram→g, lt/litre→L, ml, adet). Ama `parseQuantity`'nin kendisi kullanılamaz: deseni sayıyı `^` ile başa sabitliyor, yani **baştaki** miktarı ayrıştırıyor, sondakini değil.
  **Alternatif/ek kaynak:** marketfiyati'nın `gramaj` alanı (F0.4) — fiş metnine hiç güvenmeyen bir yol.
  **Kabul ölçütü:** aynı ürünün 1 L ve 900 ml gözlemi varken satır **trend göstermez**, `900 ml → 800 g · aynı fiyat` biçiminde `PackChanged` gösterir. Ambalaj küçülmesi **asla** fiyat düşüşü gibi görünmemeli.

- [ ] **5.3 — Ürün Detayı sheet (Ekran 5).** *Hiçbir biçimde yok: composable yok, ViewModel yok, NavKey yok, iskelet bile yok.*
  **Hedef DEĞİL, sheet:** `Destinations.kt` KDoc'u kararı yazıyor — *"EKLE ve ÜRÜN DETAYI de hedef değil, Liste üzerinde açılan bottom sheet'lerdir"*. Yani Ekle sheet'iyle aynı desen: `ListViewModel` state'i (`_sheetOpen`/`_sheetCategory` örneği).
  **Giriş noktası hazır ve bağlanmamış:** `ListItemRow.onPriceClick` parametresi var, `PriceChip`'e geçiyor, **ama hiçbir çağıran onu vermiyor** — yani çip bugün tıklanamaz. `PriceChip` kendi 44dp hedefine sahip; bileşenin belgelenmiş sözleşmesi *"fiyat çipi fiyat geçmişini açar"* ve o vaat karşılanmamış.
  **Ekranın asıl unsuru grafik değil, cümle:** **24sp Fraunces manşet cümlesi** — okunacak ve ekran görüntüsü alınacak şey o. 36sp Fraunces öncülü `BasketAndSummary.kt:141`'de var, yazı tipi erişimi `neydiDisplayFamily()`.
  **Grafik elle çizilecek:** `Sparkline`'ın KDoc'u bunu açıkça söylüyor — büyük fiyat grafiği de Canvas, **grafik kütüphanesi yok**. Her gözlem bir nokta + **minimum** ve **ortalama** referans çizgileri.
  **Aralık seçici tam olarak 1 ay / 6 ay / 1 yıl; "1 hafta" YASAK** — 10 günlük tempo haftalık çözünürlük taşımaz, haftalık grafik gürültüden başka bir şey göstermez.
  **İki alt anahtar buraya ait:** *"Her zamankilere ekle"* (`isStaple` yazan **ilk ve tek** yer olacak — bkz. F6.8) ve *"Bunu önerme"* (engelleme listesinin giriş noktası — bkz. F6.5).
  **⚠️ Bilinen kırpılma riski baştan hesaba katılmalı:** kısmi açık `ModalBottomSheet` içeriği **sınırsız** yükseklikle ölçüyor ve taşanı kaydırmıyor, **kırpıyor** (F3.7'de beş denemeyle öğrenildi, F10.5 açık). Uzun bir grafik sheet'i aynı duvara çarpar — yükseklik tavanı + kaydırma **baştan** konmalı.
  **Eksik veri sorguları:** min/ortalama ve aralık kapsamlı okuma (bkz. F5.2 listesi), artı nokta başına ambalaj etiketi (F5.7) ve birim seçici (paket fiyatı / kg-lt fiyatı).
  **İki boş durum:** hiç gözlem yok · tek gözlem var (→ trend **yok**, yalnızca o nokta).

- [ ] **5.4 — marketfiyati entegrasyonu** *(F0.4'e bağlı; fazın sonunda)*. `/api/v2/search`, `User-Agent` **zorunlu** (yoksa 404), **agresif cache**. Endpoint dokümante değil — **her an kapanabilir kabul et**. Dönen alanlar: başlık, marka, gramaj, kategori ve BİM/A101/ŞOK/Migros/CarrefourSA/Tarım Kredi/Hakmar için mağaza bazlı fiyatlar, lat-lon süzgeci ve ~saatlik tazelik.
  **"Nerede ucuz" bloğu çevrimdışıyken SESSİZCE yok olur** — market reyonunda hata mesajı göstermez. Bu bir tercih değil kural: reyonda elleri dolu bir insana ağ hatası göstermek özelliği zararlı yapar.
  **Sıfırdan başlıyor:** repoda `HttpClient` yok. ktor 3.5.2 katalogda hazır ama bağımlılık değil. **Pinler eski**, Faz 5 başında yeniden doğrulanmalı.
  **Araştırmanın önerdiği ama yapılmayan iş buraya ait:** kanonik ürün kataloğunu marketfiyati'ndan **önden tohumlamak**. Onun yerine 245 ürünlük **elle yazılmış** katalog (`tools/catalog/gen_catalog.py`) konuldu — çalışıyor ama araştırmanın "değer eğrisinin en ucuz düzeltmesi" dediği şey **yapılmadı**, sessizce ikame edildi. Bu adımda geri gelmeli.

- [ ] **5.5 — "Başka markette ucuz" çipi** *(F5.4'e bağlı; en son)*.   **Çip hazır:** `ListRow.cheaperElsewhere` + `AccentChip` ile çiziliyor, alışveriş modunda **bastırılıyor**, ve ürün adını kırpmaması için **bilerek ikinci satıra** taşınmış (F3.1'de birinci satırda yatay bütçe çalıp adı kırpmıştı). Amber dolgunun 1,5dp kontur kuralı `AccentChip`'in içinde tek noktada zorunlu — **kendi çipini yazma**.
  **Eksik olan kural:** **liste başına en fazla 3**, mutlak TL tasarrufuna göre sıralı. Üstü listeyi reklam yüzeyine çevirir ve özellik kesilir. **Bunu uygulayacak yer bugün yok:** `toSections` satır satır eşliyor ve **satırlar arası state taşımıyor**, `toUiRow` tek projeksiyon görüyor. Ya eşlemeden önce tüm listeyi gezen bir ön geçiş top-3'ü seçecek, ya da bölümler kurulduktan sonra bir süsleme adımı gelecek.

- [ ] **5.11 — İki küçük biçimlendirici** *(F5.2, F5.3, F6.4 ve Ekran 1 başlığı buna bağlı)*.
  **(a) Tam TL biçimlendirici yok.** `formatMinor` **her zaman iki ondalık** basıyor. Dört yüzey tam lira istiyor: `Tahmini sepet: ~640 TL`, başlıktaki `Son alışveriş: 8 gün önce · 642 TL`, Ekran 5 manşeti, ve "Nerede ucuz" bloğu. Bugün sepet satırı *"en az 640,00 TL"* basıyor.
  **Beraberinde gelmesi gereken kural:** **tahminler ve manşetler yuvarlar, gerçekten ÖDENEN tutarlar asla yuvarlamaz.** Özet kartında `642,50 TL` gerçek bir sayı ve onu `642` basmak, veri modelinin bütün etiğinin karşı durduğu türden **sessiz bir yalan** olur. Fonksiyonun KDoc'u nerede kullanılabileceğini **ve nerede kullanılamayacağını** yazmalı.
  **(b) Göreli gün biçimlendirici yok ve naif hali burada YANLIŞ.** Gereken metinler: "12 gün önce" (`PriceHint.Single.daysAgo` zaten hesaplanmış bir sayı bekliyor ve **onu hesaplayan hiçbir çağıran yok**), "bugün"/"dün", Ekran 5'in tempo satırı, ve F6.4'ün dört gerekçe şablonu. **Tuzak Riskler bölümünde:** `(now - then) / 86_400_000` 24 saatlik blok sayar, takvim günü saymaz. Aynı aritmetik **hem gösterimde hem `medianIntervalDays` hesabında** kullanılmak zorunda.

## Faz 6 — Öneri motoru

> **Sıra: 6.8 → 6.1 → 6.2 → 6.3 → 6.4 → 6.5 → 6.6 → 6.7.** 6.8 en başta çünkü onsuz 6.4'ün ikinci bölümü **kalıcı olarak boş** kalır ve "boş ise ekran hiç açılmaz" kuralıyla birleşince **Ekran 3 hiç açılamaz**.
>
> **Fazın durumu tek cümlede:** iki tablo (`product_stats`, `suggestion_event`) cihazda **var ama Kotlin'den erişilemiyor** — DAO'ları, accessor'ları ve Koin binding'leri yok. Öneri çipi bileşeni hazır ama **gerekçe yerine birim** gösteriyor. Üç ekran iskelet ve **ikisine hiçbir yerden gidilemiyor**.

- [x] **6.8 — `isStaple` yazma yolu.** ✅ *Cihazda uçtan uca doğrulandı: satıra uzun basıldı → sheet açıldı → anahtar açıldı → satır **"Her zamankiler"** bölümüne geçti (raptiye + %70 opaklık); gezi kapatıldı, yeni gezi açıldı ve **sabit kendiliğinden geri geldi**; alışveriş modunda ise kendi reyonunda göründü.*
  **Sorun neydi:** `isStaple` **beş yerde okunuyordu, sıfır yerde yazılıyordu**. Yani %70 opaklık dalı ve raptiye çalışan uygulamada erişilemezdi, `ProductDao.observeStaples` çağıranı olmayan ölü koddu, Ekran 3'ün *"Her zamankiler"* bölümü daima boş kalırdı — ve *"boş ise ekran açılmaz"* kuralıyla birleşince **Ekran 3 hiç açılamazdı**.
  **Yazma yolu:** `ProductDao.setStaple` + `ListRepository.setStaple`. `updatedAt` de yazılıyor (v3 kolonu) — LWW'nin karşılaştıracağı damga olmadan senkron bu yazmayı kaybedebilirdi.
  **Otomatik ekleme `openOrGetActiveTrip`'in İÇİNDE, ayrı bir fonksiyonda değil.** Tasarım bunu özet kartında kullanıcıya açıktan söylüyor: *"Bir sonraki alışverişte her zamankiler yeniden eklenecek."* Gezinin doğduğu tek yer o ve KDoc'u da *"yeni gezi açmadan önce mutlaka buradan geçilmeli"* diyor; tohumlamayı çağıranın sorumluluğuna bırakmak bu projede defalarca yaşanan **"çağırmayı unut"** hatasını davet ederdi — ve unutulsa sessizce yalnızca boş liste görünürdü. Bu yüzden `memberId` parametresi **zorunlu ve varsayılansız** eklendi (sabit satırların da bir ekleyeni olmak zorunda) — 28 çağrı yeri güncellendi.
  **"Her zamankiler" bölümü YALNIZCA planlama modunda.** Tasarım maketlerinde bölüm planlama modunda en üstte, sayısıyla ve satır başına raptiyeyle duruyor; **alışveriş modu maketinde hiç yok**. Sebebi aynı ekranın iki modunun farklı işi: reyonda sıra **donuyor** ve sabit bir ürün de sonuçta bir reyondan alınacak, yani onu listenin başına çekmek market yürüyüşünü bozardı. En fazla **12 satır** (`STAPLE_LIMIT`).
  **Giriş noktası: Ürün Detayı sheet'inin sıfır-gözlem hali** — tasarımın kendi affordance'ı. Maketlerde *"Her zamankilere ekle"* ve *"Bunu önerme"* anahtarları sheet'in **üç veri halinin hepsinde** var, sıfır gözlemli halde bile; yani bu hal fiyat verisine hiç ihtiyaç duymuyor ve Faz 5'i beklemesi gerekmiyordu. Alternatif anahtarı geçici olarak Ayarlar'a koymaktı — tasarımın belirlediği yer yerine yeni bir yer icat etmek olurdu.
  **⚠️ Açıcı hareket bir ara çözüm ve karar senin:** tasarım planlama modunda sheet'i **fiyat çipinden** açıyor, ama çip ancak F5.2 fiyat hafızasını bağlayınca görünecek (bugün `priceHint` hep `None`). Alışveriş modundaki chevron da ayrı bağlam — reyonda sabit işaretlenmiyor. O yüzden şimdilik **uzun basma**: yeni piksel eklemiyor ve geri alınabilir. F5.2 gelince çip asıl açıcı olur.
  **Yeni bileşen: `NeydiSwitch`** (`Modifier.pressable` üzerine, önizlemeli). Material3 `Switch` de indication'ı sabit kodluyor; yeni bir Material3 kontrolü eklemek F1.5'te kaldırılan ripple'ı geri getirirdi. F6.7 Ayarlar da bunu kullanacak.
  **13 test** (8 yazma yolu + 5 bölüm), aralarında "mevcut geziye tekrar eklemiyor" (adet artmamalı), "kapanıştan sonraki yeni gezide geri geliyor" ve "alışveriş modunda bölüm yok".

- [x] **6.1 — `ProductStats` hesabı.** ✅ *Gerçek cihaz verisiyle doğrulandı: `product_stats` dört ürün taşıyor ve biri **yalnızca fişten** geldi.*
  **KARAR VERİLDİ (Açık kararlar #2): istatistik hem `trip_line` hem eşleşmiş `receipt_line` okuyor.** Yalnızca listeyi okumak, tam olarak kullanıcının **yazmayı unuttuğu** ürünleri saymamak demekti — yani Faz 4'ün var olma sebebini (*"fiş, listeye yazılmasa bile ekmeği içeriyor"*) es geçmek.
  **Cihazda kanıt:** *Sos* satırı `alım=1` ve kaynağı **fiş** — o ürün hiç listeye yazılmadı, File fişinde düzeltilen satırdan geldi (F4.7'nin alias'ı). Onayda bekleyen **5 fiş satırı sayılmadı**.
  **⚠️ Kararın bedeli tekilleştirme, ve o olmadan sessiz bir bozulma var:** aynı ürün hem listede işaretli hem fişte eşleşmiş olabilir ve bu **bir** alıştır. Tekilleştirilmezse `purchaseCount` ikiye katlanır ve `medianIntervalDays` **yarıya düşer** — yani uygulama her şeyi iki kat sık önermeye başlar. Sorgu bu yüzden `GROUP BY productId, tripId` ile tekilleştiriyor; aynı tehlike gezi kapanışının çift koşmasında da kayıtlı.
  **⚠️ Zaman damgası `trip.completedAt`, `trip_line.checkedAt` DEĞİL.** İyimser mutabakat (F4.8) kapanışta işaretlenmemiş **her** satıra kapanış damgasını yazıyor, yani tembel kullanımda — F4.8'in beklediği yaygın durum — bütün gezinin `checkedAt`'i aynı. O alan satın alma anı olarak kullanılamaz. Fişten gelen olaylarda `MIN()` ile **fişin bastığı tarih kazanıyor**: satın almanın gerçekleştiği an o, gezinin kapanması ise kullanıcının "bitir"e bastığı an.
  **Üç yerden tetikleniyor ve üçü de gerekli:** (1) gezi kapanışı, iyimser mutabakattan **sonra** — önce çağrılsaydı yeni kapanan gezinin alındı yazılan satırları kendi tetiklediği kurulumun dışında kalırdı; (2) fiş işlendikten sonra; (3) Fiş Kontrol'de kullanıcı bir satırı ürüne bağladıktan sonra. **(2) ve (3) atlanabilir görünüyor ama değil: fiş gezi kapandıktan SONRA çekiliyor**, yani yalnızca kapanışta hesaplasaydık fişten gelen alımlar daima bir gezi geride kalırdı.
  **Medyan için en az 3 alım (2 aralık) şart.** İki alım tek aralık verir ve tek örneğin medyanı o örneğin kendisidir: bir kez 40 gün unutmak *"normalde 40 günde bir alıyorsun"* olurdu. Yetmiyorsa alan **null** — *"uydurma yerine bilmiyorum"*. Bir test farkı gösteriyor: aralıklar 10/10/40 iken **medyan 10, ortalama 20**.
  **Aralıklar TAKVİM GÜNÜ** (`daysBetween`), çıkarma değil. `(b − a) / 86_400_000` 24 saatlik blok sayar; yerel saatle dün 22:00'deki bir alışveriş bugün 22:00'ye kadar "0 gün önce" okunur. Temposu ~10 gün olan bir uygulamada bir günlük kayma, önerinin **tetiklenmesi ile tetiklenmemesi** arasındaki fark. Aynı yardımcı F5.11'in gösterim biçimlendiricisiyle paylaşılacak ki ekran ile skor farklı konuşmasın.
  **Tam yeniden kurulum, tek transaction'da, asla incremental** (`@Transaction` DAO metodu — projede ilk kez). Türetilmiş bir önbelleği artımlı güncellemek, kaynak veri her değiştiğinde (satır silindi, fiş yeniden okundu, mutabakat geri alındı) sayacı hangi yönde düzelteceğini bilmek demekti. Bu ölçekte milisaniyeler sürüyor ve **her zaman doğru**. `muAdjust` korunuyor: o türetilmiş veri değil, kullanıcının/motorun düzeltmesi — sıfırlanması yanlış öğrenmeyi geri almanın tek yolunu bütün istatistiği silmek yapardı.
  **16 test**, aralarında "aynı ürün hem listede hem fişte = bir alım", "onaya düşmüş fiş satırı alım değil", "açık gezi sayılmaz", "medyan bir unutulmuş boşluğa direniyor" ve "yeniden kurulum `muAdjust`'ı koruyor".

- [x] **6.2 — Skor formülü.** ✅ Sıklık + gecikmişlik + geçen sefer unutuldu mu + `muAdjust` — **saf fonksiyon, bütün ağırlıklar tek dosyada**: "uygulama bunu neden önerdi" sorusunun cevabı okunarak bulunabilmeli.
  **Gecikmişlik gövde ve ORANA göre** (`daysSince / medyan`): 3 günde bir alınan ekmeğin 4. günü, 30 günde bir alınan deterjanın 20. gününden acil — test bunu iddia ediyor. **Sıklık eşitliği bozar, gövdeyi deviremez**: `ln(1+alım)/10`, 100 alımda bile katkı 0,46 — vakti gelmemiş çok-alınan ürün, vakti gelmiş az-alınanı geçemiyor (testli). **FORGOTTEN +0,5 / NOT_NEEDED −1,0**: "gerekmedi" tam bir tempo boyunca susturuyor ve bir tempo sonra kendiliğinden geri geliyor (testli) — F4.12'deki ayrımın bütün sebebi. **Tempo yoksa skor YOK**: uydurmak yerine null; soğuk başlangıç F6.3/F6.6'nın işi.
  **Üretici (`SuggestionEngine`) yalnızca veri veriyor** — şerit/çip/metin F6.3'ün. Üç kural: aktif listede olan önerilmez (kullanıcı zaten yazmış); eşik **0,85** — vakti gelmemiş ürün "en iyi 5" bile olsa listelenmez, boş şerit alakasız şeritten iyi; en fazla **5** (tasarımın sınırı). Eşik 1,0 değil bilerek: 10 günlük ürünü 9. günde önermek erken değil **hatırlatma**.
  **Gerekçe verisi skorla birlikte taşınıyor** (`daysSince`, `intervalDays`, `forgottenLastTrip`): F6.3'ün çipi "Yumurta · 14 gün oldu" yazacak ve *gerekçesiz çip reklam gibi okunur*. **11 test** — 6 saf formül + 5 gerçek Room.

- [x] **6.3 — Öneri şeridi: TEK ŞERİT, İKİ MOD.** ✅ Girdi **boşken** motorun önerileri, kullanıcı **yazarken** otomatik tamamlama — aynı `LazyRow`, modu girdinin boş olup olmaması seçiyor. İki ayrı şerit tasarımda üst üste binerdi; karar buydu ve uygulandı.
  **Çipler gerekçeli ve metinler maketlerden birebir:** `"14 gün oldu"`, `"geçen sefer unutmuştun"` — *gerekçesiz çip reklam gibi okunur*. "Unuttum" beyanı gün sayısını **eziyor**: kullanıcının kendi söylediği şey herhangi bir sayıdan güçlü gerekçe. Biçimlendirici saf ve testli (`reasonText`).
  **Öneri isabeti artık ölçülüyor:** çipten ekleme `isFromSuggestion = true` yazıyor — `TripLine.fromSuggestion` kolonu ve parametre en baştan vardı ama **hiçbir çağıran doldurmuyordu**, yani isabet ölçülemiyordu.
  **Şerit listeye bağlı canlı:** `rows` akışı her değişiminde motor yeniden hesaplıyor — çipten eklenen ürün **anında** şeritten düşüyor, çünkü motor aktif listedekini önermiyor.
  **Cihaz doğrulamasının dürüst sınırı:** tamamlama modu cihazda doğrulandı (*"yum" → Yumurta, Yumuşatıcı*); **öneri modu cihazda henüz çizilemez** çünkü cihazdaki bütün alımlar aynı gün yapıldı → medyan 0 → tempo yok → skor yok — ve bu **doğru davranış** (uydurma tempo yok). Öneri modu Room testleri + önizlemeyle kanıtlı; gerçek çipler günler arayla alışveriş birikince kendiliğinden görünecek.
  **Bilinçli erteleme:** `SuggestionEvent` yazımı (SHOWN/ADDED) **F6.5'te** — tablo orada DAO'suyla birlikte geliyor; üç-vuruş bastırması olmadan yalnız gösterim kaydı yarım bir iş olurdu.

- [~] **6.4 — Eksik Olabilir (Ekran 3).** *Ekran yazıldı ve cihazda doğrulandı; fiyat satırı F5.1'i bekliyor.*
  **✅ İKİ TEL HATASI DA DÜZELTİLDİ (15 Ağu).** `onGoShopping` artık Ekran 1'in altındaki *"Alışverişe çıkıyorum"* butonundan çağrılıyor; `onAdd` Ekran 4'e değil, seçilenleri ekleyip **alışveriş moduna** geçiyor. Aşağıdaki teşhis kayıt olarak duruyor.
  **⚠️ (çözüldü) EKRAN AÇILAMIYORDU ve sebebi F4.9'daki hatanın aynısı:** `ListScreen` `onGoShopping` parametresini alıyor, `App.kt` ona `go(MissingItems)` veriyor, ve **gövdede hiçbir yerde çağrılmıyor** — `ListContent`'in 17 parametresi arasında bile yok. Başlıkta *"Alışverişe çıkıyorum"* diye bir düğme **hiç yok**. Kotlin kullanılmayan fonksiyon parametresi için uyarı vermediği için sessiz. (`onHistory` da tam böyleydi; F4.9'da yakalandı.)
  **⚠️ İkinci tel hatası:** `onAdd = { go(FinishShopping()) }` — yani Ekran 3'ün birincil eylemi kullanıcıyı **Ekran 4'e** yolluyor. Tasarım *"[Ekle (4)]"* diyor: seçilenleri listeye ekle ve **alışveriş moduna gir**. Üstelik `FinishShopping(null)` `FinishShoppingViewModel`'de `flowOf(emptyList())`'e düşüyor, yani o ekran **kalıcı olarak boş** çiziliyor. Bir de iki spec çelişiyor: `Destinations` KDoc'u *"tripId null ise aktif alışveriş kapatılıyor"* diyor, `App.kt` yorumu *"gezi ZATEN KAPALI, bu ekran kapatmıyor"* diyor. **Karar:** `tripId` non-null olsun ve null dalı silinsin, ya da "aktif geziyi kapat" anlamı gerçekten yazılsın.
  **Üç bölüm ve asimetrik varsayılanlar** — asimetri bilinçli: *"geçen sefer unuttun"* ve *"her zamankiler"* varsayılan **açık**, tahmin bölümü varsayılan **kapalı**. Tahmin uygulamanın **kendi başına akıl yürüttüğü tek yer** ve varsayılan-açık muamelesi görmez.
  **Bölüm bölüm eksikler:**
  1. *Geçen sefer unuttun* — gezi-ötesi sorgu yok (F6.1) **ve sinyal kayıplı** (F6.2). Turuncu sol şerit için hiçbir bileşende şerit parametresi yok.
  2. *Her zamankiler* — `isStaple` hiç yazılmıyor (**F6.8**), yani bölüm daima boş.
  3. *Bitmiş olabilir* — `ProductStats` + skor formülü gerekiyor; ikisi de yok.
  4. **Satır başına düz Türkçe gerekçe** (13sp, solgun): *"son 12 alışverişin 11'inde aldın"*, *"12 gündür almadın, normalde 10 günde bir"*, *"genelde 4 alışverişte bir alıyorsun"*, *"geçen sefer unutmuştun"*. **Biçimlendirici yok:** `DateText.kt` yalnızca mutlak tarih veriyor (`formatDayMonthTime`/`formatDayMonthYear`); "N gün önce" üreten hiçbir yardımcı yok ve `PriceHint.Single.daysAgo` zaten hesaplanmış bir sayı bekliyor. Bu yardımcı F5.2 ile paylaşılacak.
  5. Sağa hizalı son ödenen fiyat — **F5.1'e bağlı**.
  6. **Boş durum = EKRAN HİÇ AÇILMAZ** + 2 saniyelik *"Liste hazır, eksik görünmüyor."* bildirimi. Tasarım bunu uygulamanın **en önemli boş-durum kararı** sayıyor. İki sonucu var: skor **çağıran tarafta** (yani `ListViewModel`'de) koşmak zorunda, hedefin içinde değil; ve bir bildirim yüzeyi gerekiyor — `ListScreen`'de `SnackbarHost` **yok**, çünkü F3.5'te işaretlemenin snackbar'ı olmaması bilinçli olarak kararlaştırılmıştı.
  7. **En fazla 8 satır**, başlıkta canlı sayı (`Eksik olabilir (4)`), altta `[Ekle (4)]` birincil + `[Boşver]` metin düğmesi. Bugün iki eşit ağırlıklı düğme var.
  8. Seçim onay kutusu — **bileşen yok**, ve `ListItemRow`'un `CheckTarget`'ini yeniden kullanmak **anlamsal olarak yanlış** olur: orada işaretli "alındı" demek.
  9. ViewModel ve Koin kaydı yok. Parametreli kayıt deseni için `FinishShoppingViewModel` örnek alınmalı.

- [ ] **6.5 — Sabit terfisi + bastırma.**
  **⚠️ Şemada bastırma/engelleme kavramı HİÇ YOK.** Engelleme tablosu yok, hiçbir entity'de "önermeyi bırak" kolonu yok.
  **Tek ilgili yapı:** `SuggestionEvent` + `SuggestionOutcome` (`GOSTERILDI/EKLENDI/REDDEDILDI/YOKSAYILDI`). KDoc'u gerekçeyi tam söylüyor: *"Bu tablo olmadan öneri motoru KENDİ İSABETİNİ ÖLÇEMEZ. Sürekli reddedilen bir öneriyi susturmanın tek yolu reddedildiğini kaydetmiş olmak."* `reason` kolonu kullanıcıya **gösterilen** gerekçeyi saklıyor — F6.3'ün ihtiyaç duyduğu denetim izi.
  **Eksikler:** (1) **DAO yok**, accessor yok, Koin binding yok — tablo boş ve erişilemez. (2) **Index yok**: üç-vuruş kuralı `productId` başına `COUNT(*) WHERE outcome IN (REDDEDILDI, YOKSAYILDI)` istiyor; bu, her gösterimle büyüyen append-only bir günlükte **tam tarama**. En az `(householdId, productId, outcome)` gerekiyor. (3) **Geri alınabilir engelleme kaydı yok**: tasarım kalıcı engelleme listesinin **Ayarlar'da görünür** ve her satırın **tek dokunuşla geri alınabilir** olmasını şart koşuyor (*"kara delik olmamalı"*). Append-only bir günlükte temiz bir "engeli kaldır" yazması yok — karşı-olay eklemek ve her okumayı onu katlamayı öğretmek gerekir. **Karar verildi ve tablo hazır:** ayrı `suggestion_block` tablosu (`productId`, `source`, `blockedAt`, `unblockedAt`, `UNIQUE(householdId, productId)`). Append-only olay günlüğünde temiz bir "engeli kaldır" yazması yoktu ve tasarım listenin görünür + tek dokunuşla geri alınabilir olmasını şart koşuyor. `SuggestionEvent`'e de tombstone eklendi.
  **⚠️ Faz 7'den ÖNCE çözülmesi gereken sözleşme çelişkisi:** `SuggestionEvent` `householdId` taşıyor — Conventions kuralı 2'ye göre senkron edilen kullanıcı verisi — ama `deletedAt` **taşımıyor** ve `ProductStats` gibi yazılı bir muafiyeti de yok. Üstelik `Daos.kt` başlığı *"`deletedAt IS NULL` HER SORGUDA"* diye katı bir kural koyuyor ve bu tabloda o süzgeç **yazılamaz**. Ya yerel-yalnız append-only günlük olduğunu (ProductStats gibi) **yazacak**, ya da tombstone alacak. Sonradan kolon eklemek bir şema bump'ı daha demek.
  **Giriş noktaları yok:** tasarım *"Bunu önerme"* anahtarını **Ekran 5**'e koyuyor (F5.3, henüz yok) ve oraya giden tek kanca `onPriceClick` — o da bağlanmamış.
  **Üç-vuruş sessiz otomatik bastırma** hiç "önerme" demeyen kullanıcı için kendi kendini iyileştirir; bu, listenin görünür olmasının **yerine** geçmez, ikisi birlikte gerekir.

- [~] **6.6 — Kurulum (Ekran 8).** *Kod tamam, cihaz doğrulaması bekliyor.* **İKİ adım** (tasarım karari 6), ~40 ürünlük grid, tempo çipi. Var olma sebebi tek: 15. gezide değil **3. gezide** akıllı hissetmek.
  **İki karar da tasarımdan geldi ve uygulandı (karar 6):**
  1. **Hane adımı Faz 7'de.** Var olmayan bir auth için e-posta alanı çizmek tutulamayacak bir söz vermek olurdu. Kurulum şimdilik "1 / 2" ve "2 / 2"; auth geldiği gün yeniden üç adım olacak ve ilerleme çubuğu segment sayısını adımdan aldığı için kendiliğinden düzelecek.
  2. **Tetikleyici: `setupCompletedAt` boş VE hane hiç ürün görmemiş.** İkinci koşul olmadan mevcut kurulumlarda dolu bir veritabanının üstüne onboarding açılıyordu. `ProductDao.count` bunu tek sorguyla kesiyor.
  **`AppSettingsDao` yazıldı** — `app_settings` v3'te şemaya girmişti ama hiçbir okuyanı yoktu (`NeydiDatabase` KDoc'u bunu zaten söylüyordu: *"DAO'lar bu bump'a dahil değil"*). İlk okuyanı bu adım.
  **Hedefe artık gidiliyor:** `App.kt` bootstrap bittikten sonra koşulu soruyor ve Kurulum'u **Liste'nin üstüne** bindiriyor — kök olarak değil. Böylece "Listeme geç" ve "Atla" aynı yere çıkıyor ve eski `onFinish = back()` tuzağı (kök olsaydı sessiz no-op) doğmuyor.
  **Sınır kararı:** seçim `STAPLE_LIMIT` (12) ile sınırlı — Ayarlar'da kullanıcının yeniden göreceği sayının aynısı. Sınıra gelince seçilmemiş çipler sönüyor, **seçilmişler dokunulabilir kalıyor** (yoksa kullanıcı kendi seçtiği on iki ürünle hapsolurdu).
  **Üçüncü boş durum hâlâ açık:** `EmptyKind` iki girdi taşıyor, üçüncüsü *"kurulum atlandı"*. Kurulum artık `setupCompletedAt` yazdığı için ayrım **mümkün**; boş durumun kendisi ayrı bir adım.

- [x] **6.7 — Ayarlar (Ekran 7).** ✅ *Cihazda doğrulandı.* *"Sıfır tasarım yatırımı, düz liste"* — ve beş bölümün üçünün veri kaynağı yoktu.
  **Tasarımın kendi kuralı sorunu çözdü:** *"Boş bir bölüm başlığı, olmayan bir işi varmış gibi gösterir."* Veri olmayan bölüm **hiç çizilmiyor**, sahte veriyle ya da "yakında" yazısıyla doldurulmuyor. Çizilenler: Hane (Ad, Üyeler), Her zamankiler (N/12 + liste + kaldırma), Gizlilik.
  **Çizilmeyenler ve sebepleri:** `suggestion_block` tablosu şemada var ama **hiçbir yazan yok** (F6.5 gelince dolacak); `store` tablosu var, hiçbir yazan yok — fiş `storeNameRaw` yazıyor ama Store satırı üretmiyor; `Household.joinCode` alanı var ama null, kod üretimi Faz 7'nin işi.
  **"Verilerimi sil" bilerek yazılmadı** — bkz. Faz 11 açık soruları.
  Ekran **erişilebilir** (F4.9'da yatay kaydırma eklenene kadar "Ayarlar" düğmesi ekranın sağında kesiliyordu), ama içerik iskelet.
  1. **Hane** — `HouseholdDao.observeActive/upsert` ve `MemberDao.observeAll` var. **6 karakterlik katılma kodu: kolon yok, üretici yok** (F7.2'ye ait ama satır burada görünecek).
  2. **Her zamankiler (9/12, sıralanabilir, silinebilir)** — `observeStaples` sorgusu var ama **beslenmiyor ve çağıranı yok** (F6.8). *"Sıralanabilir"* için `Product`'ta `sortOrder` kolonu **yok**. *"Silinebilir"* = `isStaple = false` yazmak, yani yine F6.8.
  3. **Önerilmeyenler** — **hiçbir şey yok** (F6.5). Tasarım bu listeyi görünür kılmanın, kalıcı reddin kara delik gibi hissedilmesini engelleyen şey olduğunu söylüyor.
  4. **Mağazalar** — `Store` entity var, DAO/accessor/binding **yok**, satır **yok** (F5.9).
  5. **Gizlilik (KVKK — düz dil + "Verilerimi sil")** — `HouseholdDao.softDelete` var ama **cascade veya silme rutini yok**; Conventions kuralı 3 gereği haneyi tombstone'lamak alt satırların **hiçbirini** silmez. Ayrıca fiş JPEG'leri diskte ayrı duruyor ve onları da silmek gerekir — *"Verilerimi sil"* bunları kapsamazsa yazdığı şeyi yapmıyor demektir. (İlgili: manifest'teki `allowBackup` varsayılanı bu oturumda kapatıldı, çünkü fiş fotoğraflarını ve veritabanını sessizce Google Drive'a yedekliyordu.)

- [x] **6.9 — Kategori tonlarını bağla** ✅ *(veri zaten geliyor; hiçbir adım sahiplenmiyordu)*. `Category.tintArgb` **12 gerçek ton ile tohumlanıyor** ve **hiçbir yerde okunmuyor** — `CategoryTile`'ın `tint`'i her çağrı yerinde varsayılanda kalıyor, yani **12 kategori kutucuğunun hepsi aynı gri** çiziliyor. Tasarımın *"56dp sıcak tonlu squircle"*'ı uygulanmamış, halbuki palet **veritabanında yayında**. `TODO(kategori-tonlari)` *"ton paleti tasarımdan gelecek"* diyor — **çoktan geldi**.
  **Çözüm: UI'da türetildi** (`CategoryTint`), maddenin kendi öngördüğü yol. Ham ton dolgu olamıyordu — orta-koyu ve doygun, üstelik iki tema için tek küme. Ton zeminle karıştırılıyor (ışıkta %22, karanlıkta %40), böylece hue korunuyor ama üzerine temanın normal metin rengi oturuyor. **Kontrast iddia değil ölçüm:** `CategoryTintTest` 12 tonun her birini iki temada da metne ve zemine karşı ölçüyor (4 test). Katalog yeniden tohumlaması (F2.7) gerekmedi.
  **İçinde iki gerçek karar saklı, tıkanmasının sebebi de bu:** (1) Bunlar orta-koyu doygun dolgular ve `contentColor` varsayılanı **koyu** — koyu zemin üstünde koyu harf. Tonu bağlamak **açık bir içerik rengini de** bağlamak demek, yani iki çağrı yerinde ikinci bir parametre. (2) **İki tema için tek ton kümesi var**: krem `#FBF7F2` üstünde sıcak aksan gibi okunuyorlar, koyu `#13100E` üstünde bazıları zeminden **neredeyse ayrışmıyor**. Referans tablosu ancak katalog yeniden tohumlamasıyla değişebildiği (F2.7) için doğru cevap büyük olasılıkla **UI'da türetmek** — ve bu yazılı olmalı.
  **Test ucuz:** F1.2'nin kontrast koşumu sRGB bağıl parlaklığı zaten hesaplıyor; iki-harf baş harfler kutucukların ~%80'inin **birincil içeriği** ve bugün **hiçbir test** onları kategori tonuna karşı ölçmüyor.

## Faz 7 — Senkron

> **Fazın hiç başlamamış olduğunun kanıtı şemada duruyor:** outbox altyapısı (`PendingOp` — `entityTable`, `entityId`, `opType`, `payloadJson`, `attempts`, `lastError`) ve `SyncMeta` **entity olarak kayıtlı ve tablo cihazda var**, ama **DAO'ları yok, accessor'ları yok, ve hiçbir yazma kuyruk kaydı üretmiyor** — bugün her yazma doğrudan Room'a gidiyor.

- [ ] **7.1 — Supabase projesi + şema + RLS** *(cihazsız)*. `householdId` üzerinden satır düzeyi güvenlik. **Ön koşul zaten hazır:** her kullanıcı tablosu `householdId` taşıyor (Conventions kuralı 2); bilinçli istisnalar `Category` ve `CatalogSeed` — referans verisi, kimseye ait değil. Supabase bağımlılıkları katalogda pinli (3.7.0) ama **hiçbiri build script'te değil**; pinler 13 Ağu 2026 tarihli, Faz 7 başında yeniden doğrulanmalı.
  **Şemayı taşımadan önce çözülmesi gereken iki tutarsızlık:** (a) `SuggestionEvent` `householdId` taşıyor ama `deletedAt` taşımıyor ve muafiyeti yazılı değil (bkz. F6.5); (b) `ProductStats` bilinçli olarak senkron edilmiyor ve tombstone **taşımıyor** — bu **doğru** ve Supabase şemasına hiç girmemeli, ama yazılı olmalı ki karşı taraf onu unutulmuş sanmasın.

- [ ] **7.2 — Auth.** E-posta OTP (6 haneli kod) — magic link **değil**: universal link / App Links yapılandırmasından ve Apple Guideline 4.8'den kaçınır. + 6 karakterlik hane katılma kodu.
  **Şema eksiği:** `Member`'da e-posta kolonu **yok**, `Household`'da katılma kodu kolonu **yok**. İkisi de şema bump'ı gerektiriyor ve F6.7'nin Hane bölümü bunlara bakıyor.

- [ ] **7.3 — v1 senkron: Realtime `postgres_changes` + yerel cache, outbox YOK.** ~50 satır. Çevrimdışı düzenleme kaybı **bilinçli olarak** kabul edilir. İki kişilik, sinyalli bir markette kullanılan liste için yeterli.
  **Bu kararın kod tarafındaki karşılığı zaten kurulu:** `PendingOp` tablosu var ama kullanılmıyor — yani "outbox yok" bir eksiklik değil, **uygulanmış bir karar**. F7.5 onu yalnızca gerçek bir kayıp gözlenirse açacak.
  **⚠️ Bir yazma yolu bu karara aykırı davranabilir:** `closeIfOpen` karşılaştır-ve-yaz ile "tek cihaz kapatır" kuralını **veritabanı düzeyinde** zorluyor ve 0/1 dönüyor. Realtime ile iki cihaz aynı geziyi görürken bu garanti korunmalı; yoksa mutabakat iki kez koşar ve `medianIntervalDays` yarıya düşer.

- [ ] **7.4 — `updated_at`** *("(cihazsız)" işareti KALDIRILDI — bu adım 11 tablolu bir istemci migration'ı içeriyor)*. Postgres UPDATE trigger'ı ile, **asla istemciden**. Tek bir istemci timestamp'i LWW'yi kalıcı ve izsiz biçimde bozar (cihaz saatleri kayar; kaybeden yazma hiçbir iz bırakmaz).
  **⚠️ Ama LWW'nin karşılaştıracağı YEREL kolon yok.** `updatedAt` bütün kod tabanında **tek bir yerde** var — `ProductStats`'ta, yani **senkron edilmeyen tek tabloda**. Gerçekten senkron edilen 11 tablonun hepsinde yalnızca `createdAt` ve `deletedAt` var. `Conventions.kt`'nin beş numaralı kuralı arasında `updatedAt` **hiç geçmiyor** — yani eksiklik bir entity'de gözden kaçmış değil, **sözleşmede yok**.
  **Sonuçları:** bu adım server-only değil; 11 tablolu bir migration + Conventions'a yeni bir kural + her DAO UPDATE'inin `updatedAt` yazması demek (bugün `setChecked`, `setStatus`, `setTotal`, `closeIfOpen`, `confirmMatch`, `setAmount` hiçbiri zaman damgasına dokunmuyor). Kolon **`Long?`** olmak zorunda (ifade varsayılanı ve geri-doldurma yok) ve her okuyucu null'ı `createdAt` saymalı — `defaultValue = "0"` migration'dan önceki her satırın **her ilk çatışmayı kaybetmesi** demek. **Ve v3 bump'ına girmeli, Faz 7'nin içine değil**: Faz 7 canlıya geçtikten sonra iki telefon ayrışmış veri tutuyor ve null-updatedAt penceresi teorik değil **gerçek bir çatışma penceresi** olur.

- [ ] **7.5 — Outbox + tombstone + add-beats-remove.** **Yalnızca gerçekten bir düzenleme kaybı gözlemlenirse.** Erken yapmak 300–500 satırlık tahmini ikiye katlayan fotoğraf-yükleme kuplajını getirir. Tombstone tarafı hazır: gerçek silme yok, `deletedAt` var, ve `findIncludingDeleted` ile "mezardan çıkarma" deseni F2.6'da kuruldu.

- [ ] **7.6 — Keep-alive** *(cihazsız)*. `pg_cron` veya Cloudflare Worker cron. **GitHub Actions ile değil**: 60 gün sessiz repoda zamanlanmış workflow devre dışı kalır → önce keep-alive sessizce ölür, sonra veritabanı duraklar. **İki sessiz hata üst üste** ve ikisi de fark edilmeyecek türden. Supabase ücretsiz katmanı 7 gün hareketsizlikte duraklatıyor — bu, iki kişilik bir uygulamanın **tatilde** yaşayacağı şey.

## Faz 8 — Marka varlıkları

- [ ] **8.1 — Logo üretimi.** Recraft ile, `docs/02-logo-splash-prompt.md`'deki 4 konsept. **Tek renkli siluet önce**, renk sonra — Android monochrome ve iOS tinted zaten siluet istiyor, yani renkle başlamak iki kez çalışmak demek.

- [ ] **8.2 — 66dp monochrome testi.** Basitleştirme merdiveni (66dp ve 24dp varyantları). Burada okunmuyorsa konsept ölü, Figma'da ne kadar iyi durduğunun önemi yok.

- [ ] **8.3 — Android app icon.** Adaptive icon (108/72/**66dp garanti daire**) + **monochrome katmanı** (Android 13+ temalı ikonlar için zorunlu) + legacy mipmap'ler + 512×512 Play PNG. **Bugünkü hal:** manifest `@android:drawable/sym_def_app_icon` — framework yer tutucusu.

- [ ] **8.4 — Android splash.** `values-v31/themes.xml`: tek düz opak renk, 288dp canvas / 192dp görünür daire, metin yok, branding image yok.
  **⚠️ SPEC'İN KENDİSİ DÜZELTİLMELİ — yazıldığı gibi uygulanırsa önlemeye çalıştığı flaşı karanlık modda ÜRETİR.** Bugün `androidApp/src/main/res/` **tek konfigürasyonlu**: yalnızca `values/themes.xml` var ve `windowBackground` sabit `#FBF7F2` — o **açık** mod yüzeyi. Karanlık paletin yüzeyi `#13100E`. `values-night/` olmadığı için karanlık modda soğuk açılış **krem** boyayıp ilk Compose karesinde **neredeyse siyaha** kesiyor. TODO metni ve bu adım *"#FBF7F2, ilk Compose karesiyle birebir aynı olmalı"* diyor ve bu **yalnızca açık modda** doğru. Sadık bir uygulama karanlık modda flaş bırakır ve **adım yeşil görünür**, çünkü açık mod kontrolü geçer.
  **Doğrusu iki konfigürasyon:** `values/` → `#FBF7F2`, `values-night/` → `#13100E`, ve splash öznitelikleri API 31+ olduğu için `values-v31/` + `values-night-v31/`. Test cihazı API 31, yani **iki yol da onda doğrulanabilir**; F1.6 zaten bu cihazda karanlık mod geçişlerinin hataya en açık durum olduğunu gösterdi. → `themes.xml` TODO(splash) kapanır.

- [ ] **8.5 — iOS ikon varlıkları.** 1024 PNG, **alpha kanalı yok** (otomatik App Store reddi), light/dark/tinted varyantlar. Xcode entegrasyonu Faz 9'da.

- [ ] **8.6 — Yayın yapılandırması** *(yeni; hiçbir adım sahiplenmiyordu)*. Bugün `release { isMinifyEnabled = false }` — **R8 yok, proguard kuralı yok, signingConfig yok, shrinkResources yok**, `versionCode = 1`. Geliştirmeyi engellemiyor ama **F9.5 (TestFlight) ve herhangi bir Play yüklemesi bunların hepsini istiyor** ve 11 fazın hiçbirinde bir kutusu yoktu. Bir derlemenin yüklenmesi gerektiği gün keşfedilecek şey olmasın diye buraya yazıldı. Keystore **repoya girmez**.

## Faz 9 — iOS (Mac gerektirir)

> Windows'ta yapılamaz. Kod baştan doğru yazılıyor; bu faz yalnızca derleme ve doğrulama bekliyor. **Kapı 2 burada iPhone'a dönüşür.**
>
> **İyi haber, bu oturumda kazanıldı:** `:composeApp:compileKotlinIosSimulatorArm64` **artık yeşil.** HEAD'de kırıktı (F4.10) ve o aynı zamanda ortak test paketini de bloklıyordu.
>
> **⚠️ Araştırmanın "TEK native dikiş bekle (kamera)" tahmini yanlış çıktı: bugün `expect/actual` dikişi **sekiz** ve kamera onlardan biri DEĞİL** (FileKit ortak API veriyor). Mevcut dikişler: `platformModule`, `ioDispatcher`, `ApplySystemBarAppearance`, `KeepScreenOn`, `ReceiptReader`, `downscaleForOcr`, `writeBytesTo`/`deleteFileAt`, ve veritabanı yolu. Faz 9'un bütçesi buna göre okunmalı.

- [ ] **9.1 — iOS kabuğunu SIFIRDAN kur.** *(Adı "ilk derleme"ydi ve bu ciddi biçimde eksik anlatıyordu.)* **Doğrulandı: `iosApp` dizini YOK, Xcode projesi YOK** — ne `.xcodeproj`, ne `Info.plist`, ne Swift giriş noktası, ne asset katalog. (`.gitignore` var olmayan bir dizin için satırlar taşıyor.) `composeApp` framework'ü üretiyor ve **Kotlin tarafı derleniyor**; eksik olan **kabuğun tamamı**: Xcode projesi, bundle id, deployment target, framework entegrasyon seçimi (doğrudan embed / SwiftPM / CocoaPods — bu seçim F9.5'in TestFlight derlemelerinin nasıl üretildiğini belirliyor), `MainViewController`'ı çağıran host, ve F8.5'in 1024 PNG'sinin gireceği asset katalog yuvaları.
  **⚠️ Android deneyiminden sonra şaşırtıcı olan izin maddesi:** F4.2'nin manşet bulgusu *"CAMERA izni gerekmiyor, cihazda doğrulandı"* — sistem kamera intent'i kullanıldığı için. iOS'ta aynı yol **`NSCameraUsageDescription`** istiyor, FileKit'in galeri yolu da **`NSPhotoLibraryUsageDescription`**. Onlar olmadan uygulama **sormaz, SONLANIR** — yani uygulamanın en önemli ikinci akışında sert çökme, ve **yalnızca Mac'te** keşfedilebilir. F9.2 SwiftUI yüzeyiyle ilgili; **plist bundan önce gelir**.
  Ayrıca launch screen storyboard yok ve F8.4'ün splash işi **yalnızca Android** için yazılmış (`values-v31/themes.xml`) — iOS'un splash adımı hiç yok.
  Derleme zaten geçiyor; kalan iş **kabuğu kurmak ve çalıştırmak**. Mac'e geçişte `kotlin.native.ignoreDisabledTargets=true` bayrağı da silinmeli — yalnızca Windows'ta iosX64Test uyarılarını susturuyor.

- [ ] **9.2 — Kamera native dikişi.** Alan raporları tutarlı biçimde burada SwiftUI'a inildiğini söylüyordu; **FileKit bunu çürüttü** — kamera ortak API'den çalışıyor. Bu adım artık kamera değil, **iOS fiş hattı**: bugün `downscaleForOcr`, `writeBytesTo`, `deleteFileAt` hepsi `false` dönüyor ve `IosReceiptReader` açık bir `NotImplementedError` veriyor.
  **⚠️ Bu arada iOS'ta gerçekleşen sessiz zarar:** `SummaryCard.onTakeReceipt` *"null ise fiş butonu HİÇ çizilmez"* diye belgelenmiş, **ama tek çağrı yeri her zaman non-null veriyor ve ağaçta hiçbir platform kontrolü yok**. Yani iOS'ta *"Fiş çek"* çizilir ve tıklanır; ardından küçültme başarısız olur, ham baytları yazma da sessizce başarısız olur (dönüş değeri yoksayılıyor), silme no-op olur, ve `enqueueReceipt` **dosyası olmayan bir yola** işaret eden satır yazar. Sonuç: diskte yetim bir kamera dosyası + Geçmiş'te kalıcı `okunamadı` fişi, ve *"iOS'ta henüz yok"* diyen hiçbir mesaj yok — okuyucunun açık hata döndürme niyetinin tam tersi. **Ya butonu platformda gizle, ya da hatayı yüzeye çıkar.**

- [ ] **9.3 — Status bar + safe area.** `preferredStatusBarStyle` common koddan **set edilemez**, barındıran view controller'da ayarlanır — F1.6 bu yüzden baştan platform-ayrık kurgulandı ve iOS `actual`'ı **kasıtlı boş**.   **⚠️ `MainViewController.kt`'deki TODO'nun ikinci maddesi ARTIK GEÇERSİZ:** Nav3 saveable back stack işini F1.4 yaptı ve TODO'nun gerekçesi ("iOS'ta sessizce tezahür eder") o adımda **çürütüldü** — hata her platformda gürültülü. TODO metni güncellenmeli, yoksa gelecekte yapılmış bir işi tekrar yaptırır. Ayrıca `SystemBars.ios.kt`'deki `TODO(ios-statusbar)` bu adıma ait ve TODO tablosunda hiç görünmüyor. → `MainViewController.kt` TODO(ios) kapanır.

- [ ] **9.4 — Gerçek iPhone doğrulaması.** `tnum` gerçekten uygulanıyor mu (Skia desteklemediği OpenType özelliklerini **sessizce** yok sayabiliyor — yani yanlış hizalanmış bir fiyat kolonu hiçbir hata vermeden çıkar), variable font eksenleri varsayılana düşüyor mu. **F1.1 bu riski baştan azalttı:** Plus Jakarta Sans da variable değil **statik** bundle edildi, çünkü `FontVariation.Settings` iOS'ta güvenilir değil ve Mac olmadan doğrulanamıyordu. → `Type.kt` TODO(tnum) kapanır.

- [ ] **9.5 — TestFlight internal.** ~85 günde bir yeniden yükleme gerekiyor — **takvime al**. Araştırmanın *"projenin durma noktası"* dediği yer: 4. ayda ilk TestFlight süresi dolarken grafikler hâlâ boşsa uygulama yeniden yüklemeye değmez hale gelir. **Bu riski azaltan hiçbir adım yok** — bkz. **Riskler**. Araştırma dağıtım planında *"Play internal testing"*'i de sayıyor ve hiçbir fazda karşılığı yoktu; F8.6 ile birlikte düşünülmeli.

## Faz 10 — Sürekli / refactor

> **Sıra numara sırasında değil ve bu kasıtlı:** 10.5 (sheet yüksekliği) 10.2'den (Nav3 Scene) önce duruyor, çünkü 10.5'in kendi metni *"ikisi aynı işte buluşuyor"* diyor — yükseklik bütçesini çözecek kapsayıcı Scene geçişiyle birlikte yazılacak. Yani **10.5, 10.2'yi kapılıyor**, tersi değil.

- [x] **10.1 — AGP 9'a geçiş.** ✅ *("cihazsız" işareti kaldırıldı — fazlasıyla cihaz gerektirdi.)* AGP **9.3.1**, compileSdk/targetSdk **37**, lifecycle **2.11.0**. **Engel hâlâ duruyor:** `com.android.application` + `org.jetbrains.kotlin.multiplatform` aynı modülde 9.3.1'de de reddediliyor, birebir aynı hatayla. Bypass flag'leri (`android.builtInKotlin=false` + `android.newDsl=false`) çalışıyor ve ölçüldü — ama AGP 10.0'da kaldırılıyorlar ve `newDsl=false` AGP 9'un yeni DSL'ini kapatıyor, yani sürüm 9 DSL 8 olurdu. **Modül ikiye ayrıldı:** `:composeApp` artık `com.android.kotlin.multiplatform.library` ile kütüphane, APK'yı ince `:androidApp` üretiyor (yalnızca `MainActivity` + manifest + tema). JetBrains Mayıs 2026'dan beri KMP sihirbazında zaten bunu üretiyor.

  **Dört tuzak — üçü sessiz:**
  1. **`:androidApp` Compose derleyici eklentisini uygulamak ZORUNDA.** AGP 9'un `builtInKotlin`'i Kotlin'i derliyor ama Compose eklentisini getirmiyor. Onsuz `setContent { App() }` içindeki `@Composable` lambda düz `Function0` olarak derleniyor, kütüphane `Function2` bekliyor. **Derleme sessizce geçiyor**, uygulama açılışta `NoSuchMethodError` ile çöküyor. Dex'i açıp imzaları karşılaştırarak bulundu (`dexdump`).
  2. **`androidResources { enable = true }` zorunlu** — kütüphane modülünde varsayılan kapalı, kapalıyken Compose Resources (bu projede fontlar) çalışma zamanında patlıyor.
  3. **Tüm eklentiler kökte `apply false` ile bildirilmeli.** İki modül aynı AGP'yi sürümle isterse Gradle `already on the classpath with an unknown version` diyip reddediyor.
  4. **Test görevi yeniden adlandı:** `testDebugUnitTest` → `testAndroidHostTest`. Eski adla çağırınca görev bulunamıyor; daha kötüsü, yanlış ada rağmen yeşil görünen bir koşu sıfır test çalıştırabiliyor.

  **Bonus bulgu:** bölünme gizli bir sürüm kaymasını açığa çıkardı — `activity-compose` derlemede 1.12.0, çalışma zamanında 1.12.4'tü. Tek modüldeyken ikisi aynı classpath olduğu için görünmüyordu. 1.12.4'e hizalandı. (Çökmenin sebebi bu değildi — sebep 1. maddeydi — ama gerçek bir kaymaydı.)

  **Ölçüldü:** 8 birim test geçiyor, 79 iOS task'ı hâlâ tanımlı, Room şeması ve KSP üretimi sağlam, cihazda galeri + Room sondası + süreç ölümünden back stack geri dönüşü çalışıyor, çökme yok.
- [ ] **10.5 — Sheet yüksekliği: sihirli sayıyı kaldır** *(cihazda ölçülmeli)*. `AddSheet.GRID_RATIO = 0.24f` **ayarlanmış bir sabit, çözüm değil.** Bu telefonda ölçüldü (`uiautomator`, buton `y 1993→2047`); başka bir ekran oranında, katlanabilir cihazda, yazı tipi ölçeği büyütülmüş bir kullanıcıda ya da 12'den fazla reyon olduğunda **yeniden taşabilir** — ve taştığında **sessizce** taşar, çünkü sheet kırpıyor, hata vermiyor.
  **Kök sebep:** kısmi açık `ModalBottomSheet` içeriği **sınırsız** yükseklikle ölçüyor, o yüzden `weight` çalışmıyor; taşan içeriği de kaydırmıyor, **kırpıyor**.
  **Çözüm adayları:** (a) F10.2'deki Nav3 custom Scene'e geçerken yüksekliği kendimiz sınırlayan bir kapsayıcı yazmak — ikisi aynı işte buluşuyor; (b) grid'i kendi `verticalScroll`'una alıp butonu `Box` içinde alta sabitlemek; (c) `SubcomposeLayout` ile önce butonu ölçüp kalanı grid'e vermek.
  **Regresyon nöbetçisi gerekiyor:** hangi çözüm seçilirse seçilsin, butonun sıfır olmayan sınırlara sahip olduğunu doğrulayan bir kontrol olmalı — `bounds="[0,0][0,0]"` derlemede görünmüyor.
  **Aynı sınıftan kalan iki aday, ikisi de bütçesiz:** (1) **Özet kartı** — en dolu halinde altı çocuk (başlık, 36sp tutar, iki satıra sarabilen gövde, "Fiş çek", "Hepsini almadım", "Tamam") + 32dp üst/alt dolgu + çubuk inset'i, ve **hiç yükseklik tavanı yok**. Sıralama sonucu keskinleştiriyor: "Tamam" bilerek **en sonda** ve silik, yani taşma olursa kırpılan şeyler **F4.8'in zorunlu geri alma yolu** ile tek kapatma kontrolü oluyor. Önizlemeleri `onTakeReceipt`/`onFixTaken` vermediği için **en uzun hali hiç çizilmedi**. (2) **Ekle sheet'inin başlık satırı** — `SpaceBetween` ile iki `Text`, ikisinde de `weight`/`maxLines`/`Ellipsis` yok; uzun bir reyon adı (*"Süt-Kahvaltılık"*, *"Konserve-Salça"*) + büyük yazı ölçeği ile *"← Reyonlar"* kesilir — kategori grid'ine dönmenin sheet'i kapatmadan tek yolu o.
  *(Fiş Kontrol'ün düzeltme sheet'i aynı sınıftaydı ve bu oturumda düzeltildi: yükseklik tavanı + kaydırma + `imePadding` eklendi. En kötü hali ~520dp'ydi ve en altta duran "Kaydet" — F4.7 alias öğrenmesini yazan tek düğme — ilk kırpılacak şeydi.)*

- [ ] **10.2 — Bottom sheet'leri Nav3 Scene'e taşı.** Şu an ekran state'i; Nav3'ün custom Scene API'si doğru yer.
  **Üç sheet, üç ayrı el yapımı host, paylaşılan sözleşme yok:** Ekle sheet'i (`vm.sheetOpen`), özet kartı (`vm.summary`), düzeltme sheet'i (`vm.editing`). **Hiçbiri süreç ölümünden sağ çıkmıyor** — sheet görünürlüğü `SavedStateHandle` desteklemeyen ViewModel akışlarında yaşıyor, yani süreç ölümünde açık bir sheet kapalı olarak geri geliyor. Scene geçişi state geri yüklemesini, yükseklik bütçesini (F10.5) ve kapsayıcı rengi tutarlılığını **tek geçişte** çözüyor.

- [ ] **10.6 — Material3 tıklanabilir bileşen sözleşmesini iki yeni ekranda geri getir** *(bu oturumda tespit edildi)*. Çalışma sözleşmesi *"Tıklanabilir Material3 `Surface`/`Button`/`Card` kullanılmaz"* diyor ve gerekçesi ölçülmüş: material3'ün `Surface`'ı indication'ı **sabit kodluyor** (üç ayrı yerde), yani temadaki `LocalIndication` override'ı onlara **hiç ulaşmıyor**.
  **F4.6 ve F4.9'da yazılan iki ekran bu kurala uymuyor:** `HistoryScreen`'de `TextButton` ve satır başına `Surface(onClick)`; `ReceiptCheckScreen`'de `Button`, `OutlinedButton`, `Surface(onClick)` ve **iki `OutlinedTextField`** — sonuncusu ayrıca `QuickAdd`'in *"M3 TextField değil `BasicTextField`"* öncülüne de aykırı. Sonuç: Geçmiş ve Fiş Kontrol'de kullanıcı Android ripple'ı görüyor, uygulamanın geri kalanında %6 tonal karartma + 0,97 ölçek görüyor.
  **Düğmeler mekanik (`NeydiButton` / `Modifier.pressable`), metin alanları değil:** sayısal klavye + etiket + kenarlık taşıyan bir `NeydiTextField` bileşeni gerekiyor, yani bu iş **F3.1 bileşen kütüphanesine bir ekleme**. O yüzden ayrı madde.

- [ ] **10.7 — Odak halkasını bağla** *(erişilebilirlik)*. `Modifier.focusRing` yazılmış ve **hiçbir yerde uygulanmamış** — repo genelinde tek referans kendi tanımı. Kendi KDoc'u neden var olduğunu söylüyor: *"Ripple olmadığı için klavye ve erişilebilirlik gezinmesinde görünür tek işaret bu."* Ripple **global olarak** kaldırıldığı ve `NeydiIndication` yalnızca `PressInteraction`'a tepki verdiği için bugün **hiçbir kontrolde** klavye/D-pad/switch-access odağı görsel değişiklik üretmiyor — her düğme, her çip, her kategori kutucuğu, her satır dahil.
  **Doğru yer tek nokta:** `Modifier.pressable` zaten `MutableInteractionSource`'u sahipleniyor; `collectIsFocusedAsState()` ile halkayı orada bağlamak sözleşmeyi her çağrı yerinde değil **bir yerde** onurlandırır. Aynı geçişte `Role.Button` semantiği ve `onClickLabel` de eklenmeli — `pressable` ikisini de geçirmiyor.

- [ ] **10.8 — 44dp dokunma hedefi tabanının altındaki üç kontrol** *(cihazsız ölçüm, cihazda doğrulama)*. `Sizes.minTapTarget = 44.dp` projenin kendi tabanı. Üç ihlal: (1) *"← Reyonlar"* ≈ **28dp** — kategori grid'ine dönmenin tek yolu; (2) *"Hepsini almadım"* ≈ **28dp** — F4.8'in zorunlu geri alma girişi; (3) token düzeyinde çelişki: `SizesExtra.suggestionChip = 40.dp` tabanın **4dp altında** ve `SuggestionChip` uygulamanın en çok dokunulan kontrolü (boş durum, Ekle sheet'i, otomatik tamamlama şeridi). Ya token yükseltilecek ya da istisna **yazılı** olacak.

- [ ] **10.9 — Satır silme yolu yok** *(kullanıcıya dokunan boşluk)*. `ListViewModel.remove` ve `ListRepository.remove` (yumuşak silme) **hazır ve hiçbir composable onları çağırmıyor** — kaydırma, uzun basma, çöp ikonu, bağlam menüsü: hiçbiri yok. Yani yanlış yazılmış ya da yanlışlıkla yapıştırılmış bir satır **yalnızca işaretlenerek** listeden çıkabiliyor, ve işaretlemek onu **alındı** kaydeder, yani öneri motoruna bir satın alma olarak girer. Pano ile toplu yapıştırma (F3.4) tek dokunuşla N satır ekleyebildiği ve **önizleme/onay adımı olmadığı** için bu, o özelliğin en keskin kenarı. Veri katmanı ve ViewModel hazır; eksik olan **yalnızca hareket**.

- [ ] **10.10 — Pano okumasını güncel API'ye taşı** *(F3.4'ün kapanması buna bağlı)*. `LocalClipboardManager` ve `ClipboardManager` **kullanımdan kalktı** (CMP 1.11.1 kaynağından doğrulandı); yerine gelen `Clipboard.getClipEntry()` **suspend**. Çağrı yeri zaten `LaunchedEffect` içinde olduğu için geçiş mekanik.
  **İki ek kusur, ROADMAP'te kayıtlı değildi:** (a) `clipboardText` `rememberSaveable` değil `remember` ile tutuluyor ve "tüketildi" bilgisi yalnızca o değerde yaşıyor — konfigürasyon değişimi ya da süreç ölümünde çip **aynı pano içeriğiyle geri geliyor** ve kullanıcı aynı listeyi ikinci kez ekleyebiliyor. Çökme yok, ama **adetler sessizce ikiye katlanıyor**. Geçmiş/Ayarlar'dan Liste'ye her dönüşte de aynı şey oluyor. (b) Okuma `LaunchedEffect(Unit)`'ten geldiği için Liste'ye **her girişte** koşuyor; iOS'ta bu sistem yapıştırma izni uyarısını tetikliyor. Yeni API iOS tarafında önce `hasPlainText`'e bakıyor — yani **kullanımdan kalkmayan API aynı zamanda uyarı dostu olan**.

- [ ] **10.11 — Ölü kod ve ölü token temizliği** *(cihazsız)*. Hepsi doğrulandı:
  - `ProductDao.observeStaples` — sorgu var, **çağıranı yok** (F6.8 ile canlanacak).
  - `AccentStrip` — belgelenmiş tüketicisi *"fiş kontrolünde yeni ürün / emin değil satırları"*, ama o ekran `AccentChip` + tonlu kapsayıcı kullanıyor. Ya silinecek ya benimsenecek; bugünkü hali gelecek okuyucuyu yanıltıyor.
  - `SafeArea` objesi (44/34dp) — sıfır referans; inset'ler `WindowInsets` ile yönetiliyor ki **daha doğrusu o**. Obje bayat.
  - `Elevation` objesi, `Sizes.toolbarAction` — tanımladıkları bileşenler yok.
  - `Sizes.hairline` — **sıfır referans**, iki kenarlık çağrısı da `1.dp`'yi elle yazıyor. Yani *"0.5dp ASLA — iOS 3x'te alt-piksele düşüp kaybolur"* garantisi **zorlanmıyor**.
  - `NeydiExtraShapes.checkRest`/`checkChecked` — sıfır referans; `CheckTarget` aynı değerleri satır içinde yeniden yazıyor.
  - `TODO(kategori-tonlari)` — 12 kategori tonu gelmediği için **12 kategori kutucuğunun hepsi aynı gri** çiziliyor. Hiçbir adım sahiplenmiyordu; tasarım devriyle birlikte gelmeli.
  - Dört ölü import ve bir yanlış yerleştirilmiş KDoc.
  - Kotlin 2.4.10'da artık gereksiz olan dört `@OptIn(ExperimentalTime)` + bir `@OptIn(ExperimentalUuidApi)` *(ikincisi `generateV7` deneysel olduğu için **kalmalı**)*.

- [ ] **10.12 — Derleyici uyarılarını sıfıra indir** *(cihazsız)*. Üçü de gerçek borç:
  - `ReceiptReader.android.kt` *"Condition is always true"* — kozmetik ama gerçek.
  - `ReceiptParser.kt` iki *"Unnecessary non-null assertion"* — `!!` taşıyıcı görünüyor ama kanıtlanabilir şekilde gereksiz.
  - `ListScreen.kt` `LocalClipboardManager` deprecation → **F10.10**.

- [ ] **10.13 — `androidHostTest` kaynak kümesi ve ölçümle kazanılmış iki fonksiyonu teste bağla** *(cihazsız)*. `composeApp/src` altında **androidHostTest dizini yok** — `commonTest`, `androidMain`, `commonMain`, `iosMain` var. Sonuç: `visualRows` ve `score` **test edilmiyor ve commonTest'ten edilemez** (ML Kit `Text` ve `android.graphics.Rect` bağımlı). Oysa bunlar projenin **en zor kazanılmış iki fonksiyonu**: görsel satır gruplaması satır sayısını 47'den 25'e indirdi ve kolon karışmasını çözdü; yön puanlayıcı 8 puan / 0 puan farkıyla ölçüldü. Bugün o bilgiyi koruyan tek şey **elle cihaz koşumu**.
  **Ucuz yarısı:** `score(rows: List<String>)` imzasında **hiç Android tipi yok** — commonMain'e taşınırsa mevcut `commonTest`'ten, elimizde zaten olan gerçek fiş satır listeleriyle test edilebilir ve 8-vs-0 ölçümü **regresyon testine** dönüşür. `visualRows` gerçekten androidHostTest + sahte bir ML Kit `Text` istiyor; asıl korunmaya değer kısmı `Rect` geometrisi (medyan yükseklik, 0,6 tolerans, `centerY` gruplaması).
  *Not: `androidHostTest` build modelinde **var** ve taşıyıcı — `commonTest` onun derlemesine giriyor ve JVM SQLite'ı oradan alıyor (F2.3). Eksik olan yalnızca kendi test dosyaları.*

- [x] **10.16 — `PriceText`'i ikiye ayır** ✅ *(tasarım kararı 10)*. `priceChip` 14sp/600 ve `priceRow` 17sp/600 ayrıldı; fiş satırı `priceRow` kullanıyor — bir kademe büyük olması dokunulabilirliğin kendisi, süs değil.
  *Aşağıdaki gerekçe kayıt için duruyor:* Tasarım fiyat metnini iki bağlamda ayrı istiyor: **çipte 14sp**, **fiş satırında 17sp**. Repoda tek stil **15sp** ve handoff'un kendi uyuşmazlık tablosu *"ayrılması öneriliyor — tek 15sp iki bağlamı da tam karşılamıyor"* diyor. Bugün görünür bir etkisi yok çünkü fiyat çipi hiç çizilmiyor (`priceHint` hep `None`); **F5.2 çipi bağladığı an** iki bağlam da aynı stille çizilecek. Aynı geçişte `PriceChip`'in 92dp sabit genişliğine `maxLines = 1` + `Ellipsis` eklenmeli (bkz. F10.5'teki kırpılma adayı).

- [ ] **10.3 — `graph.json` takip kararı** *(cihazsız)*. Mac'e geçişte yeniden değerlendir — merge driver kurulu ama graph.json gitignore'da olduğu için şu an atıl.

- [ ] **10.4 — Araştırma güncellemesi** *(cihazsız)*. Faz 0 sonucunu `docs/03-arastirma-bulgulari.md`'ye işle; çürütülen varsayımları güncelle. **Bu adım koştuğu ana kadar o doküman aktif olarak yanlış bilgi veriyor** — en az on bir iddiası çürütüldü ve doküman hâlâ ilk hallerini yazıyor (görsel LLM'in birincil mimari olduğu, çevrimdışı OCR'ın kapsam dışı olduğu, TOPLAM'ın neredeyse kusursuz okunduğu, mağaza adının neredeyse kusursuz okunduğu, fiş başına ~20 kalem, tek native dikiş, barkod yolunun çalıştığı…). Tam liste bu dosyanın **Öğrenilenler** bölümünde.

## Faz 11 — Tasarım sadakati *(15 Ağu 2026'da açıldı)*

> **Tasarım kaynağı artık repoda:** `docs/tasarim/`. Devir paketinin *"Claude Design projesi burada YOK"* uyarısı geçersiz — proje `Neydi Tasarım.zip` içinde duruyormuş. Denetim: [05-tasarim-denetimi.md](05-tasarim-denetimi.md).
>
> **Mekanik kuralların hepsi zaten temizdi** (Fraunces 24sp altı yok, accent yalnızca `AccentSurface` içinde, `clickable` sıfır kullanım, `uppercase` yok, `0.5.dp` yok, blur/renkli gölge yok, dynamic color yok, dialog/badge/bottom-nav yok). Sapmalar **yapısal**.

- [x] **11.1 — İkon seti.** ✅ Uygulamada **hiç ikon yoktu**; tasarım her ekranda kullanıyor. Sadakatin önündeki en büyük tekil engeldi.
  **Sapma kayda geçti:** tasarım **Material Symbols Rounded** istiyor, o font paketlenmiş bir Compose artifact'i olarak yayınlanmıyor ve indirilemedi. `Icons.Rounded.*` kullanıldı — aynı çizim dili, aynı 24dp optik boyut. `NeydiIcon.kt` tasarımın **adlarını** taşıyor (`NeydiIcons.PushPin`), böylece çağrı yerleri tasarım dosyasıyla yan yana okunabiliyor ve set değişirse tek dosya değişiyor.
  Artifact JetBrains tarafında 1.7.3'ten sonra yayınlanmadı; sürüm ayrı pinlendi (ikonlar salt veri, çalışma zamanında bir şey bağlamıyor).
- [x] **11.2 — Ekran 1 yapısal sapmaları.** ✅ Çip şeridi → `more_vert` taşma menüsü + altta "Alışverişe çıkıyorum"; başlık alt satırı "N ürün" → "Son alışveriş: 8 gün önce · 642 TL"; başlık 24sp → 22sp/700; `size/header` token'ı eklendi; `push_pin`, girdide `add`, floating toolbar (pill + hairline + 3dp gölge + 56dp hedefler).
- [x] **11.3 — Boş durumlar.** ✅ Sola hizalı, Fraunces başlık, tasarımın metinleri, ve **"Geçen sefer aldıklarını ekle"** (yeni yetenek: `addFromLastTrip`).

- [ ] **11.4 — Tanımlı ama hiç kullanılmayan tasarım primitifleri.** *(`Category.tintArgb`/F6.9 ile aynı sınıf hata)*
  Devir paketi bunları getirdi, hiçbir ekran bağlamadı: **`Modifier.focusRing`** (0 kullanım — tasarım *"ripple olmadığı için 2dp halka zorunlu"* diyor, yani klavye/erişilebilirlik odağı hiçbir yerde görünmüyor), **`AccentSurface`** ve **`AccentStrip`** (0 kullanım; şerit Ekran 3'ün *"geçen sefer unuttun"* satırı için), **`SafeArea`** (0 kullanım).
  `SafeArea` için **karar verildi ve sapma bilinçli:** ekranlar sabit 44/34dp yerine gerçek `WindowInsets.safeDrawing` kullanıyor. Android'de doğrusu bu — tasarımın sabitleri iOS ölçüleri ve cihazdan cihaza değişen çentik/gezinme çubuğunu karşılamıyor. `SafeArea` sabitleri referans olarak duruyor.
- [x] **11.5 — Ekran 6 · 2 · 4 sadakat denetimi.** ✅ Yazılmış ama denetlenmemiş ekranlar denetlendi; bulunanlar 11.7–11.9'a dağıldı.
- [ ] **11.6 — Alışveriş modu satır container'ı.** Tasarım: ışıkta surface `#FFFFFF`'e çıkıyor, satırlar 1.5dp kenarlık kazanıyor, metadata tek bir `chevron_right` arkasına katlanıyor. Kodda kenarlık var, surface yükselmesi ve chevron katlaması yok.

- [x] **11.7 — Karar defteri, birinci tur (1, 3, 5, 7, 8, 9, 10, 12).** ✅ Tasarımın on iki maddelik karar defteri geldi ve uygulandı: alışveriş modundan çıkış (`more_vert` → *"Alışverişi bırak"*), toolbar iki hedefe indi, ilk gün 12 **ürün** çipi, Ekran 3'ün asimetrik varsayılanları, `NeydiToast`, Fiş Kontrol manşeti, başlıktaki avatar, Ekle sheet'indeki işaret.
- [x] **11.8 — Karar defteri, ikinci tur (13, 14, 15).** ✅ *Cihazda doğrulandı.* Zincir adı (ticari unvan değil), adı okunamayan satırın başlığında **barkod**, ve toplam okunamadığında `~` önekli manşet. `StoreDisplayName.kt`, `barcodeOf`, `AccentSurface`'ten geçen kısa amber çip.
  **Bilinçli sapma:** büyük/küçük harf düzeni uygulanmadı — locale'siz dönüşüm bu projede yasak (`"İNCİR".lowercase()` yedi kod noktası).
- [x] **11.9 — Karar defteri: 2, 4, 11.** ✅ *Cihazda doğrulandı.* *"Verilerimi sil"* tam ekran onay destinasyonu (F11 açık soru 5 kapandı), çok parçalı fiş tek akış, Mağazalar bölümü (F5.9).
- [ ] **11.10 — Tasarım bulguları, üçüncü tur.** [08-tasarim-bulgulari.md](08-tasarim-bulgulari.md) — teknik iş yaparken görülen eksikler burada birikiyor, toplu prompt olarak tasarıma verilecek. **Şu an altı madde:** harf düzeni, barkodu da olmayan tartı satırı, *"Takip edilen zincirler"* chevron'unun hedefi, Ekran 7 boş halinin karar 11 ile çelişmesi, tek akışta eskiyen parça çipi, *"devamını çek"* satırının uzun fişte kaydırmadan görünmemesi.

### ✅ Kullanıcıya sorulacaklar — hepsi karara bağlandı

Bu bölümdeki yedi madde tasarımın **karar defteriyle** (`docs/tasarim/Neydi - Kararlar.dc.html`) kapandı. Kayıt için, hangi madde nasıl cevaplandı:

| Soru | Karar | Nerede |
|---|---|---|
| Toolbar'ın `undo` / `filter_list` düğmeleri | Toolbar iki hedefe indi; ikisi de kaldırıldı | 3 → F11.7 |
| İlk gün çipleri: ürün mü reyon mu | **Ürün çipi**, ama sabit liste değil — `commonalityRank` ilk 12'si | 5 → F11.7 |
| Başlıktaki avatar | Çiziliyor | 9 → F11.7 |
| Alışveriş modundan çıkış yolu | `more_vert` menüsünde tek madde: *"Alışverişi bırak"* | 1 → F11.7 |
| *"Verilerimi sil"* nasıl onaylanacak | Dialog değil, **tam ekran destinasyon** | 2 → F11.9 |
| Ayarlar'daki *"Takip edilen zincirler"* | Satır **fişten doğuyor**, elle mağaza eklenmiyor | 11 → F11.9 |
| `PriceText` ayrımı | Çipte 14sp / fiş satırında 17sp — ikiye ayrıldı | 10 → F11.7, F10.16 |

*"Haneden çık"* hâlâ Faz 7'ye bağlı ve bilerek çizilmedi.

**Yeni sorular F11.10'da birikiyor** — [08-tasarim-bulgulari.md](08-tasarim-bulgulari.md).

## Şema sürüm planı — v2 → v3 ✅ **yapıldı ve cihazda doğrulandı**

> **Durum: bump tamamlandı.** `version = 3`, `AutoMigration(2, 3)`, dışa aktarılmış `3.json` (identityHash `9a25da10097f10bb5f49f777e7a8c9ae`). Aşağıdaki plan kayıt için duruyor; **kural kısmı hâlâ geçerli ve gelecek her bump'ta uygulanacak.**

### Ne girdi

Üretilen migration **20 `ALTER TABLE` + 2 `CREATE TABLE` + 1 `CREATE UNIQUE INDEX`** — boş değil, ve bu ayrıca kontrol edildi (F4.1'in belirtisi tam olarak *hiçbir şey yapmayan* bir migration'dı).

| Ne | Kime ait | Biçim |
|---|---|---|
| `ReceiptLine.unit` | F5.1 | `String?` — fiyatın hangi birim başına olduğunu belirleyen şey |
| `ReceiptLine.isDiscount` | F5.6 | `Boolean, defaultValue = "0"` |
| `Receipt.receiptDate` | F5.8 | `Long?` |
| `PriceObservation.priceUnit` | F5.1 | `String?` |
| `ProductStats.muAdjust` | F6.2 | `Double, defaultValue = "0"` |
| `TripLine.takeOutcome` | F6.2 / F4.12 | `TakeOutcome?` = `TAKEN / NOT_NEEDED / FORGOTTEN` |
| `suggestion_block` tablosu | F6.5 | `productId` + `source` (`AUTO/MANUAL`) + `blockedAt` + `unblockedAt?`, `UNIQUE(householdId, productId)` |
| `app_settings` tablosu | F6.6 / F6.7 | hane başına tek satır: `setupCompletedAt`, `tempoDays`, `syncPhotos` |
| `Household.joinCode`, `Member.email` | F6.7 / F7.2 | `String?` |
| `updatedAt` — **senkron edilen 11 tablonun hepsinde** | F7.4 | `Long?` |
| `SuggestionEvent.deletedAt` | F6.5 | `Long?` — tabloda tombstone **yoktu** ve yazılı muafiyeti de yoktu |
| Enum girdileri Türkçe→İngilizce | — | `OpType` → `INSERT/UPDATE/DELETE`; `SuggestionOutcome` → `SHOWN/ADDED/REJECTED/IGNORED` |

### Ne bilerek GİRMEDİ

**`external_price` tablosu (F5.4).** Şeklini marketfiyati'nın gerçek yanıtı belirliyor ve o endpoint **hiç çağrılmadı**. Şeklini tahmin ettiğim bir tabloyu boşken eklemek, sonradan bir bump'tan **daha kötü**: yanlış şekil de boşken bedava değil, çünkü F5.4 onu doldurduğunda düzeltmek yine bump gerektirir. Bu yüzden F5.4 kendi bump'ını taşıyacak ve o zamana kadar API'nin dönüşü bilinecek. Sınır şu: **şekli bugün var olan kod ya da tasarım spec'i belirliyorsa girdi, keşfedilmemiş bir dış API belirliyorsa girmedi.**

### Kural (değişmedi, her bump'ta geçerli): yeni kolon nullable ya da varsayılanlı

`connection.execSQL` commonMain'de **yok** (androidx.sqlite bilerek dışarıda bıraktı: web varyantı suspend, nonWeb değil). Bunun **iki yarısı var:**

1. Yasak olan **yalnızca VERİ GERİ-DOLDURMA**. Anotasyonla yapısal değişiklikler — `@DeleteTable`, `@DeleteColumn`, `@RenameTable`, `@RenameColumn` — `execSQL` istemiyor ve **kullanılabilir**.
2. Dolayısıyla **sert kural:** yeni kolon ya nullable olacak ya `@ColumnInfo(defaultValue = ...)` taşıyacak. Bu bump'ta eklenen iki NOT NULL kolonun (`isDiscount`, `muAdjust`) ikisi de varsayılan taşıyor; üretilen SQL'de `NOT NULL DEFAULT 0` olarak göründüğü doğrulandı. Anlamlı bir sabit varsayılanı olmayan bir NOT NULL kolon **hiç eklenemez**.

**Bu kuralın belirlediği bir gelecek:** F7.4'ün `updatedAt`'i `createdAt`'e **varsayılan olamazdı** (ifade varsayılanı yok), o yüzden `Long?` girdi ve **her okuyucu null'ı `createdAt` saymak zorunda**. Bunu Conventions'a madde 7 olarak yazmak ve her DAO UPDATE'inin `updatedAt` yazmasını sağlamak **F7.4'ün işi** — kolon hazır, yazma yolu değil.

### Cihazda doğrulama — F4.1'i yakalayan sınavın aynısı

**Gerçek v2 verisi olan telefona v3 kuruldu, `pm clear` YAPILMADAN.** Sonuç:

- `PRAGMA user_version = 3`
- **19 tablo** (18 entity + Room'un `room_master_table`'ı); iki yeni tablo da yerinde
- Beklenen **21 yeni kolonun 21'i** de var, eksik yok
- **Migration öncesi verinin tamamı sağ:** 245 katalog tohumu, 12 kategori, 7 ürün, 4 gezi, 6 gezi satırı, 4 fiş, 6 fiş satırı — ve **1 `ProductAlias`, yani F4.7'nin öğrendiği düzeltme migration'dan sağ çıktı**
- Yeni nullable kolonlar NULL, yeni NOT NULL kolonlar varsayılanında; okunmuş File fişi hâlâ 484,58 toplamını taşıyor
- Çökme yok, `Migration didn't properly handle` yok

*Not: doğrulama ekran görüntüsüyle değil **veritabanının kendisi çekilip okunarak** yapıldı — şema sürümü, her kolon ve her satır sayısı doğrudan sorgulandı. Ekran görüntüsünden güçlü kanıt.*

### Ve artık bir nöbetçisi var → **F10.15 ✅**

`SchemaBaselineTest` (`androidHostTest` — projenin ilk androidHostTest dosyası, bkz. F10.13) yayınlanmış her şema JSON'unun `identityHash`'ini **elle yazılmış** değerlere karşı doğruluyor: 1.json, 2.json ve 3.json. Üç test: temeller değişmemiş · her yayınlanmış dosya var · diskteki en yüksek şema takip ediliyor.

**Neden elle yazılmış hash:** dosyayı kendisiyle karşılaştırmak hiçbir şey kanıtlamaz. Bu değerler **ikinci bir gerçek kaynağı** — biri değişirse ikisi ayrışır ve test bağırır. F4.1'in kazasını *sessiz ve yıkıcı* olmaktan *gürültülü* olmaya çeviren şey bu; o gün hiçbir şey fark etmemişti.

**Bump sırası da bunun için seçildi:** nöbetçi **bump'tan önce** yazıldı ve 1/2 ile yeşil olduğu görüldü; bump'tan sonra **hâlâ yeşil** kaldı — yani temellerin dokunulmadığı iddia edilmedi, **ölçüldü**. (Ayrıca `git status` yalnızca `3.json`'u yeni gösterdi, 1 ve 2'de diff yok.)

**Yeni sürüm eklerken:** yeni `<n>.json` üretildikten sonra hash'i teste eklenir. **Eski satırlar asla güncellenmez** — güncellemek nöbetçiyi anlamsız kılar.

---

## Öğrenilenler — ne yanlış çıktı, ne doğru çıktı

> Bu bölüm sonradan eklendi ve **fazlar bittiğinde silinmeyecek**. Sebebi tek: bu projede yanlış çıkan şeylerin **çoğu sessizce** yanlış çıktı — derleme yeşil, testler yeşil, ekran doğru görünüyor. O yüzden hangi sınıf hataların bu kod tabanında **tekrar ettiği** yazılı olmak zorunda.

### Tekrar eden altı sessiz hata sınıfı

**1. Kendi yazdığım örnek kendi varsayımımı onaylar.** En pahalı örnek: fiş ayrıştırıcının **tamamı yanlıştı ve 17 testi geçiyordu** — örnek fişleri de kuralları da ben yazmıştım. İki gerçek fiş **üç varsayımı birden** çürüttü (nokta ondalık, toplam satırında KDV geçmesi, miktar satırının üründen önce gelmesi). Aynı sınıftan: aynı oturumda **dört test boş liste üzerinde geçiyordu** (`all {}` / `none {}` boş kümede daima true) ve katalog testleri veritabanını **veri dosyasıyla** karşılaştırdığı için 245 ürün 50'ye düşseydi hepsi geçerdi. **Yerleşen kural:** kurgu gerçek cihaz çıktısından gelir, ve negatif iddianın altına bir **boyut tabanı** konur.

**2. Testin ısırabildiği kanıtlanmadıkça test yoktur.** F1.2 kontrast testi ilk koşumunda **gerçek bir erişilebilirlik hatası** buldu (çifte soluklaştırılmış metadata, 3,98:1). F2.3 kısıt testleri **DAO üzerinden değil ham SQL** ile yazıldı, çünkü DAO `REPLACE` ile yazsaydı ikinci satır sessizce üstüne biner ve test yeşil kalırdı. Bu oturumda UUID testi `random()`'a geri alınarak **kırıldığı gösterildi**. Yerleşen pratik: yeni bir değişmez yazarken **onu bilerek boz ve testin bağırdığını gör**.

**3. Önek/alt-dizgi eşleşmesi kelime sınırı olmadan.** Aynı oturumda **üç kez**: `import androidx.room3.RoomDatabase` `RoomDatabaseConstructor`'ı önek olarak eşleyip böldü (Room KSP'nin kafa karıştırıcı hatasına yol açtı); `" pos"` `poseti`'nin içinde bulundu ve **alışveriş poşeti ödeme satırı sanıldı** (1,00 TL toplamdan düştü, aritmetik kapısı **haksız yere** tutmadı); ve yeniden adlandırmada `\b` alt çizgiyi kelime karakteri saydığı için `_girdi` atlandı.

**4. Kolon adı / bağ değişkeni / SQL takma adı, Kotlin ile sözleşmedir.** Yeniden adlandırma `WHERE matchKey LIKE :onEk` bağ değişkenini ve `p.name AS ad` takma adını bozdu — ikisini de Room derleyicisi yakaladı. Ders: *"string'lere dokunma"* kuralının **ters yüzü var** — SQL string'i ve Kotlin şablonu (`${...}` içi) **gerçek kod** taşır.

**5. Yerel-duyarsız harf dönüşümü Türkçe'yi bozar, hata vermeden.** `"İNCİR".lowercase()` beş harf yerine **yedi kod noktası** üretiyor (her İ'nin ardına U+0307) ve `== "incir"` **false** — kullanıcı "İncir" yazıp fiş "INCIR" yazdığında uygulama iki ayrı ürün sanır ve **fiyat geçmişini ikiye böler**. Aynısı `uppercase()` ile iki-harf avatarında: `"incir" → "IN"` yanlış, `"İN"` doğru.

**6. Ekranda görünmeyen şey yoktur — ve önizleme onu maskeleyebilir.** Bu oturumda: `onHistory` ve `onGoShopping` aşağıya geçiliyor ama **hiç çağrılmıyor** (Kotlin kullanılmayan parametre için uyarı vermiyor) → iki ekran **hiçbir yerden açılamıyor**. Üç başlık düğmesi taşıyordu ve *"Ayarlar"* **kesiliyordu** — `Row` taşan içeriği kırpıyor, kaydırmıyor. Ve en öğreticisi: iki yeni ekran karanlık modda **açık zemin** gösteriyordu, **önizlemeler bunu göremezdi** çünkü `NeydiPreview` içeriği kendi `Surface`'ına sarıyor — yani koşum tam olarak gerçek ekranın **eksik olduğu şeyi** sağlıyordu.

### Çürütülen varsayımlar

| Varsayım | Gerçek | Nasıl anlaşıldı |
|---|---|---|
| Görsel LLM tek yol, cihazda OCR bir "fallback" | **Cihazda ML Kit tek yol oldu**; API anahtarı, proxy, ağ ve ücret hiç gerekmedi | Kullanıcı ücret sordu, alternatif ölçüldü |
| Çevrimdışı OCR **kapsam dışı** | Kapsamın **tamamı** | Aynı karar |
| Aritmetik `Σ(satır) + KDV = TOPLAM` | **KDV tamamen dışarıda**; raf fiyatı kanunen KDV dahil | İki gerçek fiş, birebir 225,50 ve 484,58 |
| TOPLAM "neredeyse kusursuz" okunuyor | Toplam satırı **"Ödenecek KDV Dahil Tutar"** ve içinde KDV geçiyor; KDV'yi eleyen kural **gerçek toplamı eliyordu** | Gerçek fiş |
| Mağaza adı "neredeyse kusursuz" | İki fişten birinde **adres satırı** yakalanıyor (`FiLE OVACIK / KEÇİÖREN/ ANKARA`) ve **hiçbir test bunu tutmuyor** | Cihazda Fiş Kontrol başlığı |
| Fiş başına ~20 kalem | ~60 kalemlik fiş tek karede **4,7 piksel/satır** — ML Kit 60 satırın **2**'sini okudu. Yazılımla çözülemez | Uzun fiş ölçüldü |
| Fiş QR kodu satır kalemleri taşır | **Taşımıyor** — yapısal | Araştırma |
| Barkod ile kanonik eşleşme çalışır | `searchByIdentity` gerçek EAN-13'lerde **6/6 boş**; o metot marketfiyati'nın kendi iç token'ını alıyor | İlk araştırma ajanı **test ettiğini iddia etmişti**, doğru değildi |
| CMP iOS'ta **tek** native dikiş (kamera) | Dikiş **sekiz** ve **kamera onlardan biri değil** (FileKit ortak API veriyor) | Kod sayıldı |
| Nav3 saveable back stack "iOS'ta sessizce bozulur" | **Her platformda gürültülü**: `rememberNavBackStack` varsayılan modülü **koşulsuz** `require` ile reddediyor | navigation3-runtime 1.1.1 kaynağı |
| Ripple teemada kaldırıldı | **Hiç yürürlükte değildi**: material3'ün `Surface`'ı indication'ı **üç ayrı yerde sabit kodluyor**, tema override'ı ona hiç ulaşmıyor | material3 1.9.0 kaynağı + cihazda piksel ölçümü |
| `compose.material3` = CMP sürümü (1.11.1) | **1.9.0** — material3 CMP'de **ayrı** sürümleniyor ve 1.11.1 diye bir sürümü **yok** | compose-gradle-plugin kaynağı |
| Room 2 örnekleri geçerli | Gradle eklentisi kendini **`room3`** adıyla kaydediyor; `room { }` yazan her örnek patlıyor | Eklenti kaynağı |
| AGP 9 ile tek modül çalışır, bypass bayrakları çözüm | Bayraklar **AGP 10'da kalkıyor** ve `newDsl=false` sürüm 9'u DSL 8'e düşürüyor → **modül ikiye ayrıldı** | Ölçüldü |
| id'ler UUID **v7** | **v4**'tü — `Uuid.random()` = `generateV4()`. Sözleşme yorumda doğru, kodda tam tersi | stdlib kaynağı |
| iOS hedefi derleniyor | **HEAD'de kırıktı** ve bu ortak test paketini de bloklıyordu, yani `allTests` **hiç koşmuyordu** | `git stash` ile doğrulandı |
| `(1)` ve `(2)` bitmeden tek satır Kotlin yazma | 30 adım **F0.3/0.4/0.5 açıkken** yazıldı — ve bu **doğru karar** oldu: görünür ilerleme fiş ölçümünden bağımsızdı | Kayıt |

### Ölçümle doğrulanan doğru kararlar

- **Cihazda ML Kit** — daha az bağımlılık, sıfır ücret, fotoğraf telefondan çıkmıyor, ve güvenlik ağı (Fiş Kontrol ekranı) zaten şart koşulmuştu.
- **`closeIfOpen` tek karşılaştır-ve-yaz + kapalılığın otoritesi `completedAt`** — ikinci kapatma **sıfır satır** günceller, yani "ben kapattım" ile "zaten kapalıydı" ayırt edilebiliyor. Bu, mutabakatın çift koşmasını **veritabanı düzeyinde** engelliyor.
- **Zincir bazlı `ProductAlias`** — cihazda kanıtlandı: düzeltme, fişin **sıfırdan yeniden okunmasından** sonra da uygulanıyor ve tekrar sorulmuyor.
- **Görsel satır gruplaması + yön puanlaması** — satır sayısı 47→25, ve **aynı fiş iki yönde de aynı 25 satırı** veriyor. EXIF'e güvenilmedi; küçültmede yeniden kodlanan JPEG yön bilgisini taşımıyor.
- **Aritmetik kapısının üç durumu** — "doğrulanamadı" ile "tutmadı" ayrı; toplamı okuyamamak **bizim** hatamız ve kullanıcıya onun hatası gibi gösterilmiyor.
- **Tek SQL JOIN, üç `combine` edilmiş Flow değil** — üç Flow her değişimde üç yeniden yayın üretir ve satırlar bir kare boşluklu görünürdü.
- **Reyon sırası alışverişte donuyor** — piksel diff: bir satır işaretlendi, altındaki üç satırın farkı **0/60000**.
- **`NeydiButton` + `Modifier.pressable`** — cihazda ölçüldü: **%5,6 karartma**, belgelenen %6 tonal overlay. Material3 ripple'ı rengi **açıyordu**.
- **Para = `Long` kuruş, `Double` değil** — bu uygulamanın işi fiyat toplamak.
- **`packSize`/`packUnit` ayrı alanlar** — ambalajı görmeyen bir fiyat hafızası shrinkflation'ı "fiyat sabit" diye raporlar, yani **yalan söyler**.
- **`medianIntervalDays` medyan, ortalama değil** — bir kez 40 gün unutmak ortalamayı kaydırıp "10 günde bir alıyoruz" gerçeğini gizler.
- **UI'da dürüstlük kuralları** — uydurma sayı yok (`null` = bilmiyoruz, `0` = bedava değil), iyi durum **sessiz** (VERIFIED'a çip konmuyor), geri alma yolu **görünür** ("Hepsini almadım").
- **Kontrast kuralı iddia değil TÜRETME ile kilitli** — amber'ın kontur gerektirmesi ölçümden çıkıyor, bağımsız bir iddia olarak yazılmıyor; ikisi birlikte yanlış olamaz.

---

## Açık kararlar — iş başlamadan verilmesi gerekenler

> Her biri **kod yazmayı bloklamıyor** ama **yanlış verilirse geri alınması pahalı**. Sırası kabaca ihtiyaç sırası.

1. **Şema: tek v3 bump mı, faz başına bump mı?** → Yukarıdaki bölüm **tek bump** öneriyor. Kararı geciktirmenin maliyeti: her geciken hafta boş kalması gereken bir tabloya veri yazma riskini artırıyor.
2. ~~**`ProductStats` neyi sayacak?**~~ ✅ **KARAR: `trip_line` ∪ eşleşmiş `receipt_line`**, `(productId, tripId)` üzerinde tekilleştirilmiş. F6.1'de uygulandı ve cihazda doğrulandı — *Sos* ürünü yalnızca fişten sayıldı.
3. **"Unuttum" ile "gerekmedi" ayrılacak mı?** Tasarım üç düğme istiyor, şemada tek boolean var. Ayrılmazsa öneri motoru iki zıt sinyali aynı sayar.
4. **Fiyat gözleminin birimi ne?** `PriceObservation` fiyatın **hangi birim başına** olduğunu kaydedemiyor, ve `observeEstimate` `quantity × unitPriceMinor` çarpıyor — kg başına bir fiyatı adet sayısıyla çarpmak sessiz bir yanlış sonuç üretir.
5. **Katalog fiyatı ile ödenen fiyat aynı tabloda mı?** Ayırt edici bir alan yok. Karışırsa uygulama kullanıcıya **ödemediği bir fiyatı ödedin** der — hem de reyonda.
6. **Engelleme listesi olaylardan mı türetilecek, ayrı tablo mu?** Tasarım listenin **görünür ve tek dokunuşla geri alınabilir** olmasını şart koşuyor; append-only günlükte temiz bir "geri al" yazması yok.
7. **`SuggestionEvent` senkron ediliyor mu?** `householdId` taşıyor ama `deletedAt` taşımıyor ve yazılı muafiyeti yok. Faz 7'den **önce** karara bağlanmalı.
8. **Fiş fotoğrafları senkron edilecek mi?** `supabase-storage` katalogda hazır. Cevap **hayır** olmalı ve **yazılı** olmalı — yoksa gizlilik özelliği ilk depolama bağlamasında sessizce tersine döner.
9. **Katalog nasıl güncellenecek?** Tohumlayıcı `COUNT(*) FROM category > 0` ile kapıda duruyor, yani **ilk açılıştan sonra katalog o telefonda kalıcı olarak dondu**. Bu, `Diğer` kategorisini eklemeyi, kategori düzeltmeyi ve marketfiyati tohumlamasını **imkânsız** kılıyor. Ayrıca tohum id'leri **sıradan türetiliyor** (`seed-<rank>`) — `Product.seedId` doldurulduğu an sıralar **sonsuza kadar donuyor**.
10. **Katılma akışı hane kimliğini nasıl değiştirecek?** `DEFAULT_HOUSEHOLD_ID` **her kurulumda aynı sabit**, yani ikinci telefon aynı kimlikle başlıyor. Bir haneye katılmak, ilk push'tan önce **her yerel satırın yeniden anahtarlanmasını** gerektiriyor.

---

## Riskler

**1. 4. ay çarpışması — araştırmanın "projenin durma sebebi" dediği yer.** İlk TestFlight süresi (~85 gün) dolarken grafikler hâlâ boşsa uygulama yeniden yüklemeye değmez hale gelir. **Bu riski azaltan hiçbir adım yok.** Değer eğrisi aritmetiği de ölçülmedi: ~360 satır, ~100 ürün, ve 6 ayda **yalnızca 10-20 trend edilebilir SKU**. Azaltıcı adaylar: F0.4 katalog tohumlaması (soğuk başlangıcı kısaltır), F6.6 Kurulum (3. gezide akıllı hissettirmek — var olma sebebi tam bu), ve tek gözlemle bile anlamlı olan `PriceHint.Single`.

**2. İki sessiz hata üst üste: Supabase duraklaması + GitHub Actions.** Ücretsiz katman 7 gün hareketsizlikte duruyor; 60 gün sessiz repoda zamanlanmış workflow **devre dışı kalıyor**. Yani keep-alive'ı Actions ile kurmak **önce keep-alive'ın sessizce ölmesi, sonra veritabanının durması** demek. F7.6 bunu doğru taşıyor ama **hiç doğrulanmadı** (Faz 7 başlamadı). İki kişilik bir uygulamanın **tatilde** yaşayacağı şey bu.

**3. Çevrimdışı düzenleme kaybı, iki kişilik bir hanede soyut değildir.** F7.3 bilinçli olarak kaybı kabul ediyor. Ama kaybedilen düzenleme *"eşimin eklediği ürün kayboldu"* cümlesine dönüşüyor — tombstone kuralının **tam olarak engellemek için var olduğu** cümle. **Yazılması gereken:** F7.3 hangi kaybı kabul ediyor (çevrimdışı işaretleme) ve hangisini kabul etmiyor (**EKLEME**)? Add-beats-remove F7.5'e ertelendi.

**4. Ölçek riski YOK ve bu bir karar.** ~60 satır/ay, 6 ayda ~360 satır. `receipt` tablosunun **hiç index'i yok** (DAO üçüne göre sorguluyor) ve bu **yıllarca sorun olmayacak**. Faz 5-7'de performans için index eklenmeyecek, F6.1'in tek transaction'da tam yeniden kurulumu **incremental yapılmayacak**. Taşıyıcı olan tek index zaten var: `(productId, observedAt)`.

**5. Türkçe bir yerelleştirme tercihi değil, doğruluk kısıtı.** Yerel-duyarsız harf dönüşümü bu projeyi **iki kez** ısırdı. Kalan her metin üreten adım bunu miras alıyor: F6.3 gerekçe metinleri, F6.4'ün dört şablonu, F5.3 manşeti, F6.6 tempo çipleri. **İki kural:** kullanıcıdan/fişten gelen metne asla `lowercase()`/`uppercase()` uygulanmaz (`matchKey` / `turkishInitials` kullanılır), ve **%25 metin genişlemesi** payı hesaba katılır.

**6. Gün sınırı tuzağı — öneri motorunun çalışıp çalışmamasını belirliyor.** Bütün zaman damgaları UTC epoch millis, kullanıcının "gün" kavramı ise Europe/Istanbul. `(now - then) / 86_400_000` **24 saatlik blok sayısı**, takvim günü sayısı değil — yerel saatle dün 22:00'deki bir alışveriş bugün 22:00'ye kadar "0 gün önce" okunuyor. Temposu ~10 gün olan bir uygulamada bir günlük kayma, *"12 gündür almadın, normalde 10 günde bir"* önerisinin **tetiklenmesi ile tetiklenmemesi** arasındaki fark. Hem gösterim katmanı hem `medianIntervalDays` hesabı **aynı** takvim-günü aritmetiğini kullanmak zorunda, yoksa ekran ile skor birbirinden farklı konuşur.

## Kod TODO eşlemesi

*Tablo bu oturumda koddan yeniden üretildi — dört satırı bayattı (kapanmış üç TODO hâlâ listedeydi, bir yol yeniden adlandırmadan sonra güncellenmemişti) ve iki gerçek marker hiç görünmüyordu.*

| TODO | Dosya | Kapatan adım |
|---|---|---|
| `TODO(sheet-yuksekligi)` | `ui/list/AddSheet.kt:155` | F10.5 |
| `TODO(kategori-tonlari)` | `ui/components/CategoryTile.kt:34` | **F6.9** *(yeni: hiçbir adım sahiplenmiyordu)* |
| `TODO(splash)` | `androidApp/src/main/res/values/themes.xml:4` | F8.4 |
| `TODO(ios-statusbar)` | `iosMain/ui/theme/SystemBars.ios.kt:16` | **F9.3** *(yeni: tabloda yoktu)* |
| `TODO(ios)` | `iosMain/MainViewController.kt:11` | F9.3 — **metni güncellenmeli:** ikinci maddesi F1.4'ün **yaptığı** işi istiyor ve gerekçesi o adımda **çürütüldü** |
| `TODO(tnum)` | `ui/theme/Type.kt:166` | F9.4 |

**Kapanmış ve tablodan silindi:** `TODO(font)` (F1.1), `TODO(saveable)` (F1.4), `TODO(ios-serialization)` (F1.4) — üçü de koddan gitmişti, tabloda kalmıştı.

## Bayat adlar — ROADMAP'in yazdığı ≠ kodun içindeki

*Kod Türkçe'den İngilizce tanımlayıcılara geçti, bu doküman geçmedi. Aşağıdaki adları grep'leyen biri hiçbir şey bulamaz. Metin içinde düzeltildi; tablo kayıt için duruyor.*

| ROADMAP'te yazan | Gerçek sembol |
|---|---|
| `ListeEkrani` | `ListScreen` |
| `ListeRepository` | `ListRepository` |
| `UiSatir` | `UiRow` |
| `EkleSheet` / `ui/liste/` | `AddSheetContent` / `ui/list/AddSheet.kt` |
| `GRID_ORANI` | `GRID_RATIO` |
| `miktarAyristir` | `parseQuantity` |
| `kurusFormatla` | `formatMinor` |
| `AyarlarScreen` | `SettingsScreen` |
| `KisitTest` | `ConstraintTest` |
| `Sozlesme.kt` *(kod yorumlarında)* | `Conventions.kt` — madde numaraları aynı kaldı |

**Bilerek Türkçe kalanlar, "düzeltilmemeli":** `EmptyKind.ILK_GUN` / `DONGU_ORTASI` (UI'da, hiç kalıcılaşmıyor) ve `SuggestionOutcome.GOSTERILDI/EKLENDI/REDDEDILDI/YOKSAYILDI` + `OpType.EKLE/GUNCELLE/SIL` — **ama son ikisi yayınlanmış şemada TEXT olarak duruyor ve yeniden adlandırma penceresi ilk yazmada kapanıyor** (bkz. **Şema sürüm planı**).

## Tasarım devir paketi — 15 Ağu 2026

**Claude Design handoff'u geldi** (`Neydi Tasarım.zip`) ve içinde `Dimens.kt`/`Motion.kt`'nin kaynak olarak gösterdiği `handoff/tokens.json` de var — o dosya daha önce repoda yoktu ve erişilemiyordu. Paket: `tokens.json`, `handoff/theme/{Dimens,Motion}.kt`, `handoff/ui/AccentChip.kt`, ve altı tasarım dosyası (Tasarım sistemi · Ekran 1 · Ekranlar 2–4 · Ekranlar 5–8 · Boş Durumlar · Compose Spec).

**Handoff kendi uyuşmazlık kaydını taşıyor** ve iki karar zaten repo lehine verilmiş: `dark.hairline` (repo kazandı) ve `Spacing` 12dp adımı (eklendi). Üçüncüsü **hâlâ açık ve yeni bir madde gerektiriyor:** `PriceText` tasarımda çipte **14sp**, fiş satırında **17sp**; repoda tek stil **15sp**. Handoff *"ayrılması öneriliyor"* diyor — tek 15sp iki bağlamı da tam karşılamıyor. → **F10.16**

**Bu oturumda kullanıldı:** F6.8'in üç kararı doğrudan maketlerden çıktı — bölümün en üstte olması, alışveriş modunda **hiç olmaması**, ve sabit satırın `%70 opaklık + 12dp raptiye` görünümü (repoda zaten doğruydu). Ekran 5'in üç veri halinin hepsinde iki anahtarın bulunması da F6.8'in giriş noktasını belirledi.

**Henüz işlenmemiş, kalan fazları etkileyen bulgular:**

- **Sepet tahmini eşikleri artık sayıyla belli** (F5.2/F3.8): *"Satırların %60'ından azının fiyatı biliniyorsa %40 opaklık ve `~` öneki; %30'un altında satır hiç gösterilmez."* Kod bugün yalnızca `pricedCount == 0` durumunu biliyor.
- **Ekran 1 başlığı** eksikleri: *"Son alışveriş: 8 gün önce · 642 TL"*, eş avatarı, `more_vert` taşma menüsü, ve alışveriş modunda *"Migros Ataşehir · 12/18 alındı"*.
- **Öneri şeridi gerekçe metinleri** maketlerde birebir yazıyor: *"Ekmek · her seferinde"*, *"Domates · genelde alıyorsun"*, *"Yumurta · 14 gün oldu"*, *"Çay · 21 gün oldu"* — F6.3'ün üreteceği metinlerin tonu bunlar.
- **Alışveriş modu başlığı mağaza adı gösteriyor**, yani F5.9'un `Store` çözümlemesi yalnızca fiyat karşılaştırması için değil başlık için de gerekiyor.
- Boş durumların üçünde de *"İllüstrasyon yok, üzgün yüz yok, sihirbaz yok"* — F6.6'nın 12 ürün çipi ve *"WhatsApp'tan listeni yapıştır"* butonu maketlerde duruyor.

*Paket `docs/` altına konmadı: `tokens.json` tasarım tarafının kaynağı ve handoff README'si *"repoya konmaz"* diyor. Masaüstünde `Neydi-Tasarim/` klasöründe duruyor; repoya girecek olan tek şey ondan türetilen kod.*

## İlgili dokümanlar

- [`00-isim-onerileri.md`](00-isim-onerileri.md) — isim analizi ve eleme gerekçeleri
- [`01-claude-design-prompt.md`](01-claude-design-prompt.md) — ekran ekran tasarım spec'i, CMP kısıtları
- [`02-logo-splash-prompt.md`](02-logo-splash-prompt.md) — logo konseptleri, ikon/splash teknik gereksinimleri
- [`03-arastirma-bulgulari.md`](03-arastirma-bulgulari.md) — planı değiştiren bulgular, ölçülmemiş varsayımlar
- [`../graphify-out/GRAPH_REPORT.md`](../graphify-out/GRAPH_REPORT.md) — bilgi grafiği raporu
