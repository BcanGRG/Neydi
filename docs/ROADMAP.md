# Neydi — Yol Haritası

Tek gerçek kaynak. Sıradaki iş her zaman **en üstteki işaretlenmemiş kutu**.

**Ürün:** iki kişilik bir hane için ortak market listesi — ne alacağınızı
hatırlatan ve raf etiketi çektikçe **ürün bazında** fiyat hafızası biriktiren.
Döngü: *liste → markette işaretle → etiket çek → ürün + marka + market + tarih
+ fiyat gözlemi → sonraki listede fiyat ipucu*.

**Durum:** **Faz E bitti (19/19).** Etiket okuyucusu **üç zincirde** çalışıyor
(A101, BİM, Migros); Metro bilinçli olarak ertelendi. Fikstür seti **99 gerçek
etiket**, dört zincir. Uygulama derleniyor, cihazda kurulu, **415 test yeşil**,
**sıfır derleyici uyarısı**.

Tasarım kararları **46–74** kodlandı (`11-tasarim-kararlari.md`); 56 ajanlı denetimin
**41 bulgusunun 41'i** kapandı ya da gerekçesiyle bloklu kaydedildi.

> Bu dosya yalnızca **yapılacak işi** ve **kalıcı kuralları** taşır.
> Fiş dönemine (16 Ağu 2026 pivotundan öncesi) ait her şey
> [ARSIV-fis-donemi.md](ARSIV-fis-donemi.md)'de dondurulmuş durumda —
> koddaki `F4.13` gibi göndermelerin kaynağı orası. Buraya geri taşınmaz.

---

## Sıradaki üç iş

| # | İş | Neden şimdi | Kimi bekliyor |
|---|---|---|---|
| 1 | **A101'i cihazda doğrula** | Gramer 19 ölçülmüş etikette yeşil ama uygulamanın **kendi çekiminde** hiç denenmedi | A101'de bir çekim |
| 2 | **Cihazda göz kontrolü** (67 · 68 · 69) | Trend manşeti, Geçmiş grafiği ve özet kartı kodlandı ama hiç görülmedi | 3 gözlemli ürün + 3 tutarlı gezi |
| 3 | **F6.5 — sabit terfisi + bastırma** | "Bunu önerme" listesinin giriş noktası | — |

*E12 ✅ · E13 ✅ · E14 ✅ · E15 ✅ · E16 ✅ · E17 ✅ · E18 ✅ · E19 ✅ — Faz E kapandı.*

### Yeni bir zincirin grameri nasıl ölçülür

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

## Zincir önceliği *(kullanıcı verdi, 18 Ağustos)*

Sıra kullanıcının **gerçekten gittiği** marketlere göre; listede olmayanlar
için gramer **yazılmayacak** (Metro, CarrefourSA, File dahil).

| # | Zincir | Gramer | Fotoğraf | Not |
|---|---|---|---|---|
| 1 | **FullGross** | ❌ | ❌ **yok** | toptancı — Metro sınıfı, en zoru |
| 2 | **Gimat** | ❌ | ❌ **yok** | toptancı — Metro sınıfı |
| 3 | **BİM** | ✅ | ✅ 27 | 24/27 fiyat, sıfır yanlış |
| 4 | **A101** | ✅ | ✅ 19 | 15/19 fiyat, sıfır yanlış (`docs/24`) |
| 5 | **ŞOK** | ❌ | ❌ **yok** | BİM sınıfı olabilir |
| 6 | **Tarım Kredi** | ❌ | ❌ **yok** | |
| 7 | **Migros** | ✅ | ✅ 19 | 16/19 fiyat, sıfır yanlış |

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

### ▸ E12 — Etiket ölçümü ✅ *(27 gerçek BİM etiketi)*

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

### ▸ E14 — TagReader + TagParser ✅

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
    o bayrağa bakıp fiyat alanına odaklanacak (E15).
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
| Kart fotoğrafın üstünde | ✅ `TagThumbnail` kırpılmış kareyi çiziyor (`TagCaptureScreen.kt:491`) |
| Yatay düzen | ❌ (`Gezinme Sozlesmesi:587`) — **E15'ten kalan tek madde** |

