# Tasarım bulguları — biriken liste

Bu dosya **açık uçlu**: teknik bir iş yaparken tasarımda bir eksik ya da sapma
görüldüğü anda buraya yazılıyor, birikince topluca tasarıma prompt olarak
veriliyor. Kapanan maddeler [Kararlar](tasarim/Neydi%20-%20Kararlar.dc.html)'a
geçiyor ve buradan siliniyor.

Biçim, iki turda da işe yarayan biçim: **tasarımın verdiği** → **gerçek** →
**soru**. Her maddede hangi işi yaparken çıktığı yazıyor, çünkü bir bulgunun ne
kadar acil olduğunu o söylüyor.

---

## 1. Zincir adı büyük/küçük harf düzeni

**Nerede çıktı.** Karar 13 uygulanırken.

**Tasarımın verdiği.** Örneklerde `File` ve `Migros` yazıyor — yani başlık
düzeni (ilk harf büyük, gerisi küçük).

**Gerçek.** Bu projede locale'siz harf dönüşümü yasak ve sebebi ölçülmüş:
`"İNCİR".lowercase()` beş harf yerine yedi kod noktası üretiyor (bkz.
`MatchKey.kt`). Türkçe İ/ı kuralı için doğru dönüşüm locale gerektiriyor, o da
Compose Multiplatform'un ortak katmanında yok.

Şu an fişin bastığı hal olduğu gibi gösteriliyor: `AKYURT`, `FiLE`, `BIM`.

**Soru.** Zincir adı hep büyük harf mi kalsın (fişin yazdığı hal, dürüst ama
tasarımdan sapma), yoksa başlık düzeni tasarım açısından zorunlu mu? Zorunluysa
her platform için ayrı bir `lowercase(Locale)` köprüsü yazılacak.

## 2. Barkodu da olmayan satır ne yazacak?

**Nerede çıktı.** Karar 14 cihazda doğrulanırken.

**Karar 14'ün verdiği.** Adı okunamayan satırın başlığında **barkod** duruyor.

**Gerçek.** Tartı satırlarında barkod yok. Cihazdaki AKYURT fişinde
`9 2902925 1,206 Kg 109,00 %01 131,45` satırı: sekiz haneli bir dizi var ama o
barkod değil, tartı etiketinin kendi kodu — ve satırda ürün adı hiç geçmiyor.

Bu satır hâlâ **"Eşleşmedi"** yazıyor, yani karar 14'ün kapattığı sorunun
küçük bir kalıntısı duruyor: yedi satırdan biri hâlâ hata mesajını ürün adının
yerine koyuyor.

**Soru.** Ne barkod ne ad okunabilen satırın başlığı ne olmalı? Ham metnin
kendisi mi, *"Tartı ürünü"* gibi bir tür adı mı, yoksa satır bambaşka mı
çizilmeli (örneğin yalnız tutar ve "dokun, ürünü seç")?

## 3. "Takip edilen zincirler" satırındaki chevron nereye gidiyor?

**Nerede çıktı.** Karar 11 uygulanırken.

**Tasarımın verdiği.** Ekran 7'de satır: *"Takip edilen zincirler · Migros,
A101, BİM · `chevron_right`"*.

**Gerçek.** Chevron bir destinasyon sözü veriyor ama o ekran hiç çizilmedi — ve
karar 11 zaten "kullanıcı elle mağaza eklemiyor" diyor, yani açılacak ekranın
yapacağı bir iş yok. Satır kendi başına tamam: adlar değerin içinde duruyor.

Uygulama chevron'u **çizmedi**. Aynı tercih "Sabit ürünler" satırında da
yapılmıştı (tasarımda chevron var, uygulama listeyi satırın altında açıyor).

**Soru.** Chevron kalksın mı, yoksa bir mağaza ekranı mı gelecek? Gelecekse ne
gösterecek — zincir başına fiş sayısı, son fiyatlar?

## 4. Ekran 7'nin boş hali karar 11 ile çelişiyor

**Nerede çıktı.** Karar 11 uygulanırken.

