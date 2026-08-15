package com.neydi.app.data.receipt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * AKYURT'un IKI SATIRLI DUZENI (F4.14).
 *
 * GIRDI GERCEK: asagidaki satirlar kullanicinin telefonundan, `Receipt.rawOcrText`
 * kolonundan aynen cikarildi (fis `01a00732-df7a-...`). OCR hatalari dahil
 * hicbir sey duzeltilmedi - `Ka` (Kg olacakti), `869O508101426` (sifir yerine
 * harf O), `S0` (51 olacakti), satir sonlarindaki `t`/`E`/`Ł` artiklari ve
 * toplamdaki eksik hane (`*.709,08`, dogrusu 5.709,08) hepsi FISIN GERCEK
 * OKUNUSU.
 *
 * Sentetik kurgu bu projede iki kez bosa cikti; bu yuzden bu dosya elle
 * yazilmis tek bir satir bile icermiyor.
 *
 * IKI AYRI HATA VAR VE IKISI DE BURADA OLCULUYOR:
 *
 * 1. **Ad, tutar satirinin ALTINDA.** AKYURT once tutar satirini basiyor
 *    (`sira no + barkod + adet + birim fiyat + %KDV + tutar`), urun adini bir
 *    ALT satira. Ayristirici "ad ve tutar ayni gorsel satirda" varsayiyordu,
 *    o yuzden her satirin adi BARKOD cikiyordu.
 *
 * 2. **Satir sonu artigi tutari gizliyordu.** `AMOUNT_SUFFIX` tutarin satir
 *    SONUNDA olmasini sart kosuyordu; OCR'in ekledigi tek harflik artik
 *    (`176,31 t`) eslesmeyi tamamen dusuruyordu. On dokuz urun satirinin
 *    ALTISI bu yuzden hic okunmadi - ve bu, iki satirli duzenden bile buyuk
 *    bir kayipti.
 */
class AkyurtLayoutTest {

    /**
     * Cihazdaki AKYURT fisinin ORTA parcasi, ham OCR gorsel satirlari.
     *
     * Kunye satiri YOK cunku bu bir orta parca - uzun fisin ilk karesinde
     * basili. Bu da gercegin bir parcasi ve testte oyle duruyor.
     */
    private val akyurtLines = listOf(
        "42 2980889 1,764 Kg 99,95 %01 176,31 t",
        "TAVUK SENPILİÇ POŞETLİ KG",
        "43 8690576029431 1 Adet 28,50 %01 28,50",
        "ANKARA MAK.500 GR.KALEM",
        "44 8690576029257 1 Adet 28,50 %01 28,50",
        "ANKARA MAK,500 GR. MANTI",
        "45 8685198441150 1 Adet 349,95 %01 349,95 t",
        "ÜÇ YILDIZ TAZE KAŞAR 1000 GR",
        "46 2902885 0,886 Ka 169,90 %01 150,53 t",
        "KİRAZ KG.",
        "47 2905089 1,644 Kg 79,90 %01 131,36 E",
        "DOMATES SALKIM KG",
        "48 8684000453503 1 Adet 41,95 %01 41,95 Ł",
        "YÖRSAN sỬT 1 LT3 YAĞLI",
        "49 S684000453503 1 Adet 41,95 %01 41,95 E",
        "YÖRSAN sÜT 1 LT3 YAĞLI",
        "S0 2902898 0,648 Kg 59,50 %01 38,56",
        "sOĞAN KURU KG.",
        "51 2905090 3,284 Kg 39,50 %01 129,72 t",
        "PATATES TAZE KG.",
        "52 86920117 2 Adet 6,50 %01 13,00",
        "ETİ PUF 16 GR.HÍNDİSTAN CEVİZLİ.",
        "53 8684000453749 1 Adet 89,95 %01 89,95 E",
        "YÖRSAN SÜZME PEYNİR 400 GR",
        "54 8684928923317 1 Adet 84,90 %01 84,90",
        "MANTAR HÜRRİYET YILMAZ 400 GR PKT",
        "55 8691381000370 1 Pkt 60,90 %01 60,90",
        "BEYPAZARI SODA 200 ML.KARPUZ-ÇILE",
        "56 869O508101426 1 Adet 53,90 %01 53,90",
        "TUKAS DOM, SALCA 830 GR,TNK",
        "57 8690508101426 1 Adet 53,90 %01 53,90",
        "TUKAS DOM, SALÇA 830 GR.TNK",
        "58 8690574114481 1 Adet 52,90 %10 52,90",
        "PEPSİ COLA 1,5 LT MAXX",
        "59 8690565027851 1 Adet 149,95 %01 149,95 t",
        "PINAR YOĞURT 1 YAĞLI 2000 GR",
        "60 2902866 11,315 Kg 9,90 %01 112,02 t",
        "KARPUZ KG",
        "KDV % Matrah KDV Tutar",
        "o01 4.571,58 45,72",
        "o10 248,88 24,89",
        "o20 681,67 136,33",
        "Índirim o25 INDIRIM SENSODYNE VE PARODOr : •73,63 TL",
        "Indirimn 1 ALANA 1 BEDAVA ALPEDO 400 GR URL: 209,90 TL",
        "KDV TOPLAM * 206,94 t",
        "Brüt Toplam *5.992,61 t",
        "Indirim Toplanı K 283,53 t",
        "Ödenecek KDV Dahíl Tutar *.709,08 t",
        "YALNIZ BEŞBİNYEDİYÜZDOKUZTürkLtasiSEKIZ- dur",
        "Iş Bankası Kredi Kartı *5.709, 08",
        "Müş PUAN: Müs Ind,. : *0,00",
        "00169/000490/14302/ F** ** S****",
        "e-Arşiv Fatura kapsamında oluşturulmuştur.",
        "ÍRSALIYE YERINE GEÇER",
        "Faturanız Için www.akyurt.com.tr/earsiv",
        "Bankacilık İşlem Bilgileri TÜRKIYE İŞ BAN <ASI",
        "51932405****3594 *5.709,08 TL",
        "12.08.2026 18:36 Bat.ID: 000264 SN: 00030",
        "RefNo: 6224105113795 Onay No: 022403",
        "Is.ID: 698191962 Term: S1BKRQ3C",
    )

