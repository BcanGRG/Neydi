# Tasarıma sorular — üçüncü tur

**16 Ağustos 2026.** Bu tur öncekilerden bir yönüyle farklı: sorulardan biri
**bir kararın öncülünü** yıkıyor. Karar 4 hâlâ doğru bir karar ama dayandığı
ölçüm yanlış çıktı, ve o karara bağlı üç madde birden onunla birlikte oynuyor.

Biçim aynı: **tasarımın verdiği** → **gerçek** → **karar gereken soru**.

Sıra önem sırası, birikme sırası değil. 1. madde 2, 3 ve 4'ü belirliyor; onu
okumadan onlara karar verilemez. **5. madde bu turda yeni** ve tasarımda hiç
karşılığı olmayan tek eksik: çekilen fotoğraf kullanıcıya hiç gösterilmiyor.

**10. madde sona eklendi ama küçük değil:** 1. maddenin ölçümü yapılırken çıktı
ve 2. maddeyi doğrudan etkiliyor — Ekran 4 hakkında soru sormadan önce o
ekranın kullanıcıya hiç görünmediğini bilmek gerekiyor.

---

## 1. Parça parça çekim, beklenen yol olmaktan çıktı — karar 4'ün öncülü değişti

**Bu turun en büyük maddesi. 2, 3 ve 4 buna bağlı.**

**Tasarımın verdiği.** Karar 4: *"Parça normal bir hal, hata hali değil."*
Gerekçesi de yazılı: *"60 kalemlik bir fiş tek kareye sığdırılınca satır başına
4,7 piksel düşüyor — çok kare fiziksel bir zorunluluk, kolaylık değil."*

**Gerçek — ve o ölçüm eksikti.** 4,7 piksel, fotoğraf OCR'a verilmeden önce
**bizim** uzun kenarı 2576 piksele indirmemizden sonraki değerdi. Sensör ~4000
veriyor. Yani kaybın büyük kısmı fizik değil, kendi koyduğumuz sınırdı.

Fotoğraf artık **içeride** üst üste binen şeritlere bölünüp her şerit kendi
çözünürlüğünde okunuyor; kullanıcıdan dört ayrı kare istemeden aynı etki
alınıyor.

**Kullanıcı bunu üç kez, açıkça istedi:** *"parça parça çekmeye çok karşıyım,
tekte çekip içeride bizim ayarlamamız lazım"*, *"ard arda doğru satırı
ayarlamak çok zahmetli ve tutarlı olmuyor"*.

**Ölçüm — cihazda, gerçek fişle.** Karar 4'ün beklediği sayı bu:

| | tek karede okunan kalem |
|---|---|
| eski akış (parça parça) | 60 kalemin 42'si **iki parçada birden**, 6'sı **hiç** okunmadı |
| tek çekim, ilk hali | 39 kalemin **0–1'i** |
| tek çekim, bugün | 39 kalemin **31'i** |

Yani cevap **"çoğu" — ama "hepsi" değil.** Karar 4'ün *"parça normal bir hal"*
öncülü artık doğru değil, ama *"parça yok"* da doğru değil. Parça **yok
olmadı, istisnaya indi:** tek kare okunamazsa, ya da eksik kalem tespit
edilirse hâlâ gerekiyor. Uygulama eksiği zaten adıyla söylüyor: *"17 kalem
eksik görünüyor · sıra 15, 38, 39, 40, 42, 43…"*.

Fiş Kontrol'deki bölüm başlıkları ve Geçmiş'teki girintili parça satırları
duruyor — yani ekranların parça dili silinmedi, yalnızca **ne zaman**
görüneceği değişti.

**Ölçüldü — ve tek kare fiziksel bir tavana çarpıyor.** *(16 Ağustos, cihazda)*

Tarayıcının verdiği sayfa **693 × 4023 piksel**. Aynı cihazın ana kamerası
4032 × 3024 (12 MP). Fişin oranı 1:5,81 ve bu oranda bir nesne kadrajın uzun
kenarını doldurduğunda genişliği en çok **4032 ÷ 5,81 = 694 piksel**
olabiliyor. Ölçülen 693.