**Bitti sayılır — ÖLÇÜT DÜZELTİLDİ.** Önce *"1 etiket → Tahmini sepet
görünüyor"* yazıyordu ve bu **ulaşılamaz**: `BasketAndSummary.kt:228`
`MIN_PRICED_ITEMS = 3` ve gerekçesi kodda yazılı (*"~40 TL yazan bir satır, on
sekiz ürünlük bir sepetin yanında yanlış bir güven veriyor"*). Eşiği düşürüp
kutuyu yeşile boyamak yerine ölçüt ikiye ayrıldı:

- **Kapı A — bir gözlem yazıldı:** Ayarlar → Zincirler satırında o zincir
  soluktan normale döner. Sıfır kod; `observeStoreIdsWithObservations` zaten
  okuyor. ✅ *cihazda 12 gözlemle doğrulandı*
- **Kapı B — Tahmini sepet:** listede **üç** fiyatlı ürün gerekir. Bu E16'nın
  satır ipucuyla birlikte doğal olarak gelir.

### ▸ E16 — Satır fiyat ipucu ✅ *(kod; cihaz doğrulaması 2 gözlemli ürün bekliyor)*

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

**Bitti sayılır:** 2 gözlemli ürünün satırında delta çipi çiziliyor.
⚠ Cihazda **henüz görülmedi** — mevcut 12 gözlemin hepsi ayrı ürün, yani
hiçbir üründe iki gözlem yok. Aynı üründen ikinci çekim gerekiyor.

### ▸ E17 — Ekran 5 fiyat bölümü ✅ *(kod; cihaz doğrulaması 2 market bekliyor)*

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

**Bitti sayılır:** `BİM · Dost · 100 TL` / `Migros · Pınar · 130 TL` çiziliyor.
⚠ Testte çiziliyor (tasarımın kendi örneği), **cihazda henüz değil** — mevcut
12 gözlemin hepsi BİM'de, yani bölüm eşiği açılmadı. Migros'ta çekim
gerekiyor.

### ▸ E18 — `~` tahminleri ✅ *(kod; cihaz doğrulaması 3 fiyatlı ürün bekliyor)*

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

**Bitti sayılır:** başlık *"Son alışveriş: dün · ~642 TL"*.
⚠ Cihazda **henüz görülmedi** — bir gezide en az üç fiyatlı ürün gerekiyor;
mevcut 12 gözlem hiçbir geziye bağlı değil (etiketler geziden bağımsız
çekildi, pivot kararı 3).

### ▸ E19 — Tasarım revizyonu ✅

`10-tasarima-pivot.md`'nin cevabıyla karar defteri güncellenir, ölen ekranlar
silinir.

**Bitti sayılır:** hiçbir tasarım dokümanı var olmayan bir ekranı anlatmıyor.

**Nasıl kapandı:** tasarım projesi yedinci ve dokuzuncu turlarda baştan üretildi;
dokuz `.dc.html` dosyasının hepsi repoda güncel (21 Ağustos). Fiş dönemine ait
her şey `ARSIV-fis-donemi.md`'de dondurulmuş ve oradan geri taşınmıyor. Karar
defteri 56 geçerli karar taşıyor ve **karar 63 kendi yazarı tarafından geri
alındı** — defter artık yalnızca bugün geçerli olanı anlatıyor.

---

# Faz E sonrası

Numaralar **kimliktir, sıra değildir** — eski F-numaraları PR geçmişi ve kod
göndermeleri bozulmasın diye korunuyor. Ayrıntıları arşivde.

## Öncelik 1 — Fiyat hafızasını tamamlayan işler

- [~] **F5.7 ✅ (kod) — Ambalaj boyu çıkarımı.** `readTagPack` **gramaj
      satırından** okuyor: `750 G` → `750.0` + `gr`.
      **Kopukluk tek bir yerdeydi ve sinsiydi:** şema kolonları (`packSize`,
      `packUnit`), `observeList`in alt sorguları ve `PriceHint.PackChanged`
      dalı E16'dan beri hazırdı, `readTagPack` gramajı okuyordu — ama
      `TagCaptureViewModel`'de **`pack` kelimesi hiç geçmiyordu**. Her gözlem
      iki NULL kolonla yazılıyordu, yani shrinkflation dalı **hiç
      ateşleyemezdi** ve hiçbir test bunu söylemiyordu.
      **Yazmadan önce ölçüldü** — 99 fikstürde 50 gramaj okunuyor; asıl soru
      *aynı etiketin iki çekimi aynı gramajı veriyor mu* idi ve cevap evet:
      `133220/226/227` üçünde de `1.0 lt`, `133247/248/249` üçünde de `2.0 kg`.
      İki **farklı** gramaj okuyan tek vaka yok — uydurma bir ambalaj
      değişiminin kaynağı tam da bu olurdu.
      **Çelişkili etikette gramaj da düşüyor** (7/50): çapraz kontrol üç
      sayıdan birinin yanlış olduğunu söylüyor, hangisinin olduğunu değil.
      Fiyat hatası bir sayıdır, yanlış gramaj bir **iddiadır** — *"ambalaj
      küçüldü"*.
      Kartın kopyası `ConfirmCard.readFrom` olarak **ayrıldı**: kusurun yaşadığı
      dikiş artık ViewModel'in dışında ve test edilebilir.
      `writeTagObservation`'ın ambalaj parametrelerinin **varsayılanı yok** —
      atlanmaları derleme hatası; `save()`in çağrı yeri birim testle
      korunamıyor, tek nöbetçisi derleyici.
      ⚠ **Cihazda görülmedi:** ipucu için aynı üründen **iki farklı boyda**
      gerçek çekim gerekiyor. Ölçüm `docs/24`'ün yöntemiyle, kanıt
      `ListPriceHintTest.twoRealTagsInDifferentSizesRaiseShrinkflation`.
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
      noktası; Ekran 5'e bağlı. *(Öncelik 1'de kalan tek kodlanacak madde.)*

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
- **F10.5 ✅ — Sheet yüksekliğindeki sihirli sayı.** `TODO(sheet-yuksekligi)`
      koddan silindi. Oran denendi ve **cihazda düştü**: kısmen açılmış bir M3
      sheet içeriği kısıtlamıyor, **kırpıyor** — üçüncü satır 22 piksele iniyor
      ve kaçış butonu `bounds=[0,0][0,0]` oluyordu. Çözüm sayı değil davranış:
      `rememberModalBottomSheetState(skipPartiallyExpanded = true)`
      (`ListScreen.kt:221`).
