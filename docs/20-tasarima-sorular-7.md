# Tasarıma sorular — yedinci tur

**18 Ağustos 2026.** Girdi listesi `19-tasarim-denetimi-girdileri.md`. Okunan:
`docs/tasarim/` altındaki dokuz dosya, karar defterinin kırk beş maddesi,
ölçümler `17-e12-etiket-olcumu.md` (27 BİM etiketi) ve
`18-zincir-karsilastirmasi.md` (80 etiket, üç zincir).

Biçim önceki turlarla aynı: **tasarımın verdiği** → **gerçek** → **soru**.
Bu turda her maddede ayrıca açık bir *"neden soruluyor"* satırı var.

Girdi listesindeki on sekiz case'in hepsi bir soru numarasına bağlandı;
eşleme §8'de. Denetim bunlara ek olarak **on dokuz yeni case** buldu — onlar
da aynı listede.

---

## §1 · En büyük eksik: yazılan gözlemin geri dönüşü yok

### 1. Kaydedilmiş bir gözlem düzeltilemiyor, silinemiyor

**Tasarımın verdiği.** Kart kuralları elle fiyat girmenin gerekçesini yazıyor:

> *"Elle fiyat girme yasağının tek istisnası: **yanlış okunanı düzeltmek**.
> Alan dolu gelirse dokunuşla düzeltilir."* — Ekranlar 2–4

Ama bu istisna yalnızca **kart açıkken** geçerli. Kaydet'e basıldıktan sonra
gözleme dokunan bir yüzey tasarımın hiçbir dosyasında yok. Bağlantı matrisi —
ki *"listede olmayan kenar yoktur"* diyor — gözlem silen ya da düzelten tek
bir kenar taşımıyor. Ürün Detayı'nın son satırı **"Listeden çıkar"**, yani
listeden; gözlemden değil. Ayarlar'daki tek yıkıcı iş **"Verilerimi sil"** ve
kapsamı 46 gözlemin **hepsi**.

**Gerçek.** Veri katmanı bunu zaten destekliyor: `PriceObservation.deletedAt`
kolonu duruyor ve `ListPriceHintTest.aDeletedObservationIsInvisible()`
sorguların ona uyduğunu kanıtlıyor. Yani eksik olan altyapı değil, **kapı**.
Hiçbir ViewModel `deletedAt` yazmıyor.

Bunun bedeli ölçümde somut: Migros'ta patates için ayrıştırıcı **4389,00 TL**
üretiyordu (`docs/18`). O satır bir kez yazıldığında karar 26'nın market+marka
fiyat geçmişine giriyor; sparkline'ı, delta çipini, "Nerede ucuz" sıralamasını
ve "Başka markette ucuz" çipini kalıcı olarak bozuyor — ve kullanıcının tek
çaresi **bütün hanenin verisini silmek**.

**Soru.**
1. Yazılmış bir gözlem nereden düzeltilir? Ürün Detayı'ndaki gözlem satırına
   uzun dokunuş → "Bu gözlemi sil" mi, yoksa satırın kendisi düzenlenebilir mi?
2. Silme geri alınabilir mi? Snackbar'ın **üçüncü** kullanımı olur — ve
   değişmez *"Snackbar iki yerde yaşar"* diyor. Üçüncü yer açılıyor mu, yoksa
   gözlem silme aksiyonsuz toast mu alıyor?
3. Yıkıcı işin jest olmayan eşi kuralı burada nasıl işliyor?

> **Neden soruluyor:** uygulamanın ikinci varlık sebebi fiyat hafızası ve o
> hafızaya yanlış bir sayı girmenin **hiçbir geri dönüşü yok**. Ölçüm bu
> sayının gerçekten üretildiğini gösterdi; tasarım o günü hiç tarif etmemiş.

---

## §2 · C öbeği — ölçüme dayanarak verdiğimiz, tasarımın görmediği altı karar

Her biri **onaylanacak ya da reddedilecek** bir soru hâline getirildi.

### 2. C1 · Onay kartında "Vazgeç" düğmesi var mı, yok mu?

**Tasarımın verdiği.** Gezinme sözleşmesi Vazgeç'i **üç ayrı yerde** adıyla
anıyor:

> *"Kartta ← yok; **'Vazgeç'** aynı işi yapar."* — geri sözleşmesi, yukarı oku sütunu
>
> *"Kart dolu · **Vazgeç / geri** → Gözlem yazılmaz, fotoğraf silinir."* — durum makinesi
>
> *"Onay kartı → Etiket çek · tetikleyici: **Kaydet ya da Vazgeç**"* — bağlantı matrisi

**Gerçek.** Girdi listesindeki gerekçe (*"tasarımın kart durumunda da yok"*)
**yanlış** — tasarım Vazgeç'i istiyor. Kodda düğme gerçekten yok, ama eksiklik
bilinçli bir sadeleştirme değil, bir atlama: `TagCaptureScreen`
`onDismissCard` parametresini **alıyor** ve hiçbir yere bağlamıyor (yalnızca
üç önizlemede boş lambda olarak geçiyor). Aynı dosyanın KDoc'u ise
*"kapatmak icin acik bir 'Vazgec' gerekiyor"* diye yazıyor. Yani kod kendi
belgesiyle de çelişiyor.

**Soru.** Vazgeç düğmesi çiziliyor mu? Çiziliyorsa **nerede** — Kaydet'in
solunda ikincil buton mu, kartın başlığında `close` mu? Çizilmiyorsa üç
dokümandaki üç satır düşürülmeli ve geri tuşunun tek çıkış olduğu koridorda
bunun nasıl öğretileceği yazılmalı.

> **Neden soruluyor:** kararı "tasarım da istemiyordu" diye vermiştik;
> okuyunca tasarımın tam tersini söylediği görüldü. Kararın dayanağı çöktüğü
> için karar yeniden alınmalı.

### 3. C2 · Grameri çözülmemiş zincirde hiçbir alan doldurulmuyor

**Tasarımın verdiği.** Tasarım zincirler arasında **hiçbir yetenek farkı
tanımıyor**. Karar 11 yedi zinciri eşit tohumluyor, durum makinesi her çekim
için *"Fiyat, ürün, marka dolar"* diyor.

**Gerçek.** `grammarFor` yalnızca **BİM ve Migros**'u tanıyor. Yani tohumlanan
yedi zincirin **beşi** (A101, ŞOK, CarrefourSA, File, Tarım Kredi) ve
*"+ Yeni market"* ile eklenen her market `UNSUPPORTED_CHAIN` kapısına düşüyor:
kart boş açılıyor.

İki tasarım kuralı burada çarpışıyor:

- *"Aynı anda birden çok alan boşsa **yalnızca ilki** vurgulanıyor"* (karar 25)
- *"Kaydet pasif; **ilk rakamda** etkinleşir"* (durum makinesi)

Sonuç: A101'de çekim yapan kullanıcı **bir** amber şerit, üç sessiz boş alan
ve pasif bir Kaydet görüyor. Ekranda o marketin desteklenmediğini söyleyen
hiçbir şey yok — kullanıcı OCR'ın bozuk olduğunu sanıyor.

**Soru.**
1. Kapı onaylanıyor mu? (*"Yanlış fiyat, fiyat olmamasından kötü"* kuralının
   devamı.)
