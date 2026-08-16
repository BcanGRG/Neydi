package com.neydi.app.data.receipt

import com.neydi.app.data.db.Receipt
import com.neydi.app.data.store.chainKey

/**
 * Bir gezideki fisleri FIZIKSEL FISLERE gruplar (F4.13 duzeltmesi).
 *
 * NEDEN GEREKLI: aritmetik degismez fiziksel fise ait, fotografa degil. Uzun
 * fis parca parca cekilince TOPLAM yalnizca son parcada basili oluyor ve o
 * parca fisin yalnizca bir bolumunun satirlarini tasiyor - kapiyi tek fotograf
 * kapsaminda hesaplamak orada YAPISAL OLARAK "tutmuyor" cikariyor.
 *
 * AMA GEZI KAPSAMI DA YANLIS ve bu cihazda olculdu: kullanicinin
 * veritabanindaki bir gezide iki AYRI magaza fisi vardi (BIM 225,50 ve File
 * Market 484,58). Ikisini tek kumede toplamak, File Market'in dogru yesil
 * kapisini BIM'in ayristirma hatasi yuzunden sahte amber'a ceviriyordu - yani
 * duzeltilmek istenen hatanin ta kendisi, yalnizca yer degistirmis hali.
 *
 * AYIRT EDICI VERI MAGAZA ZINCIRI: fisin kunyesi yalnizca BASINDA basili
 * oldugu icin uzun fisin ilk parcasi magaza adini tasiyor, sonraki parcalar
 * TASIMIYOR. Iki ayri magaza fisiyse ikisi de kendi adini tasiyor ve adlar
 * FARKLI. Kural bu gozlemden turetildi:
 *
 *   - Okunabilir bir zincir tasiyan fis, icinde bulundugu grubun zincirinden
 *     FARKLIYSA yeni grup acar.
 *   - Magaza adi okunamamis fis ONCEKI gruba KATILIR: uzun fisin devam
 *     parcasinin beklenen hali bu.
 *
 * SIRALAMA `capturedAt`: parcalar cekim sirasina gore birbirini izliyor.
 * `id` sirasina guvenmek UUID v7 ile kazara calisirdi ama sozlesme degil.
 *
 * TAVIZ KAYDA GECSIN: ayni magazadan ayni gezide cekilmis IKI AYRI fis
 * (ornegin kasadan iki kez gecmek) tek fiziksel fis sayilir ve kapi ikisinin
 * toplamini karsilastirir - toplamlarin ikisi de okunduysa sonuc yine dogru
 * cikar, cunku hem satirlar hem toplamlar toplanir. Bozuldugu tek hal
 * birinin toplaminin okunamamasi, ki o zaten "dogrulanamadi" durumu.
 *
 * @return [targetId]'nin ait oldugu grup; fis listede yoksa bos liste.
 */
internal fun samePhysicalReceipt(receipts: List<Receipt>, targetId: String): List<Receipt> =
    physicalReceipts(receipts).firstOrNull { group -> group.any { it.id == targetId } }.orEmpty()

/** Gezideki fisleri fiziksel fis gruplarina ayirir - bkz. [samePhysicalReceipt]. */
internal fun physicalReceipts(receipts: List<Receipt>): List<List<Receipt>> {
    val groups = mutableListOf<MutableList<Receipt>>()
    var openChain: String? = null
    for (receipt in receipts.sortedBy { it.capturedAt }) {
        val chain = receipt.storeNameRaw?.takeIf { it.isNotBlank() }?.let { chainKey(it) }
        val current = groups.lastOrNull()
        // Zincir okunamadiysa onceki gruba katiliyor: uzun fisin devam
        // parcasinin beklenen hali bu (kunye yalnizca ilk karede).
        if (current != null && (chain == null || chain == openChain)) {
            current.add(receipt)
        } else {
            groups.add(mutableListOf(receipt))
            openChain = chain
        }
        // Grubun zinciri ILK okunabilir addan geliyor ve sonra degismiyor;
        // yoksa okunamayan bir parca zinciri null'a dusurur ve bir sonraki
        // FARKLI magaza fisi de ayni gruba katilirdi.
        if (openChain == null) openChain = chain
    }
    return groups
}
