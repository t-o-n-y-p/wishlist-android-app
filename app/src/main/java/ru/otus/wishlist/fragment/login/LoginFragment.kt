package ru.otus.wishlist.fragment.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentLoginBinding
import ru.otus.wishlist.fragment.showErrorAlert

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loginState.observe(viewLifecycleOwner) {
            when (it) {
                LoginFragmentViewModel.LoginState.NotSet -> {
                    binding.logInButton.isVisible = true
                    binding.waitButton.isVisible = false
                }
                LoginFragmentViewModel.LoginState.Loading -> {
                    binding.logInButton.isVisible = false
                    binding.waitButton.isVisible = true
                }
                LoginFragmentViewModel.LoginState.Success -> {
                    findNavController().navigate(R.id.go_to_app_main)
                }
                LoginFragmentViewModel.LoginState.Error -> {
                    binding.logInButton.isVisible = true
                    binding.waitButton.isVisible = false
                    requireContext().showErrorAlert()
                }
            }
        }
        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.go_to_register)
        }
        binding.logInButton.setOnClickListener {
            viewModel.login(
                username = binding.usernameTextInput.text.toString(),
                password = binding.passwordTextInput.text.toString()
            )
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {}
        viewModel.setSessionId(binding)
    }
}