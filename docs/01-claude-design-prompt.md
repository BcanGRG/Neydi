# Claude Design — Ana Prompt

> Aşağıdaki bloğun tamamını kopyalayıp claude.ai/design'da **yeni bir design system projesine** yapıştır.
> Tek değiştirmen gereken şey ilk satırdaki **UYGULAMA ADI**.
> Bu prompt tek seferde her şeyi istemiyor: önce design system + 2 ekran üretiyor, sonra alttaki takip promptlarıyla ilerliyorsun.

---

## ⬇️ BURADAN İTİBAREN KOPYALA

**UYGULAMA ADI: Neydi**

Türkçe konuşan bir çift için, Kotlin Multiplatform + Compose Multiplatform ile iOS ve Android'de tek kod tabanından çalışacak bir market listesi uygulamasının design system'ini ve ekranlarını tasarlamanı istiyorum. Arayüz dili **sadece Türkçe** — tüm metinleri gerçek Türkçe yaz, placeholder/Lorem ipsum kullanma.

### 1. ÜRÜN VE KULLANICILAR

İki kişilik bir hane. Yaklaşık 10 günde bir markete gidiyorlar. Bugün eşlerden biri telefonunda not tutuyor, markette tek tek işaretliyorlar. İki gerçek problemi var:

1. **Unutma:** Her seferinde aldıkları şeyler (tam buğday ekmeği gibi) listeye yazılmadığı için alınmadan eve dönülüyor.
2. **Fiyat körlüğü:** Bir ürüne geçen sefer ne ödediklerini, fiyatın artıp artmadığını, başka markette daha ucuz olup olmadığını bilmiyorlar.

Uygulamanın kapalı döngüsü: **liste → markette işaretle → fiş fotoğrafı → ürün bazında fiyat geçmişi → sonraki listede öneri.**

**Mutlak kısıt:** Kullanıcı elle fiyat yazmayacak. Uygulamanın tamamında sayısal klavye **tek bir yerde** açılır: fişten yanlış okunan bir fiyatı düzeltirken. Fiyat girmeyi gerektiren hiçbir akış tasarlama.

**İkinci kısıt:** Az ekran. Bu bir dashboard'lu, bottom-nav'lı, feed'li uygulama değil. **Liste ekranı uygulamanın kendisidir**, diğer her yer oraya dönen bir sapmadır. Bottom navigation bar YOK.

### 2. TASARIM İLKELERİ (bunlar tasarım kararlarını yönetir)

- **Tek liste, tek döngü.** Ana ekran haricindeki her destinasyon varlığını ispat etmek zorunda.
- **Liste ekranı bir "mod makinesi"dir, ekran seti değil.** Planlama modu, alışveriş modu ve alışveriş-sonrası modu *aynı ekranın* farklı yoğunluk, sıralama ve birincil butonlarıdır. Bunları ayrı ekranlara bölme — bu, ekran sayısındaki en büyük tasarruf.
- **Fiyat hafızası satırda yaşar, sekmede değil.** "Bu pahalandı mı?" sorusunun cevabı, karar anında, reyonda, dokunmadan görünür olmalı. Fiyat geçmişi ekranı satırı *açıklamak* için var, ziyaret edilmek için değil.
- **Asla boş ekran gösterme.** İçi boş açılacak bir ekran hiç açılmaz — otomatik tamamlanır ve bir toast gösterir. Yokluk geçerli ve en iyi boş durumdur.
- **Varsayılanı tersine çevir.** "Eklemeyi hatırla" yerine "gerekmiyorsa çıkar". Her zaman alınanlar her yeni listeye otomatik eklenir. Ekmek problemi bir hatırlatma problemi değil, bir varsayılan problemi.
- **Her öneri düz Türkçe bir gerekçe cümlesi taşır, yoksa gösterilmez.** "son 12 alışverişin 11'inde aldın" bir hafıza yardımıdır; gerekçesiz bir çip reklamdır.
- **Hiçbir şey bölmez.** v1'de sıfır push bildirimi, sıfır badge, sıfır kırmızı nokta, sıfır modal dialog.
- **Yanlış bir şey göstermektense hiçbir şey gösterme.** Tek gözlemden yüzde gösterilmez, ambalaj boyu değiştiyse trend gösterilmez, 2 gözlemin altında grafik çizilmez.

### 3. TEKNİK KISITLAR — Compose Multiplatform (BUNLARI İHLAL ETME)

