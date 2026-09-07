package ru.otus.wishlist.fragment.tab.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentUsersTabBinding

@AndroidEntryPoint
class UsersTabFragment : Fragment(R.layout.fragment_users_tab) {

    private lateinit var binding: FragmentUsersTabBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersTabBinding.inflate(inflater)
        return binding.root
    }
}