# 37 — Planlamada işaretleme, ve satırın markete bağlanması

**23 Ağustos 2026.** Kullanıcının iki cümlesi, ikisi de aynı yere bakıyor:
**planlama modu ne için var.**

> *"Ben liste yaparken neden alındı/alınmadı var ki? Zaten alışverişe çıkıyorum
> dediğimde orada işaretleme yapıyorum."*

> *"Ben liste yaparken hangi markete gittiğimi de belirtebilmeliyim. Bazen
> mesela **BİM'e gidiyorumdur, 10 tane şeyi oradan almak için giderim, 2-3
> tanesini de A101'den alacağım** diye işaretlerim — bu şekilde de tahmini
> sepet hesaplanabilir."*

---

## S1 — Planlama modunda işaret ne işe yarıyor?

Bugün her satırda onay hedefi var ve işaretlenen satır **"Alındı"** bölümüne
iniyor. Kod bunu bilinçli yazmış (`ListState`): *"iki modda da doğru:
planlamada işaretli satırlar `taken`'a iner."*

Kullanıcının itirazı davranışsal: **planlama listeyi kurma anı**, bir şeyi
işaretlemek için değil. İşaretleme reyonda oluyor.

Ve bedeli somut: bu turlarda cihazda test yaparken **yanlışlıkla yapılan her
dokunuş bir satırı "Alındı"ya taşıdı** ve listeyi kirletti. Planlama modunda
satırın tamamı işaretleme hedefi — yani en büyük hedef, en az istenen işe
bağlı.

- **(a)** Planlamada onay hedefi **yok**; satır dokunuşu Ürün Detayı'nı açar
  (uzun dokunuş zaten oraya gidiyordu, kısa dokunuş boşa gidiyor).
  *"Alındı" bölümü de yalnızca alışveriş ve sonrası modlarda.*
- **(b)** Onay hedefi kalsın ama **anlamı değişsin**: planlamada *"bu zaten
  evde var"* demek olsun — satır listede kalır, soluklaşır, tahmine girmez.
- **(c)** Bugünkü hâl doğru: kullanıcı evde de işaretlemek isteyebilir
  *(o zaman gerekçesi yazılmalı, çünkü kullanıcı tersini söylüyor)*.

---

## S2 — Satır hangi markete ait? *(kullanıcının asıl istediği)*

Bu, **karar 97'nin ertelediği şey değil.** 97 *"tahmini seçilen markete göre
hesapla"* idi ve **veri yoğunluğu** gerekçesiyle ertelendi (ürün başına ortalama
zincir > 1,5; bugün 1,0–1,4).

Kullanıcının istediği başka bir şey: **beyan.** Uygulama en ucuzu bulmuyor;
kullanıcı *"şunlar BİM'den, şunlar A101'den"* diyor. Bu bir **organizasyon**
özelliği ve değeri fiyat kapsamına bağlı değil — reyonda "şimdi neredeyim,
buradan ne alacağım" sorusunu cevaplıyor.

Yani 97'nin erteleme gerekçesi bu isteği kapsamıyor.

### Bugünkü veri

| | |
|---|---|
| `trip.storeId` | **var, kullanılmıyor** — KDoc: *"alışverişe çıkarken nereye gidileceği her zaman belli değil"* |
| `trip_line`'da market alanı | **yok** |
| Gözlem görülen zincir sayısı | **2** (BİM, A101) — dokuz tohumdan yedisinde hiç gözlem yok |
| Ürün başına ortalama zincir (karar 115) | **1,0–1,4** |

Yani bugün "A101'den alacağım" diye işaretlenen bir satırın A101 fiyatı
**çoğunlukla bilinmiyor** — tahmin yine son fiyata düşer. Özellik yine de
çalışır (liste bölünür, reyon sırası korunur), ama **tahmini bugün
değiştirmez**.

### Sorular

**S2.1 — Hedef market gezi düzeyinde mi, yoksa yalnız satır düzeyinde mi?**

- **(a)** Gezi düzeyinde bir hedef (*"bugün BİM'e gidiyorum"*) + satır düzeyinde
  **istisna**. Kullanıcının cümlesi birebir bu. `trip.storeId` zaten var.
- **(b)** Yalnız satır düzeyinde: her satır kendi marketini taşır, gezi hedefi
  yok. Daha basit ama on satırı tek tek işaretletir.
- **(c)** Gezi hedefi yok, satırlar **bölümleniyor**: liste market başına
  gruplanır (reyon sırası ikinci kırılım olur).