Bu tasarım Figma'da kalmayacak, Compose Multiplatform'da kodlanacak ve iki platformda **aynı** render edilmek zorunda. Aşağıdakiler güzel görünüp kodda kırılan şeyler:

- **Blur / frosted glass / Liquid Glass / vibrancy YOK.** Derinlik için opak tonal katman kullan (surface → surfaceVariant → +1 ton), gölge değil.
- **Renkli gölge YOK.** Gölge sadece iki elemanda: floating toolbar ve ekleme butonu, maksimum 3–4dp, varsayılan siyah. Kaydırılan içeriğin içinde hiç gölge yok — ayrım 1dp hairline ile yapılır.
- **Sistem fontu YOK.** "iOS'ta SF Pro, Android'de Roboto" yazma. Tek bir variable font ailesi bundle edilecek.
- **Material ripple YOK.** Her etkileşimli eleman için basılı durumu açıkça tanımla: scale 0.97 + %6 tonal overlay. "Varsayılan ripple" bir cevap değil.
- **Platforma özgü bileşen YOK.** iOS date picker, UIKit alert, native segmented control, share sheet — hiçbiri.
- **Scrollbar YOK.** Konum geri bildirimi için sticky kategori başlıkları veya sayaç kullan.
- **Emoji ikonografi olarak YOK.** Apple ve Google emoji'leri farklı çizer.
- **ALL-CAPS YOK ve hiçbir metne uppercase/lowercase uygulanmaz.** Türkçe İ/i/I/ı bunu bozar. Butonlarda İngilizceye göre **%25 metin genişlemesi** payı bırak.
- **Sol kenar swipe'ı iOS'ta geri gitmeye ayrılmıştır.** Satırlarda swipe hareketi tasarlarsan sadece sağ kenardan başlasın ve her yıkıcı işlem için jest olmayan bir yol da bulunsun.
- **Safe area zorunlu ve simetrik değil.** Alt kenara yapışık birincil aksiyon olmaz — iOS home indicator için 34pt boşluk bırak, üstte 44pt.
- **0.5dp hairline YOK.** Minimum 1dp.
- **Dynamic color / Material You kimliğin parçası olamaz** (Android-only). Sabit marka paleti varsayılan.

### 4. DESIGN SYSTEM — "Sıcak Kiler"

Ton: iyi tutulmuş bir mutfak rafı. Sıcak kağıt, terracotta, ot yeşili, bal. "Bir ürün" değil "bizim" hissi versin. Evde sakin, markette hâlâ okunabilir olsun.

**Işık modu token'ları:**
```
surface          #FBF7F2
surfaceVariant   #F1E7DB
primary          #B34418
onPrimary        #FFFFFF
secondary        #3F6B54
accent           #E0A32E
accentOutline    #8A5A00   ← ışık modunda her accent dolgusunun ZORUNLU 1.5dp kenarlığı
onAccent         #3A2600
success          #2E6B45
warning          #96560A
error            #B3261E
outline          #8A7666
textPrimary      #221A14
textSecondary    #5C4F45
hairline         #E7DACB
```

**Karanlık modu token'ları:**
```
surface          #13100E
surfaceVariant   #241E1A
primary          #FF9166
onPrimary        #3B1503
secondary        #8FC7A2
accent           #F2C14E
onAccent         #2A1D00
success          #7FD1A0
warning          #F0B357
error            #FF8A80
outline          #8A7A6E
textPrimary      #F5EDE6
textSecondary    #C6B6A9
```

