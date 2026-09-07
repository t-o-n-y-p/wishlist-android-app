package ru.otus.wishlist.fragment.users.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentUsersFilterBinding
import ru.otus.wishlist.fragment.FRAGMENT_USERS_FILTER
import ru.otus.wishlist.fragment.RESULT
import ru.otus.wishlist.fragment.SUCCESS
import ru.otus.wishlist.fragment.dismissWithToast
import ru.otus.wishlist.fragment.setFragmentResult

@AndroidEntryPoint
class UsersFilterFragment : BottomSheetDialogFragment(R.layout.fragment_users_filter) {

    private lateinit var binding: FragmentUsersFilterBinding
    private val viewModel: UsersFilterFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersFilterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.applyButton.setOnClickListener {
            viewModel.applyUsersFilter(binding)
            dismissWithToast(viewModel.getToastText())
            setFragmentResult(
                fragment = FRAGMENT_USERS_FILTER,
                key = RESULT,
                value = SUCCESS
            )
        }
        viewModel.fillFieldsFromCache(binding)
    }
}