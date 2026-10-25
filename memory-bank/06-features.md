# 06 — Features (main UI, tabs, users, wishlists, gifts, lists)

Sources: `fragment/main/*`, `fragment/tab/*`, `fragment/users/*`, `fragment/users/filter/*`
(+ wishlists/gifts/recyclerview — appended below)

## `AppMainFragment` (destination in login_register graph)
- Hosts the **bottom-bar nav graph** `main_navigation` in a nested `NavHostFragment`
  (child fragment id `app_main_fragment_container`).
- Bottom nav listener: on tab select → `viewModel.clearCache()` (i.e. **switching tabs
  wipes WizardCache**) then `navigate(tabId)` with `singleTop` + `popUpTo(startDestination,
  inclusive=false)`. Unknown tab id → `IllegalArgumentException` caught, listener returns
  false.
- `AppMainFragmentViewModel` does nothing but wrap `WizardCache.clear()`.

## Tab fragments
- `MineTabFragment` / `UsersTabFragment`: each mounts its **own nested NavHostFragment**
  (`mine_tab_container` → `mine_tab_navigation`; `users_tab_container` →
  `users_tab_navigation`) and registers an `onBackPressedDispatcher` callback
  (viewLifecycleOwner-scoped): pop tab back stack if non-empty, else disable itself and
  defer to system back. (Same idiom as MainActivity.)
- `ProfileTabFragment`: shows `usernameText` from `UserPreferences` (only if logged in);
  `logOutButton` → `sharedPreferences.logout()` (MainActivity's pref listener performs
  the actual navigation to login) + `cache.wishlists.clear(); cache.users.clear()`.
  (Note: partial manual clear, not `cache.clear()`.)