2. Onaylanıyorsa **desteklenmeyen zincirin kendi hâli** çizilecek mi — kartın
   başında tek cümle (*"BİM ve Migros etiketlerini okuyabiliyoruz; burada
   fiyatı sen yaz"*) gibi? Yoksa "yalnızca ilki vurgulanır" kuralı bu hâlde
   askıya mı alınıyor?
3. Market seçicide desteklenen zincir ayırt ediliyor mu? Karar 36 Ayarlar'da
   aynı işi **renkle** yapıyor (gözlemli / gözlemsiz); aynı jest burada da
   kullanılabilir.

> **Neden soruluyor:** kararın kendisi ölçüme dayanıyor ve sağlam, ama
> tasarımın *"her zincir aynı"* varsayımını çürütüyor. Kullanıcı yedi zincirin
> ikisinde çalışan bir özelliği yedisinde de çalışıyor sanıyor.

### 4. C3 · Manşet birim fiyatla çelişirse fiyat yazılmıyor

**Tasarımın verdiği.** Kartın tek eksik-alan sözlüğü var: *"fiyat okunamadı —
yaz"*. Amber'in anlamı ise iki şeyi birden kapsıyor: *"Bir şey **eksik** ya da
**emin değiliz**"*.

**Gerçek.** Kodda ayrım zaten yapılmış — `TagSkip` iki değer taşıyor
(`UNSUPPORTED_CHAIN`, `PRICE_CONTRADICTS_UNIT_PRICE`) ve KDoc'u *"'fiyat
okunamadi' ile 'fiyat okundu ama guvenilmez' kullaniciya ayni gorunse de bize
ayni degil"* diyor. **Kullanıcıya aynı görünüyor**, çünkü tasarımda ikinci
cümle yok.

Daha ciddisi: kontrol **çalıştığı yerde gereksiz, gerektiği yerde ölü.**
`contradictsUnitPrice` gramajı bilmeden çarpanı kuramıyor
(`pack?.sizeIn(...) ?: return false`). Ölçüme göre gramaj **BİM'de 23/27** ama
**Migros'ta 1/19, Metro'da 0/34**. Yani 4389 TL'yi üreten etiketin tam
sınıfında kontrol hiç tetiklenmiyor; sessiz kapsam kaybı ise iki **doğru** BİM
etiketinde yaşanıyor.

**Soru.**
1. Kural onaylanıyor mu?
2. *"Fiyat okundu ama güvenilmez"* için ikinci bir şerit cümlesi yazılıyor mu?
   Öneri: *"okunan fiyat etiketin birim fiyatıyla uyuşmuyor — doğrula"*.
3. Bu ikinci hâlde alan **boş** mu gelmeli, yoksa okunan sayı **kesik
   çerçeveli** (marka önerisindeki jest) mi gelmeli? İkincisi kullanıcıya
   düzeltecek bir şey verir; birincisi sıfırdan yazdırır.

> **Neden soruluyor:** karar "sus" diyor ama tasarımda susmanın iki farklı
> sebebini ayıran bir dil yok — ve kod ayrımı zaten üretiyor, yalnızca
> gösterecek yeri yok.

### 5. C4 · Migros'ta ürün adı hiç okunmuyor

**Tasarımın verdiği.** Değişmez: *"**Aynı etiket metni aynı markette bir kez
sorulur.**"* Yani ürün eşleştirme maliyeti bir kereliktir.

**Gerçek.** O değişmez Migros'ta çalışmıyor, çünkü **anahtarın kendisi
gürültü**. Ölçümdeki Migros çıktıları: `TO00ge`, `NUIK KREMIASI orünün lot
ve…`. Aynı rafın aynı etiketi ikinci çekimde farklı gürültü üretiyor,
dolayısıyla "aynı etiket metni" hiçbir zaman eşleşmiyor ve ürün **her
seferinde** soruluyor. Ölçüm: Migros'ta ad 15/19 "döndü" ama döndüğü şey
kullanılabilir bir ad değil.

**Soru.**
1. Karar onaylanıyor mu (o markette ad hiç önerilmesin)?
2. Onaylanıyorsa *"bir kez sorulur"* değişmezi ne oluyor — düşüyor mu, yoksa
   **market + kullanıcının seçtiği ürün** üzerinden mi kuruluyor? İkincisi
   mümkün: kullanıcı bir kez "Yoğurt 1 kg" seçince o marketteki sonraki
   çekimlerde **son seçilen ürün** hazır gelebilir (marketin yapışkanlığının
   ikizi).
3. Ürün seçici her karede açılacaksa arama alanına odak **kendiliğinden**
   gidiyor mu? Bugün *"Hiçbir ekran açılırken klavye açmaz"* kuralı buna engel.

> **Neden soruluyor:** kararın kendisi ucuz görünüyor ("ad elle") ama
> tasarımın seri çekim vaadini o markette tamamen bitiriyor: her kare için
> sheet aç, ara, seç.

### 6. C5 · Marka/ad sözcük kapısı (3 harf + sesli + markada rakam yok)

**Tasarımın verdiği.** Karar 39 marka satırını **klavyesiz** bir çip sheet'ine
bağlıyor: *"bu ürün için görülmüş markalar + 'Marka yok'; OCR'ın tahmini kesik
çerçeveli çip olarak listenin başında."* Ve: *"Yazma yok — OCR artığı (DST)
tek dokunuşla **doğrusuna çevriliyor** ya da boşalıyor."*

**Gerçek — ve bu bir kısır döngü.** Sheet'te üç kaynak var: (a) daha önce
görülmüş markalar, (b) OCR'ın tahmini, (c) "Marka yok". Görülmüş markalar
listesi ancak **kabul edilmiş OCR tahminlerinden** doluyor. Yani markanın
uygulamaya girebileceği **tek** kapı OCR — ve ölçüm o kapıdan `oOoao000`,
`Tntkn`, `A.Ş.`, `KG` geldiğini gösteriyor. Sözcük kapısı bunları eliyor;
elediğinde geriye yalnızca "Marka yok" kalıyor.

Sonuç: yeni bir marka **hiçbir zaman** doğru adıyla giremiyor. Karar 39'un
*"doğrusuna çevriliyor"* cümlesinin kodda karşılığı yok, çünkü doğrusunun
nereden geleceği yazılmamış. Ve karar 26 "Nerede ucuz" satırının kimliğini tam
olarak bu alana bağlıyor.

**Soru.**
1. Sözcük kapısı onaylanıyor mu?
2. Marka sheet'i **gerçekten** klavyesiz kalıyor mu? Kalıyorsa yeni bir marka
   sisteme nasıl giriyor — yoksa v1'de markalar yalnızca OCR'ın doğru okuduğu
   kadar mı var?
3. Alternatif: marka sheet'i o **markette görülmüş** markaları da göstersin
   (bugün yalnızca "bu ürün için"). BİM'de bir kez okunan "Dost", yoğurt
   dışındaki ürünlerde de aday olur.

> **Neden soruluyor:** karar 39 "yazma yok" diyerek bir kapıyı kapatıyor ama
> açık bıraktığı tek kapıdan ölçüme göre çöp geliyor. Karar 26'nın dayandığı
> alan bu.

### 7. C6 · E15 kabul ölçütü ikiye ayrıldı

**Tasarımın verdiği.** Eşik tablosu net: *"Sepet tahmini — en az **3 fiyatlı
ürün**; altında satır hiç görünmez."*

**Gerçek.** ROADMAP E15'in kabul ölçütü *"1 etiket çek → Tahmini sepet
satırında görünsün"* diyordu; `MIN_PRICED_ITEMS = 3` yüzünden bu ölçüt
ulaşılamaz. Ölçüt ikiye ayrıldı: gözlem yazılıyor mu (1 etiket) ve satır
görünüyor mu (3 ürün).

**Soru.** Eşik 3'te kalıyor mu? Kalıyorsa **tek etiket çeken kullanıcının
ekranında hiçbir şey değişmiyor** — bu kabul ediliyor mu, yoksa ilk gözlemden
sonra listede görünür bir iz (satırın fiyat çipi, ikinci satır ipucu) doğması
mı gerekiyor? Eşik tablosu bugün *"Delta çipi — 2 gözlem; çip yok, **'ilk
gözlem' ibaresi de yok**"* diyor, yani bugünkü cevap "hiçbir iz yok".

> **Neden soruluyor:** eşik tasarımın kararı, kabul ölçütü bizimdi ve ölçüt
> yanlıştı. Ama düzeltirken ortaya çıkan asıl soru şu: ilk çekimden sonra
> kullanıcı çalıştığını nereden anlayacak?

---

## §3 · D öbeği — dört iddianın doğrulaması

### 8. D1 · Money fiyatı — **doğrulandı, tasarımda hiç geçmiyor**

**Doğrulama.** Dokuz dosyada `Money`, "kart", "üye fiyatı", "iki fiyat"
geçmiyor. Tasarımın fiyat hakkında söylediği tek şey biçim:

> *"gözlem fiyatı — 100,00 TL · etiketten okunan **tek fiyat** kesindir"*

*"Tek fiyat"* varsayımı Migros'ta yanlış: normal ve Money kart fiyatı ayrı
basılıyor ve ayrıştırıcı hangisini seçtiğini söylemiyor.

**Soru.** Hangi fiyat kaydediliyor — **etikette büyük basılan** mı, yoksa
**kullanıcının ödeyeceği** mi? İkincisi ise kullanıcının kartı olup olmadığı
bir kurulum sorusu (*"Money kartın var mı?"*) hâline gelir ve kurulum iki
adımdan üçe çıkar. Birincisi ise kart sahibi için fiyat hafızası sistematik
olarak yüksek kalır ve "Nerede ucuz" karşılaştırması yanılır.

> **Neden soruluyor:** karar 26 marketler arası karşılaştırma vaat ediyor;
> marketlerden biri iki fiyat basıyor ve tasarım hangisini kastettiğini
> söylemiyor. Vaadin doğruluğu bu seçime bağlı.

### 9. D2 · Çok-al etiketleri — **doğrulandı, ölçümde de var**

**Doğrulama.** Metro Lay's Süper Boy 125G (`docs/18`): tek ürün **66,30**,
çok-al **56,5**, ve promosyon şeridi (`ÇOK AL` h=156, `AZ ÖDE` h=149) manşet
fiyattan (h=127/119) **büyük** basılmış. Ayrıştırıcı 56,00 seçiyor: hem yanlış
fiyatı, hem kuruşu düşürerek.

Tasarımda çoklu koşullu fiyat hiç geçmiyor. "Nerede ucuz" maketi (`oilStores`)
her market için tek fiyat + tek birim fiyat çiziyor.

**Soru.** Fiyat geçmişi hangisini taşımalı — **tek ürün fiyatı** mı, koşullu
fiyat mı? Öneri: tek ürün fiyatı, çünkü karşılaştırılabilir olan o.
Onaylanırsa ayrıştırıcıya *"promosyon şeridi olan etikette en büyük glif
kuralı geçersiz"* diye bir kural gerekiyor ve `ÇOK AL AZ ÖDE` gibi şeritler
bir sözlük hâline geliyor — yeni bir bakım yükü. Alternatif: Metro'yu
desteklenmeyen zincir bırakmak (bkz. 3 ve 24).

> **Neden soruluyor:** iddia doğru çıktı ve ölçümdeki üç yanlış fiyattan biri
> tam olarak bu. Kararsız kalırsa Metro'da her etiket sessizce yanlış yazar.

### 10. D3 · `unitPriceMinor` / `packSize` / `priceUnit` — **doğrulandı, iddiadan geniş**

**Doğrulama.** İddia *"şemada birim fiyatın tutarı için kolon yok"* diyordu.
Doğru — ve şema kendi KDoc'unda da çelişiyor:

- `unitPriceMinor` — *"Kurus - **etiketin BUYUK rakami**"* (yani manşet)
- aynı kolon için birkaç satır sonra — *"690,00 TL bir kilo fiyatiysa 0,182
  ile carpilmasi gerekiyor"* (yani **birim başına** fiyat)

Bir kolon iki farklı şey olamaz. Ve tasarım **ikisini de aynı anda çiziyor**:
Ekran 5'in `oilStores` satırı `369,90 TL` + `92,48/lt` taşıyor, `oilBuys`
tablosu dokuz satırın her birinde ikisini birden gösteriyor.

Karar 44 üstüne bir üçüncü şey daha istiyor: ambalaj boyu **filtre**
(*"yalnızca eski boydan gözlemi olan market listeden düşüyor"*) — bu da
`packSize`'ın **güvenilir** olmasını gerektiriyor. Ölçüm: gramaj Metro'da
**0/34**, Migros'ta **1/19**.

**Soru.**
1. `unitPriceMinor` **etiketin manşet fiyatı** mı? (Öneri: evet — okunan şey o.)
2. Öyleyse birim fiyatın **tutarı** için ikinci bir kolon açılıyor mu
   (`unitPriceAmountMinor` + `unitPriceUnit`)? Ekran 5'in iki sütunu ve C3'ün
   çapraz kontrolü ikisini de gerektiriyor.
3. `packSize` iki zincirde okunamıyorken karar 44'ün filtresi ne yapıyor —
   boyu bilinmeyen gözlem **düşüyor** mu, **kalıyor** mu? Düşerse Metro/Migros
   gözlemleri "Nerede ucuz"da hiç görünmez; kalırsa filtre bir işe yaramaz.

> **Neden soruluyor:** E17 "paket mi kg mı" sorusunu depolanmış veriden
> cevaplayamaz durumda ve Ekran 5 tasarımın en çok sütun içeren yüzeyi. Kolon
> kararı E17 başlamadan verilmeli.

### 11. D4 · Aynı ürünün ikiye bölünmesi — **doğrulandı**

**Doğrulama.** Cihazda aynı süt iki ürün oldu: `SEK sÜT %0,5 YAĞLI` ve
`SÜT %0,5 YAĞLI |1L`. Karar 26 **gözlem satırının** kimliğini market+marka
yapıyor ama **ürünün** kimliğini hiçbir karar tanımlamıyor; eşleştirme
`resolveProduct`'ta ve tasarım eşiği söylemiyor.

Sonuç doğrudan tasarımın vaadini kırıyor: iki ürün ayrı olduğu için Ürün
Detayı'nda sparkline'ın **3 gözlem** eşiği hiç dolmuyor, "Nerede ucuz"un
**2 market** eşiği hiç dolmuyor. Yani ekranlar çizilmiyor ve kullanıcı
sebebini göremiyor.

**Soru.**
1. Eşleştirme ne kadar gevşek olmalı? Tasarımın kendi jesti burada
   kullanılabilir: **kullanıcıya sor**. Ürün seçici zaten açılıyor; iki aday
   yakınsa *"Bu 'Süt %0,5 Yağlı 1 L' mi?"* diye tek satır sormak, sessiz
   birleştirmeden de sessiz bölmeden de dürüst.
2. Yanlış bölünmüş iki ürün sonradan **birleştirilebiliyor** mu? Bugün hiçbir
   yüzeyde yok — madde 1'in aynısı: yanlışın geri dönüşü yok.
3. Ürün adı jenerik kalacaksa (*"marka gözlemin alanıdır"*), OCR'dan gelen
   `SEK sÜT %0,5 YAĞLI` neden bir **ürün adı** oluyor? Beklenen davranış
   katalogdaki jenerik "Süt 1 L"ye bağlanmak.

> **Neden soruluyor:** eşik tablosundaki dokuz eşiğin **hepsi** ürün kimliğine
> bağlı. Kimlik gevşek ya da sıkı olduğunda tablo baştan aşağı farklı davranır
> ve tasarımda bu eşiği söyleyen tek satır yok.

---

## §4 · Kendi içinde çelişen kurallar

### 12. Amber iki şey söylüyor — ve §11'in kendi kuralını çiğniyor

**Tasarımın verdiği.** Rengin anlamı bölümü kendi başlığında şart koşuyor:

> *"Renk bir süs değil bir cümle; **aynı renk iki şey söyleyemez.**"*
>
> *"Amber — Bir şey **eksik ya da emin değiliz**. Dokunulacak bir iş olduğunu
> söyler. **Asla:** uyarı, tehlike ya da **dekoratif vurgu**."*

Ama karar 43 aynı rengi ikinci bir işe veriyor:

> *"ucuzluğu rozetleyen bileşen **amber** 'başka markette ucuz' çipi."*

**Gerçek.** Onay kartında amber = *bir alan eksik, doldur*. Liste satırında
amber = *bu ürün başka markette ucuz*. İkincisi ne eksik ne belirsiz — tam
tersine, en kesin bilgilerden biri.

**Soru.** Hangisi amber kalıyor? Seçenekler: (a) "başka markette ucuz" çipi
kiremit kenarlığa geçer (ileri götüren bir iş), (b) eksik alan şeridi
`warning` tonuna geçer ve amber yalnızca fırsat rengi olur, (c) §11'in *"aynı
renk iki şey söyleyemez"* cümlesi düşer.

> **Neden soruluyor:** ikisi de aynı oturumda görünüyor — kullanıcı listeye
> döndüğünde az önce kartta "doldur" diye öğrendiği rengi "ucuz" diye okumak
> zorunda. Ve kural dosyanın kendi başlığında yazılı.

### 13. Fiyat düşüşü hangi yeşil — #3F6B54 mü #2E6B45 mi?

Üç dosya üç şey söylüyor:

| Kaynak | Fiyat düşüşü oku | Birincil buton dolgusu |
|---|---|---|
| Gezinme sözleşmesi · rengin anlamı | **#3F6B54** (Yeşil) | #3F6B54 |
| Karar 42 + 43 | **#2E6B45** | #3F6B54 · *"#2E6B45 buton zemini olarak hiç kullanılmıyor"* |
| Compose Spec · rol tablosu | `success` **#2E6B45** | `success` — *"**birincil buton dolgusu**"* |

Compose Spec'in `success` satırı karar 42'nin *"hiç kullanılmıyor"* cümlesiyle
doğrudan çelişiyor; gezinme sözleşmesi ise iki yeşili tek renk sayıyor.

**Soru.** İki yeşil de kalıyor mu? Kalıyorsa Compose Spec'in `success` rolü
*"birincil buton dolgusu"* ibaresinden temizlenmeli ve gezinme sözleşmesinin
palet satırı ikiye ayrılmalı. Kalmıyorsa hangisi düşüyor?

> **Neden soruluyor:** ikisi arasındaki fark gözle ayırt edilmiyor ama kodda
> iki ayrı token ve inceleme listesi ikisini de denetliyor. Yanlış olan
> derlenir ve kimse görmez.

### 14. En küçük dokunma hedefi 44dp mi 48dp mi?

| Kaynak | Değer |
|---|---|
| Gezinme sözleşmesi · girdi kuralları | *"En küçük hedef **48dp**"* |
| İkonografi · boyut kuralı | *"Dokunma hedefi her zaman en az **48dp**"* |
| Tasarım sistemi · safe area | *"Minimum dokunma hedefi **44dp**"* |
| Compose Spec | `val minTouch = 44.dp` + inceleme maddesi *"Dokunma hedefi minimum 44dp"* |

**Soru.** Hangisi? Kod bugün 44'ü alıyor (spec'te sayı olarak yazılı olan o)
ama sözleşme ve ikonografi 48 diyor — yani 48'i uygulayan kod **incelemeden
geçmez**, 44'ü uygulayan kod **sözleşmeye aykırı**.

