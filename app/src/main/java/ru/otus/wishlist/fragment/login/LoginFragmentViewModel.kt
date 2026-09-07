package ru.otus.wishlist.fragment.login

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.otus.wishlist.databinding.FragmentLoginBinding
import ru.otus.wishlist.storage.SessionPreferences
import ru.otus.wishlist.storage.UserPreferences
import ru.otus.wishlist.storage.get
import ru.otus.wishlist.storage.put
import javax.inject.Inject

@HiltViewModel
class LoginFragmentViewModel @Inject constructor(
    private val useCase: LoginFragmentUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val mLoginState = MutableLiveData<LoginState>(LoginState.NotSet)
    val loginState: LiveData<LoginState>
        get() = mLoginState

    fun login(username: String, password: String) =
        viewModelScope.launch {
            try {
                mLoginState.value = LoginState.Loading
                val tokenResponse = useCase.login(
                    username = username,
                    password = password
                ).getOrThrow()
                sharedPreferences.put(
                    UserPreferences(username = username, token = tokenResponse.token.orEmpty())
                )
                mLoginState.value = LoginState.Success
            } catch (_: Throwable) {
                mLoginState.value = LoginState.Error
            }
        }

    fun setSessionId(binding: FragmentLoginBinding) {
        binding.sessionIdValue.text = sharedPreferences.get<SessionPreferences>().sessionId
    }

    sealed class LoginState {

        data object NotSet: LoginState()

        data object Loading: LoginState()

        data object Success: LoginState()

        data object Error : LoginState()
    }
}