package com.neydi.app.ui.list

/**
 * Bir satirin `ListContent` icindeki `LazyColumn` dizini.
 *
 * ⚠ **BU FONKSIYON `ListContent`'IN ICERIK SIRASINI AYNALIYOR.** LazyColumn'a
 * yeni bir `item {}` eklenirse burasi da degismeli, yoksa listeye kaydirma bir
 * satir sasar. Ikisini tek yapiya indirmek mumkundu ama ana ekranin butun
 * govdesini yeniden yazmak gerekirdi; bunun yerine sira BURADA sayiyla yazili
 * ve `RowIndexTest` aritmetigi kilitliyor.
 *
 * NEDEN `layoutInfo`DAN OKUNMUYOR: `LazyListState.layoutInfo` yalnizca
 * **bestelenmis** ogeleri taniyor. Yeni eklenen satir ekranin epey altindaysa
 * hic bestelenmemis olur ve orada aranan sey bulunamaz - tam da kaydirmanin
 * gerektigi durumda. Dizini veriden hesaplamak bu bosluga dusmiyor.
 *
 * @return dizin, ya da satir listede yoksa null.
 */
internal fun rowIndexInList(state: ListState, showsClipboardChip: Boolean, rowId: String): Int? {
    // --- LazyColumn'un bas kismi (bkz. ListContent) ---
    var index = 1 // baslik: her zaman var
    if (!state.isEmpty) index++ // "tahmin" karti
    if (state.isEmpty) {
        index++ // bos durum
    } else if (showsClipboardChip) {
        index++ // pano cipi
    }

    // --- Reyon bloklari: her biri bir baslik + satirlari ---
    state.sections.forEach { section ->
        index++ // reyon basligi
        section.rows.forEach { row ->
            if (row.id == rowId) return index
            index++
        }
    }

    // --- "Alindi" blogu ---
    // Yeni eklenen satir buraya DUSMEZ ama fonksiyon genel: cagiran taraf
    // isaretlenmis bir satiri da gorunur kilmak isterse dogru cevabi alsin.
    if (state.taken.isNotEmpty()) {
        index++ // "Alındı" basligi
        state.taken.forEach { row ->
            if (row.id == rowId) return index
            index++
        }
    }

    return null
}
