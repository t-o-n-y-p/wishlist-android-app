package ru.otus.wishlist.fragment.wishlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.otus.wishlist.DtoMapper
import ru.otus.wishlist.WizardCache
import ru.otus.wishlist.recyclerview.wishlists.WishlistsItem
import ru.otus.wishlist.recyclerview.wishlists.WishlistsItemAdapter
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class WishlistsFragmentViewModel @Inject constructor(
    private val useCase: WishlistsFragmentUseCase,
    private val cache: WizardCache,
    private val mapper: DtoMapper
) : ViewModel() {

    private val mDataState = MutableLiveData<DataState>(DataState.NotSet)
    val dataState: LiveData<DataState>
        get() = mDataState
    private val mContentState = MutableLiveData<List<WishlistsItem>>()
    val contentState: LiveData<List<WishlistsItem>>
        get() = mContentState

    private var refreshDataTask: Job = Job()

    fun fillWishlistsFromCache() {
        cache.wishlists
            .takeIf { it.isEmpty() }
            ?.let { loadWishlistsAndSaveToCache() }
            ?: let {
                mDataState.value = DataState.Loading
                mContentState.value = cache.wishlists
                mDataState.value = DataState.Content
            }
    }

    fun loadWishlistsAndSaveToCache() {
        refreshDataTask.cancel()
        refreshDataTask = viewModelScope.launch {
            try {
                mDataState.value = DataState.Loading
                val result = cache.currentUser?.id?.let {
                    useCase.getUserWishlists(it)
                } ?: useCase.getAllWishlists()
                val data = result.getOrThrow()
                data.takeIf { it.isNotEmpty() }
                    ?.let {
                        cache.wishlists = mapper.mapToWishlistsItem(it)
                        mContentState.value = cache.wishlists
                        mDataState.value = DataState.Content
                    }
                    ?: let { mDataState.value = DataState.Empty }
            } catch (_: Throwable) {
                mDataState.value = DataState.Error
            }
        }
    }

    fun getOnScrollListener(adapter: WishlistsItemAdapter, pageSize: Int) =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (recyclerView.layoutManager as? LinearLayoutManager)?.apply {
                    findLastVisibleItemPosition()
                        .takeIf { it == itemCount - 1 && itemCount < cache.wishlists.size }
                        ?.let {
                            adapter.submitList(
                                cache.wishlists.slice(
                                    0
                                            until
                                            min(cache.wishlists.size, itemCount + pageSize)
                                ))
                        }
                }
            }
        }

    fun saveCurrentWishlist(item: WishlistsItem, position: Int) {
        cache.currentWishlist = item
        cache.currentWishlistPosition = position
    }

    fun getCurrentWishlistPosition() = cache.currentWishlistPosition

    fun deleteWishlistItem(item: WishlistsItem) =
        viewModelScope.launch {
            try {
                item.deleteInProgress()
                useCase.deleteWishlist(item.id).getOrThrow()
                item.deleteSuccess()
                cache.wishlists.apply {
                    remove(item)
                    mContentState.value = this
                    ifEmpty { mDataState.value = DataState.Empty }
                }
            } catch (_: Throwable) {
                item.deleteError()
            }
        }

    fun clearCurrentWishlist() {
        cache.currentWishlist = null
        cache.currentWishlistPosition = 0
    }

    fun getCurrentUser() = cache.currentUser

    sealed class DataState {

        data object NotSet: DataState()

        data object Loading: DataState()

        data object Empty: DataState()

        data object Content: DataState()

        data object Error : DataState()
    }
}