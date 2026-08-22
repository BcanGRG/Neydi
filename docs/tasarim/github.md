repo: BcanGRG/Neydi
branch: main
path: composeApp/src/commonMain/kotlin/com/neydi/app/ui

## Last sync

date: 2026-08-22

### Updated in this project

- On birinci tur (docs/26, tek soru) cevaplandı: karar 75 — sağdan dolum hane seçimiyle gevşetildi: dokunuş en yakın haneye tek atımlık seçim koyar (altında 2dp outline), yazılan rakam yalnız o haneyi değiştirir, uzunluk sabit, seçim düşer ve ilerlemez; ayraç alanda sabit kaldığı için yüz kat koruması bozulmuyor; "dolu alanda ilk rakam sıfırlar" yalnız seçimsiz yazımda geçerli; ⌫ değişmedi (sağdan siler, varsa seçimi düşürür). 48dp ihlali yok: hedef alanın bütünü, hane çözümü metin imleci yerleşimi gibi
- On birinci tur bildirimleri: uppercase() (market onay çipi) ve kategori kutucuğu (44dp → CategoryTile 56dp) kodda düzeltildi — tasarım değişmedi; kalan tek eksik Ürün Detayı "Bunu önerme" satırı (F6.5, yol haritasında)
- Onuncu tur (cihaz provası, docs/25) cevaplandı: karar defterine 70–74 girdi — dikeyde klavye açılınca kırpım şeridi toplanır (kart kaydırılmaz, Tarih kalır, klavye kapanınca şerit döner), Kaydet+Vazgeç yatay çifte indi (Vazgeç solda metin buton, 1:2 genişlik, her durumda), kuruş uyarısı fiyat alanındaki ilk düzenlemede susar ve o kartta geri gelmez, fiyat alanı yazar kasa gibi sağdan dolar (3 → 0,03 · 3950 → 39,50; virgül/nokta yok sayılır, dolu alanda ilk rakam sıfırlar, Kaydet değer sıfırdan çıkınca), kırpım rehber (3:2) bölgesinden alınır ve şerit 92dp'ye döner (128dp geçici yamaydı)
- Maketler ve belgeler eşitlendi: onay kartının iki çerçevesi yatay buton çiftine geçti, klavye çerçevesine "kırpım toplandı" notu ve keypad altına sağdan dolum satırı girdi; kart kuralları (bekleyen/fiyat/klavye/eksik alan) tazelendi; sözleşmede "ilk rakamda etkinleşir" → "değer sıfırdan çıkınca", yeni "Sağdan dolum" giriş kuralı, "Kart · kuruş uyarısı" akış satırı ve rotasyon satırına dikey klavye cümlesi; Compose Spec denetimine iki satır ("Fiyat alanı sağdan dolar", "Dikeyde klavye kartı örtmez")
- Kapanan kod bulgusu (soru değildi): NeydiButton pasif hâli kodda düzeltildi — tasarım sisteminin %38 + surfaceVariant kuralı doğruydu, tasarımda değişiklik yok
- Kod tarafına kalan: fiyat alanına sağdan dolum (parseMinorInput karttan çıkar, `,`/`.` yok sayılır, Kaydet değer>0, boşalan alan — TL), Kaydet+Vazgeç yatay çift, kırpımın rehber bölgesinden alınması + şerit 92dp; kırpımın toplanması ve uyarının susması cihazda zaten yapıldı — ikisi de aynen onaylandı. Önceki turdan bekliyor olabilecekler: AddSheet iki yüzeye bölünmesi, DateText ay + kuruşsuz manşet biçimi (formatWhole), HistoryScreen tutar grafiği, özetin satır içi karta dönmesi

## Screen map