- [ ] **F10.2 — Bottom sheet'leri Nav3 Scene'e taşı.** *(F10.5 bağı düştü)*
- **F10.6 ✅ — M3 tıklanabilir bileşen sözleşmesi.** Kod zaten temiz: tıklanabilir
      `Button`/`Card`/`ListItem` sıfır, dokuz `Surface`'ın hiçbiri tıklanamaz.
      `HistoryScreen` satırları da fiş döneminde dokunulabilirdi, karar 30 o hedefi
      kaldırdı — madde koda değil, kendi kaydına takılı kalmış.
- [ ] **F10.7 — Odak halkasını bağla.** `Modifier.focusRing` tanımlı, çağıranı yok.
- [ ] **F10.8 — 48dp altında kalan TEK kontrol.** `ProductSheet.kt:143`
      (`.size(44.dp)`). Üç değil bir: karar 56 en küçük hedefi tek sayıya
      indirdi (48dp) ve kalanlar o turda düzeltildi. `SafeArea.top = 44.dp`
      bu listeye ait değil — dokunma hedefi değil, güvenli alan boşluğu
      (üstelik `SafeArea`'nın kendisi ölü, bkz. F11.4).
- **F10.9 ✅ — Satır silme** *(cihazda doğrulandı)*. Sağdan sola swipe,
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
- [ ] **F10.11 — Ölü kod ve ölü token temizliği.** *(bir bölümü yapıldı)*
      **Silinenler:** `ui/screens/Placeholders.kt` (86 satır, hiçbir dosya import
      etmiyordu), `ListScreen`'deki altı FileKit import'u, `formatDayMonthTime`,
      `NeydiExtraShapes.barTop`, katalogdan `coil`/`coil-compose`.
      **Kalan adaylar:** `SafeArea`, `AccentStrip`, `Modifier.focusRing`,
      `parseMinorInput` *(karar 73 onu onay kartından çıkardı; üretimde
      çağıranı kalmadı, yalnız kendi testi var — F5.4 dış veriyle geri
      dönebileceği için silinmedi)*.
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

- **F11.20 ✅ — `FinishShoppingScreen` silindi** (231 satır + ViewModel +
      destinasyon + Koin kaydı + özet kartındaki "Hepsini almadım" hedefi).
      Boş Durumlar çerçeve 04'ün başlığı birebir *"Alışveriş kapanışı ·
      **açılmaz**"*; karar 31 pivot turunda teyit etti. İşaretlenmemiş
      satırların cevabı bir **sonraki** gezinin başında "Eksik olabilir"de.
      ⚠ **Test bir şey yakaladı:** `quantityBadge` o pakette yaşıyordu ve
      `quantityLabel`'ın neredeyse birebir kopyasıydı. İddiaları silmek
      yerine `ListStateTest`'e taşıdım — `0,182 kg` gibi üç ondalıklı
      vakalar yalnızca orada vardı. `"ad"` kısaltması dalı düştü:
      `normalizeUnit` onu sınırda zaten `"adet"`e çeviriyor.
- **F11.21 ✅ — Ara kare çizilmiyor** *(cihazda doğrulandı)*. Sebebi inceydi:
      `shouldSkip = !loading && rows.isEmpty()`, yani **yüklenirken henüz
      `false`** — ekran o arada sıfır satırla çizilip sonra atlanıyordu.
      Artık `!loading && !shouldSkip` olmadan hiç çizilmiyor. Yerine bir şey
      konmuyor: *"kurulum dışında hiçbir ekran tam ekran yükleme
      göstermez"* + *"boş ekran açılmaz"*.
- **F11.22 ✅ — Dört kararımızın dördü de onaylandı**, sıfır itiraz; karar
      42/43/44/45 olarak deftere girdi. Karar 44 eşiği ("iki market")
      sayısallaştırarak onayladı. Ayrımımız tutmuş: yüzeyin doğup doğmadığı
      sözleşmenin işi, dolgunun rengi maketin işi.
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
- **F11.29 ✅ — İkon envanteri 17.** İki Phosphor oku (`ph-arrow-up` /
      `ph-arrow-down`) taşındı, `autoMirror` **kapalı** — dikey yön taşıyorlar.
      `Chips.kt` artık Unicode glif (`↑`) yerine ikon çiziyor: karar 32
      *"ikonlar `Text` olarak çizilmiyor"* diyor, üstelik glif sistem
      fontundan çözülüyordu ve Skia'nın yedek zinciri iki platformda aynı
      şekli vermiyordu. Testin `autoMirror` vakası iki taraflı: hepsine
      `autoMirror` veren bir değişiklik burada düşer.
      ⚠ **Cihazda görülemedi** — delta çipi ≥2 gözlem istiyor ve gözlem
      üretebilen yüzey yok. **F11.19 ile aynı kuyrukta**, E15'te bakılacak.
      Ok boyu (14dp) tasarımda dp olarak yazılı değil (*"12sp metinle
      birlikte"*); gözle seçildi, türetilmedi.
- **F11.12 ✅ · F11.13 ✅ · F11.14 ✅ · F11.15 ✅ · F11.16 ✅ — beşinci tur kapandı.**
      Ayna dokuz dosyayla tazelendi (**İkonografi ilk kez geldi**), Ekran 1
      başlık örneği merdivene uydu, karar 33 ilişki olarak yeniden yazıldı ve
      okumamız birebir benimsendi, ekran haritasındaki ölü fiş yolları düştü.
      Eşik çelişkisini **karar 36** kapattı → aşağıda F11.19.
- [x] **F11.23 — Özet kartı tutar yokken çiziliyor, karar 45'in tersi.** ✅ Ertelenme gerekçesi E18'in kapanmasıyla düştü.
      `BasketAndSummary.kt:141` yalnızca *manşeti* koşula bağlıyor; kart ve
      "8 ürün · 24 dakika" satırı her hâlde çiziliyor. ⚠ **Sıralama tuzağı:**
      dosyanın kendi KDoc'u *"E18'e kadar tutar HER ZAMAN bilinmiyor"* diyor —
      karar 45 bugün uygulanırsa özet kartı **tamamen kaybolur**. **E18 ile
      aynı PR'da** yapılmalı.
- **F11.24 ✅ — `NeydiToast` KDoc'u güncellendi** (snackbar iki yerde).
- **F11.25 ✅ — Yaş biçimlendiricisi.** `formatAge(days)`: 2–13 gün "N gün
      önce", 14+ "N hafta önce". Merdivenden **ayrı** ve sebebi tek cümle:
      `formatRelativeDay` 7–13 günü "geçen hafta"ya topluyor, oysa bir yaşta
      okunan şey tam olarak *"8 gün önce"* ile *"12 gün önce"* farkı.
      Merdiven tek başına duran tarihler için (gezi tarihi, kayıt saati).
      `ListItemRow`'daki fiyat ipucu satırı elle `"$daysAgo gün önce"`
      yazıyordu — hafta basamağı yoktu; artık ortak fonksiyonu çağırıyor.
- **F11.26 ✅ — Çip para biçimi.** `formatChipMinor` → `"89,00"`, TL yok.
      `formatMinor(x, "")` ile aynı sonucu veriyor ama **ayrı bir ad**: boş
      dize geçen bir çağrı kuralı taşımıyor, okuyan niye boş geçtiğini
      bilmiyor ve bir gün cümle içinde de öyle çağırır.
      **Çağıranı henüz yok** (`cheaperElsewhere`'i dolduran E16 getirecek);
      kural çağıranından önce yazıldı ve `RowModel` KDoc'una işlendi.
- **F11.28 ✅ — Kodda zaten doğruymuş.** Alışveriş başlığı yalnızca
      *"N kaldı"* yazıyor; market adı hiç eklenmemişti. Karar 28 eskiydi,
      bu turda **maket koda uydu**. `storeDisplayName` ise hâlâ ölü —
      yalnız testi çağırıyor (F10.11 listesinde duruyor).
- **F11.17 ✅ — Ekran 1'in beşinci çerçevesi tilde aldı.** Maket koda uydu;
      Geçmiş'teki yedi gezi tutarı da kuruşunu bıraktı. Kod işi yok.
- **F11.18 ✅ — İkonografi karar 33'e uydu** (`#C6B6A9` → `#E4D8C9`). Kod zaten
      böyleydi; `NeydiIcon.kt`'deki "neden saptık" savunma paragrafı artık
      gereksiz, tek cümleye inebilir.
- [ ] **F11.19 — Karar 36'nın renk ayrımı cihazda görülmedi.** Kod, testler
      (8 yeni) ve önizleme yerinde; ama uygulamada bugün **gözlem üretebilen
      bir yüzey yok**, yani karışık liste (kimi koyu, kimi soluk) çalışan
      uygulamada ulaşılamaz bir hâl. Sıfır gözlemli hâl cihazda doğrulandı.
      **E15** gelince gözle bakılacak — **F11.29'un delta oku da aynı kuyrukta**.
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

Kodda bugün duran TODO'lar (`grep -rn "TODO(" composeApp/src`):

| TODO | Kapatan adım |
|---|---|
| `tnum` | F9.4 |
| `kategori-tonlari` | F6.9 *(adım kapandı, TODO hâlâ kodda — silinecek)* |
| `ios` · `ios-statusbar` | F9.1 · F9.3 |

`sheet-yuksekligi` ✅ silindi (F10.5). `splash` kodda yok — F8.4 geldiğinde
yazılacak.

---

## İlgili dokümanlar

| Dosya | Ne işe yarar |
|---|---|
| [11-tasarim-kararlari.md](11-tasarim-kararlari.md) | **Aktif** — 56 kararın kod durumu (46–69 dahil), gezinme sözleşmesi sabitleri, ikonografi |
| [17](17-e12-etiket-olcumu.md) · [18](18-zincir-karsilastirmasi.md) · [24](24-a101-olcumu.md) | **Etiket ölçüm raporları** — BİM · üç zincir karşılaştırması · A101 |
| [25-tasarima-sorular-10.md](25-tasarima-sorular-10.md) | Onuncu tur soruları — **cevaplandı**, kararlar 70–74 (`docs/11`) |
| [21](21-tasarim-denetimi-38-kalan.md) · [22](22-tasarima-sorular-8.md) · [23](23-tasarima-sorular-9.md) | Sekizinci/dokuzuncu tur denetim ve sorular |
| [12-tasarima-sorular-4.md](12-tasarima-sorular-4.md) | Dördüncü tur — cevaplandı, arşiv değeri |
| [10-tasarima-pivot.md](10-tasarima-pivot.md) | Tasarıma pivot bildirimi — cevaplandı, arşiv değeri |
| [ARSIV-fis-donemi.md](ARSIV-fis-donemi.md) | Pivottan önceki tam harita; F-numaralarının kaynağı |
| [01-claude-design-prompt.md](01-claude-design-prompt.md) | Sekiz ekranın özgün spesifikasyonu |
| [05](05-tasarim-denetimi.md) · [06](06-tasarima-sorular.md) · [07](07-tasarima-sorular-2.md) · [08](08-tasarim-bulgulari.md) · [09](09-tasarima-sorular-3.md) | Önceki tasarım turları — fiş dönemi, arşiv değeri |
| [03-arastirma-bulgulari.md](03-arastirma-bulgulari.md) | ⚠ Fiş iddiaları geçersiz, başında arşiv notu var |
| [00-isim-onerileri.md](00-isim-onerileri.md) · [02-logo-splash-prompt.md](02-logo-splash-prompt.md) | İsim analizi · logo/splash promptları |
| `tasarim/` | Ekran tasarımları, karar defteri, devir paketi |
