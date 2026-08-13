# Graph Report - Neydi  (2026-08-13)

## Corpus Check
- 21 files · ~15,949 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 195 nodes · 253 edges · 18 communities (17 shown, 1 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 36 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `903dd196`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- "Sıcak Kiler" Design System
- Neydi (Proje)
- Adversaryal Doğrulama Ajanı (isim taraması)
- EKRAN 1 — Liste (ana ekran)
- App
- NeydiIndicationNode
- Fiş Fotoğrafı + Görsel LLM = Birincil Mimari
- NeydiKey
- focusRing
- AccentSurface
- Dimens.kt
- gradlew
- Color.kt
- Neydi — Yol Haritası

## God Nodes (most connected - your core abstractions)
1. `Neydi — Yol Haritası` - 15 edges
2. `Adversaryal Doğrulama Ajanı (isim taraması)` - 15 edges
3. `EKRAN 1 — Liste (ana ekran)` - 14 edges
4. `Neydi (Proje)` - 12 edges
5. `App()` - 11 edges
6. `NeydiKey` - 8 edges
7. `Iskele()` - 8 edges
8. `"Sıcak Kiler" Design System` - 8 edges
9. `NeydiIndicationNode` - 6 edges
10. `Mod A — Fiş Kontrol Ekranı` - 6 edges

## Surprising Connections (you probably didn't know these)
- `EKRAN 1 — Liste (ana ekran)` --implements--> `Az Ekran Kısıtı`  [INFERRED]
  docs/01-claude-design-prompt.md → README.md
- `Native → Compose Devir Teslimi` --references--> `Kotlin 2.4.10 + Compose Multiplatform 1.11.1`  [INFERRED]
  docs/02-logo-splash-prompt.md → README.md
- `Fraunces Statik TTF Kararı` --rationale_for--> `Fraunces (sadece display, 24sp ve üstü)`  [INFERRED]
  README.md → docs/01-claude-design-prompt.md
- `App()` --calls--> `NeydiTheme()`  [INFERRED]
  composeApp/src/commonMain/kotlin/com/neydi/app/App.kt → composeApp/src/commonMain/kotlin/com/neydi/app/ui/theme/Theme.kt
- `Neydi (Proje)` --references--> `Neydi (Seçilen İsim)`  [EXTRACTED]
  README.md → docs/00-isim-onerileri.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Neydi Kapalı Döngüsü (liste → işaretle → fiş → fiyat geçmişi → öneri)** — readme_kapali_dongu, docs_01_claude_design_prompt_ekran_liste, docs_01_claude_design_prompt_alisveris_modu, docs_01_claude_design_prompt_ekran_alisverisi_bitir, docs_01_claude_design_prompt_fis_kontrol, docs_01_claude_design_prompt_ekran_urun_detayi, docs_01_claude_design_prompt_oneri_seridi [EXTRACTED 1.00]
- **Fiş Okuma Mimarisi (araştırma bulgusundan ekran spec'ine)** — docs_03_arastirma_bulgulari_gib_karekod, docs_03_arastirma_bulgulari_fis_fotografi_vlm, docs_03_arastirma_bulgulari_ocr_dogrulugu_varsayimi, docs_03_arastirma_bulgulari_aritmetik_dogrulama, docs_03_arastirma_bulgulari_maliyet_analizi, readme_fis_okuma, docs_01_claude_design_prompt_fis_kontrol, docs_01_claude_design_prompt_duzeltme_sozlesmesi [INFERRED 0.95]
- **Tek Vektör Beş İş — Marka Asset Seti** — docs_02_logo_splash_prompt_tek_vektor_bes_is, docs_02_logo_splash_prompt_66dp_monochrome_testi, docs_02_logo_splash_prompt_android_adaptive_icon, docs_02_logo_splash_prompt_ios_app_icon, docs_02_logo_splash_prompt_splash_screen, docs_02_logo_splash_prompt_devir_teslim [EXTRACTED 1.00]

## Communities (18 total, 1 thin omitted)

### Community 0 - ""Sıcak Kiler" Design System"
Cohesion: 0.10
Nodes (22): Alışveriş Sonrası Özet Kartı, CMP Teknik Tasarım Kısıtları (ihlal edilemez liste), Fraunces (sadece display, 24sp ve üstü), İşaretleme Hareketi (daire → squircle, 200ms), Kategori Kutucuğu + İki-Harf Fallback, Amber Kontrast Kuralı (accentOutline), Plus Jakarta Sans (UI / gövde / rakamlar), "Sıcak Kiler" Design System (+14 more)

### Community 1 - "Neydi (Proje)"
Cohesion: 0.11
Nodes (21): Düzeltme Sözleşmesi (her hata sınıfı ≤3 dokunuş), İlke: Hiçbir şey bölmez, Uygulamadaki Tek Sayısal Klavye (fiyat düzeltme), CMP iOS Kamera Dikişi + Platform Tarafında Küçültme, FileKit (aktif, 0.14.2'ye pinlenecek), GitHub Actions Cron Keep-Alive Tuzağı, v1 Kapsam Dışı Bırakılanlar, Peekaboo ve CameraK Ölü (+13 more)

### Community 2 - "Adversaryal Doğrulama Ajanı (isim taraması)"
Cohesion: 0.09
Nodes (25): Adversaryal Doğrulama Ajanı (isim taraması), Alsak (kısa liste), Bizde (elendi), Kadans (elendi), Kavo (elendi), Listemiz (kısa liste), Listu (ilk turda elendi), Nelazım (elendi) (+17 more)

### Community 3 - "EKRAN 1 — Liste (ana ekran)"
Cohesion: 0.10
Nodes (28): Alışveriş Modu (aynı ekranın farklı hali), İlke: Asla boş ekran gösterme, EKRAN 4 — Alışverişi Bitir, EKRAN 7 — Ayarlar, EKRAN 2 — Ekle (modal sheet), EKRAN 3 — Eksik Olabilir, EKRAN 6 — Geçmiş, EKRAN 8 — Kurulum (3 adım, bir daha görünmez) (+20 more)

### Community 4 - "App"
Cohesion: 0.17
Nodes (14): Bundle, ComponentActivity, MainActivity, App(), AlisverisiBitirScreen(), AyarlarScreen(), EksikOlabilirScreen(), GecmisScreen() (+6 more)

### Community 5 - "NeydiIndicationNode"
Cohesion: 0.13
Nodes (10): Modifier, NeydiIndication, NeydiIndicationNode, NeydiTheme(), Sizes, Spacing, DelegatableNode, DrawModifierNode (+2 more)

### Community 6 - "Fiş Fotoğrafı + Görsel LLM = Birincil Mimari"
Cohesion: 0.33
Nodes (7): Anthropic API Türkiye'den Kullanılabilir, Fiş Fotoğrafı + Görsel LLM = Birincil Mimari, GİB Karekod Standardı — Fişte Satır Kalemi Yok, Fiş Başına Maliyet Analizi (~$0,029), Structured Output Şema Cache'i (24 saat), Adaptive Thinking Varsayılan Açık Tuzağı, Fiş Okuma Hattı (Claude vision API + Cloudflare Worker proxy)

### Community 7 - "NeydiKey"
Cohesion: 0.39
Nodes (8): AlisverisiBitir, Ayarlar, EksikOlabilir, Gecmis, Kurulum, Liste, NeydiKey, NavKey

### Community 8 - "focusRing"
Cohesion: 0.32
Nodes (6): Color, focusRing(), Modifier, Shape, Motion, pressable()

### Community 9 - "AccentSurface"
Cohesion: 0.60
Nodes (5): AccentChip(), AccentStrip(), AccentSurface(), Modifier, Shape

### Community 10 - "Dimens.kt"
Cohesion: 0.33
Nodes (5): Elevation, NeydiExtraShapes, SafeArea, SizesExtra, SpacingExtra

### Community 11 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 17 - "Neydi — Yol Haritası"
Cohesion: 0.11
Nodes (17): Cihaz döngüsü, Faz 0 — Risk azaltma (kod yazmadan önce), Faz 10 — Sürekli / refactor, Faz 1 — Temel borçlar, Faz 2 — Veri katmanı, Faz 3 — Liste ekranı (uygulamanın kalbi), Faz 4 — Alışveriş kapatma ve fiş, Faz 5 — Fiyat hafızası (+9 more)

## Knowledge Gaps
- **40 isolated node(s):** `NeydiExtraColors`, `SpacingExtra`, `NeydiExtraShapes`, `SizesExtra`, `SafeArea` (+35 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **1 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Neydi (Proje)` connect `Neydi (Proje)` to `"Sıcak Kiler" Design System`, `Adversaryal Doğrulama Ajanı (isim taraması)`, `EKRAN 1 — Liste (ana ekran)`?**
  _High betweenness centrality (0.130) - this node is a cross-community bridge._
- **Why does `EKRAN 1 — Liste (ana ekran)` connect `EKRAN 1 — Liste (ana ekran)` to `"Sıcak Kiler" Design System`, `Neydi (Proje)`, `Adversaryal Doğrulama Ajanı (isim taraması)`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **Why does `Adversaryal Doğrulama Ajanı (isim taraması)` connect `Adversaryal Doğrulama Ajanı (isim taraması)` to `EKRAN 1 — Liste (ana ekran)`?**
  _High betweenness centrality (0.060) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `App()` (e.g. with `.onCreate()` and `AlisverisiBitir`) actually correct?**
  _`App()` has 10 INFERRED edges - model-reasoned connections that need verification._
- **What connects `NeydiExtraColors`, `SpacingExtra`, `NeydiExtraShapes` to the rest of the system?**
  _40 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `"Sıcak Kiler" Design System` be split into smaller, more focused modules?**
  _Cohesion score 0.1038961038961039 - nodes in this community are weakly interconnected._
- **Should `Neydi (Proje)` be split into smaller, more focused modules?**
  _Cohesion score 0.11428571428571428 - nodes in this community are weakly interconnected._