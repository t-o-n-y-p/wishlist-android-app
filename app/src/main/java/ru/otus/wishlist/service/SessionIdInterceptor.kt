package ru.otus.wishlist.service

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import ru.otus.wishlist.storage.SessionPreferences
import ru.otus.wishlist.storage.get
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionIdInterceptor @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder()
                .header("Session-ID",
                    sharedPreferences
                        .get<SessionPreferences>()
                        .sessionId
                )
                .build())
}