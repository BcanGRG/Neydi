/**
 * Fiş çıkarımı — receipts/ altındaki her görseli modele gönderir, out/ altına JSON yazar.
 *
 *   node extract.ts                      # varsayılan: claude-opus-5, thinking kapalı
 *   node extract.ts --model claude-sonnet-5
 *   node extract.ts --thinking           # adaptive thinking açık (maliyet/doğruluk karşılaştırması icin)
 *   node extract.ts --only a101.jpg
 */
import Anthropic from "@anthropic-ai/sdk";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { RECEIPT_SCHEMA } from "./schema.ts";
import { SYSTEM_PROMPT, USER_TEXT } from "./prompt.ts";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const RECEIPTS = path.join(HERE, "receipts");
const OUT = path.join(HERE, "out");

const MEDIA: Record<string, "image/jpeg" | "image/png" | "image/webp"> = {
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".png": "image/png",
  ".webp": "image/webp",
};

function arg(name: string): string | undefined {
  const i = process.argv.indexOf(`--${name}`);
  return i === -1 ? undefined : process.argv[i + 1];
}
const has = (name: string) => process.argv.includes(`--${name}`);

const model = arg("model") ?? "claude-opus-5";
const useThinking = has("thinking");
const only = arg("only");

// Claude Opus 5'te thinking VARSAYILAN OLARAK AÇIK. Kapatmak istiyorsak acikca
// disabled vermeliyiz — ve disabled yalnizca effort "high" ve altinda kabul edilir;
// xhigh/max ile birlikte 400 doner. Bu yuzden effort'u burada high'a sabitliyoruz.
const thinking = useThinking
  ? ({ type: "adaptive" } as const)
  : ({ type: "disabled" } as const);

const client = new Anthropic();

async function main() {
  fs.mkdirSync(OUT, { recursive: true });
  const files = fs
    .readdirSync(RECEIPTS)
    .filter((f) => MEDIA[path.extname(f).toLowerCase()])
    .filter((f) => !only || f === only);

  if (files.length === 0) {
    console.error(`receipts/ altinda gorsel yok (veya --only eslesmedi).`);
    console.error(`Desteklenen uzantilar: ${Object.keys(MEDIA).join(", ")}`);
    process.exit(1);
  }

  console.log(`model=${model}  thinking=${useThinking ? "adaptive" : "disabled"}  dosya=${files.length}\n`);

  let totalIn = 0;
  let totalOut = 0;

  for (const file of files) {
    const bytes = fs.readFileSync(path.join(RECEIPTS, file));
    const mediaType = MEDIA[path.extname(file).toLowerCase()]!;
    const started = Date.now();

    try {
      const response = await client.messages.create({
        model,
        max_tokens: 16000,
        thinking,
        // Structured outputs: output_config.format — deprecated `output_format` DEGIL.
        // Sema ilk kullanimda derlenir ve 24 saat cache'lenir; ayda 3 fiste cache
        // HER ZAMAN soguk olacak, yani bu gecikme uretimde de kalici.
        output_config: { effort: "high", format: { type: "json_schema", schema: RECEIPT_SCHEMA } },
        system: SYSTEM_PROMPT,
        messages: [
          {
            role: "user",
            content: [
              // Gorsel blok metinden ONCE gelmeli.
              { type: "image", source: { type: "base64", media_type: mediaType, data: bytes.toString("base64") } },
              { type: "text", text: USER_TEXT },
            ],
          },
        ],
      });

      const ms = Date.now() - started;
      totalIn += response.usage.input_tokens;
      totalOut += response.usage.output_tokens;

      if (response.stop_reason === "refusal") {
        console.error(`  ${file}  REDDEDILDI (${response.stop_details?.category ?? "kategori yok"})`);
        continue;
      }
      if (response.stop_reason === "max_tokens") {
        console.error(`  ${file}  UYARI: max_tokens'a carpti, JSON eksik olabilir`);
      }

      const textBlock = response.content.find((b) => b.type === "text");
      if (!textBlock || textBlock.type !== "text") {
        console.error(`  ${file}  metin blogu yok (stop_reason=${response.stop_reason})`);
        continue;
      }

      const parsed = JSON.parse(textBlock.text);
      parsed._meta = {
        model: response.model,
        thinking: useThinking ? "adaptive" : "disabled",
        input_tokens: response.usage.input_tokens,
        output_tokens: response.usage.output_tokens,
        latency_ms: ms,
      };

      const outFile = path.join(OUT, `${path.parse(file).name}.json`);
      fs.writeFileSync(outFile, JSON.stringify(parsed, null, 2), "utf8");

      const products = parsed.lines.filter((l: any) => l.kind === "product").length;
      console.log(
        `  ${file}  ${parsed.lines.length} satir (${products} urun)  ` +
          `${(ms / 1000).toFixed(1)}s  ${response.usage.input_tokens}/${response.usage.output_tokens} tok` +
          `${parsed.legible ? "" : "  [legible=false]"}`,
      );
    } catch (err) {
      if (err instanceof Anthropic.AuthenticationError) {
        console.error("\nKimlik dogrulama basarisiz. `ant auth login` calistir veya ANTHROPIC_API_KEY ayarla.");
        process.exit(1);
      }
      if (err instanceof Anthropic.RateLimitError) {
        console.error(`  ${file}  rate limit — biraz bekleyip tekrar dene`);
        continue;
      }
      if (err instanceof Anthropic.APIError) {
        console.error(`  ${file}  API hatasi ${err.status}: ${err.message}`);
        continue;
      }
      throw err;
    }
  }

  // Opus 5: $5/MTok girdi, $25/MTok cikti
  const cost = (totalIn / 1e6) * 5 + (totalOut / 1e6) * 25;
  console.log(`\ntoplam ${totalIn} girdi / ${totalOut} cikti token  ~$${cost.toFixed(4)} (opus-5 fiyatiyla)`);
  console.log(`ciktilar: ${OUT}`);
}

main();
