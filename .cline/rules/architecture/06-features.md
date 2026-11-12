# 06 — Features (main UI, tabs, users, wishlists, gifts, lists)

Reference files: `fragment/main/*`, `fragment/tab/*`, `fragment/users/*`,
`fragment/users/filter/*` (+ wishlists/gifts/recyclerview — appended below)

## `AppMainFragment` (destination in the login_register graph)
- Keep hosting the **bottom-bar nav graph** `main_navigation` in a nested
  `NavHostFragment` (child fragment id `app_main_fragment_container`).
- Keep the bottom nav listener: on tab select → `viewModel.clearCache()` (i.e.
  **switching tabs wipes WizardCache**), then `navigate(tabId)` with `singleTop` +
  `popUpTo(startDestination, inclusive=false)`. Keep catching unknown tab id →
  `IllegalArgumentException`, listener returns false.
- Keep `AppMainFragmentViewModel` limited to wrapping `WizardCache.clear()`.

## Tab fragments
- Keep `MineTabFragment` / `UsersTabFragment`: each mounts its **own nested
  NavHostFragment** (`mine_tab_container` → `mine_tab_navigation`;
  `users_tab_container` → `users_tab_navigation`) and registers an
  `onBackPressedDispatcher` callback (viewLifecycleOwner-scoped): pop the tab back
  stack if non-empty, else disable itself and defer to system back. (Same idiom as
  MainActivity — reuse it.)
- Keep `ProfileTabFragment`: show `usernameText` from `UserPreferences` (only if
  logged in); `logOutButton` → `sharedPreferences.logout()` (MainActivity's pref
  listener performs the actual navigation to login) + `cache.wishlists.clear();
  cache.users.clear()`. (Note: a partial manual clear, not `cache.clear()`.)

