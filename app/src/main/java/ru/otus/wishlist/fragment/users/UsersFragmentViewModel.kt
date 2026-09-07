package ru.otus.wishlist.fragment.users

import android.content.SharedPreferences
import androidx.core.view.isVisible
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
import ru.otus.wishlist.databinding.FragmentUsersBinding
import ru.otus.wishlist.recyclerview.users.UsersItem
import ru.otus.wishlist.recyclerview.users.UsersItemAdapter
import ru.otus.wishlist.storage.UserPreferences
import ru.otus.wishlist.storage.get
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class UsersFragmentViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val useCase: UsersFragmentUseCase,
    private val cache: WizardCache,
    private val mapper: DtoMapper
) : ViewModel() {

    private val mDataState = MutableLiveData<DataState>(DataState.NotSet)
    val dataState: LiveData<DataState>
        get() = mDataState
    private val mContentState = MutableLiveData<List<UsersItem>>()
    val contentState: LiveData<List<UsersItem>>
        get() = mContentState

    private var refreshDataTask: Job = Job()

    fun fillUsersFromCache(binding: FragmentUsersBinding) {
        binding.clearFilterButton.isVisible = cache.usernameFilter.isNotBlank()
        cache.users
            .takeIf { it.isEmpty() }
            ?.let { loadUsersAndSaveToCache() }
            ?: let {
                mDataState.value = DataState.Loading
                cache.filteredUsers =
                    cache.users.filter { user ->
                        cache.usernameFilter
                            .takeIf { it.isNotEmpty() }
                            ?.let { it == user.username }
                            ?: true
                    }
                mContentState.value = cache.filteredUsers
                mDataState.value =
                    if (cache.filteredUsers.isEmpty()) DataState.Empty else DataState.Content
            }
    }

    fun loadUsersAndSaveToCache() {
        refreshDataTask.cancel()
        refreshDataTask = viewModelScope.launch {
            try {
                mDataState.value = DataState.Loading
                val data = useCase.getAllUsers().getOrThrow()
                data.takeIf { it.isNotEmpty() }
                    ?.let {
                        cache.users = mapper.mapToUsersItem(it) { user ->
                            user.username != sharedPreferences.get<UserPreferences>().username
                        }
                        cache.filteredUsers = cache.users
                        mContentState.value = cache.filteredUsers
                        mDataState.value = DataState.Content
                    }
                    ?: let { mDataState.value = DataState.Empty }
            } catch (_: Throwable) {
                mDataState.value = DataState.Error
            }
        }
    }

    fun getOnScrollListener(adapter: UsersItemAdapter, pageSize: Int) =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (recyclerView.layoutManager as? LinearLayoutManager)?.apply {
                    findLastVisibleItemPosition()
                        .takeIf { it == itemCount - 1 && itemCount < cache.filteredUsers.size }
                        ?.let {
                            adapter.submitList(
                                cache.filteredUsers.slice(
                                    0
                                            until
                                            min(cache.filteredUsers.size, itemCount + pageSize)
                                ))
                        }
                }
            }
        }

    fun saveCurrentUser(item: UsersItem) {
        if (item != cache.currentUser) {
            cache.wishlists = mutableListOf()
        }
        cache.currentUser = item
    }

    fun clearUsersFilter() {
        cache.usernameFilter = ""
    }

    sealed class DataState {

        data object NotSet: DataState()

        data object Loading: DataState()

        data object Empty: DataState()

        data object Content: DataState()

        data object Error : DataState()
    }
}