# Tasarıma sorular — altıncı tur

**16 Ağustos 2026.** Beşinci tur kapandı (karar 36), ayna tazelendi. Bu turun
ana maddesi **satır silme** — ve sormaya başlarken gördük ki siz zaten
cevaplamışsınız. Soru "nasıl silinsin" değil, **sizin iki dokümanınızın
birbiriyle çelişmesi**.

Biçim aynı: **tasarımın verdiği** → **gerçek** → **soru**.

---

## 1. Snackbar kaç yerde? Karar 8 ile gezinme sözleşmesi çelişiyor

### Tasarımın verdiği — iki ayrı yerde, iki ayrı sayı

**Gezinme sözleşmesi, "Jest" satırı:**

> Sağdan sola swipe listede satırı siler (geri alma "Alındı" bölümünde değil,
> **5 sn'lik snackbar**'da). Etiket akışında jest yok.

**Karar 8'in gerekçesi:**

> Snackbar aksiyon taşıyor ve uygulamada **tek bir yerde** kullanılıyor:
> alışveriş kendiliğinden kapandığında "Geçmiş'te gör".

Silme geri alması **ikinci** kullanım olur. "Tek bir yerde" cümlesi de o anda
yanlış hâle gelir.

### Gerçek

Uygulamada **hiç snackbar bileşeni yok**. `NeydiToast` var ve karar 8 gereği
bilerek aksiyonsuz — KDoc'unda *"ikisini aynı bileşene bindirmek, işaretlemede
snackbar yasağını da bulanıklaştırırdı"* yazıyor.

Yani silmeyi uygulamak, uygulamanın **ilk aksiyon taşıyan snackbar**'ını
yazmak demek. Bunu iki dokümanınız "olabilir mi" konusunda anlaşmazken yapmak
istemedik.

Silme yolunun geri kalanı zaten kararlı ve bizde hazır:
- **Jest:** sağdan sola swipe ✔ (sözleşme)
- **Animasyon:** 200 ms, yükseklik daralması, *"silinen satırın altındakiler
  tek hareketle toplanır"* ✔ (sözleşme)
- **Veri:** soft delete altyapısı var (`remove()` → `softDelete`), tombstone
  duruyor ✔

Eksik olan tek parça geri alma yüzeyi.

### Sorular

1. **Snackbar ikinci kullanımı alıyor mu?** Alıyorsa karar 8'in *"tek bir
   yerde"* cümlesi güncellenmeli. Almıyorsa geri alma nerede yaşıyor?
2. Snackbar'ın **metni ve aksiyon etiketi** ne? Yazım kuralınız *"fiil ve tek
   kelime: Kaydet, Bitir, Ekle, Sil"* diyor — geri alma için **"Geri al"** iki
   kelime. İstisna mı, yoksa başka bir sözcük mü?
3. **Alışveriş modunda swipe var mı?** Sözleşme yalnızca *"etiket akışında jest
   yok"* diyor. Alışveriş modunda satırlar işaretleniyor ve orada yanlışlıkla
   silmenin bedeli daha yüksek — reyondasın, geri alma penceresi 5 sn.
4. **"Alındı" bölümündeki satır silinebilir mi?** Sözleşme geri almanın "Alındı"
   bölümünde *olmadığını* söylüyor ama o bölümdeki satıra swipe atılıp
   atılamayacağını söylemiyor.

---

## 2. Beşinci turdan devreden iki küçük madde

**2a · Ekran 1'in beşinci çerçevesi tildesiz** (F11.17). Dört maket
`~642 TL` oldu, biri hâlâ *"Son alışveriş: bugün · 642,50 TL"* — tilde yok,
kuruş var. Türetilmiş bir tutar olduğu için biçim kuralına aykırı.

**2b · İkonografi karar 33'ü eski çiftiyle örnekliyor** (F11.18). Karar defteri
ilişkiyi doğru yazıyor (*"ikon yanındaki metinden bir kademe açık"*) ama
İkonografi aynı kuralı hâlâ *"metin `#E4D8C9`, ikon `#F5EDE6`"* diye
örnekliyor. Defteri esas aldık.

---

