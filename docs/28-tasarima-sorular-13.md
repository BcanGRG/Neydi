# 28 — Tasarıma sorular (on üçüncü tur): liste satırı taşıyabildiğinden fazlasını taşıyor

**22 Ağustos 2026.** Kullanıcı "başka markette ucuz" çipini cihazda gördükten
sonra şunu söyledi:

> *"A101'de 36,00 çipi sanki uzun olsa oraya sığmayacak gibi. Bence burada
> tasarımdan **item görünümü için kapsamlı bir yeni redesign** isteyebiliriz,
> çünkü birkaç tane badge aynı anda gelebilir vs."*

Ölçtük. **Haklı, ve durum tahmininden kötü.**

Bu tur bir hata raporu değil; satırın **bütçesini** masaya koyuyor ve kararı
tasarıma bırakıyor.

---

## Ölçüm nasıl yapıldı

Metin genişlikleri tahmin değil: `plusjakartasans_*.ttf` dosyalarının
`hmtx`/`cmap` tablolarından hesaplandı (kerning hariç, ±%2). Ekran 1080px ÷
2.625 = **411,43dp**; satır dolgusundan sonra kalan **379,43dp**.

Kritik nokta ölçüm sırası: Compose `Row`'da weight'siz çocuklar **önce**
ölçülür, `weight(1f)` olan artakalanı alır. Satırda weight'i olan tek şey **ad
sütunu**. Yani sıkışan şey her zaman **ürün adı**.

---

## Bulgu 1 — Kod bir satıra 8 koşullu öğe koyabiliyor; maketler en fazla 2 çiziyor

Şablon altı koşullu yuva tanımlıyor (`hasQty`, `hasMeta`, `hasChip`,
`hasDelta`, `hasPrice`, `hasPartner`) ama **hiçbir maket verisi ikiden
fazlasını birden açmıyor**. Kodda böyle bir sınır yok.

Bugün hiçbir kural şunların birlikteliğini engellemiyor:

| Kombinasyon | Maket çizmiş mi |
|---|---|
| raptiye + adet rozeti | ❌ (sabit şablonunda adet yuvası yok) |
| adet rozeti + fiyat çipi + eş avatarı | ❌ |
| delta + sparkline + fiyat çipi | ❌ maket delta verince fiyat çipini kapatmış |
| meta metni + ucuz çipi | ❌ maket çip verince meta'yı kapatmış |
| ambalaj değişimi metni + ucuz çipi | ❌ |

Kodun dışladığı yalnızca iki şey var (`SecondLine` sealed interface ve
alışveriş modu). Maketlerin dışladıkları **veriyle** dışlanmış, kuralla değil —
yani kod onları öğrenemedi.

---

## Bulgu 2 — Ada kalan pay %35'e düşüyor ve sıradan bir ad kırpılıyor

Senaryo: sabit + `1 kg` rozeti + `1.234,56 TL` + eş avatarı (planlama modu).

```
onay dairesi         24,00
onay-ad boşluğu      12,00
raptiye              12,00
boşluk                8,00
adet rozeti "1 kg"   48,54
boşluk                8,00
fiyat çipi           99,38
avatar boşluğu        8,00
eş avatarı           24,00
---------------------------
SABİT               243,92   (%64,3)
ADA KALAN           135,51   (%35,7)
```

- `"Beyaz Peynir 600 g Tam Yağlı"` **234,07dp** ister → `Beyaz Peynir 6…`
- `"Tam Buğday Ekmek"` **158,66dp** ister → `Tam Buğday Ek…`

360dp'lik bir ekranda ada kalan **84,08dp** — yaklaşık **dokuz karakter**.

Kodun kendi kuralı bunu yasaklıyor ve tutmuyor:

> *"Ad kırpılması kabul edilemez — fiyat ipucu yardımcı bilgi, ad ise satırın
> varlık sebebi."*

Alınan tek önlem ucuz çipini ana satırdan indirmek olmuş; fiyat çipi, adet
rozeti ve avatar hâlâ sınırsız kardeşler.

---

## Bulgu 3 — İkinci satır, taşımak için var olduğu cümleyi kaybediyor

Trend hâlinde delta çipi + sparkline + aralarındaki boşluklar **91,07dp**
alıyor; `MetaText`'e **44,44dp** kalıyor, oysa `"önce 1.234,56"` **92,29dp**
istiyor. Sonuç: `önce…` — süs kalıyor, cümle gidiyor.

