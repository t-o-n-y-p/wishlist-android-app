# 05 — Cross-cutting pieces

Sources: `NetworkCallProcessor.kt`, `DtoMapper.kt`, `WizardCache.kt`,
`service/{AuthInterceptor,SessionIdInterceptor,UsersService,WishlistsService,GiftsService}.kt`,
`fragment/{extensions,constants}.kt`

## `NetworkCallProcessor` (@ActivityRetainedScoped)
- `suspend fun <T> call(block: suspend () -> Response<T>): Result<T>`:
  - runs on `Dispatchers.IO`, wrapped in `runCatching`.
  - **200** → `body()` (null body = `IOException`);
  - **401** → `cache.clear()` + `sharedPreferences.forceLogout()` + `throw SecurityException()`;
  - anything else → `IOException`.
- All business errors collapse to one generic "Error" state in ViewModels.

## Retrofit services (hand-written, thin; models come from `wishlist-api`)
| Service | Endpoints |
|---|---|
| `AuthService` | `POST auth/register`, `POST auth/login` |
| `UsersService` | `GET users` → `List<User>` |
| `WishlistsService` | `GET wishlists` (own), `POST wishlists`, `PUT/DELETE wishlists/{id}`, `GET wishlists/user/{userId}` |
| `GiftsService` | `PUT gifts/{id}`, `DELETE gifts/{id}`, `POST gifts/wishlist/{wishlistId}`, `PUT gifts/{id}/reserve` |
- All paths relative to `BuildConfig.BASE_URL`; the OpenAPI paths start with `/api/...`
  so the **base URL is expected to end with `/api/`**.
- Interceptors: `AuthInterceptor` adds `Authorization: Bearer <token>` (UserPreferences);
  `SessionIdInterceptor` adds `Session-ID: <sessionId>` (SessionPreferences). Both @Singleton.

## `DtoMapper` (@ActivityRetainedScoped) — DTO → UI item
- `Wishlist → WishlistsItem(id, title, description, gifts)`,
  `Gift → GiftsItem(id, name, description, price:Int, reserved:Boolean)`,
  `User → UsersItem(id, username)`.
- List overloads; `mapToUsersItem(List<User>, filter)` applies a predicate first.
- Nullables defensively collapsed via `orEmpty()` / `?: 0` / `?: false`.

## `WizardCache` (@ActivityRetainedScoped) — shared in-memory flow state
(The name "Wizard" = the users → wishlists → gifts navigation flow. No "Wishlist" typo fix.)
- Fields: `users: MutableList<UsersItem>`, `filteredUsers: List<UsersItem>`,
  `currentUser: UsersItem?`, `usernameFilter: String`, `wishlists: MutableList<WishlistsItem>`,
  `currentWishlist: WishlistsItem?`, `currentWishlistPosition: Int`,
  `currentGift: GiftsItem?`, `currentGiftPosition: Int`.
- `clear()` resets all above **except `filteredUsers`** (latent quirk); called on 401.
- Retained per activity → survives config changes; fragments read/write it instead of
  passing large objects as nav args.

## Fragment helpers (`fragment/extensions.kt`)
- `BottomSheetDialogFragment.dismissWithToast(resId)` — edit/create/filter screens are
  **bottom sheets**, confirmed by this + constants below.
- `Fragment.setFragmentResult(fragment, key, value)` — string wrapper over FragmentResult API.
- `Context.showErrorAlert()` — "error / something went wrong" MaterialAlertDialog.
- `Context.showConfirmationAlert(action)` — "confirmation / are you sure" with OK/Cancel.

## `fragment/constants.kt`
- `RECYCLER_VIEW_PAGE_SIZE = 10` (list page size; note: backend returns full lists,
  so this is used as a chunking hint in list UI).
- FragmentResult request keys: `FRAGMENT_WISHLISTS_EDIT`, `FRAGMENT_WISHLISTS_CREATE`,
  `FRAGMENT_GIFTS_EDIT`, `FRAGMENT_GIFTS_CREATE`, `FRAGMENT_USERS_FILTER`;
  result keys `RESULT`, `SUCCESS`.

## DI scopes summary
- Singleton: OkHttpClient, converters, all 4 Retrofit services, both interceptors,
  SharedPreferences.
- ActivityRetainedScoped: NetworkCallProcessor, DtoMapper, WizardCache
  (survive rotation, die with activity).
- ViewModelScoped: the `*UseCase` classes.
- HiltViewModel: ViewModels.
