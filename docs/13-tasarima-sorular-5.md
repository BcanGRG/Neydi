# Tasarıma sorular — beşinci tur

**16 Ağustos 2026.** Dördüncü tur kapandı (karar 32–35), ikon seti Phosphor'a
taşındı, biçimlendiriciler girdi. Bu tur **iki kararın birbiriyle çeliştiği**
bir yerden ve kayıt denetiminden çıkan üç küçük maddeden oluşuyor.

Biçim aynı: **tasarımın verdiği** → **gerçek** → **soru**.

---

## 1. Mağazalar eşiği ölü — karar 11 ile gezinme sözleşmesi birbirini yiyor

Bu turun asıl maddesi. Diğer üçü tazeleme işi; bu bir karar.

### Tasarımın verdiği — iki ayrı yerde, iki ayrı şey

**Gezinme sözleşmesi, eşik tablosu:**

| Yüzey | En az | Altında |
|---|---|---|
| Ayarlar · Mağazalar | **1 gözlem** | Bölüm çizilmez |

**Karar 11 (pivotta revize):** *"Yedi zincir tohumlanır, market etiket
çekerken seçilir, son seçilen yapışkandır."*

Bu iki cümle aynı anda doğru olamıyor.

### Gerçek

Kod eşiğe hiç bakmıyor:

```kotlin
// SettingsScreen.kt
if (state.stores.isNotEmpty()) { ... }        // kaynağı storeDao.observeAll()
```

**Gözlem sayısı hiçbir yerde okunmuyor.** Üstelik karar 11'in tohumu
bootstrap'te yedi satır yazdığı için `stores` **asla boş dönmüyor** — yani
eşik her zaman aşılmış sayılıyor.

Somut sonuç: **sıfır gözlemi olan, yepyeni bir kurulumda** Ayarlar şunu
gösteriyor:

> **Takip edilen zincirler**
> BİM, A101, ŞOK, Migros, CarrefourSA, File, Tarım Kredi

Kullanıcı bu marketlerin hiçbirine gitmemiş, hiçbirinde etiket çekmemiş.

### Neden yalnızca bir tutarsızlık değil

**Karar 24 tam bu deseni reddetmişti.** Katılma kodu satırı için: *"üretilmiş
bir kod göstermek en pahalı hata türüydü — kullanıcı kodu eşine verir ve
karşılığında hiçbir şey olmaz."* Değer, henüz doğru olmayan bir şeyi vaat
ediyordu.

"Takip edilen zincirler" de aynı şeyi yapıyor: hiçbiri takip edilmiyor.

**Ayrıca bu satır bugün görsel olarak da kırıldı.** Yedi zincir adı elli
karakter ve satır etiketi harf harf alt alta aktı. Düzeltildi (adlar kendi
satırına indi), ama şunu not etmek gerekiyor: **bölüm sözleşmeye uygun
davransaydı satır hiç çizilmeyecek ve hata da hiç doğmayacaktı.**

### Bu arada karar 23'ün varsayımı da düştü

Karar 23 chevron'u kaldırırken gerekçe *"üç beş zincir tek satıra sığıyor,
adlar değerin içinde"*ydi. Tohum yüzünden ilk günden yedi zincir var ve tek
satıra sığmıyor.

**Kararı çevirmedim:** gerekçesi ayakta (satırın götüreceği bir ekran yok,
adlar hâlâ değerin kendisi, chevron hâlâ yok). Yalnızca geometri değişti —
adlar etiketin altına, tam genişliğe indi. Çerçevenin buna göre tazelenmesi
gerekiyor.

### Üç okuma

| | Ne olur | Bedeli |
|---|---|---|
| **(a)** Sözleşme haklı — bölüm ilk gözleme kadar gizlensin | Temiz kurulumda bölüm yok | Bugün gözlem üretebilen yüzey yok (E14/E15 gelmedi), yani bölüm uygulamadan **tamamen kaybolur**. Ayrıca "hangi marketleri seçebilirim" bilgisi de kaybolur — oysa tohumun varlık sebebi tam olarak o |
| **(b)** Kod haklı — eşik pivotla anlamını yitirdi | Tablo güncellenir | Eşik, mağazaların *fiş künyesinden doğduğu* dönemde yazılmıştı: o zaman mağaza ancak gerçek veriden sonra vardı. Tohum bu ontolojiyi değiştirdi |
| **(c)** Etiket yanlış — bölüm kalsın, başlık düzelsin | "Takip edilen" → doğru bir şey | En küçük değişiklik; yanlış olan tek şeyi düzeltiyor |

