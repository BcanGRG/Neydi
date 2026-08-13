# Logo + App Icon + Splash — Üretim Promptları

## Önce iş akışı (bunu atlarsan logo 66dp'de dağılır)

1. **Recraft** veya **Adobe Firefly Text to Vector** ile üret — üretime girecek her asset için. Midjourney'i sadece mood/konsept keşfi için kullan; raster'dan trace edilen eğriler ikon boyutunda titrek durur ve bu en çok kaçınamayacağın monochrome/tinted varyantlarda belli olur.
2. **Önce tek renkli siluet üret.** Android monochrome katmanı ve iOS tinted varyantı zaten bunu istiyor. Şekil oturmadan renk ekleme.
3. **66dp monochrome'da test et.** Orada okunmuyorsa konsept ölü — Figma'da ne kadar iyi göründüğünün önemi yok.
4. **Çıkanı final dosya olarak kullanma.** Şekil referansı olarak al, eğrileri düğüm düğüm yeniden çiz veya temizle.

**Tek vektör, beş iş:** Aynı çizim şunların hepsine hizmet etmeli — Android adaptive foreground, Android monochrome katmanı, Android splash ikonu, iOS Icon Composer foreground, iOS launch screen görseli. Beş ayrı çizim gerekiyorsa konsept yanlıştır.

**Ortak teknik spec (her prompta dahil):** tek renk siluet · ortalanmış · canvas'ın ~%60'ı · 66dp ölçeğinde minimum 5dp çizgi kalınlığı · metin yok · gradyan yok · gölge yok · fotografik detay yok · hairline yok.

---

## KONSEPT A — "Düğüm" (parmağa ip bağlamak)

**Neden bu:** Unutmamak için ipe düğüm atmak, Türkçede hafızanın kendisi. Sıcak, oyuncu, kültürel olarak tamamen sizin — hiçbir uluslararası rakip bunu kullanamaz. Uygulamanın vaadinin birebir resmi. Bir düğüm 66dp'de bozulmaz.

### Recraft (üretim)
```
A minimalist flat vector app icon: a single continuous rope or thread rendered as one
even-weight stroke, tied into a simple overhand knot at the center. The knot's loop is
slightly wider than tall, forming a soft rounded bowl shape. The two loose ends of the
thread extend outward and downward, one slightly longer than the other, with rounded
caps. Perfectly geometric construction, no fraying, no texture, no fiber detail.
Solid single color on a plain background. Stroke weight is thick and uniform, roughly
1/12 of the canvas width, with fully rounded terminals. Centered, occupying about 60
percent of a square canvas. Flat design, no gradient, no shadow, no highlight, no
outline, no text, no letters. Clean SVG-style geometry with smooth bezier curves.
```
**Recraft ayarı:** Style = Vector Illustration → alt stil "Flat / Minimal Icon". İlk üretim **tek renk siyah**, sonra `#B34418` terracotta.

### Varyasyon istekleri (aynı promptun sonuna ekle)
- `...and one loose end curls upward and terminates as a check mark stroke.` → düğüm + tik birleşimi
- `...the knot's loop is drawn as a perfect circle, the ends as two straight capsules.` → daha geometrik, ikon setine daha uyumlu
- `...the thread forms a knot that also reads as a lowercase letter n.` → isim "Neydi" ise harf bağı

### Midjourney (sadece mood)
```
minimal flat vector app icon, a thread tied in a single knot, memory knot, one continuous
even stroke, rounded terminals, geometric, terracotta on warm cream, no texture,
no gradient, icon design, centered --style raw --no text, letters, words, watermark,
shadow, 3d, realistic rope, fibers, frayed ends --ar 1:1 --v 7
```

---

## KONSEPT B — "File" (Türk file torbası)

**Neden bu:** Evrensel market sembolü market arabası ya da kağıt torba — ikisi de "market zinciri" der. File, "Türkiye'de pazara gitmek" der ve bir perakendeciye değil çifte aittir. Sıcak ve spesifik, ama kostüm değil. Tek dolu baklava, simetrik bir nesneye asimetrik odak noktası verir — logo ile stok ikonu arasındaki fark tam olarak budur. Aynı zamanda "listedeki tek gerçekten lazım olan şey" okuması var.

### Recraft (üretim)
```
A minimalist flat vector app icon of a Turkish string market bag drawn as one continuous
even-weight outline stroke. Two open arcs at the top form the handles without crossing
each other. The body is a soft wide U shape, about 60 percent of the canvas width.
Inside the body sit exactly three diamond-shaped mesh openings arranged two on top and
one below; the single lower diamond is filled solid while the upper two remain open
outlines. The filled diamond sits slightly off-center to the right. Nothing else inside
the bag: no fruit, no bread, no groceries, no bulges. Uniform thick stroke weight with
rounded joins, roughly 1/16 of the canvas width. Solid single color on a plain
background. Flat design, no gradient, no shadow, no text, no letters. Clean geometric
SVG-style construction.
```
**Recraft ayarı:** Style = Vector Illustration → "Line Art / Outline Icon". Tek renk `#3F6B54` ot yeşili veya `#B34418`.

