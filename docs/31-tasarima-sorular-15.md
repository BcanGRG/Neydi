# 31 — Tasarıma sorular (on beşinci tur): tasarımın çizip kodun kullanmadığı dört şey

**22 Ağustos 2026.** Bu tur kısa ve tek konulu: tasarım sisteminde **tanımlı**
ama uygulamada **hiçbir yerden çağrılmayan** dört şey var. Silmek de bağlamak da
tasarımın kararı; ikisini de kendi başımıza yapmadık.

Sebep: bunlar birer stil tercihi değil, birer **sözleşme**. Odak halkası
erişilebilirlik sözleşmesi; amber şerit "eksik/emin değiliz" dilinin bir
parçası. Sessizce silmek, sözleşmeyi kod tarafından tek taraflı feshetmek olur.

---

## S1 — `Modifier.focusRing`: bağlanacak mı, silinecek mi?

**Bugünkü durum:** tanımlı, üretimde **sıfır çağıran**.

Tasarım sisteminde odak halkası yazılı ve karar 64 kök yazma alanı için *"yeşil
2dp odak çerçevesi"* diyor. Ama uygulamada odak halkası **hiçbir yerde
çizilmiyor** — ne kök alanda, ne sheet'in arama alanında, ne fiyat alanında.

Bunun kimi ilgilendirdiği açık: **klavye ve switch access kullanıcıları.**
Ekranda hangi elemanda olduğunu gösteren tek şey o halka. Dokunmatik
kullanıcısı için bir süs; onlar için gezinmenin kendisi.

**Soru:** halka bağlanacak mı?

- **(a) Bağlansın** — hangi elemanlarda? Yalnızca metin alanları mı, bütün
  `pressable` hedefleri mi? (Uygulamada tıklanabilir her şey `Modifier.pressable`
  üzerinden geçiyor, yani tek yerden bağlanabilir.)
- **(b) Silinsin** — o zaman odak göstergesi olarak ne kalıyor? Bugün hiçbir
  şey yok.

Not: bu proje Material3'ün tıklanabilir `Surface`/`Button`/`Card`'ını
kullanmıyor, yani platformun kendi odak göstergesi de devrede değil. "Sil"
cevabı, odak göstergesinin **hiç olmaması** anlamına gelir.

---

## S2 — `AccentStrip`: amber sözleşmesi kendi şeridini imkânsız kılıyor

**Bugünkü durum:** tanımlı, ölü.

Şerit fiş döneminde "yeni ürün" ve "emin değiliz" satırlarının sol kenarına
çiziliyordu. Pivotla o ekran gitti; şerit kaldı.

Ama şeridi geri getirmenin önünde **sözleşmenin kendisinden** gelen bir engel
var:

- Tasarım sistemi: amber şerit **3dp**.
- Amber sözleşmesi: ışık modunda her amber dolgu **1,5dp `accentOutline`
  kenarlık** taşımak zorunda (kontrast 2,08:1, tek başına sınırını taşımıyor).

3dp genişliğinde bir şeride iki yandan 1,5dp kenarlık koyunca **iç dolgu 0dp**
kalıyor: ekranda amber görünmüyor, yalnızca kenarlık rengi görünüyor. Yani
şerit, sözleşmeye uyduğu anda kendisi olmaktan çıkıyor.

**Soru:**

- **(a)** Şerit silinsin (pivotla işi bitti).
- **(b)** Kalsın ve **genişliği artsın** — kaç dp'de amber görünür olur?
- **(c)** Kalsın ve **kenarlık kuralından muaf** olsun — o zaman muafiyetin
  gerekçesi ne (şerit metin taşımıyor, kontrast eşiği farklı mı)?

---

## S3 — `AccentChip` ve `AccentSurface` de ölü; amber'ın uygulamada tek bir çizimi kaldı

**Bugünkü durum:** ikisi de tanımlı, kendi dosyaları dışında **sıfır çağıran**.

`AccentChip` bir süre *"başka markette ucuz"* çipiydi. **Karar 57** amberi tek
anlama (*eksik / emin değiliz*) kilitleyince o iş kiremide taşındı ve `AccentChip`
işsiz kaldı. `AccentSurface` de yalnızca `AccentChip` ve `AccentStrip`
tarafından kullanılıyordu.

Sonuç: **amber sözleşmesi uygulamada bugün hiçbir yerde uygulanmıyor.** Amber,
tasarım sisteminde tanımlı ve gerekçelendirilmiş bir renk — ama ekranda
görünmüyor.

Bu bir kusur olabilir de olmayabilir de: belki *"eksik / emin değiliz"* hâli
hâlâ gelecek (etiket okumasında düşük güvenli alanlar için) ve bileşen o günü
bekliyor.

**Soru:** amber'ın uygulamada bir yüzeyi olacak mı?

- **(a) Evet** — nerede? En güçlü aday etiket onay kartında **okunamamış
  alanlar** ("marka boş", "ambalaj okunamadı"). Bugün o alanlar sessizce boş
  kalıyor; amber onların dili olabilir.
- **(b) Hayır** — üç bileşen de silinsin ve amber tema token'ı **yalnızca
  `warning` metni** olarak kalsın.

---

## S4 — Kategori tonları: kodun uydurduğu 12 renk kullanılsın mı, silinsin mi?

**Bugünkü durum:** `CatalogSeedData` on iki kategori için birer ton taşıyor —
`#6E8B3D` Meyve-Sebze, `#B07A3C` Fırın-Ekmek, `#4A7C8C` Süt-Kahvaltılık…
Renkler veritabanına yazılıyor, `CategoryTile` bir `tint` parametresi
taşıyor — ve **iki çağrı yerinin ikisi de tonsuz çağırıyor.** Palet
veritabanında duruyor ve hiç çizilmiyor.

Kutucuğun KDoc'u bunu bir borç olarak kaydetmiş:

> *"Kategoriye özgü zemin tonu. **Tasarım sistemi kategori tonlarını verene
> kadar** varsayılan `surfaceVariant`."*

Ve haklı: tasarım dosyalarının hiçbirinde kategori tonu tanımlı değil. O on iki
hex **kod tarafında uyduruldu**. Bağlamadık, çünkü bağlamak tasarlanmamış on iki
rengi uygulamanın en çok tekrar eden görsel öğesine yaymak olurdu.

**Soru:**

- **(a)** Palet tasarımdan gelsin — on iki ton, ışık ve karanlık için.
  (Kutucuk iki harf taşıyor, yani her tonun **metin kontrastı** da gerekiyor.)
- **(b)** Kategori tonu diye bir şey olmasın — kutucuk her yerde
  `surfaceVariant` kalsın, `tintArgb` kolonu ve parametresi silinsin.

Bugünkü hâl ikisi de değil: renk var, kullanılmıyor, ve kimse hangisi olduğunu
söylememiş.

---

## Neden sizin kararınız

Kodun kendi kuralı ölü kodu silmek yönünde ve bu turda `Placeholders.kt`,
`formatDayMonthTime`, `NeydiExtraShapes.barTop` ve iki bağımlılık zaten
silindi. Bu dördünü listeye alıp silmedik çünkü ötekilerin aksine bunların
**tasarımda bir gerekçesi** var — kullanılmamaları bir ihmal de olabilir,
bilinçli bir vazgeçiş de. Aradaki farkı ancak siz söyleyebilirsiniz.

Cevap ne olursa olsun tek bir şey rica ediyoruz: **"kalsın" cevabı bir
kullanım yeriyle birlikte gelsin.** Kullanılmayan bir bileşen bir sonraki
denetimde yine bu listeye düşer.
