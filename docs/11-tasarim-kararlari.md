# Tasarım kararları — koda çevrilmiş hâli

**16 Ağustos 2026.** Design pivot turunu tamamladı ve karar defterini **yirmi
maddeye** indirdi: fiş dönemine ait on bir karar (4, 9, 13–21) defterden
tamamen çıkarıldı. İki de yeni dosya geldi — **Gezinme sözleşmesi** ve
**İkonografi**. Altıncı turda defter **45 karara** çıktı.

Bu dosya karar defterinin kopyası değil; **her kararın kodda karşılığı ne, hangi
adımda yapılıyor** onu söylüyor. Kararların kendisi ve gerekçeleri
[`tasarim/Neydi - Kararlar.dc.html`](tasarim/) altında.

> **Ayna durumu: temiz** (altıncı tur sonrası). Dokuz `.dc.html` ile
> `github.md` yeniden indirildi; **dokuzunun dokuzu da değişti** — silme
> jesti ve geri alma çerçeveleri, marka çip sheet'i, market seçicide arama
> alanı, Ürün Detayı'na "Listeden çıkar", envanter 17.
>
> **Ayna denetlenmeli, güvenilmemeli.** Önceki turda ROADMAP F11.12'yi
> "tazelendi" diye kapatmıştı ama bayt kopyası indirilmemişti: dosyada hâlâ
> `undo`/`filter_list` ve sıfır `~` tutar vardı. Tasarımın yapması ile bizde
> olması ayrı iki olay; ✅ ancak ikincisinden sonra yazılır.
>
> **Ayna aynanın kendisiyle de çelişebiliyor** (yedinci tur, `docs/20`). E15
> sonrası denetim dokuz dosyayı karşılaştırdı ve aynı sayının/kuralın iki
> dosyada iki türlü yazıldığı **on üç yer** buldu: iki yeşil, 44dp / 48dp,
> 60 sn / "aynı dakika", ikon renginin üç ayrı kuralı, kategori kutucuğunda
> ürün mü kategori mi baş harfi. Bu dosyanın da iki satırı yanlış aktarımdı
> — biçim ve değişmez satırları aşağıda düzeltildi.
>
> Canlı kaynak:
> [design projesi](https://claude.ai/design/p/8eea982a-c3f6-4008-8789-81aaf478b51d).

---

## Kırk beş kararın kod durumu

| # | Karar | Kod durumu |
|---|---|---|
| 1 | Alışveriş modunda `more_vert` → tek madde "Alışverişi bırak" | ✅ var |
| 2 | "Verilerimi sil" tam ekran onay destinasyonu; kapsamda fotoğraf yok | ✅ var · kapsam E11'de düzeldi |
| 3 | Toolbar iki hedef (`add` + Bitir) | ✅ var · ikonlar bu turda silindi |
| 5 | İlk gün 12 ürün çipi, `commonalityRank`'tan | ✅ var |
| 6 | Kurulum iki adım; tetikleyici `setupCompletedAt` + ürün sayısı | ✅ var |
| 7 | Ekran 3'ün üç bölüm notu | ✅ var |
| 8 | Toast: aksiyonsuz, 2 sn, kuyruksuz. **Gerekçesi düzeltildi:** snackbar artık **iki yerde** (kapanış + "Geri al") | ✅ var · KDoc **F11.24** |
| 10 | Avatar tek kişilik hanede de; `priceChip` 14sp / `priceRow` 17sp | ✅ var |
| 11 | **Revize:** 7 zincir tohumlanır, market çekimde seçilir, yapışkan | ✅ E13 · yapışkan seçim **E15** |
| 12 | Ekle sheet'indeki işaret "bu listede var" | ✅ var |
| 22 | Zincir adı etiketteki gibi büyük harf, caps satır 500 ağırlık | ✅ var (locale'siz dönüşüm zaten yasak) |
| 23 | Zincirler satırından chevron kalktı | ✅ **bu turda** |
| 24 | Katılma kodu soluk "Faz 7'de açılıyor" *(Mağazalar yarısını **karar 36** geçersiz kıldı)* | ✅ var |
| 25 | Onay kartı fotoğrafın üstünde; eksik alan amber şerit; dışına dokunmak kapatmaz | ⏳ **E15** |
| 26 | "Nerede ucuz" satırının kimliği **market + marka** çifti | ⏳ **E17** |
| 27 | Etiket çekimine tek giriş: **liste başlığında kamera hedefi** | ⏳ **E15** |
| 28 | Alışveriş başlığındaki market = o gezide son çekilen etiketin marketi | ⏳ **E18** |
| 29 | Fotoğraf Kaydet'e basıldığı anda silinir; hiçbir yüzey çizmez | ⏳ **E15** · planla aynı |
| 30 | Geçmiş'te gözlem satırı yok; gezi satırı tarih + kalem + `~` tutar | ✅ E8 · `~` tutar **E18** |
| 31 | Boş durum 04 kategorisi değişmedi, yalnızca gerekçe metni | ✅ tasarım tazeledi |
| **32** | **İkon A yolu düştü** — ikonlar Phosphor Regular çizimleriyle elle `ImageVector`; **`Text` olarak çizilmiyor** | ✅ F11.11 · oklar **F11.29** |
| **33** | **Yeniden yazıldı:** kural mutlak renk değil **ilişki** — ikon yanındaki metinden bir kademe açık; palet değişmiyor | ✅ **okumamız onaylandı** |
| **34** | Envanter **17** — delta çipinin `arrow_upward`/`arrow_downward` okları girdi; `check` ile `check_circle` ayrı | ✅ **F11.29** · 17 taşındı |
| **35** | Gizlilik notu + katılma kodu metni onaylandı | ✅ **birebir uygulandı** |
| **36** | **Mağazalar bölümü kalıyor**, eşik kalktı; satır "Zincirler"; gözlemi olan zincir metin renginde, yalnızca seçilebilir olan soluk | ✅ **bu turda** |
| **37** | Satır silme: sağdan sola swipe, **yalnız plan modu ve alınmamış satır**; geri alma 5 sn snackbar, aksiyon **"Geri al"** | ⏳ **F10.9** |
| **38** | Jestsiz eş **taşma menüsünde değil**, Ürün Detayı'nın son satırı: error renkli "Listeden çıkar" | ⏳ **F10.9** |
| **39** | Marka satırı **klavyesiz çip sheet'i** açıyor: görülmüş markalar + "Marka yok"; OCR tahmini kesik çerçeveli | ⏳ **E15** |
| **40** | Market seçici ürün seçicinin ikizi: arama alanı + `+ Yeni market «AKYURT»`. Tek-klavye istisnası artık **"arama alanları"** | ⏳ **E15** |
| **41** | "Başka markette ucuz" çipi: **≥%10 VE ≥5 TL**, karşı gözlem **14 günden eski değil**; çip trendi bastırır | ⏳ **F5.5/E16** |
| **42** | `#B34418` dolgu = ileri götüren birincil, `#3F6B54` = onay/bitirme, `#8A7666` kenarlık = üçüncü | ✅ kod zaten öyle |
| **43** | Delta çipi ve trend oku kırmızı/yeşil; §11'in kırmızı "asla"sından *"fiyat artışı"* kalktı | ✅ kod zaten öyle |
| **44** | "Nerede ucuz"da ambalaj boyu **filtre**; geriye **2 market** kalmazsa bölüm çizilmez | ⏳ **E17** |
| **45** | 36sp manşet düşüyorsa özet kartı **hiç görünmüyor** | ⏳ **F11.23** (E18 ile) |

**Karar 29 planı doğruladı:** fotoğrafın kayıttan sonra silinmesi benim önerimdi
ve açık karar olarak duruyordu — design aynı sonuca vardı, gerekçesi de aynı:
etiket bir ödeme kanıtı değil, bir fiyatın okunduğu andır. Açık kararlardan
düştü.

---

## Gezinme sözleşmesi (yeni dosya) — kodlanacak sabitler

Ekran çizimleri *neyin göründüğünü*, bu dosya *ne olduğunu* söylüyor. E15'in
ihtiyacı olan her şey yazılı.

### Geri tuşu sırası — tek basış, bu sırayla

1. Klavye → 2. Sheet → 3. Onay kartı → 4. `more_vert` menüsü →
5. Bir üst destinasyon → 6. Uygulamadan çık

**Geri asla:** "kaydedilmemiş değişiklikler" sormaz · alışveriş modunu
kapatmaz · sheet ile arkasındaki destinasyonu aynı basışta kapatmaz · toast'ı
erken kapatmaz · kökte "çıkmak için tekrar bas" göstermez.

### Eşikler — hepsi kodlanacak sayı

| Yüzey | En az | Altında | Kod |
|---|---|---|---|
| Ürün Detayı sparkline | **3 gözlem** | Grafik hiç çizilmez | ✅ bu turda |
| "Nerede ucuz" bölümü | **2 market** | Bölüm çizilmez |
| Delta çipi | **2 gözlem** | Çip yok, "ilk gözlem" ibaresi de yok |
| Ambalaj küçülmesi | **2 farklı boy** | Sessiz |
| Sepet tahmini | **3 fiyatlı ürün** | Satır hiç görünmez | ✅ bu turda |
| "Her zamankiler" öğrenmesi | **3 gezi** | Kurulumdaki seçim neyse o |
| "Bitmiş olabilir" | **4 alım** | Bölüm çizilmez |
| Eksik olabilir ekranı | **1 satır** | Ekran açılmaz, toast bilgilendirir |
| Geçmiş grafiği | **3 gezi** | Çubuklar çizilmez |

### Tarih merdiveni (F5.11'in eksik yarısı — artık tam)

`0–6 saat` → "az önce" · `bugün` → "bugün 15:38" · `1 gün` → "dün" ·
`2–6 gün` → "3 gün önce" · `7–13 gün` → "geçen hafta" · `14+ gün` →
"12 Ağustos" (yıl yalnızca farklı yılsa)

### Biçimler

`1.085,65 TL` (binlik nokta, ondalık virgül, TL sonda boşlukla) ·
**tahmin `~642 TL`** — tilde bitişik, **kuruş yazılmaz** ·
birim fiyat `92,48/lt` · ağırlık `1,206 kg` (üç ondalık yalnızca tartıda) ·
sayaç `12/18` boşluksuz · saat `15:38` 24 saatlik.
**Gözlem fiyatı `100,00 TL`** — etiketten okunan tek fiyat kesindir, tilde
almaz, kuruş yazılır. Tilde yalnızca **ondan türetilen** tutarlarda (sepet,
gezi, ortalama).

> **Düzeltme (yedinci tur).** Burada önceden *"kesin tutar diye bir biçim yok"*
> yazıyordu; bu tasarımın biçim tablosunun yanlış aktarımıydı. Tablo
> *"gözlem fiyatı — 100,00 TL · etiketten okunan tek fiyat kesindir, tilde
> almaz, kuruş yazılır"* diyor ve değişmez de aynı ayrımı taşıyor. Compose
> Spec'in inceleme listesi hâlâ *"her tutarın önünde ~"* diyor — o çelişki
> tasarıma soruldu (`docs/20`, madde 16).

### Etiket akışı (E15'in sözleşmesi)

- Deklanşör → kare alınır, kırpılır, OCR başlar, **kart hemen açılır** (boş alanlarla)
- OCR **1,5 sn**'yi geçerse alanlar iskelet olur, kart beklemez
- Fiyat boşsa **klavye kendiliğinden açılır**, Kaydet ilk rakamda etkinleşir
- Kaydet → gözlem yazılır, fotoğraf silinir, **kamera 300 ms içinde hazır**, toast 2 sn
- Kaydet sırasında geri basılırsa **kayıt tamamlanır**, iptal edilmez
- Vazgeç onay **istemez** — çekim ucuz, tekrarı bir dokunuş
- Seri çekimde kuyruk yok: önceki kart kapanmadan kamera çalışmaz
- **Aynı market + ürün + fiyat 60 sn içinde tekrarlanırsa ikinci gözlem yazılmaz** → F5.10'un cevabı

### Hata yolları

Kamera izni reddedildi → Liste + toast "Kamera izni olmadan etiket çekilemez" ·
kalıcı reddedildi → tek satırlık yüzey "Kamera izni kapalı" + "Ayarları aç" ·
OCR hiçbir şey okuyamadı → kart yine açılır, amber şerit "fiyat okunamadı — yaz" ·
depolama dolu → kart açılmaz, toast · çevrimdışı → **hiçbir şey**.

### Değişmezler

Tek modal dialog yok · boş bölüm çizilmez, boş ekran açılmaz · geri her zaman
bir şey kapatır, asla soru sormaz · alışveriş modu gezinin durumudur ·
**etiket fotoğrafı kayıttan sonra silinir** · **tek gözlem fiyatı kesindir,
ondan türetilen her tutar `~` alır** · **marka gözlemin alanıdır** · aynı
etiket metni aynı markette bir kez sorulur · işaretleme snackbar açmaz · toast
kuyruğu yoktur.

---

## İkonografi (yeni dosya)

Envanter **18 → 12**'ye iniyor. Düşenler: `receipt_long`, `error_outline`,
`functions`, `zoom_in`, `content_copy`, `shopping_basket`.

Kalan 12: `add` · `photo_camera` · `more_vert` · `arrow_back` · `close` ·
`search` · `check_circle` · `chevron_right` · `expand_more` · `logout` ·
`bolt` · `info`

**Yapıldı:** kod envanteri önce **23 → 13**'e indi (silinenler: `ArrowUpward`,
`ArrowDownward` — DeltaChip kendi okunu çiziyor —, `Undo`, `FilterList`
(karar 3), `Functions`, `ContentCopy` (karar 24), `LightMode`, `DragIndicator`,
`HourglassTop`, `Error`), sonra karar 34 ile **17**'ye sabitlendi.

Karar 34 beş ikonu geri getirdi: `push_pin`, `check`, `content_paste` — üçü de
kullanımdaydı ama tasarım envanterinde yoktu — ve delta çipinin iki oku,
`arrow_upward` / `arrow_downward`. Oklar bir ara silinmişti (*"DeltaChip kendi
okunu çiziyor"*); karar 34 onları envantere geri koyunca çizim de setin içine
alındı, çünkü çipin oku Phosphor değilse denetlenmemiş bir çizim olarak kalır.
`check` ile `check_circle`
**ayrı kalıyor** ve bu kasıtlı: çıplak `check` satırda *"işaretlendi"*,
`check_circle` çipte/seçicide *"seçili"*. İkisini tek ikona indirmek iki farklı
fiili aynı sözcükle söylemek olurdu.

`bolt` ve `info` **şimdi yazıldı**, çağıranları E15'te gelecek. Daha önce
"eklemek ölü kod olurdu" denmişti; karar 34 envanteri sabitleyince tercih
değişti — seti tanımlayan taşımada iki ikonu dışarıda bırakmak, E15'te aynı
elle-taşıma işini ikinci kez açmak demekti.

### ✅ A yolu düştü, set Phosphor'a taşındı (F11.11)

Design'ın önerdiği A yolu tek bir `IconDefaults` istiyordu: **24dp, wght 300,
opsz 24, açık temada GRAD 0, karanlıkta GRAD 100**. Bunlar **Material Symbols
değişken fontunun eksenleri**; uygulamanın kullandığı `androidx.compose.material
.icons` ise derlenmiş **statik `ImageVector`** veriyor — ekseni yok. Yani A
yolunun tek avantajı olan *ucuzluk* gerçek değildi: ekseni gerçekten çalıştırmak
fontu paketleyip ikonları `Text` olarak çizmeyi gerektirirdi ve o yol Fraunces'te
bir kez reddedilmişti (iOS'ta `FontVariation` güvenilir değil).

Design bu itirazı kabul etti (karar 32) ve gerekçesinde aynı akışı kullandı:
kalan iki seçenek **aynı mekanik işi** istediğine göre, kimlik kazancı olan
taraf seçilir. Sonuç: **17 ikon Phosphor Regular 2.1.1 (MIT) çizimleriyle elle
`ImageVector` olarak taşındı.**

Kazanç yalnızca kimlik değil: `material-icons-extended` bağımlılığı tamamen
düştü — o artifact JetBrains tarafında **1.7.3'te donmuştu** ve tek kullanıcısı
`NeydiIcon.kt`'ydi. Şimdi 17 path dizesi, birkaç KB kaynak.

**Ara katman sınandı ve tuttu:** set baştan sona değişti, `NeydiIcons.ArrowBack`
diyen 17 çağrı yerinin **hiçbiri** değişmedi. `NeydiIcons`'un varlık sebebi tam
olarak buydu ve ilk kez gerçek bir taşımada ödendi.

**Elle taşımanın iki sessiz hata modu** teste bağlandı: kırpılmış bir `d` dizesi
boş vektör üretir ve hiçbir şey şikâyet etmez; satır kopyalanıp path değiştirmeyi
unutmak iki ikona aynı çizimi verir. `NeydiIconsTest` ikisini de yakalıyor.
Testin *ölçemediği* şey çizimin ne olduğu — onun için `NeydiIcon.kt`'de on yedi
ikonun `@PreviewLightDark` atlası var, ve beşi cihazda gözle doğrulandı.

---

## Dördüncü tur kapandı ✅

`12-tasarima-sorular-4.md`'nin beş sorusunun hepsi cevaplandı (karar 32–35 +
atlas tazelemesi). **Teknik itiraz tuttu:** design A yolunu düşürdü ve
gerekçesinde bizim argümanımızı birebir kullandı — eksenler yalnızca değişken
fontta yaşıyor, A'nın tek avantajı olan ucuzluk gerçek değildi, font paketleme
Fraunces'te zaten reddedilmişti, ve kalan iki seçenek aynı mekanik işi
istediği için kimlik kazancı olan taraf seçildi.

## Beşinci tur kapandı ✅ — dördünün dördü de cevaplandı

| Soru | Cevap | Kod |
|---|---|---|
| **F11.15** Mağazalar eşiği | **Karar 36**: bölüm kalıyor, eşik satırı tablodan kalktı, etiket "Zincirler", gözlem ayrımı **renkle** | ✅ bu turda |
| **F11.13** Ekran 1 başlık örneği | Tasarım merdivene uydu: *"geçen hafta · ~642 TL"* | ✅ kodda iş yok, `formatRelativeDay` zaten doğruydu |
| **F11.14** Karar 33'ün renkleri | Karar **ilişki olarak yeniden yazıldı**; okumamız birebir benimsendi | ✅ `Color.kt` değişmedi, çekince silindi |
| **F11.12 / F11.16** Bayat ayna | Atlas ve ekran haritası tazelendi, **İkonografi ilk kez geldi** | ✅ dokuz dosya indirildi |

**Karar 36, önerdiğimizden bir adım ileri gitti.** Biz *"bölüm kalsın, yanlış
olan tek şeyi — iddiayı — düzeltelim"* demiştik. Tasarım bunu kabul etti ve
üstüne **gözlem ayrımını renkle** ekledi: gerçekten fiyat kaydedilmiş zincir
metin renginde, yalnızca seçilebilir olan soluk.

Bu ekleme, gizleme seçeneğinin (a) kaybedeceğinden korktuğumuz bilgiyi geri
getiriyor — *"hangisini gerçekten takip ediyorum"* sorusu, bölüm görünür
kalırken de cevaplanıyor. Kararın kendi ifadesi: **"tek liste, tek satır, yeni
bileşen yok."**

Kodda karşılığı: `hasObservation` bayrağı (yeni `DISTINCT storeId` sorgusundan),
tek `AnnotatedString` içinde iki `SpanStyle`, ve **gözlemliler önce** sıralaması.
Sıralama kararın metninde yok ama dolu makette var — ve gerekli: ayrımın tek
taşıyıcısı renk olsaydı renk görmeyen kullanıcıya hiç ulaşmazdı.

## Açık kalan iki küçük madde

**1 · Ekran 1'in beşinci çerçevesi hâlâ tildesiz.** Dört maket *"~642 TL"*
oldu ama biri *"Son alışveriş: bugün · 642,50 TL"* — tilde yok, kuruş var.
**Gezi toplamı türetilmiş bir tutar** olduğu için biçim kuralına aykırı; tek
gözlem fiyatı olsaydı doğru olurdu. → **F11.17**

**2 · İkonografi dosyası karar 33'ü eski çiftiyle örnekliyor.** Karar defteri
ilişkiyi doğru yazıyor (*"ikon yanındaki metinden bir kademe açık"*) ama
İkonografi aynı kuralı hâlâ *"metin `#E4D8C9`, ikon `#F5EDE6`"* diye
örnekliyor. İki ayna kuralda hemfikir, örnekte değil; defter esas alındı.
→ **F11.18**

> **Yedinci tur bunu genişletti.** Çelişki yalnızca örnekte değil, **kuralın
> kendisinde**: aynı İkonografi dosyası bir yerde *"ikon içinde bulunduğu
> metnin rengini alır"*, başka yerde delta oku için *"çipin rengini alır"*
> diyor, karar 33 ise *"bir kademe açık"* diyor. Üç kural, tek dosya. F11.18
> kapatılırken üçü birden tek cümleye indirilmeli — `docs/20` madde 18.

---

## Yedinci tur açıldı — denetim çıktısı `docs/20`

E15 cihazda koştu ve on iki gözlem üretti; girdiler
[`19-tasarim-denetimi-girdileri.md`](19-tasarim-denetimi-girdileri.md)'de
toplandı, denetim [`20-tasarima-sorular-7.md`](20-tasarima-sorular-7.md)
olarak yazıldı: **36 case, 32'si tasarımdan cevap bekliyor.**

Karar defterini doğrudan ilgilendiren, cevap gelmeden kodlanamayacak dört
madde:

| Konu | Etkilediği karar | `docs/20` |
|---|---|---|
| Yazılmış gözlem düzeltilemiyor / silinemiyor — `deletedAt` var, kapı yok | 26 · 29 | **1** |
| Onay kartında "Vazgeç" — tasarım üç yerde istiyor, kodda yok | 25 | **2** |
| Marka sheet'i klavyesiz, marka yalnızca OCR'dan gelebiliyor (kısır döngü) | 39 · 26 | **6** |
| Birim fiyatın **tutarı** için kolon yok; Ekran 5 iki sayıyı birden çiziyor | 44 | **10** |

---

## Yedinci tur kapandı ✅ — kararlar 46–63

Canlı tasarım projesi 18 Ağustos'ta yedinci turu cevapladı: **36 case ve 13 iç
çelişki kapandı, tasarıma sorulacak açık madde kalmadı.** Aşağıdaki tablo her
kararın **koddaki** durumunu tutuyor.

| # | Karar | Kod | Nerede |
|---|---|---|---|
| 46 | Gözlem uzun dokunuşla silinir, 5 sn geri alma | ✅ | `ProductSheet.kt`, `ListViewModel.deleteObservation` |
| 47 | Kaydedilen fiyat = etiketin manşet fiyatı | ✅ | `TagGrammar` (zaten böyleydi) |
| 48 | Metro sekizinci zincir, tohuma girer | ❌ **bilinçli sapma** | aşağıda |
| 49 | Desteklenmeyen zincirin tek cümlesi, şerit yok | ✅ | `ConfirmCard.unsupportedChainMessage` |
| 50 | Güvenilmez fiyat: cümle + kesik çerçeveli sayı | ⚠️ **yarısı** | aşağıda |
| 51 | Ürün kimliği katalogdan; OCR metni asla ad değil | ✅ | `ProductPicker`, `ConfirmCard.tagText` |
| 52 | Marka havuzu markete genişler, sheet klavyesiz | ✅ | `BrandPicker`, `brandsSeenAt` |
| 53 | İlk gözlemin izi fiyat çipi; sepet eşiği 3 | ✅ | zaten öyleydi (`PriceHint.Single`) |
| 54 | Birim fiyat kendi kolonunu alır | ❌ | Faz 4 (şema) |
| 55 | 120 ms örtücü flaşı + haptik, üç olay | ✅ | `TagCaptureScreen` |
| 56 | En küçük dokunma hedefi tek sayı 48dp | ✅ | `Sizes.minTapTarget` |
| 57 | Amber tek anlam; ucuzluk kiremit çipe geçti | ✅ | `CheaperChip` |
| 58 | Şube yok; "Nerede ucuz" eşiği 2 satır | ✅ | `PriceSection.MIN_ROWS` |
| 59 | Yeni market ikinci dokunuş ister; gözlemsiz market silinir | ✅ | `StorePicker`, `deleteStore` |
| 60 | Flaş: iki hâl, oturumluk | ✅ | `CaptureController.torch` |
| 61 | Yatayda kart sağ yarıda dikey panel | ❌ | aşağıda |
| 62 | Vizör koyu / kart açık; doğrulama kırpımda | ✅ | `FlowPalette`, `TagThumbnail` |
| 63 | Alt giriş bir buton | ✅ | `QuickAdd` |

### Karar 50 — yarısı alındı, gerekçesi ölçüm

Karar *"alan boş gelmez; okunan sayı kesik çerçeveli gelir"* diyor ve gerekçesi
*"düzeltilecek bir şey vermek sıfırdan yazdırmaktan ucuz"*. Bu gerekçe okunan
sayının **yaklaşık doğru** olduğunu varsayıyor. Ölçülen üç vakada öyle değil:

| Fikstür | Okunan | Gerçeği |
|---|---|---|
| `20260817_183746` | 6,00 | 106,00 |
| `20260817_183847` | 7,50/hg | 57,50/kg |
| `20260817_211219` | 799,50 | 79,95 |

İlk satır tehlikeli olanı: **6,00 TL makul görünüyor** ve kesik çerçeve fark
edilmeden kaydedilebilir. Karar 49'un kendi gerekçesi *"yanlış fiyat, fiyat
olmamasından kötü"* diyor; 50 ile 49 burada çelişiyor ve ölçüm 49'un yanında.
**Cümlesi alındı, sayıyı göstermesi alınmadı.** Gerekçe
`TagSkip.PRICE_CONTRADICTS_UNIT_PRICE` KDoc'unda; tasarıma bildirilecek.

### Karar 48 — Metro

Karar Metro'yu sekizinci zincir yapıp tohuma koyuyor. **Kullanıcı "Metro'yu
boş ver" dedi** ve öncelik listesine almadı (FullGross, Gimat, BİM, A101, ŞOK,
Tarım Kredi, Migros). Kullanıcının kendi alışveriş sırası, tasarımın
"kullanıcının gerçekten etiket çektiği zincirler" ölçütünden daha yeni bir
bilgi. `StoreSeed.SEED_CHAINS` gerekçesiyle birlikte Metro'yu dışarıda
bırakıyor.

### Kalanlar

- **Karar 61 (yatay düzen)** — kart bugün her yönde alta yapışık.
- **Karar 54 (birim fiyat kolonu)** — Faz 4, Room otomatik migrasyon.
- **Süreç ölümünde kart** — `SavedStateHandle` yok; sözleşme kartın aynı
  değerlerle dönmesini istiyor.
- **Karar 55'in ikinci yarısı** — yakalanan karenin küçülerek kırpıma uçması
  (260 ms). Flaş ve haptik var, uçuş yok.

---

## Dokuzuncu tur — kararlar 64, 66–69 · **karar 63 defterden düştü**

Tasarım projesi 19 Ağustos'ta `docs/22` ve `docs/23`'teki soruların hepsini
cevapladı. Defter artık **elli altı geçerli karar** taşıyor.

### Karar 63 geri alındı — tasarımın kendi cümlesiyle

> *"63'ün 'aynı işi yapan ikinci yol' teşhisi yanlıştı; **bunu biz koyduk, biz
> düzeltiyoruz**."*

Karar 63 kökteki metin alanını butona çevirmişti; PR #77'de kodladım ve
kullanıcı cihazda bildirdi: yazarak ekleme 1 dokunuştan 3'e çıkmıştı, on
kalemlik bir turda otuz fazladan dokunuş. `docs/22` bunu ölçümle sordu, karar
64 kararı düşürdü.

| # | Karar | Kod |
|---|---|---|
| **64** | Ekleme iki yola ayrıldı: kökte yazma, sheet'te keşif | ✅ |
| **66** | Ekle kataloğunda fiyat çizilmez — alt satır her zaman birim | ✅ zaten öyleydi |
| **67** | Trend manşetinin üç kuralı (aralık veriden, kuruşsuz tam lira, ambalajda yüzde yok) | ✅ |
| **68** | Geçmiş grafiği **tutar** ölçer; tutarsız gezi kesik konturlu kısa çubuk | ✅ |
| **69** | Alışveriş özeti listenin **içinde** kart, sheet değil | ✅ |

### Karar 64'ün iki yolu

**Yol 1 — kökte yazma.** Hedef dokunuşla *yerinde* alana dönüşüyor (yeşil 2dp
odak çerçevesi), klavye açılıyor, Enter ekliyor ve **alan açık kalıyor** (seri
ekleme). Klavyenin üstünde tek sıra öneri çipi; girdi boşken motorun
önerileri, yazarken otomatik tamamlama. Klavye kuralı bozulmuyor — klavyeyi
ekran değil kullanıcının dokunuşu açıyor.

**Yol 2 — sheet'te keşif.** Reyon kutucukları **öldü**; sheet doğrudan ürünle
açılıyor. Üstte arama, altında yatay reyon *filtre* çipleri, sonra iki bölüm:
"En sık aldıkların" iki sütunlu kutucuk ızgarası, "Nadir aldıkların" sarılan
çip. Sıralama `product_stats.purchaseCount`tan — kataloğun genel yaygınlığından
değil, **bu hanenin geçmişinden**. Izgara kesimi altı kutucuk: maketin kendi
sayısı, uydurulmuş bir eşik değil.

**Alt kaçış iki yolu bağlıyor:** "Kendim yazayım" sheet'i kapatıp kökteki alanı
odaklıyor.

### İçe aktarımda kalan boşluk

Tasarım projesinden **dokuz dosyanın altısı** tazelendi. Üçü — Boş Durumlar,
İkonografi, Compose Spec — araç tarafından satır içi döndüğü için betikle
yazılamadı; repodaki kopyaları **17 Ağustos tarihli**. Okundular ve maddi
değişiklikleri buraya işlendi, ama dosyalar bayat:

- **İkonografi** artık **19 ikon** sayıyor (karar 64 `grid_view` ve `keyboard`
  ekledi) ve etiketsiz ikon istisnası **sekize** çıktı (katalog eklendi).
  Repodaki kopya hâlâ 17 ve altı diyor.
- **Compose Spec** karar 64 ve 67'yi denetim listesine işlemiş.

Bu üçü elle yeniden indirilmeli.

---

## Onuncu tur — kararlar 70–74 · onay kartı + klavye

`docs/25`'in beş sorusunun beşi de cevaplandı. Bu turun kaynağı bir denetim
değil, **cihazda yapılan uçtan uca bir prova**: deklanşör → kart → fiyat →
ürün → market → kaydet → veritabanı.

### Karar 70 — dikeyde klavye açılınca kırpım toplanır ✅

**Kod tarafının geçici çözümü kural oldu.** Ölçüm: kart içeriği 1669 px,
klavyenin üstünde 1198 px. Kart kaydırılmıyor, Tarih satırı kalıyor; kalan her
şey kaydırmasız görünüyor.

Sözleşme bir ayrıntı daha ekledi: **şerit toplanmış doğuyor** — fiyatı
okunamamış kart klavyeyi kendiliğinden açıyor ve o anda kullanıcı fiyatı
ekrandan değil raftaki etiketten okuyor. `TagCaptureScreen`'de sınırlı bir
bekleme var (`IME_WAIT_MS`), çünkü IME birkaç yüz milisaniye sonra görünüyor
ve yalnızca ona bağlanan şerit bir görünüp sonra toplanıyordu.

### Karar 71 — Kaydet ile Vazgeç yatay çift ✅

Alt alta iki satır tek satıra indi: solda Vazgeç (metin, `flex:1`), sağda
Kaydet (dolgulu, `flex:2`), arada 10dp, satır 52dp. **Her durumda böyle.**

Gerekçe kalıcı: Vazgeç karar 29'un (fotoğraf silinir) görünür yolu ve
görünürlüğü klavyenin durumuna bağlanamaz. Kırpım toplandıktan sonra bile
Vazgeç fold'un 22 px altında kalıyordu.

### Karar 72 — kuruş uyarısı ilk düzenlemede susar ✅

**Onaylandı.** Uyarı yalnızca OCR değeri hiç ellenmediyse sürüyor ve
**Kaydet'i hiçbir hâlde engellemiyor**. `ConfirmCard.priceTouched` taşıyor.

### Karar 73 — fiyat alanı sağdan dolar ✅

Yazar kasa girişi: `3` → 0,03 · `39` → 0,39 · `3950` → 39,50. Virgül ve nokta
yok sayılıyor, ⌫ sağdan siliyor, **dolu** alanda ilk rakam değeri sıfırlayıp
baştan başlatıyor, boşalan alan `— TL`ye dönüyor.

**Sözleşmenin bir cümlesi değişti:** Kaydet artık *"ilk rakamda"* değil
**değer sıfırdan çıkınca** etkinleşiyor — `0` tuşlamak bir rakam ama bir fiyat
değil.

`parseMinorInput` kartın alanından çıktı; kart `priceMinor: Long` tutuyor.
Etiket metnini okuyan `parseMinor` yerinde — o başka bir kaynağın doğrusu.

⚠ **`parseMinorInput` artık üretimde çağrılmıyor** (yalnızca kendi testi var).
Silinmedi: para ayrıştırma veri katmanının genel API'si ve F5.4 dış veriyle
geri gelebilir. F10.11'in ölü kod listesine eklendi.

### Karar 74 — kırpım rehber bölgesinden; şerit 92dp'ye döner ❌ **yapılmadı**

Tasarım asıl düzeltmeyi onayladı: küçük kopya **rehber (3:2) bölgesinden**
kırpılacak ve *o gün* şerit maketin 92dp'sine dönecek. 128dp geçici yama.

Bu tek karar bu turda **uygulanmadı** ve sebebi kapsam: kamera hattına
dokunuyor (rehber dikdörtgenini kareye eşlemek, `PreviewView` FILL_CENTER
ölçeği, yeni bir `expect/actual` kırpma). Tasarımın kendi cümlesi de ikisini
ayırıyor — şerit ancak kaynak düzelince 92dp oluyor. **Sıradaki iş.**

### Bu turda tazelenen dosyalar

Altı `.dc.html` betikle indirildi ve uzakla **birebir** doğrulandı: Kararlar,
Ekranlar 2-4, Gezinme Sözleşmesi, Ekran 1, Ekranlar 5-8, Tasarım Sistemi.

**Compose Spec** yine satır içi döndü (araç ~40KB altını betikle yazmıyor);
denetim listesindeki **altı eksik madde elle işlendi** ve liste artık uzakla
aynı yirmi maddeyi taşıyor. Dosyanın kalanı için bkz. `OKU-BUNLAR-BAYAT.md`.

**İkonografi** kontrol edildi ve **güncelmiş** — önceki turun "bayat" notu
artık geçerli değil. **Boş Durumlar** bu turdan etkilenmedi.

### Karar 74 — kırpım rehber bölgesinden; şerit 92dp ✅

**Uygulandı.** Küçük kopya artık `cropToGuide` ile **rehberin (3:2) bölgesinden**
kırpılıyor; şerit maketin **92dp**'sine döndü.

**Eşleme `PreviewView` FILL_CENTER'in tersi** ve ortak/saf bir fonksiyonda
(`GuideBox.inImage`) — yani test edilebilir:

```
ölçek   = max(vizörGenişlik / kareGenişlik, vizörYükseklik / kareYükseklik)
görünen = vizör / ölçek
pay     = (kare - görünen) / 2
```

Cihazda ölçülen: vizör 1080×2047, kare 3024×4032 → ölçek 0,5077, karenin
**897 pikseli hiç görünmüyor** (her yandan 448). Rehber `left=22`'de başlasa
bile karede ~491'de başlıyor.

**Sıra zorunlu: önce yön, sonra kırpım, sonra ölçek.** Hesap `PreviewView`'in
gösterdiği kareye göre yazıldı; kırpım yönden önce yapılsaydı dikdörtgen
doksan derece yanlış yere düşerdi — ve bu **sessiz** bir hata olurdu, çünkü
çıkan şerit yine bir şeyler gösterirdi.

**Cihazda kanıtlandı:** kaynak kare 4032×3024 (oran 1,33), küçük kopya
**720×480 = tam 3:2**. Merkez kırpımıyla oran 3:4 kalırdı.

Kırpım başarısız olursa merkez kırpımına **düşülüyor** — yanlış yerden doğru
bir şerit, boş şeritten iyi. iOS'ta `cropToGuide` bugün her zaman `false`
dönüyor (Faz 9) ve orada şerit 92dp'de daha çok kesiyor; iOS kabuğu henüz yok,
kabul edildi.

**Onuncu tur kapandı: kararlar 70–74'ün beşi de kodda.**

---

## Denetim: tasarımın kendi listesi koda karşı *(22 Ağustos)*

Compose Spec'in **20 maddelik kod incelemesi denetim listesi** koda karşı
koşuldu — tasarımın bu iş için yazdığı araç. **On sekizi temiz**, ikisi
sapmıştı:

| # | Madde | Bulgu |
|---|---|---|
| 13 | Sıfır uppercase/lowercase | ❌ → ✅ market onay çipi `.uppercase()` çağırıyordu |
| 18 | Kategori kutucuğu / hedef ölçüleri | ❌ → ✅ Ürün Detayı elle 44dp kutu çiziyordu |

**Madde 13 gerçek bir Türkçe hatasıydı.** `«${ad.uppercase()}» diye yeni
market` — kullanıcı `işkur` yazsa çip `ISKUR` gösteriyordu, doğrusu `İŞKUR`.
Projenin en çok belgelenmiş tuzağı (`"İNCİR".lowercase()` yedi kod noktası
üretiyor) tam da kullanıcıya görünen tek yerde kaçmış. Ad artık yazıldığı gibi.

**Madde 18 bileşen atlamasıydı.** `CategoryTile` bu iş için yazılmış ve 56dp;
Ürün Detayı kendi 44dp kutusunu çiziyor, tipografisi de farklıydı
(`labelLarge` yerine `quantityBadge` olmalı). Aynı görsel öğenin iki boyu
olması bileşen katmanının vaadini boşa çıkarıyordu.

Temiz çıkanlar arasında: sıfır dialog/push/badge, sıfır dynamic color, sıfır
`0.5.dp`, Fraunces 24sp altında yok, gölge yalnızca floating toolbar'da,
sayısal klavye tek yerde, amber dolgu kart paletinin kendi şeridinde.

### Kalan tek yüzey eksiği: `Bunu önerme`

Boş durum atlasının **05 karesi** Ürün Detayı'nda üç satır çiziyor — *Her
zamankilere ekle · **Bunu önerme** · Listeden çıkar*. Kodda ikisi var; cihazda
doğrulandı. F6.5 olarak yol haritasında ve **sıradaki iş**.

---

## On birinci tur — karar 75 · fiyat alanında hane seçimi

`docs/26`'nın tek sorusu **gevşetilerek** cevaplandı. Gerekçe bizim
gözlemimizin aynısı:

> *"Yüz kat hata **ayracın yokluğundan** doğuyordu, haneye dokunmaktan değil —
> ayraç sabit kaldıkça karar 73'ün koruduğu şey bozulmuyor."*

### Kural

Dokunuş en yakın haneye **tek atımlık** seçim koyuyor (hane altında 2dp
çizgi); yazılan rakam **yalnız o haneyi** değiştiriyor, uzunluk sabit kalıyor,
seçim düşüyor. **Seçim ilerlemiyor** — ikinci hane ikinci dokunuş ister.

Seçim yokken her şey karar 73. **İki kural çakışmıyor çünkü tetikleyicileri
ayrık:** sıfırlama yalnızca *seçimsiz* ilk rakamda.

### Uygulamada iki şey ölçümle öğrenildi

**1 · Dokunuş `Initial` geçişte, tüketilmeden dinleniyor.** İlk hali
`detectTapGestures` kullanıyordu ve cihazda **hiç ateşlenmedi**: `450,99`da
5'e dokunup 6 yazmak `4.509,96` verdi — dokunuş sessizce karar 73'e düştü.
Sebep sıra: `BasicTextField` kendi dokunuş işleyicisini **içeride** kuruyor ve
Main geçişinde önce o görüp tüketiyor. Initial geçiş dışarıdan içeriye aktığı
için önce biz görüyoruz; tüketmediğimiz için alan odağını ve imlecini normal
alıyor.

**2 · Hane, imleçten değil dokunuşun x'inden çözülüyor.** `TextFieldValue`
imleci bir **sınır** veriyor — iki hane arası — ve hangi glife dokunulduğu
ondan çıkmıyor: `450,99`da 5'in soluna dokunmakla sağına dokunmak aynı sınırı
verip farklı haneleri kastediyor. Glifin kutusu (`getBoundingBox`) tam cevap
veriyor; tasarımın gerekçesi de bunu varsayıyor (*"tnum hane konumlarını
sabitliyor"*).

### Baştaki sıfır atılmıyor

`450` (4,50) için baş hane `0` yapılırsa değer `050` olmalı, `50` değil —
yoksa kullanıcı tek hane değiştirdiğini sanırken fiyat 0,50'ye düşer.
`trimStart('0')` bu dalda **bilerek** uygulanmıyor.

**Cihazda doğrulandı:** `450,99` → 5'e dokun → `6` → **`460,99`**.

---

## F4.7 — etiket metni → ürün eşlemesi *(23 Ağustos, kullanıcı bildirdi)*

Kullanıcı BİM ve A101'de on çekim yaptı ve **onunda da ürünü elle seçti.**
Sinirlenmesi haklıydı; sebebi OCR değildi.

### Veri ne dedi

| | Fiyat | Marka | Gramaj |
|---|---|---|---|
| A101 (4 çekim) | **4/4** | 0/4 *(bilerek kapalı, `docs/24`)* | 3/4 |
| BİM (6 çekim) | **6/6** | 6/6 dolu, ~yarısı temiz | 4/6 |
| **Ürün** | | **0/10 — her seferinde elle** | |

### Sebep: tasarımın dört kez yazdığı bir kural hiç kodlanmamıştı

> *"Seçim bu markette bu **etiket metnine bağlanır**; aynı etiket bir daha
> sorulmaz."*
> *"Aynı etiket metni daha önce eşlendiyse **ürün sorulmaz**."*

`product_alias` tablosu, `UNIQUE(householdId, storeChain, rawTextNormalized)`
indeksi ve `find` sorgusu **fiş döneminden beri duruyordu**. DAO'nun kendi
KDoc'u bile *"alias öğrenmesinin bütün değeri bu sorguda"* diyor.

**Hiçbiri çağrılmıyordu.** `tagText` karta geliyor, ürün seçiciye *"Etiket
metni: …"* diye yazılıyor ve orada ölüyordu.

### Ne yapıldı

- **Kaydederken** etiket metni seçilen ürüne bağlanıyor — `confirmedAt` dolu,
  çünkü tahmin değil kullanıcının kararı.
- **Çekerken** eşleşme aranıyor; varsa ürün alanı kendiliğinden doluyor.
- **Eşleme zincir bazında** (`storeChain`), şube bazında değil — `Shopping.kt`
  zaten böyle diyor: fiyat karşılaştırması zincir bazında anlamlıysa eşleme de
  öyle.
- **Düzeltme kazanıyor** (`REPLACE`): metin yanlış ürüne bağlandıysa ikinci
  seçim eskisini eziyor.
- **Metin okunamadıysa hiçbir şey bağlanmıyor** — uydurma bir anahtar kalıcı
  bir yanlış eşleme üretirdi.

**Karar 51 bozulmuyor:** OCR metni hâlâ ürün adı olmuyor. Geri gelen şey
kullanıcının *daha önce kendi seçtiği* ürün, ve `save` onu yine
`resolveProduct`tan geçiriyor.

### Kalan: marka kalitesi ölçülecek

Bugünkü BİM turunda marka bazen çöp geldi (`CE UZ`, `BAlkon`, `BILI BIL`).
Bugünkü fotoğraflar karar 29 gereği silindiği için ölçüm yapılamadı; **OCR
dökümü cihazda açıldı**, bir sonraki tur kaydedilecek.

---

## F5.5 — "Başka markette ucuz" çipi bağlandı, iki yanlış yüzde bulundu

**22 Ağustos 2026.** Kullanıcı akşam listeye baktı. Dört satırın **ikisinde
trend çipi** vardı ve **ikisi de yanlıştı** — ne OCR ne de fiyat okuma hatası;
ikisi de doğru okunmuştu.

| Ürün | Gözlemler | Satırın yazdığı | Gerçek |
|---|---|---|---|
| **Süt** | 16:48 A101 36,00 (1 lt) · 16:55 BİM 62,50 (1 lt) | `↑ %74` | aynı gün, iki zincir — **zam yok** |
| **Yoğurt** | 16:58 BİM 102,00 (1,5 kg) · 16:59 BİM 192,00 (ambalaj okunamadı) | `↑ %88` | iki farklı kova — **zam yok** |

Süt'te çarpıcı olan şu: **bir ekran derindeki "Nerede ucuz" aynı iki sayıyı
doğru okuyordu** (*A101 36,00 · BİM 62,50*). Uygulama aynı veri hakkında
birbiriyle çelişen iki cümle kuruyordu ve yanlış olan, önce görülendi.

### Karar 41 bu vakayı zaten yazmış

> *"Çip iki koşulu birden istiyor: karşı gözlem en az %10 ve en az 5 TL daha
> ucuz, ve 14 günden eski değil. Aynı satırda hem trend hem çip doğruysa
> **çip kazanıyor, trend bastırılıyor**. Sıralama mutlak TL tasarrufuna göre,
> liste başına en fazla 3."*

Eksik olan tek şey **çipi dolduran taraftı**. `cheaperElsewhere` alanı,
`CheaperChip` bileşeni ve satır bağlantısı hazırdı; `RowModel.kt`'nin kendi
KDoc'u *"bu alan bugün hiçbir yerden dolmuyor"* diye yazıyordu. **Alias
vakasının aynısı: makine hazır, çağıran yok.**

Yol haritası bunu *"Öncelik 2 — Dış veri"* altında bekletiyordu; yanlıştı. Dış
veriye bağlı olan çipin **kapsamı**, mekanizması değil.

### Ambalaj şartı tasarımdakinden katı tutuldu

Çip yeni bir iddia kuruyor (*"şu ürün orada şu fiyata"*) ve yanlışsa
kullanıcıyı başka bir markete yolluyor. Trendin gevşek `null` kuralı burada
paylaşılmadı: **iki ambalajın aynı olduğu kanıtlı olmalı.**

Bunun bedeli kullanıcının kendi verisinde ölçüldü. Gevşek bıraksaydık Yoğurt
satırına **`A101'de 49,00`** yazacaktı — 250 ml'lik kâseyle 3 kg'lık kovayı
karşılaştıran bir cümle. Trendin yalanını çipin yalanıyla değiştirmiş
olurduk. Gerekçe karar 58'in kendi ilkesi: karşılaştırılamayan bir
karşılaştırmadansa **sessizlik**.

Kod bunu iki ayrı yüklemle söylüyor: `comparablePack` (trend, gevşek) ve
`provablySamePack` (çip, kanıt ister). İkisi yan yana duruyor ve farkın
gerekçesi ikisinin de KDoc'unda.

### Türkçe bulunma hâli eki

Çip metni `A101'de` / `ŞOK'ta` / `Migros'ta` istiyor. Ek, harften değil son
rakamın **okunuşundan** çıkıyor: *"yüz bir"* → `i` ince, `r` yumuşak →
**A101'de** — tasarımın kendi maketindeki metnin aynısı. Harfe bakan bir kural
`A101'da` yazardı.

### Yoğurt satırı DÜZELMEDİ

Ambalajlardan biri okunamadığı için çip de çizilmiyor, trend de bastırılmıyor;
satır hâlâ `↑ %88` yazıyor. Bu bilinçli: trendin `null` kuralı **tasarımın**
kuralı ve tek taraflı gevşetilmedi. Dört soru tasarıma gitti
(`docs/27-tasarima-sorular-12.md`) — asimetrik ambalaj, aynı-zincir şartı,
çipin hangi gözlemi söylediği, ve çipin reyonda neden gizlendiği.

### Kanıt

Dokuz kuralın dokuzu da geri alındığında **tam kendi testini** düşürdü:
5 TL eşiği, %10 eşiği, ambalaj kapısı, en-fazla-3, tasarrufa göre sıralama,
14 gün penceresi (SQL), farklı market şartı, trendin bastırılması, rakamın
okunuşu. 441 test yeşil, sıfır uyarı.

Cihazda doğrulandı: Süt satırı artık `BİM · bugün` + **`A101'de 36,00`**.

---

## F6.5 — "Bunu önerme" bağlandı; tablo iki fazdır bekliyordu

**22 Ağustos 2026.** Tasarım bu anahtarı Ürün Detayı'nın **yedi çiziminde**
gösteriyor — sıfır gözlemli halde bile. Kodda yoktu.

Sebep kayıtlıydı: `suggestion_block` tablosu **v5 şemasından beri** duruyor,
`NeydiDatabase`'in KDoc'u da bunu bilerek yazmış — *"DAO'LAR BU BUMP'A DAHİL
DEĞİL: yeni tabloların okuyucularını kendi fazları yazıyor (… F6.5 …)"*.
`ProductSheet.kt` ise satırın yerini yorumla rezerve etmişti:

> *"F6.5 ikinci anahtarı bağlayacak — bugün engelleme tablosu var ama DAO'su
> yok, ve **görünüp çalışmayan bir anahtar, çalışmayan bir anahtardan
> kötüdür**."*

Yani **migrasyon gerekmedi.** Yazılan şey DAO, motorun dördüncü kuralı ve iki
yüzey.

### Süzgeç motorda, tüketicide değil

Anahtarın adı *"bunu önerme"*, *"bunu şeritte gösterme"* değil. Motoru iki
yüzey paylaşıyor — Ekran 1'in öneri şeridi ve Ekran 3 "Eksik Olabilir".
Süzgeci tüketiciye koysaydık biri susar, öteki konuşmaya devam ederdi ve
kullanıcı ayarın çalışmadığını düşünürdü.

### Kaldırmak SİLMEK değil

`unblock` satırı silmiyor, `unblockedAt` yazıyor. Gerekçe `SuggestionBlock`
KDoc'unda ve **geleceğe ait**: üç-vuruş otomatik bastırma yazıldığında motor,
bir ürünü geri engellemeden önce kullanıcının onu **elle** serbest bıraktığını
görebilmeli — *"kullanıcının elle kaldırdığı bir engeli motorun sessizce geri
koyması, ayarın işe yaramadığı hissi verir"*.

Bu yüzden `blockHistory` sorgusu da yazıldı: bugün yalnızca testler çağırıyor,
çağıran kodu AUTO yarısı getirecek. Satırların saklanmasının **tek** sebebi o.

### Ayarlar bölümü özelliğin şartı, süsü değil

*"Önerilmeyenler"* listesi kalıcı bir reddin geri alınabilir olduğunu gösteren
tek yüzey — görünmeseydi "Bunu önerme" bir **kara delik** olurdu. Bölüm boşken
hiç çizilmiyor ve bunun için bir açıklama notu da yok; sabitlerin aksine, boş
engel listesi kullanıcının **yapması gerekmeyen** bir şey.

Satır sonundaki kontrol ikon değil **kelime**: "çıkar" bir işlem, "geri al" bir
düzeltme.

### Yazılmayan yarılar ve neden

- **Üç-vuruş otomatik bastırma:** kaynağı `suggestion_event` ve o tabloya
  **yazan tek satır kod yok** — DAO'su, indeksi, `@Insert`'ü hiç yazılmamış.
  Ayrıca üç-vuruş sorgusu `(householdId, productId, outcome)` indeksi istiyor
  ve tablonun `indices` listesi boş, yani bir **v6 bump'ı** gerekiyor. Ayrı iş.
- **Sabit terfisi:** F6.5'in başlığında ama **yapılmadı**, çünkü iki tasarım
  dosyası çelişiyor (*"üç geziden sonra kendiliğinden"* ↔ *"birkaç geziden
  sonra… istersen şimdi de ekleyebilirsin"*) ve kodun kendi KDoc'u otomatiği
  **yasaklıyor**: *"kullanıcı işaretler, motor değil — sabitlik bir çıkarım
  değil, beyan"*. Tasarıma soruldu (`docs/28`).

### Kanıt

Altı kuralın altısı da geri alındığında tam kendi testini düşürdü: motorun
süzgeci, yürürlük şartı, ikinci kaldırmanın koruması, `REPLACE`, yeniden-eskiye
sıralama, silinmiş ürünün elenmesi. **449 test yeşil, sıfır uyarı.**

Cihazda uçtan uca doğrulandı: anahtar açıldı → Ayarlar'da *"Önerilmeyenler ·
Süt · [Geri al]"* göründü → geri alındı → bölüm kayboldu.

---

## Marka okuması ölçüldü: %52 → %66, ve çöpün yarısı tek bir kaynaktı

**23 Ağustos 2026.** Kullanıcı *"çektiğim etiketlerin marka ve ürün kısmını hep
ben elimle doldurdum"* diye şikâyet etmişti. Ölçüm **tur beklemeden** yapıldı —
repodaki 99 gerçek etiket fikstürü zaten elde.

`readTagFields` 99 fikstür üzerinde koşturuldu: **46 etikette marka
üretiliyordu ve 22'si çöptü** (%52 doğruluk). Ve çöpün yarısından fazlası tek
bir sınıftan geliyordu — **her etikette basılı duran yasal ibareler**:

| Üretilen "marka" | Kaç kez |
|---|---|
| `MENSE ULKE:TURKYE` ve altı bozuk varyantı | **8** |
| `FİYAT GEÇERLİLİK TARİHİ` / `FNAT DTARIHI` | 3 |
| `Ürt. yeri:Türkiye` · `Gùvencesi` | 2 |

### Neden ad bloğuna düşüyorlardı

**Konumları doğru.** Sol sütunda, fiyatın üstünde, tam ad bloğunun hizasında.
Geometriye bakan bir eleme bunları asla yakalayamaz — ayırt edici olan konum
değil **metin**. Bu yüzden kural sözlüğe bakıyor.

### Kök eşleniyor, gövde değil

OCR aynı ibareyi sürekli başka türlü bozuyor: `MENSE ULIKE:TURKYE`,
`MENSE ULKE.TURKYE`, `MENSEL`, `MENSE ULURKYE`. Tam metin karşılaştırması
beşini yakalar, altısını kaçırırdı. Harf dışı her şey atılıyor, aksanlar
katlanıyor, kalan dizginin **başı** kök listesine bakılıyor.

Aksan katlaması Türkçe'den geniş tutuldu ve bu da ölçümden: `Güvencesi` cihazdan
**`Gùvencesi`** diye çıkmıştı (u-grave, u-umlaut değil) ve yalnızca Türkçe
harfleri katlayan bir eşleme onu kaçırdı.

### Sonuç ve kalan

**36 marka, 24'ü doğru (%66).** Doğru markaların hiçbiri kaybolmadı.

Kalan 12 çöp **başka bir sınıf**: `orize`, `NUIK KREMIASI`, `Aklı Diş Etleri`,
`Florürlü Dis Macunu` — bunlar rafın etiketi değil, **ürünün kendi ambalajı**
okunuyor. Fotoğraf çerçevesi/geometri sorunu, sözlükle çözülmez.

⚠ **Kök listesi kısa kök kabul etmiyor.** `MENSE` güvenli (Türkçe'de böyle
başlayan ürün adı yok) ama `NET` olsaydı `NEKTAR` gibi meşru bir adı yerdi.
Yeni kök eklenirken şart: kökü taşıyan bir **ürün adı düşünülemiyor** olmalı.

---

## F2.7: katalog düzeltmeleri kurulu telefonlara hiç ulaşmıyordu

**23 Ağustos 2026.** Tohumlayıcının kapısı `SELECT COUNT(*) FROM category > 0`
idi: kategori tablosu bir kez doldu mu bir daha hiçbir şey yazmıyordu. Yani
**ilk açılıştan sonra `CatalogSeedData`daki hiçbir düzeltme o telefona
ulaşmıyordu** — yeni bir ürün, düzeltilmiş bir kategori, değişmiş bir
`matchKey` kuralı, hiçbiri. Yalnızca uygulamayı silip yeniden kuranlar
görüyordu. Sessiz, çünkü hiçbir şey patlamıyor.

Kapı artık **sürüme** bakıyor (`app_settings.catalogSeedVersion` ↔
`CATALOG_SEED_VERSION`). `null` damga "bilinmiyor, yeniden yaz" demek, yani
v6'dan önceki kurulumların kataloğu ilk açılışta tazeleniyor.

### Silme yok, üzerine yazma var

`DELETE` + `INSERT` yıkıcı olurdu: `product.categoryId` kategorilere bakıyor ve
aradaki o anda **kullanıcının kendi ürünleri sahipsiz kalırdı**. `INSERT OR
REPLACE`in güvenli olmasının şartı id'lerin deterministik olması — kategoriler
sabit id taşıyor, tohum ürünleri `seed-<yaygınlık>`.

### Yakalanan ikinci sessiz hata

Damga satırını açan `INSERT OR IGNORE` yalnızca `householdId` veriyordu, oysa
`syncPhotos` ve `createdAt` NOT NULL. **`OR IGNORE` o ihlali yutuyordu**: satır
hiç doğmuyor, damga hiç düşmüyor, katalog her açılışta baştan yazılıyordu — ve
hiçbir şey patlamıyordu. Testi yazmasaydım fark edilmezdi.

### v6 tek bump'ta iki ekleme

`catalogSeedVersion` kolonu ve `suggestion_event` üzerindeki
`(householdId, productId, outcome)` indeksi aynı bump'a alındı: ikisi de
otomatik, veri geri-doldurması yok, ve ayrı iki bump iki cihaz dansı demek
olurdu. `suggestion_event` bugün boş — boş tablonun şema değişikliği bedava.

**Cihazda doğrulandı** (`pm clear` YAPILMADAN): sürüm 5 → 6, dokuz tablonun
satır sayısı birebir aynı (product 59, trip_line 118, price_observation 26…),
damga düştü, indeks oluştu, uygulama açıldı.

---

## Karar 61 uygulandı — ve bir cümlesi Android'de uygulanamıyor

**23 Ağustos 2026.** Faz E'nin son açık maddesi. Karar 61 birebir:

> *"Kart yatayda sağ yarıda dikey panel; sayısal klavye sol yarıyı (vizörü)
> örter, kartı asla örtmez; amber şerit kartın içinde kalır."*

**Birinci cümle yapıldı ve cihazda doğrulandı**: kart `CenterEnd`'e yapışık,
tam yükseklik, sol köşeleri yuvarlak. Yön `BoxWithConstraints`ten okunuyor,
platform yapılandırmasından değil — `commonMain`de çalışıyor, yani aynı kural
iOS'ta da geçerli olacak ve bölünmüş ekranda doğru cevabı veriyor.

Genişlik **yarım değil %58**. Tam yarım denendi ve sığmadı: kart iki sütunlu
alan çifti ve Kaydet+Vazgeç ikilisi taşıyor; 360dp'lik bir cihazda kartın iç
genişliği 148dp'ye düşüyor. Vizöre kalan %42, 3:2 rehberi çizmeye fazlasıyla
yetiyor.

### İkinci cümle uygulanamıyor

Android'de IME **alttan ve tam genişlikte** gelir; bir yarıya hapsedilemez.
Cihazda görüldü: yatayda klavye kartın alt yarısını örtüyor. Dikeyde bu sorunu
karar 70 çözmüştü (şerit toplanır, kart kendi içinde kayar) ve aynı çözüm
yatayda da çalışıyor — ama kararın cümlesi bunu söylemiyor. Tasarıma bildirildi
(`docs/32`).

### Yatay vizör hiç tarif edilmemiş — ve kırıktı

Karar 61 yalnızca kartı anlatıyor. Vizörün yatay hâli hiçbir maketle
karşılanmıyor ve dikey için yazılmış kural yatayda **bozuluyordu**:
`fillMaxWidth().aspectRatio(3:2)` 2280px genişliği **1520px yüksekliğe**
çıkarıyor, ekran 1080 — rehber taşıp yalnızca iki dikey kenarı görünüyor,
içindeki *"Etiket kadraja otursun."* metni ekranın altına düşüyordu.

Rehberi kısa kenardan bağladık (yatayda yükseklikten). Cihazda ipucu metni
y=1051'den y=693'e çıktı, yani ekrana girdi. **Bu seçim bizim, tasarımın
değil** — `docs/32`'de soruldu.

---

## Kararlar 76–101 geldi — on altıncı tur

**23 Ağustos 2026.** Tasarım altı dosyanın hepsini cevapladı: **25 yeni karar
(76–100)**, artı 41/55/56/61'e güncelleme notu, artı gecikmeli gelen **101**.
Otuz dört sorunun otuz dördü cevaplı.

### Bu turda kodlananlar: 76 · 77 · 78

**Karar 76 — trend tek taraflı bilinmezlikte susar.** Dört durum tamamlandı:

| Ambalajlar | Sonuç |
|---|---|
| ikisi biliniyor, aynı | trend |
| ikisi biliniyor, farklı | `PackChanged` (karar 67) |
| ikisi de bilinmiyor | trend — tarihsel taban |
| **tam olarak biri biliniyor** | **hiçbir iddia**, satır `Single`'a döner |

Gerekçe tasarımın: *"«%88 zam» da «ambalaj değişti» de aynı önermeye dayanıyor:
bu iki şey aynı boy. **Bilginin yarısı varken iddia kurmak, hiç yokken
kurmaktan daha çok uydurma** — eldeki yarı, iddiayı çürütebilecek yarı."*

Çipin "kanıtlı aynı ambalaj" şartı **koddaki katı hâliyle onaylandı** —
`null` yetmez.

**Karar 77 — trend aynı zincirin cümlesi.** `prev` artık "zamanda bir önceki"
değil, **son gözlemin zincirinden bir önceki**. O zincirde tek gözlem varsa
satır `Single`. Cihazdaki Süt satırı (A101 36,00 → BİM 62,50, *"↑ %74"*)
kendiliğinden düzeldi.

Çip bastırması (karar 41) duruyor ama **artık tesadüfe yaslanmıyor**: eskiden
Süt satırı yalnızca 3 çip sınırının içinde kaldığı için doğruydu.

**Karar 78 — çip en son gözlemi söyler.** Eskiden 14 günlük penceredeki *en
ucuz* karşı gözlemi yazıyordu; A101'de üç taze gözlem varken (36 · 40 · 44)
çip **36,00** diyordu — görülmüş en iyi fiyat, güncel değil.

Seçim artık **iki katmanlı**: önce her rakip zincirin *en son* gözlemi, sonra
onların arasından *en ucuzu*. Tek katmanlı bir sıralama iki rakip zincirde
yanlış cevap verirdi — testi de o vakayı kuruyor.

**Karar 79** kodun davranışını onayladı (çip yalnız plan modunda); karar 41'in
*"reyonda"* cümlesi gerekçeden düştü.

### Kalan kod işleri

80–86 (satır bütçesi), 87–88 (F6.5 metinleri), 89–92 (ekleme geri bildirimi),
93–94 (focusRing + amber), 95–96 (tahmin), 98–100 (yatay), 101 (kategori tonu
silinir — şema kolonu, v7 bump'ı gerekiyor). 97 yol haritasına yazıldı.

---

## Kararlar 89–92 kodlandı — ekleme artık "yapıldı" diyor

**23 Ağustos 2026.** Kullanıcının *"eklenmiş hissi vermiyor"* şikâyetinin
tasarım tarafı. Ölçüm on dört ekleme yolunun **on dördünde de** vurgu
olmadığını göstermişti; sheet'te tek onay köşedeki gri sayaç ve kutucuğun
**%38'e sönmesi**ydi — yani *pasiflik* dili, onay dili değil.

### `successSurface` doğdu (karar 89)

Yeni token: ışık `#E9EFE8`, koyu `#1D2E23`. **`success`ten ayrı**, çünkü
`success` bir metin/ikon rengi (kontrast taşıyor), bu bir **zemin**; ikisini
tek token yapmak yeşil zemine yeşil metin yazmaya davet ederdi.

Maket eklenen satırı **amber-krem** (`#F6E7D2`) çiziyordu ve biz onu bilerek
uygulamamıştık: karar 57 amberi *"eksik / emin değiliz"*e kilitlemişti. Tasarım
bizi doğruladı ve rengi değiştirdi — **sayılar aynı kaldı**
(`Motion.JUST_ADDED` onaylandı, KDoc'undaki *"tasarımdan değil"* notu düştü).

Üç kanal:

1. **Satır yıkaması** — 1.200 ms dolu, 400 ms sönme. Girişi ani (ekleme anının
   kendisi zaten olay), çıkışı yumuşak.
2. **Sayaç ve adet rozeti** — değişimde 150 ms ölçek vurgusu (1 → 1,12 → 1).
   Rozet 1 adette hiç çizilmediği için ikinci eklemede *birdenbire* beliriyordu;
   vurgu değil sıçramaydı.
3. **İşaretli kutucuk sönmüyor** — `successSurface` dolgu + işaret.

Sönmeyi kaldırmak `pressable`a bir parametre gerektirdi (`dimWhenDisabled`):
dokunma tarafı **aynı** kalmalı (kutucuk yine tıklanamaz), yalnızca görünüm
farklı konuşmalı. Ayrı bir modifier yazmak ikisini ayırırdı.

### Haptik dördüncü olayı sayıyor (karar 90)

Karar 55 üç olay saymıştı ve ölçütü karar 3'te yazılı: **sık tekrarlanan ve
görsel onayı zayıf** eylem. Sheet'ten ekleme ölçütün tam içinde (oturumda 5–10
kez, liste sheet'in arkasında görünmüyor). Kök alandan ekleme **haptik almıyor**
— satır gözün önünde beliriyor ve yıkama onayı zaten veriyor.

### Toplu ekleme sayısını söylüyor (karar 91)

Toast'ın yedinci kullanımı. Pano ve *"geçen sefer aldıklarını ekle"* sayıyı
hesaplayıp **atıyordu**. Ve sıfır hâli daha önemli: *"hiçbir şeyin olmadığı
ekran, çalışmayan uygulamadan ayırt edilemez."*

Toplu yolda mükerrer satır **adet artırmıyor, atlanıyor**: tek tek eklemede
ikinci dokunuş "bir tane daha" demek, toplu yolda kazayla ikinci kez
yapıştırılan bir liste **listeyi katlardı**.

### Cihazda doğrulandı

Sheet'in kutucukları yeşil "yapıldı" dolgusunda; sayaç dokunuşta vurguluyor;
kök alandan eklenen satır yeşil yıkamayla beliriyor.

---

## Kararlar 80–85 kodlandı — satır bütçesi yazılı hâle geldi

**23 Ağustos 2026.** Kodun kendi kuralı baştan beri *"ad kırpılması kabul
edilemez — fiyat ipucu yardımcı bilgi, ad ise satırın varlık sebebi"* diyordu
ve **tutmuyordu**: ölçüm 411dp'de ada %35, 360dp'de dokuz karakter kaldığını
göstermişti.

### Ad tabanı + feda sırası (80)

`widthIn(min = 120dp)` (~13 karakter) ve yer yetmediğinde sıra:
**sparkline → delta çipi → eş avatarı → raptiye → adet rozeti.** Fiyat çipi ve
ad asla düşmez.

Sıranın mantığı bilgi değeri: *"sparkline süs, delta özeti metada da yaşar,
avatar bağlam, raptiye bölüm başlığının tekrarı; rozet ise miktar — yanlış
adedin bedeli parayla ödenir."*

Aritmetik **saf bir fonksiyona** çıkarıldı (`survivingElements`) çünkü karar
80 bir yerleşim ayrıntısı değil bir **söz**: 360dp'lik bir cihazda adın 13
karakterin altına inmemesi. Saf fonksiyon o sözü Compose kurmadan sınanabilir
kılıyor ve maketin verdiği sayılar doğrudan test olarak yazılabiliyor.

⚠ **Testi yazarken bir şey anlaşıldı:** `available` hesabından **fiyat çipini
çıkarmak zorunlu**. Çip hiç düşmüyor, yani bütçenin konusu değil — çıkarmayı
unutan ilk hesap 360dp'de *"hiçbir şey düşmesin"* diyordu, oysa maket üç
öğenin birden düştüğünü yazıyor. Çip çıkarılınca maketin sayısı **birebir**
çıktı: ad 192dp.

### Delta + sparkline ana satıra (82)

Maket ikisini baştan beri ana satırda çiziyordu; kod ikinci satırın içine
koymuştu ve orada **ad sütununun** genişliğini paylaşıyorlardı — kaybeden hep
cümle oluyordu (*"önce 1.234,56"* yerine *"önce…"*). Artık paylarını feda
sırasından alıyorlar.

### Güncel fiyat yalnız çipte (83) ve çip kendi satırında (84)

İkinci satır **tek içerik ve tek satır** taşıyor: geçmiş metası **ya** ucuz
çipi **ya** öneri gerekçesi. Birlikteliğin dışlanması yeni bir kısıt değil,
var olan kuralların sonucu — çip varken trend bastırılıyor (karar 41) ve
`PackChanged` çipi zaten imkânsız kılıyor. Kod bunu artık **veriyle değil
kuralla** biliyor.

`PackChanged` metni de değişti: *"900 g → 800 g · 45,00"* yerine
**"ambalaj küçüldü: 900 g → 800 g"**. Güncel fiyat çipe taşındı.

### 92dp fiyat sütunu ve binde kuruş (81, 85)

`SizesExtra.priceColumn` **ölü bir sabitti**: tasarım sistemi, altı maket
kullanımı ve Compose Spec 92dp diyordu, kodda hiçbir yerde geçmiyordu. Sonuç:
dört haneli fiyat 99,38dp'ye taşıp fazlasını addan çalıyordu.

1.000 TL ve üstünde kuruş yazılmıyor — bin liralık bir sepette 56 kuruş
okunacak bir şey değil ve tam değeri Ürün Detayı'nda duruyor. **En yakın
liraya**, aşağı değil: aşağı yuvarlamak bütün fiyatları sistematik olarak ucuz
gösterirdi.

Çipin 48dp'lik hedefi artık kendi kutusundan değil **satır yüksekliğinden**
geliyor (karar 85). `heightIn(48dp)` fiyatlı her satırı 56dp yerine 64dp
yapıyor ve *"10–11 satır görünür"* hedefini dokuza düşürüyordu.

---

## Kararlar 102–106 — satır yeniden çizildi: iki bant

`docs/33`'ün sorusuna gelen cevap net: **feda sırası bir ara adımdı.**

### Kimlik bandı ve ekonomi bandı (102–104)

Ölçüm 80–86'yı kendi gerekçesiyle çürüttü: 360dp'de avatar, raptiye **ve adet
rozeti** birlikte düşüyordu — yani *"yanlış adedin bedeli parayla ödenir"*
diyen kural adedi siliyordu, çünkü feda edecek başka şey kalmamıştı.

Satır artık tek katta yarışmıyor:

| Bant | Ne söyler | Üyeleri | Feda |
|---|---|---|---|
| **Kimlik** | ne alınacak | rozet · ad (flex) · avatar 20dp · raptiye 14dp | **hiçbiri, hiçbir genişlikte** |
| **Ekonomi** | ne biliyoruz | meta (flex) · delta · fiyat çipi 92dp | **yalnız delta** |

Ad tabanı (`widthIn(min = 120dp)`) **kalktı** — gereği kalmadı: 411dp'de ada
231dp, 360dp'de 180dp kalıyor. Eski düzen 192dp'yi ancak üç öğeyi düşürerek
buluyordu.

Yükseklik **56dp** (yalnız kimlik) / **72dp** (iki bant). 68dp gitti.

Delta'nın düşme ölçütü bir eşik değil bir **karşılaştırma**: *"cümle tam
kalır."* Çip cümleyi kırpacaksa çip düşer, cümle değil — bu yüzden ikisi de
çizilmeden önce ölçülüyor (`deltaSurvives`). `weight`'e bırakılsaydı kaybeden
hep esneyen taraf, yani cümle olurdu.

### Sparkline silindi (106)

24×14dp'de okunmuyordu ve **tam da bu yüzden** feda sırasının ilk üyesiydi.
Sıralamak yerine kaldırıldı; yeri Ürün Detayı'ndaki grafik.

Bileşenle birlikte **veri hattı da** silindi: `Trend.history`, `parseHistory`
ve satır başına korele bir `group_concat` alt sorgusu. Çizgiyi kaldırıp
hattı ayakta bırakmak sessiz çürüme olurdu — alan yalnızca yazılan, hiç
okunmayan bir şeye dönüşecekti.

### Karar 83'ün çelişkisi çözüldü (105)

Karar 83 *"Single'ın ikinci satırı yok"* diyordu, maket üç yerde `son 24,90
TL · Migros · 8 gün önce` çiziyordu, kod da üçüncü bir şey yazıyordu. Üçü de
haklıymış — **iki farklı tazelik hâliymiş**:

| Gözlem | Fiyat çipi | Ekonomi bandı |
|---|---|---|
| ≤ 7 gün | **var** — güncel fiyat | `BİM · bugün` |
| > 7 gün | **yok** | `son 24,90 TL · Migros · 8 gün önce` |

*"Fiyat iki yerde asla yazılmaz"* kuralı iki hâlde de ayakta. Çipin ne zaman
çizildiği ile cümlenin ne yazdığı ayrı yerlerde kararlaştığı için ikisi
`FRESH_DAYS` üzerinden aynı sınıra bağlandı ve sınır teste alındı.

### Adet rozeti her satırda (103)

Eski kural (*"her satıra 1x yazmak gürültü"*) rozet yalnızca **okunan** bir şey
olduğu sürece doğruydu. Karar 107 onu miktarın **düzenlendiği** yer yapınca
çizilmeyen rozet, düzenlenemeyen miktar anlamına gelmeye başladı.

Gürültü itirazı susturarak değil **küçülterek** çözüldü: 20sp/800 → **14sp/700**
(ada rakip olmasın), "1x" değil yalın **"1"**, ve varsayılan miktar
**dolgusuz kontur** — dolgu artık "bunu ben seçtim" demek.

### "Birebir aynı" turu — maketin sayıları okundu

Kullanıcı *"her şey tasarımdaki görünüm ile birebir aynı olsun"* dedi. Maket
tarayıcıda açılıp her öğenin **hesaplanmış stili** okundu (`getComputedStyle`),
göz kararı yapılmadı. Bulunan ve düzeltilen farklar:

| Öğe | Maket | Koddaydı |
|---|---|---|
| Bantlar arası boşluk | 5px | yoktu |
| Onay → kimlik bandı | 10px | 12dp |
| Adet rozeti | h 24, yan dolgu 8, min 30 | h 26, dolgu 6 |
| Eş avatarı | **dolu** `#3F6B54` + beyaz harf, 10sp/700 | %18 zemin + yeşil harf |
| Raptiye rengi | `#8A7666` (`outline`) | `onSurfaceVariant` |
| Meta puntosu | 13px | 14sp |
| Delta çipi | h 22, yan dolgu 7, 12sp/600 | dolgu 6/2, 11sp |
| Fiyat hapı | yan dolgu 10 | 12 |

Rozetin zemini maketde `#EADCCB` — palette **olmayan** bir değer, fiyat
çipinin zemininden (`#F1E7DB`) bir tık koyu, ki aynı satırdaki iki hap
birbirinden ayrılsın. En yakın token `hairline` (`#E7DACB`) kullanıldı; fark
gözle seçilmiyor ve karar 101'in az önce sildiği renk çoğalmasını geri
getirmek istemedik.

---

## Kararlar 107–110 — miktar düzenlenebilir oldu

Kullanıcının cümlesi turun başındaydı:

> *"Kategoriden domates ekliyorum, 1 kg ekleniyor. Ama sonra bunu 4–5 yapmak
> istediğimde ya tekrardan yazmam gerekiyor ya da katalogdan sürekli ekle-ekle
> yapmam lazım."*

Denetim beş değil **sekiz** ekleme yolu buldu; hiçbirinde miktar
düzenlenemiyordu. Tek yol ürünü tekrar eklemekti ve o da **birer birer**
artırıyordu.

### Üç yol tek davranışa indi (109)

`ListRepository.add` ikinci eklemede adedi **artırıyordu** ve savunması
makuldü: *"iki kişi aynı ekmeği istedi, iki ekmek değil."* Ama o cümle
miktarın **başka bir evi olmadığı** dünyada yazılmıştı — artırmak, adedi
değiştirmenin tek yoluydu.

O dünyada aynı jest üç farklı şey yapıyordu: tek tek ekleme artırıyor, toplu
ekleme atlıyor (karar 91), keşif sheet'inde işaretli hücre pasif olduğu için
hiçbir şey olmuyordu.

Ve **sessiz bir hata** üretiyordu: sabitler her gezide otomatik ekleniyor,
kullanıcı "ekmek" yazıp eklediğinde adet 2 oluyordu — kimse istemeden. Bu
hatanın testi yoktu; kural değişince yazılabildi.

İkinci ekleme artık yalnızca yıkamayı çalıştırıyor. `AddResult` ikisini
ayırıyor: yıkamanın satıra ihtiyacı var, sayaçların ise *"bu bir ekleme
değildi"* bilgisine. Ve o dalda tabloya **hiçbir şey yazılmıyor** — boş bir
yazma bile `updatedAt`i tazeleyip Faz 7'nin LWW birleştirmesinde bu cihazı
haksız yere "daha yeni" yapardı.

### Miktarın kendi yazma yolu (107–108)

`setQuantity` **mutlak** yazıyor, `add` gibi delta değil: sayaç yukarı aşağı
gidiyor ve delta biriktiren bir yol, kaybolan tek bir dokunuşta sessizce
yanlış sayıya oturur. Hedefli `UPDATE`, `@Update` değil — aynı satıra
`setChecked`, `setOutcome` ve `softDelete` de yazıyor.

Üç yüzey, tek aritmetik ve tek bileşen:

| Yüzey | Ölçü | Nereden |
|---|---|---|
| Satır | dügme 44×32, ikon 20 | karar 107 |
| Ürün Detayı | 48×48, ikon 22, yazılabilir değer | karar 108 |
| Keşif hücresi | 26×26, ikon 15 | karar 109 |

Adım birime bağlı ve kural *"adet ise 1"* değil **"tartılmıyorsa 1"** diye
yazıldı — paket, kutu, demet ve şişe de sayılıyor.

### Karar 110 — bir satırda bir yığılmış hedef

`docs/35`'te sorduğumuz soruyu tasarım **yeniden çerçeveledi**: sorun çipin
26dp olması değil, *"72dp satırda İKİ yığılmış 48dp hedef istenmesi (rozet +
çip; 2 × 48 = 96)"*.

- **Fiyat çipi dokunulabilir değil**; ekonomi bandının tamamı bilgi.
- Satırın tek yığılmış hedefi **adet rozeti**.
- Karar 84'ün *"çip dokunuşu Ürün Detayı açar"* fıkrası **geri çekildi** —
  uzun dokunuş zaten aynı şeyi yapıyordu.

Rozetin 48dp hedefi `docs/35`'in kaydettiği iki başarısız yoldan sonra
**üçüncü yolla** verildi: hedef dar bandın dışına, satırın kök kutusuna
konuluyor ve konumu ölçümden geliyor. Cihazda doğrulandı.

### Cihazda bulunan dört hata

Hiçbiri derlemeyle ya da testle yakalanmazdı:

1. **Eksi tuşu artırabiliyordu.** Taban düz dönüyordu; katalogda gerçekten
   "1 g" Çay var ve eksiye basmak onu 100 g yapıyordu.
2. **Birim seçimi hiç silinemiyordu.** Ortak yardımcı `override ?:
   current.unitOverride` yazıyordu — "null = dokunma" ile "null = sil" aynı
   sayılmıştı.
3. **Yazarken alan sıçrıyordu.** Yerel metin sayıya bağlı hatırlanıyordu;
   "2,5"i silip "1" yazmak "11" üretti.
4. **Sayaç alışveriş modunda da açılıyordu.** Ekonomi bandının kendi
   `!shoppingMode` koruması zaten gerekçeyi taşıyordu.

Ayrıca kullanıcı bildirdi: **Ürün Detayı yarım açılıyordu** ve her seferinde
kaydırmak gerekiyordu. Ekle sheet'i baştan beri `skipPartiallyExpanded`
kullanıyordu; ikisinin ayrı davranması bir tercih değil, unutulmuş bir satırdı.

### Şema v7

`trip_line.unitOverride` (nullable) ve karar 101'in borcu olan
`category.tintArgb` **aynı bump'a** bindi — her bump bir elle cihaz dansı.
Cihazda v6 → v7 koşuldu, `pm clear` yapılmadan: dokuz tablo sayısı da aynı.

`unitOverride` ayrı bir kolon, çünkü `tl.unit` zaten satıra özel ama *"katalog
böyle diyor"* ile *"kullanıcı böyle seçti"*yi ayıramıyor. Ve fark teorik değil:
`CatalogSeeder` katalogu `INSERT OR REPLACE` ile yeniliyor.

---

## Kararlar 95–96 ve 111–115 — tahmin artık kör çarpmıyor

Kullanıcının bildirdiği kusur: `3 kg Yoğurt`, bir 3 kg'lık kovanın 192,00 TL
fiyatıyla çarpılıp **576,00 TL** yazıyordu.

### Çarpan koşullu oldu (96, 111)

| Satır | Katkı |
|---|---|
| gözlem yok | yok |
| sayılan birim | `miktar × fiyat` |
| tartılı, ambalaj biliniyor | `⌈miktar ÷ ambalaj⌉ × fiyat` |
| tartılı, ambalaj bilinmiyor | **yok** — toplamdan düşer, **paydada kalır** |

Kural *"adet ise 1"* değil **"tartılmıyorsa 1"** diye yazıldı: paket, kutu,
demet ve şişe de sayılıyor ve katalogda gerçekten var.

Düşen satır **sessiz değil**: meta yuvası *"3 kg · ambalaj bilinmiyor"* yazıyor,
fiyat çipi yerinde kalıyor. **0,00 TL yazılmıyor** — düşen satırın tutarı yok,
sıfır değil. Olguyu vermenin yeri Ürün Detayı'ndaki gözlem satırı: kesik
konturlu bir *"ambalaj?"* alanı, altında *"Etiketten okunamadı. Yazarsan bu
satır tahmine girer."* Liste satırına düğme konmadı — karar 110'un kapattığı
iki yığılmış 48dp hedef geri gelmesin diye.

### ⚠ Bir 1000× mayını bulundu ve karara girdi

`trip_line.unit` ile `packUnit` **farklı kanonlar** konuşuyor ve bu bilinçliydi
(`QuantityParser` KDoc, E2). Aktif listedeki **beş fiyatlı satırın sıfırında**
ikisi aynı dizge — `L`/`lt`, `g`/`kg`.

Kapalı bir gezide duran `2 g Çay` satırı, `1 kg`'lık ambalaja karşı ham bölmeyle
**798,00 TL** verirdi (doğrusu 399,00); `500 g` olsaydı **199.500,00 TL**. Yani
`⌈⌉` dalı bugünkü 3× hatayı ara sıra 1000× hataya çevirecekti.

Karar 111 bunu kurala yazdı: **birimler aynı ölçeğe indirilemiyorsa bölme
yapılmaz, satır ambalajsız sayılır.** Uzlaşma **okurken** oluyor — hiçbir satır
yeniden yazılmıyor, E2'nin kuralı ayakta.

### Satır kaynağını yazıyor (95, 113, 114)

`Tahmini sepet ~624 TL` · `BİM fiyatlarıyla · 4/7`. Tek zincir → zincir adı;
karışıksa *"son fiyatlarla"*. **Tek marketsiz gözlem zincir adını düşürür** —
ve bu ayrı bir dal değil, aynı kuralın doğal sonucu: `null` da bir değer ve
tekilliğe katılıyor. Etikete **yalnız toplama giren satırlar** oy veriyor.

Zincir **anahtarla karşılaştırılıyor, adla yazılıyor**: `chain` normalize bir
anahtar (`bim`) ve ekrana yazılınca küçük harfle çıkıyordu — cihazda görüldü.
Dönüştürmek de yasak; doğru yol tablodaki adı okumak.

### Eşik mutlak kaldı (112)

Tasarım sistemi bir süre *"%60'ından azı → %40 opaklık ve `~`; %30'un altı →
gizle"* diyordu, kod karar 53'ün mutlak üçünü kullanıyordu. Karar 96 **payı**
düşürünce ikisi ilk kez çakıştı; karar 112 yüzde fıkralarının üçünü de düşürdü.
Sayaç zaten kapsamı yazıyor. Eşiğin altında **sessizlik** — *"yeterli veri
yok"* diye bir yüzey yazılmıyor.

Eşik artık **toplama giren** satırı sayıyor, fiyatı olanı değil.

### Hesap SQL'den Kotlin'e taşındı

Zorunluydu: karar 96 payla paydanın **ayrışmasını** istiyor ve tek `GROUP BY`
üzerindeki `SUM` + `COUNT` bunu ifade edemiyor — toplamdan çıkan satır sayıdan
da çıkıyordu. Ayrıca `⌈⌉` ve birim dönüşümü bir `@Query` dizesinin içinde ne
yazılabilir ne sınanabilirdi. Dosyanın kendi kuralı da bunu söylüyor: *"SQL
veriyi getirir, iddiayı Kotlin kurar."*

### Cihazda bulunan bir yalan daha

`PackChanged` iki ambalaj **farklıysa** ateşleniyordu, yönüne bakmadan — ama
metin her zaman *"ambalaj küçüldü"* yazıyordu. Kullanıcının kendi verisinde
`1,5 kg → 3 kg` bir **büyüme** ve satır onu küçülme diye yazdı. Fiil artık
ölçülen yöne bağlı; karar 67'nin shrinkflation uyarısı yalnızca gerçekten
küçülen ambalajda çıkıyor.

### Açık kalan

- **`priceUnit` hâlâ yazıcısız.** Karar 96'nın önkoşulu **değil** — formülü o
  kolona bakmıyor — ama kapattığı delik ayrı ve açık: kilo fiyatı, adet birimli
  satıra yazıldığında (`MigrosGrammar`'ın manav yolu) satır yine yanlış
  çarpılıyor. Okuyucusu (`readTagUnitPrice`) zaten var ve normalize; eksik olan
  bir alan, bir parametre ve bir atama.
- **Karar 97 ertelenmiş kalıyor.** Bugüne kadar yalnız **iki** zincirde gözlem
  var; ürün başına ortalama 1,0–1,4 zincir, eşik 1,5 (karar 115).
- `docs/34`'ün add-path tablosu **beş** yol yazıyor; kodda **sekiz** var.

---

## Kararlar 116–120 — planlama bir plan oldu *(23 Ağustos, 21. tur)*

Kullanıcının iki cümlesi, ikisi de aynı yere bakıyordu — **planlama modu ne
için var** (`docs/37`):

> *"Ben liste yaparken neden alındı/alınmadı var ki? Zaten alışverişe çıkıyorum
> dediğimde orada işaretleme yapıyorum."*

> *"BİM'e gidiyorumdur, 10 tane şeyi oradan almak için giderim, 2-3 tanesini de
> A101'den alacağım diye işaretlerim."*

### Karar 116 — planlamada işaretlenecek bir şey yok ✅

Onay dairesi **yalnızca alışverişte** çiziliyor; planlamada satıra dokunmak
Ürün Detayı'nı açıyor ve **"Alındı" bölümü planlamada yok.**

Bedeli ölçüldü: cihaz testlerinde **yanlışlıkla yapılan her dokunuş** bir satırı
"Alındı"ya taşıdı. Planlamada satırın *tamamı* işaretleme hedefiydi — yani
ekranın en büyük hedefi, en az istenen işe bağlıydı.

Daire (24dp + 10dp boşluk) düşünce kimlik bandı **34dp kazandı**: 360dp'de ada
180dp yerine 214dp kalıyor.

⚠ **Kod tasarımdan bir adım ileri gitti ve bu `docs/38` S4'te soruldu:** kural
metni bölümün *"alışveriş ve sonrasında"* var olduğunu söylüyor ve maket onu
çiziyor; kod **iki modda da** kaldırdı. Gerekçesi ölçülmüş: reyonda işaretlenen
satırın yer değiştirmesi, hareket eden başparmağın altında yeniden sıralama
demek. Aynı maket *"işaretli satır yerinde kalır"* da diyor — ikisi birlikte
duramıyor.

### Karar 117 — hedef gezide, istisna satırda ✅

`trip.storeId` (zaten vardı, kullanılmıyordu) hedefi taşıyor; **şema v8**
`trip_line.storeId`'yi ekledi — tek nullable kolon, otomatik migrasyon,
`null` = *"hedefi izliyor"*.

Başlığın alt satırı, bugün *"Son alışveriş: dün · ~1.505 TL"* yazan yuva,
hedef seçiliyken **beyan cümlesine** dönüşüyor:

> 🏪 **BİM'e gidiyorsun** · 2'si A101'de

**Cümlenin kısa biçimi bir üslup değil ölçüm sonucu.** Tasarımın kendi hesabı:
başlıkta dört hedef ve storefront glifinden sonra **108dp** kalıyor (390dp'de),
cümle **72dp** — 360dp'de de sığıyor. Reddedilen `"2 satır A101'de"` **97dp**'ydi
ve 360dp'de kırpılıyordu. Chevron (20dp) **çizilmiyor**; dokunma hedefi başlık
bloğunun tamamı.

Bu, koda **iki yeni Türkçe ek kuralı** getirdi ve ikisi de var olan
`turkishLocative`'ten farklı:

| Ek | Kural | Neden ayrı |
|---|---|---|
| **Yönelme** (`turkishDative`) | ünlü uyumu + **kaynaştırma `y`** | Ek tek ünlü; iki ünlü yan yana gelemez. Yedi tohum zincirinin **üçü** bunu istiyor: "File'ye", "CarrefourSA'ya", "Tarım Kredi'ye" |
| **İyelik** (`possessiveSuffix`) | okunuşa göre **tablo** | Dokuz rakam **beş** ayrı ek üretiyor: `i, si, ü, sı, u`. İki eksenli bir bayrak bunları üretemez |

Seçici (`"Nereye gidiyorsun?"`) çip tabanlı: h44, dolgu 0/16, yarıçap 999,
aralık 8; seçili `inverseSurface`+`inverseOnSurface` 15sp/600, seçili olmayan
`surfaceVariant` + 0.8dp hairline 15sp/500; ayırıcının altında `"Belli değil"`
h48, 17sp/500 `onSurfaceVariant`.

⚠ **İstisnayı yazan jest hiç tasarlanmadı** (`docs/38` S6). Kullanıcının
cümlesindeki tek fiil — *"işaretlerim"* — karşılıksız: veri alanı var, gösterimi
var, `setLineStore` hazır ve **çağransız**. Kod bir jest uydurmadı çünkü
seçeneklerinin hepsi var olan bir kararı deliyor (38'in satır sırası, 110'un
tek yığılmış hedefi, 116'nın "satır yüzeyi veri değiştirmez"i).

### Karar 118 — işaret sapmadır ✅

Ekonomi bandının **başında**: `storefront` 14dp `outline` + zincir adı
13sp/600 `onSurface` + `·` + meta 13sp/400 `onSurfaceVariant`; grup içi 4dp,
bandın kendisi 8dp. **Meta kırpılır, sapma kırpılmaz** — sapma kullanıcının
yazdığı, meta bizim hatırlattığımız şey.

Sapma **tek başına bandı var ediyor**: fiyatı, geçmişi, hiçbir şeyi olmayan bir
satır da 56dp'den 72dp'ye çıkıyor.

Alışverişte sapanlar reyonlarından çıkıp **zincir başına bir bölüme** iniyor,
başlığında storefront + `"A101'de"`. Gerekçe reyon sırasının ne işe yaradığı:
liste *"bu markette hangi sırayla yürüyeceksin"* diyor ve başka bir marketten
alınacak satır o yürüyüşün içinde durursa her reyonda aranır, bulunamaz.

Envanter **20 → 21**: `storefront` (Phosphor Regular 2.1.1, MIT).

⚠ **Bant tek akışta iki zincir adı taşıyabiliyor** ve bu cihazda görüldü:
`🏪 A101 · BİM · dün` — biri beyan, öteki gözlem (karar 105). Makette bu
bileşim **bir kez bile** çizilmemiş (`docs/38` S1).

⚠ **"Başka markette ucuz" çipi sapma varken bastırılıyor** — kod kararı,
makette yok. Çip *"istersen A101'e uğra"* diyor, sapma ise *"zaten A101'den
alacağım"*. Kapanmış bir soruyu yeniden sormak, üstelik 92dp'lik çipi 50dp'lik
işaretin yanına koyup ikisini birden kırpmak pahasına.

### Karar 119 — beyan organizasyon, aritmetik değil ✅

Tahmin **değişmiyor**. Cihazda doğrulandı: hedef seçilmeden önce ve sonra
`~609 TL · BİM fiyatlarıyla · 4/6`. Yapısal olarak da mümkün değil — tahmin
`EstimateRow` üzerinden hesaplanıyor ve o projeksiyonda `storeId` **yok**.

Maketin kendi cümlesi ikisinin **çelişmesini meşru** kılıyor:
*"'BİM'e gidiyorsun' + 'Migros fiyatlarıyla · 15/18' ikilisi tahminin gittiğin
marketten olmadığını söyler. İki tutar yazılmaz."*

⚠ Kullanıcının cümlesinin ikinci yarısı — *"bu şekilde de tahmini sepet
hesaplanabilir"* — maketin özetinde düşmüş ve karar o düşen yarıyı reddediyor
(`docs/38` girişi). Karar 97 (markete göre tahmin) ertelenmiş kalıyor: eşik 1,5
zincir/ürün, bugün 1,0–1,4.

### Karar 120 — geriye dönük kayıt ✅

Kullanıcının bildirdiği iki kusur, ikisi de commit `9b2bcd3` ile kapandı:

1. **Ürün Detayı sheet'inin altı görünmüyordu** — içerik bir `Column`dı ve
   kaydırması yoktu; sheet'ten uzun olan her şey sessizce kırpılıyordu.
2. **Silme jesti yarım kalıyordu** — eşik açılan alanın %60'ıydı. Artık
   **sonuna kadar çekmek** siliyor, daha azı kapanıyor.

### Bu turda ölçülen ve düzeltilen — tasarım kaynaklı değil

1. **Bölüm başlığı simetrikti** (8dp/8dp) ve cihazda **yanlış tarafa
   yaslanıyordu**: üstünde 20,2dp, altında 24,4dp — her başlık bir önceki
   bölümün kuyruğu gibi okunuyordu. Maketin ölçüsü **20dp/4dp**; uygulandıktan
   sonra cihazda 33,1dp/20,6dp. Kullanıcı bunu *"listedeki itemlar çok iç içe
   gibi duruyor, karışık gibi"* diye bildirdi ve **satırlar zaten doğruydu** —
   düz olan tek yer başlığın çevresiydi.
2. **Ekonomi bandının aralığı 6dp'ydi**, maket 8px diyor.
3. **`inverseSurface`/`inverseOnSurface` tanımsızdı** — M3 baseline morunu
   taşıyorlardı; onları kullanan ilk yüzey sessizce mor çizerdi.
4. **Test dosyasında `emptyList()` adlı bir test** standart `emptyList()`'i
   gölgeliyordu; hata *"beklenen `List<String>`, gelen `Unit`"* diye çıkıyor ve
   sebebi hiçbir yerde görünmüyordu.

---

## Kararlar 121–126 — istisnayı yazan jest bulundu *(23 Ağustos, 22. tur)*

`docs/38`'in **altı maddesinin altısı da** cevaplandı. İki kod kararı
onaylandı (125 ve 123'ün seçici yarısı), biri değiştirildi (121), üçü yeni iş.
Karşılık tablosu `ROADMAP.md` §3.2'de.

⚠ **Defterin kendi sayacı düzeltildi:** 97 değil **113 giriş**, 126'ya kadar
numaralı.

### Karar 121 — nokta grup içini bağlar, boşluk grupları ayırır ✅

**Sorun `docs/38` S1'de yazılıydı ve cihazda görülmüştü:** ekonomi bandı tek
akışta **iki zincir adı** taşıyordu ve aralarında tek bir `·` vardı —

> 🏪 **A101** · BİM · bugün

Biri **beyan** (*"burayı A101'den alacağım"*), öteki **gözlem** (*"fiyatı
BİM'de gördük"*). Aynı noktalama ikisini birbirine bağlıyordu.

**Tasarımın cevabı yeni bir yol:** aradaki nokta **silinir**, iki grup bandın
kendi **8dp**'sine bırakılır, grup içi **4dp** yerinde kalır.

```
🏪 A101␣␣BİM · bugün
   └─4dp┘  └─ 8dp ─┘
```

Gerekçe bandın kendi dilbilgisi: *"nokta grup içini bağlar, boşluk grupları
ayırır"* — ve fiyat çipini de aynı 8dp ayırıyor.

**Kodda ne değişti:** sapma işareti `DeviationMark` adıyla kendi bileşeni
oldu ve bandın **kardeşi**; içerideki `Text("·")` ve `"· $text"` iç içeliği
kalktı. Kırpılan taraf yine meta (`weight(1f)` onda), sapma `flex:none`
karşılığı olarak ağırlıksız — yani asla kırpılmıyor.

⚠ **Taze gözlemin zincir adını düşürmek (b seçeneği) REDDEDİLDİ.** İki ad
birlikte bir **çelişki** gösteriyor — *"A101'den alacağım ama fiyatı BİM'de
gördüm"* — ve karar 119 tam o sinyali koruyor. `EconomyBandTest` bu
bilgisizliği kilitliyor: cümle sapmadan haberdar değil ve olmamalı.

### 121 bir hatayı da görünür yaptı: delta bütçesi işareti saymıyordu

Karar 118 işareti banda soktu ama **bütçe onu görmüyordu** — işaret
`weight`li grubun *içindeydi* ve bant düzeyindeki hesap yalnızca fiyat çipini
biliyordu:

```kotlin
val room = if (priceText != null) maxWidth - SizesExtra.priceColumn - 6.dp else maxWidth
```

İki grup kardeş olunca eksiklik görünür hâle geldi: aynı kısa cümle, aynı
ekran, ama bandın **56dp**'si zaten işarete gitmiş oluyor ve delta yine de
*"sığıyorum"* diyordu. Kırpılan şey **cümle** oluyordu — karar 104'ün tek
yasağı.

Hesap saf bir fonksiyona çıktı (`deltaBudget`) ve kural bir liste değil bir
**çıkarma**: bandın düşmeyen her üyesi **kendi aralığıyla birlikte** bütçeden
iner. `- 6.dp` de aynı ailedendi — bandın aralığı 21. turda 8dp'ye
düzeltilmişti, bu satır 6dp'de kalmıştı.

**Isırma kanıtı** (üç kural, üç ısırık, her biri tam kendi testini düşürdü):

| Tersine çevrilen | Düşen test |
|---|---|
| `deltaBudget`'ten sapma bacağı | `theDeviationMarkAloneCanDropTheDelta` + `everyFixedMemberBringsItsOwnGap` |
| Üyelerin kendi aralığı | `theChipAndItsGapAreWhatMakeTheBandOneHundredEightySix` + `everyFixedMemberBringsItsOwnGap` |
| `DEVIATION_GAP` = 8dp (gruplar kaynaşır) | `spaceSeparatesTheGroupsAndTheDotOnlyBindsWithinThem` |

⚠ **Cevapsız kalan tek kod kararı yerinde duruyor:** sapma varken *"başka
markette ucuz"* çipi bastırılıyor. `docs/38` S1'in içinde geçiyordu ama karar
121 yalnızca iki zincir adının ayrılmasını cevapladı; çipin kaderi hâlâ
**kodun kararı**.

### Karar 126 — istisnayı yazan jest bulundu ✅

`docs/38` S6 **engelleyiciydi** ve sorusu tek cümlelikti: kullanıcının
*"2-3 tanesini de A101'den alacağım diye **işaretlerim**"* cümlesindeki fiil
hangi yüzeyde yaşıyor? Veri alanı vardı (şema v8), gösterimi vardı
(karar 118), yazma yolu vardı (`setLineStore`) — ve **hiçbiri
çağrılmıyordu**. Kod bir jest uydurmadı çünkü seçeneklerinin hepsi var olan
bir kararı deliyordu.

**Tasarım (a)'yı seçti:** Ürün Detayı'nın **eylem grubunun ilk satırı**.
Karar 38'in sabit sırası **bu tek ekleme için** açıldı. Gerekçe maketin
kendi notunda: *"eylem grubunun başında, çünkü satırın olgusu"* — anahtarlar
**ürüne** ait ("her zamanki mi", "önerilsin mi"), bu satır **listedeki
satıra**. Yıkıcı satır sonda kalıyor.

Reddedilenler: çoklu işaretleme (b) *"kullanıcıya baktığı listeyi reyon
bağlamı olmadan yeniden tanımlatıyordu"*, satırda yeni jest (d) karar 110'a
dokunuyordu.

**İki hal, iki ağırlık** (maketten ölçüldü, h56):

| | Değer | Punto | Renk | İkon |
|---|---|---|---|---|
| Hedefteyken | `BİM · hedef` | 17sp/**500** | `onSurfaceVariant` | yok |
| İstisnadayken | `A101` | 17sp/**600** | `onSurface` | storefront 18dp + `chevron_right` 22dp |

Ayrım karar 118'in mantığının aynısı — *"işaret sapmadır"*: hedefteki
satırın söyleyecek özel bir şeyi yok, o yüzden gösterişi de yok. Bütün satır
dokunulabilir, chevron değil: chevron 22dp ve tek başına karar 56'nın 48dp
tabanını karşılamaz.

**Dokunuş beyanın çip ızgarasını açıyor** — `StoreTargetPicker`'ın kendisi,
başlığı *"Nereden alınacak?"* ve temizleme satırı *"Hedefte al"*. İkinci bir
bileşen yazmak aynı jesti iki yerde ayrı ayrı bozulabilir hâle getirirdi.
Izgara Ürün Detayı'nın **üstünde** açılıyor, onun yerine geçmiyor: seçimden
sonra kullanıcı satırın sheet'ine dönüyor ve yazdığının ne olduğunu görüyor.

⚠ **Seçili çip istisna yokken HEDEFE düşüyor.** Satır gerçekten oradan
alınacak; boş bir seçim göstermek satırın durumunu gizlerdi. Hedef çipine
dokunmak da *"Hedefte al"* ile aynı sonucu veriyor —
`ListRepository.setLineStore` hedefin kendisini zaten istisna saymıyor.

### 126 aynı soruyu dördüncü kez sordurdu

Sapma sorusu artık **dört** yerde soruluyor: satırın işareti, alışverişteki
bölümleme, beyan cümlesinin sayısı ve *"Nereden alınacak"* satırı. İlk üçü
bir `ListRowProjection` görüyor; dördüncüsü görmüyor — sheet'in elinde
yalnızca hedefin ve satırın `storeId`'si var.

Kural bu yüzden `deviantStoreName`'e indi ve `deviatesFrom` onun gövdesine
döndü. Ayrılsalardı sheet *"A101"* derken bandın hiçbir şey çizmediği bir
hâl oluşurdu ve **hiçbir şey şikâyet etmezdi**.

⚠ **KOD KARARI — tasarıma sorulacak: hedef yokken satır çizilmiyor.**
Karar 117 hedefi boş bırakmayı meşru kıldı ("Belli değil"), ama
`deviantStoreName`'in ilk şartı hedefin **varlığı**: istisnanın istisna
olabilmesi için bir kural gerekiyor. Hedef yokken satır çizilseydi dokunuşla
yazılan istisna **hiçbir yerde** görünmezdi — ne bandın işaretinde, ne
alışverişin bölümlemesinde, ne başlık cümlesinde. Yani jest, sonucu olmayan
bir jest olurdu.

**Isırma kanıtı:**

| Tersine çevrilen | Düşen test |
|---|---|
| `label()`'da `"· hedef"` düşürüldü | `aRowThatFollowsTheTargetSaysSoNextToTheTargetsName` + `theTargetItselfNeverArrivesAsAnException` |
| `deviatesFrom` kendi gövdesini geri aldı (hedef şartı düştü) | `theSheetAndTheRowAskTheSameDeviationQuestion` + `aRowThatFollowsTheTargetCarriesNoMark` |

### Karar 122 — beyanı olan market silinemez ✅

`docs/38` S2'nin bildirdiği hasar **sessiz ve yıkıcıydı**. Karar 59'un silme
kapısı yalnızca **gözleme** bakıyordu (`hasObservationsAt`) ve karar 117 tam
o boşluğa yerleşti: kullanıcı hiç etiket çekmediği bir zincire *"bugün oraya
gidiyorum"* diyebiliyor — *"2-3 tanesini A101'den alacağım"* cümlesi fiyat
bilgisi gerektirmiyor.

Zincir şuydu: etiket ekranında uzun dokunuş → `softDelete` → sorgudaki
`deletedAt IS NULL` yüzünden ad `null` döner → satırdaki sapma işareti
kaybolur, başlıktaki sayaç düşer, hedef silinmişse beyan hiç çizilmez. Yani
**ilgisiz bir ekrandaki tek uzun dokunuş, kullanıcının yazdığı planı haber
vermeden imha ediyordu.**

**Tasarım (a)'yı seçti:** kapı `hasObservationsAt` **OR** `hasDeclarationsAt`.
Uyarıp silmek (b) reddedildi — *"doğru soruyu yanlış yerde soruyordu"*:
kullanıcı o an etiket işinde ve kaç satırın etkilendiğini görmüyor.

**Sayım iki bacaklı** (`TripDao.linesHeadedTo`):

1. **Satırın kendi istisnası** (`trip_line.storeId`) — *"bunu A101'den
   alacağım"*.
2. **Gezinin hedefi** (`trip.storeId`) ve satırın istisnası **yok** — satır
   hedefi izliyor, yani o da bu markete gidiyor.

⚠ İkincisi olmadan kapı yarım kalırdı ve hasarın **en büyüğü** oradan
geçerdi: hedefi BİM olan on sekiz satırlık bir liste varken BİM silinebilir
olurdu, `deviatesFrom`'un ilk şartı (hedef var) düşerdi ve listedeki **bütün**
sapma işaretleri aynı anda kaybolurdu.

`completedAt` **sorulmuyor**: kapanmış gezi de o marketi gösteriyor; silinirse
Geçmiş'teki o gezinin zinciri adsız kalır — aynı sessiz hasar, başka ekranda.

**Engel sebebini yazıyor**, *"silinemez"* demiyor: `blockedStoreDeleteMessage`
→ **"Bu markete giden 3 satır var."** Sayı iki iş birden yapıyor —
kaybedeceğinin boyunu söylüyor ve kapının nerede açılacağını ima ediyor.
Cümle ViewModel'in dışında, `savedToast` ile aynı gerekçeyle: metin test
edilebilir olmalı.

### Karar 123 — iki yapışkanlık, iki ayrı olay ✅

Seçicinin **ayrı bileşen** olması ve **bütün zincirleri** göstermesi zaten
doğruydu (`StoreTargetPicker`, aramasız/eklemesiz/silmesiz, dokuz zincir
cihazda görüldü). Kalan tek iş `lastDeclaredStoreId`'ydi.

**Yeni kolon açılmadı.** Beyanın kendisi zaten `trip.storeId`'de yazılı;
ikinci bir kolon **üçüncü bir gerçek kaynağı** olurdu ve gün gelir sapardı.
Sorgu en son beyan edilen zinciri `trip` tablosundan okuyor — karar 59'un
yapışkanlığı ise `price_observation`dan (`lastUsedStoreId`). Tasarımın
uyarısı tam buydu: tek değere bağlamak, **A101'de çekilen bir etiketin BİM
gezisinin hedefini değiştirmesi** demekti.

**Yapışkanlık bir ÖNERİ, bir beyan değil:** `trip.storeId` yazılmıyor,
yalnızca çip işaretli geliyor. Otomatik yazsaydık uygulama, kullanıcının
söylemediği bir cümleyi (*"BİM'e gidiyorsun"*) onun ağzından kurmuş olurdu.

⚠ **KOD KARARI — tasarıma sorulacak:** kullanıcı bu gezide *"Belli değil"*i
seçtiyse seçiciyi yeniden açtığında öneri gene işaretli gelir. `trip.storeId`
*"hiç seçilmedi"* ile *"belli değil seçildi"* hâllerinin ikisini de `null`
ile yazıyor; ayırmak için üçüncü bir alan gerekirdi.

### Karar 125 — değişiklik yok ✅

Kodun **iki kararı da onaylandı**: cümlede tek zincirde ad kalır
(*"3'ü A101'de"*), birden fazlasında ad düşer sayı kalır (*"3'ü başka
marketlerde"*); bölümleme zincir başına, başlıkta storefront + `"A101'de"`.
`storeDeclaration` ve `toSections` olduğu gibi duruyor.

⚠ **Compose Spec'te bir tutarsızlık var ve kod maketi izliyor:** denetim
satırı bölüm başlığını `"A101'de · 2"` diye yazıyor, karar 117-118'in satırı
ise `"A101'de (2)"`. Kod bugün adı ve sayıyı `ListSection` üzerinden ayrı
taşıyor; hangisinin çizileceği başlığın kendi bileşeninde. Tasarıma
bildirilecek.

### Isırma kanıtı — 122 ve 123

| Tersine çevrilen | Düşen test |
|---|---|
| Sayımdan hedef bacağı (`OR l.storeId IS NULL AND t.storeId = ...`) | `linesThatFollowTheTargetCountTowardTheTarget` + `anExceptionLeavesTheTargetsCount` |
| Sayımdan `l.deletedAt IS NULL` | `aRemovedLineStopsHoldingItsChainHostage` |
| `ORDER BY startedAt DESC` → `ASC` | `theStickyChainComesFromTheLastDeclarationNotTheLastTag` |
| Cümleden sayı düşürüldü | `engellenen silme kac satiri korudugunu yaziyor` |

⚠ **Testin kendisi bir kez ısırılıp düzeltildi.** `lastDeclaredStoreId`
testi önce **sabit saatle** yazılmıştı: iki gezi aynı `startedAt` damgasını
taşıyordu, yani `DESC` → `ASC` ısırığı **hiçbir testi düşürmüyordu**. Sıralama
iddiası ancak damgalar farklıyken korunuyor; saat ilerletildi.

### Karar 124 — aynı blok, iki mod, iki farklı vaat ✅

`docs/38` S4 **engelleyiciydi** ve iki doğru cümle birbirini kesiyordu:

- Karar 116 *"Alındı"* bölümünü **alışverişte istiyordu** ve maket onu
  çiziyordu (`Alındı (12)` + `expand_more`).
- Kod bölümü **kaldırmıştı** ve gerekçesi ölçülmüştü: alışverişte işaretlenen
  satırın yer değiştirmesi *"hareket eden başparmağın altında yeniden
  sıralama"* demek — kullanıcı bir sonrakine dokunacakken liste kayar ve
  **yanlış ürünü işaretler**.

⚠ Üstelik karar 118'in sapanlar bölümü *"Alındı'nın üstünde"* diye
konumlandırılmıştı; Alındı yoksa **çapa da yoktu**.

**Tasarım (b)'yi seçti ve kodun ölçülmüş gerekçesini kabul etti.** Ayrımın
ekseni **parmak**:

| Mod | Satır | Blok |
|---|---|---|
| **SHOPPING** | yerinde kalır | genişleMEYEN sayaç: `Alındı · 12/18`, **chevron yok** |
| **POST_SHOPPING** | bölüme iner | katlanabilir bölüm: `Alındı 12` + `expand_more`, **kapalı açılır** |

Alışverişte başparmak işaretliyor, yani satırın oynaması yanlış ürünü
işaretletir. Sonrasında işaretlenecek bir şey kalmadı: alınanlar artık
yapılacak iş değil **kayıt**, ve listenin işi geriye kalanı göstermek.

**Chevron çizilmiyor ve bu bir süsleme kararı değil:** chevron bir **vaattir**
— *"dokun, açılır"*. Alışverişte açılacak bir şey yok, satırlar zaten
listenin içinde. Çizilseydi dokunan kullanıcı hiçbir şey olmadığını görürdü.

**Karar 118'in çapası yeniden yazılmadan yerini buldu:** sapan zincir
bölümleri `sections` içinde, blok bütün bölümlerden sonra — yani *"Alındı'nın
üstünde"* cümlesi iki modda da doğru. Cihazda görüldü: `🏪 A101'de 2` bölümü,
altında `Alındı · 0/6`.

### 124 üçüncü bir mod doğurdu — ve saklanmıyor

`shoppingMode` bir `Boolean`; POST_SHOPPING'i ayırmak için üçüncü bir bayrak
gerekmiyor. **Planlamada işaretlenecek bir şey yok** (karar 116 onay dairesini
ve satırın onay hedefini kaldırdı), yani *"işaretli satır var"* cümlesi zaten
*"alışveriş yapıldı"* demek. Üçüncü bir alan, aynı olguyu ikinci kez saklamak
olurdu.

⚠ **Bu tur üç testi tersine çevirdi ve gerekçesi yazılı.** Dosya bir tur
boyunca *"işaretli satır HER İKİ MODDA yerinde kalır"* diyordu; o cümle
yazıldığında mod **sayısı ikiydi**. Kullanıcının şikâyeti (*"liste yaparken
neden alındı/alınmadı var ki?"*) **yerinde duruyor**: planlamada bölüm
hiçbir zaman doğmuyor, çünkü orada işaretlenecek bir şey yok —
`planningHasNoTakenSection` tam olarak bunu tutuyor.

⚠ **KOD KARARI — okuma tasarıma bildirilecek:** POST_SHOPPING'i *"listenin
alışveriş sonrası hâli"* diye okudum. Dayanağı Compose Spec'in kendi cümlesi:
*"sapan zincir bölümleri **her iki modda** bu bloğun üstünde"* — sapan zincir
bölümleri yalnızca **listede** var, dolayısıyla POST_SHOPPING bir liste modu.
Bitir ekranı okunsaydı o cümle anlamsız kalırdı.

⚠ **MAKET BU MADDEDE BAYAT:** Ekran 1'in alışveriş çizimleri hâlâ
`Alındı (12)` + `expand_more` gösteriyor. Karar metni (github.md ve Compose
Spec denetim satırı) `Alındı · 12/18` + chevron yok diyor. Kod **karar
metnini** izledi; maketin alışveriş paneli güncellenmemiş.

### Isırma kanıtı — 124

| Tersine çevrilen | Düşen test |
|---|---|
| Alınanlar alışverişte de bölüme insin | `aCheckedRowStaysInPlaceWhileShopping` + `aCheckedStapleStaysInPlaceWhileShopping` |
| Sonrasında da inmesin (124 öncesi hâl) | `afterShoppingTheTakenRowsCollectAtTheBottom` + `aCheckedStapleLeavesTheStapleSectionAfterShopping` |
| Sayaç tek sayı yazsın | `TakenBlockTest`in **dördü birden** |

### 22. turun kapanışı — cihazda görülenler

| Karar | Cihazda |
|---|---|
| **121** | Yumurta satırı: `🏪 A101␣␣BİM · dün`. Ölçüldü: `A101` 172px'de bitiyor, `BİM · dün` 193px'de başlıyor → **21px = 8,0dp**; ikon ile ad arası 4dp |
| **122** | Gimat'a uzun dokunuş: **"Bu markete giden 1 satır var."** (gözlemi yok, bir istisnası var). A101'de birinci kapı: *"Bu markette gözlem var, silinemez"* |
| **123** | Seçici dokuz zincirin hepsini çiziyor. ⚠ Yapışkanlık **cihazda gösterilemedi**: `trip.storeId` v8'de doğdu, kapanmış hiçbir gezi beyan taşımıyor — sorgu doğru olarak `null` dönüyor. Kanıtı `StoreDeleteGateTest` |
| **124** | Alışverişte `Alındı · 0/6` → iki satır işaretlendi → `Alındı · 2/6`, **satırlar yerinden oynamadı**; `🏪 A101'de 2` bölümü bloğun üstünde. Bırakınca `Alındı 2` + chevron, **kapalı**; dokununca açıldı ve chevron döndü |
| **126** | `Nereden alınacak → 🏪 A101 ›` → çip ızgarası → *"Hedefte al"* → satır `BİM · hedef`'e döndü ve **başlık aynı anda** `2'si A101'de` → `1'i A101'de` oldu |
