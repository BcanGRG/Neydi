# 39 — 22. tur kodlandı: kodun tek başına verdiği üç karar ve iki ayna çelişkisi

**24 Ağustos 2026.** Kararlar **121–126'nın altısı da** koda indi ve cihazda
doğrulandı (R58N81SAZ1Y). 579 test yeşil, sıfır derleyici uyarısı, şema v8
değişmedi.

Bu rapor **yeni bir tur açmıyor**. Ölçülü olan her şey uygulandı; aşağıdakiler
kararların **kenarında kalan** ve kodun tek taraflı doldurmak zorunda kaldığı
boşluklar. Her madde önce **kodun ne yaptığını** söylüyor, çünkü uygulama
duruyor: tasarım aynı fikirdeyse yazılacak bir şey yok.

---

## S1 — Hedef yokken *"Nereden alınacak"* satırı çizilmiyor *(karar 126)*

Karar 117 hedefi boş bırakmayı **meşru** kıldı: *"Belli değil"* geçerli bir
cevap. Ama sapma kuralının (`deviatesFrom`) **ilk şartı hedefin varlığı** —
istisnanın istisna olabilmesi için bir kural gerekiyor.

İkisi birleşince kodun cevaplaması gereken bir hâl doğuyor: **hedef yokken
satır ne yazsın?**

**Kod bugün:** satırı **hiç çizmiyor**. Gerekçe: hedefsizken yazılan istisna
**hiçbir yerde** görünmezdi — ne bandın işaretinde (karar 118), ne alışverişin
bölümlemesinde (karar 125), ne başlık cümlesinde (karar 117). Yani dokunuş,
sonucu olmayan bir jest olurdu.

- **(a)** Böyle kalsın — hedef yoksa satır da yok.
- **(b)** Satır çizilsin, değer *"Belli değil"* yazsın; dokunuş yine çip
  ızgarasını açsın. ⚠ Seçilen zincir hiçbir yüzeyde görünmez.
- **(c)** Satır çizilsin ama **pasif** olsun (dokunma hedefi yok).

---

## S2 — Beyan yapışkanlığı *"Belli değil"*i hatırlamıyor *(karar 123)*

`lastDeclaredStoreId` yazıldı ve `lastTaggedStoreId`'den **ayrı** kaynaktan
besleniyor (`trip` ⟂ `price_observation`) — kararın istediği bu.

**Kod bugün:** yapışkanlık bir **öneri**, bir beyan değil. `trip.storeId`
yazılmıyor; yalnızca çip işaretli geliyor ve kullanıcı dokunana kadar başlıkta
hiçbir cümle çıkmıyor. Otomatik yazsaydık uygulama, kullanıcının söylemediği
bir cümleyi (*"BİM'e gidiyorsun"*) onun ağzından kurmuş olurdu.

⚠ **Boşluk:** kullanıcı bu gezide *"Belli değil"*i seçtiyse seçiciyi yeniden
açtığında **öneri gene işaretli gelir**. `trip.storeId` iki farklı hâli aynı
değerle yazıyor:

| Kullanıcının yaptığı | `trip.storeId` |
|---|---|
| Hiç seçmedi | `null` |
| *"Belli değil"* dedi | `null` |

Ayırmak üçüncü bir alan ister (`storeDeclaredAt` gibi bir damga).

- **(a)** Böyle kalsın — öneri her açılışta gelir, *"Belli değil"* bir kez
  daha dokunulur.
- **(b)** Ayrı damga eklensin; *"Belli değil"* dedikten sonra öneri susar.
- **(c)** Yapışkanlık yalnızca **yeni gezinin ilk açılışında** gelsin.

---

## S3 — POST_SHOPPING bir **liste modu** diye okundu *(karar 124)*

Denetim satırı iki modu yan yana yazıyor:

> SHOPPING: … listenin sonunda genişlemeyen "Alındı · 12/18" satırı.
> POST_SHOPPING: karar 116'nın katlanabilir bölümü, expand_more'lu.
> Sapan zincir bölümleri **her iki modda** bu bloğun üstünde.

