# Araştırma Bulguları — planlama aşamasının girdisi

12 ajan, ~400 kaynak sorgusu. Aşağıdakiler **kararları değiştiren** bulgular. Doğrulama ajanları bazı ilk bulguları çürüttü — çürütülenler de burada, çünkü onlara güvenip kod yazmak en pahalı hata olurdu.

## 🔴 Planı değiştiren beş bulgu

### 1. Türk market fişinde okunabilir kod YOK — bu yapısal, aşılabilir bir engel değil
GİB'in Karekod Standardı Kılavuzu V1.1 (Temmuz 2023) doğrudan indirilip incelendi. QR yükü **sadece başlık alanlarını** taşıyor: VKN/TCKN, senaryo, tip, tarih, no, ETTN, para birimi, mal-hizmet toplam, KDV matrah, hesaplanan KDV, ödenecek. **Sıfır satır kalemi, tasarım gereği.** Dahası standardın kapsam listesi ÖKC fişini tamamen dışarıda bırakıyor — GİB'in Aralık 2025 tarihli ÖKC format kılavuzundaki örnek market fişinde ne QR var ne ETTN.

Fiş üzerinde bazen görünen "TR Karekod" bir **ödeme** QR'ı (BKM), belge QR'ı değil. Satın alma verisi içermiyor.

**GİB'e vatandaş erişimi de kapalı.** e-Arşiv Portal endpoint'leri mükellef kullanıcı kodu istiyor, sadece belge *listesi* döndürüyor, ve market fişi GİB'e zaten Z-raporu içinde toplu gidiyor — tüketici başına çekilebilir bir belge olarak hiç var olmuyor.

→ **Fiş fotoğrafı + görsel LLM tek yol. Bu bir fallback değil, birincil mimari.** QR/e-Arşiv/"TC kimliğinle giriş yap fişlerini çek" için tek saat bütçeleme.

### 2. marketfiyati.org.tr barkod API'si YOK — ilk araştırma yanlıştı, doğrulama ajanı çürüttü
İlk araştırma `POST /api/v2/searchByIdentity` ile barkoddan ürün+fiyat çözdüğünü ve test ettiğini iddia etti. Doğrulama ajanı aynı endpoint'i 6 gerçek EAN-13 ile denedi: **6/6 `numberOfFound: 0`.** Endpoint marketfiyati'nin *kendi* iç katalog ID'sini alıyor (`1YXB` gibi 4 karakterlik token), barkodu değil. Katalog şemasında barcode/EAN/GTIN alanı hiç yok.

Ama `/api/v2/search` (kelime araması) **gerçekten çalışıyor**: başlık, marka, gramaj, kategori, BİM/A101/ŞOK/Migros/CarrefourSA/Tarım Kredi/Hakmar için mağaza bazlı fiyat, lat-lon filtresi, ~saatlik tazelik. Uyarı: **User-Agent header'ı olmayan her istek 404 dönüyor.**

→ Raf barkodu tarama akışını plandan sil. marketfiyati'yi **fiyat karşılaştırma ve ürün normalizasyonu** için kullan, yakalama için değil. Endpoint'i her an kapanabilir kabul et, agresif cache'le.

### 3. Aritmetik doğrulama formülü Türkiye için yanlıştı
İlk plan `satır toplamı − indirim + KDV = TOPLAM` diyordu. Türkiye'de perakende fiyatları **kanunen KDV dahil** — TOPKDV, TOPLAM'ın *içindeki* verginin bilgi amaçlı dökümü, eklenecek bir kalem değil. Bu formül her fişi vergi tutarı kadar aşar ve **%100'ünü manuel düzeltmeye yollar.**

→ Doğru değişmez: **`satır toplamları − İNDİRİM satırları = TOPLAM`**, KDV/TOPKDV satırları toplamın tamamen dışında. Tartılı ürünlerde yuvarlama için **±0,05 TL tolerans** — yoksa manavda sürekli yanlış alarm verir.

### 4. Peekaboo ve CameraK ölü — FileKit kullan
`io.github.onseok:peekaboo` hâlâ 0.5.2'de ve o sürüm **15 Nisan 2024** tarihli (28 ay). CMP 1.6.2'ye pinli. Bahsi geçen "jordond fork"u Maven Central'da yok. CameraK'nın grubu Maven Central'da **0 artifact** döndürüyor.

