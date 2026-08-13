# -*- coding: utf-8 -*-
"""Neydi gomulu Turk katalogunu Kotlin kaynagi olarak uretir."""
import io, os

KAT = [
    ("meyve-sebze",  "Meyve-Sebze",     0xFF6E8B3D),
    ("firin-ekmek",  "Firin-Ekmek",     0xFFB07A3C),
    ("sut-kahvalti", "Sut-Kahvaltilik", 0xFF4A7C8C),
    ("et-tavuk",     "Et-Tavuk-Balik",  0xFFA3453B),
    ("sarkuteri",    "Sarkuteri",       0xFF8C5A6B),
    ("dondurulmus",  "Dondurulmus",     0xFF5B7FA6),
    ("temel-gida",   "Temel Gida",      0xFF8A6D3B),
    ("konserve",     "Konserve-Salca",  0xFF9C5A2E),
    ("atistirmalik", "Atistirmalik",    0xFF7A5C9E),
    ("icecek",       "Icecek",          0xFF3F6B8C),
    ("temizlik",     "Temizlik",        0xFF4F7A6B),
    ("bakim",        "Kisisel Bakim",   0xFF7C6A8A),
]
KAT_AD = {
    "meyve-sebze": "Meyve-Sebze", "firin-ekmek": "F\u0131r\u0131n-Ekmek",
    "sut-kahvalti": "S\u00fct-Kahvalt\u0131l\u0131k", "et-tavuk": "Et-Tavuk-Bal\u0131k",
    "sarkuteri": "\u015eark\u00fcteri", "dondurulmus": "Dondurulmu\u015f",
    "temel-gida": "Temel G\u0131da", "konserve": "Konserve-Sal\u00e7a",
    "atistirmalik": "At\u0131\u015ft\u0131rmal\u0131k", "icecek": "\u0130\u00e7ecek",
    "temizlik": "Temizlik", "bakim": "Ki\u015fisel Bak\u0131m",
}

