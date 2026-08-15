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
