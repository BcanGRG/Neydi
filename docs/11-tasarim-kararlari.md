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
| 31 | Boş durum 04 kategorisi değişmedi, yalnızca gerekçe metni | 📄 doküman |

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

**Bu turda yapıldı:** kod envanteri **23 → 13**. Silinenler: `ArrowUpward`,
`ArrowDownward` (DeltaChip kendi okunu çiziyor), `Undo`, `FilterList`
(karar 3), `Functions`, `ContentCopy` (karar 24), `LightMode`,
`DragIndicator`, `HourglassTop`, `Error`.

Koddaki 13 = tasarımın 12'si − `bolt`/`info` (E15'te ekleniyor, şimdi
eklemek ölü kod olurdu) + `PushPin`, `Check`, `ContentPaste` (üçü de
kullanımda ama tasarım envanterinde yok — **tasarıma sorulacak**).

### ⚠ A yolu bugünkü kodla uygulanamıyor

Design'ın önerdiği A yolu tek bir `IconDefaults` istiyor: **24dp, wght 300,
opsz 24, açık temada GRAD 0, karanlıkta GRAD 100**.

Bunlar **Material Symbols değişken fontunun eksenleri**. Uygulama ise
`androidx.compose.material.icons` kullanıyor — bunlar **statik
`ImageVector`**, ekseni yok. `wght 300` vermek mümkün değil.

Üç seçenek, üçü de bir iş kalemi:
1. **Material Symbols fontunu bundle et** ve ikonları `Text` olarak çiz —
   `fontVariationSettings` ile eksenler gerçekten çalışır; Fraunces'te aynı
   yol seçilmedi çünkü iOS'ta güvenilir değildi *(aynı risk burada da var)*
2. **12 ikonu wght 300'de elle `ImageVector` olarak taşı** — bağımlılık yok,
   APK etkisi birkaç KB; design'ın B yolu için önerdiği yöntemin aynısı
3. **B yoluna geç** (Phosphor) — kimlik kararı, ayrı konu

Karar tasarımın; ROADMAP'e **F11.11** olarak girdi.

---

## Tasarıma sorulacaklar

1. **`PushPin`, `Check`, `ContentPaste`** ikon envanterinde yok ama kodda
   kullanımda (sabit ürün rozeti, onay tiki, pano yapıştırma). 12'lik listeye
   girecek mi, yoksa yerlerine kalan 12'den biri mi geçecek?
2. **İkon A yolu** yukarıdaki üç seçenekten hangisi? (Compose'da değişken font
   ekseni ancak fontu bundle edip `Text` çizerek çalışıyor.)
3. **Boş durum 01** başlıkta `Son alışveriş: 3 gün önce · 642 TL` yazıyor —
   biçim kuralı her tutarın `~` taşımasını söylüyor. Çerçeve mi güncellenecek?
4. **Boş durum 03** hâlâ dört hedefli toolbar çiziyor (`add`, `undo`,
   `filter_list`, `Bitir`) — karar 3 ikiye indirmişti. Çerçeve tazelenecek mi?
5. **Boş durum 07** Katılma kodu satırını `R4TB9C` + kopyala ikonuyla
   çiziyor — karar 24 soluk "Faz 7'de açılıyor" diyor. Aynı çerçevede
   Mağazalar satırı da chevron'lu ve "İlk gözlemden öğrenilecek" değerli;
   karar 23 ve 24 ikisini de kaldırmıştı.
