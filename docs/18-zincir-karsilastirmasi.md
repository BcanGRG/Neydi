# 18 — Üç zincir, üç ayrı etiket grameri

**17 Ağustos 2026.** Kullanıcı aynı akşam Metro'da 34, Migros'ta 19 raf etiketi
daha çekti. Fikstür seti 27'den **80'e** çıktı; hepsi ML Kit'ten geçti,
**sıfır hata**.

> Bu doküman bir ölçüm raporu. Her iddianın arkasında fikstürde bakılabilir bir
> satır var.

---

## Önce: BİM partisi yeniden döküldü, çıktı birebir aynı

Aynı 27 fotoğraf ikinci kez OCR'dan geçti ve **27/27 commit'li fikstürle
birebir aynı** çıktı (tek fark satır sonu: git CRLF'e çevirmiş). ML Kit
deterministik, yani fikstürler gerçekten sabit bir zemin.

Bu bedava bir kontroldü — ikinci partiyi almanın yan faydası.

---

## Ölçüm: E14 ayrıştırıcısı üç zincirde

| Zincir | n | fiyat döndü | kuruş | ad | gramaj | birim fiyat |
|---|---|---|---|---|---|---|
| **BİM** | 27 | 26 | 11 | 26 | 23 | 13 |
| **Metro** | 34 | **34** | 18 | 10 | **0** | **0** |
| **Migros** | 19 | **19** | 1 | 15 | **1** | 12 |

İlk bakışta Metro ve Migros'ta fiyat **%100** okunuyor gibi görünüyor. Bu tam
olarak yanlış okuma.

**"Bir sayı döndü" ile "doğru sayı döndü" aynı şey değil.** Kural *"rakamla
başlayan en büyük glifli satır"* ve bu etiketlerde her zaman bir büyük sayı var
— ama o sayı fiyat olmayabiliyor.

---

## Üç doğrulanmış yanlış fiyat

### 1. Migros · İçim Laktozsuz Süt 1L — **10 kat**

```
y=2638 h=469  x=1519  799            <- en büyük glif
y=2648 h=271  x=1928  ,50
y=3476 h=48   x=358   BÝRÝM FÝYAT: 79,50 TL/LT
```

Ayrıştırıcı **799,00 TL** diyor. Etiketin gerçeği **79,50 TL** — birim fiyat
satırı zaten söylüyor. `,50` parçası ayırıcı taşıdığı için kuruş kuralı onu
reddediyor (o kural BİM'de `89,s6` üstü çizili eski fiyatı elemek için
konmuştu), geriye `799` kalıyor.

### 2. Migros · Patates — **100 kat**

```
y=1474 h=364  x=1135  4389           <- en büyük glif
y=2076 h=28   x=646   BİRİM FİYAT: 43,95 TL/KG
```

Ayrıştırıcı **4389,00 TL**. Gerçek **43,95 TL/kg**. Migros manşeti kuruşu
üstsimge basıyor ve OCR ikisini tek sayıya yapıştırıyor.

### 3. Metro · Lay's Süper Boy 125G — **yanlış fiyatı seçiyor**

```
y=2773 h=156  x=1420  çOK AL         <- promosyon şeridi, fiyattan BÜYÜK
y=3000 h=149  x=1366  AZ ÖDE
y=1987 h=127  x=1889  56.5           <- çok-al fiyatı
y=1968 h=119  x=819   66,            <- tek ürün fiyatı
```

Ayrıştırıcı **56,00** diyor: çok-al fiyatını seçiyor *ve* kuruşunu düşürüyor.
Tek ürün fiyatı 66,30.

Buradaki yapısal fark **etikette iki fiyat olması**. BİM'de de üstü çizili eski
fiyat vardı ve virgül kuralı onu eliyordu; Metro'nun ikinci fiyatı ise geçerli
bir fiyat, sadece farklı bir koşulun fiyatı.

---

## Neden: üç ayrı gramer

| | BİM | Metro | Migros |
|---|---|---|---|
| Manşet | dev lira + üstsimge kuruş | lira + `TL` + kuruş (`39,TL 90`) | lira + üstsimge, OCR yapıştırıyor (`4389`) |
| Gramaj | **kendi satırı**, ad bloğunu bitirir | ad satırının **içinde** (`PATOS PARTİ BOY 185G`) | ad satırının **içinde** (`ÜLKER DİDO 35G`) |
| Kod | `P728` (mağaza) | 6 hane (`279805`) | 13 hane barkod |
| Birim fiyat | bazen, normal punto | yok/nadir | **`BİRİM FİYAT: 79,50 TL/KG`, temiz** |
| Ekstra | — | `KDV Dahildir` üstte, promo şeridi, çift fiyat | `MENŞE:`, `FİYAT DEĞİŞİKLİK TARİHİ` |

E14'ün üç taşıyıcı kuralı da BİM'e özel çıktı:

1. **"Gramaj satırı ad bloğunu bitirir"** — Metro/Migros'ta gramaj ayrı satır
   değil. Gramaj 0/34 ve 1/19 bu yüzden.
2. **"Ad solda, fiyat sağda"** — Migros manşeti ortada, Metro'da promo şeridi
   sol kolonu kaplıyor.
3. **"En büyük rakamla başlayan satır = lira"** — Metro'da promo şeridi
   fiyattan büyük, Migros'ta iki fiyat (satış + birim) yarışıyor.

---

## E15 için anlamı — ve bu bir engel

Onay kartı OCR'ın doldurduğu alanları gösterip kullanıcıya "kaydet" dedirtiyor.
Bugünkü ayrıştırıcıyla Migros'ta patates için **4389,00 TL** ön-dolduruluyor.

Karar 26 fiyat geçmişini market+marka çifti üzerine kuruyor; oraya giren yanlış
bir kayıt **kalıcı olarak** yanıltır. Ve bu, E14'te bulanık çekim için verilen
kararın aynısı: *yanlış fiyat, fiyat olmamasından kötü.*

Yani **E15 bugünkü ayrıştırıcıyla BİM dışında güvenle yazamaz.**

### Elde ne var

İyi haber: Migros'un **birim fiyat satırı temiz** ve 19 etiketin 12'sinde
okundu (`BİRİM FİYAT: 79,50 TL/LT`). Bu hem bağımsız bir kaynak hem de bir
**çapraz kontrol**: manşet 799,00 ile birim 79,50 çelişiyorsa manşete
güvenilmemeli. İkisinin çeliştiği yerde susmak, uydurmaktan iyi.

---

## Eksik kalan

**Yan tutulmuş kare hâlâ yok.** 80 fotoğrafın hepsinde metin dik; köşe sırası
sözleşmesi (`VisualRows.kt` köşelerin *metnin kendi yönünde* olduğunu ilan
ediyor, ML Kit dokümantasyonu referans çerçevesini söylemiyor) bu partiyle de
kapanmadı.
