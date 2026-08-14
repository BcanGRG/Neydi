package com.neydi.app.data

import com.neydi.app.data.catalog.tohumlaKatalog
import com.neydi.app.data.db.NeydiDatabase

/**
 * Uygulama acilisinda bir kez calisan hazirlik.
 *
 * commonMain'de: iOS de ayni tohumlamayi almali. Android'e ozel bir
 * Application.onCreate'e koysaydik iOS sessizce katalogsuz acilirdi ve bu,
 * Mac'e gecene kadar fark edilmezdi.
 *
 * Kendisi idempotent (tohumlaKatalog oyle); tekrar cagrilmasi zararsiz.
 */
suspend fun NeydiDatabase.bootstrap() {
    tohumlaKatalog()
}
