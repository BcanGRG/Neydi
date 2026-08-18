# 19 — Tasarım denetimine girdi (E15 sonrası, açık case listesi)

**18 Ağustos 2026.** E15 kod olarak bitti, cihazda 12 gözlem üretti. Bu dosya
**denetimin girdisi**, çıktısı değil: bir sonraki turda tasarıma sorulacak /
bildirilecek her şeyin toplandığı yer. Denetim yapıldığında bulgular
`20-tasarima-sorular-7.md` olarak yazılacak.

> **Bu dosya kapanmamış case bırakmamak için var.** Her madde ya tasarıma
> soruluyor, ya bizim kararımız olduğu işaretleniyor, ya da "yapılacak" diye
> kuyruğa giriyor.

---

## A · Cihazda kullanıcının gördüğü, henüz cevaplanmamış

### A1 — Deklanşöre basınca hiçbir geri bildirim yok ⚠️ *tasarımda YOK*

Kullanıcının ifadesi: *"çekme butonuna basınca bir animasyon bir şey lazım,
şu an çalışmıyormuş hissi veriyor."*

Ölçülebilir gerçek: deklanşörden onay kartına kadar geçen sürede **hiçbir şey
değişmiyor**. `Modifier.pressable`ın %97 ölçek darbesi var ama o basma anının
kendisi; çekimin *olduğunu* söyleyen bir sinyal yok. Fotoğraf yazılıyor + OCR
başlıyor, bu ölçülen sürede (E12: 27 fotoğraf 31 sn ≈ **1,15 sn/kare**)
ekran donmuş görünüyor.

Tasarım dokümanlarında deklanşör geri bildirimi **hiç geçmiyor** — ne örtücü
animasyonu, ne titreşim, ne ses. Gezinme sözleşmesi yalnızca *"kaydetmede tek
darbe"* diyor (haptik), çekimde değil.

**Tasarıma sorulacak:** çekim anının geri bildirimi ne olmalı? Örtücü flaşı
(ekran bir kare beyazlar), yakalanan karenin küçülerek karta dönüşmesi,
haptik darbe, yoksa üçü birden mi?

### A2 — Onay kartı açık temada da koyu · *tasarım kararı, bizim değil*

Kullanıcı iki kez bildirdi. **Tasarım açıkça istiyor:**

> *"Karanlık tema · kamera — Kamera ve onay kartı her iki temada da koyu; tema
> değişimi bu akışı etkilemez."* — Gezinme Sözleşmesi

Yani kod doğru. Ama kullanıcı bunu **iki kez hata sanarak** bildirdi ve bu
başlı başına bir sinyal: gerekçe kullanıcıya görünmüyor.

**Tasarıma sorulacak:** kural bilinçli mi, yoksa kamera ekranının koyu olması
varsayımından mı geliyor? Açık temada kullanan biri için kart "yanlış
uygulanmış" hissi veriyor. Karar korunacaksa **neden** korunduğu karar
defterine yazılmalı (gözün karanlık vizöre uyum sağlaması? kameranın üstünde
açık bir kartın yüzen beyaz dikdörtgene dönmesi?).

---

## B · Tasarımda var, kodda yok

| # | Madde | Kaynak | Durum |
|---|---|---|---|
| B1 | **Kartın başındaki kırpılmış etiket görüntüsü** | Ekranlar 2-4: *"etiket fotoğrafı yalnızca kart açıkken yaşar: kırpılmış hâli kartın başında, kullanıcı okuduğunu doğrulasın diye"* | Kod kamerayı **karartıyor**, kareyi göstermiyor. Kullanıcının "ne okudum" doğrulaması yok. |
| B2 | **1,5 sn iskelet eşiği** | ROADMAP E15: *"OCR 1,5 sn'yi geçerse alanlar iskelet"* | İskelet var ama **eşik yok** — OCR ne kadar sürerse iskelet o kadar duruyor. Ölçüm 1,15 sn/kare olduğu için eşik pratikte hiç tetiklenmezdi; eşiğin amacı belirsiz. |
| B3 | **Ürün / Marka / Market seçicileri** | Ekranlar 2-4: satırlarda `chevron_right` | Chevron çizildi ama **seçici yok**: Ürün ve Market satır altında açılıyor, Marka hiç düzenlenemiyor. |
| B4 | **Marka çip sayfası** | Karar 39 | Yazılmadı. Marka yalnızca OCR önerisi; kullanıcı değiştiremiyor. |
| B5 | **Yatay (landscape) düzen** | Gezinme Sözleşmesi | Hiç ele alınmadı. |
| B6 | **Ekran okuyucu / haptik** | Gezinme Sözleşmesi: *"kaydetmede tek darbe"*, fiyat çipleri *"38 lira 50 kuruş"* diye okunmalı | Projede haptik API'si **yok**. |

