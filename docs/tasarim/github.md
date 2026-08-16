repo: BcanGRG/Neydi
branch: main
path: composeApp/src/commonMain/kotlin/com/neydi/app/ui

## Last sync

date: 2026-08-16T22:30:00Z

### Updated in this project

- Beşinci tur cevaplandı: karar 36 (Mağazalar bölümü kalıyor, satır "Zincirler", gözlem ayrımı renkle) ve karar 33 ilişki olarak yeniden yazıldı
- Ekran 1 başlık örneği tarih merdivenine uydu ("geçen hafta · ~642 TL"); eşik tablosundan Mağazalar satırı kalktı

- `docs/12-tasarima-sorular-4.md` cevaplandı: karar defterine 32–35 eklendi (ikon taşıma yolu, karanlık tema telafisi, envanter 15, gizlilik notu + katılma kodu metni)
- Boş durum atlası tazelendi: toolbar iki hedef, tüm tutarlar `~` biçiminde, eşikler sayıyla (3 gezi), "parse" dili kalktı, çerçeve 04 → "Alışveriş kapanışı"
- İkonografi kararı A'dan B'ye döndü: Phosphor Regular, 15 ikon elle ImageVector, GRAD yerine renk kademesi; karşılaştırma tablosuna check / push_pin / content_paste eşlemesi girdi
- Karar defteri fiş dönemine ait 11 kararı tamamen bıraktı; kalan 25 karar bugünkü ürünü anlatıyor

## Screen map

| Tasarım dosyası / ekran | Repo kaynağı |
|---|---|
| Neydi - Ekran 1 Liste · plan + alışveriş modu, toolbar, etiket giriş hedefi | `ui/list/ListScreen.kt`, `ui/list/AddSheet.kt` |
| Neydi - Ekranlar 2-4 · Ekle sheet'i | `ui/list/AddSheet.kt` |
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

- **2026-08-16 · pivot** — Fiş okuma çıktı, raf etiketi geldi. Ekran 4 yeniden çizildi (kamera + onay kartı + seçiciler), Ekran 5'e marka satırları, giriş noktası Liste başlığına, Geçmiş gezi düzeyine indi. Beş karar düştü, üçü revize, 25–31 yeni. Yeni dosyalar: Gezinme sözleşmesi, İkonografi.
- **2026-08-16 · üçüncü tur (16–24)** — Tek çekim varsayılan olmuştu; kamera rehberi, eksik kalem şeridi, fotoğraf görünürlüğü kararları. Pivotla tamamı düştü.
- **2026-08-15 · ikinci tur (13–15)** — Başlıkta zincir adı, adı okunamayan satır, toplam okunamadığında manşet. Pivotla düştü.
- **2026-08-15 · birinci tur (1–12)** — Alışveriş modundan çıkış (`more_vert` → "Alışverişi bırak"), toolbar iki hedefe indi, "Verilerimi sil" onay ekranı, toast bileşeni, kurulum iki adım, Ekran 3 bölüm notları, mağaza ve Ekle sheet'i işareti.
