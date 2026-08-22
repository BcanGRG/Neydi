# 27 — Tasarıma sorular (on ikinci tur): trend neyi neyle karşılaştırıyor

**22 Ağustos 2026.** Kullanıcı aynı gün BİM ve A101'de etiket çekti, akşam
listeye baktı. Ekranda **iki trend çipi** vardı ve **ikisi de yanlıştı.**

Bu tur uydurma senaryolarla değil, o dört satırla yazıldı.

---

## Ekrandaki veri

| Ürün | Gözlemler | Satırın yazdığı |
|---|---|---|
| **Süt** | 16:48 **A101** 36,00 (1 lt) · 16:55 **BİM** 62,50 (1 lt) | `önce 36,00` **↑ %74** |
| **Yoğurt** | 16:58 **BİM** 102,00 (**1,5 kg**) · 16:59 **BİM** 192,00 (**ambalaj okunamadı**) | `önce 102,00` **↑ %88** |

**Süt'te hiçbir fiyat artmadı** — aynı gün, yedi dakika arayla, iki farklı
zincir. Bir ekran derindeki "Nerede ucuz" aynı iki sayıyı doğru okuyordu:
*A101 36,00 · BİM 62,50.* Uygulama aynı veri hakkında birbiriyle çelişen iki
cümle kuruyordu ve yanlış olan, önce görülendi.

**Yoğurt'ta da zam yok** — kullanıcı 1,5 / 2 / 3 kg'lık üç boy çekti. 192,00
büyük kova; 102,00 küçüğü. Ambalaj dalı devreye girmedi çünkü bir yanın
ambalajı **okunamamıştı**.

---

## Bu turda kendiliğimizden yaptığımız (ve neden)

**F5.5'in yerel yarısı açıldı** — karar 41'in çipi artık çiziliyor:

> *"Aynı satırda hem trend hem çip doğruysa **çip kazanıyor, trend
> bastırılıyor**."*

Süt satırı bugün `BİM · bugün` + **`A101'de 36,00`** yazıyor. Yanlış yüzde
gitti. Bunu sormadan yaptık çünkü karar 41 vakayı birebir tarif ediyordu;
eksik olan tek şey çipi dolduran taraftı.

Çipin ambalaj şartını **tasarımdakinden katı** tuttuk: iki ambalajın aynı
olduğu *kanıtlı* olmalı, `null` yetmiyor. Gevşek bıraksaydık Yoğurt satırına
**`A101'de 49,00`** yazacaktı — 250 ml'lik kâseyle 3 kg'lık kovayı
karşılaştıran bir cümle. Trendin yalanını çipin yalanıyla değiştirmiş
olurduk. Gerekçe tasarımın kendi ilkesi (karar 58): karşılaştırılamayan bir
karşılaştırmadansa **sessizlik**.

---

## S1 — Ambalajlardan biri okunamadıysa trend yazılmalı mı

Bugünkü kural (`PriceHintMapping.comparablePack`) şöyle diyor ve doğru diyor:

> *"`null` 'aynı değil' demek değil, 'bilmiyorum' demek. Bilinmeyenden
> **ambalaj değişimi** çıkarmak uydurma olurdu."*

Ama madalyonun öteki yüzü açıkta: bilinmeyenden **trend** çıkarmak da aynı
uydurma. `null` *"ambalaj değişti"* iddiasını engelliyor, *"%88 zam"*
iddiasını engellemiyor — oysa ikisi de **aynı önermeye** dayanıyor: "bu iki
şey aynı boy."

Üç durum var, ikisi yazılı, biri değil:

| Durum | Bugün | Doğru mu |
|---|---|---|
| İkisi de biliniyor, aynı | Trend | ✅ |
| İkisi de biliniyor, farklı | `PackChanged` | ✅ karar 67 |
| İkisi de **bilinmiyor** | Trend | ✅ tarihsel taban; başka bilgi yok |
| **Biri biliniyor, öteki bilinmiyor** | Trend | ❌ Yoğurt satırı |

Dördüncü satır üçüncüden farklı: orada **hiç** bilgi yok, burada bilginin
**yarısı** var ve atılıyor.

