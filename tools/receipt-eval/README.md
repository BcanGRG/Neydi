# F0.1 — Fiş doğruluk koşumu

Türk market fişini modele okutur ve **satır adı doğruluğunu fiyat doğruluğundan ayrı** skorlar.

Bu ayrım koşumun bütün amacı: fiyat karşılaştırma özelliğinin ihtiyaç duyduğu alan **ad**, ve başarısız olması beklenen alan da **ad**. İkisini tek bir "doğruluk" sayısına karıştırmak ölçümü yok eder.

## Kurulum

```bash
cd tools/receipt-eval
npm install
```

Node 24 `.ts` dosyalarını doğrudan çalıştırıyor — derleme adımı yok.

### Kimlik doğrulama

İki seçenek, tercih edilen sırayla:

```bash
ant auth login
```

`ant` CLI'ı kurmak istemiyorsan ortam değişkeni de olur:

```bash
setx ANTHROPIC_API_KEY "sk-ant-..."
```

> Anahtarı bu repoya, bir dosyaya veya sohbete **yazma**. `.gitignore` `receipts/`, `out/`, `truth/` ve `node_modules/` klasörlerini zaten dışarıda tutuyor.

## Kullanım

**1. Fişleri koy.** `receipts/` altına `.jpg` / `.png` / `.webp`. Dosya adı zinciri belli etsin, sonradan kolay okunur:

```
receipts/
  a101-2026-08-02.jpg
  migros-2026-08-11.jpg
```

**2. Çıkarımı çalıştır.**

```bash
npm run extract
```

Sonuçlar `out/<isim>.json` altına yazılır. Bayraklar:

| Bayrak | Etki |
|---|---|
| `--model claude-sonnet-5` | Modeli değiştir (varsayılan `claude-opus-5`) |
| `--thinking` | Adaptive thinking'i **aç** (varsayılan kapalı) |
| `--only a101.jpg` | Tek dosya çalıştır |

**3. Doğruluk dosyalarını yaz.** `truth/<isim>.json` — `out/` çıktısıyla aynı şekil, ama fişe bakarak elle doğrulanmış. En pratik yol: `out/` dosyasını `truth/`'a kopyala, sonra fişi elinde tutup satır satır düzelt. Skorlama yalnızca `kind: "product"` satırlarına bakar, yani KDV/TOPLAM/ödeme satırlarını düzeltmene gerek yok.

```jsonc
{
  "merchantChain": "A101",
  "receiptDate": "2026-08-02",
  "totalRead": 642.50,
  "lines": [
    { "rawText": "TM BGD EKMEK 500G", "productName": "Tam Buğday Ekmek 500 g",
      "kind": "product", "quantity": 1, "unit": "adet", "unitPrice": null, "lineTotal": 18.50 }
  ]
}
```

> **Yöntemsel uyarı:** doğruluk dosyasını modelin kendi çıktısından türetmek tam bağımsız bir ölçüm değil — düzelttiğin şeyi ölçmüş olursun. Fişi elinde tutup satır satır karşılaştır; ekrana bakarak "doğru görünüyor" deyip geçme.

**4. Skorla.**

```bash
npm run score
```

## Çıktı

```
=== TOPLAM ===
fiş sayısı          2
satır bulma         94%  (32/34)
ham metin birebir   81%
ÜRÜN ADI birebir    62%   <-- fiyat karşılaştırması buna bağlı
FIYAT birebir       97%
aritmetik kapısı   100%  (+/-0.05 TL)
```

Bu tabloda kritik satır **ÜRÜN ADI**. Fiyat neredeyse her zaman yüksek çıkar; ad çıkmazsa aynı ürünü iki fiş arasında eşleştiremeyiz ve fiyat geçmişi kurulamaz.

## Aritmetik değişmez (F0.3)

Koşum şunu doğrular:

```
Σ(ürün satırları) + Σ(indirim satırları, negatif) = TOPLAM   ±0,05 TL
```

**KDV / TOPKDV satırları toplamın dışındadır.** Türkiye'de perakende fiyatları kanunen KDV dahil basılır; TOPKDV, TOPLAM'ın *içindeki* verginin dökümüdür. Araştırmanın ilk yazdığı `+KDV` formülü yanlıştı ve her fişi manuel düzeltmeye yollardı.

±0,05 TL toleransı tartılı ürünlerin (`x,xxx KG × yy,yy TL/KG`) yuvarlaması için; toleranssız manavda sürekli yanlış alarm verir.

## API şekliyle ilgili notlar

Bu koşum üretimde Cloudflare Worker'ın (F4.3) göndereceği isteğin aynısını gönderiyor — burada ölçtüğümüz doğruluk üretimde alacağımız doğruluk.

- **Thinking varsayılan olarak AÇIK** (Claude Opus 5'te). Kapatmak açık istek gerektiriyor, ve `disabled` yalnızca `effort` **high ve altında** kabul ediliyor — `xhigh`/`max` ile birlikte 400 dönüyor. Koşum bu yüzden `effort: "high"` sabitliyor.
- **Structured output** `output_config.format` ile veriliyor; eski `output_format` parametresi kullanımdan kalktı.
- **Şema kısıtları:** her nesnede `additionalProperties: false` ve açık `required`; sayısal (`minimum`) veya metin (`minLength`) kısıtı desteklenmiyor; nullable alanlar `anyOf` ile yazılıyor.
- **Şema cache'i 24 saat.** Ayda 3 fişte cache **her zaman** soğuk olacak — soğuk derleme gecikmesi kalıcı, iyileşmeyecek. Yakalama UX'inde buna yer bırak (F4.2: fotoğraf asla bloklamaz).
- **Görsel blok metin bloğundan önce** geliyor.
- **Yüksek çözünürlük:** uzun kenar 2576px'e kadar, görsel başına ~4784 token'a kadar. Fiş fotoğrafını küçültme — ince baskı okunabilirliği bundan zarar görür. Üretimde küçültmenin sebebi doğruluk değil, iOS bellek baskısı.

## Maliyet

Fiş başına ~$0,03 (Opus 5, thinking kapalı). 20 fişlik tam koşum ~60 sent. `--thinking` ile 2–3 katına çıkar — o yüzden varsayılan kapalı, ve doğruluk farkını ölçmek istersen iki koşumu karşılaştır.
