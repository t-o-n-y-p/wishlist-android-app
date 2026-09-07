package ru.otus.wishlist.fragment.gifts.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentGiftsEditBinding
import ru.otus.wishlist.fragment.RESULT
import ru.otus.wishlist.fragment.SUCCESS
import ru.otus.wishlist.fragment.dismissWithToast
import ru.otus.wishlist.fragment.setFragmentResult
import ru.otus.wishlist.fragment.showErrorAlert

@AndroidEntryPoint
class GiftsEditFragment : BottomSheetDialogFragment(R.layout.fragment_gifts_edit) {

    private lateinit var binding: FragmentGiftsEditBinding
    private val viewModel: GiftsEditFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGiftsEditBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.operationState.observe(viewLifecycleOwner) {
            when (it) {
                GiftsEditFragmentViewModel.OperationState.NotSet -> {
                    binding.saveButton.isVisible = true
                    binding.waitButton.isVisible = false
                }
                GiftsEditFragmentViewModel.OperationState.Loading -> {
                    binding.saveButton.isVisible = false
                    binding.waitButton.isVisible = true
                }
                GiftsEditFragmentViewModel.OperationState.Success -> {
                    dismissWithToast(viewModel.getToastText())
                    setFragmentResult(
                        fragment = viewModel.getFragmentResultRequestKey(),
                        key = RESULT,
                        value = SUCCESS
                    )
                }
                GiftsEditFragmentViewModel.OperationState.Error -> {
                    binding.saveButton.isVisible = true
                    binding.waitButton.isVisible = false
                    requireContext().showErrorAlert()
                }
            }
        }
        binding.saveButton.setOnClickListener {
            viewModel.createOrUpdateGift(
                name = binding.nameInput.text.toString(),
                description = binding.descriptionInput.text.toString(),
                price = binding.priceInput.text.toString().toInt()
            )
        }
        viewModel.fillFieldsFromCache(
            binding,
            editTitle = getString(R.string.gift_edit_title)
        )
    }
}