**FileKit gerçekten aktif:** 0.15.0 (8 Ağu 2026), 0.14.2 (13 Haz 2026), 0.14.1 (5 May 2026). → **0.14.2'ye pinle** (0.15.0 dört günlük).

Doğrulanmış diğer sürümler: **Room 3.0.1** (`androidx.room3`, 29 Tem 2026 — Google Maven'dan birebir doğrulandı, KMP-native, GA). Room 2.x tutorial'ları eski koordinatı import ettirecek, dikkat.

### 5. Supabase ücretsiz katman 7 günde duraklıyor — ve akla gelen çözüm de ölüyor
7 günlük düşük aktivite duraklaması güncel politika, geri alma **manuel**. Akla gelen çözüm GitHub Actions cron — ama **GitHub, 60 gün aktivite olmayan repo'larda zamanlanmış workflow'ları devre dışı bırakıyor.** İki kişilik bir uygulamada repo aylarca sessiz kalır: önce keep-alive sessizce ölür, sonra veritabanı duraklar. İki sessiz hata üst üste.

→ Supabase'in kendi **pg_cron**'unu veya bir **Cloudflare Worker cron**'unu kullan (API anahtarı proxy'si için zaten bir Worker açacaksın).

## 🟡 Ölçülmemiş ama her şeyin bağlı olduğu iki varsayım

### A. Türk fişinde OCR doğruluğu — kimse test etmemiş
VLM'lerin Türkçede klasik OCR'ı yendiğine dair kanıt, **telefonla çekilmiş buruşuk termal fiş** üzerinde değil, sentetik genel belge benchmark'ında. Etkinin yönü destekli, büyüklüğü bilinmiyor. Halka açık Türkçe fiş veri seti yok, her zincirin düzeni farklı, ürün adları agresif kısaltılmış, termal baskı düzensiz soluyor.

**Planlanacak gerçekçi beklenti:** TOPLAM / tarih / market adında neredeyse kusursuz, satır **fiyatlarında** iyi, satır **ADLARINDA** belirgin şekilde kötü — ve fiyat geçmişi özelliğinin ihtiyaç duyduğu alan tam olarak ad.