Yani tarayıcı çıktısı **sensörün verebileceğinin tamamı** — kırpma dışında
hiçbir şey atılmıyor. Bu iki şeyi birden söylüyor:

1. **Daha büyük bir görüntü alınamaz.** Çözünürlük tarayıcının mod seçimiyle
   değil, fişin kadrajdaki oranıyla sınırlı. (F4.19'daki *"tarayıcı 661 KB,
   ham karemiz 1,5 MB"* gözlemi çözünürlük farkı değil, atılan **arka plan**
   farkıymış — yanlış okumuşuz.)
2. **Bir satırda ~45 karakter varsa karakter başına ~15 piksel düşüyor.** Daha
   fazlası ancak fişi uzunluğu boyunca **iki kareye bölerek** alınabilir.

Karar 4'ün *"çok kare fiziksel bir zorunluluk"* sezgisi bu yönüyle doğruymuş —
sayısı (4,7 piksel) yanlıştı ama tavan gerçek. Eksik kalan 8 kalemin sebebinin
bu tavan mı yoksa ayrıştırıcı mı olduğu **henüz ayrıştırılmadı**; ikisi de
mümkün. Ama tavanın varlığı, parçanın istisna olarak **kalması** gerektiğini
söylüyor: bir tercih değil, tavana çarpıldığında kalan tek yol.

**Hâlâ açık olan tek şey çözünürlük değil, temizlik.** Tarayıcının bugünkü
modu (FULL) leke ve parmak silmek için bir doldurma modeli çalıştırıyor ve
termal fişin soluk glifleri o modele lekeye benziyor olabilir. Daha az müdahale
eden BASE modu **aynı çözünürlüğü** verecek, belki daha bütün glifler. Bu da
1. maddenin cevabının şeklini değiştirmez.

**Sorular.**
1. Karar 4'ün *"parça normal bir hal"* öncülü nasıl güncellensin? Önerimiz
   *"istisnai hal, ama hata değil"* — yani kullanıcı parçaya düştüğünde
   yanlış bir şey yaptığını **hissetmemeli**, ama oraya da varsayılan olarak
   yönlendirilmemeli.
2. İstisna **ne zaman** görünür olsun? Bugün iki tetikleyici var: (a) hiç
   okunamadı, (b) eksik kalem tespit edildi. İkisi aynı dili mi konuşmalı?
3. Ekran 6'daki parça satırları istisna hâline göre mi çizilmeli — örneğin
   yalnızca bir parça sorunluysa?

## 2. Kamera rehberi artık ikinci kare istemiyor

**Tasarımın verdiği.** Ekran 4'ün rehber metni: *"Fişin tamamı kadraja girsin.
Uzunsa 2 kare çek."* Altında da *"1. kare"* sayacı ve *"Bitti"*.

**Gerçek.** İkinci kare artık *beklenen* şey değil, yalnızca okuma
yetmediğinde başvurulan yol (bkz. 1. madde). Metin *"Fişin tamamı kadraja
girsin."* olarak kısaldı.

Bir de tasarımın hesaba katmadığı bir şey oldu: çerçeve rehberi fişin oranına
getirilince (1:4–1:12, kare değil) kullanıcı telefonu **yan tutmaya** başladı.
Yani "dik tutulmuş kare" varsayımı artık geçerli değil.

**Sorular.**
1. Sayaç (*"1. kare"*) ve *"Bitti"* tek çekim varsayılan olunca hâlâ doğru
   kelimeler mi? Tek kare çekip biten bir akışta *"Bitti"* yerine *"Kullan"* ya
   da doğrudan Fiş Kontrol'e geçiş daha mı doğru?
2. Kullanıcı telefonu yan tutuyorsa rehber metni ve sayaç **nasıl
   yerleşmeli**? Bugün ikisi de dik yerleşim varsayıyor.

## 3. Tek akışta "Parça fişi · toplam son parçada" çipi artık ekranı anlatmıyor

