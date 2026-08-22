# 29 — Tasarıma sorular (on dördüncü tur): eklemenin geri bildirimi

**22 Ağustos 2026.** Kullanıcı katalogdan ürün eklerken şunu bildirdi:

> *"Herhangi bir ürüne tıklıyorum ama **eklenmiş hissi vermiyor**. Orada UX
> açısından doğru olmayan şeyler var gibi… **eklendiğinde o hissiyat her yerde
> tam olmalı.**"*

On dört ekleme yolunu tek tek ölçtük. Dördü kusurdu ve **düzeltildi**; kalanı
tasarımın kararını bekliyor.

---

## Ölçüm: geri bildirim matrisi

| Kanal | Silme / işaretleme | **Ekleme** |
|---|---|---|
| Haptik | ✅ işaretlemede var (karar 3, 55) | **14 yolun 14'ünde yok** |
| Snackbar | ✅ satır silme, gözlem silme | yok *(ve olmamalı — karar 8)* |
| Toast | ✅ altı kullanım | yok |
| Satır vurgusu | — | **yok** |
| Sayaç | — | yalnız sheet başlığında |

**Silmek geri bildirimli, eklemek değil.** Uygulamanın elindeki üç güçlü kanalın
üçü de ekleme yolunda kullanılmıyor.

---

## Düzelttiklerimiz (sormadan, çünkü hepsi kusurdu)

1. **"«kuru kayısı» ekle" düğmesi eklemiyordu.** Sheet'in alt satırı arama
   boşken *"Kendim yazayım"*, doluyken *"«X» ekle"* yazıyor — ikisi de aynı
   davranışa bağlıydı: sheet kapanıyor, yazılan kelime **atılıyordu**.
   Ekleyecek fonksiyon yazılıydı ve hiç çağrılmıyordu.
2. **Karar 12'nin işareti üçüncü yüzeye hiç uygulanmamıştı.** Izgara kutucuğu
   ve arama çipi işaret + pasif alıyordu, *"Nadir aldıkların"* çipi almıyordu.
   Aynı çipe üç kez dokunmak ürünün **adedini** üçe çıkarıyor, sayaç ise
   *"3 ürün eklendi"* yazıyordu.
3. **Sayaç olmayan eklemeleri sayabiliyordu** — senkron artıyordu, ekleme ise
   sessizce başarısız olabiliyor.
4. **Eklenen satıra kaydırma yanlış satıra gidiyordu.** Dizin aynası alışveriş
   modunda bir fazla, alışveriş sonrası bir eksik sayıyordu.

---

## S1 — Sheet açıkken listeyi göremiyoruz; telafi ne olmalı?

Karar 64 sheet'i **tam açık** yaptı ve gerekçesinde *"«liste arkada görünsün»
hiçbir tasarım dosyasının kuralı değildi"* dedi. Doğru — ama sonucu şu:
**listeye ekleme, listeyi görmeden yapılan bir iş oldu.**

Geriye kalan iki telafi de zayıf:

- **Sayaç** ekranın karşı köşesinde, 14sp gri, **animasyonsuz** bir rakam.
  Kullanıcının parmağı ve gözü ortadaki ızgarada.
- **Kutucuğun sönmesi** (%38 opaklık) *"yaptın"* değil **"yapamazsın"** der.
  Ekleme onayı için yanlış görsel dil.

Ayrıca "eklenen satırı göster" telafisi mekanik olarak da çalışmıyordu:
kaydırma testi `LazyListState.layoutInfo`ya bakıyor ve **o sheet'i bilmiyor** —
sheet'in altındaki bir satır "tam görünür" sayılıyor. Yani telafi tam da
gerektiği yerde susuyor.

**Soru:** sheet açıkken ekleme onayı ne olacak?

| | Bugün | Seçenekler |
|---|---|---|
| Sayaç | üst köşede, sabit | **sayı değişince kısa bir vurgu** (ölçek/renk) |
| Kutucuk | %38'e söner | sönme yerine **dolgu + işaret** — "yapıldı" dili |
| Üçüncü kanal | yok | dokunulan kutucuktan sayaca giden kısa bir hareket? |

---

## S2 — Maket bir satır vurgusu çiziyor, sözleşme onu tanımıyor

Ekran 2 · *"Hızlı yazma · kök"* maketinde, az önce Enter'lanan satır
**`#F6E7D2` amber-krem dolgu** taşıyor; komşuları taşımıyor.

