# Tasarıma sorular — dördüncü tur

**16 Ağustos 2026.** Pivot turu uygulandı; karar defterinin yirmi maddesinin
kod durumu [`11-tasarim-kararlari.md`](11-tasarim-kararlari.md)'de.

Bu tur **yeni bir kapsam açmıyor**. Kararlar uygulanırken *başka dosyalarda*
kalmış eski hâller ve kodda karşılığı olmayan bir istek var. Hepsi düzeltme
işi; ikisi hariç cevap "evet, tazele" olabilir.

Biçim aynı: **tasarımın verdiği** → **gerçek** → **soru**.

---

## 1. Boş durum atlası kararlarla çelişiyor — dört çerçeve

Atlas pivot turunda tazelenmemiş görünüyor. Dört çerçeve, bugün geçerli olan
kararların *öncesini* çiziyor. Hepsi tek tek küçük ama atlas bir referans
belgesi: kodlarken bakılan yer orası.

### 1a · Çerçeve 03 — dört hedefli toolbar

**Çizilen.** Floating toolbar dört hedef taşıyor: `add` (dolu kiremit),
`undo`, `filter_list`, `Bitir (0/18)`.

**Karar 3.** *"Toolbar iki hedefe indi: add ve Bitir (n/n). undo ile
filter_list kaldırıldı."* Uygulamada iki hedef var; `Undo` ve `FilterList`
ikonları bu turda koddan da silindi.

**Ayrıca aynı çerçevede:** başlık `Migros Ataşehir · 0/18 alındı` diyor.
Karar 28'e göre market adının kaynağı *o gezide son çekilen etiketin
marketi*; hiç etiket çekilmediyse ad **hiç yazılmıyor** ve başlık
`Liste · 0/18` oluyor. Bu çerçeve tam da "henüz hiçbir şey olmamış" hâli.

### 1b · Çerçeve 04 — tildesiz kesin tutar

**Çizilen.** Özet kartı manşeti `642,50 TL` (36sp Fraunces), altında
`Geçen sefer 601,00 TL (18 gün önce)`. Aynı çerçevedeki snackbar ise
`~642 TL` yazıyor.

**Biçim kuralı** (gezinme sözleşmesi): *"kesin tutar: **Yok.** Uygulamada her
tutar gözlemden hesaplanır."* Tahmin biçimi `~642 TL` — tilde bitişik, kuruş
yazılmaz.

Yani aynı çerçevenin içinde iki biçim birden var: kart kuruşlu ve tildesiz,
snackbar tildeli ve kuruşsuz.

**Ayrıca:** çerçevenin altındaki açıklama *"Alışveriş kasada kapandı; parse
arkada bitti"* diyor — "parse" fiş döneminden kalma.

### 1c · Çerçeve 07 — katılma kodu ve Mağazalar satırı

**Çizilen.** Katılma kodu satırı `R4TB9C` + kopyalama ikonu taşıyor.
Mağazalar bölümü çiziliyor: `Takip edilen zincirler · İlk gözlemden
öğrenilecek · chevron_right`.

**Karar 24.** Katılma kodu satırı üretilmiş bir kod göstermiyor: **soluk** ve
*"Faz 7'de açılıyor"* yazıyor. Mağazalar bölümü boş hâlde **hiç
çizilmiyor**.
**Karar 23.** Zincirler satırından chevron kaldırıldı.
**İkonografi.** `content_copy` envanterden düştü.

Üçü de kodda uygulandı; çerçeve üçünün de öncesini gösteriyor.

**Ayrıca aynı çerçevede:** *"Her alışverişte aldığın ürünler birkaç
**geziden** sonra kendiliğinden burada birikir."* — eşik tablosu
*"Her zamankiler: en az **3 gezi**"* diyor. Sayıyı yazmak ister misiniz?

### 1d · Çerçeve 01 ve 06 — küçük iki uyumsuzluk

**01:** başlıkta `Son alışveriş: 3 gün önce · 642 TL`, gövdede
`Son alışveriş 3 gün önce, 642 TL.` — ikisi de tildesiz.

**06:** *"Tek alışverişte de gizli kalır — tek çubuklu grafik grafik
değildir."* Eşik tablosu Geçmiş grafiği için **3 gezi** diyor, yani iki
alışverişte de gizli kalıyor.

> **Soru 1.** Dört çerçeve de tazelenecek mi? Tazelenecekse bizden bir şey
> gerekiyor mu — yoksa kararlar ve eşik tablosu yeterli mi?

---

## 2. Atlasın kategori tablosunda "Ekran 4" artık iki şeyi birden anlatıyor

**Tasarımın verdiği.** Atlasın başındaki dört kategoriden biri:
*"**Ekran hiç açılmaz** — Ekran 3 ve Ekran 4. Boş bir kontrol listesi
kullanıcıya butonun değersiz olduğunu öğretir."*

**Gerçek.** Pivotla **Ekran 4 = Etiket çek** oldu (kamera + onay kartı) ve o
ekran *isteyerek açılan* bir ekran — liste başlığındaki kamera hedefinden,
her iki modda, gezi olmadan bile (karar 27). Hiç açılmayan şey, artık var
olmayan alışveriş-sonrası kontrol ekranıydı.

Karar 31 *"kategori değişmedi, ekran hâlâ hiç açılmıyor"* diyor ve bu çerçeve
04 için doğru — ama kategori tablosu "Ekran 4" diyerek şimdi kamerayı
işaret ediyor.

