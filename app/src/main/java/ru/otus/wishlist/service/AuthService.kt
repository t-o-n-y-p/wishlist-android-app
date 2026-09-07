package ru.otus.wishlist.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.otus.wishlist.api.models.Login
import ru.otus.wishlist.api.models.LoginDto
import ru.otus.wishlist.api.models.UserRegistrationDto

interface AuthService {

    @POST("auth/register")
    suspend fun register(@Body requestBody: UserRegistrationDto): Response<Unit>

    @POST("auth/login")
    suspend fun login(@Body requestBody: LoginDto): Response<Login>
}