package ru.otus.wishlist.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
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
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAppMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.app_main_fragment_container)
                as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            viewModel.clearCache()
            val navOptions =
                NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(
                        navController.graph.findStartDestination().id,
                        inclusive = false,
                        saveState = false,
                    )
                    .build()
            try {
                navController.navigate(item.itemId, null, navOptions)
                true
            } catch (_: IllegalArgumentException) {
                false
            }
        }
    }
}
