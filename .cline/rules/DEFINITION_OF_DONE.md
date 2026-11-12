# Definition of Done

Binding completion criteria for every task in this repository. A task is not done
until every gate below passes; verify by running the commands from the repository
root. When a task conflicts with a gate, fix the code — not the gate.

Reference files: `app/build.gradle.kts` (ktfmt plugin configuration),
`gradle/libs.versions.toml` (ktfmt plugin version),
`architecture/01-build-system.md` (build system rules).

## Gate 1 — Formatting (`:app`, ktfmt)
- All Kotlin code in the `:app` module must be formatted with the ktfmt plugin
  (`com.ncorti.ktfmt.gradle`, configured in `app/build.gradle.kts` with
  kotlinLangStyle and maxWidth 90): **zero violations**.
- Verify with `./gradlew :app:ktfmtCheck` — it must finish with
  `BUILD SUCCESSFUL`.
- Fix violations with `./gradlew :app:ktfmtFormat`, then re-run the check. Do not
  finish a task while the check reports violations.

## Gate 2 — Build (entire project)
- The Gradle build task must be successful across the entire project — both `:app`
  and `:wishlist-api`, all build types, including OpenAPI code generation,
  compilation, and the `check` tasks (lint): `./gradlew build` must finish with
  `BUILD SUCCESSFUL`.
