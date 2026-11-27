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

## Font

Plus Jakarta Sans u svim stilovima (`AppTypography` u `Theme.kt`). Jedan varijabilni file `res/font/plus_jakarta_sans.ttf`, težine 400, 500, 600 i 700. Licenca (OFL) je u `assets/licenses/`.

## Mjere

- Naslov zaslona 26sp, semibold.
- Dodir najmanje 48dp.
- Polje i glavni gumb 56dp, radius 12dp. U obrascima (`FormActions`, polja Opreme i Rezervacije) 14dp.
- Pretraga iznad popisa: `SearchField`, 56dp, pilula na `surfaceContainerHigh`, obrub samo u fokusu.
- Filter: `FilterChips`, zasebni čipovi 48dp, radius 12dp, red se pomiče vodoravno. Označen: `primaryContainer` i kvačica. Ostali: `surface`, obrub 1dp `outline`. Uz `counts` broj stoji iza natpisa, svjetliji (na Rezervacijama: koliko ih po stanju odgovara pretrazi, i kad je 0).

Popis se osvježava povlačenjem prema dolje (`PullToRefreshBox`), i na Početnoj. Gumba Osvježi nema.

Podaci se učitavaju pri prijavi i nakon izdavanja, razduživanja i spremanja rezervacije (`StaffSession.refreshAll`). Spremanje opreme samo mijenja popis u `StaffSession`. Ulazak na ekran ne pita server, nego prikazuje popis iz `StaffSession`. Server pita samo kad tog popisa još nema.

## Traka

Ikone odredišta su iz `ui/AppIcons.kt` (vidi Ikone). Mobitel u portretu: donja traka s pet odredišta, naslov zaslona gore (osim na Početnoj, vidi dolje). Natpis za Razduživanje na traci je „Razduži“ (`barLabel`). Označeno odredište: pilula `primaryContainer`, ikona `onPrimaryContainer`. Tema i Odjava su u izborniku ikone računa gore desno. Traka se skriva dok je tipkovnica otvorena.

Tablet u pejzažu: panel fiksno lijevo, 220dp. Tablet u portretu i mobitel u pejzažu: hamburger otvara isti panel kao modal, širina 240dp.

Redoslijed: Početna, Izdavanje, Razduživanje, Rezervacije, Oprema, Inventura. Na mobitelu bez Inventure. Stavka panela 72dp. Dno panela: Tema, Odjava, 48dp. Označena stavka je svjetlija ploha.

Statusna traka: `AppShell` crta ispod nje traku `HomeHeader` dok je Početna na mobitelu u portretu, inače `surface`. Boju ikona postavlja `SystemBarIcons` (`ui/SystemBars.kt`): svijetle u tamnoj temi i iznad `HomeHeader`, inače tamne. Ikone navigacijske trake prate temu. Prijava: gore tamne ikone (svijetla voda), dolje prema listu (portret) ili tamnoj fotografiji (pejzaž).

Promjena odredišta: `Crossfade` 180 ms u `ShellContent`. Gornja traka i statusna traka mijenjaju se odmah.

## Ikone

`ui/AppIcons.kt`: tanke obrisne ikone iz odobrenog mockupa, iste SVG putanje (`addPathNodes`), mreža 24, potez 1.8, zaobljeni krajevi, bez ispune. Boju daje `Icon` (`tint` ili `LocalContentColor`). Početna `HomeIcon`, Izdavanje `IssueIcon`, Razduživanje `ReturnIcon`, Rezervacije `CalendarIcon`, Oprema `BoxIcon`, Inventura `ChecklistIcon`, Popis inventure `ListIcon`. Koriste ih traka i panel (`Destination`), kartice sažetka na Početnoj i prazni popisi. Ostale ikone (račun, tema, pretraga, kvačica) su Material `Icons.Rounded`.

## Prijava

Fotografija `login_bg.jpg` preko cijelog zaslona (portretni izrez u `res/drawable-nodpi`, pejzažni u `res/drawable-land-nodpi`, oba iz istog Unsplash originala), ispod nje statusna traka, preko nje okomiti tamni preljev. Portret: bijeli logo, naslov i podnaslov (sa sjenom) na fotografiji, obrazac u donjem listu `surface` (gore radius 28dp) koji se diže s tipkovnicom. Pejzaž: naslov i kartica obrasca jedno do drugog. Lozinka ima ikonu oka („Prikaži lozinku“ / „Sakrij lozinku“).

