# 30 — Tasarıma: "markete göre tahmin" · analiz ve öneri

**22 Ağustos 2026.** Kullanıcının talebi birebir:

> *"Ben mesela BİM'e markete gidecem, listemi yaptım — o zaman **BİM'deki
> fiyatlara göre hesaplama** olsa. Ama bazı ürünleri de mesela **A101'den
> alacam**, onu da **düzenleyebilmeliyim**. Ama bunu her seferinde listeye
> eklerken bir de **hangi marketten alacağımı seçme** vs ekleme.*
> *Burası biraz **büyük bir refactor** ve tasarımında **geniş kapsamlı UX** ile
> birlikte değişmesi gerekiyor."*

Üç şart: **(1)** hesap gidilecek markete göre, **(2)** satır bazında istisna,
**(3)** ekleme anında market sorulmasın.

Sekiz kollu bir analiz koştuk: veri modeli, tasarım sözleşmesi, yüzey
envanteri, gerçek veri üzerinde kapsam ölçümü, dört UX yaklaşımı ve üç ayrı
mercekten (kullanıcı / tasarım / veri) çürütme turu.

**Sonuç, talebi doğrudan uygulamak yönünde çıkmadı** — ve sebebi ölçüm.
Aşağıda önce bulunanı, sonra öneriyi, en sonda soruları yazıyoruz.

---

## Bulgu 1 — Tahmin **zaten** markete bağlı, ama kazara

Bugünkü sorgu her ürün için **en son gözlemi** alıyor:

```sql
ORDER BY observedAt DESC LIMIT 1
```

Kullanıcının ekranındaki `~1.505 TL`'nin **%100'ü BİM fiyatı.** Sebep: A101
bloğu 13:48–13:50'de, BİM bloğu 13:55–14:00'te taranmış. Beş dakika.

Yani tahmin "market-bağımsız" değil — **"en son hangi zincir tarandıysa o"**.
Aynı liste, A101 sonra taransaydı sessizce **325,50 TL** gösterirdi ve ekran
sebebini söylemezdi.

**Kullanıcının sezdiği "tahminde bozulma" büyük ihtimalle bu.** Ve market
seçimi eklemeden de var olan bir kusur.

---

## Bulgu 2 — Tahmin ambalajı hiç okumuyor

Daha ağırı: sorgu `packSize`, `packUnit`, `priceUnit` ve `trip_line.unit`'in
**hiçbirini** okumuyor. Sadece `adet × etiket fiyatı`.

Satır seviyesinde bu koruma **var ve çalışıyor** — `PriceHintMapping` iki
gözlem arasında ambalaj değişimini yakalayıp trendi bastırıyor ve KDoc'u
*"trend dalı önce seçilseydi yeşil bir aşağı ok çizerdi ve kullanıcıya gerçeğin
tersini söylerdi"* diyor.

**Üstündeki tahmin satırının bu korumaların hiçbiri yok.** Ürün satırı
*"ambalaj değişti, karşılaştırma yapmıyorum"* derken, bir üstteki tahmin aynı
iki sayıyı sessizce çarpıyor.

`priceUnit` kolonu ayrıca **hiç yazılmıyor** — tanımı ve bir test yorumu
dışında sıfır referansı var. Kendi KDoc'u ise şunu diyor: *"Bu kolon olmadan
`unitPriceMinor` tek başına anlamsızdı."*

---

## Bulgu 3 — Şema yarısı neredeyse hazır

`Trip.storeId` **v1'den beri** duruyor. Yazacak DAO'su da var
(`setStoreIfAbsent`) ve **çağıranı yok**. KDoc'u kelimesi kelimesine şunu
yazıyor:

> *"ÇAĞIRANI YOK (E11'den beri)… cevap «o gezinin marketi» olursa **yazan taraf
> tam burası olacak**."*

Yani "gezinin marketi" fikri kodda bekliyor. Gereken tek yeni kolon
`trip_line`'ın istisna alanı — nullable, tam otomatik göç, Faz 7'de tek DTO
alanı (sunucu şemasında `trip.store_id` **zaten var**).

⚠ Bir tuzak: `trip_line`'ın id'si Faz 7'de `(tripId, productId)`'den
**türetiliyor**. Market alanı türetmeye girmemeli — girerse satırı başka
markete atamak id'sini değiştirir ve eşin telefonunda ikinci satır doğar.

