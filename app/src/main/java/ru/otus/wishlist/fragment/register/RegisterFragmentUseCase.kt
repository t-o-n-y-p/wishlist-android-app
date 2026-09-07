package ru.otus.wishlist.fragment.register

import dagger.hilt.android.scopes.ViewModelScoped
import ru.otus.wishlist.NetworkCallProcessor
import ru.otus.wishlist.api.models.UserRegistrationDto
import ru.otus.wishlist.service.AuthService
import javax.inject.Inject

@ViewModelScoped
class RegisterFragmentUseCase @Inject constructor(
    private val networkCallProcessor: NetworkCallProcessor,
    private val authService: AuthService
) {

    suspend fun register(username: String, email: String, password: String): Result<Unit> =
        networkCallProcessor.call {
            val dto = UserRegistrationDto(
                username = username,
                email = email,
                password = password
            )
            authService.register(dto)
        }
}