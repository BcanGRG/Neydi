# 24 — A101 grameri: 19 etiketten ölçüm

**21 Ağustos 2026.** Kullanıcı A101'de 19 raf etiketi çekti — ama **telefonun
kendi kamerasıyla**, çünkü o gün uygulama her karede yanlış bir ürün adı
öneriyordu ve turu yarıda bıraktı.

Fotoğraflar sonradan `dumpImportedPhotos` ile **aynı ML Kit yolundan**
geçirildi. Ölçümün kaynağı değişti, yöntemi değişmedi: buradaki her sayı yine
cihazda ML Kit'ten çıktı. İkinci bir market turu gerekmedi.

Fikstür seti 80'den **99'a** çıktı.

> Bu doküman bir ölçüm raporu. Her iddianın arkasında fikstürde bakılabilir bir
> satır var — `TagFixtures.of("A101")`.

---

## Merkezdeki bulgu: virgül fiyat boyunda, kuruş yarım boy üstsimge

`133227`nin fotoğrafına bakıldı. A101'in dizgisi şu:

```
43 , ⁵⁰ TL
│  │  │
│  │  └─ kuruş: yarım boy, üstsimge, liraya tepeden hizalı
│  └──── virgül: LİRANIN KENDİSİ KADAR BÜYÜK, taban çizgisinde
└─────── lira
```

O büyük virgül A101'in bütün OCR kusurlarının kaynağı. **Aynı etiketin üç
çekimi üç ayrı şey okuttu** — BİRŞAH SÜT 1 L, 43,50 TL:

| Kare | manşet | ayrı kuruş | virgüle ne oldu |
|---|---|---|---|
| `133220` | `439` h=546 x=1603..2432 | `,50` h=241 x=2209 | **9 rakamı sanıldı** |
| `133226` | `43-50` h=429 | — (kaynadı) | tire sanıldı |
| `133227` | `43` h=525 x=1444..2016 | `50` h=229 x=2079 | düştü |

Bu üçlü grameri tek başına yazdırdı.

---

## BİM'in kuralı A101'de çalışmıyor

BİM'de lira ve kuruş **iki ayrı parça** gelir ve `readTagPrice` bunun üzerine
kurulu. A101'de kuruş **beş ayrı şekilde** geliyor ve 19 karenin yalnızca
üçünde ayrı bir parça:

| Şekil | Ölçülen | Kural |
|---|---|---|
| Parçanın içinde tam para | `19.75`, `99,50` | 1 |
| Ayrı parça | `43`+`50`, `439`+`,50`, `595`+`50` | 2 |
| Liraya yapışık | `6450`, `6490`, `11650`, `43-50`, `1585o` | 4 |
| Etikette yok (tam lira) | `188.`, `95`, `135`, `105.`, `169.`, `249.`, `265,` | 5 |

Üstelik BİM'in kuralı `133220`de **aktif olarak zarar verirdi**: orada ayırıcı
taşıyan kuruş adayı "üstü çizili eski fiyat" demekti ve elenirdi — burada doğru
cevabın kendisi.

---

## Yapışan glifi geometri ayıklıyor, metin değil

`439`+`,50` ve `595`+`50` aynı kusur: kuruşun ilk glifi manşetin kutusuna
girmiş. Kanıt kutuların kendisinde — manşetin **sağ** kenarı kuruşun **sol**
kenarını geçiyor:

| Kare | manşet kutusu | kuruş x | fark | karar |
|---|---|---|---|---|
| `133220` | 1603..2432 | 2209 | **223 px bindirme** | son hane atılır |
| `133322` | 1557..2320 | 2125 | **195 px bindirme** | son hane atılır |
| `133227` | 1444..2016 | 2079 | 63 px boşluk | hane korunur |

Bindirme her iki vakada da bir glif genişliğine denk (276 ve 254 piksel/hane).
Metne bakarak ayırt edilemezdi: `439`un son hanesi 9, kuruşun ilk hanesi 5.

---

## Kuruş sıfırsa A101 onu hiç basmıyor

Dokuz karede kuruş parçası hiç çıkmadı. İlk açıklamam *"üstsimge sıfırlar ML
Kit'e görünmüyor"* idi ve **yanlıştı**. Üç etiketin fotoğrafına bakıldı:

| Kare | etikette yazan |
|---|---|
| `133214` | `265 TL` |
| `133249` | `105 TL` |
| `133340` | `249 TL` |

Kuruş **basılı değil**. Okunacak bir şey yoktu; tabelanın dizgisi böyle.

Dolayısıyla kuruş parçası yoksa fiyat tam lira ve `kurusFromOcr = true` —
yani kart *"Kuruş okunamadı"* uyarısı **göstermiyor**. Dayanak:

- Kuruşu sıfır olan 9 karenin 9'u çapraz kontrolle doğrulandı
  (`1.253,33 × 150 G = 188,00`, `52,50 × 2 KG = 105,00`, `5,63 × 30 = 169,00` …),
  üçü ayrıca fotoğraftan.
- Kuruşu sıfır **olmayan** 10 karenin 10'unda kuruş okundu — biri bile kaçmadı.

