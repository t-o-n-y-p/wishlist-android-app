package ru.otus.wishlist.fragment.wishlists.edit

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
import ru.otus.wishlist.api.models.Wishlist
import ru.otus.wishlist.databinding.FragmentWishlistsEditBinding
import ru.otus.wishlist.fragment.FRAGMENT_WISHLISTS_CREATE
import ru.otus.wishlist.fragment.FRAGMENT_WISHLISTS_EDIT

@HiltViewModel
class WishlistsEditFragmentViewModel
@Inject
constructor(
    private val useCase: WishlistsEditFragmentUseCase,
    private val cache: WizardCache,
    private val mapper: DtoMapper,
) : ViewModel() {

    private val mOperationState = MutableLiveData<OperationState>(OperationState.NotSet)
    val operationState: LiveData<OperationState>
        get() = mOperationState

    fun fillFieldsFromCache(binding: FragmentWishlistsEditBinding, editTitle: String) =
        cache.currentWishlist?.apply {
            binding.titleInput.setText(title)
            binding.descriptionInput.setText(description)
            binding.wishlistEditTitle.text = editTitle
        }

    fun createOrUpdateWishlist(title: String, description: String) =
        viewModelScope.launch {
            try {
                cache.currentWishlist?.apply {
                    mOperationState.value = OperationState.Loading
                    useCase
                        .updateWishlist(
                            id = id,
                            title = title,
                            description = description,
                        )
                        .getOrThrow()
                    this.title = title
                    this.description = description
                    mOperationState.value = OperationState.Success
                }
                    ?: let {
                        mOperationState.value = OperationState.Loading
                        val createResponse: Wishlist =
                            useCase
                                .createWishlist(
                                    title = title,
                                    description = description,
                                )
                                .getOrThrow()
                        val newWishlist = mapper.mapToWishlistsItem(createResponse)
                        cache.wishlists.add(newWishlist)
                        mOperationState.value = OperationState.Success
                    }
            } catch (_: Throwable) {
                mOperationState.value = OperationState.Error
            }
        }

    fun getToastText() =
        cache.currentWishlist?.let { R.string.wishlists_updated }
            ?: R.string.wishlists_created

    fun getFragmentResultRequestKey() =
        cache.currentWishlist?.let { FRAGMENT_WISHLISTS_EDIT }
            ?: FRAGMENT_WISHLISTS_CREATE

    sealed class OperationState {

        data object NotSet : OperationState()

        data object Loading : OperationState()

        data object Success : OperationState()

        data object Error : OperationState()
    }
}
