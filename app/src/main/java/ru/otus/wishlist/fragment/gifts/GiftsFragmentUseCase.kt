package ru.otus.wishlist.fragment.gifts

import dagger.hilt.android.scopes.ViewModelScoped
import ru.otus.wishlist.NetworkCallProcessor
import ru.otus.wishlist.api.models.Gift
import ru.otus.wishlist.service.GiftsService
import javax.inject.Inject

@ViewModelScoped
class GiftsFragmentUseCase @Inject constructor(
    private val networkCallProcessor: NetworkCallProcessor,
    private val giftsService: GiftsService
) {

    suspend fun deleteGift(id: String): Result<Unit> =
        networkCallProcessor.call {
            giftsService.deleteGift(id = id)
        }

    suspend fun reserveGift(id: String): Result<Gift> =
        networkCallProcessor.call {
            giftsService.reserveGift(id = id)
        }
}