**Kontrast kuralı — ihlal etme:** Amber (#E0A32E) ışık modunda surface üzerinde 2.08:1, yani tek başına sınırını taşıyamaz. Işık modundaki her amber dolgu 1.5–2dp `#8A5A00` kenarlık taşımak zorunda; amber ışık modunda asla metin rengi değildir. Diğer tüm çiftler WCAG AA'yı geçiyor, bozma.

**Tipografi:**
- **Plus Jakarta Sans** — UI, gövde, etiketler ve **tüm rakamlar**. Kullanılan ağırlıklar: 400 / 500 / 600 / 700 / 800. Hafif geometrik ama humanist sıcaklığı olan bir yüz.
- **Fraunces** — **SADECE display**, ağırlık 600. Yumuşak, sıcak bir optik serif. Mutfak rafı hissini taşıyan şey bu.
- İkisi de OFL ve bundle edilebilir. İkisinin de `latin-ext` kapsamı doğrulandı — `Ğğ İı Şş ÇÖÜçöü` tam destekli.
- Fraunces'i **optik boyut ~72, SOFT ~30, WONK 0** ayarında kullan. WONK açık kalırsa harfler kasıtlı olarak "eğri" davranır — istediğimiz sıcaklık bu değil, o garipliğe kaçar. SOFT ~30 köşeleri yumuşatır ve serif'i ciddi/editoryal olmaktan çıkarıp sıcak yapan şey odur.

**Fraunces'in kullanım sınırı — bu kural katı:** Fraunces yalnızca **24sp ve üstünde** kullanılır. Liste satırında, çipte, butonda, etiketde, metadata satırında, form alanında **asla** görünmez. Görevi tam olarak dört yer: (1) Ürün Detayı'ndaki manşet cümlesi, (2) alışveriş sonrası özet kartındaki büyük tutar, (3) Kurulum adım başlıkları, (4) boş durum başlıkları. Serif'i yoğun listeye sokarsan tasarım anında dağılır.

Ölçek (sp):
- **display** 44 / 36 / 30 — Fraunces 600, satır yüksekliği 1.10, harf aralığı −1%
- **headline** 28 / 24 — Fraunces 600, lh 1.20
- **title** 22 / 18 / 16 — Plus Jakarta Sans 600–700, lh 1.28
- **body** 17 / 15 / 14 — Plus Jakarta Sans 400–500, lh 1.45
- **label** 15 / 14 / 12 — Plus Jakarta Sans 500–600, lh 1.30

**Gövde minimumu 14sp, 13sp değil.** Plus Jakarta Sans'ta optik boyut ekseni yok, yani küçük punto optik olarak telafi edilmiyor. Satırın ikinci satırı (fiyat ipucu, gerekçe, not) 13sp değil **14sp** olacak; %60 opaklıkla birlikte 13sp okunaklılık sınırının altına düşer.

Liste öğesi adı = 17sp Plus Jakarta Sans 500 (alışveriş modunda 20sp / 700). Adet rozeti = 20sp Plus Jakarta Sans 800. **Tüm fiyatlar tabular figures (`tnum`) ile** — rakamlar sütun halinde dikey hizalanmalı. Buna ek olarak fiyat sütununu **sabit genişlikte ve sağa dayalı** tasarla; böylece `tnum` herhangi bir sebeple uygulanmazsa da düzen bozulmaz.

**Şekil:** 8dp grid. Liste satırı container 20dp · kategori kutucuğu 24dp · bottom sheet üst 28dp · text field 18dp · çip ve buton tam yuvarlak (pill) · onay hedefi daire, işaretlendiğinde 12dp squircle'a dönüşür.

**İkonografi:** Material Symbols Rounded, weight 400, optical size 24, rest'te fill 0 / seçilide fill 1. Kategori kutucuğu 56dp sıcak tonlu squircle. **Önce şu fallback'i tasarla — öğelerin %80'i bunu gösterecek:** aynı squircle, kategorinin tonunda, ürünün ilk iki harfi Plus Jakarta Sans 800 20sp ile (harfler kaynakta büyük yazılır, runtime'da değil).

**Hareket:** Yaylanma damping ~0.9 / stiffness ~400 — kararlı oturur, görünür overshoot yok. İşaretleme (200ms): daire→squircle şekil dönüşümü + 0.96 scale çukuru, satır doygunluğunu kaybeder ve 260ms'de "Alınanlar" bölümüne kayar. **Işınlanmaz** — göz takip edebilmeli, yoksa kullanıcı zaten işaretlediği şeyi tekrar ekler.

### 5. EKRANLAR

Toplam 6 tasarlanmış ekran + 2 yardımcı. Her ekranın **boş / dolu / karanlık mod** halini üret.

---

#### EKRAN 1 — LİSTE (ana ekran, uygulamanın kalbi)

Üç modu olan tek ekran. Uygulamada geçirilen sürenin %90'ı burada.

**Header (56dp, kompakt):** Başlık "Liste". Alt satır: `Son alışveriş: 8 gün önce · 642 TL`. Sağda eşin baş harfi avatarı + 6dp senkron noktası (gri = sadece cihazda, yeşil = senkron; asla hata dialogu yok). Taşma menüsü → Geçmiş, Ayarlar.

**Gövde:** Tek bir dikey liste, bölüm başlıklı. Sekme YOK, yatay sayfalama YOK, iç içe scroll YOK.