Ucuz çipi hâlinde daha kötü: `"Tarım Kredi'de 1.234,56"` **155,68dp** ister,
çipe verilebilen en fazla **129,51dp**. *(Çipin `maxLines` kilidi bu turda
kondu; öncesinde metin sarıyor, satır `rowWithMeta`'yı sessizce aşıyor ve meta
sıfır genişliğe düşüp yok oluyordu.)* Kısa çiple bile (`A101'de 159,90` =
108,22dp) meta'ya **21,29dp** kalıyor.

---

## Bulgu 4 — Üç sessiz sapma (maket ↔ kod)

1. **92dp fiyat sütunu kodda hiç kurulmamış.** `SizesExtra.priceColumn = 92.dp`
   **ölü sabit** — tüm depoda yalnız tanımı ve bir KDoc cümlesi geçiyor.
   Compose Spec açıkça *"fiyat sütunu ayrıca `Modifier.width(92.dp)` ve
   `TextAlign.End` ile sabitlenir"* diyor; maket altı yerde `width:92px`
   yazıyor. Sonuç: 4 haneli fiyat çipi **99,38dp**'ye çıkıp fazlasını doğrudan
   addan çalıyor.
2. **Delta + sparkline yanlış katta.** Maket ikisini **ana satırda,
   `flex:none`** çiziyor — ad sütunuyla yarışmıyorlar. Kod ikisini **ikinci
   satırın içine** koymuş, yani zaten dar olan sütunun içine.
3. **Ucuz çipi yanlış katta.** Maket çipi ad sütununun **üçüncü satırı**
   yapıyor (`align-self:flex-start`). Kod onu meta metninin **kardeşi** yapmış
   — tam da kodun kendi yorumunun *"kardeş olursa adı kırpar"* diye reddettiği
   düzenin bir kat aşağıdaki kopyası.

Ayrıca satır yüksekliği: fiyat çipinin 48dp'lik dokunma hedefi (karar 56)
fiyatlı **her** satırı 56dp yerine **64dp** yapıyor. Maket aynı işi 32px'lik
hap + `min-height:40px` ile 56px'te yapıyor. Satır başına 8dp fazla, *"10–11
satır görünür"* hedefini pratikte **9**'a düşürüyor.

---

## Sorular

### S1 — Ad sütununa taban mı, feda sırası mı?

Bugün kod ne birini ne ötekini biliyor; kırpılan hep ad. İki yol:

- **Taban genişlik** (`widthIn(min = …)`): ad hiçbir zaman şu kadarın altına
  inmez, taşan öğeler kırpılır.
- **Feda sırası**: yer yetmediğinde önce sparkline düşer, sonra delta, sonra
  rozet — ad en son.

Hangisi? Taban seçilirse **kaç dp**?

### S2 — Fiyat çipi 92dp'ye sığmıyorsa ne olacak?

`"1.234,56 TL"` 99,38dp istiyor. Üç yol:

- **(a)** çip 92dp'de kalsın, fiyat kırpılsın/küçülsün
- **(b)** 4 hane için ayrı genişlik (ör. 104dp)
- **(c)** 1.000 TL üstünde kuruş düşsün — `1.235 TL` = 79,5dp, rahat sığar.
  Bu, karar 67'nin manşet kuralının (*"kuruş sıfırsa yazılmaz"*) satıra taşınmış
  hâli olurdu.

### S3 — Delta + sparkline hangi katta?

Maketin katına mı dönelim (ana satır, `flex:none`), yoksa ikinci satırda kalıp
**meta metni öncelikli** mi olsun — yani sığmadığında sparkline düşsün?

### S4 — Trend satırında fiyat iki kez mi yazılacak?

Maket `"Ayçiçek Yağı 5 L"` satırında fiyatı meta içine koyup
(`324,00 → 369,90 TL`) fiyat çipini **kapatmış**. Kod ikiye bölmüş: meta
`önce 324,00` + çip `369,90 TL`. Kodun kendi yorumu *"İkinci satır güncel
fiyatı yazmaz"* diyor ama `PackChanged` dalı tam da onu yapıyor ve o satırda
fiyat çipi hiç çizilmiyor. **Üç dal için tek bir kural** yazar mısınız?

