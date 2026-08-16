# Tasarıma: Pivot — fiş okuma çıktı, raf etiketi geldi

**16 Ağustos 2026.** Bu doküman önceki turlardan farklı: bir soru listesi
değil, **ürünün orta halkasının değiştiğinin bildirimi**. Üç iş istiyoruz:

1. **Kaldır** — fişle birlikte ölen ekranları ve kararları defterden düş.
2. **Revize et** — öncülü değişen kararları güncelle.
3. **Tasarla** — hiç çizilmemiş yeni yüzeyleri çiz (asıl iş bu).

---

## 1. Ne oldu ve neden

Fiş OCR'ı iki hafta gerçek cihazda, gerçek fişlerle ölçüldü. Sonuç: 60
kalemin 31'i okunuyor ve tavan fiziksel — fiş oranındaki bir nesne 12MP
sensörden en fazla ~694 piksel genişlik alabiliyor, yani "daha iyi kamera
işi" diye bir çözüm yok. Kullanıcı kararı verdi: **fiş okuma tamamen çıktı.**

**Yeni model:** kullanıcı markette tek tek **raf etiketi** çeker. Her çekim
tek bir tarihli fiyat gözlemidir: **ürün + marka + market + fiyat + tarih**.

Ürünün derdi aynı kaldı (fiyat körlüğü), kanıt yüzeyi değişti. Kullanıcının
kendi örneği, ürünün yeni çekirdek senaryosu:

> Yoğurdu **BİM**'den **Dost** marka **100 TL**'ye almışım;
> **File**'den **Harras** **110 TL**; **Migros**'tan **Pınar** **130 TL** —
> ben çektikçe tarih bazlı biriksin, hangi tarihte kaça almışım göreyim.

Dört karar (kullanıcı verdi, tartışma kapalı):

1. Çekilen şey **raf etiketi** — fiyatı taşıyan tek yüzey. Ambalaj değil.
2. **Marka gözlemin alanıdır, ürünün değil.** Ürün jenerik kalır ("yoğurt");
   aynı ürünün farklı marketlerdeki farklı markaları yan yana karşılaştırılır.
3. Etiket çekimi **listeden ve geziden bağımsızdır** — her an, her markette,
   alınsın alınmasın.
4. **"Ne ödedik" artık bir tahmindir** — gözlemlerden hesaplanır, her zaman
   `~` ile gösterilir. Gerçek ödenen tutar diye bir veri artık yok.

Teknik taraf hazır: fiyat gözlem tablosu ve onu çizen bileşenler (fiyat çipi,
delta çipi, sparkline, sepet tahmini) zaten yazılmıştı ve veri bekliyordu.

## 2. Kaldırılacaklar

| Ne | Neden |
|---|---|
| **Ekran 4 · Mod A (Fiş Kontrol)** — bütün çerçeveler, karanlık dahil | Ekranın konusu (fiş satırlarını doğrulama) yok |
| **Ekran 4 · kamera** — fiş çerçeve rehberi, "1. kare" sayacı, "Bitti", "Uzunsa 2 kare çek" | Fiş çekimi yok; yerine yeni etiket yüzeyi geliyor (bkz. §4) |
| **Karar 4** (çok parçalı fiş tek akış) | Parça kavramı tamamen öldü — istisna olarak bile yok |
| **Karar 9** (Fiş Kontrol manşeti, 36sp toplam) | Ekranıyla birlikte |
| **Karar 13, 14, 15** (zincir adı fiş künyesinden, barkod başlık, `~` manşet) | Üçü de fiş satırı/künyesi kavramına bağlıydı |
| **Ekran 6'daki parça hiyerarşisi** — girintili parça satırları, durum ikonları, "en kötü durum" ikonu | Geziye bağlı fiş yok |
| **Boş durumlar 04** ("Alışverişi bitir · açılmaz") | Mod A ölünce Mod B tek yol; çerçeve sadeleşmeli |
| **Logo konsepti C ("Fişin Kuyruğu")** | Fiş artık ürünün parçası değil |
| 09-tasarima-sorular-3.md'nin 1, 2, 3, 4, 5, 6, 10. maddeleri | Soruların konusu ortadan kalktı — cevap beklemiyoruz |