### Önerim: (c) + (b)

Bölüm kalsın, başlık *"Seçilebilen zincirler"* (ya da sizin seçeceğiniz doğru
bir ifade) olsun, eşik tablosundaki satır kaldırılsın.

Gerekçe: tohum, kullanıcı neyi seçebileceğini bilsin diye var. Bölümü
gizlemek karar 11'in kendi amacına ters düşer. Yanlış olan tek şey **iddia**,
o yüzden düzeltilecek tek şey de o olmalı.

### Sorular

1. **Bölüm gizlensin mi, kalsın mı?**
2. Kalacaksa **başlık ne olsun?**
3. Gözlemler gelmeye başlayınca **ayrım çizilecek mi** — gerçekten fiyat
   kaydedilmiş zincirler ile yalnızca seçilebilir olanlar farklı mı
   görünecek, yoksa liste tek mi kalacak?

---

## 2. Ekran 1 başlık örneği kendi tarih merdiveninizle çelişiyor

**Tasarımın verdiği.** Ekran 1 başlık örneği: *"Son alışveriş: **8 gün önce**
· 642 TL"*.

**Gerçek.** Aynı turda verdiğiniz tarih merdiveni `7–13 gün` aralığını
**"geçen hafta"**ya topluyor. Sekiz gün o aralığın içinde.

Kod merdiveni esas aldı — daha yeni, daha açık ve sayıyla tanımlı.

**Soru.** Örnek mi güncellenecek (*"geçen hafta · ~642 TL"*), yoksa başlık
merdivenin dışında mı kalacak? *(Not: örnekteki tutarın da `~` alması
gerekiyor — biçim kuralı istisnasız.)*

---

## 3. Karar 33'ün renkleri palete oturmuyor

**Tasarımın verdiği.** Karar 33: *"karanlık temada GRAD yerine renk kademesi
— metin `#E4D8C9`, ikon `#F5EDE6`."*

**Gerçek.** Sizin kendi `handoff/tokens.json`'ınız karanlık `textPrimary`yi
**`#F5EDE6`** yazıyor. Yani kararın "ikon" rengi, uygulamanın bugünkü *metin*
rengi. `#E4D8C9` ise palette hiç geçmiyor — tasarım dokümanlarında yalnızca
HTML çerçevesinin kendi zemin rengi olarak var.

**Ne yapıldı.** Çifti iki mutlak renk değil bir **ilişki** olarak okudum:
*ikon, yanındaki metinden bir kademe açık*. Uygulamanın karanlık rampası
`#C6B6A9` → `#F5EDE6` ve `#E4D8C9` tam ikisinin arasına düşüyor, o yüzden
ikincil metnin yanındaki ikon `#E4D8C9`'a çıkıyor. Birincil metnin yanındaki
zaten rampanın tepesinde — bir *kaldırma* tepenin üstüne çıkamaz.

Alternatif okuma, karanlık gövde metnini bir kademe indirmekti (`#F5EDE6` →
`#E4D8C9`). Sayılara birebir uyuyordu ama bir **ikon** kararından türetilen,
her ekranı etkileyen bir palet değişikliği olurdu; o yüzden yapmadım.

**Soru.** Okuma doğru mu? Yoksa karanlık `textPrimary` gerçekten `#E4D8C9`'a
mı inecek — bu durumda `tokens.json` da güncellenmeli.

---

## 4. İki dosya tazelenmeyi bekliyor

Kayıt denetiminden çıktı; ikisi de sizin tarafınızdaki dosyalar.

**4a · Boş durum atlası.** F11.12'de tazelediğinizi biliyoruz ama depodaki
kopya (`docs/tasarim/Neydi - Bos Durumlar.dc.html`) hâlâ pivot öncesi: dört
hedefli toolbar (`undo`, `filter_list`), "Fiş Kontrol" ibaresi,
`content_copy`'li katılma kodu ve **sıfır** adet `~` tutar. Yeni sürümün
paylaşılması yeterli.

**4b · `github.md` ekran haritası.** Tablo bir satırını
`ui/receipt/ReceiptCheckScreen.kt` ve `data/receipt/ReceiptGrouping.kt`'ye
bağlıyor; **ikisi de pivotta silindi**. Buna karşılık pivotla gelen
`ui/capture/CameraSurface.kt` tabloda hiç geçmiyor.

Bir de: **İkonografi dosyası** hiç paylaşılmamış. İçeriğini karar
metinlerinden okuyup elle işledik, yani eksiğimiz yok — ama `.dc.html`
kopyası gelecekse bilelim.