- **Bölüm 0 — "Her zamankiler":** Yeni listeye otomatik eklenen sabitler. **%70 opaklıkta** ve 12dp raptiye ikonuyla — kullanıcı ekledikleri satırlardan görsel olarak daha hafif olmalı ki "uygulama benim ağzıma laf koydu" hissi vermesin. Maksimum 12 satır.
- **Bölüm 1..n — Reyon grupları:** Başlık 13sp, muted, öğe sayısıyla. Sıra: Meyve-Sebze / Fırın-Ekmek / Süt-Kahvaltılık / Et-Şarküteri / Temel Gıda / Konserve-Sos / Atıştırmalık / İçecek / Dondurulmuş / Temizlik / Kişisel Bakım / Diğer. Boş bölüm hiç çizilmez.
- **Son bölüm — "Alındı (12)":** Üstü çizili öğeler, en son işaretlenen en üstte, 3 satıra katlanmış + "Tümünü göster".

**Satır anatomisi (56dp; ikinci satır varsa 68dp):**
`[onay kutusu 24dp] [adet rozeti "2x" veya "1 kg" — sadece adet 1 değilse] [AD 17sp medium] ......... [FİYAT ÇİPİ, kendi 44dp dokunma hedefi] [eşin baş harfi — sadece eş eklediyse]`

**İkinci satır (13sp, %60 opaklık) — şunlardan SADECE BİRİ, bu öncelikle:** (1) fiyat ipucu, (2) satır bir öneriden geldiyse gerekçesi ("12 gündür almadın"), (3) kullanıcı notu, (4) hiçbir şey. Asla iki satır metadata olmaz.

**Fiyat ipucunun üç veri durumu — üçünü de tasarla:**
- **0 gözlem:** İkinci satır yok, çip yok, hiçbir şey yok. Asla "fiyat yok" yazma.
- **1 gözlem:** `son 38,50 TL · A101 · 12 gün önce`
- **2+ gözlem, aynı ambalaj:** `38,50 → 42,00 TL` + delta çipi `%9` + anlamsal ok (kırmızı yukarı / yeşil aşağı) + sağ kenarda son 8 gözlemin 24×16dp sparkline'ı.
- **Ambalaj küçüldüyse (shrinkflation):** Trend bastırılır, yerine `900g → 800g · aynı fiyat` yazılır. Ambalaj küçülmesi asla fiyat düşüşü gibi görünmemeli.
- **Başka markette ucuzsa:** İnce accent çipi `A101'de 34,90`. **Liste başına en fazla 3 tane** — üstü reklam yüzeyine dönüşür.

**Sepet tahmini (öneri şeridinin üstünde 32dp sabit satır):** `Tahmini sepet: ~640 TL (18 ürün)`. Satırların %60'ından azının fiyatı biliniyorsa %40 opaklık ve "~" öneki; %30'un altında hiç gösterilmez.

**Öneri şeridi (girişin hemen üstünde, 40dp çipler):** En fazla 5 çip. Gerekçe çipin içinde: `Yumurta · 14 gün oldu`, `Ekmek · her seferinde`. Liste 6 öğeyi geçince tek bir `+3 öneri` hapına katlanır. Asla animasyon, asla badge, asla nokta.

**Hızlı ekleme girişi (alta sabit):** Placeholder `Ne lazım?`. Otomatik tamamlama listesi alanın **üstüne** açılır ve **alfabetik değil, öneri skoruna göre** sıralanır — her satırda ürünün son ödenen fiyatı küçük gri metin olarak.

**Pano yapıştırma:** Panoda 3+ satır varsa girişin üstünde tek dokunuşluk çip: `Panodaki 7 satırı ekle`.

**Alt birincil buton (moda göre):** Planlama → `Alışverişe çıkıyorum` · Alışveriş → `Alışverişi bitir (12/18)`.

**Boş durumlar (üçünü de tasarla):**
- *1. gün, kurulum yapıldı:* Liste zaten dolu. Kapatılabilir üst kart: `İlk listen hazır. Her zaman aldıklarınızı ekledik — gerekmeyeni sil, eksiği ekle.`
- *1. gün, kurulum atlandı:* Ortada 3×4 grid, 12 tek-dokunuşluk çip (ekmek, süt, yumurta, peynir, zeytin, domates, salatalık, soğan, çay, yoğurt, makarna, deterjan) + ikincil buton `WhatsApp'tan listeni yapıştır`. **İllüstrasyon yok, üzgün yüz yok, sihirbaz yok.**
- *Döngü ortası (uygulamanın hayatının çoğunu geçirdiği hal):* `Liste boş. Son alışveriş 3 gün önce, 642 TL.` + öneri şeridi görünür kalır + tek hayalet buton `Geçen sefer aldıklarını ekle`. Bu hal ölü hissettirmemeli.

