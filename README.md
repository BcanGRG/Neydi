# Neydi

İki kişilik bir hane için ortak market listesi — ne aldığınızı hatırlayan ve ne ödediğinizi bilen.

**Kapalı döngü:** liste → markette işaretle → fiş fotoğrafı → ürün bazında fiyat geçmişi → sonraki listede öneri.

## Neden var

1. **Unutma.** Her seferinde alınan şeyler (tam buğday ekmeği gibi) listeye yazılmadığı için alınmadan eve dönülüyor.
2. **Fiyat körlüğü.** Bir ürüne geçen sefer ne ödendiği, fiyatın artıp artmadığı, başka markette daha ucuz olup olmadığı bilinmiyor.

## İki mutlak kısıt

- **Elle fiyat girilmez.** Uygulamanın tamamında sayısal klavye tek bir yerde açılır: fişten yanlış okunan bir fiyatı düzeltirken.
- **Az ekran.** Bottom navigation yok, dashboard yok, feed yok. Liste ekranı uygulamanın kendisidir; diğer her yer oraya dönen bir sapmadır.

## Teknoloji

| Katman | Seçim | Not |
|---|---|---|
| Dil / UI | Kotlin 2.4.10 + Compose Multiplatform 1.11.1 | Android + iOS tek kod tabanı |
| Navigasyon | Navigation 3 — `org.jetbrains.androidx.navigation3:navigation3-ui:1.1.1` | AndroidX `navigation3-ui`'nin iOS hedefi **yok**, JetBrains portu şart. CMP 1.10+ gerekiyor |
| DI | Koin 4.2.2 | |
| Yerel DB | Room 3.0.1 (`androidx.room3`) | Koordinat 2.x'ten değişti — eski tutorial'lar yanlış import ettirir |
| Backend | Supabase 3.7.0 | Ücretsiz katman **7 günde duraklıyor** — keep-alive `pg_cron` veya Cloudflare Worker ile, GitHub Actions ile **değil** (60 gün sessiz repo'da workflow devre dışı kalır) |
| Kamera / dosya | FileKit 0.14.2 | Peekaboo ve CameraK ölü (Peekaboo 0.5.2 = Nisan 2024) |
| Fiş okuma | Claude vision API, Cloudflare Worker proxy arkasından | Fiş başına ~$0,029; ayda 3 fişte yılda ~$1 |

## Durum

**Milestone 1 — iskelet.** Nav3 grafiği ayakta, tema ("Sıcak Kiler") bağlı, tasarım devir paketi indirilmiş, 6 hedef placeholder ekran olarak duruyor.

## Yol haritası

Tüm iş **[`docs/ROADMAP.md`](docs/ROADMAP.md)** altında: 11 faz, 66 adım, her adım bir PR.

Sıralama **risk azaltma önce**: fiş okuma doğruluğu ölçülmeden ekran yazılmıyor, çünkü satır adı doğruluğu kötü çıkarsa ürünün yarısının kapsamı değişir.

Her adımın iki kapısı var — `assembleDebug` yeşil **ve** değişikliğin bağlı telefonda gözle doğrulanması. Cihazda görülmeyen bir şey "bitti" sayılmıyor.

## Geliştirme

```bash
./gradlew :composeApp:assembleDebug
```

Bağlı fiziksel cihaza kur (her adımın 2. kapısı):

```bash
./gradlew :composeApp:installDebug
```

**iOS Windows'ta derlenmez.** Hedefler `composeApp/build.gradle.kts` içinde tanımlı ve Gradle host'un desteklemediği task'leri çalıştırmıyor — Mac'e geçildiğinde tek satır değişiklik gerekmeden derlenmeye başlar.

## Bilgi grafiği (graphify)

Proje bir graphify bilgi grafiği taşıyor: **177 node · 236 edge · 17 topluluk**. Kod AST ile (ücretsiz), `docs/` semantik çıkarımla işlendi. Rapor `graphify-out/GRAPH_REPORT.md` altında ve repoda takip ediliyor; `graph.json` / `graph.html` yeniden üretilebilir olduğu için gitignore'da.

Grafın en çok bağlantılı düğümü bir özellik değil, bir yöntem: `Adversaryal Doğrulama Ajanı` (15 kenar) — aynı doğrulama mekanizması hem isim elemesinde hem fizibilite kontrolünde çalıştığı için iki ayrı topluluğu birbirine bağlıyor.

**Klondan sonra hook'u yeniden kur.** `.git/hooks/` versiyon kontrolüne girmez, yani başka bir makinede (örneğin iOS için Mac'e geçtiğinde) hook gelmez:

```bash
graphify hook install
```

⚠️ **Windows / PowerShell 5.1 tuzağı:** graphify'ın kurulum adımı durum dosyalarını (`graphify-out/.graphify_python`, `.graphify_root`) `Out-File -Encoding utf8` ile yazar; PS 5.1 bunlara **BOM** ekler ve hook yolu `﻿C:\...` diye okuyup `WinError 123` ile patlar. Aynı sınıf hata `local.properties` için de geçerli. Dosyaları BOM'suz yaz:

```bash
python -c "import pathlib,sys; p=pathlib.Path(sys.argv[1]); p.write_text(p.read_text(encoding='utf-8-sig'), encoding='utf-8')" graphify-out/.graphify_root
```

## Bilinen ve kasıtlı borçlar

- `App.kt` düz `mutableStateListOf` back stack kullanıyor. `rememberNavBackStack`'e geçmeden önce iOS için `SavedStateConfiguration` + polymorphic `SerializersModule` gerekiyor — iOS ve web'de reflection tabanlı serialization yok. Android'de bugün sorun değil; iOS'ta "back stack restore olmuyor" diye sessizce tezahür eder.
- Fontlar (Plus Jakarta Sans + Fraunces) henüz bundle edilmedi; ölçek ve düzen doğru, yüz henüz `FontFamily.Default`.
- Fraunces **variable font olarak bundle edilmeyecek** — CMP'de `FontVariation.Settings` iOS'ta güvenilir değil ve sessizce `opsz=14` varsayılanına düşer, 44sp'de yanlış görünür. `opsz=72 SOFT=30 WONK=0 wght=600` ayarında tek statik TTF üretilecek.

## Planlama dokümanları

`../market-app-planning/` altında: isim analizi, Claude Design promptu, logo/splash promptları ve araştırma bulguları.
