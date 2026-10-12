# rkg-armory

Evidencija opreme za izlete: tko ide, što je traženo, što je izdano, i što je na stanju.

## Language

**Komad**:
Jedan fizički predmet opreme, identificiran šifrom.
_Avoid_: artikal, stavka, item

**Šifra**:
Identifikator jednog komada. Nosi veličinu i broj tog komada. X zamjenjuje znamenku koja se s komada ne može pročitati. Čizmice počinju s B. Kompenzator počinje s J, slijedi veličina pa broj. Primjer: `JM08`. Rukavice počinju s G.
_Avoid_: kod, ID

**Broj komada**:
Redni broj komada unutar iste veličine. Dio šifre. Primjer: u `0512` i `M12` broj je 12.
_Avoid_: šifra, količina

**Vrsta opreme**:
Tip komada. Primjer: Odijelo.
_Avoid_: kategorija, naziv

**Veličina**:
Veličina komada, ovisi o vrsti. Odijelo 1–9, čizma 3–12, peraja S, R ili XL, kompenzator XS, S, M, L, XL ili XXL, rukavice S, M, L ili XL. Regulator je nema.
_Avoid_: broj, količina, debljina

**Debljina**:
Debljina odijela. `3mm`, `5mm` ili `7mm`. Ostale vrste je nemaju.
_Avoid_: veličina

**Tip regulatora**:
Marka regulatora. Apeks, Mares ili Scubapro. U šifri prefiks RA, RM ili RS.
_Avoid_: veličina, vrsta opreme

**Status**:
Gdje je komad. Na stanju ili Izdano.
_Avoid_: stanje

**Stanje**:
Kakvoća komada na inventuri odijela, peraja, čizmica, rukavica, kompenzatora i regulatora. Novo, Dobro, Za otpis ili Neispravno.
_Avoid_: status

**Izdavanje**:
Prozor na kojem se komad zadužuje članu.
_Avoid_: checkout, posudba

**Član**:
Osoba kluba. Može dobiti opremu i bez zahtjeva.
_Avoid_: sudionik, korisnik

**Zaduženje**:
Predaja komada jednom članu. Select nudi samo članove koji su zatražili opremu. Novo zaduženje može i za člana koji nije zatražio.
_Avoid_: posudba

**Razduživanje**:
Zatvaranje traženog komada koji ima šifru. Komad se vraća na stanje ili je izgubljen.
_Avoid_: check-in, odjava

**Izgubljeno**:
Komad koji se s izleta ne vraća.
_Avoid_: otpis, za otpis

**Inventura**:
Unos opreme koja se broji. Neke vrste zbrajaju količinu, neke dodaju redak. Popis stoji na tabletu.
_Avoid_: lista opreme, stocktake

**Popis**:
Svi retci na tabletu. Ostaje dok se redak ne makne.
_Avoid_: stanje

**Količina**:
Broj komada te vrste na inventuri. Vrijedi za olovo 1kg, olovo 2kg, pojas, masku, disalicu, kadicu i razno.
_Avoid_: veličina, šifra

**Razno**:
Količina s imenom koje sam upišeš.
_Avoid_: slobodno, free unos

**Napomena**:
Tekst uz razno. Ostale vrste je nemaju.
_Avoid_: komentar, bilješka

**Pojas**:
Pojas za utege. Uz olovo na zahtjevu se ne ispisuje. Podrazumijeva se.
_Avoid_: pojasevi

**Maska**:
Maska za ronjenje.

**Disalica**:
Cijev za disanje na površini.
_Avoid_: dišalica, snorkel

**Kadica**:
Posuda u kojoj se oprema ispire.

**Redak inventure**:
Jedan komad na inventuri, identificiran šifrom. Odijelo, peraje, kompenzator, regulator, čizmice ili rukavice. Svaki Unesi je novi redak.
_Avoid_: količina

**Izlet**:
Izlazak koji ima sudionike i zahtjeve za opremom. Na početnoj je jedan redak.
_Avoid_: trip, tura

**Sudionik**:
Osoba na izletu. Na početnoj se vidi samo broj.
_Avoid_: korisnik

**Zahtjev za opremom**:
Tražena oprema jednog člana. Na popisu piše ime člana i ime izleta. Može postojati i bez izleta. Tada piše samo ime člana.
_Avoid_: narudžba, rezervacija

**Olovo 1kg**:
Uteg od jednog kilograma.
_Avoid_: olo, uteg

**Olovo 2kg**:
Uteg od dva kilograma.
_Avoid_: olo, uteg
