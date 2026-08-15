# Tasarıma sorular — ikinci tur

**15 Ağustos 2026.** Birinci turun on iki maddesi [Kararlar](tasarim/Neydi%20-%20Kararlar.dc.html) ile kapandı ve uygulandı. Bu dosya **kararları uygularken ortaya çıkan** yeni eksikleri topluyor.

Aynı biçim: **tasarımın varsaydığı** → **gerçek veride olan** → **karar gereken soru**.

---

## 1. Uzun mağaza adı başlık alt satırını yutuyor

**Tasarımın varsaydığı.** Fiş Kontrol alt satırı: *"Migros Ataşehir · 12 Ağustos 15:31 · 2 parça"* — üç bilgi tek satıra sığıyor.

**Gerçek veri.** Fişte basılı olan **ticari unvan**, marka adı değil. Cihazdaki gerçek fiş: `AKYURT SÜPERMARKET GIDA İNS.SAN.VE TİC. A.Ş.` — tek başına satırın tamamını doldurup tarihi ve parça sayısını ekran dışına itiyor. `FiLE MARKET MAGAZACILIK ANONIM SIRKETI` de aynı.

Uygulama şu an satırı kırpıyor, yani **tarih ve parça sayısı hiç görünmüyor** — halbuki tasarımın o satıra koyduğu asıl ayırt edici bilgi onlar (aynı marketten iki fiş varsa tarih ayırıyor).

**Soru.** Ad nasıl kısaltılmalı?
- İlk iki kelime (`AKYURT SÜPERMARKET`)?
- `chainKey`'in ürettiği zincir adı (`AKYURT`) — alias öğrenmesi zaten bunu kullanıyor?
- Ad kırpılsın ama tarih **hiç kırpılmasın** (ad `weight(1f)`, tarih sabit)?

Aynı sorun Geçmiş satırında da var.

## 2. Adı olmayan fiş satırı ne yazacak?

**Tasarımın varsaydığı.** Fiş satırı: `{{ r.name }}` üstte, `{{ r.ocr }}` altta gri — yani okunabilir bir ad **var** ve ham metin onu doğruluyor.

**Gerçek veri.** AKYURT düzeninde (F4.14) ürün adı ayrı satırda basılıyor, tutar satırında yalnızca `3 8683206511079 1 Adet 189,90 %20 189,90` var. Ayrıştırıcı adı **barkod** okuyor, eşleşme bulunamıyor ve satır başlığı boş kalıyor.

Uygulama şu an başlığa **"Eşleşmedi"** yazıyor — bir hata mesajını ürün adının yerine koyuyor, yani yedi satırın yedisi de "Eşleşmedi" diyor.

**Soru.** Ad hiç okunamadığında satır başlığı ne olmalı? Ham metnin kendisi mi (barkod dahil), *"Adı okunamadı"* gibi bir şey mi, yoksa satır başka türlü mü çizilmeli? Bu F4.14 çözülene kadar geçerli olacak bir hal — ve F4.14 sonrası da bazı satırlarda kalacak.

## 3. "Toplam okunamadı" çipi bir manşet kadar yer kaplıyor

**Tasarımın verdiği.** Aritmetik çipi 32dp, `align-self: flex-start`, kısa metin (*"Fişin tamamı tutuyor"*).

**Gerçek veri.** Üçüncü hal olan *"Toplam okunamadı · satırlar 1.085,65 TL"* çipi ekranın neredeyse tamamını kaplıyor ve manşetin yerini alıyor — üstelik tam da manşetin **çizilmediği** durumda (toplam yok).

**Soru.** Toplam okunamadığında ne manşet olmalı? Satırların toplamı manşet olup çip *"toplam okunamadı"* mı desin — yoksa bu hal tasarımın istediği gibi sessiz mi kalsın?

---

## Not: birinci turdan taşınan tek madde

**F6.6 Kurulum'un tetikleyicisi** karara bağlandı (`setupCompletedAt` boş **ve** hane hiç ürün görmemiş) ama uygulama henüz yazılmadı; veri katmanı hazır, ekran sırada.