---

## C · Bizim verdiğimiz, tasarımın onaylamadığı kararlar

Bunların hepsi ölçümle gerekçelendirildi ama **tasarım görmedi**. Denetimde
tek tek onaylanmalı ya da reddedilmeli.

| # | Karar | Gerekçe | Risk |
|---|---|---|---|
| C1 | **Vazgeç düğmesi kaldırıldı** | Tasarımın kart durumunda da yok; çıkış yolu geri tuşu | Geri tuşunu bilmeyen kullanıcı sıkışabilir |
| C2 | **Grameri çözülmemiş zincirde hiçbir alan doldurulmuyor** | Migros'ta patatese 4389 TL yazılıyordu (`docs/18`) | Kullanıcı o markette her şeyi elle yazıyor |
| C3 | **Manşet ile birim fiyat çelişirse fiyat yazılmıyor** | Ölçüm: 2 BİM etiketinde **doğru** manşet de eleniyor | Sessiz kapsam kaybı |
| C4 | **Migros'ta ürün adı hiç okunmuyor** | Çıktılar `TO00ge`, `NUIK KREMIASI orünün lot ve…` | O markette ad her seferinde elle |
| C5 | **Marka/ad sözcük kapısı** (3 harf + sesli + markada rakam yok) | Cihazda `oOoao000`, `Tntkn`, `A.Ş.`, `KG` yazıldı | Meşru kısa markalar elenebilir |
| C6 | **E15 kabul ölçütü ikiye ayrıldı** | *"1 etiket → Tahmini sepet"* ulaşılamaz: `MIN_PRICED_ITEMS = 3` | ROADMAP'in orijinal ölçütü yanlıştı |

---

## D · Tasarımda anlamsız / çelişkili görünenler

Denetimde doğrulanacak — bunlar **iddia**, henüz kanıt değil.

- **D1 · Money fiyatı ile "elle fiyat girilmez" kuralı.** Tasarım fiyatı
  düzenlenebilir tek alan yapıyor ("elle fiyat girilmez kuralının tek
  istisnası"). Ama Migros'ta iki geçerli fiyat var (normal + Money kart) ve
  hangisinin kaydedileceği tasarımda hiç geçmiyor. Kullanıcı Money kartı
  varsa kaydettiğimiz fiyat onun ödediği fiyat değil.
- **D2 · Çok-al etiketleri.** Metro `ÇOK AL AZ ÖDE` etiketlerinde tek ürün ve
  çoklu alım fiyatı ayrı basılıyor. Fiyat geçmişi hangisini taşımalı?
  Tasarımda yok.
- **D3 · `packSize`/`priceUnit` ile `unitPriceMinor` ilişkisi.** ROADMAP
  "açık kararlar" 1. maddesi *"E14 `priceUnit` ile ikisini de taşıyor"*
  diyor; şemada birim fiyatın **tutarı için kolon yok**. E17 "paket mi kg mı"
  sorusunu depolanmış veriden cevaplayamaz.
- **D4 · Aynı ürünün ikiye bölünmesi.** Cihazda aynı süt iki ürün oldu
  (`SEK sÜT %0,5 YAĞLI` + `SÜT %0,5 YAĞLI |1L`). Karar 26 kimliği
  market+marka çifti yapıyor ama **ürün** eşleşmesi `resolveProduct`'ta ve
  tasarım o eşleşmenin ne kadar gevşek olacağını söylemiyor.

---

## E · Denetimin kapsamı

Okunacaklar: `docs/tasarim/` altındaki dokuz dosya, `11-tasarim-kararlari.md`
(45 karar), ölçümler `17-e12-etiket-olcumu.md` ve
`18-zincir-karsilastirmasi.md`.

Aranacaklar:
1. **Mantık hatası** — kendi içinde çelişen kurallar
2. **UX eksiği** — kullanıcının sıkışacağı, geri dönemeyeceği yerler
3. **Eksik tasarım** — kodda gereken ama hiç tarif edilmemiş ekran/durum
4. **Gereksiz karmaşa** — iki kural bir işi yapıyorsa, ya da kural
   uygulanamıyorsa

Çıktı `20-tasarima-sorular-7.md`, numaralı ve her maddede *"neden soruluyor"*
satırıyla — önceki turların biçimi.
