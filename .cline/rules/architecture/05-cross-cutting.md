# 05 — Cross-cutting pieces

Reference files: `NetworkCallProcessor.kt`, `DtoMapper.kt`, `WizardCache.kt`,
`service/{AuthInterceptor,SessionIdInterceptor,UsersService,WishlistsService,GiftsService}.kt`,
`fragment/{extensions,constants}.kt`

## `NetworkCallProcessor` (@ActivityRetainedScoped)
- Route every Retrofit call through `suspend fun <T> call(block: suspend () ->
  Response<T>): Result<T>`; keep its behavior:
  - runs on `Dispatchers.IO`, wrapped in `runCatching`.
  - **200** → `body()` (null body = `IOException`);
  - **401** → `cache.clear()` + `sharedPreferences.forceLogout()` + `throw SecurityException()`;
  - anything else → `IOException`.
- Keep collapsing all business errors to one generic "Error" state in ViewModels.

## Retrofit services (hand-written, thin; models come from `wishlist-api`)
Keep the services and endpoints:
| Service | Endpoints |
|---|---|
| `AuthService` | `POST auth/register`, `POST auth/login` |
| `UsersService` | `GET users` → `List<User>` |
| `WishlistsService` | `GET wishlists` (own), `POST wishlists`, `PUT/DELETE wishlists/{id}`, `GET wishlists/user/{userId}` |
| `GiftsService` | `PUT gifts/{id}`, `DELETE gifts/{id}`, `POST gifts/wishlist/{wishlistId}`, `PUT gifts/{id}/reserve` |
- Keep all paths relative to `BuildConfig.BASE_URL`; the OpenAPI paths start with
  `/api/...`, so keep the **base URL ending with `/api/`**.
- Keep the interceptors: `AuthInterceptor` adds `Authorization: Bearer <token>`
  (UserPreferences); `SessionIdInterceptor` adds `Session-ID: <sessionId>`
  (SessionPreferences). Both are @Singleton.

## `DtoMapper` (@ActivityRetainedScoped) — DTO → UI item
- Keep the mappings: `Wishlist → WishlistsItem(id, title, description, gifts)`,
  `Gift → GiftsItem(id, name, description, price:Int, reserved:Boolean)`,
  `User → UsersItem(id, username)`.
- Keep the list overloads; `mapToUsersItem(List<User>, filter)` applies a predicate
  first.
- Keep collapsing nullables defensively via `orEmpty()` / `?: 0` / `?: false`.

## `WizardCache` (@ActivityRetainedScoped) — shared in-memory flow state
(The name "Wizard" = the users → wishlists → gifts navigation flow. No "Wishlist"
typo fix.)
- Keep the fields: `users: MutableList<UsersItem>`, `filteredUsers: List<UsersItem>`,
  `currentUser: UsersItem?`, `usernameFilter: String`, `wishlists:
  MutableList<WishlistsItem>`, `currentWishlist: WishlistsItem?`,
  `currentWishlistPosition: Int`, `currentGift: GiftsItem?`,
  `currentGiftPosition: Int`.
- Be aware: `clear()` resets everything **except `filteredUsers`** (latent quirk);
  `clear()` is called on 401. If a flow relies on `clear()` to reset the filtered
  list, reset it explicitly.
- Keep using the cache (retained per activity → survives configuration changes) for
  cross-screen flow data; fragments read/write it instead of passing large objects as
  nav args.

## Fragment helpers (`fragment/extensions.kt`)
- Keep `BottomSheetDialogFragment.dismissWithToast(resId)` — edit/create/filter screens
  are **bottom sheets**; implement new dialogs the same way.
- Keep `Fragment.setFragmentResult(fragment, key, value)` — the string wrapper over the
  FragmentResult API; use it for fragment → host communication.
- Keep `Context.showErrorAlert()` — the "error / something went wrong"
  MaterialAlertDialog.
- Keep `Context.showConfirmationAlert(action)` — the "confirmation / are you sure"
  with OK/Cancel.

## `fragment/constants.kt`
- Keep `RECYCLER_VIEW_PAGE_SIZE = 10` (list page size; note: the backend returns full
  lists, so this is used as a chunking hint in the list UI).
- Keep the FragmentResult request keys: `FRAGMENT_WISHLISTS_EDIT`,
  `FRAGMENT_WISHLISTS_CREATE`, `FRAGMENT_GIFTS_EDIT`, `FRAGMENT_GIFTS_CREATE`,
  `FRAGMENT_USERS_FILTER`; result keys `RESULT`, `SUCCESS`.

## DI scopes — follow when adding new classes
- Singleton: OkHttpClient, converters, all 4 Retrofit services, both interceptors,
  SharedPreferences.
- ActivityRetainedScoped: NetworkCallProcessor, DtoMapper, WizardCache
  (survive rotation, die with the activity).
- ViewModelScoped: the `*UseCase` classes.
- HiltViewModel: ViewModels.