**Çevrimdışı:** Banner yok, dialog yok. Senkron noktası grileşir ve header alt satırında `Çevrimdışı · değişiklikler kaydediliyor` belirir. Her aksiyon çalışmaya devam eder.

**ALIŞVERİŞ SONRASI ÖZET KARTI:** Bir alışveriş kapandıktan sonra listenin en üstünde **bir kez** görünen, kapatılabilir kart. Tutar **36sp Fraunces 600** ile — uygulamanın duygusal karşılığı ve ekran görüntüsü alınacak an burası, tipografi bunu hak ediyor.
> `Bu alışveriş 642,50 TL`
> `Geçen sefer 601,00 TL (18 gün önce) · En çok artan: Ayçiçek yağı %14`

Bu bir ekran değil, karttır. Ayrı bir "özet ekranı" tasarlama.

**ALIŞVERİŞ MODU (aynı ekranın farklı hali — ayrı bir ekran olarak tasarlama):**
- Satır yüksekliği 56/68dp → **72dp**, ürün adı 17sp → **20sp weight 600–700**.
- Ekran uyanık kalır.
- Reyon sırası **donar** — hareket eden bir başparmağın altında liste asla yeniden sıralanmaz.
- İkincil metadata (not, kategori etiketi, sparkline, öneri şeridi) tek bir `Detay` affordance'ının arkasına katlanır. Ekranda 10–11 değil **7–8 satır** görünür.
- Kontrast bir kademe artar: surface #FBF7F2 → #FFFFFF, satır container'ları 1.5dp kenarlık kazanır.
- **Tüm birincil aksiyonlar ekranın alt %40'ına iner** — yatay floating toolbar: ekle · son işaretlemeyi geri al · filtre · bitir. Her biri minimum 56dp.
- İşaretli hal kol mesafesinden **dolgulu bir şekil** olarak okunmalı — gri metnin yanındaki küçük tik değil.
- İşaretlemede haptik onay. **İşaretlemede snackbar YOK** (bir gezide 20 işaretleme var); geri alma "Alındı" bölümünden tek dokunuş.

---

#### EKRAN 2 — EKLE (Liste üzerinde modal sheet, ayrı destinasyon değil)

Kullanıcının aradığı kelimeyi bilmediği durum için. Sheet olarak tasarla ki liste arkada görünsün.

Üstte arama alanı · 3 sütunlu kategori kutucuk grid'i (her kutucukta kategori adı + hanenin oradaki ürün sayısı) · kategori içinde iki sütunlu ürün çipleri, öneri skoruna göre sıralı, listede olanlar işaretli ve pasif · her çipte son ödenen fiyat küçük metin olarak · en altta `Nadir aldıkların` bölümü · her zaman görünen kaçış yolu: `+ "kuru kayısı" ekle`.

Dokunma = ekle, sheet açık kalır, başlıkta `3 ürün eklendi` sayacı. **Boş arama sonucu:** tek satır `+ "X" ekle` — form yok, kategori seçici yok.

---

#### EKRAN 3 — EKSİK OLABİLİR (evden çıkmadan önceki son kontrol)

Sadece Liste'deki `Alışverişe çıkıyorum` butonundan açılır. Otomatik tetiklenmez.

Başlık canlı sayaçla: `Eksik olabilir (4)`.

- **Bölüm 1 — "Geçen sefer unuttun":** Onay kutusu **varsayılan AÇIK**, turuncu sol şerit. Uygulamanın en yüksek sinyalli satırları.
- **Bölüm 2 — "Her zamankiler":** Listede olmayan sabitler. Varsayılan **AÇIK**.
- **Bölüm 3 — "Bitmiş olabilir":** Skorlanmış tahminler. Varsayılan **KAPALI** — uygulamanın tahmin yürüttüğü tek bölüm bu, tahminler varsayılan-açık muamelesi görmez.

**Her satır düz Türkçe bir gerekçe taşır** (13sp, muted): `son 12 alışverişin 11'inde aldın` / `12 gündür almadın, normalde 10 günde bir` / `genelde 4 alışverişte bir alıyorsun` / `geçen sefer unutmuştun`. Sağda son ödenen fiyat.

