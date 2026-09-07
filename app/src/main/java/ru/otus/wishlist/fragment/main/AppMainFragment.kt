package ru.otus.wishlist.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentAppMainBinding

@AndroidEntryPoint
class AppMainFragment : Fragment(R.layout.fragment_app_main) {

    private lateinit var binding: FragmentAppMainBinding
    private val viewModel: AppMainFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomNavigation.apply {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.mine_menu -> navigate(R.id.go_to_mine_tab)
                    R.id.users_menu -> navigate(R.id.go_to_users_tab)
                    R.id.profile_menu -> navigate(R.id.go_to_profile_tab)
                    else -> false
                }
            }
            setOnItemReselectedListener {}
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {}
    }

    private fun navigate(resId: Int): Boolean {
        viewModel.clearCache()
        binding.appMainFragmentContainer.findNavController().navigate(resId)
        return true
    }
}