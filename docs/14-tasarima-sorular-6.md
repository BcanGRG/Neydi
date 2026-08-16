# Tasarıma sorular — altıncı tur

**16 Ağustos 2026.** Beşinci tur kapandı (karar 36), ayna tazelendi. Bu turun
ana maddesi **satır silme** — ve sormaya başlarken gördük ki siz zaten
cevaplamışsınız. Soru "nasıl silinsin" değil, **sizin iki dokümanınızın
birbiriyle çelişmesi**.

Biçim aynı: **tasarımın verdiği** → **gerçek** → **soru**.

---

## 1. Snackbar kaç yerde? Karar 8 ile gezinme sözleşmesi çelişiyor

### Tasarımın verdiği — iki ayrı yerde, iki ayrı sayı

**Gezinme sözleşmesi, "Jest" satırı:**

> Sağdan sola swipe listede satırı siler (geri alma "Alındı" bölümünde değil,
> **5 sn'lik snackbar**'da). Etiket akışında jest yok.

**Karar 8'in gerekçesi:**

> Snackbar aksiyon taşıyor ve uygulamada **tek bir yerde** kullanılıyor:
> alışveriş kendiliğinden kapandığında "Geçmiş'te gör".

Silme geri alması **ikinci** kullanım olur. "Tek bir yerde" cümlesi de o anda
yanlış hâle gelir.

### Gerçek

Uygulamada **hiç snackbar bileşeni yok**. `NeydiToast` var ve karar 8 gereği
bilerek aksiyonsuz — KDoc'unda *"ikisini aynı bileşene bindirmek, işaretlemede
snackbar yasağını da bulanıklaştırırdı"* yazıyor.

Yani silmeyi uygulamak, uygulamanın **ilk aksiyon taşıyan snackbar**'ını
yazmak demek. Bunu iki dokümanınız "olabilir mi" konusunda anlaşmazken yapmak
istemedik.

Silme yolunun geri kalanı zaten kararlı ve bizde hazır:
- **Jest:** sağdan sola swipe ✔ (sözleşme)
- **Animasyon:** 200 ms, yükseklik daralması, *"silinen satırın altındakiler
  tek hareketle toplanır"* ✔ (sözleşme)
- **Veri:** soft delete altyapısı var (`remove()` → `softDelete`), tombstone
  duruyor ✔

Eksik olan tek parça geri alma yüzeyi.

### Sorular

1. **Snackbar ikinci kullanımı alıyor mu?** Alıyorsa karar 8'in *"tek bir
   yerde"* cümlesi güncellenmeli. Almıyorsa geri alma nerede yaşıyor?
2. Snackbar'ın **metni ve aksiyon etiketi** ne? Yazım kuralınız *"fiil ve tek
   kelime: Kaydet, Bitir, Ekle, Sil"* diyor — geri alma için **"Geri al"** iki
   kelime. İstisna mı, yoksa başka bir sözcük mü?
3. **Alışveriş modunda swipe var mı?** Sözleşme yalnızca *"etiket akışında jest
   yok"* diyor. Alışveriş modunda satırlar işaretleniyor ve orada yanlışlıkla
   silmenin bedeli daha yüksek — reyondasın, geri alma penceresi 5 sn.
4. **"Alındı" bölümündeki satır silinebilir mi?** Sözleşme geri almanın "Alındı"
   bölümünde *olmadığını* söylüyor ama o bölümdeki satıra swipe atılıp
   atılamayacağını söylemiyor.

---

## 2. Beşinci turdan devreden iki küçük madde

**2a · Ekran 1'in beşinci çerçevesi tildesiz** (F11.17). Dört maket
`~642 TL` oldu, biri hâlâ *"Son alışveriş: bugün · 642,50 TL"* — tilde yok,
kuruş var. Türetilmiş bir tutar olduğu için biçim kuralına aykırı.

**2b · İkonografi karar 33'ü eski çiftiyle örnekliyor** (F11.18). Karar defteri
ilişkiyi doğru yazıyor (*"ikon yanındaki metinden bir kademe açık"*) ama
İkonografi aynı kuralı hâlâ *"metin `#E4D8C9`, ikon `#F5EDE6`"* diye
örnekliyor. Defteri esas aldık.

---

## 3. Bir not: sözleşme sandığımızdan çok şey cevaplıyor

Bu turu açarken "satır silme tasarlanmamış" sanıyorduk ve ROADMAP'te dört açık
tasarım sorusu olarak duruyordu. Gezinme sözleşmesini okuyunca üçünün
cevabının zaten yazılı olduğu görüldü.

Kendi payımıza ders: **soru sormadan önce sözleşmeyi okumak.** Aynayı
tazeledik ama içindekini taramamıştık.
