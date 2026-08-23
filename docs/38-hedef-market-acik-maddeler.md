# 38 — Hedef market beyanı: kodun cevaplayamadığı altı madde

**23 Ağustos 2026.** Kararlar 116–120 koda indi. Ölçülen her şey maketten
alındı ve birebir uygulandı; aşağıdakiler makette **yok** ya da **kendisiyle
çelişiyor** — kod bunlara hakemlik edemez.

Her madde önce **kodun bugün ne yaptığını** söylüyor, çünkü uygulama duruyor:
tasarım aynı fikirdeyse yazılacak bir şey yok, değilse ne değişeceği belli.

---

## S1 — Bir bantta iki zincir adı: beyan mı, gözlem mi?

Karar 105 metanın taze hâlini `"BİM · bugün"` diye yazdı. Karar 118 bandın
**başına** `storefront + zincir adı` koydu. İkisi aynı satırda buluşunca satır
şunu okuyor:

> 🏪 **A101** · BİM · bugün

İki zincir adı, aralarında tek bir `·`. Biri **beyan** (*"burayı A101'den
alacağım"*), öteki **gözlem** (*"fiyatı BİM'de gördük"*). Maketin on üç satırlık
verisi bu bileşimi **bir kez bile** çizmiyor: 4d'deki sapmalı satırın metası
`"önce 158,00 TL"`, yani karar 105'in iki cümlesinden hiçbiri.

**Kod bugün:** ikisini de çiziyor. Beyan `13sp/600 onSurface` + ikon, gözlem
`13sp/400 onSurfaceVariant`. Ayrım yalnızca punto ve mürekkep.

- **(a)** Böyle kalsın — ikon ve kalınlık yeter.
- **(b)** Sapma varken taze gözlemin **zincir adı düşsün**: `🏪 A101 · bugün`.
  Karar 105 değişir.
- **(c)** Sapma başka bir ayırıcıyla kapansın (`🏪 A101 — BİM · bugün`).
- **(d)** Sapma banttan çıkıp kimlik bandına gitsin. Karar 103 değişir.

---

## S2 — Karar 59 gözlemsiz marketi siliyor; beyan edilen market gözlemsiz olabilir

Karar 59'un silme kapısı **yalnızca gözleme** bakıyor
(`hasObservationsAt`); `trip.storeId` ya da `trip_line.storeId` sorulmuyor.

Karar 117 tam bu boşluğa yerleşiyor: kullanıcı **hiç etiket çekmediği** bir
zincire "gidiyorum" diyebilir — *"2-3 tanesini A101'den alacağım"* cümlesi fiyat
bilgisi gerektirmiyor ve karar 119 zaten *"tahmini bugün değiştirmez"* diyor.
Yeni zincirin gözlemi de yoktur.

Bugünkü zincir şu: etiket çekiminde o markete uzun dokunuş → `softDelete` →
sorgudaki `deletedAt IS NULL` yüzünden ad `null` döner → **satırdaki sapma
işareti kaybolur**, satır sessizce hedefin satırı gibi görünür, başlıktaki sayaç
da düşer. Hedef silinirse beyan hiç çizilmez.

Yani kullanıcının yazdığı plan, **ilgisiz bir ekrandaki tek uzun dokunuşla
haber verilmeden imha oluyor.**

- **(a)** Silme ölçütü genişlesin: gözlemi **ya da** bir beyanı olan market
  silinemez.
- **(b)** Silinsin ama **uyarsın**: *"Bu markete giden 3 satır var."*
- **(c)** Sessizce silinsin, beyanlar temizlensin (bugünkü hâl).

---

## S3 — "Nereye gidiyorsun?" karar 59'un seçicisi değil

Maketin notu *"Seçici karar 59'un market seçicisi — son seçilen zincir yapışkan
gelir"* diyor. Ama **aynı maket seti ikisini farklı çiziyor:**

| | Karar 59 (Ekranlar 2-4) | "Nereye gidiyorsun?" (Ekran 1) |
|---|---|---|
| Biçim | 56px **liste satırları** | 44px **çipler** |
| Zincir | **yedi** | **beş** |
| Arama alanı | var | yok |
| Yeni market | var (iki dokunuşlu onay) | yok |
| Uzun dokunuş | **siler** | yok |
| Seçimi kaldırma | yok | **"Belli değil"** |

Ve iki **yapışkanlık farklı olaydan** besleniyor: karar 59'unki *"en son etiket
çektiğin market"*, beyanınki *"en son gitmeye karar verdiğin market"*. Tek
değere bağlamak, A101'de çekilen bir etiketin BİM gezisinin hedefini
değiştirmesi demek.

⚠ Bu bir uygulama detayı değil **davranış sözü**: kullanıcı bir yerde öğrendiği
jestin (uzun dokunuş = sil) ötekinde de geçerli olmasını bekler.

**Kod bugün:** ikisini **ayrı** bileşen yazdı. Beyan seçicisi çip, aramasız,
eklemesiz, silmesiz; adayları `ListViewModel.storeOptions`'tan. Ortak kod yok.

- **(a)** İki ayrı seçici doğru — maketin cümlesi düzeltilsin.
- **(b)** Tek seçici olsun; beyan sheet'i de arama + ekleme + silme taşısın.
- **(c)** Tek seçici ama **yıkıcı jest yalnız etiket tarafında** — bu da yazılsın.

---

## S4 — "Alındı" alışverişte var mı? *(karar 118'in çapası buna bağlı)*

Karar 116'nın kural metni: bölüm planlamada yok, **"alışveriş ve sonrasında"**
var — ve maketin alışveriş çizimi bunu doğruluyor: listenin dibinde
`Alındı (12)` + `expand_more`.

**Kod bugün bölümü iki modda da kaldırdı** ve gerekçesi ölçülmüş bir bulgu:
alışverişte işaretlenen satırın yer değiştirmesi *"hareket eden başparmağın
altında yeniden sıralama"* demek — kullanıcı bir sonrakine dokunacakken liste
kayar ve yanlış ürünü işaretler. Aynı maket de *"işaretli satır yerinde
kalır"* diyor; bu ikisi birlikte duramıyor.

⚠ **Bu karar 118'i doğrudan kırıyor:** sapanlar bölümünün yeri *"Alındı'nın
üstünde"* diye tarif edilmiş. Alındı yoksa çapa da yok. Kod bölümü **listenin
sonuna** koydu.

- **(a)** Alındı alışverişte geri gelsin; işaretlenen satır oraya **insin**
  (yeniden sıralama kabul edilir).
- **(b)** Alındı geri gelsin ama **katlanmış/sayaç** olarak — satır yerinden
  oynamasın, bölüm yalnızca "12/18" desin.
- **(c)** Alındı hiç olmasın (bugünkü hâl); karar 118'in çapası "listenin sonu"
  diye yeniden yazılsın.

---

## S5 — İki zincire sapılırsa cümle ve bölüm ne diyor?

`trip_line.storeId` serbest bir alan: bir listede hem A101 hem ŞOK istisnası
olabilir. **Maketin bütün örnekleri tek zincirli** — başlıkta `3'ü A101'de` ve
`2'si A101'de`, alışveriş bölümü başlığında `🏪 A101'de · 2`, ve nesir bölümün
**tek** olduğunda ısrar ediyor.

İki zincirde ikisi de kırılıyor. Başlık `"2'si A101'de · 1'i ŞOK'ta"` bütçeyi
aşar — kuralın **kendi ölçümü**: tek zincirli cümle 72dp, reddedilen
`"2 satır A101'de"` 97dp'ydi ve 360dp'de kırpılıyordu.

**Kod bugün** cümle tarafında karar verdi: **zincir adı düşer, sayı doğru
kalır** → `"3'ü başka marketlerde"`. Gerekçe: en büyük zinciri yazıp ötekileri
sessizce düşürmek satır sayısını yalanlardı. Bölüm tarafında **zincir başına bir
bölüm** açtı, çünkü başlık zincirin adını yazıyor ve iki zinciri tek başlık
altında toplamak o adı yalan yapardı.

- **(a)** İkisi de doğru — yazılsın.
- **(b)** Cümle başka bir şey desin *(öneri bekleniyor)*.
- **(c)** Bölüm tek kalsın, başlığı `Başka marketlerde · 3` olsun.

---

## S6 — İstisnayı **hangi jest** yazıyor? *(en sert boşluk)*

Karar 117 istisnanın nerede **durduğunu** söylüyor (`trip_line.storeId`),
karar 118 nasıl **göründüğünü** (bandın başında storefront + zincir adı),
karar 116 ise planlamada **satır yüzeyinin veri değiştirmesini yasaklıyor**.

Geriye Ürün Detayı kalıyor — ama 4d panelinin hiçbir çizimi ve 116–120'nin
hiçbir maddesi orada bir market satırı göstermiyor; o sheet'in çizili tek yeni
bloğu *"Bu listedeki miktar"*.

Yani kullanıcının cümlesindeki tek fiil karşılıksız kaldı:

> *"2-3 tanesini de A101'den alacağım diye **işaretlerim**"*

**Kod bugün:** veri alanı var (şema v8), gösterimi var (karar 118), yazma yolu
`ListRepository.setLineStore` → `ListViewModel.setLineStore` **hazır ve
çağransız**. Kod kendi başına bir jest seçmedi, çünkü seçenekleri var olan
kararları deliyor: Ürün Detayı'na blok eklemek karar 38'in sabitlediği satır
sırasını açar; satırda uzun dokunuş / çoklu seçim ise karar 110'un *"satırda tek
yığılmış hedef"* kuralına dokunur.

- **(a)** Ürün Detayı'na **market satırı** (karar 38 yeniden açılır).
- **(b)** Seçicide **çoklu işaretleme**: hedefi seçtikten sonra "hangileri
  başka yerden?" diye satır listesi.
- **(c)** Alışverişe çıkarken sorulsun — ama kullanıcı **liste yaparken**
  istiyor.
- **(d)** Satırda ikinci bir jest (uzun dokunuş şu an Ürün Detayı'nı açıyor).

---

## Bu turda kodda yapılanlar

Makette **ölçülü** olan her şey uygulandı ve cihazda doğrulandı:

- **Şema v8** — `trip_line.storeId`, tek nullable kolon, otomatik migrasyon.
- **Beyan cümlesi** — `storefront 15dp outline` + hedef `14sp/600 onSurface` +
  `·` outline + sapma `14sp/400 onSurfaceVariant`, aralık 4dp; kırpılan tek
  parça sapma. Chevron çizilmiyor, dokunma hedefi başlık bloğunun tamamı.
- **Türkçe ek kuralları** — yönelme (`turkishDative`: "BİM'e", kaynaştırmalı
  "File'ye") ve iyelik (`possessiveSuffix`: "2'si", "3'ü"). Maketin kısa biçimi
  bir üslup değil **ölçüm sonucu**: `"2 satır A101'de"` 97dp'ydi ve kırpılıyordu.
- **Seçici** — `"Nereye gidiyorsun?"` 20sp/700, çipler h44/pad 0-16/radius 999/
  gap 8, seçili `inverseSurface` + `inverseOnSurface` 15sp/600, seçili olmayan
  `surfaceVariant` + 0.8dp hairline 15sp/500, ayırıcı 1dp, `"Belli değil"` h48
  17sp/500 `onSurfaceVariant`.
- **Sapma işareti** — bandın başında `storefront 14dp` + zincir `13sp/600` +
  `·` + meta; grup içi 4dp, bant 8dp. Sapma tek başına bandı var ediyor
  (56dp → 72dp).
- **Alışverişte bölümleme** — sapanlar reyondan çıkıp zincir başına bölüme
  iniyor, başlıkta storefront + `"A101'de"`.
- **Karar 119 doğrulandı** — beyan aritmetiğe girmiyor; tahmin `EstimateRow`
  üzerinden hesaplanıyor ve o projeksiyonda `storeId` **yok**.

### Yan bulgular *(tasarım kaynaklı değil, kodda düzeltildi)*

1. **Bölüm başlığı simetrikti** (8dp/8dp) ve cihazda **yanlış tarafa
   yaslanıyordu**: üstünde 20,2dp, altında 24,4dp — yani her başlık bir önceki
   bölümün kuyruğu gibi okunuyordu. Maketin ölçüsü 20dp/4dp; uygulandı, cihazda
   33,1dp/20,6dp. *(Kullanıcı bunu "listedeki itemlar çok iç içe gibi duruyor"
   diye bildirdi.)*
2. **Ekonomi bandının aralığı 6dp'ydi**, maket 8px diyor. Sapmasız satırları da
   ilgilendiriyordu.
3. **`inverseSurface`/`inverseOnSurface` tanımsızdı** — M3 baseline morunu
   taşıyorlardı ve onları kullanan ilk yüzey sessizce mor çizerdi.
4. **`storefront` ikonu envantere girdi** (20 → 21), Phosphor Regular 2.1.1,
   yolu kaynağıyla birebir doğrulandı.
5. **Test dosyasında `emptyList()` adlı bir test** standart `emptyList()`'i
   gölgeliyordu; hata "beklenen `List<String>`, gelen `Unit`" diye çıkıyor ve
   sebebi hiçbir yerde görünmüyordu. Adı düzeltildi.