---

## Bulgu 4 — Kapsam ölçümü özelliği desteklemiyor

Gerçek cihaz verisi:

| Ölçü | Değer |
|---|---|
| Canlı fiyat gözlemi | **10** (hepsi tek 12 dakikalık oturumda) |
| Hanedeki ürün | 42 · **fiyatı olan: 5 (%12)** |
| Ortalama zincir/ürün (listeye girenler) | **0,64** |
| İki zincirde birden gözlemi olan ürün | **2** (Süt, Yoğurt) |
| 12 kapalı gezinin gezi sırasında yazılmış gözlemi | **0** |
| `trip.storeId` dolu olan gezi | **0 / 13** |

Hedef markete göre tahmin ne olurdu:

| Gidilecek market | Fiyatı bilinen satır | Katı tahmin |
|---|---|---|
| **BİM** | 4/5 | **1.504,50 TL** — bugünkünün **aynısı**, fark 0 kuruş |
| **A101** | 2/5 | **325,50 TL** — %78'i kayboluyor, üstelik 3 fiyatlı ürün eşiğinin altına düşüp **satır tamamen kayboluyor** |
| Diğer yedi zincir | 0/5 | **0,00 TL** — satır yok |

**Yani özellik bugün ya hiçbir şeyi değiştiriyor ya rakamı yok ediyor.**
"Hedef markette fiyat yok" bir kenar durum değil — medyan zincirde **%100**.

---

## Bulgu 5 — Reyonda bu özelliğin **yüzeyi yok**

Alışveriş modunda tasarım, fiyatla ilgili her şeyi bilerek gizliyor: fiyat
çipi, ikinci satır, ucuz çipi ve tahmin satırının kendisi. Gerekçe yazılı:
*"reyonda 10–11 değil 7–8 satır görünmeli, ve gerekli olan tek bilgi ürün
adı."*

Yani **BİM'de rafın önünde duran kullanıcı için bu özelliğin görünür hiçbir
çıktısı olmaz.** Özellik, adı ne olursa olsun, **evde plan yaparken** çalışan
bir şey. Reyonda fiyatı geri getirmek karar 56'nın satır yüksekliğini ve 7–8
satır hedefini birlikte iptal eder.

---

## Bulgu 6 — Şart 2'nin karşılığı **zaten var**

Kullanıcının *"bazı ürünleri A101'den alacağım"* cümlesinin veri karşılığı,
karar 41'in **"başka markette ucuz" çipi**: satır satır *"A101'de 36,00"*.
Geçen hafta bağlandı ve cihazda çalışıyor.

Çipin üç kapısı var (taze ≤14 gün · ambalajı kanıtlı aynı · hem %10 hem 5 TL)
ve bu üç kapı, aynı iddianın **sepet ekseninde hiçbir ispat istemeden** kurulmuş
olmasıyla çelişirdi: bir çipin *"A101 ucuz"* demesi üç ispat istiyorken,
tahminin *"A101'e göre şu kadar"* demesi sıfır ispat isteyecekti.

---

## Öneri: talebi **iki parçaya bölmek**

### Şimdi yapılacak — "tahmin hangi markete ait, GÖRÜNSÜN"

Tahmin satırı bugün zaten bir markete ait; söylemiyor. Önerimiz satırın kendi
kaynağını yazması:

```
Tahmini sepet                      ~1.505 TL
BİM fiyatlarına göre · 5/6 ürün
```

Bu, **sıfır dokunuş** ve **sıfır şema** ile şart 1'in dürüst yarısını veriyor:
kullanıcı rakamın nereden geldiğini görüyor. Bugünkü sessiz kırılganlığı
(bulgu 1) de kapatıyor.

Ve alternatif de aynı satıra sığar — seçim, mod veya yapışkanlık olmadan:

```
BİM'e göre ~1.505 · A101'de ~326
```

### Şimdi yapılmayacak — gezi marketi + satır istisnası

Şema ucuz ama **kullanacak verisi yok** (bulgu 4), reyonda yüzeyi yok
(bulgu 5) ve istisna için zaten daha dürüst bir araç var (bulgu 6). Bu karar,
hanenin **iki farklı zincirde gerçekten alışveriş yaptığı 3–4 gezi**
biriktikten sonra aynı sorgu tekrar koşularak verilmeli. Eşik ölçülebilir:
**listeye giren ürünlerin ortalama zincir sayısı ~1,5'i geçtiğinde.** Bugün
**0,64**.