U = [
    ("Ekmek","firin-ekmek","adet"),("S\u00fct","sut-kahvalti","L"),("Yumurta","sut-kahvalti","adet"),
    ("\u00c7ay","icecek","g"),("Toz \u015eeker","temel-gida","kg"),("Un","temel-gida","kg"),
    ("Makarna","temel-gida","adet"),("Pirin\u00e7","temel-gida","kg"),("Ay\u00e7i\u00e7ek Ya\u011f\u0131","temel-gida","L"),
    ("Domates","meyve-sebze","kg"),("So\u011fan","meyve-sebze","kg"),("Patates","meyve-sebze","kg"),
    ("Beyaz Peynir","sut-kahvalti","g"),("Yo\u011furt","sut-kahvalti","kg"),("Tereya\u011f\u0131","sut-kahvalti","g"),
    ("Zeytin","sut-kahvalti","g"),("Domates Sal\u00e7as\u0131","konserve","g"),("Tuz","temel-gida","kg"),
    ("Bula\u015f\u0131k Deterjan\u0131","temizlik","L"),("Tuvalet K\u00e2\u011f\u0131d\u0131","temizlik","adet"),

    ("Salatal\u0131k","meyve-sebze","kg"),("Biber","meyve-sebze","kg"),("Patl\u0131can","meyve-sebze","kg"),
    ("Kabak","meyve-sebze","kg"),("Havu\u00e7","meyve-sebze","kg"),("Marul","meyve-sebze","adet"),
    ("Maydanoz","meyve-sebze","demet"),("Dereotu","meyve-sebze","demet"),("Nane","meyve-sebze","demet"),
    ("Roka","meyve-sebze","demet"),("Ispanak","meyve-sebze","kg"),("P\u0131rasa","meyve-sebze","kg"),
    ("Lahana","meyve-sebze","adet"),("Karnabahar","meyve-sebze","adet"),("Brokoli","meyve-sebze","adet"),
    ("Taze Fasulye","meyve-sebze","kg"),("Bezelye","meyve-sebze","kg"),("Bamya","meyve-sebze","kg"),
    ("Sar\u0131msak","meyve-sebze","adet"),("Limon","meyve-sebze","kg"),("Elma","meyve-sebze","kg"),
    ("Muz","meyve-sebze","kg"),("Portakal","meyve-sebze","kg"),("Mandalina","meyve-sebze","kg"),
    ("Armut","meyve-sebze","kg"),("\u00dcz\u00fcm","meyve-sebze","kg"),("Karpuz","meyve-sebze","adet"),
    ("Kavun","meyve-sebze","adet"),("\u015eeftali","meyve-sebze","kg"),("Kay\u0131s\u0131","meyve-sebze","kg"),
    ("Erik","meyve-sebze","kg"),("Kiraz","meyve-sebze","kg"),("\u00c7ilek","meyve-sebze","kg"),
    ("\u0130ncir","meyve-sebze","kg"),("Nar","meyve-sebze","kg"),("Avokado","meyve-sebze","adet"),
    ("Kivi","meyve-sebze","kg"),("Ananas","meyve-sebze","adet"),("Mantar","meyve-sebze","g"),
    ("Turp","meyve-sebze","demet"),("Kereviz","meyve-sebze","adet"),("Enginar","meyve-sebze","adet"),

    ("Tam Bu\u011fday Ekmek","firin-ekmek","adet"),("Tost Ekme\u011fi","firin-ekmek","adet"),
    ("Simit","firin-ekmek","adet"),("Po\u011fa\u00e7a","firin-ekmek","adet"),("A\u00e7ma","firin-ekmek","adet"),
    ("Lava\u015f","firin-ekmek","adet"),("Pide","firin-ekmek","adet"),("Yufka","firin-ekmek","adet"),
    ("Galeta Unu","firin-ekmek","g"),("Kruvasan","firin-ekmek","adet"),
    ("Baget Ekmek","firin-ekmek","adet"),("Kepekli Ekmek","firin-ekmek","adet"),

    ("Ka\u015far Peyniri","sut-kahvalti","g"),("Tulum Peyniri","sut-kahvalti","g"),
    ("Krem Peynir","sut-kahvalti","g"),("Labne","sut-kahvalti","g"),("Ayran","sut-kahvalti","L"),
    ("Kefir","sut-kahvalti","L"),("S\u00fczme Yo\u011furt","sut-kahvalti","kg"),("Kaymak","sut-kahvalti","g"),
    ("Bal","sut-kahvalti","g"),("Re\u00e7el","sut-kahvalti","g"),("Pekmez","sut-kahvalti","g"),
    ("Tahin","sut-kahvalti","g"),("F\u0131nd\u0131k Ezmesi","sut-kahvalti","g"),("Kakaolu Krema","sut-kahvalti","g"),
    ("Sucuk","sut-kahvalti","g"),("Ye\u015fil Zeytin","sut-kahvalti","g"),("Siyah Zeytin","sut-kahvalti","g"),
    ("Margarin","sut-kahvalti","g"),("\u00c7\u00f6kelek","sut-kahvalti","g"),("Lor Peyniri","sut-kahvalti","g"),

    ("Tavuk G\u00f6\u011fs\u00fc","et-tavuk","kg"),("Tavuk But","et-tavuk","kg"),("B\u00fct\u00fcn Tavuk","et-tavuk","adet"),
    ("K\u0131yma","et-tavuk","kg"),("Ku\u015fba\u015f\u0131 Et","et-tavuk","kg"),("Biftek","et-tavuk","kg"),
    ("Kuzu Pirzola","et-tavuk","kg"),("Tavuk Kanat","et-tavuk","kg"),("Hindi Eti","et-tavuk","kg"),
    ("Hamsi","et-tavuk","kg"),("Levrek","et-tavuk","kg"),("\u00c7ipura","et-tavuk","kg"),
    ("Somon","et-tavuk","kg"),("Karides","et-tavuk","g"),("Alabal\u0131k","et-tavuk","kg"),

    ("Salam","sarkuteri","g"),("Sosis","sarkuteri","g"),("Past\u0131rma","sarkuteri","g"),
    ("Jambon","sarkuteri","g"),("F\u00fcme Et","sarkuteri","g"),

    ("Dondurulmu\u015f Pizza","dondurulmus","adet"),("Patates K\u0131zartmas\u0131","dondurulmus","g"),
    ("Dondurma","dondurulmus","L"),("Dondurulmu\u015f Sebze","dondurulmus","g"),
    ("Milf\u00f6y Hamuru","dondurulmus","g"),("Mant\u0131","dondurulmus","g"),("B\u00f6rek","dondurulmus","g"),

    ("Zeytinya\u011f\u0131","temel-gida","L"),("M\u0131s\u0131r Ya\u011f\u0131","temel-gida","L"),("Bulgur","temel-gida","kg"),
    ("Mercimek","temel-gida","kg"),("Nohut","temel-gida","kg"),("Kuru Fasulye","temel-gida","kg"),
    ("Barbunya","temel-gida","kg"),("\u015eehriye","temel-gida","g"),("\u0130rmik","temel-gida","g"),
    ("Ni\u015fasta","temel-gida","g"),("Kabartma Tozu","temel-gida","adet"),("Vanilya","temel-gida","adet"),
    ("Maya","temel-gida","g"),("Sirke","temel-gida","L"),("Limon Suyu","temel-gida","L"),
    ("Karabiber","temel-gida","g"),("K\u0131rm\u0131z\u0131 Biber","temel-gida","g"),("Kimyon","temel-gida","g"),
    ("Kekik","temel-gida","g"),("Nane Kurusu","temel-gida","g"),("Pul Biber","temel-gida","g"),
    ("Tar\u00e7\u0131n","temel-gida","g"),("K\u00f6ri","temel-gida","g"),("Susam","temel-gida","g"),
    ("Kabak \u00c7ekirde\u011fi","temel-gida","g"),("Yulaf","temel-gida","g"),("M\u0131s\u0131r Gevre\u011fi","temel-gida","g"),
    ("Granola","temel-gida","g"),("Kusk\u00fcs","temel-gida","g"),("Kinoa","temel-gida","g"),
    ("Esmer \u015eeker","temel-gida","kg"),("Pudra \u015eekeri","temel-gida","g"),("Kakao","temel-gida","g"),
    ("Hindistan Cevizi","temel-gida","g"),

    ("Biber Sal\u00e7as\u0131","konserve","g"),("Ton Bal\u0131\u011f\u0131","konserve","adet"),("M\u0131s\u0131r Konservesi","konserve","adet"),
    ("Bezelye Konservesi","konserve","adet"),("Barbunya Konservesi","konserve","adet"),
    ("Tur\u015fu","konserve","g"),("Ket\u00e7ap","konserve","g"),("Mayonez","konserve","g"),
    ("Hardal","konserve","g"),("Soya Sosu","konserve","L"),("Domates P\u00fcresi","konserve","g"),
    ("Zeytin Ezmesi","konserve","g"),("Ac\u0131 Sos","konserve","g"),

    ("Bisk\u00fcvi","atistirmalik","g"),("\u00c7ikolata","atistirmalik","g"),("Gofret","atistirmalik","adet"),
    ("Kek","atistirmalik","adet"),("Kraker","atistirmalik","g"),("Cips","atistirmalik","g"),
    ("F\u0131nd\u0131k","atistirmalik","g"),("Ceviz","atistirmalik","g"),("Badem","atistirmalik","g"),
    ("Antep F\u0131st\u0131\u011f\u0131","atistirmalik","g"),("Leblebi","atistirmalik","g"),("Kuruyemi\u015f","atistirmalik","g"),
    ("Kuru \u00dcz\u00fcm","atistirmalik","g"),("Kuru Kay\u0131s\u0131","atistirmalik","g"),("Kuru \u0130ncir","atistirmalik","g"),
    ("Hurma","atistirmalik","g"),("Lokum","atistirmalik","g"),("Helva","atistirmalik","g"),
    ("Sak\u0131z","atistirmalik","adet"),("\u015eekerleme","atistirmalik","g"),("Puding","atistirmalik","adet"),
    ("S\u00fctla\u00e7","atistirmalik","adet"),("Patlam\u0131\u015f M\u0131s\u0131r","atistirmalik","g"),

    ("Su","icecek","L"),("Maden Suyu","icecek","adet"),("Kahve","icecek","g"),
    ("T\u00fcrk Kahvesi","icecek","g"),("Filtre Kahve","icecek","g"),("Meyve Suyu","icecek","L"),
    ("Kola","icecek","L"),("Gazoz","icecek","L"),("Soda","icecek","adet"),
    ("\u015ealgam","icecek","L"),("Limonata","icecek","L"),("Bitki \u00c7ay\u0131","icecek","adet"),
    ("Ye\u015fil \u00c7ay","icecek","g"),("S\u0131cak \u00c7ikolata","icecek","g"),("Enerji \u0130\u00e7ece\u011fi","icecek","adet"),

    ("\u00c7ama\u015f\u0131r Deterjan\u0131","temizlik","L"),
    ("Yumu\u015fat\u0131c\u0131","temizlik","L"),("\u00c7ama\u015f\u0131r Suyu","temizlik","L"),("Y\u00fczey Temizleyici","temizlik","L"),
    ("Cam Temizleyici","temizlik","L"),("Banyo Temizleyici","temizlik","L"),("Ovma Tozu","temizlik","g"),
    ("Bula\u015f\u0131k S\u00fcngeri","temizlik","adet"),("\u00c7\u00f6p Po\u015feti","temizlik","adet"),
    ("K\u00e2\u011f\u0131t Havlu","temizlik","adet"),("Pe\u00e7ete","temizlik","adet"),("Stre\u00e7 Film","temizlik","adet"),
    ("Al\u00fcminyum Folyo","temizlik","adet"),("Pi\u015firme K\u00e2\u011f\u0131d\u0131","temizlik","adet"),
    ("Bula\u015f\u0131k Makinesi Tableti","temizlik","adet"),("Parlat\u0131c\u0131","temizlik","L"),
    ("Oda Spreyi","temizlik","adet"),("B\u00f6cek \u0130lac\u0131","temizlik","adet"),("Lavabo A\u00e7\u0131c\u0131","temizlik","L"),

    ("\u015eampuan","bakim","L"),("Sa\u00e7 Kremi","bakim","L"),("Du\u015f Jeli","bakim","L"),
    ("Sabun","bakim","adet"),("Di\u015f Macunu","bakim","adet"),("Di\u015f F\u0131r\u00e7as\u0131","bakim","adet"),
    ("Deodorant","bakim","adet"),("Tra\u015f K\u00f6p\u00fc\u011f\u00fc","bakim","adet"),("Tra\u015f B\u0131\u00e7a\u011f\u0131","bakim","adet"),
    ("Nemlendirici","bakim","adet"),("G\u00fcne\u015f Kremi","bakim","adet"),("El Kremi","bakim","adet"),
    ("Islak Mendil","bakim","adet"),("Kulak \u00c7ubu\u011fu","bakim","adet"),("Ped","bakim","adet"),
    ("Bebek Bezi","bakim","adet"),("Bebek \u015eampuan\u0131","bakim","L"),("Sa\u00e7 Boyas\u0131","bakim","adet"),
    ("Oje","bakim","adet"),("Pamuk","bakim","g"),
]

