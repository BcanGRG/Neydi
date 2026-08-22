# Tasarım dosyaları nasıl tazelenir

Dokuz `.dc.html` dosyası Claude Design projesi
`8eea982a-c3f6-4008-8789-81aaf478b51d`'den geliyor ve **hepsi güncel**
(21 Ağustos itibarıyla).

## Yöntem

DesignSync `get_file` çağrısı, dosya büyükse sonucu **diske** yazıyor ve
oradan betikle kopyalanabiliyor. Küçük dosyalar (~40 KB altı) **satır içi**
dönüyor; onları elle yazmak gerekiyor.

**Alt ajanların DesignSync erişimi YOK.** Bu iş ana oturumdan yapılmalı — bir
alt ajan denedi, aracı bulamadı ve içerik uydurmayı doğru şekilde reddetti.

## Kopyanın doğruluğu nasıl kontrol edilir

Elle yazılan bir kopyada `git diff` **küçük ve anlamlı** olmalı. 21 Ağustos
turunda iki dosya elle yazıldı ve diff 17 ekleme / 14 silme çıktı — hepsi
beklenen içerik değişiklikleri. Diff devasa çıkıyorsa kopya bozulmuştur.

## 21 Ağustos turunda ne değişti

| Dosya | Değişiklik |
|---|---|
| `Neydi - Ikonografi.dc.html` | İkon envanteri **17 → 19** (`grid_view`, `keyboard` — karar 64); etiketsiz ikon istisnası **altı → sekiz** (flaş ve katalog eklendi) |
| `Neydi - Compose Spec.dc.html` | Denetim listesine karar **64** (metin girişi dört yerde), **67** (manşet biçimi), **56** (tek sayı 48dp) girdi |
| `Neydi - Bos Durumlar.dc.html` | **Değişmemiş.** Dosya tarihi eskiydi ama içeriği zaten güncelmiş — bayat sanılmıştı |

## Neden önemli

Bayat bir tasarım dosyası sessiz bir tuzak. Bir ajan `NeydiIcon.kt`'ye
"etiketsiz istisna sekiz hedef" yazmayı **reddetti**, çünkü repodaki kopya
hâlâ altı diyordu ve iddiayı doğrulayamıyordu. Reddi doğruydu; eksik olan
dosyaydı.

---

## 22 Ağustos 2026 · onuncu tur

**Altı dosya betikle indirildi ve uzakla birebir doğrulandı:** Kararlar,
Ekranlar 2-4, Gezinme Sözleşmesi, Ekran 1, Ekranlar 5-8, Tasarım Sistemi.

**İkonografi güncelmiş** — bu dosyanın önceki "bayat" kaydı düştü.

**Compose Spec kısmen elle işlendi.** Araç ~40KB altındaki dosyaları satır içi
döndürüyor ve betikle yazılamıyor. Denetim listesindeki **altı eksik madde**
elle eklendi; liste artık uzakla aynı yirmi maddeyi taşıyor:

- Fiyat alanı sağdan dolar (karar 73)
- Dikeyde klavye kartı örtmez (karar 70, 71, 74)
- Safe area asimetrik ve zorunlu
- Dokunma hedefi minimum 48dp
- Sıfır dialog, sıfır push, sıfır badge
- Dynamic color / Material You kapalı

⚠ **Dosyanın geri kalanı doğrulanmadı** — yalnızca `checks` dizisi uzakla
karşılaştırıldı. Tam eşitlik için dosya elle yeniden indirilmeli.
