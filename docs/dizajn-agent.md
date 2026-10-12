# Dizajn za agente

Primijenjeni izgled aplikacije. Boje žive u `app/src/main/java/hr/gearmory/app/ui/theme/Theme.kt`. Ovaj file kaže kad koju uzeti.

`docs/dizajn.md` je stari Lovable uzor (tirkiz, `ocean-shell`). Za boje, traku, kućicu i karticu slijedi ovaj file.

## Boje

Svijetla tema, bijela pozadina.

| Uloga | Token | Hex |
| --- | --- | --- |
| Akcent, označeno, rub kartice | `primary` | `#1A6BB5` |
| Podloga istaknute kartice | `primaryContainer` | `#D9EDF8` |
| Akcent u tamnoj temi | `primary` | `#7EBAE3` |
| Na stanju | `StatusInStock` | `#2B7FD4` |
| Izdano | `StatusIssued` | `#D46A52` |
| Sekundarni gumb (Odustani) | `secondary` | `#0E4E78` |

Pilula statusa: puna boja statusa, svijetli tekst.

Označena stavka trake u tamnoj temi: `#334155`.

## Mjere

- Naslov zaslona 26sp, semibold.
- Dodir najmanje 48dp.
- Polje i glavni gumb 56dp, radius 12dp.
- Čip 48dp, radius 12dp.

## Traka

Pejzaž: fiksna lijevo, 220dp. Portret: hamburger otvara isti panel kao modal, širina 240dp.

Redoslijed: Početna, Izdavanje, Razduživanje, Oprema, Inventura. Stavka 72dp. Dno: Tema, Odjava, 48dp. Označena stavka je svjetlija ploha.

Tema stoji na traci.

## Kućica

Krug, isti na Izdavanju, Razduživanju i Opremi.

- Crtanje 28dp, dodir 48dp, oblik `extraLarge`.
- Prazno: prozirna ispuna, obrub 1dp `outline`.
- Označeno: ispuna `primary`, kvačica `onPrimary` 18dp.
- Na plavom retku (Razduživanje): ispuna bijela 18% i obrub bijeli 75%.

Dodir kućice označava. Dodir ostatka reda radi svoju radnju.

## Kartica

Svaka kartica: sjena 4dp. `CardDefaults.cardElevation(defaultElevation = 4.dp)`.

Popis u portretu je kartica, u pejzažu tablica.

Kartica popisa u portretu (Oprema): bijela `surface`, visina najmanje 96dp. Razmak 10dp.

Sažetak na Izdavanju: ispuna `primaryContainer`, obrub 2dp `primary`, ista sjena 4dp.

Redak Opreme, sve u `bodyLarge`. Natpis sivi, vrijednost tamna medium. Tri linije:

1. lijevo natpis Šifra i šifra, sredina vrsta, desno pilula
2. natpis Veličina, veličina
3. zadužio/la i datum, samo ako je Izdano

## Tablica u pejzažu

Red najmanje 64dp. Stupci: Šifra, Vrsta opreme, Veličina, Status, Zadužio/la, Datum izdavanja. Na stanju: osoba i datum prazni. Kućica lijevo, isti krug.

## Gumbi

- **Odustani**: `secondary`, odbacuje izmjenu.
- **Spremi** i **Vraćeno**: `primary`.
- **Briši**: `error`, vidljiv tek kad je bar jedna kućica označena.
