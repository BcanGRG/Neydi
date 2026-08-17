# E12 — Etiket ölçümü: 27 gerçek BİM etiketi

**17 Ağustos 2026.** Kullanıcı BİM'de 27 raf etiketi fotoğrafı çekti (18:36–18:43,
telefonun kendi kamerası, 4032×3024). Hepsi ML Kit Text Recognition v2'den
geçirildi, ham çıktı `composeApp/src/commonTest/etiket-fikstur/` altında.

**27/27 okundu, sıfır hata.** Toplam süre 31 saniye.

> Bu doküman bir ölçüm raporu, tasarım değil. Her iddianın arkasında fikstürde
> bakılabilir bir satır var.

---

## Soru 1 — Kuruş üstsimgesi tek parça mı, iki parça mı?

**Cevap: ikisi de değil. Çoğu zaman HİÇ OKUNMUYOR.**

27 etiketin en büyük glifli satırı sayıldı:

| Durum | Adet |
|---|---|
| Lira + ayırıcı bitişik (`19,` `69.` `33,` `59,` `74,` `86.`) — kuruş hanesi yok | **6** |
| Kuruş ayrı bir parça olarak bulundu (`74,` + `501`) | **1** |
| Ne ayırıcı ne kuruş — yalnızca lira (`149` `60` `34` `219` `209` `389` `255` `143`…) | **21** |

Tek net vaka `20260817_184225` (SABAN KIRMIZI MERCİMEK 1 KG) ve yapısı öğretici:

```
x=1519 y=2529 w=1163 h=697   74,      <- lira, AYIRICI BİTİŞİK
x=2802 y=2701 w= 228 h=200   501      <- kuruş, AYRI parça, 3,5x küçük, sağ üstte
x=2475 y=2160 w= 577 h=315   89,s0    <- ESKİ fiyat (üstü çizili)
x=1881 y=3499 w= 274 h= 60   74,50t   <- birim fiyat satırı, TEMİZ
```

Üç ders:

**Ayırıcı liraya ait.** Birleştirme `"74," + "50"` → `74,50`; `74` + `,` + `50`
değil. Kuruş parçası ayırıcı taşımıyor.

**Kuruş yanlış okunuyor.** `50` → `501`. Başka etiketlerde `s0`, ve
`20260817_183830`'da kuruş bir **derece işaretine** dönüşmüş: `2,°`.

**Çoğunlukla hiç gelmiyor.** 21 etikette ayırıcı bile okunmamış — yani kuruşu
"ayrı parça olarak topla" stratejisi de vakaların dörtte üçünde boşa düşüyor.

### Bunun E14'e maliyeti

**`parseMinor` bu 27 etiketin hiçbirinin manşet fiyatını okuyamıyor.** Fonksiyon
**tam iki ondalık hane** şart koşuyor (`Money.kt`, gerekçesi yazılı: iki hanesi
olmayan sayı fiyat değil, miktar ya da barkod parçasıdır). Etiketlerin verdiği
`74,` ya da `149` bu desene uymuyor.

Yani E14'ün *"fiyat = para desenine uyan parçalar arasından en büyük glifli"*
kuralı bugünkü hâliyle **hiçbir şey seçmiyor** — süzgeç önce her şeyi eliyor.

---

## Soru 2 — Elde çekimde yön düzeltmesi gerekiyor mu?

**Cevap: evet, ve zorunlu.**

27 fotoğrafın **26'sı EXIF=6** (telefon dikey tutulmuş, sensör yatay kaydetmiş),
biri EXIF=1 (yatay tutulmuş). Kaynak boyutu hepsinde 4032×3024.

OCR'a giren görüntünün boyutu kanıtı veriyor:

| Kaynak EXIF | Kaynak | OCR'a giren | Anlamı |
|---|---|---|---|
| 6 (26 fotoğraf) | 4032×3024 | **3024×4032** | `downscaleForOcr` döndürdü |
| 1 (1 fotoğraf) | 4032×3024 | 4032×3024 | döndürecek bir şey yoktu |

Düzeltme olmasaydı 26 fotoğraf ML Kit'e **yan** girecekti — F4.20'de fişte
görülen ve sayfayı sekiz dev satıra çöken hatanın aynısı.

**Ölçek gerekmedi:** 4032 < `MAX_LONG_EDGE` (4096). Yani EXIF piksele işlenirken
küçültme hiç devreye girmedi — `downscaleForOcr`'ın ölçekten bağımsız olarak
yönü işlemesi bu yüzden önemliydi.

---

## Beklenmeyen üçüncü bulgu — "en büyük glif" kuralı %22 yanılıyor

27 etiketin **6'sında** en büyük glifli satır fiyat değil:

| Etiket | En büyük glif | Yükseklik |
|---|---|---|
| 183645 | `OMIKPA` | 774 |
| 183746 | `Şekeňm` | 927 |
| 184116 | `RNİŞON TUP` | 608 |
| 184202 | `Krena` | 1032 |
| 184206 | `Kar` | 1244 |
| 183808 | `86.` | 12 *(başarısız çekim)* |

