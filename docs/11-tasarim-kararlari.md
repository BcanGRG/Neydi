# Tasarım kararları — koda çevrilmiş hâli

**16 Ağustos 2026.** Design pivot turunu tamamladı ve karar defterini **yirmi
maddeye** indirdi: fiş dönemine ait on bir karar (4, 9, 13–21) defterden
tamamen çıkarıldı. İki de yeni dosya geldi — **Gezinme sözleşmesi** ve
**İkonografi**.

Bu dosya karar defterinin kopyası değil; **her kararın kodda karşılığı ne, hangi
adımda yapılıyor** onu söylüyor. Kararların kendisi ve gerekçeleri
[`tasarim/Neydi - Kararlar.dc.html`](tasarim/) altında.

> **Ayna durumu.** `docs/tasarim/` altındaki `.dc.html` kopyaları bu oturumda
> tazelendi: **Ekran 1**, **Ekranlar 2-4**, **Ekranlar 5-8**, **Tasarım
> sistemi** ve yeni **Gezinme sözleşmesi**. Tazelenmeyen üç dosya —
> **Kararlar**, **İkonografi**, **Boş durumlar**, **Compose spec** — hâlâ
> pivot öncesi sürümü taşıyor; içerikleri okundu ve aşağıya işlendi ama
> bayt kopyaları güncellenmedi. Canlı kaynak:
> [design projesi](https://claude.ai/design/p/8eea982a-c3f6-4008-8789-81aaf478b51d).

---

## Yirmi kararın kod durumu

| # | Karar | Kod durumu |
|---|---|---|
| 1 | Alışveriş modunda `more_vert` → tek madde "Alışverişi bırak" | ✅ var |
| 2 | "Verilerimi sil" tam ekran onay destinasyonu; kapsamda fotoğraf yok | ✅ var · kapsam E11'de düzeldi |
| 3 | Toolbar iki hedef (`add` + Bitir) | ✅ var · ikonlar bu turda silindi |
| 5 | İlk gün 12 ürün çipi, `commonalityRank`'tan | ✅ var |
| 6 | Kurulum iki adım; tetikleyici `setupCompletedAt` + ürün sayısı | ✅ var |
| 7 | Ekran 3'ün üç bölüm notu | ✅ var |
| 8 | Toast: aksiyonsuz, 2 sn, kuyruksuz | ✅ var |
| 10 | Avatar tek kişilik hanede de; `priceChip` 14sp / `priceRow` 17sp | ✅ var |
| 11 | **Revize:** 7 zincir tohumlanır, market çekimde seçilir, yapışkan | ⏳ **E13** · Ayarlar metni bu turda düzeldi |
| 12 | Ekle sheet'indeki işaret "bu listede var" | ✅ var |
| 22 | Zincir adı etiketteki gibi büyük harf, caps satır 500 ağırlık | ✅ var (locale'siz dönüşüm zaten yasak) |
| 23 | Zincirler satırından chevron kalktı | ✅ **bu turda** |
| 24 | Boş hâlde Mağazalar çizilmez; Katılma kodu soluk "Faz 7'de açılıyor" | ✅ **bu turda** |
| 25 | Onay kartı fotoğrafın üstünde; eksik alan amber şerit; dışına dokunmak kapatmaz | ⏳ **E15** |
| 26 | "Nerede ucuz" satırının kimliği **market + marka** çifti | ⏳ **E17** |
| 27 | Etiket çekimine tek giriş: **liste başlığında kamera hedefi** | ⏳ **E15** |
| 28 | Alışveriş başlığındaki market = o gezide son çekilen etiketin marketi | ⏳ **E18** |
| 29 | Fotoğraf Kaydet'e basıldığı anda silinir; hiçbir yüzey çizmez | ⏳ **E15** · planla aynı |
| 30 | Geçmiş'te gözlem satırı yok; gezi satırı tarih + kalem + `~` tutar | ✅ E8 · `~` tutar **E18** |
| 31 | Boş durum 04 kategorisi değişmedi, yalnızca gerekçe metni | ✅ tasarım tazeledi |
| **32** | **İkon A yolu düştü** — 15 ikon Phosphor Regular çizimleriyle elle `ImageVector` | ✅ **F11.11** |
| **33** | Karanlık temada GRAD yerine **renk kademesi**: metin `#E4D8C9`, ikon `#F5EDE6`; dolgulu ikon telafi almaz | ✅ F11.11 · renkler **F11.14** |
| **34** | Envanter **15** — `check`/`push_pin`/`content_paste` geri girdi; `check` ile `check_circle` ayrı | ✅ kodda zaten öyle |
| **35** | Gizlilik notu + katılma kodu metni onaylandı | ✅ **birebir uygulandı** |

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

| Yüzey | En az | Altında |
|---|---|---|
| Ürün Detayı sparkline | **3 gözlem** | Grafik hiç çizilmez | ✅ bu turda |
| "Nerede ucuz" bölümü | **2 market** | Bölüm çizilmez |
| Delta çipi | **2 gözlem** | Çip yok, "ilk gözlem" ibaresi de yok |
| Ambalaj küçülmesi | **2 farklı boy** | Sessiz |
| Sepet tahmini | **3 fiyatlı ürün** | Satır hiç görünmez | ✅ bu turda |
| "Her zamankiler" öğrenmesi | **3 gezi** | Kurulumdaki seçim neyse o |
| "Bitmiş olabilir" | **4 alım** | Bölüm çizilmez |
| Eksik olabilir ekranı | **1 satır** | Ekran açılmaz, toast bilgilendirir |
| Geçmiş grafiği | **3 gezi** | Çubuklar çizilmez |
| Ayarlar · Mağazalar | **1 gözlem** | Bölüm çizilmez | ✅ var |

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

## Yeni açık sorular

**1 · Karar 33'ün renkleri palete oturmuyor.** Karar *"metin `#E4D8C9`, ikon
`#F5EDE6`"* diyor. Ama tasarımın kendi `handoff/tokens.json`'ı karanlık
`textPrimary`yi **`#F5EDE6`** yazıyor — yani kararın "ikon" rengi uygulamanın
bugünkü *metin* rengi — ve `#E4D8C9` palette hiç geçmiyor (tasarım
dokümanlarında yalnızca HTML çerçevesinin kendi zemin/metin rengi olarak var).

İki okuma mümkün:
- **(a)** Karanlık gövde metni bir kademe insin (`#F5EDE6` → `#E4D8C9`), ikon
  yerinde kalsın. Sayılara birebir uyar ama **bir ikon kararından türetilen
  palet çapında bir değişiklik** olurdu ve her ekranı etkilerdi.
- **(b)** Çift, iki mutlak renk değil bir **ilişki**: ikon yanındaki metinden
  bir basamak yukarıda. Uygulamanın karanlık rampası `#C6B6A9` → `#F5EDE6` ve
  `#E4D8C9` tam bu ikisinin arasına düşüyor.

**(b) uygulandı** — ikincil metnin yanındaki ikon `#E4D8C9`'a çıkıyor, birincil
metnin yanındaki zaten rampanın tepesinde. Telafi bir *kaldırma*; tepenin
üstüne çıkamaz. Okuma `Color.kt`'de not olarak duruyor ve `NeydiColorTest`
ölçüyor. → **F11.14**

**2 · Ekran 1 başlık örneği merdivenle çelişiyor.** Tasarımın örneği *"Son
alışveriş: 8 gün önce · 642 TL"*; kendi tarih merdiveni ise 7–13 günü **"geçen
hafta"**ya topluyor. Kod merdiveni esas aldı (daha yeni, daha açık ve
sayıyla tanımlı). Örnek mi güncellenecek, yoksa başlık merdivenin dışında mı
kalacak? → **F11.13**
