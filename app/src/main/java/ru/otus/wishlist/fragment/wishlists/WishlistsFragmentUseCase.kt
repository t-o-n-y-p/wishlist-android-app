package ru.otus.wishlist.fragment.wishlists

import dagger.hilt.android.scopes.ViewModelScoped
import ru.otus.wishlist.NetworkCallProcessor
import ru.otus.wishlist.api.models.Wishlist
import ru.otus.wishlist.service.WishlistsService
import javax.inject.Inject

@ViewModelScoped
class WishlistsFragmentUseCase @Inject constructor(
    private val networkCallProcessor: NetworkCallProcessor,
    private val wishlistsService: WishlistsService
) {

    suspend fun getAllWishlists(): Result<List<Wishlist>> =
        networkCallProcessor.call {
            wishlistsService.getAllWishlists()
        }

    suspend fun deleteWishlist(id: String): Result<Unit> =
        networkCallProcessor.call {
            wishlistsService.deleteWishlist(id = id)
        }

    suspend fun getUserWishlists(userId: String): Result<List<Wishlist>> =
        networkCallProcessor.call {
            wishlistsService.getUserWishlists(userId = userId)
        }
}