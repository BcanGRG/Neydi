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
| **34** | Envanter **17** — delta çipinin `arrow_upward`/`arrow_downward` okları girdi; `check` ile `check_circle` ayrı | ⏳ **F11.29** (15 taşındı) |
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
**Kesin tutar diye bir biçim yok** — her tutar gözlemden hesaplanır.

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
**etiket fotoğrafı kayıttan sonra silinir** · **her tutar tahmindir ve önünde
`~` vardır** · **marka gözlemin alanıdır** · aynı etiket metni aynı markette
bir kez sorulur · işaretleme snackbar açmaz · toast kuyruğu yoktur.

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
`HourglassTop`, `Error`), sonra karar 34 ile **15**'e sabitlendi.

Karar 34 üç ikonu geri getirdi: `push_pin`, `check`, `content_paste` — üçü de
kullanımdaydı ama tasarım envanterinde yoktu. `check` ile `check_circle`
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
taraf seçilir. Sonuç: **15 ikon Phosphor Regular 2.1.1 (MIT) çizimleriyle elle
`ImageVector` olarak taşındı.**

Kazanç yalnızca kimlik değil: `material-icons-extended` bağımlılığı tamamen
düştü — o artifact JetBrains tarafında **1.7.3'te donmuştu** ve tek kullanıcısı
`NeydiIcon.kt`'ydi. Şimdi 15 path dizesi, birkaç KB kaynak.

**Ara katman sınandı ve tuttu:** set baştan sona değişti, `NeydiIcons.ArrowBack`
diyen 17 çağrı yerinin **hiçbiri** değişmedi. `NeydiIcons`'un varlık sebebi tam
olarak buydu ve ilk kez gerçek bir taşımada ödendi.

**Elle taşımanın iki sessiz hata modu** teste bağlandı: kırpılmış bir `d` dizesi
boş vektör üretir ve hiçbir şey şikâyet etmez; satır kopyalanıp path değiştirmeyi
unutmak iki ikona aynı çizimi verir. `NeydiIconsTest` ikisini de yakalıyor.
Testin *ölçemediği* şey çizimin ne olduğu — onun için `NeydiIcon.kt`'de on beş
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
Türetilmiş bir tutar olduğu için biçim kuralına aykırı. → **F11.17**

**2 · İkonografi dosyası karar 33'ü eski çiftiyle örnekliyor.** Karar defteri
ilişkiyi doğru yazıyor (*"ikon yanındaki metinden bir kademe açık"*) ama
İkonografi aynı kuralı hâlâ *"metin `#E4D8C9`, ikon `#F5EDE6`"* diye
örnekliyor. İki ayna kuralda hemfikir, örnekte değil; defter esas alındı.
→ **F11.18**
