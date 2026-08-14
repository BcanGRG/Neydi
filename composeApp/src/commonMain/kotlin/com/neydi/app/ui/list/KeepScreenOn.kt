package com.neydi.app.ui.list

import androidx.compose.runtime.Composable

/**
 * Alisveris modunda ekran UYANIK kalir.
 *
 * Reyonda telefon her 30 saniyede bir kararsa kullanici listeyi okumak icin
 * surekli ekrani acar; elleri dolu, tek eli serbest. Bu, ekranin en cok
 * kullanildigi anda en cok engel cikardigi durum.
 *
 * YALNIZCA alisveris modunda: planlama modunda ekrani zorla acik tutmak
 * pil yakar ve karsiliginda hicbir sey vermez.
 */
@Composable
expect fun KeepScreenOn(enabled: Boolean)
