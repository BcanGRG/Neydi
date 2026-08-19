# Tasarım denetimi — kalan 38 bulgu

56 ajanlı bir denetim her ekranı kendi tasarım dosyasıyla karşılaştırdı; her
iddia, onu **çürütmekle görevli** ayrı bir ajandan geçtikten sonra sayıldı.
**49 iddia, 41 doğrulandı, 8 elendi.**

Üçü ("broken") [9d6041b](../../commit/9d6041b) ile kapandı: karar 45 ihlali
(özet kartı), geçmiş satırlarında tarih yokluğu, seçici sheet'lerinin
kapanmaması. Aşağıdakiler **kapanmadı**.

## Durum — 24 Ağustos

**41 bulgunun 36'sı kapandı.** Kalan beşi tasarıma soruldu: [`23-tasarima-sorular-9.md`](23-tasarima-sorular-9.md). Kalan altısı kapanmadı çünkü *kod eksik değil,
veri ya da karar eksik*:

| Bulgu | Neyin eksik olduğu |
|---|---|
| Ekle sheet'inde reyon başına ürün sayısı | Sayı hiçbir yerde yok: `Category`'de kolon, `CatalogSeedDao`'da `GROUP BY categoryId` sorgusu yok |
| Ekle sheet'inde ürün fiyatı | `CatalogSeed`'de fiyat alanı yok; fiyatlar hanenin kendi `Product`'ına bağlı. Ayrıca hiç alınmamış üründe ne yazılacağı bir **ürün kararı** |
| Ürün Detayı trend manşeti | Ay aralığı biçimleyicisi yok (`DateText` haftada bitiyor) ve yüzde, ambalaj değişimi üzerinden hesaplanamaz — maketin örneği tam da o vaka |
| Geçmiş 6 çubuklu mini grafik | Tasarım iki şeyi söylemiyor: çubuk **neyi** ölçüyor, ve tutarı hesaplanamayan gezi nasıl çiziliyor |
| Özet kartının sheet yerine satır içi kart olması | Yapısal yeniden düzenleme |
| ~~Kurulum adım 2/2'de geri~~ | ✅ kapandı — tasarım sorusu değildi, yalnızca dosya bölümlemesi yüzünden açık kalmıştı |

Ayrıca **bilinçli bir daraltma**: alışveriş modu satırının `surfaceVariant`
dolgusu yalnızca **koyu temada** uygulandı. Açık maket o rengi zaten
*işaretli* satıra veriyor; her satıra boyamak işaretli satırın tek kapsayıcı
sinyalini silerdi.

---

Sıra ciddiyete göre; her madde tasarımın dediğini (T) ve kodun yaptığını (K)
taşıyor. Denetim raporunun tamamı oturum çıktısında.

---

