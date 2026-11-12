package ru.otus.wishlist.fragment

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Screen-level state shared by all list ViewModels (users / wishlists / gifts).
 *
 * A screen starts in [NotSet]; every (re)load emits [Loading] and ends in [Content]
 * (non-empty list), [Empty] (empty list) or [Error] (failed load).
 */
sealed class DataState {

    data object NotSet : DataState()

    data object Loading : DataState()

    data object Empty : DataState()

    data object Content : DataState()

    data object Error : DataState()
}

/**
 * Maps a loaded list to [DataState.Content] (non-empty) or [DataState.Empty] (empty) and
 * emits the resulting state into [this].
 */
fun <T> MutableLiveData<DataState>.mapToState(items: List<T>) {
    value = if (items.isEmpty()) DataState.Empty else DataState.Content
}

/**
 * Runs a suspending list load in [scope] and drives [this] through [DataState.Loading] →
 * [DataState.Content]/[DataState.Empty]; any [Throwable] thrown by [block] collapses to
 * [DataState.Error].
 *
 * [block] performs the load (fetch + mapping + cache) and returns the list to publish;
 * the helper emits that list into [contentState] and maps it to the resulting state.
 * Returns the launched [Job] so the caller can cancel/restart the load.
 */
fun <T> MutableLiveData<DataState>.fetchList(
    scope: CoroutineScope,
    contentState: MutableLiveData<List<T>>,
    block: suspend () -> List<T>,
): Job {
    return scope.launch {
        value = DataState.Loading
        try {
            val items = block()
            contentState.value = items
            mapToState(items)
        } catch (_: Throwable) {
            value = DataState.Error
        }
    }
}