**S2.2 — Bu işaret listeyi nasıl gösteriyor?**

Bugün liste **reyon sırasına** göre bölümlü (karar: "market gezme sırası") ve
bu sıra ekranın bütün işi. Market ikinci bir kırılım getiriyor.

- **(a)** Market **bir rozet/işaret**, bölüm değil: satır reyon bölümünde kalır,
  hedeften farklıysa küçük bir işaret taşır *(kimlik bandı dolu — karar 103'ten
  sonra rozet, ad, avatar, raptiye var)*.
- **(b)** Liste market başına **bölünür**, içinde reyon sırası korunur.
- **(c)** Alışveriş modunda **yalnız hedef marketin satırları** görünür,
  ötekiler ayrı bir bölüme iner.

**S2.3 — Tahmin ne diyor?**

Karar 95 satırın sağına kaynağı yazıyor: *"BİM fiyatlarıyla · 4/7"*.

- **(a)** Hedef market seçiliyse tahmin **o marketin fiyatlarını** kullanır,
  bulunamayan satır son fiyata düşer ve sayaç bunu söyler.
- **(b)** Tahmin değişmez (son fiyatlar), market yalnızca organizasyon.
- **(c)** İki tutar: *"BİM'de ~X · A101'de ~Y"* — ama satır tek satır ve
  karar 95 onu bir cümleye indirmişti.

⚠ **(a) seçilirse karar 97 fiilen açılmış olur** — ama farklı bir kapıdan:
kullanıcı marketi **seçiyor**, uygulama **aramıyor**. 97'nin 1,5 eşiği "hangi
market daha ucuz" sorusu içindi; bu soru sorulmuyor.

**S2.4 — Market nereden seçiliyor?**

Etiket çekiminde market seçici zaten var (karar 59, "market yapışkanlığı").

- **(a)** Liste başlığından: bugün *"Son alışveriş: dün · ~1.505 TL"* yazan
  satır, hedef seçiliyse *"BİM'e gidiyorsun"* der.
- **(b)** "Alışverişe çıkıyorum" butonuna basınca sorulur — ama kullanıcı
  **liste yaparken** belirtmek istiyor, çıkarken değil.
- **(c)** Satırın kendi Ürün Detayı'ndan (istisna için), gezi hedefi ayrıca.

---

## S3 — İki soru birbirine bağlı

S1 planlamada **işareti kaldırmayı** öneriyorsa, S2 planlamaya **yeni bir
işaret** getiriyor. İkisi aynı yere bakıyor: *planlama modunda satıra
dokunmak ne demek?*

Bir olasılık ikisini birden çözüyor: **planlamada dokunuş = markete ata**,
alışverişte dokunuş = işaretle. Aynı jest, moda göre farklı fiil — ekranın
zaten "bir mod makinesi" olması bunu doğal kılar.

Bu bir öneri, karar değil; tasarımın kendi çözümü daha iyi olabilir.

---

## Bu turda kodda yapılanlar (cevap beklemeden)

Kullanıcının aynı mesajda bildirdiği iki kusur, ikisi de doğrulandı ve
düzeltildi:

1. **Ürün Detayı sheet'inin altı görünmüyordu.** İçerik bir `Column`dı ve
   **kaydırması yoktu** — sheet'ten uzun olan her şey sessizce kırpılıyordu.
   Süt satırında uçtan uca görüldü: "Listeden çıkar" ekranın dışında kalıyordu.
   *(Geçen tur `skipPartiallyExpanded` eklendi, sheet tam açılıyor — ama tam
   açık bir sheet de ekran kadar. İki ayrı kusur, ikisi de gerekliydi.)*

2. **Silme jesti yarım kalıyordu.** Eşik açılan alanın %60'ıydı, yani yarım bir
   çekiş bazen siliyor bazen geri dönüyordu ve hangisinin olacağı parmağın
   nerede durduğundan belli değildi. Artık **sonuna kadar çekmek** siliyor,
   daha azı kapanıyor — kullanıcının istediği birebir bu.

⚠ Karar 37'nin cümlesi *"100dp'lik alan çıkıyor ve içinde tek kelime var:
Sil"* — bu, alanın **açılıp kalması ve "Sil"e dokunulması** gibi de okunabilir.
Kod hiçbir zaman öyle çalışmadı ("Sil" dokunulabilir değil). Kullanıcı
tam-çekiş istedi ve o uygulandı; tasarım tersini istiyorsa jest yeniden
yazılır.
