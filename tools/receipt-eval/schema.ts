/**
 * Structured output schema for Turkish grocery receipt extraction.
 *
 * Passed as output_config.format = { type: "json_schema", schema: RECEIPT_SCHEMA }.
 * NOT the deprecated top-level `output_format` parameter.
 *
 * Schema constraints imposed by the API (do not add these back):
 *   - every object needs additionalProperties: false and an explicit `required`
 *   - no numeric constraints (minimum/maximum/multipleOf)
 *   - no string constraints (minLength/maxLength/pattern)
 *   - no recursive schemas
 * Nullable fields use anyOf rather than a ["string","null"] type array.
 */

const nullableString = { anyOf: [{ type: "string" }, { type: "null" }] } as const;
const nullableNumber = { anyOf: [{ type: "number" }, { type: "null" }] } as const;

export const RECEIPT_SCHEMA = {
  type: "object",
  additionalProperties: false,
  required: [
    "merchantName",
    "merchantChain",
    "receiptDate",
    "receiptTime",
    "receiptNo",
    "lines",
    "totalRead",
    "kdvTotalRead",
    "legible",
  ],
  properties: {
    merchantName: {
      type: "string",
      description: "Fişin üstündeki mağaza adı, yazıldığı gibi.",
    },
    merchantChain: {
      type: "string",
      enum: ["BIM", "A101", "SOK", "MIGROS", "CARREFOUR", "TARIM_KREDI", "HAKMAR", "OTHER"],
      description: "Zincir. Emin değilsen OTHER.",
    },
    receiptDate: { ...nullableString, description: "ISO 8601 (YYYY-MM-DD). Okunamıyorsa null." },
    receiptTime: { ...nullableString, description: "HH:MM. Okunamıyorsa null." },
    receiptNo: { ...nullableString, description: "FİŞ NO. Okunamıyorsa null." },
    totalRead: { type: "number", description: "TOPLAM satırındaki tutar." },
    kdvTotalRead: { ...nullableNumber, description: "TOPKDV / KDV toplamı. Yoksa null." },
    legible: {
      type: "boolean",
      description: "Fiş bütün olarak okunabilir mi. Kadraj kesikse veya baskı silikse false.",
    },
    lines: {
      type: "array",
      description: "Fişteki her satır, yazıldığı sırayla. Hiçbir satırı atlama.",
      items: {
        type: "object",
        additionalProperties: false,
        required: ["rawText", "productName", "kind", "quantity", "unit", "unitPrice", "lineTotal", "confident"],
        properties: {
          rawText: {
            type: "string",
            description:
              "Satırın ürün/açıklama kısmı, fişte BASILDIĞI GİBİ, kısaltmalar açılmadan. Tutar bu alana girmez.",
          },
          productName: {
            type: "string",
            description:
              "rawText'in açılmış, normalleştirilmiş hali (örn. 'TM BGD EKMEK 500G' -> 'Tam Buğday Ekmek 500 g'). kind product değilse rawText ile aynı olabilir.",
          },
          kind: {
            type: "string",
            enum: ["product", "discount", "tax_summary", "total", "payment", "other"],
            description:
              "product: satın alınan kalem (POŞET dahil - parası ödenir). discount: İNDİRİM satırları. tax_summary: KDV/TOPKDV. total: TOPLAM. payment: NAKİT/KREDİ KARTI. other: MERSİS, EKÜ NO, Z NO, mali sembol vb.",
          },
          quantity: { type: "number", description: "Adet veya ağırlık. Belirtilmemişse 1." },
          unit: { type: "string", enum: ["adet", "kg", "lt", "paket"] },
          unitPrice: { ...nullableNumber, description: "Birim fiyat (TL/KG gibi). Yoksa null." },
          lineTotal: {
            type: "number",
            description:
              "Satırın tutarı. discount satırlarında NEGATİF yaz. tax_summary/other satırlarında 0.",
          },
          confident: {
            type: "boolean",
            description: "Bu satırı doğru okuduğundan emin misin. Şüphedeysen false.",
          },
        },
      },
    },
  },
} as const;
