package ru.otus.wishlist.fragment.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.otus.wishlist.WizardCache
import javax.inject.Inject

@HiltViewModel
class AppMainFragmentViewModel @Inject constructor(
    private val cache: WizardCache
) : ViewModel() {

    fun clearCache() {
        cache.clear()
    }
}