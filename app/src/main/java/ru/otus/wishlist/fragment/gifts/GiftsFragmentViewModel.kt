package ru.otus.wishlist.fragment.gifts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.otus.wishlist.WizardCache
import ru.otus.wishlist.recyclerview.gifts.GiftsItem
import ru.otus.wishlist.recyclerview.gifts.GiftsItemAdapter
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class GiftsFragmentViewModel @Inject constructor(
    private val useCase: GiftsFragmentUseCase,
    private val cache: WizardCache
) : ViewModel() {

    private val mDataState = MutableLiveData<DataState>(DataState.NotSet)
    val dataState: LiveData<DataState>
        get() = mDataState
    private val mContentState = MutableLiveData<List<GiftsItem>>()
    val contentState: LiveData<List<GiftsItem>>
        get() = mContentState

    fun fillGiftsFromCache() {
        cache.currentWishlist
            ?.gifts
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                mDataState.value = DataState.Loading
                mContentState.value = it
                mDataState.value = DataState.Content
            }
            ?: let {
                mDataState.value = DataState.Loading
                mDataState.value = DataState.Empty
            }
    }

    fun getOnScrollListener(adapter: GiftsItemAdapter, pageSize: Int) =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (recyclerView.layoutManager as? LinearLayoutManager)?.apply {
                    val numberOfGifts = cache.currentWishlist?.gifts?.size ?: 0
                    findLastVisibleItemPosition()
                        .takeIf {
                            it == itemCount - 1 && itemCount < numberOfGifts
                        }
                        ?.let {
                            adapter.submitList(
                                cache.currentWishlist?.gifts?.slice(
                                    0
                                            until
                                            min(numberOfGifts, itemCount + pageSize)
                                ))
                        }
                }
            }
        }

    fun saveCurrentGift(item: GiftsItem, position: Int) {
        cache.currentGift = item
        cache.currentGiftPosition = position
    }

    fun getCurrentGiftPosition() = cache.currentGiftPosition

    fun deleteGiftItem(item: GiftsItem) =
        viewModelScope.launch {
            try {
                item.operationInProgress()
                useCase.deleteGift(item.id).getOrThrow()
                item.operationSuccess()
                cache.currentWishlist?.gifts?.apply {
                    remove(item)
                    mContentState.value = this
                    ifEmpty { mDataState.value = DataState.Empty }
                }
            } catch (_: Throwable) {
                item.operationError()
            }
        }

    fun reserveGiftItem(item: GiftsItem) =
        viewModelScope.launch {
            try {
                item.operationInProgress()
                val response = useCase.reserveGift(item.id).getOrThrow()
                item.reserved = response.reserved ?: false
                item.operationSuccess()
            } catch (_: Throwable) {
                item.operationError()
            }
        }

    fun clearCurrentGift() {
        cache.currentGift = null
        cache.currentGiftPosition = 0
    }

    fun getCurrentWishlist() = cache.currentWishlist

    fun getCurrentUser() = cache.currentUser

    sealed class DataState {

        data object NotSet: DataState()

        data object Loading: DataState()

        data object Empty: DataState()

        data object Content: DataState()

        data object Error : DataState()
    }
}