[BROKEN] Özet kartı tutar hesaplanamadığında da çiziliyor (karar 45 ihlali)
   ui/list/ListViewModel.kt:473
   T: Karar 45: "36sp manşet düşüyorsa kart hiç görünmüyor, yerine hiçbir şey konmuyor. İlk gezide 'Geçen sefer', gözlemsiz gezide 'En çok artan' satırları da sessizce düşüyor." Kartın gövdesi (Ekran 1 A3) 
   K: finishShopping() `_summary.value = ShoppingSummary(...)` değerini koşulsuz set ediyor; yalnızca `amountMinor = estimate.takeIf { priced >= MIN_PRICED_ITEMS }` null oluyor. SummaryCard (BasketAndSummar

[BROKEN] Alım geçmişi rows carry no date — observations are unidentifiable
   ui/product/ProductSheet.kt:290
   T: The purchase-history row is four columns, and the date is the FIRST one: `<div style="width:76px;...">{{ b.date }}</div>` then store (flex:1), then qty/pack (34px/38px), then price right-aligned (76px
   K: PriceBlock draws only two cells per row — `Text(text = row.store, ...)` and `Text(text = row.price, ...)`. `HistoryRow.observedAt` is populated in PriceSection.kt:98-103 and then never rendered anywhe

[BROKEN] Seçici sheet'leri dışına dokunulunca kapanmıyor ve dokunuşu geçiriyor
   ui/capture/TagPickers.kt:362
   T: Bölüm 01, yüzey türleri: "Sheet: Yığına girmez ama geri onu kapatır. Arkasındaki ekran görünür ve canlı kalır; dışına dokunmak da kapatır." Bölüm 09: "Sheet açılışı 320 ms ... Arkadaki ekran %4 karart
   K: PickerSheet, `Box(Modifier.fillMaxSize(), contentAlignment = BottomCenter)` içinde sadece çizim yapan bir Column; ne karartma (scrim) var, ne pointerInput/clickable, ne de bir kapatma geri çağrısı. St

[MISMATCH] Shopping-close snackbar ("Geçmiş'te gör") is never shown
   ui/list/ListViewModel.kt:473
   T: Frame 04 "Alışveriş kapanışı · açılmaz" draws a dark snackbar reading "Alışveriş kaydedildi · 18 ürün · ~642 TL" with the action "Geçmiş'te gör" (#FF9166), and the caption states: "Alışveriş kasada ka
   K: finishShopping() builds a ShoppingSummary and opens a ModalBottomSheet with SummaryCard; no snackbar is emitted. Grepping the whole source for "Geçmiş'te gör" / "Alışveriş kaydedildi" returns zero hit

[MISMATCH] Product Detail: destructive "Listeden çıkar" is drawn above the toggle, not last
   ui/product/ProductSheet.kt:172
   T: Frame 05 "Ürün Detayı · 0 gözlem" stacks three 56px rows in this order, each with a top hairline: "Her zamankilere ekle" (switch), "Bunu önerme" (switch), then "Listeden çıkar" in #B3261E. The destruc
   K: ProductSheetContent renders the red "Listeden çıkar" row first (line 168-183, preceded by the hairline) and then NeydiSwitch("Her zamankilere ekle") at line 185-189. The order is inverted, so in the z

[MISMATCH] Settings empty-staples note rewrites the design's copy and promises behaviour that does not exist
   ui/settings/SettingsScreen.kt:159
   T: Frame 07 "Ayarlar · yeni hane" prints exactly: "Her alışverişte aldığın ürünler üç geziden sonra kendiliğinden burada birikir." — the threshold (three trips) is the informative part of the sentence, m
   K: Renders "Her alışverişte aldığın ürünler birkaç alışverişten sonra kendiliğinden burada birikir." — "üç geziden" replaced by the vague "birkaç alışverişten". Worse, nothing in the app ever promotes a 

[MISMATCH] Alışveriş modu başlığının alt satırı "N kaldı" diyor, "N/M alındı" demiyor
   ui/list/ListScreen.kt:632
   T: "MİGROS ATAŞEHİR · 12/18 alındı" (Ekran 1 Liste.dc.html:336 ve 2a karanlık maketi; Ekranlar 2-4'te "MİGROS ATAŞEHİR · 0/18 alındı"). Karar 28: market adının kaynağı o gezide son çekilen etiket; "Hiç e
   K: `"${state.remainingRow} kaldı"` — ters metrik (kalan), toplam yok, market adı yok. Oysa aynı ekranın alt çubuğu ilerlemeyi zaten doğru biçimde hesaplıyor: ShoppingBottomBar `taken = state.totalRows - 

[MISMATCH] Alışveriş modunda başlık "Liste" yerine "Alışveriş" oluyor
   ui/list/ListScreen.kt:623
   T: Başlık her iki modda da "Liste" (22sp/700). Alışveriş modu maketlerinin ikisinde de (Ekran 1 Liste.dc.html:336 ve 2a karanlık; ayrıca Ekranlar 2-4) manşet "Liste". Karar 28 da bunu tekrarlıyor: "başlı
   K: `text = if (state.shoppingMode) "Alışveriş" else "Liste"` — mod değişince ekranın adı da değişiyor, yani kullanıcıya başka bir ekrana geçmiş gibi okunuyor.

[MISMATCH] Sepet tahmini alışveriş modunda da çiziliyor
   ui/list/ListScreen.kt:388
   T: Ölçülebilir farklar tablosu: "Öneri şeridi + sepet tahmini · plan: görünür → alışveriş: gizli" (Ekran 1 Liste.dc.html:676). Alışveriş modu maketlerinin hiçbirinde tahmin satırı yok.
   K: `if (!state.isEmpty) { item(key = "tahmin") { EstimatedBasket(...) } }` — tek koşul listenin boş olmaması; `state.shoppingMode` hiç sorulmuyor. Öneri şeridi doğru şekilde `if (!state.shoppingMode)` bl

[MISMATCH] Alışveriş modu satırı surfaceVariant dolgusunu almıyor, yalnız kenarlık kazanıyor
   ui/components/ListItemRow.kt:226
   T: Fark tablosu: "Satır container'ı · plan: dolgusuz, kenarlıksız → alışveriş: surfaceVariant + 1.5dp kenarlık" (Ekran 1 Liste.dc.html:674). Karanlık mod gerekçesi açıkça yazılı: "satır container'ı surfa
   K: `.clip(NeydiShapes.large).then(if (shoppingMode) Modifier.border(SizesExtra.rowBorderShopping, extras.hairline, ...) else Modifier)` — 1.5dp kenarlık var, `background(surfaceVariant)` yok. Karanlık te

[MISMATCH] Alışveriş sonrası özet, listenin içindeki kapatılabilir kart değil modal sheet
   ui/list/ListScreen.kt:242
   T: A3 çerçevesinin başlığı birebir: "Alışveriş sonrası · özet kartı — ekran değil, kart · bir kez görünür". Maket kartı liste alanının en üstüne koyuyor (24dp radius, surfaceVariant, sağ üstte `close` ik
   K: Özet bir `ModalBottomSheet` içinde açılıyor: scrim'li, listeyi kapatan, kapatmak için "Tamam" butonu gerektiren bir yüzey. Kartın kendi kapatma ikonu yok, arkasındaki boş liste ve alt şerit görünmüyor

[MISMATCH] "Geçen sefer unuttun" şeridi kiremit yerine amber çiziliyor
   ui/missing/MissingItemsScreen.kt:215
   T: Ekran 3 satır verisinde unutulmuş satırlar `strip: "#B34418"` / `stripDark: "#FF9166"` taşıyor ve boyama kodu `el.style.borderLeft = '4px solid ' + v` diyor — yani kartın sol kenarında 4px KİREMİT şer
   K: Şerit `extras.accent` ile boyanıyor; `Color.kt:27` `LightAccent = Color(0xFFE0A32E)` yani AMBER. Tasarımın kiremidi projede `LightPrimary = Color(0xFFB34418)` / `DarkPrimary = Color(0xFFFF9166)` olara

[MISMATCH] Fiyat okunamadığında klavye kendiliğinden açılmıyor, dolu değer de seçili gelmiyor
   ui/capture/TagCaptureScreen.kt:624
   T: Maket başlığı: "Fiyat okunamadı · tek boş alan vurgulanır, klavye hazır açılır". Kart kuralı (fiyat): "Alan dolu gelirse dokunuşla düzeltilir, boş gelirse klavye kendiliğinden açılır." Gezinme Sözleşm
   K: `PriceField` düz bir `BasicTextField(value: String)`; dosyada (ve `ui/capture/` altının tamamında) hiç `FocusRequester`, `LaunchedEffect { requestFocus() }` ya da `TextFieldValue`+`TextRange` yok. Kar

[MISMATCH] Eksik olabilir satırlarında fiyat sütunu yok
   ui/missing/MissingItemsScreen.kt:221
   T: Her satırın sağında sabit genişlikte fiyat: `width:84px;flex:none;text-align:right;font:600 14px/1.3;color:#5C4F45;font-variant-numeric:tabular-nums` → "18,00 TL", "89,90 TL", "289,00 TL", "174,50 TL"
   K: `MissingRow` veri sınıfında fiyat alanı bile yok (`productId, name, reason, section, selected`), satır da yalnızca ad + gerekçe çiziyor. Kullanıcı neyi listeye alacağına fiyatı görmeden karar veriyor.

[MISMATCH] Eksik olabilir satırları kart olarak çizilmiyor
   ui/missing/MissingItemsScreen.kt:202
   T: Satır bir kart: `min-height:68px;padding:10px 14px;border-radius:20px;background:#FFFFFF;border:1px solid #E7DACB` (koyu: `background:#241E1A;border:1px solid #3A322C`), satırlar arası `gap:6px`.
   K: Satır çıplak bir `Row`: yalnızca `heightIn(min = 68.dp)` var; zemin, kenarlık, 20dp köşe yarıçapı ve 10/14dp iç boşluk hiç uygulanmıyor, satır arası boşluk da yok. Liste, tasarımın kart ritmi yerine d

[MISMATCH] Ekle sheet'inde reyon kutucuklarının ürün sayısı satırı eksik
   ui/list/AddSheet.kt:232
   T: Kategori grid'inin her hücresi üç parça: 56×56 kutucuk, reyon adı (12px/500), ve altında sayaç `{{ c.count }}` → "18 ürün", "6 ürün", "14 ürün" (`font:500 12px;color:#8A7666;font-variant-numeric:tabul
   K: Hücre yalnızca `CategoryTile` + reyon adı çiziyor; sayaç satırı hiç yok, `Category` üzerinden bir ürün sayısı da okunmuyor. Kullanıcı bir reyona girmeden içinde kaç ürün olduğunu göremiyor.

[MISMATCH] Ekle sheet'indeki ürünler tek sütunlu çip ve fiyat yerine birim gösteriyor
   ui/list/AddSheet.kt:245
   T: Reyon ürünleri iki sütunlu grid: `display:grid;grid-template-columns:1fr 1fr;gap:10px`, her hücre `height:56px;border-radius:20px;background:#F1E7DB;border:1px solid #E7DACB`, içinde üst satırda (vars
   K: Ürünler tek sütunlu bir `LazyColumn` içinde hap biçimli `SuggestionChip` olarak çiziliyor ve ikinci değer olarak fiyat değil `seed.defaultUnit` ("kg", "adet") geçiliyor. Arama sonuçları için de aynı: 

[MISMATCH] Ürün seçici satırları "hanenin listesinde var / katalog" notunu taşımıyor
   ui/capture/TagPickers.kt:148
   T: "Ürün seç" ekranındaki 60px'lik satır iki satırlı: ad (17px/500) + kaynağını söyleyen not (13px, `color:#8A7666`). `catalogPicks` verisi bunu birebir yazıyor: `{ name: "Yoğurt 1 kg", note: "hanenin li
   K: Satır 60dp yüksekliği ayırıyor ama içinde tek bir `Text(seed.name)` var; not satırı hiç çizilmiyor, dolayısıyla hanenin kendi listesindeki ürünle ham katalog kaydı görsel olarak ayırt edilemiyor.

[MISMATCH] Ürün Detayı has no manşet sentence — the screen's stated centerpiece
   ui/product/ProductSheet.kt:140
   T: Section header: "Okunacak şey grafik değil manşet cümlesi; grafik cümleyi açıklar." The sheet head is a Fraunces 24px line — `Süt 32 TL → 41 TL · son 3 ayda %28 arttı`; for the shrinkflation case `Ayç
   K: The sheet head is only the product name in `titleLarge` (`Text(text = state.name, style = MaterialTheme.typography.titleLarge, ...)`). With observations present it drops straight into the "Nerede ucuz

[MISMATCH] Geçmiş header is missing the 6-bar mini chart the code comment claims is there
   ui/history/HistoryScreen.kt:86
   T: The Geçmiş header block is back arrow + title + a mini bar chart in one block: `<div style="display:flex;align-items:flex-end;gap:10px;height:96px">` with bars `background:#B34418;border-radius:8px 8p
   K: The header Column contains only the back-arrow/title Row; nothing draws bars anywhere in the file (its `verticalArrangement = Arrangement.spacedBy(14.dp)` has a single child). The comment on line 86 s

[MISMATCH] Sheet prices are formatted without "TL", against the codebase's own chip-only rule
   ui/product/PriceSection.kt:89
   T: Every price in the sheet carries the currency: Nerede ucuz rows are `{ name: "BİM", sub: "Dost · dün", price: "100,00 TL" }`, Alım geçmişi rows `{ date: "6 Ağu", store: "Migros", qty: "2", price: "41,
   K: Both `CheapRow.price` and `HistoryRow.price` are built with `formatChipMinor(it.unitPriceMinor)`, which strips the currency. The user sees `100,00` and `41,00` with no unit, in 15sp/14sp text rows — n

[MISMATCH] "Listeden çıkar" is drawn above the toggle instead of as the last row
   ui/product/ProductSheet.kt:166
   T: The sheet's tail is three 56px rows in a fixed order, each with a top hairline: `Her zamankilere ekle` (switch), `Bunu önerme` (switch), then last `<div style="font:500 17px...;color:#B3261E">Listeden
   K: The destructive row is emitted first — hairline + "Listeden çıkar" — and `NeydiSwitch(label = "Her zamankilere ekle", ...)` comes after it, so the red row sits between the content and the toggle rathe

[MISMATCH] "Nerede ucuz" rows lose the tile treatment and the observation's age
   ui/product/ProductSheet.kt:209
   T: Each Nerede ucuz row is a filled tile: `height:52px;padding:0 14px;border-radius:16px;background:#F1E7DB;border:1px solid #E7DACB`. Its sub-line is brand plus recency — `{ name: "BİM", sub: "Dost · dü
   K: Rows are bare `Row`s with `padding(horizontal = Spacing.md, vertical = Spacing.xs)` — no background, no border, no 16dp corner, ~44dp tall. The sub-line is `listOfNotNull(row.brand, row.pack).joinToSt

[MISMATCH] Kamera izni reddedilince Liste'ye düşülmüyor; kalıcı ret metni sözleşmedeki cümle değil
   ui/capture/TagCaptureScreen.kt:149
   T: Bölüm 05: "Kamera izni reddedildi | Liste'de 2 sn toast: «Kamera izni olmadan etiket çekilemez» | Nereye düşer: Liste. Kamera hedefi görünmeye devam eder." ve "İzin kalıcı reddedildi | Etiket ekranı y
   K: Reddedilince ekranda kalınıyor: TagCapture destinasyonu açık kalıyor, koyu vizör zemininde ortalanmış "Etiket çekmek için kamera izni gerekiyor" yazılıyor. Liste'ye dönüş de, sözleşmedeki 2 sn'lik toa

[MISMATCH] Fiyat boş gelen onay kartında klavye kendiliğinden açılmıyor
   ui/capture/TagCaptureScreen.kt:616
   T: Bölüm 03: "Kart · fiyat boş | Girdi: Klavye kendiliğinden açılır | Kaydet pasif; ilk rakamda etkinleşir." Bölüm 07 (İlk odak): "Hiçbir ekran açılırken klavye açmaz. Tek istisna: fiyatı okunamamış onay
   K: PriceField düz bir BasicTextField; FocusRequester/SoftwareKeyboardController kullanımı commonMain'in tamamında yok. OCR fiyat okuyamadığında kart açılıyor, amber şerit ve "Fiyat okunamadı — yaz" çizil

[MISMATCH] Kurulumda geri, uygulamadan çıkmak yerine Liste'ye düşürüyor
   App.kt:134
   T: Bölüm 02 geri sözleşmesi: "Kurulum · adım 1/2 | Uygulamadan çıkar; kurulum bir sonraki açılışta baştan gelir." ve "Kurulum · adım 2/2 | Adım 1'e döner." Bölüm 01: Kurulum "tam ekran akış ... yalnızca 
   K: Setup, Liste'nin ÜSTÜNE ekleniyor (`backStack.add(Setup)`), SetupScreen'de hiçbir geri yakalaması yok. Bu yüzden hangi adımda olursa olsun tek geri basışı Setup'ı yığından atıyor ve kullanıcı kurulumu

[MISMATCH] Bölüm 09'un hareket süreleri hiç uygulanmamış: destinasyonlar, kart ve sheet'ler animasyonsuz
   App.kt:74
   T: Bölüm 09: "Destinasyon açılışı 300 ms · kayarak sağdan, %8 mesafe", "Destinasyon kapanışı 250 ms · ters yön", "Sheet açılışı 320 ms · yay, sönüm .82", "Sheet kapanışı 220 ms", "Onay kartı 260 ms · aşa
   K: NavDisplay'e ne `transitionSpec` ne `popTransitionSpec` veriliyor; kütüphanenin `defaultTransitionSpec()`i devrede (Android'de fade, desktop'ta hiç animasyon, iOS'ta 500 ms kayma) — 300/250 ms'lik sağ

[MISMATCH] check_circle is drawn outline; the spec calls it the one FILL 1 icon
   ui/components/NeydiIcon.kt:109
   T: Envanter satırı: check_circle · "Onaylandı" · "FILL 1 — dolu olan tek ikon". FILL ekseni kuralı: "Dolgu bir durum bildirir: seçili, onaylanmış, tamamlanmış. Süs olarak kullanılmaz. Uygulamada dolu çiz
   K: NeydiIcons.CheckCircle Phosphor'un REGULAR (kontur) çizimini taşıyor: path 104 yarıçaplı dış halka + 88 yarıçaplı iç halka ("M232,128A104,104,0,1,1,128,24…Zm-16,0a88,88,0,1,0-88,88A88.1,88.1,0,0,0,216

[MISMATCH] List row's "işaretlendi" tick is a Unicode ✓ text glyph, not the check icon
   ui/components/ListItemRow.kt:311
   T: Envanterde check ikonunun işi ve yeri: "İşaretlendi · Liste satırı, Ekle sheet'i · Çıplak check satırda". Karar 32 (ikonografi, Yol A/B): "Değişken font paketlenmiyor, ikonlar Text olarak çizilmiyor".
   K: CheckTarget işaretliyken ikon değil metin çiziyor: Text(text = "✓", style = typography.labelMedium, color = onPrimary). Yani (a) NeydiIcons.Check hiç kullanılmıyor, (b) glif sistem fontunun yedek zinc

[MISMATCH] Checked checkbox fills with primary (terracotta) instead of secondary (green)
   ui/components/ListItemRow.kt:295
   T: The checked state is a 12dp squircle filled with secondary: `border-radius:12px;background:#3F6B54` with a white `check` glyph in light mode, and `background:#8FC7A2` with a `#13100E` check in dark mo
   K: `CheckTarget` animates `fill` to `MaterialTheme.colorScheme.primary` and draws the tick in `onPrimary`, so every checked row shows a terracotta #B34418 square (dark: #FF9166 orange) instead of the her

[MISMATCH] Unchecked checkbox ring uses hairline at 1.5dp, not outline at 2dp — the ring is invisible
   ui/components/ListItemRow.kt:306
   T: Every unchecked checkbox in every mockup is `width:24px;height:24px;border-radius:50%;border:2px solid #8A7666` — the `outline` token, 2dp. 15 occurrences in the design-system file alone, 34 across th
   K: `val outline = LocalNeydiExtraColors.current.hairline` and `.border(1.5.dp, ... outline, ...)`, so the resting checkbox is a 1.5dp #E7DACB ring on #FBF7F2 surface — about 1.2:1 contrast, effectively n

[MISMATCH] Price chip renders as bare text — no pill container
   ui/components/Chips.kt:55
   T: 'Fiyat çipi' is a filled pill: `height:32px;padding:0 12px;border-radius:999px;background:#F1E7DB` (dark #241E1A), text `font:600 14px` in textPrimary #221A14. The design system draws four states of i
   K: `PriceChip` is a `Box` with a width and a min tap height containing only a `Text` — no `clip(CircleShape)`, no `background(surfaceVariant)` — and the text color is `onSurfaceVariant` (#5C4F45) rather 

[MISMATCH] Quantity badge drawn at 14sp Medium instead of the specified 20sp/800
   ui/components/Chips.kt:188
   T: Row anatomy: '[adet rozeti — yalnızca adet 1 değilse, 20sp/800]'. The mockup badge is `min-width:32px;height:26px;background:#F1E7DB;font:800 20px/1;color:#221A14` (shopping mode: `min-width:36px;heig
   K: `QuantityBadge` styles its text with `MaterialTheme.typography.labelMedium` (14sp, FontWeight.Medium) and `onSurfaceVariant`, so '2x' renders about the same size and weight as the metadata line instea

[MISMATCH] Check mark is a Unicode text glyph, not an icon
   ui/components/ListItemRow.kt:311
   T: The tick is the `check` icon (Material Symbols Rounded, `font-size:16px`, FILL 1) inside the 24dp squircle; the iconography section states 'Emoji ikonografi olarak kullanılmaz' and decision 32 (quoted
   K: `CheckTarget` draws `Text(text = "✓", style = MaterialTheme.typography.labelMedium, ...)`. The glyph resolves through the font fallback chain, so its shape and stroke weight differ between Android and

[COSMETIC] First-day paste button has no content_paste icon
   ui/list/EmptyStates.kt:138
   T: Frame 08 draws the paste route as a 48px pill with an 8px gap between a 20px `content_paste` glyph and the label "WhatsApp'tan listeni yapıştır". The icon inventory lists `content_paste` with job "Lis
   K: The button is a plain NeydiButton, which has no icon slot at all (NeydiButton.kt:41-59 renders only a Text), so the pill is text-only. NeydiIcons.ContentPaste is defined at NeydiIcon.kt:202 but has ze

[COSMETIC] Liste empty block sits under the header instead of centred in the content area
   ui/list/EmptyStates.kt:63
   T: Frames 01 and 08 both give the empty block `flex:1;min-height:0;display:flex;flex-direction:column;justify-content:center` — the title/line/ghost-button group is vertically centred in the space betwee
   K: EmptyState is emitted as the second item of the ListScreen LazyColumn (ListScreen.kt:399-417), immediately after ListHeader, with a fixed vertical padding of SpacingExtra.emptyStateBlock (48.dp). On a

[COSMETIC] Mid-cycle empty subtitle uses "·" where the design (and the code's own KDoc) use a comma
   ui/list/ListScreen.kt:407
   T: Frame 01 body line is "Son alışveriş 3 gün önce, ~642 TL." — a comma separates the recency from the amount, deliberately different from the header line above it which uses "Son alışveriş: 3 gün önce ·
   K: The body line is derived from the header string: lastTripSummary() produces "Son alışveriş: 3 gün önce · ~642 TL" (ListState.kt:78), the prefix "Son alışveriş: " is stripped and "Son alışveriş " re-pr

[COSMETIC] Çekim hatası 4 sn'lik üst şerit olarak gösteriliyor; sözleşme 2 sn'lik toast diyor
   ui/capture/TagCaptureScreen.kt:815
   T: Bölüm 01: "Toast: 2 sn, aksiyonsuz, kuyruksuz." Bölüm 05: "Kamera donanımı meşgul → Toast: «Kamera şu an kullanılamıyor»", "Depolama dolu → Kart açılmaz; toast: «Yer kalmadı, fotoğraf alınamadı»".
   K: Hata, alt taraftaki NeydiToast (2 sn) yerine ekranın üstüne çizilen ayrı bir şeritle ve FAILURE_MS = 4000L ile gösteriliyor — sabitin KDoc'u "toast ile ayni omur" dese de toast 2000 ms. Metin de iki s

[COSMETIC] Bolt KDoc still claims the flash icon has no caller
   ui/components/NeydiIcon.kt:138
   T: Karar 60 (Flaş): "v1'de var: iki hâl (kapalı/açık), sağ üst köşe, oturumluk… Etiketsiz istisna yediye çıktı." Yani bolt artık ölü ikon değil, çalışan bir hedef.
   K: Bolt'un KDoc'u hâlâ "HENUZ CAGIRAN YOK, kasitli… E15 gelince ikonu ikinci kez elle tasimak gerekmesin diye simdi yaziliyor" diyor; oysa TagCaptureScreen.kt:359-365 flaş düğmesini çiziyor (48dp hedef, 

[COSMETIC] Sparkline is drawn in red/green instead of the neutral outline colour
   ui/components/ListItemRow.kt:370
   T: Every sparkline in the doc set is stroked with the outline token — `stroke="#8A7666"` in light mode, `stroke="#8A7A6E"` in dark — including the one sitting next to a red '%14 arttı' delta chip. The co
   K: `SecondLineContent` passes `color = if (h.rising) priceUp else priceDown`, so the 24x16dp line is drawn in error red #B3261E or success green #2E6B45, duplicating the delta chip's signal and putting a

[COSMETIC] Suggestion chip has no hairline border
   ui/components/Chips.kt:137
   T: Öneri çipi: `height:40px;padding:0 14px;border-radius:999px;background:#F1E7DB;border:1px solid #E7DACB` — the fill and a 1px hairline border together, for all four states shown.
   K: `SuggestionChip` clips to a circle and fills with `surfaceVariant` but never applies a border, so the chip edge is only the surface/surfaceVariant tone step. Horizontal padding is also `Spacing.md` (1
