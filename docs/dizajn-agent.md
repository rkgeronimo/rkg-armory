# Dizajn za agente

Primijenjeni izgled aplikacije. Boje žive u `app/src/main/java/hr/rkgarmory/app/ui/theme/Theme.kt`. Ovaj file kaže kad koju uzeti.

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

Status: `StatusChip` (`ui/StatusChip.kt`). Podloga je boja statusa na 16%, točka i tekst su boja statusa pomaknuta prema `onSurface`. Nema pune pilule sa svijetlim tekstom.

Označena stavka panela u tamnoj temi: `#334155`.

Plohe (svijetla / tamna):

| Uloga | Token | Hex |
| --- | --- | --- |
| Pozadina stranice | `surfaceVariant` | `#F3F6F8` / `#1E293B` |
| Kartica, polje, tipka | `surface` | `#FFFFFF` / `#111827` |
| Izbornik (`DropdownMenu`, `ExposedDropdownMenu`) | `surfaceContainer` | `#FFFFFF` / `#111827` |
| Pretraga, dijalog, krug praznog popisa, oznake | `surfaceContainerHigh` | `#E9EFF4` / `#273449` |
| Staza isključenog `Switch` | `surfaceContainerHighest` | `#E2E8F0` / `#334155` |

Token kojeg nema u `Theme.kt` ostaje zadani M3 (ljubičasto-sivi). Prije upotrebe ga postavi u obje sheme.

## Font

Plus Jakarta Sans u svim stilovima (`AppTypography` u `Theme.kt`). Jedan varijabilni file `res/font/plus_jakarta_sans.ttf`, težine 400, 500, 600 i 700. Licenca (OFL) je u `assets/licenses/`.

## Mjere

- Naslov zaslona 26sp, semibold.
- Dodir najmanje 48dp.
- Polje 56dp, radius 12dp. Gumbi obrasca (`FormActions`) 48dp, radius 12dp (vidi Gumbi).
- Pretraga iznad popisa: `SearchField`, 56dp, pilula na `surfaceContainerHigh`, obrub samo u fokusu.
- Filter: `FilterChips`, zasebni čipovi 48dp, radius 12dp, red se pomiče vodoravno. Označen: `primaryContainer` i kvačica. Ostali: `surface`, obrub 1dp `outline`. Uz `counts` broj stoji iza natpisa, svjetliji (na Rezervacijama: koliko ih po stanju odgovara pretrazi, i kad je 0).

Popis se osvježava povlačenjem prema dolje (`PullToRefreshBox`), i na Početnoj. Gumba Osvježi nema.

Podaci se učitavaju pri prijavi i nakon izdavanja, razduživanja i spremanja rezervacije (`StaffSession.refreshAll`). Spremanje opreme samo mijenja popis u `StaffSession`. Ulazak na ekran ne pita server, nego prikazuje popis iz `StaffSession`. Server pita samo kad tog popisa još nema.

## Traka

Ikone odredišta su iz `ui/AppIcons.kt` (vidi Ikone). Mobitel u portretu: donja traka s pet odredišta, naslov zaslona gore (osim na Početnoj, vidi dolje). Natpis za Razduživanje na traci je „Razduži“ (`barLabel`). Označeno odredište: pilula `primaryContainer`, ikona `onPrimaryContainer`. Tema i Odjava su u izborniku ikone računa gore desno. Traka se skriva dok je tipkovnica otvorena.

Tablet u pejzažu: panel fiksno lijevo, 220dp. Tablet u portretu i mobitel u pejzažu: hamburger otvara isti panel kao modal, širina 240dp.

Redoslijed: Početna, Izdavanje, Razduživanje, Rezervacije, Oprema, Inventura. Na mobitelu bez Inventure. Stavka panela 72dp. Dno panela: Tema, Odjava, 48dp. Označena stavka je svjetlija ploha.

Statusna traka: ispod nje je traka `surface`. Iznimka je Početna na mobitelu u portretu: tamo `AppShell` ne odmiče sadržaj od statusne trake (samo bočni i donji inseti), pa se fotografija zaglavlja nastavlja iza nje. `HomeHero` sam dodaje razmak statusne trake (`windowInsetsPadding(statusBars)`, na ostalim ljuskama 0 jer ga je ljuska već potrošila). Kad se sadržaj pomakne, iza statusne trake se pojavi `HomeHeader` (pun nakon 96dp skrolanja), da ispod bijelih ikona ne prolaze bijele kartice. Indikator povlačenja za osvježavanje je ispod statusne trake. Boju ikona postavlja `SystemBarIcons` (`ui/SystemBars.kt`): svijetle u tamnoj temi i iznad `HomeHeader`, inače tamne. Ikone navigacijske trake prate temu. Prijava: gore tamne ikone (svijetla voda), dolje prema listu (portret) ili tamnoj fotografiji (pejzaž).