## 3. Bir not: sözleşme sandığımızdan çok şey cevaplıyor

Bu turu açarken "satır silme tasarlanmamış" sanıyorduk ve ROADMAP'te dört açık
tasarım sorusu olarak duruyordu. Gezinme sözleşmesini okuyunca üçünün
cevabının zaten yazılı olduğu görüldü.

Kendi payımıza ders: **soru sormadan önce sözleşmeyi okumak.** Aynayı
tazeledik ama içindekini taramamıştık.


---

# Ek — aynanın ikinci taraması

**17 Ağustos 2026.** Altıncı turun ilk yarısı (yukarıdaki üç madde) yazıldıktan sonra
tasarım dosyalarını bir kez daha, bu sefer "cevabı yok" iddiasını kanıtlamak için
taradık. Sekiz açık nokta çıktı: **dördü gerçekten sizin kararınız**, dördünü
**biz karara bağlayıp geçtik**, ve dört tanesinin de cevabı meğer zaten
yazılıymış — onları sormuyoruz, kendi dersimiz olarak en alta koyduk.

Biçim aynı: **tasarımın verdiği** → **gerçek** → **soru**.

---

## A · Tasarıma sorulacaklar

Bloke edenler başta. "Bloke" = bir sonraki iş kalemi bu cevap gelmeden
yazılamaz, ya da yazılırsa geri alınamaz veri üretir.

### A1 · Onay kartındaki "Marka" satırı hiçbir yüzeye açılmıyor — **bloke (E15)**

**Tasarımın verdiği.** Onay kartı kuralları marka için şunu diyor
(*Ekranlar 2-4*, satır 534):

> Etiket metninin ilk kelimesinden öneriliyor, **düzenlenebilir ve boş
> bırakılabilir**: manavda marka yok.

Karar 27 de öneriyi *"kesik çerçeveyle tahmin olduğunu söyler"* diye çiziyor ve
satır maketlerde `chevron_right` taşıyor.

**Gerçek.** "Düzenlenebilir" sözü bir yüzeye bağlanmıyor. Gezinme sözleşmesinin
ikinci kat yüzey listesinde de §08 bağlantı matrisinde de marka için bir yüzey
ya da kenar yok; §03 etiket akışı durum makinesinde marka yalnızca *"Fiyat,
ürün, marka dolar"* satırında geçiyor (satır 438), dokunulduğunda ne olduğu
hiçbir satırda yok; §07 ise metin girişini iki yere kapatıyor (*"başka hiçbir
yerde metin girişi yoktur — Ekle'deki arama alanı hariç"*, satır 478).

Bu bir kozmetik boşluk değil: E14 markayı etiket adının **ilk kelimesinden**
tahmin ediyor, yani `DST`, `YGRT` gibi OCR artıkları normal çıktı. Karar 26
satırın kimliğini *"market + marka çifti"* yaptığı için düzeltme yolu olmayan
her artık doğrudan kimliğe yazılır: Ekran 5'in "Nerede ucuz" listesi
**"BİM · DST · 100,00 TL"** diye çizilir ve aynı marka iki ayrı satıra bölünür.
Markasız gözlem (manav) de yazılamaz.

**Neden bloke.** E15 onay kartı bu cevap olmadan çizilemez — chevron'un arkası
bilinmeden satır ya yanlış çizilir ya dokunulamaz kalır. Daha ağırı: bu satır
kimliğe yazdığı için yanlış çekilmiş her gözlem, sonradan düzeltilemeyen bir
kayıt olur.

**Soru.** Onay kartındaki **Marka** satırına dokununca ne oluyor:
**(a)** market seçicinin ikizi, klavyesiz bir çip sheet'i mi açılıyor (bu ürün
için daha önce görülmüş markalar + "Marka yok"), **(b)** satır tek dokunuşla
öneriyi temizleyen bir anahtar mı (dokun = boşalt), yoksa **(c)** öneri hiç
dokunulamaz mı ve `chevron_right` maket artığı mı?

---

### A2 · "+ Yeni market" satırının arkasında yüzey yok — **bloke (E15)**

