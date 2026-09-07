package ru.otus.wishlist.service

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import ru.otus.wishlist.storage.UserPreferences
import ru.otus.wishlist.storage.get
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder()
                .header(
                    "Authorization",
                    sharedPreferences
                        .get<UserPreferences>()
                        .getAuthHeaderValue()
                )
                .build())
}