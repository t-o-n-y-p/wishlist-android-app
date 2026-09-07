package ru.otus.wishlist.fragment.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterFragmentViewModel @Inject constructor(
    private val useCase: RegisterFragmentUseCase
) : ViewModel() {

    private val mRegisterState = MutableLiveData<RegisterState>(RegisterState.NotSet)
    val registerState: LiveData<RegisterState>
        get() = mRegisterState

    fun register(username: String, email: String, password: String) =
        viewModelScope.launch {
            try {
                mRegisterState.value = RegisterState.Loading
                useCase.register(
                    username = username,
                    email = email,
                    password = password
                ).getOrThrow()
                mRegisterState.value = RegisterState.Success
            } catch (_: Throwable) {
                mRegisterState.value = RegisterState.Error
            }
        }

    sealed class RegisterState {

        data object NotSet: RegisterState()

        data object Loading: RegisterState()

        data object Success: RegisterState()

        data object Error : RegisterState()
    }
}