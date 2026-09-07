package ru.otus.wishlist.fragment.gifts.edit

import dagger.hilt.android.scopes.ViewModelScoped
import ru.otus.wishlist.NetworkCallProcessor
import ru.otus.wishlist.api.models.Gift
import ru.otus.wishlist.api.models.GiftDto
import ru.otus.wishlist.service.GiftsService
import javax.inject.Inject

@ViewModelScoped
class GiftsEditFragmentUseCase @Inject constructor(
    private val networkCallProcessor: NetworkCallProcessor,
    private val giftsService: GiftsService
) {

    suspend fun updateGift(id: String, name: String, description: String, price: Int): Result<Gift> =
        networkCallProcessor.call {
            val dto = GiftDto(
                name = name,
                description = description,
                price = price.toDouble()
            )
            giftsService.updateGift(id, dto)
        }

    suspend fun createGift(wishlistId: String, name: String, description: String, price: Int): Result<Gift> =
        networkCallProcessor.call {
            val dto = GiftDto(
                name = name,
                description = description,
                price = price.toDouble()
            )
            giftsService.createGift(wishlistId, dto)
        }
}