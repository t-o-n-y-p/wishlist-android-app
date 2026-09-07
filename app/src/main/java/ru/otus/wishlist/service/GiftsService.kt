package ru.otus.wishlist.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ru.otus.wishlist.api.models.Gift
import ru.otus.wishlist.api.models.GiftDto

interface GiftsService {

    @PUT("gifts/{id}")
    suspend fun updateGift(
        @Path("id") id: String, @Body requestBody: GiftDto
    ) : Response<Gift>

    @DELETE("gifts/{id}")
    suspend fun deleteGift(@Path("id") id: String): Response<Unit>

    @POST("gifts/wishlist/{wishlistId}")
    suspend fun createGift(
        @Path("wishlistId") wishlistId: String, @Body requestBody: GiftDto
    ): Response<Gift>

    @PUT("gifts/{id}/reserve")
    suspend fun reserveGift(@Path("id") id: String): Response<Gift>
}