# 02 — App bootstrap

Reference files: `app/src/main/AndroidManifest.xml`,
`.../ru/otus/wishlist/application.kt`, `MainActivity.kt`, `res/layout/activity_main.xml`,
`res/xml/network_security_config.xml`

## Manifest
- Keep the single permission `INTERNET`; add others only when a feature truly needs
  them.
- Keep `<application android:name=".WishlistApp">` — the Hilt entry
  (`@HiltAndroidApp class WishlistApp : Application()`).
- Keep exactly one activity: `.MainActivity` (exported, `MAIN`/`LAUNCHER`). The app has
  no deep links, no services/receivers/providers — do not introduce them casually.
- Keep the `networkSecurityConfig`: base = system CAs; **debug builds additionally
  trust user-installed CAs** (allows MITM proxying in debug only). Do not extend this
  to release builds.

## Activity layout (`activity_main.xml`)
- Keep the root `ConstraintLayout` (bg `?attr/tintColor`) containing a single
  full-screen `FragmentContainerView` id `fragment_container` hosting a
  `NavHostFragment`.
- Keep the activity-level nav graph **`@navigation/login_register`** — the auth flow is
  the entry point; the main app graph is a navigation destination inside it. Do not
  mount a different top-level graph.

## DI module (`application.kt`, `Module` @InstallIn(SingletonComponent))
- Keep the `SharedPreferences` name `"commonKeyValue"` (MODE_PRIVATE) provided as a
  singleton — existing session state is stored under it; renaming loses the session.
- Keep the `OkHttpClient` connect/call/read timeouts at 1 minute.
- Keep JSON via `Json.asConverterFactory("application/json")` (kotlinx + Jake Wharton
  converter).
- Keep four Retrofit services, all `baseUrl = BuildConfig.BASE_URL` (secrets plugin):
  - `AuthService` — client with **only `SessionIdInterceptor`** (no token; used
    pre-login).
  - `UsersService`, `WishlistsService`, `GiftsService` — client with
    **`AuthInterceptor` + `SessionIdInterceptor`**.
- Note: there is one `Retrofit` instance per service (per client config), not shared.
  When adding a service, create a client with exactly the interceptors it needs.

## `MainActivity` (single-activity app)
- Keep `@AndroidEntryPoint`; inject `SharedPreferences`.
- Keep the lifecycle setup: `enableEdgeToEdge()`, `installSplashScreen()`, transparent
  nav bar (SDK < 29 fallback sets color), ViewBinding (`ActivityMainBinding`), root
  padding from system-bar insets.
- Keep the session bootstrap: read the intent extra `"session_id"`; if absent,
  generate `uuid4()` (com.benasher44); persist via
  `sharedPreferences.put(SessionPreferences(...))`.
- Keep the login shortcut: if `UserPreferences.isLoggedIn()` → immediately
  `navController.navigate(R.id.go_to_app_main)` (skips the login screen).
- Keep the logout listener: an `OnSharedPreferenceChangeListener` watches the pref
  named `UserPreferences::class.simpleName`; on change, if `isNotLoggedIn()` →
  `navController.navigate(R.id.logout)`; if `logoutEvent == FORCE_LOGOUT` also show a
  MaterialAlertDialog ("you must log in again"). Any component writing
  `UserPreferences` triggers the global logout navigation — use this for logout, not
  direct navigation.
- Keep the back handling: a custom `onBackPressedDispatcher` callback — pop the back
  stack when non-empty; when empty, disable itself and defer to the system default
  (finishes).
