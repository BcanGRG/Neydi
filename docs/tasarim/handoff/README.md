# Neydi · tasarım → kod devir paketi

Hedef repo: **BcanGRG/Neydi** (branch \`main\`) · paket \`com.neydi.app.ui.theme\`

Repo'da tema **zaten var** (\`Theme.kt\`, \`Color.kt\`, \`Type.kt\`) ve tasarımla büyük ölçüde
uyumlu. Bu klasör onları **değiştirmez**; yalnızca tasarımda tanımlı olup kodda henüz
karşılığı olmayan parçaları ekler. Çakışma olursa repo kazanır.

## Dosyalar

| dosya | hedef yol | ne ekliyor |
|---|---|---|
| \`tokens.json\` | (repoya konmaz, tasarım tarafının kaynağı) | Tüm token'lar + M3 eşlemesi + reconciliation listesi |
| \`theme/Motion.kt\` | \`composeApp/src/commonMain/kotlin/com/neydi/app/ui/theme/\` | \`Motion.settle()\` = spring(0.9, 400), 200/260 ms, \`Modifier.pressable\` (0.97 scale), \`Modifier.focusRing\` |
| \`theme/Dimens.kt\` | aynı klasör | \`SpacingExtra\`, \`NeydiExtraShapes\`, \`SizesExtra\`, \`SafeArea\`, \`Elevation\` |
| \`ui/AccentChip.kt\` | \`.../com/neydi/app/ui/components/\` | Amber sözleşmesinin tek uygulanma yeri: \`AccentSurface\`, \`AccentChip\`, \`AccentStrip\` |

## Repo'da zaten doğru olanlar (dokunma)

- Palet birebir tutuyor; \`NeydiExtraColors.accentNeedsOutline\` amber kuralını taşıyor.
- \`NeydiIndication\` ripple'ı global olarak kaldırmış (%6 overlay).
- \`NeydiShapes\`: large 20dp = liste satırı, extraLarge 28dp = bottom sheet üstü.
- \`Sizes\`: 56/68/72dp satırlar, 44dp min hedef, 1dp hairline, 1.5dp accentOutline.
- \`Type.kt\`: Fraunces yalnızca display + headlineLarge/Medium; \`headlineSmall\` bilerek PJS.
- Fraunces'i variable font olarak bundle etmeme kararı — tasarım da bunu varsayıyor.

## Kodda henüz eksik olanlar (bu paketin kapattığı)

1. **Hareket eğrisi yok.** spring(0.9, 400) ve 200/260 ms süreleri hiçbir yerde tanımlı değil.
2. **Basılı halin scale yarısı yok.** Theme.kt overlay'i veriyor, 0.97 scale çağrı yerine bırakılmış → \`Modifier.pressable\`.
3. **Odaklı hal tanımsız.** Ripple olmadığı için 2dp halka zorunlu → \`Modifier.focusRing\`.
4. **Safe area sabitleri yok.** Üst 44dp / alt 34dp; alta yapışık birincil aksiyon olmaz.
5. **Elevation politikası kodda yazılı değil.** Gölge yalnızca floating toolbar (3dp) ve ekleme butonu (4dp).
6. **Kategori kutucuğu / text field / pill şekilleri yok** → \`NeydiExtraShapes\`.
7. **Amber dolgu için ortak composable yok** → \`AccentSurface\` (kenarlığı unutmak imkânsız hale gelir).
8. **12dp boşluk adımı yok** (onay kutusu ↔ ürün adı) → \`SpacingExtra\`.

## Uyuşmazlıklar ve kararlar

| token | tasarım | repo | karar |
|---|---|---|---|
| \`dark.hairline\` | #3A322C | #2E2621 | **Repo kazandı.** tokens.json güncellendi; tasarım dosyalarındaki karanlık ayırıcılar bir sonraki geçişte hizalanacak. |
| \`PriceText\` | çipte 14sp, fiş satırında 17sp | tek stil 15sp | **Ayrılması öneriliyor:** \`PriceChip\` 14sp + \`PriceRow\` 17sp. Tek 15sp iki bağlamı da tam karşılamıyor. |
| \`Spacing\` | 12dp adımı var | yok | \`SpacingExtra.betweenCheckboxAndName\` ile eklendi. |

## Kod incelemesi listesi

- Fraunces 24sp altında hiçbir yerde geçmez (\`displayFamily\` yalnızca display + headlineLarge/Medium'da).
- \`background(extras.accent)\` yalnızca \`AccentSurface\` içinde.
- Kaydırılan içerikte elevation 0; gölge yalnızca toolbar (3dp) ve ekleme butonu (4dp).
- Her \`clickable\` yerine \`Modifier.pressable\` — basılı hal tanımsız kalmaz.
- \`uppercase()\` / \`lowercase()\` / \`capitalize()\` yok.
- Fiyat Text'lerinde tnum **ve** \`SizesExtra.priceColumn\` + \`TextAlign.End\`.
- \`0.5.dp\` yok; hairline minimum 1dp.
- \`Modifier.blur\`, renkli shadow, vibrancy yok.
- Dokunma hedefi ≥ 44dp; toolbar hedefleri 56dp.
- Dialog, push, badge yok. Bottom navigation yok.
- \`dynamicColorScheme\` yok.

## Ekran davranışları

Ekran ekran akış, üç mod, veri durumları ve boş durumlar tasarım dosyalarında —
\`Neydi - Ekran 1 Liste.dc.html\`, \`Neydi - Ekranlar 2-4.dc.html\`,
\`Neydi - Ekranlar 5-8.dc.html\`, \`Neydi - Bos Durumlar.dc.html\`,
\`Neydi - Compose Spec.dc.html\`. Repo'daki \`Placeholders.kt\` bunların yerine geçecek.
