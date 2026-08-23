> **Ek — `KODDAN-SORULAR-17-SATIR.md` ile aynı turda okunmalı.** Satır yeniden
> çiziliyorsa bu boşluk da o çizimin içinde kapanmalı; ayrı bir yüzey olarak
> sonradan eklenirse satır bütçesi ikinci kez bozulur.

# 34 — Miktar, eklendikten sonra düzenlenemiyor

**23 Ağustos 2026.** Kullanıcının cümlesi:

> *"Ben kategoriden Domates ekliyorum ya da yazarak ekliyorum, **1 kg
> ekleniyor**. Ama sonra bunu **4–5 yapmak istediğimde** ya tekrardan yazmam
> gerekiyor ya da katalogdan sürekli ekle-ekle yapmam lazım. Bu adet, gram
> vs. onları da ürünü **ekledikten sonra** ve **eklerken** düzenleyebiliyor
> olmamız lazım."*

Kontrol ettik: **miktar düzenleme hiçbir yerde yok.** Ne tasarım dosyalarında,
ne yol haritasında, ne kodda. Bugün mümkün olan tek şey ürünü tekrar eklemek —
o da miktarı **birer birer** artırıyor.

---

## Bugün ne oluyor

| Yol | Miktar nereden geliyor | Sonradan değiştirilebilir mi |
|---|---|---|
| Kök alandan yazma | `parseQuantity`: *"2 kg elma"* çalışıyor | ❌ |
| Keşif sheet'i — ızgara | katalogun `defaultUnit`i, **adet 1** | ❌ (kutucuk ikinci kez tıklanamıyor) |
| Keşif sheet'i — nadir çip | aynı | ❌ (bu turda işaretlendi, artık o da tıklanamıyor) |
| Öneri çipi | 1 | ❌ |
| Pano | satırdan ayrıştırılıyor | ❌ |

Yani **yazma yolu** miktarı destekliyor, **keşif yolu** hiç desteklemiyor —
ve karar 64 ikisini eşit iki yol ilan etmişti.

⚠ Bu turda kapattığımız bir kusur, boşluğu **daha da görünür** yaptı:
işaretli kutucuk ve nadir çip artık pasif (karar 12'nin işareti üçüncü yüzeye
de uygulandı). Yani *"tekrar tekrar dokunarak artırma"* yolu sheet'te
**tamamen kapandı**. Doğru bir düzeltmeydi ama geriye hiçbir yol bırakmadı.

---

## S1 — Eklerken miktar nasıl belirlenecek?

Kök alanda `parseQuantity` var ve çalışıyor. Keşif sheet'inde kutucuğa
dokunmak **1 adet** ekliyor ve başka seçenek yok.

- **(a)** Kutucuğa **uzun dokunuş** miktar seçtirsin (küçük bir adım seçici).
- **(b)** Kutucuk eklesin; miktar **sonradan** düzenlensin (S2'ye bırak).
- **(c)** Sheet'te kutucuğun içinde `−  1  +` çifti belirsin (ama karar 66
  hücrenin alt satırını **birime** ayırmıştı ve grid ritmi değişmez diyor).

### Dikkat: karar 91 ile çakışma

*"Toplu yolda mükerrer satır adet artırmaz, atlanır."* Tek tek eklemede ise
ikinci dokunuş **adedi artırıyor** — ama sheet'te o dokunuş artık mümkün değil.
Üç yol (tek tek / toplu / sheet) bugün üç farklı davranışta.

---

## S2 — Eklendikten sonra miktar nerede düzenlenecek?

Satırda bir **adet rozeti** zaten var (*"1 kg"*, *"2x"*) ve karar 80 onu feda
sırasının **sonuncusu** yaptı — gerekçesi *"yanlış adedin bedeli parayla
ödenir."* Yani tasarım miktarı en değerli öğe sayıyor ama düzenlenemiyor.

- **(a)** Rozete dokunmak miktar düzenleyicisini açsın. *(Ama rozet feda
  sırasında düşebiliyor — düştüğünde düzenleme yolu da kaybolur.)*
- **(b)** Ürün Detayı sheet'ine bir satır: kuyruğun başında *"Miktar"*.
  *(Sheet zaten uzun dokunuşla açılıyor ve karar 38 kuyruğun sırasını sabitledi
  — yeni satır nereye girer?)*
- **(c)** Satırda `−  N  +` çifti. *(Satır bütçesine iki dokunma hedefi daha
  ekler; `docs/33`'te sorduğumuz sıkışıklığı büyütür.)*
- **(d)** Uzun dokunuş → hızlı bir miktar sheet'i (rakam tuşları + birim).

---

## S3 — Birim de değişebilmeli mi?

Kullanıcı *"adet, gram vs."* diyor. Bugün birim **katalogtan** geliyor
(`defaultUnit`) ve değiştirilemiyor. Domates katalogda `kg`; kullanıcı *"3
adet domates"* demek isterse yolu yok.

Birim değişimi fiyat tarafını da ilgilendiriyor: karar 96 çarpanı
*"yalnız adet-birimli satırda"* diyor, tartılıda `⌈miktar ÷ ambalaj⌉`. Yani
birim tahmin hesabının **girdisi** — kullanıcı elle değiştirebiliyorsa hesap
da onu izlemeli.

---

## Neden bu turda soruluyor

`docs/33`'te satırın **yeniden çizilmesini** sorduk. Miktar düzenleme bir
kontrol istiyor ve o kontrol satırın içinde ya da satırdan açılan bir yüzeyde
duracak. İkisini ayrı turlarda çözmek, satır bütçesini **ikinci kez** bozmak
demek — bu yüzden aynı çizimin parçası olmalı.
