# 01 — Build system

Reference files: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`,
`wishlist-api/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`,
`app/proguard-rules.pro`, `gradle/wrapper/gradle-wrapper.properties`

## Project layout
- Keep the root project name `Wishlist` with exactly two modules, `:app` and
  `:wishlist-api` (`settings.gradle.kts`).
- Declare repositories only in `settings.gradle.kts` (google(), mavenCentral()) — the
  project uses `FAIL_ON_PROJECT_REPOS`, so do not add repository blocks to module
  build scripts.
- Keep the Foojay toolchain resolver plugin 1.0.0; it provisions the JVM toolchain.

## Toolchain
- Keep the toolchain versions: Gradle 9.6.0 (wrapper), Kotlin 2.4.21, Android Gradle
  Plugin 9.4.1, Java 17. Upgrade the version catalog and the wrapper together.
- Keep the `gradle.properties` settings: `android.useAndroidX=true`,
  `android.nonTransitiveRClass=true`, `kotlin.code.style=official`, JVM args 2048m.

## Version catalog (`gradle/libs.versions.toml`)
- Add new dependencies and versions to the version catalog, not to build scripts.
  Key versions in use: hilt 2.60.1 (+ ksp 2.3.6), retrofit 3.0.0, coroutines 1.11.0,
  kotlinx-serialization 1.11.0, openapi-generator 7.26.0, navigation 2.10.2,
  material 1.14.0, recyclerview 1.4.0, core-splashscreen 1.2.0, uuid 0.8.4
  (com.benasher44), kotlinx-datetime 0.8.0, ktfmt plugin 0.27.0,
  secrets-gradle-plugin 2.0.1.

## `:app` module (`app/build.gradle.kts`)
- Keep namespace/applicationId `ru.otus.wishlist`, minSdk 27, target/compileSdk 37,
  versionCode 102 / versionName "1.2"; bump versions deliberately.
- Keep the plugin set: android.application, kotlinx.serialization, hilt, ksp, secrets,
  ktfmt.
- Keep the build features **viewBinding** and **buildConfig** enabled —
  `BuildConfig.BASE_URL` is injected from a local, uncommitted `secrets.properties` by
  the secrets plugin; do not hard-code the base URL.
- Do not enable minify in release builds; `proguard-rules.pro` is the default
  all-comment template (no custom rules).
- `:app` depends on `project(":wishlist-api")` — keep this direction; the API module
  must not depend on `:app`.
- Keep ktfmt configured with kotlinLangStyle, maxWidth 90 — format new code
  accordingly.
- There are no unit-test or instrumented-test source sets (only
  `testInstrumentationRunner` is declared). If you add tests, add the corresponding
  source sets.

## `:wishlist-api` module (`wishlist-api/build.gradle.kts`)
- Keep this module pure Kotlin/JVM (kotlin.jvm plugin), Java 17 target — do not add
  Android dependencies to it.
- Keep the codegen pipeline: the `org.openapi.generator` plugin's `openApiGenerate`
  task reads `wishlist-api/src/main/resources/wishlist.yaml`, generator `kotlin`,
  package root `ru.otus.wishlist.api` (subpackages `.api`, `.models`, `.invoker`).
- Keep the configOptions: `library=multiplatform`, `enumPropertyNaming=UPPERCASE`,
  `collectionType=list`; dates via kotlinx-datetime.
- Keep the generated sources wired into the main source set at
  `build/generate-resources/main/src/main/kotlin` and `compileKotlin dependsOn
  openApiGenerate`. Do not hand-edit generated code — change `wishlist.yaml` and
  regenerate.
- Keep `globalProperties`: `models=""` (model class names not suffixed),
  `modelDocs=false`.
- The module outputs `wishlist-api/build/libs/wishlist-api.jar`; the app uses only the
  *models* package (e.g. `ru.otus.wishlist.api.models.Login`, `LoginDto`,
  `UserRegistrationDto`).
