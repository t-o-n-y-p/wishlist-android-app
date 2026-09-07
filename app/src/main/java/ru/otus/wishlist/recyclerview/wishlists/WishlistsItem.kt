package ru.otus.wishlist.recyclerview.wishlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.otus.wishlist.recyclerview.gifts.GiftsItem

data class WishlistsItem(
    val id: String = "",
    var title: String = "",
    var description: String = "",
    val gifts: MutableList<GiftsItem> = mutableListOf(),
    private val mDeleteState: MutableLiveData<DeleteState> = MutableLiveData(DeleteState.NotSet)
) {
    val deleteState: LiveData<DeleteState>
        get() = mDeleteState

    fun clearDeleteState() {
        mDeleteState.value = DeleteState.NotSet
    }

    fun deleteInProgress() {
        mDeleteState.value = DeleteState.Loading
    }

    fun deleteSuccess() {
        mDeleteState.value = DeleteState.Success
    }

    fun deleteError() {
        mDeleteState.value = DeleteState.Error
    }

    sealed class DeleteState {

        data object NotSet: DeleteState()

        data object Loading: DeleteState()

        data object Success: DeleteState()

        data object Error : DeleteState()
    }
}
