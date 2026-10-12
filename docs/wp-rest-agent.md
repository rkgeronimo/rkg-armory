# Uputa: REST za inventuru, zahtjeve i članove

Repo: https://github.com/rkgeronimo/wp-web

Napravi fork, granu s izmjene, pa PR na `main`. Samo čitanje. Ne diraj `admin-ajax.php`, shemu baze, ni postojeće admin stranice.

Plugin: `web/app/plugins/rkg-plugin`. Autoload: klasa `RKGeronimo\X` ide u `lib/X.php`. Komponente se pale u `lib/RKGeronimo.php`, metoda `initComponents`.

## Što dodati

Nova klasa `RKGeronimo\RestApi` u `lib/RestApi.php`. U `initComponents` dodaj `'RestApi'` uz `'Inventory'`. U `init()` zakači `rest_api_init`.

Namespace ruta: `rkg/v1`.

Svaka ruta ima `permission_callback` koji vraća `current_user_can('manage_equipment')`. Zabranjen je `__return_true` i `wp_ajax_nopriv_*`.

Odgovor je JSON. Upiti idu preko `$wpdb->prepare`. Ne vraćaj email, telefon, OIB, lozinku, `note`, ni ostali user meta.

## Rute

### `GET /wp-json/rkg/v1/inventory`

Stari popis komada. Tablica `$wpdb->prefix . 'rkg_inventory'`.

Stupci: `id`, `type`, `size`, `thickness`, `state`, `user_id`, `issue_date`.

Izostavi retke sa `state = 5` (`Definitions::EQUIPMENT_STATUS_DELETED`).

`state`: 0 Na stanju, 1 Izdano, 2 Neispravno, 3 Izgubljeno, 4 Otpisano, 5 Obrisano.

### `GET /wp-json/rkg/v1/gear-requests`

Zahtjevi za opremu na izletu. Tablica `$wpdb->prefix . 'rkg_excursion_gear'`.

Samo retci s `created >= '2026-09-01 00:00:00'`. Rujan 2026. ulazi. Listopad nije granica.

Izostavi `state = 3` (`Definitions::RESERVATION_STATUS_DELETED`).

Stupci: `id`, `user_id`, `post_id`, `created`, `state`, `mask`, `regulator`, `suit`, `boots`, `gloves`, `fins`, `bcd`, `lead`, `lead_size`, te `mask_returned`, `regulator_returned`, `suit_returned`, `boots_returned`, `gloves_returned`, `fins_returned`, `bcd_returned`, `lead_returned`.

`post_id` je id izleta. `user_id` je član.

### `GET /wp-json/rkg/v1/members`

WordPress korisnici s ulogom `member`.

Svaki objekt ima samo tri polja:

- `id` — WP user ID
- `first_name` — user meta `first_name`
- `last_name` — user meta `last_name`

Prazan meta ostaje prazan string. Ne sastavljaj ime iz emaila.

## Korisnik za tablet

Uloga `equipmentManager` (Oružar) već postoji i ima samo `manage_equipment`. Ne pravi novu ulogu. Ne daji ovom korisniku `god`, administratora ni `member`.

Pri aktivaciji plugina, ako korisnik ne postoji, napravi ga:

- login: `tablet`
- uloga: `equipmentManager`
- ime: Tablet
- email: `tablet@rkgeronimo.hr`

Lozinka se generira s `wp_generate_password`. Ne upisuj je u kod, README, migraciju ni PR.

Application password se ne može spremiti u git. U PR dodaj WP-CLI naredbu koju netko pokrene jednom na serveru i koja ispiše tajnu samo u terminal:

`wp rkg tablet-auth`

Naredba nađe korisnika `tablet`, napravi application password imena `ge-armory` i ispiše ga. Ako `ge-armory` već postoji, ne radi novi i ne ispisuje stari.

## Gotovo kad

Korisnik `tablet` postoji i ima samo ulogu `equipmentManager`. Nema ga u `/members`.

`curl -u tablet:application-password https://HOST/wp-json/rkg/v1/inventory` vrati JSON popis komada.

Isti poziv na `/gear-requests` vrati samo zahtjeve od 1.9.2026.

Isti poziv na `/members` vrati samo `id`, `first_name`, `last_name`.

Bez prijave sve tri rute vraćaju 401.

PR opis: što rute vraćaju, da je čitanje, da zahtjevi počinju 1.9.2026., i da se tablet javlja kao korisnik `tablet` nakon `wp rkg tablet-auth`.