Hâlâ geçerli eski sorular: 09'un **7** (zincir adı büyük/küçük — market adları
hâlâ gösteriliyor), **8** (Takip edilen zincirler chevron'u), **9** (Ekran
7'nin boş hâli).

## 3. Revize edilecekler

**Karar 11 — "kullanıcı elle mağaza eklemiyor".** Öncülü öldü: mağazanın tek
otomatik kaynağı fiş künyesiydi. Yeni durum: yedi zincir hazır gelir (BİM,
A101, ŞOK, Migros, CarrefourSA, File, Tarım Kredi), kullanıcı etiket
çekerken **market seçer** (yapışkan: son seçilen hatırlanır), gerekirse "+
Yeni market" ile ekler. Bu, karar 11'in yasakladığı işlevsiz form değil —
seçim doğrudan fiyat karşılaştırmasını besliyor, ki karar 11'in kendi ölçütü
tam buydu. Ayarlar'daki Mağazalar bölümü artık ilk kurulumdan itibaren dolu.

**Karar 2 — "Verilerimi sil" kapsamı.** Kapsam metninde "fiş fotoğrafları"
geçiyordu; artık silinecek fotoğraf yok (etiket fotoğrafı kayıttan hemen
sonra siliniyor — öneri, aşağıda soru 5). Kapsam: liste, gezi, ürün, fiyat
gözlemi, mağaza.

**Ekran 1 — iki küçük yeniden bağlama.** (a) Alışveriş modu başlığındaki
mağaza adı artık fişten değil; kaynak, o gezide son çekilen etiketin marketi
olabilir ya da hiç gösterilmez — öneriniz? (b) "Tahmini sepet: ~640 TL"
artık gerçekten çalışacak (bugüne dek verisizdi) ve tek tutar türü tahmin
olduğu için `~` her tutarın önünde.

**Ekran 6 — Geçmiş.** Gezi satırı kalır: tarih + kalem sayısı + `~` tutar.
Fiş/parça alt satırları gider. Bir gezinin altında ne görünmeli — o gün
çekilen etiket gözlemleri mi, hiçbir şey mi?

## 4. Tasarlanacak yeni yüzeyler — asıl istek

### 4a. Etiket çekim + onay yüzeyi (hiç çizilmedi, en öncelikli)

Tek ekran, iki durum; kullanıcı market koridorunda tek elle, seri halde
kullanacak:

**Durum 1 · Kamera.** Etiket oranında çerçeve rehberi (etiketler yatay
dikdörtgen, fişin tersi). Rehber metni kısa: etiket kadraja otursun.
Sayaç yok, "Bitti" yok — her çekim kendi başına tam bir iş.

**Durum 2 · Onay kartı** (çekimden hemen sonra, aynı ekranda):

| Alan | İçerik | Davranış |
|---|---|---|
| **Fiyat** | OCR'dan dolu, büyük punto | Düzenlenebilir — "elle fiyat girilmez" kuralının tek istisnası: yanlış okumayı düzeltmek. OCR bulamazsa boş gelir |
| **Ürün** | Etiket metni tanınıyorsa çözülmüş ürün çipi ("Yoğurt") | Tanınmıyorsa "Ürün seç" → katalog/ürün seçici; kullanıcı BİR KEZ seçer, uygulama o marketin o etiket metnini öğrenir, bir daha sormaz |
| **Marka** | Etiket metninin ilk kelimesinden öneri ("Dost") | Düzenlenebilir çip, opsiyonel — markasız ürün (manav) meşru |
| **Market** | Yapışkan seçici, son seçilen önde | Yedi zincir + "Yeni market" |
| **Tarih** | Bugün | v1'de düzenlenmez |

Tek birincil eylem: **Kaydet** → kart kapanır, kamera geri gelir (bir sonraki
etiket). İkincil: vazgeç.

Sorularınız için elimizdeki kısıtlar: onay kartı çekilen fotoğrafın üzerine mi
oturmalı (kırpılmış etiket görüntüsü kartın arkasında/üstünde görünür mü)?
Karta ulaşılamayan alan kalırsa OCR hangi alanı dolduramadıysa kart o alanı
nasıl vurgulamalı?

### 4b. Ekran 5 (Ürün Detayı) — marka satırları

Ekran 5 zaten 0/1/9 gözlem hâlleriyle çizili ve **ayakta** — ama marka
kavramı yok. İstenen ek: mağaza karşılaştırma listesinde her satır **market +
marka + fiyat + tarih** taşımalı:

```
BİM      · Dost   · 100 TL · dün
File     · Harras · 110 TL · 3 gün önce
Migros   · Pınar  · 130 TL · geçen hafta
```

Bu liste ürünün yeni çekirdek ekranı — kullanıcının verdiği örnek birebir
burada çizilecek. Aynı marketten iki farklı marka gözlemi varsa ne olur
(iki satır mı, son olan mı) — kararınız.

Ayrıca 0-gözlem boş hâlinin metni "Fiş çektikçe..." diyordu — "Etiket
çektikçe..." olacak; yeni metni siz verin.

### 4c. Giriş noktası

Etiket çekimine nereden girilir? Elimizdeki aday: özet kartındaki eski "Fiş
çek" düğmesinin yerine "Etiket çek" — ama karar 3 gereği çekim geziden
bağımsız, yani gezi yokken de erişilebilir olmalı. Liste ekranının başlığı
mı, ayrı bir kalıcı düğme mi — kararınız.

## 5. Sorular (numaralı, cevap bekliyor)

1. Onay kartı fotoğrafın üzerine mi oturur; OCR'ın dolduramadığı alan nasıl
   vurgulanır? (§4a)
2. Ekran 5 marka satırları: aynı marketten iki marka = iki satır mı? (§4b)
3. Etiket çekimine giriş noktası neresi? (§4c)
4. Alışveriş modu başlığındaki mağaza adının yeni kaynağı ne — son etiketin
   marketi mi, hiç mi? (§3)
5. Etiket fotoğrafı kayıttan sonra **siliniyor** (önerimiz: etiket ödeme
   kanıtı değil, saklamak maruziyet). Katılıyor musunuz, yoksa gözlemin
   yanında küçük bir kanıt görseli tasarım açısından değerli mi?
6. Geçmiş'te gezi satırının altında etiket gözlemleri görünmeli mi? (§3)
7. Boş durumlar atlası: 04 çerçevesi yeni akışa göre yeniden çizilmeli mi,
   yoksa "Ekran hiç açılmaz" kategorisinde mi kalıyor?

---

*Teknik zemin (bilgi): gözlem verisi ürün başına (market, marka, fiyat,
tarih, birim fiyat, ambalaj boyu) taşır; sparkline/delta/fiyat çipi
bileşenleri mevcut ve değişmiyor; tek-SQL liste kuralı ve `Modifier.pressable`
sözleşmesi aynen geçerli.*
