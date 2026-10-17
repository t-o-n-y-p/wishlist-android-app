package ru.otus.wishlist.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ru.otus.wishlist.api.models.Wishlist
import ru.otus.wishlist.api.models.WishlistDto

interface WishlistsService {

    @GET("wishlists") suspend fun getAllWishlists(): Response<List<Wishlist>>

    @POST("wishlists")
    suspend fun createWishlist(@Body requestBody: WishlistDto): Response<Wishlist>

    @PUT("wishlists/{id}")
    suspend fun updateWishlist(
        @Path("id") id: String,
        @Body requestBody: WishlistDto,
    ): Response<Wishlist>

    @DELETE("wishlists/{id}")
    suspend fun deleteWishlist(@Path("id") id: String): Response<Unit>

    @GET("wishlists/user/{userId}")
    suspend fun getUserWishlists(@Path("userId") userId: String): Response<List<Wishlist>>
}
