package ru.otus.wishlist.recyclerview.gifts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class GiftsItem(
    val id: String = "",
    var name: String = "",
    var description: String = "",
    var price: Int = 0,
    var reserved: Boolean = false,
    private val mOperationState: MutableLiveData<OperationState> =
        MutableLiveData(OperationState.NotSet)
) {

    val operationState: LiveData<OperationState>
        get() = mOperationState

    fun clearOperationState() {
        mOperationState.value = OperationState.NotSet
    }

    fun operationInProgress() {
        mOperationState.value = OperationState.Loading
    }

    fun operationSuccess() {
        mOperationState.value = OperationState.Success
    }

    fun operationError() {
        mOperationState.value = OperationState.Error
    }

    fun isOperationStateError() =
        mOperationState.value == OperationState.Error

    sealed class OperationState {

        data object NotSet: OperationState()

        data object Loading: OperationState()

        data object Success: OperationState()

        data object Error : OperationState()
    }
}
