# Architecture Rules — wishlist-android

Binding architecture rules for this codebase. Each file in the `architecture/` folder
covers one stage of the recommended code-reading order; rules cite their source file
paths. Follow these rules while working on the project. When a change conflicts with a
rule, update the rule in this folder together with the code.

| # | File | Topic |
|---|------|-------|
| 1 | `architecture/01-build-system.md` | Gradle modules, toolchain, versions, codegen |
| 2 | `architecture/02-app-bootstrap.md` | Manifest, Application, DI module, MainActivity |
| 3 | `architecture/03-navigation.md` | Nav graphs, screen map, tabs, routing actions |
| 4 | `architecture/04-feature-auth.md` | Login + register feature end-to-end |
| 5 | `architecture/05-cross-cutting.md` | Retrofit services, interceptors, mapper, cache, storage |
| 6 | `architecture/06-features.md` | Main UI, tabs, users/wishlists/gifts features, lists |
| 7 | `architecture/07-api-contract.md` | OpenAPI spec (wishlist.yaml) |

## Global rules
- Write all application code in Kotlin; build with Gradle Kotlin DSL and declare
  dependencies and versions in the version catalog — do not pin versions inline.
- Keep the two-module layout: `:app` (Android app) and `:wishlist-api` (Kotlin/JVM,
  code-generated from OpenAPI). Do not add new Gradle modules or move packages across
  them.
- Keep the package root `ru.otus.wishlist`; put new app classes under it.
- Keep the MVVM-ish layering: Fragment → ViewModel (LiveData sealed state) →
  UseCase (`@ViewModelScoped`, returns `Result<T>`) → Retrofit service;
  `NetworkCallProcessor` is the single HTTP gate (IO dispatcher, 200/401 handling,
  401 → force logout). Do not skip a layer or call Retrofit outside of it.
- Keep cross-feature state in `WizardCache` (`@ActivityRetainedScoped`): the
  users → wishlists → gifts "wizard" flow must read/write it instead of passing data
  through nav arguments.
- Follow the UI state pattern: screen-level 5-state `DataState`
  (NotSet/Loading/Content/Empty/Error) — the single shared sealed class in
  `fragment/DataState.kt` — plus per-row state LiveData embedded in the mutable row
  data classes (`WishlistsItem`, `GiftsItem`) observed inside `onBind`. List
  ViewModels drive the shared `DataState` through its helpers (`mapToState` for the
  Empty/Content mapping, `fetchList` for Loading → load → Content/Empty with
  `Throwable` → Error); do not redefine `DataState` per ViewModel.
- Implement edit/create dialogs as `BottomSheetDialogFragment`s that communicate back
  via the FragmentResult API (keys in `fragment/constants.kt`) and mutate the cache.
- Keep the "own" vs "someone else's" mode decision in `cache.currentUser == null`: it
  toggles create/edit/delete (own) vs the gift reserve switch (other). Do not
  introduce a second mode mechanism.
- Keep all API paths under `/api/...` and ensure the base URL ends with `/api/`.

## Known pitfalls (do not repeat / be aware)
- `WizardCache.clear()` does **not** reset `filteredUsers` (latent bug): if a flow
  expects `clear()` to reset it, reset `filteredUsers` explicitly.
- Diff callbacks compare whole data classes (`areItemsTheSame = oldItem == newItem`) —
  effectively identity, so list updates rely on `notifyItemChanged(position)` / re-emit.
  Do not assume `submitList` alone updates mutated rows.
- `WishlistsItem`/`GiftsItem` embed `MutableLiveData` inside `data class`es (equality
  includes the LiveData field). Be careful when changing their fields or relying on
  their equality.
- The users list excludes the current user; the username filter is an **exact match**,
  client-side. Do not add server-side or substring filtering without updating these
  rules.
- `GiftsFragment` has no fetch of its own — gifts come from the parent `Wishlist` DTO.
  Do not add a separate gift-list endpoint call.
- Logout = writing a blank-token `UserPreferences` to SharedPreferences; MainActivity
  reacts via a SharedPreferences change listener (also handles FORCE_LOGOUT from 401).
  Trigger logout by writing the preference — do not navigate to login directly.
- The `Session-ID` header is sent on every request (`SessionIdInterceptor`); the id is
  generated at app start and displayed on the login screen. Keep this behavior when
  creating new Retrofit clients.