**Tasarımın verdiği.** Market seçicinin son satırı çizilmiş
(*Ekranlar 2-4*, satır 407): `add` ikonu + **"Yeni market"**, kiremit renginde.
İkonografi de bunu envanterine almış (satır 261): *"Tek ekleme ikonu; \"+ Yeni
market\" de aynısını kullanır"*. Karar 22 yedi zinciri tohumluyor ve
*"gerekirse \"Yeni market\" ile ekler"* diyor.

**Gerçek.** Adın **nerede** yazıldığı çizilmemiş, ve yazılabileceği her yer
sözleşmenin bir değişmezine çarpıyor:

- §07 *Tek klavye* (satır 478): *"başka hiçbir yerde metin girişi yoktur —
  Ekle'deki arama alanı hariç"* → üçüncü bir metin alanı bugünkü metne aykırı.
- §14 (satır 587): *"Uygulamada tek bir modal dialog yok"* → ayrı bir pencere de
  aykırı.
- Ama §02 geri tablosunda **"Ürün / market seçici · Arama metni atılır"**
  satırı duruyor (satır 412) — market seçicide de bir arama metni varmış gibi
  okunuyor. Oysa Ekranlar 2-4'ün market maketinde arama alanı çizilmemiş.
- Karar 22 adın etiketteki gibi kalmasını istiyor (AKYURT, FiLE) → serbest
  metin, ve İ/ı köprüsü riski buraya düşüyor.

**Neden bloke.** E13 yalnızca yedi zinciri tohumluyor; sekizinci market (yerel
bakkal, pazar, indirim marketi) tam olarak bu satırın arkasında. Satır
yazılmazsa o gözlemler ya yanlış zincire yazılır ya hiç çekilmez — ikisi de
düzeltilemez kayıt. Yazılması için de "tek klavye" ya da "sıfır dialog"
değişmezlerinden biri delinmek zorunda; bu tek başına kod tarafının alacağı
karar değil.

**Soru.** Market seçici, ürün seçicinin aynısını mı alıyor — sheet'in başında
arama/filtre alanı, yazılan metin yedi zincirden hiçbirine uymayınca
**"+ Yeni market «AKYURT»"** tek dokunuşla ekliyor (tıpkı §03'teki *"Katalogda
yoksa \"Yeni ürün olarak ekle\" tek dokunuş"* gibi) ve öyleyse *Tek klavye*
değişmezinin istisnası **"arama alanları"** diye mi düzeltiliyor — ürün
seçicinin arama alanı da bugün o listede yazılı değil —, değilse "+ Yeni
market" hangi yüzeye gidiyor?
*(Aynı cevabın içinde tek kelime: kullanıcı zaten var olan bir adı — "BIM" —
ikinci kez yazarsa **sessizce var olan zincire mi bağlanıyor**, yoksa yeni satır
mı açılıyor?)*

---

### A3 · Satır silmenin jest olmayan eşi hiçbir yerde yok — **bloke (F10.9)**

*(Bu, 1. maddedeki snackbar sorusundan ayrı ve ikinci bir engel: orada geri
almanın yüzeyi eksikti, burada silmenin kendisinin ikinci kapısı eksik.)*

**Tasarımın verdiği.** İki dosya aynı cümleyi kuruyor.
*Tasarım Sistemi*, satır 332:

> Sağ kenardan swipe = sil; her yıkıcı işlem için **taşma menüsünde jest olmayan
> bir yol** da var.

*Compose Spec* §04, satır 140: *"...her yıkıcı işlemin taşma menüsünde jest
olmayan bir eşi vardır."*

**Gerçek.** O eş envanterde yok, ve mekanik olarak olamaz da. İki moddaki
`more_vert` envanteri sabit — plan modu: *Reyonlardan ekle / Geçmiş / Ayarlar*;
alışveriş modu: tek madde *"Alışverişi bırak"*. Silme maddesi hiçbirinde yok.
Taşıyamaz da: **menü ekran düzeyinde yaşıyor, silme ise satır düzeyinde bir iş**
— menü hangi satırda olduğumuzu bilmiyor. Ürün Detayı sheet'inde de silme yok.
TalkBack ve switch access swipe üretemez; sözleşme bugünkü hâliyle kabul
edilirse silme bu kullanıcılar için hiç var olmayan bir özellik olur.

