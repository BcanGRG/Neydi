package com.neydi.app.data.db

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.first

/**
 * GECICI. F2.6'da repository katmani gelince SILINECEK.
 *
 * Room zincirinin ucundan ucuna calistigini CIHAZDA gostermek icin var:
 * veritabani aciliyor mu, yazma isliyor mu, Flow gozlemi tetikleniyor mu.
 * Uc adimin herhangi biri koparsa derleme yesil kalir ama uygulama calismaz -
 * o yuzden kanit ekranda olmali, log'da degil.
 */
@Composable
fun rememberDbProbe(): String {
    var sonuc by remember { mutableStateOf("veritabani aciliyor...") }

    LaunchedEffect(Unit) {
        sonuc = runCatching {
            val db = buildNeydiDatabase()
            val dao = db.householdDao()

            if (dao.getActive() == null) {
                dao.upsert(
                    Household(
                        id = "0198f2a1-0000-7000-8000-000000000001",
                        name = "Bizim ev",
                        createdAt = 0L,
                    ),
                )
            }
            // Flow uzerinden oku: yalnizca yazma degil, invalidation da calisiyor mu.
            val gozlenen = dao.observeActive().first()
            "hane: ${gozlenen?.name ?: "YOK"} · Flow gozlemi calisti"
        }.getOrElse { "HATA: ${it::class.simpleName}: ${it.message}" }
    }

    return sonuc
}
