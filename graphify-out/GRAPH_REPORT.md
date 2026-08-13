# Graph Report - C:\Users\buroc\AndroidStudioProjects\Neydi  (2026-08-13)

## Corpus Check
- Corpus is ~13,203 words - fits in a single context window. You may not need a graph.

## Summary
- 177 nodes · 236 edges · 17 communities (16 shown, 1 thin omitted)
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 36 edges (avg confidence: 0.85)
- Token cost: 141,568 input · 0 output

## Community Hubs (Navigation)
- Tasarım Sistemi ve İkonografi
- Fiş Yakalama ve Doğruluk
- İsim Adayları ve Eleme
- Ekran Mimarisi ve İlkeler
- Uygulama Girişi ve Ekran Kodu
- Tema ve Basılı Hal
- Fiş Okuma Teknoloji Kararları
- Navigation 3 Hedefleri
- Hareket ve Odak Halleri
- Amber Sözleşmesi Bileşenleri
- Ölçü ve Boşluk Token'ları
- Gradle Wrapper Script
- Renk Token'ları

## God Nodes (most connected - your core abstractions)
1. `Adversaryal Doğrulama Ajanı (isim taraması)` - 15 edges
2. `EKRAN 1 — Liste (ana ekran)` - 14 edges
3. `Neydi (Proje)` - 12 edges
4. `App()` - 11 edges
5. `NeydiKey` - 8 edges
6. `Iskele()` - 8 edges
7. `"Sıcak Kiler" Design System` - 8 edges
8. `NeydiIndicationNode` - 6 edges
9. `Mod A — Fiş Kontrol Ekranı` - 6 edges
10. `AccentSurface()` - 5 edges

## Surprising Connections (you probably didn't know these)
- `EKRAN 1 — Liste (ana ekran)` --implements--> `Az Ekran Kısıtı`  [INFERRED]
  docs/01-claude-design-prompt.md → README.md
- `Fraunces Statik TTF Kararı` --rationale_for--> `Fraunces (sadece display, 24sp ve üstü)`  [INFERRED]
  README.md → docs/01-claude-design-prompt.md
- `App()` --calls--> `NeydiTheme()`  [INFERRED]
  composeApp/src/commonMain/kotlin/com/neydi/app/App.kt → composeApp/src/commonMain/kotlin/com/neydi/app/ui/theme/Theme.kt
- `Neydi (Proje)` --references--> `Neydi (Seçilen İsim)`  [EXTRACTED]
  README.md → docs/00-isim-onerileri.md
- `Neydi (Proje)` --references--> `"Sıcak Kiler" Design System`  [EXTRACTED]
  README.md → docs/01-claude-design-prompt.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Neydi Kapalı Döngüsü (liste → işaretle → fiş → fiyat geçmişi → öneri)** — readme_kapali_dongu, docs_01_claude_design_prompt_ekran_liste, docs_01_claude_design_prompt_alisveris_modu, docs_01_claude_design_prompt_ekran_alisverisi_bitir, docs_01_claude_design_prompt_fis_kontrol, docs_01_claude_design_prompt_ekran_urun_detayi, docs_01_claude_design_prompt_oneri_seridi [EXTRACTED 1.00]
