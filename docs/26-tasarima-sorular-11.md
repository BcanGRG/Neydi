# 26 — Tasarıma sorular (on birinci tur): fiyat alanında imleç

**22 Ağustos 2026.** Karar 73 (sağdan dolum) uygulandı ve cihazda çalışıyor:
`3-9-5-0` artık 39,50 veriyor, provada yaşanan yüz kat hata kapandı.

Kullanıcı aynı gün somut bir eksik bildirdi. Bu tek soruluk bir tur.

---

## S1 — Uzun bir fiyatta tek haneyi düzeltmek

**Kullanıcının cümlesi:**

> *"450,99 yazınca 450 değil de 460 olmasını istediğimde 5'in oraya dokunup
> onu 6 yapabilmeliyim."*

Karar 73 bunu açıkça kapattı:

> *"İmleç, seçim, ondalık ayracı — **üç kural birden düşer**."*
> *"Sağdan dolan bir alanda ortadan düzenleme diye bir işlem tanımlı değil,
> düzeltme yeniden yazmaktır."*

Bugünkü maliyet **beş tuş** (`4-6-0-9-9`) ve kararın kendi cümlesi bunu
*"fiyat 3–5 tuş"* diye kabul ediyor. Yani kullanıcının şikâyeti kuralın
öngördüğü sınırın tam ucunda.

### Neden bunu kendiliğimizden yapmadık

Karar bir gün önce verildi ve gerekçesi hâlâ geçerli: serbest yazım
**sessiz bir yüz kat hataya** açıktı ve fiyat hafızasını zehirliyordu.

### Ama teknik engel yok — ve kararın koruduğu şey de bozulmuyor

Ondalık ayracı **alanın kendisinde sabit**: kullanıcı onu taşıyamıyor,
silemiyor, ikinci bir tane koyamıyor. Dolayısıyla imleçle **bir haneyi
değiştirmek** 3.950,00 hatasını geri getirmiyor — o hata ayracın *yokluğundan*
doğuyordu, imleçten değil.

Uygulaması `TextFieldValue` + gerçek bir `OffsetMapping` ile mümkün:
dokunulan ekran konumu hangi haneye denk geliyorsa o hane değişir, alanın
uzunluğu sabit kalır.

**Soru:** Karar 73 bu yönde gevşetilsin mi?

| | Bugün (karar 73) | Öneri |
|---|---|---|
| Yazma | sağdan dolar | **değişmez** |
| Virgül/nokta tuşu | yok sayılır | **değişmez** |
| ⌫ | sağdan siler | **değişmez** |
| Dolu alanda ilk rakam | baştan başlatır | ⚠ imleç konulduysa **başlatmaz**, o haneyi değiştirir |
| Ekrana dokunmak | yok sayılır | imleci o haneye koyar |

Son iki satır kararın metnini değiştiriyor; gerisi aynı kalıyor.

**İkincil soru:** imleç konulduğunda "dolu alanda ilk rakam sıfırlar" kuralı
ne olacak? İki davranış aynı anda tanımlı olamaz — dokunulan alanda sıfırlama
düşmeli mi, yoksa sıfırlama yalnızca alana *dokunulmadan* yazmaya
başlandığında mı geçerli?

---

## Bu turda ayrıca: denetim listesinden çıkan iki sapma *(soru değil, bildirim)*

Tasarımın kendi denetim listesi (Compose Spec · 20 madde) koda karşı
koşuldu. Yirmi maddenin on sekizi temiz; ikisi sapmıştı ve **düzeltildi**:

- **`uppercase()` kullanıcıya görünen metinde.** Market seçicinin onay çipi
  `«${ad.uppercase()}» diye yeni market` yazıyordu. Locale'siz dönüşüm
  Türkçe'de bozuyor: `işkur` → `ISKUR`, doğrusu `İŞKUR`. Liste maddesi 13
  bunu açıkça yasaklıyor. Ad artık yazıldığı gibi gösteriliyor — çipin kendi
  gerekçesi de bunu söylüyordu: *onaylanan şey "yeni market" değil TAM OLARAK
  BU AD*.

- **Kategori kutucuğu iki boyda.** Ürün Detayı kendi 44dp kutusunu çiziyordu;
  tasarım sisteminin ölçüsü 56dp (`Size.categoryTile`) ve `CategoryTile`
  bileşeni tam da bu iş için yazılmıştı. Tipografi de farklıydı. Bileşene
  bağlandı.

**Kalan tek gerçek eksik: `Bunu önerme` satırı.** Boş durum atlasının 05
karesi Ürün Detayı'nda üç satır çiziyor (*Her zamankilere ekle · Bunu önerme ·
Listeden çıkar*); kodda ikisi var. F6.5 olarak yol haritasında duruyor ve
sıradaki iş.
