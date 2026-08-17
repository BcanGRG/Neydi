# Faz 7 — Postgres şeması, RLS ve senkron protokolü (tasarım turu)

**16 Ağustos 2026.** Üç bağımsız öneri (izolasyon-önce, basitlik-önce,
protokol-önce) hazırlandı, her biri ayrı bir yargıç tarafından **çürütülmeye**
çalışıldı, sonra kazananın üstüne diğerlerinin en iyi fikirleri aşılandı.

> ⚠ **Bu plan henüz UYGULANMADI.** Supabase projesi (`vjinflzmjcsaicaeatic`,
> eu-central-1) boş duruyor. Uygulanmadan önce okunması gereken şey aşağıdaki
> **ölümcül kusurlar** bölümü — plan onları kapatıyor ama kusurlar kayıt
> olarak duruyor, aynı tuzaklara yeniden düşmemek için.

## Puanlar

| Açı | Toplam | İzolasyon | Doğruluk | Basitlik | Uygunluk |
|---|---|---|---|---|---|
| protokol-once | **53** | 8 | 4 | 7 | 7 |
| basitlik-once | **49** | 6 | 5 | 9 | 6 |
| izolasyon-once | **39** | 8 | 3 | 5 | 2 |

İzolasyona **üç kat** ağırlık verildi: veri sızması geri alınamaz, diğer
kusurlar düzeltilebilir.

**En dikkat çekici sonuç: üçünün de doğruluk puanı düşük (4 · 5 · 3).**
Hiçbir öneri olduğu gibi uygulanabilir değildi.

---

## Yargıların bulduğu ölümcül kusurlar

- protokol-once: OLUMCUL - household ve member'da INSERT politikasi yok, bu iki tabloya yapilan HER PostgREST upsert'i 42501 ile oldurur. Postgres'te `INSERT ... ON CONFLICT DO UPDATE` icin RLS INSERT WITH CHECK kontrolu (ExecInsert icindeki WCO_RLS_INSERT_CHECK) catisma tespitinden ONCE calisir; INSERT politikasi hic yoksa planlayici sabit `false` bir WithCheckOption ekler. Yani satir ZATEN VARSA ve niyet saf UPDATE olsa bile ifade `new row violates row-level security policy` ile patlar. Onerinin kendi istemci sozlesmesi outbox'i `household -> member -> ...` sirasiyla bosaltiyor ve Bootstrap.kt zaten yerelde household+member satirlarini uretiyor - yani ilk senkron ADIM 1'DE kalici olarak takilir ve arkasindaki her sey bloke olur. Bu, onerinin madde 6'da bizzat korktugu hatanin ta kendisi. Duzeltme ucuz: bu iki tabloya `for insert with check (false)` benzeri bir seyi DEGIL, istemcinin upsert yerine PATCH (?id=eq.X) kullanmasini sartlamak, ya da RPC disi INSERT'i engelleyen ama upsert'e izin veren bir INSERT politikasi + tetikleyici muhafiz yazmak.

- protokol-once: OLUMCUL - `on_conflict=trip_id,product_id` + `resolution=merge-duplicates` birincil anahtari EZER. PostgREST govdedeki TUM kolonlar icin `DO UPDATE SET col = EXCLUDED.col` uretir, PK'yi ozel olarak dislamaz. Payload `id` icerdigine gore sunucudaki satirin `id`'si ikinci cihazin UUID'siyle DEGISIR. Oneri bunun tersini acikca iddia ediyor ('ikincisi o satiri DO UPDATE eder ve id'si degismez') ve bu yanlis. Daha kotusu: onerilen tespit mekanizmasi ('donen id yereldekinden farkliysa yeniden anahtarla') ISE YARAMAZ, cunku `return=representation` ile geri donen id her zaman iten cihazin KENDI id'sidir - sinyal hicbir zaman atesleneme. Sonuc: A iter (id=A), B iter (id=B), A ceker ve A'nin yerelinde eslesmeyen satir kalir, A tekrar iter (id=A)... iki telefon arasinda sonsuz id ping-pongu, her donuste bir server_version yakiliyor ve Realtime kapi zili caliyor. Ayni desen product_alias ve suggestion_block icin de gecerli. Bu, protokolun karar verilmemis en buyuk deligi.

- protokol-once: Madde 6'nin ('sunucu, istemcide OLMAYAN hicbir kisit tasimaz') kendi SQL'i tarafindan cignenmesi: `constraint household_id_len check (char_length(id) between 16 and 64)`. Room'da boyle bir kisit yok. UUIDv7 (36 karakter) gectigi icin uretimde uyuyor ama testlerdeki/fixture'lardaki kisa id'ler ('m1', 'id-1' - ConventionsTest, ListRepositoryTest, DuplicateObservationTest hepsi boyle uretiyor) create_household'i 400'le reddeder. Kural koyup ilk firsatta cignemek, kuraldan daha kotu.

- protokol-once: `trip_line`'in benzersiz indeksi (trip_id, product_id) GLOBAL - household_id icermiyor - ve benzersizlik kontrolu RLS'i atlar. Bu iki sey birlesince RLS sinirini asan iki sey uretiyor: (a) varlik oracle'i - saldirgan kendi household_id'siyle o cifti yazmayi deneyip 23505 alip almamasina bakarak baska bir hanede o satirin VAR OLDUGUNU ogrenir; (b) kalici yazma jam'i - saldirgan kurbanin (trip_id, product_id) yuvasini kendi household_id'siyle isgal ederse, kurbanin mesru upsert'i o satira carpar, UPDATE USING politikasi kurbani reddeder ve o op outbox'ta SONSUZA KADAR takilir. UUID bilmek gerektigi icin dar ama madde 9'daki 'degmez' gerekcesi bu iki sonucu hesaba katmiyor.

- protokol-once: Madde 9'daki kabul edilen FK deligi itiraf edilenden GENIS. Yalnizca trip_line.trip_id degil: product_id, added_by_member_id, store_id, owner_member_id, suggestion_event.product_id/trip_id, price_observation.product_id/store_id, product_alias.product_id - hicbiri hedefin ayni haneye ait oldugunu denetlemiyor. Tek basina sizinti degil (satirlar kurbanin cekmesinde household_id filtresine takiliyor) ama yukaridaki global-unique maddesiyle birlesince yazma-jam DoS'unun yuzeyi oluyor.

- protokol-once: Yetki hijyeninde bosluk: `app.new_join_code()`, `app.sync_row_guard()` ve `app.household_code_guard()` icin `revoke ... from public` YOK. Postgres yeni fonksiyonlara varsayilan olarak PUBLIC'e EXECUTE verir; `app` semasi PostgREST'e acik olmadigi ve `authenticated`in dogrudan SQL yolu bulunmadigi icin SOMUT OLARAK somurulebilir degil, ama diger yedi fonksiyonda titizlikle yapilan revoke'un burada unutulmus olmasi savunmanin tek katmana (sema gizliligine) indigi anlamina geliyor. Fonksiyon sayilabilir, yani gozden kacan bir kalem degil - tutarsizlik.

- protokol-once: `join_household` iki sessiz mantik hatasi tasiyor: (a) `insert into public.member ... on conflict (id) do nothing` - kullanici daha once bu haneden soft-delete edildiyse eski satir deletedAt dolu halde durur, insert sessizce hicbir sey yapmaz ve fonksiyon `ok:true` doner; kullanici basarili katildim sanir ama `current_household_ids()` o satiri `deleted_at is null` ile eledigi icin hala kilitli kalir. `do update set deleted_at = null, auth_user_id = ...` olmaliydi. (b) `create_household` hesabin zaten bir haneye bagli olup olmadigini 409 ile denetlerken `join_household` ayni denetimi HIC yapmiyor - ayni hesap ikinci bir haneye katilabiliyor, `current_household_ids()` iki satir donmeye basliyor. Asimetri kasitli degilse hata, kasitliysa yazilmamis.

- basitlik-once: CIHAZLAR ARASI UNIQUE CARPISMASI OUTBOX'I KALICI TIKAR (en agir kusur, uygulamanin EN OLASI es zamanli eylemi). C:\Users\buroc\AndroidStudioProjects\Neydi\composeApp\schemas\com.neydi.app.data.db.NeydiDatabase\5.json dogruluyor: trip_line'da unique index_trip_line_tripId_productId, suggestion_block'ta unique index_suggestion_block_householdId_productId, product_alias'ta unique dogal anahtar VAR ve oneri bunlarin ucunu de sunucuya aynaliyor. Tek cihazda Room indeksi mukerreri onler; IKI cihazda hicbir sey onlemez. Iki kisi cevrimdisiyken ayni geziye 'sut' ekler -> ayni (trip_id, product_id), FARKLI istemci uretimi TEXT id. Ikinci push 23505 alir. pending_op FIFO (created_at sirasi, attempts + lastError) oldugu icin kuyrugun basi SONSUZA KADAR tikanir. Ustelik cekimde de kirilir: karsi cihaz gelen satiri kendi Room unique indeksine carptirir. Bu, onerinin CHECK kisitlarini reddederken kullandigi tam gerekcenin ('sunucu reddi = sessizce tikanmis outbox, en kotu hata bicimi') kendi ustune dusmesi - tasarim kendi icinde tutarsiz. Cozum yonu: ya bu ucu sunucuda dusur (ayna sadakati burada ZARAR veriyor), ya da dogal anahtari PK yap (deterministik id = hash(trip_id, product_id)) ki carpisma red degil upsert olsun. ROADMAP.md L249'daki 'F7.5 outbox+tombstone+add-beats-remove' zaten birlestirme yonunu isaret ediyor.

- basitlik-once: join_code ISTEMCIYE YAZILABILIR VE AYNALANAN BIR KOLON - butun davet guvenligi kagit uzerinde. Household.kt:28'de `val joinCode: String? = null` ZATEN VAR (Room semasinda da var), ama joinCodeExpiresAt YOK. Sunucu ise household'in BUTUN kolonlarinda `grant update ... to authenticated` veriyor, kolon bazli grant yok. Sonuclari: (a) bayat bir istemci kendi yerel kopyasindaki joinCode'u push eder -> tuketilmis kod DIRILIR ya da canli kod NULL'lanir; bir guvenlik jetonu LWW'ye birakilmis. (b) Istemci PostgREST uzerinden dogrudan `update household set join_code='AAAAAAAA'` yazabilir, join_code_expires_at NULL kalir ve kod NULL expiry'yi 'hic dolmaz' sayiyor -> 39 bitlik entropi de 60 dakikalik TTL de tamamen atlanir. (c) join_code_expires_at sunucuda var Room'da yok; yani 'aptal tablo-tablo ayna' varsayimi bu tabloda ZATEN kirik. Cozum: join_code + expires_at'i sunucuya ozel ayri bir tabloya al, ya da `grant update (name, updated_at, deleted_at) on household` ile kolon bazli kisitla; o kolonlara yalnizca new_join_code/join_household dokunsun.

- basitlik-once: member.user_id ISTEMCIYE YAZILABILIR VE GUVENLIK SINIRININ TA KENDISI. `grant select, insert, update on public.member to authenticated` user_id'yi de kapsiyor; politikanin WITH CHECK'i YALNIZCA household_id'yi kisitliyor, user_id'ye hic bakmiyor. Dolayisiyla A hanesindeki kimlikli bir kullanici: (1) `insert into member (id, household_id, user_id, ...) values (yeni, A, <herhangi bir auth.users uuid>, ...)` yazabilir -> yabanci kullanici A hanesine SOKULUR; UNIQUE(user_id) yuzunden o kullanici artik kendi hanesine ait olamaz, current_household_id() A doner, kendi verisinden koparilir ve A'nin verisini okur. (2) `update member set user_id = null where id = <es>` ile esini haneden KALICI OLARAK ATABILIR. Disari dogru okuma izolasyonu kirilmiyor (B hanesini hala okuyamiyorsun), o yuzden veri sizintisi degil - ama uuid'sini ogrendigin herkese uygulanabilen bir hesap-ele gecirme/kilitleme ilkeli ve bunu bir politika gozden kacmasi degil DOGRUDAN SEMA veriyor. Cozum: user_id'yi authenticated icin guncellenemez yap (kolon bazli grant, ya da `new.user_id is distinct from old.user_id` ise reddeden bir tetikleyici); member INSERT'i yalnizca iki RPC'den gecsin.

