package ru.otus.wishlist.fragment.users.filter

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.otus.wishlist.R
import ru.otus.wishlist.WizardCache
import ru.otus.wishlist.databinding.FragmentUsersFilterBinding
import javax.inject.Inject

@HiltViewModel
class UsersFilterFragmentViewModel @Inject constructor(
    private val cache: WizardCache
) : ViewModel() {

    fun fillFieldsFromCache(binding: FragmentUsersFilterBinding) {
        binding.usernameInput.setText(cache.usernameFilter)
    }

    fun applyUsersFilter(binding: FragmentUsersFilterBinding) {
        cache.usernameFilter = binding.usernameInput.text.toString()
    }

    fun getToastText() =
        if (cache.usernameFilter.isBlank()) R.string.filter_cleared else R.string.filter_applied
}