**Kod bugün:** POST_SHOPPING'i *"listenin alışveriş sonrası hâli"* diye okudu.
Dayanağı son cümle: **sapan zincir bölümleri yalnızca listede var**, dolayısıyla
POST_SHOPPING bir liste modu olmalı — Bitir ekranı okunsaydı o cümle anlamsız
kalırdı.

Mod **saklanmıyor, türetiliyor**: planlamada işaretlenecek bir şey yok (karar
116), yani *"işaretli satır var"* zaten *"alışveriş yapıldı"* demek.

⚠ **Bu okuma üç testi tersine çevirdi.** Dosya bir tur boyunca *"işaretli satır
her iki modda yerinde kalır"* diyordu; o cümle yazıldığında mod sayısı ikiydi.
Kullanıcının şikâyeti (*"liste yaparken neden alındı/alınmadı var ki?"*)
korunuyor: planlamada bölüm hiçbir zaman doğmuyor.

- **(a)** Okuma doğru — yazılsın.
- **(b)** POST_SHOPPING **Bitir ekranı**dır; listede yalnızca sayaç kalsın,
  katlanabilir bölüm oraya taşınsın.

---

## Ayna çelişkileri *(soru değil, bildirim)*

1. **Ekran 1'in alışveriş çizimleri bayat.** Üç panelde hâlâ
   `Alındı (12)` + `expand_more` var; karar metni (github.md ve Compose Spec
   denetim satırı) alışveriş için `Alındı · 12/18` ve **chevron yok** diyor.
   Kod **karar metnini** izledi.
2. **Bölüm başlığı iki dosyada iki türlü.** Compose Spec'in 125 satırı
   `"A101'de · 2"`, aynı dosyanın 117–118 satırı `"A101'de (2)"` yazıyor.
   Kod adı ve sayıyı ayrı taşıyor; cihazda `A101'de 2` çiziliyor.
3. **Sapma ikonu iki ölçüde yazılı.** 117–118 satırı `storefront 13.dp`,
   karar 121 satırı `storefront 14dp` diyor. Kod **14dp** kullanıyor (maketin
   `font-size:14px`'i ölçüldü).

---

## Hâlâ cevapsız — 21. turdan devreden

⚠ **Sapma varken *"başka markette ucuz"* çipi bastırılıyor.** `docs/38` S1'in
içinde geçiyordu ama karar 121 yalnızca iki zincir adının ayrılmasını
cevapladı. Çip *"istersen A101'e uğra"* diyor, sapma ise kullanıcının *"zaten
A101'den alacağım"* kararı — kapanmış bir soruyu yeniden sormak, üstelik
92dp'lik çipi 50dp'lik işaretin yanına koyup 360dp'de ikisini birden kırpmak
pahasına. **Hâlâ kodun kararı.**

---

## Bu turda kodda yapılanlar

- **121** — bant iki `Row` grubu; aradaki nokta silindi, grupları bandın kendi
  8dp'si ayırıyor. Cihazda ölçüldü: `A101` 172px'de bitiyor, `BİM · dün`
  193px'de başlıyor → **21px = 8,0dp**.
- **121'in yan bulgusu** — delta bütçesi sapma işaretini **saymıyordu**.
  Hesap saf bir fonksiyona çıktı (`deltaBudget`); bandın düşmeyen her üyesi
  kendi aralığıyla iniyor. `- 6.dp` de aynı ailedendi (bandın aralığı 21.
  turda 8dp'ye düzeltilmişti, bu satır 6dp'de kalmıştı).
- **122** — silme kapısı iki bacaklı sayıyor: satırın kendi istisnası **ve**
  hedefi izleyen satırlar. İkincisi olmadan hasarın en büyüğü geçerdi.
  Cihazda: *"Bu markete giden 1 satır var."*
- **123** — `TripDao.lastDeclaredStoreId`, yeni kolon **açmadan**.
- **124** — `TakenCounter` / `TakenSection`; üçüncü mod türetiliyor.
- **125** — değişiklik yok; kodun iki kararı da onaylandı.
- **126** — `ProductSheet`'in eylem grubunun ilk satırı; `setLineStore`'un
  çağranı nihayet var. Sapma sorusu artık **dört** yerde soruluyor ve dördü
  de tek fonksiyonu (`deviantStoreName`) okuyor.
