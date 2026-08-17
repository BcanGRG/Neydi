package com.neydi.app.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neydi.app.data.DEFAULT_HOUSEHOLD_ID
import com.neydi.app.data.db.CatalogSeedDao
import com.neydi.app.data.db.PriceObservation
import com.neydi.app.data.db.PriceObservationDao
import com.neydi.app.data.db.StoreDao
import com.neydi.app.data.db.insertUnlessRecentDuplicate
import com.neydi.app.data.image.deleteFileAt
import com.neydi.app.data.image.downscaleForOcr
import com.neydi.app.data.ocr.TagFields
import com.neydi.app.data.ocr.readTag
import com.neydi.app.data.ocr.readTagFields
import com.neydi.app.data.parseMinorInput
import com.neydi.app.data.repo.ListRepository
import com.neydi.app.data.repo.resolveProduct
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Etiket cekim ekraninin beyni (E15).
 *
 * ## Kamera bu sinifi HIC gormüyor
 *
 * Ekran fotografi cekiyor ve yalnizca YOLU veriyor. Sebep test edilebilirlik:
 * bu makinede cihaz test kaynak kumesi yok, ama `commonTest` gercek bir
 * bellek-ici Room veritabani kurabiliyor. Kamera disarida kalinca cekimden
 * gozleme kadar butun yol - OCR, alan doldurma, urun cozme, mukerrer koruma -
 * cihazsiz kosuyor.
 *
 * ## Fotograf KAYDEDINCE DE VAZGECINCE DE siliniyor
 *
 * Karar 29 fotografi saklamiyor. Ilk yazdigimda yalnizca kaydetme yolunda
 * siliyordum; vazgecen kullanicinin karesi diskte kaliyordu - yani "fotograf
 * saklanmiyor" sozu yalnizca mutlu yolda dogruydu.
 */
internal class TagCaptureViewModel(
    private val repo: ListRepository,
    private val catalogSeedDao: CatalogSeedDao,
    private val storeDao: StoreDao,
    private val priceObservationDao: PriceObservationDao,
    private val clock: () -> Long,
    private val newId: () -> String,
    /** OCR ADIMI DISARIDAN: testler gercek bir fotograf olmadan fikstur verebilsin. */
    private val readFields: suspend (photoPath: String, chain: String?) -> TagFields = ::readFieldsFromPhoto,
) : ViewModel() {

    private val household = DEFAULT_HOUSEHOLD_ID

    private val _state = MutableStateFlow(TagCaptureState())
    val state: StateFlow<TagCaptureState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // TEK ATISLIK okuma: cekim sirasinda zincir listesi degismiyor, ve
            // canli bir akis burada yalnizca kamera acikken veritabanini
            // dinlemek olurdu.
            _state.value = _state.value.copy(
                stores = storeDao.observeAll(household).first(),
                storeId = priceObservationDao.lastUsedStoreId(household),
            )
        }
    }

    fun selectStore(storeId: String) {
        _state.value = _state.value.copy(storeId = storeId)
    }

    /**
     * Kare cekildi - kart HEMEN aciliyor, OCR arkada kosuyor.
     *
     * Kartin beklememesi tasarimin sarti: kullanici deklansore basti, bir sey
     * gorunmek zorunda. Alanlar `reading = true` iken iskelet ciziliyor.
     */
    fun onCaptured(photoPath: String) {
        _state.value = _state.value.copy(card = ConfirmCard(photoPath = photoPath), failure = null)
        viewModelScope.launch {
            val chain = currentChain()
            val fields = runCatching { readFields(photoPath, chain) }.getOrNull()
            // KART DEGISTIYSE YAZMA. Kullanici bu arada vazgecip yeni bir kare
            // cekmis olabilir; geciken OCR sonucu yeni karti ezmemeli.
            val card = _state.value.card ?: return@launch
            if (card.photoPath != photoPath) return@launch
            _state.value = _state.value.copy(
                card = card.copy(
                    reading = false,
                    priceText = fields?.price?.let { minorToInput(it.minor) } ?: "",
                    productName = fields?.name?.name.orEmpty(),
                    brand = fields?.name?.brand,
                    kurusFromOcr = fields?.price?.kurusFromOcr == true,
                    skipped = fields?.skipped,
                ),
            )
        }
    }

    fun editPrice(text: String) = updateCard { it.copy(priceText = text) }

    fun editProductName(text: String) = updateCard { it.copy(productName = text) }

    /** Karti kapatir ve kareyi siler - saklanmayan fotograf saklanmamali. */
    fun dismissCard() {
        val path = _state.value.card?.photoPath
        _state.value = _state.value.copy(card = null)
        if (path != null) viewModelScope.launch { deleteFileAt(path) }
    }

    /**
     * Gozlemi yazar.
     *
     * @param onSaved toast metni - MUKERRER ILE YENI AYRI cumleler. Kullanici
     *   deklansore basti, bir sey olmali; ama "kaydedildi" demek yanlis olurdu.
     */
    fun save(onSaved: (String) -> Unit) {
        val card = _state.value.card ?: return
        val minor = parseMinorInput(card.priceText) ?: return
        if (!card.canSave || _state.value.saving) return
        _state.value = _state.value.copy(saving = true)
        viewModelScope.launch {
            val product = resolveProduct(
                repo = repo,
                catalogSeedDao = catalogSeedDao,
                householdId = household,
                name = card.productName.trim(),
            )
            val at = clock()
            val written = priceObservationDao.insertUnlessRecentDuplicate(
                PriceObservation(
                    id = newId(),
                    householdId = household,
                    productId = product.id,
                    storeId = _state.value.storeId,
                    unitPriceMinor = minor,
                    brand = card.brand?.trim()?.ifBlank { null },
                    observedAt = at,
                    createdAt = at,
                ),
            )
            deleteFileAt(card.photoPath)
            _state.value = _state.value.copy(card = null, saving = false)
            onSaved(if (written) "Fiyat kaydedildi" else "Aynı fiyat az önce kaydedilmişti")
        }
    }

    private fun currentChain(): String? {
        val id = _state.value.storeId ?: return null
        return _state.value.stores.firstOrNull { it.id == id }?.chain
    }

    private fun updateCard(block: (ConfirmCard) -> ConfirmCard) {
        val card = _state.value.card ?: return
        _state.value = _state.value.copy(card = block(card))
    }
}

/**
 * Fotograftan alanlara - gercek boru hatti.
 *
 * `downscaleForOcr` ONCE kosmak ZORUNDA: yonu piksele isliyor. Atlanirsa ML Kit
 * kareyi YAN goruyor ve sayfa dev satirlara cokuyor (F4.20'de fiste olculdu).
 *
 * `readTag`e EXIF gecilmiyor cunku hazirlanmis dosyada EXIF YOK - yon zaten
 * piksele islendi. O parametre yalnizca fikstur dokumu icin var.
 */
private suspend fun readFieldsFromPhoto(photoPath: String, chain: String?): TagFields {
    val staged = "$photoPath.ocr.jpg"
    val bytes = PlatformFile(photoPath).readBytes()
    val ok = downscaleForOcr(bytes, staged)
    val ocr = if (ok) readTag(staged) else readTag(photoPath)
    deleteFileAt(staged)
    return readTagFields(ocr, chain)
}

/** Kurusu duzenlenebilir metne cevirir: 3850 -> "38,50". */
internal fun minorToInput(minor: Long): String {
    val lira = minor / 100
    val kurus = (minor % 100).toInt()
    return "$lira,${kurus.toString().padStart(2, '0')}"
}
