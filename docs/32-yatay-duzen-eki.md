# 32 — Ek: yatay düzenin tarif edilmemiş yarısı (karar 61)

**23 Ağustos 2026.** Karar 61'i uygularken iki şey çıktı. Kısa tur; on beşinci
turun eki sayın.

Karar 61 birebir:

> *"Kart yatayda sağ yarıda dikey panel; sayısal klavye sol yarıyı (vizörü)
> örter, kartı asla örtmez; amber şerit kartın içinde kalır."*

Birinci cümle uygulandı ve cihazda çalışıyor. İkinci cümle **uygulanamıyor**,
üçüncüsü zaten sağlanıyor.

---

## S1 — "Klavye sol yarıyı örter, kartı asla örtmez" Android'de mümkün değil

Android'de IME **alttan ve tam genişlikte** gelir; bir yarıya hapsedilemez.
Sistem klavyesini yarım ekrana sıkıştıran bir API yok — üçüncü parti
klavyelerin "yüzen" kipi var ama onu uygulama seçemez, kullanıcı seçer.

Yani yatayda klavye açıldığında kartın alt kısmı **kaçınılmaz olarak**
örtülüyor.

Dikeyde bu sorunu **karar 70** çözmüştü: klavye açılınca kırpım şeridi
toplanıyor, kart kaydırılmıyor. Aynı çözüm yatayda da çalışıyor (kart kendi
içinde kayıyor, şerit toplanıyor) ama kararın cümlesi bunu söylemiyor.

**Soru:** karar 61'in ikinci cümlesi nasıl düzeltilsin?

- **(a)** *"…klavye alttan gelir ve kartın alt kısmını örter; kart kendi içinde
  kayar, şerit toplanır (karar 70)"* — bugün yapılan bu.
- **(b)** Yatayda fiyat alanına dokunulunca kart **geçici olarak tam ekran**
  olsun (vizör tamamen gizlensin) — klavyenin üstünde bütün alanlar görünür.
- **(c)** Yatayda sayısal giriş için klavye **hiç açılmasın**: kartın kendi
  içine bir tuş takımı çizilsin. (Fiyat zaten yalnızca rakam alıyor.)

---

## S2 — Yatay **vizör** hiç tarif edilmemiş

Karar 61 yalnızca kartı anlatıyor. Vizörün yatay hâli hiçbir yerde çizilmemiş
ve bugünkü kod, dikey için yazılmış kuralları yatayda uygulayınca **bozuluyor**
(cihazda ölçüldü, 2280×1080):

| Öğe | Dikeyde | Yatayda bugün |
|---|---|---|
| 3:2 rehber kadrajı | genişliği doldurur, oran doğru | genişliği doldurunca **1520px yükseklik** ister, ekran 1080 → taşıyor, yalnızca iki dikey kenarı görünüyor |
| *"Etiket kadraja otursun."* | rehberin alt içinde | rehber taştığı için **ekranın altına düşüyor**, kırpılıyor |
| Deklanşör | altta ortada | altta ortada — çalışıyor ama kart açılınca kartın altında kalıyor |

Kısa kenardan bağlayarak (rehber yüksekliği doldurur, genişlik ondan çıkar)
düzelttik. Ama bu bizim seçimimiz, tasarımın değil.

**Soru:** yatay vizör nasıl olacak?

- Rehber **kısa kenardan** mı bağlansın (bugün yapılan), yoksa vizörün belli
  bir yüzdesi mi olsun?
- Deklanşör yatayda **altta ortada** mı kalsın, yoksa **sağ kenarda dikey
  ortada** mı olsun (fotoğraf makinesi tutuşu)? Kart sağ yarıyı aldığına göre
  ikisi çakışır — deklanşör kart açılmadan önce var, sonra yok; yine de yer
  seçimi tarif edilmeli.
- İpucu metni nerede duracak?

---

## S3 — Kartın genişliği "yarım" değil, %58

Karar *"sağ yarıda"* diyor. Tam yarım denedik ve **sığmadı**: kart iki sütunlu
alan çifti (Ürün/Marka, Market/Tarih) ve Kaydet+Vazgeç ikilisi taşıyor;
360dp'lik bir cihazda tam yarım, kartın iç genişliğini 148dp'ye düşürüyor ve
*"Ürün adı"* alanı ile *"Kaydet"* aynı satıra sığmıyor.

%58 seçtik. Vizöre kalan %42, 3:2 rehberi çizmeye fazlasıyla yetiyor.

**Soru:** %58 kabul mü, yoksa kartın iç düzeni yatayda değişsin de yarım mı
kalsın (ör. alan çiftleri yatayda **tek sütuna** insin)?
