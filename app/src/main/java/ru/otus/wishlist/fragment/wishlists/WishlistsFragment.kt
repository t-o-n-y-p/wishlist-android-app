package ru.otus.wishlist.fragment.wishlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentWishlistsBinding
import ru.otus.wishlist.fragment.FRAGMENT_WISHLISTS_CREATE
import ru.otus.wishlist.fragment.FRAGMENT_WISHLISTS_EDIT
import ru.otus.wishlist.fragment.RECYCLER_VIEW_PAGE_SIZE
import ru.otus.wishlist.fragment.RESULT
import ru.otus.wishlist.fragment.SUCCESS
import ru.otus.wishlist.fragment.showConfirmationAlert
import ru.otus.wishlist.fragment.showErrorAlert
import ru.otus.wishlist.fragment.wishlists.edit.WishlistsEditFragment
import ru.otus.wishlist.recyclerview.wishlists.WishlistsItem
import ru.otus.wishlist.recyclerview.wishlists.WishlistsItemAdapter
import kotlin.math.min

@AndroidEntryPoint
class WishlistsFragment : Fragment(R.layout.fragment_wishlists) {

    private lateinit var binding: FragmentWishlistsBinding
    private val viewModel: WishlistsFragmentViewModel by viewModels()
    private val adapter: WishlistsItemAdapter = WishlistsItemAdapter(
        onItemClicked = { item, position ->
            viewModel.saveCurrentWishlist(item, position)
            findNavController().navigate(R.id.go_to_gifts)
        },
        onEditButtonClicked = { item, position ->
            viewModel.saveCurrentWishlist(item, position)
            item.clearDeleteState()
            WishlistsEditFragment().show(
                parentFragmentManager,
                WishlistsEditFragment::class.simpleName)
        },
        onDeleteButtonClicked = { item ->
            requireActivity().showConfirmationAlert { _, _ ->
                viewModel.deleteWishlistItem(item)
            }
        },
        onBind = { item, actionGroup, loadingGroup ->
            item.deleteState.observe(viewLifecycleOwner) {
                when (it) {
                    WishlistsItem.DeleteState.NotSet -> {
                        actionGroup.isVisible = viewModel.getCurrentUser() == null
                        loadingGroup.isVisible = false
                    }
                    WishlistsItem.DeleteState.Loading -> {
                        actionGroup.isVisible = false
                        loadingGroup.isVisible = true
                    }
                    WishlistsItem.DeleteState.Success -> {
                        Toast.makeText(
                            context,
                            R.string.wishlists_deleted,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    WishlistsItem.DeleteState.Error -> {
                        actionGroup.isVisible = true
                        loadingGroup.isVisible = false
                        requireContext().showErrorAlert()
                    }
                }
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.wishlistsContent.wishlists.apply {
            adapter = this@WishlistsFragment.adapter
            addOnScrollListener(
                viewModel.getOnScrollListener(
                    this@WishlistsFragment.adapter, RECYCLER_VIEW_PAGE_SIZE))
        }
        setFragmentResultListener(FRAGMENT_WISHLISTS_EDIT) { _, bundle ->
            when (bundle.getString(RESULT)) {
                SUCCESS -> adapter.notifyItemChanged(viewModel.getCurrentWishlistPosition())
            }
        }
        setFragmentResultListener(FRAGMENT_WISHLISTS_CREATE) { _, bundle ->
            when (bundle.getString(RESULT)) {
                SUCCESS -> viewModel.fillWishlistsFromCache()
            }
        }
        viewModel.dataState.observe(viewLifecycleOwner) {
            when (it) {
                WishlistsFragmentViewModel.DataState.NotSet -> {
                    binding.wishlistsLoading.loadingGroup.isVisible = false
                    binding.wishlistsContent.contentGroup.isVisible = false
                    binding.wishlistsError.errorGroup.isVisible = false
                    binding.wishlistsNoResults.noResultsGroup.isVisible = false
                }
                WishlistsFragmentViewModel.DataState.Loading -> {
                    binding.wishlistsLoading.loadingGroup.isVisible = true
                    binding.wishlistsContent.contentGroup.isVisible = false
                    binding.wishlistsError.errorGroup.isVisible = false
                    binding.wishlistsNoResults.noResultsGroup.isVisible = false
                }
                WishlistsFragmentViewModel.DataState.Content -> {
                    binding.wishlistsLoading.loadingGroup.isVisible = false
                    binding.wishlistsContent.contentGroup.isVisible = true
                    binding.wishlistsError.errorGroup.isVisible = false
                    binding.wishlistsNoResults.noResultsGroup.isVisible = false
                }
                WishlistsFragmentViewModel.DataState.Error -> {
                    binding.wishlistsLoading.loadingGroup.isVisible = false
                    binding.wishlistsContent.contentGroup.isVisible = false
                    binding.wishlistsError.errorGroup.isVisible = true
                    binding.wishlistsNoResults.noResultsGroup.isVisible = false
                }
                WishlistsFragmentViewModel.DataState.Empty -> {
                    binding.wishlistsLoading.loadingGroup.isVisible = false
                    binding.wishlistsContent.contentGroup.isVisible = false
                    binding.wishlistsError.errorGroup.isVisible = false
                    binding.wishlistsNoResults.noResultsGroup.isVisible = true
                }
            }
        }
        viewModel.contentState.observe(viewLifecycleOwner) {
            adapter.submitList(it.slice(0 until min(it.size, RECYCLER_VIEW_PAGE_SIZE)))
        }
        binding.addButton.setOnClickListener {
            viewModel.clearCurrentWishlist()
            WishlistsEditFragment().show(
                parentFragmentManager,
                WishlistsEditFragment::class.simpleName)
        }
        viewModel.getCurrentUser()
            ?.apply {
                binding.addButton.isVisible = false
                binding.topAppBar.title = getString(R.string.wishlists_of_user).format(username)
                requireActivity().onBackPressedDispatcher.addCallback(this@WishlistsFragment) {
                    findNavController().navigate(R.id.go_to_users)
                }
            }
            ?: let {
                binding.topAppBar.title = getString(R.string.mine)
            }
        viewModel.fillWishlistsFromCache()
    }
}