**Tasarımın verdiği.** *"Ayarlar · boş hal (yeni hane)"* çerçevesinde Mağazalar
bölümü **çiziliyor** ve değeri *"İlk fişten öğrenilecek"*.

**Gerçek.** Karar 11 aynı bölüm için *"bölüm satır yokken hiç çizilmiyor"*
diyor. İkisi aynı anda doğru olamaz; uygulama kararı esas aldı, çünkü karar
mockup'tan sonra yazıldı.

**Soru.** Boş hal çerçevesi güncellensin mi? (Aynı çerçevede "Katılma kodu"
satırı da dolu görünüyor ama Faz 7'ye kadar üretilmiyor.)

## 5. Tek akışta "Parça fişi · toplam son parçada" çipi artık ekranı anlatmıyor

**Nerede çıktı.** Karar 4 uygulanırken.

**Tasarımın verdiği.** Çok parçalı çerçevede çip: *"Fişin tamamı tutuyor"* —
yani aritmetik tuttuğunda ne yazacağı belli.

**Gerçek.** Üçüncü hal — **hiçbir parçanın toplamı okunamadı** — hâlâ eski
cümleyi taşıyor: *"Parça fişi · toplam son parçada"*. Bu cümle karar 4'ten önce
doğruydu, çünkü ekran o zaman tek bir parçayı gösteriyordu. Artık ekran fişin
tamamını gösteriyor, yani "bu bir parça fişi" demek ekranın kendisiyle
çelişiyor.

Cihazda görüldü: iki parçalı AKYURT fişinde ikinci parça hiç okunamadı, manşet
`~229,05 TL` (satırlardan hesaplandı) ama çip hâlâ "toplam son parçada" diyor —
oysa son parça okunamadı ve kullanıcının yapması gereken şey onu yeniden
çekmek.

**Soru.** Bu halde çip ne demeli? *"Son parça okunamadı"* gibi eksiği söyleyen
bir şey mi, yoksa karar 15'in çipiyle mi ("Satırlardan hesaplandı")
birleşmeli?

## 6. "Bu fişin devamını çek" listenin dibinde kalıyor

**Nerede çıktı.** Karar 4 cihazda doğrulanırken.

**Tasarımın verdiği.** Satır, kaydırılan içeriğin **sonunda** duruyor — son
parçanın satırlarından hemen sonra.

**Gerçek.** Tasarımın çerçevesinde altı satır var; gerçek fişte altmış olabilir.
Cihazda satır listesi kaydırılmadan bu satır görünmüyor — yani "devamını çek"
tam da uzun fişte, en çok gerektiği anda gizli kalıyor.

**Soru.** Satır listenin sonunda mı kalsın (tasarımdaki yer), yoksa alt buton
bloğuna mı sabitlensin? Sabitlenirse "Onayla ve kaydet" ile aynı görsel
ağırlığa gelmemesi gerekir.

## 7. Parça parça çekim, beklenen yol olmaktan çıktı — karar 4'ün öncülü değişti

**Nerede çıktı.** F4.17 (tek çekim) uygulanırken. **Bu listedeki en büyük
madde; diğerlerinin çoğu buna bağlı.**

**Tasarımın verdiği.** Karar 4: *"Parça normal bir hal, hata hali değil."*
Gerekçesi de yazılı: *"60 kalemlik bir fiş tek kareye sığdırılınca satır başına
4,7 piksel düşüyor — çok kare fiziksel bir zorunluluk, kolaylık değil."*

**Gerçek — ve o ölçüm eksikti.** 4,7 piksel, fotoğraf **OCR'a verilmeden önce
bizim** uzun kenarı 2576 piksele indirmemizden sonraki değerdi. Sensör ~4000
veriyor. Yani kaybın büyük kısmı fizik değil, kendi koyduğumuz sınırdı.

Artık fotoğraf **içeride** üst üste binen şeritlere bölünüp her şerit kendi
çözünürlüğünde okunuyor; kullanıcıdan dört ayrı kare istemeden aynı etki
alınıyor.

**Kullanıcı bunu üç kez, açıkça istedi:** *"parça parça çekmeye çok karşıyım,
tekte çekip içeride bizim ayarlamamız lazım"*, *"ard arda doğru satırı ayarlamak
çok zahmetli ve tutarlı olmuyor"*. Ölçüm de onu doğruladı: dört parçalı bir
çekimde 60 kalemin 42'si iki parçada birden okunmuş, 6'sı hiç okunmamıştı.

**Parça YOK OLMADI, istisnaya indi.** Hâlâ gerekiyor: tek kare okunamazsa,
ya da eksik kalem tespit edilirse. Fiş Kontrol'deki bölüm başlıkları ve
Geçmiş'teki girintili parça satırları duruyor.

**Sorular.**
1. Karar 4'ün *"parça normal bir hal"* öncülü güncellensin mi — "istisnai hal,
   ama hata değil" gibi?
2. Ekran 4'ün alt şeridi (*"1. kare"* sayacı + *"Bitti"*) tek çekim varsayılan
   olunca hâlâ doğru mu? Tek kare çekip biten akışta "Bitti" yerine doğrudan
   Fiş Kontrol'e geçmek daha mı doğru?
3. Ekran 6'daki parça satırları istisna hâline göre mi çizilmeli (örneğin
   yalnızca bir parça sorunluysa)?