Promjena odredišta: `Crossfade` 180 ms u `ShellContent`. Gornja traka i statusna traka mijenjaju se odmah.

## Ikone

`ui/AppIcons.kt`: tanke obrisne ikone iz odobrenog mockupa, iste SVG putanje (`addPathNodes`), mreža 24, potez 1.8, zaobljeni krajevi, bez ispune. Boju daje `Icon` (`tint` ili `LocalContentColor`). Početna `HomeIcon`, Izdavanje `IssueIcon`, Razduživanje `ReturnIcon`, Rezervacije `CalendarIcon`, Oprema `BoxIcon`, Inventura `ChecklistIcon`, Popis inventure `ListIcon`. Koriste ih traka i panel (`Destination`), kartice sažetka na Početnoj i prazni popisi. Ostale ikone (račun, tema, pretraga, kvačica) su Material `Icons.Rounded`.

## Prijava

Fotografija `login_bg.jpg` preko cijelog zaslona (portretni izrez u `res/drawable-nodpi`, pejzažni u `res/drawable-land-nodpi`, oba iz istog Unsplash originala), ispod nje statusna traka, preko nje okomiti tamni preljev. Portret: bijeli logo, naslov i podnaslov (sa sjenom) na fotografiji, obrazac u donjem listu `surface` (gore radius 28dp) koji se diže s tipkovnicom. Pejzaž: naslov i kartica obrasca jedno do drugog. Lozinka ima ikonu oka („Prikaži lozinku“ / „Sakrij lozinku“).

## Početna

- Zaglavlje: `HomeHeader` (`#0B3A5C`), isto u obje teme. Logo bijeli, moto kluba „Nevrijeme je naše vrijeme“ (`ClubMotto`, `headlineSmall`) bijeli bold, ispod datum („Petak, 27. studenoga“) bijeli 80%. Pozadina je podvodna fotografija s Prijave, izrezana u traku (`home_header.jpg`: `drawable-nodpi` 1080×730 za mobitel, `drawable-land-nodpi` i `drawable-sw600dp-nodpi` 1920×600 za široke zaslone), `ContentScale.Crop`. Preko nje tamnoplavi veo (`HeroVeil`, okomiti gradijent `HomeHeader`): 75% na vrhu (iza statusne trake, ispod bijelih ikona), 55% na 30%, 82% na 70%, 92% na dnu ispod kartica. Ne koristiti cijeli `login_bg` (1080×2424), da se na svakom ulasku na Početnu ne dekodira velika slika.
- Na mobitelu u portretu nema gornje trake: izbornik računa je bijel u zaglavlju. Ostale ljuske zadržavaju svoju traku.
- Dvije kartice sažetka (Izdavanje `IssueIcon`, Razduživanje `ReturnIcon`) prelaze donji rub zaglavlja. Pločica ikone je boja na 16%, ikona boja pomaknuta prema `onSurface`.
- Odjeljak: naslov i „Sve“, pa kartica s najviše 6 redaka, grupirano po izletu („Bez izleta“). Više od 6: „Prikaži sve (N)“. Cijeli redak otvara zahtjev.
- Redak: `Avatar` (`ui/Avatar.kt`), ime, strelica. Na Razduživanju `StatusChip` „N izlet/izleta vani“ (`excursionsOutLabel()`) kad je `excursionsWhileOut` > 0.

`Avatar`: krug 40dp, inicijali (najviše dva). Boja iz popisa prema imenu, podloga 18%, tekst kao u `StatusChip`. TalkBack ga preskače, ime stoji pored.

## Izdavanje, Razduživanje, Rezervacije

Popis je grupiran po izletu („Bez izleta“), redoslijed kako dolazi sa servera. Jedna kartica po izletu, redak po članu, razdjelnik `outlineVariant`.