Aktüel/kampanya etiketlerinde **marka adı fiyattan büyük basılıyor.** Kural
"para desenine uyanlar arasından" dediği için teorik olarak korunuyordu — ama
soru 1 gösterdi ki o süzgeçten hiçbir şey geçmiyor. İkisi birleşince kural
çalışmaz hâle geliyor.

`183808` ayrı bir vaka: EXIF=1 olan tek fotoğraf ve en büyük glifi **12 piksel**
— yani OCR neredeyse hiçbir şey okumamış. Muhtemelen bulanık. Bir başarısız
çekimin fikstürde durması iyi: hata yolunun gerçek örneği.

---

## Dördüncü bulgu — birim fiyat satırı etiketteki EN TEMİZ sayı

10 etikette birim fiyat satırı **iki ondalık hanesiyle** okundu:

```
239,44B | kg      2,90t / adet      83,60* /kg       223,09% / kg
779,00t / kg      93,80 litre       126,12Í ikg      11,92t / adet
T06,00 kg         E7,50hg
```

Sebebi basit: birim fiyat **normal punto** basılıyor, manşet fiyat ise dev
punto + üstsimge. `parseMinor` bu satırlarda **çalışıyor**.

Baştaki/sondaki karakter kirliliği (`T06,00`, `E7,50`, `239,44B`) para birimi
simgesinin sızması — `parseMinor` zaten `*` ve `x` önekini kırpıyor, aynı sınıf.

**`184225`'te birim fiyat manşet fiyata eşit** (`74,50t` ↔ `74,` + `501`), yani
o etikette birim fiyat manşeti **doğrulayabilir**. Ama genel değil: `184300`'de
manşet `143`, birim `11,92 / adet` — çoklu paket.

---

## E14 için ne değişti

Bunlar ölçüm; kararı E14 verecek. Ama üç şey artık kanıtlı:

1. **Fiyat tek bir OCR parçasından okunamıyor.** Lira ve kuruş ayrı parçalar ve
   kuruş çoğu zaman yok. Ayrıştırıcı **lira-yalnız fiyatı kabul etmek zorunda**,
   yoksa 27 etiketin 21'inde hiçbir şey bulamaz.
2. **`parseMinor` manşet fiyat için kullanılamaz.** Ya ayrı bir "etiket fiyatı"
   ayrıştırıcısı gerekiyor, ya `parseMinor`'a iki-hane şartını gevşeten bir
   kapı — ve o kapı fiş döneminde bilinçli olarak kapatılmıştı, gerekçesi
   `Money.kt`'de yazılı. Karar bilinçli verilmeli.
3. **Birim fiyat satırı bir doğrulama kaynağı.** ROADMAP'te `priceUnit` +
   `packSize` çıkarımı olarak duruyordu; ölçüm onu **fiyatın kendisi için de
   kullanılabilir** hâle getiriyor.

---

## Koşum

**Kalıcı** (E14'ün okuyucu yarısı): `data/ocr/TagReader.kt` + Android actual +
iOS stub. Tek bitmap, tek ML Kit çağrısı; şerit/yön oylaması yok.

**Geçici** (fikstür üretildikten sonra silinecek): `TagFixtureDump.android.kt`
ve `MainActivity`'deki tetikleyici blok. Yeni gradle kaynak kümesi açılmadı —
ML Kit zaten `androidMain`in sınıfında, uygulama zaten cihazda koşuyor, eksik
olan tek şey bir tetikleyiciydi.

```bash
adb shell mkdir -p /sdcard/Android/data/com.neydi.app/files/etiket-in
adb shell cp /sdcard/DCIM/Camera/*.jpg /sdcard/Android/data/com.neydi.app/files/etiket-in/
adb shell "echo go > /sdcard/Android/data/com.neydi.app/files/etiket-in/RUN"
adb shell am force-stop com.neydi.app && adb shell am start -n com.neydi.app/.MainActivity
adb logcat -d -s E12:I
adb pull /sdcard/Android/data/com.neydi.app/files/etiket-out
```

### Kendi hatam, kayda geçsin

İlk koşumda **her fikstüre `exif=0` yazdı.** EXIF'i `downscaleForOcr`'ın yazdığı
dosyadan okuyordum — o dosya tanım gereği **etiketsiz** (yön piksele işlenmiş).
Doğru cevap, yanlış soruya. Kaynak EXIF artık çağırandan geçiyor.

Fark önemsiz değildi: soru 2'nin bütün kanıtı o alan ile OCR boyutunun
karşılaştırılmasına dayanıyor.

---

## Eksik kalan

**Metro örneği yok** (toptancı; birim fiyat baskın, koli fiyatı, bazen KDV
hariç) ve **kasıtlı 90° / eğik kontrol karesi yok.** İkincisi hâlâ açık bir
soruyu kapatabilir: `VisualRows.kt` köşe sıralamasının **metnin kendi yönünde**
olduğunu ilan ediyor, ML Kit dokümantasyonu ise referans çerçevesini
söylemiyor. Bu 27 fotoğrafta metin dik olduğu için iki okuma çakışıyor ve
ayrım görünmüyor.