## Users tab → `UsersFragment`
- Keep the layout `fragment_users` = 4 mutually exclusive groups switched by
  `DataState`: `usersLoading.loadingGroup` / `usersContent.contentGroup` /
  `usersError.errorGroup` / `usersNoResults.noResultsGroup` (NotSet/Loading/Content/
  Error/**Empty** — the list features add Empty to the auth 4-state pattern).
- Keep the data flow: `fillUsersFromCache` → if `cache.users` is empty,
  `loadUsersAndSaveToCache()`: `useCase.getAllUsers()` →
  `DtoMapper.mapToUsersItem(list) { user.username != myUsername }` — **the current
  user is excluded from the users list**. Cache the result once; cancel/restart the
  `refreshDataTask` Job on reload.
- Keep the client-side "pagination": initial `submitList(first min(size,
  RECYCLER_VIEW_PAGE_SIZE)=10)`; an `OnScrollListener` (provided by the ViewModel)
  grows the visible slice by 10 when the last visible item is reached and more cached
  items exist.
- Keep the filtering: `cache.usernameFilter` is applied with **exact equality**
  (`it == user.username`), not substring. Keep `clearFilterButton` visible when a
  filter is active.
- Keep the filter UI: top-app-bar menu `R.id.filter` → show `UsersFilterFragment`
  (BottomSheetDialogFragment) on `parentFragmentManager`; on Apply the sheet writes
  `cache.usernameFilter`, `dismissWithToast(filter_applied|filter_cleared)` and emits
  FragmentResult `(FRAGMENT_USERS_FILTER, RESULT=SUCCESS)`, which `UsersFragment`
  listens for to re-filter.
- Keep item click: `viewModel.saveCurrentUser(item)` — if a *different* user than
  before, reset `cache.wishlists = mutableListOf()` (so the next wishlists screen
  refetches); then `navigate(R.id.go_to_wishlists)`.

## `UsersFragmentUseCase`
- Keep a single method `getAllUsers(): Result<List<User>>` via `NetworkCallProcessor`.

## Wishlists (shared `WishlistsFragment` — own vs. user's)
- Keep the mode determined by **`cache.currentUser`**: null → "mine" (title `mine`,
  `addButton` visible); non-null → user's wishlists (title `wishlists_of_user` +
  username, add button hidden). Keep the same fragment instance in both tab graphs.
- Keep the data: `fillWishlistsFromCache` → if `cache.wishlists` is empty, fetch
  `getUserWishlists(currentUser.id)` if in user mode else `getAllWishlists()`, map
  via `DtoMapper`, cache, emit. Reuse the cached list until the cache is cleared (tab
  switch / new user selected / logout).
- Keep item actions: click → `saveCurrentWishlist(item, position)` →
  `navigate(go_to_gifts)`; edit → bottom sheet `WishlistsEditFragment`; delete →
  `showConfirmationAlert` → `deleteWishlistItem` (the per-item
  `WishlistsItem.deleteState` drives the row's action-group vs spinner visibility;
  success → Toast `wishlists_deleted`, item removed from `cache.wishlists`; empty →
  DataState.Empty).
- Keep the FragmentResult listeners: `FRAGMENT_WISHLISTS_EDIT` →
  `notifyItemChanged(position)`; `FRAGMENT_WISHLISTS_CREATE` →
  `fillWishlistsFromCache()` (the new item is already added to the cache by the
  sheet's ViewModel).
- Keep the same 5-state DataState + scroll-chunked `submitList(first 10)` as
  UsersFragment.

## `WishlistsEditFragment` (bottom sheet — create **and** edit)
- Keep the mode by `cache.currentWishlist` (null = create): edit pre-fills
  title/description and shows `wishlist_edit_title`; the create path calls
  `clearCurrentWishlist()` before showing it (from addButton).
- Keep the save behavior: edit → `updateWishlist(id, title, description)`, then
  mutate the cached item in place; create → `createWishlist` → map the response →
  `cache.wishlists.add(newItem)`.
- Keep the toasts `wishlists_updated`/`wishlists_created`; emit FragmentResult with
  key `FRAGMENT_WISHLISTS_EDIT` or `FRAGMENT_WISHLISTS_CREATE` (chosen by mode) — the
  host fragment listens on **both**.
- Be aware: edit mutates the in-place cached object rather than replacing it, and the
  `WishlistsItem` data class's `MutableLiveData` field makes `areItemsTheSame`
  (`oldItem == newItem`) effectively identity-based — diff callbacks are effectively
  no-ops (see below).

## Gifts (`GiftsFragment`)
- Keep the no-separate-fetch design: gifts come **inside the Wishlist DTO** —
  `cache.currentWishlist.gifts` (already mapped into `WishlistsItem.gifts`).
  `fillGiftsFromCache` just emits them (empty → DataState.Empty).
  `GiftsFragmentUseCase` has only `deleteGift` + `reserveGift` (no list endpoint
  exists).
- Keep the title: own wishlist → `wishlist.title`; user's → `wishlist_of_user_title`
  (user, wishlist) + add button hidden.
- Keep the row UI (see GiftsItemAdapter/ViewHolder): **edit / delete buttons are shown
  only in "own" mode** (`actionGroup.isVisible = getCurrentUser() == null`); in user
  mode a **`reserved` SwitchCompat** is shown instead (`reservedGroup` visible when
  `getCurrentUser() != null`) — that's the "reserve someone else's gift" interaction.
- Keep the switch toggle behavior: if the item is in Error state, just clear the
  state; otherwise `saveCurrentGift` + `reserveGiftItem(item)`: per-row
  `GiftsItem.operationState` (Loading hides the action/reserved groups, shows the
  spinner); success updates `item.reserved = response.reserved ?: false` + Toast
  (`gifts_status_change` in user mode); error → `showErrorAlert`-like row state.
- Keep delete: confirm → `deleteGiftItem` → remove from `cache.currentWishlist.gifts`
  (+DataState.Empty when the last one is removed).
- Keep the `GiftsEditFragment` bottom sheet: create/edit by `cache.currentGift`;
  create adds to `cache.currentWishlist.gifts`; edit mutates in place. Emit
  `FRAGMENT_GIFTS_EDIT` / `FRAGMENT_GIFTS_CREATE` results like the wishlist sheet.
- Keep the permission rules: **no wishlist create in user mode, no gift create in
  user mode** (add buttons hidden); also no delete/edit of others' wishlists (their
  edit/delete buttons hidden in the NotSet state — see onBind above).

## RecyclerView layer (`recyclerview/{users,wishlists,gifts}/`)
- Keep the plain `ListAdapter` + `ViewHolder` + `DiffUtil.ItemCallback` triplets,
  **no ViewBinding in holders** (raw `findViewById`).
- Keep the row layouts: `fragment_users_info` (username), `fragment_wishlists_info`
  (title/description + edit/delete buttons in `action_group`, spinner in
  `loading_group`), `fragment_gifts_info` (title/description/price + `reserved`
  SwitchCompat in `reserved_group`, edit/delete in `action_group`, spinner in
  `loading_group`).
- Keep adapters taking lambda params (click/edit/delete/onBind) so the Fragment owns
  all logic; `onBind` hands the fragment the `Group`s + switch to drive state
  visibility.
- Be aware the diff callbacks are buggy-by-design: `areItemsTheSame = oldItem ==
  newItem` (data-class equality — includes the embedded MutableLiveData) and
  `areContentsTheSame = id equality`. In practice `submitList` with the same list
  objects yields no partial updates; keep edits relying on explicit
  `notifyItemChanged(position)` / re-emit of the list.
- Keep `WishlistsItem`/`GiftsItem` as `data class`es with **mutable fields
  (`var title/price/reserved...`) and an embedded `MutableLiveData` state** — the
  "smart row items" pattern: the row observes its own item's state LiveData.
- Keep `UsersItem` a plain immutable DTO.