→ Fallback ikinci bir çıkarım hattı değil, **hızlı düzenlenebilir kontrol ekranı** — ve bu bir hata yolu değil birinci sınıf özellik olarak ele alınmalı. (Tasarım promptunda bu şekilde spec'lendi.)

### B. En büyük risk teknik değil, aritmetik: değer eğrisi çok yavaş
Ayda 3 fiş × ~20 kalem = ayda ~60 satır, ~40 farklı ürün. Bir ürünün fiyatını karşılaştırmak için **aynı kanonik ürünün en az 2 gözlemi** gerekiyor. 6 ay sonra ~360 satır, belki 100 farklı ürün — bunların sadece tekrar eden temel gıdaları (süt, ekmek, yumurta, ~10-20 SKU) trend çizecek kadar gözleme sahip olacak. Zincirler aynı ürünü farklı kısaltıyor (A101 `PNR SUT 200`, Migros `PINAR SÜT 200ML`), ve barkod kısayolu artık yok. Manav/fırın/şarküteride EAN zaten hiç yok.

**Bunu çözmenin en ucuz yolu:** kanonik ürün katalogunu **marketfiyati `/api/v2/search`'ten önden tohumla** ki 1. ayın fişleri sıfırdan alias tablosu kurmak yerine bilinen bir kataloğa eşleşsin.

**Projenin durma sebebi büyük ihtimalle şu olacak:** 4. ay çarpışma noktası — ilk TestFlight süresi dolarken grafikler hâlâ boş. Uygulama başarısız olmuyor, sadece yeniden yüklemeye değmez hale geliyor.

## 🟢 Doğrulanmış iyi haberler

- **Anthropic API Türkiye'den kullanılabilir** — Türkiye desteklenen ülkeler listesinde. (Kontrol edilecek tek şey: kartın kabul edilmesi.)
- **Maliyet kısıt değil.** 2576px fiş ≈ 4.784 görsel token; ~1.500 prompt + ~700 çıktı token'ı ile Sonnet 5'te **fiş başına ~$0,029**. Ayda 3 fişte **yılda ~$1**. Ayda 100 fişte bile yılda ~$35 (Haiku 4.5 ile ~$7).
  - ⚠️ **İki tuzak:** (1) Sonnet 5 ve Opus 5'te adaptive thinking **varsayılan açık** ve thinking token'ları çıktı olarak faturalanıyor — yoğun bir fişte maliyeti 2-3 katına çıkarır ve mobil yakalama akışına saniyeler ekler. `thinking: {"type":"disabled"}` veya `effort:"low"` açıkça set et. (2) Structured output şeması ilk kullanımda derlenip **24 saat** cache'leniyor; ayda 3 fişte cache **her zaman** süresi dolmuş olacak, yani her istek soğuk derleme gecikmesini ödeyecek. Yakalama UX'inde buna yer bırak — gecikme hiç iyileşmeyecek.
- **CMP iOS shippable ama tek native dikiş bekle.** Alan raporları tutarlı biçimde kamerayı işaret ediyor: çok kişi kamera yüzeyi için SwiftUI'a iniyor. Çok megapiksellik fiş fotoğrafını **platform tarafında küçült**, common koda geçtikten sonra değil.
- **İki kişilik senkron için outbox+LWW muhtemelen fazla.** v1'de Realtime `postgres_changes` + yerel DB cache (çevrimdışı düzenlemenin kaybolmasını kabul ederek) ~50 satır. Outbox'ı gerçekten bir düzenleme kaybettiğini gözlemleyince ekle. Tam tasarımı yaparsan `updated_at`'i Postgres UPDATE **trigger**'ı ile zorla — istemciden gelen tek bir timestamp LWW'yi kalıcı ve izsiz biçimde bozar.

## ✅ 1. hafta de-risking (kod yazmadan önce, bu sırayla)

1. **A101/BİM/ŞOK/Migros/CarrefourSA'dan 20 gerçek fiş fotoğrafla.** Etrafında uygulama olmadan, JSON şemanla Sonnet 5'ten geçir. **Satır adı doğruluğunu fiyat doğruluğundan AYRI skorla** — karşılaştırma özelliğinin ihtiyacı ad, ve başarısız olacak olan da ad. Maliyet: ~60 sent.
2. Aynı 20 fişte, çıkan satır adlarının marketfiyati `/api/v2/search`'te bir şeye bulanık eşleşip eşleşmediğini test et. Kanonik ürün kimliğinin çözülebilir olup olmadığını bir öğleden sonrada söyler.
3. **Ancak ondan sonra** ürünün bir fiyat takipçisi mi yoksa harcama defteri mi olduğuna karar ver ve kapsamı ona göre yaz.

(1) ve (2) bitmeden tek satır Kotlin yazma.

## Kapsam dışı bırakılanlar (v1)

Mağaza bazlı ayrı listeler · raf barkodu tarama (v1.5, düzeltme aracı olarak) · geofence eş bildirimi (v1.5 — araştırmanın bulduğu en ayırt edici çift özelliği ama arka plan konumu ve bildirim yüzeyi gerektiriyor) · **tüm push bildirimleri** · ikili birliktelik önerileri ("makarna aldın, sos?" — tek hanede her şey her şeyle birlikte görünür, lift ~1'e çöker, güvenli saçmalık üretir) · mevsimsellik/Ramazan tabloları · bütçe/harcama kategorileri (Türkiye'deki her fiş uygulaması zaten harcama aracı ve hiçbiri ürün bazında fiyat geçmişi kurmuyor — o kategoriye kayma) · tarif/öğün planlama/kiler envanteri · fiyat alarmları · **elle fiyat girme** · ikiden fazla kullanıcı · kişi bazlı model (hane modeldir) · çevrimdışı OCR fallback · web/watch/widget/tablet · **halka açık store yayını** (TestFlight internal + Play internal testing yeter) · reklam/monetizasyon · CRDT senkron kütüphaneleri.
