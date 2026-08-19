# Tasarıma sorular — sekizinci tur

**Konu: Ekleme akışı yapısal olarak yeniden ele alınmalı mı?**

Bu tur bir denetimden değil, **kullanıcının kendi cihazında yaşadığı iki
şikayetten** doğdu. İkisi de tek bir yüzeye bakıyor: Ekran 1'in ekleme yolu.

> *"listede ekleme yaparken ne lazım a basınca neden bottomsheet açılıyor,
> ekleme kısmı çok basit ve hızlı yapılabilmeli"*
>
> *"Reyondan ekle kısmının hem tasarımı çok kesik kesik duruyor hem de ux
> açısından hiç kullanışlı değil"*

Aşağıdakiler ölçüm; öneri değil. Kararı tasarım versin.

---

## Soru 1 — Ekleme neden bir sheet açıyor?

### Karar 63 akışı bir adım uzattı

Karar 63 kökteki metin alanını butona çevirdi ve gerekçesi sağlamdı: *"kökte
dördüncü bir metin alanı, Ekle sheet'iyle aynı işi yapan ikinci bir yol
demekti"*. Kodlandı (PR #77). Ama akışın **dokunuş sayısı** şöyle değişti:

| | Önce (metin alanı) | Şimdi (buton + sheet) |
|---|---|---|
| Yazarak ekle | alana dokun → yaz → Bitti | **butona dokun** → sheet → **arama alanına dokun** → yaz → **sonuca dokun** |
| Dokunuş | 1 + klavye | **3** + klavye |

Bir tur alışverişte on kalem yazan biri için fark otuz dokunuş. Kullanıcının
"çok basit ve hızlı yapılabilmeli" dediği şey bu.

### Sheet'in kendisi de ücretsiz değil

Sheet açılıyor, listeyi örtüyor, kapanması gerekiyor. Ekleme *listeye* yapılan
bir iş ama listeyi görmeden yapılıyor. Sheet'in "N ürün eklendi" sayacı zaten
bu körlüğün telafisi olarak var (kodun kendi yorumu: *"sheet kapanmadığı için
kullanıcı listeye bakamıyor, sayaç tek geri bildirim"*) — yani tasarım bu
maliyeti biliyor ve bir yama koymuş.

### Sorular

1. **Kökteki hızlı yazma yolu geri gelmeli mi?** Karar 63'ün gerekçesi "ikinci
   bir yol" idi; ama iki yol *aynı işi* yapmıyor olabilir: biri **bildiğin
   şeyi yazmak**, öteki **ne alacağını hatırlamak**. Bunlar farklı işler.
2. Geri gelecekse "hiçbir ekran açılırken klavye açmaz" kuralı nasıl korunur?
3. Gelmeyecekse sheet açılınca **arama alanına odak gitmeli mi**? Bugün
   gitmiyor; giderse dokunuş 3'ten 2'ye iner ama klavye kuralı tartışılır.

---

## Soru 2 — "Reyondan ekle" bu hâliyle kullanışlı mı?

### Ölçülen hâli (SM-G975F, 1080×2280)

Sheet **kısmi açık** geliyordu ve içerik ekranın %47'sine sığmıyordu.
uiautomator çıktısı:

| Öğe | Yükseklik | |
|---|---|---|
| 1. sıra kutucukları | 70px | tam |
| 2. sıra kutucukları | 70px | tam |
| **3. sıra kutucukları** | **22px** | **kırpık** |
| **3. sıra etiketleri** | **0px** | **hiç çizilmiyor** |
| 4. sıra (3 reyon) | 0px | hiç yok |
| **"Listede yok, kendim yazayım"** | **`bounds=[0,0][0,0]`** | **dokunulamaz** |

Kullanıcının "kesik kesik" dediği şey bu. Sonuncusu ayrıca işlevsel bir
kayıptı: katalogda olmayan bir ürünü eklemenin tek yolu o butondu.

**Kök sebep:** kısmi açık M3 `ModalBottomSheet` içeriğine sınır vermiyor,
**kırpıyor**. İçerik ekran boyuyla ölçülüyor, sheet yarısını gösteriyor.
İçerik tarafından hiçbir ölçü bunu çözemez — `heightIn(ekran × oran)` bir
tahmindi ve kendi TODO'sunda *"sessizce taşar"* diye yazıyordu; taştı.
`fillMaxHeight()` de çare değil, gelen sınır zaten ekran boyu.

**Yapılan:** sheet tam açık hâle getirildi (`skipPartiallyExpanded`). Artık
12 reyonun hepsi tam çiziliyor ve kaçış butonu dokunulabilir. **Bedeli:**
"liste arkada görünür kalsın" özelliği gitti. Bu özelliği tasarım dosyası
değil, kodun kendi yorumu iddia ediyordu — ama yine de sapma olarak
bildiriyoruz.

### Bilgi taşımayan kutucuklar

Grid 12 reyonu **iki harfle** çiziyor: `ME` `FI` `SÜ` `ET` `ŞA` `DO` `TE` `KO`
`AT` `İÇ` `TE` `Kİ`. Daire hiçbir şey söylemiyor; okunması gereken şey altındaki
etiket. Üstelik **iki reyon aynı iki harfle başlıyor** (`TE` = Temel Gıda ve
Temizlik), yani kısaltma ayırt bile etmiyor.

### Yolun maliyeti

Reyondan ekleme: butona dokun → reyona dokun → ürüne dokun = **3 dokunuş**, ve
ilk iki dokunuşta ekranda hiç ürün adı görünmüyor.

### Sorular

4. **Reyon gezinmesi v1'de kalmalı mı?** Kullanıcı "hiç kullanışlı değil"
   diyor. Alternatif: sheet doğrudan ürün listesiyle açılsın (en yaygın N
   ürün), reyon yalnızca bir filtre çipi olsun.
5. Kalacaksa kutucuk **iki harf** mi olmalı? İki reyon aynı harfleri
   paylaşıyor.
6. Sheet tam açık kalsın mı, yoksa "liste arkada görünsün" kuralı korunup
   içerik mi küçülmeli? (İkincisi seçilirse hangi eleman düşecek —
   kaçış butonu düşemez.)

---

## Bu turda tasarımdan beklenen

Bu iki yüzey **yapısal olarak** yeniden ele alınabilir; kullanıcının isteği o.
Nokta düzeltme değil, akışın kendisi sorgulanıyor:

- Ekleme kaç yoldan yapılmalı ve hangisi birincil?
- Reyon gezinmesi bir ekleme yolu mu, yoksa bir keşif yolu mu?
- Sheet doğru kap mı?

Kod tarafında bugün **kırık olan** düzeltildi (kırpılan grid, dokunulamayan
buton). Yapısal karar beklemede.