Toplam **en fazla 8 satır**. Alt bar: `[Ekle (4)]` birincil + `[Boşver]` metin butonu.

**Boş durum = EKRAN HİÇ AÇILMAZ.** Hiçbir şey uygun değilse doğrudan alışveriş moduna girilir ve 2 saniyelik toast çıkar: `Liste hazır, eksik görünmüyor.` Boş bir kontrol listesi göstermek kullanıcıya butonun değersiz olduğunu öğretir. Bu, uygulamadaki en önemli boş-durum kararı.

---

#### EKRAN 4 — ALIŞVERİŞİ BİTİR (tek ekran, iki mod)

Uygulamadaki en önemli ekran — verinin dürüst kalmasını sağlayan yer.

**Adım 0 — seçim sheet'i (ekran değil):** `[Fiş çek]` birincil · `[Fişsiz bitir]` ikincil · `[Vazgeç]`.

**Kamera bir sistem yüzeyi**, tasarlanmış bir ekran değil. Sadece overlay rehberi: kenar çerçevesi + `Fişin tamamı kadraja girsin. Uzunsa 2 kare çek.`

**Fotoğraf çekimi asla bloklamaz.** Fotoğraf yerel kaydedilir, kuyruğa alınır, **alışveriş anında kapanır**. Kullanıcının arkasında bekleyeceği bir spinner asla olmaz. Market kasasında kuyrukta bekleyen biri tasarlıyorsun.

**MOD A — FİŞ KONTROL:**

*Header:* Algılanan market adı + fiş tarihi + okunan TOPLAM + aritmetik kontrol çipi. Yeşil `Toplam tutuyor` veya amber `Toplam 4,20 TL tutmuyor`.

*Satır anatomisi:* `[eşleşen ürün adı 17sp] [adet] [fiyat, sağa dayalı, dokunulabilir]` ve altında **ham OCR metni 12sp gri** (`YMRT KLS 10LU`). Ham metin pazarlık konusu değil — kullanıcının kağıt fişi tekrar okumadan eşleşmeyi doğrulama yolu bu.

*Üç görsel ağırlık:* (1) **Eşleşti** — düz satır, renk yok, aksiyon yok. (2) **Yeni ürün** — amber sol şerit, `Yeni: Tam Buğday Ekmek`, satır içi anahtar **varsayılan AÇIK** (yeni olmak normaldir, dokunuş gerektirmez). (3) **Emin değil** — amber sol şerit, en iyi tahmin + chevron.

*Alt bölüm — "Listede vardı, fişte yok (3)":* Katlanabilir. Satır başına üç buton: `[Aldım] [Gerekmedi] [Unuttum]`.

**MOD B — FİŞSİZ MUTABAKAT:** (a) `Listede vardı, işaretlemedin` aynı üç butonla · (b) `Bunları da aldın mı?` — listede olmayan en olası 5 ürün, tek dokunuşluk anahtarlar + `+ Başka bir şey` serbest metin satırı.

**Düzeltme sözleşmesi — her hata sınıfı ≤3 dokunuş (bunları tasarla):**
- *Yanlış eşleşme:* Satıra dokun → aday sheet'inden doğru ürünü seç. **2 dokunuş.** Sheet: en iyi 3 bulanık aday, sonra `Yeni ürün olarak ekle`, sonra arama alanı.
- *Yanlış fiyat:* Fiyata dokun → sayısal klavye **değer önceden dolu ve seçili** açılır → yaz → sonraki satıra dokununca kapanır. **Uygulamadaki tek klavye bu.**
- *Ürün olmayan satır (POŞET, İNDİRİM, KDV):* Sola swipe = `ürün değil`. **1 jest**, ve o zincir için kalıcı olarak hatırlanır.
- *OCR satır atladıysa:* Listenin altında `+ Eksik satır ekle`.

**Boş durum = EKRAN AÇILMAZ.** Fiş temiz okunduysa ve düşük güvenli satır yoksa alışveriş kendiliğinden kapanır, snackbar: `Alışveriş kaydedildi · 18 ürün · 642,50 TL. [Fişi gör]`

**Hata — okunamadı:** `Fiş okunamadı.` + `[Yeniden çek] [Elle sadece toplam gir] [Fişsiz devam et]`. Alışveriş verisi asla kaybolmaz çünkü parse başlamadan önce kapanmıştı.

---