    private fun reading() = parseReceipt(akyurtLines)

    // --- Ad, tutar satirinin altinda ----------------------------------------

    /**
     * ADLAR OKUNUYOR, BARKOD DEGIL. Bu adimin butun sebebi bu tek iddia.
     *
     * Ilk uc urunun adi tam olarak fiste yazan hali - normalizasyon yok,
     * duzeltme yok. Fiste "MAK,500" virgulle basilmis ve oyle kaliyor:
     * yazim hatasini biz duzeltirsek kullanici fise bakip dogrulayamaz.
     */
    @Test
    fun productNamesComeFromTheLineBelow() {
        val names = reading().lines.map { it.name }
        assertTrue(
            names.contains("TAVUK SENPILİÇ POŞETLİ KG"),
            "ilk urunun adi alt satirdan gelmeliydi, gelen: ${names.take(3)}",
        )
        assertTrue(names.contains("ANKARA MAK.500 GR.KALEM"))
        assertTrue(names.contains("ÜÇ YILDIZ TAZE KAŞAR 1000 GR"))
    }

    /** HICBIR SATIR ADINDA BARKOD TASIMIYOR - eski halin tam belirtisi buydu. */
    @Test
    fun noLineIsNamedAfterItsBarcode() {
        val barcodeNamed = reading().lines.filter { line ->
            line.name.split(" ").any { it.length >= 8 && it.all(Char::isDigit) }
        }
        assertEquals(emptyList(), barcodeNamed.map { it.name })
    }

    // --- Satir sonu artigi ---------------------------------------------------

    /**
     * SATIR SONU ARTIGI TUTARI GIZLEMIYOR.
     *
     * `176,31 t` satiri eskiden hic eslesmiyordu. Bu tek harf, on dokuz urun
     * satirinin altisini goze gorunmez yapiyordu.
     */
    @Test
    fun trailingOcrNoiseDoesNotHideTheAmount() {
        val amounts = reading().lines.map { it.amountMinor }
        assertTrue(17631 in amounts, "artikli ilk satir okunmaliydi")
        assertTrue(34995 in amounts, "349,95 t okunmaliydi")
        assertTrue(11202 in amounts, "112,02 t okunmaliydi")
    }

    /**
     * ON DOKUZ URUN SATIRI, ON DOKUZ SATIR.
     *
     * Sayi fisin kendisinden: sira numaralari 42'den 60'a kadar kesintisiz.
     * Eski hal 13 satir uretiyordu.
     */
    @Test
    fun allNineteenProductLinesAreRead() {
        val lines = reading().lines.filterNot { it.discount }
        assertEquals(19, lines.size, "okunan adlar: ${lines.map { it.name }}")
    }

    // --- Kunye satiri olmayan parca -----------------------------------------

    /**
     * TUTAR SATIRI MAGAZA ADI OLAMAZ.
     *
     * Cihazda tam bu oldu: kunye bu parcada basili olmadigi icin yedek aday
     * ILK satiri aldi ve Ayarlar'a "42 2980889 1,764 Kg..." diye bir zincir
     * yazilacakti (karar 11'den sonra kalici olurdu).
     */
    @Test
    fun amountLineIsNotMistakenForStoreName() {
        val store = reading().storeName
        assertTrue(
            store == null || !store.startsWith("42 "),
            "magaza adi tutar satirindan gelmemeliydi, gelen: $store",
        )
    }

    // --- Toplam --------------------------------------------------------------

    /**
     * OCR TOPLAMDAN BIR HANE DUSURDU (`*.709,08`, dogrusu 5.709,08) ve bunu
     * DUZELTMIYORUZ.
     *
     * Eksik haneyi tahmin etmek, kullanicinin sorgulayamayacagi bir yerde
     * uydurma bir rakami gercek gibi sunmak olurdu. Dogru davranis toplami
     * OKUNAMADI saymak; karar 15 zaten o hal icin "~" onekli manseti veriyor.
     */
    @Test
    fun brokenTotalIsNotGuessed() {
        val total = reading().totalMinor
        assertTrue(
            total == null || total != 70908L,
            "eksik haneli toplam gercek gibi kabul edilmemeliydi: $total",
        )
    }
}
