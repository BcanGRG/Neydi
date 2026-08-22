# Neydi — Yol Haritası

Tek gerçek kaynak. **Açık işin tamamı §1'deki tek tablodadır**; sıradaki iş her
zaman o tablonun en üst satırı. §3 yalnızca o satırların gerekçesini açar,
§6 kapanmış işi saklar.

**Ürün:** iki kişilik bir hane için ortak market listesi — ne alacağınızı
hatırlatan ve raf etiketi çektikçe **ürün bazında** fiyat hafızası biriktiren.
Döngü: *liste → markette işaretle → etiket çek → ürün + marka + market + tarih
+ fiyat gözlemi → sonraki listede fiyat ipucu*.

**Durum:** Faz E'nin **on sekiz adımı kapandı**; açık kalan tek madde
**E15'in yatay düzeni** (karar 61). Etiket okuyucusu **üç zincirde** çalışıyor
(A101, BİM, Migros); Metro ölçüldü ve bilinçli olarak ertelendi. Fikstür seti
**99 gerçek etiket**, dört zincir. Uygulama derleniyor, cihazda kurulu,
**452 test yeşil**, **sıfır derleyici uyarısı**.

Tasarım kararları **46–75** kodlandı (`11-tasarim-kararlari.md`); 56 ajanlı
denetimin **41 bulgusunun 41'i** kapandı ya da gerekçesiyle bloklu kaydedildi.

> Bu dosya **açık işi**, **kalıcı kuralları** ve **kapanmış işin arşivini**
> taşır. Fiş dönemine (16 Ağu 2026 pivotundan öncesi) ait her şey
> [ARSIV-fis-donemi.md](ARSIV-fis-donemi.md)'de dondurulmuş durumda —
> koddaki `F4.13` gibi göndermelerin kaynağı orası. Buraya geri taşınmaz.

---

## 1. Açık iş

Bu tablo dosyanın **tek doğruluk kaynağı**. Burada olmayan iş açık değildir;
buradaki her satırın gerekçesi §3'te açılıyor. **Sıra = öncelik.**

**Durum sütunu:** `[ ]` kodlanacak · `[~]` kısmen kodlandı ·
`[cihaz]` kod yeşil, cihazda görülecek (Kapı 2)

