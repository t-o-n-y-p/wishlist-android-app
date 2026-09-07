package ru.otus.wishlist.fragment.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentRegisterBinding
import ru.otus.wishlist.fragment.showErrorAlert

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.registerState.observe(viewLifecycleOwner) {
            when (it) {
                RegisterFragmentViewModel.RegisterState.NotSet -> {
                    binding.registerButton.isVisible = true
                    binding.waitButton.isVisible = false
                }
                RegisterFragmentViewModel.RegisterState.Loading -> {
                    binding.registerButton.isVisible = false
                    binding.waitButton.isVisible = true
                }
                RegisterFragmentViewModel.RegisterState.Success -> {
                    findNavController().navigate(R.id.go_to_login)
                    MaterialAlertDialogBuilder(requireActivity(), R.style.Alert)
                        .setTitle(getString(R.string.successful_registration))
                        .setMessage(getString(R.string.now_you_can_log_in))
                        .setPositiveButton(getString(R.string.ok)) { _, _ -> }
                        .show()
                }
                RegisterFragmentViewModel.RegisterState.Error -> {
                    binding.registerButton.isVisible = true
                    binding.waitButton.isVisible = false
                    requireContext().showErrorAlert()
                }
            }
        }
        binding.logInButton.setOnClickListener {
            findNavController().navigate(R.id.go_to_login)
        }
        binding.registerButton.setOnClickListener {
            viewModel.register(
                username = binding.usernameTextInput.text.toString(),
                email = binding.emailTextInput.text.toString(),
                password = binding.passwordTextInput.text.toString()
            )
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.go_to_login)
        }
    }
}