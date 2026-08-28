# 41 — Ad bloğunun üst sınırı: kadraja giren ne varsa ada karışıyordu

**24 Ağustos 2026.** `docs/40` iki marka çöpünün de ad bloğunun **üstünden**
sızdığını göstermişti. Bu tur o üst sınırı ölçtü ve koydu.

Ölçüm cihaz istemedi: 46 etiket (27 BİM + 19 A101), hepsi repodaki fikstürler.

---

## Bloğun altı yazılıydı, üstü değildi

Gramaj satırı bloğu **aşağıdan** bitiriyor (`250 G`) ve o kural ölçülmüştü.
Yukarıdan bitiren hiçbir şey yoktu — yani kadraja giren ne varsa bloğa
girebiliyordu:

| Kaynak | Fikstür | Bloğa giren |
|---|---|---|
| Etiketin kendi **besin değerleri tablosu** | `183949` | `Yağlg)` (bozulmuş `Yağ (g)`) |
| Ürünün **kendi ambalajı** | `183947` | `Zme`, `aği Taze Peynir` |
| **Raf tabelası** kırıntıları | `184116` | `U`, `L` |
| Etiketin **künyesi ve fiyat cümlesi** | 5 A101 etiketi | tarih, birim fiyat, KDV cümlesi |

⚠ Üçüncü ve dördüncü sınıfı `docs/40` **saymamıştı**: orada yalnızca *marka*
çöpü aranmıştı, oysa bu ikisi **adı** kirletiyordu.

---

## Ayıraç: satır arası boşluk

Ad bloğunun satırları sık dizili, davetsiz misafir uzakta. Ölçüm:

| | boşluk / lira boyu |
|---|---|
| blok içi, ölçülen **en büyük** | **0,62** |
| davetsiz, ölçülen **en küçük** | **1,55** |

Eşik **1,0** — ikisinin ortası. Gerçek bir satırı düşürmek için aralığın %60
büyümesi, bir davetsizi kaçırmak için %35 daralması gerekiyor.

**Neden lira boyuna oranlanıyor:** mutlak piksel çalışmaz — aynı etiket
yakından da uzaktan da çekiliyor ve fikstür setinde lira boyu **227 ile 697
piksel** arasında değişiyor. Lira etiketin ölçeği ve `readableLira` ile zaten
bulunmuş durumda.

**Neden aşağıdan yukarı:** bloğun bilinen ucu alt uç (gramaj satırı).
Yukarıdan başlamak, aradığımız şeyi bildiğimizi varsaymak olurdu.

### Neden x hizası değil

İlk denediğim ayıraç sol hizaydı ve **çöktü**: `184202` ve `184206`
karelerinde **iki etiket birden** var, yani ikinci etiketin bütün bloğu 559–772
piksel yana kaymış durumda. Hiza, kadrajda tek etiket varsayıyor.

---

## Sonuç: 45 satırın 9'u değişti

| Fikstür | Önce | Sonra |
|---|---|---|
| `183949` | marka `Yağlg)` · ad `AYCA PEYNİR CEŞİTLERİ` | marka **`AYCA`** · ad `PEYNİR CEŞİTLERİ` |
| `183947` | marka `Zme` · ad `aği Taze Peynir tçİM. SÜZME PEYNİR TAM YAĞLI` | marka `tçİM.` · ad **`SÜZME PEYNİR TAM YAĞLI`** |
| `184116` | ad `U L FIÇI KORNİŞON TURŞUSU` | marka `FIÇI` · ad **`KORNİŞON TURŞUSU`** |
| `133226` | `15 Ağustos 2026 1 LT = 80,56 TL KDV Dahildir BİRŞAH SÜT…` | **`BİRŞAH SÜT YARIM YAĞLI (EN; AZ /o1,5 YAĞLI)`** |
| `133211` | `ada KAKAOLU EINDIK KREMASI TORKU BANADA KAKAOLU FINDIK KREMASI` | **`TORKU BANADA KAKAOLU FINDIK KREMASI`** |
| `133411` | `2 Kez Firçalamayla PARODONTAX DIŞ MACUNU` | **`PARODONTAX DIŞ MACUNU`** |
| `133036` | `VEGAN KAYNAĞI 160ge CEREZYA KURUYEMİS FINDIK IÇI` | **`CEREZYA KURUYEMİS FINDIK IÇI`** |
| `133322` | `AMET 1929 Leeet Sanot J25U0JI95-REJ NAMET HÌNDİ FÜME` | `J25U0JI95-REJ NAMET HÌNDİ FÜME` *(kısmen)* |
| `133214` | `FERRERO NUTELLA KAKAOLU FINDIK KRM.` | `NUTELLA KAKAOLU FINDIK KRM.` ⚠ **kayıp** |

