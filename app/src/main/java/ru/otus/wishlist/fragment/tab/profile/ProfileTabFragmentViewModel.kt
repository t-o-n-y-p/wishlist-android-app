package ru.otus.wishlist.fragment.tab.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.otus.wishlist.WizardCache
import ru.otus.wishlist.databinding.FragmentProfileTabBinding
import ru.otus.wishlist.storage.UserPreferences
import ru.otus.wishlist.storage.get
import ru.otus.wishlist.storage.logout
import javax.inject.Inject

@HiltViewModel
class ProfileTabFragmentViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val cache: WizardCache
) : ViewModel() {

    fun fillDataFromPreferences(binding: FragmentProfileTabBinding) =
        sharedPreferences.get<UserPreferences>()
            .takeIf { it.isLoggedIn() }
            ?.let { binding.usernameText.text = it.username }

    fun logout() {
        sharedPreferences.logout()
        cache.wishlists.clear()
        cache.users.clear()
    }
}