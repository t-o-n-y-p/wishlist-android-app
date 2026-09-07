package ru.otus.wishlist.fragment.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentUsersBinding
import ru.otus.wishlist.fragment.FRAGMENT_USERS_FILTER
import ru.otus.wishlist.fragment.RECYCLER_VIEW_PAGE_SIZE
import ru.otus.wishlist.fragment.RESULT
import ru.otus.wishlist.fragment.SUCCESS
import ru.otus.wishlist.fragment.users.filter.UsersFilterFragment
import ru.otus.wishlist.recyclerview.users.UsersItemAdapter
import kotlin.math.min

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users) {

    private lateinit var binding: FragmentUsersBinding
    private val viewModel: UsersFragmentViewModel by viewModels()
    private val adapter: UsersItemAdapter = UsersItemAdapter(
        onItemClicked = { item ->
            viewModel.saveCurrentUser(item)
            findNavController().navigate(R.id.go_to_wishlists)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.usersContent.users.apply {
            adapter = this@UsersFragment.adapter
            addOnScrollListener(
                viewModel.getOnScrollListener(
                    this@UsersFragment.adapter, RECYCLER_VIEW_PAGE_SIZE))
        }
        setFragmentResultListener(FRAGMENT_USERS_FILTER) { _, bundle ->
            when (bundle.getString(RESULT)) {
                SUCCESS -> {
                    viewModel.fillUsersFromCache(binding)
                }
            }
        }
        viewModel.dataState.observe(viewLifecycleOwner) {
            when (it) {
                UsersFragmentViewModel.DataState.NotSet -> {
                    binding.usersLoading.loadingGroup.isVisible = false
                    binding.usersContent.contentGroup.isVisible = false
                    binding.usersError.errorGroup.isVisible = false
                    binding.usersNoResults.noResultsGroup.isVisible = false
                }
                UsersFragmentViewModel.DataState.Loading -> {
                    binding.usersLoading.loadingGroup.isVisible = true
                    binding.usersContent.contentGroup.isVisible = false
                    binding.usersError.errorGroup.isVisible = false
                    binding.usersNoResults.noResultsGroup.isVisible = false
                }
                UsersFragmentViewModel.DataState.Content -> {
                    binding.usersLoading.loadingGroup.isVisible = false
                    binding.usersContent.contentGroup.isVisible = true
                    binding.usersError.errorGroup.isVisible = false
                    binding.usersNoResults.noResultsGroup.isVisible = false
                }
                UsersFragmentViewModel.DataState.Error -> {
                    binding.usersLoading.loadingGroup.isVisible = false
                    binding.usersContent.contentGroup.isVisible = false
                    binding.usersError.errorGroup.isVisible = true
                    binding.usersNoResults.noResultsGroup.isVisible = false
                }
                UsersFragmentViewModel.DataState.Empty -> {
                    binding.usersLoading.loadingGroup.isVisible = false
                    binding.usersContent.contentGroup.isVisible = false
                    binding.usersError.errorGroup.isVisible = false
                    binding.usersNoResults.noResultsGroup.isVisible = true
                }
            }
        }
        viewModel.contentState.observe(viewLifecycleOwner) {
            adapter.submitList(it.slice(0 until min(it.size, RECYCLER_VIEW_PAGE_SIZE)))
        }
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.filter -> {
                    UsersFilterFragment().show(
                        parentFragmentManager,
                        UsersFilterFragment::class.simpleName
                    )
                    true
                }
                else -> false
            }
        }
        binding.clearFilterButton.setOnClickListener {
            viewModel.clearUsersFilter()
            viewModel.fillUsersFromCache(binding)
        }
        viewModel.fillUsersFromCache(binding)
    }
}