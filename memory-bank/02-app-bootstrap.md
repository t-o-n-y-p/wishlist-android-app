# 02 — App bootstrap

Sources: `app/src/main/AndroidManifest.xml`, `.../ru/otus/wishlist/application.kt`,
`MainActivity.kt`, `res/layout/activity_main.xml`, `res/xml/network_security_config.xml`

## Manifest
- Single permission: `INTERNET`.
- `<application android:name=".WishlistApp">` — Hilt entry (`@HiltAndroidApp class WishlistApp : Application()`).
- Exactly one activity: `.MainActivity` (exported, `MAIN`/`LAUNCHER`). No deep links,
  no services/receivers/providers.
- `networkSecurityConfig`: base = system CAs; **debug builds additionally trust
  user-installed CAs** (allows MITM proxying in debug only).

## Activity layout (`activity_main.xml`)
- Root `ConstraintLayout` (bg `?attr/tintColor`) containing a single full-screen
  `FragmentContainerView` id `fragment_container` hosting a `NavHostFragment`.
- The activity-level nav graph is **`@navigation/login_register`** — the auth flow is
  the entry point; the main app graph is a navigation destination inside it.

## DI module (`application.kt`, `Module` @InstallIn(SingletonComponent))
- `SharedPreferences` name `"commonKeyValue"` (MODE_PRIVATE) provided as singleton.
- `OkHttpClient`: connect/call/read timeouts = 1 minute.
- JSON via `Json.asConverterFactory("application/json")` (kotlinx + Jake Wharton converter).
- Four Retrofit services, all `baseUrl = BuildConfig.BASE_URL` (secrets plugin):
  - `AuthService` — client with **only `SessionIdInterceptor`** (no token; used pre-login).
  - `UsersService`, `WishlistsService`, `GiftsService` — client with **`AuthInterceptor` + `SessionIdInterceptor`**.
- Note: one `Retrofit` instance per service (per client config), not shared.

## `MainActivity` (single-activity app)
- `@AndroidEntryPoint`; injects `SharedPreferences`.
- Lifecycle setup: `enableEdgeToEdge()`, `installSplashScreen()`, transparent nav bar
  (SDK < 29 fallback sets color), ViewBinding (`ActivityMainBinding`), root padding
  from system-bar insets.
- **Session bootstrap**: reads intent extra `"session_id"`; if absent generates
  `uuid4()` (com.benasher44); persists via `sharedPreferences.put(SessionPreferences(...))`.
- **Login shortcut**: if `UserPreferences.isLoggedIn()` → immediately
  `navController.navigate(R.id.go_to_app_main)` (skips login screen).
- **Logout listener**: `OnSharedPreferenceChangeListener` watches the pref named
  `UserPreferences::class.simpleName`; on change, if `isNotLoggedIn()` →
  `navController.navigate(R.id.logout)`; if `logoutEvent == FORCE_LOGOUT` also shows a
  MaterialAlertDialog ("you must log in again"). I.e., any component deleting
  UserPreferences triggers global logout navigation.
- **Back handling**: custom `onBackPressedDispatcher` callback — pop back stack when
  non-empty; when empty, disables itself and defers to system default (finishes).
