# Dizajn

Primijenjeni izgled: [dizajn-agent.md](./dizajn-agent.md). Ovaj file je Lovable uzor.

Odobren prozor: [lista-opreme.png](./lista-opreme.png).

Lovable. Plavi pomak nije u ovome.

## Okvir

`ocean-shell`: puna visina (`min-h-dvh`), centrirano, `max-w-[1280px]`.

**Plitko more** (zadano): `#F6FBFB` → `#E6F4F4`. Plohe bijele, rub `oklch(0.817 0.036 202)`. Tekst `#14343A`.

**Dubina**: `#071820` → `#0E3A46`. Tekst `#E5F3F2`.

Tailwind v4, boje u oklch (`src/styles.css`). Sjena `--shadow-ocean`: color-mix dubokog tirkiza i `inset 0 1px 0`. Nema blura ni glow-a. Gumbi izdignuti.

| Uloga | Boja |
| --- | --- |
| Akcent, gumbi | `#1A7A86` |
| Na stanju | `#1E8A6E` |
| Izdano | `#D46A52` |

Tekst najmanje 16px. Naslov 26px, semibold. Dodir najmanje 48px.

## Sidebar

Uzor rasporeda: [sidebar.png](./sidebar.png). Lijeva traka pune visine, okomiti prijelaz. Boje ostaju morske, ne roze.

Šira od uzora. Tipke velike: puna širina trake, visina najmanje 72px, ikona i jedna riječ. Označena tipka je svjetlija ploha.

Na tabletu traka je uvijek tu, oko 220px. Na mobitelu nije fiksna uska traka: iste tipke, velike, ne jedu pola ekrana.

Tipke, odozgo: **Početna**, **Izdavanje**, **Razduživanje**, **Oprema**, **Inventura**.

**Početna** otvara popis izleta. Na retku su broj sudionika i broj zahtjeva za opremom. Vidi [pocetna.md](./pocetna.md).

**Razduživanje** otvara select člana koji drži opremu. Vidi [razduzivanje.md](./razduzivanje.md).

## Tri cjeline

1. Ljepljivo zaglavlje
2. Tablica ili kartice
3. Donja traka samo na mobitelu

### Zaglavlje

Ljepljivo (`sticky top-0 z-20`), obrub odozdo. Tri reda:

1. Lijevo **Oprema**. Desno dva kruga 48px: tema (sunce/mjesec), sken.
2. Pretraga visine 56px. Čipovi statusa visine 48px: Sve, Na stanju, Izdano. **Dodaj opremu** visine 56px, u ovom redu na tabletu, skriven na mobitelu.
3. Čipovi vrste, visina 48px, bez trake za pomicanje: Sve, Odijelo, Čizme, Rukavice, Pojasevi. Kacige nema.

### Odabir

Dugi pritisak 450ms. Traka ispod zaglavlja: broj odabranih, X, Odaberi sve / Ukloni sve. Kućica 48px na početku retka. Kratak dodir samo označi red.

### Sadržaj

Tablet pejzaž (`lg`): tablica u `ocean-panel`. Stupci: Šifra, Vrsta opreme, Veličina, Status, Zadužio/la, Datum izdavanja. Red najmanje 64px. Na stanju: osoba i datum prazni.

Mobitel i tablet portret: kartica najmanje 96px. Prva linija: šifra, vrsta, pilula desno. Dalje: veličina, a ako je izdano i osoba i datum.

Pilula: Na stanju `#1E8A6E`, Izdano `#D46A52`, svijetli tekst.

### Donja traka

Samo mobitel, fiksirana na dnu. **Dodaj opremu** preko cijele širine, visina 56px. Na tabletu je nema.

## Nije primijenjeno

Boja u plavo, ne u zeleno ni tirkiz. Akcent `#1A6BB5`, Na stanju `#2B7FD4`, Izdano ostaje `#D46A52`.
