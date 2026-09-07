package ru.otus.wishlist.fragment.users

import dagger.hilt.android.scopes.ViewModelScoped
import ru.otus.wishlist.NetworkCallProcessor
import ru.otus.wishlist.api.models.User
import ru.otus.wishlist.service.UsersService
import javax.inject.Inject

@ViewModelScoped
class UsersFragmentUseCase @Inject constructor(
    private val networkCallProcessor: NetworkCallProcessor,
    private val usersService: UsersService
) {

    suspend fun getAllUsers(): Result<List<User>> =
        networkCallProcessor.call {
            usersService.getAllUsers()
        }
}