**Neden bloke.** Vaadin kendisi bugünkü menü tasarımıyla tutulamıyor; silme bu
hâliyle kodlanırsa ya yazılı vaadi ya erişilebilirliği ihlal ederek kodlanır.

**Soru.** Swipe üretemeyen kullanıcı bir satırı nereden siliyor:
**(a)** satıra uzun dokunuşla açılan Ürün Detayı'na eklenen, `error` renginde
tek bir **"Listeden çıkar"** satırı mı (sözleşmenin kenar listesinde geçen ama
Ekran 5 maketinde çizilmemiş "Listeye ekle" satırının karşılığı olarak),
**(b)** görsel yüzeyi olmayan, yalnızca ekran okuyucuya açılan satır içi bir
erişilebilirlik eylemi mi — bu durumda *"taşma menüsünde"* cümlesi düzeltilmeli
—, yoksa **(c)** başka bir yer mi; ve bu eş **alışveriş modunda da var mı**?

---

### A4 · "Başka markette ucuz" çipinin eşiği ve önceliği yazılmamış — **önemli (E16)**

**Tasarımın verdiği.** *Tasarım Sistemi*, satır 379: amber bir çip,
**"Başka markette ucuz · liste başına en fazla 3"**, örnek metni "A101'de 89,00".

**Gerçek.** İki kural eksik ve ikisi de kodun kendi başına uyduramayacağı türden.

*Eşik yok.* Çipi ne hak ettiriyor, ve karşı gözlem kaç günden eskiyse artık çip
çizdirmiyor — yazılı değil. Ekran 5'te bayatlığı gizlemek yerine ifşa
ediyorsunuz (*"14:20 itibarıyla"*, *"Sütaş · 2 hafta önce"*), ama 24dp'lik satır
çipinde tarih yazacak yer yok; orada tek çare eşik, yoksa çip üç ay önceki bir
etiketi bugünün iddiası gibi sunar.

*Çakışma kuralı yok.* İkinci satır için *"şunlardan yalnızca biri, bu öncelikle"*
listesinin 1. maddesi "fiyat ipucu" — hem trend (38,50 → 42,00 + delta +
sparkline) hem bu çip o tanıma giriyor. Ambalaj küçülmesi için *"trend
bastırılır"* diye açık hüküm var, bu çip için karşılığı yok; Ekran 1 maketinde
ikisi hiç aynı satırda kurulmadığı için durum sınanmamış.

Kod tarafı: E16'nın planlanan sorgusu (son + önceki gözlem için iki correlated
subselect) bu çipi **hesaplayamıyor** — başka marketlerin en ucuzunu hiç
okumuyor; `PriceHint` de bugün dört dal taşıyor (`None/Single/Trend/PackChanged`,
`RowModel.kt`), beşinci dal yok. E16 bugünkü tarifiyle bitirilirse tasarımın
çizdiği bir bileşen sessizce hiç doğmaz.

**Soru.** Çip için iki sayı verin: **(1) eşik** — karşı gözlemin en az ne kadar
ucuz olması gerekir (mutlak TL mi, yüzde mi, ikisi birden mi) ve kaç günden eski
karşı gözlem çip çizdirmez; **(2) çakışma** — aynı satırda hem trend hem bu çip
doğruysa hangisi çizilir?
*(Not: "liste başına en fazla 3, mutlak TL tasarrufuna göre sıralı" kuralını
zaten kaydettik; onu sormuyoruz, yalnızca yanlışsa düzeltin.)*

---

## B · Biz karar verip geçiyoruz

Bunlar soru değil, **bildirim**. Dördü de tasarım itiraz ederse geri alınır.

Üçünün gerekçesi ortak ve sözleşmenin kendi açılış cümlesinden geliyor
(*Gezinme Sözleşmesi*, satır 26): *"Ekran çizimleri **neyin göründüğünü**
söylüyor; bu dosya **ne olduğunu** söylüyor."* Bir yüzeyin doğup doğmadığı "ne
olduğu"dur — orada sözleşme kazanır. Bir dolgunun rengi ise tam olarak "neyin
göründüğü"dür — orada maketler kazanır.

