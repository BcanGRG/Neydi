package com.neydi.app.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Hane - her seyin koku.
 *
 * Bu uygulamada TEK hane var (kullanici + esi). Yine de her satir householdId
 * tasiyor, cunku Supabase'e gecince satir seviyesi guvenlik (RLS) bu alana
 * dayanacak ve sonradan eklemek tum tablolari yeniden yazmak demek.
 *
 * Silme YOK: deletedAt tombstone. Iki cihaz cevrimdisi calisirken gercek silme
 * "benim sildigim urun geri geldi" ya da daha kotusu "esimin ekledigi urun
 * kayboldu" olarak tezahur eder. Tombstone senkronun uzlastirabilecegi tek sey.
 */
@Entity(tableName = "household")
data class Household(
    /** UUID v7 - zaman siralanabilir, boylece index'ler dagilmaz. */
    @PrimaryKey val id: String,
    val name: String,
    /**
     * Haneye katilma kodu - 6 karakter, Ayarlar'da dokunmayla kopyalaniyor.
     *
     * Null = henuz uretilmedi. Uretimi ve dogrulanmasi F7.2'nin isi; kolon
     * simdi eklendi cunku sonradan eklemek bir sema bump'i daha demekti.
     */
    val joinCode: String? = null,
    val createdAt: Long,
    /** LWW icin; null = hic guncellenmedi, `createdAt` gecerli (bkz. Conventions madde 7). */
    val updatedAt: Long? = null,
    val deletedAt: Long? = null,
)