> **Neden soruluyor:** iki sayı iki ayrı platform geleneği (iOS 44pt, Android
> 48dp) ve uygulama ikisinde de çıkıyor. Karar tek olmalı, yoksa her satır iki
> kez tartışılır.

### 15. Tekrarlanan gözlem: 60 saniye mi, aynı dakika mı?

Aynı dosyada iki kural:

> *"Aynı market + ürün + fiyat **60 sn içinde** tekrarlanırsa ikinci gözlem
> yazılmaz."* — durum makinesi
>
> *"Market + ürün + fiyat + **aynı dakika** = tek gözlem. Eşitleme sırasında
> ikincisi düşürülür."* — iki kişi, iki cihaz

Bunlar aynı kural değil: 15:38:59 ile 15:39:01 iki saniye arayla **ayrı**
dakikalar; 15:38:01 ile 15:38:59 elli sekiz saniye arayla **aynı** dakika.

**Gerçek.** Kod 60 sn'lik kayan pencereyi uyguluyor
(`insertUnlessRecentDuplicate`) ve ikinci kuralı **bilerek** reddediyor:

> *"Iki cihaz ayni anda ayni etiketi cekerse iki gozlem yazilir; bu bir hata
> degil — iki kisi gercekten iki kez gormus demektir."*

**Soru.** Tek kural hangisi? Ve eşitlemede ikinci cihazın gözlemi **düşüyor**
mu, yoksa kodun dediği gibi ayrı satır olarak mı duruyor? İkisi farklı ürün
verir: birincisinde "iki kişi aynı rafa baktı" bilgisi kayboluyor.