| # | İş | Durum | Neyi bekliyor | Ayrıntı |
|---|---|---|---|---|
| 1 | **F4.7 — alias sahada doğrulanacak** | `[cihaz]` | Bu build'le atılacak **ilk** çekimi: `product_alias` bugün **0 satır** | [→](#f47) |
| 2 | **Marka okuma kalitesi ölçümü** | `[ ]` | Hiçbir şeyi — 99 fikstür üzerinde, yeni tur **beklemeden** koşulabilir | [→](#marka) |
| 3 | **`priceUnit` / `packSize` normalizasyonu** | `[ ]` | — *(her market kararının ÖN KOŞULU)* | [→](#tahmin-carpimi) |
| 5 | **Geçmiş grafiği + başlık tutarı** | `[cihaz]` | **Bugünden sonra 3 gezi** — 12 gezi kapalı ama `observeTripEstimates` sıfır satır dönüyor | [→](#gezi) |
| 6 | **F6.5 — üç vuruşta otomatik bastırma** | `[~]` | `suggestion_event`'e yazan kodu ve **şema v6 bump'ını** | [→](#f65) |
| 7 | **F6.5 — sabit terfisi** | `[~]` | **Tasarımı** (`docs/28`) — iki tasarım dosyası çelişiyor | [→](#f65) |
| 8 | **docs/29 — ekleme geri bildirimi (beş soru)** | `[ ]` | Tasarımı | [→](#tasarim) |
| 9 | **docs/30 — markete göre tahmin (beş soru)** | `[ ]` | Tasarımı; **ölçüm ertelemeyi öneriyor** | [→](#tasarim) |
| 10 | **Ölü primitif sorusunu YAZ** | `[ ]` | — *(ölü primitif maddesi ona bağlı)* | [→](#olu-kod) |
| 11 | **docs/27 — on ikinci tur (dört soru)** | `[ ]` | Tasarımı | [→](#tasarim) |
| 12 | **docs/28 — on üçüncü tur (on üç soru)** | `[ ]` | Tasarımı | [→](#tasarim) |
| 13 | **F5.7 — ambalaj küçülmesi ipucu** | `[cihaz]` | Aynı üründen **iki farklı boyda** gerçek çekim | [→](#f57) |
| 14 | **F6.4 — Eksik Olabilir (Ekran 3)** | `[cihaz]` | Göz kontrolünü | [→](#f64) |
| 15 | **F11.19 — karar 36'nın renk ayrımı** | `[cihaz]` | **Karışık liste**: kimi ürün gözlemli, kimi gözlemsiz | [→](#f1119) |
| 16 | **F1.3b — `@Preview` altyapısı** | `[cihaz]` | Göz kontrolünü | [→](#f13b) |
| 17 | **F3.3 — Hızlı ekleme** | `[cihaz]` | Göz kontrolünü | [→](#f33) |
| 18 | **F3.4 — Pano yapıştırma** | `[cihaz]` | Göz kontrolünü *(pano cihazsız doğrulanamıyor)* | [→](#f34) |
| 20 | **F3.9 — "Diğer" kategorisi** | `[ ]` | F2.7'yi | [→](#f39) |
| 21 | **F6.9 — `kategori-tonlari` TODO'su silinecek** | `[ ]` | — *(tek satır)* | [→](#f69) |
| 22 | **F10.7 + F10.11 + F11.4 — ölü primitifler** | `[ ]` | Önce **soruyu yazmayı** — `focusRing`/`AccentStrip` tasarıma **sorulmadı** | [→](#olu-kod) |
| 23 | **F11.6 — alışveriş modu satır container'ı** | `[ ]` | — | [→](#f116) |
| 24 | **F10.2 — bottom sheet'leri Nav3 Scene'e taşı** | `[ ]` | — *(F10.5 bağı düştü)* | [→](#f102) |
| 25 | **F10.3 — `graph.json` takip kararı** | `[ ]` | — | [→](#f103) |
| 26 | **F0.4 — kanonik ürün kimliği** | `[ ]` | — *(F5.4'ü açar)* | [→](#f04) |
| 27 | **F5.4 — marketfiyati entegrasyonu** | `[ ]` | F0.4'ü; **F5.5'in kalan yarısı buraya bağlı** | [→](#f54) |
| 28 | **Faz 7 — Senkron** | `[ ]` | Mimari düzeltmeyi: deterministik id, yoksa outbox tıkanıyor | [→](#faz7) |
| 29 | **Faz 8 — Marka varlıkları** | `[ ]` | — | [→](#faz8) |
| 30 | **Faz 9 — iOS** | `[ ]` | Mac'i | [→](#faz9) |
1. **On iki kapanmış gezi var ama `observeTripEstimates` sıfır satır
   döndürüyor.** Bütün gözlemler 22 Ağustos, geziler 15–16 Ağustos ve sorgu
   `observedAt <= completedAt` şartını taşıyor (E18'in "o günkü fiyat" kuralı).
   Yani Geçmiş grafiği ve başlık tutarı "3 gezi" değil, **"bugünden sonra
   3 gezi"** bekliyor.
2. **`product_alias` 0 satır.** Çekimler 16:48–17:00 arasında, F4.7'li kurulum
   18:35'te yapıldı — alias yazan kod bu telefonda **hiç koşmadı**. F4.7'nin
   doğrulaması, bu build'le atılacak ilk çekimle başlıyor.
3. **Beş sabitin `medianIntervalDays` değeri 0** (on iki gezi aynı gün
   kapandı), yani "Bitmiş olabilir" önerisi hiç ateşleyemiyor. Gereken şey
   gezi *sayısı* değil: **en az üç FARKLI güne yayılmış gezi.**

---

## 2. Çalışma sözleşmesi

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

**İşaretler:** `[ ]` yapılmadı · `[~]` **kısmen kodlandı** · `[cihaz]` kod
tamam, cihaz doğrulaması bekliyor · `(cihazsız)` Kapı 2'den muaf ·
**✅ = kapanmış, kutusu yok — §6 arşivinde**.

**Cihaz döngüsü:**
```bash
./gradlew :androidApp:installDebug
```
Ekran görüntüsü: `adb exec-out screencap -p > shot.png` ·
`adb`: `C:\Users\buroc\AppData\Local\Android\Sdk\platform-tools\adb.exe`

---

## 3. Açık işin ayrıntıları

Numaralar **kimliktir, sıra değildir** — eski F-numaraları PR geçmişi ve kod
göndermeleri bozulmasın diye korunuyor. Kapanmış maddelerin gövdesi §6'da.

### 3.1 Cihazda görülecekler

Kod yeşil, testler yerinde, cihazda görülmedi. Kapı 2 kuralı gereği hiçbiri
"bitti" sayılmıyor.

#### F5.7 — Ambalaj boyu çıkarımı <a id="f57"></a>

**✅ (kod)** — `readTagPack` **gramaj satırından** okuyor: `750 G` → `750.0` + `gr`.

**Kopukluk tek bir yerdeydi ve sinsiydi:** şema kolonları (`packSize`,
`packUnit`), `observeList`in alt sorguları ve `PriceHint.PackChanged` dalı
E16'dan beri hazırdı, `readTagPack` gramajı okuyordu — ama
`TagCaptureViewModel`'de **`pack` kelimesi hiç geçmiyordu**. Her gözlem iki
NULL kolonla yazılıyordu, yani shrinkflation dalı **hiç ateşleyemezdi** ve
hiçbir test bunu söylemiyordu.

**Yazmadan önce ölçüldü** — 99 fikstürde 50 gramaj okunuyor; asıl soru *aynı
etiketin iki çekimi aynı gramajı veriyor mu* idi ve cevap evet:
`133220/226/227` üçünde de `1.0 lt`, `133247/248/249` üçünde de `2.0 kg`.
İki **farklı** gramaj okuyan tek vaka yok — uydurma bir ambalaj değişiminin
kaynağı tam da bu olurdu.

**Çelişkili etikette gramaj da düşüyor** (7/50): çapraz kontrol üç sayıdan
birinin yanlış olduğunu söylüyor, hangisinin olduğunu değil. Fiyat hatası bir
sayıdır, yanlış gramaj bir **iddiadır** — *"ambalaj küçüldü"*.

Kartın kopyası `ConfirmCard.readFrom` olarak **ayrıldı**: kusurun yaşadığı
dikiş artık ViewModel'in dışında ve test edilebilir.
`writeTagObservation`'ın ambalaj parametrelerinin **varsayılanı yok** —
atlanmaları derleme hatası; `save()`in çağrı yeri birim testle korunamıyor,
tek nöbetçisi derleyici.

⚠ **Cihazda görülmedi:** ipucu için aynı üründen **iki farklı boyda** gerçek
çekim gerekiyor. Ölçüm `docs/24`'ün yöntemiyle, kanıt
`ListPriceHintTest.twoRealTagsInDifferentSizesRaiseShrinkflation`.

#### F6.4 — Eksik Olabilir (Ekran 3) <a id="f64"></a>

**✅ (kod)** — "Son ödenen fiyat" kolonu artık gözlemden besleniyor.
Cihazda göz kontrolü bekliyor.

#### F11.19 — Karar 36'nın renk ayrımı cihazda görülmedi <a id="f1119"></a>

Kod, testler (8 yeni) ve önizleme yerinde; ama uygulamada bugün **gözlem
üretebilen bir yüzey** ilk kez E15'le geldiği için karışık liste (kimi koyu,
kimi soluk) çalışan uygulamada henüz görülmedi. Sıfır gözlemli hâl cihazda
doğrulandı.

E15 kapandı; madde artık **cihazda görülecekler kuyruğunda** —
**F11.29'un delta oku da aynı kuyrukta**.

#### F11.29 — İkon envanteri 17 <a id="f1129"></a>

**✅ (kod)** — İki Phosphor oku (`ph-arrow-up` / `ph-arrow-down`) taşındı,
`autoMirror` **kapalı** — dikey yön taşıyorlar. `Chips.kt` artık Unicode glif
(`↑`) yerine ikon çiziyor: karar 32 *"ikonlar `Text` olarak çizilmiyor"* diyor,
üstelik glif sistem fontundan çözülüyordu ve Skia'nın yedek zinciri iki
platformda aynı şekli vermiyordu. Testin `autoMirror` vakası iki taraflı:
hepsine `autoMirror` veren bir değişiklik burada düşer.

Ok boyu (14dp) tasarımda dp olarak yazılı değil (*"12sp metinle birlikte"*);
gözle seçildi, türetilmedi.

⚠ **Cihazda görülemedi** — delta çipi ≥2 gözlem istiyor ve bugünkü 12 gözlemin
hepsi ayrı ürün. **F11.19 ile aynı kuyrukta.**

#### F1.3b — `@Preview` altyapısı <a id="f13b"></a>

**✅ (kod)** — cihazda göz kontrolü bekliyor.

#### F3.3 — Hızlı ekleme <a id="f33"></a>

**✅ (kod)** — cihazda göz kontrolü bekliyor.

#### F3.4 — Pano yapıştırma <a id="f34"></a>

**✅ (kod)** — cihazda göz kontrolü bekliyor. *(Pano cihazsız doğrulanamıyor;
emülatörde ve testte anlamlı bir kanıt üretmiyor.)*

### 3.2 Kodlanacak — yerel

#### Tahmin çarpımı ambalajı okumuyor <a id="tahmin-carpimi"></a>

Tahmin sorgusu `adet × etiket fiyatı` yapıyor; `packSize`, `packUnit`,
`priceUnit` ve `trip_line.unit`'in **hiçbirini** okumuyor.

Satır seviyesinde bu koruma **var ve çalışıyor** — `PriceHintMapping` ambalaj
değişiminde trendi bastırıyor ve KDoc'u *"trend dalı önce seçilseydi yeşil bir
aşağı ok çizerdi ve kullanıcıya gerçeğin tersini söylerdi"* diyor. Bir
üstteki tahmin satırının bu korumaların **hiçbiri yok**: ürün satırı *"ambalaj
değişti, karşılaştırma yapmıyorum"* derken tahmin aynı iki sayıyı sessizce
çarpıyor.

Somut vaka (kullanıcının kendi listesi): `3 kg Yoğurt`, son gözlem `192,00 TL`
— bir **3 kg'lık kova**. Bugün 3 × 192,00 = **576,00 TL** hesaplanıyor;
doğrusu 192,00.

`priceUnit` kolonu ayrıca **hiç yazılmıyor** — tanımı ve bir test yorumu
dışında sıfır referansı var, oysa kendi KDoc'u *"bu kolon olmadan
`unitPriceMinor` tek başına anlamsızdı"* diyor.

⚠ **Bu, «markete göre tahmin» tartışmasının ön koşulu** (`docs/30`): yanlış
markete göre yanlış bir çarpım, doğru markete göre yanlış bir çarpımdan iyi
değil.

Hiçbiri dış veriye bağlı değil; hepsi bugünkü şema ve bugünkü verinin üstünde
yazılabilir.


**Faz E'den kalan tek madde.** Etiket çekme ekranı bugün yalnızca dikey düzeni
tanıyor; yatay düzen sözleşmede yazılı ama kodda karşılığı yok
(`Gezinme Sozlesmesi:587`). Bu kutu kapandığında Faz E tamamen kapanır.

#### F6.5 — Sabit terfisi + bastırma <a id="f65"></a>

**✅ (bastırma yarısı).** Ürün Detayı'ndaki *"Bunu önerme"* anahtarı, motorun
dördüncü kuralı (engelli ürün önerilmez — süzgeç **motorda**, yani her iki
yüzeyi birden susturuyor) ve Ayarlar'daki *"Önerilmeyenler · [Geri al]"*
bölümü. **Migrasyon gerekmedi:** `suggestion_block` v5'ten beri şemadaydı,
eksik olan yalnızca DAO'suydu. Cihazda uçtan uca doğrulandı.
*Tasarımın söyleyip de yapmadığımız son yüzey kapandı.*

**Kalan iki yarı, ikisi de ayrı iş:**

- **Üç vuruşta otomatik bastırma** — kaynağı `suggestion_event` ve o tabloya
  **yazan tek satır kod yok** (DAO'su, `@Insert`'ü, accessor'ı hiç
  yazılmamış). Ayrıca sorgusu `(householdId, productId, outcome)` indeksi
  istiyor, tablonun `indices` listesi boş → **v6 bump'ı** gerekiyor. Tablo
  boşken şema hatası bedava (bkz. §4 Şema kuralı).
- **Sabit terfisi** — tasarıma soruldu (`docs/28`). İki tasarım dosyası
  çelişiyor ve kodun kendi KDoc'u otomatiği yasaklıyor: *"kullanıcı işaretler,
  motor değil"*.

#### F2.7 ✅ — Katalog yeniden tohumlanabilir oldu <a id="f27"></a>

**Kapandı (23 Ağustos).** Kapı artık `SELECT COUNT(*) FROM category > 0` değil
`app_settings.catalogSeedVersion`. Gömülü sabit (`CATALOG_SEED_VERSION`)
büyükse katalog **üzerine yazılıyor** — silinmiyor, çünkü `product.categoryId`
kategorilere bakıyor ve aradaki o anda kullanıcının ürünleri sahipsiz kalırdı.
`null` damga = "bilinmiyor, yeniden yaz", yani v6 öncesi kurulumların kataloğu
ilk açılışta tazeleniyor. Cihazda doğrulandı.

⚠ **Bir sessiz hata daha yakalandı:** damga satırını açan `INSERT OR IGNORE`
yalnızca `householdId` veriyordu, oysa `syncPhotos` ve `createdAt` NOT NULL —
`OR IGNORE` o ihlali **yutuyordu**, satır hiç doğmuyor, damga hiç düşmüyor ve
katalog her açılışta baştan yazılıyordu. Hiçbir şey patlamadan.

#### ~~F2.7 — eski hâli~~ <a id="f27-eski"></a>

*(cihazsız)* F0.4 ve F3.9'u açar.

**Sessiz kullanıcı hatası:** `seedCatalog` `SELECT COUNT(*) FROM category > 0`
ile kapıda duruyor; ilk açılıştan sonra `CatalogSeedData` değişiklikleri o
telefona hiç ulaşmıyor.

> Bu madde eskiden "Öncelik 2 — Dış veri" altında bekliyordu ve bu yanlıştı:
> tohum tamamen yereldir, marketfiyati ile hiçbir bağı yok. Dış veri onu
> *kullanacak*, gerektirmiyor.

#### F3.9 — "Diğer" kategorisi <a id="f39"></a>

F2.7'ye bağlı — yeni kategori ancak katalog yeniden tohumlanabildiğinde
mevcut telefonlara ulaşır. F2.7 gibi bu da yerel bir iş; "Dış veri" başlığı
altında beklemesi yanlıştı.

#### F6.9 — `kategori-tonlari` TODO'su silinecek <a id="f69"></a>

F6.9 adımının kendisi kapandı (§6) ama `CategoryTile.kt:34`'teki
`TODO(kategori-tonlari)` kodda duruyor. Çalışma sözleşmesi bunu açıkça
yasaklıyor: *"bir TODO kapandığında hem koddan silinir hem burada
işaretlenir."* Tek satırlık iş, ama sözleşmenin kendi kuralı.

#### F10.7 + F10.11 + F11.4 — Ölü kod, ölü token, ölü primitifler <a id="olu-kod"></a>

**Üç madde tek maddeye birleştirildi.** Ayrı dururken `focusRing` üç farklı
kaderle üç yerde geçiyordu — F10.7 *"bağla"*, F10.11 *"sil"*, F11.4
*"tasarıma sor"* — ve aynı şey `SafeArea`, `AccentStrip`, `storeDisplayName`
için de olmuştu. Bir isim için üç plan, plan değildir.

**Zaten silinenler:** `ui/screens/Placeholders.kt` (86 satır, hiçbir dosya
import etmiyordu), `ListScreen`'deki altı FileKit import'u,
`formatDayMonthTime`, `NeydiExtraShapes.barTop`, katalogdan `coil` /
`coil-compose`.

**Kalan adaylar — her isim için tek karar:**

| İsim | Bugünkü durum | Karar |
|---|---|---|
| `Modifier.focusRing` | Tanımlı, üretimde **sıfır çağıran** | **AÇIK SORU — soru henüz yazılmadı:** bağlanacak mı, silinecek mi? Odak halkası bir erişilebilirlik sözleşmesi; silmek tasarımın kararı olmalı, bizim değil. |
| `SafeArea` | Ölü | Silinecek. `SafeArea.top = 44.dp` F10.8'de bu listeye ait olmadığı anlaşılmıştı (güvenli alan boşluğu, dokunma hedefi değil) — ama o düzeltme boyutla ilgiliydi, `SafeArea`'nın kendisi hâlâ çağrılmıyor. |
| `AccentStrip` | Ölü | **Tasarıma bağlı:** amber şerit 3dp ve amber sözleşmesi 1.5dp kenarlık şart koşuyor, yani iki yandan kenarlık konunca iç dolgu 0dp kalıyor ve amber tamamen kayboluyor. Sorulacak. |
| `AccentSurface` | Ölü | **Listeye geri girdi.** Eskiden *"bu listeden çıktı, `AccentChip.kt:62` ve `:78`'den çağrılıyor"* yazıyordu; düzeltmenin kendisi yanlıştı — `AccentChip`'in de kendi dosyası dışında sıfır çağıranı var. |
| `AccentChip` | Ölü | **Listeye geri girdi**, aynı sebeple: repo genelinde kendi dosyası dışında sıfır çağıran. |
| `storeDisplayName` | Ölü — yalnız testi çağırıyor | Silinecek. *(F11.28 bunu "F10.11 listesinde duruyor" diye gönderiyordu; listede yoktu. Gönderme artık bu maddeye.)* |
| `parseMinorInput` | Üretimde çağıranı yok, yalnız kendi testi var | **Silinmiyor:** karar 73 onu onay kartından çıkardı, ama F5.4 dış veriyle geri dönebilir. |

#### F11.6 — Alışveriş modu satır container'ı <a id="f116"></a>

Alışveriş modundaki satırın kendi container'ı yok; plan modunun bileşeni
ödünç alınıyor.

#### F10.2 — Bottom sheet'leri Nav3 Scene'e taşı <a id="f102"></a>

*(F10.5 bağı düştü — `skipPartiallyExpanded` çözümü bu maddeyi beklemiyor.)*

#### F10.3 — `graph.json` takip kararı <a id="f103"></a>

`graphify-out/graph.json` sürüm kontrolünde tutulacak mı, yoksa üretilmiş
çıktı olarak `.gitignore`'a mı düşecek?

#### Tasarım cevabı bekleyen sorular <a id="tasarim"></a>

Kod tarafında yapılacak bir şey yok; ikisi de tasarımın kalemini bekliyor.

- **`docs/27` — on ikinci tur, dört soru.** En görünür olanı: yoğurt satırı
  hâlâ `↑ %88` yazıyor. Trendin `null` kuralı **tasarımın kuralı**, tek
  taraflı gevşetilmedi. F5.5'in ambalaj şartını tasarımdakinden **katı**
  tutmamızın gerekçesi de bu dosyada.
- **`docs/28` — on üçüncü tur, on üç soru.** İçinde F6.5'in sabit terfisi ve
  ⚠ `focusRing` / `AccentStrip` bu turlarda **sorulmadı**; sorunun kendisi
  hâlâ yazılacak iş.

### 3.3 Kodlanacak — dış veri

Bu bölümde **yalnızca iki madde** var, ve ikisi de gerçekten ağ istiyor.
Eskiden burada bekleyen F2.7, F3.9 ve F5.5 yerel işlerdi — §3.2'ye taşındı.

#### F0.4 — Kanonik ürün kimliği <a id="f04"></a>

*(cihazsız)* marketfiyati fuzzy match; **F5.4'ü açar.**

#### F5.4 — marketfiyati entegrasyonu <a id="f54"></a>

`/api/v2/search`, `User-Agent` zorunlu, agresif cache. **Çevrimdışıysa blok
sessizce yok olur** — reyonda elleri dolu birine ağ hatası göstermek özelliği
zararlı yapar. Repoda `HttpClient` yok; ktor katalogda hazır ama bağımlılık
değil.

- **F5.5'in kalan yarısı buraya bağlı.** Çipin yerel yarısı bitti (§6):
  kullanıcının iki zincirde çektiği her ürün karşılaştırmayı zaten kendi
  verisinden kuruyor. Kalan yarı, kullanıcının **hiç etiket çekmediği**
  zincirler: karşı gözlem marketfiyati'nden gelecek.

### 3.4 Saha ölçümü

Kod yazılarak kapanmayan, telefonu alıp markete gitmeyi ya da eldeki 99
fikstürü yeniden koşturmayı gerektiren işler.

#### F4.7 — Alias sahada doğrulanacak <a id="f47"></a>

Etiket metni → ürün eşlemesi bağlandı; **ikinci çekimde ürünün kendiliğinden
dolduğu** görülmeli.

⚠ Bugüne kadar doğrulanamamasının sebebi ölçüldü: `product_alias` tablosu
**0 satır**. Çekimler 16:48–17:00 arasında yapıldı, F4.7'yi taşıyan kurulum
18:35'te — yani **alias yazan kod bu telefonda hiç koşmadı**. Elde 12 gözlem
olması yanıltıcı; hiçbiri alias yolundan geçmedi. Bu build'le atılacak çekim,
F4.7'nin ilk gerçek denemesi olacak.

#### Marka okuma kalitesi ölçümü <a id="marka"></a>

Bugünkü turda marka bazen çöp geldi (`CE UZ`, `BAlkon`, `BILI BIL`); ölçüm
dökümü **açık** ve sonraki çekimler kaydediliyor.

⚠ Ama bu iş **yeni bir tur beklemek zorunda değil**: elde 99 gerçek fikstür
var ve marka okuyucusu onların üstünde bugün koşturulabilir. Ölçüm önce,
düzeltme sonra — `readTagName`'in marka dalı da tam olarak böyle yazılmıştı.

#### Geçmiş grafiği için üç yeni gezi <a id="gezi"></a>

E18'in üç yüzeyi (başlık alt satırı, Geçmiş satırı, özet kartı manşeti) ve
Geçmiş grafiği bugün cihazda boş.

⚠ **Sebebi eksik gezi değil, tarih sırası.** On iki kapanmış gezi var ama
`observeTripEstimates` sıfır satır dönüyor: bütün gözlemler 22 Ağustos'ta
yazıldı, geziler 15–16 Ağustos'ta kapandı ve sorgu `observedAt <= completedAt`
istiyor. Bu şart E18'in kendi doğrusu ve doğru — geçmiş gezinin tutarı O
GÜNKÜ fiyattan olmalı. Yani gereken şey **"3 gezi" değil, "bugünden sonra
kapatılmış 3 gezi"**.

Aynı sebebin ikinci yüzü: beş sabitin `medianIntervalDays` değeri **0**, çünkü
on iki gezi aynı gün kapandı. "Bitmiş olabilir" önerisi bu yüzden hiç
ateşleyemiyor ve gereken şey **en az üç FARKLI güne yayılmış gezi**.

#### Yeni bir zincirin grameri nasıl ölçülür

Gramer **fotoğrafa bakarak yazılamaz** — kurallar metnin değil GEOMETRİSİNİN
üstünde duruyor (`docs/18`). Ama OCR o fotoğrafların **üstünde koşturulabilir**
ve ölçüm aynen elde edilir; A101 grameri (`docs/24`) tam olarak böyle yazıldı.

`TagOcrDump.kt` iki yol sunuyor, ikisi de **işaret dosyasına** bağlı:

```bash
adb shell run-as com.neydi.app mkdir -p files/tags-dump
adb shell "run-as com.neydi.app sh -c 'echo x > files/tags-dump/ENABLE'"
```

- **Canlı çekim** — kullanıcı normal çeker, döküm her karede yazılır.
- **Hazır fotoğraf** — `files/tags-dump/in/` altına kopyalanan `.jpg`'ler
  Etiket çek ekranı açılınca aynı ML Kit yolundan geçirilir
  (`dumpImportedPhotos`). İkinci bir market turu gerekmez.

Dökümler `files/tags-dump/*.kt.txt` olarak birikir ve doğrudan `TagFixtures`'a
yapıştırılır. **Ölçüm bitince işaret dosyası silinir** — bu bir teşhis, ürün
özelliği değil.

#### Zincir önceliği *(kullanıcı verdi, 18 Ağustos)*

Sıra kullanıcının **gerçekten gittiği** marketlere göre; listede olmayanlar
için gramer **yazılmayacak** (Metro, CarrefourSA, File dahil).

> **Görünür çelişki, tek cümlelik cevabı:** E13'ün tohumu CarrefourSA ve
> File'ı **mağaza** olarak yaratıyor ama gramerleri yazılmayacak — çünkü tohum
> **market seçicinin** listesi, gramer listesi değil: kullanıcı oralarda
> çektiği etiketin fiyatını elle yazar, mağaza yine doğru kaydedilir.

| # | Zincir | Gramer | Fotoğraf | Not |
|---|---|---|---|---|
| 1 | **FullGross** | ❌ | ❌ **yok** | toptancı — Metro sınıfı, en zoru |
| 2 | **Gimat** | ❌ | ❌ **yok** | toptancı — Metro sınıfı |
| 3 | **BİM** | ✅ | ✅ 27 | 24/27 fiyat, sıfır yanlış |
| 4 | **A101** | ✅ | ✅ 19 | 15/19 fiyat, sıfır yanlış (`docs/24`) |
| 5 | **ŞOK** | ❌ | ❌ **yok** | BİM sınıfı olabilir |
| 6 | **Tarım Kredi** | ❌ | ❌ **yok** | |
| 7 | **Migros** | ✅ | ✅ 19 | 16/19 fiyat, sıfır yanlış |
| — | **Metro** | ❌ | ✅ 34 | **ölçüldü (34 etiket), bilinçli ertelendi** — gerekçe aşağıda |

**Yedi zincirin dördü fotoğraf bekliyor.** Görmediğim bir etiketin gramerini
yazamam — E14'ün kuralları 27 BİM etiketinden çıkarıldı ve 53 Metro/Migros
etiketi onların BİM'e özel olduğunu gösterdi (`docs/18`). Tahminle yazılmış
bir gramer, ölçülmüş bir gramerin verdiği güveni vermez.

**İlk iki sıra aynı anda en değerli ve en zor.** Toplu alışveriş fiyat
geçmişinin en çok işe yaradığı yer, ama toptancı etiketi Metro'da ölçüldüğü
gibi davranıyorsa (34 etiketin 23'ünde fiyat "bulunuyor", yalnızca 14'ünde
kuruş gerçekten okunuyor, üçü ispatlanabilir şekilde yanlış) oradaki kural
"okuyamadım" demeyi öğrenmek zorunda. O iki markette bugün kart **boş
açılıyor** ve fiyatı kullanıcı yazıyor — yanlış değil, eksik.

**Açık soru elden geçirmeye kalıyor:** yedi zincir için yedi gramer
sürdürülebilir mi, yoksa "OCR yalnızca emin olduğunda doldursun, gerisini
kullanıcı yazsın" tek akışı mı? Migros'ta ürün adı için bu karar zaten
verildi — yarısı doğru bir ad, hiç ad olmamasından kötüydü.

**Bilinçli olarak ertelenen:** Metro grameri. Ölçüldü ve yazılmadı — önerilen
kural 34 etiketin 23'ünde fiyat veriyor ama yalnızca 14'ünde kuruş gerçekten
okunuyor, üçü etiketin kendi birim fiyatına karşı ispatlanabilir şekilde
yanlış. Ayrıca çapraz kontrol Metro'da hiç çalışmıyor: `readTagUnitPrice`
34 etiketin 0'ında sonuç veriyor, çünkü Metro birim sözcüğünü ve sayıyı ayrı
OCR parçalarına basıyor. Dürüst hâli *"kuruş ölçülmediyse reddet"* (~14/34).
Ölçüm [`18-zincir-karsilastirmasi.md`](18-zincir-karsilastirmasi.md).

**Tasarım cevapladı** (16 Ağu): karar defteri 20 maddeye indi, iki yeni dosya
geldi (gezinme sözleşmesi + ikonografi). Kararların kod karşılığı ve kalan beş
soru: [`11-tasarim-kararlari.md`](11-tasarim-kararlari.md). E15 ve E17'nin
spesifikasyonu artık tam — eşikler, geri sırası, hata yolları dahil.

### 3.5 Büyük fazlar

#### Faz 7 — Senkron <a id="faz7"></a>

(7.1 Supabase+RLS · 7.2 Auth · 7.3 Realtime v1 · 7.4 `updated_at` ·
7.5 outbox+tombstone+add-beats-remove · 7.6 keep-alive).

**Supabase projesi açıldı:** `vjinflzmjcsaicaeatic`, **eu-central-1**
(Frankfurt — bölge sonradan değiştirilemiyor), ücretsiz plan. Şema **boş**,
hiçbir migration uygulanmadı.

**Tasarım turu bitti** → [`15-faz7-sema-plani.md`](15-faz7-sema-plani.md):
üç bağımsız öneri, her biri ayrı yargıçla çürütülmeye çalışıldı, **üçünün de
doğruluk puanı düşük çıktı** (4 · 5 · 3) — hiçbiri olduğu gibi uygulanabilir
değildi. Yirmi beş ölümcül kusur kayıtlı.

⚠ **En ağırı mimari:** iki kişi çevrimdışıyken aynı geziye aynı ürünü eklerse
`(tripId, productId)` UNIQUE çakışıyor, ikinci push 23505 alıyor ve
`pending_op` FIFO olduğu için **outbox kalıcı olarak tıkanıyor** — hem de
uygulamanın en olası eşzamanlı eylemi bu. Çözüm yönü doğal anahtardan
türetilen deterministik id, yani çakışma red değil upsert olur. F7.5'in
"add-beats-remove"u zaten oraya işaret ediyordu.

`syncPhotos` kolonu **ölü** (karar 29 fotoğrafı siliyor); sunucuya taşınmıyor,
yerelden de düşürülebilir.

**F5.10'un kalan yarısı burada:** "eşitlemede aynı dakika = tek gözlem"
mükerrer koruması senkron motoru gelince yazılacak; bugün `pending_op`'a yazan
kod yok.

#### Faz 8 — Marka varlıkları <a id="faz8"></a>

(8.1–8.6). ⚠ Logo konsepti **C ("Fişin Kuyruğu") elenmeli** — fiş artık ürünün
parçası değil.

#### Faz 9 — iOS <a id="faz9"></a>

(9.1 kabuk · 9.2 **etiket hattı**: `downscaleForOcr` + `readTag` actual'ları ·
9.3 status bar · 9.4 gerçek cihaz · 9.5 TestFlight). Mac gerektirir.

---

## 4. Kalıcı kurallar ve dersler

*Pivottan bağımsız; fazlar bitince de silinmez.*

### Şema kuralı

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

### Altı sessiz hata sınıfı

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

### Riskler

- **Ölçek riski YOK ve bu bir karar** — iki kişilik hane, elli gezi
- **Türkçe yerelleştirme değil, doğruluk kısıtı**
- **Takvim günü ≠ 86.4M ms bloğu**
- **⚠ Yeni: etiket çekim yükü.** Değer eğrisi artık "kullanıcı kaç etiket
  çekerse ayakta kalır" sorusuna bağlı. E15'in seri çekim akışı bu riskin ilk
  cevabı; ölçümü Faz 7 öncesi yapılmalı.

### Açık kararlar

1. **Fiyat gözlem birimi** — paket mi kg mı? E14 `priceUnit` ile ikisini de
   taşıyor; *gösterim* kararı E17'de.
2. **Katalog fiyatı ile gözlem aynı tabloda mı** (F5.4).
3. ~~**Blok listesi olay mı tablo mu** (F6.5)~~ — **kapandı: tablo.** Gerekçesi
   `Suggestion.kt:61-77` KDoc'unda yazılı. *(Olay tablosu, `suggestion_event`,
   ayrı bir iş olarak F6.5'in otomatik bastırma yarısında duruyor — bastırmanın
   kendisi tabloyla çözüldü.)*
4. **Hane yeniden anahtarlama** (Faz 7).
5. ~~Etiket fotoğrafı kayıttan sonra silinsin mi~~ — **kapandı**: karar 29
   evet diyor, gerekçesi de aynı (etiket ödeme kanıtı değil, fiyatın okunduğu
   an). Hiçbir yüzey fotoğraf çizmiyor.

### Bayat adlar — harita ≠ kod

| Dokümanda | Kodda |
|---|---|
| fiş çekme akışı | `TagCapture` *(E15'ten sonra)* |
| `ReceiptReader` | `readTag` *(E14'ten sonra)* |
| Fiş Kontrol | yok — onay kartı `TagCapture`'ın içinde |
| `attachReceiptToTrip` | yok — gözlem geziye bağlanmıyor (pivot karar 3) |
| `ListeEkrani` / `kurusFormatla` | `ListScreen` / `formatMinor` |

### Kod TODO eşlemesi

Kodda bugün duran TODO'lar (`grep -rn "TODO(" composeApp/src`):

| TODO | Kapatan adım |
|---|---|
| `tnum` | F9.4 |
| `kategori-tonlari` | F6.9 *(adım kapandı, TODO hâlâ `CategoryTile.kt:34`'te — §1'de 18. sıra)* |
| `ios` · `ios-statusbar` | F9.1 · F9.3 |

`sheet-yuksekligi` ✅ silindi (F10.5). `splash` kodda yok — F8.4 geldiğinde
yazılacak.

---

## 5. İlgili dokümanlar

| Dosya | Ne işe yarar |
|---|---|
| [11-tasarim-kararlari.md](11-tasarim-kararlari.md) | **Aktif** — 56 kararın kod durumu (46–75 dahil), gezinme sözleşmesi sabitleri, ikonografi |
| [27-tasarima-sorular-12.md](27-tasarima-sorular-12.md) | **AÇIK** — on ikinci tur, dört soru; F5.5'in katı ambalaj şartının gerekçesi de burada |
| [28-tasarima-sorular-13.md](28-tasarima-sorular-13.md) | **AÇIK** — on üçüncü tur, on üç soru; F6.5 sabit terfisi ve ölü primitiflerin kaderi |
| [17](17-e12-etiket-olcumu.md) · [18](18-zincir-karsilastirmasi.md) · [24](24-a101-olcumu.md) | **Etiket ölçüm raporları** — BİM · üç zincir karşılaştırması · A101 |
| [15-faz7-sema-plani.md](15-faz7-sema-plani.md) | **Faz 7 şema planı** — üç öneri, yirmi beş ölümcül kusur; senkron başlarken okunacak |
| [19-tasarim-denetimi-girdileri.md](19-tasarim-denetimi-girdileri.md) | 56 ajanlı tasarım denetiminin girdileri |
| [21](21-tasarim-denetimi-38-kalan.md) · [22](22-tasarima-sorular-8.md) · [23](23-tasarima-sorular-9.md) | Sekizinci/dokuzuncu tur denetim ve sorular |
| [25-tasarima-sorular-10.md](25-tasarima-sorular-10.md) | Onuncu tur soruları — **cevaplandı**, kararlar 70–74 (`docs/11`) |
| [26-tasarima-sorular-11.md](26-tasarima-sorular-11.md) | On birinci tur — **cevaplandı**, karar 75 (`docs/11`) |
| [20-tasarima-sorular-7.md](20-tasarima-sorular-7.md) | Yedinci tur — cevaplandı; tasarım projesi bu turda baştan üretildi |
| [16-tur6-cikarim.md](16-tur6-cikarim.md) | Altıncı turun çıkarımı — kararların koda nasıl indiği |
| [13-tasarima-sorular-5.md](13-tasarima-sorular-5.md) · [14-tasarima-sorular-6.md](14-tasarima-sorular-6.md) | Beşinci/altıncı tur — cevaplandı, arşiv değeri |
| [12-tasarima-sorular-4.md](12-tasarima-sorular-4.md) | Dördüncü tur — cevaplandı, arşiv değeri |
| [10-tasarima-pivot.md](10-tasarima-pivot.md) | Tasarıma pivot bildirimi — cevaplandı, arşiv değeri |
| [ARSIV-fis-donemi.md](ARSIV-fis-donemi.md) | Pivottan önceki tam harita; F-numaralarının kaynağı |
| [01-claude-design-prompt.md](01-claude-design-prompt.md) | Sekiz ekranın özgün spesifikasyonu |
| [05](05-tasarim-denetimi.md) · [06](06-tasarima-sorular.md) · [07](07-tasarima-sorular-2.md) · [08](08-tasarim-bulgulari.md) · [09](09-tasarima-sorular-3.md) | Önceki tasarım turları — fiş dönemi, arşiv değeri |
| [03-arastirma-bulgulari.md](03-arastirma-bulgulari.md) | ⚠ Fiş iddiaları geçersiz, başında arşiv notu var |
| [00-isim-onerileri.md](00-isim-onerileri.md) · [02-logo-splash-prompt.md](02-logo-splash-prompt.md) | İsim analizi · logo/splash promptları |
| `tasarim/` | Ekran tasarımları, karar defteri, devir paketi |

---

## 6. Kapanmış iş arşivi

Buradaki hiçbir maddenin kutusu yok — kapanmış işin kaydı, açık iş değil.
Gerekçeler bilerek kısaltılmadı: kararın *neden* öyle verildiği, kararın
kendisinden daha uzun ömürlü.

Pivottan önceki fazların (fiş dönemi) tam anlatısı bu dosyada değil:
[ARSIV-fis-donemi.md](ARSIV-fis-donemi.md).

### 6.1 Faz E — Fişten Etikete *(kapandı — 22 Ağu 2026)*

Her adım bir commit/PR; her adımdan sonra uygulama derlendi ve kuruldu.
On dokuz adımın **on sekizi** kapandı; E15'in yatay düzeni açık iş olarak
§3.2'de duruyor.

#### ~~E-A · Kurtarma~~ ✅

Fiş silinmeden önce, fişe ait olmayan parçaları çıkarma işi.

- ✅ **~~E1 — Yol haritası, arşiv, README, tasarım bildirimi~~** *(cihazsız)*
- ✅ **~~E2 — Para/rakam kurtarma~~** — `parseMinor`→`Money.kt`,
      `normalizeDigits`→`data/ocr/`, `normalizeUnit`→`QuantityParser.kt`
- ✅ **~~E3 — Dosya/görsel kurtarma~~** — `data/image/` (EXIF dersiyle),
      `VisualRows`→`data/ocr/`
- ✅ **~~E4 — Mağaza adı kurtarma~~** — `chainKey`+`storeDisplayName`→`data/store/`

#### ~~E-B · Yıkım~~ ✅

- ✅ **~~E5 — Fiş Kontrol~~** (1.612 satır) · ✅ **~~E6 — Çekim akışı + tarayıcı
      bağımlılığı~~** · ✅ **~~E7 — ListViewModel'in arka plan OCR'ı~~**
- ✅ **~~E8 — Geçmiş gezi düzeyine indi~~** (498→215 satır)
- ✅ **~~E9+E10 — Repository fişsiz; `purchaseEvents` tek kaynak~~**
- ✅ **~~E11 — `data/receipt/` + şema v4→v5~~** — cihazda `pm clear`'sız doğrulandı

#### ~~E-C · Etiket akışı~~ ✅

##### ▸ ~~E12 — Etiket ölçümü~~ ✅ *(27 gerçek BİM etiketi)*

Rapor: [`17-e12-etiket-olcumu.md`](17-e12-etiket-olcumu.md). Ham OCR çıktısı
`composeApp/src/commonTest/etiket-fikstur/` altında, 27/27 okundu, sıfır hata.

**Soru 1 — kuruş üstsimgesi tek parça mı iki parça mı?** İkisi de değil:
**21/27'de hiç okunmuyor.** 6 etikette lira ayırıcıyla bitişik geliyor (`74,`),
yalnız 1 etikette kuruş ayrı parça olarak bulundu (`501` ← `50`), birinde
kuruş **derece işaretine** dönüşmüş (`2,°`).
⚠ **`parseMinor` bu 27 etiketin hiçbirinin manşet fiyatını okuyamıyor** — tam
iki ondalık hane şart koşuyor, etiket `74,` veriyor. E14'ün *"para desenine
uyanlar arasından en büyük glifli"* kuralı bugünkü hâliyle **hiçbir şey
seçmiyor**.

**Soru 2 — yön düzeltmesi gerekiyor mu?** Evet, zorunlu. 26/27 fotoğraf EXIF=6
(telefon dikey tutulmuş) ve `downscaleForOcr` onları döndürüyor: kaynak
4032×3024 → OCR'a giren 3024×4032. Düzeltme olmasaydı 26 fotoğraf ML Kit'e
**yan** girecekti. Ölçek hiç devreye girmedi (4032 < 4096), yani yönün
ölçekten bağımsız işlenmesi tam da bu yüzden önemliydi.

**Beklenmeyen bulgu:** "en büyük glif = fiyat" kuralı **6/27'de yanılıyor** —
aktüel etiketlerde marka adı fiyattan büyük basılıyor (`Krena` h=1032,
`Kar` h=1244).

**Dördüncü bulgu:** birim fiyat satırı etiketteki **en temiz sayı** —
10 etikette iki ondalık hanesiyle okundu ve `parseMinor` orada **çalışıyor**.
Bir etikette manşet fiyata eşit, yani doğrulama kaynağı olabilir.

**Eksik:** Metro (toptan) örneği ve kasıtlı 90°/eğik kontrol karesi yok.
İkincisi `VisualRows`'un köşe sıralaması sözleşmesini kapatabilirdi — bu 27
fotoğrafta metin dik olduğu için iki okuma çakışıyor.

##### ▸ ~~E13 — Mağaza tohumu~~ ✅ *(cihazda doğrulandı)*

- Bootstrap'te 7 zincir: **BİM · A101 · ŞOK · Migros · CarrefourSA · File ·
  Tarım Kredi** — `chainKey` ile `chain`, `insert` IGNORE (idempotent)
- `PriceObservationDao.lastUsedStoreId(householdId)` — yapışkan seçicinin
  varsayılanı; **şema değişikliği yok**, son gözlemin marketi okunuyor
- `StoreDao.findByChain` zaten var, çağıranı E13'te geldi
- `StoreDao` KDoc'undaki karar-11 metni revize edildi

**Bitti sayıldı:** temiz kurulumda Ayarlar → Mağazalar'da 7 market görünüyor. ✅
Tohum **zincir-farkında**: başka yoldan gelmiş aynı zinciri ikinci kez
yaratmıyor, önce gelen kazanıyor.

> ⚠ **Cihazda fiş dönemi çöpü bulundu.** Eski `rememberStore` künyeden okuduğu
> her şeyi mağaza yazmış: `Kg`, `KDV`, `Term:`, `Adet`, `Kq`, `ECioREM`,
> `RSALIYE`, `(BTECH)`, `Ae`, `DD`… test cihazında 17 satır. Bunlar Ayarlar'ın
> "Takip edilen zincirler" satırını okunamaz yapıyordu. Kod hatası değil, veri
> kalıntısı — F10.17'de temizlendi.

##### ▸ ~~E14 — TagReader + TagParser~~ ✅

**Yapıldı:**
- `readTag` — tek bitmap, tek ML Kit çağrısı. Şerit/yön oylaması/mükerrer eleme
  yok; o makine metrelik fiş içindi. Android actual yazıldı, iOS **F9.2**'ye
  bırakıldı (patlıyor, sessizce boş dönmüyor).
- **`readTagPrice` — ayrı bir etiket fiyatı okuyucusu.** `parseMinor`
  kullanmıyor ve sebebi ölçüldü: E12'de **27 gerçek etiketin hiçbirinin**
  manşet fiyatı ondan geçmiyor (tam iki ondalık hane şartı; etiket `74,`
  veriyor). O kural fişin kendi doğrusu ve orada geçerli — iki yüzeyin iki
  doğrusunu tek fonksiyona bindirmek ikisini de zayıflatırdı.
  - Lira = **rakamla başlayan** en büyük glifli satır. "Para desenine uyanlar"
    değil, çünkü o süzgeç kümeyi önce boşaltıyor. Yan fayda: 6 etikette en
    büyük glif marka adı (`Kar` 1244px) ve "rakamla başlar" onları eliyor.
  - Kuruş = **iki rakam + en fazla bir çöp karakter** (`50t` `90%` `501` —
    ₺ simgesi `t`/`%`/`1` diye okunuyor), liranın sağ-üst bandında, ondan
    küçük. **Ayırıcı taşıyan aday reddediliyor**: `89,s6` üstü çizili eski
    fiyat ve kuruştan BÜYÜK glifli, `82:` saat parçası.
  - **26/27 etikette lira, 11/27'de kuruş** okundu. Kuruş okunmadıysa `,00`
    varsayılıyor ama **işaretleniyor** (`kurusFromOcr = false`) — onay kartı
    o bayrağa bakıp fiyat alanına odaklanıyor (E15).
- `readTagUnitPrice` — birim fiyat satırı `parseMinor` ile okunuyor; orada iki
  ondalık hane gerçekten var (normal punto). Birim sözcüğü şart: `74,50t`
  gibi sözcüksüz satır null dönüyor, çünkü "1 KG" ayrı bir satır ve ikisini
  birleştirmek tahmin olurdu.
- Fikstür `TagFixtures.kt` olarak **üretildi** (elle yazılmadı); ham dökümler
  `commonTest/etiket-fikstur/` altında ve her sayı orada doğrulanabilir.
- **`readTagName` + `readTagPack` — ad, marka önerisi ve gramaj.** Etiket
  **kolonlu**: ad solda bir blok, fiyat sağda tek parça, künye altta. Bloğu
  bitiren satır gramaj; marka bloğun ilk satırı ve yalnızca **öneri**
  (karar 39 — manavda marka yok).
  - **26/27'de ad, 23/27'de gramaj** okundu. 22 etikette marka+ad **tam
    doğru**. Eksik dördünün her birinin gerekçesi testte yazılı: `53-62 G`
    aralık (bilinçli red), bulanık çekim, aktüel etiket düzeni, gramajı
    olmayan etiket (`12Lİ` adet çarpanı).
  - Süzülenler: mağaza kodu (`P728`, 27 etikette de var, hiçbirinde ad değil),
    raf adedi (`X 34 Adet`). **Paket çarpanı süzülmüyor** — `12Lİ` adın
    parçası; ayıran tek işaret baştaki `X`.
  - `groupVisualRows` **kullanılmadı** ve bu plan sapması bilinçli: fişte ad
    ile tutar aynı görsel satırın iki ucuydu, etikette değil. Gerekçe
    `TagFieldReader.kt` KDoc'unda.
- **Bulanık çekim artık fiyat da uydurmuyor.** `readTagPrice` `183808` için
  `86 TL` dönüyordu — 12 piksellik bir gürültü parçası. Eski test bunu gevşek
  bir koşulla tolere ediyordu (`if (price != null) …`), yani önlemesi gereken
  şeyi geçiriyordu. İki okuyucu artık paylaşılan `MIN_LIRA_RATIO` eşiğini
  kullanıyor: manşet, kaynak yüksekliğin %2'sinden küçükse okuma yok.

**Kendi hatam, kayda geçsin:** ad bloğundaki gürültüyü "raf tabelası ad
satırından on kat büyük" diye **yükseklik eşiğine** bağlamıştım. Test ısırması
yanlışladı — eşiği kaldırdım, tabela testleri ayakta kaldı. Gerçek sebep
tabelanın etiketin **bütün genişliğini** kaplaması (`Krena` x=132..3060, lira
x=1979), yani kolon süzgeci onu zaten eliyor. Eşiğin tek gerçek işi bulanık
çekimi düşürmekti ve onu da *kazara* yapıyordu (medyan yükseklik negatif
çıkıyor, çarpım daha da negatif oluyor). Doğru sonuç, tesadüfi sebep — eşik
silindi, yerine ölçülmüş bir kural kondu.

##### ▸ ~~E15 — TagCapture ekranı~~ ✅ *(pivotun canlandığı adım)*

- `TagCapture` nav key — **parametresiz** (çekim geziden bağımsız)
- Kamera durumu: `CameraSurface` + `CaptureController` (E6'dan beri hazır
  bekliyordu), etiket oranında çerçeve rehberi
- Onay kartı **fotoğrafın üstünde**, dışına dokunmak kapatmıyor (karar 25):
  **Fiyat** (düzenlenebilir — "elle fiyat girilmez" kuralının tek istisnası) ·
  **Ürün** (alias çözüyorsa sıfır soru) · **Marka** (kesik çerçeve = tahmin) ·
  **Market** (yapışkan) · **Tarih** (bugün)
- Eksik alan **amber şerit + tek cümle** ("fiyat okunamadı — yaz"); birden çok
  alan boşsa **yalnızca ilki** vurgulanıyor
- Kart hemen açılıyor; OCR gecikirse alanlar **iskelet**, kart beklemiyor
- Kaydet → alias + `PriceObservation` → fotoğraf silinir (karar 29) → kamera
  **300 ms** içinde hazır → toast 2 sn. Kaydet sırasında geri = kayıt tamamlanır
- Aynı market+ürün+fiyat **60 sn** içinde tekrarlanırsa ikinci gözlem yazılmaz
- Giriş: **liste başlığında kalıcı kamera hedefi**, her iki modda (karar 27)
- Geri sırası: klavye → sheet → kart → menü → destinasyon → çıkış

**Durum (18 Ağustos, cihazda 12 etiket çekildikten sonra):**

| Madde | Durum |
|---|---|
| Parametresiz nav key | ✅ |
| `CameraSurface` + çerçeve rehberi | ✅ |
| Kart: Fiyat (düzenlenebilir) · Ürün · Market (yapışkan) | ✅ |
| Kart: Marka, kesik çerçeve | ✅ |
| Kart: Tarih | ✅ |
| Amber şerit, yalnızca ilk eksik alan | ✅ |
| Kaydet → gözlem → fotoğraf silinir | ✅ *(cihazda doğrulandı)* |
| **Kamera kayıttan sonra hazır** | ✅ *(seri çekim; ilk sürüm ekrandan çıkıyordu)* |
| Kaydet sırasında geri = kayıt tamamlanır | ✅ |
| Geri sırası: kart → destinasyon | ✅ |
| 60 sn mükerrer koruması | ✅ |
| Başlıkta kamera hedefi, iki modda | ✅ |
| OCR sırasında iskelet | ✅ **eşik yok ve olmamalı** — karar 62 1,5 sn eşiğini kaldırdı (ölçülen süre 1,15 sn, eşik hiç tetiklenmiyordu) |
| Kart fotoğrafın üstünde | ✅ `TagThumbnail` kırpılmış kareyi çiziyor (`TagThumbnail.kt:112`) |
| Yatay düzen | ❌ (`Gezinme Sozlesmesi:587`) — **açık iş, §3.2'ye taşındı** |

**Bitti sayıldı — ÖLÇÜT DÜZELTİLDİ.** Önce *"1 etiket → Tahmini sepet
görünüyor"* yazıyordu ve bu **ulaşılamazdı**: `BasketAndSummary.kt:244`
`MIN_PRICED_ITEMS = 3` ve gerekçesi kodda yazılı (*"~40 TL yazan bir satır, on
sekiz ürünlük bir sepetin yanında yanlış bir güven veriyor"*). Eşiği düşürüp
kutuyu yeşile boyamak yerine ölçüt ikiye ayrıldı:

- **Kapı A — bir gözlem yazıldı:** Ayarlar → Zincirler satırında o zincir
  soluktan normale döner. Sıfır kod; `observeStoreIdsWithObservations` zaten
  okuyor. ✅ *cihazda 12 gözlemle doğrulandı*
- **Kapı B — Tahmini sepet:** listede **üç** fiyatlı ürün gerekir. Bu E16'nın
  satır ipucuyla birlikte doğal olarak geldi.

##### ▸ ~~E16 — Satır fiyat ipucu~~ ✅ *(cihazda doğrulandı — 22 Ağu)*

`observeList` artık fiyat ipucunu da taşıyor: **iki correlated alt sorgu**
(son + önceki gözlem), son gözlemin **market join**'i, ve sparkline için
`group_concat` ile **son 8 fiyat**. Tek-SQL kuralı korundu — satır başına Flow
yok; yirmi satırlık listede yirmi Flow her gözlem yazımında yirmi yeniden
yayın üretirdi.

`toPriceHint` dört dalı da eşliyor ve **sıra bilinçli**: ambalaj kontrolü
trendin ÖNÜNDE. 900 gr → 800 gr aynı fiyata satılıyorsa bu düşüş değil gizli
zam; trend dalı önce seçilseydi yeşil aşağı ok çizip gerçeğin tersini
söylerdi. Ambalajlardan biri **bilinmiyorsa** değişim iddia edilmiyor —
`null` "aynı değil" değil "bilmiyorum" demek, ve etiketlerin çoğunda gramaj
okunamıyor (`docs/18`).

`now` **zorunlu parametre**: varsayılanı olsaydı bütün gözlemler "bugün"
görünür ve hiçbir şey patlamazdı.

**Bitti sayıldı:** 2 gözlemli ürünün satırında delta çipi çiziliyor. ✅

##### ▸ ~~E17 — Ekran 5 fiyat bölümü~~ ✅ *(cihazda doğrulandı — 22 Ağu)*

`history(householdId, productId, 9)` — mağaza adı **join**'den geliyor,
gözlemden değil: kullanıcı marketi yeniden adlandırırsa geçmiş de yeni adı
göstermeli. `LEFT JOIN`, çünkü marketi seçilmemiş gözlem de geçmişte
**görünmeli** — `INNER` olsaydı kullanıcının kendi kaydettiği çekimler
sessizce kaybolurdu.

**"Nerede ucuz" kimliği market + marka çifti** (karar 26): aynı marketten iki
marka **iki satır**. Yalnızca markete göre gruplamak *"BİM'de 100 TL"* derdi
ve hangi marka olduğunu söylemezdi — oysa fiyat farkının büyük kısmı marka
farkı. Her çift için **en son** fiyat, ortalama değil: soru *"şimdi nerede
ucuz"*, ortalama zam yapmış marketi ucuz göstermeye devam ederdi.

Eşikler ve her birinin engellediği şey:
- **Bölüm 2 market** — tek marketle "nerede ucuz" cevabı olmayan bir soru
- **Sparkline 3 gözlem** — iki nokta bir doğru parçası çizer ve olmayan bir
  trendi varmış gibi gösterir
- **Delta çipi 2 gözlem** — E16'da, satır tarafında

**Bitti sayıldı:** iki market + marka çifti çiziliyor. ✅ *İkinci zinciri
A101 verdi.*

##### ▸ ~~E18 — `~` tahminleri~~ ✅ *(cihazda doğrulandı — 22 Ağu)*

E8 ve E11'de bilerek boşa düşürülen **üç yer** geri geldi: başlık alt satırı,
Geçmiş satırı, özet kartı manşeti. Hepsi `formatEstimate` ile — **her zaman
tilde, hiç kuruş**: uygulamada kesin tutar diye bir veri yok ve iki ondalık
hane bir kesinlik iddiasıdır, tildenin söylediğini aynı satırda geri alır.

**Geçmiş gezinin tutarı O GÜNKÜ fiyattan.** `observeTripEstimates`
`observedAt <= completedAt` şartını taşıyor. Bugünkü fiyat kullanılsaydı geçen
ayın alışverişi her zamdan sonra biraz daha pahalı görünürdü — kullanıcının
hiç yaşamadığı bir tutar. Aktif sepet ise "en son fiyat" kullanmaya devam
ediyor, çünkü oradaki soru *"kasada ne ödeyeceğim"*.

**Eşik `EstimatedBasket` ile aynı sabit** (`MIN_PRICED_ITEMS = 3`): fiyatı
bilinen ürün sayısı altındaysa tutar **hiç yazılmıyor**. İki yerde iki farklı
sayı olsaydı aynı gezi listede tutarlı, başlıkta tutarsız görünürdü. Hiçbir
ürünün fiyatı yoksa gezi sorguda hiç görünmüyor — sıfır değil, yok.

**Bitti sayıldı:** başlık *"Son alışveriş: dün · ~642 TL"*. ✅

> ⚠ Bu şartın bugünkü veride yan etkisi var ve bu bir kusur değil: mevcut 12
> gezi 15–16 Ağustos'ta kapandı, gözlemler 22 Ağustos'ta yazıldı, yani sorgu
> sıfır satır dönüyor. Geçmiş grafiğinin dolması için **bugünden sonra** gezi
> kapatılması gerekiyor — §3.4'teki saha maddesi bu.

##### ▸ ~~E19 — Tasarım revizyonu~~ ✅

`10-tasarima-pivot.md`'nin cevabıyla karar defteri güncellendi, ölen ekranlar
silindi.

**Bitti sayıldı:** hiçbir tasarım dokümanı var olmayan bir ekranı anlatmıyor.

**Nasıl kapandı:** tasarım projesi yedinci ve dokuzuncu turlarda baştan üretildi;
dokuz `.dc.html` dosyasının hepsi repoda güncel (21 Ağustos). Fiş dönemine ait
her şey `ARSIV-fis-donemi.md`'de dondurulmuş ve oradan geri taşınmıyor. Karar
defteri 56 geçerli karar taşıyor ve **karar 63 kendi yazarı tarafından geri
alındı** — defter artık yalnızca bugün geçerli olanı anlatıyor.

*(F11.10 bu adıma devroldu ve burada kapandı.)*

### 6.2 Kapanmış F maddeleri

Numara sırasında. Numaralar **kimliktir, sıra değildir** — eski F-numaraları
PR geçmişi ve kod göndermeleri bozulmasın diye korunuyor.

- ✅ **~~F3.12 — Eklenen satır klavyenin altında kalıyordu~~** *(kullanıcı bildirdi)*.
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
- ✅ **~~F5.5 (yerel yarısı) — "Başka markette ucuz" çipi~~.** Karar 41'in
      dört kuralı da kodda: **hem %10 hem 5 TL**, **14 gün** (SQL penceresi),
      **liste başına en fazla 3** mutlak TL tasarrufuna göre sıralı, ve
      **çakışmada trend bastırılıyor**. Ambalaj şartı tasarımdakinden **katı**
      tutuldu (kanıt isteniyor, `null` yetmiyor) — gerekçesi
      `docs/27-tasarima-sorular-12.md`.
      ⚠ **Bu madde "Öncelik 2 — Dış veri" altında beklemekle vakit kaybetti.**
      Dış veriye bağlı olan şey çipin **kapsamı**, mekanizması değil:
      kullanıcının iki zincirde çektiği her ürün karşılaştırmayı zaten kendi
      verisinden kuruyor ve "Nerede ucuz" (E17) bunu aynı tablodan çoktan
      çiziyordu. Alias'ta olanın aynısı: makine hazırdı, çağıran yoktu.
      Kalan yarısı **F5.4**'te (§3.3).
- ✅ **~~F5.10 (yerel yarısı) — Mükerrer gözlem koruması~~.** Tasarımın kuralı:
      *"aynı market + ürün + fiyat 60 sn içinde tekrarlanırsa ikinci gözlem
      yazılmaz"*. `countRecentDuplicates` + `insertUnlessRecentDuplicate`,
      10 test. **E15'ten ÖNCE yazıldı** ve bu kasıtlı: kural yazma yolundan
      önce var olursa çağıran ona uymak zorunda kalır, sonra eklenirse
      "zaten çalışıyordu" sanılan bir şey için ısırdığı hiç görülmeyen bir
      test yazılır.
      SQL `storeId IS :storeId` kullanıyor, `=` değil: `NULL = NULL` yanlıştır,
      yani `=` ile marketi seçilmemiş çekimler **sessizce** korumasız kalırdı.
      Testin ısırdığı kanıtlandı — `=`'e çevirince yalnızca o vaka düştü.
      **Kalan yarı Faz 7'de** (§3.5): "eşitlemede aynı dakika = tek gözlem";
      bugün `pending_op`'a yazan kod yok.
- ✅ **~~F5.11 — İki biçimlendirici~~.** `formatEstimate` (`~642 TL`, tilde
      bitişik, kuruşsuz, en yakına yuvarlar) ve `formatRelativeDay` (altı
      basamaklı tarih merdiveni). Başlık, sepet tahmini ve özet manşeti
      bağlandı. Takvim-günü tuzağı zaten `daysBetween`'de çözülmüştü.
- ✅ **~~F6.9 — Kategori tonları~~.** ⚠ Adım kapandı ama
      `CategoryTile.kt:34`'teki `TODO(kategori-tonlari)` koddan silinmedi —
      açık iş olarak §3.2'de.
- ✅ **~~F10.1~~**
- ✅ **~~F10.4~~** *(`03-arastirma-bulgulari.md`'ye arşiv notu düşüldü)*
- ✅ **~~F10.5 — Sheet yüksekliğindeki sihirli sayı~~.** `TODO(sheet-yuksekligi)`
      koddan silindi. Oran denendi ve **cihazda düştü**: kısmen açılmış bir M3
      sheet içeriği kısıtlamıyor, **kırpıyor** — üçüncü satır 22 piksele iniyor
      ve kaçış butonu `bounds=[0,0][0,0]` oluyordu. Çözüm sayı değil davranış:
      `rememberModalBottomSheetState(skipPartiallyExpanded = true)`
      (`ListScreen.kt:221`).
- ✅ **~~F10.6 — M3 tıklanabilir bileşen sözleşmesi~~.** Kod zaten temizdi: tıklanabilir
      `Button`/`Card`/`ListItem` sıfır, dokuz `Surface`'ın hiçbiri tıklanamaz.
      `HistoryScreen` satırları da fiş döneminde dokunulabilirdi, karar 30 o hedefi
      kaldırdı — madde koda değil, kendi kaydına takılı kalmıştı.
- ✅ **~~F10.8 — 44dp dokunma hedefi~~ — kapandı, ve madde baştan yanlış kurulmuştu.**
      `ProductSheet.kt:143`'teki 44dp bir **dokunma hedefi değildi**: iki
      harfli kategori kutucuğuydu ve elle çizilmişti. Tasarım sisteminin
      ölçüsü 56dp (`Size.categoryTile`) ve `CategoryTile` bileşeni tam da bu
      iş için yazılmıştı — kutucuk bileşene bağlandı, tipografisi de düzeldi.
      `SafeArea.top = 44.dp` zaten bu listeye ait değildi (güvenli alan
      boşluğu, hedef değil); `SafeArea`'nın kendisinin ölü olması ayrı bir
      mesele ve §3.2'deki birleşik ölü kod maddesinde.
- ✅ **~~F10.9 — Satır silme~~** *(cihazda doğrulandı)*. Sağdan sola swipe,
      arkasında 100dp'lik alan ve içinde **"Sil" kelimesi** — çöp kutusu
      ikonu envanterde yok. Eşik 60dp; geçilmeden bırakılırsa 200 ms'de
      yerine dönüyor. Geri alma **5 sn'lik snackbar** (`NeydiSnackbar` —
      uygulamanın **ilk aksiyon taşıyan geçici yüzeyi**). Jestsiz eş:
      Ürün Detayı'nın son satırı, error renginde "Listeden çıkar".
      Jest **yalnız plan modunda**; "Alındı" satırı hiç almıyor.
      ⚠ **Geri alma AYRI bir sorgu:** mevcut "mezar kazma" yolu satırı
      yeniden kuruyordu (`quantity`/`checked`/`addedBy` sıfırlanıyor).
      Onunla yazılsaydı "Geri al" 2 kg elmayı sessizce 1 kg yapardı —
      hata vermeden, test kırmadan. `restore` bu yüzden var ve testi
      ısırdığı kanıtlandı (sorguya `quantity = 1.0` eklenince tam iki test
      düştü).
- ✅ **~~F10.10 — Pano okuması güncel API'ye taşındı~~.** `LocalClipboardManager` →
      `LocalClipboard`. Yenisi metni `ClipEntry` olarak veriyor ve `ClipEntry`'nin
      commonMain'de metin okuyan public üyesi **yok** (Compose'un kendi `readText()`
      yardımcısı `internal`), o yüzden küçük bir `expect/actual` gerekti:
      `plainTextOrNull()`. Android'de `ClipData`, iOS'ta `getPlainText()`.
      **Projenin tek derleyici uyarısı kapandı** — zorlanmış tam derlemede sıfır.
- ✅ **~~F10.12 — Derleyici uyarıları~~** *(uyarı sayısı 5→1)*
- ✅ **~~F10.13~~**
- ⚠ **F10.14 — kaydı bulunamadı.** Eski "Kapandı" satırı F10.13'ten
      doğrudan F10.15'e atlıyordu: **F10.14 bu dosyada hiç geçmiyor** ve
      **F10.15 ise tanımlı**: şema temellerinin nöbetçisi (`SchemaBaselineTest`),
      arşivde ve kodun kendi KDoc'unda yazılı. Sessizce geçilmiyor;
      kaynağı (PR başlığı ya da eski taslak) bulunursa buraya yazılacak.
- ✅ **~~F10.16~~**
- ✅ **~~F10.17 — Fiş dönemi mağaza kalıntısı~~.** Test cihazında 17 çöp `store`
      satırı vardı (eski `rememberStore` her yanlış okunan künye satırını
      mağaza yazmış). Silindi, 10 `trip.storeId` referansı boşaltıldı.
      **Temizlik göçü yazılmadı ve bu bilinçli:** fiş ayrıştırıcısı öldüğü için
      yeni çöp üretilemez; tek seferlik bir iş için kalıcı göç yazmak sonsuza
      kadar taşınacak ölü kod olurdu.
- ✅ **~~F11.10 → E19'a devroldu~~** (§6.1).
- ✅ **~~F11.11 — İkon seti Phosphor'a taşındı (karar 32–34)~~.** 15 ikon
      Phosphor Regular 2.1.1 çizimleriyle elle `ImageVector`. Değişken font
      paketlenmedi, `Text` olarak çizilmedi ve **`material-icons-extended`
      bağımlılığı tamamen düştü** (sürüm kataloğundan da silindi).
      Çağrı yerlerinin hiçbiri değişmedi — `NeydiIcons` katmanının vaadi
      sınandı ve tuttu. Karanlık tema telafisi renk kademesi olarak geldi
      (`iconMuted`, karar 33). Yeni testler: her path'in gerçekten ayrıştığı,
      iki ikonun aynı çizimi taşımadığı, yalnızca yön taşıyanların
      `autoMirror` olduğu. Cihazda beş ikon gözle doğrulandı; on beşinin
      atlası `NeydiIcon.kt`'de `@PreviewLightDark` olarak duruyor.
- ✅ **~~F11.12 · F11.13 · F11.14 · F11.15 · F11.16 — beşinci tur kapandı~~.**
      Ayna dokuz dosyayla tazelendi (**İkonografi ilk kez geldi**), Ekran 1
      başlık örneği merdivene uydu, karar 33 ilişki olarak yeniden yazıldı ve
      okumamız birebir benimsendi, ekran haritasındaki ölü fiş yolları düştü.
      Eşik çelişkisini **karar 36** kapattı → F11.19.
- ✅ **~~F11.17 — Ekran 1'in beşinci çerçevesi tilde aldı~~.** Maket koda uydu;
      Geçmiş'teki yedi gezi tutarı da kuruşunu bıraktı. Kod işi yok.
- ✅ **~~F11.18 — İkonografi karar 33'e uydu~~** (`#C6B6A9` → `#E4D8C9`). Kod zaten
      böyleydi; `NeydiIcon.kt`'deki "neden saptık" savunma paragrafı artık
      gereksiz, tek cümleye inebilir.
- ✅ **~~F11.20 — `FinishShoppingScreen` silindi~~** (231 satır + ViewModel +
      destinasyon + Koin kaydı + özet kartındaki "Hepsini almadım" hedefi).
      Boş Durumlar çerçeve 04'ün başlığı birebir *"Alışveriş kapanışı ·
      **açılmaz**"*; karar 31 pivot turunda teyit etti. İşaretlenmemiş
      satırların cevabı bir **sonraki** gezinin başında "Eksik olabilir"de.
      ⚠ **Test bir şey yakaladı:** `quantityBadge` o pakette yaşıyordu ve
      `quantityLabel`'ın neredeyse birebir kopyasıydı. İddiaları silmek
      yerine `ListStateTest`'e taşıdım — `0,182 kg` gibi üç ondalıklı
      vakalar yalnızca orada vardı. `"ad"` kısaltması dalı düştü:
      `normalizeUnit` onu sınırda zaten `"adet"`e çeviriyor.
- ✅ **~~F11.21 — Ara kare çizilmiyor~~** *(cihazda doğrulandı)*. Sebebi inceydi:
      `shouldSkip = !loading && rows.isEmpty()`, yani **yüklenirken henüz
      `false`** — ekran o arada sıfır satırla çizilip sonra atlanıyordu.
      Artık `!loading && !shouldSkip` olmadan hiç çizilmiyor. Yerine bir şey
      konmadı: *"kurulum dışında hiçbir ekran tam ekran yükleme
      göstermez"* + *"boş ekran açılmaz"*.
- ✅ **~~F11.22 — Dört kararımızın dördü de onaylandı~~**, sıfır itiraz; karar
      42/43/44/45 olarak deftere girdi. Karar 44 eşiği ("iki market")
      sayısallaştırarak onayladı. Ayrımımız tutmuş: yüzeyin doğup doğmadığı
      sözleşmenin işi, dolgunun rengi maketin işi.
- ✅ **~~F11.23 — Özet kartı tutar yokken çiziliyordu, karar 45'in tersi~~.**
      `BasketAndSummary.kt:62` yalnızca *manşeti* koşula bağlıyordu; kart ve
      "8 ürün · 24 dakika" satırı her hâlde çiziliyordu. Ertelenme gerekçesi
      E18'in kapanmasıyla düştü ve düzeltme **E18 ile aynı PR'da** yapıldı —
      **sıralama tuzağı** buydu: dosyanın kendi KDoc'u *"E18'e kadar tutar HER
      ZAMAN bilinmiyor"* diyordu, yani karar 45 tek başına uygulansaydı özet
      kartı tamamen kaybolacaktı.
- ✅ **~~F11.24 — `NeydiToast` KDoc'u güncellendi~~** (snackbar iki yerde).
- ✅ **~~F11.25 — Yaş biçimlendiricisi~~.** `formatAge(days)`: 2–13 gün "N gün
      önce", 14+ "N hafta önce". Merdivenden **ayrı** ve sebebi tek cümle:
      `formatRelativeDay` 7–13 günü "geçen hafta"ya topluyor, oysa bir yaşta
      okunan şey tam olarak *"8 gün önce"* ile *"12 gün önce"* farkı.
      Merdiven tek başına duran tarihler için (gezi tarihi, kayıt saati).
      `ListItemRow`'daki fiyat ipucu satırı elle `"$daysAgo gün önce"`
      yazıyordu — hafta basamağı yoktu; artık ortak fonksiyonu çağırıyor.
- ✅ **~~F11.26 — Çip para biçimi~~.** `formatChipMinor` → `"89,00"`, TL yok.
      `formatMinor(x, "")` ile aynı sonucu veriyor ama **ayrı bir ad**: boş
      dize geçen bir çağrı kuralı taşımıyor, okuyan niye boş geçtiğini
      bilmiyor ve bir gün cümle içinde de öyle çağırır.
      Kural çağıranından **önce** yazıldı ve `RowModel` KDoc'una işlendi;
      **bugün üç üretim çağıranı var** — eski *"çağıranı henüz yok, E16
      getirecek"* notu geçersiz.
- ✅ **~~F11.28 — Kodda zaten doğruymuş~~.** Alışveriş başlığı yalnızca
      *"N kaldı"* yazıyor; market adı hiç eklenmemişti. Karar 28 eskiydi,
      bu turda **maket koda uydu**. `storeDisplayName` ise hâlâ ölü —
      yalnız testi çağırıyor; kaderi §3.2'deki birleşik ölü kod maddesinde.
      *(Eski not onu "F10.11 listesinde duruyor" diye gönderiyordu; o listede
      hiç yoktu.)*
