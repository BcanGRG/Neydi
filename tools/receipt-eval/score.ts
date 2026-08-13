/**
 * Skorlama — out/*.json cikarimlarini truth/*.json ile karsilastirir.
 *
 *   node score.ts
 *
 * F0.2'nin sarti: SATIR ADI dogrulugu ile FIYAT dogrulugu AYRI skorlanir.
 * Ad, fiyat karsilastirma ozelliginin ihtiyac duydugu ve basarisiz olmasi
 * beklenen alan; ikisini tek bir "dogruluk" sayisina karistirmak olcumu yok eder.
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(HERE, "out");
const TRUTH = path.join(HERE, "truth");

const PRICE_EPS = 0.005; // fiyat esitligi
const ARITH_EPS = 0.05; // tartili urun yuvarlamasi icin aritmetik toleransi

type Line = {
  rawText: string;
  productName: string;
  kind: string;
  quantity: number;
  unit: string;
  unitPrice: number | null;
  lineTotal: number;
  confident?: boolean;
};
type Receipt = {
  merchantChain: string;
  receiptDate: string | null;
  totalRead: number;
  lines: Line[];
  _meta?: Record<string, unknown>;
};

/** Turkce diakritikleri katla, sonra kucult. Locale'siz lowercase Turkce'de I/i'yi bozar. */
function fold(s: string): string {
  return s
    .replace(/[İIı]/g, "i")
    .replace(/[Şş]/g, "s")
    .replace(/[Ğğ]/g, "g")
    .replace(/[Üü]/g, "u")
    .replace(/[Öö]/g, "o")
    .replace(/[Çç]/g, "c")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

/** 0..1 token-overlap benzerligi (Jaccard). */
function sim(a: string, b: string): number {
  const A = new Set(fold(a).split(" ").filter(Boolean));
  const B = new Set(fold(b).split(" ").filter(Boolean));
  if (A.size === 0 && B.size === 0) return 1;
  let inter = 0;
  for (const t of A) if (B.has(t)) inter++;
  return inter / (A.size + B.size - inter);
}

const products = (r: Receipt) => r.lines.filter((l) => l.kind === "product");
const discounts = (r: Receipt) => r.lines.filter((l) => l.kind === "discount");

/** Cikarim satirlarini truth satirlarina aclik-gozlu esle (rawText benzerligine gore). */
function align(got: Line[], want: Line[]): Array<[Line, Line]> {
  const pairs: Array<[Line, Line, number]> = [];
  for (const g of got) for (const w of want) pairs.push([g, w, sim(g.rawText, w.rawText)]);
  pairs.sort((a, b) => b[2] - a[2]);
  const usedG = new Set<Line>();
  const usedW = new Set<Line>();
  const out: Array<[Line, Line]> = [];
  for (const [g, w, s] of pairs) {
    if (s < 0.34) break; // esik altinda eslesme sayilmaz
    if (usedG.has(g) || usedW.has(w)) continue;
    usedG.add(g);
    usedW.add(w);
    out.push([g, w]);
  }
  return out;
}

type Row = {
  file: string;
  chain: string;
  truthLines: number;
  gotLines: number;
  matched: number;
  rawExact: number;
  nameExact: number;
  priceExact: number;
  dateOk: boolean;
  totalOk: boolean;
  arithDelta: number;
  arithOk: boolean;
};

function scoreOne(file: string, got: Receipt, want: Receipt): Row {
  const gp = products(got);
  const wp = products(want);
  const matched = align(gp, wp);

  let rawExact = 0;
  let nameExact = 0;
  let priceExact = 0;
  for (const [g, w] of matched) {
    if (fold(g.rawText) === fold(w.rawText)) rawExact++;
    if (fold(g.productName) === fold(w.productName)) nameExact++;
    if (Math.abs(g.lineTotal - w.lineTotal) <= PRICE_EPS) priceExact++;
  }

  // F0.3 degismezi: urun toplami + indirimler (negatif) == TOPLAM, +/-0,05 TL.
  // KDV/TOPKDV satirlari toplamin DISINDA — fiyatlar zaten KDV dahil.
  const sum =
    gp.reduce((a, l) => a + l.lineTotal, 0) + discounts(got).reduce((a, l) => a + l.lineTotal, 0);
  const arithDelta = sum - got.totalRead;

  return {
    file,
    chain: want.merchantChain || got.merchantChain || "OTHER",
    truthLines: wp.length,
    gotLines: gp.length,
    matched: matched.length,
    rawExact,
    nameExact,
    priceExact,
    dateOk: (got.receiptDate ?? null) === (want.receiptDate ?? null),
    totalOk: Math.abs(got.totalRead - want.totalRead) <= PRICE_EPS,
    arithDelta,
    arithOk: Math.abs(arithDelta) <= ARITH_EPS,
  };
}

const pct = (n: number, d: number) => (d === 0 ? "  —  " : `${((n / d) * 100).toFixed(0).padStart(3)}%`);

function main() {
  if (!fs.existsSync(TRUTH)) {
    console.error(`truth/ yok. Once her fis icin bir dogruluk dosyasi olustur (README'ye bak).`);
    process.exit(1);
  }
  const rows: Row[] = [];
  for (const f of fs.readdirSync(OUT).filter((f) => f.endsWith(".json"))) {
    const truthFile = path.join(TRUTH, f);
    if (!fs.existsSync(truthFile)) {
      console.log(`atlandi (truth yok): ${f}`);
      continue;
    }
    const got = JSON.parse(fs.readFileSync(path.join(OUT, f), "utf8")) as Receipt;
    const want = JSON.parse(fs.readFileSync(truthFile, "utf8")) as Receipt;
    rows.push(scoreOne(f, got, want));
  }

  if (rows.length === 0) {
    console.error("Skorlanacak eslesen cift yok.");
    process.exit(1);
  }

  console.log("\n=== FIS BAZINDA ===");
  console.log("dosya                zincir      satir   eslesme  hamMetin      AD     FIYAT  aritmetik");
  for (const r of rows) {
    console.log(
      `${r.file.padEnd(20)} ${r.chain.padEnd(11)} ${String(r.gotLines).padStart(2)}/${String(r.truthLines).padEnd(3)} ` +
        `${pct(r.matched, r.truthLines)}    ${pct(r.rawExact, r.matched)}  ${pct(r.nameExact, r.matched)}  ` +
        `${pct(r.priceExact, r.matched)}   ${r.arithOk ? "OK" : `${r.arithDelta >= 0 ? "+" : ""}${r.arithDelta.toFixed(2)} TL`}`,
    );
  }

  console.log("\n=== ZINCIR BAZINDA ===");
  const chains = [...new Set(rows.map((r) => r.chain))].sort();
  for (const c of chains) {
    const rs = rows.filter((r) => r.chain === c);
    const s = (f: (r: Row) => number) => rs.reduce((a, r) => a + f(r), 0);
    console.log(
      `${c.padEnd(12)} n=${rs.length}  satir bulma ${pct(s((r) => r.matched), s((r) => r.truthLines))}  ` +
        `AD ${pct(s((r) => r.nameExact), s((r) => r.matched))}  ` +
        `FIYAT ${pct(s((r) => r.priceExact), s((r) => r.matched))}`,
    );
  }

  const tot = (f: (r: Row) => number) => rows.reduce((a, r) => a + f(r), 0);
  const M = tot((r) => r.matched);
  console.log("\n=== TOPLAM ===");
  console.log(`fis sayisi          ${rows.length}`);
  console.log(`satir bulma         ${pct(M, tot((r) => r.truthLines))}  (${M}/${tot((r) => r.truthLines)})`);
  console.log(`ham metin birebir   ${pct(tot((r) => r.rawExact), M)}`);
  console.log(`URUN ADI birebir    ${pct(tot((r) => r.nameExact), M)}   <-- fiyat karsilastirmasi buna bagli`);
  console.log(`FIYAT birebir       ${pct(tot((r) => r.priceExact), M)}`);
  console.log(`tarih dogru         ${pct(rows.filter((r) => r.dateOk).length, rows.length)}`);
  console.log(`TOPLAM dogru        ${pct(rows.filter((r) => r.totalOk).length, rows.length)}`);
  console.log(`aritmetik kapisi    ${pct(rows.filter((r) => r.arithOk).length, rows.length)}  (+/-${ARITH_EPS} TL)`);

  const nameRate = M === 0 ? 0 : tot((r) => r.nameExact) / M;
  console.log(
    `\nYORUM: ad dogrulugu ${(nameRate * 100).toFixed(0)}% — ` +
      (nameRate >= 0.8
        ? "fiyat takipcisi uygulanabilir gorunuyor (F0.5: fiyat takipcisi)."
        : nameRate >= 0.5
          ? "sinirda. ProductAlias ogrenmesi ve marketfiyati tohumlamasi sart (F0.4)."
          : "dusuk. F0.5'te harcama defterine daralmayi ciddi degerlendir."),
  );
  console.log("Not: az sayida fisle bu bir izlenimdir, olcum degil. Fis biriktikce tekrar calistir.");
}

main();