- basitlik-once: `revoke all on function ... from public` anon'un EXECUTE yetkisini KALDIRMAZ - dosyanin 'anon'dan butun yetkiler acikca geri alindi' iddiasi YANLIS. Supabase dokumantasyonu (Database Functions, guvenlik bolumu) bunu acikca soyluyor: kisitlamak icin hem `public`'ten HEM DE `anon, authenticated` rollerinden ayri ayri revoke etmek gerekir, cunku Supabase public semasindaki YENI fonksiyonlara anon/authenticated icin varsayilan yetki (alter default privileges) tanimlar. Oneri yalnizca `from public` yapiyor; anon uc RPC'de de EXECUTE'u KORUR. Somut sonuc: kimlik dogrulanmamis bir istemci create_household/join_household/new_join_code cagirabilir. Zarar simdilik kazayla sinirli (create ve join `auth.uid() is null` kontrolu yapiyor), ama new_join_code'da auth.uid() KONTROLU HIC YOK - guvenligi yalnizca 'current_household_id() nasil olsa NULL doner' tesadufune yaslaniyor. Bu kadar ozenli bir dosyada bir kontrol degil bir kaza savunma sayilamaz. Cozum: uc fonksiyon icin de `revoke all on function ... from anon;` ekle ve new_join_code'a auth.uid() guardi koy.

- basitlik-once: KATILMA KODU ENTROPISI IDDIA EDILENIN YARISI VE 'modulo yanliligi yok' ONERMESI YANLIS. private.random_join_code() `uuid_send(gen_random_uuid())` uzerinden 0..7 bayt okuyor; gen_random_uuid() bir v4 UUID uretir ve v4'te 6. baytin (0-tabanli) ust nibble'i SURUM ALANI olarak 0x4'e SABITLENMISTIR. Yani o bayt yalnizca 0x40-0x4F = 64..79 arasindadir, %32 -> yalnizca 0..15. Sekiz karakterin yedincisi 32 degil 16 degerlidir. Gercek entropi 7x5 + 4 = 39 bit ~ 5.5e11; dosyanin yazdigi ~1.1e12 tam iki kat fazla. (Varyant bitlerini tasiyan 8. bayt araligin disinda kalmis - bu tasarim degil sans.) '256 %% 32 = 0 oldugu icin modulo yanliligi yok' akil yurutmesi TEKDUZE baytlar icin dogru, ama kaynak baytlarin hepsi tekduze degil; onerme uygulandigi yerde gecersiz. Tek kullanimlik + 60 dk TTL sayesinde SOMURULEBILIR degil, ama dosya iki kat yanlis bir sayi ve saglanmayan bir ozellik iddia ediyor - hiz sinirlamasini reddeden gerekce de tam bu sayiya dayaniyordu. Cozum: `extensions.gen_random_bytes(8)` (pgcrypto Supabase'de hazir) ya da uuid'nin 0..5 ve 9..10 baytlarini oku.

- basitlik-once: OpType.DELETE OUTBOX'TA VAR AMA SUNUCUDA DELETE YETKISI YOK. C:\Users\buroc\AndroidStudioProjects\Neydi\composeApp\src\commonMain\kotlin\com\neydi\app\data\db\Sync.kt:37 -> `enum class OpType { INSERT, UPDATE, DELETE }`. Oneri 'istemci hicbir zaman DELETE atmaz' diyor; bu koda dair bir VARSAYIM ve kod bugun bunun aksini tasiyor. Herhangi bir yol DELETE kuyruga koyarsa push 42501 alir ve yine kuyrugun basi kalici tikanir - 1 numarali kusurla ayni olum bicimi. Ya bu fazda enum'dan DELETE'i dusur, ya da push katmaninda DELETE -> soft-delete UPDATE cevirisini SOZLESME olarak yaz.

- basitlik-once: sync_meta ONERININ VARSAYDIGI IMLEC SEKLINE UYMUYOR. Room'daki sync_meta: PK householdId, lastPulledAt INTEGER, lastPushedAt INTEGER, cursor TEXT - yani hane basina TEK imlec. Onerinin istemci sozlesmesi ise TABLO BASINA `server_updated_at >= :cursor` imleci tarif ediyor. Ya `cursor TEXT` bir JSON haritasina donusturulecek (hicbir yerde yazmiyor, semada da belli degil) ya da tek global imlec kullanilacak - o zaman 60 saniyelik geri sarma her cekimde 11 tabloya birden uygulanir ve 'bedeli birkac satir' iddiasi hafife alinmis olur. sema bu migration'in kapsaminda olmadigi icin bunu kimse istemci yazilana kadar fark etmez.

- izolasyon-once: ÖLÜMCÜL 1 — leave_household() HER ZAMAN patlar. private.guard_member_row() içinde 'if old.user_id is not null and new.user_id is distinct from old.user_id then raise' var; leave_household() ise tam olarak 'set user_id = null' yapıyor. null, dolu bir uuid'den distinct'tir → her çıkış denemesi 42501 ile ölür. Aynı kural member.user_id -> auth.users(id) ON DELETE SET NULL yolunu da kırar: FK'nin tetiklediği SET NULL gerçek bir UPDATE'tir ve trigger'ı ateşler, yani Supabase'te bir kullanıcıyı silmek 'Database error deleting user' ile başarısız olur (Supabase troubleshooting: dashboard-errors-when-managing-users-N1ls4A, sebep listesinde 'trigger on referencing table' birinci sırada). Hesap silme = KVKK/GDPR yolu kapalı. Düzeltme: guard'ı 'yalnızca dolu -> BAŞKA dolu değere geçiş yasak' olacak şekilde yaz (new.user_id is not null AND new.user_id <> old.user_id).

- izolasyon-once: ÖLÜMCÜL 2 — Kaba kuvvet freni ÖLÜ KOD. private.join_attempt sayacı artırılıyor, sonra yanlış kodda 'raise exception' ediliyor. PostgREST her RPC'yi tek transaction'da çalıştırır; RAISE transaction'ı ROLLBACK eder ve sayaç artışı da geri alınır. Yani başarısız denemeler ASLA sayılmaz, tablo sadece 'attempts = 0' yazılarak sıfırlanır. Metindeki '15 dakikada 10 deneme freni' iddiası tamamen yanlış — sınırsız deneme hakkı var. Fren istiyorsan RAISE etme: fonksiyon (status, household_id, member_id) döndürsün ve hatayı veri olarak taşısın ki transaction commit olsun.

- izolasyon-once: ÖLÜMCÜL 3 — CHECK kısıtları uydurma; dört tablonun HİÇBİR satırı yazılamaz. Gerçek değerler (C:\Users\buroc\AndroidStudioProjects\Neydi\composeApp\src\commonMain\kotlin\com\neydi\app\data\db\Shopping.kt:169 TripStatus = PLANNING/SHOPPING/CLOSED; Shopping.kt:150 TakeOutcome = TAKEN/NOT_NEEDED/FORGOTTEN; Suggestion.kt:51 SuggestionOutcome = SHOWN/ADDED/REJECTED/IGNORED; Suggestion.kt:99 BlockSource = AUTO/MANUAL) ile SQL'deki listeler ('draft','active','completed','abandoned' / 'taken','skipped','replaced' / 'pending','accepted','rejected','expired' / 'user','system') sıfır kesişiyor — büyük/küçük harf bile tutmuyor. Room v5'te trip.status'un defaultValue'su zaten 'PLANNING'. Sonuç: trip, trip_line, suggestion_event, suggestion_block push'ları %100 reddedilir ve pending_op sonsuza kadar yeniden dener. Bu, önerinin kendi 4 numaralı ödününü ('sunucuya iş kuralı koymayacağız, çünkü çevrimdışı kuyruğu kalıcı kilitler') doğrudan çiğniyor.

- izolasyon-once: ÖLÜMCÜL 4 — create_household() ikinci kullanıcıda çalışmaz. C:\Users\buroc\AndroidStudioProjects\Neydi\composeApp\src\commonMain\kotlin\com\neydi\app\data\Bootstrap.kt:10 'const val DEFAULT_HOUSEHOLD_ID = "0198f2a1-0000-7000-8000-000000000001"' — derleme sabiti, her kurulumda AYNI, ve on kadar ViewModel'e hardcode edilmiş. RPC hane id'sini istemciden aldığı için dünyada ilk çağıran kazanır, geri kalan herkes unique_violation alır; üstelik 'oracle olmasın' diye hata genelleştirildiği için kimse sebebini teşhis edemez. Ayrıca istemci id seçebildiği için ucuz bir squat/DoS yüzeyi doğar. Doğrusu: hane id'sini SUNUCU üretsin (gen_random_uuid()) ve döndürsün. ROADMAP'in 'Açık kararlar 4 — Hane yeniden anahtarlama (Faz 7)' maddesi hâlâ açık; öneri bu kararı sessizce verilmiş sayıyor.

- izolasyon-once: ÖLÜMCÜL 5 — Kısıt asimetrisi pull tarafını kilitler. Room v5'te ÜÇ unique indeks var: product_alias(householdId, storeChain, rawTextNormalized), trip_line(tripId, productId), suggestion_block(householdId, productId). Sunucuda hiçbiri yok (ödün 4 gereği bilerek). İki cihaz aynı satırı üretirse sunucu ikisini de kabul eder, sonra pull sırasında Room'un UNIQUE'i ikinciyi reddeder — sıkışma push'tan pull'a taşınmış olur, orada kurtarmak daha zordur. Ters yönde de var: Room'da store.chain ve product.categoryId NOT NULL, SQL'de ikisi de nullable; sunucudan gelen tek bir NULL Room insert'ini düşürür.

- izolasyon-once: ÖLÜMCÜL 6 — synced_at imleci satır kaybeder. Trigger 'new.synced_at := now()' yazıyor; now() transaction BAŞLANGIÇ zamanıdır, commit zamanı değil. Uzun süren T1 işlemi (synced_at = t1) T2 işleminden (synced_at = t2 > t1) SONRA commit edebilir. İstemci arada pull yapıp imleci t2'ye çekerse t1'li satır 'synced_at > t2' filtresine bir daha asla düşmez — sessiz, kalıcı veri kaybı. Öneri istemci saat kaymasını çözdüğünü söylerken yerine commit-sırası yarışını koymuş. (synced_at, id) bileşik imleç bunu ÇÖZMEZ. Çare: örtüşme penceresi (imleç - N dakika) veya pg_snapshot_xmin tabanlı filigran.

- izolasyon-once: İZOLASYON SIZINTISI (küçük ama gerçek) — hata oracle'ı tam kapanmamış. Yanlış kod ve süresi dolmuş kod aynı 22023'ü döndürüyor (doğru), ama 'household is full' P0001 ile ayrışıyor: dolu bir haneye ait GEÇERLİ bir kodu tahmin eden saldırgan bunu ayırt eder. Ayrıca istemci member tablosuna doğrudan INSERT edebildiği için member_limit tamamen atlanabilir; join_household içindeki sayım da 'select count(*)' ile kilitsiz, iki eşzamanlı katılım limiti aşabilir (household satırına FOR UPDATE yok).

- izolasyon-once: Katılma kodu entropisi iddiası yanlış. random_join_code() bayt'ları gen_random_uuid()'nin METİN gösteriminden alıyor; tirelersiz UUID'de 13. karakter her zaman sürüm nibble'ı '4'tür ve p_len=8 için bu tam olarak 7. üretilen karakterin yüksek nibble'ıdır. Yani byte = 0x40..0x4F, mod 32 = 0..15 → 7. karakter DAİMA '23456789ABCDEFGH' içinden gelir. '256 mod 32 = 0 olduğu için yansız' argümanı girdi bayt'ları düzgün dağılmadığı için burada geçersiz; gerçek entropi 40 değil ~39 bit ve kodların parmak izi var. Doğrusu: gen_random_bytes() (pgcrypto) veya uuid'nin metnini değil rastgele bayt'ları kullan.

- izolasyon-once: FK RESTRICT + outbox sıralaması ikinci bir kalıcı kilit kaynağı. Bütün çocuk tablolar (household_id, x_id) bileşik FK ile ve ON UPDATE/DELETE RESTRICT bağlı, hiçbiri DEFERRABLE değil. Çevrimdışı kuyruk trip_line'ı trip'ten önce iterse FK ihlali → kalıcı ret → pending_op sonsuz yeniden deneme. Öneri UNIQUE'leri tam bu riske dayanarak reddediyor ama aynı riski FK'lerle geri getiriyor. Ya DEFERRABLE INITIALLY DEFERRED yap, ya bir push partisini tek transaction'da gönder.