> **Soru 2.** Kategori satırı nasıl okunmalı? *"Ekran 3 ve alışveriş-sonrası
> özet"* gibi bir ifade mi, yoksa çerçeve 04'ün adı mı değişmeli
> (*"Alışverişi bitir"* → başka bir şey)?

---

## 3. İkon envanterinde olmayan üç ikon kodda kullanımda

**Tasarımın verdiği.** İkonografi envanteri 18'den 12'ye indi. Kalan 12:
`add` · `photo_camera` · `more_vert` · `arrow_back` · `close` · `search` ·
`check_circle` · `chevron_right` · `expand_more` · `logout` · `bolt` · `info`

**Gerçek.** Kod envanteri bu turda 23'ten 13'e indi. Aradaki fark üç ikon —
üçü de bugün kullanımda ve envanterde karşılığı yok:

| İkon | Nerede | Neden duruyor |
|---|---|---|
| `push_pin` | Ayarlar · sabit ürün satırı | "Her zamankiler"in görsel işareti; `check_circle` bu işi yapmıyor (o *seçili* demek) |
| `check` | Liste satırı işaretleme, Ekle sheet'i | `check_circle` çipte, çıplak `check` satırda kullanılıyor — envanter ikisini tek satırda topluyor olabilir |
| `content_paste` | Kurulum · "WhatsApp'tan listeni yapıştır" | Atlas çerçeve 08 bu ikonu çiziyor ama envanterde yok |

> **Soru 3.** Üçü 12'lik listeye mi giriyor (envanter 15 olur), yoksa
> yerlerine kalan 12'den biri mi geçecek? Özellikle `check` / `check_circle`
> ayrımı: ikisi de mi kalıyor?

---

## 4. İkon "A yolu" bugünkü kodla uygulanamıyor — teknik engel

Bu, cevabı bizden değil sizden gelmesi gereken tek **teknik** madde.

**Tasarımın verdiği.** A yolu: tek bir `IconDefaults` — **24dp, wght 300,
opsz 24, açık temada GRAD 0, karanlıkta GRAD 100.** *"Tek satırlık bir kod
değişikliği."*

**Gerçek.** `wght`, `opsz`, `GRAD`, `FILL` **Material Symbols değişken
fontunun eksenleri**. Uygulama ise `androidx.compose.material.icons`
kullanıyor: bunlar derlenmiş **statik `ImageVector`**'lar — eksen taşımıyorlar.
`Icons.Rounded.PhotoCamera`'ya `wght 300` verilemez; öyle bir API yok.

Üç yol var, üçü de bir iş kalemi:

| | Yol | Bedeli | Riski |
|---|---|---|---|
| **1** | Material Symbols **değişken fontunu bundle et**, ikonları `Text` olarak çiz | Font dosyası (~300 KB alt küme) + ikon çizim yolunun değişmesi | ⚠ Fraunces'te tam bu yol **reddedilmişti**: *"CMP'de `FontVariation.Settings` iOS'ta güvenilir değil ve sessizce varsayılana düşer"*. Aynı risk burada da geçerli — ikonlar iOS'ta yanlış ağırlıkta çizilebilir |
| **2** | **12 ikonu wght 300'de elle `ImageVector` olarak taşı** | Bir günlük iş, bağımlılık yok, APK etkisi birkaç KB | GRAD ekseni kaybolur: karanlık tema telafisi için ikinci bir set ya da opaklık ayarı gerekir |
| **3** | **B yoluna geç** (Phosphor) | Sizin de yazdığınız gibi 12 ikon elle taşınırsa bağımlılık bile gerekmez | Aynı GRAD sorunu; ama kimlik kazancı var |

Yol 2 ile yol 3 aslında **aynı mekanik iş** — fark yalnızca hangi setin
çizimlerini taşıdığımız. Yani "A'yı görüp sonra B'ye karar vermek" bu kodda
mümkün değil: her ikisi de aynı taşıma işini gerektiriyor.

> **Soru 4.** Bu bilgiyle A/B tercihi değişiyor mu? Eğer taşıma işi zaten
> yapılacaksa doğrudan Phosphor'a geçmek daha mı doğru?
>
> **Soru 5.** Karanlık tema telafisi GRAD olmadan nasıl yapılsın — ikinci bir
> ağırlık seti mi, yoksa ikon rengine hafif bir açma mı?

---

## Kapanmış olanlar — bilgi için

Üçüncü turdaki (`09-tasarima-sorular-3.md`) on maddenin **hepsi** karşılandı;
pivotla konusu kalmayanlar defterden çıktı, kalanlar 22–31 arası kararlara
dönüştü. Özellikle:

- **Fotoğrafın ömrü** — karar 29 bizim önerimizle aynı yere vardı
  (kayıtta silinir, hiçbir yüzey çizmez) ve gerekçe de aynı: etiket bir ödeme
  kanıtı değil, bir fiyatın okunduğu an. Açık kararlar listesinden düştü.
- **Etiket çekimine giriş** — karar 27 liste başlığına koydu; özet kartı
  seçeneği geziye bağlı olduğu için elendi. Kodda o düğme zaten kaldırılmıştı.
- **Geçmiş'te ne olacağı** — karar 30 "hiçbir şey" dedi; kod E8'de zaten
  gezi düzeyine inmişti.

Gezinme sözleşmesi tek başına E15'in bütün spesifikasyonunu kapattı: geri
sırası, on eşik, tarih merdiveni, hata yolları, 60 saniyelik mükerrer kuralı.
Kodlarken karar verilecek bir şey bırakmadı — teşekkürler.