**Ölçüm bekliyor:** tek karede çekilmiş 60 kalemlik bir fişin kaç kalemini
okuduğumuz henüz görülmedi. Sonuç "hepsi" değilse bu maddenin cevabı değişir —
o yüzden tasarıma verilecek prompt bu ölçümden **sonra** yazılmalı.

**Ölçüm neden bugüne kadar yapılamadı (F4.20).** Ölçülecek olsaydı şerit
yöntemi değil, **silinmiş bir EXIF etiketi** ölçülmüş olurdu. Zincir şuydu:
CameraX yönü yalnızca EXIF'e yazıyor, pikselleri döndürmüyor; `downscaleForOcr`
etiketi okumadan çözüp etiketsiz yazdığı için yön bilgisi diske yazarken
kayboluyordu; okuma tarafı onu tahmin etmeye çalışıyor ama tahmini dörtte bir
boyunda bir kopyaya bakarak yapıyordu ve uzun fişte hiçbir şey okuyamayıp her
seferinde 0 dereceye düşüyordu; görsel satır gruplaması da o yanlış açıya
güvendiği için fişi birkaç dev satıra çöküyordu. Yani "tek kare yetmiyor"
sonucu, ölçüm yapılsaydı **yanlış nedenle** çıkacaktı ve karar 4'ün öncülü
yanlışlıkla geri alınacaktı.

Üçü de kapatıldı: yön artık piksele işleniyor, gruplama ekseni OCR'ın kendi
köşe noktalarından ölçülüyor (dört yönde de aynı satırları verdiği testle
tutuluyor) ve yön seçimi tam çözünürlüklü bir şeritten yapılıyor. Ölçüm artık
anlamlı — ve tarayıcı modu ölçümüyle (`adb logcat -s NeydiOlcum`) aynı koşumda
alınabilir.

## 8. Kamera rehberi artık ikinci kare istemiyor

**Nerede çıktı.** F4.17 (tek çekim) uygulanırken.

**Tasarımın verdiği.** Ekran 4'ün rehber metni: *"Fişin tamamı kadraja girsin.
Uzunsa 2 kare çek."* Altında da "1. kare" sayacı ve "Bitti".

**Gerçek.** Fotoğraf artık **içeride** üst üste binen şeritlere bölünüp her
şerit kendi çözünürlüğünde okunuyor. İkinci kare artık *beklenen* şey değil,
yalnızca okuma yetmediğinde başvurulan yol — kullanıcı da part part çekmeye
açıkça karşı çıktı.

Metin *"Fişin tamamı kadraja girsin."* olarak kısaldı; ikinci kare ancak ilk
kare okunamadığında ya da eksik kalem tespit edildiğinde öneriliyor.

**Soru.** Sayaç ("1. kare") ve "Bitti" tek çekim varsayılan olunca hâlâ doğru
kelimeler mi? Tek kare çekip biten bir akışta "Bitti" yerine "Kullan" ya da
doğrudan Fiş Kontrol'e geçiş daha mı doğru?
