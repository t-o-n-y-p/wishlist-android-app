package ru.otus.wishlist.fragment.tab.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentMineTabBinding

@AndroidEntryPoint
class MineTabFragment : Fragment(R.layout.fragment_mine_tab) {

    private lateinit var binding: FragmentMineTabBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMineTabBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.mine_tab_container)
                as NavHostFragment
        val navController = navHostFragment.navController
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.previousBackStackEntry?.let { navController.popBackStack() }
                ?: let {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
        }
    }
}
