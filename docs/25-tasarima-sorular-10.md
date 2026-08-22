# 25 — Tasarıma sorular (onuncu tur): onay kartı + klavye

**22 Ağustos 2026.** Bu tur bir denetimden değil, **cihazda yapılan bir
provadan** çıktı: uygulamanın kendi kamerasıyla uçtan uca sahte bir çekim
yapıldı (deklanşör → kart → fiyat → ürün → market → kaydet → veritabanı) ve
akış boyunca ölçüm alındı.

> Sorular ölçülmüş sayılarla geliyor. Her birinin altında cihazdan alınmış
> piksel değerleri var.

---

## Kapanan: koddaki sapma (soru değil, bildirim)

**Pasif buton pasif görünmüyordu.** Tasarım sistemi açık:

> *"Devre dışı: **%38 opaklık**, dolgu **surfaceVariant**'a düşer."*

`NeydiButton` `enabled`'ı yalnızca dokunmaya veriyordu; pasif Kaydet aktif
olanla **birebir aynı** çiziliyor ve basınca hiçbir şey olmuyordu. Düzeltildi.

Bu önemliydi çünkü gezinme sözleşmesinin *"Kaydet pasif; **ilk rakamda
etkinleşir**"* cümlesinin gözlemlenecek bir karşılığı yoktu.

---

## S1 — Dikey düzende klavye kartı örtüyor. Ne feda edilmeli?

**Tasarım bunu yalnızca yatay için yazmış:**

> *"Onay kartı sağ yarıda dikey panel; sayısal klavye sol yarıyı (vizörü)
> örter, **kartı asla örtmez**."*

Dikeyde böyle bir cümle yok. Ölçüm:

| | piksel |
|---|---|
| Kart içeriği (kırpım + fiyat + 3 seçici + tarih + 2 buton) | **1669** |
| Sayısal klavyenin üstünde kalan alan | **1198** |
| **Açık** | **471** |

Yani dikeyde klavye açıkken kart **sığmıyor** ve Kaydet ile Vazgeç altta
kalıyordu. Kullanıcı kartı tam sanıyor, butonları göremiyordu.

**Şimdilik yaptığımız:** klavye açıkken **kırpım şeridi toplanıyor** (336 px +
12dp boşluk = 369 px). Market seçili kartta içerik 1148 px'e iniyor ve hiçbir
şey kaydırılmadan hepsi görünüyor.

Gerekçemiz karar 62'nin kendi cümlesi: kırpımın işi *"ne çektim"* doğrulaması
ve o iş kart **açılırken** yapılıyor; kullanıcı fiyatı yazmaya başladığında
doğrulama bitmiş oluyor. Klavye kapanınca şerit geri geliyor.

**Denenip bırakılanlar:**
- *Kartı sonuna kaydırmak* — Kaydet geliyor ama **fiyat alanı** yukarıdan
  çıkıyor; kullanıcının tam o anda yazdığı alan kayboluyor.
- *Kartı küçültmek* — hangi satırın gideceği bir tasarım kararı, bizim değil.

**Soru:** dikeyde klavye açıkken ne feda edilmeli? Kırpım mı, Tarih satırı mı,
yoksa kart mı kaydırılmalı?

---

## S2 — Vazgeç, klavye açıkken görünmüyor. Görünmeli mi?

Kırpım toplandıktan sonra bile Vazgeç fold'un **22 px** altında kalıyor.

Geri tuşu zaten iptal ediyor (gezinme sözleşmesi: *"Klavye açıksa önce onu
kapatır. Kapalıysa kartı iptal eder"*), yani çıkış yolu var.

**Soru:** Vazgeç'in klavye açıkken de görünmesi şart mı? Şartsa Kaydet ile
Vazgeç yan yana mı gelmeli (dikey yerine yatay çift)?

---

## S3 — "Kuruş okunamadı — kontrol et" ne zaman susmalı?

**Cihazda görüldü:** kullanıcı fiyat alanına `39,50` yazıyor ve kart hâlâ
*"Kuruş okunamadı — kontrol et"* diyor.

Uyarının gerekçesi kodda yazılı: *"kullanıcıdan iki hane istemek, yanlış iki
haneyi sessizce kaydetmekten iyi"*. Yani iş kullanıcıyı fiyata **baktırmak**.

**Şimdilik yaptığımız:** kullanıcı alanı düzenlediği anda uyarı susuyor.
`39` yazıp kuruş yazmamayı da bir **cevap** sayıyoruz — boş bırakılmış değil,
39,00 seçilmiş.

**Soru:** doğru mu? Yoksa uyarı yazılan metinde kuruş yoksa sürmeli mi?

---

## S4 — Fiyat alanı ondalığı kendi koymalı mı?

`parseMinorInput` sözleşmesi: `"106"` → **106,00**. Yani virgülsüz yazılan sayı
tam lira.

Cihazda sayısal klavye hem `.` hem `,` tuşu gösteriyor. Kullanıcı **39,50**
demek isteyip `3950` yazarsa fiyat **3.950,00** kaydediliyor — yüz kat hata ve
hiçbir uyarı çıkmıyor.

Bu provada bizzat yaşandı: `3950` yazdım, kart 3.950,00 kabul etti.

**Soru:** fiyat alanı para girişleri gibi **sağdan doldurmalı** mı
(`3` → 0,03 · `39` → 0,39 · `3950` → 39,50), yoksa bugünkü serbest yazım mı
doğru? Sağdan doldurma OCR'ın önceden yazdığı değeri düzeltmeyi zorlaştırır;
serbest yazım ise sessiz bir yüz kat hataya açık.

---

## S5 — Kırpım şeridinin yüksekliği hâlâ türetilmiş değil

Maket `height:92px` diyor. Cihazda denendi: tam genişlikte 92dp ≈ **4:1** bant
demek, oysa kadraj rehberi **3:2**. `ContentScale.Crop` merkezden aldığı için
etiketin üst ve alt kenarları kesiliyordu; kullanıcı *"üstten kesiyor gibi"*
diye bildirdi. Bugün **128dp** (≈3:1).

Asıl çözüm şeridin yüksekliği değil: küçük kopyayı çekerken **rehberin
bölgesinden** kırpmak. O zaman şerit ne kadar kısa olursa olsun gösterdiği şey
tam olarak kullanıcının kadraja oturttuğu şey olur.

**Soru:** rehber-bölgesinden kırpma yapıldığında şerit maketin 92px'ine geri
dönmeli mi?

---

## Provanın doğruladıkları *(soru değil, kayıt)*

Uçtan uca çalıştığı görülen yollar:

- Deklanşör → kart (~1,15 sn), kırpım kartın başında
- Market çipi → seçici (9 zincir) → yapışkan kalıyor
- Ürün seçici → **mevcut** ürüne çözülüyor, mükerrer ürün yaratmıyor
- Amber şerit sırası: fiyat → ürün → kuruş, aynı anda yalnızca biri
- Kaydet → gözlem yazılıyor, **fotoğraf siliniyor** (karar 29), kameraya dönülüyor
- Karar 49 cümlesi: *"A101, BİM ve Migros etiketlerini okuyabiliyoruz"*