# --- dogrulama ---
adlar = [a for a, _, _ in U]
dup = sorted({a for a in adlar if adlar.count(a) > 1})
assert not dup, f"tekrar eden urun: {dup}"
kat_ids = {k for k, _, _ in KAT}
bilinmeyen = sorted({k for _, k, _ in U if k not in kat_ids})
assert not bilinmeyen, f"bilinmeyen kategori: {bilinmeyen}"

# --- Kotlin uret ---
out = []
out.append("package com.neydi.app.data.catalog")
out.append("")
out.append("/**")
out.append(" * GOMULU TURK KATALOGU - URETILMIS DOSYA, ELLE DUZENLEME.")
out.append(" * Kaynak betik: tools/catalog/gen_catalog.py")
out.append(" *")
out.append(" * Bu uygulamanin TEK SOGUK-BASLANGIC MEKANIZMASI. Tek hane oldugu icin")
out.append(" * isbirlikci filtreleme yapacak baska kullanici verisi yok; ilk gezilerde")
out.append(" * uygulamanin bir sey bilmesinin baska yolu bulunmuyor.")
out.append(" *")
out.append(" * commonalityRank GLOBAL ve 1'den baslar (1 = en yaygin). Oneri sirasi ve")
out.append(" * yazarken tamamlama sirasi bundan cikiyor, kategori icinden degil - kullanici")
out.append(" * 'ek' yazdiginda once Ekmek gelmeli, alfabetik olarak once gelen bir sey degil.")
out.append(" */")
out.append("internal data class SeedKategori(")
out.append("    val id: String,")
out.append("    val ad: String,")
out.append("    /** MARKET GEZME sirasi, alfabetik DEGIL. Alfabetik siralamak insani")
out.append("     *  markette ileri geri yurutur. */")
out.append("    val sira: Int,")
out.append("    val tonArgb: Long,")
out.append(")")
out.append("")
out.append("internal data class SeedUrun(")
out.append("    val ad: String,")
out.append("    val kategoriId: String,")
out.append("    val yayginlik: Int,")
out.append("    val birim: String,")
out.append(")")
out.append("")
out.append(f"/** {len(KAT)} kategori, market gezme sirasinda. */")
out.append("internal val SEED_KATEGORILER: List<SeedKategori> = listOf(")
for i, (kid, _, ton) in enumerate(KAT):
    out.append(f'    SeedKategori("{kid}", "{KAT_AD[kid]}", {i}, 0x{ton:08X}L),')
out.append(")")
out.append("")
out.append(f"/** {len(U)} urun. matchKey KAYDEDILMIYOR - ekleme aninda matchKey() ile")
out.append(" *  turetiliyor ki normalizasyon kurali tek yerde kalsin (F2.4). */")
out.append("internal val SEED_URUNLER: List<SeedUrun> = listOf(")
for i, (ad, kat, birim) in enumerate(U, start=1):
    out.append(f'    SeedUrun("{ad}", "{kat}", {i}, "{birim}"),')
out.append(")")
out.append("")

hedef = os.path.join(
    r'C:\Users\buroc\AndroidStudioProjects\Neydi',
    'composeApp', 'src', 'commonMain', 'kotlin', 'com', 'neydi', 'app', 'data', 'catalog',
    'CatalogSeedData.kt')
os.makedirs(os.path.dirname(hedef), exist_ok=True)
io.open(hedef, 'w', encoding='utf-8', newline='\n').write('\n'.join(out))

print(f"kategori: {len(KAT)}")
print(f"urun:     {len(U)}")
print(f"yazildi:  {hedef}")
from collections import Counter
for k, n in Counter(k for _, k, _ in U).most_common():
    print(f"  {k:14} {n}")
