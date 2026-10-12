# Inventura

Unos komada. Svaka šifra je jedan redak. Nema količine, pojaseva, oloa, maski ni dišalica.

## Prozor

Čipovi vrste gore, bez Sve: Odijelo, Čizmice, Peraje, Kompenzator, Rukavice, Regulator. Odabrana vrsta ostaje.

Pejzaž: lista desno. Portret: lista ispod unosa. Svi retci ostaju. Vide se zadnjih 5, najnoviji gore. Čip ne filtrira. Redak: šifra, veličina iz šifre, debljina, stanje. Ikona na retku miče taj redak.

Ispod toga, ili lijevo na pejzažu: stanje, pa za Odijelo debljina `3mm`, `5mm` ili `7mm`, pa za Regulator tip regulatora (Apeks `RA`, Mares `RM`, Scubapro `RS`), pa veličina samo za Peraje, Kompenzator i Rukavice, pa šifra s tipkovnicom kao na Izdavanju, pa **Unesi**. Prefiks ulazi u šifru: tip regulatora, `B` za čizmice, `J` za kompenzator, `G` za rukavice. Kompenzator bira veličinu, ali ona ne ulazi u šifru.

## Unesi

Brojevi na tipkovnici: odijelo i čizmice do 4, ostale vrste do 2. Peraje primaju jedan ili dva, npr. `1` i `11`.

Šifra prazna: gumb šuti. Slovne vrste traže veličinu. Odijelo traži debljinu. Regulator traži tip. Ista vrsta i ista šifra drugi put ne ulaze. `X` samo je pun unos. X u šifri zamjenjuje znamenku koja se ne čita. Veličina na retku je — ako se iz šifre ne čita. Debljina ostalih vrsta je —.

Stanje: Novo, Dobro, Za otpis, Neispravno. Jedno je označeno. Kreće na Dobro.

**Unesi** doda redak i počisti šifru, veličinu, debljinu i stanje. Vrsta ostaje. Stanje se vrati na Dobro.

Csv stupac `Debljina`. Odijelo piše `3mm`, `5mm` ili `7mm`. Ostale vrste prazno.

Redak koji se ne da čitati se makne. Ostali ostaju. Obavijest kaže koliko je maknuto.

Popis je dummy u memoriji. Nestane kad se aplikacija zatvori.