### Varyasyon istekleri
- `...the filled diamond is replaced by a small solid check mark.` → file + tik
- `...the handles are drawn as two separate half-circles that do not touch the body.` → daha hafif, daha zarif
- `...five diamonds in a three-over-two arrangement, the center one filled.` → daha zengin doku, 24dp'de riskli

### Midjourney (sadece mood)
```
minimal flat vector icon of a mesh string shopping bag, single continuous line, two
handles, three diamond mesh openings, one filled solid, geometric, olive green on warm
cream, empty bag, icon design, centered --style raw --no text, letters, groceries,
fruit, vegetables, gradient, shadow, 3d, photo, texture --ar 1:1 --v 7
```

---

## KONSEPT C — "Fişin Kuyruğu"

**Neden bu:** Her liste uygulaması tik'li bir liste çizer. Neredeyse hiçbiri fiş çizmez ve bu kategoride yırtık zigzag kenarı kimse sahiplenmemiş — renksiz ve yazısız, bir saniyenin altında "fiş" diye okunan tek işaret. Ayrıca ürünün tezinin resmi: liste → fiş → fiyat geçmişi. Tik'in siluetin dışına taşması, onu piktogramdan marka işaretine çeviren kasıtlı düzensizlik; tam içine oturan bir tik unutulur, taşma akılda kalır.

### Recraft (üretim)
```
A minimalist flat vector app icon: a single upright receipt shape, taller than wide,
with its top-left corner folded back as a small triangle. The bottom edge is cut into a
zigzag of exactly five sharp peaks, like torn thermal till paper. Inside the receipt
sit three horizontal bars of decreasing length representing list lines. The lowest and
shortest bar is not a bar but a bold check mark stroke that breaks out past the
receipt's right edge, overshooting the silhouette by about 8 percent of the canvas
width. Solid single color, flat filled receipt with the bars and check cut out as
negative space. Geometric construction, sharp zigzag, rounded terminals on the check
stroke only. Centered, about 60 percent of a square canvas. No gradient, no shadow,
no text, no letters, no numbers. Clean SVG-style geometry.
```
**Recraft ayarı:** Style = Vector Illustration → "Flat / Solid Icon". Tek renk `#B34418`, tik `#E0A32E` amber (ama önce tek renk halini onayla).

### Basitleştirme merdiveni (Recraft'a ayrı ayrı ürettir, ikon boyutları için gerekli)
- **66dp:** zigzag 5 → 3 tepe, iç çubuklar 3 → 2
- **24dp:** kıvrım kalkar, sadece siluet + 1 çubuk + tik kalır

### Midjourney (sadece mood)
```
minimal flat vector app icon, a receipt with a torn zigzag bottom edge and a folded top
corner, three horizontal lines inside, a bold check mark breaking out past the right
edge, geometric, terracotta on cream, icon design, centered --style raw --no text,
letters, numbers, digits, price, barcode, gradient, shadow, 3d, photo, paper texture
--ar 1:1 --v 7
```

---

## KONSEPT D (bonus) — "İ ve ı"

**Neden bu:** Noktalı/noktasız i çifti Türkçede var, başka neredeyse hiçbir dilde yok — hiçbir stok ikon setinde bulunmaz, hiçbir uluslararası rakip kullanamaz. Aynı zamanda dört konsept içinde zorlu boyutlarda **en güvenlisi**: iki kapsül ve bir daire 66dp monochrome'da, iOS tinted varyantında veya adaptive icon maskesi altında kırılamaz. Çift okuması var — bir Türkçe harf çifti ve bir tik'lenmiş iki satırlık liste.

### Recraft (üretim)
```
A minimalist flat vector app icon: two vertical bars with fully rounded capsule ends
standing side by side on a plain square field, baseline aligned at the bottom. The left
bar is taller and has a separate circular dot floating above it with a clear gap. The
right bar is shorter and bare, with no dot. Bars are thick, about 1/8 of the canvas
width; the dot's diameter equals the bar width. The height difference between the two
bars is the only asymmetry. Three primitives total: two capsules and one circle. Solid
single color on a plain background. Flat design, no gradient, no shadow, no outline,
no text beyond these abstract shapes. Perfectly geometric, centered, about 60 percent
of the canvas.
```

---

## SPLASH SCREEN

Splash'te **logo dışında hiçbir şey yok**. Metin yok, slogan yok, illüstrasyon yok, gradyan yok. Bu bir tercih değil, iki platformun da teknik zorunluluğu.

### Android (Android 12+ SplashScreen API — kapatamazsın)
- Android 12+ splash **her zaman çalışır**. Kendi splash Activity'ni eklersen sistemin splash'ine **ek olarak** görünür — ekleme.
- **İkon geometrisi:** İkon arkaplanı yoksa canvas 288×288dp, anlamlı içerik ortadaki **192dp çaplı daireye** sığmalı. İkon arkaplanı varsa 240×240dp / 160dp daire. Sistem her iki durumda da daireye maskeler — köşelerdeki her şey kesilir.
- **Arkaplan tek düz opak renk** olmak zorunda — gradyan yok, görsel yok. `#FBF7F2` (ışık) / `#13100E` (karanlık). Bu renk **ilk Compose karesiyle birebir aynı** olmalı, yoksa görünür bir flaş olur.
- Animasyon istiyorsan AnimatedVectorDrawable, süre **≤1000ms** (pratikte ~800ms), kendi kendine yeten (network yok, state yok). `setKeepOnScreenCondition`'ı yavaş senkronu gizlemek için kullanma.
- **İkonda metin yok.** `windowSplashScreenBrandingImage` kullanma — Google öneriyor değil.