**Tasarımın verdiği.** Çok parçalı çerçevede çip: *"Fişin tamamı tutuyor"* —
yani aritmetik tuttuğunda ne yazacağı belli.

**Gerçek.** Üçüncü hal — **hiçbir parçanın toplamı okunamadı** — hâlâ eski
cümleyi taşıyor: *"Parça fişi · toplam son parçada"*. Bu cümle karar 4'ten önce
doğruydu, çünkü ekran o zaman tek bir parçayı gösteriyordu. Artık ekran fişin
tamamını gösteriyor, yani *"bu bir parça fişi"* demek ekranın kendisiyle
çelişiyor.

Cihazda görüldü: iki parçalı AKYURT fişinde ikinci parça hiç okunamadı, manşet
`~229,05 TL` (satırlardan hesaplandı) ama çip hâlâ *"toplam son parçada"*
diyor — oysa son parça okunamadı ve kullanıcının yapması gereken şey onu
yeniden çekmek.

**Soru.** Bu hâlde çip ne demeli? *"Son parça okunamadı"* gibi eksiği söyleyen
bir şey mi, yoksa karar 15'in çipiyle (*"Satırlardan hesaplandı"*) mi
birleşmeli?

## 4. "Bu fişin devamını çek" listenin dibinde kalıyor

**Tasarımın verdiği.** Satır, kaydırılan içeriğin **sonunda** duruyor — son
parçanın satırlarından hemen sonra.

**Gerçek.** Tasarımın çerçevesinde altı satır var; gerçek fişte altmış olabilir.
Cihazda satır listesi kaydırılmadan bu satır görünmüyor — yani *"devamını çek"*
tam da uzun fişte, en çok gerektiği anda gizli kalıyor.

1. madde bunu **daha da keskinleştiriyor:** artık bu satıra ancak istisnai bir
durumda ihtiyaç var, ama ihtiyaç duyulduğunda **kesinlikle** görünmesi
gerekiyor.

**Soru.** Satır listenin sonunda mı kalsın (tasarımdaki yer), yoksa alt buton
bloğuna mı sabitlensin? Sabitlenirse *"Onayla ve kaydet"* ile aynı görsel
ağırlığa gelmemesi gerekir.

## 5. Fiş Kontrol çekilen fotoğrafı hiç göstermiyor

**Tasarımın verdiği.** Fiş Kontrol çerçevelerinde manşet, aritmetik çipi, satır
listesi ve butonlar var — **fotoğrafın kendisi hiçbir çerçevede yok**.

**Gerçek.** Fotoğraf çekiliyor, cihazda saklanıyor, OCR'a veriliyor, kişisel
veri diye işaretleniyor — ve kullanıcı onu **bir daha hiç görmüyor**.
Uygulamada `Receipt.imagePath`'i çizen tek bir yüzey yok; yol yalnızca OCR'a ve
veri silmeye gidiyor.

Bu, ekranın kendi işiyle çelişiyor. Fiş Kontrol'ün işi kullanıcıya *"okuduğum
bu, doğru mu?"* diye sormak; ama doğrulama karşılaştırılacak bir **asıl**
olmadan yapılamıyor. Kullanıcı elindeki kâğıda bakmak zorunda kalıyor — ve
kâğıt çoğu zaman çoktan çöpte.

Bugün cevabı yalnızca fotoğrafta olan dört soru var, hepsi cihazda görüldü:

- 39 kalemin 31'i okundu, **8'i eksik** — *hangileri?*
- Adı okunamayan satır (6. madde) — *bu ne ürünüydü?*
- Toplam okunamadı (3. madde) — *fişte basılı toplam kaçtı?*
- *"Başka yönde oku"* — *hangi yön doğru?*

**Sorular.**
1. Fotoğraf ekranda nerede dursun? Manşetin altında küçük bir önizleme,
   dokununca tam ekran mı — yoksa satır listesiyle birlikte kaydırılan bir
   panel mi?
2. Her zaman mı görünsün, yoksa yalnızca sorunlu hâllerde mi (eksik kalem,
   okunamayan satır, tutmayan aritmetik)?
