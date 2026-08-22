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