### iOS (statik launch screen)
- `UILaunchScreen` Info.plist sözlüğü (tercih edilen) veya launch storyboard. **Senin kodun çalışmadan önce** render edilir: animasyon yok, Compose yok, mantık yok, dinamik içerik yok.
- **Metin olamaz.** Lokalize edilemez, Apple HIG bunu bir marka anı olarak kullanmamanı açıkça söylüyor.
- Asset katalogunda **Any + Dark** varyantları şart, yoksa karşı temada her soğuk açılışta renk flaşı olur.
- Üstte 44pt (Dynamic Island), altta 34pt (home indicator) temiz kalsın. Sadece basit view'lar güvenilir render olur — düz zemin üstünde bir UIImageView, tasarımın tamamı bu.
- **OS agresif cache'ler.** Değişiklik çoğu zaman uygulamayı silip yeniden kurmadan ya da cihazı yeniden başlatmadan görünmez — değişikliğin çalışmadı sanma.

### Devir teslim (ikisinde de kritik)
CMP iOS'ta Kotlin/Native + Skia olduğu için ilk Compose karesi SwiftUI'a göre ölçülebilir biçimde geç gelir. **Birebir aynı Compose karesini** yap — aynı arkaplan rengi, aynı işaret, aynı konum ve boyut — ve açılışta hemen göster ki native→Compose geçişi iki platformda da görünmez olsun. `kmpbits/KMP-Splash` Gradle eklentisi ikisini tek config'den üretiyor, bakmaya değer.

---

## APP ICON — üretim gereksinimleri

### Android
- **Adaptive icon:** toplam 108×108dp, güvenli alan 72×72dp, ama **her launcher'da garanti görünen tek şey ortadaki 66dp çaplı daire**. Dıştaki 18dp maskeleme ve parallax için — harcanabilir kabul et.
- Foreground ve background ayrı vector drawable. Foreground şeffaflık taşımalı ve **kendi gölgesini içine gömmemeli** — sistem zaten uyguluyor.
- **Monochrome katmanı (`android:monochrome`) zorunlu** — Android 13+ temalı ikonlar için. Alpha-only, tek renk siluet, aynı 108/72/66 geometrisi. **Rengi eklemeden ÖNCE işareti 66dp düz monochrome'da doğrula.**
- Ayrıca legacy raster mipmap'ler 48/72/96/144/192px + Play listesi için 512×512 32-bit PNG (≤1MB). Hiçbirinde metin yok.

### iOS
- Tek bir **1024×1024 PNG** kaynak, sRGB, düzleştirilmiş, **alpha kanalı YOK** — alpha kanalı otomatik App Store reddi.
- Yuvarlak köşeleri ve gölgeyi **sen gömme** — sistem squircle maskesini ve kendi gölgelendirmesini uyguluyor.
- **iOS 18+ light / dark / tinted varyantları istiyor.** Tinted varyant tek kanallı gri tonlamadan türetiliyor — yani işaret **tüm renk kaldırıldığında ve tüm ton ilişkileri yok edildiğinde** okunabilmeli. Anlamı iki rengin birbirine değmesine bağlı olan her konsept tinted'da çöker.
- **iOS 26 / Xcode 26** Icon Composer (.icon) kullanıyor: katmanlı foreground/background, sistemin uyguladığı specular ve blur. İşareti canvas'ın ~%80'inde ortada tut, detayı kenarlardan uzak tut, ve tüm efektler kapalıyken doğru okunan düz bir fallback her zaman ver.

### İkisinde de
Metin yok · wordmark yok · fotografik detay yok · hairline yok. **Minimum etkili çizgi kalınlığı 66dp ölçeğinde ~5dp**, yani 1024'lük iOS canvas'ında ~24px. Bundan inceler Spotlight'ta, Ayarlar'da ve bildirim boyutunda yok olur.

---

## Karşılaştırma checklist'i (dört konsepti de bundan geçir)

| Test | Neden |
|---|---|
| 66dp düz siyah siluet | Android monochrome + gerçek launcher boyutu |
| iOS tinted (tek kanal gri) | Renk ilişkileri yok edilince ayakta kalıyor mu |
| 24dp (Spotlight / bildirim) | Basitleştirme merdiveni gerekiyor mu |
| Adaptive icon dairesi altında | Köşelerde anlam kaldı mı |
| Yan yana 5 rakip ikonla | 1 saniyede ayırt edilebiliyor mu |
| Splash'te 192dp daire içinde | Maskeleme sonrası hâlâ dengeli mi |