#### EKRAN 5 — ÜRÜN DETAYI (fiyat geçmişi — genişleyebilen bottom sheet)

Liste satırındaki fiyat çipinden tek dokunuşla açılır. Reyondayken listeyi terk etmiş hissettirmemeli, o yüzden push edilen ekran değil sheet.

- **En üstte manşet cümlesi, 24sp Fraunces 600, düz Türkçe:** `Süt 32 TL → 41 TL · son 3 ayda %28 arttı`. Okunacak ve ekran görüntüsü alınacak şey grafik değil bu cümle; grafik cümleyi açıklar. Fraunces'in dört kullanım yerinden biri bu.
- **Çizgi grafik** — her gözlem bir nokta, artı minimum referans çizgisi ve ortalama referans çizgisi.
- **Aralık seçici:** `1 ay / 6 ay / 1 yıl`. **"1 hafta" YOK** — 10 günlük alışveriş temposunun haftalık çözünürlüğü yoktur.
- **Her noktada ambalaj boyu etiketli**, artı birim seçici (`paket fiyatı / kg-lt fiyatı`).
- **"Nerede ucuz" bloğu:** Yakın zincirlerin güncel fiyatları, en ucuz üstte, `saat 14:20 itibarıyla` tazelik damgası. Çevrimdışıyken **sessizce yok olur** — asla hata metni gösterme.
- **"Alım geçmişi":** tarih, market, adet, ödenen fiyat.
- **Tempo satırı:** `Genelde 10 günde bir alıyorsun · son alım 6 gün önce.`
- Altta iki anahtar: `Her zamankilere ekle` ve `Bunu önerme`.

**Boş durumlar:** *0 gözlem* → grafik yok, manşet yok, sadece `Fiş çektikçe burada fiyat geçmişi birikecek.` (tek satır, illüstrasyon yok). *1 gözlem* → manşet `Son ödediğin: 38,50 TL · A101 · 12 gün önce`, grafik yok, yüzde yok. **Tek noktadan trend bir yalandır.**

---

#### EKRAN 6 — GEÇMİŞ

Uygulamanın en ucuz ekranı, düz bir liste. Yanlış okunmuş bir fişe geri dönmenin tek yolu olduğu için var.

Header'da son 6 alışveriş toplamının 6 çubuklu mini grafiği (eksen yok, açıklama yok, etkileşim yok). Satırlar: tarih, market adı, ürün sayısı, toplam, fiş durumu ikonu (fiş var / fiş yok / işleniyor / kontrol bekliyor).

**Boş:** `Henüz alışveriş yok. İlk listeni tamamladığında burada görünecek.` Tek satır, illüstrasyon yok, CTA yok. **Tek alışveriş varsa mini grafik gizlenir** — tek çubuklu grafik grafik değildir.

---

#### EKRAN 7 — AYARLAR (sıfır tasarım yatırımı, düz liste)

Bölümler: **Hane** (ad, 6 karakterlik katılma kodu — dokun kopyala, üyeler, çık) · **Her zamankiler** (9/12, sıralanabilir, silinebilir) · **Önerilmeyenler** (her biri tek dokunuşla geri alınabilir — bu listeyi görünür kılmak, kalıcı reddin kara delik hissi vermesini engelleyen şey) · **Mağazalar** · **Gizlilik** (düz dil + `Verilerimi sil`).

---

#### EKRAN 8 — KURULUM (ilk açılış, 3 adım, bir daha görünmez)

Var olma sebebi tek: uygulamanın 15. gezide değil **3. gezide** akıllı hissetmesi.

- **Adım 1 — Hane:** `[Yeni hane oluştur]` veya `[Kodla katıl]`. E-posta + 6 haneli kod.
- **Adım 2 — önemli olan:** `Her alışverişte aldıklarınız` başlığı altında ~40 hazır Türk market ürününün grid'i. Çoklu seçim, arama gerekmez, iki ekrandan fazla kaydırma yok.
- **Adım 3 — Tempo:** Tek sıra çip: `Haftada 1 / 10 günde bir / 2 haftada bir / Belirsiz`.
- Adım 2 ve 3'te `[Atla]` görünür.

Bitirince **zaten dolu** bir Liste ekranına düşülür.

### 6. NE ÜRETMENİ İSTİYORUM

