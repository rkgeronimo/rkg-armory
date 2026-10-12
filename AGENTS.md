# AGENTS.md

Guidance for AI coding agents working in this repository.

## Project

Android app (Kotlin, Jetpack Compose, Material 3) for the RK Geronimo diving
club armory: reservations, issuing and returning equipment, the equipment list
and stocktaking. Domain language is in `CONTEXT.md`. Screen rules are in
`docs/dizajn-agent.md`.

The app talks to the WordPress backend over `wp-json/rkg/v1`. The client is
`app/src/main/java/hr/rkgarmory/app/remote/ReservationClient.kt`.

## Backend change log

The backend lives in the `wp-web` repository, checked out next to this one at
`../wp-web`. Every change to its API, data model, migrations or behaviour is
described in `../wp-web/docs/changes/`, one file per change, named
`YYYY-MM-DD-short-slug.md`.

- Before touching anything that calls the backend, read the entries in
  `../wp-web/docs/changes/` that are newer than the last one this app
  implemented. Follow their "Action required by other agents" section.
- Take routes, field names and status codes from those entries and from
  `../wp-web/web/app/plugins/rkg-plugin/lib/Api/`. Do not guess them.
- Do not edit files in `../wp-web/docs/changes/` from here. They are written
  by the backend agent.
- `docs/wp-rest-agent.md` in this repository is the original brief and is
  older than the change log. Where they disagree, the change log wins.

Last entry implemented in the app: `2026-10-12-reservation-archive-and-donation.md`.
Update this line when a newer entry is implemented.

## Build

Gradle needs a JDK 17. Android Studio's bundled one works:
`JAVA_HOME=C:\Program Files\Android\Android Studio\jbr`.

- Try changes with the debug build: `gradlew :app:assembleDebug`. Debug talks
  to the local backend (`10.0.2.2:8080` on the emulator).
- Release (`gradlew :app:assembleRelease`) talks to production and is signed
  with values from `keystore.properties`, which is not in git. Build it only
  when asked.

## Layout

A screen is a phone when `smallestScreenWidthDp < 600`. Phones get cards,
tablets get tables. Check both when changing a list.
