package com.neydi.app.data.receipt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testler GERCEK FISLERIN OCR CIKTISINDAN geliyor, elle yazilmis ornekten degil.
 *
 * NEDEN BU FARK ONEMLI: onceki surumde 17 test vardi, hepsi geciyordu ve
 * hicbiri bir sey kanitlamiyordu - cunku ornek fisleri de kurallari da ben
 * yazmistim, yani kendi varsayimlarimi kendime onaylatiyordum. Iki gercek fis
 * uc varsayimi birden curuttu (nokta ondalik, toplam satirinda KDV gecmesi,
 * miktar satirinin urunden once gelmesi).
 *
 * Asagidaki satirlar cihazda ML Kit'in urettigi ve gorsel satirlara gruplanmis
 * CIKTININ AYNISI - OCR hatalari dahil, hicbiri temizlenmedi. `TURŞU KORNI ŞON`
 * bolunmus kelime, `21.` bozuk KDV isareti, `x484.58` yildiz yerine x, `2 ad %
 * 25.50` carpim yerine yuzde: hepsi gercek ve hepsi gecmek zorunda.
 */
class ReceiptParserTest {

    /** BIM, 13.08.2026, cihazda okunan hali. */
    private val bim = listOf(
        "E-Arsiv Fatura",
        "BIM BIRLESIK MAGAZALAR A.S.",
        "BADEMLIK MAH.BADEML IK YOLU CAD.",
        "NO :55/A-B KEÇIÖREN / ANKARA",
        "Buyük Mükel lefler VDM 1750051846",
        "FATURA N0:T082026082544469",
        "13.08.2026 18:49 Sira No : 218",
        "ETTNO483dfaa-6f61 -43ef-9b1e-09a6159393c3",
        "12479021308613420218",
        "TCKN/VKN:11111111111-NIHAL TÜKETICI",
        "2 ad X 53.00",
        "KREMA 18YAĞLI 200ML %1. *106.00",
        "TURŞU KORNI ŞON 670G 21. *84.50",
        "ALIŞVERIŞ POŞETi BiM 220 *1.00",
        "GOFRET FIND KREM142G %1. *34.00",
        "TOPLAM KDV *2.39",
        "Odenecek KDV Dahil Tutar *225.50",
        "Banka Kredi Kartı (1) *225.50",
        "GARANT! BANKASI",
        "1:1982030 T:03394805 519324******3594",
        "13.08.2026 18:49 B:3004 S:4742",
        "Onay No:700733 Ref.No:622576613383",
        "KDV MATRAH %1. 222.28 220 0.83 KDV TUTAR *0.17 *2.22 KDV DAHIL *224.50 *1.00",
        "POS:2 - 342619 - Mxx** G**** 9519124792",
        "12479NECID AZheECiÖRENSlan342",
    )

    /** File Market, 12.08.2026, cihazda okunan hali. */
    private val file = listOf(
        "aalye",
        "E-frslv Fatura",
        "FiLE OVACIK / KEÇ1ÖREN/ ANKARA",
        "FiLE MARKET MAĞAZACIL IK ANONiM ŞIRKET!",
        "OVACIK MAHALLESI, YOZGAT BULVARI 84-A KE",
        "ÇlöREN / ANKARA",
        "Sarigazi VDM 3671427056",
        "FATURA NO:VO82026003852402",
        "12.08.2026 18:46 Sira No : 153",
        "ETTNS745a21 C-ccAd-4a08-8fa0-889F91102659",
        "23230031208605520153",
        "TCKN/VKN:11111111111-NiHAi TüKETiCi",
        "İNCEYULAF350G HARRAS %1. *49.00",
        "2 ad X 41.50",
        "HARRAS ACI BiBER SOS %1. *83.00",
        "0.182 kg X 690.00",
        "KRUVASAN+ KG %1. *125.58",
        "HARRAS SUTLÜ CIK.80G %1. *47.00",
        "SRIRACHA S0S 230 GR %1. *129.00",
        "2 ad % 25.50",
        "HARRAS VYEBiTSIN HIND %1. *51.00",
        "TOPLAM KDV 4.80",
        "Odenecek KDV Dahil Tutar 484.58",
        "Ippos Kredi Kartı (1) x484.58",
        "GARANTI BANKASI",
        "I:2936784 T:04284345 519324****3594",
        "12.08.2026 18:47 B:1187 S:8",
        "Onay No:782192 Ref.No:622478677539",
        "KDV MATRAH %1. 479.78 KDV TUTAR KDV DAHIL *4.80 *484.58",
        "POS:3 - 430831 - į*** Gk*** 940123233",
        "223 nvarK / KECiÖREN/ ANKAGS No: 552",
    )

    // --- Para ---------------------------------------------------------------

    /** Ilk surum noktayi REDDEDIYORDU ve iki zincirin ikisi de nokta basiyor. */
    @Test
    fun parsesDotAsDecimalSeparator() {
        assertEquals(10600, parseMinor("106.00"))
        assertEquals(48458, parseMinor("484.58"))
        assertEquals(18, parseMinor("0.18"))
    }

    /** Virgul de kabul: baska zincirler oyle basabilir, ikisini de destekliyoruz. */
    @Test
    fun parsesCommaAsDecimalSeparator() {
        assertEquals(1250, parseMinor("12,50"))
        assertEquals(123456, parseMinor("1.234,56"))
        assertEquals(123456, parseMinor("1,234.56"))
    }

