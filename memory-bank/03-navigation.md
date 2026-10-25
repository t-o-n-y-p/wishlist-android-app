# 03 — Navigation / screen map

Sources: `res/navigation/{login_register,main_navigation,mine_tab_navigation,users_tab_navigation}.xml`,
`res/menu/bottom_navigation.xml`, `res/menu/users_filter.xml`

## Top-level graph: `login_register.xml` (mounted in `activity_main.xml`)
startDestination = `login_fragment`.

| id | Fragment | Actions |
|---|---|---|
| `login_fragment` | `fragment.login.LoginFragment` | `go_to_register` → register; `go_to_app_main` → app main (**popUpTo login inclusive** — replaces auth stack) |
| `register_fragment` | `fragment.register.RegisterFragment` | none (back only) |
| `app_main_fragment` | `fragment.main.AppMainFragment` | `logout` → login (**popUpTo app main inclusive**) |

So the whole app lives inside this one graph: the auth pair at the root, and
`AppMainFragment` is a normal destination. `MainActivity`'s `go_to_app_main`
(auto-login) and `logout` (logout listener) actions resolve against this graph.

## `main_navigation.xml` — bottom bar graph inside AppMainFragment
startDestination = `mine_tab`; three destinations:
- `mine_tab` → `fragment.tab.mine.MineTabFragment`
- `users_tab` → `fragment.tab.users.UsersTabFragment`
- `profile_tab` → `fragment.tab.profile.ProfileTabFragment`

Tab destinations are plain fragments; the nested per-tab graphs are mounted by the
tab fragments themselves (verified in stage 6).

## `mine_tab_navigation.xml` — "Mine" tab
startDestination = `wishlists_fragment`:
- `wishlists_fragment` → `WishlistsFragment` (action `go_to_gifts` → gifts)
- `gifts_fragment` → `GiftsFragment`
Flow: my wishlists → open a wishlist → its gifts.

## `users_tab_navigation.xml` — "Users" tab
startDestination = `users_fragment`:
- `users_fragment` → `UsersFragment` (action `go_to_wishlists` → wishlists)
- `wishlists_fragment` → `WishlistsFragment` (action `go_to_gifts` → gifts)
- `gifts_fragment` → `GiftsFragment`
Flow: all users → open a user → that user's wishlists → gifts.
**`WishlistsFragment` and `GiftsFragment` are reused in both tab graphs** (own vs.
someone else's data — argument-based, see stage 6).

## Menus
- `bottom_navigation.xml`: 3 items — `mine_tab` (list_alt icon), `users_tab`
  (article_person icon), `profile_tab` (person icon); titles @string/mine|users|profile.
- `users_filter.xml`: single always-shown `filter` item (filter_alt icon) — users screen toolbar.