### S5 — Ucuz çipi kendi satırına insin mi?

Maket onu üçüncü satır yapmış ve **meta ile birlikte hiç çizmemiş**. Üç satırlı
bir satır (ad / meta / çip, ~86dp) kabul mü, yoksa çip varken meta susacak mı?
Ayrıca: çipin metni için bir **genişlik tavanı** var mı? Market adı kullanıcının
yazdığı serbest metin.

### S6 — Raptiye + adet rozeti aynı satırda olabilir mi?

Maket bunu hiç çizmedi çünkü iki ayrı şablonu var; kodda tek şablon var ve
"Her zamankiler" bölümündeki bir satır pekâlâ `2x` taşıyabiliyor.

### S7 — Eş avatarı yer değiştirmeli mi?

Maket avatarı hep tek başına çizdi. Kodda avatar + boşluk 32dp'yi fiyat çipinin
sağından alıyor. Adın önüne mi taşınmalı, fiyat çipi varken mi düşmeli?

### S8 — Satır yüksekliği sözleşmesi hangisi?

Ya satır 64dp olur (ve *"10–11 satır"* hedefi 9'a düşer), ya da çipin dokunma
hedefi satırın kendi yüksekliğinden gelir (görsel hap 32dp kalır, hedef satır
boyunca uzar).

### S9 — En kötü hâli maket olarak çizer misiniz?

İstenen tek şey: **raptiye + `1,5 kg` rozeti + 28 karakterlik ad + ambalaj
değişimi metni + ucuz çipi + 4 haneli fiyat + eş avatarı**, hem **411dp** hem
**360dp** genişlikte. Bugün böyle bir maket yok — bu yüzden koda bakan hiç kimse
*"sığıyor mu"* sorusunu ölçüyle cevaplayamıyor.

### S10 — "Gerekçe" satırı canlanacak mı?

`suggestionReason` alanı (*"12 gündür almadın"*) üretimde **hiçbir yerden
dolmuyor**; `SecondLine.Reason` bugün yalnızca önizlemede var. Tasarım hâlâ
vaat ediyor. Redesign'da bu üçüncü içerik bütçeye girecek mi?

---

## Ekler — F6.5'ten çıkan üç soru

### S11 — Sabit terfisi otomatik mi, değil mi?

İki tasarım dosyası çelişiyor:

| Dosya | Cümle |
|---|---|
| Boş Durumlar | *"…**üç geziden sonra kendiliğinden** burada birikir."* |
| Ekranlar 5-8 | *"…**birkaç geziden sonra** kendiliğinden burada birikir; **istersen şimdi de ekleyebilirsin.**"* |

Kodun kendi KDoc'u otomatiği **yasaklıyor**: *"öneri motoru kendi başına set
etmez — sabitlik bir çıkarım değil, **beyan**"*. Bu yüzden Ayarlar'ın boş hâli
tasarımın cümlesini yazmıyor; olmayan bir davranışı vaat etmemek için.

Vaat yumuşadı mı, kalktı mı?

### S12 — "Bunu önerme" açıkken anahtar ne söylüyor?

Yedi çizimin **hepsi kapalı** hâli gösteriyor. Açık hâlin alt açıklaması
(`supporting`) çizilmemiş — ör. *"3 Ağustos'ta engellendi"*. Bileşende yuva
hazır bekliyor. Boş mu kalsın?

### S13 — AUTO bloğu listede MANUAL'den ayırt edilecek mi?

`BlockSource` ayrımı kodda var ve KDoc onu davranışsal olarak anlamlı sayıyor
(*"kullanıcının elle kaldırdığı bir engeli motorun sessizce geri koymaması"*),
ama tasarım listede iki satırı **aynı** çiziyor. Kullanıcı *"bunu ben mi dedim,
uygulama mı?"* sorusunu soramıyor. Ayrım görünecek mi?

---

## Değişecek yerler

S1–S3 ve S6–S8 `ListItemRow.kt`'nin düzeni, S4 `PriceHintMapping.kt`, S5 hem
`ListItemRow` hem `AccentChip`, S10 `ListState.toUiRow`, S11 `ProductDao.
setStaple`'ın değişmezi, S12 `ProductSheet.kt`, S13 `SuggestionBlockDao.
observeBlocked` + `SettingsScreen.kt`.
