# 01 — Build system

Sources: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`,
`wishlist-api/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`,
`app/proguard-rules.pro`, `gradle/wrapper/gradle-wrapper.properties`

## Project layout
- Root project name: `Wishlist`; modules: `:app`, `:wishlist-api` (`settings.gradle.kts`).
- Repositories centralized in `settings.gradle.kts`: google(), mavenCentral();
  `FAIL_ON_PROJECT_REPOS` mode (modules must not declare their own repos).
- Foojay toolchain resolver plugin 1.0.0 (JVM toolchain provisioning).

## Toolchain
- Gradle 9.6.0 (wrapper), Kotlin 2.4.21, Android Gradle Plugin 9.4.1, Java 17.
- `gradle.properties`: `android.useAndroidX=true`, `android.nonTransitiveRClass=true`,
  `kotlin.code.style=official`, JVM args 2048m.

## Version catalog (`gradle/libs.versions.toml`) — key versions
- hilt 2.60.1 (+ ksp 2.3.6), retrofit 3.0.0, coroutines 1.11.0,
  kotlinx-serialization 1.11.0, openapi-generator 7.26.0, navigation 2.10.2,
  material 1.14.0, recyclerview 1.4.0, core-splashscreen 1.2.0, uuid 0.8.4
  (com.benasher44), kotlinx-datetime 0.8.0, ktfmt plugin 0.27.0,
  secrets-gradle-plugin 2.0.1.

## `:app` module (`app/build.gradle.kts`)
- namespace/applicationId `ru.otus.wishlist`; minSdk 27, target/compileSdk 37;
  versionCode 102 / versionName "1.2".
- Plugins: android.application, kotlinx.serialization, hilt, ksp, secrets, ktfmt.
- Build features: **viewBinding** and **buildConfig** (so `BuildConfig.BASE_URL` exists —
  the secrets plugin injects it from a local `secrets.properties`, not committed).
- Release build: minify disabled; proguard-rules.pro is all-comment default template
  (no custom rules).
- Depends on `project(":wishlist-api")`.
- ktfmt: kotlinLangStyle, maxWidth 90.
- No unit-test or instrumented-test source sets are present (only
  `testInstrumentationRunner` declared).

## `:wishlist-api` module (`wishlist-api/build.gradle.kts`)
- Pure Kotlin/JVM (kotlin.jvm plugin), Java 17 target.
- **Codegen**: `org.openapi.generator` plugin; `openApiGenerate` task reads
  `wishlist-api/src/main/resources/wishlist.yaml`, generator `kotlin`,
  package root `ru.otus.wishlist.api` (subpackages `.api`, `.models`, `.invoker`).
- configOptions: `library=multiplatform`, `enumPropertyNaming=UPPERCASE`,
  `collectionType=list`; dates via kotlinx-datetime.
- Generated sources wired into the main source set at
  `build/generate-resources/main/src/main/kotlin`; `compileKotlin dependsOn openApiGenerate`.
- `globalProperties`: `models=""` (model class names not suffixed), `modelDocs=false`.
- Output: `wishlist-api/build/libs/wishlist-api.jar`; only the *models* package is used
  by the app (e.g. `ru.otus.wishlist.api.models.Login`, `LoginDto`, `UserRegistrationDto`).
