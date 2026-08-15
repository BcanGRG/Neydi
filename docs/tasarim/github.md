repo: BcanGRG/Neydi
branch: main
path: composeApp/src/commonMain/kotlin/com/neydi/app/ui

## Last sync

date: 2026-08-15T19:05:00Z

### Updated in this project

- Alışveriş modundan çıkış: başlıkta `more_vert`, tek madde "Alışverişi bırak"
- Floating toolbar iki hedefe indi (`add` + `Bitir`), `undo`/`filter_list` kaldırıldı
- Yeni yüzeyler: "Verilerimi sil" onay ekranı, çok parçalı fiş (Fiş Kontrol + Geçmiş), toast bileşeni
- Kurulum iki adıma indi (sabitler + tempo); Ekran 3 bölüm notları ve Fiş Kontrol manşeti yazıldı
- Açık kalan iki madde de kapandı: mağaza satırı ilk fişten doğar, Ekle sheet'indeki işaret "bu listede var" demektir

## Screen map

| Tasarım dosyası / ekran | Repo kaynağı |
|---|---|
| Neydi - Ekran 1 Liste · alışveriş modu, toolbar, boş durumlar | `ui/list/ListScreen.kt`, `ui/list/AddSheet.kt` |
| Neydi - Ekranlar 2-4 · Ekran 3 notları | `ui/missing/MissingItemsScreen.kt` |
| Neydi - Ekranlar 2-4 · Fiş Kontrol manşeti, çok parçalı fiş | `ui/receipt/ReceiptCheckScreen.kt`, `data/receipt/ReceiptGrouping.kt` |
| Neydi - Ekranlar 5-8 · Ayarlar, "Verilerimi sil" | `ui/settings/SettingsScreen.kt` |
| Neydi - Ekranlar 5-8 · Geçmiş, parçalı gezi satırı | `ui/history/HistoryScreen.kt` |
| Neydi - Kararlar · yeni yüzeyler ve karar defteri | yukarıdakilerin tamamı |
| Neydi - Tasarim Sistemi · toolbar, toast, snackbar | `ui/list/ListScreen.kt`, `ui/theme/*` |