1. **Design system temeli:** Renk token'ları (ışık + karanlık, rol adlarıyla), tipografi ölçeği, spacing skalası (8dp grid), köşe yarıçapları, gölge/katman kuralları, hareket eğrileri. Token adları Compose'a birebir taşınacağı için rol bazlı olsun (`surface`, `surfaceVariant`, `primary`… — `blue500` gibi değil).
2. **Bileşen kütüphanesi:** Liste satırı (tüm varyantları: normal / sabit / işaretli / eş-eklemiş / fiyat ipuçlu / alışveriş modu), fiyat çipi, delta çipi, sparkline, öneri çipi, kategori başlığı, kategori kutucuğu + iki-harf fallback'i, hızlı ekleme girişi, floating toolbar, bottom sheet, snackbar, boş durum bloğu, fiş kontrol satırı (3 hali).
3. **Ekranlar:** Yukarıdaki 8 ekran, her biri için dolu + boş + karanlık mod.
4. Her bileşende **basılı / devre dışı / odaklı** hallerini açıkça göster (ripple olmadığı için bunlar tanımlanmak zorunda).

Metinlerin hepsi gerçek Türkçe olsun, örnek verilerde gerçekçi Türk market ürünleri ve TL fiyatları kullan (Pınar Süt 1L, Tam Buğday Ekmek, Yumurta 10'lu, Ayçiçek Yağı 5L gibi).

## ⬆️ BURAYA KADAR KOPYALA

---

## Font notları (prompt dışı — kod tarafı için)

**Fraunces'i variable font olarak bundle etme, statik instance olarak bundle et.** CMP'de `FontVariation.Settings` (opsz / SOFT / WONK gibi eksenler) iOS'ta güvenilir değil ve hata vermeden sessizce varsayılan instance'a düşebiliyor. Fraunces'in varsayılanı `opsz 14, SOFT 0, WONK 0` — yani küçük punto için optimize edilmiş, yüksek kontrastlı bir kesim. 44sp'de bu yanlış görünür ve neden yanlış göründüğünü anlamak saatler alır.

Çözüm basit: Fraunces'i `opsz 72, SOFT 30, WONK 0, wght 600` ayarında **tek bir statik TTF** olarak instance'la (fontTools `varLib.instancer` ile) ve onu bundle et. Display yüzü zaten tek ağırlığa ihtiyaç duyuyor, yani bundle boyutu da küçülür.

Plus Jakarta Sans'ta sadece `wght` ekseni var — variable eksenler içinde en iyi desteklenen eksen bu, variable olarak bundle edebilirsin. Yine de ilk hafta gerçek bir iPhone'da doğrula: hem ağırlık geçişlerini hem de `tnum` özelliğinin gerçekten uygulandığını. Skia desteklemediği OpenType özelliklerini sessizce yok sayabiliyor — bu yüzden fiyat sütununu sabit genişlik + sağa dayalı tasarlattık, `tnum` çalışmasa bile düzen ayakta kalsın diye.

**Beğenmezsen tek blokluk alternatifler** (promptta sadece Tipografi bölümünü değiştir, gerisi aynı kalır):
- **Tamamı Plus Jakarta Sans** — display'de 800 ağırlık + −2% harf aralığı. En güvenli, en tutarlı, tek font bundle'ı. Karakteri az ama hiçbir yerde yanlış görünmez.
- **Plus Jakarta Sans + Instrument Serif** — Instrument Serif tek ağırlıklı, daha keskin ve daha "editoryal" bir display serif. Fraunces'ten daha az yumuşak, daha çok dergi kapağı. Sıcak Kiler paletiyle yine iyi durur, sadece daha az oyuncu.

## Takip promptları (design system geldikten sonra sırayla)

1. `Liste ekranını alışveriş modunda, 72dp satırlarla, floating toolbar ile ve karanlık modda üret. Yan yana normal modla karşılaştırmalı göster.`
2. `Fiş kontrol ekranını 17 satırlık gerçek bir A101 fişiyle üret: 14 satır temiz eşleşmiş, 2 satır "emin değil", 1 satır "yeni ürün". Ham OCR metinleri gerçekçi Türk fişi kısaltmaları olsun.`
3. `Ürün detayı sheet'ini üç veri durumunda üret: 0 gözlem, 1 gözlem, 9 gözlem (biri ambalaj küçülmesi içeren).`
4. `Tüm boş durumları tek bir sayfada topla — 8 ekranın boş hallerini yan yana göster.`
5. `Renk token'larını ve tipografi ölçeğini Compose'a taşınmaya hazır biçimde, rol adlarıyla bir spec sayfası olarak çıkar.`
