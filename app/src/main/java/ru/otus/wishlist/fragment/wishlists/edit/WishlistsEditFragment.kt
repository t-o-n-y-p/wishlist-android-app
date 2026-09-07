package ru.otus.wishlist.fragment.wishlists.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentWishlistsEditBinding
import ru.otus.wishlist.fragment.RESULT
import ru.otus.wishlist.fragment.SUCCESS
import ru.otus.wishlist.fragment.dismissWithToast
import ru.otus.wishlist.fragment.setFragmentResult
import ru.otus.wishlist.fragment.showErrorAlert

@AndroidEntryPoint
class WishlistsEditFragment : BottomSheetDialogFragment(R.layout.fragment_wishlists_edit) {

    private lateinit var binding: FragmentWishlistsEditBinding
    private val viewModel: WishlistsEditFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistsEditBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.operationState.observe(viewLifecycleOwner) {
            when (it) {
                WishlistsEditFragmentViewModel.OperationState.NotSet -> {
                    binding.saveButton.isVisible = true
                    binding.waitButton.isVisible = false
                }
                WishlistsEditFragmentViewModel.OperationState.Loading -> {
                    binding.saveButton.isVisible = false
                    binding.waitButton.isVisible = true
                }
                WishlistsEditFragmentViewModel.OperationState.Success -> {
                    dismissWithToast(viewModel.getToastText())
                    setFragmentResult(
                        fragment = viewModel.getFragmentResultRequestKey(),
                        key = RESULT,
                        value = SUCCESS
                    )
                }
                WishlistsEditFragmentViewModel.OperationState.Error -> {
                    binding.saveButton.isVisible = true
                    binding.waitButton.isVisible = false
                    requireContext().showErrorAlert()
                }
            }
        }
        binding.saveButton.setOnClickListener {
            viewModel.createOrUpdateWishlist(
                title = binding.titleInput.text.toString(),
                description = binding.descriptionInput.text.toString()
            )
        }
        viewModel.fillFieldsFromCache(
            binding,
            editTitle = getString(R.string.wishlist_edit_title)
        )
    }
}