- izolasyon-once: Doğrulama bölümü kendi ölümcül koşulunu kontrol etmiyor. Metin 'FORCE row level security kullanılırsa özyineleme geri gelir' diyor ama 7.x sorgularında relforcerowsecurity kontrolü YOK (tek satır: select relname from pg_class where relforcerowsecurity). 7.4 yalnızca information_schema.role_table_grants'e bakıyor — kolon bazlı grant'ler (bu tasarımın belkemiği) ve en tehlikeli yüzey olan anon'un RPC EXECUTE hakkı (role_routine_grants) hiç kontrol edilmiyor. Ayrıca 'alter default privileges' yalnızca onu çalıştıran rolün yarattığı nesneleri etkiler; supabase_admin'in yarattıklarını kapsamaz.

- izolasyon-once: Faz 7 planıyla uyumsuz. ROADMAP.md:248 'Faz 7 — Senkron (7.1 Supabase+RLS · 7.2 Auth · 7.3 Realtime v1 · 7.4 updated_at · 7.5 outbox+tombstone+add-beats-remove)' diyor; yani v1 pull'u Realtime postgres_changes. SQL'de supabase_realtime publication'a ekleme de, RLS'in postgres_changes tarafında çalışması için gereken REPLICA IDENTITY ayarı da yok. Buna karşılık plan dışı bir synced_at imleç mekanizması eklenmiş. Ayrıca updated_at tamamen istemci kontrolünde ve hiçbir yerde 'yeni olan kazanır' kontrolü yok — bayat bir yazma taze veriyi sessizce ezer (F7.4/F7.5 hâlâ açık).


---

Doğrulamalar tamam. Room v5 şemasını, `Conventions.kt` (madde 1–6, **7 yok**), `Sync.kt` (`OpType{INSERT,UPDATE,DELETE}`, `sync_meta.cursor TEXT` hane başına tek), `Bootstrap.kt:10` (derleme sabiti hane id'si), `ListRepository.kt:186-220` (doğal anahtar zaten yerel kimlik), `Daos.kt` (on mutasyon yolu damgasız, `@Delete` madde 3 ihlali), `Member.kt:21` (`email` **hiç kullanılmıyor**) ve iki Supabase doküman maddesini (lint 0028/0029; Postgres Changes'in `public` şemasında kutudan çıktığı gibi çalıştığı) yerinde kontrol ettim.

---

# 1. KARAR ÖZETİ

## 1.1 Üyelik modeli

| Karar | Tek cümle gerekçe |
|---|---|
| Köprü tek alan: `public.member.auth_user_id uuid` | Kimlik zinciri `auth.uid() → member.auth_user_id → member.household_id → satırın household_id'si` tek yardımcıda toplanıyor ve politikaların hepsi tek cümleye iniyor. |
| **`auth.users`'a FK YOK** | `on delete set null` gerçek bir UPDATE'tir, muhafız tetikleyicisini ateşler ve Supabase'te hesap silmeyi "Database error deleting user" ile kırar — FK'yi kaldırmak bu sınıfı bütünüyle yok ediyor, karşılığında kaybedilen bütünlük garantisini zaten yalnızca RPC'ler `auth.uid()`'den yazdığı için hiç kullanmıyorduk. |
| `auth_user_id` **sunucuya ait kolon**: istemci INSERT'te NULL'lanır, UPDATE'te OLD korunur (`t00_identity_guard`) | Aksi halde A hanesindeki bir üye `insert member(user_id=<yabancı uuid>)` ile hane işgal eder ya da `update member set user_id=null` ile eşini kalıcı olarak atardı; muhafız bunu kolon-bazlı grant'e başvurmadan kapatıyor, yani DTO simetrik kalabiliyor. |
| Kısmi tekil indeks: `member(auth_user_id) where auth_user_id is not null and deleted_at is null` | "Bir kullanıcı en fazla bir aktif hanede" kuralını şemaya gömüyor — basitlik-önce'nin en iyi fikri — ve `create_household`/`join_household`'daki 409 kontrolünü kesinleştiriyor. |
| Yardımcı yine `setof text` döndürüyor (skaler değil) | Kural şemada zorlanıyor, imza değişmediği için ileride iki hane gerekirse tek politika bile yeniden yazılmıyor. |
| `member.is_self` sunucuda **yok** | Benim telefonumda "ben" olan satır eşimin telefonunda "ben" değil; senkron edilirse iki cihaz onu birbirinden geri alır, istemci `auth_user_id = kendi uid'im` diye türetir. |
| `member.email` sunucuda **yok** | Kodda tek okuyanı/yazanı yok (`Member.kt:21` dışında hiç geçmiyor) ve modeldeki tek PII — bugün taşınmaması veri asgariliği, gerekirse nullable kolon olarak sonradan eklemek ucuz. |
| `app_settings.sync_photos` sunucuda **yok** | Karar 29 fotoğrafı kayıt anında siliyor; ölü kolonu yeni sisteme taşımak onu sonsuza kadar taşımak demek. |

**Reddedilenler:**
- **Bileşik PK `(household_id, id)` + bileşik FK** (izolasyon-önce): kazancı varlık oracle'ını kapatmak, ama oracle **global tek kolonlu PK üzerinden zaten var** ve UUIDv7 entropisiyle ihmal edilebilir; bedeli gerçek (PostgREST `on_conflict`, iki kolonlu her filtre, Room'un tek kolonlu PK'siyle ayrışma).
- **JWT / `app_metadata` içinde hane iddiası**: haneden çıkarılan kullanıcı token yenilenene kadar erişimini sürdürür.
- **`household`/`member`'da INSERT politikası olmaması** (kazananın özgün hali): Postgres `INSERT ... ON CONFLICT DO UPDATE` için RLS INSERT WITH CHECK'ini çakışma tespitinden **önce** değerlendirir ve politika yoksa sabit `false` koyar — satır zaten varsa bile her upsert 42501 ile ölür, ilk senkron adım 1'de kalıcı takılırdı.
- **Kolon bazlı grant ile `auth_user_id`/`join_code` koruması** (izolasyon-önce): gövdede o kolon varsa 42501, yoksa PGRST204 — iki yönlü tuzak; tetikleyici muhafız gövdeyi sessizce düzeltip her ikisini de imkânsız kılıyor.

## 1.2 RLS ailesi

**Tek cümle, on üç tablo:** `household_id in (select app.current_household_ids())`.