> **Neden soruluyor:** iki kural aynı işi yapıyor ve biri kod, biri eşitleme
> tarafında yaşıyor. Faz 7'de eşitleme yazılırken çarpışacaklar; şimdi çözmek
> ucuz.

### 16. Gözlem fiyatı tilde alır mı — değişmez kendi kendini yiyor

> *"gözlem fiyatı — **100,00 TL** · etiketten okunan tek fiyat **kesindir,
> tilde almaz, kuruş yazılır**"* — biçimler
>
> *"Tek bir gözlem fiyatı kesindir; ondan **türetilen** her tutar tilde alır."* — değişmezler
>
> *"**Her tutarın önünde ~** · Gerçek ödenen tutar diye bir veri yok; **tahmin
> dışı tutar biçimi kullanılmaz**."* — Compose Spec inceleme listesi

Üçüncüsü ilk ikisiyle çelişiyor ve inceleme listesinde olduğu için
**maketlerin hepsini reddeder**: `42,00 TL`, `164,00 TL`, `89,90 TL` —
hiçbiri tilde taşımıyor ve hepsi doğru.

**Gerçek.** `docs/11`'e biz de yanlış aktarmışız: *"Kesin tutar diye bir biçim
yok"* yazmışız, oysa var — gözlem fiyatı. F11.17 (*"Son alışveriş: bugün ·
642,50 TL"*) yine de geçerli bir bulgu, çünkü **gezi toplamı** türetilmiş bir
tutar.

**Soru.** Compose Spec'in inceleme maddesi düzeltiliyor mu? Öneri metin:
*"Türetilen her tutarın önünde ~ ve kuruşsuz; tek gözlem fiyatı tildesiz ve
kuruşlu."*

> **Neden soruluyor:** inceleme listesi bir kod incelemesinde tek başına
> yeterli olsun diye yazılmış. Bugünkü hâliyle doğru kodu reddediyor.

### 17. Kategori kutucuğundaki iki harf: ürünün mü kategorinin mi?

> *"İki-harf fallback'i — öğelerin %80'i bunu gösterir"* ve örnekler:
> **DO** Meyve-Sebze · **EK** Fırın-Ekmek · **SÜ** Süt-Kahvaltılık
> — Tasarım sistemi §04

Bunlar **ürün** baş harfleri (DOmates, EKmek, SÜt). Ekranlar 2–4 ise aynı
kutucuk için **kategori** baş harflerini veriyor: **MS** Meyve-Sebze,
**FE** Fırın-Ekmek, **SK** Süt-Kahvaltılık.

**Gerçek.** İkisi aynı 56dp kutucuk ve aynı kategori tonu. Ürün okuması
ayrıca ikinci bir kuralla çarpışıyor: harfler ürün adından geliyorsa
**runtime'da büyütülmeleri** gerekir, oysa *"Hiçbir metne runtime'da uppercase
uygulanmaz — Türkçe İ/i/I/ı bozulur"*. `İçim` → `İÇ` locale'siz dönüşümde `IÇ`
olur.

**Soru.** Kutucuk hangi iki harfi taşıyor? (Öneri: kategori — hem sabit
sayıda, hem kaynakta büyük yazılabilir, hem *"öğelerin %80'i"* ifadesi
kategoriyle tutarlı.)