3. Tam ekran görünümde **yakınlaştırma şart mı?** Fiş 693 × 4023 piksel; telefon
   ekranına sığdırıldığında satırlar okunmuyor. Yakınlaştırma varsa, uzun fişte
   kullanıcının aradığı satırı bulması ayrı bir tasarım işi.
4. Satırla fotoğrafı **eşlemek** mümkün mü — bir satıra dokununca fotoğrafta o
   satırın olduğu yere gitmek? (Teknik olarak elimizde satırın fotoğraftaki
   koordinatı var; bu bir tasarım tercihi, bir kısıt değil.)

**Gizlilik engel değil:** fotoğraf zaten cihazda ve varsayılan olarak dışarı
çıkmıyor (`syncPhotos = false`). Gösterilecek şey kullanıcının kendi verisi,
kendi cihazında.

---

## 6. Barkodu da olmayan satır ne yazacak?

**Karar 14'ün verdiği.** Adı okunamayan satırın başlığında **barkod** duruyor.

**Gerçek.** Tartı satırlarında barkod yok. Cihazdaki AKYURT fişinde
`9 2902925 1,206 Kg 109,00 %01 131,45` satırı: sekiz haneli bir dizi var ama o
barkod değil, tartı etiketinin kendi kodu — ve satırda ürün adı hiç geçmiyor.

Bu satır hâlâ **"Eşleşmedi"** yazıyor, yani karar 14'ün kapattığı sorunun
küçük bir kalıntısı duruyor: yedi satırdan biri hâlâ hata mesajını ürün adının
yerine koyuyor.

**Soru.** Ne barkod ne ad okunabilen satırın başlığı ne olmalı? Ham metnin
kendisi mi, *"Tartı ürünü"* gibi bir tür adı mı, yoksa satır bambaşka mı
çizilmeli (örneğin yalnız tutar ve *"dokun, ürünü seç"*)?

## 7. Zincir adı büyük/küçük harf düzeni

**Tasarımın verdiği.** Örneklerde `File` ve `Migros` yazıyor — yani başlık
düzeni (ilk harf büyük, gerisi küçük).

**Gerçek.** Bu projede locale'siz harf dönüşümü yasak ve sebebi ölçülmüş:
`"İNCİR".lowercase()` beş harf yerine yedi kod noktası üretiyor. Türkçe İ/ı
kuralı için doğru dönüşüm locale gerektiriyor, o da Compose Multiplatform'un
ortak katmanında yok.

Şu an fişin bastığı hâl olduğu gibi gösteriliyor: `AKYURT`, `FiLE`, `BIM`.

**Soru.** Zincir adı hep büyük harf mi kalsın (fişin yazdığı hâl, dürüst ama
tasarımdan sapma), yoksa başlık düzeni tasarım açısından zorunlu mu? Zorunluysa
her platform için ayrı bir `lowercase(Locale)` köprüsü yazılacak — yani bu
sorunun cevabı doğrudan bir iş kalemi.

## 8. "Takip edilen zincirler" satırındaki chevron nereye gidiyor?

**Tasarımın verdiği.** Ekran 7'de satır: *"Takip edilen zincirler · Migros,
A101, BİM · `chevron_right`"*.

**Gerçek.** Chevron bir destinasyon sözü veriyor ama o ekran hiç çizilmedi — ve
karar 11 zaten *"kullanıcı elle mağaza eklemiyor"* diyor, yani açılacak ekranın
yapacağı bir iş yok. Satır kendi başına tamam: adlar değerin içinde duruyor.

Uygulama chevron'u **çizmedi**. Aynı tercih *"Sabit ürünler"* satırında da
yapılmıştı (tasarımda chevron var, uygulama listeyi satırın altında açıyor).

**Soru.** Chevron kalksın mı, yoksa bir mağaza ekranı mı gelecek? Gelecekse ne
gösterecek — zincir başına fiş sayısı, son fiyatlar?

## 9. Ekran 7'nin boş hâli karar 11 ile çelişiyor