| Karar | Gerekçe |
|---|---|
| `app.current_household_ids()` — SETOF text, STABLE, SECURITY DEFINER, `search_path=''`, PostgREST'e **kapalı** `app` şemasında | Sahibi `postgres` olduğu ve tablo sahibi RLS'i atladığı için `member` politikasının `member`'ı okumasından doğan 42P17 özyinelemesi kırılıyor. |
| **Hiçbir tabloda `force row level security` YOK** | FORCE sahibin muafiyetini kaldırır ve özyinelemeyi geri getirir — bu yüzden şemada yorum olarak da duruyor. |
| Her çağrı `(select ...)` içinde | initPlan üretir; ifade satır başına değil ifade başına bir kez çalışır (lint 0003). |
| `in (select ...)`, JOIN değil | Supabase'in "minimize joins" tavsiyesi: kaynak tabloyu `member`'a bağlamak yerine hedefi bir kümeye indiriyor. |
| Komut başına ayrı politika (SELECT / INSERT / UPDATE), `for all` değil | Katalogda "DELETE politikası yok" **görünür** kalıyor; `for all` bunu bir ihmal gibi gizlerdi. |
| **DELETE politikası hiçbir yerde yok, DELETE grant'i de yok** | Conventions madde 3 gerçek silmeyi yasaklıyor; mezar taşı bir UPDATE'tir, ayrı bir fiil değil — yetki katmanında da kapalı olması kazayla sert silmeyi yapısal olarak imkânsız kılıyor. |
| UPDATE politikaları hem `using` hem `with check` | `using` olmadan yabancı satır düzenlenir, `with check` olmadan satır başka bir haneye taşınır. |
| `household` INSERT politikası `with check (id in (...))` — güvenli | Zaten üyesi olduğun bir haneyi "insert" edebilirsin (upsert'ün UPDATE koluna düşer); gerçekten yeni bir hane için üye olman gerekir, üyelik ise `member.household_id` FK'si yüzünden hanenin var olmasını gerektirir — döngü kapalı, tek giriş `create_household`. |

**Reddedilenler:**
- **RESTRICTIVE + PERMISSIVE dörtlüsü** (izolasyon-önce): `in (select …)` her komutta zaten varken ek izolasyon getirmiyor, politika sayısını ikiye katlıyor ve `multiple_permissive_policies` gürültüsü üretiyor.
- **Tek `for all` politikası** (basitlik-önce): yukarıdaki görünürlük gerekçesi.
- **Enum CHECK kısıtları** (kazanan bunları taşıyordu, değerleri de doğruydu): yine de **silindi** — Room'da karşılığı olmayan her sunucu kısıtı, istemci bir gün yeni bir enum değeri üretirse outbox'ı kalıcı tıkar; bu, kazananın kendi 6. maddesi. Aynı gerekçeyle `household_id_len` CHECK'i de silindi (testlerdeki `m1`, `id-1` gibi kısa id'leri 400'le reddediyordu).
- **Tek istisna: FK'ler duruyor.** Bilinçli bir madde-6 istisnası; bedeli 23503 riski, karşılığı kablo üstünde öksüz satır olmaması. Azaltıcı sözleşme aşağıda (tablo sıralı toplu push + 23503 = yeniden denenebilir).

## 1.3 İsimlendirme

**Postgres'te snake_case; Kotlin'de `JsonNamingStrategy.SnakeCase` — `@SerialName` yok.**

| Karar | Gerekçe |
|---|---|
| snake_case | Tablo adları **zaten** snake_case (`trip_line`, `price_observation`); supabase-kt 3.7.0'ın `PropertyConversionMethod` varsayılanı camelCase KProperty'yi snake_case'e çeviriyor, yani (a) kütüphaneyle aynı yöne akıyor. |
| Tek satırlık global strateji, anotasyon değil | 40 kolonun tamamı saf lowerCamelCase (bitişik büyük harf/kısaltma yok), yani dönüşüm birebir ve tersinir — otomatik stratejinin patladığı hiçbir vaka yok. |
| **Senkron DTO'larında hiçbir alana varsayılan değer verilmez** | Eksik anahtar sessizce `null` olursa `deletedAt` mezar taşı kaybolur ve her çekmede silinen ürünler dirilir; varsayılansız alan `MissingFieldException` fırlatır. |
| `ignoreUnknownKeys = true` | Sunucudaki `server_version`, `server_updated_at`, `updated_by`, `join_code_expires_at` istemcide yok — ileri uyumluluk. |
| `serialDescriptor` sözleşme testi | Çalışma zamanı riskini derleme zamanına en yakın yere (testi kırmaya) taşır. |
| **Room entity'si DTO DEĞİLDİR** | `is_self`, `email`, `sync_photos` sunucuda yok; entity çıplak serileştirilirse PGRST204 döner ve o op outbox'ta sonsuza kadar takılır. |

**Reddedilen:** Room kolonlarını da `@ColumnInfo` ile snake_case'e çevirmek — v6 bump'ı + her elle yazılmış `@Query`'nin yeniden yazımı; Room zaten DAO'ların arkasında, adı yalnızca DTO katmanında dışarı sızıyor.

## 1.4 Katılma kodu akışı

Karar 24: *"üretilmiş ama çalışmayan kod en pahalı hataydı"* → **kod, çalışmadan önce var olmamalı.**

1. **Üretim yasağı.** `t00_code_guard`, INSERT'te `join_code`/`join_code_expires_at`'i NULL'a zorlar, UPDATE'te OLD değeri geri koyar. İstemci ne gönderirse göndersin kod yazamaz; RPC'ler kilidi işlem-yerel bir GUC ile açar. Tetikleyici adı `t00_`, çünkü ad sırası çalışma sırasıdır ve muhafız, LWW'nin "aynı gövde" karşılaştırmasından **önce** geçmelidir.
2. **Hane doğuşu.** `create_household(household_id, name, member_id, display_name)` — SECURITY DEFINER. Hane + `auth.uid()`'e bağlı üye + `app_settings` tek işlemde. **Satırlar `updated_at = 0` ile yazılır**: RPC yalnızca hak iddia eder, gerçek içeriği ilk push taşır ve LWW'de her zaman kazanmalıdır (aksi halde istemcinin yerel ayarları sessizce çöpe giderdi — kazananın gözden kaçırdığı hata).
3. **Kod doğuşu.** `rotate_join_code(household_id, ttl default 60)` — **yalnızca kullanıcı "kodu göster"e bastığında.** 6 karakter, 32 harflik alfabe (0/O/1/I yok, kod sesli okunuyor), `gen_random_uuid()`'nin **0..5. baytları** — v4 UUID'de sürüm nibble'ı 6., varyant 8. bayttadır, yani bu altı bayt tamamen rastgele; `256 % 32 = 0` olduğu için modulo sapması yok. 32⁶ ≈ 1.07e9. Tekil kısmi indeks + sekiz deneme.
4. **Katılma.** `join_household(code, member_id, display_name)` — SECURITY DEFINER olmak **zorunda**, katılan kişi haneyi henüz göremiyor. `for update` ile kilitlenir, kod tüketilir (NULL'lanır). Düzeltilen üç hata: (a) hesap zaten bir haneye bağlıysa **409** — `create_household` ile simetri; (b) daha önce soft-delete edilmiş üye satırı **`(household_id, auth_user_id)` üzerinden bulunup diriltilir**, id'si korunur ve **yanıtta döndürülür** — `on conflict (id) do nothing` hem sessizce hiçbir şey yapıp `ok:true` diyordu hem de farklı p_member_id ile ikinci canlı satır üretip tekil indeksi patlatırdı; (c) başarısız kod **hata fırlatmaz, `{ok:false}` döner** — fırlatan hata işlemi geri alır ve `join_attempt` satırını da götürür, yani kısıtlayıcı hiçbir şeyi sayamazdı.
5. **Üçüncü durum (yeniden kurulum / ikinci cihaz): RPC YOK.** Üyeliği zaten olan kullanıcı `GET /rest/v1/member?auth_user_id=eq.<uid>&select=id,household_id` ile kendini bulur — RLS bunu zaten geçirir. Kazananın da diğerlerinin de yazmadığı üçüncü durum böyle kapanıyor, sıfır yeni yüzeyle.
6. **Katıldıktan sonra** `sync_pull(household_id, 0)` sayfa sayfa çekilir; ayrı bootstrap yolu yok.

## 1.5 Doğal anahtarlı üç tablo — protokolün kararsız bırakılmış en büyük deliği

`on_conflict=trip_id,product_id` + `merge-duplicates` **birincil anahtarı ezer** (PostgREST gövdedeki tüm kolonları `EXCLUDED`'den set eder, `id` dâhil) ve önerilen tespit yolu çalışmaz (`return=representation` ile dönen id her zaman iten cihazın kendi id'sidir). Sonuç iki telefon arasında sonsuz id ping-pongu.

**Karar: `trip_line`, `product_alias`, `suggestion_block` için id, doğal anahtardan TÜRETİLİR** (deterministik, çakışmaya dayanıklı 128 bit, UUIDv8 metni — RFC 9562 v8 keyfi bitlere izin veriyor). İki cihaz çevrimdışıyken aynı satırı üretirse **aynı id'yi** üretir; push her tabloda `on_conflict=id` olur, çakışmayı LWW çözer, Room'un tekil indeksi kendiliğinden sağlanır.

- Bu, `ListRepository.kt:186`'nın zaten yaptığı şeyin şemaya yazılmış hâli: `findIncludingDeleted(tripId, productId)` yerelde doğal anahtarı **zaten kimlik olarak** kullanıyor.
- Bedeli: Conventions madde 1'in "id = UUID v7" cümlesine **açık bir istisna maddesi** eklenmesi. Sessizce delmiyoruz.
- İki kişinin ayrı ayrı "süt" eklemesi tek satırda birleşir, adetler toplanmaz — dedupe'ın kaçınılmaz sonucu, ve doğru sonuç.
- Sunucudaki tekil indeksler **duruyor** (türetme hatası için yüksek sesli emniyet); `trip_line`'ınki `(household_id, trip_id, product_id)` olarak genişletildi — Room'unki `(tripId, productId)` daha dar, yani sunucu Room'un reddettiğini asla üretmez.

**Reddedilen:** push sonrası "sunucunun id'sini benimse" ile yeniden anahtarlama — çalışır (id gövdeden çıkarılırsa `return=representation` gerçekten sunucunun id'sini döner) ama çalışma zamanı protokolü, sıralama tehlikeleri ve outbox'taki `entityId`'nin bozulması demek; deterministik id saf bir fonksiyon.

## 1.6 İmleç ve kapı zili

- **Hane başına monoton dizi** (`public.sync_cursor.last_version`). Saat protokolden tamamen çıkıyor: `now()` işlem **başlangıç** anıdır, uzun bir işlem kendisinden sonra başlayan kısa bir işlemden sonra commit edebilir ve "t'den sonra değişenler" sorgusu o satırı sonsuza kadar atlar — hiçbir hata üretmeyen sessiz veri kaybı. Sayaç artışı ile satır yazımı **aynı işlemde** olduğu için, commit edilmiş sayaç değeri v ise v'ye kadarki bütün satırlar da commit edilmiştir; çekme kilit almadan okuyabilir.
- Room'daki `sync_meta.cursor TEXT` hane başına tek imleç tutuyor — **bire bir uyuyor, şema bump'ı gerekmiyor.**
- İmleç tablosu `app` yerine **`public`'te**: Supabase dokümanı Postgres Changes'in `public` şemasında kutudan çıktığı gibi çalıştığını, özel şema için ayrıca SELECT grant'i gerektiğini söylüyor — ayrıca `public`'te olması istemciye **tek satırlık ucuz bir yoklama** kapısı veriyor (ücretsiz planda proje bir hafta hareketsizlikte duruyor, Realtime'a tek bacakla dayanmak istemiyoruz).
- Yayında **yalnızca** imleç var: satır yükü hiç çıkmıyor, sıra garantisi çekmede zaten var, aynı satır iki yoldan gelip çakışmıyor.

---

# 2. MIGRATION SQL

> **UYGULANMADI.** Aşağıdaki yalnızca metindir. `postgres` rolüyle, tek işlemde çalışacak şekilde yazıldı.

```sql
-- =====================================================================
-- Neydi — Supabase schema + RLS + sync protocol (Phase 7)
-- Postgres 17 / project ref vjinflzmjcsaicaeatic (eu-central-1)
-- Source of truth for shapes: Room v5
--   composeApp/schemas/com.neydi.app.data.db.NeydiDatabase/5.json
--
-- Axioms:
--   A1. All ids are TEXT, generated by the client.
--   A2. All timestamps are BIGINT = UTC epoch millis (Conventions #5).
--   A3. Deletion is soft; DELETE is granted nowhere and has no policy.
--   A4. The server carries NO constraint the client lacks -- with exactly
--       one deliberate exception, the foreign keys (see section 4).
--       A server-only constraint turns a client bug into an outbox that
--       is jammed forever and needs manual repair.
--   A5. Every function pins `set search_path = ''` (lint 0011).
--   A6. RLS helpers live in schema `app`, which is NEVER added to
--       PostgREST "Exposed schemas".
-- =====================================================================

begin;

-- ---------------------------------------------------------------------
-- 0. Schemas
-- ---------------------------------------------------------------------
create schema if not exists app;

revoke all on schema app from public;
grant usage on schema app to authenticated, service_role;

-- Future objects in `public` must not inherit anon grants.
-- NOTE: `alter default privileges` only affects objects created by the
-- role that runs it. It is a safety net, not a guarantee -- the explicit
-- revokes in section 7 are what actually holds today.
alter default privileges in schema public revoke all     on tables    from anon;
alter default privileges in schema public revoke all     on sequences from anon;
alter default privileges in schema public revoke execute on functions from anon, public;

-- =====================================================================
-- 1. Tables
--
-- Shared columns on every synced table:
--   created_at / updated_at  bigint      epoch millis, client clock
--   deleted_at               bigint      tombstone (absent on app_settings,
--                                        mirroring Room)
--   server_version           bigint      per-household monotonic cursor
--   server_updated_at        timestamptz server clock (diagnostics)
--   updated_by               uuid        last writer's auth user
--
-- NOT NULL mirrors Room v5 exactly, column for column.
-- =====================================================================

-- 1.0 The cursor. Lives in `public` on purpose: Realtime Postgres Changes
--     works out of the box for public-schema tables, and the client can
--     poll this single row as a doorbell when Realtime is unavailable
--     (the free plan pauses a project after a week of inactivity).
--     Readable by members, writable by NOBODY except the trigger.
create table public.sync_cursor (
  household_id text        primary key,
  last_version bigint      not null default 0,
  updated_at   timestamptz not null default now()
);

-- 1.1 household
--     join_code / join_code_expires_at are SERVER-OWNED (decision 24).
--     Room has joinCode but not joinCodeExpiresAt; an extra server column
--     is not a constraint, so A4 holds. The client never writes either:
--     t00_code_guard strips them.
create table public.household (
  id                   text        primary key,
  name                 text        not null,
  join_code            text,
  join_code_expires_at timestamptz,
  created_at           bigint      not null,
  updated_at           bigint,
  deleted_at           bigint,
  server_version       bigint      not null default 0,
  server_updated_at    timestamptz not null default now(),
  updated_by           uuid
);

-- 1.2 member -- the only bridge between auth.uid() and a household.
--
--     NO foreign key to auth.users. `on delete set null` is a real UPDATE:
--     it fires the identity guard, the guard restores the old value, and
--     deleting an auth user then fails with "Database error deleting
--     user". Dropping the FK removes that entire class. Nothing is lost:
--     auth_user_id is only ever written from auth.uid() inside two RPCs.
--
--     is_self is deliberately absent (device-local concept).
--     email is deliberately absent (unused in the client, and the only
--     PII in the model).
create table public.member (
  id                text        primary key,
  household_id      text        not null references public.household (id),
  display_name      text        not null,
  auth_user_id      uuid,
  created_at        bigint      not null,
  updated_at        bigint,
  deleted_at        bigint,
  server_version    bigint      not null default 0,
  server_updated_at timestamptz not null default now(),
  updated_by        uuid
);

-- 1.3 store
create table public.store (
  id                text        primary key,
  household_id      text        not null references public.household (id),
  name              text        not null,
  chain             text        not null,   -- NOT NULL: Room says so
  created_at        bigint      not null,
  updated_at        bigint,
  deleted_at        bigint,
  server_version    bigint      not null default 0,
  server_updated_at timestamptz not null default now(),
  updated_by        uuid
);

-- 1.4 product
--     category_id / seed_id point at reference data that ships with the
--     app and is never synced -> no table here, therefore no FK.
create table public.product (
  id                text        primary key,
  household_id      text        not null references public.household (id),
  name              text        not null,
  match_key         text        not null,
  category_id       text        not null,   -- NOT NULL: Room says so
  seed_id           text,
  default_unit      text        not null,
  is_staple         boolean     not null default false,
  created_at        bigint      not null,
  updated_at        bigint,
  deleted_at        bigint,
  server_version    bigint      not null default 0,
  server_updated_at timestamptz not null default now(),
  updated_by        uuid
);

-- 1.5 product_alias -- natural key table, deterministic id (section 12)
create table public.product_alias (
  id                  text        primary key,
  household_id        text        not null references public.household (id),
  store_chain         text        not null,
  raw_text_normalized text        not null,
  product_id          text        not null references public.product (id),
  confirmed_at        bigint,
  created_at          bigint      not null,
  updated_at          bigint,
  deleted_at          bigint,
  server_version      bigint      not null default 0,
  server_updated_at   timestamptz not null default now(),
  updated_by          uuid
);

-- 1.6 trip
--     No CHECK on status. The allowed values are
--     PLANNING / SHOPPING / CLOSED (Shopping.kt TripStatus) but Room
--     carries no such constraint, and a server-only one would jam the
--     outbox forever the day a client learns a fourth value (A4).
create table public.trip (
  id                text        primary key,
  household_id      text        not null references public.household (id),
  store_id          text        references public.store (id),
  started_at        bigint      not null,
  status            text        not null default 'PLANNING',
  owner_member_id   text        references public.member (id),
  completed_at      bigint,
  created_at        bigint      not null,
  updated_at        bigint,
  deleted_at        bigint,
  server_version    bigint      not null default 0,
  server_updated_at timestamptz not null default now(),
  updated_by        uuid
);

-- 1.7 trip_line -- natural key table, deterministic id (section 12)
--     quantity: SQLite REAL is 8-byte IEEE -> double precision.
--     take_outcome allowed values: TAKEN / NOT_NEEDED / FORGOTTEN. No CHECK (A4).
create table public.trip_line (
  id                 text             primary key,
  household_id       text             not null references public.household (id),
  trip_id            text             not null references public.trip (id),
  product_id         text             not null references public.product (id),
  quantity           double precision not null,
  unit               text             not null,
  checked            boolean          not null default false,
  checked_at         bigint,
  added_by_member_id text             not null references public.member (id),
  from_suggestion    boolean          not null default false,
  note               text,
  take_outcome       text,
  created_at         bigint           not null,
  updated_at         bigint,
  deleted_at         bigint,
  server_version     bigint           not null default 0,
  server_updated_at  timestamptz      not null default now(),
  updated_by         uuid
);

-- 1.8 price_observation
--     unit_price_minor: kurus, bigint. Never floating point (Conventions #4).
--     No uniqueness guard here: Room has none either, the F5.10 duplicate
--     guard is client-side (A4).
create table public.price_observation (
  id                text             primary key,
  household_id      text             not null references public.household (id),
  product_id        text             not null references public.product (id),
  store_id          text             references public.store (id),
  unit_price_minor  bigint           not null,
  pack_size         double precision,
  pack_unit         text,
  price_unit        text,
  brand             text,
  observed_at       bigint           not null,
  created_at        bigint           not null,
  updated_at        bigint,
  deleted_at        bigint,
  server_version    bigint           not null default 0,
  server_updated_at timestamptz      not null default now(),
  updated_by        uuid
);

-- 1.9 suggestion_event
--     outcome allowed values: SHOWN / ADDED / REJECTED / IGNORED. No CHECK (A4).
create table public.suggestion_event (
  id                text        primary key,
  household_id      text        not null references public.household (id),
  product_id        text        not null references public.product (id),
  trip_id           text        references public.trip (id),
  suggested_at      bigint      not null,
  reason            text        not null,
  outcome           text        not null,
  responded_at      bigint,
  created_at        bigint      not null,
  updated_at        bigint,
  deleted_at        bigint,
  server_version    bigint      not null default 0,
  server_updated_at timestamptz not null default now(),
  updated_by        uuid
);

-- 1.10 suggestion_block -- natural key table, deterministic id (section 12)
--      source allowed values: AUTO / MANUAL. No CHECK (A4).
create table public.suggestion_block (
  id                text        primary key,
  household_id      text        not null references public.household (id),
  product_id        text        not null references public.product (id),
  source            text        not null,
  blocked_at        bigint      not null,
  unblocked_at      bigint,
  created_at        bigint      not null,
  updated_at        bigint,
  deleted_at        bigint,
  server_version    bigint      not null default 0,
  server_updated_at timestamptz not null default now(),
  updated_by        uuid
);

-- 1.11 app_settings -- household_id is both PK and tenant key.
--      No deleted_at (Room has none). No sync_photos (decision 29).
create table public.app_settings (
  household_id       text        primary key references public.household (id),
  setup_completed_at bigint,
  tempo_days         integer,
  created_at         bigint      not null,
  updated_at         bigint,
  server_version     bigint      not null default 0,
  server_updated_at  timestamptz not null default now(),
  updated_by         uuid
);

-- Local-only Room tables are absent on purpose and will stay absent:
--   category, catalog_seed -> reference data shipped with the app
--   product_stats          -> derived, recomputable
--   pending_op, sync_meta   -> the sync engine's own ledger

-- 1.12 Internal bookkeeping. Both live in `app`, which PostgREST does not
--      expose, and neither is granted to any client role -- read them from
--      the SQL editor. RLS is enabled anyway (defence in depth); with no
--      policy and no grant, nobody but the owner can touch them.
create table app.sync_conflict (
  id                bigserial   primary key,
  household_id      text        not null,
  table_name        text        not null,
  row_id            text        not null,
  loser_updated_at  bigint,
  winner_updated_at bigint,
  loser_uid         uuid,
  at                timestamptz not null default now()
);
create index sync_conflict_household_at_idx
  on app.sync_conflict (household_id, at desc);

create table app.join_attempt (
  auth_user_id uuid        not null,
  at           timestamptz not null default now(),
  ok           boolean     not null
);
create index join_attempt_user_at_idx on app.join_attempt (auth_user_id, at desc);

-- =====================================================================
-- 2. Functions
-- =====================================================================

-- 2.1 The heart of RLS.
--
--     SECURITY DEFINER, owned by postgres. Table owners are exempt from
--     RLS, so reading public.member here never re-enters member's own
--     policy -- that is what breaks the 42P17 recursion.
--     >>> Therefore NO table may ever get `force row level security`. <<<
--     FORCE removes the owner exemption and brings the recursion back.
create or replace function app.current_household_ids()
returns setof text
language sql
stable
security definer
set search_path = ''
as $$
  select m.household_id
    from public.member m
   where m.auth_user_id = (select auth.uid())
     and m.deleted_at is null;
$$;

revoke all on function app.current_household_ids() from public, anon;
grant execute on function app.current_household_ids() to authenticated;

-- 2.2 Raise an error that PostgREST turns into a real HTTP status.
create or replace function app.http_error(
  p_status int, p_code text, p_message text, p_hint text default null
) returns void
language plpgsql
set search_path = ''
as $$
begin
  raise sqlstate 'PGRST' using
    message = json_build_object(
      'code', p_code, 'message', p_message, 'hint', p_hint)::text,
    detail  = json_build_object(
      'status', p_status, 'headers', json_build_object())::text;
end;
$$;

revoke all on function app.http_error(int, text, text, text) from public, anon;
grant execute on function app.http_error(int, text, text, text) to authenticated;

-- 2.3 Join code generator.
--
--     32-symbol alphabet without 0/O/1/I -- the user reads this code out
--     loud. Bytes 0..5 of a v4 UUID are fully random: the version nibble
--     sits in byte 6 and the variant bits in byte 8, neither of which we
--     touch. 256 % 32 = 0, so the modulo is unbiased. 32^6 ~ 1.07e9.
--     Using gen_random_uuid() rather than pgcrypto's gen_random_bytes()
--     avoids depending on an extension being installed in a given schema.
create or replace function app.new_join_code()
returns text
language sql
volatile
set search_path = ''
as $$
  with b as (
    select decode(replace(gen_random_uuid()::text, '-', ''), 'hex') as raw
  )
  select string_agg(
           substr('ABCDEFGHJKLMNPQRSTUVWXYZ23456789',
                  1 + (get_byte(b.raw, g.i) % 32), 1), '' order by g.i)
    from b, generate_series(0, 5) as g(i);
$$;

revoke all on function app.new_join_code() from public, anon, authenticated;

-- 2.4 Guard: join_code is server-owned (decision 24).
--     Runs as t00_, i.e. BEFORE the sync guard, so that the "same body"
--     comparison in 2.6 sees the corrected value.
create or replace function app.household_code_guard()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if coalesce(current_setting('neydi.privileged_write', true), '') = 'on' then
    return new;
  end if;
  if tg_op = 'INSERT' then
    new.join_code            := null;
    new.join_code_expires_at := null;
  else
    new.join_code            := old.join_code;
    new.join_code_expires_at := old.join_code_expires_at;
  end if;
  return new;
end;
$$;

revoke all on function app.household_code_guard() from public, anon, authenticated;

-- 2.5 Guard: member.auth_user_id is server-owned.
--
--     Without this, a member of household A could insert a row carrying
--     a stranger's uuid (dragging them into A and cutting them off from
--     their own household), or null out a spouse's uuid and evict them
--     permanently. The client may send the column -- it is simply
--     ignored, which keeps the DTO symmetric and avoids both the
--     column-grant 42501 and the PGRST204 traps.
create or replace function app.member_identity_guard()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if coalesce(current_setting('neydi.privileged_write', true), '') = 'on' then
    return new;
  end if;
  if tg_op = 'INSERT' then
    new.auth_user_id := null;
  else
    new.auth_user_id := old.auth_user_id;
  end if;
  return new;
end;
$$;

revoke all on function app.member_identity_guard() from public, anon, authenticated;

-- 2.6 The single sync guard: clock hygiene + LWW + add-beats-remove +
--     cursor allocation.
--
--     Why a trigger and not a column or an app layer: a column cannot
--     express a DECISION ("reject this write"), and there is no app layer
--     -- the client upserts straight into PostgREST. Columns still carry
--     state (deleted_at carries the tombstone, server_version carries the
--     order); the trigger enforces the rule.
create or replace function app.sync_row_guard()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_now_ms   bigint := (extract(epoch from clock_timestamp()) * 1000)::bigint;
  v_new      jsonb;
  v_old      jsonb;
  v_house    text;
  v_new_at   bigint;
  v_old_at   bigint;
  v_new_del  bigint;
  v_old_del  bigint;
  v_version  bigint;
  v_suppress boolean := coalesce(
                current_setting('neydi.suppress_resurrect', true) = 'on', false);
  v_server_keys constant text[] :=
    array['server_version', 'server_updated_at', 'updated_by'];
begin
  ------------------------------------------------------------------
  -- (a) Clock hygiene.
  --     updated_at is nullable in Room, and LWW cannot run on a nullable
  --     comparator, so it is filled here. A device more than five minutes
  --     into the future is pulled back to server time: otherwise a single
  --     phone that thinks it is 2027 declares every correction its
  --     partner ever makes stale, forever.
  ------------------------------------------------------------------
  new.updated_at := coalesce(new.updated_at, new.created_at);
  if new.updated_at > v_now_ms + 300000 then
    new.updated_at := v_now_ms;
  end if;

  v_new  := to_jsonb(new);
  v_house := v_new ->> tg_argv[0];

  if v_house is null then
    raise exception 'sync_row_guard: %.% has a null tenant key (%)',
      tg_table_schema, tg_table_name, tg_argv[0];
  end if;

  if tg_op = 'UPDATE' then
    v_old     := to_jsonb(old);
    v_new_at  := (v_new ->> 'updated_at')::bigint;
    v_old_at  := coalesce((v_old ->> 'updated_at')::bigint,
                          (v_old ->> 'created_at')::bigint);
    v_new_del := (v_new ->> 'deleted_at')::bigint;   -- null if key absent
    v_old_del := (v_old ->> 'deleted_at')::bigint;

    ----------------------------------------------------------------
    -- (b) ADD BEATS REMOVE.
    --     Tombstone -> alive wins regardless of stamps, and the stamp is
    --     pushed one past the tombstone; otherwise the other device
    --     re-pushes its (newer) tombstone and the row ping-pongs.
    --     This assumes pending_op carries INTENT: every queued row is an
    --     edit the user just made, not a replay of an old snapshot. Bulk
    --     restores turn it off with
    --     `set local neydi.suppress_resurrect = 'on'`.
    ----------------------------------------------------------------
    if v_old_del is not null and v_new_del is null and not v_suppress then
      if v_new_at <= v_old_at then
        new.updated_at := v_old_at + 1;
      end if;

    ----------------------------------------------------------------
    -- (c) LWW: a stale write loses and touches nothing.
    --     `return null` from a BEFORE trigger skips this row: no new
    --     tuple, no version burned. With return=representation the client
    --     gets no row back and reads that as "the server has something
    --     newer" -- a pull signal, not an error.
    ----------------------------------------------------------------
    elsif v_new_at < v_old_at then
      insert into app.sync_conflict (
        household_id, table_name, row_id,
        loser_updated_at, winner_updated_at, loser_uid)
      values (
        v_house, tg_table_name, coalesce(v_new ->> 'id', v_house),
        v_new_at, v_old_at, (select auth.uid()));
      return null;

    ----------------------------------------------------------------
    -- (d) Same stamp + same body = the same op delivered twice.
    --     Swallow it. This turns at-least-once delivery into
    --     exactly-once effect without a separate op-id ledger.
    ----------------------------------------------------------------
    elsif v_new_at = v_old_at
      and (v_new - v_server_keys) = (v_old - v_server_keys) then
      return null;
    end if;
    -- Remaining cases: incoming is newer, or stamps tie with different
    -- bodies. Incoming wins; on a tie arrival order decides, and since
    -- the server is the only referee both devices converge on it.
  end if;

  ------------------------------------------------------------------
  -- (e) Cursor allocation. The UPDATE holds the row lock until commit,
  --     so writes within one household serialise and version order
  --     equals commit order. No gaps, no clocks.
  ------------------------------------------------------------------
  insert into public.sync_cursor (household_id)
  values (v_house)
  on conflict (household_id) do nothing;

  update public.sync_cursor
     set last_version = last_version + 1,
         updated_at   = clock_timestamp()
   where household_id = v_house
  returning last_version into v_version;

  new.server_version    := v_version;
  new.server_updated_at := clock_timestamp();
  new.updated_by        := (select auth.uid());

  return new;
end;
$$;

revoke all on function app.sync_row_guard() from public, anon, authenticated;

-- =====================================================================
-- 3. Triggers. Name order IS execution order: the t00_ guards must run
--    before t10_sync so that the "same body" comparison in 2.6(d) sees
--    already-corrected server-owned columns.
-- =====================================================================
create trigger t00_code_guard     before insert or update on public.household
  for each row execute function app.household_code_guard();
create trigger t00_identity_guard before insert or update on public.member
  for each row execute function app.member_identity_guard();

create trigger t10_sync before insert or update on public.household
  for each row execute function app.sync_row_guard('id');
create trigger t10_sync before insert or update on public.member
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.store
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.product
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.product_alias
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.trip
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.trip_line
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.price_observation
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.suggestion_event
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.suggestion_block
  for each row execute function app.sync_row_guard('household_id');
create trigger t10_sync before insert or update on public.app_settings
  for each row execute function app.sync_row_guard('household_id');

-- =====================================================================
-- 4. Indexes
-- =====================================================================

-- (a) Pull index. (household_id, server_version) is sync_pull's ONLY
--     access pattern.
create index member_pull_idx            on public.member            (household_id, server_version);
create index store_pull_idx             on public.store             (household_id, server_version);
create index product_pull_idx           on public.product           (household_id, server_version);
create index product_alias_pull_idx     on public.product_alias     (household_id, server_version);
create index trip_pull_idx              on public.trip              (household_id, server_version);
create index trip_line_pull_idx         on public.trip_line         (household_id, server_version);
create index price_observation_pull_idx on public.price_observation (household_id, server_version);
create index suggestion_event_pull_idx  on public.suggestion_event  (household_id, server_version);
create index suggestion_block_pull_idx  on public.suggestion_block  (household_id, server_version);
create index household_pull_idx         on public.household         (id, server_version);
create index app_settings_pull_idx      on public.app_settings      (household_id, server_version);

-- (b) The hottest index in the system: every policy evaluation reads it.
--     UNIQUE and partial, which also encodes "one auth user is active in
--     at most one household" -- the rule that keeps the id set a
--     singleton and makes the 409 checks in the RPCs exact.
create unique index member_auth_user_uk on public.member (auth_user_id)
  where auth_user_id is not null and deleted_at is null;

-- (c) Join code lookup must be unambiguous.
create unique index household_join_code_uk on public.household (join_code)
  where join_code is not null;

-- (d) Room mirrors. Neither missing (convergence breaks) nor extra
--     (outbox jams). trip_line's is WIDENED with household_id: Room's
--     (tripId, productId) is the stricter of the two, so the server can
--     never reject something Room accepts.
create index        product_match_idx          on public.product           (household_id, match_key);
create unique index product_alias_natural_uk   on public.product_alias     (household_id, store_chain, raw_text_normalized);
create index        trip_completed_idx         on public.trip              (household_id, completed_at);
create unique index trip_line_natural_uk       on public.trip_line         (household_id, trip_id, product_id);
create index        price_observation_prod_idx on public.price_observation (product_id, observed_at);
create unique index suggestion_block_natural_uk on public.suggestion_block (household_id, product_id);

-- Deliberately NOT created: covering indexes for every foreign key.
-- They matter for deletes and parent-key updates; we never delete and
-- never re-key. The `unindexed_foreign_keys` advisor will report them --
-- see the verification notes.

-- =====================================================================
-- 5. RLS
--
--    One policy family, thirteen tables, one sentence:
--    "is this row's household in the set of households I belong to?"
--    The set comes from app.current_household_ids(), the call is wrapped
--    in (select ...) so it becomes an initPlan (evaluated once per
--    statement, not once per row), and it uses `in` rather than a join.
-- =====================================================================
alter table public.sync_cursor       enable row level security;
alter table public.household         enable row level security;
alter table public.member            enable row level security;
alter table public.store             enable row level security;
alter table public.product           enable row level security;
alter table public.product_alias     enable row level security;
alter table public.trip              enable row level security;
alter table public.trip_line         enable row level security;
alter table public.price_observation enable row level security;
alter table public.suggestion_event  enable row level security;
alter table public.suggestion_block  enable row level security;
alter table public.app_settings      enable row level security;
alter table app.sync_conflict        enable row level security;  -- no policy, no grant
alter table app.join_attempt         enable row level security;  -- no policy, no grant

-- household: INSERT is allowed and is NOT a hole. You can only "insert"
-- a household you already belong to, which is exactly the ON CONFLICT
-- path a PostgREST upsert needs. A genuinely new household would require
-- membership first, and membership requires the household row to exist
-- (member.household_id FK) -- so the only door remains create_household().
-- Without this policy Postgres evaluates a constant-false INSERT
-- WITH CHECK before ON CONFLICT arbitration and every upsert dies 42501.
create policy household_select on public.household
  for select to authenticated
  using (id in (select app.current_household_ids()));

create policy household_insert on public.household
  for insert to authenticated
  with check (id in (select app.current_household_ids()));

create policy household_update on public.household
  for update to authenticated
  using      (id in (select app.current_household_ids()))
  with check (id in (select app.current_household_ids()));

-- The remaining ten tables: identical select / insert / update.
-- NO `for delete` policy anywhere -- real deletion does not exist.
do $policies$
declare
  t   text;
  col text;
begin
  foreach t in array array[
    'member', 'store', 'product', 'product_alias', 'trip', 'trip_line',
    'price_observation', 'suggestion_event', 'suggestion_block',
    'app_settings'
  ] loop
    col := 'household_id';

    execute format(
      'create policy %I on public.%I for select to authenticated
         using (%I in (select app.current_household_ids()))',
      t || '_select', t, col);

    execute format(
      'create policy %I on public.%I for insert to authenticated
         with check (%I in (select app.current_household_ids()))',
      t || '_insert', t, col);

    execute format(
      'create policy %I on public.%I for update to authenticated
         using      (%I in (select app.current_household_ids()))
         with check (%I in (select app.current_household_ids()))',
      t || '_update', t, col, col);
  end loop;
end;
$policies$;

-- The cursor is readable (the Realtime doorbell and the cheap poll both
-- ride on it) and writable by nobody: only the trigger, which is
-- security definer and therefore owner, ever touches it.
create policy sync_cursor_select on public.sync_cursor
  for select to authenticated
  using (household_id in (select app.current_household_ids()));

-- =====================================================================
-- 6. Grants. anon touches NOTHING.
-- =====================================================================
revoke all on all tables in schema public from anon, authenticated;

grant select, insert, update on
  public.household, public.member, public.store, public.product,
  public.product_alias, public.trip, public.trip_line,
  public.price_observation, public.suggestion_event,
  public.suggestion_block, public.app_settings
  to authenticated;

grant select on public.sync_cursor to authenticated;   -- select ONLY

grant all privileges on all tables in schema public to service_role;

-- =====================================================================
-- 7. PULL
--
--    The server reads the cursor WITHOUT locking (see 2.6(e) for why that
--    is safe). If a table hits the page limit the cursor is pulled back
--    to that table's high water mark and `complete=false` is returned;
--    the client loops. Rows above the mark are still shipped -- applying
--    them twice is harmless because every apply is an idempotent upsert.
--
--    SECURITY INVOKER: every SELECT inside still goes through RLS. The
--    membership check at the top is a fast reject, not the security
--    boundary -- if this function had a bug, RLS would still hold.
--    VOLATILE, so PostgREST exposes it as POST.
-- =====================================================================
create or replace function public.sync_pull(
  p_household_id text,
  p_since        bigint default 0,
  p_limit        int    default 500
) returns jsonb
language plpgsql
security invoker
set search_path = ''
as $$
declare
  v_tables constant text[] := array[
    'household', 'member', 'store', 'product', 'product_alias',
    'trip', 'trip_line', 'price_observation',
    'suggestion_event', 'suggestion_block', 'app_settings'];
  v_t    text;
  v_key  text;
  v_upto bigint;
  v_next bigint;
  v_rows jsonb;
  v_cnt  int;
  v_max  bigint;
  v_out  jsonb := '{}'::jsonb;
begin
  if (select auth.uid()) is null then
    perform app.http_error(401, 'NEYDI_ANON', 'Oturum yok.');
  end if;

  if p_household_id is null
     or p_household_id not in (select app.current_household_ids()) then
    perform app.http_error(403, 'NEYDI_NOT_MEMBER',
      'Bu hanenin uyesi degilsiniz.');
  end if;

  if p_limit is null or p_limit < 1 or p_limit > 2000 then
    p_limit := 500;
  end if;
  p_since := coalesce(p_since, 0);

  select c.last_version into v_upto
    from public.sync_cursor c
   where c.household_id = p_household_id;
  v_upto := coalesce(v_upto, 0);
  v_next := v_upto;

  foreach v_t in array v_tables loop
    v_key := case when v_t = 'household' then 'id' else 'household_id' end;

    execute format(
      'select coalesce(jsonb_agg(t order by t.server_version), ''[]''::jsonb),
              count(*)::int,
              coalesce(max(t.server_version), 0)
         from (select * from public.%I
                where %I = $1
                  and server_version >  $2
                  and server_version <= $3
                order by server_version
                limit $4) t',
      v_t, v_key)
    into v_rows, v_cnt, v_max
    using p_household_id, p_since, v_upto, p_limit;

    v_out := v_out || jsonb_build_object(v_t, v_rows);

    if v_cnt >= p_limit and v_max < v_next then
      v_next := v_max;
    end if;
  end loop;

  return jsonb_build_object(
    'cursor',    v_next,
    'upto',      v_upto,
    'complete',  (v_next = v_upto),
    'server_ms', (extract(epoch from clock_timestamp()) * 1000)::bigint,
    'tables',    v_out);
end;
$$;

revoke all on function public.sync_pull(text, bigint, int) from public, anon;
grant execute on function public.sync_pull(text, bigint, int) to authenticated;

-- =====================================================================
-- 8. Household birth, invite, join (decision 24)
-- =====================================================================

-- 8.1 Household birth.
--
--     updated_at is written as 0 on purpose: this RPC only stakes the
--     claim, the first push carries the real content and must always win
--     LWW. Seeding with server `now()` would make the client's own
--     settings row look stale and discard it silently.
create or replace function public.create_household(
  p_household_id text,
  p_name         text,
  p_member_id    text,
  p_display_name text
) returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_me  uuid   := (select auth.uid());
  v_now bigint := (extract(epoch from clock_timestamp()) * 1000)::bigint;
begin
  if v_me is null then
    perform app.http_error(401, 'NEYDI_ANON', 'Oturum yok.');
  end if;
  if p_household_id is null or btrim(p_household_id) = ''
     or p_member_id is null or btrim(p_member_id) = ''
     or p_name is null or btrim(p_name) = ''
     or p_display_name is null or btrim(p_display_name) = '' then
    perform app.http_error(400, 'NEYDI_BAD_INPUT', 'Eksik alan.');
  end if;

  if exists (select 1 from public.member m
              where m.auth_user_id = v_me and m.deleted_at is null) then
    perform app.http_error(409, 'NEYDI_HAS_HOUSEHOLD',
      'Bu hesap zaten bir haneye bagli.');
  end if;

  perform set_config('neydi.privileged_write', 'on', true);

  begin
    insert into public.household (id, name, created_at, updated_at)
    values (p_household_id, p_name, v_now, 0);
  exception when unique_violation then
    -- Never leak whether that id exists: generic 409.
    perform app.http_error(409, 'NEYDI_HOUSEHOLD_TAKEN',
      'Hane olusturulamadi.');
  end;

  insert into public.member (id, household_id, display_name,
                             auth_user_id, created_at, updated_at)
  values (p_member_id, p_household_id, p_display_name, v_me, v_now, 0);

  insert into public.app_settings (household_id, created_at, updated_at)
  values (p_household_id, v_now, 0);

  return jsonb_build_object('ok', true,
                            'household_id', p_household_id,
                            'member_id',    p_member_id);
end;
$$;

revoke all on function public.create_household(text, text, text, text) from public, anon;
grant execute on function public.create_household(text, text, text, text) to authenticated;

-- 8.2 The code is born ONLY when the user taps "show code" -- that is the
--     whole of decision 24. Single use, default 60 minutes: this is a
--     handshake, not a password.
create or replace function public.rotate_join_code(
  p_household_id text,
  p_ttl_minutes  int default 60
) returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_code text;
  v_exp  timestamptz;
  v_try  int := 0;
begin
  if (select auth.uid()) is null then
    perform app.http_error(401, 'NEYDI_ANON', 'Oturum yok.');
  end if;
  if p_household_id is null
     or p_household_id not in (select app.current_household_ids()) then
    perform app.http_error(403, 'NEYDI_NOT_MEMBER',
      'Bu hanenin uyesi degilsiniz.');
  end if;

  p_ttl_minutes := least(greatest(coalesce(p_ttl_minutes, 60), 5), 1440);
  v_exp := clock_timestamp() + make_interval(mins => p_ttl_minutes);

  perform set_config('neydi.privileged_write', 'on', true);
  loop
    v_try  := v_try + 1;
    v_code := app.new_join_code();
    begin
      update public.household
         set join_code            = v_code,
             join_code_expires_at = v_exp,
             updated_at = (extract(epoch from clock_timestamp()) * 1000)::bigint
       where id = p_household_id;
      exit;
    exception when unique_violation then
      if v_try >= 8 then raise; end if;
    end;
  end loop;

  return jsonb_build_object('ok', true, 'join_code', v_code, 'expires_at', v_exp);
end;
$$;

revoke all on function public.rotate_join_code(text, int) from public, anon;
grant execute on function public.rotate_join_code(text, int) to authenticated;

-- 8.3 Join.
--
--     SECURITY DEFINER is mandatory: the joiner cannot see the household
--     yet, RLS blocks it. This is the only door.
--
--     A bad code does NOT raise, it returns {ok:false}. A raise rolls the
--     transaction back and takes the app.join_attempt row with it, so the
--     limiter could never count anything; it needs the transaction to
--     commit.
--
--     Resurrection is keyed on (household_id, auth_user_id), NOT on the
--     supplied member id: a previously soft-deleted member row keeps its
--     own id, and a second live row for the same uid would violate
--     member_auth_user_uk. The surviving id is returned and the client
--     MUST adopt it.
create or replace function public.join_household(
  p_code         text,
  p_member_id    text,
  p_display_name text
) returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_me        uuid   := (select auth.uid());
  v_id        text;
  v_name      text;
  v_now       bigint := (extract(epoch from clock_timestamp()) * 1000)::bigint;
  v_fail      int;
  v_member_id text;
begin
  if v_me is null then
    perform app.http_error(401, 'NEYDI_ANON', 'Oturum yok.');
  end if;

  -- Symmetric with create_household: one active household per account.
  if exists (select 1 from public.member m
              where m.auth_user_id = v_me and m.deleted_at is null) then
    perform app.http_error(409, 'NEYDI_HAS_HOUSEHOLD',
      'Bu hesap zaten bir haneye bagli.');
  end if;

  select count(*) into v_fail
    from app.join_attempt a
   where a.auth_user_id = v_me
     and a.ok = false
     and a.at > clock_timestamp() - interval '10 minutes';

  if v_fail >= 10 then
    return jsonb_build_object('ok', false, 'error', 'NEYDI_TOO_MANY');
  end if;

  select h.id, h.name into v_id, v_name
    from public.household h
   where h.join_code = upper(btrim(coalesce(p_code, '')))
     and h.join_code_expires_at > clock_timestamp()
     and h.deleted_at is null
   for update;

  if not found then
    insert into app.join_attempt (auth_user_id, ok) values (v_me, false);
    return jsonb_build_object('ok', false, 'error', 'NEYDI_BAD_CODE');
  end if;

  if (select count(*) from public.member m
       where m.household_id = v_id and m.deleted_at is null) >= 6 then
    insert into app.join_attempt (auth_user_id, ok) values (v_me, false);
    return jsonb_build_object('ok', false, 'error', 'NEYDI_HOUSEHOLD_FULL');
  end if;

  perform set_config('neydi.privileged_write', 'on', true);

  -- Rejoining after leaving: revive the old row, keep its id.
  select m.id into v_member_id
    from public.member m
   where m.household_id = v_id and m.auth_user_id = v_me
   limit 1;

  if v_member_id is not null then
    update public.member
       set deleted_at   = null,
           display_name = coalesce(nullif(btrim(p_display_name), ''), display_name),
           updated_at   = v_now
     where id = v_member_id;
  else
    v_member_id := p_member_id;
    insert into public.member (id, household_id, display_name,
                               auth_user_id, created_at, updated_at)
    values (v_member_id, v_id,
            coalesce(nullif(btrim(p_display_name), ''), 'Uye'),
            v_me, v_now, 0);
  end if;

  -- Consume the code.
  update public.household
     set join_code = null, join_code_expires_at = null, updated_at = v_now
   where id = v_id;

  insert into app.join_attempt (auth_user_id, ok) values (v_me, true);

  return jsonb_build_object('ok', true,
                            'household_id', v_id,
                            'name',         v_name,
                            'member_id',    v_member_id);
end;
$$;

revoke all on function public.join_household(text, text, text) from public, anon;
grant execute on function public.join_household(text, text, text) to authenticated;

-- 8.4 The third state -- reinstall / second device -- needs NO RPC.
--     An existing member is already visible to themselves through RLS:
--       GET /rest/v1/member?auth_user_id=eq.<uid>&select=id,household_id
--     Then sync_pull(household_id, 0).

-- =====================================================================
-- 9. REALTIME -- a doorbell, not a data bus.
--
--    Only the cursor table is published. A write drops
--    {household_id, last_version} on the client, which compares it with
--    its own cursor and calls sync_pull if needed. Publishing eleven
--    tables instead would ship row payloads Realtime does not need,
--    duplicate ordering guarantees pull already provides, and let the
--    same row arrive by two paths and race.
--
--    REPLICA IDENTITY stays default: we never need the `old` record.
--    The same table is also a plain poll target (one row, one bigint),
--    which is the fallback when Realtime is unavailable.
-- =====================================================================
do $realtime$
begin
  if exists (select 1 from pg_publication where pubname = 'supabase_realtime') then
    execute 'alter publication supabase_realtime add table public.sync_cursor';
  end if;
end;
$realtime$;

commit;

notify pgrst, 'reload schema';

-- =====================================================================
-- 10. CLIENT CONTRACT (not SQL -- the rest of the protocol)
--
-- PULL
--   POST /rest/v1/rpc/sync_pull
--        {"p_household_id": "...", "p_since": <sync_meta.cursor>, "p_limit": 500}
--   Response: {cursor, upto, complete, server_ms, tables:{...}}
--   Apply every row as an idempotent upsert, then write
--   sync_meta.cursor = cursor. Loop while complete=false.
--   sync_meta.lastPulledAt = server_ms (diagnostics only).
--
-- PUSH -- plain PostgREST upsert, ALWAYS on_conflict=id, no exceptions
--   POST /rest/v1/<table>?on_conflict=id
--   Prefer: resolution=merge-duplicates, return=representation
--   Never send server_version / server_updated_at / updated_by.
--   A row that does not come back LOST LWW; that is a pull signal, not
--   an error. Match the response to the request BY ID, never by index --
--   skipped rows are simply absent.
--
-- PUSH ORDER (the FKs are the one deliberate exception to A4)
--   One bulk request per table, tables in dependency order:
--     household -> member -> store -> product -> product_alias -> trip ->
--     trip_line -> price_observation -> suggestion_event ->
--     suggestion_block -> app_settings
--   23503 must be classified RETRYABLE, never permanent.
--   OpType.DELETE must never reach the wire: no role holds DELETE.
--
-- DETERMINISTIC IDS (mandatory for these three tables)
--   trip_line        id = f("trip_line",        trip_id, product_id)
--   product_alias    id = f("product_alias",    household_id, store_chain,
--                                               raw_text_normalized)
--   suggestion_block id = f("suggestion_block", household_id, product_id)
--   f = deterministic, collision-resistant, 128-bit, formatted as UUIDv8.
--   Without this, two offline devices produce the same natural key with
--   different ids and no upsert shape can converge them.
--
-- DTO
--   A Room entity is NOT a DTO. member.is_self, member.email and
--   app_settings.sync_photos do not exist on the server; sending them
--   returns PGRST204 and jams that op forever. On the way back, supply
--   is_self (= auth_user_id equals my uid) and sync_photos (= false).
--   JsonNamingStrategy.SnakeCase, ignoreUnknownKeys = true, and NO
--   default values on sync DTOs.
-- =====================================================================
```

---

# 3. DOĞRULAMA

## 3.1 `get_advisors` NE YAKALAR

**Boş dönmesi gerekenler** (biri dolarsa gerileme var):

| Lint | Beklenti |
|---|---|
| `rls_disabled_in_public` | Boş — `public`teki 12 tablonun hepsinde RLS açık. |
| `rls_enabled_no_policy` | Boş — hepsinde en az bir politika var. |
| `function_search_path_mutable` (0011) | Boş — dokuz fonksiyonun dokuzunda da `set search_path = ''`. |
| `anon_security_definer_function_executable` (0028) | Boş — her fonksiyonda `revoke ... from public, anon`. **Bu lint'in asıl değeri burada**: `revoke ... from public` tek başına anon'un yetkisini kaldırmıyor, doküman bunu açıkça söylüyor. |
| `auth_rls_initplan` (0003) | Boş — her çağrı `(select ...)` içinde. Dolarsa biri sarmalayıcıyı kaldırmış demektir. |
| `multiple_permissive_policies` (0006) | Boş — komut başına tek politika. |
| `security_definer_view`, `no_primary_key` | Boş. |

**Doldurması BEKLENEN, kabul edilmiş bulgular** (susturulmalı ya da yazılı olarak kabul edilmeli):

| Lint | Neden kabul |
|---|---|
| `authenticated_security_definer_function_executable` (0029) | `create_household`, `join_household`, `rotate_join_code` **tanımı gereği** SECURITY DEFINER ve `authenticated`e açık — üçü de RLS'in henüz göremediği satırları yazmak için var. Her biri `auth.uid()` kontrolüyle açılıyor. Kasıtlı. |
| `unindexed_foreign_keys` | On kadar FK'nin kapsayıcı indeksi yok. Bu indeks silmede ve ebeveyn anahtar güncellemesinde işe yarar; ikisini de yapmıyoruz. Yüzlerce satırlık tablolarda bedeli sıfır. |
| `unused_index` | Migration'dan hemen sonra **hepsi** kullanılmamış görünür (trafik yok). İlk gerçek senkrondan sonra tekrar bakılmalı. |

## 3.2 `get_advisors`'ın YAKALAYAMAYACAKLARI — elle sınanması şart

1. **`app` şemasındaki hiçbir şey.** Advisor `public`/açık şemaları tarıyor; `app.sync_conflict` ve `app.join_attempt`'in RLS'i ve grant'siz oluşu elle doğrulanmalı.
2. **Politikanın İFADESİ.** `using (true)` bütün lint'lerden geçer. Doğruluk lint'in konusu değil.
3. **`force row level security`.** Hiçbir lint bakmıyor, ama açılırsa sahibin muafiyeti kalkar ve `app.current_household_ids()` 42P17'ye düşer:
   ```sql
   select relname from pg_class c join pg_namespace n on n.oid = c.relnamespace
    where n.nspname in ('public','app') and c.relforcerowsecurity;   -- BOŞ dönmeli
   ```
4. **Tetikleyici doğruluğu.** LWW, add-beats-remove, kod muhafızı, kimlik muhafızı — hiçbiri statik olarak denetlenmiyor.
5. **Tetikleyici SIRASI.** `t00_*` gerçekten `t10_sync`'ten önce mi:
   ```sql
   select tgrelid::regclass, tgname from pg_trigger
    where not tgisinternal order by 1, tgname;
   ```
6. **Sunucu/Room şema uyumu.** Hiçbir lint "sunucuda `is_self` yok" demez; bunu ancak sözleşme testi yakalar.

## 3.3 "Başka hanenin satırını okuyamıyorum" nasıl KANITLANIR

Bu iddia elle kanıtlanmalı; hiçbir advisor kanıtlayamaz. İki gerçek auth kullanıcısı (A, B), iki hane (Ha, Hb), her hanede en az bir tanınabilir satır. Bütün çağrılar **publishable key + kullanıcının kendi JWT'si** ile — `service_role` ile yapılan hiçbir test hiçbir şey kanıtlamaz (BYPASSRLS).

| # | Test | Beklenen |
|---|---|---|
| 1 | A olarak **filtresiz** `GET /rest/v1/trip_line?select=*` | Yalnızca Ha satırları. Sayı Ha'nın satır sayısına **eşit** olmalı — "B'nin satırını görmedim" yetmez, **fazlalık olmadığı** gösterilmeli. |
| 2 | A olarak `?household_id=eq.<Hb>` | `[]` — boş dizi, hata değil. Hata dönmesi bir oracle olurdu. |
| 3 | A olarak `GET /rest/v1/member?select=*` | Yalnızca Ha üyeleri. B'nin `auth_user_id`'si görünmemeli. **Özyineleme kanıtı da budur**: 42P17 gelseydi burada gelirdi. |
| 4 | A olarak Hb'nin `household_id`'siyle upsert | 42501 (`new row violates row-level security policy`). |
| 5 | A olarak **kendi** hanesindeki bir satırı Hb'ye taşıyan PATCH | 42501 — `with check` çalışıyor. |
| 6 | A olarak `POST /rpc/sync_pull {"p_household_id": "<Hb>"}` | 403 `NEYDI_NOT_MEMBER`. |
| 7 | A olarak `GET /rest/v1/sync_cursor` | Tek satır (Ha). Ardından `PATCH sync_cursor` → 42501/permission denied. |
| 8 | Yalnızca anon key (JWT yok) ile 1–7 | 401 ya da boş; hiçbir tabloda satır yok. |
| 9 | A olarak `PATCH /household?id=eq.<Ha>` gövdesinde `{"join_code":"AAAAAA","name":"x"}` | `name` değişir, `join_code` **değişmez** — kod muhafızı kanıtı. |
| 10 | A olarak `PATCH /member?id=eq.<A'nın eşi>` gövdesinde `{"auth_user_id":null}` | `auth_user_id` **değişmez**; eş hâlâ hanede — kimlik muhafızı kanıtı. |
| 11 | A olarak `POST /member` gövdesinde `{"auth_user_id":"<B'nin uuid'si>", ...}` | Satır yazılır ama `auth_user_id` **null** döner — yabancı içeri alınamıyor. |
| 12 | A olarak `DELETE /rest/v1/trip_line?id=eq.<kendi satırı>` | 42501 — DELETE hiçbir role verilmedi. |

**Ayrıca protokol testleri** (izolasyon değil, doğruluk):

| # | Test | Beklenen |
|---|---|---|
| 13 | Mezar taşı görünürlüğü: `deleted_at is not null` satır `sync_pull`'da **geliyor** | Gelmezse silmeler karşı cihaza hiç ulaşmaz. Politikalar bilerek `deleted_at` filtresi taşımıyor. |
| 14 | LWW: aynı satıra `updated_at = T` sonra `updated_at = T-1000` | İkinci istek **boş** temsil döner, `app.sync_conflict`'e bir satır düşer. |
| 15 | Ekleme silmeyi yener: tombstone satırı `deleted_at=null` ve **daha eski** damgayla push | Satır dirilir, `updated_at` tombstone'un bir üstüne çekilir. |
| 16 | Tekrarlı op: aynı gövde iki kez | İkincisi `server_version`'ı **artırmaz**. |
| 17 | İmleç tekliği: iki eşzamanlı yazma | İki farklı `server_version`, boşluksuz. |
| 18 | Kod tek kullanımlık: aynı kodla iki `join_household` | İkincisi `{ok:false, NEYDI_BAD_CODE}`. |
| 19 | Kısıtlayıcı gerçekten sayıyor: 11 yanlış kod | 11.'si `NEYDI_TOO_MANY`; `app.join_attempt`'te **10 satır** durmalı (sıfır değil — sıfırsa rollback hatası geri gelmiş demektir). |
| 20 | Doğal anahtar yakınsaması: iki istemci aynı `(trip_id, product_id)` için satır üretip push eder | **Tek** satır; ikisinin de `id`'si aynı. Deterministik id çalışmıyorsa burada 23505 patlar. |

---

# 4. BUGÜN YEREL OLARAK YAPILACAKLAR

E15'e temas etmeyen, hepsi bugün bedava ya da bedava sayılır:

1. **`Conventions.kt`'ye madde 7'yi yaz.** Dokuz KDoc "bkz. Conventions madde 7" diyor, dosyada madde 1–6 var — referans boşluğa gidiyor (doğruladım). Madde 7 şunu yazmalı: `updatedAt` LWW damgasıdır; null = hiç güncellenmedi, o zaman `createdAt` geçerlidir; **her mutasyon yolu damgayı yazar**; `createdAt` bir daha asla değişmez.
2. **`updatedAt`'i on mutasyon yoluna bağla (F7.4).** Bugün yazan tek yol `Daos.kt:114` (`setStaple`). Yazmayanlar: `ProductDao.softDelete` (124), `TripDao.setStatus` (182) / `setStoreIfAbsent` (197) / `closeIfOpen` (214), `TripLineDao.markAllTaken` (351) / `setOutcome` (386) / `setChecked` (394) / `softDelete` (397), `HouseholdDao.softDelete`. **Tombstone'ların hiçbiri damga taşımıyor** — LWW'de "sildim" ile "geri ekledim" yarışırsa karşılaştıracak bir şey yok. Damga outbox'ın önkoşulu, tersi değil.
3. **`ListRepository.kt:199` `createdAt` ezmesini düzelt.** Diriltme yolu `createdAt = clock()` yazıyor ve `updatedAt`'e hiç dokunmuyor — LWW için tam ters. `createdAt` sabit kalmalı, `updatedAt = clock()` olmalı. Satır 203'teki adet artırma da damgasız.
4. **`TripLineDao.delete`'i sil** (`Daos.kt:400`). Çağıranı yok ve Conventions madde 3'ü ihlal ediyor. Sunucuda DELETE yetkisi zaten yok; enkazı burada bırakmak, bir gün birinin onu çağırıp senkronu sessizce bozması demek.
5. **`DEFAULT_HOUSEHOLD_ID` derleme sabitini öldür** (`Bootstrap.kt:10`). Her kurulumda **aynı** — `create_household` hane id'sini istemciden aldığı için dünyada ilk çağıran kazanır, ikinci kullanıcıdan itibaren `unique_violation`. **Faz 7'yi bugün tamamen bloke ediyor.** İlk açılışta üretilen ve kalıcılaştırılan bir UUIDv7'ye çevrilmeli; on kadar ViewModel'e yayılmış olduğu için bugün yapılması yarın yapılmasından ucuz. (ROADMAP "Açık kararlar 4 — Hane yeniden anahtarlama" bunu zaten biliyor; bu, o kararın ucuz yarısı.)
6. **`trip_line` için deterministik id'ye geç** (`ListRepository.kt:209`) ve **Conventions madde 1'e istisna maddesini yaz**. Doğal anahtarlı üç tablonun id'si `newId()` değil, doğal anahtardan türetilir. `product_alias` ve `suggestion_block` için **kuralı bugün yaz, uygulamayı yazan kodla birlikte yap** (aşağı bak). Bugün yapılabilir olmasının nedeni: `pending_op` boş, DTO katmanı henüz yok, ve yerelde tek cihaz var — yani yeniden anahtarlama borcu sıfır.
7. **`isSelf` kararını yaz** (kolon değil, karar): senkron edilmez, `authUserId = kendi uid'im` diye türetilir. Aynı notta `MemberDao.self()`'in `LIMIT 1` tehlikesi de yazılmalı — eşleşmeden sonra her iki cihazda da iki satır `isSelf = 1` olurdu ve "ben kimim" sessizce yanlış cevaplanırdı.
8. **`member.email` kararını yaz**: senkron edilmez. Kodda `Member.kt:21` dışında **hiç geçmiyor** (doğruladım) ve modeldeki tek PII. Kolonu Room'dan silmek bump gerektirir — bugün değil, ama karar bugün.
9. **`OpType.DELETE` kararını yaz**: ya enum'dan düşür ya da "push katmanı DELETE'i soft-delete UPDATE'e çevirir" sözleşmesini yaz. Sunucuda DELETE yetkisi hiçbir role verilmedi; kuyruğa bir DELETE düşerse 42501 alır ve kuyruğun başı kalıcı tıkanır.
10. **İsimlendirme kararını Json yapılandırmasına yaz** — `JsonNamingStrategy.SnakeCase`, `ignoreUnknownKeys = true`, `PropertyConversionMethod`'a dokunma. `pending_op.payloadJson` serileştirilmiş gövdeyi **yerel diskte** tutuyor; karar ilk `pending_op` satırı yazılmadan önce kilitlenmeli, sonra değiştirilirse kuyrukta eski anahtarlı gövdeler kalır. Bugün bedava.

---

# 5. E15'İ BEKLEYENLER

| İş | Neden bekliyor |
|---|---|
| **Outbox'a yazan kod** | Outbox her yazma yolunu saran bir **kesit**; şeklini çağıranların toplamı belirler ve E15 o toplamın son iki üyesini (`ProductAliasDao.insert`, `PriceObservationDao.insert`) getiriyor. F5.10'un "kuralı önce yaz" mantığı burada geçmiyor: F5.10 tek fonksiyonluk saf bir kuraldı, bu değil. |
| **`PendingOpDao` / `SyncMetaDao` yüzeyi** | Entity'ler `NeydiDatabase.kt`'de kayıtlı ama **DAO'ları hiç yok** — tablolar Kotlin'den erişilemez. Bugün yazmak "hangi metodlar" sorusunu eksik çağıran listesiyle cevaplamak demek. |
| **Senkron DTO sınıfları** | Kuralı (snake_case, varsayılansız alan, `serialDescriptor` testi) bugün yazıyoruz; sınıfların kendisi hangi tabloların hangi sırayla push edileceğine bağlı, o da outbox'a bağlı. |
| **`product_alias` deterministik id uygulaması** | Kuralı bugün yazılıyor, ama insert yolunun kendisini E15 üretiyor — yolu var olmadan id türetmesini yazmak, çağıransız kod bırakmak. |
| **`member.authUserId` kolonu** | Nullable kolon = tam otomatik `AutoMigration(5,6)`, spec gerekmiyor; maliyet bugün de Faz 7.2'de de **aynı ve sıfıra yakın**. Erken eklemenin tek etkisi adının ve anlamının auth akışı şekillenmeden donması. Kazanç yok, kayıp var. |
| **`syncPhotos` kolonunu silmek** | v6 bump'ı + `@DeleteColumn` taşıyan bir `AutoMigrationSpec` demek; `NeydiDatabase.kt`'nin "spec yok" kuralını bozar. Bedeli var, kazancı yok — sunucuda zaten yok, istemcide okuyanı yok. |

---

# 6. AÇIKÇA BELİRSİZ BIRAKILANLAR

Bunlar karar **verilmedi**; uydurmuyorum:

1. **Hane soft-delete'i ve KVKK (F6.7).** `app.current_household_ids()` yalnızca `member.deleted_at`'e bakıyor; hane tombstone'lansa bile herkes yazmaya devam eder. Sunucuda **gerçek silmenin yolu yok** (DELETE hiçbir role verilmedi, hard-delete RPC'si de yok). Yani "Verilerimi sil" düğmesi bugün sunucuda hiçbir şey yapmaz. Membership fonksiyonuna `household.deleted_at is null` eklemek de çözüm değil — o zaman karşı cihaz tombstone'u hiç çekemez. **Karar verilmedi.**
2. **Haneden çıkma / üye çıkarma.** `leave_household` yok. İzolasyon-önce'nin sürümü ölümcül şekilde bozuktu ve ROADMAP Faz 7'de bu adımı listelemiyor. **Kapsam dışı, ama boşluk olduğu yazılmalı.**
3. **`auth.users` hesabı silinince `member.auth_user_id`.** FK'yi bilerek kaldırdık (hesap silmeyi kıran sınıfı yok etmek için); sonucu, silinmiş bir hesabın uuid'sinin `member` satırında kalması. KVKK açısından temiz değil. Temizleyecek yönetim işi **tasarlanmadı**.
4. **Zaten verisi olan bir cihazın var olan bir haneye katılması.** Bugünkü tasarım katılan cihazın **yerel sıfırlama** yaptığını varsayıyor (`Bootstrap` kendi hanesini ve mağazalarını tohumluyor). İki dolu cihazı birleştirmek bir merge protokolü ister; **Faz 7 kapsamı dışında.**
5. **Deterministik id'nin somut biçimi.** "128 bit, çakışmaya dayanıklı, UUIDv8 metni" sözleşme; SHA-256'yı bir bağımlılıkla mı yoksa elle mi getireceğiniz **açık** — commonMain'de hazır bir hash yok. Sunucu SQL'i bu seçimden etkilenmiyor.
6. **Realtime mi, yoklama mı v1?** ROADMAP F7.3 açıkça "Realtime postgres_changes" diyor. Bu tasarım Realtime'ı **kapı zili** olarak bırakıyor ama tek satırlık `sync_cursor` yoklamasını da eşdeğer bir yol olarak açıyor (ücretsiz planda proje bir hafta hareketsizlikte duruyor, F7.6 keep-alive de zaten bu yüzden var). Hangisinin v1 olduğu bir **roadmap sapması** ve adı konmalı.
7. **`app.sync_conflict` ne zaman temizlenir.** İki kişilik hanede hacmi yok, ama bir budama işi yazılmadı.
