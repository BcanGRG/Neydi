# 40 — Marka okuma kalitesi ölçüldü: sorun sandığımız yerde değil

**24 Ağustos 2026.** Yol haritasının 2. satırı *"marka bazen çöp geliyor
(`CE UZ`, `BAlkon`, `BILI BIL`), 99 fikstür üzerinde ölçülebilir"* diyordu.
Ölçüm yapıldı ve **satırın kendisi bayat çıktı**: o üç örnekten ikisi artık
üretilmiyor, üçüncüsü zaten çöp değil.

Ölçüm cihaz istemedi — 99 gerçek etiket fikstürü repoda, cihazda ML Kit'ten
çıkmış ham dökümler hâlinde.

---

## Bugünkü hâl

| Zincir | Etiket | Ad okunuyor | Marka | Not |
|---|---|---|---|---|
| **BİM** | 27 | 27 | **24** (22 gerçek, 2 çöp) | %92 |
| **A101** | 19 | 19 | 0 | **karar 39** — marka tahmin edilmiyor, ada katılıyor |
| **Migros** | 19 | **0** | 0 | ölçülmüş ret (`MigrosGrammar.readName`) |
| **Metro** | 34 | 0 | 0 | grameri yazılmadı, bilinçli erteleme |

**En önemli bulgu bir sayı değil bir çerçeve hatası:** marka okuması tek bir
zincirin özelliği. A101 ve Migros'un sıfırları birer **karar**, eksiklik
değil — ikisi de kendi dosyasında gerekçesiyle yazılı ve testi var. Yani
*"marka kalitesi %X"* diye tek bir yüzde konuşmak yanlış soru; doğru soru
*"BİM'de %92, ötekilerde tanım gereği yok"*.

### `BİLİ BİLİ` çöp değil

Yol haritasının verdiği üç örnekten biri (`BILI BIL`) **gerçek bir marka** —
BİM'in yumurta markası, ve bugün doğru okunuyor. Diğer ikisi (`CE UZ`,
`BAlkon`) corpusun hiçbir yerinde artık üretilmiyor.

### Üretilen 22 gerçek marka

`ARBELLA` · `BOL BOL` · `BİLİ BİLİ` · `CENTRO` · `CHEESE TİME` · `DAPHNE` ×2 ·
`DOST` · `EFSANE` ×2 · `ETI` · `PARTY` · `PATOS` · `PEPSİ ZERO SUGAR` ·
`QUEEN` · `SABAN` ×2 · `SEK` · `SOFRA` · `SOLE` · `TORKU BANADA` · `ŞAFAK`

---

## Kalan iki çöp — ikisi de aynı yapısal sebepten

| Fikstür | Marka | Ad | Kaynak |
|---|---|---|---|
| `20260817_183949` | `Yağlg)` | `AYCA PEYNİR CEŞİTLERİ` ✅ | etiketin **besin değerleri tablosu** |
| `20260817_183947` | `Zme` | `aği Taze Peynir tçİM. SÜZME…` | ürünün **kendi ambalajı** |

İkisi de `looksLikeBrand`'in üç şartını (rakamsız · ≥3 harf · ≥1 sesli)
geçiyor. Ham dökümler sebebi açık ediyor:

```
183949: AYCA | PEYNİR | CEŞİTLERİ | 500 G | P728 |
        Enerji ve Besin Öğeleri | Enerji (kJ/kcal) | Yağlg) |
        -Doymuş Yağ (9) | Karbonhidrat (g) | …
183947: Zme | aği Taze Peynir | tçİM. | SÜZME PEYNİR | TAM YAĞLI | 900 G | …
```

Yani sorun **ad bloğunun kendisi değil, bloğun üstüne düşen şey**: büyük
etiketlerde besin tablosu, ve kadraja giren ürün ambalajı. Bu, Migros'un ad
okumayı bilerek reddetme gerekçesinin aynısı — *"ürünün kendi ambalajı da
kadrajda ve etiketin ad satırından büyük basılıyor"*.

---

## Bir ayıraç bulundu, ama **kural yapılmadı**

Corpus temiz bir ayıraç veriyor: **22 gerçek markanın 22'si tamamen büyük
harf, iki çöpün ikisi değil.** Sıfır yanlış pozitif.

`looksLikeBrand`'e dördüncü şart olarak eklendi ve **geri alındı.** Sebebi
ölçüldü:

```
öncesi : brand=Yağlg)   name=AYCA PEYNİR CEŞİTLERİ
sonrası: brand=null     name=Yağlg) AYCA PEYNİR CEŞİTLERİ
```

Kural markayı elemiyor, **ada taşıyor** — çünkü marka olamayan ilk satır ada
katılıyor (`U` / `TURŞU` vakası bunu bilerek yapıyor). Defekti kaldırmayan
bir düzeltme, düzeltme değil; ve *"marka kalitesi düzeldi"* diye göndermek
onu görünmez yapardı.

⚠ **Blok üyeliğinde büyük harf kuralı da GÜVENLİ DEĞİL:** `ETI`nin adı
`Cici BEBE` diye ayrı bir karışık harfli satır taşıyor ve o satır meşru.

Ölçüm testi (`BrandQualityTest`) ayıracı **kaydediyor**, uygulamıyor.

---

## Sıradaki adım için ne biliyoruz

Doğru düzeltme marka yuvasında değil **blok sınırında**: `Yağlg)` ad
bloğunun ilk satırı olmamalıydı, çünkü besin tablosuna ait. Gramaj satırı
(`500 G`) bloğu bitiriyor ama bu etikette besin paneli adın **üstünde**
duruyor, yani üst sınır da gerekiyor.

İki yön var ve ikisi de ölçülmeden seçilmemeli:

1. **Besin tablosu kökleri** — `ENERJI`, `KARBONHIDRAT`, `PROTEIN`,
   `KALSIYUM`, `BRD` gibi. ⚠ `YAG` kök olarak **kullanılamaz**: yağ bir ürün.
   Yani `Yağlg)`'yi kök sözlüğü yakalayamaz.
2. **Geometrik üst sınır** — ad bloğu fiyatın hizasından yukarı doğru sınırlı
   olmalı. Ölçüm gerektirir: 27 BİM etiketinde ad bloğunun fiyata göre dikey
   konumu.

---

## Ölçüm nasıl yapıldı

Geçici bir döküm aracı 99 fikstürü gerçek zinciriyle `readTagFields`ten
geçirdi ve marka/ad/ham satırları diske yazdı. Araç ölçüm bitince silindi;
kalıcı olan `BrandQualityTest` — corpusun sayılarını kilitliyor.

⚠ **Aracın kendi hatası ölçümün hatasıdır.** İlk döküm `skipped != null` olan
her fikstürü atlıyordu, oysa `PRICE_CONTRADICTS_UNIT_PRICE` yalnızca fiyatı
ve gramajı düşürüyor — ad okunmaya devam ediyor. Yedi fikstürün markası
sayılmadan kalmıştı ve ilk sayı 22 çıkmıştı; düzeltilince 24 oldu.
