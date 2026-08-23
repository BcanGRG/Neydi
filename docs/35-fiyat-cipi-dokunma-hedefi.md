# 35 — Karar 105'in üç şartı aynı anda sağlanamıyor

**23 Ağustos 2026.** Kararlar 102–106 uygulandı ve **görünüm maketle birebir**
tutuyor. Tek bir madde geometrik olarak imkânsız çıktı.

## Kararın cümlesi

> *"Fiyat çipi görsel 26dp, dokunma hedefi 48dp — satırın alt yarısına dikey
> olarak yayılır, çip görselini büyütmez."*

## Neden üçü birden olmuyor

Maketin kendi ölçüleri (panel 4a'dan, `getComputedStyle` ile okundu):

| | Ölçü |
|---|---|
| Satır (dolgu dahil) | **73px** ≈ 72dp |
| Satır dikey dolgusu | 9px × 2 = 18px |
| Kimlik bandı | **24px** |
| Bantlar arası boşluk | **5px** |
| Ekonomi bandı | **26px** |

24 + 5 + 26 = 55, +18 dolgu = 73. **Ekonomi bandına 26dp kalıyor** ve çip o
bandın içinde duruyor. Çipi bandın dışına almak da mümkün değil: maket
raptiyeyi çiple **aynı dikeyde** çiziyor; çip dışarı alınınca kimlik bandı
92dp daralıyor ve raptiye satırın ortasında asılı kalıyor (denendi, görüldü).

## Denenen iki yol — ikisi de cihazda başarısız

1. **Ölçüm/yerleşim ayırmak** — düğümü 48dp ölçüp ebeveyne 26dp bildirmek.
   Compose'un isabet testi ebeveynin **bildirilen** boyutunu kullanıyor;
   görselin 8dp üstüne dokunmak çipi değil **satırı** işaretledi.
2. **`minimumInteractiveComponentSize()`** — bu sürümde yerleşimi büyütüyor;
   fiyatlı satırlar 72dp'yi aştı ve iki bant birbirinden ayrıldı.

## Bugünkü hâl

Hedef **92dp × 26dp**. Yatayda cömert, dikeyde karar 56'nın *"tek sayı 48dp"*
kuralının altında. Görünüm maketle birebir; eksik olan yalnızca hedef.

## Soru

- **(a)** Satır iki bantlı hâlde **80dp** olsun (24 + 5 + 48 − örtüşme).
  Ekranda 8–9 yerine 7–8 satır görünür.
- **(b)** Fiyat çipi **kimlik bandına** çıksın (adın sağına), ekonomi bandında
  yalnız meta + delta kalsın. O zaman çip 24dp'lik bandın içinde ama satırın
  tamamı boyunca uzatılabilir — hedef 72dp olur.
- **(c)** 26dp kabul edilsin ve karar 56'ya *"fiyat çipi istisna, çünkü
  yatayda 92dp"* diye yazılı bir istisna eklensin.
- **(d)** Çipin hedefi **ekonomi bandının tamamı** olsun (meta dahil): bant
  26dp × tam genişlik, dokununca fiyat geçmişi açılır. Meta zaten fiyatın
  geçmişini anlatıyor, yani anlamsal olarak aynı şeyin parçası.
