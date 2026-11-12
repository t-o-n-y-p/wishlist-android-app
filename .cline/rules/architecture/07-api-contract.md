# 07 — API contract (OpenAPI)

Reference: `wishlist-api/src/main/resources/wishlist.yaml` (OpenAPI 3.0.1, "Wishlist
API", v1.0). All paths are under `/api/...` — keep `BASE_URL` ending with `/api/`.
Model classes are code-generated at build time: do not commit generated sources —
change `wishlist.yaml` and regenerate.

## Endpoints
Keep the endpoint surface (the "used by app" column reflects current usage):

| Method | Path | operationId | Used by app? |
|---|---|---|---|
| GET | `/api/wishlists/{id}` | getWishlist | no |
| PUT | `/api/wishlists/{id}` | updateWishlist | yes |
| DELETE | `/api/wishlists/{id}` | deleteWishlist | yes |
| GET | `/api/gifts/{id}` | getGift | no |
| PUT | `/api/gifts/{id}` | updateGift | yes |
| DELETE | `/api/gifts/{id}` | deleteGift | yes |
| PUT | `/api/gifts/{id}/reserve` | toggleReserved | yes |
| GET | `/api/wishlists` | getCurrentUserWishlists | yes |
| POST | `/api/wishlists` | createWishlist | yes |
| GET | `/api/gifts/wishlist/{wishListId}` | getAllGiftsInWishlist | no* |
| POST | `/api/gifts/wishlist/{wishListId}` | createGift | yes |
| POST | `/api/auth/register` | registerUser | yes |
| POST | `/api/auth/login` | authenticateUser | yes |
| GET | `/api/wishlists/user/{userId}` | getUserWishlists | yes |
| GET | `/api/users` | getAllUsersOpen | yes |
| GET | `/api/users/{userId}/wishlists` | getUserWishlistsOpen | no (app uses the other path) |

\* Unused because **gifts arrive embedded in Wishlist DTOs** — do not add a separate
gift-list fetch.

## Schemas
- `User` — `id` (uuid), `username`, `email`.
- `Wishlist` — `id`, `title`, `description`, `owner: User`, **`gifts: Gift[]`**.
- `Gift` — `id`, `name`, `description`, `imageUrl`, `storeUrl`, `price: number`,
  `reserved: boolean`, `wishList: Wishlist` (back-reference, unused by the app; the
  app also ignores `imageUrl`/`storeUrl` — no images in the UI).
- `WishlistDto` — `title` (required), `description`.
- `GiftDto` — `id`, `name` (required, ≤255), `description` (≤1000), `imageUrl`,
  `price: number` (required), `storeUrl` (URL pattern), `reserved`.
- `UserRegistrationDto` — `username` (3–50), `email`, `password` (≥6), all required.
- `LoginDto` — `username`, `password` (both required).
- `Login` — `token`.

## Consequences to respect in app code
- Keep treating gifts as child data of wishlists: the app never lists/fetches gifts
  separately; reserve/delete/update are the only per-gift calls.
- Keep the price handling: `price` is `number` in the DTO (`GiftDto.price: Double`),
  `Double?` in `Gift`; `DtoMapper` narrows it to `Int` for the UI
  (`price?.toInt() ?: 0`).
- Keep treating all ids as UUIDs (strings) — plain Strings.
