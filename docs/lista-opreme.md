# Lista opreme

Odobren izgled prozora: [lista-opreme.png](./lista-opreme.png).

Uzor: [Cheqroom](https://www.cheqroom.com/capabilities/mobile-app/) (Android). Popis komada, status, tko drži, sken šifre, izdavanje s retka.

Tablica samo na tabletu u pejzažu. Mobitel i tablet u portretu: kartica.

## Prozor

Zaglavlje, pa popis, pa na mobitelu **Dodaj opremu** na dnu.

```
Oprema                      tema    sken

[ traži........................ ]  [ Sve ] [ Na stanju ] [ Izdano ]  [ Dodaj opremu ]

[ Sve ] [ Odijelo ] [ Čizme ] [ Rukavice ] [ Pojasevi ]
```

**Dodaj opremu** je u zaglavlju na tabletu, na dnu samo na mobitelu.

Pretraga: šifra, vrsta, ime. Sken je gumb. Čipovi statusa i čipovi vrste filtriraju.

Kratak dodir označi red. Dugi pritisak (450ms) otvara odabir: broj, X, Odaberi sve / Ukloni sve.

## Kartica

Mobitel i tablet portret.

| Linija | Sadržaj |
| --- | --- |
| 1 | Šifra, vrsta, status desno |
| 2 | Veličina |
| 3 | Zadužio/la i datum, samo ako je Izdano |

## Tablica

Tablet pejzaž. Red najmanje 64px. Kacige nema.

| Stupac | Kad je Na stanju | Kad je Izdano |
| --- | --- | --- |
| Šifra | popunjena | popunjena |
| Vrsta opreme | popunjena | popunjena |
| Veličina | popunjena | popunjena |
| Status | Na stanju | Izdano |
| Zadužio/la | prazno | ime člana |
| Datum izdavanja | prazno | datum |

**Zadužio/la** je član koji drži komad. Piše se samo uz **Izdano**.

## Šifra

Odijelo, veličine 1–9. Prva dva znaka su veličina, ostatak je broj komada. `0512` je veličina 5, broj 12.

Čizmice, veličine 3–12. Šifra počinje s `B`. Sljedeća dva znaka su veličina, ostatak je broj komada. `B0811` je veličina 8, broj 11. Veličina 12 piše se `12`, npr. `B1204`.

Peraja, veličine `S`, `R`, `XL`. S početka je veličina, ostatak je broj, jedan ili dva znaka. `R1` je broj 1, `R11` je broj 11.

Kompenzator, veličine `XS`, `S`, `M`, `L`, `XL`, `XXL`. Veličina nije u šifri. Šifra počinje s `J`, ostatak je broj. `J12` je broj 12.

Rukavice, veličine `S`, `M`, `L`, `XL`. Šifra počinje s `G`, zatim veličina, ostatak je broj. `GM12` je veličina M, broj 12.

Regulator nema veličinu. Šifra je samo broj. `12` je broj 12.