    /** OCR yildizi bazen `x` okuyor, bazen hic okumuyor - ucu de gecmeli. */
    @Test
    fun acceptsStarOrXOrNoPrefix() {
        assertEquals(10600, parseMinor("*106.00"))
        assertEquals(48458, parseMinor("x484.58"))
        assertEquals(480, parseMinor("4.80"))
    }

    /** Iki ondalik hanesi olmayan sayi PARA DEGIL. */
    @Test
    fun rejectsNonMoneyNumbers() {
        assertNull(parseMinor("1"))
        assertNull(parseMinor("0.182"))
        assertNull(parseMinor("%1."))
        assertNull(parseMinor("abc"))
    }

    // --- BIM fisi -----------------------------------------------------------

    @Test
    fun bimReceiptYieldsFourProducts() {
        val r = parseReceipt(bim)
        assertEquals(
            listOf(10600L, 8450L, 100L, 3400L),
            r.lines.map { it.amountMinor },
        )
    }

    /** Toplam satiri "Odenecek KDV Dahil Tutar" - icinde KDV gectigi halde bulunmali. */
    @Test
    fun bimTotalIsFoundDespiteContainingVat() {
        assertEquals(22550, parseReceipt(bim).totalMinor)
    }

    /** "TOPLAM KDV *2.39" urun DEGIL: toplamin icindeki verginin dokumu. */
    @Test
    fun bimVatLineIsNotAProduct() {
        val lines = parseReceipt(bim).lines
        // Negatif iddianin TABANI: satir listesi bosalirsa `none {}` yine true
        // doner ve test hicbir sey kanitlamaz.
        assertEquals(4, lines.size)
        assertTrue(lines.none { it.amountMinor == 239L })
    }

    /** Odeme satiri toplamla AYNI tutari tasiyor; urun sayilirsa toplam ikiye katlanir. */
    @Test
    fun bimPaymentLineIsNotAProduct() {
        val lines = parseReceipt(bim).lines
        assertEquals(4, lines.size)
        assertTrue(lines.none { it.amountMinor == 22550L })
    }

    /** Miktar satiri urunden ONCE geliyor: "2 ad X 53.00" sonra KREMA *106.00. */
    @Test
    fun bimQuantityLineBindsToFollowingProduct() {
        val krema = parseReceipt(bim).lines.first()
        assertEquals(2.0, krema.count)
        assertEquals("ad", krema.unit)
        assertEquals(5300, krema.unitPriceMinor)
        assertEquals(10600, krema.amountMinor)
    }

    /** Bozuk KDV isareti ("21.", "220") urun adindan temizlenmeli. */
    @Test
    fun bimStripsMangledVatMarkFromName() {
        val names = parseReceipt(bim).lines.map { it.name }
        assertEquals(4, names.size)
        assertTrue(names.none { it.endsWith("21.") || it.endsWith("220") }, "kalan: $names")
    }

    @Test
    fun bimArithmeticHolds() {
        assertEquals(true, arithmeticHolds(parseReceipt(bim)))
    }

    // --- File Market fisi ---------------------------------------------------

    @Test
    fun fileReceiptYieldsSixProducts() {
        val r = parseReceipt(file)
        assertEquals(
            listOf(4900L, 8300L, 12558L, 4700L, 12900L, 5100L),
            r.lines.map { it.amountMinor },
        )
    }

    /** Bu fiste tutarlarin oneki YOK: "Odenecek KDV Dahil Tutar 484.58". */
    @Test
    fun fileTotalIsFoundWithoutStarPrefix() {
        assertEquals(48458, parseReceipt(file).totalMinor)
    }

    /** Tartili urun: "0.182 kg X 690.00" -> KRUVASAN, 0,182 kg, 125,58 TL. */
    @Test
    fun fileWeighedItemCarriesWeightAndUnitPrice() {
        val kruvasan = parseReceipt(file).lines.first { it.amountMinor == 12558L }
        assertEquals(0.182, kruvasan.count)
        assertEquals("kg", kruvasan.unit)
        assertEquals(69000, kruvasan.unitPriceMinor)
    }

    /** OCR carpiyi `%` okumus: "2 ad % 25.50" yine miktar satiri sayilmali. */
    @Test
    fun filePercentIsAcceptedAsMultiplicationSign() {
        val hindi = parseReceipt(file).lines.first { it.amountMinor == 5100L }
        assertEquals(2.0, hindi.count)
        assertEquals(2550, hindi.unitPriceMinor)
    }

    @Test
    fun fileArithmeticHolds() {
        assertEquals(true, arithmeticHolds(parseReceipt(file)))
    }

    // --- Aritmetik kapisi ---------------------------------------------------

    /** Kacirilan satir toplami bozar - kapinin butun varlik sebebi bu. */
    @Test
    fun missingLineIsCaught() {
        val eksik = parseReceipt(bim).let { r ->
            r.copy(lines = r.lines.dropLast(1))
        }
        assertEquals(false, arithmeticHolds(eksik))
    }

    /** "Dogrulanamadi" ile "tutmadi" AYRI SEYLER - null bunu tasiyor. */
    @Test
    fun returnsNullWhenTotalUnreadable() {
        val toplamsiz = parseReceipt(bim.filter { !it.contains("Odenecek") })
        assertNull(arithmeticHolds(toplamsiz))
    }

    /** Tartili urun yuvarlamasi bes kurusa kadar oynatabilir. */
    @Test
    fun allowsFiveMinorTolerance() {
        val r = parseReceipt(bim)
        assertEquals(true, arithmeticHolds(r.copy(totalMinor = r.totalMinor!! + 4)))
        assertEquals(false, arithmeticHolds(r.copy(totalMinor = r.totalMinor!! + 13)))
    }
}
