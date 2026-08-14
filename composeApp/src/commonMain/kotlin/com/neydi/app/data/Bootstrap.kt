package com.neydi.app.data

import com.neydi.app.data.catalog.tohumlaKatalog
import com.neydi.app.data.db.Household
import com.neydi.app.data.db.Member
import com.neydi.app.data.db.NeydiDatabase

/** Tek hane oldugu icin id sabit. Kurulum ekrani (Faz 8) adini degistirebilecek. */
const val DEFAULT_HOUSEHOLD_ID: String = "0198f2a1-0000-7000-8000-000000000001"

/**
 * Uygulama acilisinda bir kez calisan hazirlik.
 *
 * commonMain'de: iOS de ayni tohumlamayi almali. Android'e ozel bir
 * Application.onCreate'e koysaydik iOS sessizce katalogsuz acilirdi ve bu
 * Mac'e gecene kadar fark edilmezdi.
 *
 * Tamami idempotent; tekrar cagrilmasi zararsiz.
 */
suspend fun NeydiDatabase.bootstrap(newId: () -> String, clock: () -> Long) {
    tohumlaKatalog()

    // Hane ve "ben" uyesi. Kurulum ekrani gelene kadar varsayilan isimlerle
    // duruyor - liste ekraninin calismasi icin bir hane ve bir uye SART.
    if (householdDao().getActive() == null) {
        householdDao().upsert(
            Household(id = DEFAULT_HOUSEHOLD_ID, name = "Bizim ev", createdAt = clock()),
        )
    }
    if (memberDao().self(DEFAULT_HOUSEHOLD_ID) == null) {
        memberDao().upsert(
            Member(
                id = newId(),
                householdId = DEFAULT_HOUSEHOLD_ID,
                displayName = "Ben",
                isSelf = true,
                createdAt = clock(),
            ),
        )
    }
}
