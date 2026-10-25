# Memory Bank — wishlist-android

Persistent fact base for this codebase. Each file below covers one stage of the
code-reading order; facts cite their source file paths. Update the status as
stages are completed.

| # | File | Topic | Status |
|---|------|-------|--------|
| 1 | `01-build-system.md` | Gradle modules, toolchain, versions, codegen | done |
| 2 | `02-app-bootstrap.md` | Manifest, Application, DI module, MainActivity | done |
| 3 | `03-navigation.md` | Nav graphs, screen map, tabs, routing actions | done |
| 4 | `04-feature-auth.md` | Login + register feature end-to-end | done |
| 5 | `05-cross-cutting.md` | Retrofit services, interceptors, mapper, cache, storage | done |
| 6 | `06-features.md` | Main UI, tabs, users/wishlists/gifts features, lists | done |
| 7 | `07-api-contract.md` | OpenAPI spec (wishlist.yaml) summary | done |

## Global facts
- Language: Kotlin; build: Gradle Kotlin DSL with a version catalog.
- Modules: `:app` (Android app) and `:wishlist-api` (Kotlin/JVM, code-generated from OpenAPI).
- Package root: `ru.otus.wishlist`.
- Architecture: MVVM-ish with Hilt DI; Fragment → ViewModel (LiveData sealed state) →
  UseCase (`@ViewModelScoped`, `Result<T>`) → Retrofit service; `NetworkCallProcessor`
  is the single HTTP gate (IO dispatcher, 200/401 handling, 401 → force logout).
- Cross-feature state lives in `WizardCache` (`@ActivityRetainedScoped`): the
  users → wishlists → gifts "wizard" flow reads/writes it instead of nav args.
- UI state pattern: screen-level 5-state `DataState` (NotSet/Loading/Content/Empty/Error)
  + per-row state LiveData embedded in mutable row data classes (`WishlistsItem`,
  `GiftsItem`) observed inside `onBind`.
- Edit/create dialogs are `BottomSheetDialogFragment`s that communicate back via
  the FragmentResult API (keys in `fragment/constants.kt`) and mutate the cache.
- "Own" vs "someone else's" mode is decided by `cache.currentUser == null`; it toggles
  create/edit/delete (own) vs the gift reserve switch (other).
- All API paths live under `/api/...`; base URL must end with `/api/`.

## Notable quirks / gotchas
- `WizardCache.clear()` does **not** reset `filteredUsers` (latent bug).
- Diff callbacks compare whole data classes (`areItemsTheSame = oldItem == newItem`) —
  effectively identity; updates rely on `notifyItemChanged(position)` / re-emit.
- `WishlistsItem`/`GiftsItem` embed `MutableLiveData` inside `data class`es (equality
  includes the LiveData field).
- Users list excludes the current user; username filter is **exact match**, client-side.
- `GiftsFragment` has no fetch of its own — gifts come from the parent `Wishlist` DTO.
- Logout = writing a blank-token `UserPreferences` to SharedPreferences; MainActivity
  reacts via a SharedPreferences change listener (also handles FORCE_LOGOUT from 401).
- `Session-ID` header is sent on every request (`SessionIdInterceptor`); the id is
  generated at app start and displayed on the login screen.