## Početna

- Zaglavlje: `HomeHeader` (`#0B3A5C`), isto u obje teme. Logo bijeli, pozdrav (Dobro jutro / Dobar dan / Dobra večer) bijeli bold, ispod datum („Petak, 27. studenoga“) bijeli 80%. Iza sadržaja, uz gornji desni kut, blijedi bijeli prsten (bijelo 6%, polumjer 96dp, debljina 28dp, središte 50dp od desnog i 60dp od gornjeg ruba), odrezan na rub zaglavlja (`drawBehind` + `clipToBounds`).
- Na mobitelu u portretu nema gornje trake: izbornik računa je bijel u zaglavlju. Ostale ljuske zadržavaju svoju traku.
- Dvije kartice sažetka (Izdavanje `IssueIcon`, Razduživanje `ReturnIcon`) prelaze donji rub zaglavlja. Pločica ikone je boja na 16%, ikona boja pomaknuta prema `onSurface`.
- Odjeljak: naslov i „Sve“, pa kartica s najviše 6 redaka, grupirano po izletu („Bez izleta“). Više od 6: „Prikaži sve (N)“. Cijeli redak otvara zahtjev.
- Redak: `Avatar` (`ui/Avatar.kt`), ime, strelica. Na Razduživanju `StatusChip` „N izlet/izleta vani“ kad je `excursionsWhileOut` > 0.

`Avatar`: krug 40dp, inicijali (najviše dva). Boja iz popisa prema imenu, podloga 18%, tekst kao u `StatusChip`. TalkBack ga preskače, ime stoji pored.

## Izdavanje, Razduživanje, Rezervacije

Popis je grupiran po izletu („Bez izleta“), redoslijed kako dolazi sa servera. Jedna kartica po izletu, redak po članu, razdjelnik `outlineVariant`.

- Izdavanje i Razduživanje: `RequestGroups` (`ui/RequestGroups.kt`). Naziv izleta iznad kartice. Redak najmanje 72dp: `Avatar`, ime, `FilledTonalButton` „Zaduži“ / „Razduži“ 48dp. Cijeli redak otvara zahtjev.
- Rezervacije na mobitelu: `LazyColumn`, `stickyHeader` s izletom i brojem na `surfaceVariant`. Redak: `Avatar`, ime, ispod šifre izdane opreme kao oznake (`FlowRow`, `surfaceContainerHigh`, radius 6dp) ili „Bez opreme“; desno `StatusChip` i donacija. Bez stranica.
- Rezervacije na tabletu: tablica, 20 po stranici (`PageBar`).

## Prazan popis

`EmptyState` (`ui/EmptyState.kt`): ikona 30dp u krugu 64dp `surfaceContainerHigh`, ispod tekst `bodyLarge` `onSurfaceVariant`, centrirano. Ikona je ukras, TalkBack čita samo tekst. Ikone (`AppIcons`): Izdavanje `IssueIcon`, Razduživanje `ReturnIcon`, Rezervacije `CalendarIcon`, Oprema `BoxIcon`. Pretraga bez pogodaka: `Icons.Rounded.SearchOff` i „Nema pronađenih“.

## Animacije popisa

Grupe Rezervacija (zaglavlje `stickyHeader` i kartica) i retci Opreme na mobitelu imaju `Modifier.animateItem()` (ključ je id ili izlet).

## Kućica

Krug, isti na Izdavanju, Razduživanju i Opremi.

- Crtanje 28dp, dodir 48dp, oblik `extraLarge`.
- Prazno: prozirna ispuna, obrub 1dp `outline`.
- Označeno: ispuna `primary`, kvačica `onPrimary` 18dp.
- Na plavom retku (Razduživanje): ispuna bijela 18% i obrub bijeli 75%.

Dodir kućice označava. Dodir ostatka reda radi svoju radnju.

## Kartica

Kartica popisa u portretu nema sjenu. Obrub 1dp `outlineVariant`: `border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)`.

Kartica tablice i sažetka: sjena 1dp i isti obrub. Kartica obrasca (`FormCard`) je bez sjene, samo obrub. Kartica Prijave (pejzaž) ima sjenu 4dp.

Popis u portretu je kartica, u pejzažu tablica.

Sažetak na Izdavanju: kartica `surface`, zaglavlje u `primaryContainer`.

## Oprema

