package ru.otus.wishlist.fragment.wishlists.edit

import dagger.hilt.android.scopes.ViewModelScoped
import ru.otus.wishlist.NetworkCallProcessor
import ru.otus.wishlist.api.models.Wishlist
import ru.otus.wishlist.api.models.WishlistDto
import ru.otus.wishlist.service.WishlistsService
import javax.inject.Inject

@ViewModelScoped
class WishlistsEditFragmentUseCase @Inject constructor(
    private val networkCallProcessor: NetworkCallProcessor,
    private val wishlistsService: WishlistsService
) {

    suspend fun updateWishlist(id: String, title: String, description: String): Result<Wishlist> =
        networkCallProcessor.call {
            val dto = WishlistDto(
                title = title,
                description = description
            )
            wishlistsService.updateWishlist(id, dto)
        }

    suspend fun createWishlist(title: String, description: String): Result<Wishlist> =
        networkCallProcessor.call {
            val dto = WishlistDto(
                title = title,
                description = description
            )
            wishlistsService.createWishlist(dto)
        }
}