`false` dönmek daha temkinli *görünürdü* ama değil: kart A101 etiketlerinin
yarısında uyarı verir, kullanıcı her seferinde bakıp kontrol edecek bir şey
bulamaz ve uyarı gerçekten önemli olduğu yerde anlamını yitirirdi.

**Kalan risk açık ve çapraz kontrol bunu yakalamaz:** OCR basılı bir `50`yi
kaçırırsa 43,00 yazılır, oran %1,15 sapar ve %2 toleransın içinde kalır. İddia
`A101GrammarTest.aPrintedKurusIsNeverMissed` ile kilitli — karşı örnek çıkan
gün kırılacak test o.

---

## Sonuç

| Alan | 19 karede | not |
|---|---|---|
| **Fiyat (gramer)** | **19 doğru / 0 yanlış** | üç kural sırayla |
| **Fiyat (kart)** | **15** | çapraz kontrol 4'ünü eledi |
| **Gramaj** | **18** | biri yumurta `53-62 G` aralığı, bilerek boş |
| **Ad (ipucu)** | 19 | ~13'ü işe yarar, gerisi kadraj kirliliği |
| **Marka** | **0** | bilerek — aşağıda |

### Elenen 4 karenin hepsi kadrajda **iki etiket** olan kareler

`133220`, `133226`, `133227`, `133411`. Dördünde de manşetin sahibi bir etiket,
okunan birim fiyat satırı öteki etiket; çapraz kontrol uyuşmadığını görüp
fiyatı bırakıyor. Doğru davranış — hangi etiketin kastedildiği bilinmiyor.

**Sahada bu oran daha iyi olmalı:** bu 19 kare telefonun kamerasıyla, rehbersiz
çekildi. Uygulamanın kendi çekiminde kırpım tek etikete daralıyor.

### Ad okunuyor, marka okunmuyor

A101 her etikette ad bloğunun üstüne bir künye kodu basıyor — `0430 2605091327`,
`0430_2608021225`, `0430260R190943` — ve BİM'in `isStoreCode` süzgeci yalnızca
`P728` biçimini tanıyor. **19 adın 19'u** bu önekle geliyordu. Süzgeç biçime
değil orana bakıyor (yarıyı geçen oranda rakam); `0430` öneki kullanılmadı,
çünkü o A101'in mağaza numarası ve başka şubede başka.

Kod ayıklandıktan sonra marka kutusu doldu ve sekizi doğru geldi (`LAYS`,
`TUKAŞ`, `PETEK`, `BİRŞAH`, `NİMET` …). Ama **altısı yanlıştı**:

```
VEGAN                     ← ürünün AMBALAJINDAN, etiketten değil
AMET                      ← NAMET'in baş harfi düşmüş
ada                       ← gürültü
FİYAT GEÇERLİLİK TARİHİ   ← etiketin kendi künyesi
Ürt. yeri:Türkiye         ← etiketin kendi künyesi
```

Sebep yapısal: A101 fotoğraflarının çoğunda ürünün **kendi ambalajı** da
kadrajda ve ambalaj yazısı ad satırıyla aynı kolonda. `VEGAN` ile `LAYS`i
biçimden ayırt etmenin yolu yok.

Karar 26 satırın kimliğini market+marka çifti yapıyor: yanlış marka **kalıcı**
bir ayrışma. Karar 39 ise markayı yalnızca öneri sayıyor ve `null`u meşru cevap
kabul ediyor. Kullanıcı markayı karar 52'nin çip sheet'inden seçiyor.

Okunan marka yine de atılmıyor — ipucu metnine geri katılıyor, çünkü *"Etiket
metni: LAYS PATATES CİPSİ"* kullanıcıya *"PATATES CİPSİ"*den daha çok şey
söylüyor. **İpucu kalıcı değil, kimlik kalıcı.**

---

## Ölçülmeyen: bin liranın üstü

19 karenin hiçbirinde manşet bin lirayı geçmiyor. Böyle bir etikette kuruş
sıfırsa manşet `1.250.` diye gelir ve "son iki hane kuruş" kuralı 12,50 yazardı.

Buna karşı bir koruma yazıldı (binlik ayırıcı görünce hepsini lira say) ve
dayanağı ölçülmüş: ML Kit A101'in binlik noktalarını birim fiyat satırında
**koruyor** — `1 KG = 1.253,33 TL` (`133036`), `1 LT 2.330,00 TL` (`133411`).
Koruma yalnızca açık bir ayırıcı görünce ateşlendiği için muhafazakâr ve
`theThousandsGuardFiresOnNoMeasuredTag` ölçülen 19 kareyi değiştirmediğini
kilitliyor.

---

## Yan bulgu: desteklenen zincir cümlesi Türkçe değildi

`SUPPORTED_CHAINS` listesi cümleyi `joinToString(" ve ")` ile kuruyordu. İki
zincirle doğru görünüyordu — *"BİM ve Migros"* — ve **üçüncüsü eklendiği gün**
*"A101 ve BİM ve Migros"* çıktı. Son zincir "ve" ile, öncekiler virgülle
bağlanacak şekilde düzeltildi.

Kusuru testin kendisi yakaladı; cümlenin elle yazılmadığını kanıtlamak için
konmuş olan test, üçüncü gramer eklendiğinde tam da beklendiği gibi kırmızı
yandı.