### Ondan önce yapılacak — `priceUnit` / `packSize` normalizasyonu

Bulgu 2 hangi yol seçilirse seçilsin ön koşul: markete göre hesap, **hesabın
kendisi doğru olmadan** anlamsız. Yanlış markete göre yanlış bir çarpım,
doğru markete göre yanlış bir çarpımdan daha iyi değil.

---

## Sorular

### S1 — Tahmin satırı kaynağını yazsın mı, ve nasıl?

Üç biçim:

- **(a)** Alt satıra ekle: `BİM fiyatlarına göre · 5/6 ürün` — ama alt satır
  şu an `"N üründen M tanesini biliyorum"` taşıyor ve ikisi birden sığmıyor
  (`docs/28`'de ölçülen bütçe sorunu).
- **(b)** İki sayı yan yana: `BİM'e göre ~1.505 · A101'de ~326`. Seçim yok.
- **(c)** Hiçbiri — tahmin market söylemesin, bugünkü gibi kalsın.

### S2 — Fiyatlar birden fazla zincirden geliyorsa satır ne der?

Bugünkü tahmin **karışık** olabiliyor: bir ürün BİM'den, öteki A101'den.
"BİM fiyatlarına göre" o zaman yalan olur. `"karışık fiyatlara göre"` mi,
`"son gördüğün fiyatlara göre"` mi, yoksa zincir dağılımı mı yazılmalı?

### S3 — Ambalaj uyuşmazlığında tahmin ne yapsın?

Satır seviyesinde kural net: *"ambalaj değiştiyse yüzde iddia edilmez"*.
Tahminde karşılığı ne?

- Ürün toplamdan **düşsün** (payda aynı kalır: `6 üründen 4'ünü biliyorum`)
- Yine de **çarpılsın** ama satır bunu söylesin
- `priceUnit` normalize edilene kadar tahmin hiç çizilmesin

Somut vaka: kullanıcının listesinde `3 kg Yoğurt` var ve son gözlem
`192,00 TL` — bir **3 kg'lık kova**. Bugün 3 × 192,00 = **576,00 TL**
hesaplanıyor. Doğrusu 192,00.

### S4 — "Markete göre" özelliği ertelenirse, kullanıcıya ne denir?

Talep ölçülebilir bir eşiğe bağlanacak. Kullanıcı bunu bir **ret** olarak
değil bir **sıra** olarak görmeli. Uygulamada bunun bir yüzeyi olmalı mı
(*"iki markette de fiyatını bildiğim ürün sayısı: 2"*), yoksa bu tamamen
yol haritasının işi mi?

### S5 — Erteleme kabul edilmezse: dört yaklaşımdan hangisi?

Analiz dört yaklaşımı puanladı (kullanıcı şartları / tasarım çatışması /
maliyet / risk):

| Yaklaşım | Toplam | Ana kusur |
|---|---|---|
| **Hesap merceği** (tahmin satırı seçici olur, liste değişmez) | **15/20** | reyonda yüzeyi yok |
| Çıkarımlı (ilk etiketten anlaşılır) | 14/20 | şart 2'yi hiç karşılamıyor |
| Gezi marketi + satır rozeti | 11/20 | yanlış seçim tahmini yok ediyor; iki yapışkanlık ayrışıyor |
| Bölümlü liste (markete göre gruplama) | 8/20 | reyon sırasını, sabitler bölümünü ve silme jestini birden bozuyor |

Merceğin dört alt sorusu var: yapışkan mı geziyle mi ölüyor · satır sabitlemesi
geziyle mi ölüyor · mercek marketinde fiyatı olmayan ürün toplamdan düşünce
payda ne olur · mercek `pricedCount`'u 3'ün altına düşürüp **kendi kontrolünü**
yok ederse ne yapılır.

---

## Ekler

Ölçüm betikleri ve ham çıktılar oturum klasöründe. Kod tarafında dokunulacak
yerler: `Daos.kt` (tahmin sorgusu 546–568, rakip gözlem 362–408, geçmiş gezi
723–744) · `CheaperElsewhere.kt` · `PriceHintMapping.kt` ·
`BasketAndSummary.kt` · `PriceObservation.kt`'nin `priceUnit`/`packSize`
KDoc'ları.
