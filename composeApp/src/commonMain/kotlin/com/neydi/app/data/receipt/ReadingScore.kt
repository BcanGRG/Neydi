package com.neydi.app.data.receipt

/**
 * Bir OKUMANIN ne kadar kullanisli oldugunu puanlar.
 *
 * NEDEN AYRISTIRICIYI CALISTIRIYOR: eski puanlayici satirin SEKLINE bakiyordu -
 * "solunda metin, sagında tutar" olan satirlari sayiyor, **rakamla baslayan
 * satirlari hic saymiyordu**. Gerekcesi o gun dogruydu (yanlis yonde tutarlar
 * yalniz kalir), ama AKYURT duzeninde her urun satiri SIRA NUMARASIYLA, yani
 * rakamla basliyor - o fislerin butun satirlari **sifir puan** aliyordu ve yon
 * secimi pratikte rastgele oluyordu. Cihazda tam bu goruldu: ayni fotograf
 * pes pese okumada bir 19 satir, bir 3 satir, bir hic verdi.
 *
 * Dogru olcut sekil degil SONUC: bu okumadan kac URUN SATIRI cikiyor. Boylece
 * puanlayici desteklenen her duzeni - bugunkuleri ve yarin eklenecekleri -
 * kendiliginden dogru olcuyor, cunku olctugu sey ayristiricinin kendisi.
 *
 * TOPLAM AYRI BIR AGIRLIK TASIYOR: toplami okunmus bir okuma, ayni sayida satir
 * veren ama toplami okunamamis bir okumadan iyidir - aritmetik kapisi ancak o
 * sayiyla calisiyor. Agirlik uc satir degerinde; toplam tek basina uc satirdan
 * daha degerli degil ama esitlik bozan bir agirligi olmali.
 *
 * NEDEN commonMain: eski hali `androidMain`'deydi ve **test edilemiyordu**
 * (F10.13). Girdisi yalnizca metin satirlari oldugu icin platforma ait bir sebep
 * yoktu; tasinmasi F10.13'un yarisini kapatiyor.
 */
internal fun score(rows: List<String>): Int {
    val reading = parseReceipt(rows)
    return reading.lines.size + if (reading.totalMinor != null) TOTAL_BONUS else 0
}

private const val TOTAL_BONUS = 3