**Tasarımın verdiği.** *"Ayarlar · boş hal (yeni hane)"* çerçevesinde Mağazalar
bölümü **çiziliyor** ve değeri *"İlk fişten öğrenilecek"*.

**Gerçek.** Karar 11 aynı bölüm için *"bölüm satır yokken hiç çizilmiyor"*
diyor. İkisi aynı anda doğru olamaz; uygulama kararı esas aldı, çünkü karar
mockup'tan sonra yazıldı.

**Soru.** Boş hâl çerçevesi güncellensin mi? (Aynı çerçevede *"Katılma kodu"*
satırı da dolu görünüyor ama Faz 7'ye kadar üretilmiyor.)

---

## 10. Ekran 4 pratikte hiç açılmıyor — açıldığında da uzun fişi okuyamıyor

*Bu madde diğerlerinden sonra, 1. maddenin ölçümü yapılırken cihazda çıktı.*

**Tasarımın verdiği.** Ekran 4 · Kamera: amber köşeli çerçeve rehberi, *"1.
kare"* sayacı, *"Bitti"*, izin ve hata metinleri. Tasarımın çizdiği, uygulamanın
da birebir yazdığı bir ekran.

**Gerçek — iki katmanlı.**

**(a) Ekran açılmıyor.** Belge tarayıcısı geldiğinden beri (F4.18) *"Fiş çek"*
ekranı açılır açılmaz **tarayıcı** başlıyor; kendi kameramız yalnızca tarayıcı
başlatılamazsa devreye giriyor. Play Services'i olan hiçbir cihazda o yola
düşülmüyor. Yani `CaptureScreen` — çerçeve rehberi, sayaç, *"Bitti"*, üç
önizleme — **yazılmış ama görünmeyen bir ekran**.

Bunun 2. maddeye doğrudan etkisi var: orada bu ekranın sayacını ve *"Bitti"*
metnini soruyoruz. Sormadan önce cevaplanması gereken soru şu — kullanıcının
gerçekten gördüğü Ekran 4, **tarayıcının kendi arayüzü** ve onu biz
çizmiyoruz.

**(b) Açıldığında uzun fişi okuyamıyor.** Cihazda ölçüldü, aynı fiş:

| | fişin kadrajdaki genişliği | tavanın yüzdesi | okunan görsel satır |
|---|---|---|---|
| tarayıcı | 693 px | %100 | **159** |
| kendi kameramız | ~500 px | %72 | **6** |

Sebep çözünürlük tavanı **değil** — tavan ikisinde de aynı (694 px, bkz. 1.
madde). Sebep **çerçeveleme**. Tarayıcı fişin kenarını buluyor, kırpıyor ve
kırpılmış hâli gösterip onaylatıyor. Bizim ekranımızda çerçeve rehberi yalnızca
bir overlay: hiçbir şey onu dayatmıyor, hiçbir şey kırpmıyor, ve kullanıcı
çerçeveye hizalamadığında bunu söyleyen hiçbir şey yok. Ölçüm karesinde fiş
kadrajın **%16'sını** kaplıyordu; gerisi masa, ayaklar ve **iki başka fiş**.

Sonuncusu ayrı bir tehlike: kadrajda üç fiş varken tarayıcı birine kırpar,
bizim yolumuz üçünü birden OCR'a verir — ve ne ayrıştırıcının ne satır
tekilleştirmesinin *"bu hangi fiş"* diye bir kavramı var.

**Sorular.**
1. Yedek yol **ciddi mi?** Ciddiyse ekranın kenar bulma/kırpma ya da en azından
   *"fiş çerçeveye sığmadı"* uyarısı taşıması gerekiyor: bugün çerçeve rehberi
   bir söz veriyor ve tutmuyor.
2. Ciddi değilse, kullanıcıya **söylenmeli mi?** Bugün hangi yolda olduğunu
   bilmiyor — tarayıcı açılamadığında sessizce daha zayıf bir kameraya düşüyor.
3. 2. maddedeki sayaç ve *"Bitti"* soruları hangi ekran için soruluyor? İkisi
   ayrı ekran, ve bugün görünen olan bizim değil.
