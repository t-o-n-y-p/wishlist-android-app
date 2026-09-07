package ru.otus.wishlist.fragment.tab.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentProfileTabBinding
import kotlin.getValue

@AndroidEntryPoint
class ProfileTabFragment : Fragment(R.layout.fragment_profile_tab) {

    private lateinit var binding: FragmentProfileTabBinding
    private val viewModel: ProfileTabFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileTabBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logOutButton.setOnClickListener {
            viewModel.logout()
        }
        viewModel.fillDataFromPreferences(binding)
    }
}