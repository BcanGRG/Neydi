# 33 — Kısa tur: satır *sığdırılmadı*, öğeler *feda ediliyor*

**23 Ağustos 2026.** Kararlar 80–86 uygulanırken kullanıcı araya girdi ve
sorduğu şey haklı:

> *"Bu listedeki item'ların tasarımı baştan değişecekti — **chip ve badge'ler
> daha rahat yerleşsin** diye. O yapılmış mı? **Sıkışık olmasın**, oraya yeni
> tasarım istiyorum."*

Onun `docs/28`'de istediği şey **kapsamlı bir item redesign**'dı. Gelen cevap
(80–86) satırın **anatomisini değiştirmiyor**; aynı "tek satır + ikinci satır"
düzenini koruyup yer yetmediğinde öğeleri **düşürüyor**.

---

## Farkı somutlaştıralım

Karar 80'in kendi maketinden, 360dp'lik cihaz:

> *"76dp'lik «1,5 kg» tabana hiç sığmaz: **feda 3–4–5 işler (avatar, raptiye,
> rozet)**, ad 192dp."*

Yani en yaygın telefon genişliğinde, tek bir satırda:

- **eş avatarı düşüyor** — o satırı eşin eklediği bilgisi kayboluyor
- **raptiye düşüyor** — sabit olduğu görünmüyor
- **adet rozeti düşüyor** — *"1,5 kg"* kayboluyor

Rozetin feda sırasında **sonuncu** olmasının gerekçesi tasarımın kendi
cümlesi: *"yanlış adedin bedeli parayla ödenir."* Ama düşüyor. Yani kural,
kendi gerekçesine göre en pahalı öğeyi de feda edebiliyor — çünkü feda
edilecek başka bir şey kalmıyor.

**Kullanıcının istediği bu değildi.** İstediği, o öğelerin *düşmek zorunda
kalmadığı* bir düzen.

---

## Sorulmayan alternatifler

`docs/28` on üç soru sordu ve hepsi **mevcut anatominin içinde** kalıyordu
(hangi kat, hangi genişlik, hangi öğe önce düşer). Anatominin kendisini
değiştiren seçenekler hiç masaya gelmedi:

1. **İki satır varsayılan olsun.** Satır her zaman 68dp; ad üstte tek başına
   (tam genişlik), rozet + meta + çip altta. Ad hiçbir zaman yarışmaz — bugün
   ada 120dp taban koymak zorunda olmamızın sebebi ortadan kalkar.
2. **Fiyat satırdan çıksın.** Fiyat çipi 92dp + 8dp boşluk = **100dp**, yani
   satırın en pahalı öğesi ve her fiyatlı satırda var. Fiyat Ürün Detayı'nda
   zaten kuruşuyla duruyor; listede *"biliyorum"* işareti (küçük bir nokta ya
   da ince bir renk) yetmez mi?
3. **Rozet adın altına insin.** *"1,5 kg"* bir miktar, adın devamı — solda
   dikey bir sütun tutmak yerine ikinci satırın başında durabilir.
4. **Avatar satırın dışına.** Bugün en sağda 32dp yer kaplıyor ve tasarımın
   kendi maketlerinde hep **tek başına** çizilmiş. Bölüm başlığında ya da
   satırın sol kenarında ince bir şerit olarak durabilir mi?

---

## Sorular

### S1 — Feda sırası bir çözüm mü, yoksa geçici bir tampon mu?

Kullanıcının cümlesi *"sıkışık olmasın"*. 360dp'de üç öğenin birden düşmesi
sıkışıklığın çözümü mü, yoksa sıkışıklığın kabulü mü?

Kararlar 80–86'yı **uyguluyoruz** (bugün olandan kesinlikle iyi ve ölçülü).
Soru bunun **son hâl** mi yoksa bir **ara adım** mı olduğu.

### S2 — Anatomi değişecekse hangisi?

Yukarıdaki dört seçenekten biri mi, başka bir şey mi? Değişmeyecekse gerekçesi
ne — çünkü kullanıcı açıkça yeni bir düzen istedi ve gelen cevap düzeni
korudu.

### S3 — Karar 83 ile maket çelişiyor: `Single`'ın ikinci satırı var mı?

Karar 83: *"Single: **ikinci satır yok**."*
Maket verisi (**üç yerde**): `son 24,90 TL · Migros · 8 gün önce`.

Kodun bugünkü hâli ikisinin arasında ve 83'ün amacına daha uygun:
`BİM · bugün` — **fiyat yok**, yalnızca market ve tazelik. Maketinki fiyatı
ikinci kez yazıyor, yani 83'ü asıl ihlal eden o.

Bu yüzden `BİM · bugün`'ü **silmedik**. Doğrusu hangisi?

- **(a)** Kod haklı: `Single` ikinci satırı market + tazelik yazar, fiyat yok.
- **(b)** Karar 83 harfiyen: `Single`'ın ikinci satırı hiç yok, satır 56dp'ye
  düşer. (O zaman *"hangi markette, ne kadar taze"* bilgisi listede hiç
  görünmez.)
- **(c)** Maket haklı: `son 24,90 TL · Migros · 8 gün önce` — ama o zaman
  karar 83'ün *"yalnız çipte"* kuralı `Single` için delinmiş olur.
