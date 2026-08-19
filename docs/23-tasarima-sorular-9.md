# Tasarıma sorular — dokuzuncu tur

**Konu: kodlanamayan beş madde. Hepsinde eksik olan çizim değil.**

56 ajanlı denetim 41 bulgu doğruladı; 35'i kapandı. Kalan beşi kapanmadı ve
sebep hepsinde aynı sınıfta: **maket bir şey çiziyor, ama o şeyi çizmek için
gereken veri ya da karar yok.** Tahminle doldurmak, uygulamanın kendi kuralını
("yanlış bir şey göstermektense hiçbir şey göstermemek") çiğnemek olurdu.

Her madde: maketin ne dediği → neyin eksik olduğu → sorulan şey.

> Bu tur [`22-tasarima-sorular-8.md`](22-tasarima-sorular-8.md) ile birlikte
> okunmalı. Orada Ekle akışının **yapısal olarak** yeniden ele alınması
> soruluyor; aşağıdaki 1 ve 2 numaralı maddeler o akışın içindeki
> ayrıntılar. **Eğer akış yeniden kurulacaksa bu ikisi kendiliğinden düşebilir**
> — o yüzden önce 22'yi cevaplamak mantıklı.

---

## 1 · Ekle sheet'inde reyon başına ürün sayısı

