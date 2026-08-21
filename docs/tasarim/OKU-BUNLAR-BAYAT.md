# ⚠ Üç dosya bayat — 17 Ağustos

`docs/tasarim/` klasöründeki dokuz `.dc.html` dosyasının **altısı** 19 Ağustos
sürümüyle tazelendi. **Üçü tazelenemedi** ve hâlâ 17 Ağustos tarihli:

- `Neydi - Ikonografi.dc.html`
- `Neydi - Bos Durumlar.dc.html`
- `Neydi - Compose Spec.dc.html`

## Neden

DesignSync `get_file` çağrısı büyük dosyaları diske yazıyor ve oradan
betikle kopyalanabiliyor; bu üçü **eşiğin altında kaldığı için satır içi**
döndü ve betikle yazılamadı. Elle yeniden yazmak 105 KB HTML'i kopyalamak
demekti — **sessizce bozulmuş bir tasarım referansı, dürüstçe bayat olandan
kötüdür**, o yüzden yapılmadı.

## Bu boşluk zaten bir bedel ödetti

Bir ajan, `NeydiIcon.kt`'ye "etiketsiz ikon istisnası **sekiz** hedef" yazmayı
**reddetti** — ve haklıydı: repodaki bayat dosya hâlâ **altı** diyor ve ajan
iddiayı doğrulayamadı. Reddi doğru davranıştı; eksik olan dosyaydı.

## Bilinen farklar (taze sürümden okundu, `docs/11`'e işlendi)

| Konu | Bayat dosya | Taze sürüm |
|---|---|---|
| İkon envanteri | 17 | **19** — karar 64 `grid_view` ve `keyboard` ekledi |
| Etiketsiz ikon istisnası | altı hedef | **sekiz** — flaş (karar 60) ve katalog (karar 64) eklendi |
| İkon seti kararı | "iki yol" açık | **Phosphor Regular seçildi**, A yolu teknik olarak düştü |
| Boş Durumlar çerçeve 04 | — | özet kartı **satır içi kart**, `close` ikonlu (karar 69) |
| Compose Spec denetim listesi | — | karar 64 (dört metin alanı) ve 67 (manşet biçimi) işlendi |

**Koddaki sayı doğru olan.** `NeydiIcons` 19 ikon taşıyor ve testi bunu
kilitliyor; bayat dosyaya bakıp "17 olmalı" diye düzeltmeye kalkma.

## Nasıl tazelenir

DesignSync ile proje `8eea982a-c3f6-4008-8789-81aaf478b51d`'den üç dosyayı
`get_file` ile çekip `content` alanını birebir bu klasöre yazmak yeterli.
Alt ajanların DesignSync erişimi **yok** — bu iş ana oturumdan yapılmalı.