> **Neden soruluyor:** iki dosya aynı bileşeni farklı çiziyor ve biri
> uygulanamaz (uppercase yasağı). Kutucuk her liste satırında var.

### 18. İkon rengi: üç ayrı kural

> *"İkon kendi rengini taşımaz; **içinde bulunduğu metnin rengini alır**. Tek
> istisna amber şeritteki bilgi ikonu."* — İkonografi, kurallar
>
> *"karanlık temada ikon, yanındaki metinden **bir kademe açık** çizilir"* — karar 33
>
> *"[delta oku] **çipin rengini alır**"* — İkonografi, envanter notu

F11.18 bunlardan yalnızca **örneği** yakalamıştı (*"metin #E4D8C9, ikon
#F5EDE6"*); asıl çelişki **kuralın kendisinde**: aynı dosya hem "aynı renk"
hem "bir kademe açık" diyor.

**Soru.** Kural tek cümleye indiriliyor mu? Öneri: *"İkon metnin rengini alır;
karanlık temada ikincil metnin yanındaki ikon bir kademe açık çizilir
(#C6B6A9 → #E4D8C9). Birincil metnin yanında telafi yoktur."*

> **Neden soruluyor:** İkonografi kod envanterinin kaynağı ve tek dosyada iki
> zıt kural taşıyor. F11.18 açık ve yalnızca yarısını kapatıyor.

### 19. Işıkta okunabilir amber: `accentOutline` mi `warning` mi?

> `accentOutline` **#8A5A00** — *"ışıkta **okunabilir amber-metin ihtiyacının
> tek karşılığı**"*
>
> `warning` **#96560A** — *"'fiyat okunamadı' gibi eksik alan cümleleri.
> **Amber metnin ışıktaki yerine geçer.**"*

İki token, aynı iddia, iki farklı hex. Üstüne İkonografi *"tek istisna amber
şeritteki bilgi ikonu"* diyerek `info` ikonuna **amber'in kendisini** veriyor —
ve amber kuralı *"ışık modunda amber metin rengi: **yasak**"* diyor (#E0A32E
surface üzerinde **2.08:1**).

**Soru.**
1. Amber şeritteki cümle hangi token? (`warning` #96560A mı, `accentOutline`
   #8A5A00 mı?)
2. Şeritteki `info` ikonu hangi renk? Amber dolgunun **üstünde**yse `onAccent`
   #3A2600 olur ve istisnaya gerek kalmaz; dolgunun **dışında**ysa 2.08:1
   kontrastla çiziliyor demektir ve amber kuralını çiğniyor.

> **Neden soruluyor:** ikisi "aynı işi yapan iki token"; biri gereksiz. Ve
> bilgi ikonunun rengi cihazda okunamayacak kadar düşük kontrastta olabilir —
> kullanıcı zaten açık temada okunabilirlik sorunu bildirdi (bkz. 29).

---

## §5 · Kullanıcının sıkışacağı yerler

### 20. Fiyat çipinin dokunma hedefi var, kenar matrisinde karşılığı yok

**Tasarımın verdiği.** Liste satırı anatomisi: *"[fiyat çipi, **kendi 44dp
dokunma hedefi**]"*, ve bileşen kütüphanesi çipin **dört hâlini** çiziyor:
rest · basılı · devre dışı · odaklı. Compose Spec inceleme listesi bunu ayrıca
şart koşuyor: *"Fiyat çipi kendi 44dp hedefine sahip."*

Ama bağlantı matrisi — *"Listede olmayan kenar yoktur"* diyen tablo — fiyat
çipinden çıkan **hiçbir** kenar taşımıyor.

Aynı sorun ikinci bir yerde: alışveriş modu satırı `chevron_right` taşıyor ve
*"metadata tek bir 'Detay' affordance'ının arkasına katlanır"* deniyor. Matris
Ürün Detayı'na giden tek kenarı *"Satıra **uzun dokunuş**"* diye yazıyor;
chevron kenarı listede yok.

**Gerçek.** Kodda fiyat çipinin ayrı hedefi yok; `ListItemRow` satırın
tamamına tek `pressable` bağlıyor (`onTap = onToggle`). Yani çipe dokunmak
**satırı işaretliyor** — alışveriş modunda fiyatı okumak için çipe uzanan
kullanıcı ürünü yanlışlıkla "alındı" yapıyor.

**Soru.**
1. Fiyat çipine dokunmak ne yapıyor? (Öneri: Ürün Detayı — fiyatın hikâyesi
   orada.) Yoksa çipin dokunma hedefi ve dört hâli düşüyor mu?
2. Alışveriş modundaki chevron matrise ekleniyor mu?

> **Neden soruluyor:** dokunulabilir görünüp bir işe bağlı olmayan bir eleman,
> tasarımın *"chevron dokunulabilirliğin tek işareti"* kuralını da bozuyor. Ve
> alışveriş modunda bedeli somut: yanlış işaretleme.

### 21. Yanlışlıkla yaratılan market düzeltilemiyor

**Tasarımın verdiği.** Market seçici tek dokunuşla market yaratıyor: *"uyan
zincir yoksa **'+ Yeni market «AKYURT»'** tek dokunuşla ekliyor"* (karar 40).
Ayarlar'daki Zincirler satırından ise **chevron kaldırıldı** (karar 23):
*"mağaza ekranı çizilmeyecek. Satır kendi başına tamam."*

**Gerçek.** Kullanıcı arama alanına `AKYRUT` yazıp yeni market eklerse:

- market kalıcı olarak listeye giriyor,
- karar 26 fiyat geçmişinin kimliğini o markete bağlıyor,
- ve onu **silecek, yeniden adlandıracak ya da birleştirecek hiçbir yüzey yok**.

*"Var olan bir adın başka yazımı (BIM / BİM) sessizce var olan zincire
bağlanıyor"* kuralı yalnızca **bilinen** adları kurtarıyor; yazım hatası zaten
bilinmeyen bir addır.

**Soru.** Yanlış market nereden düzeltilir? Seçenekler: (a) Zincirler satırına
chevron geri gelir ve karar 23 düşer, (b) market seçicide gözlemsiz bir
markete uzun dokunuş silme sunar, (c) yeni market yaratmak ikinci bir dokunuş
ister (*"«AKYRUT» diye yeni market"* onayı).

> **Neden soruluyor:** madde 1'in ikizi — tek dokunuşla yaratılan, hiçbir
> dokunuşla yok edilemeyen kalıcı bir varlık. Ve zincir listesi Ayarlar'da
> görünür olduğu için hata her açılışta görünüyor.

### 22. `MİGROS ATAŞEHİR` — şube kavramı nereden geliyor?

**Tasarımın verdiği.** Geçmiş ekranının gezi satırları şube taşıyor:
`MİGROS ATAŞEHİR`, `A101 KÜÇÜKBAKKALKÖY`, `BİM BARBAROS`. Karar 22 bunu kural
hâline getiriyor: *"AKYURT, FiLE, BİM, **MİGROS ATAŞEHİR**"*. Karar 28
başlıktaki adı *"o gezide son çekilen etiketin marketi"*ne bağlıyor.

Ama Ekran 5'in "Nerede ucuz" satırları **şubesiz**: `BİM`, `A101`, `Migros`.
Market seçicinin tohum listesi de şubesiz yedi zincir.

**Gerçek.** Aynı varlık iki maket arasında iki farklı şey. Karar 26 fiyat
kimliğini "market + marka" yapıyor — **market = zincir mi, şube mi?** Şubeyse
BİM Barbaros ile BİM Ataşehir ayrı satır olur ve "Nerede ucuz"un 2 market
eşiği çok daha geç dolar. Zincirse Geçmiş'teki şube adı nereden geliyor?

**Soru.** Şube v1'de var mı? Varsa nereden giriliyor (market seçicide ikinci
bir alan mı, yoksa *"+ Yeni market"* ile `MİGROS ATAŞEHİR` tek dize mi)? Yoksa
Geçmiş maketleri şubesiz hâle mi getiriliyor?

> **Neden soruluyor:** karar 26'nın kimliği ve eşik tablosundaki iki eşik
> doğrudan buna bağlı, ve iki maket birbirini yalanlıyor.

### 23. "Nerede ucuz" eşiği satır kimliğiyle uyuşmuyor

**Tasarımın verdiği.** Eşik: *"'Nerede ucuz' bölümü — en az **2 market**;
altında bölüm çizilmez — tek marketle karşılaştırma olmaz."* Ama karar 26
satırın kimliğini **market + marka** yapıyor ve maket bunu gösteriyor:
`yogurtObs` dört satır taşıyor ve **ikisi de BİM** (Dost 100,00 ·
Sütaş 118,00).

**Gerçek.** Eşik **market** sayıyor, bölüm **satır** çiziyor. Tek markette iki
marka görülmüşse ortada gerçek ve yararlı bir karşılaştırma var (*"BİM'de Dost
100, Sütaş 118"*) ama eşik bölümü çizdirmiyor.

**Soru.** Eşik **2 satır** mı olmalı, yoksa *"tek marketle karşılaştırma
olmaz"* gerekçesi marka çiftleri için de geçerli mi? Ve karar 44'ün ambalaj
filtresi eşiği hangi sayıya uyguluyor — filtreden **önce** mi sonra mı?

> **Neden soruluyor:** eşik tablosu kodlanacak sayılar listesi olarak yazıldı;
> bu satırın birimi kararla uyuşmuyor ve kodda iki farklı sorguya çıkıyor.

### 24. Metro tohum listesinde yok — ama ölçümün üçte biri Metro

**Tasarımın verdiği.** Karar 11 yedi zinciri tohumluyor: BİM, A101, ŞOK,
Migros, CarrefourSA, File, Tarım Kredi.

**Gerçek.** Kullanıcının ölçtüğü 80 etiketin **34'ü Metro** — en büyük parti.
Metro listede yok, dolayısıyla *"+ Yeni market"* ile serbest metin olarak
giriyor ve `grammarFor` onu asla tanımıyor. Üstelik Metro toptancı:
*"KDV Dahildir"* şeridi bazı etiketlerde var, bazılarında yok; koli fiyatı ve
çok-al fiyatı ayrı basılıyor (madde 9).

**Soru.** Metro tohum listesine ekleniyor mu? Eklenirse KDV'li/KDV'siz fiyat
sorusu D1'in (Money) kardeşi olarak açılıyor: **hangi sayı kaydediliyor?**
Eklenmezse tohum listesinin ölçütü nedir — kullanıcının gerçekten gittiği
marketler mi, perakende zincirleri mi?

> **Neden soruluyor:** tohum listesi tasarımın kararı ve kullanıcının en çok
> etiket çektiği market listede yok. Liste ölçütü yazılı olmadığı için
> sekizinci zincir sorusu her yeni markette tekrar sorulur.

### 25. A1 · Deklanşöre basınca hiçbir geri bildirim yok

**Tasarımın verdiği.** Hareket tablosu *"tek yerde toplanmış her süre"* diye
yazıldı ve şu satırları taşıyor: onay kartı 260 ms, kart kapanıp kamera
300 ms, işaretleme 180 ms, toast 150/2000/150. **Deklanşör satırı yok.**
Haptik kuralı da yalnızca *"işaretlemede hafif tık; **kaydetmede** tek darbe"*
diyor — çekimde değil.

**Gerçek.** Kullanıcının ifadesi: *"çekme butonuna basınca bir animasyon bir
şey lazım, şu an çalışmıyormuş hissi veriyor."* Ölçülen boşluk **1,15 sn/kare**
(27 fotoğraf 31 sn). O sürede ekranda `pressable`ın %97 ölçek darbesinden
başka hiçbir şey değişmiyor — o da basma anının kendisi, çekimin değil.

Kod bu boşluğu doldurmak için **tasarımda olmayan bir deyim** icat etmiş:
Kaydet düğmesi metnini *"Kaydediliyor..."* yapıyor. Bu hem buton kuralına
(*"fiil ve tek kelime"*) hem bekleme kuralına (*"iskelet, dönen çark yok"*)
aykırı ve üçüncü bir bekleme dili açıyor.

**Soru.**
1. Çekim anının geri bildirimi ne? Örtücü flaşı (bir kare beyazlama),
   yakalanan karenin küçülerek karta dönüşmesi, haptik darbe — ya da üçü?
   Süresi hareket tablosuna hangi satır olarak giriyor?
2. Kaydet basıldıktan sonraki bekleme neyle anlatılıyor? (Öneri: düğme
   `enabled=false` + iskelet; metin değişmiyor.)

> **Neden soruluyor:** kullanıcının kendi cümlesiyle bildirdiği tek hata bu ve
> tasarım dokümanlarının hiçbirinde deklanşör geri bildirimi geçmiyor —
> kararsızlık değil, boşluk.

### 26. Flaş: envanterde var, hiçbir yerde tarif edilmemiş

**Tasarımın verdiği.** İkon envanterinde `bolt` duruyor: *"iş: **Flaş** ·
nerede: **Kamera** · not: sistem davranışı; kendi ikonumuz olmalı"*. Karar 34
onu on yediye sabitledi ve ikon Phosphor çizimiyle taşındı.

**Gerçek.** Flaşın **kaç hâli** olduğu (otomatik / açık / kapalı), **nerede
durduğu**, seçimin market gibi **yapışkan** olup olmadığı ve yatayda nereye
gittiği hiçbir dosyada yok. Kod da `NeydiIcons.Bolt`'u hiçbir ekranda
çağırmıyor — envanterdeki tek ölü ikon.

Ayrıca İkonografi'nin kendi kuralı: *"Yalnızca **evrensel altı hedef**
etiketsiz durabilir: geri, kapat, menü, ara, ekle, kamera. Diğer her ikon
metinle birlikte."* Flaş bu altının içinde değil, ama kamera ekranında metin
yazacak yer yok.

**Soru.** Flaş v1'de var mı? Varsa üç hâli mi iki hâli mi, hangi köşede, ve
etiketsiz istisnası yediye mi çıkıyor? Yoksa `bolt` envanterden düşüyor mu?

> **Neden soruluyor:** raf etiketi parlak ve markette ışık düşük — flaş
> okunabilirliği doğrudan etkiliyor, ve ölçümdeki tek başarısız çekim
> (`183808`, en büyük glif 12 piksel) muhtemelen bulanıklık. Envanterde ikon
> var, arkasında tasarım yok.

### 27. B5 · Yatay düzende onay kartı nereye gidiyor?

**Tasarımın verdiği.** Cihaz tablosu kamerayı **istisna** ilan ediyor:

> *"Rotasyon · kamera — **Tek yüzeyde gerçek yatay düzen var**: kontroller kısa
> kenara taşınır, metin dönmez."*

**Gerçek.** Cümle yalnızca **kontrolleri** taşıyor. Aynı ekranda yaşayan üç
şey hakkında tek satır yok: **onay kartı** (kameranın üstünde, alttan
yükseliyor), **kendiliğinden açılan sayısal klavye** (yatayda ekranın yarısını
yiyor ve kartı fiyat alanıyla birlikte örtebilir) ve **amber şerit**.

Ayrıca raf etiketi yatay bir dikdörtgen; kullanıcının telefonu yatay tutması
beklenen davranış. Ölçümde 27 fotoğrafın 26'sı dikeydi ama o fotoğraflar
telefonun kendi kamerasıyla çekildi, bizim ekranımızla değil.

**Soru.** Yatayda kart nasıl duruyor — sağ yarıda dikey bir panel mi, yoksa
yatay düzen yalnızca kamera hazır hâlinde mi geçerli ve kart açılınca ekran
dikeye mi kilitleniyor? Klavye açıldığında ne örtülüyor?

> **Neden soruluyor:** sözleşme yatay düzeni açıkça vaat ediyor (*"gerçek
> yatay düzen var"*), yani kod bunu yapmak zorunda; ama yapacak kadar tarif
> yok.

---

## §6 · Gereksiz karmaşa — iki kural bir işi yapıyor

### 28. "Kullanıcı ne çektiğini görsün" iki mekanizmayla vaat ediliyor

**Tasarımın verdiği — iki ayrı yerde, iki ayrı çözüm:**

> *"Onay kartı · 260 ms · **Kamera görüntüsü donar, karartılmaz** — kullanıcı
> ne çektiğini görür."* — hareket tablosu
>
> *"kart alttan yükselir ve **başında etiketin kırpılmış görüntüsünü** taşır"* — karar 25
>
> *"Kod tarafı burayı **kırpılmış gerçek fotoğrafla** çizecek, desenle değil."* — Ekranlar 2–4

**Gerçek.** Kod **ikisini de** vermiyor: `TagCaptureScreen` kart açılınca
kameranın üstüne `Color.Black.copy(alpha = 0.86f)` çekiyor — yani donmuş kare
%86 karartılıyor — ve kartın başında kırpılmış görüntü yok.

**Soru.** İkisi de gerekli mi? Donmuş kare karartılmadan duruyorsa kartın
başındaki kırpım **aynı bilgiyi ikinci kez** veriyor ve kartın yüksekliğini
büyütüyor. Biri seçilecekse hangisi? (Öneri: kırpım — kart alanların yanında
duruyor, göz ikisini birlikte okuyor; donmuş kare kartın arkasında zaten
kısmen örtülü.)

> **Neden soruluyor:** kod ikisini birden atlamış ve şu an kullanıcının *"ne
> okudum"* doğrulaması yok. Hangisinin yazılacağına karar vermeden
> düzeltilemez.

### 29. A2 · Onay kartı açık temada da koyu — kural bilinçli mi?

**Tasarımın verdiği.** Açık ve kasıtlı görünüyor:

> *"Karanlık tema · kamera — Kamera ve onay kartı **her iki temada da koyu**;
> tema değişimi bu akışı etkilemez."*

**Gerçek.** Kod doğru. Ama kullanıcı bunu **iki kez hata sanarak** bildirdi ve
bu başlı başına bir sinyal: kuralın gerekçesi kullanıcıya görünmüyor. Karar
defterinde de gerekçe yok — cihaz tablosunda tek satır olarak duruyor,
kararlar bölümünde karşılığı yok.

**Soru.**
1. Kural korunuyor mu? Korunuyorsa **gerekçesi** karar defterine yazılıyor mu?
   (Gözün karanlık vizöre uyum sağlaması? Kameranın üstündeki açık bir kartın
   yüzen beyaz dikdörtgene dönmesi?)
2. Koyu kart açık temada hangi tokenları kullanıyor — karanlık paletin tamamı
   mı, yoksa yalnızca zemin mi koyu? Bu, madde 19'daki amber/`warning`
   seçimini de belirliyor: koyu zeminde `warning` #96560A okunmaz.

> **Neden soruluyor:** kural tasarımın, uygulama doğru — ama kullanıcı iki kez
> hata sandı. Karar korunacaksa gerekçesi yazılı olmalı, yoksa her turda
> yeniden bildirilir.

### 30. B2 · 1,5 saniyelik iskelet eşiği neyi çözüyor?

**Tasarımın verdiği.** Eşik üç dosyada tekrarlanıyor: durum makinesi (*"OCR
1,5 sn'yi geçerse alanlar iskelet olur, kart beklemez"*), hareket tablosu
(*"1,5 sn'yi geçen tek iş OCR"*), cihaz tablosu (*"OCR 1,5 sn'de dönmezse
iskelet"*).

**Gerçek.** Ölçüm **1,15 sn/kare** (27 fotoğraf 31 sn; fotoğraf yazma + OCR
dahil). Yani eşik pratikte hiç tetiklenmiyor ve kod eşiği hiç yazmamış —
iskelet OCR bitene kadar duruyor.

Asıl belirsizlik eşikte değil, **iki hâlin farkında**: kart zaten *"boş
alanlarla hemen"* açılıyor. Boş alan ile iskelet alan kullanıcıya ne söylüyor?
Biri *"burası boş kalacak"*, diğeri *"bekleniyor"* — ama ikisi de aynı
1,15 saniyede görünüyor.

**Soru.** Kart açıldığı **an** alanlar iskelet mi, boş mu? (Öneri: iskelet —
OCR her zaman çalışıyor ve 1,15 sn görünür bir süre.) Öyleyse 1,5 sn eşiği
düşüyor ve üç dosyadaki üç satır tek cümleye iniyor: *"alanlar OCR dönene
kadar iskelet."*

> **Neden soruluyor:** üç dosyada tekrarlanan bir sayı ölçümle hiç
> tetiklenmiyor. Uygulanamayan kural, gereksiz karmaşa.

### 31. "Tek klavye" değişmezi artık dört alan — ve inceleme listesi eski

**Tasarımın verdiği.** Karar 40 istisnayı genişletti:

> *"Tek klavye istisnası artık 'arama alanları' — Ekle, ürün seçici, market
> seçici."*

Ama Compose Spec'in inceleme listesi hâlâ eski hâli denetliyor:

> *"**Tek klavye** — Sayısal klavye yalnızca onay kartındaki fiyat alanında;
> **Ekle'deki arama dışında** metin girişi yok."*

**Gerçek.** Karar 40'ın istediği market seçici arama alanı, Compose Spec'in
inceleme maddesine göre **reddedilir**. Ve değişmezin adı ("tek klavye") artık
dört alanı tarif ediyor.

**Soru.** İnceleme maddesi karar 40'a göre güncelleniyor mu? Ve değişmez
yeniden adlandırılıyor mu — öneri: *"Sayısal klavye tek yerde; metin girişi
yalnızca arama alanlarında."*

> **Neden soruluyor:** inceleme listesi *"bir kod incelemesinde tek başına
> yeterli olsun"* diye yazıldı; bugün doğru kodu reddediyor.

### 32. Liste altındaki giriş: buton mu, yazılabilir alan mı?

İki dosya iki farklı bileşen tarif ediyor:

> Bağlantı matrisi: *"Liste → Ekle · tetikleyici: alt girişteki **'+ Ürün
> ekle'**"* — yani bir hedef, sheet açıyor.
>
> Tasarım sistemi: *"**Hızlı ekleme girişi** · `add` **Ne lazım?** · rest ·
> odaklı (2dp primary) · devre dışı"* ve üstünde otomatik tamamlama listesi
> (*"yoğu"* yazılmış hâli; altında Yoğurt 1 kg / Yoğurt 2,5 kg / Süzme Yoğurt
> 750 g, her satırda son ödenen fiyat).

Birincisi bir buton, ikincisi **odak hâli olan, içine yazılan bir alan**.

**Soru.** Alt giriş yazılabilir mi? Yazılabilirse *"Hiçbir ekran açılırken
klavye açmaz"* korunuyor ama kök ekranda **dördüncü** bir metin alanı doğuyor
ve otomatik tamamlama Ekle sheet'iyle aynı işi yapıyor (gereksiz karmaşa).
Yazılamıyorsa Tasarım sistemi'ndeki üç hâl ve otomatik tamamlama maketi
düşüyor.

> **Neden soruluyor:** kök ekranın birincil ekleme yolu bu ve iki dosya iki
> farklı bileşen çiziyor — biri kodlanınca diğeri yanlış olur.

---

## §7 · Bayat ayna — küçük, ama açık case bırakmamak için

### 33. Beş küçük tutarsızlık

**33a · Tasarım sistemi §04 hâlâ eski ikon setini anlatıyor.** Bölüm
*"Material Symbols Rounded · weight 400 · opsz 24"* diyor ve örnek olarak
**`shopping_basket`** çiziyor — karar 32 seti Phosphor'a taşıdı, karar 34
`shopping_basket`'i envanterden **düşürdü**. Bölüm iki kez bayat.

**33b · Karar 2'nin `where` alanı yanlış.** Karar defterinde *"Verilerimi sil
ile sıfır dialog"* kararının yeri **"Ekran 4 · etiket çekimi"** yazıyor;
doğrusu Ekran 7 · Ayarlar → Gizlilik.

**33c · `search` ikonunun yeri market seçiciyi saymıyor.** Envanter
*"nerede: Ekle sheet'i, ürün seçici"* diyor; karar 40 üçüncü bir arama alanı
ekledi.

**33d · Sparkline sekiz gözlem mi dokuz mu?** Bileşen kütüphanesi *"son **8**
gözlemin 24×16dp sparkline'ı"* diyor; ekran okuyucu cümlesi *"**son 9
gözlem**, yükseliyor"* diye örnekleniyor.

**33e · "Hiçbir yerden Kurulum'a dönülemez" — ama bir kenar var.** Bilerek
olmayan kenarlar listesi böyle diyor; bağlantı matrisi ise *"Verilerimi sil →
Kurulum · tetikleyici: 'Verileri sil' onayı"* kenarını taşıyor. Cümle
muhtemelen *"tamamlanmış kurulum bir daha açılmaz"* demek istiyor.

**Soru.** Beşi de ayna tazelemesinde düzeltiliyor mu?

> **Neden soruluyor:** ayna denetlenmeli, güvenilmemeli — beşinci turun dersi.
> Bunlar tek tek küçük ama ikisi (33a, 33c) doğrudan kod envanterinin kaynağı
> olan dosyalarda.

### 34. B6 · Ekran okuyucu ve haptik — iki vaat, sıfır altyapı

**Tasarımın verdiği.**

> *"Fiyat çipleri **'38 lira 50 kuruş'** olarak okunur; sparkline tek cümleye
> indirgenir: 'son 9 gözlem, yükseliyor'."*
>
> *"**Haptik** — işaretlemede hafif tık; kaydetmede tek darbe; hata yok.
> Haptik uygulamanın **tek ses kanalı** — ses efekti yok."*

**Gerçek.** Projede haptik API'si **yok** — ne `expect fun`, ne Android
`actual`. Ekran okuyucu cümlesi ise ölçümle çarpışıyor: 27 BİM etiketinin
**21'inde kuruş hiç okunmuyor**, yani fiyatların çoğu `149,00` gibi tam lira.
*"Yüz kırk dokuz lira sıfır kuruş"* okumak yanlış olmaz ama gereksiz.

**Soru.**
1. Kuruşsuz fiyat nasıl okunuyor — *"149 lira"* mı, *"149 lira 0 kuruş"* mu?
2. Haptik iki olayla mı sınırlı? Madde 25 çekim anına üçüncü bir darbe
   öneriyor; onaylanırsa *"tek ses kanalı"* cümlesi üç olayı sayar.

> **Neden soruluyor:** ikisi de sözleşmede **vaat** olarak duruyor, yani
> kodlanacak. Kuruş kuralı ölçüme takılıyor ve haptik hiç yazılmadı.

### 35. B3 / B4 · Chevron çizili, seçici yok

**Durum kaydı, soru değil.** Onay kartının Ürün / Marka / Market satırları
`chevron_right` taşıyor — *"dokunulabilirliğin tek işareti"*. Kodda Ürün ve
Market satır altında açılıyor, **Marka hiç düzenlenemiyor** ve karar 39'un çip
sheet'i yazılmadı.

Bunlar tasarımda tam tarif edilmiş (karar 39, karar 40) ve **kuyrukta**; bu
turun sorusu değil. Yalnız madde 6 (C5) marka sheet'inin içeriğini
değiştirebilir — o cevap gelmeden yazılmamalı.

> **Neden soruluyor:** girdi listesinde açık case olarak duruyordu; kapatılıyor
> ve bağımlılığı (madde 6) işaretleniyor.

### 36. Delta çipinin oku: envanterde ikon, `docs/11`'de eski sayı

**Tasarımın verdiği.** Karar 34 envanteri **on yediye** sabitledi ve
`arrow_upward` / `arrow_downward` bu on yedinin içinde: *"Yalnızca çipin
içinde, 12sp metinle birlikte; çipin rengini alır."*

**Gerçek — ve bu bir düzeltme.** `docs/11` karar 34 satırını hâlâ *"⏳ F11.29
(15 taşındı)"* diye işaretliyor; kodda **on yedi ikon var** (`NeydiIcon.kt`
atlas önizlemesi on yediyi listeliyor). F11.29 kapanmış, `docs/11` bayat.

**Soru yok** — yalnızca `docs/11`'in satırı ✅'ye çevrilecek.

> **Neden soruluyor:** girdi listesinin dayandığı dosya bu; bayat kalırsa
> gelecek turda case olarak tekrar açılır.

---

## §8 · İzlenebilirlik — girdi listesinin her case'i

| Girdi | Nerede |
|---|---|
| A1 · Deklanşör geri bildirimi | **25** |
| A2 · Açık temada koyu kart | **29** (+ 19) |
| B1 · Kırpılmış etiket görüntüsü | **28** |
| B2 · 1,5 sn iskelet eşiği | **30** |
| B3 · Ürün / Marka / Market seçicileri | **35** |
| B4 · Marka çip sayfası | **35** (+ 6) |
| B5 · Yatay düzen | **27** |
| B6 · Ekran okuyucu / haptik | **34** |
| C1 · Vazgeç kaldırıldı | **2** — gerekçe yanlış çıktı |
| C2 · Çözülmemiş zincirde alan doldurulmuyor | **3** |
| C3 · Manşet/birim çelişkisi | **4** |
| C4 · Migros'ta ad okunmuyor | **5** |
| C5 · Marka/ad sözcük kapısı | **6** |
| C6 · E15 kabul ölçütü | **7** |
| D1 · Money fiyatı | **8** — doğrulandı |
| D2 · Çok-al etiketleri | **9** — doğrulandı |
| D3 · packSize / priceUnit / unitPriceMinor | **10** — doğrulandı, iddiadan geniş |
| D4 · Aynı ürünün ikiye bölünmesi | **11** — doğrulandı |

**Denetimin kendi bulduğu on dokuz case:** 1 (gözlemin geri dönüşü),
12 (amber iki anlam), 13 (iki yeşil), 14 (44 / 48dp), 15 (60 sn / aynı dakika),
16 (gözlem fiyatı ve tilde), 17 (kategori kutucuğu iki harf), 18 (ikon rengi),
19 (iki amber token), 20 (fiyat çipi kenarı), 21 (market düzeltilemiyor),
22 (şube), 23 ("Nerede ucuz" eşiği), 24 (Metro), 26 (flaş), 31 (tek klavye
incelemesi), 32 (alt giriş), 33 (beş bayat ayna maddesi), 36 (on yedi ikon).

---

## Kapsam notu

Otuz altı case incelendi ve hepsi bu dosyada — **açık case bırakılmadı.**
Otuz ikisi tasarımdan cevap bekliyor; ikisi durum kaydı (35, 36), dördü ise
iddiaydı ve dördü de doğrulandı (8–11).

Öncelik sırası, cevap gelmezse ne bloke olduğuna göre:

1. **Madde 1** — yanlış gözlemin geri dönüşü. E17 fiyat geçmişini çizmeye
   başlamadan cevaplanmalı; sonrası göç işi olur.
2. **Madde 10 (D3)** — kolon kararı. E17 başlamadan.
3. **Madde 3, 4, 5, 6** — E15'in kalan yarısını (marka sheet'i, desteklenmeyen
   zincir hâli) bunlar belirliyor.
4. **Madde 2, 25, 28** — onay kartının çizimi. Üçü de aynı ekranda ve birlikte
   cevaplanmalı.
5. Geri kalanı paralel ilerleyebilir.
