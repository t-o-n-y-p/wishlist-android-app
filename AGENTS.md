# AGENTS.md

## Architecture rules (read first)
Before writing or modifying code in this repository, read `.cline/rules/ARCHITECTURE.md`
and follow the architecture rules defined there and in the numbered files under
`.cline/rules/architecture/`. The rules are binding: when a change conflicts with a
rule, update the rule together with the code. Do not duplicate those rules here.

## Definition of done
All tasks must comply with `.cline/rules/DEFINITION_OF_DONE.md`: a task is not
complete until every gate defined there passes. Do not duplicate the gates here.

## Project architecture overview

### Purpose
A mobile app for a "wishlist" service: users register and log in, manage their own
wishlists and the gifts inside them, browse other users' wishlists, and mark someone
else's gift as "reserved". The backend is a REST API described by the OpenAPI spec in
`wishlist-api/src/main/resources/wishlist.yaml`.

### Key directories
- `app/` — the Android application. Kotlin code under
  `app/src/main/java/ru/otus/wishlist/`, screen layouts and navigation graphs under
  `app/src/main/res/`.
- `wishlist-api/` — Kotlin/JVM module that generates API model classes from the
  OpenAPI spec at build time (no hand-written code).
- `gradle/` — Gradle wrapper and `libs.versions.toml` (the version catalog — the
  single place for dependency versions).
- `.cline/rules/` — architecture rules for working on this codebase.
- `build/`, `.gradle/`, `.idea/`, `.kotlin/` — build and IDE artifacts; ignore them.

### Entrypoints
- `app/src/main/AndroidManifest.xml` — declares the single Activity.
- `app/src/main/java/ru/otus/wishlist/MainActivity.kt` — the only Activity (Android's
  single full-screen window; the app shell).
- `app/src/main/java/ru/otus/wishlist/application.kt` — `WishlistApp` (Application
  subclass, Hilt DI bootstrap) plus the DI `Module` that builds the OkHttpClient and
  Retrofit services.
- The UI entry screen is the login fragment — the start destination of the
  `login_register` nav graph (`app/src/main/res/navigation/login_register.xml`).
- Build entrypoints: the Gradle Kotlin DSL scripts (`settings.gradle.kts`,
  `build.gradle.kts`, and each module's `build.gradle.kts`).

### Modules
- `:app` — the Android app module. MVVM-ish: Fragments (screens) observe ViewModels
  (sealed LiveData states), which call UseCases (`@ViewModelScoped`, return
  `Result<T>`), which wrap thin hand-written Retrofit service calls through
  `NetworkCallProcessor`. Hilt provides dependency injection.
- `:wishlist-api` — plain JVM module; code-generates DTO/model classes from
  `wishlist.yaml` (package `ru.otus.wishlist.api.models`). `:app` depends on it;
  never the reverse.

### Order of reading the code
1. `settings.gradle.kts`, `build.gradle.kts`, both modules' `build.gradle.kts`,
   `gradle/libs.versions.toml` — how the project builds.
2. `app/src/main/AndroidManifest.xml`, `application.kt`, `MainActivity.kt` — app
   bootstrap, DI, and session/login/logout wiring.
3. `app/src/main/res/navigation/*.xml` — the full screen map.
4. `fragment/login/` and `fragment/register/` — the canonical feature pattern
   (Fragment → ViewModel → UseCase → service) that every other feature repeats.
5. `NetworkCallProcessor.kt`, `service/` interceptors, `DtoMapper.kt`,
   `WizardCache.kt`, `storage/` — the shared plumbing between features.
6. `fragment/main/`, `fragment/tab/`, `fragment/users/`, `fragment/wishlists/`,
   `fragment/gifts/`, `recyclerview/` — feature screens and list rendering.
7. `wishlist-api/src/main/resources/wishlist.yaml` — the API contract.

File-level rules for every step above live in `.cline/rules/ARCHITECTURE.md` and the
numbered files under `.cline/rules/architecture/` — read those before making changes.