**Maket** (`Ekranlar 2-4.dc.html:62, :138, veri :502-515): her reyon hücresi üç
parça — 56×56 kutucuk, reyon adı (12px/500), altında sayaç **"18 ürün"**
(12px/500, `#8A7666`, tabular-nums).

**Eksik olan: sayının kendisi.** Hiçbir yerde tutulmuyor:
- `Category` = `(id, name, sortOrder, tintArgb)` — sayaç kolonu yok
- `CategoryDao`'nun tek sorgusu `SELECT * FROM category ORDER BY sortOrder`
- `CatalogSeedDao`'da yalnızca kataloğun **tamamı** için `COUNT(*)` var,
  `GROUP BY categoryId` yok

Yazılması zor değil (tek sorgu + tek `StateFlow`). Sorulan şey **sayının ne
işe yaradığı**:

> **Soru 1.** "18 ürün" kullanıcıya ne söylüyor? Reyon seçimini değiştiren bir
> bilgi mi, yoksa kutucuğu doldurmak için mi? Eğer amaç "bu reyon dolu/boş"
> ayrımıysa, kullanıcının kendi listesindeki ürün sayısı (ör. "3'ü listende")
> katalog sayısından daha mı yararlı olur?

---

## 2 · Ekle sheet'inde ürün fiyatı

**Maket** (`Ekranlar 2-4.dc.html:77, :153, veri :517-523): her ürün hücresinin
alt satırı fiyat — "42,00 TL", "89,90 TL", "138,50 TL".

**Eksik olan hem veri hem karar:**
- `CatalogSeed` = `(id, name, matchKey, categoryId, commonalityRank,
  defaultUnit)` — fiyat alanı **yok**
- Fiyatlar `PriceObservation`'da ve **`productId`** ile anahtarlı, yani hanenin
  kendi `Product` satırıyla. Katalog tohumunun `id`'siyle değil.
- Asıl mesele: katalog tohumu **hanenin hiç almadığı** bir ürün olabilir. O
  zaman sıfır gözlem vardır. Maketteki gibi *her* hücrede fiyat çizmek mümkün
  değil.

> **Soru 2.** Hiç gözlemi olmayan üründe alt satırda **ne yazıyor**? Üç seçenek:
> (a) satır hiç çizilmiyor — grid'in ritmi hücreden hücreye değişir;
> (b) yerine birim yazıyor ("kg", "adet") — bugünkü davranış;
> (c) yerine bir yer tutucu ("—") — boşluk kalır ama hizalama korunur.
>
> **Soru 2b.** Fiyat çizilecekse hangi fiyat? "En son gördüğün" mü, "en ucuz
> gördüğün market" mi? İkincisi listeye eklerken market kararı da veriyormuş
> gibi okunabilir.

---

## 3 · Ürün Detayı'nın trend manşeti

**Maket** (`Ekranlar 5-8.dc.html:247, :365`): **"Süt 32 TL → 41 TL · son 3
ayda %28 arttı"**

Tek gözlemlik manşet ("Son ödediğin: 138,50 TL") kodlandı ve çalışıyor. Trend
hâli kodlanmadı, çünkü üç şey birden gerekiyor ve üçü de bugün yok:

1. **Ay aralığı biçimleyicisi yok.** `DateText.kt` haftada bitiyor; "son 3
   ayda" uydurma bir metin olurdu.
2. **Kuruşsuz tam lira biçimi yok.** `formatMinor` → "32,00 TL",
   `formatEstimate` → "~32 TL". Tilde bir **tahmin** işareti; gözlenmiş bir
   fiyata koymak yalan olur.
3. **Yüzde, ambalaj değişimi üzerinden hesaplanamaz** — ve maketin kendi
   örneği tam da o vaka: 5 L → 4 L. `PriceHint.PackChanged` bu yüzden zaten
   trendi bastırıyor.

> **Soru 3.** Manşetin zaman aralığı nasıl seçiliyor — sabit mi ("son 3 ay"),
> yoksa eldeki gözlemlerin yayıldığı aralık mı ("6 Haziran'dan beri")?
>
> **Soru 3b.** Ambalaj değiştiyse manşet ne diyor? Yüzde iddia edemeyiz;
> "900 g → 800 g, aynı fiyat" gibi bir cümle mi, yoksa manşet o üründe hiç
> çizilmiyor mu?
>
> **Soru 3c.** Kaç gözlemden sonra trend manşeti tek gözlem manşetinin yerini
> alıyor? (Eşik tablosunda karşılığı yok; sparkline 3, delta çipi 2.)

---

## 4 · Geçmiş başlığındaki 6 çubuklu mini grafik

**Maket** (`Ekranlar 5-8.dc.html`, "6 Geçmiş"): altı çubuk, 96dp yükseklik,
`primary`, üst köşeler 8dp, 10dp ara.

**Eksik olan iki karar:**

1. **Çubuğun yüksekliği neyi ölçüyor?** Gezi tutarı mı, kalem sayısı mı?
   İkisi çok farklı grafikler üretir — "8 kalemlik ucuz alışveriş" ile "2
   kalemlik pahalı alışveriş" ters sıralanır.
2. **Tutarı hesaplanamayan gezi nasıl çiziliyor?** Bu hâl kodda **var**:
   `HistoryTrip.estimateMinor == null` (eşik: 3'ten az fiyatlı ürün) ve
   `TripRow` bugün tutarı hiç yazmayarak çözüyor. Grafik sıfır yükseklikli bir
   çubuk çizerse "bedava alışveriş" demiş olur; çubuğu atlarsa altı çubuk
   beşe düşer ve aralar bozulur.

> **Soru 4.** Çubuk neyi ölçüyor?
>
> **Soru 4b.** Tutarsız gezi: boşluk mu, kesik/soluk çubuk mu, yoksa grafik
> tamamen mi düşüyor?
>
> **Soru 4c.** Eşik ne? (Karar defterinin kendi mantığı "üç geziden az ise
> çizme" derdi — tek çubuklu grafik grafik değildir — ama yazılı değil.)

---

## 5 · Alışveriş sonrası özet: sheet mi, satır içi kart mı?

**Maket** (`Ekran 1 Liste.dc.html`): özet, listenin **içinde** duran ve kendi
kapatma ikonu olan bir kart. Arkasındaki liste ve alt şerit görünür kalıyor.

**Kodda:** `ModalBottomSheet` — karartmalı, listeyi kapatan, kapatmak için
"Tamam" butonu gerektiren bir yüzey.

Bu, denetimin "yapısal" dediği tek madde. Kararı da etkiliyor: karar 45 artık
kodlandı (tutar hesaplanamıyorsa kart **hiç açılmıyor**), yani "hiç açılmama"
davranışı sheet'te de kartta da aynı — ama **nereye** açıldığı farklı.

> **Soru 5.** Özet neden listenin içinde? Tahminim: alışveriş biter bitmez
> kullanıcının gördüğü şey **listenin son hâli** olmalı, özet ona bir yorum.
> Sheet bunu tersine çeviriyor. Doğru mu?
>
> **Soru 5b.** Kart kapatılınca ne oluyor — kalıcı olarak mı gidiyor, yoksa
> Geçmiş'ten geri gelebiliyor mu? (Bugün sheet kapanınca özet kayboluyor ve
> Geçmiş'te tutar var ama "N ürün alındı" satırı yok.)

---

## Bu turda kod tarafında ne yapıldı

Kalan altıncı madde (**Kurulum adım 2/2'de geri**) tasarım sorusu değildi,
yalnızca dosya bölümlemesi yüzünden açık kalmıştı; bu turda kapandı.
`SetupViewModel.previous()` eklendi ve geri yalnızca adım 2'de yakalanıyor —
adım 1'de yakalanmadığı için sistem varsayılanı, yani "uygulamadan çık",
kendiliğinden doğru davranış oluyor.

Beş maddenin hiçbiri için **tahminle kod yazılmadı**. Cevaplar gelince beşi de
küçük işler; bugün eksik olan tek şey ne çizileceğine dair karar.
