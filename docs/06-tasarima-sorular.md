# Tasarıma sorular — kodun öğrendikleri

**15 Ağustos 2026.** Bu dosya Claude Design projesine verilecek prompttur. Kaynak tasarım `docs/tasarim/`, denetim [05-tasarim-denetimi.md](05-tasarim-denetimi.md).

---

## Bağlam

Neydi'nin tasarımı ile kodu bugün baştan sona karşılaştırıldı. **Mekanik kuralların hepsi zaten tutuyordu** — Fraunces 24sp altında yok, amber dolgular yalnızca `AccentSurface` üzerinden, ripple yok, `uppercase()` yok, `0.5.dp` yok, blur yok, dialog/badge/bottom-nav yok, dynamic color yok. Palet, tipografi ve ölçü token'ları `tokens.json` ile birebir.

Yapısal sapmaların çoğu düzeltildi (ikon seti bağlandı, Ekran 1'in çip şeridi kaldırılıp `more_vert` + alttaki birincil butona dönüldü, boş durumlar, Geçmiş, kategori tonları, Ekran 3 ve Ekran 7 yazıldı).

**Geriye kalanlar kod kararı değil — tasarım kararı.** Aşağıdakilerin her biri ya tasarımın iki kuralının çeliştiği ya da tasarımın yazıldığı tarihten sonra öğrenilmiş bir gerçeğin karşılığı olmayan bir yer. Uygulama bunları **boş bıraktı**; tahmin edip yanlış yapmaktansa sormayı seçti.

Her madde şu biçimde: **tasarımın kendi kuralı** → **kodun ölçtüğü gerçek** → **karar gereken soru**.

---

## 1. Alışveriş modundan çıkış yolu yok · **en acil**

**Tasarımın kuralı.** Ekran 1 üç modun tek ekranı. Alışveriş modunda gezinme gizli: *"Reyonda yanlışlıkla Ayarlar'a düşmek listeyi kaybetmek gibi hissettirir."* Başlıkta `more_vert` yok, geri oku yok. Tek çıkış floating toolbar'daki **"Bitir (12/18)"**.

**Kodun gerçeği.** Alışveriş modu ekranın durumu değil, **gezinin** durumu (`Trip.status`) — kalıcı. Uygulamayı kapatıp açmak modu bozmuyor; eşler de aynı modu görecek (Faz 7). Yani moda yanlışlıkla giren kullanıcı:

- geri tuşuyla çıkamıyor (mod ekran değil),
- uygulamayı kapatarak çıkamıyor (kalıcı),
- yalnızca **"Bitir"** ile çıkabiliyor — o da **yapmadığı bir alışverişi kapatıyor**, mutabakatı çalıştırıyor, istatistikleri yeniden kuruyor.

15 Ağustos'ta tasarıma dönerken birincil aksiyon **"Alışverişe çıkıyorum"** ekranın altına taşındı (tasarımın *"tüm birincil aksiyonlar ekranın alt %40'ında"* kuralı). Doğru oldu ama **maruziyeti artırdı**: mod artık tek dokunuş uzakta, en kolay erişilen yerde.

**Soru.** Alışveriş modundan geziyi kapatmadan çıkmanın yolu ne olmalı? Tasarımın *"reyonda gezinme gizli"* kuralını bozmadan:

- alışveriş modunda da `more_vert` açılsın, içinde **tek** madde: *"Alışverişi bırak"*?
- başlığa (`Liste` / `Migros Ataşehir · 12/18`) dokunmak modu bıraksın?
- floating toolbar'a dördüncü bir hedef mi?
- yoksa **bilinçli olarak çıkış olmasın** ve bunun yerine moda girmek onaylı mı olsun?

Karar aynı zamanda şunu belirliyor: mod yanlışlıkla girilebilir bir şey mi, yoksa kasıtlı bir taahhüt mü?

---

## 2. "Verilerimi sil" ile "sıfır dialog" çelişiyor

**Tasarımın iki kuralı.**
- Ekran 7 (Ayarlar) → Gizlilik bölümünde **"Verilerimi sil"** düz bir satır, `error` renginde.
- Compose spec → *"v1'de sıfır modal dialog, sıfır push, sıfır badge."*

**Kodun gerçeği.** Geri alınamaz bir silmeyi onaysız çalıştırmak kabul edilemez. Onay için tasarımda tanımlı tek yüzey yok: dialog yasak, snackbar geri-alma için kullanılıyor ama silme snackbar süresinden uzun sürecek bir iş.

Satır bu yüzden **hiç çizilmedi** — Ayarlar'ın Gizlilik bölümü yalnızca açıklama metnini gösteriyor.

**Soru.** KVKK silmesi hangi yüzeyle onaylanacak?

- satır içi iki aşamalı dokunuş (*"Verilerimi sil"* → *"Emin misin? Dokun ve onayla"*)?
- ayrı bir onay ekranı (dialog değil, tam ekran destinasyon)?
- yazarak onay (*"SİL" yaz*)?
- yoksa dialog yasağının **tek istisnası** bu mu?

Ayrıca: silme neyi siliyor? Hane mi, yalnızca bu cihazın verisi mi, fiş fotoğrafları dahil mi?

---

## 3. Floating toolbar'ın `undo` ve `filter_list` düğmeleri tanımsız

**Tasarımın gösterdiği.** Alışveriş modu toolbar'ında dört hedef: `add` (dolgulu, birincil), `undo`, `filter_list`, ve **"Bitir (0/18)"**.

**Tasarımın başka bir yerde yazdığı.** Ekran 1 notları: *"Geri alma 'Alındı' bölümünden tek dokunuş."*

**Kodun gerçeği.** `add` bağlandı (Ekle sheet'ini açıyor — reyonda klavye açmadan ekleme yolu). Diğer ikisi **çizilmedi**, çünkü:

- `undo` neyi geri alıyor belirsiz. Son işaretlemeyi mi? Ama tasarım geri almayı zaten "Alındı" bölümüne veriyor ve orada tek dokunuş. İki ayrı geri alma yolu iki farklı zihinsel model demek.
- `filter_list` hiçbir yerde tanımlı değil. Neye göre filtre? Reyona göre mi (liste zaten reyona göre gruplu), alınmamışlara göre mi (başlık zaten "2 kaldı" diyor)?

Hiçbir şey yapmayan buton çizmek, tasarımın *"boş bölüm çizilmez"* ilkesinin aynı sınıfı olurdu.

**Soru.** Bu iki hedef ne yapıyor — yoksa toolbar iki hedefe mi (`add` + `Bitir`) inmeli?

---

## 4. Çok parçalı fiş tasarımda yok

**Tasarımın varsayımı.** Bir alışveriş = bir fiş. Ekran 4'ün kamera adımı *"Uzunsa 2 kare çek"* diyor ama sonrasındaki ekranların hiçbiri iki kareyi ayrı ayrı göstermiyor; Geçmiş satırı tek fiş varsayıyor, Fiş Kontrol tek fiş gösteriyor.

**Kodun ölçtüğü.** ~60 kalemlik bir fiş tek kareye sığdırılınca **satır başına 4,7 piksel** düşüyor ve OCR 60 satırın 2'sini okuyabiliyor — *ham kamera çözünürlüğü bile ~7 px/satır verirdi*. Yani çok kare **fiziksel bir zorunluluk**, bir kolaylık değil. Uygulama bunun üzerine bir akış kurdu (F4.13): parçalar aynı geziye bağlanıyor, toplam yalnızca son parçada basılı oluyor.

Bunun tasarımda karşılığı olmayan üç sonucu var:

- **Geçmiş satırı**: bir gezi = bir satır mı, yoksa parçalar altında listelenmeli mi? (Uygulama şu an parçaları girintili listeliyor — yoksa yanlış okunmuş bir parçaya erişilemiyor, ki bu ekranın varlık sebebi tam olarak o.)
- **Aritmetik kapısı**: toplam son parçada, satırlar bütün parçalarda. Kapı fişin tamamı üzerinden hesaplanıyor. Ekranda "Fişin tamamı tutuyor" mu yazmalı, yoksa parça başına bir şey mi?
- **Fiş durumu ikonu**: bir parça okunamadıysa gezinin ikonu ne? (Uygulama en kötü hali gösteriyor.)

**Soru.** Çok parçalı fiş, Ekran 4 ve Ekran 6'da nasıl görünmeli? Parça bir "hata hali" değil normal bir hal — tasarımın bunu nötr göstermesi gerekiyor.

---

## 5. İlk gün boş durumu: ürün çipi mi, reyon çipi mi?

**Tasarımın gösterdiği** (Boş Durumlar 08, "Kurulum atlandı"): 3×4 grid, **12 başlangıç ÜRÜNÜ** (Ekmek, Süt, Yumurta, Peynir, Zeytin, Domates, Salatalık, Soğan, Çay, Yoğurt, Makarna, Deterjan), *"Dokun, listeye düşsün."*

**Kodun yaptığı** (F3.6 kararı): **reyon** çipleri. Gerekçesi kayıtlı — reyon çipi Ekle sheet'ini açıyor ve oradan seçtiriyor.

İkisi farklı iş: ürün çipi **tek dokunuşta listeye düşürüyor**, reyon çipi **bir adım daha ekliyor**.

**Soru.** Tasarımın 12 ürünü mü kazanmalı? Öyleyse o 12 ürün nereden geliyor — sabit bir liste mi, katalogdaki `commonalityRank` ilk 12'si mi?

---

## 6. Kurulum (Ekran 8) — 1. adım auth'a bağlı, ve ekran ne zaman açılacak?

**Tasarımın gösterdiği.** Üç adım: (1) Hane — e-posta, *"Yeni hane oluştur"*, katılma kodu, *"Kodla katıl"*; (2) Her zamankiler grid'i, *"Devam (9 seçildi)"*; (3) Tempo çipleri, *"Listeme geç"*. Adım 2 ve 3'te **"Atla"** var, adım 1'de yok.

**Kodun gerçeği.** Adım 2 ve 3'ün veri yolu hazır (`productDao.setStaple`, `AppSettings.tempoDays`). **Adım 1'in tamamı Faz 7'de** — e-posta, auth, katılma kodu üretimi, hane katılma akışı; hiçbiri yok.

Ayrıca tasarım *"bir daha görünmez"* diyor ama **tetikleyici tanımlı değil**. `setupCompletedAt == null` tek başına yetmiyor: mevcut kurulumlarda **dolu bir veritabanının üstüne onboarding açılır**.

**İki soru.**
- Kurulum, auth gelene kadar **iki adım** mı olsun (tempo + sabitler, *"1 / 2"* · *"2 / 2"*), yoksa Faz 7'ye kadar hiç mi görünmesin?
- Ekran hangi koşulda açılıyor? "Hane hiç ürün görmedi" koşulu da gerekli mi?

---

## 7. Ekran 3'ün bölüm "not" alanı boş

**Tasarımın gösterdiği.** Ekran 3'te her bölüm bir `title` **ve** bir `note` taşıyor: `{{ m.title }}` / `{{ m.note }}`.

**Kodun gerçeği.** Başlıklar yazıldı (*"Geçen sefer unuttun"*, *"Her zamankiler"*, *"Bitmiş olabilir"*) ama `note`'un ne diyeceği tasarımda örneklenmemiş. Uygulama **notu hiç çizmedi** — uydurmak, gerekçe yazma disiplinine aykırı olurdu.

**Soru.** Üç bölümün notu ne? Örneğin *"Bitmiş olabilir"* için *"Tahmin — sen onaylamadan eklenmez"* gibi bir şey mi?

---

## 8. "Liste hazır, eksik görünmüyor" bildirimi için yüzey yok

**Tasarımın gösterdiği.** Ekran 3 boşsa açılmıyor, doğrudan alışveriş moduna giriliyor ve **2 saniyelik koyu bir pill** görünüyor: *"Liste hazır, eksik görünmüyor"*.

**Kodun gerçeği.** Atlama davranışı yazıldı ve cihazda doğrulandı — ama **geçici mesaj yüzeyi yok**. Uygulamanın snackbar'ı da yok: F3.5'te *"bir gezide 20 işaretleme var, snackbar ekranı felce uğratırdı"* diye bilinçli olarak eklenmemişti.

**Soru.** Bu 2 saniyelik bildirim hangi bileşen? Snackbar yasağının istisnası mı, yoksa ayrı bir "toast" bileşeni mi tanımlanmalı? Tanımlanacaksa: konumu (toolbar'ın üstü), zemini (`textPrimary`), süresi, ve **hangi olaylarda** kullanılacağı.

---

## 9. Ekran 4 · Fiş Kontrol — tutar manşet değil

**Tasarımın gösterdiği.** Fiş Kontrol'de **642,50 TL** Fraunces 36sp manşet, yanında `check_circle` ve *"Toplam tutuyor"*. Ayrıca `add` **"Eksik satır ekle"**, **"Onayla ve kaydet"**, ve yanlış eşleşme için **aday sheet'i** (*"Bu satır hangi ürün?"* + *"en olası"* + arama).

**Kodun gerçeği.** Tutar küçük bir pill çipin içinde; "Eksik satır ekle" yok; düzeltme 3 dokunuş (tasarım 2 hedefliyor); buton *"Tamam"*.

Ekran **çalışıyor ve gerçek fişle doğrulanmış** — bunlar işlev kaybı değil sadakat farkı. Ama tasarımın niyeti açık: *"ekran görüntüsü alınacak şey bu cümle"* ilkesi Ürün Detayı için yazılmış ve burada da geçerli görünüyor.

**Soru.** Fiş Kontrol'de manşet tutar mı olmalı yoksa satırlar mı? (Ekran zaten kaydırmalı ve satır sayısı değişken.)

---

## 10. Küçük kalemler

- **Avatar.** Tasarımda her ekranın başlığında 28dp avatar + 6dp varlık noktası var. Tek kullanıcılı hanede de görünmeli mi? (Uygulamada `avatarOnlyDrawnWhenPartnerAdded` testi var ama başlıkta çizilmiyor.)
- **`PriceText`.** Devir paketinin çözülmemiş tek token uyuşmazlığı: tasarım çipte 14sp / fiş satırında 17sp istiyor, kodda tek 15sp var. Paket *"ikiye ayır"* öneriyor — onaylanıyor mu?
- **Mağazalar ve Önerilmeyenler bölümleri (Ekran 7).** Tasarım ikisini de gösteriyor; `store` ve `suggestion_block` tabloları şemada var ama **hiçbir yazan yok**. Bölümler çizilmedi (*"boş bölüm çizilmez"*). Mağaza satırları ne zaman doluyor — ilk fişten mi, kullanıcı mı ekliyor?
- **Ekle sheet'inde `check_circle`.** Tasarım eklenmiş ürünleri işaretli gösteriyor. "Eklenmiş" = bu listede mi, yoksa bu sheet oturumunda eklenmiş mi?

---

## Not: bilinçli sapmalar (karar gerekmiyor, bilgi için)

- **İkon seti.** Tasarım Material Symbols Rounded istiyor; o font paketlenmiş bir Compose artifact'i olarak yayınlanmıyor. `Icons.Rounded.*` kullanıldı — aynı çizim dili, aynı 24dp optik boyut. Tek dosyadan (`NeydiIcon.kt`) değiştirilebilir.
- **Safe area.** Tasarım sabit 44/34dp veriyor; kod gerçek `WindowInsets.safeDrawing` kullanıyor. Android'de doğrusu bu — sabitler iOS ölçüleri ve cihazdan cihaza değişen çentik/gezinme çubuğunu karşılamıyor.
- **Kamera overlay.** Tasarımın *"Fişin tamamı kadraja girsin"* overlay'i sistem kamerasına konamıyor; metin çekimden **önceye** taşındı (F4.13).
- **Renk sistemi.** Compose spec `LocalNeydiColors` öneriyor, kod M3 `ColorScheme` + `NeydiExtraColors` kullanıyor. Devir paketi bunu zaten uzlaştırmış: *"Çakışma olursa repo kazanır."*
