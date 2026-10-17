package ru.otus.wishlist.fragment.users

import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import ru.otus.wishlist.NetworkCallProcessor
import ru.otus.wishlist.api.models.User
import ru.otus.wishlist.service.UsersService

@ViewModelScoped
class UsersFragmentUseCase
@Inject
constructor(
    private val networkCallProcessor: NetworkCallProcessor,
    private val usersService: UsersService,
) {

    suspend fun getAllUsers(): Result<List<User>> = networkCallProcessor.call {
        usersService.getAllUsers()
    }
}