- Mobitel: `PieceList`, `LazyColumn` bez stranica. Gore broj („N komad/komada · X izdano“), pa jedan blok `surface`, prvi i zadnji redak zaobljeni 16dp, između razdjelnik `outlineVariant`. Dodavanje: `ExtendedFloatingActionButton` „Dodaj opremu“ dolje desno, `primary`.
- Tablet: gumb „Dodaj opremu“ uz pretragu, tablica 40 po stranici (`PageBar`).

Redak Opreme na mobitelu (`PieceRow`), najmanje 64dp, cijeli redak otvara komad:

1. lijevo `TypeTile`: 44dp, radius 12dp, `primaryContainer`, prva dva slova vrste velikim slovima. TalkBack ga preskače.
2. sredina: šifra (`titleMedium`, semibold), „vrsta · vel. N“ sivo, zadužio/la i datum (samo Izdano) medium, napomena `bodySmall` sivo, najviše 2 retka
3. desno `StatusChip`

## Tablica u pejzažu

Red najmanje 64dp. Stupci: Šifra, Vrsta opreme, Veličina, Status, Zadužio/la, Datum izdavanja. Na stanju: osoba i datum prazni. Kućica lijevo, isti krug.

## Ikona aplikacije

Adaptivna ikona `mipmap-anydpi-v26/ic_launcher.xml`: bijela pozadina, sprijeda `logo_blue` uvučen 21% (`drawable/ic_launcher_foreground.xml`, ostaje u sigurnoj zoni 66 od 108dp), isti sloj kao `monochrome` (tematske ikone, Android 13+). Manifest: `icon` i `roundIcon` su `@mipmap/ic_launcher`. Android 12+ istu ikonu prikazuje na sustavskom splashu.

## Obrazac

Dijelovi su u `ui/FormParts.kt`. Obrazac je stupac kartica, razmak 12dp, rub 16dp, centriran, najviše 560dp (Oprema) ili 840dp (Rezervacija).

- `FormCard`: kartica `surface`, obrub 1dp `outlineVariant`, bez sjene, unutra 16dp.
- `SectionLabel`: naslov skupine polja, `titleSmall` semibold `onSurfaceVariant`, 8dp ispod.
- `ChoiceChip`: jedan izbor (stanje, vrsta, veličina, stanje vraćanja). Izgleda kao `FilterChips`: 48dp, radius 12dp, označen `primaryContainer` i kvačica, inače `surface` i obrub 1dp `outline`. TalkBack čita označeno. Dok se sprema je isključen.
- Polje: `OutlinedTextField`, radius 14dp, podloga `surface`.
- `FormActions`: zadnji red obrasca, Odustani i Spremi jednake širine (vidi Gumbi).
- Poruka greške ispod kartica, `error`, medium.

Detalji:

- Komad opreme (`EquipmentForm`): kartica zaglavlja (`TypeTile` 52dp, šifra `headlineSmall` bold, ispod „vrsta · vel. N“ sivo, desno `StatusPill`; kad je Izdano, ispod razdjelnika `Avatar` 32dp i „ime · datum“), pa kartica sa Stanjem (`ChoiceChip`) i Napomenom.
- Novi komad (`EquipmentCreateForm`): kartica Vrsta, pa kartica Veličina, Šifra, Napomena. Promjena vrste briše veličinu.
- Rezervacija (`ReservationForm`): kartica zaglavlja (`Avatar` 48dp, ime, izlet, desno `StatusChip` stanja), kartica „Oprema“ (redak po komadu, razdjelnik `outlineVariant`, stanje vraćanja kao `ChoiceChip`), kartica s Donacijom, Napomenom i retkom „Arhiviraj“. Arhiviraj: cijeli redak (najmanje 56dp) je prekidač (`toggleable`, `Role.Switch`), desno `Switch` bez vlastitog klika.
- Izdavanje, Razduživanje i Popis inventure imaju isti `FormActions`.

## Gumbi

- **Odustani**: `OutlinedButton`, obrub 1dp `outline`, u `FormActions`. Odbacuje izmjenu, isključen dok se sprema.
- **Spremi** (na Izdavanju „Izdaj opremu“): `Button` `primary`, u `FormActions`. Siv (zadana isključena boja) dok nema izmjene ili obrazac nije valjan, kružić `onPrimary` dok se sprema. Oba 56dp, radius 14dp.
- **Vraćeno**: `primary`.
- **Briši**: `error`, vidljiv tek kad je bar jedna kućica označena.