## Users tab → `UsersFragment`
- Layout `fragment_users` = 4 mutually exclusive groups switched by `DataState`:
  `usersLoading.loadingGroup` / `usersContent.contentGroup` / `usersError.errorGroup` /
  `usersNoResults.noResultsGroup` (NotSet/Loading/Content/Error/**Empty** — the list
  features add Empty to the auth 4-state pattern).
- Data flow: `fillUsersFromCache` → if `cache.users` empty, `loadUsersAndSaveToCache()`:
  `useCase.getAllUsers()` → `DtoMapper.mapToUsersItem(list) { user.username !=
  myUsername }` — **the current user is excluded from the users list**. Result cached
  once; `refreshDataTask` Job cancelled/restarted on reload.
- Client-side "pagination": initial `submitList(first min(size, RECYCLER_VIEW_PAGE_SIZE)=10)`;
  an `OnScrollListener` (provided by the ViewModel) grows the visible slice by 10 when the
  last visible item is reached and more cached items exist.
- Filtering: `cache.usernameFilter` is applied with **exact equality**
  (`it == user.username`), not substring. `clearFilterButton` visible when filter active.
- Filter UI: top-app-bar menu `R.id.filter` → shows `UsersFilterFragment`
  (BottomSheetDialogFragment) on `parentFragmentManager`; on Apply the sheet writes
  `cache.usernameFilter`, `dismissWithToast(filter_applied|filter_cleared)` and emits
  FragmentResult `(FRAGMENT_USERS_FILTER, RESULT=SUCCESS)` which `UsersFragment` listens
  for to re-filter.
- Item click: `viewModel.saveCurrentUser(item)` — if a *different* user than before,
  resets `cache.wishlists = mutableListOf()` (so the next wishlists screen refetches);
  then `navigate(R.id.go_to_wishlists)`.

## `UsersFragmentUseCase`
- Single method `getAllUsers(): Result<List<User>>` via `NetworkCallProcessor`.

## Wishlists (shared `WishlistsFragment` — own vs. user's)
- Mode determined by **`cache.currentUser`**: null → "mine" (title `mine`,
  `addButton` visible); non-null → user's wishlists (title `wishlists_of_user` + username,
  add button hidden). Same fragment instance in both tab graphs.
- Data: `fillWishlistsFromCache` → if `cache.wishlists` empty, fetch
  `getUserWishlists(currentUser.id)` if in user mode else `getAllWishlists()`, map via
  `DtoMapper`, cache, emit. Cached lists are then reused until cache cleared (tab switch /
  new user selected / logout).
- Item actions: click → `saveCurrentWishlist(item, position)` → `navigate(go_to_gifts)`;
  edit → bottom sheet `WishlistsEditFragment`; delete → `showConfirmationAlert` →
  `deleteWishlistItem` (per-item `WishlistsItem.deleteState` drives the row's
  action-group vs spinner visibility; success → Toast `wishlists_deleted`, item removed
  from `cache.wishlists`; empty → DataState.Empty).
- FragmentResult listeners: `FRAGMENT_WISHLISTS_EDIT` → `notifyItemChanged(position)`;
  `FRAGMENT_WISHLISTS_CREATE` → `fillWishlistsFromCache()` (new item already added to
  cache by the sheet's ViewModel).
- Same 5-state DataState + scroll-chunked `submitList(first 10)` as UsersFragment.

## `WishlistsEditFragment` (bottom sheet — create **and** edit)
- Mode by `cache.currentWishlist` (null = create): edit pre-fills title/description and
  shows `wishlist_edit_title`; create path `clearCurrentWishlist()` was called before
  showing it (from addButton).
- On save: edit → `updateWishlist(id, title, description)`, then mutates the cached item
  in place; create → `createWishlist` → maps response → `cache.wishlists.add(newItem)`.
- Toast `wishlists_updated`/`wishlists_created`; emits FragmentResult with key
  `FRAGMENT_WISHLISTS_EDIT` or `FRAGMENT_WISHLISTS_CREATE` (chosen by mode) — the
  host fragment listens on **both**.
- Note: edit mutates the in-place cached object rather than replacing it, and the
  `WishlistsItem` data class's `MutableLiveData` field makes `areItemsTheSame`
  (`oldItem == newItem`) effectively identity-based — diff callbacks are
  effectively no-ops (see below).

## Gifts (`GiftsFragment`)
- No separate fetch: gifts come **inside the Wishlist DTO** — `cache.currentWishlist.gifts`
  (already mapped into `WishlistsItem.gifts`). `fillGiftsFromCache` just emits them
  (empty → DataState.Empty). `GiftsFragmentUseCase` has only `deleteGift` +
  `reserveGift` (no list endpoint exists).
- Title: own wishlist → `wishlist.title`; user's → `wishlist_of_user_title` (user,
  wishlist) + add button hidden.
- Row UI (see GiftsItemAdapter/ViewHolder): **edit / delete buttons are shown only in
  "own" mode** (`actionGroup.isVisible = getCurrentUser() == null`); in user mode a
  **`reserved` SwitchCompat** is shown instead (`reservedGroup` visible when
  `getCurrentUser() != null`) — that's the "reserve someone else's gift" interaction.
- Switch toggle → if the item is in Error state, just clears the state; otherwise
  `saveCurrentGift` + `reserveGiftItem(item)`: per-row `GiftsItem.operationState`
  (Loading hides action/reserved groups, shows spinner); success updates
  `item.reserved = response.reserved ?: false` + Toast (`gifts_status_change` in user
  mode); error → `showErrorAlert`-like row state.
- Delete: confirm → `deleteGiftItem` → removed from `cache.currentWishlist.gifts`
  (+DataState.Empty when last removed).
- `GiftsEditFragment` bottom sheet: create/edit by `cache.currentGift`; create adds to
  `cache.currentWishlist.gifts`; edit mutates in place. Emits `FRAGMENT_GIFTS_EDIT` /
  `FRAGMENT_GIFTS_CREATE` results like the wishlist sheet.
- **No wishlist create in user mode, no gift create in user mode** (add buttons hidden);
  also no delete/edit of others' wishlists (their edit/delete buttons hidden in
  NotSet state — see onBind above).

## RecyclerView layer (`recyclerview/{users,wishlists,gifts}/`)
- Plain `ListAdapter` + `ViewHolder` + `DiffUtil.ItemCallback` triplets, **no
  ViewBinding in holders** (raw `findViewById`).
- Row layouts: `fragment_users_info` (username), `fragment_wishlists_info`
  (title/description + edit/delete buttons in `action_group`, spinner in
  `loading_group`), `fragment_gifts_info` (title/description/price + `reserved`
  SwitchCompat in `reserved_group`, edit/delete in `action_group`, spinner in
  `loading_group`).
- Adapters take lambda params (click/edit/delete/onBind) so the Fragment owns all logic;
  `onBind` hands the fragment the `Group`s + switch to drive state visibility.
- Diff callbacks are buggy-by-design: `areItemsTheSame = oldItem == newItem`
  (data-class equality — includes the embedded MutableLiveData) and
  `areContentsTheSame = id equality`. In practice `submitList` with the same list
  objects yields no partial updates; edits rely on explicit `notifyItemChanged(position)`
  / re-emit of the list.
- `WishlistsItem`/`GiftsItem` are `data class`es with **mutable fields
  (`var title/price/reserved...`) and an embedded `MutableLiveData` state** —
  "smart row items" pattern: the row observes its own item's state LiveData.
- `UsersItem` is a plain immutable DTO.