- Izdavanje i Razduživanje: `RequestGroups` (`ui/RequestGroups.kt`). Naziv izleta iznad kartice. Redak najmanje 72dp: `Avatar`, ime, `FilledTonalButton` „Zaduži“ / „Razduži“ 48dp. Cijeli redak otvara zahtjev.
- Izdavanje: između pretrage i popisa `ListActionBar` (vidi Gumbi): „N zahtjev/zahtjeva“ po pretrazi i „+ Novo izdavanje“ (izdavanje bez rezervacije). Okruglog gumba + ispod popisa nema.
- Rezervacije na mobitelu: `LazyColumn`, `stickyHeader` s izletom i brojem na `surfaceVariant`. Redak: `Avatar`, ime, ispod šifre izdane opreme kao oznake (`FlowRow`, `surfaceContainerHigh`, radius 6dp) ili „Bez opreme“; desno `StatusChip` i donacija. Bez stranica.
- Rezervacije na tabletu: tablica, 20 po stranici (`PageBar`).

## Prazan popis

`EmptyState` (`ui/EmptyState.kt`): ikona 30dp u krugu 64dp `surfaceContainerHigh`, ispod tekst `bodyLarge` `onSurfaceVariant`, centrirano. Ikona je ukras, TalkBack čita samo tekst. Ikone (`AppIcons`): Izdavanje `IssueIcon`, Razduživanje `ReturnIcon`, Rezervacije `CalendarIcon`, Oprema `BoxIcon`. Pretraga bez pogodaka: `Icons.Rounded.SearchOff` i „Nema pronađenih“.

## Animacije popisa

Grupe Rezervacija (zaglavlje `stickyHeader` i kartica) i retci Opreme na mobitelu imaju `Modifier.animateItem()` (ključ je id ili izlet).

## Kućica

Krug, isti na Izdavanju i Opremi.

- Crtanje 28dp, dodir 48dp, oblik `extraLarge`.
- Prazno: prozirna ispuna, obrub 1dp `outline`.
- Označeno: ispuna `primary`, kvačica `onPrimary` 18dp.
- Na plavom retku (označena oprema na Izdavanju): prozirna ispuna, obrub bijeli 75%.
- Popunjena oprema na Izdavanju: ispuna `#1B7F4E` 24%, bez obruba, kvačica u boji teksta retka.

Dodir kućice označava. Dodir ostatka reda radi svoju radnju.

## Kartica

Kartica popisa u portretu nema sjenu. Obrub 1dp `outlineVariant`: `border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)`.

Kartica tablice i sažetka: sjena 1dp i isti obrub. Kartica obrasca (`FormCard`) je bez sjene, samo obrub. Kartica Prijave (pejzaž) ima sjenu 4dp.

Popis u portretu je kartica, u pejzažu tablica.

Sažetak na Izdavanju: kartica `surface`, zaglavlje u `primaryContainer`. U portretu je u donjem listu (vidi Izdavanje u portretu).

## Oprema

- Filtri Status i Vrsta: na tabletu dva reda `FilterChips`. Na mobitelu dva `FilterSelect` jedan do drugog, čip s izbornikom kao `FilterChips` (48dp, radius 12dp). Dok je „Sve“: naziv filtra, `surface`, obrub 1dp `outline`. Kad filtrira: odabrana vrijednost s kvačicom na `primaryContainer`. Desno strelica. Izbornik `DropdownMenu`, radius 12dp, `surfaceContainer`, odabrana stavka semibold s kvačicom (TalkBack čita označeno). TalkBack: padajući popis „Status: vrijednost“.
- Mobitel: iznad popisa fiksna traka (`PhoneListBar`, `ListActionBar`): lijevo broj po filtrima („N komad/komada · X izdano“), desno `FilledTonalButton` „+ Dodaj opremu“ 48dp, radius 12dp. Bez FAB-a, da ne pokriva retke. Ispod `PieceList`, `LazyColumn` bez stranica: jedan blok `surface`, prvi i zadnji redak zaobljeni 16dp, između razdjelnik `outlineVariant`.
- Tablet: gumb „Dodaj opremu“ uz pretragu, tablica 40 po stranici (`PageBar`).

Redak Opreme na mobitelu (`PieceRow`), najmanje 64dp, cijeli redak otvara komad:

1. lijevo `TypeTile`: 44dp, radius 12dp, `primaryContainer`, prva dva slova vrste velikim slovima. TalkBack ga preskače.
2. sredina: šifra (`titleMedium`, semibold), „vrsta · vel. N“ sivo, zadužio/la i datum (samo Izdano) medium, napomena `bodySmall` sivo, najviše 2 retka
3. desno `StatusChip`

Otvoren komad (`EquipmentForm`, vidi Obrazac): zaglavlje, Stanje i Napomena, `FormActions`, ispod kartica „Povijest“ (tko ga je imao, po rezervaciji, najnovija gore).

## Tablica u pejzažu

Red najmanje 64dp. Stupci: Šifra, Vrsta opreme, Veličina, Status, Zadužio/la, Datum izdavanja. Na stanju: osoba i datum prazni. Kućica lijevo, isti krug.

## Ikona aplikacije

Adaptivna ikona `mipmap-anydpi-v26/ic_launcher.xml`: bijela pozadina, sprijeda `logo_blue` uvučen 21% (`drawable/ic_launcher_foreground.xml`, ostaje u sigurnoj zoni 66 od 108dp), isti sloj kao `monochrome` (tematske ikone, Android 13+). Manifest: `icon` i `roundIcon` su `@mipmap/ic_launcher`. Android 12+ istu ikonu prikazuje na sustavskom splashu.

## Obrazac

Dijelovi su u `ui/FormParts.kt`. Obrazac je stupac kartica, razmak 12dp, rub 16dp, centriran, najviše 560dp (Oprema) ili 840dp (Rezervacija).

- `FormCard`: kartica `surface`, obrub 1dp `outlineVariant`, bez sjene, unutra 16dp.
- `SectionLabel`: naslov skupine polja, `titleSmall` semibold `onSurfaceVariant`, 8dp ispod.
- `ChoiceChip`: jedan izbor (stanje, vrsta, veličina, stanje vraćanja). Izgleda kao `FilterChips`: 48dp, radius 12dp, označen `primaryContainer` i kvačica, inače `surface` i obrub 1dp `outline`. TalkBack čita označeno. Dok se sprema je isključen.
- Polje: `OutlinedTextField`, radius 12dp, podloga `surface`.
- `FormActions`: zadnji red obrasca, Odustani i Spremi jednake širine (vidi Gumbi).
- Poruka greške ispod kartica, `error`, medium.

Detalji:

- Komad opreme (`EquipmentForm`): kartica zaglavlja (`TypeTile` 52dp, šifra `headlineSmall` bold, ispod „vrsta · vel. N“ sivo, desno `StatusPill`; kad je Izdano, ispod razdjelnika `Avatar` 32dp i „ime · datum“), pa kartica sa Stanjem (`ChoiceChip`) i Napomenom, pa `FormActions`. Ispod kartica „Povijest“ (`HistoryCard`): rezervacije na kojima je komad bio (`pieceHistory`, iz `StaffSession.cachedReservations`, bez novog poziva), najnovija gore. Zaglavlje „Povijest“ `titleSmall` semibold `onSurfaceVariant`, desno „N rezervacija/rezervacije“. Redak najmanje 60dp, razdjelnik `outlineVariant`: `Avatar` 36dp, ime `bodyLarge` semibold, ispod „izlet · #id“ sivo (jedan red), desno `StatusChip` za taj komad: „Izgubljeno“, „Vraćeno“, „Izdano“, inače stanje rezervacije. TalkBack čita redak kao jednu stavku. Arhivirane i obrisane rezervacije server ne šalje, pa ih nema. Za olovo kartice nema (rezervacija čuva kilograme, ne šifru).
- Novi komad (`EquipmentCreateForm`): kartica Vrsta, pa kartica Veličina, Šifra, Napomena. Promjena vrste briše veličinu.
- Rezervacija (`ReservationForm`): kartica zaglavlja (`Avatar` 48dp, ime, izlet, desno `StatusChip` stanja), kartica „Oprema“ (redak po komadu, razdjelnik `outlineVariant`, stanje vraćanja kao `ChoiceChip`), kartica s Donacijom, Napomenom i retkom „Arhiviraj“. Arhiviraj: cijeli redak (najmanje 56dp) je prekidač (`toggleable`, `Role.Switch`), desno `Switch` bez vlastitog klika.
- Razduživanje (`ReturnsScreen`): stupac kartica, najviše 720dp. Kartica zaglavlja (`Avatar` 48dp, ime, izlet, desno `StatusChip` `excursionsOutLabel()` kad je `excursionsWhileOut` > 0), pa jedna kartica-tablica bez sjene (`HeldPiecesCard`), da se i mnogo komada lako pregleda. Gore jednom nazivi stupaca (Oprema, Šifra, Vraćeno, Izgubljeno; `labelMedium` semibold `onSurfaceVariant`, „Izgubljeno“ `error`). Redak po komadu, najmanje 60dp, razdjelnik `outlineVariant`: naziv `bodyLarge` semibold (najviše 2 retka), ispod „vel. Y“ sivo; šifra kao oznaka (`surfaceContainerHigh`, radius 6dp); dva kruga oznake (`MarkCircle`) u stupcima 68dp. Nazivi stupaca su u jednom retku i s velikim fontom sustava se smanje da stanu (`BasicText` s `TextAutoSize.StepBased`), umjesto da se odrežu. Krug kao Kućica (crtanje 28dp, dodir 48dp, prazno obrub 1dp `outline`), označen: „Vraćeno“ `primary` s kvačicom `onPrimary`, „Izgubljeno“ `error` s križićem `onError`. Ponovni dodir označenog briše oznaku. TalkBack: potvrdni okvir „Vraćeno: naziv šifra“. Zadnji redak kartice je Donacija (polje 160dp, „€“). Ispod `FormActions`.
- Izdavanje (`IssuanceScreen`, `IssuanceEditor`): za rezervaciju iznad urednika jedan redak člana (`IssuanceMember`: `Avatar` 36dp, ime `titleMedium` semibold, „ · izlet“ sivo, jedan red), za novo izdavanje bez rezervacije `UserPicker`. Popunjena oprema: `#1B7F4E` na 16%, tekst i kvačica ta boja pomaknuta prema `onSurface`, bez obruba (i u kompaktnom prikazu). Označena: `primary`. Tražena (`WantedEquipment`): `#E0A800` na 22%, tekst ta boja pomaknuta 55% prema `onSurface`, obrub 1dp `outline`, čita se u obje teme. Prednost: popunjena, označena, tražena. Veličina: gumbi 56dp, radius 12dp, označen `primaryContainer`, inače `surface` i obrub 1dp `outline`, bez sjene. Tipke i polje šifre bez sjene.
- Izdavanje u portretu (`CompactIssuanceEditor`, i tablet u portretu): gore se pomiču čipovi opreme (tri u redu, 48dp). Dolje fiksno: polje šifre, veličina, tipkovnica (tipke 48dp) i `FormActions`, da nijedna tipka ne bude pod gumbima. Kad je editor niži od 600dp (mali mobitel, otvorena tipkovnica), polje, veličina i tipkovnica prelaze u pomični dio ispod čipova, fiksni ostaje samo `FormActions`. Sažetak nije na zaslonu: slobodna tipka ispod 7 je „Pregled“ (`primaryContainer`, `ListIcon` 20dp i tekst `labelLarge` semibold) i otvara `SummarySheet`, `ModalBottomSheet` bez djelomičnog stanja, podloga `surfaceVariant`, naslov „Pregled opreme“ (`titleMedium` semibold) i desno `TextButton` „Zatvori“, ispod sažetak. Zatvara se i povlačenjem dolje, dodirom izvan lista i tipkom Natrag. U pejzažu (`WideIssuanceEditor`) oprema je lijevo 320dp, desno polje, veličina i tipke 56dp pa sažetak. Pomiče se sve osim `FormActions`.
- Izdavanje, Razduživanje i Popis inventure imaju isti `FormActions`.

## Gumbi

- **Odustani**: `FilledTonalButton`, u `FormActions`. Odbacuje izmjenu, isključen dok se sprema.
- **Spremi** (na Izdavanju „Izdaj opremu“): `Button` `primary`, u `FormActions`. Siv (zadana isključena boja) dok nema izmjene ili obrazac nije valjan, kružić `onPrimary` dok se sprema. Oba 48dp, radius 12dp.
- **Dodaj** iznad popisa: `ListActionBar` (`ui/ListActionBar.kt`), fiksna traka iznad popisa: lijevo broj redaka sivo (`bodyMedium`), desno `FilledTonalButton` „+ natpis“ 48dp, radius 12dp. Na Opremi (mobitel) i Izdavanju. Ne pokriva retke, zato nema FAB-a ni okruglog gumba +.