**Soru:** Tam olarak bir yanın ambalajı biliniyorsa trend düşsün mü?

| | Bugün | Öneri |
|---|---|---|
| İkisi de bilinmiyor | Trend | **değişmez** |
| Biri biliniyor | Trend | `Single` — "son ödediğin 192,00 · BİM · bugün" |
| Bedeli | — | ambalaj okunma oranı arttıkça bu durum **azalır** |

Karşı argüman: ambalaj her etikette okunmuyor, kural trendin kapsamını
daraltır. Ama daralttığı yer tam olarak **iddianın dayanaksız olduğu** yer.

---

## S2 — Aynı gün, iki zincir: bu bir trend mi

Süt vakası çiple kapandı, ama **tesadüfen**: çip "liste başına en fazla 3"
sınırının içinde kaldığı için. Dördüncü sıradaki bir ürün aynı durumda olsa
çip düşer, **yanlış yüzde geri gelir.**

Sebep daha derinde: `prevPriceMinor` **"zamanda bir önceki gözlem"**, başka
hiçbir şart yok. Farklı zincir olabilir. Karar 41'in kendi gerekçesi ise ikisini
ayırıyor:

> *"trend bilgi, çip eylem — reyonda 'burada 12 TL fazla veriyorsun',
> **'geçen ay 38,50'ydi'**den acil."*

*"Geçen ay 38,50'ydi"* aynı yerin zaman içindeki cümlesi. Farklı zincirlerden
kurulmuş bir yüzde ise iki şeyi karıştırıyor: gerçek zam ve mağaza farkı.

**Soru:** Trend **aynı zincir** şartı istesin mi?

| | Bugün | Öneri |
|---|---|---|
| `prev` seçimi | zamanda bir önceki | son gözlemin **zincirinden** bir önceki |
| Süt | çip sayesinde doğru | **kendiliğinden** doğru (BİM'de tek gözlem → `Single`) |
| Bedeli | — | zincir değiştiren kullanıcıda trend seyrekleşir |

Bu **S1'den bağımsız**: biri ambalaj eksenini, öteki market eksenini kapatıyor.
Yoğurt satırı ikisinden yalnızca S1 ile düzeliyor (iki gözlem de BİM'de).

---

## S3 — Çip hangi karşı gözlemi söylüyor: en ucuzu mu, en günceli mi

Bugün **14 gün penceresindeki en ucuz** gözlem. A101'de üç taze gözlem varsa
(36,00 · 40,00 · 44,00) çip **36,00** diyor — görülmüş en iyi fiyat, güncel
fiyat değil.

Karar 41 tazeliği 14 günle bağlamış ve *"24dp'lik çipte tarih yazacak yer
yok"* demiş. Yani çip tarihsiz bir iddia kuruyor ve reyonda **bugünkü fiyat**
gibi okunuyor.

**Soru:** En ucuz mu kalsın, yoksa rakip zincirdeki **en son** gözlem mi?

- **En ucuz** — kullanıcıyı en iyi ihtimale yönlendirir; yanılırsa yukarı
  yanılır (gidip daha pahalı bulur).
- **En son** — o zincirin bilinen güncel durumu; daha dürüst ama tasarrufu
  küçük gösterebilir.

---

## S4 — Çip reyonda neden yok

Karar 41'in gerekçesindeki cümle **reyonda** geçiyor: *"burada 12 TL fazla
veriyorsun"*. Ama alışveriş modunun satırı **tek satırlık** (`rowShopping`) ve
kod çipi orada gizliyor (`ListItemRow.kt:122`).

Yani çip, varlık gerekçesinin geçtiği yerde görünmüyor.

**Soru:** Hangisi kazanacak — reyonun sıkı satırı mı, çipin reyon amacı mı?
Alışveriş modu satırı çip için iki satıra çıkabilir mi, yoksa çip planlama
moduna mı ait?

---

## Değişen tek yer

S1'in cevabı `PriceHintMapping.comparablePack`, S2'ninki `TripLineDao`
sorgusundaki `prevPriceMinor` alt sorgusu, S3'ünki aynı sorgudaki
`ORDER BY po.unitPriceMinor ASC`, S4'ünki `ListItemRow`. Dördü de tek satır.