- **Fiş Okuma Mimarisi (araştırma bulgusundan ekran spec'ine)** — docs_03_arastirma_bulgulari_gib_karekod, docs_03_arastirma_bulgulari_fis_fotografi_vlm, docs_03_arastirma_bulgulari_ocr_dogrulugu_varsayimi, docs_03_arastirma_bulgulari_aritmetik_dogrulama, docs_03_arastirma_bulgulari_maliyet_analizi, readme_fis_okuma, docs_01_claude_design_prompt_fis_kontrol, docs_01_claude_design_prompt_duzeltme_sozlesmesi [INFERRED 0.95]
- **Tek Vektör Beş İş — Marka Asset Seti** — docs_02_logo_splash_prompt_tek_vektor_bes_is, docs_02_logo_splash_prompt_66dp_monochrome_testi, docs_02_logo_splash_prompt_android_adaptive_icon, docs_02_logo_splash_prompt_ios_app_icon, docs_02_logo_splash_prompt_splash_screen, docs_02_logo_splash_prompt_devir_teslim [EXTRACTED 1.00]

## Communities (17 total, 1 thin omitted)

### Community 0 - "Tasarım Sistemi ve İkonografi"
Cohesion: 0.09
Nodes (25): Alışveriş Sonrası Özet Kartı, CMP Teknik Tasarım Kısıtları (ihlal edilemez liste), Fraunces (sadece display, 24sp ve üstü), İşaretleme Hareketi (daire → squircle, 200ms), Kategori Kutucuğu + İki-Harf Fallback, Amber Kontrast Kuralı (accentOutline), Plus Jakarta Sans (UI / gövde / rakamlar), "Sıcak Kiler" Design System (+17 more)

### Community 1 - "Fiş Yakalama ve Doğruluk"
Cohesion: 0.10
Nodes (24): Düzeltme Sözleşmesi (her hata sınıfı ≤3 dokunuş), EKRAN 4 — Alışverişi Bitir, EKRAN 6 — Geçmiş, Mod A — Fiş Kontrol Ekranı, Mod B — Fişsiz Mutabakat, İlke: Hiçbir şey bölmez, Uygulamadaki Tek Sayısal Klavye (fiyat düzeltme), Aritmetik Doğrulama Değişmezi (KDV dahil) (+16 more)

### Community 2 - "İsim Adayları ve Eleme"
Cohesion: 0.10
Nodes (23): Adversaryal Doğrulama Ajanı (isim taraması), Alsak (kısa liste), Bizde (elendi), Kadans (elendi), Kavo (elendi), Listemiz (kısa liste), Listu (ilk turda elendi), Nelazım (elendi) (+15 more)

### Community 3 - "Ekran Mimarisi ve İlkeler"
Cohesion: 0.14
Nodes (21): Alışveriş Modu (aynı ekranın farklı hali), İlke: Asla boş ekran gösterme, EKRAN 7 — Ayarlar, EKRAN 2 — Ekle (modal sheet), EKRAN 3 — Eksik Olabilir, EKRAN 8 — Kurulum (3 adım, bir daha görünmez), EKRAN 1 — Liste (ana ekran), EKRAN 5 — Ürün Detayı (fiyat geçmişi sheet'i) (+13 more)

### Community 4 - "Uygulama Girişi ve Ekran Kodu"
Cohesion: 0.17
Nodes (14): Bundle, ComponentActivity, MainActivity, App(), AlisverisiBitirScreen(), AyarlarScreen(), EksikOlabilirScreen(), GecmisScreen() (+6 more)

### Community 5 - "Tema ve Basılı Hal"
Cohesion: 0.13
Nodes (10): Modifier, NeydiIndication, NeydiIndicationNode, NeydiTheme(), Sizes, Spacing, DelegatableNode, DrawModifierNode (+2 more)

### Community 6 - "Fiş Okuma Teknoloji Kararları"
Cohesion: 0.24
Nodes (10): Anthropic API Türkiye'den Kullanılabilir, FileKit (aktif, 0.14.2'ye pinlenecek), Fiş Fotoğrafı + Görsel LLM = Birincil Mimari, GİB Karekod Standardı — Fişte Satır Kalemi Yok, Fiş Başına Maliyet Analizi (~$0,029), Peekaboo ve CameraK Ölü, Structured Output Şema Cache'i (24 saat), Adaptive Thinking Varsayılan Açık Tuzağı (+2 more)

### Community 7 - "Navigation 3 Hedefleri"
Cohesion: 0.39
Nodes (8): AlisverisiBitir, Ayarlar, EksikOlabilir, Gecmis, Kurulum, Liste, NeydiKey, NavKey

### Community 8 - "Hareket ve Odak Halleri"
Cohesion: 0.32
Nodes (6): Color, focusRing(), Modifier, Shape, Motion, pressable()

### Community 9 - "Amber Sözleşmesi Bileşenleri"
Cohesion: 0.60
Nodes (5): AccentChip(), AccentStrip(), AccentSurface(), Modifier, Shape

### Community 10 - "Ölçü ve Boşluk Token'ları"
Cohesion: 0.33
Nodes (5): Elevation, NeydiExtraShapes, SafeArea, SizesExtra, SpacingExtra

### Community 11 - "Gradle Wrapper Script"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **25 isolated node(s):** `NeydiExtraColors`, `SpacingExtra`, `NeydiExtraShapes`, `SizesExtra`, `SafeArea` (+20 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **1 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Neydi (Proje)` connect `Fiş Yakalama ve Doğruluk` to `Tasarım Sistemi ve İkonografi`, `İsim Adayları ve Eleme`, `Fiş Okuma Teknoloji Kararları`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `EKRAN 1 — Liste (ana ekran)` connect `Ekran Mimarisi ve İlkeler` to `Tasarım Sistemi ve İkonografi`, `Fiş Yakalama ve Doğruluk`?**
  _High betweenness centrality (0.081) - this node is a cross-community bridge._
- **Why does `Adversaryal Doğrulama Ajanı (isim taraması)` connect `İsim Adayları ve Eleme` to `Fiş Yakalama ve Doğruluk`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `App()` (e.g. with `.onCreate()` and `AlisverisiBitir`) actually correct?**
  _`App()` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `NeydiExtraColors`, `SpacingExtra`, `NeydiExtraShapes` to the rest of the system?**
  _25 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Tasarım Sistemi ve İkonografi` be split into smaller, more focused modules?**
  _Cohesion score 0.09333333333333334 - nodes in this community are weakly interconnected._
- **Should `Fiş Yakalama ve Doğruluk` be split into smaller, more focused modules?**
  _Cohesion score 0.10144927536231885 - nodes in this community are weakly interconnected._