### B1 · Birincil buton dolgusu: maketleri esas alıyoruz

**Çelişki.** §11 kiremit `#B34418` için *"never: Dolgulu buton zemini"* diyor ve
yeşili *"birincil butonun tek rengi"* ilan ediyor. Ama dört maketin tamamı altı
ayrı dolgulu kiremit buton çiziyor ("Alışverişe çıkıyorum", `add` FAB, "Ekle (4)",
"Yeni hane oluştur", "Devam (9 seçildi)", "Listeme geç"), Compose Spec'te sırf
dolgu için var olan bir `onPrimary` rolü duruyor (*"primary üzerindeki metin ve
ikon"*), dolgulu yeşil `#3F6B54` yalnızca "Bitir" ve "Kaydet"te görünüyor, ve
Compose Spec'in *"birincil buton dolgusu"* dediği üçüncü yeşil `#2E6B45` hiçbir
makette buton zemini değil.

**Kararımız.** Maketler geçerli:
- **`#B34418` dolgu** = ileri götüren birincil aksiyon (üstünde `onPrimary`),
- **`#3F6B54` dolgu** = onay/bitirme aksiyonu (Bitir, Kaydet),
- **`#8A7666` kenarlık** = üçüncü varyant,
- **`#2E6B45`** buton dolgusu olarak hiç kullanılmıyor.

**Gerekçe.** Renk "neyin göründüğü" tarafında; altı çizilmiş örnek tek satırlık
bir "never" hükmünden ağır basıyor. Ayrıca `onPrimary` rolünün varlığı dolgulu
birincil butonun var olduğunu ispatlıyor — aksi hâlde o rol boşta kalırdı.
§11'in kiremit "never" satırı ile `success`'in "birincil buton dolgusu" ibaresini
düzeltilecek maket artığı sayıyoruz. İtiraz gelirse yeni dolgunun `#3F6B54` mi
`#2E6B45` mi olduğunu ve "Alışverişe çıkıyorum"un dolgusuz bağlantıya inip
inmediğini o zaman soracağız.

### B2 · Fiyat yönü kırmızı/yeşil kalıyor

**Çelişki.** §11 kırmızı `#B3261E` için *"Asla: hata mesajı, doğrulama, fiyat
artışı"* diyor. Buna karşılık Compose Spec §01 rol tablosu (satır 435)
`error`'u *"Fiyat artışı oku ve yıkıcı işlem metni"* diye tanımlıyor,
`handoff/tokens.json`'ın semantic bloğu *"priceUp: error, priceDown: success"*
yazıyor, ve Ekran 1 ile Tasarım Sistemi maketleri delta çipini
`#B3261E/#F7E4E2` (`arrow_upward`) ve `#2E6B45/#E3EFE7` (`arrow_downward`)
olarak çiziyor.

**Kararımız.** Delta çipi ve trend oku **kırmızı/yeşil kalıyor**; §11'in kırmızı
"asla" satırındaki *"fiyat artışı"* ibaresini maket artığı sayıyoruz.

**Gerekçe.** Yine "neyin göründüğü" tarafı; üstelik burada üç bağımsız kaynak
(rol tablosu, `tokens.json`, iki maket) tek bir satıra karşı — ve `tokens.json`
makine-okunur kaynak, kod doğrudan onu okuyor. Yeşilin *"iyi fiyat anlamı"*
yasağını da **rozetle değil sırayla anlatma** kuralı olarak okuyoruz: ucuzluğu
rozetleyen bileşen "başka markette ucuz" çipi ve o amber kalıyor, dolayısıyla
yasak zaten karşılanmış; fiyat düşüşü okunu bu yasağın dışında sayıyoruz.

### B3 · "Nerede ucuz" ambalaj boyunu **filtre** olarak okuyoruz

**Boşluk.** Ekran 5'in "9 gözlem · ambalaj küçülmesi" çerçevesinde başlık
*"14:20 itibarıyla · 4 L"* diyor ve çizilen üç satırın üçü de 4 L; ama örnekte
yalnızca eski boydan (5 L) görülmüş bir market bulunmadığı için "· 4 L"nin filtre
mi yoksa bağlam damgası mı olduğu anlaşılmıyor.

**Kararımız.** Filtre. O üründe yalnızca **eski boydan** gözlemi olan market
listeden **düşer**; geriye bölümün kendi eşiğinin altında market kalırsa bölüm
**hiç çizilmez**. Grafiğin "1 ay / 6 ay / 1 yıl" aralık çipleri bu bölümü
daraltmaz — çipler grafiğin denetimi, bölümün zaman ifadesi başlıktaki
"… itibarıyla" damgasıdır.

**Gerekçe.** Tasarımın kendi ilkesi: *"ambalaj boyu değiştiyse trend
gösterilmez"* — iki farklı boyu tek listede fiyata göre sıralamak da aynı
türden yanlış bilgi olurdu, üstelik *"boş bölüm çizilmez"* değişmezi eksik
kalan hâlin ne olacağını zaten söylüyor. Bu yolu seçince karar 26'nın
*"sıralama fiyata göre"* cümlesinin paket fiyatını mı birim fiyatını mı
kastettiği sorusu kendiliğinden düşüyor: tek boy kaldığı için ikisi aynı sırayı
veriyor.

### B4 · Tutarı hesaplanamayan özet kartı hiç çizilmiyor

**Boşluk.** Alışveriş sonrası özet kartı yalnızca tutarı bilinen ve öncesinde bir
gezi olan hâlde çizilmiş; ~ tutarı hesaplanamadığında (o gezide fiyatı bilinen
ürün yoksa) kartın ne olacağı yazılı değil.

**Kararımız.** 36sp Fraunces manşeti düşüyorsa **kart hiç görünmüyor**;
manşetin yerine hiçbir şey konmuyor.

**Gerekçe.** *"Boş bölüm çizilmez, boş ekran açılmaz"* değişmezi ile *"Yanlış
bir şey göstermektense hiçbir şey gösterme"* ilkesi; kartın var oluş sebebi
manşet, o düşünce geriye kalan iki satır kart açmayı hak etmiyor. İlk gezide
"Geçen sefer" ve gözlemsiz gezide "En çok artan" satırlarını da eşik tablosunun
delta kuralına uyarak (*"Çip yok; 'ilk gözlem' ibaresi de yok"*) sessizce
düşürüyoruz.

---

## C · Zaten cevaplıymış — bu bölüm tasarıma gitmiyor

Dört madde daha soracaktık; dördünün de cevabı yazılıymış. Kayda geçiyoruz ki
aynı hatayı yedinci turda tekrarlamayalım.

### C1 · Kartın "Vazgeç"i kamerada değil, kartta

§03 durum makinesi kameranın çıkışını ve kartın Vazgeç'ini **ayrı satırlarda**
tanımlıyor ve kamera satırında Vazgeç yok
(`docs/tasarim/Neydi - Gezinme Sozlesmesi.dc.html`, satır 442 ve 444):

> `{ state: "Çıkış", input: "✕ / geri (kart kapalıyken)", result: "Liste'ye dönülür; market seçimi bir sonraki çekim için hatırlanır.", edge: "Hiç gözlem çekilmeden çıkılırsa hiçbir iz kalmaz." }`
> `{ state: "Kart dolu", input: "Vazgeç / geri", result: "Gözlem yazılmaz, fotoğraf silinir, kamera geri gelir.", edge: "Onay istenmez — çekim ucuz, tekrarı bir dokunuş." }`

§02 geri tablosu da kamera satırında yalnızca iki kapı sayıyor (satır 410-411),
§01 ise kartın kendi iptal kontrolünü taşımak zorunda olduğunu söylüyor (satır
402): *"Kameranın üstünde yaşayan tek yüzey. Dışına dokunmak kapatmaz."*
Ekranlar 2-4'teki kamera alt şeridindeki "Vazgeç" **maket artığı** ve karar
27'nin *"aynı işe iki kapı açıp ikisini de zayıflatırdı"* gerekçesine de aykırı.

### C2 · "Tahmini sepet" eşiği: 3 fiyatı bilinen ürün, ikili davranış

Sözleşme kendi üstünlüğünü açılış paragrafında ilan ediyor (satır 26) ve §12
alt başlığı bunu tekrarlıyor: *"Veri eşikleri — Bir yüzeyin ne zaman doğduğu, ne
zaman sustuğu."* Kural (satır 570):
`{ name: "Sepet tahmini", min: "3 fiyatı bilinen ürün", below: "Satır hiç görünmez" }`

**%60/%40/%30 notu tasarımın kararı değil, bizim kendi brief'imizin yankısı** —
`docs/01-claude-design-prompt.md` satır 153'teki cümle Ekran 1'in not kutusuna
yapışmış. Düşük güven işareti diye bir hâl de yok: tilde koşulsuz (*"türetilen
her tutar tilde alır"*, §10 ve §14), %40 opaklık tasarım sisteminin opaklık
merdiveninde hiç geçmiyor (%38 devre dışı, %60 ikinci satır, %70 sabit), ve
eşik altı çerçeveler ("1 Gün1", "1 Atlandı", "1 Döngü", "1 Çevrimdışı") satırı
soluk değil **hiç** çizmiyor. `BasketAndSummary.kt`'nin bugünkü hâli
(`pricedCount < MIN_PRICED_ITEMS → return`) sözleşmeyle birebir doğru.

### C3 · "Yükleniyor karesi" bilerek çizilmemiş

`Neydi - Gezinme Sozlesmesi.dc.html`:529 →
`{ name: "Bekleme", dur: "—", curve: "İskelet, dönen çark yok", rule: "1,5 sn'yi geçen tek iş OCR; onun da yeri kartın kendisi" }`
Satır 583: *"Kurulum dışında hiçbir ekran tam ekran yükleme göstermez."*
Satır 588: *"Boş bölüm çizilmez, boş ekran açılmaz."*
`docs/01-claude-design-prompt.md`:37: *"Yanlış bir şey göstermektense hiçbir şey
gösterme."*

Ekran 3'ün "Eksik olabilir (0)" ara karesi de boşluk değil, **ihlal**:
`Neydi - Bos Durumlar.dc.html`:32 *"Ekran hiç açılmaz"*; aynı dosya:118
*"ekran atlanır, doğrudan alışveriş moduna girilir, 2 saniyelik toast çıkar"*;
sözleşme:385 ve :573 aynı şeyi iki kez daha yazıyor.

### C4 · "Hepsini almadım" ekranı tasarlanmamış değil, **çıkarılmış**

`Neydi - Bos Durumlar.dc.html` çerçeve 04'ün başlığı birebir *"Alışveriş
kapanışı · açılmaz"* (satır 123); gerekçe (satır 139): *"Kontrol edilecek bir
belge yok; alışveriş kapanınca liste kendiliğinden temizlenir ve ekran hiç
açılmaz."* Karar 31 pivot turunda bunu özellikle yeniden ele alıp *"ekran hâlâ
hiç açılmıyor"* diye teyit etmiş. §08 kenar matrisi Bitir'i doğrudan plan moduna
bağlıyor, ara destinasyon yok.

İşaretlenmemiş satırların cevabı da var, sadece başka zamanda: 12/18 bitince
kalan 6 satır bir düzeltme ekranında sorulmuyor, **bir sonraki gezinin başında**
"Eksik olabilir" ekranında *"Geçen sefer unuttun · listede vardı, alınmadı"*
diye geri geliyor.

**Kod sonucu:** `FinishShoppingScreen` sözleşmenin karşılığı olmayan bir yüzey —
silinmesi gerekiyor. Karar 31 `docs/11-tasarim-kararlari.md` satır 51'de
işaretliymiş ama bu sonuca hiç bağlanmamış.

---

## Kapsam notu

Bu ek, otuz altı ham bulgunun elenmesinden çıktı. **Yirmi dördü
düşük öncelikli olduğu için hiç incelenmedi** — temiz oldukları
anlamına gelmiyor, yalnızca bakılmadı. İncelenen on ikinin dördü
gerçek soru, dördünü biz karara bağladık, dördünün cevabı zaten
yazılıymış.