**Sekizi kazanç, biri kayıp.** Kalan 36 satır değişmedi.

### Tek kayıp yazılı: `FERRERO`

`133214`te üretici adı etiketin **tepesinde**, bloğun 954 piksel uzağında
basılı — lira boyunun **3,37** katı, yani gerçek davetsizlerle *aynı
mesafede*. `FERRERO`yu tutup `Yağlg)`yi (3,84) düşüren bir eşik **yok**.

Kayıp kabul edildi çünkü küçük: `NUTELLA KAKAOLU FINDIK KRM.` tam bir ürün
adı ve `NUTELLA` zaten insanların söylediği marka. `NameBlockBoundaryTest`
bunu kaydediyor — bir gün başka bir sinyalle geri kazanılırsa o test düşer.

---

## Marka tablosu güncellendi

| | önce | sonra |
|---|---|---|
| BİM'de üretilen marka | 24 | **25** |
| gerçek marka | 22 | **23** (`AYCA` katıldı) |
| marka olmayan | 2 (`Zme`, `Yağlg)`) | 2 (`tçİM.`, `FIÇI`) |

Sayı aynı ama **sınıf değişti** ve ikisi de öncekinden zararsız:

- `tçİM.` bozulmuş, ama **doğru markayı** gösteriyor (`İÇİM`); `Zme` hiçbir
  şeyi göstermiyordu.
- `FIÇI` adın kendi kelimesi — ama o satırın adı artık temiz.

⚠ **Büyük harf ayıracı artık ayırmıyor:** `FIÇI` tamamen büyük harf.
`docs/40`'ta ölçülüp *uygulanmayan* o ayıraç, bugün uygulansa işe yaramazdı —
kalan iki çıktı artık *"kadraja giren yabancı metin"* değil, *"bloğun ilk
satırı marka değil"* sınıfından. Uygulamamak doğru karardı.

---

## Isırma kanıtı

| Tersine çevrilen | Düşen test |
|---|---|
| Kural tamamen kaldırıldı | 5 sınır testi + 3 corpus testi |
| Eşik **2,0** (en küçük davetsizin üstü) | `theShelfSignsCrumbsStayOutOfTheName` + `theA101NamesLoseTheirLeadingClutter` |
| Eşik **0,25** (en büyük blok içi aralığın altı) | 9 test, `TagFieldReaderTest`in dördü dahil |

Ortadaki ısırık en değerlisi: eşiğin **varlığını** değil **yerini** kanıtlıyor.

---

## Ölçüm aracı

Geçici bir döküm 46 etiketin sol kolonunu geometrisiyle diske yazdı; ölçüm
bitince silindi. Kalıcı olan `NameBlockBoundaryTest` ve `BrandQualityTest`.

⚠ `docs/40`'ın dersi uygulandı: araç okuyucunun **kendi** `readableLira()`sını
ve `left` süzgecini çağırdı, kendi türetmemi değil — araç okuyucudan sapınca
ölçüm değil araç yalan söyler.

⚠ İkinci ders: eşiği önce yalnız **BİM'den** ölçüp A101'e de uygulamıştım.
Fark edilince A101 de ölçüme katıldı — `FERRERO` kaybı ancak o zaman görüldü.
