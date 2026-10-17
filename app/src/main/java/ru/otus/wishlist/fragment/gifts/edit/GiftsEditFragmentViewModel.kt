package ru.otus.wishlist.fragment.gifts.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import ru.otus.wishlist.DtoMapper
import ru.otus.wishlist.R
import ru.otus.wishlist.WizardCache
import ru.otus.wishlist.api.models.Gift
import ru.otus.wishlist.databinding.FragmentGiftsEditBinding
import ru.otus.wishlist.fragment.FRAGMENT_GIFTS_CREATE
import ru.otus.wishlist.fragment.FRAGMENT_GIFTS_EDIT

@HiltViewModel
class GiftsEditFragmentViewModel
@Inject
constructor(
    private val useCase: GiftsEditFragmentUseCase,
    private val cache: WizardCache,
    private val mapper: DtoMapper,
) : ViewModel() {

    private val mOperationState = MutableLiveData<OperationState>(OperationState.NotSet)
    val operationState: LiveData<OperationState>
        get() = mOperationState

    fun fillFieldsFromCache(binding: FragmentGiftsEditBinding, editTitle: String) =
        cache.currentGift?.apply {
            binding.nameInput.setText(name)
            binding.descriptionInput.setText(description)
            binding.priceInput.setText(price.toString())
            binding.giftEditTitle.text = editTitle
        }

    fun createOrUpdateGift(name: String, description: String, price: Int) =
        viewModelScope.launch {
            try {
                cache.currentGift?.apply {
                    mOperationState.value = OperationState.Loading
                    useCase
                        .updateGift(
                            id = id,
                            name = name,
                            description = description,
                            price = price,
                        )
                        .getOrThrow()
                    this.name = name
                    this.description = description
                    this.price = price
                    mOperationState.value = OperationState.Success
                }
                    ?: let {
                        mOperationState.value = OperationState.Loading
                        val createResponse: Gift =
                            useCase
                                .createGift(
                                    wishlistId = cache.currentWishlist?.id.orEmpty(),
                                    name = name,
                                    description = description,
                                    price = price,
                                )
                                .getOrThrow()
                        val newGift = mapper.mapToGiftsItem(createResponse)
                        cache.currentWishlist?.gifts?.add(newGift)
                        mOperationState.value = OperationState.Success
                    }
            } catch (_: Throwable) {
                mOperationState.value = OperationState.Error
            }
        }

    fun getToastText() =
        cache.currentGift?.let { R.string.gifts_updated } ?: R.string.gifts_created

    fun getFragmentResultRequestKey() =
        cache.currentGift?.let { FRAGMENT_GIFTS_EDIT } ?: FRAGMENT_GIFTS_CREATE

    sealed class OperationState {

        data object NotSet : OperationState()

        data object Loading : OperationState()

        data object Success : OperationState()

        data object Error : OperationState()
    }
}
