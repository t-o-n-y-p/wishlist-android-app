package ru.otus.wishlist.service

import retrofit2.Response
import retrofit2.http.GET
import ru.otus.wishlist.api.models.User

interface UsersService {

    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>
}