| Tasarım dosyası / ekran | Repo kaynağı |
|---|---|
| Neydi - Ekran 1 Liste · plan + alışveriş modu, toolbar, etiket giriş hedefi | `ui/list/ListScreen.kt`, `ui/list/AddSheet.kt` |
| Neydi - Ekranlar 2-4 · Ekle akışı (kök hızlı yazma + keşif sheet'i) | `ui/list/AddSheet.kt`, `ui/list/ListScreen.kt` |
| Neydi - Ekranlar 2-4 · Eksik olabilir | `ui/missing/MissingItemsScreen.kt` |
| Neydi - Ekranlar 2-4 · Etiket çek (kamera, onay kartı, ürün/market seçici) | `ui/capture/CameraSurface.kt`, `ui/label/` |
| Neydi - Ekranlar 5-8 · Ürün Detayı, marka satırları | `ui/product/` |
| Neydi - Ekranlar 5-8 · Geçmiş | `ui/history/HistoryScreen.kt` |
| Neydi - Ekranlar 5-8 · Ayarlar, "Verilerimi sil", kurulum | `ui/settings/SettingsScreen.kt` |
| Neydi - Bos Durumlar · sekiz boş hâl | yukarıdakilerin tamamı |
| Neydi - Gezinme Sozlesmesi · gezinme, geri, durum koruma, hata yolları | `ui/navigation/`, `ui/list/ListScreen.kt` |
| Neydi - Ikonografi · envanter ve taşıma kararı | `ui/theme/` |
| Neydi - Tasarim Sistemi · token, bileşen, toast/snackbar | `ui/theme/` |
| Neydi - Compose Spec · renk rolleri, tipografi, denetim listesi | `ui/theme/` |

## Sync history

- **2026-08-19 · altıncı–dokuzuncu turlar (kararlar 37–69)** — Ekleme iki yola ayrıldı: kök hızlı yazma (1 dokunuş + Enter seri) + keşif sheet'i (reyon çipleri, "N ürün" sayacı öldü; karar 64, 65 boş). Trend manşeti üç kural + 3 gözlem eşiği; Geçmiş grafiği tutar ölçer (tutarsız gezi kesik konturlu, eşik 3); alışveriş özeti satır içi kart, kapatınca kalıcı gider. Gözlem/satır silme + snackbar, marka çip sheet'i, market yapışkanlığı, amber tek anlam, yatayda kart sağ panel, deklanşör geri bildirimi, 48dp tek sayı, onay kartına görünür Vazgeç, karartı %86 — doğrulama kırpımda (karar 62). İkon envanteri 19 (grid_view + keyboard); iki yeşil ayrıştı, 60 sn kayan pencere tek kural, beş bayat ayna maddesi düzeltildi.

- **2026-08-16 · beşinci tur** — Karar 36 (Mağazalar → "Zincirler", gözlem ayrımı renkle), karar 33 ilişki olarak yeniden yazıldı; Ekran 1 başlığı tarih merdivenine uydu, eşik tablosundan Mağazalar satırı kalktı.

- **2026-08-16 · pivot** — Fiş okuma çıktı, raf etiketi geldi. Ekran 4 yeniden çizildi (kamera + onay kartı + seçiciler), Ekran 5'e marka satırları, giriş noktası Liste başlığına, Geçmiş gezi düzeyine indi. Beş karar düştü, üçü revize, 25–31 yeni. Yeni dosyalar: Gezinme sözleşmesi, İkonografi.
- **2026-08-16 · üçüncü tur (16–24)** — Tek çekim varsayılan olmuştu; kamera rehberi, eksik kalem şeridi, fotoğraf görünürlüğü kararları. Pivotla tamamı düştü.
- **2026-08-15 · ikinci tur (13–15)** — Başlıkta zincir adı, adı okunamayan satır, toplam okunamadığında manşet. Pivotla düştü.
- **2026-08-15 · birinci tur (1–12)** — Alışveriş modundan çıkış (`more_vert` → "Alışverişi bırak"), toolbar iki hedefe indi, "Verilerimi sil" onay ekranı, toast bileşeni, kurulum iki adım, Ekran 3 bölüm notları, mağaza ve Ekle sheet'i işareti.