Bu vurgunun **ne süresi, ne sönme eğrisi, ne adı** hiçbir tasarım dosyasında
yok. Ve **uygulamadık**, çünkü:

> **Karar 57 amberi tek anlama kilitledi: eksik / emin değiliz.**

Aynı rengi "eklendi" için kullanmak amber'a üçüncü anlam yüklerdi — geçen hafta
"başka markette ucuz" çipi tam bu sebeple amberden kiremide taşınmıştı
(karar 43 + 57). Ayrıca `#F6E7D2` başka bir işte daha kullanılıyor: kategori
kutucuğunun yedek zemini.

**Soru:** hangisi geçerli?

- **(a)** Vurgu kalsın, **rengi değişsin** — hangi token? (`success` yeşili?
  `surfaceVariant` bir kademe koyu?)
- **(b)** Vurgu kalksın; maketin pikseli eski bir taslaktı.
- **(c)** Amber üçüncü anlamı alsın (karar 57 gevşesin).

(a) seçilirse **süre ve eğri** de gerekiyor. Bizim geçici seçimimiz kodda
`Motion.JUST_ADDED_MS = 1200` + 400ms sönme olarak duruyor ve **tasarımdan
değil** — sabitın KDoc'u bunu açıkça yazıyor.

---

## S3 — Ekleme haptik almalı mı?

Karar 55 birebir: *"**Haptik üç olay sayar: işaretleme, çekim, kaydet.**"*
Ekleme listede yok — ve bu bir eksiklik değil, **sayılmış bir liste**. Bu
yüzden eklemedik.

Ama aynı defterin başka bir satırı şunu diyor (karar 3): *"İşaretlemede haptik
onay var, snackbar yok — **bir gezide 20 işaretleme oluyor**."* Yani haptik,
**sık tekrarlanan ve görsel onayı zayıf** olan eylem için seçilmiş. Sheet'ten
ekleme tam olarak o tarife uyuyor: bir oturumda 5–10 kez oluyor ve görsel onayı
(yukarıda ölçtük) zayıf.

**Soru:** haptik **dört** olay mı sayacak? Sayacaksa yalnız sheet'ten mi
(listenin görünmediği yer), yoksa her ekleme yolundan mı?

---

## S4 — Toplu eklemeler kaç satır eklediğini hiç söylemiyor

İki yol var ve ikisi de sessiz:

- **Pano yapıştırma** — 12 satırlık bir listeyi yapıştırınca hiçbir şey
  yazmıyor. Sayı hesaplanıyor ve **atılıyor**.
- **"Geçen sefer aldıklarını ekle"** — aynı; üstelik sıfır satır eklendiyse
  ekranda hiçbir şey olmuyor.

Toast'ın kendi kuralı bu vakayı **zaten kapsıyor**: *"yalnızca «oldu bitti,
dokunacak bir şey yok» olaylarında"*. Ama gezinme sözleşmesi altı kullanımı
tek tek sayıyor ve yedincisini biz eklemedik.

**Soru:** `"12 satır eklendi"` toast'ı yedinci kullanım olarak açılsın mı?
Açılırsa metin birebir ne olacak?

---

## S5 — Aynı ürünü ikinci kez eklemek: adet artıyor, kullanıcı görmüyor

`ListRepository.add` aynı ürün için yeni satır açmıyor, **adedi artırıyor**.
Doğru davranış — ama görünmüyor:

- Satır yerinden kıpırdamıyor.
- Tek görsel değişiklik miktar rozeti; ve `1 adet` için rozet **hiç
  çizilmiyor**, yani birinci eklemede rozet yok, ikincisinde birden `2x`
  beliriyor. Animasyon yok.

Sheet'ten bu artık imkânsız (üç yüzeyin üçü de işaretli/pasif). Ama kök
alandan, öneri çipinden ve panodan mümkün.

**Soru:** adet artışı ekleme ile aynı geri bildirimi mi almalı, yoksa **kendi**
işaretini mi? (Rozetin belirmesi bir geçişle mi olmalı?)

---

## Değişecek yerler

S1 `AddSheet.kt` başlık bloğu + `DiscoveryTile`, S2 `ListItemRow` + `Motion.kt`,
S3 `ListViewModel.signalAdded` (tek yer — bütün yollar oradan geçiyor),
S4 `App.kt`'nin toast kanalı, S5 `ListItemRow`'un miktar rozeti.
